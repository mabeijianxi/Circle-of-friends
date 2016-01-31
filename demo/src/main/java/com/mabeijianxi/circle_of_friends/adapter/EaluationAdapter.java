package com.mabeijianxi.circle_of_friends.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.mabeijianxi.circle_of_friends.R;
import com.mabeijianxi.circle_of_friends.activity.LookBigPicActivity;
import com.mabeijianxi.circle_of_friends.activity.MainActivity;
import com.mabeijianxi.circle_of_friends.bean.EaluationListBean;
import com.mabeijianxi.circle_of_friends.bean.EvaluatereplysBean;
import com.mabeijianxi.circle_of_friends.utils.CommonUtils;
import com.mabeijianxi.circle_of_friends.view.CircularImage;
import com.mabeijianxi.circle_of_friends.view.CustomGridView;
import com.mabeijianxi.circle_of_friends.view.linearlistview.LinearListView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mabeijianxi on 2016/1/3.
 */
public class EaluationAdapter extends RecyclerView.Adapter<EaluationAdapter.EaluationHolder> {
    /**
     * 当高分辨率的时候服务器的图片显得太小，这里优化下显示比例
     */
    private Float fTimes;
    private Context mContext;
    private boolean mIsLoadImage = true;
    private ArrayList<EaluationListBean> mEaluationList;
    private EaluationGvPicAdaper mEaluationGvPicAdaper;
    private ImageLoader mImageLoader = ImageLoader.getInstance();
    private DisplayImageOptions mConfig = new DisplayImageOptions.Builder()
            .showImageForEmptyUri(R.drawable.home_youpin)
            .showImageOnFail(R.drawable.home_youpin)
            .cacheInMemory(true)// 在内存中会缓存该图片
            .cacheOnDisk(true)// 在硬盘中会缓存该图片
            .considerExifParams(true)// 会识别图片的方向信息
            .resetViewBeforeLoading(true)// 重设图片
            .build();


    public ArrayList<EaluationListBean> getmEaluationList() {
        return mEaluationList;
    }

    /**
     * 是否加载图片
     *
     * @param isLoadImage
     */
    public void setLoadImage(boolean isLoadImage) {
        this.mIsLoadImage = isLoadImage;
    }

    public EaluationAdapter(Context context) {
        this.mContext = context;
        mEaluationList = new ArrayList<>();
//        适配单图放大比例
        String sTimes = mContext.getResources().getString(R.string.times);
        fTimes = Float.valueOf(sTimes);
    }

    public void clearAdapterNotifyData() {
        mEaluationList.clear();
        notifyDataSetChanged();
    }

    public void clearAdapter() {
        mEaluationList.clear();
    }

    public void addEaluationDataAllNotifyData(ArrayList<EaluationListBean> data) {
        if (data != null) {
            mEaluationList.addAll(data);
            notifyDataSetChanged();
        }

    }

    public void addEaluationDataAll(ArrayList<EaluationListBean> data) {
        if (data != null) {
            mEaluationList.addAll(data);
        }

    }

    public void addEaluationData(EaluationListBean data) {
        if (data != null) {
            mEaluationList.add(data);
            notifyDataSetChanged();
        }

    }

    @Override
    public int getItemCount() {
        return mEaluationList.size();
    }

    @Override
    public EaluationHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_comments, parent, false);
        return new EaluationHolder(view);
    }

    @Override
    public void onBindViewHolder(EaluationHolder holder, int position) {
        EaluationListBean ealuationListBean = mEaluationList.get(position);
        List<EaluationListBean.EaluationPicBean> attachments = ealuationListBean.attachments;
        if (ealuationListBean.avatar != null) {
            mImageLoader.displayImage(ealuationListBean.avatar.smallPicUrl, holder.icon, mConfig);
            setIconClick(holder, ealuationListBean.avatar.smallPicUrl, ealuationListBean.avatar.picUrl);
        } else {
            holder.icon.setImageResource(R.drawable.home_youpin);
            setIconClick(holder, "null", "null");
        }
        holder.tv_nickname.setText(ealuationListBean.userName);
        holder.tv_text.setText(ealuationListBean.content);
        holder.tv_date.setText(ealuationListBean.creatTime);
        holder.rb_stars.setRating(ealuationListBean.grade);
        setUpImage(holder, attachments, position);
        setUpTereplys(holder, ealuationListBean.evaluatereplys);
    }

    /**
     * 设置回复内容规则
     *这里用的是自定义的LinearLayout，这样比listview消耗要小一些
     * @param holder
     * @param evaluatereplysList
     */
    private void setUpTereplys(EaluationHolder holder, List<EvaluatereplysBean> evaluatereplysList) {
        if (evaluatereplysList != null && evaluatereplysList.size() > 0) {
            holder.lv_comments_details.setVisibility(View.VISIBLE);
            EvaluatereplysAdapter evaluatereplysAdapter = new EvaluatereplysAdapter(mContext, evaluatereplysList);
            holder.lv_comments_details.setAdapter(evaluatereplysAdapter);
        } else {
            holder.lv_comments_details.setVisibility(View.GONE);
        }
    }

    /**
     * 设置图片显示规则
     *
     * @param holder
     * @param attachments
     * @param position
     */
    private void setUpImage(EaluationHolder holder, List<EaluationListBean.EaluationPicBean> attachments, int position) {
        holder.fl_image.setVisibility(View.GONE);
        if (attachments != null) {
            if (attachments.size() == 0) {
                holder.fl_image.setVisibility(View.GONE);
            } else if (attachments.size() == 1) {
                setSingleImage(attachments, holder, position);
                holder.gv_image.setVisibility(View.GONE);
                holder.iv_image.setVisibility(View.VISIBLE);
                holder.fl_image.setVisibility(View.VISIBLE);
            } else {
                holder.iv_image.setVisibility(View.GONE);
                holder.gv_image.setVisibility(View.VISIBLE);
                holder.fl_image.setVisibility(View.VISIBLE);
                setManyImage(attachments, holder, position);
            }
        } else {
            holder.fl_image.setVisibility(View.GONE);
        }
    }

    /**
     * 设置头像的点击看大图事件，这里为了方便直接把bean类进行了转换传递
     * @param holder
     * @param miniPicUrl
     * @param picUrl
     */
    private void setIconClick(final EaluationHolder holder, final String miniPicUrl, final String picUrl) {
        holder.icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, LookBigPicActivity.class);
                Bundle bundle = new Bundle();
                List<EaluationListBean.EaluationPicBean> attachments = new ArrayList<EaluationListBean.EaluationPicBean>();
                EaluationListBean.EaluationPicBean ealuationPicBean = new EaluationListBean().new EaluationPicBean();

                int height = holder.icon.getHeight();
                int width = holder.icon.getWidth();
                int[] points=new int[2];
                holder.icon.getLocationInWindow(points);
                ealuationPicBean.height=height;
                ealuationPicBean.width=width;
                ealuationPicBean.x=points[0];
                ealuationPicBean.y=points[1]- CommonUtils.getStatusBarHeight(holder.icon);
                ealuationPicBean.imageUrl = picUrl;
                ealuationPicBean.smallImageUrl = miniPicUrl;
                attachments.add(ealuationPicBean);

                bundle.putSerializable(LookBigPicActivity.PICDATALIST, (Serializable) attachments);
                intent.putExtras(bundle);
                intent.putExtra(LookBigPicActivity.CURRENTITEM, 0);
                mContext.startActivity(intent);
//                动画处理
                startActivityAnim();
            }
        });
    }

    /**
     * 设置多图
     *
     * @param attachments
     * @param holder
     * @param position
     */
    private void setManyImage(List<EaluationListBean.EaluationPicBean> attachments, EaluationHolder holder, int position) {
//         mEaluationGvPicAdaper = (EaluationGvPicAdaper) holder.gv_image.getTag(position);
//        if(mEaluationGvPicAdaper==null){
        EaluationGvPicAdaper mEaluationGvPicAdaper = new EaluationGvPicAdaper(mContext, attachments, mIsLoadImage);
//            holder.gv_image.setTag(position,mEaluationGvPicAdaper);
        holder.gv_image.setAdapter(mEaluationGvPicAdaper);
//        }
    }

    /**
     * 设置单图
     *
     * @param attachments
     * @param holder
     */
    private void setSingleImage(final List<EaluationListBean.EaluationPicBean> attachments, final EaluationHolder holder, final int position) {

//可更具请求选择是否设置是否对单图快滑处理
//        if (mIsLoadImage) {
        mImageLoader.displayImage(attachments.get(0).smallImageUrl, holder.iv_image, mConfig, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
//                优化显示比例
                if (fTimes != 1) {
                    int height = loadedImage.getHeight();
                    int width = loadedImage.getWidth();
                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams((int) (width * fTimes), (int) (height * fTimes));
                    holder.iv_image.setLayoutParams(params);
                }
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {

            }
        });
        holder.iv_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                            点击查看大图的操作
                Intent intent = new Intent(mContext, LookBigPicActivity.class);
                Bundle bundle = new Bundle();
                int height = holder.iv_image.getHeight();
                int width = holder.iv_image.getWidth();
                int[] points=new int[2];
                holder.iv_image.getLocationInWindow(points);
                attachments.get(0).height=height;
                attachments.get(0).width=width;
                attachments.get(0).x=points[0];
                attachments.get(0).y=points[1]-CommonUtils.getStatusBarHeight(holder.iv_image);
                bundle.putSerializable(LookBigPicActivity.PICDATALIST, (Serializable) attachments);
                intent.putExtras(bundle);
                intent.putExtra(LookBigPicActivity.CURRENTITEM, 0);
                mContext.startActivity(intent);
                startActivityAnim();

            }
        });
//        }
//        优化快滑时的图片加载
       /* else {
            Bitmap bitmap = mImageLoader.getMemoryCache().get(attachments.get(0).smallImageUrl);
            if (bitmap != null) {
                holder.iv_image.setImageBitmap(bitmap);
            }
        }*/
    }

    /**
     * 开始跳转动画
     */
    private void startActivityAnim() {
        ((MainActivity) mContext).overridePendingTransition(0, 0);
    }

    static class EaluationHolder extends RecyclerView.ViewHolder {
        public CircularImage icon;
        public ImageView iv_image;
        public TextView tv_nickname;
        public TextView tv_text;
        //        可更具情况设置为emoji表情
        public TextView tv_date;
        public RatingBar rb_stars;
        public CustomGridView gv_image;
        public LinearListView lv_comments_details;
        public FrameLayout fl_image;

        public EaluationHolder(View itemView) {
            super(itemView);
            icon = (CircularImage) itemView.findViewById(R.id.icon);
            iv_image = (ImageView) itemView.findViewById(R.id.iv_image);
            tv_nickname = (TextView) itemView.findViewById(R.id.tv_nickname);
            tv_text = (TextView) itemView.findViewById(R.id.tv_text);
            tv_date = (TextView) itemView.findViewById(R.id.tv_date);
            rb_stars = (RatingBar) itemView.findViewById(R.id.rb_stars);
            gv_image = (CustomGridView) itemView.findViewById(R.id.gv_image);
            lv_comments_details = (LinearListView) itemView.findViewById(R.id.lv_comments_details);
            fl_image = (FrameLayout) itemView.findViewById(R.id.fl_image);

        }
    }

}
