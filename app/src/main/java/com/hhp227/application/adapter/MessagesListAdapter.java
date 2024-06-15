package com.hhp227.application.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hhp227.application.R;
import com.hhp227.application.model.ChatItem;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MessagesListAdapter extends BaseAdapter {
	private Context context;
	private List<ChatItem.Message> messagesItems;
	private int user_id;

	public MessagesListAdapter(Context context, List<ChatItem.Message> navDrawerItems, int user_id) {
		this.context = context;
		this.messagesItems = navDrawerItems;
		this.user_id = user_id;
	}

	@Override
	public int getCount() {
		return messagesItems.size();
	}

	@Override
	public Object getItem(int position) {
		return messagesItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@SuppressLint("InflateParams")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		/**
		 * 다음 목록은 목록 항목으로 재사용 가능한 목록 항목을 구현하지
		 * 하나가있는 경우 잘못된 데이터 솔루션을 추가 보이고있다.
		 * */

		ChatItem.Message m = messagesItems.get(position);

		LayoutInflater mInflater = (LayoutInflater) context .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

		// 메시지 소유자 식별
		// 메시지가에 속하는, 오른쪽 정렬 된 레이아웃을로드
		// 메시지가 다른 사람에게 속한, 왼쪽 정렬 된 레이아웃을로드
		convertView = mInflater.inflate(messagesItems.get(position).getUser().getId() == user_id ? R.layout.list_item_message_right : R.layout.list_item_message_left, null);

		LinearLayout msgBox = convertView.findViewById(R.id.message_box);
		TextView lblFrom = convertView.findViewById(R.id.lbl_msg_from);
		TextView txtMsg = convertView.findViewById(R.id.txt_msg);
		TextView msgTime = convertView.findViewById(R.id.msg_time);
		ImageView profileImg = convertView.findViewById(R.id.iv_profile_image);

		txtMsg.setText(m.getMessage());
		lblFrom.setText(m.getUser().getName());
		msgTime.setText(getTimeStamp(m.getTime()));

		if (position > 0 && getTimeStamp(messagesItems.get(position - 1).getTime()).equals(getTimeStamp(messagesItems.get(position).getTime())) && messagesItems.get(position - 1).getUser().getId() == messagesItems.get(position).getUser().getId()) {
			lblFrom.setVisibility(View.GONE);
			msgBox.setPadding(msgBox.getPaddingLeft(), 0, msgBox.getPaddingRight(), msgBox.getPaddingBottom());
		} else {
         /*Glide.with(context)
               .load(URLs.URL_USER_PROFILE_IMAGE + m.getUser().getProfileImage())
               .apply(RequestOptions.errorOf(R.drawable.profile_img_circle).circleCrop())
               .into(profileImg);*/
		}
		try {
			// 타임스탬프와 유저넘버가 이후포지션과 같다면
			if (getTimeStamp(messagesItems.get(position).getTime()).equals(getTimeStamp(messagesItems.get(position + 1).getTime())) && messagesItems.get(position).getUser().getId() == messagesItems.get(position + 1).getUser().getId()) {
				msgTime.setVisibility(View.INVISIBLE);
			}
		} catch (Exception e) {
		}

		return convertView;
	}

	public static String getTimeStamp(String dateStr) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String timestamp = "";

		try {
			Date date = format.parse(dateStr);
			format = new SimpleDateFormat("a hh:mm");
			String date1 = format.format(date);
			timestamp = date1.toString();
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return timestamp;
	}
}