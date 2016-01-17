package com.mabeijianxi.circle_of_friends.view.myrecyclerview;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mabeijianxi.circle_of_friends.R;
import com.mabeijianxi.circle_of_friends.callback.PullCallback;
import com.mabeijianxi.circle_of_friends.utils.CommonUtils;

/**
 * @author mabeijianxi
 * 封装了上拉加载更多下拉刷新等回调和ui
 */
public class PullToLoadView extends FrameLayout {

    private final TextView empty_pager;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private ProgressBar mProgressBar;
    private PullCallback mPullCallback;
    protected ScrollDirection mCurScrollingDirection;
    protected int mPrevFirstVisibleItem = 0;
    private int mLoadMoreOffset = 5;
    private boolean mIsLoadMoreEnabled = false;
    private String emptyMes;

    public PullToLoadView(Context context) {
        this(context, null);
    }

    public PullToLoadView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PullToLoadView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mInflater.inflate(R.layout.loadview, this, true);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        empty_pager = (TextView) findViewById(R.id.empty_pager);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        init();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    private void init() {
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (null != mPullCallback) {
                    mPullCallback.onRefresh();
                }
            }
        });

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                mCurScrollingDirection = null;
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (mCurScrollingDirection == null) { //User has just started a scrolling motion
                    mCurScrollingDirection = ScrollDirection.SAME;
                    mPrevFirstVisibleItem = getMyFirstVisibleItemPosition();
                } else {
                    final int firstVisibleItem = getMyFirstVisibleItemPosition();

                    if (firstVisibleItem > mPrevFirstVisibleItem) {
                        //User is scrolling up
                        mCurScrollingDirection = ScrollDirection.UP;
                    } else if (firstVisibleItem < mPrevFirstVisibleItem) {
                        //User is scrolling down
                        mCurScrollingDirection = ScrollDirection.DOWN;
                    } else {
                        mCurScrollingDirection = ScrollDirection.SAME;
                    }
                    mPrevFirstVisibleItem = firstVisibleItem;
                }
                if (mIsLoadMoreEnabled && (mCurScrollingDirection == ScrollDirection.UP)) {


                    //We only need to paginate if user scrolling near the end of the list
                    if (!mPullCallback.isLoading() && !mPullCallback.hasLoadedAllItems()) {
                        //Only trigger a load more if a load operation is NOT happening AND all the items have not been loaded
                        final int totalItemCount = getItemCount();
                        final int firstVisibleItem = getMyFirstVisibleItemPosition();
                        final int visibleItemCount = Math.abs(getMyLastVisibleItemPosition() - firstVisibleItem);
                        final int lastAdapterPosition = totalItemCount - 1;
                        final int lastVisiblePosition = (firstVisibleItem + visibleItemCount) - 1;
                        if (lastVisiblePosition >= (lastAdapterPosition - mLoadMoreOffset)) {
                            if (null != mPullCallback) {
                                mProgressBar.setVisibility(VISIBLE);
                                mPullCallback.onLoadMore();
                            }
                        }
                    }
                }
            }
        });
    }

    /**
     * 加载完成时可以调用
     */
    public void setComplete() {
        mProgressBar.setVisibility(GONE);
        if (mRecyclerView.getAdapter() != null && mRecyclerView.getAdapter().getItemCount() > 0) {
            empty_pager.setVisibility(GONE);
        } else {
            empty_pager.setVisibility(VISIBLE);
        }
//        MyLog.e("KKK", " mSwipeRefreshLayout.isRefreshing() is " + mSwipeRefreshLayout.isRefreshing());
        mSwipeRefreshLayout.setRefreshing(false);
//        MyLog.e("KKK", " mSwipeRefreshLayout.isRefreshing() is " + mSwipeRefreshLayout.isRefreshing());
    }

    public void setLayoutManager(LinearLayoutManager manager) {

        mRecyclerView.setLayoutManager(manager);
    }

    /**
     * 可自定义当listview条目为0时显示的提示信息
     * @param text
     */
    public void setEmptyText(String text) {
        this.emptyMes = text;
        if (empty_pager != null) {
            empty_pager.setText(emptyMes);
        }
    }

    /**
     * 调用次方发可手动刷新
     */
    public void initLoad() {
        if (null != mPullCallback) {
            mSwipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    mSwipeRefreshLayout.setRefreshing(true);
                }
            });
            mPullCallback.onRefresh();
        }
    }

    /**
     * 调用次方发可手动刷新,但没网除外
     * @param context
     */
    public void initNetLoad(Context context) {
        if (null != mPullCallback) {
            if (CommonUtils.isNetworkConnected(context)) {
                mSwipeRefreshLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        mSwipeRefreshLayout.setRefreshing(true);
                    }
                });
            } else {
                if (mSwipeRefreshLayout != null) {
                    mSwipeRefreshLayout.setRefreshing(true);
                }
            }
            mPullCallback.onRefresh();
        }
    }

    /**
     * 自定义SwipeRefreshLayout刷新的颜色
     * @param colorResIds
     */
    public void setColorSchemeResources(int... colorResIds) {
        mSwipeRefreshLayout.setColorSchemeResources(colorResIds);
    }

    public RecyclerView getRecyclerView() {
        return this.mRecyclerView;
    }

    /**
     * 设置回调
     * @param mPullCallback
     */
    public void setPullCallback(PullCallback mPullCallback) {
        this.mPullCallback = mPullCallback;
    }

    /**
     * 设置加载更多的容差，比如可以提前5个item加载
     * @param mLoadMoreOffset
     */
    public void setLoadMoreOffset(int mLoadMoreOffset) {
        this.mLoadMoreOffset = mLoadMoreOffset;
    }

    /**
     * 是否开启加载更多功能
     * @param mIsLoadMoreEnabled
     */
    public void isLoadMoreEnabled(boolean mIsLoadMoreEnabled) {
        this.mIsLoadMoreEnabled = mIsLoadMoreEnabled;
    }

    /**
     * 是否开启可刷新功能
     * @param mIsLoadMoreEnabled
     */
    public void isRefreshEnabled(boolean mIsLoadMoreEnabled) {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setEnabled(mIsLoadMoreEnabled);
        }
    }

    /**
     * 隐藏或者显示刷新Swipe的ui
     *
     * @param refreshing
     */
    public void setSwipeRefreshing(boolean refreshing) {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setRefreshing(refreshing);
        }
    }
    /**
     * 得到条目总数
     * @return
     */
    private int getItemCount() {
        RecyclerView.LayoutManager layoutManager = mRecyclerView.getLayoutManager();
        if(layoutManager!=null){
           return layoutManager.getItemCount();
        }
        return 0;
    }
    /**
     * 得到第一条可见的条目
     *
     * @return
     */
    private int getMyFirstVisibleItemPosition() {
        RecyclerView.LayoutManager layoutManager = mRecyclerView.getLayoutManager();
        int position;
        if (layoutManager instanceof LinearLayoutManager) {
            position = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
        } else if (layoutManager instanceof GridLayoutManager) {
            position = ((GridLayoutManager) layoutManager).findFirstVisibleItemPosition();
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            StaggeredGridLayoutManager staggeredlayoutManager = (StaggeredGridLayoutManager) layoutManager;
            int[] lastPositions = staggeredlayoutManager.findFirstVisibleItemPositions(new int[staggeredlayoutManager.getSpanCount()]);
            position = getMinPositions(lastPositions);
        } else {
            position = 0;
        }
        return position;
    }
    /**
     * 得到最后一条可见的条目
     *
     * @return
     */
    private int getMyLastVisibleItemPosition() {
        RecyclerView.LayoutManager layoutManager = mRecyclerView.getLayoutManager();
        int position;
        if (layoutManager instanceof LinearLayoutManager) {
            position = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
        } else if (layoutManager instanceof GridLayoutManager) {
            position = ((GridLayoutManager) layoutManager).findLastVisibleItemPosition();
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            StaggeredGridLayoutManager staggeredlayoutManager = (StaggeredGridLayoutManager) layoutManager;
            int[] lastPositions = staggeredlayoutManager.findLastVisibleItemPositions(new int[staggeredlayoutManager.getSpanCount()]);
            position = getMaxPositions(lastPositions);
        } else {
            position = 0;
        }
        return position;
    }

    /**
     * 获得当前展示最小的position
     *
     * @param positions
     * @return
     */
    private int getMinPositions(int[] positions) {
        int size = positions.length;
        int minPosition = Integer.MAX_VALUE;
        for (int i = 0; i < size; i++) {
            minPosition = Math.min(minPosition, positions[i]);
        }
        return minPosition;
    }
    /**
     * 获得当前展示最大的position
     *
     * @param positions
     * @return
     */
    private int getMaxPositions(int[] positions) {
        int size = positions.length;
        int maxPosition = 0;
        for (int i = 0; i < size; i++) {
            maxPosition = Math.max(maxPosition, positions[i]);
        }
        return maxPosition;
    }

}
