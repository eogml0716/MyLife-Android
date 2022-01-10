package com.example.mylife.item;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * 유저 불러오는 모델
 *
 * - 유저 검색 기능에 활용
 */
public class Users {
    @SerializedName("items")
    @Expose
    List<User> items = null;

    @SerializedName("totalItems")
    @Expose
    int totalItems;

    public List<User> getItems() {
        return items;
    }

    public int getTotalItems() {
        return totalItems;
    }
}
