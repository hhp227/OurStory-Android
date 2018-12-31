package com.hhp227.application.bbs;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hhp227.application.R;

public class BbsListAdapter extends BaseAdapter {
	private List<BbsItem> mListData;
	private Activity activity;
    private LayoutInflater inflater;

    public BbsListAdapter(Activity activity, ArrayList<BbsItem> mListData) {
        this.activity = activity;
        this.mListData = mListData;
    }

    @Override
    public int getCount() {
        return mListData.size();
    }

    @Override
    public Object getItem(int position) {
        return mListData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

    	if (inflater == null)
            inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) 
            convertView = inflater.inflate(R.layout.bbs_itemstyle, null);

        TextView mTitle = (TextView) convertView.findViewById(R.id.item_title);
        TextView mWriter = (TextView) convertView.findViewById(R.id.item_writer);
        TextView mDate = (TextView) convertView.findViewById(R.id.item_date);

        BbsItem mData = mListData.get(position);

        if(mData.getType().equals("공지")){
            mTitle.setText(Html.fromHtml("<font color=#616161>[공지] </font>" + mData.getTitle())); //"공지" 의 색깔을 부분적으로 약간 진하게 수정.
        }else{
            mTitle.setText(Html.fromHtml("" +mData.getTitle()));
        }

        mWriter.setText(mData.getWriter());
        mDate.setText(mData.getDate());

        return convertView;

    }

}
/*
public class BbsListAdapter extends BaseAdapter {
private List<BbsItem> mListData;
private Activity activity;
private LayoutInflater inflater;

public BbsListAdapter(Activity activity, ArrayList<BbsItem> mListData) {
    this.activity = activity;
    this.mListData = mListData;
}

@Override
public int getCount() {
    return mListData.size();
}

@Override
public Object getItem(int position) {
    return mListData.get(position);
}

@Override
public long getItemId(int position) {
    return position;
}

@Override
public View getView(int position, View convertView, ViewGroup parent) {

	if (inflater == null)
        inflater = (LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    if (convertView == null) 
        convertView = inflater.inflate(R.layout.deptnotice_itemstyle, null);

    TextView mTitle = (TextView) convertView.findViewById(R.id.item_title);
    TextView mWriter = (TextView) convertView.findViewById(R.id.item_writer);
    TextView mDate = (TextView) convertView.findViewById(R.id.item_date);

    BbsItem mData = mListData.get(position);

    if(mData.mType.equals("공지")){
        mTitle.setText(Html.fromHtml("<font color=#616161>[공지] </font>" +mData.mTitle)); //"공지" 의 색깔을 부분적으로 약간 진하게 수정.
    }else{
        mTitle.setText(Html.fromHtml("" +mData.mTitle));
    }

    mWriter.setText(mData.mWriter);
    mDate.setText(mData.mDate);

    return convertView;

}

}*/