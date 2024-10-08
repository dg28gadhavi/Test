package com.sec.internal.ims.fcm;

import android.content.Context;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.sec.internal.ims.fcm.interfaces.IFcmEventListener;
import com.sec.internal.ims.fcm.interfaces.IFcmHandler;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FcmHandler implements IFcmHandler {
    public static final String API_KEY = "AIzaSyC9rGRRr3J16mn510MIjZx0DbCEbwesCbM";
    public static final String FIREBASE_URL = "https://fir-e287d.firebaseio.com";
    private static final String LOG_TAG = "FcmHandler";
    public static final String MOBILESDK_APP_ID = "1:907837128383:android:63ec13a18eb17af2";
    public static final String PROJECT_ID = "fir-e287d";
    public static final String PROJECT_NUMBER = "907837128383";
    public static final String STORAGE_BUCKET = "fir-e287d.appspot.com";
    private List<IFcmEventListener> mFcmEventListeners = new ArrayList();

    public FcmHandler(Context context) {
        try {
            FirebaseApp.initializeApp(context, new FirebaseOptions.Builder().setApplicationId(MOBILESDK_APP_ID).setApiKey(API_KEY).setDatabaseUrl(FIREBASE_URL).setGcmSenderId(PROJECT_NUMBER).setProjectId(PROJECT_ID).setStorageBucket(STORAGE_BUCKET).build());
            IMSLog.i(LOG_TAG, "FirebaseApp initialization successful");
        } catch (Exception e) {
            String str = LOG_TAG;
            IMSLog.e(str, "FirebaseApp initialization unsuccessful: " + e.getMessage());
        }
    }

    public void registerFcmEventListener(IFcmEventListener iFcmEventListener) {
        unregisterFcmEventListener(iFcmEventListener);
        String str = LOG_TAG;
        IMSLog.i(str, "registerFcmEventListener: fcmEventListener: " + iFcmEventListener);
        this.mFcmEventListeners.add(iFcmEventListener);
    }

    public void unregisterFcmEventListener(IFcmEventListener iFcmEventListener) {
        String str = LOG_TAG;
        IMSLog.i(str, "unregisterFcmEventListener: fcmEventListener: " + iFcmEventListener);
        this.mFcmEventListeners.remove(iFcmEventListener);
    }

    public void onMessageReceived(Context context, String str, Map map) {
        IMSLog.i(LOG_TAG, "onMessageReceived:");
        for (IFcmEventListener next : this.mFcmEventListeners) {
            String str2 = LOG_TAG;
            IMSLog.i(str2, "onMessageReceived: listener: " + next);
            next.onMessageReceived(context, str, map);
        }
    }

    public void onTokenRefresh(Context context) {
        IMSLog.i(LOG_TAG, "onTokenRefresh:");
        for (IFcmEventListener next : this.mFcmEventListeners) {
            String str = LOG_TAG;
            IMSLog.i(str, "onTokenRefresh: listener: " + next);
            next.onTokenRefresh(context);
        }
    }
}
