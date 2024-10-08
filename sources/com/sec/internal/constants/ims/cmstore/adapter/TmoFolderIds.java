package com.sec.internal.constants.ims.cmstore.adapter;

import com.sec.internal.constants.ims.cmstore.adapter.DeviceConfigAdapterConstants;
import java.util.Arrays;

public enum TmoFolderIds {
    VM_GREETINGS(DeviceConfigAdapterConstants.TmoMstoreServerValues.TmoFolderId.VM_GREETINGS),
    VM_INBOX(DeviceConfigAdapterConstants.TmoMstoreServerValues.TmoFolderId.VM_INBOX);
    
    private final String mName;

    private TmoFolderIds(String str) {
        this.mName = str;
    }

    public String getValue() {
        return this.mName;
    }

    public static boolean equals(String str) {
        return Arrays.stream(values()).anyMatch(new TmoFolderIds$$ExternalSyntheticLambda0(str));
    }
}
