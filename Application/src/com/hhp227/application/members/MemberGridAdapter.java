package com.hhp227.application.members;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.hhp227.application.R;
import com.hhp227.application.app.AppController;

public class MemberGridAdapter extends BaseAdapter {	
	private Activity activity;
	private LayoutInflater inflater;
	private List<MemberItem> memberItems;
	TextView name;
	ImageLoader imageLoader = AppController.getInstance().getImageLoader();

	public MemberGridAdapter(Activity activity, List<MemberItem> memberItems) {
		this.activity = activity;
		this.memberItems = memberItems;
	}

	@Override
	public int getCount() {
		return memberItems.size();
	}

	@Override
	public Object getItem(int location) {
		return memberItems.get(location);
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
			convertView = inflater.inflate(R.layout.member_item, null);

		if (imageLoader == null)
			imageLoader = AppController.getInstance().getImageLoader();

		name = (TextView) convertView.findViewById(R.id.tvname_user);
		NetworkImageView profileImg = (NetworkImageView) convertView
				.findViewById(R.id.profile_img);
		ImageView nullprofileImg = (ImageView) convertView.findViewById(R.id.null_profile_img);
		MemberItem item = memberItems.get(position);

		name.setText(item.getName());

		// 사용자 프로필 이미지가 비었는지 확인
		if (item.getProfile_img() != null) { // 프로필url이 비었으면
			profileImg.setImageUrl(item.getProfile_img(), imageLoader);
			profileImg.setVisibility(View.VISIBLE); // 네트워크이미지뷰가 뜬다
			nullprofileImg.setVisibility(View.GONE);
		} else {
			nullprofileImg.setVisibility(View.VISIBLE); // null프로필 이미지가 뜬다
			profileImg.setVisibility(View.GONE);
		}
		
		return convertView;
	}

}
