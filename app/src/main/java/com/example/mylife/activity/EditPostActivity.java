package com.example.mylife.activity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.mylife.R;
import com.example.mylife.item.Post;
import com.example.mylife.util.DialogHelper;
import com.example.mylife.util.MethodHelper;
import com.example.mylife.util.NetworkConnection;
import com.example.mylife.util.RetrofitHelper;
import com.google.android.material.textfield.TextInputLayout;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.mylife.MyApplication.USER_IDX;
import static com.example.mylife.MyApplication.USER_SESSION;
import static com.example.mylife.util.NetworkConnection.TYPE_NOT_CONNECTED;

/**
 * 게시글 수정 화면
 *
 * 기능
 * 1. 게시글 이미지 선택
 * 2. 게시글 작성
 * 3. 업로드
 */
// TODO: 그냥 PostingFragment를 없애고, PostingActivity를 만들어서 수정하는 것까지 한꺼번에 처리하는 게 나았을 거 같다. 코드 90% 이상이 중복되네
public class EditPostActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = "EditPostActivity";
    private final RetrofitHelper retrofitHelper = RetrofitHelper.getInstance();
    private final NetworkConnection networkConnection = NetworkConnection.getInstance();
    private final DialogHelper dialogHelper = DialogHelper.getInstance();
    private final MethodHelper methodHelper = MethodHelper.getInstance();

    private ImageView ivUploadImage;
    private TextInputLayout tilContents;
    private Button btnUpload;
    private ImageButton ibBack;

    private int boardIdx;
    private String imageUrl, contents;

    // 이미지 수정 여부를 담는 변수,
    // 이미지를 변경하지 않고 서버로 이미지를 전송하는 경우와 그렇지 않은 경우를 구분해주기 위해서 만든 변수
    private boolean isImageChange = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post);
        bindView();
        setInitData();
    }

    /**
     * ------------------------------- category 0. 최초 설정 -------------------------------
     */
    private void setInitData() {
        boardIdx = getIntent().getIntExtra("board_idx", 0);
        imageUrl = getIntent().getStringExtra("image_url");
        contents = getIntent().getStringExtra("contents");

        ivUploadImage.post(() -> {
            ivUploadImage.setBackground(null); // 배경 이미지 변경
        });
        Glide.with(this).load(imageUrl).into(ivUploadImage);
        Objects.requireNonNull(tilContents.getEditText()).setText(contents);
    }

    private void bindView() {
        /* 뷰 바인드 */
        ivUploadImage = findViewById(R.id.iv_upload_image);
        tilContents = findViewById(R.id.til_contents);
        btnUpload = findViewById(R.id.btn_upload);
        ibBack = findViewById(R.id.ib_back);

        /* 리스너 관련 */
        ivUploadImage.setOnClickListener(this);
        btnUpload.setOnClickListener(this);
        ibBack.setOnClickListener(this);
    }

    /**
     * ------------------------------- category 1. 뷰 리스너 -------------------------------
     */
    @Override
    public void onClick(View v) {
        if (ivUploadImage.equals(v)) {
            Intent toPick = new Intent(Intent.ACTION_PICK);
            toPick.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
            imagePickResultLauncher.launch(toPick);
        } else if (btnUpload.equals(v)) {
            Bitmap uploadImage = ((BitmapDrawable) ivUploadImage.getDrawable()).getBitmap();
            // 이미지 공백 체크
            if (uploadImage == null) {
                dialogHelper.showConfirmDialog(this , dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.image_null));
                return;
            }

            // contents 공백 체크
            String contents = methodHelper.getTextInputLayoutString(TAG, tilContents);
            if (contents == null) {
                dialogHelper.showConfirmDialog(this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.edittext_null));
                return;
            }

            // 네트워크 체크 및 업로드
            if (networkConnection.checkNetworkConnection(this) == TYPE_NOT_CONNECTED) {
                dialogHelper.showConfirmDialog(this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.no_connected_network));
            } else {
                updatePost(uploadImage, contents);
            }
        } else if (ibBack.equals(v)) {
            Intent toMainIntent = new Intent(EditPostActivity.this, MainActivity.class);
            startActivity(toMainIntent);
            finish();
        }
    }

    private final ActivityResultLauncher<Intent> imagePickResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
            Intent data = result.getData();
            Uri imageUri = data.getData();
            Log.d(TAG, "registerForActivityResult - result : " + result);
            Log.d(TAG, "registerForActivityResult - data : " + data);
            Log.d(TAG, "registerForActivityResult - imageUri : " + imageUri);
            ivUploadImage.post(new Runnable() {
                @Override
                public void run() {
                    ivUploadImage.setBackground(null); // 배경 이미지 변경
                }
            });
            Glide.with(this).load(imageUri).into(ivUploadImage);
            isImageChange = true;
        }
    });

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent toMainIntent = new Intent(EditPostActivity.this, MainActivity.class);
        startActivity(toMainIntent);
        finish();
    }

    /**
     * ------------------------------- category ?. 서버 통신 -------------------------------
     */
    private void updatePost(Bitmap bitmap, String contents) {
        dialogHelper.showLoadingDialog(this, "업로드 중 입니다.");

        if (isImageChange) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            String image = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
            String imageName = String.valueOf(Calendar.getInstance().getTimeInMillis());

            Call<Post> callUpdatePost = retrofitHelper.getRetrofitInterFace().updatePost(USER_SESSION, boardIdx, USER_IDX, image, imageName, contents, isImageChange);
            Log.d(TAG, "updatePost - USER_SESSION : " + USER_SESSION);
            Log.d(TAG, "updatePost - boardIdx : " + boardIdx);
            Log.d(TAG, "updatePost - USER_IDX : " + USER_IDX);
            Log.d(TAG, "updatePost - image : " + image);
            Log.d(TAG, "updatePost - imageName " + imageName);
            Log.d(TAG, "updatePost - contents " + contents);
            Log.d(TAG, "updatePost - isImageChange " + isImageChange);

            callUpdatePost.enqueue(new Callback<Post>() {
                @Override
                public void onResponse(@NotNull Call<Post> call, @NotNull Response<Post> response) {
                    dialogHelper.dismissLoading();
                    switch (response.code()) {
                        case 400:
                            retrofitHelper.printRetrofitResponse(TAG, response);
                            dialogHelper.showConfirmDialog(EditPostActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.client_error_message));
                            break;

                        case 500:
                            retrofitHelper.printRetrofitResponse(TAG, response);
                            dialogHelper.showConfirmDialog(EditPostActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.server_error_message));
                            break;

                        case 200:
                            // TODO: 화면 갱신이 아니라 리사이클러뷰 아이템 갱신하는 방식으로 변경하기
                            Intent toMainIntent = new Intent(EditPostActivity.this, MainActivity.class);
                            startActivity(toMainIntent);
                            finish();
                            break;
                    }
                }

                @Override
                public void onFailure(@NotNull Call<Post> call, @NotNull Throwable t) {
                    Log.e(TAG, "updatePost - callUpdatePost failed : " + t.getMessage());
                    dialogHelper.showConfirmDialog(EditPostActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.network_not_stable));
                }
            });
        } else {
            Call<Post> callUpdatePost = retrofitHelper.getRetrofitInterFace().updatePost(USER_SESSION, boardIdx, USER_IDX, "image", "imageName", contents, isImageChange);
            Log.d(TAG, "updatePost - USER_SESSION : " + USER_SESSION);
            Log.d(TAG, "updatePost - boardIdx : " + boardIdx);
            Log.d(TAG, "updatePost - USER_IDX : " + USER_IDX);
            Log.d(TAG, "updatePost - contents " + contents);
            Log.d(TAG, "updatePost - isImageChange " + isImageChange);

            callUpdatePost.enqueue(new Callback<Post>() {
                @Override
                public void onResponse(@NotNull Call<Post> call, @NotNull Response<Post> response) {
                    dialogHelper.dismissLoading();

                    switch (response.code()) {
                        case 400:
                            retrofitHelper.printRetrofitResponse(TAG, response);
                            dialogHelper.showConfirmDialog(EditPostActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.client_error_message));
                            break;

                        case 500:
                            retrofitHelper.printRetrofitResponse(TAG, response);
                            dialogHelper.showConfirmDialog(EditPostActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.server_error_message));
                            break;

                        case 200:
                            // TODO: startActivityForResult 메소드 같은 거 사용해서 처리를 해주어야할 듯, 그냥 단순히 바뀐 경우엔 리사이클러뷰에 반영이 안됨, 일단 포폴용으로 쓰고 변경하기
                            Intent toMainIntent = new Intent(EditPostActivity.this, MainActivity.class);
                            startActivity(toMainIntent);
                            finish();
                            break;
                    }
                }

                @Override
                public void onFailure(@NotNull Call<Post> call, @NotNull Throwable t) {
                    Log.e(TAG, "updatePost - callUpdatePost failed : " + t.getMessage());
                    dialogHelper.showConfirmDialog(EditPostActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.network_not_stable));
                }
            });
        }
    }
}