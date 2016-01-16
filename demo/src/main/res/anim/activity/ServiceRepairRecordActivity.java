package anim.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.asyey.footballlibrary.network.exception.HttpException;
import com.asyey.footballlibrary.network.http.ResponseInfo;
import com.asyey.footballlibrary.universalimageloader.core.DisplayImageOptions;
import com.asyey.footballlibrary.universalimageloader.core.ImageLoader;
import com.henanjianye.soon.communityo2o.BaseActivity;
import com.henanjianye.soon.communityo2o.R;
import com.henanjianye.soon.communityo2o.common.Constant;
import com.henanjianye.soon.communityo2o.common.MyLog;
import com.henanjianye.soon.communityo2o.common.UserDataHelper;
import com.henanjianye.soon.communityo2o.common.enties.BaseBean;
import com.henanjianye.soon.communityo2o.common.enties.JsonUtil;
import com.henanjianye.soon.communityo2o.common.enties.RepairBean;
import com.henanjianye.soon.communityo2o.common.util.CommonUtils;
import com.henanjianye.soon.communityo2o.common.util.MyTitleBarHelper;
import com.henanjianye.soon.communityo2o.common.util.NetWorkStateUtils;
import com.henanjianye.soon.communityo2o.common.util.SharedPreferencesUtil;
import com.henanjianye.soon.communityo2o.common.util.UserSharedPreferencesUtil;
import com.henanjianye.soon.communityo2o.common.view.MySeekBar;
import com.henanjianye.soon.communityo2o.interf.NetWorkCallback;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

public class ServiceRepairRecordActivity extends BaseActivity {
    private ListView record_listView;
    private MyTitleBarHelper myTitleBarHelper;
    private RecordAdapter mAdapter;
    private SwipeRefreshLayout swipe_repair_layout;
    private TextView no_record;
    public  static  final  int TOCOMMENT=11;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_repair_record);
        initViews();
        initEvents();
        getData();
        init();
    }

    private void initViews() {
        myTitleBarHelper = new MyTitleBarHelper(this, getWindow().getDecorView().findViewById(android.R.id.content));
        myTitleBarHelper.setLeftImgVisible(false);
        myTitleBarHelper.resetState();
        myTitleBarHelper.setMiddleText("报修记录");
        myTitleBarHelper.setRightImgVisible(false);
        myTitleBarHelper.setRightTxVisible(false);
//        myTitleBarHelper.setRightText("我要报修");
        myTitleBarHelper.setLeftImag(R.mipmap.btn_back);
        swipe_repair_layout = (SwipeRefreshLayout) findViewById(R.id.swipe_repair_layout);
        swipe_repair_layout.setColorSchemeResources(R.color.repair_line_color);
        record_listView = (ListView) findViewById(R.id.record_listview);
        orgId = new UserSharedPreferencesUtil().getUserInfo(this).orgId;
        no_record = (TextView) findViewById(R.id.no_record);
    }

    private void initEvents() {
        myTitleBarHelper.setOnclickListener(this);
        swipe_repair_layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                no_record.setVisibility(View.GONE);
                requestData(orgId);
            }
        });
    }

    private int orgId;

    private void getData() {
        //1. 保修记录缓存
        String repairString = SharedPreferencesUtil.getStringData(this,
                Constant.Repair.REPAIR_RECORD, null);
        // 保修记录JSON解析
        if (!TextUtils.isEmpty(repairString)) {// 解析数据
            parseRepairRecord(repairString);
        }
        if (CommonUtils.isNetworkConnected(this)) {
            if (orgId != -1) {
                // 报修记录网络请求
                requestData(orgId);
            } else {
                toast("数据错误");
            }
        } else {
            toast("网络未连接或不可用");
        }

    }

    private void requestData(int orgId) {
        new UserDataHelper(this).getRepairRecordInfor(getNetRequestHelper(this).isShowProgressDialog(false), orgId);
    }


    private void parseRepairRecord(String result) {
        try {
            BaseBean<RepairBean> json = JsonUtil.jsonArray(result, RepairBean.class);
            if (json.code == 100) {
                if (json.data.size() > 0) {
                    no_record.setVisibility(View.GONE);
                    if (recordBeanList != null && recordBeanList.size() > 0) {
                        //清空数据
                        recordBeanList.clear();
                        if (mAdapter != null) {
                            mAdapter.notifyDataSetChanged();
                        }
                    }
                    recordBeanList = json.data;
                    if (recordBeanList.size() > 0) {
                        if (mAdapter == null) {
                            mAdapter = new RecordAdapter(this);
                            record_listView.setAdapter(mAdapter);
                        } else {
                            mAdapter.notifyDataSetChanged();
                        }
                        //停止刷新
                        if (swipe_repair_layout != null) {
                            swipe_repair_layout.setRefreshing(false);
                        }
                    }
                } else {
                    no_record.setVisibility(View.VISIBLE);
                    //请求成功但没有数据  停止刷新
                    if (swipe_repair_layout != null) {
                        swipe_repair_layout.setRefreshing(false);
                    }
                    if (mAdapter != null && recordBeanList != null && recordBeanList.size() > 0) {
                        //这里是为了防止用户切换账号时的数据不统一
                        recordBeanList.clear();
                        mAdapter.notifyDataSetChanged();
                    }
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
            MyLog.e("FFF", "e is " + e);
        }
    }


    public static String RECORD_INFO = "record_info";

    private void init() {
        mAdapter = new RecordAdapter(this);
        record_listView.setAdapter(mAdapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case 1:
                //取消报修
                if (CommonUtils.isNetworkConnected(this)) {
                    if (orgId != -1) {
                        requestData(orgId);
                    } else {
                        toast("数据错误");
                    }
                } else {
                    toast("网络未连接或不可用");
                }
                break;
            case 2:
                // 报修记录网络请求 添加了一个报修
                if (CommonUtils.isNetworkConnected(this)) {
                    if (orgId != -1) {
                        requestData(orgId);
                    } else {
                        toast("数据错误");
                    }
                } else {
                    toast("网络未连接或不可用");
                }
                break;
            case TOCOMMENT:
                if(data!=null){
                  int idcode= data.getIntExtra("idcode",-1);
                    if(idcode!=-1){
                        RepairBean repairBean = recordBeanList.get(idcode);
                        repairBean.state="06";
                        recordBeanList.remove(idcode);
                        recordBeanList.add(idcode,repairBean);
                        mAdapter.notifyDataSetChanged();

                    }
                }
                break;
        }
//        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.iv_title_bar_left:
                this.finish();
                break;
            case R.id.tv_title_bar_right:
                if (CommonUtils.isNetworkConnected(ServiceRepairRecordActivity.this)) {
                    Intent intent = new Intent(ServiceRepairRecordActivity.this, ServiceRepairActivity.class);
                    startActivityForResult(intent, 2);
                } else {
                    toast("网络未连接或不可用");
                }
                break;
            default:
                break;
        }
    }

    List<RepairBean> recordBeanList = new ArrayList<>();

    class RecordAdapter extends BaseAdapter {
        Context mContext;
        LayoutInflater mLInflater;
        ImageLoader instance = ImageLoader.getInstance();
        DisplayImageOptions build = new DisplayImageOptions.Builder()
                .showImageOnFail(R.mipmap.repair_default)
                .showImageForEmptyUri(R.mipmap.repair_default).showImageOnLoading(R.mipmap.repair_default).build();

        public RecordAdapter(Context context) {
            this.mContext = context;
            this.mLInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return recordBeanList.size();
        }

        @Override
        public Object getItem(int position) {
            if (recordBeanList != null && recordBeanList.size() != 0) {
                return recordBeanList.get(position);
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final RepairBean bean = (RepairBean) getItem(position);
//            MyLog.e("FFF", "repair bean.toString() is " + bean.toString());
            RecordHolder holder;
            if (convertView == null) {
                convertView = mLInflater.inflate(R.layout.item_repair_record, parent, false);
                holder = new RecordHolder();
                /**得到各个控件的对象*/
                holder.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
                holder.tv_type = (TextView) convertView.findViewById(R.id.record_type);
                holder.tv_date = (TextView) convertView.findViewById(R.id.record_date);
                holder.tv_intro = (TextView) convertView.findViewById(R.id.tv_intro);
                holder.iv_img = (ImageView) convertView.findViewById(R.id.record_img);
                holder.custom_seekbar = (MySeekBar) convertView.findViewById(R.id.custom_seekbar);
                holder.ll_record = (LinearLayout) convertView.findViewById(R.id.ll_record);
                holder.tv_canceled = (TextView) convertView.findViewById(R.id.tv_canceled);
                holder.view_line = convertView.findViewById(R.id.view_line);
                holder.tv_fix_over = (TextView) convertView.findViewById(R.id.tv_fix_over);
                holder.record_address = (TextView) convertView.findViewById(R.id.record_address);
                holder.ratingbar= (RatingBar) convertView.findViewById(R.id.ratingbar);
                holder.tv_pinglun= (TextView) convertView.findViewById(R.id.tv_pinglun);
//                holder.bottom_line = convertView.findViewById(R.id.bottom_line);
                convertView.setTag(holder);//绑定ViewHolder对象
            } else {
                holder = (RecordHolder) convertView.getTag();//取出ViewHolder对象
            }
            if (bean != null) {
                holder.tv_time.setText("期望上门时段：" + bean.visitDate + " " + bean.visitTime);
                holder.tv_type.setText(bean.repairTypeShow);
                holder.tv_date.setText( bean.createTimeShow);
                holder.tv_intro.setText(bean.descriptions);
                holder.record_address.setText(bean.orgName);
                holder.tv_canceled.setVisibility(View.GONE);
                holder.view_line.setVisibility(View.GONE);
                holder.custom_seekbar.setVisibility(View.VISIBLE);
                if(bean.state.equals("04")||bean.state.equals("06")){//已评价
                    holder.tv_fix_over.setText("已评价");
                    holder.tv_fix_over.setTextColor(getResources().getColor(R.color.line_color));
                    holder.custom_seekbar.setProgress(99);
                    holder.custom_seekbar.setSecondaryProgress(100);
                    holder.tv_pinglun.setVisibility(View.GONE);
                } else if (bean.stateShow.equals("已完成")) {
                    holder.tv_fix_over.setText("已完成");
                    holder.tv_fix_over.setTextColor(getResources().getColor(R.color.line_color));
                    holder.custom_seekbar.setProgress(92);
                    holder.custom_seekbar.setSecondaryProgress(100);
                    holder.tv_pinglun.setVisibility(View.VISIBLE);
                }
                 else if (bean.stateShow.equals("已提交")) {
                    holder.tv_fix_over.setText("已完成");
                    holder.tv_fix_over.setTextColor(getResources().getColor(R.color.line_color));
                    holder.custom_seekbar.setProgress(8);
                    holder.custom_seekbar.setSecondaryProgress(0);
                    holder.tv_pinglun.setVisibility(View.GONE);
                } else if (bean.stateShow.equals("处理中")) {
                    holder.tv_fix_over.setText("已完成");
                    holder.tv_fix_over.setTextColor(getResources().getColor(R.color.line_color));
                    holder.custom_seekbar.setProgress(50);
                    holder.custom_seekbar.setSecondaryProgress(0);
                    holder.tv_pinglun.setVisibility(View.GONE);
                }else {
                    //已取消状态
                    holder.tv_fix_over.setText("已取消");
                    holder.tv_fix_over.setTextColor(getResources().getColor(R.color.cancel_color));
                    holder.tv_canceled.setVisibility(View.GONE);
                    holder.view_line.setVisibility(View.VISIBLE);
                    holder.custom_seekbar.setVisibility(View.GONE);
                    //TODO................... this  is gone
                    holder.tv_pinglun.setVisibility(View.GONE);
                }
                if (bean.repairImage != null) {
//                    MyLog.e("KKK","bean.repairImage.size() is "+bean.repairImage.size());
                    if (bean.repairImage.size() > 0) {
                        instance.displayImage(bean.repairImage.get(0).imageUrl, holder.iv_img, build);
                    } else {
                        holder.iv_img.setImageResource(R.drawable.home_youpin);
                    }
                }
                holder.ll_record.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (CommonUtils.isNetworkConnected(ServiceRepairRecordActivity.this)) {
                            Intent intent = new Intent(ServiceRepairRecordActivity.this, ServiceRepairDetailActivity.class);
                            intent.putExtra(RECORD_INFO, bean.rid);
                            startActivityForResult(intent, 1);
                        } else {
                            toast("网络未连接或不可用");
                        }

                    }
                });

                //评论
                //TODO...
                holder.tv_pinglun.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent Intent=new Intent(ServiceRepairRecordActivity.this,ServiceRepairToComment.class);
                        Intent.putExtra("myrid", bean.rid);
                        Intent.putExtra("descriptions", bean.descriptions);
                        Intent.putExtra("id",position);
                        startActivityForResult(Intent,TOCOMMENT);

                    }
                });
            }
            return convertView;
        }

        private final class RecordHolder {
            public TextView tv_time;
            public TextView tv_type;
            public TextView tv_date;
            public TextView tv_intro;
            private ImageView iv_img;
            public View bottom_line;
            public MySeekBar custom_seekbar;
            public LinearLayout ll_record;
            public TextView tv_canceled;
            public View view_line;
            private TextView tv_fix_over,tv_pinglun;
            private TextView record_address;
            private RatingBar ratingbar;
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
                if (requestTag.equals(Constant.Repair.REPAIR_RECORD)) {// 报修记录
                    SharedPreferencesUtil.saveStringData(ServiceRepairRecordActivity.this,
                            Constant.Repair.REPAIR_RECORD, responseInfo.result);
                    parseRepairRecord(responseInfo.result);
                    MyLog.e("REPAIR_RECORD",responseInfo.result.toString());
                }
            }

            @Override
            public void onFailure(HttpException error, String msg, String requestTag) {
//                MyLog.e("FFF", "error is " + error.toString() + "; requestTag is " + requestTag);
                NetWorkStateUtils.errorNetMes(ServiceRepairRecordActivity.this);
            }
        };
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
