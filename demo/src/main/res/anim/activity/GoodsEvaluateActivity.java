package anim.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;

import com.asyey.footballlibrary.network.exception.HttpException;
import com.asyey.footballlibrary.network.http.ResponseInfo;
import com.asyey.footballlibrary.universalimageloader.core.DisplayImageOptions;
import com.asyey.footballlibrary.universalimageloader.core.ImageLoader;
import com.henanjianye.soon.communityo2o.BaseActivity;
import com.henanjianye.soon.communityo2o.R;
import com.henanjianye.soon.communityo2o.common.Constant;
import com.henanjianye.soon.communityo2o.common.CropImgAsyctask;
import com.henanjianye.soon.communityo2o.common.CropImgAsyctask.Listener;
import com.henanjianye.soon.communityo2o.common.MyLog;
import com.henanjianye.soon.communityo2o.common.UserDataHelper;
import com.henanjianye.soon.communityo2o.common.enties.BaseDataBean;
import com.henanjianye.soon.communityo2o.common.enties.EvaluateBean;
import com.henanjianye.soon.communityo2o.common.enties.EvaluateListBean;
import com.henanjianye.soon.communityo2o.common.enties.JsonUtil;
import com.henanjianye.soon.communityo2o.common.util.CommonUtils;
import com.henanjianye.soon.communityo2o.common.util.JianPanUtils;
import com.henanjianye.soon.communityo2o.common.util.MyTitleBarHelper;
import com.henanjianye.soon.communityo2o.common.util.NetWorkStateUtils;
import com.henanjianye.soon.communityo2o.common.view.CustomProgressDialog;
import com.henanjianye.soon.communityo2o.interf.NetWorkCallback;
import com.henanjianye.soon.communityo2o.submitphote.AddImageListAdapter;
import com.henanjianye.soon.communityo2o.submitphote.AddImageView;
import com.henanjianye.soon.communityo2o.submitphote.ImagePagerActivity;
import com.henanjianye.soon.communityo2o.submitphote.ImageUtils;
import com.henanjianye.soon.communityo2o.submitphote.PhotoInfo;
import com.henanjianye.soon.communityo2o.submitphote.PhotoSerializable;
import com.henanjianye.soon.communityo2o.submitphote.SelectPhotoActivity;
import com.henanjianye.soon.communityo2o.submitphote.SelectPicPopupWindow;
import com.henanjianye.soon.communityo2o.view.EmojiParseUtils;
import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GoodsEvaluateActivity extends BaseActivity implements View.OnClickListener, AdapterView.OnItemClickListener, AddImageView.OnAddListener, Listener {
    private MyTitleBarHelper myTitleBarHelper;
    private EditText et_evaluate;
    private AddImageView mAddImage;
    private Uri mPhotoUri;
    private String mProtraitPath;
    private SelectPicPopupWindow mMenuWindow;
    private AddImageListAdapter mAdapter;
    //图片路径
    private List<String> urls = new ArrayList<>();
    private Context mContext;
    private ImageView iv_evaluate_img;
    private RatingBar rb_rate;
    private CheckBox cb_is_anonymity;
    private int anonymous = 1;//是否匿名评价，0-否，1-是，默认为1
    private String evaluate_content;
    public static final String EVALUATEFLAG = "evaluate_flag";
    private boolean isEvaluated;
    private int goodsId;
    private int goodCardId;
    private String picUrl;
    public static final String GOODSID = "evaluate_goods_id";
    public static final String EVALUATESTATUS = "evaluate_status";
    public static final String EVALUATEGOODSCARTID = "evaluate_goodscart_id";
    public static final String PICTUREURL = "evaluate_picture_url";
    ImageLoader instance = ImageLoader.getInstance();
    DisplayImageOptions build = new DisplayImageOptions.Builder()
            .showImageOnFail(R.drawable.home_youpin)
            .showImageForEmptyUri(R.drawable.home_youpin).showImageOnLoading(R.drawable.home_youpin).build();
    private int evaluationId;
    public static final int EVALUATECODE = 0x100;

    //isEvaluated 是否评价过  goodsId商品ID  goodscartid商品购物车ID  picUrl评价商品的URL
    public static void TransferEvaluation(Activity context, boolean isEvaluated, int goodsid, int goodscartid, String picUrl) {
        Intent intent = new Intent(context, GoodsEvaluateActivity.class);
        Bundle bundle = new Bundle();
        bundle.putBoolean(EVALUATESTATUS, isEvaluated);
        bundle.putInt(GOODSID, goodsid);
        bundle.putInt(EVALUATEGOODSCARTID, goodscartid);
        bundle.putString(PICTUREURL, picUrl);
        intent.putExtras(bundle);
        context.startActivityForResult(intent, EVALUATECODE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goods_evaluate);
        mContext = this;
        Bundle bundle = getIntent().getExtras();
        isEvaluated = bundle.getBoolean(EVALUATESTATUS, false);
        goodCardId = bundle.getInt(EVALUATEGOODSCARTID, -1);
        goodsId = bundle.getInt(GOODSID, -1);
        picUrl = bundle.getString(PICTUREURL);
        initViews();
        initEvents();
        init();
    }


    private void init() {
        if (!TextUtils.isEmpty(picUrl)) {
            instance.displayImage(picUrl, iv_evaluate_img, build);
        } else {
            iv_evaluate_img.setImageResource(R.drawable.home_youpin);
        }
        mAdapter = new AddImageListAdapter(this);
        mAddImage.setAdapter(mAdapter);
        if (isEvaluated) {
            //请求评价过的信息
            new UserDataHelper(this).requestEvaluateData(getNetRequestHelper(mContext), goodCardId);
        }
    }

    private void initViews() {
        myTitleBarHelper = new MyTitleBarHelper(this, getWindow().getDecorView().findViewById(android.R.id.content));
        myTitleBarHelper.setLeftImgVisible(false);
        myTitleBarHelper.resetState();
        if (isEvaluated) {
            myTitleBarHelper.setMiddleText("追加图片评价");
        } else {
            myTitleBarHelper.setMiddleText("评价晒单");
        }
        myTitleBarHelper.setRightText("完成");
        myTitleBarHelper.setLeftImag(R.mipmap.btn_back);
        et_evaluate = (EditText) findViewById(R.id.et_evaluate);
        mAddImage = (AddImageView) findViewById(R.id.add_img);
        iv_evaluate_img = (ImageView) findViewById(R.id.iv_evaluate_img);
        rb_rate = (RatingBar) findViewById(R.id.rb_rate);
        cb_is_anonymity = (CheckBox) findViewById(R.id.cb_is_anonymity);
        if (isEvaluated) {
            //追加评价
            et_evaluate.setTextColor(getResources().getColor(R.color.light_grey_color));
            et_evaluate.setFocusable(false);
            et_evaluate.setFocusableInTouchMode(false);
            rb_rate.setIsIndicator(true);
            rb_rate.setNumStars(5);
            cb_is_anonymity.setClickable(false);
        } else {
            //没有评价过
        }
    }

    private void initEvents() {
        myTitleBarHelper.setOnclickListener(this);
        //拍照或者上传本地图片
        mAddImage.setOnItemClickListener(this);
        mAddImage.setOnAddListener(this);
        cb_is_anonymity.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    anonymous = 1;
                } else {
                    anonymous = 0;
                }
            }
        });

        et_evaluate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().length()==500){
                    toast("最多输入500个字符");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {


            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_title_bar_left:
                this.finish();
                break;
            case R.id.tv_title_bar_right:
                if (CommonUtils.isNetworkConnected(this)) {
                    if (!check_input()) {
                        return;
                    }
                    urls = mAdapter.getPhotoPathList();
                    CropImgAsyctask task = new CropImgAsyctask(this, this);
                    //执行图片压缩
                    if (Build.VERSION.SDK_INT >= 11) {
                        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, urls);
                    } else {
                        task.execute(urls);
                    }
                } else {
                    toast("网络未连接或不可用");
                }
                break;
            default:
                break;
        }
    }

    private CustomProgressDialog customProgressDialog;
    private List<String> CompressUrls = new ArrayList<>();

    @Override
    public void SST_OnBegin() {
        if (CompressUrls.size() > 0) {
            CompressUrls.clear();
        }
        customProgressDialog = CustomProgressDialog.createDialog(GoodsEvaluateActivity.this);
        if (customProgressDialog != null && !customProgressDialog.isShowing()) {
            customProgressDialog.show();
        }
    }

    @Override
    public void SST_OnDataReady(String data) {
        MyLog.e("MMM", "data is " + data);
        if (data != null && !data.equals("")) {
            CompressUrls.add(data);
        }
    }

    @Override
    public void SST_OnFinished(boolean bError) {
        if (customProgressDialog != null && customProgressDialog.isShowing()) {
            customProgressDialog.dismiss();
        }
        MyLog.e("MMM", "CompressUrls is " + CompressUrls.size());
        if (bError) {
            EvaluateBean eBean = new EvaluateBean();
            eBean.content = evaluate_content;
            eBean.goodsId = goodsId;
            eBean.goodsCartId = goodCardId;
            eBean.anonymous = anonymous;
            eBean.grade = (int) rb_rate.getRating();
            UserDataHelper userDataHelper = new UserDataHelper(getApplication());
            List<String> photoUrls = new ArrayList<>();
            if (CompressUrls != null && CompressUrls.size() > 0) {
                for (int i = 0; i < CompressUrls.size(); i++) {
                    String photo = CompressUrls.get(i);
                    if (photo.contains("file://")) {
                        photo = photo.replace("file://", "");
                    }
                    MyLog.e("MMM", "photo is " + photo);
                    photoUrls.add(photo);
                }
            }
            MyLog.e("MMM", "eBean is " + eBean.toString());
            if (!isEvaluated) {
                //第一次评价
                if (photoUrls.size() > 0) {
                    hasPic = true;
                } else {
                    hasPic = false;
                }
                userDataHelper.Submit_evaluate(getNetRequestHelper(mContext), eBean.goodsId,
                        eBean.goodsCartId, eBean.content, eBean.anonymous, eBean.grade, photoUrls);
            } else {
                //追加评价
                if (photoUrls.size() > 0) {
                    userDataHelper.SubmitExtraEvaluate(getNetRequestHelper(mContext), evaluationId, photoUrls);
                } else {
                    toast("未上传任何图片");
                }
            }
        }
    }

    private boolean hasPic;
    public static final String PICTUREFLAG = "picture_flag";

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
                MyLog.e("MMM", "responseInfo.result is " + responseInfo.result + ",requestTag is " + requestTag);
                if (requestTag.equals(Constant.ShouYeUrl.GOODSEVALUATE)) {
                    BaseDataBean<Object> baseDataBean = JsonUtil.parseDataObject(responseInfo.result, Object.class);
                    if (baseDataBean.code == 100) {
                        toast(baseDataBean.msg);
                        Intent intent = new Intent();
                        intent.putExtra(PICTUREFLAG, hasPic);
                        setResult(RESULT_OK, intent);
                        GoodsEvaluateActivity.this.finish();
                    } else {
                        toast(baseDataBean.msg);
                    }
                } else if (requestTag.equals(Constant.ShouYeUrl.GOODSEVALUATED)) {
                    parseEvaluatedData(responseInfo.result);
                } else if (requestTag.equals(Constant.ShouYeUrl.GOODSPICEXTRA)) {
                    BaseDataBean<Object> baseDataBean = JsonUtil.parseDataObject(responseInfo.result, Object.class);
                    if (baseDataBean.code == 100) {
                        toast(baseDataBean.msg);
                        Intent intent = new Intent();
                        intent.putExtra(PICTUREFLAG, true);
                        setResult(RESULT_OK,intent);
                        finish();
                    } else {
                        toast(baseDataBean.msg);
                    }
                }
            }

            @Override
            public void onFailure(HttpException error, String msg, String requestTag) {
                MyLog.e("MMM", "msg is " + msg);
                NetWorkStateUtils.errorNetMes(GoodsEvaluateActivity.this);
            }
        };
    }

    private void parseEvaluatedData(String result) {
        try {
            BaseDataBean<EvaluateListBean> json = JsonUtil.parseDataObject(result, EvaluateListBean.class);
            if (json.code == 100) {
                EvaluateListBean eBean = json.data;
                if (eBean != null && eBean.evaluations.size() > 0) {
                    EvaluateBean eBean1 = eBean.evaluations.get(0);
                    evaluationId = eBean1.evaluationId;
                    et_evaluate.setText(eBean1.content);
                    rb_rate.setRating(eBean1.grade);
                    if (eBean1.anonymous == 1) {
                        //匿名评论
                        cb_is_anonymity.setChecked(true);
                    } else {
                        //不匿名
                        cb_is_anonymity.setChecked(false);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean check_input() {
        evaluate_content= EmojiParseUtils.sendToServer(et_evaluate.getText().toString(),GoodsEvaluateActivity.this);
        MyLog.e("evaluate_content--",evaluate_content+"1");
        if (TextUtils.isEmpty(evaluate_content)) {
            toast("请输入评价");
            return false;
        }
        if(evaluate_content.contains("[e]")&&evaluate_content.contains("[/e]")){
            toast("评论内容不可输入特殊的字符及表情");
            return false;
        }
        return true;
    }


//    private void hideSoftBoard() {
//        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//        if (imm.isActive()) {
//            imm.hideSoftInputFromWindow(et_evaluate.getWindowToken(), 0); //强制隐藏键盘
//        }
//    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//        hideSoftBoard();
        JianPanUtils.hideIme(this);
        if (position == 0) {
            //点击加号
            if ((mAdapter.getCount() - 1) >= 3) {
                toast("上传图片的最大数量是3");
                return;
            }
            mMenuWindow = new SelectPicPopupWindow(this, mItemsOnClick);
            mMenuWindow.showAtLocation(getWindow().getDecorView(),
                    Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
        } else {
            //放大选中的图片
            Intent intent = new Intent(this, ImagePagerActivity.class);
            intent.putExtra(ImagePagerActivity.Extra.IMAGE_POSITION, position - 1);
            intent.putStringArrayListExtra(ImagePagerActivity.Extra.IMAGES,
                    mAdapter.getPhotoPathList());
            startActivity(intent);
        }
    }

    @Override
    public void onAdd() {
        JianPanUtils.hideIme(this);
        mMenuWindow = new SelectPicPopupWindow(this, mItemsOnClick);
        mMenuWindow.showAtLocation(getWindow().getDecorView(), Gravity.BOTTOM
                | Gravity.CENTER_HORIZONTAL, 0, 0);
    }

    private View.OnClickListener mItemsOnClick = new View.OnClickListener() {

        public void onClick(View v) {
            mMenuWindow.dismiss();
            switch (v.getId()) {
                case R.id.btn_take_photo:
                    startActionCamera();
                    break;
                case R.id.btn_pick_photo:
                    Intent intent = new Intent();
                    intent.setClass(GoodsEvaluateActivity.this,
                            SelectPhotoActivity.class);
                    intent.putExtra(SelectPhotoActivity.PHOTO_UPLOAD_MAX_NUM,
                            3);
                    intent.putExtra(SelectPhotoActivity.PHOTO_SELETED_PATH_LIST,
                            mAdapter.getPhotoPathList());
                    startActivityForResult(intent,
                            SelectPhotoActivity.ACTIVITY_REQUESTCODE);
                    break;
                default:
                    break;
            }
        }

    };
    //    public static final String KEY_RESULT = "result";
//    public static final String KEY_SUB_RESULT = "sub_result";
//    public static final String KEY_OPTIONS = "options";
//    public static final String KEY_SUB_OPTIONS = "sub_options";
    public static final int ACTIVITY_REQUESTCODE = 1;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case ACTIVITY_REQUESTCODE:// 调用选择列表返回的结构
                if (data == null) {
                    return;
                }
                break;
            case SelectPhotoActivity.ACTIVITY_REQUESTCODE:// 调用相册返回的结果
                if (data == null) {
                    return;
                }
                PhotoSerializable photoSerializable = (PhotoSerializable) data
                        .getSerializableExtra(PhotoSerializable.TAG);
                if (photoSerializable == null) {
                    return;
                }
                List<PhotoInfo> addList = photoSerializable.getList();
                if (addList == null || addList.size() == 0) {
                    return;
                }
                int count = mAdapter.getCount();
                if (count == 0) {
                    PhotoInfo headPhoto = new PhotoInfo();
                    addList.add(0, headPhoto);
                }
                mAdapter.addAll(addList);
                break;
            case ImageUtils.REQUEST_CODE_GETIMAGE_BYCAMERA:// 调用照相机返回的结果
                if (mProtraitPath == null) {
                    String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss",
                            Locale.getDefault()).format(new Date());
                    // 照片命名
                    String cropFileName = "community" + timeStamp + ".jpg";
                    // 裁剪头像的绝对路径
                    mProtraitPath = ImageUtils.REQUEST_CODE_GETIMAGE_BYCAMERA_PATH + "/"
                            + cropFileName;
                }
                File file = new File(mProtraitPath);
                if (!file.exists()) {
                    return;
                }
                List<PhotoInfo> addList1 = new ArrayList<PhotoInfo>();
                int count1 = mAdapter.getCount();

                if (count1 == 0) {
                    PhotoInfo headPhoto = new PhotoInfo();
                    addList1.add(headPhoto);
                }
                PhotoInfo headPhoto = new PhotoInfo();
                headPhoto.setPath_file("file://" + mProtraitPath);
                headPhoto.setPath_absolute(mProtraitPath);
                addList1.add(headPhoto);
                mAdapter.addAll(addList1);
                break;
            default:
                break;
        }
    }

    /**
     * 相机拍照
     *
     * @param
     */
    private void startActionCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        mPhotoUri = getCameraTempFile();
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mPhotoUri);
        intent.putExtra("return-data", true);
        startActivityForResult(intent,
                ImageUtils.REQUEST_CODE_GETIMAGE_BYCAMERA);
    }

    private Uri getCameraTempFile() {
        String storageState = Environment.getExternalStorageState();
        if (storageState.equals(Environment.MEDIA_MOUNTED)) {
            File savedir = new File(
                    ImageUtils.REQUEST_CODE_GETIMAGE_BYCAMERA_PATH);
            if (!savedir.exists()) {
                savedir.mkdirs();
            }
        } else {
            return null;
        }
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss",
                Locale.getDefault()).format(new Date());
        // 照片命名
        String cropFileName = "community" + timeStamp + ".jpg";
        // 裁剪头像的绝对路径
        mProtraitPath = ImageUtils.REQUEST_CODE_GETIMAGE_BYCAMERA_PATH + "/"
                + cropFileName;
        File protraitFile = new File(mProtraitPath);
        Uri cropUri = Uri.fromFile(protraitFile);
        return cropUri;
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
