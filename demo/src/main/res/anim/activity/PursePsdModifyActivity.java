package anim.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import com.henanjianye.soon.communityo2o.BaseActivity;
import com.henanjianye.soon.communityo2o.R;
import com.henanjianye.soon.communityo2o.common.Constant;
import com.henanjianye.soon.communityo2o.common.util.MyTitleBarHelper;

public class PursePsdModifyActivity extends BaseActivity {
    private MyTitleBarHelper myTitleBarHelper;
    private RelativeLayout tv_psd_modify;
    private RelativeLayout psd_forget;
    public static Activity Act = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.purse_password_manage);
        Act = this;
        initViews();
        initEvents();
    }

    private void initViews() {
        myTitleBarHelper = new MyTitleBarHelper(this, getWindow().getDecorView().findViewById(android.R.id.content));
        myTitleBarHelper.setLeftImgVisible(false);
        myTitleBarHelper.resetState();
        myTitleBarHelper.setMiddleText("支付密码管理");
//        myTitleBarHelper.setRightText("下一步");
        myTitleBarHelper.setRightTxVisible(false);
        myTitleBarHelper.setLeftImag(R.mipmap.btn_back);
        tv_psd_modify = (RelativeLayout) findViewById(R.id.tv_psd_modify);
        psd_forget = (RelativeLayout) findViewById(R.id.psd_forget);
    }

    private void initEvents() {
        myTitleBarHelper.setOnclickListener(this);
        tv_psd_modify.setOnClickListener(this);
        psd_forget.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_title_bar_left:
                finish();
                break;
            case R.id.tv_psd_modify:
                Intent intent = new Intent(PursePsdModifyActivity.this, PursePsdManagerActivity.class);
                intent.putExtra(Constant.Purse.PASSWORDFLAG, Constant.Purse.PASSWORDMODIFY);
                startActivity(intent);
                break;
            case R.id.psd_forget:
                startActivity(new Intent(PursePsdModifyActivity.this, PursePsdResetActivity.class));
                break;
            default:
                break;
        }
    }
}
