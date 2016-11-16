package com.zero.pictureselect;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.isseiaoki.simplecropview.CropImageView;
import com.zero.pictureselect.model.Constant;
import com.zero.pictureselect.utils.BitmapUtil;
import com.zero.pictureselect.utils.CropUtil;
import com.zero.pictureselect.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * Created by hjf on 2016/11/10 17:53.
 * Used to 图片剪裁功能
 */
public class ImageCropActivity extends AppCompatActivity implements View.OnClickListener {

    private Toolbar toolbar;
    private CropImageView cropImageView;//剪裁ImageView
    private View okView;

    private Uri sourceUri, saveUri;

    public static void start(Activity a, String imagePath) {
        Intent intent = new Intent(a.getApplicationContext(), ImageCropActivity.class);
        intent.putExtra("imagePath", imagePath);
        a.startActivityForResult(intent, Constant.RequestCode.ImageCrop);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_image_crop);

        initData();
        initView();
        initListener();
    }

    private void initData() {
        String imagePath = getIntent().getStringExtra("imagePath");
        File file = new File(imagePath);
        if (file == null) {
            Toast.makeText(getApplicationContext(),
                    getApplicationContext().getString(R.string.message_error1), Toast.LENGTH_LONG).show();
            finish();
        }
        sourceUri = Uri.fromFile(file);
    }

    private void initView() {
        toolbar = (Toolbar) findViewById(R.id.toolBar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.mipmap.title_back);

        okView = findViewById(R.id.t_ok);
        cropImageView = (CropImageView) findViewById(R.id.cropImageView);

        cropImageView.setHandleSizeInDp(10);

        //获取文件的旋转角度
        File file = CropUtil.getFile4Uri(this, getContentResolver(), sourceUri);
        int exifRotation = BitmapUtil.getExifRotation(file.getPath());
//        int exifRotation = BitmapUtil.getExifRotation(sourceUri.getPath());

        // 设置剪裁用的Bitmap
        InputStream is = null;
        try {
            int sampleSize = BitmapUtil.getBitmapSampleSize(getContentResolver(), sourceUri);
            is = getContentResolver().openInputStream(sourceUri);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = sampleSize;
            Bitmap sizeBitmap = BitmapFactory.decodeStream(is, null, options);
            if (sizeBitmap == null) return;
            Matrix matrix = BitmapUtil.getRotateMatrix(sizeBitmap, exifRotation % 360);
            Bitmap rotatedBitmap = Bitmap.createBitmap(sizeBitmap, 0, 0,
                    sizeBitmap.getWidth(), sizeBitmap.getHeight(), matrix, true);
            cropImageView.setImageBitmap(rotatedBitmap);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            CropUtil.closeStream(is);
        }
    }

    private void initListener() {
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        okView.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            //OK 确认剪裁
            case R.id.t_ok:
                ProgressDialog.show(ImageCropActivity.this, null, getString(R.string.save_ing), true, false);
                saveUri = Uri.fromFile(FileUtils.createCropFile(ImageCropActivity.this));
                saveOutput(cropImageView.getCroppedBitmap());
                break;
        }
    }


    private void saveOutput(Bitmap croppedImage) {
        if (saveUri != null) {
            OutputStream outputStream = null;
            try {
                outputStream = getContentResolver().openOutputStream(saveUri);
                if (outputStream != null) {
                    croppedImage.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                CropUtil.closeStream(outputStream);
            }
            setResult(RESULT_OK, new Intent().putExtra(Constant.ResultDataKey.PICTURE_CLIP_DATA, saveUri.getPath()));
        }
        final Bitmap b = croppedImage;
        new Handler().post(new Runnable() {
            public void run() {
                b.recycle();
            }
        });
        finish();
    }
}
