package anim.activity;

import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.asyey.footballlibrary.network.exception.HttpException;
import com.asyey.footballlibrary.network.http.ResponseInfo;
import com.henanjianye.soon.communityo2o.BaseActivity;
import com.henanjianye.soon.communityo2o.R;
import com.henanjianye.soon.communityo2o.common.UserDataHelper;
import com.henanjianye.soon.communityo2o.common.enties.BaseDataBean;
import com.henanjianye.soon.communityo2o.common.enties.IdeaReturnBean;
import com.henanjianye.soon.communityo2o.common.enties.JsonUtil;
import com.henanjianye.soon.communityo2o.common.util.NetWorkStateUtils;
import com.henanjianye.soon.communityo2o.interf.NetWorkCallback;
import com.umeng.analytics.MobclickAgent;

import java.io.File;

/**
 * Created by sms on 2015/9/9.
 */
public class MyIdeaReturnActivity extends BaseActivity {

    private TextView my_account_text;
    private ImageView system_back;
    private TextView tv_title_bar_right;
    private EditText et_idea;

    @Override
    public int mysetContentView() {
        return R.layout.my_idea_return_fragment;
    }

    @Override
    public void onFragmentInteraction(Message mes) {
        super.onFragmentInteraction(mes);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        setListener();
        processLogic();
    }
    public void initView() {
        my_account_text = (TextView) findViewById(R.id.tv_title_bar_middle);
        my_account_text.setText("意见反馈");
        system_back = (ImageView)findViewById(R.id.iv_title_bar_left);
        tv_title_bar_right = (TextView)findViewById(R.id.tv_title_bar_right);
        tv_title_bar_right.setText("提交");
        tv_title_bar_right.setVisibility(View.VISIBLE);
        et_idea = (EditText)findViewById(R.id.et_idea);
    }
    public void setListener() {
        system_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toast("取消提交操作");
//                startActivity(new Intent(MyIdeaReturnActivity.this, MySystemSettingFragment.class));
                MyIdeaReturnActivity.this.finish();
            }
        });
        tv_title_bar_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String idea;
                //获取提交文本
                idea = et_idea.getText().toString();
                if (!TextUtils.isEmpty(idea)) {
                    updataIdea(idea, null);
                } else {
                    toast("请填写，意见");
                }
            }

            private void updataIdea(String opinion, File file) {
                UserDataHelper userDataHelper = new UserDataHelper(getApplication());
                userDataHelper.submit_Idea(getNetRequestHelper(MyIdeaReturnActivity.this), opinion, file);
            }
        });
    }
    public void processLogic() {

    }
    @Override
    protected NetWorkCallback setNetWorkCallback() {
        return new NetWorkCallback() {
            @Override
            public void onStart(String requestTag) {
            }
            @Override
            public void onCancelled(String requestTag) {
                toast("取消提交操作");
            }
            @Override
            public void onLoading(long total, long current, boolean isUploading, String requestTag) {
            }
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo, String requestTag) {
                BaseDataBean<IdeaReturnBean> baseDataBean = JsonUtil.parseDataObject(responseInfo.result, IdeaReturnBean.class);
                if(baseDataBean.code == 100){
                    toast("提交成功");
                    MyIdeaReturnActivity.this.finish();
                }else {
                    toast(baseDataBean.msg);
                }
            }

            @Override
            public void onFailure(HttpException error, String msg, String requestTag) {
                NetWorkStateUtils.errorNetMes(MyIdeaReturnActivity.this);
            }
        };
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
