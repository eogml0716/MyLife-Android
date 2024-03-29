package com.example.mylife.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mylife.R;
import com.example.mylife.item.User;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * 유저 정보 가져와서 뿌려주는 Adapter
 *
 * 기능
 * 1. TODO: 유저 검색 시 사용
 */
public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserHolder>   {
    private final String TAG = "UserAdapter";
    private final Context context;
    private final ArrayList<User> users;
    private final ItemClickListener itemClickListener;

    public UserAdapter(Context context, ArrayList<User> users, ItemClickListener itemClickListener) {
        this.context = context;
        this.users = users;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public UserAdapter.UserHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserHolder(view, itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull UserAdapter.UserHolder holder, int position) {
        Glide.with(context).load(users.get(position).getProfileImageUrl()).into(holder.ivProfile);
        holder.tvName.setText(users.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return (users == null) ? 0 : users.size();
    }

    public class UserHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        CircleImageView ivProfile;
        TextView tvName;

        public UserHolder(@NonNull View itemView, ItemClickListener itemClickListener) {
            super(itemView);
            ivProfile = itemView.findViewById(R.id.iv_profile);
            tvName = itemView.findViewById(R.id.tv_name);

            ivProfile.setOnClickListener(this);
            tvName.setOnClickListener(this);
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
