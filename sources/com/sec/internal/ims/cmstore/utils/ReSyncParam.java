package com.sec.internal.ims.cmstore.utils;

import android.util.Log;
import com.sec.ims.util.IMSLog;
import com.sec.internal.ims.cmstore.MessageStoreClient;

public class ReSyncParam {
    public static final String TAG = "ReSyncParam";
    private static String mChannelResUrl;
    private static String mChannelURL;
    private static String mNotifyURL;
    private static String mRestartToken;
    private static ReSyncParam sInstance = new ReSyncParam();

    private ReSyncParam() {
    }

    public static ReSyncParam getInstance() {
        return sInstance;
    }

    public static void update(MessageStoreClient messageStoreClient) {
        CloudMessagePreferenceManager prerenceManager = messageStoreClient.getPrerenceManager();
        mRestartToken = prerenceManager.getOMASSubscriptionRestartToken();
        mNotifyURL = prerenceManager.getOMACallBackURL();
        mChannelURL = prerenceManager.getOMAChannelURL();
        mChannelResUrl = prerenceManager.getOMASubscriptionResUrl();
        String str = TAG;
        Log.i(str, "ReSyncParam: mRestartToken:: " + IMSLog.checker(mRestartToken) + ",ReSyncParam: mNotifyURL:: " + IMSLog.checker(mNotifyURL) + ",ReSyncParam: mChannelURL:: " + IMSLog.checker(mChannelURL) + ",ReSyncParam: mChannelResUrl:: " + IMSLog.checker(mChannelResUrl));
    }

    public String getRestartToken() {
        return mRestartToken;
    }

    public String getNotifyURL() {
        return mNotifyURL;
    }

    public String getChannelURL() {
        return mChannelURL;
    }

    public String getChannelResURL() {
        return mChannelResUrl;
    }
}
