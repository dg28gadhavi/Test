package com.sec.internal.log;

import android.os.SystemClock;

public class IMSLogTimer {
    private static final String TAG = "IMSLogTimer";
    private static boolean[] mIsImsPdnRequest = {false, false};
    private static long[] mLatchEndTime = {0, 0};
    private static long[] mLatchStartTime = {0, 0};
    private static long[] mPdnEndTime = {0, 0};
    private static long[] mPdnStartTime = {0, 0};
    private static long[] mVolteRegisterEndTime = {0, 0};
    private static long[] mVolteRegisterStartTime = {0, 0};

    private static long getCurrentTime() {
        return SystemClock.elapsedRealtime();
    }

    public static boolean getIsImsPdnRequest(int i) {
        return mIsImsPdnRequest[i];
    }

    public static void setLatchStartTime(int i) {
        long currentTime = getCurrentTime();
        if (i == -1) {
            long[] jArr = mLatchStartTime;
            jArr[0] = currentTime;
            jArr[1] = currentTime;
        } else {
            mLatchStartTime[i] = currentTime;
        }
        boolean[] zArr = mIsImsPdnRequest;
        zArr[0] = false;
        zArr[1] = false;
    }

    public static long getLatchStartTime(int i) {
        return mLatchStartTime[i];
    }

    public static void setLatchEndTime(int i) {
        mLatchEndTime[i] = getCurrentTime();
    }

    public static long getLatchEndTime(int i) {
        return mLatchEndTime[i];
    }

    public static void setPdnStartTime(int i, boolean z) {
        mPdnStartTime[i] = getCurrentTime();
        mIsImsPdnRequest[i] = z;
    }

    public static long getPdnStartTime(int i) {
        if (getIsImsPdnRequest(i)) {
            return mPdnStartTime[i];
        }
        return mLatchEndTime[i];
    }

    public static void setPdnEndTime(int i) {
        mPdnEndTime[i] = getCurrentTime();
    }

    public static long getPdnEndTime(int i) {
        return mPdnEndTime[i];
    }

    public static void setVolteRegisterStartTime(int i) {
        mVolteRegisterStartTime[i] = getCurrentTime();
    }

    public static long getVolteRegisterStartTime(int i) {
        return mVolteRegisterStartTime[i];
    }

    public static void setVolteRegisterEndTime(int i) {
        mVolteRegisterEndTime[i] = getCurrentTime();
    }

    public static long getVolteRegisterEndTime(int i) {
        return mVolteRegisterEndTime[i];
    }
}
