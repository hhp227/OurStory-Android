package com.hhp227.application;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.hhp227.application.fragment.MyPostFragment;
import com.hhp227.application.fragment.MyInfoFragment;
import com.hhp227.application.ui.tabhostviewpager.FakeContent;

import java.util.List;
import java.util.Vector;

public class MyInfoActivity extends FragmentActivity {
    private ViewPager viewPager;
    private TabsPagerAdapter mAdapter;
    private android.widget.TabHost TabHost;
    private String[] Tabnames = {"내 정보", "내가 쓴 글"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_tabs);

        TabHost = findViewById(android.R.id.tabhost);

        TabHost.setup();

        for (int i = 0; i < Tabnames.length; i++) {
            android.widget.TabHost.TabSpec tabSpec;
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

        viewPager = findViewById(R.id.viewPager);

        List<Fragment> fragments = new Vector<Fragment>();
        fragments.add(MyInfoFragment.Companion.newInstance());
        fragments.add(MyPostFragment.Companion.newInstance());
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