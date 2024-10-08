package com.samsung.android.cmcsetting;

import android.content.Context;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SemSystemProperties;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import com.samsung.android.cmcsetting.CmcSettingManagerConstants;
import com.samsung.android.cmcsetting.listeners.CmcActivationInfoChangedListener;
import com.samsung.android.cmcsetting.listeners.CmcCallActivationInfoChangedListener;
import com.samsung.android.cmcsetting.listeners.CmcDeviceInfoChangedListener;
import com.samsung.android.cmcsetting.listeners.CmcLineInfoChangedListener;
import com.samsung.android.cmcsetting.listeners.CmcMessageActivationInfoChangedListener;
import com.samsung.android.cmcsetting.listeners.CmcNetworkModeInfoChangedListener;
import com.samsung.android.cmcsetting.listeners.CmcSameWifiNetworkStatusListener;
import com.samsung.android.cmcsetting.listeners.CmcSamsungAccountInfoChangedListener;
import com.samsung.android.cmcsetting.listeners.CmcWatchActivationInfoChangedListener;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.entitilement.NSDSContractExt;
import com.sec.internal.ims.core.cmc.CmcConstants;
import com.sec.internal.ims.servicemodules.im.interfaces.ImIntent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class CmcSettingManager {
    private static boolean IS_DUAL_SIM_SUPPORTED = (SemSystemProperties.getInt("ro.build.version.oneui", -1) >= 50100);
    private static boolean IS_MORE_THAN_U_OS = true;
    private static boolean SHIP_BUILD = CloudMessageProviderContract.JsonData.TRUE.equals(SemSystemProperties.get("ro.product_ship", ConfigConstants.VALUE.INFO_COMPLETED));
    private Uri authorityUri = Uri.parse("content://com.samsung.android.mdec.provider.setting");
    private Uri authorityUriForCmcActivation = Uri.parse("content://com.samsung.android.mdec.provider.setting/cmc_activation");
    private Uri authorityUriForCmcCallActivation = Uri.parse("content://com.samsung.android.mdec.provider.setting/cmc_call_activation");
    private Uri authorityUriForCmcMessageActivation = Uri.parse("content://com.samsung.android.mdec.provider.setting/cmc_message_activation");
    private Uri authorityUriForDevices = Uri.parse("content://com.samsung.android.mdec.provider.setting/devices");
    private Uri authorityUriForLines = Uri.parse("content://com.samsung.android.mdec.provider.setting/lines");
    private Uri authorityUriForNetworkMode = Uri.parse("content://com.samsung.android.mdec.provider.setting/network_mode");
    private Uri authorityUriForSaInfo = Uri.parse("content://com.samsung.android.mdec.provider.setting/sainfo");
    private Uri authorityUriForSameWifiNetworkStatus = Uri.parse("content://com.samsung.android.mdec.provider.setting/same_wifi_network_status");
    private Uri authorityUriForWatchActivation = Uri.parse("content://com.samsung.android.mdec.provider.setting/watch_activation");
    /* access modifiers changed from: private */
    public ArrayList<CmcActivationInfoChangedListener> mCmcActivationChangedListenerList = null;
    private ContentObserver mCmcActivationDbChangeObserver = null;
    /* access modifiers changed from: private */
    public ArrayList<CmcCallActivationInfoChangedListener> mCmcCallActivationChangedListenerList = null;
    private ContentObserver mCmcCallActivationDbChangeObserver = null;
    /* access modifiers changed from: private */
    public ArrayList<CmcDeviceInfoChangedListener> mCmcDeviceInfoChangedListenerList = null;
    /* access modifiers changed from: private */
    public ArrayList<CmcLineInfoChangedListener> mCmcLineInfoChangedListenerList = null;
    /* access modifiers changed from: private */
    public ArrayList<CmcMessageActivationInfoChangedListener> mCmcMessageActivationChangedListenerList = null;
    private ContentObserver mCmcMessageActivationDbChangeObserver = null;
    /* access modifiers changed from: private */
    public ArrayList<CmcNetworkModeInfoChangedListener> mCmcNetworkModeChangedListenerList = null;
    /* access modifiers changed from: private */
    public ArrayList<CmcSamsungAccountInfoChangedListener> mCmcSamsungAccountInfoChangedListenerList = null;
    /* access modifiers changed from: private */
    public ArrayList<CmcWatchActivationInfoChangedListener> mCmcWatchActivationChangedListenerList = null;
    private Context mContext = null;
    private ContentObserver mDevicesDbChangeObserver = null;
    private ContentObserver mLinesDbChangeObserver = null;
    private ContentObserver mNetworkModeDbChangeObserver = null;
    private ContentObserver mSaInfoDbChangeObserver = null;
    /* access modifiers changed from: private */
    public ArrayList<CmcSameWifiNetworkStatusListener> mSameWifiNetworkStatusListenerList = null;
    private ContentObserver mSameWifiNetworkStatusObserver = null;
    private ContentObserver mWatchActivationDbChangeObserver = null;

    private enum OBSERVER_TYPE {
        mainActivation,
        messageActivation,
        callActivation,
        watchActivation,
        networkMode,
        lineInfo,
        deviceInfo,
        saInfo,
        sameWifiNetworkStatus,
        all
    }

    public boolean init(Context context) {
        Log.i("CmcSettingManager", "init : CmcSettingManager version : 2.1.2, context : " + context);
        if (context == null) {
            Log.e("CmcSettingManager", "context is null");
            return false;
        }
        this.mContext = context;
        return isCmcPackageInstalled(context);
    }

    public void deInit() {
        Log.i("CmcSettingManager", "deInit");
        unregisterListener();
        this.mContext = null;
    }

    public boolean registerListener(CmcActivationInfoChangedListener cmcActivationInfoChangedListener) {
        Log.d("CmcSettingManager", "registerListener : CmcActivationInfoChangedListener");
        if (this.mContext == null) {
            Log.e("CmcSettingManager", "context is null");
            return false;
        } else if (cmcActivationInfoChangedListener == null) {
            Log.e("CmcSettingManager", "listener is null");
            return false;
        } else {
            if (this.mCmcActivationChangedListenerList == null) {
                this.mCmcActivationChangedListenerList = new ArrayList<>();
            }
            this.mCmcActivationChangedListenerList.add(cmcActivationInfoChangedListener);
            registerObserver(OBSERVER_TYPE.mainActivation);
            return true;
        }
    }

    public boolean registerListener(CmcCallActivationInfoChangedListener cmcCallActivationInfoChangedListener) {
        Log.d("CmcSettingManager", "registerListener : CmcCallActivationInfoChangedListener");
        if (this.mContext == null) {
            Log.e("CmcSettingManager", "context is null");
            return false;
        } else if (cmcCallActivationInfoChangedListener == null) {
            Log.e("CmcSettingManager", "listener is null");
            return false;
        } else {
            if (this.mCmcCallActivationChangedListenerList == null) {
                this.mCmcCallActivationChangedListenerList = new ArrayList<>();
            }
            this.mCmcCallActivationChangedListenerList.add(cmcCallActivationInfoChangedListener);
            registerObserver(OBSERVER_TYPE.callActivation);
            return true;
        }
    }

    public boolean registerListener(CmcNetworkModeInfoChangedListener cmcNetworkModeInfoChangedListener) {
        Log.d("CmcSettingManager", "registerListener : CmcNetworkModeInfoChangedListener");
        if (this.mContext == null) {
            Log.e("CmcSettingManager", "context is null");
            return false;
        } else if (cmcNetworkModeInfoChangedListener == null) {
            Log.e("CmcSettingManager", "listener is null");
            return false;
        } else {
            if (this.mCmcNetworkModeChangedListenerList == null) {
                this.mCmcNetworkModeChangedListenerList = new ArrayList<>();
            }
            this.mCmcNetworkModeChangedListenerList.add(cmcNetworkModeInfoChangedListener);
            registerObserver(OBSERVER_TYPE.networkMode);
            return true;
        }
    }

    public boolean registerListener(CmcLineInfoChangedListener cmcLineInfoChangedListener) {
        Log.d("CmcSettingManager", "registerListener : CmcLineInfoChangedListener");
        if (this.mContext == null) {
            Log.e("CmcSettingManager", "context is null");
            return false;
        } else if (cmcLineInfoChangedListener == null) {
            Log.e("CmcSettingManager", "listener is null");
            return false;
        } else {
            if (this.mCmcLineInfoChangedListenerList == null) {
                this.mCmcLineInfoChangedListenerList = new ArrayList<>();
            }
            this.mCmcLineInfoChangedListenerList.add(cmcLineInfoChangedListener);
            registerObserver(OBSERVER_TYPE.lineInfo);
            return true;
        }
    }

    public boolean registerListener(CmcDeviceInfoChangedListener cmcDeviceInfoChangedListener) {
        Log.d("CmcSettingManager", "registerListener : CmcDeviceInfoChangedListener");
        if (this.mContext == null) {
            Log.e("CmcSettingManager", "context is null");
            return false;
        } else if (cmcDeviceInfoChangedListener == null) {
            Log.e("CmcSettingManager", "listener is null");
            return false;
        } else {
            if (this.mCmcDeviceInfoChangedListenerList == null) {
                this.mCmcDeviceInfoChangedListenerList = new ArrayList<>();
            }
            this.mCmcDeviceInfoChangedListenerList.add(cmcDeviceInfoChangedListener);
            registerObserver(OBSERVER_TYPE.deviceInfo);
            return true;
        }
    }

    public boolean registerListener(CmcSamsungAccountInfoChangedListener cmcSamsungAccountInfoChangedListener) {
        Log.d("CmcSettingManager", "registerListener : CmcSamsungAccountInfoChangedListener");
        if (this.mContext == null) {
            Log.e("CmcSettingManager", "context is null");
            return false;
        } else if (cmcSamsungAccountInfoChangedListener == null) {
            Log.e("CmcSettingManager", "listener is null");
            return false;
        } else {
            if (this.mCmcSamsungAccountInfoChangedListenerList == null) {
                this.mCmcSamsungAccountInfoChangedListenerList = new ArrayList<>();
            }
            this.mCmcSamsungAccountInfoChangedListenerList.add(cmcSamsungAccountInfoChangedListener);
            registerObserver(OBSERVER_TYPE.saInfo);
            return true;
        }
    }

    public boolean registerListener(CmcSameWifiNetworkStatusListener cmcSameWifiNetworkStatusListener) {
        Log.d("CmcSettingManager", "registerListener : CmcSameWifiNetworkStatusListener");
        if (this.mContext == null) {
            Log.e("CmcSettingManager", "context is null");
            return false;
        } else if (cmcSameWifiNetworkStatusListener == null) {
            Log.e("CmcSettingManager", "listener is null");
            return false;
        } else {
            if (this.mSameWifiNetworkStatusListenerList == null) {
                this.mSameWifiNetworkStatusListenerList = new ArrayList<>();
            }
            this.mSameWifiNetworkStatusListenerList.add(cmcSameWifiNetworkStatusListener);
            registerObserver(OBSERVER_TYPE.sameWifiNetworkStatus);
            return true;
        }
    }

    public boolean unregisterListener() {
        Log.d("CmcSettingManager", "unregisterListener : all");
        if (this.mContext == null) {
            Log.e("CmcSettingManager", "context is null");
            return false;
        }
        Log.d("CmcSettingManager", "context : " + this.mContext);
        this.mCmcActivationChangedListenerList = null;
        this.mCmcMessageActivationChangedListenerList = null;
        this.mCmcCallActivationChangedListenerList = null;
        this.mCmcWatchActivationChangedListenerList = null;
        this.mCmcNetworkModeChangedListenerList = null;
        this.mCmcLineInfoChangedListenerList = null;
        this.mCmcDeviceInfoChangedListenerList = null;
        this.mCmcSamsungAccountInfoChangedListenerList = null;
        this.mSameWifiNetworkStatusListenerList = null;
        unregisterObserver(OBSERVER_TYPE.all);
        return true;
    }

    public boolean getCmcSupported() {
        Log.d("CmcSettingManager", "getCmcSupported");
        return isCmcPackageInstalled(this.mContext);
    }

    public boolean getOwnCmcActivation() {
        Log.d("CmcSettingManager", "getOwnCmcActivation");
        Context context = this.mContext;
        if (context == null) {
            Log.e("CmcSettingManager", "context is null");
            return false;
        }
        int i = Settings.Global.getInt(context.getContentResolver(), "cmc_activation", 0);
        Log.i("CmcSettingManager", "cmc activation : " + i);
        if (i == 1) {
            return true;
        }
        return false;
    }

    public boolean getCmcCallActivation(String str) {
        Log.i("CmcSettingManager", "getCmcCallActivation : " + str);
        if (this.mContext == null) {
            Log.e("CmcSettingManager", "context is null");
            return false;
        }
        try {
            Bundle bundle = new Bundle();
            bundle.putString("device_id", str);
            Bundle call = this.mContext.getContentResolver().call(this.authorityUri, "v1/get_activations", (String) null, bundle);
            if (call == null) {
                return false;
            }
            if (call.getInt("result", -1) == 1) {
                int i = call.getInt("call_activation", -1);
                Log.i("CmcSettingManager", "call inf : getCmcCallActivation success : " + i);
                if (i == 1) {
                    return true;
                }
                return false;
            }
            Log.e("CmcSettingManager", "call inf : getCmcCallActivation fail : " + call.getString(ImIntent.Extras.ERROR_REASON));
            return false;
        } catch (Exception e) {
            Log.e("CmcSettingManager", "exception is occured : " + e.toString());
            return false;
        }
    }

    public CmcSettingManagerConstants.DeviceType getOwnDeviceType() {
        Log.d("CmcSettingManager", "getOwnDeviceType");
        Context context = this.mContext;
        if (context == null) {
            Log.e("CmcSettingManager", "context is null");
            return null;
        }
        String string = Settings.Global.getString(context.getContentResolver(), "cmc_device_type");
        Log.d("CmcSettingManager", "own device type - db : " + string);
        if (!TextUtils.isEmpty(string)) {
            return getDeviceTypeInternal(string);
        }
        PackageManager packageManager = this.mContext.getPackageManager();
        if (packageManager != null) {
            return packageManager.hasSystemFeature("com.samsung.feature.device_category_tablet") ? CmcSettingManagerConstants.DeviceType.DEVICE_TYPE_SD : CmcSettingManagerConstants.DeviceType.DEVICE_TYPE_PD;
        }
        String str = SemSystemProperties.get("ro.build.characteristics", "");
        Log.d("CmcSettingManager", "own device type - characteristics : " + str);
        return str.contains("tablet") ? CmcSettingManagerConstants.DeviceType.DEVICE_TYPE_SD : CmcSettingManagerConstants.DeviceType.DEVICE_TYPE_PD;
    }

    public String getOwnDeviceId() {
        Log.d("CmcSettingManager", "getOwnDeviceId");
        Context context = this.mContext;
        String str = null;
        if (context == null) {
            Log.e("CmcSettingManager", "context is null");
            return null;
        }
        try {
            Bundle call = context.getContentResolver().call(this.authorityUri, "v1/get_own_device_id", (String) null, (Bundle) null);
            if (call != null) {
                if (call.getInt("result", -1) == 1) {
                    str = call.getString("own_device_id", "");
                    Log.d("CmcSettingManager", "call inf : getOwnDeviceId success : " + str);
                } else {
                    Log.e("CmcSettingManager", "call inf : getOwnDeviceId fail : " + call.getString(ImIntent.Extras.ERROR_REASON));
                    str = "";
                }
            }
        } catch (Exception e) {
            Log.e("CmcSettingManager", "exception is occurred : " + e.toString());
            str = Settings.Global.getString(this.mContext.getContentResolver(), "cmc_device_id");
        }
        Log.d("CmcSettingManager", "own device id: " + str);
        return str;
    }

    public String getOwnServiceVersion() {
        Log.d("CmcSettingManager", "getOwnServiceVersion");
        Context context = this.mContext;
        if (context == null) {
            Log.e("CmcSettingManager", "context is null");
            return null;
        }
        String string = Settings.Global.getString(context.getContentResolver(), "cmc_service_version");
        Log.d("CmcSettingManager", "own service version in global : " + string);
        if (!TextUtils.isEmpty(string)) {
            return string;
        }
        String str = SemSystemProperties.get("ro.cmc.version", "");
        Log.d("CmcSettingManager", "own service version in prop : " + str);
        return str;
    }

    public CmcSettingManagerConstants.NetworkMode getOwnNetworkMode() {
        Log.d("CmcSettingManager", "getOwnNetworkMode");
        if (this.mContext != null) {
            return getNetworkModeInternal();
        }
        Log.e("CmcSettingManager", "context is null");
        return null;
    }

    private CmcSettingManagerConstants.NetworkMode getNetworkModeInternal() {
        int i = Settings.Global.getInt(this.mContext.getContentResolver(), "cmc_network_type", -1);
        Log.d("CmcSettingManager", "own network mode : " + i);
        if (i == 0) {
            return CmcSettingManagerConstants.NetworkMode.NETWORK_MODE_USE_MOBILE_NETWORK;
        }
        if (1 == i) {
            return CmcSettingManagerConstants.NetworkMode.NETWORK_MODE_WIFI_ONLY;
        }
        return CmcSettingManagerConstants.NetworkMode.NETWORK_MODE_UNDEFINED;
    }

    public String getLineId() {
        Log.d("CmcSettingManager", "getLineId");
        Context context = this.mContext;
        if (context == null) {
            Log.e("CmcSettingManager", "context is null");
            return null;
        }
        try {
            Bundle call = context.getContentResolver().call(this.authorityUri, "v1/get_line_id", (String) null, (Bundle) null);
            if (call == null) {
                return null;
            }
            if (call.getInt("result", -1) == 1) {
                String string = call.getString(NSDSContractExt.ServiceColumns.LINE_ID, "");
                Log.d("CmcSettingManager", "getLineId success : " + inspector(string));
                return string;
            }
            Log.e("CmcSettingManager", "getLineId fail : " + call.getString(ImIntent.Extras.ERROR_REASON));
            return "";
        } catch (Exception e) {
            Log.e("CmcSettingManager", "exception is occured : " + e.toString());
            return null;
        }
    }

    public ArrayList<String> getLinePcscfAddrList() {
        Log.d("CmcSettingManager", "getLinePcscfAddrList");
        Context context = this.mContext;
        if (context == null) {
            Log.e("CmcSettingManager", "context is null");
            return null;
        }
        try {
            Bundle call = context.getContentResolver().call(this.authorityUri, "v1/get_line_pcscf_addr_list", (String) null, (Bundle) null);
            if (call == null) {
                return null;
            }
            if (call.getInt("result", -1) == 1) {
                ArrayList<String> stringArrayList = call.getStringArrayList("pcscf_addr_list");
                Log.d("CmcSettingManager", "call inf : getPcscfAddrList success : " + inspector(stringArrayList));
                return stringArrayList;
            }
            Log.e("CmcSettingManager", "call inf : getPcscfAddrList fail : " + call.getString(ImIntent.Extras.ERROR_REASON));
            return null;
        } catch (Exception e) {
            Log.e("CmcSettingManager", "exception is occured : " + e.toString());
            return null;
        }
    }

    public String getLineImpu() {
        Log.d("CmcSettingManager", "getLineImpu");
        Context context = this.mContext;
        if (context == null) {
            Log.e("CmcSettingManager", "context is null");
            return null;
        }
        try {
            Bundle call = context.getContentResolver().call(this.authorityUri, "v1/get_line_impu", (String) null, (Bundle) null);
            if (call == null) {
                return null;
            }
            if (call.getInt("result", -1) == 1) {
                String string = call.getString("impu", "");
                Log.d("CmcSettingManager", "call inf : getLineImpu success : " + inspector(string));
                return string;
            }
            Log.e("CmcSettingManager", "call inf : getLineImpu fail : " + call.getString(ImIntent.Extras.ERROR_REASON));
            return "";
        } catch (Exception e) {
            Log.e("CmcSettingManager", "exception is occured : " + e.toString());
            return null;
        }
    }

    public ArrayList<String> getDeviceIdList() {
        Log.d("CmcSettingManager", "getDeviceIdList");
        Context context = this.mContext;
        if (context == null) {
            Log.e("CmcSettingManager", "context is null");
            return null;
        }
        try {
            Bundle call = context.getContentResolver().call(this.authorityUri, "v1/get_device_id_list", (String) null, (Bundle) null);
            if (call == null) {
                return null;
            }
            if (call.getInt("result", -1) == 1) {
                ArrayList<String> stringArrayList = call.getStringArrayList("device_id_list");
                Log.d("CmcSettingManager", "call inf : getDeviceIdList success : " + stringArrayList);
                return stringArrayList;
            }
            Log.e("CmcSettingManager", "call inf : getDeviceIdList fail : " + call.getString(ImIntent.Extras.ERROR_REASON));
            return null;
        } catch (Exception e) {
            Log.e("CmcSettingManager", "exception is occured : " + e.toString());
            return null;
        }
    }

    public CmcSettingManagerConstants.DeviceType getDeviceType(String str) {
        Log.d("CmcSettingManager", "getDeviceType : " + str);
        if (this.mContext == null) {
            Log.e("CmcSettingManager", "context is null");
            return null;
        }
        CmcSettingManagerConstants.DeviceType deviceType = CmcSettingManagerConstants.DeviceType.DEVICE_TYPE_UNDEFINED;
        try {
            Bundle bundle = new Bundle();
            bundle.putString("device_id", str);
            Bundle call = this.mContext.getContentResolver().call(this.authorityUri, "v1/get_device_type", (String) null, bundle);
            if (call == null) {
                return deviceType;
            }
            if (call.getInt("result", -1) == 1) {
                String string = call.getString(NSDSContractExt.DeviceColumns.DEVICE_TYPE, "");
                Log.d("CmcSettingManager", "call inf : getDeviceType success : " + string);
                return getDeviceTypeInternal(string);
            }
            Log.e("CmcSettingManager", "call inf : getDeviceType fail : " + call.getString(ImIntent.Extras.ERROR_REASON));
            return deviceType;
        } catch (Exception e) {
            Log.e("CmcSettingManager", "exception is occured : " + e.toString());
            return deviceType;
        }
    }

    public boolean isCallAllowedSdByPd(String str) {
        Log.d("CmcSettingManager", "isCallAllowedSdByPd : " + str);
        if (this.mContext == null) {
            Log.e("CmcSettingManager", "context is null");
            return false;
        }
        try {
            Bundle bundle = new Bundle();
            bundle.putString("device_id", str);
            Bundle call = this.mContext.getContentResolver().call(this.authorityUri, "v1/get_call_allowed_sd_by_pd", (String) null, bundle);
            if (call == null) {
                return false;
            }
            if (call.getInt("result", -1) == 1) {
                int i = call.getInt("call_allowed_sd_by_pd", -1);
                Log.d("CmcSettingManager", "call inf : isCallAllowedSdByPd success : " + i);
                if (i == 1) {
                    return true;
                }
                return false;
            }
            Log.e("CmcSettingManager", "call inf : isCallAllowedSdByPd fail : " + call.getString(ImIntent.Extras.ERROR_REASON));
            return false;
        } catch (Exception e) {
            Log.e("CmcSettingManager", "exception is occured : " + e.toString());
            return false;
        }
    }

    public CmcDeviceInfo getDeviceInfo(String str) {
        Log.d("CmcSettingManager", "getDeviceInfo : " + str);
        if (this.mContext == null) {
            Log.e("CmcSettingManager", "context is null");
            return null;
        }
        try {
            Bundle bundle = new Bundle();
            bundle.putString("device_id", str);
            Bundle call = this.mContext.getContentResolver().call(this.authorityUri, "v1/get_device_info", (String) null, bundle);
            if (call == null) {
                return null;
            }
            if (call.getInt("result", -1) == 1) {
                CmcDeviceInfo cmcDeviceInfo = new CmcDeviceInfo();
                cmcDeviceInfo.setDeviceId(str);
                for (String str2 : call.keySet()) {
                    if ("device_name".equalsIgnoreCase(str2)) {
                        cmcDeviceInfo.setDeviceName(call.getString(str2));
                    } else if ("device_category".equalsIgnoreCase(str2)) {
                        cmcDeviceInfo.setDeviceCategory(getDeviceCategoryInternal(call.getString(str2)));
                    } else if (NSDSContractExt.DeviceColumns.DEVICE_TYPE.equalsIgnoreCase(str2)) {
                        cmcDeviceInfo.setDeviceType(getDeviceTypeInternal(call.getString(str2)));
                    } else {
                        boolean z = false;
                        if ("activation".equalsIgnoreCase(str2)) {
                            if (call.getInt(str2) == 1) {
                                z = true;
                            }
                            cmcDeviceInfo.setActivation(z);
                        } else if ("message_activation".equalsIgnoreCase(str2)) {
                            if (call.getInt(str2) == 1) {
                                z = true;
                            }
                            cmcDeviceInfo.setMessageActivation(z);
                        } else if ("call_activation".equalsIgnoreCase(str2)) {
                            if (call.getInt(str2) == 1) {
                                z = true;
                            }
                            cmcDeviceInfo.setCallActivation(z);
                        } else if ("message_allowed_sd_by_pd".equalsIgnoreCase(str2)) {
                            if (call.getInt(str2) == 1) {
                                z = true;
                            }
                            cmcDeviceInfo.setMessageAllowedSdByPd(z);
                        } else if ("call_allowed_sd_by_pd".equalsIgnoreCase(str2)) {
                            if (call.getInt(str2) == 1) {
                                z = true;
                            }
                            cmcDeviceInfo.setCallAllowedSdByPd(z);
                        } else if ("emergency_supported".equalsIgnoreCase(str2)) {
                            cmcDeviceInfo.setEmergencyCallSupported(call.getBoolean(str2));
                        }
                    }
                }
                return cmcDeviceInfo;
            }
            Log.e("CmcSettingManager", "call inf : getDeviceInfo fail : " + call.getString(ImIntent.Extras.ERROR_REASON));
            return null;
        } catch (Exception e) {
            Log.e("CmcSettingManager", "exception is occured : " + e.toString());
            return null;
        }
    }

    public CmcSaInfo getSamsungAccountInfo() {
        Log.d("CmcSettingManager", "getSamsungAccountInfo");
        if (this.mContext == null) {
            Log.e("CmcSettingManager", "context is null");
            return null;
        }
        CmcSaInfo cmcSaInfo = new CmcSaInfo();
        try {
            Bundle call = this.mContext.getContentResolver().call(this.authorityUri, "v1/get_sa_info", (String) null, (Bundle) null);
            if (call != null) {
                if (call.getInt("result", -1) == 1) {
                    cmcSaInfo.setSaUserId(call.getString("samsung_user_id"));
                    cmcSaInfo.setSaAccessToken(call.getString("access_token"));
                    Log.d("CmcSettingManager", "call inf : getSamsungAccountInfo success");
                } else {
                    Log.e("CmcSettingManager", "call inf : getSamsungAccountInfo fail : " + call.getString(ImIntent.Extras.ERROR_REASON));
                }
            }
        } catch (Exception e) {
            Log.e("CmcSettingManager", "exception is occured : " + e.toString());
        }
        return cmcSaInfo;
    }

    public boolean isSameWifiNetworkOnly() {
        Log.d("CmcSettingManager", "isSameWifiNetworkOnly");
        Context context = this.mContext;
        if (context == null) {
            Log.e("CmcSettingManager", "context is null");
            return false;
        }
        int i = Settings.Global.getInt(context.getContentResolver(), "cmc_same_wifi_network_status", 0);
        Log.d("CmcSettingManager", "sameWifiNetworkStatus : " + i);
        if (i == 1) {
            return true;
        }
        return false;
    }

    public boolean isEmergencyCallSupported() {
        Log.d("CmcSettingManager", "isEmergencyCallSupported");
        if (this.mContext == null) {
            Log.e("CmcSettingManager", "context is null");
            return false;
        } else if (!getOwnCmcActivation()) {
            Log.e("CmcSettingManager", "cmc activation is false");
            return false;
        } else {
            ArrayList<String> deviceIdList = getDeviceIdList();
            if (deviceIdList == null || deviceIdList.size() <= 0) {
                Log.e("CmcSettingManager", "deviceIdList is empty");
                return false;
            }
            String ownDeviceId = getOwnDeviceId();
            if (TextUtils.isEmpty(ownDeviceId)) {
                Log.e("CmcSettingManager", "ownDeviceId is empty");
                return false;
            }
            Iterator<String> it = deviceIdList.iterator();
            boolean z = false;
            boolean z2 = false;
            while (it.hasNext()) {
                String next = it.next();
                CmcDeviceInfo deviceInfo = getDeviceInfo(next);
                if (deviceInfo != null) {
                    if (deviceInfo.getDeviceType() == CmcSettingManagerConstants.DeviceType.DEVICE_TYPE_PD) {
                        z2 = deviceInfo.isEmergencyCallSupported();
                    }
                    if (ownDeviceId.equalsIgnoreCase(next)) {
                        z = deviceInfo.isEmergencyCallSupported();
                    }
                }
            }
            Log.d("CmcSettingManager", "isOwnEmergencyCallSupported(" + z + "), isPdEmergencyCallSupported(" + z2 + ")");
            if (!z || !z2) {
                return false;
            }
            return true;
        }
    }

    public boolean isDualSimSupportedOnPd() {
        int i;
        Log.d("CmcSettingManager", "isDualSimSupportedOnPd");
        if (this.mContext == null) {
            Log.e("CmcSettingManager", "context is null");
            return false;
        } else if (!isApiSupportedWithDualSimSupported()) {
            return false;
        } else {
            if (IS_MORE_THAN_U_OS) {
                CmcSettingManagerConstants.DeviceType ownDeviceType = getOwnDeviceType();
                if (ownDeviceType == CmcSettingManagerConstants.DeviceType.DEVICE_TYPE_PD) {
                    i = Settings.Global.getInt(this.mContext.getContentResolver(), CmcSettingManagerConstants.SETTINGS_KEY_CMC_IS_DUAL_SIM_SUPPORTED, CmcSettingManagerConstants.KEY_NOT_EXIST);
                } else if (ownDeviceType != CmcSettingManagerConstants.DeviceType.DEVICE_TYPE_SD) {
                    return false;
                } else {
                    i = Settings.Global.getInt(this.mContext.getContentResolver(), CmcSettingManagerConstants.SETTINGS_KEY_CMC_IS_DUAL_SIM_SUPPORTED_ON_PD, CmcSettingManagerConstants.KEY_NOT_EXIST);
                }
                if (i != CmcSettingManagerConstants.KEY_NOT_EXIST) {
                    Log.i("CmcSettingManager", "call inf : isDualSimSupportedOnPd success with global db : " + i);
                    if (i == CmcSettingManagerConstants.SUPPORTED) {
                        return true;
                    }
                    return false;
                }
            }
            try {
                Bundle call = this.mContext.getContentResolver().call(this.authorityUri, "v1/is_dual_sim_supported_on_pd", (String) null, (Bundle) null);
                if (call != null) {
                    if (call.getInt("result", -1) == 1) {
                        boolean z = call.getBoolean("dual_sim_supported_on_pd", false);
                        Log.d("CmcSettingManager", "call inf : isDualSimSupportedOnPd success : " + z);
                        return true;
                    }
                    Log.e("CmcSettingManager", "call inf : isDualSimSupportedOnPd fail : " + call.getString(ImIntent.Extras.ERROR_REASON));
                }
            } catch (Exception e) {
                Log.e("CmcSettingManager", "exception is occured : " + e.toString());
            }
            return false;
        }
    }

    public List<Integer> getSelectedSimSlotsOnPd() {
        int i;
        Log.d("CmcSettingManager", "getSelectedSimSlotsOnPd");
        if (this.mContext == null) {
            Log.e("CmcSettingManager", "context is null");
            return new ArrayList();
        } else if (!isApiSupportedWithDualSimSupported()) {
            return new ArrayList();
        } else {
            if (!IS_MORE_THAN_U_OS || (i = Settings.Global.getInt(this.mContext.getContentResolver(), CmcSettingManagerConstants.SETTINGS_KEY_CMC_SELECTED_SIMS_ON_PD, CmcSettingManagerConstants.KEY_NOT_EXIST)) == CmcSettingManagerConstants.KEY_NOT_EXIST) {
                try {
                    Bundle call = this.mContext.getContentResolver().call(this.authorityUri, "v1/get_selected_sim_slots_on_pd", (String) null, (Bundle) null);
                    if (call != null) {
                        if (call.getInt("result", -1) == 1) {
                            ArrayList<Integer> integerArrayList = call.getIntegerArrayList("selected_sim_slots_on_pd");
                            Log.d("CmcSettingManager", "call inf : getSelectedSimSlotsOnPd success");
                            if (integerArrayList != null) {
                                return integerArrayList;
                            }
                            Log.e("CmcSettingManager", "selectedSimSlotsList is null");
                            return new ArrayList();
                        }
                        Log.e("CmcSettingManager", "call inf : getSelectedSimSlotsOnPd fail : " + call.getString(ImIntent.Extras.ERROR_REASON));
                    }
                } catch (Exception e) {
                    Log.e("CmcSettingManager", "exception is occured : " + e.toString());
                }
                return new ArrayList();
            }
            Log.i("CmcSettingManager", "call inf : getSelectedSimSlotsOnPd success with global db : " + i);
            if (i == 0) {
                return new ArrayList(Arrays.asList(new Integer[]{0}));
            }
            if (i == 1) {
                return new ArrayList(Arrays.asList(new Integer[]{1}));
            }
            if (i != 2) {
                return new ArrayList();
            }
            return new ArrayList(Arrays.asList(new Integer[]{0, 1}));
        }
    }

    private boolean isApiSupportedWithDualSimSupported() {
        if (IS_DUAL_SIM_SUPPORTED) {
            return true;
        }
        Log.e("CmcSettingManager", "invalid oneui version");
        return false;
    }

    private void registerObserver(OBSERVER_TYPE observer_type) {
        if (Looper.myLooper() == null) {
            Log.d("CmcSettingManager", "looper is null create");
            Looper.prepare();
        }
        switch (AnonymousClass19.$SwitchMap$com$samsung$android$cmcsetting$CmcSettingManager$OBSERVER_TYPE[observer_type.ordinal()]) {
            case 1:
                registerCmcActivationObserver();
                return;
            case 2:
                registerCmcMessageActivationObserver();
                return;
            case 3:
                registerCmcCallActivationObserver();
                return;
            case 4:
                registerCmcWatchActivationObserver();
                return;
            case 5:
                registerCmcNetworkModeObserver();
                return;
            case 6:
                registerCmcLineInfoObserver();
                return;
            case 7:
                registerCmcDeviceInfoObserver();
                return;
            case 8:
                registerSamsungAccountInfoObserver();
                return;
            case 9:
                registerSameWifiNetworkStatusObserver();
                return;
            default:
                return;
        }
    }

    /* renamed from: com.samsung.android.cmcsetting.CmcSettingManager$19  reason: invalid class name */
    static /* synthetic */ class AnonymousClass19 {
        static final /* synthetic */ int[] $SwitchMap$com$samsung$android$cmcsetting$CmcSettingManager$OBSERVER_TYPE;

        /* JADX WARNING: Can't wrap try/catch for region: R(20:0|1|2|3|4|5|6|7|8|9|10|11|12|13|14|15|16|17|18|(3:19|20|22)) */
        /* JADX WARNING: Can't wrap try/catch for region: R(22:0|1|2|3|4|5|6|7|8|9|10|11|12|13|14|15|16|17|18|19|20|22) */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:11:0x003e */
        /* JADX WARNING: Missing exception handler attribute for start block: B:13:0x0049 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:15:0x0054 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:17:0x0060 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:19:0x006c */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0012 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x001d */
        /* JADX WARNING: Missing exception handler attribute for start block: B:7:0x0028 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:9:0x0033 */
        static {
            /*
                com.samsung.android.cmcsetting.CmcSettingManager$OBSERVER_TYPE[] r0 = com.samsung.android.cmcsetting.CmcSettingManager.OBSERVER_TYPE.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$samsung$android$cmcsetting$CmcSettingManager$OBSERVER_TYPE = r0
                com.samsung.android.cmcsetting.CmcSettingManager$OBSERVER_TYPE r1 = com.samsung.android.cmcsetting.CmcSettingManager.OBSERVER_TYPE.mainActivation     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = $SwitchMap$com$samsung$android$cmcsetting$CmcSettingManager$OBSERVER_TYPE     // Catch:{ NoSuchFieldError -> 0x001d }
                com.samsung.android.cmcsetting.CmcSettingManager$OBSERVER_TYPE r1 = com.samsung.android.cmcsetting.CmcSettingManager.OBSERVER_TYPE.messageActivation     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r0 = $SwitchMap$com$samsung$android$cmcsetting$CmcSettingManager$OBSERVER_TYPE     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.samsung.android.cmcsetting.CmcSettingManager$OBSERVER_TYPE r1 = com.samsung.android.cmcsetting.CmcSettingManager.OBSERVER_TYPE.callActivation     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                int[] r0 = $SwitchMap$com$samsung$android$cmcsetting$CmcSettingManager$OBSERVER_TYPE     // Catch:{ NoSuchFieldError -> 0x0033 }
                com.samsung.android.cmcsetting.CmcSettingManager$OBSERVER_TYPE r1 = com.samsung.android.cmcsetting.CmcSettingManager.OBSERVER_TYPE.watchActivation     // Catch:{ NoSuchFieldError -> 0x0033 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0033 }
                r2 = 4
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0033 }
            L_0x0033:
                int[] r0 = $SwitchMap$com$samsung$android$cmcsetting$CmcSettingManager$OBSERVER_TYPE     // Catch:{ NoSuchFieldError -> 0x003e }
                com.samsung.android.cmcsetting.CmcSettingManager$OBSERVER_TYPE r1 = com.samsung.android.cmcsetting.CmcSettingManager.OBSERVER_TYPE.networkMode     // Catch:{ NoSuchFieldError -> 0x003e }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x003e }
                r2 = 5
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x003e }
            L_0x003e:
                int[] r0 = $SwitchMap$com$samsung$android$cmcsetting$CmcSettingManager$OBSERVER_TYPE     // Catch:{ NoSuchFieldError -> 0x0049 }
                com.samsung.android.cmcsetting.CmcSettingManager$OBSERVER_TYPE r1 = com.samsung.android.cmcsetting.CmcSettingManager.OBSERVER_TYPE.lineInfo     // Catch:{ NoSuchFieldError -> 0x0049 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0049 }
                r2 = 6
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0049 }
            L_0x0049:
                int[] r0 = $SwitchMap$com$samsung$android$cmcsetting$CmcSettingManager$OBSERVER_TYPE     // Catch:{ NoSuchFieldError -> 0x0054 }
                com.samsung.android.cmcsetting.CmcSettingManager$OBSERVER_TYPE r1 = com.samsung.android.cmcsetting.CmcSettingManager.OBSERVER_TYPE.deviceInfo     // Catch:{ NoSuchFieldError -> 0x0054 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0054 }
                r2 = 7
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0054 }
            L_0x0054:
                int[] r0 = $SwitchMap$com$samsung$android$cmcsetting$CmcSettingManager$OBSERVER_TYPE     // Catch:{ NoSuchFieldError -> 0x0060 }
                com.samsung.android.cmcsetting.CmcSettingManager$OBSERVER_TYPE r1 = com.samsung.android.cmcsetting.CmcSettingManager.OBSERVER_TYPE.saInfo     // Catch:{ NoSuchFieldError -> 0x0060 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0060 }
                r2 = 8
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0060 }
            L_0x0060:
                int[] r0 = $SwitchMap$com$samsung$android$cmcsetting$CmcSettingManager$OBSERVER_TYPE     // Catch:{ NoSuchFieldError -> 0x006c }
                com.samsung.android.cmcsetting.CmcSettingManager$OBSERVER_TYPE r1 = com.samsung.android.cmcsetting.CmcSettingManager.OBSERVER_TYPE.sameWifiNetworkStatus     // Catch:{ NoSuchFieldError -> 0x006c }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x006c }
                r2 = 9
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x006c }
            L_0x006c:
                int[] r0 = $SwitchMap$com$samsung$android$cmcsetting$CmcSettingManager$OBSERVER_TYPE     // Catch:{ NoSuchFieldError -> 0x0078 }
                com.samsung.android.cmcsetting.CmcSettingManager$OBSERVER_TYPE r1 = com.samsung.android.cmcsetting.CmcSettingManager.OBSERVER_TYPE.all     // Catch:{ NoSuchFieldError -> 0x0078 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0078 }
                r2 = 10
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0078 }
            L_0x0078:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.samsung.android.cmcsetting.CmcSettingManager.AnonymousClass19.<clinit>():void");
        }
    }

    private void registerCmcActivationObserver() {
        if (this.mCmcActivationDbChangeObserver == null) {
            this.mCmcActivationDbChangeObserver = new ContentObserver(new Handler()) {
                public void onChange(boolean z) {
                    Log.d("CmcSettingManager", "mCmcActivationDbChangeObserver : selfChange = " + z);
                    CmcSettingManager.this.sendEventCmcActivation();
                }
            };
            this.mContext.getContentResolver().registerContentObserver(this.authorityUriForCmcActivation, true, this.mCmcActivationDbChangeObserver);
        }
    }

    private void registerCmcMessageActivationObserver() {
        if (this.mCmcMessageActivationDbChangeObserver == null) {
            this.mCmcMessageActivationDbChangeObserver = new ContentObserver(new Handler()) {
                public void onChange(boolean z) {
                    Log.d("CmcSettingManager", "mCmcMessageActivationDbChangeObserver : selfChange = " + z);
                    CmcSettingManager.this.sendEventCmcMessageActivation();
                }
            };
            this.mContext.getContentResolver().registerContentObserver(this.authorityUriForCmcMessageActivation, true, this.mCmcMessageActivationDbChangeObserver);
        }
    }

    private void registerCmcCallActivationObserver() {
        if (this.mCmcCallActivationDbChangeObserver == null) {
            this.mCmcCallActivationDbChangeObserver = new ContentObserver(new Handler()) {
                public void onChange(boolean z) {
                    Log.d("CmcSettingManager", "mCmcCallActivationDbChangeObserver : selfChange = " + z);
                    CmcSettingManager.this.sendEventCmcCallActivation();
                }
            };
            this.mContext.getContentResolver().registerContentObserver(this.authorityUriForCmcCallActivation, true, this.mCmcCallActivationDbChangeObserver);
        }
    }

    private void registerCmcWatchActivationObserver() {
        if (this.mWatchActivationDbChangeObserver == null) {
            this.mWatchActivationDbChangeObserver = new ContentObserver(new Handler()) {
                public void onChange(boolean z) {
                    Log.d("CmcSettingManager", "mWatchActivationDbChangeObserver : selfChange = " + z);
                    CmcSettingManager.this.sendEventCmcWatchActivation();
                }
            };
            this.mContext.getContentResolver().registerContentObserver(this.authorityUriForWatchActivation, true, this.mWatchActivationDbChangeObserver);
        }
    }

    private void registerCmcNetworkModeObserver() {
        if (this.mNetworkModeDbChangeObserver == null) {
            this.mNetworkModeDbChangeObserver = new ContentObserver(new Handler()) {
                public void onChange(boolean z) {
                    Log.d("CmcSettingManager", "mNetworkModeDbChangeObserver : selfChange = " + z);
                    CmcSettingManager.this.sendEventCmcNetworkMode();
                }
            };
            this.mContext.getContentResolver().registerContentObserver(this.authorityUriForNetworkMode, true, this.mNetworkModeDbChangeObserver);
        }
    }

    private void registerCmcLineInfoObserver() {
        if (this.mLinesDbChangeObserver == null) {
            this.mLinesDbChangeObserver = new ContentObserver(new Handler()) {
                public void onChange(boolean z) {
                    Log.d("CmcSettingManager", "mLinesDbChangeObserver : selfChange = " + z);
                    CmcSettingManager.this.sendEventCmcLines();
                }
            };
            this.mContext.getContentResolver().registerContentObserver(this.authorityUriForLines, true, this.mLinesDbChangeObserver);
        }
    }

    private void registerCmcDeviceInfoObserver() {
        if (this.mDevicesDbChangeObserver == null) {
            this.mDevicesDbChangeObserver = new ContentObserver(new Handler()) {
                public void onChange(boolean z) {
                    Log.d("CmcSettingManager", "mDevicesDbChangeObserver : selfChange = " + z);
                    CmcSettingManager.this.sendEventCmcDevices();
                }
            };
            this.mContext.getContentResolver().registerContentObserver(this.authorityUriForDevices, true, this.mDevicesDbChangeObserver);
        }
    }

    private void registerSamsungAccountInfoObserver() {
        if (this.mSaInfoDbChangeObserver == null) {
            this.mSaInfoDbChangeObserver = new ContentObserver(new Handler()) {
                public void onChange(boolean z) {
                    Log.d("CmcSettingManager", "mSaInfoDbChangeObserver : selfChange = " + z);
                    CmcSettingManager.this.sendEventCmcSaInfo();
                }
            };
            this.mContext.getContentResolver().registerContentObserver(this.authorityUriForSaInfo, true, this.mSaInfoDbChangeObserver);
        }
    }

    private void registerSameWifiNetworkStatusObserver() {
        if (this.mSameWifiNetworkStatusObserver == null) {
            this.mSameWifiNetworkStatusObserver = new ContentObserver(new Handler()) {
                public void onChange(boolean z) {
                    Log.d("CmcSettingManager", "mSameWifiNetworkStatusObserver : selfChange = " + z);
                    CmcSettingManager.this.sendEventSameWifiNetworkStatus();
                }
            };
            this.mContext.getContentResolver().registerContentObserver(this.authorityUriForSameWifiNetworkStatus, true, this.mSameWifiNetworkStatusObserver);
        }
    }

    private void unregisterObserver(OBSERVER_TYPE observer_type) {
        switch (AnonymousClass19.$SwitchMap$com$samsung$android$cmcsetting$CmcSettingManager$OBSERVER_TYPE[observer_type.ordinal()]) {
            case 1:
                unregisterCmcActivationObserver();
                return;
            case 2:
                unregisterCmcMessageActivationObserver();
                return;
            case 3:
                unregisterCmcCallActivationObserver();
                return;
            case 4:
                unregisterCmcWatchActivationObserver();
                return;
            case 5:
                unregisterCmcNetworkModeObserver();
                return;
            case 6:
                unregisterCmcLineInfoObserver();
                return;
            case 7:
                unregisterCmcDeviceInfoObserver();
                return;
            case 8:
                unregisterSamsungAccountInfoObserver();
                return;
            case 9:
                unregisterSameWifiNetworkStatusObserver();
                return;
            case 10:
                unregisterCmcActivationObserver();
                unregisterCmcMessageActivationObserver();
                unregisterCmcCallActivationObserver();
                unregisterCmcWatchActivationObserver();
                unregisterCmcNetworkModeObserver();
                unregisterCmcLineInfoObserver();
                unregisterCmcDeviceInfoObserver();
                unregisterSamsungAccountInfoObserver();
                unregisterSameWifiNetworkStatusObserver();
                return;
            default:
                return;
        }
    }

    private void unregisterCmcActivationObserver() {
        if (this.mCmcActivationDbChangeObserver != null) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mCmcActivationDbChangeObserver);
            this.mCmcActivationDbChangeObserver = null;
        }
    }

    private void unregisterCmcMessageActivationObserver() {
        if (this.mCmcMessageActivationDbChangeObserver != null) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mCmcMessageActivationDbChangeObserver);
            this.mCmcMessageActivationDbChangeObserver = null;
        }
    }

    private void unregisterCmcCallActivationObserver() {
        if (this.mCmcCallActivationDbChangeObserver != null) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mCmcCallActivationDbChangeObserver);
            this.mCmcCallActivationDbChangeObserver = null;
        }
    }

    private void unregisterCmcWatchActivationObserver() {
        if (this.mWatchActivationDbChangeObserver != null) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mWatchActivationDbChangeObserver);
            this.mWatchActivationDbChangeObserver = null;
        }
    }

    private void unregisterCmcNetworkModeObserver() {
        if (this.mNetworkModeDbChangeObserver != null) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mNetworkModeDbChangeObserver);
            this.mNetworkModeDbChangeObserver = null;
        }
    }

    private void unregisterCmcLineInfoObserver() {
        if (this.mLinesDbChangeObserver != null) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mLinesDbChangeObserver);
            this.mLinesDbChangeObserver = null;
        }
    }

    private void unregisterCmcDeviceInfoObserver() {
        if (this.mDevicesDbChangeObserver != null) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mDevicesDbChangeObserver);
            this.mDevicesDbChangeObserver = null;
        }
    }

    private void unregisterSamsungAccountInfoObserver() {
        if (this.mSaInfoDbChangeObserver != null) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mSaInfoDbChangeObserver);
            this.mSaInfoDbChangeObserver = null;
        }
    }

    private void unregisterSameWifiNetworkStatusObserver() {
        if (this.mSameWifiNetworkStatusObserver != null) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mSameWifiNetworkStatusObserver);
            this.mSameWifiNetworkStatusObserver = null;
        }
    }

    private CmcSettingManagerConstants.DeviceType getDeviceTypeInternal(String str) {
        if ("pd".equalsIgnoreCase(str)) {
            return CmcSettingManagerConstants.DeviceType.DEVICE_TYPE_PD;
        }
        if ("sd".equalsIgnoreCase(str)) {
            return CmcSettingManagerConstants.DeviceType.DEVICE_TYPE_SD;
        }
        return CmcSettingManagerConstants.DeviceType.DEVICE_TYPE_UNDEFINED;
    }

    private CmcSettingManagerConstants.DeviceCategory getDeviceCategoryInternal(String str) {
        if ("Phone".equalsIgnoreCase(str)) {
            return CmcSettingManagerConstants.DeviceCategory.DEVICE_CATEGORY_PHONE;
        }
        if ("Tablet".equalsIgnoreCase(str)) {
            return CmcSettingManagerConstants.DeviceCategory.DEVICE_CATEGORY_TABLET;
        }
        if ("BT-Watch".equalsIgnoreCase(str) || "Watch".equalsIgnoreCase(str)) {
            return CmcSettingManagerConstants.DeviceCategory.DEVICE_CATEGORY_BT_WATCH;
        }
        if ("Speaker".equalsIgnoreCase(str)) {
            return CmcSettingManagerConstants.DeviceCategory.DEVICE_CATEGORY_SPEAKER;
        }
        if ("PC".equalsIgnoreCase(str)) {
            return CmcSettingManagerConstants.DeviceCategory.DEVICE_CATEGORY_PC;
        }
        if ("TV".equalsIgnoreCase(str)) {
            return CmcSettingManagerConstants.DeviceCategory.DEVICE_CATEGORY_TV;
        }
        if ("Laptop".equalsIgnoreCase(str)) {
            return CmcSettingManagerConstants.DeviceCategory.DEVICE_CATEGORY_LAPTOP;
        }
        if ("VST".equalsIgnoreCase(str)) {
            return CmcSettingManagerConstants.DeviceCategory.DEVICE_CATEGORY_VST;
        }
        return CmcSettingManagerConstants.DeviceCategory.DEVICE_CATEGORY_UNDEFINED;
    }

    private boolean isCmcPackageInstalled(Context context) {
        try {
            context.getPackageManager().getPackageInfo(CmcConstants.SERVICE_PACKAGE_NAME, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("CmcSettingManager", "cmc package is not exist : " + e);
            return false;
        }
    }

    private String inspector(Object obj) {
        if (obj == null) {
            return null;
        }
        if (SHIP_BUILD) {
            return "xxxxx";
        }
        return "" + obj;
    }

    /* access modifiers changed from: private */
    public void sendEventCmcActivation() {
        new Thread(new Runnable() {
            public void run() {
                if (CmcSettingManager.this.mCmcActivationChangedListenerList != null) {
                    Iterator it = CmcSettingManager.this.mCmcActivationChangedListenerList.iterator();
                    while (it.hasNext()) {
                        CmcActivationInfoChangedListener cmcActivationInfoChangedListener = (CmcActivationInfoChangedListener) it.next();
                        if (cmcActivationInfoChangedListener != null) {
                            cmcActivationInfoChangedListener.onChangedCmcActivation();
                        }
                    }
                }
            }
        }).start();
    }

    /* access modifiers changed from: private */
    public void sendEventCmcMessageActivation() {
        new Thread(new Runnable() {
            public void run() {
                if (CmcSettingManager.this.mCmcMessageActivationChangedListenerList != null) {
                    Iterator it = CmcSettingManager.this.mCmcMessageActivationChangedListenerList.iterator();
                    while (it.hasNext()) {
                        CmcMessageActivationInfoChangedListener cmcMessageActivationInfoChangedListener = (CmcMessageActivationInfoChangedListener) it.next();
                        if (cmcMessageActivationInfoChangedListener != null) {
                            cmcMessageActivationInfoChangedListener.onChangedCmcMessageActivation();
                        }
                    }
                }
            }
        }).start();
    }

    /* access modifiers changed from: private */
    public void sendEventCmcCallActivation() {
        new Thread(new Runnable() {
            public void run() {
                if (CmcSettingManager.this.mCmcCallActivationChangedListenerList != null) {
                    Iterator it = CmcSettingManager.this.mCmcCallActivationChangedListenerList.iterator();
                    while (it.hasNext()) {
                        CmcCallActivationInfoChangedListener cmcCallActivationInfoChangedListener = (CmcCallActivationInfoChangedListener) it.next();
                        if (cmcCallActivationInfoChangedListener != null) {
                            cmcCallActivationInfoChangedListener.onChangedCmcCallActivation();
                        }
                    }
                }
            }
        }).start();
    }

    /* access modifiers changed from: private */
    public void sendEventCmcWatchActivation() {
        new Thread(new Runnable() {
            public void run() {
                if (CmcSettingManager.this.mCmcWatchActivationChangedListenerList != null) {
                    Iterator it = CmcSettingManager.this.mCmcWatchActivationChangedListenerList.iterator();
                    while (it.hasNext()) {
                        CmcWatchActivationInfoChangedListener cmcWatchActivationInfoChangedListener = (CmcWatchActivationInfoChangedListener) it.next();
                        if (cmcWatchActivationInfoChangedListener != null) {
                            cmcWatchActivationInfoChangedListener.onChangedWatchActivation();
                        }
                    }
                }
            }
        }).start();
    }

    /* access modifiers changed from: private */
    public void sendEventCmcNetworkMode() {
        new Thread(new Runnable() {
            public void run() {
                if (CmcSettingManager.this.mCmcNetworkModeChangedListenerList != null) {
                    Iterator it = CmcSettingManager.this.mCmcNetworkModeChangedListenerList.iterator();
                    while (it.hasNext()) {
                        CmcNetworkModeInfoChangedListener cmcNetworkModeInfoChangedListener = (CmcNetworkModeInfoChangedListener) it.next();
                        if (cmcNetworkModeInfoChangedListener != null) {
                            cmcNetworkModeInfoChangedListener.onChangedNetworkMode();
                        }
                    }
                }
            }
        }).start();
    }

    /* access modifiers changed from: private */
    public void sendEventCmcLines() {
        new Thread(new Runnable() {
            public void run() {
                if (CmcSettingManager.this.mCmcLineInfoChangedListenerList != null) {
                    Iterator it = CmcSettingManager.this.mCmcLineInfoChangedListenerList.iterator();
                    while (it.hasNext()) {
                        CmcLineInfoChangedListener cmcLineInfoChangedListener = (CmcLineInfoChangedListener) it.next();
                        if (cmcLineInfoChangedListener != null) {
                            cmcLineInfoChangedListener.onChangedLineInfo();
                        }
                    }
                }
            }
        }).start();
    }

    /* access modifiers changed from: private */
    public void sendEventCmcDevices() {
        new Thread(new Runnable() {
            public void run() {
                if (CmcSettingManager.this.mCmcDeviceInfoChangedListenerList != null) {
                    Iterator it = CmcSettingManager.this.mCmcDeviceInfoChangedListenerList.iterator();
                    while (it.hasNext()) {
                        CmcDeviceInfoChangedListener cmcDeviceInfoChangedListener = (CmcDeviceInfoChangedListener) it.next();
                        if (cmcDeviceInfoChangedListener != null) {
                            cmcDeviceInfoChangedListener.onChangedDeviceInfo();
                        }
                    }
                }
            }
        }).start();
    }

    /* access modifiers changed from: private */
    public void sendEventCmcSaInfo() {
        new Thread(new Runnable() {
            public void run() {
                if (CmcSettingManager.this.mCmcSamsungAccountInfoChangedListenerList != null) {
                    Iterator it = CmcSettingManager.this.mCmcSamsungAccountInfoChangedListenerList.iterator();
                    while (it.hasNext()) {
                        CmcSamsungAccountInfoChangedListener cmcSamsungAccountInfoChangedListener = (CmcSamsungAccountInfoChangedListener) it.next();
                        if (cmcSamsungAccountInfoChangedListener != null) {
                            cmcSamsungAccountInfoChangedListener.onChangedSamsungAccountInfo();
                        }
                    }
                }
            }
        }).start();
    }

    /* access modifiers changed from: private */
    public void sendEventSameWifiNetworkStatus() {
        new Thread(new Runnable() {
            public void run() {
                if (CmcSettingManager.this.mSameWifiNetworkStatusListenerList != null) {
                    Iterator it = CmcSettingManager.this.mSameWifiNetworkStatusListenerList.iterator();
                    while (it.hasNext()) {
                        CmcSameWifiNetworkStatusListener cmcSameWifiNetworkStatusListener = (CmcSameWifiNetworkStatusListener) it.next();
                        if (cmcSameWifiNetworkStatusListener != null) {
                            cmcSameWifiNetworkStatusListener.onChangedSameWifiNetworkStatus();
                        }
                    }
                }
            }
        }).start();
    }
}
