package com.sec.internal.ims.servicemodules.im;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.os.PhoneConstants;
import com.sec.internal.helper.SimUtil;

public class SmsReceiver extends BroadcastReceiver {
    public static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    private String TAG = SmsReceiver.class.getSimpleName();
    private ImLatchingProcessor mModule;
    private int mPhoneId;

    public SmsReceiver(ImLatchingProcessor imLatchingProcessor) {
        this.mModule = imLatchingProcessor;
        this.mPhoneId = SimUtil.getActiveDataPhoneId();
    }

    public void onReceive(Context context, Intent intent) {
        Bundle extras;
        if (SMS_RECEIVED.equals(intent.getAction()) && (extras = intent.getExtras()) != null) {
            int i = extras.getInt(PhoneConstants.PHONE_KEY, this.mPhoneId);
            SmsMessage smsMessage = Telephony.Sms.Intents.getMessagesFromIntent(intent)[0];
            String originatingAddress = smsMessage.getOriginatingAddress();
            long timestampMillis = smsMessage.getTimestampMillis();
            String str = this.TAG;
            Log.d(str, "origNum - " + originatingAddress + ", smsTime - " + timestampMillis);
            ImLatchingProcessor imLatchingProcessor = this.mModule;
            StringBuilder sb = new StringBuilder();
            sb.append("tel:");
            sb.append(originatingAddress);
            this.mModule.processForResolvingLatchingStatus(imLatchingProcessor.normalizeUri(i, ImsUri.parse(sb.toString())), timestampMillis, i);
        }
    }
}
