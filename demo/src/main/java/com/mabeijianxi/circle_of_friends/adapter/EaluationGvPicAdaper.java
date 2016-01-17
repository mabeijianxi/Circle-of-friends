package com.mabeijianxi.circle_of_friends.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
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
 * Created by jian on 2016/1/3.
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
                bundle.putSerializable(LookBigPicActivity.PICDATALIST, (Serializable) mAttachmentsList);
                intent.putExtras(bundle);
                intent.putExtra(LookBigPicActivity.CURRENTITEM, position);
                mContext.startActivity(intent);
                ((MainActivity) mContext).overridePendingTransition(R.anim.activity2pic_in, R.anim.activity2pic_out);

            }
        });
        return convertView;
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
