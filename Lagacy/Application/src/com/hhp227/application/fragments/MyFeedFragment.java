package com.hhp227.application.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.hhp227.application.R;
import com.hhp227.application.ArticleActivity;
import com.hhp227.application.app.AppController;
import com.hhp227.application.app.URLs;
import com.hhp227.application.feed.FeedItem;
import com.hhp227.application.feed.FeedListAdapter;
import com.hhp227.application.ui.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyFeedFragment extends Fragment {
    private static final String TAG = "내가 쓴 피드";
    private FloatingActionButton floatingActionButton;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView listView;
    private List<FeedItem> feedItems;
    private int listViewPosition;
    private FeedListAdapter listAdapter;
    private ProgressDialog progressDialog;

    public static MyFeedFragment newInstance() {
        MyFeedFragment fragment = new MyFeedFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tab1, container, false);

        floatingActionButton = (FloatingActionButton) rootView.findViewById(R.id.fabbutton);
        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh_layout);
        listView = (ListView) rootView.findViewById(R.id.list);

        floatingActionButton.setVisibility(View.GONE);

        feedItems = new ArrayList<FeedItem>();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listViewPosition = position;
                FeedItem feedItem = feedItems.get(position);
                int feed_id = feedItem.getId();
                int user_id = feedItem.getUser_id();
                String name = ((TextView) view.findViewById(R.id.name)).getText().toString();
                String timeStamp = ((TextView) view.findViewById(R.id.timestamp)).getText().toString();

                Intent intent = new Intent(getActivity(), ArticleActivity.class);
                intent.putExtra("feed_id", feed_id);
                intent.putExtra("user_id", user_id);
                intent.putExtra("name", name);
                intent.putExtra("timeStamp", timeStamp);

                startActivityForResult(intent, Tab1Fragment.FEEDINFO_CODE);
            }
        });

        listAdapter = new FeedListAdapter(getActivity(), feedItems);
        listView.setAdapter(listAdapter);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        listAdapter.notifyDataSetChanged();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }, 1000);
            }
        });
        progressDialog = ProgressDialog.show(getActivity(), "", "불러오는중...");

        JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(URLs.URL_FEEDS.concat("/"), new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                VolleyLog.d(TAG, "응답 : " + response.toString());
                if (response != null) {
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject object = (JSONObject) response.get(i);

                            FeedItem feedItem = new FeedItem();
                            int id = object.getInt("id");
                            feedItem.setId(id);
                            feedItem.setUser_id(object.getInt("user_id"));
                            feedItem.setName(object.getString("name"));

                            // 이미지는 때때로 NULL
                            String[] images = object.isNull("image") ? null : object.getString("image").split("[|]");
                            feedItem.setImage(object.isNull("image") ? null : images[0]);

                            feedItem.setFeed(object.getString("feed"));
                            feedItem.setStatus("status");
                            // 프로필사진은 때때로 NULL
                            String profilePic = object.isNull("profile_img") ? null : object.getString("profile_img");
                            feedItem.setProfilePic(profilePic);
                            feedItem.setTimeStamp(object.getString("created_at"));

                            feedItem.setReplyCount(object.getString("reply_count"));
                            feedItem.setLikeCount(object.getInt("like_count"));

                            feedItems.add(feedItem);
                        }
                        listAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        Log.e(TAG, "에러" + e);
                    }
                    hideProgressDialog();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Volley에러 : " + error.getMessage());
                hideProgressDialog();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", AppController.getInstance().getPrefManager().getUser().getApi_key());
                return headers;
            }
        };
        AppController.getInstance().addToRequestQueue(jsonObjectRequest);

        return rootView;
    }

    public void onResume() {
        super.onResume();
        listAdapter.notifyDataSetChanged();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Tab1Fragment.FEEDINFO_CODE && resultCode == Tab1Fragment.FEEDINFO_CODE) { // 피드 수정이 일어나면 클라이언트측에서 피드아이템을 수정
            FeedItem feedItem = feedItems.get(listViewPosition);
            feedItem.setFeed(data.getStringExtra("feed"));
            String[] images = data.getStringExtra("image").equals("null") ? null : data.getStringExtra("image").split("[|]");
            feedItem.setImage(data.getStringExtra("image").equals("null") ? null : images[0]);
            feedItem.setReplyCount(data.getStringExtra("reply_count"));
            feedItems.set(listViewPosition, feedItem);
        }
    }

    private void hideProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }
}
