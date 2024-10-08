package com.sec.internal.ims.core.handler.secims;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.Registrant;
import com.sec.internal.helper.RegistrantList;
import com.sec.internal.ims.core.handler.RawSipHandler;
import com.sec.internal.interfaces.ims.IImsFramework;

public class ResipRawSipHandler extends RawSipHandler {
    private static final int EVENT_SIP_INCOMING_MESSAGE = 100;
    private static final int EVENT_SIP_OUTGOING_MESSAGE = 200;
    private static final String LOG_TAG = ResipRawSipHandler.class.getSimpleName();
    private final IImsFramework mImsFramework;
    private final RegistrantList mRawSipIncomingRegistrantList = new RegistrantList();
    private final RegistrantList mRawSipOutgoingRegistrantList = new RegistrantList();
    private StackIF mStackIf = null;

    protected ResipRawSipHandler(Looper looper, IImsFramework iImsFramework) {
        super(looper);
        this.mImsFramework = iImsFramework;
    }

    public void init() {
        super.init();
        StackIF instance = StackIF.getInstance();
        this.mStackIf = instance;
        instance.registerRawSipIncomingEvent(this, 100, (Object) null);
        this.mStackIf.registerRawSipOutgoingEvent(this, 200, (Object) null);
    }

    public void registerForIncomingMessages(Handler handler, int i, Object obj) {
        Log.i(LOG_TAG, "registerForIncomingMessages");
        this.mRawSipIncomingRegistrantList.add(new Registrant(handler, i, obj));
    }

    public void registerForOutgoingMessages(Handler handler, int i, Object obj) {
        Log.i(LOG_TAG, "registerForOutgoingMessages");
        this.mRawSipOutgoingRegistrantList.add(new Registrant(handler, i, obj));
    }

    public void unregisterForEvent(Handler handler) {
        Log.i(LOG_TAG, "unregisterForEvent");
        this.mRawSipIncomingRegistrantList.remove(handler);
        this.mRawSipOutgoingRegistrantList.remove(handler);
    }

    public boolean sendSip(int i, String str, Message message) {
        UserAgent ua = getUa(i);
        if (ua == null) {
            Log.e(LOG_TAG, "sendSip: UserAgent not found");
            return false;
        }
        this.mStackIf.sendSip(ua.getHandle(), str, message);
        return true;
    }

    public void openSipDialog(boolean z) {
        this.mStackIf.openSipDialog(z);
    }

    private UserAgent getUa(int i) {
        return (UserAgent) this.mImsFramework.getRegistrationManager().getUserAgentByRegId(i);
    }

    public void handleMessage(Message message) {
        String str = LOG_TAG;
        Log.i(str, "handleMessage: event: " + message.what);
        int i = message.what;
        if (i == 100) {
            this.mRawSipIncomingRegistrantList.notifyResult(((AsyncResult) message.obj).result);
        } else if (i == 200) {
            this.mRawSipOutgoingRegistrantList.notifyResult(((AsyncResult) message.obj).result);
        }
    }
}
