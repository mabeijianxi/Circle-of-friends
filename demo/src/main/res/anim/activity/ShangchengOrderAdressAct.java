package anim.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.asyey.footballlibrary.network.exception.HttpException;
import com.asyey.footballlibrary.network.http.ResponseInfo;
import com.henanjianye.soon.communityo2o.BaseActivity;
import com.henanjianye.soon.communityo2o.R;
import com.henanjianye.soon.communityo2o.adapter.CountryCityZoneAdapter;
import com.henanjianye.soon.communityo2o.common.Constant;
import com.henanjianye.soon.communityo2o.common.OrderDataHelper;
import com.henanjianye.soon.communityo2o.common.enties.AppConsigneeInfor;
import com.henanjianye.soon.communityo2o.common.enties.AppCountryCityZoneBean;
import com.henanjianye.soon.communityo2o.common.enties.BaseBean;
import com.henanjianye.soon.communityo2o.common.enties.BaseDataBean;
import com.henanjianye.soon.communityo2o.common.enties.JsonUtil;
import com.henanjianye.soon.communityo2o.common.util.MyTitleBarHelper;
import com.henanjianye.soon.communityo2o.common.util.NetWorkStateUtils;
import com.henanjianye.soon.communityo2o.common.util.SharedPreferencesUtil;
import com.henanjianye.soon.communityo2o.common.util.UserSharedPreferencesUtil;
import com.henanjianye.soon.communityo2o.common.view.CustomProgressDialog;
import com.henanjianye.soon.communityo2o.interf.IAppCountry;
import com.henanjianye.soon.communityo2o.interf.NetWorkCallback;
import com.henanjianye.soon.communityo2o.timeSelector.cascade.widget.OnWheelChangedListener;
import com.henanjianye.soon.communityo2o.timeSelector.cascade.widget.WheelView;
import com.umeng.analytics.MobclickAgent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ShangchengOrderAdressAct extends BaseActivity implements
        OnWheelChangedListener {
    // /**
    // * 所有省
    // */
    // private List<String> mProvinceDatas;

    /**
     * 当前省的名称
     */
    private String mCurrentProviceName;
    /**
     * 当前市的名称
     */
    private String mCurrentCityName;
    /**
     * 当前区的名称
     */
    private String mCurrentDistrictName = "";

    /**
     * 当前区的邮政编码
     */
    private int mcountryId = -1;
    public static final int RESULTCODEOK = 0x002;
    public static final int RESULTCODECANCEL = 0x003;
    public static final String ConsigneeINFORADRESS = "consigneereaponsbean";
    // public static final String CONSIGNEEREAPONSBEAN = "consigneereaponsbean";
    private Intent intent;
    private CustomProgressDialog createDialog;
    private LinearLayout ll_zone;
    private TextView tx_zone;
    private EditText name;
    private EditText phone;
    private EditText detailaddress;
    private AppConsigneeInfor comeInfor;
    private WheelView mViewProvince;
    private WheelView mViewCity;
    private WheelView mViewDistrict;
    private boolean isContryInforRequestOK;
    private List<AppCountryCityZoneBean> mProvinceDatas;
    private List<AppCountryCityZoneBean.City.Zone> mDistrictDatasMap;
    private List<AppCountryCityZoneBean.City> cities;
    private RelativeLayout chooseZone;
    private boolean contryInforRequestF;
    private CustomProgressDialog createDialogCuntry;
    private Button countryConform;
    private String defaultProvince = "河南省";
    private OrderDataHelper orderDataHelper;
    private MyTitleBarHelper myTitleBarHelper;
    private CountryCityZoneAdapter countryAdapter;
    private CountryCityZoneAdapter citiesAdapter;
    private CountryCityZoneAdapter mDistrictAdapter;

    @Override
    public int mysetContentView() {
        return R.layout.abc_order_address;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        getCountryInfor();
        processLogic();
    }

    public void initView() {
        View findViewById = findViewById(R.id.button1);
        findViewById.setOnClickListener(this);
        name = (EditText) findViewById(R.id.et_consignee_name);
        phone = (EditText) findViewById(R.id.et_consignee_phone);
        tx_zone = (TextView) findViewById(R.id.tx_consignee_zone);
        ll_zone = (LinearLayout) findViewById(R.id.ll_consignee_zone);
        detailaddress = (EditText) findViewById(R.id.et_consignee_detailaddress);
        mViewProvince = (WheelView) findViewById(R.id.address_country);
        mViewCity = (WheelView) findViewById(R.id.address_city);
        mViewDistrict = (WheelView) findViewById(R.id.address_zone);
        chooseZone = (RelativeLayout) findViewById(R.id.rl_address_choose);
        countryConform = (Button) findViewById(R.id.bt_queding);
        myTitleBarHelper = new MyTitleBarHelper(this, getWindow().getDecorView().getRootView());
        myTitleBarHelper.resetState();
        myTitleBarHelper.setMiddleText("送货地址");
        myTitleBarHelper.setRightText("完成");
        myTitleBarHelper.setOnclickListener(this);
        ll_zone.setOnClickListener(this);
        mViewProvince.addChangingListener(this);
        // 添加change事件
        mViewCity.addChangingListener(this);
        // 添加change事件
        mViewDistrict.addChangingListener(this);
        countryConform.setOnClickListener(this);

    }

    private void getCountryInfor() {
        //TODO 加载缓存
        String cach = SharedPreferencesUtil.getStringData(this, Constant.CachTag.APP_COMMDITY_PROVINCE_TREE, "");
        startParseProvinceDate(cach, true);
        if (orderDataHelper == null) {
            orderDataHelper = new OrderDataHelper(this);
        }
        orderDataHelper.getAddressProvince(getNetRequestHelper(this).isShowProgressDialog(false));
    }

    public void processLogic() {
        intent = getIntent();
        String action = intent.getAction();
        // if (ShangchengOrderDetailAct.ADDRESS_CREAT.equals(action)) {
        // }
        if (CommoditySettlementActivity.ADDRESS_UPDATE.equals(action)) {
            Serializable serializableExtra = intent
                    .getSerializableExtra(CommoditySettlementActivity.CONSIGNEEINFOR);
            if (serializableExtra instanceof AppConsigneeInfor) {
                comeInfor = (AppConsigneeInfor) serializableExtra;
                if (comeInfor == null) {
                    comeInfor = new AppConsigneeInfor();
                }
                setViewDate();
            }
        }
    }

    private void setWheelUpData(List<AppCountryCityZoneBean> mProvinceDatas2) {
        // initProvinceDatas();
        // mProvinceDatas = firstDatas;
        // mCitisDatasMap = secondDatas;
        // mDistrictDatasMap = thirdDatas;
//        if (countryAdapter == null) {

        countryAdapter = new CountryCityZoneAdapter(this);
        countryAdapter.setDatas(mProvinceDatas2);
        mViewProvince.setViewAdapter(countryAdapter);
//        } else {
//            countryAdapter.setDatas(mProvinceDatas2);
//        }
        // 设置可见条目数量
        mViewProvince.setVisibleItems(7);

        mViewCity.setVisibleItems(7);
        mViewDistrict.setVisibleItems(7);
        updateCities();
        updateAreas();
    }

    private void setCurrentPosition(String province, String city, String zone) {
        if (province == null) {
            province = defaultProvince;
        }
        int posPro = getCityZonePosition(mProvinceDatas, province);
//		MyLog.e("AAA", "posPro--addddd-" + posPro);
        mViewProvince.setCurrentItem(posPro);
        int posCity = getCityZonePosition(cities, city);
        mViewCity.setCurrentItem(posCity);
        int posZone = getCityZonePosition(mDistrictDatasMap, zone);
        mViewDistrict.setCurrentItem(posZone);

    }

    // private int getProvincePosition(
    // List<AppCountryCityZoneBean> mProvinceDatas2, String name) {
    //
    // List<String> aa = new ArrayList<String>();
    // for (int i = 0; i < mProvinceDatas2.size(); i++) {
    // aa.add(mProvinceDatas2.get(i).areaName);
    // }
    //
    // return aa.indexOf(name);
    // }

    private int getCityZonePosition(List<?> data, String name) {

        List<String> aa = new ArrayList<String>();
        for (int i = 0; i < data.size(); i++) {
            aa.add(((IAppCountry) data.get(i)).getName());
        }
        return aa.indexOf(name) == -1 ? 0 : aa.indexOf(name);
    }

    /**
     * 根据当前的市，更新区WheelView的信息
     */
    private void updateAreas() {
        int pCurrent = mViewCity.getCurrentItem();
        mCurrentCityName = cities.get(pCurrent).areaName;
        mDistrictDatasMap = cities.get(pCurrent).children;

        if (mDistrictDatasMap == null) {
            mDistrictDatasMap = new ArrayList<AppCountryCityZoneBean.City.Zone>();
        }
//        if (mDistrictAdapter == null) {
        mDistrictAdapter = new CountryCityZoneAdapter(this);
        mDistrictAdapter.setDatas(mDistrictDatasMap);
        mViewDistrict.setViewAdapter(mDistrictAdapter);
//        } else {
//            mDistrictAdapter.setDatas(mDistrictDatasMap);
//        }
        mViewDistrict.setCurrentItem(0);
        int pZone = mViewDistrict.getCurrentItem();
        if (mDistrictDatasMap.size() > pZone) {
            mCurrentDistrictName = mDistrictDatasMap.get(pZone).areaName;
            mcountryId = mDistrictDatasMap.get(pZone).areaId;
        }

    }

    /**
     * 根据当前的省，更新市WheelView的信息
     */
    private void updateCities() {
        int pCurrent = mViewProvince.getCurrentItem();
        if (mProvinceDatas.size() > pCurrent) {

            mCurrentProviceName = mProvinceDatas.get(pCurrent).areaName;
            cities = mProvinceDatas.get(pCurrent).children;
        }
        if (cities == null) {
            cities = new ArrayList<AppCountryCityZoneBean.City>();
        }
//        if (citiesAdapter == null) {
        citiesAdapter = new CountryCityZoneAdapter(this);
        citiesAdapter.setDatas(cities);
        mViewCity.setViewAdapter(citiesAdapter);
//        } else {
//            citiesAdapter.setDatas(cities);
//        }
        mViewCity.setCurrentItem(0);
        updateAreas();
    }

    private void setViewDate() {
        name.setText(comeInfor.getAddress().receiverName);
        phone.setText(comeInfor.getAddress().mobile);
        detailaddress.setText(comeInfor.getAddress().detailAddress);
        tx_zone.setText(comeInfor.getAddress().provinceName + " "
                + comeInfor.getAddress().cityName + " "
                + comeInfor.getAddress().countyName);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button1:

                saveAddress();

                break;
            case R.id.iv_title_bar_left:
                finish();
                break;
            case R.id.tv_title_bar_right:
                saveAddress();
                break;
            case R.id.ll_consignee_zone:
                startChooseAddress();
                break;
            case R.id.bt_queding:
                conformCountryInfor();
                break;

            default:
                break;
        }
    }

    private void conformCountryInfor() {
        // TODO
        if (comeInfor == null) {
            comeInfor = new AppConsigneeInfor();
        }
        if (mcountryId != -1) {

            comeInfor.getAddress().countyId = mcountryId;
        } else {

            toast("请选择收货区域");
            return;
        }
        if (!TextUtils.isEmpty(mCurrentCityName)) {

            comeInfor.getAddress().cityName = mCurrentCityName;
        } else {
            toast("请选择收货城市");
            return;
        }
        if (!TextUtils.isEmpty(mCurrentDistrictName)) {

            comeInfor.getAddress().countyName = mCurrentDistrictName;
        } else {
            toast("请选择收货区、县");
            return;
        }
        if (!TextUtils.isEmpty(mCurrentProviceName)) {

            comeInfor.getAddress().provinceName = mCurrentProviceName;
        } else {
            toast("请选择收货省，直辖市");
            return;
        }

        tx_zone.setText(mCurrentProviceName + " " + mCurrentCityName + " "
                + mCurrentDistrictName);
        chooseZone.setVisibility(View.INVISIBLE);

    }

    /**
     * 显示选择区县对话框
     */
    private void startChooseAddress() {
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(phone.getWindowToken(), 0);
            imm.hideSoftInputFromWindow(name.getWindowToken(), 0);
            imm.hideSoftInputFromWindow(detailaddress.getWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        createDialogCuntry = CustomProgressDialog.createDialog(this);
        createDialogCuntry.show();
        if (isContryInforRequestOK) {
            createDialogCuntry.cancel();
            chooseZone.setVisibility(View.VISIBLE);
        } else if (contryInforRequestF) {
            getCountryInfor();
            // startChooseAddress();
        }
    }

    private boolean isAvalible(EditText et) {
        if (et.getText() == null) {
            return false;
        }
        if (et.getText().toString().trim().length() > 0) {
            return true;
        }
        return false;
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

                if (requestTag.equals(Constant.MallUrl.COMMODITY_PROVINCE_CITY_)) {
                    startParseProvinceDate(responseInfo.result, false);
                    // MyLog.e("AAA", "responseInfo.result----" + responseInfo.result);


                }
                if (requestTag.equals(Constant.MallUrl.COMMODITY_ADDRESS_SAVE)) {
                    createDialog.dismiss();

                    BaseDataBean<AppConsigneeInfor> parseDataObject = JsonUtil
                            .parseDataObject(responseInfo.result,
                                    AppConsigneeInfor.class);
                    if (100 == parseDataObject.code) {
                        AppConsigneeInfor appConsigneeInfor = parseDataObject.data;
                        comeInfor.addressId = appConsigneeInfor.addressId;
                        comeInfor.getAddress().addressId = appConsigneeInfor.addressId;
                        comeInfor.transfee = appConsigneeInfor.transfee;
                        comeInfor.transfeeShow = appConsigneeInfor.transfeeShow;
                        Intent intent2 = getIntent();
                        intent2.putExtra(ConsigneeINFORADRESS, comeInfor);
                        // intent2.putExtra(CONSIGNEEREAPONSBEAN, parseDataObject.data);
                        setResult(RESULTCODEOK, getIntent());
                        finish();
                    } else {
                        Toast.makeText(ShangchengOrderAdressAct.this, parseDataObject.msg, Toast.LENGTH_SHORT)
                                .show();
                    }

                }
            }

            @Override
            public void onFailure(HttpException error, String msg, String requestTag) {
                NetWorkStateUtils.errorNetMes(ShangchengOrderAdressAct.this);
            }
        };
    }

    /**
     * @param b 是否是缓存
     */
    private void startParseProvinceDate(String result, boolean b) {
        BaseBean<AppCountryCityZoneBean> parseDataObject = JsonUtil
                .jsonArray(result,
                        AppCountryCityZoneBean.class);
        if (100 == parseDataObject.code) {

            if (!b) {
                SharedPreferencesUtil.saveStringData(ShangchengOrderAdressAct.this, Constant.CachTag.APP_COMMDITY_PROVINCE_TREE, result);
            }
            if (!chooseZone.isShown()) {
                isContryInforRequestOK = true;
                mProvinceDatas = parseDataObject.data;
                setWheelUpData(mProvinceDatas);
                if (comeInfor == null) {
                    comeInfor = new AppConsigneeInfor();
                }
                setCurrentPosition(comeInfor.getAddress().provinceName,
                        comeInfor.getAddress().cityName,
                        comeInfor.getAddress().countyName);
            }
            if (createDialogCuntry != null) {
                createDialogCuntry.cancel();
                chooseZone.setVisibility(View.VISIBLE);
            }
        } else {
            contryInforRequestF = true;
        }
    }


    private void saveAddress() {
        //TODO 判断是否已经登录
        if (UserSharedPreferencesUtil.getUserLoginState(getApplicationContext()) > 0) {
            if (comeInfor == null) {
                comeInfor = new AppConsigneeInfor();
            }
            HashMap<String, Object> requestMap = new HashMap<String, Object>();

            if (isAvalible(name)) {
                requestMap.put("receiverName", name.getText().toString());
                comeInfor.getAddress().receiverName = name.getText().toString();
            } else {
                Toast.makeText(this, "名字不能为空", Toast.LENGTH_SHORT).show();
                return;
            }
            if (name.getText().toString().length() > 8) {
                Toast.makeText(this, "用户名不能多于八个字", Toast.LENGTH_SHORT).show();
                return;
            }
            if (isAvalible(phone) && isPhoneNum(phone.getText().toString())) {
                requestMap.put("mobile", phone.getText().toString());
                comeInfor.getAddress().mobile = phone.getText().toString();
            } else {
                Toast.makeText(this, "电话号码不合法", Toast.LENGTH_SHORT).show();
                return;
            }
            if (isAvalible(detailaddress)) {
                requestMap.put("detailAddress", detailaddress.getText()
                        .toString());
                comeInfor.getAddress().detailAddress = detailaddress.getText()
                        .toString();
            } else {
                Toast.makeText(this, "地址不能为空", Toast.LENGTH_SHORT).show();
                return;
            }
            if (comeInfor.getAddress().countyId != -1) {
                requestMap.put("countyId", comeInfor.getAddress().countyId);
            }


            if (comeInfor.addressId != -1) {

                requestMap.put("addressId", comeInfor.addressId);
            }

            createDialog = CustomProgressDialog.createDialog(this);
            createDialog.show();
//			MyLog.e("AAA",
//					" comeInfor.getAddress().countyId---"
//							+ comeInfor.getAddress().countyId + "---goodsid--"
//							+ goodsid + "--- comeInfor.addressId"
//							+ comeInfor.addressId);
            orderDataHelper.saveAddressInfor(getNetRequestHelper(this).isShowProgressDialog(true), requestMap);
        } else if (UserSharedPreferencesUtil.getUserLoginState(getApplicationContext()) == 0) {
            //TODO 跳转选择小区界面
            toast("未选择小区");
//			Intent intent2 = new Intent(this, FirstActivity.class);
//			intent2.putExtra(FirstActivity.loginFlgStr, 1);
//			startActivity(intent2);
        } else {
            //TODO 跳转至登录界面
            toast("请登录后再进行购买");
        }
    }

    private boolean isPhoneNum(String str) {
        return str.matches("^1[9|7|6|2|3|4|5|8][0-9]\\d{8}$");
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    @Override
    public void onChanged(WheelView wheel, int oldValue, int newValue) {
        // TODO Auto-generated method stub
        if (wheel == mViewProvince) {
            updateCities();
        } else if (wheel == mViewCity) {
            updateAreas();
        } else if (wheel == mViewDistrict) {
            mCurrentDistrictName = mDistrictDatasMap.get(newValue).areaName;
            mcountryId = mDistrictDatasMap.get(newValue).areaId;
        }

    }
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(this.getClass().getSimpleName()); //统计页面(仅有Activity的应用中SDK自动调用，不需要单独写)
        MobclickAgent.onResume(this);          //统计时长
        if (createDialog != null && createDialog.isShowing()) {
            createDialog.dismiss();
        }
    }
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(this.getClass().getSimpleName()); // （仅有Activity的应用中SDK自动调用，不需要单独写）保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息
        MobclickAgent.onPause(this);
    }
}
