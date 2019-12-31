package com.hhp227.application.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hhp227.application.R;
import com.hhp227.application.scrollable.BaseFragment;

public class Tab3Fragment extends BaseFragment {
    public static Tab3Fragment newInstance() {
        Tab3Fragment fragment = new Tab3Fragment();
        return fragment;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tab3, container, false);

        return rootView;
    }

    @Override
    public boolean canScrollVertically(int direction) {
        return false;
    }
}
