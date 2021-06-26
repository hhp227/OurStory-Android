package com.hhp227.application.write;

import android.graphics.Bitmap;
import android.net.Uri;

public class Writeitem {
    Uri fileUri;
    Bitmap bitmap;
    String image;

    public Writeitem() {
    }

    public Writeitem(Uri fileUri, Bitmap bitmap, String image) {
        super();
        this.fileUri = fileUri;
        this.bitmap = bitmap;
        this.image = image;
    }

    public Uri getFileUri() {
        return fileUri;
    }

    public void setFileUri(Uri fileUri) {
        this.fileUri = fileUri;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
