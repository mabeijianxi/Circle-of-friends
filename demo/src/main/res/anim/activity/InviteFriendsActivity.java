package anim.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.ClipboardManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.asyey.footballlibrary.network.exception.HttpException;
import com.asyey.footballlibrary.network.http.ResponseInfo;
import com.henanjianye.soon.communityo2o.BaseActivity;
import com.henanjianye.soon.communityo2o.R;
import com.henanjianye.soon.communityo2o.common.Constant;
import com.henanjianye.soon.communityo2o.common.enties.BaseDataBean;
import com.henanjianye.soon.communityo2o.common.enties.JsonUtil;
import com.henanjianye.soon.communityo2o.common.enties.UserBean;
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
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.tencent.qzone.QZone;
import cn.sharesdk.wechat.friends.Wechat;
import cn.sharesdk.wechat.moments.WechatMoments;

public class InviteFriendsActivity extends BaseActivity implements ShareSDKConfigUtil.MPlatformActionListener {
    private MyTitleBarHelper myTitleBarHelper;
    private CustomGridView mGridview;
    private MyAdapter myAdapter;
    private List<HashMap> mList = new ArrayList<>();
    private static final String ICONS = "share_icons";
    private static final String NAMES = "share_names";
    private Button btn_copy;
    private TextView tv_copy_content;
    private String
            shareTextTitle,
            shareTextContent,
            sharedUrl, shareDesc;
    private UserBean uBean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_friends);
        //1、分享的初始化
        ShareSDK.initSDK(this);
        initViews();
        initEvents();
        init();

//        getData();
    }

//    private void getData() {
//        if (CommonUtils.isNetworkConnected(this)) {
//            // 通知网络请求
//            requestAddressData();
//        } else {
//            toast("网络未连接或不可用");
//        }
//    }
//
//    private void requestAddressData() {
//        new UserDataHelper(this).getHouseAddrInfor(getNetRequestHelper(this).isShowProgressDialog(true), orgId);
//    }


//    @Override
//    protected void onResume() {
//        super.onResume();
//        shareTextTitle = "一家APP";
//        shareTextContent = "一家APP邀请你来下载~";
////        sharedUrl = "http://yijia.henanjianye.cn/";
//    }

    private void initViews() {
        myTitleBarHelper = new MyTitleBarHelper(this, getWindow().getDecorView().findViewById(android.R.id.content));
        myTitleBarHelper.setLeftImgVisible(false);
        myTitleBarHelper.resetState();
        myTitleBarHelper.setMiddleText("邀请家人");
        myTitleBarHelper.setRightImgVisible(false);
        myTitleBarHelper.setRightTxVisible(false);
        myTitleBarHelper.setLeftImag(R.mipmap.btn_back);
        mGridview = (CustomGridView) findViewById(R.id.mService);
        btn_copy = (Button) findViewById(R.id.btn_copy);
        tv_copy_content = (TextView) findViewById(R.id.tv_copy_content);
        uBean=UserSharedPreferencesUtil.getUserInfo(this);
        if(uBean!=null&&uBean.shareDesc!=null&&!uBean.shareDesc.equals("")){
            shareDesc=uBean.shareDesc.replace("\\n","\n");
            sharedUrl=uBean.inviteUrl;
            shareTextTitle = "一家";
            shareTextContent = shareDesc;
        }
    }

    private void initEvents() {
        myTitleBarHelper.setOnclickListener(this);
        btn_copy.setOnClickListener(this);
        mGridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(CommonUtils.isNetworkConnected(InviteFriendsActivity.this)&&shareDesc!=null&&!shareDesc.equals("")){
                    switch (position) {
                        case 0:
                            //微信分享
                            UmShareUtils.getInstance(InviteFriendsActivity.this).singleShare(Wechat.NAME, shareTextContent, shareTextTitle, sharedUrl, InviteFriendsActivity.this);
                            break;
                        case 1:
                            //QQ
                            UmShareUtils.getInstance(InviteFriendsActivity.this).singleShare(QQ.NAME, shareTextContent, shareTextTitle, sharedUrl, InviteFriendsActivity.this);
                            break;
                        case 2:
                            //微博
                            UmShareUtils.getInstance(InviteFriendsActivity.this).singleShare(SinaWeibo.NAME, shareTextContent, shareTextTitle, sharedUrl, InviteFriendsActivity.this);
                            break;
                        case 3:
                            //空间
                            UmShareUtils.getInstance(InviteFriendsActivity.this).singleShare(QZone.NAME, shareTextContent, shareTextTitle, sharedUrl, InviteFriendsActivity.this);
                            break;
                        case 4:
                            //朋友圈
                            UmShareUtils.getInstance(InviteFriendsActivity.this).singleShare(WechatMoments.NAME, shareTextContent, shareTextTitle, sharedUrl, InviteFriendsActivity.this);
                            break;
                        default:
                            break;
                    }
                }else{
                    toast("网络不可用");
                }
            }
        });
    }

    private void init() {
        int[] icons = new int[]{R.drawable.btn_share_wechat, R.drawable.btn_share_qq, R.drawable.btn_share_weibo, R.drawable.btn_share_zone, R.drawable.btn_share_pengyouquan};
        String[] names = new String[]{"微信", "QQ", "微博", "空间", "朋友圈"};
        for (int i = 0; i < icons.length; i++) {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put(ICONS, icons[i]);
            hashMap.put(NAMES, names[i]);
            mList.add(hashMap);
        }
        myAdapter = new MyAdapter(this, mList);
        mGridview.setAdapter(myAdapter);
        tv_copy_content.setText(shareDesc);
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
//                http://http//123.57.162.168:8080/jyo2o_web/app/view/invite/share.htm?code=mpXC
//                http://123.57.162.168:8080/jyo2o_web/app/view/invite/share.htm?code=mpXC
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_title_bar_left:
                finish();
                break;
            case R.id.btn_copy:
                ClipboardManager cmb = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                cmb.setText(tv_copy_content.getText().toString()+sharedUrl);
                toast("复制成功");
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
                        InviteFriendsActivity.this.finish();
                    } else {
                        toast(baseDataBean.msg);
                    }
                }
            }

            @Override
            public void onFailure(HttpException error, String msg, String requestTag) {
                NetWorkStateUtils.errorNetMes(InviteFriendsActivity.this);
            }
        };
    }

    @Override
    public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
        if (platform.getName().equals(SinaWeibo.NAME)) {// 判断成功的平台是不是新浪微博
            handler.sendEmptyMessage(1);
        } else if (platform.getName().equals(Wechat.NAME)) {
            handler.sendEmptyMessage(1);
        } else if (platform.getName().equals(WechatMoments.NAME)) {
            handler.sendEmptyMessage(3);
        } else if (platform.getName().equals(QQ.NAME)) {
            handler.sendEmptyMessage(4);
        }
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
                case 1:
                    Toast.makeText(getApplicationContext(), "微博分享成功", Toast.LENGTH_SHORT).show();
                    break;

                case 2:
                    Toast.makeText(getApplicationContext(), "微信分享成功", Toast.LENGTH_SHORT).show();
                    break;
                case 3:
                    Toast.makeText(getApplicationContext(), "朋友圈分享成功", Toast.LENGTH_SHORT).show();
                    break;
                case 4:
                    Toast.makeText(getApplicationContext(), "QQ分享成功", Toast.LENGTH_SHORT).show();
                    break;

                case 5:
                    Toast.makeText(getApplicationContext(), "取消分享", Toast.LENGTH_SHORT).show();
                    break;
                case 6:
//                    Toast.makeText(getApplicationContext(), "分享失败" + msg.obj, Toast.LENGTH_SHORT).show();
                    Toast.makeText(getApplicationContext(), "分享失败", Toast.LENGTH_SHORT).show();
                    break;

                default:
                    break;
            }
        }

    };
}
