package com.sec.internal.ims.core.handler.secims;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.PreciseAlarmManager;
import com.sec.internal.helper.RegistrantList;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.core.handler.MiscHandler;
import com.sec.internal.ims.core.handler.secims.StackIF;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.EcholocateMsg;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.EcholocateMsg_.EcholocateRtpMsg;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.EcholocateMsg_.EcholocateSignalMsg;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.XqMessage;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.XqMessage_.XqContent;
import com.sec.internal.ims.servicemodules.volte2.data.EcholocateEvent;
import com.sec.internal.ims.xq.att.data.XqEvent;
import com.sec.internal.interfaces.ims.IImsFramework;

public class ResipMiscHandler extends MiscHandler implements StackIF.MiscEventListener {
    /* access modifiers changed from: private */
    public static String ATCMD_CHECK_OMADM = "AT+VOLTECON=1,0,1,0";
    /* access modifiers changed from: private */
    public static String ATCMD_CHECK_SMS_FORMAT = "AT+IMSSTEST=0,0,0";
    /* access modifiers changed from: private */
    public static String ATCMD_COMMAND_EXTRA = "command";
    /* access modifiers changed from: private */
    public static String ATCMD_IMSTEST_RESULT_PREFIX = "\r\n+IMSSTEST:0,";
    /* access modifiers changed from: private */
    public static String ATCMD_INTENT = "com.sec.factory.RECEIVED_FROM_RIL";
    /* access modifiers changed from: private */
    public static String ATCMD_RESET_OMADM = "AT+VOLTECON=0,0,0,0";
    private static String ATCMD_RESULT_ACTION = "com.sec.factory.SEND_TO_RIL";
    private static String ATCMD_RESULT_KEY = "message";
    /* access modifiers changed from: private */
    public static String ATCMD_RESULT_NG = "NG";
    /* access modifiers changed from: private */
    public static String ATCMD_RESULT_OK = "OK";
    /* access modifiers changed from: private */
    public static String ATCMD_RESULT_SUFFIX = "\r\n\r\nOK\r\n";
    /* access modifiers changed from: private */
    public static String ATCMD_VOLTECON_RESULT_PREFIX = "\r\n+VOLTECON:0,";
    private static final int EVENT_ALARM_CANCELLED = 2;
    private static final int EVENT_ALARM_FIRED = 3;
    private static final int EVENT_ALARM_REQUESTED = 1;
    private static final int EVENT_ECHOLOCATE_RECEIVED = 4;
    private static final int EVENT_XQ_MTRIP_RECEIVED = 5;
    /* access modifiers changed from: private */
    public static final String LOG_TAG = ResipMiscHandler.class.getSimpleName();
    private PreciseAlarmManager mAlarmManager = null;
    private final SparseArray<Message> mAlarmMessageList = new SparseArray<>();
    private ATCmdReceiver mAtCmdReceiver = null;
    private final Context mContext;
    private final RegistrantList mEcholocateEventRegistrants = new RegistrantList();
    /* access modifiers changed from: private */
    public final IImsFramework mImsFramework;
    private final StackIF mStackIF;
    private final RegistrantList mXqMtripEventRegistrants = new RegistrantList();

    private class ATCmdReceiver extends BroadcastReceiver {
        private ATCmdReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String str;
            String str2;
            if (context == null || intent == null) {
                Log.e(ResipMiscHandler.LOG_TAG, "Wrong Event Ignore.");
                return;
            }
            String action = intent.getAction();
            Log.i(ResipMiscHandler.LOG_TAG, "Receive Action " + action);
            if (!ResipMiscHandler.ATCMD_INTENT.equals(action)) {
                return;
            }
            if (!intent.hasExtra(ResipMiscHandler.ATCMD_COMMAND_EXTRA)) {
                Log.e(ResipMiscHandler.LOG_TAG, "Factory intent doesn't have [" + ResipMiscHandler.ATCMD_COMMAND_EXTRA + "]");
                return;
            }
            String stringExtra = intent.getStringExtra(ResipMiscHandler.ATCMD_COMMAND_EXTRA);
            if (TextUtils.isEmpty(stringExtra)) {
                Log.e(ResipMiscHandler.LOG_TAG, "Factory intent doesn't have value");
                return;
            }
            Mno mno = SimUtil.getMno();
            Log.i(ResipMiscHandler.LOG_TAG, "Factory intent command " + stringExtra);
            boolean z = true;
            if (ResipMiscHandler.ATCMD_CHECK_SMS_FORMAT.equals(stringExtra)) {
                if (mno == Mno.VZW) {
                    z = ResipMiscHandler.this.mImsFramework.isDefaultDmValue(ConfigConstants.ATCMD.SMS_SETTING, 0);
                }
                str = ResipMiscHandler.ATCMD_IMSTEST_RESULT_PREFIX;
            } else if (ResipMiscHandler.ATCMD_CHECK_OMADM.equals(stringExtra)) {
                if (mno == Mno.VZW) {
                    z = ResipMiscHandler.this.mImsFramework.isDefaultDmValue(ConfigConstants.ATCMD.OMADM_VALUE, 0);
                }
                str = ResipMiscHandler.ATCMD_VOLTECON_RESULT_PREFIX;
            } else if (ResipMiscHandler.ATCMD_RESET_OMADM.equals(stringExtra)) {
                if (mno == Mno.VZW) {
                    z = ResipMiscHandler.this.mImsFramework.setDefaultDmValue(ConfigConstants.ATCMD.OMADM_VALUE, 0);
                }
                str = ResipMiscHandler.ATCMD_VOLTECON_RESULT_PREFIX;
            } else {
                return;
            }
            if (z) {
                str2 = str + ResipMiscHandler.ATCMD_RESULT_OK;
            } else {
                str2 = str + ResipMiscHandler.ATCMD_RESULT_NG;
            }
            ResipMiscHandler.this.sendATCmdResponse(str2 + ResipMiscHandler.ATCMD_RESULT_SUFFIX);
        }
    }

    public void handleMessage(Message message) {
        int i = message.what;
        if (i == 1) {
            int i2 = message.arg1;
            if (this.mAlarmMessageList.get(i2) != null) {
                String str = LOG_TAG;
                Log.e(str, "Already reigstered id " + i2);
                return;
            }
            Message obtainMessage = obtainMessage(3, i2, -1);
            this.mAlarmMessageList.put(i2, obtainMessage);
            this.mAlarmManager.sendMessageDelayed(getClass().getSimpleName(), obtainMessage, (long) message.arg2);
        } else if (i == 2) {
            int i3 = message.arg1;
            Message message2 = this.mAlarmMessageList.get(i3);
            if (message2 == null) {
                String str2 = LOG_TAG;
                Log.e(str2, "Not reigstered id " + i3);
                return;
            }
            this.mAlarmManager.removeMessage(message2);
            this.mAlarmMessageList.remove(i3);
        } else if (i == 3) {
            int i4 = message.arg1;
            String str3 = LOG_TAG;
            Log.i(str3, "ALARM_WAKE_UP id=" + i4);
            this.mStackIF.sendAlarmWakeUp(i4);
            this.mAlarmMessageList.remove(i4);
        } else if (i == 4) {
            EcholocateMsg echolocateMsg = (EcholocateMsg) ((AsyncResult) message.obj).result;
            EcholocateEvent echolocateEvent = new EcholocateEvent();
            if (echolocateMsg.msgtype() == 0) {
                echolocateEvent.setType(EcholocateEvent.EcholocateType.signalMsg);
                EcholocateSignalMsg echolocateSignalData = echolocateMsg.echolocateSignalData();
                if (echolocateSignalData != null) {
                    echolocateEvent.setSignalData(echolocateSignalData.origin(), echolocateSignalData.line1(), echolocateSignalData.callid(), echolocateSignalData.cseq(), echolocateSignalData.sessionid(), echolocateSignalData.reason(), echolocateSignalData.contents(), echolocateSignalData.peernumber(), echolocateSignalData.isEpdgCall());
                }
            } else {
                echolocateEvent.setType(EcholocateEvent.EcholocateType.rtpMsg);
                EcholocateRtpMsg echolocateRtpData = echolocateMsg.echolocateRtpData();
                if (echolocateRtpData != null) {
                    echolocateEvent.setRtpData(echolocateRtpData.dir(), echolocateRtpData.id(), echolocateRtpData.lossrate(), echolocateRtpData.delay(), echolocateRtpData.jitter(), echolocateRtpData.measuredperiod());
                }
            }
            this.mEcholocateEventRegistrants.notifyResult(echolocateEvent);
        } else if (i != 5) {
            super.handleMessage(message);
        } else {
            Log.i(LOG_TAG, "XqMessage");
            XqMessage xqMessage = (XqMessage) ((AsyncResult) message.obj).result;
            XqEvent xqEvent = new XqEvent();
            xqEvent.setXqMtrips(xqMessage.mtrip());
            for (int i5 = 0; i5 < xqMessage.mContentLength(); i5++) {
                XqContent mContent = xqMessage.mContent(i5);
                if (mContent != null) {
                    xqEvent.setContent(mContent.type(), (int) mContent.intVal(), mContent.strVal() != null ? mContent.strVal() : "");
                }
            }
            this.mXqMtripEventRegistrants.notifyResult(xqEvent);
        }
    }

    protected ResipMiscHandler(Looper looper, Context context, IImsFramework iImsFramework) {
        super(looper);
        StackIF instance = StackIF.getInstance();
        this.mStackIF = instance;
        this.mContext = context;
        this.mImsFramework = iImsFramework;
        instance.registerMiscListener(this);
        instance.registerEcholocateEvent(this, 4, (Object) null);
        instance.registerXqMtrip(this, 5, (Object) null);
        this.mAlarmManager = PreciseAlarmManager.getInstance(context);
        this.mAtCmdReceiver = new ATCmdReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ATCMD_INTENT);
        context.registerReceiver(this.mAtCmdReceiver, intentFilter);
    }

    public void init() {
        super.init();
    }

    public void registerForEcholocateEvent(Handler handler, int i, Object obj) {
        Log.i(LOG_TAG, "registerForEcholocateEvent:");
        this.mEcholocateEventRegistrants.addUnique(handler, i, obj);
    }

    public void unregisterForEcholocateEvent(Handler handler) {
        Log.i(LOG_TAG, "unregisterForEcholocateEvent:");
        this.mEcholocateEventRegistrants.remove(handler);
    }

    public void registerForXqMtripEvent(Handler handler, int i, Object obj) {
        this.mXqMtripEventRegistrants.addUnique(handler, i, obj);
    }

    public void unregisterForXqMtripEvent(Handler handler) {
        this.mXqMtripEventRegistrants.remove(handler);
    }

    public void onAlarmRequested(int i, int i2) {
        String str = LOG_TAG;
        Log.i(str, "onAlarmRequested: delay=" + i2 + " id=" + i);
        sendMessage(obtainMessage(1, i, i2));
    }

    public void onAlarmCancelled(int i) {
        String str = LOG_TAG;
        Log.i(str, "onAlarmCancelled: id=" + i);
        sendMessage(obtainMessage(2, i, 0));
    }

    /* access modifiers changed from: private */
    public void sendATCmdResponse(String str) {
        String str2 = LOG_TAG;
        Log.i(str2, "send AT CMD response : " + str);
        Intent intent = new Intent(ATCMD_RESULT_ACTION);
        Bundle bundle = new Bundle();
        bundle.putString(ATCMD_RESULT_KEY, str);
        intent.putExtras(bundle);
        this.mContext.sendBroadcast(intent);
    }
}
