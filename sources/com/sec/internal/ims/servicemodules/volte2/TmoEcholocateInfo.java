package com.sec.internal.ims.servicemodules.volte2;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.pm.SigningInfo;
import android.telephony.CellIdentity;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityNr;
import android.telephony.CellInfo;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.os.PhoneConstants;
import com.sec.internal.constants.ims.servicemodules.volte2.CallConstants;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.core.cmc.CmcConstants;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.ims.util.SemTelephonyAdapter;
import com.sec.internal.interfaces.ims.core.IPdnController;
import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class TmoEcholocateInfo {
    private static final String LOG_TAG = "Echolocate_Info";
    protected static final String LTE_RAT = "3GPP-E-UTRAN-FDD";
    private static final String NETWORK_NA = "NA";
    private static final String NETWORK_TYPE_ENDC = "ENDC";
    private static final String NETWORK_TYPE_LTE = "LTE";
    private static final String NETWORK_TYPE_SA5G = "SA5G";
    private static final String NETWORK_TYPE_WIFI = "WFC2";
    protected static final int NR_EPSFB_FAILED = 3;
    protected static final int NR_EPSFB_INIT = 0;
    protected static final int NR_EPSFB_STARTED = 1;
    protected static final int NR_EPSFB_SUCCESS = 2;
    private static final String NR_FDD_RAT = "3GPP-NR-FDD";
    private static final String NR_RAT = "3GPP-NR";
    private static final int NR_STATUS_CONNECTED = 3;
    protected static final int NR_TAU_REJECT = 4;
    private static final String NR_TDD_RAT = "3GPP-NR-TDD";
    private static final int RAT_5G = 6;
    private static final int RAT_LTE = 3;
    private static final Signature SIGNATURES_ECHO_APP = new Signature("308203623082024aa00302010202044df1bf45300d06092a864886f70d01010505003073310b3009060355040613025553310b30090603550408130257413111300f0603550407130842656c6c657675653111300f060355040a1308542d4d6f62696c6531133011060355040b130a546563686e6f6c6f6779311c301a0603550403131350726f64756374205265616c697a6174696f6e301e170d3131303631303036353235335a170d3338313032363036353235335a3073310b3009060355040613025553310b30090603550408130257413111300f0603550407130842656c6c657675653111300f060355040a1308542d4d6f62696c6531133011060355040b130a546563686e6f6c6f6779311c301a0603550403131350726f64756374205265616c697a6174696f6e30820122300d06092a864886f70d01010105000382010f003082010a0282010100c1456176d31c8989df7e0b30569da5c9b782380d3ff28fb48b4a17c8a125f40ba14862518397800f7a1030bf7cc188b9296d84af5cc5dc37752a1ca2c33d654258a3fdd29d19f2a0dd4e24b328b03bfef8c17bb8da11a25fdae10c1e1e288e3c1f47ee47617972382b0854474da1d6b526b9787d9a2f8e00600a4e436bfa790d04a0376fd7bd5c6ee78a6e522bbaa969d63667d17ca8fd90087fcc4acf2a2676d341a8e19dc46beb82bb1990710bd4101df8943ef8a3f2d7cb0bac6677ae69f9f3d25c134c08dfeb82000f44dea4164f90a65e352387fdd203c3479cfb380a2f8af5af3219a726ba9d82d72229a8d32979ce84be52006f4b71fe75011e8e2d090203010001300d06092a864886f70d01010505000382010100188d18ea72a49334736e118e766744489c7a5c47543cc35cc62a8cce35e84dfd426af3595fe55192dcb2a54c594a8d0de064dad96d72969fbc873c7a9fe7e14b11aed16c6d4bf90c1911b7d8a054c0c34c7a58c4a434d46e72f6142b654af24d461089c4633aa21cead0b154efac0aec4d68403c51bceab76c33a819857531c6a459a266f495f810417e9583d71f3f53a533f1e7013007253e9ed3466432a21977837669cff2b6b20612c055ff09b44ca15ca6830cdb289398d290852d3b0204deecbb00292194cc7533e5ae593e0d355883ea8022eb6fe5e807d6c059b3f6d6f637cd4014da425742f21b54ec37c6f55d3f0b8b6ced1cbc09376e8ea023396f");
    private static final Signature SIGNATURES_MY_TMOBILE = new Signature("308203623082024aa00302010202044df1bf45300d06092a864886f70d01010505003073310b3009060355040613025553310b30090603550408130257413111300f0603550407130842656c6c657675653111300f060355040a1308542d4d6f62696c6531133011060355040b130a546563686e6f6c6f6779311c301a0603550403131350726f64756374205265616c697a6174696f6e301e170d3131303631303036353235335a170d3338313032363036353235335a3073310b3009060355040613025553310b30090603550408130257413111300f0603550407130842656c6c657675653111300f060355040a1308542d4d6f62696c6531133011060355040b130a546563686e6f6c6f6779311c301a0603550403131350726f64756374205265616c697a6174696f6e30820122300d06092a864886f70d01010105000382010f003082010a0282010100c1456176d31c8989df7e0b30569da5c9b782380d3ff28fb48b4a17c8a125f40ba14862518397800f7a1030bf7cc188b9296d84af5cc5dc37752a1ca2c33d654258a3fdd29d19f2a0dd4e24b328b03bfef8c17bb8da11a25fdae10c1e1e288e3c1f47ee47617972382b0854474da1d6b526b9787d9a2f8e00600a4e436bfa790d04a0376fd7bd5c6ee78a6e522bbaa969d63667d17ca8fd90087fcc4acf2a2676d341a8e19dc46beb82bb1990710bd4101df8943ef8a3f2d7cb0bac6677ae69f9f3d25c134c08dfeb82000f44dea4164f90a65e352387fdd203c3479cfb380a2f8af5af3219a726ba9d82d72229a8d32979ce84be52006f4b71fe75011e8e2d090203010001300d06092a864886f70d01010505000382010100188d18ea72a49334736e118e766744489c7a5c47543cc35cc62a8cce35e84dfd426af3595fe55192dcb2a54c594a8d0de064dad96d72969fbc873c7a9fe7e14b11aed16c6d4bf90c1911b7d8a054c0c34c7a58c4a434d46e72f6142b654af24d461089c4633aa21cead0b154efac0aec4d68403c51bceab76c33a819857531c6a459a266f495f810417e9583d71f3f53a533f1e7013007253e9ed3466432a21977837669cff2b6b20612c055ff09b44ca15ca6830cdb289398d290852d3b0204deecbb00292194cc7533e5ae593e0d355883ea8022eb6fe5e807d6c059b3f6d6f637cd4014da425742f21b54ec37c6f55d3f0b8b6ced1cbc09376e8ea023396f");
    private static final Signature SPRINT_HUB_SIGNATURES = new Signature("3082036c30820254a00302010202044d23332e300d06092a864886f70d01010505003078310b3009060355040613025553310b3009060355040813024b53311630140603550407130d4f7665726c616e64205061726b310f300d060355040a1306537072696e74310b3009060355040b13024345312630240603550403131d537072696e7420416e64726f69642050726f64756374696f6e204b6579301e170d3131303130343134343831345a170d3338303532323134343831345a3078310b3009060355040613025553310b3009060355040813024b53311630140603550407130d4f7665726c616e64205061726b310f300d060355040a1306537072696e74310b3009060355040b13024345312630240603550403131d537072696e7420416e64726f69642050726f64756374696f6e204b657930820122300d06092a864886f70d01010105000382010f003082010a0282010100b3cca5f477ea6e744a61b7c19706d7976da388ea4b8598c4fbc5c31cc95abb3a7b949d5b10692d397f3d980eb7c5e305b2eac5329d485c76a2df1b530d3cffa5f4c436735449bd676eabc403e2981edfe883b296dbf89bdd655e2b8a065d68189db9763681aee66e1c0bed05defc4dbc9d749a04a4206b89cc9d6765ab726d3301fdffe21285fcffe8ba2c3069048e3435c8b73b0aeb79433e3dd5d19e35f3c618dc95103b89a562f4952543cf1221797fa3cbb224184e17fcb95c5c7474db377f106918cf84bbecb2da57c3bb2e01d4d4939dcf7e3c01288a9d3909606f99b040a62a920112a21b23602f1473966d3d3379018a2e0088e0209587ea06e084dd0203010001300d06092a864886f70d01010505000382010100766f3c7d3e9db4364856693f6acb07af7269d0524d5b6bb6072e78fd0873a102f427de9affa72d3b297c997d601d9678f6d670beaf0425653527ec327dc4817082b9afaa1ce10d3f979b5d950efe1ef5eeeecc06c0aebab6e941cc25983a6be2c724c7e2b2bbe52de9ffd10e0cb4b99f83c1680c5a5927e3752d9d5b7f30c53a93f83b17c708cb338550dc2d64b6f58f2594f6af3bef770dd4d2551818dbd8cbe6b853b9e8b611d2766dcadf57e2b2c42aa3bb7c914461686df500c0a9cc01ab3df1bc997a1c8608df7a3e335cf628682f8015ca274d10476b3b3eaa34c224301d6a92a85624a4c56473a54e56a7ae395edb012472c1b07bc84202da98433238");
    private static final String WIFI_RAT = "IEEE-802.11";
    private final Context mContext;
    private TmoEcholocateController mEchoController;
    private VolteServiceModuleInternal mModule = null;
    private IPdnController mPdnController = null;
    private TelephonyManager mTelephonyManager = null;
    private PackageManager pm = null;

    /* access modifiers changed from: protected */
    public String getEPSFBState(int i) {
        return i == 1 ? "EPSFB_STARTED" : (i == 3 || i == 4) ? "EPSFB_FAILED" : i == 2 ? "EPSFB_SUCCESSFUL" : "";
    }

    /* access modifiers changed from: protected */
    public String getNetworkTypeForEPSFB(int i) {
        return (i == 1 || i == 3) ? NETWORK_TYPE_SA5G : NETWORK_TYPE_LTE;
    }

    /* access modifiers changed from: protected */
    public String getPSHOState(int i) {
        return i == 1 ? "PSHO_STARTED" : (i == 3 || i == 4) ? "PSHO_FAILED" : i == 2 ? "PSHO_SUCCESSFUL" : "";
    }

    public TmoEcholocateInfo(Context context, TmoEcholocateController tmoEcholocateController, IPdnController iPdnController, VolteServiceModuleInternal volteServiceModuleInternal) {
        this.mContext = context;
        this.mEchoController = tmoEcholocateController;
        this.pm = context.getPackageManager();
        this.mPdnController = iPdnController;
        this.mModule = volteServiceModuleInternal;
        this.mTelephonyManager = (TelephonyManager) context.getSystemService(PhoneConstants.PHONE_KEY);
    }

    /* access modifiers changed from: protected */
    public boolean isEndCall(String str) {
        return str.contains("CANCEL") || str.contains("BYE");
    }

    /* access modifiers changed from: protected */
    public String getSDPContents(String str) {
        StringBuffer stringBuffer = new StringBuffer();
        String[] split = str.split("\r\n");
        stringBuffer.append(";");
        for (String str2 : split) {
            if (str2.contains("c=") || str2.contains("a=rtpmap") || str2.contains("a=recvonly") || str2.contains("a=sendonly") || str2.contains("a=sendrecv")) {
                stringBuffer.append(CmcConstants.E_NUM_STR_QUOTE);
                stringBuffer.append(str2);
                stringBuffer.append(CmcConstants.E_NUM_STR_QUOTE);
                stringBuffer.append(";");
            }
        }
        return stringBuffer.toString();
    }

    /* access modifiers changed from: protected */
    public String getNewAppCallId() {
        String valueOf = String.valueOf(ImsUtil.getRandom().nextInt(89999999) + 10000000);
        Log.i(LOG_TAG, "make new echo AppCallId " + valueOf);
        return valueOf;
    }

    /* access modifiers changed from: protected */
    public String getNetworkTypeForPSHO(int i, int i2, int i3, int i4) {
        if (i2 == 1) {
            return i3 == 6 ? NETWORK_TYPE_SA5G : NETWORK_TYPE_LTE;
        }
        if (i2 == 3 || i2 == 4) {
            return getNetworkType(i, false);
        }
        return (i2 == 2 && i4 == 6) ? NETWORK_TYPE_SA5G : NETWORK_TYPE_LTE;
    }

    /* access modifiers changed from: protected */
    public String getVoiceConfig() {
        String valueDeviceConfig = getValueDeviceConfig("//VoNR/VoNRDefault", false);
        if ("-2".equalsIgnoreCase(valueDeviceConfig)) {
            return valueDeviceConfig;
        }
        if (CloudMessageProviderContract.JsonData.TRUE.equalsIgnoreCase(valueDeviceConfig)) {
            return "ON_VONR";
        }
        return ConfigConstants.VALUE.INFO_COMPLETED.equalsIgnoreCase(valueDeviceConfig) ? "OFF_VONR" : "DEFAULT_ON_VONR";
    }

    /* access modifiers changed from: protected */
    public String getVoWiFiConfig() {
        String valueDeviceConfig = getValueDeviceConfig("//VoNR/VoWiFiDisable5GSA", false);
        if ("-2".equalsIgnoreCase(valueDeviceConfig)) {
            return valueDeviceConfig;
        }
        if (CloudMessageProviderContract.JsonData.TRUE.equalsIgnoreCase(valueDeviceConfig)) {
            return "DISABLED_5GSA";
        }
        return ConfigConstants.VALUE.INFO_COMPLETED.equalsIgnoreCase(valueDeviceConfig) ? "ENABLED_5GSA" : "DEFAULT_DISABLED_5GSA";
    }

    /* access modifiers changed from: protected */
    public LinkedHashMap<String, String> getSa5gBandConfig(int i) {
        String str;
        LinkedHashMap<String, String> linkedHashMap = new LinkedHashMap<>();
        if (SemTelephonyAdapter.isSupportedNrca(i)) {
            str = getValueDeviceConfig("//StandaloneBands5G_NRCA", true);
        } else {
            str = getValueDeviceConfig("//StandaloneBands5G", true);
        }
        if (TextUtils.isEmpty(str)) {
            linkedHashMap.put("NONE", "-1");
            return linkedHashMap;
        } else if (!"-2".equalsIgnoreCase(str)) {
            return parseBands5GXml(str);
        } else {
            linkedHashMap.put("ERROR", "-2");
            return linkedHashMap;
        }
    }

    /* access modifiers changed from: protected */
    public String getConfigVersion() {
        String valueDeviceConfig = getValueDeviceConfig("//configInfo/version", false);
        if (TextUtils.isEmpty(valueDeviceConfig)) {
            return "-1";
        }
        if ("-2".equalsIgnoreCase(valueDeviceConfig)) {
            return "-1";
        }
        return valueDeviceConfig;
    }

    /* JADX WARNING: Removed duplicated region for block: B:19:0x0057 A[SYNTHETIC, Splitter:B:19:0x0057] */
    /* JADX WARNING: Removed duplicated region for block: B:31:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private java.lang.String getValueDeviceConfig(java.lang.String r10, boolean r11) {
        /*
            r9 = this;
            java.lang.String r0 = "Echolocate_Info"
            if (r11 == 0) goto L_0x0008
            java.lang.String r1 = "tag"
            goto L_0x000b
        L_0x0008:
            java.lang.String r1 = "xpath"
        L_0x000b:
            java.lang.String r2 = "content://com.samsung.ims.entitlementconfig.provider"
            android.net.Uri r2 = android.net.Uri.parse(r2)
            java.lang.String r3 = "config"
            android.net.Uri r2 = android.net.Uri.withAppendedPath(r2, r3)
            android.net.Uri r1 = android.net.Uri.withAppendedPath(r2, r1)
            android.net.Uri$Builder r1 = r1.buildUpon()
            java.lang.String r2 = "tag_name"
            r1.appendQueryParameter(r2, r10)
            android.net.Uri r4 = r1.build()
            android.content.Context r9 = r9.mContext     // Catch:{ SQLException -> 0x0066 }
            android.content.ContentResolver r3 = r9.getContentResolver()     // Catch:{ SQLException -> 0x0066 }
            r5 = 0
            r6 = 0
            r7 = 0
            r8 = 0
            android.database.Cursor r9 = r3.query(r4, r5, r6, r7, r8)     // Catch:{ SQLException -> 0x0066 }
            if (r9 == 0) goto L_0x004f
            boolean r10 = r9.moveToFirst()     // Catch:{ all -> 0x004d }
            if (r10 == 0) goto L_0x004f
            if (r11 == 0) goto L_0x0047
            r10 = 0
            java.lang.String r10 = r9.getString(r10)     // Catch:{ all -> 0x004d }
            goto L_0x0055
        L_0x0047:
            r10 = 1
            java.lang.String r10 = r9.getString(r10)     // Catch:{ all -> 0x004d }
            goto L_0x0055
        L_0x004d:
            r10 = move-exception
            goto L_0x005b
        L_0x004f:
            java.lang.String r10 = "getValueDeviceConfig : cursor is null"
            android.util.Log.i(r0, r10)     // Catch:{ all -> 0x004d }
            r10 = 0
        L_0x0055:
            if (r9 == 0) goto L_0x007d
            r9.close()     // Catch:{ SQLException -> 0x0066 }
            goto L_0x007d
        L_0x005b:
            if (r9 == 0) goto L_0x0065
            r9.close()     // Catch:{ all -> 0x0061 }
            goto L_0x0065
        L_0x0061:
            r9 = move-exception
            r10.addSuppressed(r9)     // Catch:{ SQLException -> 0x0066 }
        L_0x0065:
            throw r10     // Catch:{ SQLException -> 0x0066 }
        L_0x0066:
            r9 = move-exception
            java.lang.StringBuilder r10 = new java.lang.StringBuilder
            r10.<init>()
            java.lang.String r11 = "getValueDeviceConfig: "
            r10.append(r11)
            r10.append(r9)
            java.lang.String r9 = r10.toString()
            android.util.Log.e(r0, r9)
            java.lang.String r10 = "-2"
        L_0x007d:
            return r10
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.volte2.TmoEcholocateInfo.getValueDeviceConfig(java.lang.String, boolean):java.lang.String");
    }

    private LinkedHashMap<String, String> parseBands5GXml(String str) {
        LinkedHashMap<String, String> linkedHashMap = new LinkedHashMap<>();
        try {
            XmlPullParser newPullParser = XmlPullParserFactory.newInstance().newPullParser();
            newPullParser.setInput(new StringReader(str));
            for (int eventType = newPullParser.getEventType(); eventType != 1; eventType = newPullParser.next()) {
                if (eventType == 2) {
                    String name = newPullParser.getName();
                    if (!"StandaloneBands5G".equalsIgnoreCase(name) && !"StandaloneBands5G_NRCA".equalsIgnoreCase(name)) {
                        String str2 = "";
                        String text = newPullParser.next() == 4 ? newPullParser.getText() : str2;
                        if (!TextUtils.isEmpty(text)) {
                            if (CloudMessageProviderContract.JsonData.TRUE.equalsIgnoreCase(text) || ConfigConstants.VALUE.INFO_COMPLETED.equalsIgnoreCase(text)) {
                                str2 = text;
                            }
                        }
                        linkedHashMap.put(name, str2);
                    }
                }
            }
            Log.i(LOG_TAG, "End document");
        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
        }
        return linkedHashMap;
    }

    /* access modifiers changed from: protected */
    public String getRatType(int i, String str) {
        Log.i(LOG_TAG, "getRatType Type: " + str);
        if (!NETWORK_TYPE_SA5G.equals(str)) {
            return NETWORK_TYPE_WIFI.equals(str) ? "IEEE-802.11" : LTE_RAT;
        }
        String telephonyProperty = TelephonyManager.getTelephonyProperty(i, "ril.nrnetworktype", "");
        if ("TDD".equals(telephonyProperty)) {
            return NR_TDD_RAT;
        }
        return "FDD".equals(telephonyProperty) ? NR_FDD_RAT : NR_RAT;
    }

    private TelephonyManager getTelephonyManager(int i) {
        return ((TelephonyManager) this.mContext.getSystemService(PhoneConstants.PHONE_KEY)).createForSubscriptionId(i);
    }

    /* access modifiers changed from: protected */
    public String getNetworkType(int i, boolean z) {
        if (z) {
            return NETWORK_TYPE_WIFI;
        }
        if (this.mEchoController.getEPSFBsuccess(i)) {
            return NETWORK_TYPE_LTE;
        }
        try {
            int subId = SimUtil.getSubId(i);
            if (getTelephonyManager(subId) == null) {
                return NETWORK_NA;
            }
            if (getTelephonyManager(subId).getNetworkType() == 20) {
                return NETWORK_TYPE_SA5G;
            }
            ServiceState serviceState = getTelephonyManager(subId).getServiceState();
            if (serviceState == null) {
                return NETWORK_NA;
            }
            if (serviceState.getNrState() == 3) {
                return NETWORK_TYPE_ENDC;
            }
            return NETWORK_TYPE_LTE;
        } catch (NullPointerException unused) {
            Log.i(LOG_TAG, "ServiceState is Null");
            return NETWORK_NA;
        }
    }

    /* access modifiers changed from: protected */
    public String getLteBand(int i, boolean z, String str) {
        ServiceState serviceState;
        String telephonyProperty = TelephonyManager.getTelephonyProperty(i, "ril.lteband", "0");
        int subId = SimUtil.getSubId(i);
        if (TextUtils.isEmpty(telephonyProperty) || z || TextUtils.equals(telephonyProperty, "255")) {
            return NETWORK_NA;
        }
        if (!this.mEchoController.getEPSFBsuccess(i) && str != null && str.equals(NETWORK_TYPE_SA5G)) {
            telephonyProperty = "n" + telephonyProperty;
        }
        try {
            if (!(getTelephonyManager(subId) == null || (serviceState = getTelephonyManager(subId).getServiceState()) == null || !serviceState.isUsingCarrierAggregation())) {
                String telephonyProperty2 = TelephonyManager.getTelephonyProperty(i, "ril.ltescellbands", "0");
                if (!TextUtils.isEmpty(telephonyProperty2) && !TextUtils.equals(telephonyProperty2, "255")) {
                    telephonyProperty = telephonyProperty + "," + telephonyProperty2;
                }
                Log.e(LOG_TAG, "isUsingCarrierAggregation() scndBand[" + i + "]:" + telephonyProperty2);
            }
        } catch (NullPointerException unused) {
            Log.i(LOG_TAG, "ServiceState is Null");
        }
        Log.i(LOG_TAG, "strband[" + i + "]:" + telephonyProperty + ", nwType:" + str);
        return telephonyProperty;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x012b  */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x0130  */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x0137  */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x01d7  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public java.lang.String getNwStateSignal(int r19, boolean r20) {
        /*
            r18 = this;
            r0 = r18
            r1 = r19
            int r2 = com.sec.internal.helper.SimUtil.getSubId(r19)
            java.lang.StringBuffer r3 = new java.lang.StringBuffer
            r3.<init>()
            android.telephony.TelephonyManager r4 = r0.getTelephonyManager(r2)
            if (r4 == 0) goto L_0x001c
            android.telephony.TelephonyManager r2 = r0.getTelephonyManager(r2)
            int r2 = r2.getNetworkType()
            goto L_0x001e
        L_0x001c:
            r2 = 13
        L_0x001e:
            com.sec.internal.ims.servicemodules.volte2.TmoEcholocateController r4 = r0.mEchoController
            com.sec.internal.helper.os.SignalStrengthWrapper r4 = r4.getSignalStrength(r1)
            java.lang.String r5 = "NA"
            if (r4 == 0) goto L_0x0031
            int r6 = r4.getDbm(r2)
            java.lang.String r6 = java.lang.Integer.toString(r6)
            goto L_0x0032
        L_0x0031:
            r6 = r5
        L_0x0032:
            android.content.Context r7 = r0.mContext
            java.lang.String r8 = "wifi"
            java.lang.Object r7 = r7.getSystemService(r8)
            android.net.wifi.WifiManager r7 = (android.net.wifi.WifiManager) r7
            r8 = 3
            java.lang.String r9 = ""
            r10 = 1
            java.lang.String r11 = ";"
            if (r20 == 0) goto L_0x0075
            if (r7 == 0) goto L_0x005c
            android.net.wifi.WifiInfo r12 = r7.getConnectionInfo()
            if (r12 == 0) goto L_0x005a
            android.net.wifi.WifiInfo r7 = r7.getConnectionInfo()
            int r7 = r7.getRssi()
            java.lang.String r7 = java.lang.Integer.toString(r7)
            goto L_0x005d
        L_0x005a:
            r7 = r9
            goto L_0x005d
        L_0x005c:
            r7 = r5
        L_0x005d:
            android.content.Context r0 = r0.mContext
            int r0 = com.sec.internal.constants.ims.VowifiConfig.getPrefMode(r0, r10, r1)
            if (r0 != r8) goto L_0x0076
            r3.append(r11)
            r3.append(r7)
            java.lang.String r0 = ";NA;NA;NA;NA;NA;NA;"
            r3.append(r0)
            java.lang.String r0 = r3.toString()
            return r0
        L_0x0075:
            r7 = r5
        L_0x0076:
            boolean r0 = android.text.TextUtils.isEmpty(r6)
            r6 = 20
            java.lang.String r12 = "Echolocate_Info"
            java.lang.String r13 = "]"
            java.lang.String r14 = ";NA;"
            if (r0 == 0) goto L_0x008f
            r3.append(r11)
            r3.append(r7)
            r3.append(r14)
            goto L_0x0117
        L_0x008f:
            if (r4 == 0) goto L_0x0117
            java.lang.String r0 = " "
            if (r2 != r6) goto L_0x00dd
            int r15 = r4.getNrLevel()
            int r10 = r4.getInvalidSignalStrength()
            if (r15 == r10) goto L_0x00dd
            int r10 = r4.getNrSsRsrp()
            java.lang.String r10 = java.lang.Integer.toString(r10)
            int r15 = r4.getNrSsRsrq()
            java.lang.String r15 = java.lang.Integer.toString(r15)
            int r16 = r4.getNrSsSinr()
            java.lang.String r6 = java.lang.Integer.toString(r16)
            java.lang.StringBuilder r8 = new java.lang.StringBuilder
            r8.<init>()
            r17 = r2
            java.lang.String r2 = "Nr [ rsrp: rsrq: sinr: "
            r8.append(r2)
            r8.append(r10)
            r8.append(r0)
            r8.append(r15)
            r8.append(r0)
            r8.append(r6)
            r8.append(r13)
            java.lang.String r0 = r8.toString()
            android.util.Log.d(r12, r0)
            goto L_0x010d
        L_0x00dd:
            r17 = r2
            int r2 = r4.getLteRsrp()
            java.lang.String r10 = java.lang.Integer.toString(r2)
            int r2 = r4.getLteRsrq()
            java.lang.String r15 = java.lang.Integer.toString(r2)
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r6 = "Default LTE [ rsrp: rsrq:"
            r2.append(r6)
            r2.append(r10)
            r2.append(r0)
            r2.append(r15)
            r2.append(r13)
            java.lang.String r0 = r2.toString()
            android.util.Log.d(r12, r0)
            r6 = r5
        L_0x010d:
            r3.append(r11)
            r3.append(r7)
            r3.append(r14)
            goto L_0x011c
        L_0x0117:
            r17 = r2
            r6 = r5
            r10 = r6
            r15 = r10
        L_0x011c:
            java.lang.String r0 = "ril.signal.param"
            java.lang.String r2 = "0"
            java.lang.String r0 = android.telephony.TelephonyManager.getTelephonyProperty(r1, r0, r2)
            boolean r2 = android.text.TextUtils.isEmpty(r0)
            if (r2 != 0) goto L_0x0130
            java.lang.String[] r2 = r0.split(r11)
            goto L_0x0131
        L_0x0130:
            r2 = 0
        L_0x0131:
            if (r2 == 0) goto L_0x01d7
            int r7 = r2.length
            r8 = 3
            if (r7 < r8) goto L_0x01d7
            r7 = 0
            r8 = r2[r7]
            boolean r8 = r9.equals(r8)
            r20 = r12
            java.lang.String r12 = "255"
            if (r8 != 0) goto L_0x0150
            r8 = r2[r7]
            boolean r8 = r12.equals(r8)
            if (r8 == 0) goto L_0x014d
            goto L_0x0150
        L_0x014d:
            r7 = r2[r7]
            goto L_0x0151
        L_0x0150:
            r7 = r5
        L_0x0151:
            r3.append(r7)
            r3.append(r11)
            r3.append(r10)
            r3.append(r11)
            r3.append(r15)
            boolean r7 = r6.equals(r5)
            if (r7 != 0) goto L_0x0182
            r7 = r17
            r8 = 20
            if (r7 != r8) goto L_0x0182
            if (r4 == 0) goto L_0x0182
            int r7 = r4.getNrLevel()
            int r4 = r4.getInvalidSignalStrength()
            if (r7 == r4) goto L_0x0182
            r3.append(r11)
            r3.append(r6)
            r3.append(r14)
            goto L_0x01ed
        L_0x0182:
            r4 = 2
            r6 = r2[r4]
            boolean r6 = r9.equals(r6)
            if (r6 != 0) goto L_0x0196
            r6 = r2[r4]
            boolean r6 = r12.equals(r6)
            if (r6 == 0) goto L_0x0194
            goto L_0x0196
        L_0x0194:
            r5 = r2[r4]
        L_0x0196:
            r2[r4] = r5
            r5 = 1
            r6 = r2[r5]
            boolean r6 = r9.equals(r6)
            if (r6 != 0) goto L_0x01bf
            r6 = r2[r5]
            boolean r6 = r12.equals(r6)
            if (r6 == 0) goto L_0x01aa
            goto L_0x01bf
        L_0x01aa:
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            r4.append(r11)
            r2 = r2[r5]
            r4.append(r2)
            r4.append(r14)
            java.lang.String r2 = r4.toString()
            goto L_0x01d3
        L_0x01bf:
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            r5.append(r11)
            r2 = r2[r4]
            r5.append(r2)
            r5.append(r14)
            java.lang.String r2 = r5.toString()
        L_0x01d3:
            r3.append(r2)
            goto L_0x01ed
        L_0x01d7:
            r20 = r12
            r3.append(r5)
            r3.append(r11)
            r3.append(r10)
            r3.append(r11)
            r3.append(r15)
            java.lang.String r2 = ";NA;NA;"
            r3.append(r2)
        L_0x01ed:
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r4 = "signal["
            r2.append(r4)
            r2.append(r1)
            r2.append(r13)
            r2.append(r0)
            java.lang.String r0 = " result["
            r2.append(r0)
            java.lang.String r0 = r3.toString()
            r2.append(r0)
            r2.append(r13)
            java.lang.String r0 = r2.toString()
            r1 = r20
            android.util.Log.i(r1, r0)
            java.lang.String r0 = r3.toString()
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.volte2.TmoEcholocateInfo.getNwStateSignal(int, boolean):java.lang.String");
    }

    /* access modifiers changed from: protected */
    public ImsCallSession getPreCallSession(int i) {
        List<ImsCallSession> sessionByState = this.mModule.getSessionByState(i, CallConstants.STATE.OutGoingCall);
        List<ImsCallSession> sessionByState2 = this.mModule.getSessionByState(i, CallConstants.STATE.AlertingCall);
        List<ImsCallSession> sessionByState3 = this.mModule.getSessionByState(i, CallConstants.STATE.ReadyToCall);
        List<ImsCallSession> sessionByState4 = this.mModule.getSessionByState(i, CallConstants.STATE.IncomingCall);
        if (sessionByState.size() > 0) {
            return sessionByState.get(0);
        }
        if (sessionByState2.size() > 0) {
            return sessionByState2.get(0);
        }
        if (sessionByState3.size() > 0) {
            return sessionByState3.get(0);
        }
        if (sessionByState4.size() > 0) {
            return sessionByState4.get(0);
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public String getTimeStamp(int i) {
        return Long.toString(System.currentTimeMillis() - ((long) i));
    }

    /* access modifiers changed from: protected */
    public int getPhoneIdFromSessionId(int i) {
        int i2;
        if (i >= 1 && (i2 = i / 10) < 2) {
            return i2;
        }
        return 0;
    }

    /* access modifiers changed from: protected */
    public String getCellId(int i, String str, boolean z) {
        String str2;
        Log.i(LOG_TAG, "getCellId Type: " + str);
        List<CellInfo> allCellInfo = this.mPdnController.getAllCellInfo(i, true);
        if (allCellInfo != null && !allCellInfo.isEmpty()) {
            Iterator<CellInfo> it = allCellInfo.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                CellInfo next = it.next();
                if (next.isRegistered()) {
                    CellIdentity cellIdentity = next.getCellIdentity();
                    if (cellIdentity instanceof CellIdentityLte) {
                        str2 = String.valueOf(((CellIdentityLte) cellIdentity).getCi());
                        break;
                    } else if (cellIdentity instanceof CellIdentityNr) {
                        str2 = String.valueOf(((CellIdentityNr) cellIdentity).getNci());
                        break;
                    }
                }
            }
        }
        str2 = "-1";
        Log.i(LOG_TAG, "getCellId : " + str2);
        return str2;
    }

    /* access modifiers changed from: protected */
    public boolean checkSecurity(String str) {
        if ("TMB".equalsIgnoreCase(str) || "TMK".equalsIgnoreCase(str)) {
            if (checkMyTmobileSignatureKey() || checkEchoAppSignatureKey()) {
                return true;
            }
            return false;
        } else if ("SPR".equalsIgnoreCase(str)) {
            return checkPackageSprintHubSignatureKey();
        } else {
            return false;
        }
    }

    private boolean checkPackageSignature(String str, Signature signature) {
        Signature[] apkContentsSigners;
        try {
            SigningInfo signingInfo = this.pm.getPackageInfo(str, PackageManager.PackageInfoFlags.of(134217728)).signingInfo;
            if (!(signingInfo == null || (apkContentsSigners = signingInfo.getApkContentsSigners()) == null)) {
                for (Signature equals : apkContentsSigners) {
                    if (equals.equals(signature)) {
                        return true;
                    }
                }
            }
        } catch (PackageManager.NameNotFoundException unused) {
            Log.i(LOG_TAG, str + " is not installed");
        }
        return false;
    }

    private boolean checkMyTmobileSignatureKey() {
        return checkPackageSignature("com.tmobile.pr.mytmobile", SIGNATURES_MY_TMOBILE);
    }

    private boolean checkEchoAppSignatureKey() {
        return checkPackageSignature("com.tmobile.echolocate", SIGNATURES_ECHO_APP);
    }

    private boolean checkPackageSprintHubSignatureKey() {
        return checkPackageSignature("com.sprint.ms.smf.services", SPRINT_HUB_SIGNATURES);
    }
}
