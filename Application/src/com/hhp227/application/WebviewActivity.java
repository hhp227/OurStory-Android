package com.hhp227.application;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class WebviewActivity extends Activity {
	public static String URL = "url";
	WebView webview;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_webview);
		webview = (WebView) findViewById(R.id.webView);
		
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setDisplayShowHomeEnabled(false);
		
		Intent intent = getIntent();
		URL = intent.getStringExtra(URL);
		webview.loadUrl(URL);
		webview.getSettings().setLoadWithOverviewMode(true);
		webview.getSettings().setUseWideViewPort(true);
		WebSettings webSettings = webview.getSettings();
        // 모바일에서 자바스크립트를 실행 시키기 위한 용도
        webSettings.setJavaScriptEnabled(true);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.webview, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case android.R.id.home :
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
