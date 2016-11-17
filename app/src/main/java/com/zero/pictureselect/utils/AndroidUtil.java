package com.zero.pictureselect.utils;

import android.content.Context;
import android.os.storage.StorageManager;

import java.lang.reflect.Method;

/**
 * Created by hjf on 2016/11/17 19:11.
 * Used to
 */
public class AndroidUtil {

    //一般情况：0内存 1sd卡 2外置sd卡
    public static String getStorageDirectory(Context context, int position) {
        if (position > 2 || position < 0) position = 0;
        String returnPath = null;
        StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        try {
            Class<?>[] paramClasses = {};
            Method getVolumePathsMethod = StorageManager.class.getMethod("getVolumePaths", paramClasses);
            getVolumePathsMethod.setAccessible(true);
            Object[] params = {};
            Object invoke = getVolumePathsMethod.invoke(storageManager, params);
            returnPath = ((String[]) invoke)[0];
        } catch (Exception e) {
        }
        return returnPath;
    }
}
