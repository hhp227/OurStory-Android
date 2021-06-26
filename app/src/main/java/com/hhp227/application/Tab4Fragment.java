package com.hhp227.application;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.android.volley.*;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.hhp227.application.activity.FeedbackActivity;
import com.hhp227.application.activity.MyinfoActivity;
import com.hhp227.application.activity.VerInfoActivity;
import com.hhp227.application.app.AppController;
import com.hhp227.application.app.URLs;
import com.hhp227.application.dto.User;
import com.hhp227.application.fragment.GroupFragment;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Tab4Fragment extends Fragment implements View.OnClickListener {
    private static final String TAG = Tab4Fragment.class.getSimpleName();
    private boolean mIsAuth;
    private int mGroupId, mAuthorId;
    private String name, email, profile_img;

    public static Tab4Fragment newInstance(int groupId, int authorId) {
        Tab4Fragment fragment = new Tab4Fragment();
        Bundle args = new Bundle();

        args.putInt("group_id", groupId);
        args.putInt("author_id", authorId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mGroupId = getArguments().getInt("group_id");
            mAuthorId = getArguments().getInt("author_id");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tab, container, false);
        SwipeRefreshLayout swipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_layout);
        RecyclerView recyclerView = rootView.findViewById(R.id.recycler_view);

        swipeRefreshLayout.setEnabled(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(new RecyclerView.Adapter<ViewHolder>() {
            @Override
            public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                return new ViewHolder(LayoutInflater.from(getContext()).inflate(R.layout.fragment_tab4, parent, false));
            }

            @Override
            public void onBindViewHolder(ViewHolder holder, int position) {
                User user = AppController.Companion.getInstance().getPreferenceManager().getUser();
                mIsAuth = user.getId() == mAuthorId;

                name = user.getName();
                email = user.getEmail();
                profile_img = user.getProfileImage();

                holder.txtName.setText(name);
                holder.txtEmail.setText(email);
                Glide.with(getActivity())
                        .load(URLs.URL_USER_PROFILE_IMAGE + profile_img)
                        .apply(RequestOptions.errorOf(R.drawable.profile_img_circle).circleCrop())
                        .into(holder.profilePic);

                AdRequest adRequest = new AdRequest.Builder().build();
                holder.mAdView.loadAd(adRequest);
                holder.Profile.setOnClickListener(Tab4Fragment.this);
                holder.txtWithdrawal.setText("그룹" + (mIsAuth ? " 폐쇄" : " 탈퇴"));
                holder.WithDrawal.setOnClickListener(Tab4Fragment.this);
                holder.Appstore.setOnClickListener(Tab4Fragment.this);
                holder.Feedback.setOnClickListener(Tab4Fragment.this);
                holder.Version.setOnClickListener(Tab4Fragment.this);
                holder.Share.setOnClickListener(Tab4Fragment.this);
            }

            @Override
            public int getItemCount() {
                return 1;
            }
        });

        return rootView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.profile:
                profile();
                break;
            case R.id.llWithdrawal:
                withdrawal();
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
                break;
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

    private void withdrawal() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder.setMessage((mIsAuth ? "폐쇄" : "탈퇴") + "하시겠습니까?");
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.DELETE, (mIsAuth ? URLs.URL_GROUP : URLs.URL_LEAVE_GROUP) + "/" + mGroupId, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (!response.getBoolean("error")) {
                                getActivity().setResult(Activity.RESULT_OK, new Intent(getContext(), GroupFragment.class));
                                getActivity().finish();
                                // 글쓰기나 글삭제후 그룹탈퇴하면 GroupFragment 목록이 새로고침이 되지 않음
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyLog.e(TAG, error.getMessage());
                    }
                }) {
                    @Override
                    public Map<String, String> getHeaders() {
                        Map<String, String> headers = new HashMap<>();

                        headers.put("Authorization", AppController.Companion.getInstance().getPreferenceManager().getUser().getApiKey());
                        return headers;
                    }
                };

                AppController.Companion.getInstance().addToRequestQueue(jsonObjectRequest);
            }
        });
        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void appstore() {
        String appUrl = "https://play.google.com/store/apps/details?id=" + getContext().getPackageName();
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
        Intent intent = new Intent(getActivity(), VerInfoActivity.class);
        startActivity(intent);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView profilePic;
        private TextView txtName, txtEmail, txtWithdrawal;
        private LinearLayout Profile, WithDrawal, Version, Feedback, Appstore, Share;
        private AdView mAdView;

        ViewHolder(View itemView) {
            super(itemView);
            profilePic = itemView.findViewById(R.id.profilePic);
            txtName = itemView.findViewById(R.id.pname);
            txtEmail = itemView.findViewById(R.id.pemail);
            Profile = itemView.findViewById(R.id.profile);
            WithDrawal = itemView.findViewById(R.id.llWithdrawal);
            txtWithdrawal = itemView.findViewById(R.id.tvWithdrawal);
            Version = itemView.findViewById(R.id.verinfo);
            Appstore = itemView.findViewById(R.id.appstore);
            Feedback = itemView.findViewById(R.id.feedback);
            Share = itemView.findViewById(R.id.share);
            mAdView = itemView.findViewById(R.id.adView);
        }
    }
}
