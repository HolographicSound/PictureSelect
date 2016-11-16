package com.zero.pictureselect.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by hjf on 2016/11/10 11:30.
 * Used to 媒体文件
 */
public class LocalMedia implements Parcelable {
    private String path;
    private long duration;

    public LocalMedia() {
    }

    public LocalMedia(String path, long duration) {
        this.path = path;
        this.duration = duration;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.path);
        dest.writeLong(this.duration);
    }

    protected LocalMedia(Parcel in) {
        this.path = in.readString();
        this.duration = in.readLong();
    }

    public static final Parcelable.Creator<LocalMedia> CREATOR = new Parcelable.Creator<LocalMedia>() {
        @Override
        public LocalMedia createFromParcel(Parcel source) {
            return new LocalMedia(source);
        }

        @Override
        public LocalMedia[] newArray(int size) {
            return new LocalMedia[size];
        }
    };
}
