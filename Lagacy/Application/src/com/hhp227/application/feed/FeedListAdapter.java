package com.hhp227.application.feed;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.StringRequest;
import com.hhp227.application.R;
import com.hhp227.application.ArticleActivity;
import com.hhp227.application.app.AppController;
import com.hhp227.application.app.URLs;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class FeedListAdapter extends BaseAdapter {
    private Activity activity;
    private LayoutInflater inflater;
    private List<FeedItem> feedItems;
    private TextView name, timestamp, Contents, Contents_more, replycount, likecount;
    private ImageView favorites;
    private LinearLayout replybutton, likebutton;
    private ImageLoader imageLoader = AppController.getInstance().getImageLoader();
    public static boolean liked;

    public FeedListAdapter(Activity activity, List<FeedItem> feedItems) {
        this.activity = activity;
        this.feedItems = feedItems;
    }

    // 어탭터에 항목이 몇개있는지 조사
    @Override
    public int getCount() {
        return feedItems.size();
    }

    @Override
    public Object getItem(int position) {
        return feedItems.get(position);
    }

    // position 위치의 항목ID 반환
    @Override
    public long getItemId(int position) {
        return position;
    }

    // 각 항목의 View 생성후 반환
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (inflater == null)
            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null)
            convertView = inflater.inflate(R.layout.feed_item, null);

        if (imageLoader == null)
            imageLoader = AppController.getInstance().getImageLoader();

        name = (TextView) convertView.findViewById(R.id.name);
        timestamp = (TextView) convertView.findViewById(R.id.timestamp);
        Contents = (TextView) convertView.findViewById(R.id.txtContents);
        Contents.setMaxLines(4);
        Contents_more = (TextView) convertView.findViewById(R.id.txtContents_more);
        CircularNetworkImageView profilePic = (CircularNetworkImageView) convertView.findViewById(R.id.profilePic);
        FeedImageView feedImageView = (FeedImageView) convertView.findViewById(R.id.feedImage1);
        replycount = (TextView) convertView.findViewById(R.id.replycount);
        replybutton = (LinearLayout) convertView.findViewById(R.id.linear_reply);
        likecount = (TextView) convertView.findViewById(R.id.likecount);
        likebutton = (LinearLayout) convertView.findViewById(R.id.linear_like);
        favorites = (ImageView) convertView.findViewById(R.id.favorites);

        FeedItem item = feedItems.get(position);

        name.setText(item.getName());

        String date = getPeriodTimeGenerator(activity, item.getTimeStamp());
        timestamp.setText(date);
        // 피드의 메시지가 비었는지 확인
        if (!TextUtils.isEmpty(item.getFeed())) {
            Contents.setText(item.getFeed());
            Contents.setVisibility(View.VISIBLE);
        } else {
            // 피드 내용이 비었으면 화면에서 삭제
            Contents.setVisibility(View.GONE);
        }

        Contents_more.setVisibility(Contents.getLineCount() > 4 ? View.VISIBLE : View.GONE);

        // 사용자 프로필 이미지
        profilePic.setImageUrl(URLs.URL_USER_PROFILE_IMAGE + item.getProfilePic(), imageLoader);
        profilePic.setDefaultImageResId(R.drawable.profile_img_circle);
        profilePic.setErrorImageResId(R.drawable.profile_img_circle);

        // 피드 이미지
        if (item.getImage() != null) {
            feedImageView.setImageUrl(URLs.URL_FEED_IMAGE + item.getImage(), imageLoader);
            feedImageView.setVisibility(View.VISIBLE);
            feedImageView
                    .setResponseObserver(new FeedImageView.ResponseObserver() {
                        @Override
                        public void onError() {
                        }

                        @Override
                        public void onSuccess() {
                        }
                    });
        } else
            feedImageView.setVisibility(View.GONE);

        replycount.setText(item.getReplyCount());

        // 댓글 버튼을 누르면 댓글쓰는곳으로 이동
        replybutton.setTag(position);
        replybutton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Integer position = (Integer) v.getTag();
                FeedItem item = feedItems.get(position);
                int feed_id = item.getId();
                int user_id = item.getUser_id();
                String name = item.getName();
                String timeStamp = getPeriodTimeGenerator(activity, item.getTimeStamp());

                Intent intent = new Intent(activity, ArticleActivity.class);
                intent.putExtra("position", position);
                intent.putExtra("isbottom", true);

                intent.putExtra("feed_id", feed_id);
                intent.putExtra("user_id", user_id);
                intent.putExtra("name", name);
                intent.putExtra("timeStamp", timeStamp);
                (activity).startActivity(intent);
            }
        });
        likecount.setText(String.valueOf(item.getLikeCount()));
        likecount.setVisibility(item.getLikeCount() == 0 ? View.GONE : View.VISIBLE);
        favorites.setVisibility(item.getLikeCount() == 0 ? View.GONE : View.VISIBLE);

        // 좋아요 버튼을 누르면 일어나는 동작
        liked = false;
        likebutton.setTag(position);
        likebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                final String TAG = "좋아요";
                final FeedItem item = feedItems.get(position);
                String tag_string_req = "req_delete";
                String URL_FEED_LIKE = URLs.URL_FEED_LIKE.replace("{FEED_ID}", String.valueOf(item.getId()));
                StringRequest strReq = new StringRequest(Request.Method.GET, URL_FEED_LIKE, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        VolleyLog.d(TAG, "응답 : " + response.toString());
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            boolean error = jsonObject.getBoolean("error");
                            if (!error) {
                                String result = jsonObject.getString("result");
                                item.setLikeCount(result.equals("insert") ? item.getLikeCount() + 1 : item.getLikeCount() - 1);
                                feedItems.set(position, item);
                                notifyDataSetChanged(); // 데이터가 변경될때마다 화면을 새로고침
                            } else {
                                // 삭제에서 에러 발생. 에러 내용
                                String errorMsg = jsonObject.getString("message");
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "에러" + e);
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyLog.d(TAG, "에러 : " + error.getMessage());
                    }
                }) {
                    String apikey = AppController.getInstance().getPrefManager().getUser().getApi_key();
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
        });

        return convertView;
    }
    /**
     * 24시간 이전까진 시 ,분 ,초 만표시하고 24시간 이상넘어가면 yyyy-MM-dd HH:mm:ss전체를 보여준다.
     */
    private static String getPeriodTimeGenerator(Activity activity, String strDate) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        df.setTimeZone(TimeZone.getDefault());
        Date date = null;

        if (TextUtils.isEmpty(strDate))
            return "";

        try {
            date = df.parse(strDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
        long write_datetime = date.getTime();

        Date nowDate = new Date();
        long now_datetime = nowDate.getTime();

        long mMilliSecs = (now_datetime - write_datetime);

        int nSecs = getSeconds(mMilliSecs);
        int nMins = getMinutes(mMilliSecs);
        int nHours = getHours(mMilliSecs);
        int nDays = getDays(mMilliSecs);

        String ret = strDate;
        if (nDays > 1) {
            Calendar cal = Calendar.getInstance();
            // yyyy-MM-dd HH:mm:ss => "MM월 dd일"
            SimpleDateFormat sdf = new SimpleDateFormat(activity.getResources().getString(R.string.format_date));
            return sdf.format(date);
        }
        if (nDays > 0)
            return String.format("%d" + activity.getString(R.string.day), nDays);

        if (nHours > 0)
            return String.format("%d" + activity.getString(R.string.hour), nHours);
        if (nMins > 0)
            return String.format("%d" + activity.getString(R.string.minute), nMins);
        if (nSecs > 1)
            return String.format("%d" + activity.getString(R.string.second), nSecs);
        if (nSecs < 2)
            return String.format("" + activity.getString(R.string.afew), nSecs);
        return ret;
    }

    private static int getSeconds(long mMilliSecs) {
        return (int) (mMilliSecs / 1000);
    }

    /**
     * 분
     * @return
     */
    private static int getMinutes(long mMilliSecs) {
        return (getSeconds(mMilliSecs) / 60);
    }

    /**
     * 시
     * @return
     */
    private static int getHours(long mMilliSecs) {
        return (getMinutes(mMilliSecs) / 60);
    }

    /**
     * 일
     * @return
     */
    private static int getDays(long mMilliSecs) {
        return (getHours(mMilliSecs) / 24);
    }
}
