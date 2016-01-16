package anim.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.asyey.footballlibrary.network.exception.HttpException;
import com.asyey.footballlibrary.network.http.ResponseInfo;
import com.henanjianye.soon.communityo2o.R;
import com.henanjianye.soon.communityo2o.common.Constant;
import com.henanjianye.soon.communityo2o.common.UserDataHelper;
import com.henanjianye.soon.communityo2o.common.enties.BaseDataBean;
import com.henanjianye.soon.communityo2o.common.enties.JsonUtil;
import com.henanjianye.soon.communityo2o.common.enties.UserBean;
import com.henanjianye.soon.communityo2o.common.util.CommonUtils;
import com.henanjianye.soon.communityo2o.common.util.NetRequestHelper;
import com.henanjianye.soon.communityo2o.common.util.NetWorkStateUtils;
import com.henanjianye.soon.communityo2o.common.util.UserSharedPreferencesUtil;
import com.henanjianye.soon.communityo2o.interf.NetWorkCallback;
import com.henanjianye.soon.communityo2o.selectPortrait.FileUtils;
import com.henanjianye.soon.communityo2o.selectPortrait.PhotoUtils;
import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.io.Serializable;

public class SelectPortraitActivity extends Activity implements View.OnClickListener {
    private TextView reg_photo_layout_selectphoto;
    private TextView reg_photo_layout_takepicture;
    private ImageView mIvUserPhoto;
    private TextView save_pic;
    private NetRequestHelper netRequestHelper;
    private Context mContext;
    private UserDataHelper userDataHelper;
    public static final int CHANGE_USER_PHOTO = 0x2015;
    public static final String TRANSFER_USER_PHOTO = "transfer_user_info";
    public static final String IMAGE_TYPE = "image_type";
    private TYPE MyType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_portrait);
//        Window window = getWindow();
//        WindowManager.LayoutParams layoutParams = window.getAttributes();
//        //设置窗口的大小及透明度
//        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
//        layoutParams.height = layoutParams.WRAP_CONTENT;
//        window.setAttributes(layoutParams);
        mContext = this;
        initViews();
        initEvents();
        init();
    }

    private void initViews() {
        reg_photo_layout_selectphoto = (TextView) findViewById(R.id.reg_photo_layout_selectphoto);
        reg_photo_layout_takepicture = (TextView) findViewById(R.id.reg_photo_layout_takepicture);
        mIvUserPhoto = (ImageView) findViewById(R.id.reg_photo_iv_userphoto);
        save_pic = (TextView) findViewById(R.id.save_pic);
        netRequestHelper = new NetRequestHelper(mContext, new defaultNetWorkCallback());
        userDataHelper = new UserDataHelper(getApplication());
        Intent intent = getIntent();
        Serializable serializable = intent.getSerializableExtra(IMAGE_TYPE);
        if (serializable != null) {
            MyType = (TYPE) serializable;
        }
    }


    private void initEvents() {
        reg_photo_layout_selectphoto.setOnClickListener(this);
        reg_photo_layout_takepicture.setOnClickListener(this);
        save_pic.setOnClickListener(this);
    }

    private void init() {
    }

    private String mTakePicturePath = "";

    public enum TYPE {
        BIGIMAGE, SMALLIMAGE
    }

    @Override
    public void onClick(View v) {
//        super.onClick(v);
        switch (v.getId()) {
            case R.id.reg_photo_layout_selectphoto:
                PhotoUtils.selectPhoto(SelectPortraitActivity.this);
                break;
            case R.id.reg_photo_layout_takepicture:
                mTakePicturePath = PhotoUtils.takePicture(SelectPortraitActivity.this);
                break;
            case R.id.save_pic:
                if (CommonUtils.isNetworkConnected(this)) {
                    try {
//                        Log.e("MMM","mMyPhoto is "+mMyPhoto);
//                        Log.e("MMM","MyType is "+MyType);

                        if (mMyPhoto != null && !mMyPhoto.equals("") && MyType != null) {
                            userDataHelper.Modify_photo_info(netRequestHelper.isShowProgressDialog(true), MyType, new File(mMyPhoto));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(this, "网络未连接或不可用", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    private Bitmap mUserPhoto;
    private String mMyPhoto;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PhotoUtils.INTENT_REQUEST_CODE_ALBUM:
                if (data == null) {
                    return;
                }
                if (resultCode == RESULT_OK) {
                    if (data.getData() == null) {
                        return;
                    }
                    if (!FileUtils.isSdcardExist()) {
                        Toast.makeText(SelectPortraitActivity.this, "SD卡不可用,请检查", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Uri uri = data.getData();
                    String[] proj = {MediaStore.Images.Media.DATA};
                    Cursor cursor = managedQuery(uri, proj, null, null, null);
                    if (cursor != null) {
                        int column_index = cursor
                                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                        if (cursor.getCount() > 0 && cursor.moveToFirst()) {
                            String path = cursor.getString(column_index);
//                            Log.e("MMM","path is "+path);
                            Bitmap bitmap = PhotoUtils.imageZoom(BitmapFactory.decodeFile(path));
                            //压缩后的新路径
                            path=PhotoUtils.savePhotoToSDCard(bitmap);

                            if (PhotoUtils.bitmapIsLarge(bitmap)) {
                                PhotoUtils.cropPhoto(this, this, path);
                            } else {
                                setUserPhoto(bitmap);
                            }
                        }
                    }
                }
                break;

            case PhotoUtils.INTENT_REQUEST_CODE_CAMERA:
                if (resultCode == RESULT_OK) {
                    if (!mTakePicturePath.equals("")) {
//                        mMyPhoto = mTakePicturePath;
                        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
                        bitmapOptions.inSampleSize = 4;
                        Bitmap bitmap = PhotoUtils.imageZoom(BitmapFactory.decodeFile(mTakePicturePath, bitmapOptions));
                        //压缩后的新路径
                        mTakePicturePath=PhotoUtils.savePhotoToSDCard(bitmap);
                        if (PhotoUtils.bitmapIsLarge(bitmap)) {
                            PhotoUtils.cropPhoto(this, this, mTakePicturePath);
                        } else {
                            setUserPhoto(bitmap);
                        }
                    }
                }
                break;

            case PhotoUtils.INTENT_REQUEST_CODE_CROP:
                if (resultCode == RESULT_OK) {
                    String path = data.getStringExtra("path");
                    if (path != null) {
                        Bitmap bitmap = BitmapFactory.decodeFile(path);
                        mMyPhoto = path;
//                        Log.e("MMM","mMyPhoto333 is "+mMyPhoto);
                        if (bitmap != null) {
                            setUserPhoto(bitmap);
                        }
                    }
                }
                break;
        }
    }

    class defaultNetWorkCallback implements NetWorkCallback {
        @Override
        public void onStart(String requestTag) {

        }

        @Override
        public void onCancelled(String requestTag) {
            Toast.makeText(SelectPortraitActivity.this, "取消操作", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onLoading(long total, long current, boolean isUploading, String requestTag) {

        }

        @Override
        public void onSuccess(ResponseInfo<String> responseInfo, String requestTag) {
            if (requestTag.equals(Constant.ShouYeUrl.USR_IMAGE)) {
                BaseDataBean<UserBean> baseDataBean1 = JsonUtil.parseDataObject(responseInfo.result, UserBean.class);
                // BaseDataBean<Object> baseDataBean = JsonUtil.parseDataObject(responseInfo.result, Object.class);
                if (baseDataBean1.code == 100) {
                    Toast.makeText(SelectPortraitActivity.this, "提交成功", Toast.LENGTH_SHORT).show();
                    UserBean userBean = baseDataBean1.data;
                    UserSharedPreferencesUtil.savaUserInfo(SelectPortraitActivity.this, userBean);
                    UserSharedPreferencesUtil.savaUserJsonInfo(SelectPortraitActivity.this, responseInfo.result);
                    Intent intent = new Intent();
                    intent.putExtra(TRANSFER_USER_PHOTO, mMyPhoto);
                    setResult(RESULT_OK, intent);
                    SelectPortraitActivity.this.finish();
                }
                /*if (baseDataBean.code == 100) {
                    Toast.makeText(SelectPortraitActivity.this, "提交成功", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent();
                    intent.putExtra(TRANSFER_USER_PHOTO, mMyPhoto);
                    setResult(RESULT_OK, intent);
                    SelectPortraitActivity.this.finish();
                }*/
                else {
                    Toast.makeText(SelectPortraitActivity.this, baseDataBean1.msg, Toast.LENGTH_SHORT).show();
                }
            }
        }

        @Override
        public void onFailure(HttpException error, String msg, String requestTag) {
            NetWorkStateUtils.errorNetMes(SelectPortraitActivity.this);
        }
    }

    public void setUserPhoto(Bitmap bitmap) {
        if (bitmap != null) {
            mUserPhoto = bitmap;
            mIvUserPhoto.setImageBitmap(mUserPhoto);
            return;
        }
        Toast.makeText(SelectPortraitActivity.this, "未获取到图片", Toast.LENGTH_SHORT).show();
        mUserPhoto = null;
        mIvUserPhoto.setImageResource(R.mipmap.gouwuche_image);
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
