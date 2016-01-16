package anim.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.asyey.footballlibrary.network.exception.HttpException;
import com.asyey.footballlibrary.network.http.ResponseInfo;
import com.henanjianye.soon.communityo2o.BaseActivity;
import com.henanjianye.soon.communityo2o.R;
import com.henanjianye.soon.communityo2o.common.Constant;
import com.henanjianye.soon.communityo2o.common.UserDataHelper;
import com.henanjianye.soon.communityo2o.common.enties.BaseDataBean;
import com.henanjianye.soon.communityo2o.common.enties.JsonUtil;
import com.henanjianye.soon.communityo2o.common.enties.VerifyBean;
import com.henanjianye.soon.communityo2o.common.util.MyTitleBarHelper;
import com.henanjianye.soon.communityo2o.common.util.UserSharedPreferencesUtil;
import com.henanjianye.soon.communityo2o.common.view.MyCountTimer;
import com.henanjianye.soon.communityo2o.interf.NetWorkCallback;

public class PursePsdResetActivity extends BaseActivity {
    private MyTitleBarHelper myTitleBarHelper;
    private MyCountTimer myCountTimer;
    private Button send_control;
    private EditText writenum;
    private String phoneNum = "";
    private TextView tv_phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.purse_password_reverse);
        initViews();
        initEvents();
        if (!phoneNum.equals("")) {
            getcode(phoneNum);
        } else {
            toast("未获得手机号码");
        }
    }


    private void initViews() {
        phoneNum = UserSharedPreferencesUtil.getUserInfo(this, UserSharedPreferencesUtil.MOBILEPHONE, "");
        myTitleBarHelper = new MyTitleBarHelper(this, getWindow().getDecorView().findViewById(android.R.id.content));
        myTitleBarHelper.setLeftImgVisible(false);
        myTitleBarHelper.resetState();
        myTitleBarHelper.setMiddleText("重置支付密码");
        myTitleBarHelper.setRightText("下一步");
//        myTitleBarHelper.setRightTxVisible(false);
        myTitleBarHelper.setLeftImag(R.mipmap.btn_back);
        send_control = (Button) findViewById(R.id.send_control);
        writenum = (EditText) findViewById(R.id.writenum);
        tv_phone = (TextView) findViewById(R.id.tv_phone);
        tv_phone.setText(phoneNum);
        myCountTimer = new MyCountTimer(60 * 1000, 1000, send_control, "获取验证码").setNormalColor(Color.parseColor("#505050")).setNormalBackgroundColor(Color.parseColor("#000000")).setTimingBackgroundColor(Color.parseColor("#dadada"));
    }

    private void initEvents() {
        myTitleBarHelper.setOnclickListener(this);
        send_control.setOnClickListener(this);
    }

    private String code;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_title_bar_left:
                finish();
                break;
            case R.id.tv_title_bar_right:
                code = writenum.getText().toString();
                if (TextUtils.isEmpty(code)) {
                    toast("验证码不能为空");
                    return;
                }
                requestCheckCode(phoneNum, code);
                break;
            case R.id.send_control:
                if (!phoneNum.equals("")) {
                    getcode(phoneNum);
                } else {
                    toast("未获得手机号码");
                }
                break;
            default:
                break;
        }
    }

    private void getcode(String phone) {
        new UserDataHelper(this).getVerifyCode(getNetRequestHelper(this).isShowProgressDialog(true), phone);
    }

    // 请求检查验证码
    private void requestCheckCode(String phoneNum, String code) {
        new UserDataHelper(this).phoneVerify(getNetRequestHelper(this), phoneNum, code);
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
                if (Constant.Purse.VERIFY_CODE.equals(requestTag)) {
                    BaseDataBean<VerifyBean> json = JsonUtil.parseDataObject(responseInfo.result, VerifyBean.class);
                    if (json.code == 100) {
                        VerifyBean vBean = json.data;
                        if (vBean != null) {
                            phoneNum = vBean.mobilephone;
                            toast(json.msg);
                            if (myCountTimer != null) {
                                myCountTimer.start();
                            }
                        }
                    }

                } else if (requestTag.equals(Constant.Purse.VERIFY_INPUT_CODE)) {
                    BaseDataBean<Object> json = JsonUtil.parseDataObject(responseInfo.result, Object.class);
                    if (json.code == 100) {
                        Intent intent = new Intent(PursePsdResetActivity.this, PursePsdManagerActivity.class);
                        intent.putExtra(Constant.Purse.PASSWORDFLAG, Constant.Purse.PASSWORDINPUTNEW);
                        intent.putExtra(Constant.Purse.PSD_VERIFY_FLAG, code);
                        startActivity(intent);
                        if (PursePsdModifyActivity.Act != null) {
                            PursePsdModifyActivity.Act.finish();
                        }
                        finish();
                    } else {
                        toast(json.msg);
                    }

                }
            }

            @Override
            public void onFailure(HttpException error, String msg, String requestTag) {
//                MyLog.e("MMM", "msg is " + msg);
            }
        };
    }
}
