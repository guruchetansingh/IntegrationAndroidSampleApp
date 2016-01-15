package com.payu.testapp.hash;

import android.os.AsyncTask;
import android.util.Log;

import com.payu.india.Model.PaymentParams;
import com.payu.india.Model.PayuHashes;
import com.payu.india.Payu.PayuConstants;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Iterator;

/**
 * Created by guruchetansingh on 1/5/16.
 */

/**
 * This class is used to get all hashes from server(merchant server).
 */
public class GetHashesFromServerTask extends AsyncTask<PaymentParams, String, PayuHashes> {



    private  HashGenerationCallBack hashGenerationCallBack;
    private String urlForHash;

    public GetHashesFromServerTask(HashGenerationCallBack hashGenerationCallBack,String urlForHash)
    {
        Log.i("GetData", "GetHashesFromServerTask ");
        this.hashGenerationCallBack=hashGenerationCallBack;
        this.urlForHash=urlForHash;
    }
    @Override
    protected PayuHashes doInBackground(PaymentParams... paymentParams) {

        /*
        * This payuHashes object is used to capture all the hashes in itself.
        * */
        PayuHashes payuHashes = new PayuHashes();
        try {

            //here you need to put your hash generation URL
            URL url = new URL(urlForHash);


            String postParam = createPostParamsForHashFromServer(paymentParams[0]);

            byte[] postParamsByte = postParam.getBytes("UTF-8");

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Content-Length", String.valueOf(postParamsByte.length));
            conn.setDoOutput(true);
            conn.getOutputStream().write(postParamsByte);

            InputStream responseInputStream = conn.getInputStream();
            StringBuffer responseStringBuffer = new StringBuffer();
            byte[] byteContainer = new byte[1024];
            for (int i; (i = responseInputStream.read(byteContainer)) != -1; ) {
                responseStringBuffer.append(new String(byteContainer, 0, i));
            }

            JSONObject response = new JSONObject(responseStringBuffer.toString());

            Log.i("GetData", "response = "+response);
            Iterator<String> payuHashIterator = response.keys();
            while (payuHashIterator.hasNext()) {
                String key = payuHashIterator.next();
                switch (key) {
                    case "payment_hash":
                        payuHashes.setPaymentHash(response.getString(key));
                        break;
                    case "get_merchant_ibibo_codes_hash":
                        payuHashes.setMerchantIbiboCodesHash(response.getString(key));
                        break;
                    case "vas_for_mobile_sdk_hash":
                        payuHashes.setVasForMobileSdkHash(response.getString(key));
                        break;
                    case "payment_related_details_for_mobile_sdk_hash":
                        payuHashes.setPaymentRelatedDetailsForMobileSdkHash(response.getString(key));
                        break;
                    case "delete_user_card_hash":
                        payuHashes.setDeleteCardHash(response.getString(key));
                        break;
                    case "get_user_cards_hash":
                        payuHashes.setStoredCardsHash(response.getString(key));
                        break;
                    case "edit_user_card_hash":
                        payuHashes.setEditCardHash(response.getString(key));
                        break;
                    case "save_user_card_hash":
                        payuHashes.setSaveCardHash(response.getString(key));
                        break;
                    case "check_offer_status_hash":
                        payuHashes.setCheckOfferStatusHash(response.getString(key));
                        break;
                    case "check_isDomestic_hash":
                        payuHashes.setCheckIsDomesticHash(response.getString(key));
                        break;
                    case "verify_payment_hash":
                        payuHashes.setVerifyPaymentHash(response.getString(key));
                        break;
                    default:
                        break;
                }
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return payuHashes;
    }

    @Override
    protected void onPostExecute(PayuHashes payuHashes) {
        super.onPostExecute(payuHashes);
        hashGenerationCallBack.hashGenerationAPIResponse(payuHashes);
    }


    private String concatParams(String key, String value) {
        return key + "=" + value + "&";
    }

    private String createPostParamsForHashFromServer(PaymentParams mPaymentParams)
    {
        // lets create the post params
        StringBuffer postParamsBuffer = new StringBuffer();
        postParamsBuffer.append(concatParams(PayuConstants.KEY, mPaymentParams.getKey()));
        postParamsBuffer.append(concatParams(PayuConstants.AMOUNT, mPaymentParams.getAmount()));
        postParamsBuffer.append(concatParams(PayuConstants.TXNID, mPaymentParams.getTxnId()));
        postParamsBuffer.append(concatParams(PayuConstants.EMAIL, null == mPaymentParams.getEmail() ? "" : mPaymentParams.getEmail()));
        postParamsBuffer.append(concatParams(PayuConstants.PRODUCT_INFO, mPaymentParams.getProductInfo()));
        postParamsBuffer.append(concatParams(PayuConstants.FIRST_NAME, null == mPaymentParams.getFirstName() ? "" : mPaymentParams.getFirstName()));
        postParamsBuffer.append(concatParams(PayuConstants.UDF1, mPaymentParams.getUdf1() == null ? "" : mPaymentParams.getUdf1()));
        postParamsBuffer.append(concatParams(PayuConstants.UDF2, mPaymentParams.getUdf2() == null ? "" : mPaymentParams.getUdf2()));
        postParamsBuffer.append(concatParams(PayuConstants.UDF3, mPaymentParams.getUdf3() == null ? "" : mPaymentParams.getUdf3()));
        postParamsBuffer.append(concatParams(PayuConstants.UDF4, mPaymentParams.getUdf4() == null ? "" : mPaymentParams.getUdf4()));
        postParamsBuffer.append(concatParams(PayuConstants.UDF5, mPaymentParams.getUdf5() == null ? "" : mPaymentParams.getUdf5()));
        postParamsBuffer.append(concatParams(PayuConstants.USER_CREDENTIALS, mPaymentParams.getUserCredentials() == null ? PayuConstants.DEFAULT : mPaymentParams.getUserCredentials()));

        // for offer_key
        if (null != mPaymentParams.getOfferKey())
            postParamsBuffer.append(concatParams(PayuConstants.OFFER_KEY, mPaymentParams.getOfferKey()));
        // for check_isDomestic
        if (null != mPaymentParams.getCardBin())
            postParamsBuffer.append(concatParams(PayuConstants.CARD_BIN, mPaymentParams.getCardBin()));

        String postParams = postParamsBuffer.charAt(postParamsBuffer.length() - 1) == '&' ? postParamsBuffer.substring(0, postParamsBuffer.length() - 1).toString() : postParamsBuffer.toString();

        return postParams;
    }
}

