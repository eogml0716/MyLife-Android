package com.example.mylife.api;

import com.example.mylife.item.Comment;
import com.example.mylife.item.Post;
import com.example.mylife.item.User;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

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
    Call<User> signinGeneral(@Field("email") String email,
                            @Field("password") String password);

    // TODO: 자동 로그인

    // TODO: 네이버 로그인

    // TODO: 카카오 로그인

    // TODO: 로그아웃

    /**
     * ------------------------------- category ?. 게시글 -------------------------------
     */
    // TODO: 게시글 리스트 가져오기 (무한 스크롤링)
    @GET("/read/posts")
    Call<Post> readPosts();
    // TODO: 댓글 리스트 가져오기 (무한 스크롤링)
    @GET("/read/comments")
    Call<Comment> readComments();

    // TODO: 게시글 가져오기 (1개)
    @GET("/read/post")
    Call<Post> readPost();

    // TODO: 댓글 가져오기 (1개)
    @GET("/read/comment")
    Call<Comment> readComment();

    // 게시글 추가
    @FormUrlEncoded
    @POST("/create/post")
    Call<Void> createPost(@Header("Cookie") String session,
                          @Field("user_idx") int userIdx,
                          @Field("image") String image,
                          @Field("image_name") String imageName,
                          @Field("contents") String contents);

    // TODO: 댓글 추가
    @FormUrlEncoded
    @POST("/create/comment")
    Call<Void> createComment();

    // TODO: 게시글 수정
    @FormUrlEncoded
    @HTTP(method = "PUT", path = "/update/post", hasBody = true)
    Call<Post> updatePost();

    // TODO: 댓글 수정
    @FormUrlEncoded
    @HTTP(method = "PUT", path = "/update/comment", hasBody = true)
    Call<Comment> updateComment();

    // TODO: 게시글 삭제
    @FormUrlEncoded
    @HTTP(method = "DELETE", path = "/delete/post", hasBody = true)
    Call<Void> deletePost();

    // TODO: 댓글 삭제
    @FormUrlEncoded
    @HTTP(method = "DELETE", path = "/delete/comment", hasBody = true)
    Call<Void> deleteComment();

    // TODO: 게시글 좋아요
    @FormUrlEncoded
    @HTTP(method = "PUT", path = "/update/like/post", hasBody = true)
    Call<Void> updateLikePost();

    // TODO: 댓글 좋아요
    @FormUrlEncoded
    @HTTP(method = "PUT", path = "/update/like/comment", hasBody = true)
    Call<Void> updateLikeComment();
}
