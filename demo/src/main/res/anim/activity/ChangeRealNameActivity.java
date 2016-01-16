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
 * Created by sms on 2015/9/12.
 */
public class ChangeRealNameActivity extends BaseActivity implements View.OnClickListener {
    private TextView tv_change_what;
    private ImageView iv_change_back;
    private TextView btn_change_nick_save;
    private EditText et_chang_what;
    private String realname;
    private Intent intent;
    public static final int CHANGE_REAL_NAME_ACTIVITY=0x1102;
    private UserDataHelper userDataHelper;

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

    private void setListener() {
        iv_change_back.setOnClickListener(this);
        btn_change_nick_save.setOnClickListener(this);
    }

    private void initView() {
        iv_change_back = (ImageView) findViewById(R.id.iv_title_bar_left);
        btn_change_nick_save = (TextView) findViewById(R.id.tv_title_bar_right);
        btn_change_nick_save.setText("保存");
        btn_change_nick_save.setVisibility(View.VISIBLE);
        et_chang_what = (EditText) findViewById(R.id.et_change_what);
        tv_change_what = (TextView) findViewById(R.id.tv_title_bar_middle);
        tv_change_what.setText("修改真实姓名");
        et_chang_what.setHint("点击修改真实姓名");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_title_bar_left:
//                startActivity(new Intent(ChangeRealNameActivity.this,PersonalActivity.class));
                JianPanUtils.hideIme(this);
                ChangeRealNameActivity.this.finish();
                break;
            case R.id.tv_title_bar_right:
                realname = et_chang_what.getText().toString();
                if(!TextUtils.isEmpty(realname)){
                    requestUpdateRealName(realname);
                }else {
                    toast("真实姓名不能为空");
                }
                break;


        }

    }
    private void requestUpdateRealName(String name) {
        userDataHelper = new UserDataHelper(this);
        userDataHelper.Change_RealName(getNetRequestHelper(this), name);
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
                BaseDataBean<ChangeBeen> baseDataBean = JsonUtil.parseDataObject(responseInfo.result,ChangeBeen.class);
                if(baseDataBean.code == 100){
                    UserSharedPreferencesUtil.savaUserInfo(ChangeRealNameActivity.this,UserSharedPreferencesUtil.REALNAME,realname);
                    intent.putExtra("realname", realname);
                    setResult(CHANGE_REAL_NAME_ACTIVITY, intent);
                    ChangeRealNameActivity.this.finish();
                }else {
                    toast(baseDataBean.msg);
                }
            }
            @Override
            public void onFailure(HttpException error, String msg, String requestTag) {
                NetWorkStateUtils.errorNetMes(ChangeRealNameActivity.this);
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
