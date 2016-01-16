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
import android.webkit.WebSettings;
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
import com.henanjianye.soon.communityo2o.R;
import com.henanjianye.soon.communityo2o.adapter.CommodityPropsAdapter;
import com.henanjianye.soon.communityo2o.common.Constant;
import com.henanjianye.soon.communityo2o.common.GoodsCardDataHelper;
import com.henanjianye.soon.communityo2o.common.enties.AppGoodsChooseInfor;
import com.henanjianye.soon.communityo2o.common.enties.BaseBean;
import com.henanjianye.soon.communityo2o.common.enties.BaseDataBean;
import com.henanjianye.soon.communityo2o.common.enties.GoodDetailBean;
import com.henanjianye.soon.communityo2o.common.enties.JsonUtil;
import com.henanjianye.soon.communityo2o.common.util.CommonUtils;
import com.henanjianye.soon.communityo2o.common.util.MyTitleBarHelper;
import com.henanjianye.soon.communityo2o.common.util.NetWorkStateUtils;
import com.henanjianye.soon.communityo2o.common.util.ShareSDKConfigUtil;
import com.henanjianye.soon.communityo2o.common.util.SharedPreferencesUtil;
import com.henanjianye.soon.communityo2o.common.util.UmShareUtils;
import com.henanjianye.soon.communityo2o.common.util.UserSharedPreferencesUtil;
import com.henanjianye.soon.communityo2o.common.view.CustomProgressDialog;
import com.henanjianye.soon.communityo2o.common.view.ProgressWebView;
import com.henanjianye.soon.communityo2o.interf.NetWorkCallback;
import com.henanjianye.soon.communityo2o.view.MyLayoutManager;
import com.umeng.analytics.MobclickAgent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cn.sharesdk.framework.Platform;

/**
 * Created by Administrator on 2015/9/14.
 */
public class WebViewHaveShareActivity extends BaseActivity {

    private LinearLayout ll_refresh;
    private Button bt_refresh;
    private ProgressWebView iv_goods_details;
    private TextView detail_for_help;
    private TextView detail_add_card;
    private TextView detail_buy_now;

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
    //    private BadgeView goodsCard;
    private RelativeLayout ll_control_panel;
    private static final String APP_CACAHE_DIRNAME = "/webcache";
    private ImageView iv_title_bar_right;//分享
    private String url;
    String content;
    String title,sharetitle,sharepic;
    @Override
    public int mysetContentView() {
        return R.layout.activity_commodity_detail;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initEvent();
        initProcess();
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
        ll_control_panel = (RelativeLayout) findViewById(R.id.ll_control_panel);
        ll_control_panel.setVisibility(View.GONE);
        iv_title_bar_right = (ImageView) findViewById(R.id.iv_title_bar_right);



    }

    private void initEvent() {
        bt_refresh.setOnClickListener(this);
        detail_for_help.setOnClickListener(this);
        detail_add_card.setOnClickListener(this);
        detail_buy_now.setOnClickListener(this);
        iv_zhezhao.setOnClickListener(this);
        bt_buy.setOnClickListener(this);
        iv_title_bar_right.setOnClickListener(this);
    }

    private void initProcess() {
        myTitleBarHelper.resetState();

        iv_title_bar_right.setVisibility(View.VISIBLE);
        iv_title_bar_right.setImageResource(R.mipmap.event_share);
        Intent intent = getIntent();
        title = intent.getStringExtra("Title");
        url = intent.getStringExtra("Url");
        sharetitle = intent.getStringExtra("sharetitle");
        sharepic = intent.getStringExtra("sharepic");
        content = intent.getStringExtra("content");
        myTitleBarHelper.setMiddleText(title);
        initWebView(iv_goods_details);
        //暂时用购物代替
        iv_goods_details.setWebViewClient(new MyWebViewClient());
        iv_goods_details.loadUrlNoAssesToken(url);
        MyHander myHander = new MyHander();
        iv_goods_details.setHandler(myHander);
//        commodityPropsAdapter = new CommodityPropsAdapter(this, appGoodsChooseInfor);
//        commodityPropsAdapter.addHeaderView(headerView);
//        commodityPropsAdapter.addFooterView(footView);
//        mRecyclerView.setAdapter(commodityPropsAdapter);
//        goodsCardDataHelper = new GoodsCardDataHelper(this);
        myTitleBarHelper.setOnclickListener(this);
//        initPropsData();
    }

    private void initWebView(WebView mWebView) {
        if(CommonUtils.isNetworkConnected(this)){
            mWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);  //设置 缓存模式
        }else{
            mWebView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);  //设置 缓存模式
        }

        // 开启 DOM storage API 功能
        mWebView.getSettings().setDomStorageEnabled(true);
        //开启 database storage API 功能
        mWebView.getSettings().setDatabaseEnabled(true);
        String cacheDirPath = getFilesDir().getAbsolutePath()+APP_CACAHE_DIRNAME;
//      String cacheDirPath = getCacheDir().getAbsolutePath()+Constant.APP_DB_DIRNAME;
        //设置数据库缓存路径
        mWebView.getSettings().setDatabasePath(cacheDirPath);
        //设置  Application Caches 缓存目录
        mWebView.getSettings().setAppCachePath(cacheDirPath);
        //开启 Application Caches 功能
        mWebView.getSettings().setAppCacheEnabled(true);
    }

//    private void initPropsData() {
//        goodsCardDataHelper.goodsProps(getNetRequestHelper(this).isShowProgressDialog(true), currentGoodsId);
//    }


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
                    CommonUtils.showPhoneDialog(WebViewHaveShareActivity.this, appGoodsChooseInfor.storeTelephone);
                } else {
                    toast("请咨询当地服务站");
                }
                break;
            case R.id.detail_add_card:
                if (UserSharedPreferencesUtil.getUserLoginState(WebViewHaveShareActivity.this) < 2) {
                    CommonUtils.startLoginActivity(WebViewHaveShareActivity.this);
                }
                isAddCard = true;

                rl_goods_props.setVisibility(View.VISIBLE);
                break;
            case R.id.detail_buy_now:
                rl_goods_props.setVisibility(View.VISIBLE);
                break;
            case R.id.iv_title_bar_left:
                finish();
                break;
            case R.id.iv_title_bar_right://分享
                UmShareUtils.getInstance(WebViewHaveShareActivity.this).shareTextAndImage(sharepic,content,sharetitle,url,new ShareSDKConfigUtil.MPlatformActionListener(){

                    @Override
                    public void onComplete(Platform platform, int action, HashMap<String, Object> res) {
                        toast("分享成功");
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

                break;
            case R.id.iv_zhezhao:
                isAddCard = false;
                rl_goods_props.setVisibility(View.GONE);
                break;
            case R.id.bt_buy:
                if (appGoodInfor == null || appGoodInfor.params == null || appGoodsChooseInfor == null || appGoodsChooseInfor.getProps() == null) {
                    return;
                }
                if (UserSharedPreferencesUtil.getUserLoginState(WebViewHaveShareActivity.this) < 2) {
                    CommonUtils.startLoginActivity(WebViewHaveShareActivity.this);
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
                mSetGoodsNumEdit(false);
                break;
            case R.id.tv_props_goods_num_plus:
                mSetGoodsNumEdit(true);
                break;
            case R.id.tv_title_bar_left:
                finish();
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

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            // pb.setVisibility(View.GONE);
            if (!isError) {
                ll_refresh.setVisibility(View.GONE);
                iv_goods_details.setVisibility(View.VISIBLE);
            }
        }

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

    class MyHander extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constant.StateInt.DIALOG_STATE_CANCLE:
                    if (customProgressDialog != null) {
                        customProgressDialog.dismiss();
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
                        toast(baseDataBean.msg);
                    }
                } else if (Constant.MallUrl.COMMODITY_CART_ADD.equals(requestTag)) {
                    BaseBean<Object> baseBean = JsonUtil.jsonArray(responseInfo.result, Object.class);
                    if (baseBean.code == 100) {
                        int num = SharedPreferencesUtil.getIntData(WebViewHaveShareActivity.this, Constant.ShouYeUrl.SHAOPPING_NUM, 0) + 1;
//                        CommonUtils.setRedDotNum(goodsCard, num);
                        SharedPreferencesUtil.saveIntData(WebViewHaveShareActivity.this, Constant.ShouYeUrl.SHAOPPING_NUM, num);
                        toast(baseBean.msg);
                    } else {
                        toast(baseBean.msg);
                    }
//                    toast(responseInfo.result);
                } else if (Constant.MallUrl.COMMODITY_BUY_NOW.equals(requestTag)) {
                    // TODO 处理跳转至结算页
                    BaseBean<Object> baseBean = JsonUtil.jsonArray(responseInfo.result, Object.class);
                    if (baseBean.code == 100) {
                        SharedPreferencesUtil.saveStringData(WebViewHaveShareActivity.this, Constant.CachTag.APP_COMMDITY_GOODS_CART_SUBMMIT, responseInfo.result);
                        Intent intent = new Intent(WebViewHaveShareActivity.this, CommoditySettlementActivity.class);
//                        intent.putExtra(CommodityGoodsCardListActivity.CURRENTSUBMMITCARDIDS, currentSubmmitCardIds);
                        startActivity(intent);
                    } else {
                        toast(baseBean.msg);
                    }
                }
            }

            @Override
            public void onFailure(HttpException error, String msg, String requestTag) {
                NetWorkStateUtils.errorNetMes(WebViewHaveShareActivity.this);
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
            } else {
                parseInt = parseInt >= appGoodInfor.inventory ? appGoodInfor.inventory
                        : ++parseInt;
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
        //        if (goodsCard == null) {
//            goodsCard = new BadgeView(this, myTitleBarHelper.getRightImag());
////            goodsCard.setTextColor(Color.WHITE);
////            goodsCard.setBadgePosition(BadgeView.POSITION_TOP_RIGHT);
////            goodsCard.setTextSize(12);
//        }
//        CommonUtils.setRedDotNum(goodsCard, SharedPreferencesUtil.getIntData(this, Constant.ShouYeUrl.SHAOPPING_NUM, 0));
//        setRedDotNum(goodsCard, SharedPreferencesUtil.getIntData(this, Constant.ShouYeUrl.SHAOPPING_NUM, 0));
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(this.getClass().getSimpleName()); // （仅有Activity的应用中SDK自动调用，不需要单独写）保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息
        MobclickAgent.onPause(this);
    }
}
