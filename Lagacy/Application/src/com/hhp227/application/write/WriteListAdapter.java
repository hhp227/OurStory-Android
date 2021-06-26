package com.hhp227.application.write;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.VideoView;

import com.android.volley.toolbox.ImageLoader;
import com.hhp227.application.R;
import com.hhp227.application.app.AppController;
import com.hhp227.application.app.URLs;
import com.hhp227.application.feed.FeedImageView;

import java.util.List;

public class WriteListAdapter extends ArrayAdapter<Writeitem> {
    private Context context;
    private LayoutInflater inflater;
    private int resource;
    private FeedImageView feedImageView;
    private ImageView image_preview;
    private VideoView video_preview;
    private ImageLoader imageLoader = AppController.getInstance().getImageLoader();

    public WriteListAdapter(Context context, int resource, List<Writeitem> list) {
        super(context, resource, list);
        this.context = context;
        this.resource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (inflater == null)
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null)
            convertView = inflater.inflate(resource, null);

        if (imageLoader == null)
            imageLoader = AppController.getInstance().getImageLoader();

        image_preview = (ImageView) convertView.findViewById(R.id.image_preview);
        video_preview = (VideoView) convertView.findViewById(R.id.video_preview);
        feedImageView = (FeedImageView) convertView.findViewById(R.id.feedImage1);

        Writeitem writeitem = getItem(position);

        if (writeitem.getFileUri() != null) {
            image_preview.setImageBitmap(writeitem.getBitmap());
            image_preview.setVisibility(View.VISIBLE);
        } else
            image_preview.setVisibility(View.GONE);

        if (writeitem.getImage() != null) {
            feedImageView.setImageUrl(URLs.URL_FEED_IMAGE + writeitem.getImage(), imageLoader);
            feedImageView.setVisibility(View.VISIBLE);
        } else
            feedImageView.setVisibility(View.GONE);

        return convertView;
    }
}
