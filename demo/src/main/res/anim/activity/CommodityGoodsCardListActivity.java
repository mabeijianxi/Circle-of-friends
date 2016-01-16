package anim.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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
import com.henanjianye.soon.communityo2o.common.Constant;
import com.henanjianye.soon.communityo2o.common.GoodsCardDataHelper;
import com.henanjianye.soon.communityo2o.common.MyLog;
import com.henanjianye.soon.communityo2o.common.enties.BaseBean;
import com.henanjianye.soon.communityo2o.common.enties.CommodityGoodsCartBean;
import com.henanjianye.soon.communityo2o.common.enties.JsonUtil;
import com.henanjianye.soon.communityo2o.common.util.CommonUtils;
import com.henanjianye.soon.communityo2o.common.util.MyTitleBarHelper;
import com.henanjianye.soon.communityo2o.common.util.NetWorkStateUtils;
import com.henanjianye.soon.communityo2o.common.util.SharedPreferencesUtil;
import com.henanjianye.soon.communityo2o.common.util.UserSharedPreferencesUtil;
import com.henanjianye.soon.communityo2o.common.view.DividerItemDecoration;
import com.henanjianye.soon.communityo2o.interf.MyItemClickListener;
import com.henanjianye.soon.communityo2o.interf.NetWorkCallback;
import com.henanjianye.soon.communityo2o.view.UserMenu;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/9/14.
 */
public class CommodityGoodsCardListActivity extends BaseActivity implements MyItemClickListener {

    private static final int GOODSCARTTITLE = 0x60001;
    private static final int GOODSCARTNORMAL = 0x60002;
    public static final String COMMODITY_GOODSCARD_LIST_ACTIVITY = "commoditygoodscardlistactivity";
    public static final String CURRENTSUBMMITCARDIDS = "currentsubmmitcardids";
    private PullToLoadView mPullToLoadView;
    private GoodsCardAdapter mAdapter;
    private boolean isLoading = false;
    private RecyclerView mRecyclerView;
    private UserMenu mMenu;
    private MyTitleBarHelper myTitleBarHelper;
    private int page = 1;
    private int orgId;
    private GoodsCardDataHelper goodsCardDataHelper;
    private boolean isHasLoadedAll = true;
    private CheckBox goods_card_check_all;
    private ArrayList<Boolean> selecteDeleteData;
    //选中的准备删除的数据
    private ArrayList<Integer> selectedDeleteData = new ArrayList<Integer>();
    private TextView tv_submmit_button;
    private TextView tag_totle_price;
    private TextView tag_totle_buzhong;
    private TextView tv_totle_price;
    private TextView tag_delete_all;
    private boolean currentControlEditeViewState;
    /**
     * 自动全选的标识
     */
    private boolean autoChecked = true;
    private float currentTotalPrece;
    private String currentSubmmitCardIds;
    private int[] currentUpdateNum = new int[2];
    //-1,全选。0  id  , 1,状态，用0 ，1表示
    private int[] currentUpdateCheck = new int[2];
    private boolean isSelecteByHandItem;
    public static final int Commodity_GoodsCard_ListActivity = 33456;

    @Override
    public int mysetContentView() {
        return R.layout.activity_commodity_goods_car;
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
        mPullToLoadView = (PullToLoadView) findViewById(R.id.commodity_large);
        mRecyclerView = mPullToLoadView.getRecyclerView();
        myTitleBarHelper = new MyTitleBarHelper(this, getWindow().getDecorView().findViewById(android.R.id.content));
        goods_card_check_all = (CheckBox) findViewById(R.id.goods_card_check_all);
        goods_card_check_all.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    autoChecked = false;
                }
                return false;
            }
        });
        goods_card_check_all.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!autoChecked) {
                    //处理check 事件
                    if (currentControlEditeViewState) {
                        initCheckBoxData(selecteDeleteData, isChecked);
                    } else {
                        //TOdo 请求网络 添加全部商品选中或取消
                        currentUpdateCheck[0] = -1;
                        currentUpdateCheck[1] = isChecked ? 1 : 0;
//                        isAllSelectDo = true;
                        goodsCardDataHelper.saveSelectState(getNetRequestHelper(CommodityGoodsCardListActivity.this), getAllGoosCartIds(), isChecked);
//                      initCheckBoxData(selecteData, isChecked);
                    }
                    mAdapter.notifyDataSetChanged();
                }
                autoChecked = true;
            }
        });
        tv_submmit_button = (TextView) findViewById(R.id.tv_submmit_button);
        tv_submmit_button.setOnClickListener(this);
        tag_totle_price = (TextView) findViewById(R.id.tag_totle_price);
        tag_totle_buzhong = (TextView) findViewById(R.id.tag_totle_buzhong);
        //显示总价格
        tv_totle_price = (TextView) findViewById(R.id.tv_totle_price);
        //编辑状态下的全选
        tag_delete_all = (TextView) findViewById(R.id.tag_delete_all);
        showEditeControlView(false);
    }

    private void initCheckBoxData(ArrayList<Boolean> mselecteDeleteData, boolean isChecked) {
        for (int i = 0; i < mselecteDeleteData.size(); i++) {
            if (mselecteDeleteData.get(i) != null) {
                mselecteDeleteData.set(i, isChecked);
            }
        }
    }

    private void showEditeControlView(Boolean isShowEdite) {
        tv_submmit_button.setTag(isShowEdite);
        currentControlEditeViewState = isShowEdite;
        if (isShowEdite) {
            tag_delete_all.setVisibility(View.VISIBLE);
            tag_totle_price.setVisibility(View.GONE);
            tag_totle_buzhong.setVisibility(View.GONE);
            tv_totle_price.setVisibility(View.GONE);
            tv_submmit_button.setText("删除");
            myTitleBarHelper.setRightText("取消");
            initCheckBoxData(selecteDeleteData, false);
            if (mAdapter != null) {
                mAdapter.notifyDataSetChanged();
            }
        } else {
            tag_delete_all.setVisibility(View.GONE);
            tag_totle_price.setVisibility(View.VISIBLE);
            tag_totle_buzhong.setVisibility(View.VISIBLE);
            tv_totle_price.setVisibility(View.VISIBLE);
            tv_submmit_button.setText("结算");
            myTitleBarHelper.setRightText("编辑");
            if (mAdapter != null) {
                mAdapter.notifyDataSetChanged();
                resetTotalPriceView(true);
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
            case R.id.tv_title_bar_right:
                showEditeControlView(!currentControlEditeViewState);
                break;
            case R.id.tv_submmit_button:
                Object object = v.getTag();
                if (object != null) {
                    if (Boolean.parseBoolean(object.toString())) {
                        String ids = getDeleteGoosCartIds();
                        if (!TextUtils.isEmpty(ids)) {
                            goodsCardDataHelper.deleteGoodsCard(getNetRequestHelper(CommodityGoodsCardListActivity.this), ids);
                        } else {
                            toast("请选择需要删除的商品");
                        }
                    } else {
                        currentSubmmitCardIds = getSelectGoosCartIds();
                        if (!TextUtils.isEmpty(currentSubmmitCardIds)) {
                            goodsCardDataHelper.submmitGoodsCard(getNetRequestHelper(CommodityGoodsCardListActivity.this), currentSubmmitCardIds, null, null);
                        } else {
                            toast("请选择需要结算的订单");
                        }
                    }
                }
                break;
        }
    }

    private String getAllGoosCartIds() {
        String goodsCartIds = "";
        List<Object> objects = mAdapter.getmList();
        for (Object object : objects) {
            if (object instanceof CommodityGoodsCartBean.GoodsCart) {
                CommodityGoodsCartBean.GoodsCart goodsCart = ((CommodityGoodsCartBean.GoodsCart) object);
                if (goodsCart.isEnough == 1 && goodsCart.goods.goodsStatus == 0) {
                    goodsCartIds = goodsCartIds + goodsCart.goodsCartId + ",";
                }
            }
        }
        return goodsCartIds;
    }

    private String getSelectGoosCartIds() {
        String goodsCartIds = "";
        List<Object> objects = mAdapter.getmList();
        for (Object object : objects) {
            if (object instanceof CommodityGoodsCartBean.GoodsCart) {
                CommodityGoodsCartBean.GoodsCart goodsCart = ((CommodityGoodsCartBean.GoodsCart) object);
                if (goodsCart.isSelected == 1) {
                    if (goodsCart.isEnough == 1 && goodsCart.goods.goodsStatus == 0) {
                        goodsCartIds = goodsCartIds + goodsCart.goodsCartId + ",";
                    }
                }
            }
        }
        return goodsCartIds;
    }

    private String getDeleteGoosCartIds() {
        String goodsCartIds = "";
        if (selecteDeleteData.contains(true)) {
            for (int i = 0; i < selecteDeleteData.size(); i++) {
                if (selecteDeleteData.get(i) == null ? false : selecteDeleteData.get(i)) {
                    selectedDeleteData.add(i);
                    CommodityGoodsCartBean.GoodsCart goodsCart = (CommodityGoodsCartBean.GoodsCart) mAdapter.getmList().get(i);
                    goodsCartIds = goodsCartIds + goodsCart.goodsCartId + ",";
                }
            }
        }
        return goodsCartIds;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAdapter != null && mAdapter.getmList().size() > 0) {
            SharedPreferencesUtil.saveIntData(CommodityGoodsCardListActivity.this, Constant.ShouYeUrl.SHAOPPING_NUM, getGoodsNum(mAdapter.getmList()));
        }
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    private void initEvent() {
        myTitleBarHelper.setOnclickListener(this);
        myTitleBarHelper.setRightImag(R.drawable.title_commodity_goodscard);
        LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mPullToLoadView.setLayoutManager(manager);
    }

    private void initProcess() {
        tv_totle_price.setText("¥" + CommonUtils.floatToTowDecima(currentTotalPrece));
        selecteDeleteData = new ArrayList<Boolean>();
        orgId = UserSharedPreferencesUtil.getUserInfo(this, UserSharedPreferencesUtil.ORGID, -1);
        if (orgId == -1) {
            toast("小区id不能为空");
            return;
        }
        myTitleBarHelper.setMiddleText("购物车");
        myTitleBarHelper.setRightText("编辑");

//        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(
                this, DividerItemDecoration.HORIZONTAL_LIST));
        mAdapter = new GoodsCardAdapter();
        mAdapter.setOnItemClickListener(this);
        mRecyclerView.setAdapter(mAdapter);
        //设置分隔线，和listView 不同注意
        mPullToLoadView.isLoadMoreEnabled(true);
        mPullToLoadView.setPullCallback(new CommodityPullCallback());
        String resultCach = null;
        parseDate(resultCach, true);
        mPullToLoadView.initNetLoad(this);
    }

    /**
     * @param page 页码
     */
    private void loadData(final int page) {
        if (goodsCardDataHelper == null) {
            goodsCardDataHelper = new GoodsCardDataHelper(this);
        }
        goodsCardDataHelper.goodsCardInfor(getNetRequestHelper(this).isShowProgressDialog(false));
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
                if (Constant.MallUrl.COMMODITY_CART_LIST.equals(requestTag)) {
                    parseDate(responseInfo.result, false);

                    resetTotalPriceView(true);
                }
                if (Constant.MallUrl.COMMODITY_CART_UPDATE.equals(requestTag)) {
                    BaseBean<Object> baseBean = JsonUtil.jsonArray(responseInfo.result, Object.class);
                    if (baseBean.code == 100) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (mAdapter.getmList().get(currentUpdateNum[0]) instanceof CommodityGoodsCartBean.GoodsCart) {
                                    CommodityGoodsCartBean.GoodsCart goodsCart = (CommodityGoodsCartBean.GoodsCart) mAdapter.getmList().get(currentUpdateNum[0]);
                                    goodsCart.goodsCount = currentUpdateNum[1];
                                    mAdapter.notifyItemChanged(currentUpdateNum[0]);
                                    resetTotalPriceView(false);
                                }
                            }
                        });
                    } else {
                        //有时小于0 报错
                        if(currentUpdateNum[1]>0){
                            toast(baseBean.msg);
                        }
                    }
                }
                if (Constant.MallUrl.COMMODITY_CART_DELETE.equals(requestTag)) {
                    BaseBean<CommodityGoodsCartBean> baseBean = JsonUtil.jsonArray(responseInfo.result, CommodityGoodsCartBean.class);
                    List<CommodityGoodsCartBean> commodityGoodsCartBeans = baseBean.data;
                    List<Object> positionData = commodityGoodsCartBeansToPositionDataNoclear(commodityGoodsCartBeans);
                    if (baseBean.code == 100) {
                        if (mAdapter != null) {
                            mAdapter.clear();
                            SharedPreferencesUtil.saveStringData(CommodityGoodsCardListActivity.this, Constant.CachTag.APP_COMMDITY_GOODS_CART_LIST, responseInfo.result);
                        }
                        isLoading = false;
                        //放到adapter更新数据之前
                        updateGoodsCartInforAfterDelete();
                        if (positionData != null && positionData.size() > 0) {
                            mAdapter.add(positionData);
                        }
                        SharedPreferencesUtil.saveIntData(CommodityGoodsCardListActivity.this, Constant.ShouYeUrl.SHAOPPING_NUM, getGoodsNum(mAdapter.getmList()));
                        showEditeControlView(!currentControlEditeViewState);
                        resetTotalPriceView(true);
                    } else {
                        selectedDeleteData.clear();
                        toast(baseBean.msg);
                    }
                }
                if (Constant.MallUrl.COMMODITY_CART_SUBMMIT.equals(requestTag)) {
                    BaseBean<Object> baseBean = JsonUtil.jsonArray(responseInfo.result, Object.class);
                    if (baseBean.code == 100) {
                        SharedPreferencesUtil.saveStringData(CommodityGoodsCardListActivity.this, Constant.CachTag.APP_COMMDITY_GOODS_CART_SUBMMIT, responseInfo.result);
                        Intent intent = new Intent(CommodityGoodsCardListActivity.this, CommoditySettlementActivity.class);
                        intent.putExtra(CURRENTSUBMMITCARDIDS, currentSubmmitCardIds);
                        startActivityForResult(intent, Commodity_GoodsCard_ListActivity);
                    } else {
                        toast(baseBean.msg);
                    }
                }
                if (Constant.MallUrl.COMMODITY_SAVE_SELECT_STATE.equals(requestTag)) {
                    BaseBean<Object> baseBean = JsonUtil.jsonArray(responseInfo.result, Object.class);

                    if (baseBean.code == 100) {
                        int pos = currentUpdateCheck[0];
                        int ischecked = currentUpdateCheck[1];
                        MyLog.e("AAA", "pos---" + pos + "--ischecked--" + ischecked);
                        if (pos == -1) {
                            setSelectedDateState(true, -1, ischecked == 1 ? true : false);
                            mAdapter.notifyDataSetChanged();
                        } else {
                            setSelectedDateState(false, pos, ischecked == 1 ? true : false);
                            mAdapter.notifyItemChanged(pos);
                        }
                        resetTotalPriceView(true);

                    }
//                    isAllSelectDo=false;
                }
                if (mPullToLoadView != null) {
                    mPullToLoadView.setComplete();
                }
            }

            @Override
            public void onFailure(HttpException error, String msg, String requestTag) {
                MyLog.e("AAA", "-----onFailure----------");
                mPullToLoadView.post(new Runnable() {
                    @Override
                    public void run() {
                        mPullToLoadView.setComplete();
                    }
                });
                NetWorkStateUtils.errorNetMes(CommodityGoodsCardListActivity.this);
            }
        };
    }

    private void resetTotalPriceView(boolean isControlCheck) {
        if(isControlCheck){
            if (isAllDateSelecte()) {
                goods_card_check_all.setChecked(true);
            } else {
                goods_card_check_all.setChecked(false);
            }
        }

        currentTotalPrece = getTotalPrice();
        tv_totle_price.setText("¥" + CommonUtils.floatToTowDecima(currentTotalPrece));
    }

    /**
     * 更新购物车选择列表去掉删除项
     */
    private void updateGoodsCartInforAfterDelete() {
//        for (int i = 0; i < selectedDeleteData.size(); i++) {
//            if (selectedDeleteData.get(i) < selecteData.size()) {
//                selecteData.remove((int) selectedDeleteData.get(i));
//            }
//        }
        this.selectedDeleteData.clear();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (currentControlEditeViewState && keyCode == KeyEvent.KEYCODE_BACK) {
            showEditeControlView(!currentControlEditeViewState);
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * @param str
     * @param isCach 解析请求的购物车列表
     */
    private synchronized void parseDate(String str, boolean isCach) {
        if (str == null) {
            return;
        }
        selecteDeleteData.clear();
        BaseBean<CommodityGoodsCartBean> baseBean = JsonUtil.jsonArray(str, CommodityGoodsCartBean.class);
        List<CommodityGoodsCartBean> commodityGoodsCartBeans = baseBean.data;
        List<Object> positionData = commodityGoodsCartBeansToPositionData(commodityGoodsCartBeans);
        if (baseBean.code == 100) {
            if (!isCach) {
                if (mAdapter != null) {
                    mAdapter.clear();
                    SharedPreferencesUtil.saveStringData(this, Constant.CachTag.APP_COMMDITY_GOODS_CART_LIST, str);
                }
            } else {
                if (mAdapter != null) {
                    mAdapter.clear();
                }
            }
//            mPullToLoadView.setComplete();
            isLoading = false;
            if (positionData != null && positionData.size() > 0) {
                mAdapter.add(positionData);
            }
//            if (commodityListBeans.totalCount > commodityListBeans.pageNo * commodityListBeans.pageSize) {
//                nextPage = commodityListBeans.pageNo + 1;
//                isHasLoadedAll = false;
//            } else {
//                isHasLoadedAll = true;
//            }
        } else {
            mPullToLoadView.setComplete();
            toast(baseBean.msg);
        }
    }

    /**
     * @param commodityGoodsCartBeans
     * @return 将列表转换成list列表
     */
    private List<Object> commodityGoodsCartBeansToPositionData(List<CommodityGoodsCartBean> commodityGoodsCartBeans) {
        List<Object> positionData = new ArrayList<Object>();
        if (commodityGoodsCartBeans == null) {
            return positionData;
        }
        for (int i = 0; i < commodityGoodsCartBeans.size(); i++) {
            positionData.add(commodityGoodsCartBeans.get(i));
            selecteDeleteData.add(null);
            for (int j = 0; j < commodityGoodsCartBeans.get(i).goodsCartList.size(); j++) {
                positionData.add(commodityGoodsCartBeans.get(i).goodsCartList.get(j));
                selecteDeleteData.add(false);
            }
        }
        return positionData;
    }

    /**
     * @param commodityGoodsCartBeans
     * @return 将列表转换成list列表 不清空选中列表
     */
    private List<Object> commodityGoodsCartBeansToPositionDataNoclear(List<CommodityGoodsCartBean> commodityGoodsCartBeans) {
        List<Object> positionData = new ArrayList<Object>();
        if (commodityGoodsCartBeans == null) {
            return positionData;
        }
        selecteDeleteData.clear();
        for (int i = 0; i < commodityGoodsCartBeans.size(); i++) {
            positionData.add(commodityGoodsCartBeans.get(i));
            selecteDeleteData.add(null);
            for (int j = 0; j < commodityGoodsCartBeans.get(i).goodsCartList.size(); j++) {
                positionData.add(commodityGoodsCartBeans.get(i).goodsCartList.get(j));
                selecteDeleteData.add(false);
            }
        }
        return positionData;
    }

    @Override
    public void onItemClick(View view, int postion) {

        List<Object> objects = mAdapter.getmList();
        if (objects != null && objects.size() > postion) {
            Object object = objects.get(postion);
            //TODO  订单点击跳转到商品详情状态
            if (object instanceof CommodityGoodsCartBean.GoodsCart) {
                CommodityGoodsCartBean.GoodsCart.Goods goods = ((CommodityGoodsCartBean.GoodsCart) object).goods;
                Intent intent = new Intent(this, CommodityDetailInforActivity.class);
                intent.setAction(COMMODITY_GOODSCARD_LIST_ACTIVITY);
                intent.putExtra("CommodityGoodsCartBean.GoodsCart.Goods", goods);
                startActivity(intent);
            }
//            MyLog.e("AAA", "commodityBean.url---------" + commodityBean.url);
//            Intent intent = new Intent(this, CommodityDetailInforActivity.class);
//            intent.putExtra("CommodityBean", commodityBean);
//            startActivity(intent);
        }

    }

    private class GoodsCardAdapter extends RecyclerView.Adapter<ViewHolder> {

        private List<Object> mList;
        private MyItemClickListener myItemClickListener;

        public GoodsCardAdapter() {
            mList = new ArrayList<>();
        }

        public List<Object> getmList() {
            return mList;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            if (i == GOODSCARTTITLE) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.activity_commodity_goods_car_header_item, viewGroup, false);
                return new CellHolderHeader(view, myItemClickListener);
            } else {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.activity_commodity_goods_car_item, viewGroup, false);
                return new CellHolder(view, myItemClickListener);
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

            if (holder instanceof CellHolderHeader) {
                CellHolderHeader cellHolderHeader = (CellHolderHeader) holder;
                CommodityGoodsCartBean.Store store = ((CommodityGoodsCartBean) (getmList().get(i))).store;
                if (store != null) {
                    cellHolderHeader.tv_goodscar_community_header_name.setText(store.storeName);
                }
            } else {
                CommodityGoodsCartBean.GoodsCart goodsCart = ((CommodityGoodsCartBean.GoodsCart) (getmList().get(i)));
                CellHolder cellHolder = (CellHolder) holder;
                cellHolder.tv_goodscar_title.setText(goodsCart.goods.name);
                cellHolder.tv_goodscar_price.setText("¥" + CommonUtils.floatToTowDecima(goodsCart.goodsPrice));
                cellHolder.tv_goodscar_second_title.setText(goodsCart.goodsSpecInfo);
                ImageLoader instance = ImageLoader.getInstance();
                DisplayImageOptions build = new DisplayImageOptions.Builder()
                        .showImageOnFail(R.drawable.ic_launcher_default)
                        .showImageForEmptyUri(R.drawable.ic_launcher_default).build();
                if (goodsCart.goods.mainPhoto.smallPicUrl != null) {
                    instance.displayImage(goodsCart.goods.mainPhoto.smallPicUrl, cellHolder.iv_goodscar_img, build);
                }
                cellHolder.et_props_goods_num.setText(goodsCart.goodsCount + "");

                if (currentControlEditeViewState) {
                    if (selecteDeleteData.size() > i) {
                        cellHolder.cb_goodcar_check.setChecked(selecteDeleteData.get(i));
                    }
                } else {
//                    if (selecteData.size() > i) {
                    cellHolder.cb_goodcar_check.setChecked(goodsCart.isSelected == 1);
//                    }
                }
                cellHolder.cb_goodcar_check.setTag(i);
                cellHolder.cb_goodcar_check.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            isSelecteByHandItem = true;
                        }
                        return false;
                    }
                });
                cellHolder.cb_goodcar_check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        Object object = buttonView.getTag();
                        if (object != null) {
                            int pos = Integer.parseInt(object.toString());
                            if (currentControlEditeViewState) {
                                if (pos < selecteDeleteData.size()) {
                                    selecteDeleteData.set(pos, isChecked);
                                    if (!selecteDeleteData.contains(!isChecked)) {
                                        goods_card_check_all.setChecked(isChecked);
                                    }
                                }
                            } else {
                                if (isSelecteByHandItem) {
                                    CommodityGoodsCartBean.GoodsCart goodsCart1 = (CommodityGoodsCartBean.GoodsCart) getmList().get(pos);
                                    currentUpdateCheck[0] = pos;
                                    currentUpdateCheck[1] = isChecked ? 1 : 0;
                                    if (goodsCart1.isEnough == 1 && goodsCart1.goods.goodsStatus == 0) {
                                        goodsCardDataHelper.saveSelectState(getNetRequestHelper(CommodityGoodsCardListActivity.this), goodsCart1.goodsCartId + "", isChecked);
                                    } else {
                                        toast("商品不支持购买");
                                    }
                                }
                                isSelecteByHandItem = false;
                            }
                        }

                    }
                });
//设置货品数量控制
                cellHolder.tv_props_goods_num_minus.setEnabled(goodsCart.goodsCount > 1);
                cellHolder.tv_props_goods_num_minus.setTag(i);
                cellHolder.tv_props_goods_num_minus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //  请求添加件数，添加成功则刷新数据
                        Object object = v.getTag();
                        if (object != null) {
                            int pos = Integer.parseInt(object.toString());
                            if (getmList().get(pos) instanceof CommodityGoodsCartBean.GoodsCart) {
                                CommodityGoodsCartBean.GoodsCart goodsCart = (CommodityGoodsCartBean.GoodsCart) getmList().get(pos);
                                currentUpdateNum[0] = pos;
                                currentUpdateNum[1] = goodsCart.goodsCount - 1;
                                goodsCardDataHelper.updateGoodsCardNum(getNetRequestHelper(CommodityGoodsCardListActivity.this), goodsCart.goodsCartId, goodsCart.goodsCount - 1);
                            }

                        }
                    }
                });
//                cellHolder.tv_props_goods_num_plus.setEnabled(goodsCart.goodsCount < goodsCart.goodsSpecInventory);
                cellHolder.tv_props_goods_num_plus.setTag(i);
                cellHolder.tv_props_goods_num_plus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //  请求添加件数，添加成功则刷新数据
                        Object object = v.getTag();
                        if (object != null) {
                            int pos = Integer.parseInt(object.toString());
                            if (getmList().get(pos) instanceof CommodityGoodsCartBean.GoodsCart) {
                                CommodityGoodsCartBean.GoodsCart goodsCart = (CommodityGoodsCartBean.GoodsCart) getmList().get(pos);
                                //判断当前件数是否大于库存数
                                if (goodsCart.goodsCount < goodsCart.goodsSpecInventory) {
                                    currentUpdateNum[0] = pos;
                                    currentUpdateNum[1] = goodsCart.goodsCount + 1;
                                    goodsCardDataHelper.updateGoodsCardNum(getNetRequestHelper(CommodityGoodsCardListActivity.this), goodsCart.goodsCartId, goodsCart.goodsCount + 1);
                                } else {
                                    toast("商品库存不足");
                                }
                            }
                        }
                    }
                });

//                MyLog.e("AAA", i + "---goodsCart.goods.goodsStatus--" + goodsCart.goods.goodsStatus + "----goodsCart.isEnough == 0---" + goodsCart.isEnough);
//                if (!currentControlEditeViewState) {
                if (goodsCart.goods.goodsStatus == 0) {
                    if (goodsCart.isEnough == 0) {
                        cellHolder.tv_goodscar_tag.setVisibility(View.VISIBLE);
                        if (!currentControlEditeViewState) {
                            cellHolder.cb_goodcar_check.setChecked(false);
                            cellHolder.cb_goodcar_check.setEnabled(false);
                        } else {
                            cellHolder.cb_goodcar_check.setEnabled(true);
                        }
                        cellHolder.tv_goodscar_tag.setText("无货");
                        cellHolder.tv_goodscar_tag.setTextColor(Color.parseColor("#505050"));
                    } else {
                        cellHolder.cb_goodcar_check.setEnabled(true);
                        cellHolder.tv_goodscar_tag.setVisibility(View.GONE);
                    }
                } else {
                    cellHolder.tv_goodscar_tag.setVisibility(View.VISIBLE);
                    if (!currentControlEditeViewState) {
                        cellHolder.cb_goodcar_check.setEnabled(false);
                        cellHolder.cb_goodcar_check.setChecked(false);
                    } else {
                        cellHolder.cb_goodcar_check.setEnabled(true);
                    }
                    cellHolder.tv_goodscar_tag.setTextColor(Color.parseColor("#505050"));
                    cellHolder.tv_goodscar_tag.setText("下架");
                }
//                }
            }
//            //调整图片的高根据屏幕宽
//            ViewGroup.LayoutParams layoutParams = holder.iv_commodity_large_img.getLayoutParams();
//            layoutParams.width = CommonUtils.getScreenSizeWidth(CommodityGoodsCardListActivity.this);
//            layoutParams.height = (int) (layoutParams.width * 0.8);
//            holder.iv_commodity_large_img.setLayoutParams(layoutParams);
//            if (mList.get(i).mainPhoto != null) {
//                instance.displayImage(mList.get(i).mainPhoto.middlePicUrl, holder.iv_commodity_large_img, build);
//            }
        }

        public void add(List<Object> beans) {
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
                Object object = mList.get(position);
                if (object instanceof CommodityGoodsCartBean) {
                    return GOODSCARTTITLE;
                } else {
                    return GOODSCARTNORMAL;
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
            return mList.size();
        }
    }

    private float getTotalPrice() {
        List<Object> objects = mAdapter.getmList();
        float temp = 0f;
        for (int i = 0; i < objects.size(); i++) {
            try {
                CommodityGoodsCartBean.GoodsCart goodsCart = (CommodityGoodsCartBean.GoodsCart) objects.get(i);
                if (goodsCart.isSelected == 1 && (goodsCart.isEnough == 1 && goodsCart.goods.goodsStatus == 0)) {
                    temp = temp + goodsCart.goodsPrice * goodsCart.goodsCount;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return temp;
    }

    private class CellHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public MyItemClickListener myItemClickListener;
        public ImageView iv_goodscar_img;
        public TextView tv_goodscar_tag;
        //        public TextView tv_goodscar_community_name;
        public TextView tv_goodscar_title;
        public TextView tv_goodscar_price;
        public TextView tv_props_goods_num_minus;
        public TextView et_props_goods_num;
        public TextView tv_props_goods_num_plus;
        public CheckBox cb_goodcar_check;
        public TextView tv_goodscar_second_title;

        public CellHolder(View itemView, MyItemClickListener myItemClickListener) {
            super(itemView);
            this.myItemClickListener = myItemClickListener;
            itemView.setOnClickListener(this);
            cb_goodcar_check = (CheckBox) itemView.findViewById(R.id.cb_goodcar_check);
            iv_goodscar_img = (ImageView) itemView.findViewById(R.id.iv_goodscar_img);
            tv_goodscar_tag = (TextView) itemView.findViewById(R.id.tv_goodscar_tag);
//            tv_goodscar_community_name = (TextView) itemView.findViewById(R.id.tv_goodscar_community_name);
            tv_goodscar_title = (TextView) itemView.findViewById(R.id.tv_goodscar_title);
            tv_goodscar_second_title = (TextView) itemView.findViewById(R.id.tv_goodscar_second_title);
            tv_goodscar_price = (TextView) itemView.findViewById(R.id.tv_goodscar_price);
            tv_props_goods_num_minus = (TextView) itemView.findViewById(R.id.tv_props_goods_num_minus);
            et_props_goods_num = (TextView) itemView.findViewById(R.id.et_props_goods_num);
            tv_props_goods_num_plus = (TextView) itemView.findViewById(R.id.tv_props_goods_num_plus);
        }

        @Override
        public void onClick(View v) {
            myItemClickListener.onItemClick(v, getAdapterPosition());
        }
    }


    private class CellHolderHeader extends RecyclerView.ViewHolder implements View.OnClickListener {
        public MyItemClickListener myItemClickListener;
        public TextView tv_goodscar_community_header_name;

        public CellHolderHeader(View itemView, MyItemClickListener myItemClickListener) {
            super(itemView);
            this.myItemClickListener = myItemClickListener;
            itemView.setOnClickListener(this);
            tv_goodscar_community_header_name = (TextView) itemView.findViewById(R.id.tv_goodscar_community_header_name);
        }

        @Override
        public void onClick(View v) {
            myItemClickListener.onItemClick(v, getAdapterPosition());
        }
    }

    class CommodityPullCallback implements PullCallback {
        @Override
        public void onLoadMore() {
//            loadData(nextPage);
        }

        @Override
        public void onRefresh() {


//            mAdapter.clear();
//            isHasLoadedAll = false;
            if (CommonUtils.isNetworkConnected(CommodityGoodsCardListActivity.this)) {
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

    /**
     * @return 判断是否已经是所有选中
     */
    public boolean isAllDateSelecte() {
        List<Object> objects = mAdapter.getmList();
        for (Object object : objects) {
            if (object instanceof CommodityGoodsCartBean.GoodsCart) {
                CommodityGoodsCartBean.GoodsCart goodsCart = ((CommodityGoodsCartBean.GoodsCart) object);
                if (goodsCart.isSelected == 0 && (goodsCart.isEnough == 1 && goodsCart.goods.goodsStatus == 0)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean setSelectedDateState(boolean isAll, int pos, boolean ischecked) {
        List<Object> objects = mAdapter.getmList();
        if (isAll) {
            for (Object object : objects) {
                if (object instanceof CommodityGoodsCartBean.GoodsCart) {
                    //TOdo
                    CommodityGoodsCartBean.GoodsCart goodsCart = (CommodityGoodsCartBean.GoodsCart) object;
                    if ((goodsCart.isEnough == 1 && goodsCart.goods.goodsStatus == 0)) {
                        goodsCart.isSelected = (ischecked ? 1 : 0);
                    }
                }
            }
        } else {
            if (objects.size() > pos) {
                if (objects.get(pos) instanceof CommodityGoodsCartBean.GoodsCart) {
                    CommodityGoodsCartBean.GoodsCart goodsCart = ((CommodityGoodsCartBean.GoodsCart) objects.get(pos));
                    goodsCart.isSelected = (ischecked ? 1 : 0);
                }
            } else {
                return false;
            }
        }
        return false;
    }


    public int getGoodsNum(List<Object> list) {
        int i = 0;
        for (Object object : list) {
            if (object instanceof CommodityGoodsCartBean.GoodsCart) {
                i++;
            }
        }
        return i;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == Commodity_GoodsCard_ListActivity) && (resultCode == RESULT_OK)) {
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
