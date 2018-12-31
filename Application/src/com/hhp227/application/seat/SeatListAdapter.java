package com.hhp227.application.seat;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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

import com.hhp227.application.R;

public class SeatListAdapter extends BaseAdapter {
    private Activity activity;
    private LayoutInflater inflater;
    private List<SeatItem> seatItems;
    private TextView name, text, status;

    public SeatListAdapter(Activity activity, List<SeatItem> seatItems) {
        this.activity = activity;
        this.seatItems = seatItems;
    }

    @Override
    public int getCount() {
        return seatItems.size();
    }

    @Override
    public Object getItem(int position) {
        return seatItems.get(position);
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
            convertView = inflater.inflate(R.layout.seat_item, null);

        name = (TextView) convertView.findViewById(R.id.name);
        text = (TextView) convertView.findViewById(R.id.text);
        status = (TextView) convertView.findViewById(R.id.seat);

        SeatItem seatItem = seatItems.get(position);
        name.setText(seatItem.name);
        text.setText(seatItem.disable == null ? "사용중 좌석" : seatItem.disable[0]);
        status.setText(seatItem.disable == null ?
                new StringBuilder().append("[").append(seatItem.occupied).append("/").append(seatItem.activeTotal).append("]").toString() :
                new StringBuilder().append(getPeriodTimeGenerator(activity, seatItem.disable[1])).append(" ~ ").append(getPeriodTimeGenerator(activity, seatItem.disable[2])).toString());

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
        SimpleDateFormat sdf = new SimpleDateFormat(context.getResources().getString(R.string.format_date));
        return sdf.format(date);
    }
}
