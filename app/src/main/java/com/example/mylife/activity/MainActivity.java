package com.example.mylife.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;

import com.example.mylife.MyApplication;
import com.example.mylife.R;
import com.example.mylife.dialog.SimpleConfirmDialog;
import com.example.mylife.fragment.HomeFragment;
import com.example.mylife.fragment.MyPageFragment;
import com.example.mylife.fragment.NotificationFragment;
import com.example.mylife.fragment.PostingFragment;
import com.example.mylife.fragment.SearchFragment;
import com.example.mylife.util.DialogHelper;
import com.example.mylife.util.NetworkConnection;
import com.example.mylife.util.RetrofitHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setFragment();
        bindView();
    }

    /**
     * ------------------------------- category 0. 최초 설정 -------------------------------
     */
    private void setInit() {
    }

    private void bindView() {
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

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }

    /**
     * ------------------------------- category ?. 서버 통신 -------------------------------
     */

    /**
     * ------------------------------- category ?. 유틸리티 -------------------------------
     */
}
