package anim.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.asyey.footballlibrary.network.exception.HttpException;
import com.asyey.footballlibrary.network.http.ResponseInfo;
import com.henanjianye.soon.communityo2o.BaseActivity;
import com.henanjianye.soon.communityo2o.R;
import com.henanjianye.soon.communityo2o.common.Constant;
import com.henanjianye.soon.communityo2o.common.MyLog;
import com.henanjianye.soon.communityo2o.common.UserDataHelper;
import com.henanjianye.soon.communityo2o.common.enties.BalanceInfoBean;
import com.henanjianye.soon.communityo2o.common.enties.BaseBean;
import com.henanjianye.soon.communityo2o.common.enties.JsonUtil;
import com.henanjianye.soon.communityo2o.common.util.CommonUtils;
import com.henanjianye.soon.communityo2o.common.util.FootBarHelper;
import com.henanjianye.soon.communityo2o.common.util.MyTitleBarHelper;
import com.henanjianye.soon.communityo2o.common.util.SharedPreferencesUtil;
import com.henanjianye.soon.communityo2o.interf.NetWorkCallback;

import java.util.ArrayList;
import java.util.List;


public class RestBalanceRecordActivity extends BaseActivity {
    private MyTitleBarHelper myTitleBarHelper;
    private ListView bListView;
    private BalanceAdapter mAdapter;
    private View headView;
    public static String PURSE_KEY = "purse_key";
    public static String PURSE_NUM_KEY = "purse_num_key";
    private int PurseType;//1为账户余额 2为建业通宝
    private String account_num = "0";
    private SwipeRefreshLayout swipe_balance_layout;
    private TextView tv_empty_view;
    private int pageBalanceNum = 1;//页数
    private int pageBalanceSize = 20;//一页的数据条数
    private FootBarHelper mFootBarView = null;
    private View footBar;
    private boolean isShowDialog = true;//请求网络时是否有对话框
    //    private boolean hasInsert = false;
    private boolean canBalanceLoad = true;//是否可以加载更多数据
    private TextView tv_money_icon;
    private TextView tv_balance_num;
    private TextView tv_account_type;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        initViews();
        init();
        initEvents();
    }

    private void initViews() {
        Intent intent = getIntent();
        PurseType = intent.getIntExtra(PURSE_KEY, -1);
        account_num = intent.getStringExtra(PURSE_NUM_KEY);
        myTitleBarHelper = new MyTitleBarHelper(this, getWindow().getDecorView().findViewById(android.R.id.content));
        myTitleBarHelper.setLeftImgVisible(false);
        myTitleBarHelper.resetState();
        if (PurseType == 1) {
            myTitleBarHelper.setMiddleText("余额记录");
        } else if (PurseType == 2) {
            myTitleBarHelper.setMiddleText("建业通宝记录");
        }
        myTitleBarHelper.setRightImgVisible(false);
        myTitleBarHelper.setLeftImag(R.mipmap.btn_back);
        tv_empty_view = (TextView) findViewById(R.id.tv_empty_view);
        bListView = (ListView) findViewById(R.id.balance_list);
        bListView.setEmptyView(tv_empty_view);
        //HeaderView
        headView = LayoutInflater.from(this).inflate(R.layout.rest_balance_header, null);
        tv_money_icon = (TextView) headView.findViewById(R.id.tv_money_icon);
        tv_balance_num = (TextView) headView.findViewById(R.id.tv_balance_num);
        tv_account_type = (TextView) headView.findViewById(R.id.tv_account_type);
        if (PurseType == 1) {
            //余额
            tv_money_icon.setVisibility(View.VISIBLE);
            tv_balance_num.setText(account_num);
            tv_account_type.setText("元");
        } else if (PurseType == 2) {
            //建业通宝
            tv_money_icon.setVisibility(View.GONE);
            tv_balance_num.setText(account_num);
            tv_account_type.setText("个");
        }
        bListView.addHeaderView(headView);
        //FooterView
        footBar = LayoutInflater.from(this).inflate(R.layout.item_progressbar, null);
        bListView.addFooterView(footBar);
        mFootBarView = new FootBarHelper(footBar, this);
        mFootBarView.hideFooter();
        swipe_balance_layout = (SwipeRefreshLayout) findViewById(R.id.swipe_balance_layout);
    }


    private void initEvents() {
        myTitleBarHelper.setOnclickListener(this);
        swipe_balance_layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                if (CommonUtils.isNetworkConnected(RestBalanceRecordActivity.this)) {
                    pageBalanceNum = 1;
                    isShowDialog = false;
                    if (PurseType == 1) {
                        //余额
                        requestBalanceData("1", pageBalanceNum, pageBalanceSize);
                    } else if (PurseType == 2) {
                        //建业通宝
                        requestBalanceData("2", pageBalanceNum, pageBalanceSize);
                    }

                } else {
                    swipe_balance_layout.setRefreshing(false);
                    toast("网络未连接或不可用");
                }

            }
        });
        bListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                switch (scrollState) {
                    case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                        if (canBalanceLoad && bListView.getCount() > 0
                                && bListView.getLastVisiblePosition() == (bListView
                                .getCount() - 1)) {
                            //上拉刷新停止
                            if (mAdapter == null || swipe_balance_layout.isRefreshing())
                                return;
                            if (CommonUtils.isNetworkConnected(RestBalanceRecordActivity.this)) {
                                isShowDialog = false;
                                //请求下一页数据
                                pageBalanceNum++;
                                mFootBarView.showFooter();
                                if (PurseType == 1) {
                                    //余额
                                    requestBalanceData("1", pageBalanceNum, pageBalanceSize);
                                } else if (PurseType == 2) {
                                    //建业通宝
                                    requestBalanceData("2", pageBalanceNum, pageBalanceSize);
                                }
                            } else {
                                mFootBarView.hideFooter();
                                toast("网络不可用");
                            }
                        }
                        break;
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {

            }
        });
    }

//    private List<CardInfoBean> cardInfoBeanList = new ArrayList<>();

    private void init() {
        mAdapter = new BalanceAdapter(this);
        bListView.setAdapter(mAdapter);
        if (PurseType == 1) {
            // 我的钱包缓存
            String balanceString = SharedPreferencesUtil.getStringData(this,
                    Constant.Purse.CACHERESTBALANCE, null);
            if (!TextUtils.isEmpty(balanceString)) {
                parseRestBalanceData(balanceString);
            }
        } else {
            // 建业通宝缓存
            String jianyeCoinString = SharedPreferencesUtil.getStringData(this,
                    Constant.Purse.CACHEJIANYECOIN, null);
            if (!TextUtils.isEmpty(jianyeCoinString)) {
                parseRestBalanceData(jianyeCoinString);
            }
        }

        if (CommonUtils.isNetworkConnected(this)) {
            if (PurseType == 1) {
                //余额请求
                requestBalanceData("1", pageBalanceNum, pageBalanceSize);
            } else if (PurseType == 2) {
                //建业通宝请求
                requestBalanceData("2", pageBalanceNum, pageBalanceSize);
            }
        } else {
            toast("网络不可用");
        }
//        for (int i = 0; i < 3; i++) {
//            CardInfoBean cardInfoBean = new CardInfoBean();
//            cardInfoBean.name = "Animation" + i;
//            cardInfoBeanList.add(cardInfoBean);
//        }
//        walletAdapter = new WalletAdapter(this, cardInfoBeanList);
    }

    private void requestBalanceData(String accountType, int pageNum, int pageSize) {
        new UserDataHelper(this).getAccontInfo(getNetRequestHelper(this).isShowProgressDialog(isShowDialog), accountType, pageNum, pageSize);
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
                MyLog.e("MMM", "responseInfo.result is " + responseInfo.result);
                if (swipe_balance_layout.isRefreshing()) {
                    swipe_balance_layout.setRefreshing(false);
                }
                if (requestTag.equals(Constant.Purse.ACCOUNTINFO)) {
                    if (PurseType == 1) {
                        //缓存余额
                        SharedPreferencesUtil.saveStringData(RestBalanceRecordActivity.this,
                                Constant.Purse.CACHERESTBALANCE, responseInfo.result);
                    } else {
                        //缓存建业通宝
                        SharedPreferencesUtil.saveStringData(RestBalanceRecordActivity.this,
                                Constant.Purse.CACHEJIANYECOIN, responseInfo.result);
                    }
                    mFootBarView.hideFooter();
                    parseRestBalanceData(responseInfo.result);
                }
            }

            @Override
            public void onFailure(HttpException error, String msg, String requestTag) {
                if (swipe_balance_layout.isRefreshing()) {
                    swipe_balance_layout.setRefreshing(false);
                }
            }
        };
    }

    List<BalanceInfoBean> bList = new ArrayList<>();

    private void parseRestBalanceData(String purseString) {
        try {
            BaseBean<BalanceInfoBean> bBean = JsonUtil.jsonArray(purseString, BalanceInfoBean.class);
            if (pageBalanceNum == 1) {
                //页数为第一页的时候 清除数据
                mAdapter.clear();
            }
            bList = bBean.data;
            if (bList.size() < pageBalanceSize) {
                //没有更多数据
                canBalanceLoad = false;
            } else {
                canBalanceLoad = true;
            }
            mAdapter.addAll(bList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.iv_title_bar_left:
                this.finish();
                break;
        }
    }

    public class BalanceAdapter extends BaseAdapter {
        private Context mContext;
        private LayoutInflater mLayoutInflater;
        private List<BalanceInfoBean> aList = new ArrayList<>();

        public BalanceAdapter(Context context) {
            this.mContext = context;
            this.mLayoutInflater = LayoutInflater.from(mContext);
        }

        public void addAll(List<BalanceInfoBean> bList) {
            aList.addAll(bList);
            notifyDataSetChanged();
        }

        public void clear() {
            if (aList != null && aList.size() > 0) {
                aList.clear();
            }
        }

        @Override
        public int getCount() {
            return aList.size();
        }

        @Override
        public Object getItem(int position) {
            return aList.size() > 0 ? aList.get(position) : 0;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            AccountHolder mHolder;
            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.item_account_record, parent, false);
                mHolder = new AccountHolder();
                mHolder.tv_account_info = (TextView) convertView.findViewById(R.id.tv_account_info);
                mHolder.tv_account_num = (TextView) convertView.findViewById(R.id.tv_account_num);
                mHolder.tv_account_date = (TextView) convertView.findViewById(R.id.tv_account_date);
                mHolder.tv_account_status = (TextView) convertView.findViewById(R.id.tv_account_status);
                convertView.setTag(mHolder);
            } else {
                mHolder = (AccountHolder) convertView.getTag();
            }
            BalanceInfoBean bBean = (BalanceInfoBean) getItem(position);
            if (bBean != null) {
                if (!TextUtils.isEmpty(bBean.ordercode)) {
                    mHolder.tv_account_info.setText("订单号：" + bBean.ordercode);
                } else {
                    String typeShow = "";
                    if (bBean.type.equals("01")) {
                        typeShow = "交易类型：支付宝充值";
                    } else if (bBean.type.equals("02")) {
                        typeShow = "交易类型：微信充值";
                    } else if (bBean.type.equals("03")) {
                        typeShow = "交易类型：银联充值";
                    } else if (bBean.type.equals("04")) {
                        typeShow = "交易类型：至尊卡关联";
                    } else if (bBean.type.equals("05")) {
                        typeShow = "交易类型：订单交易消费";
                    } else if (bBean.type.equals("06")) {
                        typeShow = "交易类型：支付宝提现";
                    } else if (bBean.type.equals("07")) {
                        typeShow = "交易类型：微信提现";
                    } else if (bBean.type.equals("08")) {
                        typeShow = "交易类型：银联提现";
                    } else if (bBean.type.equals("09")) {
                        typeShow = "交易类型：退款";
                    }
                    mHolder.tv_account_info.setText(typeShow);
                }
                boolean tradeFlag;
                if (bBean.type.equals("01") || bBean.type.equals("02") || bBean.type.equals("03") || bBean.type.equals("04") || bBean.type.equals("09")) {
                    tradeFlag = true;
                } else {
                    tradeFlag = false;
                }
                if (tradeFlag) {
                    mHolder.tv_account_num.setTextColor(getResources().getColor(R.color.button_color_press));
                    mHolder.tv_account_num.setText(bBean.amountShow);
                } else {
                    mHolder.tv_account_num.setTextColor(getResources().getColor(R.color.grey_color));
                    mHolder.tv_account_num.setText(bBean.amountShow);
                }
                mHolder.tv_account_date.setText(bBean.createTimeShow);
                if (bBean.state.equals("01")) {
                    mHolder.tv_account_status.setText("交易完成");
                } else {
                    mHolder.tv_account_status.setText("交易失败");
                }
            }
            return convertView;
        }

        public final class AccountHolder {
            TextView tv_account_info;
            TextView tv_account_date;
            TextView tv_account_num;
            TextView tv_account_status;
        }
    }
}
