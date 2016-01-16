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
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
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
import com.henanjianye.soon.communityo2o.common.enties.CommodityBean;
import com.henanjianye.soon.communityo2o.common.enties.JsonUtil;
import com.henanjianye.soon.communityo2o.common.enties.MessageBean;
import com.henanjianye.soon.communityo2o.common.util.CommonUtils;
import com.henanjianye.soon.communityo2o.common.util.FootBarHelper;
import com.henanjianye.soon.communityo2o.common.util.MyTitleBarHelper;
import com.henanjianye.soon.communityo2o.common.util.NetWorkStateUtils;
import com.henanjianye.soon.communityo2o.common.util.SharedPreferencesUtil;
import com.henanjianye.soon.communityo2o.common.util.UserSharedPreferencesUtil;
import com.henanjianye.soon.communityo2o.eventFunction.activity.EventCheckAllReplay;
import com.henanjianye.soon.communityo2o.eventFunction.activity.EventCommentActivity;
import com.henanjianye.soon.communityo2o.eventFunction.activity.applayActivityPersonList;
import com.henanjianye.soon.communityo2o.interf.NetWorkCallback;

import java.util.ArrayList;

public class MessageActivity extends BaseActivity {
    private SwipeRefreshLayout swipe_mess_layout;
    private ListView lv_message;
    private MessageAdapter mAdapter;
    private MyTitleBarHelper myTitleBarHelper;
    private TextView no_mess;
    private int orgId;
    private int pageMessNum = 1;//页数
    private int pageMessSize = 20;//一页的数据条数
    private int delePos = -1;//要删除那条数据的位置
    private FootBarHelper mFootBarView = null;
    private View footBar;
    private boolean isShowDialog = true;//请求网络时是否有对话框
    private boolean hasInsert = false;
    private boolean canMessLoad = true;//是否可以加载更多数据

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        initViews();
        initEvents();
        init();
        getData();
    }


    public enum TYPE {CACHE, NET}

    private void initViews() {
        swipe_mess_layout = (SwipeRefreshLayout) findViewById(R.id.swipe_mess_layout);
        swipe_mess_layout.setColorSchemeResources(R.color.repair_line_color);
        myTitleBarHelper = new MyTitleBarHelper(this, getWindow().getDecorView().findViewById(android.R.id.content));
        myTitleBarHelper.setLeftImgVisible(false);
        myTitleBarHelper.resetState();
        myTitleBarHelper.setMiddleText("消息");
        myTitleBarHelper.setRightImgVisible(false);
        myTitleBarHelper.setLeftImag(R.mipmap.btn_back);
        myTitleBarHelper.setRightText("全部已读");
        no_mess = (TextView) findViewById(R.id.no_mess);
        lv_message = (ListView) findViewById(R.id.lv_message);
        lv_message.setEmptyView(no_mess);
        orgId = UserSharedPreferencesUtil.getUserInfo(this).orgId;
        footBar = LayoutInflater.from(this).inflate(R.layout.item_progressbar, null);
        lv_message.addFooterView(footBar);
        mFootBarView = new FootBarHelper(footBar, this);
        mFootBarView.hideFooter();
    }

    private void initEvents() {
        myTitleBarHelper.setOnclickListener(this);
        swipe_mess_layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                if (CommonUtils.isNetworkConnected(MessageActivity.this)) {
                    pageMessNum = 1;
                    isShowDialog = false;
                    requestMessData(1, pageMessSize);
                } else {
                    swipe_mess_layout.setRefreshing(false);
                    toast("网络未连接或不可用");
                }

            }
        });

        lv_message.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                switch (scrollState) {
                    case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                        if (canMessLoad && lv_message.getCount() > 0
                                && lv_message.getLastVisiblePosition() == (lv_message
                                .getCount() - 1)) {
                            //上拉刷新停止
                            if (mAdapter == null || swipe_mess_layout.isRefreshing())
                                return;
                            if (CommonUtils.isNetworkConnected(MessageActivity.this)) {
                                isShowDialog = false;
                                //请求下一页数据
                                pageMessNum++;
                                mFootBarView.showFooter();
                                requestMessData(pageMessNum, pageMessSize);
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
        //通知列表
        mAdapter = new MessageAdapter(this);
        lv_message.setAdapter(mAdapter);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.iv_title_bar_left:
                setBack();
                this.finish();
                break;
            case R.id.tv_title_bar_right:
                changeMessAllRead();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        setBack();
        super.onBackPressed();
    }

    private void setBack() {
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
    }

    //消息
    class MessageAdapter extends BaseAdapter {
        private LayoutInflater mLayoutInflater;
        MessageBean.Message message;
        ArrayList<MessageBean.Message> mList = new ArrayList<>();

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
            message = (MessageBean.Message) getItem(position);
            final MessageHolder mHolder;
            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.item_message, parent, false);
                mHolder = new MessageHolder();
                mHolder.tv_mess_Content = (TextView) convertView.findViewById(R.id.tv_mess_content);
                mHolder.tv_mess_Time = (TextView) convertView.findViewById(R.id.tv_mess_time);
                mHolder.iv_mess_Icon = (ImageView) convertView.findViewById(R.id.iv_mess_icon);
                mHolder.card_view = (RelativeLayout) convertView.findViewById(R.id.card_view);
                convertView.setTag(mHolder);
            } else {
                mHolder = (MessageHolder) convertView.getTag();
            }
            mHolder.tv_mess_Content.setTextColor(getResources().getColor(R.color.word_normal_color));
            if (message != null) {
                //0表示未读 内容颜色深  1表示已读 内容颜色浅
                if (message.isRead == 0) {
                    mHolder.tv_mess_Content.setTextColor(getResources().getColor(R.color.word_normal_color));
                } else {
                    mHolder.tv_mess_Content.setTextColor(getResources().getColor(R.color.light_grey_color));
                }

                //根据消息类型显示右边图片类型
                if (message.messageType / 100 == 12) {
                    //客服
                    mHolder.iv_mess_Icon.setImageResource(R.mipmap.news_customerserviceicon_normal);
                } else if (message.messageType == 1301 || message.messageType == 1302) {
                    //维修
                    mHolder.iv_mess_Icon.setImageResource(R.mipmap.news_repairicon_normal);
                } else if (message.messageType / 100 == 11) {
                    //系统
                    mHolder.iv_mess_Icon.setImageResource(R.mipmap.news_systemicon_normal);
                } else if (message.messageType == 1601 || message.messageType == 1602 || message.messageType == 1603) {
                    //订单
                    mHolder.iv_mess_Icon.setImageResource(R.mipmap.news_ordericon_normal);
                } else if (message.messageType / 100 == 14) {
                    //快递
                    mHolder.iv_mess_Icon.setImageResource(R.mipmap.news_expressicon_normal);
                } else if (message.messageType == 1701 || message.messageType == 1702 || message.messageType == 1703) {
                    //活动
                    mHolder.iv_mess_Icon.setImageResource(R.mipmap.activity_icon_nromal);
                } else if (message.messageType == 1501 || message.messageType == 1502) {
                    //商品
                    mHolder.iv_mess_Icon.setImageResource(R.mipmap.product_icon_normal);
                }
                mHolder.tv_mess_Content.setText(message.messageTitle);
                mHolder.tv_mess_Time.setText(message.messageTimeShow);
                mHolder.card_view.setTag(position);
                mHolder.card_view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Object object = v.getTag();
                        if (object != null) {
                            int pos = Integer.parseInt(object.toString());
                            MessageBean.Message message = (MessageBean.Message) getItem(pos);
                            if (CommonUtils.isNetworkConnected(MessageActivity.this)) {
                                if (message != null) {
                                    mHolder.tv_mess_Content.setTextColor(getResources().getColor(R.color.light_grey_color));
                                    if (message.isRead == 0) {
                                        //判断当前是否是未读信息 如果是未读消息 请求改变为已读状态
                                        message.isRead = 1;
                                        changeMessReadStatus(message.messageId);
                                    }
                                    //根据消息类型跳转不同页面
                                    if (message.messageType / 100 == 12) {
                                        //客服
//                                        toast("客服Item被点击");
                                    } else if (message.messageType == 1301 || message.messageType == 1302) {
                                        //维修详情
                                        switch (message.messageType) {
                                            case 1301:
                                                //维修处理中
                                                Intent intent = new Intent(MessageActivity.this, ServiceRepairDetailActivity.class);
                                                intent.putExtra(ServiceRepairRecordActivity.RECORD_INFO, message.extra.rid);
                                                startActivityForResult(intent, 1);
                                                break;
                                            case 1302:
                                                //维修已经完成
                                                Intent intent1 = new Intent(MessageActivity.this, ServiceRepairDetailActivity.class);
                                                intent1.putExtra(ServiceRepairRecordActivity.RECORD_INFO, message.extra.rid);
                                                startActivity(intent1);
                                                break;
                                            default:
                                                break;
                                        }
                                    } else if (message.messageType / 100 == 11) {
                                        //系统
//                                        toast("系统Item被点击");
                                    } else if (message.messageType == 1601 || message.messageType == 1602 || message.messageType == 1603) {
                                        //订单 1601普通订单支付超时 1602 抢购订单支付超时  1603 订单发货
                                        if (message.extra.orderId != null && !message.extra.orderId.equals("")) {
                                            Intent orderIntent = new Intent(MessageActivity.this, OrderDetailActivity.class);
                                            orderIntent.putExtra(CommodityOrderListActivity.ORDERID, Long.parseLong(message.extra.orderId));
                                            startActivity(orderIntent);
                                        }
                                    } else if (message.messageType / 100 == 14) {
                                        //快递 认证了
                                        Intent expressIntent = new Intent(MessageActivity.this, ExpressMainListActivity.class);
                                        expressIntent.putExtra(NoticeAndMessageActivity.EXPRESS_NUM, message.extra.expressNum);
                                        startActivity(expressIntent);
//                                         CommonUtils.startWebViewActivity(MessageActivity.this, "快递", SharedPreferencesUtil.getStringData(MessageActivity.this, SharedPreferencesUtil.APP_URL_PATHEXPRESS, null));
                                    } else if (message.messageType == 1701 || message.messageType == 1702 || message.messageType == 1703) {
                                        //活动
                                        switch (message.messageType) {
                                            case 1701:
                                                //活动报名
                                                if (message.extra.activityId != -1) {
                                                    Intent intent = new Intent(MessageActivity.this, applayActivityPersonList.class);
                                                    intent.putExtra("activityId", message.extra.activityId);
                                                    intent.putExtra("title_top", message.extra.activity_title_top);
                                                    startActivity(intent);
                                                }
                                                break;
                                            case 1702:
                                                //活动回复(自己发布的别人评论)
                                                Intent intet1 = new Intent(MessageActivity.this, EventCommentActivity.class);
                                                intet1.putExtra("activityId", message.extra.activityId);
                                                startActivity(intet1);
                                                break;
                                            case 1703:
                                                //评论回复（自己评论的别人回复）
                                                Intent intet2 = new Intent(MessageActivity.this, EventCheckAllReplay.class);
                                                intet2.putExtra("onlineFlag", false);
                                                intet2.putExtra("commentId", message.extra.commentId);
                                                startActivity(intet2);
                                                break;
                                            default:
                                                break;
                                        }
                                    } else if (message.messageType == 1501) {
                                        // 商品-每周优选
                                        if (message.extra != null && message.extra.goodsId != null && !message.extra.goodsId.equals("")) {
                                            CommodityBean commodityBean = new CommodityBean();
                                            commodityBean.url = message.extra.goodsUrl;
                                            commodityBean.currentPriceShow = message.extra.currentPriceShow;
                                            //假数据 服务器返回的数据有问题
                                            //                                    commodityBean.goodsId = 98526;
                                            commodityBean.goodsId = Integer.parseInt(message.extra.goodsId);
                                            commodityBean.name = message.messageTitle;
                                            Intent intent = new Intent(MessageActivity.this, CommodityDetailInforActivity.class);
                                            intent.putExtra("CommodityBean", commodityBean);
                                            startActivity(intent);
                                        }
                                    } else if (message.messageType == 1502) {
                                        //商品-限时抢购 详情界面
                                        if (message.extra != null && message.extra.goodsId != null && !message.extra.goodsId.equals("")) {
                                            CommodityBean commodityBean = new CommodityBean();
                                            commodityBean.url = message.extra.goodsUrl;
                                            commodityBean.currentPriceShow = message.extra.currentPriceShow;
                                            commodityBean.name = message.messageTitle;
                                            commodityBean.goodsId = Integer.parseInt(message.extra.goodsId);
                                            Intent intent = new Intent(MessageActivity.this, CommodityDetailInforActivity.class);
                                            intent.putExtra("CommodityBean", commodityBean);
                                            startActivity(intent);
                                        }
                                    }
                                }
                            } else {
                                toast("网络未连接或不可用");
                            }
                        }
                    }
                });
                mHolder.card_view.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        Object object = v.getTag();
                        if (object != null) {
                            int pos = Integer.parseInt(object.toString());
                            //要删除的当前的位置
                            delePos = pos;
                            MessageBean.Message message = (MessageBean.Message) getItem(pos);
                            createDialog(D_TYPE.ITEM_MESS, 0, message.messageId);
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
            ImageView iv_mess_Icon;
            RelativeLayout card_view;
        }

        //        public void add(ArrayList<MessageBean> list) {
//            mList.addAll(list);
//            notifyDataSetChanged();
//        }
        public void add(MessageBean.Message bean) {
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

    private void getData() {
        //消息数据缓存
        String messageString = SharedPreferencesUtil.getStringData(this,
                Constant.ShouYeUrl.MESSAGE, null);
        // 消息JSON解析
        if (!TextUtils.isEmpty(messageString)) {// 解析数据
            parseMessageRecord(messageString, TYPE.CACHE);
        }
        if (CommonUtils.isNetworkConnected(this)) {
            if (orgId != -1) {
                //消息网络请求
                requestMessData(1, pageMessSize);
            } else {
                toast("数据错误");
            }
        }
    }

    private void changeMessAllRead() {
        new UserDataHelper(this).getMessageAllRead(getNetRequestHelper(this).isShowProgressDialog(true));
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
                if (requestTag.equals(Constant.ShouYeUrl.MESSAGE)) {// 消息记录
                    SharedPreferencesUtil.saveStringData(MessageActivity.this,
                            Constant.ShouYeUrl.MESSAGE, responseInfo.result);
                    //停止刷新
                    if (swipe_mess_layout != null) {
                        swipe_mess_layout.setRefreshing(false);
                    }
                    mFootBarView.hideFooter();
                    parseMessageRecord(responseInfo.result, TYPE.NET);
                } else if (requestTag.equals(Constant.ShouYeUrl.READ_MESS_STATUS)) {
                    //改变消息读取状态
                } else if (requestTag.equals(Constant.ShouYeUrl.REMOVE_ITEM_MESSAGE)) {
                    //删除单个消息
                    BaseDataBean<Object> baseDataBean = JsonUtil.parseDataObject(responseInfo.result, Object.class);
                    if (baseDataBean.code == 100) {
                        if (mAdapter != null && mAdapter.mList != null && mAdapter.mList.size() > delePos) {
                            if (mAdapter.mList.get(delePos).isRead == 0) {
                                mAdapter.mList.get(delePos).isRead = 1;
                            }
                            mAdapter.mList.remove(delePos);
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
                        requestMessData(1, pageMessSize);
                    }
                }
            }

            @Override
            public void onFailure(HttpException error, String msg, String requestTag) {
                if (requestTag.equals(Constant.ShouYeUrl.MESSAGE)) {
                    if (swipe_mess_layout != null) {
                        swipe_mess_layout.setRefreshing(false);
                    }
                }
                mFootBarView.hideFooter();
                NetWorkStateUtils.errorNetMes(MessageActivity.this);
            }
        };
    }

    private void requestMessData(int pageNo, int pageSize) {
        new UserDataHelper(this).getMessageInfor(getNetRequestHelper(this).isShowProgressDialog(isShowDialog), pageNo, pageSize);
    }

    private enum D_TYPE {ITEM_MESS, MESS}

    private void createDialog(final D_TYPE type, final int deleId, final String messId) {
        //布局文件转换为view对象
        LayoutInflater inflaterDl = LayoutInflater.from(this);
        LinearLayout layout = (LinearLayout) inflaterDl.inflate(R.layout.delete_noti_and_aess, null);
        //对话框
        final Dialog dialog = new AlertDialog.Builder(MessageActivity.this).create();
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
                if (type.equals(D_TYPE.ITEM_MESS)) {
                    //删除一条消息
                    if (CommonUtils.isNetworkConnected(MessageActivity.this)) {
                        removeItemMess(messId);
                    } else {
                        toast("网络未连接或不可用");
                    }
                } else if (type.equals(D_TYPE.MESS)) {
                    //清空消息
                    if (CommonUtils.isNetworkConnected(MessageActivity.this)) {
                        clearMess();
                    } else {
                        toast("网络未连接或不可用");
                    }
                }
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

    private void removeItemMess(String messageId) {
        new UserDataHelper(this).removeItemMess(getNetRequestHelper(this).isShowProgressDialog(true), messageId);
    }

    private void clearMess() {
        new UserDataHelper(this).clearMess(getNetRequestHelper(this).isShowProgressDialog(true));
    }

    private void changeMessReadStatus(String messId) {
        new UserDataHelper(this).changeMessReadStatus(getNetRequestHelper(this).isShowProgressDialog(false), messId);
    }

    //解析消息
    private void parseMessageRecord(String result, TYPE type) {
        try {
            BaseDataBean<MessageBean> json = JsonUtil.parseDataObject(result, MessageBean.class);
            if (json.code == 100) {
                if (json.data != null && json.data.list.size() > 0) {
                    //只有一页时重新加载 否则是加载更多
                    if (json.data.pageNo == 1) {
                        mAdapter.clear();
                    }
                    if (json.data.pageNo * json.data.pageSize >= json.data.totalCount) {
                        //总共加载的数目大于实际的总数目就不让加载了
                        canMessLoad = false;
                    } else {
                        canMessLoad = true;
                    }
                    ArrayList<MessageBean.Message> mmList = (ArrayList) json.data.list;
                    for (int i = 0; i < mmList.size(); i++) {
                        //客服消息只加入最新一条 12XX是客服消息
                        if (mmList.get(i).messageType / 100 == 12) {
                            //循环发现客服最新一条消息加到List中  以后就不加了
                            if (!hasInsert) {
                                mAdapter.add(mmList.get(i));
                                hasInsert = true;
                            }
                        } else {
                            mAdapter.add(mmList.get(i));
                        }
                    }
                    if (mAdapter == null) {
                        mAdapter = new MessageAdapter(this);
                        lv_message.setAdapter(mAdapter);
                    } else {
                        mAdapter.notifyDataSetChanged();
                    }
                } else {
                    if (mAdapter != null && json.data != null && json.data.pageNo == 1) {
                        //为了清空切换账号时 上次账号保存的数据
                        mAdapter.clear();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
