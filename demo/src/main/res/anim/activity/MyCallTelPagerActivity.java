package anim.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.henanjianye.soon.communityo2o.BaseActivity;
import com.henanjianye.soon.communityo2o.R;
import com.umeng.analytics.MobclickAgent;

/**
 * Created by sms on 2015/10/10.
 */
public class MyCallTelPagerActivity extends BaseActivity implements View.OnClickListener{
    private TextView tv_tel_num;
    private Intent intent;
    private String keeperNum;

    @Override
    public int mysetContentView() {
        return R.layout.my_tel_yellow_pager_dialog;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }

    private void initView() {
        intent = getIntent();
        keeperNum =  intent.getStringExtra("keeperNum");
        tv_tel_num = (TextView) findViewById (R.id.tv_tel_num);
        tv_tel_num.setText(keeperNum);
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
