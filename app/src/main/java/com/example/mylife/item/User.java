package com.example.mylife.item;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("user_idx")
    @Expose
    private int userIdx;

    @SerializedName("email")
    @Expose
    private String email;

    @SerializedName("password")
    @Expose
    private String password;

    @SerializedName("name")
    @Expose
    private String name; // 사용자명

    @SerializedName("profile_image_url")
    @Expose
    private String imageUrl; // 유저 프로필 이미지 url

    @SerializedName("about_me")
    @Expose
    private String aboutMe; // 유저 자기소개

    @SerializedName("create_date")
    @Expose
    private String createDate;

    @SerializedName("update_date")
    @Expose
    private String updateDate;

    @SerializedName("delete_date")
    @Expose
    private String deleteDate;

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
