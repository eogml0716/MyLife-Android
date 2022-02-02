package com.example.mylife.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.mylife.R;
import com.example.mylife.activity.LoginActivity;
import com.example.mylife.activity.SignUpActivity;
import com.example.mylife.item.Post;

import java.text.ParseException;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.mylife.util.NetworkConnection.TYPE_NOT_CONNECTED;

/**
 * 게시글 정보 가져와서 뿌려주는 Adapter
 *
 * 기능 (정확하지 않음)
 * 1. 홈 화면에서 게시글 뿌려주기
 * 2. 검색 화면에서 랜덤으로 게시글 뿌려주기
 * 3. 마이페이지에서 내가 작성한 게시글 뿌려주기
 */
public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostHolder>  {
    private final String TAG = "PostAdapter";
    private final Context context;
    private final ArrayList<Post> posts;
    private final ItemClickListener itemClickListener;
    private final RequestManager requestManager;

    public PostAdapter(Context context, ArrayList<Post> posts, ItemClickListener itemClickListener) {
        this.context = context;
        this.posts = posts;
        this.itemClickListener = itemClickListener;
        this.requestManager = Glide.with(context);
    }

    @NonNull
    @Override
    public PostHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new PostHolder(view, itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull PostHolder holder, int position) {
        Glide.with(context).load(posts.get(position).getProfileImageUrl()).into(holder.ivProfile);
        holder.tvName.setText(posts.get(position).getName());
        try {
            holder.tvUploadDate.setText(posts.get(position).getUpdateDate());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        requestManager.load(posts.get(position).getImageUrl()).listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                holder.pbLoading.setVisibility(View.GONE);
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                holder.pbLoading.setVisibility(View.GONE);
                return false;
            }
        }).into(holder.ivPost);
        holder.ivPost.requestLayout();

        // TODO: int값인데 String으로 자동으로 setText가 변환해줄 줄 알았는데 안해줌 String.valueOf 빼면 에러터짐
        holder.tvLikeCount.setText(String.valueOf(posts.get(position).getLikes()));
        holder.tvCommentCount.setText(String.valueOf(posts.get(position).getComments()));

        holder.cbHeart.setOnCheckedChangeListener(null);
        holder.cbHeart.setChecked(posts.get(position).isLike());
        holder.cbHeart.setOnCheckedChangeListener((buttonView, isChecked) -> posts.get(position).setLike(isChecked));

        holder.tvContents.setText(posts.get(position).getContents());
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public class PostHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        CircleImageView ivProfile;
        TextView tvName, tvUploadDate, tvLike, tvLikeCount, tvCommentCount, tvContents;
        ImageButton ibThreeDots;
        ImageView ivPost, ivComment, ivShare;
        CheckBox cbHeart;
        ProgressBar pbLoading;

        public PostHolder(@NonNull View itemView, ItemClickListener itemClickListener) {
            super(itemView);

            ivProfile = itemView.findViewById(R.id.iv_profile);
            tvName = itemView.findViewById(R.id.tv_name);
            tvUploadDate = itemView.findViewById(R.id.tv_upload_date);
            tvLike = itemView.findViewById(R.id.tv_like);
            tvLikeCount = itemView.findViewById(R.id.tv_like_count);
            tvCommentCount = itemView.findViewById(R.id.tv_comment_count);
            ibThreeDots = itemView.findViewById(R.id.ib_threedots);
            ivPost = itemView.findViewById(R.id.iv_post);
            ivComment = itemView.findViewById(R.id.iv_comment);
            ivShare = itemView.findViewById(R.id.iv_share);
            cbHeart = itemView.findViewById(R.id.cb_heart);
            tvContents = itemView.findViewById(R.id.tv_contents);
            pbLoading = itemView.findViewById(R.id.pb_loading);

            tvName.setOnClickListener(this);
            tvLike.setOnClickListener(this);
            ibThreeDots.setOnClickListener(this);
            ivComment.setOnClickListener(this);
            ivShare.setOnClickListener(this);
            cbHeart.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getBindingAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                itemClickListener.onClickItem(v, position);
            }
        }
    }
}
