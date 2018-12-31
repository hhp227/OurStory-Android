package com.hhp227.application.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.hhp227.application.R;
import com.hhp227.application.app.AppController;
import com.hhp227.application.app.URLs;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

public class DCDormMealFragment extends Fragment {
    private static final String TAG = "문화관 식단표";
    private TextView[] menuView;

	public static DCDormMealFragment newInstance() {
		DCDormMealFragment fragment = new DCDormMealFragment();
		return fragment;
		}

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	}
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_dormmeal, container, false);

        menuView = new TextView[] {
                (TextView) root.findViewById(R.id.breakfast),
                (TextView) root.findViewById(R.id.lunch),
                (TextView) root.findViewById(R.id.dinner)
        };

        String endPoint = URLs.URL_KNU_DORM_MEAL.replace("{ID}", "2");
        AppController.getInstance().addToRequestQueue(new StringRequest(Request.Method.GET, endPoint, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                ArrayList<String> arrayList = new ArrayList<String>();
                try {
                    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                    XmlPullParser parser = factory.newPullParser();
                    parser.setInput(new StringReader(response));
                    int eventType = parser.getEventType();

                    while(eventType != XmlPullParser.END_DOCUMENT) {
                        switch(eventType) {
                            case XmlPullParser.START_TAG :
                                String startTag = parser.getName();
                                if(startTag.equals("data"))
                                    arrayList.add(new SpannableString(Html.fromHtml(parser.nextText())).toString());
                                break;
                        }
                        eventType = parser.next();
                    }
                } catch(XmlPullParserException e) {
                    e.printStackTrace();
                } catch(IOException e) {
                    e.printStackTrace();
                }

                for(int i = 0; i < menuView.length; i++)
                    menuView[i].setText(arrayList.get(i));
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e(TAG, error.getMessage());
            }
        }));

        return root;
    }
}