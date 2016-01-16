package anim.activity;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.asyey.footballlibrary.network.exception.HttpException;
import com.asyey.footballlibrary.network.http.ResponseInfo;
import com.asyey.footballlibrary.universalimageloader.core.ImageLoader;
import com.henanjianye.soon.communityo2o.BaseActivity;
import com.henanjianye.soon.communityo2o.MainActivity;
import com.henanjianye.soon.communityo2o.R;
import com.henanjianye.soon.communityo2o.common.Constant;
import com.henanjianye.soon.communityo2o.common.MyLog;
import com.henanjianye.soon.communityo2o.common.UserDataHelper;
import com.henanjianye.soon.communityo2o.common.enties.AppUpdateBean;
import com.henanjianye.soon.communityo2o.common.enties.BaseDataBean;
import com.henanjianye.soon.communityo2o.common.enties.JsonUtil;
import com.henanjianye.soon.communityo2o.common.util.CommonUtils;
import com.henanjianye.soon.communityo2o.common.util.DataCleanManager;
import com.henanjianye.soon.communityo2o.common.util.FileToM;
import com.henanjianye.soon.communityo2o.common.util.NetWorkStateUtils;
import com.henanjianye.soon.communityo2o.common.util.SharedPreferencesUtil;
import com.henanjianye.soon.communityo2o.common.util.UserSharedPreferencesUtil;
import com.henanjianye.soon.communityo2o.interf.NetWorkCallback;
import com.kyleduo.switchbutton.SwitchButton;
import com.umeng.analytics.MobclickAgent;

/**
 * Created by sms on 2015/10/5.
 */
public class MySystemActivity extends BaseActivity implements View.OnClickListener {
    private TextView tv_now_version;
    private TextView tv_clear_cache;
    private RelativeLayout rl_aboutus;
    private RelativeLayout rl_fankui;
    private RelativeLayout rl_tuijian;
    private TextView text;
    private ImageView system_back;
    private RelativeLayout rl_version_update;
    private RelativeLayout del_huancun;
    private ImageView iv_red_point;
    private Button exit_button;
    private TextView tv_title_bar_right;
    private RelativeLayout rl_tui_song;
    private SwitchButton switch_save_liuliang;
    private RelativeLayout rl_invite_code;
    private TextView tv_detection;

    @Override
    public int mysetContentView() {

        return R.layout.my_system_seting;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        setListener();
        processLogic();
        redPoint(SharedPreferencesUtil.getStringData(MySystemActivity.this, SharedPreferencesUtil.APP_SERVER_VERSION_JSON, ""));
    }

    private void redPoint(final String updatejson) {
        // 解析升级数据
                /*
                 * HashMap<String, Object> hashMape = new HashMap<String,
				 * Object>(); hashMape.put("osType", 1 + "");
				 * postRequest(Constant.APP_CHECKUODATE, hashMape,
				 * Constant.APP_CHECKUODATE);
				 */
        if(TextUtils.isEmpty(updatejson)){
            return;
        }
        BaseDataBean<AppUpdateBean> json = JsonUtil.parseDataObject(updatejson, AppUpdateBean.class);
        final AppUpdateBean   updateBean=json.data;
        int versionCode =CommonUtils.getVerCode(MySystemActivity.this);
        if (null==updateBean||versionCode >= updateBean.versionCode) {
            iv_red_point.setVisibility(View.GONE);
            return;
        } else {
            iv_red_point.setVisibility(View.VISIBLE);
        }
    }
    public void initView() {
        String isOpen = SharedPreferencesUtil.getStringData(this, "savedatamode", "-1");
        long length = ImageLoader.getInstance().getDiskCache().getDirectory().length();
        String size = FileToM.getFormatSize(length);
//        button = (Button)view.findViewById(R.id.sb_liuliang);//SwitchButton
        tv_now_version = (TextView) findViewById(R.id.tv_now_version);//发现新版本
        tv_detection = (TextView) findViewById(R.id.tv_detection);//发现新版本
        tv_now_version.setText("当前版本 "+getResources().getText(R.string.app_version));
        tv_clear_cache = (TextView) findViewById(R.id.tv_clear_cache);//清空缓存
        rl_aboutus = (RelativeLayout) findViewById(R.id.rl_aboutus);//关于我们
        // rl_fankui = (RelativeLayout) findViewById(R.id.rl_fankui);//反馈
        rl_tuijian = (RelativeLayout) findViewById(R.id.rl_tuijian);//推荐
        text = (TextView) findViewById(R.id.tv_title_bar_middle);//头部文字
        exit_button = (Button) findViewById(R.id.exit);//退出按钮
        text.setText("系统设置");
        tv_clear_cache.setText(size);
        system_back = (ImageView) findViewById(R.id.iv_title_bar_left);
        tv_title_bar_right = (TextView) findViewById(R.id.tv_title_bar_right);
        tv_title_bar_right.setText("");
        //返回按钮的监听
        system_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (UserSharedPreferencesUtil.getUserLoginState(MySystemActivity.this) <= 2) {
                    MySystemActivity.this.finish();
                } else {
                    requsetSaveButtonState("0");
                    MySystemActivity.this.finish();
                }
            }
        });
        //版本更新
        rl_version_update = (RelativeLayout) findViewById(R.id.rl_version_update);
        //清除缓存
        del_huancun = (RelativeLayout) findViewById(R.id.del_huancun);
        //版本更新红点
        iv_red_point = (ImageView) findViewById(R.id.iv_red_point);
        rl_tui_song = (RelativeLayout) findViewById(R.id.rl_tui_song);
        switch_save_liuliang = (SwitchButton) findViewById(R.id.switch_save_liuliang);
        if (isOpen.equals("1")) {
            switch_save_liuliang.setChecked(true);
        } else {
            switch_save_liuliang.setChecked(false);
        }
        rl_invite_code = (RelativeLayout) findViewById(R.id.rl_invite_code);
        rl_invite_code.setOnClickListener(this);
    }

    public void setListener() {
        rl_tuijian.setOnClickListener(this);
        //  rl_fankui.setOnClickListener(this);
        rl_tuijian.setOnClickListener(this);
        del_huancun.setOnClickListener(this);
        rl_version_update.setOnClickListener(this);
        exit_button.setOnClickListener(this);
        rl_tui_song.setOnClickListener(this);
        switch_save_liuliang.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    SharedPreferencesUtil.saveStringData(MySystemActivity.this, "savedatamode", "1");
                } else {
                    SharedPreferencesUtil.saveStringData(MySystemActivity.this, "savedatamode", "0");
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.switch_save_liuliang:
                switch_save_liuliang.setChecked(true);
                break;
            case R.id.rl_tui_song:
//                推送设置
                if (UserSharedPreferencesUtil.getUserLoginState(MySystemActivity.this) < 2) {
                    CommonUtils.startLoginActivity(MySystemActivity.this);
                } else {
                    startActivity(new Intent(MySystemActivity.this, TuiSongActivity.class));
                }
                break;
            case R.id.rl_version_update:
                //TODO 版本更新
//                toast("版本更新zhong");
//                sendIsUpdate();
                new UserDataHelper(MySystemActivity.this).sendIsUpdate(getNetRequestHelper(MySystemActivity.this));
                break;
            case R.id.del_huancun:
                //TODO 清除缓存
                //清除ImageLoader的缓存
                ImageLoader.getInstance().getDiskCache().clear();
                DataCleanManager.cleanApplicationData(MySystemActivity.this);
                DataCleanManager.cleanSharedPreference(MySystemActivity.this);
                tv_clear_cache.setText("");
                toast("清空缓存成功");
                break;
            case R.id.rl_aboutus:
                //TODO 关于我们
                break;
           /* case R.id.rl_fankui:
                //TODO 意见反馈
                startActivity(new Intent(MySystemActivity.this, MyIdeaReturnActivity.class));
                break;*/
            case R.id.rl_tuijian:
                //TODO 推荐应用
                break;
            case R.id.exit:
                //TODO 退出登录
//                提示框
                AlertDialog.Builder builder = new AlertDialog.Builder(MySystemActivity.this);
                builder.setMessage("确认退出吗？");
                builder.setTitle("提示");
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        UserSharedPreferencesUtil.clearUserInfor(MySystemActivity.this);
                        ActivityManager mActivityManager =(ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
//                        mActivityManager.clearApplicationUserData();
                        Intent intent = new Intent(MySystemActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.setAction(MainActivity.EXITE_LOGIN_TAG);
                        startActivity(intent);
                        finish();


                    }
                });
                builder.create().show();
                break;
            case R.id.rl_invite_code:
                // 邀请码
                if (UserSharedPreferencesUtil.getUserLoginState(MySystemActivity.this) < 2) {
                    CommonUtils.startLoginActivity(MySystemActivity.this);
                } else {
                    startActivity(new Intent(this, SettingForSendInviteCodeActivity.class));
                }

                break;


        }
    }

    private void requsetSaveButtonState(String state) {
        UserDataHelper userDataHelper = new UserDataHelper(MySystemActivity.this);
        userDataHelper.saveButton(getNetRequestHelper(MySystemActivity.this), state);
    }

    //退出的逻辑
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)

//    //版本的更新
////    得到当前的版本
//     private String getVersion() {
//    // PackgeManager//包管理器
//    PackageManager pm = getPackageManager();
//    // 得到功能清单文件信息
//    try {
//        PackageInfo packInfo = pm.getPackageInfo(getPackageName(), 0);
//        return packInfo.versionName;
//    } catch (PackageManager.NameNotFoundException e) {
//        // TODO Auto-generated catch block
//        e.printStackTrace();
//        return "";
//    }
//    }




    private void redPoint(final AppUpdateBean updateBean) {
        // 解析升级数据
                /*
                 * HashMap<String, Object> hashMape = new HashMap<String,
				 * Object>(); hashMape.put("osType", 1 + "");
				 * postRequest(Constant.APP_CHECKUODATE, hashMape,
				 * Constant.APP_CHECKUODATE);
				 */
        int versionCode = CommonUtils.getVerCode(MySystemActivity.this);
        if (null==updateBean||versionCode >= updateBean.versionCode) {
            Toast.makeText(getApplicationContext(), "已经是最新版本", Toast.LENGTH_SHORT)
                    .show();
        } else {
            AlertDialog.Builder alert = new AlertDialog.Builder(MySystemActivity.this);
            alert.setTitle("软件升级")
                    .setMessage("发现新版本,建议立即更新使用.")
                    .setPositiveButton("更新",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(
                                        DialogInterface dialog,
                                        int which) {
                                    if (updateBean == null) {
                                        Toast.makeText(
                                                getApplicationContext(),
                                                "已经是最新版本", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Uri uri = Uri
                                                .parse(updateBean.url);
                                        Intent downloadIntent = new Intent(
                                                Intent.ACTION_VIEW, uri);
                                        startActivity(downloadIntent);
                                    }
                                }
                            })
                    .setNegativeButton("取消",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(
                                        DialogInterface dialog,
                                        int which) {
                                    dialog.dismiss();
                                }
                            });
            alert.create().show();
        }
    }


    public void processLogic() {


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

    @Override
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
            public void onSuccess(ResponseInfo<String> responseInfo, String requestTag) {
                MyLog.e("AAA", "onSuccess----" + responseInfo.result);
                if (Constant.AppUrl.APP_COMMON_UPDATE.equals(requestTag)) {
                    BaseDataBean<AppUpdateBean> json = JsonUtil.parseDataObject(responseInfo.result, AppUpdateBean.class);
                    if (json.code == 100) {
                        redPoint(json.data);
                    } else {
                        toast(json.msg);
                    }
                }
            }
            @Override
            public void onFailure(HttpException error, String msg, String requestTag) {
                NetWorkStateUtils.errorNetMes(MySystemActivity.this);
            }
        };
    }
}
