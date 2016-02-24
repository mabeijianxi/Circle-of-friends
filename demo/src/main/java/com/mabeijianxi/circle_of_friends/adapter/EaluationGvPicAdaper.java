package com.mabeijianxi.circle_of_friends.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.mabeijianxi.circle_of_friends.R;
import com.mabeijianxi.circle_of_friends.activity.LookBigPicActivity;
import com.mabeijianxi.circle_of_friends.activity.MainActivity;
import com.mabeijianxi.circle_of_friends.bean.EaluationListBean;
import com.mabeijianxi.circle_of_friends.utils.CommonUtils;
import com.mabeijianxi.circle_of_friends.utils.ImageUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.Serializable;
import java.util.List;

/**
 * Created by mabeijianxi on 2016/1/3.
 */
public class EaluationGvPicAdaper extends BaseAdapter {
    private Context mContext;
    private List<EaluationListBean.EaluationPicBean> mAttachmentsList;
    private ImageLoader mImageLoader = ImageLoader.getInstance();
    private DisplayImageOptions mConfig = new DisplayImageOptions.Builder()
            .showImageOnLoading(R.drawable.home_youpin)
            .showImageForEmptyUri(R.drawable.home_youpin)
            .showImageOnFail(R.drawable.home_youpin)
            .cacheInMemory(true)// 在内存中会缓存该图片
            .cacheOnDisk(true)// 在硬盘中会缓存该图片
            .considerExifParams(true)// 会识别图片的方向信息
            .build();
    /**
     * 是否加载图片，主要也是对滑动的优化
     */
    private boolean mIsLoadImage = true;
    private final int mImageWidth;

    public EaluationGvPicAdaper(Context mContext, List<EaluationListBean.EaluationPicBean> attachments, boolean mIsLoadImage) {
        this.mContext = mContext;
        this.mAttachmentsList = attachments;
        this.mIsLoadImage = mIsLoadImage;
//        这里我曾经在加载的时候动态获取每个item的宽度在设置，但效果不理想。会出现测量不及时，使总高度变高，于是留下大片空白区域，但多滑动几次又正常了。
        mImageWidth = (CommonUtils.getScreenSizeWidth((Activity) mContext) - CommonUtils.dip2px(mContext, 102)) / 3;
    }

    @Override
    public int getCount() {
        if (mAttachmentsList != null) {
            return mAttachmentsList.size();
        }
        return 0;
    }

    @Override
    public EaluationListBean.EaluationPicBean getItem(int position) {
        return mAttachmentsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = View.inflate(mContext, R.layout.item_grid_image, null);
            holder.iv_image = (ImageView) convertView.findViewById(R.id.iv_image);

            setImageParms(holder);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        EaluationListBean.EaluationPicBean picBean = getItem(position);
//主要是也优化处理，这里注意else里面的得到方式，如果直接带入url去get有可能会有问题的
        if (mIsLoadImage) {
            mImageLoader.displayImage(picBean.smallImageUrl, holder.iv_image, mConfig);
        } else {
            Bitmap bitmap = ImageUtils.getBitmapFromCache(picBean.smallImageUrl, mImageLoader);
            if (bitmap != null) {
                holder.iv_image.setImageBitmap(bitmap);
            }
        }
        holder.iv_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                点击查看大图
                Intent intent = new Intent(mContext, LookBigPicActivity.class);
                Bundle bundle = new Bundle();

                setupCoords(holder.iv_image, mAttachmentsList, position);
                Log.e("log", "imageUrl:"+mAttachmentsList.get(position).imageUrl);
                Log.e("log", "smallImageUrl:"+mAttachmentsList.get(position).smallImageUrl);
                        bundle.putSerializable(LookBigPicActivity.PICDATALIST, (Serializable) mAttachmentsList);
                intent.putExtras(bundle);
                intent.putExtra(LookBigPicActivity.CURRENTITEM, position);
                mContext.startActivity(intent);
                ((MainActivity) mContext).overridePendingTransition(0, 0);

            }
        });
        return convertView;
    }

    /**
     * 计算每个item的坐标
     * @param iv_image
     * @param mAttachmentsList
     * @param position
     */
    private void setupCoords(ImageView iv_image, List<EaluationListBean.EaluationPicBean> mAttachmentsList, int position) {
//        x方向的第几个
        int xn=position%3+1;
//        y方向的第几个
        int yn=position/3+1;
//        x方向的总间距
        int h=(xn-1)*CommonUtils.dip2px(mContext,4);
//        y方向的总间距
        int v=h;
//        图片宽高
        int height = iv_image.getHeight();
        int width = iv_image.getWidth();
//        获取当前点击图片在屏幕上的坐标
        int[] points=new int[2];
        iv_image.getLocationInWindow(points);
//        获取第一张图片的坐标
        int x0=points[0]-(width+h)*(xn-1) ;
        int y0=points[1]-(height+v)*(yn-1);
//        给所有图片添加坐标信息
        for(int i=0;i<mAttachmentsList.size();i++){
            EaluationListBean.EaluationPicBean ealuationPicBean = mAttachmentsList.get(i);
            ealuationPicBean.width=width;
            ealuationPicBean.height=height;
            ealuationPicBean.x=x0+(i%3)*(width+h);
            ealuationPicBean.y=y0+(i/3)*(height+v)-CommonUtils.getStatusBarHeight(iv_image);
        }
    }

    /**
     * 设置图片的显示大小
     * @param holder
     */
    private void setImageParms(ViewHolder holder) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(mImageWidth, mImageWidth);
        holder.iv_image.setLayoutParams(params);
    }

    public boolean ismIsLoadImage() {
        return mIsLoadImage;
    }

    public void setmIsLoadImage(boolean mIsLoadImage) {
        this.mIsLoadImage = mIsLoadImage;
    }

    static class ViewHolder {
        public ImageView iv_image;
    }
}
