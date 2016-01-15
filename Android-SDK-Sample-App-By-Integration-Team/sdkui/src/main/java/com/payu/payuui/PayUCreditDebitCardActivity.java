    package com.payu.payuui;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.payu.india.Interfaces.GetOfferStatusApiListener;
import com.payu.india.Model.MerchantWebService;
import com.payu.india.Model.PaymentParams;
import com.payu.india.Model.PayuConfig;
import com.payu.india.Model.PayuHashes;
import com.payu.india.Model.PayuResponse;
import com.payu.india.Model.PostData;
import com.payu.india.Payu.PayuConstants;
import com.payu.india.Payu.PayuErrors;
import com.payu.india.Payu.PayuUtils;
import com.payu.india.PostParams.MerchantWebServicePostParams;
import com.payu.india.PostParams.PaymentPostParams;
import com.payu.india.Tasks.GetOfferStatusTask;


    public class PayUCreditDebitCardActivity extends AppCompatActivity implements View.OnClickListener,GetOfferStatusApiListener {

    private Button payNowButton;
    private EditText cardNameEditText;
    private EditText cardNumberEditText;
    private EditText cardCvvEditText;
    private EditText cardExpiryMonthEditText;
    private EditText cardExpiryYearEditText;
    private Bundle bundle;
    private CheckBox saveCardCheckBox;
    private CheckBox enableOneClickPaymentCheckBox;

    private String cardName;
    private String cardNumber;
    private String cvv;
    private String expiryMonth;
    private String expiryYear;

    private PayuHashes mPayuHashes;
    private PaymentParams mPaymentParams;
    private PostData postData;
    private Toolbar toolbar;
    private MerchantWebService merchantWebService;

    private TextView amountTextView;
    private TextView transactionIdTextView;
    private PayuConfig payuConfig;

    private PayuUtils payuUtils;

    int storeOneClickHash;
        private boolean offerAvailed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card);

        // todo lets set the toolbar
        /*toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);*/
        
        (payNowButton = (Button) findViewById(R.id.button_card_make_payment)).setOnClickListener(this);

        cardNameEditText = (EditText) findViewById(R.id.edit_text_name_on_card);
        cardNumberEditText = (EditText) findViewById(R.id.edit_text_card_number);
        cardCvvEditText = (EditText) findViewById(R.id.edit_text_card_cvv);
        cardExpiryMonthEditText = (EditText) findViewById(R.id.edit_text_expiry_month);
        cardExpiryYearEditText = (EditText) findViewById(R.id.edit_text_expiry_year);
        saveCardCheckBox = (CheckBox) findViewById(R.id.check_box_save_card);
        enableOneClickPaymentCheckBox = (CheckBox) findViewById(R.id.check_box_enable_one_click_payment);

        bundle = getIntent().getExtras();

        storeOneClickHash = bundle.getInt(PayuConstants.STORE_ONE_CLICK_HASH);
        if(storeOneClickHash == PayuConstants.STORE_ONE_CLICK_HASH_NONE)
            enableOneClickPaymentCheckBox.setVisibility(View.GONE);

        // lets get payment default params and hashes
        mPayuHashes = bundle.getParcelable(PayuConstants.PAYU_HASHES);
        mPaymentParams = bundle.getParcelable(PayuConstants.PAYMENT_PARAMS);
        payuConfig = bundle.getParcelable(PayuConstants.PAYU_CONFIG);
        payuConfig = null != payuConfig ? payuConfig : new PayuConfig();

        (amountTextView = (TextView) findViewById(R.id.text_view_amount)).setText(PayuConstants.AMOUNT + ": " + mPaymentParams.getAmount());
        (transactionIdTextView = (TextView) findViewById(R.id.text_view_transaction_id)).setText(PayuConstants.TXNID + ": " + mPaymentParams.getTxnId());

        // lets not show the save card check box if user credentials is not found!
        if(null == mPaymentParams.getUserCredentials()) {
            saveCardCheckBox.setVisibility(View.GONE);
            enableOneClickPaymentCheckBox.setVisibility(View.GONE);
        }else {
            saveCardCheckBox.setVisibility(View.VISIBLE);
        }
        payuUtils = new PayuUtils();


        cardNumberEditText.addTextChangedListener(new TextWatcher() {
            String issuer;
            Drawable issuerDrawable;
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 5) { // to confirm rupay card we need min 6 digit.
                    if (null == issuer) issuer = payuUtils.getIssuer(charSequence.toString());
                    if (issuer != null && issuer.length() > 1 && issuerDrawable == null) {
                        issuerDrawable = getIssuerDrawable(issuer);
                        if (issuer.contentEquals(PayuConstants.SMAE)) { // hide cvv and expiry
                            cardExpiryMonthEditText.setVisibility(View.GONE);
                            cardExpiryYearEditText.setVisibility(View.GONE);
                            cardCvvEditText.setVisibility(View.GONE);
                        } else { //show cvv and expiry
                            cardExpiryMonthEditText.setVisibility(View.VISIBLE);
                            cardExpiryYearEditText.setVisibility(View.VISIBLE);
                            cardCvvEditText.setVisibility(View.VISIBLE);
                        }
                    }
                    if (charSequence.length() > 12) {

                            if (new PayuUtils().validateCardNumber(charSequence.toString())) {
                                if (!(offerAvailed)) {
                                    offerAvailed = true;
                                    // Common for all the flow.
                                    merchantWebService = new MerchantWebService();
                                    merchantWebService.setKey(mPaymentParams.getKey());
                                    merchantWebService.setCommand(PayuConstants.CHECK_OFFER_STATUS);
                                    merchantWebService.setHash(mPayuHashes.getCheckOfferStatusHash());
                                    merchantWebService.setVar1(mPaymentParams.getOfferKey());
                                    merchantWebService.setVar2(mPaymentParams.getAmount());

                                    merchantWebService.setVar3("CC");
                                    merchantWebService.setVar4(cardNumberEditText.getText().toString().startsWith("4") ? "VISA" : "MAST");
                                    // Required only for new card
                                    merchantWebService.setVar5(cardNumberEditText.getText().toString());
                                    // Optional
                                    merchantWebService.setVar6(cardNameEditText.getText().toString());

                                    merchantWebService.setVar7(mPaymentParams.getPhone());
                                    merchantWebService.setVar8(mPaymentParams.getEmail());
                                    merchantWebService.setVar9("");

                                    postData = new MerchantWebServicePostParams(merchantWebService).getMerchantWebServicePostParams();

                                    if (postData.getCode() == PayuErrors.NO_ERROR) {
                                        payuConfig.setData(postData.getResult());

                                        GetOfferStatusTask getOfferStatusTask = new GetOfferStatusTask(PayUCreditDebitCardActivity.this);
                                        getOfferStatusTask.execute(payuConfig);

                                    } else {
                                        Toast.makeText(PayUCreditDebitCardActivity.this, postData.getResult(), Toast.LENGTH_LONG).show();
                                    }
                                }
                            }
                        }


                }else{
                    issuer = null;
                    issuerDrawable = null;
                }
                cardNumberEditText.setCompoundDrawablesWithIntrinsicBounds(null, null, issuerDrawable, null);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_card, menu);
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
    public void onClick(View v) {
        // Oh crap! Resource IDs cannot be used in a switch statement in Android library modules less... (Ctrl+F1)
        // Validates using resource IDs in a switch statement in Android library module
        // we cant not use switch and gotta use simple if else
        if (v.getId() == R.id.button_card_make_payment) {

            // do i have to store the card
            if (saveCardCheckBox.isChecked()) {
                mPaymentParams.setStoreCard(1);
            }else{
                mPaymentParams.setStoreCard(0);
            }

            // do i have to store the cvv
            if (enableOneClickPaymentCheckBox.isChecked()) {
                mPaymentParams.setEnableOneClickPayment(1);
            }else{
                mPaymentParams.setEnableOneClickPayment(0);
            }

            // setup the hash
            mPaymentParams.setHash(mPayuHashes.getPaymentHash());

            // lets try to get the post params
            postData = null;
            // lets get the current card number;
            cardNumber = String.valueOf(cardNumberEditText.getText());
            cardName = cardNameEditText.getText().toString();
            expiryMonth = cardExpiryMonthEditText.getText().toString();
            expiryYear = cardExpiryYearEditText.getText().toString();
            cvv = cardCvvEditText.getText().toString();

            // lets not worry about ui validations.
            mPaymentParams.setCardNumber(cardNumber);
            mPaymentParams.setCardName(cardName);
            mPaymentParams.setNameOnCard(cardName);
            mPaymentParams.setExpiryMonth(expiryMonth);
            mPaymentParams.setExpiryYear(expiryYear);
            mPaymentParams.setCvv(cvv);
            postData = new PaymentPostParams(mPaymentParams, PayuConstants.CC).getPaymentPostParams();
            if (postData.getCode() == PayuErrors.NO_ERROR) {
                // okay good to go.. lets make a transaction
                // launch webview
                String result = postData.getResult();
                payuConfig.setData(postData.getResult());
                Intent intent = new Intent(this, PaymentsActivity.class);
                intent.putExtra(PayuConstants.PAYU_CONFIG, payuConfig);
                intent.putExtra(PayuConstants.STORE_ONE_CLICK_HASH, storeOneClickHash);
                startActivityForResult(intent, PayuConstants.PAYU_REQUEST_CODE);
            } else {
                Toast.makeText(this, postData.getResult(), Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, postData.getResult(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PayuConstants.PAYU_REQUEST_CODE) {
            setResult(resultCode, data);
            finish();
        }
    }

    private Drawable getIssuerDrawable(String issuer){

        if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
            switch (issuer) {
                case PayuConstants.VISA:
                    return getResources().getDrawable(R.drawable.visa);
                case PayuConstants.LASER:
                    return getResources().getDrawable(R.drawable.laser);
                case PayuConstants.DISCOVER:
                    return getResources().getDrawable(R.drawable.discover);
                case PayuConstants.MAES:
                    return getResources().getDrawable(R.drawable.maestro);
                case PayuConstants.MAST:
                    return getResources().getDrawable(R.drawable.master);
                case PayuConstants.AMEX:
                    return getResources().getDrawable(R.drawable.amex);
                case PayuConstants.DINR:
                    return getResources().getDrawable(R.drawable.diner);
                case PayuConstants.JCB:
                    return getResources().getDrawable(R.drawable.jcb);
                case PayuConstants.SMAE:
                    return getResources().getDrawable(R.drawable.maestro);
                case PayuConstants.RUPAY:
                    return getResources().getDrawable(R.drawable.rupay);
            }
            return null;
        }else {

            switch (issuer) {
                case PayuConstants.VISA:
                    return getResources().getDrawable(R.drawable.visa, null);
                case PayuConstants.LASER:
                    return getResources().getDrawable(R.drawable.laser, null);
                case PayuConstants.DISCOVER:
                    return getResources().getDrawable(R.drawable.discover, null);
                case PayuConstants.MAES:
                    return getResources().getDrawable(R.drawable.maestro, null);
                case PayuConstants.MAST:
                    return getResources().getDrawable(R.drawable.master, null);
                case PayuConstants.AMEX:
                    return getResources().getDrawable(R.drawable.amex, null);
                case PayuConstants.DINR:
                    return getResources().getDrawable(R.drawable.diner, null);
                case PayuConstants.JCB:
                    return getResources().getDrawable(R.drawable.jcb, null);
                case PayuConstants.SMAE:
                    return getResources().getDrawable(R.drawable.maestro, null);
                case PayuConstants.RUPAY:
                    return getResources().getDrawable(R.drawable.rupay, null);
            }
            return null;
        }
    }

        @Override
        public void onGetOfferStatusApiResponse(PayuResponse payuResponse) {
           // Toast.makeText(this, "Response status: " + payuResponse.getResponseStatus().getResult() + ": Discount = " + payuResponse.getPayuOffer().getDiscount(), Toast.LENGTH_LONG).show();
            try {
                Double finalAmount = Double.parseDouble(mPaymentParams.getAmount()) - Double.parseDouble(payuResponse.getPayuOffer().getDiscount());
                String discountedAmount = String.valueOf(finalAmount);
                amountTextView.setText(discountedAmount);
            }catch (Exception e){e.printStackTrace();}

        }
    }
