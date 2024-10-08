package com.sec.internal.ims.servicemodules.options;

import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.text.TextUtils;
import com.sec.ims.options.Capabilities;
import com.sec.ims.options.ICapabilityServiceEventListener;
import com.sec.ims.util.ImsUri;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.base.ServiceModuleBase;
import com.sec.internal.ims.servicemodules.csh.CshModuleBase;
import com.sec.internal.log.IMSLog;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CapabilityServiceEventListener {
    private static final String LOG_TAG = "CapabilityServiceEventListener";
    private Map<Integer, RemoteCallbackList<ICapabilityServiceEventListener>> mListenersList = new HashMap();

    public CapabilityServiceEventListener() {
        int phoneCount = SimUtil.getPhoneCount();
        for (int i = 0; i < phoneCount; i++) {
            this.mListenersList.put(Integer.valueOf(i), new RemoteCallbackList());
        }
    }

    public void registerListener(ICapabilityServiceEventListener iCapabilityServiceEventListener, int i) {
        IMSLog.i(LOG_TAG, i, "registerListener: " + iCapabilityServiceEventListener);
        RemoteCallbackList remoteCallbackList = this.mListenersList.get(Integer.valueOf(i));
        if (remoteCallbackList != null) {
            synchronized (remoteCallbackList) {
                if (iCapabilityServiceEventListener != null) {
                    remoteCallbackList.register(iCapabilityServiceEventListener);
                    try {
                        iCapabilityServiceEventListener.onOwnCapabilitiesChanged();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    this.mListenersList.put(Integer.valueOf(i), remoteCallbackList);
                }
            }
        }
    }

    public void unregisterListener(ICapabilityServiceEventListener iCapabilityServiceEventListener, int i) {
        IMSLog.i(LOG_TAG, i, "unregisterListener: " + iCapabilityServiceEventListener);
        RemoteCallbackList remoteCallbackList = this.mListenersList.get(Integer.valueOf(i));
        if (remoteCallbackList != null) {
            synchronized (remoteCallbackList) {
                if (iCapabilityServiceEventListener != null) {
                    remoteCallbackList.unregister(iCapabilityServiceEventListener);
                    this.mListenersList.put(Integer.valueOf(i), remoteCallbackList);
                }
            }
        }
    }

    public void notifyOwnCapabilitiesChanged(int i) {
        RemoteCallbackList remoteCallbackList = this.mListenersList.get(Integer.valueOf(i));
        try {
            int beginBroadcast = remoteCallbackList.beginBroadcast();
            IMSLog.i(LOG_TAG, i, "notifyOwnCapabilitiesChanged: eventListener length: " + beginBroadcast);
            for (int i2 = 0; i2 < beginBroadcast; i2++) {
                ICapabilityServiceEventListener broadcastItem = remoteCallbackList.getBroadcastItem(i2);
                IMSLog.s(LOG_TAG, i, "No. " + i2 + " notifyOwnCapabilitiesChanged: listener: " + broadcastItem);
                broadcastItem.onOwnCapabilitiesChanged();
            }
        } catch (RemoteException | IllegalStateException | NullPointerException e) {
            e.printStackTrace();
        }
        try {
            IMSLog.i(LOG_TAG, i, "notifyOwnCapabilitiesChanged: finishBroadcast()");
            remoteCallbackList.finishBroadcast();
        } catch (IllegalStateException | NullPointerException e2) {
            e2.printStackTrace();
        }
    }

    public void notifyCapabilitiesChanged(List<ImsUri> list, Capabilities capabilities, ImsUri imsUri, int i) {
        RemoteCallbackList remoteCallbackList = this.mListenersList.get(Integer.valueOf(i));
        try {
            int beginBroadcast = remoteCallbackList.beginBroadcast();
            IMSLog.i(LOG_TAG, i, "notifyCapabilitiesChanged: eventListener length: " + beginBroadcast);
            for (int i2 = 0; i2 < beginBroadcast; i2++) {
                remoteCallbackList.getBroadcastItem(i2).onCapabilitiesChanged(list, capabilities);
            }
        } catch (RemoteException | IllegalStateException | NullPointerException e) {
            e.printStackTrace();
        }
        try {
            remoteCallbackList.finishBroadcast();
        } catch (IllegalStateException | NullPointerException e2) {
            e2.printStackTrace();
        }
        if (imsUri != null) {
            for (ImsUri msisdn : list) {
                if (TextUtils.equals(msisdn.getMsisdn(), imsUri.getMsisdn())) {
                    for (ServiceModuleBase next : ImsRegistry.getAllServiceModules()) {
                        if (next instanceof CshModuleBase) {
                            ((CshModuleBase) next).onRemoteCapabilitiesChanged(capabilities);
                        }
                    }
                }
            }
        }
    }

    public void notifyEABServiceAdvertiseResult(int i, int i2) {
        RemoteCallbackList remoteCallbackList = this.mListenersList.get(Integer.valueOf(i2));
        try {
            int beginBroadcast = remoteCallbackList.beginBroadcast();
            IMSLog.i(LOG_TAG, i2, "notifyEABServiceAdvertiseResult: eventListener length: " + beginBroadcast);
            for (int i3 = 0; i3 < beginBroadcast; i3++) {
                remoteCallbackList.getBroadcastItem(i3).onCapabilityAndAvailabilityPublished(i);
            }
            remoteCallbackList.finishBroadcast();
        } catch (RemoteException | IllegalStateException | NullPointerException e) {
            e.printStackTrace();
        }
    }
}
