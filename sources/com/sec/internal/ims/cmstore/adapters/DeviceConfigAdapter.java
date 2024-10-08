package com.sec.internal.ims.cmstore.adapters;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;
import com.google.gson.JsonSyntaxException;
import com.sec.internal.constants.ims.cmstore.adapter.DeviceConfigAdapterConstants;
import com.sec.internal.constants.ims.cmstore.adapter.TmoFolderIds;
import com.sec.internal.constants.ims.entitilement.EntitlementConfigContract;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.omanetapi.tmoappapi.deviceconfig.DeviceConfig;
import com.sec.internal.ims.cmstore.omanetapi.tmoappapi.deviceconfig.parser.DeviceMstoreConfigParser;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import java.util.HashMap;
import java.util.Map;

public class DeviceConfigAdapter {
    private static final String DEVICE_CONFIG_UPDATED = "com.samsung.nsds.action.DEVICE_CONFIG_UPDATED";
    /* access modifiers changed from: private */
    public String TAG = DeviceConfigAdapter.class.getSimpleName();
    private final Context mContext;
    private final BroadcastReceiver mDeviceConfigUpdatedReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("com.samsung.nsds.action.DEVICE_CONFIG_UPDATED".equals(intent.getAction())) {
                Log.d(DeviceConfigAdapter.this.TAG, "DEVICE_CONFIG_UPDATED received");
                DeviceConfigAdapter.this.parseDeviceConfig();
            }
        }
    };
    private final ContentResolver mResolver;
    private final MessageStoreClient mStoreClient;
    public Map<String, String> mStoreDataMap = new HashMap();

    public DeviceConfigAdapter(MessageStoreClient messageStoreClient, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        String str = this.TAG + "[" + messageStoreClient.getClientID() + "]";
        this.TAG = str;
        Log.d(str, "onCreate()");
        this.mStoreClient = messageStoreClient;
        Context context = messageStoreClient.getContext();
        this.mContext = context;
        this.mResolver = context.getContentResolver();
    }

    private void setTmoFolderIdMStoreDataMap(DeviceConfig.VVMConfig.VVMFolderID vVMFolderID) {
        Log.d(this.TAG, "setTmoFolderIdMStoreDataMap");
        for (DeviceConfig.VVMConfig.FolderName next : vVMFolderID.mFolderName) {
            if (!TextUtils.isEmpty(next.mName) && TmoFolderIds.equals(next.mName)) {
                this.mStoreDataMap.put(next.mName, next.mValue);
            }
        }
    }

    public void registerDeviceConfigUpdatedReceiver(Context context) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.samsung.nsds.action.DEVICE_CONFIG_UPDATED");
        context.registerReceiver(this.mDeviceConfigUpdatedReceiver, intentFilter);
    }

    public void parseDeviceConfig() {
        saveDeviceConfig(getDeviceConfig());
    }

    public void saveDeviceConfig(String str) {
        if (str != null) {
            try {
                DeviceConfig parseDeviceConfig = DeviceMstoreConfigParser.parseDeviceConfig(str);
                if (parseDeviceConfig != null) {
                    DeviceConfig.VVMConfig vVMConfig = parseDeviceConfig.mVVMConfig;
                    if (vVMConfig != null) {
                        if (!TextUtils.isEmpty(vVMConfig.mWsgUri)) {
                            String str2 = parseDeviceConfig.mVVMConfig.mWsgUri;
                            String str3 = this.TAG;
                            Log.d(str3, "vvmConfig.WSG_URI: " + str2);
                            this.mStoreDataMap.put(DeviceConfigAdapterConstants.TmoMstoreServerValues.WSG_URI, str2);
                        }
                        if (!TextUtils.isEmpty(parseDeviceConfig.mVVMConfig.mRootUrl)) {
                            String str4 = parseDeviceConfig.mVVMConfig.mRootUrl;
                            this.mStoreDataMap.put(DeviceConfigAdapterConstants.TmoMstoreServerValues.SIT_URL, str4);
                            String str5 = this.TAG;
                            Log.d(str5, "vvmConfig.rootUtl: " + str4);
                        }
                        DeviceConfig.VVMConfig.VVMFolderID vVMFolderID = parseDeviceConfig.mVVMConfig.mVVMFolderID;
                        if (vVMFolderID != null) {
                            setTmoFolderIdMStoreDataMap(vVMFolderID);
                        }
                    }
                }
                Log.e(this.TAG, "deviceConfiguration is null");
                return;
            } catch (JsonSyntaxException unused) {
                Log.e(this.TAG, "saveDeviceConfig: malformed device config xml");
            }
        } else {
            Log.e(this.TAG, "!!!!Device Config XML is NULL!!!!");
        }
        this.mStoreClient.getCloudMessageStrategyManager().getStrategy().setDeviceConfigUsed(this.mStoreDataMap);
    }

    public String getDeviceConfig() {
        Cursor query = this.mResolver.query(EntitlementConfigContract.DeviceConfig.CONTENT_URI, new String[]{"device_config"}, "imsi = ?", new String[]{this.mStoreClient.getCurrentIMSI()}, (String) null);
        if (query != null) {
            try {
                if (query.moveToFirst()) {
                    String string = query.getString(0);
                    query.close();
                    return string;
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (query == null) {
            return null;
        }
        query.close();
        return null;
        throw th;
    }
}
