package com.hhp227.application.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.StringRequest;
import com.hhp227.application.R;
import com.hhp227.application.app.AppController;
import com.hhp227.application.app.URLs;
import com.hhp227.application.user.User;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class MyinfoFragment extends Fragment {
    private static final String TAG = "유저정보화면";
    private static String name, email, profile_img, created_at;
    private TextView txtName, txtEmail, txtCreated_At;
    private NetworkImageView profileImg;
    private User user;

    ImageLoader imageLoader = AppController.getInstance().getImageLoader();

    public MyinfoFragment newInstance() {
        MyinfoFragment fragment = new MyinfoFragment();
        return fragment;
    }

    public MyinfoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_myinfo, container, false);

        txtName = (TextView) root.findViewById(R.id.tvname);
        txtEmail = (TextView) root.findViewById(R.id.tvemail);
        txtCreated_At = (TextView) root.findViewById(R.id.tvjoin);
        profileImg = (NetworkImageView) root.findViewById(R.id.profileimage);

        user = AppController.getInstance().getPrefManager().getUser();

        name = user.getName();
        email = user.getEmail();
        profile_img = user.getProfile_img().equals(null) ? null : user.getProfile_img();
        created_at = user.getCreated_at();

        txtName.setText(name);
        txtEmail.setText(email);
        txtCreated_At.setText(getPeriodTimeGenerator(getActivity(), created_at).concat(" 가입"));
        profileImg.setImageUrl(URLs.URL_USER_PROFILE_IMAGE + profile_img, imageLoader);
        profileImg.setDefaultImageResId(R.drawable.profile_img_square);
        profileImg.setErrorImageResId(R.drawable.profile_img_square);

        profileImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerForContextMenu(v);
                getActivity().openContextMenu(v);
                unregisterForContextMenu(v);
            }
        });

        return root;
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

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("프로필 이미지 변경");
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.myinfo, menu);
    }
    /**
     * 프로필사진을 누르면 뜨는 메뉴
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.album:
                pickImage();
                return true;
            case R.id.camera:
                captureImage();
                return true;
            case R.id.remove:
                resetImage();
                return true;
        }
        return false;
    }

    private void captureImage() {
    }

    private void pickImage() {
    }

    private void resetImage() {
        action_Send();
    }

    private void action_Send() {
        StringRequest stringRequest = new StringRequest(Request.Method.PUT, URLs.URL_PROFILE_EDIT, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                user.setProfile_img(null);
                profileImg.setImageUrl(user.getProfile_img(), imageLoader);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Volley에러 : " + error.getMessage());
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", user.getApi_key());
                return headers;
            }
            @Override
            protected Map<String, String> getParams() {
                // 전송 받을때 받는 입력값들
                Map<String, String> params = new HashMap<String, String>();
                params.put("status", "1");

                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(stringRequest);
    }
}