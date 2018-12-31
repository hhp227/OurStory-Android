package com.hhp227.application;

import static com.hhp227.application.app.URLs.URL_FEEDS;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
import com.hhp227.application.user.User;
import com.hhp227.application.volly.util.VolleyMultipartRequest;
import com.hhp227.application.write.WriteListAdapter;
import com.hhp227.application.write.Writeitem;

public class WriteActivity extends Activity {
    // 파일 첨부 제한수
    public static final int LIMIT = 3;
    public static final String IMAGE_DIRECTORY_NAME = "안드로이드 이미지 업로드";
    // 인텐트값
    public static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    public static final int CAMERA_PICK_IMAGE_REQUEST_CODE = 200;
    public static final int CAMERA_CAPTURE_VIDEO_REQUEST_CODE = 300;
    private static final String TAG = "쓰기화면";
    // 콘텍스트메뉴 노출 요청값
    private int contextMenuRequest;
    private AdapterView.AdapterContextMenuInfo info;

    private ListView listView;
    private EditText inputfeed;
    private ProgressDialog progressDialog;
    private View headerView;
    private LinearLayout buttonImage, buttonVideo;
    private WriteListAdapter listAdapter;
    private List<Writeitem> contents;
    private List<String> fileNames;
    private String apikey;
    private boolean isTrue; // 전송버튼 여러번 누를시 여러번 업로드를 제어할 변수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);

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

        registerForContextMenu(listView); // 콘텍스트메뉴
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
                if(contents.size() < LIMIT) {
                    registerForContextMenu(v);
                    openContextMenu(v);
                    unregisterForContextMenu(v);
                } else {
                    Toast.makeText(getBaseContext(), "첨부 제한 " + String.valueOf(LIMIT) + "개", Toast.LENGTH_LONG).show();
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

    }

    /**
     * 전송을 눌렀을때 작동하는 내용
     * @param text
     */
    private void Action_Send(final String text, final List<String> files) {
        String tag_string_req = "req_send";

        progressDialog.setMessage("전송중...");
        showProgressDialog();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_FEEDS, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG, "전송 응답 : " + response);
                hideProgressDialog();

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean error = jsonObject.getBoolean("error");
                    if(!error) {
                        String message = jsonObject.getString("message");
                        Toast.makeText(getApplicationContext(), "전송완료", Toast.LENGTH_LONG).show();

                        Intent intent = new Intent(WriteActivity.this, MainActivity.class);
                        // 모든 이전 Activity 초기화
                        intent.setFlags(Intent.FLAG_ACTIVITY_TASK_ON_HOME | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    } else {
                        String errorMsg = jsonObject.getString("message");
                        Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch(JSONException e) {
                    Log.e(TAG, "에러" + e);
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
                if(files != null)
                    params.put("image", convertToString(files));

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
        for(int i = 0; i < files.size(); i++) {
            result.append(files.get(i));
            if(i != files.size() - 1)
                result.append("|");
        }
        return result.toString();
    }

    /*
     * The method is taking Bitmap as an argument
     * then it will return the byte[] array for the given bitmap
     * and we will send this array to the server
     * here we are using PNG Compression with 80% quality
     * you can give quality between 0 to 100
     * 0 means worse quality
     * 100 means best quality
     * */
    public byte[] getFileDataFromDrawable(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 80, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    /*public Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }*/

    /*private static File getOutputMediaFile(int type) {

        // 외부 sdcard 구역
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), IMAGE_DIRECTORY_NAME);

        // 저장디렉터리가 존재하지 않는다면 저장 디텍터리 생성
        if(!mediaStorageDir.exists()) {
            if(!mediaStorageDir.mkdirs()) {
                Log.d(TAG, "생성 실패 " + IMAGE_DIRECTORY_NAME + " directory");
                return null;
            }
        }

        // 미디어 파일이름 생성
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File mediaFile;

        if(type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }*/

    /*@Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable("file_uri", fileUri);
    }*/

    /*@Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // 파일 URL 얻기
        fileUri = savedInstanceState.getParcelable("file_uri");
    }*/
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == CAMERA_PICK_IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {

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
        } else if(requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE && resultCode == RESULT_OK) {
            // 이미지 캡쳐 성공하면
            // 미리보기
            //launchPreview(true);
        } else if(requestCode == CAMERA_CAPTURE_VIDEO_REQUEST_CODE && resultCode == RESULT_OK) {
            // 비디오 녹화 성공
            // 미리보기
            //launchPreview(false);
        }
    }

    /*public static boolean copyFile(File srcFile, File destFile) {
        boolean result = false;
        try {
            InputStream in = new FileInputStream(srcFile);
            try {
                result = copyToFile(in, destFile);
            } finally {
                in.close();
            }
        } catch(IOException e) {
            result = false;
        }
        return result;
    }*/

    /*private static boolean copyToFile(InputStream inputStream, File destFile) {
        try {
            OutputStream out = new FileOutputStream(destFile);
            try {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while((bytesRead = inputStream.read(buffer)) >= 0) {
                    out.write(buffer, 0, bytesRead);
                }
            } finally {
                out.close();
            }
            return true;
        } catch(IOException e) {
            return false;
        }
    }*/

    /**
     * Crop된 이미지가 저장될 파일을 만든다.
     */
    private Uri createSaveCropFile(){
        Uri uri;
        String url = "tmp_" + String.valueOf(System.currentTimeMillis()) + ".jpg";
        uri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), url));
        return uri;
    }

    /**
     * 선택된 uri의 사진 Path를 가져온다.
     * uri 가 null 경우 마지막에 저장된 사진을 가져온다.
     */
    private File getImageFile(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        if(uri == null) {
            uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }

        Cursor mCursor = getContentResolver().query(uri, projection, null, null, MediaStore.Images.Media.DATE_MODIFIED + " desc");
        if(mCursor == null || mCursor.getCount() < 1) {
            return null; // no cursor 또는 no record
        }
        int column_index = mCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        mCursor.moveToFirst();

        String path = mCursor.getString(column_index);

        if(mCursor !=null ) {
            mCursor.close();
            mCursor = null;
        }

        return new File(path);
    }

    /*private void launchPreview(boolean b) {
        if(fileUri.getPath() != null) {
            // 화면에 이미지나 동영상을 보여주기
            previewMedia(b);
        } else {
            Toast.makeText(getApplicationContext(), "파일 지정이 없습니다", Toast.LENGTH_LONG).show();
        }
    }*/

    /*private void previewMedia(boolean isImage) {
        // 캡쳐된 이미지나 비디오인지 아닌지 확인
        if(isImage) {
            image_preview.setVisibility(View.VISIBLE);
            video_preview.setVisibility(View.GONE);
            // 비트맵 팩토리
            BitmapFactory.Options options = new BitmapFactory.Options();

            // 이미지들
            options.inSampleSize = 8;

            final Bitmap bitmap = BitmapFactory.decodeFile(fileUri.getPath(), options);

            image_preview.setImageBitmap(bitmap);
        } else {
            image_preview.setVisibility(View.GONE);
            video_preview.setVisibility(View.VISIBLE);
            video_preview.setVideoPath(fileUri.getPath());
            // 플레이 시작
            video_preview.start();
        }
    }*/

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
                Intent intent = new Intent();
                setResult(RESULT_OK, intent); // 인텐트를 반환값으로 돌려준다
                finish();
                return true;
            case R.id.action_send :
                String text = inputfeed.getText().toString().trim();
                if(!text.isEmpty() || contents.size() > 0) {
                    if(contents.size() > 0) {
                        fileNames = new ArrayList<>();
                        progressDialog.setMessage("파일 전송중...");
                        showProgressDialog();
                        for(int i = 0; i < contents.size(); i++) {
                            Bitmap bitmap = contents.get(i).getBitmap();
                            uploadBitmap(text, bitmap);
                        }
                    } else {
                        fileNames = null;
                        Action_Send(text, fileNames);// 일반 전송
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
        switch(contextMenuRequest) {
            case 1 :
                menu.setHeaderTitle("작업 선택");
                menu.add(Menu.NONE, 1, Menu.NONE, "삭제");
                break;
            case 2 :
                menu.setHeaderTitle("이미지 선택");
                menu.add(Menu.NONE, 2, Menu.NONE, "앨범");
                menu.add(Menu.NONE, 3, Menu.NONE, "카메라");
                break;
            case 3 :
                menu.setHeaderTitle("작업 선택");
                menu.add(Menu.NONE, 4, Menu.NONE, "동영상");
                break;
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case 1 :
                removeListItem();
                return true;
            case 2 :
                pickImage();
                return true;
            case 3 :
                captureImage();
                return true;
            case 4 :
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
        startActivityForResult(intent, CAMERA_PICK_IMAGE_REQUEST_CODE);
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
                    if(fileNames.size() == contents.size())
                        // 파일전송이 완료하면, 응답값을 받아 글쓰기를 완료한다
                        Action_Send(text, fileNames);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "응답 에러" + error.getMessage(), Toast.LENGTH_SHORT).show();
                hideProgressDialog();
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

    public void onResume() {
        super.onResume();
        listAdapter.notifyDataSetChanged();
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