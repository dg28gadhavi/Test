package com.sec.imsservice;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Intent;
import android.database.sqlite.SQLiteFullException;
import android.os.Process;
import android.util.Log;
import com.sec.ims.extensions.Extensions;
import com.sec.internal.constants.ims.entitilement.SoftphoneContract;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.imsservice.ImsService;
import com.sec.internal.ims.imsservice.ImsServiceStub;
import java.util.List;

public class ImsFrameworkApp extends Application {
    private static final String TAG = "ImsFrameworkApp";

    public void onCreate() {
        super.onCreate();
        if (Extensions.UserHandle.myUserId() != 0) {
            Log.e(TAG, "Do not initialize on non-system user");
            return;
        }
        int myPid = Process.myPid();
        ActivityManager activityManager = (ActivityManager) getSystemService("activity");
        if (activityManager == null) {
            Log.e(TAG, "Do not initalize IMS when AM is null");
            return;
        }
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = activityManager.getRunningAppProcesses();
        String str = "";
        if (runningAppProcesses != null && runningAppProcesses.size() > 0) {
            for (ActivityManager.RunningAppProcessInfo next : runningAppProcesses) {
                if (next != null && next.pid == myPid) {
                    str = next.processName;
                }
            }
        }
        Log.i(TAG, "current process :" + str);
        if (str.endsWith(":ConfigService")) {
            Log.i(TAG, "this is rcs config process. stop init");
        } else if (str.endsWith(":CloudMessageService")) {
            Log.i(TAG, "this is CloudMessage process.");
            try {
                startService(new Intent(this, Class.forName("com.sec.internal.ims.cmstore.CloudMessageService")));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else if (str.endsWith(":CABService")) {
            Log.i(TAG, "this is CABService process. stop init");
        } else {
            Log.i(TAG, "onCreate()");
            try {
                ImsServiceStub.makeImsService(this);
                startService(new Intent(this, ImsService.class));
            } catch (SQLiteFullException e2) {
                Log.e(TAG, "makeImsService " + e2.getMessage());
            }
            if (SimUtil.isSoftphoneEnabled()) {
                try {
                    startService(new Intent(this, Class.forName(SoftphoneContract.SERVICE_CLASS_NAME)));
                } catch (ClassNotFoundException e3) {
                    e3.printStackTrace();
                }
            }
        }
    }
}
