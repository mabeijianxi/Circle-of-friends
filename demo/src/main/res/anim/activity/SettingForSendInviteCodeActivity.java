package anim.activity;

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
import com.henanjianye.soon.communityo2o.common.util.MyTitleBarHelper;
import com.henanjianye.soon.communityo2o.common.util.NetWorkStateUtils;
import com.henanjianye.soon.communityo2o.common.util.UserSharedPreferencesUtil;
import com.henanjianye.soon.communityo2o.eventFunction.bean.EventIsSignupBean;
import com.henanjianye.soon.communityo2o.interf.NetWorkCallback;
import com.umeng.analytics.MobclickAgent;

/**
 * 填写退货物流信息
 */
public class SettingForSendInviteCodeActivity extends BaseActivity {

    private Button bt_invit_submmit;
    private MyTitleBarHelper myTitleBarHelper;
    private UserDataHelper userDataHelper;
    private String code;
    private TextView tv_my_invit_code;
    private EditText et_invit_code;

    @Override
    public int mysetContentView() {
        return R.layout.setting_send_invit_code;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        intView();
    }
    private void intView() {
        bt_invit_submmit = (Button) findViewById(R.id.bt_invit_submmit);
        tv_my_invit_code= (TextView) findViewById(R.id.tv_my_invit_code);
        et_invit_code= (EditText)findViewById(R.id.et_invit_code);
        bt_invit_submmit.setOnClickListener(this);
        myTitleBarHelper = new MyTitleBarHelper(this, getWindow().getDecorView().getRootView());
        myTitleBarHelper.resetState();
        myTitleBarHelper.setMiddleText("邀请码");
        myTitleBarHelper.setOnclickListener(this);
        code = UserSharedPreferencesUtil.getUserInfo(this, UserSharedPreferencesUtil.RECOMMANDCODE, "");
        if (tv_my_invit_code != null&&!TextUtils.isEmpty(code)) {
            tv_my_invit_code.setText(code);
        }else{
            toast("获取邀请码失败");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_invit_submmit:
                sendCode();
                break;
            case R.id.iv_title_bar_left:
                finish();
                break;
            case R.id.tv_title_bar_right:

                break;
        }
    }

    private void sendCode() {
        if (userDataHelper == null) {
            userDataHelper = new UserDataHelper(this);
        }
       String mycode=et_invit_code.getText().toString();
        if (!TextUtils.isEmpty(mycode)) {
            userDataHelper.sendInvitCode(getNetRequestHelper(this), mycode);
        } else {
            toast("邀请码不能为空！");
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

            }

            @Override
            public void onLoading(long total, long current, boolean isUploading, String requestTag) {

            }

            @Override
            public void onSuccess(ResponseInfo<String> responseInfo, String requestTag) {
                if (requestTag == Constant.UserUrl.REMOVE_THIRD_PARTY) {
                    BaseDataBean<Object> baseBean = JsonUtil.parseDataObject(responseInfo.result, Object.class);
                    if (baseBean.code == 100) {
                        toast(baseBean.msg);
                    } else {
                        toast(baseBean.msg);
                    }
                }

            }

            @Override
            public void onFailure(HttpException error, String msg, String requestTag) {
                NetWorkStateUtils.errorNetMes(SettingForSendInviteCodeActivity.this);
            }
        };
    }

    //解析数据
    private void pareInfo(String result) {
        BaseDataBean<EventIsSignupBean> json = JsonUtil.parseDataObject(result,
                EventIsSignupBean.class);

        String msg = json.msg;
        toast(msg);
        if (json.code == 100) {
            //跳到订单列表界面
            //TODO
            setResult(RESULT_OK, getIntent());
            finish();
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
}
