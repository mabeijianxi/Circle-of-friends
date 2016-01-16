package anim.activity;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
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
import com.henanjianye.soon.communityo2o.common.MallDataHelper;
import com.henanjianye.soon.communityo2o.common.MyLog;
import com.henanjianye.soon.communityo2o.common.enties.BaseBean;
import com.henanjianye.soon.communityo2o.common.enties.BaseDataBean;
import com.henanjianye.soon.communityo2o.common.enties.CommodityBean;
import com.henanjianye.soon.communityo2o.common.enties.CommodityListBeans;
import com.henanjianye.soon.communityo2o.common.enties.CommoditySortBean;
import com.henanjianye.soon.communityo2o.common.enties.JsonUtil;
import com.henanjianye.soon.communityo2o.common.util.CommonUtils;
import com.henanjianye.soon.communityo2o.common.util.JianPanUtils;
import com.henanjianye.soon.communityo2o.common.util.MyTitleBarHelper;
import com.henanjianye.soon.communityo2o.common.util.NetWorkStateUtils;
import com.henanjianye.soon.communityo2o.common.util.SharedPreferencesUtil;
import com.henanjianye.soon.communityo2o.common.util.UserSharedPreferencesUtil;
import com.henanjianye.soon.communityo2o.common.view.BadgeView;
import com.henanjianye.soon.communityo2o.interf.MyItemClickListener;
import com.henanjianye.soon.communityo2o.interf.NetWorkCallback;
import com.henanjianye.soon.communityo2o.view.PopMenu;
import com.henanjianye.soon.communityo2o.view.UserMenu;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.ValueAnimator;
import com.umeng.analytics.MobclickAgent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/9/14.
 */
public class CommodityListActivity extends BaseActivity implements MyItemClickListener {
    private PullToLoadView mPullToLoadView;
    private SimpleAdapter mAdapter;
    private boolean isLoading = false;
    private boolean isHasLoadedAll = false;
    private int nextPage;
    private RecyclerView mRecyclerView;
    private TextView tv_commodity_sort;
    private TextView tv_commodity_price_order;
    private TextView tv_commodity_num_order;
    private UserMenu mMenu;
    private MyTitleBarHelper myTitleBarHelper;
    private EditText searchBar;
    private MallDataHelper mallDataHelper;
    private int page = 1;
    private final int LIST_COMMODITY_ALL = 0;
    private final int LIST_COMMODITY_SORT = 1;
    private final int LIST_COMMODITY_KEY = 2;
    private final int LIST_COMMODITY_ORDER = 3;
    private int listState = LIST_COMMODITY_ALL;
    private int listOrderState = LIST_COMMODITY_ALL;
    private String keyWord;
    private int orgId;
    private List<CommoditySortBean> commoditySortBeans;
    private int currentClassID = 65681;
    private int currentClassType = 0;
    private Integer currentPriceOrder;
    private Integer currentSalesOrder;
    private ArrayList<CommodityBean> commodityBeans;
    private String ACTION_ACYIVITY;
    private ImageView move_to_top;
    private BadgeView goodsCard;
    private int isFirst = 0;
    private ImageView iv_title_bar_right;
    private LinearLayoutManager manager;
    private int tempNextPage=2;

    @Override
    public int mysetContentView() {
        return R.layout.activity_commodity_list;
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
        iv_title_bar_right = (ImageView) findViewById(R.id.iv_title_bar_right);
        mPullToLoadView = (PullToLoadView) findViewById(R.id.commodity_list);
        mRecyclerView = mPullToLoadView.getRecyclerView();
        tv_commodity_sort = (TextView) findViewById(R.id.tv_commodity_sort);
        tv_commodity_price_order = (TextView) findViewById(R.id.tv_commodity_price_order);
        tv_commodity_num_order = (TextView) findViewById(R.id.tv_commodity_num_order);
        tv_commodity_sort.setOnClickListener(this);
        tv_commodity_price_order.setOnClickListener(this);
        tv_commodity_num_order.setOnClickListener(this);
        myTitleBarHelper = new MyTitleBarHelper(this, getWindow().getDecorView().findViewById(android.R.id.content));
        searchBar = myTitleBarHelper.getSearchBar();
        tv_commodity_sort.setTextColor(getResources().getColor(R.color.indicator_selector_color));

        move_to_top = (ImageView) findViewById(R.id.move_to_top);
//        if(page <= 1){
//            move_to_top.setVisibility(View.GONE);
//        }
        move_to_top.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.tv_commodity_sort:
                showPopuMenu(v);
                break;
            case R.id.tv_commodity_price_order:
                //T按价格进行排序
                if (currentPriceOrder == null) {
                    currentPriceOrder = 0;
                } else if (currentPriceOrder == 1) {
                    currentPriceOrder = 0;
                } else if (currentPriceOrder == 0) {
                    currentPriceOrder = 1;
                }
                currentSalesOrder = null;
                setOrderViewState(tv_commodity_price_order, currentPriceOrder);
                setOrderViewState(tv_commodity_num_order, currentSalesOrder);
                loadData(page, LIST_COMMODITY_ORDER, false);
//                    mallDataHelper.commodityList(getNetRequestHelper(this).isShowProgressDialog(false), orgId, page);
                break;
            case R.id.tv_commodity_num_order:
                //按照数量排序
                if (currentSalesOrder == null) {
                    currentSalesOrder = 0;
                } else if (currentSalesOrder == 0) {
                    currentSalesOrder = 1;
                } else if (currentSalesOrder == 1) {
                    currentSalesOrder = 0;
                }
                currentPriceOrder = null;
                setOrderViewState(tv_commodity_price_order, currentPriceOrder);
                setOrderViewState(tv_commodity_num_order, currentSalesOrder);
                loadData(page, LIST_COMMODITY_ORDER, false);
//                    mallDataHelper.commodityList(getNetRequestHelper(this).isShowProgressDialog(false), orgId, page);
                break;
            case R.id.iv_title_bar_left:
                finish();
                break;
            case R.id.iv_title_bar_right:
                //TODO 购物车
                if (isFirst == 0) {

                    CommonUtils.startGoodsCartActivity(CommodityListActivity.this);
                } else {
                    //TODO...搜索
                    keyWord = searchBar.getText().toString();
                    loadData(page, LIST_COMMODITY_KEY, false);
                }
//                startActivity(new Intent(CommodityListActivity.this,CommodityGoodsCardListActivity.class));
                break;
            case R.id.rl_title_bar_search:
                //TODO
                break;
            case R.id.move_to_top:
//                回到顶部的操作
                mRecyclerView.smoothScrollToPosition(0);
                break;

        }
    }

    private void setOrderViewState(TextView tex, Integer orderState) {

        Drawable img = null;
        Resources res = getResources();
        if (orderState == null) {
            img = res.getDrawable(R.mipmap.siftnecessary_arrowicon_normal);
            tex.setTextColor(Color.parseColor("#000000"));
        } else if (orderState == 0) {
            img = res.getDrawable(R.mipmap.siftnecessary_arrowicon_highlight_down);
            tex.setTextColor(getResources().getColor(R.color.indicator_selector_color));
        } else if (orderState == 1) {
            img = res.getDrawable(R.mipmap.siftnecessary_arrowicon_highlight_up);
            tex.setTextColor(getResources().getColor(R.color.indicator_selector_color));
        }
        if (img != null) {
//调用setCompoundDrawables时，必须调用Drawable.setBounds()方法,否则图片不显示
            img.setBounds(0, 0, img.getMinimumWidth(), img.getMinimumHeight());
            tex.setCompoundDrawables(null, null, img, null); //设置左图标
        }
    }

    private void initEvent() {
        myTitleBarHelper.setOnclickListener(this);
        myTitleBarHelper.setRightImag(R.drawable.title_commodity_goodscard);

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                MyLog.e("Editable s", s.toString());
                if (!TextUtils.isEmpty(s.toString())) {

                    isFirst++;
                    MyLog.e("isFirst", isFirst + "");
                    if (isFirst == 1) {

                        myTitleBarHelper.setRightImag(R.drawable.youpin_sousuo_button);
                        ScaleAnimation scaleAnimation = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f,
                                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                        scaleAnimation.setDuration(500);
                        iv_title_bar_right.startAnimation(scaleAnimation);
                    }


                } else {
                    isFirst = 0;
                    myTitleBarHelper.setRightImag(R.drawable.title_commodity_goodscard);
                }

            }
        });

        searchBar.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                /*判断是否是“Search”键*/
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    keyWord = v.getText().toString();
                    tv_commodity_sort.setText("全部");
                    loadData(page, LIST_COMMODITY_KEY, false);
                    return true;
                }
                return false;
            }
        });
        searchBar.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus && (Constant.StateString.ACTION_SEARCH_TO_COMMMODITY).equals(ACTION_ACYIVITY)) {
                    JianPanUtils.showIme(CommodityListActivity.this, v);
                }
            }
        });

        manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mPullToLoadView.setLayoutManager(manager);


    }

    private void initProcess() {
        orgId = UserSharedPreferencesUtil.getUserInfo(this, UserSharedPreferencesUtil.ORGID, -1);
        if (orgId == -1) {
            toast("请先选择小区id");
            return;
        }
        Intent intent = getIntent();
        ACTION_ACYIVITY = intent.getAction();
        Serializable serializable = intent.getSerializableExtra("CommoditySortBean");
        if (serializable != null) {
            CommoditySortBean commoditySortBean = (CommoditySortBean) serializable;
            currentClassID = commoditySortBean.classId;
            currentClassType = commoditySortBean.classType;
            listState = LIST_COMMODITY_SORT;
            if (tv_commodity_sort != null) {
                tv_commodity_sort.setText(commoditySortBean.name);
            }
        }
//        mRecyclerView.setLayoutManager(manager);
//        mRecyclerView.addItemDecoration(new DividerItemDecoration(
//                this, DividerItemDecoration.HORIZONTAL_LIST));
        mAdapter = new SimpleAdapter();
        mAdapter.setOnItemClickListener(this);
        mRecyclerView.setAdapter(mAdapter);
        //
        //设置分隔线，和listView 不同注意
        mPullToLoadView.isLoadMoreEnabled(true);
        mPullToLoadView.setPullCallback(new CommodityPullCallback());
        mPullToLoadView.initNetLoad(this);
        initCommoditySort();
        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (manager.findFirstVisibleItemPosition() == 0) {
                    move_to_top.setVisibility(View.GONE);
                }
                if (manager.findFirstVisibleItemPosition() == 1) {
                    move_to_top.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void initCommoditySort() {
        //TODO 处理请求的缓存
        if (mallDataHelper == null) {
            mallDataHelper = new MallDataHelper(this);
        }
        mallDataHelper.commodityListSort(getNetRequestHelper(this).isShowProgressDialog(false), orgId);
    }

    private void showPopuMenu(View v) {
        // 需要获得服务器传值中的商品种类
        mMenu = new UserMenu(CommodityListActivity.this);
        if (commoditySortBeans != null) {
            for (int i = 0; i < commoditySortBeans.size(); i++) {
                CommoditySortBean commoditySortBean = commoditySortBeans.get(i);
                if (currentClassID == commoditySortBean.classId) {
                    mMenu.addItem(commoditySortBean.name, i, true);
                } else {
                    mMenu.addItem(commoditySortBean.name, i, false);
                }
            }
        }
        mMenu.setOnItemSelectedListener(new PopMenu.OnItemSelectedListener() {
            @Override
            public void selected(View view, PopMenu.Item item, int position) {
                currentClassID = commoditySortBeans.get(position).classId;
                currentClassType = commoditySortBeans.get(position).classType;
                if (tv_commodity_sort != null) {
                    tv_commodity_sort.setText(commoditySortBeans.get(position).name);
                }
                loadData(page, LIST_COMMODITY_SORT, false);
            }
        });
        mMenu.showAsDropDown(v);
    }

    private void loadData(final int page, int mlistState, boolean isfresh) {
        if (mallDataHelper == null) {
            mallDataHelper = new MallDataHelper(this);
        }
        listState = mlistState;
        if (orgId <= 0) {
            return;
        }

        switch (mlistState) {
            case LIST_COMMODITY_ALL:
                listOrderState = mlistState;
                if (orgId > 0) {
                    if (page == 1) {
                        currentPriceOrder = null;
                        currentSalesOrder = null;
                        setOrderViewState(tv_commodity_num_order, currentPriceOrder);
                        setOrderViewState(tv_commodity_price_order, currentSalesOrder);
                    }
                    mallDataHelper.commodityList(getNetRequestHelper(this).isShowProgressDialog(false), orgId, page);
                } else {
                    toast("小区id为空");
                }
                break;
            case LIST_COMMODITY_KEY:
                listOrderState = mlistState;
                if (TextUtils.isEmpty(keyWord)) {
                    toast("输入关键字不能为空");
                    break;
                }
                if (orgId > 0) {
                    if (page == 1) {
                        currentPriceOrder = null;
                        currentSalesOrder = null;
                        setOrderViewState(tv_commodity_num_order, currentPriceOrder);
                        setOrderViewState(tv_commodity_price_order, currentSalesOrder);
                        mallDataHelper.commodityList(getNetRequestHelper(this).isShowProgressDialog(!isfresh), orgId, page, keyWord);
                    } else {
                        mallDataHelper.commodityList(getNetRequestHelper(this).isShowProgressDialog(false), orgId, page, keyWord);
                    }
                } else {
                    toast("小区id为空");
                }
                break;
            case LIST_COMMODITY_SORT:
                listOrderState = mlistState;
                if (currentClassID == -1) {
                    toast("类别为空");
                    break;
                }
                if (orgId > 0) {
                    if (page == 1) {
                        currentPriceOrder = null;
                        currentSalesOrder = null;
                        setOrderViewState(tv_commodity_num_order, currentPriceOrder);
                        setOrderViewState(tv_commodity_price_order, currentSalesOrder);
                        mallDataHelper.commodityList(getNetRequestHelper(this).isShowProgressDialog(!isfresh), orgId, page, currentClassID, currentClassType);
                    } else {
                        mallDataHelper.commodityList(getNetRequestHelper(this).isShowProgressDialog(false), orgId, page, currentClassID, currentClassType);
                    }
                } else {
                    toast("小区id为空");
                }
                break;
            case LIST_COMMODITY_ORDER:
                if (listOrderState == LIST_COMMODITY_ALL) {
                    if (page == 1) {
                        mallDataHelper.commodityListOrderAll(getNetRequestHelper(this).isShowProgressDialog(!isfresh), orgId, page, currentPriceOrder, currentSalesOrder);
                    } else {
                        mallDataHelper.commodityListOrderAll(getNetRequestHelper(this).isShowProgressDialog(false), orgId, page, currentPriceOrder, currentSalesOrder);
                    }
                } else if (listOrderState == LIST_COMMODITY_KEY) {
                    if (page == 1) {
                        mallDataHelper.commodityListSearchKeyOrder(getNetRequestHelper(this).isShowProgressDialog(!isfresh), orgId, page, keyWord, currentPriceOrder, currentSalesOrder);
                    } else {
                        mallDataHelper.commodityListSearchKeyOrder(getNetRequestHelper(this).isShowProgressDialog(false), orgId, page, keyWord, currentPriceOrder, currentSalesOrder);
                    }
                } else if (listOrderState == LIST_COMMODITY_SORT) {
                    if (page == 1) {
                        mallDataHelper.commodityListSortOrder(getNetRequestHelper(this).isShowProgressDialog(!isfresh), orgId, page, currentClassID, currentClassType, currentPriceOrder, currentSalesOrder);
                    } else {
                        mallDataHelper.commodityListSortOrder(getNetRequestHelper(this).isShowProgressDialog(false), orgId, page, currentClassID, currentClassType, currentPriceOrder, currentSalesOrder);
                    }
                }
                //
                break;
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

                synchronized (this) {
                    if (Constant.MallUrl.COMMODITY_LIST.equals(requestTag)) {
                        BaseDataBean<CommodityListBeans> baseDataBean = JsonUtil.parseDataObject(responseInfo.result, CommodityListBeans.class);
                        if (baseDataBean.code == 100) {
                            CommodityListBeans commodityListBeans = baseDataBean.data;
                            if (mAdapter != null && commodityListBeans.pageNo == 1) {
                                mAdapter.clear();
                            }
                            isLoading = false;
                            commodityBeans = commodityListBeans.list;
                            if (commodityBeans.size() > 0) {
                                mAdapter.add(commodityBeans);
                            }
                            if (commodityListBeans.totalCount > commodityListBeans.pageNo * commodityListBeans.pageSize) {
                                nextPage = commodityListBeans.pageNo + 1;
                                isHasLoadedAll = false;
                            } else {
                                isHasLoadedAll = true;
                            }
                        } else {
                            toast(baseDataBean.msg);
                        }
                        mPullToLoadView.setComplete();
                    }
                }
                if (Constant.MallUrl.COMMODITY_SORT.equals(requestTag)) {
                    BaseBean<CommoditySortBean> baseDataBean = JsonUtil.jsonArray(responseInfo.result, CommoditySortBean.class);
                    if (baseDataBean.code == 100) {
                        commoditySortBeans = baseDataBean.data;
                    } else {
                        toast(baseDataBean.msg);
                    }
                }
            }

            @Override
            public void onFailure(HttpException error, String msg, String requestTag) {
                mPullToLoadView.setComplete();
                NetWorkStateUtils.errorNetMes(CommodityListActivity.this);
            }
        };
    }

    @Override
    public void onItemClick(View view, int postion) {
        if (mAdapter != null && mAdapter.getItemCount() > postion) {

            List<CommodityBean> commodityBeans = mAdapter.getmList();
            Intent intent = new Intent(this, CommodityDetailInforActivity.class);
            intent.putExtra("CommodityBean", commodityBeans.get(postion));
            startActivity(intent);
        }
    }

    private class SimpleAdapter extends RecyclerView.Adapter<CellHolder> {
        private DisplayImageOptions mConfig = new DisplayImageOptions.Builder()
//            .showImageOnLoading(R.drawable.home_youpin)
                .showImageForEmptyUri(R.drawable.ic_launcher)
                .showImageOnFail(R.drawable.ic_launcher)
                .cacheInMemory(true)// 在内存中会缓存该图片
                .cacheOnDisk(true)// 在硬盘中会缓存该图片
                .considerExifParams(true)// 会识别图片的方向信息
                .resetViewBeforeLoading(true)// 重设图片
//            .displayer(new FadeInBitmapDisplayer(500))
                .build();
        private List<CommodityBean> mList;
        private MyItemClickListener myItemClickListener;
        private ImageLoader instance = ImageLoader.getInstance();

        public SimpleAdapter() {
            mList = new ArrayList<>();
        }

        @Override
        public CellHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.activity_commodity_list_item, viewGroup, false);
            return new CellHolder(view, myItemClickListener);
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
        public void onBindViewHolder(CellHolder holder, int i) {
            holder.commodity_list_item_title.setText(mList.get(i).name);
            holder.commodity_list_item_price.setText("¥" + mList.get(i).currentPriceShow);

            SpannableString spannableString = CommonUtils.deleteStyle("¥" + CommonUtils.floatToTowDecima(mList.get(i).price));

            holder.commodity_list_item_favorite.setText(spannableString);

            if (mList.get(i).mainPhoto != null) {
                instance.displayImage(mList.get(i).mainPhoto.middlePicUrl, holder.img, mConfig);
            }
        }

        public void add(ArrayList<CommodityBean> beans) {
            if (beans != null) {
                mList.addAll(beans);
                notifyDataSetChanged();
            }
        }

        public void clear() {
            mList.clear();
            notifyDataSetChanged();
        }

        public List<CommodityBean> getmList() {
            return mList;
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

    private class CellHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private MyItemClickListener myItemClickListener;
        public TextView commodity_list_item_title;
        public TextView commodity_list_item_price;
        public TextView commodity_list_item_favorite;
        public ImageView img;

        public CellHolder(View itemView, MyItemClickListener myItemClickListener) {
            super(itemView);
            this.myItemClickListener = myItemClickListener;
            itemView.setOnClickListener(this);
            img = (ImageView) itemView.findViewById(R.id.commodity_list_item_img);
            commodity_list_item_title = (TextView) itemView.findViewById(R.id.commodity_list_item_title);
            commodity_list_item_price = (TextView) itemView.findViewById(R.id.commodity_list_item_price);
            commodity_list_item_favorite = (TextView) itemView.findViewById(R.id.commodity_list_item_favorite);
        }

        @Override
        public void onClick(View v) {
            myItemClickListener.onItemClick(v, getAdapterPosition());
        }
    }

    class CommodityPullCallback implements PullCallback {
        @Override
        public void onLoadMore() {
            MyLog.e("AAA","onLoadMore---"+tempNextPage+"-nextPage--"+nextPage);
           if(tempNextPage==nextPage){
               tempNextPage=nextPage+1;
               loadData(nextPage, listState, false);
           }else{
               mPullToLoadView.setComplete();
           }
        }

        @Override
        public void onRefresh() {
            if (CommonUtils.isNetworkConnected(CommodityListActivity.this)) {
                isHasLoadedAll = false;
                page = 1;
                tempNextPage=2;
                loadData(page, listState, true);
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

    public void rotateyAnimRun(final View view) {
        ObjectAnimator anim = ObjectAnimator//
                .ofFloat(view, "zhy", 1.0F, 0.0F)//
                .setDuration(500);//
        anim.start();
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float cVal = (Float) animation.getAnimatedValue();


            }
        });
    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(this.getClass().getSimpleName()); //统计页面(仅有Activity的应用中SDK自动调用，不需要单独写)
        MobclickAgent.onResume(this);          //统计时长
        if (goodsCard == null) {
            goodsCard = new BadgeView(this, myTitleBarHelper.getRightImag());
        }
        CommonUtils.setRedDotNum(goodsCard, SharedPreferencesUtil.getIntData(this, Constant.ShouYeUrl.SHAOPPING_NUM, 0));
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(this.getClass().getSimpleName()); // （仅有Activity的应用中SDK自动调用，不需要单独写）保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息
        MobclickAgent.onPause(this);
    }
}
