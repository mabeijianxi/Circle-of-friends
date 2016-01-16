package anim.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.asyey.footballlibrary.network.exception.HttpException;
import com.asyey.footballlibrary.network.http.ResponseInfo;
import com.asyey.footballlibrary.universalimageloader.core.DisplayImageOptions;
import com.asyey.footballlibrary.universalimageloader.core.ImageLoader;
import com.henanjianye.soon.communityo2o.BaseActivity;
import com.henanjianye.soon.communityo2o.MainActivity;
import com.henanjianye.soon.communityo2o.R;
import com.henanjianye.soon.communityo2o.adapter.Gv_Order_list_Imags_Adapter;
import com.henanjianye.soon.communityo2o.common.Constant;
import com.henanjianye.soon.communityo2o.common.OrderDataHelper;
import com.henanjianye.soon.communityo2o.common.enties.AppConsigneeInfor;
import com.henanjianye.soon.communityo2o.common.enties.AppMallOrderResponsBean;
import com.henanjianye.soon.communityo2o.common.enties.BaseDataBean;
import com.henanjianye.soon.communityo2o.common.enties.CommodityOrderBean;
import com.henanjianye.soon.communityo2o.common.enties.JsonUtil;
import com.henanjianye.soon.communityo2o.common.util.CommonUtils;
import com.henanjianye.soon.communityo2o.common.util.MyTitleBarHelper;
import com.henanjianye.soon.communityo2o.common.util.NetWorkStateUtils;
import com.henanjianye.soon.communityo2o.common.util.SharedPreferencesUtil;
import com.henanjianye.soon.communityo2o.interf.NetWorkCallback;
import com.henanjianye.soon.communityo2o.view.CountdownViewUseList;
import com.henanjianye.soon.communityo2o.view.MyGridView;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;

/**
 * Created by sks on 2015/10/12.
 */
public class OrderDetailActivity extends BaseActivity {
    private OrderDataHelper orderDataHelper;
    private long orderid;
    private CommodityOrderBean commodityOrderBean;
    private MyTitleBarHelper myTitleBarHelper;
    private TextView tv_order_id;
    private TextView tv_order_time;
    private TextView tv_order_consignees_name;
    private TextView tv_order_consignees_phone;
    private TextView tv_order_consignees_address1;
    private TextView tv_order_consignees_address2;
    private TextView tv_order_pay_way;
    private TextView tv_order_take_way;
    private TextView tv_order_totle_price;
    private TextView tv_order_trainfee_price;
    private TextView tv_order_pay_price;
    private TextView tv_button_left;
    private TextView tv_button_right;
    private ImageView iv_order_detail_img;
    private TextView tv_order_detail_title;
    private TextView tv_order_detail_price;
    private TextView tv_order_detail_num;
    private TextView iv_commodity_settement_title_tag;
    private TextView iv_commodity_settement_title;
    private RelativeLayout detail_sg_item1;
    private LinearLayout detail_multi_item2;
    private MyGridView gv_commodity_settement_imag;
    private RelativeLayout Ll_order_consignees;
    private AppConsigneeInfor.Address currentAddress;
    private int currentStatue;
    private int currentStateCode;
    public static int ORDERDETAILACTIVITY_JUMP = 12355;
    public static int ORDERDETAILACTIVITY_JUMP_GOODSRETRUNACTIVITY = 22354;
    public static int ORDERDETAILACTIVITY_JUMP_PAYALLWAYACTIVITY = 32354;
    public static int ORDERDETAILACTIVITY_JUMP_WRITEINFOFORGOODSRETURNS = 42354;
    private CountdownViewUseList countDownView;
    private boolean ifDataChange;
    private TextView tv_last_time;

    @Override
    public int mysetContentView() {
        return R.layout.order_detailactivity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
        initEvent();
        initProcess();
    }

    private void initViews() {
        tv_order_id = (TextView) findViewById(R.id.tv_order_id);
        tv_order_time = (TextView) findViewById(R.id.tv_order_time);
        tv_order_consignees_name = (TextView) findViewById(R.id.tv_order_consignees_name);
        tv_order_consignees_phone = (TextView) findViewById(R.id.tv_order_consignees_phone);
        tv_order_consignees_address1 = (TextView) findViewById(R.id.tv_order_consignees_address1);
        tv_order_consignees_address2 = (TextView) findViewById(R.id.tv_order_consignees_address2);
        //收货人点击
        Ll_order_consignees = (RelativeLayout) findViewById(R.id.Ll_order_consignees);
        //中间信息
        iv_commodity_settement_title = (TextView) findViewById(R.id.iv_commodity_settement_title);
        tv_order_pay_way = (TextView) findViewById(R.id.tv_order_pay_way);
        tv_order_pay_way.setText("在线支付");
        tv_order_take_way = (TextView) findViewById(R.id.tv_order_take_way);
        tv_order_totle_price = (TextView) findViewById(R.id.tv_order_totle_price);
        tv_order_trainfee_price = (TextView) findViewById(R.id.tv_order_trainfee_price);
        tv_order_pay_price = (TextView) findViewById(R.id.tv_order_pay_price);
        //商品信息  sg
        detail_sg_item1 = (RelativeLayout) findViewById(R.id.detail_sg_item1);
        iv_order_detail_img = (ImageView) findViewById(R.id.iv_order_detail_img);
        tv_order_detail_title = (TextView) findViewById(R.id.tv_order_detail_title);
        tv_order_detail_price = (TextView) findViewById(R.id.tv_order_detail_price);
        tv_order_detail_num = (TextView) findViewById(R.id.tv_order_detail_num);
        //商品信息  multi
        detail_multi_item2 = (LinearLayout) findViewById(R.id.detail_multi_item2);
        gv_commodity_settement_imag = (MyGridView) findViewById(R.id.gv_commodity_settement_imag);
        //底部按钮
        tv_button_left = (TextView) findViewById(R.id.tv_button_left);
        tv_button_right = (TextView) findViewById(R.id.tv_button_right);
        countDownView = (CountdownViewUseList) findViewById(R.id.cv_time);
        tv_last_time = (TextView) findViewById(R.id.tv_last_time);
        Intent intent = getIntent();
        isNotice = intent.getBooleanExtra("onlineFlag", false);
    }

    private boolean isNotice = false;
    //    Intent intent=getIntent();
    //    isNotice = intent.getBooleanExtra("onlineFlag", false);
    private void press_back() {
        if (isNotice && SharedPreferencesUtil.getStringData(this, Constant.ShouYeUrl.APP_STATUS, "0").equals("1")) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            if (ifDataChange) {
                setResult(RESULT_OK, getIntent());
                ifDataChange = false;
            }
            this.finish();
        }
    }


    private void initEvent() {
        tv_button_left.setOnClickListener(this);
        tv_button_right.setOnClickListener(this);
        Ll_order_consignees.setOnClickListener(this);
    }

    private void initProcess() {
        Intent intent = getIntent();
        orderid = intent.getLongExtra(CommodityOrderListActivity.ORDERID, -1);
        if (orderid == -1) {
            toast("订单id错误");
            finish();
            return;
        }
        orderDataHelper = new OrderDataHelper(this);
        orderDataHelper.getOrderDetailInfor(getNetRequestHelper(this), orderid);
        myTitleBarHelper = new MyTitleBarHelper(this, getWindow().getDecorView().getRootView());
        myTitleBarHelper.setMiddleText("订单状态");
        myTitleBarHelper.setOnclickListener(this);
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
                if (Constant.OrderUrl.COMMODITY_ORDER_DETAIL.equals(requestTag)) {
                    parseDate(responseInfo.result);
                }
                if (Constant.OrderUrl.COMMODITY_ORDER_CANCEL.equals(requestTag)) {
                    if (responseInfo.result == null) {
                        toast("数据错误");
                        return;
                    }
                    BaseDataBean<Object> baseBean = JsonUtil.parseDataObject(responseInfo.result, Object.class);
                    if (baseBean.code == 100) {
                        // 取消
                        ifDataChange = true;
                        orderDataHelper.getOrderDetailInfor(getNetRequestHelper(OrderDetailActivity.this), orderid);
                    } else {
                        toast(baseBean.msg);
                    }
                }
                if (Constant.OrderUrl.COMMODITY_ORDER_DELETE.equals(requestTag)) {
                    if (responseInfo.result == null) {
                        toast("数据错误");
                        return;
                    }
                    BaseDataBean<Object> baseBean = JsonUtil.parseDataObject(responseInfo.result, Object.class);
                    if (baseBean.code == 100) {
                        //  删除
//                        orderDataHelper.getOrderDetailInfor(getNetRequestHelper(OrderDetailActivity.this), orderid);
                        setResult(RESULT_OK, getIntent());
                        OrderDetailActivity.this.finish();
                    } else {

                        toast(baseBean.msg);
                    }
                }
                if (Constant.OrderUrl.COMMODITY_ORDER_RECEIVED_CONFIRM.equals(requestTag)) {
                    if (responseInfo.result == null) {
                        toast("数据错误");
                        return;
                    }
                    BaseDataBean<Object> baseBean = JsonUtil.parseDataObject(responseInfo.result, Object.class);
                    if (baseBean.code == 100) {
                        //  确认收货
                        ifDataChange = true;
                        orderDataHelper.getOrderDetailInfor(getNetRequestHelper(OrderDetailActivity.this), orderid);
                    } else {
                        toast(baseBean.msg);
                    }
                }
            }

            @Override
            public void onFailure(HttpException error, String msg, String requestTag) {
                NetWorkStateUtils.errorNetMes(OrderDetailActivity.this);
            }
        };
    }


    @Override
    public void onBackPressed() {
        press_back();
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_title_bar_left:
                press_back();
                break;
            case R.id.tv_button_left:
                clickEventleft(v);
//                finish();
                break;
            case R.id.tv_button_right:
                clickEventRight(v);
                break;
        }
    }

    private void clickEventRight(View v) {
//        int[] aa = (int[]) v.getTag();
//        int tag = aa[1];
//        int pos = aa[0];
        switch (currentStateCode) {
            case 0:
                //已关闭(已取消)
//                setButtonText(cellHolderHeader, "", false, "删除订单", true);
                orderDelete();
                break;
            case 10:
                //待付款
//                setButtonText(cellHolderHeader, "取消订单", false, "立即支付", true);
                orderPayNow();
                break;
            case 11:
                // 等待支付确认中
                orderCallForHelp();
                break;
            case 20:
                //已付款
//                if (commodityOrderBean.isShowReturnRequest) {
//                    currentStateCode = 200;
//                    setButtonText(cellHolderHeader, "联系客服", false, "已支付", false);
//                } else {
//                    setButtonText(cellHolderHeader, "联系客服", false, "已支付", false);
//                }

                break;
            case 200:
                //已付款
//                if (commodityOrderBean.isShowReturnRequest) {
//                    currentStateCode = 200;
//                    setButtonText(cellHolderHeader, "联系客服", false, "已支付", false);
//                } else {
//                    setButtonText(cellHolderHeader, "联系客服", false, "已支付", false);
//                }

                break;
            case 26:
                // 退款确认中
//                setButtonText(cellHolderHeader, "联系客服", false, "已确认", false);
                break;
            case 30:
                //已发货
//                if (commodityOrderBean.isShowReturnRequest) {
//                    currentStateCode = 300;
//                    setButtonText(cellHolderHeader, "联系客服", false, "已发货", false);
//                } else {
//                    setButtonText(cellHolderHeader, "联系客服", false, "确认收货", true);
//                }

                orderConfirmRecieve();
                break;
            case 300:
                //已发货
//                if (commodityOrderBean.isShowReturnRequest) {
//                    currentStateCode = 300;
//                    setButtonText(cellHolderHeader, "联系客服", false, "已发货", false);
//                } else {
//                    setButtonText(cellHolderHeader, "联系客服", false, "确认收货", true);
//                }
                break;
            case 40:
                // 已收货
                if (this.commodityOrderBean.isShowReturnRequest) {

                } else{
                    startDispatchActivity(-1,commodityOrderBean);
                }
//                if (commodityOrderBean.isShowReturnRequest) {
//                    currentStateCode = 400;
//                    setButtonText(cellHolderHeader, "联系客服", false, "已收货", false);
//                } else {
//                    setButtonText(cellHolderHeader, "联系客服", false, "已收货", false);
//                }
                break;
            case 400:
                // 已收货
//                if (commodityOrderBean.isShowReturnRequest) {
//                    currentStateCode = 400;
//                    setButtonText(cellHolderHeader, "联系客服", false, "已收货", false);
//                } else {
//                    setButtonText(cellHolderHeader, "联系客服", false, "已收货", false);
//                }
                break;
            case 45:
                // 取消订单申请中
//                setButtonText(cellHolderHeader, "联系客服", false, "退货物流", true);

                orderWriteFeedBackInfor();
                break;
            case 46:
                // 退货中
//                setButtonText(cellHolderHeader, "联系客服", false, "退货中", false);
                break;
            case 47:
                // 取消订单成功
                // 退货中
//                setButtonText(cellHolderHeader, "联系客服", false, "删除订单", true);
                orderDelete();
                break;
            case 48:
                // 取消订单失败
//                setButtonText(cellHolderHeader, "联系客服", false, "删除订单", true);
                orderDelete();
                break;
            case 50:
                //已完成
//                setButtonText(cellHolderHeader, "", false, "删除订单", false);
                orderDelete();
                break;
        }
    }

    private void clickEventleft(View v) {
        switch (currentStateCode) {
            case 0:
                //已关闭(已取消)
//                setButtonText(cellHolderHeader, "", false, "删除订单", true);
                break;
            case 10:
                //待付款
//                setButtonText(cellHolderHeader, "取消订单", false, "立即支付", true);
                orderCancle();
                break;
            case 11:
                // 等待支付确认中

                break;
            case 20:
                //已付款
//                if (commodityOrderBean.isShowReturnRequest) {
//                    currentStateCode = 200;
//                    setButtonText(cellHolderHeader, "联系客服", false, "已支付", false);
//                } else {
//                    setButtonText(cellHolderHeader, "联系客服", false, "已支付", false);
//                }
                orderCancle();
//                orderCallForHelp();
                break;
            case 200:
                //已付款
                orderCallForHelp();
                break;
            case 26:
                // 退款确认中
//                setButtonText(cellHolderHeader, "联系客服", false, "已确认", false);
                orderCallForHelp();
                break;
            case 30:
                //已发货
                orderCancle();

                break;
            case 300:
                //已发货
                orderCallForHelp();

                break;
            case 40:
                // 已收货
                if (this.commodityOrderBean.isShowReturnRequest) {
                    orderCallForHelp();
                } else{
                    orderCancle();
                }

                break;

            case 400:
                // 已收货
                orderCallForHelp();
                break;
            case 45:
                // 取消订单申请中
//                setButtonText(cellHolderHeader, "联系客服", false, "退货物流", true);
                orderCallForHelp();
                break;
            case 46:
                // 退货中
//                setButtonText(cellHolderHeader, "联系客服", false, "退货中", false);
                orderCallForHelp();
                break;
            case 47:
                // 取消订单成功
                // 退货中
//                setButtonText(cellHolderHeader, "联系客服", false, "删除订单", true);
                orderCallForHelp();
                break;
            case 48:
                // 取消订单失败
//                setButtonText(cellHolderHeader, "联系客服", false, "删除订单", true);
                orderCallForHelp();
                break;
            case 49:
                // 退货(退款)失败
                break;
            case 50:
                //已完成
//                setButtonText(cellHolderHeader, "", false, "删除订单", false);


                break;
        }
    }
    private void startDispatchActivity(int pos,CommodityOrderBean commodityOrderBean) {
        if(commodityOrderBean.goods.size()>1){
            // 跳转多商品列表评价界面
            Intent intent=    new Intent(this,CommodityOrderEvaluteDetailListActivity.class);
            intent.putExtra(CommodityOrderListActivity.KEYTOECALUTELIST,commodityOrderBean);
            startActivityForResult(intent, CommodityOrderEvaluteDetailListActivity.REQUESTTOCOMMODITYORDEREVALUTEDETAILLIST);
        }else{
            // 跳转商品评价界面
            //TODO 做相应跳转
            CommodityOrderBean.OderGoods oderGoods=     commodityOrderBean.goods.get(0);
            if(oderGoods.canAddPic||oderGoods.canEvaluate){
                //TODO 跳转至评论页面
                GoodsEvaluateActivity.TransferEvaluation(OrderDetailActivity.this,oderGoods.canAddPic,oderGoods.goodsId,oderGoods.goodsCartId,oderGoods.picMap.middlePicUrl);
            }else{
                //  跳转至评论详情页面
                EaluationActivity.goEalutionActivity(OrderDetailActivity.this,oderGoods.goodsId);
            }
        }
    }
    private void orderWriteFeedBackInfor() {
        //TODO  填写退货信息   0表示已发货退款
        Intent intent = new Intent(this, WriteInfoForGoodsReturns.class);
        intent.putExtra("orderId", commodityOrderBean.orderId);
        startActivityForResult(intent, ORDERDETAILACTIVITY_JUMP_WRITEINFOFORGOODSRETURNS);
    }
    private void orderRequestFeedBack() {
        //  申请退货  0表示已发货退款
        GoodsRetrunActivity.startGoodsRetrunActivityForresult(this, commodityOrderBean.orderId, 0, ORDERDETAILACTIVITY_JUMP_GOODSRETRUNACTIVITY);
    }
    private void orderConfirmRecieve() {
        //  确认收货
        orderDataHelper.confirmTakeGoods(getNetRequestHelper(OrderDetailActivity.this), commodityOrderBean.orderId);
    }

    private void orderPayNow() {
        // 立刻支付
        AppMallOrderResponsBean appMallOrderResponsBean = new AppMallOrderResponsBean();
        appMallOrderResponsBean.orderNos = commodityOrderBean.orderNo;
        ArrayList<CommodityOrderBean.OderGoods> OderGoods = commodityOrderBean.goods;
        String name = "";
        for (CommodityOrderBean.OderGoods good : OderGoods) {
            name = name + good.goodsName + "X" + good.goodsNum;
        }
        appMallOrderResponsBean.goodsNames = name;
        appMallOrderResponsBean.totalPrice = commodityOrderBean.totalPrice;
        PayAllWayActivity.startPayAllWayActivityForresult(this, ORDERDETAILACTIVITY_JUMP_PAYALLWAYACTIVITY, appMallOrderResponsBean);
    }
    private void orderDelete() {
        //  删除订单
        orderDataHelper.deleteOrder(getNetRequestHelper(OrderDetailActivity.this), commodityOrderBean.orderId);
    }
    private void orderCancle() {
        //  取消订单
        if (currentStateCode == 10) {
            orderDataHelper.cancelOrder(getNetRequestHelper(OrderDetailActivity.this), commodityOrderBean.orderId);
        } else {
            orderRequestFeedBack();
        }
//   orderDataHelper.cancelOrder(getNetRequestHelper(OrderDetailActivity.this), commodityOrderBean.orderId);
    }

    private void orderCallForHelp() {
        //   联系客服
        CommonUtils.showPhoneDialog(OrderDetailActivity.this, commodityOrderBean.shopPhone);
    }

    /**
     * @param str
     */
    private synchronized void parseDate(String str) {
        if (str == null) {
            return;
        }
        BaseDataBean<CommodityOrderBean> baseBean = JsonUtil.parseDataObject(str, CommodityOrderBean.class);
        if (baseBean.code == 100) {
            commodityOrderBean = baseBean.data;
            setRightView(commodityOrderBean);
        } else {
            toast(baseBean.msg);
        }
    }

    private void setRightView(CommodityOrderBean commodityOrderBean) {

        tv_order_id.setText("订单编号：" + commodityOrderBean.orderNo);
        tv_order_time.setText("下单时间：" + commodityOrderBean.addtime);
//        countDownView.setMills(commodityOrderBean.diffTime);
//        countDownView.setOnFinishedListener(new CountDownView.OnFinishedListener() {
//            @Override
//            public void onFinished(CountDownView countDownView) {
//                countDownView.setText("已过期");
//            }
//        });
        currentAddress = commodityOrderBean.address;
        if (currentAddress != null) {
            tv_order_consignees_name.setText(currentAddress.receiverName);
            tv_order_consignees_phone.setText(currentAddress.mobile);
            tv_order_consignees_address1.setText(currentAddress.provinceName + " " + currentAddress.cityName + " " + currentAddress.countyName);
            tv_order_consignees_address2.setText(currentAddress.detailAddress);
        }
        if (commodityOrderBean.goods.size() > 1) {
            detail_sg_item1.setVisibility(View.GONE);
            detail_multi_item2.setVisibility(View.VISIBLE);
            Gv_Order_list_Imags_Adapter gv_order_list_imags_adapter = new Gv_Order_list_Imags_Adapter(OrderDetailActivity.this);
            gv_order_list_imags_adapter.setData(commodityOrderBean.goods);
            gv_commodity_settement_imag.setAdapter(gv_order_list_imags_adapter);
            gv_commodity_settement_imag.setClickable(false);
            gv_commodity_settement_imag.setEnabled(false);

        } else if (commodityOrderBean.goods.size() == 1) {
            CommodityOrderBean.OderGoods oderGoods = commodityOrderBean.goods.get(0);
            detail_sg_item1.setVisibility(View.VISIBLE);
            detail_multi_item2.setVisibility(View.GONE);
            if (oderGoods != null) {
                tv_order_detail_title.setText(oderGoods.goodsName);
                tv_order_detail_price.setText("¥" + CommonUtils.floatToTowDecima(oderGoods.goodsPrice));
                tv_order_detail_num.setText("X" + oderGoods.goodsNum);
                ImageLoader instance = ImageLoader.getInstance();
                DisplayImageOptions build = new DisplayImageOptions.Builder()
                        .showImageOnFail(R.drawable.ic_launcher_default)
                        .showImageForEmptyUri(R.drawable.ic_launcher_default).build();
                instance.displayImage(oderGoods.picMap.middlePicUrl, iv_order_detail_img, build);
            }
        } else {
            detail_sg_item1.setVisibility(View.GONE);
            detail_multi_item2.setVisibility(View.GONE);
        }
        //实际付款  目前不确定
        tv_order_take_way.setText(commodityOrderBean.takeWay);
        iv_commodity_settement_title.setText("由" + commodityOrderBean.serviceStation + "配送");
        // 总付款
        tv_order_totle_price.setText("¥" + CommonUtils.floatToTowDecima(commodityOrderBean.goodsTotalPrice));
        tv_order_trainfee_price.setText("¥" + CommonUtils.floatToTowDecima(commodityOrderBean.transfeee));
        // 实际付款  目前不确定
        tv_order_pay_price.setText("¥" + CommonUtils.floatToTowDecima(commodityOrderBean.payPrice));
        currentStatue = commodityOrderBean.orderStatus;
        if (currentStatue == 10) {
            countDownView.setVisibility(View.VISIBLE);
            tv_last_time.setVisibility(View.VISIBLE);
            countDownView.setOnCountdownEndListener(new CountdownViewUseList.OnCountdownEndListener() {
                @Override
                public void onEnd(CountdownViewUseList viewUseList) {
                    viewUseList.setmText("已过期");
                }
            });
            if (commodityOrderBean.diffTime == 0) {
                countDownView.setmText("已过期");
            } else {
                if (commodityOrderBean.diffTime > (24 * 3600)) {
                    countDownView.setShowDayView(true);
                } else {
                    countDownView.setShowDayView(false);
                }
                countDownView.start(commodityOrderBean.diffTime * 1000);
            }
        } else {
            countDownView.setVisibility(View.GONE);
            tv_last_time.setVisibility(View.GONE);
        }
        setRightBottomButton(currentStatue,commodityOrderBean);
    }

    private void setRightBottomButton(int currentStatue, CommodityOrderBean commodityOrderBean) {
        currentStateCode = currentStatue;
        switch (currentStatue) {
            case 0:
                //已关闭(已取消)
                myTitleBarHelper.setMiddleSecondText("交易关闭");
                setButtonText(null, false, "删除订单", true);
                break;
            case 10:
                //待付款
                myTitleBarHelper.setMiddleSecondText("等待付款");
                setButtonText("取消订单", false, "立即支付", true);
                break;
            case 11:
                // 等待支付确认中
                myTitleBarHelper.setMiddleSecondText("支付中");
                setButtonText("", false, "联系客服", false);
                break;
            case 20:
                //已付款
                if (this.commodityOrderBean.isShowReturnRequest) {
                    currentStateCode = 200;
                    myTitleBarHelper.setMiddleSecondText("取消订单中");
                    setButtonText("联系客服", false, "已支付", false);
                } else {
                    myTitleBarHelper.setMiddleSecondText("等待发货");
                    setButtonText("取消订单", false, "已支付", false);
                }

                break;
            case 26:
                // 退款确认中
                myTitleBarHelper.setMiddleSecondText("取消订单已确认");
                setButtonText("联系客服", false, "已确认", false);
                break;
            case 30:
                //已发货
                if (this.commodityOrderBean.isShowReturnRequest) {
                    currentStateCode = 300;
                    myTitleBarHelper.setMiddleSecondText("取消订单中");
                    setButtonText("联系客服", false, "已发货", false);
                } else {
                    myTitleBarHelper.setMiddleSecondText("已发货");
                    setButtonText("取消订单", false, "确认收货", true);
                }
                break;
            case 40:
                // 已收货
                if (this.commodityOrderBean.isShowReturnRequest) {
                    currentStateCode = 400;
                    myTitleBarHelper.setMiddleSecondText("取消订单中");
                    setButtonText("联系客服", false, "已收货", false);
                } else {
                    myTitleBarHelper.setMiddleSecondText("已收货");
                    ArrayList<CommodityOrderBean.OderGoods> oderGoodses=      commodityOrderBean.goods;
                    if(oderGoodses.size()>1){
                        boolean  mCanEvaluate=false;
                        boolean  mCanAddPic=false;
                        for (int i=0;i<oderGoodses.size();i++){
                            if(oderGoodses.get(i).canEvaluate){
                                mCanEvaluate=true;
                                break;
                            }
                            if(oderGoodses.get(i).canAddPic){
                                mCanAddPic=true;
                            }
                        }
                        if(mCanEvaluate){
                            setButtonText( "取消订单", false, "晒单评价", false);
                        }else if(mCanAddPic){
                            setButtonText("取消订单", false, "追加评价", false);
                        }else{
                            setButtonText("取消订单", false, "查看评价", false);
                        }
                    }else{
                        CommodityOrderBean.OderGoods oderGoods= commodityOrderBean.goods.get(0);
                        if(oderGoods!=null&&oderGoods.canEvaluate){
                            setButtonText( "取消订单", false, "晒单评价", false);
                        }else if(oderGoods!=null&&oderGoods.canAddPic){
                            setButtonText( "取消订单", false, "追加评价", false);
                        }else{
                            setButtonText("取消订单", false, "查看评价", false);
                        }
                    }
//                    setButtonText("取消订单", false, "已收货", false);
                }
                break;
            case 45:
                // 取消订单申请中
                myTitleBarHelper.setMiddleSecondText("退货申请成功");
                setButtonText("联系客服", false, "退货物流", true);
                break;
            case 46:
                // 退货中
                myTitleBarHelper.setMiddleSecondText("退货中");
                setButtonText("联系客服", false, "退货中", false);
                break;
            case 47:
                // 取消订单成功
                // 退货中
                myTitleBarHelper.setMiddleSecondText("取消订单成功");
                setButtonText("联系客服", false, "删除订单", true);
                break;
            case 48:
                // 取消订单失败
                myTitleBarHelper.setMiddleSecondText("取消订单失败");
                setButtonText("联系客服", false, "删除订单", true);
                break;
            case 49:
                // 退货(退款)失败
                break;
            case 50:
                //已完成
                myTitleBarHelper.setMiddleSecondText("交易完成");

                break;
        }
    }

    private void setButtonText(String left, boolean hlightl, String right, boolean hlightr) {
        //1 right 2 left  0 all
        setButtonText(left, right);
        if (hlightl) {
            tv_button_left.setTextColor(getResources().getColor(R.color.white));
            tv_button_left.setBackgroundColor(getResources().getColor(R.color.indicator_selector_color));
        }
        if (hlightr) {
            tv_button_right.setTextColor(getResources().getColor(R.color.white));
            tv_button_right.setBackgroundColor(getResources().getColor(R.color.indicator_selector_color));
        }
    }

    private void setButtonText(String left, String right) {
        //1 right 2 left  0 all
        if (!TextUtils.isEmpty(left)) {
            tv_button_left.setVisibility(View.VISIBLE);
            tv_button_right.setVisibility(View.VISIBLE);
            tv_button_right.setText(right);
            tv_button_left.setText(left);
        } else {
            tv_button_left.setVisibility(View.GONE);
            tv_button_right.setVisibility(View.VISIBLE);
            tv_button_right.setText(right);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ORDERDETAILACTIVITY_JUMP_GOODSRETRUNACTIVITY && resultCode == RESULT_OK) {
            orderDataHelper.getOrderDetailInfor(getNetRequestHelper(OrderDetailActivity.this), orderid);
        }
        if (requestCode == ORDERDETAILACTIVITY_JUMP_PAYALLWAYACTIVITY && resultCode == RESULT_OK) {
            orderDataHelper.getOrderDetailInfor(getNetRequestHelper(OrderDetailActivity.this), orderid);
        }
        if (requestCode == ORDERDETAILACTIVITY_JUMP && resultCode == RESULT_OK) {
            orderDataHelper.getOrderDetailInfor(getNetRequestHelper(OrderDetailActivity.this), orderid);
        }
        if (requestCode == ORDERDETAILACTIVITY_JUMP_WRITEINFOFORGOODSRETURNS && resultCode == RESULT_OK) {
            orderDataHelper.getOrderDetailInfor(getNetRequestHelper(OrderDetailActivity.this), orderid);
        }
        if (requestCode == GoodsEvaluateActivity.EVALUATECODE && resultCode == RESULT_OK) {
            orderDataHelper.getOrderDetailInfor(getNetRequestHelper(OrderDetailActivity.this), orderid);
        }
        if (requestCode == CommodityOrderEvaluteDetailListActivity.REQUESTTOCOMMODITYORDEREVALUTEDETAILLIST&& resultCode == RESULT_OK) {
            orderDataHelper.getOrderDetailInfor(getNetRequestHelper(OrderDetailActivity.this), orderid);
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
