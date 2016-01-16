package anim.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.asyey.footballlibrary.network.exception.HttpException;
import com.asyey.footballlibrary.network.http.ResponseInfo;
import com.henanjianye.soon.communityo2o.BaseActivity;
import com.henanjianye.soon.communityo2o.LoginActivity;
import com.henanjianye.soon.communityo2o.MainActivity;
import com.henanjianye.soon.communityo2o.R;
import com.henanjianye.soon.communityo2o.adapter.NewleadAdapter;
import com.henanjianye.soon.communityo2o.common.Constant;
import com.henanjianye.soon.communityo2o.common.UserDataHelper;
import com.henanjianye.soon.communityo2o.common.enties.AppUpdateBean;
import com.henanjianye.soon.communityo2o.common.enties.AppUrlBean;
import com.henanjianye.soon.communityo2o.common.enties.BaseDataBean;
import com.henanjianye.soon.communityo2o.common.enties.JsonUtil;
import com.henanjianye.soon.communityo2o.common.util.CommonUtils;
import com.henanjianye.soon.communityo2o.common.util.NetWorkStateUtils;
import com.henanjianye.soon.communityo2o.common.util.SharedPreferencesUtil;
import com.henanjianye.soon.communityo2o.common.util.UserSharedPreferencesUtil;
import com.henanjianye.soon.communityo2o.interf.NetWorkCallback;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

/**
 * @author li
 * @项目名称 sport
 * @创建时间 2015-5-23上午11:14:56
 * @邮箱 jayronlou@163.com
 * @作用描述 引导页加广告业
 * @备注
 */
public class GuideAct extends BaseActivity implements OnPageChangeListener {
    private static final String SECOND_AD = "SECOND_AD";

    private ViewPager vp_pager;
    private RelativeLayout rl_guide;
    private LinearLayout ll_dots;
    private ImageView iv_news;
    private UserDataHelper userDataHelper;
    private int[] pics = new int[]{R.mipmap.pagerone, R.mipmap.pagertwo,
            R.mipmap.pagerthree, R.mipmap.pagerfour};
    private List<View> views;
    //当前滑动的位置
    private int lastValue = -1;
    private ImageView click_into;

    @Override
    public int mysetContentView() {
        return R.layout.guideact;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        processLogic();
        getNewVersionInfo();
    }

    private void getNewVersionInfo() {
        if (CommonUtils.isNetworkConnected(this)) {
            requestAppUrls();
        }
    }

    private void requestAppUrls() {
        if (userDataHelper == null) {
            userDataHelper = new UserDataHelper(this);
        }
        userDataHelper.getAppUrls(getNetRequestHelper(this).isShowProgressDialog(false));
    }

//    /**
//     * 请求购物车信息
//     */
//    private void requestShoppData(int orgId) {
//        if (userDataHelper == null) {
//            userDataHelper = new UserDataHelper(this);
//        }
//        userDataHelper.getShopNumInfor(getNetRequestHelper(this).isShowProgressDialog(false), orgId);
//    }

//    private void pasrseNoticeAndMess(String result) {
//        try {
//            BaseDataBean<NumBean> json = JsonUtil.parseDataObject(result, NumBean.class);
//            if (json.code == 100) {
//                NumBean nBean = json.data;
//                if (nBean != null) {
//                    SharedPreferencesUtil.saveIntData(GuideAct.this,
//                            Constant.ShouYeUrl.NOTICE_MESS_NUM, nBean.totalCount);
//                }
//            }
//        } catch (Exception e) {
//            // TODO: handle exception
//        }
//    }

//    private void pasrseShopNum(String result) {
//        try {
//            BaseDataBean<NumBean> json = JsonUtil.parseDataObject(result, NumBean.class);
//            if (json.code == 100) {
//                NumBean numBean = json.data;
//                if (numBean != null) {
//                    //购物车数量大于0时显示
//                    SharedPreferencesUtil.saveIntData(GuideAct.this,
//                            Constant.ShouYeUrl.SHAOPPING_NUM, numBean.goodscartCount);
//                }
//            }
//        } catch (Exception e) {
//            // TODO: handle exception
//        }
//    }

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
//               if (requestTag.equals(Constant.ShouYeUrl.SHAOPPING_NUM)) {
//                    //解析购物车数量
//                    pasrseShopNum(responseInfo.result);
//                }
                if (Constant.AppUrl.APP_COMMON_UPDATE.equals(requestTag)) {
                    BaseDataBean<AppUpdateBean> json = JsonUtil.parseDataObject(responseInfo.result, AppUpdateBean.class);
                    if (json.code == 100) {
                        // TODO  做一些操作
                        int loginState = UserSharedPreferencesUtil.getUserLoginState(GuideAct.this);
                        if (loginState < 1) {
                            Intent intent = new Intent(GuideAct.this, LoginActivity.class);
                            intent.setAction(LoginActivity.GUIDEACT);
                            intent.putExtra(LoginActivity.LOGIN_STATE_KEY, loginState);
                            startActivity(intent);
                        } else {
                            Intent intent = new Intent(GuideAct.this, MainActivity.class);
                            startActivity(intent);
                        }
                        SharedPreferencesUtil.saveStringData(GuideAct.this, SharedPreferencesUtil.APP_SERVER_VERSION_JSON, responseInfo.result);
                    } else {
                        toast(json.msg);
                    }
                    finish();
                }
                if (requestTag.equals(Constant.AppUrl.APP_COMMON_URL)) {
                    BaseDataBean<AppUrlBean> json = JsonUtil.parseDataObject(responseInfo.result, AppUrlBean.class);
                    AppUrlBean appUrlBean = json.data;
                    if (appUrlBean != null) {
                        if (!TextUtils.isEmpty(appUrlBean.pathExpress)) {
                            SharedPreferencesUtil.saveStringData(GuideAct.this, SharedPreferencesUtil.APP_URL_PATHEXPRESS, appUrlBean.pathExpress);
                        }
                        if (!TextUtils.isEmpty(appUrlBean.pathOpendoor)) {
                            SharedPreferencesUtil.saveStringData(GuideAct.this, SharedPreferencesUtil.APP_URL_PATHOPENDOOR, appUrlBean.pathOpendoor);
                        }
                        if (!TextUtils.isEmpty(appUrlBean.pathPrivacy)) {
                            SharedPreferencesUtil.saveStringData(GuideAct.this, SharedPreferencesUtil.APP_URL_PATHPRIVACY, appUrlBean.pathPrivacy);
                        }
                        if (!TextUtils.isEmpty(appUrlBean.returnDescription)) {
                            SharedPreferencesUtil.saveStringData(GuideAct.this, SharedPreferencesUtil.APP_URL_RETURNDESCRIPTION, appUrlBean.returnDescription);
                        }
                    }
                }
            }

            @Override
            public void onFailure(HttpException error, String msg, String requestTag) {
                NetWorkStateUtils.errorNetMes(GuideAct.this);
                if (Constant.AppUrl.APP_COMMON_UPDATE.equals(requestTag)) {
//                    finish();
                    toast("服务器错误");
                }
            }
        };
    }


    public void initView() {
        iv_news = (ImageView) findViewById(R.id.iv_welcome_pager);
        vp_pager = (ViewPager) findViewById(R.id.vp_pager);
        rl_guide = (RelativeLayout) findViewById(R.id.rl_guide);
        ll_dots = (LinearLayout) findViewById(R.id.ll_dots);
        views = new ArrayList<View>();
        click_into = (ImageView) findViewById(R.id.click_into);
//        helpfinishDrawable = getResources().getDrawable(
//                R.drawable.help_coming_border_bg);
    }

    public void processLogic() {
//        MobclickAgent.setDebugMode(true);
        //友盟统计使用针对有fragment的应用
        MobclickAgent.openActivityDurationTrack(false);
        //目前是没有引导页，,添加引导页只需要把字符串去掉
        String stringData = SharedPreferencesUtil.getStringData(this, SECOND_AD,
                "");
        if (TextUtils.isEmpty(stringData)) {//第一次
            iv_news.setVisibility(View.GONE);
            rl_guide.setVisibility(View.VISIBLE);
            vp_pager.setAdapter(new NewleadAdapter(GuideAct.this));
//        adapter = new SlideImageAdapter();
//        vp_pager.setAdapter(adapter);
            vp_pager.setOffscreenPageLimit(2);
            vp_pager.setOnPageChangeListener(this);
            click_into.setOnClickListener(this);
            onPageSelected(0);
        } else {
            dismissGuide();
        }
    }


    @Override
    public void onPageScrollStateChanged(int arg0) {
        if (arg0 == 0) {
            if (lastValue == pics.length - 1) {
                // Toast.makeText(this, "已经是最后一张了", Toast.LENGTH_SHORT).show();
                click_into.setVisibility(View.VISIBLE);
            } else {
                click_into.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
        // TODO Auto-generated method stub
        if (arg0 == 3) {
            click_into.setVisibility(View.VISIBLE);
        }
        lastValue = arg0;
    }

    @Override
    public void onPageSelected(int arg0) {
        if (arg0 == vp_pager.getAdapter().getCount() - 1) {
            ll_dots.setVisibility(View.GONE);
        } else {
            ll_dots.setVisibility(View.VISIBLE);
            ll_dots.removeAllViews();
            for (int i = 0; i < vp_pager.getAdapter().getCount(); i++) {
                View dotView = LayoutInflater.from(this).inflate(R.layout.one_dot,
                        null);
                ImageView iv_dot = (ImageView) dotView.findViewById(R.id.iv_dot);
                if (i == arg0) {
                    iv_dot.setBackgroundResource(R.mipmap.guid_dot_selecte);
                } else {
                    iv_dot.setBackgroundResource(R.mipmap.guid_dot_normal);
                }
                ll_dots.addView(dotView, i);
            }
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.click_into:
                click_into.setVisibility(View.GONE);
                dismissGuide();
                break;
        }
    }

    /**
     * 消失引导页
     */
    public void dismissGuide() {
        SharedPreferencesUtil.saveStringData(this, SECOND_AD,
                "SECOND_AD");
        rl_guide.setVisibility(View.GONE);
        iv_news.setVisibility(View.VISIBLE);
        if (CommonUtils.isNetworkConnected(this)) {
            if (userDataHelper == null) {
                userDataHelper = new UserDataHelper(this);
            }
            userDataHelper.sendIsUpdate(getNetRequestHelper(this).isShowProgressDialog(false));
        } else {
            int loginState = UserSharedPreferencesUtil.getUserLoginState(GuideAct.this);
            if (loginState < 1) {
                Intent intent = new Intent(GuideAct.this, LoginActivity.class);
                intent.setAction(LoginActivity.GUIDEACT);
                intent.putExtra(LoginActivity.LOGIN_STATE_KEY, loginState);
                startActivity(intent);
            } else {
                Intent intent = new Intent(GuideAct.this, MainActivity.class);
                startActivity(intent);
            }
            finish();
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
