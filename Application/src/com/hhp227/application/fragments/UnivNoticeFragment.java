package com.hhp227.application.fragments;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTag;
import android.app.ProgressDialog;
import android.content.Intent;
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
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.ListView;

import com.hhp227.application.R;
import com.hhp227.application.WebviewActivity;
import com.hhp227.application.bbs.BbsItem;
import com.hhp227.application.bbs.BbsListAdapter;

public class UnivNoticeFragment extends Fragment {
	private static final String TAG = "경북대게시판"; 
	private static String URL_PRIMARY = "http://www.knu.ac.kr";
    private URL URL;
	private SwipeRefreshLayout SWPRefresh;
    private Element BBS_DIV, BBS_TABLE, BBS_TBODY;
    
    private Source source;
    private ProgressDialog pDialog;
    private BbsListAdapter BBSAdapter;
    private ListView BBSList;
    private int BBSlocate;
    
    int mCurrentScrollState;
    private boolean mHasRequestedMore;

    private int offSet;
    private int maxPage = 3; // 최대볼수 있는 페이지 수

    private ArrayList<BbsItem> mListData;
    
    public static UnivNoticeFragment newInstance() {
        UnivNoticeFragment fragment = new UnivNoticeFragment();
        return fragment;
    }

    public UnivNoticeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_list, container, false);
        BBSList = (ListView)root.findViewById(R.id.listView);
        SWPRefresh = (SwipeRefreshLayout) root.findViewById(R.id.swipe_refresh_layout);
        
        // 처음 offset은 1이다, 파싱이 되는동안 업데이트 될것
        offSet = 1;
        
        mListData = new ArrayList<>();
        BBSAdapter = new BbsListAdapter(getActivity(), mListData);
        BBSList.setAdapter(BBSAdapter);
        BBSList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        	
        	@Override
        	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        		BbsItem mData = mListData.get(position); // 클릭한 포지션의 데이터를 가져온다.
                String URL_BCS = mData.getUrl(); //가져온 데이터 중 url 부분만 적출해낸다.
                Intent intent = new Intent(getActivity(),
        				WebviewActivity.class);
                intent.putExtra(WebviewActivity.URL, URL_PRIMARY + URL_BCS);// 인텐트 name
        		startActivity(intent);
        		}
        	});
        
        // 리스트뷰 스크롤 리스너 등록
        BBSList.setOnScrollListener(new OnScrollListener() {
        	
        	@Override
        	public void onScrollStateChanged(AbsListView view, int scrollState) {
        		mCurrentScrollState = scrollState;
        		}
        	
        	@Override
        	public void onScroll(AbsListView view, int firstVisibleItem,
        			int visibleItemCount, int totalItemCount) {
        		boolean loadMore = firstVisibleItem + visibleItemCount >= totalItemCount;
        		if (!mHasRequestedMore && loadMore && mCurrentScrollState != SCROLL_STATE_IDLE) {
        			Log.e(TAG, "???" + mHasRequestedMore);
        			
        			// offSet이 maxPage이면 더 안보여줌
        			// 페이지보기 제한 최대 maxPage개까지 더보기 할수있다.
        			if (offSet != maxPage) {
        				offSet++; // offSet증가
        				mHasRequestedMore = true; // mHasRequestedMore가 true로 바뀌어 데이터를 불러온다
        				onLoadMoreItems();
        				} else {
        					mHasRequestedMore = false;
        					}
        			}
        		}
        	});
        
		SWPRefresh.setOnRefreshListener(new OnRefreshListener() {
			
			@Override
			public void onRefresh() {
				new Handler().postDelayed(new Runnable(){
					public void run() {
						updateListView();
						SWPRefresh.setRefreshing(false);// 당겨서 새로고침 숨김
					}

					private void updateListView() {
						offSet = 1; // offSet초기화
						mListData = new ArrayList<>();
				        BBSAdapter = new BbsListAdapter(getActivity(), mListData);
				        BBSList.setAdapter(BBSAdapter);
				        try {
							process();
							BBSAdapter.notifyDataSetChanged();
						} catch (IOException e) {
							Log.e(TAG, "에러" + e);
						}
					}
				}, 1000);
			}
		});
        
		pDialog = new ProgressDialog(getActivity());
		pDialog.setMessage("게시판 정보 불러오는중...");
		pDialog.show();

        try {
        	/*
        	 *  네트워크 관련은 따로 쓰레드를 생성해야 UI 쓰레드와 겹치지 않는다. 
        	 *  그러므로 Thread 가 선언된 process 메서드를 호출한다.
        	 */
            process(); 
            BBSAdapter.notifyDataSetChanged();
        } catch (Exception e) {
        	Log.e(TAG, "에러" + e);
        }
        return root;
    }
    
    /**
     * 리스트 하단으로 가면 더 불러오기
     */
	private void onLoadMoreItems() {
		mHasRequestedMore = false;
		pDialog = new ProgressDialog(getActivity());
		pDialog.setMessage("게시판 정보 불러오는중...");
		pDialog.show();
			
		BBSAdapter = new BbsListAdapter(getActivity(), mListData);
	    BBSList.setAdapter(BBSAdapter);
		try {
			process(); 
	        BBSAdapter.notifyDataSetChanged();
	        } catch (Exception e) {
	        	Log.e(TAG, "에러" + e);
	        	}
		}

	private void process() throws IOException {

        new Thread() {

            @Override
            public void run() {

                try {
                    String url = URL_PRIMARY + 
                    		"/wbbs/wbbs/bbs/btin/list.action?bbs_cde=1&btin.page=" + 
                    		offSet + // 페이지, offSet이 증가할때마다 페이지 증가한 페이지 불러오기
                    		"&popupDeco=false&btin.search_type=&btin.search_text=&menu_idx=67";
                    URL = new URL(url);
                    InputStream html = URL.openStream();
                    source = new Source(new InputStreamReader(html, "utf-8")); //소스 인코딩 타입
                    source.fullSequentialParse(); // 순차적으로 구문분석
                } catch (Exception e) {
                	Log.e(TAG, "에러" + e);
                }

                List<StartTag> tabletags = source.getAllStartTags(HTMLElementName.DIV); // DIV 타입의 모든 태그들을 불러온다.

                for(int i = 0; i < tabletags.size(); i++){ // DIV 모든 태그중 bbsContent 태그가 몇번째임을 구한다.


                 if(tabletags.get(i).toString().equals("<div class=\"board_list\">")) {
                     BBSlocate = i; // DIV 클래스가 bbsContent 면 i 값을 BBSlocate 로 몇번째인지 저장한다.
                     Log.d(TAG, "게시판위치" + i); // i 로깅
                     break;
                 }
                }

                BBS_DIV = (Element) source.getAllElements(HTMLElementName.DIV).get(BBSlocate); // BBSlocate 번째 의 DIV 를 모두 가져온다.
                BBS_TABLE = (Element) BBS_DIV.getAllElements(HTMLElementName.TABLE).get(0); // 테이블 접속
                BBS_TBODY = (Element) BBS_TABLE.getAllElements(HTMLElementName.TBODY).get(0); // 데이터가 있는 TBODY 에 접속

                for(int i = 0; i < BBS_TBODY.getAllElements(HTMLElementName.TR).size(); i++){ // 여기서는 이제부터 게시된 게시물 데이터를 불러와 게시판 인터페이스를 구성할 것이다.

                    // 소스의 효율성을 위해서는 for 문을 사용하는것이 좋지만 , 이해를 돕기위해 소스를 일부로 늘려 두었다.

                    try {
                    	BbsItem item = new BbsItem();
                        Element BBS_TR = (Element) BBS_TBODY.getAllElements(HTMLElementName.TR).get(i); //TR 접속

                        Element BC_TYPE = (Element) BBS_TR.getAllElements(HTMLElementName.TD).get(0); //타입 을 불러온다.

                        Element BC_info = (Element) BBS_TR.getAllElements(HTMLElementName.TD).get(1); //URL(herf) TITLE(title) 을 담은 정보를 불러온다.
                        Element BC_a = (Element) BC_info.getAllElements(HTMLElementName.A).get(0); //BC_info 안의 a 태그를 가져온다.
                        
                        Element BC_writer = (Element) BBS_TR.getAllElements(HTMLElementName.TD).get(3); //글쓴이를 불러온다.
                        
                        Element BC_date = (Element) BBS_TR.getAllElements(HTMLElementName.TD).get(4); // 날짜를 불러온다.

                        item.setType(BC_TYPE.getContent().toString()); // 타입값을 담은 엘레먼트의 컨텐츠를 문자열로 변환시켜 가져온다.
                        item.setTitle(BC_a.getTextExtractor().toString()); // a 태그의 title 은 BCS_title 로 선언
                        item.setUrl(BC_a.getAttributeValue("href")); // a 태그의 herf 는 BCS_url 로 선언
                        item.setWriter(BC_writer.getContent().toString()); // 작성자값을 담은 엘레먼트의 컨텐츠를 문자열로 변환시켜 가져온다.
                        item.setDate(BC_date.getContent().toString()); // 작성일자값을 담은 엘레먼트의 컨텐츠를 문자열로 변환시켜 가져온다.

                        mListData.add(item); // 데이터 리스트 클래스에 데이터들을 등록한다.
                    }catch(Exception e){
                    	Log.e(TAG, "에러" + e);
                    }
                    }
                Handler mHandler = new Handler(Looper.getMainLooper());
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        BBSAdapter.notifyDataSetChanged(); // 모든 작업이 끝나면 리스트 갱신
                        hideDialog();
                    }
                }, 0);
           }

        }.start();

    }
	
    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}
