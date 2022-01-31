package com.example.mylife.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.mylife.R;

public class DialogLoading extends DialogFragment {
    private final String TAG = "DialogLoading";
//    private LayoutInflater inflater;
//    private View dialogLoading;
//    private TextView tvLoadingMessage;
//    private String loadingMessage;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
//        setInitData();
//        bindView();
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogLoading = inflater.inflate(R.layout.dialog_loading, null);
        TextView tvLoadingMessage = dialogLoading.findViewById(R.id.tv_loading_message);
        String loadingMessage = requireArguments().getString("message");
        tvLoadingMessage.setText(loadingMessage);

        setCancelable(false);
        return new AlertDialog.Builder(requireContext())
                .setView(dialogLoading)
                .create();
    }

    // TODO: setInitData()와 bindView로 명확하게 나누어서 처리하기가 어려운 파일
    // bindView를 제일 처음 실행하는 코드로 두니까 inflater가 정의가 안되어서 오류가 남
    // setInitData를 제일 처음 실행하는 코드로 두니까 tvLoadingMessage가 findViewById가 안되어서 setText에서 에러가 뜸
//    /**
//     * ------------------------------- category 0. 최초 설정 -------------------------------
//     */
//    private void setInitData() {
//        inflater = requireActivity().getLayoutInflater();
//        loadingMessage = requireArguments().getString("message");
//
//        tvLoadingMessage.setText(loadingMessage);
//    }
//
//    private void bindView() {
//        dialogLoading = inflater.inflate(R.layout.dialog_loading, null);
//        tvLoadingMessage = dialogLoading.findViewById(R.id.tv_loading_message);
//    }
}