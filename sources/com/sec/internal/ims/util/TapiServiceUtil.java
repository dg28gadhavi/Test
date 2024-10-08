package com.sec.internal.ims.util;

import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.settings.DeviceConfigManager;

public class TapiServiceUtil {
    public static boolean isSupportTapi() {
        int phoneCount = SimUtil.getPhoneCount();
        boolean z = false;
        for (int i = 0; i < phoneCount; i++) {
            boolean z2 = true;
            if (DmConfigHelper.getImsSwitchValue(ImsRegistry.getContext(), DeviceConfigManager.RCS, i) != 1) {
                z2 = false;
            }
            z |= z2;
        }
        return z;
    }
}
