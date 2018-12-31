package com.hhp227.application.reply;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.hhp227.application.R;
import com.hhp227.application.app.AppController;
import com.hhp227.application.app.URLs;
import com.hhp227.application.feed.CircularNetworkImageView;

public class ReplyListAdapter extends BaseAdapter {
    private Activity activity;
    private LayoutInflater inflater;
    private List<ReplyItem> replyItems;
    private String date;
    ImageLoader imageLoader = AppController.getInstance().getImageLoader();

    public ReplyListAdapter(Activity activity, List<ReplyItem> replyItems) {
        this.activity = activity;
        this.replyItems = replyItems;
    }

    @Override
    public int getCount() {
        return replyItems.size();
    }

    @Override
    public Object getItem(int location) {
        return replyItems.get(location);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if(inflater == null)
            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if(convertView == null)
            convertView = inflater.inflate(R.layout.reply_item, null);

        if(imageLoader == null)
            imageLoader = AppController.getInstance().getImageLoader();
        CircularNetworkImageView profileImg = (CircularNetworkImageView) convertView.findViewById(R.id.profile_img);
        TextView name = (TextView) convertView.findViewById(R.id.name);
        TextView reply = (TextView) convertView.findViewById(R.id.reply);
        TextView timestamp = (TextView) convertView.findViewById(R.id.date);

        // 덧글 데이터 얻기
        ReplyItem item = replyItems.get(position);

        profileImg.setImageUrl(URLs.URL_USER_PROFILE_IMAGE + item.getProfileImg(), imageLoader);
        profileImg.setDefaultImageResId(R.drawable.profile_img_circle);
        profileImg.setErrorImageResId(R.drawable.profile_img_circle);
        name.setText(item.getName());
        reply.setText(item.getReply());
        // 시간 변환
        date = getPeriodTimeGenerator(activity, item.getTimeStamp());
        timestamp.setText(date);

        return convertView;
    }

    // 타임 제네레이터
    private static String getPeriodTimeGenerator(Context context, String strDate) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        df.setTimeZone(TimeZone.getDefault());
        Date date = null;

        if(TextUtils.isEmpty(strDate))
            return "";

        try {
            date = df.parse(strDate);
        } catch(ParseException e) {
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
        if(nDays > 1) {
            Calendar cal = Calendar.getInstance();
            // yyyy-MM-dd HH:mm:ss => "MM월 dd일"
            SimpleDateFormat sdf = new SimpleDateFormat(context.getResources().getString(R.string.format_date));
            return sdf.format(date);
        }
        if(nDays > 0)
            return String.format("%d" + context.getString(R.string.day), nDays);
        if(nHours > 0)
            return String.format("%d" + context.getString(R.string.hour), nHours);
        if(nMins > 0)
            return String.format("%d" + context.getString(R.string.minute), nMins);
        if(nSecs > 1)
            return String.format("%d" + context.getString(R.string.second), nSecs);
        if(nSecs < 2)
            return String.format("" + context.getString(R.string.afew), nSecs);
        return ret;
    }

    private static int getSeconds(long mMilliSecs) {
        return (int) (mMilliSecs / 1000);
    }

    private static int getMinutes(long mMilliSecs) {
        return (getSeconds(mMilliSecs) / 60);
    }

    private static int getHours(long mMilliSecs) {
        return (getMinutes(mMilliSecs) / 60);
    }

    private static int getDays(long mMilliSecs) {
        return (getHours(mMilliSecs) / 24);
    }
}