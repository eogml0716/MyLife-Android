package com.example.mylife.item;

import android.annotation.SuppressLint;

import com.google.gson.annotations.SerializedName;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class Notification {
    @SerializedName("from_user_idx")
    private int fromUserIdx;

    @SerializedName("from_name")
    private String fromName;

    @SerializedName("from_profile_image_url")
    private String fromProfileImageUrl;

    @SerializedName("to_user_idx")
    private int toUserIdx;

    @SerializedName("to_name")
    private String toName;

    @SerializedName("to_profile_image_url")
    private String toProfileImageUrl;

    @SerializedName("notification_type")
    private String notificationType;

    @SerializedName("contents")
    private String contents;

    @SerializedName("table_type")
    private String tableType;

    @SerializedName("idx")
    private int idx;

    @SerializedName("create_date")
    private String createDate;

    @SerializedName("update_date")
    private String updateDate;

    @SerializedName("delete_date")
    private String deleteDate;

    @SerializedName("notifications")
    private ArrayList<Notification> notifications;


    // 날짜 변환
    @SuppressLint("SimpleDateFormat")
    public String customizeDate(String strDate) throws ParseException {
        final int SECOND_MILLIS = 1000;
        final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
        final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
        final int DAY_MILLIS = 24 * HOUR_MILLIS;
        final int WEEK_MILLIS = 7 * DAY_MILLIS;

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat newSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
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

    public int getFromUserIdx() {
        return fromUserIdx;
    }

    public void setFromUserIdx(int fromUserIdx) {
        this.fromUserIdx = fromUserIdx;
    }

    public String getFromName() {
        return fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public String getFromProfileImageUrl() {
        return fromProfileImageUrl;
    }

    public void setFromProfileImageUrl(String fromProfileImageUrl) {
        this.fromProfileImageUrl = fromProfileImageUrl;
    }

    public int getToUserIdx() {
        return toUserIdx;
    }

    public void setToUserIdx(int toUserIdx) {
        this.toUserIdx = toUserIdx;
    }

    public String getToName() {
        return toName;
    }

    public void setToName(String toName) {
        this.toName = toName;
    }

    public String getToProfileImageUrl() {
        return toProfileImageUrl;
    }

    public void setToProfileImageUrl(String toProfileImageUrl) {
        this.toProfileImageUrl = toProfileImageUrl;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public String getTableType() {
        return tableType;
    }

    public void setTableType(String tableType) {
        this.tableType = tableType;
    }

    public int getIdx() {
        return idx;
    }

    public void setIdx(int idx) {
        this.idx = idx;
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

    public ArrayList<Notification> getNotifications() {
        return notifications;
    }

    public void setNotifications(ArrayList<Notification> notifications) {
        this.notifications = notifications;
    }
}
