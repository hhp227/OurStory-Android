package com.hhp227.application.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import java.io.FileNotFoundException;
import java.io.IOException;

public class BitmapUtil {
    private final Context mContext;

    public BitmapUtil(Context context) {
        this.mContext = context;
    }

    public Bitmap bitmapResize(Uri uri, int resize) {
        Bitmap result = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        try {
            BitmapFactory.decodeStream(mContext.getContentResolver().openInputStream(uri), null, options);

            int width = options.outWidth;
            int height = options.outHeight;
            int sampleSize = 1;

            while (width / 2 >= resize && height / 2 >= resize) {
                width /= 2;
                height /= 2;
                sampleSize *= 2;
            }
            options.inSampleSize = sampleSize;
            result = BitmapFactory.decodeStream(mContext.getContentResolver().openInputStream(uri), null, options);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return result;
    }

    public Bitmap uriToBitmap(Uri uri) {
        try {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && uri != null ?
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(mContext.getContentResolver(), uri))
                    :
                    MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), uri);
        } catch (IOException e) {
            return null;
        }
    }

    public Bitmap rotateImage(Bitmap bitmap, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }
}