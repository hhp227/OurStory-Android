package com.hhp227.application.staggeredgrid;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.android.volley.toolbox.ImageLoader;
import com.hhp227.application.PictureActivity;
import com.hhp227.application.R;
import com.hhp227.application.app.AppController;
import com.hhp227.application.ui.staggeredgrid.grid.util.DynamicHeightImageView;

public class AlbumListAdapter extends BaseAdapter {

	private static final String TAG = "앨범어뎁터";

	private LayoutInflater mLayoutInflater;
	//private final Random mRandom;
	ImageLoader imageLoader = AppController.getInstance().getImageLoader();
	private Activity activity;

	private List<AlbumItem> albumItems;
	private static final SparseArray<Double> sPositionHeightRatios = new SparseArray<Double>();

	public AlbumListAdapter(Activity activity,
			List<AlbumItem> albumItems) {
		this.activity = activity;
		this.albumItems = albumItems;
	}

	@Override
	public int getCount() {
		return albumItems.size();
	}

	@Override
	public Object getItem(int position) {
		return albumItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	@Override
	public View getView(final int position, View convertView,
			final ViewGroup parent) {

		if (mLayoutInflater == null)
			mLayoutInflater = (LayoutInflater) activity
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if (convertView == null) 
			convertView = mLayoutInflater.inflate(R.layout.row_grid_item, null);
		if (imageLoader == null)
			imageLoader = AppController.getInstance().getImageLoader();
			
		DynamicHeightImageView imgView = (DynamicHeightImageView) convertView
					.findViewById(R.id.imgView);

		final AlbumItem item = albumItems.get(position);
		double positionHeight = getPositionRatio(position);
		
		if (item.getImge() != null) {
			imgView.setHeightRatio(positionHeight);
			imgView.setImageUrl(item.getImge(), imageLoader);
			// 이미지 클릭시 확대할수있는 화면으로 넘어감
			imgView.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(activity, PictureActivity.class);
					intent.putExtra(PictureActivity.IMAGE_URL, item.getImge().toString());
					activity.startActivity(intent);
				}
				
			});
		} else {
			imgView.setVisibility(View.GONE);
		}
		//ImageLoader.getInstance().displayImage(getItem(position), vh.imgView);
		return convertView;
	}

	static class ViewHolder {
		DynamicHeightImageView imgView;
	}

	private double getPositionRatio(final int position) {
		double ratio = sPositionHeightRatios.get(position, 0.0);
		// if not yet done generate and stash the columns height
		// in our real world scenario this will be determined by
		// some match based on the known height and width of the image
		// and maybe a helpful way to get the column height!
		if (ratio == 0) {
			//ratio = getRandomHeightRatio();
			sPositionHeightRatios.append(position, ratio);
			Log.d(TAG, "getPositionRatio:" + position + " ratio:" + ratio);
		}
		return ratio;
	}

	/*private double getRandomHeightRatio() {
		return (mRandom.nextDouble() / 2.0) + 1.0; // 세로그크기는 1.0 - 1.5 될것	
	}*/
}