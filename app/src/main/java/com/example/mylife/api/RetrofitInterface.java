package com.example.mylife.api;

import com.example.mylife.item.User;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface RetrofitInterface {
    // 회원가입
    @FormUrlEncoded
    @POST("/signup")
    Call<User> signup(@Field("email") String email,
                      @Field("password") String password,
                      @Field("name") String name);

    // 검색

}
