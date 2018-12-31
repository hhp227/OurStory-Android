package com.hhp227.application.fragments;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.hhp227.application.R;
import com.hhp227.application.app.AppController;
import com.hhp227.application.app.URLs;
import com.hhp227.application.members.MemberGridAdapter;
import com.hhp227.application.members.MemberItem;
import com.hhp227.application.scrollable.BaseFragment;

public class Tab4Fragment extends BaseFragment {
    private static final String TAG = "맴버목록";
    private ProgressDialog progressDialog;
    private GridView gridView;
    private MemberGridAdapter gridAdapter;
    private List<MemberItem> memberItems;

    public static Tab4Fragment newInstance() {
        Tab4Fragment fragment = new Tab4Fragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tab4, container, false);

        gridView = (GridView) rootView.findViewById(R.id.gridView);
        memberItems = new ArrayList<MemberItem>();
        gridAdapter = new MemberGridAdapter(getActivity(), memberItems);
        gridView.setAdapter(gridAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MemberItem memberItem = memberItems.get(position);
                String userid = (memberItem.getId());
                String name = ((TextView) view.findViewById(R.id.tvname_user)).getText().toString();
                String email = memberItem.getEmail();
                String profile_img = memberItem.getProfile_img();
                String created_at = memberItem.getTimeStamp();

                Bundle args = new Bundle();
                args.putString("userid", userid);
                args.putString("name", name);
                args.putString("email", email);
                args.putString("profile_img", profile_img);
                args.putString("created_at", created_at);

                showDialog(args);
            }
        });
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("불러오는중...");
        progressDialog.show();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Method.GET, URLs.URL_MEMBER, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                VolleyLog.d(TAG, "응답" + response.toString());
                if(response != null) {
                    parseJson(response);
                    hideProgressDialog();
                }
            }
            }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "응답" + error.getMessage());
                hideProgressDialog();
            }
        });
        AppController.getInstance().addToRequestQueue(jsonObjectRequest);

        return rootView;
    }

    private void parseJson(JSONObject jsonObject) {
        try {
            JSONArray jsonArray = jsonObject.getJSONArray("users");
            for(int i = 0; i < jsonArray.length(); i++) {
                JSONObject memberObject = (JSONObject) jsonArray.get(i);
                MemberItem memberItem = new MemberItem();
                memberItem.setId(memberObject.getString("id"));
                memberItem.setName(memberObject.getString("name"));
                // 프로필사진은 때때로 NULL
                String profile_img = memberObject.isNull("profile_img") ? null : memberObject.getString("profile_img");
                memberItem.setProfile_img(profile_img);
                memberItem.setTimeStamp(memberObject.getString("created_at"));

                memberItems.add(memberItem);
            }
            gridAdapter.notifyDataSetChanged();
        } catch(JSONException e) {
            Log.e(TAG, "에러" + e);
        }
    }

    private void hideProgressDialog() {
        if(progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    private void showDialog(Bundle args) {
        // Create the fragment and show it as a dialog.
        DialogFragment newFragment = UserFragment.newInstance();
        newFragment.setArguments(args);
        newFragment.show(getChildFragmentManager(), "dialog");
    }

    @Override
    public boolean canScrollVertically(int direction) {
        return gridView != null && gridView.canScrollVertically(direction);
    }
}
