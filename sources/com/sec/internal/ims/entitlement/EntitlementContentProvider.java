package com.sec.internal.ims.entitlement;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.text.TextUtils;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.entitilement.EntitlementNamespaces;
import com.sec.internal.constants.ims.entitilement.NSDSContractExt;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.entitlement.config.EntitlementConfigService;
import com.sec.internal.ims.entitlement.storagehelper.NSDSSharedPrefHelper;
import com.sec.internal.ims.entitlement.util.NSDSConfigHelper;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.interfaces.ims.aec.IAECModule;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.log.IMSLog;

public class EntitlementContentProvider extends ContentProvider {
    private static final String AKA_TOKEN = "aka_token";
    private static final String LOG_TAG = EntitlementContentProvider.class.getSimpleName();
    private static final String PROVIDER_NAME = "com.samsung.ims.entitlement.provider";
    private static final int RETRIEVE_AKA_TOKEN = 1;
    private static final int RETRIEVE_VOWIFI_ENTITLEMENT_REQUIRED = 2;
    private static final int RETRIEVE_VOWIFI_ENTITLEMENT_STATUS = 3;
    private static final String SLOT_ID = "slot";
    private static final String VOWIFI_ENTITLEMENT_REQUIRED = "vowifi_entitlement_required";
    private static final String VOWIFI_ENTITLEMENT_STATUS = "vowifi_entitlement_status";
    private static final UriMatcher sUriMatcher;
    private Context mContext = null;

    public int delete(Uri uri, String str, String[] strArr) {
        return 0;
    }

    public String getType(Uri uri) {
        return null;
    }

    public Uri insert(Uri uri, ContentValues contentValues) {
        return null;
    }

    public int update(Uri uri, ContentValues contentValues, String str, String[] strArr) {
        return 0;
    }

    static {
        UriMatcher uriMatcher = new UriMatcher(-1);
        sUriMatcher = uriMatcher;
        uriMatcher.addURI(PROVIDER_NAME, "aka_token", 1);
        uriMatcher.addURI(PROVIDER_NAME, VOWIFI_ENTITLEMENT_REQUIRED, 2);
        uriMatcher.addURI(PROVIDER_NAME, VOWIFI_ENTITLEMENT_STATUS, 3);
    }

    public boolean onCreate() {
        this.mContext = getContext();
        return true;
    }

    public Cursor query(Uri uri, String[] strArr, String str, String[] strArr2, String str2) {
        int parseInt = Integer.parseInt(uri.getQueryParameter("slot"));
        String str3 = LOG_TAG;
        IMSLog.s(str3, parseInt, "query uri:" + uri);
        int match = sUriMatcher.match(uri);
        if (match == 1) {
            return getAkaToken(parseInt);
        }
        if (match == 2) {
            return isVoWiFiEntitlementRequired(parseInt);
        }
        if (match != 3) {
            return null;
        }
        return getVoWiFiEntitlementStatus(parseInt);
    }

    private void activateSimDevice(int i) {
        String str = LOG_TAG;
        IMSLog.i(str, i, "activateSimDevice()");
        String configServer = NSDSConfigHelper.getConfigServer(i);
        if (!"Nsds".equalsIgnoreCase(configServer) && !"Nsdsconfig".equalsIgnoreCase(configServer)) {
            return;
        }
        if (SimUtil.getSimMno(i) == Mno.TMOUS) {
            IMSLog.i(str, i, "retrieve aka token for config");
            Intent intent = new Intent(this.mContext, EntitlementConfigService.class);
            intent.setAction(EntitlementNamespaces.EntitlementActions.ACTION_REFRESH_DEVICE_CONFIG);
            intent.putExtra(NSDSNamespaces.NSDSExtras.DEVICE_EVENT_TYPE, 19);
            intent.putExtra(NSDSNamespaces.NSDSExtras.SIM_SLOT_IDX, i);
            this.mContext.startService(intent);
            return;
        }
        IMSLog.i(str, i, "retrieve aka token for nsds");
        this.mContext.getContentResolver().update(Uri.withAppendedPath(NSDSContractExt.AUTHORITY_URI, "retrieve_aka_token"), new ContentValues(), (String) null, (String[]) null);
    }

    private Cursor getAkaToken(int i) {
        String str;
        String str2;
        IAECModule aECModule;
        String configServer = NSDSConfigHelper.getConfigServer(i);
        boolean supportEntitlementSlot = supportEntitlementSlot(i);
        String str3 = NSDSNamespaces.AkaAuthResultType.AKA_NOT_SUPPORTED;
        if (supportEntitlementSlot) {
            if ("Nsds".equalsIgnoreCase(configServer) || "Nsdsconfig".equalsIgnoreCase(configServer)) {
                ISimManager simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(i);
                if (simManagerFromSimSlot == null) {
                    str = "";
                } else {
                    str = simManagerFromSimSlot.getImsi();
                }
                String akaToken = NSDSSharedPrefHelper.getAkaToken(this.mContext, str);
                if (TextUtils.isEmpty(akaToken)) {
                    activateSimDevice(i);
                    str2 = "InProgress";
                } else {
                    str3 = akaToken;
                }
            } else if (("ts43".equalsIgnoreCase(configServer) || "nsds_eur".equalsIgnoreCase(configServer)) && (aECModule = ImsRegistry.getAECModule()) != null && aECModule.isEntitlementRequired(i)) {
                str2 = aECModule.getAkaToken(i);
            }
            str3 = str2;
        }
        String str4 = LOG_TAG;
        IMSLog.s(str4, i, "getAkaToken(): " + str3);
        MatrixCursor matrixCursor = new MatrixCursor(new String[]{"aka_token"});
        matrixCursor.addRow(new String[]{str3});
        return matrixCursor;
    }

    private boolean supportEntitlementSlot(int i) {
        String configServer = NSDSConfigHelper.getConfigServer(i);
        if (!TextUtils.isEmpty(configServer)) {
            String str = LOG_TAG;
            IMSLog.i(str, i, "supportEntitlementSlot: " + configServer);
            return true;
        }
        IMSLog.i(LOG_TAG, i, "supportEntitlementSlot : Not Support");
        return false;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:2:0x0017, code lost:
        r0 = com.sec.internal.ims.registry.ImsRegistry.getAECModule();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private android.database.Cursor isVoWiFiEntitlementRequired(int r6) {
        /*
            r5 = this;
            android.database.MatrixCursor r5 = new android.database.MatrixCursor
            java.lang.String r0 = "vowifi_entitlement_required"
            java.lang.String[] r0 = new java.lang.String[]{r0}
            r5.<init>(r0)
            com.sec.internal.constants.Mno r0 = com.sec.internal.helper.SimUtil.getSimMno(r6)
            boolean r0 = r0.isEur()
            r1 = 0
            if (r0 == 0) goto L_0x0022
            com.sec.internal.interfaces.ims.aec.IAECModule r0 = com.sec.internal.ims.registry.ImsRegistry.getAECModule()
            if (r0 == 0) goto L_0x0022
            boolean r0 = r0.isEntitlementRequired(r6)
            goto L_0x0023
        L_0x0022:
            r0 = r1
        L_0x0023:
            java.lang.String r2 = LOG_TAG
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "isVoWiFiEntitlementRequired: "
            r3.append(r4)
            r3.append(r0)
            java.lang.String r3 = r3.toString()
            com.sec.internal.log.IMSLog.i(r2, r6, r3)
            r6 = 1
            java.lang.Object[] r6 = new java.lang.Object[r6]
            java.lang.Integer r0 = java.lang.Integer.valueOf(r0)
            r6[r1] = r0
            r5.addRow(r6)
            return r5
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.entitlement.EntitlementContentProvider.isVoWiFiEntitlementRequired(int):android.database.Cursor");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:2:0x0017, code lost:
        r0 = com.sec.internal.ims.registry.ImsRegistry.getAECModule();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private android.database.Cursor getVoWiFiEntitlementStatus(int r6) {
        /*
            r5 = this;
            android.database.MatrixCursor r5 = new android.database.MatrixCursor
            java.lang.String r0 = "vowifi_entitlement_status"
            java.lang.String[] r0 = new java.lang.String[]{r0}
            r5.<init>(r0)
            com.sec.internal.constants.Mno r0 = com.sec.internal.helper.SimUtil.getSimMno(r6)
            boolean r0 = r0.isEur()
            r1 = 1
            if (r0 == 0) goto L_0x0028
            com.sec.internal.interfaces.ims.aec.IAECModule r0 = com.sec.internal.ims.registry.ImsRegistry.getAECModule()
            if (r0 == 0) goto L_0x0028
            boolean r2 = r0.isEntitlementRequired(r6)
            if (r2 == 0) goto L_0x0028
            boolean r0 = r0.getVoWiFiEntitlementStatus(r6)
            goto L_0x0029
        L_0x0028:
            r0 = r1
        L_0x0029:
            java.lang.String r2 = LOG_TAG
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "getVoWiFiEntitlementStatus: "
            r3.append(r4)
            r3.append(r0)
            java.lang.String r3 = r3.toString()
            com.sec.internal.log.IMSLog.i(r2, r6, r3)
            java.lang.Object[] r6 = new java.lang.Object[r1]
            java.lang.Integer r0 = java.lang.Integer.valueOf(r0)
            r1 = 0
            r6[r1] = r0
            r5.addRow(r6)
            return r5
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.entitlement.EntitlementContentProvider.getVoWiFiEntitlementStatus(int):android.database.Cursor");
    }
}
