package anim.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import com.asyey.footballlibrary.network.exception.HttpException;
import com.asyey.footballlibrary.network.http.ResponseInfo;
import com.henanjianye.soon.communityo2o.BaseActivity;
import com.henanjianye.soon.communityo2o.R;
import com.henanjianye.soon.communityo2o.common.Constant;
import com.henanjianye.soon.communityo2o.common.MyLog;
import com.henanjianye.soon.communityo2o.common.bluetooth.BlueToothService;
import com.henanjianye.soon.communityo2o.common.bluetooth.InterfBlueTooth;
import com.henanjianye.soon.communityo2o.common.enties.BaseDataBean;
import com.henanjianye.soon.communityo2o.common.enties.JsonUtil;
import com.henanjianye.soon.communityo2o.common.util.CommonUtils;
import com.henanjianye.soon.communityo2o.common.util.MyTitleBarHelper;
import com.henanjianye.soon.communityo2o.common.util.NetWorkStateUtils;
import com.henanjianye.soon.communityo2o.interf.NetWorkCallback;


public class ServiceOpenDoorActivity extends BaseActivity {
    private MyTitleBarHelper myTitleBarHelper;
    private WebView open_door_webview;
    private BlueToothService.MyBinder myBinder;
    private MConnection mConnection;
    private boolean isActivityClosed;
    private final int BLUETOOTH_OPEN_REQUEST_CODE = 100;
    private ImageView iv_show_qr;
    private TextView tv_mac;

    @SuppressLint("JavascriptInterface")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_open_door);
        initViews();
        startOpenDoor();
        initEvents();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isActivityClosed = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isActivityClosed = true;
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    private void startOpenDoor() {
        mConnection = new MConnection();
        bindService(
                new Intent(ServiceOpenDoorActivity.this, BlueToothService.class),
                mConnection, Service.BIND_AUTO_CREATE);
    }

    public class MConnection implements ServiceConnection {


        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MyLog.e("AAA", "onServiceConnected-----");
            myBinder = (BlueToothService.MyBinder) service;
            myBinder.setBlueToothListener(new InterfBlueTooth() {
                @Override
                public void stopScaning(boolean is) {
                    // TODO Auto-generated method stub
                    MyLog.e("AAA", "stopScaning-----");
                }

                @Override
                public void failConnection(final int errorState) {
                    MyLog.e("AAA", "failConnection---errorState--" + errorState);
                    if (!isActivityClosed) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showMyErrorDialog(errorState);
                            }
                        });
                    }
                }

                @Override
                public void sendMsgOver() {

                    MyLog.e("AAA","sendMsgOver-------------");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (open_door_webview != null) {
                                open_door_webview.loadUrl("javascript:btnShow()");
                            }
                        }
                    });


                }

                @Override
                public void startScaning() {
                    // TODO Auto-generated method stub
                    MyLog.e("AAA", "startScaning-----");
                }

                @Override
                public void deviceFound(BluetoothDevice device) {
                    // TODO Auto-generated method stub
                    MyLog.e("AAA", "deviceFound-----");
                    tv_mac.append(device.getAddress()+"-------next-----");
//                    myBinder.antoConnection(device);
                }

                @Override
                public void deviceConnected(String deviceId) {
                    // TODO Auto-generated method stub
                    MyLog.e("AAA", "deviceConnected-----");
                    myBinder.sendCmdForOpenDoor();
                }
            });
            myBinder.findDevice();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // TODO Auto-generated method stub

        }

    }

    public void showMyErrorDialog(int errorCode) {


        AlertDialog.Builder builder = new AlertDialog.Builder(ServiceOpenDoorActivity.this);
        builder.setTitle("提示");

        switch (errorCode) {
            case BlueToothService.BLUETOOTH_CONNECTION_FAIL:
                open_door_webview.loadUrl("javascript:btnShow()");
                builder.setMessage("链接蓝牙失败，您可以重试或者使用二维码开门BLUETOOTH_CONNECTION_FAIL"+errorCode);
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        open_door_webview.loadUrl("javascript:btnShow()");
                        dialog.dismiss();
                    }
                });
            case BlueToothService.BLUETOOTH_FIND_FAIL:
                open_door_webview.loadUrl("javascript:btnShow()");
                builder.setMessage("链接蓝牙失败，您可以重试或者使用二维码开门BLUETOOTH_FIND_FAIL"+errorCode);
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                    }
                });
                break;
            case BlueToothService.BLUETOOTH_DEVICE_FAIL:
                builder.setMessage("您的设备不支持蓝牙BLUETOOTH_DEVICE_FAIL"+errorCode);
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                });
                break;
            case BlueToothService.BLUETOOTH_NOT_OPEN:
                builder.setMessage("蓝牙未打开，请打开蓝牙BLUETOOTH_NOT_OPEN"+errorCode);
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                });
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //TODO 打开蓝牙
                        Intent enabler = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enabler, BLUETOOTH_OPEN_REQUEST_CODE);//同startActivity(enabler);
                    }
                });
                break;
            case BlueToothService.BLUETOOTH_STATE_ERROR:
                open_door_webview.loadUrl("javascript:btnShow()");
                builder.setMessage("链接蓝牙失败，您可以重试或者使用二维码开门BLUETOOTH_STATE_ERROR"+errorCode);
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                break;
            case BlueToothService.BLUETOOTH_ORGID_ERROR:
                open_door_webview.loadUrl("javascript:btnShow()");
                builder.setMessage("小区ID错误，请重新登录账号BLUETOOTH_ORGID_ERROR"+errorCode);
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                });
                break;
            default:
                open_door_webview.loadUrl("javascript:btnShow()");
                builder.setMessage("链接蓝牙失败，您可以重试或者使用二维码开门default"+errorCode);
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        }
        builder.create().show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (myBinder != null) {
            myBinder.disConnection();
            unbindService(mConnection);
        }
    }

    private void initViews() {
        myTitleBarHelper = new MyTitleBarHelper(this, getWindow().getDecorView().findViewById(android.R.id.content));
        myTitleBarHelper.setLeftImgVisible(false);
        myTitleBarHelper.resetState();
        myTitleBarHelper.setMiddleText("一键开门");
        myTitleBarHelper.setRightImgVisible(false);
        myTitleBarHelper.setRightTxVisible(false);
        myTitleBarHelper.setLeftImag(R.mipmap.btn_back);
        open_door_webview = (WebView) findViewById(R.id.open_door_webview);
        iv_show_qr = (ImageView) findViewById(R.id.iv_show_qr);
        tv_mac= (TextView) findViewById(R.id.tv_mac);
    }

    private void initEvents() {
        myTitleBarHelper.setOnclickListener(this);
        iv_show_qr.setOnClickListener(this);
        //获得WebSetting对象,支持js脚本,可访问文件,支持缩放,以及编码方式
        WebSettings webSettings = open_door_webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDefaultTextEncodingName("UTF-8");
        open_door_webview.loadUrl("file:///android_asset/unlock.html");
        //设置WebChromeClient,处理网页中的各种js事件
        open_door_webview.setWebViewClient(new MyWebViewClient());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_title_bar_left:
                finish();
                break;
            case R.id.iv_show_qr:
//                open_door_webview.loadUrl("javascript:btnShow()");
                if (CommonUtils.isNetworkConnected(this)) {
                    startActivity(new Intent(ServiceOpenDoorActivity.this, ServiceShareQRCodeActivity.class));
                } else {
                    toast("网络未连接");
                }
                break;

        }
    }

    @Override
    protected NetWorkCallback setNetWorkCallback() {
        return new NetWorkCallback() {
            @Override
            public void onStart(String requestTag) {
            }

            @Override
            public void onCancelled(String requestTag) {
                toast("取消提交操作");
            }

            @Override
            public void onLoading(long total, long current, boolean isUploading, String requestTag) {
            }

            @Override
            public void onSuccess(ResponseInfo<String> responseInfo, String requestTag) {
                if (requestTag.equals(Constant.Repair.HOUSE_ADDRESS)) {
//                    parseHouseInfoRecord(responseInfo.result);
                } else if (requestTag.equals(Constant.Repair.REPAIR_SUBMIT)) {
                    BaseDataBean<Object> baseDataBean = JsonUtil.parseDataObject(responseInfo.result, Object.class);
                    if (baseDataBean.code == 100) {
                        toast("提交成功");
                        Intent intent = new Intent();
                        setResult(RESULT_OK, intent);
                        ServiceOpenDoorActivity.this.finish();
                    } else {
                        toast(baseDataBean.msg);
                    }
                }
            }

            @Override
            public void onFailure(HttpException error, String msg, String requestTag) {
                NetWorkStateUtils.errorNetMes(ServiceOpenDoorActivity.this);
            }
        };
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        MyLog.e("AAA", "resultCode------===" + resultCode);
        if (requestCode == BLUETOOTH_OPEN_REQUEST_CODE && resultCode == -1) {
            if (myBinder != null) {
                myBinder.findDevice();
            }
        }
    }

    @SuppressLint("NewApi")
    private class MyWebViewClient extends WebViewClient {
//        @Override
//        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
//            // TODO Auto-generated method stub
//            MyLog.e("MMM", "url is " + url);
//            if (url.contains("opendoor")) {
//                MyLog.e("MMM", "正在开门");
//            }
//            return super.shouldInterceptRequest(view, url);
//        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.contains("opendoor")) {
                if (myBinder != null) {
//                   MyLog.e("AAA","antoConnection--------null-----"+url);
                    myBinder.antoConnection(null);
                }
            }
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
//            MyLog.e("MMM", "url22222 is " + url);
            super.onPageFinished(view, url);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode,
                                    String description, String failingUrl) {
//            MyLog.e("MMM", "url33333 is " + description);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
//            MyLog.e("MMM", "url33333 is " + url);
            super.onPageStarted(view, url, favicon);
        }
    }


}
