package anim.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.henanjianye.soon.communityo2o.common.enties.BaseDataBean;
import com.henanjianye.soon.communityo2o.common.enties.CommodityBean;
import com.henanjianye.soon.communityo2o.common.enties.CommodityIndexJumpBean;
import com.henanjianye.soon.communityo2o.common.enties.CommodityListBeans;
import com.henanjianye.soon.communityo2o.common.enties.CommoditySortBean;
import com.henanjianye.soon.communityo2o.common.enties.JsonUtil;
import com.henanjianye.soon.communityo2o.common.util.CommonUtils;
import com.henanjianye.soon.communityo2o.common.util.MyTitleBarHelper;
import com.henanjianye.soon.communityo2o.common.util.NetWorkStateUtils;
import com.henanjianye.soon.communityo2o.common.util.SharedPreferencesUtil;
import com.henanjianye.soon.communityo2o.common.util.UserSharedPreferencesUtil;
import com.henanjianye.soon.communityo2o.common.view.BadgeView;
import com.henanjianye.soon.communityo2o.common.view.DividerItemDecoration;
import com.henanjianye.soon.communityo2o.interf.MyItemClickListener;
import com.henanjianye.soon.communityo2o.interf.NetWorkCallback;
import com.henanjianye.soon.communityo2o.view.CountDownView;
import com.henanjianye.soon.communityo2o.view.UserMenu;
import com.umeng.analytics.MobclickAgent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/9/14.
 */
public class  CommodityLargeListActivity extends BaseActivity implements MyItemClickListener {

    private PullToLoadView mPullToLoadView;
    private CommodityLargeAdapter mAdapter;
    private boolean isLoading = false;
    private boolean isHasLoadedAll = false;
    private int nextPage;
    private RecyclerView mRecyclerView;
    private UserMenu mMenu;
    private MyTitleBarHelper myTitleBarHelper;
    private MallDataHelper mallDataHelper;
    private int page = 1;
    private int orgId;
    private List<CommoditySortBean> commoditySortBeans;
    private int currentClassID = 65681;
    private int currentClassType = 1;
    private final int UXUAN = 1;
    private final int QGOU = 2;
    private final int YJIANG = 3;
    private ArrayList<CommodityBean> commodityBeans;
    private ImageView move_to_top;
    private BadgeView goodsCard;
    private int tempNextPage;

    @Override
    public int mysetContentView() {
        return R.layout.activity_commodity_large;
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


        move_to_top = (ImageView) findViewById(R.id.move_to_top);
        if (page <= 1) {
            move_to_top.setVisibility(View.GONE);
        }
    }



    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.iv_title_bar_left:
                finish();
                break;
            case R.id.iv_title_bar_right:
                //TODO 购物车
                CommonUtils.startGoodsCartActivity(this);
                break;
            case R.id.move_to_top:
//                回到顶部的操作
                mRecyclerView.smoothScrollToPosition(0);
                toast("回到顶部");
                break;
        }
    }

    private void initEvent() {
        myTitleBarHelper.setOnclickListener(this);
        myTitleBarHelper.setRightImag(R.drawable.title_commodity_goodscard);
        LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mPullToLoadView.setLayoutManager(manager);
        move_to_top.setOnClickListener(this);
    }

    private void initProcess() {
        orgId = UserSharedPreferencesUtil.getUserInfo(this, UserSharedPreferencesUtil.ORGID, -1);
        Intent intent = getIntent();
        Serializable ob = intent.getSerializableExtra("CommodityIndexJumpBean");
        if (ob != null) {
            CommodityIndexJumpBean commodityIndexJumpBean = (CommodityIndexJumpBean) ob;
            currentClassType = commodityIndexJumpBean.classType;
            currentClassID = commodityIndexJumpBean.classId;
            myTitleBarHelper.setMiddleText(commodityIndexJumpBean.className);
        } else {
            toast("传递数据有误");
            return;
        }
        if (orgId == -1) {
            toast("小区id不能为空");
            return;
        }
//        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(
                this, DividerItemDecoration.HORIZONTAL_LIST));
        mAdapter = new CommodityLargeAdapter();
        mAdapter.setOnItemClickListener(this);
        mRecyclerView.setAdapter(mAdapter);
        //设置分隔线，和listView 不同注意
        mPullToLoadView.isLoadMoreEnabled(true);
        mPullToLoadView.setPullCallback(new CommodityPullCallback());
//        initCommoditySort();
        String resultCach = null;
        switch (currentClassType) {
            case UXUAN:
                //优品选购
                resultCach = SharedPreferencesUtil.getStringData(this, Constant.CachTag.APP_COMMDITY_YXUAN, null);
                break;
            case QGOU:
                //抢购
                resultCach = SharedPreferencesUtil.getStringData(this, Constant.CachTag.APP_COMMDITY_QGOU, null);
                break;
            case YJIANG:
                resultCach = SharedPreferencesUtil.getStringData(this, Constant.CachTag.APP_COMMDITY_YJIANG, null);
                break;
        }
        parseDate(resultCach, true);
        mPullToLoadView.initNetLoad(this);
//        mPullToLoadView.initLoad();
    }

    private void initCommoditySort() {
        //TODO 处理请求的缓存
        if (mallDataHelper == null) {
            mallDataHelper = new MallDataHelper(this);
        }
        mallDataHelper.commodityListSort(getNetRequestHelper(this).isShowProgressDialog(false), orgId);
    }
    /**
     * @param page 页码
     */
    private void loadData(final int page) {
        if (mallDataHelper == null) {
            mallDataHelper = new MallDataHelper(this);
        }
        mallDataHelper.commodityList(getNetRequestHelper(this).isShowProgressDialog(false), orgId, page, currentClassID, currentClassType);
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
                if (Constant.MallUrl.COMMODITY_LIST.equals(requestTag)) {
                    parseDate(responseInfo.result, false);
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
                NetWorkStateUtils.errorNetMes(CommodityLargeListActivity.this);
            }
        };
    }

    private synchronized void parseDate(String str, boolean isCach) {
        if (str == null) {
            return;
        }
        BaseDataBean<CommodityListBeans> baseDataBean = JsonUtil.parseDataObject(str, CommodityListBeans.class);
        CommodityListBeans commodityListBeans = baseDataBean.data;
        if (baseDataBean.code == 100) {
            //TODO 获取传递来的  classType  和 classID 以下为测试值
            if (!isCach) {
                if (commodityListBeans.pageNo == 1 && mAdapter != null) {
                    mAdapter.clear();
                    switch (currentClassType) {
                        case UXUAN:
                            SharedPreferencesUtil.saveStringData(this, Constant.CachTag.APP_COMMDITY_YXUAN, str);
                            break;
                        case QGOU:
                            SharedPreferencesUtil.saveStringData(this, Constant.CachTag.APP_COMMDITY_QGOU, str);
                            break;
                        case YJIANG:
                            SharedPreferencesUtil.saveStringData(this, Constant.CachTag.APP_COMMDITY_YJIANG, str);
                            break;
                    }
                }

            } else {
                if (mAdapter != null) {
                    mAdapter.clear();
                }
            }
            isLoading = false;
            commodityBeans = commodityListBeans.list;
            if (commodityBeans != null && commodityBeans.size() > 0) {
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

    @Override
    public void onItemClick(View view, int postion) {
        List<CommodityBean> commodityBeans = mAdapter.getmList();
        if (commodityBeans != null && commodityBeans.size() > postion) {
            CommodityBean commodityBean = commodityBeans.get(postion);
            Intent intent = new Intent(this, CommodityDetailInforActivity.class);
            intent.putExtra("CommodityBean", commodityBean);
            startActivity(intent);
        }

    }

    private class CommodityLargeAdapter extends RecyclerView.Adapter<CellHolder> {

        private List<CommodityBean> mList;
        private MyItemClickListener myItemClickListener;

        public CommodityLargeAdapter() {
            mList = new ArrayList<>();
        }

        public List<CommodityBean> getmList() {
            return mList;
        }

        @Override
        public CellHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.activity_commodity_large_item, viewGroup, false);
            if (i <= 0) {
                move_to_top.setVisibility(View.GONE);
            } else if (i > 0) {
                move_to_top.setVisibility(View.VISIBLE);
            }
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
            holder.iv_commodity_large_title.setText(mList.get(i).name);
            holder.iv_commodity_large_price.setText("¥ " + mList.get(i).currentPriceShow);
            holder.iv_commodity_large_left.setText("¥ " + mList.get(i).priceShow);
            holder.iv_commodity_large_right.setText("商品类型 " + mList.get(i).classType);
            if (currentClassType == 2) {
                //限时抢购才显示倒计时，否则不显示
                holder.tv_title_blank.setVisibility(View.VISIBLE);
            } else {
                holder.tv_title_blank.setVisibility(View.GONE);
            }
//            mList.get(i).qiangSeconds=15*1000;
            if (mList.get(i).qiangSeconds <= 0) {
                holder.tv_title_blank.setText("抢购已结束");
            } else {
                if(mList.get(i).qiangStatus==1){
                    //尚未开始
                    holder.tv_title_blank.setTypeMills(mList.get(i).qiangSeconds* 1000,1);
                }else {
                    //已经开始抢购
                    holder.tv_title_blank.setTypeMills(mList.get(i).qiangSeconds* 1000,2);
                }
                holder.tv_title_blank.setOnFinishedListener(new CountDownView.OnFinishedListener() {
                    @Override
                    public void onFinished(CountDownView countDownView) {
                        countDownView.setText("已过期");
                    }
                });
            }
//            holder.tv_title_blank.setMills(mList.get(i).qiangSeconds);
            ImageLoader instance = ImageLoader.getInstance();
            DisplayImageOptions build = new DisplayImageOptions.Builder()
                    .showImageOnFail(R.mipmap.ic_launcher)
                    .showImageForEmptyUri(R.mipmap.ic_launcher).build();
            //调整图片的高根据屏幕宽
            ViewGroup.LayoutParams layoutParams = holder.iv_commodity_large_img.getLayoutParams();
            layoutParams.width = CommonUtils.getScreenSizeWidth(CommodityLargeListActivity.this);
            layoutParams.height = (int) (layoutParams.width * 0.8);
            holder.iv_commodity_large_img.setLayoutParams(layoutParams);
            if (mList.get(i).mainPhoto != null) {
                instance.displayImage(mList.get(i).mainPhoto.picUrl, holder.iv_commodity_large_img, build);
            }
        }

        public void add(ArrayList<CommodityBean> beans) {
            mList.addAll(beans);
            notifyDataSetChanged();
        }

        public void clear() {
            mList.clear();
            notifyDataSetChanged();
        }

        @Override
        public int getItemViewType(int position) {
            return super.getItemViewType(position);
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
        private final MyItemClickListener myItemClickListener;
        public TextView iv_commodity_large_title;
        public TextView iv_commodity_large_price;
        public TextView iv_commodity_large_left;
        public TextView iv_commodity_large_right;
        public ImageView iv_commodity_large_img;
        public CountDownView tv_title_blank;

        public CellHolder(View itemView, MyItemClickListener myItemClickListener) {
            super(itemView);
            this.myItemClickListener = myItemClickListener;
            itemView.setOnClickListener(this);
            iv_commodity_large_img = (ImageView) itemView.findViewById(R.id.iv_commodity_large_img);


            iv_commodity_large_title = (TextView) itemView.findViewById(R.id.iv_commodity_large_title);
            iv_commodity_large_price = (TextView) itemView.findViewById(R.id.iv_commodity_large_price);
            iv_commodity_large_left = (TextView) itemView.findViewById(R.id.iv_commodity_large_left);
            iv_commodity_large_right = (TextView) itemView.findViewById(R.id.iv_commodity_large_right);
            tv_title_blank = (CountDownView) itemView.findViewById(R.id.tv_title_blank);
        }

        @Override
        public void onClick(View v) {
            myItemClickListener.onItemClick(v, getAdapterPosition());
        }
    }

    class CommodityPullCallback implements PullCallback {
        @Override
        public void onLoadMore() {
            if(tempNextPage==nextPage){
                tempNextPage=nextPage+1;
                loadData(nextPage);
            }else{
                mPullToLoadView.setComplete();
            }

        }

        @Override
        public void onRefresh() {

            if (CommonUtils.isNetworkConnected(CommodityLargeListActivity.this)) {
                isHasLoadedAll = false;
                tempNextPage=2;
                page = 1;
                loadData(page);
            } else {
                toast("网络不可用");
                mPullToLoadView.setComplete();
            }
//            mAdapter.clear();

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
