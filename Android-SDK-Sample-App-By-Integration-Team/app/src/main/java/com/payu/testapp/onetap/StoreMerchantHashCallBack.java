package com.payu.testapp.onetap;

/**
 * Created by guruchetansingh on 1/8/16.
 */
public interface StoreMerchantHashCallBack {
    /**
     * This will provide status after storing  merchant hashes and card tokens.
     * */
    void storeMerchantHashAPIResponse(String response);
}
