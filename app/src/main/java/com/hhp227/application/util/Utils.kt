package com.hhp227.application.util;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import com.hhp227.application.R;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class Utils {
    public static String getPeriodTimeGenerator(Context context, String strDate) {
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
            SimpleDateFormat sdf = new SimpleDateFormat(context.getResources().getString(R.string.format_date));
            return sdf.format(date);
        }
        if (nDays > 0)
            return String.format("%d" + context.getString(R.string.day), nDays);

        if (nHours > 0)
            return String.format("%d" + context.getString(R.string.hour), nHours);
        if (nMins > 0)
            return String.format("%d" + context.getString(R.string.minute), nMins);
        if (nSecs > 1)
            return String.format("%d" + context.getString(R.string.second), nSecs);
        if (nSecs < 2)
            return String.format("" + context.getString(R.string.afew), nSecs);
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
