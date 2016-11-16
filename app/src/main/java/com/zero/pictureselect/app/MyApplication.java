package com.zero.pictureselect.app;

import android.app.Application;
import android.content.Context;

import com.zero.pictureselect.utils.MyImageLoader;

/**
 * Created by hjf on 2016/10/10.
 * Used to
 */
public class MyApplication extends Application {

    private static Context myContext;

    public static Context getMyContext() {
        return myContext;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        myContext = getApplicationContext();
        MyImageLoader.init(3, MyImageLoader.DisplayType.LIFO);
    }
}
