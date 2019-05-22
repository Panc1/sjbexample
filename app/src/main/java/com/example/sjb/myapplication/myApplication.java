package com.example.sjb.myapplication;

import android.app.Application;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;

/**
 * Created by wz_03 on 2019/1/11.
 */

public class myApplication extends Application {

    private String App_Key = "5c36fbe9b465f5c264000a35";

    @Override
    public void onCreate() {
        UMConfigure.init(this, App_Key, "香蕉头条", UMConfigure.DEVICE_TYPE_PHONE,
                "");
        MobclickAgent.setSecret(this,"s10bacedtyz");
        super.onCreate();
    }


}
