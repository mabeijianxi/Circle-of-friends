package anim.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.asyey.footballlibrary.network.exception.HttpException;
import com.asyey.footballlibrary.network.http.ResponseInfo;
import com.henanjianye.soon.communityo2o.BaseActivity;
import com.henanjianye.soon.communityo2o.MainActivity;
import com.henanjianye.soon.communityo2o.R;
import com.henanjianye.soon.communityo2o.adapter.CommodityPropsAdapter;
import com.henanjianye.soon.communityo2o.common.Constant;
import com.henanjianye.soon.communityo2o.common.GoodsCardDataHelper;
import com.henanjianye.soon.communityo2o.common.MyLog;
import com.henanjianye.soon.communityo2o.common.enties.AppGoodsChooseInfor;
import com.henanjianye.soon.communityo2o.common.enties.BaseBean;
import com.henanjianye.soon.communityo2o.common.enties.BaseDataBean;
import com.henanjianye.soon.communityo2o.common.enties.CommodityBean;
import com.henanjianye.soon.communityo2o.common.enties.CommodityGoodsCartBean;
import com.henanjianye.soon.communityo2o.common.enties.GoodDetailBean;
import com.henanjianye.soon.communityo2o.common.enties.JsonUtil;
import com.henanjianye.soon.communityo2o.common.util.CommonUtils;
import com.henanjianye.soon.communityo2o.common.util.FastClick;
import com.henanjianye.soon.communityo2o.common.util.MyTitleBarHelper;
import com.henanjianye.soon.communityo2o.common.util.NetWorkStateUtils;
import com.henanjianye.soon.communityo2o.common.util.ShareSDKConfigUtil;
import com.henanjianye.soon.communityo2o.common.util.SharedPreferencesUtil;
import com.henanjianye.soon.communityo2o.common.util.UmShareUtils;
import com.henanjianye.soon.communityo2o.common.util.UserSharedPreferencesUtil;
import com.henanjianye.soon.communityo2o.common.view.BadgeView;
import com.henanjianye.soon.communityo2o.common.view.CustomProgressDialog;
import com.henanjianye.soon.communityo2o.common.view.ProgressWebView;
import com.henanjianye.soon.communityo2o.interf.NetWorkCallback;
import com.henanjianye.soon.communityo2o.view.MyLayoutManager;
import com.umeng.analytics.MobclickAgent;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cn.sharesdk.framework.Platform;

/**
 * Created by Administrator on 2015/9/14.
 */
public class CommodityDetailInforActivity extends BaseActivity {

    private LinearLayout ll_refresh;
    private Button bt_refresh;
    private ProgressWebView iv_goods_details;
    private TextView detail_for_help;
    private TextView detail_add_card;
    private TextView detail_buy_now;

    private String goodsDetailUrl;
    private CustomProgressDialog customProgressDialog;
    private MyTitleBarHelper myTitleBarHelper;
    private RelativeLayout rl_goods_props;
    private ImageView iv_zhezhao;
    //    private PullToLoadView pull_props_list;
    private Button bt_buy;
    private RecyclerView mRecyclerView;
    private GoodsCardDataHelper goodsCardDataHelper;
    private TextView tv_props_name_two;
    private TextView tv_props_pre_price_two;
    private TextView tv_props_detail_store;
    private TextView tv_props_detail_store_num;
    private TextView tv_props_goods_num_minus;
    private TextView et_props_goods_num;
    private TextView tv_props_goods_num_plus;
    private CommodityPropsAdapter commodityPropsAdapter;
    private GoodDetailBean appGoodInfor;
    private int goodsDefaultNum = 1;
    private AppGoodsChooseInfor appGoodsChooseInfor;
    private GoodDetailBean.ItemInventory rightInventory;
    private boolean isAddCard;
    private int currentGoodsId;
    private View headerView;
    private View footView;
    private BadgeView goodsCard;
    private boolean isNoSupportBuy;
    ImageView iv_title_bar_right2;
    private String sharepic;//分享图片
    private String sharetitle;//分享标题
    private String shareurl;//分享url
    android.os.Handler Handler =new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            toast("分享成功");
        }
    };
    @Override
    public int mysetContentView() {
        return R.layout.activity_commodity_detail2;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initEvent();
        initProcess();
    }

    private boolean isNotice = false;

    private void press_back() {
        if (isNotice && SharedPreferencesUtil.getStringData(this, Constant.ShouYeUrl.APP_STATUS, "0").equals("1")) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            this.finish();
        }
    }

    @Override
    public void onBackPressed() {
        press_back();
        super.onBackPressed();
    }

    private void initView() {
        ll_refresh = (LinearLayout) findViewById(R.id.ll_refresh);
        bt_refresh = (Button) findViewById(R.id.bt_refresh);
        iv_goods_details = (ProgressWebView) findViewById(R.id.iv_goods_details);
        detail_for_help = (TextView) findViewById(R.id.detail_for_help);
        detail_add_card = (TextView) findViewById(R.id.detail_add_card);
        detail_buy_now = (TextView) findViewById(R.id.detail_buy_now);
        customProgressDialog = CustomProgressDialog.createDialog(this);
        myTitleBarHelper = new MyTitleBarHelper(this, getWindow().getDecorView().findViewById(android.R.id.content));
//        为选择属性的布局
        rl_goods_props = (RelativeLayout) findViewById(R.id.rl_goods_props);
        iv_zhezhao = (ImageView) findViewById(R.id.iv_zhezhao);
//        pull_props_list = (PullToLoadView) findViewById(R.id.pull_props_list);
//        mRecyclerView = pull_props_list.getRecyclerView();
        mRecyclerView = (RecyclerView) findViewById(R.id.pull_props_list);
        MyLayoutManager manager = new MyLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(manager);

        bt_buy = (Button) findViewById(R.id.bt_buy);
        headerView = LayoutInflater.from(this).inflate(R.layout.commodity_goods_detail_header, null, false);
        footView = LayoutInflater.from(this).inflate(R.layout.commodity_goods_detail_last, null, false);
        //初始化头尾布局
        tv_props_name_two = (TextView) headerView.findViewById(R.id.tv_props_name_two);
        tv_props_pre_price_two = (TextView) headerView.findViewById(R.id.tv_props_pre_price_two);
        tv_props_detail_store = (TextView) footView.findViewById(R.id.tv_props_detail_store);
        tv_props_detail_store_num = (TextView) footView.findViewById(R.id.tv_props_detail_store_num);
        tv_props_goods_num_minus = (TextView) footView.findViewById(R.id.tv_props_goods_num_minus);
        tv_props_goods_num_minus.setOnClickListener(this);
        et_props_goods_num = (TextView) footView.findViewById(R.id.et_props_goods_num);
        tv_props_goods_num_plus = (TextView) footView.findViewById(R.id.tv_props_goods_num_plus);
        tv_props_goods_num_plus.setOnClickListener(this);

        iv_title_bar_right2 = (ImageView) findViewById(R.id.iv_title_bar_right2);
        iv_title_bar_right2.setImageResource(R.mipmap.event_share);
    }

    private void initEvent() {
        bt_refresh.setOnClickListener(this);
        detail_for_help.setOnClickListener(this);
        detail_add_card.setOnClickListener(this);
        detail_buy_now.setOnClickListener(this);
        iv_zhezhao.setOnClickListener(this);
        bt_buy.setOnClickListener(this);
        iv_title_bar_right2.setOnClickListener(this);
    }

    private void initProcess() {
        Intent intent = getIntent();
        isNotice = intent.getBooleanExtra("onlineFlag", false);
        String action = intent.getAction();
        if (CommodityGoodsCardListActivity.COMMODITY_GOODSCARD_LIST_ACTIVITY.equals(action)) {
            Serializable serializable = intent.getSerializableExtra("CommodityGoodsCartBean.GoodsCart.Goods");
            if (serializable == null || !(serializable instanceof CommodityGoodsCartBean.GoodsCart.Goods)) {
                toast("链接信息有误");
                return;
            }
            CommodityGoodsCartBean.GoodsCart.Goods goods = (CommodityGoodsCartBean.GoodsCart.Goods) serializable;
            appGoodsChooseInfor = new AppGoodsChooseInfor();
            appGoodsChooseInfor.setGoodsId(goods.goodsId);
            appGoodsChooseInfor.storeTelephone = goods.storeTelephone;
            myTitleBarHelper.setMiddleText(goods.name);
            tv_props_name_two.setText(goods.name);
            tv_props_pre_price_two.setText("¥" + goods.currentPriceShow);
            goodsDetailUrl = goods.url;
            currentGoodsId = goods.goodsId;
           try{
               sharepic=goods.mainPhoto.smallPicUrl;
           }catch (Exception e){
                e.printStackTrace();
           }

             sharetitle=goods.name;
             shareurl=goods.url;


        } else {
            Serializable serializable = intent.getSerializableExtra("CommodityBean");
            if (serializable == null || !(serializable instanceof CommodityBean)) {
                toast("链接信息有误");
                return;
            }
            CommodityBean commodityBean = (CommodityBean) serializable;
            appGoodsChooseInfor = new AppGoodsChooseInfor();
            appGoodsChooseInfor.setGoodsId(commodityBean.goodsId);
            appGoodsChooseInfor.storeTelephone = commodityBean.storeTelephone;
            myTitleBarHelper.setMiddleText(commodityBean.name);
            tv_props_name_two.setText(commodityBean.name);
            tv_props_pre_price_two.setText("¥" + commodityBean.currentPriceShow);
            goodsDetailUrl = commodityBean.url;
            currentGoodsId = commodityBean.goodsId;
            appGoodsChooseInfor.storeTelephone = commodityBean.storeTelephone;
            shareurl=commodityBean.url;
            sharetitle=commodityBean.name;
            try {
                sharepic=commodityBean.mainPhoto.smallPicUrl;
            }catch (Exception e){
                e.printStackTrace();
            }


        }
        //暂时用购物代替
        myTitleBarHelper.setRightImag(R.drawable.title_commodity_goodscard);
        iv_goods_details.getSettings().setJavaScriptEnabled(true);

        iv_goods_details.setWebViewClient(new MyWebViewClient());
//        MyHander myHander = new MyHander();
//        iv_goods_details.setHandler(myHander);
        iv_goods_details.loadUrl(goodsDetailUrl);
        iv_goods_details.addJavascriptInterface(this, "commodityDetail");
        commodityPropsAdapter = new CommodityPropsAdapter(this, appGoodsChooseInfor);
        commodityPropsAdapter.addHeaderView(headerView);
        commodityPropsAdapter.addFooterView(footView);
        mRecyclerView.setAdapter(commodityPropsAdapter);
        goodsCardDataHelper = new GoodsCardDataHelper(this);
        myTitleBarHelper.setOnclickListener(this);
        initPropsData();

    }
//    @JavascriptInterface
//    public void gotoEvaluteActivity(int goodsId){
//        //  跳转至评论详情页面
//        EaluationActivity.goEalutionActivity(CommodityDetailInforActivity.this,goodsId);
//    }

    private void initPropsData() {
        goodsCardDataHelper.goodsProps(getNetRequestHelper(this).isShowProgressDialog(true), currentGoodsId);
    }


    @Override
    public void onClick(View v) {
        super.onClick(v);


        switch (v.getId()) {
            case R.id.bt_refresh:
                if (CommonUtils.isNetworkConnected(this)) {
                    iv_goods_details.reload();
                }
                break;
            case R.id.detail_for_help:
                if (!TextUtils.isEmpty(appGoodsChooseInfor.storeTelephone)) {
                    CommonUtils.showPhoneDialog(CommodityDetailInforActivity.this, appGoodsChooseInfor.storeTelephone);
                } else {
                    toast("请咨询当地服务站");
                }
                break;
            case R.id.detail_add_card:
                if (isNoSupportBuy) {
                    toast("商品不支持购买");
                    return;
                }
                if (UserSharedPreferencesUtil.getUserLoginState(CommodityDetailInforActivity.this) < 2) {
                    CommonUtils.startLoginActivity(CommodityDetailInforActivity.this);
                }
                isAddCard = true;

                rl_goods_props.setVisibility(View.VISIBLE);
                break;
            case R.id.detail_buy_now:
                if (isNoSupportBuy) {
                    toast("商品不支持购买");
                    return;
                }
                rl_goods_props.setVisibility(View.VISIBLE);
                break;
            case R.id.iv_title_bar_left:
                press_back();
                break;
            case R.id.iv_title_bar_right:
                CommonUtils.startGoodsCartActivity(CommodityDetailInforActivity.this);
//                startActivity(new Intent(CommodityDetailInforActivity.this, CommodityGoodsCardListActivity.class));
                break;
            case R.id.iv_zhezhao:
                if (isNoSupportBuy) {
                    toast("商品不支持购买");
                    return;
                }
                isAddCard = false;
                rl_goods_props.setVisibility(View.GONE);
                break;
            case R.id.bt_buy:
                if (isNoSupportBuy) {
                    toast("商品不支持购买");
                    return;
                }
                if (appGoodInfor == null || appGoodInfor.params == null || appGoodsChooseInfor == null || appGoodsChooseInfor.getProps() == null) {
                    return;
                }
                if (UserSharedPreferencesUtil.getUserLoginState(CommodityDetailInforActivity.this) < 2) {
                    CommonUtils.startLoginActivity(CommodityDetailInforActivity.this);
                    return;
                }

                if (appGoodInfor.params.size() > appGoodsChooseInfor.getProps()
                        .size()) {
                    toast("请选择相应商品属性");
                    break;
                }
                if (appGoodsChooseInfor.getCount() < 1) {
                    toast("购买数量为0");
                    break;
                }
                if (rightInventory != null
                        && Integer.parseInt(rightInventory.count) == 0) {
                    toast("亲，您选择的商品已售罄");
                    break;
                }
                if (rightInventory == null && appGoodInfor.inventory == 0) {
                    toast("亲，您选择的商品已售罄");
                    break;
                }
                //跳转到订单页面
                startCommodityOrderOrGoodCard(isAddCard);
                rl_goods_props.setVisibility(View.GONE);
                isAddCard = false;
                break;
            case R.id.tv_props_goods_num_minus:
                if (isNoSupportBuy) {
                    toast("商品不支持购买");
                    return;
                }
                mSetGoodsNumEdit(false);
                break;
            case R.id.tv_props_goods_num_plus:
                if (isNoSupportBuy) {
                    toast("商品不支持购买");
                    return;
                }
                mSetGoodsNumEdit(true);
                break;
            case R.id.iv_title_bar_right2:
                if(!FastClick.isFastClick()){


                UmShareUtils.getInstance(CommodityDetailInforActivity.this).shareTextAndImage(sharepic,"一家有好东东，等你领回家！",sharetitle,shareurl,new ShareSDKConfigUtil.MPlatformActionListener(){

                    @Override
                    public void onComplete(Platform platform, int action, HashMap<String, Object> res) {

                        Handler.sendEmptyMessage(0);
                    }

                    @Override
                    public void onCancel(Platform platform, int arg1) {
                        toast("取消分享");
                    }

                    @Override
                    public void onError(Platform platform, int arg1, Throwable arg2) {
                        toast("分享失败");
                    }
                });
                }
                break;
        }
    }

    /**
     * @param isGoodcard 跳转到订单页面或者加入购物车
     */
    private void startCommodityOrderOrGoodCard(boolean isGoodcard) {
        if (appGoodsChooseInfor != null
                && appGoodsChooseInfor.getProps().size() == appGoodInfor.params
                .size()) {
            if (isGoodcard) {
                goodsCardDataHelper.addGoods
                        (getNetRequestHelper(this), appGoodsChooseInfor.getGoodsId(), appGoodsChooseInfor.getCount(),
                                CommonUtils.propsToString(appGoodsChooseInfor));
            } else {
                //TODO 跳转到订单
                String param = appGoodsChooseInfor.getGoodsId() + "," + CommonUtils.propsToString(appGoodsChooseInfor) + "," + appGoodsChooseInfor.getCount();
                goodsCardDataHelper.settlementBuyNow(getNetRequestHelper(this), param);
//                Intent intent = new Intent(this, ShangchengOrderDetailAct.class);
//            intent.putExtra(TOShangChengOrderFlag, appGoodsChooseInfor);
//            startActivity(intent);
            }
//
        }
    }

    private class MyWebViewClient extends WebViewClient {
        boolean isError = false;
        boolean isFinish=false;
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            // pb.setVisibility(View.GONE);
            if (!isError) {
                isFinish=true;
                ll_refresh.setVisibility(View.GONE);
                iv_goods_details.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            MyLog.e("AAA", "request---" + url);
            //http://123.57.162.168:8081/mall/app/goods/evaluation.htm?goodsId=98576
            if (url.contains("goodsId=")&&isFinish) {
                String goodsId = url.substring(url.indexOf("=")+1, url.length());
                MyLog.e("AAA", "goodsId---" + goodsId);
                //  跳转至评论详情页面
                try {
                    EaluationActivity.goEalutionActivity(CommodityDetailInforActivity.this, Integer.parseInt(goodsId));
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            return super.shouldInterceptRequest(view, url);
        }
//        @Override
//        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
//
//            MyLog.e("AAA","request---"+request.getUrl());
//            return super.shouldInterceptRequest(view, request);
//        }

        @Override
        public void onReceivedError(WebView view, int errorCode,
                                    String description, String failingUrl) {
            isError = true;
            iv_goods_details.setVisibility(View.GONE);
            ll_refresh.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            isError = false;
            // pb.setVisibility(View.VISIBLE);
            ll_refresh.setVisibility(View.GONE);
        }
    }

    class MyHander extends android.os.Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constant.StateInt.DIALOG_STATE_CANCLE:
                    if (customProgressDialog != null) {
//                        customProgressDialog.dismiss();
                    }
                    break;
                case Constant.StateInt.DIALOG_STATE_SHOW:
                    if (customProgressDialog != null && !customProgressDialog.isShowing()) {
                        customProgressDialog.show();
                    }
                    break;
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
                if (Constant.MallUrl.COMMODITY_PROPS.equals(requestTag)) {
                    BaseDataBean<GoodDetailBean> baseDataBean = JsonUtil.parseDataObject(responseInfo.result, GoodDetailBean.class);
                    appGoodInfor = baseDataBean.data;
                    if (baseDataBean.code == 100) {
                        commodityPropsAdapter.add(appGoodInfor.params);
                        mInitGoodsNumEdit(et_props_goods_num);
                    } else {
                        isNoSupportBuy = true;
                        toast(baseDataBean.msg);
                    }
                } else if (Constant.MallUrl.COMMODITY_CART_ADD.equals(requestTag)) {
                    BaseBean<Object> baseBean = JsonUtil.jsonArray(responseInfo.result, Object.class);
                    if (baseBean.code == 100) {
                        int num = SharedPreferencesUtil.getIntData(CommodityDetailInforActivity.this, Constant.ShouYeUrl.SHAOPPING_NUM, 0) + 1;
                        CommonUtils.setRedDotNum(goodsCard, num);
                        SharedPreferencesUtil.saveIntData(CommodityDetailInforActivity.this, Constant.ShouYeUrl.SHAOPPING_NUM, num);
                        toast(baseBean.msg);
                    } else {
                        toast(baseBean.msg);
                    }
//                    toast(responseInfo.result);
                } else if (Constant.MallUrl.COMMODITY_BUY_NOW.equals(requestTag)) {
                    // TODO 处理跳转至结算页
                    BaseBean<Object> baseBean = JsonUtil.jsonArray(responseInfo.result, Object.class);
                    if (baseBean.code == 100) {
//                        appGoodsChooseInfor.setCount(appGoodsChooseInfor.getCount() - 1);
                        SharedPreferencesUtil.saveStringData(CommodityDetailInforActivity.this, Constant.CachTag.APP_COMMDITY_GOODS_CART_SUBMMIT, responseInfo.result);
                        Intent intent = new Intent(CommodityDetailInforActivity.this, CommoditySettlementActivity.class);
//                        intent.putExtra(CommodityGoodsCardListActivity.CURRENTSUBMMITCARDIDS, currentSubmmitCardIds);
                        startActivity(intent);
                    } else {
                        toast(baseBean.msg);
                    }
                }
            }

            @Override
            public void onFailure(HttpException error, String msg, String requestTag) {
                NetWorkStateUtils.errorNetMes(CommodityDetailInforActivity.this);
            }
        };
    }

    private void mInitGoodsNumEdit(TextView goodEt) {
        // less than 0 ,more than inventory
//        if (appGoodInfor.inventory < goodsDefaultNum) {
//            goodsDefaultNum = appGoodInfor.inventory;
//        }
        goodEt.setText(goodsDefaultNum + "");
        appGoodsChooseInfor.setCount(goodsDefaultNum);
        tv_props_detail_store_num.setText(appGoodInfor.inventory + "");
    }

    public void setGoosInventory() {

        if (appGoodInfor.params.size() == appGoodsChooseInfor.getProps().size()) {
//			MyLog.e("AAA", "setGoosInventory----"
//					+ appGoodsChooseInfor.getProps().size());
            tv_props_detail_store.setText("(库存");
            String idsChoose = getIds(appGoodsChooseInfor.getProps());
            rightInventory = getRightInventory(appGoodInfor.detailInventory,
                    idsChoose);
            if (rightInventory != null) {
                updateInventoryView(rightInventory);
            }
        }
    }

    private String getIds(Map<Integer, GoodDetailBean.Param.Prop> props) {
        Iterator iter = props.entrySet().iterator();
        String ids = "";
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            Integer key = (Integer) entry.getKey();
            GoodDetailBean.Param.Prop val = (GoodDetailBean.Param.Prop) entry.getValue();
            ids = ids + val.propId + "_";
        }
        return ids;
    }

    /**
     * @param detailInventory 返回的价格属性组合列表
     * @param str             自己生成的属性列表
     * @return 返回匹配的属性组合对象
     */
    private GoodDetailBean.ItemInventory getRightInventory(
            List<GoodDetailBean.ItemInventory> detailInventory, String str) {
        if (detailInventory == null) {
            return null;
        }
        for (int i = 0; i < detailInventory.size(); i++) {
            GoodDetailBean.ItemInventory itemInventory = detailInventory.get(i);

            if (idsSort(str).equals(idsSort(itemInventory.id))) {
                return itemInventory;
            }
        }
        return null;
    }

    private String idsSort(String str) {
        String[] split = str.split("_");
        Arrays.sort(split);
        return Arrays.toString(split);
    }

    /**
     * 更新库存量，和价格的view
     */
    private void updateInventoryView(GoodDetailBean.ItemInventory rightInventory) {
        // TODO Auto-generated method stub
        if (rightInventory.count != null) {

            tv_props_detail_store_num.setText(rightInventory.count + "");

            try {
                tv_props_pre_price_two.setText("¥" +
                        CommonUtils.floatToTowDecima(Float
                                .parseFloat(rightInventory.price)));
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @param b 商品数量的+——更新和控制
     */
    private void mSetGoodsNumEdit(boolean b) {
        // less than 0 ,more than inventory
        int parseInt = Integer.parseInt(et_props_goods_num.getText().toString().trim());

        // MyLog.e("CommodityDetailAct", "parseInt----------" + parseInt
        // + "----------------" + appGoodInfor.inventory);
        if (b) {
            if (rightInventory != null) {
                int parseInt2 = Integer.parseInt(rightInventory.count);
                if (parseInt2 == 0) {
                    parseInt = 1;
                } else {
                    parseInt = parseInt >= parseInt2 ? parseInt2
                            : ++parseInt;
                }
            } else if (appGoodInfor != null) {
                parseInt = parseInt >= appGoodInfor.inventory ? appGoodInfor.inventory
                        : ++parseInt;
            }
            if(parseInt<=1){
                parseInt=1;
            }
            et_props_goods_num.setText(parseInt + "");
        } else if (parseInt > 0) {
            parseInt = (parseInt < 2 ? 1 : --parseInt);
            et_props_goods_num.setText(parseInt + "");
        }
        appGoodsChooseInfor.setCount(parseInt);
    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(this.getClass().getSimpleName()); //统计页面(仅有Activity的应用中SDK自动调用，不需要单独写)
        MobclickAgent.onResume(this);          //统计时长
        if (goodsCard == null) {
            goodsCard = new BadgeView(this, myTitleBarHelper.getRightImag());
//            goodsCard.setTextColor(Color.WHITE);
//            goodsCard.setBadgePosition(BadgeView.POSITION_TOP_RIGHT);
//            goodsCard.setTextSize(12);
        }
        CommonUtils.setRedDotNum(goodsCard, SharedPreferencesUtil.getIntData(this, Constant.ShouYeUrl.SHAOPPING_NUM, 0));
//        setRedDotNum(goodsCard, SharedPreferencesUtil.getIntData(this, Constant.ShouYeUrl.SHAOPPING_NUM, 0));
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(this.getClass().getSimpleName()); // （仅有Activity的应用中SDK自动调用，不需要单独写）保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息
        MobclickAgent.onPause(this);
    }
}
