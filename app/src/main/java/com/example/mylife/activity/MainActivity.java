package com.example.mylife.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.mylife.MyApplication;
import com.example.mylife.R;
import com.example.mylife.dialog.SimpleConfirmDialog;
import com.example.mylife.fragment.HomeFragment;
import com.example.mylife.fragment.MyPageFragment;
import com.example.mylife.fragment.NotificationFragment;
import com.example.mylife.fragment.PostingFragment;
import com.example.mylife.fragment.SearchFragment;
import com.example.mylife.item.ChatRoom;
import com.example.mylife.item.User;
import com.example.mylife.util.DialogHelper;
import com.example.mylife.util.NetworkConnection;
import com.example.mylife.util.RetrofitHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.messaging.FirebaseMessaging;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.mylife.MyApplication.FIREBASE_TOKEN;
import static com.example.mylife.MyApplication.LOGIN_TYPE;
import static com.example.mylife.MyApplication.PROFILE_IMAGE_URL;
import static com.example.mylife.MyApplication.USER_EMAIL;
import static com.example.mylife.MyApplication.USER_IDX;
import static com.example.mylife.MyApplication.USER_NAME;
import static com.example.mylife.MyApplication.USER_SESSION;
import static com.example.mylife.util.NetworkConnection.TYPE_NOT_CONNECTED;

/**
 * 메인화면
 *
 * 기능
 * 1. TODO : 홈 화면 연결
 * 2. TODO : 검색 화면 연결
 * 3. TODO : 포스팅 화면 연결
 * 4. TODO : 알림 화면 연결
 * 5. TODO : 마이페이지 화면 연결
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener, NavigationBarView.OnItemSelectedListener {
    private final String TAG = "MainActivity";
    private final RetrofitHelper retrofitHelper = RetrofitHelper.getInstance();
    private final NetworkConnection networkConnection = NetworkConnection.getInstance();
    private final DialogHelper dialogHelper = DialogHelper.getInstance();

    private final FragmentManager fragmentManager = getSupportFragmentManager();
    private HomeFragment homeFragment;
    private SearchFragment searchFragment;
    private PostingFragment postingFragment;
    private NotificationFragment notificationFragment;
    private MyPageFragment myPageFragment;

    private BottomNavigationView bnvBottom;
    private ImageView ivLogo;
    private ImageButton ibChat;

    private Menu bnvMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setFragment();
        bindView();
        setInitData();
    }

    /**
     * ------------------------------- category 0. 최초 설정 -------------------------------
     */
    private void setInitData() {
        bnvMenu = bnvBottom.getMenu();
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                        return;
                    }

                    // Get new FCM registration token
                    String token = task.getResult();

                    uploadFirebaseToken(token);
                });
    }

    private void bindView() {
        /* 뷰 바인드 */
        ivLogo = findViewById(R.id.iv_logo);
        ibChat = findViewById(R.id.ib_chat);
        bnvBottom = findViewById(R.id.bnv_bottom);

        /* 리스너 관련 */
        ivLogo.setOnClickListener(this);
        ibChat.setOnClickListener(this);
        bnvBottom.setOnItemSelectedListener(this);
    }

    private void setFragment() {
        if (homeFragment == null) {
            homeFragment = new HomeFragment();
            fragmentManager.beginTransaction().add(R.id.fl_middle, homeFragment).commitAllowingStateLoss();
        }
        if (homeFragment != null) {
            fragmentManager.beginTransaction().show(homeFragment).commitAllowingStateLoss();
        }
        if (searchFragment != null) {
            fragmentManager.beginTransaction().hide(searchFragment).commitAllowingStateLoss();
        }
        if (postingFragment != null) {
            fragmentManager.beginTransaction().hide(postingFragment).commitAllowingStateLoss();
        }
        if (notificationFragment != null) {
            fragmentManager.beginTransaction().hide(notificationFragment).commitAllowingStateLoss();
        }
        if (myPageFragment != null) {
            fragmentManager.beginTransaction().hide(myPageFragment).commitAllowingStateLoss();
        }
    }


    /**
     * ------------------------------- category 1. 뷰 리스너 -------------------------------
     */
    @Override
    public void onClick(View v) {
        if (ivLogo.equals(v)) {
           // TODO: 로고 클릭 시 뭐할 지 생각 중
        } else if (ibChat.equals(v)) {
            // TODO: 채팅 클릭 시 채팅방 화면으로 넘어감
            Intent toChatRoomIntent = new Intent(this, ChatRoomActivity.class);
            startActivity(toChatRoomIntent);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        // TODO: 프래그먼트 각 메소드들 어떻게 동작하는 지도 공부를 해야할 거 같다.
        // --------- 바텀 네비게이션 클릭 이벤트 ---------
        if (itemId == R.id.HOME) {
            // TODO: 프래그먼트 만드는 조건문 계속 반복해서 쓰는데 메소드로 뺄 수 있는 방식도 생각해보기
            if (homeFragment == null) {
                homeFragment = new HomeFragment();
                fragmentManager.beginTransaction().add(R.id.fl_middle, homeFragment).commitAllowingStateLoss();
            } else {
                fragmentManager.beginTransaction().show(homeFragment).commitAllowingStateLoss();
            }
            if (searchFragment != null) fragmentManager.beginTransaction().hide(searchFragment).commitAllowingStateLoss();
            if (postingFragment != null) fragmentManager.beginTransaction().hide(postingFragment).commitAllowingStateLoss();
            if (notificationFragment != null) fragmentManager.beginTransaction().hide(notificationFragment).commitAllowingStateLoss();
            if (myPageFragment != null) fragmentManager.beginTransaction().hide(myPageFragment).commitAllowingStateLoss();
        } else if (itemId == R.id.SEARCH) {
            if (searchFragment == null) {
                searchFragment = new SearchFragment();
                fragmentManager.beginTransaction().add(R.id.fl_middle, searchFragment).commitAllowingStateLoss();
            } else {
                fragmentManager.beginTransaction().show(searchFragment).commitAllowingStateLoss();
            }
            if (homeFragment != null) fragmentManager.beginTransaction().hide(homeFragment).commitAllowingStateLoss();
            if (postingFragment != null) fragmentManager.beginTransaction().hide(postingFragment).commitAllowingStateLoss();
            if (notificationFragment != null) fragmentManager.beginTransaction().hide(notificationFragment).commitAllowingStateLoss();
            if (myPageFragment != null) fragmentManager.beginTransaction().hide(myPageFragment).commitAllowingStateLoss();
        } else if (itemId == R.id.POSTING) {
            if (postingFragment == null) {
                postingFragment = new PostingFragment();
                fragmentManager.beginTransaction().add(R.id.fl_middle, postingFragment).commitAllowingStateLoss();
            } else {
                fragmentManager.beginTransaction().show(postingFragment).commitAllowingStateLoss();
            }
            if (homeFragment != null) fragmentManager.beginTransaction().hide(homeFragment).commitAllowingStateLoss();
            if (searchFragment != null) fragmentManager.beginTransaction().hide(searchFragment).commitAllowingStateLoss();
            if (notificationFragment != null) fragmentManager.beginTransaction().hide(notificationFragment).commitAllowingStateLoss();
            if (myPageFragment != null) fragmentManager.beginTransaction().hide(myPageFragment).commitAllowingStateLoss();
        } else if (itemId == R.id.NOTIFICATION) {
            if (notificationFragment == null) {
                notificationFragment = new NotificationFragment();
                fragmentManager.beginTransaction().add(R.id.fl_middle, notificationFragment).commitAllowingStateLoss();
            } else {
                fragmentManager.beginTransaction().show(notificationFragment).commitAllowingStateLoss();
            }
            if (homeFragment != null) fragmentManager.beginTransaction().hide(homeFragment).commitAllowingStateLoss();
            if (searchFragment != null) fragmentManager.beginTransaction().hide(searchFragment).commitAllowingStateLoss();
            if (postingFragment != null) fragmentManager.beginTransaction().hide(postingFragment).commitAllowingStateLoss();
            if (myPageFragment != null) fragmentManager.beginTransaction().hide(myPageFragment).commitAllowingStateLoss();
        } else if (itemId == R.id.MYPAGE) {
            if (myPageFragment == null) {
                myPageFragment = new MyPageFragment();
                fragmentManager.beginTransaction().add(R.id.fl_middle, myPageFragment).commitAllowingStateLoss();
            } else {
                fragmentManager.beginTransaction().show(myPageFragment).commitAllowingStateLoss();
            }
            if (homeFragment != null) fragmentManager.beginTransaction().hide(homeFragment).commitAllowingStateLoss();
            if (searchFragment != null) fragmentManager.beginTransaction().hide(searchFragment).commitAllowingStateLoss();
            if (postingFragment != null) fragmentManager.beginTransaction().hide(postingFragment).commitAllowingStateLoss();
            if (notificationFragment != null) fragmentManager.beginTransaction().hide(notificationFragment).commitAllowingStateLoss();
        }
        return true;
    }

    /**
     * ------------------------------- category ?. 서버 통신 -------------------------------
     */
    private void uploadFirebaseToken(String firebaseToken) {
        Log.d(TAG, "uploadFirebaseToken - firebaseToken : " + firebaseToken);
        Call<User> uploadFirebaseToken = retrofitHelper.getRetrofitInterFace().uploadFirebaseToken(USER_SESSION, USER_IDX, firebaseToken);
        uploadFirebaseToken.enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NotNull Call<User> call, @NotNull Response<User> response) {
                switch (response.code()) {
                    case 500:
                        retrofitHelper.printRetrofitResponse(TAG, response);
                        dialogHelper.showConfirmDialog(MainActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.server_error_message));
                        break;

                    case 400:
                        retrofitHelper.printRetrofitResponse(TAG, response);
                        dialogHelper.showConfirmDialog(MainActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.client_error_message));
                        break;

                    case 200:
                        Log.i(TAG, "uploadFirebaseToken - onResponse : " + response);
                        assert response.body() != null;

                        // TODO: 유저 로그인 타입은 자동 로그인, 네이버 로그인, 카카오 로그인 구현하면 다시 건드리기
                        FIREBASE_TOKEN = response.body().getFirebaseToken();

                        SharedPreferences.Editor editor = getSharedPreferences("auto", Activity.MODE_PRIVATE).edit();
                        editor.putString(getString(R.string.key_firebase_token), FIREBASE_TOKEN);
                        editor.apply();

                        Log.d(TAG, "uploadFirebaseToken - FIREBASE_TOKEN : " + FIREBASE_TOKEN);
                        break;
                }
            }

            @Override
            public void onFailure(@NotNull Call<User> call, Throwable t) {
                Log.e(TAG, "uploadFirebaseToken onFailure : " + t.toString());
                dialogHelper.showConfirmDialog(MainActivity.this, dialogHelper.NO_LISTENER_DIALOG_ID, getString(R.string.network_not_stable));
            }
        });
    }

    /**
     * ------------------------------- category ?. 유틸리티 -------------------------------
     */
}
