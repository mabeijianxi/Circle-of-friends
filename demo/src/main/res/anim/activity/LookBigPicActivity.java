package anim.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.henanjianye.soon.communityo2o.R;
import com.henanjianye.soon.communityo2o.adapter.ImageScaleAdapter;
import com.henanjianye.soon.communityo2o.common.enties.EaluationListBean;
import com.henanjianye.soon.communityo2o.common.util.DisplayUtils;
import com.henanjianye.soon.communityo2o.view.HackyViewPager;
import com.henanjianye.soon.communityo2o.view.MaterialTextView;
import com.nineoldandroids.view.ViewHelper;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jian on 2016/1/5.
 */
public class LookBigPicActivity extends Activity implements View.OnClickListener, HackyViewPager.HackyViewPagerDispatchListener {
    private List<EaluationListBean.EaluationPicBean> picDataList;
    private List<View> dotList = new ArrayList<>();
    public static String PICDATALIST = "PICDATALIST";
    public static String CURRENTITEM = "CURRENTITEM";
    private int currentItem;
    public int mPositon;
    private ImageScaleAdapter imageScaleAdapter;
    private HackyViewPager viewPager;
    private LinearLayout ll_dots;
    private TextView tv_back;
    private TextView tv_pager;
    private LinearLayout ll_bottom;
    private MaterialTextView bt_left;
    private MaterialTextView bt_right;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_look_big_pic);
        getData();
        intiView();
        setUpEvent();
        initDot(currentItem);
    }

    private void setUpEvent() {
        bt_left.setOnClickListener(this);
        bt_right.setOnClickListener(this);
        viewPager.setmHackyViewPagerDispatchListener(this);
        viewPager.setAdapter(imageScaleAdapter);
        viewPager.setCurrentItem(currentItem);
        setTitleNum(currentItem);
        tv_back.setOnClickListener(this);
        setPagerChangeListener(viewPager);
    }

    private void getData() {
        Intent intent = getIntent();
        picDataList = (List<EaluationListBean.EaluationPicBean>) intent.getSerializableExtra("PICDATALIST");
        currentItem = intent.getIntExtra(CURRENTITEM, 0);
        mPositon = currentItem;
        imageScaleAdapter = new ImageScaleAdapter(this, picDataList);
    }

    private void intiView() {
        ll_dots = (LinearLayout) findViewById(R.id.ll_dots);
        tv_back = (TextView) findViewById(R.id.tv_back);
        tv_pager = (TextView) findViewById(R.id.tv_pager);
        viewPager = (HackyViewPager) findViewById(R.id.viewpager);
        ll_bottom = (LinearLayout) findViewById(R.id.ll_bottom);
        bt_left = (MaterialTextView) findViewById(R.id.bt_left);
        bt_right = (MaterialTextView) findViewById(R.id.bt_right);
    }

    private void setPagerChangeListener(HackyViewPager viewPager) {
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mPositon = position;
                setTitleNum(position);
                initDot(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void setTitleNum(int position) {
        tv_pager.setText((position + 1) + "/" + picDataList.size());
    }

    /**
     * 初始化轮播图点
     */
    private void initDot(int index) {
        // 清空点所在集合
        dotList.clear();
        ll_dots.removeAllViews();
        for (int i = 0; i < picDataList.size(); i++) {
            ImageView view = new ImageView(this);
            if (i == index || picDataList.size() == 1) {
                view.setBackgroundResource(R.mipmap.type_selected);
            } else {
                view.setBackgroundResource(R.mipmap.type_normal);
            }
            // 指定点的大小
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    DisplayUtils.dip2px(this, 5), DisplayUtils.dip2px(this, 5));
            // 指定点的间距
            layoutParams.setMargins(DisplayUtils.dip2px(this, 2), 0, DisplayUtils.dip2px(this, 2), 0);
            // 添加到线性布局中
            ll_dots.addView(view, layoutParams);
            // 添加到集合中去
            dotList.add(view);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_back:
                finish();
                overridePendingTransition(R.anim.pic2activity_in,R.anim.pic2activity_out);

                break;
            case R.id.bt_left:
                View primaryView = imageScaleAdapter.getPrimaryItem();
                if (primaryView != null) {
                    float rotation = ViewHelper.getRotation(primaryView);
                    ViewHelper.setRotation(primaryView, rotation + 90.0f);
                    primaryView.requestLayout();
                }
                break;
            case R.id.bt_right:
                View primaryView1 = imageScaleAdapter.getPrimaryItem();
                if (primaryView1 != null) {
                    float rotation = ViewHelper.getRotation(primaryView1);
                    ViewHelper.setRotation(primaryView1, rotation - 90.0f);
                    primaryView1.requestLayout();
                }
                break;
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

    @Override
    public void isDown() {
        ll_bottom.setVisibility(View.GONE);
    }

    @Override
    public void isUp() {
        ll_bottom.setVisibility(View.VISIBLE);
    }

    @Override
    public void isCancel() {
        ll_bottom.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.pic2activity_in, R.anim.pic2activity_out);
    }
}
