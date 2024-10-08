package com.sec.internal.ims.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.provider.Telephony;
import android.text.TextUtils;
import android.util.Log;
import com.samsung.android.feature.SemFloatingFeature;
import com.sec.internal.constants.ims.os.SecFeature;

public class MessagingAppInfoReceiver extends BroadcastReceiver {
    private static final String ACTION_PACKAGE_REPLACED = "android.intent.action.PACKAGE_REPLACED";
    private static final String ANDROID_MESSAGE_APP = "com.google.android.apps.messaging";
    private static final String DATA_SCHEME_PACKAGE = "package";
    private static final String LOG_TAG = MessagingAppInfoReceiver.class.getSimpleName();
    private static final String SAMSUNG_MESSAGE_APP = "com.samsung.android.messaging";
    private final Context mContext;
    public MsgApp mDefaultMsgApp;
    private IntentFilter mFilter;
    private boolean mIsRegistered;
    private final IMessagingAppInfoListener mListener;
    public String mMsgAppVersion;
    private final String mPackageName_SM;

    public enum MsgApp {
        SAMSUNG_MESSAGE,
        ANDROID_MESSAGE,
        ETC
    }

    public MessagingAppInfoReceiver(Context context, IMessagingAppInfoListener iMessagingAppInfoListener) {
        this.mContext = context;
        this.mListener = iMessagingAppInfoListener;
        String string = SemFloatingFeature.getInstance().getString(SecFeature.FLOATING.CONFIG_PACKAGE_NAME);
        this.mPackageName_SM = TextUtils.isEmpty(string) ? "com.samsung.android.messaging" : string;
        this.mDefaultMsgApp = getDefaultMsgAPP();
        this.mMsgAppVersion = getMessagingAppVersion();
        setIntentFilterForDefaultMsgApp();
        this.mIsRegistered = false;
    }

    private void setIntentFilterForDefaultMsgApp() {
        if (this.mDefaultMsgApp == MsgApp.ETC) {
            this.mFilter = null;
            return;
        }
        IntentFilter intentFilter = new IntentFilter();
        this.mFilter = intentFilter;
        intentFilter.addAction(ACTION_PACKAGE_REPLACED);
        this.mFilter.addDataScheme(DATA_SCHEME_PACKAGE);
        int i = AnonymousClass1.$SwitchMap$com$sec$internal$ims$util$MessagingAppInfoReceiver$MsgApp[this.mDefaultMsgApp.ordinal()];
        if (i == 1) {
            this.mFilter.addDataSchemeSpecificPart(this.mPackageName_SM, 0);
        } else if (i == 2) {
            this.mFilter.addDataSchemeSpecificPart(ANDROID_MESSAGE_APP, 0);
        }
    }

    /* renamed from: com.sec.internal.ims.util.MessagingAppInfoReceiver$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$ims$util$MessagingAppInfoReceiver$MsgApp;

        /* JADX WARNING: Can't wrap try/catch for region: R(6:0|1|2|3|4|6) */
        /* JADX WARNING: Code restructure failed: missing block: B:7:?, code lost:
            return;
         */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0012 */
        static {
            /*
                com.sec.internal.ims.util.MessagingAppInfoReceiver$MsgApp[] r0 = com.sec.internal.ims.util.MessagingAppInfoReceiver.MsgApp.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$sec$internal$ims$util$MessagingAppInfoReceiver$MsgApp = r0
                com.sec.internal.ims.util.MessagingAppInfoReceiver$MsgApp r1 = com.sec.internal.ims.util.MessagingAppInfoReceiver.MsgApp.SAMSUNG_MESSAGE     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = $SwitchMap$com$sec$internal$ims$util$MessagingAppInfoReceiver$MsgApp     // Catch:{ NoSuchFieldError -> 0x001d }
                com.sec.internal.ims.util.MessagingAppInfoReceiver$MsgApp r1 = com.sec.internal.ims.util.MessagingAppInfoReceiver.MsgApp.ANDROID_MESSAGE     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.util.MessagingAppInfoReceiver.AnonymousClass1.<clinit>():void");
        }
    }

    public void onReceive(Context context, Intent intent) {
        String str = LOG_TAG;
        Log.d(str, "onReceive(): intent - " + intent);
        this.mMsgAppVersion = getMessagingAppVersion();
        this.mListener.onMessagingAppPackageReplaced();
    }

    public void registerReceiver() {
        MsgApp defaultMsgAPP = getDefaultMsgAPP();
        if (!this.mIsRegistered || this.mDefaultMsgApp != defaultMsgAPP) {
            unregisterReceiver();
            this.mDefaultMsgApp = defaultMsgAPP;
            setIntentFilterForDefaultMsgApp();
            this.mMsgAppVersion = getMessagingAppVersion();
            String str = LOG_TAG;
            Log.d(str, "registerReceiver(): IsRegistered = " + this.mIsRegistered + ", mDefaultMsgApp = " + this.mDefaultMsgApp);
            if (this.mDefaultMsgApp == MsgApp.ETC) {
                Log.d(str, "registerReceiver(): does not need to registe receiver.");
                return;
            }
            this.mContext.registerReceiver(this, this.mFilter);
            this.mIsRegistered = true;
        }
    }

    public void unregisterReceiver() {
        String str = LOG_TAG;
        Log.d(str, "unregisterReceiver(): IsRegistered = " + this.mIsRegistered);
        if (this.mIsRegistered) {
            this.mContext.unregisterReceiver(this);
            this.mIsRegistered = false;
        }
    }

    public String getMessagingAppVersion() {
        PackageInfo packageInfo;
        String str = "";
        try {
            PackageManager packageManager = this.mContext.getPackageManager();
            int i = AnonymousClass1.$SwitchMap$com$sec$internal$ims$util$MessagingAppInfoReceiver$MsgApp[this.mDefaultMsgApp.ordinal()];
            if (i == 1) {
                packageInfo = packageManager.getPackageInfo(this.mPackageName_SM, 0);
            } else if (i != 2) {
                packageInfo = null;
            } else {
                packageInfo = packageManager.getPackageInfo(ANDROID_MESSAGE_APP, 0);
            }
            if (packageInfo != null) {
                str = packageInfo.versionName;
            }
        } catch (PackageManager.NameNotFoundException unused) {
            Log.e(LOG_TAG, "getMessagingAppVersion(): Cannot find the package.");
        }
        String str2 = LOG_TAG;
        Log.d(str2, "getMessagingAppVersion(): " + this.mDefaultMsgApp + " - " + str);
        return str;
    }

    public MsgApp getDefaultMsgAPP() {
        String str;
        try {
            str = Telephony.Sms.getDefaultSmsPackage(this.mContext);
        } catch (Exception e) {
            String str2 = LOG_TAG;
            Log.e(str2, "Failed to currentDefaultMsgApp: " + e);
            str = null;
        }
        if (str == null) {
            str = Settings.Secure.getString(this.mContext.getContentResolver(), "sms_default_application");
        }
        if (TextUtils.equals(str, this.mPackageName_SM)) {
            return MsgApp.SAMSUNG_MESSAGE;
        }
        if (TextUtils.equals(str, ANDROID_MESSAGE_APP)) {
            return MsgApp.ANDROID_MESSAGE;
        }
        return MsgApp.ETC;
    }
}
