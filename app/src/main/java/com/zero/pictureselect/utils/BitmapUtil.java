package com.zero.pictureselect.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.opengl.GLES10;


import com.zero.pictureselect.app.MyApplication;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by hjf on 2016/10/10.
 * Used to 进行本地图片的读取、压缩
 */
public class BitmapUtil {

    private static final int SIZE_DEFAULT = 2048;
    private static final int SIZE_LIMIT = 4096;

    public static int[] getBitmapWidthAndHeight(String imagePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);
        return new int[]{options.outWidth, options.outHeight};
    }

    //获取缩放比例
    public static int getBitmapSampleSize(ContentResolver resolver, Uri uri) throws IOException {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        InputStream is = resolver.openInputStream(uri);
        BitmapFactory.decodeStream(is, null, options);
        CropUtil.closeStream(is);
        int maxSize = getMaxImageSize();
        return _calculatedScaleNum(maxSize, maxSize, options);
    }

    private static int getMaxImageSize() {
        int textureLimit = getMaxTextureSize();
        if (textureLimit == 0) {
            return SIZE_DEFAULT;
        } else {
            return Math.min(textureLimit, SIZE_LIMIT);
        }
    }

    private static int getMaxTextureSize() {
        int[] maxSize = new int[1];
        GLES10.glGetIntegerv(GLES10.GL_MAX_TEXTURE_SIZE, maxSize, 0);
        return maxSize[0];
    }

    //缩略图
    public static Bitmap compressSize(String imagePath, int toWidth, int toHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);
        int scaleNum = _calculatedScaleNum(toWidth, toHeight, options);
        options.inSampleSize = Math.max(1, scaleNum);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(imagePath, options);
    }


    //原图：太大，根据屏幕来好了
    public static Bitmap compressSize(String imagePath) {
        Context myContext = MyApplication.getMyContext();
        int screenWidth = ScreenUtils.getScreenWidth(myContext);
        int screenHeight = ScreenUtils.getScreenHeight(myContext);
        return compressSize(imagePath, screenWidth, screenHeight);
    }


    //计算缩放比例
    private static int _calculatedScaleNum(int toWidth, int toHeight, BitmapFactory.Options options) {
        int outWidth = options.outWidth;
        int outHeight = options.outHeight;
        int scaleNum = 1;
        //此方法压缩大了一倍 最大比数4.5  方法一8  方法二5
        /*while (outWidth / scaleNum > toWidth || outHeight / scaleNum > toHeight) {
            scaleNum = scaleNum << 1;
        }*/
        if (outWidth > toWidth || outHeight > toHeight) {
            double scaleW = outWidth / toWidth * 1d;
            double scaleH = outHeight / toHeight * 1d;
            scaleNum = (int) Math.ceil(Math.max(scaleW, scaleH));
        }
        return scaleNum;
    }

    /**
     * 质量压缩 TODO 测试
     *
     * @param fileByteLength 单位：KB
     */
    public static Bitmap compressQuality(String imagePath, int fileByteLength) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int percent = 100;
        Bitmap bitmap = compressSize(imagePath);
        bitmap.compress(Bitmap.CompressFormat.JPEG, percent, baos);
        fileByteLength *= 1024;
        //图片大小和规定大小比较
        if (baos.toByteArray().length <= fileByteLength)
            return bitmap;
        while (baos.toByteArray().length > fileByteLength) {
            baos.reset();
            percent -= 10;
            bitmap.compress(Bitmap.CompressFormat.JPEG, percent, baos);
        }
        byte[] bytes = baos.toByteArray();
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }


    public static Bitmap compressQuality(String imagePath) {
        //逢低i地方
        return compressQuality(imagePath, 2);
    }


    /**
     * Bitmap 角度调整,回收传入的Bitmap，只保留一个
     */
    public static Bitmap adjustBitmapDegree(String imagePath, Bitmap bitmap) {
        //根据图片文件获取 EXIF信息、获取角度信息
        int degree = getExifRotation(imagePath);

        //调整 Bitmap 的角度
        if (degree == 0) return bitmap;

        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap newBitmap;
        try {
            newBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } catch (OutOfMemoryError e) {
            newBitmap = null;
        }
        if (newBitmap == null) {
            newBitmap = bitmap;
        } else {
            bitmap.recycle();
        }
        return newBitmap;
    }

    //获取exif的旋转信息
    public static int getExifRotation(String imagePath) {
        try {
            ExifInterface exifInterface = new ExifInterface(imagePath);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return 90;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return 180;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return 270;
                default:
                    return ExifInterface.ORIENTATION_UNDEFINED;
            }
        } catch (Exception e) {
            return 0;
        }
    }

    //获取旋转矩阵
    public static Matrix getRotateMatrix(Bitmap bitmap, int rotation) {
        Matrix matrix = new Matrix();
        if (bitmap != null && rotation != 0) {
            int cx = bitmap.getWidth() / 2;
            int cy = bitmap.getHeight() / 2;
            matrix.preTranslate(-cx, -cy);
            matrix.postRotate(rotation);
            matrix.postTranslate(cx, cy);
        }
        return matrix;
    }
}
