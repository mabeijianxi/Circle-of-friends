package anim.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.asyey.footballlibrary.network.exception.HttpException;
import com.asyey.footballlibrary.network.http.ResponseInfo;
import com.henanjianye.soon.communityo2o.BaseActivity;
import com.henanjianye.soon.communityo2o.R;
import com.henanjianye.soon.communityo2o.common.UserDataHelper;
import com.henanjianye.soon.communityo2o.common.enties.BaseDataBean;
import com.henanjianye.soon.communityo2o.common.enties.ChangeBeen;
import com.henanjianye.soon.communityo2o.common.enties.JsonUtil;
import com.henanjianye.soon.communityo2o.common.util.JianPanUtils;
import com.henanjianye.soon.communityo2o.common.util.NetWorkStateUtils;
import com.henanjianye.soon.communityo2o.common.util.UserSharedPreferencesUtil;
import com.henanjianye.soon.communityo2o.interf.NetWorkCallback;
import com.umeng.analytics.MobclickAgent;

/**
 * Created by sms on 2015/9/11.
 */
public class ChangeNickNameActivity extends BaseActivity implements View.OnClickListener {
    private EditText et_change_what;
    private ImageView iv_change_back;
    private TextView btn_change_save;
    private String changename;
    private Intent intent;
    public static final int CHANGE_NICKNAMEA_CTIVITY = 0x1101;
    private UserDataHelper userDataHelper;
    private TextView tv_title_bar_middle;

    @Override
    public int mysetContentView() {
        return R.layout.change_personal_message;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        setListener();
        initProcess();
    }

    private void initProcess() {
        intent = getIntent();
    }

    public void initView() {
        iv_change_back = (ImageView) findViewById(R.id.iv_title_bar_left);
        et_change_what = (EditText) findViewById(R.id.et_change_what);
//        new SafeKeyboard(et_change_what);
//        new SafeKeyboard(et_change_what, 10, SafeKeyboard.TYPE_DIGIT_ONLY);
        btn_change_save = (TextView) findViewById(R.id.tv_title_bar_right);
        btn_change_save.setText("保存");
        btn_change_save.setVisibility(View.VISIBLE);
        tv_title_bar_middle = (TextView) findViewById(R.id.tv_title_bar_middle);
        tv_title_bar_middle.setText("修改昵称");
    }
    public void setListener() {
        iv_change_back.setOnClickListener(this);
        btn_change_save.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_title_bar_left:
//                startActivity(new Intent(ChangeNickNameActivity.this, PersonalActivity.class));
                JianPanUtils.hideIme(this);
                ChangeNickNameActivity.this.finish();
                break;
            case R.id.tv_title_bar_right:
                changename = et_change_what.getText().toString();
                //TODO 判断空
                if (!TextUtils.isEmpty(changename)) {
                    requestUpdateNikename(changename);
                }else{
                    toast("请输入新的昵称");
                }
                break;
        }
    }
    private void requestUpdateNikename(String name) {
        userDataHelper = new UserDataHelper(this);
        userDataHelper.Change_NikeName(getNetRequestHelper(this), name);
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
                BaseDataBean<ChangeBeen> baseDataBean = JsonUtil.parseDataObject(responseInfo.result, ChangeBeen.class);
                if (baseDataBean.code == 100) {
                    UserSharedPreferencesUtil.savaUserInfo(ChangeNickNameActivity.this, UserSharedPreferencesUtil.NICKNAME, changename);
                    intent.putExtra("nickname", changename);
                    setResult(CHANGE_NICKNAMEA_CTIVITY, intent);
                    ChangeNickNameActivity.this.finish();
                } else {
                    toast(baseDataBean.msg);
                }
            }

            @Override
            public void onFailure(HttpException error, String msg, String requestTag) {
                NetWorkStateUtils.errorNetMes(ChangeNickNameActivity.this);
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
