package com.example.mylife.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.mylife.R;
import com.example.mylife.activity.LoginActivity;
import com.example.mylife.activity.MainActivity;
import com.example.mylife.activity.SignUpActivity;
import com.example.mylife.item.User;
import com.example.mylife.util.DialogHelper;
import com.example.mylife.util.MethodHelper;
import com.example.mylife.util.NetworkConnection;
import com.example.mylife.util.RetrofitHelper;
import com.google.android.material.textfield.TextInputLayout;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.mylife.MyApplication.USER_IDX;
import static com.example.mylife.MyApplication.USER_SESSION;
import static com.example.mylife.util.NetworkConnection.TYPE_NOT_CONNECTED;

/**
 * 게시글 작성 화면
 *
 * 기능
 * 1. 게시글 이미지 선택
 * 2. 게시글 작성
 * 3. 업로드
 */
public class PostingFragment extends Fragment implements View.OnClickListener {
    private final String TAG = "PostingFragment";
    private final RetrofitHelper retrofitHelper = RetrofitHelper.getInstance();
    private final NetworkConnection networkConnection = NetworkConnection.getInstance();
    private final DialogHelper dialogHelper = DialogHelper.getInstance();
    private final MethodHelper methodHelper = MethodHelper.getInstance();
    private Context mContext;
    private Activity mActivity;
    private Bitmap bitmap;

    private ImageView ivUploadImage;
    private TextInputLayout tilContents;
    private Button btnUpload;

    public PostingFragment() {
        super(R.layout.fragment_posting);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setInitData();
        bindView(view);
    }

    /**
     * ------------------------------- category 0. 최초 설정 -------------------------------
     */
    private void setInitData() {

    }

    private void bindView(View v) {
        /* 뷰 바인드 */
        ivUploadImage = v.findViewById(R.id.iv_upload_image);
        tilContents = v.findViewById(R.id.til_contents);
        btnUpload = v.findViewById(R.id.btn_upload);

        /* 리스너 관련 */
        ivUploadImage.setOnClickListener(this);
        btnUpload.setOnClickListener(this);
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
                dialogHelper.showConfirmDialog((AppCompatActivity) mContext, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.image_null));
                return;
            }

            // contents 공백 체크
            String contents = methodHelper.getTextInputLayoutString(TAG, tilContents);
            if (contents == null) {
                dialogHelper.showConfirmDialog((AppCompatActivity) mContext, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.edittext_null));
                return;
            }

            // 네트워크 체크 및 업로드
            if (networkConnection.checkNetworkConnection((AppCompatActivity) mContext) == TYPE_NOT_CONNECTED) {
                dialogHelper.showConfirmDialog((AppCompatActivity) mContext, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.no_connected_network));
            } else {
                uploadPost(uploadImage, contents);
            }
        }
    }

    /**
     * TODO: startActivityForResult가 Deprecated가 되었고 이게 왔는데... 좀 더 깔끔하게 쓰는 법 없을까?
     * TODO: requestCode가 없는 이유는 그냥 다른 것들은 각 개체별로 설정을 하라는 얘기인건가?
     */
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
            Glide.with(mContext).load(imageUri).into(ivUploadImage);
        }
    });

    /**
     * ------------------------------- category 3. 서버 통신 -------------------------------
     */
    private void uploadPost(Bitmap bitmap, String contents) {
        // TODO: String만 받게 만들어서 string.xml에 있는 문자열을 못 가져오는데 @Nonull이랑 @Nullable 써서 파라미터 수정해보기
        dialogHelper.showLoadingDialog((AppCompatActivity) mContext, "업로드 중 입니다.");

        // TODO: Bitmap을 Base64로 인코딩해서 이미지를 서버로 전송하는건데 이거 메소드로 바꿀까...
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        String image = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
        String imageName = String.valueOf(Calendar.getInstance().getTimeInMillis());

        Call<Void> callCreatePost = retrofitHelper.getRetrofitInterFace().createPost(USER_SESSION, USER_IDX, image, imageName, contents);
        Log.d(TAG, "uploadPost - USER_SESSION : " + USER_SESSION);
        Log.d(TAG, "uploadPost - USER_IDX : " + USER_IDX);
        Log.d(TAG, "uploadPost - image : " + image);
        Log.d(TAG, "uploadPost - imageName " + imageName);
        Log.d(TAG, "uploadPost - contents " + contents);

        callCreatePost.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NotNull Call<Void> call, @NotNull Response<Void> response) {
                dialogHelper.dismissLoading();

                switch (response.code()) {
                    case 400:
                        retrofitHelper.printRetrofitResponse(TAG, response);
                        dialogHelper.showConfirmDialog((AppCompatActivity) mContext, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.client_error_message));
                        break;

                    case 500:
                        retrofitHelper.printRetrofitResponse(TAG, response);
                        dialogHelper.showConfirmDialog((AppCompatActivity) mContext, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.server_error_message));
                        break;

                    case 200:
                        // TODO: 밑에서 Activity 종료 시키고 초기화 시켜주는데 이 밑에 코드들이 의미가 있을까?
                        // 이미지 초기화
                        Glide.with(mContext).load(R.drawable.ic_basic_null_image).into(ivUploadImage);
                        // 내용을 적는 EditText 비우기
                        tilContents.post(() -> { Objects.requireNonNull(tilContents.getEditText()).setText(""); });

                        /**
                         * TODO: Fragment(PostingFragment) -> Activity(MainActivity - 사실상 HomeFragment로 옮겨주기 위함...) 좀 더 나은 방법이 없을까
                         * 부연설명 : 게시글 업로드 창에서 홈 화면으로 옮겨주려는건데 일단 MainActivity의 최초 Fragment 화면이 HomeFragment여서 이렇게 Intent로 옮겨주는데
                         * 좀 더 나은 방법이 없는 지 찾아봐야할듯?
                         */
                        Intent toMainIntent = new Intent(mContext, MainActivity.class);
                        startActivity(toMainIntent);
                        requireActivity().finish();
                        break;
                }
            }

            @Override
            public void onFailure(@NotNull Call<Void> call, @NotNull Throwable t) {
                Log.e(TAG, "uploadPost - callCreatePost failed : " + t.getMessage());
                dialogHelper.showConfirmDialog((AppCompatActivity) mContext, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.network_not_stable));
            }
        });
    }

    /**
     * ------------------------------- category ?. 생명주기 -------------------------------
     */
    // TODO: 이 밑에 생명주기 코드들이 의미가 있나? mActivity랑 mContext는 메소드로 대체해도 될 거 같은데

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        mContext = context;
        if (context instanceof Activity) mActivity = (Activity) mContext;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivity = null;
        mContext = null;
    }
}