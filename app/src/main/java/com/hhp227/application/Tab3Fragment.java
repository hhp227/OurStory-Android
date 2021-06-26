package com.hhp227.application;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.hhp227.application.adapter.MemberGridAdapter;
import com.hhp227.application.app.AppController;
import com.hhp227.application.dto.MemberItem;

import com.hhp227.application.fragment.UserFragment;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.hhp227.application.app.URLs.URL_MEMBER;

public class Tab3Fragment extends Fragment {
    private static final String TAG = "맴버목록";
    private List<MemberItem> mMemberItems;
    private MemberGridAdapter mAdapter;
    private ProgressBar mProgressBar;
    private RecyclerView mRecyclerView;
    private RecyclerView.OnScrollListener mOnScrollListener;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private int mGroupId;

    public static Tab3Fragment newInstance(int groupId) {
        Tab3Fragment fragment = new Tab3Fragment();
        Bundle args = new Bundle();

        args.putInt("group_id", groupId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mGroupId = getArguments().getInt("group_id");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tab, container, false);
        mProgressBar = rootView.findViewById(R.id.progress_bar);
        mRecyclerView = rootView.findViewById(R.id.recycler_view);
        mSwipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_layout);
        mMemberItems = new ArrayList<>();
        mAdapter = new MemberGridAdapter(getActivity(), mMemberItems);
        mOnScrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        };

        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 4));
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnScrollListener(mOnScrollListener);
        mAdapter.setOnItemClickListener(new MemberGridAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(View v, int position) {
                MemberItem memberItem = mMemberItems.get(position);
                String userid = (memberItem.getId());
                String name = ((TextView) v.findViewById(R.id.tvname_user)).getText().toString();
                String email = memberItem.getEmail();
                String profile_img = memberItem.getProfile_img();
                String created_at = memberItem.getTimeStamp();

                Bundle args = new Bundle();
                args.putString("user_id", userid);
                args.putString("name", name);
                args.putString("email", email);
                args.putString("profile_img", profile_img);
                args.putString("created_at", created_at);

                DialogFragment newFragment = UserFragment.Companion.newInstance();
                newFragment.setArguments(args);
                newFragment.show(getChildFragmentManager(), "dialog");
            }
        });
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mMemberItems.clear();
                        mSwipeRefreshLayout.setRefreshing(false);
                        fetchDataTask();
                    }
                }, 1000);
            }
        });
        showProgressBar();
        fetchDataTask();

        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRecyclerView.removeOnScrollListener(mOnScrollListener);
    }

    private void fetchDataTask() {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, URL_MEMBER + "/" + mGroupId, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                VolleyLog.d(TAG, "응답" + response.toString());
                if (response != null) {
                    parseJson(response);
                    hideProgressBar();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "응답" + error.getMessage());
                hideProgressBar();
            }
        });
        AppController.Companion.getInstance().addToRequestQueue(jsonObjectRequest);
    }

    private void parseJson(JSONObject jsonObject) {
        try {
            JSONArray jsonArray = jsonObject.getJSONArray("users");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject memberObject = (JSONObject) jsonArray.get(i);
                MemberItem memberItem = new MemberItem();
                memberItem.setId(memberObject.getString("id"));
                memberItem.setName(memberObject.getString("name"));
                // 프로필사진은 때때로 NULL
                String profile_img = memberObject.isNull("profile_img") ? null : memberObject.getString("profile_img");
                memberItem.setProfile_img(profile_img);
                memberItem.setTimeStamp(memberObject.getString("created_at"));

                mMemberItems.add(memberItem);
                mAdapter.notifyItemChanged(mMemberItems.size() - 1);
            }
        } catch (JSONException e) {
            Log.e(TAG, "에러" + e);
        }
    }

    private void showProgressBar() {
        if (mProgressBar != null && mProgressBar.getVisibility() == View.GONE)
            mProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        if (mProgressBar != null && mProgressBar.getVisibility() == View.VISIBLE)
            mProgressBar.setVisibility(View.GONE);
    }
}
