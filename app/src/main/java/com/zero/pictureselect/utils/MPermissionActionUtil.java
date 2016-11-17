package com.zero.pictureselect.utils;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;


/**
 * Android M(6.0) 危险权限组（Dangerous Permission）
 * 当允许CALL_PHONE权限时，CALL_PHONE所在的权限组（permission_group.PHONE）的其他权限也会被获取
 * android.permission-group  --  android.permission
 * CALENDAR     --  READ_CALENDAR、WRITE_CALENDAR
 * CAMERA       --  CAMERA
 * CONTACTS     --  READ_CALENDAR、WRITE_CALENDAR、GET_ACCOUNTS
 * LOCATION     --  ACCESS_FINE_LOCATION、ACCESS_COARSE_LOCATION
 * MICROPHONE   --  RECORD_AUDIO
 * PHONE        --  READ_PHONE_STATE、CALL_PHONE、READ_CALL_LOG、WRITE_CALL_LOG、ADD_VOICEMAIL、USE_SIP、PROCESS_OUTGOING_CALLS
 * SENSORS      --  BODY_SENSORS
 * SMS          --  SEND_SMS、RECEIVE_SMS、READ_SMS、RECEIVE_WAP_PUSH、RECEIVE_MMS、READ_CELL_BROADCASTS
 * STORAGE      --  READ_EXTERNAL_STORAGE、WRITE_EXTERNAL_STORAGE
 * TODO 用户拒绝并禁止访问处理
 */
public class MPermissionActionUtil {

    final public static int REQUEST_CODE_ASK_CALL_PHONE = 101;//电话权限
    final public static int REQUEST_CODE_ASK_READ_PHONE_STATE = 102;//手机信息,IM
    final public static int REQUEST_CODE_ASK_CAMERA = 103;//相机权限
    final public static int REQUEST_CODE_ASK_READ_WRITE = 105;//读写权限
    final public static int REQUEST_CODE_ASK_Location = 106;//定位权限


    /**
     * 获取手机信息权限，和 CALL_PHONE 在一个分组
     * 登录的时候询问，接受完成后完成IM的初始化，拒绝的话不进行IM的初始化操作
     */
    public static boolean checkMicrophonePermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= 23) {
            int checkStorePermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO);
            if (checkStorePermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_CODE_ASK_READ_PHONE_STATE);
                return false;
            }
        }
        return true;
    }

    /**
     * 仅仅查询是否具有权限
     */
    public static boolean checkReadPhoneStatePermission(Context context) {
        if (Build.VERSION.SDK_INT >= 23) {
            int checkSelfPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE);
            return checkSelfPermission == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    public static boolean checkReadPhoneStatePermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= 23) {
            int checkStorePermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_PHONE_STATE);
            if (checkStorePermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_CODE_ASK_READ_PHONE_STATE);
                return false;
            }
        }
        return true;
    }

    /**
     * 获取储存权限
     */
    public static boolean checkReadAndWritePermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= 23) {
            int checkStorePermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);
            if (checkStorePermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_ASK_READ_WRITE);
                return false;
            }
        }
        return true;
    }

    /**
     * 定位权限
     */
    public static boolean checkLocationPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= 23) {
            int checkFinePermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION);
            if (checkFinePermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_ASK_Location);
                return false;
            }
        }
        return true;
    }

    public static boolean checkCameraPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= 23) {
            int checkFinePermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA);
            if (checkFinePermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_ASK_CAMERA);
                return false;
            }
        }
        return true;
    }
}
