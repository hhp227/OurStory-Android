package com.hhp227.application;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.hhp227.application.app.AppController;
import com.hhp227.application.app.URLs;
import com.hhp227.application.user.User;

public class ReplyModifyActivity extends Activity {
    private static final String TAG = "수정화면";

    private static int reply_id;
    private static String replymsg;
    private EditText inputreply;
    private ProgressDialog progressDialog;
    private ListView listView;
    private View headerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reply_modify);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);
        listView = (ListView) findViewById(R.id.lv_write);
        headerView = getLayoutInflater().inflate(R.layout.input_text, null, false);
        inputreply = (EditText) headerView.findViewById(R.id.inputcontents);
        listView.addHeaderView(headerView);

        listView.setAdapter(null);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        Intent intent = getIntent();
        reply_id = intent.getIntExtra("reply_id", reply_id);
        replymsg = intent.getStringExtra("replymsg");

        inputreply.setText(replymsg);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.write, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch(id) {
            case android.R.id.home :
                finish();
                return true;
            case R.id.action_send :
                String text = inputreply.getText().toString().trim();

                if(!text.isEmpty()) {
                    Action_Send(text);
                } else {
                    Toast.makeText(getApplicationContext(), "내용을 입력하세요.", Toast.LENGTH_LONG).show();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 전송을 눌렀을때 작동하는 내용
     */
    private void Action_Send(final String text) {
        String tag_string_req = "req_send";
        progressDialog.setMessage("전송중...");
        showProgressDialog();

        String URL_REPLY = URLs.URL_REPLY.replace("{REPLY_ID}", String.valueOf(reply_id));
        StringRequest stringRequest = new StringRequest(Request.Method.PUT, URL_REPLY, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG, "전송 응답 : " + response);
                hideProgressDialog();
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean error = jsonObject.getBoolean("error");
                    if(!error) {
                        String message = jsonObject.getString("message");
                        
                        modifyData(text); // 클라이언트측에 처리해줘야할 메소드
                        Toast.makeText(getApplicationContext(), "수정완료", Toast.LENGTH_LONG).show();
                    } else {
                        String errorMsg = jsonObject.getString("message");
                        Toast.makeText(getApplicationContext(), "수정할 수 없습니다.", Toast.LENGTH_LONG).show();
                    }
                } catch(JSONException e) {
                    Log.e(TAG, "JSON에러" + e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "전송 에러 : " + error.getMessage());
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                hideProgressDialog();
            }
        }) {
            User user = AppController.getInstance().getPrefManager().getUser();

            String apikey = user.getApi_key();
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", apikey);
                return headers;
            }
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("reply", text);
                params.put("status", "0");

                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(stringRequest, tag_string_req);
    }

    /**
     * 전송이 완료되면 이전 Activity에 값을 반환
     */
    private void modifyData(String reply) {
        // 입력 자판 숨기기
        View view = this.getCurrentFocus();
        if(view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra("modify_reply", reply);
        setResult(RESULT_OK, intent);
        finish();
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