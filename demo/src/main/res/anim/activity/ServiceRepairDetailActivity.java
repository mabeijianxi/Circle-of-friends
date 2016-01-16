package anim.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.asyey.footballlibrary.network.exception.HttpException;
import com.asyey.footballlibrary.network.http.ResponseInfo;
import com.asyey.footballlibrary.universalimageloader.core.DisplayImageOptions;
import com.asyey.footballlibrary.universalimageloader.core.ImageLoader;
import com.henanjianye.soon.communityo2o.BaseActivity;
import com.henanjianye.soon.communityo2o.MainActivity;
import com.henanjianye.soon.communityo2o.R;
import com.henanjianye.soon.communityo2o.common.Constant;
import com.henanjianye.soon.communityo2o.common.MyLog;
import com.henanjianye.soon.communityo2o.common.UserDataHelper;
import com.henanjianye.soon.communityo2o.common.enties.BaseDataBean;
import com.henanjianye.soon.communityo2o.common.enties.CancelBean;
import com.henanjianye.soon.communityo2o.common.enties.JsonUtil;
import com.henanjianye.soon.communityo2o.common.enties.RepairBean;
import com.henanjianye.soon.communityo2o.common.enties.RepairDetailBean;
import com.henanjianye.soon.communityo2o.common.util.CommonUtils;
import com.henanjianye.soon.communityo2o.common.util.MyTitleBarHelper;
import com.henanjianye.soon.communityo2o.common.util.NetWorkStateUtils;
import com.henanjianye.soon.communityo2o.common.util.SharedPreferencesUtil;
import com.henanjianye.soon.communityo2o.interf.NetWorkCallback;
import com.henanjianye.soon.communityo2o.submitphote.ImagePagerActivity;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

public class ServiceRepairDetailActivity extends BaseActivity {
    private ListView detail_list;
    private MyTitleBarHelper myTitleBarHelper;
    private ImageView repair_img;
    private static ImageLoader instance = ImageLoader.getInstance();
    private static DisplayImageOptions build = new DisplayImageOptions.Builder()
            .showImageOnFail(R.drawable.home_youpin)
            .showImageForEmptyUri(R.drawable.home_youpin).showImageOnLoading(R.drawable.home_youpin).build();
    private ArrayList<String> imgUrlList = new ArrayList<>();
    private RepairBean rBean;
    private TextView repair_id;
    private TextView repair_title;
    private TextView repair_intro;
    private TextView tv_indicator;
    private Button btn_cancel_repair;
    private String rid = "";
    private DetailAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_repair_detail);
        initViews();
        initEvents();
        getData();
    }

    private void getData() {
        if (!rid.equals("")) {
            if (CommonUtils.isNetworkConnected(this)) {
                // 通知网络请求
                requestDetailData(rid);
            } else {
                toast("网络未连接或不可用");
            }
        } else {
            toast("数据错误");
        }
    }

    private void requestDetailData(String rid) {
        new UserDataHelper(this).getRepairDetailInfor(getNetRequestHelper(this).isShowProgressDialog(true), rid);
    }

    RepairDetailBean repairDetailBean;

    private void parseDetailRecord(String result) {
        BaseDataBean<RepairDetailBean> json = JsonUtil.parseDataObject(result, RepairDetailBean.class);
        if (json.code == 100) {
            if (json.data != null) {
                repairDetailBean = json.data;
                mAdapter.setData(repairDetailBean);
                rBean = repairDetailBean.repairInfo;
                repair_id.setText("单号：" + rBean.repairCode);
                if (rBean.repairType != null) {
                    if (rBean.repairType.equals("2")) {
                        repair_title.setText("居家维修");
                    } else {
                        repair_title.setText("公共维修");
                    }
                }
                repair_intro.setText(rBean.descriptions);

                //详情界面的图片
                if (imgUrlList != null && rBean.repairImage != null) {
                    for (int i = 0; i < rBean.repairImage.size(); i++) {
                        imgUrlList.add(rBean.repairImage.get(i).imageUrl);
                    }
                    if (imgUrlList.size() > 0) {
                        instance.displayImage(imgUrlList.get(0), repair_img, build);
                        tv_indicator.setText("1/" + imgUrlList.size());
                    } else {
                        repair_img.setImageResource(R.drawable.home_youpin);
                        tv_indicator.setText("1/1");
                    }
                }
                if (detailBeanList != null && detailBeanList.size() > 0) {
                    detailBeanList.clear();
                }

//                if (repairDetailBean.rstList.size() == 4) {
//                    //后台数据不对  一共就三条数据
//                    for (int i = 0; i < repairDetailBean.rstList.size() - 1; i++) {
//                        if (repairDetailBean.rstList != null && repairDetailBean.rstList.size() > 0) {
//                            detailBeanList.add(repairDetailBean.rstList.get(i));
//                        }
//                    }
//                } else {
                if (repairDetailBean.rstList != null && repairDetailBean.rstList.size() > 0) {
                    detailBeanList.addAll(repairDetailBean.rstList);
                }
//                }

                if (mAdapter != null) {
                    mAdapter.notifyDataSetChanged();
                }

//                if (rBean.stateShow.equals("已提交")) {
//                    if (detailBeanList != null) {
//                        detailBeanList.add(rBean.);
//                    }
//                } else if (rBean.stateShow.equals("已完成")) {
//                    if (detailBeanList != null) {
//                        detailBeanList.addAll(repairDetailBean.rstList);
//                        detailBeanList.add(rBean);
//                        detailBeanList.add(rBean);
//                    }
//                }
                //只有一条数据的时候显示取消按钮
                if (detailBeanList != null && detailBeanList.size() == 1) {
                    btn_cancel_repair.setVisibility(View.VISIBLE);
                }
//            img_urls.addAll(rBean.upload);
//            confirm_question.setText(rBean.des);
//            confirm_date_time.setText("期望上门时间： " + rBean.visitDate + " " + rBean.visitTime);
            }
        }
    }

    private void initViews() {
        myTitleBarHelper = new MyTitleBarHelper(this, getWindow().getDecorView().findViewById(android.R.id.content));
        myTitleBarHelper.setLeftImgVisible(false);
        myTitleBarHelper.resetState();
        myTitleBarHelper.setMiddleText("报修详情");
        myTitleBarHelper.setRightImgVisible(false);
        myTitleBarHelper.setLeftImag(R.mipmap.btn_back);
        repair_id = (TextView) findViewById(R.id.repair_id);
        repair_title = (TextView) findViewById(R.id.repair_title);
        repair_intro = (TextView) findViewById(R.id.repair_intro);
        repair_img = (ImageView) findViewById(R.id.repair_img);
        detail_list = (ListView) findViewById(R.id.detail_list);
        btn_cancel_repair = (Button) findViewById(R.id.btn_cancel_repair);
        tv_indicator = (TextView) findViewById(R.id.tv_indicator);
        Intent intent = getIntent();
        isNotice = intent.getBooleanExtra("onlineFlag", false);
        rid = intent.getStringExtra(ServiceRepairRecordActivity.RECORD_INFO);
    }

    private void initEvents() {
        myTitleBarHelper.setOnclickListener(this);
        repair_img.setOnClickListener(this);
        mAdapter = new DetailAdapter(this);
        detail_list.setAdapter(mAdapter);
        detail_list.setDividerHeight(0);
        btn_cancel_repair.setOnClickListener(this);
    }

    private boolean isNotice = false;

    //    isNotice = intent.getBooleanExtra("onlineFlag", false);
    private void press_back() {
        if (isNotice && SharedPreferencesUtil.getStringData(this, Constant.ShouYeUrl.APP_STATUS, "0").equals("1")) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            this.finish();
        }
    }

    @Override
    public void onBackPressed() {
        press_back();
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.iv_title_bar_left:
                press_back();
                break;
            case R.id.repair_img:
                if (imgUrlList != null && imgUrlList.size() > 0) {
                    Intent intent = new Intent(ServiceRepairDetailActivity.this, ImagePagerActivity.class);
                    intent.putExtra(ImagePagerActivity.Extra.IMAGE_POSITION, 0);
                    intent.putStringArrayListExtra(ImagePagerActivity.Extra.IMAGES, imgUrlList);
                    startActivity(intent);
                }
                break;
            case R.id.btn_cancel_repair:
                showDialog();
                break;
            default:
                break;
        }
    }

    private void showDialog() {
        LayoutInflater factory = LayoutInflater.from(this);
        final View textEntryView = factory.inflate(R.layout.repair_cancel, null);
        final Dialog dialog = new AlertDialog.Builder(ServiceRepairDetailActivity.this)
                .setView(textEntryView)
                .create();
        //取消按钮
        ImageView btnCancel = (ImageView) textEntryView.findViewById(R.id.btn_close);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        final EditText edit_cancel_Content = (EditText) textEntryView.findViewById(R.id.edit_cancel_Content);
        // 获取编辑框焦点
        edit_cancel_Content.setFocusable(true);
//        //打开软键盘
//        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        //取消维修
        TextView tv_cancel = (TextView) textEntryView.findViewById(R.id.tv_cancel);
        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CommonUtils.isNetworkConnected(ServiceRepairDetailActivity.this)) {
                    String getContet = edit_cancel_Content.getText().toString();
                    if (rBean != null) {
                        requestData(rBean.rid, getContet);
                    }
                } else {
                    toast("网络未连接或不可用");
                }
            }
        });
        dialog.show();
    }

    private void requestData(String repairId, String remark) {
        new UserDataHelper(this).requestCancelRepair(getNetRequestHelper(this).isShowProgressDialog(true), repairId, remark);
    }

    @Override
    protected NetWorkCallback setNetWorkCallback() {
        return new NetWorkCallback() {
            @Override
            public void onStart(String requestTag) {
            }

            @Override
            public void onCancelled(String requestTag) {
                toast("取消提交操作");
            }

            @Override
            public void onLoading(long total, long current, boolean isUploading, String requestTag) {
            }

            @Override
            public void onSuccess(ResponseInfo<String> responseInfo, String requestTag) {
                if (requestTag.equals(Constant.Repair.REPAIR_CANCEL)) {
                    BaseDataBean<CancelBean> baseDataBean = JsonUtil.parseDataObject(responseInfo.result, CancelBean.class);
                    if (baseDataBean.code == 100) {
                        CancelBean cancelBean = baseDataBean.data;
                        toast(cancelBean.rtnMsg);
                        Intent intent = new Intent();
                        setResult(RESULT_OK, intent);
                        finish();
//                        switch (cancelBean.rtnCode) {
//                            //0:已接单不能撤销，
//                            case 0:
//                                finish();
//                                break;
//                            //1：撤销成功，
//                            case 1:
//                                finish();
//                                break;
//                            //2：无此报修单
//                            case 2:
//                                Intent intent = new Intent();
//                                setResult(RESULT_OK, intent);
//                                finish();
//                                break;
//                            default:
//                                break;
//                        }
                    } else {
                        toast(baseDataBean.msg);
                    }
                } else if (requestTag.equals(Constant.Repair.REPAIR_DETAIL)) {
                    parseDetailRecord(responseInfo.result);
                    MyLog.e("REPAIR_DETAIL", responseInfo.result.toString());
                }
            }

            @Override
            public void onFailure(HttpException error, String msg, String requestTag) {
                NetWorkStateUtils.errorNetMes(ServiceRepairDetailActivity.this);
            }
        };
    }


    List<RepairDetailBean.RepairState> detailBeanList = new ArrayList<>();
    public static int STATUS_SUBMITED = 0;
    public static int STATUS_DEALING = 1;
    public static int STATUS_FINISHED = 2;

    class DetailAdapter extends BaseAdapter {
        Context mContext;
        LayoutInflater mLInflater;
        RepairDetailBean repairDetailBean;

        public void setData(RepairDetailBean repairDetailBean) {
            this.repairDetailBean = repairDetailBean;
        }


        public DetailAdapter(Context context) {
            this.mContext = context;
            this.mLInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return detailBeanList.size();
        }

        @Override
        public Object getItem(int position) {
            if (detailBeanList != null && detailBeanList.size() != 0) {
                return detailBeanList.get(position);
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            DetailHolder holder;
            if (convertView == null) {
                convertView = mLInflater.inflate(R.layout.item_repair_detail, parent, false);
                holder = new DetailHolder();
                /**得到各个控件的对象*/
                holder.tv_status = (TextView) convertView.findViewById(R.id.tv_status);
                holder.tv_detail_1 = (TextView) convertView.findViewById(R.id.tv_detail_1);
                holder.tv_detail_2 = (TextView) convertView.findViewById(R.id.tv_detail_2);
                holder.tv_detail_3 = (TextView) convertView.findViewById(R.id.tv_detail_3);
                holder.tv_detail_4 = (TextView) convertView.findViewById(R.id.tv_detail_4);
                holder.bottom_line = convertView.findViewById(R.id.bottom_line);
                holder.img_icon = (ImageView) convertView.findViewById(R.id.img_icon);
                holder.top_line = convertView.findViewById(R.id.top_line);
                holder.ratingbar = (RatingBar) convertView.findViewById(R.id.ratingbar);
                convertView.setTag(holder);//绑定ViewHolder对象
            } else {
                holder = (DetailHolder) convertView.getTag();//取出ViewHolder对象
            }
            RepairDetailBean.RepairState repairState = (RepairDetailBean.RepairState) getItem(position);
            if (repairState != null) {
                //   维修单状态 ，01:已提交，02：处理站，03：已完成待评价，04：已完成已评价，05：已取消，06：已回复

                if (repairState.afterState.equals("04")) {//已评价且已回复
                    holder.tv_status.setText(repairState.afterStateShow);
                    holder.tv_detail_1.setText(repairState.createTimeShow);
                    holder.tv_detail_2.setVisibility(View.GONE);
                    holder.tv_detail_4.setVisibility(View.VISIBLE);
                    holder.top_line.setBackgroundColor(getResources().getColor(R.color.repair_line_color));
                    try {
                        if (!TextUtils.isEmpty(repairDetailBean.evaluationInfo.content)) {
                            holder.tv_detail_3.setText("我：" + repairDetailBean.evaluationInfo.content);
                        } else {
                            holder.tv_detail_3.setText("");
                        }
                        if (!TextUtils.isEmpty(repairDetailBean.evaluationInfo.evaluateReply.content) && !TextUtils.isEmpty(repairDetailBean.evaluationInfo.evaluateReply.replyuser)) {
                            // holder.tv_detail_4.setText("  一家客服："+repairDetailBean.evaluationInfo.evaluateReply.content);
                            holder.tv_detail_4.setText(repairDetailBean.evaluationInfo.evaluateReply.replyuser + ":" + repairDetailBean.evaluationInfo.evaluateReply.content);
                            holder.tv_detail_4.setBackgroundResource(R.drawable.event_di);
                        } else {
                            holder.tv_detail_4.setText("");
                        }
                        //  if(repairDetailBean.evaluationInfo.showEvaluationState)
                        //TODO..评论等级待处理
                        holder.ratingbar.setVisibility(View.VISIBLE);
                        if (repairDetailBean != null) {
                            holder.ratingbar.setRating(repairDetailBean.evaluationInfo.grade);
                        }
                    } catch (Exception e) {
                        MyLog.e("afterState.equals(04)", e.toString());
                    }


                } /*else if(repairState.afterState.equals("04")){//已评价
                    holder.tv_status.setText(repairState.afterStateShow);
                    holder.tv_detail_1.setText(repairState.createTimeShow);
                    holder.tv_detail_2.setVisibility(View.GONE);
                    holder.tv_detail_3.setText("评价内容");
                    holder.tv_detail_4.setVisibility(View.GONE);
                    holder.ratingbar.setVisibility(View.VISIBLE);
                    holder.ratingbar.setRating(4);

                }*/ else if (repairState.afterState.equals("01") || repairState.afterState.equals("05")) {
                    //已提交和已撤单
                    holder.ratingbar.setVisibility(View.GONE);
                    holder.tv_detail_2.setVisibility(View.VISIBLE);
                    holder.tv_status.setText(repairState.afterStateShow);
                    holder.tv_detail_1.setText(repairState.createTimeShow);
                    holder.tv_detail_2.setText("报修" + repairState.afterStateShow);
                    holder.tv_detail_3.setText("期望上门时段：" + rBean.visitDate + " " + rBean.visitTime);
                    holder.tv_detail_4.setVisibility(View.GONE);
                    holder.top_line.setBackgroundColor(getResources().getColor(R.color.repair_line_color));
                    //控制是否显示小绿点
                    if (detailBeanList.size() != 1) {
                        holder.img_icon.setVisibility(View.GONE);
                    } else {
                        holder.img_icon.setVisibility(View.VISIBLE);
                    }
                } else {
                    //处理中和已完成
                    holder.ratingbar.setVisibility(View.GONE);
                    holder.tv_detail_2.setVisibility(View.VISIBLE);
                    holder.tv_status.setText(repairState.afterStateShow);
                    holder.tv_detail_1.setText(repairState.createTimeShow);
                    holder.tv_detail_2.setText("报修" + repairState.afterStateShow);
                    holder.tv_detail_3.setText("处理人：" + repairState.handleUser);
                    if (repairState.repairUserContact != null) {
                        holder.tv_detail_4.setText("联系方式：" + repairState.repairUserContact);
                    } else {
                        holder.tv_detail_4.setVisibility(View.GONE);
                    }
                    holder.top_line.setBackgroundColor(getResources().getColor(R.color.repair_line_color));
                    //已完成显示小绿点 否则不显示
                    if (!repairState.afterState.equals("04")) {
                        holder.img_icon.setVisibility(View.GONE);
                    } else {
                        holder.img_icon.setVisibility(View.VISIBLE);
                    }
                }
                if (repairDetailBean != null) {
                    //已撤单状态把字都显示成灰色
                    if (repairDetailBean.repairInfo.stateShow.equals("已撤单")) {
                        holder.tv_status.setTextColor(getResources().getColor(R.color.gray));
                        holder.tv_detail_1.setTextColor(getResources().getColor(R.color.gray));
                        holder.tv_detail_2.setTextColor(getResources().getColor(R.color.gray));
                        holder.tv_detail_3.setTextColor(getResources().getColor(R.color.gray));
                        holder.tv_detail_4.setTextColor(getResources().getColor(R.color.gray));
                        holder.img_icon.setVisibility(View.GONE);
                        holder.top_line.setBackgroundColor(getResources().getColor(R.color.gray));
                        holder.bottom_line.setBackgroundColor(getResources().getColor(R.color.gray));
                    } else {
                        if (detailBeanList.size() != 1) {
                            holder.bottom_line.setBackgroundColor(getResources().getColor(R.color.repair_line_color));
                        }
                    }
                }
                /*if (position == 3) {
                    //已完成状态都不显示底部的线条
                    holder.bottom_line.setVisibility(View.GONE);
                } else {
                    holder.bottom_line.setVisibility(View.VISIBLE);
                }*/
                //已评论或者已回复的此时为结束点
                if (repairState.afterState.equals("06") || repairState.afterState.equals("04")) {
                    holder.bottom_line.setVisibility(View.GONE);
                } else {
                    holder.bottom_line.setVisibility(View.VISIBLE);
                }
            }
            return convertView;
        }

        @Override
        public int getItemViewType(int position) {
            switch (position) {
                case 0:
                    return STATUS_SUBMITED;
                case 1:
                    return STATUS_DEALING;
                case 2:
                    return STATUS_FINISHED;
                default:
                    return super.getItemViewType(position);
            }
        }

        @Override
        public int getViewTypeCount() {
            return 3;
        }


    }

    /**
     * 存放控件
     */
    private final class DetailHolder {
        public TextView tv_status;
        public ImageView img_icon;
        public TextView tv_detail_1;
        public TextView tv_detail_2;
        public TextView tv_detail_3;
        public TextView tv_detail_4;
        public View bottom_line;
        public View top_line;
        public RatingBar ratingbar;
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
