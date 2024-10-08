package com.sec.internal.ims.config.adapters;

import android.content.Context;
import com.sec.internal.interfaces.ims.config.IConfigModule;

public class TelephonyAdapterPrimaryDevice extends TelephonyAdapterPrimaryDeviceBase {
    public TelephonyAdapterPrimaryDevice(Context context, IConfigModule iConfigModule, int i) {
        super(context, iConfigModule, i);
        registerSmsReceiver();
        initState();
    }
}
