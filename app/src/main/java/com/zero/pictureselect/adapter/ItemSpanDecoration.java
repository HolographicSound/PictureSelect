/*
 * @(#) ItemSpanDecoration.java 2015/11/6.
 * CopyRight 2015 TaoYuanTn All Rights Reserved
 * @modify
 */
package com.zero.pictureselect.adapter;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/*
 * @author avans 2015/11/6.
 * @version 1.0
 * @desc RecycleView item decoration span decoration
 * @since JDK1.6+ SDK14+
 */
public class ItemSpanDecoration extends RecyclerView.ItemDecoration {

    private int left, right, top, bottom;

    //Set span for left,right,top and bottom
    public ItemSpanDecoration(int left, int top, int right, int bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;

    }

    //Set the same span for left,right,top and bottom
    public ItemSpanDecoration(int span) {
        this(span, span, span, span);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.set(this.left, this.top, this.right, this.bottom);
    }
}
