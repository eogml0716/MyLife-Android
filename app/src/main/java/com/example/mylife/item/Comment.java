package com.example.mylife.item;

import android.annotation.SuppressLint;

import com.google.gson.annotations.SerializedName;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class Comment {
    @SerializedName("comment_idx")
    private int commentIdx;

    @SerializedName("board_idx")
    private int boardIdx;

    @SerializedName("user_idx")
    private int userIdx;

    @SerializedName("name")
    private String name;

    @SerializedName("profile_image_url")
    private String profileImageUrl;

    @SerializedName("contents")
    private String contents;

    @SerializedName("likes")
    private int likes;

    @SerializedName("create_date")
    private String createDate;

    @SerializedName("update_date")
    private String updateDate;

    @SerializedName("delete_date")
    private String deleteDate;

    // 컨텐츠 아이템 리스트
    @SerializedName("comments")
    ArrayList<Comment> comments;

    @SerializedName("is_like")
    private boolean isLike = false;

    // 날짜 변환
    @SuppressLint("SimpleDateFormat")
    public String customizeDate(String strDate) throws ParseException {
        int SECOND_MILLIS = 1000;
        int MINUTE_MILLIS = 60 * SECOND_MILLIS;
        int HOUR_MILLIS = 60 * MINUTE_MILLIS;
        int DAY_MILLIS = 24 * HOUR_MILLIS;
        int WEEK_MILLIS = 7 * DAY_MILLIS;

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        SimpleDateFormat newSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date date = simpleDateFormat.parse(strDate);
        Date now = Calendar.getInstance().getTime();
        assert date != null;
        final long diff = now.getTime() - date.getTime();

        if (diff < SECOND_MILLIS) {
            return "지금";
        } else if (diff < MINUTE_MILLIS) {
            return diff / SECOND_MILLIS + "초 전";
        } else if (diff < 2 * MINUTE_MILLIS) {
            return "1분 전";
        } else if (diff < 59 * MINUTE_MILLIS) {
            return diff / MINUTE_MILLIS + "분 전";
        } else if (diff < 90 * MINUTE_MILLIS) {
            return "1시간 전";
        } else if (diff < 24 * HOUR_MILLIS) {
            return diff / HOUR_MILLIS + "시간 전";
        } else if (diff < 48 * HOUR_MILLIS) {
            return "어제";
        } else if (diff < 6 * DAY_MILLIS) {
            return diff / DAY_MILLIS + "일 전";
        } else if (diff < 11 * DAY_MILLIS) {
            return "1주 전";
        } else {
            return diff / WEEK_MILLIS + "주 전";
        }
    }

    public int getCommentIdx() {
        return commentIdx;
    }

    public void setCommentIdx(int commentIdx) {
        this.commentIdx = commentIdx;
    }

    public int getBoardIdx() {
        return boardIdx;
    }

    public void setBoardIdx(int boardIdx) {
        this.boardIdx = boardIdx;
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

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public String getCreateDate() throws ParseException {
        return customizeDate(createDate);
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

    public ArrayList<Comment> getComments() {
        return comments;
    }

    public void setComments(ArrayList<Comment> comments) {
        this.comments = comments;
    }

    public boolean isLike() {
        return isLike;
    }

    public void setLike(boolean like) {
        isLike = like;
    }

}
