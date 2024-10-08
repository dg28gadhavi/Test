package com.sec.internal.ims.config.adapters;

import android.content.Context;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.text.TextUtils;
import com.sec.ims.IAutoConfigurationListener;
import com.sec.internal.constants.Mno;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceBase;
import com.sec.internal.ims.core.RegistrationGovernor;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.log.IMSLog;
import java.util.Calendar;
import java.util.concurrent.Semaphore;

public class TelephonyAdapterPrimaryDeviceJibe extends TelephonyAdapterPrimaryDeviceBase {
    /* access modifiers changed from: private */
    public static final String LOG_TAG = TelephonyAdapterPrimaryDeviceJibe.class.getSimpleName();
    protected Semaphore mOtpSemaphore = new Semaphore(0);

    public TelephonyAdapterPrimaryDeviceJibe(Context context, IConfigModule iConfigModule, int i) {
        super(context, iConfigModule, i);
        registerPortSmsReceiver();
        initState();
    }

    public void handleMessage(Message message) {
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "message:" + message.what);
        int i2 = message.what;
        if (i2 != 14) {
            if (i2 != 15) {
                switch (i2) {
                    case 2:
                        if (this.mIsWaitingForOtp) {
                            notifyAutoConfigurationListener(50, true);
                            return;
                        }
                        return;
                    case 3:
                        this.mOtp = null;
                        try {
                            int i3 = this.mPhoneId;
                            IMSLog.i(str, i3, "semaphore release with mCurrentOtpPermits: " + this.mCurrentOtpPermits);
                            IMSLog.c(LogClass.TAPDJ_OTP_TIMEOUT, this.mPhoneId + ",OT");
                            this.mOtpSemaphore.release(this.mCurrentOtpPermits);
                            return;
                        } catch (IllegalArgumentException e) {
                            e.printStackTrace();
                            return;
                        }
                    case 4:
                        IMSLog.i(str, this.mPhoneId, "receive port sms");
                        Object obj = message.obj;
                        if (obj == null) {
                            IMSLog.i(str, this.mPhoneId, "no SMS data!");
                            return;
                        } else if (((String) obj).contains(TelephonyAdapterPrimaryDeviceBase.SMS_CONFIGURATION_REQUEST)) {
                            IMSLog.i(str, this.mPhoneId, "request force configuration");
                            this.mModule.getHandler().sendMessage(obtainMessage(21, Integer.valueOf(this.mPhoneId)));
                            return;
                        } else {
                            removeMessages(5);
                            this.mPortOtp = (String) message.obj;
                            int i4 = this.mPhoneId;
                            IMSLog.i(str, i4, "mPortOtp: " + IMSLog.checker(this.mPortOtp));
                            this.mPortOtpReceivedTime = Calendar.getInstance().getTimeInMillis();
                            try {
                                int i5 = this.mPhoneId;
                                IMSLog.i(str, i5, "otp received: semaphore release with mCurrentPortPermits: " + this.mCurrentPortPermits);
                                this.mPortOtpSemaphore.release(this.mCurrentPortPermits);
                                this.mCurrentPortPermits = 0;
                                return;
                            } catch (IllegalArgumentException e2) {
                                e2.printStackTrace();
                                return;
                            }
                        }
                    case 5:
                        this.mPortOtp = null;
                        try {
                            int i6 = this.mPhoneId;
                            IMSLog.i(str, i6, "otp timeout: semaphore release with mCurrentPortPermits: " + this.mCurrentPortPermits);
                            IMSLog.c(LogClass.TAPDJ_PORT_OTP_TIMEOUT, this.mPhoneId + ",POT");
                            this.mPortOtpSemaphore.release(this.mCurrentPortPermits);
                            this.mCurrentPortPermits = 0;
                            return;
                        } catch (IllegalArgumentException e3) {
                            e3.printStackTrace();
                            return;
                        }
                    case 6:
                        if (this.mIsWaitingForMsisdn) {
                            notifyAutoConfigurationListener(51, true);
                            return;
                        }
                        return;
                    case 7:
                        this.mMsisdn = null;
                        try {
                            int i7 = this.mPhoneId;
                            IMSLog.i(str, i7, "semaphore release with mCurrentMsisdnPermits: " + this.mCurrentMsisdnPermits);
                            IMSLog.c(LogClass.TAPDJ_MSISDN_TIMEOUT, this.mPhoneId + ",MT");
                            this.mMsisdnSemaphore.release(this.mCurrentMsisdnPermits);
                            return;
                        } catch (IllegalArgumentException e4) {
                            e4.printStackTrace();
                            return;
                        }
                    default:
                        super.handleMessage(message);
                        return;
                }
            } else {
                this.mIidToken = null;
                try {
                    int i8 = this.mPhoneId;
                    IMSLog.i(str, i8, "semaphore release with mCurrentIidTokenPermits: " + this.mCurrentIidTokenPermits);
                    IMSLog.c(LogClass.TAPDJ_IIDTOKEN_TIMEOUT, this.mPhoneId + ",ITT");
                    this.mIidTokenSemaphore.release(this.mCurrentIidTokenPermits);
                } catch (IllegalArgumentException e5) {
                    e5.printStackTrace();
                }
            }
        } else if (this.mIsWaitingForIidToken) {
            notifyAutoConfigurationListener(53, true);
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
            IMSLog.i(TelephonyAdapterPrimaryDeviceJibe.LOG_TAG, TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId, "ready state");
        }

        public String getOtp() {
            IMSLog.i(TelephonyAdapterPrimaryDeviceJibe.LOG_TAG, TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId, "getOtp");
            TelephonyAdapterPrimaryDeviceJibe telephonyAdapterPrimaryDeviceJibe = TelephonyAdapterPrimaryDeviceJibe.this;
            telephonyAdapterPrimaryDeviceJibe.mCurrentOtpPermits = 0;
            telephonyAdapterPrimaryDeviceJibe.mIsWaitingForOtp = true;
            telephonyAdapterPrimaryDeviceJibe.sendMessageDelayed(telephonyAdapterPrimaryDeviceJibe.obtainMessage(2), 300);
            TelephonyAdapterPrimaryDeviceJibe telephonyAdapterPrimaryDeviceJibe2 = TelephonyAdapterPrimaryDeviceJibe.this;
            telephonyAdapterPrimaryDeviceJibe2.sendMessageDelayed(telephonyAdapterPrimaryDeviceJibe2.obtainMessage(3), 310000);
            try {
                TelephonyAdapterPrimaryDeviceJibe telephonyAdapterPrimaryDeviceJibe3 = TelephonyAdapterPrimaryDeviceJibe.this;
                telephonyAdapterPrimaryDeviceJibe3.mCurrentOtpPermits = telephonyAdapterPrimaryDeviceJibe3.mOtpSemaphore.availablePermits() + 1;
                String r0 = TelephonyAdapterPrimaryDeviceJibe.LOG_TAG;
                int i = TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId;
                IMSLog.i(r0, i, "semaphore acquire with mCurrentOtpPermits: " + TelephonyAdapterPrimaryDeviceJibe.this.mCurrentOtpPermits);
                TelephonyAdapterPrimaryDeviceJibe telephonyAdapterPrimaryDeviceJibe4 = TelephonyAdapterPrimaryDeviceJibe.this;
                telephonyAdapterPrimaryDeviceJibe4.mOtpSemaphore.acquire(telephonyAdapterPrimaryDeviceJibe4.mCurrentOtpPermits);
            } catch (IllegalArgumentException | InterruptedException e) {
                e.printStackTrace();
            }
            TelephonyAdapterPrimaryDeviceJibe telephonyAdapterPrimaryDeviceJibe5 = TelephonyAdapterPrimaryDeviceJibe.this;
            telephonyAdapterPrimaryDeviceJibe5.mIsWaitingForOtp = false;
            return telephonyAdapterPrimaryDeviceJibe5.mOtp;
        }

        public String getPortOtp() {
            IMSLog.i(TelephonyAdapterPrimaryDeviceJibe.LOG_TAG, TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId, "getPortOtp");
            TelephonyAdapterPrimaryDeviceJibe telephonyAdapterPrimaryDeviceJibe = TelephonyAdapterPrimaryDeviceJibe.this;
            telephonyAdapterPrimaryDeviceJibe.mCurrentPortPermits = 0;
            telephonyAdapterPrimaryDeviceJibe.sendMessageDelayed(telephonyAdapterPrimaryDeviceJibe.obtainMessage(5), 300000);
            try {
                TelephonyAdapterPrimaryDeviceJibe telephonyAdapterPrimaryDeviceJibe2 = TelephonyAdapterPrimaryDeviceJibe.this;
                telephonyAdapterPrimaryDeviceJibe2.mCurrentPortPermits = telephonyAdapterPrimaryDeviceJibe2.mPortOtpSemaphore.availablePermits() + 1;
                String r0 = TelephonyAdapterPrimaryDeviceJibe.LOG_TAG;
                int i = TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId;
                IMSLog.i(r0, i, "getPortOtp: semaphore acquire with mCurrentPortPermits: " + TelephonyAdapterPrimaryDeviceJibe.this.mCurrentPortPermits);
                TelephonyAdapterPrimaryDeviceJibe telephonyAdapterPrimaryDeviceJibe3 = TelephonyAdapterPrimaryDeviceJibe.this;
                telephonyAdapterPrimaryDeviceJibe3.mPortOtpSemaphore.acquire(telephonyAdapterPrimaryDeviceJibe3.mCurrentPortPermits);
            } catch (IllegalArgumentException | InterruptedException e) {
                e.printStackTrace();
            }
            return TelephonyAdapterPrimaryDeviceJibe.this.mPortOtp;
        }

        public String getMsisdnNumber() {
            IMSLog.i(TelephonyAdapterPrimaryDeviceJibe.LOG_TAG, TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId, "getMsisdnNumber");
            String msisdn = getMsisdn();
            if (!TextUtils.isEmpty(msisdn)) {
                IMSLog.i(TelephonyAdapterPrimaryDeviceJibe.LOG_TAG, TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId, "msisdn exists from telephony");
                IMSLog.c(LogClass.TAPDJ_EXIST_MSISDN_TELEPHONY, TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId + ",EMT");
                TelephonyAdapterPrimaryDeviceJibe.this.mMsisdn = msisdn;
                return msisdn;
            }
            Mno simMno = SimUtil.getSimMno(TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId);
            if (ConfigUtil.isRcsPreConsent(TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId) || !simMno.isEmeasewaoce()) {
                IMSLog.i(TelephonyAdapterPrimaryDeviceJibe.LOG_TAG, TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId, "need to get msisdn from application");
                TelephonyAdapterPrimaryDeviceJibe telephonyAdapterPrimaryDeviceJibe = TelephonyAdapterPrimaryDeviceJibe.this;
                telephonyAdapterPrimaryDeviceJibe.mCurrentMsisdnPermits = 0;
                telephonyAdapterPrimaryDeviceJibe.mIsWaitingForMsisdn = true;
                telephonyAdapterPrimaryDeviceJibe.sendMessageDelayed(telephonyAdapterPrimaryDeviceJibe.obtainMessage(6), 300);
                TelephonyAdapterPrimaryDeviceJibe telephonyAdapterPrimaryDeviceJibe2 = TelephonyAdapterPrimaryDeviceJibe.this;
                telephonyAdapterPrimaryDeviceJibe2.sendMessageDelayed(telephonyAdapterPrimaryDeviceJibe2.obtainMessage(7), 310000);
                try {
                    TelephonyAdapterPrimaryDeviceJibe telephonyAdapterPrimaryDeviceJibe3 = TelephonyAdapterPrimaryDeviceJibe.this;
                    telephonyAdapterPrimaryDeviceJibe3.mCurrentMsisdnPermits = telephonyAdapterPrimaryDeviceJibe3.mMsisdnSemaphore.availablePermits() + 1;
                    String r0 = TelephonyAdapterPrimaryDeviceJibe.LOG_TAG;
                    int i = TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId;
                    IMSLog.i(r0, i, "semaphore acquire with mCurrentMsisdnPermits: " + TelephonyAdapterPrimaryDeviceJibe.this.mCurrentMsisdnPermits);
                    TelephonyAdapterPrimaryDeviceJibe telephonyAdapterPrimaryDeviceJibe4 = TelephonyAdapterPrimaryDeviceJibe.this;
                    telephonyAdapterPrimaryDeviceJibe4.mMsisdnSemaphore.acquire(telephonyAdapterPrimaryDeviceJibe4.mCurrentMsisdnPermits);
                } catch (IllegalArgumentException | InterruptedException e) {
                    e.printStackTrace();
                }
                TelephonyAdapterPrimaryDeviceJibe telephonyAdapterPrimaryDeviceJibe5 = TelephonyAdapterPrimaryDeviceJibe.this;
                telephonyAdapterPrimaryDeviceJibe5.mIsWaitingForMsisdn = false;
                return telephonyAdapterPrimaryDeviceJibe5.mMsisdn;
            }
            IMSLog.i(TelephonyAdapterPrimaryDeviceJibe.LOG_TAG, TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId, "operator using jibe, but not GC, use FW's dialog to ask for MSISDN");
            return null;
        }

        public String getIidToken() {
            IMSLog.i(TelephonyAdapterPrimaryDeviceJibe.LOG_TAG, TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId, "getIidToken");
            TelephonyAdapterPrimaryDeviceJibe telephonyAdapterPrimaryDeviceJibe = TelephonyAdapterPrimaryDeviceJibe.this;
            telephonyAdapterPrimaryDeviceJibe.mCurrentIidTokenPermits = 0;
            telephonyAdapterPrimaryDeviceJibe.mIsWaitingForIidToken = true;
            telephonyAdapterPrimaryDeviceJibe.sendMessageDelayed(telephonyAdapterPrimaryDeviceJibe.obtainMessage(14), 300);
            TelephonyAdapterPrimaryDeviceJibe telephonyAdapterPrimaryDeviceJibe2 = TelephonyAdapterPrimaryDeviceJibe.this;
            telephonyAdapterPrimaryDeviceJibe2.sendMessageDelayed(telephonyAdapterPrimaryDeviceJibe2.obtainMessage(15), 310000);
            try {
                TelephonyAdapterPrimaryDeviceJibe telephonyAdapterPrimaryDeviceJibe3 = TelephonyAdapterPrimaryDeviceJibe.this;
                telephonyAdapterPrimaryDeviceJibe3.mCurrentIidTokenPermits = telephonyAdapterPrimaryDeviceJibe3.mIidTokenSemaphore.availablePermits() + 1;
                String r0 = TelephonyAdapterPrimaryDeviceJibe.LOG_TAG;
                int i = TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId;
                IMSLog.i(r0, i, "semaphore acquire with mCurrentIidTokenPermits: " + TelephonyAdapterPrimaryDeviceJibe.this.mCurrentIidTokenPermits);
                TelephonyAdapterPrimaryDeviceJibe telephonyAdapterPrimaryDeviceJibe4 = TelephonyAdapterPrimaryDeviceJibe.this;
                telephonyAdapterPrimaryDeviceJibe4.mIidTokenSemaphore.acquire(telephonyAdapterPrimaryDeviceJibe4.mCurrentIidTokenPermits);
            } catch (IllegalArgumentException | InterruptedException e) {
                e.printStackTrace();
            }
            TelephonyAdapterPrimaryDeviceJibe telephonyAdapterPrimaryDeviceJibe5 = TelephonyAdapterPrimaryDeviceJibe.this;
            telephonyAdapterPrimaryDeviceJibe5.mIsWaitingForIidToken = false;
            String str = telephonyAdapterPrimaryDeviceJibe5.mIidToken;
            telephonyAdapterPrimaryDeviceJibe5.mIidToken = null;
            return str;
        }

        public void registerAutoConfigurationListener(IAutoConfigurationListener iAutoConfigurationListener) {
            if (iAutoConfigurationListener == null) {
                IMSLog.i(TelephonyAdapterPrimaryDeviceJibe.LOG_TAG, TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId, "listener: null");
                return;
            }
            synchronized (TelephonyAdapterPrimaryDeviceJibe.this.mLock) {
                if (TelephonyAdapterPrimaryDeviceJibe.this.mAutoConfigurationListener != null) {
                    String r1 = TelephonyAdapterPrimaryDeviceJibe.LOG_TAG;
                    int i = TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId;
                    IMSLog.i(r1, i, "register listener: " + iAutoConfigurationListener);
                    IMSLog.c(LogClass.TAPDJ_REG_LISTNER, TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId + ", listener added");
                    TelephonyAdapterPrimaryDeviceJibe.this.mAutoConfigurationListener.register(iAutoConfigurationListener);
                    if (!TelephonyAdapterPrimaryDeviceJibe.this.mPostponedNotification.isEmpty()) {
                        for (Integer intValue : TelephonyAdapterPrimaryDeviceJibe.this.mPostponedNotification.keySet()) {
                            int intValue2 = intValue.intValue();
                            notifyAutoConfigurationListener(intValue2, TelephonyAdapterPrimaryDeviceJibe.this.mPostponedNotification.get(Integer.valueOf(intValue2)).booleanValue());
                        }
                    }
                }
            }
        }

        public void unregisterAutoConfigurationListener(IAutoConfigurationListener iAutoConfigurationListener) {
            if (iAutoConfigurationListener == null) {
                IMSLog.i(TelephonyAdapterPrimaryDeviceJibe.LOG_TAG, TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId, "listener: null");
                return;
            }
            synchronized (TelephonyAdapterPrimaryDeviceJibe.this.mLock) {
                if (TelephonyAdapterPrimaryDeviceJibe.this.mAutoConfigurationListener != null) {
                    String r1 = TelephonyAdapterPrimaryDeviceJibe.LOG_TAG;
                    int i = TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId;
                    IMSLog.i(r1, i, "unregister listener: " + iAutoConfigurationListener);
                    IMSLog.c(LogClass.TAPDJ_UNREG_LISTNER, TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId + ",UL:" + iAutoConfigurationListener);
                    TelephonyAdapterPrimaryDeviceJibe.this.mAutoConfigurationListener.unregister(iAutoConfigurationListener);
                    TelephonyAdapterPrimaryDeviceJibe.this.mPostponedNotification.clear();
                }
            }
        }

        public void notifyAutoConfigurationListener(int i, boolean z) {
            String r0 = TelephonyAdapterPrimaryDeviceJibe.LOG_TAG;
            int i2 = TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId;
            IMSLog.i(r0, i2, "notifyAutoConfigurationListener: type: " + i + ", result: " + z);
            if (i != 50 && i != 51 && i != 53 && i != 52) {
                IMSLog.i(TelephonyAdapterPrimaryDeviceJibe.LOG_TAG, TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId, "notifyAutoConfigurationListener: unknown notification type");
            } else if ((i != 50 || TelephonyAdapterPrimaryDeviceJibe.this.mIsWaitingForOtp) && ((i != 51 || TelephonyAdapterPrimaryDeviceJibe.this.mIsWaitingForMsisdn) && (i != 53 || TelephonyAdapterPrimaryDeviceJibe.this.mIsWaitingForIidToken))) {
                synchronized (TelephonyAdapterPrimaryDeviceJibe.this.mLock) {
                    RemoteCallbackList<IAutoConfigurationListener> remoteCallbackList = TelephonyAdapterPrimaryDeviceJibe.this.mAutoConfigurationListener;
                    if (remoteCallbackList == null) {
                        IMSLog.i(TelephonyAdapterPrimaryDeviceJibe.LOG_TAG, TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId, "notifyAutoConfigurationListener: mAutoConfigurationListener: empty");
                        return;
                    }
                    try {
                        int beginBroadcast = remoteCallbackList.beginBroadcast();
                        String r6 = TelephonyAdapterPrimaryDeviceJibe.LOG_TAG;
                        int i3 = TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId;
                        IMSLog.i(r6, i3, "notifyAutoConfigurationListener: listener length: " + beginBroadcast);
                        if (i == 50) {
                            IMSLog.i(TelephonyAdapterPrimaryDeviceJibe.LOG_TAG, TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId, "notifyAutoConfigurationListener: onVerificationCodeNeeded");
                            IMSLog.c(LogClass.TAPDJ_OTP_NEEDED, TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId + ",VCN,LEN:" + beginBroadcast);
                        } else if (i == 51) {
                            IMSLog.i(TelephonyAdapterPrimaryDeviceJibe.LOG_TAG, TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId, "notifyAutoConfigurationListener: onMsisdnNumberNeeded");
                            IMSLog.c(LogClass.TAPDJ_MSISDN_NEEDED, TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId + ",MNN,LEN:" + beginBroadcast);
                        } else if (i == 53) {
                            IMSLog.i(TelephonyAdapterPrimaryDeviceJibe.LOG_TAG, TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId, "notifyAutoConfigurationListener: onIidTokenNeeded");
                            IMSLog.c(LogClass.TAPDJ_IIDTOKEN_NEEDED, TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId + ",ITN,LEN:" + beginBroadcast);
                        } else {
                            IMSLog.i(TelephonyAdapterPrimaryDeviceJibe.LOG_TAG, TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId, "notifyAutoConfigurationListener: onAutoConfigurationCompleted");
                            IMSLog.c(LogClass.TAPDJ_ACS_COMPLETED, TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId + ",ACC:" + z + ",LEN:" + beginBroadcast);
                        }
                        if (beginBroadcast == 0) {
                            String r62 = TelephonyAdapterPrimaryDeviceJibe.LOG_TAG;
                            int i4 = TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId;
                            IMSLog.i(r62, i4, "Listener not registered yet. Postpone notify later: " + i);
                            if (i == 52) {
                                TelephonyAdapterPrimaryDeviceJibe.this.mPostponedNotification.clear();
                            } else {
                                TelephonyAdapterPrimaryDeviceJibe.this.mPostponedNotification.put(Integer.valueOf(i), Boolean.valueOf(z));
                            }
                        }
                        for (int i5 = 0; i5 < beginBroadcast; i5++) {
                            IAutoConfigurationListener broadcastItem = TelephonyAdapterPrimaryDeviceJibe.this.mAutoConfigurationListener.getBroadcastItem(i5);
                            if (i == 50) {
                                broadcastItem.onVerificationCodeNeeded();
                            } else if (i == 51) {
                                broadcastItem.onMsisdnNumberNeeded();
                            } else if (i == 53) {
                                broadcastItem.onIidTokenNeeded();
                            } else {
                                broadcastItem.onAutoConfigurationCompleted(z);
                                TelephonyAdapterPrimaryDeviceJibe.this.mPostponedNotification.clear();
                            }
                        }
                    } catch (RemoteException | IllegalStateException | NullPointerException e) {
                        String r12 = TelephonyAdapterPrimaryDeviceJibe.LOG_TAG;
                        int i6 = TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId;
                        IMSLog.i(r12, i6, "notifyAutoConfigurationListener: Exception: " + e.getMessage());
                    } catch (AbstractMethodError e2) {
                        String r122 = TelephonyAdapterPrimaryDeviceJibe.LOG_TAG;
                        int i7 = TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId;
                        IMSLog.i(r122, i7, "notifyAutoConfigurationListener: AbstractMethodError: " + e2.getMessage());
                    }
                    try {
                        TelephonyAdapterPrimaryDeviceJibe.this.mAutoConfigurationListener.finishBroadcast();
                    } catch (IllegalStateException e3) {
                        String r123 = TelephonyAdapterPrimaryDeviceJibe.LOG_TAG;
                        int i8 = TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId;
                        IMSLog.i(r123, i8, "notifyAutoConfigurationListener: Exception: " + e3.getMessage());
                    }
                }
            } else {
                String r11 = TelephonyAdapterPrimaryDeviceJibe.LOG_TAG;
                int i9 = TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId;
                IMSLog.i(r11, i9, "notifyAutoConfigurationListener: ignore notification type, mIsWaitingForOtp: " + TelephonyAdapterPrimaryDeviceJibe.this.mIsWaitingForOtp + " mIsWaitingForMsisdn: " + TelephonyAdapterPrimaryDeviceJibe.this.mIsWaitingForMsisdn + " mIsWaitingForIidToken: " + TelephonyAdapterPrimaryDeviceJibe.this.mIsWaitingForIidToken);
            }
        }

        public void sendVerificationCode(String str) {
            String r0 = TelephonyAdapterPrimaryDeviceJibe.LOG_TAG;
            int i = TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId;
            IMSLog.i(r0, i, "sendVerificationCode value: " + str);
            IMSLog.c(LogClass.TAPDJ_SEND_OTP, TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId + ",VC:" + str);
            TelephonyAdapterPrimaryDeviceJibe.this.removeMessages(3);
            String r02 = TelephonyAdapterPrimaryDeviceJibe.LOG_TAG;
            int i2 = TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId;
            IMSLog.i(r02, i2, "mIsWaitingForOtp: " + TelephonyAdapterPrimaryDeviceJibe.this.mIsWaitingForOtp);
            TelephonyAdapterPrimaryDeviceJibe telephonyAdapterPrimaryDeviceJibe = TelephonyAdapterPrimaryDeviceJibe.this;
            if (telephonyAdapterPrimaryDeviceJibe.mIsWaitingForOtp) {
                telephonyAdapterPrimaryDeviceJibe.mOtp = str;
                try {
                    String r5 = TelephonyAdapterPrimaryDeviceJibe.LOG_TAG;
                    int i3 = TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId;
                    IMSLog.i(r5, i3, "semaphore release with mCurrentOtpPermits: " + TelephonyAdapterPrimaryDeviceJibe.this.mCurrentOtpPermits);
                    TelephonyAdapterPrimaryDeviceJibe telephonyAdapterPrimaryDeviceJibe2 = TelephonyAdapterPrimaryDeviceJibe.this;
                    telephonyAdapterPrimaryDeviceJibe2.mOtpSemaphore.release(telephonyAdapterPrimaryDeviceJibe2.mCurrentOtpPermits);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
        }

        public void sendMsisdnNumber(String str) {
            IMSLog.i(TelephonyAdapterPrimaryDeviceJibe.LOG_TAG, TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId, "sendMsisdnNumber");
            IMSLog.c(LogClass.TAPDJ_SEND_MSISDN, TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId + ",MN");
            TelephonyAdapterPrimaryDeviceJibe.this.removeMessages(7);
            if (str == null || "".equals(str)) {
                IMSLog.i(TelephonyAdapterPrimaryDeviceJibe.LOG_TAG, TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId, "value is null or empty");
            }
            String r0 = TelephonyAdapterPrimaryDeviceJibe.LOG_TAG;
            int i = TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId;
            IMSLog.i(r0, i, "mIsWaitingForMsisdn: " + TelephonyAdapterPrimaryDeviceJibe.this.mIsWaitingForMsisdn);
            TelephonyAdapterPrimaryDeviceJibe telephonyAdapterPrimaryDeviceJibe = TelephonyAdapterPrimaryDeviceJibe.this;
            if (telephonyAdapterPrimaryDeviceJibe.mIsWaitingForMsisdn) {
                telephonyAdapterPrimaryDeviceJibe.mMsisdn = str;
                try {
                    String r5 = TelephonyAdapterPrimaryDeviceJibe.LOG_TAG;
                    int i2 = TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId;
                    IMSLog.i(r5, i2, "semaphore release with mCurrentMsisdnPermits: " + TelephonyAdapterPrimaryDeviceJibe.this.mCurrentMsisdnPermits);
                    TelephonyAdapterPrimaryDeviceJibe telephonyAdapterPrimaryDeviceJibe2 = TelephonyAdapterPrimaryDeviceJibe.this;
                    telephonyAdapterPrimaryDeviceJibe2.mMsisdnSemaphore.release(telephonyAdapterPrimaryDeviceJibe2.mCurrentMsisdnPermits);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
        }

        public void sendIidToken(String str) {
            IMSLog.i(TelephonyAdapterPrimaryDeviceJibe.LOG_TAG, TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId, "sendIidToken");
            IMSLog.c(LogClass.TAPDJ_SEND_IIDTOKEN, TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId + ",IT");
            TelephonyAdapterPrimaryDeviceJibe.this.removeMessages(15);
            if (str == null || "".equals(str)) {
                IMSLog.i(TelephonyAdapterPrimaryDeviceJibe.LOG_TAG, TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId, "value is null or empty");
            }
            String r0 = TelephonyAdapterPrimaryDeviceJibe.LOG_TAG;
            int i = TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId;
            IMSLog.i(r0, i, "mIsWaitingForIidToken: " + TelephonyAdapterPrimaryDeviceJibe.this.mIsWaitingForIidToken);
            TelephonyAdapterPrimaryDeviceJibe telephonyAdapterPrimaryDeviceJibe = TelephonyAdapterPrimaryDeviceJibe.this;
            if (telephonyAdapterPrimaryDeviceJibe.mIsWaitingForIidToken) {
                telephonyAdapterPrimaryDeviceJibe.mIidToken = str;
                try {
                    String r5 = TelephonyAdapterPrimaryDeviceJibe.LOG_TAG;
                    int i2 = TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId;
                    IMSLog.i(r5, i2, "semaphore release with mCurrentIidTokenPermits: " + TelephonyAdapterPrimaryDeviceJibe.this.mCurrentIidTokenPermits);
                    TelephonyAdapterPrimaryDeviceJibe telephonyAdapterPrimaryDeviceJibe2 = TelephonyAdapterPrimaryDeviceJibe.this;
                    telephonyAdapterPrimaryDeviceJibe2.mIidTokenSemaphore.release(telephonyAdapterPrimaryDeviceJibe2.mCurrentIidTokenPermits);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected class AbsentState extends TelephonyAdapterPrimaryDeviceBase.AbsentState {
        public String getDeviceId(int i) {
            return null;
        }

        public String getIdentityByPhoneId(int i) {
            return null;
        }

        public String getOtp() {
            return null;
        }

        protected AbsentState() {
            super();
        }

        public String getPortOtp() {
            long timeInMillis = Calendar.getInstance().getTimeInMillis();
            String r2 = TelephonyAdapterPrimaryDeviceJibe.LOG_TAG;
            IMSLog.d(r2, "CurrentTime =" + timeInMillis + ", mPortOTP = " + TelephonyAdapterPrimaryDeviceJibe.this.mPortOtpReceivedTime);
            TelephonyAdapterPrimaryDeviceJibe telephonyAdapterPrimaryDeviceJibe = TelephonyAdapterPrimaryDeviceJibe.this;
            if (telephonyAdapterPrimaryDeviceJibe.mPortOtp == null || timeInMillis >= telephonyAdapterPrimaryDeviceJibe.mPortOtpReceivedTime + RegistrationGovernor.RETRY_AFTER_PDNLOST_MS) {
                return null;
            }
            IMSLog.i(TelephonyAdapterPrimaryDeviceJibe.LOG_TAG, TelephonyAdapterPrimaryDeviceJibe.this.mPhoneId, "getPortOtp exist");
            return TelephonyAdapterPrimaryDeviceJibe.this.mPortOtp;
        }
    }

    public String getOtp() {
        return this.mState.getOtp();
    }
}
