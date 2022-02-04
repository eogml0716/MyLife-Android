package com.example.mylife.item;

import android.annotation.SuppressLint;

import com.google.gson.annotations.SerializedName;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class Post {
    @SerializedName("board_idx")
    private int boardIdx;

    @SerializedName("user_idx")
    private int userIdx;

    @SerializedName("name")
    private String name;

    @SerializedName("profile_image_url")
    private String profileImageUrl;

    // TODO: 다중 이미지 일 때는 변경 예정
    @SerializedName("image_url")
    private String imageUrl;

    @SerializedName("contents")
    private String contents;

    @SerializedName("likes")
    private int likes;

    @SerializedName("comments")
    private int comments;

    @SerializedName("create_date")
    private String createDate;

    @SerializedName("update_date")
    private String updateDate;

    @SerializedName("delete_date")
    private String deleteDate;

    // 컨텐츠 아이템 리스트
    @SerializedName("posts")
    ArrayList<Post> posts;

    // TODO: liked_idx 값을 통해서 좋아요 등록, 삭제를 구현하려고 했는데 이게 지속적으로 변화하는 값이고 에러가 계속 터져서 좋아요 구현 방법을 변경함, 나중에 시간나면 에러 터지는 이유 찾기

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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
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

    public int getComments() {
        return comments;
    }

    public void setComments(int comments) {
        this.comments = comments;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public String getUpdateDate() throws ParseException {
        return customizeDate(updateDate);
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

    public ArrayList<Post> getPosts() {
        return posts;
    }

    public void setPosts(ArrayList<Post> posts) {
        this.posts = posts;
    }

    public boolean isLike() {
        return isLike;
    }

    public void setLike(boolean like) {
        isLike = like;
    }
}
