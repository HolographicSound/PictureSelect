package com.zero.pictureselect.utils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.zero.pictureselect.model.MConstant;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by hjf on 2016/11/16 17:40.
 * Used to 意图(Intent)工具类
 */
public class IntentUtil {

    //打开相机 -- （返回）照相图片的位置
    public static String openCamera(Activity activity) {
        return openCamera(activity, null, CropUtil.getCropImageStoragePath(activity.getApplicationContext()));
    }

    public static String openCamera(Activity activity, Fragment fragment, String defaultPath) {
        //检测权限
        if (!MPermissionActionUtil.checkReadAndWritePermission(activity)) return null;
        if (!MPermissionActionUtil.checkCameraPermission(activity)) return null;
        //检测设备
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(activity.getApplicationContext(), "无内部储存设备", Toast.LENGTH_SHORT).show();
            return null;
        }
        // 新建目录
        File dir = new File(defaultPath);
        if (!dir.exists()) dir.mkdirs();
        // 生成文件名
        SimpleDateFormat timeSimple = new SimpleDateFormat("yyyyMMddssSSS", Locale.CHINA);
        String filename = "CM" + timeSimple.format(new Date()) + ".jpg";
//        filename = new Md5FileNameGenerator().generate(filename);//MD5加密
        // 新建文件
        File file = new File(defaultPath, filename);
        Intent takePhotos = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePhotos.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
        if (fragment == null) {
            activity.startActivityForResult(takePhotos, MConstant.RequestCode.OpenCamera);
        } else {
            fragment.startActivityForResult(takePhotos, MConstant.RequestCode.OpenCamera);
        }
        return file.getPath();
    }
}
