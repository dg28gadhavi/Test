package com.sec.internal.ims.config.adapters;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import com.sec.ims.IAutoConfigurationListener;
import com.sec.ims.settings.ImsProfile;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.aec.AECNamespace;
import com.sec.internal.constants.ims.os.IccCardConstants;
import com.sec.internal.constants.ims.os.PhoneConstants;
import com.sec.internal.helper.ImsCallUtil;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.helper.os.TelephonyManagerWrapper;
import com.sec.internal.ims.core.RegistrationGovernor;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.settings.ImsProfileLoaderInternal;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.config.ITelephonyAdapter;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.log.IMSLog;
import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Semaphore;

public class TelephonyAdapterPrimaryDeviceBase extends Handler implements ITelephonyAdapter {
    protected static final int EVENT_ADS_CHANGED = 100;
    protected static final int EVENT_SIM_REMOVE_OR_REFRESH = 101;
    protected static final int HANDLE_EVENT_SIM_READY = 9;
    protected static final int HANDLE_EVENT_SIM_REMOVED = 10;
    protected static final int HANDLE_EVENT_SIM_STATE_CHANGED = 11;
    protected static final int HANDLE_GET_APP_TOKEN = 12;
    protected static final int HANDLE_GET_APP_TOKEN_TIMEOUT = 13;
    protected static final int HANDLE_GET_IIDTOKEN = 14;
    protected static final int HANDLE_GET_IIDTOKEN_TIMEOUT = 15;
    protected static final int HANDLE_GET_MSISDN = 6;
    protected static final int HANDLE_GET_MSISDN_TIMEOUT = 7;
    protected static final int HANDLE_GET_OTP = 2;
    protected static final int HANDLE_GET_OTP_TIMEOUT = 3;
    protected static final int HANDLE_GET_PORT_OTP = 4;
    protected static final int HANDLE_GET_PORT_OTP_TIMEOUT = 5;
    protected static final int HANDLE_INTENT_DATA_SMS_RECEIVED_ACTION = 1;
    protected static final int HANDLE_NOTIFY_OTP_NEEDED = 8;
    protected static final int HANDLE_SMS_CONFIGURATION_REQUEST = 0;
    /* access modifiers changed from: private */
    public static final String LOG_TAG = TelephonyAdapterPrimaryDeviceBase.class.getSimpleName();
    protected static final int NOTIFY_AUTO_CONFIGURATION_COMPLETED = 52;
    protected static final int NOTIFY_IID_TOKEN_NEEDED = 53;
    protected static final int NOTIFY_MSISDN_NUMBER_NEEDED = 51;
    protected static final int NOTIFY_VERIFICATION_CODE_NEEDED = 50;
    protected static String SMS_CONFIGURATION_REQUEST = "-rcscfg";
    protected final RemoteCallbackList<IAutoConfigurationListener> mAutoConfigurationListener = new RemoteCallbackList<>();
    protected Context mContext;
    protected int mCurrentIidTokenPermits = 0;
    protected int mCurrentMsisdnPermits = 0;
    protected int mCurrentOtpPermits = 0;
    protected int mCurrentPermits = 0;
    protected int mCurrentPortPermits = 0;
    protected String mIidToken = null;
    protected Semaphore mIidTokenSemaphore = new Semaphore(0);
    protected boolean mIsWaitingForIidToken = false;
    protected boolean mIsWaitingForMsisdn = false;
    protected boolean mIsWaitingForOtp = false;
    protected final Object mLock = new Object();
    protected Looper mLooper;
    protected IConfigModule mModule;
    protected String mMsisdn = null;
    protected Semaphore mMsisdnSemaphore = new Semaphore(0);
    protected String mOtp = null;
    protected long mOtpReceivedTime = 0;
    protected int mPhoneId;
    protected String mPortOtp = null;
    protected long mPortOtpReceivedTime = 0;
    protected Semaphore mPortOtpSemaphore = new Semaphore(0);
    protected PortSmsReceiverBase mPortSmsReceiver;
    protected Map<Integer, Boolean> mPostponedNotification;
    protected Semaphore mSemaphore = new Semaphore(0);
    protected ISimManager mSimManager;
    protected SmsReceiverBase mSmsReceiver;
    protected TelephonyAdapterState mState = null;
    protected ITelephonyManager mTelephony;

    public void registerUneregisterForOTP(boolean z) {
    }

    public TelephonyAdapterPrimaryDeviceBase(Context context, IConfigModule iConfigModule, int i) {
        super(iConfigModule.getHandler().getLooper());
        this.mContext = context;
        this.mModule = iConfigModule;
        this.mLooper = iConfigModule.getHandler().getLooper();
        this.mPhoneId = i;
        this.mTelephony = TelephonyManagerWrapper.getInstance(this.mContext);
        this.mSimManager = SimManagerFactory.getSimManagerFromSimSlot(this.mPhoneId);
        this.mPostponedNotification = new HashMap();
        getState(TelephonyAdapterState.IDLE_STATE);
        registerSimEventListener();
    }

    /* access modifiers changed from: protected */
    public void registerSimEventListener() {
        ISimManager iSimManager = this.mSimManager;
        if (iSimManager != null) {
            iSimManager.registerForSimReady(this, 9, (Object) null);
            this.mSimManager.registerForSimRemoved(this, 10, (Object) null);
            this.mSimManager.registerForSimStateChanged(this, 11, (Object) null);
        }
    }

    /* access modifiers changed from: protected */
    public void createSmsReceiver() {
        this.mSmsReceiver = new SmsReceiverBase();
    }

    /* access modifiers changed from: protected */
    public void registerSmsReceiver() {
        if (this.mModule != null) {
            createSmsReceiver();
            Context context = this.mContext;
            SmsReceiverBase smsReceiverBase = this.mSmsReceiver;
            context.registerReceiver(smsReceiverBase, smsReceiverBase.getIntentFilter());
        }
    }

    /* access modifiers changed from: protected */
    public void createPortSmsReceiver() {
        this.mPortSmsReceiver = new PortSmsReceiverBase();
    }

    /* access modifiers changed from: protected */
    public void registerPortSmsReceiver() {
        if (this.mModule != null) {
            createPortSmsReceiver();
            Context context = this.mContext;
            PortSmsReceiverBase portSmsReceiverBase = this.mPortSmsReceiver;
            context.registerReceiver(portSmsReceiverBase, portSmsReceiverBase.getIntentFilter());
        }
    }

    /* access modifiers changed from: protected */
    public void sendSmsPushForConfigRequest(boolean z) {
        Message message;
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "sendSmsPushForConfigRequest: isForceConfigRequest: " + z);
        IMSLog.c(LogClass.TAPDB_RECE_PUSHSMS, this.mPhoneId + ",RPUSH");
        Handler handler = this.mModule.getHandler();
        if (z) {
            message = obtainMessage(4, Integer.valueOf(this.mPhoneId));
        } else {
            message = obtainMessage(21, Integer.valueOf(this.mPhoneId));
        }
        handler.sendMessage(message);
    }

    /* access modifiers changed from: protected */
    public void updateOtpInfo(Message message, boolean z) {
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "updateOtpInfo: mIsWaitingForOtp: " + this.mIsWaitingForOtp + " useWaitingForOtp: " + z);
        StringBuilder sb = new StringBuilder();
        sb.append(this.mPhoneId);
        sb.append(",ROTP");
        IMSLog.c(LogClass.TAPDB_RECE_OTP, sb.toString());
        this.mOtp = (String) message.obj;
        this.mOtpReceivedTime = Calendar.getInstance().getTimeInMillis();
        if (this.mIsWaitingForOtp || !z) {
            this.mSemaphore.release();
        }
    }

    /* access modifiers changed from: protected */
    public void handleReceivedDataSms(Message message, boolean z, boolean z2) {
        Object obj = message.obj;
        if (obj == null) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "handleReceivedDataSms: no received data sms");
        } else if (((String) obj).contains(SMS_CONFIGURATION_REQUEST)) {
            sendSmsPushForConfigRequest(z);
        } else {
            updateOtpInfo(message, z2);
        }
    }

    /* access modifiers changed from: protected */
    public void handleOtpTimeout(boolean z) {
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "handleOtpTimeout: useWaitingForOtpFlag: " + z);
        IMSLog.c(LogClass.TAPDB_OTP_TIMEOUT, this.mPhoneId + ",TOTP");
        removeMessages(3);
        this.mOtp = null;
        this.mOtpReceivedTime = 0;
        if (this.mIsWaitingForOtp || !z) {
            this.mSemaphore.release();
        }
    }

    public void handleMessage(Message message) {
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "message:" + message.what);
        int i2 = message.what;
        if (i2 == 1) {
            handleReceivedDataSms(message, true, true);
        } else if (i2 != 3) {
            switch (i2) {
                case 9:
                    int i3 = this.mPhoneId;
                    IMSLog.i(str, i3, "SIM_READY, Current state: " + this.mState.getClass().getSimpleName());
                    if (!(this.mState instanceof ReadyState)) {
                        getState(TelephonyAdapterState.READY_STATE);
                        return;
                    }
                    return;
                case 10:
                    int i4 = this.mPhoneId;
                    IMSLog.i(str, i4, "SIM_REMOVED, Current state: " + this.mState.getClass().getSimpleName());
                    if (!(this.mState instanceof AbsentState)) {
                        getState(TelephonyAdapterState.ABSENT_STATE);
                        return;
                    }
                    return;
                case 11:
                    int i5 = this.mPhoneId;
                    IMSLog.i(str, i5, "SIM_STATE_CHANGED, Current state: " + this.mState.getClass().getSimpleName());
                    int simState = this.mTelephony.getSimState();
                    String telephonyProperty = this.mTelephony.getTelephonyProperty(this.mPhoneId, ImsConstants.SystemProperties.SIM_STATE, "UNKNOWN");
                    int simSlotPriority = SimUtil.getSimSlotPriority();
                    int i6 = this.mPhoneId;
                    IMSLog.i(str, i6, "sim state:" + simState + ", icc state:" + telephonyProperty);
                    int i7 = this.mPhoneId;
                    if (i7 != simSlotPriority) {
                        IMSLog.i(str, i7, "Omit no default sim event. phoneId = " + this.mPhoneId + " default_phoneId = " + simSlotPriority);
                        return;
                    } else if (IccCardConstants.INTENT_VALUE_ICC_LOADED.equals(telephonyProperty)) {
                        if (this.mState instanceof IdleState) {
                            getState(TelephonyAdapterState.READY_STATE);
                            return;
                        }
                        return;
                    } else if (1 == simState) {
                        if (this.mState instanceof IdleState) {
                            getState(TelephonyAdapterState.ABSENT_STATE);
                            return;
                        }
                        return;
                    } else if (!"IMSI".equals(telephonyProperty)) {
                        TelephonyAdapterState telephonyAdapterState = this.mState;
                        if ((telephonyAdapterState instanceof ReadyState) || (telephonyAdapterState instanceof AbsentState)) {
                            getState(TelephonyAdapterState.IDLE_STATE);
                            return;
                        }
                        return;
                    } else {
                        return;
                    }
                default:
                    return;
            }
        } else {
            handleOtpTimeout(true);
        }
    }

    protected abstract class SmsReceiver extends BroadcastReceiver {
        protected IntentFilter mIntentFilter = null;

        /* access modifiers changed from: protected */
        public abstract void readMessageFromSMSIntent(Intent intent);

        public SmsReceiver() {
            IntentFilter intentFilter = new IntentFilter();
            this.mIntentFilter = intentFilter;
            intentFilter.addAction(AECNamespace.Action.RECEIVED_SMS_NOTIFICATION);
            this.mIntentFilter.addDataScheme("sms");
            this.mIntentFilter.addDataAuthority("localhost", TelephonyAdapterState.SMS_DEST_PORT);
        }

        public void onReceive(Context context, Intent intent) {
            if (AECNamespace.Action.RECEIVED_SMS_NOTIFICATION.equals(intent.getAction())) {
                try {
                    readMessageFromSMSIntent(intent);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        }

        public IntentFilter getIntentFilter() {
            return this.mIntentFilter;
        }
    }

    protected class PortSmsReceiverBase extends SmsReceiver {
        public PortSmsReceiverBase() {
            super();
            IMSLog.i(TelephonyAdapterPrimaryDeviceBase.LOG_TAG, TelephonyAdapterPrimaryDeviceBase.this.mPhoneId, "PortSmsReceiverBase");
        }

        /* access modifiers changed from: protected */
        public void readMessageFromSMSIntent(Intent intent) {
            SmsMessage smsMessage;
            SmsMessage[] messagesFromIntent = Telephony.Sms.Intents.getMessagesFromIntent(intent);
            IMSLog.i(TelephonyAdapterPrimaryDeviceBase.LOG_TAG, TelephonyAdapterPrimaryDeviceBase.this.mPhoneId, "readMessageFromSMSIntent: enter");
            if (messagesFromIntent != null && (smsMessage = messagesFromIntent[0]) != null) {
                String displayMessageBody = smsMessage.getDisplayMessageBody();
                int slotId = SimManagerFactory.getSlotId(intent.getIntExtra(PhoneConstants.SUBSCRIPTION_KEY, -1));
                if (displayMessageBody == null) {
                    displayMessageBody = new String(smsMessage.getUserData(), Charset.forName("UTF-16"));
                }
                TelephonyAdapterPrimaryDeviceBase telephonyAdapterPrimaryDeviceBase = TelephonyAdapterPrimaryDeviceBase.this;
                if (telephonyAdapterPrimaryDeviceBase.mPhoneId == slotId) {
                    telephonyAdapterPrimaryDeviceBase.sendMessage(telephonyAdapterPrimaryDeviceBase.obtainMessage(4, slotId, 0, displayMessageBody));
                }
            }
        }
    }

    protected class SmsReceiverBase extends SmsReceiver {
        public SmsReceiverBase() {
            super();
            IMSLog.i(TelephonyAdapterPrimaryDeviceBase.LOG_TAG, TelephonyAdapterPrimaryDeviceBase.this.mPhoneId, "SmsReceiverBase");
        }

        /* access modifiers changed from: protected */
        public void readMessageFromSMSIntent(Intent intent) {
            SmsMessage smsMessage;
            SmsMessage[] messagesFromIntent = Telephony.Sms.Intents.getMessagesFromIntent(intent);
            IMSLog.i(TelephonyAdapterPrimaryDeviceBase.LOG_TAG, TelephonyAdapterPrimaryDeviceBase.this.mPhoneId, "readMessageFromSMSIntent: enter");
            if (messagesFromIntent != null && (smsMessage = messagesFromIntent[0]) != null) {
                int slotId = SimManagerFactory.getSlotId(intent.getIntExtra(PhoneConstants.SUBSCRIPTION_KEY, -1));
                String displayMessageBody = smsMessage.getDisplayMessageBody();
                if (displayMessageBody == null) {
                    displayMessageBody = new String(smsMessage.getUserData(), Charset.forName("UTF-16"));
                }
                Message obtainMessage = TelephonyAdapterPrimaryDeviceBase.this.obtainMessage();
                obtainMessage.what = 1;
                obtainMessage.arg1 = slotId;
                obtainMessage.obj = displayMessageBody;
                TelephonyAdapterPrimaryDeviceBase telephonyAdapterPrimaryDeviceBase = TelephonyAdapterPrimaryDeviceBase.this;
                if (telephonyAdapterPrimaryDeviceBase.mPhoneId == slotId) {
                    telephonyAdapterPrimaryDeviceBase.sendMessage(obtainMessage);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void initState() {
        int simState = this.mTelephony.getSimState(this.mPhoneId);
        if (5 == simState) {
            if (TextUtils.isEmpty(this.mTelephony.getSubscriberId(SimUtil.getSubId(this.mPhoneId)))) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "simState is ready but imsi is empty");
                getState(TelephonyAdapterState.IDLE_STATE);
                return;
            }
            IMSLog.i(LOG_TAG, this.mPhoneId, "simState is ready and imsi is existed");
            getState(TelephonyAdapterState.READY_STATE);
        } else if (1 == simState) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "simState is absent");
            getState(TelephonyAdapterState.ABSENT_STATE);
        } else {
            IMSLog.i(LOG_TAG, this.mPhoneId, "simState is not ready");
            getState(TelephonyAdapterState.IDLE_STATE);
        }
    }

    /* access modifiers changed from: protected */
    public void getState(String str) {
        String str2 = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str2, i, "getState: change to " + str);
        if (TelephonyAdapterState.IDLE_STATE.equals(str)) {
            this.mState = new IdleState();
        } else if (TelephonyAdapterState.READY_STATE.equals(str)) {
            this.mState = new ReadyState();
        } else if (TelephonyAdapterState.ABSENT_STATE.equals(str)) {
            this.mState = new AbsentState();
        }
    }

    protected class IdleState extends TelephonyAdapterState {
        public IdleState() {
            IMSLog.i(TelephonyAdapterPrimaryDeviceBase.LOG_TAG, TelephonyAdapterPrimaryDeviceBase.this.mPhoneId, "idle state");
        }
    }

    protected class ReadyState extends TelephonyAdapterState {
        public String getSipUri() {
            return "";
        }

        public boolean isReady() {
            return true;
        }

        public ReadyState() {
            IMSLog.i(TelephonyAdapterPrimaryDeviceBase.LOG_TAG, TelephonyAdapterPrimaryDeviceBase.this.mPhoneId, "ready state");
        }

        public String getPrimaryIdentity() {
            String str;
            IMSLog.i(TelephonyAdapterPrimaryDeviceBase.LOG_TAG, TelephonyAdapterPrimaryDeviceBase.this.mPhoneId, "getPrimaryIdentity()");
            if (!TextUtils.isEmpty(getImsi())) {
                str = "IMSI_" + getImsi();
            } else if (!TextUtils.isEmpty(getMsisdn())) {
                str = "MSISDN_" + getMsisdn();
            } else {
                IMSLog.i(TelephonyAdapterPrimaryDeviceBase.LOG_TAG, TelephonyAdapterPrimaryDeviceBase.this.mPhoneId, "identity error");
                str = "";
            }
            return str.replaceAll("[\\W]", "");
        }

        public String getMcc() {
            ISimManager iSimManager = TelephonyAdapterPrimaryDeviceBase.this.mSimManager;
            String simOperator = iSimManager != null ? iSimManager.getSimOperator() : "";
            if (TextUtils.isEmpty(simOperator)) {
                IMSLog.i(TelephonyAdapterPrimaryDeviceBase.LOG_TAG, TelephonyAdapterPrimaryDeviceBase.this.mPhoneId, "MCC sim operator: empty");
                return simOperator;
            }
            try {
                return simOperator.substring(0, 3);
            } catch (IndexOutOfBoundsException unused) {
                String r1 = TelephonyAdapterPrimaryDeviceBase.LOG_TAG;
                int i = TelephonyAdapterPrimaryDeviceBase.this.mPhoneId;
                IMSLog.i(r1, i, "sim operator:" + simOperator);
                return simOperator;
            }
        }

        public String getMnc() {
            ISimManager iSimManager = TelephonyAdapterPrimaryDeviceBase.this.mSimManager;
            String simOperator = iSimManager != null ? iSimManager.getSimOperator() : "";
            if (TextUtils.isEmpty(simOperator)) {
                IMSLog.i(TelephonyAdapterPrimaryDeviceBase.LOG_TAG, TelephonyAdapterPrimaryDeviceBase.this.mPhoneId, "MNC sim operator: empty");
                return simOperator;
            }
            try {
                if (simOperator.length() > 5) {
                    return simOperator.substring(3, 6);
                }
                return "0" + simOperator.substring(3, 5);
            } catch (IndexOutOfBoundsException unused) {
                String r1 = TelephonyAdapterPrimaryDeviceBase.LOG_TAG;
                int i = TelephonyAdapterPrimaryDeviceBase.this.mPhoneId;
                IMSLog.i(r1, i, "sim operator:" + simOperator);
                return simOperator;
            }
        }

        public String getImsi() {
            TelephonyAdapterPrimaryDeviceBase telephonyAdapterPrimaryDeviceBase = TelephonyAdapterPrimaryDeviceBase.this;
            if (telephonyAdapterPrimaryDeviceBase.mTelephony.getSubscriberId(SimUtil.getSubId(telephonyAdapterPrimaryDeviceBase.mPhoneId)) == null) {
                IMSLog.i(TelephonyAdapterPrimaryDeviceBase.LOG_TAG, TelephonyAdapterPrimaryDeviceBase.this.mPhoneId, "imsi error");
                return "";
            }
            TelephonyAdapterPrimaryDeviceBase telephonyAdapterPrimaryDeviceBase2 = TelephonyAdapterPrimaryDeviceBase.this;
            return telephonyAdapterPrimaryDeviceBase2.mTelephony.getSubscriberId(SimUtil.getSubId(telephonyAdapterPrimaryDeviceBase2.mPhoneId));
        }

        public String getImei() {
            TelephonyAdapterPrimaryDeviceBase telephonyAdapterPrimaryDeviceBase = TelephonyAdapterPrimaryDeviceBase.this;
            if (telephonyAdapterPrimaryDeviceBase.mTelephony.getImei(telephonyAdapterPrimaryDeviceBase.mPhoneId) == null) {
                IMSLog.i(TelephonyAdapterPrimaryDeviceBase.LOG_TAG, TelephonyAdapterPrimaryDeviceBase.this.mPhoneId, "imei error");
                return "";
            }
            TelephonyAdapterPrimaryDeviceBase telephonyAdapterPrimaryDeviceBase2 = TelephonyAdapterPrimaryDeviceBase.this;
            return telephonyAdapterPrimaryDeviceBase2.mTelephony.getImei(telephonyAdapterPrimaryDeviceBase2.mPhoneId);
        }

        public String getMsisdn() {
            int subId = SimUtil.getSubId(TelephonyAdapterPrimaryDeviceBase.this.mPhoneId);
            String msisdn = TelephonyAdapterPrimaryDeviceBase.this.mTelephony.getMsisdn(subId);
            if (TextUtils.isEmpty(msisdn)) {
                msisdn = TelephonyAdapterPrimaryDeviceBase.this.mTelephony.getLine1Number(subId);
                if (TextUtils.isEmpty(msisdn)) {
                    IMSLog.i(TelephonyAdapterPrimaryDeviceBase.LOG_TAG, TelephonyAdapterPrimaryDeviceBase.this.mPhoneId, "MSISDN doesn't exist");
                    msisdn = "";
                }
            }
            return ImsCallUtil.validatePhoneNumber(msisdn, getSimCountryCode());
        }

        public String getSimCountryCode() {
            return TelephonyAdapterPrimaryDeviceBase.this.mTelephony.getSimCountryIso().toUpperCase(Locale.ENGLISH);
        }

        public String getNetType() {
            return TelephonyAdapterPrimaryDeviceBase.this.mTelephony.getNetworkType() == 13 ? "LTE" : "3G";
        }

        public String getOtp() {
            long timeInMillis = Calendar.getInstance().getTimeInMillis();
            TelephonyAdapterPrimaryDeviceBase telephonyAdapterPrimaryDeviceBase = TelephonyAdapterPrimaryDeviceBase.this;
            if (telephonyAdapterPrimaryDeviceBase.mOtp == null || timeInMillis >= telephonyAdapterPrimaryDeviceBase.mOtpReceivedTime + RegistrationGovernor.RETRY_AFTER_PDNLOST_MS) {
                IMSLog.i(TelephonyAdapterPrimaryDeviceBase.LOG_TAG, TelephonyAdapterPrimaryDeviceBase.this.mPhoneId, "OTP don't exist. wait OTP");
                IMSLog.c(LogClass.TAPDB_WAIT_OTP, TelephonyAdapterPrimaryDeviceBase.this.mPhoneId + ",WOTP");
                TelephonyAdapterPrimaryDeviceBase telephonyAdapterPrimaryDeviceBase2 = TelephonyAdapterPrimaryDeviceBase.this;
                telephonyAdapterPrimaryDeviceBase2.mIsWaitingForOtp = true;
                try {
                    telephonyAdapterPrimaryDeviceBase2.mSemaphore.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (Throwable th) {
                    TelephonyAdapterPrimaryDeviceBase.this.mIsWaitingForOtp = false;
                    throw th;
                }
                TelephonyAdapterPrimaryDeviceBase.this.mIsWaitingForOtp = false;
                TelephonyAdapterPrimaryDeviceBase.this.removeMessages(3);
                String r0 = TelephonyAdapterPrimaryDeviceBase.LOG_TAG;
                int i = TelephonyAdapterPrimaryDeviceBase.this.mPhoneId;
                IMSLog.i(r0, i, "receive OTP: " + IMSLog.checker(TelephonyAdapterPrimaryDeviceBase.this.mOtp));
            } else {
                IMSLog.i(TelephonyAdapterPrimaryDeviceBase.LOG_TAG, TelephonyAdapterPrimaryDeviceBase.this.mPhoneId, "OTP exist. send immediately");
            }
            return TelephonyAdapterPrimaryDeviceBase.this.mOtp;
        }

        public String getIdentityByPhoneId(int i) {
            return ConfigUtil.buildIdentity(TelephonyAdapterPrimaryDeviceBase.this.mContext, i);
        }

        public String getSubscriberId(int i) {
            return TelephonyAdapterPrimaryDeviceBase.this.mTelephony.getSubscriberId(i);
        }

        public String getMsisdn(int i) {
            return TelephonyAdapterPrimaryDeviceBase.this.mTelephony.getMsisdn(i);
        }

        public String getDeviceId(int i) {
            return TelephonyAdapterPrimaryDeviceBase.this.mTelephony.getImei(i);
        }
    }

    protected class AbsentState extends TelephonyAdapterState {
        ImsProfile mImsProfile = null;

        public String getImsi() {
            return "";
        }

        public String getMsisdn() {
            return "";
        }

        public String getSimCountryCode() {
            return "";
        }

        public String getSipUri() {
            return "";
        }

        public boolean isReady() {
            return true;
        }

        public AbsentState() {
            IMSLog.i(TelephonyAdapterPrimaryDeviceBase.LOG_TAG, TelephonyAdapterPrimaryDeviceBase.this.mPhoneId, "absent state");
            List<ImsProfile> profileList = ImsProfileLoaderInternal.getProfileList(TelephonyAdapterPrimaryDeviceBase.this.mContext, TelephonyAdapterPrimaryDeviceBase.this.mPhoneId);
            if (profileList == null || profileList.size() <= 0 || profileList.get(0) == null) {
                IMSLog.i(TelephonyAdapterPrimaryDeviceBase.LOG_TAG, TelephonyAdapterPrimaryDeviceBase.this.mPhoneId, "AbsentState : no ImsProfile loaded");
            } else {
                this.mImsProfile = profileList.get(0);
            }
        }

        public String getPrimaryIdentity() {
            String str;
            if (!TextUtils.isEmpty(getImsi())) {
                str = "IMSI_" + getImsi();
            } else if (!TextUtils.isEmpty(getMsisdn())) {
                str = "MSISDN_" + getMsisdn();
            } else if (!TextUtils.isEmpty(getImei())) {
                str = "IMEI_" + getImei();
            } else {
                IMSLog.i(TelephonyAdapterPrimaryDeviceBase.LOG_TAG, TelephonyAdapterPrimaryDeviceBase.this.mPhoneId, "identity error");
                str = "";
            }
            return str.replaceAll("[\\W]", "");
        }

        public String getMcc() {
            ImsProfile imsProfile = this.mImsProfile;
            return imsProfile != null ? imsProfile.getDefaultMcc() : "450";
        }

        public String getMnc() {
            ImsProfile imsProfile = this.mImsProfile;
            if (imsProfile != null) {
                return imsProfile.getDefaultMnc();
            }
            try {
                return "0" + "01";
            } catch (IndexOutOfBoundsException unused) {
                String r0 = TelephonyAdapterPrimaryDeviceBase.LOG_TAG;
                int i = TelephonyAdapterPrimaryDeviceBase.this.mPhoneId;
                IMSLog.i(r0, i, "sim operator:" + "45001");
                return "45001";
            }
        }

        public String getImei() {
            if (TelephonyAdapterPrimaryDeviceBase.this.mTelephony.getImei() != null) {
                return TelephonyAdapterPrimaryDeviceBase.this.mTelephony.getImei();
            }
            IMSLog.i(TelephonyAdapterPrimaryDeviceBase.LOG_TAG, TelephonyAdapterPrimaryDeviceBase.this.mPhoneId, "imei error");
            return "";
        }

        public String getNetType() {
            return TelephonyAdapterPrimaryDeviceBase.this.mTelephony.getNetworkType() == 13 ? "LTE" : "3G";
        }

        public String getOtp() {
            long timeInMillis = Calendar.getInstance().getTimeInMillis();
            TelephonyAdapterPrimaryDeviceBase telephonyAdapterPrimaryDeviceBase = TelephonyAdapterPrimaryDeviceBase.this;
            if (telephonyAdapterPrimaryDeviceBase.mOtp == null || timeInMillis >= telephonyAdapterPrimaryDeviceBase.mOtpReceivedTime + RegistrationGovernor.RETRY_AFTER_PDNLOST_MS) {
                IMSLog.i(TelephonyAdapterPrimaryDeviceBase.LOG_TAG, TelephonyAdapterPrimaryDeviceBase.this.mPhoneId, "OTP don't exist. wait OTP");
                TelephonyAdapterPrimaryDeviceBase telephonyAdapterPrimaryDeviceBase2 = TelephonyAdapterPrimaryDeviceBase.this;
                telephonyAdapterPrimaryDeviceBase2.mIsWaitingForOtp = true;
                try {
                    telephonyAdapterPrimaryDeviceBase2.mSemaphore.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (Throwable th) {
                    TelephonyAdapterPrimaryDeviceBase.this.mIsWaitingForOtp = false;
                    throw th;
                }
                TelephonyAdapterPrimaryDeviceBase.this.mIsWaitingForOtp = false;
                String r0 = TelephonyAdapterPrimaryDeviceBase.LOG_TAG;
                int i = TelephonyAdapterPrimaryDeviceBase.this.mPhoneId;
                IMSLog.i(r0, i, "receive OTP:" + IMSLog.checker(TelephonyAdapterPrimaryDeviceBase.this.mOtp));
            } else {
                IMSLog.i(TelephonyAdapterPrimaryDeviceBase.LOG_TAG, TelephonyAdapterPrimaryDeviceBase.this.mPhoneId, "OTP exist. send immediately");
            }
            return TelephonyAdapterPrimaryDeviceBase.this.mOtp;
        }

        public String getIdentityByPhoneId(int i) {
            return ConfigUtil.buildIdentity(TelephonyAdapterPrimaryDeviceBase.this.mContext, i);
        }

        public String getDeviceId(int i) {
            return TelephonyAdapterPrimaryDeviceBase.this.mTelephony.getImei(i);
        }
    }

    public boolean isReady() {
        if (this.mState instanceof IdleState) {
            initState();
        }
        return this.mState.isReady();
    }

    public String getPrimaryIdentity() {
        return this.mState.getPrimaryIdentity();
    }

    public String getMcc() {
        return this.mState.getMcc();
    }

    public String getMnc() {
        return this.mState.getMnc();
    }

    public String getImsi() {
        return this.mState.getImsi();
    }

    public String getImei() {
        return this.mState.getImei();
    }

    public String getSimCountryCode() {
        return this.mState.getSimCountryCode();
    }

    public String getMsisdn() {
        return this.mState.getMsisdn();
    }

    public String getSipUri() {
        return this.mState.getSipUri();
    }

    public String getNetType() {
        return this.mState.getNetType();
    }

    public String getSmsDestPort() {
        return this.mState.getSmsDestPort();
    }

    public String getSmsOrigPort() {
        return this.mState.getSmsOrigPort();
    }

    public String getExistingOtp() {
        return this.mState.getExistingOtp();
    }

    public String getExistingPortOtp() {
        return this.mState.getExistingPortOtp();
    }

    public String getOtp() {
        sendMessageDelayed(obtainMessage(3), 300000);
        return this.mState.getOtp();
    }

    public String getPortOtp() {
        return this.mState.getPortOtp();
    }

    public String getMsisdnNumber() {
        return this.mState.getMsisdnNumber();
    }

    public String getIidToken() {
        return this.mState.getIidToken();
    }

    public String getAppToken(boolean z) {
        return this.mState.getAppToken(z);
    }

    public void registerAutoConfigurationListener(IAutoConfigurationListener iAutoConfigurationListener) {
        this.mState.registerAutoConfigurationListener(iAutoConfigurationListener);
    }

    public void unregisterAutoConfigurationListener(IAutoConfigurationListener iAutoConfigurationListener) {
        this.mState.unregisterAutoConfigurationListener(iAutoConfigurationListener);
    }

    public void notifyAutoConfigurationListener(int i, boolean z) {
        this.mState.notifyAutoConfigurationListener(i, z);
    }

    public void sendVerificationCode(String str) {
        this.mState.sendVerificationCode(str);
    }

    public void sendMsisdnNumber(String str) {
        this.mState.sendMsisdnNumber(str);
    }

    public void onADSChanged() {
        this.mState.onADSChanged();
    }

    public void sendIidToken(String str) {
        this.mState.sendIidToken(str);
    }

    public String getIdentityByPhoneId(int i) {
        return this.mState.getIdentityByPhoneId(i);
    }

    public String getSubscriberId(int i) {
        return this.mState.getSubscriberId(i);
    }

    public String getMsisdn(int i) {
        return this.mState.getMsisdn(i);
    }

    public String getDeviceId(int i) {
        return this.mState.getDeviceId(i);
    }

    public void cleanup() {
        String str = LOG_TAG;
        IMSLog.i(str, this.mPhoneId, "cleanup");
        if (!(this.mModule == null || this.mSmsReceiver == null)) {
            IMSLog.i(str, this.mPhoneId, "unregister mSmsReceiver");
            this.mContext.unregisterReceiver(this.mSmsReceiver);
            this.mSmsReceiver = null;
        }
        if (!(this.mModule == null || this.mPortSmsReceiver == null)) {
            IMSLog.i(str, this.mPhoneId, "unregister mPortSmsReceiver");
            this.mContext.unregisterReceiver(this.mPortSmsReceiver);
            this.mPortSmsReceiver = null;
        }
        if (this.mSimManager != null) {
            IMSLog.i(str, this.mPhoneId, "deregister SimReady/SimRemoved/SimStateChanged");
            this.mSimManager.deregisterForSimReady(this);
            this.mSimManager.deregisterForSimRemoved(this);
            this.mSimManager.deregisterForSimStateChanged(this);
        }
        this.mState.cleanup();
    }
}
