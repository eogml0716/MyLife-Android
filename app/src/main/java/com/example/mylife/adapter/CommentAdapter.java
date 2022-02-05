package com.example.mylife.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
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
import com.example.mylife.item.Comment;
import com.example.mylife.item.Post;

import java.text.ParseException;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentHolder>   {
    private final String TAG = "PostAdapter";
    private final Context context;
    private final ArrayList<Comment> comments;
    private final ItemClickListener itemClickListener;

    public CommentAdapter(Context context, ArrayList<Comment> comments, ItemClickListener itemClickListener) {
        this.context = context;
        this.comments = comments;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public CommentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new CommentHolder(view, itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentHolder holder, int position) {
        Glide.with(context).load(comments.get(position).getProfileImageUrl()).into(holder.ivProfile);
        holder.tvName.setText(comments.get(position).getName());
        try {
            holder.tvUploadDate.setText(comments.get(position).getCreateDate());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // TODO: int값인데 String으로 자동으로 setText가 변환해줄 줄 알았는데 안해줌 String.valueOf 빼면 에러터짐
        holder.tvLikeCount.setText(String.valueOf(comments.get(position).getLikes()));

        holder.cbHeart.setOnCheckedChangeListener(null);
        holder.cbHeart.setChecked(comments.get(position).isLike());
        holder.cbHeart.setOnCheckedChangeListener((buttonView, isChecked) -> comments.get(position).setLike(isChecked));

        holder.tvContents.setText(comments.get(position).getContents());
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public class CommentHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        CircleImageView ivProfile;
        TextView tvName, tvUploadDate, tvLikeCount, tvContents;
        ImageButton ibThreeDots;
        CheckBox cbHeart;

        public CommentHolder(@NonNull View itemView, ItemClickListener itemClickListener) {
            super(itemView);

            ivProfile = itemView.findViewById(R.id.iv_profile);
            tvName = itemView.findViewById(R.id.tv_name);
            tvUploadDate = itemView.findViewById(R.id.tv_upload_date);
            tvLikeCount = itemView.findViewById(R.id.tv_like_count);
            ibThreeDots = itemView.findViewById(R.id.ib_threedots);
            cbHeart = itemView.findViewById(R.id.cb_heart);
            tvContents = itemView.findViewById(R.id.tv_contents);

            tvName.setOnClickListener(this);
            ibThreeDots.setOnClickListener(this);
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
