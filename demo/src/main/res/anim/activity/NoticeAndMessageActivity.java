package anim.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.asyey.footballlibrary.network.exception.HttpException;
import com.asyey.footballlibrary.network.http.ResponseInfo;
import com.henanjianye.soon.communityo2o.BaseActivity;
import com.henanjianye.soon.communityo2o.R;
import com.henanjianye.soon.communityo2o.common.Constant;
import com.henanjianye.soon.communityo2o.common.UserDataHelper;
import com.henanjianye.soon.communityo2o.common.enties.BaseDataBean;
import com.henanjianye.soon.communityo2o.common.enties.JsonUtil;
import com.henanjianye.soon.communityo2o.common.enties.NewsBean;
import com.henanjianye.soon.communityo2o.common.enties.NoticeBean;
import com.henanjianye.soon.communityo2o.common.util.CommonUtils;
import com.henanjianye.soon.communityo2o.common.util.FootBarHelper;
import com.henanjianye.soon.communityo2o.common.util.MyTitleBarHelper;
import com.henanjianye.soon.communityo2o.common.util.NetWorkStateUtils;
import com.henanjianye.soon.communityo2o.common.util.SharedPreferencesUtil;
import com.henanjianye.soon.communityo2o.common.util.UserSharedPreferencesUtil;
import com.henanjianye.soon.communityo2o.interf.NetWorkCallback;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;

public class NoticeAndMessageActivity extends BaseActivity {
    private MyTitleBarHelper myTitleBarHelper;
    private SwipeRefreshLayout refreshLayout;
    private SwipeRefreshLayout swipe_mess_layout;
    private ListView lv_notice;
    private NoticeAdapter nAdapter;
    private ListView lv_message;
    private MessageAdapter mAdapter;
    private Button btn_notice;
    private Button btn_message;
    private ImageView red_noti_icon;
    //    private ImageView red_mess_icon;
    private int notMessRead = 0;
    private int notNoticeRead = 0;
    private TextView no_noti;
    private TextView no_mess;
    //为0代表通知  1为消息
    private int currentPage = 0;
    public static final String EXPRESS_NUM = "express_num";
    private FootBarHelper mFootBarView = null;
    private View footBar;
    private int delete_type = -1;//0为通知  1为新闻

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice_and_message);
        initViews();
        initEvents();
        init();
        getData();
    }

    private void saveData() {
        //保存未读通知和消息的总数量
        SharedPreferencesUtil.saveIntData(this,
                Constant.ShouYeUrl.NOTICE_MESS_NUM, notMessRead + notNoticeRead);
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
    }

    private int orgId;

    private void initViews() {
        myTitleBarHelper = new MyTitleBarHelper(this, getWindow().getDecorView().findViewById(android.R.id.content));
        myTitleBarHelper.setLeftImgVisible(false);
        myTitleBarHelper.resetState();
        myTitleBarHelper.setMiddleText("通知和新闻");
        myTitleBarHelper.setRightImgVisible(false);
        myTitleBarHelper.setLeftImag(R.mipmap.btn_back);
        myTitleBarHelper.setRightTxVisible(false);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);
        refreshLayout.setColorSchemeResources(R.color.repair_line_color);
        no_noti = (TextView) findViewById(R.id.no_noti);
        no_mess = (TextView) findViewById(R.id.no_mess);
        footBar = LayoutInflater.from(this).inflate(R.layout.item_progressbar, null);
        lv_notice = (ListView) findViewById(R.id.lv_notice);
        lv_notice.setEmptyView(no_noti);
        lv_notice.addFooterView(footBar);
        swipe_mess_layout = (SwipeRefreshLayout) findViewById(R.id.swipe_mess_layout);
        swipe_mess_layout.setColorSchemeResources(R.color.repair_line_color);
        lv_message = (ListView) findViewById(R.id.lv_message);
        lv_message.addFooterView(footBar);
        lv_message.setEmptyView(no_mess);
        btn_notice = (Button) findViewById(R.id.btn_notice);
        btn_message = (Button) findViewById(R.id.btn_message);
        orgId = UserSharedPreferencesUtil.getUserInfo(this).orgId;
        red_noti_icon = (ImageView) findViewById(R.id.notice_red_icon);
//        red_mess_icon = (ImageView) findViewById(R.id.mess_red_icon);
        red_noti_icon.setVisibility(View.GONE);
//        red_mess_icon.setVisibility(View.GONE);
        mFootBarView = new FootBarHelper(footBar, this);
        mFootBarView.hideFooter();
    }

    private int pageNum = 1;
    private int pageSize = 20;
    private int pageMessNum = 1;
    private int pageMessSize = 20;
    private boolean isLoading = false;
    private boolean isMessLoading = false;

    private void initEvents() {
        myTitleBarHelper.setOnclickListener(this);
        btn_notice.setOnClickListener(this);
        btn_message.setOnClickListener(this);
        lv_notice.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//                lastItem = firstVisibleItem + visibleItemCount;
            }

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                switch (scrollState) {
                    case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                        if (lv_notice.getLastVisiblePosition() == lv_notice.getCount() - 1) {
                            if (CommonUtils.isNetworkConnected(NoticeAndMessageActivity.this)) {
                                // 服务器还有数据 并且没有上拉刷新 通知列表没有数据
                                if (canLoad && !isLoading && lv_notice.getCount() > 0 && !refreshLayout.isRefreshing() && nAdapter.getCount() != 0) {
                                    pageNum++;
                                    mFootBarView.showFooter();
                                    isLoading = true;
                                    requestNoticeData(orgId, pageNum, pageSize);
                                } else {
                                    mFootBarView.hideFooter();
                                }
                            } else {
                                toast("网络未连接或不可用");
                            }
                        }
                        break;
                }
            }
        });
        lv_message.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//                lastItem = firstVisibleItem + visibleItemCount;
            }

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                switch (scrollState) {
                    case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                        if (CommonUtils.isNetworkConnected(NoticeAndMessageActivity.this)) {
                            if (lv_message.getLastVisiblePosition() == lv_message.getCount() - 1) {
                                if (canMessLoad && !isMessLoading && lv_message.getCount() > 0 && !swipe_mess_layout.isRefreshing() && mAdapter.getCount() != 0) {
                                    pageMessNum++;
                                    mFootBarView.showFooter();
                                    isMessLoading = true;
                                    requestNewsData(orgId, pageMessNum, pageMessSize);
                                } else {
                                    mFootBarView.hideFooter();
                                }
                            }
                        } else {
                            toast("网络未连接或不可用");
                        }
                        break;
                }
            }
        });
    }

    private void init() {
        //消息列表
        nAdapter = new NoticeAdapter(this);
        lv_notice.setAdapter(nAdapter);
        refreshLayout.setVisibility(View.VISIBLE);
        //消息列表
        mAdapter = new MessageAdapter(this);
        lv_message.setAdapter(mAdapter);
        no_mess.setVisibility(View.GONE);
        swipe_mess_layout.setVisibility(View.GONE);
        // 注意这里的是SwipeRefreshLayout.OnRefreshListener
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                pageNum = 1;
                notNoticeRead = 0;
                requestNoticeData(orgId, pageNum, pageSize);
            }
        });
        swipe_mess_layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                pageMessNum = 1;
                notMessRead = 0;
                requestNewsData(orgId, 1, pageMessSize);
            }
        });
    }

    private void changeMessAllRead() {
        new UserDataHelper(this).getMessageAllRead(getNetRequestHelper(this).isShowProgressDialog(true));
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.iv_title_bar_left:
                saveData();
                this.finish();
                break;
            case R.id.tv_title_bar_right:
                changeMessAllRead();
                break;
            case R.id.iv_title_bar_right:
                switch (currentPage) {
                    case 0:
                        //删除通知
                        toast("删除通知");
                        createDialog(D_TYPE.NOTI, 0, "0");
                        break;
                    case 1:
                        //删除消息
                        toast("删除消息");
                        createDialog(D_TYPE.MESS, 0, "0");
                        break;
                    default:
                        break;
                }
                break;
            case R.id.btn_notice:
                MobclickAgent.onEvent(NoticeAndMessageActivity.this,"NoticeInfoTabTap");
                currentPage = 0;
                no_mess.setVisibility(View.GONE);
                nAdapter.notifyDataSetChanged();
                refreshLayout.setVisibility(View.VISIBLE);
                if (swipe_mess_layout != null) {
                    swipe_mess_layout.setRefreshing(false);
                    swipe_mess_layout.setVisibility(View.GONE);
                }
                btn_notice.setBackgroundColor(getResources().getColor(R.color.repair_line_color));
                btn_notice.setTextColor(getResources().getColor(R.color.white));
                btn_message.setTextColor(getResources().getColor(R.color.black));
                btn_message.setBackgroundColor(getResources().getColor(R.color.white));
                break;
            case R.id.btn_message:
                MobclickAgent.onEvent(NoticeAndMessageActivity.this,"NewsInfoTabTap");
                isShowEmptyMess = true;
                no_noti.setVisibility(View.GONE);
                mAdapter.notifyDataSetChanged();
                currentPage = 1;
                if (refreshLayout != null) {
                    refreshLayout.setRefreshing(false);
                    refreshLayout.setVisibility(View.GONE);
                }
                swipe_mess_layout.setVisibility(View.VISIBLE);
                btn_message.setTextColor(getResources().getColor(R.color.white));
                btn_message.setBackgroundColor(getResources().getColor(R.color.repair_line_color));
                btn_notice.setTextColor(getResources().getColor(R.color.black));
                btn_notice.setBackgroundColor(getResources().getColor(R.color.white));
                break;

        }
    }


    private enum D_TYPE {ITEM_NOTI, ITEM_NEWS, NOTI, MESS}

    private void createDialog(final D_TYPE type, final int deleId, final String messId) {
        //布局文件转换为view对象
        LayoutInflater inflaterDl = LayoutInflater.from(this);
        LinearLayout layout = (LinearLayout) inflaterDl.inflate(R.layout.delete_noti_and_aess, null);
        //对话框
        final Dialog dialog = new AlertDialog.Builder(NoticeAndMessageActivity.this).create();
        dialog.show();
//        dialog.setCancelable(false);
        dialog.getWindow().setContentView(layout);
        //标题
        TextView tilte_tv = (TextView) layout.findViewById(R.id.tv_title);
        tilte_tv.setText("提示");
        //取消按钮
        ImageView btnCancel = (ImageView) layout.findViewById(R.id.btn_close);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        //确认删除
        TextView tv_confirm = (TextView) layout.findViewById(R.id.tv_confirm);
        tv_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (type.equals(D_TYPE.ITEM_NOTI)) {
                    //删除一条通知
                    if (CommonUtils.isNetworkConnected(NoticeAndMessageActivity.this)) {
                        removeItemNotice(deleId);
                    } else {
                        toast("网络未连接或不可用");
                    }
                } else if (type.equals(D_TYPE.NOTI)) {
                    //清空通知
                    if (CommonUtils.isNetworkConnected(NoticeAndMessageActivity.this)) {
                        clearNotice();
                    } else {
                        toast("网络未连接或不可用");
                    }
                } else if (type.equals(D_TYPE.ITEM_NEWS)) {
                    //删除一条消息
                    if (CommonUtils.isNetworkConnected(NoticeAndMessageActivity.this)) {
                        removeItemNotice(deleId);
                    } else {
                        toast("网络未连接或不可用");
                    }
                }
//                else if (type.equals(D_TYPE.ITEM_MESS)) {
//                    //删除一条消息
//                    if (CommonUtils.isNetworkConnected(NoticeAndMessageActivity.this)) {
//                        removeItemMess(messId);
//                    } else {
//                        toast("网络未连接或不可用");
//                    }
//                }
// else if (type.equals(D_TYPE.MESS)) {
//                    //清空消息
//                    if (CommonUtils.isNetworkConnected(NoticeAndMessageActivity.this)) {
//                        clearMess();
//                    } else {
//                        toast("网络未连接或不可用");
//                    }
//                }
                dialog.dismiss();
            }
        });
        //取消删除
        TextView tv_cancel = (TextView) layout.findViewById(R.id.tv_cancel);
        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }


    public static final String NOTICE_URL = "notice_url";
    private int delePos = -1;

    //通知
    class NoticeAdapter extends BaseAdapter {
        NoticeBean.Notice mNotice;
        private LayoutInflater mInflater;
        ArrayList<NoticeBean.Notice> nList = new ArrayList<>();

        public NoticeAdapter(Context mContext) {
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
        public NoticeBean.Notice getItem(int position) {
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
        public View getView(final int position, View convertView, ViewGroup parent) {
            mNotice = getItem(position);
            final NoticeHolder nHolder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_notice, parent, false);
                nHolder = new NoticeHolder();
                nHolder.tv_Content = (TextView) convertView.findViewById(R.id.tv_content);
                nHolder.tv_Time = (TextView) convertView.findViewById(R.id.tv_time);
                nHolder.iv_More = (ImageView) convertView.findViewById(R.id.iv_more);
                nHolder.rl_notice = (RelativeLayout) convertView.findViewById(R.id.rl_notice);
                convertView.setTag(nHolder);
            } else {
                nHolder = (NoticeHolder) convertView.getTag();
            }
            if (mNotice != null) {
                //0表示未读 内容颜色深  1表示已读 内容颜色浅
                if (mNotice.isRead == 0) {
                    red_noti_icon.setVisibility(View.VISIBLE);
                    nHolder.tv_Content.setTextColor(getResources().getColor(R.color.word_normal_color));
                } else {
                    nHolder.tv_Content.setTextColor(getResources().getColor(R.color.light_grey_color));
                }
                nHolder.tv_Content.setText(mNotice.discription);
                nHolder.tv_Time.setText(mNotice.createTimeShow);
                nHolder.rl_notice.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mNotice = getItem(position);
                        if (CommonUtils.isNetworkConnected(NoticeAndMessageActivity.this)) {
                            if (mNotice != null) {
                                nHolder.tv_Content.setTextColor(getResources().getColor(R.color.light_grey_color));
                                if (mNotice.isRead == 0) {
                                    mNotice.isRead = 1;
                                    //通知消息总数量减一
                                    notNoticeRead--;
                                    if (notNoticeRead == 0) {
                                        //总的未读通知为0时隐藏红点
                                        red_noti_icon.setVisibility(View.GONE);
                                    }
                                    changeReadStatus(mNotice.noticeId);
                                }
                                //CommonUtils.startWebViewActivity(NoticeAndMessageActivity.this, "消息通知", mNotice.pageUrl);
                                CommonUtils.startWebViewHaveShareActivity(NoticeAndMessageActivity.this, "消息通知", mNotice.pageUrl, mNotice.discription, "消息通知", "");
//                                Intent intent = new Intent(NoticeAndMessageActivity.this, NoticeDetailActivity.class);
//                                intent.putExtra(NOTICE_URL, mNotice.pageUrl);
//                                startActivity(intent);
                            }
                        } else {
                            toast("网络未连接或不可用");
                        }
                    }
                });
                nHolder.rl_notice.setOnLongClickListener(new OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        //要删除的当前的位置
                        delePos = position;
                        delete_type = 0;
                        mNotice = getItem(position);
                        createDialog(D_TYPE.ITEM_NOTI, mNotice.noticeId, "0");
                        return false;
                    }
                });
            }
            return convertView;
        }

        public final class NoticeHolder {
            TextView tv_Content;
            TextView tv_Time;
            ImageView iv_More;
            RelativeLayout rl_notice;
        }

        public void add(ArrayList<NoticeBean.Notice> list) {
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

    //新闻
    class MessageAdapter extends BaseAdapter {
        private LayoutInflater mLayoutInflater;
        NewsBean.News news;
        ArrayList<NewsBean.News> mList = new ArrayList<>();

        public MessageAdapter(Context mContext) {
            this.mLayoutInflater = LayoutInflater.from(mContext);
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object getItem(int position) {
            if (mList != null && mList.size() > 0) {
                return mList.get(position);
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
            news = (NewsBean.News) getItem(position);
            final MessageHolder mHolder;
            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.item_news, parent, false);
                mHolder = new MessageHolder();
                mHolder.tv_mess_Content = (TextView) convertView.findViewById(R.id.tv_mess_content);
                mHolder.tv_mess_Time = (TextView) convertView.findViewById(R.id.tv_mess_time);
                mHolder.card_view = (RelativeLayout) convertView.findViewById(R.id.card_view);
                convertView.setTag(mHolder);
            } else {
                mHolder = (MessageHolder) convertView.getTag();
            }
            mHolder.tv_mess_Content.setTextColor(getResources().getColor(R.color.word_normal_color));
            if (news != null) {
                //0表示未读 内容颜色深  1表示已读 内容颜色浅
                if (news.isRead == 0) {
                    mHolder.tv_mess_Content.setTextColor(getResources().getColor(R.color.word_normal_color));
                } else {
                    mHolder.tv_mess_Content.setTextColor(getResources().getColor(R.color.light_grey_color));
                }
                mHolder.tv_mess_Content.setText(news.discription);
                mHolder.tv_mess_Time.setText(news.createTimeShow);
                mHolder.card_view.setTag(position);
                mHolder.card_view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Object object = v.getTag();
                        if (object != null) {
                            int pos = Integer.parseInt(object.toString());
                            NewsBean.News news = (NewsBean.News) getItem(pos);
                            if (news != null) {
                                if (CommonUtils.isNetworkConnected(NoticeAndMessageActivity.this)) {
                                    if (news.isRead == 0) {
                                        mHolder.tv_mess_Content.setTextColor(getResources().getColor(R.color.light_grey_color));
                                        news.isRead = 1;
                                        //通知消息总数量减一
                                        notMessRead--;
                                        if (notMessRead == 0) {
                                            //总的未读通知为0时隐藏红点
                                            red_noti_icon.setVisibility(View.GONE);
                                        }
                                        changeReadStatus(news.noticeId);
                                    }
                                    CommonUtils.startWebViewActivity(NoticeAndMessageActivity.this, "新闻", news.pageUrl);
                                } else {
                                    toast("网络未连接或不可用");
                                }
                            }
                        }
                    }
                });
                mHolder.card_view.setOnLongClickListener(new OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        Object object = v.getTag();
                        if (object != null) {
                            int pos = Integer.parseInt(object.toString());
                            //要删除的当前的位置
                            delePos = pos;
                            delete_type = 1;
                            NewsBean.News news = (NewsBean.News) getItem(pos);
//                            MessageBean.Message message = (MessageBean.Message) getItem(pos);
                            createDialog(D_TYPE.ITEM_NEWS, news.noticeId, "0");

                        }
                        return false;
                    }
                });
            }
            return convertView;
        }

        public final class MessageHolder {
            TextView tv_mess_Content;
            TextView tv_mess_Time;
            RelativeLayout card_view;
        }

        //        public void add(ArrayList<MessageBean> list) {
//            mList.addAll(list);
//            notifyDataSetChanged();
//        }
        public void add(NewsBean.News bean) {
            mList.add(bean);
            notifyDataSetChanged();
        }

        public void clear() {
            if (mList != null && mList.size() > 0) {
                mList.clear();
                notifyDataSetChanged();
            }
        }
    }

    public enum TYPE {CACHE, NET}

    private void getData() {
        //1. 通知数据缓存
        String noticeString = SharedPreferencesUtil.getStringData(this,
                Constant.ShouYeUrl.NOTICE, null);
        // 通知JSON解析
        if (!TextUtils.isEmpty(noticeString)) {// 解析数据
            parseNoticeRecord(noticeString, TYPE.CACHE);
        }
        //2. 消息数据缓存
        String messageString = SharedPreferencesUtil.getStringData(this,
                Constant.ShouYeUrl.NEWS, null);
        // 消息JSON解析
        if (!TextUtils.isEmpty(messageString)) {// 解析数据
            parseNews(messageString, TYPE.CACHE);
        }
        if (CommonUtils.isNetworkConnected(this)) {
            if (orgId != -1) {
                // 通知网络请求
                requestNoticeData(orgId, 1, pageSize);
                //消息网络请求
                requestNewsData(orgId, 1, pageMessSize);
            } else {
                refreshLayout.setVisibility(View.GONE);
                mFootBarView.hideFooter();
                swipe_mess_layout.setVisibility(View.GONE);
                toast("数据错误");
            }
        } else {
            if (currentPage == 0) {
                //通知没网时把footer去掉
                mFootBarView.hideFooter();
                if (nAdapter.getCount() == 0) {
                    refreshLayout.setVisibility(View.GONE);
//                    no_noti.setVisibility(View.VISIBLE);
                }
            } else if (currentPage == 1) {
                //消息没网时把footer去掉
                mFootBarView.hideFooter();
                if (mAdapter.getCount() == 0) {
                    swipe_mess_layout.setVisibility(View.GONE);
//                    no_mess.setVisibility(View.VISIBLE);
                }
            }
            toast("网络未连接或不可用");
        }
        ;
    }

    private void changeReadStatus(int noticeId) {
        new UserDataHelper(this).changeReadStatus(getNetRequestHelper(this).isShowProgressDialog(false), noticeId);
    }

//    private void changeMessReadStatus(String messId) {
//        new UserDataHelper(this).changeMessReadStatus(getNetRequestHelper(this).isShowProgressDialog(false), messId);
//    }

    private void requestNoticeData(int orgId, int pageNo, int pageSize) {
        new UserDataHelper(this).getNoticeInfor(getNetRequestHelper(this).isShowProgressDialog(false), orgId, pageNo, pageSize);
    }

    private void removeItemNotice(int noticeId) {
        new UserDataHelper(this).removeItemNoti(getNetRequestHelper(this).isShowProgressDialog(true), noticeId);
    }

    private void clearNotice() {
        new UserDataHelper(this).clearNoti(getNetRequestHelper(this).isShowProgressDialog(true));
    }

    private void requestNewsData(int orgId, int pageNo, int pageSize) {
        new UserDataHelper(this).getNewsInfor(getNetRequestHelper(this).isShowProgressDialog(false), orgId, pageNo, pageSize);
    }

//    private void removeItemMess(String messageId) {
//        new UserDataHelper(this).removeItemMess(getNetRequestHelper(this).isShowProgressDialog(true), messageId);
//    }
//
//    private void clearMess() {
//        new UserDataHelper(this).clearMess(getNetRequestHelper(this).isShowProgressDialog(true));
//    }

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
                isLoading = false;
                isMessLoading = false;
                if (requestTag.equals(Constant.ShouYeUrl.NOTICE)) {// 通知记录
                    SharedPreferencesUtil.saveStringData(NoticeAndMessageActivity.this,
                            Constant.ShouYeUrl.NOTICE, responseInfo.result);
//                    MyLog.e("MMM", "Constant.ShouYeUrl.NOTICE.result is " + responseInfo.result);
                    //停止刷新
                    if (refreshLayout != null) {
                        refreshLayout.setRefreshing(false);
                    }
                    mFootBarView.hideFooter();
                    parseNoticeRecord(responseInfo.result, TYPE.NET);
                } else if (requestTag.equals(Constant.ShouYeUrl.NEWS)) {//新闻列表
//                    MyLog.e("MMM", "Constant.ShouYeUrl.NEWS.result is " + responseInfo.result);
                    SharedPreferencesUtil.saveStringData(NoticeAndMessageActivity.this,
                            Constant.ShouYeUrl.NEWS, responseInfo.result);
                    //停止刷新
                    if (swipe_mess_layout != null) {
                        swipe_mess_layout.setRefreshing(false);
                    }
                    mFootBarView.hideFooter();
                    parseNews(responseInfo.result, TYPE.NET);
                } else if (requestTag.equals(Constant.ShouYeUrl.READ_STATUS)) {
//                    MyLog.e("MMM", "READ_STATUS responseInfo.result is " + responseInfo.result);
                    //改变通知读取状态
                } else if (requestTag.equals(Constant.ShouYeUrl.READ_MESS_STATUS)) {
                    //改变消息读取状态
                } else if (requestTag.equals(Constant.ShouYeUrl.REMOVE_ITEM_NOTICE)) {
                    //删除单个通知
                    BaseDataBean<Object> baseDataBean = JsonUtil.parseDataObject(responseInfo.result, Object.class);
                    if (baseDataBean.code == 100) {
                        if (delete_type == 0) {
                            //通知删除
                            if (nAdapter != null && nAdapter.nList != null && nAdapter.nList.size() > delePos) {
                                if (nAdapter.nList.get(delePos).isRead == 0) {
                                    nAdapter.nList.get(delePos).isRead = 1;
                                    //未读通知总数减一
                                    notNoticeRead--;
                                    if (notNoticeRead == 0) {
                                        //总的未读通知为0时隐藏红点
                                        red_noti_icon.setVisibility(View.GONE);
                                    }
                                }
                                nAdapter.nList.remove(delePos);
                                nAdapter.notifyDataSetChanged();
                            }
                        } else {
                            //新闻删除
                            if (mAdapter != null && mAdapter.mList != null && mAdapter.mList.size() > delePos) {
                                if (mAdapter.mList.get(delePos).isRead == 0) {
                                    mAdapter.mList.get(delePos).isRead = 1;
                                    //未读通知总数减一
                                    notMessRead--;
//                                    if (notMessRead == 0) {
//                                        //总的未读通知为0时隐藏红点
//                                        red_mess_icon.setVisibility(View.GONE);
//                                    }
                                }
                                mAdapter.mList.remove(delePos);
                                mAdapter.notifyDataSetChanged();
                            }
                        }
                    } else {
                        toast(baseDataBean.msg);
                    }
                } else if (requestTag.equals(Constant.ShouYeUrl.CLEAR_NOTICE)) {
                    //清空通知
                    BaseDataBean<Object> baseDataBean = JsonUtil.parseDataObject(responseInfo.result, Object.class);
                    if (baseDataBean.code == 100) {
                        notNoticeRead = 0;
                        red_noti_icon.setVisibility(View.GONE);
                        if (nAdapter != null && nAdapter.nList != null && nAdapter.nList.size() > 0) {
                            nAdapter.nList.clear();
                            nAdapter.notifyDataSetChanged();
//                            //删除通知后没有数据了
//                            if (nAdapter.getCount() == 0) {
//                                no_noti.setVisibility(View.VISIBLE);
//                            }
                        }
                    } else {
                        toast(baseDataBean.msg);
                    }
                } else if (requestTag.equals(Constant.ShouYeUrl.CLEAR_MESSAGE)) {
                    //清空消息
                    BaseDataBean<Object> baseDataBean = JsonUtil.parseDataObject(responseInfo.result, Object.class);
                    if (baseDataBean.code == 100) {
                        notMessRead = 0;
//                        red_mess_icon.setVisibility(View.GONE);
                        if (mAdapter != null && mAdapter.mList != null && mAdapter.mList.size() > 0) {
                            mAdapter.mList.clear();
                            mAdapter.notifyDataSetChanged();
                        }
                    } else {
                        toast(baseDataBean.msg);
                    }
                } else if (requestTag.equals(Constant.ShouYeUrl.MESSAGE_READED)) {
                    //清空消息
                    BaseDataBean<Object> baseDataBean = JsonUtil.parseDataObject(responseInfo.result, Object.class);
                    if (baseDataBean.code == 100) {
                        toast(baseDataBean.msg);
                        pageMessNum = 1;
                        notMessRead = 0;
                        requestNewsData(orgId, 1, pageMessSize);
                    }
                }
            }

            @Override
            public void onFailure(HttpException error, String msg, String requestTag) {
                if (requestTag.equals(Constant.ShouYeUrl.NOTICE)) {
                    if (nAdapter != null && nAdapter.getCount() == 0) {
//                        no_noti.setVisibility(View.VISIBLE);
                        mFootBarView.hideFooter();
                    }
                } else if (requestTag.equals(Constant.ShouYeUrl.NEWS)) {
//                    if (mAdapter != null && mAdapter.getCount() == 0) {
//                        no_mess.setVisibility(View.VISIBLE);
//                    }
                    mFootBarView.hideFooter();
                }
                NetWorkStateUtils.errorNetMes(NoticeAndMessageActivity.this);
            }
        };
    }

    private boolean canLoad = true;

    private void parseNoticeRecord(String result, TYPE type) {
        try {
            BaseDataBean<NoticeBean> json = JsonUtil.parseDataObject(result, NoticeBean.class);
            if (json.code == 100) {
                if (json.data != null && json.data.noticeList.size() > 0) {
//                    no_noti.setVisibility(View.GONE);
                    //只有一页时重新加载 否则是加载更多
                    if (json.data.pageNo == 1) {
                        nAdapter.clear();
                    }
                    if (json.data.pageNo * json.data.pageSize >= json.data.totalCount) {
                        //总共加载的数目大于实际的总数目就不让加载了
                        canLoad = false;
                        mFootBarView.hideFooter();
                    } else {
                        canLoad = true;
                    }
                    ArrayList<NoticeBean.Notice> list = (ArrayList) json.data.noticeList;
                    nAdapter.add(list);
                    for (int i = 0; i < list.size(); i++) {
                        if (list.get(i).isRead == 0) {
                            //未读且为网路请求回来的数据
                            if (type.equals(TYPE.NET)) {
                                notNoticeRead++;
                            }
                            red_noti_icon.setVisibility(View.VISIBLE);
                        }
                    }
                    if (notNoticeRead == 0) {
                        red_noti_icon.setVisibility(View.GONE);
                    }
                    if (nAdapter == null) {
                        nAdapter = new NoticeAdapter(this);
                        lv_notice.setAdapter(nAdapter);
                    } else {
                        nAdapter.notifyDataSetChanged();
                    }
                } else {
                    if (nAdapter != null && json.data != null && json.data.pageNo == 0) {
                        //为了清空切换账号时  上次账号中保存的缓存数据
//                        no_noti.setVisibility(View.VISIBLE);
                        red_noti_icon.setVisibility(View.GONE);
                        nAdapter.clear();
                    }
                }
            }
//            else {
//                no_noti.setVisibility(View.VISIBLE);
//            }
        } catch (Exception e) {
//            MyLog.e("KKK", "e is " + e);
        }
    }

    private boolean canMessLoad = true;
    private boolean isShowEmptyMess = false;

    //解析新闻
    private void parseNews(String result, TYPE type) {
        try {
            BaseDataBean<NewsBean> json = JsonUtil.parseDataObject(result, NewsBean.class);
            if (json.code == 100) {
                if (json.data != null && json.data.noticeList.size() > 0) {
//                    no_mess.setVisibility(View.GONE);
                    //只有一页时重新加载 否则是加载更多
                    if (json.data.pageNo == 1) {
                        mAdapter.clear();
                    }
                    if (json.data.pageNo * json.data.pageSize >= json.data.totalCount) {
                        //总共加载的数目大于实际的总数目就不让加载了
                        canMessLoad = false;
                        mFootBarView.hideFooter();
                    } else {
                        canMessLoad = true;
                    }
                    ArrayList<NewsBean.News> mmList = (ArrayList) json.data.noticeList;
                    for (int i = 0; i < mmList.size(); i++) {
                        mAdapter.add(mmList.get(i));
                        if (mmList.get(i).isRead == 0) {
                            //未读且为网路请求回来的数据
                            if (type.equals(TYPE.NET)) {
                                notMessRead++;
                            }
//                            red_mess_icon.setVisibility(View.VISIBLE);
                        }
                    }
//                    if (notMessRead == 0) {
//                        red_mess_icon.setVisibility(View.GONE);
//                    }
                    if (mAdapter.getCount() > 0) {
                        if (mAdapter == null) {
                            mAdapter = new MessageAdapter(this);
                            lv_message.setAdapter(mAdapter);
                        } else {
                            mAdapter.notifyDataSetChanged();
                        }
                    }
//                    else {
//                        no_mess.setVisibility(View.VISIBLE);
//                    }
                } else {
                    if (mAdapter != null && json.data != null && json.data.pageNo == 1) {
                        //为了清空切换账号时 上次账号保存的数据
//                        red_mess_icon.setVisibility(View.GONE);
                        mAdapter.clear();
//                        if (isShowEmptyMess) {
//                            no_mess.setVisibility(View.VISIBLE);
//                        }
                    }
                }
            }
//            else {
//                if (isShowEmptyMess) {
//                    no_mess.setVisibility(View.VISIBLE);
//                }
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        saveData();
        super.onBackPressed();
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
