package com.sec.internal.ims.core.handler;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.sec.internal.constants.ims.servicemodules.im.params.AcceptFtSessionParams;
import com.sec.internal.constants.ims.servicemodules.im.params.AcceptSlmParams;
import com.sec.internal.constants.ims.servicemodules.im.params.RejectFtSessionParams;
import com.sec.internal.constants.ims.servicemodules.im.params.RejectSlmParams;
import com.sec.internal.constants.ims.servicemodules.im.params.SendSlmFileTransferParams;
import com.sec.internal.constants.ims.servicemodules.im.params.SendSlmMessageParams;
import com.sec.internal.interfaces.ims.servicemodules.im.ISlmServiceInterface;

public abstract class SlmHandler extends BaseHandler implements ISlmServiceInterface {
    public void acceptFtSlmMessage(AcceptFtSessionParams acceptFtSessionParams) {
    }

    public void acceptSlm(AcceptSlmParams acceptSlmParams) {
    }

    public void cancelFtSlmMessage(RejectFtSessionParams rejectFtSessionParams) {
    }

    public void registerForSlmImdnNotification(Handler handler, int i, Object obj) {
    }

    public void registerForSlmIncomingFileTransfer(Handler handler, int i, Object obj) {
    }

    public void registerForSlmIncomingMessage(Handler handler, int i, Object obj) {
    }

    public void registerForSlmLMMIncomingSession(Handler handler, int i, Object obj) {
    }

    public void registerForSlmTransferProgress(Handler handler, int i, Object obj) {
    }

    public void rejectFtSlmMessage(RejectFtSessionParams rejectFtSessionParams) {
    }

    public void rejectSlm(RejectSlmParams rejectSlmParams) {
    }

    public void sendFtSlmMessage(SendSlmFileTransferParams sendSlmFileTransferParams) {
    }

    public void sendSlmMessage(SendSlmMessageParams sendSlmMessageParams) {
    }

    public void unregisterAllSLMFileTransferProgress() {
    }

    public void unregisterForSlmImdnNotification(Handler handler) {
    }

    public void unregisterForSlmIncomingFileTransfer(Handler handler) {
    }

    public void unregisterForSlmIncomingMessage(Handler handler) {
    }

    public void unregisterForSlmLMMIncomingSession(Handler handler) {
    }

    public void unregisterForSlmTransferProgress(Handler handler) {
    }

    protected SlmHandler(Looper looper) {
        super(looper);
    }

    public void handleMessage(Message message) {
        int i = message.what;
        String str = this.LOG_TAG;
        Log.e(str, "Unknown event " + message.what);
    }
}
