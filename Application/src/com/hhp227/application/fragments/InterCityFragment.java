package com.hhp227.application.fragments;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.hhp227.application.R;
import com.hhp227.application.app.URLs;

public class InterCityFragment extends Fragment {
	private static final String TAG = "시외버스시간표";
	private SwipeRefreshLayout SWPRefresh;
	private Handler handler;
	private Source source;
	private ArrayList<HashMap<String, String>> data;
    private ProgressDialog progressDialog;
    private ListView ShuttleList;
	private SimpleAdapter ShuttleAdapter;
    
    public static InterCityFragment newInstance() {
    	InterCityFragment fragment = new InterCityFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_shuttle_schedule_sc, container, false);
        ShuttleList = (ListView)root.findViewById(R.id.listView);
        data = new ArrayList<HashMap<String, String>>();
        SWPRefresh = (SwipeRefreshLayout) root.findViewById(R.id.swipe_refresh_layout);
        
        ShuttleAdapter = new SimpleAdapter(getActivity(), data, R.layout.shuttle_item,
        		new String[]{"구분", "시간"},
        		new int[]{R.id.division, R.id.time_label});

        ShuttleList.setAdapter(ShuttleAdapter);
        
		SWPRefresh.setOnRefreshListener(new OnRefreshListener() {
			
			@Override
			public void onRefresh() {
				new Handler().postDelayed(new Runnable() {
					public void run() {
						SWPRefresh.setRefreshing(false); // 당겨서 새로고침 숨김
					}
				}, 1000);
			}
		});

		progressDialog = new ProgressDialog(getActivity());
		progressDialog.setMessage("불러오는중...");
		progressDialog.show();
		
        try {
			new Thread() {
				public void run() {
					try {
						URL URL = new URL(URLs.URL_INTER_CITY_SHUTTLE);
						InputStream html = URL.openStream();
						// html소스 코드 인코딩 방식
						source = new Source(new InputStreamReader(html, "utf-8"));
						source.fullSequentialParse(); // 순차적으로 구문분석
					} catch(Exception e) {
						Log.e(TAG, "에러" + e);
					}

					Element table = (Element) source.getAllElements(HTMLElementName.TABLE).get(13);

					Log.d(TAG, "TABLE 갯수" + source.getAllElements(HTMLElementName.TABLE).size());

					for(int i = 1; i < table.getAllElements(HTMLElementName.TD).size(); i++) {
						Element TR = (Element) table.getAllElements(HTMLElementName.TD).get(i);
						HashMap<String, String> hashmap = new HashMap<String, String>();

						Element Time = (Element) TR.getAllElements(HTMLElementName.B).get(0);

						hashmap.put("구분", "대구 북부정류장");
						hashmap.put("시간", (Time).getContent().toString());

						data.add(hashmap);
					}

					handler = new Handler(Looper.getMainLooper());
					handler.postDelayed(new Runnable() {
						@Override
						public void run() {
							// 모든 작업이 끝나면 리스트 갱신
							ShuttleAdapter.notifyDataSetChanged();
							progressDialog.dismiss(); // 모든 작업이 끝나면 다이어로그 종료
						}
					}, 0);
				}
			}.start();
        } catch (Exception e) {
        	Log.e(TAG, "에러" + e);
        }
        
        return root;
    }
}