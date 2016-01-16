package anim.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.henanjianye.soon.communityo2o.BaseActivity;
import com.henanjianye.soon.communityo2o.R;
import com.umeng.analytics.MobclickAgent;

/**
 * Created by sms on 2015/9/22.
 */
public class MyCreditBalanceActivity extends BaseActivity implements View.OnClickListener{
    private ImageView iv_title_bar_left;
    private TextView tv_title_bar_right;
    private TextView tv_title_bar_middle;

    @Override
    public int mysetContentView() {
        return R.layout.my_credit_balance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        setListener();
        processLogic();
    }
    private void initView() {
        iv_title_bar_left = (ImageView) findViewById(R.id.iv_title_bar_left);
        tv_title_bar_right = (TextView) findViewById(R.id.tv_title_bar_right);
        tv_title_bar_right.setText("");
        tv_title_bar_middle = (TextView) findViewById(R.id.tv_title_bar_middle);
        tv_title_bar_middle.setText("我的余额");
    }
    private void setListener() {
        iv_title_bar_left.setOnClickListener(this);
    }
    private void processLogic() {
    }
    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()){
            case R.id.iv_title_bar_left:
//                startActivity(new Intent(MyCreditBalanceActivity.this, MyAcountFragment.class));
                MyCreditBalanceActivity.this.finish();
                break;

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
