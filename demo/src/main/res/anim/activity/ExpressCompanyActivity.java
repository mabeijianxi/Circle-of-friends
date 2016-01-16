package anim.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.asyey.footballlibrary.network.exception.HttpException;
import com.asyey.footballlibrary.network.http.ResponseInfo;
import com.henanjianye.soon.communityo2o.BaseActivity;
import com.henanjianye.soon.communityo2o.R;
import com.henanjianye.soon.communityo2o.common.Constant;
import com.henanjianye.soon.communityo2o.common.DividList.CharacterParser;
import com.henanjianye.soon.communityo2o.common.DividList.PinyinComparator;
import com.henanjianye.soon.communityo2o.common.DividList.SideBarC;
import com.henanjianye.soon.communityo2o.common.DividList.SortModel;
import com.henanjianye.soon.communityo2o.common.UserDataHelper;
import com.henanjianye.soon.communityo2o.common.enties.BaseDataBean;
import com.henanjianye.soon.communityo2o.common.enties.ExpressCompanyBean;
import com.henanjianye.soon.communityo2o.common.enties.JsonUtil;
import com.henanjianye.soon.communityo2o.common.util.CommonUtils;
import com.henanjianye.soon.communityo2o.common.util.MyTitleBarHelper;
import com.henanjianye.soon.communityo2o.common.util.SharedPreferencesUtil;
import com.henanjianye.soon.communityo2o.interf.NetWorkCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class ExpressCompanyActivity extends BaseActivity {
    private MyTitleBarHelper myTitleBarHelper;
    private ListView lv_express_company;
    //    private TextView empty_list_view;
    private ExpressCompanyAdapter eAdapter;
    private SideBarC sideBar;
    private TextView dialog;
    /**
     * 汉字转换成拼音的类
     */
    private CharacterParser characterParser;
    private ArrayList<SortModel> SourceDateList;
    /**
     * 根据拼音来排列ListView里面的数据类
     */
    private PinyinComparator pinyinComparator;
    List<ExpressCompanyBean.ExpressCompany> nList1 = new ArrayList<>();
    private UserDataHelper userDataHelper;
    public static final String COMPANY_ID = "company_id";
    public static final String COMPANY_NAME = "company_name";
    public static final String COMPANY_CODE = "company_code";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_express_company);
        initViews();
        initEvents();
        init();
        getData();
    }


    private void initViews() {
        myTitleBarHelper = new MyTitleBarHelper(this, getWindow().getDecorView().findViewById(android.R.id.content));
        myTitleBarHelper.setLeftImgVisible(false);
        myTitleBarHelper.resetState();
        myTitleBarHelper.setMiddleText("快递公司");
        myTitleBarHelper.setRightImgVisible(false);
        myTitleBarHelper.setLeftImag(R.mipmap.btn_back);
//        empty_list_view = (TextView) findViewById(R.id.empty_list_view);
        lv_express_company = (ListView) findViewById(R.id.lv_express_company);
//        lv_no_receive.setEmptyView(empty_list_view);
        //实例化汉字转拼音类
        characterParser = CharacterParser.getInstance();

        pinyinComparator = new PinyinComparator();
        sideBar = (SideBarC) findViewById(R.id.ct_house_step1_sidrbar);
        dialog = (TextView) findViewById(R.id.tv_house_step1_dialog);
        sideBar.setTextView(dialog);
        //设置右侧触摸监听
        sideBar.setOnTouchingLetterChangedListener(new SideBarC.OnTouchingLetterChangedListener() {

            @Override
            public void onTouchingLetterChanged(String s) {
                //该字母首次出现的位置
                if (s.charAt(0) == '☆') {
                    lv_express_company.setSelection(0);
                } else {
                    int position = eAdapter.getPositionForSection(s.charAt(0));
                    if (position != -1) {
                        lv_express_company.setSelection(position);
                    }
                }
            }
        });
    }

    private void initEvents() {
        myTitleBarHelper.setOnclickListener(this);
        lv_express_company.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (eAdapter != null && eAdapter.getCount() > 0) {
                    Intent mIntent = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putInt(COMPANY_ID, eAdapter.getItem(position).getId());
                    bundle.putString(COMPANY_NAME, eAdapter.getItem(position).getName());
                    bundle.putString(COMPANY_CODE, eAdapter.getItem(position).getCompanyCode());
                    mIntent.putExtras(bundle);
                    setResult(RESULT_OK, mIntent);
                    finish();
                }
            }
        });
    }

    private void init() {
        userDataHelper = new UserDataHelper(this);
        eAdapter = new ExpressCompanyAdapter(this);
        lv_express_company.setAdapter(eAdapter);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.iv_title_bar_left:
                this.finish();
                break;
            default:
                break;
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
                if (requestTag.equals(Constant.Express.EXPRESS_COMPANY)) {// 快递公司
                    SharedPreferencesUtil.saveStringData(ExpressCompanyActivity.this,
                            Constant.Express.EXPRESS_COMPANY, responseInfo.result);
                    parseExpressCompany(responseInfo.result);
                }
            }

            @Override
            public void onFailure(HttpException error, String msg, String requestTag) {
            }
        };
    }

    private void parseExpressCompany(String result) {
        try {
            BaseDataBean<ExpressCompanyBean> json = JsonUtil.parseDataObject(result, ExpressCompanyBean.class);
            if (json.code == 100) {
                if (json.data != null) {
                    if (json.data.expressCompanyListCommon != null && json.data.expressCompanyListCommon.size() > 0) {
                        //常用快递
                        nList1 = json.data.expressCompanyListCommon;
                        SourceDateList = fillDataCommon(nList1);
                        eAdapter.add(SourceDateList);
                    }
                    if (json.data.expressCompanyList.size() > 0) {
                        //按拼音排序的快递
                        nList1 = json.data.expressCompanyList;
                        SourceDateList = filledData(nList1);
                        // 根据a-z进行排序源数据
                        Collections.sort(SourceDateList, pinyinComparator);
                        eAdapter.add(SourceDateList);
                    }
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //快递公司
    class ExpressCompanyAdapter extends BaseAdapter implements SectionIndexer {
        SortModel eBean;
        private LayoutInflater mInflater;
        ArrayList<SortModel> nList = new ArrayList<>();

        public ExpressCompanyAdapter(Context mContext) {
            this.mInflater = LayoutInflater.from(mContext);
        }

        @Override
        public int getCount() {
            if (nList != null && nList.size() > 0) {
                return nList.size();
            }
            return 0;
        }

        @Override
        public SortModel getItem(int position) {
            if (nList != null && nList.size() > 0) {
                return nList.get(position);
            } else {
                return null;
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            eBean = getItem(position);
            final ExpressHolder nHolder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_express_company, parent, false);
                nHolder = new ExpressHolder();
                nHolder.tv_name = (TextView) convertView.findViewById(R.id.express_company_name);
                nHolder.iv_express_company = (ImageView) convertView.findViewById(R.id.iv_company_icon);
                nHolder.catalog = (TextView) convertView.findViewById(R.id.catalog);
                convertView.setTag(nHolder);
            } else {
                nHolder = (ExpressHolder) convertView.getTag();
            }

            //根据position获取分类的首字母的char ascii值
            int section = getSectionForPosition(position);
            //如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现
//            MyLog.e("MMM","eBean.getSortLetters() is "+eBean.getSortLetters());
//            MyLog.e("MMM","getPositionForSection(section) is "+getPositionForSection(section));
            if (position == getPositionForSection(section)) {
                nHolder.catalog.setVisibility(View.VISIBLE);
                nHolder.catalog.setText(eBean.getSortLetters());
            } else {
                nHolder.catalog.setVisibility(View.GONE);
            }
            nHolder.tv_name.setText(eBean.getName());
            return convertView;
        }

        public final class ExpressHolder {
            TextView catalog;
            TextView tv_name;
            ImageView iv_express_company;
        }

        public void add(ArrayList<SortModel> list) {
            nList.addAll(list);
            notifyDataSetChanged();
        }

        public void clear() {
            if (nList != null && nList.size() > 0) {
                nList.clear();
                notifyDataSetChanged();
            }
        }


        public int getPositionForSection(int section) {
//            MyLog.e("MMM","getCount() is "+getCount());
            for (int i = 0; i < getCount(); i++) {
                String sortStr = nList.get(i).getSortLetters();
//                MyLog.e("MMM","sortStr is "+sortStr);
                char firstChar = sortStr.toUpperCase().charAt(0);
//                MyLog.e("MMM","firstChar is "+firstChar);
                if (firstChar == section) {
                    return i;
                }
            }
            return -1;
        }

        @Override
        public Object[] getSections() {
            return new Object[0];
        }

        @Override
        public int getSectionForPosition(int position) {
            return nList.get(position).getSortLetters().charAt(0);
        }
    }

    /**
     * 为ListView填充数据
     *
     * @param expressCompanyBeans
     * @return
     */
    private ArrayList<SortModel> fillDataCommon(List<ExpressCompanyBean.ExpressCompany> expressCompanyBeans) {
        ArrayList<SortModel> mSortList = new ArrayList<>();
        for (int i = 0; i < expressCompanyBeans.size(); i++) {
            SortModel sortModel = new SortModel();
            sortModel.setObj(expressCompanyBeans.get(i));
            sortModel.setName(expressCompanyBeans.get(i).comName);
            sortModel.setId(expressCompanyBeans.get(i).id);
            sortModel.setCompanyCode(expressCompanyBeans.get(i).comCode);
            if (characterParser == null || expressCompanyBeans.get(i) == null) {
                break;
            }
//            //汉字转换成拼音
//            String pinyin = characterParser.getSelling(expressCompanyBeans.get(i).comName);
//            String sortString = pinyin.substring(0, 1).toUpperCase();
            // 正则表达式，判断首字母是否是英文字母
//            if (sortString.matches("[A-Z]")) {
//                sortModel.setSortLetters(sortString.toUpperCase());
//            } else {
            sortModel.setSortLetters("常用");
//            }

            mSortList.add(sortModel);
        }
        return mSortList;
    }

    /**
     * 为ListView填充数据
     *
     * @param expressCompanyBeans
     * @return
     */
    private ArrayList<SortModel> filledData(List<ExpressCompanyBean.ExpressCompany> expressCompanyBeans) {
        ArrayList<SortModel> mSortList = new ArrayList<>();
        for (int i = 0; i < expressCompanyBeans.size(); i++) {
            SortModel sortModel = new SortModel();
            sortModel.setObj(expressCompanyBeans.get(i));
            sortModel.setName(expressCompanyBeans.get(i).comName);
            sortModel.setId(expressCompanyBeans.get(i).id);
            sortModel.setCompanyCode(expressCompanyBeans.get(i).comCode);
            if (characterParser == null || expressCompanyBeans.get(i) == null) {
                break;
            }
//            MyLog.e("MMM","expressCompanyBeans.get(i).CompanayName is "+expressCompanyBeans.get(i).CompanayName);
            //汉字转换成拼音
            String pinyin = characterParser.getSelling(expressCompanyBeans.get(i).comName);
//            MyLog.e("MMM","pinyin is "+pinyin);
            String sortString = pinyin.substring(0, 1).toUpperCase();
//            MyLog.e("MMM","sortString is "+sortString);
            // 正则表达式，判断首字母是否是英文字母
            if (sortString.matches("[A-Z]")) {
                sortModel.setSortLetters(sortString.toUpperCase());
            } else {
                sortModel.setSortLetters("#");
            }

            mSortList.add(sortModel);
        }
        return mSortList;
    }

    private void getData() {
        if (CommonUtils.isNetworkConnected(this)) {
            requestExpressCompanyData();
        } else {
            toast("网络不可用");
        }
    }

    private void requestExpressCompanyData() {
        userDataHelper.getExpressCompany(getNetRequestHelper(this).isShowProgressDialog(true));
    }
}
