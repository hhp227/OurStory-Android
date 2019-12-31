package com.hhp227.application;

import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.hhp227.application.app.AppController;
import com.hhp227.application.app.Config;
import com.hhp227.application.app.URLs;
import com.hhp227.application.chat.Message;
import com.hhp227.application.chat.MessagesListAdapter;
import com.hhp227.application.fcm.NotificationUtils;
import com.hhp227.application.ui.navigationdrawer.DrawerArrowDrawable;
import com.hhp227.application.user.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends Activity {
    // 로그캣 태그
    private static final String TAG = "채팅화면";
    private String chatRoomId, apikey;
    private int myUser_id, offSet, previousMessageCnt;
    private TextView btnSend;
    private EditText inputMsg;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private List<Message> listMessages;
    private ListView listViewMessages;
    private MessagesListAdapter messagesListAdapter;

    int CurrentScrollState;
    private boolean HasRequestedMore;   // 데이터 불러올때 중복안되게 하기위한 변수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        ActionBar actionBar = getActionBar();

        // 액션바 뒤로가기
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(new DrawerArrowDrawable(this) {
            @Override
            public boolean isLayoutRtl() {
                return false;
            }
        });

        btnSend = (TextView) findViewById(R.id.btnSend);
        inputMsg = (EditText) findViewById(R.id.inputMsg);
        listViewMessages = (ListView) findViewById(R.id.list_view_messages);

        listMessages = new ArrayList<>();

        // 처음 offset은 0이다, json파싱이 되는동안 업데이트 될것
        offSet = 0;

        User user = AppController.getInstance().getPrefManager().getUser();
        apikey = user.getApi_key();
        myUser_id = user.getId();

        // 리스트뷰 스크롤 리스너 등록
        listViewMessages.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                Log.d(TAG, "onScrollStateChanged:" + scrollState);
                CurrentScrollState = scrollState;
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                boolean loadMore = firstVisibleItem == 0;
                if (!HasRequestedMore && loadMore && CurrentScrollState != SCROLL_STATE_IDLE) {
                    if (totalItemCount == previousMessageCnt)
                        return;
                    offSet = totalItemCount;
                    HasRequestedMore = true;
                    fetchChatThread();
                }
            }
        });

        messagesListAdapter = new MessagesListAdapter(this, listMessages, myUser_id);
        listViewMessages.setAdapter(messagesListAdapter);

        chatRoomId = "1"; // 현재는 메인 채팅방이 1로 지정

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Config.PUSH_NOTIFICATION)) {
                    // new push message is received
                    handlePushNotification(intent);
                }
            }
        };

        inputMsg.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnSend.setBackgroundResource(s.length() > 0 ? R.drawable.background_sendbtn_p : R.drawable.background_sendbtn_n);
                btnSend.setTextColor(getResources().getColor(s.length() > 0 ? android.R.color.white : android.R.color.darker_gray));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (inputMsg.getText().toString().trim().length() > 0) {
                    sendMessage();
                    // 전송하면 텍스트창 리셋
                    inputMsg.setText("");
                } else {
                    Toast.makeText(getApplicationContext(), "입력하고 전송하세요", Toast.LENGTH_LONG).show();
                }
            }
        });

        fetchChatThread();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // registering the receiver for new notification
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver, new IntentFilter(Config.PUSH_NOTIFICATION));

        NotificationUtils.clearNotifications();
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }

    /**
     * Handling new push message, will add the message to
     * recycler view and scroll it to bottom
     * */
    private void handlePushNotification(Intent intent) {
        Message message = (Message) intent.getSerializableExtra("message");
        String chatRoomId = intent.getStringExtra("chat_room_id");

        if (message != null && chatRoomId != null) {
            listMessages.add(message);
            messagesListAdapter.notifyDataSetChanged();
            listViewMessages.setSelection(listViewMessages.getCount());
        }
    }

    private void sendMessage() {
        final String message = this.inputMsg.getText().toString().trim();
        String endPoint = URLs.URL_CHAT_SEND.replace("{CHATROOM_ID}", chatRoomId);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, endPoint, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG, "response: " + response);
                try {
                    JSONObject obj = new JSONObject(response);

                    if (obj.getBoolean("error") == false) {
                        JSONObject commentObj = obj.getJSONObject("message");

                        int commentId = commentObj.getInt("message_id");
                        String commentText = commentObj.getString("message");
                        String createdAt = commentObj.getString("created_at");

                        JSONObject userObj = obj.getJSONObject("user");
                        int userId = userObj.getInt("user_id");
                        String userName = userObj.getString("name");
                        User user = new User(userId, userName, null, null, null, null);

                        Message message = new Message();
                        message.setId(commentId);
                        message.setMessage(commentText);
                        message.setTime(createdAt);
                        message.setUser(user);

                        listMessages.add(message);

                        messagesListAdapter.notifyDataSetChanged();
                        listViewMessages.setSelection(listViewMessages.getCount());
                    } else {
                        Toast.makeText(getApplicationContext(), "에러 : " + obj.getString("message"), Toast.LENGTH_LONG).show();
                    }

                } catch (JSONException e) {
                    Log.e(TAG, "json parsing error: " + e.getMessage());
                    Toast.makeText(getApplicationContext(), "json parse error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                Log.e(TAG, "Volley error: " + error.getMessage() + ", code: " + networkResponse);
                Toast.makeText(getApplicationContext(), "Volley error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                inputMsg.setText(message);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", apikey);
                return headers;
            }
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("message", message);

                Log.e(TAG, "Params: " + params.toString());

                return params;
            }
        };
        // disabling retry policy so that it won't make
        // multiple http calls
        int socketTimeout = 0;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        stringRequest.setRetryPolicy(policy);

        //Adding request to request queue
        AppController.getInstance().addToRequestQueue(stringRequest);
    }

    /**
     * 채팅방 메시지 인출
     * */
    private void fetchChatThread() {
        String endPoint = URLs.URL_CHAT_THREAD.replace("{CHATROOM_ID}", chatRoomId);
        Log.e(TAG, "endPoint: " + endPoint);

        StringRequest strReq = new StringRequest(Request.Method.GET, endPoint + offSet, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.e(TAG, "response: " + response);
                try {
                    JSONObject obj = new JSONObject(response);
                    // check for error
                    if (obj.getBoolean("error") == false) {
                        JSONArray commentsObj = obj.getJSONArray("messages");
                        for (int i = 0; i < commentsObj.length(); i++) {
                            JSONObject commentObj = (JSONObject) commentsObj.get(i);
                            int commentId = commentObj.getInt("message_id");
                            String commentText = commentObj.getString("message");
                            String createdAt = commentObj.getString("created_at");

                            JSONObject userObj = commentObj.getJSONObject("user");
                            int userId = userObj.getInt("user_id");
                            String userName = userObj.getString("username");
                            String profile_Img = userObj.getString("profile_img");
                            User user = new User(userId, userName, null, null, profile_Img, null);

                            Message message = new Message();
                            message.setId(commentId);
                            message.setMessage(commentText);
                            message.setTime(createdAt);
                            message.setUser(user);

                            listMessages.add(0, message);
                        }
                        messagesListAdapter.notifyDataSetChanged();
                        onLoadMoreItems(commentsObj.length());
                    } else {
                        Toast.makeText(getApplicationContext(), "" + obj.getJSONObject("error").getString("message"), Toast.LENGTH_LONG).show();
                    }

                } catch (JSONException e) {
                    Log.e(TAG, "json parsing error: " + e.getMessage());
                    Toast.makeText(getApplicationContext(), "json parse error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                Log.e(TAG, "Volley error: " + error.getMessage() + ", code: " + networkResponse);
                Toast.makeText(getApplicationContext(), "Volley error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        //Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq);
    }

    private void onLoadMoreItems(int AddCount) {
        if (HasRequestedMore == true) {
            int firstVisPos = listViewMessages.getFirstVisiblePosition();
            View firstVisView = listViewMessages.getChildAt(0);
            int top = firstVisView != null ? firstVisView.getTop() : 0;
            listViewMessages.setSelectionFromTop(firstVisPos + AddCount, top);
        } else
            listViewMessages.setSelection(listViewMessages.getCount());
        previousMessageCnt = listMessages.size() - AddCount;
        HasRequestedMore = false;
    }

    /**
     * 액션바에 뒤로가기 버튼을 누르면 나타나는 동작
     */

    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);// 인텐트를 반환값으로 돌려준다
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
