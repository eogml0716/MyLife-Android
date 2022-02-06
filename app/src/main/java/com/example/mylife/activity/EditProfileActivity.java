package com.example.mylife.activity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.example.mylife.R;
import com.example.mylife.item.Post;
import com.example.mylife.item.User;
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

import static com.example.mylife.MyApplication.LOGIN_TYPE;
import static com.example.mylife.MyApplication.PROFILE_IMAGE_URL;
import static com.example.mylife.MyApplication.USER_EMAIL;
import static com.example.mylife.MyApplication.USER_IDX;
import static com.example.mylife.MyApplication.USER_NAME;
import static com.example.mylife.MyApplication.USER_SESSION;
import static com.example.mylife.util.NetworkConnection.TYPE_NOT_CONNECTED;

// TODO: 기본 이미지로 변경하기 기능 추가하기
public class EditProfileActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = "EditProfileActivity";
    private final RetrofitHelper retrofitHelper = RetrofitHelper.getInstance();
    private final NetworkConnection networkConnection = NetworkConnection.getInstance();
    private final DialogHelper dialogHelper = DialogHelper.getInstance();
    private final MethodHelper methodHelper = MethodHelper.getInstance();

    private ImageView ivProfile;
    private TextInputLayout tilAboutMe;
    private EditText etName;
    private Button btnComplete;
    private ImageButton ibBack;
    private ProgressBar pbLoading; // 프로그래스 바

    private String profileImageUrl, aboutMe;

    // 이미지 수정 여부를 담는 변수,
    // 이미지를 변경하지 않고 서버로 이미지를 전송하는 경우와 그렇지 않은 경우를 구분해주기 위해서 만든 변수
    private boolean isImageChange = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        bindView();
        setInitData();
    }

    /**
     * ------------------------------- category 0. 최초 설정 -------------------------------
     */
    private void setInitData() {
        loadInfo();
        etName.setText(USER_NAME);
        ivProfile.post(() -> {
            ivProfile.setBackground(null); // 배경 이미지 변경
        });
        Glide.with(this).load(PROFILE_IMAGE_URL).into(ivProfile);
    }

    private void bindView() {
        /* 뷰 바인드 */
        ibBack = findViewById(R.id.ib_back);
        ivProfile = findViewById(R.id.iv_profile);
        tilAboutMe = findViewById(R.id.til_about_me);
        etName = findViewById(R.id.et_name);
        btnComplete = findViewById(R.id.btn_complete);
        pbLoading = findViewById(R.id.pb_loading);

        /* 리스너 관련 */
        ibBack.setOnClickListener(this);
        ivProfile.setOnClickListener(this);
        btnComplete.setOnClickListener(this);
    }

    /**
     * ------------------------------- category 1. 뷰 리스너 -------------------------------
     */
    @Override
    public void onClick(View v) {
        if (ibBack.equals(v)) {
            finish();
        } else if (ivProfile.equals(v)) {
            Intent toPick = new Intent(Intent.ACTION_PICK);
            toPick.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
            imagePickResultLauncher.launch(toPick);
        } else if (btnComplete.equals(v)) {
            Bitmap uploadImage = ((BitmapDrawable) ivProfile.getDrawable()).getBitmap();
            // TODO: 기본 이미지로 변경하는 버튼 추가 시에 다시 구현할 것
//            // 이미지 공백 체크
//            if (uploadImage == null) {
//                dialogHelper.showConfirmDialog(this , dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.image_null));
//                return;
//            }

            // name 공백 체크
            String name = etName.getText().toString();
            if (name == null || name.equals("")) {
                dialogHelper.showConfirmDialog(this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.edittext_null));
                return;
            }

            // 네트워크 체크 및 업로드
            if (networkConnection.checkNetworkConnection(this) == TYPE_NOT_CONNECTED) {
                dialogHelper.showConfirmDialog(this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.no_connected_network));
            } else {
                aboutMe = methodHelper.getTextInputLayoutString(TAG, tilAboutMe);
                updateProfile(uploadImage, name, aboutMe);
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private final ActivityResultLauncher<Intent> imagePickResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
            Intent data = result.getData();
            Uri imageUri = data.getData();
            Log.d(TAG, "registerForActivityResult - result : " + result);
            Log.d(TAG, "registerForActivityResult - data : " + data);
            Log.d(TAG, "registerForActivityResult - imageUri : " + imageUri);
            ivProfile.post(new Runnable() {
                @Override
                public void run() {
                    ivProfile.setBackground(null); // 배경 이미지 변경
                }
            });
            Glide.with(this).load(imageUri).into(ivProfile);
            isImageChange = true;
        }
    });

    /**
     * ------------------------------- category ?. 서버 통신 -------------------------------
     */
    private void loadInfo() {
        Call<User> callReadInfo = retrofitHelper.getRetrofitInterFace().readInfo(USER_SESSION, USER_IDX, USER_IDX);
        callReadInfo.enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NotNull Call<User> call, @NotNull Response<User> response) {
                pbLoading.setVisibility(View.GONE);
                switch (response.code()) {
                    case 400:
                        retrofitHelper.printRetrofitResponse(TAG, response);
                        dialogHelper.showConfirmDialog(EditProfileActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.client_error_message));
                        break;

                    case 500:
                        retrofitHelper.printRetrofitResponse(TAG, response);
                        dialogHelper.showConfirmDialog(EditProfileActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.server_error_message));
                        break;

                    case 200:
                        assert response.body() != null;
                        aboutMe = response.body().getAboutMe();
                        Log.d(TAG, "loadInfo - aboutMe : " + aboutMe);
                        if(aboutMe.equals(getString(R.string.no_self_introduction))) {
                            Objects.requireNonNull(tilAboutMe.getEditText()).setText(null);
                        } else {
                            Objects.requireNonNull(tilAboutMe.getEditText()).setText(aboutMe);
                        }
                        break;
                }
            }

            @Override
            public void onFailure(@NotNull Call<User> call, @NotNull Throwable t) {
                Log.e(TAG, "loadInfo - onFailure : " + t.getMessage());
                pbLoading.setVisibility(View.GONE);
                dialogHelper.showConfirmDialog(EditProfileActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.network_not_stable));
            }
        });
    }

    private void updateProfile(Bitmap bitmap, String name, String aboutMe) {
        dialogHelper.showLoadingDialog(this, "프로필 업데이트 중 입니다.");
        // TODO: 자기소개 관련해서 에러 처리를 너무 이상하게 하는데 꼭 바꿀 것
        if (aboutMe == null || aboutMe.equals("")) {
            aboutMe = "about_me";
        }
        if (isImageChange) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            String image = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
            String imageName = String.valueOf(Calendar.getInstance().getTimeInMillis());

            Call<User> callUpdateProfile = retrofitHelper.getRetrofitInterFace().updateProfile(USER_SESSION, USER_IDX, image, imageName, name, aboutMe, isImageChange);
            Log.d(TAG, "updateProfile - USER_SESSION : " + USER_SESSION);
            Log.d(TAG, "updateProfile - USER_IDX : " + USER_IDX);
            Log.d(TAG, "updateProfile - image : " + image);
            Log.d(TAG, "updateProfile - imageName " + imageName);
            Log.d(TAG, "updateProfile - imageName " + name);
            Log.d(TAG, "updateProfile - contents " + aboutMe);
            Log.d(TAG, "updateProfile - isImageChange " + isImageChange);

            callUpdateProfile.enqueue(new Callback<User>() {
                @Override
                public void onResponse(@NotNull Call<User> call, @NotNull Response<User> response) {
                    dialogHelper.dismissLoading();
                    switch (response.code()) {
                        case 400:
                            retrofitHelper.printRetrofitResponse(TAG, response);
                            dialogHelper.showConfirmDialog(EditProfileActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.client_error_message));
                            break;

                        case 500:
                            retrofitHelper.printRetrofitResponse(TAG, response);
                            dialogHelper.showConfirmDialog(EditProfileActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.server_error_message));
                            break;

                        case 200:
                            // TODO: 일단 Activity 종료하는 방식으로, 원래 Fragment 갱신하는 방식으로 해도 되는데 너무 코드가 더러워질 거 같아서 일단 이렇게 하기
                            assert response.body() != null;
                            String name = response.body().getName();
                            String profileImageUrl = response.body().getProfileImageUrl();

                            USER_NAME = name;
                            PROFILE_IMAGE_URL = profileImageUrl;

                            SharedPreferences.Editor editor = getSharedPreferences("auto", Activity.MODE_PRIVATE).edit();
                            editor.putString(getString(R.string.name), USER_NAME);
                            editor.putString(getString(R.string.profile_image_url), PROFILE_IMAGE_URL);
                            editor.apply();

                            Log.d(TAG, "updateProfile - USER_NAME : " + USER_NAME);
                            Log.d(TAG, "updateProfile - PROFILE_IMAGE_URL : " + PROFILE_IMAGE_URL);

                            // TODO: 일단 Activity 종료하는 방식으로, 원래 Fragment 갱신하는 방식으로 해도 되는데 너무 코드가 더러워질 거 같아서 일단 이렇게 하기
                            finish();
                            break;
                    }
                }

                @Override
                public void onFailure(@NotNull Call<User> call, @NotNull Throwable t) {
                    Log.e(TAG, "updateProfile - callUpdatePost failed : " + t.getMessage());
                    dialogHelper.showConfirmDialog(EditProfileActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.network_not_stable));
                }
            });
        } else {
            Call<User> callUpdateProfile = retrofitHelper.getRetrofitInterFace().updateProfile(USER_SESSION, USER_IDX, "image", "imageName", name, aboutMe, isImageChange);
            Log.d(TAG, "updateProfile - USER_SESSION : " + USER_SESSION);
            Log.d(TAG, "updateProfile - USER_IDX : " + USER_IDX);
            Log.d(TAG, "updateProfile - imageName " + name);
            Log.d(TAG, "updateProfile - contents " + aboutMe);
            Log.d(TAG, "updateProfile - isImageChange " + isImageChange);

            callUpdateProfile.enqueue(new Callback<User>() {
                @Override
                public void onResponse(@NotNull Call<User> call, @NotNull Response<User> response) {
                    dialogHelper.dismissLoading();
                    switch (response.code()) {
                        case 400:
                            retrofitHelper.printRetrofitResponse(TAG, response);
                            dialogHelper.showConfirmDialog(EditProfileActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.client_error_message));
                            break;

                        case 500:
                            retrofitHelper.printRetrofitResponse(TAG, response);
                            dialogHelper.showConfirmDialog(EditProfileActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.server_error_message));
                            break;

                        case 200:
                            assert response.body() != null;
                            String name = response.body().getName();
                            String profileImageUrl = response.body().getProfileImageUrl();

                            USER_NAME = name;
                            PROFILE_IMAGE_URL = profileImageUrl;

                            SharedPreferences.Editor editor = getSharedPreferences("auto", Activity.MODE_PRIVATE).edit();
                            editor.putString(getString(R.string.name), USER_NAME);
                            editor.putString(getString(R.string.profile_image_url), PROFILE_IMAGE_URL);
                            editor.apply();

                            Log.d(TAG, "updateProfile - USER_NAME : " + USER_NAME);
                            Log.d(TAG, "updateProfile - PROFILE_IMAGE_URL : " + PROFILE_IMAGE_URL);

                            // TODO: 일단 Activity 종료하는 방식으로, 원래 Fragment 갱신하는 방식으로 해도 되는데 너무 코드가 더러워질 거 같아서 일단 이렇게 하기
                            finish();
                            break;
                    }
                }

                @Override
                public void onFailure(@NotNull Call<User> call, @NotNull Throwable t) {
                    Log.e(TAG, "updateProfile - callUpdatePost failed : " + t.getMessage());
                    dialogHelper.showConfirmDialog(EditProfileActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.network_not_stable));
                }
            });
        }
    }
}