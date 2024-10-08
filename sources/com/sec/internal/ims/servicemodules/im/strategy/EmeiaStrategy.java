package com.sec.internal.ims.servicemodules.im.strategy;

import android.content.Context;
import com.sec.ims.options.Capabilities;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.ChatData;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.servicemodules.options.CapabilityConstants;
import com.sec.internal.ims.servicemodules.im.ImConfig;
import com.sec.internal.log.IMSLog;
import java.util.Set;

public class EmeiaStrategy extends DefaultRCSMnoStrategy {
    private static final String TAG = "EmeiaStrategy";

    public EmeiaStrategy(Context context, int i) {
        super(context, i);
    }

    public boolean isFTViaHttp(ImConfig imConfig, Set<ImsUri> set, ChatData.ChatType chatType) {
        return imConfig.getFtDefaultMech() == ImConstants.FtMech.HTTP && isFtHttpRegistered() && (ChatData.ChatType.isGroupChatIdBasedGroupChat(chatType) || checkFtHttpCapability(set) || checkUserAvailableOffline(set));
    }

    public long updateAvailableFeatures(Capabilities capabilities, long j, CapabilityConstants.CapExResult capExResult) {
        String str = TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "updateAvailableFeatures:" + capabilities);
        if (capabilities == null) {
            return j;
        }
        if (capExResult == CapabilityConstants.CapExResult.USER_UNAVAILABLE) {
            j = (long) (capabilities.isAvailable() ? Capabilities.FEATURE_OFFLINE_RCS_USER : Capabilities.FEATURE_NON_RCS_USER);
        }
        int i2 = this.mPhoneId;
        IMSLog.i(str, i2, "updateAvailableFeatures: mAvailableFeatures " + j);
        return j;
    }

    public boolean needCapabilitiesUpdate(CapabilityConstants.CapExResult capExResult, Capabilities capabilities, long j, long j2) {
        if (capabilities == null || capExResult == CapabilityConstants.CapExResult.USER_NOT_FOUND) {
            IMSLog.i(TAG, this.mPhoneId, "needCapabilitiesUpdate: Capability is null");
            return true;
        } else if (capExResult.isOneOf(CapabilityConstants.CapExResult.UNCLASSIFIED_ERROR, CapabilityConstants.CapExResult.FORBIDDEN_403)) {
            IMSLog.i(TAG, this.mPhoneId, "needCapabilitiesUpdate: do not change anything");
            return false;
        } else if (capExResult != CapabilityConstants.CapExResult.FAILURE) {
            return true;
        } else {
            return false;
        }
    }

    public long updateFeatures(Capabilities capabilities, long j, CapabilityConstants.CapExResult capExResult) {
        if (capabilities == null || capabilities.hasFeature(Capabilities.FEATURE_NOT_UPDATED) || capabilities.hasFeature(Capabilities.FEATURE_NON_RCS_USER) || j == ((long) Capabilities.FEATURE_NON_RCS_USER)) {
            String str = TAG;
            int i = this.mPhoneId;
            IMSLog.i(str, i, "updateFeatures: set features " + Capabilities.dumpFeature(j));
            return j;
        } else if (j == ((long) Capabilities.FEATURE_NOT_UPDATED)) {
            String str2 = TAG;
            int i2 = this.mPhoneId;
            IMSLog.i(str2, i2, "updateFeatures: feature is NOT_UPDATED, remains previous features " + Capabilities.dumpFeature(capabilities.getFeature()));
            return capabilities.getFeature();
        } else {
            String str3 = TAG;
            int i3 = this.mPhoneId;
            IMSLog.i(str3, i3, "updateFeatures: updated features " + Capabilities.dumpFeature(capabilities.getFeature() | j));
            return capabilities.getFeature() | j;
        }
    }
}
