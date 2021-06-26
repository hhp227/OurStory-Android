package com.hhp227.application;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;

public class SplashActivity extends Activity {

	// 뜨는 시간
	private static int SPLASH_TIME_OUT = 1250;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// 액션바 안보이기
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);

		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				// 화면이 닫히고 전환됨
				Intent intent = new Intent(SplashActivity.this, MainActivity.class);
				startActivity(intent);

				finish();
			}
		}, SPLASH_TIME_OUT);
	}

}