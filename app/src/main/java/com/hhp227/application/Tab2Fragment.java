package com.hhp227.application;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.android.volley.Cache;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.hhp227.application.app.AppController;
import com.hhp227.application.app.URLs;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Tab2Fragment extends Fragment implements AbsListView.OnScrollListener {
    private static final String TAG = "커스텀그리드뷰";

    public static final String UPLOAD_URL = "http://hong227.dothome.co.kr/hong227/v1/php/AlbumUpload.php";

    private static final int PICK_IMAGE_REQUEST_CODE = 100;

    private ProgressDialog pDialog;

    private SwipeRefreshLayout SWPRefresh;

    private boolean mHasRequestedMore;

    private String image_url;

    private int offSet;

    public static Tab2Fragment newInstance() {
        Tab2Fragment fragment = new Tab2Fragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tab2, container, false);
        SWPRefresh = rootView.findViewById(R.id.swipe_refresh_layout);

        // 끌어서 새로고침 리스너 등록
        SWPRefresh.setOnRefreshListener(() -> {
            new Handler().postDelayed(() -> {
                SWPRefresh.setRefreshing(false);// 당겨서 새로고침 숨김
            }, 1000); // 1초동안 보이기
        });
        pDialog = ProgressDialog.show(getActivity(), "", "불러오는중...");

        // 처음 캐시메모리 요청을 체크
        Cache cache = AppController.Companion.getInstance().getRequestQueue().getCache();
        Cache.Entry entry = cache.get(URLs.URL_ALBUM);

        fetchData();

        return rootView;
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

        AppController.Companion.getInstance().addToRequestQueue(jsonReq);
    }

    private void parseJson(JSONObject response) {
        try {
            JSONArray albumArray = response.getJSONArray("album");

            for (int i = 0; i < albumArray.length(); i++) {
                JSONObject albumObj = (JSONObject) albumArray.get(i);

                image_url = albumObj.getString("image");
                Log.e("확인", image_url);

                offSet++;
            }
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
            }
        }
    }

    private void hideDialog() {
        if (pDialog != null) {
            pDialog.dismiss();
            pDialog = null;
        }
    }
}
