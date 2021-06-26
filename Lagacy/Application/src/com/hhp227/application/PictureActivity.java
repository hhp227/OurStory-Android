package com.hhp227.application;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.Window;

import com.android.volley.toolbox.ImageLoader;
import com.hhp227.application.app.AppController;
import com.hhp227.application.app.URLs;
import com.hhp227.application.helper.ZoomImageView;
import com.hhp227.application.ui.navigationdrawer.DrawerArrowDrawable;

public class PictureActivity extends Activity {
    public static final int TYPEFEED = 1, TYPEALBUM = 2;
    public static final String IMAGE_URL = "image_url", TYPE = "type";
    private static final String TAG = "포토뷰";
    private ZoomImageView mPicture;
    ImageLoader imageLoader = AppController.getInstance().getImageLoader();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 상단 타이틀바를 투명하게
        this.requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        setContentView(R.layout.activity_picture);
        ActionBar actionBar = getActionBar();
        // 뒤로가기버튼
        actionBar.setDisplayHomeAsUpEnabled(true);
        // 앱 아이콘 숨기기
        actionBar.setDisplayShowHomeEnabled(false);
        // 액션바 투명
        actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.transparent));
        // 액션바 화살표
        actionBar.setHomeAsUpIndicator(new DrawerArrowDrawable(this) {
            @Override
            public boolean isLayoutRtl() {
                return false;
            }
        });

        mPicture = (ZoomImageView) findViewById(R.id.image);
        mPicture.setAdjustViewBounds(false);

        int type = 0;
        String imageUrl = null;
        Bundle b = getIntent().getExtras();
        if (b != null) {
            type = b.getInt(TYPE);
            imageUrl = b.getString(IMAGE_URL);
        }

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int mScreenWidth = metrics.widthPixels;
        int mScreenHeight = metrics.heightPixels;

        try {
            imageLoader.get(type == TYPEFEED ? URLs.URL_FEED_IMAGE + imageUrl : imageUrl,
                    ImageLoader.getImageListener(mPicture,
                            R.drawable.bg_no_image, // default image resId
                            R.drawable.bg_no_image), // error image resId
                    mScreenWidth, mScreenHeight - 50);
        } catch (Exception e) {
            Log.e(TAG, "에러" + e);
            mPicture.setImageResource(R.drawable.bg_no_image);
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
