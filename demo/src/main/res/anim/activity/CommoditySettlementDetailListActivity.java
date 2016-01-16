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

import com.asyey.footballlibrary.universalimageloader.core.DisplayImageOptions;
import com.asyey.footballlibrary.universalimageloader.core.ImageLoader;
import com.henanjianye.soon.communityo2o.BaseActivity;
import com.henanjianye.soon.communityo2o.R;
import com.henanjianye.soon.communityo2o.RecycleView.PullToLoadView;
import com.henanjianye.soon.communityo2o.common.MallDataHelper;
import com.henanjianye.soon.communityo2o.common.enties.CommodityBean;
import com.henanjianye.soon.communityo2o.common.enties.CommodityGoodsCartBean;
import com.henanjianye.soon.communityo2o.common.enties.CommoditySettlementBean;
import com.henanjianye.soon.communityo2o.common.enties.CommoditySortBean;
import com.henanjianye.soon.communityo2o.common.util.CommonUtils;
import com.henanjianye.soon.communityo2o.common.util.MyTitleBarHelper;
import com.henanjianye.soon.communityo2o.common.util.UserSharedPreferencesUtil;
import com.henanjianye.soon.communityo2o.common.view.DividerItemDecoration;
import com.henanjianye.soon.communityo2o.interf.MyItemClickListener;
import com.henanjianye.soon.communityo2o.view.UserMenu;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/9/14.
 */
public class CommoditySettlementDetailListActivity extends BaseActivity implements MyItemClickListener {

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
    private final int UXUAN = 1;
    private final int QGOU = 2;
    private final int YJIANG = 3;
    private ArrayList<CommodityBean> commodityBeans;
    private CommoditySettlementBean commoditySettlementBean;

    @Override
    public int mysetContentView() {
        return R.layout.activity_commodity_settlement_detail;
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
        myTitleBarHelper.resetState();
        myTitleBarHelper.setOnclickListener(this);
        myTitleBarHelper.setMiddleText("订单详情");
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

                break;


        }
    }


    private void initEvent() {
        myTitleBarHelper.setOnclickListener(this);
        LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mPullToLoadView.setLayoutManager(manager);
    }

    private void initProcess() {
        orgId = UserSharedPreferencesUtil.getUserInfo(this, UserSharedPreferencesUtil.ORGID, -1);

//        Intent intent = getIntent();
//        Serializable ob = intent.getSerializableExtra("CommodityIndexJumpBean");
//        if (ob != null) {
//            CommodityIndexJumpBean commodityIndexJumpBean = (CommodityIndexJumpBean) ob;
//            myTitleBarHelper.setMiddleText(commodityIndexJumpBean.className);
//        } else {
//            toast("传递数据有误");
//            return;
//        }
        if (orgId == -1) {
            toast("小区id不能为空");
            return;
        }
        Intent intent = getIntent();
        Object object = intent.getSerializableExtra(CommoditySettlementActivity.COMMODITYSETTLEMENTBEAN1);
        if (object != null && object instanceof CommoditySettlementBean) {
            commoditySettlementBean = (CommoditySettlementBean) object;
        }else{
            toast("数据错误");
            return;
        }
//        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(
                this, DividerItemDecoration.HORIZONTAL_LIST));
        mAdapter = new CommodityLargeAdapter();
        mAdapter.setOnItemClickListener(this);
        mAdapter.add(commoditySettlementBean.goodsCartList);
        mRecyclerView.setAdapter(mAdapter);
        //设置分隔线，和listView 不同注意
        mPullToLoadView.isLoadMoreEnabled(false);
        mPullToLoadView.isRefreshEnabled(false);
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
//        mallDataHelper.commodityList(getNetRequestHelper(this).isShowProgressDialog(false), orgId, page, currentClassID, currentClassType);
    }


//    @Override
//    protected NetWorkCallback setNetWorkCallback() {
//        return new NetWorkCallback() {
//            @Override
//            public void onStart(String requestTag) {
//                isLoading = true;
//            }
//
//            @Override
//            public void onCancelled(String requestTag) {
//                mPullToLoadView.setComplete();
//            }
//
//            @Override
//            public void onLoading(long total, long current, boolean isUploading, String requestTag) {
//
//            }
//
//            @Override
//            public void onSuccess(ResponseInfo<String> responseInfo, String requestTag) {
//                mPullToLoadView.setComplete();
//                MyLog.e("AAA", "-----onSuccess----------");
//                if (Constant.MallUrl.COMMODITY_LIST.equals(requestTag)) {
//                    parseDate(responseInfo.result, false);
//                }
//            }
//
//            @Override
//            public void onFailure(HttpException error, String msg, String requestTag) {
//                MyLog.e("AAA", "-----onFailure----------");
//                mPullToLoadView.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        mPullToLoadView.setComplete();
//                    }
//                });
//
//                toast(msg);
//            }
//        };
//    }

//    private synchronized void parseDate(String str, boolean isCach) {
//        if (str == null) {
//            return;
//        }
//        BaseDataBean<CommodityListBeans> baseDataBean = JsonUtil.parseDataObject(str, CommodityListBeans.class);
//        CommodityListBeans commodityListBeans = baseDataBean.data;
//        if (baseDataBean.code == 100) {
//            //TODO 获取传递来的  classType  和 classID 以下为测试值
//            if (!isCach) {
//
//                if (commodityListBeans.pageNo == 1 && mAdapter != null) {
//                    mAdapter.clear();
//                }
//
//            } else {
//                if (mAdapter != null) {
//                    mAdapter.clear();
//                }
//            }
//            mPullToLoadView.setComplete();
//
//            isLoading = false;
//            commodityBeans = commodityListBeans.list;
//            if (commodityBeans != null && commodityBeans.size() > 0) {
//                mAdapter.add(commodityBeans);
//            }
//            if (commodityListBeans.totalCount > commodityListBeans.pageNo * commodityListBeans.pageSize) {
//                nextPage = commodityListBeans.pageNo + 1;
//                isHasLoadedAll = false;
//            } else {
//                isHasLoadedAll = true;
//            }
//        } else {
//            mPullToLoadView.setComplete();
//            toast(baseDataBean.msg);
//        }
//    }

    @Override
    public void onItemClick(View view, int postion) {

    }

    private class CommodityLargeAdapter extends RecyclerView.Adapter<CellHolder> {

        private List<CommodityGoodsCartBean.GoodsCart> mList;
        private MyItemClickListener myItemClickListener;

        public CommodityLargeAdapter() {
            mList = new ArrayList<>();
        }

        public List<CommodityGoodsCartBean.GoodsCart> getmList() {
            return mList;
        }

        @Override
        public CellHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.activity_commodity_settlement_detail_item, viewGroup, false);
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

            holder.tv_order_detail_num.setText("X" + mList.get(i).goodsCount);
            holder.tv_order_detail_price.setText(CommonUtils.floatToTowDecima(mList.get(i).goodsPrice));
            holder.tv_order_detail_title.setText(mList.get(i).goods.name);
            ImageLoader instance = ImageLoader.getInstance();
            DisplayImageOptions build = new DisplayImageOptions.Builder()
                    .showImageOnFail(R.drawable.ic_launcher_default)
                    .showImageForEmptyUri(R.drawable.ic_launcher_default).build();
            instance.displayImage(mList.get(i).goods.mainPhoto.smallPicUrl, holder.iv_order_detail_img, build);
        }

        public void add(ArrayList<CommodityGoodsCartBean.GoodsCart> beans) {
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
//            return 5;
            return mList.size();
        }
    }

    private class CellHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final MyItemClickListener myItemClickListener;
        public final ImageView iv_order_detail_img;
        public final TextView tv_order_detail_title;
        public final TextView tv_order_detail_price;
        public final TextView tv_order_detail_num;

        public CellHolder(View itemView, MyItemClickListener myItemClickListener) {
            super(itemView);
            this.myItemClickListener = myItemClickListener;
//            itemView.setOnClickListener(this);
            itemView.setEnabled(false);
            iv_order_detail_img = (ImageView) itemView.findViewById(R.id.iv_order_detail_img);
            tv_order_detail_title = (TextView) itemView.findViewById(R.id.tv_order_detail_title);
            tv_order_detail_price = (TextView) itemView.findViewById(R.id.tv_order_detail_price);
            tv_order_detail_num = (TextView) itemView.findViewById(R.id.tv_order_detail_num);
        }

        @Override
        public void onClick(View v) {
            myItemClickListener.onItemClick(v, getAdapterPosition());
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
