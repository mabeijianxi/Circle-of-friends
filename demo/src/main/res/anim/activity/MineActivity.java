package anim.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;

import com.henanjianye.soon.communityo2o.BaseActivity;
import com.henanjianye.soon.communityo2o.R;
import com.henanjianye.soon.communityo2o.fragment.Fragment03;
import com.henanjianye.soon.communityo2o.fragment.me.MyAcountFragment;
import com.henanjianye.soon.communityo2o.fragment.me.MyActivityFragment;
import com.henanjianye.soon.communityo2o.fragment.me.MyOrderFragment;
import com.henanjianye.soon.communityo2o.fragment.me.MySystemSettingFragment;
import com.henanjianye.soon.communityo2o.fragment.me.MyTenementChangeBindFragment;
import com.henanjianye.soon.communityo2o.fragment.me.MyrepairsFragment;
import com.umeng.analytics.MobclickAgent;

/**
 * Created by sms on 2015/9/6.
 */
public class MineActivity extends BaseActivity {
    private long exitTime;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
////    按两下返回键 退出程序
//    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN)
//        {
//            if((System.currentTimeMillis()-exitTime) > 2000)  //System.currentTimeMillis()无论何时调用，肯定大于2000
//            {
//                Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
//                exitTime = System.currentTimeMillis();
//            }
//            else
//            {
//                finishAffinity();
//                System.exit(0);
//            }
//            return true;
//        }
//        return super.onKeyDown(keyCode, event);
//    }
    //fragment 和activity 间进行交互
    @Override
    public void onFragmentInteraction(Message mes) {
        switch (mes.arg1){
            case 0:
                replaceFragmentWithStack(R.id.base_content_fragment,MySystemSettingFragment.newInstance(null,null),true);
            break;
            case 1:
                toast("0000000");
                startActivity(new Intent(MineActivity.this,PersonalActivity.class));
                break;
            case 2:
                replaceFragmentWithStack(R.id.base_content_fragment,MyAcountFragment.newInstance(null,null),true);
                toast("0000000");
            break;
            case 3:
                replaceFragmentWithStack(R.id.base_content_fragment,MyOrderFragment.newInstance(null, null),true);
            break;
            case 4:
                replaceFragmentWithStack(R.id.base_content_fragment,MyrepairsFragment.newInstance(null,null),true);
            break;
            case 5:
                replaceFragmentWithStack(R.id.base_content_fragment,MyActivityFragment.newInstance(null, null),true);
            break;
            case 6:
                replaceFragmentWithStack(R.id.base_content_fragment, MyTenementChangeBindFragment.newInstance(null, null),true);
            break;
        }
    }

    @Override
    public Fragment myFragment() {
        return new Fragment03();
    }
    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);       //统计时长
    }
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }
}
