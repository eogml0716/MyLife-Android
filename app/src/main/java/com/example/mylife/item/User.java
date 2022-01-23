package com.example.mylife.item;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

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
    private String imageUrl; // 유저 프로필 이미지 url

    @SerializedName("about_me")
    private String aboutMe; // 유저 자기소개

    @SerializedName("create_date")
    private String createDate;

    @SerializedName("update_date")
    private String updateDate;

    @SerializedName("delete_date")
    private String deleteDate;

    public User(int userIdx, String email, String password, String name, String imageUrl, String aboutMe, String createDate, String updateDate, String deleteDate) {
        this.userIdx = userIdx;
        this.email = email;
        this.password = password;
        this.name = name;
        this.imageUrl = imageUrl;
        this.aboutMe = aboutMe;
        this.createDate = createDate;
        this.updateDate = updateDate;
        this.deleteDate = deleteDate;
    }

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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getAboutMe() {
        return aboutMe;
    }

    public void setAboutMe(String aboutMe) {
        this.aboutMe = aboutMe;
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
}
