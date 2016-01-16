package anim.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
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
import com.henanjianye.soon.communityo2o.common.util.NetWorkStateUtils;
import com.henanjianye.soon.communityo2o.common.util.UserSharedPreferencesUtil;
import com.henanjianye.soon.communityo2o.interf.NetWorkCallback;
import com.umeng.analytics.MobclickAgent;

/**
 * Created by sms on 2015/9/18.
 */
public class ChangeSExActivity extends BaseActivity implements View.OnClickListener{
    private ImageView iv_title_bar_left;
    private TextView tv_title_bar_middle;
    private TextView tv_title_bar_right;
    private TextView tv_sex_male;
    private TextView tv_sex_female;
    private int sex;
    private Intent intent;
    public  static  final  int CHANGE_SEX_ACTIVITY = 0x1105;

    @Override
    public int mysetContentView() {
        return R.layout.change_sex;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        iv_title_bar_left = (ImageView) findViewById(R.id.iv_title_bar_left);
        tv_title_bar_middle = (TextView) findViewById(R.id.tv_title_bar_middle);
        tv_title_bar_middle.setText("选择性别");
        tv_title_bar_right = (TextView) findViewById(R.id.tv_title_bar_right);
        tv_title_bar_right.setText("完成");
        tv_title_bar_right.setVisibility(View.VISIBLE);
        tv_sex_male = (TextView) findViewById (R.id.tv_sex_male);
        tv_sex_female = (TextView) findViewById(R.id.tv_sex_female);
        iv_title_bar_left.setOnClickListener(this);
        tv_title_bar_right.setOnClickListener(this);
        tv_sex_male.setOnClickListener(this);
        tv_sex_female.setOnClickListener(this);
//        tv_sex_male.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if(event.getAction() == KeyEvent.ACTION_DOWN){
//                    toast("anxia");
//                    tv_sex_male.setTextColor(Color.parseColor("#9AE96C"));
//                    tv_sex_female.setTextColor(Color.parseColor("#505050"));
//                }else if(event.getAction() == KeyEvent.ACTION_UP){
//                    toast("taiqi");
//                    tv_sex_male.setTextColor(Color.parseColor("#505050"));
//                }
//                return false;
//            }
//        });
//        tv_sex_female.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if (event.getAction() == KeyEvent.ACTION_DOWN) {
//                    toast("anxia");
//                    tv_sex_female.setTextColor(Color.parseColor("#9AE96C"));
//                    tv_sex_male.setTextColor(Color.parseColor("#505050"));
//                } else if (event.getAction() == KeyEvent.ACTION_UP) {
//                    toast("taiqi");
//                    tv_sex_female.setTextColor(Color.parseColor("#505050"));
//                }
//                return false;
//            }
//        });
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()){
            case R.id.iv_title_bar_left:
                toast("返回成功");
//                startActivity(new Intent(ChangeSExActivity.this, PersonalActivity.class));
                ChangeSExActivity.this.finish();
            break;
            case R.id.tv_title_bar_right:
                toast("性别更改完成");
                requestUpdateSex();
            break;
            case R.id.tv_sex_male:
                tv_sex_male.setTextColor(Color.parseColor("#a2e542"));
                tv_sex_female.setTextColor(Color.parseColor("#8e8e8e"));
                sex = 1;
                break;
            case R.id.tv_sex_female:
                tv_sex_female.setTextColor(Color.parseColor("#a2e542"));
                tv_sex_male.setTextColor(Color.parseColor("#8e8e8e"));
                sex = 2;
            break;
        }
    }
    private void requestUpdateSex() {
        UserDataHelper userDataHelper = new UserDataHelper(this);
        userDataHelper.Change_Sex(getNetRequestHelper(this),sex);
    }

    @Override
    protected NetWorkCallback setNetWorkCallback() {
        return new NetWorkCallback() {
            @Override
            public void onStart(String requestTag) {

            }

            @Override
            public void onCancelled(String requestTag) {
                    toast("取消操作");
            }
            @Override
            public void onLoading(long total, long current, boolean isUploading, String requestTag) {
            }
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo, String requestTag) {
                View v = null;
                BaseDataBean<ChangeBeen> baseDataBean = JsonUtil.parseDataObject(responseInfo.result,ChangeBeen.class);
                if(baseDataBean.code == 100){
                    UserSharedPreferencesUtil.savaUserInfo(ChangeSExActivity.this, UserSharedPreferencesUtil.SEX, sex);
                    intent=getIntent();
                    intent.putExtra("sex", sex);
                    setResult(CHANGE_SEX_ACTIVITY, intent);
                    ChangeSExActivity.this.finish();
                }else {
                    toast(baseDataBean.msg);
                }
            }
            @Override
            public void onFailure(HttpException error, String msg, String requestTag) {
                NetWorkStateUtils.errorNetMes(ChangeSExActivity.this);
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
