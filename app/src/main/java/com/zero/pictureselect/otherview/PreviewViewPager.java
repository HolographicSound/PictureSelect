package com.zero.pictureselect.otherview;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class PreviewViewPager extends ViewPager {

    private boolean isLocked;

    public PreviewViewPager(Context context) {
        super(context);
        isLocked = false;
    }

    public PreviewViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        isLocked = false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!isLocked) {
            try {
                return super.onInterceptTouchEvent(ev);
            } catch (IllegalArgumentException e) {
                //不理会
                return false;
            } catch (ArrayIndexOutOfBoundsException e) {
                //不理会
                return false;
            }
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isLocked) {
            return super.onTouchEvent(event);
        }
        return false;
    }

    public void toggleLock() {
        isLocked = !isLocked;
    }

    public void setLocked(boolean isLocked) {
        this.isLocked = isLocked;
    }

    public boolean isLocked() {
        return isLocked;
    }
}
