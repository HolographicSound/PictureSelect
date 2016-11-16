/**
 * @(#) CommonAdapter.java 2016/1/15
 * CopyRight 2015 All rights reserved
 * @modify
 */
package com.zero.pictureselect.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.zero.pictureselect.utils.MyImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hjf on 2016/10/10.
 * Used to 用于生成通用适配器(适用于RecyclerView)
 */
public abstract class CommonRecyclerAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    protected Context context;
    protected ArrayList<T> mDataSource;


    public CommonRecyclerAdapter(Context context) {
        this.context = context;
        this.mDataSource = new ArrayList<>();
    }

    public void setData(List<T> data) {
        //设置为null时代表设置空数组操作
        if (data == null) data = new ArrayList<>();
        this.mDataSource.clear();
        this.mDataSource.addAll(data);
        this.notifyDataSetChanged();
    }

    public void clearData() {
        this.mDataSource.clear();
        this.notifyDataSetChanged();
    }

    public void addDatas(List<T> datas) {
        //需在AdapterDataObserver中的onItemRangeInserted方法监听数据的添加量
        if (datas == null) datas = new ArrayList<>();
        this.mDataSource.addAll(datas);
        this.notifyItemRangeInserted(this.mDataSource.size() - datas.size(), datas.size());
    }

    public void addData(T data) {
        if (data == null) return;
        this.mDataSource.add(data);
        this.notifyItemInserted(this.mDataSource.size());
    }

    public void addData(T data, int index) {
        if (data == null) return;
        this.mDataSource.add(index, data);
        this.notifyItemInserted(index);
    }

    public T getData(int index) {
        return this.mDataSource.get(index);
    }


    public void removeData(T data) {
        int index = this.mDataSource.indexOf(data);
        this.removeData(index);
    }

    public void removeData(int index) {
        if (index == -1) return;
        this.mDataSource.remove(index);
        this.notifyItemRemoved(index);
    }

    public ArrayList<T> getDatas() {
        return mDataSource;
    }


    @Override
    public int getItemViewType(int position) {
        return onBindViewResource(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(viewType, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        onBindView((ViewHolder) holder, getItem(position), position);
    }

    @Override
    public int getItemCount() {
        return mDataSource != null ? mDataSource.size() : 0;
    }

    protected T getItem(int position) {
        if (mDataSource == null) return null;
        if (position >= mDataSource.size()) return null;
        return mDataSource.get(position);
    }

    public abstract void onBindView(ViewHolder mViewHolder, T data, int position);

    public abstract int onBindViewResource(int position);


    protected class ViewHolder extends RecyclerView.ViewHolder {
        private SparseArray<View> viewCache;
        private View itemView;

        public ViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            this.viewCache = new SparseArray<>();
        }

        public <T extends View> T getView(int viewId) {
            View view = viewCache.get(viewId);
            if (view == null) {
                view = this.itemView.findViewById(viewId);
                viewCache.put(viewId, view);
            }
            return (T) view;
        }

        public View getConvertView() {
            return this.itemView;
        }

        //TextView 设置方法
        public TextView setText(int textViewResId, String textString) {
            TextView textView = getView(textViewResId);
            textView.setText(textString);
            return textView;
        }

        //ImageView 设置方法
        public ImageView setImage(int imageViewResId, String imagePath) {

            ImageView imageView = getView(imageViewResId);

            //获取图片 Bitmap 并压缩显示
//            imageView.setImageBitmap(BitmapUtil.compressSize(imagePath));

            //使用自定义的 MyImageLoader 加载图片
            MyImageLoader.displays(imagePath, imageView);

            //采用 Glide框架 加载图片
//            GlideUtil.load4Path(imagePath, imageView);

            return imageView;
        }
    }
}
