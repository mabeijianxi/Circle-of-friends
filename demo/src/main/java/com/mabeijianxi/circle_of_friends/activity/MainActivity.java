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
import com.mabeijianxi.circle_of_friends.view.myrecyclerview.DividerItemDecoration;
import com.mabeijianxi.circle_of_friends.view.myrecyclerview.PullToLoadView;
import com.mabeijianxi.circle_of_friends.utils.CommonUtils;
import com.mabeijianxi.circle_of_friends.utils.JsonUtil;
import com.mabeijianxi.circle_of_friends.utils.SharedPreferencesUtil;
import com.zhy.http.okhttp.OkHttpUtils;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by mabeijianxi on 2016/1/15.
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener, NetWorkCallback {

    private static final String URL = "";
    private PullToLoadView mPullToLoadView;
    private ImageView iv_default;
    private RecyclerView mRecyclerView;
    private EaluationAdapter mAdapter;
    private ArrayList<EaluationListBean> mEvaluataions;
    /**
     * 得到的json串
     */
    private String mdataString;
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
     * 添加RecyclerView的滑动事件监听
     */
    private void addScrollListener() {

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
        mRecyclerView.addItemDecoration(new DividerItemDecoration(
                this, DividerItemDecoration.VERTICAL_LIST));

        mPullToLoadView.isLoadMoreEnabled(true);

    }

    private void loadData(int pageNo) {
        isLoading = true;
        if (CommonUtils.isNetworkConnected(this)) {
            mPullToLoadView.setVisibility(View.VISIBLE);
            iv_default.setVisibility(View.GONE);
            if (getId() != -1) {
                requestData(pageNo);
            } else {
                Toast.makeText(this,"数据错误",Toast.LENGTH_SHORT);
            }
        } else if (!CommonUtils.isNetworkConnected(this) && TextUtils.isEmpty(mdataString)) {
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
     * 发起网络请求，这里用封装好的okhttp
     *
     * @param pageNo
     */
    private void requestData(int pageNo) {

        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("goodsId", String.valueOf(getId()));
        hashMap.put("pageNo", String.valueOf(pageNo));
        OkHttpUtils.post().params(hashMap).url("").build().execute(okRequestCallBack);
    }


    /**
     * 得到物品id以便网络请求带上，当然只是本服务器接口需要而已
     *
     * @return
     */
    public int getId() {

        return 0;
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
            SharedPreferencesUtil.saveStringData(this,
                    URL + pageNo + getId(), responseInfo);
            mdataString =responseInfo;
            parseData(responseInfo);

        if (mPullToLoadView != null) {
            mPullToLoadView.setComplete();
        }
    }

    @Override
    public void onFailure(Exception error, String msg) {

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
