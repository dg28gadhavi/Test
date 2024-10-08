package com.sec.internal.ims.servicemodules.im;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.sec.ims.extensions.ContextExt;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.constants.ims.servicemodules.im.NotificationStatus;
import com.sec.internal.constants.ims.servicemodules.im.result.Result;
import com.sec.internal.helper.os.IntentUtil;
import com.sec.internal.ims.servicemodules.im.interfaces.ImIntent;
import com.sec.internal.ims.servicemodules.im.listener.IFtEventListener;
import com.sec.internal.ims.servicemodules.im.listener.IMessageEventListener;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Locale;

public class ThirdPartyTranslation implements IMessageEventListener, IFtEventListener {
    private static final String LOG_TAG = "ThirdPartyTranslation";
    private final Context mContext;
    private final ImModule mImModule;

    public void onCancelMessageResponse(String str, String str2, boolean z) {
    }

    public void onCancelRequestFailed(FtMessage ftMessage) {
    }

    public void onFileResizingNeeded(FtMessage ftMessage, long j) {
    }

    public void onFileTransferAttached(FtMessage ftMessage) {
    }

    public void onFileTransferCreated(FtMessage ftMessage) {
    }

    public void onFileTransferReceived(FtMessage ftMessage) {
    }

    public void onImdnNotificationReceived(FtMessage ftMessage, ImsUri imsUri, NotificationStatus notificationStatus, boolean z) {
    }

    public void onImdnNotificationReceived(MessageBase messageBase, ImsUri imsUri, NotificationStatus notificationStatus, boolean z) {
    }

    public void onMessageSendResponseFailed(String str, int i, int i2, String str2) {
    }

    public void onMessageSendResponseTimeout(MessageBase messageBase) {
    }

    public void onMessageSendingFailed(MessageBase messageBase, IMnoStrategy.StrategyResponse strategyResponse, Result result) {
    }

    public void onMessageSendingSucceeded(MessageBase messageBase) {
    }

    public void onNotifyCloudMsgFtEvent(FtMessage ftMessage) {
    }

    public void onTransferCanceled(FtMessage ftMessage) {
    }

    public void onTransferProgressReceived(FtMessage ftMessage) {
    }

    public void onTransferStarted(FtMessage ftMessage) {
    }

    public ThirdPartyTranslation(Context context, ImModule imModule) {
        Log.i(LOG_TAG, "Create ThirdPartyTranslation.");
        this.mContext = context;
        this.mImModule = imModule;
        imModule.registerMessageEventListener(ImConstants.Type.TEXT, this);
        imModule.registerMessageEventListener(ImConstants.Type.TEXT_PUBLICACCOUNT, this);
        imModule.registerFtEventListener(ImConstants.Type.MULTIMEDIA, this);
        imModule.registerFtEventListener(ImConstants.Type.MULTIMEDIA_PUBLICACCOUNT, this);
    }

    public void broadcastIntent(Intent intent) throws NullPointerException {
        String str = LOG_TAG;
        IMSLog.s(str, "broadcastIntent: " + intent + intent.getExtras());
        IntentUtil.sendBroadcast(this.mContext, intent, ContextExt.CURRENT_OR_SELF);
    }

    public void onTransferCompleted(FtMessage ftMessage) {
        if (this.mImModule.notifyRCSMessages()) {
            ImSession imSession = this.mImModule.getImSession(ftMessage.getChatId());
            ArrayList arrayList = new ArrayList();
            if (imSession != null) {
                arrayList.addAll(imSession.getParticipantsString());
            }
            if (arrayList.isEmpty()) {
                Log.i(LOG_TAG, "onTransferCompleted: no participants for this chat");
            }
            Intent intent = new Intent(ImIntent.Action.RCS_MESSAGE);
            intent.addCategory("com.gsma.services.rcs.category.ACTION");
            ImDirection direction = ftMessage.getDirection();
            ImDirection imDirection = ImDirection.INCOMING;
            String str = "";
            if (direction == imDirection) {
                intent.putExtra("from", ftMessage.getRemoteUri() == null ? str : ftMessage.getRemoteUri().toString());
            } else {
                intent.putExtra("recipients", arrayList);
            }
            intent.putExtra("direction", ftMessage.getDirection().getId());
            broadcastIntent(intent);
            if (ftMessage.getDirection() == imDirection) {
                Intent intent2 = new Intent(ImIntent.Action.RECEIVE_RCS_MESSAGE);
                intent2.addCategory("com.gsma.services.rcs.category.ACTION");
                String upperCase = "from".toUpperCase(Locale.US);
                if (ftMessage.getRemoteUri() != null) {
                    str = ftMessage.mRemoteUri.toString();
                }
                intent2.putExtra(upperCase, str);
                broadcastIntent(intent2);
            }
        }
    }

    public void onMessageSendResponse(MessageBase messageBase) {
        if (this.mImModule.notifyRCSMessages()) {
            ImSession imSession = this.mImModule.getImSession(messageBase.getChatId());
            ArrayList arrayList = new ArrayList();
            if (imSession != null) {
                arrayList.addAll(imSession.getParticipantsString());
            }
            if (arrayList.isEmpty()) {
                Log.i(LOG_TAG, "onMessageSendResponse: no participants for this chat");
            }
            Intent intent = new Intent(ImIntent.Action.RCS_MESSAGE);
            intent.addCategory("com.gsma.services.rcs.category.ACTION");
            intent.putStringArrayListExtra("recipients", arrayList);
            intent.putExtra("direction", ImDirection.OUTGOING.getId());
            broadcastIntent(intent);
        }
    }

    public void onMessageReceived(MessageBase messageBase, ImSession imSession) {
        if (this.mImModule.notifyRCSMessages()) {
            Intent intent = new Intent(ImIntent.Action.RCS_MESSAGE);
            intent.addCategory("com.gsma.services.rcs.category.ACTION");
            ImDirection direction = messageBase.getDirection();
            ImDirection imDirection = ImDirection.INCOMING;
            if (direction == imDirection) {
                intent.putExtra("from", messageBase.getRemoteUri().toString());
            }
            intent.putExtra("direction", messageBase.getDirection().getId());
            broadcastIntent(intent);
            if (messageBase.getDirection() == imDirection) {
                Intent intent2 = new Intent(ImIntent.Action.RECEIVE_RCS_MESSAGE);
                intent2.addCategory("com.gsma.services.rcs.category.ACTION");
                intent2.putExtra("from".toUpperCase(Locale.US), messageBase.getRemoteUri().toString());
                broadcastIntent(intent2);
            }
        }
    }
}
