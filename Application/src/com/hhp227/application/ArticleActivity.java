package com.hhp227.application;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.hhp227.application.app.AppController;
import com.hhp227.application.app.URLs;
import com.hhp227.application.feed.CircularNetworkImageView;
import com.hhp227.application.feed.FeedImageView;
import com.hhp227.application.fragments.Tab1Fragment;
import com.hhp227.application.reply.ReplyItem;
import com.hhp227.application.reply.ReplyListAdapter;
import com.hhp227.application.ui.navigationdrawer.DrawerArrowDrawable;
import com.hhp227.application.user.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.hhp227.application.fragments.Tab1Fragment.FEEDINFO_CODE;

public class ArticleActivity extends Activity {
    protected static final String TAG = "피드화면";
    public static final int REQUEST_CODE = 1;

    private static int feed_id, user_id, reply_count, myUser_id;
    private static String apikey, writer, feed, timeStamp, profile_img, image;
    private ProgressDialog progressDialog;
    private ListView listView;
    private TextView labelName, labelTime, labelContents, buttonSend;
    private EditText inputReply;
    private SwipeRefreshLayout swipeRefreshLayout;
    private View headerView;
    private LinearLayout feedImageViews;
    private CircularNetworkImageView labelProfilepic;
    private FeedImageView feedImageView;
    private List<ReplyItem> replyItemList;
    private ReplyListAdapter replyListAdapter;
    private boolean isUpdate; // 피드 업데이트했는지 확인할 변수

    // 콘텍스트메뉴
    private AdapterView.AdapterContextMenuInfo info;
    private int contextmenu_number;
    private CharSequence clipboard_content;

    // 리스트 하단
    private int position;
    public boolean isBottom;

    //댓글 입력 비었는지
    private boolean isinputReplyEmpty;
    ImageLoader imageLoader = AppController.getInstance().getImageLoader();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);
        ActionBar actionBar = getActionBar();
        // 액션바 뒤로가기
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setHomeAsUpIndicator(new DrawerArrowDrawable(this) {
            @Override
            public boolean isLayoutRtl() {
                return false;
            }
        });

        // PreferenceManager에서 apikey를 인출
        User user = AppController.getInstance().getPrefManager().getUser();
        apikey = user.getApi_key();
        myUser_id = user.getId();

        Intent intent = getIntent();

        position = intent.getIntExtra("position", position);
        isBottom = intent.getBooleanExtra("isbottom", false);

        feed_id = intent.getIntExtra("feed_id", feed_id);
        user_id = intent.getIntExtra("user_id", user_id);
        writer = intent.getStringExtra("name");
        timeStamp = intent.getStringExtra("timeStamp");

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        listView = (ListView) findViewById(R.id.lv_singlefeed);
        buttonSend = (TextView) findViewById(R.id.btnSend);
        inputReply = (EditText) findViewById(R.id.inputReply);

        // 리스트헤더, 실질적으로 받아오는 아이템
        headerView = getLayoutInflater().inflate(R.layout.single_feed, null, false);
        labelName = (TextView) headerView.findViewById(R.id.name_label);
        labelTime = (TextView) headerView.findViewById(R.id.time_label);
        labelContents = (TextView) headerView.findViewById(R.id.Contents_label);
        labelProfilepic = (CircularNetworkImageView) headerView.findViewById(R.id.profilePic_label);
        feedImageViews = (LinearLayout) headerView.findViewById(R.id.feedImage1);

        labelName.setText(writer);
        labelTime.setText(timeStamp);
        listView.addHeaderView(headerView);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        updateListView();
                        swipeRefreshLayout.setRefreshing(false); // 당겨서 새로고침 숨김
                    }
                }, 1000);
            }
        });
        replyItemList = new ArrayList<ReplyItem>();
        replyListAdapter = new ReplyListAdapter(this, replyItemList);
        listView.setAdapter(replyListAdapter);

        progressDialog = ProgressDialog.show(this, "", "불러오는중...");

        fetchDataTask();
        fetchReply();
        inputReply.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                isinputReplyEmpty = s.length() > 0;
                buttonSend.setBackgroundResource(isinputReplyEmpty ? R.drawable.background_sendbtn_p : R.drawable.background_sendbtn_n);
                buttonSend.setTextColor(getResources().getColor(isinputReplyEmpty ? android.R.color.white : android.R.color.darker_gray));
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (inputReply.getText().toString().trim().length() > 0) {
                    action_Send(inputReply.getText().toString());
                    // 전송하면 텍스트창 초기화
                    inputReply.setText("");
                    if (v != null) {
                        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                } else
                    Toast.makeText(getApplicationContext(), "입력하고 전송하세요", Toast.LENGTH_LONG).show();
            }
        });

        registerForContextMenu(listView); // 콘텍스트메뉴
        headerView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                clipboard_content = labelContents.getText().toString();
                contextmenu_number = 0;
                v.showContextMenu();
                return true;
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (parent.getAdapter() instanceof HeaderViewListAdapter) {
                    if (((HeaderViewListAdapter) parent.getAdapter()).getWrappedAdapter() instanceof ListAdapter) {
                        ListAdapter listAdapter = ((HeaderViewListAdapter) parent.getAdapter()).getWrappedAdapter();
                        try {
                            ReplyItem replyItem = (ReplyItem) listAdapter.getItem(position - 1);
                            clipboard_content = replyItem.getReply();
                            contextmenu_number = replyItem.getId();
                        } catch (Exception e) {
                            Log.e(TAG, "에러" + e);
                        }
                    }
                }
                return false;
            }
        });
    }

    /**
     * 댓글 전송을 누르면 작동하는 내용
     */
    private void action_Send(final String text) {
        String tag_string_req = "req_send";

        progressDialog.setMessage("전송중...");
        showDialog();
        String URL_SEND = URLs.URL_REPLYS.replace("{FEED_ID}", String.valueOf(feed_id));
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_SEND, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "전송 응답 : " + response);
                hideDialog();
                
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean error = jsonObject.getBoolean("error");
                    if (!error) {
                        String message = jsonObject.getString("message");
                        Toast.makeText(getApplicationContext(), "전송완료", Toast.LENGTH_LONG).show();
                        
                        // 리스트뷰 새로고침
                        updateListView();
                        
                        setListViewBottom(position); // 전송할때마다 리스트뷰 아래로
                    } else {
                        // 전송 에러 발생 메시지
                        String errorMessage = jsonObject.getString("message");
                        Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "에러" + e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "전송 에러 : " + error.getMessage());
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", apikey);

                return headers;
            }
            @Override
            protected Map<String, String> getParams() {
                // 전송 받을때 받는 입력값들
                Map<String, String> params = new HashMap<String, String>();
                params.put("reply", text);

                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(stringRequest, tag_string_req);
    }

    /**
     * 리스트뷰 하단으로 간다.
     */
    private void setListViewBottom(final int position) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                View view = listView.getChildAt(position);
                if (view != null) {
                    int profileHeight = headerView.getMeasuredHeight();
                    int distance = view.getTop() + profileHeight; // 젤 위와 헤더뷰거리를 합친다.
                    listView.setSelection(distance);
                } else {
                    int profileHeight = headerView.getMeasuredHeight();
                    listView.setSelection(profileHeight);
                }
                // 2번째 리스트아이템 이후로 view가 왜 null로 되는지 모르겠음
                Log.e("체크", "확인" + view);
            }
        }, 300);
    }

    private void updateListView() {
        replyItemList = new ArrayList<ReplyItem>(); // 리스트뷰 초기화
        replyListAdapter = new ReplyListAdapter(this, replyItemList);
        listView.setAdapter(replyListAdapter);

        isUpdate = true;
        fetchDataTask();
        fetchReply();
    }

    /**
     * 선택한 피드 불러오기
     */
    private void fetchDataTask() {
        String URL_FEED = URLs.URL_FEED.replace("{FEED_ID}", String.valueOf(feed_id));
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, URL_FEED, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                VolleyLog.d(TAG, "응답 : " + response.toString());
                if (response != null) {
                    try {
                        feed_id = response.getInt("id");
                        //user_id = response.getInt("user_id");유저아이디는 인텐트로 받아옴
                        //writer = response.getString("name");글쓴이는 인텐트로 받아옴
                        image = response.getString("image");
                        feed = response.getString("feed");
                        // 프로필 이미지는 때때로 NULL
                        profile_img = response.isNull("profile_img") ? null : response.getString("profile_img");
                        //String created_at = response.getString("created_at");타임스탬프는 인텐트로 받아옴
                        //url = response.getString("url");
                        reply_count = response.getInt("reply_count");

                        if (!TextUtils.isEmpty(feed)) {
                            labelContents.setText(feed);
                            labelContents.setVisibility(View.VISIBLE);
                        } else {
                            // 피드 내용이 비었으면 화면에서 삭제
                            labelContents.setVisibility(View.GONE);
                        }
                        labelProfilepic.setImageUrl(URLs.URL_USER_PROFILE_IMAGE + profile_img, imageLoader);
                        labelProfilepic.setDefaultImageResId(R.drawable.profile_img_circle);
                        labelProfilepic.setErrorImageResId(R.drawable.profile_img_circle);
                        /*if(url != "null") {
                            labelUrl.setText(Html.fromHtml("<a href=\"" + url + "\">" + url + "</a> "));
                            // URL링크를 만들어줌
                            labelUrl.setMovementMethod(LinkMovementMethod.getInstance());
                            labelUrl.setVisibility(View.VISIBLE);
                        } else {
                            // URL이 비었으면 화면에서 화면에서 안보임
                            labelUrl.setVisibility(View.GONE);
                        }*/
                        if (!image.equals("null")) {
                            final String[] images = image.split("[|]");
                            for (int i = 0; i < images.length; i++) {
                                feedImageView = new FeedImageView(getApplicationContext());
                                feedImageView.setAdjustViewBounds(true);
                                feedImageView.setImageUrl(URLs.URL_FEED_IMAGE + images[i], imageLoader);
                                feedImageView.setScaleType(ImageView.ScaleType.FIT_XY);
                                feedImageView.setPadding(0, 0, 0, 30);
                                // 이미지 클릭시 큰이미지화면으로 넘어감
                                final int finalI = i;
                                feedImageView.setOnClickListener(new View.OnClickListener() {

                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent(ArticleActivity.this, PictureActivity.class);
                                        intent.putExtra(PictureActivity.TYPE, PictureActivity.TYPEFEED);
                                        intent.putExtra(PictureActivity.IMAGE_URL, images[finalI]);
                                        startActivity(intent);
                                    }

                                });
                                feedImageViews.addView(feedImageView);
                            }
                            feedImageViews.setVisibility(View.VISIBLE);
                            feedImageView.setResponseObserver(new FeedImageView.ResponseObserver() {
                                        @Override
                                        public void onError() {
                                        }

                                        @Override
                                        public void onSuccess() {
                                        }
                                    });
                        } else {
                            feedImageViews.setVisibility(View.GONE);
                        }
                        feedImageViews = new LinearLayout(getApplicationContext()); // 이미지를 다 그려준후, LinearLayout 변수 초기화
                        if (isUpdate == true)
                            responseUpdate(feed_id, image, feed, reply_count);

                    } catch (JSONException e) {
                        Log.e(TAG, "에러" + e);
                        Toast.makeText(getApplicationContext(), "에러: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    hideDialog();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "에러 : " + error.getMessage());
                hideDialog();
            }
        });
        AppController.getInstance().addToRequestQueue(jsonObjectRequest);
    }

    /**
     * 댓글 목록 불러오기
     */
    private void fetchReply() {
        String URL_REPLYS = URLs.URL_REPLYS.replace("{FEED_ID}", String.valueOf(feed_id));
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(URL_REPLYS, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                Log.d(TAG, response.toString());
                hideDialog();

                // json파싱
                for (int i = 0; i < response.length(); i++) {
                    try {
                        JSONObject jsonObject = response.getJSONObject(i);
                        ReplyItem replyItem = new ReplyItem();
                        int reply_id = jsonObject.getInt("id");
                        replyItem.setId(reply_id);
                        replyItem.setUser_id(jsonObject.getInt("user_id"));
                        replyItem.setName(jsonObject.getString("name"));

                        //프로필 사진은 때때로 NULL
                        profile_img = jsonObject.isNull("profile_img") ? null : jsonObject.getString("profile_img");

                        replyItem.setProfileImg(profile_img);
                        replyItem.setReply(jsonObject.getString("reply"));
                        replyItem.setTimeStamp(jsonObject.getString("created_at"));

                        // 덧글 배열에 덧글 추가
                        replyItemList.add(replyItem);
                    } catch (JSONException e) {
                        Log.e(TAG, "에러" + e);
                    }
                }
                replyListAdapter.notifyDataSetChanged();
                // isBottom이 참이면 화면 아래로 이동
                if (isBottom == true)
                    setListViewBottom(position);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Volley에러 : " + error.getMessage());
                hideDialog();
            }
        }) {
            /**
             * 헤더 요청
             * */
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", apikey);
                return headers;
            }
        };
        AppController.getInstance().addToRequestQueue(jsonArrayRequest);
    }

    /**
     * 댓글을 길게 클릭하면 콘텍스트 메뉴가 뜸
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("작업선택");
        if (contextmenu_number == 0) {
            menu.add(Menu.NONE, 1, Menu.NONE, "내용 복사");
        }
        if (contextmenu_number != 0) {
            // user_id를 기준으로 메뉴노출이 달라짐
            int user_id = replyItemList.get(((AdapterView.AdapterContextMenuInfo) menuInfo).position - 1).getUser_id();
            menu.add(Menu.NONE, 1, Menu.NONE, "내용 복사");
            if (user_id == myUser_id) {
                menu.add(Menu.NONE, 2, Menu.NONE, "댓글 수정");
                menu.add(Menu.NONE, 3, Menu.NONE, "댓글 삭제");
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int id = item.getItemId();
        switch (id) {
            case 1:
                copyText();
                return true;
            case 2:
                Modify_Reply();
                return true;
            case 3:
                Action_ReplyDelete();
                return true;
        }
        return false;
    }

    private void copyText() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setText(clipboard_content);
        Toast.makeText(getApplicationContext(), "클립보드에 복사되었습니다!", Toast.LENGTH_SHORT).show();
    }

    private void Modify_Reply() {
        Intent intent = new Intent(ArticleActivity.this, ReplyModifyActivity.class);
        ReplyItem item = replyItemList.get(info.position - 1); // 헤더가 있기때문에 포지션에서 -1을 해준다
        int reply_id = item.getId();
        String reply = item.getReply();
        intent.putExtra("reply_id", reply_id);
        intent.putExtra("replymsg", reply);
        startActivityForResult(intent, REQUEST_CODE);
    }

    // 글 삭제을 눌렀을때 작동하는 내용
    private void Action_ReplyDelete() {
        ReplyItem item = replyItemList.get(info.position - 1); // 헤더가 있기때문에 포지션에서 -1을 해준다
        int reply_id = item.getId();

        String tag_string_req = "req_delete";

        progressDialog.setMessage("요청중 ...");
        showDialog();
        String URL_REPLY = URLs.URL_REPLY.replace("{REPLY_ID}", String.valueOf(reply_id));
        StringRequest strReq = new StringRequest(Request.Method.DELETE, URL_REPLY, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "삭제 응답: " + response.toString());
                hideDialog();
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean error = jsonObject.getBoolean("error");
                    if (!error) {
                        String message = jsonObject.getString("message");

                        Toast.makeText(getApplicationContext(), "삭제완료", Toast.LENGTH_LONG).show();
                        replyItemList.remove(info.position - 1); // 헤더가 있기때문에 포지션에서 -1을 해준다
                        replyListAdapter.notifyDataSetChanged();
                    } else {
                        // 삭제에서 에러 발생. 에러 내용
                        String errorMsg = jsonObject.getString("message");
                        Toast.makeText(getApplicationContext(), "삭제할수 없습니다.", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "에러" + e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "전송 에러: " + error.getMessage());
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", apikey);
                return headers;
            }
        };
        // 큐를 요청하는 요청을 추가
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 조건을 위해 xml레이아웃을 사용하지 않고 코드로 옵션메뉴를 구성함
        if (user_id == myUser_id) {
            menu.add(Menu.NONE, 1, Menu.NONE, "수정하기");
        }
        menu.add(Menu.NONE, 2, Menu.NONE, "삭제하기");

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
                return true;
            case 1:
                Intent intent = new Intent(ArticleActivity.this, ModifyActivity.class);
                intent.putExtra("feed_id", feed_id);
                intent.putExtra("feedmsg", feed);
                intent.putExtra("image", image);
                startActivityForResult(intent, FEEDINFO_CODE);
                return true;
            case 2:
                Action_FeedDelete();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 글 삭제
     */
    private void Action_FeedDelete() {
        String tag_string_req = "req_delete";

        progressDialog.setMessage("요청중 ...");
        showDialog();
        String URL_FEED = URLs.URL_FEED.replace("{FEED_ID}", String.valueOf(feed_id));
        StringRequest strReq = new StringRequest(Request.Method.DELETE, URL_FEED, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "삭제 응답: " + response);
                hideDialog();

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean error = jsonObject.getBoolean("error");
                    if (!error) {
                        String message = jsonObject.getString("message");

                        Toast.makeText(getApplicationContext(), "삭제완료", Toast.LENGTH_LONG).show();

                        Intent intent = new Intent(ArticleActivity.this, MainActivity.class);
                        // 모든 이전 activity 초기화
                        intent.setFlags(Intent.FLAG_ACTIVITY_TASK_ON_HOME | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    } else {
                        // 삭제에서 에러 발생. 에러 내용
                        String errorMsg = jsonObject.getString("message");
                        Toast.makeText(getApplicationContext(),
                                "삭제할수 없습니다.", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "JSON에러" + e);
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "전송 에러: " + error.getMessage());
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", apikey);
                return headers;
            }
        };
        // 큐를 요청하는 요청을 추가
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    /**
     * Activity Result완료 후 인텐트 불러옴
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) { // 댓글 수정이 완료하면 일어나는 동작
            String modify_reply = data.getStringExtra("modify_reply");

            ReplyItem replyItem = replyItemList.get(info.position - 1); // 헤더가 있기때문에 포지션에서 -1을 해준다
            replyItem.setReply(modify_reply);
            replyItem.getReply();
            replyItemList.set(info.position - 1, replyItem); // 헤더가 있기때문에 포지션에서 -1을 해준다
            replyListAdapter.notifyDataSetChanged();
        } else if (requestCode == FEEDINFO_CODE && resultCode == RESULT_OK) { // 피드 수정이 완료하면 일어나는 동작
            isUpdate = true;
            onCreate(new Bundle());
        }
    }

    private void responseUpdate(int feed_id, String image, String feed, int reply_count) {
        Intent intent = new Intent(ArticleActivity.this, Tab1Fragment.class);

        intent.putExtra("feed_id", feed_id);
        intent.putExtra("feed", feed);
        intent.putExtra("image", image);
        intent.putExtra("reply_count", String.valueOf(reply_count));
        setResult(FEEDINFO_CODE, intent); // 인텐트를 반환값으로 돌려준다
    }

    private void showDialog() {
        if (!progressDialog.isShowing())
            progressDialog.show();
    }

    private void hideDialog() {
        if (progressDialog.isShowing())
            progressDialog.dismiss();
    }
}