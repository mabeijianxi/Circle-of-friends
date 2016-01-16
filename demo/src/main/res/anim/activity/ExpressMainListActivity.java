package anim.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.asyey.footballlibrary.network.exception.HttpException;
import com.asyey.footballlibrary.network.http.ResponseInfo;
import com.asyey.footballlibrary.universalimageloader.core.DisplayImageOptions;
import com.asyey.footballlibrary.universalimageloader.core.ImageLoader;
import com.henanjianye.soon.communityo2o.BaseActivity;
import com.henanjianye.soon.communityo2o.MainActivity;
import com.henanjianye.soon.communityo2o.R;
import com.henanjianye.soon.communityo2o.RecycleView.PullCallback;
import com.henanjianye.soon.communityo2o.RecycleView.PullToLoadView;
import com.henanjianye.soon.communityo2o.common.Constant;
import com.henanjianye.soon.communityo2o.common.ExpressDataHelper;
import com.henanjianye.soon.communityo2o.common.MyLog;
import com.henanjianye.soon.communityo2o.common.enties.BaseDataBean;
import com.henanjianye.soon.communityo2o.common.enties.ExpressMyBean;
import com.henanjianye.soon.communityo2o.common.enties.JsonUtil;
import com.henanjianye.soon.communityo2o.common.enties.ViewWrapper;
import com.henanjianye.soon.communityo2o.common.util.CommonUtils;
import com.henanjianye.soon.communityo2o.common.util.MyTitleBarHelper;
import com.henanjianye.soon.communityo2o.common.util.NetWorkStateUtils;
import com.henanjianye.soon.communityo2o.common.util.ShareSDKConfigUtil;
import com.henanjianye.soon.communityo2o.common.util.SharedPreferencesUtil;
import com.henanjianye.soon.communityo2o.common.util.UmShareUtils;
import com.henanjianye.soon.communityo2o.common.util.UserSharedPreferencesUtil;
import com.henanjianye.soon.communityo2o.common.view.BadgeView;
import com.henanjianye.soon.communityo2o.common.view.DividerItemDecoration;
import com.henanjianye.soon.communityo2o.interf.MyItemClickListener;
import com.henanjianye.soon.communityo2o.interf.NetWorkCallback;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ObjectAnimator;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.ShareSDK;

/**
 * Created by Administrator on 2015/9/14.
 */
public class ExpressMainListActivity extends BaseActivity implements MyItemClickListener {

    private PullToLoadView mPullToLoadView;
    private ExpressAdapter mAdapter;
    private boolean isLoading = false;
    private boolean isHasLoadedAll = false;
    private int nextPage;
    private RecyclerView mRecyclerView;
    private MyTitleBarHelper myTitleBarHelper;
    private int page = 1;
    private int orgId;
    private BadgeView goodsCard;
    private LinearLayout ll_express_null;
    private int backgroundWith;
    private boolean isAnimationLoad;
    private ExpressDataHelper expressDataHelper;
    private final int EXPAND_VIEW_HIGHT = 218;
    private String express_num;
    private boolean isNotifyCustom;
    private int notifyId = -1;

    @Override
    public int mysetContentView() {
        return R.layout.activity_express_user;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_commodity_list);
        ShareSDK.initSDK(this, "b15b2fb3fd04");
        initView();
        initEvent();
        initProcess();
    }

    @Override
    protected void onDestroy() {
        ShareSDK.stopSDK(this);
        super.onDestroy();
    }

    private void initView() {
        mPullToLoadView = (PullToLoadView) findViewById(R.id.pv_express_list);
        ll_express_null = (LinearLayout) findViewById(R.id.ll_express_null);
        mRecyclerView = mPullToLoadView.getRecyclerView();
        myTitleBarHelper = new MyTitleBarHelper(this, getWindow().getDecorView().findViewById(android.R.id.content));
        expressDataHelper = new ExpressDataHelper(this);
        Intent intent = getIntent();
        isNotice = intent.getBooleanExtra("onlineFlag", false);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.iv_title_bar_left:
                press_back();
                break;
            case R.id.iv_title_bar_right:
                // 跳转至查询界面
                startActivity(new Intent(ExpressMainListActivity.this, QueryExpress.class));
                break;
        }
    }

    private void initEvent() {
        myTitleBarHelper.setOnclickListener(this);
        myTitleBarHelper.setRightImag(R.drawable.title_index_search);
        myTitleBarHelper.setMiddleText("我的快递");
        LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mPullToLoadView.setLayoutManager(manager);
    }

    private void initProcess() {
        Intent intent = getIntent();
        express_num = intent.getStringExtra(NoticeAndMessageActivity.EXPRESS_NUM);
        orgId = UserSharedPreferencesUtil.getUserInfo(this, UserSharedPreferencesUtil.ORGID, -1);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(
                this, DividerItemDecoration.HORIZONTAL_LIST));
        mAdapter = new ExpressAdapter();
        mAdapter.setOnItemClickListener(this);
        mRecyclerView.setAdapter(mAdapter);
        //设置分隔线，和listView 不同注意
        mPullToLoadView.isLoadMoreEnabled(true);
        mPullToLoadView.setPullCallback(new CommodityPullCallback());
        mPullToLoadView.initNetLoad(this);
    }

    private void initCommoditySort() {
        //TODO 处理请求的缓存
    }

    /**
     * @param page 页码
     */
    private void loadData(final int page) {
        if (expressDataHelper == null) {
            expressDataHelper = new ExpressDataHelper(this);
        }
        expressDataHelper.getMyExpress(getNetRequestHelper(this).isShowProgressDialog(false), page);
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

                if (Constant.Express.EXPRESS_MY_INFOR.equals(requestTag)) {
                    MyLog.e("AAA", "responseInfo.result----" + responseInfo.result);
                    parseDate(responseInfo.result, false);
                }
                mPullToLoadView.setComplete();
            }

            @Override
            public void onFailure(HttpException error, String msg, String requestTag) {
                mPullToLoadView.post(new Runnable() {
                    @Override
                    public void run() {
                        mPullToLoadView.setComplete();
                    }
                });
                NetWorkStateUtils.errorNetMes(ExpressMainListActivity.this);
            }
        };
    }

    private synchronized void parseDate(String str, boolean isCach) {
        if (str == null) {
            showErrorView(true);
            return;
        }
        BaseDataBean<ExpressMyBean> baseDataBean = JsonUtil.parseDataObject(str, ExpressMyBean.class);
        ExpressMyBean expressMyBean = baseDataBean.data;
        if (baseDataBean.code == 100) {
            //TODO 解析快递数据
            if (isCach) {

            } else {
                if (expressMyBean.pageNo == 1) {
                    mAdapter.clear();
                }
                ArrayList<ExpressMyBean.UserExpressMyBean> userExpressList = expressMyBean.userExpressList;
                mAdapter.add(userExpressList);
                if (notifyId != -1 && mAdapter.getItemCount() > notifyId) {
                    mRecyclerView.scrollToPosition(notifyId);
                }
            }
            isLoading = false;
            if (expressMyBean.totalCount > expressMyBean.pageNo * expressMyBean.pageSize) {
                nextPage = expressMyBean.pageNo + 1;
                isHasLoadedAll = false;
            } else {
                isHasLoadedAll = true;
            }
        } else {
            toast(baseDataBean.msg);
        }
        showErrorView(mAdapter.getItemCount() <= 0);
        mPullToLoadView.setComplete();
    }

    private void showErrorView(boolean isShow) {
        if (isShow) {
            ll_express_null.setVisibility(View.VISIBLE);
            mPullToLoadView.setVisibility(View.INVISIBLE);
        } else {
            ll_express_null.setVisibility(View.INVISIBLE);
            mPullToLoadView.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onItemClick(View view, int postion) {
        MyLog.e("AAA", view.getHeight() + "postion------" + postion);
        // 如果正在执行动画，不响应
        if (isAnimationLoad) {
            return;
        }
        String color = null;
        ExpressMyBean.UserExpressMyBean userExpressMyBean = null;
        if (mAdapter.getmList() != null && mAdapter.getmList().size() > postion) {
            userExpressMyBean = mAdapter.getmList().get(postion);
            color = userExpressMyBean.comColor;
        }
//        mAdapter.notifyItemRangeChanged(0, postion);
//        mAdapter.notifyItemRangeChanged(postion + 1, mAdapter.getItemCount() - postion + 1);
        LinearLayout ll_express_expand = (LinearLayout) view.findViewById(R.id.ll_express_expand);
        if (ll_express_expand.getLayoutParams().height != 0) {
            userExpressMyBean.isOpen = false;
            if (notifyId != -1 && notifyId == postion) {
                isNotifyCustom = true;
            }
            rotateyAnimRun(view, ll_express_expand, false, color);
        } else {
            notifyIdentyPosition();
            userExpressMyBean.isOpen = true;
            rotateyAnimRun(view, ll_express_expand, true, color);
        }
        View item_mian_w = view.findViewById(R.id.item_mian_w);
        if (backgroundWith == 0) {
            backgroundWith = item_mian_w.getWidth() - CommonUtils.dip2px(ExpressMainListActivity.this, 5);
        }
        View v_express_background = view.findViewById(R.id.v_express_background);
        backgroundColorAnimRun(v_express_background);
    }

    private void notifyIdentyPosition() {
        isNotifyCustom = true;
        for (int i = 0; i < mAdapter.getmList().size(); i++) {
            if (mAdapter.getmList().get(i).isOpen) {
                mAdapter.getmList().get(i).isOpen = false;
                mAdapter.notifyItemChanged(i);
            }
        }
    }

    public void backgroundColorAnimRun(final View view) {
        MyLog.e("AAA", "view.getWidth()===" + view.getWidth());

        ViewWrapper wrapper = new ViewWrapper(view);
        ObjectAnimator objectAnimator;
        if (view.getLayoutParams().width != 0) {

            objectAnimator = ObjectAnimator.ofInt(wrapper, "width", backgroundWith, 0).setDuration(400);
            objectAnimator.start();
        } else {
            objectAnimator = ObjectAnimator.ofInt(wrapper, "width", backgroundWith).setDuration(400);
            objectAnimator.start();
        }
    }

    @Override
    public void onBackPressed() {
        press_back();
        super.onBackPressed();
    }

    private boolean isNotice = false;

    private void press_back() {
        if (isNotice && SharedPreferencesUtil.getStringData(this, Constant.ShouYeUrl.APP_STATUS, "0").equals("1")) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            saveData();
            this.finish();
        }
    }

    private void saveData() {
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
    }

    public void rotateyAnimRun(final View rootView, final View view, Boolean isShow, final String color) {
        ViewWrapper wrapper = new ViewWrapper(view);
        ObjectAnimator objectAnimator;
        if (isShow) {
            objectAnimator = ObjectAnimator.ofInt(wrapper, "height", CommonUtils.dip2px(ExpressMainListActivity.this, EXPAND_VIEW_HIGHT)).setDuration(400);
        } else {
            objectAnimator = ObjectAnimator.ofInt(wrapper, "height", CommonUtils.dip2px(ExpressMainListActivity.this, EXPAND_VIEW_HIGHT), 0).setDuration(400);
        }
        objectAnimator.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                isAnimationLoad = true;
                if (view.getLayoutParams().height != 0) {
//                    setItemTextColor(rootView,false);
                } else {
                    setItemTitleTextColor(rootView, true, color);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (view.getLayoutParams().height == 0) {
                    setItemTitleTextColor(rootView, false, color);
                }
                isAnimationLoad = false;
            }
        });
        MyLog.e("AAA", "objectAnimator-----" + objectAnimator);
        objectAnimator.start();
    }


    public void setItemTitleTextColor(View view, boolean isOpen, String color) {
        TextView tv_express_name = (TextView) view.findViewById(R.id.tv_express_name);
        TextView tv_express_time_tag = (TextView) view.findViewById(R.id.tv_express_time_tag);
        TextView tv_express_time = (TextView) view.findViewById(R.id.tv_express_time);
        TextView tv_express_order_num_tag = (TextView) view.findViewById(R.id.tv_express_order_num_tag);
        TextView tv_express_order_num = (TextView) view.findViewById(R.id.tv_express_order_num);
//        StateListDrawable drawable=new StateListDrawable();
//        tv_express_time_tag.setBackgroundDrawable(drawable);
        if (isOpen) {
            tv_express_name.setTextColor(getResources().getColor(R.color.white));
            tv_express_time_tag.setTextColor(getResources().getColor(R.color.white));
            tv_express_time.setTextColor(getResources().getColor(R.color.white));
            tv_express_order_num_tag.setTextColor(getResources().getColor(R.color.white));
            tv_express_order_num.setTextColor(getResources().getColor(R.color.white));
        } else {
            //TODO 换成当前条目的颜色
            tv_express_name.setTextColor(Color.parseColor(color));
            tv_express_time_tag.setTextColor(getResources().getColor(R.color.grey_color));
            tv_express_time.setTextColor(getResources().getColor(R.color.grey_color));
            tv_express_order_num_tag.setTextColor(getResources().getColor(R.color.light_grey_color));
            tv_express_order_num.setTextColor(getResources().getColor(R.color.light_grey_color));
        }
    }


    private class ExpressAdapter extends RecyclerView.Adapter<CellHolder> {

        private List<ExpressMyBean.UserExpressMyBean> mList;
        private MyItemClickListener myItemClickListener;

        public ExpressAdapter() {
            mList = new ArrayList<>();
        }

        public List<ExpressMyBean.UserExpressMyBean> getmList() {
            return mList;
        }

        @Override
        public CellHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.activity_express_user_item, viewGroup, false);
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
            //TODO 设置相关的数据
            ExpressMyBean.UserExpressMyBean userExpressMyBean = mList.get(i);
            MyLog.e("AAA", "userExpressMyBean.isOpen----" + userExpressMyBean.isOpen + "--i----" + i + "---" + CommonUtils.dip2px(ExpressMainListActivity.this, EXPAND_VIEW_HIGHT));
            if (userExpressMyBean.isOpen || isNotifyComeOpen(userExpressMyBean, i)) {
                holder.tv_express_name.setTextColor(Color.parseColor("#ffffff"));
                holder.ll_express_expand.getLayoutParams().height = CommonUtils.dip2px(ExpressMainListActivity.this, EXPAND_VIEW_HIGHT);
                holder.ll_express_expand.requestLayout();
                holder.v_express_background.getLayoutParams().width = 0;
                holder.v_express_background.requestLayout();
                setItemTitleTextColor(holder.ll_express_up, true, userExpressMyBean.comColor);
            } else {
                holder.tv_express_name.setTextColor(Color.parseColor(userExpressMyBean.comColor));
                holder.ll_express_expand.getLayoutParams().height = 0;
                holder.ll_express_expand.requestLayout();
                if (backgroundWith > 0) {
                    holder.v_express_background.getLayoutParams().width = backgroundWith;
                    holder.v_express_background.requestLayout();
                }
                setItemTitleTextColor(holder.ll_express_up, false, userExpressMyBean.comColor);
            }
            holder.tv_express_name.setText(userExpressMyBean.comName);
            holder.tv_express_time.setText(userExpressMyBean.arriveTimeShow);
            holder.tv_express_order_num.setText(userExpressMyBean.expressNum);
            holder.tv_qr_num.setText(userExpressMyBean.qrValue);
            holder.tv_express_orgname.setText("-- " + userExpressMyBean.orgName + " --");
            if (!TextUtils.isEmpty(userExpressMyBean.pickupQr)) {
                ImageLoader instance = ImageLoader.getInstance();
                DisplayImageOptions build = new DisplayImageOptions.Builder()
                        .showImageOnFail(R.mipmap.ic_launcher)
                        .showImageForEmptyUri(R.mipmap.ic_launcher).build();
                instance.displayImage(userExpressMyBean.pickupQr, holder.iv_express_qr, build);
            }
            //未签收
            if (userExpressMyBean.state == 1) {
                holder.tv_express_find_agent.setVisibility(View.VISIBLE);
                holder.iv_express_get_tag.setText("未签收");
            } else if (userExpressMyBean.state == 2) {
                //have been recieved
                holder.iv_express_get_tag.setText("已签收");
                holder.tv_express_find_agent.setVisibility(View.INVISIBLE);
            }
            setItemAllTextColor(holder, userExpressMyBean.comColor);
            holder.tv_express_service_des.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CommonUtils.startWebViewActivity(ExpressMainListActivity.this, "代收服务条款", Constant.Express.EXPRESS_SERVICE_INFOR);
                }
            });
            holder.tv_express_find_agent.setTag(i);
            holder.tv_express_find_agent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        int pos = Integer.parseInt(v.getTag().toString());
                        ExpressMyBean.UserExpressMyBean userExpressMyBean1 = mAdapter.getmList().get(pos);
                        //TODO 分享界面点击链接是暂时的需要更换
                        UmShareUtils.getInstance(ExpressMainListActivity.this)
                                .shareTextAndImageForExpress(userExpressMyBean1.pickupQr, "凭借二维码或二维号码把您的快递领回家", "有新的快递需要领回家[" + userExpressMyBean1.orgName + "]",
                                        userExpressMyBean1.shareUrl, new ShareSDKConfigUtil.MPlatformActionListener() {
                                            @Override
                                            public void onError(Platform platform,
                                                                int arg1, Throwable arg2) {
                                                toast("分享失败");
                                                MyLog.e("AAA", "platform--" + platform.getName() + "---arg1---" + arg1 + "arg2=====" + arg2.getMessage());
                                            }

                                            private String source;

                                            @Override
                                            public void onComplete(
                                                    Platform platform, int action,
                                                    HashMap<String, Object> res) {
                                                MyLog.e("AAA", "onComplete--" + platform.getName() + "---action---" + action);
                                                //TODO 分享成功，做相应处理
                                            }

                                            @Override
                                            public void onCancel(Platform platform,
                                                                 int arg1) {
                                                MyLog.e("AAA", "onCancel--" + platform.getName() + "---arg1---" + arg1);
                                                // TODO Auto-generated method stub
                                                toast("取消分享");
                                            }
                                        });
                    } catch (Exception e) {

                    }
                }
            });
        }

        public void add(ArrayList<ExpressMyBean.UserExpressMyBean> beans) {
            //测试需要加了三遍
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
            return mList == null ? 0 : mList.size();
        }
    }

    private boolean isNotifyComeOpen(ExpressMyBean.UserExpressMyBean userExpressMyBean, int pos) {
        if (isNotifyCustom) {
            return false;
        }
        if (TextUtils.isEmpty(express_num)) {
            return false;
        } else if (express_num.equals(userExpressMyBean.expressNum)) {
            notifyId = pos;
            userExpressMyBean.isOpen = true;
            return true;
        }

        return false;
    }

    public void setItemAllTextColor(CellHolder holder, String color) {

        holder.iv_express_get_tag.setBackgroundDrawable(CommonUtils.creatRectangleDrawble(color));
        holder.cv_express_mian.setCardBackgroundColor(Color.parseColor(color));
    }

    private class CellHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final MyItemClickListener myItemClickListener;
        private final TextView tv_express_name;
        private final TextView tv_express_time;
        private final TextView tv_express_order_num;
        private final TextView tv_qr_num;
        private final ImageView iv_express_qr;
        private final TextView tv_express_service_des;
        private final TextView tv_express_find_agent;
        private final LinearLayout ll_express_expand;
        private final LinearLayout ll_express_up;
        private final TextView tv_express_order_num_tag;
        private final TextView tv_express_time_tag;
        private final TextView iv_express_get_tag;
        private final CardView cv_express_mian;
        private final View v_express_background;
        private final TextView tv_express_orgname;

        public CellHolder(View itemView, MyItemClickListener myItemClickListener) {
            super(itemView);
            this.myItemClickListener = myItemClickListener;
            itemView.setOnClickListener(this);
//            new CardView().setPreventCornerOverlap();
            cv_express_mian = (CardView) itemView;
            tv_express_name = (TextView) itemView.findViewById(R.id.tv_express_name);
            tv_express_time = (TextView) itemView.findViewById(R.id.tv_express_time);
            tv_express_time_tag = (TextView) itemView.findViewById(R.id.tv_express_time_tag);
            tv_express_order_num = (TextView) itemView.findViewById(R.id.tv_express_order_num);
            tv_express_order_num_tag = (TextView) itemView.findViewById(R.id.tv_express_order_num_tag);
            tv_qr_num = (TextView) itemView.findViewById(R.id.tv_qr_num);
            iv_express_qr = (ImageView) itemView.findViewById(R.id.iv_express_qr);
            tv_express_orgname = (TextView) itemView.findViewById(R.id.tv_express_orgname);
            v_express_background = itemView.findViewById(R.id.v_express_background);
            v_express_background.setBackgroundDrawable(CommonUtils.creatRectangleDrawbleByDefine("#ffffff", CommonUtils.dip2px(ExpressMainListActivity.this, 5), false, false, true, true));
            tv_express_service_des = (TextView) itemView.findViewById(R.id.tv_express_service_des);
            tv_express_service_des.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
            tv_express_service_des.getPaint().setAntiAlias(true);
            tv_express_find_agent = (TextView) itemView.findViewById(R.id.tv_express_find_agent);
            iv_express_get_tag = (TextView) itemView.findViewById(R.id.iv_express_get_tag);
            ll_express_expand = (LinearLayout) itemView.findViewById(R.id.ll_express_expand);
            ll_express_up = (LinearLayout) itemView.findViewById(R.id.ll_express_up);
        }

        @Override
        public void onClick(View v) {
            myItemClickListener.onItemClick(v, getAdapterPosition());
        }
    }

    class CommodityPullCallback implements PullCallback {
        @Override
        public void onLoadMore() {
            loadData(nextPage);
        }

        @Override
        public void onRefresh() {
            if (CommonUtils.isNetworkConnected(ExpressMainListActivity.this)) {
                isHasLoadedAll = false;
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
