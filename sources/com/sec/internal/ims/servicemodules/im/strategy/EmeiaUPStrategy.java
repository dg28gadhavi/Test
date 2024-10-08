package com.sec.internal.ims.servicemodules.im.strategy;

import android.content.Context;
import com.sec.ims.options.Capabilities;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.ChatData;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.servicemodules.options.CapabilityConstants;
import com.sec.internal.ims.servicemodules.im.ImConfig;
import com.sec.internal.ims.util.ChatbotUriUtil;
import com.sec.internal.log.IMSLog;
import java.util.Set;

public class EmeiaUPStrategy extends DefaultUPMnoStrategy {
    private static final String TAG = "EmeiaUPStrategy";

    public EmeiaUPStrategy(Context context, int i) {
        super(context, i);
    }

    public boolean isFTViaHttp(ImConfig imConfig, Set<ImsUri> set, ChatData.ChatType chatType) {
        return imConfig.getFtDefaultMech() == ImConstants.FtMech.HTTP && isFtHttpRegistered() && (ChatData.ChatType.isGroupChatIdBasedGroupChat(chatType) || checkFtHttpCapability(set) || checkUserAvailableOffline(set));
    }

    public boolean needCapabilitiesUpdate(CapabilityConstants.CapExResult capExResult, Capabilities capabilities, long j, long j2) {
        if (capabilities == null || capExResult == CapabilityConstants.CapExResult.USER_NOT_FOUND) {
            IMSLog.i(TAG, this.mPhoneId, "needCapabilitiesUpdate: Capability is null");
            return true;
        } else if (capExResult == CapabilityConstants.CapExResult.UNCLASSIFIED_ERROR || capExResult == CapabilityConstants.CapExResult.FORBIDDEN_403) {
            IMSLog.i(TAG, this.mPhoneId, "needCapabilitiesUpdate: do not change anything");
            return false;
        } else if (capExResult != CapabilityConstants.CapExResult.FAILURE) {
            return true;
        } else {
            return false;
        }
    }

    public long updateAvailableFeatures(Capabilities capabilities, long j, CapabilityConstants.CapExResult capExResult) {
        int i;
        if (capabilities == null) {
            IMSLog.i(TAG, this.mPhoneId, "updateAvailableFeatures: capex is null.");
            return j;
        }
        if (capExResult == CapabilityConstants.CapExResult.USER_UNAVAILABLE) {
            if (capabilities.isAvailable() || ChatbotUriUtil.hasUriBotPlatform(capabilities.getUri(), this.mPhoneId)) {
                i = Capabilities.FEATURE_OFFLINE_RCS_USER;
            } else {
                i = Capabilities.FEATURE_NON_RCS_USER;
            }
            j = (long) i;
        }
        String str = TAG;
        int i2 = this.mPhoneId;
        IMSLog.i(str, i2, "updateAvailableFeatures:" + capabilities + ", mAvailableFeatures " + j);
        return j;
    }
}
