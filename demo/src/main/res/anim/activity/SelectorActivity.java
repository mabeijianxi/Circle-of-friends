package anim.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.henanjianye.soon.communityo2o.R;
import com.henanjianye.soon.communityo2o.adapter.CountryAdapter;
import com.henanjianye.soon.communityo2o.timeSelector.cascade.widget.OnWheelChangedListener;
import com.henanjianye.soon.communityo2o.timeSelector.cascade.widget.WheelView;
import com.umeng.analytics.MobclickAgent;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class SelectorActivity extends BaseSelectorActivity implements OnClickListener, OnWheelChangedListener {
    private WheelView mViewProvince;
    private WheelView mViewCity;
    private WheelView mViewDistrict;
    private WheelView mViewTime;
    private TextView selected_over;
    private Button mBtnConfirm;
    String[] array = {"09:30-10:30", "10:30-11:30", "11:30-12:30", "12:30-13:30", "13:30-14:30", "14:30-15:30", "15:30-16:30", "16:30-17:30", "17:30-18:30"};
    List<String> list = Arrays.asList(array);
    private int year;
    private int month;
    private int day;
    private int hour;
    private int minitue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selector);
        Window window = getWindow();
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        //设置窗口的大小及透明度
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = layoutParams.WRAP_CONTENT;
//		layoutParams.alpha = 0.5f;
        window.setAttributes(layoutParams);
        Calendar c = Calendar.getInstance(Locale.CHINA);
        year = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH) + 1;
        day = c.get(Calendar.DAY_OF_MONTH);
        hour = c.get(Calendar.HOUR_OF_DAY);
        minitue = c.get(Calendar.MINUTE);
        setUpViews();
        setUpListener();
        setUpData();
    }

    private void setUpViews() {
        mViewProvince = (WheelView) findViewById(R.id.id_province);
        mViewCity = (WheelView) findViewById(R.id.id_city);
        mViewDistrict = (WheelView) findViewById(R.id.id_district);
        mViewTime = (WheelView) findViewById(R.id.id_time);
        mBtnConfirm = (Button) findViewById(R.id.btn_confirm);
        selected_over = (TextView) findViewById(R.id.selected_over);
    }

    private void setUpListener() {
        mViewProvince.addChangingListener(this);
        mViewCity.addChangingListener(this);
        mViewDistrict.addChangingListener(this);
        mViewTime.addChangingListener(this);
        mBtnConfirm.setOnClickListener(this);
        selected_over.setOnClickListener(this);
    }

    private void setUpData() {
        initProvinceDatas();
        mViewProvince.setViewAdapter(new CountryAdapter(SelectorActivity.this, mProvinceDatas));
        if (year == 2015) {
            mViewProvince.setCurrentItem(0, false);
        } else if (year == 2016) {
            mViewProvince.setCurrentItem(1, false);
        } else if (year == 2017) {
            mViewProvince.setCurrentItem(2, false);
        }
        mViewTime.setViewAdapter(new CountryAdapter(SelectorActivity.this, array));
        if (hour < 9) {
            mViewTime.setCurrentItem(0, false);
        } else if ((hour == 9 && minitue >= 30) || (hour == 10 && minitue < 30)) {
            mViewTime.setCurrentItem(1, false);
        } else if ((hour == 10 && minitue >= 30) || (hour == 11 && minitue < 30)) {
            mViewTime.setCurrentItem(2, false);
        } else if ((hour == 11 && minitue >= 30) || (hour == 12 && minitue < 30)) {
            mViewTime.setCurrentItem(3, false);
        } else if ((hour == 12 && minitue >= 30) || (hour == 13 && minitue < 30)) {
            mViewTime.setCurrentItem(4, false);
        } else if ((hour == 13 && minitue >= 30) || (hour == 14 && minitue < 30)) {
            mViewTime.setCurrentItem(5, false);
        } else if ((hour == 14 && minitue >= 30) || (hour == 15 && minitue < 30)) {
            mViewTime.setCurrentItem(6, false);
        } else if ((hour == 15 && minitue >= 30) || (hour == 16 && minitue < 30)) {
            mViewTime.setCurrentItem(7, false);
        } else if ((hour == 16 && minitue >= 30)|| (hour == 17 && minitue < 30)) {
            mViewTime.setCurrentItem(8, false);
        }
        mViewProvince.setVisibleItems(7);
        mViewCity.setVisibleItems(7);
        mViewDistrict.setVisibleItems(7);
        mViewTime.setVisibleItems(7);
        updateCities();
        updateAreasFirst();
        updateTime();
    }

    @Override
    public void onChanged(WheelView wheel, int oldValue, int newValue) {
        // TODO Auto-generated method stub
        if (wheel == mViewProvince) {
            updateCities();
        } else if (wheel == mViewCity) {
            updateAreas();
        } else if (wheel == mViewDistrict) {
            mCurrentDistrictName = mDistrictDatasMap.get(mCurrentCityName)[newValue];
        } else if (wheel == mViewTime) {
            updateTime();
        }
    }

    private void updateAreasFirst() {
        int pCurrent = mViewCity.getCurrentItem();
        mCurrentCityName = mCitisDatasMap.get(mCurrentProviceName)[pCurrent];
        String[] areas = mDistrictDatasMap.get(mCurrentCityName);

        if (areas == null) {
            areas = new String[]{""};
        }
        mViewDistrict.setViewAdapter(new CountryAdapter(this, areas));
        int pos = 0;
        for (int i = 0; i < areas.length; i++) {
            if (Integer.parseInt(areas[i]) == day) {
                pos = i;
                break;
            }
        }
        if ((hour == 17 && minitue >= 30)||(hour >= 18)) {
            mViewDistrict.setCurrentItem(pos+1);
        }else {
            mViewDistrict.setCurrentItem(pos);
        }

    }

    private void updateAreas() {
        int pCurrent = mViewCity.getCurrentItem();
        mCurrentCityName = mCitisDatasMap.get(mCurrentProviceName)[pCurrent];
        String[] areas = mDistrictDatasMap.get(mCurrentCityName);

        if (areas == null) {
            areas = new String[]{""};
        }
        if (Integer.parseInt(mCurrentCityName) == month) {
            isFirstLoadDay = true;
        }
        mViewDistrict.setViewAdapter(new CountryAdapter(this, areas));
        if (isFirstLoadDay) {
            int pos = 0;
            for (int i = 0; i < areas.length; i++) {
                if (Integer.parseInt(areas[i]) == day) {
                    pos = i;
                    break;
                }
            }
            mViewDistrict.setCurrentItem(pos);
            isFirstLoadDay = false;
        } else {
            mViewDistrict.setCurrentItem(0);
        }
    }

    private boolean isFirstLoadMonth = true;
    private boolean isFirstLoadDay = true;

    private void updateCities() {
        int pCurrent = mViewProvince.getCurrentItem();
        mCurrentProviceName = mProvinceDatas[pCurrent];
        String[] cities = mCitisDatasMap.get(mCurrentProviceName);
        if (cities == null) {
            cities = new String[]{""};
        }
        if (Integer.parseInt(mCurrentProviceName) == year) {
            isFirstLoadMonth = true;
        }
        mViewCity.setViewAdapter(new CountryAdapter(this, cities));
        if (isFirstLoadMonth) {
            int pos = 0;
            for (int i = 0; i < cities.length; i++) {
                if (Integer.parseInt(cities[i]) == month) {
                    pos = i;
                    break;
                }
            }
            mViewCity.setCurrentItem(pos);
            isFirstLoadMonth = false;
        } else {
            mViewCity.setCurrentItem(0);
        }
        updateAreas();
    }

    private void updateTime() {
        int pCurrent = mViewTime.getCurrentItem();
        mCurrentTime = array[pCurrent];
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_confirm:
//                showSelectedResult();
                break;
            case R.id.selected_over:
                showSelectedResult();
                break;
            default:
                break;
        }
    }

    private boolean checkDate() {
        if (Integer.parseInt(mCurrentProviceName) > year) {
            return true;
        } else if (Integer.parseInt(mCurrentProviceName) == year) {
            if (Integer.parseInt(mCurrentCityName) > month) {
                return true;
            } else if (Integer.parseInt(mCurrentCityName) == month) {
                if (Integer.parseInt(mCurrentDistrictName) > day) {
                    return true;
                } else if (Integer.parseInt(mCurrentDistrictName) == day) {
                    //在判断下当前时间 若当前时间大于选定时间 则返回false
                    if (hour > Integer.parseInt(mCurrentTime.substring(0, 2))) {
                        return false;
                    }
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private void showSelectedResult() {
        if (!checkDate()) {
            Toast.makeText(SelectorActivity.this, "期望时间不能小于当前时间", Toast.LENGTH_SHORT).show();
            return;
        }
        String ServiceTime = mCurrentProviceName + "-" + mCurrentCityName + "-" + mCurrentDistrictName + " " + mCurrentTime;
        Intent intent = new Intent();
        intent.putExtra(ServiceRepairActivity.REPAIR_CONFIRM_TIME, ServiceTime);
        setResult(RESULT_OK, intent);
        finish();
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
