package com.example.mylife.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mylife.R;
import com.example.mylife.item.Comment;
import com.example.mylife.item.User;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class FollowAdapter extends RecyclerView.Adapter<FollowAdapter.FollowHolder>   {
    private final String TAG = "FollowAdapter";
    private final Context context;
    private final ArrayList<User> users;
    private final ItemClickListener itemClickListener;

    public FollowAdapter(Context context, ArrayList<User> users, ItemClickListener itemClickListener) {
        this.context = context;
        this.users = users;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public FollowAdapter.FollowHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_follow, parent, false);
        return new FollowHolder(view, itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull FollowAdapter.FollowHolder holder, int position) {
        Glide.with(context).load(users.get(position).getProfileImageUrl()).into(holder.ivProfile);
        holder.tvName.setText(users.get(position).getName());

        boolean isFollow = users.get(position).isFollow();
        if (isFollow) {
            holder.btnFollow.setVisibility(View.INVISIBLE);
            holder.btnFollow.setVisibility(View.VISIBLE);
        } else {
            holder.btnFollow.setVisibility(View.VISIBLE);
            holder.btnFollow.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return (users == null) ? 0 : users.size();
    }

    public class FollowHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        CircleImageView ivProfile;
        TextView tvName;
        Button btnFollow, btnUnfollow;

        public FollowHolder(@NonNull View itemView, ItemClickListener itemClickListener) {
            super(itemView);
            ivProfile = itemView.findViewById(R.id.iv_profile);
            tvName = itemView.findViewById(R.id.tv_name);
            btnFollow = itemView.findViewById(R.id.btn_follow);
            btnUnfollow = itemView.findViewById(R.id.btn_unfollow);

            ivProfile.setOnClickListener(this);
            tvName.setOnClickListener(this);
            btnFollow.setOnClickListener(this);
            btnUnfollow.setOnClickListener(this);
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
