package com.sec.internal.ims.cmstore.ambs;

import android.os.Handler;
import android.os.Looper;
import com.sec.ims.ImsRegistration;
import com.sec.ims.settings.ImsProfile;
import com.sec.internal.constants.ims.os.NetworkEvent;
import com.sec.internal.ims.gba.GbaServiceModule;
import com.sec.internal.ims.servicemodules.tapi.service.api.interfaces.ITapiServiceManager;
import com.sec.internal.interfaces.ims.IImsFramework;
import com.sec.internal.interfaces.ims.cmstore.ICmsModule;
import com.sec.internal.interfaces.ims.core.IRegisterTask;
import com.sec.internal.interfaces.ims.gba.IGbaServiceModule;
import com.sec.internal.interfaces.ims.servicemodules.IServiceModuleManager;
import com.sec.internal.interfaces.ims.servicemodules.csh.IImageShareModule;
import com.sec.internal.interfaces.ims.servicemodules.csh.IVideoShareModule;
import com.sec.internal.interfaces.ims.servicemodules.euc.IEucModule;
import com.sec.internal.interfaces.ims.servicemodules.gls.IGlsModule;
import com.sec.internal.interfaces.ims.servicemodules.im.IImModule;
import com.sec.internal.interfaces.ims.servicemodules.openapi.IImsStatusServiceModule;
import com.sec.internal.interfaces.ims.servicemodules.openapi.IOpenApiServiceModule;
import com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityDiscoveryModule;
import com.sec.internal.interfaces.ims.servicemodules.options.IOptionsModule;
import com.sec.internal.interfaces.ims.servicemodules.presence.IPresenceModule;
import com.sec.internal.interfaces.ims.servicemodules.quantumencryption.IQuantumEncryptionServiceModule;
import com.sec.internal.interfaces.ims.servicemodules.session.ISessionModule;
import com.sec.internal.interfaces.ims.servicemodules.sms.ISmsServiceModule;
import com.sec.internal.interfaces.ims.servicemodules.ss.IUtServiceModule;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule;
import java.util.List;
import java.util.Set;

public class CmsServiceModuleManager extends Handler implements IServiceModuleManager {
    private static final String TAG = CmsServiceModuleManager.class.getSimpleName();
    private static IServiceModuleManager mCmsModuleManager = null;
    GbaServiceModule mGbaServiceModule;
    IImsFramework mImsFramework;

    public void checkRcsServiceModules(List<IRegisterTask> list, int i) {
    }

    public void cleanUpModules() {
    }

    public void forceCallOnServiceSwitched(int i) {
    }

    public ICapabilityDiscoveryModule getCapabilityDiscoveryModule() {
        return null;
    }

    public ICmsModule getCmsModule() {
        return null;
    }

    public IEucModule getEucModule() {
        return null;
    }

    public IGlsModule getGlsModule() {
        return null;
    }

    public IImModule getImModule() {
        return null;
    }

    public IImageShareModule getImageShareModule() {
        return null;
    }

    public IImsStatusServiceModule getImsStatusServiceModule() {
        return null;
    }

    public IOpenApiServiceModule getOpenApiServiceModule() {
        return null;
    }

    public IOptionsModule getOptionsModule() {
        return null;
    }

    public IPresenceModule getPresenceModule() {
        return null;
    }

    public IQuantumEncryptionServiceModule getQuantumEncryptionServiceModule() {
        return null;
    }

    public Handler getServiceModuleHandler(String str) {
        return null;
    }

    public ISessionModule getSessionModule() {
        return null;
    }

    public ISmsServiceModule getSmsServiceModule() {
        return null;
    }

    public ITapiServiceManager getTapiServiceManager() {
        return null;
    }

    public IUtServiceModule getUtServiceModule() {
        return null;
    }

    public IVideoShareModule getVideoShareModule() {
        return null;
    }

    public IVolteServiceModule getVolteServiceModule() {
        return null;
    }

    public void initSequentially() {
    }

    public boolean isLooperExist() {
        return false;
    }

    public void notifyAutoConfigDone(int i) {
    }

    public void notifyConfigured(boolean z, int i) {
    }

    public void notifyDeregistering(ImsRegistration imsRegistration) {
    }

    public void notifyImsRegistration(ImsRegistration imsRegistration, boolean z, int i) {
    }

    public void notifyImsSwitchUpdateToApp() {
    }

    public void notifyNetworkChanged(NetworkEvent networkEvent, int i) {
    }

    public void notifyOmadmVolteConfigDone(int i) {
    }

    public void notifyRcsDeregistering(Set<String> set, ImsRegistration imsRegistration) {
    }

    public void notifyReRegistering(int i, Set<String> set) {
    }

    public void notifySimChange(int i) {
    }

    public void onImsSwitchUpdated(int i) {
    }

    public void serviceStartDeterminer(List<ImsProfile> list, int i) {
    }

    public void updateCapabilities(int i) {
    }

    public static IServiceModuleManager getInstance(IImsFramework iImsFramework, GbaServiceModule gbaServiceModule) {
        if (mCmsModuleManager == null) {
            mCmsModuleManager = new CmsServiceModuleManager(iImsFramework, gbaServiceModule);
        }
        return mCmsModuleManager;
    }

    CmsServiceModuleManager(IImsFramework iImsFramework, GbaServiceModule gbaServiceModule) {
        super(Looper.myLooper());
        this.mImsFramework = iImsFramework;
        this.mGbaServiceModule = gbaServiceModule;
    }

    public IGbaServiceModule getGbaServiceModule() {
        return this.mGbaServiceModule;
    }
}
