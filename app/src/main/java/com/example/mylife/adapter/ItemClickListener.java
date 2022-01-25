package com.example.mylife.adapter;

import android.view.View;

import java.text.ParseException;

public interface ItemClickListener extends View.OnClickListener {
    void onClickItem(View view, int position);
}
