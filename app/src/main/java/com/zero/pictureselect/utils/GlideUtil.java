package com.zero.pictureselect.utils;

import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.zero.pictureselect.app.MyApplication;
import com.zero.pictureselect.R;

import java.io.File;

/**
 * Created by hjf on 2016/10/10.
 * Used to 使用Glide
 */
public class GlideUtil {

    public static void load4Url(String imagePath, ImageView imageView) {
        Glide
                .with(MyApplication.getMyContext())
                .load(imagePath)
                .centerCrop()
                .error(R.mipmap.loading_failed)
                //磁盘缓存行为。ALL:存所有(默)，NONE:不缓存，SOURCE:存源图像，RESULT:存目标图像（低分辨率）
//                .diskCacheStrategy(DiskCacheStrategy.NONE)
                //跳过内存缓存
//                .skipMemoryCache(true)
                //设置图片显示优先级：IMMEDIATE(立即) ...
                .priority(Priority.priority)
                .into(imageView);
    }

    public static void load4Path(String filePath, ImageView imageView) {
        Glide
                .with(MyApplication.getMyContext())
                .load(new File(filePath))
                .error(R.mipmap.loading_failed)
                .centerCrop()
                .into(imageView);
    }
}
