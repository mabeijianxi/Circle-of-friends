package anim.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
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
import com.henanjianye.soon.communityo2o.common.enties.BaseBean;
import com.henanjianye.soon.communityo2o.common.enties.CommoditySortBean;
import com.henanjianye.soon.communityo2o.common.enties.JsonUtil;
import com.henanjianye.soon.communityo2o.common.util.CommonUtils;
import com.henanjianye.soon.communityo2o.common.util.MyTitleBarHelper;
import com.henanjianye.soon.communityo2o.common.util.NetWorkStateUtils;
import com.henanjianye.soon.communityo2o.common.util.SharedPreferencesUtil;
import com.henanjianye.soon.communityo2o.common.util.UserSharedPreferencesUtil;
import com.henanjianye.soon.communityo2o.common.view.BadgeView;
import com.henanjianye.soon.communityo2o.common.view.DividerGridItemDecoration;
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
public class CommoditySortActivity extends BaseActivity implements MyItemClickListener {

    private PullToLoadView mPullToLoadView;
    private CommoditySortAdapter mAdapter;
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
    private BadgeView goodsCard;


    @Override
    public int mysetContentView() {
        return R.layout.activity_commodity_sort;
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
        mPullToLoadView = (PullToLoadView) findViewById(R.id.commodity_sort);
        mRecyclerView = mPullToLoadView.getRecyclerView();

        myTitleBarHelper = new MyTitleBarHelper(this, getWindow().getDecorView().findViewById(android.R.id.content));
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.iv_title_bar_left:
                finish();
                break;
            case R.id.iv_title_bar_right:
                // 购物车
                CommonUtils.startGoodsCartActivity(this);
                break;
        }
    }
    private void initEvent() {
        myTitleBarHelper.setOnclickListener(this);
        myTitleBarHelper.setRightImag(R.drawable.title_commodity_goodscard);
        myTitleBarHelper.setMiddleText("优品分类");
//        LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        LinearLayoutManager manager = new GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new DividerGridItemDecoration(CommoditySortActivity.this));
        mPullToLoadView.setLayoutManager(manager);
    }

    private void initProcess() {
        orgId = UserSharedPreferencesUtil.getUserInfo(this, UserSharedPreferencesUtil.ORGID, -1);
        if (orgId == -1) {
            toast("小区id不能为空");
            return;
        }
//        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(
                this, DividerItemDecoration.HORIZONTAL_LIST));
        mAdapter = new CommoditySortAdapter();
        mAdapter.setOnItemClickListener(this);
        //设置分隔线，和listView 不同注意
        mPullToLoadView.isLoadMoreEnabled(false);
        mPullToLoadView.isRefreshEnabled(false);
        mPullToLoadView.setPullCallback(new CommodityPullCallback());
        mRecyclerView.setAdapter(mAdapter);
        initCach();
        mPullToLoadView.initNetLoad(this);
    }

    private void initCach() {
        String result = SharedPreferencesUtil.getStringData(CommoditySortActivity.this, Constant.CachTag.APP_COMMDITY_SORT,
                null);
        parseDate(result, true);
    }

    private void initCommoditySort() {
        //TODO 处理请求的缓存
        if (mallDataHelper == null) {
            mallDataHelper = new MallDataHelper(this);
        }
        mallDataHelper.commodityListSort(getNetRequestHelper(this).isShowProgressDialog(false), orgId);
    }


    private void loadData() {

        initCommoditySort();
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

            }

            @Override
            public void onLoading(long total, long current, boolean isUploading, String requestTag) {

            }

            @Override
            public void onSuccess(ResponseInfo<String> responseInfo, String requestTag) {
                mPullToLoadView.setComplete();
                if (Constant.MallUrl.COMMODITY_SORT.equals(requestTag)) {
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
                NetWorkStateUtils.errorNetMes(CommoditySortActivity.this);
            }
        };
    }

    private void parseDate(String result, boolean isCach) {
        if (result == null) {
            return;
        }
        BaseBean<CommoditySortBean> baseDataBean = JsonUtil.jsonArray(result, CommoditySortBean.class);
        if (baseDataBean.code == 100) {
            if (!isCach) {
                if (mAdapter != null) {
                    //因为只有一页，所以每次请求到结果都将重新设置数据
                    mAdapter.clear();
                }
                SharedPreferencesUtil.saveStringData(CommoditySortActivity.this, Constant.CachTag.APP_COMMDITY_SORT,
                        result);
            }
            commoditySortBeans = baseDataBean.data;
            if (mAdapter != null && commoditySortBeans != null) {
                mAdapter.clear();
                mAdapter.add(commoditySortBeans);
            } else {
                toast("数据为空");
            }

        } else {
            toast(baseDataBean.msg);
        }
    }

    @Override
    public void onItemClick(View view, int postion) {
        if (postion < 0) {
            return;
        }
        //TODO 处理点击事件
        List<CommoditySortBean> commoditySortBeans = mAdapter.getmList();
        if (commoditySortBeans.size() > postion) {
            CommoditySortBean commoditySortBean = commoditySortBeans.get(postion);
            Intent intent = new Intent(this, CommodityListActivity.class);
            intent.setAction(Constant.StateString.ACTION_SORT_TO_COMMMODITY);
            intent.putExtra("CommoditySortBean", commoditySortBean);
            startActivity(intent);
        }

    }

    private class CommoditySortAdapter extends RecyclerView.Adapter<CellHolder> {

        private List<CommoditySortBean> mList;
        private MyItemClickListener myItemClickListener;

        public CommoditySortAdapter() {

            mList = new ArrayList<>();
        }

        public List<CommoditySortBean> getmList() {
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

        @Override
        public CellHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.activity_commodity_sort_item, viewGroup, false);
            return new CellHolder(view, myItemClickListener);
        }

        @Override
        public void onBindViewHolder(CellHolder holder, int i) {
            ImageLoader instance = ImageLoader.getInstance();
            DisplayImageOptions build = new DisplayImageOptions.Builder()
                    .showImageOnFail(R.mipmap.gouwuche_image)
                    .showImageForEmptyUri(R.mipmap.gouwuche_image).build();
            if (mList.get(i).icon != null && mList.get(i).icon.picUrl != null) {
                instance.displayImage(mList.get(i).icon.picUrl, holder.iv_commodity_sort_img, build);
            }
            holder.iv_commodity_sort_title.setText(mList.get(i).name);

        }

        public void add(List<CommoditySortBean> beans) {
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
        private MyItemClickListener myItemClickListener;
        public TextView iv_commodity_sort_title;
        public ImageView iv_commodity_sort_img;

        public CellHolder(View itemView, MyItemClickListener myItemClickListener) {
            super(itemView);
            this.myItemClickListener = myItemClickListener;
            itemView.setOnClickListener(this);
            iv_commodity_sort_img = (ImageView) itemView.findViewById(R.id.iv_commodity_sort_img);
            iv_commodity_sort_title = (TextView) itemView.findViewById(R.id.iv_commodity_sort_title);
        }

        @Override
        public void onClick(View v) {

            myItemClickListener.onItemClick(v, getAdapterPosition());
        }
    }

    class CommodityPullCallback implements PullCallback {
        @Override
        public void onLoadMore() {
            loadData();
        }

        @Override
        public void onRefresh() {

            if (CommonUtils.isNetworkConnected(CommoditySortActivity.this)) {
                isHasLoadedAll = false;
                page = 1;
                loadData();
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
