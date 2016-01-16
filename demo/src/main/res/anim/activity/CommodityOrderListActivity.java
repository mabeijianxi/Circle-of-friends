package anim.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.asyey.footballlibrary.network.exception.HttpException;
import com.asyey.footballlibrary.network.http.ResponseInfo;
import com.asyey.footballlibrary.universalimageloader.core.DisplayImageOptions;
import com.asyey.footballlibrary.universalimageloader.core.ImageLoader;
import com.henanjianye.soon.communityo2o.BaseActivity;
import com.henanjianye.soon.communityo2o.R;
import com.henanjianye.soon.communityo2o.RecycleView.PullCallback;
import com.henanjianye.soon.communityo2o.RecycleView.PullToLoadView;
import com.henanjianye.soon.communityo2o.adapter.Gv_Order_list_Imags_Adapter;
import com.henanjianye.soon.communityo2o.common.Constant;
import com.henanjianye.soon.communityo2o.common.MyLog;
import com.henanjianye.soon.communityo2o.common.OrderDataHelper;
import com.henanjianye.soon.communityo2o.common.enties.AppMallOrderResponsBean;
import com.henanjianye.soon.communityo2o.common.enties.BaseDataBean;
import com.henanjianye.soon.communityo2o.common.enties.CommodityOrderBean;
import com.henanjianye.soon.communityo2o.common.enties.CommodityOrderListBean;
import com.henanjianye.soon.communityo2o.common.enties.JsonUtil;
import com.henanjianye.soon.communityo2o.common.util.CommonUtils;
import com.henanjianye.soon.communityo2o.common.util.MyTitleBarHelper;
import com.henanjianye.soon.communityo2o.common.util.NetWorkStateUtils;
import com.henanjianye.soon.communityo2o.common.util.SharedPreferencesUtil;
import com.henanjianye.soon.communityo2o.common.util.UserSharedPreferencesUtil;
import com.henanjianye.soon.communityo2o.common.view.DividerItemDecoration;
import com.henanjianye.soon.communityo2o.interf.MyItemClickListener;
import com.henanjianye.soon.communityo2o.interf.NetWorkCallback;
import com.henanjianye.soon.communityo2o.view.CountdownViewUseList;
import com.henanjianye.soon.communityo2o.view.UserMenu;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/9/14.
 */
public class CommodityOrderListActivity extends BaseActivity implements MyItemClickListener {

    private static final int ORDER_SG = 0x2201;
    private static final int ORDER_MULTI = 0x2202;
    private PullToLoadView mPullToLoadView;
    private OrderListAdapter mAdapter;
    private boolean isLoading = false;
    //    private boolean isHasLoadedAll = false;
//    private int nextPage;
    private RecyclerView mRecyclerView;
    private UserMenu mMenu;
    private MyTitleBarHelper myTitleBarHelper;
    private int page = 1;
    private int orgId;
    //    private List<CommoditySortBean> commoditySortBeans;
//    private ArrayList<CommodityBean> commodityBeans;
    private OrderDataHelper orderDataHelper;
    private boolean isHasLoadedAll = true;
    private float currentTotalPrece;
    private String currentSubmmitCardIds;
    private int nextPage;
    private int currentStateCode;
    public static int COMMODITYORDERLISTACTIVITY_JUMP = 12344;
    public static int COMMODITYORDERLISTACTIVITY_JUMP_GOODSRETRUNACTIVITY = 22344;
    public static int COMMODITYORDERLISTACTIVITY_JUMP_PAYALLWAYACTIVITY = 32344;
    public static int COMMODITYORDERLISTACTIVITY_JUMP_WRITEINFOFORGOODSRETURNS = 42344;
    public static String ORDERID = "orderId";
    public static String KEYTOECALUTELIST = "keytoecalutelist";

    /**
     * 添加测试接口
     */
    private boolean debug = false;
    private int tempNextPage=5;

    @Override
    public int mysetContentView() {
        return R.layout.activity_commodity_order_list;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initEvent();
        initProcess();
    }

    private void initView() {
        mPullToLoadView = (PullToLoadView) findViewById(R.id.commodity_large);
        mRecyclerView = mPullToLoadView.getRecyclerView();
        myTitleBarHelper = new MyTitleBarHelper(this, getWindow().getDecorView().findViewById(android.R.id.content));
    }

    private void initCheckBoxData(ArrayList<Boolean> mselecteDeleteData, boolean isChecked) {
        for (int i = 0; i < mselecteDeleteData.size(); i++) {
            if (mselecteDeleteData.get(i) != null) {
                mselecteDeleteData.set(i, isChecked);
            }
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.iv_title_bar_left:
                finish();
                break;
            case R.id.bt_order_left:
                //订单操作
                clickEventleft(v);
                break;
            case R.id.bt_order_right:
                //订单操作
                clickEventRight(v);
                break;
        }
    }

    private void clickEventRight(View v) {
        int[] aa = (int[]) v.getTag();
        int tag = aa[1];
        int pos = aa[0];
        switch (tag) {
            case 0:
                //已关闭(已取消)
//                setButtonText(cellHolderHeader, "", false, "删除订单", true);
                orderDelete(pos);
                break;
            case 10:
                //待付款
//                setButtonText(cellHolderHeader, "取消订单", false, "立即支付", true);
                orderPayNow(pos);
                break;
            case 11:
                // 等待支付确认中
                orderCallForHelp(pos);
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

                orderConfirmRecieve(pos);
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
                if ((mAdapter.getmList().size()>pos&&mAdapter.getmList().get(pos).isShowReturnRequest)){

                }else{
                    startDispatchActivity(pos, mAdapter.getmList().get(pos));
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

                orderWriteFeedBackInfor(pos);
                break;
            case 46:
                // 退货中
//                setButtonText(cellHolderHeader, "联系客服", false, "退货中", false);
                break;
            case 47:
                // 取消订单成功
                // 退货中
//                setButtonText(cellHolderHeader, "联系客服", false, "删除订单", true);
                orderDelete(pos);
                break;
            case 48:
                // 取消订单失败
//                setButtonText(cellHolderHeader, "联系客服", false, "删除订单", true);
                orderDelete(pos);
                break;
            case 50:
                //已完成
//                setButtonText(cellHolderHeader, "", false, "删除订单", false);
                orderDelete(pos);
                break;
        }
    }

    private void orderWriteFeedBackInfor(int pos) {
        //  填写物流信息  0表示已发货退款
        //orderId = intent.getLongExtra("orderId", -1);
        Intent intent = new Intent(this, WriteInfoForGoodsReturns.class);
        intent.putExtra("orderId", mAdapter.getmList().get(pos).orderId);
        startActivityForResult(intent, COMMODITYORDERLISTACTIVITY_JUMP_WRITEINFOFORGOODSRETURNS);
    }

    private void orderRequestFeedBack(int pos) {
        //  申请退货  0表示已发货退款
        GoodsRetrunActivity.startGoodsRetrunActivityForresult(this, mAdapter.getmList().get(pos).orderId, 0, COMMODITYORDERLISTACTIVITY_JUMP_GOODSRETRUNACTIVITY);
    }

    private void orderConfirmRecieve(int pos) {
        //  确认收货
        orderDataHelper.confirmTakeGoods(getNetRequestHelper(CommodityOrderListActivity.this).isShowProgressDialog(false), mAdapter.getmList().get(pos).orderId);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (mPullToLoadView != null) {
            mPullToLoadView.initNetLoad(CommodityOrderListActivity.this);
        }
    }

    private void orderPayNow(int pos) {
        // 立刻支付
        CommodityOrderBean commodityOrderBean = mAdapter.getmList().get(pos);
        AppMallOrderResponsBean appMallOrderResponsBean = new AppMallOrderResponsBean();
        appMallOrderResponsBean.orderNos = commodityOrderBean.orderNo;
        ArrayList<CommodityOrderBean.OderGoods> OderGoods = commodityOrderBean.goods;
        String name = "";
        for (CommodityOrderBean.OderGoods good : OderGoods) {
            name = name + good.goodsName + "X" + good.goodsNum;
        }
        appMallOrderResponsBean.goodsNames = name;
        appMallOrderResponsBean.totalPrice = commodityOrderBean.totalPrice;
        PayAllWayActivity.startPayAllWayActivityForresult(this, COMMODITYORDERLISTACTIVITY_JUMP_PAYALLWAYACTIVITY, appMallOrderResponsBean);
    }

    private void orderDelete(int pos) {
        //  删除订单
        orderDataHelper.deleteOrder(getNetRequestHelper(CommodityOrderListActivity.this).isShowProgressDialog(false), mAdapter.getmList().get(pos).orderId);
    }

    private void orderCallForHelp(int pos) {
        //   联系客服
        CommonUtils.showPhoneDialog(CommodityOrderListActivity.this, mAdapter.getmList().get(pos).shopPhone);
    }

    private void orderCancle(int pos, boolean isPayed) {
        //  取消订单
        if (!isPayed) {
            orderDataHelper.cancelOrder(getNetRequestHelper(CommodityOrderListActivity.this).isShowProgressDialog(false), mAdapter.getmList().get(pos).orderId);
        } else {
            orderRequestFeedBack(pos);
        }
//        orderDataHelper.cancelOrder(getNetRequestHelper(CommodityOrderListActivity.this), mAdapter.getmList().get(pos).orderId);
    }

    private void clickEventleft(View v) {
        int[] aa = (int[]) v.getTag();
        int tag = aa[1];
        int pos = aa[0];
        switch (tag) {
            case 0:
                //已关闭(已取消)
//                setButtonText(cellHolderHeader, "", false, "删除订单", true);
                break;
            case 10:
                //待付款
//                setButtonText(cellHolderHeader, "取消订单", false, "立即支付", true);
                if (debug) {
                    startDispatchActivity(pos, mAdapter.getmList().get(pos));
                } else {

                    orderCancle(pos, false);
                }

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
                orderCallForHelp(pos);
                break;
            case 200:
                //已付款
//                if (commodityOrderBean.isShowReturnRequest) {
//                    currentStateCode = 200;
//                    setButtonText(cellHolderHeader, "联系客服", false, "已支付", false);
//                } else {
//                    setButtonText(cellHolderHeader, "联系客服", false, "已支付", false);
//                }
                orderCallForHelp(pos);
                break;
            case 26:
                // 退款确认中
//                setButtonText(cellHolderHeader, "联系客服", false, "已确认", false);
                orderCallForHelp(pos);
                break;
            case 30:
                //已发货
//                if (commodityOrderBean.isShowReturnRequest) {
//                    currentStateCode = 300;
//                    setButtonText(cellHolderHeader, "联系客服", false, "已发货", false);
//                } else {
//                    setButtonText(cellHolderHeader, "联系客服", false, "确认收货", true);
//                }
                orderCallForHelp(pos);

                break;
            case 300:
                //已发货
//                if (commodityOrderBean.isShowReturnRequest) {
//                    currentStateCode = 300;
//                    setButtonText(cellHolderHeader, "联系客服", false, "已发货", false);
//                } else {
//                    setButtonText(cellHolderHeader, "联系客服", false, "确认收货", true);
//                }
                orderCallForHelp(pos);

                break;
            case 40:
                // 已收货
//                if (commodityOrderBean.isShowReturnRequest) {
//                    currentStateCode = 400;
//                    setButtonText(cellHolderHeader, "联系客服", false, "已收货", false);
//                } else {
//                    setButtonText(cellHolderHeader, "联系客服", false, "已收货", false);
//                }
                orderCallForHelp(pos);
                break;

            case 400:
                // 已收货
//                if (commodityOrderBean.isShowReturnRequest) {
//                    currentStateCode = 400;
//                    setButtonText(cellHolderHeader, "联系客服", false, "已收货", false);
//                } else {
//                    setButtonText(cellHolderHeader, "联系客服", false, "已收货", false);
//                }
                orderCallForHelp(pos);
                break;
            case 45:
                // 取消订单申请中
//                setButtonText(cellHolderHeader, "联系客服", false, "退货物流", true);
                orderCallForHelp(pos);
                break;
            case 46:
                // 退货中
//                setButtonText(cellHolderHeader, "联系客服", false, "退货中", false);
                orderCallForHelp(pos);
                break;
            case 47:
                // 取消订单成功
                // 退货中
//                setButtonText(cellHolderHeader, "联系客服", false, "删除订单", true);
                orderCallForHelp(pos);
                break;
            case 48:
                // 取消订单失败
//                setButtonText(cellHolderHeader, "联系客服", false, "删除订单", true);
                orderCallForHelp(pos);
                break;
            case 49:
                // 退货(退款)失败
                break;
            case 50:
                //已完成，跳转相应ac

//                setButtonText(cellHolderHeader, "", false, "删除订单", false);
                break;
        }
    }

    private void startDispatchActivity(int pos, CommodityOrderBean commodityOrderBean) {
        if (commodityOrderBean.goods.size() > 1) {
            // 跳转多商品列表评价界面
            Intent intent = new Intent(this, CommodityOrderEvaluteDetailListActivity.class);
            intent.putExtra(KEYTOECALUTELIST, commodityOrderBean);
            startActivityForResult(intent, CommodityOrderEvaluteDetailListActivity.REQUESTTOCOMMODITYORDEREVALUTEDETAILLIST);
        } else {
            // 跳转商品评价界面
            //TODO 做相应跳转
            CommodityOrderBean.OderGoods oderGoods = commodityOrderBean.goods.get(0);
            if (oderGoods.canAddPic || oderGoods.canEvaluate) {
                //TODO 跳转至评论页面
                GoodsEvaluateActivity.TransferEvaluation(CommodityOrderListActivity.this, oderGoods.canAddPic, oderGoods.goodsId, oderGoods.goodsCartId,oderGoods.picMap.middlePicUrl);
            } else {
                //  跳转至评论详情页面
                EaluationActivity.goEalutionActivity(CommodityOrderListActivity.this, oderGoods.goodsId);
            }

        }
    }


    private void initEvent() {
        myTitleBarHelper.setOnclickListener(this);
        myTitleBarHelper.resetState();
        myTitleBarHelper.setRightImag(R.drawable.title_commodity_goodscard);
        LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mPullToLoadView.setLayoutManager(manager);
    }

    private void initProcess() {
        orgId = UserSharedPreferencesUtil.getUserInfo(this, UserSharedPreferencesUtil.ORGID, -1);
        if (orgId == -1) {
            toast("小区id不能为空");
            return;
        }
        myTitleBarHelper.resetState();
        myTitleBarHelper.setMiddleText("我的订单");
//        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(
                this, DividerItemDecoration.HORIZONTAL_LIST));
        mAdapter = new OrderListAdapter();
        mAdapter.setOnItemClickListener(this);
        mRecyclerView.setAdapter(mAdapter);
        //设置分隔线，和listView 不同注意
        mPullToLoadView.isLoadMoreEnabled(true);
        mPullToLoadView.setPullCallback(new CommodityPullCallback());
//        SharedPreferencesUtil.saveStringData(this, Constant.CachTag.APP_COMMDITY_ORDER_LIST, str);
        String resultCach = SharedPreferencesUtil.getStringData(this, Constant.CachTag.APP_COMMDITY_ORDER_LIST, "");
        // 缓存
        parseDate(resultCach, true);
        mPullToLoadView.initLoad();
    }

    /**
     * @param page 页码
     */
    private void loadData(final int page) {
        if (orderDataHelper == null) {
            orderDataHelper = new OrderDataHelper(this);
        }
//        if (page == 1) {
        orderDataHelper.getOrderListInfor(getNetRequestHelper(this).isShowProgressDialog(false), page);
//        } else {
//            orderDataHelper.getOrderListInfor(getNetRequestHelper(this).isShowProgressDialog(false), page);
//        }
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
                if (Constant.OrderUrl.COMMODITY_ORDER_LIST_.equals(requestTag)) {
                    parseDate(responseInfo.result, false);
                    mPullToLoadView.setComplete();
                }
                if (Constant.OrderUrl.COMMODITY_ORDER_CANCEL.equals(requestTag)) {
                    if (responseInfo.result == null) {
                        toast("数据错误");
                        return;
                    }
                    BaseDataBean<Object> baseBean = JsonUtil.parseDataObject(responseInfo.result, Object.class);
                    if (baseBean.code == 100) {
                        //  取消
                        mPullToLoadView.initNetLoad(CommodityOrderListActivity.this);
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
                        mPullToLoadView.initNetLoad(CommodityOrderListActivity.this);
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
                        //TODO  确认收货
                        mPullToLoadView.initNetLoad(CommodityOrderListActivity.this);
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
                NetWorkStateUtils.errorNetMes(CommodityOrderListActivity.this);
            }
        };
    }


    /**
     * @param str
     * @param isCach 解析请求的购物车列表
     */
    private synchronized void parseDate(String str, boolean isCach) {
        if (str == null) {
            return;
        }
        BaseDataBean<CommodityOrderListBean> baseBean = JsonUtil.parseDataObject(str, CommodityOrderListBean.class);
        CommodityOrderListBean commodityOrderListBean = baseBean.data;
        ArrayList<CommodityOrderBean> commodityOrderBeans = null;
        if (commodityOrderListBean != null) {
            commodityOrderBeans = commodityOrderListBean.list;
        }
        if (baseBean.code == 100) {
            if (!isCach) {
                if (mAdapter != null && commodityOrderListBean.pageNo == 1) {
                    mAdapter.clear();
                    SharedPreferencesUtil.saveStringData(this, Constant.CachTag.APP_COMMDITY_ORDER_LIST, str);
                }
            }
            isLoading = false;
            if (commodityOrderListBean.totalCount > commodityOrderListBean.pageNo * commodityOrderListBean.pageSize) {
                nextPage = commodityOrderListBean.pageNo + 1;
                isHasLoadedAll = false;
            } else {
                isHasLoadedAll = true;
            }
            if (commodityOrderBeans != null && commodityOrderBeans.size() > 0) {
                mAdapter.add(commodityOrderBeans);
            }
        } else {
            if (!TextUtils.isEmpty(baseBean.msg)) {
                toast(baseBean.msg);
            }
        }

    }

    @Override
    public void onItemClick(View view, int postion) {
        List<CommodityOrderBean> objects = mAdapter.getmList();
        if (objects != null && objects.size() > postion) {
//            Object object = objects.get(postion);
            //  订单点击跳转到商品详情状态
            Intent intent = new Intent(this, OrderDetailActivity.class);
            intent.putExtra(ORDERID, mAdapter.getmList().get(postion).orderId);
            CommodityOrderListActivity.this.startActivityForResult(intent, COMMODITYORDERLISTACTIVITY_JUMP);
        }
    }

    private class OrderListAdapter extends RecyclerView.Adapter<ViewHolder> {
        private List<CommodityOrderBean> mList;
        private MyItemClickListener myItemClickListener;
        private DisplayImageOptions build = null;
        private ImageLoader instance;

        public OrderListAdapter() {
            mList = new ArrayList<>();
            instance = ImageLoader.getInstance();
            build = new DisplayImageOptions.Builder()
                    .showImageOnFail(R.mipmap.dingdan_image)
                    .showImageForEmptyUri(R.mipmap.dingdan_image).build();
        }

        public List<CommodityOrderBean> getmList() {
            return mList;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            if (i == ORDER_MULTI) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.activity_commodity_order_list_multi_item, viewGroup, false);
                return new CellHolderMulti(view, myItemClickListener);
            } else {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.activity_commodity_order_list_sg_item, viewGroup, false);
                return new CellHolderSg(view, myItemClickListener);
            }
        }

        /**
         * 设置Item点击监听
         *
         * @param listener
         */
        public void setOnItemClickListener(MyItemClickListener listener) {
            this.myItemClickListener = listener;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int i) {
            List<CommodityOrderBean> commodityOrderBeans = getmList();
            CommodityOrderBean commodityOrderBean = commodityOrderBeans.get(i);

            if (holder instanceof CellHolderMulti) {
                CellHolderMulti cellHolderHeader = (CellHolderMulti) holder;
                if (commodityOrderBean.goods.size() > 1) {
                    Gv_Order_list_Imags_Adapter gv_order_list_imags_adapter = new Gv_Order_list_Imags_Adapter(CommodityOrderListActivity.this);
                    gv_order_list_imags_adapter.setData(commodityOrderBean.goods);
                    cellHolderHeader.gv_order_imag.setAdapter(gv_order_list_imags_adapter);
                }
                setCellHolderMultiView(cellHolderHeader, commodityOrderBean, i);
            } else {
                CellHolderSg cellHolderSg = (CellHolderSg) holder;
                if (commodityOrderBean.goods.size() == 1) {
                    cellHolderSg.tv_title.setText(commodityOrderBean.goods.get(0).goodsName);
                    cellHolderSg.tv_amount.setText(commodityOrderBean.goods.get(0).goodsNum + "");
                    cellHolderSg.tv_price.setText("¥" + CommonUtils.floatToTowDecima(commodityOrderBean.goods.get(0).goodsPrice));
                    ArrayList<CommodityOrderBean.OderGoods.GoodsSpec> goodsSpecs = commodityOrderBean.goods.get(0).goodsSpecList;
                    cellHolderSg.pro_tag1.setVisibility(View.INVISIBLE);
                    cellHolderSg.pro_tag1_value.setVisibility(View.INVISIBLE);
                    cellHolderSg.pro_tag2.setVisibility(View.INVISIBLE);
                    cellHolderSg.pro_tag2_value.setVisibility(View.INVISIBLE);
                    //TODO 此处目前只适应最多两个属性，目前只显示两个
                    if (goodsSpecs.size() == 1) {
                        cellHolderSg.pro_tag1.setText(goodsSpecs.get(0).param);
                        cellHolderSg.pro_tag1_value.setText(goodsSpecs.get(0).paramValue);
                        cellHolderSg.pro_tag2.setVisibility(View.INVISIBLE);
                        cellHolderSg.pro_tag2_value.setVisibility(View.INVISIBLE);
                    } else if (goodsSpecs.size() >= 2) {
                        cellHolderSg.pro_tag1.setText(goodsSpecs.get(0).param);
                        cellHolderSg.pro_tag1_value.setText(goodsSpecs.get(0).paramValue);
                        cellHolderSg.pro_tag2.setText(goodsSpecs.get(1).param);
                        cellHolderSg.pro_tag2_value.setText(goodsSpecs.get(1).paramValue);
                    } else {
                        cellHolderSg.pro_tag1.setVisibility(View.INVISIBLE);
                        cellHolderSg.pro_tag1_value.setVisibility(View.INVISIBLE);
                        cellHolderSg.pro_tag2.setVisibility(View.INVISIBLE);
                        cellHolderSg.pro_tag2_value.setVisibility(View.INVISIBLE);
                    }

                    if (commodityOrderBean.goods.get(0).picMap.smallPicUrl != null) {
                        instance.displayImage(commodityOrderBean.goods.get(0).picMap.middlePicUrl, cellHolderSg.iv_img, build);
                    }
                }
                setCellHolderSgView(cellHolderSg, commodityOrderBean, i);
            }
        }

        public void add(List<CommodityOrderBean> beans) {
            mList.addAll(beans);
            notifyDataSetChanged();
        }

        public void clear() {
            mList.clear();
            notifyDataSetChanged();
        }

        @Override
        public int getItemViewType(int position) {
            if (mList.size() > position) {
                CommodityOrderBean object = mList.get(position);
                if (object.goods.size() > 1) {
                    return ORDER_MULTI;
                } else {
                    return ORDER_SG;
                }
            } else {
                return super.getItemViewType(position);
            }

        }

        @Override
        public long getItemId(int position) {
            return super.getItemId(position);
        }

        @Override
        public int getItemCount() {
            return mList == null ? 0 : mList.size();
        }
    }

    private void setCellHolderMultiView(CellHolderMulti cellHolderHeader, CommodityOrderBean commodityOrderBean, int pos) {
        int orderStatus = commodityOrderBean.orderStatus;
        ArrayList<CommodityOrderBean.OderGoods> oderGoodses = commodityOrderBean.goods;
        boolean mCanEvaluate = false;
        boolean mCanAddPic = false;
        for (int i = 0; i < oderGoodses.size(); i++) {
            if (oderGoodses.get(i).canEvaluate) {
                mCanEvaluate = true;
                break;
            }
            if (oderGoodses.get(i).canAddPic) {
                mCanAddPic = true;
            }
        }
        cellHolderHeader.tv_title_draw_img.setVisibility(View.VISIBLE);
        cellHolderHeader.tv_title_blank.setVisibility(View.GONE);
        cellHolderHeader.tv_title_blank_tag.setVisibility(View.GONE);
        cellHolderHeader.tv_title_draw_img.setText(commodityOrderBean.orderNo);
        cellHolderHeader.tv_order_price.setText("¥" + CommonUtils.floatToTowDecima(commodityOrderBean.payPrice));
        currentStateCode = orderStatus;
        switch (orderStatus) {
            case 0:
                //已关闭(已取消)
                cellHolderHeader.tv_title_order_state.setText("交易关闭");

                setButtonText(cellHolderHeader, "", false, "删除订单", true);
                break;
            case 10:
                //待付款
                cellHolderHeader.tv_title_draw_img.setVisibility(View.GONE);
                cellHolderHeader.tv_title_blank.setVisibility(View.VISIBLE);
                cellHolderHeader.tv_title_blank_tag.setVisibility(View.VISIBLE);
                //TODO 倒计时
                cellHolderHeader.tv_title_order_state.setText("等待付款");
                cellHolderHeader.tv_title_order_state.setTextColor(getResources().getColor(R.color.indicator_selector_color));
                cellHolderHeader.tv_title_blank.setOnCountdownEndListener(new CountdownViewUseList.OnCountdownEndListener() {
                    @Override
                    public void onEnd(CountdownViewUseList viewUseList) {
                        viewUseList.setmText("已过期");
                    }
                });

                if (commodityOrderBean.diffTime == 0) {
                    cellHolderHeader.tv_title_blank.setmText("已过期");
                } else {
                    if (commodityOrderBean.diffTime > (24 * 3600)) {
                        cellHolderHeader.tv_title_blank.setShowDayView(true);
                    } else {
                        cellHolderHeader.tv_title_blank.setShowDayView(false);
                    }
                    cellHolderHeader.tv_title_blank.start(commodityOrderBean.diffTime * 1000);
                }
                setButtonText(cellHolderHeader, "取消订单", false, "立即支付", true);
                break;
            case 11:
                // 等待支付确认中
                //已关闭(已取消)
                cellHolderHeader.tv_title_order_state.setText("支付中");

                setButtonText(cellHolderHeader, "", false, "联系客服", true);
                break;
            case 20:
                //已付款
                if (commodityOrderBean.isShowReturnRequest) {
                    currentStateCode = 200;
                    cellHolderHeader.tv_title_order_state.setText("取消订单中");
                    setButtonText(cellHolderHeader, "联系客服", false, "已支付", false);
                } else {
                    cellHolderHeader.tv_title_order_state.setText("等待发货");
                    setButtonText(cellHolderHeader, "联系客服", false, "已支付", false);
                }

                break;
            case 26:
                // 退款确认中
                cellHolderHeader.tv_title_order_state.setText("取消订单已确认");
                setButtonText(cellHolderHeader, "联系客服", false, "已确认", false);
                break;
            case 30:
                //已发货
                if (commodityOrderBean.isShowReturnRequest) {
                    currentStateCode = 300;
                    cellHolderHeader.tv_title_order_state.setText("取消订单中");
                    setButtonText(cellHolderHeader, "联系客服", false, "已发货", false);
                } else {
                    cellHolderHeader.tv_title_order_state.setText("已发货");
                    setButtonText(cellHolderHeader, "联系客服", false, "确认收货", true);
                }


                break;
            case 40:
                // 已收货
                if (commodityOrderBean.isShowReturnRequest) {
                    currentStateCode = 400;
                    cellHolderHeader.tv_title_order_state.setText("取消订单中");
                    setButtonText(cellHolderHeader, "联系客服", false, "已收货", false);
                } else {
                    cellHolderHeader.tv_title_order_state.setText("已收货");
                    if (mCanEvaluate) {
                        setButtonText(cellHolderHeader, "联系客服", false, "晒单评价", false);
                    } else if (mCanAddPic) {
                        setButtonText(cellHolderHeader, "联系客服", false, "追加评价", false);
                    } else {
                        setButtonText(cellHolderHeader, "联系客服", false, "查看评价", false);
                    }
                }
                break;
            case 45:
                // 取消订单申请中
                cellHolderHeader.tv_title_order_state.setText("申请成功");
                cellHolderHeader.tv_title_order_state.setTextColor(getResources().getColor(R.color.indicator_selector_color));
                setButtonText(cellHolderHeader, "联系客服", false, "退货物流", true);
                break;
            case 46:
                // 退货中
                cellHolderHeader.tv_title_order_state.setText("退货中");
                setButtonText(cellHolderHeader, "联系客服", false, "退货中", false);
                break;
            case 47:
                // 取消订单成功
                // 退货中
                cellHolderHeader.tv_title_order_state.setText("取消订单成功");
                setButtonText(cellHolderHeader, "联系客服", false, "删除订单", true);
                break;
            case 48:
                // 取消订单失败
                cellHolderHeader.tv_title_order_state.setText("取消订单失败");
                setButtonText(cellHolderHeader, "联系客服", false, "删除订单", true);
                break;
            case 49:
                // 退货(退款)失败
                break;
            case 50:
                //已完成
                cellHolderHeader.tv_title_order_state.setText("交易完成");

                break;
        }
        int[] aa = new int[2];
        aa[0] = pos;
        aa[1] = currentStateCode;
        cellHolderHeader.bt_order_right.setTag(aa);
        cellHolderHeader.bt_order_right.setOnClickListener(this);
        cellHolderHeader.bt_order_left.setTag(aa);
        cellHolderHeader.bt_order_left.setOnClickListener(this);
    }

    private void setCellHolderSgView(CellHolderSg cellHolderHeader, CommodityOrderBean commodityOrderBean, int pos) {
        int orderStatus = commodityOrderBean.orderStatus;
        MyLog.e("AAA", commodityOrderBean.goods.size() + "---size");
        cellHolderHeader.tv_title_draw_img.setVisibility(View.VISIBLE);
        cellHolderHeader.tv_title_blank.setVisibility(View.GONE);
        cellHolderHeader.tv_title_blank_tag.setVisibility(View.GONE);
        cellHolderHeader.tv_title_draw_img.setText(commodityOrderBean.orderNo);
        //实付款
        cellHolderHeader.tv_order_price.setText("¥" + CommonUtils.floatToTowDecima(commodityOrderBean.payPrice));
        currentStateCode = orderStatus;
        MyLog.e("AAA", "orderStatus----" + orderStatus + "----" + pos);
        switch (orderStatus) {
            case 0:
                //已关闭(已取消)
                cellHolderHeader.tv_title_order_state.setText("交易关闭");
                setButtonText(cellHolderHeader, "", false, "删除订单", true);
                break;
            case 10:
                //待付款
                cellHolderHeader.tv_title_draw_img.setVisibility(View.GONE);
                cellHolderHeader.tv_title_blank.setVisibility(View.VISIBLE);
                cellHolderHeader.tv_title_blank_tag.setVisibility(View.VISIBLE);

                //TODO 倒计时
                cellHolderHeader.tv_title_order_state.setText("等待付款");
                cellHolderHeader.tv_title_order_state.setTextColor(getResources().getColor(R.color.indicator_selector_color));
//                cellHolderHeader.tv_title_blank.setOnFinishedListener(new CountDownView.OnFinishedListener() {
//                    @Override
//                    public void onFinished(CountDownView countDownView) {
//                        countDownView.setText("已过期");
//                    }
//                });
//                if (commodityOrderBean.diffTime == 0) {
//                    cellHolderHeader.tv_title_blank.setText("已过期");
//                } else {
//                    cellHolderHeader.tv_title_blank.setMills(commodityOrderBean.diffTime);
//                }
                cellHolderHeader.tv_title_blank.setOnCountdownEndListener(new CountdownViewUseList.OnCountdownEndListener() {
                    @Override
                    public void onEnd(CountdownViewUseList viewUseList) {
                        viewUseList.setmText("已过期");
                    }
                });
                if (commodityOrderBean.diffTime == 0) {
                    cellHolderHeader.tv_title_blank.setmText("已过期");
                } else {
                    if (commodityOrderBean.diffTime > (24 * 3600)) {
                        cellHolderHeader.tv_title_blank.setShowDayView(true);
                    } else {
                        cellHolderHeader.tv_title_blank.setShowDayView(false);
                    }
                    cellHolderHeader.tv_title_blank.start(commodityOrderBean.diffTime * 1000);
                }
                setButtonText(cellHolderHeader, "取消订单", false, "立即支付", true);
                break;
            case 11:
                // 等待支付确认中
                cellHolderHeader.tv_title_order_state.setText("支付中");
                setButtonText(cellHolderHeader, "", false, "联系客服", false);
//                setButtonText(cellHolderHeader, "", false, "删除订单", false);
                break;
            case 20:
                //已付款
                if (commodityOrderBean.isShowReturnRequest) {
                    currentStateCode = 200;
                    cellHolderHeader.tv_title_order_state.setText("取消订单中");
                    setButtonText(cellHolderHeader, "联系客服", false, "已支付", false);
                } else {
                    cellHolderHeader.tv_title_order_state.setText("等待发货");
                    setButtonText(cellHolderHeader, "联系客服", false, "已支付", false);
                }
                break;
            case 26:
                // 退款确认中
                cellHolderHeader.tv_title_order_state.setText("取消订单已确认");
                setButtonText(cellHolderHeader, "联系客服", false, "已确认", false);
                break;
            case 30:
                //已发货
                if (commodityOrderBean.isShowReturnRequest) {
                    currentStateCode = 300;
                    cellHolderHeader.tv_title_order_state.setText("取消订单中");
                    setButtonText(cellHolderHeader, "联系客服", false, "已发货", false);
                } else {
                    cellHolderHeader.tv_title_order_state.setText("已发货");
                    setButtonText(cellHolderHeader, "联系客服", false, "确认收货", true);
                }
                break;
            case 40:
                // 已收货
                if (commodityOrderBean.isShowReturnRequest) {
                    currentStateCode = 400;
                    cellHolderHeader.tv_title_order_state.setText("取消订单中");
                    setButtonText(cellHolderHeader, "联系客服", false, "已收货", false);
                } else {
                    cellHolderHeader.tv_title_order_state.setText("已收货");
                    if (commodityOrderBean.goods.size() == 1) {
                        CommodityOrderBean.OderGoods goods = commodityOrderBean.goods.get(0);
                        MyLog.e("AAA","goods.canEvaluate==="+goods.canEvaluate+"-- goods.canAddPic---"+ goods.canAddPic);
                        if (goods != null && goods.canEvaluate) {
                            setButtonText(cellHolderHeader, "联系客服", false, "晒单评价", false);
                        } else if (goods != null && goods.canAddPic) {
                            setButtonText(cellHolderHeader, "联系客服", false, "追加评价", false);
                        } else {
                            setButtonText(cellHolderHeader, "联系客服", false, "查看评价", false);
                        }
                    }
                }
                break;
            case 45:
                // 取消订单申请中
                cellHolderHeader.tv_title_order_state.setText("申诉成功");
                cellHolderHeader.tv_title_order_state.setTextColor(getResources().getColor(R.color.indicator_selector_color));
                setButtonText(cellHolderHeader, "联系客服", false, "退货物流", true);
                break;
            case 46:
                // 退货中
                cellHolderHeader.tv_title_order_state.setText("退货申请中");
                setButtonText(cellHolderHeader, "联系客服", false, "退货中", false);
                break;
            case 47:
                // 取消订单成功
                // 退货中
                cellHolderHeader.tv_title_order_state.setText("取消订单成功");
                setButtonText(cellHolderHeader, "联系客服", false, "删除订单", true);
                break;
            case 48:
                // 取消订单失败
                cellHolderHeader.tv_title_order_state.setText("取消订单失败");
                setButtonText(cellHolderHeader, "联系客服", false, "删除订单", true);
                break;
            // 退货(退款)失败
            case 49:
                break;
            case 50:
                //已完成

                cellHolderHeader.tv_title_order_state.setText("交易完成");
                setButtonText(cellHolderHeader, "", false, "删除订单", false);
                break;
        }
        int[] aa = new int[2];
        aa[0] = pos;
        aa[1] = currentStateCode;
        cellHolderHeader.bt_order_right.setTag(aa);
        cellHolderHeader.bt_order_right.setOnClickListener(this);
        cellHolderHeader.bt_order_left.setTag(aa);
        cellHolderHeader.bt_order_left.setOnClickListener(this);
    }

    private void setButtonText(CellHolderMulti cellHolderHeader, String left, boolean hlightl, String right, boolean hlightr) {
        //1 right 2 left  0 all
        setButtonText(cellHolderHeader, left, right);
        if (hlightl) {
            cellHolderHeader.bt_order_left.setTextColor(getResources().getColor(R.color.white));
            cellHolderHeader.bt_order_left.setBackgroundResource(R.mipmap.butten_green);
        } else {
            cellHolderHeader.bt_order_left.setTextColor(getResources().getColor(R.color.little_grey));
            cellHolderHeader.bt_order_left.setBackgroundResource(R.mipmap.butten);
        }
        if (hlightr) {
            cellHolderHeader.bt_order_right.setTextColor(getResources().getColor(R.color.white));
            cellHolderHeader.bt_order_right.setBackgroundResource(R.mipmap.butten_green);
        } else {
            cellHolderHeader.bt_order_right.setTextColor(getResources().getColor(R.color.little_grey));
            cellHolderHeader.bt_order_right.setBackgroundResource(R.mipmap.butten);
        }
    }

    private void setButtonText(CellHolderMulti cellHolderHeader, String left, String right) {
        //1 right 2 left  0 all
        if (!TextUtils.isEmpty(left)) {
            cellHolderHeader.bt_order_left.setVisibility(View.VISIBLE);
            cellHolderHeader.bt_order_right.setVisibility(View.VISIBLE);
            cellHolderHeader.bt_order_right.setText(right);
            cellHolderHeader.bt_order_left.setText(left);
        } else {
            cellHolderHeader.bt_order_left.setVisibility(View.GONE);
            cellHolderHeader.bt_order_right.setVisibility(View.VISIBLE);
            cellHolderHeader.bt_order_right.setText(right);
        }
    }

    private void setButtonText(CellHolderSg cellHolderHeader, String left, boolean hlightl, String right, boolean hlightr) {
        //1 right 2 left  0 all
        setButtonText(cellHolderHeader, left, right);
        if (hlightl) {
            cellHolderHeader.bt_order_left.setTextColor(getResources().getColor(R.color.white));
            cellHolderHeader.bt_order_left.setBackgroundResource(R.mipmap.butten_green);
        } else {
            cellHolderHeader.bt_order_left.setTextColor(getResources().getColor(R.color.little_grey));
            cellHolderHeader.bt_order_left.setBackgroundResource(R.mipmap.butten);
        }
        if (hlightr) {
            cellHolderHeader.bt_order_right.setTextColor(getResources().getColor(R.color.white));
            cellHolderHeader.bt_order_right.setBackgroundResource(R.mipmap.butten_green);
        } else {
            cellHolderHeader.bt_order_right.setTextColor(getResources().getColor(R.color.little_grey));
            cellHolderHeader.bt_order_right.setBackgroundResource(R.mipmap.butten);
        }
    }

    private void setButtonText(CellHolderSg cellHolderHeader, String left, String right) {
        //1 right 2 left  0 all
        if (!TextUtils.isEmpty(left)) {
            cellHolderHeader.bt_order_left.setVisibility(View.VISIBLE);
            cellHolderHeader.bt_order_right.setVisibility(View.VISIBLE);
            cellHolderHeader.bt_order_right.setText(right);
            cellHolderHeader.bt_order_left.setText(left);
        } else {
            cellHolderHeader.bt_order_left.setVisibility(View.GONE);
            cellHolderHeader.bt_order_right.setVisibility(View.VISIBLE);
            cellHolderHeader.bt_order_right.setText(right);
        }
    }

    private class CellHolderSg extends ViewHolder implements View.OnClickListener {
        public final CountdownViewUseList tv_title_blank;
        public final TextView tv_title_draw_img;
        public final TextView tv_title_order_state;
        public final TextView tv_order_price;
        public final Button bt_order_right;
        public final Button bt_order_left;
        private final ImageView iv_img;
        private final TextView tv_title;
        private final TextView pro_tag1;
        private final TextView pro_tag2;
        private final TextView pro_tag1_value;
        private final TextView pro_tag2_value;
        private final TextView tv_amount;
        private final TextView tv_price;
        private final TextView tv_title_blank_tag;
        public MyItemClickListener myItemClickListener;

        public CellHolderSg(View itemView, MyItemClickListener myItemClickListener) {
            super(itemView);
            this.myItemClickListener = myItemClickListener;
            itemView.setOnClickListener(this);
            tv_title_blank = (CountdownViewUseList) itemView.findViewById(R.id.tv_title_blank);
            tv_title_blank_tag = (TextView) itemView.findViewById(R.id.tv_title_blank_tag);
            tv_title_draw_img = (TextView) itemView.findViewById(R.id.tv_title_draw_img);
            tv_title_order_state = (TextView) itemView.findViewById(R.id.tv_title_order_state);
            tv_order_price = (TextView) itemView.findViewById(R.id.tv_order_price);
            bt_order_right = (Button) itemView.findViewById(R.id.bt_order_right);
            bt_order_left = (Button) itemView.findViewById(R.id.bt_order_left);

            iv_img = (ImageView) itemView.findViewById(R.id.iv_img);
            tv_title = (TextView) itemView.findViewById(R.id.tv_title);
            pro_tag1 = (TextView) itemView.findViewById(R.id.pro_tag1);
            pro_tag2 = (TextView) itemView.findViewById(R.id.pro_tag2);
            pro_tag1_value = (TextView) itemView.findViewById(R.id.pro_tag1_value);
            pro_tag2_value = (TextView) itemView.findViewById(R.id.pro_tag2_value);
            tv_amount = (TextView) itemView.findViewById(R.id.tv_amount);
            tv_price = (TextView) itemView.findViewById(R.id.tv_price);
        }

        @Override
        public void onClick(View v) {
            myItemClickListener.onItemClick(v, getAdapterPosition());
        }
    }

    private class CellHolderMulti extends ViewHolder implements View.OnClickListener {
        public final GridView gv_order_imag;
        private final TextView tv_title_blank_tag;
        public MyItemClickListener myItemClickListener;
        public final CountdownViewUseList tv_title_blank;
        public final TextView tv_title_draw_img;
        public final TextView tv_title_order_state;
        public final TextView tv_order_price;
        public final Button bt_order_right;
        public final Button bt_order_left;

        public CellHolderMulti(View itemView, MyItemClickListener myItemClickListener) {
            super(itemView);
            this.myItemClickListener = myItemClickListener;
            itemView.setOnClickListener(this);
            tv_title_blank = (CountdownViewUseList) itemView.findViewById(R.id.tv_title_blank);
            tv_title_blank_tag = (TextView) itemView.findViewById(R.id.tv_title_blank_tag);
            tv_title_draw_img = (TextView) itemView.findViewById(R.id.tv_title_draw_img);
            tv_title_order_state = (TextView) itemView.findViewById(R.id.tv_title_order_state);
            tv_order_price = (TextView) itemView.findViewById(R.id.tv_order_price);
            bt_order_right = (Button) itemView.findViewById(R.id.bt_order_right);
            bt_order_left = (Button) itemView.findViewById(R.id.bt_order_left);
            gv_order_imag = (GridView) itemView.findViewById(R.id.gv_order_imag);
            gv_order_imag.setPressed(false);
            gv_order_imag.setEnabled(false);
            gv_order_imag.setClickable(false);
        }
        @Override
        public void onClick(View v) {
            myItemClickListener.onItemClick(v, getAdapterPosition());
        }
    }

    class CommodityPullCallback implements PullCallback {
        @Override
        public void onLoadMore() {
            MyLog.e("AAA","onLoadMore---"+tempNextPage+"---nextPage--"+nextPage);
            if(tempNextPage==nextPage){
                tempNextPage=nextPage+1;
                loadData(nextPage);
            }else{
                mPullToLoadView.setComplete();
            }
        }

        @Override
        public void onRefresh() {


            if (CommonUtils.isNetworkConnected(CommodityOrderListActivity.this)) {
//                mAdapter.clear();
                isHasLoadedAll = false;
                tempNextPage=2;
                page = 1;
                loadData(page);
            } else {
                toast("网络不可用");
                mPullToLoadView.setComplete();
            }
        }

        @Override
        public boolean isLoading() {
            return isLoading;
        }

        @Override
        public boolean hasLoadedAllItems() {
            return isHasLoadedAll;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == COMMODITYORDERLISTACTIVITY_JUMP_GOODSRETRUNACTIVITY && resultCode == RESULT_OK) {
            mPullToLoadView.initNetLoad(CommodityOrderListActivity.this);
        }
        if (requestCode == COMMODITYORDERLISTACTIVITY_JUMP_PAYALLWAYACTIVITY && resultCode == RESULT_OK) {
            mPullToLoadView.initNetLoad(CommodityOrderListActivity.this);
        }
        if (requestCode == COMMODITYORDERLISTACTIVITY_JUMP && resultCode == RESULT_OK) {
            mPullToLoadView.initNetLoad(CommodityOrderListActivity.this);
        }
        if (requestCode == COMMODITYORDERLISTACTIVITY_JUMP_WRITEINFOFORGOODSRETURNS && resultCode == RESULT_OK) {
            mPullToLoadView.initNetLoad(CommodityOrderListActivity.this);
        }
        if (requestCode == GoodsEvaluateActivity.EVALUATECODE && resultCode == RESULT_OK) {
            mPullToLoadView.initNetLoad(CommodityOrderListActivity.this);
        }
        if (requestCode == CommodityOrderEvaluteDetailListActivity.REQUESTTOCOMMODITYORDEREVALUTEDETAILLIST&& resultCode == RESULT_OK) {
            mPullToLoadView.initNetLoad(CommodityOrderListActivity.this);
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
