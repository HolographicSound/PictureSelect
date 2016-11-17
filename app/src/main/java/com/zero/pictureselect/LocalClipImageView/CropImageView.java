package com.zero.pictureselect.LocalClipImageView;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.RelativeLayout;

import com.zero.pictureselect.R;
import com.zero.pictureselect.utils.MyImageLoader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 图片切割视图
 */
public class CropImageView extends RelativeLayout {
    private ZoomImageView mZoomImageView;
    private CropImageBorderView mCropImageBorderView;


    //水平边距
    private int mHorizontalPadding = 20;
    //画笔颜色
    private int color = Color.parseColor("#FFFFFF");
    //需要切割的图片
    private Drawable ClipDrawable;

    public CropImageView(Context context) {
        super(context);
        init(context);
    }

    public CropImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ClipImageLayout);
        mHorizontalPadding = a.getInt(R.styleable.ClipImageLayout_HorizontalPadding, mHorizontalPadding);
        color = a.getColor(R.styleable.ClipImageLayout_BorderColor, color);
        ClipDrawable = a.getDrawable(R.styleable.ClipImageLayout_ClipDrawable);
        init(context);

    }

    private void init(Context c) {
        mZoomImageView = new ZoomImageView(c);
        mCropImageBorderView = new CropImageBorderView(c);
        android.view.ViewGroup.LayoutParams lp = new LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT);
        // 计算padding的px
        mHorizontalPadding = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, mHorizontalPadding, getResources()
                        .getDisplayMetrics());
        mZoomImageView.setHorizontalPadding(mHorizontalPadding);
        mCropImageBorderView.setHorizontalPadding(mHorizontalPadding);
        mCropImageBorderView.setmBorderColor(color);
        if (ClipDrawable != null)
            mZoomImageView.setImageDrawable(ClipDrawable);
        this.addView(mZoomImageView, lp);
        this.addView(mCropImageBorderView, lp);
    }


    /**
     * 对外公布设置边距的方法,单位为dp
     */
 /*   public void setHorizontalPadding(int mHorizontalPadding) {
        this.mHorizontalPadding = mHorizontalPadding;
        mZoomImageView.setHorizontalPadding(mHorizontalPadding);
        mClipImageBorderView.setHorizontalPadding(mHorizontalPadding);
        mZoomImageView.invalidate();
        mClipImageBorderView.invalidate();
    }*/

    /**
     * 设置边框颜色.默认白色1px
     *
     * @param color
     */
   /* public void setBorderColor(int color) {
        this.color = color;
        mClipImageBorderView.setmBorderColor(color);
        mClipImageBorderView.invalidate();
    }*/

    /**
     * 设置剪裁的图片
     *
     * @param d drawable
     */
    /*public void setClipDrawable(Drawable d) {
        mZoomImageView.setImageDrawable(d);
    }*/

    /**
     * 设置剪裁的图片
     * 要自己处理好bitmap OOM问题 建议使用setClipUrl方法
     *
     * @param b bitmap
     */
    /*public void setClipBitmap(Bitmap b) {
        mZoomImageView.setImageBitmap(b);
    }*/

    /**
     * 设置剪裁的图片
     * 要自己处理好bitmap OOM问题 建议使用setClipUrl方法
     *
     * @param uri uri
     */
    /*public void setClipUri(Uri uri) {
        mZoomImageView.setImageURI(uri);
    }*/

    /**
     * 设置剪裁的图片，剪裁是会先从缓存中拿去图片
     */
    /*public void setClipUrl(String path) {
        ImageLoader.getInstance().loadImage(path, mZoomImageView);
    }*/

    /**
     * 设置剪裁的图片
     */
    public void setClipUrl(String path) {
        MyImageLoader.display(path, mZoomImageView);
    }

    /**
     * 裁切图片
     */
    public Bitmap clip() {
        return mZoomImageView.clip();
    }

    /**
     * @return 把剪裁后的图片保存在指定的path并返回完整路径名
     */
    public String clip(String path) {
        FileOutputStream out = null;
        Bitmap b = null;
        File f = new File(path);
        if (!f.exists())
            f.mkdirs();
        SimpleDateFormat t = new SimpleDateFormat("yyyyMMddssSSS", Locale.CHINA);
        String filename = "CM" + (t.format(new Date())) + ".jpg";
        // 有必要可以使用 MD5
        // filename = new Md5FileNameGenerator().generate(filename);//MD5
        File filePath = new File(f, filename);
        try {
            b = clip();
            out = new FileOutputStream(filePath);
            b.compress(Bitmap.CompressFormat.JPEG, 80, out);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.flush();
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (b != null)
                b.recycle();
        }
        return filePath.getPath();
    }

    //递归生成文件夹
    private boolean makeFileDirs(File dir) {
        if (!dir.exists() && makeFileDirs(dir.getParentFile())) dir.mkdir();
        return true;
    }
}
