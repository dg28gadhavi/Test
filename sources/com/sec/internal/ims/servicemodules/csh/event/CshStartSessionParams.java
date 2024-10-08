package com.sec.internal.ims.servicemodules.csh.event;

import android.os.Message;
import com.sec.internal.log.IMSLog;

public class CshStartSessionParams {
    public Message mCallback;
    public String mReceiver;

    public CshStartSessionParams(String str, Message message) {
        this.mReceiver = str;
        this.mCallback = message;
    }

    public String toString() {
        return "Receiver : " + IMSLog.checker(this.mReceiver) + " Callback : " + this.mCallback;
    }
}
