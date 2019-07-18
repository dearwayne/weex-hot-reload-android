package com.wayne.weexhotreload;

import android.content.Intent;
import android.util.Log;

import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

class SocketsManager {
    private static final String TAG = "SocketsManager";
    private Set<String> serverUrls = new HashSet<>();
    private volatile String jsHash;

    private static SocketsManager instance = new SocketsManager();

    private SocketsManager() {
    }

    static SocketsManager getInstance() {
        return instance;
    }

    void connect(String url) {
        Request request = new Request.Builder().url(url).build();
        if (!serverUrls.contains(request.url().toString())) {
            OkHttpClient okHttpClient = new OkHttpClient();
            okHttpClient.newWebSocket(request, new WXWebSocketListener());
            okHttpClient.dispatcher().executorService().shutdown();
        }
    }

    class WXWebSocketListener extends WebSocketListener {
        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            super.onOpen(webSocket, response);
            serverUrls.add(webSocket.request().url().toString());
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            super.onMessage(webSocket, text);
            try {
                if (text.startsWith("{\"type\":\"hash\"")) {
                    JSONObject jsonObject = new JSONObject(text);
                    String hash = jsonObject.getString("data");
                    if (!hash.equals(jsHash)) {
                        jsHash = hash;
                        HRReloadManager.getInstance().application.sendBroadcast(new Intent()
                                .setAction(HRReloadManager.ACTION_DEBUG_INSTANCE_REFRESH)
                                .putExtra("params", ""));
                    }
                } else if (text.startsWith("{\"type\":\"error\"")) {
                    JSONObject jsonObject = new JSONObject(text);
                    String data = jsonObject.getString("data");
                    Log.e(TAG, data);
                }
            } catch (Exception e) {

            }
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            super.onClosing(webSocket, code, reason);
            webSocket.close(1000, null);
        }

        @Override
        public void onClosed(WebSocket webSocket, int code, String reason) {
            super.onClosed(webSocket, code, reason);
            Log.e(TAG, "hotReload socket closed");
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            super.onFailure(webSocket, t, response);
        }
    }
}
