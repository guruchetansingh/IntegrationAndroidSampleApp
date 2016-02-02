package com.payu.payuui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.payu.custombrowser.Bank;
import com.payu.custombrowser.PayUWebChromeClient;
import com.payu.custombrowser.PayUWebViewClient;
import com.payu.india.Extras.PayUSdkDetails;
import com.payu.india.Model.PayuConfig;
import com.payu.india.Payu.PayuConstants;
import com.payu.india.Payu.PayuUtils;
import com.payu.magicretry.Helpers.Util;
import com.payu.magicretry.MagicRetryFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class PaymentsActivity extends AppCompatActivity implements MagicRetryFragment.ActivityCallback{

    Bundle bundle;
    String url;
    boolean cancelTransaction = false;
    PayuConfig payuConfig;
    private BroadcastReceiver mReceiver = null;
    private  boolean viewPortWide = false;
    private WebView mWebView;
    private PayuUtils mPayuUtils;

    private int storeOneClickHash;

    private String merchantHash;
    MagicRetryFragment magicRetryFragment;
    String txnId = null;


    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /**
         * when the device running out of memory we don't want the user to restart the payment. rather we close it and redirect them to previous activity.
         */

        if(savedInstanceState!=null){
            super.onCreate(null);
            finish();//call activity u want to as activity is being destroyed it is restarted
        }else {
            super.onCreate(savedInstanceState);
        }
        setContentView(R.layout.activity_payments);
        mWebView = (WebView) findViewById(R.id.webview);

        //WebView.setWebContentsDebuggingEnabled(true);
        mPayuUtils = new PayuUtils();

        //region Replace the whole code by the commented code if you are NOT using custombrowser
        // Replace the whole code by the commented code if you are NOT using custombrowser.

        bundle = getIntent().getExtras();
        payuConfig = bundle.getParcelable(PayuConstants.PAYU_CONFIG);
        storeOneClickHash = bundle.getInt(PayuConstants.STORE_ONE_CLICK_HASH);
        mWebView = (WebView) findViewById(R.id.webview);

        switch (payuConfig.getEnvironment()) {
            case PayuConstants.PRODUCTION_ENV:
                url = PayuConstants.PRODUCTION_PAYMENT_URL;
                break;
            case PayuConstants.MOBILE_STAGING_ENV:
                url = PayuConstants.MOBILE_TEST_PAYMENT_URL;
                break;
            case PayuConstants.STAGING_ENV:
                url = PayuConstants.TEST_PAYMENT_URL;
                break;
            default:
                url = PayuConstants.PRODUCTION_PAYMENT_URL;
                break;
        }



        String [] list =  payuConfig.getData().split("&");

        String merchantKey = null;
        for (String item : list) {
            String[] items = item.split("=");
            if(items.length >= 2) {
                String id = items[0];
                switch (id) {
                    case "txnid":
                        txnId = items[1];
                        break;
                    case "key":
                        merchantKey = items[1];
                        break;
                    case "pg":
                        if (items[1].contentEquals("NB")) {
                            viewPortWide = true;
                        }
                        break;

                }
            }
        }

        try {
            Class.forName("com.payu.custombrowser.Bank");
            final Bank bank = new Bank() {
                @Override
                public void registerBroadcast(BroadcastReceiver broadcastReceiver, IntentFilter filter) {
                    mReceiver = broadcastReceiver;
                    registerReceiver(broadcastReceiver, filter);
                }

                @Override
                public void unregisterBroadcast(BroadcastReceiver broadcastReceiver) {
                    if(mReceiver != null){
                        unregisterReceiver(mReceiver);
                        mReceiver = null;
                    }
                }

                @Override
                public void onHelpUnavailable() {
                    findViewById(R.id.parent).setVisibility(View.GONE);
                    findViewById(R.id.trans_overlay).setVisibility(View.GONE);
                }

                @Override
                public void onBankError() {
                    findViewById(R.id.parent).setVisibility(View.GONE);
                    findViewById(R.id.trans_overlay).setVisibility(View.GONE);
                }

                @Override
                public void onHelpAvailable() {
                    findViewById(R.id.parent).setVisibility(View.VISIBLE);
                }
            };
            bank.setArguments(settingBundleForCB(merchantKey));
            findViewById(R.id.parent).bringToFront();
            try {
                getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.fade_in, R.anim.cb_face_out).add(R.id.parent, bank).commit();
            }catch(Exception e)
            {
                e.printStackTrace();
                finish();
            }
            initMagicRetry();

            mWebView.setWebChromeClient(new PayUWebChromeClient(bank));
            mWebView.setWebViewClient(new PayUWebViewClient(bank, magicRetryFragment,merchantKey));
            //mWebView is the WebView Object
            magicRetryFragment.setWebView(mWebView);
            // MR Integration - initMRSettingsFromSharedPreference
            magicRetryFragment.initMRSettingsFromSharedPreference(this);

            Log.i("GetData", "payuConfig.getData() =  " + payuConfig.getData());
            mWebView.postUrl(url, payuConfig.getData().getBytes());
            (new PayuUtils()).deviceAnalytics(PaymentsActivity.this, Bank.Version, merchantKey, txnId);
        } catch (ClassNotFoundException e) {
            mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
            mWebView.getSettings().setSupportMultipleWindows(true);
            mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
            // Setting view port for NB
            if(viewPortWide){
                mWebView.getSettings().setUseWideViewPort(viewPortWide);
            }
            // Hiding the overlay
            View transOverlay = findViewById(R.id.trans_overlay);
            transOverlay.setVisibility(View.GONE);

            mWebView.addJavascriptInterface(new Object() {
                @JavascriptInterface
                public void onSuccess() {
                    onSuccess("");
                }

                @JavascriptInterface
                public void onSuccess(final String result) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            Intent intent = new Intent();
                            intent.putExtra("result", result);
                            if (storeOneClickHash == PayuConstants.STORE_ONE_CLICK_HASH_SERVER && null != merchantHash) {
                                intent.putExtra(PayuConstants.MERCHANT_HASH, merchantHash);
                            }
                            setResult(Activity.RESULT_OK, intent);
                            finish();

                        }
//                }
                    });
                }

                @JavascriptInterface
                public void onFailure() {
                    onFailure("");
                }

                @JavascriptInterface
                public void onFailure(final String result) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent();
                            intent.putExtra("result", result);
                            setResult(RESULT_CANCELED, intent);
                            finish();
                        }
                    });
                }

                @JavascriptInterface
                public void onMerchantHashReceived(final String result) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            switch (storeOneClickHash) {
                                case PayuConstants.STORE_ONE_CLICK_HASH_MOBILE:
                                    try {
                                        JSONObject hashObject = new JSONObject(result);
                                        new PayuUtils().storeInSharedPreferences(PaymentsActivity.this, hashObject.getString(PayuConstants.CARD_TOKEN), hashObject.getString(PayuConstants.MERCHANT_HASH));
                                    } catch (JSONException e) {
                                        e.printStackTrace();

                                    }
                                    break;
                                case PayuConstants.STORE_ONE_CLICK_HASH_SERVER:
                                    merchantHash = result;
                                    break;
                                case PayuConstants.STORE_ONE_CLICK_HASH_NONE:
                                    break;
                            }
                        }
                    });
                }

            }, "PayU");

            mWebView.setWebChromeClient(new WebChromeClient());
            mWebView.setWebViewClient(new WebViewClient());
            mWebView.getSettings().setJavaScriptEnabled(true);
            mWebView.getSettings().setDomStorageEnabled(true);
            Log.i("GetData", "payuConfig.getData() =  " + payuConfig.getData());
            mWebView.postUrl(url, payuConfig.getData().getBytes());

        }

    }


    /**
     * This method is used to arrange all setting necessary for custom browser.
     *
     * @return bundle object
     * */
    private Bundle settingBundleForCB(String merchantKey){
        Bundle args = new Bundle();
        try {


            args.putInt(Bank.WEBVIEW, R.id.webview);
            args.putInt(Bank.TRANS_LAYOUT, R.id.trans_overlay);
            args.putInt(Bank.MAIN_LAYOUT, R.id.r_layout);
            args.putBoolean(Bank.VIEWPORTWIDE, viewPortWide);
            args.putBoolean(Bank.AUTO_SELECT_OTP, true);
            args.putBoolean(Bank.AUTO_SELECT_OTP, false);
            args.putBoolean(Bank.AUTO_APPROVE, true);
            args.putString(Bank.TXN_ID, txnId == null ? String.valueOf(System.currentTimeMillis()) : txnId);
            args.putString(Bank.MERCHANT_KEY, null != merchantKey ? merchantKey : "could not find");
            PayUSdkDetails payUSdkDetails = new PayUSdkDetails();
            args.putString(Bank.SDK_DETAILS, payUSdkDetails.getSdkVersionName());
            // should cb send the merchant hash back to app?
            args.putInt(Bank.STORE_ONE_CLICK_HASH, storeOneClickHash);
            if (getIntent().getExtras().containsKey(Bank.SHOW_CUSTOMROWSER)) {
                args.putBoolean(Bank.SHOW_CUSTOMROWSER, getIntent().getBooleanExtra(Bank.SHOW_CUSTOMROWSER, false));
            }
            args.putBoolean(Bank.SHOW_CUSTOMROWSER, true);

            //for android M
            args.putBoolean(Bank.MERCHANT_SMS_PERMISSION, true);
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        return args;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_payments, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed(){
        if(cancelTransaction){
            cancelTransaction = false;
            Intent intent = new Intent();
            intent.putExtra("result", "Transaction canceled due to back pressed!");
            setResult(RESULT_CANCELED, intent);
            super.onBackPressed();
            return;
        }

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);;
        alertDialog.setCancelable(false);
        alertDialog.setMessage("Do you really want to cancel the transaction ?");
        alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                cancelTransaction = true;
                dialog.dismiss();
                onBackPressed();
            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.show();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    private void initMagicRetry() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        magicRetryFragment = new MagicRetryFragment();
        Bundle newInformationBundle = new Bundle();
        newInformationBundle.putString(MagicRetryFragment.KEY_TXNID, txnId);
        magicRetryFragment.setArguments(newInformationBundle);

        Map<String, String> urlList = new HashMap<String, String>();
        urlList.put(url, payuConfig.getData());
        magicRetryFragment.setUrlListWithPostData(urlList);
        fragmentManager.beginTransaction().add(R.id.magic_retry_container, magicRetryFragment, "magicRetry").commit();
        toggleFragmentVisibility(Util.HIDE_FRAGMENT);
        magicRetryFragment.isWhiteListingEnabled(true);
    }


    public void toggleFragmentVisibility(int flag){
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (!isFinishing()) {
            if (flag == Util.SHOW_FRAGMENT) {
                // Show fragment
                ft.show(magicRetryFragment).commitAllowingStateLoss();
            } else if (flag == Util.HIDE_FRAGMENT) {
                // Hide fragment
                ft.hide(magicRetryFragment).commitAllowingStateLoss();
                // ft.hide(magicRetryFragment);
                Log.v("#### PAYU", "hidhing magic retry");
            }
        }
    }
    @Override
    public void showMagicRetry() {
        toggleFragmentVisibility(Util.SHOW_FRAGMENT);
    }

    @Override
    public void hideMagicRetry() {
        toggleFragmentVisibility(Util.HIDE_FRAGMENT);
    }
}


