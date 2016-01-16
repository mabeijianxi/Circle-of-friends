package anim.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.asyey.footballlibrary.network.exception.HttpException;
import com.asyey.footballlibrary.network.http.ResponseInfo;
import com.asyey.footballlibrary.universalimageloader.core.DisplayImageOptions;
import com.asyey.footballlibrary.universalimageloader.core.ImageLoader;
import com.henanjianye.soon.communityo2o.BaseActivity;
import com.henanjianye.soon.communityo2o.R;
import com.henanjianye.soon.communityo2o.RecycleView.PullToLoadView;
import com.henanjianye.soon.communityo2o.adapter.AdvanceAdapter;
import com.henanjianye.soon.communityo2o.adapter.Gv_Imags_Adapter;
import com.henanjianye.soon.communityo2o.common.Constant;
import com.henanjianye.soon.communityo2o.common.GoodsCardDataHelper;
import com.henanjianye.soon.communityo2o.common.MyLog;
import com.henanjianye.soon.communityo2o.common.UserDataHelper;
import com.henanjianye.soon.communityo2o.common.enties.AccountBean;
import com.henanjianye.soon.communityo2o.common.enties.AppConsigneeInfor;
import com.henanjianye.soon.communityo2o.common.enties.AppMallOrderResponsBean;
import com.henanjianye.soon.communityo2o.common.enties.BaseDataBean;
import com.henanjianye.soon.communityo2o.common.enties.CommodityBean;
import com.henanjianye.soon.communityo2o.common.enties.CommodityGoodsCartBean;
import com.henanjianye.soon.communityo2o.common.enties.CommoditySettlementAllBeans;
import com.henanjianye.soon.communityo2o.common.enties.CommoditySettlementBean;
import com.henanjianye.soon.communityo2o.common.enties.CommoditySortBean;
import com.henanjianye.soon.communityo2o.common.enties.JsonUtil;
import com.henanjianye.soon.communityo2o.common.enties.PayOrderIdsBean;
import com.henanjianye.soon.communityo2o.common.enties.Tranfee;
import com.henanjianye.soon.communityo2o.common.util.CommonUtils;
import com.henanjianye.soon.communityo2o.common.util.MD5Tool;
import com.henanjianye.soon.communityo2o.common.util.MyTitleBarHelper;
import com.henanjianye.soon.communityo2o.common.util.NetWorkStateUtils;
import com.henanjianye.soon.communityo2o.common.util.SharedPreferencesUtil;
import com.henanjianye.soon.communityo2o.common.util.UserSharedPreferencesUtil;
import com.henanjianye.soon.communityo2o.common.view.DividerItemDecoration;
import com.henanjianye.soon.communityo2o.interf.MyItemClickListener;
import com.henanjianye.soon.communityo2o.interf.NetWorkCallback;
import com.henanjianye.soon.communityo2o.view.SafeKeyboard;
import com.henanjianye.soon.communityo2o.view.UserMenu;
import com.kyleduo.switchbutton.SwitchButton;
import com.umeng.analytics.MobclickAgent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/9/14.
 */
public class CommoditySettlementActivity extends BaseActivity implements MyItemClickListener {
    public static final int ShangchengOrderDetailActTOADRESS = 0x001;
    private static final int PAY_REQUEST_CODE = 22222;
    public static final int SETTLEMENTE_SET_PAY_PSW = 112;
    private PullToLoadView mPullToLoadView;
    private CommoditySettlementOrderAdapter mAdapter;
    private boolean isLoading = false;
    private boolean isHasLoadedAll = false;
    private int nextPage;
    private RecyclerView mRecyclerView;
    private UserMenu mMenu;
    private MyTitleBarHelper myTitleBarHelper;
    //    private MallDataHelper mallDataHelper;
    private int page = 1;
    private int orgId;
    private List<CommoditySortBean> commoditySortBeans;
    private ArrayList<CommodityBean> commodityBeans;
    private View settement_address;
    public static final int SINGLE_ITEM_TYPE = 0x1002;
    public static final int MULTI_ITEM_TYPE = 0x1003;
    private RelativeLayout Ll_order_no_consignees;
    private RelativeLayout Ll_order_consignees;
    private TextView tv_order_consignees_name;
    private TextView tv_order_consignees_phone;
    private TextView tv_order_consignees_address1;
    private TextView tv_order_consignees_address2;
    private TextView tag_totle_buzhong;
    private CheckBox goods_card_check_all;
    private TextView tv_submmit_button;
    private TextView tv_totle_price;
    private float currentTotlePrice;
    private String currentTotleNames = "";
    private float currentTotleTranfee;
    /**
     * 0.表示没有地址， 大于0表示有地址
     */
    private int currentAddressState;
    public static final String CONSIGNEEINFOR = "consigneeinfor";
    public static final String ADDRESS_CREAT = "com.order.address.creat";
    public static final String ADDRESS_UPDATE = "com.order.address.update";
    public static final String COMMODITYSETTLEMENTBEAN1 = "commoditysettlementbean1";
    private AppConsigneeInfor.Address address;
    private AppConsigneeInfor appConsigneeInfor;
    private GoodsCardDataHelper goodsCardDataHelper;
    //    private String currentsubmmitcardids;
    private int getWayState;
    private View settement_discount;
    private TextView tv_num_des;
    private SwitchButton switch_button_discount;
    private ImageView iv_discount_infor;
    private boolean isUseJYCoin;
    private CommoditySettlementAllBeans commoditySettlementAllBeans;
    private float payTotalPrice;
    private String currentPayPaw;
    private float currentUseJYCoin;
    private AlertDialog alertDialog_set_paw;

    @Override
    public int mysetContentView() {
        return R.layout.activity_commodity_settlement;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_commodity_list);
        initView();
        initEvent();
        initProcess();
    }

    private void initView() {
        mPullToLoadView = (PullToLoadView) findViewById(R.id.commodity_settement);
        mPullToLoadView.isRefreshEnabled(false);
        mPullToLoadView.isLoadMoreEnabled(false);
        mRecyclerView = mPullToLoadView.getRecyclerView();
        myTitleBarHelper = new MyTitleBarHelper(this, getWindow().getDecorView().findViewById(android.R.id.content));
        settement_address = LayoutInflater.from(this).inflate(R.layout.settement_address, null, false);
        Ll_order_no_consignees = (RelativeLayout) settement_address.findViewById(R.id.Ll_order_no_consignees);
        Ll_order_no_consignees.setOnClickListener(this);
        Ll_order_consignees = (RelativeLayout) settement_address.findViewById(R.id.Ll_order_consignees);
        Ll_order_consignees.setOnClickListener(this);
        tv_order_consignees_name = (TextView) settement_address.findViewById(R.id.tv_order_consignees_name);
        tv_order_consignees_phone = (TextView) settement_address.findViewById(R.id.tv_order_consignees_phone);
        tv_order_consignees_address1 = (TextView) settement_address.findViewById(R.id.tv_order_consignees_address1);
        tv_order_consignees_address2 = (TextView) settement_address.findViewById(R.id.tv_order_consignees_address2);
        settement_discount = LayoutInflater.from(this).inflate(R.layout.settement_discount, null, false);
        tv_num_des = (TextView) settement_discount.findViewById(R.id.tv_num_des);
        switch_button_discount = (SwitchButton) settement_discount.findViewById(R.id.switch_button_discount);
        iv_discount_infor = (ImageView) settement_discount.findViewById(R.id.iv_discount_infor);
        iv_discount_infor.setOnClickListener(this);
        //合计补充
        tag_totle_buzhong = (TextView) findViewById(R.id.tag_totle_buzhong);
        //全选按钮
        goods_card_check_all = (CheckBox) findViewById(R.id.goods_card_check_all);
        goods_card_check_all.setChecked(true);
        goods_card_check_all.setEnabled(false);
        //付款按钮
        tv_submmit_button = (TextView) findViewById(R.id.tv_submmit_button);
        tv_submmit_button.setOnClickListener(this);
        //总价格
        tv_totle_price = (TextView) findViewById(R.id.tv_totle_price);

    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.iv_title_bar_left:
                finish();
                break;
            case R.id.iv_discount_infor:
                //TODO 跳转至说明页面
                CommonUtils.startWebViewActivity(CommoditySettlementActivity.this, "用户须知", Constant.CommonUrl.JY_COIN_DES);
                break;
            case R.id.tv_submmit_button:
                //TODO 付款
                if (isUseJYCoin && currentPayPaw == null) {
                    createDialogForSubmmit(CommoditySettlementActivity.this);
                } else {
                    submmitForPay(currentUseJYCoin, currentPayPaw);
                }
                break;
            case R.id.Ll_order_no_consignees:
                //TODO 跳转
                toast("无地址");
                Intent intent = new Intent(this, ShangchengOrderAdressAct.class);
                intent.setAction(ADDRESS_CREAT);
                startActivityForResult(intent, ShangchengOrderDetailActTOADRESS);
                break;
            case R.id.Ll_order_consignees:
                if (appConsigneeInfor == null) {
                    appConsigneeInfor = new AppConsigneeInfor();
                }
                //TODO 跳转
                Intent intent2 = new Intent(this,
                        ShangchengOrderAdressAct.class);
                intent2.setAction(ADDRESS_UPDATE);
                intent2.putExtra(CONSIGNEEINFOR, appConsigneeInfor);
                startActivityForResult(intent2,
                        ShangchengOrderDetailActTOADRESS);
                // isConsigneeInfroOk=false;
                break;
        }
    }

    /**
     * 请求是否设置过密码
     */
    private void requestCheckSetStatus() {
        new UserDataHelper(this).getSetPsdStatus(getNetRequestHelper(CommoditySettlementActivity.this));
    }

    private void submmitForPay(float mJyCoin, String pwd) {
        List<CommoditySettlementBean> list = mAdapter.getmList();
        if ((appConsigneeInfor != null && appConsigneeInfor.addressId > 0) || (list.size() > 0) && (list.get(0).address != null) && list.get(0).address.addressId > 0) {
            if (goodsCardDataHelper == null) {
                goodsCardDataHelper = new GoodsCardDataHelper(CommoditySettlementActivity.this);
            }
            goodsCardDataHelper.settlementSubmmit(getNetRequestHelper(CommoditySettlementActivity.this), getSettlementSubmmitParams(), mJyCoin, pwd);
        } else {
            toast("请选择收货地址");
        }
    }

    private AlertDialog dialog;

    private void createDialogForSubmmit(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.dia_psd_input, null);

        dialog = new AlertDialog.Builder(context).setView(view).setTitle("安全验证").create();
        dialog.show();

        Window window = dialog.getWindow();
        window.setContentView(R.layout.dia_psd_input);
        WindowManager.LayoutParams params =
                dialog.getWindow().getAttributes();
        params.height = WindowManager.LayoutParams.FILL_PARENT;
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
                    submmitForPay(currentUseJYCoin, MD5Tool.MD5PsdSet(et_psw_jycion.getText().toString()));
                }
            }
        });
        //忘记密码
        TextView tv_forget_psd = (TextView) dialog.findViewById(R.id.tv_forget_psd);
        tv_forget_psd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO 取消对话框
                dialog.dismiss();
            }
        });
    }

    public void alertDlg() {
        EditText edit = new EditText(this);
        AlertDialog.Builder db = new AlertDialog.Builder(this);
        db.setView(edit);
        SafeKeyboard kb = new SafeKeyboard(edit);
        kb.setViewToBeShownIn(findViewById(android.R.id.content));
        db.create().show();
    }

    private String getSettlementSubmmitParams() {
        String result = "{";
        List<CommoditySettlementBean> list = mAdapter.getmList();
        for (CommoditySettlementBean commoditySettlementBean : list
                ) {
            result = result + "\"" + commoditySettlementBean.storeCartId + "\":{\"goodsCartIds\":\"";
            for (CommodityGoodsCartBean.GoodsCart goodscart : commoditySettlementBean.goodsCartList
                    ) {
                result = result + goodscart.goodsCartId + ",";
            }
            result = result.substring(0, result.length() - 1);
            if (commoditySettlementBean.takeWay == 0) {
                if (appConsigneeInfor != null && appConsigneeInfor.addressId > 0) {
                    result = result + "\",\"transType\":" + "\"express\"" + ",\"addressId\":" + appConsigneeInfor.addressId + ",\"msg\":\"" + commoditySettlementBean.message + "\"";
                } else {
                    result = result + "\",\"transType\":" + "\"express\"" + ",\"addressId\":" + commoditySettlementBean.address.addressId + ",\"msg\":\"" + commoditySettlementBean.message + "\"";
                }
            } else {
                if (appConsigneeInfor != null && appConsigneeInfor.addressId > 0) {
                    result = result + "\",\"transType\":" + "\"self\"" + ",\"addressId\":" + appConsigneeInfor.addressId + ",\"msg\":\"" + commoditySettlementBean.message + "\"";
                } else {
                    result = result + "\",\"transType\":" + "\"self\"" + ",\"addressId\":" + commoditySettlementBean.address.addressId + ",\"msg\":\"" + commoditySettlementBean.message + "\"";
                }
            }
            result = result + "},";
        }
        result = result.substring(0, result.length() - 1) + "" +
                "}";
        return result;
    }

    private void initEvent() {
        Ll_order_no_consignees.setOnClickListener(this);
        Ll_order_consignees.setOnClickListener(this);
        myTitleBarHelper.setOnclickListener(this);
        LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mPullToLoadView.setLayoutManager(manager);
        switch_button_discount.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MyLog.e("AAA", "onCheckedChanged------");
                isUseJYCoin = isChecked;
                resetTotlePrice(false);
            }
        });
        switch_button_discount.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEvent.ACTION_UP == event.getAction()) {

                    //请求接口判断是否已经设置密码
                    if (!switch_button_discount.isChecked()) {
                        requestCheckSetStatus();
                        MyLog.e("AAA", "setOnTouchListener----qq--");
                        return true;
                    }

                }
                return false;

            }
        });
    }

    private void initProcess() {
        appConsigneeInfor = new AppConsigneeInfor();
        orgId = UserSharedPreferencesUtil.getUserInfo(this, UserSharedPreferencesUtil.ORGID, -1);
        myTitleBarHelper.resetState();
        myTitleBarHelper.setMiddleText("结算");
        tv_submmit_button.setText("付款");
        if (orgId == -1) {
            toast("小区id不能为空");
            return;
        }
//        currentsubmmitcardids = getIntent().getStringExtra(CommodityGoodsCardListActivity.CURRENTSUBMMITCARDIDS);
//        if (TextUtils.isEmpty(currentsubmmitcardids)) {
//            toast("商品id空");
//            return;
//        }
        mRecyclerView.addItemDecoration(new DividerItemDecoration(
                this, DividerItemDecoration.HORIZONTAL_LIST));
        mAdapter = new CommoditySettlementOrderAdapter();
        mAdapter.addHeaderView(settement_address);
        mAdapter.addFooterView(settement_discount);
        mAdapter.setOnItemClickListener(this);
        mRecyclerView.setAdapter(mAdapter);
        //处理缓存
        initDate();
    }

    private void initDate() {
        String resultCach = SharedPreferencesUtil.getStringData(this, Constant.CachTag.APP_COMMDITY_GOODS_CART_SUBMMIT, null);
        if (!TextUtils.isEmpty(resultCach)) {
            parseDate(resultCach, true);
        } else {
            toast("数据为空");
        }
    }

    @Override
    protected NetWorkCallback setNetWorkCallback() {
        return new NetWorkCallback() {
            @Override
            public void onStart(String requestTag) {
                isLoading = true;
            }

            @Override
            public void onCancelled(String requestTag) {
                mPullToLoadView.setComplete();
            }

            @Override
            public void onLoading(long total, long current, boolean isUploading, String requestTag) {

            }

            @Override
            public void onSuccess(ResponseInfo<String> responseInfo, String requestTag) {
                mPullToLoadView.setComplete();
//                if (Constant.MallUrl.COMMODITY_CART_SUBMMIT.equals(requestTag)) {
//                    parseDate(responseInfo.result, false);
//                }
                MyLog.e("AAA", "responseInfo.result----" + responseInfo.result);
                if (Constant.Purse.PAYPSDSETSTATUS.equals(requestTag)) {
                    parsePsdSetStatus(responseInfo.result);
                }
                if (Constant.MallUrl.COMMODITY_CART_SETTLEMENT_TRANSFEE.equals(requestTag)) {
                    BaseDataBean<Tranfee> baseBean = JsonUtil.parseDataObject(responseInfo.result, Tranfee.class);
                    if (baseBean.code == 100) {
                        Tranfee tranfee = baseBean.data;
                        currentTotleTranfee = tranfee.feeTotal;
                        tag_totle_buzhong.setText("（含运费" + CommonUtils.floatToTowDecima(tranfee.feeTotal) + "元）");
                        tv_totle_price.setText("¥" + CommonUtils.floatToTowDecima(currentTotlePrice + tranfee.feeTotal));
                    } else {
                        toast(baseBean.msg);
                    }
                }
                if (Constant.MallUrl.COMMODITY_CART_SETTLEMENT_SUBMMIT.equals(requestTag)) {
                    BaseDataBean<PayOrderIdsBean> baseBean = JsonUtil.parseDataObject(responseInfo.result, PayOrderIdsBean.class);
                    if (baseBean.code == 100) {
                        //20时表示已经用积分或者通报付款结束。
                        if (baseBean.data.orderStatus == 20) {
                            Intent intent = new Intent(CommoditySettlementActivity.this, CommodityOrderListActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        } else {
                            AppMallOrderResponsBean appMallOrderResponsBean = new AppMallOrderResponsBean();
                            appMallOrderResponsBean.orderNos = baseBean.data.orderFormIds;
//                        appMallOrderResponsBean.totalPrice = currentTotlePrice + currentTotleTranfee;
                            appMallOrderResponsBean.totalPrice = payTotalPrice;
                            appMallOrderResponsBean.goodsNames = currentTotleNames;
                            int num = SharedPreferencesUtil.getIntData(CommoditySettlementActivity.this, Constant.ShouYeUrl.SHAOPPING_NUM, 0) - (appMallOrderResponsBean.orderNos.split(",").length);
                            SharedPreferencesUtil.saveIntData(CommoditySettlementActivity.this, Constant.ShouYeUrl.SHAOPPING_NUM, num > 0 ? num : 0);
                            //TODO 传值购买商品的值
                            Intent intent = new Intent(CommoditySettlementActivity.this, PayAllWayActivity.class);
                            intent.putExtra(PayAllWayActivity.PAYRESULT_KEY, appMallOrderResponsBean);
                            startActivity(intent);
                        }

                        setResult(RESULT_OK, getIntent());
                        CommoditySettlementActivity.this.finish();
                    } else {
                        toast(baseBean.msg);
                    }
                }

            }

            @Override
            public void onFailure(HttpException error, String msg, String requestTag) {
                mPullToLoadView.post(new Runnable() {
                    @Override
                    public void run() {
                        mPullToLoadView.setComplete();
                    }
                });
                NetWorkStateUtils.errorNetMes(CommoditySettlementActivity.this);
            }
        };
    }

    private void parsePsdSetStatus(String result) {
        try {
            MyLog.e("AAA", "result-----" + result);
            BaseDataBean<AccountBean> json = JsonUtil.parseDataObject(result, AccountBean.class);
            if (json.code == 100) {
                AccountBean aBean = json.data;
                if (aBean != null) {
                    if (aBean.flag) {
                        //已设置过密码
                        if (switch_button_discount != null) {
                            switch_button_discount.setChecked(true);
                        }
                    } else {
                        //还没有设置过密码,显示对话框询问用户设置密码
                        alertDialog_set_paw = CommonUtils.createDialogToAddPsw(CommoditySettlementActivity.this, SETTLEMENTE_SET_PAY_PSW);
                    }
                }
            } else {
                Toast.makeText(this, "数据异常，请稍后重试", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private synchronized void parseDate(String str, boolean isCach) {
        Log.e("AAA", "str--parseDate--" + str);
        if (str == null) {
            currentAddressState = 0;
            setConsigneeView(false, null);
            return;
        }
        Log.e("AAA", "str---" + str);
        BaseDataBean<CommoditySettlementAllBeans> baseBean = JsonUtil.parseDataObject(str, CommoditySettlementAllBeans.class);
        commoditySettlementAllBeans = baseBean.data;
        if (commoditySettlementAllBeans == null) {
            currentAddressState = 0;
            setConsigneeView(false, null);
            return;
        }
        List<CommoditySettlementBean> commoditySettlementBeans = commoditySettlementAllBeans.storeCartList;

        if (commoditySettlementBeans != null && commoditySettlementBeans.size() > 0) {
            currentTotlePrice = 0;
            for (int i = 0; i < commoditySettlementBeans.size(); i++) {
                CommoditySettlementBean samplebean = commoditySettlementBeans.get(i);
                if (i == 0) {
                    address = samplebean.address;
                }
                currentTotlePrice = currentTotlePrice + samplebean.goodsAmountTotal;
                currentTotleTranfee = currentTotleTranfee + samplebean.transfee;
                ArrayList<CommodityGoodsCartBean.GoodsCart> goodsCartList = samplebean.goodsCartList;
                if (goodsCartList != null) {
                    for (CommodityGoodsCartBean.GoodsCart goodscart : goodsCartList
                            ) {
                        currentTotleNames = currentTotleNames + goodscart.goods.name + "X" + goodscart.goodsCount + ",";
                    }
                    currentTotleNames = currentTotleNames.substring(0, currentTotleNames.length() - 1);
                }
            }
            resetTotlePrice(true);

            appConsigneeInfor.transfee = currentTotleTranfee;

            tag_totle_buzhong.setText("（含运费" + CommonUtils.floatToTowDecima(currentTotleTranfee) + "元）");
            if (commoditySettlementAllBeans.canJycoinDeduct) {
                tv_num_des.setText("共" + commoditySettlementAllBeans.accountJycoin + "建业通宝，可抵扣" + commoditySettlementAllBeans.jycoinDeduction + "元");
                switch_button_discount.setVisibility(View.VISIBLE);
                iv_discount_infor.setVisibility(View.GONE);
            } else {
                tv_num_des.setText("共" + commoditySettlementAllBeans.accountJycoin + "建业通宝,本单不可使用");
                switch_button_discount.setVisibility(View.GONE);
                iv_discount_infor.setVisibility(View.VISIBLE);
            }
            Log.e("AAA", "str--address--" + address);
            // 设置地址是否已经存在
            if (address == null || address.addressId == 0) {
                currentAddressState = 0;
                setConsigneeView(false, null);
            } else {
                Log.e("AAA", "str--address--11");
                appConsigneeInfor.setAddress(address);
                currentAddressState = address.addressId;
                setConsigneeView(true, address);
            }
        } else {
            Log.e("AAA", "str--address--22");
            toast(baseBean.msg);
            return;
        }
        //TODO 获取数据   ,设置地址数据，  转换列表数据
        mAdapter.add(commoditySettlementBeans);
    }

    private void resetTotlePrice(boolean isInit) {

        currentUseJYCoin = 0f;
        if (isInit) {
            tv_totle_price.setText("¥" + CommonUtils.floatToTowDecima(currentTotlePrice + currentTotleTranfee));
            payTotalPrice = currentTotlePrice + currentTotleTranfee;
        } else if (isUseJYCoin && commoditySettlementAllBeans != null) {
            Log.e("AAA", currentTotlePrice + "---" + currentTotleTranfee + "----" + commoditySettlementAllBeans.jycoinDeduction);
            tv_totle_price.setText("¥" + CommonUtils.floatToTowDecima(currentTotlePrice + currentTotleTranfee - commoditySettlementAllBeans.jycoinDeduction));
            currentUseJYCoin = commoditySettlementAllBeans.jycoinDeduction;
            payTotalPrice = currentTotlePrice + currentTotleTranfee - commoditySettlementAllBeans.jycoinDeduction;
        } else if (!isUseJYCoin && commoditySettlementAllBeans != null) {
            Log.e("AAA", currentTotlePrice + "---" + currentTotleTranfee + "-ee---" + commoditySettlementAllBeans.jycoinDeduction);
            tv_totle_price.setText("¥" + CommonUtils.floatToTowDecima(currentTotlePrice + currentTotleTranfee));
            payTotalPrice = currentTotlePrice + currentTotleTranfee;

        }
    }

    @Override
    public void onItemClick(View view, int postion) {
        //TODO 点击事件

    }

    private class CommoditySettlementOrderAdapter extends AdvanceAdapter<RecyclerView.ViewHolder> {
        private List<CommoditySettlementBean> mList;
        private MyItemClickListener myItemClickListener;


        public CommoditySettlementOrderAdapter() {
            mList = new ArrayList<>();
        }

        public List<CommoditySettlementBean> getmList() {
            return mList;
        }

        /**
         * 设置Item点击监听
         *
         * @param listener
         */
        public void setOnItemClickListener(MyItemClickListener listener) {
            this.myItemClickListener = listener;
        }

        public void add(List<CommoditySettlementBean> beans) {
            mList.clear();
            mList.addAll(beans);
            notifyDataSetChanged();
        }

        public void clear() {
            mList.clear();
            notifyDataSetChanged();
        }

        @Override
        public int getAdvanceViewType(int position) {
            CommoditySettlementBean commoditySettlementBean = mList.get(position);
            if (commoditySettlementBean.goodsCartList.size() > 1) {
                return MULTI_ITEM_TYPE;
            } else {
                return SINGLE_ITEM_TYPE;
            }
        }

        @Override
        protected int getAdvanceCount() {
            return mList.size();
        }

        @Override
        protected void onBindAdvanceViewHolder(RecyclerView.ViewHolder holder, int i) {
            CommoditySettlementBean commoditySettlementBean = getmList().get(i);
            if (holder instanceof CellHolder) {
                CellHolder cellHolder = (CellHolder) holder;
                cellHolder.iv_sg_commodity_settement_title_tag.setText("订单" + (i + 1));
                cellHolder.iv_sg_commodity_settement_title.setText(commoditySettlementBean.store.storeName);
                cellHolder.tv_sg_num.setText("X" + commoditySettlementBean.goodsCartList.get(0).goodsCount);
                cellHolder.tv_sg_price.setText("¥" + CommonUtils.floatToTowDecima(commoditySettlementBean.goodsCartList.get(0).goodsPrice));
                cellHolder.tv_sg_title.setText(commoditySettlementBean.goodsCartList.get(0).goods.name);
                if (commoditySettlementBean.takeWay == 1) {
                    cellHolder.tv_commodity_settement_pay_way.setText("自取" + " " + "线上支付");
                } else {
                    cellHolder.tv_commodity_settement_pay_way.setText("送货上门" + " " + "线上支付");
                }
                cellHolder.ll_commodity_settement_pay.setTag(i);
                cellHolder.ll_commodity_settement_pay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int pos = Integer.parseInt(v.getTag().toString());
                        // 跳转至相关修改界面
//                        toast("点击支付方式页面");
                        getWayState = mAdapter.getmList().get(pos).takeWay;
                        Intent intent = new Intent(CommoditySettlementActivity.this, CommodityChoosePayWayListActivity.class);
                        intent.putExtra("getWayState", getWayState);
                        intent.putExtra("isPickup", mAdapter.getmList().get(pos).isPickup);
                        intent.putExtra("pos", pos);
                        startActivityForResult(intent, PAY_REQUEST_CODE);
                    }
                });
                cellHolder.et_sg_commodity_settement_message.setTag(i);
                ImageLoader instance = ImageLoader.getInstance();
                DisplayImageOptions build = new DisplayImageOptions.Builder()
                        .showImageOnFail(R.drawable.ic_launcher_default)
                        .showImageForEmptyUri(R.drawable.ic_launcher_default).build();
                instance.displayImage(commoditySettlementBean.goodsCartList.get(0).goods.mainPhoto.smallPicUrl, ((CellHolder) holder).iv_sg_img, build);
                if (commoditySettlementBean.message != null) {
                    cellHolder.et_sg_commodity_settement_message.setText(commoditySettlementBean.message);
                }
                cellHolder.et_sg_commodity_settement_message.addTextChangedListener(new MTextWatcher(i));
            } else {
                CellHolderMulti cellHolderMulti = (CellHolderMulti) holder;
                cellHolderMulti.iv_commodity_settement_title_tag.setText("订单" + (i + 1));
                cellHolderMulti.iv_commodity_settement_title.setText(commoditySettlementBean.store.storeName);
                if (commoditySettlementBean.takeWay == 1) {
                    cellHolderMulti.tv_commodity_settement_pay_way.setText("自取" + " " + "线上支付");
                } else {
                    cellHolderMulti.tv_commodity_settement_pay_way.setText("送货上门" + " " + "线上支付");
                }
                cellHolderMulti.rl_commodity_settement_pay.setTag(i);
                cellHolderMulti.rl_commodity_settement_pay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int pos = Integer.parseInt(v.getTag().toString());
                        //TODO 跳转至相关修改界面

                        //TODO 跳转至相关修改界面
                        toast("点击支付方式页面");
                        getWayState = mAdapter.getmList().get(pos).takeWay;
                        Intent intent = new Intent(CommoditySettlementActivity.this, CommodityChoosePayWayListActivity.class);
                        intent.putExtra("getWayState", getWayState);
                        intent.putExtra("pos", pos);
                        startActivityForResult(intent, PAY_REQUEST_CODE);
                    }
                });
                cellHolderMulti.et_commodity_settement_message.setTag(i);
                if (commoditySettlementBean.message != null) {
                    cellHolderMulti.et_commodity_settement_message.setText(commoditySettlementBean.message);
                }
                cellHolderMulti.et_commodity_settement_message.addTextChangedListener(new MTextWatcher(i));
                //TODO 展示多张图片 ，点击跳转
                cellHolderMulti.ll_commodity_settement_imags.setTag(i);
                cellHolderMulti.ll_commodity_settement_imags.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int pos = Integer.parseInt(v.getTag().toString());
                        //TODO 跳转至详细订单列表
                        if (getmList().size() > pos) {
                            CommoditySettlementBean commoditySettlementBean1 = getmList().get(pos);
                            Intent intent = new Intent(CommoditySettlementActivity.this, CommoditySettlementDetailListActivity.class);
                            intent.putExtra(COMMODITYSETTLEMENTBEAN1, commoditySettlementBean1);
                            startActivity(intent);
                        }
                    }
                });
                //TODO 设置gridadater  数据
                Gv_Imags_Adapter gv_imags_adapter = new Gv_Imags_Adapter(CommoditySettlementActivity.this);
                gv_imags_adapter.setData(commoditySettlementBean.goodsCartList);
                cellHolderMulti.gv_commodity_settement_imag.setAdapter(gv_imags_adapter);
            }
        }

        @Override
        protected RecyclerView.ViewHolder onCreateAdvanceViewHolder(ViewGroup parent, int viewType) {
            if (viewType == MULTI_ITEM_TYPE) {
                return new CellHolderMulti(LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_commodity_settement_multi_item, parent, false), myItemClickListener);
            } else {
                return new CellHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_commodity_settement_item, parent, false), myItemClickListener);
            }
        }
    }

    private class CellHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final MyItemClickListener myItemClickListener;
        public final ImageView iv_sg_img;
        public final TextView iv_sg_commodity_settement_title_tag;
        public final TextView iv_sg_commodity_settement_title;
        public final TextView tv_sg_title;
        public final TextView tv_sg_price;
        public final TextView tv_sg_num;
        public final TextView tv_commodity_settement_pay_way;
        public final EditText et_sg_commodity_settement_message;
        public final RelativeLayout ll_commodity_settement_pay;
        public final RelativeLayout ll_sg_commodity_settement_imags;

        public CellHolder(View itemView, MyItemClickListener myItemClickListener) {
            super(itemView);
            this.myItemClickListener = myItemClickListener;
            itemView.setOnClickListener(this);
            iv_sg_img = (ImageView) itemView.findViewById(R.id.iv_sg_img);
            iv_sg_commodity_settement_title_tag = (TextView) itemView.findViewById(R.id.iv_sg_commodity_settement_title_tag);
            iv_sg_commodity_settement_title = (TextView) itemView.findViewById(R.id.iv_sg_commodity_settement_title);
            tv_sg_title = (TextView) itemView.findViewById(R.id.tv_sg_title);
            tv_sg_price = (TextView) itemView.findViewById(R.id.tv_sg_price);
            tv_sg_num = (TextView) itemView.findViewById(R.id.tv_sg_num);
            //支付方式选择
            ll_commodity_settement_pay = (RelativeLayout) itemView.findViewById(R.id.ll_commodity_settement_pay);
            ll_sg_commodity_settement_imags = (RelativeLayout) itemView.findViewById(R.id.ll_sg_commodity_settement_imags);
            tv_commodity_settement_pay_way = (TextView) itemView.findViewById(R.id.tv_commodity_settement_pay_way);
            et_sg_commodity_settement_message = (EditText) itemView.findViewById(R.id.et_sg_commodity_settement_message);
        }

        @Override
        public void onClick(View v) {
            myItemClickListener.onItemClick(v, getAdapterPosition());
        }
    }

    private class CellHolderMulti extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final MyItemClickListener myItemClickListener;
        public final TextView iv_commodity_settement_title_tag;
        public final TextView iv_commodity_settement_title;
        public final RelativeLayout rl_commodity_settement_pay;
        public final TextView tv_commodity_settement_pay_way;
        public final TextView et_commodity_settement_message;
        public final GridView gv_commodity_settement_imag;
        public final LinearLayout ll_commodity_settement_imags;


        public CellHolderMulti(View itemView, MyItemClickListener myItemClickListener) {
            super(itemView);
            this.myItemClickListener = myItemClickListener;
            itemView.setOnClickListener(this);
            iv_commodity_settement_title_tag = (TextView) itemView.findViewById(R.id.iv_commodity_settement_title_tag);
            iv_commodity_settement_title = (TextView) itemView.findViewById(R.id.iv_commodity_settement_title);
            //支付方式选择
            rl_commodity_settement_pay = (RelativeLayout) itemView.findViewById(R.id.rl_commodity_settement_pay);
            tv_commodity_settement_pay_way = (TextView) itemView.findViewById(R.id.tv_commodity_settement_pay_way);
            et_commodity_settement_message = (TextView) itemView.findViewById(R.id.et_commodity_settement_message);

            gv_commodity_settement_imag = (GridView) itemView.findViewById(R.id.gv_commodity_settement_imag);
            gv_commodity_settement_imag.setPressed(false);
            gv_commodity_settement_imag.setEnabled(false);
            gv_commodity_settement_imag.setClickable(false);
            ll_commodity_settement_imags = (LinearLayout) itemView.findViewById(R.id.ll_commodity_settement_imags);
        }

        @Override
        public void onClick(View v) {
            myItemClickListener.onItemClick(v, getAdapterPosition());
        }
    }

    class MTextWatcher implements TextWatcher {
        private final int pos;

        public MTextWatcher(int pos) {
            this.pos = pos;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (mAdapter.getmList().size() > pos) {
                mAdapter.getmList().get(pos).message = s.toString();
            }
            //
        }
    }

    @Override
    protected void onActivityResult(int arg0, int arg1, Intent arg2) {
        if (arg0 == ShangchengOrderDetailActTOADRESS
                && arg1 == ShangchengOrderAdressAct.RESULTCODEOK) {
            Serializable serializableExtra = arg2
                    .getSerializableExtra(ShangchengOrderAdressAct.ConsigneeINFORADRESS);
            if (serializableExtra instanceof AppConsigneeInfor) {
                appConsigneeInfor = (AppConsigneeInfor) serializableExtra;
                if (appConsigneeInfor != null) {
                    setConsigneeView(true, appConsigneeInfor.getAddress());
                }
            } else {
                toast("收货地址有误");
            }
            // appConsigneeInfor = new AppConsigneeInfor();
        } else if (arg0 == PAY_REQUEST_CODE && arg1 == RESULT_OK) {
            int getWayState = arg2.getIntExtra("getWayState", -1);
            int pos = arg2.getIntExtra("pos", -1);
            if (pos != -1 && getWayState != -1) {
                mAdapter.getmList().get(pos).takeWay = getWayState;
                mAdapter.notifyDataSetChanged();
                requestSettlementDate();
            } else {
                toast("送货方式选择有误" + getWayState + "--" + pos);
            }
            // 处理返回的支付方式
        } else if (arg0 == SETTLEMENTE_SET_PAY_PSW && arg1 == RESULT_OK) {
            toast("密码设置成功");
            currentPayPaw = arg2.getStringExtra(PursePsdManagerActivity.PSDMD5);
            if (switch_button_discount != null) {
                switch_button_discount.setChecked(true);
            }
            if (alertDialog_set_paw != null) {
                alertDialog_set_paw.dismiss();
            }

        }

    }

    private void requestSettlementDate() {
        if (appConsigneeInfor == null || appConsigneeInfor.getAddress() == null || appConsigneeInfor.getAddress().addressId == -1) {
            toast("请选择收货地址");
            return;
        }
//        if (!TextUtils.isEmpty(currentsubmmitcardids)) {
        if (goodsCardDataHelper == null) {
            goodsCardDataHelper = new GoodsCardDataHelper(CommoditySettlementActivity.this);
        }

        String params = getTranfeeParameters();
        goodsCardDataHelper.getTransfee(getNetRequestHelper(CommoditySettlementActivity.this), params);
//        } else {
//            toast("请选择需要结算的订单");
//        }
    }

    private String getTranfeeParameters() {
        String result = "";
        if (mAdapter != null) {
            List<CommoditySettlementBean> list = mAdapter.getmList();
            for (int i = 0; i < list.size(); i++) {
                CommoditySettlementBean commoditySettlementBean = list.get(i);
                result = result + commoditySettlementBean.storeCartId + "_";
                ArrayList<CommodityGoodsCartBean.GoodsCart> goodsCartList = commoditySettlementBean.goodsCartList;
                for (CommodityGoodsCartBean.GoodsCart goodcart : goodsCartList) {
                    result = result + goodcart.goodsCartId + ",";
                }

                if (commoditySettlementBean.takeWay == 0) {

                    result = result.substring(0, result.length() - 1) + "_" + "express" + "_" + appConsigneeInfor.getAddress().addressId + "|";
                } else {
                    result = result.substring(0, result.length() - 1) + "_" + "self" + "_" + appConsigneeInfor.getAddress().addressId + "|";

                }
            }
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    private void setConsigneeView(boolean b, AppConsigneeInfor.Address address) {
        //TODO 设置地址是否已经存在
        if (!b) {
            currentAddressState = 0;
            Ll_order_no_consignees.setVisibility(View.VISIBLE);
            Ll_order_consignees.setVisibility(View.GONE);
        } else {
            appConsigneeInfor.setAddress(address);
            currentAddressState = address.addressId;
            Ll_order_no_consignees.setVisibility(View.GONE);
            Ll_order_consignees.setVisibility(View.VISIBLE);
            tv_order_consignees_phone.setText(address.mobile);
            tv_order_consignees_name.setText(address.receiverName);
            tv_order_consignees_address1.setText(address.provinceName + " " + address.cityName + " " + address.countyName);
            tv_order_consignees_address2.setText(address.detailAddress);
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
