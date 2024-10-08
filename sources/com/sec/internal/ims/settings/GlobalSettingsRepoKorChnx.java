package com.sec.internal.ims.settings;

import android.content.Context;
import android.os.SemSystemProperties;

public class GlobalSettingsRepoKorChnx extends GlobalSettingsRepoBase {
    public GlobalSettingsRepoKorChnx(Context context, int i) {
        super(context, i);
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x0078  */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x007a  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int preUpdateSystemSettings(com.sec.internal.constants.Mno r4, int r5, boolean r6, boolean r7) {
        /*
            r3 = this;
            if (r7 != 0) goto L_0x0003
            return r5
        L_0x0003:
            android.content.Context r7 = r3.mContext
            r0 = -1
            int r1 = r3.mPhoneId
            int r7 = com.sec.internal.constants.ims.ImsConstants.SystemSettings.getVoiceCallType(r7, r0, r1)
            r0 = 0
            if (r7 != 0) goto L_0x0014
            if (r5 == 0) goto L_0x0012
            goto L_0x0014
        L_0x0012:
            r7 = r0
            goto L_0x0015
        L_0x0014:
            r7 = 1
        L_0x0015:
            com.sec.internal.constants.Mno r1 = com.sec.internal.constants.Mno.CTC
            if (r4 == r1) goto L_0x004c
            com.sec.internal.constants.Mno r1 = com.sec.internal.constants.Mno.CTCMO
            if (r4 != r1) goto L_0x001e
            goto L_0x004c
        L_0x001e:
            com.sec.internal.constants.Mno r1 = com.sec.internal.constants.Mno.CMCC
            if (r4 != r1) goto L_0x0038
            boolean r1 = r3.isSupport5GConcept()
            if (r1 == 0) goto L_0x006d
            boolean r1 = com.sec.internal.helper.OmcCode.isMainlandChinaOmcCode()
            if (r1 == 0) goto L_0x006d
            if (r7 == 0) goto L_0x006d
            android.content.Context r5 = r3.mContext
            int r7 = r3.mPhoneId
            com.sec.internal.constants.ims.ImsConstants.SystemSettings.setVoiceCallType(r5, r0, r7)
            goto L_0x006c
        L_0x0038:
            com.sec.internal.constants.Mno r1 = com.sec.internal.constants.Mno.CU
            if (r4 != r1) goto L_0x006d
            boolean r1 = com.sec.internal.helper.OmcCode.isMainlandChinaOmcCode()
            if (r1 == 0) goto L_0x006d
            if (r7 == 0) goto L_0x006d
            android.content.Context r5 = r3.mContext
            int r7 = r3.mPhoneId
            com.sec.internal.constants.ims.ImsConstants.SystemSettings.setVoiceCallType(r5, r0, r7)
            goto L_0x006c
        L_0x004c:
            boolean r1 = r3.isSupport5GConcept()
            if (r1 != 0) goto L_0x005d
            java.lang.String r1 = "ro.product.first_api_level"
            int r1 = android.os.SemSystemProperties.getInt(r1, r0)
            r2 = 29
            if (r1 < r2) goto L_0x006d
        L_0x005d:
            boolean r1 = com.sec.internal.helper.OmcCode.isChinaOmcCode()
            if (r1 == 0) goto L_0x006d
            if (r7 == 0) goto L_0x006d
            android.content.Context r5 = r3.mContext
            int r7 = r3.mPhoneId
            com.sec.internal.constants.ims.ImsConstants.SystemSettings.setVoiceCallType(r5, r0, r7)
        L_0x006c:
            r5 = r0
        L_0x006d:
            boolean r7 = r4.isHkMo()
            if (r7 != 0) goto L_0x007a
            com.sec.internal.constants.Mno r7 = com.sec.internal.constants.Mno.CTCMO
            if (r4 != r7) goto L_0x0078
            goto L_0x007a
        L_0x0078:
            r0 = r5
            goto L_0x0083
        L_0x007a:
            if (r6 == 0) goto L_0x0083
            android.content.Context r4 = r3.mContext
            int r3 = r3.mPhoneId
            com.sec.internal.constants.ims.ImsConstants.SystemSettings.setVoiceCallType(r4, r0, r3)
        L_0x0083:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.settings.GlobalSettingsRepoKorChnx.preUpdateSystemSettings(com.sec.internal.constants.Mno, int, boolean, boolean):int");
    }

    private boolean isSupport5GConcept() {
        try {
            if (Integer.parseInt(SemSystemProperties.get("ro.telephony.default_network", "0,0").trim().split(",")[0]) >= 23) {
                return true;
            }
            return false;
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return false;
        }
    }
}
