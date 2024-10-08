package com.sec.internal.ims.core.cmc;

import android.os.SemSystemProperties;
import android.text.TextUtils;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.config.ConfigConstants;

public class CmcConstants {
    public static final String E_NUM_SLOT_SPLIT = "-";
    public static final String E_NUM_STR_QUOTE = "\"";
    public static final boolean IS_TEST_MODE;
    public static final String SERVICE_PACKAGE_NAME = "com.samsung.android.mdecservice";
    public static final boolean SHIP_BUILD;
    public static final int START_REGISTRATION_TIME_DELAY = 5;
    public static final String URN_PREFIX = "urn:duid:";

    public static class Profile {
        public static final String DEFAULT_NAME = "SamsungCMC";
        public static final int DEFAULT_PORT = 8000;
        public static final String PD_NAME = "SamsungCMC_PD";
        public static final String SD_NAME = "SamsungCMC_SD";
    }

    public static class SA {
        public static final String ACCOUNT_SP = "cmcaccount";
        public static final int BIND_RETRY_MAX_COUNT = 5;
        public static final int BIND_RETRY_TIME_INTERVAL = 30;
        public static final int REQUEST_EXPIRE_TIMER = 31;
        public static final int REQUEST_RETRY_MAX_COUNT = 3;
        public static final String TOKEN_DEFAULT = (CmcConstants.IS_TEST_MODE ? TestConstants.TEST_ACCESS_TOKEN : "default_token");
        public static final String TOKEN_SP = "accesstoken";
        public static final String URL_DEFAULT = "us-auth2.samsungosp.comus-aut";
        public static final String URL_SP = "saurl";
    }

    public static class SystemProperties {
        public static final String CMC_DEVICE_TYPE_PROP = "ro.cmc.device_type";
    }

    static {
        boolean equals = CloudMessageProviderContract.JsonData.TRUE.equals(SemSystemProperties.get("ro.product_ship", ConfigConstants.VALUE.INFO_COMPLETED));
        SHIP_BUILD = equals;
        IS_TEST_MODE = !equals && TestConstants.TEST_MODE && !TextUtils.isEmpty(TestConstants.TEST_LINEID) && !TextUtils.isEmpty(TestConstants.TEST_PD_DEVICEID) && !TextUtils.isEmpty(TestConstants.TEST_SD_DEVICEID);
    }

    public static class TestConstants {
        public static final String DEV_URL = "pcscf2-c0.ane2.mdc-dev.net:8000";
        public static final String TEST_ACCESS_TOKEN = "testAccessToken";
        public static final String TEST_LINEID = SemSystemProperties.get("persist.cmc.lineid", "");
        public static final boolean TEST_MODE;
        public static final String TEST_PD_DEVICEID = SemSystemProperties.get("persist.cmc.pd_deviceid", "");
        public static final String TEST_SD_DEVICEID = SemSystemProperties.get("persist.cmc.sd_deviceid", "");

        static {
            boolean z = false;
            if (SemSystemProperties.getInt("persist.cmc.testmode", 0) == 1) {
                z = true;
            }
            TEST_MODE = z;
        }
    }
}
