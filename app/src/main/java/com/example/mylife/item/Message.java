package com.example.mylife.item;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class Message {
    @SerializedName("message_idx")
    private int messageIdx;

    @SerializedName("chat_room_idx")
    private int chatRoomIdx;

    @SerializedName("user_idx")
    private int userIdx;

    @SerializedName("name")
    private String name;

    @SerializedName("profile_image_url")
    private String profileImageUrl;

    @SerializedName("message_type")
    private String messageType;

    @SerializedName("contents")
    private String contents;

    @SerializedName("create_date")
    private String createDate;

    @SerializedName("update_date")
    private String updateDate;

    @SerializedName("delete_date")
    private String deleteDate;

    @SerializedName("messages")
    private ArrayList<Message> messages;

    private MessageLocation messageLocation;

    public Message(int messageIdx, int chatRoomIdx, int userIdx, String name, String profileImageUrl, String messageType, String contents, String createDate, String updateDate, MessageLocation messageLocation) {
        this.messageIdx = messageIdx;
        this.chatRoomIdx = chatRoomIdx;
        this.userIdx = userIdx;
        this.name = name;
        this.profileImageUrl = profileImageUrl;
        this.messageType = messageType;
        this.contents = contents;
        this.createDate = createDate;
        this.updateDate = updateDate;
        this.messageLocation = messageLocation;
    }

    public int getMessageIdx() {
        return messageIdx;
    }

    public void setMessageIdx(int messageIdx) {
        this.messageIdx = messageIdx;
    }

    public int getChatRoomIdx() {
        return chatRoomIdx;
    }

    public void setChatRoomIdx(int chatRoomIdx) {
        this.chatRoomIdx = chatRoomIdx;
    }

    public int getUserIdx() {
        return userIdx;
    }

    public void setUserIdx(int userIdx) {
        this.userIdx = userIdx;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
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

    public String getDeleteDate() {
        return deleteDate;
    }

    public void setDeleteDate(String deleteDate) {
        this.deleteDate = deleteDate;
    }

    public ArrayList<Message> getMessages() {
        return messages;
    }

    public void setMessages(ArrayList<Message> messages) {
        this.messages = messages;
    }

    public MessageLocation getMessageLocation() {
        return messageLocation;
    }

    public void setMessageLocation(MessageLocation messageLocation) {
        this.messageLocation = messageLocation;
    }
}