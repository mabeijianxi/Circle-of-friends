package anim.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.asyey.footballlibrary.network.exception.HttpException;
import com.asyey.footballlibrary.network.http.ResponseInfo;
import com.henanjianye.soon.communityo2o.BaseActivity;
import com.henanjianye.soon.communityo2o.R;
import com.henanjianye.soon.communityo2o.adapter.WalletAdapter;
import com.henanjianye.soon.communityo2o.common.Constant;
import com.henanjianye.soon.communityo2o.common.CustomListView;
import com.henanjianye.soon.communityo2o.common.MyLog;
import com.henanjianye.soon.communityo2o.common.UserDataHelper;
import com.henanjianye.soon.communityo2o.common.enties.AccountInfoBean;
import com.henanjianye.soon.communityo2o.common.enties.BaseDataBean;
import com.henanjianye.soon.communityo2o.common.enties.JsonUtil;
import com.henanjianye.soon.communityo2o.common.util.CommonUtils;
import com.henanjianye.soon.communityo2o.common.util.MyTitleBarHelper;
import com.henanjianye.soon.communityo2o.common.util.SharedPreferencesUtil;
import com.henanjianye.soon.communityo2o.interf.NetWorkCallback;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;


public class MyWalletActivity extends BaseActivity {
    private MyTitleBarHelper myTitleBarHelper;
    private CustomListView mListView;
    private WalletAdapter walletAdapter;
    //    private static final int SCALE_DELAY = 100;
    private TextView tv_add_card;
    private RelativeLayout rest_balance;
    private RelativeLayout jianye_balance;
    private static final int ADD_CARD_CODE = 0x2018;
    private TextView tv_rest_balance;
    private TextView tv_jianye_balance;
    private ScrollView sv_wallet;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mywallet);
        initViews();
        init();
        initEvents();
//        scrollView.smoothScrollTo();
    }

    private void initViews() {
        myTitleBarHelper = new MyTitleBarHelper(this, getWindow().getDecorView().findViewById(android.R.id.content));
        myTitleBarHelper.setLeftImgVisible(false);
        myTitleBarHelper.resetState();
        myTitleBarHelper.setMiddleText("我的钱包");
//        myTitleBarHelper.setRightImgVisible(false);
        myTitleBarHelper.setRightImag(R.drawable.account_record);
        myTitleBarHelper.setLeftImag(R.mipmap.btn_back);
        mListView = (CustomListView) findViewById(R.id.mListView);
        sv_wallet = (ScrollView) findViewById(R.id.sv_wallet);
        tv_add_card = (TextView) findViewById(R.id.tv_add_card);
        rest_balance = (RelativeLayout) findViewById(R.id.rest_balance);
        jianye_balance = (RelativeLayout) findViewById(R.id.jianye_balance);
        tv_rest_balance = (TextView) findViewById(R.id.tv_rest_balance);
        tv_jianye_balance = (TextView) findViewById(R.id.tv_jianye_balance);
    }

    private void initEvents() {
        myTitleBarHelper.setOnclickListener(this);
        rest_balance.setOnClickListener(this);
        tv_add_card.setOnClickListener(this);
        jianye_balance.setOnClickListener(this);
    }

    @Override
    public void onBackPressed() {
        sendBackBalance();
    }

    public static String PURSE_REST_BALANCE = "purse_rest_balance";
    public static String PURSE_JIANYE_BALANCE = "purse_jianye_balance";
    private boolean isNew = false;

    private void sendBackBalance() {
        if (isNew) {
            Intent intent = getIntent();
            intent.putExtra(PURSE_REST_BALANCE, rest_balances);
            intent.putExtra(PURSE_JIANYE_BALANCE, jianye_balances);
            setResult(RESULT_OK, intent);
        }
        finish();
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.iv_title_bar_left:
                sendBackBalance();
                break;
            case R.id.iv_title_bar_right:
                startActivity(new Intent(this, PurseSelect.class));
                break;
            case R.id.tv_add_card:
//                CardInfoBean cardInfoBean = new CardInfoBean();
//                cardInfoBean.name = "Animation new";
////                    cardInfoBeanList.add(cardInfoBean);
//                int pos1 = walletAdapter.getCardsList().size();
//                walletAdapter.addData(0, cardInfoBean);
                startActivityForResult(new Intent(this, ExtremeCardBoundActivity.class), ADD_CARD_CODE);
                break;
            case R.id.rest_balance:
                Intent intent = new Intent(this, RestBalanceRecordActivity.class);
                intent.putExtra(RestBalanceRecordActivity.PURSE_KEY, 1);
                intent.putExtra(RestBalanceRecordActivity.PURSE_NUM_KEY, rest_balances);
                startActivity(intent);
                break;
            case R.id.jianye_balance:
                Intent intent1 = new Intent(this, RestBalanceRecordActivity.class);
                intent1.putExtra(RestBalanceRecordActivity.PURSE_KEY, 2);
                intent1.putExtra(RestBalanceRecordActivity.PURSE_NUM_KEY, jianye_balances);
                startActivity(intent1);
//                startActivity(new Intent(this, JianYeCoinRecordActivity.class));
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case ADD_CARD_CODE:
                    //重新请求 刷新界面
                    requestPurseData();
                    break;
                default:
                    break;
            }
        }
    }

    private List<AccountInfoBean.ExtremeCard> eCards = new ArrayList<>();

    private void init() {
//        for (int i = 0; i < 3; i++) {
//            CardInfoBean cardInfoBean = new CardInfoBean();
//            cardInfoBean.name = "Animation" + i;
//            cardInfoBeanList.add(cardInfoBean);
//        }
        walletAdapter = new WalletAdapter(this);
        mListView.setAdapter(walletAdapter);
        // 我的钱包缓存
        String purseString = SharedPreferencesUtil.getStringData(this,
                Constant.ShouYeUrl.PURSE_INFO_URL, null);
        if (!TextUtils.isEmpty(purseString)) {
            parsePurseData(purseString);
        }
        if (CommonUtils.isNetworkConnected(this)) {
            requestPurseData();
        } else {
            toast("网络不可用");
        }
    }

    private void requestPurseData() {
        new UserDataHelper(this).getPurseInfor(getNetRequestHelper(this).isShowProgressDialog(false));
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
                MyLog.e("MMM", "responseInfo.result is " + responseInfo.result);
                if (requestTag.equals(Constant.ShouYeUrl.PURSE_INFO_URL)) {
                    SharedPreferencesUtil.saveStringData(MyWalletActivity.this,
                            Constant.ShouYeUrl.PURSE_INFO_URL, responseInfo.result);
                    parsePurseData(responseInfo.result);
                }
            }

            @Override
            public void onFailure(HttpException error, String msg, String requestTag) {
                MyLog.e("MMM", "msg is " + msg);
            }
        };
    }

    private String rest_balances = "0";
    private String jianye_balances = "0";

    private void parsePurseData(String purseInfo) {
        try {
            BaseDataBean<AccountInfoBean> json = JsonUtil.parseDataObject(purseInfo, AccountInfoBean.class);
            if (json.code == 100) {
                AccountInfoBean accountInfoBean = json.data;
                if (accountInfoBean != null) {
                    isNew = true;
                    if (accountInfoBean.balance != null) {
                        rest_balances = accountInfoBean.balance;
                        tv_rest_balance.setText(accountInfoBean.balance);
                    } else {
                        tv_rest_balance.setText("0");
                    }
                    if (accountInfoBean.jycoin != null) {
                        jianye_balances = accountInfoBean.jycoin;
                        tv_jianye_balance.setText(accountInfoBean.jycoin);
                    } else {
                        tv_jianye_balance.setText("0");
                    }
//                    tv_jianye_balance.setText(accountInfoBean.jycoin);
                    eCards = accountInfoBean.bindList;
                    if (eCards != null && eCards.size() > 0) {
                        walletAdapter.addAll(eCards);
                        sv_wallet.smoothScrollTo(0, 0);
                    }
                } else {
                    tv_rest_balance.setText("0");
                    tv_jianye_balance.setText("0");
                }
            } else {
                toast("数据错误");
            }
        } catch (Exception e) {
            e.printStackTrace();
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
