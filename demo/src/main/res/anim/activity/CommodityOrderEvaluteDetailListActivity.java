package anim.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.asyey.footballlibrary.universalimageloader.core.DisplayImageOptions;
import com.asyey.footballlibrary.universalimageloader.core.ImageLoader;
import com.henanjianye.soon.communityo2o.BaseActivity;
import com.henanjianye.soon.communityo2o.R;
import com.henanjianye.soon.communityo2o.RecycleView.PullToLoadView;
import com.henanjianye.soon.communityo2o.common.MallDataHelper;
import com.henanjianye.soon.communityo2o.common.enties.CommodityBean;
import com.henanjianye.soon.communityo2o.common.enties.CommodityOrderBean;
import com.henanjianye.soon.communityo2o.common.enties.CommoditySortBean;
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
public class CommodityOrderEvaluteDetailListActivity extends BaseActivity implements MyItemClickListener {
    public static final int REQUESTTOCOMMODITYORDEREVALUTEDETAILLIST = 0x3301;
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
    private CommodityOrderBean commodityOrderGoodsBean;
    private int currentProcessPos = -1;
    private boolean isChange = false;

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
        myTitleBarHelper.setMiddleText("评价晒单");
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();

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
        Object object = intent.getSerializableExtra(CommodityOrderListActivity.KEYTOECALUTELIST);
        if (object != null && object instanceof CommodityOrderBean) {
            commodityOrderGoodsBean = (CommodityOrderBean) object;
        } else {
            toast("数据错误");
            return;
        }
//        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(
                this, DividerItemDecoration.HORIZONTAL_LIST));
        mAdapter = new CommodityLargeAdapter();
        mAdapter.setOnItemClickListener(this);
        mAdapter.add(commodityOrderGoodsBean.goods);
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


    @Override
    public void onItemClick(View view, int postion) {

    }

    private class CommodityLargeAdapter extends RecyclerView.Adapter<CellHolder> {

        private List<CommodityOrderBean.OderGoods> mList;
        private MyItemClickListener myItemClickListener;

        public CommodityLargeAdapter() {
            mList = new ArrayList<>();
        }

        public List<CommodityOrderBean.OderGoods> getmList() {
            return mList;
        }

        @Override
        public CellHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.activity_commodity_order_evalute_detail_item, viewGroup, false);
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
            holder.tv_order_evalute_detail_title.setText(mList.get(i).goodsName);
            ImageLoader instance = ImageLoader.getInstance();
            DisplayImageOptions build = new DisplayImageOptions.Builder()
                    .showImageOnFail(R.drawable.ic_launcher_default)
                    .showImageForEmptyUri(R.drawable.ic_launcher_default).build();
            instance.displayImage(mList.get(i).picMap.middlePicUrl, holder.iv_order_evalute_detail_img, build);

            if (mList.get(i) != null && mList.get(i).canEvaluate) {
                holder.bt_order_evalute_left.setText("晒单评价");
            } else if (mList.get(i) != null && mList.get(i).canAddPic) {
                holder.bt_order_evalute_left.setText("追加评价");
            } else {
                holder.bt_order_evalute_left.setText("查看评价");
            }
            holder.bt_order_evalute_left.setTag(i);
            holder.bt_order_evalute_left.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = Integer.parseInt(v.getTag().toString());
                    CommodityOrderBean.OderGoods oderGoods = mAdapter.getmList().get(pos);
                    //TODO 做相应跳转
                    if (oderGoods.canAddPic || oderGoods.canEvaluate) {
                        //TODO 跳转至评论页面
                        currentProcessPos = pos;
                        GoodsEvaluateActivity.TransferEvaluation(CommodityOrderEvaluteDetailListActivity.this, oderGoods.canAddPic, oderGoods.goodsId, oderGoods.goodsCartId, oderGoods.picMap.middlePicUrl);
                    } else {
                        //  跳转至评论详情页面
                        EaluationActivity.goEalutionActivity(CommodityOrderEvaluteDetailListActivity.this, oderGoods.goodsId);
                        ;
                    }
                }
            });
        }

        public void add(ArrayList<CommodityOrderBean.OderGoods> beans) {
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
        public final ImageView iv_order_evalute_detail_img;
        public final TextView tv_order_evalute_detail_title;
        public final Button bt_order_evalute_left;

        public CellHolder(View itemView, MyItemClickListener myItemClickListener) {
            super(itemView);
            this.myItemClickListener = myItemClickListener;
//            itemView.setOnClickListener(this);
            itemView.setEnabled(false);
            iv_order_evalute_detail_img = (ImageView) itemView.findViewById(R.id.iv_order_evalute_detail_img);
            tv_order_evalute_detail_title = (TextView) itemView.findViewById(R.id.tv_order_evalute_detail_title);
            bt_order_evalute_left = (Button) itemView.findViewById(R.id.bt_order_evalute_left);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GoodsEvaluateActivity.EVALUATECODE && resultCode == RESULT_OK) {
            //TODO
            if (currentProcessPos >= 0) {
                if (commodityOrderGoodsBean.goods.size() > currentProcessPos) {
                    setResult(RESULT_OK, getIntent());
                    boolean isContainPic=false ;
                    if(data!=null){
                         isContainPic = data.getBooleanExtra(GoodsEvaluateActivity.PICTUREFLAG, false);
                    }

                    if (isContainPic) {
                        if (commodityOrderGoodsBean.goods.get(currentProcessPos).canEvaluate) {
                            commodityOrderGoodsBean.goods.get(currentProcessPos).canEvaluate = false;
                        }
                        if (commodityOrderGoodsBean.goods.get(currentProcessPos).canAddPic) {
                            commodityOrderGoodsBean.goods.get(currentProcessPos).canAddPic = false;
                        }
                    } else {
                        if (commodityOrderGoodsBean.goods.get(currentProcessPos).canEvaluate) {
                            commodityOrderGoodsBean.goods.get(currentProcessPos).canEvaluate = false;
                        }
                        commodityOrderGoodsBean.goods.get(currentProcessPos).canAddPic = true;
                    }

                    mAdapter.notifyDataSetChanged();
                }
            }
        }

    }
}
