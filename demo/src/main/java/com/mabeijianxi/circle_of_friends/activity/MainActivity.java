package com.mabeijianxi.circle_of_friends.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.mabeijianxi.circle_of_friends.R;
import com.mabeijianxi.circle_of_friends.adapter.EaluationAdapter;
import com.mabeijianxi.circle_of_friends.bean.BaseDataBean;
import com.mabeijianxi.circle_of_friends.bean.EaluationBean;
import com.mabeijianxi.circle_of_friends.bean.EaluationListBean;
import com.mabeijianxi.circle_of_friends.callback.NetWorkCallback;
import com.mabeijianxi.circle_of_friends.callback.OkRequestCallBack;
import com.mabeijianxi.circle_of_friends.callback.PullCallback;
import com.mabeijianxi.circle_of_friends.utils.CommonUtils;
import com.mabeijianxi.circle_of_friends.utils.JsonUtil;
import com.mabeijianxi.circle_of_friends.utils.SharedPreferencesUtil;
import com.mabeijianxi.circle_of_friends.view.myrecyclerview.DividerItemDecoration;
import com.mabeijianxi.circle_of_friends.view.myrecyclerview.PullToLoadView;
import com.zhy.http.okhttp.OkHttpUtils;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by mabeijianxi on 2016/1/15.
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener, NetWorkCallback {
    /**
     * 本接口不一定长期有效，如果不能正常请求的话可以自定义实现数据
     */
    private static final String URL = "http://123.57.162.168:8081/mall/app/goods/evaluation/list.json";
    private PullToLoadView mPullToLoadView;
    private ImageView iv_default;
    private RecyclerView mRecyclerView;
    private EaluationAdapter mAdapter;
    private ArrayList<EaluationListBean> mEvaluataions;
    /**
     * 得到的json串
     */
    private String mDataString;
    /**
     * 是否是刷新
     */
    private boolean mIsRefresh = false;
    /**
     * 是否是加载中
     */
    private boolean isLoading = false;
    /**
     * 是否是第一次加载
     */
    private boolean mHasLoadedOnce = false;
    /**
     * 是否一次加载全部
     */
    private boolean isHasLoadedAll = false;
    /**
     * 页面号
     */
    private int pageNo = 1;
    private int mPageCount;
    private OkRequestCallBack okRequestCallBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initeven();
        parseLocal();
        startNetworkLoad();
    }

    /**
     * 开始网络请求
     */
    private void startNetworkLoad() {
        if (!mHasLoadedOnce && mPullToLoadView != null) {
            mPullToLoadView.initNetLoad(this);
        }
    }

    /**
     * 本地数据加载（如果有缓存）
     */
    private void parseLocal() {

        mDataString = SharedPreferencesUtil.getStringData(this,
                URL + pageNo  + getId(), null);
        if (!TextUtils.isEmpty(mDataString)) {// 解析数据
            parseData(mDataString);
        }
    }

    /**
     * 初始化事件
     */
    private void initeven() {
        okRequestCallBack = new OkRequestCallBack();
//        注册网络请求回调
        okRequestCallBack.setNetWorkCallback(this);
        iv_default.setOnClickListener(this);
        addScrollListener();
        mPullToLoadView.setPullCallback(setPullCallback());
    }

    /**
     * 添加RecyclerView的滑动事件监听，主要是ListView的优化（快滑处理）
     */
    private void addScrollListener() {
        mRecyclerView.addOnScrollListener(
                new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                        super.onScrollStateChanged(recyclerView, newState);
                        EaluationAdapter adapter = (EaluationAdapter) recyclerView.getAdapter();
                        if (adapter != null) {
                            switch (newState) {
//                                拖拽的时候
                                case RecyclerView.SCROLL_STATE_DRAGGING:
                                    adapter.setLoadImage(false);
//                                    adapter.notifyDataSetChanged();
                                    break;
//                                静止的时候
                                case RecyclerView.SCROLL_STATE_IDLE:
                                    adapter.setLoadImage(true);
                                    adapter.notifyDataSetChanged();
                                    break;
//                                惯性快滑
                                case RecyclerView.SCROLL_STATE_SETTLING:
                                    adapter.setLoadImage(false);
                                    break;

                            }
                        }
                    }
                });
    }

    /**
     * RecyclerView的事件回调
     *
     * @return
     */
    private PullCallback setPullCallback() {
        return new PullCallback() {
            @Override
            public void onLoadMore() {
//                这里其实我写的不严谨，不过这不是重点
                if (20 == mPageCount) {
                    loadData(++pageNo);
                } else {
                    isLoading = false;
                    if (mPullToLoadView != null) {
                        mPullToLoadView.setComplete();
                    }

                }
            }

            @Override
            public void onRefresh() {
                isHasLoadedAll = false;
                mIsRefresh = true;
                loadData(pageNo = 1);
            }

            @Override
            public boolean isLoading() {
                return isLoading;
            }

            @Override
            public boolean hasLoadedAllItems() {
                return isHasLoadedAll;
            }
        };
    }

    /**
     * 初始化view
     */
    public void initView() {
        mPullToLoadView = (PullToLoadView) findViewById(R.id.comments);
        iv_default = (ImageView) findViewById(R.id.iv_default_two);
        mRecyclerView = mPullToLoadView.getRecyclerView();
        LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(manager);
//        添加分割线
        mRecyclerView.addItemDecoration(new DividerItemDecoration(
                this, DividerItemDecoration.VERTICAL_LIST));

        mPullToLoadView.isLoadMoreEnabled(true);

    }

    /**
     * 准备网络加载
     * @param pageNo
     */
    private void loadData(int pageNo) {
        isLoading = true;
//        当有网络的时候
        if (CommonUtils.isNetworkConnected(this)) {
            mPullToLoadView.setVisibility(View.VISIBLE);
            iv_default.setVisibility(View.GONE);
            if (getId() != -1) {
                requestData(pageNo);
            } else {
                Toast.makeText(this,"数据错误",Toast.LENGTH_SHORT);
            }
        } else if (!CommonUtils.isNetworkConnected(this) && TextUtils.isEmpty(mDataString)) {
            //没网且没有缓存数据
            mPullToLoadView.setVisibility(View.GONE);
            iv_default.setVisibility(View.VISIBLE);
            Toast.makeText(this, "网络未连接或不可用", Toast.LENGTH_SHORT);
            if (mPullToLoadView != null) {
                mPullToLoadView.setSwipeRefreshing(false);
            }
        } else {
            mPullToLoadView.setVisibility(View.VISIBLE);
            iv_default.setVisibility(View.GONE);
            //没网但是有缓存
            Toast.makeText(this, "网络未连接或不可用", Toast.LENGTH_SHORT);
            parseLocal();
            if (mPullToLoadView != null) {
                mPullToLoadView.setComplete();
            }
        }
    }

    /**
     * 发起网络请求，这里用hongyang封装好的okhttp,比较要用，okhttp的优缺点可以自己查询
     *这里必须带入请求的字段是goodsId，页数是为了分页需要
     * @param pageNo
     */
    private void requestData(int pageNo) {

        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("goodsId", String.valueOf(getId()));
        hashMap.put("pageNo", String.valueOf(pageNo));
        OkHttpUtils.post().params(hashMap).url(URL).build().execute(okRequestCallBack);
    }


    /**
     * 得到物品id以便网络请求带上，当然只是本服务器接口需要而已
     *
     * @return
     */
    public int getId() {

        return 98573;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
//            加载失败，点击刷新
            case R.id.iv_default_two:
                if (mPullToLoadView != null) {
                    mPullToLoadView.initNetLoad(this);
                }
                break;
        }
    }


    @Override
    public void onBefore() {

    }

    @Override
    public void onLoading(float progress) {

    }

    @Override
    public void onSuccess(String responseInfo) {
        mHasLoadedOnce = true;
        isLoading=false;
//        缓存处理
            SharedPreferencesUtil.saveStringData(this,
                    URL + pageNo + getId(), responseInfo);
            mDataString =responseInfo;
            parseData(responseInfo);

        if (mPullToLoadView != null) {
            mPullToLoadView.setComplete();
        }
    }

    @Override
    public void onFailure(Exception error, String msg) {
        isLoading=false;
        parseLocal();
        if (mPullToLoadView != null) {
            mPullToLoadView.setComplete();
        }
        CommonUtils.errorNetMes(this);
    }
    /**
     * 数据解析
     *
     * @param mdatatring
     */
    private EaluationBean mEaluationBean = null;

    private void parseData(String mdatatring) {
        try {
            BaseDataBean<EaluationBean> json = JsonUtil.parseDataObject(mdatatring, EaluationBean.class);
            if (json.code == 100) {
                if (json.data != null) {
                    mEaluationBean = json.data;
                    mEvaluataions = mEaluationBean.evaluataions;
                    mPageCount = mEaluationBean.pageCount;
                }
                if (mAdapter == null) {
                    mAdapter = new EaluationAdapter(this);
                    mAdapter.addEaluationDataAll(mEvaluataions);
                    mRecyclerView.setAdapter(mAdapter);
                    mIsRefresh = false;
                } else {
                    if (mIsRefresh) {
                        mAdapter.clearAdapter();
                        mIsRefresh = false;
                    }
                    mAdapter.addEaluationDataAllNotifyData(mEvaluataions);
                }
            }

        } catch (Exception e) {
            // TODO: handle exception
        }
    }
}
