package com.sec.internal.constants.ims.cmstore.adapter;

import com.sec.internal.constants.ims.cmstore.adapter.DeviceConfigAdapterConstants;
import java.util.Arrays;

public enum MstoreServerValues {
    SERVER_ROOT(DeviceConfigAdapterConstants.TmoMstoreServerValues.SERVER_ROOT),
    API_VERSION(DeviceConfigAdapterConstants.TmoMstoreServerValues.API_VERSION),
    STORE_NAME(DeviceConfigAdapterConstants.TmoMstoreServerValues.STORE_NAME),
    SIT_URL(DeviceConfigAdapterConstants.TmoMstoreServerValues.SIT_URL),
    AKA_URL(DeviceConfigAdapterConstants.TmoMstoreServerValues.AKA_URL),
    PUSH_SYNC_DELAY(DeviceConfigAdapterConstants.TmoMstoreServerValues.PUSH_SYNC_DELAY),
    DISABLE_DIRECTION_HEADER(DeviceConfigAdapterConstants.TmoMstoreServerValues.DISABLE_DIRECTION_HEADER),
    SYNC_TIMER("SyncTimer"),
    DATA_CONNECTION_SYNC_TIMER("DataConnectionSyncTimer"),
    AUTH_PROT("AuthProt"),
    USER_NAME("UserName"),
    USER_PWD("UserPwd"),
    EVENT_RPTING("EventRpting"),
    SMS_STORE("SMSStore"),
    MMS_STORE("MMSStore"),
    MAX_BULK_DELETE(DeviceConfigAdapterConstants.TmoMstoreServerValues.MAX_BULK_DELETE),
    MAX_SEARCH(DeviceConfigAdapterConstants.TmoMstoreServerValues.MAX_SEARCH);
    
    private String mName;

    private MstoreServerValues(String str) {
        this.mName = str;
    }

    public String getValue() {
        return this.mName;
    }

    public static boolean equals(String str) {
        return Arrays.stream(values()).anyMatch(new MstoreServerValues$$ExternalSyntheticLambda0(str));
    }
}
