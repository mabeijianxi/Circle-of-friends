package anim.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.henanjianye.soon.communityo2o.BaseActivity;
import com.henanjianye.soon.communityo2o.R;
import com.henanjianye.soon.communityo2o.common.enties.AccountBean;
import com.henanjianye.soon.communityo2o.common.util.MyTitleBarHelper;

import java.util.ArrayList;
import java.util.List;


public class JianYeCoinRecordActivity extends BaseActivity {
    private MyTitleBarHelper myTitleBarHelper;
    private ListView bListView;
    private BalanceAdapter mAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jianye_account);
        initViews();
        init();
        initEvents();
    }

    private void initViews() {
        myTitleBarHelper = new MyTitleBarHelper(this, getWindow().getDecorView().findViewById(android.R.id.content));
        myTitleBarHelper.setLeftImgVisible(false);
        myTitleBarHelper.resetState();
        myTitleBarHelper.setMiddleText("建业通宝记录");
        myTitleBarHelper.setRightImgVisible(false);
        myTitleBarHelper.setLeftImag(R.mipmap.btn_back);
        bListView = (ListView) findViewById(R.id.balance_list);
    }

    private void initEvents() {
        myTitleBarHelper.setOnclickListener(this);
    }

//    private List<CardInfoBean> cardInfoBeanList = new ArrayList<>();

    private void init() {
        mAdapter = new BalanceAdapter(this);
        bListView.setAdapter(mAdapter);
//        for (int i = 0; i < 3; i++) {
//            CardInfoBean cardInfoBean = new CardInfoBean();
//            cardInfoBean.name = "Animation" + i;
//            cardInfoBeanList.add(cardInfoBean);
//        }
//        walletAdapter = new WalletAdapter(this, cardInfoBeanList);
//        cardRecycleView.setLayoutManager(new LinearLayoutManager(this));
//        cardRecycleView.setItemAnimator(new DefaultItemAnimator());
//        cardRecycleView.setAdapter(walletAdapter);
    }


    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.iv_title_bar_left:
                this.finish();
                break;
        }
    }

    public class BalanceAdapter extends BaseAdapter {
        private Context mContext;
        private LayoutInflater mLayoutInflater;
        private List<AccountBean> aList = new ArrayList<>();

        public BalanceAdapter(Context context) {
            this.mContext = context;
            this.mLayoutInflater = LayoutInflater.from(mContext);
        }

        @Override
        public int getCount() {
            return 10;
        }

        @Override
        public Object getItem(int position) {
            return aList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            AccountHolder mHolder;
            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.item_account_record, parent, false);
                mHolder = new AccountHolder();
                mHolder.tv_account_info = (TextView) convertView.findViewById(R.id.tv_account_info);
                mHolder.tv_account_num = (TextView) convertView.findViewById(R.id.tv_account_num);
                mHolder.tv_account_date = (TextView) convertView.findViewById(R.id.tv_account_date);
                mHolder.tv_account_status = (TextView) convertView.findViewById(R.id.tv_account_status);
                convertView.setTag(mHolder);
            } else {
                mHolder = (AccountHolder) convertView.getTag();
            }
            return convertView;
        }

        public final class AccountHolder {
            TextView tv_account_info;
            TextView tv_account_date;
            TextView tv_account_num;
            TextView tv_account_status;
        }
    }
}
