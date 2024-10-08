package com.sec.internal.ims.config.adapters;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.text.TextUtils;
import com.sec.ims.IAutoConfigurationListener;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceBase;
import com.sec.internal.ims.core.RegistrationGovernor;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.log.IMSLog;
import java.util.Calendar;

public class TelephonyAdapterPrimaryDeviceSec extends TelephonyAdapterPrimaryDeviceBase {
    /* access modifiers changed from: private */
    public static final String LOG_TAG = TelephonyAdapterPrimaryDeviceSec.class.getSimpleName();
    private static final String NIRSMS_KEYWORD = "NIRSMS0001";

    public TelephonyAdapterPrimaryDeviceSec(Context context, IConfigModule iConfigModule, int i) {
        super(context, iConfigModule, i);
        registerPortSmsReceiver();
        initState();
    }

    public void handleMessage(Message message) {
        String str = LOG_TAG;
        IMSLog.i(str, this.mPhoneId, "message:" + message.what);
        int i = message.what;
        if (i != 3) {
            boolean z = false;
            if (i == 4) {
                IMSLog.i(str, this.mPhoneId, "receive port sms");
                Object obj = message.obj;
                if (obj == null) {
                    IMSLog.i(str, this.mPhoneId, "no SMS data!");
                } else if (((String) obj).contains(TelephonyAdapterPrimaryDeviceBase.SMS_CONFIGURATION_REQUEST)) {
                    if (ImsConstants.SystemSettings.getRcsUserSetting(this.mContext, -1, this.mPhoneId) != -1) {
                        z = true;
                    }
                    IMSLog.c(LogClass.TAPDS_RECE_NRCR, this.mPhoneId + ",NRCR:" + TelephonyAdapterPrimaryDeviceBase.SMS_CONFIGURATION_REQUEST + ", RcsUserSetting:" + z);
                    if (z) {
                        IMSLog.i(str, this.mPhoneId, "force configuration request");
                        this.mModule.getHandler().sendMessage(obtainMessage(4, Integer.valueOf(this.mPhoneId)));
                        return;
                    }
                    IMSLog.i(str, this.mPhoneId, "User didn't try RCS service yet");
                } else {
                    removeMessages(5);
                    this.mPortOtp = (String) message.obj;
                    this.mPortOtpReceivedTime = Calendar.getInstance().getTimeInMillis();
                    try {
                        IMSLog.i(str, this.mPhoneId, "otp received: semaphore release with mCurrentPortPermits: " + this.mCurrentPortPermits);
                        this.mPortOtpSemaphore.release(this.mCurrentPortPermits);
                        this.mCurrentPortPermits = 0;
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    }
                }
            } else if (i == 5) {
                this.mPortOtp = null;
                try {
                    IMSLog.i(str, this.mPhoneId, "otp timeout: semaphore release with mCurrentPortPermits: " + this.mCurrentPortPermits);
                    this.mPortOtpSemaphore.release(this.mCurrentPortPermits);
                    this.mCurrentPortPermits = 0;
                } catch (IllegalArgumentException e2) {
                    e2.printStackTrace();
                }
            } else if (i != 8) {
                super.handleMessage(message);
            } else if (this.mIsWaitingForOtp) {
                notifyAutoConfigurationListener(50, true);
            }
        } else {
            this.mOtp = null;
            try {
                IMSLog.i(str, this.mPhoneId, "semaphore release with mCurrentPermits: " + this.mCurrentPermits);
                this.mSemaphore.release(this.mCurrentPermits);
            } catch (IllegalArgumentException e3) {
                e3.printStackTrace();
            }
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
        public ReadyState() {
            super();
            IMSLog.i(TelephonyAdapterPrimaryDeviceSec.LOG_TAG, TelephonyAdapterPrimaryDeviceSec.this.mPhoneId, "ready state");
        }

        public String getOtp() {
            IMSLog.i(TelephonyAdapterPrimaryDeviceSec.LOG_TAG, TelephonyAdapterPrimaryDeviceSec.this.mPhoneId, "getOtp");
            TelephonyAdapterPrimaryDeviceSec telephonyAdapterPrimaryDeviceSec = TelephonyAdapterPrimaryDeviceSec.this;
            telephonyAdapterPrimaryDeviceSec.mCurrentPermits = 0;
            telephonyAdapterPrimaryDeviceSec.mIsWaitingForOtp = true;
            telephonyAdapterPrimaryDeviceSec.sendMessageDelayed(telephonyAdapterPrimaryDeviceSec.obtainMessage(8), 300);
            TelephonyAdapterPrimaryDeviceSec telephonyAdapterPrimaryDeviceSec2 = TelephonyAdapterPrimaryDeviceSec.this;
            telephonyAdapterPrimaryDeviceSec2.sendMessageDelayed(telephonyAdapterPrimaryDeviceSec2.obtainMessage(3), 310000);
            try {
                TelephonyAdapterPrimaryDeviceSec telephonyAdapterPrimaryDeviceSec3 = TelephonyAdapterPrimaryDeviceSec.this;
                telephonyAdapterPrimaryDeviceSec3.mCurrentPermits = telephonyAdapterPrimaryDeviceSec3.mSemaphore.availablePermits() + 1;
                String r0 = TelephonyAdapterPrimaryDeviceSec.LOG_TAG;
                int i = TelephonyAdapterPrimaryDeviceSec.this.mPhoneId;
                IMSLog.i(r0, i, "semaphore acquire with mCurrentPermits: " + TelephonyAdapterPrimaryDeviceSec.this.mCurrentPermits);
                TelephonyAdapterPrimaryDeviceSec telephonyAdapterPrimaryDeviceSec4 = TelephonyAdapterPrimaryDeviceSec.this;
                telephonyAdapterPrimaryDeviceSec4.mSemaphore.acquire(telephonyAdapterPrimaryDeviceSec4.mCurrentPermits);
            } catch (InterruptedException e) {
                TelephonyAdapterPrimaryDeviceSec.this.mIsWaitingForOtp = false;
                e.printStackTrace();
            } catch (IllegalArgumentException e2) {
                TelephonyAdapterPrimaryDeviceSec.this.mIsWaitingForOtp = false;
                e2.printStackTrace();
            }
            TelephonyAdapterPrimaryDeviceSec.this.mIsWaitingForOtp = false;
            String r02 = TelephonyAdapterPrimaryDeviceSec.LOG_TAG;
            int i2 = TelephonyAdapterPrimaryDeviceSec.this.mPhoneId;
            IMSLog.i(r02, i2, "otp: " + IMSLog.checker(TelephonyAdapterPrimaryDeviceSec.this.mOtp));
            return TelephonyAdapterPrimaryDeviceSec.this.mOtp;
        }

        public String getPortOtp() {
            IMSLog.i(TelephonyAdapterPrimaryDeviceSec.LOG_TAG, TelephonyAdapterPrimaryDeviceSec.this.mPhoneId, "getPortOtp");
            TelephonyAdapterPrimaryDeviceSec telephonyAdapterPrimaryDeviceSec = TelephonyAdapterPrimaryDeviceSec.this;
            telephonyAdapterPrimaryDeviceSec.mCurrentPortPermits = 0;
            telephonyAdapterPrimaryDeviceSec.sendMessageDelayed(telephonyAdapterPrimaryDeviceSec.obtainMessage(5), 900000);
            try {
                TelephonyAdapterPrimaryDeviceSec telephonyAdapterPrimaryDeviceSec2 = TelephonyAdapterPrimaryDeviceSec.this;
                telephonyAdapterPrimaryDeviceSec2.mCurrentPortPermits = telephonyAdapterPrimaryDeviceSec2.mPortOtpSemaphore.availablePermits() + 1;
                String r0 = TelephonyAdapterPrimaryDeviceSec.LOG_TAG;
                int i = TelephonyAdapterPrimaryDeviceSec.this.mPhoneId;
                IMSLog.i(r0, i, "getPortOtp: semaphore acquire with mCurrentPortPermits: " + TelephonyAdapterPrimaryDeviceSec.this.mCurrentPortPermits);
                TelephonyAdapterPrimaryDeviceSec telephonyAdapterPrimaryDeviceSec3 = TelephonyAdapterPrimaryDeviceSec.this;
                telephonyAdapterPrimaryDeviceSec3.mPortOtpSemaphore.acquire(telephonyAdapterPrimaryDeviceSec3.mCurrentPortPermits);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e2) {
                e2.printStackTrace();
            }
            String r02 = TelephonyAdapterPrimaryDeviceSec.LOG_TAG;
            int i2 = TelephonyAdapterPrimaryDeviceSec.this.mPhoneId;
            IMSLog.i(r02, i2, "receive Port OTP:" + IMSLog.checker(TelephonyAdapterPrimaryDeviceSec.this.mPortOtp));
            return TelephonyAdapterPrimaryDeviceSec.this.mPortOtp;
        }

        public void registerAutoConfigurationListener(IAutoConfigurationListener iAutoConfigurationListener) {
            if (iAutoConfigurationListener == null) {
                IMSLog.i(TelephonyAdapterPrimaryDeviceSec.LOG_TAG, TelephonyAdapterPrimaryDeviceSec.this.mPhoneId, "listener: null");
                return;
            }
            synchronized (TelephonyAdapterPrimaryDeviceSec.this.mLock) {
                if (TelephonyAdapterPrimaryDeviceSec.this.mAutoConfigurationListener != null) {
                    String r1 = TelephonyAdapterPrimaryDeviceSec.LOG_TAG;
                    int i = TelephonyAdapterPrimaryDeviceSec.this.mPhoneId;
                    IMSLog.i(r1, i, "register listener: " + iAutoConfigurationListener);
                    IMSLog.c(LogClass.TAPDS_LISTNER, TelephonyAdapterPrimaryDeviceSec.this.mPhoneId + "," + iAutoConfigurationListener);
                    TelephonyAdapterPrimaryDeviceSec.this.mAutoConfigurationListener.register(iAutoConfigurationListener);
                    if (!TelephonyAdapterPrimaryDeviceSec.this.mPostponedNotification.isEmpty()) {
                        for (Integer intValue : TelephonyAdapterPrimaryDeviceSec.this.mPostponedNotification.keySet()) {
                            int intValue2 = intValue.intValue();
                            notifyAutoConfigurationListener(intValue2, TelephonyAdapterPrimaryDeviceSec.this.mPostponedNotification.get(Integer.valueOf(intValue2)).booleanValue());
                        }
                    }
                }
            }
        }

        public void unregisterAutoConfigurationListener(IAutoConfigurationListener iAutoConfigurationListener) {
            if (iAutoConfigurationListener == null) {
                IMSLog.i(TelephonyAdapterPrimaryDeviceSec.LOG_TAG, TelephonyAdapterPrimaryDeviceSec.this.mPhoneId, "listener: null");
                return;
            }
            synchronized (TelephonyAdapterPrimaryDeviceSec.this.mLock) {
                if (TelephonyAdapterPrimaryDeviceSec.this.mAutoConfigurationListener != null) {
                    String r1 = TelephonyAdapterPrimaryDeviceSec.LOG_TAG;
                    int i = TelephonyAdapterPrimaryDeviceSec.this.mPhoneId;
                    IMSLog.i(r1, i, "unregister listener: " + iAutoConfigurationListener);
                    TelephonyAdapterPrimaryDeviceSec.this.mAutoConfigurationListener.unregister(iAutoConfigurationListener);
                    TelephonyAdapterPrimaryDeviceSec.this.mPostponedNotification.clear();
                }
            }
        }

        public void notifyAutoConfigurationListener(int i, boolean z) {
            String r0 = TelephonyAdapterPrimaryDeviceSec.LOG_TAG;
            int i2 = TelephonyAdapterPrimaryDeviceSec.this.mPhoneId;
            IMSLog.i(r0, i2, "notifyAutoConfigurationListener: type: " + i + ", result: " + z);
            if (i != 50 && i != 52) {
                IMSLog.i(TelephonyAdapterPrimaryDeviceSec.LOG_TAG, TelephonyAdapterPrimaryDeviceSec.this.mPhoneId, "notifyAutoConfigurationListener: unknown notification type");
                return;
            } else if (i != 50 || TelephonyAdapterPrimaryDeviceSec.this.mIsWaitingForOtp) {
                synchronized (TelephonyAdapterPrimaryDeviceSec.this.mLock) {
                    RemoteCallbackList<IAutoConfigurationListener> remoteCallbackList = TelephonyAdapterPrimaryDeviceSec.this.mAutoConfigurationListener;
                    if (remoteCallbackList == null) {
                        IMSLog.i(TelephonyAdapterPrimaryDeviceSec.LOG_TAG, TelephonyAdapterPrimaryDeviceSec.this.mPhoneId, "notifyAutoConfigurationListener: mAutoConfigurationListener: empty");
                        return;
                    }
                    try {
                        int beginBroadcast = remoteCallbackList.beginBroadcast();
                        String r4 = TelephonyAdapterPrimaryDeviceSec.LOG_TAG;
                        int i3 = TelephonyAdapterPrimaryDeviceSec.this.mPhoneId;
                        IMSLog.i(r4, i3, "notifyAutoConfigurationListener: listener length: " + beginBroadcast);
                        if (i == 50) {
                            IMSLog.i(TelephonyAdapterPrimaryDeviceSec.LOG_TAG, TelephonyAdapterPrimaryDeviceSec.this.mPhoneId, "notifyAutoConfigurationListener: onVerificationCodeNeeded");
                            IMSLog.c(LogClass.TAPDS_OTP_NEEDED, TelephonyAdapterPrimaryDeviceSec.this.mPhoneId + ",VCN,LEN:" + beginBroadcast);
                        } else {
                            IMSLog.i(TelephonyAdapterPrimaryDeviceSec.LOG_TAG, TelephonyAdapterPrimaryDeviceSec.this.mPhoneId, "notifyAutoConfigurationListener: onAutoConfigurationCompleted");
                            IMSLog.c(LogClass.TAPDS_ACS_RESULT, TelephonyAdapterPrimaryDeviceSec.this.mPhoneId + ",ACS:" + z + ",LEN:" + beginBroadcast);
                        }
                        if (beginBroadcast == 0) {
                            String r42 = TelephonyAdapterPrimaryDeviceSec.LOG_TAG;
                            int i4 = TelephonyAdapterPrimaryDeviceSec.this.mPhoneId;
                            IMSLog.i(r42, i4, "Listener not registered yet. Postpone notify later: " + i);
                            if (i == 52) {
                                TelephonyAdapterPrimaryDeviceSec.this.mPostponedNotification.clear();
                            }
                            TelephonyAdapterPrimaryDeviceSec.this.mPostponedNotification.put(Integer.valueOf(i), Boolean.valueOf(z));
                        }
                        for (int i5 = 0; i5 < beginBroadcast; i5++) {
                            IAutoConfigurationListener broadcastItem = TelephonyAdapterPrimaryDeviceSec.this.mAutoConfigurationListener.getBroadcastItem(i5);
                            if (i == 50) {
                                broadcastItem.onVerificationCodeNeeded();
                            } else {
                                broadcastItem.onAutoConfigurationCompleted(z);
                                TelephonyAdapterPrimaryDeviceSec.this.mPostponedNotification.clear();
                            }
                        }
                    } catch (RemoteException | IllegalStateException | NullPointerException e) {
                        String r10 = TelephonyAdapterPrimaryDeviceSec.LOG_TAG;
                        int i6 = TelephonyAdapterPrimaryDeviceSec.this.mPhoneId;
                        IMSLog.i(r10, i6, "notifyAutoConfigurationListener: Exception: " + e.getMessage());
                    }
                    try {
                        TelephonyAdapterPrimaryDeviceSec.this.mAutoConfigurationListener.finishBroadcast();
                    } catch (IllegalStateException e2) {
                        String r102 = TelephonyAdapterPrimaryDeviceSec.LOG_TAG;
                        int i7 = TelephonyAdapterPrimaryDeviceSec.this.mPhoneId;
                        IMSLog.i(r102, i7, "notifyAutoConfigurationListener: finishBroadcast Exception: " + e2.getMessage());
                    }
                }
            } else {
                String r9 = TelephonyAdapterPrimaryDeviceSec.LOG_TAG;
                int i8 = TelephonyAdapterPrimaryDeviceSec.this.mPhoneId;
                IMSLog.i(r9, i8, "notifyAutoConfigurationListener: ignore notification type, mIsWaitingForOtp: " + TelephonyAdapterPrimaryDeviceSec.this.mIsWaitingForOtp);
                return;
            }
        }

        public void sendVerificationCode(String str) {
            String r0 = TelephonyAdapterPrimaryDeviceSec.LOG_TAG;
            int i = TelephonyAdapterPrimaryDeviceSec.this.mPhoneId;
            IMSLog.i(r0, i, "sendVerificationCode value: " + str);
            IMSLog.c(LogClass.TAPDS_SEND_OTP, TelephonyAdapterPrimaryDeviceSec.this.mPhoneId + ",VC:" + str);
            TelephonyAdapterPrimaryDeviceSec telephonyAdapterPrimaryDeviceSec = TelephonyAdapterPrimaryDeviceSec.this;
            if (telephonyAdapterPrimaryDeviceSec.mIsWaitingForOtp) {
                telephonyAdapterPrimaryDeviceSec.removeMessages(3);
                TelephonyAdapterPrimaryDeviceSec telephonyAdapterPrimaryDeviceSec2 = TelephonyAdapterPrimaryDeviceSec.this;
                telephonyAdapterPrimaryDeviceSec2.mOtp = str;
                if (str != null) {
                    telephonyAdapterPrimaryDeviceSec2.mOtpReceivedTime = Calendar.getInstance().getTimeInMillis();
                }
                try {
                    String r5 = TelephonyAdapterPrimaryDeviceSec.LOG_TAG;
                    int i2 = TelephonyAdapterPrimaryDeviceSec.this.mPhoneId;
                    IMSLog.i(r5, i2, "semaphore release with mCurrentPermits: " + TelephonyAdapterPrimaryDeviceSec.this.mCurrentPermits);
                    TelephonyAdapterPrimaryDeviceSec telephonyAdapterPrimaryDeviceSec3 = TelephonyAdapterPrimaryDeviceSec.this;
                    telephonyAdapterPrimaryDeviceSec3.mSemaphore.release(telephonyAdapterPrimaryDeviceSec3.mCurrentPermits);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            } else if (TelephonyAdapterPrimaryDeviceSec.NIRSMS_KEYWORD.equals(str)) {
                TelephonyAdapterPrimaryDeviceSec telephonyAdapterPrimaryDeviceSec4 = TelephonyAdapterPrimaryDeviceSec.this;
                if (ImsConstants.SystemSettings.getRcsUserSetting(telephonyAdapterPrimaryDeviceSec4.mContext, -1, telephonyAdapterPrimaryDeviceSec4.mPhoneId) != -1) {
                    IMSLog.i(TelephonyAdapterPrimaryDeviceSec.LOG_TAG, TelephonyAdapterPrimaryDeviceSec.this.mPhoneId, "sendVerificationCode: NIRSMS0001 received, force configuration request");
                    IMSLog.c(LogClass.TAPDS_RECE_NIRSMS, TelephonyAdapterPrimaryDeviceSec.this.mPhoneId + ",NRCR:" + str);
                    Handler handler = TelephonyAdapterPrimaryDeviceSec.this.mModule.getHandler();
                    TelephonyAdapterPrimaryDeviceSec telephonyAdapterPrimaryDeviceSec5 = TelephonyAdapterPrimaryDeviceSec.this;
                    handler.sendMessage(telephonyAdapterPrimaryDeviceSec5.obtainMessage(4, Integer.valueOf(telephonyAdapterPrimaryDeviceSec5.mPhoneId)));
                    return;
                }
                IMSLog.i(TelephonyAdapterPrimaryDeviceSec.LOG_TAG, TelephonyAdapterPrimaryDeviceSec.this.mPhoneId, "sendVerificationCode: NIRSMS0001 received, but User didn't try RCS service yet");
            }
        }
    }

    protected class AbsentState extends TelephonyAdapterPrimaryDeviceBase.AbsentState {
        public String getDeviceId(int i) {
            return null;
        }

        public String getMsisdn(int i) {
            return null;
        }

        public String getSubscriberId(int i) {
            return null;
        }

        protected AbsentState() {
            super();
        }

        public String getOtp() {
            long timeInMillis = Calendar.getInstance().getTimeInMillis();
            TelephonyAdapterPrimaryDeviceSec telephonyAdapterPrimaryDeviceSec = TelephonyAdapterPrimaryDeviceSec.this;
            if (telephonyAdapterPrimaryDeviceSec.mOtp == null || timeInMillis >= telephonyAdapterPrimaryDeviceSec.mOtpReceivedTime + RegistrationGovernor.RETRY_AFTER_PDNLOST_MS) {
                return null;
            }
            IMSLog.i(TelephonyAdapterPrimaryDeviceSec.LOG_TAG, TelephonyAdapterPrimaryDeviceSec.this.mPhoneId, "getOtp exist");
            return TelephonyAdapterPrimaryDeviceSec.this.mOtp;
        }

        public String getPortOtp() {
            long timeInMillis = Calendar.getInstance().getTimeInMillis();
            TelephonyAdapterPrimaryDeviceSec telephonyAdapterPrimaryDeviceSec = TelephonyAdapterPrimaryDeviceSec.this;
            if (telephonyAdapterPrimaryDeviceSec.mPortOtp == null || timeInMillis >= telephonyAdapterPrimaryDeviceSec.mPortOtpReceivedTime + RegistrationGovernor.RETRY_AFTER_PDNLOST_MS) {
                return null;
            }
            IMSLog.i(TelephonyAdapterPrimaryDeviceSec.LOG_TAG, TelephonyAdapterPrimaryDeviceSec.this.mPhoneId, "getPortOtp exist");
            return TelephonyAdapterPrimaryDeviceSec.this.mPortOtp;
        }

        public String getIdentityByPhoneId(int i) {
            String str;
            IMSLog.i(TelephonyAdapterPrimaryDeviceSec.LOG_TAG, i, "getIdentityByPhoneId: ABSENT");
            String imei = TelephonyAdapterPrimaryDeviceSec.this.mTelephony.getImei(i);
            if (!TextUtils.isEmpty(imei)) {
                str = "IMEI_" + imei;
            } else {
                IMSLog.i(TelephonyAdapterPrimaryDeviceSec.LOG_TAG, i, "identity error");
                str = "";
            }
            return str.replaceAll("[\\W]", "");
        }
    }

    public String getOtp() {
        return this.mState.getOtp();
    }
}
