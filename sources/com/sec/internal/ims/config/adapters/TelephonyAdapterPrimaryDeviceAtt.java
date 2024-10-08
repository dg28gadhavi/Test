package com.sec.internal.ims.config.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Message;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceBase;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.log.IMSLog;
import java.nio.charset.Charset;

public class TelephonyAdapterPrimaryDeviceAtt extends TelephonyAdapterPrimaryDeviceBase {
    /* access modifiers changed from: private */
    public static final String LOG_TAG = TelephonyAdapterPrimaryDeviceAtt.class.getSimpleName();

    public TelephonyAdapterPrimaryDeviceAtt(Context context, IConfigModule iConfigModule, int i) {
        super(context, iConfigModule, i);
        registerSmsReceiver();
        initState();
    }

    /* access modifiers changed from: protected */
    public void createSmsReceiver() {
        this.mSmsReceiver = new SmsReceiver();
    }

    /* access modifiers changed from: protected */
    public void sendSmsPushForConfigRequest(boolean z) {
        sendEmptyMessage(3);
        super.sendSmsPushForConfigRequest(z);
    }

    public void handleMessage(Message message) {
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "message:" + message.what);
        if (message.what != 1) {
            super.handleMessage(message);
        } else {
            handleReceivedDataSms(message, false, true);
        }
    }

    public String getOtp() {
        sendMessageDelayed(obtainMessage(3), 1200000);
        return this.mState.getOtp();
    }

    protected class SmsReceiver extends TelephonyAdapterPrimaryDeviceBase.SmsReceiverBase {
        protected SmsReceiver() {
            super();
        }

        /* access modifiers changed from: protected */
        public void readMessageFromSMSIntent(Intent intent) {
            SmsMessage smsMessage;
            SmsMessage[] messagesFromIntent = Telephony.Sms.Intents.getMessagesFromIntent(intent);
            IMSLog.i(TelephonyAdapterPrimaryDeviceAtt.LOG_TAG, TelephonyAdapterPrimaryDeviceAtt.this.mPhoneId, "readMessageFromSMSIntent: enter");
            if (messagesFromIntent != null && (smsMessage = messagesFromIntent[0]) != null) {
                String displayMessageBody = smsMessage.getDisplayMessageBody();
                if (displayMessageBody == null) {
                    displayMessageBody = new String(smsMessage.getUserData(), Charset.forName("UTF-16"));
                }
                TelephonyAdapterPrimaryDeviceAtt telephonyAdapterPrimaryDeviceAtt = TelephonyAdapterPrimaryDeviceAtt.this;
                telephonyAdapterPrimaryDeviceAtt.sendMessage(telephonyAdapterPrimaryDeviceAtt.obtainMessage(1, displayMessageBody));
            }
        }
    }
}
