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
import android.widget.Toast;

import com.android.volley.Cache;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.hhp227.application.R;
import com.hhp227.application.album.AlbumItem;
import com.hhp227.application.album.AlbumListAdapter;
import com.hhp227.application.app.AppController;
import com.hhp227.application.app.URLs;
import com.hhp227.application.scrollable.BaseFragment;
import com.hhp227.application.ui.floatingactionbutton.FloatingActionButton;
import com.hhp227.application.ui.staggeredgrid.grid.StaggeredGridView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class Tab2Fragment extends BaseFragment implements AbsListView.OnScrollListener {
    private static final String TAG = "커스텀그리드뷰";
    public static final String UPLOAD_URL = "http://hong227.dothome.co.kr/hong227/v1/php/AlbumUpload.php";
    private static final int PICK_IMAGE_REQUEST_CODE = 100;
    private ProgressDialog pDialog;
    private SwipeRefreshLayout SWPRefresh;
    private StaggeredGridView mGridView;
    private boolean mHasRequestedMore;
    private AlbumListAdapter mAdapter;
    private FloatingActionButton mFab;
    private String image_url;
    private List<AlbumItem> albumItems;
    private int offSet;

    public static Tab2Fragment newInstance() {
        Tab2Fragment fragment = new Tab2Fragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tab2, container, false);

        mFab = (FloatingActionButton) rootView.findViewById(R.id.fabbutton);
        SWPRefresh = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh_layout);
        mGridView = (StaggeredGridView) rootView.findViewById(R.id.grid_view);

        // 처음 offset은 0이다, json파싱이 되는동안 업데이트 될것
        offSet = 0;

        albumItems = new ArrayList<AlbumItem>();

        mAdapter = new AlbumListAdapter(getActivity(), albumItems);

        mGridView.setAdapter(mAdapter);
        mGridView.setOnScrollListener(this);

        // 끌어서 새로고침 리스너 등록
        SWPRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable(){
                    public void run() {
                        updateGridView();
                        SWPRefresh.setRefreshing(false);// 당겨서 새로고침 숨김
                    }
                }, 1000); // 1초동안 보이기
            }
        });
        pDialog = ProgressDialog.show(getActivity(), "", "불러오는중...");

        // 플로팅 액션버튼 클릭리스너 등록
        mFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                pickImage();
                return;
            }
        });

        // 처음 캐시메모리 요청을 체크
        Cache cache = AppController.getInstance().getRequestQueue().getCache();
        Cache.Entry entry = cache.get(URLs.URL_ALBUM);

        fetchData();

        return rootView;
    }

    private void updateGridView() {
        albumItems = new ArrayList<AlbumItem>();
        mAdapter = new AlbumListAdapter(getActivity(), albumItems);
        mGridView.setAdapter(mAdapter);

        fetchData();
    }

    private void fetchData() {
        // volley 요청 새로고침 json얻기
        JsonObjectRequest jsonReq = new JsonObjectRequest(Request.Method.GET, URLs.URL_ALBUM + offSet, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                VolleyLog.d(TAG, "응답: " + response.toString());
                if (response != null) {
                    parseJson(response);
                    hideDialog();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Volley에러: " + error.getMessage());
                hideDialog();
            }
        });

        AppController.getInstance().addToRequestQueue(jsonReq);
    }

    private void parseJson(JSONObject response) {
        try {
            JSONArray albumArray = response.getJSONArray("album");

            for (int i = 0; i < albumArray.length(); i++) {
                JSONObject albumObj = (JSONObject) albumArray.get(i);

                AlbumItem item = new AlbumItem();
                int id = albumObj.getInt("id");
                item.setId(id);
                item.setName(albumObj.getString("name"));
                item.setImge(image_url);
                item.setTimeStamp(albumObj.getString("created_at"));
                image_url = albumObj.getString("image");
                Log.e("확인", image_url);

                albumItems.add(item);

                offSet++;
            }
            mAdapter.notifyDataSetChanged();
        } catch (JSONException e) {
            Log.e(TAG, "에러" + e);
        }
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
        intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        Tab2Fragment.this.startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Toast.makeText(getActivity(), "Consumed by nested fragment", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean canScrollVertically(int direction) {
        return mGridView != null && mGridView.canScrollVertically(direction);
    }

    @Override
    public void onScrollStateChanged(final AbsListView view, final int scrollState) {
        Log.d(TAG, "onScrollStateChanged:" + scrollState);
    }

    @Override
    public void onScroll(final AbsListView view, final int firstVisibleItem, final int visibleItemCount, final int totalItemCount) {
        Log.d(TAG, "onScroll firstVisibleItem:" + firstVisibleItem +
                " visibleItemCount:" + visibleItemCount +
                " totalItemCount:" + totalItemCount);

        if (!mHasRequestedMore) {
            int lastInScreen = firstVisibleItem + visibleItemCount;
            if (lastInScreen >= totalItemCount) {
                Log.d(TAG, "onScroll lastInScreen - so load more");
                mHasRequestedMore = true;
                onLoadMoreItems();
            }
        }
    }

    private void onLoadMoreItems() {
        mAdapter.notifyDataSetChanged();
        mHasRequestedMore = false;
    }

    private void hideDialog() {
        if (pDialog != null) {
            pDialog.dismiss();
            pDialog = null;
        }
    }
}
