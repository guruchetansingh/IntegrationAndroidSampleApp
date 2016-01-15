package com.payu.testapp.onetap;

import android.os.AsyncTask;

import com.payu.india.Model.PaymentParams;
import com.payu.india.Payu.PayuConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;

/**
 * Created by guruchetansingh on 1/7/16.
 */

/**
 * This class is used to fetch all merchant hashes and card tokens from merchant's server
 */
public class FetchMerchantHashes extends AsyncTask<Void, Void, HashMap<String, String>> {

    private FetchMerchantHashesCallBack fetchMerchantHashesCallBack;
    private String postParams;
    private String urlForFetchingMerchantHash;

    public FetchMerchantHashes(FetchMerchantHashesCallBack fetchMerchantHashesCallBack, String urlForFetchingMerchantHash, String merchantKey, String userCredentials) {

        this.fetchMerchantHashesCallBack = fetchMerchantHashesCallBack;
        this.urlForFetchingMerchantHash = urlForFetchingMerchantHash;
        this.postParams = PayuConstants.MERCHANT_KEY + "=" + merchantKey + "&" + PayuConstants.USER_CREDENTIALS + "=" + userCredentials;
    }


    public FetchMerchantHashes(FetchMerchantHashesCallBack fetchMerchantHashesCallBack, String urlForFetchingMerchantHash, PaymentParams paymentParams) {
        this(fetchMerchantHashesCallBack, urlForFetchingMerchantHash, paymentParams.getKey(), paymentParams.getUserCredentials());

    }


    @Override
    protected HashMap<String, String> doInBackground(Void... params) {
        try {

            URL url = new URL(urlForFetchingMerchantHash);

            byte[] postParamsByte = postParams.getBytes("UTF-8");

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
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

            HashMap<String, String> cardTokens = new HashMap<String, String>();
            JSONArray oneClickCardsArray = response.getJSONArray("data");
            int arrayLength;
            if ((arrayLength = oneClickCardsArray.length()) >= 1) {
                for (int i = 0; i < arrayLength; i++) {
                    cardTokens.put(oneClickCardsArray.getJSONArray(i).getString(0), oneClickCardsArray.getJSONArray(i).getString(1));
                }
                return cardTokens;
            }


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
    protected void onPostExecute(HashMap<String, String> oneClickTokens) {
        super.onPostExecute(oneClickTokens);

        fetchMerchantHashesCallBack.fetchMerchantHashesAPIResponse(oneClickTokens);
    }
}
