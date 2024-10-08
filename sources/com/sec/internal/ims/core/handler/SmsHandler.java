package com.sec.internal.ims.core.handler;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.sec.internal.ims.servicemodules.sms.ISmsServiceInterface;

public class SmsHandler extends BaseHandler implements ISmsServiceInterface {
    public void registerForRrcConnectionEvent(Handler handler, int i, Object obj) {
    }

    public void registerForSMSEvent(Handler handler, int i, Object obj) {
    }

    public void sendMessage(String str, String str2, String str3, byte[] bArr, boolean z, String str4, int i, int i2, boolean z2) {
    }

    public void sendSMSResponse(int i, String str, int i2) {
    }

    public void setMsgAppInfoToSipUa(int i, String str) {
    }

    protected SmsHandler(Looper looper) {
        super(looper);
    }

    public void handleMessage(Message message) {
        int i = message.what;
        String str = this.LOG_TAG;
        Log.e(str, "Unknown event " + message.what);
    }
}
