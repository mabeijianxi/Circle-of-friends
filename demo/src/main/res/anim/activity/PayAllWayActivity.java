package anim.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.asyey.footballlibrary.network.exception.HttpException;
import com.asyey.footballlibrary.network.http.ResponseInfo;
import com.google.gson.Gson;
import com.henanjianye.soon.communityo2o.BaseActivity;
import com.henanjianye.soon.communityo2o.R;
import com.henanjianye.soon.communityo2o.common.Constant;
import com.henanjianye.soon.communityo2o.common.MyLog;
import com.henanjianye.soon.communityo2o.common.OrderDataHelper;
import com.henanjianye.soon.communityo2o.common.UserDataHelper;
import com.henanjianye.soon.communityo2o.common.enties.AccountBean;
import com.henanjianye.soon.communityo2o.common.enties.AccountInfoBean;
import com.henanjianye.soon.communityo2o.common.enties.AppMallOrderResponsBean;
import com.henanjianye.soon.communityo2o.common.enties.BaseDataBean;
import com.henanjianye.soon.communityo2o.common.enties.CommodityGoodsCartBean;
import com.henanjianye.soon.communityo2o.common.enties.JsonUtil;
import com.henanjianye.soon.communityo2o.common.enties.PaymentRequest;
import com.henanjianye.soon.communityo2o.common.enties.PaymentRequestWalletStep2;
import com.henanjianye.soon.communityo2o.common.enties.PaymentWalletResponse;
import com.henanjianye.soon.communityo2o.common.enties.PaymentWalletResult;
import com.henanjianye.soon.communityo2o.common.util.Base64;
import com.henanjianye.soon.communityo2o.common.util.CommonUtils;
import com.henanjianye.soon.communityo2o.common.util.DESedeCoder;
import com.henanjianye.soon.communityo2o.common.util.MD5Tool;
import com.henanjianye.soon.communityo2o.common.util.MyTitleBarHelper;
import com.henanjianye.soon.communityo2o.common.util.NetWorkStateUtils;
import com.henanjianye.soon.communityo2o.common.util.SharedPreferencesUtil;
import com.henanjianye.soon.communityo2o.common.view.CustomProgressDialog;
import com.henanjianye.soon.communityo2o.interf.NetWorkCallback;
import com.henanjianye.soon.communityo2o.view.SafeKeyboard;
import com.pingplusplus.android.PaymentActivity;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.umeng.analytics.MobclickAgent;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2015/9/14.
 */
public class PayAllWayActivity extends BaseActivity {

    private static final String SECRET_DES3KEY = "GDgLwwdK270Qj1w4xho8lyTp";
    private CheckBox cb_state0;
    private TextView tv_balabce_wallet;
    private AccountInfoBean accountInfoBean;
    private View rl_goods_props;
    private ImageView iv_close;
    private TextView tv_step2_pay_infor;
    private TextView tv_step2_pay_way;
    private TextView tv_step2_totle_price;
    private Button bt_buy;
    private AlertDialog dialog;
    private PaymentRequestWalletStep2 paymentRequestWalletStep2;
    private AlertDialog alertDialog_set_paw;
    private LinearLayout ll_ali_pay;
    private LinearLayout ll_weixin_pay;
    private LinearLayout ll_soon_e_pay;
    private LinearLayout ll_union_pay;

    public static void startPayAllWayActivityForresult(Activity context, int requestCode, AppMallOrderResponsBean appMallOrderResponsBean) {
        Intent intent = new Intent(context, PayAllWayActivity.class);
        intent.putExtra(PAYRESULT_KEY, appMallOrderResponsBean);
        context.startActivityForResult(intent, requestCode);
    }
    private MyTitleBarHelper myTitleBarHelper;
    private TextView tv_total, tv_detail, tv_name;
    private CheckBox cb_state1, cb_state3, cb_state2;
    private Button bt_zhifu;
    Map<String, String> resultunifiedorder;
    /**
     * 银联支付渠道
     */
    private static final String CHANNEL_UPMP = "upmp";
    /**
     * 钱包支付渠道
     */
    private static final String CHANNEL_WALLET  = "supervip";
    /**
     * 微信支付渠道
     */
    private static final String CHANNEL_WECHAT = "wx";
    /**
     * 支付支付渠道
     */
    private static final String CHANNEL_ALIPAY = "alipay";
    /**
     * 百度支付渠道
     */
    private static final String CHANNEL_BFB = "bfb";
    /**
     * 京东支付渠道
     */
    private String channel_current;
    private static final String CHANNEL_JDPAY_WAP = "jdpay_wap";
    private static final int REQUEST_CODE_PAYMENT = 1;
    /**
     * 银联
     */
    private String mMode = "01";// 设置测试模式:01为测试 00为正式环境
    private AppMallOrderResponsBean mall;
    public static final String PAYRESULT_KEY = "payresult";
    public static final String PAYRESULT_KEY_CODE = "payresult_key_code";
    private OrderDataHelper orderDataHelper;
    private String price;
    private TextView tv_bottom_totle_price;

    @Override
    public int mysetContentView() {
        return R.layout.pay_activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initEvent();
        initProcess();
    }

    private void initProcess() {
        orderDataHelper = new OrderDataHelper(this);
        channel_current = CHANNEL_WALLET;
    }
    private void initView() {
        /**
         * 微信支付
         */
        Intent intent = getIntent();
        Object Object = intent
                .getSerializableExtra(PAYRESULT_KEY);
        if (Object != null && Object instanceof AppMallOrderResponsBean) {
            mall = (AppMallOrderResponsBean) Object;
        } else {
            toast("订单信息有误");
            return;
        }
        ArrayList<CommodityGoodsCartBean.GoodsCart.Goods> goodes = new ArrayList<>();
        goodes.add(new CommodityGoodsCartBean.GoodsCart.Goods());

        ll_ali_pay = (LinearLayout)findViewById(R.id.ll_ali_pay);
        ll_weixin_pay = (LinearLayout)findViewById(R.id.ll_weixin_pay);
        ll_soon_e_pay = (LinearLayout)findViewById(R.id.ll_soon_e_pay);
        ll_union_pay = (LinearLayout)findViewById(R.id.ll_union_pay);


        cb_state0=(CheckBox)findViewById(R.id.cb_state0);
        cb_state0.setChecked(true);
//        cb_state0.setEnabled(false);
        tv_balabce_wallet=(TextView)findViewById(R.id.tv_balabce_wallet);
        cb_state1 = (CheckBox) findViewById(R.id.cb_state1);

        cb_state2 = (CheckBox) findViewById(R.id.cb_state2);
        cb_state3 = (CheckBox) findViewById(R.id.cb_state3);
        bt_zhifu = (Button) findViewById(R.id.bt_zhifu);
        tv_detail = (TextView) findViewById(R.id.tv_detail);
        tv_total = (TextView) findViewById(R.id.tv_total);
        tv_bottom_totle_price = (TextView) findViewById(R.id.tv_bottom_totle_price);
        tv_name = (TextView) findViewById(R.id.tv_name);
        if (mall != null) {
            tv_total.setText("订单金额： ¥" + CommonUtils.floatToTowDecima(mall.totalPrice));
            tv_detail.setText("订单编号：" + mall.orderNos);
            tv_name.setText("商品名称：" + mall.goodsNames);
            tv_bottom_totle_price.setText("¥" + CommonUtils.floatToTowDecima(mall.totalPrice));
        }else{
            toast("订单错误");
        }
        myTitleBarHelper = new MyTitleBarHelper(this, getWindow().getDecorView().findViewById(android.R.id.content));
        myTitleBarHelper.resetState();
        myTitleBarHelper.setMiddleText("收银台");
        rl_goods_props =  findViewById(R.id.rl_goods_props);
        iv_close=(ImageView)findViewById(R.id.iv_close);
        iv_close.setOnClickListener(this);
        tv_step2_pay_infor=(TextView)findViewById(R.id.tv_step2_pay_infor);
        tv_step2_pay_way=(TextView)findViewById(R.id.tv_step2_pay_way);
        tv_step2_totle_price=(TextView)findViewById(R.id.tv_step2_totle_price);
        bt_buy=(Button)findViewById(R.id.bt_buy);
        bt_buy.setOnClickListener(this);

    }

    private void initEvent() {
        myTitleBarHelper.setOnclickListener(this);
        bt_zhifu.setOnClickListener(this);
        ll_soon_e_pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = cb_state0.isChecked();
                if (!isChecked) {
                    channel_current = CHANNEL_WALLET;
                    cb_state0.setChecked(true);
                    cb_state2.setChecked(false);
                    cb_state3.setChecked(false);
                    cb_state1.setChecked(false);
//                    cb_state0.setEnabled(false);
                }
            }
        });
//        cb_state0.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView,
//                                         boolean isChecked) {
//                if (isChecked) {
//                    channel_current = CHANNEL_WALLET;
//                    cb_state2.setChecked(false);
//                    cb_state3.setChecked(false);
//                    cb_state1.setChecked(false);
//                    cb_state0.setEnabled(false);
//                } else {
//                    cb_state0.setEnabled(true);
//                }
//            }
//        });

        ll_weixin_pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = cb_state1.isChecked();
                if (!isChecked) {
                    channel_current = CHANNEL_WECHAT;
                    cb_state1.setChecked(true);
                    cb_state2.setChecked(false);
                    cb_state3.setChecked(false);
                    cb_state0.setChecked(false);
//                    cb_state1.setEnabled(false);
                }
            }
        });

//        cb_state1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView,
//                                         boolean isChecked) {
//                if (isChecked) {
//                    channel_current = CHANNEL_WECHAT;
//                    cb_state2.setChecked(false);
//                    cb_state3.setChecked(false);
//                    cb_state1.setEnabled(false);
//                    cb_state0.setChecked(false);
//                }else{
//                    cb_state1.setEnabled(true);
//                }
//            }
//        });
        ll_ali_pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = cb_state2.isChecked();
                if (!isChecked) {
                    channel_current = CHANNEL_ALIPAY;
                    cb_state2.setChecked(true);
                    cb_state1.setChecked(false);
                    cb_state3.setChecked(false);
                    cb_state0.setChecked(false);
//                    cb_state2.setEnabled(false);
                }
            }
        });
//        cb_state2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView,
//                                         boolean isChecked) {
//                if (isChecked) {
//                    channel_current = CHANNEL_ALIPAY;
//                    cb_state1.setChecked(false);
//                    cb_state3.setChecked(false);
//                    cb_state2.setEnabled(false);
//                    cb_state0.setChecked(false);
//                }else{
//                    cb_state2.setEnabled(true);
//                }
//            }
//        });
        ll_union_pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = cb_state3.isChecked();
                if (!isChecked) {
                    channel_current = CHANNEL_UPMP;
                    cb_state3.setChecked(true);
                    cb_state1.setChecked(false);
                    cb_state2.setChecked(false);
                    cb_state0.setChecked(false);
//                    cb_state3.setEnabled(false);
                }
            }
        });
//        cb_state3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView,
//                                         boolean isChecked) {
//                if (isChecked) {
//                    channel_current = CHANNEL_UPMP;
//                    cb_state2.setChecked(false);
//                    cb_state1.setChecked(false);
//                    cb_state3.setEnabled(false);
//                    cb_state0.setChecked(false);
//                } else {
//                    cb_state3.setEnabled(true);
//                }
//
//            }
//        });
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.iv_title_bar_left:
                finish();
                break;
            case R.id.iv_close:
                rl_goods_props.setVisibility(View.GONE);
                break;
            case R.id.bt_buy:
               //TODO 点击支付显示输入密码界面
                createDialogForSubmmit(PayAllWayActivity.this);
                break;
            case R.id.bt_zhifu:
                if(CHANNEL_WALLET.equals(channel_current)){
                    requestCheckSetStatus();
//                    requestPurseData();
                }else{
//                    String amountText = "1";
//                    String replaceable = String.format("[%s, \\s.]",
//                            NumberFormat.getCurrencyInstance(Locale.CHINA).getCurrency()
//                                    .getSymbol(Locale.CHINA));
//                    String cleanString = amountText.toString().replaceAll(replaceable, "");
//                    int amount = Integer.valueOf(new BigDecimal(cleanString).toString());
                    // 支付宝，微信支付，银联，百度钱包 按键的点击响应处理
//                new PaymentTask().execute(new PaymentRequest(channel_current,
//                        (int) (mall.totalPrice * 100)));
                    new PaymentTask().execute(new PaymentRequest(channel_current,
                            (int) (mall.totalPrice * 100), mall.orderNos));
                }
                break;
        }
    }
    /**
     * 请求是否设置过密码
     */
    private void requestCheckSetStatus() {
        new UserDataHelper(this).getSetPsdStatus(getNetRequestHelper(PayAllWayActivity.this));
    }

    private void createDialogForSubmmit(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.dia_psd_input, null);

        dialog = new AlertDialog.Builder(context).setView(view).setTitle("安全验证").create();
        dialog.show();

        Window window = dialog.getWindow();
        window.setContentView(R.layout.dia_psd_input);
        WindowManager.LayoutParams params =
                dialog.getWindow().getAttributes();
        params.height =  WindowManager.LayoutParams.FILL_PARENT ;
        dialog.getWindow().setAttributes(params);
        final EditText et_psw_jycion = (EditText) dialog.findViewById(R.id.et_psw_jycion);
        dialog.getWindow().getDecorView().getRootView().setBackgroundColor(Color.TRANSPARENT);
        new SafeKeyboard(et_psw_jycion, 6, SafeKeyboard.TYPE_DIGIT_ONLY).setViewToBeShownIn(et_psw_jycion.getRootView());
        //确定按钮
        final TextView tv_ok = (TextView) dialog.findViewById(R.id.tv_ok);
        tv_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO  提交请求支付
                if (TextUtils.isEmpty(et_psw_jycion.getText().toString())) {
                    toast("输入密码为空");
                } else {
//                    toast(et_psw_jycion.getText().toString());
                    if(  paymentRequestWalletStep2!=null){
//                        String keyData = secret_key + customid + balanceAmount + coinAmount + orderId + getMpwd(payPwd) + "" + zzcard_regist_source_code;

                        String keyData = paymentRequestWalletStep2.secret_key + paymentRequestWalletStep2.customid + paymentRequestWalletStep2.balanceAmount +
                                paymentRequestWalletStep2.coinAmount + paymentRequestWalletStep2.orderid +
                                getMpwd(MD5Tool.MD5PsdSet(et_psw_jycion.getText().toString())) + ""+ URLEncoder.encode(paymentRequestWalletStep2.backUrl) + paymentRequestWalletStep2.sourceCode;
                        String key = MD5Tool.MD5PsdSet(keyData);
                        paymentRequestWalletStep2.setPsw(getMpwd(MD5Tool.MD5PsdSet(et_psw_jycion.getText().toString())));
                        paymentRequestWalletStep2.key=key;
                      MyLog.e("AAA","paymentRequestWalletStep2-----"+paymentRequestWalletStep2.toString());
                        new  PaymentTaskStep2().execute(paymentRequestWalletStep2);
                    }else{
                        toast("支付信息有误，请尝试其他支付方式");
                        dialog.dismiss();
                    }
                }
            }
        });
        //忘记密码
        TextView tv_forget_psd = (TextView) dialog.findViewById(R.id.tv_forget_psd);
        tv_forget_psd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 取消对话框
                dialog.dismiss();
            }
        });
    }

    private static final String before_pwd_prefix = "jycard";
    private static final String after_pwd_prefix = "jycoin";

    private String getMpwd(String pwd){
//		pwd = passwordEncoder.encodePassword(pwd); //md5鍔犲瘑
        if(pwd==null){
            return "";
        }
        String newpwd = before_pwd_prefix+pwd+after_pwd_prefix;
        return new String(Base64.encode(newpwd.getBytes()));
    }

    /**
     * 传输数据加密
     * @param str
     * @return
     * @throws Exception
     */
    private String getMi(String str,String orderId) throws Exception {
        String strMi = DESedeCoder.encode(str, SECRET_DES3KEY);
        String param = "{" +
                "\"datami\":\""+strMi+"\"," +"\"orderid\":\""+orderId+"\""+
                "}";
        return param;
    }


    /**
     * onActivityResult 获得支付结果，如果支付成功，服务器会收到ping++ 服务器发送的异步通知。 最终支付成功根据异步通知为准
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //TODO 设置点击按钮为enable
        bt_zhifu.setEnabled(true);
        // 支付页面返回处理
        if (requestCode == REQUEST_CODE_PAYMENT) {
            if (resultCode == Activity.RESULT_OK) {
                String result = data.getExtras().getString("pay_result");
                /*
                 * 处理返回值 "success" - payment succeed "fail" - payment failed
				 * "cancel" - user canceld "invalid" - payment plugin not
				 */
                String errorMsg = data.getExtras().getString("error_msg"); // 错误信息
                String extraMsg = data.getExtras().getString("extra_msg"); // 错误信息
//                showMsg(result, errorMsg, extraMsg);
                Intent intent = new Intent(this, ZhiFuJieGuoActivity.class);
                intent.putExtra(PayAllWayActivity.PAYRESULT_KEY, mall);
                HashMap<String, Object> payResult = new HashMap<String, Object>();
                payResult.put("orderNo", mall.orderNos);
                if (result != null && result.equals("success")) {
                    intent.putExtra(PayAllWayActivity.PAYRESULT_KEY_CODE, 1);
                    payResult.put("isSuccess", 1);
                    getNetRequestHelper(PayAllWayActivity.this).isShowProgressDialog(false).postRequest(Constant.PayUrl.PAY_RESULT_INTERFACE, payResult, Constant.PayUrl.PAY_RESULT_INTERFACE);
                    startActivity(intent);
                    PayAllWayActivity.this.finish();
                } else if (result != null && result.equals("cancel")) {
                    payResult.put("isSuccess", 0);
                    getNetRequestHelper(PayAllWayActivity.this).isShowProgressDialog(false).postRequest(Constant.PayUrl.PAY_RESULT_INTERFACE, payResult, Constant.PayUrl.PAY_RESULT_INTERFACE);
                    intent.putExtra(PayAllWayActivity.PAYRESULT_KEY_CODE, 3);
                    startActivity(intent);
                    PayAllWayActivity.this.finish();
                } else if (result != null && result.equals("invalid")) {

                } else {
                    payResult.put("isSuccess", 0);
                    getNetRequestHelper(PayAllWayActivity.this).isShowProgressDialog(false).postRequest(Constant.PayUrl.PAY_RESULT_INTERFACE, payResult, Constant.PayUrl.PAY_RESULT_INTERFACE);
                    intent.putExtra(PayAllWayActivity.PAYRESULT_KEY_CODE, 0);
                    startActivity(intent);
                    PayAllWayActivity.this.finish();
                }
//                setResult(RESULT_OK, getIntent());
            }
        } else if(requestCode==SETTLEMENTE_SET_PAY_PSW&&resultCode== RESULT_OK){
            toast("密码设置成功");
//            currentPayPaw= data.getStringExtra(PursePsdManagerActivity.PSDMD5);
            if(alertDialog_set_paw!=null){
                alertDialog_set_paw.dismiss();
            }
            requestPurseData();
        }
    }
    private void requestPurseData() {
        new UserDataHelper(this).getPurseInfor(getNetRequestHelper(this).isShowProgressDialog(true));
    }

    public void showMsg(String title, String msg1, String msg2) {
        String str = title;
        if (null != msg1 && msg1.length() != 0) {
            str += "\n" + msg1;
        }
        if (null != msg2 && msg2.length() != 0) {
            str += "\n" + msg2;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(PayAllWayActivity.this);
        builder.setMessage(str);
        builder.setTitle("提示");
        builder.setPositiveButton("OK", null);
        builder.create().show();
    }

    class PaymentTask extends AsyncTask<PaymentRequest, Void, String> {

        private CustomProgressDialog customProgressDialog;

        @Override
        protected void onPreExecute() {
            // 按键点击之后的禁用，防止重复点击
            if(!channel_current.equals(CHANNEL_WALLET)){
                bt_zhifu.setEnabled(false);
            }
            customProgressDialog = CustomProgressDialog.createDialog(PayAllWayActivity.this);
            customProgressDialog.show();
        }

        @Override
        protected String doInBackground(PaymentRequest... pr) {
            PaymentRequest paymentRequest = pr[0];
            String data = null;
            String json = new Gson().toJson(paymentRequest);
            MyLog.e("TTT", "appId-00startIndex-json----===-" + json);
            try {
                // 向Your Ping++ Server SDK请求数据
                data = postJson(Constant.PayUrl.PAY_SERVER_INFOR, json);
            } catch (Exception e) {
                e.printStackTrace();
            }
            MyLog.e("TTT", "appId-00startIndex-data----===-" + data);
            return data;
        }

        private String postJson(String url, String json) throws IOException {
            MediaType type = MediaType.parse("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(type, json);
            Request request = new Request.Builder().url(url).post(body).build();
            OkHttpClient client = new OkHttpClient();
            Response response = client.newCall(request).execute();
            String str = response.body().string();
            return str;
        }

        /**
         * 获得服务端的charge，调用ping++ sdk。
         */
        @Override
        protected void onPostExecute(String data) {
            if (null != data) {
                if(channel_current.equals(CHANNEL_WALLET)){
                    PaymentWalletResponse paymentWalletResponse = JsonUtil.parseSimpleJsonObject(data, PaymentWalletResponse.class);
                    if(paymentWalletResponse!=null&&paymentWalletResponse.credential!=null&&"true".equals(paymentWalletResponse.credential.supervip.canPay)){
                        paymentRequestWalletStep2=    new PaymentRequestWalletStep2(paymentWalletResponse.credential.supervip.customid
                                ,paymentWalletResponse.credential.supervip.orderId,paymentWalletResponse.credential.supervip.sourceCode
                                ,paymentWalletResponse.credential.supervip.backUrl,mall.totalPrice);
                        paymentRequestWalletStep2.secret_key=paymentWalletResponse.credential.supervip.secretKey;
                        paymentRequestWalletStep2.callurl=paymentWalletResponse.credential.supervip.callurl;
                        //TODO 显示确认界面，确认支付，然后输密码
                        rl_goods_props.setVisibility(View.VISIBLE);
                        tv_step2_pay_infor.setText(paymentWalletResponse.body);
                        tv_step2_totle_price.setText(CommonUtils.floatToTowDecima(mall.totalPrice)+"元");
                    }else{
                        toast("支付失败，请尝试其他支付方式");
                    }

                }else{

                    Intent intent = new Intent();
                    String packageName = getPackageName();
                    ComponentName componentName = new ComponentName(packageName,
                            packageName + ".wxapi.WXPayEntryActivity");
                    intent.setComponent(componentName);
                    intent.putExtra(PaymentActivity.EXTRA_CHARGE, data);
                    startActivityForResult(intent, REQUEST_CODE_PAYMENT);
                }
            } else {
                showMsg("请求出错", "请检查URL", "URL无法获取charge");
            }
            if (customProgressDialog != null) {
                customProgressDialog.cancel();
            }
        }

    }

    class PaymentTaskStep2 extends AsyncTask<PaymentRequestWalletStep2, Void, String> {

        private CustomProgressDialog customProgressDialog;

        @Override
        protected void onPreExecute() {
            // 按键点击之后的禁用，防止重复点击
            bt_zhifu.setEnabled(false);
            bt_buy.setEnabled(false);
            customProgressDialog = CustomProgressDialog.createDialog(PayAllWayActivity.this);
            customProgressDialog.show();
        }

        @Override
        protected String doInBackground(PaymentRequestWalletStep2... pr) {
            PaymentRequestWalletStep2 paymentRequest = pr[0];
            String data = null;
//            String json = new Gson().toJson(paymentRequest);

            String json =  "{" +
                    "\"customid\":\""+paymentRequest.customid+"\"," +
                    "\"balanceAmount\":\""+paymentRequest.balanceAmount+"\"," +
                    "\"coinAmount\":\""+paymentRequest.coinAmount+"\"," +
                    "\"orderId\":\""+paymentRequest.orderid+"\"," +
                    "\"key\":\""+paymentRequest.key+"\"," +
                    "\"payPwd\":\""+paymentRequest.payPwd+"\"," +
                    "\"backUrl\":\""+ URLEncoder.encode(paymentRequest.backUrl)+"\"," +
                    "\"sourceCode\":\""+paymentRequest.sourceCode+"\"" +
                    "}";
            MyLog.e("TTT", "appId-PaymentTaskStep2-000json----===-" + json);
            try {
                String postData = getMi(json,paymentRequest.orderid);

                // 向Your Ping++ Server SDK请求数据
                data = postJson(paymentRequest.callurl, postData);
            } catch (Exception e) {
                e.printStackTrace();
            }
            MyLog.e("TTT", "appId-PaymentTaskStep2-data----===-" + data);
            return data;
        }

        private String postJson(String url, String json) throws IOException {
            MediaType type = MediaType.parse("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(type, json);
            Request request = new Request.Builder().url(url).post(body).build();
            OkHttpClient client = new OkHttpClient();
            Response response = client.newCall(request).execute();
            String str = response.body().string();
            return str;
        }

        /**
         * 获得服务端的charge，调用ping++ sdk。
         */
        @Override
        protected void onPostExecute(String data) {
            bt_zhifu.setEnabled(true);
            bt_buy.setEnabled(true);
            Intent intent = new Intent(PayAllWayActivity.this, ZhiFuJieGuoActivity.class);
            intent.putExtra(PayAllWayActivity.PAYRESULT_KEY, mall);
            HashMap<String, Object> payResult = new HashMap<String, Object>();
            payResult.put("orderNo", mall.orderNos);
            if (null != data) {
                PaymentWalletResult paymentWalletResult=    JsonUtil.parseSimpleJsonObject(data, PaymentWalletResult.class);
                if (paymentWalletResult != null && paymentWalletResult.status.equals("1")) {
                    intent.putExtra(PayAllWayActivity.PAYRESULT_KEY_CODE, 1);
                    payResult.put("isSuccess", 1);
                    getNetRequestHelper(PayAllWayActivity.this).isShowProgressDialog(false).postRequest(Constant.PayUrl.PAY_RESULT_INTERFACE, payResult, Constant.PayUrl.PAY_RESULT_INTERFACE);
                    startActivity(intent);
                    PayAllWayActivity.this.finish();
                } else  {
                    payResult.put("isSuccess", 0);
                    if(paymentWalletResult!=null){
                        payResult.put("failReason", paymentWalletResult.status);
                    }
                    getNetRequestHelper(PayAllWayActivity.this).isShowProgressDialog(false).postRequest(Constant.PayUrl.PAY_RESULT_INTERFACE, payResult, Constant.PayUrl.PAY_RESULT_INTERFACE);
                    intent.putExtra(PayAllWayActivity.PAYRESULT_KEY_CODE, 3);
                    startActivity(intent);
                    PayAllWayActivity.this.finish();
                }
            } else {
                showMsg("请求出错", "请检查URL", "URL无法获取charge");
                payResult.put("isSuccess", 0);
                getNetRequestHelper(PayAllWayActivity.this).isShowProgressDialog(false).postRequest(Constant.PayUrl.PAY_RESULT_INTERFACE, payResult, Constant.PayUrl.PAY_RESULT_INTERFACE);
                intent.putExtra(PayAllWayActivity.PAYRESULT_KEY_CODE, 3);
                startActivity(intent);
                PayAllWayActivity.this.finish();
            }
            if (customProgressDialog != null) {
                customProgressDialog.cancel();
            }
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

                if (Constant.Purse.PAYPSDSETSTATUS.equals(requestTag)) {
                    parsePsdSetStatus(responseInfo.result);
                }
                if (Constant.PayUrl.PAY_SERVER_INFOR.equals(requestTag)) {
//                    MyLog.e("TTT", "responseInfo.result===" + responseInfo.result);
//                    toast(responseInfo.result);
//                    if (null == responseInfo.result) {
//                        showMsg("请求出错", "请检查URL", "URL无法获取charge");
//                        return;
//                    }
//                    Intent intent = new Intent();
//                    String packageName = getPackageName();
//                    ComponentName componentName = new ComponentName(packageName,
//                            packageName + ".wxapi.WXPayEntryActivity");
//                    intent.setComponent(componentName);
//                    intent.putExtra(PaymentActivity.EXTRA_CHARGE, responseInfo.result);
//                    startActivityForResult(intent, REQUEST_CODE_PAYMENT);
                }
                if (requestTag.equals(Constant.ShouYeUrl.PURSE_INFO_URL)) {
                    SharedPreferencesUtil.saveStringData(PayAllWayActivity.this,
                            Constant.ShouYeUrl.PURSE_INFO_URL, responseInfo.result);
                    parsePurseData(responseInfo.result);
                }
            }

            @Override
            public void onFailure(HttpException error, String msg, String requestTag) {
                NetWorkStateUtils.errorNetMes(PayAllWayActivity.this);
            }
        };
    }
    public static final int SETTLEMENTE_SET_PAY_PSW = 112;
    private void parsePsdSetStatus(String result) {
        try {
            MyLog.e("AAA","result-----"+result);
            BaseDataBean<AccountBean> json = JsonUtil.parseDataObject(result, AccountBean.class);
            if (json.code == 100) {
                AccountBean aBean = json.data;
                if (aBean != null) {
                    if (aBean.flag) {
                        //已设置过密码
                        requestPurseData();
                    } else {
                        //还没有设置过密码,显示对话框询问用户设置密码
                             alertDialog_set_paw=      CommonUtils.createDialogToAddPsw(PayAllWayActivity.this,SETTLEMENTE_SET_PAY_PSW);
                    }
                }
            } else {
                Toast.makeText(this, "数据异常，请稍后重试", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void parsePurseData(String purseInfo) {
        try {
            MyLog.e("AAA","purseInfo---"+purseInfo);
            BaseDataBean<AccountInfoBean> json = JsonUtil.parseDataObject(purseInfo, AccountInfoBean.class);
            if (json.code == 100) {
                 accountInfoBean = json.data;
                if (accountInfoBean != null) {
                  String cardNum= accountInfoBean.cardNum;
                    if(cardNum==null){
                        toast("钱包数据错误");
                    }else if(Float.parseFloat(accountInfoBean.balance)<mall.totalPrice){
                        toast("钱包余额不足");
                    }else{
                        new PaymentTask().execute(new PaymentRequest(channel_current,
                                (int) (mall.totalPrice * 100), mall.orderNos,cardNum));
                    }
                }
            } else {
                toast("钱包数据错误");
            }
        } catch (Exception e) {
            e.printStackTrace();
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
