package com.sec.internal.ims;

import android.content.ContentValues;
import android.content.Context;
import android.os.Binder;
import android.os.Message;
import android.os.RemoteException;
import com.sec.ims.IImsRegistrationListener;
import com.sec.ims.ImsRegistration;
import com.sec.internal.ims.fcm.interfaces.IFcmHandler;
import com.sec.internal.ims.registry.ImsRegistry;
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
import com.sec.internal.interfaces.ims.core.iil.IIilManager;
import com.sec.internal.interfaces.ims.core.imslogger.IImsDiagMonitor;
import com.sec.internal.interfaces.ims.rcs.IRcsPolicyManager;
import com.sec.internal.interfaces.ims.servicemodules.IServiceModuleManager;
import java.util.List;

public class ImsFramework implements IImsFramework {
    private IImsFramework mImsFramework;

    public ImsFramework(IImsFramework iImsFramework) {
        this.mImsFramework = iImsFramework;
        ImsRegistry.init(this);
    }

    public IPdnController getPdnController() {
        return this.mImsFramework.getPdnController();
    }

    public ICmcAccountManager getCmcAccountManager() {
        return this.mImsFramework.getCmcAccountManager();
    }

    public IRcsPolicyManager getRcsPolicyManager() {
        return this.mImsFramework.getRcsPolicyManager();
    }

    public IRegistrationManager getRegistrationManager() {
        return this.mImsFramework.getRegistrationManager();
    }

    public IConfigModule getConfigModule() {
        return this.mImsFramework.getConfigModule();
    }

    public IHandlerFactory getHandlerFactory() {
        return this.mImsFramework.getHandlerFactory();
    }

    public IAECModule getAECModule() {
        return this.mImsFramework.getAECModule();
    }

    public ICmcConnectivityController getCmcConnectivityController() {
        return this.mImsFramework.getCmcConnectivityController();
    }

    public IGeolocationController getGeolocationController() {
        return this.mImsFramework.getGeolocationController();
    }

    public INtpTimeController getNtpTimeController() {
        return this.mImsFramework.getNtpTimeController();
    }

    public IImsDiagMonitor getImsDiagMonitor() {
        return this.mImsFramework.getImsDiagMonitor();
    }

    public IFcmHandler getFcmHandler() {
        return this.mImsFramework.getFcmHandler();
    }

    public IIilManager getIilManager(int i) {
        return this.mImsFramework.getIilManager(i);
    }

    public IWfcEpdgManager getWfcEpdgManager() {
        return this.mImsFramework.getWfcEpdgManager();
    }

    public List<ServiceModuleBase> getAllServiceModules() {
        return this.mImsFramework.getAllServiceModules();
    }

    public IServiceModuleManager getServiceModuleManager() {
        return this.mImsFramework.getServiceModuleManager();
    }

    public IRawSipSender getRawSipSender() {
        return this.mImsFramework.getRawSipSender();
    }

    public Context getContext() {
        return this.mImsFramework.getContext();
    }

    public String registerImsRegistrationListener(IImsRegistrationListener iImsRegistrationListener, boolean z, int i) {
        return this.mImsFramework.registerImsRegistrationListener(iImsRegistrationListener, z, i);
    }

    public String getString(int i, String str, String str2) {
        return this.mImsFramework.getString(i, str, str2);
    }

    public String[] getStringArray(int i, String str, String[] strArr) {
        return this.mImsFramework.getStringArray(i, str, (String[]) null);
    }

    public int getInt(int i, String str, int i2) {
        return this.mImsFramework.getInt(i, str, i2);
    }

    public boolean getBoolean(int i, String str, boolean z) {
        return this.mImsFramework.getBoolean(i, str, z);
    }

    public ContentValues getConfigValues(String[] strArr, int i) {
        return this.mImsFramework.getConfigValues(strArr, i);
    }

    public boolean isServiceAvailable(String str, int i, int i2) throws RemoteException {
        return this.mImsFramework.isServiceAvailable(str, i, i2);
    }

    public void setRttMode(int i, int i2) {
        this.mImsFramework.setRttMode(i, i2);
    }

    public void registerImsRegistrationListener(IImsRegistrationListener iImsRegistrationListener) throws RemoteException {
        this.mImsFramework.registerImsRegistrationListener(iImsRegistrationListener);
    }

    public void unregisterImsRegistrationListener(IImsRegistrationListener iImsRegistrationListener) throws RemoteException {
        this.mImsFramework.unregisterImsRegistrationListener(iImsRegistrationListener);
    }

    public ImsRegistration[] getRegistrationInfoByPhoneId(int i) throws RemoteException {
        return this.mImsFramework.getRegistrationInfoByPhoneId(i);
    }

    public int getNetworkType(int i) {
        return this.mImsFramework.getNetworkType(i);
    }

    public boolean isRcsEnabledByPhoneId(int i) {
        return this.mImsFramework.isRcsEnabledByPhoneId(i);
    }

    public void startAutoConfig(boolean z, Message message) {
        this.mImsFramework.startAutoConfig(z, message);
    }

    public Binder getBinder(String str) {
        return this.mImsFramework.getBinder(str);
    }

    public Binder getBinder(String str, String str2) {
        return this.mImsFramework.getBinder(str, str2);
    }

    public String getRcsProfileType(int i) throws RemoteException {
        return this.mImsFramework.getRcsProfileType(i);
    }

    public void enableRcsByPhoneId(boolean z, int i) throws RemoteException {
        this.mImsFramework.enableRcsByPhoneId(z, i);
    }

    public boolean isServiceEnabledByPhoneId(String str, int i) throws RemoteException {
        return this.mImsFramework.isServiceEnabledByPhoneId(str, i);
    }

    public void triggerAutoConfigurationForApp(int i) throws RemoteException {
        this.mImsFramework.triggerAutoConfigurationForApp(i);
    }

    public boolean isDefaultDmValue(String str, int i) {
        return this.mImsFramework.isDefaultDmValue(str, i);
    }

    public boolean setDefaultDmValue(String str, int i) {
        return this.mImsFramework.setDefaultDmValue(str, i);
    }

    public int[] getCallCount(int i) throws RemoteException {
        return this.mImsFramework.getCallCount(i);
    }

    public void notifyImsReady(boolean z, int i) {
        this.mImsFramework.notifyImsReady(z, i);
    }

    public void sendDeregister(int i, int i2) {
        this.mImsFramework.sendDeregister(i, i2);
    }

    public void suspendRegister(boolean z, int i) {
        this.mImsFramework.suspendRegister(z, i);
    }

    public void setIsimLoaded() {
        this.mImsFramework.setIsimLoaded();
    }

    public boolean isCrossSimCallingSupportedByPhoneId(int i) {
        return this.mImsFramework.isCrossSimCallingSupportedByPhoneId(i);
    }
}
