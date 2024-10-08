package com.sec.internal.ims.servicemodules.tapi.service.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.interfaces.ims.servicemodules.session.ISessionModule;

public class PackageEventReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        if (intent.getDataString() != null) {
            String replaceFirst = intent.getDataString().replaceFirst("package:", "");
            String action = intent.getAction();
            ISessionModule sessionModule = ImsRegistry.getServiceModuleManager().getSessionModule();
            if (sessionModule != null) {
                if (action.equals("android.intent.action.PACKAGE_ADDED")) {
                    if (sessionModule.needRegister(replaceFirst)) {
                        sessionModule.registerApp();
                    }
                } else if (action.equals("android.intent.action.PACKAGE_REMOVED") && sessionModule.needDeRegister(replaceFirst)) {
                    sessionModule.deRegisterApp();
                }
            }
        }
    }
}
