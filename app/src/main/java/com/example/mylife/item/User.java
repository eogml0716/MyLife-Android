package com.example.mylife.item;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class User {
    @SerializedName("user_idx")
    private int userIdx;

    @SerializedName("email")
    private String email;

    @SerializedName("password")
    private String password;

    @SerializedName("name")
    private String name; // 사용자명

    @SerializedName("profile_image_url")
    private String profileImageUrl; // 유저 프로필 이미지 url

    @SerializedName("about_me")
    private String aboutMe; // 유저 자기소개

    @SerializedName("post_count")
    private int postCount; // 게시글 개수, TODO: posts라고 ArrayList에 이름을 지정을 해놔서 좀 복잡해졌네

    @SerializedName("follower_count")
    private int followerCount; // 팔로워 수

    @SerializedName("following_count")
    private int followingCount; // 팔로잉 수

    @SerializedName("from_user_idx")
    private int fromUserIdx;

    @SerializedName("to_user_idx")
    private int toUserIdx;

    // 해당 유저와 채팅방이 만들어져있는 지 확인하는 용도
    @SerializedName("chat_room_idx")
    private int chatRoomIdx;

    // TODO: 따로 Follow 아이템 클래스 하나 파서 따로 빼야하나? 안해도 될 거 같긴함
    @SerializedName("followings")
    private ArrayList<User> followings;

    // TODO: 따로 Follow 아이템 클래스 하나 파서 따로 빼야하나? 안해도 될 거 같긴함
    @SerializedName("followers")
    private ArrayList<User> followers;

    @SerializedName("firebase_token")
    private String firebaseToken;

    @SerializedName("is_follow")
    private boolean isFollow;

    @SerializedName("create_date")
    private String createDate;

    @SerializedName("update_date")
    private String updateDate;

    @SerializedName("delete_date")
    private String deleteDate;

    // TODO: 따로 Follow 아이템 클래스 하나 파서 따로 빼야하나? 안해도 될 거 같긴함
    @SerializedName("users")
    private ArrayList<User> users;

    public int getUserIdx() {
        return userIdx;
    }

    public void setUserIdx(int userIdx) {
        this.userIdx = userIdx;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public String getAboutMe() {
        return aboutMe;
    }

    public void setAboutMe(String aboutMe) {
        this.aboutMe = aboutMe;
    }

    public int getPostCount() {
        return postCount;
    }

    public void setPostCount(int postCount) {
        this.postCount = postCount;
    }

    public int getFollowerCount() {
        return followerCount;
    }

    public void setFollowerCount(int followerCount) {
        this.followerCount = followerCount;
    }

    public int getFollowingCount() {
        return followingCount;
    }

    public void setFollowingCount(int followingCount) {
        this.followingCount = followingCount;
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

    public int getFromUserIdx() {
        return fromUserIdx;
    }

    public void setFromUserIdx(int fromUserIdx) {
        this.fromUserIdx = fromUserIdx;
    }

    public int getToUserIdx() {
        return toUserIdx;
    }

    public void setToUserIdx(int toUserIdx) {
        this.toUserIdx = toUserIdx;
    }

    public int getChatRoomIdx() {
        return chatRoomIdx;
    }

    public void setChatRoomIdx(int chatRoomIdx) {
        this.chatRoomIdx = chatRoomIdx;
    }

    public ArrayList<User> getFollowings() {
        return followings;
    }

    public void setFollowings(ArrayList<User> followings) {
        this.followings = followings;
    }

    public ArrayList<User> getFollowers() {
        return followers;
    }

    public void setFollowers(ArrayList<User> followers) {
        this.followers = followers;
    }

    public boolean isFollow() {
        return isFollow;
    }

    public void setFollow(boolean follow) {
        isFollow = follow;
    }

    public ArrayList<User> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<User> users) {
        this.users = users;
    }

    public String getFirebaseToken() {
        return firebaseToken;
    }

    public void setFirebaseToken(String firebaseToken) {
        this.firebaseToken = firebaseToken;
    }
}
