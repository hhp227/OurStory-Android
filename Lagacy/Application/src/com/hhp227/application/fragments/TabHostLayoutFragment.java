package com.hhp227.application.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.TextView;

import com.hhp227.application.R;
import com.hhp227.application.scrollable.BaseFragment;
import com.hhp227.application.ui.scrollable.CanScrollVerticallyDelegate;
import com.hhp227.application.ui.scrollable.ScrollableLayout;
import com.hhp227.application.ui.tabhostviewpager.FakeContent;

import java.util.List;
import java.util.Vector;

public class TabHostLayoutFragment extends Fragment {
    private ViewPager viewPager;
    private TabsPagerAdapter mAdapter;
    private android.widget.TabHost TabHost;
    private String[] Tabnames = {"소식", "앨범", "일정", "맴버", "설정"};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tab_host_layout, container, false);
        TabHost = (android.widget.TabHost) rootView.findViewById(android.R.id.tabhost);
        ScrollableLayout scrollableLayout = (ScrollableLayout) rootView.findViewById(R.id.scrollable_layout);
        viewPager = (ViewPager) rootView.findViewById(R.id.viewPager);
        viewPager.setOffscreenPageLimit(Tabnames.length);
        final View header = rootView.findViewById(R.id.header);
        /**
         *  중요: 반드시 child 프래그먼트메니져 사용.
         */
        TabHost.setup();

        for (int i = 0; i < Tabnames.length; i++) {
            TabHost.TabSpec tabSpec;
            tabSpec = TabHost.newTabSpec(Tabnames[i]);
            tabSpec.setIndicator(Tabnames[i]);
            tabSpec.setContent(new FakeContent(getActivity()));
            TabHost.addTab(tabSpec);
            TextView textView = (TextView) TabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
            textView.setTextColor(Color.parseColor("#FFFFFF"));
        }
        TabHost.setOnTabChangedListener(new android.widget.TabHost.OnTabChangeListener() {
            // 탭호스트 리스너
            @Override
            public void onTabChanged(String tabId) {
                int selectedItem = TabHost.getCurrentTab();
                viewPager.setCurrentItem(selectedItem);
            }
        });

        List<BaseFragment> fragments = new Vector<BaseFragment>();
        fragments.add(new Tab1Fragment());
        fragments.add(new Tab2Fragment());
        fragments.add(new Tab3Fragment());
        fragments.add(new Tab4Fragment());
        fragments.add(new Tab5Fragment());
        mAdapter = new TabsPagerAdapter(getChildFragmentManager(), fragments);
        viewPager.setAdapter(mAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
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

        scrollableLayout.setCanScrollVerticallyDelegate(new CanScrollVerticallyDelegate() {
            @Override
            public boolean canScrollVertically(int direction) {
                return mAdapter.canScrollVertically(viewPager.getCurrentItem(), direction);
            }
        });

        /*scrollableLayout.setOnScrollChangedListener(new OnScrollChangedListener() {
            @Override
            public void onScrollChanged(int y, int oldY, int maxY) {
                final float tabsTranslationY;
                if(y < maxY)
                    tabsTranslationY = .0F;
                else
                    tabsTranslationY = y - maxY;

                TabHost.setTranslationY(tabsTranslationY);

                header.setTranslationY(y / 2);
            }
        });*/

        return rootView;
    }

    public class TabsPagerAdapter extends FragmentPagerAdapter {
        private List<BaseFragment> fragments;
        public TabsPagerAdapter(FragmentManager fragmentManager, List<BaseFragment> fragments) {
            super(fragmentManager);
            this.fragments = fragments;
        }

        // 스크롤 관련
        boolean canScrollVertically(int position, int direction) {
            return getItem(position).canScrollVertically(direction);
        }

        @Override
        public BaseFragment getItem(int position) {
            BaseFragment getItem = fragments.get(position);
            return getItem;
        }
        @Override
        public int getCount() {
            return fragments.size();
        }
    }

    public static TabHostLayoutFragment newInstance() {
        TabHostLayoutFragment fragment = new TabHostLayoutFragment();
        return fragment;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
