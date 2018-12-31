package com.hhp227.application;

import java.util.List;
import java.util.Vector;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;
import android.widget.TabHost;

import com.hhp227.application.fragments.MyFeedFragment;
import com.hhp227.application.fragments.MyinfoFragment;
import com.hhp227.application.ui.tabhostviewpager.FakeContent;
import com.hhp227.application.ui.tabhostviewpager.TabsPagerAdapter;

public class MyinfoActivity extends FragmentActivity {
    private ViewPager viewPager;
    private TabsPagerAdapter mAdapter;
    private TabHost TabHost;
    private String[] Tabnames = {"내 정보", "내가 쓴 글"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_tabs);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);

        TabHost = (TabHost) findViewById(android.R.id.tabhost);

        TabHost.setup();

        for(int i = 0; i < Tabnames.length; i++) {
            TabHost.TabSpec tabSpec;
            tabSpec = TabHost.newTabSpec(Tabnames [i]);
            tabSpec.setIndicator(Tabnames [i]);
            tabSpec.setContent(new FakeContent(this));
            TabHost.addTab(tabSpec);
        }

        TabHost.setOnTabChangedListener(new android.widget.TabHost.OnTabChangeListener() {

            // 탭호스트 리스너
            @Override
            public void onTabChanged(String tabId) {
                int selectedItem = TabHost.getCurrentTab();
                viewPager.setCurrentItem(selectedItem);
            }

        });

        viewPager = (ViewPager) findViewById(R.id.viewPager);

        List<Fragment> fragments = new Vector<Fragment>();
        fragments.add(new MyinfoFragment());
        fragments.add(new MyFeedFragment());
        mAdapter = new TabsPagerAdapter(getSupportFragmentManager(), fragments);
        viewPager.setAdapter(mAdapter);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            // 뷰페이져 리스너
            @Override
            public void onPageScrollStateChanged(int arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onPageSelected(int selectedItem) {
                TabHost.setCurrentTab(selectedItem);
            }

        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}