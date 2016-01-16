package anim.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.asyey.footballlibrary.network.exception.HttpException;
import com.asyey.footballlibrary.network.http.ResponseInfo;
import com.henanjianye.soon.communityo2o.BaseActivity;
import com.henanjianye.soon.communityo2o.R;
import com.henanjianye.soon.communityo2o.adapter.MyTelRecycleAdapter;
import com.henanjianye.soon.communityo2o.common.Constant;
import com.henanjianye.soon.communityo2o.common.MyLog;
import com.henanjianye.soon.communityo2o.common.UserDataHelper;
import com.henanjianye.soon.communityo2o.common.enties.BaseBean;
import com.henanjianye.soon.communityo2o.common.enties.BaseDataBean;
import com.henanjianye.soon.communityo2o.common.enties.JsonUtil;
import com.henanjianye.soon.communityo2o.common.enties.TelPagerBean;
import com.henanjianye.soon.communityo2o.common.enties.UserBean;
import com.henanjianye.soon.communityo2o.common.enties.WuyeKeeperBean;
import com.henanjianye.soon.communityo2o.common.util.CommonUtils;
import com.henanjianye.soon.communityo2o.common.util.MyTitleBarHelper;
import com.henanjianye.soon.communityo2o.common.util.NetWorkStateUtils;
import com.henanjianye.soon.communityo2o.common.util.UserSharedPreferencesUtil;
import com.henanjianye.soon.communityo2o.interf.MyItemClickListener;
import com.henanjianye.soon.communityo2o.interf.NetWorkCallback;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sms on 2015/10/6.
 */
public class MyTelYellowPagerActivity extends BaseActivity implements View.OnClickListener, MyItemClickListener {
    private MyTitleBarHelper title;
    private ImageView iv_title_bar_left;
    private RecyclerView mRecyclerView;
    private int orgId;
    private MyTelRecycleAdapter adapter;
    private String building;
    private UserDataHelper userDataHelper;
    private TextView keeperTitle;
    private TextView wuyeKeeper;
    private TextView keeperTel;
    private RelativeLayout cell;
    private TextView cellTitle;
    private String keeperNum;
    private String telNum;
    private RelativeLayout cellTwo;
    private TextView commTel;
    private String commNum;
    private BaseBean<TelPagerBean> baseBean;
    private ImageView img_tel_one;
    private LinearLayout ll_wuye_keeper;
    //    int orgCertStatus;
    private BaseDataBean<WuyeKeeperBean> beanBaseDataBean;
    private BaseDataBean<UserBean> houseList;
    private TextView tv_tel_pager;
    private int loginState;

    @Override
    public int mysetContentView() {
        return R.layout.my_tel_yellow_page;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initView();
        setListener();
        processLogic();

    }

    private void initView() {
        title = new MyTitleBarHelper(this, getWindow().getDecorView().findViewById(android.R.id.content));
        title.setMiddleText("常用电话");
        title.setOnclickListener(this);

        keeperTitle = (TextView) findViewById(R.id.my_title_tel);
        wuyeKeeper = (TextView) findViewById(R.id.tv_wuye);
        keeperTel = (TextView) findViewById(R.id.tv_tel);
        cell = (RelativeLayout) findViewById(R.id.phone_line_one);
        cellTitle = (TextView) findViewById(R.id.my_title_tel);
        img_tel_one = (ImageView) findViewById(R.id.img_tel_one);
        ll_wuye_keeper = (LinearLayout) findViewById(R.id.ll_wuye_keeper);
//        commTel = (TextView) findViewById(R.id.tel1);

        iv_title_bar_left = (ImageView) findViewById(R.id.iv_title_bar_left);
        tv_tel_pager = (TextView) findViewById(R.id.tv_tel_pager);
//         创建布局管理器
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
        adapter = new MyTelRecycleAdapter();
        adapter.setOnItemClickListener(this);
        mRecyclerView.setAdapter(adapter);
    }

    private void setListener() {
        cell.setOnClickListener(this);
    }

    private void processLogic() {
        orgId = UserSharedPreferencesUtil.getUserInfo(this, UserSharedPreferencesUtil.ORGID, -1);
        loginState = UserSharedPreferencesUtil.getUserLoginState(this);
        String houseDataJson = UserSharedPreferencesUtil.getUserJsonInfo(this);
        if (houseDataJson != null) {
            houseList = JsonUtil.parseDataObject(houseDataJson, UserBean.class);
//            if (houseList != null) {
//                if (houseList.data != null) {
//                    if (houseList.data.houseList == null) {
//                        for (int i = 0; i < houseList.data.houseList.size(); i++)
//                            orgCertStatus = houseList.data.houseList.get(i).orgCertStatus;
//                    }
//                }
//            }
        }
//        building 暂时是假数据
        building = UserSharedPreferencesUtil.getUserInfo(this, UserSharedPreferencesUtil.BUILDING, null);
        MyLog.e("AAA", "orgId----" + orgId + "----building---" + building+"--loginState--"+loginState);
        if (loginState == 3) {
            if (orgId > 0 && !TextUtils.isEmpty(building)) {
                requestKeeper(orgId, building);
            }
        }
        if (orgId == -1) {
            toast("小区ID不能为空");
            return;
        } else if (orgId > 0) {
            requestComm(orgId);
        }
    }

    private void requestKeeper(int orgId, String building) {
        userDataHelper = new UserDataHelper(this);
        userDataHelper.getTelKeeper(getNetRequestHelper(MyTelYellowPagerActivity.this), orgId, building);
    }

    private void requestComm(int orgId) {
        userDataHelper = new UserDataHelper(this);
        userDataHelper.getTelNomal(getNetRequestHelper(MyTelYellowPagerActivity.this), orgId);
    }


    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.iv_title_bar_left:
                MyTelYellowPagerActivity.this.finish();
                break;
            case R.id.phone_line_one:
//   上面：设置Item的监听事件
                keeperNum = keeperTel.getText().toString();
                CommonUtils.showPhoneDialog(MyTelYellowPagerActivity.this, keeperNum);

                /*if(keeperNum.contains(",")){

                    String[] split = keeperNum.split(",");
                 final String  number1= split[0];
                 final String  number2= split[1];

                    AlertDialog.Builder builder = new AlertDialog.Builder(MyTelYellowPagerActivity.this);
                    String[] items={number1,number2};
                    builder.setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which){
                                case 0 :
                                    CommonUtils.showPhoneDialog(MyTelYellowPagerActivity.this,number1);

                                break;
                                case 1:
                                    CommonUtils.showPhoneDialog(MyTelYellowPagerActivity.this,number2);
                               break;
                            }

                        }
                    });
                       builder.create().show();

                }else{

                    CommonUtils.showPhoneDialog(MyTelYellowPagerActivity.this,keeperNum);
                }
*/
//                keeperNum = keeperTel.getText().toString();
//                toast("keeper" + keeperNum);
//////                跳转到activity后背景不透明
////                Intent intent = new Intent();
////                intent.putExtra("keeperNum",keeperNum);
////                intent.setClass(MyTelYellowPagerActivity.this,MyCallTelPagerActivity.class);
////                startActivity(intent);
////                callTel(keeperNum);
//                View view = View.inflate(MyTelYellowPagerActivity.this, R.layout.event_dialog,
//                        null);
//                final TransDialog alertDialog = new TransDialog(MyTelYellowPagerActivity.this, 0, view);
//                TextView tv_event_number = (TextView) view
//                        .findViewById(R.id.tv_event_number);
//                tv_event_number.setText(keeperNum);
//                ImageView iv_event_dialog_close = (ImageView)
//                        view.findViewById(R.id.iv_event_dialog_close);
//                TextView tv_event_call = (TextView)
//                        view.findViewById(R.id.tv_event_call);
//                iv_event_dialog_close.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        // TODO Auto-generated method stub
//                        alertDialog.dismiss();
//                    }
//                });
//                tv_event_call.setOnClickListener(new View.OnClickListener() {
//
//                    @Override
//                    public void onClick(View v) {
//                        alertDialog.dismiss();
//                        callTel(keeperNum);
//                    }
//                });
//                alertDialog.show();
                break;
        }
    }


    //    下面：设置Item的监听事件
    @Override
    public void onItemClick(View view, int postion) {
        commTel = (TextView) view.findViewById(R.id.tel1);
        commNum = commTel.getText().toString();
        MobclickAgent.onEvent(MyTelYellowPagerActivity.this,"StewardTap");
        if (commNum.contains(",")) {


            String[] split = commNum.split(",");
            if (split.length > 1) {


                final String number1 = split[0];
                final String number2 = split[1];
                //  toast("监听事件" + number1);
                AlertDialog.Builder builder = new AlertDialog.Builder(MyTelYellowPagerActivity.this);
                String[] items = {number1, number2};
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                CommonUtils.showPhoneDialog(MyTelYellowPagerActivity.this, number1);

                                break;
                            case 1:
                                CommonUtils.showPhoneDialog(MyTelYellowPagerActivity.this, number2);
                                break;
                        }

                    }
                });
                builder.create().show();


            }

        } else {

            CommonUtils.showPhoneDialog(MyTelYellowPagerActivity.this, commNum);
        }


        //toast("监听事件" + commNum);

        //CommonUtils.showPhoneDialog(MyTelYellowPagerActivity.this, commNum);

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
                MyLog.e("AAA", requestTag + "responseInfo.result----" + responseInfo.result);
                if (Constant.TelPage.TELPHONEPAGER.equals(requestTag)) {
                    baseBean = JsonUtil.jsonArray(responseInfo.result, TelPagerBean.class);

                    List<TelPagerBean> telPagerBean = baseBean.data;
                    List<Object> positionData = telPagerBeanToPositionData(telPagerBean);
                    if (baseBean.code == 100) {
                        if (baseBean.data.size() != 0) {
                            adapter.addData(positionData);
                        } else {
                            tv_tel_pager.setVisibility(View.VISIBLE);
                        }
                    } else {
                        toast(baseBean.msg);
                    }
                }
                if (Constant.TelPage.WUYEKEEPER.equals(requestTag)) {
                    beanBaseDataBean = JsonUtil.parseDataObject(responseInfo.result, WuyeKeeperBean.class);

                    MyLog.e("AAA","responseInfo.result--"+responseInfo.result);
                    if (beanBaseDataBean.data != null) {
                        if (beanBaseDataBean.code == 100) {
                            if (beanBaseDataBean.data.pNum != null) {
                                ll_wuye_keeper.setVisibility(View.VISIBLE);
                                wuyeKeeper.setText(beanBaseDataBean.data.pName);
                                if (beanBaseDataBean.data.description != null) {
                                    keeperTitle.setText(beanBaseDataBean.data.description);
                                }
                                keeperTel.setText(beanBaseDataBean.data.pNum);
                                img_tel_one.setImageResource(R.mipmap.tlb_phoneicon_normal);
                            } else {
                                ll_wuye_keeper.setVisibility(View.GONE);
                            }
                        } else {
                            toast(beanBaseDataBean.msg);
                        }
                    }
                }
            }

            @Override
            public void onFailure(HttpException error, String msg, String requestTag) {
                NetWorkStateUtils.errorNetMes(MyTelYellowPagerActivity.this);
            }
        };
    }

    //将树形结构添加到一个对象中
    private List<Object> telPagerBeanToPositionData(List<TelPagerBean> telPagerBean) {
        List<Object> positionData = new ArrayList<Object>();
        if (telPagerBean == null) {
            return positionData;
        }
        for (int i = 0; i < telPagerBean.size(); i++) {
            positionData.add(telPagerBean.get(i));
            for (int j = 0; j < telPagerBean.get(i).phones.size(); j++) {
                positionData.add(telPagerBean.get(i).phones.get(j));
            }
        }
        return positionData;
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
