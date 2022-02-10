package com.example.mylife.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.example.mylife.R;
import com.example.mylife.item.Notification;

import java.text.ParseException;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationHolder> {
    private final String TAG = "NotificationAdapter";
    private final Context context;
    private final ArrayList<Notification> notifications;
    private final ItemClickListener itemClickListener;
    private final RequestManager requestManager;

    public NotificationAdapter(Context context, ArrayList<Notification> notifications, ItemClickListener itemClickListener) {
        this.context = context;
        this.notifications = notifications;
        this.itemClickListener = itemClickListener;
        this.requestManager = Glide.with(context);
    }

    @NonNull
    @Override
    public NotificationHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new NotificationHolder(view, itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationHolder holder, int position) {
        // 알림을 보낸 사람의 값을 받아와서 보여줘야하니까 from에 해당하는 값을 가져온다.
        Glide.with(context).load(notifications.get(position).getFromProfileImageUrl()).into(holder.ivProfile);
        holder.tvName.setText(notifications.get(position).getFromName());
        try {
            holder.tvUploadDate.setText(notifications.get(position).getCreateDate());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        holder.tvContents.setText(notifications.get(position).getContents());
    }

    @Override
    public int getItemCount() {
        return (notifications == null) ? 0 : notifications.size();
    }

    public class NotificationHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        CircleImageView ivProfile;
        TextView tvName, tvUploadDate, tvContents;

        public NotificationHolder(@NonNull View itemView, ItemClickListener itemClickListener) {
            super(itemView);

            ivProfile = itemView.findViewById(R.id.iv_profile);
            tvName = itemView.findViewById(R.id.tv_name);
            tvUploadDate = itemView.findViewById(R.id.tv_upload_date);
            tvContents = itemView.findViewById(R.id.tv_contents);

            ivProfile.setOnClickListener(this);
            tvName.setOnClickListener(this);
            tvContents.setOnClickListener(this);
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
