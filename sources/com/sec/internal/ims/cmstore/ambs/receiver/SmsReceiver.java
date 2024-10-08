package com.sec.internal.ims.cmstore.ambs.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.sec.ims.util.IMSLog;
import com.sec.internal.constants.ims.os.PhoneConstants;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.RetryStackAdapterHelper;
import com.sec.internal.ims.cmstore.ambs.cloudrequest.ReqZCode;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;

public class SmsReceiver extends BroadcastReceiver {
    private String TAG = SmsReceiver.class.getSimpleName();
    IAPICallFlowListener mListener;
    private final MessageStoreClient mStoreClient;

    public SmsReceiver(IAPICallFlowListener iAPICallFlowListener, MessageStoreClient messageStoreClient) {
        this.TAG += "[" + messageStoreClient.getClientID() + "]";
        this.mStoreClient = messageStoreClient;
        this.mListener = iAPICallFlowListener;
    }

    public void onReceive(Context context, Intent intent) {
        Log.v(this.TAG, ">>>>>>>onReceive start");
        Bundle extras = intent.getExtras();
        if (intent.getAction().equals(com.sec.internal.ims.servicemodules.im.SmsReceiver.SMS_RECEIVED) && extras != null) {
            StringBuilder sb = new StringBuilder();
            Object[] objArr = (Object[]) extras.get("pdus");
            if (objArr == null || objArr.length == 0) {
                Log.d(this.TAG, "invalid pdus");
                return;
            }
            int length = objArr.length;
            SmsMessage[] smsMessageArr = new SmsMessage[length];
            String str = ((TelephonyManager) context.getSystemService(PhoneConstants.PHONE_KEY)).getPhoneType() == 2 ? com.sec.internal.constants.ims.servicemodules.sms.SmsMessage.FORMAT_3GPP2 : com.sec.internal.constants.ims.servicemodules.sms.SmsMessage.FORMAT_3GPP;
            for (int i = 0; i < objArr.length; i++) {
                smsMessageArr[i] = SmsMessage.createFromPdu((byte[]) objArr[i], str);
            }
            SmsMessage smsMessage = smsMessageArr[0];
            String displayOriginatingAddress = smsMessage != null ? smsMessage.getDisplayOriginatingAddress() : "";
            for (int i2 = 0; i2 < length; i2++) {
                sb.append(smsMessageArr[i2].getDisplayMessageBody());
            }
            if (sb.length() == 0 || displayOriginatingAddress.isEmpty()) {
                Log.e(this.TAG, "invalid message data");
                return;
            }
            String sb2 = sb.toString();
            int i3 = extras.getInt(PhoneConstants.PHONE_KEY, 0);
            if (i3 == this.mStoreClient.getClientID()) {
                checkAndHandleZCode(sb2, displayOriginatingAddress);
            } else {
                Log.i(this.TAG, "ignore this sms message, phoneId:" + i3 + ", mStoreClientId:" + this.mStoreClient.getClientID() + ", currentNum:" + IMSLog.checker(this.mStoreClient.getPrerenceManager().getUserCtn()));
            }
        }
        Log.v(this.TAG, ">>>>>>>onReceive end");
    }

    private void checkAndHandleZCode(String str, String str2) {
        if (ReqZCode.isSmsZCode(str, str2)) {
            ReqZCode.handleSmsZCode(str, this.mListener, new RetryStackAdapterHelper(), this.mStoreClient);
        }
    }
}
