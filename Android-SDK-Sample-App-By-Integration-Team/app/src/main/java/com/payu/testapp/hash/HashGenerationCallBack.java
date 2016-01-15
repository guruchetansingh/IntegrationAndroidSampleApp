package com.payu.testapp.hash;

import com.payu.india.Model.PayuHashes;

/**
 * Created by guruchetansingh on 1/5/16.
 */
public interface HashGenerationCallBack {

    /**
     *This method provide all hashes
     * */
    void hashGenerationAPIResponse(PayuHashes payuHashes);

}
