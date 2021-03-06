package com.zero.pictureselect;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;


import com.zero.pictureselect.LocalClipImageView.ImageCropActivity2;
import com.zero.pictureselect.adapter.ItemSpanDecoration;
import com.zero.pictureselect.adapter.PicAdapter;
import com.zero.pictureselect.model.LocalMedia;
import com.zero.pictureselect.model.LocalMediaFolder;
import com.zero.pictureselect.model.MConstant;
import com.zero.pictureselect.otherview.FolderWindow;
import com.zero.pictureselect.utils.IntentUtil;
import com.zero.pictureselect.utils.LocalMediaLoader;

import java.util.ArrayList;

/**
 * Created by hjf on 2016/10/10.
 * Used to 图片浏览界面
 */
public class PictureSelectActivity extends AppCompatActivity implements View.OnClickListener, PicAdapter.OnPictureAction {


    //视图控件
    private Toolbar toolbar;
    private RecyclerView mRecyclerView;
    private TextView picDirView, preView, sendView;
    private FolderWindow folderWindow;


    private PicAdapter picAdapter;
    private int actionCode = 1;//0裁剪 1选择
    private int selectMaxNum = 8;
    private String cameraImagePath;
    //所有的文件夹数据
    private ArrayList<LocalMediaFolder> allImageFolders;


    //外部进来的方法
    public static void startCrop(Activity activity) {
        Intent intent = _getIntent(activity.getApplicationContext(), 0);
        activity.startActivityForResult(intent, MConstant.RequestCode.ImageCrop);
    }

    public static void startCrop(Fragment fragment) {
        Intent intent = _getIntent(fragment.getContext(), 0);
        fragment.startActivityForResult(intent, MConstant.RequestCode.ImageCrop);
    }

    public static void startSelect(Activity activity, int selectMaxNum) {
        Intent intent = _getIntent(activity.getApplicationContext(), 1);
        intent.putExtra("maxNum", selectMaxNum);
        activity.startActivityForResult(intent, MConstant.RequestCode.PictureSelect);
    }

    private static Intent _getIntent(Context context, int actionCode) {
        Intent intent = new Intent(context, PictureSelectActivity.class);
        intent.putExtra("actionCode", actionCode);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        actionCode = getIntent().getIntExtra("actionCode", 1);
        selectMaxNum = getIntent().getIntExtra("maxNum", 8);
        setContentView(R.layout.a_picture_select);
        loadLocalImage();
    }


    @Override
    public void onContentChanged() {
        toolbar = (Toolbar) findViewById(R.id.toolBar);
        toolbar.setTitle(getString(R.string.app_name));
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.mipmap.title_back);

        mRecyclerView = (RecyclerView) findViewById(R.id.v_recyclerView);
        picDirView = (TextView) findViewById(R.id.folder_name);
        preView = (TextView) findViewById(R.id.t_toPreview);
        sendView = (TextView) findViewById(R.id.t_toSend);
        if (actionCode == 0) {
            preView.setVisibility(View.GONE);
            sendView.setVisibility(View.GONE);
        }
        initListener();
        initRecyclerView();
    }


    private void initListener() {
        sendView.setOnClickListener(this);
        preView.setOnClickListener(this);
        findViewById(R.id.folder_layout).setOnClickListener(this);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initRecyclerView() {
        mRecyclerView.setLayoutManager(new GridLayoutManager(getApplicationContext(), 3));
        mRecyclerView.addItemDecoration(new ItemSpanDecoration(1));
        mRecyclerView.setAdapter(picAdapter = new PicAdapter(getApplicationContext(), actionCode, selectMaxNum));
        picAdapter.setOnPictureActionListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            //发送
            case R.id.t_toSend:
                sendSelectData();
                break;

            //预览
            case R.id.t_toPreview:
                ArrayList<String> selectMediasPath = picAdapter.getSelectMediasPath();
                ArrayList<LocalMedia> selectMedia = new ArrayList<>();
                for (String path : selectMediasPath) {
                    selectMedia.add(new LocalMedia(path, -1));
                }
                jump2preview(selectMedia, 0, selectMediasPath);
                break;

            //选择图片目录
            case R.id.folder_layout:
                if (folderWindow == null) {
                    folderWindow = FolderWindow.newInstance(getApplicationContext());
                    folderWindow.bindFolders(allImageFolders);
                    folderWindow.setOnFolderSelectedListener(new FolderWindow.OnFolderSelectedListener() {
                        @Override
                        public void onFolderSelectedListener(LocalMediaFolder data) {
                            picAdapter.bindImage(data.getMedias());
                        }
                    });
                }
                if (folderWindow.isShowing()) {
                    folderWindow.dismiss();
                } else {
                    folderWindow.showAsDropDown(toolbar);
                }
                break;
        }
    }


    private void loadLocalImage() {
        LocalMediaLoader.startLoader(this, MConstant.MEDIA_TYPE_IMAGE, new LocalMediaLoader.LocalMediaLoadListener() {
            @Override
            public void loadComplete(ArrayList<LocalMediaFolder> data) {
                allImageFolders = data;
                ArrayList<LocalMedia> medias = data.get(0).getMedias();
                String folderName = data.get(0).getName();
                picDirView.setText(folderName);
                picAdapter.bindImage(medias);
            }
        });
    }


    @Override
    public void onPictureSelect(int selectNum) {
        if (selectNum == 0) {
            preView.setEnabled(false);
            sendView.setEnabled(false);
            preView.setText(getString(R.string.s_yl));
            sendView.setText(getString(R.string.s_fs));
        } else {
            preView.setEnabled(true);
            sendView.setEnabled(true);
            preView.setText(getString(R.string.s_ylNum, selectNum));
            sendView.setText(getString(R.string.s_fsNum, selectNum, selectMaxNum));
        }
    }

    @Override
    public void onTakePhoto() {
        cameraImagePath = IntentUtil.openCamera(this);
    }

    @Override
    public void onPictureClick(int position) {
        if (actionCode == 0) {//截图(两种截图界面)
//            ImageCropActivity.start(this, picAdapter.getData(position).getCropImageStoragePath());
            ImageCropActivity2.start(this, picAdapter.getData(position).getPath());
        } else if (actionCode == 1) {//选图
            jump2preview(picAdapter.getDatas(), position, picAdapter.getSelectMediasPath());
        }
    }


    private void jump2preview(ArrayList<LocalMedia> data, int position, ArrayList<String> selectData) {
        PicturePreviewActivity.start(this, data, position, selectData, selectMaxNum);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) return;
        switch (requestCode) {

            //多选返回
            case MConstant.RequestCode.PictureSelect:
                ArrayList<String> tempSelectMedias = data.getStringArrayListExtra(MConstant.ResultDataKey.PICTURE_SELECT_DATA);
                picAdapter.setSelectMedias(tempSelectMedias);
                if (data.getBooleanExtra(MConstant.ResultDataKey.PICTURE_PREVIEW_IS_DONE, false)) {
                    sendSelectData();
                }
                break;

            //切图返回
            case MConstant.RequestCode.ImageCrop:
                String pathCrop = data.getStringExtra(MConstant.ResultDataKey.PICTURE_CLIP_DATA);
                sendClipData(pathCrop);
                break;

            //照相返回
            case MConstant.RequestCode.OpenCamera:
                if (actionCode == 0) {
                    ImageCropActivity.start(this, cameraImagePath);
                } else if (actionCode == 1) {
                    ArrayList<String> selectMediasPath = new ArrayList<>();
                    selectMediasPath.add(cameraImagePath);
                    ArrayList<LocalMedia> selectMedia = new ArrayList<>();
                    selectMedia.add(new LocalMedia(cameraImagePath, -1));
                    jump2preview(selectMedia, 0, selectMediasPath);
                }
                break;

            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }


    private void sendClipData(String path) {
        setResult(RESULT_OK, new Intent().putExtra(MConstant.ResultDataKey.PICTURE_CLIP_DATA, path));
        finish();
    }

    //发送消息会原来界面
    private void sendSelectData() {
        ArrayList<String> selectMedias = picAdapter.getSelectMediasPath();
        setResult(RESULT_OK, new Intent().putStringArrayListExtra(MConstant.ResultDataKey.PICTURE_SELECT_DATA, selectMedias));
        finish();
    }
}
