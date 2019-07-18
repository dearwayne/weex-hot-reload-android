package com.wayne.weexhotreload;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

class DynamicProxy implements InvocationHandler {
    private InvocationHandler handler;
    private Object object;

    DynamicProxy(Object object, InvocationHandler hander) {
        this.object = object;
        this.handler = hander;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result = method.invoke(object, args);
        this.handler.invoke(object,method,args);
        return result;
    }
}
