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
import androidx.fragment.app.FragmentManager;

import com.example.mylife.R;

import org.w3c.dom.Text;

public class SimpleAlertDialog extends DialogFragment implements View.OnClickListener {
    private final String TAG = "SimpleAlertDialog";
    private DialogListener dialogListener;

    private int id;
    private String message;

    private View dialogSimpleAlert;
    private TextView tvMessage, btnConfirm, btnCancel;

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
                .setView(dialogSimpleAlert)
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
        dialogSimpleAlert = requireActivity().getLayoutInflater().inflate(R.layout.dialog_simple_alert, null);

        tvMessage = dialogSimpleAlert.findViewById(R.id.tv_message);

        btnConfirm = dialogSimpleAlert.findViewById(R.id.btn_confirm);
        btnCancel = dialogSimpleAlert.findViewById(R.id.btn_cancel);

        btnConfirm.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
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
            if (getTargetFragment() != null) {
                Log.d(TAG, "setListener() - if (getTargetFragment() != null)");
                dialogListener = (DialogListener) getTargetFragment();
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
                dismiss();
                break;

            case R.id.btn_cancel:
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