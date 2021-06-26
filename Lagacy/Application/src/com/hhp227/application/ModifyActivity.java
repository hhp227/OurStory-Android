package com.hhp227.application;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.hhp227.application.app.AppController;
import com.hhp227.application.app.URLs;
import com.hhp227.application.ui.navigationdrawer.DrawerArrowDrawable;
import com.hhp227.application.user.User;
import com.hhp227.application.volley.util.VolleyMultipartRequest;
import com.hhp227.application.write.WriteListAdapter;
import com.hhp227.application.write.Writeitem;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModifyActivity extends Activity {
    private static final String TAG = "수정화면";

    // 콘텍스트메뉴 노출 요청값
    private int contextMenuRequest;
    private AdapterView.AdapterContextMenuInfo info;
    private static int feed_id;
    private static String feedmsg, image, apikey;
    private EditText inputfeed;
    private ListView listView;
    private View headerView;
    private LinearLayout buttonImage, buttonVideo;
    private ProgressDialog progressDialog;
    private WriteListAdapter listAdapter;
    private List<Writeitem> contents;
    private List<String> fileNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setHomeAsUpIndicator(new DrawerArrowDrawable(this) {
            @Override
            public boolean isLayoutRtl() {
                return false;
            }
        });
        listView = (ListView) findViewById(R.id.lv_write);
        headerView = getLayoutInflater().inflate(R.layout.input_text, null, false);
        inputfeed = (EditText) headerView.findViewById(R.id.inputcontents);
        buttonImage = (LinearLayout) findViewById(R.id.ll_image);
        buttonVideo = (LinearLayout) findViewById(R.id.ll_video);

        listView.addHeaderView(headerView);

        User user = AppController.getInstance().getPrefManager().getUser();

        apikey = user.getApi_key();

        contents = new ArrayList<>();

        listAdapter = new WriteListAdapter(getApplicationContext(), R.layout.input_contents, contents);

        listView.setAdapter(listAdapter);

        registerForContextMenu(listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                contextMenuRequest = 1;
                view.showContextMenu();
            }
        });

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        buttonImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contextMenuRequest = 2;
                if (contents.size() < WriteActivity.LIMIT) {
                    registerForContextMenu(v);
                    openContextMenu(v);
                    unregisterForContextMenu(v);
                } else {
                    Toast.makeText(getBaseContext(), "첨부 제한 " + String.valueOf(WriteActivity.LIMIT) + "개", Toast.LENGTH_LONG).show();
                }
            }
        });

        buttonVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contextMenuRequest = 3;
                registerForContextMenu(v);
                openContextMenu(v);
                unregisterForContextMenu(v);
            }
        });

        Intent intent = getIntent();
        feed_id = intent.getIntExtra("feed_id", feed_id);
        feedmsg = intent.getStringExtra("feedmsg");
        image = intent.getStringExtra("image");

        inputfeed.setText(feedmsg);

        if (!image.equals("null")) {
            String[] images = image.split("[|]");
            for (int i = 0; i < images.length; i++) {
                Writeitem writeitem = new Writeitem();
                writeitem.setImage(images[i]);
                contents.add(writeitem);
            }
        }
    }

    /**
     * 전송을 눌렀을때 작동하는 내용
     */
    private void Action_Send(final String text, final List<String> files) {
        String tag_string_req = "req_send";
        progressDialog.setMessage("전송중...");
        showDialog();

        String URL_FEED = URLs.URL_FEED.replace("{FEED_ID}", String.valueOf(feed_id));
        StringRequest stringRequest = new StringRequest(Request.Method.PUT, URL_FEED, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG, "전송 응답 : " + response.toString());
                hideDialog();

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean error = jsonObject.getBoolean("error");
                    if (!error) {
                        String message = jsonObject.getString("message");

                        Toast.makeText(getApplicationContext(), "수정완료", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(ModifyActivity.this, ArticleActivity.class);
                        setResult(RESULT_OK, intent);
                        finish();
                    } else {
                        String errorMsg = jsonObject.getString("message");
                        Toast.makeText(getApplicationContext(), "수정할 수 없습니다.", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "JSON에러" + e);
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
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", apikey);
                return headers;
            }
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("feed", text);
                if (files != null)
                    params.put("image", convertToString(files));
                params.put("status", "0");

                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(stringRequest, tag_string_req);
    }

    /**
     * 여러개 전송을 할 경우 처리해야할 부분
     */
    private String convertToString(List<String> files) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < files.size(); i++) {
            result.append(files.get(i));
            if (i != files.size() - 1)
                result.append("|");
        }
        return result.toString();
    }

    public byte[] getFileDataFromDrawable(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 80, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == WriteActivity.CAMERA_PICK_IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {

            Uri fileUri = data.getData();
            try {
                // uri로부터 비트맵 얻기
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), fileUri);

                Writeitem writeitem = new Writeitem();
                writeitem.setBitmap(bitmap);
                writeitem.setFileUri(fileUri);

                contents.add(writeitem);
                listAdapter.notifyDataSetChanged();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (requestCode == WriteActivity.CAMERA_CAPTURE_IMAGE_REQUEST_CODE && resultCode == RESULT_OK) {
            // 이미지 캡쳐 성공하면
            // 미리보기
            //launchPreview(true);
        } else if (requestCode == WriteActivity.CAMERA_CAPTURE_VIDEO_REQUEST_CODE && resultCode == RESULT_OK) {
            // 비디오 녹화 성공
            // 미리보기
            //launchPreview(false);
        }
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
        switch (id) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_send:
                String text = inputfeed.getText().toString().trim();
                if (!text.isEmpty() || contents.size() > 0) {
                    if (contents.size() > 0) {
                        fileNames = new ArrayList<>();
                        progressDialog.setMessage("파일 전송중...");
                        showDialog();
                        for (int i = 0; i < contents.size(); i++) {
                            if (contents.get(i).getImage() != null) {
                                fileNames.add(contents.get(i).getImage());
                                imgIncludeSend(text);
                            } else {
                                Bitmap bitmap = contents.get(i).getBitmap();
                                uploadBitmap(text, bitmap);
                            }
                        }
                    } else {
                        fileNames = null;
                        Action_Send(text, fileNames); // 일반 전송
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "내용을 입력하세요.", Toast.LENGTH_LONG).show();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        switch (contextMenuRequest) {
            case 1:
                menu.setHeaderTitle("작업 선택");
                menu.add(Menu.NONE, 1, Menu.NONE, "삭제");
                break;
            case 2:
                menu.setHeaderTitle("이미지 선택");
                menu.add(Menu.NONE, 2, Menu.NONE, "앨범");
                menu.add(Menu.NONE, 3, Menu.NONE, "카메라");
                break;
            case 3:
                menu.setHeaderTitle("작업 선택");
                menu.add(Menu.NONE, 4, Menu.NONE, "동영상");
                break;
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case 1:
                removeListItem();
                return true;
            case 2:
                pickImage();
                return true;
            case 3:
                captureImage();
                return true;
            case 4:
                recordVideo();
                return true;
        }
        return false;
    }

    private void removeListItem() {
        contents.remove(info.position - 1);
        listAdapter.notifyDataSetChanged();
    }

    /**
     * 앨범 호출
     */
    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
        intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, WriteActivity.CAMERA_PICK_IMAGE_REQUEST_CODE);
    }

    /**
     * 카메라 호출
     */
    private void captureImage() {
        /*Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);*/
    }

    /**
     * 동영상 촬영 호출
     */
    private void recordVideo() {
        Toast.makeText(getBaseContext(), "비디오 선택", Toast.LENGTH_LONG).show();
    }

    private void uploadBitmap(final String text, final Bitmap bitmap) {
        VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, URLs.URL_FEED_IMAGE_UPLOAD, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                try {
                    JSONObject obj = new JSONObject(new String(response.data));

                    fileNames.add(obj.getString("image"));

                    imgIncludeSend(text);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "응답 에러" + error.getMessage(), Toast.LENGTH_SHORT).show();
                hideDialog();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", apikey);
                return headers;
            }
            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                long imagename = System.currentTimeMillis();
                params.put("image", new DataPart(imagename  + ".jpg", getFileDataFromDrawable(bitmap)));
                return params;
            }
        };
        Volley.newRequestQueue(this).add(volleyMultipartRequest);
    }

    private void imgIncludeSend(String text) {
        if (fileNames.size() == contents.size())
            // 파일전송이 완료하면, 응답값을 받아 글쓰기를 완료한다
            Action_Send(text, fileNames);
    }

    public void onResume() {
        super.onResume();
        listAdapter.notifyDataSetChanged();
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
