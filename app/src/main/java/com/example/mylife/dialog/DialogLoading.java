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
    private LayoutInflater inflater;
    private View dialogLoading;
    private TextView tvLoadingMessage;
    private String loadingMessage;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        bindView();
        setInitData();

        return new AlertDialog.Builder(requireContext())
                .setView(dialogLoading)
                .setCancelable(false)
                .create();
    }

    /**
     * ------------------------------- category 0. 최초 설정 -------------------------------
     */
    private void setInitData() {
        inflater = requireActivity().getLayoutInflater();
        loadingMessage = requireArguments().getString("message");

        tvLoadingMessage.setText(loadingMessage);
    }

    private void bindView() {
        dialogLoading = inflater.inflate(R.layout.dialog_loading, null);
        tvLoadingMessage = dialogLoading.findViewById(R.id.tv_loading_message);
    }
}