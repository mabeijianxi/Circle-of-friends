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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.asyey.footballlibrary.network.exception.HttpException;
import com.asyey.footballlibrary.network.http.ResponseInfo;
import com.henanjianye.soon.communityo2o.BaseActivity;
import com.henanjianye.soon.communityo2o.R;
import com.henanjianye.soon.communityo2o.common.Constant;
import com.henanjianye.soon.communityo2o.common.CropImgAsyctask;
import com.henanjianye.soon.communityo2o.common.UserDataHelper;
import com.henanjianye.soon.communityo2o.common.enties.BaseBean;
import com.henanjianye.soon.communityo2o.common.enties.BaseDataBean;
import com.henanjianye.soon.communityo2o.common.enties.HouseInfoBean;
import com.henanjianye.soon.communityo2o.common.enties.JsonUtil;
import com.henanjianye.soon.communityo2o.common.enties.RepairBean;
import com.henanjianye.soon.communityo2o.common.enties.UserBean;
import com.henanjianye.soon.communityo2o.common.util.CommonUtils;
import com.henanjianye.soon.communityo2o.common.util.FastClick;
import com.henanjianye.soon.communityo2o.common.util.JianPanUtils;
import com.henanjianye.soon.communityo2o.common.util.MyTitleBarHelper;
import com.henanjianye.soon.communityo2o.common.util.NetWorkStateUtils;
import com.henanjianye.soon.communityo2o.common.util.UserSharedPreferencesUtil;
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
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ServiceRepairActivity extends BaseActivity implements View.OnClickListener, AdapterView.OnItemClickListener, AddImageView.OnAddListener, CropImgAsyctask.Listener {
    private MyTitleBarHelper myTitleBarHelper;
    private RadioGroup mRadioGroup;
    private TextView iv_submit;
    private EditText et_question;
    private TextView hope_repair_time;
    private AddImageView mAddImage;
    private SelectPicPopupWindow mMenuWindow;
    private AddImageListAdapter mAdapter;
    private Uri mPhotoUri;
    private String mProtraitPath;
    public static String REPAIR_INFO = "repair_info";
    public static String REPAIR_BACK_INFO = "repair_back_info";
    public static String REPAIR_BACK_RECORK_INFO = "repair_back_recork_info";
    public static String REPAIR_CONFIRM_TIME = "repair_confirm_time";
    private int requestTimeCode = 100;
    //    private Button button;
    //维修类型
    private String repair_Type = "2";
    //上门时间段
    private String repair_date = "";
    private String repair_time = "";
    //图片路径
    private List<String> urls = new ArrayList<>();
    private List<String> CompressUrls = new ArrayList<>();
    //问题描述
    private String repair_question;
    public static Activity prepairAct;
    private TextView repiar_name;
    private TextView repair_phone;
    private TextView tv_address;
    //    private CustomSinnper spinner;
    private BaseDataBean<UserBean> houseList;
    private Context mContext;
    private UserSharedPreferencesUtil userSharedPreferencesUtil;
    String[] strs;
    private int orgId;
    //    private String repairPhone = "";
    private Spinner gradeSpinner;
    //    private int curPos = 0;
    private CustomProgressDialog customProgressDialog;
    private int currentHouseId;
    private RadioButton repair_jujia;
    private RadioButton repair_pub;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_repair);
        prepairAct = this;
        mContext = this;
        initViews();
        initEvents();
        init();
        getData();
    }

    private void getData() {
        if (CommonUtils.isNetworkConnected(this)) {
            // 通知网络请求
            requestAddressData();
        } else {
            toast("网络未连接或不可用");
        }
    }

    private void requestAddressData() {
        new UserDataHelper(this).getHouseAddrInfor(getNetRequestHelper(this).isShowProgressDialog(true), orgId);
    }

    UserBean bean;

    private void init() {
        mAdapter = new AddImageListAdapter(this);
        mAddImage.setAdapter(mAdapter);
        bean = userSharedPreferencesUtil.getUserInfo(mContext);
        if (bean != null) {
            if (bean.realname != null && !bean.realname.equals("")) {
                getName = bean.realname;
            } else if (bean.nickname != null && !bean.nickname.equals("")) {
                getName = bean.nickname;
            } else {
                getName = bean.username;
            }
            repiar_name.setText(getName);
            repair_phone.setText(bean.mobilephone);
            tv_address.setVisibility(View.GONE);
        }
    }

    private void initViews() {
        myTitleBarHelper = new MyTitleBarHelper(this, getWindow().getDecorView().findViewById(android.R.id.content));
        myTitleBarHelper.setLeftImgVisible(false);
        myTitleBarHelper.resetState();
        myTitleBarHelper.setMiddleText("报修");
//        myTitleBarHelper.setRightImag(R.drawable.title_index_phone);
        myTitleBarHelper.setRightImgVisible(false);
        myTitleBarHelper.setRightText("报修记录");
        myTitleBarHelper.setLeftImag(R.mipmap.btn_back);
        mRadioGroup = (RadioGroup) findViewById(R.id.type_repair);
        repair_jujia = (RadioButton) findViewById(R.id.repair_jujia);
        repair_pub = (RadioButton) findViewById(R.id.repair_pub);
        iv_submit = (TextView) findViewById(R.id.iv_submit);
        mAddImage = (AddImageView) findViewById(R.id.add_img);
        et_question = (EditText) findViewById(R.id.editContent);
        hope_repair_time = (TextView) findViewById(R.id.repair_time);
        repiar_name = (TextView) findViewById(R.id.repiar_name);
        repair_phone = (TextView) findViewById(R.id.repair_phone);
        tv_address = (TextView) findViewById(R.id.tv_address);
        //自定义下拉列表 地址
        gradeSpinner = (Spinner) findViewById(R.id.gradeSpinner);
        userSharedPreferencesUtil = new UserSharedPreferencesUtil();
        orgId = UserSharedPreferencesUtil.getUserInfo(this, UserSharedPreferencesUtil.ORGID, -1);
    }

//    private void createDialog() {
//        //布局文件转换为view对象
//        LayoutInflater inflaterDl = LayoutInflater.from(this);
//        LinearLayout layout = (LinearLayout) inflaterDl.inflate(R.layout.phone_consult, null);
//        //对话框
//        final Dialog dialog = new AlertDialog.Builder(ServiceRepairActivity.this).create();
//        dialog.show();
////        dialog.setCancelable(false);
//        dialog.setCancelable(true);
//        dialog.getWindow().setContentView(layout);
//        //取消按钮
//        ImageView btnCancel = (ImageView) layout.findViewById(R.id.btn_close);
//        btnCancel.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                dialog.dismiss();
//            }
//        });
//        TextView tv_phone_show = (TextView) layout.findViewById(R.id.tv_phone_show);
//        tv_phone_show.setText(phoneList.get(gradeSpinner.getSelectedItemPosition()));
//        //打电话
//        TextView tv_call = (TextView) layout.findViewById(R.id.tv_call);
//        tv_call.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(Intent.ACTION_DIAL,
//                        Uri.parse("tel:" + phoneList.get(gradeSpinner.getSelectedItemPosition())));
//                startActivity(intent);
//            }
//        });
//    }

    private void initEvents() {
        et_question.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                return (event.getKeyCode() == KeyEvent.KEYCODE_ENTER);
            }
        });
        myTitleBarHelper.setOnclickListener(this);
//        take_picture1.setOnClickListener(this);
        iv_submit.setOnClickListener(this);
        hope_repair_time.setOnClickListener(this);
        //拍照或者上传本地图片
        mAddImage.setOnItemClickListener(this);
        mAddImage.setOnAddListener(this);
        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.repair_jujia) {
                    repair_jujia.setBackgroundResource(R.drawable.btn_soild_green_bg);
                    repair_jujia.setTextColor(getResources().getColor(R.color.white));
                    repair_pub.setBackgroundResource(R.drawable.btn_stroke_green_bg);
                    repair_pub.setTextColor(getResources().getColor(R.color.repair_line_color));
                    repair_Type = "2";
                } else if (checkedId == R.id.repair_pub) {
                    repair_pub.setBackgroundResource(R.drawable.btn_soild_green_bg);
                    repair_pub.setTextColor(getResources().getColor(R.color.white));
                    repair_jujia.setBackgroundResource(R.drawable.btn_stroke_green_bg);
                    repair_jujia.setTextColor(getResources().getColor(R.color.repair_line_color));
                    repair_Type = "1";
                }
            }
        });
        gradeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (myAdapter != null) {
                    gradeSpinner.setSelection(position, true);
                    if (gradeSpinner.getSelectedItemPosition() == myAdapter.mList.size() - 1) {
                        //点击的最后一个 添加物业信息
                        CommonUtils.startLoginChooseOrgActivity(ServiceRepairActivity.this);
                        ServiceRepairActivity.this.finish();
                    } else {
                        //点击其他 切换小区
                        if (houseIdList.size() > 0 && houseIdList.size() > position) {
                            currentHouseId = houseIdList.get(position);
                        } else {
                            toast("数据错误");
                        }
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    String getName = "";
    private static long back_pressed;

    @Override
    public void onClick(View v) {
//        super.onClick(v);
        switch (v.getId()) {
            case R.id.iv_title_bar_left:
                this.finish();
                break;
            case R.id.tv_title_bar_right:
//                createDialog();
                if (CommonUtils.isNetworkConnected(ServiceRepairActivity.this)) {
                    Intent intent = new Intent(ServiceRepairActivity.this, ServiceRepairRecordActivity.class);
                    startActivity(intent);
                } else {
                    toast("网络未连接或不可用");
                }
                break;
            case R.id.repair_time:
                if (back_pressed + 1000 < System.currentTimeMillis()&& !FastClick.isFastClick()) {
                    Intent intRepair = new Intent(ServiceRepairActivity.this, SelectorActivity.class);
                    startActivityForResult(intRepair, requestTimeCode);
                }
                back_pressed = System.currentTimeMillis();
                break;

            case R.id.iv_submit:
                if (!check_input()) {
                    return;
                }
                if (!CommonUtils.isNetworkConnected(this)) {
                    toast("网络未连接");
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
                break;
            default:
                break;
        }
    }


    @Override
    public void SST_OnBegin() {
        customProgressDialog = CustomProgressDialog.createDialog(ServiceRepairActivity.this);
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
            RepairBean rBean = new RepairBean();
            rBean.repairType = repair_Type;
            rBean.visitDate = repair_date;
            rBean.visitTime = repair_time;
            rBean.upload = CompressUrls;
            rBean.des = repair_question;
            UserDataHelper userDataHelper = new UserDataHelper(getApplication());
            List<String> photoUrls = new ArrayList<>();
            if (rBean.upload != null && rBean.upload.size() > 0) {
                for (int i = 0; i < rBean.upload.size(); i++) {
                    String photo = rBean.upload.get(i);
                    if (photo.contains("file:")) {
                        photo = photo.replace("file:", "");
                    }
                    photoUrls.add(photo);
                }
            }
            if (bean != null) {
                rBean.houseId = currentHouseId;
                rBean.userName = getName;
                rBean.userMobile = bean.mobilephone;
            }
            userDataHelper.Submit_repairInfo(getNetRequestHelper(mContext), rBean.repairType, rBean.des,
                    rBean.visitDate, rBean.visitTime, rBean.houseId, rBean.userName, rBean.userMobile, photoUrls, bean.orgId);
            iv_submit.setClickable(false);
        }
    }

    ArrayList<String> houseAddrList = new ArrayList<>();
    ArrayList<Integer> houseIdList = new ArrayList<>();
    ArrayList<String> phoneList = new ArrayList<>();
    MyAdapter myAdapter;

    private void parseHouseInfoRecord(String result) {
        if (!result.equals("")) {
//            BaseDataBean<HouseInfoBean> baseDataBean = JsonUtil.parseDataObject(result, HouseInfoBean.class);
            BaseBean<HouseInfoBean> baseDataBean = JsonUtil.jsonArray(result, HouseInfoBean.class);
            if (baseDataBean.code == 100) {
                if (baseDataBean.data != null && baseDataBean.data.size() > 0) {
                    List<HouseInfoBean> houseList = baseDataBean.data;
                    for (int i = 0; i < houseList.size(); i++) {
                        HouseInfoBean houseInfoBean = houseList.get(i);
                        houseAddrList.add(houseInfoBean.fullAddress);
                        houseIdList.add(houseInfoBean.houseId);
                        phoneList.add(houseInfoBean.repairPhone);
                    }
                    //
                    houseAddrList.add("添加新的物业信息");
                    myAdapter = new MyAdapter(mContext, houseAddrList);
                    gradeSpinner.setAdapter(myAdapter);
                }
            }
        }
    }


    public class MyAdapter extends BaseAdapter {
        private List<String> mList;
        private Context mContext;
        LayoutInflater _LayoutInflater;

        public MyAdapter(Context pContext, List<String> pList) {
            this.mContext = pContext;
            this.mList = pList;
            _LayoutInflater = LayoutInflater.from(mContext);
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        /**
         * 下面是重要代码
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            MyHolder holder;
            if (convertView == null) {
                convertView = _LayoutInflater.inflate(R.layout.item_custom, parent, false);
                holder = new MyHolder();
                holder._TextView2 = (TextView) convertView.findViewById(R.id.spinner_item_label);
                holder.imageView = (ImageView) convertView.findViewById(R.id.spinner_item_checked_image);
                holder.rl_item = (RelativeLayout) convertView.findViewById(R.id.rl_item);
                convertView.setTag(holder);//绑定ViewHolder对象
            } else {
                holder = (MyHolder) convertView.getTag();//取出ViewHolder对象
            }
            if (gradeSpinner.getSelectedItemPosition() == mList.size() - 1) {
                //最后一个位置
                holder.imageView.setVisibility(View.GONE);
            } else {
                if (gradeSpinner.getSelectedItemPosition() == position) {
//                    curPos = position;
                    holder.imageView.setImageResource(R.mipmap.round_butten_select);
                } else {
                    holder.imageView.setImageResource(R.mipmap.round_butten_normal);
                }
                holder._TextView2.setText("" + mList.get(position));
            }

            return convertView;
        }

        class MyHolder {
            public TextView _TextView2;
            private ImageView imageView;
            private RelativeLayout rl_item;
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
                if (requestTag.equals(Constant.Repair.HOUSE_ADDRESS)) {
                    parseHouseInfoRecord(responseInfo.result);
                } else if (requestTag.equals(Constant.Repair.REPAIR_SUBMIT)) {
                    BaseDataBean<Object> baseDataBean = JsonUtil.parseDataObject(responseInfo.result, Object.class);
//                    MyLog.e("KKK", "提交返回 baseDataBean.code is " + baseDataBean.code);
                    if (baseDataBean.code == 100) {
                        toast("提交成功");
//                        Intent intent = new Intent();
////                    intent.putExtra(ServiceRepairActivity.REPAIR_BACK_INFO, rBean);
//                        setResult(RESULT_OK, intent);
//                        ServiceRepairActivity.this.finish();
//                        Intent intent = new Intent(ServiceRepairActivity.this, ServiceRepairRecordActivity.class);
                        startActivity(new Intent(ServiceRepairActivity.this, ServiceRepairRecordActivity.class));
                        iv_submit.setClickable(true);
                    } else {
                        toast(baseDataBean.msg);
                    }
                }
            }

            @Override
            public void onFailure(HttpException error, String msg, String requestTag) {
                NetWorkStateUtils.errorNetMes(ServiceRepairActivity.this);
                iv_submit.setClickable(true);
            }
        };
    }

    private boolean check_input() {
        if (getTime == null || getTime.equals("")) {
            toast("请选择上门时段");
            return false;
        }
        repair_question = et_question.getText().toString();
        if (repair_question.equals("")) {
            toast("请输入问题描述");
            return false;
        }

        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        JianPanUtils.hideIme(this);
        if (position == 0) {
            //点击加号
            if ((mAdapter.getCount() - 1) >= ImageUtils.IMAGE_UPLOAD_MAX_NUM_1) {
                toast("上传图片的最大数量是" + ImageUtils.IMAGE_UPLOAD_MAX_NUM_1);
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
                    intent.setClass(ServiceRepairActivity.this,
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
    public static final String KEY_RESULT = "result";
    public static final String KEY_SUB_RESULT = "sub_result";
    public static final String KEY_OPTIONS = "options";
    public static final String KEY_SUB_OPTIONS = "sub_options";
    public static final int ACTIVITY_REQUESTCODE = 1;
    private String getTime = "";

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case ACTIVITY_REQUESTCODE:// 调用选择列表返回的结构
                if (data == null) {
                    return;
                }
//                String value = data.getStringExtra(KEY_RESULT);
//                if (resultCode == SelectTypeActivity.SELECT_COMMUNITY) {
//                    mSelectCommunity.setSpinnerText(value);
//                } else if (resultCode >= SelectTypeActivity.SELECT_MAINTAIN_MAIN_TYPE) {
//                    mSelectType.setSpinnerText(value);
//                }
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
                // int image_id;
//                if (data == null) {
//
//                } else {
//                    // try {
//                    // Uri imageData = data.getData();
//                    // image_id = (int) ContentUris.parseId(imageData);
//                    // headPhoto.setImage_id(image_id);
//                    // } catch (Exception e) {
//                    // // TODO Auto-generated catch block
//                    // }
//                }

                addList1.add(headPhoto);
                mAdapter.addAll(addList1);
                break;
            case 13:
                if (data == null) {
                    return;
                }
                //报修详情返回的数据
                Serializable serializable = data.getSerializableExtra(REPAIR_BACK_INFO);
                if (serializable != null) {
                    RepairBean rBean = (RepairBean) serializable;
                    Intent intent1 = new Intent();
                    intent1.putExtra(REPAIR_BACK_RECORK_INFO, rBean);
                    setResult(RESULT_OK, intent1);
                    finish();
                }

                break;
            case 100:
                if (data == null) {
                    return;
                }
                getTime = data.getStringExtra(REPAIR_CONFIRM_TIME);
                String[] array = getTime.split(" ");
                repair_date = array[0];
                repair_time = array[1];
                if (getTime != null && !getTime.equals("")) {
                    hope_repair_time.setText("期望上门时段： " + getTime);
                }
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
//    @Override
//    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//
//    }
}
