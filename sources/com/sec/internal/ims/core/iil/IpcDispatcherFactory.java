package com.sec.internal.ims.core.iil;

import android.os.SemSystemProperties;
import com.sec.internal.constants.ims.ImsConstants;

public class IpcDispatcherFactory {
    public static IpcDispatcher getIpcDispatcher(int i) {
        if (SemSystemProperties.getInt(ImsConstants.SystemProperties.FIRST_API_VERSION_VENDOR, 0) < 33) {
            return new IpcDispatcherHidl(i);
        }
        return new IpcDispatcherAidl(i);
    }
}
