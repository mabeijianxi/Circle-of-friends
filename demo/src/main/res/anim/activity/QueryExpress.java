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
import com.henanjianye.soon.communityo2o.common.enties.ExpressDetailBeforeBean;
import com.henanjianye.soon.communityo2o.common.enties.JsonUtil;
import com.henanjianye.soon.communityo2o.common.util.CommonUtils;
import com.henanjianye.soon.communityo2o.interf.NetWorkCallback;
import com.henanjianye.soon.communityo2o.zxing.ui.CaptureActivity;

import java.util.HashMap;

/**
 * 查询快递页面
 */
public class QueryExpress  extends BaseActivity{
    private ImageView saomiao;
    private final int QUERYEXPRESS =201;//查询快递的标志
    private  final int ENTRYEXPRESS=200;//录入快递的标志
    private EditText et_danhao;
    private ImageView iv_title_bar_left;
    private TextView tv_search;
    private TextView companyname;
    private int companyId;
    private ImageView iv_delect;
    private String danhao;
    private String comCode;
    @Override
    public int mysetContentView() {
        return R.layout.activity_queryexpress;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        setListener();
        processLogic();
    }
    private void initView() {
        iv_title_bar_left = (ImageView) findViewById(R.id.iv_title_bar_left);
        et_danhao = (EditText) findViewById(R.id.et_danhao);
        saomiao = (ImageView) findViewById(R.id.saomiao);

        iv_delect = (ImageView) findViewById(R.id.iv_delect);
        companyname = (TextView) findViewById(R.id.companyname);
        tv_search = (TextView) findViewById(R.id.tv_search);
        TextView tv_title_bar_middle = (TextView) findViewById(R.id.tv_title_bar_middle);
        tv_title_bar_middle.setText("查询快递");

    }

    private void setListener() {
        saomiao.setOnClickListener(this);
        iv_title_bar_left.setOnClickListener(this);
        tv_search.setOnClickListener(this);
        companyname.setOnClickListener(this);
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
        switch (v.getId()) {
            case R.id.saomiao:
                Intent saomiao=new Intent(QueryExpress.this, CaptureActivity.class);
                startActivityForResult(saomiao, ENTRYEXPRESS);
                break;
            case R.id.iv_title_bar_left:
                QueryExpress.this.finish();
                break;
            case R.id.tv_search ://查询

                danhao = et_danhao.getText().toString();
                String name = companyname.getText().toString();
                if(TextUtils.isEmpty(danhao)||name.contains("请选择")){
                    toast("您输入的信息不能为空");
                    return;

                }else{
                    requestExpress();

                }
                break;
            case R.id.companyname :
                Intent company=new Intent(QueryExpress.this, ExpressCompanyActivity.class);
                startActivityForResult(company, QUERYEXPRESS);
            break;
            case R.id.iv_delect:
                et_danhao.setText("");
                iv_delect.setVisibility(View.GONE);
                break;

            default:
                break;
        }
    }

    private void requestExpress() {
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put("expressNum", danhao);//快递单号
        requestParams.put("comCode", comCode);//快递公司id
        if (!TextUtils.isEmpty(Constant.Express.EXPRESS_BeFOREDETAIL)) {

            getNetRequestHelper(this).isShowProgressDialog(true).postRequest(Constant.Express.EXPRESS_BeFOREDETAIL, requestParams,
                    Constant.Express.EXPRESS_BeFOREDETAIL);
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
                if(requestTag== Constant.Express.EXPRESS_BeFOREDETAIL){
                    parseInFo(responseInfo.result);
                }

            }

            @Override
            public void onFailure(HttpException error, String msg, String requestTag) {
                toast(msg);
            }
        };
    }

    private void parseInFo(String result) {
        MyLog.e("query-----", result.toString());
        BaseDataBean<ExpressDetailBeforeBean> json= JsonUtil.parseDataObject(result, ExpressDetailBeforeBean.class);
        try {

            if(json.code==100){
                boolean checkResult= json.data.checkResult;
                String messageText = json.data.messageText;
                if(checkResult==true){
                    //toast("查询");//companyId//http://123.57.162.168:8080/jyo2o_web//app/view/express/detail.htm?expressNum=471217257147&comCode=4
                    CommonUtils.startWebViewActivity(this, "查询结果", Constant.Express.EXPRESS_DETAIL + "?expressNum=" + danhao + "&comCode=" + comCode);

                }else{
                    if(!TextUtils.isEmpty(messageText)){
                        toast(messageText);
                    }

                }

            }else{
                toast(json.msg);
            }

        }catch (Exception e){

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
                        et_danhao.setText(barcode);
                    }
                    break;
                case RESULT_OK://快递公司的名称 id
                    Bundle bundle = data.getExtras();
                    String companyName = bundle.getString(ExpressCompanyActivity.COMPANY_NAME);

                    companyId = bundle.getInt(ExpressCompanyActivity.COMPANY_ID);

                    comCode = bundle.getString(ExpressCompanyActivity.COMPANY_CODE);
                    MyLog.e("111", comCode + "2222222222");
                    if(!TextUtils.isEmpty(companyName)){
                        companyname.setText(companyName);
                        iv_delect.setVisibility(View.GONE);
                    }

                    break;

                default:
                    break;
            }

        }
    }
}
