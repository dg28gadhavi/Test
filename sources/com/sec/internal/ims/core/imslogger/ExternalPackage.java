package com.sec.internal.ims.core.imslogger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import com.samsung.android.emergencymode.SemEmergencyManager;
import com.sec.internal.interfaces.ims.core.imslogger.ISignallingNotifier;

public class ExternalPackage implements ISignallingNotifier {
    /* access modifiers changed from: private */
    public static final String LOG_TAG = "ExternalPackage";
    private Context mContext;
    /* access modifiers changed from: private */
    public String mPackageName;
    /* access modifiers changed from: private */
    public ISignallingNotifier.PackageStatus mPackageStatus = ISignallingNotifier.PackageStatus.NOT_INSTALLED;

    public ExternalPackage(Context context, String str) {
        this.mContext = context;
        this.mPackageName = str;
        this.mPackageStatus = checkPackageStatus();
        String str2 = LOG_TAG;
        Log.i(str2, "name: " + this.mPackageName + " status: " + this.mPackageStatus);
        registerPackageAction();
    }

    public ISignallingNotifier.PackageStatus checkPackageStatus() {
        ISignallingNotifier.PackageStatus packageStatus;
        ISignallingNotifier.PackageStatus packageStatus2 = ISignallingNotifier.PackageStatus.NOT_INSTALLED;
        try {
            int applicationEnabledSetting = this.mContext.getPackageManager().getApplicationEnabledSetting(this.mContext.getPackageManager().getPackageInfo(this.mPackageName, 1).packageName);
            if (SemEmergencyManager.isEmergencyMode(this.mContext) || applicationEnabledSetting >= 2) {
                packageStatus = ISignallingNotifier.PackageStatus.EMERGENCY_MODE;
            } else {
                packageStatus = ISignallingNotifier.PackageStatus.INSTALLED;
            }
        } catch (PackageManager.NameNotFoundException unused) {
            packageStatus = ISignallingNotifier.PackageStatus.NOT_INSTALLED;
        }
        String str = LOG_TAG;
        Log.i(str, "checkPackageStatus(): " + packageStatus);
        return packageStatus;
    }

    private void registerPackageAction() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.PACKAGE_ADDED");
        intentFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        intentFilter.addDataScheme("package");
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (("package:" + ExternalPackage.this.mPackageName).equalsIgnoreCase(intent.getData().toString())) {
                    ExternalPackage.this.mPackageStatus = "android.intent.action.PACKAGE_ADDED".equalsIgnoreCase(intent.getAction()) ? ISignallingNotifier.PackageStatus.INSTALLED : ISignallingNotifier.PackageStatus.NOT_INSTALLED;
                    String r2 = ExternalPackage.LOG_TAG;
                    Log.i(r2, "name: " + ExternalPackage.this.mPackageName + " status: " + ExternalPackage.this.mPackageStatus);
                }
            }
        }, intentFilter);
    }

    private boolean isAllow() {
        return this.mPackageStatus == ISignallingNotifier.PackageStatus.INSTALLED;
    }

    public boolean send(Object obj) {
        if (!Bundle.class.getSimpleName().equals(obj.getClass().getSimpleName())) {
            return true;
        }
        Bundle bundle = (Bundle) obj;
        if (!isAllow() || bundle.getInt("notifyType") != 0) {
            return true;
        }
        Intent intent = new Intent(ISignallingNotifier.ACTION_SIP_MESSAGE);
        intent.setPackage(this.mPackageName);
        intent.putExtras(bundle);
        this.mContext.sendBroadcast(intent, ISignallingNotifier.PERMISSION);
        return true;
    }
}
