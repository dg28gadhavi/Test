package com.sec.internal.ims.servicemodules.options;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.UriMatcher;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.telephony.TelephonyCallback;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.sec.ims.extensions.ContextExt;
import com.sec.ims.extensions.TelephonyManagerExt;
import com.sec.ims.options.Capabilities;
import com.sec.ims.options.CapabilityRefreshType;
import com.sec.ims.options.ICapabilityService;
import com.sec.ims.options.ICapabilityServiceEventListener;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.SipMsg;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.os.PhoneConstants;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.CollectionUtils;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.UriUtil;
import com.sec.internal.helper.os.ImsFrameworkState;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.csh.IshIntents;
import com.sec.internal.ims.servicemodules.csh.event.VshIntents;
import com.sec.internal.ims.settings.ImsProfileLoaderInternal;
import com.sec.internal.ims.util.CscParser;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.ims.util.UriGenerator;
import com.sec.internal.ims.util.UriGeneratorFactory;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class CapabilityProvider extends ContentProvider {
    static final String ADDITIONAL_INFO_LOCAL_OFFLINE = "local_offline;";
    static final String ADDITIONAL_INFO_NONE = "";
    static final String ADDITIONAL_INFO_REMOTE_OFFLINE = "remote_offline;";
    static final String ADDITIONAL_INFO_REMOTE_ONLINE = "fresh;";
    private static final String AUTHORITY = "com.samsung.rcs.serviceprovider";
    private static final String LOG_TAG = "CapabilityProvider";
    static final int N_INCALL_SERVICE = 4;
    static final int N_LOOKUP_URI = 1;
    static final int N_LOOKUP_URI_ID = 2;
    static final int N_OPERATOR_RCS_VERSION = 7;
    static final int N_OWN_CAPS = 5;
    static final int N_RCS_BIG_DATA = 8;
    static final int N_RCS_ENABLED_STATIC = 6;
    static final int N_SIP_URI = 3;
    private static final Pattern OPTIONS_PATTERN = Pattern.compile("\\?");
    /* access modifiers changed from: private */
    public static boolean ready_ish = true;
    /* access modifiers changed from: private */
    public static boolean ready_vsh = true;
    static UriMatcher sMatcher;
    Map<ImsUri, Capabilities> mAsyncResults;
    Context mContext = null;
    /* access modifiers changed from: private */
    public int mDataNetworkType = 0;
    /* access modifiers changed from: private */
    public ImsUri mLastInCallUri = null;
    Object mLock = new Object();
    /* access modifiers changed from: private */
    public ShareServiceBroadcastReceiver mReceiver;
    ICapabilityService mService = null;
    protected TelephonyCallbackForCapabilityProvider mTelephonyCallback;

    static {
        UriMatcher uriMatcher = new UriMatcher(0);
        sMatcher = uriMatcher;
        uriMatcher.addURI(AUTHORITY, "lookup/*/#", 2);
        sMatcher.addURI(AUTHORITY, "lookup/*", 1);
        sMatcher.addURI(AUTHORITY, "sip/*", 3);
        sMatcher.addURI(AUTHORITY, "incall/*", 4);
        sMatcher.addURI(AUTHORITY, "own", 5);
        sMatcher.addURI(AUTHORITY, "rcs_enabled_static", 6);
        sMatcher.addURI(AUTHORITY, "operator_rcs_version", 7);
        sMatcher.addURI(AUTHORITY, "rcs_big_data/*", 8);
    }

    public boolean onCreate() {
        Log.i(LOG_TAG, "onCreate");
        if (this.mContext == null) {
            this.mContext = getContext();
        }
        this.mAsyncResults = new HashMap();
        this.mReceiver = new ShareServiceBroadcastReceiver();
        this.mTelephonyCallback = new TelephonyCallbackForCapabilityProvider();
        ((TelephonyManager) this.mContext.getSystemService(PhoneConstants.PHONE_KEY)).registerTelephonyCallback(this.mContext.getMainExecutor(), this.mTelephonyCallback);
        ImsFrameworkState.getInstance(this.mContext).registerForFrameworkState(new CapabilityProvider$$ExternalSyntheticLambda0(this));
        return false;
    }

    /* access modifiers changed from: package-private */
    /* renamed from: initCapabilityService */
    public void lambda$onCreate$0() {
        Log.i(LOG_TAG, "Connecting to CapabilityDiscoveryService.");
        Intent intent = new Intent();
        intent.setClassName("com.sec.imsservice", "com.sec.internal.ims.imsservice.CapabilityService");
        ContextExt.bindServiceAsUser(this.mContext, intent, new ServiceConnection() {
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                Log.i(CapabilityProvider.LOG_TAG, "Connected to CapabilityDiscoveryService.");
                CapabilityProvider.this.mService = ICapabilityService.Stub.asInterface(iBinder);
                if (CapabilityProvider.this.mService == null) {
                    Log.i(CapabilityProvider.LOG_TAG, "Failed to get ICapabilityService with " + iBinder);
                    return;
                }
                try {
                    int phoneCount = SimUtil.getPhoneCount();
                    for (int i = 0; i < phoneCount; i++) {
                        CapabilityProvider.this.mService.registerListener(new ICapabilityServiceEventListener.Stub() {
                            public void onCapabilityAndAvailabilityPublished(int i) throws RemoteException {
                            }

                            public void onMultipleCapabilitiesChanged(List<ImsUri> list, List<Capabilities> list2) throws RemoteException {
                            }

                            public void onCapabilitiesChanged(List<ImsUri> list, Capabilities capabilities) throws RemoteException {
                                ImsUri imsUri;
                                if (list != null) {
                                    for (ImsUri next : list) {
                                        if (UriUtil.equals(next, CapabilityProvider.this.mLastInCallUri)) {
                                            CapabilityProvider.ready_ish = true;
                                            CapabilityProvider.ready_vsh = true;
                                            CapabilityProvider.this.notifyInCallServicesChange();
                                        }
                                        Iterator<ImsUri> it = CapabilityProvider.this.mAsyncResults.keySet().iterator();
                                        while (true) {
                                            if (!it.hasNext()) {
                                                imsUri = null;
                                                break;
                                            }
                                            imsUri = it.next();
                                            if (UriUtil.equals(imsUri, next)) {
                                                break;
                                            }
                                        }
                                        if (imsUri != null) {
                                            CapabilityProvider.this.mAsyncResults.put(imsUri, capabilities);
                                            CapabilityProvider.this.wakeup();
                                        }
                                        CapabilityProvider.this.notifyCapabilityChange(next);
                                    }
                                }
                            }

                            public void onOwnCapabilitiesChanged() throws RemoteException {
                                CapabilityProvider.ready_ish = true;
                                CapabilityProvider.ready_vsh = true;
                                CapabilityProvider.this.notifyOwnServicesChange();
                                CapabilityProvider.this.notifyInCallServicesChange();
                            }
                        }, i);
                    }
                } catch (RemoteException | NullPointerException e) {
                    e.printStackTrace();
                }
                CapabilityProvider capabilityProvider = CapabilityProvider.this;
                capabilityProvider.mContext.registerReceiver(capabilityProvider.mReceiver, CapabilityProvider.createIntentFilter());
            }

            public void onServiceDisconnected(ComponentName componentName) {
                Log.i(CapabilityProvider.LOG_TAG, "Disconnected.");
                CapabilityProvider.this.mService = null;
            }
        }, 1, ContextExt.CURRENT_OR_SELF);
    }

    /* access modifiers changed from: package-private */
    public void waitForUpdate(ImsUri imsUri) {
        IMSLog.s(LOG_TAG, "waitForUpdate: remote uri " + imsUri);
        try {
            this.mAsyncResults.put(imsUri, (Object) null);
            synchronized (this.mLock) {
                this.mLock.wait(1500);
            }
        } catch (InterruptedException e) {
            this.mAsyncResults.remove(imsUri);
            e.printStackTrace();
        }
    }

    /* access modifiers changed from: private */
    public void wakeup() {
        synchronized (this.mLock) {
            this.mLock.notify();
        }
    }

    /* access modifiers changed from: package-private */
    public Capabilities getAsyncCapexResult(ImsUri imsUri) {
        Capabilities capabilities = this.mAsyncResults.get(imsUri);
        if (capabilities != null) {
            this.mAsyncResults.remove(imsUri);
        }
        return capabilities;
    }

    public Cursor query(Uri uri, String[] strArr, String str, String[] strArr2, String str2) {
        IMSLog.s(LOG_TAG, "query(Uri, String[], String, String[], String) - uri: " + uri + ", selection: " + str + ", args: " + Arrays.toString(strArr2));
        if (this.mService == null) {
            Log.e(LOG_TAG, "query before provider was started! Returning empty response");
            return new MatrixCursor(Projections.SERVICE_PROJECTION);
        }
        String[] split = OPTIONS_PATTERN.split(uri.toString());
        Uri parse = Uri.parse(split[0]);
        List<String> pathSegments = parse.getPathSegments();
        CapabilityRefreshType requeryStrategyId = getRequeryStrategyId(split.length == 2 ? split[1] : null);
        int simSlotFromUri = UriUtil.getSimSlotFromUri(parse);
        switch (sMatcher.match(parse)) {
            case 2:
                IMSLog.s(LOG_TAG, simSlotFromUri, "N_LOOKUP_URI_ID | Operation for uri: ".concat(parse.toString()));
                return queryLookupUriId(pathSegments, requeryStrategyId, simSlotFromUri);
            case 3:
                IMSLog.s(LOG_TAG, simSlotFromUri, "N_SIP_URI | Operation for uri: ".concat(parse.toString()));
                return querySipUri(strArr, pathSegments, requeryStrategyId, simSlotFromUri);
            case 4:
                IMSLog.s(LOG_TAG, simSlotFromUri, "N_INCALL_SERVICE | Operation for uri: ".concat(parse.toString()));
                return queryIncallService(pathSegments, requeryStrategyId, simSlotFromUri);
            case 5:
                IMSLog.s(LOG_TAG, simSlotFromUri, "N_OWN_CAPS | Operation for uri: ".concat(parse.toString()));
                return queryOwnCaps(simSlotFromUri);
            case 6:
                IMSLog.i(LOG_TAG, simSlotFromUri, "N_RCS_ENABLED_STATIC");
                return queryRcsEnabledStatic(simSlotFromUri);
            case 7:
                IMSLog.i(LOG_TAG, simSlotFromUri, "N_OPERATOR_RCS_VERSION");
                return queryOperatorRcsVersion(simSlotFromUri);
            case 8:
                IMSLog.s(LOG_TAG, simSlotFromUri, "N_RCS_BIG_DATA | Operation for uri: ".concat(parse.toString()));
                return queryRcsBigData(pathSegments, simSlotFromUri);
            default:
                IMSLog.s(LOG_TAG, simSlotFromUri, "UNDEFINED CATEGORY! | Operation for uri: ".concat(parse.toString()));
                throw new UnsupportedOperationException("Operation not supported for uri: ".concat(parse.toString()));
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x00c0 A[Catch:{ RemoteException -> 0x01df }] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public android.database.Cursor queryLookupUriId(java.util.List<java.lang.String> r21, com.sec.ims.options.CapabilityRefreshType r22, int r23) {
        /*
            r20 = this;
            r7 = r20
            r0 = r21
            r8 = r23
            java.lang.String r1 = "queryLookupUriId"
            java.lang.String r9 = "CapabilityProvider"
            com.sec.internal.log.IMSLog.i(r9, r8, r1)
            int r1 = r21.size()
            r10 = 1
            int r1 = r1 - r10
            java.lang.Object r1 = r0.get(r1)
            java.lang.String r1 = (java.lang.String) r1
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            int r3 = r21.size()
            int r3 = r3 + -2
            java.lang.Object r0 = r0.get(r3)
            java.lang.String r0 = (java.lang.String) r0
            r2.append(r0)
            java.lang.String r0 = "/"
            r2.append(r0)
            r2.append(r1)
            java.lang.String r11 = r2.toString()
            android.database.MatrixCursor r12 = new android.database.MatrixCursor
            java.lang.String[] r0 = com.sec.internal.ims.servicemodules.options.CapabilityProvider.Projections.SERVICE_PROJECTION
            r12.<init>(r0)
            com.sec.ims.options.ICapabilityService r0 = r7.mService     // Catch:{ RemoteException -> 0x01df }
            int r2 = r22.ordinal()     // Catch:{ RemoteException -> 0x01df }
            com.sec.ims.options.Capabilities[] r13 = r0.getCapabilitiesByContactId(r1, r2, r8)     // Catch:{ RemoteException -> 0x01df }
            if (r13 != 0) goto L_0x0063
            java.lang.StringBuilder r0 = new java.lang.StringBuilder     // Catch:{ RemoteException -> 0x01df }
            r0.<init>()     // Catch:{ RemoteException -> 0x01df }
            java.lang.String r2 = "queryLookupUriId: Capabilities not found for contactId "
            r0.append(r2)     // Catch:{ RemoteException -> 0x01df }
            r0.append(r1)     // Catch:{ RemoteException -> 0x01df }
            java.lang.String r0 = r0.toString()     // Catch:{ RemoteException -> 0x01df }
            com.sec.internal.log.IMSLog.e(r9, r8, r0)     // Catch:{ RemoteException -> 0x01df }
            return r12
        L_0x0063:
            java.lang.String r0 = "remote_offline;"
            int r14 = r13.length     // Catch:{ RemoteException -> 0x01df }
            r1 = 0
            r6 = 0
        L_0x0069:
            if (r6 >= r14) goto L_0x01f4
            r5 = r13[r6]     // Catch:{ RemoteException -> 0x01df }
            boolean r2 = r5.getExpired()     // Catch:{ RemoteException -> 0x01df }
            boolean r3 = r5.isAvailable()     // Catch:{ RemoteException -> 0x01df }
            if (r3 == 0) goto L_0x007e
            if (r2 == 0) goto L_0x007c
            java.lang.String r0 = ""
            goto L_0x007e
        L_0x007c:
            java.lang.String r0 = "fresh;"
        L_0x007e:
            r16 = r0
            int r17 = r1 + 1
            int r0 = com.sec.ims.options.Capabilities.FEATURE_CHAT_CPM     // Catch:{ RemoteException -> 0x01df }
            boolean r0 = r5.hasFeature(r0)     // Catch:{ RemoteException -> 0x01df }
            if (r0 != 0) goto L_0x0095
            int r0 = com.sec.ims.options.Capabilities.FEATURE_CHAT_SIMPLE_IM     // Catch:{ RemoteException -> 0x01df }
            boolean r0 = r5.hasFeature(r0)     // Catch:{ RemoteException -> 0x01df }
            if (r0 == 0) goto L_0x0093
            goto L_0x0095
        L_0x0093:
            r2 = 0
            goto L_0x0096
        L_0x0095:
            r2 = r10
        L_0x0096:
            com.sec.ims.util.ImsUri r0 = r5.getUri()     // Catch:{ RemoteException -> 0x01df }
            java.lang.String r3 = r0.toString()     // Catch:{ RemoteException -> 0x01df }
            java.lang.String r4 = r5.getDisplayName()     // Catch:{ RemoteException -> 0x01df }
            java.lang.String r18 = r5.getNumber()     // Catch:{ RemoteException -> 0x01df }
            r0 = r20
            r10 = r5
            r5 = r16
            r19 = r6
            r6 = r18
            java.lang.Object[] r0 = r0.createImRow(r1, r2, r3, r4, r5, r6)     // Catch:{ RemoteException -> 0x01df }
            r12.addRow(r0)     // Catch:{ RemoteException -> 0x01df }
            int r18 = r17 + 1
            int r0 = com.sec.ims.options.Capabilities.FEATURE_FT     // Catch:{ RemoteException -> 0x01df }
            boolean r0 = r10.hasFeature(r0)     // Catch:{ RemoteException -> 0x01df }
            if (r0 != 0) goto L_0x00cb
            int r0 = com.sec.ims.options.Capabilities.FEATURE_FT_STORE     // Catch:{ RemoteException -> 0x01df }
            boolean r0 = r10.hasFeature(r0)     // Catch:{ RemoteException -> 0x01df }
            if (r0 == 0) goto L_0x00c9
            goto L_0x00cb
        L_0x00c9:
            r2 = 0
            goto L_0x00cc
        L_0x00cb:
            r2 = 1
        L_0x00cc:
            com.sec.ims.util.ImsUri r0 = r10.getUri()     // Catch:{ RemoteException -> 0x01df }
            java.lang.String r3 = r0.toString()     // Catch:{ RemoteException -> 0x01df }
            java.lang.String r4 = r10.getDisplayName()     // Catch:{ RemoteException -> 0x01df }
            java.lang.String r6 = r10.getNumber()     // Catch:{ RemoteException -> 0x01df }
            r0 = r20
            r1 = r17
            r5 = r16
            java.lang.Object[] r0 = r0.createFtRow(r1, r2, r3, r4, r5, r6)     // Catch:{ RemoteException -> 0x01df }
            r12.addRow(r0)     // Catch:{ RemoteException -> 0x01df }
            int r17 = r18 + 1
            int r0 = com.sec.ims.options.Capabilities.FEATURE_FT_HTTP     // Catch:{ RemoteException -> 0x01df }
            boolean r2 = r10.hasFeature(r0)     // Catch:{ RemoteException -> 0x01df }
            com.sec.ims.util.ImsUri r0 = r10.getUri()     // Catch:{ RemoteException -> 0x01df }
            java.lang.String r3 = r0.toString()     // Catch:{ RemoteException -> 0x01df }
            java.lang.String r4 = r10.getDisplayName()     // Catch:{ RemoteException -> 0x01df }
            java.lang.String r6 = r10.getNumber()     // Catch:{ RemoteException -> 0x01df }
            r0 = r20
            r1 = r18
            r5 = r16
            java.lang.Object[] r0 = r0.createFtHttpRow(r1, r2, r3, r4, r5, r6)     // Catch:{ RemoteException -> 0x01df }
            r12.addRow(r0)     // Catch:{ RemoteException -> 0x01df }
            int r6 = r17 + 1
            int r0 = com.sec.ims.options.Capabilities.FEATURE_STANDALONE_MSG     // Catch:{ RemoteException -> 0x01df }
            boolean r2 = r10.hasFeature(r0)     // Catch:{ RemoteException -> 0x01df }
            com.sec.ims.util.ImsUri r0 = r10.getUri()     // Catch:{ RemoteException -> 0x01df }
            java.lang.String r3 = r0.toString()     // Catch:{ RemoteException -> 0x01df }
            java.lang.String r4 = r10.getDisplayName()     // Catch:{ RemoteException -> 0x01df }
            java.lang.String r18 = r10.getNumber()     // Catch:{ RemoteException -> 0x01df }
            r0 = r20
            r1 = r17
            r5 = r16
            r15 = r6
            r6 = r18
            java.lang.Object[] r0 = r0.createSlmRow(r1, r2, r3, r4, r5, r6)     // Catch:{ RemoteException -> 0x01df }
            r12.addRow(r0)     // Catch:{ RemoteException -> 0x01df }
            int r6 = r15 + 1
            int r0 = com.sec.ims.options.Capabilities.FEATURE_GEOLOCATION_PUSH     // Catch:{ RemoteException -> 0x01df }
            boolean r0 = r10.hasFeature(r0)     // Catch:{ RemoteException -> 0x01df }
            com.sec.ims.util.ImsUri r1 = r10.getUri()     // Catch:{ RemoteException -> 0x01df }
            java.lang.String r1 = r1.toString()     // Catch:{ RemoteException -> 0x01df }
            java.lang.Object[] r0 = r7.createGeolocationPushRow(r15, r0, r1)     // Catch:{ RemoteException -> 0x01df }
            r12.addRow(r0)     // Catch:{ RemoteException -> 0x01df }
            int r1 = r6 + 1
            int r0 = com.sec.ims.options.Capabilities.FEATURE_GEO_VIA_SMS     // Catch:{ RemoteException -> 0x01df }
            boolean r0 = r10.hasFeature(r0)     // Catch:{ RemoteException -> 0x01df }
            com.sec.ims.util.ImsUri r2 = r10.getUri()     // Catch:{ RemoteException -> 0x01df }
            java.lang.String r2 = r2.toString()     // Catch:{ RemoteException -> 0x01df }
            java.lang.Object[] r0 = r7.createGeoPushViaSMSRow(r6, r0, r2)     // Catch:{ RemoteException -> 0x01df }
            r12.addRow(r0)     // Catch:{ RemoteException -> 0x01df }
            int r15 = r1 + 1
            int r0 = com.sec.ims.options.Capabilities.FEATURE_SF_GROUP_CHAT     // Catch:{ RemoteException -> 0x01df }
            boolean r2 = r10.hasFeature(r0)     // Catch:{ RemoteException -> 0x01df }
            com.sec.ims.util.ImsUri r0 = r10.getUri()     // Catch:{ RemoteException -> 0x01df }
            java.lang.String r3 = r0.toString()     // Catch:{ RemoteException -> 0x01df }
            java.lang.String r4 = r10.getDisplayName()     // Catch:{ RemoteException -> 0x01df }
            java.lang.String r6 = r10.getNumber()     // Catch:{ RemoteException -> 0x01df }
            r0 = r20
            r5 = r16
            java.lang.Object[] r0 = r0.createFtSfGroupChatRow(r1, r2, r3, r4, r5, r6)     // Catch:{ RemoteException -> 0x01df }
            r12.addRow(r0)     // Catch:{ RemoteException -> 0x01df }
            int r1 = r15 + 1
            int r0 = com.sec.ims.options.Capabilities.FEATURE_INTEGRATED_MSG     // Catch:{ RemoteException -> 0x01df }
            boolean r0 = r10.hasFeature(r0)     // Catch:{ RemoteException -> 0x01df }
            com.sec.ims.util.ImsUri r2 = r10.getUri()     // Catch:{ RemoteException -> 0x01df }
            java.lang.String r2 = r2.toString()     // Catch:{ RemoteException -> 0x01df }
            java.lang.Object[] r0 = r7.createIntegratedMessageRow(r15, r0, r2)     // Catch:{ RemoteException -> 0x01df }
            r12.addRow(r0)     // Catch:{ RemoteException -> 0x01df }
            int r15 = r1 + 1
            long r2 = com.sec.ims.options.Capabilities.FEATURE_PUBLIC_MSG     // Catch:{ RemoteException -> 0x01df }
            boolean r2 = r10.hasFeature(r2)     // Catch:{ RemoteException -> 0x01df }
            com.sec.ims.util.ImsUri r0 = r10.getUri()     // Catch:{ RemoteException -> 0x01df }
            java.lang.String r3 = r0.toString()     // Catch:{ RemoteException -> 0x01df }
            java.lang.String r4 = r10.getDisplayName()     // Catch:{ RemoteException -> 0x01df }
            java.lang.String r6 = r10.getNumber()     // Catch:{ RemoteException -> 0x01df }
            r0 = r20
            r5 = r16
            java.lang.Object[] r0 = r0.createPublicMsgRow(r1, r2, r3, r4, r5, r6)     // Catch:{ RemoteException -> 0x01df }
            r12.addRow(r0)     // Catch:{ RemoteException -> 0x01df }
            int r1 = r15 + 1
            long r2 = com.sec.ims.options.Capabilities.FEATURE_CANCEL_MESSAGE     // Catch:{ RemoteException -> 0x01df }
            boolean r0 = r10.hasFeature(r2)     // Catch:{ RemoteException -> 0x01df }
            com.sec.ims.util.ImsUri r2 = r10.getUri()     // Catch:{ RemoteException -> 0x01df }
            java.lang.String r2 = r2.toString()     // Catch:{ RemoteException -> 0x01df }
            java.lang.Object[] r0 = r7.createCancelMessageRow(r15, r0, r2)     // Catch:{ RemoteException -> 0x01df }
            r12.addRow(r0)     // Catch:{ RemoteException -> 0x01df }
            int r6 = r19 + 1
            r0 = r16
            r10 = 1
            goto L_0x0069
        L_0x01df:
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r1 = "queryLookupUriId: no uris exist for lookup, returning empty response: "
            r0.append(r1)
            r0.append(r11)
            java.lang.String r0 = r0.toString()
            com.sec.internal.log.IMSLog.e(r9, r8, r0)
        L_0x01f4:
            return r12
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.options.CapabilityProvider.queryLookupUriId(java.util.List, com.sec.ims.options.CapabilityRefreshType, int):android.database.Cursor");
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x00df A[Catch:{ RemoteException -> 0x02a8 }] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public android.database.Cursor querySipUri(java.lang.String[] r21, java.util.List<java.lang.String> r22, com.sec.ims.options.CapabilityRefreshType r23, int r24) {
        /*
            r20 = this;
            r0 = r20
            r8 = r24
            java.lang.String r1 = "querySipUri"
            java.lang.String r9 = "CapabilityProvider"
            com.sec.internal.log.IMSLog.i(r9, r8, r1)
            android.database.MatrixCursor r10 = new android.database.MatrixCursor
            java.lang.String[] r1 = com.sec.internal.ims.servicemodules.options.CapabilityProvider.Projections.SERVICE_PROJECTION
            r10.<init>(r1)
            int r1 = r22.size()
            r11 = 1
            int r1 = r1 - r11
            r2 = r22
            java.lang.Object r1 = r2.get(r1)
            java.lang.String r1 = (java.lang.String) r1
            java.util.ArrayList r1 = r0.getImsUriListFromQuery(r1)
            if (r1 == 0) goto L_0x02b0
            boolean r2 = r1.isEmpty()
            if (r2 == 0) goto L_0x002f
            goto L_0x02b0
        L_0x002f:
            r12 = 0
            com.sec.ims.options.ICapabilityService r2 = r0.mService     // Catch:{ RemoteException -> 0x02a8 }
            int r3 = r23.ordinal()     // Catch:{ RemoteException -> 0x02a8 }
            int r4 = com.sec.ims.options.Capabilities.FEATURE_OFFLINE_RCS_USER     // Catch:{ RemoteException -> 0x02a8 }
            com.sec.ims.options.Capabilities[] r13 = r2.getCapabilitiesWithFeatureByUriList(r1, r3, r4, r8)     // Catch:{ RemoteException -> 0x02a8 }
            if (r13 != 0) goto L_0x0054
            java.lang.StringBuilder r0 = new java.lang.StringBuilder     // Catch:{ RemoteException -> 0x02a8 }
            r0.<init>()     // Catch:{ RemoteException -> 0x02a8 }
            java.lang.String r2 = "querySipUri: Capabilities not found for "
            r0.append(r2)     // Catch:{ RemoteException -> 0x02a8 }
            r0.append(r1)     // Catch:{ RemoteException -> 0x02a8 }
            java.lang.String r0 = r0.toString()     // Catch:{ RemoteException -> 0x02a8 }
            com.sec.internal.log.IMSLog.s(r9, r8, r0)     // Catch:{ RemoteException -> 0x02a8 }
            return r10
        L_0x0054:
            int r14 = r13.length     // Catch:{ RemoteException -> 0x02a8 }
            r15 = 0
            r7 = r15
        L_0x0057:
            if (r7 >= r14) goto L_0x02a7
            r6 = r13[r7]     // Catch:{ RemoteException -> 0x02a8 }
            if (r6 != 0) goto L_0x0061
            r17 = r7
            goto L_0x02a2
        L_0x0061:
            java.lang.String r1 = "querySipUri: return service info."
            com.sec.internal.log.IMSLog.i(r9, r8, r1)     // Catch:{ RemoteException -> 0x02a8 }
            boolean r1 = r6.hasNoRcsFeatures()     // Catch:{ RemoteException -> 0x02a8 }
            if (r1 == 0) goto L_0x006f
            r5 = r12
            goto L_0x0085
        L_0x006f:
            boolean r1 = r6.getExpired()     // Catch:{ RemoteException -> 0x02a8 }
            boolean r2 = r6.isAvailable()     // Catch:{ RemoteException -> 0x02a8 }
            if (r2 == 0) goto L_0x0081
            if (r1 == 0) goto L_0x007e
            java.lang.String r1 = ""
            goto L_0x0084
        L_0x007e:
            java.lang.String r1 = "fresh;"
            goto L_0x0084
        L_0x0081:
            java.lang.String r1 = "remote_offline;"
        L_0x0084:
            r5 = r1
        L_0x0085:
            java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch:{ RemoteException -> 0x02a8 }
            r1.<init>()     // Catch:{ RemoteException -> 0x02a8 }
            java.lang.String r2 = "querySipUri: RCS additionalInfo = "
            r1.append(r2)     // Catch:{ RemoteException -> 0x02a8 }
            r1.append(r5)     // Catch:{ RemoteException -> 0x02a8 }
            java.lang.String r1 = r1.toString()     // Catch:{ RemoteException -> 0x02a8 }
            com.sec.internal.log.IMSLog.i(r9, r8, r1)     // Catch:{ RemoteException -> 0x02a8 }
            r16 = 1
            int r1 = com.sec.ims.options.Capabilities.FEATURE_CHAT_CPM     // Catch:{ RemoteException -> 0x02a8 }
            boolean r1 = r6.hasFeature(r1)     // Catch:{ RemoteException -> 0x02a8 }
            if (r1 != 0) goto L_0x00af
            int r1 = com.sec.ims.options.Capabilities.FEATURE_CHAT_SIMPLE_IM     // Catch:{ RemoteException -> 0x02a8 }
            boolean r1 = r6.hasFeature(r1)     // Catch:{ RemoteException -> 0x02a8 }
            if (r1 == 0) goto L_0x00ad
            goto L_0x00af
        L_0x00ad:
            r3 = r15
            goto L_0x00b0
        L_0x00af:
            r3 = r11
        L_0x00b0:
            com.sec.ims.util.ImsUri r1 = r6.getUri()     // Catch:{ RemoteException -> 0x02a8 }
            java.lang.String r4 = r1.toString()     // Catch:{ RemoteException -> 0x02a8 }
            java.lang.String r17 = r6.getDisplayName()     // Catch:{ RemoteException -> 0x02a8 }
            java.lang.String r18 = r6.getNumber()     // Catch:{ RemoteException -> 0x02a8 }
            r2 = 0
            r1 = r20
            r19 = r5
            r5 = r17
            r11 = r6
            r6 = r19
            r17 = r7
            r7 = r18
            java.lang.Object[] r1 = r1.createImRow(r2, r3, r4, r5, r6, r7)     // Catch:{ RemoteException -> 0x02a8 }
            r10.addRow(r1)     // Catch:{ RemoteException -> 0x02a8 }
            r18 = 2
            int r1 = com.sec.ims.options.Capabilities.FEATURE_FT     // Catch:{ RemoteException -> 0x02a8 }
            boolean r1 = r11.hasFeature(r1)     // Catch:{ RemoteException -> 0x02a8 }
            if (r1 != 0) goto L_0x00ea
            int r1 = com.sec.ims.options.Capabilities.FEATURE_FT_STORE     // Catch:{ RemoteException -> 0x02a8 }
            boolean r1 = r11.hasFeature(r1)     // Catch:{ RemoteException -> 0x02a8 }
            if (r1 == 0) goto L_0x00e8
            goto L_0x00ea
        L_0x00e8:
            r3 = r15
            goto L_0x00eb
        L_0x00ea:
            r3 = 1
        L_0x00eb:
            com.sec.ims.util.ImsUri r1 = r11.getUri()     // Catch:{ RemoteException -> 0x02a8 }
            java.lang.String r4 = r1.toString()     // Catch:{ RemoteException -> 0x02a8 }
            java.lang.String r5 = r11.getDisplayName()     // Catch:{ RemoteException -> 0x02a8 }
            r6 = 0
            java.lang.String r7 = r11.getNumber()     // Catch:{ RemoteException -> 0x02a8 }
            r1 = r20
            r2 = r16
            java.lang.Object[] r1 = r1.createFtRow(r2, r3, r4, r5, r6, r7)     // Catch:{ RemoteException -> 0x02a8 }
            r10.addRow(r1)     // Catch:{ RemoteException -> 0x02a8 }
            r16 = 3
            int r1 = com.sec.ims.options.Capabilities.FEATURE_FT_HTTP     // Catch:{ RemoteException -> 0x02a8 }
            boolean r3 = r11.hasFeature(r1)     // Catch:{ RemoteException -> 0x02a8 }
            com.sec.ims.util.ImsUri r1 = r11.getUri()     // Catch:{ RemoteException -> 0x02a8 }
            java.lang.String r4 = r1.toString()     // Catch:{ RemoteException -> 0x02a8 }
            java.lang.String r5 = r11.getDisplayName()     // Catch:{ RemoteException -> 0x02a8 }
            r6 = 0
            java.lang.String r7 = r11.getNumber()     // Catch:{ RemoteException -> 0x02a8 }
            r1 = r20
            r2 = r18
            java.lang.Object[] r1 = r1.createFtHttpRow(r2, r3, r4, r5, r6, r7)     // Catch:{ RemoteException -> 0x02a8 }
            r10.addRow(r1)     // Catch:{ RemoteException -> 0x02a8 }
            int r1 = com.sec.ims.options.Capabilities.FEATURE_STANDALONE_MSG     // Catch:{ RemoteException -> 0x02a8 }
            boolean r3 = r11.hasFeature(r1)     // Catch:{ RemoteException -> 0x02a8 }
            com.sec.ims.util.ImsUri r1 = r11.getUri()     // Catch:{ RemoteException -> 0x02a8 }
            java.lang.String r4 = r1.toString()     // Catch:{ RemoteException -> 0x02a8 }
            java.lang.String r5 = r11.getDisplayName()     // Catch:{ RemoteException -> 0x02a8 }
            java.lang.String r7 = r11.getNumber()     // Catch:{ RemoteException -> 0x02a8 }
            r1 = r20
            r2 = r16
            r6 = r19
            java.lang.Object[] r1 = r1.createSlmRow(r2, r3, r4, r5, r6, r7)     // Catch:{ RemoteException -> 0x02a8 }
            r10.addRow(r1)     // Catch:{ RemoteException -> 0x02a8 }
            int r1 = com.sec.ims.options.Capabilities.FEATURE_GEOLOCATION_PUSH     // Catch:{ RemoteException -> 0x02a8 }
            boolean r1 = r11.hasFeature(r1)     // Catch:{ RemoteException -> 0x02a8 }
            com.sec.ims.util.ImsUri r2 = r11.getUri()     // Catch:{ RemoteException -> 0x02a8 }
            java.lang.String r2 = r2.toString()     // Catch:{ RemoteException -> 0x02a8 }
            r3 = 4
            java.lang.Object[] r1 = r0.createGeolocationPushRow(r3, r1, r2)     // Catch:{ RemoteException -> 0x02a8 }
            r10.addRow(r1)     // Catch:{ RemoteException -> 0x02a8 }
            r2 = 6
            int r1 = com.sec.ims.options.Capabilities.FEATURE_GEO_VIA_SMS     // Catch:{ RemoteException -> 0x02a8 }
            boolean r1 = r11.hasFeature(r1)     // Catch:{ RemoteException -> 0x02a8 }
            com.sec.ims.util.ImsUri r3 = r11.getUri()     // Catch:{ RemoteException -> 0x02a8 }
            java.lang.String r3 = r3.toString()     // Catch:{ RemoteException -> 0x02a8 }
            r4 = 5
            java.lang.Object[] r1 = r0.createGeoPushViaSMSRow(r4, r1, r3)     // Catch:{ RemoteException -> 0x02a8 }
            r10.addRow(r1)     // Catch:{ RemoteException -> 0x02a8 }
            int r1 = com.sec.ims.options.Capabilities.FEATURE_SF_GROUP_CHAT     // Catch:{ RemoteException -> 0x02a8 }
            boolean r3 = r11.hasFeature(r1)     // Catch:{ RemoteException -> 0x02a8 }
            com.sec.ims.util.ImsUri r1 = r11.getUri()     // Catch:{ RemoteException -> 0x02a8 }
            java.lang.String r4 = r1.toString()     // Catch:{ RemoteException -> 0x02a8 }
            java.lang.String r5 = r11.getDisplayName()     // Catch:{ RemoteException -> 0x02a8 }
            r6 = 0
            java.lang.String r7 = r11.getNumber()     // Catch:{ RemoteException -> 0x02a8 }
            r1 = r20
            java.lang.Object[] r1 = r1.createFtSfGroupChatRow(r2, r3, r4, r5, r6, r7)     // Catch:{ RemoteException -> 0x02a8 }
            r10.addRow(r1)     // Catch:{ RemoteException -> 0x02a8 }
            r2 = 8
            int r1 = com.sec.ims.options.Capabilities.FEATURE_INTEGRATED_MSG     // Catch:{ RemoteException -> 0x02a8 }
            boolean r1 = r11.hasFeature(r1)     // Catch:{ RemoteException -> 0x02a8 }
            com.sec.ims.util.ImsUri r3 = r11.getUri()     // Catch:{ RemoteException -> 0x02a8 }
            java.lang.String r3 = r3.toString()     // Catch:{ RemoteException -> 0x02a8 }
            r4 = 7
            java.lang.Object[] r1 = r0.createIntegratedMessageRow(r4, r1, r3)     // Catch:{ RemoteException -> 0x02a8 }
            r10.addRow(r1)     // Catch:{ RemoteException -> 0x02a8 }
            r16 = 9
            int r1 = com.sec.ims.options.Capabilities.FEATURE_STICKER     // Catch:{ RemoteException -> 0x02a8 }
            boolean r3 = r11.hasFeature(r1)     // Catch:{ RemoteException -> 0x02a8 }
            com.sec.ims.util.ImsUri r1 = r11.getUri()     // Catch:{ RemoteException -> 0x02a8 }
            java.lang.String r4 = r1.toString()     // Catch:{ RemoteException -> 0x02a8 }
            java.lang.String r5 = r11.getDisplayName()     // Catch:{ RemoteException -> 0x02a8 }
            r6 = 0
            java.lang.String r7 = r11.getNumber()     // Catch:{ RemoteException -> 0x02a8 }
            r1 = r20
            java.lang.Object[] r1 = r1.createStickerRow(r2, r3, r4, r5, r6, r7)     // Catch:{ RemoteException -> 0x02a8 }
            r10.addRow(r1)     // Catch:{ RemoteException -> 0x02a8 }
            r18 = 10
            long r1 = com.sec.ims.options.Capabilities.FEATURE_ENRICHED_CALL_COMPOSER     // Catch:{ RemoteException -> 0x02a8 }
            boolean r3 = r11.hasFeature(r1)     // Catch:{ RemoteException -> 0x02a8 }
            com.sec.ims.util.ImsUri r1 = r11.getUri()     // Catch:{ RemoteException -> 0x02a8 }
            java.lang.String r4 = r1.toString()     // Catch:{ RemoteException -> 0x02a8 }
            java.lang.String r5 = r11.getDisplayName()     // Catch:{ RemoteException -> 0x02a8 }
            java.lang.String r7 = r11.getNumber()     // Catch:{ RemoteException -> 0x02a8 }
            r1 = r20
            r2 = r16
            r6 = r19
            java.lang.Object[] r1 = r1.createCallComposerRow(r2, r3, r4, r5, r6, r7)     // Catch:{ RemoteException -> 0x02a8 }
            r10.addRow(r1)     // Catch:{ RemoteException -> 0x02a8 }
            r16 = 11
            long r1 = com.sec.ims.options.Capabilities.FEATURE_ENRICHED_SHARED_MAP     // Catch:{ RemoteException -> 0x02a8 }
            boolean r3 = r11.hasFeature(r1)     // Catch:{ RemoteException -> 0x02a8 }
            com.sec.ims.util.ImsUri r1 = r11.getUri()     // Catch:{ RemoteException -> 0x02a8 }
            java.lang.String r4 = r1.toString()     // Catch:{ RemoteException -> 0x02a8 }
            java.lang.String r5 = r11.getDisplayName()     // Catch:{ RemoteException -> 0x02a8 }
            java.lang.String r7 = r11.getNumber()     // Catch:{ RemoteException -> 0x02a8 }
            r1 = r20
            r2 = r18
            r6 = r19
            java.lang.Object[] r1 = r1.createSharedMapRow(r2, r3, r4, r5, r6, r7)     // Catch:{ RemoteException -> 0x02a8 }
            r10.addRow(r1)     // Catch:{ RemoteException -> 0x02a8 }
            r18 = 12
            long r1 = com.sec.ims.options.Capabilities.FEATURE_ENRICHED_SHARED_SKETCH     // Catch:{ RemoteException -> 0x02a8 }
            boolean r3 = r11.hasFeature(r1)     // Catch:{ RemoteException -> 0x02a8 }
            com.sec.ims.util.ImsUri r1 = r11.getUri()     // Catch:{ RemoteException -> 0x02a8 }
            java.lang.String r4 = r1.toString()     // Catch:{ RemoteException -> 0x02a8 }
            java.lang.String r5 = r11.getDisplayName()     // Catch:{ RemoteException -> 0x02a8 }
            java.lang.String r7 = r11.getNumber()     // Catch:{ RemoteException -> 0x02a8 }
            r1 = r20
            r2 = r16
            r6 = r19
            java.lang.Object[] r1 = r1.createSharedSketchRow(r2, r3, r4, r5, r6, r7)     // Catch:{ RemoteException -> 0x02a8 }
            r10.addRow(r1)     // Catch:{ RemoteException -> 0x02a8 }
            r16 = 13
            long r1 = com.sec.ims.options.Capabilities.FEATURE_ENRICHED_POST_CALL     // Catch:{ RemoteException -> 0x02a8 }
            boolean r3 = r11.hasFeature(r1)     // Catch:{ RemoteException -> 0x02a8 }
            com.sec.ims.util.ImsUri r1 = r11.getUri()     // Catch:{ RemoteException -> 0x02a8 }
            java.lang.String r4 = r1.toString()     // Catch:{ RemoteException -> 0x02a8 }
            java.lang.String r5 = r11.getDisplayName()     // Catch:{ RemoteException -> 0x02a8 }
            java.lang.String r7 = r11.getNumber()     // Catch:{ RemoteException -> 0x02a8 }
            r1 = r20
            r2 = r18
            r6 = r19
            java.lang.Object[] r1 = r1.createPostCallRow(r2, r3, r4, r5, r6, r7)     // Catch:{ RemoteException -> 0x02a8 }
            r10.addRow(r1)     // Catch:{ RemoteException -> 0x02a8 }
            long r1 = com.sec.ims.options.Capabilities.FEATURE_PUBLIC_MSG     // Catch:{ RemoteException -> 0x02a8 }
            boolean r3 = r11.hasFeature(r1)     // Catch:{ RemoteException -> 0x02a8 }
            com.sec.ims.util.ImsUri r1 = r11.getUri()     // Catch:{ RemoteException -> 0x02a8 }
            java.lang.String r4 = r1.toString()     // Catch:{ RemoteException -> 0x02a8 }
            java.lang.String r5 = r11.getDisplayName()     // Catch:{ RemoteException -> 0x02a8 }
            r6 = 0
            java.lang.String r7 = r11.getNumber()     // Catch:{ RemoteException -> 0x02a8 }
            r1 = r20
            r2 = r16
            java.lang.Object[] r1 = r1.createPublicMsgRow(r2, r3, r4, r5, r6, r7)     // Catch:{ RemoteException -> 0x02a8 }
            r10.addRow(r1)     // Catch:{ RemoteException -> 0x02a8 }
            long r1 = com.sec.ims.options.Capabilities.FEATURE_CANCEL_MESSAGE     // Catch:{ RemoteException -> 0x02a8 }
            boolean r1 = r11.hasFeature(r1)     // Catch:{ RemoteException -> 0x02a8 }
            com.sec.ims.util.ImsUri r2 = r11.getUri()     // Catch:{ RemoteException -> 0x02a8 }
            java.lang.String r2 = r2.toString()     // Catch:{ RemoteException -> 0x02a8 }
            r3 = 14
            java.lang.Object[] r1 = r0.createCancelMessageRow(r3, r1, r2)     // Catch:{ RemoteException -> 0x02a8 }
            r10.addRow(r1)     // Catch:{ RemoteException -> 0x02a8 }
        L_0x02a2:
            int r7 = r17 + 1
            r11 = 1
            goto L_0x0057
        L_0x02a7:
            return r10
        L_0x02a8:
            r0 = move-exception
            r0.printStackTrace()
            r10.close()
            return r12
        L_0x02b0:
            java.lang.String r0 = "querySipUri: no valid uri to request"
            com.sec.internal.log.IMSLog.e(r9, r8, r0)
            return r10
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.options.CapabilityProvider.querySipUri(java.lang.String[], java.util.List, com.sec.ims.options.CapabilityRefreshType, int):android.database.Cursor");
    }

    /* access modifiers changed from: package-private */
    public Cursor queryIncallService(List<String> list, CapabilityRefreshType capabilityRefreshType, int i) {
        IMSLog.i(LOG_TAG, i, "queryIncallService");
        String decode = Uri.decode(list.get(list.size() - 1));
        MatrixCursor matrixCursor = new MatrixCursor(Projections.INCALL_PROJECTION);
        try {
            ImsUri normalizedUri = UriGeneratorFactory.getInstance().get(UriGenerator.URIServiceType.RCS_URI).getNormalizedUri(decode, true);
            Capabilities capabilities = this.mService.getCapabilities(normalizedUri, capabilityRefreshType.ordinal(), i);
            if (capabilityRefreshType.equals(CapabilityRefreshType.ALWAYS_FORCE_REFRESH)) {
                waitForUpdate(normalizedUri);
                capabilities = getAsyncCapexResult(normalizedUri);
            }
            if (capabilities == null) {
                IMSLog.s(LOG_TAG, i, "queryIncallService: Capabilities not found for " + decode);
                this.mLastInCallUri = normalizedUri;
                return matrixCursor;
            }
            this.mLastInCallUri = capabilities.getUri();
            if (capabilities.hasFeature(Capabilities.FEATURE_ISH) || capabilities.hasFeature(Capabilities.FEATURE_VSH)) {
                Capabilities ownCapabilities = this.mService.getOwnCapabilities(i);
                if (ownCapabilities == null) {
                    IMSLog.i(LOG_TAG, i, "queryIncallService: own capex is null");
                    return matrixCursor;
                } else if (!ownCapabilities.isAvailable()) {
                    IMSLog.i(LOG_TAG, i, "queryIncallService: own capex is not available");
                    return matrixCursor;
                } else if (ownCapabilities.hasFeature(Capabilities.FEATURE_ISH) || ownCapabilities.hasFeature(Capabilities.FEATURE_VSH)) {
                    IMSLog.i(LOG_TAG, i, "queryIncallService: ready_ish = " + ready_ish + ", ready_vsh = " + ready_vsh);
                    if (this.mDataNetworkType == 3 && !ImsUtil.isWifiConnected(this.mContext)) {
                        ready_vsh = false;
                    }
                    boolean z = ready_ish && capabilities.hasFeature(Capabilities.FEATURE_ISH) && ownCapabilities.hasFeature(Capabilities.FEATURE_ISH);
                    boolean z2 = ready_vsh && capabilities.hasFeature(Capabilities.FEATURE_VSH) && ownCapabilities.hasFeature(Capabilities.FEATURE_VSH);
                    IMSLog.i(LOG_TAG, i, "queryIncallService: hasfeature_ish = " + z);
                    IMSLog.i(LOG_TAG, i, "queryIncallService: hasfeature_vsh = " + z2);
                    String imsUri = capabilities.getUri().toString();
                    matrixCursor.addRow(createShareVideoRow(0, z2, imsUri));
                    matrixCursor.addRow(createImageFileShareRow(1, z, imsUri));
                    matrixCursor.addRow(createImageCameraShareRow(2, z, imsUri));
                    return matrixCursor;
                } else {
                    IMSLog.i(LOG_TAG, i, "queryIncallService: No hasFeature for ish, vsh in own capex");
                    return matrixCursor;
                }
            } else {
                IMSLog.s(LOG_TAG, i, "queryIncallService: No hasFeature for ish, vsh " + decode);
                return matrixCursor;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x008d A[Catch:{ RemoteException -> 0x0239, all -> 0x0237 }] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public android.database.Cursor queryOwnCaps(int r19) {
        /*
            r18 = this;
            r0 = r18
            r1 = r19
            java.lang.String r2 = "queryOwnCaps"
            java.lang.String r3 = "CapabilityProvider"
            com.sec.internal.log.IMSLog.i(r3, r1, r2)
            android.database.MatrixCursor r8 = new android.database.MatrixCursor
            java.lang.String[] r2 = com.sec.internal.ims.servicemodules.options.CapabilityProvider.Projections.SERVICE_PROJECTION
            r8.<init>(r2)
            long r9 = android.os.Binder.clearCallingIdentity()
            com.sec.ims.options.ICapabilityService r2 = r0.mService     // Catch:{ RemoteException -> 0x0239 }
            com.sec.ims.options.Capabilities r11 = r2.getOwnCapabilities(r1)     // Catch:{ RemoteException -> 0x0239 }
            if (r11 != 0) goto L_0x0023
            android.os.Binder.restoreCallingIdentity(r9)
            return r8
        L_0x0023:
            com.sec.ims.util.ImsUri r2 = r11.getUri()     // Catch:{ RemoteException -> 0x0239 }
            java.lang.String r4 = ""
            if (r2 == 0) goto L_0x0035
            com.sec.ims.util.ImsUri r2 = r11.getUri()     // Catch:{ RemoteException -> 0x0239 }
            java.lang.String r2 = r2.toString()     // Catch:{ RemoteException -> 0x0239 }
            r12 = r2
            goto L_0x0036
        L_0x0035:
            r12 = r4
        L_0x0036:
            boolean r2 = r11.isAvailable()     // Catch:{ RemoteException -> 0x0239 }
            if (r2 == 0) goto L_0x003e
            r13 = r4
            goto L_0x0041
        L_0x003e:
            java.lang.String r2 = "local_offline;"
            r13 = r2
        L_0x0041:
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ RemoteException -> 0x0239 }
            r2.<init>()     // Catch:{ RemoteException -> 0x0239 }
            java.lang.String r4 = "queryOwnCaps: RCS additionalInfo = "
            r2.append(r4)     // Catch:{ RemoteException -> 0x0239 }
            r2.append(r13)     // Catch:{ RemoteException -> 0x0239 }
            java.lang.String r2 = r2.toString()     // Catch:{ RemoteException -> 0x0239 }
            com.sec.internal.log.IMSLog.i(r3, r1, r2)     // Catch:{ RemoteException -> 0x0239 }
            r14 = 1
            int r1 = com.sec.ims.options.Capabilities.FEATURE_CHAT_CPM     // Catch:{ RemoteException -> 0x0239 }
            boolean r1 = r11.hasFeature(r1)     // Catch:{ RemoteException -> 0x0239 }
            r15 = 0
            r16 = 1
            if (r1 != 0) goto L_0x006d
            int r1 = com.sec.ims.options.Capabilities.FEATURE_CHAT_SIMPLE_IM     // Catch:{ RemoteException -> 0x0239 }
            boolean r1 = r11.hasFeature(r1)     // Catch:{ RemoteException -> 0x0239 }
            if (r1 == 0) goto L_0x006b
            goto L_0x006d
        L_0x006b:
            r3 = r15
            goto L_0x006f
        L_0x006d:
            r3 = r16
        L_0x006f:
            java.lang.String r5 = r11.getDisplayName()     // Catch:{ RemoteException -> 0x0239 }
            java.lang.String r7 = r11.getNumber()     // Catch:{ RemoteException -> 0x0239 }
            r2 = 0
            r1 = r18
            r4 = r12
            r6 = r13
            java.lang.Object[] r1 = r1.createImRow(r2, r3, r4, r5, r6, r7)     // Catch:{ RemoteException -> 0x0239 }
            r8.addRow(r1)     // Catch:{ RemoteException -> 0x0239 }
            r17 = 2
            int r1 = com.sec.ims.options.Capabilities.FEATURE_FT     // Catch:{ RemoteException -> 0x0239 }
            boolean r1 = r11.hasFeature(r1)     // Catch:{ RemoteException -> 0x0239 }
            if (r1 != 0) goto L_0x0098
            int r1 = com.sec.ims.options.Capabilities.FEATURE_FT_STORE     // Catch:{ RemoteException -> 0x0239 }
            boolean r1 = r11.hasFeature(r1)     // Catch:{ RemoteException -> 0x0239 }
            if (r1 == 0) goto L_0x0096
            goto L_0x0098
        L_0x0096:
            r3 = r15
            goto L_0x009a
        L_0x0098:
            r3 = r16
        L_0x009a:
            java.lang.String r5 = r11.getDisplayName()     // Catch:{ RemoteException -> 0x0239 }
            java.lang.String r7 = r11.getNumber()     // Catch:{ RemoteException -> 0x0239 }
            r1 = r18
            r2 = r14
            r4 = r12
            r6 = r13
            java.lang.Object[] r1 = r1.createFtRow(r2, r3, r4, r5, r6, r7)     // Catch:{ RemoteException -> 0x0239 }
            r8.addRow(r1)     // Catch:{ RemoteException -> 0x0239 }
            r14 = 3
            int r1 = com.sec.ims.options.Capabilities.FEATURE_FT_HTTP     // Catch:{ RemoteException -> 0x0239 }
            boolean r3 = r11.hasFeature(r1)     // Catch:{ RemoteException -> 0x0239 }
            java.lang.String r5 = r11.getDisplayName()     // Catch:{ RemoteException -> 0x0239 }
            java.lang.String r7 = r11.getNumber()     // Catch:{ RemoteException -> 0x0239 }
            r1 = r18
            r2 = r17
            r4 = r12
            r6 = r13
            java.lang.Object[] r1 = r1.createFtHttpRow(r2, r3, r4, r5, r6, r7)     // Catch:{ RemoteException -> 0x0239 }
            r8.addRow(r1)     // Catch:{ RemoteException -> 0x0239 }
            r15 = 4
            int r1 = com.sec.ims.options.Capabilities.FEATURE_FT_HTTP     // Catch:{ RemoteException -> 0x0239 }
            boolean r3 = r11.hasFeature(r1)     // Catch:{ RemoteException -> 0x0239 }
            java.lang.String r5 = r11.getDisplayName()     // Catch:{ RemoteException -> 0x0239 }
            java.lang.String r7 = r11.getNumber()     // Catch:{ RemoteException -> 0x0239 }
            r1 = r18
            r2 = r14
            r4 = r12
            r6 = r13
            java.lang.Object[] r1 = r1.createFtInGroupChatRow(r2, r3, r4, r5, r6, r7)     // Catch:{ RemoteException -> 0x0239 }
            r8.addRow(r1)     // Catch:{ RemoteException -> 0x0239 }
            int r1 = com.sec.ims.options.Capabilities.FEATURE_STANDALONE_MSG     // Catch:{ RemoteException -> 0x0239 }
            boolean r3 = r11.hasFeature(r1)     // Catch:{ RemoteException -> 0x0239 }
            java.lang.String r5 = r11.getDisplayName()     // Catch:{ RemoteException -> 0x0239 }
            java.lang.String r7 = r11.getNumber()     // Catch:{ RemoteException -> 0x0239 }
            r1 = r18
            r2 = r15
            r4 = r12
            r6 = r13
            java.lang.Object[] r1 = r1.createSlmRow(r2, r3, r4, r5, r6, r7)     // Catch:{ RemoteException -> 0x0239 }
            r8.addRow(r1)     // Catch:{ RemoteException -> 0x0239 }
            int r1 = com.sec.ims.options.Capabilities.FEATURE_GEOLOCATION_PUSH     // Catch:{ RemoteException -> 0x0239 }
            boolean r1 = r11.hasFeature(r1)     // Catch:{ RemoteException -> 0x0239 }
            r2 = 5
            java.lang.Object[] r1 = r0.createGeolocationPushRow(r2, r1, r12)     // Catch:{ RemoteException -> 0x0239 }
            r8.addRow(r1)     // Catch:{ RemoteException -> 0x0239 }
            int r1 = com.sec.ims.options.Capabilities.FEATURE_GEO_VIA_SMS     // Catch:{ RemoteException -> 0x0239 }
            boolean r1 = r11.hasFeature(r1)     // Catch:{ RemoteException -> 0x0239 }
            r2 = 6
            java.lang.Object[] r1 = r0.createGeoPushViaSMSRow(r2, r1, r12)     // Catch:{ RemoteException -> 0x0239 }
            r8.addRow(r1)     // Catch:{ RemoteException -> 0x0239 }
            long r1 = com.sec.ims.options.Capabilities.FEATURE_CHATBOT_CHAT_SESSION     // Catch:{ RemoteException -> 0x0239 }
            boolean r1 = r11.hasFeature(r1)     // Catch:{ RemoteException -> 0x0239 }
            r2 = 7
            java.lang.Object[] r1 = r0.createChatbotChatSessionRow(r2, r1, r12)     // Catch:{ RemoteException -> 0x0239 }
            r8.addRow(r1)     // Catch:{ RemoteException -> 0x0239 }
            long r1 = com.sec.ims.options.Capabilities.FEATURE_CHATBOT_STANDALONE_MSG     // Catch:{ RemoteException -> 0x0239 }
            boolean r1 = r11.hasFeature(r1)     // Catch:{ RemoteException -> 0x0239 }
            r2 = 8
            java.lang.Object[] r1 = r0.createChatbotSlmRow(r2, r1, r12)     // Catch:{ RemoteException -> 0x0239 }
            r8.addRow(r1)     // Catch:{ RemoteException -> 0x0239 }
            r2 = 10
            long r3 = com.sec.ims.options.Capabilities.FEATURE_CHATBOT_EXTENDED_MSG     // Catch:{ RemoteException -> 0x0239 }
            boolean r1 = r11.hasFeature(r3)     // Catch:{ RemoteException -> 0x0239 }
            r3 = 9
            java.lang.Object[] r1 = r0.createExtendedbotRow(r3, r1, r12)     // Catch:{ RemoteException -> 0x0239 }
            r8.addRow(r1)     // Catch:{ RemoteException -> 0x0239 }
            int r1 = com.sec.ims.options.Capabilities.FEATURE_SF_GROUP_CHAT     // Catch:{ RemoteException -> 0x0239 }
            boolean r3 = r11.hasFeature(r1)     // Catch:{ RemoteException -> 0x0239 }
            java.lang.String r5 = r11.getDisplayName()     // Catch:{ RemoteException -> 0x0239 }
            java.lang.String r7 = r11.getNumber()     // Catch:{ RemoteException -> 0x0239 }
            r1 = r18
            r4 = r12
            r6 = r13
            java.lang.Object[] r1 = r1.createFtSfGroupChatRow(r2, r3, r4, r5, r6, r7)     // Catch:{ RemoteException -> 0x0239 }
            r8.addRow(r1)     // Catch:{ RemoteException -> 0x0239 }
            r2 = 12
            int r1 = com.sec.ims.options.Capabilities.FEATURE_INTEGRATED_MSG     // Catch:{ RemoteException -> 0x0239 }
            boolean r1 = r11.hasFeature(r1)     // Catch:{ RemoteException -> 0x0239 }
            r3 = 11
            java.lang.Object[] r1 = r0.createIntegratedMessageRow(r3, r1, r12)     // Catch:{ RemoteException -> 0x0239 }
            r8.addRow(r1)     // Catch:{ RemoteException -> 0x0239 }
            r14 = 13
            long r3 = com.sec.ims.options.Capabilities.FEATURE_ENRICHED_CALL_COMPOSER     // Catch:{ RemoteException -> 0x0239 }
            boolean r3 = r11.hasFeature(r3)     // Catch:{ RemoteException -> 0x0239 }
            java.lang.String r5 = r11.getDisplayName()     // Catch:{ RemoteException -> 0x0239 }
            java.lang.String r7 = r11.getNumber()     // Catch:{ RemoteException -> 0x0239 }
            r1 = r18
            r4 = r12
            r6 = r13
            java.lang.Object[] r1 = r1.createCallComposerRow(r2, r3, r4, r5, r6, r7)     // Catch:{ RemoteException -> 0x0239 }
            r8.addRow(r1)     // Catch:{ RemoteException -> 0x0239 }
            r15 = 14
            long r1 = com.sec.ims.options.Capabilities.FEATURE_ENRICHED_SHARED_MAP     // Catch:{ RemoteException -> 0x0239 }
            boolean r3 = r11.hasFeature(r1)     // Catch:{ RemoteException -> 0x0239 }
            java.lang.String r5 = r11.getDisplayName()     // Catch:{ RemoteException -> 0x0239 }
            java.lang.String r7 = r11.getNumber()     // Catch:{ RemoteException -> 0x0239 }
            r1 = r18
            r2 = r14
            r4 = r12
            r6 = r13
            java.lang.Object[] r1 = r1.createSharedMapRow(r2, r3, r4, r5, r6, r7)     // Catch:{ RemoteException -> 0x0239 }
            r8.addRow(r1)     // Catch:{ RemoteException -> 0x0239 }
            r14 = 15
            long r1 = com.sec.ims.options.Capabilities.FEATURE_ENRICHED_SHARED_SKETCH     // Catch:{ RemoteException -> 0x0239 }
            boolean r3 = r11.hasFeature(r1)     // Catch:{ RemoteException -> 0x0239 }
            java.lang.String r5 = r11.getDisplayName()     // Catch:{ RemoteException -> 0x0239 }
            java.lang.String r7 = r11.getNumber()     // Catch:{ RemoteException -> 0x0239 }
            r1 = r18
            r2 = r15
            r4 = r12
            r6 = r13
            java.lang.Object[] r1 = r1.createSharedSketchRow(r2, r3, r4, r5, r6, r7)     // Catch:{ RemoteException -> 0x0239 }
            r8.addRow(r1)     // Catch:{ RemoteException -> 0x0239 }
            r15 = 16
            long r1 = com.sec.ims.options.Capabilities.FEATURE_ENRICHED_POST_CALL     // Catch:{ RemoteException -> 0x0239 }
            boolean r3 = r11.hasFeature(r1)     // Catch:{ RemoteException -> 0x0239 }
            java.lang.String r5 = r11.getDisplayName()     // Catch:{ RemoteException -> 0x0239 }
            java.lang.String r7 = r11.getNumber()     // Catch:{ RemoteException -> 0x0239 }
            r1 = r18
            r2 = r14
            r4 = r12
            r6 = r13
            java.lang.Object[] r1 = r1.createPostCallRow(r2, r3, r4, r5, r6, r7)     // Catch:{ RemoteException -> 0x0239 }
            r8.addRow(r1)     // Catch:{ RemoteException -> 0x0239 }
            r14 = 17
            int r1 = com.sec.ims.options.Capabilities.FEATURE_STICKER     // Catch:{ RemoteException -> 0x0239 }
            boolean r3 = r11.hasFeature(r1)     // Catch:{ RemoteException -> 0x0239 }
            java.lang.String r5 = r11.getDisplayName()     // Catch:{ RemoteException -> 0x0239 }
            java.lang.String r7 = r11.getNumber()     // Catch:{ RemoteException -> 0x0239 }
            r1 = r18
            r2 = r15
            r4 = r12
            r6 = r13
            java.lang.Object[] r1 = r1.createStickerRow(r2, r3, r4, r5, r6, r7)     // Catch:{ RemoteException -> 0x0239 }
            r8.addRow(r1)     // Catch:{ RemoteException -> 0x0239 }
            long r1 = com.sec.ims.options.Capabilities.FEATURE_PUBLIC_MSG     // Catch:{ RemoteException -> 0x0239 }
            boolean r3 = r11.hasFeature(r1)     // Catch:{ RemoteException -> 0x0239 }
            java.lang.String r5 = r11.getDisplayName()     // Catch:{ RemoteException -> 0x0239 }
            java.lang.String r7 = r11.getNumber()     // Catch:{ RemoteException -> 0x0239 }
            r1 = r18
            r2 = r14
            r4 = r12
            r6 = r13
            java.lang.Object[] r1 = r1.createPublicMsgRow(r2, r3, r4, r5, r6, r7)     // Catch:{ RemoteException -> 0x0239 }
            r8.addRow(r1)     // Catch:{ RemoteException -> 0x0239 }
            long r1 = com.sec.ims.options.Capabilities.FEATURE_CANCEL_MESSAGE     // Catch:{ RemoteException -> 0x0239 }
            boolean r1 = r11.hasFeature(r1)     // Catch:{ RemoteException -> 0x0239 }
            r2 = 18
            java.lang.Object[] r1 = r0.createCancelMessageRow(r2, r1, r12)     // Catch:{ RemoteException -> 0x0239 }
            r8.addRow(r1)     // Catch:{ RemoteException -> 0x0239 }
            long r1 = com.sec.ims.options.Capabilities.FEATURE_PLUG_IN     // Catch:{ RemoteException -> 0x0239 }
            boolean r1 = r11.hasFeature(r1)     // Catch:{ RemoteException -> 0x0239 }
            r2 = 19
            java.lang.Object[] r0 = r0.createPlugInRow(r2, r1, r12)     // Catch:{ RemoteException -> 0x0239 }
            r8.addRow(r0)     // Catch:{ RemoteException -> 0x0239 }
            goto L_0x023d
        L_0x0237:
            r0 = move-exception
            goto L_0x0241
        L_0x0239:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0237 }
        L_0x023d:
            android.os.Binder.restoreCallingIdentity(r9)
            return r8
        L_0x0241:
            android.os.Binder.restoreCallingIdentity(r9)
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.options.CapabilityProvider.queryOwnCaps(int):android.database.Cursor");
    }

    /* access modifiers changed from: package-private */
    public Cursor queryRcsEnabledStatic(int i) {
        boolean z;
        boolean z2;
        boolean z3;
        boolean z4;
        List<ImsProfile> profileList;
        IMSLog.i(LOG_TAG, i, "queryRcsEnabledStatic");
        MatrixCursor matrixCursor = new MatrixCursor(new String[]{ImsConstants.CscParserConstants.ENABLE_RCS, ImsConstants.CscParserConstants.ENABLE_RCS_CHAT_SERVICE});
        boolean isSimMobilityFeatureEnabled = SimUtil.isSimMobilityFeatureEnabled();
        String str = CloudMessageProviderContract.JsonData.TRUE;
        if (isSimMobilityFeatureEnabled) {
            if (ImsUtil.isSimMobilityActivatedForAmRcs(this.mContext, i)) {
                z3 = true;
            } else {
                if (ImsUtil.isSimMobilityActivatedForRcs(i) && (profileList = ImsProfileLoaderInternal.getProfileList(this.mContext, i)) != null && profileList.size() > 0) {
                    for (ImsProfile next : profileList) {
                        if (next != null && next.getEnableRcs()) {
                            z3 = next.getEnableRcsChat();
                            z4 = next.getEnableRcs();
                            break;
                        }
                    }
                }
                z3 = false;
            }
            z4 = z3;
            IMSLog.i(LOG_TAG, i, "queryRcsEnabledStatic: SimMobility, isEnableRcs = " + z4 + ", isEnableRcsChat = " + z3);
            String[] strArr = new String[2];
            strArr[0] = z4 ? str : ConfigConstants.VALUE.INFO_COMPLETED;
            if (!z3) {
                str = ConfigConstants.VALUE.INFO_COMPLETED;
            }
            strArr[1] = str;
            matrixCursor.addRow(strArr);
            return matrixCursor;
        }
        ISimManager simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(i);
        ContentValues cscImsSetting = CscParser.getCscImsSetting(simManagerFromSimSlot != null ? simManagerFromSimSlot.getNetworkNames() : null, i);
        if (cscImsSetting == null || cscImsSetting.size() <= 0) {
            IMSLog.i(LOG_TAG, i, "queryRcsEnabledStatic: cscSettings is null, isEnableRcs = false, isEnableRcsChat = false");
            z = false;
            z2 = false;
        } else {
            z2 = CollectionUtils.getBooleanValue(cscImsSetting, ImsConstants.CscParserConstants.ENABLE_RCS, false);
            z = CollectionUtils.getBooleanValue(cscImsSetting, ImsConstants.CscParserConstants.ENABLE_RCS_CHAT_SERVICE, false);
            IMSLog.i(LOG_TAG, i, "queryRcsEnabledStatic: Customer, isEnableRcs = " + z2 + ", isEnableRcsChat = " + z);
        }
        String[] strArr2 = new String[2];
        strArr2[0] = z2 ? str : ConfigConstants.VALUE.INFO_COMPLETED;
        if (!z) {
            str = ConfigConstants.VALUE.INFO_COMPLETED;
        }
        strArr2[1] = str;
        matrixCursor.addRow(strArr2);
        return matrixCursor;
    }

    /* access modifiers changed from: package-private */
    public Cursor queryOperatorRcsVersion(int i) {
        IMSLog.i(LOG_TAG, i, "queryOperatorRcsVersion");
        MatrixCursor matrixCursor = new MatrixCursor(new String[]{"OperatorRcsVersion"});
        matrixCursor.addRow(new String[]{ImsRegistry.getString(i, GlobalSettingsConstants.RCS.RCS_PHASE_VERSION, "")});
        return matrixCursor;
    }

    /* access modifiers changed from: package-private */
    public Cursor queryRcsBigData(List<String> list, int i) {
        String str;
        IMSLog.i(LOG_TAG, i, "queryRcsBigData");
        MatrixCursor matrixCursor = new MatrixCursor(new String[]{"RemoteRcsStatus", "RemoteLegacyLatching", "PhoneNumber"});
        String decode = Uri.decode(list.get(list.size() - 1));
        try {
            Capabilities capabilities = this.mService.getCapabilities(UriGeneratorFactory.getInstance().get(UriGenerator.URIServiceType.RCS_URI).getNormalizedUri(decode, true), CapabilityRefreshType.DISABLED.ordinal(), i);
            if (capabilities == null) {
                IMSLog.s(LOG_TAG, "queryRcsBigData: Capabilities not found for " + decode);
                return matrixCursor;
            }
            if (capabilities.hasNoRcsFeatures()) {
                str = null;
            } else {
                long availableFeatures = capabilities.getAvailableFeatures();
                str = (availableFeatures > ((long) Capabilities.FEATURE_OFFLINE_RCS_USER) ? 1 : (availableFeatures == ((long) Capabilities.FEATURE_OFFLINE_RCS_USER) ? 0 : -1)) != 0 && (availableFeatures > ((long) Capabilities.FEATURE_NON_RCS_USER) ? 1 : (availableFeatures == ((long) Capabilities.FEATURE_NON_RCS_USER) ? 0 : -1)) != 0 && (availableFeatures > ((long) Capabilities.FEATURE_NOT_UPDATED) ? 1 : (availableFeatures == ((long) Capabilities.FEATURE_NOT_UPDATED) ? 0 : -1)) != 0 ? ADDITIONAL_INFO_REMOTE_ONLINE : ADDITIONAL_INFO_REMOTE_OFFLINE;
            }
            boolean legacyLatching = capabilities.getLegacyLatching();
            IMSLog.s(LOG_TAG, i, "queryRcsBigData: remoteRcsStatus = " + str + ", remoteLegacyLatching = " + legacyLatching + ", phoneNumber = " + decode);
            String[] strArr = new String[3];
            strArr[0] = str;
            strArr[1] = legacyLatching ? CloudMessageProviderContract.JsonData.TRUE : ConfigConstants.VALUE.INFO_COMPLETED;
            strArr[2] = decode;
            matrixCursor.addRow(strArr);
            return matrixCursor;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /* access modifiers changed from: package-private */
    public ArrayList<ImsUri> getImsUriListFromQuery(String str) {
        if (str == null) {
            Log.e(LOG_TAG, "getImsUriListFromQuery: null uris");
            return null;
        }
        ArrayList<ImsUri> arrayList = new ArrayList<>();
        String[] split = str.split("\\s*,\\s*");
        if (split != null) {
            for (String parse : split) {
                ImsUri parse2 = ImsUri.parse(parse);
                if (!(parse2 == null || parse2.toString().length() == 0)) {
                    arrayList.add(parse2);
                }
            }
        }
        return arrayList;
    }

    /* access modifiers changed from: package-private */
    public CapabilityRefreshType getRequeryStrategyId(String str) {
        if ("disable_requery".equals(str)) {
            return CapabilityRefreshType.DISABLED;
        }
        if ("force_requery".equals(str)) {
            return CapabilityRefreshType.ALWAYS_FORCE_REFRESH;
        }
        if ("force_requery_uce".equals(str)) {
            return CapabilityRefreshType.FORCE_REFRESH_UCE;
        }
        if ("force_requery_sync".equals(str)) {
            return CapabilityRefreshType.FORCE_REFRESH_SYNC;
        }
        if ("msg_conditional_requery".equals(str)) {
            return CapabilityRefreshType.ONLY_IF_NOT_FRESH_IN_MSG_CTX;
        }
        return CapabilityRefreshType.ONLY_IF_NOT_FRESH;
    }

    private Object[] createImRow(int i, boolean z, String str, String str2, String str3, String str4) {
        return new Object[]{Integer.valueOf(i), Capabilities.FEATURE_TAG_CHAT, Integer.valueOf(z ? 1 : 0), Intents.VIEW_CHAT_INTENT_NAME, Intents.INTENT_CATEGORY, str, str2, str3, str4};
    }

    private Object[] createFtRow(int i, boolean z, String str, String str2, String str3, String str4) {
        return new Object[]{Integer.valueOf(i), Capabilities.FEATURE_TAG_FT, Integer.valueOf(z ? 1 : 0), Intents.FILE_TRANSFER_INTENT_NAME, Intents.INTENT_CATEGORY, str, str2, str3, str4};
    }

    private Object[] createFtHttpRow(int i, boolean z, String str, String str2, String str3, String str4) {
        return new Object[]{Integer.valueOf(i), Capabilities.FEATURE_TAG_FT_HTTP, Integer.valueOf(z ? 1 : 0), Intents.FILE_TRANSFER_INTENT_NAME, Intents.INTENT_CATEGORY, str, str2, str3, str4};
    }

    private Object[] createFtInGroupChatRow(int i, boolean z, String str, String str2, String str3, String str4) {
        return new Object[]{Integer.valueOf(i), "ft-in-group-chat", Integer.valueOf(z ? 1 : 0), Intents.FILE_TRANSFER_INTENT_NAME, Intents.INTENT_CATEGORY, str, str2, str3, str4};
    }

    private Object[] createFtSfGroupChatRow(int i, boolean z, String str, String str2, String str3, String str4) {
        return new Object[]{Integer.valueOf(i), Capabilities.FEATURE_TAG_SF_GROUP_CHAT, Integer.valueOf(z ? 1 : 0), Intents.FILE_TRANSFER_INTENT_NAME, Intents.INTENT_CATEGORY, str, str2, str3, str4};
    }

    private Object[] createSlmRow(int i, boolean z, String str, String str2, String str3, String str4) {
        return new Object[]{Integer.valueOf(i), Capabilities.FEATURE_TAG_STANDALONE_MSG, Integer.valueOf(z ? 1 : 0), Intents.VIEW_CHAT_INTENT_NAME, Intents.INTENT_CATEGORY, str, str2, str3, str4};
    }

    private Object[] createGeolocationPushRow(int i, boolean z, String str) {
        return new Object[]{Integer.valueOf(i), Capabilities.FEATURE_TAG_GEOLOCATION_PUSH, Integer.valueOf(z ? 1 : 0), null, null, str, null, null, null};
    }

    private Object[] createGeoPushViaSMSRow(int i, boolean z, String str) {
        return new Object[]{Integer.valueOf(i), Integer.valueOf(Capabilities.FEATURE_GEO_VIA_SMS), Integer.valueOf(z ? 1 : 0), null, null, str, null, null, null};
    }

    private Object[] createCancelMessageRow(int i, boolean z, String str) {
        return new Object[]{Integer.valueOf(i), Capabilities.FEATURE_TAG_CANCEL_MESSAGE, Integer.valueOf(z ? 1 : 0), null, null, str, null, null, null};
    }

    private Object[] createIntegratedMessageRow(int i, boolean z, String str) {
        return new Object[]{Integer.valueOf(i), Capabilities.FEATURE_TAG_INTEGRATED_MSG, Integer.valueOf(z ? 1 : 0), null, null, str, null, null, null};
    }

    private Object[] createStickerRow(int i, boolean z, String str, String str2, String str3, String str4) {
        return new Object[]{Integer.valueOf(i), Capabilities.FEATURE_TAG_STICKER, Integer.valueOf(z ? 1 : 0), Intents.VIEW_CHAT_INTENT_NAME, Intents.INTENT_CATEGORY, str, str2, str3, str4};
    }

    private Object[] createPublicMsgRow(int i, boolean z, String str, String str2, String str3, String str4) {
        return new Object[]{Integer.valueOf(i), Capabilities.FEATURE_TAG_PUBLIC_MSG, Integer.valueOf(z ? 1 : 0), Intents.VIEW_CHAT_INTENT_NAME, Intents.INTENT_CATEGORY, str, str2, str3, str4};
    }

    private Object[] createShareVideoRow(int i, boolean z, String str) {
        return new Object[]{Integer.valueOf(i), Capabilities.FEATURE_TAG_VSH, Integer.valueOf(z ? 1 : 0), Intents.LIVE_VIDEO_SHARE_INTENT_NAME, Intents.INTENT_CATEGORY, str, "Live video"};
    }

    private Object[] createImageFileShareRow(int i, boolean z, String str) {
        return new Object[]{Integer.valueOf(i), Capabilities.FEATURE_TAG_ISH, Integer.valueOf(z ? 1 : 0), Intents.IMAGE_FILE_SHARE_INTENT_NAME, Intents.INTENT_CATEGORY, str, "Picture"};
    }

    private Object[] createImageCameraShareRow(int i, boolean z, String str) {
        return new Object[]{Integer.valueOf(i), Capabilities.FEATURE_TAG_ISH, Integer.valueOf(z ? 1 : 0), Intents.IMAGE_CAMERA_SHARE_INTENT_NAME, Intents.INTENT_CATEGORY, str, "Take a picture"};
    }

    private Object[] createCallComposerRow(int i, boolean z, String str, String str2, String str3, String str4) {
        Log.i(LOG_TAG, "has call composer feature: " + z);
        return new Object[]{Integer.valueOf(i), SipMsg.FEATURE_TAG_ENRICHED_CALL_COMPOSER, Integer.valueOf(z ? 1 : 0), null, null, str, str2, str3, str4};
    }

    private Object[] createSharedMapRow(int i, boolean z, String str, String str2, String str3, String str4) {
        Log.i(LOG_TAG, "has shared map feature: " + z);
        return new Object[]{Integer.valueOf(i), SipMsg.FEATURE_TAG_ENRICHED_SHARED_MAP, Integer.valueOf(z ? 1 : 0), null, null, str, str2, str3, str4};
    }

    private Object[] createSharedSketchRow(int i, boolean z, String str, String str2, String str3, String str4) {
        Log.i(LOG_TAG, "has shared sketch feature: " + z);
        return new Object[]{Integer.valueOf(i), SipMsg.FEATURE_TAG_ENRICHED_SHARED_SKETCH, Integer.valueOf(z ? 1 : 0), null, null, str, str2, str3, str4};
    }

    private Object[] createPostCallRow(int i, boolean z, String str, String str2, String str3, String str4) {
        Log.i(LOG_TAG, "has post call feature: " + z);
        return new Object[]{Integer.valueOf(i), SipMsg.FEATURE_TAG_ENRICHED_POST_CALL, Integer.valueOf(z ? 1 : 0), null, null, str, str2, str3, str4};
    }

    private Object[] createChatbotChatSessionRow(int i, boolean z, String str) {
        return new Object[]{Integer.valueOf(i), SipMsg.FEATURE_TAG_CHATBOT_COMMUNICATION_SESSION, Integer.valueOf(z ? 1 : 0), null, null, str, null, null, null};
    }

    private Object[] createChatbotSlmRow(int i, boolean z, String str) {
        return new Object[]{Integer.valueOf(i), SipMsg.FEATURE_TAG_CHATBOT_COMMUNICATION_STAND_ALONE, Integer.valueOf(z ? 1 : 0), null, null, str, null, null, null};
    }

    private Object[] createExtendedbotRow(int i, boolean z, String str) {
        return new Object[]{Integer.valueOf(i), "+g.3gpp.iari-ref=\"urn%3Aurn-7%3A3gpp-application.ims.iari.rcs.xbotmessage\"", Integer.valueOf(z ? 1 : 0), null, null, str, null, null, null};
    }

    private Object[] createPlugInRow(int i, boolean z, String str) {
        return new Object[]{Integer.valueOf(i), "+g.3gpp.iari-ref=\"urn%3Aurn-7%3A3gpp-application.ims.iari.rcs.plugin\"", Integer.valueOf(z ? 1 : 0), null, null, str, null, null, null};
    }

    static class Projections {
        static final String[] FEATURE_TAG_PROJECTION = {Columns.SERVICE_INDICATOR};
        static final String[] INCALL_PROJECTION = {"_id", Columns.SERVICE_INDICATOR, Columns.IS_ENABLED, Columns.INTENT_NAME, Columns.INTENT_CATEGORY, "sip_uri", "service_name"};
        static final String[] SERVICE_PROJECTION = {"_id", Columns.SERVICE_INDICATOR, Columns.IS_ENABLED, Columns.INTENT_NAME, Columns.INTENT_CATEGORY, "sip_uri", Columns.DISPLAYNAME, Columns.ADDITIONAL_INFO, "number"};

        Projections() {
        }
    }

    private class TelephonyCallbackForCapabilityProvider extends TelephonyCallback implements TelephonyCallback.DataConnectionStateListener {
        private TelephonyCallbackForCapabilityProvider() {
        }

        public void onDataConnectionStateChanged(int i, int i2) {
            Log.i(CapabilityProvider.LOG_TAG, "onDataConnectionStateChanged(): state [" + i + "] networkType [" + TelephonyManagerExt.getNetworkTypeName(i2) + "]");
            CapabilityProvider.this.mDataNetworkType = i2;
        }
    }

    /* access modifiers changed from: private */
    public void notifyCapabilityChange(ImsUri imsUri) {
        Log.i(LOG_TAG, "notifyCapabilityChange");
        IMSLog.s(LOG_TAG, "notifyCapabilityChange: uri " + imsUri);
        ContentResolver contentResolver = getContext().getContentResolver();
        contentResolver.notifyChange(Uri.parse("content://com.samsung.rcs.serviceprovider/sip/" + imsUri.toString()), (ContentObserver) null);
    }

    /* access modifiers changed from: private */
    public void notifyOwnServicesChange() {
        Log.i(LOG_TAG, "notifyOwnServicesChange");
        getContext().getContentResolver().notifyChange(Uri.parse("content://com.samsung.rcs.serviceprovider/own"), (ContentObserver) null);
    }

    /* access modifiers changed from: private */
    public void notifyInCallServicesChange() {
        Log.i(LOG_TAG, "notifyInCallServicesChange");
        getContext().getContentResolver().notifyChange(Uri.parse("content://com.samsung.rcs.serviceprovider/incall"), (ContentObserver) null);
    }

    public String getType(Uri uri) {
        throw new UnsupportedOperationException("Operation not supported for uri:".concat(uri.toString()));
    }

    public Uri insert(Uri uri, ContentValues contentValues) {
        throw new UnsupportedOperationException("Operation not supported for uri:".concat(uri.toString()));
    }

    public int delete(Uri uri, String str, String[] strArr) {
        throw new UnsupportedOperationException("Operation not supported for uri:".concat(uri.toString()));
    }

    public int update(Uri uri, ContentValues contentValues, String str, String[] strArr) {
        throw new UnsupportedOperationException("Operation not supported for uri:".concat(uri.toString()));
    }

    protected class ShareServiceBroadcastReceiver extends BroadcastReceiver {
        protected ShareServiceBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            IMSLog.s(CapabilityProvider.LOG_TAG, "ShareServiceBroadcastReceiver: action = " + action);
            if (action.equals(IshIntents.IshNotificationIntent.NOTIFICATION_CSH_SERVICE_NOT_READY) || action.equals(VshIntents.VshNotificationIntent.NOTIFICATION_CSH_SERVICE_NOT_READY)) {
                CapabilityProvider.ready_ish = false;
                CapabilityProvider.ready_vsh = false;
                CapabilityProvider.this.notifyInCallServicesChange();
            }
        }
    }

    /* access modifiers changed from: private */
    public static IntentFilter createIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addCategory(IshIntents.IshNotificationIntent.CATEGORY_NOTIFICATION);
        intentFilter.addAction(IshIntents.IshNotificationIntent.NOTIFICATION_CSH_SERVICE_NOT_READY);
        intentFilter.addCategory(VshIntents.VshNotificationIntent.CATEGORY_NOTIFICATION);
        intentFilter.addAction(VshIntents.VshNotificationIntent.NOTIFICATION_CSH_SERVICE_NOT_READY);
        return intentFilter;
    }
}
