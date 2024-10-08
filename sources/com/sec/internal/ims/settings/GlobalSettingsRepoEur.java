package com.sec.internal.ims.settings;

import android.content.Context;
import android.text.TextUtils;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.helper.ImsSharedPrefHelper;
import com.sec.internal.helper.os.DeviceUtil;
import com.sec.internal.log.IMSLog;
import java.util.Locale;

public class GlobalSettingsRepoEur extends GlobalSettingsRepoBase {
    private final String LOG_TAG = GlobalSettingsRepoEur.class.getSimpleName();

    public GlobalSettingsRepoEur(Context context, int i) {
        super(context, i);
    }

    /* access modifiers changed from: protected */
    public boolean needToCheckResetSetting() {
        return this.mVersionUpdated || this.mMnoNameUpdated;
    }

    /* access modifiers changed from: protected */
    public void initNeedToCheckResetSetting() {
        this.mVersionUpdated = false;
        this.mMnoNameUpdated = false;
    }

    /* access modifiers changed from: protected */
    public boolean needResetVolteAsDefault(int i, int i2, String str, String str2) {
        Locale locale = Locale.US;
        if (TextUtils.equals(str2.toUpperCase(locale), "ALWAYS")) {
            return true;
        }
        if (TextUtils.equals(str2.toUpperCase(locale), "ONETIME") && !isFinishResetVoiceCallType(this.mPhoneId, str)) {
            finishResetVoiceCallType(this.mPhoneId, str, true);
            return true;
        } else if (!this.mVersionUpdated || i == i2) {
            return false;
        } else {
            return true;
        }
    }

    private void finishResetVoiceCallType(int i, String str, boolean z) {
        ImsSharedPrefHelper.save(i, this.mContext, "imsswitch", "reset_voicecall_type_done_" + str, z);
        String str2 = this.LOG_TAG;
        IMSLog.s(str2, i, "finishResetVoiceCallType: Mno(" + str + ")");
    }

    private boolean isFinishResetVoiceCallType(int i, String str) {
        boolean z = ImsSharedPrefHelper.getBoolean(i, this.mContext, "imsswitch", "reset_voicecall_type_done_" + str, false);
        String str2 = this.LOG_TAG;
        IMSLog.s(str2, i, "isFinishResetVoiceCallType: Mno(" + str + "):" + z);
        return z;
    }

    /* access modifiers changed from: protected */
    public int preUpdateSystemSettings(Mno mno, int i, boolean z, boolean z2) {
        int i2;
        if (DeviceUtil.removeVolteMenuByCsc(this.mPhoneId)) {
            IMSLog.d(this.LOG_TAG, this.mPhoneId, "reset voice and vt call settings db because of VOICECLLCSC CONFIGOPSTYLEMOBILENETWORKSETTINGMENU Feature");
            i2 = 0;
            ImsConstants.SystemSettings.setVoiceCallType(this.mContext, 0, this.mPhoneId);
        } else {
            i2 = i;
        }
        String str = this.LOG_TAG;
        int i3 = this.mPhoneId;
        IMSLog.d(str, i3, "preUpdateSystemSettings: from " + i + " to " + i2);
        return i2;
    }
}
