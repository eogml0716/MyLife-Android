package com.example.mylife.util;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

public class NetworkConnection {
    private final String TAG = "NetworkConnection";
    private static NetworkConnection networkConnection;
    public static final int TYPE_NOT_CONNECTED = -1;

    public synchronized static NetworkConnection getInstance() {
        if (networkConnection == null) networkConnection = new NetworkConnection();
        return networkConnection;
    }

    /**
     * checkNetworkConnection
     * 인터넷 연결 체크 : 인터넷이 연결되어 있지 않을 경우 서버에서 데이터를 불러오지 못하므로 접속 제한을 둔다.
     * + 로그 데이터를 쌓기 위해 어떠한 인터넷으로 접속하는 지 상세히 체크한다.
     * @param activity
     * @return
     */
    public int checkNetworkConnection(Activity activity) {
        if (activity != null) {
            ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
            /* NetworkInfo has been deprecated by API 29 [duplicate] */
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Network network = connectivityManager.getActiveNetwork();
                if (network == null) return TYPE_NOT_CONNECTED;
                NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
                if (networkCapabilities != null) {
                    if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        Log.d(TAG, "checkNetworkConnection : " + NetworkCapabilities.TRANSPORT_WIFI);
                        return NetworkCapabilities.TRANSPORT_WIFI;
                    } else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        Log.d(TAG, "checkNetworkConnection : " + NetworkCapabilities.TRANSPORT_CELLULAR);
                        return NetworkCapabilities.TRANSPORT_CELLULAR;
                    } else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                        Log.d(TAG, "checkNetworkConnection : " + NetworkCapabilities.TRANSPORT_ETHERNET);
                        return NetworkCapabilities.TRANSPORT_ETHERNET;
                    } else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH)) {
                        Log.d(TAG, "checkNetworkConnection : " + NetworkCapabilities.TRANSPORT_BLUETOOTH);
                        return NetworkCapabilities.TRANSPORT_BLUETOOTH;
                    }
                }
            } else {
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                if (networkInfo == null) return TYPE_NOT_CONNECTED;
                int networkType = networkInfo.getType();
                Log.d(TAG, "checkNetworkConnection : " + networkType);
                if (networkType == ConnectivityManager.TYPE_MOBILE) {
                    Log.d(TAG, "checkNetworkConnection : " + ConnectivityManager.TYPE_MOBILE);
                    return ConnectivityManager.TYPE_MOBILE;
                } else if (networkType == ConnectivityManager.TYPE_WIFI) {
                    Log.d(TAG, "checkNetworkConnection : " + ConnectivityManager.TYPE_WIFI);
                    return ConnectivityManager.TYPE_WIFI;
                }
            }
        }
        return TYPE_NOT_CONNECTED;
    }
}