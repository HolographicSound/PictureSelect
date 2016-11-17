package com.zero.pictureselect.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.text.TextUtils;


import com.zero.pictureselect.model.MConstant;

import java.io.Closeable;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by hjf on 2016/11/15 16:14.
 * Used to 图片剪裁工具类
 */
public class CropUtil {

    public static void closeStream(Closeable closeable) {
        if (closeable == null) return;
        try {
            closeable.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getCropImageStoragePath(Context context) {
        return AndroidUtil.getStorageDirectory(context, 0) + "/DCIM/Screenshots/";
    }

    public static File getFile4Uri(Context context, ContentResolver resolver, Uri uri) {
        if (uri == null) return null;
        if (MConstant.SCHEME_FILE.equals(uri.getScheme())) {
            return new File(uri.getPath());
        } else if (MConstant.SCHEME_CONTENT.equals(uri.getScheme())) {
            String[] filePathColumn = {MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.DISPLAY_NAME};
            Cursor cursor = null;
            try {
                cursor = resolver.query(uri, filePathColumn, null, null, null);
                if (cursor != null) {
                    int columIndex = uri.toString().startsWith("content://com.google.android.gallery3d") ?
                            cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME) :
                            cursor.getColumnIndex(MediaStore.MediaColumns.DATA);
                    if (columIndex != -1) {
                        String filePath = cursor.getString(columIndex);
                        if (!TextUtils.isEmpty(filePath))
                            return new File(filePath);
                    }
                }
            } catch (IllegalArgumentException e) {
                // Google Drive images
                return getFromMediaUriPfd(context, resolver, uri);
            } catch (SecurityException e) {
                // Nothing we can do
            } finally {
                if (cursor != null) cursor.close();
            }
        }
        return null;
    }


    private static File getFromMediaUriPfd(Context context, ContentResolver resolver, Uri uri) {
        if (uri == null) return null;
        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {
            ParcelFileDescriptor pfd = resolver.openFileDescriptor(uri, "r");
            FileDescriptor fd = pfd.getFileDescriptor();
            fis = new FileInputStream(fd);

            String outputFilePath = getTempFileName(context);
            fos = new FileOutputStream(outputFilePath);

            int read;
            byte[] bytes = new byte[4096];
            while ((read = fis.read(bytes)) != -1) {
                fos.write(bytes, 0, read);
            }
            return new File(outputFilePath);
        } catch (IOException e) {
            // Nothing we can do
        } finally {
            closeStream(fis);
            closeStream(fos);
        }
        return null;
    }

    private static String getTempFileName(Context context) throws IOException {
        File outputDir = context.getCacheDir();
        File outputFile = File.createTempFile("image", "tmp", outputDir);
        return outputFile.getAbsolutePath();
    }
}
