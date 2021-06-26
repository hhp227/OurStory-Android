package com.hhp227.application.ui.tabhostviewpager;

import android.content.Context;
import android.view.View;
import android.widget.TabHost.TabContentFactory;

public class FakeContent implements TabContentFactory {
	Context context;
	
	public FakeContent (Context mcontext) {
		context = mcontext;
	}

	@Override
	public View createTabContent(String tag) {
		View view = new View(context);
		view.setMinimumWidth(0);
		view.setMinimumHeight(0);
		return view;
	} 
}