package com.hhp227.application;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.hhp227.application.app.AppController;
import com.hhp227.application.app.URLs;
import com.hhp227.application.user.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends Activity {
    private Button login;
    private TextView register;
    private EditText inputEmail, inputPassword;
    private static final String TAG = "로그인화면";
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 액션바 없음
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        login = (Button) findViewById(R.id.bLogin);
        inputEmail = (EditText) findViewById(R.id.etEmail);
        inputPassword = (EditText) findViewById(R.id.etPassword);
        register = (TextView) findViewById(R.id.register);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        // 사용자가 이미 로그인되있는지 아닌지 확인
        if (AppController.getInstance().getPrefManager().getUser() != null) {
            startActivity(new Intent(this, SplashActivity.class));
            finish();
        }

        // 로그인 버튼 클릭 이벤트
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = inputEmail.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();

                // 폼에 데이터가 비어있는지 확인
                if (!email.isEmpty() && !password.isEmpty()) {
                    // 로그인 유저
                    checkLogin(email, password);
                } else {
                    Toast.makeText(getApplicationContext(),
                            "이메일 또는 비밀번호가 잘못되었습니다.", Toast.LENGTH_LONG)
                            .show();
                }
            }
        });

        // 가입하기로 이동
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }
    /**
     * mysql db에 정보를 얻어내는 함수
     * */
    private void checkLogin(final String email, final String password) {
        // 태그는 요청을 취소할때 사용
        String tag_string_req = "req_login";

        progressDialog.setMessage("로그인중...");
        showDialog();
        StringRequest strReq = new StringRequest(Request.Method.POST, URLs.URL_LOGIN, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "로그인 응답: " + response);
                hideDialog();

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean error = jsonObject.getBoolean("error");

                    if (!error) {
                        //JSONObject user = jObj.getJSONObject("user");
                        int user_id = jsonObject.getInt("id");
                        String name = jsonObject.getString("name");
                        String email = jsonObject.getString("email");
                        String api_key = jsonObject.getString("api_key");
                        String profile_img = jsonObject.getString("profile_img");
                        String created_at = jsonObject.getString("created_at");

                        User user = new User(user_id, name, email, api_key, profile_img, created_at);

                        AppController.getInstance().getPrefManager().storeUser(user);

                        // 메인 엑티비티 실행
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        String errorMsg = jsonObject.getString("error_msg");
                        Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // JSON 에러
                    Log.e(TAG, "JSON에러" + e);
                    Toast.makeText(getApplicationContext(), "로그인 실패", Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "로그인 에러: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("email", email);
                params.put("password", password);

                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
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
