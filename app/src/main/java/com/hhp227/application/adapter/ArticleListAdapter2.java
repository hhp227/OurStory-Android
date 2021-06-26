package com.hhp227.application.adapter;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.*;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.hhp227.application.R;
import com.hhp227.application.app.AppController;
import com.hhp227.application.app.URLs;
import com.hhp227.application.dto.PostItem;
import com.hhp227.application.util.Utils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArticleListAdapter2 extends RecyclerView.Adapter {
    private static final int TYPE_ARTICLE = 0;

    private static final int TYPE_LOADER = 1;

    private static final int CONTENT_MAX_LINE = 4;

    private static final String TAG = "좋아요";

    private static boolean liked;

    private int mVisibility;

    private Activity mActivity;

    private List<Object> articleItems;

    private OnItemClickListener mOnItemClickListener;

    public ArticleListAdapter2(Activity mActivity, List<Object> articleItems) {
        this.mActivity = mActivity;
        this.articleItems = articleItems;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_ARTICLE:
                View itemView = LayoutInflater.from(mActivity).inflate(R.layout.item_post, parent, false);
                return new ItemHolder(itemView);
            case TYPE_LOADER:
                View footerView = LayoutInflater.from(mActivity).inflate(R.layout.load_more, parent, false);
                return new FooterHolder(footerView);
        }
        throw new NullPointerException();
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof ItemHolder) {
            PostItem postItem = (PostItem) articleItems.get(position);
            ((ItemHolder) holder).cardView.setOnClickListener(v -> {
                if (mOnItemClickListener != null)
                    mOnItemClickListener.onItemClick(v, position);
            });
            ((ItemHolder) holder).name.setText(postItem.getName());
            ((ItemHolder) holder).timestamp.setText(Utils.getPeriodTimeGenerator(mActivity, postItem.getTimeStamp()));
            if (!TextUtils.isEmpty(postItem.getText())) {
                ((ItemHolder) holder).contents.setText(postItem.getText());
                ((ItemHolder) holder).contents.setMaxLines(CONTENT_MAX_LINE);
                ((ItemHolder) holder).contents.setVisibility(View.VISIBLE);
            } else
                ((ItemHolder) holder).contents.setVisibility(View.GONE);
            ((ItemHolder) holder).contentsMore.setVisibility(!TextUtils.isEmpty(postItem.getText()) && ((ItemHolder) holder).contents.getLineCount() > CONTENT_MAX_LINE ? View.VISIBLE : View.GONE);
            Glide.with(mActivity)
                    .load(postItem.getProfileImage() != null ? URLs.URL_USER_PROFILE_IMAGE + postItem.getProfileImage() : null)
                    .apply(RequestOptions.errorOf(R.drawable.profile_img_circle).circleCrop())
                    .into(((ItemHolder) holder).profilePic);

            if (!postItem.getImageItemList().isEmpty()) {
                ((ItemHolder) holder).articleImageView.setVisibility(View.VISIBLE);
                Glide.with(mActivity)
                        .load(URLs.URL_POST_IMAGE_PATH + postItem.getImageItemList().get(0).image)
                        .apply(RequestOptions.errorOf(R.drawable.ic_launcher))
                        .transition(DrawableTransitionOptions.withCrossFade(150))
                        .into(((ItemHolder) holder).articleImageView);
            } else
                ((ItemHolder) holder).articleImageView.setVisibility(View.GONE);
            ((ItemHolder) holder).replycount.setText(String.valueOf(postItem.getReplyCount()));

            // 댓글 버튼을 누르면 댓글쓰는곳으로 이동
            ((ItemHolder) holder).replybutton.setTag(position);
            ((ItemHolder) holder).replybutton.setOnClickListener(v -> {
                if (mOnItemClickListener != null)
                    mOnItemClickListener.onItemClick(v, position);
            });
            ((ItemHolder) holder).likecount.setText(String.valueOf(postItem.getLikeCount()));
            ((ItemHolder) holder).likecount.setVisibility(postItem.getLikeCount() == 0 ? View.GONE : View.VISIBLE);
            ((ItemHolder) holder).favorites.setVisibility(postItem.getLikeCount() == 0 ? View.GONE : View.VISIBLE);

            // 좋아요 버튼을 누르면 일어나는 동작
            ((ItemHolder) holder).likebutton.setTag(position);
            ((ItemHolder) holder).likebutton.setOnClickListener(v -> {
                final PostItem item = (PostItem) articleItems.get(position);
                String tag_string_req = "req_delete";
                String URL_FEED_LIKE = URLs.URL_POST_LIKE.replace("{POST_ID}", String.valueOf(item.getId()));
                StringRequest strReq = new StringRequest(Request.Method.GET, URL_FEED_LIKE, response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        boolean error = jsonObject.getBoolean("error");
                        if (!error) {
                            String result = jsonObject.getString("result");
                            item.setLikeCount(result.equals("insert") ? item.getLikeCount() + 1 : item.getLikeCount() - 1);
                            articleItems.set(position, item);
                            notifyDataSetChanged(); // 데이터가 변경될때마다 화면을 새로고침
                        } else {
                            // 삭제에서 에러 발생. 에러 내용
                            String errorMsg = jsonObject.getString("message");
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "에러" + e);
                    }
                }, error -> VolleyLog.d(TAG, "에러 : " + error.getMessage())) {
                    String apikey = AppController.Companion.getInstance().getPreferenceManager().getUser().getApiKey();
                    @Override
                    public Map<String, String> getHeaders() {
                        HashMap<String, String> headers = new HashMap<>();
                        headers.put("Authorization", apikey);
                        return headers;
                    }
                };
                // 큐를 요청하는 요청을 추가
                AppController.Companion.getInstance().addToRequestQueue(strReq, tag_string_req);
            });
        } else if (holder instanceof FooterHolder) {
            ((FooterHolder) holder).progressBar.setVisibility(mVisibility);
            ((FooterHolder) holder).textView.setVisibility(mVisibility);
        }
    }

    @Override
    public int getItemCount() {
        return articleItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        return articleItems.get(position) instanceof PostItem ? TYPE_ARTICLE : TYPE_LOADER;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    public void addFooterView(Object object) {
        articleItems.add(object);
    }

    public void setLoaderVisibility(int visibility) {
        this.mVisibility = visibility;
    }

    public static class ItemHolder extends RecyclerView.ViewHolder {
        private CardView cardView;

        private TextView name, timestamp, contents, contentsMore, replycount, likecount;

        private ImageView profilePic, articleImageView, favorites;

        private LinearLayout replybutton, likebutton;

        public ItemHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            profilePic = itemView.findViewById(R.id.ivProfileImage);
            name = itemView.findViewById(R.id.tvName);
            timestamp = itemView.findViewById(R.id.tvCreateAt);
            contents = itemView.findViewById(R.id.tvText);
            contentsMore = itemView.findViewById(R.id.tvTextMore);
            articleImageView = itemView.findViewById(R.id.ivPost);
            replycount = itemView.findViewById(R.id.replyCount);
            replybutton = itemView.findViewById(R.id.llReply);
            likecount = itemView.findViewById(R.id.likeCount);
            likebutton = itemView.findViewById(R.id.llLike);
            favorites = itemView.findViewById(R.id.ivFavorites);
        }
    }

    public static class FooterHolder extends RecyclerView.ViewHolder {
        private ProgressBar progressBar;
        private TextView textView;

        FooterHolder(View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.pb_more);
            textView = itemView.findViewById(R.id.tv_list_footer);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View v, int position);
    }
}
