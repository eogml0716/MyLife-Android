package com.example.mylife.item;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class ChatRoom {
    @SerializedName("chat_room_idx")
    private int chatRoomIdx;

    @SerializedName("chat_room_image_url")
    private String chatRoomImageUrl;

    @SerializedName("type")
    private String type;

    @SerializedName("open_type")
    private String openType;

    @SerializedName("chat_room_name")
    private String chatRoomName;

    @SerializedName("chat_room_member_count")
    private int chatRoomMemberCount;

    @SerializedName("last_message")
    private String lastMessage;

    @SerializedName("last_message_date")
    private String lastMessageDate;

    @SerializedName("chat_rooms")
    private ArrayList<ChatRoom> chatRooms;

    @SerializedName("create_date")
    private String createDate;

    @SerializedName("update_date")
    private String updateDate;

    public int getChatRoomIdx() {
        return chatRoomIdx;
    }

    public void setChatRoomIdx(int chatRoomIdx) {
        this.chatRoomIdx = chatRoomIdx;
    }

    public String getChatRoomImageUrl() {
        return chatRoomImageUrl;
    }

    public void setChatRoomImageUrl(String chatRoomImageUrl) {
        this.chatRoomImageUrl = chatRoomImageUrl;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getOpenType() {
        return openType;
    }

    public void setOpenType(String openType) {
        this.openType = openType;
    }

    public String getChatRoomName() {
        return chatRoomName;
    }

    public void setChatRoomName(String chatRoomName) {
        this.chatRoomName = chatRoomName;
    }

    public int getChatRoomMemberCount() {
        return chatRoomMemberCount;
    }

    public void setChatRoomMemberCount(int chatRoomMemberCount) {
        this.chatRoomMemberCount = chatRoomMemberCount;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getLastMessageDate() {
        return lastMessageDate;
    }

    public void setLastMessageDate(String lastMessageDate) {
        this.lastMessageDate = lastMessageDate;
    }

    public ArrayList<ChatRoom> getChatRooms() {
        return chatRooms;
    }

    public void setChatRooms(ArrayList<ChatRoom> chatRooms) {
        this.chatRooms = chatRooms;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public String getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(String updateDate) {
        this.updateDate = updateDate;
    }
}
