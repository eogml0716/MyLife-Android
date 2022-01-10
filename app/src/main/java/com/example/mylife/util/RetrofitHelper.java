package com.example.mylife.util;

import android.util.Log;

import com.example.mylife.MyApplication;
import com.example.mylife.api.RetrofitInterface;

import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitHelper {
    private final String TAG = "RetrofitHelper";
    private static Retrofit retrofit = null;
    private static RetrofitHelper retrofitHelper;
    private RetrofitInterface retrofitInterFace;

    private RetrofitHelper() {
        retrofitInterFace = getRetrofitHelper();
    }

    public static RetrofitInterface getRetrofitHelper() {
        if (retrofit == null) {
            retrofit = new Retrofit
                    .Builder()
                    .baseUrl(MyApplication.SERVER_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(RetrofitInterface.class);
    }

    public synchronized static RetrofitHelper getInstance() {
        if (retrofitHelper == null) retrofitHelper = new RetrofitHelper();
        return retrofitHelper;
    }

    // Retrofit과 통신 시 에러 발생하면, 로그 찍는 메소드
    public void printRetrofitResponse(String TAG, Response response) {
        try {
            Log.e(TAG, "printRetrofitResponse : " + response.errorBody().string());
        } catch (Exception e) {
            Log.e(TAG, "printRetrofitResponse : " + e);
            e.printStackTrace();
        }
    }
}
