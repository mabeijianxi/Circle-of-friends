package anim.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.asyey.footballlibrary.network.exception.HttpException;
import com.asyey.footballlibrary.network.http.ResponseInfo;
import com.henanjianye.soon.communityo2o.BaseActivity;
import com.henanjianye.soon.communityo2o.R;
import com.henanjianye.soon.communityo2o.common.Constant;
import com.henanjianye.soon.communityo2o.common.MyLog;
import com.henanjianye.soon.communityo2o.common.enties.BaseDataBean;
import com.henanjianye.soon.communityo2o.common.enties.JsonUtil;
import com.henanjianye.soon.communityo2o.common.enties.RepairToCommnetBean;
import com.henanjianye.soon.communityo2o.common.util.CommonUtils;
import com.henanjianye.soon.communityo2o.common.util.NetWorkStateUtils;
import com.henanjianye.soon.communityo2o.interf.NetWorkCallback;
import com.henanjianye.soon.communityo2o.view.EmojiParseUtils;

import java.util.HashMap;

/**
 * 报修评论界面
 */
public class ServiceRepairToComment extends BaseActivity {
    private RatingBar rabar_quality;
    private RatingBar rabar_attitude;
    private RatingBar rabar_rate;
    private TextView btn_ok;
    private ImageView iv_title_bar_left;
    private TextView tv_title_bar_middle;
    private EditText et_comment;
    private  int qualitynumStars;
    private  int ratenumStars;
    private  int  attitudenumStars;
    private String myrid;
    private  int id;
    @Override
    public int mysetContentView() {
        return R.layout.servicepairtocomment_activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        setListener();
        processLogic();

    }



    private void initView() {
         myrid= getIntent().getStringExtra("myrid");

        id = getIntent().getIntExtra("id",-1);
        String descriptions= getIntent().getStringExtra("descriptions");
        TextView tv_name = (TextView) findViewById(R.id.tv_name);
        tv_title_bar_middle = (TextView) findViewById(R.id.tv_title_bar_middle);
        iv_title_bar_left = (ImageView) findViewById(R.id.iv_title_bar_left);
        rabar_quality = (RatingBar) findViewById(R.id.rabar_quality);
        rabar_rate = (RatingBar) findViewById(R.id.rabar_rate);
        rabar_attitude = (RatingBar) findViewById(R.id.rabar_attitude);
        btn_ok = (TextView) findViewById(R.id.btn_ok);

        et_comment = (EditText) findViewById(R.id.et_comment);
        tv_name.setText("报修："+descriptions);
        tv_title_bar_middle.setText("我要评价");

    }
    private void processLogic() {
    }

    private void setListener() {
        btn_ok.setOnClickListener(this);
        iv_title_bar_left.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()){
            case R.id.btn_ok:
                qualitynumStars = (int) rabar_quality.getRating();
                ratenumStars = (int) rabar_rate.getRating();
                attitudenumStars = (int) rabar_attitude.getRating();
                MyLog.e("rabar_attitude",attitudenumStars+"");

                if(qualitynumStars==0){
                    toast("请点评完成质量");
                    return;
                }
                if(ratenumStars==0){
                    toast("请点评响应速度");
                    return;
                }
                if(attitudenumStars==0){
                    toast("请点评服务态度");
                    return;
                }

                if(CommonUtils.isNetworkConnected(ServiceRepairToComment.this)){
                    //TODO  请求接口
                    postComment();
                }else{
                    toast("网络未连接或不可用");
                }
            break;
            case R.id.iv_title_bar_left:
                ServiceRepairToComment.this.finish();
            break;
            default:
                break;

        }
    }

    private void postComment() {
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("grade1",qualitynumStars);
        params.put("grade2",ratenumStars);
        params.put("grade3",attitudenumStars);
        params.put("repairId", myrid);
        String pinglun=EmojiParseUtils.sendToServer(et_comment.getText().toString(), ServiceRepairToComment.this);
        if(pinglun.contains("[e]")&&pinglun.contains("[/e]")){
            toast("评论内容不可输入特殊的字符及表情");
            return ;
        }
        params.put("content",pinglun);
        if (!TextUtils.isEmpty(Constant.Repair.REPAIR_COMMENT)) {
            getNetRequestHelper(this).postRequest(Constant.Repair.REPAIR_COMMENT, params,
                    Constant.Repair.REPAIR_COMMENT);
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
                if(requestTag== Constant.Repair.REPAIR_COMMENT){
                    MyLog.e("responseInfo----",responseInfo.result.toString());
                    BaseDataBean<RepairToCommnetBean> parseDataObject = JsonUtil.parseDataObject(responseInfo.result.toString(), RepairToCommnetBean.class);
                    if(parseDataObject.code==100){
                        toast(parseDataObject.msg);
                        Intent Intent=new Intent();
                        Intent.putExtra("idcode", id);
                        setResult(RESULT_OK, Intent);
                        ServiceRepairToComment.this.finish();
                    }else{
                        //TODO...评论失败提示
                        toast(parseDataObject.msg);
                    }
                }

            }

            @Override
            public void onFailure(HttpException error, String msg, String requestTag) {
                NetWorkStateUtils.errorNetMes(ServiceRepairToComment.this);

            }
        };
    }
}
