package com.zero.pictureselect.adapter;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;


import com.zero.pictureselect.R;
import com.zero.pictureselect.model.LocalMedia;

import java.util.ArrayList;

/**
 * Created by hjf on 2016/10/10.
 * Used to 图片展示适配器
 */
public class PicAdapter extends CommonRecyclerAdapter<LocalMedia> {

    public int maxSelectNum;
    private int actionCode;
    private Context mContext;
    private boolean showCamera = true;
    private ArrayList<String> selectMedias = new ArrayList<>();
    private OnPictureAction onPictureAction;


    public PicAdapter(Context context, int actionCode, int selectMaxNum) {
        super(context);
        this.mContext = context;
        this.actionCode = actionCode;
        this.maxSelectNum = selectMaxNum;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (showCamera && position == 0)
            onBindView((ViewHolder) holder, null, position);
        else if (showCamera)
            onBindView((ViewHolder) holder, getItem(position - 1), position);
        else
            onBindView((ViewHolder) holder, getItem(position), position);
    }

    @Override
    public void onBindView(ViewHolder mViewHolder, final LocalMedia mediaFolder, final int position) {
        mViewHolder.convertToSquare(mContext);
        mViewHolder.getConvertView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onPictureAction == null) return;
                if (showCamera && position == 0)
                    onPictureAction.onTakePhoto();
                else if (showCamera)
                    onPictureAction.onPictureClick(position - 1);
                else
                    onPictureAction.onPictureClick(position);
            }
        });
        if (showCamera && position == 0) {
            return;
        }
        boolean isSelected = selectMedias.contains(mediaFolder.getPath());
        //展示图片
        mViewHolder.setImage(R.id.iv_picture, mediaFolder.getPath());
        ImageView pictureView = mViewHolder.getView(R.id.iv_picture);
        pictureView.setColorFilter(getColor(isSelected ? R.color.c_80_0 : R.color.c_20_0), PorterDuff.Mode.SRC_ATOP);
        //选择View
        ImageView selectView = mViewHolder.getView(R.id.iv_selectState);
        if (actionCode == 0) {
            selectView.setVisibility(View.GONE);
        } else {
            selectView.setSelected(isSelected);
            selectView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkSelectState(mediaFolder);
                }
            });
        }
    }

    //选中状态变化
    private void checkSelectState(LocalMedia mediaFolder) {
        boolean selected = selectMedias.contains(mediaFolder.getPath());
        if (!selected && selectMedias.size() >= maxSelectNum) {
            Toast.makeText(context, context.getString(R.string.message_max_num, maxSelectNum), Toast.LENGTH_LONG).show();
            return;
        }
        myNotify(mediaFolder);
        if (selected) {
            selectMedias.remove(mediaFolder.getPath());
        } else {
            selectMedias.add(mediaFolder.getPath());
        }
        if (onPictureAction != null)
            onPictureAction.onPictureSelect(selectMedias.size());
    }

    @Override
    public int onBindViewResource(int position) {
        return (showCamera && position == 0) ? R.layout.item_camera : R.layout.item_pic;
    }

    // 外部 不要使用 setData 赋予数据
    public void bindImage(ArrayList<LocalMedia> medias) {
        setData(medias);
    }

    @Override
    public int getItemCount() {
        int itemCount = super.getItemCount();
        return showCamera ? itemCount + 1 : itemCount;
    }

    public void setSelectMedias(ArrayList<String> selectMedias) {
        this.selectMedias.clear();
        this.selectMedias.addAll(selectMedias);
        this.notifyDataSetChanged();
        if (onPictureAction != null)
            onPictureAction.onPictureSelect(selectMedias.size());
    }

    @ColorInt
    private int getColor(@ColorRes int colorRes) {
        return ContextCompat.getColor(mContext, colorRes);
    }

    private void myNotify(LocalMedia mediaFolder) {
        int index = getDatas().indexOf(mediaFolder);
        if (index == -1) return;
        notifyItemChanged(showCamera ? index + 1 : index);
    }

    public void setOnPictureActionListener(OnPictureAction onPictureActionListener) {
        this.onPictureAction = onPictureActionListener;
    }

    public ArrayList<String> getSelectMediasPath() {
        return selectMedias;
    }

    public interface OnPictureAction {
        void onPictureSelect(int selectNum);

        void onTakePhoto();

        void onPictureClick(int position);
    }
}
