package com.sec.internal.ims.servicemodules.tapi.service.api;

import android.os.RemoteException;
import com.gsma.services.rcs.chat.ChatLog;
import com.gsma.services.rcs.chat.IChatMessage;
import com.gsma.services.rcs.contact.ContactId;
import com.sec.internal.constants.ims.servicemodules.im.NotificationStatus;
import com.sec.internal.ims.servicemodules.im.ImCache;
import com.sec.internal.ims.servicemodules.im.MessageBase;

public class ChatMessageImpl extends IChatMessage.Stub {
    private String mMsgId;

    public boolean isRead() throws RemoteException {
        return true;
    }

    public ChatMessageImpl(String str) {
        this.mMsgId = str;
    }

    public long getTimestampDelivered() throws RemoteException {
        MessageBase queryMessageForOpenApi = ImCache.getInstance().queryMessageForOpenApi(this.mMsgId);
        if (queryMessageForOpenApi != null) {
            return queryMessageForOpenApi.getDeliveredTimestamp();
        }
        return 0;
    }

    public long getTimestampDisplayed() throws RemoteException {
        MessageBase queryMessageForOpenApi = ImCache.getInstance().queryMessageForOpenApi(this.mMsgId);
        if (queryMessageForOpenApi != null) {
            return queryMessageForOpenApi.getDisplayedTimestamp().longValue();
        }
        return 0;
    }

    public String getChatId() throws RemoteException {
        MessageBase queryMessageForOpenApi = ImCache.getInstance().queryMessageForOpenApi(this.mMsgId);
        if (queryMessageForOpenApi != null) {
            return queryMessageForOpenApi.getChatId();
        }
        return null;
    }

    public ContactId getContact() throws RemoteException {
        MessageBase queryMessageForOpenApi = ImCache.getInstance().queryMessageForOpenApi(this.mMsgId);
        if (queryMessageForOpenApi == null || queryMessageForOpenApi.getRemoteUri() == null) {
            return null;
        }
        return new ContactId(queryMessageForOpenApi.getRemoteUri().toString());
    }

    public String getContent() throws RemoteException {
        MessageBase queryMessageForOpenApi = ImCache.getInstance().queryMessageForOpenApi(this.mMsgId);
        if (queryMessageForOpenApi != null) {
            return queryMessageForOpenApi.getBody();
        }
        return null;
    }

    public int getDirection() throws RemoteException {
        MessageBase queryMessageForOpenApi = ImCache.getInstance().queryMessageForOpenApi(this.mMsgId);
        if (queryMessageForOpenApi == null || queryMessageForOpenApi.getDirection() == null) {
            return 0;
        }
        return queryMessageForOpenApi.getDirection().ordinal();
    }

    public String getId() throws RemoteException {
        MessageBase queryMessageForOpenApi = ImCache.getInstance().queryMessageForOpenApi(this.mMsgId);
        if (queryMessageForOpenApi == null) {
            return null;
        }
        return queryMessageForOpenApi.getId() + "";
    }

    public String getMimeType() throws RemoteException {
        MessageBase queryMessageForOpenApi = ImCache.getInstance().queryMessageForOpenApi(this.mMsgId);
        if (queryMessageForOpenApi != null) {
            return queryMessageForOpenApi.getContentType();
        }
        return null;
    }

    public String getMaapTrafficType() throws RemoteException {
        MessageBase queryMessageForOpenApi = ImCache.getInstance().queryMessageForOpenApi(this.mMsgId);
        if (queryMessageForOpenApi != null) {
            return queryMessageForOpenApi.getMaapTrafficType();
        }
        return null;
    }

    public int getReasonCode() throws RemoteException {
        return ChatLog.Message.Content.ReasonCode.UNSPECIFIED.toInt();
    }

    public int getStatus() throws RemoteException {
        MessageBase queryMessageForOpenApi = ImCache.getInstance().queryMessageForOpenApi(this.mMsgId);
        if (queryMessageForOpenApi == null || queryMessageForOpenApi.getStatus() == null) {
            return 0;
        }
        return convertMessageStatus(queryMessageForOpenApi.getStatus().ordinal()).toInt();
    }

    private ChatLog.Message.Content.Status convertMessageStatus(int i) {
        ChatLog.Message.Content.Status status = ChatLog.Message.Content.Status.FAILED;
        switch (i) {
            case 0:
                return ChatLog.Message.Content.Status.DELIVERED;
            case 1:
                return ChatLog.Message.Content.Status.DISPLAYED;
            case 2:
                return ChatLog.Message.Content.Status.SENDING;
            case 3:
                return ChatLog.Message.Content.Status.SENT;
            case 5:
                return ChatLog.Message.Content.Status.QUEUED;
            case 6:
                return ChatLog.Message.Content.Status.DISPLAY_REPORT_REQUESTED;
            case 7:
                return ChatLog.Message.Content.Status.QUEUED;
            case 8:
                return ChatLog.Message.Content.Status.REJECTED;
            default:
                return status;
        }
    }

    public long getTimestamp() throws RemoteException {
        MessageBase queryMessageForOpenApi = ImCache.getInstance().queryMessageForOpenApi(this.mMsgId);
        if (queryMessageForOpenApi != null) {
            return queryMessageForOpenApi.getInsertedTimestamp();
        }
        return 0;
    }

    public long getTimestampSent() throws RemoteException {
        MessageBase queryMessageForOpenApi = ImCache.getInstance().queryMessageForOpenApi(this.mMsgId);
        if (queryMessageForOpenApi != null) {
            return queryMessageForOpenApi.getSentTimestamp();
        }
        return 0;
    }

    public boolean isExpiredDelivery() throws RemoteException {
        MessageBase queryMessageForOpenApi = ImCache.getInstance().queryMessageForOpenApi(this.mMsgId);
        return queryMessageForOpenApi != null && queryMessageForOpenApi.getNotificationStatus() == NotificationStatus.NONE;
    }
}
