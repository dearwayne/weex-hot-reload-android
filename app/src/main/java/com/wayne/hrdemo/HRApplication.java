package com.wayne.hrdemo;

import android.app.Application;

import com.taobao.weex.InitConfig;
import com.taobao.weex.WXSDKEngine;
import com.taobao.weex.adapter.DefaultWXHttpAdapter;

public class HRApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        InitConfig config = new InitConfig.Builder()
                //图片库接口
                .setImgAdapter(new ImageAdapter())
                //网络库接口
                .setHttpAdapter(new DefaultWXHttpAdapter())
                .build();
        WXSDKEngine.initialize(this,config);
    }
}
