package com.zero.pictureselect.LocalClipImageView;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.zero.pictureselect.R;
import com.zero.pictureselect.model.MConstant;


/**
 * 默认的裁剪窗体(默认剪裁后返回剪裁后图片的临时保存地址)
 */
public class ImageCropActivity2 extends AppCompatActivity implements View.OnClickListener {

    private CropImageView mClipImageLayout;

    public static void start(Activity a, String imagePath) {
        Intent i = new Intent(a.getApplicationContext(), ImageCropActivity2.class);
        i.putExtra("imagePath", imagePath);
        a.startActivityForResult(i, MConstant.RequestCode.ImageCrop);
    }

    public static void start(Fragment fragment, String imagePath) {
        Intent i = new Intent(fragment.getContext(), ImageCropActivity2.class);
        i.putExtra("imagePath", imagePath);
        fragment.startActivityForResult(i, MConstant.RequestCode.ImageCrop);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_image_crop2);
        mClipImageLayout = (CropImageView) findViewById(R.id.id_clipImageLayout);
        findViewById(R.id.clip_back).setOnClickListener(this);
        findViewById(R.id.clip_commit).setOnClickListener(this);
        setClipImage();

    }

    private void setClipImage() {
        String url = getIntent().getStringExtra("imagePath");
        if (url != null) {
            mClipImageLayout.setClipUrl(url);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            //保存在临时文件返回
            case R.id.clip_commit:
                ProgressDialog d = ProgressDialog.show(this, null, "正在剪裁..");
                String path = mClipImageLayout.clip(MConstant.IMAGE_TEMP_PATH);
                Intent i = new Intent();
                i.putExtra(MConstant.ResultDataKey.PICTURE_CLIP_DATA, path);
                setResult(RESULT_OK, i);
                d.dismiss();
                finish();
                break;

            //结束界面
            case R.id.clip_back:
                finish();
                break;
        }
    }
}
