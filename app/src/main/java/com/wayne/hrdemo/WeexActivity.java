package com.wayne.hrdemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.taobao.weex.IWXRenderListener;
import com.taobao.weex.IWXStatisticsListener;
import com.taobao.weex.WXEnvironment;
import com.taobao.weex.WXSDKInstance;
import com.taobao.weex.WXSDKManager;
import com.taobao.weex.common.WXRenderStrategy;

public class WeexActivity extends AppCompatActivity implements IWXRenderListener, IWXStatisticsListener {
    private WXSDKInstance instance;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weex);
        if (WXEnvironment.JsFrameworkInit) {
            loadWeex();
        }else{
            WXSDKManager.getInstance().registerStatisticsListener(this);
        }
    }

    private void loadWeex() {
        instance = new WXSDKInstance(this);
        instance.registerRenderListener(this);
        instance.renderByUrl("WXSample", "http://192.168.0.84:8081/dist/index.js", null, null, WXRenderStrategy.APPEND_ASYNC);
    }


    @Override
    public void onSDKEngineInitialize() {

    }

    @Override
    public void onJsFrameworkStart() {

    }

    @Override
    public void onJsFrameworkReady() {
        loadWeex();
    }

    @Override
    public void onFirstView() {

    }

    @Override
    public void onFirstScreen() {

    }

    @Override
    public void onHttpStart() {

    }

    @Override
    public void onHeadersReceived() {

    }

    @Override
    public void onHttpFinish() {

    }

    @Override
    public void onException(String instanceid, String errCode, String msg) {

    }

    @Override
    public void onViewCreated(WXSDKInstance instance, View view) {
        setContentView(view);
    }

    @Override
    public void onRenderSuccess(WXSDKInstance instance, int width, int height) {

    }

    @Override
    public void onRefreshSuccess(WXSDKInstance instance, int width, int height) {

    }

    @Override
    public void onException(WXSDKInstance instance, String errCode, String msg) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(instance!=null){
            instance.onActivityResume();
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        if(instance!=null){
            instance.onActivityPause();
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        if(instance!=null){
            instance.onActivityStop();
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(instance!=null){
            instance.onActivityDestroy();
        }
    }
}
