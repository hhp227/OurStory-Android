package com.hhp227.application.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.hhp227.application.FeedbackActivity;
import com.hhp227.application.MyinfoActivity;
import com.hhp227.application.R;
import com.hhp227.application.VerinfoActivity;
import com.hhp227.application.app.AppController;
import com.hhp227.application.app.URLs;
import com.hhp227.application.feed.CircularNetworkImageView;
import com.hhp227.application.scrollable.BaseFragment;
import com.hhp227.application.user.User;

public class Tab5Fragment extends BaseFragment implements View.OnClickListener {
    private CircularNetworkImageView profilePic;
    private TextView txtName, txtEmail;
    private LinearLayout Profile, Version, Feedback, Appstore, Share;
    private String name, email, profile_img;
    
    ImageLoader imageLoader = AppController.getInstance().getImageLoader();
    
    public static Tab5Fragment newInstance() {
        Tab5Fragment fragment = new Tab5Fragment();
        return fragment;
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tab5, container, false);
        profilePic = (CircularNetworkImageView) rootView.findViewById(R.id.profilePic);

        txtName = (TextView) rootView.findViewById(R.id.pname);
        txtEmail = (TextView) rootView.findViewById(R.id.pemail);

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
        email = user.getEmail();
        profile_img = user.getProfile_img().equals(null) ? null : user.getProfile_img();

        txtName.setText(name);
        txtEmail.setText(email);
        profilePic.setImageUrl(URLs.URL_USER_PROFILE_IMAGE + profile_img, imageLoader);

        return rootView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.profile:
                profile();
                break;
            case R.id.appstore:
                appstore();
                break;
            case R.id.feedback:
                feedback();
                break;
            case R.id.share:
                share();
                break;
            case R.id.verinfo:
                verinfo();
        }
    }
    private void share() {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        share.putExtra(Intent.EXTRA_SUBJECT,
                getString(R.string.app_name));
        share.putExtra(Intent.EXTRA_TEXT, "확인하세요" + "\n" +
                "GitHub Page :  https://localhost/" +
                "Sample App : https://play.google.com/store/apps/details?id=");
        startActivity(Intent.createChooser(share,
                getString(R.string.app_name)));
    }

    private void appstore() {
        String appUrl = "https://play.google.com/store/apps/details?id=" /*+ getPackageName()*/;
        Intent rateIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(appUrl));
        startActivity(rateIntent);
    }

    private void feedback() {
        Intent intent = new Intent(getActivity(), FeedbackActivity.class);
        startActivity(intent);
    }

    private void profile() {
        Intent intent = new Intent(getActivity(), MyinfoActivity.class);
        startActivity(intent);
    }
    private void verinfo() {
        Intent intent = new Intent(getActivity(), VerinfoActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean canScrollVertically(int direction) {
        // TODO Auto-generated method stub
        return false;
    }

}
