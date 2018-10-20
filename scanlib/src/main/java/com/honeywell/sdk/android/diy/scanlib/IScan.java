package com.honeywell.sdk.android.diy.scanlib;

/**
 * Created by yangxiaoguang on 2018/1/5.
 */

public interface IScan {
    /**
     * 错误信息回调
     * @param errMsg
     * @param sdkMsg
     */
    void onErrMsg(String errMsg, String sdkMsg);


    void onInitSuccess();

    /**
     * 解码回调
     * @param barcode
     */
    void onDecode(String barcode);

}
