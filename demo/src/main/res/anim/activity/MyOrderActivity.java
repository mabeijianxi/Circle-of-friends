package anim.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.henanjianye.soon.communityo2o.BaseActivity;
import com.henanjianye.soon.communityo2o.R;
import com.henanjianye.soon.communityo2o.adapter.MyOderAdapter;
import com.umeng.analytics.MobclickAgent;

/**
 * Created by sms on 2015/10/5.
 */
public class MyOrderActivity extends BaseActivity implements View.OnClickListener {
    private TextView text;
    private ImageView system_back;
    private ListView lv_diandan;
    private MyOderAdapter mAdapter;
    private TextView tv_title_bar_right;

    @Override
    public int mysetContentView() {
        return R.layout.my_dingdan;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        setListener();
        processLogic();
    }

    public void initView() {
        text  =(TextView)findViewById(R.id.tv_title_bar_middle);
        text.setText("我的订单");
        tv_title_bar_right = (TextView)findViewById(R.id.tv_title_bar_right);
        tv_title_bar_right.setText("");
        system_back =(ImageView)findViewById(R.id.iv_title_bar_left);
        system_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                startActivity(new Intent(MyOrderActivity.this, MineActivity.class));
                MyOrderActivity.this.finish();
            }
        });
//        找到订单ListView  实例化Adapter
        lv_diandan = (ListView)findViewById(R.id.lv_diandan);
        mAdapter = new MyOderAdapter(MyOrderActivity.this);
//        将适配器添加到ListView中
        lv_diandan.setAdapter(mAdapter);
    }
    public void setListener() {

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
