package com.hhp227.application.fragments;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.hhp227.application.R;
import com.hhp227.application.app.AppController;
import com.hhp227.application.app.URLs;
import com.hhp227.application.seat.SeatItem;
import com.hhp227.application.seat.SeatListAdapter;

public class DaeguSeatFragment extends Fragment {
    private static final String TAG = "대구 열람실좌석";
    private ProgressDialog progressDialog;
    private ListView listView;
    private List<SeatItem> seatItemList;
    private SeatListAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private boolean isRefresh;

    public static DaeguSeatFragment newInstance() {
        DaeguSeatFragment fragment = new DaeguSeatFragment();
        return fragment;
    }

    public DaeguSeatFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_list, container, false);

        listView = (ListView) root.findViewById(R.id.listView);
        swipeRefreshLayout = (SwipeRefreshLayout) root.findViewById(R.id.swipe_refresh_layout);

        seatItemList = new ArrayList<>();
        adapter = new SeatListAdapter(getActivity(), seatItemList);
        listView.setAdapter(adapter);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isRefresh = true;
                        fetchData();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }, 1000);
            }
        });

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(false);
        progressDialog.setMessage("불러오는중...");
        showProgressDialog();

        fetchData();

        return root;
    }

    private void fetchData() {
        String endPoint = URLs.URL_KNULIBRARY_SEAT.replace("{ID}", "1");
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, endPoint, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject jsonObject = response.getJSONObject("data");
                    JSONArray jsonArray = jsonObject.getJSONArray("list");
                    for(int i = 0; i < jsonArray.length(); i++) {
                        JSONObject data = jsonArray.getJSONObject(i);

                        int id = data.getInt("id");
                        String name = data.getString("name");
                        int total = data.getInt("activeTotal");
                        int occupied = data.getInt("occupied");
                        int available = data.getInt("available");

                        String[] disable = null;
                        try {
                            JSONObject disablePeriod = data.getJSONObject("disablePeriod");
                            disable = new String[3];
                            disable[0] = disablePeriod.getString("name");
                            disable[1] = disablePeriod.getString("beginTime");
                            disable[2] = disablePeriod.getString("endTime");
                        } catch(JSONException e) {
                            Log.e(TAG, e.getMessage());
                        }

                        SeatItem listItem = new SeatItem(id, name, total, occupied, available, disable);

                        if(isRefresh == false)
                            seatItemList.add(listItem);
                        else
                            seatItemList.set(i, listItem);
                    }
                    adapter.notifyDataSetChanged();
                } catch(JSONException e) {
                    Log.e(TAG, e.getMessage());
                }
                hideProgressDialog();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e(TAG, error.getMessage());
                hideProgressDialog();
            }
        });
        AppController.getInstance().addToRequestQueue(jsonObjectRequest);
    }

    private void showProgressDialog() {
        if(!progressDialog.isShowing())
            progressDialog.show();
    }

    private void hideProgressDialog() {
        if(progressDialog.isShowing())
            progressDialog.dismiss();
    }
}