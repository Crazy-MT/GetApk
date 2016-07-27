package com.maotong.getapk;

import android.content.ClipData;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;
import java.util.Comparator;


public class AppInfo  implements  Parcelable {
    private String appName;
    private String appSize;
    private String appTime;
    private File apkFile;
    private Drawable appIcon;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppSize() {
        return appSize;
    }

    public void setAppSize(String appSize) {
        this.appSize = appSize;
    }

    public String getAppTime() {
        return appTime;
    }

    public void setAppTime(String appTime) {
        this.appTime = appTime;
    }

    public Drawable getAppIcon() {
        return appIcon;
    }

    public void setAppIcon(Drawable appIcon) {
        this.appIcon = appIcon;
    }

    public File getApkFile() {
        return apkFile;
    }

    public void setApkFile(File apkFile) {
        this.apkFile = apkFile;
    }

    @Override
    public String toString() {
        return "AppInfo{" +
                "appName='" + appName + '\'' +
                ", appSize='" + appSize + '\'' +
                ", appTime='" + appTime + '\'' +
                ", appIcon='" + appIcon + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }

}
