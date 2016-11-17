package com.zero.pictureselect;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;


import com.zero.pictureselect.adapter.PreViewPagerAdapter;
import com.zero.pictureselect.model.LocalMedia;
import com.zero.pictureselect.model.MConstant;
import com.zero.pictureselect.otherview.PreviewViewPager;

import java.util.ArrayList;

/**
 * Created by hjf on 2016/11/10 17:53.
 * Used to 相册大图预览
 */
public class PicturePreviewActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener, View.OnClickListener {


    private ArrayList<String> selectData;
    private int position;
    private int selectMaxNum;
    private View selectBar, statusBarSpace;
    private Toolbar toolbar;
    private CheckBox selectCheckBox;
    private TextView sendView;
    private PreviewViewPager preVP;
    private PreViewPagerAdapter adapter;
    private boolean isShowBar = true; //默认显示StatusBar


    //多图预览
    public static void start(Activity a, ArrayList<LocalMedia> data, int position, ArrayList<String> selectData, int selectMaxNum) {
        Intent intent = new Intent(a.getApplicationContext(), PicturePreviewActivity.class);
        intent.putParcelableArrayListExtra("mediaData", data);
        intent.putExtra("position", position);
        intent.putExtra("selectData", selectData);
        intent.putExtra("maxNum", selectMaxNum);
        a.startActivityForResult(intent, MConstant.RequestCode.PictureSelect);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //FLAG_LAYOUT_IN_SCREEN：窗口占满整个屏幕，忽略周围的装饰边框（例如状态栏）。此窗口需考虑到装饰边框的内容。
        //FLAG_LAYOUT_NO_LIMITS：直接呈现全屏状态的界面效果，全屏、非全屏切换时不会重写调整界面
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.a_picture_preview);
        initData();
        initView();
        initListener();

        //初始化显示的动作
        preVP.setCurrentItem(position);
        //初始化为 0，此时 position 为 0 时，不会触发 onPageSelected 方法
        if (position == 0) onPageSelected(0);
    }


    private void initData() {
        position = getIntent().getIntExtra("position", 0);
        selectMaxNum = getIntent().getIntExtra("maxNum", 8);
        selectData = getIntent().getStringArrayListExtra("selectData");
    }


    private void initView() {
        ArrayList<LocalMedia> data = getIntent().getParcelableArrayListExtra("mediaData");

        preVP = (PreviewViewPager) findViewById(R.id.pre_viewpager);
        preVP.setAdapter(adapter = new PreViewPagerAdapter(data));
        preVP.addOnPageChangeListener(this);

        toolbar = (Toolbar) findViewById(R.id.toolBar);
        toolbar.setTitle(getString(R.string.s_selectNum, position + 1, adapter.getMyCount()));
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.mipmap.title_back);

        selectBar = findViewById(R.id.select_bar_layout);
        statusBarSpace = findViewById(R.id.bar_layout);
        selectCheckBox = (CheckBox) findViewById(R.id.checkbox_select);

        sendView = (TextView) findViewById(R.id.t_toSend);
    }


    private void initListener() {
        adapter.setOnPictureClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchBarVisibility();
            }
        });
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendDataAction(false);
            }
        });
        sendView.setOnClickListener(this);
        selectCheckBox.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.t_toSend:
                sendDataAction(true);
                break;

            case R.id.checkbox_select:
                LocalMedia localMedia = adapter.getData(preVP.getCurrentItem());
                //CheckBox先改变选中状态在触发点击事件
                boolean checked = selectCheckBox.isChecked();
                if (!checked) {
                    selectDataChange(0, localMedia);
                } else if (selectData.size() < selectMaxNum) {
                    selectDataChange(1, localMedia);
                } else {
                    selectCheckBox.setChecked(false);
                    Toast.makeText(getApplicationContext(),
                            getApplicationContext().getString(R.string.message_max_num, selectMaxNum),
                            Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    private void selectDataChange(int changeCode, LocalMedia actionData) {
        String path = actionData.getPath();
        if (changeCode == 0 && selectData.contains(path)) {
            selectData.remove(path);
        } else if (changeCode == 1 && !selectData.contains(path)) {
            selectData.add(path);
        }
        setSendText();
    }

    //isDone 是否完成选中操作
    private void sendDataAction(boolean isDone) {
        Intent resultData = new Intent();
        resultData.putExtra(MConstant.ResultDataKey.PICTURE_SELECT_DATA, selectData);
        resultData.putExtra(MConstant.ResultDataKey.PICTURE_PREVIEW_IS_DONE, isDone);
        setResult(RESULT_OK, resultData);
        finish();
    }


    //展示界面选中的页面
    private void showSelectPager(int selectPosition) {
        toolbar.setTitle(getString(R.string.s_selectNum, selectPosition + 1, adapter.getMyCount()));
        LocalMedia localMedia = adapter.getData(selectPosition);
        boolean isSelected = selectData.contains(localMedia.getPath());
        selectCheckBox.setChecked(isSelected);
        selectDataChange(isSelected ? 1 : 0, localMedia);
    }

    private void setSendText() {
        int selectNum = selectData.size();
        if (selectNum > 0) {
            sendView.setEnabled(true);
            sendView.setText(getString(R.string.s_fsNum, selectNum, selectMaxNum));
        } else {
            sendView.setText(getString(R.string.s_fs));
            sendView.setEnabled(false);
        }
    }

    private void switchBarVisibility() {
        isShowBar = !isShowBar;

        statusBarSpace.setVisibility(isShowBar ? View.VISIBLE : View.GONE);
        selectBar.setVisibility(isShowBar ? View.VISIBLE : View.GONE);

        // Status change
        WindowManager.LayoutParams attrs = getWindow().getAttributes();
        if (isShowBar)
            attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
        else
            attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getWindow().setAttributes(attrs);
    }


    @Override
    public void onBackPressed() {
        sendDataAction(false);
    }

    /**
     * ViewPager 滑动监听
     */
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        //可循环时 position 可以很大
        showSelectPager(position % adapter.getMyCount());
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

}
