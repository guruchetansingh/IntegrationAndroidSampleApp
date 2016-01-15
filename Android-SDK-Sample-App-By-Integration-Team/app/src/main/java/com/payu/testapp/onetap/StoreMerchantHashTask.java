package com.payu.testapp.onetap;

import android.os.AsyncTask;

import com.payu.india.Model.PaymentParams;
import com.payu.india.Payu.PayuConstants;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;


/**
 * Created by guruchetansingh on 1/7/16.
 */

/**
 * This class is used to store merchant hash and card token on merchant's server.
 */
public class StoreMerchantHashTask extends AsyncTask<Void, Void, String> {

    private StoreMerchantHashCallBack storeMerchantHashCallBack;
    private String postParams;
    private String cardToken;
    private String oneClickHash;
    private PaymentParams paymentParams;
    private String urlForStoreMerchantHash;

    public StoreMerchantHashTask(StoreMerchantHashCallBack storeMerchantHashCallBack, String urlForStoreMerchantHash, String cardToken, String oneClickHash, PaymentParams paymentParams) {
        this.storeMerchantHashCallBack = storeMerchantHashCallBack;
        this.urlForStoreMerchantHash = urlForStoreMerchantHash;
        this.cardToken = cardToken;
        this.oneClickHash = oneClickHash;
        this.paymentParams = paymentParams;
        this.postParams = PayuConstants.MERCHANT_KEY + "=" + paymentParams.getKey() + "&" + PayuConstants.USER_CREDENTIALS + "=" + paymentParams.getUserCredentials() + "&" + PayuConstants.CARD_TOKEN + "=" + cardToken + "&" + PayuConstants.MERCHANT_HASH + "=" + oneClickHash;
    }

    @Override
    protected String doInBackground(Void... params) {

        try {
            URL url = new URL(urlForStoreMerchantHash);

            byte[] postParamsByte = postParams.getBytes("UTF-8");

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
            return response.toString();

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String response) {
        super.onPostExecute(response);
        storeMerchantHashCallBack.storeMerchantHashAPIResponse(response);
    }
}
