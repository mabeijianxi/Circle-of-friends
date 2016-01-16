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
import com.henanjianye.soon.communityo2o.common.MyLog;
import com.henanjianye.soon.communityo2o.common.enties.BaseDataBean;
import com.henanjianye.soon.communityo2o.common.enties.ExpressInputCheckBean;
import com.henanjianye.soon.communityo2o.common.enties.JsonUtil;
import com.henanjianye.soon.communityo2o.interf.NetWorkCallback;
import com.henanjianye.soon.communityo2o.view.TransDialog;
import com.henanjianye.soon.communityo2o.zxing.ui.CaptureActivity;

import java.util.HashMap;

/**
 * 录入快递
 */
public class EntryExpress  extends BaseActivity{
    private TextView tv_entryexpress;
    private ImageView saomiao;
    private EditText express_num;
    private  final int ENTRYEXPRESS=200;//录入快递的标志
    private ImageView iv_title_bar_left;
    private EditText et_phone;
    private TextView tv_name_kuaidi;
    private String num;
    private String phone;
    private  int companyId;
    private ImageView iv_delect;
    private ImageView iv_phone_delect;
    @Override
    public int mysetContentView() {
        return R.layout.activity_entryexpress;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        setListener();
        processLogic();
    }
    private void initView() {


        express_num = (EditText) findViewById(R.id.express_num);

        tv_name_kuaidi = (TextView) findViewById(R.id.tv_name_kuaidi);
        tv_entryexpress = (TextView) findViewById(R.id.tv_entryexpress);
        TextView tv_title_bar_middle = (TextView) findViewById(R.id.tv_title_bar_middle);
        tv_title_bar_middle.setText("录入快递");
        iv_title_bar_left = (ImageView) findViewById(R.id.iv_title_bar_left);
        saomiao = (ImageView) findViewById(R.id.saomiao);

        iv_phone_delect = (ImageView) findViewById(R.id.iv_phone_delect);
        iv_delect = (ImageView) findViewById(R.id.iv_delect);
        et_phone = (EditText) findViewById(R.id.et_phone);


    }
    private void processLogic() {
        express_num.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                iv_delect.setVisibility(View.VISIBLE);
                iv_phone_delect.setVisibility(View.GONE);
            }
        });
        et_phone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                iv_phone_delect.setVisibility(View.VISIBLE);
                iv_delect.setVisibility(View.GONE);

            }
        });
    }

    private void setListener() {
        tv_entryexpress.setOnClickListener(this);
        saomiao.setOnClickListener(this);
        iv_title_bar_left.setOnClickListener(this);
        tv_name_kuaidi.setOnClickListener(this);
        iv_delect.setOnClickListener(this);
        iv_phone_delect.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        super.onClick(v);

        switch (v.getId()) {
            case R.id.tv_entryexpress://查询

                num = express_num.getText().toString();

                phone = et_phone.getText().toString();
                String name = tv_name_kuaidi.getText().toString();
                if(TextUtils.isEmpty(num)|| TextUtils.isEmpty(phone)||name.startsWith("请选择快递公司")){
                    toast("您输入的信息不能为空");
                }else{
                    repuestcheck(num, phone);

                }

                break;
            case R.id.saomiao :
                Intent intent=new Intent(EntryExpress.this, CaptureActivity.class);
                startActivityForResult(intent, ENTRYEXPRESS);
                break;
            case R.id.iv_title_bar_left :
                EntryExpress.this.finish();
                break;
            case R.id.tv_name_kuaidi://选择快递公司
                    Intent comact=new Intent(EntryExpress.this,ExpressCompanyActivity.class);
                    startActivityForResult(comact,11);
                break;
            case R.id.iv_delect:
                express_num.setText("");
                iv_delect.setVisibility(View.GONE);
                break;
            case R.id.iv_phone_delect:
                et_phone.setText("");
                iv_phone_delect.setVisibility(View.GONE);
                break;

            default:
                break;
        }
    }
        //录入快递检查
    private void repuestcheck(String num, String phone) {
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put("expressNum", num);//快递单号
        requestParams.put("companyId", companyId);//快递公司id
        requestParams.put("packagePhone", phone);//收件人电话
        if (!TextUtils.isEmpty(Constant.Express.EXPRESS_INPUTCHECK)) {

            getNetRequestHelper(this).isShowProgressDialog(true).postRequest(Constant.Express.EXPRESS_INPUTCHECK, requestParams,
                    Constant.Express.EXPRESS_INPUTCHECK);
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
                if(requestTag==Constant.Express.EXPRESS_INPUTCHECK){
                    parseInfo(responseInfo.result);
                    MyLog.e("responseInfo.result-", responseInfo.result.toString());
                }else if(requestTag==Constant.Express.EXPRESS_INPUTSAVE){
                    BaseDataBean<ExpressInputCheckBean> savejson= JsonUtil.jsonObject(responseInfo.result, ExpressInputCheckBean.class, requestTag);
                    if(savejson.code==100){
                        express_num.setText("");
                        et_phone.setText("");
                        toast(savejson.msg);
                    }
                }

            }

            @Override
            public void onFailure(HttpException error, String msg, String requestTag) {
                    toast(msg);
            }
        };
    }

    private void parseInfo( String result) {
        BaseDataBean<ExpressInputCheckBean> json= JsonUtil.parseDataObject(result, ExpressInputCheckBean.class);
        if(json.code==100){
            MyLog.e("json---",json.toString());
           String packageUserName = json.data.packageUserName;
            View view=getLayoutInflater().inflate(R.layout.dialog_entryexpress,null);
            final TransDialog dialog=new TransDialog(EntryExpress.this,0,view);
            TextView tv_name = (TextView) view.findViewById(R.id.tv_name);
            tv_name.setText("收件人 ："+packageUserName);
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
                    //录入快递保存
                    querstSaveInfo();
                    dialog.dismiss();

                }
            });
            dialog.show();
        }else{
            toast(json.msg);
        }

    }

    private void querstSaveInfo() {
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put("expressNum", num);//快递单号
        requestParams.put("companyId", companyId);//快递公司id
        requestParams.put("packagePhone", phone);//收件人电话
        if (!TextUtils.isEmpty(Constant.Express.EXPRESS_INPUTSAVE)) {

            getNetRequestHelper(this).isShowProgressDialog(true).postRequest(Constant.Express.EXPRESS_INPUTSAVE, requestParams,
                    Constant.Express.EXPRESS_INPUTSAVE);
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
                        express_num.setText(barcode);
                    }
                break;
                case RESULT_OK://快递公司
                    Bundle bundle = data.getExtras();
                    String companyName = bundle.getString(ExpressCompanyActivity.COMPANY_NAME);
                    companyId = bundle.getInt(ExpressCompanyActivity.COMPANY_ID);
                    if(!TextUtils.isEmpty(companyName)){
                        tv_name_kuaidi.setText(companyName);
                    }
                    iv_delect.setVisibility(View.GONE);
                    iv_phone_delect.setVisibility(View.GONE);

                    break;
            }

        }
    }
    private boolean isPhoneNum(String str) {
        if(str.toString().length()==11){
            return true;
        }else{
            return false;
        }
       // return str.matches("^1[3|4|5|7|9|6|2|8][0-9]\\d{8}$");
    }
}
