package com.example.mylife.api;

import com.example.mylife.item.User;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface RetrofitInterface {
    /**
     * ------------------------------- category ?. 회원가입 / 로그인 -------------------------------
     */
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

    /**
     * ------------------------------- category ?. 게시글 -------------------------------
     */
    // 게시글 추가
    @Multipart
    @POST("community/post")
    Call<Void> uploadCommunityItem(@Header("Cookie") String session,
                                   @Part("userIdx") int user_info_idx,
                                   @Part MultipartBody.Part file);

}
