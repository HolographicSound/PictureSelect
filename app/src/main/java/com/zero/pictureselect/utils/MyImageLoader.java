package com.zero.pictureselect.utils;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.util.LruCache;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.zero.pictureselect.R;

import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * Created by hjf on 2016/10/10.
 * Used to 自定义图片加载工具
 * <p/>
 * 1. 初始化相关信息
 * --线程池：管理线程加载图片，可设置数量
 * --任务队列：管理图片加载任务，可设置读取方式：LIFO
 * --线程轮询器：轮训消息并交给 轮询器Handler 执行
 * --信号量：监控轮训器初始化是否完成；控制任务添加速度，避免LIFO效果不明显
 * --缓存器：LruCache，缓存加载的图片，设置 应用最大缓存 / 8 ，重写 sizeof()
 * --Handler：轮询器Handler，用于执行队列中的加载任务；图片展示Handler(UI线程)，用于图片的显示设置
 * <p/>
 * 2. 工作流程
 * 初始化加载器 --> 添加图片显示任务（是否已缓存）   --(Y)-> 发送展示消息
 * .                                            --(N)-> 建立 Runnable 加载任务对象并添加到任务列表 --> 加载完成发送展示消息
 * <p/>
 */
public class MyImageLoader {

    /*线程、任务管理*/
    private int threadPollNum = 1;//线程池数量(默认1)
    private ExecutorService mThreadPool;//线程池对象
    private LinkedList<Runnable> mTasks;//任务队列，插入删除效率高
    private DisplayType mDisplayType = DisplayType.LIFO;//默认后进先出
    @DrawableRes
    private int loadFailImage = R.mipmap.loading_failed;


    /*线程轮训工作，使用信号量控制速度*/
    private Thread mThread;//开启轮训进程
    private Handler mThreadHandler;//接受looper消息
    private volatile Semaphore mThreadHandlerSemaphore = new Semaphore(0);//引入值为0的信号量，防止mThreadHandler未初始化完成。拿到信号量的线程可以进入代码
    private volatile Semaphore mPoolSemaphore;//引入值为“1”的信号量，防止任务加入速度过快，LIFO效果不明显


    /*图片展示、内存缓存*/
    private Handler mShowHandler;//UI线程展示图片
    private LruCache<String, Bitmap> mImageCacheSmall;//图片缓存器(压缩 -- path；原图 -- path + "big")


    /*单例对象*/
    private static MyImageLoader myImageLoder;

    public static MyImageLoader getInstance() {
        if (myImageLoder == null) {
            synchronized (MyImageLoader.class) {
                if (myImageLoder == null) {
                    myImageLoder = new MyImageLoader();
                }
            }
        }
        return myImageLoder;
    }

    //只调用一次
    public static void init(int threadPollNum, @NonNull DisplayType mDisplayType) {
        new MyImageLoader(threadPollNum, mDisplayType);
    }

    private MyImageLoader() {
        this(3, DisplayType.LIFO);
    }

    private MyImageLoader(int threadPollNum, @NonNull DisplayType mDisplayType) {
        this.threadPollNum = Math.max(1, threadPollNum);
        this.mDisplayType = mDisplayType;
        _init();
    }


    private void _init() {

        //初始化轮询器
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                //启用当前线程的looper，才能绑定handler
                Looper.prepare();

                //handler 和当前线程 looper 绑定（默认绑定定义处线程的looper）
                mThreadHandler = new Handler(/*Looper.myLooper()*/) {
                    @Override
                    public void handleMessage(Message msg) {
                        //线程池执行任务
                        mThreadPool.execute(_getTask());
                        //速度控制信号量请求
                        try {
                            mPoolSemaphore.acquire();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                };
                //线程轮训器初始化完成，释放信号量
                //添加任务时 mThreadHandler 发送消息，此时需要 mThreadHandlerSemaphore 请求信号量
                mThreadHandlerSemaphore.release();

                //开始轮训
                Looper.loop();
                //Looper.loop()之后的代码不会执行，除非 mThreadHandler.getLooper().quit(); 执行
            }
        });
        mThread.start();

        //Lru缓存大小控制
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        mImageCacheSmall = new LruCache<String, Bitmap>(maxMemory / 8) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                //默认测量item数量，重写计量每个item的大小
                return bitmap.getRowBytes() * bitmap.getHeight();//bitmap.getByteCount()
            }
        };

        //初始化线程池
        mThreadPool = Executors.newFixedThreadPool(threadPollNum);

        //线程池信号量
        mPoolSemaphore = new Semaphore(threadPollNum);

        //初始化任务队列
        mTasks = new LinkedList<>();
    }


    //添加任务队列
    private synchronized void _addTask(Runnable runnable) {
        try {
            //请求信号量,防止 mThreadHandler 为null
            if (mThreadHandler == null)
                mThreadHandlerSemaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        mTasks.add(runnable);
        mThreadHandler.sendEmptyMessage(0x110);
    }


    //获取任务,默认后进先出
    private synchronized Runnable _getTask() {
        if (this.mDisplayType == DisplayType.FIFO) {
            return mTasks.removeFirst();
        } else /*if (this.mDisplayType == DisplayType.LIFO)*/
            return mTasks.removeLast();
    }


    //初始化图片显示的Handler
    private void _initShowHandler() {
        if (mShowHandler != null) return;
        mShowHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                ImageHolder imageHolder = (ImageHolder) msg.obj;
                if (imageHolder.imagePath.equals(imageHolder.imageView.getTag())) {
                    if (imageHolder.bitmap == null) {
                        imageHolder.imageView.setImageResource(loadFailImage);
                    } else {
                        imageHolder.imageView.setImageBitmap(imageHolder.bitmap);
                    }
                }
            }
        };
    }

    //加载图片进入内存
    private void _loadImage(final String imagePath, final ImageView imageView, final boolean isCompress) {
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap1;//手动回收
                Bitmap bitmap2 = null;//null代表此图片加载失败


                if (isCompress) {
                    int[] imageSize = _getImageViewSize(imageView);
                    bitmap1 = BitmapUtil.compressSize(imagePath, imageSize[0], imageSize[1]);//压缩图
                } else {
                    bitmap1 = BitmapUtil.compressSize(imagePath);//原图
                }


                if (bitmap1 != null) {
                    bitmap2 = BitmapUtil.adjustBitmapDegree(imagePath, bitmap1);
                    _putBitmapInCache(imagePath, bitmap2, isCompress);
                }

                _sendShowMessage(imagePath, imageView, bitmap2);
                //加载完成图片释放信号量,方便下一次加载图片
                mPoolSemaphore.release();
            }
        };
        _addTask(runnable);
    }


    //获取ImageView 的 [宽,高]
    private int[] _getImageViewSize(ImageView imageView) {

        DisplayMetrics displayMetrics = imageView.getContext().getResources().getDisplayMetrics();
        ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();

        //布局当前的宽度
        int width = layoutParams.width == ViewGroup.LayoutParams.WRAP_CONTENT ? 0 : layoutParams.width;
        //布满父控件时获取父控件的大小(建议图片直接设置个固定值，上面的方法就能获取到宽高)
        if (width <= 0) width = layoutParams.width == ViewGroup.LayoutParams.MATCH_PARENT ?
                (((View) imageView.getParent()).getLayoutParams().width) : 0;
        //获取布局的宽度参数
        if (width <= 0) width = layoutParams.width;
        //反射获取ImageView最大宽度
        if (width <= 0) width = ReflectiveUtil.getViewFieldValue(imageView, "mMaxWidth");
        //屏幕宽度
        if (width <= 0) width = displayMetrics.widthPixels;


        int height = layoutParams.height == ViewGroup.LayoutParams.WRAP_CONTENT ? 0 : layoutParams.height;
        if (height <= 0) height = layoutParams.height == ViewGroup.LayoutParams.MATCH_PARENT ?
                (((View) imageView.getParent()).getLayoutParams().height) : 0;
        if (height <= 0) height = layoutParams.height;
        if (height <= 0) height = ReflectiveUtil.getViewFieldValue(imageView, "mMaxHeight");
        if (height <= 0) height = displayMetrics.heightPixels;

        return new int[]{width, height};
    }


    //发送该显示图片的消息
    private void _sendShowMessage(String imagePath, ImageView imageView, Bitmap bitmap) {
        ImageHolder imageHolder = new ImageHolder();
        imageHolder.imagePath = imagePath;
        imageHolder.imageView = imageView;
        imageHolder.bitmap = bitmap;
        Message message = Message.obtain();
        message.obj = imageHolder;
        mShowHandler.sendMessage(message);
    }


    //显示本地图片（带缓存） isCompress 是否需要压缩
    private void _display(final String imagePath, final ImageView imageView, boolean isCompress) {

        imageView.setTag(imagePath);

        if (mShowHandler == null) _initShowHandler();

        Bitmap bitmap = getBitmap4Cache(imagePath, isCompress);
        if (bitmap == null) {
            _loadImage(imagePath, imageView, isCompress);
        } else {
            _sendShowMessage(imagePath, imageView, bitmap);
        }
    }


    //放图片进入缓存
    private void _putBitmapInCache(String key, Bitmap value, boolean isCompress) {
        if (mImageCacheSmall.get(getCacheKey(key, isCompress)) != null) return;
        mImageCacheSmall.put(getCacheKey(key, isCompress), value);
    }

    //从缓存冲拿去图片
    private Bitmap getBitmap4Cache(String imagePath, boolean isCompress) {
        return mImageCacheSmall.get(getCacheKey(imagePath, isCompress));
    }

    //获取缓存的key值 (原图：path + "big")
    private String getCacheKey(String imagePath, boolean isCompress) {
        return isCompress ? imagePath : imagePath + "big";
    }


    //先进先出，后进先出
    public enum DisplayType {
        FIFO, LIFO
    }

    private static class ImageHolder {
        String imagePath;
        ImageView imageView;
        Bitmap bitmap;
    }


    //小图
    public static void displays(String imagePath, ImageView imageView) {
        getInstance()._display(imagePath, imageView, true);
    }

    //原图
    public static void display(String imagePath, ImageView imageView) {
        getInstance()._display(imagePath, imageView, false);
    }
}
