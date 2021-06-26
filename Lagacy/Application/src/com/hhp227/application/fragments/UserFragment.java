package com.hhp227.application.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.hhp227.application.R;
import com.hhp227.application.app.AppController;
import com.hhp227.application.app.URLs;
import com.hhp227.application.feed.CircularNetworkImageView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class UserFragment extends DialogFragment {
    private String userid, name, email, profile_img, created_at;
    private CircularNetworkImageView ProfileImg;
    private TextView Name, Created_At;

    private ImageLoader imageLoader = AppController.getInstance().getImageLoader();

    public static UserFragment newInstance() {
        UserFragment fragment = new UserFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getDialog() != null) {
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        View rootView = inflater.inflate(R.layout.fragment_user, container, false);

        Bundle bundle = getArguments();
        if (bundle != null) {
            userid = bundle.getString("userid");
            name = bundle.getString("name");
            email = bundle.getString("email");
            profile_img = bundle.getString("profile_img");
            created_at = bundle.getString("created_at");
        }
        ProfileImg = (CircularNetworkImageView) rootView.findViewById(R.id.profilePic);
        Name = (TextView) rootView.findViewById(R.id.name);
        Created_At = (TextView) rootView.findViewById(R.id.created_at);

        Name.setText(name);
        Created_At.setText(getPeriodTimeGenerator(getContext(), created_at));
        ProfileImg.setImageUrl(URLs.URL_USER_PROFILE_IMAGE + profile_img, imageLoader);
        ProfileImg.setDefaultImageResId(R.drawable.profile_img_circle);
        ProfileImg.setErrorImageResId(R.drawable.profile_img_circle);

        return rootView;
    }

    // 타임 제네레이터
    private static String getPeriodTimeGenerator(Context context, String strDate) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        df.setTimeZone(TimeZone.getDefault());
        Date date = null;

        if (TextUtils.isEmpty(strDate))
            return "";

        try {
            date = df.parse(strDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(context.getResources().getString(R.string.format_date));
        return sdf.format(date);
    }
}