package com.example.mylife.item;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("userIdx")
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

    @SerializedName("imageUrl")
    @Expose
    private String imageUrl; // 유저 프로필 이미지 url

    @SerializedName("aboutMe")
    @Expose
    private String aboutMe; // 유저 자기소개

    @SerializedName("createDate")
    @Expose
    private String createDate;

    @SerializedName("updateDate")
    @Expose
    private String updateDate;

    @SerializedName("deleteDate")
    @Expose
    private String deleteDate;

    public int getUserIdx() {
        return userIdx;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getAboutMe() {
        return aboutMe;
    }

    public String getCreateDate() {
        return createDate;
    }

    public String getUpdateDate() {
        return updateDate;
    }

    public String getDeleteDate() {
        return deleteDate;
    }

}
