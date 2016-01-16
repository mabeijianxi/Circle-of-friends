package anim.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.asyey.footballlibrary.network.exception.HttpException;
import com.asyey.footballlibrary.network.http.ResponseInfo;
import com.asyey.footballlibrary.universalimageloader.core.DisplayImageOptions;
import com.asyey.footballlibrary.universalimageloader.core.ImageLoader;
import com.henanjianye.soon.communityo2o.BaseActivity;
import com.henanjianye.soon.communityo2o.R;
import com.henanjianye.soon.communityo2o.common.UserDataHelper;
import com.henanjianye.soon.communityo2o.common.enties.BaseDataBean;
import com.henanjianye.soon.communityo2o.common.enties.JsonUtil;
import com.henanjianye.soon.communityo2o.common.enties.RepairBean;
import com.henanjianye.soon.communityo2o.common.enties.UserBean;
import com.henanjianye.soon.communityo2o.common.util.MyTitleBarHelper;
import com.henanjianye.soon.communityo2o.common.util.NetWorkStateUtils;
import com.henanjianye.soon.communityo2o.common.util.UserSharedPreferencesUtil;
import com.henanjianye.soon.communityo2o.interf.NetWorkCallback;
import com.henanjianye.soon.communityo2o.view.CustomSinnper;
import com.umeng.analytics.MobclickAgent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ServiceRepairConfirmActivity extends BaseActivity implements View.OnClickListener {
    private MyTitleBarHelper myTitleBarHelper;
    private TextView confirm_submit;
    private CustomSinnper spinner;
    private LinearLayout mGallery;
    //    private int[] mImgIds;
    private LayoutInflater mInflater;
    private RepairBean rBean;
    private TextView confirm_question;
    private TextView confirm_date_time;
    private TextView repiar_name;
    private TextView repair_phone;
    private TextView tv_address;
    private TextView confirm_type;
    private Context mContext;

    private List<String> img_urls = new ArrayList<>();
    private UserSharedPreferencesUtil userSharedPreferencesUtil;
    ImageLoader instance = ImageLoader.getInstance();
    DisplayImageOptions build = new DisplayImageOptions.Builder()
            .showImageOnFail(R.mipmap.gouwuche_image)
            .showImageForEmptyUri(R.mipmap.gouwuche_image).showImageOnLoading(R.mipmap.gouwuche_image).build();

    String[] strs = new String[]{"洛阳市洛阳高尔夫花园-一期 10号楼-二单元 2-301",
            "北京市海淀区首体南路38号A座三层2区", "北京市海淀区首体南路38号A座三层3区", "北京市海淀区首体南路38号A座三层4区"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_repair_confirm);
        mContext = this;
        initViews();
        initEvents();
        init();
    }

    private void initViews() {
        myTitleBarHelper = new MyTitleBarHelper(this, getWindow().getDecorView().findViewById(android.R.id.content));
        myTitleBarHelper.setLeftImgVisible(false);
        myTitleBarHelper.resetState();
        myTitleBarHelper.setMiddleText("确认订单");
        myTitleBarHelper.setRightImgVisible(false);
        myTitleBarHelper.setLeftImag(R.mipmap.btn_back);
        repiar_name = (TextView) findViewById(R.id.repiar_name);
        repair_phone = (TextView) findViewById(R.id.repair_phone);
        confirm_submit = (TextView) findViewById(R.id.confirm_submit);
        confirm_question = (TextView) findViewById(R.id.confrim_question);
        confirm_date_time = (TextView) findViewById(R.id.confirm_date_time);
        tv_address = (TextView) findViewById(R.id.tv_address);
        confirm_type = (TextView) findViewById(R.id.confirm_type);
        //自定义下拉列表 地址
        spinner = (CustomSinnper) findViewById(R.id.custom_sinnper);
        //横向滑动图片
        mGallery = (LinearLayout) findViewById(R.id.id_gallery);
        mInflater = LayoutInflater.from(this);
        Intent intent = getIntent();
        Serializable serializable = intent.getSerializableExtra(ServiceRepairActivity.REPAIR_INFO);
        if (serializable != null) {
            rBean = (RepairBean) serializable;
            img_urls.addAll(rBean.upload);
            confirm_question.setText(rBean.des);
            if (rBean.repairType.equals("1")) {
                confirm_type.setText("公共维修：");
            } else {
                confirm_type.setText("居家维修：");
            }

            confirm_date_time.setText("期望上门时段： " + rBean.visitDate);
        }
    }

    ;

    private void initEvents() {
        myTitleBarHelper.setOnclickListener(this);
        confirm_submit.setOnClickListener(this);
    }

    ;
    private BaseDataBean<UserBean> houseList;

    private void init() {

        String userInfo = new UserSharedPreferencesUtil().getUserJsonInfo(mContext);
        houseList = JsonUtil.parseDataObject(userInfo, UserBean.class);
        if (houseList.data != null) {
            UserBean uBean = houseList.data;
            repiar_name.setText(uBean.username);
            repair_phone.setText(uBean.mobilephone);
            tv_address.setVisibility(View.GONE);
            spinner.setVisibility(View.VISIBLE);
//            if (uBean.houseList != null && uBean.houseList.size() > 0) {
//                //多个地址
//                tv_address.setVisibility(View.GONE);
//                spinner.setVisibility(View.VISIBLE);
//            } else {
//                //只有一个地址
//                tv_address.setText(uBean.defaultAddress);
//            }
            //list转换成array
//            int size=list.size();
//            String[] array = (String[])list.toArray(new String[size]);

//            MyLog.e("FFF","houseList.data.houseList.get(0).fullAddress is "+houseList.data.houseList.get(0).fullAddress);
//            houseList.data.houseList.get(0).fullAddress;
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.custom_xml, R.id.online_user_list_item_textview, strs);

        spinner.setAdapter(adapter);
        spinner.setOnItemSeletedListener(new CustomSinnper.OnItemSeletedListener() {

            @Override
            public void onItemSeleted(AdapterView<?> parent, View view,
                                      int position, long id) {
                Object obj = parent.getItemAtPosition(position);
                Toast.makeText(getApplicationContext(), "" + obj,
                        Toast.LENGTH_SHORT).show();
            }
        });
        if (img_urls != null && img_urls.size() > 0) {
            //横向滑动图片
            for (int i = 0; i < img_urls.size(); i++) {
                View view = mInflater.inflate(R.layout.horizontal_imge, mGallery, false);
                ImageView img = (ImageView) view
                        .findViewById(R.id.horizontal_img);
                instance.displayImage(img_urls.get(i), img, build);
                mGallery.addView(view);
            }
        }


    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.iv_title_bar_left:
                this.finish();
                break;
            case R.id.confirm_submit:
                userSharedPreferencesUtil = new UserSharedPreferencesUtil();
                UserBean bean = userSharedPreferencesUtil.getUserInfo(ServiceRepairConfirmActivity.this);
                if (rBean != null) {
                    UserDataHelper userDataHelper = new UserDataHelper(getApplication());
                    List<String> photoUrls = new ArrayList<>();
                    if (rBean.upload != null && rBean.upload.size() > 0) {
                        for (int i = 0; i < rBean.upload.size(); i++) {
                            String photo = rBean.upload.get(i);
                            if (photo.contains("file:")) {
                                photo = photo.replace("file:", "");
                            }
                            photoUrls.add(photo);
                        }
                    }
                    rBean.houseId = bean.houseId;
                    rBean.userName = bean.username;
                    rBean.userMobile = bean.mobilephone;
//                    MyLog.e("KKK","rBean.repairType is "+rBean.repairType);
//                    MyLog.e("KKK","rBean.des is "+rBean.des);
//                    MyLog.e("KKK","rBean.visitDate is "+rBean.visitDate);
//                    MyLog.e("KKK","rBean.visitTime is "+rBean.visitTime);
//                    MyLog.e("KKK","rBean.houseId is "+rBean.houseId);
//                    MyLog.e("KKK","rBean.userName is "+rBean.userName);
//                    MyLog.e("KKK","rBean.userMobile is "+rBean.userMobile);
//                    MyLog.e("KKK","photoUrls is "+photoUrls.size());
//                    MyLog.e("KKK","bean.orgId is "+bean.orgId);
                    userDataHelper.Submit_repairInfo(getNetRequestHelper(ServiceRepairConfirmActivity.this), rBean.repairType, rBean.des,
                            rBean.visitDate, rBean.visitTime, rBean.houseId, rBean.userName, rBean.userMobile, photoUrls, bean.orgId);
                }
                break;

            default:
                break;
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
                toast("取消提交操作");
            }

            @Override
            public void onLoading(long total, long current, boolean isUploading, String requestTag) {
            }

            @Override
            public void onSuccess(ResponseInfo<String> responseInfo, String requestTag) {
                BaseDataBean<Object> baseDataBean = JsonUtil.parseDataObject(responseInfo.result, Object.class);
                if (baseDataBean.code == 100) {
                    toast("提交成功");
                    Intent intent = new Intent();
                    intent.putExtra(ServiceRepairActivity.REPAIR_BACK_INFO, rBean);
                    setResult(RESULT_OK, intent);
                    ServiceRepairConfirmActivity.this.finish();
                } else {
                    toast(baseDataBean.msg);
                }
            }

            @Override
            public void onFailure(HttpException error, String msg, String requestTag) {
                NetWorkStateUtils.errorNetMes(ServiceRepairConfirmActivity.this);
            }
        };
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            if (spinner.isShowPopup()) {
                spinner.dismiss();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
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
