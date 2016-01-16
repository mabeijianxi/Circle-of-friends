package anim.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.asyey.footballlibrary.network.exception.HttpException;
import com.asyey.footballlibrary.network.http.ResponseInfo;
import com.henanjianye.soon.communityo2o.BaseActivity;
import com.henanjianye.soon.communityo2o.R;
import com.henanjianye.soon.communityo2o.common.Constant;
import com.henanjianye.soon.communityo2o.common.enties.BaseDataBean;
import com.henanjianye.soon.communityo2o.common.enties.JsonUtil;
import com.henanjianye.soon.communityo2o.common.util.MD5Tool;
import com.henanjianye.soon.communityo2o.common.util.MyTitleBarHelper;
import com.henanjianye.soon.communityo2o.common.util.NetWorkStateUtils;
import com.henanjianye.soon.communityo2o.fragment.BaseFragment;
import com.henanjianye.soon.communityo2o.fragment.me.PasswordSetFragment;
import com.henanjianye.soon.communityo2o.interf.NetWorkCallback;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PursePsdManagerActivity extends BaseActivity {
    private MyTitleBarHelper myTitleBarHelper;
    private HashMap<Integer, Fragment> fragments = new HashMap<>();
    private PasswordSetFragment passwordSetFragment;
    private String psdFlag;
    private int curPos;
    public static final String PSDMD5 = "psd_md5";
    private String getCode = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        psdFlag = intent.getStringExtra(Constant.Purse.PASSWORDFLAG);
        if (psdFlag != null) {
            if (psdFlag.equals(Constant.Purse.PASSWORDINPUTNEW)) {
                //短信修改密码
                getCode = intent.getStringExtra(Constant.Purse.PSD_VERIFY_FLAG);
            }
            fragments.put(R.id.card_bound, PasswordSetFragment.newInstance(psdFlag, ""));
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_bound);
        initViews();
        initEvents();
//        init();
    }

    @Override
    public Map<Integer, Fragment> myFragments() {
        return fragments;
    }

//    private void init() {
//    }

    private void initViews() {
        myTitleBarHelper = new MyTitleBarHelper(this, getWindow().getDecorView().findViewById(android.R.id.content));
        myTitleBarHelper.setLeftImgVisible(false);
        myTitleBarHelper.resetState();
//        myTitleBarHelper.setRightTxVisible(false);
        myTitleBarHelper.setLeftImag(R.mipmap.btn_back);
        if (psdFlag != null) {
            if (psdFlag.equals(Constant.Purse.PASSWORDMODIFY)) {
                //密码修改
                myTitleBarHelper.setMiddleText("修改支付密码");
                myTitleBarHelper.setRightText("下一步");
            } else if (psdFlag.equals(Constant.Purse.PASSWORDINPUTNEW)) {
                //密码修改
                myTitleBarHelper.setMiddleText("重置支付密码");
                myTitleBarHelper.setRightText("下一步");
            } else {
                //密码设置
                myTitleBarHelper.setMiddleText("请设置支付密码");
                myTitleBarHelper.setRightText("下一步");
            }
        }
    }

    private void initEvents() {
        myTitleBarHelper.setOnclickListener(this);
    }


    @Override
    public void onFragmentInteraction(Message mes) {
        switch (mes.arg1) {
            case Constant.Purse.PSDSET:
                //第一次进密码设置
                curPos = Constant.Purse.PSDSET;
                String tranStr = (String) mes.obj;
                passwordSetFragment = PasswordSetFragment.newInstance(Constant.Purse.PASSWORDSETAGAIN, tranStr);
                replaceFragmentWithStack(R.id.card_bound, passwordSetFragment, false, "PasswordSetFragment");
                myTitleBarHelper.setRightText("完成");
                break;
            case Constant.Purse.PSDMODIFY:
                //密码修改
                curPos = Constant.Purse.PSDMODIFY;
                String oldStr = (String) mes.obj;
                passwordSetFragment = PasswordSetFragment.newInstance(Constant.Purse.PASSWORDINPUTNEW, oldStr);
                replaceFragmentWithStack(R.id.card_bound, passwordSetFragment, false, "PasswordSetFragment02");
                myTitleBarHelper.setRightText("下一步");
                break;
            case Constant.Purse.PSDINPUTNEW:
                //密码修改
                curPos = Constant.Purse.PSDINPUTNEW;
                String tranStr1 = (String) mes.obj;
                if (!TextUtils.isEmpty(getCode)) {
                    //短信修改密码
                    tranStr1 += "," + getCode + ",1";
                } else {
                    //通过旧密码修改密码
                    tranStr1 += ",0";
                }
                passwordSetFragment = PasswordSetFragment.newInstance(Constant.Purse.PASSWORDINPUTNEWAGAIN, tranStr1);
                replaceFragmentWithStack(R.id.card_bound, passwordSetFragment, false, "PasswordSetFragment03");
                myTitleBarHelper.setRightText("完成");
                break;
            case Constant.Purse.PSDINPUTNEWAGAIN:
                //密码修改
                curPos = Constant.Purse.PSDINPUTNEWAGAIN;
                if (PursePsdModifyActivity.Act != null) {
                    PursePsdModifyActivity.Act.finish();
                }
                finish();
                break;
            case Constant.Purse.PSDSETAGAIN:
                //密码设置成功
                if (mes.obj != null) {
                    Intent intent = getIntent();
                    intent.putExtra(PSDMD5, MD5Tool.MD5PsdSet((String) mes.obj));
                    setResult(RESULT_OK, intent);
                }
                finish();
                break;
        }
        super.onFragmentInteraction(mes);
    }

    @Override
    public void onBackPressed() {
        backFragment();
    }

    private void backFragment() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            if (curPos == Constant.Purse.PSDSET || curPos == Constant.Purse.PSDMODIFY || curPos == Constant.Purse.PSDINPUTNEW) {
                myTitleBarHelper.setRightText("下一步");
            } else {
                myTitleBarHelper.setRightText("完成");
            }
            getSupportFragmentManager().popBackStack();
        }
        if (psdFlag.equals(Constant.Purse.PASSWORDMODIFY) || psdFlag.equals(Constant.Purse.PASSWORDINPUTNEW)) {
            createDialog(PursePsdManagerActivity.this, 0);
        } else {
            createDialog(PursePsdManagerActivity.this, 1);
        }
    }

    private AlertDialog dialog;
//    private TextView tv_reminder;

    private void createDialog(Context context, int type) {
        View view = LayoutInflater.from(context).inflate(R.layout.psd_error, null);
        dialog = new AlertDialog.Builder(context).setView(view).create();
        dialog.show();
        TextView tv_reminder = (TextView) view.findViewById(R.id.tv_reminder);
        if (type == 0) {
            tv_reminder.setText("是否放弃修改支付密码?");
        } else {
            tv_reminder.setText("是否放弃设置支付密码?");
        }
        TextView tv_rest_times = (TextView) view.findViewById(R.id.tv_rest_times);
        tv_rest_times.setVisibility(View.INVISIBLE);
        //否按钮
        TextView tv_ok = (TextView) view.findViewById(R.id.tv_ok);
        tv_ok.setText("否");
        tv_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        //是按钮
        TextView tv_forget_psd = (TextView) view.findViewById(R.id.tv_forget_psd);
        tv_forget_psd.setText("是");
        tv_forget_psd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                finish();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_title_bar_left:
                backFragment();
                break;
            case R.id.tv_title_bar_right:
                getVisibleFragment().titleBarRightTxtEvent();
                break;
            default:
                break;
        }
    }

    public BaseFragment getVisibleFragment() {
        FragmentManager fragmentManager = PursePsdManagerActivity.this.getSupportFragmentManager();
        List<Fragment> fragments = fragmentManager.getFragments();
        for (Fragment fragment : fragments) {
            if (fragment != null && fragment.isVisible())
                return (BaseFragment) fragment;
        }
        return null;
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
                if (requestTag.equals(Constant.ShouYeUrl.GOODSBACK)) {
                    BaseDataBean<Object> baseDataBean = JsonUtil.parseDataObject(responseInfo.result, Object.class);
                    if (baseDataBean.code == 100) {
                        toast("提交成功");
                        Intent intent = new Intent();
//                    intent.putExtra(ServiceRepairActivity.REPAIR_BACK_INFO, rBean);
                        setResult(RESULT_OK, intent);
                        PursePsdManagerActivity.this.finish();
                    } else {
                        toast(baseDataBean.msg);
                    }
                }
            }

            @Override
            public void onFailure(HttpException error, String msg, String requestTag) {
                NetWorkStateUtils.errorNetMes(PursePsdManagerActivity.this);
            }
        };
    }
}
