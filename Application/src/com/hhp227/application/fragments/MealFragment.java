package com.hhp227.application.fragments;

import java.util.List;
import java.util.Vector;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;

import com.hhp227.application.R;
import com.hhp227.application.ui.tabhostviewpager.FakeContent;
import com.hhp227.application.ui.tabhostviewpager.TabsPagerAdapter;

public class MealFragment extends Fragment {
	private ViewPager viewPager;
	private TabsPagerAdapter mAdapter;
    private TabHost TabHost;

	public MealFragment newInstance() {
		MealFragment fragment = new MealFragment();
        return fragment;
    }

    public MealFragment() {
        // Required empty public constructor
    }
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_tabs, container, false);
        
        TabHost = (TabHost) root.findViewById(android.R.id.tabhost);
        
        TabHost.setup();

        String[] Tabnames = {"문화관", "BTL", "상주생활관"};
        
        for(int i = 0; i < Tabnames.length; i++) {
        	TabHost.TabSpec tabSpec;
        	tabSpec = TabHost.newTabSpec(Tabnames [i]);
        	tabSpec.setIndicator(Tabnames [i]);
        	tabSpec.setContent(new FakeContent(getActivity()));
        	TabHost.addTab(tabSpec);
        }
		
		TabHost.setOnTabChangedListener(new OnTabChangeListener() {

			// 탭호스트 리스너
			@Override
			public void onTabChanged(String tabId) {
				int selectedItem = TabHost.getCurrentTab();
				viewPager.setCurrentItem(selectedItem);
			}
			
		});
       
        viewPager = (ViewPager) root.findViewById(R.id.viewPager);
		viewPager.setOffscreenPageLimit(Tabnames.length);
        
        List<Fragment> fragments = new Vector<Fragment>();
		fragments.add(new DCDormMealFragment());
		fragments.add(new BTLDormMealFragment());
        fragments.add(new SCDormMealFragment());
        mAdapter = new TabsPagerAdapter(getChildFragmentManager(), fragments);
        viewPager.setAdapter(mAdapter);
        viewPager.setOnPageChangeListener(new OnPageChangeListener() {

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

        return root;
	}
}