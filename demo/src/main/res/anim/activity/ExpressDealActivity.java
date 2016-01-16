package anim.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.henanjianye.soon.communityo2o.BaseActivity;
import com.henanjianye.soon.communityo2o.R;
import com.henanjianye.soon.communityo2o.common.util.MyTitleBarHelper;


public class ExpressDealActivity extends BaseActivity {
    private MyTitleBarHelper myTitleBarHelper;
    private TextView type_in_express;
    private TextView receive_express;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_express_deal);
        initViews();
        initEvents();
        init();
    }

    private void initViews() {
        myTitleBarHelper = new MyTitleBarHelper(this, getWindow().getDecorView().findViewById(android.R.id.content));
        myTitleBarHelper.setLeftImgVisible(false);
        myTitleBarHelper.resetState();
        myTitleBarHelper.setMiddleText("快递处理");
        myTitleBarHelper.setRightImgVisible(false);
        myTitleBarHelper.setLeftImag(R.mipmap.btn_back);
        myTitleBarHelper.setRightText("记录");
        type_in_express = (TextView) findViewById(R.id.type_in_express);
        receive_express = (TextView) findViewById(R.id.receive_express);
    }

    private void initEvents() {
        myTitleBarHelper.setOnclickListener(this);
        type_in_express.setOnClickListener(this);
        receive_express.setOnClickListener(this);
    }

    private void init() {
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.iv_title_bar_left:
                this.finish();
                break;
            case R.id.tv_title_bar_right:
                startActivity(new Intent(this, ExpressQueryRecordActivity.class));
                break;
            case R.id.type_in_express:
                startActivity(new Intent(this,EntryExpress.class));
                break;
            case R.id.receive_express:
                startActivity(new Intent(this, GetExpress.class));
                break;
            default:
                break;
        }
    }
}
