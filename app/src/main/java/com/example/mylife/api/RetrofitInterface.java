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

    // 자동 로그인
    @FormUrlEncoded
    @POST("/signin/auto")
    Call<User> signinAuto(@Header("Cookie") String session,
                          @Field("user_idx") int userIdx);

    // TODO: 네이버 로그인

    // TODO: 카카오 로그인

    // 로그아웃 - signin, signout 이런 걸로 할거면 하고 login, logout 이런 걸로 할거면 이런걸로 하고...
    @FormUrlEncoded
    @HTTP(method = "DELETE", path = "/signout", hasBody = true)
    Call<User> logout(@Header("Cookie") String session,
                      @Field("user_idx") int userIdx);

    /**
     * ------------------------------- category ?. 게시글 / 댓글 -------------------------------
     */
    // 게시글 리스트 가져오기 (무한 스크롤링)
    @GET("/read/posts")
    Call<Post> readPosts(@Header("Cookie") String session,
                         @Query("user_idx") int userIdx,
                         @Query("page") int page,
                         @Query("limit") int limit);

    // 댓글 리스트 가져오기 (무한 스크롤링)
    @GET("/read/comments")
    Call<Comment> readComments(@Header("Cookie") String session,
                               @Query("user_idx") int userIdx,
                               @Query("page") int page,
                               @Query("limit") int limit,
                               @Query("board_idx") int board_idx);

    // 게시글 가져오기 (1개)
    @GET("/read/post")
    Call<Post> readPost(@Header("Cookie") String session,
                        @Query("user_idx") int userIdx,
                        @Query("board_idx") int board_idx);

    // 게시글 추가
    @FormUrlEncoded
    @POST("/create/post")
    Call<Void> createPost(@Header("Cookie") String session,
                          @Field("user_idx") int userIdx,
                          @Field("image") String image,
                          @Field("image_name") String imageName,
                          @Field("contents") String contents);

    // 댓글 추가
    @FormUrlEncoded
    @POST("/create/comment")
    Call<Void> createComment(@Header("Cookie") String session,
                             @Field("user_idx") int userIdx,
                             @Field("board_idx") int boardIdx,
                             @Field("contents") String contents);

    // 게시글 수정
    @FormUrlEncoded
    @HTTP(method = "PUT", path = "/update/post", hasBody = true)
    Call<Post> updatePost(@Header("Cookie") String session,
                          @Field("board_idx") int boardIdx,
                          @Field("user_idx") int userIdx,
                          @Field("image") String image,
                          @Field("image_name") String imageName,
                          @Field("contents") String contents,
                          @Field("is_image_change") boolean isImageChange);

    // 댓글 수정
    @FormUrlEncoded
    @HTTP(method = "PUT", path = "/update/comment", hasBody = true)
    Call<Comment> updateComment(@Header("Cookie") String session,
                                @Field("user_idx") int userIdx,
                                @Field("board_idx") int boardIdx,
                                @Field("comment_idx") int commentIdx,
                                @Field("contents") String contents);

    // 게시글 삭제
    @FormUrlEncoded
    @HTTP(method = "DELETE", path = "/delete/post", hasBody = true)
    Call<Void> deletePost(@Header("Cookie") String session,
                          @Field("user_idx") int userIdx,
                          @Field("board_idx") int boardIdx);

    // 댓글 삭제
    @FormUrlEncoded
    @HTTP(method = "DELETE", path = "/delete/comment", hasBody = true)
    Call<Void> deleteComment(@Header("Cookie") String session,
                             @Field("user_idx") int userIdx,
                             @Field("comment_idx") int commentIdx);

    // 게시글 좋아요, TODO: type값 나중에 삭제하기(?) <- 체크해보고.. 일단 구현하기
    @FormUrlEncoded
    @HTTP(method = "PUT", path = "/update/like", hasBody = true)
    Call<Post> updateLikePost(@Header("Cookie") String session,
                              @Field("user_idx") int userIdx,
                              @Field("type") String type,
                              @Field("idx") int idx,
                              @Field("is_like") boolean isLike);

    // 댓글 좋아요, TODO: type값 나중에 삭제하기(?) <- 체크해보고.. 일단 구현하기
    @FormUrlEncoded
    @HTTP(method = "PUT", path = "/update/like", hasBody = true)
    Call<Comment> updateLikeComment(@Header("Cookie") String session,
                                    @Field("user_idx") int userIdx,
                                    @Field("type") String type,
                                    @Field("idx") int idx,
                                    @Field("is_like") boolean isLike);

    /**
     * ------------------------------- category ?. 검색 탭 -------------------------------
     */
    // TODO: 유저 검색

    // TODO: 게시글 랜덤으로 가져오기 (무한 스크롤링)

    /**
     * ------------------------------- category ?. 알림 탭 관련 -------------------------------
     */
    // TODO: 알림 추가(?)

    // TODO: 알림 가져오기 (무한 스크롤링)

    /**
     * ------------------------------- category ?. 마이페이지 -------------------------------
     */
    // TODO: 내가 작성한 게시글 리스트 가져오기 (무한 스크롤링) - 그냥 readPosts랑 합쳐도 될 거 같긴함
    @GET("/read/profile/mine")
    Call<Post> readMyPosts(@Header("Cookie") String session,
                           @Query("user_idx") int userIdx,
                           @Query("page") int page,
                           @Query("limit") int limit);

    // TODO: 다른 사람이 작성한 게시글 리스트 가져오기 (무한 스크롤링)
    @GET("/read/profile/others")
    Call<Post> readOtherPosts();

    // TODO: 나의 프로필 가져오기 (1개)
    @GET("/read/profile/me")
    Call<User> readMe();

    // TODO: 다른 사람의 프로필 가져오기 (1개)
    @GET("/read/profile/other")
    Call<User> readOther();

    // TODO: 나의 프로필 수정하기
    @FormUrlEncoded
    @HTTP(method = "PUT", path = "/update/profile", hasBody = true)
    Call<User> updateProfile();

    // TODO: 팔로우, 팔로잉 - 좋아요랑 구현 방식이 비슷할 거 같은데
    @FormUrlEncoded
    @HTTP(method = "PUT", path = "/update/profile/follow", hasBody = true)
    Call<User> updateFollow();
}
