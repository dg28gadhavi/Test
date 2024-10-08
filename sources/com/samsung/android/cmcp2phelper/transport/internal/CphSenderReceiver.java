package com.samsung.android.cmcp2phelper.transport.internal;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.samsung.android.cmcp2phelper.MdmnServiceInfo;
import com.samsung.android.cmcp2phelper.data.CphDeviceManager;
import com.samsung.android.cmcp2phelper.data.CphMessage;
import com.samsung.android.cmcp2phelper.transport.CphManager;

public class CphSenderReceiver implements Runnable {
    public static final String LOG_TAG = ("cmcp2phelper/1.3.06/" + CphSenderReceiver.class.getSimpleName());
    protected Handler mCallbackHandler;
    protected int mCallbackWhat;
    protected Context mContext;
    protected int mLength;
    protected String mLocalBindingIP;
    protected Handler mLogHandler;
    protected byte[] mMessage;
    protected int mPort;
    protected MdmnServiceInfo mServiceInfo;
    protected String mTargetIP;

    public void run() {
    }

    public void print(String str) {
        Log.i(LOG_TAG, str);
        Handler handler = this.mLogHandler;
        if (handler != null) {
            Message obtainMessage = handler.obtainMessage();
            obtainMessage.obj = System.currentTimeMillis() + " : " + str;
            this.mLogHandler.sendMessage(obtainMessage);
        }
    }

    public void enableApplicationLog(Handler handler) {
        this.mLogHandler = handler;
    }

    /* access modifiers changed from: protected */
    public void handleReceivedMessage(CphMessage cphMessage) {
        if (!cphMessage.isValid()) {
            Log.i(LOG_TAG, "invalid message");
        } else if (!this.mServiceInfo.getLineId().equalsIgnoreCase(cphMessage.getLineId())) {
            Log.i(LOG_TAG, "Line id not matched with received lineid");
        } else if (cphMessage.getMsgType() == 1) {
            if (this.mLocalBindingIP.equalsIgnoreCase(cphMessage.getResponderIP())) {
                CphDeviceManager.addToCache(cphMessage);
                String str = LOG_TAG;
                Log.d(str, "local binding ip(" + this.mLocalBindingIP + ") equals with response ip(" + cphMessage.getResponderIP() + "), DO NOT NEED TO RESPONSE");
                return;
            }
            CphMessage cphMessage2 = new CphMessage(2, 2.0d, this.mServiceInfo.getDeviceId(), this.mServiceInfo.getLineId(), cphMessage.getMessageId());
            CphUnicastSender cphUnicastSender = new CphUnicastSender(cphMessage.getResponderIP(), cphMessage.getResponderPort(), cphMessage2.getByte(), cphMessage2.getByte().length, 1);
            Handler handler = this.mLogHandler;
            if (handler != null) {
                cphUnicastSender.enableApplicationLog(handler);
            }
            CphManager.execute(cphUnicastSender);
            CphDeviceManager.addToCache(cphMessage);
        } else if (cphMessage.getMsgType() == 2) {
            CphDeviceManager.addToCache(cphMessage);
        }
    }
}
