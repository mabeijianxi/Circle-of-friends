package anim.activity;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.asyey.footballlibrary.network.exception.HttpException;
import com.asyey.footballlibrary.network.http.ResponseInfo;
import com.asyey.footballlibrary.universalimageloader.core.DisplayImageOptions;
import com.asyey.footballlibrary.universalimageloader.core.ImageLoader;
import com.henanjianye.soon.communityo2o.BaseActivity;
import com.henanjianye.soon.communityo2o.R;
import com.henanjianye.soon.communityo2o.adapter.MyCretificationAdapter;
import com.henanjianye.soon.communityo2o.common.Constant;
import com.henanjianye.soon.communityo2o.common.UserDataHelper;
import com.henanjianye.soon.communityo2o.common.enties.BaseBean;
import com.henanjianye.soon.communityo2o.common.enties.BaseDataBean;
import com.henanjianye.soon.communityo2o.common.enties.JsonUtil;
import com.henanjianye.soon.communityo2o.common.enties.UserBean;
import com.henanjianye.soon.communityo2o.common.util.CommonUtils;
import com.henanjianye.soon.communityo2o.common.util.NetWorkStateUtils;
import com.henanjianye.soon.communityo2o.common.util.SharedPreferencesUtil;
import com.henanjianye.soon.communityo2o.common.util.UserSharedPreferencesUtil;
import com.henanjianye.soon.communityo2o.fragment.Fragment03;
import com.henanjianye.soon.communityo2o.interf.NetWorkCallback;
import com.henanjianye.soon.communityo2o.view.MyListView;
import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.tencent.qzone.QZone;
import cn.sharesdk.wechat.friends.Wechat;

/**
 * Created by sms on 2015/9/11.
 */
public class PersonalActivity extends BaseActivity implements PlatformActionListener, View.OnClickListener {
    private TextView text;
    private ImageView system_back;
    private TextView tv_nick_name;
    private TextView tvbind_weixin;
    private TextView tvbind_QQ;
    private TextView tvbind_weibo;
    //    private ImageView iv_person_photo;
//    private Bitmap mUserPhoto;
    private String nickname;
    private String realname;
    private String bindPhoneNum;
    private TextView tv_real_name;
    private TextView tv_realTel;
    private UserDataHelper userDataHelper;
    private UserSharedPreferencesUtil userSharedPreferencesUtil;
    private String weixinHao;
    private String qq;
    private String weibo;
    private RemoveBandDialog removeBandDialog;
    private String NEW;
    private String OLD;
    private TextView tv_title_bar_right;
    private RelativeLayout rl_person_certificate;
    private RelativeLayout rl_person_info_top;
    private RelativeLayout rl_person_user_nickname;
    private RelativeLayout rl_person_realname;
    private RelativeLayout rl_person_sex;
    private RelativeLayout rl_person_tel;
    private RelativeLayout rl_person_pws;
    private RelativeLayout rl_wei_xin;
    private RelativeLayout rl_person_qq;
    private RelativeLayout rl_person_weibo;
    private String sex;
    private TextView tv_personsex;
    private Button PositiveButton;
    private Button NegativeButton;
    private TextView tv_dialog;
    private ImageView iv_person_photo;
    private int gander;
    private TextView tv_username;
    private TextView tv_confirm_register;
    private MyListView my_certification_ListView;
    //
    private String houseFullAddress;
    private int applicant;
    private ImageView iv_back0;
    private List<Object> list;
    //    private BaseDataBean<UserBean> houseList;
    private String houseDataJson;
    private String smallPicUrl;
    ImageLoader imageLoader = ImageLoader.getInstance();
    DisplayImageOptions build = new DisplayImageOptions.Builder()
            .showImageOnFail(R.mipmap.def_personal_image)
            .showImageForEmptyUri(R.mipmap.def_personal_image).build();
    private boolean isReturnImg = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_personal);
        initView();
        setListener();
        processLogic();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        returnImg();
    }

    private void returnImg() {
//        if (isReturnImg) {
            Intent intet = new Intent();
            intet.putExtra(Fragment03.GET_SMALL_PHOTOURL, photoUrl);
            setResult(RESULT_OK, intet);
//        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ShareSDK.stopSDK(this);
    }


    private void processLogic() {
        userDataHelper = new UserDataHelper(getApplication());
        userSharedPreferencesUtil = new UserSharedPreferencesUtil();
//        updateHouseCreticition(applicant, houseFullAddress);
        if (adapter == null) {
            adapter = new MyCretificationAdapter(PersonalActivity.this);
        }
        my_certification_ListView.setAdapter(adapter);
        String data = SharedPreferencesUtil.getStringData(this, Constant.CachTag.APP_COMMDITY_HOUSE_LIST, "");
        if (data != null) {
            parseHouseInfo(data);
            if (adapter.getCount() != 0) {
                tv_confirm_register.setVisibility(View.GONE);
                iv_back0.setVisibility(View.GONE);
            } else {
                tv_confirm_register.setVisibility(View.VISIBLE);
                iv_back0.setVisibility(View.VISIBLE);
            }
        }
        requestHouseData();
    }

    public String getSDPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState()
                .equals(Environment.MEDIA_MOUNTED);  //判断sd卡是否存在

        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();//获取跟目录
            return sdDir.toString();
        } else {
            return "";
        }
    }

    //数据传递
    private String photoUrl = "";

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SelectPortraitActivity.CHANGE_USER_PHOTO:
                //改变小头像
                if (data == null) {
                    return;
                }
                photoUrl = data.getStringExtra(SelectPortraitActivity.TRANSFER_USER_PHOTO);
                if (photoUrl != null && !photoUrl.equals("")) {
                    if (!getSDPath().equals("")) {
                        isReturnImg = true;
                        imageLoader.displayImage("file://" + photoUrl, iv_person_photo, build);
                    } else {
                        toast("SD卡不存在");
                    }
                }
                return;
        }
        switch (resultCode) {
            case ChangeNickNameActivity.CHANGE_NICKNAMEA_CTIVITY:
                nickname = data.getStringExtra("nickname");
                if (nickname != null) {
                    tv_nick_name.setText(nickname);
                }

//                tv_nick_name.setText(nickname);
                break;
            case ChangeRealNameActivity.CHANGE_REAL_NAME_ACTIVITY:
                realname = data.getStringExtra("realname");
                if (realname != null) {
                    tv_real_name.setText(realname);
                }
                break;
            case ChangeTelActivity.CHANGE_PHONE_NUMBER:
                bindPhoneNum = data.getStringExtra("bindPhoneNum");
                if (bindPhoneNum != null) {
                    tv_realTel.setText(bindPhoneNum);
                }
                break;
            case ChangePasswordActivity.CHANGE_PASSWORD_ACTIVITY:
                OLD = data.getStringExtra("OLD");
                NEW = data.getStringExtra("NEW");
                break;
            case ChangeSExActivity.CHANGE_SEX_ACTIVITY:
                int i = data.getIntExtra("sex", 3);
                if (i == 1) {
                    tv_personsex.setText("男");
                } else if (i == 2) {
                    tv_personsex.setText("女");
                } else if (i == 3) {
                    tv_personsex.setText("保密");
                }
                break;
        }
    }

    public MyCretificationAdapter adapter;

    public void initView() {
        ShareSDK.initSDK(this);
//        int orgId = UserSharedPreferencesUtil.getUserInfo(this, UserSharedPreferencesUtil.ORGID, -1);
//        userDataHelper.login(getNetRequestHelper(this, username, password, orgId));
        adapter = new MyCretificationAdapter(PersonalActivity.this);
        text = (TextView) findViewById(R.id.tv_title_bar_middle);
        text.setText("个人信息");
        tv_title_bar_right = (TextView) findViewById(R.id.tv_title_bar_right);
        tv_title_bar_right.setText("");
        system_back = (ImageView) findViewById(R.id.iv_title_bar_left);
        rl_person_certificate = (RelativeLayout) findViewById(R.id.rl_person_certificate);
        rl_person_info_top = (RelativeLayout) findViewById(R.id.rl_person_info_top);
        rl_person_user_nickname = (RelativeLayout) findViewById(R.id.rl_person_user_nickname);
        rl_person_realname = (RelativeLayout) findViewById(R.id.rl_person_realname);
        rl_person_sex = (RelativeLayout) findViewById(R.id.rl_person_sex);
        rl_person_tel = (RelativeLayout) findViewById(R.id.rl_person_tel);
        rl_person_pws = (RelativeLayout) findViewById(R.id.rl_person_pws);
        rl_wei_xin = (RelativeLayout) findViewById(R.id.rl_wei_xin);
        rl_person_qq = (RelativeLayout) findViewById(R.id.rl_person_qq);
        rl_person_weibo = (RelativeLayout) findViewById(R.id.rl_person_weibo);
        tv_nick_name = (TextView) findViewById(R.id.tv_nick_name);
        tvbind_weixin = (TextView) findViewById(R.id.tvbind_weixin);
        tvbind_QQ = (TextView) findViewById(R.id.tvbind_QQ);
        tvbind_weibo = (TextView) findViewById(R.id.tvbind_weibo);
        tv_real_name = (TextView) findViewById(R.id.tv_real_name);
        tv_realTel = (TextView) findViewById(R.id.tv_realTel);
        tv_personsex = (TextView) findViewById(R.id.tv_personsex);
        tv_dialog = (TextView) findViewById(R.id.tv_dialog);
        iv_person_photo = (ImageView) findViewById(R.id.iv_person_photo);
        String picurl = UserSharedPreferencesUtil.getUserInfo(this, UserSharedPreferencesUtil.SMALLPICURL, "");
        ImageLoader imageLoader2 = ImageLoader.getInstance();
        DisplayImageOptions builder3 = new DisplayImageOptions.Builder()
                .showImageOnFail(R.mipmap.def_head_image)
                .showImageForEmptyUri(R.mipmap.def_head_image).build();
        imageLoader2.displayImage(picurl, iv_person_photo, builder3);
        tv_username = (TextView) findViewById(R.id.tv_username);

        /**
         * 身份认证相关的id
         */
        tv_confirm_register = (TextView) findViewById(R.id.tv_confirm_register);
        iv_back0 = (ImageView) findViewById(R.id.iv_back0);
        my_certification_ListView = (MyListView) findViewById(R.id.my_certification_ListView);
        initUserInfo();
//        tv_nick_name.setText(mParam1);
//        iv_person_photo = (ImageView)view.findViewById(R.id.iv_person_photo);
    }
//          房屋认证信息
//    private void updateHouseCreticition(int applicant,String houseFullAddress ) {
//        userDataHelper.getCretification(getNetRequestHelper(this),applicant,houseFullAddress);
//    }

    //初始化用户信息
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void initUserInfo() {
        if (!TextUtils.isEmpty(userSharedPreferencesUtil.getUserInfo(this).nickname)) {
            tv_nick_name.setText(userSharedPreferencesUtil.getUserInfo(this).nickname);
        } else if (TextUtils.isEmpty(userSharedPreferencesUtil.getUserInfo(this).nickname)) {
            tv_nick_name.setText("");
        }
        if (!TextUtils.isEmpty(userSharedPreferencesUtil.getUserInfo(this).weixin)) {
            tvbind_weixin.setText("解除绑定");
            tvbind_weixin.setTextColor(Color.parseColor("#e5e5e5"));
        } else {
            tvbind_weixin.setText("点击绑定");
        }
        if (!TextUtils.isEmpty(userSharedPreferencesUtil.getUserInfo(this).qq)) {
            tvbind_QQ.setText("解除绑定");
            tvbind_QQ.setTextColor(Color.parseColor("#e5e5e5"));
        } else {
            tvbind_QQ.setText("点击绑定");
        }
        if (!TextUtils.isEmpty(userSharedPreferencesUtil.getUserInfo(this).weibo)) {
            tvbind_weibo.setText("解除绑定");
            tvbind_weibo.setTextColor(Color.parseColor("#e5e5e5"));
        } else {
            tvbind_weibo.setText("点击绑定");
        }
        if (!TextUtils.isEmpty(userSharedPreferencesUtil.getUserInfo(this).realname)) {
            tv_real_name.setText(userSharedPreferencesUtil.getUserInfo(this).realname);
        } else if (TextUtils.isEmpty(userSharedPreferencesUtil.getUserInfo(this).realname)) {
            tv_real_name.setText("");
        }
        if (!TextUtils.isEmpty(userSharedPreferencesUtil.getUserInfo(this).mobilephone)) {
            tv_realTel.setText(userSharedPreferencesUtil.getUserInfo(this).mobilephone);
        } else {
            tv_realTel.setText("");
        }
        gander = userSharedPreferencesUtil.getUserInfo(this).sex;
        if (!TextUtils.isEmpty(gander + "")) {
            if (gander == 1) {
                tv_personsex.setText("男");
            } else if (gander == 2) {
                tv_personsex.setText("女");
            } else if (gander == 3) {
                tv_personsex.setText("保密");
            }
        } else {
            tv_personsex.setText("");
        }
        if (!TextUtils.isEmpty(userSharedPreferencesUtil.getUserInfo(this).username)) {
            tv_username.setText(userSharedPreferencesUtil.getUserInfo(this).username);
        } else {
            tv_username.setText("");
        }
    }

    //系统返回键
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            returnImg();
            PersonalActivity.this.finish();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    public void setListener() {
        system_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                startActivity(new Intent(PersonalActivity.this,MineActivity.class));
                returnImg();
                PersonalActivity.this.finish();
            }
        });
        rl_person_certificate.setOnClickListener(this);
        rl_person_info_top.setOnClickListener(this);
        rl_person_user_nickname.setOnClickListener(this);
        rl_person_realname.setOnClickListener(this);
        rl_person_sex.setOnClickListener(this);
        rl_person_tel.setOnClickListener(this);
        rl_person_pws.setOnClickListener(this);
        rl_wei_xin.setOnClickListener(this);
        rl_person_qq.setOnClickListener(this);
        rl_person_weibo.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_person_certificate:
                if (adapter.getCount() == 0) {
                    CommonUtils.startLoginVerifyActivity(this);
                }
//                replaceFragmentWithStack(R.id.base_content_fragment, LoginVerifyIdentityFragment.newInstance(null, null), true);
                break;
            case R.id.rl_person_info_top:
//                toast("更改头像");//跳入页面
                Intent intent = new Intent(this, SelectPortraitActivity.class);
                intent.putExtra(SelectPortraitActivity.IMAGE_TYPE, SelectPortraitActivity.TYPE.SMALLIMAGE);
                startActivityForResult(intent, SelectPortraitActivity.CHANGE_USER_PHOTO);
                break;
            case R.id.rl_person_user_nickname://跳入页面
                startActivityForResult(new Intent(PersonalActivity.this, ChangeNickNameActivity.class), 1);
                break;
            case R.id.rl_person_realname:
                startActivityForResult(new Intent(PersonalActivity.this, ChangeRealNameActivity.class), 2);
                break;
            case R.id.rl_person_sex:

                startActivityForResult(new Intent(PersonalActivity.this, ChangeSExActivity.class), 5);
                break;
            case R.id.rl_person_tel:
                startActivityForResult(new Intent(PersonalActivity.this, ChangeTelActivity.class), 3);
                break;
            case R.id.rl_person_pws:
                startActivityForResult(new Intent(PersonalActivity.this, ChangePasswordActivity.class), 4);
                break;
            case R.id.rl_wei_xin:
                BindWeiXin();
                break;
            case R.id.rl_person_qq:
                BindQQ();
                break;
            case R.id.rl_person_weibo:
                BindWeiBo();
            case R.id.PositiveButton:
//                PositiveButton.setTextColor(Color.parseColor("#FF0000"));
                RemoveBind();
                break;
            case R.id.NegativeButton:
                NegativeButton.setTextColor(Color.parseColor("#FF0000"));
                removeBandDialog.dismiss();
                break;
        }
    }

    //      解除绑定的操作
    private void RemoveBind() {
        if (!TextUtils.isEmpty(weixinHao)) {
//            解除绑定
            userDataHelper.Remove_Third_Bind(getNetRequestHelper(this), "WeiChat");
            removeBandDialog.dismiss();
        }
        if (!TextUtils.isEmpty(qq)) {
//        解除绑定
            userDataHelper.Remove_Third_Bind(getNetRequestHelper(this), "QZone");
            removeBandDialog.dismiss();
        }
        if (!TextUtils.isEmpty(weibo)) {
            //解除绑定操作
            userDataHelper.Remove_Third_Bind(getNetRequestHelper(this), "SinaWeibo");
            removeBandDialog.dismiss();
        }
    }

    //显示解除绑定时的Dialog
    private void DialogShow() {
//        改变dialog透明度的代码
        removeBandDialog = new RemoveBandDialog(this, R.style.MyRemoveBindDialog);
        Window win = removeBandDialog.getWindow();
        win.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams lp = win.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        win.setAttributes(lp);
        removeBandDialog.show();
        PositiveButton = (Button) removeBandDialog.findViewById(R.id.PositiveButton);
        NegativeButton = (Button) removeBandDialog.findViewById(R.id.NegativeButton);
        PositiveButton.setTextColor(Color.parseColor("#00A3FF"));
        NegativeButton.setTextColor(Color.parseColor("#00A3FF"));
        PositiveButton.setOnClickListener(this);
        NegativeButton.setOnClickListener(this);
    }

    //    toast简单
    public void toast(String text) {
        try {
            if (!TextUtils.isEmpty(text))
                Toast.makeText(getApplication(), text, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
        }
    }

    //绑定微信
    private void BindWeiXin() {
//        判断用户是否绑定
//        根据用户的微信号来判断是否绑定微信
        weixinHao = userSharedPreferencesUtil.getUserInfo(this, UserSharedPreferencesUtil.WEIXIN, null).toString();
        if (TextUtils.isEmpty(weixinHao)) {
//            绑定操作
            Platform wechat = ShareSDK.getPlatform(Wechat.NAME);
            authorize(wechat);
        } else {
            DialogShow();
//            tv_dialog.setText("是否取消对微信的绑定");
        }
    }

    //绑定QQ
    private void BindQQ() {
        //        判断用户是否绑定
        qq = userSharedPreferencesUtil.getUserInfo(this, UserSharedPreferencesUtil.QQ, null).toString();
        if (TextUtils.isEmpty(qq)) {
//绑定操作
            Platform qzone = ShareSDK.getPlatform(QZone.NAME);
            authorize(qzone);
        } else {
            DialogShow();
//            tv_dialog.setText("是否取消对QQ的绑定");
        }
    }

    //绑定微博
    private void BindWeiBo() {
//        判断用户是否绑定
        weibo = userSharedPreferencesUtil.getUserInfo(this, UserSharedPreferencesUtil.WEIBO, null).toString();

        if (TextUtils.isEmpty(weibo)) {
            //绑定操作
            Platform weibo = ShareSDK.getPlatform(SinaWeibo.NAME);
            authorize(weibo);

        } else {
            DialogShow();
//            tv_dialog.setText("是否取消对微博的绑定");
        }
    }

    //
    private void authorize(Platform plat) {
        if (plat == null) {
            //TODO 平台不支持处理
            return;
        }
        String userId = plat.getDb().getUserId();
        String token = plat.getDb().getToken();
        if (!TextUtils.isEmpty(userId) && !TextUtils.isEmpty(token)) {
            //TODO  判断是否已经认证
            userDataHelper.submmitThirdPartyLogin(getNetRequestHelper(this), plat.getName(), userId, token, plat.getDb().getUserName(), plat.getDb().getUserGender(), plat.getDb().getUserIcon());
        } else {
            plat.setPlatformActionListener(this);
            plat.authorize();
            // 关闭SSO授权
            plat.SSOSetting(true);
        }
    }

    // 三方登陆回调接口
    @Override
    public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
        userDataHelper.submmitThirdPartyLogin(getNetRequestHelper(getApplication()), platform.getName(), platform.getDb().getUserId(), platform.getDb().getToken(), platform.getDb().getUserName(),
                platform.getDb().getUserGender(), platform.getDb().getUserIcon());
    }

    @Override
    public void onError(Platform platform, int i, Throwable throwable) {
        toast(platform.getName() + "登录失败");
    }

    @Override
    public void onCancel(Platform platform, int i) {
        toast(platform.getName() + "操作已经取消");
    }

    protected NetWorkCallback setNetWorkCallback() {
        return new NetWorkCallback() {
            @Override
            public void onStart(String requestTag) {
            }

            @Override
            public void onCancelled(String requestTag) {

            }

            @Override
            public void onLoading(long total, long current, boolean isUploading, String requestTag) {
            }

            @Override
            protected void finalize() throws Throwable {
                super.finalize();
            }

            @Override
            public void onSuccess(ResponseInfo<String> responseInfo, String requestTag) {
                BaseDataBean<UserBean> baseDataBean = null;
                BaseBean<UserBean> Data = null;
                if (requestTag == (Constant.UserUrl.REMOVE_THIRD_PARTY)) {
                    baseDataBean = JsonUtil.parseDataObject(responseInfo.result, UserBean.class);
                    if (baseDataBean.code == 100) {
                        UserBean userBean = baseDataBean.data;
                        UserSharedPreferencesUtil.savaUserInfo(getApplication(), userBean);
                        UserSharedPreferencesUtil.savaUserJsonInfo(getApplication(), responseInfo.result);
                        toast("保存数据成功");
                        toast("解除绑定成功");
////                  tv_result.setText(userBean.toString());
//                    if(!TextUtils.isEmpty(weixinHao)){
//                        tvbind_weixin.setText("解除绑定");
//                        tvbind_weixin.setTextColor(0xFF0000FF);
//                    }
//                    if(!TextUtils.isEmpty(qq)){
//                        tvbind_QQ.setText("解除绑定");
//                        tvbind_QQ.setTextColor(0xFF0000FF);
//                    }
//                    if(!TextUtils.isEmpty(weibo)){
//                        tvbind_weibo.setText("解除绑定");
//                        tvbind_weibo.setTextColor(0xFF0000FF);
                    } else {
                        toast(baseDataBean.msg);
                    }
                }
                if (requestTag == Constant.HouseUrl.GETALL) {
                    parseHouseInfo(responseInfo.result);
                    rl_person_certificate.setEnabled(true);
                    if (adapter.getCount() != 0) {
                        UserSharedPreferencesUtil.savaUserInfo(PersonalActivity.this, UserSharedPreferencesUtil.HOUSECERTSTATUS, 2);
                        tv_confirm_register.setVisibility(View.GONE);
                        iv_back0.setVisibility(View.GONE);
                    } else {
                        tv_confirm_register.setVisibility(View.VISIBLE);
                        if(UserSharedPreferencesUtil.getUserLoginState(PersonalActivity.this)==4){
                            rl_person_certificate.setEnabled(false);
                            tv_confirm_register.setText("认证中");
                            iv_back0.setVisibility(View.INVISIBLE);
                        }else{
                            tv_confirm_register.setText("未认证");
                            iv_back0.setVisibility(View.VISIBLE);
                        }

                    }
                }
            }

            @Override
            public void onFailure(HttpException error, String msg, String requestTag) {
                NetWorkStateUtils.errorNetMes(PersonalActivity.this);
            }
        };

    }

    //请求房屋信息
    private void requestHouseData() {
        userDataHelper.getHouseInfo(getNetRequestHelper(PersonalActivity.this).isShowProgressDialog(false));
    }

    private void parseHouseInfo(String result) {
        BaseBean<UserBean.House> userData = JsonUtil.jsonArray(result, UserBean.House.class);
        if (userData.code == 100) {
            if (userData.data != null && userData.data.size() > 0) {
                List<UserBean.House> houseList = userData.data;
                if (adapter == null) {
                    adapter = new MyCretificationAdapter(PersonalActivity.this);
                }
                adapter.addData(houseList);
                adapter.notifyDataSetChanged();
            }
        } else {
            toast(userData.msg);
        }
    }
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(this.getClass().getSimpleName()); //统计页面(仅有Activity的应用中SDK自动调用，不需要单独写)
        MobclickAgent.onResume(this);          //统计时长
    }
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(this.getClass().getSimpleName()); // （仅有Activity的应用中SDK自动调用，不需要单独写）保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息
        MobclickAgent.onPause(this);
    }
// _________________________________________________________________________________________________
}
