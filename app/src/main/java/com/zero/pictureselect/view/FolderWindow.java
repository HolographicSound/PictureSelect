package com.zero.pictureselect.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.PopupWindow;

import com.zero.pictureselect.R;
import com.zero.pictureselect.adapter.CommonRecyclerAdapter;
import com.zero.pictureselect.model.LocalMediaFolder;
import com.zero.pictureselect.utils.ScreenUtils;

import java.util.ArrayList;

/**
 * Created by hjf on 2016/11/10 17:53.
 * Used to 选择文件夹弹出框
 */
public class FolderWindow extends PopupWindow {

    private Context mContext;
    private View container;
    private RecyclerView recyclerView;
    private FolderListAdapter adapter;
    private int checkId = 0;
    private OnFolderSelectedListener onFolderSelectedListener;


    public static FolderWindow newInstance(Context context) {
        View container = LayoutInflater.from(context).inflate(R.layout.v_folder_selectui, null);
        int height = ScreenUtils.getScreenHeight(context) - ScreenUtils.dip2px(context, 96);
        return new FolderWindow(context, container, WindowManager.LayoutParams.MATCH_PARENT, height, true);
    }

    private FolderWindow(Context context, View container, int width, int height, boolean focusable) {
        super(container, width, height, focusable);
        this.mContext = context;
        this.container = container;
        this.setAnimationStyle(R.style.PopupWindowAnimal);
        this.setOutsideTouchable(true);
        this.update();
        this.setBackgroundDrawable(new ColorDrawable(Color.argb(153, 0, 0, 0)));
        initRecyclerView();
    }


    private void initRecyclerView() {
        recyclerView = (RecyclerView) container.findViewById(R.id.folder_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(adapter = new FolderListAdapter(mContext));
    }

    public void bindFolders(ArrayList<LocalMediaFolder> allImageFolders) {
        adapter.setData(allImageFolders);
    }

    @Override
    public void showAsDropDown(View anchor) {
        super.showAsDropDown(anchor);
        Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.up_in);
        recyclerView.startAnimation(animation);
    }


    private boolean isDismiss = false;

    @Override
    public void dismiss() {
        if (isDismiss) {
            return;
        }
        isDismiss = true;
        Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.down_out);
        recyclerView.startAnimation(animation);
        dismiss();
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                isDismiss = false;
                FolderWindow.super.dismiss();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    public void setOnFolderSelectedListener(OnFolderSelectedListener onFolderSelectedListener) {
        this.onFolderSelectedListener = onFolderSelectedListener;
    }

    public interface OnFolderSelectedListener {
        void onFolderSelectedListener(LocalMediaFolder data);
    }


    //会持有对当前类对象的强应用，最好使用 静态内部类
    private class FolderListAdapter extends CommonRecyclerAdapter<LocalMediaFolder> {
        public FolderListAdapter(Context context) {
            super(context);
        }

        @Override
        public void onBindView(ViewHolder mViewHolder, final LocalMediaFolder data, final int position) {
            mViewHolder.setImage(R.id.first_image, data.getFirstImagePath());
            mViewHolder.setText(R.id.folder_name, data.getName());
            mViewHolder.setText(R.id.image_num, mContext.getString(R.string.picNum, data.getCount()));
            mViewHolder.getView(R.id.selectStateView).setVisibility(checkId == position ? View.VISIBLE : View.GONE);
            mViewHolder.getConvertView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onFolderSelectedListener == null) return;
                    checkId = position;
                    notifyDataSetChanged();
                    onFolderSelectedListener.onFolderSelectedListener(data);
                    dismiss();
                }
            });
        }

        @Override
        public int onBindViewResource(int position) {
            return R.layout.item_folder;
        }
    }
}
