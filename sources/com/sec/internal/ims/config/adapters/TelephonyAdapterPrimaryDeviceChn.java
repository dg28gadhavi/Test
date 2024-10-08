package com.sec.internal.ims.config.adapters;

import android.content.Context;
import android.os.Message;
import com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceBase;
import com.sec.internal.ims.core.RegistrationGovernor;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.log.IMSLog;
import java.util.Calendar;

public class TelephonyAdapterPrimaryDeviceChn extends TelephonyAdapterPrimaryDeviceBase {
    /* access modifiers changed from: private */
    public static final String LOG_TAG = TelephonyAdapterPrimaryDeviceChn.class.getSimpleName();

    public TelephonyAdapterPrimaryDeviceChn(Context context, IConfigModule iConfigModule, int i) {
        super(context, iConfigModule, i);
        registerSmsReceiver();
        initState();
    }

    public void handleMessage(Message message) {
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "message:" + message.what);
        int i2 = message.what;
        if (i2 == 1) {
            handleReceivedDataSms(message, false, false);
        } else if (i2 != 3) {
            super.handleMessage(message);
        } else {
            handleOtpTimeout(false);
        }
    }

    /* access modifiers changed from: protected */
    public void getState(String str) {
        String str2 = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str2, i, "getState: change to " + str);
        if (TelephonyAdapterState.READY_STATE.equals(str)) {
            this.mState = new ReadyState();
        } else if (TelephonyAdapterState.ABSENT_STATE.equals(str)) {
            this.mState = new AbsentState();
        } else {
            super.getState(str);
        }
    }

    protected class ReadyState extends TelephonyAdapterPrimaryDeviceBase.ReadyState {
        protected ReadyState() {
            super();
        }

        public String getOtp() {
            long timeInMillis = Calendar.getInstance().getTimeInMillis();
            TelephonyAdapterPrimaryDeviceChn telephonyAdapterPrimaryDeviceChn = TelephonyAdapterPrimaryDeviceChn.this;
            if (telephonyAdapterPrimaryDeviceChn.mOtp == null || timeInMillis >= telephonyAdapterPrimaryDeviceChn.mOtpReceivedTime + RegistrationGovernor.RETRY_AFTER_PDNLOST_MS) {
                IMSLog.i(TelephonyAdapterPrimaryDeviceChn.LOG_TAG, TelephonyAdapterPrimaryDeviceChn.this.mPhoneId, "OTP don't exist. wait OTP");
                try {
                    TelephonyAdapterPrimaryDeviceChn.this.mSemaphore.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                TelephonyAdapterPrimaryDeviceChn.this.removeMessages(3);
                String r0 = TelephonyAdapterPrimaryDeviceChn.LOG_TAG;
                int i = TelephonyAdapterPrimaryDeviceChn.this.mPhoneId;
                IMSLog.i(r0, i, "receive OTP: " + IMSLog.checker(TelephonyAdapterPrimaryDeviceChn.this.mOtp));
            } else {
                IMSLog.i(TelephonyAdapterPrimaryDeviceChn.LOG_TAG, TelephonyAdapterPrimaryDeviceChn.this.mPhoneId, "OTP exist. send immediately");
            }
            return TelephonyAdapterPrimaryDeviceChn.this.mOtp;
        }
    }

    protected class AbsentState extends TelephonyAdapterPrimaryDeviceBase.AbsentState {
        public String getDeviceId(int i) {
            return null;
        }

        public String getIdentityByPhoneId(int i) {
            return null;
        }

        protected AbsentState() {
            super();
        }

        public String getOtp() {
            long timeInMillis = Calendar.getInstance().getTimeInMillis();
            TelephonyAdapterPrimaryDeviceChn telephonyAdapterPrimaryDeviceChn = TelephonyAdapterPrimaryDeviceChn.this;
            if (telephonyAdapterPrimaryDeviceChn.mOtp == null || timeInMillis >= telephonyAdapterPrimaryDeviceChn.mOtpReceivedTime + RegistrationGovernor.RETRY_AFTER_PDNLOST_MS) {
                IMSLog.i(TelephonyAdapterPrimaryDeviceChn.LOG_TAG, TelephonyAdapterPrimaryDeviceChn.this.mPhoneId, "OTP don't exist. wait OTP");
                try {
                    TelephonyAdapterPrimaryDeviceChn.this.mSemaphore.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                String r0 = TelephonyAdapterPrimaryDeviceChn.LOG_TAG;
                int i = TelephonyAdapterPrimaryDeviceChn.this.mPhoneId;
                IMSLog.i(r0, i, "receive OTP:" + IMSLog.checker(TelephonyAdapterPrimaryDeviceChn.this.mOtp));
            } else {
                IMSLog.i(TelephonyAdapterPrimaryDeviceChn.LOG_TAG, TelephonyAdapterPrimaryDeviceChn.this.mPhoneId, "OTP exist. send immediately");
            }
            return TelephonyAdapterPrimaryDeviceChn.this.mOtp;
        }
    }
}
