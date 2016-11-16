package com.zero.pictureselect.utils;

import android.widget.ImageView;

import java.lang.reflect.Field;

/**
 * Created by hjf on 2016/10/10.
 * Used to 反射工具类
 */
public class ReflectiveUtil {

    //通过反射获取最大宽高属性
    public static int getViewFieldValue(Object object, String fieldKey) {
        int fieldValue = 0;
        try {
            Field field = ImageView.class.getDeclaredField(fieldKey);
            field.setAccessible(true);
            int value = (Integer) field.get(object);
            if (value > 0 && value < Integer.MAX_VALUE)
                fieldValue = value;
        } catch (Exception e) {
        }

        return fieldValue;
    }
}
