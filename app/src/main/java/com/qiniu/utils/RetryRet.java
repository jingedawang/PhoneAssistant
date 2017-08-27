package com.qiniu.utils;

import com.qiniu.auth.CallRet;

public abstract class RetryRet extends CallRet{
    private CallRet ret;
    public RetryRet(CallRet ret){
        this.ret = ret;
    }

    public void onInit(int flag){
        ret.onInit(flag);
    }
    public void onSuccess(byte[] body){
        ret.onSuccess(body);
    }
    public abstract void onFailure(QiniuException ex);

    public void onProcess(long current, long total){
        ret.onProcess(current, total);
    }
    public void onPause(Object tag){
        ret.onPause(tag);
    }

    public static boolean noRetry(QiniuException ex){
        if (ex.code/100  == 5 && ex.code != 579) {
            return false;
        }
        if (ex.code == 996) {
            return false;
        }

        if (ex.reason instanceof java.io.IOException && !(ex.reason instanceof java.io.FileNotFoundException)) {
            return false;
        }

        return true;
    }
}
