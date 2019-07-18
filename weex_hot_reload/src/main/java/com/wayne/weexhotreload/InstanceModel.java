package com.wayne.weexhotreload;

import com.taobao.weex.WXSDKInstance;

import java.lang.reflect.Field;
import java.util.Map;

class InstanceModel {
    private WXSDKInstance instance;
    private String pageName;
    private Map<String, Object> options;
    private String jsonInitData;
    private Field instanceField;

    WXSDKInstance getInstance() {
        return instance;
    }
    void setInstance(WXSDKInstance instance) {
        this.instance = instance;
    }
    String getPageName() {
        return pageName;
    }
    void setPageName(String pageName) {
        this.pageName = pageName;
    }
    Map<String, Object> getOptions() {
        return options;
    }
    void setOptions(Map<String, Object> options) {
        this.options = options;
    }
    String getJsonInitData() {
        return jsonInitData;
    }
    void setJsonInitData(String jsonInitData) {
        this.jsonInitData = jsonInitData;
    }
    Field getInstanceField() {
        return instanceField;
    }
    void setInstanceField(Field instanceField) {
        this.instanceField = instanceField;
    }
}
