package com.hhp227.application.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.hhp227.application.NoticeActivity;
import com.hhp227.application.SuggestActivity;
import com.hhp227.application.MyinfoActivity;
import com.hhp227.application.R;
import com.hhp227.application.VerinfoActivity;
import com.hhp227.application.app.AppController;
import com.hhp227.application.app.URLs;
import com.hhp227.application.feed.CircularNetworkImageView;
import com.hhp227.application.scrollable.BaseFragment;
import com.hhp227.application.user.User;

public class Tab5Fragment extends BaseFragment implements OnClickListener {
    private CircularNetworkImageView profilePic;
    private TextView txtName, txtKnu_id;
    private LinearLayout Profile, Notice, Version, Feedback, Appstore, Share;
    private String name, knu_id, profile_img;
	
	ImageLoader imageLoader = AppController.getInstance().getImageLoader();
	
    public static Tab5Fragment newInstance(Bundle args) {
    	Tab5Fragment fragment = new Tab5Fragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    	View rootView = inflater.inflate(R.layout.fragment_tab5, container, false);
        profilePic = (CircularNetworkImageView) rootView.findViewById(R.id.profilePic);
        
        txtName = (TextView) rootView.findViewById(R.id.pname);
        txtKnu_id = (TextView) rootView.findViewById(R.id.pknu_id);
        
        Profile = (LinearLayout) rootView.findViewById(R.id.profile);
        Version = (LinearLayout) rootView.findViewById(R.id.verinfo);
        Appstore = (LinearLayout) rootView.findViewById(R.id.appstore);
        Feedback = (LinearLayout) rootView.findViewById(R.id.feedback);
        Share = (LinearLayout) rootView.findViewById(R.id.share);
        Profile.setOnClickListener(this);
        Appstore.setOnClickListener(this);
        Feedback.setOnClickListener(this);
        Version.setOnClickListener(this);
        Share.setOnClickListener(this);
        
        User user = AppController.getInstance().getPrefManager().getUser();

        name = user.getName();
        knu_id = user.getKnu_id();
        profile_img = user.getProfile_img().equals(null) ? null : user.getProfile_img();

        txtName.setText(name);
        txtKnu_id.setText(knu_id);
        profilePic.setImageUrl(URLs.URL_USER_PROFILE_IMAGE + profile_img, imageLoader);
        
        return rootView;
    }
    
    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.profile :
                startActivity(new Intent(getActivity(), MyinfoActivity.class));
                break;
            case R.id.notice :
                startActivity(new Intent(getActivity(), NoticeActivity.class));
                break;
            case R.id.appstore :
                String appUrl = "https://play.google.com/store/apps/details?id=" + getContext().getPackageName();
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(appUrl)));
                break;
            case R.id.feedback :
                Intent intent = new Intent(getActivity(), SuggestActivity.class);
                startActivity(intent);
                break;
            case R.id.share :
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("text/plain");
                share.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                share.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                share.putExtra(Intent.EXTRA_TEXT, "확인하세요" + "\n" +
                        "GitHub Page :  https://localhost/" +
                        "Sample App : https://play.google.com/store/apps/details?id=" + getContext().getPackageName());
                startActivity(Intent.createChooser(share, getString(R.string.app_name)));
                break;
            case R.id.verinfo :
                startActivity(new Intent(getActivity(), VerinfoActivity.class));
                break;
        }
    }

    @Override
    public boolean canScrollVertically(int direction) {
        // TODO Auto-generated method stub
        return false;
    }

}