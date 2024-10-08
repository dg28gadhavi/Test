package com.sec.internal.helper.os;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import com.samsung.android.feature.SemFloatingFeature;
import com.sec.internal.constants.ims.os.SecFeature;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.log.IMSLog;
import java.util.List;

public class PackageUtils {
    private static final String LOG_TAG = ImsUtil.class.getSimpleName();
    private static final String ONETALK_API_SERVICE_PACKAGE = "com.samsung.vzwapiservice";

    public static String getMsgAppPkgName(Context context) {
        String string = SemFloatingFeature.getInstance().getString(SecFeature.FLOATING.CONFIG_PACKAGE_NAME);
        if (string == null || "com.android.mms".equals(string)) {
            return "com.android.mms";
        }
        try {
            context.getPackageManager().getPackageInfo(string, 0);
            return string;
        } catch (PackageManager.NameNotFoundException unused) {
            return "com.android.mms";
        }
    }

    public static boolean isProcessRunning(Context context, String str) {
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = ((ActivityManager) context.getSystemService("activity")).getRunningAppProcesses();
        if (runningAppProcesses == null) {
            return false;
        }
        for (ActivityManager.RunningAppProcessInfo next : runningAppProcesses) {
            if (next != null && TextUtils.equals(str, next.processName)) {
                return true;
            }
        }
        return false;
    }

    public static String getProcessNameById(Context context, int i) {
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = ((ActivityManager) context.getSystemService("activity")).getRunningAppProcesses();
        if (runningAppProcesses == null) {
            return "";
        }
        for (ActivityManager.RunningAppProcessInfo next : runningAppProcesses) {
            if (next != null && next.pid == i) {
                return next.processName;
            }
        }
        return "";
    }

    public static boolean hasPackage(Context context, String str) {
        try {
            context.getPackageManager().getApplicationInfo(str, 128);
            return true;
        } catch (PackageManager.NameNotFoundException unused) {
            String str2 = LOG_TAG;
            IMSLog.i(str2, "Package not found : " + str);
            return false;
        }
    }

    public static boolean hasComponent(Context context, ComponentName componentName) {
        try {
            context.getPackageManager().getActivityInfo(componentName, 128);
            return true;
        } catch (PackageManager.NameNotFoundException unused) {
            String str = LOG_TAG;
            IMSLog.i(str, "Component not found : " + componentName);
            return false;
        }
    }

    public static boolean isOneTalkFeatureEnabled(Context context) {
        ComponentName componentName = new ComponentName("com.verizon.onetalk.dialer", "com.verizon.onetalk.dialer.CallSettingsActivity");
        PackageManager packageManager = context.getPackageManager();
        boolean z = false;
        if (packageManager != null && hasPackage(context, "com.verizon.onetalk.dialer")) {
            try {
                if (packageManager.getApplicationEnabledSetting("com.verizon.onetalk.dialer") != 2) {
                    String str = LOG_TAG;
                    IMSLog.i(str, "isBusinessVoltePackageEnabled: true");
                    if (hasComponent(context, componentName)) {
                        IMSLog.i(str, "isBusinessVolteComponentPresent: true");
                        if (packageManager.getComponentEnabledSetting(new ComponentName("com.verizon.onetalk.dialer", "com.verizon.onetalk.dialer.CallSettingsActivity")) != 2) {
                            z = true;
                        }
                    }
                }
            } catch (IllegalArgumentException e) {
                String str2 = LOG_TAG;
                IMSLog.i(str2, "isActivityEnabled, IllegalArgumentException : " + e.toString());
            }
        }
        String str3 = LOG_TAG;
        IMSLog.i(str3, "isOneTalkFeatureEnabled - " + z);
        return z;
    }
}
