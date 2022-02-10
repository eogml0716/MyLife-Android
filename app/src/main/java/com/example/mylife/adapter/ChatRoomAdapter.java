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
import com.example.mylife.item.ChatRoom;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatRoomAdapter extends RecyclerView.Adapter<ChatRoomAdapter.ChatRoomHolder> {
    private final String TAG = "ChatRoomAdapter";
    private final Context context;
    private final ArrayList<ChatRoom> chatRooms;
    private final ItemClickListener itemClickListener;
    private final RequestManager requestManager;

    public ChatRoomAdapter(Context context, ArrayList<ChatRoom> chatRooms, ItemClickListener itemClickListener) {
        this.context = context;
        this.chatRooms = chatRooms;
        this.itemClickListener = itemClickListener;
        this.requestManager = Glide.with(context);
    }

    @NonNull
    @Override
    public ChatRoomAdapter.ChatRoomHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_room, parent, false);
        return new ChatRoomAdapter.ChatRoomHolder(view, itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatRoomAdapter.ChatRoomHolder holder, int position) {
        Glide.with(context).load(chatRooms.get(position).getChatRoomImageUrl()).into(holder.ivChatRoom);
        holder.tvChatRoomName.setText(chatRooms.get(position).getChatRoomName());
        holder.tvMembers.setText(String.valueOf(chatRooms.get(position).getChatRoomMemberCount()));
        holder.tvMessage.setText(chatRooms.get(position).getLastMessage());
        holder.tvTime.setText(chatRooms.get(position).getLastMessageDate());
    }

    @Override
    public int getItemCount() {
        return (chatRooms == null) ? 0 : chatRooms.size();
    }

    public class ChatRoomHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        CircleImageView ivChatRoom;
        TextView tvChatRoomName, tvMembers, tvMessage, tvTime;

        public ChatRoomHolder(@NonNull View itemView, ItemClickListener itemClickListener) {
            super(itemView);
            ivChatRoom = itemView.findViewById(R.id.iv_chat_room);
            tvChatRoomName = itemView.findViewById(R.id.tv_chat_room_name);
            tvMembers = itemView.findViewById(R.id.tv_members);
            tvMessage = itemView.findViewById(R.id.tv_message);
            tvTime = itemView.findViewById(R.id.tv_time);

            // TODO: 그냥 리사이클러뷰 아이템 자체를 클릭하면 해당 채팅방으로 넘어가는 방식으로 해야하나
            ivChatRoom.setOnClickListener(this);
            tvChatRoomName.setOnClickListener(this);
            tvMembers.setOnClickListener(this);
            tvMessage.setOnClickListener(this);
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
