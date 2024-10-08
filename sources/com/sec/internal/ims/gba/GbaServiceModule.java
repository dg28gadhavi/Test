package com.sec.internal.ims.gba;

import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.os.RemoteException;
import android.telephony.gba.GbaAuthRequest;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.util.SparseArray;
import com.sec.internal.constants.Mno;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.StrUtil;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.helper.os.TelephonyManagerWrapper;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.gba.params.GbaData;
import com.sec.internal.ims.servicemodules.base.ServiceModuleBase;
import com.sec.internal.ims.servicemodules.ss.UtUtils;
import com.sec.internal.interfaces.ims.IImsFramework;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.interfaces.ims.gba.IGbaCallback;
import com.sec.internal.interfaces.ims.gba.IGbaServiceModule;
import com.sec.internal.log.IMSLog;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class GbaServiceModule extends ServiceModuleBase implements IGbaServiceModule {
    private static final String GBA_ME = "gba-me";
    public static final String GBA_UICC = "gba-u";
    private static final String IMS_AUTH_NO_ERR_STRING = "db";
    private static final String IMS_AUTH_SYNC_FAIL = "dc";
    private static final String LOG_TAG = GbaServiceModule.class.getSimpleName();
    private static byte[] gbaKey = null;
    private static int mGbaIdCounter = 0;
    private Context mContext;
    private Gba mGba;
    private SparseArray<IGbaCallback> mGbaCallbacks = new SparseArray<>();
    private IImsFramework mImsFramework;
    private ITelephonyManager mTelephonyManager;
    private int resLen = 0;

    public void handleIntent(Intent intent) {
    }

    public void init() {
        super.init();
        super.start();
    }

    public String[] getServicesRequiring() {
        return new String[]{"ss", "mmtel", "ft_http"};
    }

    public GbaServiceModule(Looper looper, Context context, IImsFramework iImsFramework) {
        super(looper);
        this.mContext = context;
        this.mImsFramework = iImsFramework;
        this.mTelephonyManager = TelephonyManagerWrapper.getInstance(context);
        initGbaAccessibleObj();
    }

    public IGbaCallback getGbaCallback(int i) {
        return this.mGbaCallbacks.get(i);
    }

    public void removeGbaCallback(int i) {
        this.mGbaCallbacks.remove(i);
    }

    public void storeGbaBootstrapParams(int i, byte[] bArr, String str, String str2) {
        if (this.mTelephonyManager != null) {
            String str3 = LOG_TAG;
            IMSLog.s(str3, "rand :" + StrUtil.bytesToHexString(bArr) + " btid :" + str + " keyLifetime :" + str2);
            this.mTelephonyManager.setGbaBootstrappingParams(i, Arrays.copyOfRange(bArr, 1, 17), str, str2);
        }
    }

    public String transmitLogicChannel(int i, String str, String str2, int i2) throws RemoteException {
        int iccOpenLogicalChannelAndGetChannel = this.mTelephonyManager.iccOpenLogicalChannelAndGetChannel(i, str);
        String iccTransmitApduLogicalChannel = this.mTelephonyManager.iccTransmitApduLogicalChannel(i, iccOpenLogicalChannelAndGetChannel, 2, 136, 0, 132, i2, str2);
        this.mTelephonyManager.iccCloseLogicalChannel(i, iccOpenLogicalChannelAndGetChannel);
        return iccTransmitApduLogicalChannel;
    }

    public String parseResKeyFromIsimResponse(byte[] bArr) {
        String bytesToHexString = StrUtil.bytesToHexString(bArr);
        if (bytesToHexString != null) {
            String str = LOG_TAG;
            IMSLog.s(str, "AkaResponse for GBA as received from sim: " + bytesToHexString);
            if (("" + bytesToHexString.charAt(0) + bytesToHexString.charAt(1)).equalsIgnoreCase(IMS_AUTH_NO_ERR_STRING)) {
                int parseInt = Integer.parseInt(bytesToHexString.substring(2, 4), 16);
                this.resLen = parseInt;
                if (2 >= parseInt) {
                    Log.i(str, "Illegal response received from iSim");
                    return null;
                }
            }
            int i = this.resLen;
            byte[] bArr2 = new byte[i];
            try {
                System.arraycopy(bArr, 2, bArr2, 0, i);
                String encodeToString = Base64.encodeToString(bArr2, 2);
                IMSLog.s(str, "AkaResponse for GBA to be sent: " + StrUtil.bytesToHexString(bArr2) + " base64 decode : " + encodeToString);
                return encodeToString;
            } catch (IndexOutOfBoundsException e) {
                String str2 = LOG_TAG;
                Log.e(str2, "ArrayIndexOutOfBoundsException: " + e.getMessage());
            }
        }
        return null;
    }

    public String getBtidFromSim(int i) {
        String btid = this.mTelephonyManager.getBtid(i);
        String str = LOG_TAG;
        IMSLog.s(str, "getBtid " + btid);
        return btid;
    }

    public String getKeyLifetime(int i) {
        return this.mTelephonyManager.getKeyLifetime(i);
    }

    public String getPrivateUserIdentityfromIsim(int i, ITelephonyManager iTelephonyManager, ISimManager iSimManager) {
        Mno simMno = SimUtil.getSimMno(i);
        int subId = SimUtil.getSubId(i);
        if (subId < 0) {
            return "";
        }
        String isimImpi = iTelephonyManager.getIsimImpi(subId);
        if (TextUtils.isEmpty(isimImpi)) {
            return isimImpi;
        }
        if (simMno.isOneOf(Mno.EE, Mno.EE_ESN) || simMno.isKor()) {
            String[] isimImpu = iTelephonyManager.getIsimImpu(subId);
            String isimDomain = iTelephonyManager.getIsimDomain(subId);
            boolean z = false;
            if (isimImpu != null) {
                int length = isimImpu.length;
                int i2 = 0;
                while (true) {
                    if (i2 >= length) {
                        break;
                    } else if (!TextUtils.isEmpty(isimImpu[i2])) {
                        z = true;
                        break;
                    } else {
                        i2++;
                    }
                }
            }
            String str = LOG_TAG;
            IMSLog.i(str, i, "getPrivateUserIdentityfromIsim: MNO=" + simMno + ", found impu=" + z + ", domain=" + isimDomain + ", impi=" + IMSLog.checker(isimImpi));
            if (simMno.isKor() && !z) {
                return "";
            }
            if (!z || TextUtils.isEmpty(isimDomain) || TextUtils.isEmpty(isimImpi)) {
                IMSLog.i(str, i, "getPrivateUserIdentityfromIsim: domain is null | impi is null | impu Not found");
                return "";
            }
        }
        return isimImpi;
    }

    public String getImpi(int i) {
        ISimManager simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(i);
        String str = "";
        if (simManagerFromSimSlot == null) {
            return str;
        }
        if (simManagerFromSimSlot.hasIsim()) {
            str = getPrivateUserIdentityfromIsim(i, this.mTelephonyManager, simManagerFromSimSlot);
        }
        String str2 = LOG_TAG;
        IMSLog.i(str2, "getImpi after isim impi = " + IMSLog.checker(str));
        return TextUtils.isEmpty(str) ? simManagerFromSimSlot.getDerivedImpi() : str;
    }

    public String getImei(int i) {
        ITelephonyManager iTelephonyManager = this.mTelephonyManager;
        if (iTelephonyManager == null) {
            return null;
        }
        return iTelephonyManager.getImei(i);
    }

    public boolean initGbaAccessibleObj() {
        this.mGba = new Gba();
        return true;
    }

    public boolean isGbaUiccSupported(int i) {
        return this.mTelephonyManager.isGbaSupported(i);
    }

    public synchronized int getBtidAndGbaKey(GbaAuthRequest gbaAuthRequest, IGbaCallback iGbaCallback) {
        String nafUrl;
        HttpRequestParams httpRequestParams;
        Log.i(LOG_TAG, "GBA: getBtidAndGbaKey GbaAuthRequest");
        int slotId = SimManagerFactory.getSlotId(gbaAuthRequest.getSubId());
        Mno simMno = SimUtil.getSimMno(slotId);
        nafUrl = GbaUtility.getNafUrl(gbaAuthRequest.getNafUrl().toString());
        httpRequestParams = new HttpRequestParams();
        httpRequestParams.setPhoneId(slotId);
        httpRequestParams.setMethod(HttpRequestParams.Method.GET);
        httpRequestParams.setConnectionTimeout(5000);
        httpRequestParams.setBsfUrl(UtUtils.getBSFDomain(slotId));
        httpRequestParams.setUrl(gbaAuthRequest.getNafUrl().toSafeString());
        if (simMno.isOneOf(Mno.TMOUS, Mno.DISH)) {
            httpRequestParams.setUseImei(true);
        }
        if (gbaAuthRequest.getSecurityProtocol().length != 0) {
            if (gbaAuthRequest.getSecurityProtocol().length == 5) {
                byte[] securityProtocol = gbaAuthRequest.getSecurityProtocol();
                httpRequestParams.setCipherSuite(new byte[]{securityProtocol[3], securityProtocol[4]});
            } else {
                httpRequestParams.setCipherSuite(gbaAuthRequest.getSecurityProtocol());
            }
        }
        return getBtidAndGbaKey(httpRequestParams, nafUrl, (HttpResponseParams) null, iGbaCallback);
    }

    /* JADX WARNING: Removed duplicated region for block: B:29:0x0072  */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x007b  */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x00a1  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized int getBtidAndGbaKey(com.sec.internal.helper.httpclient.HttpRequestParams r18, java.lang.String r19, com.sec.internal.helper.httpclient.HttpResponseParams r20, com.sec.internal.interfaces.ims.gba.IGbaCallback r21) {
        /*
            r17 = this;
            r1 = r17
            r0 = r18
            r2 = r19
            r3 = r21
            monitor-enter(r17)
            java.lang.String r4 = LOG_TAG     // Catch:{ all -> 0x012a }
            java.lang.String r5 = "GBA: getBtidAndGbaKey"
            android.util.Log.i(r4, r5)     // Catch:{ all -> 0x012a }
            r8 = -1
            if (r3 != 0) goto L_0x0015
            monitor-exit(r17)
            return r8
        L_0x0015:
            com.sec.internal.helper.os.ITelephonyManager r5 = r1.mTelephonyManager     // Catch:{ all -> 0x012a }
            if (r5 != 0) goto L_0x0023
            android.content.Context r0 = r1.mContext     // Catch:{ all -> 0x012a }
            com.sec.internal.helper.os.ITelephonyManager r0 = com.sec.internal.helper.os.TelephonyManagerWrapper.getInstance(r0)     // Catch:{ all -> 0x012a }
            r1.mTelephonyManager = r0     // Catch:{ all -> 0x012a }
            monitor-exit(r17)
            return r8
        L_0x0023:
            int r5 = r18.getPhoneId()     // Catch:{ all -> 0x012a }
            int r6 = com.sec.internal.helper.SimUtil.getSubId(r5)     // Catch:{ all -> 0x012a }
            com.sec.internal.helper.os.ITelephonyManager r7 = r1.mTelephonyManager     // Catch:{ all -> 0x012a }
            boolean r7 = r7.isGbaSupported(r6)     // Catch:{ all -> 0x012a }
            com.sec.internal.constants.Mno r9 = com.sec.internal.helper.SimUtil.getSimMno(r5)     // Catch:{ all -> 0x012a }
            java.lang.String r10 = "3GPP-bootstrapping"
            boolean r10 = r2.contains(r10)     // Catch:{ all -> 0x012a }
            r11 = 2
            r12 = 1
            r13 = 0
            if (r10 == 0) goto L_0x0065
            java.lang.String r10 = "uicc"
            boolean r10 = r2.contains(r10)     // Catch:{ all -> 0x012a }
            if (r10 == 0) goto L_0x004d
            if (r7 == 0) goto L_0x004d
            r7 = r12
            goto L_0x004e
        L_0x004d:
            r7 = r13
        L_0x004e:
            java.lang.String r2 = com.sec.internal.ims.gba.GbaUtility.getNafId(r19)     // Catch:{ all -> 0x012a }
            com.sec.internal.constants.Mno[] r10 = new com.sec.internal.constants.Mno[r11]     // Catch:{ all -> 0x012a }
            com.sec.internal.constants.Mno r14 = com.sec.internal.constants.Mno.TMOUS     // Catch:{ all -> 0x012a }
            r10[r13] = r14     // Catch:{ all -> 0x012a }
            com.sec.internal.constants.Mno r14 = com.sec.internal.constants.Mno.DISH     // Catch:{ all -> 0x012a }
            r10[r12] = r14     // Catch:{ all -> 0x012a }
            boolean r9 = r9.isOneOf(r10)     // Catch:{ all -> 0x012a }
            if (r9 == 0) goto L_0x0063
            goto L_0x0065
        L_0x0063:
            r10 = r7
            goto L_0x0066
        L_0x0065:
            r10 = r13
        L_0x0066:
            java.nio.charset.Charset r7 = java.nio.charset.StandardCharsets.UTF_8     // Catch:{ all -> 0x012a }
            byte[] r9 = r2.getBytes(r7)     // Catch:{ all -> 0x012a }
            int r14 = r17.createRequestId()     // Catch:{ all -> 0x012a }
            if (r10 == 0) goto L_0x007b
            java.lang.String r2 = "gba-u"
            java.nio.charset.Charset r7 = java.nio.charset.StandardCharsets.UTF_8     // Catch:{ all -> 0x012a }
            byte[] r2 = r2.getBytes(r7)     // Catch:{ all -> 0x012a }
            goto L_0x0083
        L_0x007b:
            java.lang.String r2 = "gba-me"
            java.nio.charset.Charset r7 = java.nio.charset.StandardCharsets.UTF_8     // Catch:{ all -> 0x012a }
            byte[] r2 = r2.getBytes(r7)     // Catch:{ all -> 0x012a }
        L_0x0083:
            r15 = r2
            java.lang.String r2 = "GBA: NO GBA information, need send BSF request"
            android.util.Log.i(r4, r2)     // Catch:{ all -> 0x012a }
            android.util.SparseArray<com.sec.internal.interfaces.ims.gba.IGbaCallback> r2 = r1.mGbaCallbacks     // Catch:{ all -> 0x012a }
            r2.put(r14, r3)     // Catch:{ all -> 0x012a }
            r0.setToken(r14)     // Catch:{ all -> 0x012a }
            java.lang.String r4 = r18.getBsfUrl()     // Catch:{ all -> 0x012a }
            com.sec.internal.interfaces.ims.IImsFramework r2 = r1.mImsFramework     // Catch:{ all -> 0x012a }
            java.lang.String r7 = "bsf_port"
            r13 = 80
            int r7 = r2.getInt(r5, r7, r13)     // Catch:{ all -> 0x012a }
            if (r4 == 0) goto L_0x011d
            if (r7 >= 0) goto L_0x00a5
            goto L_0x011d
        L_0x00a5:
            com.sec.internal.helper.os.ITelephonyManager r2 = r1.mTelephonyManager     // Catch:{ all -> 0x012a }
            java.lang.String r8 = r2.getImei(r5)     // Catch:{ all -> 0x012a }
            com.sec.internal.helper.os.ITelephonyManager r2 = r1.mTelephonyManager     // Catch:{ all -> 0x012a }
            java.lang.String r2 = r2.getIsimDomain(r6)     // Catch:{ all -> 0x012a }
            com.sec.internal.helper.os.ITelephonyManager r3 = r1.mTelephonyManager     // Catch:{ all -> 0x012a }
            java.lang.String r3 = r3.getIsimDomain(r6)     // Catch:{ all -> 0x012a }
            java.lang.String r6 = r1.getImpi(r5)     // Catch:{ all -> 0x012a }
            com.sec.internal.interfaces.ims.core.ISimManager r13 = com.sec.internal.ims.core.sim.SimManagerFactory.getSimManagerFromSimSlot(r5)     // Catch:{ all -> 0x012a }
            if (r13 != 0) goto L_0x00c3
            r13 = 0
            goto L_0x00c7
        L_0x00c3:
            boolean r13 = r13.hasIsim()     // Catch:{ all -> 0x012a }
        L_0x00c7:
            boolean r16 = android.text.TextUtils.isEmpty(r2)     // Catch:{ all -> 0x012a }
            if (r16 != 0) goto L_0x00cf
            if (r13 != 0) goto L_0x00dd
        L_0x00cf:
            boolean r13 = android.text.TextUtils.isEmpty(r6)     // Catch:{ all -> 0x012a }
            if (r13 != 0) goto L_0x00dd
            java.lang.String r2 = "@"
            java.lang.String[] r2 = r6.split(r2, r11)     // Catch:{ all -> 0x012a }
            r2 = r2[r12]     // Catch:{ all -> 0x012a }
        L_0x00dd:
            boolean r5 = r1.isRealmFromUsername(r5)     // Catch:{ all -> 0x012a }
            if (r5 == 0) goto L_0x00e5
            r11 = r2
            goto L_0x00f7
        L_0x00e5:
            boolean r2 = android.text.TextUtils.isEmpty(r3)     // Catch:{ all -> 0x012a }
            if (r2 != 0) goto L_0x00f6
            java.lang.String r2 = "bsf"
            boolean r2 = r3.startsWith(r2)     // Catch:{ all -> 0x012a }
            if (r2 != 0) goto L_0x00f4
            goto L_0x00f6
        L_0x00f4:
            r11 = r3
            goto L_0x00f7
        L_0x00f6:
            r11 = r4
        L_0x00f7:
            if (r20 == 0) goto L_0x010c
            boolean r2 = r18.getUseTls()     // Catch:{ all -> 0x012a }
            if (r2 == 0) goto L_0x010c
            byte[] r2 = r20.getCipherSuite()     // Catch:{ all -> 0x012a }
            if (r2 == 0) goto L_0x010c
            byte[] r2 = r20.getCipherSuite()     // Catch:{ all -> 0x012a }
            r0.setCipherSuite(r2)     // Catch:{ all -> 0x012a }
        L_0x010c:
            com.sec.internal.ims.util.httpclient.GbaHttpController r2 = com.sec.internal.ims.util.httpclient.GbaHttpController.getInstance()     // Catch:{ all -> 0x012a }
            r3 = r4
            r4 = r7
            r5 = r6
            r6 = r8
            r7 = r11
            r8 = r15
            r11 = r18
            r2.sendBsfRequest(r3, r4, r5, r6, r7, r8, r9, r10, r11)     // Catch:{ all -> 0x012a }
            monitor-exit(r17)
            return r14
        L_0x011d:
            r4 = 0
            r5 = 0
            r6 = 0
            r2 = r21
            r3 = r14
            r7 = r20
            r2.onComplete(r3, r4, r5, r6, r7)     // Catch:{ all -> 0x012a }
            monitor-exit(r17)
            return r8
        L_0x012a:
            r0 = move-exception
            monitor-exit(r17)
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.gba.GbaServiceModule.getBtidAndGbaKey(com.sec.internal.helper.httpclient.HttpRequestParams, java.lang.String, com.sec.internal.helper.httpclient.HttpResponseParams, com.sec.internal.interfaces.ims.gba.IGbaCallback):int");
    }

    public GbaValue getGbaValue(int i, String str) {
        if (TextUtils.isEmpty(str)) {
            Log.d(LOG_TAG, "Invalid URI");
            return null;
        }
        return this.mGba.getGbaValue(str.getBytes(StandardCharsets.UTF_8), GBA_ME.getBytes(StandardCharsets.UTF_8), i);
    }

    public void resetGbaKey(String str, int i) {
        byte[] bArr;
        byte[] bArr2;
        String[] split = str.split("@");
        boolean z = true;
        if (split.length <= 1) {
            Log.e(LOG_TAG, "realm does not have @. resetGbaKey can not process.");
            return;
        }
        if (split[1].contains(";")) {
            bArr = split[1].split(";")[0].getBytes(StandardCharsets.UTF_8);
        } else {
            bArr = split[1].getBytes(StandardCharsets.UTF_8);
        }
        boolean isGbaSupported = this.mTelephonyManager.isGbaSupported(SimUtil.getSubId(i));
        if (!str.contains("uicc") || !isGbaSupported) {
            z = false;
        }
        if (z) {
            bArr2 = GBA_UICC.getBytes(StandardCharsets.UTF_8);
        } else {
            bArr2 = GBA_ME.getBytes(StandardCharsets.UTF_8);
        }
        this.mGba.removeGbaKey(bArr, bArr2, i);
    }

    public String storeGbaDataAndGenerateKey(String str, String str2, String str3, byte[] bArr, byte[] bArr2, byte[] bArr3, GbaData gbaData, boolean z, int i) {
        String generateGbaKey = generateGbaKey(bArr2, bArr3, StrUtil.hexStringToBytes(splitRandAutn(str3)[0]), str, str2, bArr, gbaData, z, i);
        String str4 = LOG_TAG;
        IMSLog.s(str4, "storeGbaDataAndGenerateKey(): base64 gbaKey: " + generateGbaKey);
        return generateGbaKey;
    }

    private static String[] splitRandAutn(String str) {
        String bytesToHexString = StrUtil.bytesToHexString(Base64.decode(str.getBytes(), 2));
        IMSLog.s(LOG_TAG, "Decoded AKA Challenge: " + bytesToHexString + " length: " + bytesToHexString.length());
        if (bytesToHexString.length() < 64) {
            return new String[]{"", ""};
        }
        return new String[]{"10" + bytesToHexString.substring(0, 32), "10" + bytesToHexString.substring(32, 64)};
    }

    /* JADX WARNING: Removed duplicated region for block: B:12:0x0085  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private java.lang.String generateGbaKey(byte[] r17, byte[] r18, byte[] r19, java.lang.String r20, java.lang.String r21, byte[] r22, com.sec.internal.ims.gba.params.GbaData r23, boolean r24, int r25) {
        /*
            r16 = this;
            r0 = r16
            r11 = r17
            r12 = r18
            r1 = r19
            com.sec.internal.helper.os.ITelephonyManager r2 = r0.mTelephonyManager
            r3 = 0
            if (r2 != 0) goto L_0x000e
            return r3
        L_0x000e:
            java.lang.String r2 = new java.lang.String
            r2.<init>(r11)
            java.lang.String r4 = LOG_TAG
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r6 = "generateGbaKey(): gbaType: "
            r5.append(r6)
            java.lang.String r6 = new java.lang.String
            r6.<init>(r11)
            r5.append(r6)
            java.lang.String r6 = " nafId: "
            r5.append(r6)
            java.lang.String r6 = new java.lang.String
            r6.<init>(r12)
            r5.append(r6)
            java.lang.String r5 = r5.toString()
            com.sec.internal.log.IMSLog.s(r4, r5)
            java.lang.String r5 = "gba-u"
            boolean r2 = r5.equals(r2)
            r13 = 2
            if (r2 == 0) goto L_0x00a3
            int r2 = com.sec.internal.helper.SimUtil.getSubId(r25)
            r14 = r20
            r15 = r21
            r0.storeGbaBootstrapParams(r2, r1, r14, r15)
            r10 = r22
            r9 = r24
            byte[] r1 = com.sec.internal.ims.gba.GbaUtility.getSecurityProtocolId(r12, r10, r9)
            java.lang.String r1 = com.sec.internal.helper.StrUtil.bytesToHexString(r1)
            int r2 = com.sec.internal.helper.SimUtil.getSubId(r25)
            java.lang.String r1 = r0.getGbaKeyResponse(r2, r1)
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r5 = "generateGbaKey(): response: "
            r2.append(r5)
            r2.append(r1)
            java.lang.String r2 = r2.toString()
            com.sec.internal.log.IMSLog.s(r4, r2)
            if (r1 == 0) goto L_0x0082
            char[] r1 = r1.toCharArray()     // Catch:{ DecoderException -> 0x0082 }
            byte[] r1 = org.apache.commons.codec.binary.Hex.decodeHex(r1)     // Catch:{ DecoderException -> 0x0082 }
            goto L_0x0083
        L_0x0082:
            r1 = r3
        L_0x0083:
            if (r1 == 0) goto L_0x0089
            java.lang.String r3 = r0.parseResKeyFromIsimResponse(r1)
        L_0x0089:
            r8 = r3
            com.sec.internal.ims.gba.Gba r0 = r0.mGba
            if (r0 == 0) goto L_0x00f0
            if (r8 == 0) goto L_0x00f0
            byte[] r3 = android.util.Base64.decode(r8, r13)
            r6 = 1
            r1 = r17
            r2 = r18
            r4 = r21
            r5 = r20
            r7 = r25
            r0.storeGbaKey(r1, r2, r3, r4, r5, r6, r7)
            goto L_0x00f0
        L_0x00a3:
            r14 = r20
            r15 = r21
            r10 = r22
            r9 = r24
            r2 = 1
            r3 = 17
            byte[] r4 = java.util.Arrays.copyOfRange(r1, r2, r3)
            r8 = r25
            java.lang.String r1 = r0.getImpi(r8)
            java.lang.String r2 = r23.getCipkey()
            byte[] r2 = com.sec.internal.helper.StrUtil.hexStringToBytes(r2)
            java.lang.String r3 = r23.getIntkey()
            byte[] r3 = com.sec.internal.helper.StrUtil.hexStringToBytes(r3)
            java.nio.charset.Charset r5 = java.nio.charset.StandardCharsets.UTF_8
            byte[] r5 = r1.getBytes(r5)
            r1 = r17
            r6 = r18
            r7 = r21
            r8 = r20
            java.lang.String r8 = com.sec.internal.ims.gba.GbaUtility.igenerateGbaMEKey(r1, r2, r3, r4, r5, r6, r7, r8, r9, r10)
            com.sec.internal.ims.gba.Gba r0 = r0.mGba
            if (r0 == 0) goto L_0x00f0
            byte[] r3 = android.util.Base64.decode(r8, r13)
            r6 = 0
            r1 = r17
            r2 = r18
            r4 = r21
            r5 = r20
            r7 = r25
            r0.storeGbaKey(r1, r2, r3, r4, r5, r6, r7)
        L_0x00f0:
            return r8
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.gba.GbaServiceModule.generateGbaKey(byte[], byte[], byte[], java.lang.String, java.lang.String, byte[], com.sec.internal.ims.gba.params.GbaData, boolean, int):java.lang.String");
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v11, resolved type: byte} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v19, resolved type: byte} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v20, resolved type: byte} */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public com.sec.internal.ims.gba.params.GbaData getPassword(java.lang.String r9, boolean r10, int r11) {
        /*
            r8 = this;
            java.lang.String[] r9 = splitRandAutn(r9)
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            r1 = 0
            r1 = r9[r1]
            r0.append(r1)
            r1 = 1
            r9 = r9[r1]
            r0.append(r9)
            java.lang.String r9 = r0.toString()
            r0 = 0
            if (r10 == 0) goto L_0x0025
            int r2 = com.sec.internal.helper.SimUtil.getSubId(r11)
            java.lang.String r8 = r8.getIsimResponse(r2, r9)
            goto L_0x0031
        L_0x0025:
            com.sec.internal.interfaces.ims.core.ISimManager r8 = com.sec.internal.ims.core.sim.SimManagerFactory.getSimManagerFromSimSlot(r11)
            if (r8 != 0) goto L_0x002d
            r8 = r0
            goto L_0x0031
        L_0x002d:
            java.lang.String r8 = r8.getIsimAuthentication(r9)
        L_0x0031:
            if (r8 != 0) goto L_0x0034
            return r0
        L_0x0034:
            java.lang.String r9 = r8.toLowerCase()
            java.lang.String r2 = "dc"
            boolean r9 = r9.startsWith(r2)
            if (r9 == 0) goto L_0x0048
            com.sec.internal.ims.gba.params.GbaData r9 = new com.sec.internal.ims.gba.params.GbaData
            java.lang.String r10 = ""
            r9.<init>(r8, r10, r10)
            return r9
        L_0x0048:
            java.lang.String r9 = r8.toLowerCase()
            java.lang.String r2 = "db"
            boolean r9 = r9.startsWith(r2)
            if (r9 != 0) goto L_0x006b
            java.lang.String r9 = LOG_TAG
            java.lang.StringBuilder r10 = new java.lang.StringBuilder
            r10.<init>()
            java.lang.String r11 = "getPassword(): wrong IsimResponse: "
            r10.append(r11)
            r10.append(r8)
            java.lang.String r8 = r10.toString()
            android.util.Log.e(r9, r8)
            return r0
        L_0x006b:
            byte[] r9 = com.sec.internal.helper.StrUtil.hexStringToBytes(r8)     // Catch:{ RuntimeException -> 0x0070 }
            goto L_0x0071
        L_0x0070:
            r9 = r0
        L_0x0071:
            if (r9 != 0) goto L_0x0074
            return r0
        L_0x0074:
            byte r1 = r9[r1]
            java.lang.String r2 = new java.lang.String
            r3 = 2
            int r4 = r3 + r1
            byte[] r5 = java.util.Arrays.copyOfRange(r9, r3, r4)
            java.lang.String r6 = "CP1252"
            java.nio.charset.Charset r6 = java.nio.charset.Charset.forName(r6)
            r2.<init>(r5, r6)
            java.lang.String r5 = LOG_TAG
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            java.lang.String r7 = "getPassword(): password = "
            r6.append(r7)
            r6.append(r2)
            java.lang.String r6 = r6.toString()
            com.sec.internal.log.IMSLog.s(r5, r6)
            if (r10 != 0) goto L_0x00c0
            int r10 = r4 + 1
            byte r0 = r9[r4]
            int r1 = r1 * r3
            int r1 = r1 + 4
            int r1 = r1 + r3
            int r4 = r0 * 2
            int r4 = r4 + r1
            java.lang.String r1 = r8.substring(r1, r4)
            int r10 = r10 + r0
            byte r9 = r9[r10]
            if (r9 >= 0) goto L_0x00b6
            int r9 = 256 - r9
        L_0x00b6:
            int r4 = r4 + r3
            int r9 = r9 * r3
            int r9 = r9 + r4
            java.lang.String r0 = r8.substring(r4, r9)
            r8 = r0
            r0 = r1
            goto L_0x00c1
        L_0x00c0:
            r8 = r0
        L_0x00c1:
            com.sec.internal.ims.gba.params.GbaData r9 = new com.sec.internal.ims.gba.params.GbaData
            r9.<init>(r2, r0, r8)
            r9.setPhoneId(r11)
            return r9
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.gba.GbaServiceModule.getPassword(java.lang.String, boolean, int):com.sec.internal.ims.gba.params.GbaData");
    }

    private String getGbaKeyResponse(int i, String str) {
        if (this.mTelephonyManager == null) {
            return null;
        }
        String str2 = "DE" + Integer.toHexString(str.length() / 2) + str + "00";
        String str3 = "";
        try {
            str3 = transmitLogicChannel(i, this.mTelephonyManager.getAidForAppType(i, 5), str2, (str2.length() / 2) - 1);
            IMSLog.s(LOG_TAG, "getGbaKeyResponse response " + str3);
            return str3;
        } catch (RemoteException unused) {
            return str3;
        }
    }

    private String getIsimResponse(int i, String str) {
        if (this.mTelephonyManager == null) {
            return null;
        }
        String str2 = "DD" + str + "00";
        String str3 = "";
        try {
            str3 = transmitLogicChannel(i, this.mTelephonyManager.getAidForAppType(i, 5), str2, (str2.length() / 2) - 1);
            IMSLog.s(LOG_TAG, "getIsimResponse response " + str3);
            return str3;
        } catch (RemoteException unused) {
            return str3;
        }
    }

    private boolean isRealmFromUsername(int i) {
        Mno simMno = SimUtil.getSimMno(i);
        return simMno == Mno.KPN_NED || simMno == Mno.TELEFONICA_CZ || simMno == Mno.TELEFONICA_SLOVAKIA;
    }

    private int createRequestId() {
        if (mGbaIdCounter >= 255) {
            mGbaIdCounter = 0;
        }
        int i = mGbaIdCounter + 1;
        mGbaIdCounter = i;
        return i;
    }
}
