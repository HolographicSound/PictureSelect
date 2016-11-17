package com.zero.pictureselect.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;


import com.zero.pictureselect.Photoview.PhotoView;
import com.zero.pictureselect.Photoview.PhotoViewAttacher;
import com.zero.pictureselect.model.LocalMedia;
import com.zero.pictureselect.utils.BitmapUtil;
import com.zero.pictureselect.utils.MyImageLoader;
import com.zero.pictureselect.utils.ScreenUtils;

import java.util.ArrayList;


/**
 * Created by hjf on 2016/11/11 17:42.
 * Used to 大图预览 ViewPager 的适配器
 */
public class PreViewPagerAdapter extends PagerAdapter {

    private ArrayList<LocalMedia> dataList;
    private boolean isCyclic = false;
    private double phoneRatio = 0;
    private OnClickListener onPictureClickListener;

    public PreViewPagerAdapter(ArrayList<LocalMedia> dataList) {
        this.dataList = dataList;
        if (isCyclic) isCyclic = dataList.size() != 1;
    }

    public LocalMedia getData(int position) {
        return dataList.get(position % dataList.size());//无限循环
    }

    // getCount() 在可循环是展示的是 Integer 的最大值
    public int getMyCount() {
        return dataList.size();
    }

    @Override
    public int getCount() {
        return isCyclic ? Integer.MAX_VALUE : dataList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        // TODO: 2016/11/11 对网络图片增加支持
        final Context context = container.getContext();

        //计算图片比例、屏幕比例
        if (phoneRatio == 0) {
            phoneRatio = ScreenUtils.getScreenHeight(context) * 1d / ScreenUtils.getScreenWidth(context);
        }
        double imageRatio = 1;
        LocalMedia localMedia = getData(position);
        int[] imageSize = BitmapUtil.getBitmapWidthAndHeight(localMedia.getPath());
        if (imageSize[0] != 0 && imageSize[1] != 0) {
            imageRatio = imageSize[1] * 1d / imageSize[0];
        }

        //设置图片展示方式
        final PhotoView photoView = new PhotoView(context);
        if (imageRatio > phoneRatio) {
            photoView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else {
            photoView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        }

        //图片加载、点击
        MyImageLoader.display(localMedia.getPath(), photoView);
        photoView.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
            @Override
            public void onViewTap(View view, float x, float y) {
                if (onPictureClickListener == null) return;
                onPictureClickListener.onClick(photoView);
            }
        });
        container.addView(photoView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        return photoView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }


    public void setOnPictureClickListener(OnClickListener onPictureClickListener) {
        this.onPictureClickListener = onPictureClickListener;
    }
}
