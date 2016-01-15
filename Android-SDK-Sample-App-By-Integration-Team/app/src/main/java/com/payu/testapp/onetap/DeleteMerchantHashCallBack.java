package com.payu.testapp.onetap;

/**
 * Created by guruchetansingh on 1/8/16.
 */
public interface DeleteMerchantHashCallBack {

    /**
     * This will provide status after deleting  merchant hashes and card tokens.
     * */
    void deleteMerchantHashAPIResponse(String response);

}
