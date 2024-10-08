package com.sec.internal.ims.core.handler.secims;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.TransportInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.SemSystemProperties;
import android.telephony.CellIdentity;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityNr;
import android.telephony.CellInfo;
import android.telephony.ServiceState;
import android.text.TextUtils;
import android.util.Log;
import com.samsung.android.wifi.SemWifiManager;
import com.sec.ims.extensions.ConnectivityManagerExt;
import com.sec.ims.extensions.TelephonyManagerExt;
import com.sec.ims.settings.ImsProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.core.PaniConstants;
import com.sec.internal.constants.ims.gls.LocationInfo;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.ImsSharedPrefHelper;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.StrUtil;
import com.sec.internal.helper.os.CellIdentityWrapper;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.helper.os.ServiceStateWrapper;
import com.sec.internal.helper.os.TelephonyManagerWrapper;
import com.sec.internal.ims.core.PdnController$$ExternalSyntheticLambda0;
import com.sec.internal.ims.core.PdnController$$ExternalSyntheticLambda1;
import com.sec.internal.ims.core.cmc.CmcConstants;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.interfaces.ims.core.IGeolocationController;
import com.sec.internal.interfaces.ims.core.IPdnController;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.log.IMSLog;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

public class PaniGenerator {
    /* access modifiers changed from: private */
    public static final String LOG_TAG = "PaniGenerator";
    private static final String PLANIINTIME_PREF = "PLANIINTIME";
    protected static final String PLANI_PREF = "PLANI";
    static final List<Integer> SUPPORTED_LINK_SPEED_11A_G = Arrays.asList(new Integer[]{6, 9, 12, 18, 24, 36, 48, 54});
    static final List<Integer> SUPPORTED_LINK_SPEED_11B = Arrays.asList(new Integer[]{1, 2, 5, 11});
    boolean isSoftPhone;
    protected ConnectivityManager mConnectivityManager;
    /* access modifiers changed from: private */
    public Context mContext;
    private SimpleEventLog mEventLog;
    protected PaniGeneratorBase mGenerator;
    protected List<String> mLastPaniList = new ArrayList();
    /* access modifiers changed from: private */
    public IPdnController mPdnController;
    protected List<String> mPrevLastPaniList = new ArrayList();
    protected ITelephonyManager mTelephonyManager;

    public PaniGenerator(Context context, IPdnController iPdnController) {
        this.mContext = context;
        this.mPdnController = iPdnController;
        this.mTelephonyManager = TelephonyManagerWrapper.getInstance(context);
        this.mConnectivityManager = (ConnectivityManager) this.mContext.getSystemService(ConnectivityManager.class);
        this.isSoftPhone = SimUtil.isSoftphoneEnabled();
        this.mEventLog = new SimpleEventLog(this.mContext, LOG_TAG, 200);
        for (int i = 0; i < this.mTelephonyManager.getPhoneCount(); i++) {
            this.mPrevLastPaniList.add(i, ImsSharedPrefHelper.getString(i, this.mContext, ImsSharedPrefHelper.IMS_CONFIG, PLANI_PREF, (String) null));
            this.mLastPaniList.add(i, "");
        }
        this.mGenerator = new PaniGeneratorBase();
    }

    public long getCid(int i) {
        List<CellInfo> allCellInfo = getAllCellInfo(i);
        if (allCellInfo != null) {
            return ((Long) allCellInfo.stream().map(new PdnController$$ExternalSyntheticLambda0()).map(new PdnController$$ExternalSyntheticLambda1()).map(new PaniGenerator$$ExternalSyntheticLambda4()).findFirst().orElse(0L)).longValue();
        }
        IMSLog.e(LOG_TAG, i, "getCid: getAllCellInfo null");
        return 0;
    }

    public String generate(int i, String str, int i2, ImsProfile imsProfile) {
        int subId = SimUtil.getSubId(i2);
        int dataNetworkType = this.mTelephonyManager.getDataNetworkType(subId);
        String str2 = LOG_TAG;
        IMSLog.i(str2, i2, "generate: subId=" + subId + ", network=" + dataNetworkType + ", pdn=" + i);
        if (i == 15 && !this.isSoftPhone) {
            return generateEmergencyPani(str, i2, subId, dataNetworkType);
        }
        boolean z = false;
        boolean z2 = i == 1 || i == ConnectivityManagerExt.TYPE_WIFI_P2P;
        if (this.mPdnController.isEpdgConnected(i2) && (this.mPdnController.isWifiConnected() || this.mPdnController.getEpdgPhysicalInterface(i2) == 2)) {
            z = true;
        }
        if (z2 || z) {
            dataNetworkType = 18;
        } else if (dataNetworkType == 18 && !this.mPdnController.isEpdgConnected(i2) && (dataNetworkType = this.mTelephonyManager.getVoiceNetworkType(subId)) == 0) {
            dataNetworkType = this.mPdnController.getNetworkState(i2).getMobileDataNetworkType();
            IMSLog.i(str2, i2, "generate: mobileDataNetworkType=" + dataNetworkType);
        }
        if (dataNetworkType != 0) {
            return getGenerator(imsProfile).generate(dataNetworkType, str, i2);
        }
        Log.e(str2, "network is unknown.");
        return null;
    }

    private PaniGeneratorBase getGenerator(ImsProfile imsProfile) {
        if (imsProfile == null || imsProfile.getCmcType() == 0) {
            return this.mGenerator;
        }
        return new PaniGeneratorCmc();
    }

    private String generateEmergencyPani(String str, int i, int i2, int i3) {
        ServiceState serviceState;
        if (this.mPdnController.isEmergencyEpdgConnected(i)) {
            return this.mGenerator.generate(18, i);
        }
        if (i3 == 18 && (serviceState = this.mTelephonyManager.getServiceState(i2)) != null) {
            i3 = new ServiceStateWrapper(serviceState).getMobileDataNetworkType();
            String str2 = LOG_TAG;
            IMSLog.i(str2, i, "generateEmergencyPani: from mobile network=" + i3);
        }
        if (i2 == -1) {
            i3 = ImsUtil.getRatInNoSimCase(i, this.mContext);
        }
        PaniGeneratorBase paniGeneratorBase = this.mGenerator;
        if (i3 != 20) {
            i3 = 13;
        }
        return paniGeneratorBase.generate(i3, str, i);
    }

    protected class PaniGeneratorBase {
        protected static final String EDGE_PANI_FORMAT = "%s%04x%04x";
        protected static final String IWLAN_COUNTRY_TAG = "country=";
        protected static final String IWLAN_NODE_ID_TAG = "i-wlan-node-id=";
        protected static final String IWLAN_TIMESTAMP_TAG = "local-time-zone=";
        protected static final String LTE_PANI_FORMAT = "%s%04x%07x";
        protected static final String NR_PANI_FORMAT = "%s%06x%09x";
        protected static final String PREF_WIFI_PANI_COUNTRY_CODE_KEY = "wifiPaniCountryCode";
        protected static final String TEMPLATE_COUNTRY = "<COUNTRY>";
        protected static final String TEMPLATE_NODE_ID = "<NODE_ID>";
        protected static final String TEMPLATE_PREFIX = "<PREFIX>";
        protected static final String TEMPLATE_TIMESTAMP = "<TIMESTAMP>";
        protected static final String TEMPLATE_TYPED_PREFIX = "<TYPED_PREFIX>";

        protected PaniGeneratorBase() {
        }

        public String generate(int i, int i2) {
            return generate(i, (String) null, i2);
        }

        public String generate(int i, String str, int i2) {
            IMSLog.i(PaniGenerator.LOG_TAG, i2, "generate: network=" + i + ", fallbackPlmn=" + str);
            if (i == 18) {
                return generateWifiPani(i2);
            }
            CellIdentityWrapper currentCellIdentity = PaniGenerator.this.mPdnController.getCurrentCellIdentity(i2, i);
            String str2 = (String) Optional.ofNullable(getPsPlmn(i2, currentCellIdentity)).orElseGet(new PaniGenerator$PaniGeneratorBase$$ExternalSyntheticLambda0(this, i2));
            if (str2.length() < 5 || "00000".equals(str2) || "000000".equals(str2)) {
                Log.e(PaniGenerator.LOG_TAG, "generate: invalid network operator " + str2);
                if (TextUtils.isEmpty(str)) {
                    if (!CloudMessageProviderContract.JsonData.TRUE.equals(SemSystemProperties.get("persist.ril.ims.sipserverDebug", ConfigConstants.VALUE.INFO_COMPLETED))) {
                        return null;
                    }
                    ISimManager simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(i2);
                    if (simManagerFromSimSlot == null) {
                        str = "";
                    } else {
                        str = simManagerFromSimSlot.getSimOperator();
                    }
                    Log.e(PaniGenerator.LOG_TAG, "generate: use SIM operator " + str);
                }
            } else {
                Log.i(PaniGenerator.LOG_TAG, "generate: change to NW PLMN(" + str2 + ")");
                str = str2;
            }
            if (i == 13 && "TDD".equals(PaniGenerator.this.mTelephonyManager.getTelephonyProperty(i2, "ril.ltenetworktype", ""))) {
                i = 31;
            }
            if (!(i == 1 || i == 2)) {
                if (i != 3) {
                    if (i == 20) {
                        return generateNrPani(i2, str, currentCellIdentity);
                    }
                    if (i != 30) {
                        if (i == 31) {
                            return generateTdLtePani(i2, str, currentCellIdentity);
                        }
                        switch (i) {
                            case 8:
                            case 9:
                            case 10:
                                break;
                            default:
                                switch (i) {
                                    case 13:
                                        String generateLtePani = generateLtePani(i2, str, currentCellIdentity);
                                        if (!generateLtePani.isEmpty()) {
                                            return generateLtePani;
                                        }
                                        if (PaniGenerator.this.mTelephonyManager.getDataNetworkType(SimUtil.getSubId(i2)) != 0) {
                                            return generateLtePani;
                                        }
                                        PaniGenerator.this.mPdnController.getAllCellInfo(i2, true);
                                        return generateNrPani(i2, str, PaniGenerator.this.mPdnController.getCurrentCellIdentity(i2, 20));
                                    case 14:
                                        return generateEhrpdPani();
                                    case 15:
                                    case 17:
                                        break;
                                    case 16:
                                        break;
                                    default:
                                        Log.e(PaniGenerator.LOG_TAG, "PaniGenerator: Not supported network." + i);
                                        return "";
                                }
                        }
                    }
                }
                return generateUmtsPani(i2, str, currentCellIdentity);
            }
            return generateGeranPani(i2, str, currentCellIdentity);
        }

        /* access modifiers changed from: private */
        public /* synthetic */ String lambda$generate$0(int i) {
            return PaniGenerator.this.mTelephonyManager.getNetworkOperatorForPhone(i);
        }

        /* access modifiers changed from: protected */
        public String getPsPlmn(int i, CellIdentityWrapper cellIdentityWrapper) {
            if (cellIdentityWrapper.isCdma()) {
                String plmnFromCellInfo = getPlmnFromCellInfo(i);
                if (TextUtils.isEmpty(plmnFromCellInfo) || plmnFromCellInfo.length() < 5 || "00000".equals(plmnFromCellInfo) || "000000".equals(plmnFromCellInfo)) {
                    String r3 = PaniGenerator.LOG_TAG;
                    Log.e(r3, "getDataPlmn from RIL returns invalid dataPlmn: " + plmnFromCellInfo);
                } else {
                    try {
                        Integer.parseInt(plmnFromCellInfo);
                        String r32 = PaniGenerator.LOG_TAG;
                        Log.i(r32, "getDataPlmn returns " + plmnFromCellInfo);
                        return plmnFromCellInfo;
                    } catch (NumberFormatException e) {
                        String r33 = PaniGenerator.LOG_TAG;
                        Log.e(r33, "rePlmn by RIL is not guaranteed to be a numeric String. : " + e);
                        return null;
                    }
                }
            }
            return null;
        }

        private String getPlmnFromCellInfo(int i) {
            List<CellInfo> r3 = PaniGenerator.this.getAllCellInfo(i);
            if (r3 != null && !r3.isEmpty()) {
                for (CellInfo cellIdentity : r3) {
                    CellIdentity cellIdentity2 = cellIdentity.getCellIdentity();
                    if (((cellIdentity2 instanceof CellIdentityLte) || (cellIdentity2 instanceof CellIdentityNr)) && cellIdentity2.getMccString() != null && cellIdentity2.getMncString() != null) {
                        String str = cellIdentity2.getMccString() + cellIdentity2.getMncString();
                        IMSLog.i(PaniGenerator.LOG_TAG, i, "getPsPlmn : " + str);
                        return str;
                    }
                }
            }
            return "";
        }

        /* access modifiers changed from: protected */
        public String generateNrPani(int i, String str, CellIdentityWrapper cellIdentityWrapper) {
            if (!PaniGenerator.this.isValidInfo(cellIdentityWrapper)) {
                IMSLog.i(PaniGenerator.LOG_TAG, i, "Invalid tac or nrCid : return empty.");
                return "";
            }
            ISimManager simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(i);
            String str2 = PaniConstants.NR_PANI_PREFIX_TDD;
            if (simManagerFromSimSlot == null || !simManagerFromSimSlot.getSimMno().isKor()) {
                String telephonyProperty = PaniGenerator.this.mTelephonyManager.getTelephonyProperty(i, "ril.nrnetworktype", "");
                String r6 = PaniGenerator.LOG_TAG;
                Log.i(r6, "NR Access Type " + telephonyProperty);
                if (!"TDD".equals(telephonyProperty)) {
                    str2 = "FDD".equals(telephonyProperty) ? PaniConstants.NR_PANI_PREFIX_FDD : PaniConstants.NR_PANI_PREFIX;
                }
                return str2 + String.format(Locale.US, NR_PANI_FORMAT, new Object[]{str, Integer.valueOf(cellIdentityWrapper.getAreaCode()), Long.valueOf(cellIdentityWrapper.getCellId())});
            }
            return str2 + String.format(Locale.US, NR_PANI_FORMAT, new Object[]{str, Integer.valueOf(cellIdentityWrapper.getAreaCode()), Long.valueOf(cellIdentityWrapper.getCellId())});
        }

        /* access modifiers changed from: protected */
        public String generateLtePani(int i, String str, CellIdentityWrapper cellIdentityWrapper) {
            if (!PaniGenerator.this.isValidInfo(cellIdentityWrapper)) {
                IMSLog.i(PaniGenerator.LOG_TAG, i, "generateLtePani: Invalid Cell Id : return empty.");
                return "";
            }
            String format = String.format(Locale.US, LTE_PANI_FORMAT, new Object[]{str, Integer.valueOf(cellIdentityWrapper.getAreaCode()), Long.valueOf(cellIdentityWrapper.getCellId())});
            ISimManager simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(i);
            if (simManagerFromSimSlot == null || simManagerFromSimSlot.getSimMno() != Mno.MOVISTAR_PERU) {
                return PaniConstants.LTE_PANI_PREFIX + format;
            }
            return PaniConstants.LTE_PANI_PREFIX + format + ";" + generateCountryCode(i);
        }

        /* access modifiers changed from: protected */
        public String generateUmtsPani(int i, String str, CellIdentityWrapper cellIdentityWrapper) {
            ISimManager simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(i);
            if (simManagerFromSimSlot != null && simManagerFromSimSlot.getSimMno() == Mno.ORANGE && "7fffffff".equals(String.format("%07x", new Object[]{Long.valueOf(cellIdentityWrapper.getCellId())}))) {
                PaniGenerator.this.queryCellInfoForQualcomm();
            }
            if (!PaniGenerator.this.isValidInfo(cellIdentityWrapper)) {
                IMSLog.i(PaniGenerator.LOG_TAG, i, "generateUmtsPani: Invalid Cell Id : return empty.");
                return "";
            }
            return PaniConstants.UMTS_PANI_PREFIX + String.format(Locale.US, LTE_PANI_FORMAT, new Object[]{str, Integer.valueOf(cellIdentityWrapper.getAreaCode()), Long.valueOf(cellIdentityWrapper.getCellId())});
        }

        /* access modifiers changed from: protected */
        public String generateGeranPani(int i, String str, CellIdentityWrapper cellIdentityWrapper) {
            if (!PaniGenerator.this.isValidInfo(cellIdentityWrapper)) {
                IMSLog.i(PaniGenerator.LOG_TAG, i, "generateGeranPani: Invalid Cell Id : return empty.");
                return "";
            }
            return PaniConstants.EDGE_PANI_PREFIX + String.format(Locale.US, EDGE_PANI_FORMAT, new Object[]{str, Integer.valueOf(cellIdentityWrapper.getAreaCode()), Long.valueOf(cellIdentityWrapper.getCellId())});
        }

        /* access modifiers changed from: protected */
        public String generateTdLtePani(int i, String str, CellIdentityWrapper cellIdentityWrapper) {
            if (!PaniGenerator.this.isValidInfo(cellIdentityWrapper)) {
                IMSLog.i(PaniGenerator.LOG_TAG, i, "generateTdLtePani: Invalid Cell Id : return empty.");
                return "";
            }
            String str2 = PaniConstants.TDLTE_PANI_PREFIX + String.format(Locale.US, LTE_PANI_FORMAT, new Object[]{str, Integer.valueOf(cellIdentityWrapper.getAreaCode()), Long.valueOf(cellIdentityWrapper.getCellId())});
            ISimManager simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(i);
            if (simManagerFromSimSlot == null || simManagerFromSimSlot.getSimMno() != Mno.MOVISTAR_PERU) {
                return str2;
            }
            return str2 + ";" + generateCountryCode(i);
        }

        /* access modifiers changed from: protected */
        public String generateEhrpdPani() {
            byte[] currentUATI = TelephonyManagerExt.getCurrentUATI(PaniGenerator.this.mContext);
            if (currentUATI != null) {
                String r0 = PaniGenerator.LOG_TAG;
                Log.i(r0, "generateEhrpdPaniHeaderString(SectorId+SubnetLen) len= " + currentUATI.length);
                if (currentUATI.length == 17) {
                    byte[] bArr = new byte[16];
                    byte[] bArr2 = {currentUATI[0]};
                    System.arraycopy(currentUATI, 1, bArr, 0, 16);
                    String bytesToHexString = StrUtil.bytesToHexString(bArr);
                    String bytesToHexString2 = StrUtil.bytesToHexString(bArr2);
                    return PaniConstants.EHRPD_PANI_PREFIX + bytesToHexString + bytesToHexString2;
                }
            } else {
                Log.i(PaniGenerator.LOG_TAG, "Got NULL UATI from RIL!!!");
            }
            return null;
        }

        /* access modifiers changed from: protected */
        public String generateWifiPani(int i) {
            String wifiPaniFormatFromSetting = getWifiPaniFormatFromSetting(i);
            if (PaniGenerator.this.isSoftPhone) {
                wifiPaniFormatFromSetting = wifiPaniFormatFromSetting.replace(TEMPLATE_PREFIX, "<PREFIX><COUNTRY>");
            }
            String replace = wifiPaniFormatFromSetting.replace("><", ">;<");
            IMSLog.i(PaniGenerator.LOG_TAG, i, "generateWiFiPani: Format for generating PANI - " + replace);
            ISimManager simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(i);
            String generateCountryCode = generateCountryCode(i);
            if (simManagerFromSimSlot != null && simManagerFromSimSlot.getSimMno() == Mno.LIFECELL_UA) {
                if (!TextUtils.isEmpty(generateCountryCode)) {
                    ImsSharedPrefHelper.save(i, PaniGenerator.this.mContext, ImsSharedPrefHelper.IMS_USER_DATA, PREF_WIFI_PANI_COUNTRY_CODE_KEY, generateCountryCode);
                } else {
                    generateCountryCode = ImsSharedPrefHelper.getString(i, PaniGenerator.this.mContext, ImsSharedPrefHelper.IMS_USER_DATA, PREF_WIFI_PANI_COUNTRY_CODE_KEY, "");
                    IMSLog.i(PaniGenerator.LOG_TAG, i, "countryCode read from sharedPrefs: " + generateCountryCode);
                }
            }
            String replace2 = replace.replace(TEMPLATE_PREFIX, PaniConstants.IWLAN_PANI_PREFIX).replace(TEMPLATE_NODE_ID, generateIwlanNodeId(i)).replace(TEMPLATE_COUNTRY, generateCountryCode).replace(TEMPLATE_TIMESTAMP, generateTimeStamp(i));
            if (replace2.contains(TEMPLATE_TYPED_PREFIX)) {
                replace2 = replace2.replace(TEMPLATE_TYPED_PREFIX, PaniConstants.IWLAN_PANI_PREFIX + PaniGenerator.this.getWiFiStandard());
            }
            while (replace2.contains(";;")) {
                replace2 = replace2.replace(";;", ";");
            }
            int length = replace2.length() - 1;
            if (replace2.charAt(length) == ';') {
                replace2 = replace2.substring(0, length);
            }
            IMSLog.i(PaniGenerator.LOG_TAG, i, "generateWiFiPani: normalized PANI: " + replace2);
            return replace2;
        }

        /* access modifiers changed from: protected */
        public String getWifiPaniFormatFromSetting(int i) {
            String string = ImsRegistry.getString(i, GlobalSettingsConstants.Registration.IWLAN_PANI_FORMAT, PaniConstants.DEFAULT_IWLAN_PANI_FORMAT);
            if (!TextUtils.isEmpty(string)) {
                return string;
            }
            return PaniConstants.DEFAULT_IWLAN_PANI_FORMAT;
        }

        /* access modifiers changed from: protected */
        public String generateIwlanNodeId(int i) {
            String str;
            if (PaniGenerator.this.mPdnController.getEpdgPhysicalInterface(i) == 2) {
                str = getLocalMac();
            } else {
                str = getWifiBssid();
            }
            if (str == null) {
                return "";
            }
            return IWLAN_NODE_ID_TAG + str.replace(":", "");
        }

        /* access modifiers changed from: protected */
        public String generateTimeStamp(int i) {
            ISimManager simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(i);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat((simManagerFromSimSlot == null || simManagerFromSimSlot.getSimMno() != Mno.TMOUS) ? "yyyy-MM-dd'T'HH:mm:ssZ" : "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US);
            return "local-time-zone=\"" + simpleDateFormat.format(new Date()) + CmcConstants.E_NUM_STR_QUOTE;
        }

        /* access modifiers changed from: protected */
        public String generateCountryCode(int i) {
            String str;
            IGeolocationController geolocationController = ImsRegistry.getGeolocationController();
            if (geolocationController != null) {
                LocationInfo geolocation = geolocationController.getGeolocation();
                if (geolocation != null) {
                    str = geolocation.mCountry;
                } else {
                    str = "";
                }
                if (!TextUtils.isEmpty(str)) {
                    return IWLAN_COUNTRY_TAG + str.toUpperCase(Locale.US);
                } else if (SimUtil.getMno(i) == Mno.TMOUS) {
                    String lastAccessedNetworkCountryIso = geolocationController.getLastAccessedNetworkCountryIso(i);
                    String r1 = PaniGenerator.LOG_TAG;
                    IMSLog.i(r1, i, "generateCountryCode: last accessed network country iso: " + lastAccessedNetworkCountryIso);
                    if (!TextUtils.isEmpty(lastAccessedNetworkCountryIso)) {
                        return IWLAN_COUNTRY_TAG + lastAccessedNetworkCountryIso.toUpperCase(Locale.US);
                    }
                }
            }
            if (SimUtil.getMno(i).isAus()) {
                return "country=XX";
            }
            return "";
        }

        private String getLocalMac() {
            SemWifiManager semWifiManager = (SemWifiManager) PaniGenerator.this.mContext.getSystemService("sem_wifi");
            if (semWifiManager != null) {
                return semWifiManager.getFactoryMacAddress();
            }
            return null;
        }

        private String getWifiBssid() {
            WifiInfo connectionInfo;
            WifiManager wifiManager = (WifiManager) PaniGenerator.this.mContext.getApplicationContext().getSystemService("wifi");
            if (wifiManager == null || (connectionInfo = wifiManager.getConnectionInfo()) == null) {
                return null;
            }
            String bssid = connectionInfo.getBSSID();
            String r0 = PaniGenerator.LOG_TAG;
            Log.i(r0, "WifiManager.getBSSID(): [" + bssid + "]");
            return bssid;
        }
    }

    protected class PaniGeneratorCmc extends PaniGeneratorBase {
        /* access modifiers changed from: protected */
        public String getWifiPaniFormatFromSetting(int i) {
            return PaniConstants.DEFAULT_IWLAN_PANI_FORMAT;
        }

        protected PaniGeneratorCmc() {
            super();
        }
    }

    public boolean isChangedPlani(int i, String str) {
        String previoutPlani = getPrevioutPlani(i);
        String str2 = LOG_TAG;
        IMSLog.s(str2, i, "isChangedPlani: prev plani " + previoutPlani + ", curr plani " + str);
        if (TextUtils.equals(previoutPlani, str)) {
            return false;
        }
        setPrevioutPlani(i, str);
        return true;
    }

    public void removePreviousPlani(int i) {
        String previoutPlani = getPrevioutPlani(i);
        String str = LOG_TAG;
        IMSLog.s(str, i, "removePreviousPlani: prev plani " + previoutPlani);
        setPrevioutPlani(i, "");
    }

    public void setTimeInPlani(int i, long j) {
        ImsSharedPrefHelper.save(i, this.mContext, ImsSharedPrefHelper.USER_CONFIG, PLANIINTIME_PREF, j);
        String str = LOG_TAG;
        IMSLog.s(str, i, "setTimeInPlani: " + j);
    }

    public long getTimeInPlani(int i) {
        long j = ImsSharedPrefHelper.getLong(i, this.mContext, ImsSharedPrefHelper.USER_CONFIG, PLANIINTIME_PREF, 0);
        String str = LOG_TAG;
        IMSLog.s(str, i, "getTimeInPlani: " + j);
        return j;
    }

    public boolean needCellInfoAge(ImsProfile imsProfile) {
        return imsProfile != null && TextUtils.equals(imsProfile.getLastPaniHeader(), PaniConstants.HEADER_CELL_NET_INFO_CIA);
    }

    public boolean needCellInfoAgeInactive(ImsProfile imsProfile) {
        return imsProfile != null && TextUtils.equals(imsProfile.getLastPaniHeader(), PaniConstants.HEADER_CELL_NET_INFO_CIA_INACTIVE);
    }

    public boolean needTimeStampForLastPani(ImsProfile imsProfile) {
        return (imsProfile == null || TextUtils.equals(imsProfile.getLastPaniHeader(), PaniConstants.HEADER_CELL_NET_INFO_CIA) || Mno.fromName(imsProfile.getMnoName()) == Mno.BOG) ? false : true;
    }

    /* access modifiers changed from: protected */
    public void queryCellInfoForQualcomm() {
        String str = LOG_TAG;
        IMSLog.i(str, "queryCellInfoForQualcomm");
        if (this.mTelephonyManager.getAllCellInfo() == null) {
            IMSLog.i(str, "cellInfo is null.");
        }
    }

    public void setLkcForLastPani(int i, String str, ImsProfile imsProfile, Date date) {
        String str2;
        if (TextUtils.isEmpty(imsProfile.getLastPaniHeader())) {
            String str3 = LOG_TAG;
            Log.i(str3, "setLkcForLastPani: No Last PANI header for " + imsProfile.getName());
        } else if (!isValidPani(str, i)) {
            Log.i(LOG_TAG, "setLkcForLastPani: current PANI is not valid!");
        } else {
            if (!str.contains(PaniConstants.IWLAN_PANI_PREFIX)) {
                storeLastPani(i, str);
            } else {
                String generate = this.mGenerator.generate(this.mPdnController.getNetworkState(i).getMobileDataNetworkType(), i);
                if (!isValidPani(generate, i)) {
                    Log.i(LOG_TAG, "setLkcForLastPani: underlyingCellularPani is not valid!");
                    return;
                }
                storeLastPani(i, generate);
            }
            String storedLastPani = getStoredLastPani(i);
            if (needTimeStampForLastPani(imsProfile) && !TextUtils.isEmpty(storedLastPani)) {
                Mno fromName = Mno.fromName(imsProfile.getMnoName());
                if (fromName == Mno.TMOUS) {
                    str2 = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
                } else {
                    str2 = fromName == Mno.CELLC_SOUTHAFRICA ? "yyyy-MM-dd'T'HH:mm:ssZZZZZ" : "yyyy-MM-dd'T'HH:mm:ssZ";
                }
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(str2, Locale.US);
                storeLastPani(i, storedLastPani + ";\"" + simpleDateFormat.format(date) + CmcConstants.E_NUM_STR_QUOTE);
            }
            String storedLastPani2 = getStoredLastPani(i);
            ImsSharedPrefHelper.save(i, this.mContext, ImsSharedPrefHelper.IMS_CONFIG, PLANI_PREF, storedLastPani2);
            String str4 = LOG_TAG;
            IMSLog.s(str4, i, "setLkcForLastPani: " + storedLastPani2);
        }
    }

    public String getLastPani(int i, ImsProfile imsProfile, Date date) {
        String str;
        if (TextUtils.isEmpty(getStoredLastPani(i))) {
            String string = ImsSharedPrefHelper.getString(i, this.mContext, ImsSharedPrefHelper.IMS_CONFIG, PLANI_PREF, (String) null);
            if (TextUtils.isEmpty(string)) {
                return string;
            }
            storeLastPani(i, string);
        }
        String storedLastPani = getStoredLastPani(i);
        boolean needTimeStampForLastPani = needTimeStampForLastPani(imsProfile);
        Mno fromName = Mno.fromName(imsProfile.getMnoName());
        if (fromName == Mno.TMOUS) {
            int subId = SimUtil.getSubId(i);
            if (this.mTelephonyManager.getVoiceNetworkType(subId) != 0 && this.mTelephonyManager.getServiceStateForSubscriber(subId) == 0) {
                storedLastPani = storedLastPani.replaceAll(";\"\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.*$", "");
                needTimeStampForLastPani = false;
            }
        }
        if (needTimeStampForLastPani) {
            if (fromName == Mno.TMOUS) {
                str = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
            } else {
                str = fromName == Mno.CELLC_SOUTHAFRICA ? "yyyy-MM-dd'T'HH:mm:ssZZZZZ" : "yyyy-MM-dd'T'HH:mm:ssZ";
            }
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(str, Locale.US);
            storedLastPani = storedLastPani + ";\"" + simpleDateFormat.format(date) + CmcConstants.E_NUM_STR_QUOTE;
        }
        IMSLog.s(LOG_TAG, i, "getLastPani: " + storedLastPani);
        return storedLastPani;
    }

    /* access modifiers changed from: protected */
    public boolean isValidPani(String str, int i) {
        if (TextUtils.isEmpty(str)) {
            return false;
        }
        int subId = SimUtil.getSubId(i);
        int dataNetworkType = this.mTelephonyManager.getDataNetworkType(subId);
        int serviceStateForSubscriber = this.mTelephonyManager.getServiceStateForSubscriber(subId);
        int dataServiceState = this.mTelephonyManager.getDataServiceState(subId);
        String str2 = LOG_TAG;
        IMSLog.s(str2, i, "isValidPani: PANI [" + str + "] network [" + dataNetworkType + "] voiceSvcState [" + serviceStateForSubscriber + "] dataSvcState [" + dataServiceState + "]");
        if (!str.contains(PaniConstants.IWLAN_PANI_PREFIX)) {
            if (dataNetworkType == 18) {
                if (serviceStateForSubscriber != 0) {
                    return false;
                }
            } else if (dataServiceState != 0) {
                return false;
            }
            return true;
        } else if (serviceStateForSubscriber != 0) {
            return false;
        } else {
            return str.contains("i-wlan-node-id=");
        }
    }

    /* access modifiers changed from: package-private */
    public String getWiFiStandard() {
        Optional ofNullable = Optional.ofNullable(this.mConnectivityManager.getActiveNetwork());
        ConnectivityManager connectivityManager = this.mConnectivityManager;
        Objects.requireNonNull(connectivityManager);
        return (String) ofNullable.map(new PaniGenerator$$ExternalSyntheticLambda0(connectivityManager)).map(new PaniGenerator$$ExternalSyntheticLambda1()).map(new PaniGenerator$$ExternalSyntheticLambda2()).map(new PaniGenerator$$ExternalSyntheticLambda3(this)).orElse("");
    }

    /* access modifiers changed from: private */
    public static /* synthetic */ WifiInfo lambda$getWiFiStandard$0(TransportInfo transportInfo) {
        return (WifiInfo) transportInfo;
    }

    /* access modifiers changed from: private */
    public String convertToCharacter(WifiInfo wifiInfo) {
        int wifiStandard = wifiInfo.getWifiStandard();
        String str = LOG_TAG;
        IMSLog.i(str, "getWiFiStandard: " + wifiStandard);
        if (wifiStandard != 1) {
            switch (wifiStandard) {
                case 4:
                    return "n";
                case 5:
                    return "ac";
                case 6:
                    return "ax";
                case 7:
                    return "ad";
                case 8:
                    return "be";
                default:
                    return "";
            }
        } else {
            int frequency = wifiInfo.getFrequency() / 1000;
            int linkSpeed = wifiInfo.getLinkSpeed();
            IMSLog.i(str, String.format(Locale.US, "getWiFiStandard: freq %dghz, %dmbps", new Object[]{Integer.valueOf(frequency), Integer.valueOf(linkSpeed)}));
            if (SUPPORTED_LINK_SPEED_11B.contains(Integer.valueOf(linkSpeed))) {
                return "b";
            }
            if (SUPPORTED_LINK_SPEED_11A_G.contains(Integer.valueOf(linkSpeed))) {
                return frequency == 5 ? "a" : "g";
            }
            return "";
        }
    }

    private String getPrevioutPlani(int i) {
        try {
            return this.mPrevLastPaniList.get(i);
        } catch (IndexOutOfBoundsException unused) {
            return "";
        }
    }

    private String getStoredLastPani(int i) {
        try {
            return this.mLastPaniList.get(i);
        } catch (IndexOutOfBoundsException unused) {
            return "";
        }
    }

    private void setPrevioutPlani(int i, String str) {
        try {
            this.mPrevLastPaniList.set(i, str);
        } catch (IndexOutOfBoundsException unused) {
            IMSLog.s(LOG_TAG, i, "setPrevioutPlani: IndexOutOfBoundsException");
        }
    }

    private void storeLastPani(int i, String str) {
        try {
            this.mLastPaniList.set(i, str);
        } catch (IndexOutOfBoundsException unused) {
            IMSLog.s(LOG_TAG, i, "storeLastPani: IndexOutOfBoundsException");
        }
    }

    /* access modifiers changed from: private */
    public List<CellInfo> getAllCellInfo(int i) {
        return this.mPdnController.getAllCellInfo(i, false);
    }

    /* access modifiers changed from: private */
    public boolean isValidInfo(CellIdentityWrapper cellIdentityWrapper) {
        boolean isValid = cellIdentityWrapper.isValid();
        if (!isValid) {
            SimpleEventLog simpleEventLog = this.mEventLog;
            simpleEventLog.logAndAdd("strangeCellInfoDiscovered : " + cellIdentityWrapper);
        }
        return isValid;
    }

    public void dump() {
        IMSLog.dump(LOG_TAG, "Dump of PaniGenerator:");
        this.mEventLog.dump();
    }
}
