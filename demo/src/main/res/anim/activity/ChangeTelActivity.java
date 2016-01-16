package anim.activity;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.asyey.footballlibrary.network.exception.HttpException;
import com.asyey.footballlibrary.network.http.ResponseInfo;
import com.henanjianye.soon.communityo2o.BaseActivity;
import com.henanjianye.soon.communityo2o.R;
import com.henanjianye.soon.communityo2o.common.Constant;
import com.henanjianye.soon.communityo2o.common.UserDataHelper;
import com.henanjianye.soon.communityo2o.common.enties.BaseDataBean;
import com.henanjianye.soon.communityo2o.common.enties.JsonUtil;
import com.henanjianye.soon.communityo2o.common.util.CommonUtils;
import com.henanjianye.soon.communityo2o.common.util.JianPanUtils;
import com.henanjianye.soon.communityo2o.common.util.NetWorkStateUtils;
import com.henanjianye.soon.communityo2o.common.util.UserSharedPreferencesUtil;
import com.henanjianye.soon.communityo2o.common.view.MyCountTimer;
import com.henanjianye.soon.communityo2o.interf.NetWorkCallback;
import com.umeng.analytics.MobclickAgent;

/**
 * Created by sms on 2015/9/12.
 */
public class ChangeTelActivity extends BaseActivity implements View.OnClickListener {


    private ImageView iv_change_back;
    private TextView tv_change_what;
    private TextView writePhone;
    private String bindPhoneNum;
    private TextView describe;
    private Button send_verification;
    private TextView write_Verification;
    private String Verification;
    public static final int CHANGE_PHONE_NUMBER = 0x1103;
    //    自动回写入验证码的操作
    public static final int MSG_RECEIVED_CODE = 1;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_RECEIVED_CODE) {
                String code = (String) msg.obj;
                write_Verification.setText(code);
            }
        }
    };
    private Intent intent;
    private UserDataHelper userDataHelper;
    private SMSObserver mObserver;
    private TextView tv_title_bar_right;
    private MyCountTimer myCountTimer;
    private String starphone;

    @Override
    public int mysetContentView() {
        return R.layout.change_tel;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        短信自动回填的功能
        mObserver = new SMSObserver(ChangeTelActivity.this, handler);
        Uri uri = Uri.parse("content://sms");
        getContentResolver().registerContentObserver(uri, true, mObserver);
        initView();
        setListener();
        initProcess();
    }

    private void initProcess() {
        intent = getIntent();
    }

    private void initView() {
        iv_change_back = (ImageView) findViewById(R.id.iv_title_bar_left);
        writePhone = (TextView) findViewById(R.id.writePhone);
        describe = (TextView) findViewById(R.id.describe);
        write_Verification = (TextView) findViewById(R.id.write_Verification);
        send_verification = (Button) findViewById(R.id.send_verification);
        tv_change_what = (TextView) findViewById(R.id.tv_title_bar_middle);
        tv_change_what.setText("更改绑定手机号");
        tv_title_bar_right = (TextView) findViewById(R.id.tv_title_bar_right);
        tv_title_bar_right.setText("提交");
        tv_title_bar_right.setVisibility(View.VISIBLE);
        writePhone = (TextView) findViewById(R.id.writePhone);
        /**
         * 通过shareProference获得starPhone 带星号的Number 同时set到Text中
         */
        UserSharedPreferencesUtil userSharedPreferencesUtil = new UserSharedPreferencesUtil();
        starphone = userSharedPreferencesUtil.getUserInfo(this).starphone;
        if (!TextUtils.isEmpty(starphone)) {
            describe.setText("您当前绑定的手机号是" + starphone + "\n" + "请输入新的手机号");
            describe.setTextColor(Color.parseColor("#8e8e8e"));
            describe.setGravity(Gravity.CENTER);
        } else {
            describe.setVisibility(View.INVISIBLE);
        }
    }

    private void setListener() {
        iv_change_back.setOnClickListener(this);
        send_verification.setOnClickListener(this);
        tv_title_bar_right.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_title_bar_left:
//                startActivity(new Intent(ChangeTelActivity.this,PersonalActivity.class));
                JianPanUtils.hideIme(this);
                ChangeTelActivity.this.finish();
                break;
            case R.id.send_verification:
                bindPhoneNum = writePhone.getText().toString();

//                       之前绑定的手机号这里错了，取的是当前填的手机号码
                if (TextUtils.isEmpty(bindPhoneNum)) {
                    Toast.makeText(this, "绑定手机号不能为空", Toast.LENGTH_LONG).show();
                } else {

                    /**
                     * 发送验证码的请求
                     */
                    sendVerification();
                }
                break;
            case R.id.tv_title_bar_right:
                Verification = write_Verification.getText().toString();
                if (TextUtils.isEmpty(bindPhoneNum)) {
                    Toast.makeText(this, "手机号码不能为空", Toast.LENGTH_LONG).show();
                    return;
                }
                if (TextUtils.isEmpty(Verification)) {
                    Toast.makeText(this, "验证码不能为空", Toast.LENGTH_LONG).show();
                    return;
                }

                if (!TextUtils.isEmpty(Verification) && !TextUtils.isEmpty(bindPhoneNum)) {
                    bindPhoneNum = writePhone.getText().toString();
                    requestUpdatePhoneNumber(bindPhoneNum, Verification);
//                    请求检查验证码
//                    requestChackCode(bindPhoneNum,Verification);
                }

                break;
        }

    }

    // 请求检查验证码
    private void requestChackCode(String phoneNum, String code) {
        userDataHelper.phoneVerifyStep2(getNetRequestHelper(this), phoneNum, code);
    }

    //发送验证码的操作
    private void sendVerification() {
        myCountTimer = new MyCountTimer(60 * 1000, 1000, send_verification, "获取验证码").
                setNormalColor(Color.parseColor("#505050")).setNormalBackgroundColor(Color.parseColor("#000000")).
                setTimingBackgroundColor(Color.parseColor("#dadada"));
        getCode();
    }

    private void getCode() {
        if (TextUtils.isEmpty(bindPhoneNum)) {
            toast("手机号不能为空");
            return;
        }
        if (!CommonUtils.isAvalidPhoneNum(bindPhoneNum)) {
            toast("请填写正确的手机号码");
            return;
        }

        new UserDataHelper(this).getRegistCode(getNetRequestHelper(this), bindPhoneNum);
    }

    private void requestUpdatePhoneNumber(String bindPhoneNum, String phoneNumber) {
        userDataHelper = new UserDataHelper(this);
        userDataHelper.Change_PhoneNumber(getNetRequestHelper(this), phoneNumber, bindPhoneNum);
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

                if (requestTag == Constant.UserUrl.VERIFY_REGIST_GET_CODE) {
                    BaseDataBean<Object> baseDataBean = JsonUtil.parseDataObject(responseInfo.result, Object.class);
                    if (baseDataBean.code == 100 && myCountTimer != null) {
                        myCountTimer.start();
                    } else {
                        toast(baseDataBean.msg);
                    }
                } else if (requestTag == Constant.UserUrl.REGIST1) {
                    BaseDataBean<Object> baseDataBean1 = JsonUtil.parseDataObject(responseInfo.result, Object.class);
                    if (baseDataBean1.code == 100) {
                        UserSharedPreferencesUtil.savaUserInfo(ChangeTelActivity.this, UserSharedPreferencesUtil.MOBILEPHONE, bindPhoneNum);
                        intent.putExtra("bindPhoneNum", bindPhoneNum);
                        setResult(CHANGE_PHONE_NUMBER, intent);
                        toast(baseDataBean1.msg);
                        ChangeTelActivity.this.finish();
                    }

                }
            }

            @Override
            public void onFailure(HttpException error, String msg, String requestTag) {
                NetWorkStateUtils.errorNetMes(ChangeTelActivity.this);
            }
        };
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
}
