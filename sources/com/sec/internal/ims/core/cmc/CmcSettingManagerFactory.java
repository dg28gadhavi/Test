package com.sec.internal.ims.core.cmc;

import android.content.Context;
import android.util.Log;
import com.sec.internal.ims.core.cmc.CmcConstants;

public class CmcSettingManagerFactory {
    private static final String LOG_TAG = "CmcSettingManagerFactory";

    public static CmcSettingManagerWrapper createCmcSettingManager(Context context, CmcAccountManager cmcAccountManager) {
        if (!CmcConstants.IS_TEST_MODE) {
            return new CmcSettingManagerWrapper(context, cmcAccountManager);
        }
        String str = LOG_TAG;
        Log.i(str, "Cmc Test Mode Enabled: LINE_ID [" + CmcConstants.TestConstants.TEST_LINEID + "] PD_DEVICEID [" + CmcConstants.TestConstants.TEST_PD_DEVICEID + "] SD_DEVICEID [" + CmcConstants.TestConstants.TEST_SD_DEVICEID + "]");
        return new CmcSettingManagerTestWrapper(context, cmcAccountManager);
    }
}
