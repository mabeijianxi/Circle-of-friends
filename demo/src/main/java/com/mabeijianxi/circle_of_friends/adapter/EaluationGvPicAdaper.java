package com.mabeijianxi.circle_of_friends.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
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
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.Serializable;
import java.util.List;

/**
 * Created by jian on 2016/1/3.
 */
public class EaluationGvPicAdaper<EaluationPicBean> extends BaseAdapter {
    private Context mContext;
    private List<EaluationListBean.EaluationPicBean> mAttachmentsList;
    private ImageLoader mImageLoader = ImageLoader.getInstance();
    private DisplayImageOptions mConfig = new DisplayImageOptions.Builder()
            .showImageOnLoading(R.drawable.home_youpin)
            .showImageForEmptyUri(R.drawable.home_youpin)
            .showImageOnFail(R.drawable.home_youpin).cacheInMemory(true)// 在内存中会缓存该图片
            .cacheOnDisk(true)// 在硬盘中会缓存该图片
            .considerExifParams(true)// 会识别图片的方向信息
//            .displayer(new FadeInBitmapDisplayer(500))
            .build();
//            .displayer(new RoundedBitmapDisplayer(8000)).build();
    /**
     * sdk版本号
     */
    private int mSdkInt = Build.VERSION.SDK_INT;
    ;
    private boolean mIsLoadImage = true;
    private final int mImageWidth;

    public EaluationGvPicAdaper(Context mContext, List<EaluationListBean.EaluationPicBean> attachments, boolean mIsLoadImage) {
        this.mContext = mContext;
        this.mAttachmentsList = attachments;
        this.mIsLoadImage = mIsLoadImage;
//        这里我曾经在加载的时候动态获取每个item的宽度在设置，但效果不理想。会出现测量不及时，使总高度变高，于是留下大片空白区域，但多滑动几次又正常了。
        mImageWidth = (CommonUtils.getScreenSizeWidth((Activity) mContext) - CommonUtils.dip2px(mContext, 102))/3;
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
    public View getView( final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = View.inflate(mContext, R.layout.item_grid_image, null);
            holder.iv_image = (ImageView) convertView.findViewById(R.id.iv_image);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(mImageWidth, mImageWidth);
            holder.iv_image.setLayoutParams(params);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        EaluationListBean.EaluationPicBean picBean = getItem(position);

        if (mIsLoadImage) {
            mImageLoader.displayImage(picBean.smallImageUrl, holder.iv_image, mConfig);
        }
        else{
            Bitmap bitmap = mImageLoader.getMemoryCache().get(picBean.smallImageUrl);
            if(bitmap!=null){
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
                ((MainActivity)mContext). overridePendingTransition(R.anim.activity2pic_in, R.anim.activity2pic_out);

            }
        });
        return convertView;
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
