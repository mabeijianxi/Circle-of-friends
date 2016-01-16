package anim.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.henanjianye.soon.communityo2o.BaseActivity;
import com.henanjianye.soon.communityo2o.R;
import com.umeng.analytics.MobclickAgent;

/**
 * Created by sms on 2015/10/5.
 */
public class MyAcountActivity extends BaseActivity implements View.OnClickListener {
    private ImageView iv_title_bar_left;
    private TextView text;
    private TextView tv_title_bar_right;
    private RelativeLayout rl_yu_e;
    private RelativeLayout rl_ji_fen;
    private RelativeLayout rl_you_hui_juan;
    @Override
    public int mysetContentView() {
        return R.layout.my_account;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        setListener();
        processLogic();
    }
    public void initView() {
        iv_title_bar_left = (ImageView)findViewById(R.id.iv_title_bar_left);
        text = (TextView)findViewById(R.id.tv_title_bar_middle);
        text.setText("我的账户");
        tv_title_bar_right = (TextView)findViewById(R.id.tv_title_bar_right);
        tv_title_bar_right.setText("");
        rl_yu_e = (RelativeLayout)findViewById(R.id.rl_yu_e);
        rl_ji_fen = (RelativeLayout)findViewById(R.id.rl_ji_fen);
        rl_you_hui_juan = (RelativeLayout)findViewById(R.id.rl_you_hui_juan);
    }
    public void setListener() {
        iv_title_bar_left.setOnClickListener(this);
        rl_yu_e.setOnClickListener(this);
        rl_ji_fen.setOnClickListener(this);
        rl_you_hui_juan.setOnClickListener(this);
    }
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()){
            case R.id.iv_title_bar_left:
//                toast("ooooooo");
//                startActivity(new Intent(MyAcountActivity.this, MineActivity.class));
                MyAcountActivity.this.finish();
                break;
            case R.id.rl_yu_e:
                startActivity(new Intent(MyAcountActivity.this, MyCreditBalanceActivity.class));
                break;
            case R.id.rl_ji_fen:
                toast("积分");
                break;
            case R.id.rl_you_hui_juan:
                toast("优惠卷");
                break;
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
}
