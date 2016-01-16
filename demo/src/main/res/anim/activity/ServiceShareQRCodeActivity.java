package anim.activity;

import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.asyey.footballlibrary.network.exception.HttpException;
import com.asyey.footballlibrary.network.http.ResponseInfo;
import com.asyey.footballlibrary.universalimageloader.core.DisplayImageOptions;
import com.asyey.footballlibrary.universalimageloader.core.ImageLoader;
import com.henanjianye.soon.communityo2o.BaseActivity;
import com.henanjianye.soon.communityo2o.R;
import com.henanjianye.soon.communityo2o.common.Constant;
import com.henanjianye.soon.communityo2o.common.UserDataHelper;
import com.henanjianye.soon.communityo2o.common.enties.BaseDataBean;
import com.henanjianye.soon.communityo2o.common.enties.JsonUtil;
import com.henanjianye.soon.communityo2o.common.enties.QrCodeBean;
import com.henanjianye.soon.communityo2o.common.util.CommonUtils;
import com.henanjianye.soon.communityo2o.common.util.MyTitleBarHelper;
import com.henanjianye.soon.communityo2o.common.util.NetWorkStateUtils;
import com.henanjianye.soon.communityo2o.common.util.ShareSDKConfigUtil;
import com.henanjianye.soon.communityo2o.common.util.UmShareUtils;
import com.henanjianye.soon.communityo2o.common.util.UserSharedPreferencesUtil;
import com.henanjianye.soon.communityo2o.common.view.CustomGridView;
import com.henanjianye.soon.communityo2o.interf.NetWorkCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.wechat.friends.Wechat;


public class ServiceShareQRCodeActivity extends BaseActivity implements ShareSDKConfigUtil.MPlatformActionListener {
    private MyTitleBarHelper myTitleBarHelper;
    private CustomGridView share_qrcode;
    private static final String ICONS = "share_qrcode_icons";
    private static final String NAMES = "share_qrcode_names";
    private List<HashMap> mList = new ArrayList<>();
    private MyAdapter myAdapter;
    private String
            shareTextTitle,
            shareTextContent
            ;
    private ImageView iv_open_door;
    DisplayImageOptions build = new DisplayImageOptions.Builder()
            .showImageOnFail(R.mipmap.gouwuche_image)
            .showImageForEmptyUri(R.mipmap.gouwuche_image).showImageOnLoading(R.mipmap.gouwuche_image)
            .cacheInMemory(false).cacheOnDisk(false).build();
    private String type = "01";
    private int LoginState;
    private int orgId;
    private String QrUrl = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_share_qr_code);
        initViews();
        initEvents();
        init();
        getData();
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
//                MyLog.e("MMM", "responseInfo.result is " + responseInfo.result);
                if (requestTag.equals(Constant.ShouYeUrl.SERVICE_OPEN_DOOR)) {
                    parseQrCode(responseInfo.result);
                    startAnim(share_qrcode);
                }
            }

            @Override
            public void onFailure(HttpException error, String msg, String requestTag) {
                NetWorkStateUtils.errorNetMes(ServiceShareQRCodeActivity.this);
            }
        };
    }



    private void parseQrCode(String result) {
        try {
            BaseDataBean<QrCodeBean> baseDataBean = JsonUtil.parseDataObject(result, QrCodeBean.class);
            if (baseDataBean.code == 100) {
                QrCodeBean qrCodeBean = baseDataBean.data;
                if (qrCodeBean != null) {
                    QrUrl = qrCodeBean.codeUrl;
                    ImageLoader.getInstance().displayImage(qrCodeBean.codeUrl, iv_open_door, build);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void getData() {
        if (CommonUtils.isNetworkConnected(this)) {
            // 通知网络请求
            requestQrCodeData();
        } else {
            toast("网络未连接或不可用");
        }
    }

    private void requestQrCodeData() {
        new UserDataHelper(this).getQrCodeInfor(getNetRequestHelper(this).isShowProgressDialog(true), orgId, type);
    }
    @Override
    protected void onResume() {
        super.onResume();
        shareTextTitle = "一家";
        shareTextContent = "发送二维码给你的亲友，通过二维码来解锁";
//        sharedUrl = "http://yijia.henanjianye.cn/";
    }

    private void init() {
        orgId = UserSharedPreferencesUtil.getUserInfo(this, UserSharedPreferencesUtil.ORGID, -1);
        LoginState = UserSharedPreferencesUtil.getUserLoginState(this);
        if (LoginState >= 2) {
            type = "01";//户主
        } else {
            type = "02";//游客
        }
        int[] icons = new int[]{R.drawable.btn_share_wechat_qr, R.drawable.btn_share_qq_qr};
        String[] names = new String[]{"微信", "QQ"};
        for (int i = 0; i < icons.length; i++) {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put(ICONS, icons[i]);
            hashMap.put(NAMES, names[i]);
            mList.add(hashMap);
        }
        myAdapter = new MyAdapter(this, mList);
        share_qrcode.setAdapter(myAdapter);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void startAnim(GridView imageView) {
        if(Build.VERSION.SDK_INT<11){
            return;
        }
        float curTranslationY = imageView.getTranslationY();
        ObjectAnimator animator1 = ObjectAnimator.ofFloat(
                imageView,
                "translationY",
                curTranslationY, -300F, curTranslationY);
        animator1.setInterpolator(new BounceInterpolator());
        animator1.setDuration(1000);
        animator1.start();
    }

    private void initViews() {
        myTitleBarHelper = new MyTitleBarHelper(this, getWindow().getDecorView().findViewById(android.R.id.content));
        myTitleBarHelper.setLeftImgVisible(false);
        myTitleBarHelper.resetState();
        myTitleBarHelper.setMiddleText("分享二维码");
        myTitleBarHelper.setRightImgVisible(false);
        myTitleBarHelper.setRightTxVisible(false);
        myTitleBarHelper.setLeftImag(R.mipmap.btn_back);
        share_qrcode = (CustomGridView) findViewById(R.id.share_qrcode);
        iv_open_door = (ImageView) findViewById(R.id.iv_open_door);
    }

    private void initEvents() {
        myTitleBarHelper.setOnclickListener(this);
        share_qrcode.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!QrUrl.equals("")) {
                    switch (position) {
                        case 0:
                            //微信分享
                            UmShareUtils.getInstance(ServiceShareQRCodeActivity.this).shareWechatAndQQ(Wechat.NAME, shareTextContent, shareTextTitle, QrUrl, ServiceShareQRCodeActivity.this);
                            break;
                        case 1:
                            //QQ
                            UmShareUtils.getInstance(ServiceShareQRCodeActivity.this).shareWechatAndQQ(QQ.NAME, shareTextContent, shareTextTitle, QrUrl, ServiceShareQRCodeActivity.this);
                            break;
                        default:
                            break;
                    }
                } else {
                   toast("分享失败，请退出该界面后重试");
                } ;
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_title_bar_left:
                finish();
                break;
        }
    }

    private class MyAdapter extends BaseAdapter {
        private Context mContext;
        private List<HashMap> mList;

        public MyAdapter(Context mContext, List<HashMap> mList) {
            this.mContext = mContext;
            this.mList = mList;
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            MyHolder mHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.item_invite_friend, parent, false);
                mHolder = new MyHolder();
                mHolder.iv_img_share = (ImageView) convertView.findViewById(R.id.iv_img_share);
                mHolder.tv_name_share = (TextView) convertView.findViewById(R.id.tv_name_share);
                mHolder.ll_share_friend = (LinearLayout) convertView.findViewById(R.id.ll_share_friend);
                convertView.setTag(mHolder);
            } else {
                mHolder = (MyHolder) convertView.getTag();
            }
            mHolder.iv_img_share.setBackgroundResource((Integer) mList.get(position).get(ICONS));
            mHolder.tv_name_share.setText((String) mList.get(position).get(NAMES));
//            mHolder.ll_share_friend.setOnClickListener(new On);
            return convertView;
        }


        class MyHolder {
            LinearLayout ll_share_friend;
            ImageView iv_img_share;
            TextView tv_name_share;
        }
    }


    @Override
    public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
//      if (platform.getName().equals(Wechat.NAME)) {
//            handler.sendEmptyMessage(3);
//        }else if (platform.getName().equals(QQ.NAME)) {
//            handler.sendEmptyMessage(4);
//        }
    }

    @Override
    public void onError(Platform platform, int i, Throwable throwable) {
        Message msg = Message.obtain();
        msg.what = 6;
        msg.obj = throwable.getMessage();
        handler.sendMessage(msg);
    }

    @Override
    public void onCancel(Platform platform, int i) {
        handler.sendEmptyMessage(5);
    }

    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {

                case 3:
                    Toast.makeText(getApplicationContext(), "微信分享成功", Toast.LENGTH_SHORT).show();
                    break;
                case 4:
                    Toast.makeText(getApplicationContext(), "QQ分享成功", Toast.LENGTH_SHORT).show();
                    break;

                case 5:
                    Toast.makeText(getApplicationContext(), "取消分享", Toast.LENGTH_SHORT).show();
                    break;
                case 6:
                    Toast.makeText(getApplicationContext(), "分享失败" + msg.obj, Toast.LENGTH_SHORT).show();
                    break;

                default:
                    break;
            }
        }

    };
}
