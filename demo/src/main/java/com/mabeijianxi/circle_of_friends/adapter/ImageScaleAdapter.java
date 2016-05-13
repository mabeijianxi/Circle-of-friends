package com.mabeijianxi.circle_of_friends.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.mabeijianxi.circle_of_friends.R;
import com.mabeijianxi.circle_of_friends.activity.LookBigPicActivity;
import com.mabeijianxi.circle_of_friends.bean.EaluationListBean;
import com.mabeijianxi.circle_of_friends.utils.CommonUtils;
import com.mabeijianxi.circle_of_friends.utils.ImageUtils;
import com.mabeijianxi.circle_of_friends.view.SendDongTaiDialog;
import com.mabeijianxi.circle_of_friends.view.photoview.PhotoView;
import com.mabeijianxi.circle_of_friends.view.photoview.PhotoViewAttacher;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.List;

/**
 * Created by mabeijianxi on 2015/1/5.
 */
public class ImageScaleAdapter extends PagerAdapter implements PhotoViewAttacher.OnPhotoTapListener {
    private List<EaluationListBean.EaluationPicBean> mPicData;
    private Context mContext;
    private ImageLoader mImageLoader = ImageLoader.getInstance();
    private DisplayImageOptions pager_options = new DisplayImageOptions.Builder()
            .showImageForEmptyUri(R.drawable.home_youpin)
            .showImageOnFail(R.drawable.home_youpin)
            .cacheOnDisk(true)
            .cacheInMemory(true)
            .imageScaleType(ImageScaleType.EXACTLY)//会对图片进一步的缩放
            .bitmapConfig(Bitmap.Config.RGB_565)//此种模消耗的内存会很小,2个byte存储一个像素
            .considerExifParams(true)
            .build();
    private PhotoView imageView;
    private View mCurrentView;
    private SendDongTaiDialog sendDongTaiDialog;

    public ImageScaleAdapter(Context mContext, List<EaluationListBean.EaluationPicBean> data) {
        super();
        this.mPicData = data;
        this.mContext = mContext;
        if (sendDongTaiDialog == null) {
            sendDongTaiDialog = new SendDongTaiDialog((Activity) mContext);
            sendDongTaiDialog.getBtn1().setText("本地保存");
            sendDongTaiDialog.getBtn2().setVisibility(View.GONE);
        }
    }

    @Override
    public int getCount() {
        if (mPicData != null) {
            return mPicData.size();
        }
        return 0;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);
        mCurrentView = (View) object;
    }

    public View getPrimaryItem() {
        return mCurrentView;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        View inflate = LayoutInflater.from(mContext).inflate(R.layout.item_photoview, container, false);
        imageView = (PhotoView) inflate.findViewById(R.id.pv);
        imageView.setOnPhotoTapListener(this);
        final ProgressBar pb = (ProgressBar) inflate.findViewById(R.id.pb);
        final EaluationListBean.EaluationPicBean ealuationPicBean = mPicData.get(position);

        if (ealuationPicBean != null && ealuationPicBean.imageUrl != null && !"null".equals(ealuationPicBean.imageUrl)) {
            setupNetImage(pb, ealuationPicBean);
            savaPic(ealuationPicBean);
        } else {
            imageView.setImageResource(R.drawable.home_youpin);
        }

        container.addView(inflate);//将ImageView加入到ViewPager中
        return inflate;
    }
    private void savaPic(final EaluationListBean.EaluationPicBean ealuationPicBean) {

        imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                sendDongTaiDialog.setmDialogClickListener(new SendDongTaiDialog.DialogClickListener() {
                    @Override
                    public void onClick(int id) {
                        if (id == R.id.btn_take_photo) {
                            Bitmap bitmap = ImageUtils.getBitmapFromCache(ealuationPicBean.imageUrl, mImageLoader);
                            if (bitmap != null) {
                                ImageUtils.saveImageToGallery(mContext, bitmap);
                                CommonUtils.soonToast(mContext, "保存成功");
                            } else {
                                CommonUtils.soonToast(mContext, "保存失败");
                            }
                        }
                    }
                });
                sendDongTaiDialog.show();
                return true;
            }
        });
    }
    /**
     * 设置网络图片加载规则
     * @param pb
     * @param ealuationPicBean
     */
    private void setupNetImage(final ProgressBar pb, final EaluationListBean.EaluationPicBean ealuationPicBean) {
        mImageLoader.displayImage(ealuationPicBean.imageUrl, imageView, pager_options, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                startLoad(pb);
                showExcessPic(ealuationPicBean, imageView);
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                overLoad( pb);
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                overLoad( pb);
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                overLoad( pb);
            }
        });
    }

    /**
     * 展示过度图片
     *
     * @param ealuationPicBean
     * @param imageView
     */
    private void showExcessPic(EaluationListBean.EaluationPicBean ealuationPicBean, PhotoView imageView) {
        Bitmap bitmap = ImageUtils.getBitmapFromCache(ealuationPicBean.smallImageUrl, mImageLoader);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        } else {
            imageView.setImageResource(R.drawable.home_youpin);
        }
    }

    /**
     * 显示进度条
     *
     * @param pb
     */
    private void startLoad( ProgressBar pb) {
        pb.setVisibility(View.VISIBLE);
    }

    /**
     * 结束进度条
     *
     * @param pb
     */
    private void overLoad( ProgressBar pb) {
        pb.setVisibility(View.GONE);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
//		super.destroyItem(container, position, object);
        container.removeView((View) object);
    }

    /**
     * 单击屏幕关闭
     *
     * @param view - View the user tapped.
     * @param x    - where the user tapped from the of the Drawable, as
     *             percentage of the Drawable width.
     * @param y    - where the user tapped from the top of the Drawable, as
     */
    @Override
    public void onPhotoTap(View view, float x, float y) {
        ((LookBigPicActivity) mContext).startActivityAnim();
    }
}
