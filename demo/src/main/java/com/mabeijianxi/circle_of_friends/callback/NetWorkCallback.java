package com.mabeijianxi.circle_of_friends.callback;

/**
 * @author mabeijianxi
 * @项目名称 sport
 * @创建时间 2016-1-13
 * @作用描述 网络请求回调
 * @备注
 */
public interface NetWorkCallback {

    /**
     * 开始请求
     *
     */
    public void onBefore();

    /**
     * 当前正在加载的进度；一般是在文件上传或者文件下载的时候监听进度
     *
     * @param progress       进度
     */
    public void onLoading(float progress);

    /**
     * 请求成功
     *
     * @param responseInfo 请求返回数据
     */
    public void onSuccess(String responseInfo);

    /**
     * 请求失败
     *
     * @param error      请求失败异常
     * @param msg        错误信息
     */
    public void onFailure(
            Exception error,
            String msg);
}
