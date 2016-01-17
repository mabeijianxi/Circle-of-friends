# Circle-of-friends
###Similar to the micro channel circle of friends QQ space, but also can be used to make a list of product evaluation or other graphic display.

####先看下demo效果，其实就和朋友圈差不多，对其进行了一定程度的优化，我个人对是对性能比较重视的。我在代码里面有特别详细的注释，适合有需要的学习者，模拟器上可能有色斑与卡顿嘿嘿...

![demo](http://7xq6db.com1.z0.glb.clouddn.com/demo.gif)


-----
##本demo主要想分享的技术要点：

* listview各种嵌套处理
* listview的优化处理
* 用ImageLoader的一些特殊方式来优化图片加载
* listview的高度封装操作（刷新、加载更多、ui操作等）

##主用到的一些依赖有：
* [Android-Universal-Image-Loader](https://github.com/nostra13/Android-Universal-Image-Loader)
* [PhotoView](https://github.com/chrisbanes/PhotoView)
* [okhttp-utils](https://github.com/hongyangAndroid/okhttp-utils)
