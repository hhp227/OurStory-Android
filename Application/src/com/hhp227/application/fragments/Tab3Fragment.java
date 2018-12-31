package com.hhp227.application.fragments;

import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.hhp227.application.R;
import com.hhp227.application.app.AppController;
import com.hhp227.application.app.URLs;
import com.hhp227.application.calendar.Day;
import com.hhp227.application.calendar.ExtendedCalendarView;
import com.hhp227.application.scrollable.BaseFragment;

public class Tab3Fragment extends BaseFragment implements View.OnClickListener {
    private static final String TAG = "일정";
    private ExtendedCalendarView extendedCalendarView;
    private Calendar calendar;
    private ListView listView;
    private List<HashMap<String, String>> list;
    private HashMap<String, String> hashmap;
    private SimpleAdapter arrayAdapter;
    private String year, month;
    private View headerView;

    public static Tab3Fragment newInstance() {
        Tab3Fragment fragment = new Tab3Fragment();
        return fragment;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tab3, container, false);

        headerView = getLayoutInflater(savedInstanceState).inflate(R.layout.header_calendar, null, false);

        extendedCalendarView = (ExtendedCalendarView) headerView.findViewById(R.id.calendar);
        listView = (ListView) rootView.findViewById(R.id.lv_cal);

        calendar = Calendar.getInstance();

        extendedCalendarView.setOnDayClickListener(new ExtendedCalendarView.OnDayClickListener() {
            @Override
            public void onDayClicked(AdapterView<?> adapter, View view, int position, long id, Day day) {
                
            }
        });
        extendedCalendarView.next.setOnClickListener(this);
        extendedCalendarView.prev.setOnClickListener(this);
        listView.addHeaderView(headerView);

        fetchData();

        return rootView;
    }

    private void fetchData() {
        year = String.valueOf(calendar.get(Calendar.YEAR));
        month = String.format("%02d", calendar.get(Calendar.MONTH) + 1);

        String endPoint = URLs.URL_SCHEDULE.replace("{YEAR-MONTH}", year.concat(month));
        AppController.getInstance().addToRequestQueue(new StringRequest(Request.Method.GET, endPoint, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                list = new ArrayList<>();
                arrayAdapter = new SimpleAdapter(getContext(), list, R.layout.schedule_item, new String[]{"날짜", "내용"}, new int[]{R.id.date, R.id.content});
                listView.setAdapter(arrayAdapter);

                try {
                    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                    XmlPullParser parser = factory.newPullParser();
                    parser.setInput(new StringReader(response));
                    int eventType = parser.getEventType();

                    while(eventType != XmlPullParser.END_DOCUMENT) {
                        switch(eventType) {
                            case XmlPullParser.START_TAG :
                                switch(parser.getName()) {
                                    case "entry" :
                                        hashmap = new HashMap<String, String>();
                                        break;
                                    case "date" :
                                        hashmap.put("날짜", getTimeStamp(parser.nextText()));
                                        break;
                                    case "title" :
                                        try {
                                            hashmap.put("내용", parser.nextText());
                                        } catch(Exception e) {
                                            Log.e(TAG, e.getMessage());
                                        }
                                        break;
                                }
                                break;
                            case XmlPullParser.END_TAG :
                                if(parser.getName().equals("entry"))
                                    list.add(hashmap);
                        }
                        eventType = parser.next();
                    }
                } catch(XmlPullParserException e) {
                    e.printStackTrace();
                } catch(IOException e) {
                    e.printStackTrace();
                }
                arrayAdapter.notifyDataSetChanged();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e(TAG, error.getMessage());
            }
        }));
    }

    @Override
    public boolean canScrollVertically(int direction) {
        return listView != null && listView.canScrollVertically(direction);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case 1 :
                extendedCalendarView.previousMonth();
                if(calendar.get(Calendar.MONTH) == calendar.getActualMinimum(Calendar.MONTH)) {
                    calendar.set((calendar.get(Calendar.YEAR) - 1), calendar.getActualMaximum(Calendar.MONTH),1);
                } else {
                    calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - 1);
                }
                break;
            case 3 :
                extendedCalendarView.nextMonth();
                if(calendar.get(Calendar.MONTH) == calendar.getActualMaximum(Calendar.MONTH)) {
                    calendar.set((calendar.get(Calendar.YEAR) + 1), calendar.getActualMinimum(Calendar.MONTH),1);
                } else {
                    calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + 1);
                }
                break;
        }
        fetchData();
    }

    public static String getTimeStamp(String dateStr) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String timestamp = "";

        try {
            Date date = format.parse(dateStr);
            format = new SimpleDateFormat("dd");
            String date1 = format.format(date);
            timestamp = date1.toString();
        } catch(ParseException e) {
            e.printStackTrace();
        }

        return timestamp;
    }
}
