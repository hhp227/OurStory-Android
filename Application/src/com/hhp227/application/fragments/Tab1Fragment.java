package com.hhp227.application.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Cache;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.hhp227.application.ArticleActivity;
import com.hhp227.application.R;
import com.hhp227.application.WriteActivity;
import com.hhp227.application.app.AppController;
import com.hhp227.application.app.URLs;
import com.hhp227.application.feed.FeedItem;
import com.hhp227.application.feed.FeedListAdapter;
import com.hhp227.application.scrollable.BaseFragment;
import com.hhp227.application.ui.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Tab1Fragment extends BaseFragment {
    private static final String TAG = "소식화면";
    public static final int FEEDINFO_CODE = 100; // 수정이 일어나면 요청코드
    private ProgressDialog progressDialog;
    private ListView listView;
    private FeedListAdapter listAdapter;
    private List<FeedItem> feedItems;
    private SwipeRefreshLayout swipeRefreshLayout;
    private View FooterLoading;
    private FloatingActionButton floatingActionButton;
    private int offSet, listViewPosition;

    private boolean HasRequestedMore = false;   // 데이터 불러올때 중복안되게 하기위한 변수
    private boolean lastItemVisibleFlag = false;

    public static Tab1Fragment newInstance() {
        Tab1Fragment fragment = new Tab1Fragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tab1, container, false);

        floatingActionButton = (FloatingActionButton) rootView.findViewById(R.id.fabbutton);
        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh_layout);
        listView = (ListView) rootView.findViewById(R.id.list);
        FooterLoading = View.inflate(getActivity(), R.layout.load_more, null);
        listView.addFooterView(FooterLoading);

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

                startActivityForResult(intent, FEEDINFO_CODE);
            }
        });

        // 리스트뷰 스크롤 리스너 등록
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                Log.d(TAG, "onScrollStateChanged:" + scrollState);
                if (scrollState == SCROLL_STATE_IDLE && lastItemVisibleFlag && HasRequestedMore == false) {
                    FooterLoading.setVisibility(View.VISIBLE);
                    // 다음 데이터를 불러온다.
                    onLoadMoreItems();
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                lastItemVisibleFlag = (totalItemCount > 0) && (firstVisibleItem + visibleItemCount >= totalItemCount);
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

        /**
         * 플로팅액션버튼을 리스트뷰에 장착, 장착하고 리스트하단에 가면 불러오기가 안됨
         */
        //floatingActionButton.listenTo(listView);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), WriteActivity.class);
                startActivity(intent);
                return;
            }
        });

        // 처음 캐시메모리 요청을 체크
        Cache cache = AppController.getInstance().getRequestQueue().getCache();
        Cache.Entry entry = cache.get(URLs.URL_FEEDS);

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
                    hideProgressDialog();
                } catch (JSONException e) {
                    Log.e(TAG, "에러" + e);
                }
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, "에러" + e);
            }
        } else
            fetchFeeds();

        return rootView;
    }

    private void hideProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
        FooterLoading.setVisibility(View.GONE);
    }

    /**
     * 응답받은 파싱된 json데이터들을 리스트 뷰에 입력
     * */
    private void parseJson(JSONObject jsonObject) {
        try {
            JSONArray feedArray = jsonObject.getJSONArray("feeds");

            for (int i = 0; i < feedArray.length(); i++) {
                JSONObject object = (JSONObject) feedArray.get(i);

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
            // 중복 로딩 체크하는 Lock을 했던 HasRequestedMore변수를 풀어준다.
            HasRequestedMore = false;
            listAdapter.notifyDataSetChanged();
        } catch (JSONException e) {
            Log.e(TAG, "에러" + e);
        }
    }

    private void fetchFeeds() {
        // Volley 요청 새로고침 json얻기
        String URL_FEEDS = URLs.URL_FEEDS + "?offset=";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, URL_FEEDS + offSet, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                VolleyLog.d(TAG, "응답 : " + response.toString());
                if (response != null) {
                    parseJson(response);
                    hideProgressDialog();
                    // 리스트뷰 거꾸로 출력
                    //Collections.reverse(feedItems);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Volley에러 : " + error.getMessage());
                hideProgressDialog();
            }
        }) {
            /**
             * 헤더 요청
             */
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json");
                headers.put("api_key", "xxxxxxxxxxxxxxx");
                return headers;
            }
        };
        AppController.getInstance().addToRequestQueue(jsonObjectRequest);
    }

    /**
     * 리스트 하단으로 가면 더 불러오기
     */
    private void onLoadMoreItems() {
        offSet = feedItems.size();
        HasRequestedMore = true;
        fetchFeeds();
    }

    public void onResume() {
        super.onResume();
        listAdapter.notifyDataSetChanged();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FEEDINFO_CODE && resultCode == FEEDINFO_CODE) { // 피드 수정이 일어나면 클라이언트측에서 피드아이템을 수정
            FeedItem feedItem = feedItems.get(listViewPosition);
            feedItem.setFeed(data.getStringExtra("feed"));
            String[] images = data.getStringExtra("image").equals("null") ? null : data.getStringExtra("image").split("[|]");
            feedItem.setImage(data.getStringExtra("image").equals("null") ? null : images[0]);
            feedItem.setReplyCount(data.getStringExtra("reply_count"));
            feedItems.set(listViewPosition, feedItem);
        }
    }

    @Override
    public boolean canScrollVertically(int direction) {
        return listView != null && listView.canScrollVertically(direction);
    }
}
