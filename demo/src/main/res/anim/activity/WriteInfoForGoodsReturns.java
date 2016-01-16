package anim.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.asyey.footballlibrary.network.exception.HttpException;
import com.asyey.footballlibrary.network.http.ResponseInfo;
import com.henanjianye.soon.communityo2o.BaseActivity;
import com.henanjianye.soon.communityo2o.R;
import com.henanjianye.soon.communityo2o.common.Constant;
import com.henanjianye.soon.communityo2o.common.enties.BaseDataBean;
import com.henanjianye.soon.communityo2o.common.enties.JsonUtil;
import com.henanjianye.soon.communityo2o.common.util.NetWorkStateUtils;
import com.henanjianye.soon.communityo2o.common.view.CustomProgressDialog;
import com.henanjianye.soon.communityo2o.eventFunction.bean.EventIsSignupBean;
import com.henanjianye.soon.communityo2o.interf.NetWorkCallback;
import com.umeng.analytics.MobclickAgent;

import java.util.HashMap;

/**
 * 填写退货物流信息
 */
public class WriteInfoForGoodsReturns extends BaseActivity {
    TextView tv_title_bar_left;//取消
    TextView tv_title_bar_right;//确定
    EditText et_wuliu;//物流
    EditText et_danhao;//单号
    private String wuliu;
    private String danhao;
    private HashMap<String, Object> params;
    long orderId;
    CustomProgressDialog createDialog;

    @Override
    public int mysetContentView() {
        return R.layout.write_info_forgoodsreturn;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();

        orderId = intent.getLongExtra("orderId", -1);
        if(orderId==-1){
            setResult(RESULT_OK,getIntent());
            finish();
            return;
        }
        intView();
        intSetListener();
    }

    private void intSetListener() {
        //确定
        tv_title_bar_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                wuliu = et_wuliu.getText().toString();
                danhao = et_danhao.getText().toString();
                if (TextUtils.isEmpty(danhao) || TextUtils.isEmpty(wuliu)) {

                    toast("退货物流信息不能为空");
                } else {
                    postWuLiuInfo();
                }


            }
        });
        //取消
        tv_title_bar_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WriteInfoForGoodsReturns.this.finish();

            }
        });

    }

    private void postWuLiuInfo() {
        if (!TextUtils.isEmpty(Constant.OrderUrl.WRITEINFOFORGOODSRETURN)) {

            params = new HashMap<String, Object>();
            if (!TextUtils.isEmpty(et_wuliu.getText().toString())
                    && (!TextUtils.isEmpty(et_danhao.getText().toString()))) {
                // 此处单号得获取
                params.put("orderId", orderId);//orderId

                params.put("shipCode", et_danhao.getText().toString());
                params.put("ecName", et_wuliu.getText().toString());

            } else {
                return;
            }
            if (params.size() > 0) {

                createDialog = CustomProgressDialog.createDialog(this);
                createDialog.show();
                getNetRequestHelper(WriteInfoForGoodsReturns.this).postRequest(Constant.OrderUrl.WRITEINFOFORGOODSRETURN, params, Constant.OrderUrl.WRITEINFOFORGOODSRETURN);


            }
        }

    }


    private void intView() {

        tv_title_bar_left = (TextView) findViewById(R.id.tv_title_bar_left);
        tv_title_bar_right = (TextView) findViewById(R.id.tv_title_bar_right);
        TextView tv_title_bar_middle = (TextView) findViewById(R.id.tv_title_bar_middle);
        tv_title_bar_middle.setText("物流信息");
        ImageView iv_title_bar_left = (ImageView) findViewById(R.id.iv_title_bar_left);
        iv_title_bar_left.setVisibility(View.GONE);
        tv_title_bar_left.setVisibility(View.VISIBLE);
        tv_title_bar_left.setText("取消");
        tv_title_bar_right.setVisibility(View.VISIBLE);
        tv_title_bar_right.setText("确定");
        et_wuliu = (EditText) findViewById(R.id.et_wuliu);
        et_danhao = (EditText) findViewById(R.id.et_danhao);


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
                if (createDialog != null && createDialog.isShowing()) {
                    createDialog.dismiss();
                }
                if (requestTag == Constant.OrderUrl.WRITEINFOFORGOODSRETURN) {
                    pareInfo(responseInfo.result);
                }

            }

            @Override
            public void onFailure(HttpException error, String msg, String requestTag) {
                if (createDialog != null && createDialog.isShowing()) {
                    createDialog.dismiss();
                }
                NetWorkStateUtils.errorNetMes(WriteInfoForGoodsReturns.this);
            }
        };
    }

    //解析数据
    private void pareInfo(String result) {
        BaseDataBean<EventIsSignupBean> json = JsonUtil.parseDataObject(result,
                EventIsSignupBean.class);

        String msg = json.msg;
        toast(msg);
        if (json.code == 100) {
            //跳到订单列表界面
            //TODO
            setResult(RESULT_OK, getIntent());
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
