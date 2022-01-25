package com.example.mylife.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mylife.R;
import com.example.mylife.adapter.ItemClickListener;


public class PostingFragment extends Fragment implements View.OnClickListener  {

    public PostingFragment() {

    }


    public static PostingFragment newInstance(String param1, String param2) {


    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_posting, container, false);
    }

    @Override
    public void onClick(View v) {

    }
}