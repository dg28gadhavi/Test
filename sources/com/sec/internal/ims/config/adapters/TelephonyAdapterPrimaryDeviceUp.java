package com.sec.internal.ims.config.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import com.sec.ims.IAutoConfigurationListener;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.os.PhoneConstants;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceBase;
import com.sec.internal.ims.core.RegistrationGovernor;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.log.IMSLog;
import java.nio.charset.Charset;
import java.util.Calendar;

public class TelephonyAdapterPrimaryDeviceUp extends TelephonyAdapterPrimaryDeviceBase {
    /* access modifiers changed from: private */
    public static final String LOG_TAG = TelephonyAdapterPrimaryDeviceUp.class.getSimpleName();

    public TelephonyAdapterPrimaryDeviceUp(Context context, IConfigModule iConfigModule, int i) {
        super(context, iConfigModule, i);
        registerSmsReceiver();
        registerPortSmsReceiver();
        initState();
    }

    /* access modifiers changed from: protected */
    public void createSmsReceiver() {
        this.mSmsReceiver = new SmsReceiver();
    }

    /* access modifiers changed from: protected */
    public void createPortSmsReceiver() {
        this.mPortSmsReceiver = new PortSmsReceiver();
    }

    public void handleMessage(Message message) {
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "message:" + message.what);
        int i2 = message.what;
        if (i2 == 3) {
            this.mOtp = null;
            try {
                int i3 = this.mPhoneId;
                IMSLog.i(str, i3, "semaphore release with mCurrentPermits: " + this.mCurrentPermits);
                this.mSemaphore.release(this.mCurrentPermits);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        } else if (i2 == 4) {
            IMSLog.i(str, this.mPhoneId, "receive port sms");
            if (message.obj != null) {
                Mno simMno = SimUtil.getSimMno(this.mPhoneId);
                ISimManager simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(this.mPhoneId);
                if (!((String) message.obj).contains(TelephonyAdapterPrimaryDeviceBase.SMS_CONFIGURATION_REQUEST)) {
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
                    } catch (IllegalArgumentException e2) {
                        e2.printStackTrace();
                    }
                } else if (!simMno.isEmeasewaoce() || ConfigUtil.isRcsAvailable(this.mContext, this.mPhoneId, simManagerFromSimSlot)) {
                    IMSLog.i(str, this.mPhoneId, "request force configuration");
                    this.mModule.getHandler().sendMessage(obtainMessage(4, Integer.valueOf(this.mPhoneId)));
                } else {
                    IMSLog.i(str, this.mPhoneId, "RCS service is disabled(Default app is set as others or RCS switch turned off)");
                }
            } else {
                IMSLog.i(str, this.mPhoneId, "no SMS data!");
            }
        } else if (i2 == 5) {
            this.mPortOtp = null;
            try {
                int i6 = this.mPhoneId;
                IMSLog.i(str, i6, "otp timeout: semaphore release with mCurrentPortPermits: " + this.mCurrentPortPermits);
                this.mPortOtpSemaphore.release(this.mCurrentPortPermits);
                this.mCurrentPortPermits = 0;
            } catch (IllegalArgumentException e3) {
                e3.printStackTrace();
            }
        } else if (i2 != 8) {
            super.handleMessage(message);
        } else if (this.mIsWaitingForOtp) {
            notifyAutoConfigurationListener(50, true);
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

        public String getSmsDestPort() {
            return TelephonyAdapterState.SMS_DEST_PORT;
        }

        public String getSmsOrigPort() {
            return TelephonyAdapterState.SMS_ORIG_PORT;
        }

        public String getExistingOtp() {
            TelephonyAdapterPrimaryDeviceUp.this.mIsWaitingForOtp = false;
            long timeInMillis = Calendar.getInstance().getTimeInMillis();
            TelephonyAdapterPrimaryDeviceUp telephonyAdapterPrimaryDeviceUp = TelephonyAdapterPrimaryDeviceUp.this;
            if (telephonyAdapterPrimaryDeviceUp.mOtp == null || timeInMillis >= telephonyAdapterPrimaryDeviceUp.mOtpReceivedTime + RegistrationGovernor.RETRY_AFTER_PDNLOST_MS) {
                return null;
            }
            IMSLog.i(TelephonyAdapterPrimaryDeviceUp.LOG_TAG, TelephonyAdapterPrimaryDeviceUp.this.mPhoneId, "getExistingOtp exist");
            return TelephonyAdapterPrimaryDeviceUp.this.mOtp;
        }

        public String getExistingPortOtp() {
            long timeInMillis = Calendar.getInstance().getTimeInMillis();
            TelephonyAdapterPrimaryDeviceUp telephonyAdapterPrimaryDeviceUp = TelephonyAdapterPrimaryDeviceUp.this;
            if (telephonyAdapterPrimaryDeviceUp.mPortOtp == null || timeInMillis >= telephonyAdapterPrimaryDeviceUp.mPortOtpReceivedTime + RegistrationGovernor.RETRY_AFTER_PDNLOST_MS) {
                return null;
            }
            IMSLog.i(TelephonyAdapterPrimaryDeviceUp.LOG_TAG, TelephonyAdapterPrimaryDeviceUp.this.mPhoneId, "getExistingPortOtp exist");
            return TelephonyAdapterPrimaryDeviceUp.this.mPortOtp;
        }

        public String getOtp() {
            IMSLog.i(TelephonyAdapterPrimaryDeviceUp.LOG_TAG, TelephonyAdapterPrimaryDeviceUp.this.mPhoneId, "getOtp");
            TelephonyAdapterPrimaryDeviceUp telephonyAdapterPrimaryDeviceUp = TelephonyAdapterPrimaryDeviceUp.this;
            telephonyAdapterPrimaryDeviceUp.mCurrentPermits = 0;
            telephonyAdapterPrimaryDeviceUp.mIsWaitingForOtp = true;
            telephonyAdapterPrimaryDeviceUp.sendMessageDelayed(telephonyAdapterPrimaryDeviceUp.obtainMessage(8), 300);
            TelephonyAdapterPrimaryDeviceUp telephonyAdapterPrimaryDeviceUp2 = TelephonyAdapterPrimaryDeviceUp.this;
            telephonyAdapterPrimaryDeviceUp2.sendMessageDelayed(telephonyAdapterPrimaryDeviceUp2.obtainMessage(3), 310000);
            try {
                TelephonyAdapterPrimaryDeviceUp telephonyAdapterPrimaryDeviceUp3 = TelephonyAdapterPrimaryDeviceUp.this;
                telephonyAdapterPrimaryDeviceUp3.mCurrentPermits = telephonyAdapterPrimaryDeviceUp3.mSemaphore.availablePermits() + 1;
                String r0 = TelephonyAdapterPrimaryDeviceUp.LOG_TAG;
                int i = TelephonyAdapterPrimaryDeviceUp.this.mPhoneId;
                IMSLog.i(r0, i, "semaphore acquire with mCurrentPermits: " + TelephonyAdapterPrimaryDeviceUp.this.mCurrentPermits);
                TelephonyAdapterPrimaryDeviceUp telephonyAdapterPrimaryDeviceUp4 = TelephonyAdapterPrimaryDeviceUp.this;
                telephonyAdapterPrimaryDeviceUp4.mSemaphore.acquire(telephonyAdapterPrimaryDeviceUp4.mCurrentPermits);
            } catch (IllegalArgumentException | InterruptedException e) {
                TelephonyAdapterPrimaryDeviceUp.this.mIsWaitingForOtp = false;
                e.printStackTrace();
            }
            TelephonyAdapterPrimaryDeviceUp telephonyAdapterPrimaryDeviceUp5 = TelephonyAdapterPrimaryDeviceUp.this;
            telephonyAdapterPrimaryDeviceUp5.mIsWaitingForOtp = false;
            return telephonyAdapterPrimaryDeviceUp5.mOtp;
        }

        public String getPortOtp() {
            IMSLog.i(TelephonyAdapterPrimaryDeviceUp.LOG_TAG, TelephonyAdapterPrimaryDeviceUp.this.mPhoneId, "getPortOtp");
            TelephonyAdapterPrimaryDeviceUp telephonyAdapterPrimaryDeviceUp = TelephonyAdapterPrimaryDeviceUp.this;
            telephonyAdapterPrimaryDeviceUp.mCurrentPortPermits = 0;
            telephonyAdapterPrimaryDeviceUp.sendMessageDelayed(telephonyAdapterPrimaryDeviceUp.obtainMessage(5), 300000);
            try {
                TelephonyAdapterPrimaryDeviceUp telephonyAdapterPrimaryDeviceUp2 = TelephonyAdapterPrimaryDeviceUp.this;
                telephonyAdapterPrimaryDeviceUp2.mCurrentPortPermits = telephonyAdapterPrimaryDeviceUp2.mPortOtpSemaphore.availablePermits() + 1;
                String r0 = TelephonyAdapterPrimaryDeviceUp.LOG_TAG;
                int i = TelephonyAdapterPrimaryDeviceUp.this.mPhoneId;
                IMSLog.i(r0, i, "getPortOtp: semaphore acquire with mCurrentPortPermits: " + TelephonyAdapterPrimaryDeviceUp.this.mCurrentPortPermits);
                TelephonyAdapterPrimaryDeviceUp telephonyAdapterPrimaryDeviceUp3 = TelephonyAdapterPrimaryDeviceUp.this;
                telephonyAdapterPrimaryDeviceUp3.mPortOtpSemaphore.acquire(telephonyAdapterPrimaryDeviceUp3.mCurrentPortPermits);
            } catch (IllegalArgumentException | InterruptedException e) {
                e.printStackTrace();
            }
            return TelephonyAdapterPrimaryDeviceUp.this.mPortOtp;
        }

        public void registerAutoConfigurationListener(IAutoConfigurationListener iAutoConfigurationListener) {
            if (iAutoConfigurationListener == null) {
                IMSLog.i(TelephonyAdapterPrimaryDeviceUp.LOG_TAG, TelephonyAdapterPrimaryDeviceUp.this.mPhoneId, "listener: null");
                return;
            }
            synchronized (TelephonyAdapterPrimaryDeviceUp.this.mLock) {
                if (TelephonyAdapterPrimaryDeviceUp.this.mAutoConfigurationListener != null) {
                    String r1 = TelephonyAdapterPrimaryDeviceUp.LOG_TAG;
                    int i = TelephonyAdapterPrimaryDeviceUp.this.mPhoneId;
                    IMSLog.i(r1, i, "register listener: " + iAutoConfigurationListener);
                    IMSLog.c(LogClass.TAPDU_LISTNER, TelephonyAdapterPrimaryDeviceUp.this.mPhoneId + "," + iAutoConfigurationListener);
                    TelephonyAdapterPrimaryDeviceUp.this.mAutoConfigurationListener.register(iAutoConfigurationListener);
                    if (!TelephonyAdapterPrimaryDeviceUp.this.mPostponedNotification.isEmpty()) {
                        for (Integer intValue : TelephonyAdapterPrimaryDeviceUp.this.mPostponedNotification.keySet()) {
                            int intValue2 = intValue.intValue();
                            notifyAutoConfigurationListener(intValue2, TelephonyAdapterPrimaryDeviceUp.this.mPostponedNotification.get(Integer.valueOf(intValue2)).booleanValue());
                        }
                    }
                }
            }
        }

        public void unregisterAutoConfigurationListener(IAutoConfigurationListener iAutoConfigurationListener) {
            if (iAutoConfigurationListener == null) {
                IMSLog.i(TelephonyAdapterPrimaryDeviceUp.LOG_TAG, TelephonyAdapterPrimaryDeviceUp.this.mPhoneId, "listener: null");
                return;
            }
            synchronized (TelephonyAdapterPrimaryDeviceUp.this.mLock) {
                if (TelephonyAdapterPrimaryDeviceUp.this.mAutoConfigurationListener != null) {
                    String r1 = TelephonyAdapterPrimaryDeviceUp.LOG_TAG;
                    int i = TelephonyAdapterPrimaryDeviceUp.this.mPhoneId;
                    IMSLog.i(r1, i, "unregister listener: " + iAutoConfigurationListener);
                    TelephonyAdapterPrimaryDeviceUp.this.mAutoConfigurationListener.unregister(iAutoConfigurationListener);
                    TelephonyAdapterPrimaryDeviceUp.this.mPostponedNotification.clear();
                }
            }
        }

        public void notifyAutoConfigurationListener(int i, boolean z) {
            String r0 = TelephonyAdapterPrimaryDeviceUp.LOG_TAG;
            int i2 = TelephonyAdapterPrimaryDeviceUp.this.mPhoneId;
            IMSLog.i(r0, i2, "notifyAutoConfigurationListener: type: " + i + ", result: " + z);
            if (i != 50 && i != 52) {
                IMSLog.i(TelephonyAdapterPrimaryDeviceUp.LOG_TAG, TelephonyAdapterPrimaryDeviceUp.this.mPhoneId, "unknown notification type");
                return;
            } else if (i != 50 || TelephonyAdapterPrimaryDeviceUp.this.mIsWaitingForOtp) {
                synchronized (TelephonyAdapterPrimaryDeviceUp.this.mLock) {
                    RemoteCallbackList<IAutoConfigurationListener> remoteCallbackList = TelephonyAdapterPrimaryDeviceUp.this.mAutoConfigurationListener;
                    if (remoteCallbackList == null) {
                        IMSLog.i(TelephonyAdapterPrimaryDeviceUp.LOG_TAG, TelephonyAdapterPrimaryDeviceUp.this.mPhoneId, "mAutoConfigurationListener: empty");
                        return;
                    }
                    try {
                        int beginBroadcast = remoteCallbackList.beginBroadcast();
                        String r4 = TelephonyAdapterPrimaryDeviceUp.LOG_TAG;
                        int i3 = TelephonyAdapterPrimaryDeviceUp.this.mPhoneId;
                        IMSLog.i(r4, i3, "listener length: " + beginBroadcast);
                        if (i == 50) {
                            IMSLog.i(TelephonyAdapterPrimaryDeviceUp.LOG_TAG, TelephonyAdapterPrimaryDeviceUp.this.mPhoneId, "onVerificationCodeNeeded");
                            IMSLog.c(LogClass.TAPDU_OTP_NEEDED, TelephonyAdapterPrimaryDeviceUp.this.mPhoneId + ",VCN,LEN:" + beginBroadcast);
                        } else {
                            String r42 = TelephonyAdapterPrimaryDeviceUp.LOG_TAG;
                            int i4 = TelephonyAdapterPrimaryDeviceUp.this.mPhoneId;
                            IMSLog.i(r42, i4, "onAutoConfigurationCompleted, result: " + z);
                            IMSLog.c(LogClass.TAPDU_ACS_RESULT, TelephonyAdapterPrimaryDeviceUp.this.mPhoneId + ",ACS:" + z + ",LEN:" + beginBroadcast);
                        }
                        if (beginBroadcast == 0) {
                            String r43 = TelephonyAdapterPrimaryDeviceUp.LOG_TAG;
                            int i5 = TelephonyAdapterPrimaryDeviceUp.this.mPhoneId;
                            IMSLog.i(r43, i5, "Listener not registered yet. Postpone notify later: " + i);
                            if (i == 52) {
                                TelephonyAdapterPrimaryDeviceUp.this.mPostponedNotification.clear();
                            }
                            TelephonyAdapterPrimaryDeviceUp.this.mPostponedNotification.put(Integer.valueOf(i), Boolean.valueOf(z));
                        }
                        for (int i6 = 0; i6 < beginBroadcast; i6++) {
                            IAutoConfigurationListener broadcastItem = TelephonyAdapterPrimaryDeviceUp.this.mAutoConfigurationListener.getBroadcastItem(i6);
                            if (i == 50) {
                                broadcastItem.onVerificationCodeNeeded();
                            } else {
                                broadcastItem.onAutoConfigurationCompleted(z);
                                TelephonyAdapterPrimaryDeviceUp.this.mPostponedNotification.clear();
                            }
                        }
                    } catch (RemoteException | IllegalStateException | NullPointerException e) {
                        String r10 = TelephonyAdapterPrimaryDeviceUp.LOG_TAG;
                        int i7 = TelephonyAdapterPrimaryDeviceUp.this.mPhoneId;
                        IMSLog.i(r10, i7, "beginBroadcast Exception: " + e.getMessage());
                    }
                    try {
                        TelephonyAdapterPrimaryDeviceUp.this.mAutoConfigurationListener.finishBroadcast();
                    } catch (IllegalStateException e2) {
                        String r102 = TelephonyAdapterPrimaryDeviceUp.LOG_TAG;
                        int i8 = TelephonyAdapterPrimaryDeviceUp.this.mPhoneId;
                        IMSLog.i(r102, i8, "finishBroadcast Exception: " + e2.getMessage());
                    }
                }
            } else {
                String r9 = TelephonyAdapterPrimaryDeviceUp.LOG_TAG;
                int i9 = TelephonyAdapterPrimaryDeviceUp.this.mPhoneId;
                IMSLog.i(r9, i9, "ignore notification type, mIsWaitingForOtp: " + TelephonyAdapterPrimaryDeviceUp.this.mIsWaitingForOtp);
                return;
            }
        }

        public void sendVerificationCode(String str) {
            String r0 = TelephonyAdapterPrimaryDeviceUp.LOG_TAG;
            int i = TelephonyAdapterPrimaryDeviceUp.this.mPhoneId;
            IMSLog.i(r0, i, "sendVerificationCode value: " + str);
            IMSLog.c(LogClass.TAPDU_SEND_OTP, TelephonyAdapterPrimaryDeviceUp.this.mPhoneId + ",VC:" + str);
            TelephonyAdapterPrimaryDeviceUp.this.removeMessages(3);
            String r02 = TelephonyAdapterPrimaryDeviceUp.LOG_TAG;
            int i2 = TelephonyAdapterPrimaryDeviceUp.this.mPhoneId;
            IMSLog.i(r02, i2, "mIsWaitingForOtp: " + TelephonyAdapterPrimaryDeviceUp.this.mIsWaitingForOtp);
            TelephonyAdapterPrimaryDeviceUp telephonyAdapterPrimaryDeviceUp = TelephonyAdapterPrimaryDeviceUp.this;
            if (telephonyAdapterPrimaryDeviceUp.mIsWaitingForOtp) {
                telephonyAdapterPrimaryDeviceUp.mOtp = str;
                if (str != null) {
                    telephonyAdapterPrimaryDeviceUp.mOtpReceivedTime = Calendar.getInstance().getTimeInMillis();
                }
                try {
                    String r5 = TelephonyAdapterPrimaryDeviceUp.LOG_TAG;
                    int i3 = TelephonyAdapterPrimaryDeviceUp.this.mPhoneId;
                    IMSLog.i(r5, i3, "semaphore release with mCurrentPermits: " + TelephonyAdapterPrimaryDeviceUp.this.mCurrentPermits);
                    TelephonyAdapterPrimaryDeviceUp telephonyAdapterPrimaryDeviceUp2 = TelephonyAdapterPrimaryDeviceUp.this;
                    telephonyAdapterPrimaryDeviceUp2.mSemaphore.release(telephonyAdapterPrimaryDeviceUp2.mCurrentPermits);
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

        protected AbsentState() {
            super();
        }

        public String getSmsDestPort() {
            return TelephonyAdapterState.SMS_DEST_PORT;
        }

        public String getSmsOrigPort() {
            return TelephonyAdapterState.SMS_ORIG_PORT;
        }

        public String getExistingOtp() {
            long timeInMillis = Calendar.getInstance().getTimeInMillis();
            TelephonyAdapterPrimaryDeviceUp telephonyAdapterPrimaryDeviceUp = TelephonyAdapterPrimaryDeviceUp.this;
            if (telephonyAdapterPrimaryDeviceUp.mOtp == null || timeInMillis >= telephonyAdapterPrimaryDeviceUp.mOtpReceivedTime + RegistrationGovernor.RETRY_AFTER_PDNLOST_MS) {
                return null;
            }
            IMSLog.i(TelephonyAdapterPrimaryDeviceUp.LOG_TAG, TelephonyAdapterPrimaryDeviceUp.this.mPhoneId, "getExistingOtp exist");
            return TelephonyAdapterPrimaryDeviceUp.this.mOtp;
        }

        public String getExistingPortOtp() {
            long timeInMillis = Calendar.getInstance().getTimeInMillis();
            TelephonyAdapterPrimaryDeviceUp telephonyAdapterPrimaryDeviceUp = TelephonyAdapterPrimaryDeviceUp.this;
            if (telephonyAdapterPrimaryDeviceUp.mPortOtp == null || timeInMillis >= telephonyAdapterPrimaryDeviceUp.mPortOtpReceivedTime + RegistrationGovernor.RETRY_AFTER_PDNLOST_MS) {
                return null;
            }
            IMSLog.i(TelephonyAdapterPrimaryDeviceUp.LOG_TAG, TelephonyAdapterPrimaryDeviceUp.this.mPhoneId, "getExistingPortOtp exist");
            return TelephonyAdapterPrimaryDeviceUp.this.mPortOtp;
        }

        public String getOtp() {
            long timeInMillis = Calendar.getInstance().getTimeInMillis();
            TelephonyAdapterPrimaryDeviceUp telephonyAdapterPrimaryDeviceUp = TelephonyAdapterPrimaryDeviceUp.this;
            if (telephonyAdapterPrimaryDeviceUp.mOtp == null || timeInMillis >= telephonyAdapterPrimaryDeviceUp.mOtpReceivedTime + RegistrationGovernor.RETRY_AFTER_PDNLOST_MS) {
                return null;
            }
            IMSLog.i(TelephonyAdapterPrimaryDeviceUp.LOG_TAG, TelephonyAdapterPrimaryDeviceUp.this.mPhoneId, "getOtp exist");
            return TelephonyAdapterPrimaryDeviceUp.this.mOtp;
        }

        public String getPortOtp() {
            long timeInMillis = Calendar.getInstance().getTimeInMillis();
            TelephonyAdapterPrimaryDeviceUp telephonyAdapterPrimaryDeviceUp = TelephonyAdapterPrimaryDeviceUp.this;
            if (telephonyAdapterPrimaryDeviceUp.mPortOtp == null || timeInMillis >= telephonyAdapterPrimaryDeviceUp.mPortOtpReceivedTime + RegistrationGovernor.RETRY_AFTER_PDNLOST_MS) {
                return null;
            }
            IMSLog.i(TelephonyAdapterPrimaryDeviceUp.LOG_TAG, TelephonyAdapterPrimaryDeviceUp.this.mPhoneId, "getPortOtp exist");
            return TelephonyAdapterPrimaryDeviceUp.this.mPortOtp;
        }
    }

    private class SmsReceiver extends TelephonyAdapterPrimaryDeviceBase.SmsReceiverBase {
        private static final String SMS_OTP_FORMAT_IOT_SERVER = "your messenger verification code is";
        private static final String SMS_OTP_FORMAT_PROD_SERVER = "messenger's enhanced features have been enabled";
        private static final String SMS_OTP_NEW_FORMAT_GOOGLE_SERVER = "confirmation id";
        private static final String SMS_OTP_NEW_FORMAT_GOOGLE_SERVER_AMX = "activation code is";
        private static final String SMS_OTP_NEW_FORMAT_NEWPACE_SERVER = "the verification code for new messaging features";
        private static final String SMS_OTP_OLD_FORMAT_NEWPACE_SERVER = "here is your krypton code. please be aware that this code expires after 15 minutes then re-authentication might be needed";
        private static final String SMS_RECEIVED_ACTION = "android.provider.Telephony.SMS_RECEIVED";

        public SmsReceiver() {
            super();
            IMSLog.i(TelephonyAdapterPrimaryDeviceUp.LOG_TAG, TelephonyAdapterPrimaryDeviceUp.this.mPhoneId, "SmsReceiver");
            IntentFilter intentFilter = new IntentFilter();
            this.mIntentFilter = intentFilter;
            intentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        }

        public void onReceive(Context context, Intent intent) {
            IMSLog.i(TelephonyAdapterPrimaryDeviceUp.LOG_TAG, TelephonyAdapterPrimaryDeviceUp.this.mPhoneId, "onReceive");
            if (!intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
                IMSLog.i(TelephonyAdapterPrimaryDeviceUp.LOG_TAG, TelephonyAdapterPrimaryDeviceUp.this.mPhoneId, "invalid intent");
                return;
            }
            StringBuilder sb = new StringBuilder();
            Object[] objArr = (Object[]) intent.getExtras().get("pdus");
            if (objArr == null) {
                IMSLog.i(TelephonyAdapterPrimaryDeviceUp.LOG_TAG, TelephonyAdapterPrimaryDeviceUp.this.mPhoneId, "invalid pdus");
                return;
            }
            String stringExtra = intent.getStringExtra("format");
            if (TextUtils.isEmpty(stringExtra)) {
                IMSLog.i(TelephonyAdapterPrimaryDeviceUp.LOG_TAG, TelephonyAdapterPrimaryDeviceUp.this.mPhoneId, "invalid format");
                return;
            }
            int length = objArr.length;
            SmsMessage[] smsMessageArr = new SmsMessage[length];
            for (int i = 0; i < objArr.length; i++) {
                smsMessageArr[i] = SmsMessage.createFromPdu((byte[]) objArr[i], stringExtra);
            }
            for (int i2 = 0; i2 < length; i2++) {
                sb.append(smsMessageArr[i2].getDisplayMessageBody());
            }
            String sb2 = sb.toString();
            if (TextUtils.isEmpty(sb2)) {
                IMSLog.i(TelephonyAdapterPrimaryDeviceUp.LOG_TAG, TelephonyAdapterPrimaryDeviceUp.this.mPhoneId, "invalid smsBody");
                return;
            }
            IMSLog.i(TelephonyAdapterPrimaryDeviceUp.LOG_TAG, TelephonyAdapterPrimaryDeviceUp.this.mPhoneId, "smsBody: " + IMSLog.checker(sb2));
            String parseOtp = parseOtp(sb2);
            if (parseOtp == null) {
                IMSLog.i(TelephonyAdapterPrimaryDeviceUp.LOG_TAG, TelephonyAdapterPrimaryDeviceUp.this.mPhoneId, "failed to parse smsBody, wait for next one");
                return;
            }
            TelephonyAdapterPrimaryDeviceUp telephonyAdapterPrimaryDeviceUp = TelephonyAdapterPrimaryDeviceUp.this;
            if (telephonyAdapterPrimaryDeviceUp.mIsWaitingForOtp) {
                telephonyAdapterPrimaryDeviceUp.mOtp = parseOtp;
                IMSLog.i(TelephonyAdapterPrimaryDeviceUp.LOG_TAG, TelephonyAdapterPrimaryDeviceUp.this.mPhoneId, "mOtp: " + IMSLog.checker(TelephonyAdapterPrimaryDeviceUp.this.mOtp));
                TelephonyAdapterPrimaryDeviceUp.this.mOtpReceivedTime = Calendar.getInstance().getTimeInMillis();
            }
        }

        private String parseOtp(String str) {
            String str2;
            int length = str.length();
            if (str.toLowerCase().contains(SMS_OTP_FORMAT_PROD_SERVER) || str.toLowerCase().contains(SMS_OTP_FORMAT_IOT_SERVER)) {
                StringBuffer stringBuffer = new StringBuffer();
                for (int i = 0; i < length; i++) {
                    char charAt = str.charAt(i);
                    if (charAt >= '0' && charAt <= '9') {
                        stringBuffer.append(charAt);
                    }
                }
                str2 = stringBuffer.toString();
            } else if (str.toLowerCase().contains(SMS_OTP_NEW_FORMAT_GOOGLE_SERVER)) {
                StringBuffer stringBuffer2 = new StringBuffer();
                int indexOf = str.indexOf(58);
                while (indexOf > 0 && indexOf < length && indexOf < str.indexOf(41)) {
                    stringBuffer2.append(str.charAt(indexOf));
                    indexOf++;
                }
                str2 = stringBuffer2.toString();
            } else if (str.toLowerCase().contains(SMS_OTP_NEW_FORMAT_GOOGLE_SERVER_AMX)) {
                StringBuffer stringBuffer3 = new StringBuffer();
                int indexOf2 = str.indexOf(40);
                while (true) {
                    indexOf2++;
                    if (indexOf2 <= 0 || indexOf2 >= length || indexOf2 >= str.indexOf(41)) {
                        str2 = stringBuffer3.toString();
                    } else {
                        stringBuffer3.append(str.charAt(indexOf2));
                    }
                }
                str2 = stringBuffer3.toString();
            } else if (str.toLowerCase().contains(SMS_OTP_OLD_FORMAT_NEWPACE_SERVER) || str.toLowerCase().contains(SMS_OTP_NEW_FORMAT_NEWPACE_SERVER)) {
                StringBuffer stringBuffer4 = new StringBuffer();
                int indexOf3 = str.indexOf(58);
                while (indexOf3 > 0 && indexOf3 < length) {
                    char charAt2 = str.charAt(indexOf3);
                    if (charAt2 >= '0' && charAt2 <= '9') {
                        stringBuffer4.append(charAt2);
                    }
                    indexOf3++;
                }
                str2 = stringBuffer4.toString();
            } else {
                str2 = null;
            }
            String r0 = TelephonyAdapterPrimaryDeviceUp.LOG_TAG;
            int i2 = TelephonyAdapterPrimaryDeviceUp.this.mPhoneId;
            IMSLog.i(r0, i2, "parseOtp: " + IMSLog.checker(str2));
            return str2;
        }
    }

    private class PortSmsReceiver extends TelephonyAdapterPrimaryDeviceBase.PortSmsReceiverBase {
        private PortSmsReceiver() {
            super();
        }

        /* access modifiers changed from: protected */
        public void readMessageFromSMSIntent(Intent intent) {
            SmsMessage smsMessage;
            SmsMessage[] messagesFromIntent = Telephony.Sms.Intents.getMessagesFromIntent(intent);
            IMSLog.i(TelephonyAdapterPrimaryDeviceUp.LOG_TAG, TelephonyAdapterPrimaryDeviceUp.this.mPhoneId, "readMessageFromSMSIntent: enter");
            if (messagesFromIntent != null && (smsMessage = messagesFromIntent[0]) != null) {
                int slotId = SimManagerFactory.getSlotId(intent.getIntExtra(PhoneConstants.SUBSCRIPTION_KEY, -1));
                String displayMessageBody = smsMessage.getDisplayMessageBody();
                if (displayMessageBody == null) {
                    displayMessageBody = new String(smsMessage.getUserData(), Charset.forName("UTF-16"));
                }
                Message obtainMessage = TelephonyAdapterPrimaryDeviceUp.this.obtainMessage();
                obtainMessage.what = 4;
                obtainMessage.arg1 = slotId;
                obtainMessage.obj = displayMessageBody;
                TelephonyAdapterPrimaryDeviceUp telephonyAdapterPrimaryDeviceUp = TelephonyAdapterPrimaryDeviceUp.this;
                if (telephonyAdapterPrimaryDeviceUp.mPhoneId == slotId) {
                    telephonyAdapterPrimaryDeviceUp.sendMessage(obtainMessage);
                }
            }
        }
    }

    public String getOtp() {
        return this.mState.getOtp();
    }
}
