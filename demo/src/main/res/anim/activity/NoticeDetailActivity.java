package anim.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;

import com.henanjianye.soon.communityo2o.BaseActivity;
import com.henanjianye.soon.communityo2o.R;
import com.henanjianye.soon.communityo2o.common.util.MyTitleBarHelper;
import com.umeng.analytics.MobclickAgent;


public class NoticeDetailActivity extends BaseActivity {
    private MyTitleBarHelper myTitleBarHelper;
    private WebView webview;
//    private ProgressWebView webview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice_detail);
        initViews();
        initEvents();
        init();
    }
    private void initViews() {
        myTitleBarHelper = new MyTitleBarHelper(this, getWindow().getDecorView().findViewById(android.R.id.content));
        myTitleBarHelper.setLeftImgVisible(false);
        myTitleBarHelper.resetState();
        myTitleBarHelper.setMiddleText("通知详情");
        myTitleBarHelper.setRightImgVisible(false);
        myTitleBarHelper.setLeftImag(R.mipmap.btn_back);
        webview = (WebView) findViewById(R.id.webview_holder);
//        webview = (ProgressWebView) findViewById(R.id.webview_holder);
    }

    private void initEvents() {
        myTitleBarHelper.setOnclickListener(this);
    }

    private void init() {
        Intent intent = getIntent();
        String requestURL = intent.getStringExtra(NoticeAndMessageActivity.NOTICE_URL);
        if(!requestURL.equals("")){
            webview.loadUrl(requestURL);
//            webview.setInitialScale(70);
//            WebSettings websettings = webview.getSettings();
//            websettings.setSupportZoom(true);
//            websettings.setBuiltInZoomControls(true);

//            webview.setWebViewClient(new MyWebViewClient());
//            webview.loadUrl(requestURL);
//            MyHander myHander = new MyHander();
//            iv_goods_details.setHandler(myHander);
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.iv_title_bar_left:
                this.finish();
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
