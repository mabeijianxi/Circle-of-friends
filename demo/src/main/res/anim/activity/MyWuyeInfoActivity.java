package anim.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.asyey.footballlibrary.network.exception.HttpException;
import com.asyey.footballlibrary.network.http.ResponseInfo;
import com.henanjianye.soon.communityo2o.BaseActivity;
import com.henanjianye.soon.communityo2o.LoginActivity;
import com.henanjianye.soon.communityo2o.MainActivity;
import com.henanjianye.soon.communityo2o.R;
import com.henanjianye.soon.communityo2o.adapter.MyHouseRelationshipAdapter;
import com.henanjianye.soon.communityo2o.common.Constant;
import com.henanjianye.soon.communityo2o.common.MyLog;
import com.henanjianye.soon.communityo2o.common.UserDataHelper;
import com.henanjianye.soon.communityo2o.common.enties.AuthrStatusBean;
import com.henanjianye.soon.communityo2o.common.enties.BaseBean;
import com.henanjianye.soon.communityo2o.common.enties.BaseDataBean;
import com.henanjianye.soon.communityo2o.common.enties.JsonUtil;
import com.henanjianye.soon.communityo2o.common.enties.UserBean;
import com.henanjianye.soon.communityo2o.common.util.CommonUtils;
import com.henanjianye.soon.communityo2o.common.util.MyTitleBarHelper;
import com.henanjianye.soon.communityo2o.common.util.NetWorkStateUtils;
import com.henanjianye.soon.communityo2o.common.util.UserSharedPreferencesUtil;
import com.henanjianye.soon.communityo2o.interf.NetWorkCallback;
import com.henanjianye.soon.communityo2o.view.MyListView;
import com.henanjianye.soon.communityo2o.view.TransDialog;
import com.umeng.analytics.MobclickAgent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sms on 2015/10/8.
 */
public class MyWuyeInfoActivity extends BaseActivity implements View.OnClickListener {
    private ListView titleList;
    private MyWuyeInfoAdapter adapter;
    //    private String userDataJson;
    private static BaseBean<UserBean.House> userData;
    private MyTitleBarHelper titleBarHelper;
    private MyHouseRelationshipAdapter myHouseRelationshipAdapter;
    private UserDataHelper userDataHelper;
    private TextView tv_empty;

    @Override
    public int mysetContentView() {
        return R.layout.my_wu_ye_information;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        setProcess();
    }

    private void initView() {
        tv_empty = (TextView) findViewById(R.id.tv_empty);
        titleBarHelper = new MyTitleBarHelper(this, getWindow().getDecorView().findViewById(android.R.id.content));
        titleBarHelper.setMiddleText("我的物业信息");
        titleBarHelper.setRightImag(R.drawable.wuye_plus_image);
        titleBarHelper.setOnclickListener(this);
        titleList = (ListView) findViewById(R.id.my_titleList);
        adapter = new MyWuyeInfoAdapter(MyWuyeInfoActivity.this);
        titleList.setAdapter(adapter);
        userDataHelper = new UserDataHelper(this);
    }

    private void setProcess() {
        requestHouseData();
    }

    //删除房屋信息
    private void DeleteHouse(String house) {
        userDataHelper.delete_House(getNetRequestHelper(MyWuyeInfoActivity.this).isShowProgressDialog(true), house);
    }

    //请求房屋信息
    private void requestHouseData() {
        userDataHelper.getHouseInfo(getNetRequestHelper(MyWuyeInfoActivity.this).isShowProgressDialog(true));
    }


    //请求房屋信息
    private void saveCurrentHouseData(int orgid) {
        userDataHelper.savewHouseInfo(getNetRequestHelper(MyWuyeInfoActivity.this).isShowProgressDialog(true), orgid);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.iv_title_bar_left:
                MyWuyeInfoActivity.this.finish();
                break;
            case R.id.iv_title_bar_right:
//                toast("跳转到添加小区界面");
//                if(adapter!=null&&adapter.getCount()<6){
                CommonUtils.startLoginChooseOrgActivity(this);
//                }else{
//                    toast("该房下已经认证满员，如需增加认证，请致电贴心管家");
//                }
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

            }

            @Override
            public void onLoading(long total, long current, boolean isUploading, String requestTag) {

            }

            @Override
            public void onSuccess(ResponseInfo<String> responseInfo, String requestTag) {
                if (requestTag.equals(Constant.HouseUrl.DELETE)) {// 删除物业记录
                    //删除一条房屋信息
                    adapter.removeItem(currentPos);
                    if (adapter.getCount() == 0) {

                        requestAnthrStatus();
                    }
                } else if (requestTag.equals(Constant.HouseUrl.GETALL)) {// 获得物业记录
                    parseHouseInfo(responseInfo.result);
                }
                if (Constant.HouseUrl.ADD_ORG.equals(requestTag)) {
                    BaseDataBean<UserBean> baseDataBean = JsonUtil.parseDataObject(responseInfo.result, UserBean.class);
                    if (baseDataBean.code == 100) {
                        UserBean userBean = baseDataBean.data;
                        UserSharedPreferencesUtil.savaUserInfo(MyWuyeInfoActivity.this, userBean);
                        UserSharedPreferencesUtil.savaUserJsonInfo(MyWuyeInfoActivity.this, responseInfo.result);
                        Intent intent = new Intent(MyWuyeInfoActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.setAction(LoginActivity.Login_Verify_Identity_Over_Fragment);
                        startActivity(intent);
                    } else {
                        toast(baseDataBean.msg);
                    }

                }
                if (requestTag.equals(Constant.Repair.AUTHR_STATUS)) {
                    MyLog.e("AAA","AUTHR_STATUS----"+responseInfo.result);
                    BaseDataBean<AuthrStatusBean> baseDataBean = JsonUtil.parseDataObject(responseInfo.result, AuthrStatusBean.class);
                    if (baseDataBean.code == 100) {
                        //0:未提交审核，1:审核中，2:审核成功，3:审核失败
                        AuthrStatusBean aBean = baseDataBean.data;
                        if (aBean != null) {
                            //审核状态保存到本地
                            UserSharedPreferencesUtil.savaUserInfo(MyWuyeInfoActivity.this, UserSharedPreferencesUtil.HOUSECERTSTATUS, aBean.houseCertStatus);
                            setResult(RESULT_OK, getIntent());
                        }
                    }
                }
            }

            private void parseHouseInfo(String result) {
//                UserSharedPreferencesUtil userSharedPreferencesUtil = new UserSharedPreferencesUtil();
////        用户信息的json数据
//                userDataJson = userSharedPreferencesUtil.getUserJsonInfo(getApplication());
//                if (userDataJson != null) {
                userData = JsonUtil.jsonArray(result, UserBean.House.class);
                if (userData.data != null && userData.data.size() > 0) {
                    List<UserBean.House> houseList = userData.data;
                    if (adapter == null) {
                        adapter = new MyWuyeInfoAdapter(MyWuyeInfoActivity.this);
                    }
//                    adapter.clearData();
                    adapter.addData(houseList);
                    adapter.notifyDataSetChanged();
//                    titleList.setAdapter(adapter);
                }
                if (adapter != null && adapter.getCount() > 0) {
                    tv_empty.setVisibility(View.GONE);
                } else {
                    tv_empty.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(HttpException error, String msg, String requestTag) {
                NetWorkStateUtils.errorNetMes(MyWuyeInfoActivity.this);
            }
        };
    }


    private int currentPos = 0;

    public class MyWuyeInfoAdapter extends BaseAdapter {
        private List<UserBean.House> list = new ArrayList<>();
        private Context context;
        private UserBean userBean;
        private String userDataJson;
        ViewHolder holder = null;
        // 用来记录按钮状态的Map
        public Map<Integer, Boolean> isChecked;

        public MyWuyeInfoAdapter(Context context) {
            this.context = context;
            init(); // 一定要在这里调用，在构造Adapter对象时方便初始化
        }

        private void init() {
            isChecked = new HashMap<Integer, Boolean>();
            for (int i = 0; i < list.size(); i++) {
                isChecked.put(i, false);
            }
        }

        @Override
        public int getCount() {
            return list == null ? 0 : list.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public void clearData() {
            if (list != null && list.size() > 0) {
                list.clear();
                notifyDataSetChanged();
            }
        }

        public void removeItem(int pos) {
            list.remove(pos);
            notifyDataSetChanged();
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(context).inflate(R.layout.my_wuyeinformation_item, null);
                holder.tv_org_name = (TextView) convertView.findViewById(R.id.tv_org_name);
                holder.tv_enter_org = (TextView) convertView.findViewById(R.id.tv_enter_org);
                holder.btn_leave_org = (TextView) convertView.findViewById(R.id.btn_leave_org);
                holder.ll_ll_relationShip = (ImageView) convertView.findViewById(R.id.ll_relationShip);
                holder.my_subList = (MyListView) convertView.findViewById(R.id.my_subList);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.tv_org_name.setText(list.get(position).fullAddress);
            holder.tv_org_name.setTextColor(Color.parseColor("#505050"));
            holder.tv_org_name.setTextSize(16);

//        第一个ListView的监听事件
            holder.tv_enter_org.setTag(position);
            holder.tv_enter_org.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    int pos = (Integer) v.getTag();
                    saveCurrentHouseData(Integer.parseInt(list.get(pos).orgId));
//                    CommonUtils.startLoginChooseOrgActivity(context);
                }
            });
            holder.btn_leave_org.setTag(position);
            holder.btn_leave_org.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final int pos = (Integer) v.getTag();
//                删除Item
//                    final String house = UserSharedPreferencesUtil.getUserInfo(context, UserSharedPreferencesUtil.HOUSEID, -1) + "";
                    View view1 = View.inflate(context, R.layout.delete_house,
                            null);
                    final TransDialog alertDialog = new TransDialog(context, 0, view1);
                    TextView tv_dismiss = (TextView) view1
                            .findViewById(R.id.tv_dismiss);
                    ImageView iv_close1 = (ImageView)
                            view1.findViewById(R.id.iv_close1);
                    TextView tv_delete = (TextView)
                            view1.findViewById(R.id.tv_delete);
                    iv_close1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            alertDialog.dismiss();
                        }
                    });
                    tv_dismiss.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            alertDialog.dismiss();
                        }
                    });
                    tv_delete.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            alertDialog.dismiss();
                            DeleteHouse(list.get(pos).houseId + "");
                            currentPos = pos;
                        }
                    });
                    alertDialog.show();
                }
            });
            holder.ll_ll_relationShip.setTag(position);
            holder.ll_ll_relationShip.setTag(R.string.tencentweibo, holder.my_subList);
            holder.my_subList.setVisibility(View.GONE);
            if (list.get(position).others.size() != 0) {
                MyHouseRelationshipAdapter adapter = new MyHouseRelationshipAdapter(context);
                adapter.addData(list.get(position).others);
                holder.my_subList.setAdapter(adapter);
            } else {
                holder.my_subList.setAdapter(null);
            }
            holder.ll_ll_relationShip.setOnClickListener(new MyClick(position) {
                @Override
                public void onClick(View v) {
                    final int pos = (Integer) v.getTag();
                    MyListView myListView = (MyListView) v.getTag(R.string.tencentweibo);
                    if (list.get(position).others.size() != 0) {
                        if (myListView.getVisibility() == View.GONE) {
                            myListView.setVisibility(View.VISIBLE);
                        } else if (myListView.getVisibility() == View.VISIBLE) {
                            myListView.setVisibility(View.GONE);
                        }
                    } else {
                        toast("该房子下面没有其他人了");
                    }

                }
            });

            return convertView;
        }

        public void addData(List<UserBean.House> list) {
            this.list.clear();
            this.list.addAll(list);
            notifyDataSetChanged();
        }

        public final class ViewHolder implements Serializable {
            public TextView tv_org_name;
            public TextView tv_enter_org;
            public TextView btn_leave_org;
            public ImageView ll_ll_relationShip;
            private MyListView my_subList;
        }

        class MyClick implements View.OnClickListener {
            private int position;

            public MyClick(int position) {  // 在构造时将position传给它这样就知道点击的是哪个条目的按钮
                this.position = position;
            }

            @Override
            public void onClick(View v) {
                int vid = v.getId();
                if (vid == holder.ll_ll_relationShip.getId()) {
                    if (isChecked.get(position) == false) {
                        isChecked.put(position, true);   // 根据点击的情况来将其位置和相应的状态存入
                    } else if (isChecked.get(position) == true) {
                        isChecked.put(position, false);  // 根据点击的情况来将其位置和相应的状态存入
                    }
                    notifyDataSetChanged();
                }
            }
        }
    }

    //请求用户认证状态
    private void requestAnthrStatus() {
        new UserDataHelper(MyWuyeInfoActivity.this).getAuthrStatus(getNetRequestHelper(MyWuyeInfoActivity.this).isShowProgressDialog(true));
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
