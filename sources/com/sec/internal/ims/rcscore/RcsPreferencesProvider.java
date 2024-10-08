package com.sec.internal.ims.rcscore;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.imsservice.ImsServiceStub;
import com.sec.internal.ims.rcs.util.RcsUtils;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.interfaces.ims.servicemodules.im.IImModule;
import com.sec.internal.interfaces.ims.servicemodules.presence.IPresenceModule;

public class RcsPreferencesProvider extends ContentProvider {
    private static final String AUTHORITY = "com.sec.ims.android.rcs";
    private static final String KEY_ENRICHED_CALLING = "rcs_enriched_calling";
    private static final String KEY_MASTER_SWICH_VISIBILITY = "master_switch";
    private static final String KEY_PERMANENT_DISABLE = "permanent_disable_state";
    private static final String KEY_PERMANENT_DISABLE_AVAILABILITY = "permanent_disable_availibility";
    private static final String KEY_RCSPROFILE = "rcsprofile";
    private static final String KEY_RCS_ENABLED = "rcs_enabled";
    private static final String KEY_RCS_NOTIFICATION_SETTING = "rcs_connection_preference";
    private static final String KEY_REGISTRATION_STATUS = "registration_status";
    private static final String KEY_STATIC_ENABLE_RCS = "EnableRCS";
    private static final String KEY_STATIC_ENABLE_RCSCHAT = "EnableRCSchat";
    private static final String KEY_SUPPORT_DUAL_RCS = "support_dual_rcs";
    private static final String KEY_SUPPORT_DUAL_RCS_SETTINGS = "support_dual_rcs_settings";
    private static final String KEY_SUPPORT_DUAL_RCS_SIM1 = "support_dual_rcs_sim1";
    private static final String KEY_SUPPORT_DUAL_RCS_SIM2 = "support_dual_rcs_sim2";
    private static final String KEY_USER_ALIAS = "user_alias";
    private static final String KEY_VANILLA_APPLIED = "vanilla_applied";
    private static final String LOG_TAG = RcsPreferencesProvider.class.getSimpleName();
    private static final int MATCH_ENRICHED_CALLING = 11;
    private static final int MATCH_HOME_NETWORK = 2;
    private static final int MATCH_PERMANENT_DISABLE = 4;
    private static final int MATCH_PERMANENT_DISABLE_AVAILABILITY = 6;
    private static final int MATCH_RCSPROFILE = 8;
    private static final int MATCH_RCS_ENABLED_STATIC = 10;
    private static final int MATCH_REGISTRATION = 7;
    private static final int MATCH_ROAMING = 3;
    private static final int MATCH_SETTINGS = 1;
    private static final int MATCH_SUPPORT_DUAL_RCS = 9;
    private static final int MATCH_SUPPORT_DUAL_RCS_SETTINGS = 12;
    private static final int MATCH_USER_ALIAS = 5;
    private static final String TABLE_ENRICHED_CALLING = "rcs_enriched_calling";
    private static final String TABLE_HOME_NETWORK = "home_network";
    private static final String TABLE_PERMANENT_DISALBE = "permanent_disable_state";
    private static final String TABLE_PERMANENT_DISALBE_AVAILABILITY = "permanent_disable_availibility";
    private static final String TABLE_PREFERENCES = "preferences";
    private static final String TABLE_RCSPROFILE = "rcsprofile";
    private static final String TABLE_RCS_ENABLED_STATIC = "rcs_enabled_static";
    private static final String TABLE_REGISTRATION = "registration";
    private static final String TABLE_ROAMING = "roaming";
    private static final String TABLE_SUPPORT_DUAL_RCS = "support_dual_rcs";
    private static final String TABLE_SUPPORT_DUAL_RCS_SETTINGS = "support_dual_rcs_settings";
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

    static {
        UriMatcher uriMatcher = new UriMatcher(-1);
        sUriMatcher = uriMatcher;
        uriMatcher.addURI("com.sec.ims.android.rcs", TABLE_PREFERENCES + '/' + String.valueOf(1), 1);
        uriMatcher.addURI("com.sec.ims.android.rcs", TABLE_PREFERENCES + '/' + String.valueOf(5), 5);
        uriMatcher.addURI("com.sec.ims.android.rcs", TABLE_HOME_NETWORK, 2);
        uriMatcher.addURI("com.sec.ims.android.rcs", TABLE_ROAMING, 3);
        uriMatcher.addURI("com.sec.ims.android.rcs", "permanent_disable_state", 4);
        uriMatcher.addURI("com.sec.ims.android.rcs", "permanent_disable_availibility", 6);
        uriMatcher.addURI("com.sec.ims.android.rcs", TABLE_REGISTRATION, 7);
        uriMatcher.addURI("com.sec.ims.android.rcs", "rcsprofile", 8);
        uriMatcher.addURI("com.sec.ims.android.rcs", "support_dual_rcs", 9);
        uriMatcher.addURI("com.sec.ims.android.rcs", TABLE_RCS_ENABLED_STATIC, 10);
        uriMatcher.addURI("com.sec.ims.android.rcs", "rcs_enriched_calling", 11);
        uriMatcher.addURI("com.sec.ims.android.rcs", "support_dual_rcs_settings", 12);
    }

    public boolean onCreate() {
        this.mContext = getContext();
        return false;
    }

    public Cursor query(Uri uri, String[] strArr, String str, String[] strArr2, String str2) {
        int match = sUriMatcher.match(uri);
        String str3 = LOG_TAG;
        Log.i(str3, "query [" + uri + "] match [" + match + "]");
        int i = 0;
        if (SimUtil.isDualIMS()) {
            i = SimUtil.getActiveDataPhoneIdFromTelephony();
        }
        if (uri.getFragment() != null && uri.getFragment().contains("simslot")) {
            i = Character.getNumericValue(uri.getFragment().charAt(7));
        }
        switch (match) {
            case 1:
                return createMultiValueCursor(new String[]{KEY_RCS_ENABLED, KEY_VANILLA_APPLIED, KEY_MASTER_SWICH_VISIBILITY}, readCurrentSettingsValues(i));
            case 2:
                return createSingleValueCursor(KEY_RCS_NOTIFICATION_SETTING, (Integer) 1);
            case 3:
                return createSingleValueCursor(KEY_RCS_NOTIFICATION_SETTING, (Integer) 1);
            case 4:
                return createSingleValueCursor("permanent_disable_state", (Integer) 0);
            case 5:
                return createSingleValueCursor("user_alias", queryUserAlias(i));
            case 6:
                return createSingleValueCursor("permanent_disable_availibility", (Integer) 0);
            case 7:
                return createSingleValueCursor(KEY_REGISTRATION_STATUS, Integer.valueOf(isRcsRegistered(i) ? 1 : 0));
            case 8:
                return createSingleValueCursor("rcsprofile", ImsRegistry.getRcsProfileType(i));
            case 9:
                return createMultiValueCursor(new String[]{"support_dual_rcs", KEY_SUPPORT_DUAL_RCS_SIM1, KEY_SUPPORT_DUAL_RCS_SIM2}, getSupportDualRcs());
            case 10:
                return createMultiValueCursor(new String[]{"EnableRCS", "EnableRCSchat"}, getRcsEnabledStatic(i));
            case 11:
                return createSingleValueCursor("rcs_enriched_calling", Integer.valueOf(queryEnrichedCalling(i)));
            case 12:
                return createSingleValueCursor("support_dual_rcs_settings", Integer.valueOf(getSupportDualRcsSettings()));
            default:
                Log.e(str3, "query: uri not implemented: " + uri);
                return null;
        }
    }

    public int update(Uri uri, ContentValues contentValues, String str, String[] strArr) {
        String str2 = LOG_TAG;
        Log.d(str2, "update: " + uri);
        boolean z = false;
        if (contentValues == null) {
            Log.e(str2, "update: values are null");
            return 0;
        }
        int activeDataPhoneIdFromTelephony = SimUtil.isDualIMS() ? SimUtil.getActiveDataPhoneIdFromTelephony() : 0;
        if (uri.getFragment() != null && uri.getFragment().contains("simslot")) {
            activeDataPhoneIdFromTelephony = Character.getNumericValue(uri.getFragment().charAt(7));
        }
        int match = sUriMatcher.match(uri);
        if (match != 1) {
            if (match != 5) {
                Log.e(str2, "update: uri not implemented: " + uri);
                return 0;
            } else if (!contentValues.containsKey("user_alias")) {
                return 0;
            } else {
                String asString = contentValues.getAsString("user_alias");
                Log.d(str2, "User alias: " + asString);
                updateUserAlias(activeDataPhoneIdFromTelephony, asString);
                this.mContext.getContentResolver().notifyChange(uri, (ContentObserver) null);
            }
        } else if (!contentValues.containsKey(KEY_RCS_ENABLED)) {
            return 0;
        } else {
            if (contentValues.getAsBoolean(KEY_RCS_ENABLED) != null) {
                z = contentValues.getAsBoolean(KEY_RCS_ENABLED).booleanValue();
            }
            updateRCSSetting(z, activeDataPhoneIdFromTelephony);
        }
        return 1;
    }

    private Integer[] readCurrentSettingsValues(int i) {
        boolean z;
        boolean isServiceEnabledByPhoneId = ImsRegistry.isServiceEnabledByPhoneId("rcs_user_setting", i);
        boolean z2 = !ImsRegistry.isRcsEnabledByPhoneId(i);
        try {
            z = RcsUtils.UiUtils.isMainSwitchVisible(this.mContext, i);
        } catch (RemoteException e) {
            e.printStackTrace();
            z = false;
        }
        return new Integer[]{Integer.valueOf(isServiceEnabledByPhoneId ? 1 : 0), Integer.valueOf(z2 ? 1 : 0), Integer.valueOf(z ? 1 : 0)};
    }

    private Cursor createSingleValueCursor(String str, Integer num) {
        MatrixCursor matrixCursor = new MatrixCursor(new String[]{str}, 1);
        matrixCursor.addRow(new Integer[]{num});
        return matrixCursor;
    }

    private Cursor createSingleValueCursor(String str, String str2) {
        MatrixCursor matrixCursor = new MatrixCursor(new String[]{str}, 1);
        matrixCursor.addRow(new String[]{str2});
        return matrixCursor;
    }

    private Cursor createMultiValueCursor(String[] strArr, Integer[] numArr) {
        MatrixCursor matrixCursor = new MatrixCursor(strArr, strArr.length);
        matrixCursor.addRow(numArr);
        return matrixCursor;
    }

    private Cursor createMultiValueCursor(String[] strArr, String[] strArr2) {
        MatrixCursor matrixCursor = new MatrixCursor(strArr, strArr.length);
        matrixCursor.addRow(strArr2);
        return matrixCursor;
    }

    private void updateUserAlias(int i, String str) {
        IImModule imModule = ImsRegistry.getServiceModuleManager().getImModule();
        if (imModule != null) {
            imModule.setUserAlias(i, str);
            IPresenceModule presenceModule = ImsRegistry.getServiceModuleManager().getPresenceModule();
            if (presenceModule != null && str != null && imModule.getImConfig(i).getRealtimeUserAliasAuth()) {
                presenceModule.setDisplayText(i, str);
            }
        }
    }

    private String queryUserAlias(int i) {
        IImModule imModule = ImsRegistry.getServiceModuleManager().getImModule();
        return imModule != null ? imModule.getUserAliasFromPreference(i) : "";
    }

    private boolean isRcsRegistered(int i) {
        return ImsServiceStub.getInstance().getRegistrationManager().isRcsRegistered(i);
    }

    private void updateRCSSetting(boolean z, int i) {
        ImsRegistry.enableRcsByPhoneId(z, i);
    }

    private Integer[] getSupportDualRcs() {
        return new Integer[]{Integer.valueOf((ImsRegistry.getInt(SimUtil.getActiveDataPhoneId(), "dual_rcs_policy", 0) == 1 || ImsRegistry.getInt(SimUtil.getActiveDataPhoneId(), "dual_rcs_policy", 0) == 4) ? SimUtil.isDualIMS() : RcsUtils.DualRcs.isDualRcsReg() ? 1 : 0), Integer.valueOf(RcsUtils.DualRcs.isRegAllowed(this.mContext, 0) ? 1 : 0), Integer.valueOf(RcsUtils.DualRcs.isRegAllowed(this.mContext, 1) ? 1 : 0)};
    }

    /* JADX WARNING: Removed duplicated region for block: B:49:0x0112  */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x0114  */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x011a  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private java.lang.String[] getRcsEnabledStatic(int r12) {
        /*
            r11 = this;
            boolean r0 = com.sec.internal.helper.SimUtil.isSimMobilityFeatureEnabled()
            r1 = 2
            java.lang.String r2 = ", rcschat = "
            r3 = 1
            java.lang.String r4 = "true"
            java.lang.String r5 = "false"
            r6 = 0
            if (r0 == 0) goto L_0x007d
            android.content.Context r0 = r11.mContext
            boolean r0 = com.sec.internal.ims.util.ImsUtil.isSimMobilityActivatedForAmRcs(r0, r12)
            if (r0 == 0) goto L_0x001a
            r11 = r3
            goto L_0x0050
        L_0x001a:
            boolean r0 = com.sec.internal.ims.util.ImsUtil.isSimMobilityActivatedForRcs(r12)
            if (r0 == 0) goto L_0x004f
            android.content.Context r11 = r11.mContext
            java.util.List r11 = com.sec.internal.ims.settings.ImsProfileLoaderInternal.getProfileList(r11, r12)
            if (r11 == 0) goto L_0x004f
            int r12 = r11.size()
            if (r12 <= 0) goto L_0x004f
            java.util.Iterator r11 = r11.iterator()
        L_0x0032:
            boolean r12 = r11.hasNext()
            if (r12 == 0) goto L_0x004f
            java.lang.Object r12 = r11.next()
            com.sec.ims.settings.ImsProfile r12 = (com.sec.ims.settings.ImsProfile) r12
            if (r12 == 0) goto L_0x0032
            boolean r0 = r12.getEnableRcs()
            if (r0 == 0) goto L_0x0032
            boolean r11 = r12.getEnableRcs()
            boolean r12 = r12.getEnableRcsChat()
            goto L_0x0051
        L_0x004f:
            r11 = r6
        L_0x0050:
            r12 = r11
        L_0x0051:
            java.lang.String r0 = LOG_TAG
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            r7.<init>()
            java.lang.String r8 = "getRcsEnabledStatic: SimMobility, rcs = "
            r7.append(r8)
            r7.append(r11)
            r7.append(r2)
            r7.append(r12)
            java.lang.String r2 = r7.toString()
            android.util.Log.d(r0, r2)
            java.lang.String[] r0 = new java.lang.String[r1]
            if (r11 == 0) goto L_0x0073
            r11 = r4
            goto L_0x0074
        L_0x0073:
            r11 = r5
        L_0x0074:
            r0[r6] = r11
            if (r12 == 0) goto L_0x0079
            goto L_0x007a
        L_0x0079:
            r4 = r5
        L_0x007a:
            r0[r3] = r4
            return r0
        L_0x007d:
            java.lang.String r0 = com.sec.internal.constants.Mno.MOCK_MNO_PROPERTY
            java.lang.String r7 = ""
            java.lang.String r0 = android.os.SemSystemProperties.get(r0, r7)
            boolean r7 = android.text.TextUtils.isEmpty(r0)
            if (r7 == 0) goto L_0x0099
            android.content.Context r0 = r11.mContext
            java.lang.String r7 = "phone"
            java.lang.Object r0 = r0.getSystemService(r7)
            android.telephony.TelephonyManager r0 = (android.telephony.TelephonyManager) r0
            java.lang.String r0 = r0.getSimOperator()
        L_0x0099:
            java.lang.String r7 = LOG_TAG
            java.lang.StringBuilder r8 = new java.lang.StringBuilder
            r8.<init>()
            java.lang.String r9 = "getRcsEnabledStatic: operator = "
            r8.append(r9)
            r8.append(r0)
            java.lang.String r8 = r8.toString()
            android.util.Log.d(r7, r8)
            boolean r8 = android.text.TextUtils.isEmpty(r0)
            if (r8 == 0) goto L_0x00bf
            java.lang.String r11 = "getRcsEnabledStatic: operator is empty, rcs = false, rcschat = false"
            android.util.Log.d(r7, r11)
            java.lang.String[] r11 = new java.lang.String[]{r5, r5}
            return r11
        L_0x00bf:
            com.sec.internal.constants.Mno r8 = com.sec.internal.helper.SimUtil.getMno()
            boolean r8 = com.sec.internal.ims.util.ConfigUtil.isRcsEur((com.sec.internal.constants.Mno) r8)
            if (r8 == 0) goto L_0x00d1
            android.content.Context r11 = r11.mContext
            boolean r11 = com.sec.internal.ims.rcs.util.RcsUtils.UiUtils.isRcsEnabledinSettings(r11, r12)
        L_0x00cf:
            r12 = r11
            goto L_0x010e
        L_0x00d1:
            android.content.ContentValues r11 = com.sec.internal.ims.util.CscParser.getCscImsSetting((java.lang.String) r0, (int) r12)
            if (r11 == 0) goto L_0x0107
            int r12 = r11.size()
            if (r12 <= 0) goto L_0x0107
            java.lang.String r12 = "EnableRCS"
            boolean r12 = com.sec.internal.helper.CollectionUtils.getBooleanValue(r11, r12, r6)
            java.lang.String r0 = "EnableRCSchat"
            boolean r11 = com.sec.internal.helper.CollectionUtils.getBooleanValue(r11, r0, r6)
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r8 = "getRcsEnabledStatic: Customer, rcs = "
            r0.append(r8)
            r0.append(r12)
            r0.append(r2)
            r0.append(r11)
            java.lang.String r0 = r0.toString()
            android.util.Log.d(r7, r0)
            r10 = r12
            r12 = r11
            r11 = r10
            goto L_0x010e
        L_0x0107:
            java.lang.String r11 = "getRcsEnabledStatic: cscSettings is null, rcs = false, rcschat = false"
            android.util.Log.d(r7, r11)
            r11 = r6
            goto L_0x00cf
        L_0x010e:
            java.lang.String[] r0 = new java.lang.String[r1]
            if (r11 == 0) goto L_0x0114
            r11 = r4
            goto L_0x0115
        L_0x0114:
            r11 = r5
        L_0x0115:
            r0[r6] = r11
            if (r12 == 0) goto L_0x011a
            goto L_0x011b
        L_0x011a:
            r4 = r5
        L_0x011b:
            r0[r3] = r4
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.rcscore.RcsPreferencesProvider.getRcsEnabledStatic(int):java.lang.String[]");
    }

    private int queryEnrichedCalling(int i) {
        return RcsUtils.UiUtils.isRcsEnabledEnrichedCalling(i) ? 1 : 0;
    }

    private int getSupportDualRcsSettings() {
        return RcsUtils.DualRcs.isDualRcsSettings() ? 1 : 0;
    }
}
