package com.zero.pictureselect;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.zero.pictureselect.model.MConstant;
import com.zero.pictureselect.utils.MyImageLoader;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;


/**
 * Created by hjf on 2016/10/10.
 * Used to
 */
public class MainActivity extends AppCompatActivity {

    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_main);
        StorageManager storageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
        try {
            Class<?>[] paramClasses = {};
            Method getVolumePathsMethod = StorageManager.class.getMethod("getVolumePaths", paramClasses);
            getVolumePathsMethod.setAccessible(true);
            Object[] params = {};
            //一般情况：0内存 1sd卡 2外置sd卡
            Object invoke = getVolumePathsMethod.invoke(storageManager, params);
            for (int i = 0; i < ((String[]) invoke).length; i++) {
                System.out.println(((String[]) invoke)[i]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onContentChanged() {
        imageView = (ImageView) findViewById(R.id.imageView);
    }

    public void toSelectPicture(View v) {
        PictureSelectActivity.startSelect(this, 1);
    }

    public void toCropPicture(View v) {
        PictureSelectActivity.startCrop(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) return;
        switch (requestCode) {
            case MConstant.RequestCode.PictureSelect:
                ArrayList<String> selectMedias = data.getStringArrayListExtra(MConstant.ResultDataKey.PICTURE_SELECT_DATA);
                break;
            case MConstant.RequestCode.ImageCrop:
                String cropImagePath = data.getStringExtra(MConstant.ResultDataKey.PICTURE_CLIP_DATA);
                MyImageLoader.display(cropImagePath, imageView);
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
