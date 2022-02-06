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
import com.example.mylife.item.Post;

import java.text.ParseException;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class SquarePostAdapter extends RecyclerView.Adapter<SquarePostAdapter.SquarePostHolder> {
    private final String TAG = "SquarePostAdapter";
    private final Context context;
    private final ArrayList<Post> posts;
    private final ItemClickListener itemClickListener;
    private final RequestManager requestManager;

    public SquarePostAdapter(Context context, ArrayList<Post> posts, ItemClickListener itemClickListener) {
        this.context = context;
        this.posts = posts;
        this.itemClickListener = itemClickListener;
        this.requestManager = Glide.with(context);
    }

    @NonNull
    @Override
    public SquarePostHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_square_post, parent, false);
        return new SquarePostHolder(view, itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull SquarePostHolder holder, int position) {
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
        }).into(holder.ivSquarePost);
        holder.ivSquarePost.requestLayout();
    }

    @Override
    public int getItemCount() {
        return (posts == null) ? 0 : posts.size();
    }

    public class SquarePostHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView ivSquarePost;
        ProgressBar pbLoading;

        public SquarePostHolder(@NonNull View itemView, ItemClickListener itemClickListener) {
            super(itemView);
            ivSquarePost = itemView.findViewById(R.id.iv_square_post);
            pbLoading = itemView.findViewById(R.id.pb_loading);

            ivSquarePost.setOnClickListener(this);
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
