package com.example.mylife.api;

import com.example.mylife.item.User;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface RetrofitInterface {
    // 회원가입
    @FormUrlEncoded
    @POST("/signup")
    Call<User> signup(@Field("email") String email,
                      @Field("password") String password,
                      @Field("name") String name);

    // 일반 로그인
    @FormUrlEncoded
    @POST("/signin/general")
    Call<User> generalLogin(@Field("email") String email,
                            @Field("password") String password);

    // TODO: 자동 로그인

    // TODO: 네이버 로그인

    // TODO: 카카오 로그인

    // TODO: 로그아웃

}
