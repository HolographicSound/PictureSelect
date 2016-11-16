package com.zero.pictureselect.model;

import android.os.Environment;

/**
 * Created by hjf on 2016/11/10 10:27.
 * Used to 常量存有接口
 */
public interface Constant {

    int MEDIA_TYPE_IMAGE = 1;
    int MEDIA_TYPE_VIDEO = 2;

    String SCHEME_FILE = "file";
    String SCHEME_CONTENT = "content";

    //照片临时存放目录
    String IMAGE_TMMP_PATH = Environment.getExternalStorageDirectory() + "/CMteams/tempPath/";

    //请求Code
    interface RequestCode {
        int PictureSelect = 101;
        int ImageCrop = 103;
    }

    //Activity 返回数据用的key
    interface ResultDataKey {
        String DATA_LIST0 = "data_list0";
        String DATA_LIST1 = "data_list1";


        String PICTURE_SELECT_DATA = "PictureSelectData";
        String PICTURE_PREVIEW_IS_DONE = "PicturePreviewIsDown";
        String PICTURE_CLIP_DATA = "pictureClipData";
    }
}
