package com.sec.internal.interfaces.ims.servicemodules.im;

import android.os.Handler;
import com.sec.internal.constants.ims.servicemodules.im.params.AcceptFtSessionParams;
import com.sec.internal.constants.ims.servicemodules.im.params.AcceptSlmParams;
import com.sec.internal.constants.ims.servicemodules.im.params.RejectFtSessionParams;
import com.sec.internal.constants.ims.servicemodules.im.params.RejectSlmParams;
import com.sec.internal.constants.ims.servicemodules.im.params.SendImdnParams;
import com.sec.internal.constants.ims.servicemodules.im.params.SendSlmFileTransferParams;
import com.sec.internal.constants.ims.servicemodules.im.params.SendSlmMessageParams;

public interface ISlmServiceInterface {
    void acceptFtSlmMessage(AcceptFtSessionParams acceptFtSessionParams);

    void acceptSlm(AcceptSlmParams acceptSlmParams);

    void cancelFtSlmMessage(RejectFtSessionParams rejectFtSessionParams);

    void registerForSlmImdnNotification(Handler handler, int i, Object obj);

    void registerForSlmIncomingFileTransfer(Handler handler, int i, Object obj);

    void registerForSlmIncomingMessage(Handler handler, int i, Object obj);

    void registerForSlmLMMIncomingSession(Handler handler, int i, Object obj);

    void registerForSlmTransferProgress(Handler handler, int i, Object obj);

    void rejectFtSlmMessage(RejectFtSessionParams rejectFtSessionParams);

    void rejectSlm(RejectSlmParams rejectSlmParams);

    void sendFtSlmMessage(SendSlmFileTransferParams sendSlmFileTransferParams);

    void sendSlmDeliveredNotification(SendImdnParams sendImdnParams);

    void sendSlmDisplayedNotification(SendImdnParams sendImdnParams);

    void sendSlmMessage(SendSlmMessageParams sendSlmMessageParams);

    void unregisterAllSLMFileTransferProgress();

    void unregisterForSlmImdnNotification(Handler handler);

    void unregisterForSlmIncomingFileTransfer(Handler handler);

    void unregisterForSlmIncomingMessage(Handler handler);

    void unregisterForSlmLMMIncomingSession(Handler handler);

    void unregisterForSlmTransferProgress(Handler handler);
}
