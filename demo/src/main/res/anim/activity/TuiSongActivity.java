package anim.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.asyey.footballlibrary.network.exception.HttpException;
import com.asyey.footballlibrary.network.http.ResponseInfo;
import com.henanjianye.soon.communityo2o.BaseActivity;
import com.henanjianye.soon.communityo2o.R;
import com.henanjianye.soon.communityo2o.common.Constant;
import com.henanjianye.soon.communityo2o.common.UserDataHelper;
import com.henanjianye.soon.communityo2o.common.enties.BaseDataBean;
import com.henanjianye.soon.communityo2o.common.enties.JsonUtil;
import com.henanjianye.soon.communityo2o.common.enties.PushBean;
import com.henanjianye.soon.communityo2o.common.util.NetWorkStateUtils;
import com.henanjianye.soon.communityo2o.common.view.CustomProgressDialog;
import com.henanjianye.soon.communityo2o.interf.NetWorkCallback;
import com.kyleduo.switchbutton.SwitchButton;
import com.umeng.analytics.MobclickAgent;

import java.util.HashMap;

/**
 * Created by sms on 2015/9/22.
 */
public class TuiSongActivity extends BaseActivity implements View.OnClickListener{
    private ImageView iv_title_bar_left;
    private TextView tv_title_bar_middle;
    private TextView tv_title_bar_right;
    private String push;
    private String push_notify;
    private String push_mall;
    private String push_activity;
    private String push_voice;
    private String savedatamode;
//    String button;
//    String notify;
//    String active;
//    String mall;
//    String voice;
    private SwitchButton switch_button;
    private SwitchButton switch_message;
    private SwitchButton switch_activity;
    private SwitchButton switch_producter;
    private SwitchButton switch_voide;
    public UserDataHelper userDataHelper;
    LinearLayout ll_open;

    @Override
    public int mysetContentView() {
        postquery();
        return R.layout.my_tui_song;

    }

    private void postquery() {
        //推送设置的状态请求
        HashMap<String, Object> params = new HashMap<String, Object>();
        if(!TextUtils.isEmpty(Constant.PushUrl.GETPUSH)){

            getNetRequestHelper(TuiSongActivity.this).postRequest(Constant.PushUrl.GETPUSH, null, Constant.PushUrl.GETPUSH);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        setListener();
        processLogic();
    }
    private void initView() {

        ll_open = (LinearLayout) findViewById(R.id.ll_open);
        iv_title_bar_left = (ImageView) findViewById(R.id.iv_title_bar_left);
        tv_title_bar_middle = (TextView) findViewById(R.id.tv_title_bar_middle);
        tv_title_bar_middle.setText("推送设置");
        tv_title_bar_right = (TextView) findViewById(R.id.tv_title_bar_right);
        tv_title_bar_right.setText("");
        switch_button = (SwitchButton) findViewById(R.id.switch_button);
        switch_message = (SwitchButton) findViewById(R.id.switch_message);
        switch_activity = (SwitchButton) findViewById(R.id.switch_activity);
        switch_producter = (SwitchButton) findViewById(R.id.switch_producter);
        switch_voide = (SwitchButton) findViewById(R.id.switch_voide);
    }
    private void setListener() {
        iv_title_bar_left.setOnClickListener(this);
        switch_button.setOnClickListener(this);

        switch_button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    ll_open.setVisibility(View.VISIBLE);
                } else {
                    ll_open.setVisibility(View.GONE);
                }
            }
        });
    }
    private void processLogic() {
        userDataHelper =new UserDataHelper(this);
        userDataHelper.getPush(getNetRequestHelper(this), push, push_notify, push_mall, push_activity, push_voice, savedatamode);
    }
    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()){
            case R.id.iv_title_bar_left:
                pushManager();
                postSave();

                TuiSongActivity.this.finish();
                break;
            case R.id.switch_button:
//                if(!switch_button.isChecked()){
//                    switch_activity.setChecked(false);
//                    switch_message.setChecked(false);
//                    switch_producter.setChecked(false);
//                    switch_voide.setChecked(false);
//                }
                break;
        }
    }


//系统返回键
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            pushManager();
            postSave();

            TuiSongActivity.this.finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void postSave() {

        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("push",push);
        params.put("push_notify",push_notify);
        params.put("push_mall",push_mall);
        params.put("push_active",push_activity);
        params.put("push_voice", push_voice);
        if(!TextUtils.isEmpty(Constant.PushUrl.SAVEPUSH)){
            getNetRequestHelper(TuiSongActivity.this).isShowProgressDialog(false).postRequest(Constant.PushUrl.SAVEPUSH, params,Constant.PushUrl.SAVEPUSH);

        }


    }


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
                BaseDataBean<PushBean> beanBaseDataBean = JsonUtil.parseDataObject(responseInfo.result,PushBean.class);
                        if(requestTag == Constant.PushUrl.GETPUSH){
                              if(beanBaseDataBean.code == 100){
                                  if(beanBaseDataBean.data!=null){
                                      if(beanBaseDataBean.data.push.equals("1")){
                                          switch_button.setChecked(true);
                                          ll_open.setVisibility(View.VISIBLE);
                                         String push_active=beanBaseDataBean.data.push_active;
                                         String push_base=beanBaseDataBean.data.push_base;
                                         String push_mall=beanBaseDataBean.data.push_mall;
                                         String push_notify=beanBaseDataBean.data.push_notify;
                                         String push_voice=beanBaseDataBean.data.push_voice;
                                        if(push_active.equals("1")){
                                            switch_activity.setChecked(true);
                                        }else{
                                            switch_activity.setChecked(false);
                                        }if(push_notify.equals("1")){
                                              switch_message.setChecked(true);
                                          }else{
                                              switch_message.setChecked(false);
                                          }
                                          if(push_mall.equals("1")){
                                              switch_producter.setChecked(true);
                                          }else{
                                              switch_producter.setChecked(false);

                                          }
                                          if(push_voice.equals("1")){
                                              switch_voide.setChecked(true);
                                          }else{
                                              switch_voide.setChecked(false);
                                          }


                                      }else {
                                          ll_open.setVisibility(View.GONE);
                                      }
                                  }

                              }
                        }
                if(requestTag == Constant.PushUrl.SAVEPUSH) {
                  /*  if (beanBaseDataBean.code == 100) {
                        toast("请求成功");
                    } else {
                        toast(beanBaseDataBean.msg);
                    }*/
                }
            }
            @Override
            public void onFailure(HttpException error, String msg, String requestTag) {
                  //  toast(msg);
                NetWorkStateUtils.errorNetMes(TuiSongActivity.this);
            }
        };
    }

    private void pushManager() {
        if(switch_button.isChecked()){
            push = "1";

        }else {
            push = "0";

        }
        if(switch_message.isChecked()){
            push_notify = "1";
        }else {
            push_notify = "0";
        }
        if(switch_activity.isChecked()){
            push_activity = "1";
        }else {
            push_activity = "0";
        }
        if(switch_producter.isChecked()){
            push_mall = "1";
        }else {
            push_mall = "0";
        }
        if(switch_voide.isChecked()){
            push_voice = "1";
        }else {
            push_voice = "0";
        }

    }
    private CustomProgressDialog dialogContact;

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
