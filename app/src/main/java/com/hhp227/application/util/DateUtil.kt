package com.hhp227.application.util

import android.content.Context
import android.text.TextUtils
import com.hhp227.application.R
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object DateUtil {
    fun getPeriodTimeGenerator(context: Context, strDate: String?): String? {
        if (TextUtils.isEmpty(strDate)) {
            return ""
        } else {
            val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").apply { timeZone = TimeZone.getDefault() }
            val date: Date = try {
                df.parse(strDate)
            } catch (e: ParseException) {
                e.printStackTrace()
                return ""
            }
            val writeDatetime = date.time
            val nowDatetime = Date().time
            val milliSeconds = nowDatetime - writeDatetime
            val seconds = getSeconds(milliSeconds)
            val minutes = getMinutes(milliSeconds)
            val hours = getHours(milliSeconds)
            val days = getDays(milliSeconds)

            if (days > 1) {
                val cal = Calendar.getInstance()
                // yyyy-MM-dd HH:mm:ss => "MM월 dd일"
                val sdf = SimpleDateFormat(context.resources.getString(R.string.format_date))
                return sdf.format(date)
            }
            return if (days > 0) String.format("%d" + context.getString(R.string.day), days)
            else if (hours > 0) String.format("%d" + context.getString(R.string.hour), hours)
            else if (minutes > 0) String.format("%d" + context.getString(R.string.minute), minutes)
            else if (seconds > 1) String.format("%d" + context.getString(R.string.second), seconds)
            else if (seconds < 2) String.format("" + context.getString(R.string.afew), seconds)
            else strDate
        }
    }

    private fun getSeconds(mMilliSecs: Long): Int {
        return (mMilliSecs / 1000).toInt()
    }

    /**
     * 분
     * @return
     */
    private fun getMinutes(mMilliSecs: Long): Int {
        return getSeconds(mMilliSecs) / 60
    }

    /**
     * 시
     * @return
     */
    private fun getHours(mMilliSecs: Long): Int {
        return getMinutes(mMilliSecs) / 60
    }

    /**
     * 일
     * @return
     */
    private fun getDays(mMilliSecs: Long): Int {
        return getHours(mMilliSecs) / 24
    }

    fun getTimeStamp(dateStr: String?): String? {
        var format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        var timestamp = ""

        try {
            val date = format.parse(dateStr)
            format = SimpleDateFormat("a hh:mm")
            val date1 = format.format(date)
            timestamp = date1.toString()
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return timestamp
    }
}