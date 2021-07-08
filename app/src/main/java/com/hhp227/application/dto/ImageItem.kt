package com.hhp227.application.dto;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class ImageItem implements Parcelable {
    public int id;
    public String image;
    public String tag;
    public Bitmap bitmap;

    public ImageItem() {
    }

    public ImageItem(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public ImageItem(int id, String image, String tag) {
        this.id = id;
        this.image = image;
        this.tag = tag;
    }

    protected ImageItem(Parcel in) {
        id = in.readInt();
        image = in.readString();
        tag = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(image);
        dest.writeString(tag);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ImageItem> CREATOR = new Creator<ImageItem>() {
        @Override
        public ImageItem createFromParcel(Parcel in) {
            return new ImageItem(in);
        }

        @Override
        public ImageItem[] newArray(int size) {
            return new ImageItem[size];
        }
    };
}
