package com.sec.internal.ims.settings;

import android.content.ContentValues;
import android.content.Context;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.helper.os.DeviceUtil;
import com.sec.internal.ims.rcs.util.RcsUtils;
import com.sec.internal.ims.settings.ImsServiceSwitch;

public class ImsServiceSwitchUsa extends ImsServiceSwitchBase {
    private final String LOG_TAG = ImsServiceSwitchUsa.class.getSimpleName();

    public ImsServiceSwitchUsa(Context context, int i) {
        super(context, i);
    }

    /* access modifiers changed from: protected */
    public ContentValues overrideImsSwitchForCarrier(ContentValues contentValues) {
        if ("XAA".equals(OmcCode.getNWCode(this.mPhoneId)) && !DeviceUtil.isUSOpenDevice()) {
            contentValues.put(ImsServiceSwitch.ImsSwitch.RCS.ENABLE_RCS, ConfigConstants.VALUE.INFO_COMPLETED);
            contentValues.put(ImsServiceSwitch.ImsSwitch.RCS.ENABLE_RCS_CHAT_SERVICE, ConfigConstants.VALUE.INFO_COMPLETED);
            this.mEventLog.logAndAdd(this.mPhoneId, "OYN/XAA case. Disable RCS.");
        }
        return contentValues;
    }

    private boolean isSingleRegistrationAndAndroidMessageAppUsed() {
        return !DeviceUtil.isTablet() && RcsUtils.isSingleRegiRequiredAndAndroidMessageAppInUsed(this.mContext, this.mPhoneId);
    }

    public boolean isEnabled(String str) {
        if (this.mVolteServiceSwitch.containsKey(str)) {
            if ("ss".equals(str)) {
                if ((this.mSsEnabled || this.mVoLteEnabled) && this.mVolteServiceSwitch.get(str).booleanValue()) {
                    return true;
                }
                return false;
            } else if (!this.mVoLteEnabled || !this.mVolteServiceSwitch.get(str).booleanValue()) {
                return false;
            } else {
                return true;
            }
        } else if (!this.mRcsServiceSwitch.containsKey(str)) {
            return false;
        } else {
            if ((!this.mRcsEnabled || !this.mRcsServiceSwitch.get(str).booleanValue()) && !isSingleRegistrationAndAndroidMessageAppUsed()) {
                return false;
            }
            return true;
        }
    }

    public boolean isRcsEnabled() {
        return this.mRcsEnabled || isSingleRegistrationAndAndroidMessageAppUsed();
    }

    public boolean isRcsSwitchEnabled() {
        return this.mRcsEnabled || isSingleRegistrationAndAndroidMessageAppUsed();
    }
}
