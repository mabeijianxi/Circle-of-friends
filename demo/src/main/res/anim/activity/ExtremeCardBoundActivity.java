package anim.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.View;

import com.henanjianye.soon.communityo2o.BaseActivity;
import com.henanjianye.soon.communityo2o.R;
import com.henanjianye.soon.communityo2o.common.Constant;
import com.henanjianye.soon.communityo2o.common.util.CommonUtils;
import com.henanjianye.soon.communityo2o.common.util.MyTitleBarHelper;
import com.henanjianye.soon.communityo2o.fragment.BaseFragment;
import com.henanjianye.soon.communityo2o.fragment.me.BoundCardFragment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExtremeCardBoundActivity extends BaseActivity {
    private MyTitleBarHelper myTitleBarHelper;
    private HashMap<Integer, Fragment> fragments = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        fragments.put(R.id.card_bound, BoundCardFragment.newInstance("", ""));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_bound);
        initViews();
        initEvents();
        init();
    }

    @Override
    public Map<Integer, Fragment> myFragments() {
        return fragments;
    }

    private void init() {
    }

    private void initViews() {
        myTitleBarHelper = new MyTitleBarHelper(this, getWindow().getDecorView().findViewById(android.R.id.content));
        myTitleBarHelper.setLeftImgVisible(false);
        myTitleBarHelper.resetState();
        myTitleBarHelper.setMiddleText("至尊卡绑定");
        myTitleBarHelper.setRightText("完成关联");
//        myTitleBarHelper.setRightTxVisible(false);
        myTitleBarHelper.setLeftImag(R.mipmap.btn_back);
    }

    private void initEvents() {
        myTitleBarHelper.setOnclickListener(this);
    }

    @Override
    public void onFragmentInteraction(Message mes) {
        switch (mes.arg1) {
            case Constant.Purse.CARDBOUND:
                myTitleBarHelper.getRightText().setClickable(true);
                Intent intent = new Intent();
//                    intent.putExtra(ServiceRepairActivity.REPAIR_BACK_INFO, rBean);
                setResult(RESULT_OK, intent);
                ExtremeCardBoundActivity.this.finish();
                break;
            case 0:
                myTitleBarHelper.getRightText().setClickable(false);
                break;
            case 1:
                myTitleBarHelper.getRightText().setClickable(true);
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_title_bar_left:
                this.finish();
                break;
            case R.id.tv_title_bar_right:
                if (CommonUtils.isNetworkConnected(ExtremeCardBoundActivity.this)) {
                    getVisibleFragment().titleBarRightTxtEvent();
                } else {
                    toast("网络不可用");
                }
                break;
            default:
                break;
        }
    }

    public BaseFragment getVisibleFragment() {
        FragmentManager fragmentManager = ExtremeCardBoundActivity.this.getSupportFragmentManager();
        List<Fragment> fragments = fragmentManager.getFragments();
        for (Fragment fragment : fragments) {
            if (fragment != null && fragment.isVisible())
                return (BaseFragment) fragment;
        }
        return null;
    }
}
