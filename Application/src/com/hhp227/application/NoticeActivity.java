package com.hhp227.application;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;

public class NoticeActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice);

        ActionBar actionBar = getActionBar();
        // 액션바 뒤로가기
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);
    }
}
