package com.sec.internal.ims.servicemodules.im;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.extensions.ContextExt;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.FileDisposition;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.constants.ims.servicemodules.im.NotificationStatus;
import com.sec.internal.constants.ims.servicemodules.im.RoutingType;
import com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason;
import com.sec.internal.constants.ims.servicemodules.im.result.Result;
import com.sec.internal.helper.Preconditions;
import com.sec.internal.helper.PublicAccountUri;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.os.IntentUtil;
import com.sec.internal.helper.os.TelephonyUtilsWrapper;
import com.sec.internal.ims.servicemodules.im.interfaces.FtIntent;
import com.sec.internal.ims.servicemodules.im.interfaces.ImIntent;
import com.sec.internal.ims.servicemodules.im.listener.IFtEventListener;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.log.IMSLog;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class FtTranslation extends TranslationBase implements IFtEventListener {
    private static final String LOG_TAG = "FtTranslation";
    private final Context mContext;
    private final FtProcessor mFtProcessor;
    private final ImModule mImModule;

    public void onNotifyCloudMsgFtEvent(FtMessage ftMessage) {
    }

    public void onTransferStarted(FtMessage ftMessage) {
    }

    public FtTranslation(Context context, ImModule imModule, FtProcessor ftProcessor) {
        this.mContext = context;
        this.mImModule = imModule;
        imModule.registerFtEventListener(ImConstants.Type.MULTIMEDIA, this);
        imModule.registerFtEventListener(ImConstants.Type.MULTIMEDIA_PUBLICACCOUNT, this);
        this.mFtProcessor = ftProcessor;
    }

    private void broadcastIntent(Intent intent, int i) {
        String str = LOG_TAG;
        IMSLog.s(str, "broadcastIntent: " + intent + intent.getExtras());
        intent.addFlags(LogClass.SIM_EVENT);
        UserHandle subscriptionUserHandle = TelephonyUtilsWrapper.getSubscriptionUserHandle(this.mContext, SimUtil.getSubId(i));
        if (this.mImModule.getRcsStrategy() != null && this.mImModule.getRcsStrategy().isBMode(true)) {
            IntentUtil.sendBroadcast(this.mContext, intent, ContextExt.OWNER, "com.samsung.rcs.im.READ_PERMISSION");
        } else if (subscriptionUserHandle != null) {
            IntentUtil.sendBroadcast(this.mContext, intent, subscriptionUserHandle, "com.samsung.rcs.im.READ_PERMISSION");
        } else {
            IntentUtil.sendBroadcast(this.mContext, intent, ContextExt.CURRENT_OR_SELF, "com.samsung.rcs.im.READ_PERMISSION");
        }
    }

    public void handleIntent(Intent intent) {
        String action = intent.getAction();
        String str = LOG_TAG;
        IMSLog.s(str, "Received intent: " + action);
        action.hashCode();
        char c = 65535;
        switch (action.hashCode()) {
            case -1898279646:
                if (action.equals(FtIntent.Actions.RequestIntents.SEND_FILE_TO_GROUP_CHAT)) {
                    c = 0;
                    break;
                }
                break;
            case -1873675579:
                if (action.equals(FtIntent.Actions.RequestIntents.ATTACH_FILE_TO_GROUP_CHAT)) {
                    c = 1;
                    break;
                }
                break;
            case -1840010002:
                if (action.equals(FtIntent.Actions.RequestIntents.TRANSFER_ACCEPT)) {
                    c = 2;
                    break;
                }
                break;
            case -1784273312:
                if (action.equals(FtIntent.Actions.RequestIntents.TRANSFER_CANCEL)) {
                    c = 3;
                    break;
                }
                break;
            case -1143332975:
                if (action.equals(FtIntent.Actions.RequestIntents.RESUME_INCOMING_FILE)) {
                    c = 4;
                    break;
                }
                break;
            case -1130273179:
                if (action.equals(FtIntent.Actions.RequestIntents.SEND_FILE)) {
                    c = 5;
                    break;
                }
                break;
            case -596796384:
                if (action.equals(FtIntent.Actions.RequestIntents.SET_AUTO_ACCEPT_FT)) {
                    c = 6;
                    break;
                }
                break;
            case 1052208476:
                if (action.equals(FtIntent.Actions.ResponseIntentFromApp.RESPONSE_FILE_RESIZE)) {
                    c = 7;
                    break;
                }
                break;
            case 1399551557:
                if (action.equals(FtIntent.Actions.RequestIntents.RESUME_SENDING_FILE)) {
                    c = 8;
                    break;
                }
                break;
            case 1514235824:
                if (action.equals(FtIntent.Actions.RequestIntents.TRANSFER_DECLINE)) {
                    c = 9;
                    break;
                }
                break;
            case 1870667080:
                if (action.equals(FtIntent.Actions.RequestIntents.ATTACH_FILE)) {
                    c = 10;
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
            case 5:
                requestSendFile(intent);
                return;
            case 1:
                requestAttachFileToGroupChat(intent);
                return;
            case 2:
                requestAcceptFileTransfer(intent);
                return;
            case 3:
                requestCancelFileTransfer(intent);
                return;
            case 4:
                requestResumeReceivingFileTransfer(intent);
                return;
            case 6:
                requestSetAutoAcceptFt(intent);
                return;
            case 7:
                handleFileResizeResponse(intent);
                return;
            case 8:
                requestResumeSendingFileTransfer(intent);
                return;
            case 9:
                requestDeclineFileTransfer(intent);
                return;
            case 10:
                requestAttachFileToSingleChat(intent);
                return;
            default:
                Log.i(str, "Unexpected intent received. acition=" + action);
                return;
        }
    }

    private void requestAttachFileToSingleChat(Intent intent) {
        Bundle extras = intent.getExtras();
        String string = extras.getString("fileName");
        Uri uri = (Uri) extras.getParcelable("contentUri");
        Uri uri2 = (Uri) extras.getParcelable("contactUri");
        int i = 0;
        boolean z = extras.getBoolean("is_resizable", false);
        String string2 = extras.getString("disposition_notification");
        FileDisposition valueOf = FileDisposition.valueOf(intent.getIntExtra("file_disposition", FileDisposition.ATTACH.toInt()));
        String valueOf2 = String.valueOf(extras.getLong("request_message_id"));
        boolean z2 = extras.getBoolean("is_publicAccountMsg", false);
        boolean z3 = extras.getBoolean(FtIntent.Extras.EXTRA_EXTRA_FT, false);
        boolean z4 = extras.getBoolean(FtIntent.Extras.EXTRA_IS_FTSMS, false);
        String string3 = extras.getString("sim_slot_id");
        String string4 = extras.getString(FtIntent.Extras.EXTRA_FT_CONTENTTYPE);
        boolean z5 = extras.getBoolean(ImIntent.Extras.IS_TOKEN_USED, false);
        boolean z6 = extras.getBoolean(ImIntent.Extras.IS_TOKEN_LINK, false);
        if (z2) {
            PublicAccountUri.setPublicAccountDomain(extras.getString("publicAccount_Send_Domain"));
        }
        if (uri == null || uri2 == null) {
            String str = LOG_TAG;
            Log.e(str, "illegal arguments from message app: contentUri: " + IMSLog.checker(uri) + "uri: " + IMSLog.checker(uri2) + "disposition: " + string2 + "requestMessageId: " + valueOf2);
            return;
        }
        if (string3 != null) {
            try {
                i = Integer.parseInt(string3);
            } catch (NumberFormatException unused) {
                String str2 = LOG_TAG;
                Log.e(str2, "Invalid slot id : " + string3);
            }
        }
        String str3 = LOG_TAG;
        Log.i(str3, "requestAttachFileToSingleChat() phoneId= " + i);
        this.mFtProcessor.attachFileToSingleChat(i, string, uri, ImsUri.parse(uri2.toString()), NotificationStatus.toSet(string2), valueOf2, string4, z2, z, z3, z4, (String) null, valueOf, z5, z6);
    }

    private void requestAttachFileToGroupChat(Intent intent) {
        Bundle extras = intent.getExtras();
        Log.i(LOG_TAG, "requestAttachFileToGroupChat()");
        String string = extras.getString("chatId");
        String string2 = extras.getString("fileName");
        Uri uri = (Uri) extras.getParcelable("contentUri");
        boolean z = extras.getBoolean("is_resizable", false);
        String string3 = extras.getString("disposition_notification");
        String valueOf = String.valueOf(extras.getLong("request_message_id"));
        boolean z2 = extras.getBoolean("is_broadcast_msg", false);
        boolean z3 = extras.getBoolean(FtIntent.Extras.EXTRA_EXTRA_FT, false);
        boolean z4 = extras.getBoolean(FtIntent.Extras.EXTRA_IS_FTSMS, false);
        FileDisposition valueOf2 = FileDisposition.valueOf(intent.getIntExtra("file_disposition", FileDisposition.ATTACH.toInt()));
        String string4 = extras.getString(FtIntent.Extras.EXTRA_FT_CONTENTTYPE);
        if (uri != null) {
            this.mFtProcessor.attachFileToGroupChat(string, string2, uri, NotificationStatus.toSet(string3), valueOf, string4, z, z2, z3, z4, (String) null, valueOf2);
        }
    }

    private void requestSendFile(Intent intent) {
        this.mFtProcessor.sendFile(intent.getExtras().getString("message_imdn_id"));
    }

    private void requestAcceptFileTransfer(Intent intent) {
        Bundle extras = intent.getExtras();
        this.mFtProcessor.acceptFileTransfer(extras.getString("message_imdn_id"), ImDirection.fromId(extras.getInt("message_direction")), extras.getString("chatId"), (Uri) extras.getParcelable("contentUri"));
    }

    private void requestDeclineFileTransfer(Intent intent) {
        Bundle extras = intent.getExtras();
        this.mFtProcessor.rejectFileTransfer(extras.getString("message_imdn_id"), ImDirection.fromId(extras.getInt("message_direction")), extras.getString("chatId"));
    }

    private void requestResumeSendingFileTransfer(Intent intent) {
        Bundle extras = intent.getExtras();
        String string = extras.getString("message_imdn_id");
        boolean z = extras.getBoolean("is_resizable", false);
        String str = LOG_TAG;
        Log.i(str, "requestResumeSendingFileTransfer isResizable=" + z);
        this.mFtProcessor.resumeSendingTransfer(string, (Uri) extras.getParcelable("contentUri"), z);
    }

    private void requestResumeReceivingFileTransfer(Intent intent) {
        Bundle extras = intent.getExtras();
        this.mFtProcessor.resumeReceivingTransfer(extras.getString("message_imdn_id"), extras.getString("chatId"), (Uri) extras.getParcelable("contentUri"));
    }

    private void requestCancelFileTransfer(Intent intent) {
        Bundle extras = intent.getExtras();
        this.mFtProcessor.cancelFileTransfer(extras.getString("message_imdn_id"), ImDirection.fromId(extras.getInt("message_direction")), extras.getString("chatId"));
    }

    private void requestSetAutoAcceptFt(Intent intent) {
        int i;
        Bundle extras = intent.getExtras();
        String string = extras.getString("sim_slot_id");
        int i2 = extras.getInt(FtIntent.Extras.EXTRA_AUTO_ACCEPT_STATE);
        if (!TextUtils.isEmpty(string)) {
            try {
                i = Integer.valueOf(string).intValue();
            } catch (NumberFormatException unused) {
                String str = LOG_TAG;
                Log.e(str, "Invalid slot id : " + string);
            }
            this.mFtProcessor.setAutoAcceptFt(i, i2);
        }
        i = 0;
        this.mFtProcessor.setAutoAcceptFt(i, i2);
    }

    private void handleFileResizeResponse(Intent intent) {
        Bundle extras = intent.getExtras();
        String string = extras.getString("message_imdn_id");
        boolean z = extras.getBoolean(FtIntent.Extras.EXTRA_REQUEST_RESULT);
        Uri uri = (Uri) extras.getParcelable("contentUri");
        if (uri != null) {
            this.mFtProcessor.handleFileResizeResponse(string, z, uri);
        }
    }

    public void onFileTransferCreated(FtMessage ftMessage) {
        Log.i(LOG_TAG, "onFileTransferCreated()");
        Preconditions.checkNotNull(ftMessage, "msg is null");
        Intent intent = new Intent();
        intent.addCategory(FtIntent.CATEGORY_NOTIFICATION);
        intent.setAction(FtIntent.Actions.ResponseIntents.TRANSFER_CREATED);
        intent.putExtra("message_imdn_id", ftMessage.getImdnId());
        intent.putExtra("chatId", ftMessage.getChatId());
        intent.putExtra("contactUri", ftMessage.getRemoteUri());
        intent.putExtra("contentUri", ftMessage.getContentUri());
        intent.putExtra(FtIntent.Extras.EXTRA_BYTES_TOTAL, Long.valueOf(ftMessage.getFileSize()).intValue());
        broadcastIntent(intent, ftMessage.getPhoneId());
    }

    public void onFileTransferAttached(FtMessage ftMessage) {
        Log.i(LOG_TAG, "onFileTransferAttached()");
        Preconditions.checkNotNull(ftMessage, "msg is null");
        Intent intent = new Intent();
        intent.addCategory(FtIntent.CATEGORY_NOTIFICATION);
        intent.setAction(FtIntent.Actions.ResponseIntents.TRANSFER_ATTACHED);
        intent.putExtra("message_imdn_id", ftMessage.getImdnId());
        intent.putExtra("request_message_id", ftMessage.getRequestMessageId() == null ? -1 : Long.valueOf(ftMessage.getRequestMessageId()).longValue());
        intent.putExtra("chatId", ftMessage.getChatId());
        intent.putExtra("contactUri", ftMessage.getRemoteUri());
        intent.putExtra(FtIntent.Extras.EXTRA_BYTES_TOTAL, Long.valueOf(ftMessage.getFileSize()).intValue());
        intent.putExtra(FtIntent.Extras.EXTRA_FT_MECH, ftMessage.getTransferMech());
        broadcastIntent(intent, ftMessage.getPhoneId());
    }

    public void onTransferProgressReceived(FtMessage ftMessage) {
        Log.i(LOG_TAG, "onTransferProgressReceived()");
        Intent intent = new Intent();
        intent.addCategory(FtIntent.CATEGORY_NOTIFICATION);
        intent.setAction(FtIntent.Actions.ResponseIntents.TRANSFER_PROGRESS);
        intent.putExtra("message_imdn_id", ftMessage.getImdnId());
        intent.putExtra("message_direction", ftMessage.getDirection().getId());
        intent.putExtra("chatId", ftMessage.getChatId());
        intent.putExtra("contactUri", ftMessage.getRemoteUri());
        intent.putExtra(FtIntent.Extras.EXTRA_BYTES_DONE, Long.valueOf(ftMessage.getTransferredBytes()).intValue());
        intent.putExtra(FtIntent.Extras.EXTRA_BYTES_TOTAL, Long.valueOf(ftMessage.getFileSize()).intValue());
        broadcastIntent(intent, ftMessage.getPhoneId());
    }

    public void onTransferCompleted(FtMessage ftMessage) {
        Log.i(LOG_TAG, "onTransferCompleted()");
        Intent intent = new Intent();
        intent.addCategory(FtIntent.CATEGORY_NOTIFICATION);
        intent.setAction(FtIntent.Actions.ResponseIntents.TRANSFER_COMPLETED);
        intent.putExtra("request_message_id", ftMessage.getRequestMessageId() == null ? -1 : Long.valueOf(ftMessage.getRequestMessageId()).longValue());
        intent.putExtra("chatId", ftMessage.getChatId());
        intent.putExtra("message_direction", ftMessage.getDirection().getId());
        intent.putExtra("contactUri", ftMessage.getRemoteUri());
        intent.putExtra("message_imdn_id", ftMessage.getImdnId());
        intent.putExtra("sessionId", Long.valueOf((long) ftMessage.getId()));
        intent.putExtra(FtIntent.Extras.EXTRA_BYTES_TOTAL, Long.valueOf(ftMessage.getFileSize()).intValue());
        intent.putExtra("notification_status", ftMessage.getDesiredNotificationStatus().getId());
        intent.putExtra(FtIntent.Extras.EXTRA_FILE_EXPIRE, ftMessage.getDirection() == ImDirection.OUTGOING ? ftMessage.getFileExpire() : "");
        intent.putExtra("contentUri", ftMessage.getContentUri());
        FileDisposition fileDisposition = ftMessage.getFileDisposition();
        if (fileDisposition == null) {
            fileDisposition = FileDisposition.ATTACH;
        }
        intent.putExtra("file_disposition", fileDisposition.toInt());
        if (fileDisposition == FileDisposition.RENDER) {
            intent.putExtra("playing_length", ftMessage.getPlayingLength());
        }
        intent.putExtra(FtIntent.Extras.FT_SMS_DATAURL, ftMessage.getFileDataUrl());
        intent.putExtra(FtIntent.Extras.EXTRA_IS_FTSMS, ftMessage.isFtSms());
        if (ftMessage.isFtSms()) {
            intent.putExtra(FtIntent.Extras.FT_SMS_BRANDEDURL, ftMessage.getFileBrandedUrl());
        }
        broadcastIntent(intent, ftMessage.getPhoneId());
    }

    public void onFileTransferReceived(FtMessage ftMessage) {
        Log.i(LOG_TAG, "onFileTransferReceived()");
        Intent intent = new Intent();
        intent.addCategory(FtIntent.CATEGORY_NOTIFICATION);
        intent.setAction(FtIntent.Actions.ResponseIntents.TRANSFER_INCOMING);
        intent.putExtra("message_imdn_id", ftMessage.getImdnId());
        intent.putExtra("sessionId", Long.valueOf((long) ftMessage.getId()));
        intent.putExtra("chatId", ftMessage.getChatId());
        intent.putExtra("fileName", ftMessage.getFileName());
        intent.putExtra("contactUri", ftMessage.getRemoteUri());
        intent.putExtra(FtIntent.Extras.EXTRA_BYTES_TOTAL, ftMessage.getFileSize());
        try {
            if (!TextUtils.isEmpty(ftMessage.getThumbnailPath())) {
                File file = new File(ftMessage.getThumbnailPath());
                if (file.exists()) {
                    intent.putExtra(FtIntent.Extras.EXTRA_THUMBNAIL_DATA, Files.readAllBytes(file.toPath()));
                    ftMessage.deleteThumbnail();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        intent.putExtra(FtIntent.Extras.EXTRA_TIME_DURATION, ftMessage.getTimeDuration());
        intent.putExtra(FtIntent.Extras.EXTRA_OUTGOING_REQUEST, ftMessage.getDirection() == ImDirection.OUTGOING);
        intent.putExtra(FtIntent.Extras.EXTRA_FILE_EXPIRE, ftMessage.getFileExpire());
        intent.putExtra(FtIntent.Extras.EXTRA_IS_STANDALONE, ftMessage.getIsSlmSvcMsg());
        intent.putExtra("message_type", ftMessage.getType().getId());
        FileDisposition fileDisposition = ftMessage.getFileDisposition();
        if (fileDisposition == null) {
            fileDisposition = FileDisposition.ATTACH;
        }
        intent.putExtra("file_disposition", fileDisposition.toInt());
        if (fileDisposition == FileDisposition.RENDER) {
            intent.putExtra("playing_length", ftMessage.getPlayingLength());
        }
        intent.putExtra("message_direction", ftMessage.getDirection().getId());
        if (ftMessage.isRoutingMsg()) {
            intent.putExtra(ImIntent.Extras.IS_ROUTING_MSG, ftMessage.isRoutingMsg());
            if (!(ftMessage.getRoutingType() == null || ftMessage.getRoutingType() == RoutingType.NONE)) {
                intent.putExtra(ImIntent.Extras.ROUTING_MSG_TYPE, ftMessage.getRoutingType().getId());
            }
        }
        if (ftMessage instanceof FtMsrpMessage) {
            intent.addFlags(LogClass.SIM_EVENT);
        } else {
            intent.putExtra(FtIntent.Extras.EXTRA_FT_AUTODOWNLOAD, ftMessage.mIsAutoDownload ? 1 : 0);
        }
        if (ftMessage.getDirection() == ImDirection.INCOMING) {
            intent.putExtra("from", ftMessage.getRemoteUri() == null ? "" : ftMessage.getRemoteUri().toString());
        }
        intent.putExtra(FtIntent.Extras.EXTRA_FT_MECH, ftMessage.getTransferMech());
        if (ftMessage.getChatbotMessagingTech() == ImConstants.ChatbotMessagingTech.STANDALONE_MESSAGING) {
            intent.putExtra(ImIntent.Extras.IS_BOT, true);
        }
        putMaapExtras(ftMessage, intent);
        String rcsTrafficType = ftMessage.getRcsTrafficType();
        if (rcsTrafficType != null) {
            String str = LOG_TAG;
            Log.i(str, "rcsTrafficType = [" + rcsTrafficType + "]");
            intent.putExtra(ImIntent.Extras.RCS_TRAFFIC_TYPE, rcsTrafficType);
        }
        broadcastIntent(intent, ftMessage.getPhoneId());
    }

    public void onTransferCanceled(FtMessage ftMessage) {
        Log.i(LOG_TAG, "onTransferCanceled()");
        if (ftMessage.getCancelReason() != CancelReason.INVALID_URL_TEMPLATE) {
            Intent intent = new Intent();
            intent.addCategory(FtIntent.CATEGORY_NOTIFICATION);
            intent.setAction(FtIntent.Actions.ResponseIntents.TRANSFER_CANCELED);
            intent.putExtra("message_imdn_id", ftMessage.getImdnId());
            intent.putExtra("request_message_id", ftMessage.getRequestMessageId() == null ? -1 : Long.valueOf(ftMessage.getRequestMessageId()).longValue());
            intent.putExtra("chatId", ftMessage.getChatId());
            intent.putExtra("message_direction", ftMessage.getDirection().getId());
            intent.putExtra("contactUri", ftMessage.getRemoteUri());
            intent.putExtra("reason", ((ftMessage.getCancelReason() == null || this.mImModule.hasChatbotParticipant(ftMessage.getChatId())) ? CancelReason.UNKNOWN : ftMessage.getCancelReason()).getId());
            intent.putExtra(FtIntent.Extras.EXTRA_RESUMABLE_OPTION_CODE, ftMessage.getResumableOptionCode());
            intent.putExtra(ImIntent.Extras.ERROR_NOTIFICATION_ID, ftMessage.getErrorNotificationId().ordinal());
            broadcastIntent(intent, ftMessage.getPhoneId());
        }
    }

    public void onImdnNotificationReceived(FtMessage ftMessage, ImsUri imsUri, NotificationStatus notificationStatus, boolean z) {
        boolean z2 = !TextUtils.isEmpty(ftMessage.getMessageCreator()) && ftMessage.getMessageCreator().equalsIgnoreCase(ImConstants.MessageCreatorTag.SD);
        String str = LOG_TAG;
        Log.i(str, "onImdnNotificationReceived() isSDMessage: " + z2);
        if (!z2) {
            broadcastIntent(createImdnNotificationReceivedIntent(ftMessage, imsUri, notificationStatus, z), ftMessage.getPhoneId());
        }
    }

    public void onFileResizingNeeded(FtMessage ftMessage, long j) {
        Log.i(LOG_TAG, "requestLargeMessageModeFileResize()");
        Preconditions.checkNotNull(ftMessage, "msg is null");
        Intent intent = new Intent();
        intent.addCategory(FtIntent.CATEGORY_NOTIFICATION);
        intent.setAction(FtIntent.Actions.RequestIntentToApp.REQUEST_FILE_RESIZE);
        intent.putExtra("message_imdn_id", ftMessage.getImdnId());
        intent.putExtra("request_message_id", ftMessage.getRequestMessageId() == null ? -1 : Long.valueOf(ftMessage.getRequestMessageId()).longValue());
        intent.putExtra("chatId", ftMessage.getChatId());
        intent.putExtra(FtIntent.Extras.EXTRA_RESIZE_LIMIT, j);
        broadcastIntent(intent, ftMessage.getPhoneId());
    }

    public void onCancelRequestFailed(FtMessage ftMessage) {
        Log.i(LOG_TAG, "onCancelRequestFailed()");
        Intent intent = new Intent();
        intent.addCategory(FtIntent.CATEGORY_NOTIFICATION);
        intent.setAction(FtIntent.Actions.ResponseIntents.REQUEST_FAILED);
        intent.putExtra("message_imdn_id", ftMessage.getImdnId());
        intent.putExtra("message_direction", ftMessage.getDirection().getId());
        intent.putExtra("chatId", ftMessage.getChatId());
        intent.putExtra(FtIntent.Extras.EXTRA_INVOKING_ACTION, FtIntent.Actions.RequestIntents.TRANSFER_CANCEL);
        broadcastIntent(intent, ftMessage.getPhoneId());
    }

    public void onMessageSendingFailed(MessageBase messageBase, IMnoStrategy.StrategyResponse strategyResponse, Result result) {
        Log.i(LOG_TAG, "onMessageSendingFailed()");
        Preconditions.checkNotNull(messageBase, "message is null");
        Intent createMessageSendingFailedIntent = createMessageSendingFailedIntent(messageBase, strategyResponse, result);
        if (messageBase instanceof FtMessage) {
            createMessageSendingFailedIntent.putExtra(ImIntent.Extras.IS_FT, true);
        }
        if (messageBase instanceof FtHttpOutgoingMessage) {
            FtMessage ftMessage = (FtMessage) messageBase;
            createMessageSendingFailedIntent.putExtra(FtIntent.Extras.FT_SMS_DATAURL, ftMessage.getFileDataUrl());
            createMessageSendingFailedIntent.putExtra(FtIntent.Extras.FT_SMS_BRANDEDURL, ftMessage.getFileBrandedUrl());
        }
        broadcastIntent(createMessageSendingFailedIntent, messageBase.getPhoneId());
    }
}
