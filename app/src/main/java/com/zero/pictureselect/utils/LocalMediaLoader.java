package com.zero.pictureselect.utils;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;


import com.zero.pictureselect.model.LocalMedia;
import com.zero.pictureselect.model.LocalMediaFolder;
import com.zero.pictureselect.model.MConstant;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

/**
 * Created by hjf on 2016/11/10 11:19.
 * Used to 本地媒体加载
 */
public class LocalMediaLoader {


    private final static String[] IMAGE_PROJECTION = {
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_MODIFIED,//修改时间 ; DATE_ADDED添加时间
            MediaStore.Images.Media._ID};

    private final static String[] VIDEO_PROJECTION = {
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DATE_MODIFIED,
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DURATION};


    public static void startLoader(final Activity a, final int mediaType, @NonNull final LocalMediaLoadListener listener) {
        a.getLoaderManager().initLoader(mediaType, null, new LoaderManager.LoaderCallbacks<Cursor>() {
                    @Override
                    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                        CursorLoader cursorLoader;
                        if (mediaType == MConstant.MEDIA_TYPE_VIDEO) {
                            // TODO: 2016/11/10 没做Video扫描
                            cursorLoader = new CursorLoader(
                                    a.getApplicationContext(),
                                    //Uri：代表要查询的数据库名称加上表的名称
                                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                                    //projection：需要返回的内容。null表示所有内容都返回
                                    VIDEO_PROJECTION,
                                    //selection：设置筛选条件。null表示不进行筛选
                                    null,
                                    //selectionArgs：替代第三个参数中的 “?”
                                    null,
                                    //sortOrder：排序依据。ASC (默认升序)，DESC（降序）
                                    VIDEO_PROJECTION[2] + " DESC"
                            );
                        } else/* if (mediaType == MConstant.MEDIA_TYPE_IMAGE) */ {//默认图片
                            cursorLoader = new CursorLoader(
                                    a.getApplicationContext(),
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,//查询表名
                                    IMAGE_PROJECTION,//需要返回数据
                                    MediaStore.Images.Media.MIME_TYPE + "=? or " + MediaStore.Images.Media.MIME_TYPE + "=?",//筛选条件
                                    new String[]{"image/jpeg", "image/png"},//替代第三个参数中的 “?”
                                    IMAGE_PROJECTION[2] + " DESC" //拍照时间降序
                            );
                        }
                        return cursorLoader;
                    }

                    @Override
                    public void onLoadFinished(Loader<Cursor> loader, Cursor mCursor) {
                        if (mCursor == null) {
                            listener.loadComplete(null);
                            return;
                        }

                        //数据记录
                        ArrayList<String> parentDirPaths = new ArrayList<>();
                        ArrayList<LocalMedia> allMedia = new ArrayList<>();

                        //全部文件夹
                        ArrayList<LocalMediaFolder> mediaFolders = new ArrayList<>();
                        LocalMediaFolder allMediaFolder = new LocalMediaFolder(allMedia);
                        allMediaFolder.setName("全部");
                        mediaFolders.add(allMediaFolder);

                        while (mCursor.moveToNext()) {
                            //当前扫描图片
                            String filePath = mCursor.getString(mCursor.getColumnIndex(IMAGE_PROJECTION[0]));

                            // 只要是隐藏文件就不管,排除 ..png 等命名文件
                            if (filePath.contains("/.") && !filePath.contains("/.."))
                                continue;
                            allMedia.add(new LocalMedia(filePath, -1));

                            //当前扫描文件夹
                            File parentFile = new File(filePath).getParentFile();
                            if (parentFile == null) continue;
                            String parentPath = parentFile.getAbsolutePath();

                            //重复扫描同一文件夹判断
                            if (parentDirPaths.contains(parentPath)) continue;
                            parentDirPaths.add(parentPath);

                            //有些图片比较诡异~~ 排除他
                            if (parentFile.list() == null) continue;
                            //新文件夹流程 (可用  返回 file)
                            File[] files = parentFile.listFiles(new FilenameFilter() {
                                @Override
                                public boolean accept(File dir, String filename) {
                                    return filename.endsWith(".jpg")
                                            || filename.endsWith(".png")
                                            || filename.endsWith(".jpeg");
                                }
                            });

                            //筛选后没符合图片的可能性
                            if (files.length == 0) continue;

                            LocalMediaFolder mediaFolder = new LocalMediaFolder();
                            for (File tempFile : files) {
                                mediaFolder.addMedia(tempFile.getAbsolutePath());
                            }
                            mediaFolder.setName(parentFile.getName());
                            mediaFolder.setDir(parentFile.getAbsolutePath());
                            mediaFolder.setCount(mediaFolder.getMedias().size());
                            mediaFolder.setFirstImagePath(mediaFolder.getMedias().get(0).getPath());
                            mediaFolders.add(mediaFolder);
                        }

                        //全部文件夹不全
                        allMediaFolder.setFirstImagePath(allMediaFolder.getMedias().get(0).getPath());
                        allMediaFolder.setCount(allMediaFolder.getMedias().size());

                        //扫描完毕
                        mCursor.close();
                        listener.loadComplete(mediaFolders);
                    }

                    @Override
                    public void onLoaderReset(Loader<Cursor> loader) {
                        //销毁loader时调用
                    }
                }

        );
    }

    public interface LocalMediaLoadListener {
        void loadComplete(ArrayList<LocalMediaFolder> data);
    }
}
