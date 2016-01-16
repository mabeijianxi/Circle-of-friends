package anim.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
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
import com.henanjianye.soon.communityo2o.common.GoodsCardDataHelper;
import com.henanjianye.soon.communityo2o.common.UserDataHelper;
import com.henanjianye.soon.communityo2o.common.enties.BaseBean;
import com.henanjianye.soon.communityo2o.common.enties.BaseDataBean;
import com.henanjianye.soon.communityo2o.common.enties.CommodityBean;
import com.henanjianye.soon.communityo2o.common.enties.JsonUtil;
import com.henanjianye.soon.communityo2o.common.enties.UpdateBean;
import com.henanjianye.soon.communityo2o.common.enties.WaterBean;
import com.henanjianye.soon.communityo2o.common.util.CommonUtils;
import com.henanjianye.soon.communityo2o.common.util.MyTitleBarHelper;
import com.henanjianye.soon.communityo2o.common.util.NetWorkStateUtils;
import com.henanjianye.soon.communityo2o.common.util.SharedPreferencesUtil;
import com.henanjianye.soon.communityo2o.common.util.UserSharedPreferencesUtil;
import com.henanjianye.soon.communityo2o.interf.NetWorkCallback;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;

public class ServiceWaterCarriage extends BaseActivity {
    private MyTitleBarHelper myTitleBarHelper;
    private PullToLoadView mPullToLoadView;
    private RecyclerView mRecyclerView;
    private WaterAdapter mAdapter;
    private TextView tv_submmit_button;
    ImageLoader instance = ImageLoader.getInstance();
    DisplayImageOptions build = new DisplayImageOptions.Builder()
            .showImageOnFail(R.mipmap.gouwuche_image)
            .showImageForEmptyUri(R.mipmap.gouwuche_image).showImageOnLoading(R.mipmap.gouwuche_image).build();
    private String currentSubmmitCardIds;
    private GoodsCardDataHelper goodsCardDataHelper;
    private int[] currentUpdateNum = new int[2];
    private int LoginState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watercarriage);
        initViews();
        initEvents();
    }

    private void initViews() {
        goodsCardDataHelper = new GoodsCardDataHelper(this);
        myTitleBarHelper = new MyTitleBarHelper(this, getWindow().getDecorView().findViewById(android.R.id.content));
        myTitleBarHelper.setLeftImgVisible(false);
        myTitleBarHelper.resetState();
        myTitleBarHelper.setMiddleText("送水");
        myTitleBarHelper.setRightImgVisible(false);
        myTitleBarHelper.setLeftImag(R.mipmap.btn_back);
        mPullToLoadView = (PullToLoadView) findViewById(R.id.water_pullToRefresh);
        mPullToLoadView.isRefreshEnabled(false);
        mRecyclerView = mPullToLoadView.getRecyclerView();
        LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(manager);
        mAdapter = new WaterAdapter();
        mRecyclerView.setAdapter(mAdapter);
        tv_submmit_button = (TextView) findViewById(R.id.tv_submmit_button);
        orgId = new UserSharedPreferencesUtil().getUserInfo(this).orgId;
        LoginState = UserSharedPreferencesUtil.getUserLoginState(this);
        //1. 通知数据缓存
        String waterString = SharedPreferencesUtil.getStringData(ServiceWaterCarriage.this,
                Constant.ShouYeUrl.WATER, null);
        // 通知JSON解析
        if (!TextUtils.isEmpty(waterString)) {// 解析数据
            parseWaterRecord(waterString);
        }
    }

    ArrayList<UpdateBean> uBeansList = new ArrayList<>();

    class WaterAdapter extends RecyclerView.Adapter<WaterAdapter.MyViewHolder> {
        ArrayList<CommodityBean> waterList = new ArrayList<>();
        CommodityBean goodsBean;
//        UpdateBean updateBean;

//        public List<CommodityBean> getWaterList() {
//            return waterList;
//        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            MyViewHolder holder = new MyViewHolder(LayoutInflater.from(
                    ServiceWaterCarriage.this).inflate(R.layout.item_service_water, parent,
                    false));
            return holder;
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            if (waterList.size() > 0) {
                goodsBean = waterList.get(position);
                UpdateBean updateBean = uBeansList.get(position);
                if (goodsBean != null) {
                    holder.tv_water_title.setText(goodsBean.name);
                    holder.tv_water_price.setText("￥ " + goodsBean.currentPriceShow);
                    holder.et_water_num.setText(updateBean.getGoodsNum + "");
                    instance.displayImage(goodsBean.mainPhoto.picUrl, holder.iv_water_img, build);
                    holder.tv_water_num_minus.setEnabled(Integer.parseInt(holder.et_water_num.getText().toString()) > 0);
                    holder.tv_water_num_plus.setTag(position);
                    //点击添加
                    holder.tv_water_num_plus.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Object object = v.getTag();
                            if (LoginState < 2) {
                                //去登陆
                                CommonUtils.startLoginActivity(ServiceWaterCarriage.this);
                            } else {
                                if (object != null) {
                                    int pos = Integer.parseInt(object.toString());
                                    UpdateBean updateBean = uBeansList.get(pos);
                                    CommodityBean waterBean = waterList.get(pos);
                                    updateBean.goodsId = waterBean.goodsId;
                                    currentUpdateNum[0] = pos;
                                    currentUpdateNum[1] = updateBean.getGoodsNum + 1;
                                    goodsCardDataHelper.updateGoodsCardNum(getNetRequestHelper(ServiceWaterCarriage.this), updateBean.goodsId, updateBean.getGoodsNum + 1);
                                }
                            }
                        }
                    });
                    holder.tv_water_num_minus.setTag(position);
                    //点击减少
                    holder.tv_water_num_minus.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Object object = v.getTag();
                            if (LoginState < 2) {
                                //去登陆
                                CommonUtils.startLoginActivity(ServiceWaterCarriage.this);
                            } else {
                                //可以购买
                                if (object != null) {
                                    int pos = Integer.parseInt(object.toString());
                                    UpdateBean updateBean = uBeansList.get(pos);
                                    CommodityBean waterBean = waterList.get(pos);
                                    updateBean.goodsId = waterBean.goodsId;
                                    currentUpdateNum[0] = pos;
                                    currentUpdateNum[1] = updateBean.getGoodsNum - 1;
                                    goodsCardDataHelper.updateGoodsCardNum(getNetRequestHelper(ServiceWaterCarriage.this), goodsBean.goodsId, updateBean.getGoodsNum - 1 != 0 ? (updateBean.getGoodsNum - 1) : 1);
                                }
                            }
                        }
                    });
                }
            }
        }

        @Override
        public int getItemCount() {
            return waterList.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {

            TextView tv_water_title;
            ImageView iv_water_img;
            TextView tv_water_price;
            TextView tv_water_num_minus;
            TextView et_water_num;
            TextView tv_water_num_plus;

            public MyViewHolder(View view) {
                super(view);
                iv_water_img = (ImageView) view.findViewById(R.id.iv_water_img);
                tv_water_title = (TextView) view.findViewById(R.id.tv_water_title);
                tv_water_price = (TextView) view.findViewById(R.id.tv_water_price);
                tv_water_num_minus = (TextView) view.findViewById(R.id.tv_water_num_minus);
                et_water_num = (TextView) view.findViewById(R.id.et_water_num);
                tv_water_num_plus = (TextView) view.findViewById(R.id.tv_water_num_plus);
            }
        }

        public void add(ArrayList<CommodityBean> waterBeanList) {
            if (waterList != null) {
                waterList.addAll(waterBeanList);
                for (int i = 0; i < waterBeanList.size(); i++) {
                    UpdateBean updateBean = new UpdateBean();
                    //如有有规格 把规格ID拼接起来
                    if (waterBeanList.get(i).inventorySpecs != null && waterBeanList.get(i).inventorySpecs.size() > 0) {
                        for (int j = 0; i < waterBeanList.get(i).inventorySpecs.size(); j++) {
                            updateBean.propIds = updateBean.propIds + "_" + waterBeanList.get(i).inventorySpecs.get(j);
                        }
                    }
                    uBeansList.add(updateBean);
                }
                notifyDataSetChanged();
            }
        }

        public void clear() {
            if (waterList != null && waterList.size() > 0) {
                waterList.clear();
                notifyDataSetChanged();
            }
        }
    }

    private void submit_Goods() {
        goodsCardDataHelper = new GoodsCardDataHelper(this);
        currentSubmmitCardIds = getSelectGoosCartIds();
        if (!TextUtils.isEmpty(currentSubmmitCardIds)) {
            goodsCardDataHelper.submmitBuy(getNetRequestHelper(ServiceWaterCarriage.this), currentSubmmitCardIds);
        } else {
            toast("请选择需要结算的订单");
        }
    }

    private String getSelectGoosCartIds() {
        String goodsCartIds = "";
        String guiGeIds = "";
        for (int i = 0; i < uBeansList.size(); i++) {
            if (uBeansList.get(i).getGoodsNum > 0) {
                if (uBeansList.get(i).propIds != null && !uBeansList.get(i).propIds.equals("")) {
                    guiGeIds = uBeansList.get(i).propIds;
                } else {
                    guiGeIds = "0";
                }
                goodsCartIds = goodsCartIds + uBeansList.get(i).goodsId + "," + guiGeIds + "," + uBeansList.get(i).getGoodsNum + "|";
            }
        }
        return goodsCartIds;
    }

    private void initEvents() {
        myTitleBarHelper.setOnclickListener(this);
        tv_submmit_button.setOnClickListener(this);
        mPullToLoadView.setPullCallback(new PullCallback() {
            @Override
            public void onLoadMore() {

            }

            @Override
            public void onRefresh() {
                if (CommonUtils.isNetworkConnected(ServiceWaterCarriage.this)) {
                    mPullToLoadView.setEnabled(true);
                    if (orgId != -1) {
                        // 通知网络请求
                        requestWaterData(orgId);
                    } else {
                        toast("数据错误");
                    }
                } else {
                    toast("网络未连接或不可用");
                    if (mPullToLoadView != null) {
                        mPullToLoadView.setComplete();
                    }
                }
            }

            @Override
            public boolean isLoading() {
                return false;
            }

            @Override
            public boolean hasLoadedAllItems() {
                return false;
            }
        });
        mPullToLoadView.initNetLoad(this);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.iv_title_bar_left:
                this.finish();
                break;
            case R.id.tv_submmit_button:
                if (LoginState < 2) {
                    //去登陆
                    CommonUtils.startLoginActivity(this);
                } else {
                    //可以购买
                    submit_Goods();
                }
                break;
            default:
                break;
        }
    }

    public enum TYPE {CACHE, NET}

    private int orgId;

    private void requestWaterData(int orgId) {
        new UserDataHelper(this).getWaterInfor(getNetRequestHelper(this).isShowProgressDialog(false), orgId);
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
                if (requestTag.equals(Constant.ShouYeUrl.WATER)) {// 送水记录
                    SharedPreferencesUtil.saveStringData(ServiceWaterCarriage.this,
                            Constant.ShouYeUrl.WATER, responseInfo.result);
                    if (responseInfo.result != null && mPullToLoadView != null) {
                        mPullToLoadView.isRefreshEnabled(true);
                    }
                    parseWaterRecord(responseInfo.result);

                } else if (Constant.MallUrl.COMMODITY_CART_UPDATE.equals(requestTag)) {
                    UpdateBean updateBean = uBeansList.get(currentUpdateNum[0]);
                    if (updateBean != null) {
                        updateBean.getGoodsNum = currentUpdateNum[1];
                        mAdapter.notifyItemChanged(currentUpdateNum[0]);
                    }
                } else if (Constant.MallUrl.COMMODITY_SUBMIT_WATER.equals(requestTag)) {
//                    MyLog.e("KKK", "Constant.ShouYeUrl.SUBMIT.result is " + responseInfo.result);
                    BaseBean<Object> baseBean = JsonUtil.jsonArray(responseInfo.result, Object.class);
                    if (baseBean.code == 100) {
                        SharedPreferencesUtil.saveStringData(ServiceWaterCarriage.this, Constant.CachTag.APP_COMMDITY_GOODS_CART_SUBMMIT, responseInfo.result);
                        Intent intent = new Intent(ServiceWaterCarriage.this, CommoditySettlementActivity.class);
                        intent.putExtra(CommodityGoodsCardListActivity.CURRENTSUBMMITCARDIDS, currentSubmmitCardIds);
                        startActivity(intent);
                    } else {
                        toast(baseBean.msg);
                    }
                }
                if (mPullToLoadView != null) {
                    mPullToLoadView.setComplete();
                }
            }

            @Override
            public void onFailure(HttpException error, String msg, String requestTag) {
//                MyLog.e("KKK", "water error msg is " + error.toString() + ";requestTag is " + requestTag);
                NetWorkStateUtils.errorNetMes(ServiceWaterCarriage.this);
            }
        };
    }

    private void parseWaterRecord(String result) {
        try {
            BaseDataBean<WaterBean> json = JsonUtil.parseDataObject(result, WaterBean.class);
            if (json.code == 100) {
                if (json.data != null) {
                    mAdapter.clear();
                    if (json.data.defaultGoodsList != null && json.data.defaultGoodsList.size() > 0) {
                        mAdapter.add(json.data.defaultGoodsList);
                        if (mAdapter.getItemCount() > 0) {
                            if (mAdapter == null) {
                                mAdapter = new WaterAdapter();
                                mRecyclerView.setAdapter(mAdapter);
                            } else {
                                mAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
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
