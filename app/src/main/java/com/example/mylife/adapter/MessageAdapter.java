package com.example.mylife.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.example.mylife.R;
import com.example.mylife.item.Message;
import com.example.mylife.item.MessageLocation;
import com.example.mylife.item.Post;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final String TAG = "MessageAdapter";
    private final Context context;
    private final ArrayList<Message> messages;
    private final ItemClickListener itemClickListener;
    private final RequestManager requestManager;

    public MessageAdapter(Context context, ArrayList<Message> messages, ItemClickListener itemClickListener) {
        this.context = context;
        this.messages = messages;
        this.itemClickListener = itemClickListener;
        this.requestManager = Glide.with(context);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (Objects.requireNonNull(MessageLocation.of(viewType))) {
            case LEFT_CONTENTS:
                return new LeftViewHolder(inflater.inflate(R.layout.item_message_left, parent, false));
            case RIGHT_CONTENTS:
                return new RightViewHolder(inflater.inflate(R.layout.item_message_right, parent, false));
            case LEFT_IMAGE:
                return new LeftImageViewHolder(inflater.inflate(R.layout.item_image_left, parent, false));
            case RIGHT_IMAGE:
                return new RightImageViewHolder(inflater.inflate(R.layout.item_image_right, parent, false));
            default:
                return new CenterViewHolder(inflater.inflate(R.layout.item_message_center, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);
        if (holder instanceof LeftViewHolder) {
            Glide.with(context).load(message.getProfileImageUrl()).into(((LeftViewHolder) holder).ivProfile);
            ((LeftViewHolder) holder).tvName.setText(message.getName());
            ((LeftViewHolder) holder).tvMessage.setText(message.getContents());
            ((LeftViewHolder) holder).tvTime.setText(message.getCreateDate());
        } else if (holder instanceof RightViewHolder) {
            ((RightViewHolder) holder).tvMessage.setText(message.getContents());
            ((RightViewHolder) holder).tvTime.setText(message.getCreateDate());
        } else if (holder instanceof CenterViewHolder){
            ((CenterViewHolder) holder).tvMessage.setText(message.getContents());
        } else if (holder instanceof LeftImageViewHolder){
            Glide.with(context).load(message.getProfileImageUrl()).into(((LeftImageViewHolder) holder).ivProfile);
            ((LeftImageViewHolder) holder).tvName.setText(message.getName());
            Glide.with(context).load(message.getContents()).into(((LeftImageViewHolder) holder).ivMessage);
            ((LeftImageViewHolder) holder).tvTime.setText(message.getCreateDate());
        } else if (holder instanceof RightImageViewHolder){
            Glide.with(context).load(message.getContents()).into(((RightImageViewHolder) holder).ivMessage);
            ((RightImageViewHolder) holder).tvTime.setText(message.getCreateDate());
        }
    }

    @Override
    public int getItemCount() {
        return (messages == null) ? 0 : messages.size();
    }

    // 이 메소드를 재정의 하면 onCreateViewHolder 메소드의 두번째 파라미터 viewType 변수에 이 메소드의 리턴값이 들어간다.
    @Override
    public int getItemViewType(int position) {
        // enum 타입이라서 ordianl() 메소드의 순서값 가져옴
        // 0: 왼쪽화면, 1: 가운데 화면, 2: 오른쪽 화면
        return messages.get(position).getMessageLocation().getCode();
    }

    public class LeftViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener  {
        CircleImageView ivProfile;
        TextView tvName, tvMessage, tvTime;

        public LeftViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfile = itemView.findViewById(R.id.iv_profile);
            tvName = itemView.findViewById(R.id.tv_name);
            tvMessage = itemView.findViewById(R.id.tv_message);
            tvTime = itemView.findViewById(R.id.tv_time);

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

    public class LeftImageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener  {
        CircleImageView ivProfile;
        ImageView ivMessage;
        TextView tvName, tvTime;

        public LeftImageViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfile = itemView.findViewById(R.id.iv_profile);
            tvName = itemView.findViewById(R.id.tv_name);
            ivMessage = itemView.findViewById(R.id.iv_message);
            tvTime = itemView.findViewById(R.id.tv_time);

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


    public class CenterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvMessage;

        public CenterViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tv_message);
        }

        @Override
        public void onClick(View v) {
            int position = getBindingAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                itemClickListener.onClickItem(v, position);
            }
        }
    }

    public class RightViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvMessage, tvTime;

        public RightViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tv_message);
            tvTime = itemView.findViewById(R.id.tv_time);
        }

        @Override
        public void onClick(View v) {
            int position = getBindingAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                itemClickListener.onClickItem(v, position);
            }
        }
    }

    public class RightImageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView ivMessage;
        TextView tvTime;

        public RightImageViewHolder(@NonNull View itemView) {
            super(itemView);
            ivMessage = itemView.findViewById(R.id.iv_message);
            tvTime = itemView.findViewById(R.id.tv_time);
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
