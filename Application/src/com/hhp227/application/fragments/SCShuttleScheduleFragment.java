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

public class SCShuttleScheduleFragment extends Fragment {
	private static final String TAG = "학교버스시간표";
	private SwipeRefreshLayout SWPRefresh;
	private Handler handler;
	private Source source;
	private ArrayList<HashMap<String, String>> data;
    private ProgressDialog progressDialog;
    private ListView ShuttleList;
	private SimpleAdapter ShuttleAdapter;
    
    public static SCShuttleScheduleFragment newInstance() {
    	SCShuttleScheduleFragment fragment = new SCShuttleScheduleFragment();
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
        
        ShuttleAdapter = new SimpleAdapter(getActivity(), data, R.layout.shuttle_sc_item,
        		new String[]{"col1", "col2", "col3", "col4", "col5", "col6"}, 
        		new int[]{R.id.column1, R.id.column2, R.id.column3, R.id.column4, R.id.column5, R.id.column6});

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
						URL URL = new URL(URLs.URL_SHUTTLE);
						InputStream html = URL.openStream();
						source = new Source(new InputStreamReader(html, "utf-8")); // 소스를 UTF-8 인코딩으로 불러온다.
						source.fullSequentialParse(); // 순차적으로 구문분석
					} catch(Exception e) {
						Log.e(TAG, "에러" + e);
					}

					Element table = (Element) source.getAllElements(HTMLElementName.TABLE).get(0);

					for(int i = 1; i < table.getAllElements(HTMLElementName.TR).size(); i++) {
						Element TR = (Element) table.getAllElements(HTMLElementName.TR).get(i);
						HashMap<String, String> hashmap = new HashMap<String, String>();

						Element Col1 = (Element) TR.getAllElements(HTMLElementName.TD).get(0);
						Element Col2 = (Element) TR.getAllElements(HTMLElementName.TD).get(1);
						Element Col3 = (Element) TR.getAllElements(HTMLElementName.TD).get(3);
						Element Col4 = (Element) TR.getAllElements(HTMLElementName.TD).get(4);
						Element Col5 = (Element) TR.getAllElements(HTMLElementName.TD).get(5);
						Element Col6 = (Element) TR.getAllElements(HTMLElementName.TD).get(7);

						hashmap.put("col1", (Col1).getContent().toString());
						hashmap.put("col2", (Col2).getContent().toString());
						hashmap.put("col3", (Col3).getContent().toString());
						hashmap.put("col4", (Col4).getContent().toString());
						hashmap.put("col5", (Col5).getContent().toString());
						hashmap.put("col6", (Col6).getContent().toString());

						data.add(hashmap);
					}

					handler = new Handler(Looper.getMainLooper());
					handler.postDelayed(new Runnable() {
						@Override
						public void run() {
							ShuttleAdapter.notifyDataSetChanged(); // 모든 작업이 끝나면 리스트 갱신
							progressDialog.dismiss(); // 모든 작업이 끝나면 다이어로그 종료
						}
					}, 0);
				}
			}.start();
        } catch(Exception e) {
        	Log.e(TAG, "에러" + e);
        }
        
        return root;
    }
}