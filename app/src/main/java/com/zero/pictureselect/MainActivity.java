package com.zero.pictureselect;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.zero.pictureselect.model.Constant;

import java.util.ArrayList;


/**
 * Created by hjf on 2016/10/10.
 * Used to
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_main);
    }

    public void toSelectPicture(View v) {
        PictureSelectActivity.startSelect(this);
    }

    public void toCropPicture(View v) {
        PictureSelectActivity.startCrop(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) return;
        switch (requestCode) {
            case Constant.RequestCode.PictureSelect:
                ArrayList<String> selectMedias = data.getStringArrayListExtra(Constant.ResultDataKey.PICTURE_SELECT_DATA);
                break;
            case Constant.RequestCode.ImageCrop:
                String cropImagePath = data.getStringExtra(Constant.ResultDataKey.PICTURE_CLIP_DATA);
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
