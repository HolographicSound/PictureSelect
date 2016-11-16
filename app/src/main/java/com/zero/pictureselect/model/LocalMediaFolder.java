package com.zero.pictureselect.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.ArrayList;

/**
 * Created by hjf on 2016/10/10.
 * Used to 媒体文件夹实体类
 */
public class LocalMediaFolder implements Parcelable {

    private String name;
    private String dir;
    private ArrayList<LocalMedia> medias;
    private String firstImagePath;
    private int count;

    public LocalMediaFolder() {
        medias = new ArrayList<>();
    }

    public LocalMediaFolder(@NonNull ArrayList<LocalMedia> medias) {
        this.medias = medias;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public ArrayList<LocalMedia> getMedias() {
        return medias;
    }

    public void addMedia(String path) {
        medias.add(new LocalMedia(path, -1));
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getFirstImagePath() {
        return firstImagePath;
    }

    public void setFirstImagePath(String firstImagePath) {
        this.firstImagePath = firstImagePath;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.dir);
        dest.writeTypedList(this.medias);
    }


    protected LocalMediaFolder(Parcel in) {
        this.name = in.readString();
        this.dir = in.readString();
        this.medias = in.createTypedArrayList(LocalMedia.CREATOR);
    }

    public static final Parcelable.Creator<LocalMediaFolder> CREATOR = new Parcelable.Creator<LocalMediaFolder>() {
        @Override
        public LocalMediaFolder createFromParcel(Parcel source) {
            return new LocalMediaFolder(source);
        }

        @Override
        public LocalMediaFolder[] newArray(int size) {
            return new LocalMediaFolder[size];
        }
    };
}
