package com.mabeijianxi.circle_of_friends.utils;

import android.graphics.Bitmap;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.utils.MemoryCacheUtils;

import java.util.List;

/**
 * Created by mabeijianxi on 2016/1/16.
 */
public class ImageUtils {
    /**
     * 使用此加载框架的imageloader加载的图片，设置了缓存后，下次使用，手工从缓存取出来用，这时特别要注意，不能直接使用：
     * imageLoader.getMemoryCache().get(uri)来获取，因为在加载过程中，key是经过运算的，而不单单是uri,而是：
     * String memoryCacheKey = MemoryCacheUtil.generateKey(uri, targetSize);
     *
     * @return
     */
    public static Bitmap getBitmapFromCache(String uri,ImageLoader imageLoader){//这里的uri一般就是图片网址
        List<String> memCacheKeyNameList = MemoryCacheUtils.findCacheKeysForImageUri(uri, imageLoader.getMemoryCache());
        if(memCacheKeyNameList != null && memCacheKeyNameList.size() > 0){
            for(String each:memCacheKeyNameList){
            }
            return imageLoader.getMemoryCache().get(memCacheKeyNameList.get(0));
        }

        return null;
    }
}
