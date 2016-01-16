package anim.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.asyey.footballlibrary.network.exception.HttpException;
import com.asyey.footballlibrary.network.http.ResponseInfo;
import com.asyey.footballlibrary.universalimageloader.core.DisplayImageOptions;
import com.asyey.footballlibrary.universalimageloader.core.ImageLoader;
import com.henanjianye.soon.communityo2o.BaseActivity;
import com.henanjianye.soon.communityo2o.R;
import com.henanjianye.soon.communityo2o.common.Constant;
import com.henanjianye.soon.communityo2o.common.UserDataHelper;
import com.henanjianye.soon.communityo2o.common.enties.BaseDataBean;
import com.henanjianye.soon.communityo2o.common.enties.ExpressReceiveBean;
import com.henanjianye.soon.communityo2o.common.enties.JsonUtil;
import com.henanjianye.soon.communityo2o.common.util.CommonUtils;
import com.henanjianye.soon.communityo2o.common.util.FootBarHelper;
import com.henanjianye.soon.communityo2o.common.util.MyTitleBarHelper;
import com.henanjianye.soon.communityo2o.interf.NetWorkCallback;
import com.henanjianye.soon.communityo2o.submitphote.CircleImageView;

import java.util.ArrayList;
import java.util.List;


public class ExpressQueryRecordActivity extends BaseActivity {
    private MyTitleBarHelper myTitleBarHelper;
    private SwipeRefreshLayout refreshLayout_no_receive;
    private SwipeRefreshLayout refreshLayout_received;
    private ListView lv_no_receive;
    private ListView lv_received;
    private Button express_no_receive;
    private Button btn_express_received;
    private NoReceivedAdapter noReceivedAdapter;
    private ReceivedAdapter receivedAdapter;
    private TextView empty_list_view;
    private UserDataHelper userDataHelper;
    private int ReceivedPageNum = 1;
    private int NoReceivePageNum = 1;
    DisplayImageOptions build = new DisplayImageOptions.Builder()
            .showImageOnFail(R.mipmap.ic_launcher)
            .showImageForEmptyUri(R.mipmap.ic_launcher).showImageOnLoading(R.mipmap.ic_launcher).build();
    private FootBarHelper mFootBarView = null;
    private View footBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query_record);
        initViews();
        initEvents();
        init();
        getData();
    }

    private void getData() {
        if (CommonUtils.isNetworkConnected(this)) {
            requestExpressNoReceive();
            requestExpressReceived();
        } else {
            toast("网络不可用");
        }
    }
    private boolean isShowDialog=true;
    private static final int pageNum=10;

    private void requestExpressNoReceive() {
        userDataHelper.getExpressReceiveStatus(getNetRequestHelper(this).isShowProgressDialog(isShowDialog), 1, NoReceivePageNum, pageNum);
    }

    private void requestExpressReceived() {
        userDataHelper.getExpressReceiveStatus(getNetRequestHelper(this).isShowProgressDialog(isShowDialog), 2, ReceivedPageNum, pageNum);
    }
    private void initViews() {
        myTitleBarHelper = new MyTitleBarHelper(this, getWindow().getDecorView().findViewById(android.R.id.content));
        myTitleBarHelper.setLeftImgVisible(false);
        myTitleBarHelper.resetState();
        myTitleBarHelper.setMiddleText("查询历史");
        myTitleBarHelper.setRightImgVisible(false);
        myTitleBarHelper.setLeftImag(R.mipmap.btn_back);
        refreshLayout_no_receive = (SwipeRefreshLayout) findViewById(R.id.swipe_no_receive_layout);
        refreshLayout_no_receive.setColorSchemeResources(R.color.repair_line_color);
        refreshLayout_received = (SwipeRefreshLayout) findViewById(R.id.swipe_received_layout);
        refreshLayout_received.setColorSchemeResources(R.color.repair_line_color);
        express_no_receive = (Button) findViewById(R.id.express_no_receive);
        btn_express_received = (Button) findViewById(R.id.btn_express_received);
        empty_list_view = (TextView) findViewById(R.id.empty_list_view);
        lv_no_receive = (ListView) findViewById(R.id.lv_no_receive);
        lv_no_receive.setEmptyView(empty_list_view);
        lv_received = (ListView) findViewById(R.id.lv_received);
        lv_received.setEmptyView(empty_list_view);
        footBar = LayoutInflater.from(this).inflate(R.layout.item_progressbar, null);
        lv_no_receive.addFooterView(footBar);
        lv_received.addFooterView(footBar);
        mFootBarView = new FootBarHelper(footBar, this);
        mFootBarView.hideFooter();
    }

    private void initEvents() {
        myTitleBarHelper.setOnclickListener(this);
        express_no_receive.setOnClickListener(this);
        btn_express_received.setOnClickListener(this);
        refreshLayout_no_receive.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                if (CommonUtils.isNetworkConnected(ExpressQueryRecordActivity.this)) {
                    NoReceiveCanLoad = true;
                    NoReceivePageNum = 1;
                    isShowDialog=false;
                    requestExpressNoReceive();
                } else {
                    if (refreshLayout_no_receive != null && refreshLayout_no_receive.isRefreshing()) {
                        refreshLayout_no_receive.setRefreshing(false);
                    }
                    toast("网络不可用");
                }
            }
        });
        lv_no_receive.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                switch (scrollState) {
                    case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                        if (NoReceiveCanLoad && lv_no_receive.getCount() >= 0
                                && lv_no_receive.getLastVisiblePosition() == (lv_no_receive
                                .getCount() - 1)) {
                            //上拉刷新停止
                            if (noReceivedAdapter == null || refreshLayout_no_receive.isRefreshing())
                                return;
                            if (CommonUtils.isNetworkConnected(ExpressQueryRecordActivity.this)) {
                                isShowDialog=false;
                                //请求下一页数据
                                NoReceivePageNum++;
                                mFootBarView.showFooter();
                                requestExpressNoReceive();
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

        refreshLayout_received.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                if (CommonUtils.isNetworkConnected(ExpressQueryRecordActivity.this)) {
                    isShowDialog=false;
                    ReceivedCanLoad = true;
                    ReceivedPageNum = 1;
                    requestExpressReceived();
                } else {
                    if (refreshLayout_received != null && refreshLayout_received.isRefreshing()) {
                        refreshLayout_received.setRefreshing(false);
                    }
                    toast("网络不可用");
                }
            }
        });
        lv_received.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                switch (scrollState) {
                    case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                        if (ReceivedCanLoad && lv_received.getCount() >= 0
                                && lv_received.getLastVisiblePosition() == (lv_received
                                .getCount() - 1)) {
                            //上拉刷新停止
                            if (receivedAdapter == null || refreshLayout_received.isRefreshing())
                                return;
                            if (CommonUtils.isNetworkConnected(ExpressQueryRecordActivity.this)) {
                                isShowDialog=false;
                                //请求下一页数据
                                ReceivedPageNum++;
                                mFootBarView.showFooter();
                                requestExpressReceived();
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

    private void init() {
        userDataHelper = new UserDataHelper(this);
        receivedAdapter = new ReceivedAdapter(this);
        lv_received.setAdapter(receivedAdapter);
        empty_list_view.setVisibility(View.GONE);
        refreshLayout_received.setVisibility(View.GONE);
        noReceivedAdapter = new NoReceivedAdapter(this);
        lv_no_receive.setAdapter(noReceivedAdapter);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.iv_title_bar_left:
                this.finish();
                break;
            case R.id.express_no_receive:
                empty_list_view.setVisibility(View.GONE);
                noReceivedAdapter.notifyDataSetChanged();
                refreshLayout_received.setRefreshing(false);
                refreshLayout_received.setVisibility(View.GONE);
                refreshLayout_no_receive.setVisibility(View.VISIBLE);
                express_no_receive.setBackgroundColor(getResources().getColor(R.color.repair_line_color));
                express_no_receive.setTextColor(getResources().getColor(R.color.white));
                btn_express_received.setTextColor(getResources().getColor(R.color.repair_line_color));
                btn_express_received.setBackgroundColor(getResources().getColor(R.color.white));
                break;
            case R.id.btn_express_received:
                empty_list_view.setVisibility(View.GONE);
                receivedAdapter.notifyDataSetChanged();
                refreshLayout_no_receive.setRefreshing(false);
                refreshLayout_no_receive.setVisibility(View.GONE);
                refreshLayout_received.setVisibility(View.VISIBLE);
                btn_express_received.setTextColor(getResources().getColor(R.color.white));
                btn_express_received.setBackgroundColor(getResources().getColor(R.color.repair_line_color));
                express_no_receive.setTextColor(getResources().getColor(R.color.repair_line_color));
                express_no_receive.setBackgroundColor(getResources().getColor(R.color.white));
                break;
            default:
                break;
        }
    }

    private int tempPos = 0;

    //快递未领取
    class NoReceivedAdapter extends BaseAdapter {
        ExpressReceiveBean.ExpressBean eBean;
        private LayoutInflater mInflater;
        ArrayList<ExpressReceiveBean.ExpressBean> nList = new ArrayList<>();

        public NoReceivedAdapter(Context mContext) {
            this.mInflater = LayoutInflater.from(mContext);
        }

        @Override
        public int getCount() {
            if (nList != null && nList.size() > 0) {
                return nList.size();
            }
            return 0;
        }

        @Override
        public ExpressReceiveBean.ExpressBean getItem(int position) {
            if (nList != null && nList.size() > 0) {
                return nList.get(position);
            } else {
                return null;
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ExpressHolder nHolder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_express_no_receive, parent, false);
                nHolder = new ExpressHolder();
                nHolder.tv_name = (TextView) convertView.findViewById(R.id.tv_express_name);
                nHolder.tv_express_notice = (TextView) convertView.findViewById(R.id.tv_express_notice);
                nHolder.tv_express_phone = (TextView) convertView.findViewById(R.id.tv_express_phone);
                nHolder.tv_reach_time = (TextView) convertView.findViewById(R.id.tv_express_reachTime);
                nHolder.tv_express_company = (TextView) convertView.findViewById(R.id.tv_express_company);
                nHolder.iv_portrait = (CircleImageView) convertView.findViewById(R.id.iv_portrait);
                nHolder.rl_notice = (RelativeLayout) convertView.findViewById(R.id.rl_notice);
                nHolder.tv_express_company_num= (TextView) convertView.findViewById(R.id.tv_express_company_num);
                convertView.setTag(nHolder);
            } else {
                nHolder = (ExpressHolder) convertView.getTag();
            }
            eBean = getItem(position);
            if (eBean != null) {
                if (eBean.canNotify) {
                    //可以通知
                    nHolder.tv_express_notice.setBackgroundDrawable(CommonUtils.creatRectangleDrawble("#a2e542"));
                    nHolder.tv_express_notice.setClickable(true);
                    nHolder.tv_express_notice.setEnabled(true);
                } else {
                    //不可以通知
                    nHolder.tv_express_notice.setBackgroundDrawable(CommonUtils.creatRectangleDrawble("#d0d0d0"));
                    nHolder.tv_express_notice.setClickable(false);
                    nHolder.tv_express_notice.setEnabled(false);
                }
                //头像
                if (eBean.packageUserAvatar != null && !TextUtils.isEmpty(eBean.packageUserAvatar.smallPicUrl)) {
                    ImageLoader.getInstance().displayImage(eBean.packageUserAvatar.smallPicUrl, nHolder.iv_portrait, build);
                } else {
                    nHolder.iv_portrait.setImageResource(R.mipmap.ic_launcher);
                }
                nHolder.tv_name.setText(eBean.packageUserName);
                nHolder.tv_express_phone.setText(eBean.packagePhone);
                nHolder.tv_reach_time.setText("到达时间: " + eBean.arriveTimeShow);
                eBean.comName=eBean.comName.replace("（","(").replace("）",")");
                nHolder.tv_express_company.setText(eBean.comName+":");
                nHolder.tv_express_company_num.setText(eBean.expressNum);
                nHolder.tv_express_notice.setTag(position);
                nHolder.tv_express_notice.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (CommonUtils.isNetworkConnected(ExpressQueryRecordActivity.this)) {
                            Object object = v.getTag();
                            if (object != null) {
                                int pos = Integer.parseInt(object.toString());
                                nHolder.tv_express_notice.setClickable(false);
                                nHolder.tv_express_notice.setEnabled(false);
                                tempPos = pos;
                                requestExpressNotice(getItem(pos).expressNum);
                            }
                        } else {
                            toast("网络不可用");
                        }
                    }
                });
            }
            return convertView;
        }

        public class ExpressHolder {
            TextView tv_name;
            TextView tv_express_notice;
            TextView tv_express_phone;
            TextView tv_reach_time;
            TextView tv_express_company;
            TextView tv_express_company_num;
            CircleImageView iv_portrait;
            RelativeLayout rl_notice;
        }

        public void add(List<ExpressReceiveBean.ExpressBean> list) {
            //只有请求第一页的时候清空数据  否则认为是上拉加载更多
            if (NoReceivePageNum == 1 && nList.size() > 0) {
                nList.clear();
            }
            nList.addAll(list);
            notifyDataSetChanged();
        }

        public void clear() {
            if (nList != null && nList.size() > 0) {
                nList.clear();
                notifyDataSetChanged();
            }
        }
    }


    private void updateView(int index) {
        int firstVisiblePosition = lv_no_receive.getFirstVisiblePosition();
        int lastVisiblePosition = lv_no_receive.getLastVisiblePosition();
        if (index >= firstVisiblePosition && index <= lastVisiblePosition) {
            View view = lv_no_receive.getChildAt(index - firstVisiblePosition);
            if (view.getTag() instanceof NoReceivedAdapter.ExpressHolder) {
                NoReceivedAdapter.ExpressHolder vh = (NoReceivedAdapter.ExpressHolder) view.getTag();
                vh.tv_express_notice.setBackgroundDrawable(CommonUtils.creatRectangleDrawble("#d0d0d0"));
                //改变状态 不能再通知
                noReceivedAdapter.getItem(index).canNotify = false;
            }
        }
    }


    private void requestExpressNotice(String expressNum) {
        userDataHelper.getExpressNotice(getNetRequestHelper(this).isShowProgressDialog(true), expressNum);
    }

    //快递已领取
    class ReceivedAdapter extends BaseAdapter {
        ExpressReceiveBean.ExpressBean eBean;
        private LayoutInflater mInflater;
        ArrayList<ExpressReceiveBean.ExpressBean> rList = new ArrayList<>();

        public ReceivedAdapter(Context mContext) {
            this.mInflater = LayoutInflater.from(mContext);
        }

        @Override
        public int getCount() {
            if (rList != null && rList.size() > 0) {
                return rList.size();
            }
            return 0;
        }

        @Override
        public ExpressReceiveBean.ExpressBean getItem(int position) {
            if (rList != null && rList.size() > 0) {
                return rList.get(position);
            } else {
                return null;
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            eBean = getItem(position);
            final ExpressHolder nHolder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_express_received, parent, false);
                nHolder = new ExpressHolder();
                nHolder.tv_name = (TextView) convertView.findViewById(R.id.tv_express_name);
                nHolder.tv_express_phone = (TextView) convertView.findViewById(R.id.tv_express_phone);
                nHolder.tv_reach_time = (TextView) convertView.findViewById(R.id.tv_express_reachTime);
                nHolder.tv_express_company = (TextView) convertView.findViewById(R.id.tv_express_company);
                nHolder.iv_portrait = (ImageView) convertView.findViewById(R.id.iv_portrait);
                nHolder.rl_notice = (RelativeLayout) convertView.findViewById(R.id.rl_express_received);
                nHolder.tv_express_company_num= (TextView) convertView.findViewById(R.id.tv_express_company_num);
                convertView.setTag(nHolder);
            } else {
                nHolder = (ExpressHolder) convertView.getTag();
            }
            if (eBean != null) {
                //头像
                if (eBean.packageUserAvatar != null && !TextUtils.isEmpty(eBean.packageUserAvatar.smallPicUrl)) {
                    ImageLoader.getInstance().displayImage(eBean.packageUserAvatar.smallPicUrl, nHolder.iv_portrait, build);
                } else {
                    nHolder.iv_portrait.setImageResource(R.mipmap.ic_launcher);
                }
                nHolder.tv_name.setText(eBean.packageUserName);
                nHolder.tv_express_phone.setText(eBean.packagePhone);
                nHolder.tv_reach_time.setText("领取时间: " + eBean.arriveTimeShow);
                eBean.comName=eBean.comName.replace("（","(").replace("）",")");
                nHolder.tv_express_company.setText(eBean.comName+":");
                nHolder.tv_express_company_num.setText(eBean.expressNum);
            }
            nHolder.rl_notice.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });
            return convertView;
        }

        public final class ExpressHolder {
            TextView tv_name;
            TextView tv_express_phone;
            TextView tv_reach_time;
            TextView tv_express_company;
            TextView tv_express_company_num;
            ImageView iv_portrait;
            RelativeLayout rl_notice;
        }

        public void add(List<ExpressReceiveBean.ExpressBean> list) {
            if (ReceivedPageNum == 1 && rList.size() > 0) {
                rList.clear();
            }
            rList.addAll(list);
            notifyDataSetChanged();
        }

        public void clear() {
            if (rList != null && rList.size() > 0) {
                rList.clear();
                notifyDataSetChanged();
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
                if (requestTag.equals(Constant.Express.EXPRESS_RECEIVE_STATUS)) {// 快递签收和未签收
                    if (refreshLayout_no_receive != null && refreshLayout_no_receive.isRefreshing()) {
                        refreshLayout_no_receive.setRefreshing(false);
                    }
                    if (refreshLayout_received != null && refreshLayout_received.isRefreshing()) {
                        refreshLayout_received.setRefreshing(false);
                    }
                    parseExpressData(responseInfo.result);
                } else if (requestTag.equals(Constant.Express.EXPRESS_EXPRESS_NOTICE)) {
                    try {
                        BaseDataBean<Object> json = JsonUtil.parseDataObject(responseInfo.result, Object.class);
                        if (json.code == 100) {
                            //再次通知按钮的背景颜色变灰
                            updateView(tempPos);
                            toast(json.msg);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(HttpException error, String msg, String requestTag) {
                if (refreshLayout_no_receive != null && refreshLayout_no_receive.isRefreshing()) {
                    refreshLayout_no_receive.setRefreshing(false);
                }
                if (refreshLayout_received != null && refreshLayout_received.isRefreshing()) {
                    refreshLayout_received.setRefreshing(false);
                }
            }
        };
    }

    private List<ExpressReceiveBean.ExpressBean> receivedList;
    private List<ExpressReceiveBean.ExpressBean> NoReceiveList = new ArrayList<>();
    private boolean NoReceiveCanLoad = true;
    private boolean ReceivedCanLoad = true;

    private void parseExpressData(String result) {
        try {
            BaseDataBean<ExpressReceiveBean> json = JsonUtil.parseDataObject(result, ExpressReceiveBean.class);
            if (json.code == 100) {
                if (json.data != null) {
                    //隐藏进度条
                    mFootBarView.hideFooter();
                    if (json.data.state == 1) {
                        if (json.data.managerExpressList.size() > 0) {
                            //未签收
                            NoReceiveList = json.data.managerExpressList;
                            noReceivedAdapter.add(NoReceiveList);
                            if(json.data.managerExpressList.size()<pageNum){
                                //加载的数据数目少于请求的数目，说明没有数据了
                                NoReceiveCanLoad = false;
                            }
                        } else {
                            //已经没有数据了，不能加载了
                            NoReceiveCanLoad = false;
                        }
                    } else if (json.data.state == 2) {
                        if (json.data.managerExpressList.size() > 0) {
                            //已签收
                            receivedList = json.data.managerExpressList;
                            receivedAdapter.add(receivedList);
                            if(json.data.managerExpressList.size()<pageNum){
                                //加载的数据数目少于请求的数目，说明没有数据了
                                ReceivedCanLoad = false;
                            }
                        }else{
                            ReceivedCanLoad=false;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
