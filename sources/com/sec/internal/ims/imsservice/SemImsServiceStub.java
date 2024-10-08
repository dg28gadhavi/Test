package com.sec.internal.ims.imsservice;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Binder;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.text.TextUtils;
import android.util.Log;
import com.samsung.android.ims.ISemEpdgListener;
import com.samsung.android.ims.SemAutoConfigListener;
import com.samsung.android.ims.SemImsDmConfigListener;
import com.samsung.android.ims.SemImsRegiListener;
import com.samsung.android.ims.SemImsRegistration;
import com.samsung.android.ims.SemImsRegistrationError;
import com.samsung.android.ims.SemImsService;
import com.samsung.android.ims.SemSimMobStatusListener;
import com.samsung.android.ims.cmc.ISemCmcRecordingListener;
import com.samsung.android.ims.cmc.SemCmcRecordingInfo;
import com.samsung.android.ims.ft.SemImsFtListener;
import com.samsung.android.ims.settings.SemImsProfile;
import com.sec.ims.IAutoConfigurationListener;
import com.sec.ims.IEpdgListener;
import com.sec.ims.IImsDmConfigListener;
import com.sec.ims.IImsRegistrationListener;
import com.sec.ims.ISimMobilityStatusListener;
import com.sec.ims.ImsRegistration;
import com.sec.ims.ImsRegistrationError;
import com.sec.ims.ft.IImsOngoingFtEventListener;
import com.sec.ims.settings.ImsProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

public class SemImsServiceStub extends SemImsService.Stub {
    public static final Uri AUTO_CONFIGURATION_VERS_URI = Uri.parse(ConfigConstants.CONFIG_URI);
    private static final String IMS_SEAPI_SERVICE = "ImsBase";
    private static final String LOG_TAG = SemImsServiceStub.class.getSimpleName();
    private static final String PERMISSION = "com.sec.imsservice.PERMISSION";
    public static final String RCS_AUTOCONFIG_URI = "com.samsung.rcs.autoconfigurationprovider";
    private static SemImsServiceStub sInstance = null;
    /* access modifiers changed from: private */
    public Map<String, IBinder> mCallbacks = new ConcurrentHashMap();
    private Context mContext;
    private final HandlerThread mCoreThread;
    private ImsDmConfigCallBack mDmConfigCallbacks;
    /* access modifiers changed from: private */
    public RemoteCallbackList<SemImsDmConfigListener> mDmConfigListeners = new RemoteCallbackList<>();
    /* access modifiers changed from: private */
    public boolean[] mEpdgAvailable = new boolean[SimUtil.getPhoneCount()];
    private EpdgListenerCallback mEpdgHandoverCallback;
    /* access modifiers changed from: private */
    public Map<String, SemEpdgCallBack> mEpdgListeners = new ConcurrentHashMap();
    private ImsOngoingFtEventCallBack mOngoingFtEventCallback;
    /* access modifiers changed from: private */
    public Map<String, SemImsFtCallBack> mOngoingFtEventListeners = new ConcurrentHashMap();
    private int mRcsConfigVers = 0;

    public boolean isCmcPotentialEmergencyNumber(String str) {
        return false;
    }

    /* JADX WARNING: type inference failed for: r1v0, types: [com.sec.internal.ims.imsservice.SemImsServiceStub, java.lang.Object, android.os.IBinder] */
    private SemImsServiceStub(Context context) {
        this.mContext = context;
        this.mCoreThread = new HandlerThread(getClass().getSimpleName());
        ServiceManager.addService(IMS_SEAPI_SERVICE, this);
        Log.d(LOG_TAG, "SemImsServiceStub added");
    }

    public static synchronized SemImsServiceStub makeSemImsService(Context context) {
        synchronized (SemImsServiceStub.class) {
            if (sInstance != null) {
                Log.d(LOG_TAG, "Already created.");
                SemImsServiceStub semImsServiceStub = sInstance;
                return semImsServiceStub;
            }
            String str = LOG_TAG;
            Log.d(str, "Creating SemImsService");
            SemImsServiceStub semImsServiceStub2 = new SemImsServiceStub(context);
            sInstance = semImsServiceStub2;
            semImsServiceStub2.init();
            Log.d(str, "Done.");
            SemImsServiceStub semImsServiceStub3 = sInstance;
            return semImsServiceStub3;
        }
    }

    private void init() {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        this.mCoreThread.start();
    }

    public static SemImsServiceStub getInstance() {
        Log.i(LOG_TAG, "trying to get valid instance...");
        while (getInstanceInternal() == null) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Log.i(LOG_TAG, "returning valid instance...");
        return getInstanceInternal();
    }

    private static synchronized SemImsServiceStub getInstanceInternal() {
        SemImsServiceStub semImsServiceStub;
        synchronized (SemImsServiceStub.class) {
            semImsServiceStub = sInstance;
        }
        return semImsServiceStub;
    }

    public Binder getBinder() {
        return ImsServiceStub.getInstance().getSemBinder();
    }

    public boolean isSimMobilityActivated(int i) {
        return ImsServiceStub.getInstance().isSimMobilityActivated(i);
    }

    public boolean isServiceAvailable(String str, int i, int i2) throws RemoteException {
        return ImsServiceStub.getInstance().isServiceAvailable(str, i, i2);
    }

    public String getRcsProfileType(int i) throws RemoteException {
        return ImsServiceStub.getInstance().getRcsProfileType(i);
    }

    public ContentValues getConfigValues(String[] strArr, int i) {
        return ImsServiceStub.getInstance().getConfigValues(strArr, i);
    }

    public boolean isForbiddenByPhoneId(int i) {
        return ImsServiceStub.getInstance().isForbiddenByPhoneId(i);
    }

    public SemImsRegistration[] getRegistrationInfoByPhoneId(int i) {
        ArrayList arrayList = new ArrayList();
        ImsRegistration[] registrationInfoByPhoneId = ImsServiceStub.getInstance().getRegistrationInfoByPhoneId(i);
        if (registrationInfoByPhoneId != null) {
            for (ImsRegistration imsRegistration : registrationInfoByPhoneId) {
                if (imsRegistration.getPhoneId() == i) {
                    arrayList.add(buildSemImsRegistration(imsRegistration));
                }
            }
        }
        return (SemImsRegistration[]) arrayList.toArray(new SemImsRegistration[0]);
    }

    public SemImsProfile[] getCurrentProfileForSlot(int i) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        ArrayList arrayList = new ArrayList();
        ImsProfile[] currentProfileForSlot = ImsServiceStub.getInstance().getCurrentProfileForSlot(i);
        if (currentProfileForSlot != null) {
            for (ImsProfile buildSemImsProfile : currentProfileForSlot) {
                arrayList.add(buildSemImsProfile(buildSemImsProfile));
            }
        }
        return (SemImsProfile[]) arrayList.toArray(new SemImsProfile[0]);
    }

    public SemImsRegistration getRegistrationInfoByServiceType(String str, int i) throws RemoteException {
        String str2 = LOG_TAG;
        IMSLog.d(str2, i, "getRegistrationInfoByServiceType: phoneId " + i + " serviceType " + str);
        return buildSemImsRegistration(ImsServiceStub.getInstance().getRegistrationInfoByServiceType(str, i));
    }

    public String registerImsRegistrationListenerForSlot(SemImsRegiListener semImsRegiListener, int i) throws RemoteException {
        IMSLog.i(LOG_TAG, i, "SemRegisterImsRegistrationListener " + semImsRegiListener);
        if (semImsRegiListener == null) {
            return null;
        }
        ImsRegistrationCallBack imsRegistrationCallBack = new ImsRegistrationCallBack(semImsRegiListener, i);
        String registerImsRegistrationListener = ImsServiceStub.getInstance().registerImsRegistrationListener(imsRegistrationCallBack, false, i);
        if (!TextUtils.isEmpty(registerImsRegistrationListener)) {
            imsRegistrationCallBack.mToken = registerImsRegistrationListener;
            this.mCallbacks.put(registerImsRegistrationListener, imsRegistrationCallBack);
        } else {
            imsRegistrationCallBack.reset();
        }
        ImsRegistration[] registrationInfoByPhoneId = ImsServiceStub.getInstance().getRegistrationInfoByPhoneId(i);
        if (registrationInfoByPhoneId != null) {
            for (ImsRegistration imsRegistration : registrationInfoByPhoneId) {
                if (imsRegistration.hasVolteService() && !imsRegistration.getImsProfile().hasEmergencySupport()) {
                    try {
                        semImsRegiListener.onRegistered(buildSemImsRegistration(imsRegistration));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return registerImsRegistrationListener;
    }

    public void unregisterImsRegistrationListenerForSlot(String str, int i) {
        String str2 = LOG_TAG;
        IMSLog.d(str2, i, "SemUnregisterImsRegistrationListener");
        if (TextUtils.isEmpty(str)) {
            IMSLog.d(str2, i, "unregisterImsRegistrationListenerForSlot: token is empty.");
            return;
        }
        ImsServiceStub.getInstance().unregisterImsRegistrationListenerForSlot(str, i);
        ImsRegistrationCallBack imsRegistrationCallBack = (ImsRegistrationCallBack) this.mCallbacks.remove(str);
        if (imsRegistrationCallBack != null) {
            imsRegistrationCallBack.reset();
        }
    }

    public synchronized String registerImsOngoingFtEventListener(SemImsFtListener semImsFtListener) throws RemoteException {
        IMSLog.d(LOG_TAG, "SemRegisterImsOngoingFtListener");
        if (semImsFtListener == null) {
            return null;
        }
        if (this.mOngoingFtEventCallback == null) {
            ImsOngoingFtEventCallBack imsOngoingFtEventCallBack = new ImsOngoingFtEventCallBack();
            this.mOngoingFtEventCallback = imsOngoingFtEventCallBack;
            imsOngoingFtEventCallBack.mToken = ImsServiceStub.getInstance().registerImsOngoingFtListener(this.mOngoingFtEventCallback);
        }
        String tokenOfListener = ImsServiceStub.getTokenOfListener(semImsFtListener);
        this.mOngoingFtEventListeners.put(tokenOfListener, new SemImsFtCallBack(semImsFtListener, tokenOfListener));
        return tokenOfListener;
    }

    public void unregisterImsOngoingFtEventListener(String str) {
        String str2 = LOG_TAG;
        IMSLog.d(str2, "SemUnregisterImsOngoingFtListener");
        if (this.mOngoingFtEventCallback == null || TextUtils.isEmpty(str)) {
            IMSLog.d(str2, "unregisterImsRegistrationListenerForSlot: token is empty or mOngoingFtEventCallback is null.");
            return;
        }
        SemImsFtCallBack remove = this.mOngoingFtEventListeners.remove(str);
        if (remove != null) {
            remove.reset();
        }
        if (this.mOngoingFtEventListeners.size() <= 0) {
            ImsServiceStub.getInstance().unregisterImsOngoingFtListener(this.mOngoingFtEventCallback.mToken);
            this.mOngoingFtEventCallback = null;
        }
    }

    public String registerSimMobilityStatusListener(SemSimMobStatusListener semSimMobStatusListener, int i) throws RemoteException {
        IMSLog.d(LOG_TAG, i, "SemRegisterSimMobilityStatusListener");
        if (semSimMobStatusListener == null) {
            return null;
        }
        SimMobilityStatusCallBack simMobilityStatusCallBack = new SimMobilityStatusCallBack(semSimMobStatusListener, i);
        String registerSimMobilityStatusListener = ImsServiceStub.getInstance().registerSimMobilityStatusListener(simMobilityStatusCallBack, true, i);
        if (!TextUtils.isEmpty(registerSimMobilityStatusListener)) {
            simMobilityStatusCallBack.mToken = registerSimMobilityStatusListener;
            this.mCallbacks.put(registerSimMobilityStatusListener, simMobilityStatusCallBack);
        } else {
            simMobilityStatusCallBack.reset();
        }
        return registerSimMobilityStatusListener;
    }

    public void unregisterSimMobilityStatusListener(String str, int i) {
        String str2 = LOG_TAG;
        IMSLog.d(str2, i, "SemUnregisterSimMobilityStatusListener");
        if (TextUtils.isEmpty(str)) {
            IMSLog.d(str2, i, "unregisterImsRegistrationListenerForSlot: token is empty.");
            return;
        }
        ImsServiceStub.getInstance().unregisterSimMobilityStatusListenerByPhoneId(str, i);
        SimMobilityStatusCallBack simMobilityStatusCallBack = (SimMobilityStatusCallBack) this.mCallbacks.remove(str);
        if (simMobilityStatusCallBack != null) {
            simMobilityStatusCallBack.reset();
        }
    }

    public String registerAutoConfigurationListener(SemAutoConfigListener semAutoConfigListener, int i) {
        IMSLog.d(LOG_TAG, i, "registerAutoConfigurationListener");
        if (semAutoConfigListener == null) {
            return null;
        }
        AutoConfigCallBack autoConfigCallBack = new AutoConfigCallBack(semAutoConfigListener, i);
        String registerAutoConfigurationListener = ImsServiceStub.getInstance().registerAutoConfigurationListener(autoConfigCallBack, i);
        if (!TextUtils.isEmpty(registerAutoConfigurationListener)) {
            autoConfigCallBack.mToken = registerAutoConfigurationListener;
            this.mCallbacks.put(registerAutoConfigurationListener, autoConfigCallBack);
        } else {
            autoConfigCallBack.reset();
        }
        return registerAutoConfigurationListener;
    }

    public void unregisterAutoConfigurationListener(String str, int i) {
        String str2 = LOG_TAG;
        IMSLog.d(str2, i, "unregisterAutoConfigurationListener");
        if (TextUtils.isEmpty(str)) {
            IMSLog.d(str2, i, "unregisterAutoConfigurationListener: token is empty.");
            return;
        }
        AutoConfigCallBack autoConfigCallBack = (AutoConfigCallBack) this.mCallbacks.remove(str);
        if (autoConfigCallBack != null) {
            autoConfigCallBack.reset();
            ImsServiceStub.getInstance().unregisterAutoConfigurationListener(str, i);
        }
    }

    /* JADX WARNING: Can't wrap try/catch for region: R(6:10|(8:11|12|(1:14)(1:15)|16|17|(1:19)(1:20)|21|22)|28|29|30|(2:34|37)(1:39)) */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x008a, code lost:
        r10 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00b6, code lost:
        android.os.Binder.restoreCallingIdentity(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x00b9, code lost:
        throw r10;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Missing exception handler attribute for start block: B:28:0x008e */
    /* JADX WARNING: Removed duplicated region for block: B:10:0x0037  */
    /* JADX WARNING: Removed duplicated region for block: B:9:0x0036 A[RETURN] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isRcsEnabled(boolean r11, int r12) {
        /*
            r10 = this;
            java.lang.String r0 = "]"
            java.lang.String r1 = "["
            r2 = 1
            r3 = 0
            android.content.Context r4 = r10.mContext     // Catch:{ SettingNotFoundException -> 0x0017 }
            android.content.ContentResolver r4 = r4.getContentResolver()     // Catch:{ SettingNotFoundException -> 0x0017 }
            java.lang.String r5 = "rcs_user_setting"
            int r4 = android.provider.Settings.System.getInt(r4, r5)     // Catch:{ SettingNotFoundException -> 0x0017 }
            if (r4 != r2) goto L_0x0033
            r4 = r2
            goto L_0x0034
        L_0x0017:
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r5 = LOG_TAG
            r4.append(r5)
            r4.append(r1)
            r4.append(r12)
            r4.append(r0)
            java.lang.String r4 = r4.toString()
            java.lang.String r5 = "isRcsEnabled: rcs_user_setting is not exist."
            android.util.Log.d(r4, r5)
        L_0x0033:
            r4 = r3
        L_0x0034:
            if (r11 != 0) goto L_0x0037
            return r4
        L_0x0037:
            long r5 = android.os.Binder.clearCallingIdentity()
            boolean r11 = r10.getRcsAutoconfigVers(r12)     // Catch:{ IllegalStateException -> 0x008c }
            if (r11 == 0) goto L_0x0044
            int r11 = r10.mRcsConfigVers     // Catch:{ IllegalStateException -> 0x008c }
            goto L_0x0045
        L_0x0044:
            r11 = r3
        L_0x0045:
            java.lang.String r10 = r10.getRcsAutoConfigCompl(r12)     // Catch:{ IllegalStateException -> 0x0088 }
            if (r10 == 0) goto L_0x0053
            java.lang.String r7 = "true"
            boolean r10 = r7.equals(r10)     // Catch:{ IllegalStateException -> 0x0088 }
            goto L_0x0054
        L_0x0053:
            r10 = r3
        L_0x0054:
            java.lang.StringBuilder r7 = new java.lang.StringBuilder     // Catch:{ IllegalStateException -> 0x008e }
            r7.<init>()     // Catch:{ IllegalStateException -> 0x008e }
            java.lang.String r8 = LOG_TAG     // Catch:{ IllegalStateException -> 0x008e }
            r7.append(r8)     // Catch:{ IllegalStateException -> 0x008e }
            r7.append(r1)     // Catch:{ IllegalStateException -> 0x008e }
            r7.append(r12)     // Catch:{ IllegalStateException -> 0x008e }
            r7.append(r0)     // Catch:{ IllegalStateException -> 0x008e }
            java.lang.String r7 = r7.toString()     // Catch:{ IllegalStateException -> 0x008e }
            java.lang.StringBuilder r8 = new java.lang.StringBuilder     // Catch:{ IllegalStateException -> 0x008e }
            r8.<init>()     // Catch:{ IllegalStateException -> 0x008e }
            java.lang.String r9 = "isRcsEnabled: version "
            r8.append(r9)     // Catch:{ IllegalStateException -> 0x008e }
            r8.append(r11)     // Catch:{ IllegalStateException -> 0x008e }
            java.lang.String r9 = " autoConfigComplete "
            r8.append(r9)     // Catch:{ IllegalStateException -> 0x008e }
            r8.append(r10)     // Catch:{ IllegalStateException -> 0x008e }
            java.lang.String r8 = r8.toString()     // Catch:{ IllegalStateException -> 0x008e }
            android.util.Log.d(r7, r8)     // Catch:{ IllegalStateException -> 0x008e }
            goto L_0x00aa
        L_0x0088:
            r10 = r3
            goto L_0x008e
        L_0x008a:
            r10 = move-exception
            goto L_0x00b6
        L_0x008c:
            r10 = r3
            r11 = r10
        L_0x008e:
            java.lang.StringBuilder r7 = new java.lang.StringBuilder     // Catch:{ all -> 0x008a }
            r7.<init>()     // Catch:{ all -> 0x008a }
            java.lang.String r8 = LOG_TAG     // Catch:{ all -> 0x008a }
            r7.append(r8)     // Catch:{ all -> 0x008a }
            r7.append(r1)     // Catch:{ all -> 0x008a }
            r7.append(r12)     // Catch:{ all -> 0x008a }
            r7.append(r0)     // Catch:{ all -> 0x008a }
            java.lang.String r12 = r7.toString()     // Catch:{ all -> 0x008a }
            java.lang.String r0 = "isRcsEnabled: AutoConfiguration is not completed."
            android.util.Log.d(r12, r0)     // Catch:{ all -> 0x008a }
        L_0x00aa:
            android.os.Binder.restoreCallingIdentity(r5)
            if (r4 == 0) goto L_0x00b4
            if (r10 == 0) goto L_0x00b5
            if (r11 <= 0) goto L_0x00b4
            goto L_0x00b5
        L_0x00b4:
            r2 = r3
        L_0x00b5:
            return r2
        L_0x00b6:
            android.os.Binder.restoreCallingIdentity(r5)
            throw r10
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.imsservice.SemImsServiceStub.isRcsEnabled(boolean, int):boolean");
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r8v6, resolved type: java.lang.String} */
    /* JADX WARNING: type inference failed for: r8v4 */
    /* JADX WARNING: type inference failed for: r8v5, types: [android.database.Cursor] */
    /* JADX WARNING: type inference failed for: r8v8 */
    /* JADX WARNING: type inference failed for: r8v11 */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x0070  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private java.lang.String getRcsAutoConfigCompl(int r8) {
        /*
            r7 = this;
            android.net.Uri r0 = AUTO_CONFIGURATION_VERS_URI
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            r1.append(r0)
            java.lang.String r0 = "#simslot\\d"
            java.lang.String r2 = ""
            java.lang.String r3 = "info/completed"
            java.lang.String r0 = r3.replaceAll(r0, r2)
            r1.append(r0)
            java.lang.String r0 = r1.toString()
            android.net.Uri r0 = android.net.Uri.parse(r0)
            android.net.Uri$Builder r0 = r0.buildUpon()
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "simslot"
            r1.append(r2)
            java.lang.String r8 = java.lang.Integer.toString(r8)
            r1.append(r8)
            java.lang.String r8 = r1.toString()
            android.net.Uri$Builder r8 = r0.fragment(r8)
            android.net.Uri r1 = r8.build()
            r8 = 0
            android.content.Context r7 = r7.mContext     // Catch:{ all -> 0x006d }
            if (r7 == 0) goto L_0x0053
            android.content.ContentResolver r0 = r7.getContentResolver()     // Catch:{ all -> 0x006d }
            r2 = 0
            r3 = 0
            r4 = 0
            r5 = 0
            android.database.Cursor r7 = r0.query(r1, r2, r3, r4, r5)     // Catch:{ all -> 0x006d }
            goto L_0x0054
        L_0x0053:
            r7 = r8
        L_0x0054:
            if (r7 == 0) goto L_0x0067
            boolean r0 = r7.moveToFirst()     // Catch:{ all -> 0x0062 }
            if (r0 == 0) goto L_0x0067
            r8 = 0
            java.lang.String r8 = r7.getString(r8)     // Catch:{ all -> 0x0062 }
            goto L_0x0067
        L_0x0062:
            r8 = move-exception
            r6 = r8
            r8 = r7
            r7 = r6
            goto L_0x006e
        L_0x0067:
            if (r7 == 0) goto L_0x006c
            r7.close()
        L_0x006c:
            return r8
        L_0x006d:
            r7 = move-exception
        L_0x006e:
            if (r8 == 0) goto L_0x0073
            r8.close()
        L_0x0073:
            throw r7
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.imsservice.SemImsServiceStub.getRcsAutoConfigCompl(int):java.lang.String");
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v6, resolved type: java.lang.String} */
    /* JADX WARNING: type inference failed for: r7v4 */
    /* JADX WARNING: type inference failed for: r7v5, types: [android.database.Cursor] */
    /* JADX WARNING: type inference failed for: r7v9 */
    /* JADX WARNING: type inference failed for: r7v11 */
    /* JADX WARNING: Can't wrap try/catch for region: R(4:19|20|(1:22)|23) */
    /* JADX WARNING: Code restructure failed: missing block: B:20:?, code lost:
        android.util.Log.e(LOG_TAG, "Error while parsing integer in getIntValue() - NumberFormatException");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x007b, code lost:
        if (r0 != null) goto L_0x007d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x007d, code lost:
        r0.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0080, code lost:
        return false;
     */
    /* JADX WARNING: Missing exception handler attribute for start block: B:19:0x0074 */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x008a  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean getRcsAutoconfigVers(int r7) {
        /*
            r6 = this;
            android.net.Uri r0 = AUTO_CONFIGURATION_VERS_URI
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            r1.append(r0)
            java.lang.String r0 = "#simslot\\d"
            java.lang.String r2 = ""
            java.lang.String r3 = "parameter/version"
            java.lang.String r0 = r3.replaceAll(r0, r2)
            r1.append(r0)
            java.lang.String r0 = r1.toString()
            android.net.Uri r0 = android.net.Uri.parse(r0)
            android.net.Uri$Builder r0 = r0.buildUpon()
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "simslot"
            r1.append(r2)
            java.lang.String r7 = java.lang.Integer.toString(r7)
            r1.append(r7)
            java.lang.String r7 = r1.toString()
            android.net.Uri$Builder r7 = r0.fragment(r7)
            android.net.Uri r1 = r7.build()
            r7 = 0
            android.content.Context r0 = r6.mContext     // Catch:{ all -> 0x0087 }
            if (r0 == 0) goto L_0x0053
            android.content.ContentResolver r0 = r0.getContentResolver()     // Catch:{ all -> 0x0087 }
            r2 = 0
            r3 = 0
            r4 = 0
            r5 = 0
            android.database.Cursor r0 = r0.query(r1, r2, r3, r4, r5)     // Catch:{ all -> 0x0087 }
            goto L_0x0054
        L_0x0053:
            r0 = r7
        L_0x0054:
            r1 = 0
            if (r0 == 0) goto L_0x0065
            boolean r2 = r0.moveToFirst()     // Catch:{ all -> 0x0062 }
            if (r2 == 0) goto L_0x0065
            java.lang.String r7 = r0.getString(r1)     // Catch:{ all -> 0x0062 }
            goto L_0x0065
        L_0x0062:
            r6 = move-exception
            r7 = r0
            goto L_0x0088
        L_0x0065:
            if (r7 == 0) goto L_0x0081
            int r7 = java.lang.Integer.parseInt(r7)     // Catch:{ NumberFormatException -> 0x0074 }
            r6.mRcsConfigVers = r7     // Catch:{ NumberFormatException -> 0x0074 }
            if (r0 == 0) goto L_0x0072
            r0.close()
        L_0x0072:
            r6 = 1
            return r6
        L_0x0074:
            java.lang.String r6 = LOG_TAG     // Catch:{ all -> 0x0062 }
            java.lang.String r7 = "Error while parsing integer in getIntValue() - NumberFormatException"
            android.util.Log.e(r6, r7)     // Catch:{ all -> 0x0062 }
            if (r0 == 0) goto L_0x0080
            r0.close()
        L_0x0080:
            return r1
        L_0x0081:
            if (r0 == 0) goto L_0x0086
            r0.close()
        L_0x0086:
            return r1
        L_0x0087:
            r6 = move-exception
        L_0x0088:
            if (r7 == 0) goto L_0x008d
            r7.close()
        L_0x008d:
            throw r6
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.imsservice.SemImsServiceStub.getRcsAutoconfigVers(int):boolean");
    }

    public void enableRcsByPhoneId(boolean z, int i) {
        ImsServiceStub.getInstance().enableRcsByPhoneId(z, i);
    }

    public boolean isVoLteAvailable(int i) throws RemoteException {
        return ImsServiceStub.getInstance().hasVoLteSimByPhoneId(i);
    }

    public void sendTryRegisterByPhoneId(int i) {
        ImsServiceStub.getInstance().sendTryRegisterByPhoneId(i);
    }

    public boolean getBooleanConfig(String str, int i) {
        String str2 = LOG_TAG;
        IMSLog.d(str2, i, "getBooleanConfig : " + str);
        String[] strArr = new String[1];
        if (!"mmtel".equals(str)) {
            if ("mmtel-video".equals(str)) {
                strArr[0] = "94";
            }
            return false;
        } else if (SimUtil.getSimMno(i) == Mno.USCC) {
            strArr[0] = "81";
        } else {
            strArr[0] = "93";
        }
        ContentValues configValues = ImsServiceStub.getInstance().getConfigValues(strArr, i);
        if (configValues != null) {
            String str3 = (String) configValues.get(strArr[0]);
            if (!TextUtils.isEmpty(str3)) {
                if ("81".equals(strArr[0])) {
                    return DiagnosisConstants.RCSM_ORST_REGI.equals(str3);
                }
                return "1".equals(str3);
            }
        } else {
            IMSLog.d(str2, i, "can not read DM values");
        }
        return false;
    }

    public void setRttMode(int i, int i2) {
        ImsServiceStub.getInstance().setRttMode(i, i2);
    }

    public void sendVerificationCode(String str, int i) {
        IMSLog.d(LOG_TAG, i, "sendVerificationCode");
        ImsServiceStub.getInstance().sendVerificationCode(str, i);
    }

    public void sendMsisdnNumber(String str, int i) {
        IMSLog.d(LOG_TAG, i, "sendMsisdnNumber");
        ImsServiceStub.getInstance().sendMsisdnNumber(str, i);
    }

    public void sendIidToken(String str, int i) {
        IMSLog.d(LOG_TAG, i, "sendIidToken");
        ImsServiceStub.getInstance().sendIidToken(str, i);
    }

    public void registerDmValueListener(SemImsDmConfigListener semImsDmConfigListener) throws RemoteException {
        ImsDmConfigCallBack imsDmConfigCallBack = new ImsDmConfigCallBack();
        this.mDmConfigCallbacks = imsDmConfigCallBack;
        ImsServiceStub.getInstance().registerDmValueListener(imsDmConfigCallBack);
        if (semImsDmConfigListener != null) {
            Log.d(LOG_TAG, "mDmConfigListeners register");
            this.mDmConfigListeners.register(semImsDmConfigListener);
        }
    }

    public void unregisterDmValueListener(SemImsDmConfigListener semImsDmConfigListener) {
        if (semImsDmConfigListener != null) {
            this.mDmConfigListeners.unregister(semImsDmConfigListener);
        }
        if (this.mDmConfigCallbacks != null) {
            ImsServiceStub.getInstance().unregisterDmValueListener(this.mDmConfigCallbacks);
        }
    }

    public boolean isCmcEmergencyCallSupported() {
        IMSLog.d(LOG_TAG, "isCmcEmergencyCallSupported");
        if (ImsServiceStub.getInstance().getCmcAccountManager() == null) {
            return false;
        }
        return ImsServiceStub.getInstance().getCmcAccountManager().isEmergencyCallSupported();
    }

    public boolean isCmcEmergencyNumber(String str, int i) {
        IMSLog.d(LOG_TAG, "isCmcEmergencyNumber: ");
        if (ImsServiceStub.getInstance().getCmcAccountManager() == null) {
            return false;
        }
        return ImsServiceStub.getInstance().getCmcAccountManager().isEmergencyNumber(str, i);
    }

    public boolean isCmcPotentialEmergencyNumber(String str, int i) {
        IMSLog.d(LOG_TAG, "isCmcPotentialEmergencyNumber: ");
        if (ImsServiceStub.getInstance().getCmcAccountManager() == null) {
            return false;
        }
        return ImsServiceStub.getInstance().getCmcAccountManager().isPotentialEmergencyNumber(str, i);
    }

    public void sendSemCmcRecordingEvent(SemCmcRecordingInfo semCmcRecordingInfo, int i, int i2) {
        String str = LOG_TAG;
        IMSLog.d(str, i2, "sendSemCmcRecordingEvent : " + i);
        ImsServiceStub.getInstance().sendCmcRecordingEvent(i2, i, semCmcRecordingInfo);
    }

    public void registerSemCmcRecordingListener(ISemCmcRecordingListener iSemCmcRecordingListener, int i) {
        IMSLog.d(LOG_TAG, i, "registerSemCmcRecordingListener");
        ImsServiceStub.getInstance().registerCmcRecordingListener(i, iSemCmcRecordingListener);
    }

    public synchronized String registerEpdgListener(ISemEpdgListener iSemEpdgListener) {
        String tokenOfListener;
        if (iSemEpdgListener == null) {
            return null;
        }
        if (this.mEpdgHandoverCallback == null) {
            EpdgListenerCallback epdgListenerCallback = new EpdgListenerCallback();
            this.mEpdgHandoverCallback = epdgListenerCallback;
            epdgListenerCallback.mToken = ImsServiceStub.getInstance().registerEpdgListener(this.mEpdgHandoverCallback);
        }
        tokenOfListener = ImsServiceStub.getTokenOfListener(iSemEpdgListener);
        this.mEpdgListeners.put(tokenOfListener, new SemEpdgCallBack(iSemEpdgListener, tokenOfListener));
        int i = 0;
        while (i < SimUtil.getPhoneCount()) {
            try {
                int epdgPhysicalInterface = ImsServiceStub.getInstance().getPdnController().getEpdgPhysicalInterface(i);
                String str = LOG_TAG;
                Log.d(str, "register epdg listener on epdg available : " + this.mEpdgAvailable[i] + " epdgInterfaceState " + epdgPhysicalInterface);
                iSemEpdgListener.onEpdgAvailable(i, this.mEpdgAvailable[i], epdgPhysicalInterface);
                i++;
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return tokenOfListener;
    }

    public void unRegisterEpdgListener(String str) {
        if (this.mEpdgHandoverCallback != null && !TextUtils.isEmpty(str)) {
            SemEpdgCallBack remove = this.mEpdgListeners.remove(str);
            if (remove != null) {
                remove.reset();
            }
            if (this.mEpdgListeners.size() <= 0) {
                ImsServiceStub.getInstance().unRegisterEpdgListener(this.mEpdgHandoverCallback.mToken);
                this.mEpdgHandoverCallback = null;
            }
        }
    }

    public boolean isCrossSimCallingRegistered(int i) {
        return ImsServiceStub.getInstance().isCrossSimCallingRegistered(i);
    }

    public boolean hasCrossSimCallingSupport(int i) {
        return ImsServiceStub.getInstance().isCrossSimCallingSupportedByPhoneId(i);
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:6:0x009a, code lost:
        if (r0.contains(r4) != false) goto L_0x009e;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public com.samsung.android.ims.SemImsRegistration buildSemImsRegistration(com.sec.ims.ImsRegistration r4) {
        /*
            r3 = this;
            if (r4 == 0) goto L_0x00a7
            com.samsung.android.ims.SemImsRegistration$Builder r3 = com.samsung.android.ims.SemImsRegistration.getBuilder()
            int r0 = r4.getCurrentRat()
            r3.setRegiRat(r0)
            int r0 = r4.getNetworkType()
            r3.setPdnType(r0)
            int r0 = r4.getPhoneId()
            r3.setPhoneId(r0)
            java.util.Set r0 = r4.getServices()
            r3.setServices(r0)
            java.lang.String r0 = r4.getPAssociatedUri2nd()
            r3.setPAssociatedUri2nd(r0)
            boolean r0 = r4.getEpdgStatus()
            r3.setEpdgStatus(r0)
            boolean r0 = r4.isEpdgOverCellularData()
            r3.setEpdgOverCellularData(r0)
            int r0 = r4.getSubscriptionId()
            r3.setSubscriptionId(r0)
            com.sec.ims.util.ImsUri r0 = r4.getRegisteredImpu()
            java.util.Optional r0 = java.util.Optional.ofNullable(r0)
            com.sec.internal.ims.cmstore.syncschedulers.RcsScheduler$$ExternalSyntheticLambda0 r1 = new com.sec.internal.ims.cmstore.syncschedulers.RcsScheduler$$ExternalSyntheticLambda0
            r1.<init>()
            java.util.Optional r0 = r0.map(r1)
            java.lang.String r1 = ""
            java.lang.Object r0 = r0.orElse(r1)
            java.lang.String r0 = (java.lang.String) r0
            r3.setRegisteredPublicUserId(r0)
            com.sec.ims.util.NameAddr r0 = r4.getPreferredImpu()
            java.util.Optional r0 = java.util.Optional.ofNullable(r0)
            com.sec.internal.ims.core.RegistrationManager$$ExternalSyntheticLambda7 r2 = new com.sec.internal.ims.core.RegistrationManager$$ExternalSyntheticLambda7
            r2.<init>()
            java.util.Optional r0 = r0.map(r2)
            java.lang.Object r0 = r0.orElse(r1)
            java.lang.String r0 = (java.lang.String) r0
            r3.setPreferredPublicUserId(r0)
            java.lang.String r0 = r4.getOwnNumber()
            java.util.Optional r0 = java.util.Optional.ofNullable(r0)
            java.lang.Object r0 = r0.orElse(r1)
            java.lang.String r0 = (java.lang.String) r0
            int r4 = r4.getPhoneId()
            com.sec.internal.interfaces.ims.core.ISimManager r4 = com.sec.internal.ims.core.sim.SimManagerFactory.getSimManagerFromSimSlot(r4)
            if (r4 == 0) goto L_0x009d
            java.lang.String r4 = r4.getImsi()
            boolean r2 = android.text.TextUtils.isEmpty(r4)
            if (r2 != 0) goto L_0x009d
            boolean r4 = r0.contains(r4)
            if (r4 == 0) goto L_0x009d
            goto L_0x009e
        L_0x009d:
            r1 = r0
        L_0x009e:
            r3.setOwnNumber(r1)
            com.samsung.android.ims.SemImsRegistration r4 = new com.samsung.android.ims.SemImsRegistration
            r4.<init>(r3)
            return r4
        L_0x00a7:
            r3 = 0
            return r3
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.imsservice.SemImsServiceStub.buildSemImsRegistration(com.sec.ims.ImsRegistration):com.samsung.android.ims.SemImsRegistration");
    }

    private SemImsProfile buildSemImsProfile(ImsProfile imsProfile) {
        if (imsProfile != null) {
            return new SemImsProfile(imsProfile.toJson());
        }
        return null;
    }

    /* access modifiers changed from: private */
    public SemImsRegistrationError buildSemImsRegistrationError(ImsRegistrationError imsRegistrationError) {
        return new SemImsRegistrationError(imsRegistrationError.getSipErrorCode(), imsRegistrationError.getSipErrorReason(), imsRegistrationError.getDetailedDeregiReason(), imsRegistrationError.getDeregistrationReason());
    }

    private class ImsDmConfigCallBack extends IImsDmConfigListener.Stub {
        private ImsDmConfigCallBack() {
        }

        public void onChangeDmValue(String str, boolean z) {
            RemoteCallbackList r2 = SemImsServiceStub.this.mDmConfigListeners;
            if (r2 != null) {
                int beginBroadcast = r2.beginBroadcast();
                while (beginBroadcast > 0) {
                    beginBroadcast--;
                    try {
                        r2.getBroadcastItem(beginBroadcast).onChangeDmValue(str, z);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                r2.finishBroadcast();
            }
        }
    }

    private class AutoConfigCallBack extends IAutoConfigurationListener.Stub implements IBinder.DeathRecipient {
        SemAutoConfigListener mListener;
        private int mPhoneId;
        String mToken;

        public AutoConfigCallBack(SemAutoConfigListener semAutoConfigListener, int i) {
            this.mListener = semAutoConfigListener;
            this.mPhoneId = i;
            try {
                semAutoConfigListener.asBinder().linkToDeath(this, 0);
            } catch (RemoteException unused) {
            }
        }

        public void onVerificationCodeNeeded() {
            try {
                this.mListener.onVerificationCodeNeeded();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        public void onMsisdnNumberNeeded() {
            try {
                this.mListener.onMsisdnNumberNeeded();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        public void onIidTokenNeeded() {
            try {
                this.mListener.onIidTokenNeeded();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        public void onAutoConfigurationCompleted(boolean z) {
            try {
                this.mListener.onAutoConfigurationCompleted(z);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        public void binderDied() {
            ImsServiceStub.getInstance().unregisterAutoConfigurationListener(this.mToken, this.mPhoneId);
            SemImsServiceStub.this.mCallbacks.remove(this.mToken);
            reset();
        }

        /* access modifiers changed from: protected */
        public void reset() {
            try {
                this.mListener.asBinder().unlinkToDeath(this, 0);
            } catch (NoSuchElementException unused) {
            }
        }
    }

    private class ImsRegistrationCallBack extends IImsRegistrationListener.Stub implements IBinder.DeathRecipient {
        SemImsRegiListener mListener;
        private int mPhoneId;
        String mToken;

        public ImsRegistrationCallBack(SemImsRegiListener semImsRegiListener, int i) {
            this.mListener = semImsRegiListener;
            this.mPhoneId = i;
            try {
                semImsRegiListener.asBinder().linkToDeath(this, 0);
            } catch (RemoteException unused) {
            }
        }

        public void onRegistered(ImsRegistration imsRegistration) {
            if ((imsRegistration.hasVolteService() && !imsRegistration.getImsProfile().hasEmergencySupport()) || imsRegistration.hasRcsService()) {
                try {
                    this.mListener.onRegistered(SemImsServiceStub.this.buildSemImsRegistration(imsRegistration));
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        public void onDeregistered(ImsRegistration imsRegistration, ImsRegistrationError imsRegistrationError) {
            if ((imsRegistration.hasVolteService() && !imsRegistration.getImsProfile().hasEmergencySupport()) || imsRegistration.hasRcsService()) {
                try {
                    this.mListener.onDeregistered(SemImsServiceStub.this.buildSemImsRegistration(imsRegistration), SemImsServiceStub.this.buildSemImsRegistrationError(imsRegistrationError));
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        public void binderDied() {
            ImsServiceStub.getInstance().unregisterImsRegistrationListenerForSlot(this.mToken, this.mPhoneId);
            SemImsServiceStub.this.mCallbacks.remove(this.mToken);
            reset();
        }

        /* access modifiers changed from: protected */
        public void reset() {
            try {
                this.mListener.asBinder().unlinkToDeath(this, 0);
            } catch (NoSuchElementException unused) {
            }
        }
    }

    private class ImsOngoingFtEventCallBack extends IImsOngoingFtEventListener.Stub {
        String mToken;

        private ImsOngoingFtEventCallBack() {
            this.mToken = null;
        }

        public void onFtStateChanged(boolean z) {
            for (SemImsFtCallBack semImsFtCallBack : SemImsServiceStub.this.mOngoingFtEventListeners.values()) {
                try {
                    semImsFtCallBack.mListener.onFtStateChanged(z);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class SimMobilityStatusCallBack extends ISimMobilityStatusListener.Stub implements IBinder.DeathRecipient {
        SemSimMobStatusListener mListener;
        private int mPhoneId;
        String mToken;

        public SimMobilityStatusCallBack(SemSimMobStatusListener semSimMobStatusListener, int i) {
            this.mListener = semSimMobStatusListener;
            this.mPhoneId = i;
            try {
                semSimMobStatusListener.asBinder().linkToDeath(this, 0);
            } catch (RemoteException unused) {
            }
        }

        public void onSimMobilityStateChanged(boolean z) {
            try {
                this.mListener.onSimMobilityStateChanged(z);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        public void binderDied() {
            ImsServiceStub.getInstance().unregisterSimMobilityStatusListenerByPhoneId(this.mToken, this.mPhoneId);
            SemImsServiceStub.this.mCallbacks.remove(this.mToken);
            reset();
        }

        /* access modifiers changed from: protected */
        public void reset() {
            try {
                this.mListener.asBinder().unlinkToDeath(this, 0);
            } catch (NoSuchElementException unused) {
            }
        }
    }

    private class EpdgListenerCallback extends IEpdgListener.Stub {
        String mToken;

        public void onEpdgDeregister(int i) {
        }

        public void onEpdgHandoverEnableChanged(int i, boolean z) {
        }

        public void onEpdgRegister(int i, boolean z) {
        }

        public void onEpdgReleaseCall(int i) {
        }

        private EpdgListenerCallback() {
            this.mToken = null;
        }

        public void onEpdgAvailable(int i, int i2, int i3) {
            if (SemImsServiceStub.this.mEpdgListeners != null) {
                for (SemEpdgCallBack semEpdgCallBack : SemImsServiceStub.this.mEpdgListeners.values()) {
                    boolean z = true;
                    if (i2 != 1) {
                        z = false;
                    }
                    try {
                        SemImsServiceStub.this.mEpdgAvailable[i] = z;
                        semEpdgCallBack.mListener.onEpdgAvailable(i, z, i3);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        public void onEpdgHandoverResult(int i, int i2, int i3, String str) {
            if (SemImsServiceStub.this.mEpdgListeners != null) {
                for (SemEpdgCallBack semEpdgCallBack : SemImsServiceStub.this.mEpdgListeners.values()) {
                    try {
                        semEpdgCallBack.mListener.onHandoverResult(i, i2, i3, str);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        public void onEpdgIpsecConnection(int i, String str, int i2, int i3) {
            if (SemImsServiceStub.this.mEpdgListeners != null) {
                for (SemEpdgCallBack semEpdgCallBack : SemImsServiceStub.this.mEpdgListeners.values()) {
                    try {
                        semEpdgCallBack.mListener.onIpsecConnection(i, str, i2, i3);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        public void onEpdgIpsecDisconnection(int i, String str) {
            if (SemImsServiceStub.this.mEpdgListeners != null) {
                for (SemEpdgCallBack semEpdgCallBack : SemImsServiceStub.this.mEpdgListeners.values()) {
                    try {
                        semEpdgCallBack.mListener.onIpsecDisconnection(i, str);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        public void onEpdgShowPopup(int i, int i2) {
            if (SemImsServiceStub.this.mEpdgListeners != null) {
                for (SemEpdgCallBack semEpdgCallBack : SemImsServiceStub.this.mEpdgListeners.values()) {
                    try {
                        semEpdgCallBack.mListener.onEpdgShowPopup(i, i2);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private class SemEpdgCallBack implements IBinder.DeathRecipient {
        final ISemEpdgListener mListener;
        final String mToken;

        public SemEpdgCallBack(ISemEpdgListener iSemEpdgListener, String str) {
            this.mListener = iSemEpdgListener;
            this.mToken = str;
            try {
                iSemEpdgListener.asBinder().linkToDeath(this, 0);
            } catch (RemoteException unused) {
            }
        }

        public void binderDied() {
            SemImsServiceStub.this.mEpdgListeners.remove(this.mToken);
            reset();
        }

        /* access modifiers changed from: protected */
        public void reset() {
            try {
                this.mListener.asBinder().unlinkToDeath(this, 0);
            } catch (NoSuchElementException unused) {
            }
        }
    }

    private class SemImsFtCallBack implements IBinder.DeathRecipient {
        final SemImsFtListener mListener;
        final String mToken;

        public SemImsFtCallBack(SemImsFtListener semImsFtListener, String str) {
            this.mListener = semImsFtListener;
            this.mToken = str;
            try {
                semImsFtListener.asBinder().linkToDeath(this, 0);
            } catch (RemoteException unused) {
            }
        }

        public void binderDied() {
            SemImsServiceStub.this.mOngoingFtEventListeners.remove(this.mToken);
            reset();
        }

        /* access modifiers changed from: protected */
        public void reset() {
            try {
                this.mListener.asBinder().unlinkToDeath(this, 0);
            } catch (NoSuchElementException unused) {
            }
        }
    }
}
