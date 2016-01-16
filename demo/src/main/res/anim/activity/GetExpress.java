package anim.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.asyey.footballlibrary.network.exception.HttpException;
import com.asyey.footballlibrary.network.http.ResponseInfo;
import com.henanjianye.soon.communityo2o.BaseActivity;
import com.henanjianye.soon.communityo2o.R;
import com.henanjianye.soon.communityo2o.common.Constant;
import com.henanjianye.soon.communityo2o.common.enties.BaseDataBean;
import com.henanjianye.soon.communityo2o.common.enties.ExpressPickUpCheckBean;
import com.henanjianye.soon.communityo2o.common.enties.JsonUtil;
import com.henanjianye.soon.communityo2o.interf.NetWorkCallback;
import com.henanjianye.soon.communityo2o.view.TransDialog;
import com.henanjianye.soon.communityo2o.zxing.ui.CaptureActivity;

import java.util.HashMap;

/**
 * 领取快递
 */
public class GetExpress  extends BaseActivity{
    private ImageView iv_title_bar_left;
    private ImageView saomiao;
    private  final int ENTRYEXPRESS=200;//录入快递的标志
    private TextView getexpress;
    private EditText et_danhao;
    private String danhao;
    private ImageView iv_delect;
    @Override
    public int mysetContentView() {
        return R.layout.activity_getexpress;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initView();
        setListener();
        processLogic();
    }

    private void initView() {
        TextView tv_title_bar_middle = (TextView) findViewById(R.id.tv_title_bar_middle);
        tv_title_bar_middle.setText("领取快递");

        iv_delect = (ImageView) findViewById(R.id.iv_delect);
        et_danhao = (EditText) findViewById(R.id.et_danhao);
        getexpress = (TextView) findViewById(R.id.getexpress);
        saomiao = (ImageView) findViewById(R.id.saomiao);
        iv_title_bar_left = (ImageView) findViewById(R.id.iv_title_bar_left);
    }

    private void setListener() {
       iv_title_bar_left.setOnClickListener(this);
        saomiao.setOnClickListener(this);
        getexpress.setOnClickListener(this);
        iv_delect.setOnClickListener(this);

    }

    private void processLogic() {
        et_danhao.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                iv_delect.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()){
            case R.id.iv_title_bar_left:
                GetExpress.this.finish();
            break;
            case R.id.saomiao:
                Intent intent=new Intent(GetExpress.this, CaptureActivity.class);
                startActivityForResult(intent, ENTRYEXPRESS);
            break;
            case R.id.getexpress://领取

                danhao = et_danhao.getText().toString();
                if(TextUtils.isEmpty(danhao)){
                   toast("快递单号不能为空");
                }else{
                    //领取快递检查
                    requestPickUpCheck();
                }

            break;

            case R.id.iv_delect:

                et_danhao.setText("");
                iv_delect.setVisibility(View.GONE);
                break;
            default:
                break;

        }
    }

    private void requestPickUpCheck() {
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put("qrCode", danhao);//快递单号
        if (!TextUtils.isEmpty(Constant.Express.EXPRESS_PICKUPCHECK)) {

            getNetRequestHelper(this).isShowProgressDialog(true).postRequest(Constant.Express.EXPRESS_PICKUPCHECK, requestParams,
                    Constant.Express.EXPRESS_PICKUPCHECK);
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
                if(requestTag==Constant.Express.EXPRESS_PICKUPCHECK){
                    parsepickUpInFo(responseInfo.result);
                }else if(requestTag==Constant.Express.EXPRESS_PICKUPSAVE){
                  parsepickUpSaveInFo(responseInfo.result);

                }

            }

            @Override
            public void onFailure(HttpException error, String msg, String requestTag) {
                toast(msg);

            }
        };
    }

    private void parsepickUpSaveInFo(String result) {
        BaseDataBean<ExpressPickUpCheckBean> json= JsonUtil.parseDataObject(result, ExpressPickUpCheckBean.class);
        if(json.code==100){
            toast(json.msg);
            et_danhao.setText("");
        }else{
            toast(json.msg);
        }
    }

    private void parsepickUpInFo(String result) {
        BaseDataBean<ExpressPickUpCheckBean> json= JsonUtil.parseDataObject(result, ExpressPickUpCheckBean.class);
        if(json.code==100){
            String userName = json.data.userName;
            View view=getLayoutInflater().inflate(R.layout.dialog_entryexpress,null);
            TextView expressdialog_titile = (TextView) view.findViewById(R.id.expressdialog_titile);
            expressdialog_titile.setText("快递领取人信息确认");
            TextView tv_name = (TextView) view.findViewById(R.id.tv_name);
            tv_name.setText("收件人 ："+userName);
            final TransDialog dialog=new TransDialog(GetExpress.this,0,view);
            TextView tv_cancel = (TextView) view.findViewById(R.id.tv_cancel);
            TextView tv_ok = (TextView) view.findViewById(R.id.tv_ok);
            tv_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            tv_ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //领取快递
                    qurestSaveInfo();
                    dialog.dismiss();
                }
            });
            dialog.show();


        }else{
            toast(json.msg);
        }
    }

    private void qurestSaveInfo() {
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put("qrCode", danhao);//二维码号
        if (!TextUtils.isEmpty(Constant.Express.EXPRESS_PICKUPSAVE)) {

            getNetRequestHelper(this).isShowProgressDialog(true).postRequest(Constant.Express.EXPRESS_PICKUPSAVE, requestParams,
                    Constant.Express.EXPRESS_PICKUPSAVE);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data!=null){
            switch (resultCode){
                case ENTRYEXPRESS:
                    String barcode = data.getExtras().getString("barcode");
                    if(!TextUtils.isEmpty(barcode)){
                        try {
                            if(barcode.contains("#")){
                                String[] split = barcode.split("#");
                                barcode= split[1];
                                et_danhao.setText(barcode);
                            }else{
                                et_danhao.setText(barcode);
                            }

                        }catch (Exception e){

                        }

                    }
                    break;
                default:
                    break;
            }

        }
    }
}
