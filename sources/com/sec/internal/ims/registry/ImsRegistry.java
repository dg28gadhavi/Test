package com.sec.internal.ims.registry;

import android.content.ContentValues;
import android.content.Context;
import android.os.Binder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import com.sec.ims.IImsRegistrationListener;
import com.sec.ims.ImsRegistration;
import com.sec.internal.ims.fcm.interfaces.IFcmHandler;
import com.sec.internal.ims.servicemodules.base.ServiceModuleBase;
import com.sec.internal.imsphone.cmc.ICmcConnectivityController;
import com.sec.internal.interfaces.ims.IImsFramework;
import com.sec.internal.interfaces.ims.aec.IAECModule;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.core.ICmcAccountManager;
import com.sec.internal.interfaces.ims.core.IGeolocationController;
import com.sec.internal.interfaces.ims.core.INtpTimeController;
import com.sec.internal.interfaces.ims.core.IPdnController;
import com.sec.internal.interfaces.ims.core.IRawSipSender;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.core.IWfcEpdgManager;
import com.sec.internal.interfaces.ims.core.handler.IHandlerFactory;
import com.sec.internal.interfaces.ims.core.imslogger.IImsDiagMonitor;
import com.sec.internal.interfaces.ims.rcs.IRcsPolicyManager;
import com.sec.internal.interfaces.ims.servicemodules.IServiceModuleManager;
import java.util.List;

public class ImsRegistry {
    private static final String LOG_TAG = "ImsRegistry";
    private static boolean mIsReady = false;
    private static IImsFramework sImsFrameworkInstance;

    private ImsRegistry() {
    }

    public static void init(IImsFramework iImsFramework) {
        sImsFrameworkInstance = iImsFramework;
        mIsReady = true;
    }

    private static IImsFramework getImsFramework() {
        return sImsFrameworkInstance;
    }

    public static boolean isReady() {
        return mIsReady;
    }

    public static IPdnController getPdnController() {
        return getImsFramework().getPdnController();
    }

    public static ICmcAccountManager getCmcAccountManager() {
        return getImsFramework().getCmcAccountManager();
    }

    public static IRcsPolicyManager getRcsPolicyManager() {
        return getImsFramework().getRcsPolicyManager();
    }

    public static IRegistrationManager getRegistrationManager() {
        return getImsFramework().getRegistrationManager();
    }

    public static IConfigModule getConfigModule() {
        return getImsFramework().getConfigModule();
    }

    public static IHandlerFactory getHandlerFactory() {
        return getImsFramework().getHandlerFactory();
    }

    public static IAECModule getAECModule() {
        return getImsFramework().getAECModule();
    }

    public static IRawSipSender getRawSipSender() {
        return getImsFramework().getRawSipSender();
    }

    public static ICmcConnectivityController getICmcConnectivityController() {
        return getImsFramework().getCmcConnectivityController();
    }

    public static IGeolocationController getGeolocationController() {
        return getImsFramework().getGeolocationController();
    }

    public static INtpTimeController getNtpTimeController() {
        return getImsFramework().getNtpTimeController();
    }

    public static IImsDiagMonitor getImsDiagMonitor() {
        return getImsFramework().getImsDiagMonitor();
    }

    public static IFcmHandler getFcmHandler() {
        return getImsFramework().getFcmHandler();
    }

    public static IWfcEpdgManager getWfcEpdgManager() {
        return getImsFramework().getWfcEpdgManager();
    }

    public static List<ServiceModuleBase> getAllServiceModules() {
        return getImsFramework().getAllServiceModules();
    }

    public static IServiceModuleManager getServiceModuleManager() {
        return getImsFramework().getServiceModuleManager();
    }

    public static Context getContext() {
        return getImsFramework().getContext();
    }

    public static void registerImsRegistrationListener(IImsRegistrationListener iImsRegistrationListener, boolean z, int i) throws RemoteException {
        getImsFramework().registerImsRegistrationListener(iImsRegistrationListener, z, i);
    }

    public static int getInt(int i, String str, int i2) {
        return getImsFramework().getInt(i, str, i2);
    }

    public static boolean getBoolean(int i, String str, boolean z) {
        return getImsFramework().getBoolean(i, str, z);
    }

    public static String getString(int i, String str, String str2) {
        return getImsFramework().getString(i, str, str2);
    }

    public static String[] getStringArray(int i, String str, String[] strArr) {
        return getImsFramework().getStringArray(i, str, strArr);
    }

    public static ContentValues getConfigValues(String[] strArr, int i) {
        return getImsFramework().getConfigValues(strArr, i);
    }

    public static boolean isServiceAvailable(String str, int i, int i2) throws RemoteException {
        return getImsFramework().isServiceAvailable(str, i, i2);
    }

    public static void setRttMode(int i, int i2) {
        getImsFramework().setRttMode(i, i2);
    }

    public static void registerImsRegistrationListener(IImsRegistrationListener iImsRegistrationListener) {
        try {
            getImsFramework().registerImsRegistrationListener(iImsRegistrationListener);
        } catch (RemoteException e) {
            String str = LOG_TAG;
            Log.e(str, "registerImsRegistrationListener RemoteException do nothing : " + e.getMessage());
        }
    }

    public static void unregisterImsRegistrationListener(IImsRegistrationListener iImsRegistrationListener) throws RemoteException {
        getImsFramework().unregisterImsRegistrationListener(iImsRegistrationListener);
    }

    public static ImsRegistration[] getRegistrationInfoByPhoneId(int i) throws RemoteException {
        return getImsFramework().getRegistrationInfoByPhoneId(i);
    }

    public static int getNetworkType(int i) throws RemoteException {
        return getImsFramework().getNetworkType(i);
    }

    public static boolean isRcsEnabledByPhoneId(int i) {
        return getImsFramework().isRcsEnabledByPhoneId(i);
    }

    public static void startAutoConfig(boolean z, Message message) {
        getImsFramework().startAutoConfig(z, message);
    }

    public static Binder getBinder(String str) {
        return getImsFramework().getBinder(str);
    }

    public static Binder getBinder(String str, String str2) {
        return getImsFramework().getBinder(str, str2);
    }

    public static String getRcsProfileType(int i) {
        try {
            return getImsFramework().getRcsProfileType(i);
        } catch (RemoteException e) {
            String str = LOG_TAG;
            Log.e(str, "getRcsProfileType RemoteException return empty String : " + e.getMessage());
            return "";
        }
    }

    public static void enableRcsByPhoneId(boolean z, int i) {
        try {
            getImsFramework().enableRcsByPhoneId(z, i);
        } catch (RemoteException e) {
            String str = LOG_TAG;
            Log.e(str, "enableRcsByPhoneId RemoteException: " + e.getMessage());
        }
    }

    public static boolean isServiceEnabledByPhoneId(String str, int i) {
        try {
            return getImsFramework().isServiceEnabledByPhoneId(str, i);
        } catch (RemoteException e) {
            String str2 = LOG_TAG;
            Log.e(str2, "isServiceEnabledByPhoneId RemoteException: " + e.getMessage());
            return false;
        }
    }

    public static void triggerAutoConfigurationForApp(int i) {
        try {
            getImsFramework().triggerAutoConfigurationForApp(i);
        } catch (RemoteException e) {
            String str = LOG_TAG;
            Log.e(str, "triggerAutoConfigurationForApp RemoteException: " + e.getMessage());
        }
    }

    public static int[] getCallCount(int i) {
        try {
            return getImsFramework().getCallCount(i);
        } catch (RemoteException e) {
            String str = LOG_TAG;
            Log.e(str, "getCallCount RemoteException: " + e.getMessage());
            return new int[0];
        }
    }
}
