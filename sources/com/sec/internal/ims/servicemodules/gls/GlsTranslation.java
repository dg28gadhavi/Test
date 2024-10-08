package com.sec.internal.ims.servicemodules.gls;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.extensions.ContextExt;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.constants.ims.servicemodules.im.NotificationStatus;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImErrorReason;
import com.sec.internal.constants.ims.servicemodules.im.result.Result;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.os.IntentUtil;
import com.sec.internal.helper.os.TelephonyUtilsWrapper;
import com.sec.internal.ims.rcs.RcsPolicyManager;
import com.sec.internal.ims.servicemodules.gls.GlsIntent;
import com.sec.internal.ims.servicemodules.im.FtMessage;
import com.sec.internal.ims.servicemodules.im.ImSession;
import com.sec.internal.ims.servicemodules.im.MessageBase;
import com.sec.internal.ims.servicemodules.im.interfaces.ImIntent;
import com.sec.internal.ims.servicemodules.im.listener.IFtEventListener;
import com.sec.internal.ims.servicemodules.im.listener.IMessageEventListener;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.imscr.LogClass;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

public class GlsTranslation implements IMessageEventListener, IFtEventListener {
    private static final String LOG_TAG = "GlsTranslation";
    private final Context mContext;
    private final GlsModule mGlsModule;

    public void onCancelMessageResponse(String str, String str2, boolean z) {
    }

    public void onCancelRequestFailed(FtMessage ftMessage) {
    }

    public void onFileResizingNeeded(FtMessage ftMessage, long j) {
    }

    public void onFileTransferCreated(FtMessage ftMessage) {
    }

    public void onMessageSendResponseFailed(String str, int i, int i2, String str2) {
    }

    public void onMessageSendResponseTimeout(MessageBase messageBase) {
    }

    public void onNotifyCloudMsgFtEvent(FtMessage ftMessage) {
    }

    public void onTransferProgressReceived(FtMessage ftMessage) {
    }

    public void onTransferStarted(FtMessage ftMessage) {
    }

    public GlsTranslation(Context context, GlsModule glsModule) {
        this.mGlsModule = glsModule;
        this.mContext = context;
        ImConstants.Type type = ImConstants.Type.LOCATION;
        glsModule.registerFtEventListener(type, this);
        glsModule.registerMessageEventListener(type, this);
    }

    public void handleIntent(Intent intent) {
        if (!intent.hasCategory(GlsIntent.CATEGORY_ACTION)) {
            return;
        }
        if (GlsIntent.Actions.RequestIntents.SHARE_LOCATION_IN_CHAT.equals(intent.getAction()) || GlsIntent.Actions.RequestIntents.SHARE_LOCATION_IN_CHAT_GC.equals(intent.getAction())) {
            requestShareLocationInChat(intent);
        } else if (GlsIntent.Actions.RequestIntents.CREATE_SHARE_LOCATION_INCALL.equals(intent.getAction()) || GlsIntent.Actions.RequestIntents.CREATE_SHARE_LOCATION_INCALL_GC.equals(intent.getAction())) {
            requestCreateInCallLocationShare(intent);
        } else if (GlsIntent.Actions.RequestIntents.ACCEPT_SHARE_LOCATION_INCALL.equals(intent.getAction())) {
            requestAcceptLocationShare(intent);
        } else if (GlsIntent.Actions.RequestIntents.REJECT_SHARE_LOCATION_INCALL.equals(intent.getAction())) {
            requestRejectLocationShare(intent);
        } else if (GlsIntent.Actions.RequestIntents.START_SHARE_LOCATION_INCALL.equals(intent.getAction())) {
            requestStartLocationShareInCall(intent);
        } else {
            String str = LOG_TAG;
            Log.v(str, "Unknown action: " + intent.getAction());
        }
    }

    private void requestShareLocationInChat(Intent intent) {
        boolean z;
        ImsUri imsUri;
        int i;
        Log.i(LOG_TAG, "requestShareLocationInChat()");
        Bundle extras = intent.getExtras();
        String string = extras.getString("chat_id");
        String string2 = extras.getString("disposition_notification");
        Location location = (Location) extras.getParcelable(GlsIntent.Extras.EXTRA_LOCATION);
        String string3 = "1".equals(Integer.toString(extras.getInt(GlsIntent.Extras.EXTRA_LOCATION_TYPE))) ? null : extras.getString("label");
        String valueOf = String.valueOf(extras.getLong("request_message_id"));
        String string4 = extras.getString(GlsIntent.Extras.EXTRA_LOCATION_LINK);
        Uri uri = (Uri) extras.getParcelable("contactUri");
        String string5 = extras.getString("maap_traffic_type");
        String string6 = extras.getString("sim_slot_id");
        if (GlsIntent.Actions.RequestIntents.SHARE_LOCATION_IN_CHAT.equals(intent.getAction())) {
            z = false;
            imsUri = ImsUri.parse(uri.toString());
        } else {
            z = GlsIntent.Actions.RequestIntents.SHARE_LOCATION_IN_CHAT_GC.equals(intent.getAction());
            imsUri = null;
        }
        if (!TextUtils.isEmpty(string6)) {
            try {
                i = Integer.valueOf(string6).intValue();
            } catch (NumberFormatException unused) {
                Log.i(LOG_TAG, "Invalid slot id : " + string6);
            }
            this.mGlsModule.shareLocationInChat(i, string, NotificationStatus.toSet(string2), location, string3, valueOf, string4, imsUri, z, string5);
        }
        i = -1;
        this.mGlsModule.shareLocationInChat(i, string, NotificationStatus.toSet(string2), location, string3, valueOf, string4, imsUri, z, string5);
    }

    private void requestCreateInCallLocationShare(Intent intent) {
        boolean z;
        ImsUri imsUri;
        ImsUri imsUri2;
        Log.i(LOG_TAG, "requestCreateInCallLocationShare()");
        Bundle extras = intent.getExtras();
        String string = extras.getString("chat_id");
        Uri uri = (Uri) extras.getParcelable("contactUri");
        String string2 = extras.getString("disposition_notification");
        Location location = (Location) extras.getParcelable(GlsIntent.Extras.EXTRA_LOCATION);
        String string3 = extras.getString("label");
        String valueOf = String.valueOf(extras.getLong("request_message_id"));
        boolean z2 = extras.getBoolean("is_publicAccountMsg", false);
        if (GlsIntent.Actions.RequestIntents.CREATE_SHARE_LOCATION_INCALL.equals(intent.getAction())) {
            imsUri2 = ImsUri.parse(uri.toString());
        } else if (GlsIntent.Actions.RequestIntents.CREATE_SHARE_LOCATION_INCALL_GC.equals(intent.getAction())) {
            imsUri = ImsUri.parse("sip:anonymous@anonymous.invalid");
            z = true;
            this.mGlsModule.createInCallLocationShare(string, imsUri, NotificationStatus.toSet(string2), location, string3, valueOf, z2, z);
        } else {
            imsUri2 = null;
        }
        imsUri = imsUri2;
        z = false;
        this.mGlsModule.createInCallLocationShare(string, imsUri, NotificationStatus.toSet(string2), location, string3, valueOf, z2, z);
    }

    private void requestAcceptLocationShare(Intent intent) {
        Bundle extras = intent.getExtras();
        this.mGlsModule.acceptLocationShare(extras.getString("message_imdn_id"), extras.getString("chat_id"), (Uri) extras.getParcelable("contentUri"));
    }

    private void requestRejectLocationShare(Intent intent) {
        Bundle extras = intent.getExtras();
        this.mGlsModule.rejectLocationShare(extras.getString("message_imdn_id"), extras.getString("chat_id"));
    }

    public void requestStartLocationShareInCall(Intent intent) {
        this.mGlsModule.startLocationShareInCall(intent.getExtras().getString("message_imdn_id"));
    }

    public void onShareLocationInChatResponse(String str, String str2, String str3, boolean z) {
        long j;
        Intent intent = new Intent();
        intent.setAction(GlsIntent.Actions.ResponseIntents.SHARE_LOCATION_IN_CHAT_RESPONSE);
        intent.addCategory(GlsIntent.CATEGORY_NOTIFICATION);
        intent.putExtra("response_status", z);
        if (str2 == null) {
            j = -1;
        } else {
            j = Long.valueOf(str2).longValue();
        }
        intent.putExtra("request_message_id", j);
        intent.putExtra("chat_id", str);
        intent.putExtra("message_imdn_id", str3);
        broadcastIntent(intent, true, this.mGlsModule.getPhoneIdByChatId(str));
    }

    public void onReceiveShareLocationInChatResponse(String str, String str2, String str3, boolean z, IMnoStrategy.StrategyResponse strategyResponse, IMnoStrategy iMnoStrategy, Result result) {
        long j;
        Intent intent = new Intent();
        intent.setAction(GlsIntent.Actions.ResponseIntents.RECEIVE_SHARE_LOCATION_IN_CHAT_RESPONSE);
        intent.addCategory(GlsIntent.CATEGORY_NOTIFICATION);
        intent.putExtra("response_status", z);
        if (str2 == null) {
            j = -1;
        } else {
            j = Long.valueOf(str2).longValue();
        }
        intent.putExtra("request_message_id", j);
        intent.putExtra("chat_id", str);
        intent.putExtra("message_imdn_id", str3);
        if (strategyResponse != null) {
            intent.putExtra(ImIntent.Extras.REQUIRED_ACTION, getRequiredAction(strategyResponse.getStatusCode()));
            if (strategyResponse.getStatusCode() == IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY || strategyResponse.getStatusCode() == IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY_CFS) {
                intent.putExtra(ImIntent.Extras.ERROR_REASON, ImErrorReason.FRAMEWORK_ERROR_FALLBACKFAILED.toString());
            }
        }
        if (!(iMnoStrategy == null || result == null || !iMnoStrategy.isDisplayWarnText() || result.getImError() == null)) {
            intent.putExtra(ImIntent.Extras.WARN_TEXT, result.getImError().toString());
        }
        broadcastIntent(intent, true, this.mGlsModule.getPhoneIdByChatId(str));
    }

    public void onReceiveShareLocationInChatMsg(MessageBase messageBase, boolean z) {
        Intent intent = new Intent();
        intent.setAction(GlsIntent.Actions.ResponseIntents.RECEIVE_LOCATION_SHARE_MESSAGE);
        intent.addCategory(GlsIntent.CATEGORY_NOTIFICATION);
        intent.putExtra("chat_id", messageBase.getChatId());
        intent.putExtra("message_imdn_id", messageBase.getImdnId());
        intent.putExtra("is_group_chat", z);
        intent.putExtra("message_direction", messageBase.getDirection().getId());
        String suggestion = messageBase.getSuggestion();
        if (suggestion != null) {
            try {
                JSONObject jSONObject = new JSONObject(suggestion);
                jSONObject.remove("persistent");
                suggestion = jSONObject.toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            intent.putExtra(ImIntent.Extras.SUGGESTION_TEXT, suggestion);
        }
        String maapTrafficType = messageBase.getMaapTrafficType();
        if (maapTrafficType != null) {
            intent.putExtra("maap_traffic_type", maapTrafficType);
        }
        if (messageBase.getChatbotMessagingTech() == ImConstants.ChatbotMessagingTech.STANDALONE_MESSAGING) {
            intent.putExtra(ImIntent.Extras.IS_BOT, true);
        }
        broadcastIntent(intent, true, this.mGlsModule.getPhoneIdByChatId(messageBase.getChatId()));
    }

    public void onCreateInCallLocationShareResponse(String str, String str2, String str3, boolean z) {
        long j;
        Intent intent = new Intent();
        intent.setAction(GlsIntent.Actions.ResponseIntents.CREATE_SHARE_LOCATION_INCALL_RESPONSE);
        intent.addCategory(GlsIntent.CATEGORY_NOTIFICATION);
        intent.putExtra("response_status", z);
        if (str3 == null) {
            j = -1;
        } else {
            j = Long.valueOf(str3).longValue();
        }
        intent.putExtra("request_message_id", j);
        intent.putExtra("chat_id", str);
        intent.putExtra("message_imdn_id", str2);
        broadcastIntent(intent, this.mGlsModule.getPhoneIdByChatId(str));
    }

    public void onAcceptLocationShareInCallResponse(String str, String str2, boolean z) {
        Intent intent = new Intent();
        intent.setAction(GlsIntent.Actions.ResponseIntents.ACCEPT_LOCATION_SHARE_INCALL_RESPONSE);
        intent.addCategory(GlsIntent.CATEGORY_NOTIFICATION);
        intent.putExtra("response_status", z);
        intent.putExtra("message_imdn_id", str);
        intent.putExtra("chat_id", str2);
        broadcastIntent(intent, this.mGlsModule.getPhoneIdByChatId(str2));
    }

    public void onRejectLocationShareInCallResponse(String str, String str2, boolean z) {
        Intent intent = new Intent();
        intent.setAction(GlsIntent.Actions.ResponseIntents.REJECT_LOCATION_SHARE_INCALL_RESPONSE);
        intent.addCategory(GlsIntent.CATEGORY_NOTIFICATION);
        intent.putExtra("response_status", z);
        intent.putExtra("message_imdn_id", str);
        intent.putExtra("chat_id", str2);
        broadcastIntent(intent, this.mGlsModule.getPhoneIdByChatId(str2));
    }

    public void onCancelLocationShareInCallResponse(String str, ImDirection imDirection, String str2, boolean z) {
        Intent intent = new Intent();
        intent.setAction(GlsIntent.Actions.ResponseIntents.CANCEL_LOCATION_SHARE_INCALL_RESPONSE);
        intent.addCategory(GlsIntent.CATEGORY_NOTIFICATION);
        intent.putExtra("response_status", z);
        intent.putExtra("message_imdn_id", str);
        intent.putExtra("message_direction", imDirection);
        intent.putExtra("chat_id", str2);
        broadcastIntent(intent, this.mGlsModule.getPhoneIdByChatId(str2));
    }

    public void onDeleteAllLocationShareResponse(boolean z, List<String> list) {
        Intent intent = new Intent();
        intent.setAction(GlsIntent.Actions.ResponseIntents.DELETE_ALL_LOCATION_SHARE_INCALL_RESPONSE);
        intent.addCategory(GlsIntent.CATEGORY_NOTIFICATION);
        intent.putExtra("response_status", z);
        int i = 0;
        if (list != null && !list.isEmpty()) {
            i = this.mGlsModule.getPhoneIdByMessageId(Integer.valueOf(list.get(0)).intValue());
        }
        broadcastIntent(intent, i);
    }

    public void onStartLocationShareInCallResponse(String str, boolean z) {
        Intent intent = new Intent();
        intent.setAction(GlsIntent.Actions.ResponseIntents.START_SHARE_LOCATION_INCALL_RESPONSE);
        intent.addCategory(GlsIntent.CATEGORY_NOTIFICATION);
        intent.putExtra("response_status", z);
        intent.putExtra("message_imdn_id", str);
        broadcastIntent(intent, this.mGlsModule.getPhoneIdByImdnId(str, ImDirection.OUTGOING));
    }

    public void onIncomingLoactionShareInCall(FtMessage ftMessage) {
        Intent intent = new Intent();
        intent.addCategory(GlsIntent.CATEGORY_NOTIFICATION);
        intent.setAction(GlsIntent.Actions.ResponseIntents.INCOMING_LOCATION_SHARE_INCALL_INVITATION);
        intent.putExtra("message_imdn_id", ftMessage.getImdnId());
        intent.putExtra("chat_id", ftMessage.getChatId());
        intent.putExtra("contactUri", ftMessage.getRemoteUri());
        if (ftMessage.getChatbotMessagingTech() == ImConstants.ChatbotMessagingTech.STANDALONE_MESSAGING) {
            intent.putExtra(ImIntent.Extras.IS_BOT, true);
        }
        broadcastIntent(intent, this.mGlsModule.getPhoneIdByChatId(ftMessage.getChatId()));
    }

    public void onLocationShareInCallCompleted(String str, ImDirection imDirection, String str2, boolean z) {
        Intent intent = new Intent();
        intent.setAction(GlsIntent.Actions.ResponseIntents.RECEIVE_LOCATION_SHARE_MESSAGE);
        intent.addCategory(GlsIntent.CATEGORY_NOTIFICATION);
        intent.putExtra("response_status", z);
        intent.putExtra("message_imdn_id", str);
        intent.putExtra("message_direction", imDirection.getId());
        intent.putExtra("chat_id", str2);
        broadcastIntent(intent, true, this.mGlsModule.getPhoneIdByChatId(str2));
    }

    public void onLocationShareInCallCompleted(MessageBase messageBase, boolean z) {
        if (messageBase == null) {
            Log.e(LOG_TAG, "onLocationShareInCallCompleted: msg is null, skip");
            return;
        }
        Intent intent = new Intent();
        if (messageBase.getDirection() == ImDirection.INCOMING) {
            intent.setAction(GlsIntent.Actions.ResponseIntents.RECEIVE_LOCATION_SHARE_MESSAGE);
        } else {
            intent.setAction(GlsIntent.Actions.ResponseIntents.SENT_LOCATION_SHARE_MESSAGE);
        }
        intent.addCategory(GlsIntent.CATEGORY_NOTIFICATION);
        intent.putExtra("response_status", z);
        intent.putExtra("message_imdn_id", messageBase.getImdnId());
        intent.putExtra("message_direction", messageBase.getDirection().getId());
        intent.putExtra("request_message_id", messageBase.getRequestMessageId() == null ? -1 : Long.valueOf(messageBase.getRequestMessageId()).longValue());
        intent.putExtra("chat_id", messageBase.getChatId());
        String maapTrafficType = messageBase.getMaapTrafficType();
        if (maapTrafficType != null) {
            intent.putExtra("maap_traffic_type", maapTrafficType);
        }
        broadcastIntent(intent, true, this.mGlsModule.getPhoneIdByChatId(messageBase.getChatId()));
    }

    public void onImdnNotificationReceived(MessageBase messageBase, boolean z) {
        Intent intent = new Intent();
        intent.addCategory(GlsIntent.CATEGORY_NOTIFICATION);
        intent.setAction(GlsIntent.Actions.ResponseIntents.RECEIVE_LOCATION_NOTIFICATION_STATUS);
        intent.putExtra("message_imdn_id", messageBase.getImdnId());
        intent.putExtra("chat_id", messageBase.getChatId());
        intent.putExtra("message_notification_status", messageBase.getNotificationStatus().getId());
        intent.putExtra(ImIntent.Extras.MESSAGE_NOTIFICATION_STATUS_RECEIVED, messageBase.getLastNotificationType().getId());
        intent.putExtra("is_group_chat", z);
        broadcastIntent(intent, true, this.mGlsModule.getPhoneIdByChatId(messageBase.getChatId()));
    }

    private void broadcastIntent(Intent intent, int i) {
        broadcastIntent(intent, false, i);
    }

    private void broadcastIntent(Intent intent, boolean z, int i) {
        String str = LOG_TAG;
        Log.i(str, "broadcastIntent: " + intent.toString() + intent.getExtras());
        if (z) {
            intent.addFlags(LogClass.SIM_EVENT);
        }
        IMnoStrategy rcsStrategy = RcsPolicyManager.getRcsStrategy(i);
        UserHandle subscriptionUserHandle = TelephonyUtilsWrapper.getSubscriptionUserHandle(this.mContext, SimUtil.getSubId(i));
        if (rcsStrategy != null && rcsStrategy.isBMode(true)) {
            IntentUtil.sendBroadcast(this.mContext, intent, ContextExt.OWNER, "com.samsung.rcs.im.READ_PERMISSION");
        } else if (subscriptionUserHandle != null) {
            IntentUtil.sendBroadcast(this.mContext, intent, subscriptionUserHandle, "com.samsung.rcs.im.READ_PERMISSION");
        } else {
            IntentUtil.sendBroadcast(this.mContext, intent, ContextExt.CURRENT_OR_SELF, "com.samsung.rcs.im.READ_PERMISSION");
        }
    }

    public void onFileTransferAttached(FtMessage ftMessage) {
        Log.i(LOG_TAG, "onFileTransferAttached: call onOutgoingTransferAttached");
        this.mGlsModule.onOutgoingTransferAttached(ftMessage);
    }

    public void onFileTransferReceived(FtMessage ftMessage) {
        Log.i(LOG_TAG, "onFileTransferReceived: call onIncomingTransferUndecided");
        this.mGlsModule.onIncomingTransferUndecided(ftMessage);
    }

    public void onTransferCompleted(FtMessage ftMessage) {
        Log.i(LOG_TAG, "onTransferCompleted: call onTransferCompleted");
        this.mGlsModule.onTransferCompleted(ftMessage);
    }

    public void onTransferCanceled(FtMessage ftMessage) {
        Log.i(LOG_TAG, "onTransferCanceled: call onTransferCanceled");
        this.mGlsModule.onTransferCanceled(ftMessage);
    }

    public void onImdnNotificationReceived(FtMessage ftMessage, ImsUri imsUri, NotificationStatus notificationStatus, boolean z) {
        onImdnNotificationReceived(ftMessage, z);
    }

    public void onMessageSendResponse(MessageBase messageBase) {
        onShareLocationInChatResponse(messageBase.getChatId(), messageBase.getRequestMessageId(), messageBase.getImdnId(), true);
    }

    public void onMessageReceived(MessageBase messageBase, ImSession imSession) {
        this.mGlsModule.updateExtInfo(messageBase);
        onReceiveShareLocationInChatMsg(messageBase, imSession.isGroupChat());
    }

    public void onMessageSendingSucceeded(MessageBase messageBase) {
        onReceiveShareLocationInChatResponse(messageBase.getChatId(), messageBase.getRequestMessageId(), messageBase.getImdnId(), true, (IMnoStrategy.StrategyResponse) null, messageBase.getRcsStrategy(), (Result) null);
    }

    public void onMessageSendingFailed(MessageBase messageBase, IMnoStrategy.StrategyResponse strategyResponse, Result result) {
        onReceiveShareLocationInChatResponse(messageBase.getChatId(), messageBase.getRequestMessageId(), messageBase.getImdnId(), false, strategyResponse, messageBase.getRcsStrategy(), result);
    }

    public void onImdnNotificationReceived(MessageBase messageBase, ImsUri imsUri, NotificationStatus notificationStatus, boolean z) {
        onImdnNotificationReceived(messageBase, z);
    }

    /* renamed from: com.sec.internal.ims.servicemodules.gls.GlsTranslation$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$ims$servicemodules$im$strategy$IMnoStrategy$StatusCode;

        /* JADX WARNING: Can't wrap try/catch for region: R(8:0|1|2|3|4|5|6|(3:7|8|10)) */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0012 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x001d */
        /* JADX WARNING: Missing exception handler attribute for start block: B:7:0x0028 */
        static {
            /*
                com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StatusCode[] r0 = com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy.StatusCode.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$sec$internal$ims$servicemodules$im$strategy$IMnoStrategy$StatusCode = r0
                com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StatusCode r1 = com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy.StatusCode.DISPLAY_ERROR     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = $SwitchMap$com$sec$internal$ims$servicemodules$im$strategy$IMnoStrategy$StatusCode     // Catch:{ NoSuchFieldError -> 0x001d }
                com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StatusCode r1 = com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r0 = $SwitchMap$com$sec$internal$ims$servicemodules$im$strategy$IMnoStrategy$StatusCode     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StatusCode r1 = com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY_CFS     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                int[] r0 = $SwitchMap$com$sec$internal$ims$servicemodules$im$strategy$IMnoStrategy$StatusCode     // Catch:{ NoSuchFieldError -> 0x0033 }
                com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StatusCode r1 = com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy.StatusCode.DISPLAY_ERROR_CFS     // Catch:{ NoSuchFieldError -> 0x0033 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0033 }
                r2 = 4
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0033 }
            L_0x0033:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.gls.GlsTranslation.AnonymousClass1.<clinit>():void");
        }
    }

    private int getRequiredAction(IMnoStrategy.StatusCode statusCode) {
        int i = AnonymousClass1.$SwitchMap$com$sec$internal$ims$servicemodules$im$strategy$IMnoStrategy$StatusCode[statusCode.ordinal()];
        if (i == 2) {
            return 1;
        }
        if (i != 3) {
            return i != 4 ? 0 : 3;
        }
        return 2;
    }
}
