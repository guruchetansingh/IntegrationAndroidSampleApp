package com.payu.testapp.onetap;

import android.os.AsyncTask;

import com.payu.india.Model.StoredCard;
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
 * This class is used to delete merchant hash and card token on merchant's server.
 */
public class DeleteMerchantHashTask extends AsyncTask<Void, Void, String> {

    private DeleteMerchantHashCallBack deleteMerchantHashCallBack;
    private String postParams;
    private String cardToken;
    private String urlForDeleteMerchantHash;

    public DeleteMerchantHashTask(DeleteMerchantHashCallBack deleteMerchantHashCallBack, String urlForDeleteMerchantHash, String cardToken) {
        this.deleteMerchantHashCallBack = deleteMerchantHashCallBack;
        this.urlForDeleteMerchantHash = urlForDeleteMerchantHash;
        this.cardToken = cardToken;
        this.postParams = PayuConstants.CARD_TOKEN+"=" + cardToken;
    }

    public DeleteMerchantHashTask(DeleteMerchantHashCallBack deleteMerchantHashCallBack, String urlForDeleteMerchantHash, StoredCard storedCard) {
        this(deleteMerchantHashCallBack,urlForDeleteMerchantHash,storedCard.getCardToken());
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
//"https://payu.herokuapp.com/delete_merchant_hash"
            URL url = new URL(urlForDeleteMerchantHash);

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
        deleteMerchantHashCallBack.deleteMerchantHashAPIResponse(response);
    }
}
