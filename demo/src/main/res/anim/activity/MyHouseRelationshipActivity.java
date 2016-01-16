package anim.activity;

import android.os.Bundle;
import android.widget.ListView;

import com.henanjianye.soon.communityo2o.BaseActivity;
import com.henanjianye.soon.communityo2o.R;
import com.henanjianye.soon.communityo2o.adapter.MyHouseRelationshipAdapter;
import com.umeng.analytics.MobclickAgent;

/**
 * Created by sms on 2015/10/8.
 */
public class MyHouseRelationshipActivity extends BaseActivity {
    private ListView my_subList;
    private MyHouseRelationshipAdapter adapter;

    @Override
    public int mysetContentView() {
        return R.layout.my_wuyeinformation_item;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }

    private void initView() {
        my_subList = (ListView) findViewById(R.id.my_subList);
        adapter = new MyHouseRelationshipAdapter(MyHouseRelationshipActivity.this);
        my_subList.setAdapter(adapter);
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
