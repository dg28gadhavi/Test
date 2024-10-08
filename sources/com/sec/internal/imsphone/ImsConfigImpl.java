package com.sec.internal.imsphone;

import android.content.ContentValues;
import android.content.Context;
import android.os.Binder;
import android.telephony.ims.RcsClientConfiguration;
import android.telephony.ims.stub.ImsConfigImplBase;
import com.sec.ims.extensions.Extensions;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityDiscoveryModule;
import com.sec.internal.interfaces.ims.servicemodules.presence.IPresenceModule;
import com.sec.internal.log.IMSLog;

public class ImsConfigImpl extends ImsConfigImplBase {
    private static final String LOG_TAG = ImsConfigImpl.class.getSimpleName();
    private static final String READ_IMS_PERMISSION = "com.sec.imsservice.READ_IMS_PERMISSION";
    private ICapabilityDiscoveryModule mCapabilityDisModule = ImsRegistry.getServiceModuleManager().getCapabilityDiscoveryModule();
    private String mClientVendor;
    private String mClientVersion;
    private final Context mContext;
    private final int mPhoneId;
    private IPresenceModule mPresenceModule = ImsRegistry.getServiceModuleManager().getPresenceModule();
    private String mRcsEnabledByUser;
    private String mRcsProfile;
    private String mRcsVersion;

    public static class FeatureValueConstants {
        public static final int ERROR = -1;
        public static final int OFF = 0;
        public static final int ON = 1;
    }

    public static class RcsClientConfigurationConstants {
        public static final int CLIENT_VENDOR = 2;
        public static final int CLIENT_VERSION = 3;
        public static final int RCS_ENABLED_BY_USER = 4;
        public static final int RCS_PROFILE = 1;
        public static final int RCS_VERSION = 0;
    }

    public int setConfig(int i, String str) {
        return 0;
    }

    public ImsConfigImpl(int i, Context context) {
        this.mContext = context;
        this.mPhoneId = i;
    }

    public int setConfig(int i, int i2) {
        if (i != 66) {
            return 0;
        }
        ImsRegistry.setRttMode(this.mPhoneId, i2 == 1 ? Extensions.TelecomManager.RTT_MODE : Extensions.TelecomManager.RTT_MODE_OFF);
        return 0;
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0051, code lost:
        r2 = r0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int getConfigInt(int r6) {
        /*
            r5 = this;
            android.content.Context r0 = r5.mContext
            r1 = -1
            int r2 = android.os.Binder.getCallingUid()
            java.lang.String r3 = "com.sec.imsservice.READ_IMS_PERMISSION"
            int r0 = r0.checkPermission(r3, r1, r2)
            java.lang.String r1 = "getConfigInt: item: "
            r2 = 0
            if (r0 == 0) goto L_0x002e
            java.lang.String r0 = LOG_TAG
            int r5 = r5.mPhoneId
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            r3.append(r1)
            r3.append(r6)
            java.lang.String r6 = " there is no read_ims_permission"
            r3.append(r6)
            java.lang.String r6 = r3.toString()
            com.sec.internal.log.IMSLog.i(r0, r5, r6)
            return r2
        L_0x002e:
            long r3 = android.os.Binder.clearCallingIdentity()
            r0 = 10
            if (r6 == r0) goto L_0x00be
            r0 = 11
            if (r6 == r0) goto L_0x00b7
            switch(r6) {
                case 15: goto L_0x00ac;
                case 16: goto L_0x00a1;
                case 17: goto L_0x0096;
                case 18: goto L_0x008b;
                case 19: goto L_0x0080;
                case 20: goto L_0x0075;
                case 21: goto L_0x006a;
                case 22: goto L_0x005f;
                case 23: goto L_0x0054;
                case 24: goto L_0x0047;
                case 25: goto L_0x003f;
                default: goto L_0x003d;
            }
        L_0x003d:
            goto L_0x00c4
        L_0x003f:
            java.lang.String r0 = "31"
            int r2 = r5.getConfigValue(r0)     // Catch:{ all -> 0x00e6 }
            goto L_0x00c4
        L_0x0047:
            com.sec.internal.interfaces.ims.servicemodules.presence.IPresenceModule r0 = r5.mPresenceModule     // Catch:{ all -> 0x00e6 }
            if (r0 == 0) goto L_0x00c4
            int r2 = r5.mPhoneId     // Catch:{ all -> 0x00e6 }
            int r0 = r0.isListSubGzipEnabled(r2)     // Catch:{ all -> 0x00e6 }
        L_0x0051:
            r2 = r0
            goto L_0x00c4
        L_0x0054:
            com.sec.internal.interfaces.ims.servicemodules.presence.IPresenceModule r0 = r5.mPresenceModule     // Catch:{ all -> 0x00e6 }
            if (r0 == 0) goto L_0x00c4
            int r2 = r5.mPhoneId     // Catch:{ all -> 0x00e6 }
            int r0 = r0.getListSubExpiry(r2)     // Catch:{ all -> 0x00e6 }
            goto L_0x0051
        L_0x005f:
            com.sec.internal.interfaces.ims.servicemodules.presence.IPresenceModule r0 = r5.mPresenceModule     // Catch:{ all -> 0x00e6 }
            if (r0 == 0) goto L_0x00c4
            int r2 = r5.mPhoneId     // Catch:{ all -> 0x00e6 }
            int r0 = r0.getListSubMaxUri(r2)     // Catch:{ all -> 0x00e6 }
            goto L_0x0051
        L_0x006a:
            com.sec.internal.interfaces.ims.servicemodules.presence.IPresenceModule r0 = r5.mPresenceModule     // Catch:{ all -> 0x00e6 }
            if (r0 == 0) goto L_0x00c4
            int r2 = r5.mPhoneId     // Catch:{ all -> 0x00e6 }
            int r0 = r0.getPublishSourceThrottle(r2)     // Catch:{ all -> 0x00e6 }
            goto L_0x0051
        L_0x0075:
            com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityDiscoveryModule r0 = r5.mCapabilityDisModule     // Catch:{ all -> 0x00e6 }
            if (r0 == 0) goto L_0x00c4
            int r2 = r5.mPhoneId     // Catch:{ all -> 0x00e6 }
            int r0 = r0.getCapPollInterval(r2)     // Catch:{ all -> 0x00e6 }
            goto L_0x0051
        L_0x0080:
            com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityDiscoveryModule r0 = r5.mCapabilityDisModule     // Catch:{ all -> 0x00e6 }
            if (r0 == 0) goto L_0x00c4
            int r2 = r5.mPhoneId     // Catch:{ all -> 0x00e6 }
            int r0 = r0.getServiceAvailabilityInfoExpiry(r2)     // Catch:{ all -> 0x00e6 }
            goto L_0x0051
        L_0x008b:
            com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityDiscoveryModule r0 = r5.mCapabilityDisModule     // Catch:{ all -> 0x00e6 }
            if (r0 == 0) goto L_0x00c4
            int r2 = r5.mPhoneId     // Catch:{ all -> 0x00e6 }
            int r0 = r0.getCapInfoExpiry(r2)     // Catch:{ all -> 0x00e6 }
            goto L_0x0051
        L_0x0096:
            com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityDiscoveryModule r0 = r5.mCapabilityDisModule     // Catch:{ all -> 0x00e6 }
            if (r0 == 0) goto L_0x00c4
            int r2 = r5.mPhoneId     // Catch:{ all -> 0x00e6 }
            int r0 = r0.isCapDiscEnabled(r2)     // Catch:{ all -> 0x00e6 }
            goto L_0x0051
        L_0x00a1:
            com.sec.internal.interfaces.ims.servicemodules.presence.IPresenceModule r0 = r5.mPresenceModule     // Catch:{ all -> 0x00e6 }
            if (r0 == 0) goto L_0x00c4
            int r2 = r5.mPhoneId     // Catch:{ all -> 0x00e6 }
            int r0 = r0.getPublishExpiry(r2)     // Catch:{ all -> 0x00e6 }
            goto L_0x0051
        L_0x00ac:
            com.sec.internal.interfaces.ims.servicemodules.presence.IPresenceModule r0 = r5.mPresenceModule     // Catch:{ all -> 0x00e6 }
            if (r0 == 0) goto L_0x00c4
            int r2 = r5.mPhoneId     // Catch:{ all -> 0x00e6 }
            int r0 = r0.getPublishTimer(r2)     // Catch:{ all -> 0x00e6 }
            goto L_0x0051
        L_0x00b7:
            java.lang.String r0 = "94"
            int r2 = r5.getConfigValue(r0)     // Catch:{ all -> 0x00e6 }
            goto L_0x00c4
        L_0x00be:
            java.lang.String r0 = "93"
            int r2 = r5.getConfigValue(r0)     // Catch:{ all -> 0x00e6 }
        L_0x00c4:
            android.os.Binder.restoreCallingIdentity(r3)
            java.lang.String r0 = LOG_TAG
            int r5 = r5.mPhoneId
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            r3.append(r1)
            r3.append(r6)
            java.lang.String r6 = " value: "
            r3.append(r6)
            r3.append(r2)
            java.lang.String r6 = r3.toString()
            com.sec.internal.log.IMSLog.i(r0, r5, r6)
            return r2
        L_0x00e6:
            r5 = move-exception
            android.os.Binder.restoreCallingIdentity(r3)
            throw r5
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.imsphone.ImsConfigImpl.getConfigInt(int):int");
    }

    private int getConfigValue(String str) {
        ContentValues configValues = ImsRegistry.getConfigValues(new String[]{str}, this.mPhoneId);
        Integer asInteger = configValues != null ? configValues.getAsInteger(str) : null;
        if (asInteger != null) {
            return asInteger.intValue();
        }
        return 0;
    }

    public String getRcsClientConfiguration(int i) {
        String str;
        if (i == 0) {
            str = this.mRcsVersion;
        } else if (i == 1) {
            str = this.mRcsProfile;
        } else if (i == 2) {
            str = this.mClientVendor;
        } else if (i != 3) {
            str = i != 4 ? null : this.mRcsEnabledByUser;
        } else {
            str = this.mClientVersion;
        }
        String str2 = LOG_TAG;
        int i2 = this.mPhoneId;
        IMSLog.i(str2, i2, "getRcsClientConfiguration: item: " + i + " value: " + str);
        return str;
    }

    public void setRcsClientConfiguration(RcsClientConfiguration rcsClientConfiguration) {
        if (this.mContext.checkPermission("com.sec.imsservice.READ_IMS_PERMISSION", -1, Binder.getCallingUid()) != 0) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "setRcsClientConfiguration: there is no read_ims_permission");
            return;
        }
        long clearCallingIdentity = Binder.clearCallingIdentity();
        try {
            this.mRcsVersion = rcsClientConfiguration.getRcsVersion();
            this.mRcsProfile = rcsClientConfiguration.getRcsProfile();
            this.mClientVendor = rcsClientConfiguration.getClientVendor();
            this.mClientVersion = rcsClientConfiguration.getClientVersion();
            this.mRcsEnabledByUser = rcsClientConfiguration.isRcsEnabledByUser() ? "1" : "0";
            String str = LOG_TAG;
            int i = this.mPhoneId;
            IMSLog.i(str, i, "setRcsClientConfiguration: rcsVersion: " + this.mRcsVersion + " rcsProfile: " + this.mRcsProfile + " clientVendor: " + this.mClientVendor + " clientVersion: " + this.mClientVersion + " rcsEnabledByUser: " + this.mRcsEnabledByUser);
            ImsRegistry.getConfigModule().setRcsClientConfiguration(this.mPhoneId, this.mRcsVersion, this.mRcsProfile, this.mClientVendor, this.mClientVersion, this.mRcsEnabledByUser);
        } catch (NullPointerException e) {
            String str2 = LOG_TAG;
            int i2 = this.mPhoneId;
            IMSLog.i(str2, i2, "setRcsClientConfiguration: failed: " + e.getMessage());
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(clearCallingIdentity);
            throw th;
        }
        Binder.restoreCallingIdentity(clearCallingIdentity);
    }

    public void triggerAutoConfiguration() {
        if (this.mContext.checkPermission("com.sec.imsservice.READ_IMS_PERMISSION", -1, Binder.getCallingUid()) != 0) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "triggerAutoConfiguration: there is no read_ims_permission");
            return;
        }
        long clearCallingIdentity = Binder.clearCallingIdentity();
        try {
            ImsRegistry.getConfigModule().triggerAutoConfiguration(this.mPhoneId);
        } catch (NullPointerException e) {
            String str = LOG_TAG;
            int i = this.mPhoneId;
            IMSLog.i(str, i, "triggerAutoConfiguration: failed: " + e.getMessage());
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(clearCallingIdentity);
            throw th;
        }
        Binder.restoreCallingIdentity(clearCallingIdentity);
    }
}
