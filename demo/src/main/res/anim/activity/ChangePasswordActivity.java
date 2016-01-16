package anim.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.asyey.footballlibrary.network.exception.HttpException;
import com.asyey.footballlibrary.network.http.ResponseInfo;
import com.henanjianye.soon.communityo2o.BaseActivity;
import com.henanjianye.soon.communityo2o.R;
import com.henanjianye.soon.communityo2o.common.UserDataHelper;
import com.henanjianye.soon.communityo2o.common.enties.BaseDataBean;
import com.henanjianye.soon.communityo2o.common.enties.JsonUtil;
import com.henanjianye.soon.communityo2o.common.enties.UpdataPasswordBean;
import com.henanjianye.soon.communityo2o.common.util.JianPanUtils;
import com.henanjianye.soon.communityo2o.common.util.NetWorkStateUtils;
import com.henanjianye.soon.communityo2o.interf.NetWorkCallback;
import com.umeng.analytics.MobclickAgent;

/**
 * Created by sms on 2015/9/16.
 */
public class ChangePasswordActivity extends BaseActivity implements View.OnClickListener {
    private TextView old_password;
    private TextView new_password;
    private String OLD;
    private String NEW;
    private TextView btn_submit;
    private ImageView iv_title_bar_left;
    private TextView tv_title_bar_middle;
    private Intent intent;
    public static final int CHANGE_PASSWORD_ACTIVITY = 0x1104;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_password);
        tv_title_bar_middle = (TextView) findViewById(R.id.tv_title_bar_middle);
        tv_title_bar_middle.setText("修改密码");
        old_password = (TextView) findViewById(R.id.old_password);
        new_password = (TextView) findViewById(R.id.new_password);
        btn_submit = (TextView) findViewById(R.id.tv_title_bar_right);
        btn_submit.setText("确定");
        btn_submit.setVisibility(View.VISIBLE);
        btn_submit.setOnClickListener(this);
        iv_title_bar_left = (ImageView) findViewById(R.id.iv_title_bar_left);
        iv_title_bar_left.setOnClickListener(this);
        intent = getIntent();
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.iv_title_bar_left:
//                startActivity(new Intent(ChangePasswordActivity.this,PersonalActivity.class));
                JianPanUtils.hideIme(this);
                ChangePasswordActivity.this.finish();
                break;
            case R.id.tv_title_bar_right:
                OLD = old_password.getText().toString();
                NEW = new_password.getText().toString();
//              检查旧密码是不是正确，如果正确才执行密码的更新；
//                chackOldPassword();
                if (TextUtils.isEmpty(OLD)) {
                    toast("请输入初始密码");
                    return;
                }
                if (TextUtils.isEmpty(NEW)) {
                    toast("请输入新密码");
                    return;
                }
                requestChangePassword();
                break;
        }
    }

//    private void chackOldPassword() {
//
//    }

    private void requestChangePassword() {
        UserDataHelper userDataHelper = new UserDataHelper(this);
        userDataHelper.updatePassword(getNetRequestHelper(this), OLD, NEW);
    }

    @Override
    protected NetWorkCallback setNetWorkCallback() {
        return new NetWorkCallback() {
            @Override
            public void onStart(String requestTag) {

            }

            @Override
            public void onCancelled(String requestTag) {
                toast("操作已取消");
            }

            @Override
            public void onLoading(long total, long current, boolean isUploading, String requestTag) {

            }

            @Override
            public void onSuccess(ResponseInfo<String> responseInfo, String requestTag) {
                BaseDataBean baseDataBean = JsonUtil.parseDataObject(responseInfo.result, UpdataPasswordBean.class);
                if (baseDataBean.code == 100) {
                    intent.putExtra("OLD", OLD);
                    intent.putExtra("NEW", NEW);
                    setResult(CHANGE_PASSWORD_ACTIVITY, intent);
                    ChangePasswordActivity.this.finish();
                } else {
                    toast(baseDataBean.msg);
                }
            }

            @Override
            public void onFailure(HttpException error, String msg, String requestTag) {
                NetWorkStateUtils.errorNetMes(ChangePasswordActivity.this);
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
