package com.example.mylife.util;

import android.util.Log;

import com.example.mylife.MyApplication;
import com.example.mylife.api.RetrofitInterface;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

// TODO: Retrofit을 왜 Interface로 나누고, Helper라는 클래스를 만들어서 관리해주는 지 아직 이해를 못했음, 프로젝트 끝나면 공부하기
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
                    .addConverterFactory(new NullOnEmptyConverterFactory())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(RetrofitInterface.class);
    }

    public synchronized static RetrofitHelper getInstance() {
        if (retrofitHelper == null) retrofitHelper = new RetrofitHelper();
        return retrofitHelper;
    }

    public RetrofitInterface getRetrofitInterFace() {
        return retrofitInterFace;
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

    // TODO: 이게 뭔지 공부하기
    public static class NullOnEmptyConverterFactory extends Converter.Factory {
        @Override
        public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
            final Converter<ResponseBody, ?> delegate = retrofit.nextResponseBodyConverter(this, type, annotations);
            return (Converter<ResponseBody, Object>) body -> {
                if (body.contentLength() == 0) return null;
                return delegate.convert(body);
            };
        }
    }
}
