package anim.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.henanjianye.soon.communityo2o.BaseActivity;
import com.henanjianye.soon.communityo2o.R;
import com.henanjianye.soon.communityo2o.common.enties.AppMallOrderResponsBean;
import com.henanjianye.soon.communityo2o.common.util.CommonUtils;
import com.umeng.analytics.MobclickAgent;

public class ZhiFuJieGuoActivity extends BaseActivity {
    private ImageView iv_zhifu;
    private TextView tv_success, tv_price, tv_number, tv_name;
    private Button bt_button;
    private String failReason;


    @Override
    public int mysetContentView() {
        return R.layout.pay_allway_result;
    }

    private AppMallOrderResponsBean mall;
    private int ifsuccess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        ifsuccess = intent.getIntExtra(PayAllWayActivity.PAYRESULT_KEY_CODE, 0);
        failReason = intent.getStringExtra("failReason");
        Object object = intent.getSerializableExtra(PayAllWayActivity.PAYRESULT_KEY);
        if (object != null && object instanceof AppMallOrderResponsBean) {
            mall = (AppMallOrderResponsBean) object;
        } else {
            toast("返回结果错误");
            return;
        }
        initView();
    }

    public void initView() {
        iv_zhifu = (ImageView) findViewById(R.id.iv_zhifu);
        tv_success = (TextView) findViewById(R.id.tv_success);
        tv_number = (TextView) findViewById(R.id.tv_number);
        tv_name = (TextView) findViewById(R.id.tv_name);
        tv_price = (TextView) findViewById(R.id.tv_price);
        bt_button = (Button) findViewById(R.id.bt_button);
        bt_button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO   跳至订单页面
//                toast("功能待添加");
//                switch (ifsuccess) {
//                    case 1:
//                        //TODO 支付成功，查看订单
                Intent intent = new Intent(ZhiFuJieGuoActivity.this, CommodityOrderListActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                ZhiFuJieGuoActivity.this.finish();
//                        break;
//                    default:
//                        ZhiFuJieGuoActivity.this.finish();
                //支付失败 重新支付
//                }
//				startActivity(new Intent(ZhiFuJieGuoActivity.this,ShangChengDingDan.class));
            }
        });
        ShowIf();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    public void ShowIf() {
        tv_price.setText("¥" + CommonUtils.floatToTowDecima(mall.totalPrice));
        tv_number.setText("订单号：" + mall.orderNos);
        tv_name.setText("商品名称：" + mall.goodsNames);
        if (ifsuccess == 1) {//支付成功
            tv_success.setText("支付成功");
            iv_zhifu.setImageResource(R.mipmap.pay_lvcha);
            bt_button.setBackgroundResource(R.mipmap.chakandingdan);
        } else if (ifsuccess == 0) {
            String temp_error_infor="";
            if ("01".equals(failReason)) {
                temp_error_infor=" 非法数据传入";
            } else if ("02".equals(failReason)) {
                temp_error_infor=" 无效秘钥，非法传入";
            } else if ("03".equals(failReason)) {
                temp_error_infor=" 用户编号缺失";
            } else if ("04".equals(failReason)) {
                temp_error_infor=" 消费账户金额格式错误";
            } else if ("05".equals(failReason)) {
                temp_error_infor="  消费建业币金额格式传入有误";
            } else if ("06".equals(failReason)) {
                temp_error_infor="  消费数据传入有误（账户消费和建业币消费都为0时）";
            } else if ("07".equals(failReason)) {
                temp_error_infor=" 订单编号缺失";
            }else if ("08".equals(failReason)) {
                temp_error_infor=" 非法会员编号";
            } else if ("09".equals(failReason)) {
                temp_error_infor=" 账户金额不足";
            } else if ("10".equals(failReason)) {
                temp_error_infor=" 建业币金额不足";
            } else if ("11".equals(failReason)) {
                temp_error_infor=" 消费扣款失败";
            } else if ("12".equals(failReason)) {
                temp_error_infor=" 支付密码非法传入";
            } else if ("13".equals(failReason)) {
                temp_error_infor=" 支付密码错误";
            } else {

            }
            tv_success.setText("支付失败");

            iv_zhifu.setImageResource(R.mipmap.hongcha);
            bt_button.setBackgroundResource(R.mipmap.chongxinzhifu);
        } else {
            tv_success.setText("取消");
            iv_zhifu.setImageResource(R.mipmap.hongcha);
            bt_button.setBackgroundResource(R.mipmap.chongxinzhifu);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
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
