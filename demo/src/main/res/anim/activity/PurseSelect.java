package anim.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.asyey.footballlibrary.network.exception.HttpException;
import com.asyey.footballlibrary.network.http.ResponseInfo;
import com.henanjianye.soon.communityo2o.R;
import com.henanjianye.soon.communityo2o.common.Constant;
import com.henanjianye.soon.communityo2o.common.UserDataHelper;
import com.henanjianye.soon.communityo2o.common.enties.AccountBean;
import com.henanjianye.soon.communityo2o.common.enties.BaseDataBean;
import com.henanjianye.soon.communityo2o.common.enties.JsonUtil;
import com.henanjianye.soon.communityo2o.common.util.CommonUtils;
import com.henanjianye.soon.communityo2o.common.util.NetRequestHelper;
import com.henanjianye.soon.communityo2o.interf.NetWorkCallback;

public class PurseSelect extends Activity implements View.OnClickListener {
    private Button btn_purse_intro, btn_psd_manager, btn_cancel;
    private LinearLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purse_select);
        initViews();
        initEvents();
    }

    private void getPsdSetStatus() {
        if (CommonUtils.isNetworkConnected(this)) {
            requestCheckSetStatus();
        } else {
            Toast.makeText(this, "网络不可用", Toast.LENGTH_SHORT).show();
        }
    }

    private void requestCheckSetStatus() {
        new UserDataHelper(this).getSetPsdStatus(new NetRequestHelper(this, setNetWorkCallback()).isShowProgressDialog(true));
    }


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
                btn_psd_manager.setClickable(true);
                if (Constant.Purse.PAYPSDSETSTATUS.equals(requestTag)) {
                    parsePsdSetStatus(responseInfo.result);
                }
            }

            @Override
            public void onFailure(HttpException error, String msg, String requestTag) {
                btn_psd_manager.setClickable(true);
                Toast.makeText(PurseSelect.this, msg, Toast.LENGTH_SHORT).show();
            }
        };
    }

    private void parsePsdSetStatus(String result) {
        try {
            BaseDataBean<AccountBean> json = JsonUtil.parseDataObject(result, AccountBean.class);
            if (json.code == 100) {
                AccountBean aBean = json.data;
                if (aBean != null) {
                    if (aBean.flag) {
                        //已设置过密码
                        startActivity(new Intent(PurseSelect.this, PursePsdModifyActivity.class));
                        finish();
                    } else {
                        //还没有设置过密码
                        createDialogToAddPsw(PurseSelect.this);
//                        Intent intent = new Intent(PurseSelect.this, PursePsdManagerActivity.class);
//                        intent.putExtra(Constant.Purse.PASSWORDFLAG, Constant.Purse.PASSWORDSET);
//                        startActivity(intent);
                    }
                }
            } else {
                Toast.makeText(this, "数据异常，请稍后重试", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createDialogToAddPsw(final Activity context) {
        View view = LayoutInflater.from(context).inflate(R.layout.dia_psd_input, null);
        final AlertDialog dialog = new AlertDialog.Builder(context).setView(view).create();
        dialog.show();
        final EditText et_psw_jycion = (EditText) view.findViewById(R.id.et_psw_jycion);
        et_psw_jycion.setVisibility(View.GONE);
        TextView tv_reminder = (TextView) view.findViewById(R.id.tv_reminder);
        tv_reminder.setText("您还没有设置过支付密码是否跳转设置密码？");
        //确定按钮
        final TextView tv_ok = (TextView) view.findViewById(R.id.tv_ok);
        tv_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //还没有设置过密码
                Intent intent = new Intent(context, PursePsdManagerActivity.class);
                intent.putExtra(Constant.Purse.PASSWORDFLAG, Constant.Purse.PASSWORDSET);
                context.startActivity(intent);
                dialog.dismiss();
                finish();
            }
        });
        //忘记密码
        TextView tv_forget_psd = (TextView) view.findViewById(R.id.tv_forget_psd);
        tv_forget_psd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO 取消对话框
                dialog.dismiss();
            }
        });
    }

    private void initEvents() {
        //添加选择窗口范围监听可以优先获取触点，即不再执行onTouchEvent()函数，点击其他地方时执行onTouchEvent()函数销毁Activity
        layout.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
//                // TODO Auto-generated method stub
//                Toast.makeText(getApplicationContext(), "提示：点击窗口外部关闭窗口！",
//                        Toast.LENGTH_SHORT).show();
            }
        });
        //添加按钮监听
        btn_cancel.setOnClickListener(this);
        btn_purse_intro.setOnClickListener(this);
        btn_psd_manager.setOnClickListener(this);
    }

    private void initViews() {
        btn_purse_intro = (Button) this.findViewById(R.id.btn_purse_intro);
        btn_psd_manager = (Button) this.findViewById(R.id.btn_psd_manager);
        btn_cancel = (Button) this.findViewById(R.id.btn_cancel);
        layout = (LinearLayout) findViewById(R.id.pop_layout);
    }

    //实现onTouchEvent触屏函数但点击屏幕时销毁本Activity
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        finish();
        return true;
    }


    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_purse_intro:
                CommonUtils.startWebViewActivity(PurseSelect.this, "钱包说明", Constant.Purse.PURSEINTRODUCE);
                finish();
                break;
            case R.id.btn_psd_manager:
                if (CommonUtils.isNetworkConnected(this)) {
                    btn_psd_manager.setClickable(false);
                    getPsdSetStatus();
                } else {
                    btn_psd_manager.setClickable(true);
                    Toast.makeText(this, "网络未连接", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_cancel:
                finish();
                break;
            default:
                break;
        }
    }
}
