package com.mabeijianxi.circle_of_friends.callback;

/**
 * @author mabeijianxi
 */
public interface PullCallback {

    void onLoadMore();

    void onRefresh();

    boolean isLoading();

    boolean hasLoadedAllItems();

}
