package anim.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.AbsoluteSizeSpan;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.henanjianye.soon.communityo2o.BaseActivity;
import com.henanjianye.soon.communityo2o.R;
import com.henanjianye.soon.communityo2o.common.util.MyTitleBarHelper;
import com.henanjianye.soon.communityo2o.fragment.ealuation.EaluationFragment;

/**
 * Created by jian on 2015/12/31.
 */
public class EaluationActivity extends BaseActivity {

    public static final int EALUATION_ALL = 0;
    public static final int EALUATION_GOOD = 1;
    public static final int EALUATION_MID = 2;
    public static final int EALUATION_BAD = 3;
    public static final String GOODSID = "GOODSID";
    public static final String GOODSTYPE = "GOODSTYPE";
    /**
     *
     */
    private int mIndex = EALUATION_ALL;
    private FrameLayout fl_content;
    private TextView btn_comments_bad;
    private TextView btn_comments_all;
    private TextView btn_comments_good;
    private TextView btn_comments_mid;
    private MyTitleBarHelper myTitleBarHelper;
    private ImageView iv_title_bar_left;
    /**
     * 商品id
     */
    private int goodsId;
    private EaluationFragment ealuationFragmentAll;
    private EaluationFragment ealuationFragmentGood;
    private EaluationFragment ealuationFragmentMid;
    private EaluationFragment ealuationFragmentBad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getGoodsId();
        initView();
        initEvent();
        onClick(btn_comments_all);
    }

    private void getGoodsId() {
        goodsId = getIntent().getIntExtra("goodsId", -1);
    }

    private void initEvent() {
        btn_comments_bad.setOnClickListener(this);
        btn_comments_all.setOnClickListener(this);
        btn_comments_good.setOnClickListener(this);
        btn_comments_mid.setOnClickListener(this);
        iv_title_bar_left.setOnClickListener(this);
    }

    private void initView() {
        myTitleBarHelper = new MyTitleBarHelper(this, getWindow().getDecorView().findViewById(android.R.id.content));
        myTitleBarHelper.resetState();
        myTitleBarHelper.setLeftImgVisible(true);
        myTitleBarHelper.setMiddleText("全部评论");
        myTitleBarHelper.setRightImgVisible(false);
        myTitleBarHelper.setLeftImag(R.mipmap.btn_back);

        fl_content = (FrameLayout) findViewById(R.id.fl_content);
        btn_comments_bad = (TextView) findViewById(R.id.btn_comments_bad);
        btn_comments_all = (TextView) findViewById(R.id.btn_comments_all);
        btn_comments_good = (TextView) findViewById(R.id.btn_comments_good);
        btn_comments_mid = (TextView) findViewById(R.id.btn_comments_mid);
        iv_title_bar_left = (ImageView) findViewById(R.id.iv_title_bar_left);
        setEalutionNum(0,0,0,0);
    }

    @Override
    public void onClick(View v) {
        initViewStatus();
        switch (v.getId()) {
            case R.id.btn_comments_bad:
                mIndex = EALUATION_BAD;
                btn_comments_bad.setBackgroundResource(R.color.repair_line_color);
                btn_comments_bad.setTextColor(0xffffffff);
                myTitleBarHelper.setMiddleText("差评");
                break;
            case R.id.btn_comments_all:
                mIndex = EALUATION_ALL;
                btn_comments_all.setBackgroundResource(R.color.repair_line_color);
                btn_comments_all.setTextColor(0xffffffff);
                myTitleBarHelper.setMiddleText("全部评论");

                break;
            case R.id.btn_comments_good:
                mIndex = EALUATION_GOOD;
                btn_comments_good.setBackgroundResource(R.color.repair_line_color);
                btn_comments_good.setTextColor(0xffffffff);
                myTitleBarHelper.setMiddleText("好评");
                break;
            case R.id.btn_comments_mid:
                mIndex = EALUATION_MID;
                btn_comments_mid.setBackgroundResource(R.color.repair_line_color);
                btn_comments_mid.setTextColor(0xffffffff);
                myTitleBarHelper.setMiddleText("中评");
                break;
            case R.id.iv_title_bar_left:
                finish();
                break;
        }
        Fragment framFragment = (Fragment) fragmentPagerAdapter.instantiateItem(fl_content, mIndex);
        fragmentPagerAdapter.setPrimaryItem(fl_content, mIndex, framFragment);
        fragmentPagerAdapter.finishUpdate(fl_content);
    }

    /**
     * 颜色状态初始化
     */
    private void initViewStatus() {
        btn_comments_bad.setBackgroundResource(android.R.color.white);
        btn_comments_all.setBackgroundResource(android.R.color.white);
        btn_comments_good.setBackgroundResource(android.R.color.white);
        btn_comments_mid.setBackgroundResource(android.R.color.white);
        btn_comments_bad.setTextColor(0xff8e8e8e);
        btn_comments_all.setTextColor(0xff8e8e8e);
        btn_comments_good.setTextColor(0xff8e8e8e);
        btn_comments_mid.setTextColor(0xff8e8e8e);
    }

    @Override
    public int mysetContentView() {
        return R.layout.activity_comments;
    }

    FragmentPagerAdapter fragmentPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public Fragment getItem(int position) {
            Bundle bundle = new Bundle();
            bundle.putInt(GOODSID, goodsId);
            switch (position) {
                case EALUATION_ALL:
                    bundle.putInt(GOODSTYPE, EALUATION_ALL);
                    if (ealuationFragmentAll == null) {
                        ealuationFragmentAll = new EaluationFragment();
                        ealuationFragmentAll.setArguments(bundle);
                    }
                    return ealuationFragmentAll;
                case EALUATION_GOOD:
                    if (ealuationFragmentGood == null) {
                        bundle.putInt(GOODSTYPE, EALUATION_GOOD);
                        ealuationFragmentGood = new EaluationFragment();
                        ealuationFragmentGood.setArguments(bundle);
                    }
                    return ealuationFragmentGood;
                case EALUATION_MID:
                    if (ealuationFragmentMid == null) {
                        bundle.putInt(GOODSTYPE, EALUATION_MID);
                        ealuationFragmentMid = new EaluationFragment();
                        ealuationFragmentMid.setArguments(bundle);
                    }
                    return ealuationFragmentMid;
                case EALUATION_BAD:
                    if (ealuationFragmentBad == null) {
                        bundle.putInt(GOODSTYPE, EALUATION_BAD);
                        ealuationFragmentBad = new EaluationFragment();
                        ealuationFragmentBad.setArguments(bundle);
                    }
                    return ealuationFragmentBad;
            }
            return null;
        }
    };

    /**
     * 设置评论tab，不同类型评论的数
     *
     * @param all
     * @param good
     * @param mid
     * @param bad
     */
    public void setEalutionNum(int all, int good, int mid, int bad) {
        String AllStr = "全部评论\n" + String.valueOf(all);
        String GoodStr = "好评\n" + String.valueOf(good);
        String MidStr = "中评\n" + String.valueOf(mid);
        String BadStr = "差评\n" + String.valueOf(bad);
        SpannableString spannableStringAll = new SpannableString(AllStr);
        SpannableString spannableStringGood = new SpannableString(GoodStr);
        SpannableString spannableStringMid = new SpannableString(MidStr);
        SpannableString spannableStringBad = new SpannableString(BadStr);

        spannableStringAll.setSpan(new AbsoluteSizeSpan(13, true), 0, 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableStringAll.setSpan(new AbsoluteSizeSpan(10, true), 5, AllStr.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        btn_comments_all.setText(spannableStringAll);
        btn_comments_all.setMovementMethod(LinkMovementMethod.getInstance());

        spannableStringGood.setSpan(new AbsoluteSizeSpan(13, true), 0, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableStringGood.setSpan(new AbsoluteSizeSpan(10, true), 3, GoodStr.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        btn_comments_good.setText(spannableStringGood);
        btn_comments_good.setMovementMethod(LinkMovementMethod.getInstance());

        spannableStringMid.setSpan(new AbsoluteSizeSpan(13, true), 0, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableStringMid.setSpan(new AbsoluteSizeSpan(10, true), 3, MidStr.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        btn_comments_mid.setText(spannableStringMid);
        btn_comments_mid.setMovementMethod(LinkMovementMethod.getInstance());

        spannableStringBad.setSpan(new AbsoluteSizeSpan(13, true), 0, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableStringBad.setSpan(new AbsoluteSizeSpan(10, true), 3, BadStr.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        btn_comments_bad.setText(spannableStringBad);
        btn_comments_bad.setMovementMethod(LinkMovementMethod.getInstance());
    }

    /**
     * 跳转到评价详情
     *
     * @param mContext
     * @param goodsId
     */
    public static void goEalutionActivity(Context mContext, int goodsId) {
        Intent intent = new Intent(mContext, EaluationActivity.class);
        intent.putExtra("goodsId", goodsId);
        mContext.startActivity(intent);
    }
}

