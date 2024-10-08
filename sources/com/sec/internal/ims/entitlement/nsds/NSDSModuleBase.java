package com.sec.internal.ims.entitlement.nsds;

import android.os.Handler;
import android.os.Looper;
import com.sec.internal.helper.State;

public abstract class NSDSModuleBase extends Handler {
    public void activateSimDevice(int i, int i2) {
    }

    public void deactivateSimDevice(int i) {
    }

    public void dump() {
    }

    public void handleVoWifToggleOffEvent() {
    }

    public void handleVoWifToggleOnEvent() {
    }

    public void initForDeviceReady() {
    }

    public void onDeviceReady() {
    }

    public void onSimNotAvailable() {
    }

    public void onSimReady(boolean z) {
    }

    public void queueGcmTokenRetrieval() {
    }

    public void queuePushTokenUpdateInEntitlementServer() {
    }

    public void queueRefreshDeviceAndServiceInfo(int i, int i2) {
    }

    public void queueRefreshDeviceConfig(int i) {
    }

    public void retrieveAkaToken(int i, int i2) {
    }

    public void updateE911Address() {
    }

    public void updateEntitlementUrl(String str) {
    }

    protected NSDSModuleBase(Looper looper) {
        super(looper);
    }

    protected static class InitialState extends State {
        protected InitialState() {
        }
    }
}
