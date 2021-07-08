package com.hhp227.application.util

import android.content.Context
import android.text.TextUtils
import com.hhp227.application.R
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object Utils {
    fun getPeriodTimeGenerator(context: Context, strDate: String?): String? {
        val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").apply { timeZone = TimeZone.getDefault() }
        if (TextUtils.isEmpty(strDate)) return ""
        val date: Date = try {
            df.parse(strDate)
        } catch (e: ParseException) {
            e.printStackTrace()
            return ""
        }
        val writeDatetime = date.time
        val nowDate = Date()
        val nowDatetime = nowDate.time
        val mMilliSecs = nowDatetime - writeDatetime
        val nSecs = getSeconds(mMilliSecs)
        val nMins = getMinutes(mMilliSecs)
        val nHours = getHours(mMilliSecs)
        val nDays = getDays(mMilliSecs)

        if (nDays > 1) {
            val cal = Calendar.getInstance()
            // yyyy-MM-dd HH:mm:ss => "MM월 dd일"
            val sdf = SimpleDateFormat(context.resources.getString(R.string.format_date))
            return sdf.format(date)
        }
        if (nDays > 0) return String.format("%d" + context.getString(R.string.day), nDays)
        if (nHours > 0) return String.format("%d" + context.getString(R.string.hour), nHours)
        if (nMins > 0) return String.format("%d" + context.getString(R.string.minute), nMins)
        if (nSecs > 1) return String.format("%d" + context.getString(R.string.second), nSecs)
        return if (nSecs < 2) String.format(
            "" + context.getString(R.string.afew),
            nSecs
        ) else strDate
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
}