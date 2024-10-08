package com.samsung.android.cmcp2phelper.transport;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import com.samsung.android.cmcp2phelper.MdmnServiceInfo;
import com.samsung.android.cmcp2phelper.data.CphMessage;
import com.samsung.android.cmcp2phelper.transport.internal.CphUnicastReceiver;
import com.samsung.android.cmcp2phelper.transport.internal.CphUnicastSender;
import com.samsung.android.cmcp2phelper.utils.P2pUtils;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CphManager {
    public static final String LOG_TAG = ("cmcp2phelper/1.3.06/" + CphManager.class.getSimpleName());
    private static final ExecutorService executor = Executors.newFixedThreadPool(16);
    Context mContext;
    Handler mLogHandler;
    int mMessageId;
    MdmnServiceInfo mServiceInfo;
    CphUnicastReceiver mUnicastReceiver;
    CphUnicastReceiver mUnicastReceiver2;

    public CphManager(Context context, MdmnServiceInfo mdmnServiceInfo) {
        this.mContext = context;
        this.mServiceInfo = mdmnServiceInfo;
    }

    public static void execute(Runnable runnable) {
        try {
            executor.execute(runnable);
        } catch (Exception unused) {
        }
    }

    public void startReceive() {
        Log.i(LOG_TAG, "start NSD");
        if (this.mUnicastReceiver == null) {
            CphUnicastReceiver cphUnicastReceiver = new CphUnicastReceiver(this.mContext, this.mLogHandler, 51024, this.mServiceInfo);
            this.mUnicastReceiver = cphUnicastReceiver;
            cphUnicastReceiver.enableApplicationLog(this.mLogHandler);
            execute(this.mUnicastReceiver);
        }
        if (this.mUnicastReceiver2 == null) {
            CphUnicastReceiver cphUnicastReceiver2 = new CphUnicastReceiver(this.mContext, this.mLogHandler, 52024, this.mServiceInfo);
            this.mUnicastReceiver2 = cphUnicastReceiver2;
            cphUnicastReceiver2.enableApplicationLog(this.mLogHandler);
            execute(this.mUnicastReceiver2);
        }
    }

    public void stopReceive() {
        Log.i(LOG_TAG, "stop NSD");
        CphUnicastReceiver cphUnicastReceiver = this.mUnicastReceiver;
        if (cphUnicastReceiver != null) {
            cphUnicastReceiver.stop();
            this.mUnicastReceiver = null;
        }
        CphUnicastReceiver cphUnicastReceiver2 = this.mUnicastReceiver2;
        if (cphUnicastReceiver2 != null) {
            cphUnicastReceiver2.stop();
            this.mUnicastReceiver2 = null;
        }
    }

    public void startDiscoveryUnicast(Handler handler, int i, String str, String str2, ArrayList<String> arrayList) {
        CphMessage cphMessage = new CphMessage(1, 2.0d, str, str2, P2pUtils.getLocalIpAddress(this.mContext), 51024, getNextMessageID());
        Iterator<String> it = arrayList.iterator();
        while (it.hasNext()) {
            CphUnicastSender cphUnicastSender = new CphUnicastSender(it.next(), 51024, cphMessage.getByte(), cphMessage.getByte().length, handler, i);
            cphUnicastSender.enableApplicationLog(this.mLogHandler);
            execute(cphUnicastSender);
        }
        CphMessage cphMessage2 = new CphMessage(1, 2.0d, str, str2, P2pUtils.getLocalIpAddress(this.mContext), 52024, getNextMessageID());
        Iterator<String> it2 = arrayList.iterator();
        while (it2.hasNext()) {
            CphUnicastSender cphUnicastSender2 = new CphUnicastSender(it2.next(), 52024, cphMessage2.getByte(), cphMessage2.getByte().length, handler, i);
            cphUnicastSender2.enableApplicationLog(this.mLogHandler);
            execute(cphUnicastSender2);
        }
    }

    private int getNextMessageID() {
        int i = this.mMessageId;
        if (Integer.MAX_VALUE > i) {
            this.mMessageId = i + 1;
        } else {
            this.mMessageId = 1;
        }
        return this.mMessageId;
    }
}
