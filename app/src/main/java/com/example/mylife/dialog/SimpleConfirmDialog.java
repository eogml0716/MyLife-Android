package com.example.mylife.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.mylife.R;

public class SimpleConfirmDialog extends DialogFragment implements View.OnClickListener {
    private String TAG = "SimpleConfirmDialog";
    private DialogListener dialogListener;

    private int id;
    private String message;

    private View dialogSimpleConfirmAlert;
    private TextView tvMessage, btnConfirm;

    public interface DialogListener {
        void onConfirm(int id);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        bindView();
        setInitData();
        setListener();

        return new AlertDialog.Builder(requireContext())
                .setView(dialogSimpleConfirmAlert)
                .create();
    }

    /**
     * ------------------------------- category 0. 최초 설정 -------------------------------
     */
    private void setInitData() {
        id = requireArguments().getInt("id");
        message = requireArguments().getString("message");

        tvMessage.setText(message);
    }

    private void bindView() {
        dialogSimpleConfirmAlert = requireActivity().getLayoutInflater().inflate(R.layout.dialog_simple_confirm_alert, null);

        tvMessage = dialogSimpleConfirmAlert.findViewById(R.id.tv_message);
        btnConfirm = dialogSimpleConfirmAlert.findViewById(R.id.btn_confirm);

        btnConfirm.setOnClickListener(this);
    }

    /**
     * ------------------------------- category 1. 리스너 관련 -------------------------------
     */
    private void setListener() {
        try {
            // TODO : 이거 왜 있는건지 체크하기
            if (id == 0) {
                Log.d(TAG, "setListener() - if (id == 0)");
                return;
            }
            // TODO : 이거 왜 있는건지 체크하기
            if (id == -1) {
                Log.d(TAG, "setListener() - if (id == -1)");
                return;
            }
            dialogListener = (DialogListener) getActivity();
        } catch (ClassCastException e) {
            e.printStackTrace();
            Log.e(TAG, "setListener() : " + e);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_confirm:
                if (dialogListener != null) dialogListener.onConfirm(id);
                // TODO : 이거 왜 있는건지 체크하기
                if (id == -1 && !getActivity().isFinishing()) {
                    Log.d(TAG, "onClick() - if (id == -1 && !getActivity().isFinishing())");
                    getActivity().finish(); // Activity 종료 id인 경우 DialogHelper 클래스에 정의되어 있다.
                }
                dismiss();
                break;
        }
    }

    /**
     * ------------------------------- category 2. 생명주기 관련 -------------------------------
     */
    @Override
    public void onDetach() {
        super.onDetach();
        dialogListener = null;
    }
}
