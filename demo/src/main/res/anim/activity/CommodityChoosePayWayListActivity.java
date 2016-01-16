package anim.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.asyey.footballlibrary.network.exception.HttpException;
import com.asyey.footballlibrary.network.http.ResponseInfo;
import com.henanjianye.soon.communityo2o.BaseActivity;
import com.henanjianye.soon.communityo2o.R;
import com.henanjianye.soon.communityo2o.common.Constant;
import com.henanjianye.soon.communityo2o.common.MallDataHelper;
import com.henanjianye.soon.communityo2o.common.enties.BaseDataBean;
import com.henanjianye.soon.communityo2o.common.enties.CommodityListBeans;
import com.henanjianye.soon.communityo2o.common.enties.JsonUtil;
import com.henanjianye.soon.communityo2o.common.util.MyTitleBarHelper;
import com.henanjianye.soon.communityo2o.common.util.NetWorkStateUtils;
import com.henanjianye.soon.communityo2o.interf.MyItemClickListener;
import com.henanjianye.soon.communityo2o.interf.NetWorkCallback;
import com.henanjianye.soon.communityo2o.view.UserMenu;
import com.umeng.analytics.MobclickAgent;

/**
 * Created by Administrator on 2015/9/14.
 */
public class CommodityChoosePayWayListActivity extends BaseActivity {

    private UserMenu mMenu;
    private MyTitleBarHelper myTitleBarHelper;
    private MallDataHelper mallDataHelper;
    private CheckBox commodity_help_get;
    private CheckBox commodity_self_get;
    /**
     * 0 表示快递 1，表示自取
     */
    private int getWayState;
    private Intent intent;
    private int isPickup;
    private TextView tv_self_get;

    @Override
    public int mysetContentView() {
        return R.layout.activity_commodity_choose_pay_way;
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

        myTitleBarHelper = new MyTitleBarHelper(this, getWindow().getDecorView().findViewById(android.R.id.content));
        myTitleBarHelper.setOnclickListener(this);
        myTitleBarHelper.setRightText("确定");
        myTitleBarHelper.setMiddleText("配送与付款");
        commodity_self_get = (CheckBox) findViewById(R.id.commodity_self_get);
        commodity_help_get = (CheckBox) findViewById(R.id.commodity_help_get);
        tv_self_get = (TextView)findViewById(R.id.tv_self_get);
        commodity_help_get.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    commodity_self_get.setChecked(false);
                    getWayState = 0;
                    commodity_help_get.setEnabled(false);
                } else {
                    commodity_help_get.setEnabled(true);
                }
            }
        });
        commodity_self_get.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    commodity_help_get.setChecked(false);
                    getWayState = 1;
                    commodity_self_get.setEnabled(false);
                } else {
                    commodity_self_get.setEnabled(true);
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.iv_title_bar_left:
                finish();
                break;
            case R.id.tv_title_bar_right:
                //TODO 确定

                intent.putExtra("getWayState", getWayState);
                setResult(RESULT_OK, intent);
                finish();
                break;


        }
    }


    private void initEvent() {
        myTitleBarHelper.setOnclickListener(this);
        intent = getIntent();
        getWayState = intent.getIntExtra("getWayState", 0);
        isPickup = intent.getIntExtra("isPickup", 0);
        if (isPickup == 0) {
            commodity_self_get.setEnabled(false);
            tv_self_get.setVisibility(View.VISIBLE);
        }else{
            tv_self_get.setVisibility(View.GONE);
        }
    }

    private void initProcess() {
        if (getWayState == 0) {
            commodity_help_get.setChecked(true);
            commodity_self_get.setChecked(false);
        } else {
            commodity_help_get.setChecked(false);
            commodity_self_get.setChecked(true);
        }

    }

    private void initCommoditySort() {
        //TODO 处理请求的缓存
        if (mallDataHelper == null) {
            mallDataHelper = new MallDataHelper(this);
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
                if (Constant.MallUrl.COMMODITY_LIST.equals(requestTag)) {
                    parseDate(responseInfo.result, false);
                }
            }

            @Override
            public void onFailure(HttpException error, String msg, String requestTag) {
                NetWorkStateUtils.errorNetMes(CommodityChoosePayWayListActivity.this);
            }
        };
    }

    private synchronized void parseDate(String str, boolean isCach) {
        if (str == null) {
            return;
        }
        BaseDataBean<CommodityListBeans> baseDataBean = JsonUtil.parseDataObject(str, CommodityListBeans.class);
        CommodityListBeans commodityListBeans = baseDataBean.data;
        if (baseDataBean.code == 100) {
            //TODO 获取传递来的  classType  和 classID 以下为测试值

        }
    }


    private class CellHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final MyItemClickListener myItemClickListener;
        public TextView iv_commodity_large_title;
        public TextView iv_commodity_large_price;
        public TextView iv_commodity_large_left;
        public TextView iv_commodity_large_right;
        public ImageView iv_commodity_large_img;

        public CellHolder(View itemView, MyItemClickListener myItemClickListener) {
            super(itemView);
            this.myItemClickListener = myItemClickListener;
            itemView.setOnClickListener(this);
            iv_commodity_large_img = (ImageView) itemView.findViewById(R.id.iv_commodity_large_img);


            iv_commodity_large_title = (TextView) itemView.findViewById(R.id.iv_commodity_large_title);
            iv_commodity_large_price = (TextView) itemView.findViewById(R.id.iv_commodity_large_price);
            iv_commodity_large_left = (TextView) itemView.findViewById(R.id.iv_commodity_large_left);
            iv_commodity_large_right = (TextView) itemView.findViewById(R.id.iv_commodity_large_right);
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
