package com.hhp227.application;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.MenuItem;
import com.hhp227.application.fragments.MyFeedFragment;
import com.hhp227.application.fragments.MyinfoFragment;
import com.hhp227.application.ui.navigationdrawer.DrawerArrowDrawable;
import com.hhp227.application.ui.tabhostviewpager.FakeContent;
import java.util.List;
import java.util.Vector;

public class MyinfoActivity extends FragmentActivity {
    private ViewPager viewPager;
    private TabsPagerAdapter mAdapter;
    private android.widget.TabHost TabHost;
    private String[] Tabnames = {"내 정보", "내가 쓴 글"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_tabs);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setHomeAsUpIndicator(new DrawerArrowDrawable(this) {
            @Override
            public boolean isLayoutRtl() {
                return false;
            }
        });

        TabHost = (android.widget.TabHost) findViewById(android.R.id.tabhost);

        TabHost.setup();

        for (int i = 0; i < Tabnames.length; i++) {
            android.widget.TabHost.TabSpec tabSpec;
            tabSpec = TabHost.newTabSpec(Tabnames[i]);
            tabSpec.setIndicator(Tabnames[i]);
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
        viewPager.addOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int selectedItem) {
				TabHost.setCurrentTab(selectedItem);
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub
				
			}
		});

    }
    /*
     * 어탭터는 귀찮아서 따로 만들지 않음
     */
    public class TabsPagerAdapter extends FragmentPagerAdapter {
        private List<Fragment> fragments;

        public TabsPagerAdapter(FragmentManager fm, List<Fragment> fragments) {
            super(fm);
            this.fragments = fragments;
        }

        @Override
        public Fragment getItem(int position) {
            Fragment getItem = fragments.get(position);

            return getItem;
        }

        @Override
        public int getCount() {

            return fragments.size();
        }

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