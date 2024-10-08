package com.sec.internal.ims.core.cmc;

import android.content.Context;
import android.text.TextUtils;
import com.sec.internal.ims.core.cmc.CmcConstants;
import java.util.ArrayList;
import java.util.List;

public class CmcSettingManagerTestWrapper extends CmcSettingManagerWrapper {
    public boolean getCmcCallActivation(String str) {
        return true;
    }

    public String getCmcSaAccessToken() {
        return CmcConstants.TestConstants.TEST_ACCESS_TOKEN;
    }

    public boolean getCmcSupported() {
        return true;
    }

    public boolean getOwnCmcActivation() {
        return true;
    }

    public int getPreferredNetwork() {
        return 0;
    }

    public boolean isCallAllowedSdByPd(String str) {
        return true;
    }

    public CmcSettingManagerTestWrapper(Context context, CmcAccountManager cmcAccountManager) {
        super(context, cmcAccountManager);
    }

    public String getDeviceId() {
        if ("pd".equals(getDeviceType())) {
            return getTestPdDeviceId();
        }
        return "sd".equals(getDeviceType()) ? getTestSdDeviceId() : "";
    }

    public String getLineId() {
        return CmcConstants.TestConstants.TEST_LINEID;
    }

    public List<String> getDeviceIdList() {
        ArrayList arrayList = new ArrayList();
        arrayList.add(getTestPdDeviceId());
        arrayList.add(getTestSdDeviceId());
        return arrayList;
    }

    public String getLineImpu() {
        return "sip:" + CmcConstants.TestConstants.TEST_LINEID + "@samsungims.com";
    }

    public String getDeviceTypeWithDeviceId(String str) {
        if (TextUtils.isEmpty(str)) {
            return "";
        }
        if (str.equals(getTestPdDeviceId())) {
            return "pd";
        }
        if (str.equals(getTestSdDeviceId())) {
            return "sd";
        }
        return "";
    }

    public List<String> getPcscfAddressList() {
        ArrayList arrayList = new ArrayList();
        arrayList.add(CmcConstants.TestConstants.DEV_URL);
        return arrayList;
    }

    private String getTestPdDeviceId() {
        return CmcConstants.TestConstants.TEST_PD_DEVICEID;
    }

    private String getTestSdDeviceId() {
        return CmcConstants.TestConstants.TEST_SD_DEVICEID;
    }
}
