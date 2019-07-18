package com.wayne.weexhotreload;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;

import com.taobao.weex.IWXRenderListener;
import com.taobao.weex.WXSDKInstance;
import com.taobao.weex.WXSDKManager;
import com.taobao.weex.adapter.IWXHttpAdapter;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HRReloadManager implements Application.ActivityLifecycleCallbacks, WXSDKManager.InstanceLifeCycleCallbacks {
    static final String ACTION_DEBUG_INSTANCE_REFRESH = "DEBUG_INSTANCE_REFRESH";
    private static HRReloadManager hotRefreshInstance = new HRReloadManager();
    private Map<String, InstanceModel> instanceMaps;
    private Boolean enabled;

    private HRReloadManager() {
        this.enabled = true;
        instanceMaps = new ConcurrentHashMap<>();
    }

    Application application;

    static HRReloadManager getInstance() {
        return hotRefreshInstance;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    @SuppressWarnings("unused")
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    void install(Application application) {
        this.application = application;
        this.application.registerActivityLifecycleCallbacks(this);
        WXSDKManager.getInstance().registerInstanceLifeCycleCallbacks(this);
        registerBroadcast();
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        try {
            hookSDKHttpAdapter();
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }

    @Override
    public void onInstanceCreated(String instanceId) {
        WXSDKInstance instance = WXSDKManager.getInstance().getSDKInstance(instanceId);
        connectWebsocket(instance.getBundleUrl());
    }

    @Override
    public void onInstanceDestroyed(String instanceId) {
        instanceMaps.remove(instanceId);
    }

    private void hookSDKHttpAdapter() throws Exception {
        Field mIWXHttpAdapter = WXSDKManager.class.getDeclaredField("mIWXHttpAdapter");
        mIWXHttpAdapter.setAccessible(true);
        IWXHttpAdapter adapter = (IWXHttpAdapter) mIWXHttpAdapter.get(WXSDKManager.getInstance());

        if (Proxy.isProxyClass(adapter.getClass()) && Proxy.getInvocationHandler(adapter) instanceof DynamicProxy) {
            return;
        }

        DynamicProxy proxy = new DynamicProxy(adapter, new InvocationHandler() {
            @Override
            public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
                if (method.getName().equals("sendRequest")) {
                    getParams(objects);
                }
                return null;
            }
        });

        IWXHttpAdapter adapterProxy = (IWXHttpAdapter)Proxy.newProxyInstance(IWXHttpAdapter.class.getClassLoader(),
                new Class[]{IWXHttpAdapter.class},proxy);

        mIWXHttpAdapter.set(WXSDKManager.getInstance(), adapterProxy);
    }

    @SuppressWarnings("unchecked")
    private void getParams(Object[] objects) {
        try {
            if (objects.length < 2) return;

            Object object = objects[1];
            Class Listener = Class.forName("com.taobao.weex.WXSDKInstance$WXHttpListener");
            if (object.getClass() != Listener) return;

            WXSDKInstance instance;
            try {
                Field pageNameField = Listener.getDeclaredField("instance");
                pageNameField.setAccessible(true);
                instance = (WXSDKInstance) pageNameField.get(object);
            } catch (Exception e) {
                return;
            }

            String pageName;
            try {
                Field pageNameField = Listener.getDeclaredField("pageName");
                pageNameField.setAccessible(true);
                pageName = (String) pageNameField.get(objects[1]);
            } catch (Exception e) {
                pageName = null;
            }

            Map<String, Object> options;
            try {
                Field optionsField = Listener.getDeclaredField("options");
                optionsField.setAccessible(true);
                options = (Map<String, Object>) optionsField.get(object);
            } catch (Exception e) {
                options = null;
            }

            String jsonInitData;
            try {
                Field jsonInitDataField = Listener.getDeclaredField("jsonInitData");
                jsonInitDataField.setAccessible(true);
                jsonInitData = (String) jsonInitDataField.get(object);
            } catch (Exception e) {
                jsonInitData = null;
            }

            Field instanceField = null;
            for (Field field : getAllFields(instance.getContext())) {
                if (field.getType() == WXSDKInstance.class) {
                    field.setAccessible(true);
                    instanceField = field;
                    break;
                }
            }

            InstanceModel model = new InstanceModel();
            model.setInstance(instance);
            model.setPageName(pageName);
            model.setOptions(options);
            model.setJsonInitData(jsonInitData);
            model.setInstanceField(instanceField);

            instanceMaps.put(instance.getInstanceId(),model);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private Field[] getAllFields(Context context) {
        List<Field> fieldList = new ArrayList<>() ;
        Class tempClass = context.getClass();
        while (tempClass != null) {
            fieldList.addAll(Arrays.asList(tempClass .getDeclaredFields()));
            tempClass = tempClass.getSuperclass();
        }
        return fieldList.toArray(new Field[fieldList.size()]);
    }

    private void connectWebsocket(String url) {
        if (url == null) return;
        Uri uri = Uri.parse(url);
        if (!uri.getScheme().startsWith("http")) return;
        String port = uri.getPort() > 0 ? ":" + uri.getPort() : "";
        String socketUrl = "ws://" + uri.getHost() + port + "/sockjs-node/websocket";
        SocketsManager.getInstance().connect(socketUrl);
    }

    private void registerBroadcast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_DEBUG_INSTANCE_REFRESH);
        this.application.registerReceiver(new RefreshBroadcastReceiver(), filter);
    }

    class RefreshBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_DEBUG_INSTANCE_REFRESH.equals(intent.getAction())) {
                debugRefresh();
            }
        }
    }

    private void debugRefresh() {
        if (!getEnabled()) {
            return;
        }

        for (Iterator<Map.Entry<String, InstanceModel>> it = instanceMaps.entrySet().iterator(); it.hasNext();){
            InstanceModel model = it.next().getValue();
            it.remove();
            try {
                WXSDKInstance oInstance = model.getInstance();
                Context context = oInstance.getContext();

                try {
                    if (oInstance.getRootView() != null) {
                        oInstance.removeFixedView(oInstance.getRootView());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                WXSDKInstance instance = new WXSDKInstance(context);
                model.getInstanceField().set(context, instance);
                instance.registerRenderListener((IWXRenderListener) context);
                instance.renderByUrl(model.getPageName(), oInstance.getBundleUrl(),
                        model.getOptions(), model.getJsonInitData(),
                        oInstance.getRenderStrategy());
            }catch (Exception e){
                continue;
            }
        }
    }
}
