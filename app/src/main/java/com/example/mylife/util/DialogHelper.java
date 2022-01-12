package com.example.mylife.util;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mylife.R;
import com.example.mylife.dialog.DialogLoading;
import com.example.mylife.dialog.SimpleAlertDialog;
import com.example.mylife.dialog.SimpleConfirmDialog;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class DialogHelper {
    private final String TAG = "DialogHelper";
    private static DialogHelper dialogHelper;
    private DialogLoading loadingDialog;
    public final int NO_LISTENER_DIALOG_ID = 0;
    public final int ACTIVITY_FINISH_DIALOG_ID = -1;

    private DialogHelper() {
    }

    public static DialogHelper getInstance() {
        if (dialogHelper == null) dialogHelper = new DialogHelper();
        return dialogHelper;
    }

    /**
     * ------------------------------- category 1. 로딩 다이얼로그 -------------------------------
     */
    // (1) 로딩 다이얼로그
    public void showLoadingDialog(AppCompatActivity activity, String message) {
        if (loadingDialog == null) loadingDialog = new DialogLoading();
        Bundle bundle = new Bundle();
        if (message == null) {
            bundle.putString("message", activity.getString(R.string.loading_message));
        } else {
            bundle.putString("message", message);
        }
        loadingDialog.setArguments(bundle);
        if (!loadingDialog.isAdded()) {
            activity.getSupportFragmentManager()
                    .beginTransaction()
                    .add(loadingDialog, "DialogLoading")
                    .commitAllowingStateLoss();
        }
    }

    public void dismissLoading() {
        if (loadingDialog != null && loadingDialog.isAdded()) loadingDialog.dismiss();
    }

    /**
     * ------------------------------- category 2. Alert 다이얼로그 -------------------------------
     */
    /* 확인, 취소 버튼이 있는 Alert 다이얼로그 */
    public void showSimpleAlert(AppCompatActivity activity, int id, String message) {
        SimpleAlertDialog simpleAlertDialog = new SimpleAlertDialog();
        Bundle bundle = new Bundle();
        bundle.putInt("id", id);
        bundle.putString("message", message);
        simpleAlertDialog.setArguments(bundle);
        if (!simpleAlertDialog.isAdded()) {
            activity.getSupportFragmentManager()
                    .beginTransaction()
                    .add(simpleAlertDialog, "SimpleAlertDialog")
                    .commitAllowingStateLoss();
        }
    }

    /* 확인 버튼만 있는 Alert 다이얼로그 보여주는 메소드 */
    public void showConfirmDialog(AppCompatActivity activity, int id, String message) {
        SimpleConfirmDialog simpleConfirmDialog = new SimpleConfirmDialog();
        Bundle bundle = new Bundle();
        bundle.putInt("id", id);
        bundle.putString("message", message);
        simpleConfirmDialog.setArguments(bundle);
        if (!simpleConfirmDialog.isAdded()) {
            activity.getSupportFragmentManager()
                    .beginTransaction()
                    .add(simpleConfirmDialog, "SimpleConfirmDialog")
                    .commitAllowingStateLoss();
        }
    }
}

