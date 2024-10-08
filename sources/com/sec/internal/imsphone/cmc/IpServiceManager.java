package com.sec.internal.imsphone.cmc;

import android.content.Context;
import android.os.IBinder;
import android.os.INetworkManagementService;
import android.os.ServiceManager;
import android.util.Log;

public class IpServiceManager {
    private static final String LOG_TAG = "IpServiceManager";
    private Context mContext;
    private INetworkManagementService mNetworkService = null;

    public IpServiceManager(Context context) {
        this.mContext = context;
        IBinder service = ServiceManager.getService("network_management");
        if (service != null) {
            this.mNetworkService = INetworkManagementService.Stub.asInterface(service);
        } else {
            Log.e(LOG_TAG, "bind failed");
        }
    }

    public void ipRuleAdd(String str, String str2) {
        try {
            if (this.mNetworkService != null) {
                String str3 = LOG_TAG;
                Log.d(str3, "try to [add] iprule: " + str2 + ", in " + str);
            }
        } catch (Exception unused) {
            Log.e(LOG_TAG, "add iprule error");
        }
    }

    public void ipRuleRemove(String str, String str2) {
        try {
            if (this.mNetworkService != null) {
                String str3 = LOG_TAG;
                Log.d(str3, "try to [delete] prve iprule in " + str);
            }
        } catch (Exception unused) {
            Log.e(LOG_TAG, "remove iprule error");
        }
    }
}
