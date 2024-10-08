package com.sec.internal.ims.config.workflow;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.extensions.ContextExt;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.log.IMSLog;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

class WorkflowMsisdnHandler {
    protected static final String IS_NEEDED = "isNeeded";
    /* access modifiers changed from: private */
    public static final String LOG_TAG = "WorkflowMsisdnHandler";
    protected static final String MSISDN_KEYS_ARRAY = "msisdnArray";
    protected static final long MSISDN_MAX_TIMEOUT = 604800;
    protected static final String MSISDN_VALUE = "msisdnValue";
    protected static final String SET_SHOW_MSISDN_DIALOG = "com.sec.rcs.config.action.SET_SHOW_MSISDN_DIALOG";
    protected Set<String> mMsisdnKeys;
    protected WorkflowBase mWorkflowBase;

    /* access modifiers changed from: protected */
    public boolean getIsNeeded() {
        return true;
    }

    public WorkflowMsisdnHandler(WorkflowBase workflowBase) {
        this.mWorkflowBase = workflowBase;
    }

    /* access modifiers changed from: protected */
    public int getMsisdnSkipCount() {
        int i;
        Log.i(LOG_TAG, "getMsisdnSkipCount");
        String read = this.mWorkflowBase.mStorage.read(ConfigConstants.PATH.MSISDN_SKIP_COUNT);
        if (!TextUtils.isEmpty(read)) {
            try {
                i = Integer.parseInt(read);
            } catch (NullPointerException | NumberFormatException e) {
                e.printStackTrace();
                i = 0;
            }
        } else {
            i = -1;
        }
        String str = LOG_TAG;
        Log.i(str, "getMsisdnSkipCount :" + i);
        return i;
    }

    /* access modifiers changed from: protected */
    public void setMsisdnSkipCount(int i) {
        Log.i(LOG_TAG, "setMsisdnSkipValue");
        this.mWorkflowBase.mStorage.write(ConfigConstants.PATH.MSISDN_SKIP_COUNT, String.valueOf(i));
    }

    /* access modifiers changed from: protected */
    public void setMsisdnMsguiDisplay(String str) {
        Log.i(LOG_TAG, "setMsisdnMsguiDisplay");
        this.mWorkflowBase.mStorage.write(ConfigConstants.PATH.MSISDN_MSGUI_DISPLAY, str);
    }

    /* access modifiers changed from: protected */
    public String getLastMsisdnValue() {
        SharedPreferences sharedPreferences = this.mWorkflowBase.mSharedPreferences;
        String string = sharedPreferences.getString(MSISDN_VALUE + this.mWorkflowBase.mTelephonyAdapter.getImsi(), (String) null);
        String str = LOG_TAG;
        Log.i(str, "getLastMsisdnValue: " + IMSLog.checker(string));
        return string;
    }

    /* access modifiers changed from: protected */
    public void setMsisdnValue(String str) {
        String str2 = LOG_TAG;
        Log.i(str2, "setMsisdnValue: " + IMSLog.checker(str));
        SharedPreferences.Editor edit = this.mWorkflowBase.mSharedPreferences.edit();
        edit.putString(MSISDN_VALUE + this.mWorkflowBase.mTelephonyAdapter.getImsi(), str);
        Set<String> stringSet = this.mWorkflowBase.mSharedPreferences.getStringSet(MSISDN_KEYS_ARRAY, new HashSet());
        this.mMsisdnKeys = stringSet;
        stringSet.add(MSISDN_VALUE + this.mWorkflowBase.mTelephonyAdapter.getImsi());
        edit.putStringSet(MSISDN_KEYS_ARRAY, this.mMsisdnKeys);
        edit.apply();
    }

    /* access modifiers changed from: protected */
    public void setMsisdnTimer(CountDownTimer countDownTimer) {
        long startMsisdnTime = getStartMsisdnTime();
        String str = LOG_TAG;
        Log.i(str, "startMsisdnTime: " + startMsisdnTime);
        if (startMsisdnTime == -1) {
            Log.i(str, "msisdn timer was already called, so skip");
            return;
        }
        Date date = new Date();
        if (startMsisdnTime == 0) {
            startMsisdnTime = date.getTime() + ImsConstants.SimMobilityKitTimer.BASIC_INTERVAL;
            this.mWorkflowBase.mStorage.write(ConfigConstants.PATH.START_MSISDN_TIMER, String.valueOf(startMsisdnTime));
        }
        startMsisdnTimer(countDownTimer, (int) ((startMsisdnTime - date.getTime()) / 1000));
    }

    /* access modifiers changed from: protected */
    public long getStartMsisdnTime() {
        long j;
        try {
            j = Long.parseLong(this.mWorkflowBase.mStorage.read(ConfigConstants.PATH.START_MSISDN_TIMER));
        } catch (NullPointerException | NumberFormatException e) {
            e.printStackTrace();
            j = 0;
        }
        String str = LOG_TAG;
        Log.i(str, "getStartMsisdnTime: " + j);
        return j;
    }

    /* access modifiers changed from: protected */
    public void startMsisdnTimer(CountDownTimer countDownTimer, int i) {
        String str = LOG_TAG;
        Log.i(str, "start msisdnTimer(" + i + "sec)");
        cancelMsisdnTimer(countDownTimer, false);
        long j = (long) i;
        final int i2 = i;
        new CountDownTimer(j * 1000, 100 * j) {
            public void onTick(long j) {
                String r2 = WorkflowMsisdnHandler.LOG_TAG;
                Log.i(r2, "validity tick(" + j + ")");
            }

            public void onFinish() {
                String r0 = WorkflowMsisdnHandler.LOG_TAG;
                Log.i(r0, "msisdnTimer expired(" + i2 + ").");
                WorkflowMsisdnHandler.this.expiredMsisdnTimer();
            }
        }.start();
    }

    /* access modifiers changed from: protected */
    public void cancelMsisdnTimer(CountDownTimer countDownTimer, boolean z) {
        String str = LOG_TAG;
        Log.i(str, "cancelMsisdnTimer");
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        if (z) {
            Log.i(str, "cancelMsisdnTimer: disable DB of START_MSISDN_TIMER");
            this.mWorkflowBase.mStorage.write(ConfigConstants.PATH.START_MSISDN_TIMER, "-1");
        }
    }

    /* access modifiers changed from: protected */
    public void expiredMsisdnTimer() {
        WorkflowBase workflowBase = this.mWorkflowBase;
        boolean isRcsAvailable = ConfigUtil.isRcsAvailable(workflowBase.mContext, workflowBase.mPhoneId, workflowBase.mSm);
        String str = LOG_TAG;
        Log.i(str, "expiredMsisdnTimer: userSetting: " + isRcsAvailable);
        if (isRcsAvailable) {
            Intent intent = new Intent();
            intent.setAction(SET_SHOW_MSISDN_DIALOG);
            intent.putExtra(IS_NEEDED, getIsNeeded());
            ContextExt.sendBroadcastAsUser(this.mWorkflowBase.mContext, intent, ContextExt.ALL);
            this.mWorkflowBase.mStorage.write(ConfigConstants.PATH.MSISDN_MSGUI_DISPLAY, CloudMessageProviderContract.JsonData.TRUE);
        }
        this.mWorkflowBase.mStorage.write(ConfigConstants.PATH.START_MSISDN_TIMER, "-1");
    }
}
