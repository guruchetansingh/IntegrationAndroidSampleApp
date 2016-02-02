package com.payu.testapp.hash;

import android.util.Log;

import com.payu.india.Extras.PayUChecksum;
import com.payu.india.Model.PaymentParams;
import com.payu.india.Model.PayuHashes;
import com.payu.india.Model.PostData;
import com.payu.india.Payu.PayuConstants;
import com.payu.india.Payu.PayuErrors;


/**
 * Created by guruchetansingh on 1/5/16.
 */

/** This class is used to generate hash from SDK */
public class GetHashesFromSDK {

    public GetHashesFromSDK() {
        Log.i("GetData","GetHashesFromSDK");
    }

    /**
     * This method is used for hash generation in application.
     *Do not use this, you may use this only for testing.
     *This should be done from server side..
     *Do not keep salt anywhere in app.
     */
    public void generateHashFromSDK(HashGenerationCallBack hashGenerationCallBack, PaymentParams paymentParams, String merchantSalt) {
        PayUChecksum checksum = new PayUChecksum();
        PayuHashes payuHashes = new PayuHashes();
        PostData postData = null;
        String merchantKey = null;

        // var1 should be either user credentials or default
        String var1 = null;
        checksum.setAmount(paymentParams.getAmount());
        checksum.setKey(merchantKey = paymentParams.getKey());
        checksum.setTxnid(paymentParams.getTxnId());
        checksum.setEmail(paymentParams.getEmail());
        checksum.setSalt(merchantSalt);
        checksum.setVar1(var1 = (paymentParams.getUserCredentials() == null || paymentParams.getUserCredentials().isEmpty()) ? PayuConstants.DEFAULT : paymentParams.getUserCredentials());
        checksum.setProductinfo(paymentParams.getProductInfo());
        checksum.setFirstname(paymentParams.getFirstName());
        checksum.setUdf1(paymentParams.getUdf1());
        checksum.setUdf2(paymentParams.getUdf2());
        checksum.setUdf3(paymentParams.getUdf3());
        checksum.setUdf4(paymentParams.getUdf4());
        checksum.setUdf5(paymentParams.getUdf5());

        postData = checksum.getHash();
        if (postData.getCode() == PayuErrors.NO_ERROR) {
            payuHashes.setPaymentHash(postData.getResult());
        }


        if ((postData = calculateHash(checksum,merchantKey, PayuConstants.PAYMENT_RELATED_DETAILS_FOR_MOBILE_SDK, var1, merchantSalt)) != null && postData.getCode() == PayuErrors.NO_ERROR) // Assign post data first then check for success
            payuHashes.setPaymentRelatedDetailsForMobileSdkHash(postData.getResult());

        if ((postData = calculateHash(checksum,merchantKey, PayuConstants.VAS_FOR_MOBILE_SDK, PayuConstants.DEFAULT, merchantSalt)) != null && postData.getCode() == PayuErrors.NO_ERROR)
            payuHashes.setVasForMobileSdkHash(postData.getResult());


        if ((postData = calculateHash(checksum,merchantKey, PayuConstants.GET_MERCHANT_IBIBO_CODES, PayuConstants.DEFAULT, merchantSalt)) != null && postData.getCode() == PayuErrors.NO_ERROR)
            payuHashes.setMerchantIbiboCodesHash(postData.getResult());

        if (!var1.contentEquals(PayuConstants.DEFAULT)) {
            // get user card
            if ((postData = calculateHash(checksum,merchantKey, PayuConstants.GET_USER_CARDS, var1, merchantSalt)) != null && postData.getCode() == PayuErrors.NO_ERROR)
                payuHashes.setStoredCardsHash(postData.getResult());
            // save user card
            if ((postData = calculateHash(checksum,merchantKey, PayuConstants.SAVE_USER_CARD, var1, merchantSalt)) != null && postData.getCode() == PayuErrors.NO_ERROR)
                payuHashes.setSaveCardHash(postData.getResult());
            // delete user card
            if ((postData = calculateHash(checksum,merchantKey, PayuConstants.DELETE_USER_CARD, var1, merchantSalt)) != null && postData.getCode() == PayuErrors.NO_ERROR)
                payuHashes.setDeleteCardHash(postData.getResult());
            // edit user card
            if ((postData = calculateHash(checksum,merchantKey, PayuConstants.EDIT_USER_CARD, var1, merchantSalt)) != null && postData.getCode() == PayuErrors.NO_ERROR)
                payuHashes.setEditCardHash(postData.getResult());
        }

        if (paymentParams.getOfferKey() != null) {
            postData = calculateHash(checksum,merchantKey, PayuConstants.OFFER_KEY, paymentParams.getOfferKey(), merchantSalt);
            if (postData.getCode() == PayuErrors.NO_ERROR) {
                payuHashes.setCheckOfferStatusHash(postData.getResult());
            }
        }

        hashGenerationCallBack.hashGenerationAPIResponse(payuHashes);

    }

    // deprecated, should be used only for testing.
    private PostData calculateHash(PayUChecksum checksum,String key, String command, String var1, String salt) {
        checksum.setKey(key);
        checksum.setCommand(command);
        checksum.setVar1(var1);
        checksum.setSalt(salt);
        return checksum.getHash();
    }
}
