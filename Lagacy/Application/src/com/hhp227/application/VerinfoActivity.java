package com.hhp227.application;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import com.hhp227.application.ui.navigationdrawer.DrawerArrowDrawable;

public class VerinfoActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verinfo);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setHomeAsUpIndicator(new DrawerArrowDrawable(this) {
            @Override
            public boolean isLayoutRtl() {
                return false;
            }
        });
    }

    /**
     * 액션바에 뒤로가기 버튼을 누르면 나타나는 동작
     */
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);// 인텐트를 반환값으로 돌려준다
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}