package com.sec.internal.ims.cmstore.ambs.provision;

import android.content.Context;
import android.telephony.ServiceState;
import android.telephony.TelephonyCallback;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.sec.internal.constants.ims.cmstore.enumprovision.EnumProvision;
import com.sec.internal.ims.cmstore.utils.Util;
import com.sec.internal.interfaces.ims.cmstore.IControllerCommonInterface;

public class AmbsPhoneStateListener {
    /* access modifiers changed from: private */
    public static final String TAG = "AmbsPhoneStateListener";
    /* access modifiers changed from: private */
    public final Context mContext;
    /* access modifiers changed from: private */
    public IControllerCommonInterface mIControllerCommonInterface;
    /* access modifiers changed from: private */
    public boolean mIsPhoneServiceReady = false;
    private PhoneServiceListener mServiceStateListener = null;
    /* access modifiers changed from: private */
    public final int mSlotId;
    /* access modifiers changed from: private */
    public boolean mZcodeRequested = false;

    AmbsPhoneStateListener(int i, IControllerCommonInterface iControllerCommonInterface, Context context) {
        this.mIControllerCommonInterface = iControllerCommonInterface;
        this.mContext = context;
        this.mSlotId = i;
    }

    public void startListen() {
        if (this.mServiceStateListener == null) {
            this.mServiceStateListener = new PhoneServiceListener();
        }
        this.mZcodeRequested = false;
        TelephonyManager telephonyManager = Util.getTelephonyManager(this.mContext, this.mSlotId);
        if (telephonyManager != null) {
            telephonyManager.registerTelephonyCallback(this.mContext.getMainExecutor(), this.mServiceStateListener);
        }
    }

    public void stopListen() {
        TelephonyManager telephonyManager = Util.getTelephonyManager(this.mContext, this.mSlotId);
        PhoneServiceListener phoneServiceListener = this.mServiceStateListener;
        if (phoneServiceListener == null || telephonyManager == null) {
            Log.d(TAG, "Phone state listener was not initial, maybe provison started form the latest failed api. No need to close it.");
        } else {
            telephonyManager.unregisterTelephonyCallback(phoneServiceListener);
        }
    }

    public class PhoneServiceListener extends TelephonyCallback implements TelephonyCallback.ServiceStateListener {
        public PhoneServiceListener() {
        }

        public void onServiceStateChanged(ServiceState serviceState) {
            String r0 = AmbsPhoneStateListener.TAG;
            Log.i(r0, "onServiceStateChanged " + serviceState.getState() + " mZCode: " + AmbsPhoneStateListener.this.mZcodeRequested + " slot: " + AmbsPhoneStateListener.this.mSlotId);
            AmbsPhoneStateListener.this.mIsPhoneServiceReady = serviceState.getState() == 0 || Util.isWifiCallingEnabled(AmbsPhoneStateListener.this.mContext);
            if (AmbsPhoneStateListener.this.mIsPhoneServiceReady && !AmbsPhoneStateListener.this.mZcodeRequested) {
                AmbsPhoneStateListener.this.mIControllerCommonInterface.update(EnumProvision.ProvisionEventType.REQ_AUTH_ZCODE.getId());
                AmbsPhoneStateListener.this.mZcodeRequested = true;
            }
        }
    }
}
