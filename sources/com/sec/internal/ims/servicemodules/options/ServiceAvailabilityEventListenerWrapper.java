package com.sec.internal.ims.servicemodules.options;

import android.util.Log;
import android.util.SparseArray;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.ImsUri;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.interfaces.ims.servicemodules.im.IImModule;
import com.sec.internal.interfaces.ims.servicemodules.options.IServiceAvailabilityEventListener;
import com.sec.internal.log.IMSLog;
import java.util.Date;

public class ServiceAvailabilityEventListenerWrapper implements IServiceAvailabilityEventListener {
    private static final int EXPECTED_NUMBER_OF_SIM_SLOTS = 2;
    private static final String LOG_TAG = "ServiceAvailabilityEventListenerWrapper";
    CapabilityDiscoveryModule mCapabilityDiscovery;
    IImModule mImModule;
    private SparseArray<String> mProfileList;
    private SparseArray<IServiceAvailabilityEventListener> mServiceAvailabilityEventListenerList;

    public ServiceAvailabilityEventListenerWrapper(CapabilityDiscoveryModule capabilityDiscoveryModule) {
        this.mImModule = null;
        this.mServiceAvailabilityEventListenerList = new SparseArray<>(2);
        this.mProfileList = new SparseArray<>(2);
        this.mImModule = ImsRegistry.getServiceModuleManager().getImModule();
        this.mCapabilityDiscovery = capabilityDiscoveryModule;
    }

    public void attachServiceAvailabilityEventListener(int i, String str) {
        if (this.mImModule != null) {
            if (this.mServiceAvailabilityEventListenerList.size() == 0) {
                this.mImModule.registerServiceAvailabilityEventListener(this);
            }
            attach(i, str);
            this.mProfileList.put(i, str);
        }
    }

    private void attach(int i, String str) {
        Object obj;
        if (!ImsProfile.isRcsUpProfile(str) || this.mCapabilityDiscovery.getCapabilityConfig(i) == null || this.mCapabilityDiscovery.getCapabilityConfig(i).getDefaultDisc(i) == 2) {
            Log.i(LOG_TAG, "attaching ServiceAvailabilityEventListenerBasic phoneId: " + i);
            obj = new ServiceAvailabilityEventListenerBasic();
        } else {
            Log.i(LOG_TAG, "attaching ServiceAvailabilityEventListenerUp phoneId: " + i);
            obj = new ServiceAvailabilityEventListenerUp(this.mCapabilityDiscovery.getLooper(), this.mCapabilityDiscovery.getCapabilitiesCache(i), this.mCapabilityDiscovery.getUriGenerator());
        }
        this.mServiceAvailabilityEventListenerList.put(i, obj);
    }

    public void detachServiceAvailabilityEventListener(int i) {
        if (this.mImModule != null && this.mServiceAvailabilityEventListenerList.size() > 0) {
            this.mServiceAvailabilityEventListenerList.remove(i);
            this.mProfileList.remove(i);
            if (this.mServiceAvailabilityEventListenerList.size() == 0) {
                this.mImModule.unregisterServiceAvailabilityEventListener(this);
            }
        }
    }

    public void updateServiceAvailabilityEventListener(int i) {
        if (this.mImModule != null && this.mServiceAvailabilityEventListenerList.get(i) != null && this.mProfileList.get(i) != null) {
            attach(i, this.mProfileList.get(i));
        }
    }

    public void onServiceAvailabilityUpdate(String str, ImsUri imsUri, Date date) {
        int phoneId = SimManagerFactory.getPhoneId(str);
        if (phoneId == -1) {
            Log.e(LOG_TAG, "onServiceAvailabilityUpdate: failed to find phoneId for ownIdentity: " + IMSLog.checker(str) + "!");
        } else if (this.mServiceAvailabilityEventListenerList.get(phoneId) != null) {
            this.mServiceAvailabilityEventListenerList.get(phoneId).onServiceAvailabilityUpdate(str, imsUri, date);
        } else {
            Log.e(LOG_TAG, "onServiceAvailabilityUpdate: ServiceAvailability listener is not attached for ownIdentity: " + IMSLog.checker(str) + "!");
        }
    }
}
