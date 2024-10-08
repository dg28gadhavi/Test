package com.sec.internal.ims.core.cmc;

import java.util.ArrayList;
import java.util.List;

public class CmcInfo {
    String mAccessToken = "";
    boolean mActivation = false;
    boolean mCallforkingEnabled = true;
    String mDeviceId = "";
    String mDeviceType = "";
    boolean mHasSd = true;
    boolean mIsDualCmc = false;
    boolean mIsEmergencyCallSupported = false;
    boolean mIsSameWiFiOnly = false;
    String mLineId = "";
    String mLineImpu = "";
    String mLineOwnerDeviceId = "";
    int mLineSlotIndex = -1;
    int mNetworkPref = 1;
    boolean mOobe = false;
    List<String> mPcscfAddrList = new ArrayList();
    String mSaServerUrl = "";

    public enum DataType {
        BOOLEAN,
        INTEGER,
        STRING,
        LIST,
        NOT_DEFINED
    }

    public enum CmcInfoType {
        ACTIVATION(r1),
        LINE_SLOT_INDEX(r2),
        DEVICE_TYPE(r3),
        DEVICE_ID(r3),
        ACCESS_TOKEN(r3),
        LINE_ID(r3),
        LINE_OWNER_DEVICE_ID(r3),
        LINE_IMPU(r3),
        SA_SERVER_URL(r3),
        PCSCF_ADDR_LIST(DataType.LIST),
        CALL_FORKING_ENABLED(r1),
        HAS_SD(r1),
        NETWORK_PREF(r2),
        OOBE(r1),
        EMERGENCY_CALL_SUPPORTED(r1),
        SAME_WIFI_ONLY(r1),
        DUAL_CMC(r1);
        
        /* access modifiers changed from: private */
        public DataType mDataType;

        private CmcInfoType(DataType dataType) {
            this.mDataType = dataType;
        }

        /* JADX WARNING: Code restructure failed: missing block: B:2:0x0004, code lost:
            r1 = r1.mDataType;
         */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean isDumpPrintAvailable() {
            /*
                r1 = this;
                com.sec.internal.ims.core.cmc.CmcInfo$CmcInfoType r0 = DEVICE_TYPE
                if (r1 == r0) goto L_0x0011
                com.sec.internal.ims.core.cmc.CmcInfo$DataType r1 = r1.mDataType
                com.sec.internal.ims.core.cmc.CmcInfo$DataType r0 = com.sec.internal.ims.core.cmc.CmcInfo.DataType.INTEGER
                if (r1 == r0) goto L_0x0011
                com.sec.internal.ims.core.cmc.CmcInfo$DataType r0 = com.sec.internal.ims.core.cmc.CmcInfo.DataType.BOOLEAN
                if (r1 != r0) goto L_0x000f
                goto L_0x0011
            L_0x000f:
                r1 = 0
                goto L_0x0012
            L_0x0011:
                r1 = 1
            L_0x0012:
                return r1
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.cmc.CmcInfo.CmcInfoType.isDumpPrintAvailable():boolean");
        }
    }

    public Object getValue(CmcInfoType cmcInfoType) {
        switch (AnonymousClass1.$SwitchMap$com$sec$internal$ims$core$cmc$CmcInfo$CmcInfoType[cmcInfoType.ordinal()]) {
            case 1:
                return Boolean.valueOf(this.mOobe);
            case 2:
                return Boolean.valueOf(this.mActivation);
            case 3:
                return Integer.valueOf(this.mLineSlotIndex);
            case 4:
                return this.mDeviceType;
            case 5:
                return this.mDeviceId;
            case 6:
                return this.mAccessToken;
            case 7:
                return this.mLineId;
            case 8:
                return this.mLineOwnerDeviceId;
            case 9:
                return this.mLineImpu;
            case 10:
                return this.mSaServerUrl;
            case 11:
                return this.mPcscfAddrList;
            case 12:
                return Boolean.valueOf(this.mCallforkingEnabled);
            case 13:
                return Boolean.valueOf(this.mHasSd);
            case 14:
                return Integer.valueOf(this.mNetworkPref);
            case 15:
                return Boolean.valueOf(this.mIsEmergencyCallSupported);
            case 16:
                return Boolean.valueOf(this.mIsSameWiFiOnly);
            case 17:
                return Boolean.valueOf(this.mIsDualCmc);
            default:
                return null;
        }
    }

    public boolean compare(CmcInfoType cmcInfoType, CmcInfo cmcInfo) {
        Object value = getValue(cmcInfoType);
        Object value2 = cmcInfo.getValue(cmcInfoType);
        int i = AnonymousClass1.$SwitchMap$com$sec$internal$ims$core$cmc$CmcInfo$DataType[cmcInfoType.mDataType.ordinal()];
        if (i != 1) {
            if (i != 2) {
                if (i != 3) {
                    if (i != 4) {
                        return false;
                    }
                    if (!(value == null || value2 == null)) {
                        List list = (List) value;
                        List list2 = (List) value2;
                        if (list.size() == list2.size() && list.containsAll(list2) && list2.containsAll(list)) {
                            return true;
                        }
                    }
                    if (value == null && value2 == null) {
                        return true;
                    }
                    return false;
                } else if (value != null && value2 != null) {
                    return ((String) value).equals((String) value2);
                } else {
                    if (value == null && value2 == null) {
                        return true;
                    }
                    return false;
                }
            } else if (((Integer) value).intValue() == ((Integer) value2).intValue()) {
                return true;
            } else {
                return false;
            }
        } else if (((Boolean) value).booleanValue() == ((Boolean) value2).booleanValue()) {
            return true;
        } else {
            return false;
        }
    }

    /* renamed from: com.sec.internal.ims.core.cmc.CmcInfo$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$ims$core$cmc$CmcInfo$CmcInfoType;
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$ims$core$cmc$CmcInfo$DataType;

        /* JADX WARNING: Can't wrap try/catch for region: R(42:0|(2:1|2)|3|(2:5|6)|7|(2:9|10)|11|(2:13|14)|15|17|18|19|20|21|22|23|24|25|26|27|28|29|30|31|32|33|34|35|36|37|38|39|40|41|42|43|44|45|46|47|48|(3:49|50|52)) */
        /* JADX WARNING: Can't wrap try/catch for region: R(43:0|(2:1|2)|3|(2:5|6)|7|9|10|11|(2:13|14)|15|17|18|19|20|21|22|23|24|25|26|27|28|29|30|31|32|33|34|35|36|37|38|39|40|41|42|43|44|45|46|47|48|(3:49|50|52)) */
        /* JADX WARNING: Can't wrap try/catch for region: R(44:0|(2:1|2)|3|5|6|7|9|10|11|(2:13|14)|15|17|18|19|20|21|22|23|24|25|26|27|28|29|30|31|32|33|34|35|36|37|38|39|40|41|42|43|44|45|46|47|48|(3:49|50|52)) */
        /* JADX WARNING: Can't wrap try/catch for region: R(46:0|(2:1|2)|3|5|6|7|9|10|11|(2:13|14)|15|17|18|19|20|21|22|23|24|25|26|27|28|29|30|31|32|33|34|35|36|37|38|39|40|41|42|43|44|45|46|47|48|49|50|52) */
        /* JADX WARNING: Can't wrap try/catch for region: R(48:0|1|2|3|5|6|7|9|10|11|13|14|15|17|18|19|20|21|22|23|24|25|26|27|28|29|30|31|32|33|34|35|36|37|38|39|40|41|42|43|44|45|46|47|48|49|50|52) */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:19:0x0044 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:21:0x004e */
        /* JADX WARNING: Missing exception handler attribute for start block: B:23:0x0058 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:25:0x0062 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:27:0x006d */
        /* JADX WARNING: Missing exception handler attribute for start block: B:29:0x0078 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:31:0x0083 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:33:0x008f */
        /* JADX WARNING: Missing exception handler attribute for start block: B:35:0x009b */
        /* JADX WARNING: Missing exception handler attribute for start block: B:37:0x00a7 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:39:0x00b3 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:41:0x00bf */
        /* JADX WARNING: Missing exception handler attribute for start block: B:43:0x00cb */
        /* JADX WARNING: Missing exception handler attribute for start block: B:45:0x00d7 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:47:0x00e3 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:49:0x00ef */
        static {
            /*
                com.sec.internal.ims.core.cmc.CmcInfo$DataType[] r0 = com.sec.internal.ims.core.cmc.CmcInfo.DataType.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$sec$internal$ims$core$cmc$CmcInfo$DataType = r0
                r1 = 1
                com.sec.internal.ims.core.cmc.CmcInfo$DataType r2 = com.sec.internal.ims.core.cmc.CmcInfo.DataType.BOOLEAN     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r2 = r2.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r0[r2] = r1     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                r0 = 2
                int[] r2 = $SwitchMap$com$sec$internal$ims$core$cmc$CmcInfo$DataType     // Catch:{ NoSuchFieldError -> 0x001d }
                com.sec.internal.ims.core.cmc.CmcInfo$DataType r3 = com.sec.internal.ims.core.cmc.CmcInfo.DataType.INTEGER     // Catch:{ NoSuchFieldError -> 0x001d }
                int r3 = r3.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2[r3] = r0     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                r2 = 3
                int[] r3 = $SwitchMap$com$sec$internal$ims$core$cmc$CmcInfo$DataType     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.sec.internal.ims.core.cmc.CmcInfo$DataType r4 = com.sec.internal.ims.core.cmc.CmcInfo.DataType.STRING     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r4 = r4.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r3[r4] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                r3 = 4
                int[] r4 = $SwitchMap$com$sec$internal$ims$core$cmc$CmcInfo$DataType     // Catch:{ NoSuchFieldError -> 0x0033 }
                com.sec.internal.ims.core.cmc.CmcInfo$DataType r5 = com.sec.internal.ims.core.cmc.CmcInfo.DataType.LIST     // Catch:{ NoSuchFieldError -> 0x0033 }
                int r5 = r5.ordinal()     // Catch:{ NoSuchFieldError -> 0x0033 }
                r4[r5] = r3     // Catch:{ NoSuchFieldError -> 0x0033 }
            L_0x0033:
                com.sec.internal.ims.core.cmc.CmcInfo$CmcInfoType[] r4 = com.sec.internal.ims.core.cmc.CmcInfo.CmcInfoType.values()
                int r4 = r4.length
                int[] r4 = new int[r4]
                $SwitchMap$com$sec$internal$ims$core$cmc$CmcInfo$CmcInfoType = r4
                com.sec.internal.ims.core.cmc.CmcInfo$CmcInfoType r5 = com.sec.internal.ims.core.cmc.CmcInfo.CmcInfoType.OOBE     // Catch:{ NoSuchFieldError -> 0x0044 }
                int r5 = r5.ordinal()     // Catch:{ NoSuchFieldError -> 0x0044 }
                r4[r5] = r1     // Catch:{ NoSuchFieldError -> 0x0044 }
            L_0x0044:
                int[] r1 = $SwitchMap$com$sec$internal$ims$core$cmc$CmcInfo$CmcInfoType     // Catch:{ NoSuchFieldError -> 0x004e }
                com.sec.internal.ims.core.cmc.CmcInfo$CmcInfoType r4 = com.sec.internal.ims.core.cmc.CmcInfo.CmcInfoType.ACTIVATION     // Catch:{ NoSuchFieldError -> 0x004e }
                int r4 = r4.ordinal()     // Catch:{ NoSuchFieldError -> 0x004e }
                r1[r4] = r0     // Catch:{ NoSuchFieldError -> 0x004e }
            L_0x004e:
                int[] r0 = $SwitchMap$com$sec$internal$ims$core$cmc$CmcInfo$CmcInfoType     // Catch:{ NoSuchFieldError -> 0x0058 }
                com.sec.internal.ims.core.cmc.CmcInfo$CmcInfoType r1 = com.sec.internal.ims.core.cmc.CmcInfo.CmcInfoType.LINE_SLOT_INDEX     // Catch:{ NoSuchFieldError -> 0x0058 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0058 }
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0058 }
            L_0x0058:
                int[] r0 = $SwitchMap$com$sec$internal$ims$core$cmc$CmcInfo$CmcInfoType     // Catch:{ NoSuchFieldError -> 0x0062 }
                com.sec.internal.ims.core.cmc.CmcInfo$CmcInfoType r1 = com.sec.internal.ims.core.cmc.CmcInfo.CmcInfoType.DEVICE_TYPE     // Catch:{ NoSuchFieldError -> 0x0062 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0062 }
                r0[r1] = r3     // Catch:{ NoSuchFieldError -> 0x0062 }
            L_0x0062:
                int[] r0 = $SwitchMap$com$sec$internal$ims$core$cmc$CmcInfo$CmcInfoType     // Catch:{ NoSuchFieldError -> 0x006d }
                com.sec.internal.ims.core.cmc.CmcInfo$CmcInfoType r1 = com.sec.internal.ims.core.cmc.CmcInfo.CmcInfoType.DEVICE_ID     // Catch:{ NoSuchFieldError -> 0x006d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x006d }
                r2 = 5
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x006d }
            L_0x006d:
                int[] r0 = $SwitchMap$com$sec$internal$ims$core$cmc$CmcInfo$CmcInfoType     // Catch:{ NoSuchFieldError -> 0x0078 }
                com.sec.internal.ims.core.cmc.CmcInfo$CmcInfoType r1 = com.sec.internal.ims.core.cmc.CmcInfo.CmcInfoType.ACCESS_TOKEN     // Catch:{ NoSuchFieldError -> 0x0078 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0078 }
                r2 = 6
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0078 }
            L_0x0078:
                int[] r0 = $SwitchMap$com$sec$internal$ims$core$cmc$CmcInfo$CmcInfoType     // Catch:{ NoSuchFieldError -> 0x0083 }
                com.sec.internal.ims.core.cmc.CmcInfo$CmcInfoType r1 = com.sec.internal.ims.core.cmc.CmcInfo.CmcInfoType.LINE_ID     // Catch:{ NoSuchFieldError -> 0x0083 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0083 }
                r2 = 7
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0083 }
            L_0x0083:
                int[] r0 = $SwitchMap$com$sec$internal$ims$core$cmc$CmcInfo$CmcInfoType     // Catch:{ NoSuchFieldError -> 0x008f }
                com.sec.internal.ims.core.cmc.CmcInfo$CmcInfoType r1 = com.sec.internal.ims.core.cmc.CmcInfo.CmcInfoType.LINE_OWNER_DEVICE_ID     // Catch:{ NoSuchFieldError -> 0x008f }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x008f }
                r2 = 8
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x008f }
            L_0x008f:
                int[] r0 = $SwitchMap$com$sec$internal$ims$core$cmc$CmcInfo$CmcInfoType     // Catch:{ NoSuchFieldError -> 0x009b }
                com.sec.internal.ims.core.cmc.CmcInfo$CmcInfoType r1 = com.sec.internal.ims.core.cmc.CmcInfo.CmcInfoType.LINE_IMPU     // Catch:{ NoSuchFieldError -> 0x009b }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x009b }
                r2 = 9
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x009b }
            L_0x009b:
                int[] r0 = $SwitchMap$com$sec$internal$ims$core$cmc$CmcInfo$CmcInfoType     // Catch:{ NoSuchFieldError -> 0x00a7 }
                com.sec.internal.ims.core.cmc.CmcInfo$CmcInfoType r1 = com.sec.internal.ims.core.cmc.CmcInfo.CmcInfoType.SA_SERVER_URL     // Catch:{ NoSuchFieldError -> 0x00a7 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00a7 }
                r2 = 10
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00a7 }
            L_0x00a7:
                int[] r0 = $SwitchMap$com$sec$internal$ims$core$cmc$CmcInfo$CmcInfoType     // Catch:{ NoSuchFieldError -> 0x00b3 }
                com.sec.internal.ims.core.cmc.CmcInfo$CmcInfoType r1 = com.sec.internal.ims.core.cmc.CmcInfo.CmcInfoType.PCSCF_ADDR_LIST     // Catch:{ NoSuchFieldError -> 0x00b3 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00b3 }
                r2 = 11
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00b3 }
            L_0x00b3:
                int[] r0 = $SwitchMap$com$sec$internal$ims$core$cmc$CmcInfo$CmcInfoType     // Catch:{ NoSuchFieldError -> 0x00bf }
                com.sec.internal.ims.core.cmc.CmcInfo$CmcInfoType r1 = com.sec.internal.ims.core.cmc.CmcInfo.CmcInfoType.CALL_FORKING_ENABLED     // Catch:{ NoSuchFieldError -> 0x00bf }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00bf }
                r2 = 12
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00bf }
            L_0x00bf:
                int[] r0 = $SwitchMap$com$sec$internal$ims$core$cmc$CmcInfo$CmcInfoType     // Catch:{ NoSuchFieldError -> 0x00cb }
                com.sec.internal.ims.core.cmc.CmcInfo$CmcInfoType r1 = com.sec.internal.ims.core.cmc.CmcInfo.CmcInfoType.HAS_SD     // Catch:{ NoSuchFieldError -> 0x00cb }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00cb }
                r2 = 13
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00cb }
            L_0x00cb:
                int[] r0 = $SwitchMap$com$sec$internal$ims$core$cmc$CmcInfo$CmcInfoType     // Catch:{ NoSuchFieldError -> 0x00d7 }
                com.sec.internal.ims.core.cmc.CmcInfo$CmcInfoType r1 = com.sec.internal.ims.core.cmc.CmcInfo.CmcInfoType.NETWORK_PREF     // Catch:{ NoSuchFieldError -> 0x00d7 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00d7 }
                r2 = 14
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00d7 }
            L_0x00d7:
                int[] r0 = $SwitchMap$com$sec$internal$ims$core$cmc$CmcInfo$CmcInfoType     // Catch:{ NoSuchFieldError -> 0x00e3 }
                com.sec.internal.ims.core.cmc.CmcInfo$CmcInfoType r1 = com.sec.internal.ims.core.cmc.CmcInfo.CmcInfoType.EMERGENCY_CALL_SUPPORTED     // Catch:{ NoSuchFieldError -> 0x00e3 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00e3 }
                r2 = 15
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00e3 }
            L_0x00e3:
                int[] r0 = $SwitchMap$com$sec$internal$ims$core$cmc$CmcInfo$CmcInfoType     // Catch:{ NoSuchFieldError -> 0x00ef }
                com.sec.internal.ims.core.cmc.CmcInfo$CmcInfoType r1 = com.sec.internal.ims.core.cmc.CmcInfo.CmcInfoType.SAME_WIFI_ONLY     // Catch:{ NoSuchFieldError -> 0x00ef }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00ef }
                r2 = 16
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00ef }
            L_0x00ef:
                int[] r0 = $SwitchMap$com$sec$internal$ims$core$cmc$CmcInfo$CmcInfoType     // Catch:{ NoSuchFieldError -> 0x00fb }
                com.sec.internal.ims.core.cmc.CmcInfo$CmcInfoType r1 = com.sec.internal.ims.core.cmc.CmcInfo.CmcInfoType.DUAL_CMC     // Catch:{ NoSuchFieldError -> 0x00fb }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00fb }
                r2 = 17
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00fb }
            L_0x00fb:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.cmc.CmcInfo.AnonymousClass1.<clinit>():void");
        }
    }

    public boolean checkValid(CmcInfoType cmcInfoType) {
        Object value = getValue(cmcInfoType);
        int i = AnonymousClass1.$SwitchMap$com$sec$internal$ims$core$cmc$CmcInfo$DataType[cmcInfoType.mDataType.ordinal()];
        if (i == 3) {
            String str = (String) value;
            if (str == null || str.isEmpty()) {
                return false;
            }
            return true;
        } else if (i != 4) {
            return true;
        } else {
            List list = (List) value;
            if (list == null || list.size() <= 0) {
                return false;
            }
            return true;
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("<");
        for (CmcInfoType cmcInfoType : CmcInfoType.values()) {
            sb.append(cmcInfoType.name());
            sb.append(":");
            sb.append(getValue(cmcInfoType));
            sb.append(", ");
        }
        if (sb.lastIndexOf(", ") != -1) {
            sb.delete(sb.lastIndexOf(", "), sb.length());
        }
        sb.append(">");
        return sb.toString();
    }
}
