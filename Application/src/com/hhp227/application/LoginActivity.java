package com.hhp227.application;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTag;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.hhp227.application.app.AppController;
import com.hhp227.application.app.URLs;
import com.hhp227.application.user.User;


public class LoginActivity extends Activity {
    private Button login;
    private EditText inputId, inputPassword;
    private static final String TAG = "로그인화면";
    private ProgressDialog progressDialog;
    private Source source;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 액션바 없음
    	this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        login = (Button) findViewById(R.id.bLogin);
        inputId = (EditText) findViewById(R.id.etId);
        inputPassword = (EditText) findViewById(R.id.etPassword);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        // 사용자가 이미 로그인되있는지 아닌지 확인
        if(AppController.getInstance().getPrefManager().getUser() != null) {
            startActivity(new Intent(this, SplashActivity.class));
            finish();
        }

        // 로그인 버튼 클릭 이벤트
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String id = inputId.getText().toString();
                final String password = inputPassword.getText().toString();

                if(!id.isEmpty() && !password.isEmpty()) {
                    String tag_string_req = "req_knu_login";
                    progressDialog.setMessage("로그인중...");
                    showProgressDialog();

                    String URL_LOGIN_KNU = "http://yes.knu.ac.kr/comm/comm/support/login/login.action";
                    StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_LOGIN_KNU, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.d(TAG, "로그인 응답: " + response);
                            loginCheck(response, password);
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(TAG, "로그인 에러: " + error.getMessage());
                            Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                            hideProgressDialog();
                        }
                    }) {
                        @Override
                        protected Map<String, String> getParams() {
                            Map<String, String> params = new HashMap<String, String>();
                            params.put("user.usr_id", id);
                            params.put("user.passwd", password);

                            return params;
                        }
                    };
                    AppController.getInstance().addToRequestQueue(stringRequest, tag_string_req);
                } else {
                    Toast.makeText(getApplicationContext(), "아이디 또는 비밀번호가 잘못되었습니다.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void loginCheck(String response, String password) {
        String info = null;
        String name = null, id = null, student_number = null;
        source = new Source(response);
        List<StartTag> divtags = source.getAllStartTags(HTMLElementName.DIV); // DIV 타입의 모든 태그들을 불러온다.
        for(int i = 0; i < divtags.size(); i++) {
            if(divtags.get(i).toString().equals("<div class=\"box01\">")) {
                info = source.getAllElements(HTMLElementName.DIV).get(i).getTextExtractor().toString();
                String[] array = info.split("[/]|[(]|[)]");

                name = array[0].trim();
                id = array[1].trim();
                student_number = array[2].trim();
                break;
            }
        }
        if(info != null) {
            registerUser(name, id, password, student_number);
        } else {
            Toast.makeText(getApplicationContext(), "통합정보시스템 로그인 실패", Toast.LENGTH_LONG).show();
            hideProgressDialog();
        }
    }

    private void registerUser(final String name, final String id, final String password, final String student_number) {
        String tag_string_req = "req_register";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URLs.URL_REGISTER, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    loginUser(id, password);
                } catch(JSONException e) {
                    Log.e(TAG, "에러" + e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "전송 에러 : " + error.getMessage());
                hideProgressDialog();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("name", "익명".concat(String.valueOf(new Random().nextInt(10000))));
                params.put("knu_id", id);
                params.put("password", password);
                params.put("student_number", student_number);
                params.put("real_name", name);

                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(stringRequest, tag_string_req);
    }

    private void loginUser(final String id, final String password) {
        String tag_string_req = "req_login";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URLs.URL_LOGIN, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "로그인 응답: " + response);
                hideProgressDialog();

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean error = jsonObject.getBoolean("error");

                    if(!error) {
                        int user_id = jsonObject.getInt("id");
                        String student_number = jsonObject.getString("student_number");
                        String name = jsonObject.getString("name");
                        String knu_id = jsonObject.getString("knu_id");
                        String api_key = jsonObject.getString("api_key");
                        String profile_img = jsonObject.getString("profile_img");
                        String created_at = jsonObject.getString("created_at");

                        User user = new User(user_id, student_number, name, knu_id, api_key, profile_img, created_at, password);

                        AppController.getInstance().getPrefManager().storeUser(user);

                        // 메인 엑티비티 실행
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        String errorMsg = jsonObject.getString("error_msg");
                        Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch(JSONException e) {
                    // JSON 에러
                    Log.e(TAG, "JSON에러" + e);
                    Toast.makeText(getApplicationContext(), "로그인 실패", Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, error.getMessage());
                hideProgressDialog();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("knu_id", id);
                params.put("password", password);

                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(stringRequest, tag_string_req);
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
