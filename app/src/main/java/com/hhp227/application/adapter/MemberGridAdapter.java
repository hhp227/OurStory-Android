package com.hhp227.application.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.hhp227.application.R;
import com.hhp227.application.app.URLs;
import com.hhp227.application.dto.MemberItem;

import java.util.List;

public class MemberGridAdapter extends RecyclerView.Adapter<MemberGridAdapter.ItemHolder> {
    private Activity mActivity;
    private List<MemberItem> mMemberItems;
    private OnItemClickListener mOnItemClickListener;

    public MemberGridAdapter(Activity mActivity, List<MemberItem> mMemberItems) {
        this.mActivity = mActivity;
        this.mMemberItems = mMemberItems;
    }

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ItemHolder(LayoutInflater.from(mActivity).inflate(R.layout.item_member, parent, false));
    }

    @Override
    public void onBindViewHolder(ItemHolder holder, final int position) {
        MemberItem memberItem = mMemberItems.get(position);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null)
                    mOnItemClickListener.OnItemClick(v, position);
            }
        });
        Glide.with(mActivity)
                .load(URLs.URL_USER_PROFILE_IMAGE + memberItem.getProfile_img())
                .apply(RequestOptions.errorOf(R.drawable.profile_img_square))
                .into(holder.profileImage);
        holder.name.setText(memberItem.getName());
    }

    @Override
    public int getItemCount() {
        return mMemberItems.size();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    public static class ItemHolder extends RecyclerView.ViewHolder {
        private ImageView profileImage;
        private TextView name;

        public ItemHolder(View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profile_img);
            name = itemView.findViewById(R.id.tvname_user);
        }
    }

    public interface OnItemClickListener {
        void OnItemClick(View v, int position);
    }
}
