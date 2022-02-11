package com.example.mylife.api;

import com.example.mylife.item.ChatRoom;
import com.example.mylife.item.Comment;
import com.example.mylife.item.Message;
import com.example.mylife.item.Notification;
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

    // 파이어베이스 토큰 업로드
    @FormUrlEncoded
    @POST("/firebase")
    Call<User> uploadFirebaseToken(@Header("Cookie") String session,
                                   @Field("user_idx") int userIdx,
                                   @Field("firebase_token") String firebaseToken);

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
    // 유저 검색
    @GET("/read/search/users")
    Call<User> readSearchUsers(@Header("Cookie") String session,
                               @Query("user_idx") int userIdx,
                               @Query("page") int page,
                               @Query("limit") int limit,
                               @Query("search_word") String searchWord);

    // 게시글 랜덤으로 가져오기 (무한 스크롤링)
    @GET("/read/search/posts")
    Call<Post> readSearchPosts(@Header("Cookie") String session,
                               @Query("user_idx") int userIdx,
                               @Query("page") int page,
                               @Query("limit") int limit);

    /**
     * ------------------------------- category ?. 알림 탭 관련 -------------------------------
     */
    // 알림 가져오기 (무한 스크롤링)
    @GET("/read/notifications")
    Call<Notification> readNotifications(@Header("Cookie") String session,
                                         @Query("user_idx") int userIdx,
                                         @Query("page") int page,
                                         @Query("limit") int limit);

    /**
     * ------------------------------- category ?. 마이페이지 -------------------------------
     */
    // 프로필 페이지 작성한 게시글 리스트 가져오기 (무한 스크롤링)
    @GET("/read/profile/posts")
    Call<Post> readInfoPosts(@Header("Cookie") String session,
                             @Query("user_idx") int userIdx,
                             @Query("idx") int idx,
                             @Query("page") int page,
                             @Query("limit") int limit);

    // 프로필 가져오기 (1개)
    @GET("/read/profile/info")
    Call<User> readInfo(@Header("Cookie") String session,
                        @Query("user_idx") int userIdx,
                        @Query("idx") int idx);

    // 나의 프로필 수정하기
    @FormUrlEncoded
    @HTTP(method = "PUT", path = "/update/profile", hasBody = true)
    Call<User> updateProfile(@Header("Cookie") String session,
                             @Field("user_idx") int userIdx,
                             @Field("image") String image,
                             @Field("image_name") String imageName,
                             @Field("name") String name,
                             @Field("about_me") String aboutMe,
                             @Field("is_image_change") boolean isImageChange);

    // 팔로잉 리스트 가져오기 (무한 스크롤링)
    @GET("/read/follow/followings")
    Call<User> readFollowings(@Header("Cookie") String session,
                              @Query("user_idx") int userIdx,
                              @Query("idx") int idx,
                              @Query("page") int page,
                              @Query("limit") int limit);

    // 팔로워 리스트 가져오기 (무한 스크롤링)
    @GET("/read/follow/followers")
    Call<User> readFollowers(@Header("Cookie") String session,
                             @Query("user_idx") int userIdx,
                             @Query("idx") int idx,
                             @Query("page") int page,
                             @Query("limit") int limit);

    // 팔로우, 팔로잉 - 좋아요랑 구현 방식이 비슷할 거 같은데
    @FormUrlEncoded
    @HTTP(method = "PUT", path = "/update/profile/follow", hasBody = true)
    Call<User> updateFollow(@Header("Cookie") String session,
                            @Field("user_idx") int userIdx,
                            @Field("idx") int idx,
                            @Field("is_follow") boolean isFollow);

    /**
     * ------------------------------- category ?. 채팅 관련 -------------------------------
     */
    // 채팅방 목록 불러오기 (무한 스크롤링)
    @GET("/read/chat/rooms")
    Call<ChatRoom> readChatRooms(@Header("Cookie") String session,
                                 @Query("user_idx") int userIdx,
                                 @Query("page") int page,
                                 @Query("limit") int limit);

    // 채팅방 정보 불러오기
    @GET("/read/chat/info")
    Call<ChatRoom> readChatRoomInfo(@Header("Cookie") String session,
                                    @Query("user_idx") int userIdx,
                                    @Query("chat_room_idx") int chatRoomIdx);

    // 채팅 메시지 목록(?) 불러오기 (무한 스크롤링(?)) - TODO: 근데 이건 위로 무한 스크롤링인데 안드로이드에서 위로 스크롤링하면 최신께 역순으로(?) 나오는 식으로 해야할 듯, 일단 기능 완성시키고 1순위로 고치기
    @GET("/read/chat/messages")
    Call<Message> readChatMessages(@Header("Cookie") String session,
                                   @Query("user_idx") int userIdx,
                                   @Query("page") int page,
                                   @Query("limit") int limit,
                                   @Query("chat_room_idx") int chatRoomIdx);

    // 1:1 채팅방 만들기
    @FormUrlEncoded
    @POST("/create/chat/personal_room")
    Call<ChatRoom> createChatPersonalRoom(@Header("Cookie") String session,
                                          @Field("user_idx") int userIdx,
                                          @Field("idx") int idx);

    // 텍스트 메시지 저장하기
    @FormUrlEncoded
    @POST("/create/chat/text_message")
    Call<Message> createChatTextMessage(@Header("Cookie") String session,
                                        @Field("user_idx") int userIdx,
                                        @Field("chat_room_idx") int chatRoomIdx,
                                        @Field("contents") String contents);

    // TODO: 이미지 메시지 저장하기
    @FormUrlEncoded
    @POST("/create/chat/image_message")
    Call<Message> createChatImageMessage(@Header("Cookie") String session,
                                         @Field("user_idx") int userIdx,
                                         @Field("chat_room_idx") int chatRoomIdx,
                                         @Field("image") String image,
                                         @Field("image_name") String imageName);

    // TODO: 1:1 채팅방 openType 바꾸기, 근데 채팅방을 나가게 되면 기존 채팅내역도 다 삭제를 해주어야하는데 그냥 단순 삭제 해버리면, 안 나간 사람도 나간 사람의 채팅이 삭제되게 되는건데 이거 어떻게 처리할 지도 생각해보기
    // TODO: 이거 근데 단순히 채팅방 타입은 OPEN에서 CLOSE로 바꿔버리면 채팅방 숨기기를 원하지 않는 사용자까지 숨기기 처리가 되는데?....
    @FormUrlEncoded
    @HTTP(method = "DELETE", path = "/delete/chat/personal_room", hasBody = true)
    Call<Void> deleteChatPersonalRoom(@Header("Cookie") String session,
                                      @Field("user_idx") int userIdx,
                                      @Field("chat_room_idx") int chatRoomIdx);

}
