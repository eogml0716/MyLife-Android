package com.example.mylife.util;

import android.app.Activity;
import android.util.Log;
import android.view.View;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

public class MethodHelper {
    private final String TAG = "MethodHelper";
    private static MethodHelper methodHelper;

    private MethodHelper() {
    }

    public static MethodHelper getInstance() {
        if (methodHelper == null) methodHelper = new MethodHelper();
        return methodHelper;
    }

    // TODO : TagUsed라는 파라미터 값을 받아서, Log를 찍는 게 디버깅을 하는 것에 도움이 될까?, 써보면서 생각을 해봐야할 거 같다.
    public String getTextInputLayoutString(String TagUsed, TextInputLayout textInputLayout) {
        // 공백이 아닐 때 처리할 내용
        if (textInputLayout.getEditText().getText().length() != 0) {
            Log.d(TAG, TagUsed + " getTextInputLayoutString : " + textInputLayout.getEditText().getText());
            return textInputLayout.getEditText().getText().toString();
        }
        // 공백일 때 처리할 내용
        Log.e(TAG, TagUsed + " getTextInputLayoutString 공백일 때 : " + textInputLayout.getEditText().getText());
        return null;
    }

    public void showSnackBar(String TagUsed, Activity activity, int message){
        View rootView = activity.getWindow().getDecorView().findViewById(android.R.id.content);
        Snackbar.make(rootView, message, BaseTransientBottomBar.LENGTH_INDEFINITE).show();
    }
}
