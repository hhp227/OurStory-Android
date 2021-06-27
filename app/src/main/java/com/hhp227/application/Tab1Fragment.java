package com.hhp227.application;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.android.volley.*;
import com.android.volley.toolbox.JsonObjectRequest;
import com.hhp227.application.activity.PostDetailActivity;
import com.hhp227.application.adapter.ArticleListAdapter2;
import com.hhp227.application.app.AppController;
import com.hhp227.application.app.URLs;
import com.hhp227.application.dto.ImageItem;
import com.hhp227.application.dto.PostItem;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.hhp227.application.fragment.GroupFragment.UPDATE_CODE;

public class Tab1Fragment extends Fragment {
    public static final int FEEDINFO_CODE = 100; // 수정이 일어나면 요청코드

    private static final String TAG = "소식화면";

    private ArticleListAdapter2 mAdapter;

    private List<Object> mArticleItems;

    private ProgressBar mProgressBar;

    private RecyclerView mRecyclerView;

    private RecyclerView.OnScrollListener mOnScrollListener;

    private SwipeRefreshLayout swipeRefreshLayout;

    private int offSet, mGroupId;

    private boolean mHasRequestedMore;   // 데이터 불러올때 중복안되게 하기위한 변수

    private String mGroupName;

    public static Tab1Fragment newInstance(int groupId, String groupName) {
        Tab1Fragment fragment = new Tab1Fragment();
        Bundle arg = new Bundle();

        arg.putInt("group_id", groupId);
        arg.putString("group_name", groupName);
        fragment.setArguments(arg);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mGroupId = getArguments().getInt("group_id");
            mGroupName = getArguments().getString("group_name");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tab, container, false);
        mProgressBar = rootView.findViewById(R.id.progress_bar);
        mRecyclerView = rootView.findViewById(R.id.recycler_view);

        // 처음 캐시메모리 요청을 체크
        Cache cache = AppController.Companion.getInstance().getRequestQueue().getCache();
        Cache.Entry entry = cache.get(URLs.URL_POSTS);
        swipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_layout);
        mArticleItems = new ArrayList<>();
        mAdapter = new ArticleListAdapter2(getActivity(), mArticleItems);
        mOnScrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (!mHasRequestedMore && dy > 0 && layoutManager != null && layoutManager.findLastCompletelyVisibleItemPosition() >= layoutManager.getItemCount() - 1) {
                    mHasRequestedMore = true;
                    mAdapter.setLoaderVisibility(View.VISIBLE);
                    mAdapter.notifyItemChanged(mArticleItems.size() - 1);
                    offSet = mArticleItems.size() - 1;
                    fetchArticleList();
                }
            }
        };

        mAdapter.setLoaderVisibility(View.INVISIBLE);
        mAdapter.addFooterView(new Object());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnScrollListener(mOnScrollListener);
        mAdapter.setOnItemClickListener(new ArticleListAdapter2.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                PostItem postItem = (PostItem) mArticleItems.get(position);
                int feed_id = postItem.getId();
                int user_id = postItem.getUserId();
                String name = postItem.getName();
                String timeStamp = postItem.getTimeStamp();

                Intent intent = new Intent(getActivity(), PostDetailActivity.class);
                intent.putExtra("post_id", feed_id);
                intent.putExtra("user_id", user_id);
                intent.putExtra("name", name);
                intent.putExtra("timestamp", timeStamp);
                intent.putExtra("position", position);
                intent.putExtra("is_bottom", v.getId() == R.id.ll_reply);
                intent.putExtra("group_id", mGroupId);
                intent.putExtra("group_name", mGroupName);

                startActivityForResult(intent, FEEDINFO_CODE);
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }, 1000);
            }
        });
        showProgressBar();

        /**
         * 여기서부터 주석을 지우면 캐시메모리에서 저장된 json을 불러온다.
         * 즉 새로고침 한번만 함
         */
        if (entry != null) {
            // 캐시메모리에서 데이터 인출
            try {
                String data = new String(entry.data, "UTF-8");
                try {
                    parseJson(new JSONObject(data));
                    hideProgressBar();
                } catch (JSONException e) {
                    Log.e(TAG, "에러" + e);
                }
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, "에러" + e);
            }
        } else
            fetchArticleList();

        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRecyclerView.removeOnScrollListener(mOnScrollListener);
    }

    /**
     * 응답받은 파싱된 json데이터들을 리스트 뷰에 입력
     * */
    private void parseJson(JSONObject jsonObject) {
        try {
            JSONArray feedArray = jsonObject.getJSONArray("posts");

            for (int i = 0; i < feedArray.length(); i++) {
                JSONObject object = (JSONObject) feedArray.get(i);

                PostItem postItem = new PostItem();
                int id = object.getInt("id");
                postItem.setId(id);
                postItem.setUserId(object.getInt("user_id"));
                postItem.setName(object.getString("name"));

                // 이미지는 때때로 NULL
                JSONObject attachment = object.getJSONObject("attachment");
                JSONArray images = attachment.getJSONArray("images");

                ArrayList<ImageItem> imageItemList = new ArrayList<>();
                if (images.length() > 0) {
                    for (int j = 0; j < images.length(); j++) {
                        JSONObject imageJsonObject = images.getJSONObject(j);
                        ImageItem imageItem = new ImageItem(imageJsonObject.getInt("id"), imageJsonObject.getString("image"), imageJsonObject.getString("tag"));
                        imageItemList.add(imageItem);
                    }
                }
                postItem.setImageItemList(imageItemList);
                postItem.setText(object.getString("text"));
                postItem.setStatus("status");

                // 프로필사진은 때때로 NULL
                String profilePic = object.isNull("profile_img") ? null : object.getString("profile_img");
                postItem.setProfileImage(profilePic);
                postItem.setTimeStamp(object.getString("created_at"));
                postItem.setReplyCount(object.getInt("reply_count"));
                postItem.setLikeCount(object.getInt("like_count"));

                mArticleItems.add(mArticleItems.size() - 1, postItem);
                mAdapter.notifyItemInserted(mArticleItems.size() - 1);
            }

            // 중복 로딩 체크하는 Lock을 했던 HasRequestedMore변수를 풀어준다.
            mHasRequestedMore = false;
        } catch (JSONException e) {
            Log.e(TAG, "에러" + e);
        }
        mAdapter.setLoaderVisibility(View.INVISIBLE);
    }

    private void fetchArticleList() {
        String URL_POSTS = URLs.URL_POSTS.replace("{OFFSET}", String.valueOf(offSet));
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, URL_POSTS + "&group_id=" + mGroupId, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (response != null) {
                    parseJson(response);
                    hideProgressBar();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e(TAG, "Volley에러 : " + error.getMessage());
                mAdapter.setLoaderVisibility(View.GONE);
                mAdapter.notifyItemChanged(mArticleItems.size() - 1);
                hideProgressBar();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("api_key", "xxxxxxxxxxxxxxx");
                return headers;
            }
        };
        AppController.Companion.getInstance().addToRequestQueue(jsonObjectRequest);
    }

    public void onResume() {
        super.onResume();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FEEDINFO_CODE && resultCode == FEEDINFO_CODE) { // 피드 수정이 일어나면 클라이언트측에서 피드아이템을 수정
            int position = data.getIntExtra("position", 0);
            PostItem feedItem = (PostItem) mArticleItems.get(position);
            feedItem.setText(data.getStringExtra("text"));
            feedItem.setImageItemList(data.<ImageItem>getParcelableArrayListExtra("images"));
            feedItem.setReplyCount(data.getIntExtra("reply_count", 0));
            mArticleItems.set(position, feedItem);
            mAdapter.notifyItemChanged(position);
        } else if ((requestCode == UPDATE_CODE || requestCode == FEEDINFO_CODE) && resultCode == Activity.RESULT_OK) {
            offSet = 0;

            mArticleItems.clear();
            mAdapter.addFooterView(new Object());
            fetchArticleList();
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
