package com.mabeijianxi.circle_of_friends.callback;

import android.os.Handler;

import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.zhy.http.okhttp.callback.Callback;

import java.io.IOException;

/**
 * Created by mabeijianxi on 2016/1/15.
 * OKhttp的网络请求回调，可自定义
 */

public class OkRequestCallBack extends Callback<String> {
    private Handler handler=new Handler();
    private boolean isUploading = true;


    public NetWorkCallback getNetWorkCallback() {
        return netWorkCallback;
    }

    public void setNetWorkCallback(NetWorkCallback netWorkCallback) {
        this.netWorkCallback = netWorkCallback;
    }

    private NetWorkCallback netWorkCallback;
    @Override
    public void onAfter() {
        super.onAfter();
    }

    @Override
    public void onBefore(Request request) {
        super.onBefore(request);

        handler.post(new Runnable() {
            @Override
            public void run() {
                netWorkCallback.onBefore();
            }
        });
    }


    @Override
    public void inProgress(final float progress) {
        super.inProgress(progress);
        handler.post(new Runnable() {
            @Override
            public void run() {
                netWorkCallback.onLoading(progress);
            }
        });
    }

    @Override
    public String parseNetworkResponse(Response response) throws IOException {
        final String responseInfo = response.body().string();


                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        netWorkCallback.onSuccess(responseInfo);
                    }
                });

        return responseInfo;
    }

    @Override
    public void onError(Request request, final Exception e) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
//                        Constant.requests.remove(requestTag);
                } catch (Exception ee) {
                    // TODO: handle exception
                }
                if (netWorkCallback != null) {
                    netWorkCallback.onFailure(e, e.toString());
                }
            }
        });

    }

    @Override
    public void onResponse(String response) {

    }

}