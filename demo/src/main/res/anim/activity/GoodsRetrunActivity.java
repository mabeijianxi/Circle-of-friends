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
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.TextView;

import com.asyey.footballlibrary.network.exception.HttpException;
import com.asyey.footballlibrary.network.http.ResponseInfo;
import com.henanjianye.soon.communityo2o.BaseActivity;
import com.henanjianye.soon.communityo2o.R;
import com.henanjianye.soon.communityo2o.common.Constant;
import com.henanjianye.soon.communityo2o.common.CropImgAsyctask;
import com.henanjianye.soon.communityo2o.common.CropImgAsyctask.Listener;
import com.henanjianye.soon.communityo2o.common.UserDataHelper;
import com.henanjianye.soon.communityo2o.common.enties.BaseDataBean;
import com.henanjianye.soon.communityo2o.common.enties.GoodsBackBean;
import com.henanjianye.soon.communityo2o.common.enties.JsonUtil;
import com.henanjianye.soon.communityo2o.common.util.CommonUtils;
import com.henanjianye.soon.communityo2o.common.util.MyTitleBarHelper;
import com.henanjianye.soon.communityo2o.common.util.NetWorkStateUtils;
import com.henanjianye.soon.communityo2o.common.util.SharedPreferencesUtil;
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
import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GoodsRetrunActivity extends BaseActivity implements View.OnClickListener, AdapterView.OnItemClickListener, AddImageView.OnAddListener, Listener {
    private MyTitleBarHelper myTitleBarHelper;
    private EditText et_return_reason;
    private String return_reason = "";
    private EditText et_phone;
    private String get_phone;
    private AddImageView mAddImage;
    private Uri mPhotoUri;
    private String mProtraitPath;
    private SelectPicPopupWindow mMenuWindow;
    private AddImageListAdapter mAdapter;
    //图片路径
    private List<String> urls = new ArrayList<>();
    private Context mContext;
    private long OrderId;
    private int Type;
    TextView tv_tuihuo_dec;

//    public static void sendIdAndType(long orderId, int type) {
//        OrderId = orderId;
//        Type = type;
//    }


    public static void startGoodsRetrunActivityForresult(Activity context, long OrderId, int Type, int requestCode) {
        Intent intent = new Intent(context, GoodsRetrunActivity.class);
        intent.putExtra("TYPE", Type);
        intent.putExtra("ORDERID", OrderId);

        context.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        questdec();
        setContentView(R.layout.activity_goods_return);
        mContext = this;
        initViews();
        initEvents();
        init();
    }

//    private void questdec() {
//        if (!TextUtils.isEmpty(Constant.OrderUrl.TUIHUOSHUOMING)) {
//            getNetRequestHelper(GoodsRetrunActivity.this).postRequest(Constant.OrderUrl.TUIHUOSHUOMING, null, Constant.OrderUrl.TUIHUOSHUOMING);
//        }
//    }


    private void init() {
        mAdapter = new AddImageListAdapter(this);
        mAddImage.setAdapter(mAdapter);
        tv_tuihuo_dec.setText(SharedPreferencesUtil.getStringData(this, SharedPreferencesUtil.APP_URL_RETURNDESCRIPTION, ""));
    }

    private void initViews() {
        Intent intent = getIntent();
        Type = intent.getIntExtra("TYPE", 0);
        OrderId = intent.getLongExtra("ORDERID", -1);
        if (OrderId == -1) {
            toast("数据错误");
            return;
        }
        myTitleBarHelper = new MyTitleBarHelper(this, getWindow().getDecorView().findViewById(android.R.id.content));
        myTitleBarHelper.setLeftImgVisible(false);
        myTitleBarHelper.resetState();
        myTitleBarHelper.setMiddleText("退货申请");
        myTitleBarHelper.setRightText("确定退货");
        myTitleBarHelper.setLeftImag(R.mipmap.btn_back);
        et_return_reason = (EditText) findViewById(R.id.et_return_reason);
        tv_tuihuo_dec = (TextView) findViewById(R.id.tv_tuihuo_dec);
        mAddImage = (AddImageView) findViewById(R.id.add_img);
        et_phone = (EditText) findViewById(R.id.et_phone);
    }

    private void initEvents() {
        myTitleBarHelper.setOnclickListener(this);
        //拍照或者上传本地图片
        mAddImage.setOnItemClickListener(this);
        mAddImage.setOnAddListener(this);
    }

    @Override
    public void onClick(View v) {
//        super.onClick(v);
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
        customProgressDialog = CustomProgressDialog.createDialog(GoodsRetrunActivity.this);
        if (customProgressDialog != null && !customProgressDialog.isShowing()) {
            customProgressDialog.show();
        }
    }

    @Override
    public void SST_OnDataReady(String data) {
        if (data != null && !data.equals("")) {
            CompressUrls.add(data);
        }
    }

    @Override
    public void SST_OnFinished(boolean bError) {
        if (customProgressDialog != null && customProgressDialog.isShowing()) {
            customProgressDialog.dismiss();
        }
        if (bError) {
            GoodsBackBean goodsBackBean = new GoodsBackBean();
            goodsBackBean.reason = return_reason;
            goodsBackBean.orderId = OrderId;
            goodsBackBean.tel = get_phone;
            goodsBackBean.type = Type;
            UserDataHelper userDataHelper = new UserDataHelper(getApplication());
            List<String> photoUrls = new ArrayList<>();
            if (CompressUrls != null && CompressUrls.size() > 0) {
                for (int i = 0; i < CompressUrls.size(); i++) {
                    String photo = CompressUrls.get(i);
                    if (photo.contains("file://")) {
                        photo = photo.replace("file://", "");
                    }
                    photoUrls.add(photo);
                }
            }
            userDataHelper.Submit_goods_back_info(getNetRequestHelper(mContext), goodsBackBean.orderId,
                    goodsBackBean.reason, goodsBackBean.tel, photoUrls, goodsBackBean.type);
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
                        GoodsRetrunActivity.this.finish();
                    } else {
                        toast(baseDataBean.msg);
                    }
                }
            }

            @Override
            public void onFailure(HttpException error, String msg, String requestTag) {
                NetWorkStateUtils.errorNetMes(GoodsRetrunActivity.this);
            }
        };
    }

//    private void parse(String result) {
//
//        BaseDataBean<TuiHuoShuoMingBean> baseData = JsonUtil.parseDataObject(result, TuiHuoShuoMingBean.class);
//        if (baseData.code == 100) {
//            String returnDescription = baseData.data.returnDescription;
//            if (!TextUtils.isEmpty(returnDescription)) {
//                tv_tuihuo_dec.setText(returnDescription);
//            }
//        }
//    }

    private boolean check_input() {
        return_reason = et_return_reason.getText().toString();
        if (return_reason.equals("")) {
            toast("请输入退货原因");
            return false;
        }
        get_phone = et_phone.getText().toString();
        if (get_phone.equals("")) {
            toast("电话号码不能为空");
            return false;
        }
        if (!CommonUtils.isAvalidPhoneNum(get_phone)) {
            toast("请填写正确的手机号码");
            return false;
        }
//        if(OrderId==0){}
        return true;
    }


    private void hideSoftBoard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(et_return_reason.getWindowToken(), 0); //强制隐藏键盘
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        hideSoftBoard();
        if (position == 0) {
            //点击加号
            if ((mAdapter.getCount() - 1) >= ImageUtils.IMAGE_UPLOAD_MAX_NUM_1) {
                toast("上传图片的最大数量是6");
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
        hideSoftBoard();
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
                    intent.setClass(GoodsRetrunActivity.this,
                            SelectPhotoActivity.class);
                    intent.putExtra(SelectPhotoActivity.PHOTO_UPLOAD_MAX_NUM,
                            ImageUtils.IMAGE_UPLOAD_MAX_NUM_1);
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
