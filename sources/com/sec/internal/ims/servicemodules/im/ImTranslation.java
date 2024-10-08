package com.sec.internal.ims.servicemodules.im;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.extensions.ContextExt;
import com.sec.ims.im.ImParticipantData;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.MIMEContentType;
import com.sec.internal.constants.ims.servicemodules.im.ChatData;
import com.sec.internal.constants.ims.servicemodules.im.ImCacheAction;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.constants.ims.servicemodules.im.ImIconData;
import com.sec.internal.constants.ims.servicemodules.im.ImParticipant;
import com.sec.internal.constants.ims.servicemodules.im.ImSubjectData;
import com.sec.internal.constants.ims.servicemodules.im.NotificationStatus;
import com.sec.internal.constants.ims.servicemodules.im.RoutingType;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImErrorReason;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImSessionClosedReason;
import com.sec.internal.constants.ims.servicemodules.im.result.Result;
import com.sec.internal.helper.Preconditions;
import com.sec.internal.helper.PublicAccountUri;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.UriUtil;
import com.sec.internal.helper.os.IntentUtil;
import com.sec.internal.helper.os.TelephonyUtilsWrapper;
import com.sec.internal.ims.servicemodules.im.ImSession;
import com.sec.internal.ims.servicemodules.im.interfaces.ImIntent;
import com.sec.internal.ims.servicemodules.im.listener.IChatEventListener;
import com.sec.internal.ims.servicemodules.im.listener.IImCacheActionListener;
import com.sec.internal.ims.servicemodules.im.listener.IMessageEventListener;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.log.IMSLog;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ImTranslation extends TranslationBase implements IChatEventListener, IMessageEventListener, IImCacheActionListener {
    private static final String LOG_TAG = "ImTranslation";
    private final Context mContext;
    private final ImModule mImModule;
    private final ImProcessor mImProcessor;
    private final ImSessionProcessor mImSessionProcessor;

    public void onChatUpdateState(String str, ImDirection imDirection, ImSession.SessionState sessionState) {
    }

    public void onParticipantsAdded(ImSession imSession, Collection<ImParticipant> collection) {
    }

    public void onParticipantsJoined(ImSession imSession, Collection<ImParticipant> collection) {
    }

    public void onParticipantsLeft(ImSession imSession, Collection<ImParticipant> collection) {
    }

    public ImTranslation(Context context, ImModule imModule, ImSessionProcessor imSessionProcessor, ImProcessor imProcessor) {
        Log.i(LOG_TAG, "Create ImTranslation.");
        this.mContext = context;
        this.mImModule = imModule;
        imModule.registerChatEventListener(this);
        imModule.registerMessageEventListener(ImConstants.Type.TEXT, this);
        imModule.registerMessageEventListener(ImConstants.Type.TEXT_PUBLICACCOUNT, this);
        this.mImSessionProcessor = imSessionProcessor;
        this.mImProcessor = imProcessor;
    }

    /* JADX WARNING: Removed duplicated region for block: B:10:0x00ae  */
    /* JADX WARNING: Removed duplicated region for block: B:12:0x00b4  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void requestCreateChat(android.content.Intent r21) {
        /*
            r20 = this;
            r0 = r20
            android.os.Bundle r1 = r21.getExtras()
            java.lang.String r2 = "participants_list"
            java.util.ArrayList r2 = r1.getStringArrayList(r2)
            java.lang.String r3 = "subject"
            java.lang.String r7 = r1.getString(r3)
            java.lang.String r3 = "content_type"
            java.lang.String r8 = r1.getString(r3)
            java.lang.String r3 = "request_thread_id"
            r4 = -1
            int r9 = r1.getInt(r3, r4)
            java.lang.String r3 = "request_message_id"
            long r3 = r1.getLong(r3)
            java.lang.String r10 = java.lang.String.valueOf(r3)
            java.lang.String r3 = "is_broadcast_msg"
            r4 = 0
            boolean r11 = r1.getBoolean(r3, r4)
            java.lang.String r3 = "is_closed_group_chat"
            boolean r12 = r1.getBoolean(r3, r4)
            java.lang.String r3 = "is_token_used"
            boolean r15 = r1.getBoolean(r3, r4)
            java.lang.String r3 = "is_token_link"
            boolean r16 = r1.getBoolean(r3, r4)
            java.lang.String r3 = "sim_slot_id"
            java.lang.String r3 = r1.getString(r3)
            java.lang.String r5 = "groupchat_icon_name"
            java.lang.String r13 = r1.getString(r5)
            java.lang.String r5 = "groupchat_icon_uri"
            android.os.Parcelable r5 = r1.getParcelable(r5)
            r14 = r5
            android.net.Uri r14 = (android.net.Uri) r14
            java.lang.String r5 = "conversation_id"
            java.lang.String r17 = r1.getString(r5)
            java.lang.String r5 = "contribution_id"
            java.lang.String r18 = r1.getString(r5)
            java.lang.String r5 = "session_uri"
            java.lang.String r1 = r1.getString(r5)
            boolean r5 = android.text.TextUtils.isEmpty(r3)
            if (r5 != 0) goto L_0x0094
            java.lang.Integer r5 = java.lang.Integer.valueOf(r3)     // Catch:{ NumberFormatException -> 0x007e }
            int r3 = r5.intValue()     // Catch:{ NumberFormatException -> 0x007e }
            r5 = r3
            goto L_0x0095
        L_0x007e:
            java.lang.String r5 = LOG_TAG
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            java.lang.String r4 = "Invalid slot id : "
            r6.append(r4)
            r6.append(r3)
            java.lang.String r3 = r6.toString()
            android.util.Log.e(r5, r3)
        L_0x0094:
            r5 = 0
        L_0x0095:
            java.lang.String r3 = LOG_TAG
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r6 = "requestCreateChat() phoneId = "
            r4.append(r6)
            r4.append(r5)
            java.lang.String r4 = r4.toString()
            android.util.Log.i(r3, r4)
            if (r2 != 0) goto L_0x00b4
            com.sec.internal.constants.ims.servicemodules.im.reason.ImErrorReason r1 = com.sec.internal.constants.ims.servicemodules.im.reason.ImErrorReason.INVALID
            r0.onCreateChatFailed(r5, r9, r1, r10)
            return
        L_0x00b4:
            com.sec.internal.ims.servicemodules.im.ImSessionProcessor r4 = r0.mImSessionProcessor
            java.util.ArrayList r6 = com.sec.internal.helper.UriUtil.convertToUriList(r2)
            boolean r0 = android.text.TextUtils.isEmpty(r1)
            if (r0 == 0) goto L_0x00c2
            r0 = 0
            goto L_0x00c6
        L_0x00c2:
            com.sec.ims.util.ImsUri r0 = com.sec.ims.util.ImsUri.parse(r1)
        L_0x00c6:
            r19 = r0
            r4.createChat(r5, r6, r7, r8, r9, r10, r11, r12, r13, r14, r15, r16, r17, r18, r19)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.ImTranslation.requestCreateChat(android.content.Intent):void");
    }

    private void requestAddParticipantsToChat(Intent intent) {
        Log.i(LOG_TAG, "requestAddParticipantsToChat()");
        Bundle extras = intent.getExtras();
        String string = extras.getString("chat_id");
        List<String> arrayList = getArrayList(extras, ImIntent.Extras.PARTICIPANTS_LIST);
        if (arrayList == null || arrayList.isEmpty()) {
            onAddParticipantsFailed(string, (Collection<ImsUri>) null, ImErrorReason.INVALID);
        } else {
            this.mImModule.addParticipants(string, UriUtil.convertToUriList(arrayList));
        }
    }

    private void requestRemoveGroupChatParticipants(Intent intent) {
        Log.i(LOG_TAG, "requestRemoveGroupChatParticipants()");
        Bundle extras = intent.getExtras();
        this.mImSessionProcessor.removeParticipants(extras.getString("chat_id"), UriUtil.convertToUriList(getArrayList(extras, ImIntent.Extras.PARTICIPANTS_LIST)));
    }

    private void requestChangeGroupChatLeader(Intent intent) {
        Log.i(LOG_TAG, "requestChangeGroupChatLeader()");
        Bundle extras = intent.getExtras();
        this.mImSessionProcessor.changeGroupChatLeader(extras.getString("chat_id"), UriUtil.convertToUriList(getArrayList(extras, ImIntent.Extras.PARTICIPANTS_LIST)));
    }

    private void requestChangeGroupChatSubject(Intent intent) {
        Log.i(LOG_TAG, "requestChangeGroupChatSubject()");
        Bundle extras = intent.getExtras();
        this.mImSessionProcessor.changeGroupChatSubject(extras.getString("chat_id"), extras.getString("subject"));
    }

    private void requestChangeGroupChatIcon(Intent intent) {
        Log.i(LOG_TAG, "requestChangeGroupChatIcon()");
        Bundle extras = intent.getExtras();
        this.mImSessionProcessor.changeGroupChatIcon(extras.getString("chat_id"), extras.getString(ImIntent.Extras.GROUPCHAT_ICON_NAME), (Uri) extras.getParcelable(ImIntent.Extras.GROUPCHAT_ICON_URI));
    }

    private void requestDeleteGroupChatIcon(Intent intent) {
        Log.i(LOG_TAG, "requestDeleteGroupChatIcon()");
        this.mImSessionProcessor.changeGroupChatIcon(intent.getExtras().getString("chat_id"), (String) null, (Uri) null);
    }

    private void requestChangeGroupAlias(Intent intent) {
        Log.i(LOG_TAG, "requestChangeGroupAlias()");
        Bundle extras = intent.getExtras();
        this.mImSessionProcessor.changeGroupAlias(extras.getString("chat_id"), extras.getString("user_alias"));
    }

    private void requestReportMessage(Intent intent) {
        Log.i(LOG_TAG, "requestReportMessage");
        Bundle extras = intent.getExtras();
        this.mImProcessor.reportMessages(getArrayList(extras, ImIntent.Extras.MESSAGES_IMDN_ID_LIST), extras.getString("chat_id"));
    }

    private void requestDeliveryTimeout(Intent intent) {
        String string = intent.getExtras().getString("chat_id");
        String str = LOG_TAG;
        Log.i(str, "requestDeliveryTimeout() chatId:" + string);
        this.mImSessionProcessor.receiveDeliveryTimeout(string);
    }

    public void onMessageReportResponse(String str, String str2, boolean z) {
        String str3 = LOG_TAG;
        Log.i(str3, "onMessageReportResponse, imdnId=" + str + ", res=" + str);
        Intent intent = new Intent(ImIntent.Action.REPORT_MESSAGES_RESPONSE);
        intent.addCategory(ImIntent.CATEGORY_ACTION);
        intent.putExtra("message_imdn_id", str);
        intent.putExtra("chat_id", str2);
        intent.putExtra("response_status", z);
        broadcastIntent(intent, this.mImModule.getPhoneIdByChatId(str2));
    }

    private void requestIgnoreIncomingMsgSet(Intent intent) {
        Log.i(LOG_TAG, "requestIgnoreIncomingMsgSet");
        Bundle extras = intent.getExtras();
        this.mImSessionProcessor.ignoreIncomingMsgSet(extras.getString("chat_id"), extras.getBoolean(ImIntent.Extras.IS_IGNORE_INCOMING_MSG, false));
    }

    public void onIgnoreIncomingMsgSetResponse(String str, boolean z) {
        Log.i(LOG_TAG, "onIgnoreIncomingMsgSetResponse");
        Intent intent = new Intent();
        intent.setAction(ImIntent.Action.IGNORE_INCOMING_MESSAGE_RESPONSE);
        intent.addCategory(ImIntent.CATEGORY_ACTION);
        intent.putExtra("chat_id", str);
        intent.putExtra("response_status", z);
        broadcastIntent(intent, this.mImModule.getPhoneIdByChatId(str));
    }

    private void requestSendMessage(Intent intent) {
        String str = LOG_TAG;
        Log.i(str, "requestSendMessage()");
        Bundle extras = intent.getExtras();
        String string = extras.getString("chat_id");
        String string2 = extras.getString(ImIntent.Extras.MESSAGE_BODY);
        String string3 = extras.getString("disposition_notification");
        String string4 = extras.getString("content_type", MIMEContentType.PLAIN_TEXT);
        String valueOf = String.valueOf(extras.getLong("request_message_id"));
        int i = extras.getInt(ImIntent.Extras.MESSAGE_NUMBER);
        boolean z = extras.getBoolean("is_broadcast_msg", false);
        boolean z2 = extras.getBoolean("is_publicAccountMsg", false);
        if (z2) {
            PublicAccountUri.setPublicAccountDomain(extras.getString("publicAccount_Send_Domain"));
        }
        List<String> arrayList = getArrayList(extras, ImIntent.Extras.GROUP_CCUSER_LIST);
        boolean z3 = extras.getBoolean("is_temporary", false);
        String string5 = extras.getString("maap_traffic_type");
        String string6 = extras.getString(ImIntent.Extras.REFERENCE_MESSAGE_IMDN_ID);
        String string7 = extras.getString(ImIntent.Extras.REFERENCE_MESSAGE_TYPE);
        String string8 = extras.getString(ImIntent.Extras.REFERENCE_MESSAGE_VALUE);
        if (string5 != null) {
            Log.i(str, "requestSendMessage, maapTrafficType = [" + string5 + "]");
        }
        ImModule imModule = this.mImModule;
        Set<NotificationStatus> set = NotificationStatus.toSet(string3);
        ArrayList<ImsUri> convertToUriList = UriUtil.convertToUriList(arrayList);
        imModule.sendMessage(string, string2, set, string4, valueOf, i, z, z2, false, convertToUriList, z3, string5, string6, string7, string8);
    }

    private void requestReadMessage(Intent intent) {
        Bundle extras = intent.getExtras();
        this.mImSessionProcessor.readMessages(extras.getString("chat_id"), getArrayList(extras, ImIntent.Extras.MESSAGES_IMDN_ID_LIST), extras.getBoolean(ImIntent.Extras.UPDATE_ONLY_MSTORE, false));
    }

    private void requestCancelMessage(Intent intent) {
        Bundle extras = intent.getExtras();
        this.mImSessionProcessor.cancelMessages(extras.getString("chat_id"), getArrayList(extras, ImIntent.Extras.MESSAGES_IMDN_ID_LIST));
    }

    private void requestSendComposingNotification(Intent intent) {
        Bundle extras = intent.getExtras();
        this.mImSessionProcessor.sendComposingNotification(extras.getString("chat_id"), extras.getInt(ImIntent.Extras.INTERVAL), extras.getBoolean(ImIntent.Extras.IS_TYPING));
    }

    public void requestComposingActiveUris(Intent intent) {
        Log.i(LOG_TAG, "requestComposingActiveUris()");
        this.mImSessionProcessor.getComposingActiveUris(intent.getExtras().getString("chat_id"));
    }

    public void requestGetLastSentMessages(Intent intent) {
        List<String> arrayList = getArrayList(intent.getExtras(), ImIntent.Extras.REQUEST_MESSAGES_LIST);
        String str = LOG_TAG;
        Log.i(str, "requestGetLastSentMessages(): REQUEST_MESSAGES_LIST size:" + arrayList.size() + ", " + IMSLog.checker(intent.toString()));
        this.mImProcessor.getLastSentMessagesStatus(arrayList);
    }

    public void notifyComposingActiveUris(String str, Set<ImsUri> set) {
        Log.i(LOG_TAG, "notifyComposingActiveUris()");
        Intent intent = new Intent();
        intent.setAction(ImIntent.Action.GET_IS_COMPOSING_ACTIVE_URIS_RESPONSE);
        intent.addCategory(ImIntent.CATEGORY_ACTION);
        intent.putExtra("chat_id", str);
        ArrayList arrayList = new ArrayList();
        if (set != null) {
            arrayList = new ArrayList(set);
        }
        intent.putStringArrayListExtra(ImIntent.Extras.COMPOSING_URI_LIST, new ArrayList(UriUtil.convertToStringList(arrayList)));
        broadcastIntent(intent, true, this.mImModule.getPhoneIdByChatId(str));
    }

    public void notifyLastSentMessagesStatus(List<Bundle> list) {
        Log.i(LOG_TAG, "notifyLastSentMessagesStatus()");
        Intent intent = new Intent();
        intent.setAction(ImIntent.Action.GET_LAST_MESSAGES_SENT_RESPONSE);
        intent.addCategory(ImIntent.CATEGORY_ACTION);
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(ImIntent.Extras.LAST_SENT_MESSAGES_STATUS, list != null ? new ArrayList(list) : null);
        intent.putExtras(bundle);
        broadcastIntent(intent, true, list != null ? this.mImModule.getPhoneIdByChatId(list.get(0).getString("chat_id")) : SimUtil.getActiveDataPhoneId());
    }

    public void onRequestChatbotAnonymizeResponse(String str, boolean z, String str2, int i) {
        Log.i(LOG_TAG, "onChatbotAnonymizeNotificationReceived()");
        Intent intent = new Intent(ImIntent.Action.CHATBOT_ANONYMIZE_RESPONSE);
        intent.addCategory(ImIntent.CATEGORY_ACTION);
        Bundle bundle = new Bundle();
        bundle.putString(ImIntent.Extras.CHATBOT_ANONYMIZE_URI, str);
        bundle.putBoolean("response_status", z);
        bundle.putString(ImIntent.Extras.CHATBOT_COMMAND_ID, str2);
        if (!z && i != -1) {
            bundle.putInt(ImIntent.Extras.RETRY_AFTER, i);
        }
        intent.putExtras(bundle);
        intent.setPackage(ImsConstants.Packages.PACKAGE_SEC_MSG);
        broadcastIntent(intent, SimUtil.getActiveDataPhoneId());
    }

    public void onRequestChatbotAnonymizeNotiReceived(String str, String str2, String str3) {
        Log.i(LOG_TAG, "onChatbotAnonymizeNotificationReceived()");
        Intent intent = new Intent(ImIntent.Action.CHATBOT_ANONYMIZE_NOTIFICATION);
        intent.addCategory(ImIntent.CATEGORY_ACTION);
        Bundle bundle = new Bundle();
        bundle.putString(ImIntent.Extras.CHATBOT_ANONYMIZE_URI, str);
        bundle.putString(ImIntent.Extras.CHATBOT_ANONYMIZE_RESULT, str2);
        bundle.putString(ImIntent.Extras.CHATBOT_COMMAND_ID, str3);
        intent.putExtras(bundle);
        intent.setPackage(ImsConstants.Packages.PACKAGE_SEC_MSG);
        broadcastIntent(intent, SimUtil.getActiveDataPhoneId());
    }

    public void onReportChatbotAsSpamRespReceived(String str, boolean z, String str2) {
        Log.i(LOG_TAG, "onReportChatbotAsSpamRespReceived()");
        Intent intent = new Intent(ImIntent.Action.REPORT_CHATBOT_AS_SPAM_RESPONSE);
        intent.addCategory(ImIntent.CATEGORY_ACTION);
        Bundle bundle = new Bundle();
        bundle.putString(ImIntent.Extras.CHATBOT_URI, str);
        bundle.putBoolean("response_status", z);
        bundle.putString(ImIntent.Extras.CHATBOT_REQUEST_ID, str2);
        intent.putExtras(bundle);
        intent.setPackage(ImsConstants.Packages.PACKAGE_SEC_MSG);
        broadcastIntent(intent, SimUtil.getActiveDataPhoneId());
    }

    private void requestDeleteAllChats() {
        this.mImSessionProcessor.deleteAllChats();
    }

    private void requestAcceptChat(Intent intent) {
        Bundle extras = intent.getExtras();
        String string = extras.getString("chat_id");
        Boolean valueOf = Boolean.valueOf(extras.getBoolean(ImIntent.Extras.IS_ACCEPT));
        this.mImSessionProcessor.acceptChat(string, valueOf.booleanValue(), extras.getInt("reason", 0));
    }

    private void requestOpenChat(Intent intent) {
        Bundle extras = intent.getExtras();
        this.mImSessionProcessor.openChat(extras.getString("chat_id"), extras.getBoolean(ImIntent.Extras.INVITATION_UI, false));
    }

    private void requestCloseChat(Intent intent) {
        Bundle extras = intent.getExtras();
        this.mImSessionProcessor.closeChat(getArrayList(extras, ImIntent.Extras.CHATS_LIST), true, extras.getBoolean(ImIntent.Extras.IS_DISMISS_GROUPCHAT, false));
    }

    private void requestAnswerGcChats(Intent intent) {
        Bundle extras = intent.getExtras();
        this.mImSessionProcessor.answerGcSession(extras.getString("chat_id"), extras.getBoolean(ImIntent.Extras.BOOLEAN_ANSWER));
    }

    private void requestDeleteChats(Intent intent) {
        Bundle extras = intent.getExtras();
        this.mImSessionProcessor.deleteChats(getArrayList(extras, ImIntent.Extras.CHATS_LIST), extras.getBoolean(ImIntent.Extras.IS_LOCAL_WIPEOUT, false));
    }

    private void requestDeleteAllMessages(Intent intent) {
        Bundle extras = intent.getExtras();
        this.mImProcessor.deleteAllMessages(getArrayList(extras, ImIntent.Extras.CHATS_LIST), extras.getBoolean(ImIntent.Extras.IS_LOCAL_WIPEOUT, false));
    }

    private void requestDeleteMessages(Intent intent) {
        Bundle extras = intent.getExtras();
        String string = extras.getString("chat_id");
        boolean z = extras.getBoolean(ImIntent.Extras.IS_LOCAL_WIPEOUT, false);
        this.mImModule.deleteMessagesByImdnId((Map) extras.getSerializable(ImIntent.Extras.MESSAGES_IMDN_DIR_MAP), string, z);
    }

    private void requestMessageRevocation(Intent intent) {
        Bundle extras = intent.getExtras();
        String string = extras.getString("chat_id");
        String str = LOG_TAG;
        Log.i(str, "requestMessageRevocation(): chatId = " + string);
        boolean z = extras.getBoolean(ImIntent.Extras.USER_SELECT_RESULT);
        int i = extras.getInt(ImIntent.Extras.USER_SELECT_MESSAGE_TYPE, 3);
        String string2 = extras.getString("message_imdn_id");
        this.mImModule.revokeMessage(string, string2 != null ? new ArrayList(Collections.singletonList(string2)) : null, z, i);
    }

    private void requestChatbotAnonymize(Intent intent) {
        int i;
        Bundle extras = intent.getExtras();
        String string = extras.getString(ImIntent.Extras.CHATBOT_ANONYMIZE_URI);
        String string2 = extras.getString(ImIntent.Extras.CHATBOT_ANONYMIZE_ACTION);
        String string3 = extras.getString(ImIntent.Extras.CHATBOT_COMMAND_ID);
        String string4 = extras.getString("sim_slot_id");
        if (!TextUtils.isEmpty(string4)) {
            try {
                i = Integer.valueOf(string4).intValue();
            } catch (NumberFormatException unused) {
                String str = LOG_TAG;
                Log.e(str, "Invalid slot id : " + string4);
            }
            String str2 = LOG_TAG;
            Log.i(str2, "requestChatbotAnonymize() phoneId = " + i + ", uri = " + IMSLog.checker(string) + ", action = " + string2);
            this.mImModule.requestChatbotAnonymize(i, ImsUri.parse(string), string2, string3);
        }
        i = 0;
        String str22 = LOG_TAG;
        Log.i(str22, "requestChatbotAnonymize() phoneId = " + i + ", uri = " + IMSLog.checker(string) + ", action = " + string2);
        this.mImModule.requestChatbotAnonymize(i, ImsUri.parse(string), string2, string3);
    }

    private void reportChatbotAsSpam(Intent intent) {
        int i;
        Bundle extras = intent.getExtras();
        String string = extras.getString(ImIntent.Extras.CHATBOT_URI);
        String string2 = extras.getString(ImIntent.Extras.CHATBOT_REQUEST_ID);
        String string3 = extras.getString("sim_slot_id");
        String string4 = extras.getString(ImIntent.Extras.CHATBOT_SPAM_TYPE);
        String string5 = extras.getString(ImIntent.Extras.CHATBOT_FREE_TEXT);
        List<String> arrayList = getArrayList(extras, ImIntent.Extras.MESSAGES_ID_LIST);
        if (!TextUtils.isEmpty(string3)) {
            try {
                i = Integer.valueOf(string3).intValue();
            } catch (NumberFormatException unused) {
                String str = LOG_TAG;
                Log.e(str, "Invalid slot id : " + string3);
            }
            int i2 = i;
            String str2 = LOG_TAG;
            Log.i(str2, "reportChatbotAsSpam() phoneId = " + i2 + ", uri = " + IMSLog.checker(string));
            this.mImModule.reportChatbotAsSpam(i2, string2, ImsUri.parse(string), arrayList, string4, string5);
        }
        i = 0;
        int i22 = i;
        String str22 = LOG_TAG;
        Log.i(str22, "reportChatbotAsSpam() phoneId = " + i22 + ", uri = " + IMSLog.checker(string));
        this.mImModule.reportChatbotAsSpam(i22, string2, ImsUri.parse(string), arrayList, string4, string5);
    }

    private List<String> getArrayList(Bundle bundle, String str) {
        String[] stringArray;
        Preconditions.checkNotNull(bundle, "extras is null");
        Preconditions.checkNotNull(str, "key is null");
        List<String> stringArrayList = bundle.getStringArrayList(str);
        if (stringArrayList == null && (stringArray = bundle.getStringArray(str)) != null) {
            stringArrayList = Arrays.asList(stringArray);
        }
        return stringArrayList != null ? stringArrayList : Collections.emptyList();
    }

    public void onCreateChatSucceeded(ImSession imSession) {
        String str = LOG_TAG;
        Log.i(str, "onCreateChatSucceeded(), notify, chat : " + imSession.getChatId());
        Intent intent = new Intent();
        intent.setAction(ImIntent.Action.CREATE_CHAT_RESPONSE);
        intent.addCategory(ImIntent.CATEGORY_ACTION);
        intent.putExtra("response_status", true);
        intent.putExtra("chat_id", imSession.getChatId());
        intent.putStringArrayListExtra(ImIntent.Extras.PARTICIPANTS_LIST, new ArrayList(imSession.getParticipantsString()));
        intent.putExtra(ImIntent.Extras.REQUEST_THREAD_ID, imSession.getThreadId());
        intent.putExtra("subject", imSession.getSubject());
        intent.putExtra("request_message_id", imSession.getRequestMessageId() == null ? -1 : Long.valueOf(imSession.getRequestMessageId()).longValue());
        intent.putExtra("sim_slot_id", this.mImModule.getPhoneIdByIMSI(imSession.getOwnImsi()));
        String str2 = null;
        intent.putExtra("conversation_id", imSession.isGroupChat() ? imSession.getChatData().getConversationId() : null);
        if (imSession.isGroupChat()) {
            str2 = imSession.getChatData().getContributionId();
        }
        intent.putExtra("contribution_id", str2);
        broadcastIntent(intent, true, imSession.getPhoneId());
    }

    public void onCreateChatFailed(int i, int i2, ImErrorReason imErrorReason, String str) {
        long j;
        String str2 = LOG_TAG;
        Log.i(str2, "onCreateChatFailed(), notifyError : " + imErrorReason);
        Intent intent = new Intent();
        intent.setAction(ImIntent.Action.CREATE_CHAT_RESPONSE);
        intent.addCategory(ImIntent.CATEGORY_ACTION);
        intent.putExtra("response_status", false);
        intent.putExtra(ImIntent.Extras.ERROR_REASON, imErrorReason.toString());
        intent.putExtra(ImIntent.Extras.REQUEST_THREAD_ID, i2);
        if (str == null) {
            j = -1;
        } else {
            j = Long.valueOf(str).longValue();
        }
        intent.putExtra("request_message_id", j);
        intent.putExtra("sim_slot_id", String.valueOf(i));
        broadcastIntent(intent, true, i);
    }

    public void onMessageSendingSucceeded(MessageBase messageBase) {
        Log.i(LOG_TAG, "onMessageSendingSucceeded");
        Preconditions.checkNotNull(messageBase, "message is null");
        Intent intent = new Intent(ImIntent.Action.RECEIVE_SEND_MESSAGE_RESPONSE);
        intent.addCategory(ImIntent.CATEGORY_ACTION);
        intent.putExtra("message_imdn_id", messageBase.getImdnId());
        intent.putExtra("message_id", Long.valueOf((long) messageBase.getId()));
        intent.putExtra("response_status", true);
        intent.putExtra("request_message_id", messageBase.getRequestMessageId() == null ? -1 : Long.valueOf(messageBase.getRequestMessageId()).longValue());
        intent.putExtra("is_broadcast_msg", messageBase.isBroadcastMsg());
        if (messageBase.getReferenceImdnId() != null) {
            intent.putExtra(ImIntent.Extras.REFERENCE_MESSAGE_IMDN_ID, messageBase.getReferenceImdnId());
        }
        if (messageBase.getReferenceType() != null) {
            intent.putExtra(ImIntent.Extras.REFERENCE_MESSAGE_TYPE, messageBase.getReferenceType());
        }
        if (messageBase.getReferenceValue() != null) {
            intent.putExtra(ImIntent.Extras.REFERENCE_MESSAGE_VALUE, messageBase.getReferenceValue());
        }
        broadcastIntent(intent, true, messageBase.getPhoneId());
    }

    public void onMessageSendingFailed(MessageBase messageBase, IMnoStrategy.StrategyResponse strategyResponse, Result result) {
        Log.i(LOG_TAG, "onMessageSendingFailed()");
        Preconditions.checkNotNull(messageBase, "message is null");
        broadcastIntent(createMessageSendingFailedIntent(messageBase, strategyResponse, result), true, messageBase.getPhoneId());
    }

    public void onComposingNotificationReceived(String str, boolean z, ImsUri imsUri, String str2, boolean z2, int i) {
        String str3;
        Log.i(LOG_TAG, "onComposingNotificationReceived");
        Intent intent = new Intent(ImIntent.Action.RECEIVE_TYPING_NOTIFICATION);
        intent.addCategory(ImIntent.CATEGORY_ACTION);
        intent.putExtra("chat_id", str);
        if (imsUri == null) {
            str3 = "";
        } else {
            str3 = imsUri.toString();
        }
        intent.putExtra("participant", str3);
        intent.putExtra(ImIntent.Extras.INTERVAL, i);
        intent.putExtra(ImIntent.Extras.IS_TYPING, z2);
        if (!TextUtils.isEmpty(str2)) {
            intent.putExtra("user_alias", str2);
        }
        broadcastIntent(intent, true, this.mImModule.getPhoneIdByChatId(str));
    }

    public void onDeviceOutOfMemory() {
        Log.i(LOG_TAG, "onDeviceOutOfMemory()");
        Intent intent = new Intent(ImIntent.Action.OUT_OF_MEMORY_ERROR);
        intent.addCategory(ImIntent.CATEGORY_ACTION);
        broadcastIntent(intent, SimUtil.getActiveDataPhoneId());
    }

    public void onChatEstablished(String str, ImDirection imDirection, ImsUri imsUri, List<String> list, List<String> list2) {
        String str2;
        Log.i(LOG_TAG, "onChatEstablished()");
        Preconditions.checkNotNull(str);
        Intent intent = new Intent(ImIntent.Action.RECEIVE_CHAT_ESTABLISHED);
        intent.addCategory(ImIntent.CATEGORY_ACTION);
        intent.putExtra("chat_id", str);
        if (imsUri == null) {
            str2 = null;
        } else {
            str2 = imsUri.toString();
        }
        intent.putExtra("session_uri", str2);
        ArrayList arrayList = new ArrayList();
        if (list != null && !list.isEmpty()) {
            arrayList.addAll(list);
        }
        if (list2 != null && !list2.isEmpty()) {
            arrayList.addAll(list2);
        }
        if (!arrayList.isEmpty()) {
            intent.putStringArrayListExtra(ImIntent.Extras.SUPPORTED_CONTENT_LIST, arrayList);
        }
        broadcastIntent(intent, true, this.mImModule.getPhoneIdByChatId(str));
    }

    public void onChatClosed(String str, ImDirection imDirection, ImSessionClosedReason imSessionClosedReason) {
        Log.i(LOG_TAG, "onChatClosed()");
        Preconditions.checkNotNull(str);
        Preconditions.checkNotNull(imSessionClosedReason);
        Intent intent = new Intent(ImIntent.Action.RECEIVE_CHAT_CLOSED);
        intent.addCategory(ImIntent.CATEGORY_ACTION);
        intent.putExtra("chat_id", str);
        intent.putExtra(ImIntent.Extras.CHAT_STATUS, imSessionClosedReason.name());
        broadcastIntent(intent, true, this.mImModule.getPhoneIdByChatId(str));
    }

    public void onChatSubjectUpdated(String str, ImSubjectData imSubjectData) {
        Log.i(LOG_TAG, "onChatSubjectUpdated()");
        Preconditions.checkNotNull(str);
        Intent intent = new Intent(ImIntent.Action.RECEIVE_CHAT_SUBJECT_UPDATED);
        intent.putExtra("chat_id", str);
        intent.putExtra("subject", imSubjectData.getSubject());
        String str2 = null;
        intent.putExtra("subject_participant", imSubjectData.getParticipant() != null ? imSubjectData.getParticipant().toString() : null);
        if (imSubjectData.getTimestamp() != null) {
            str2 = imSubjectData.getTimestamp().toString();
        }
        intent.putExtra("subject_timestamp", str2);
        broadcastIntent(intent, true, this.mImModule.getPhoneIdByChatId(str));
    }

    public void onGroupChatIconUpdated(String str, ImIconData imIconData) {
        Log.i(LOG_TAG, "onGroupChatIconUpdated()");
        Preconditions.checkNotNull(str);
        Intent intent = new Intent(ImIntent.Action.RECEIVE_GROUPCHAT_ICON_UPDATED);
        intent.putExtra("chat_id", str);
        String iconLocation = imIconData.getIconLocation();
        try {
            if (!TextUtils.isEmpty(iconLocation)) {
                File file = new File(iconLocation);
                if (file.exists()) {
                    intent.putExtra(ImIntent.Extras.GROUPCHAT_ICON_DATA, Files.readAllBytes(file.toPath()));
                    intent.putExtra(ImIntent.Extras.GROUPCHAT_ICON_NAME, file.getName());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        intent.putExtra(ImIntent.Extras.GROUPCHAT_ICON_PARTICIPANT, imIconData.getParticipant());
        intent.putExtra(ImIntent.Extras.GROUPCHAT_ICON_TIMESTAMP, imIconData.getTimestamp());
        broadcastIntent(intent, true, this.mImModule.getPhoneIdByChatId(str));
    }

    public void onGroupChatIconDeleted(String str) {
        Log.i(LOG_TAG, "onGroupChatIconDeleted()");
        Preconditions.checkNotNull(str);
        Intent intent = new Intent(ImIntent.Action.RECEIVE_GROUPCHAT_ICON_DELETED);
        intent.putExtra("chat_id", str);
        broadcastIntent(intent, true, this.mImModule.getPhoneIdByChatId(str));
    }

    public void onParticipantAliasUpdated(String str, ImParticipant imParticipant) {
        String str2 = LOG_TAG;
        Log.i(str2, "onParticipantAliasUpdated: " + IMSLog.numberChecker(imParticipant.getUri()));
        Preconditions.checkNotNull(str);
        Intent intent = new Intent(ImIntent.Action.RECEIVE_PARTICIPANT_ALIAS_UPDATED);
        intent.putExtra("chat_id", str);
        intent.putExtra("participant", imParticipant.getUri().toString());
        intent.putExtra(ImIntent.Extras.PARTICIPANT_ID, Long.valueOf((long) imParticipant.getId()));
        intent.putExtra("user_alias", imParticipant.getUserAlias());
        broadcastIntent(intent, true, this.mImModule.getPhoneIdByChatId(str));
    }

    public void onChatInvitationReceived(ImSession imSession) {
        String str = LOG_TAG;
        Log.i(str, "onChatInvitationReceived()");
        Preconditions.checkNotNull(imSession);
        ArrayList arrayList = new ArrayList();
        List<String> list = imSession.mRemoteAcceptTypes;
        if (list != null && !list.isEmpty()) {
            arrayList.addAll(imSession.mRemoteAcceptTypes);
        }
        List<String> list2 = imSession.mRemoteAcceptWrappedTypes;
        if (list2 != null && !list2.isEmpty()) {
            arrayList.addAll(imSession.mRemoteAcceptWrappedTypes);
        }
        String str2 = null;
        if (imSession.needToUseGroupChatInvitationUI()) {
            Intent intent = new Intent(ImIntent.Action.RECEIVE_GROUPCHAT_SESSION);
            intent.putExtra("chat_id", imSession.getChatId());
            intent.putExtra("subject", imSession.getSubject());
            intent.putExtra("content_type", imSession.getSdpContentType());
            intent.putExtra("conversation_id", imSession.isGroupChat() ? imSession.getChatData().getConversationId() : null);
            intent.putExtra("contribution_id", imSession.isGroupChat() ? imSession.getChatData().getContributionId() : null);
            if (imSession.getChatData().getSessionUri() != null) {
                str2 = imSession.getChatData().getSessionUri().toString();
            }
            intent.putExtra("session_uri", str2);
            if (imSession.getInitiator() != null) {
                intent.putExtra("remote_uri", imSession.getInitiator().toString());
            }
            if (!arrayList.isEmpty()) {
                intent.putStringArrayListExtra(ImIntent.Extras.SUPPORTED_CONTENT_LIST, arrayList);
            }
            broadcastIntent(intent, true, imSession.getPhoneId());
            return;
        }
        Intent intent2 = new Intent(ImIntent.Action.RECEIVE_CHAT_INVITATION);
        intent2.addCategory(ImIntent.CATEGORY_ACTION);
        intent2.putExtra("chat_id", imSession.getChatId());
        intent2.putExtra("subject", imSession.getSubject());
        intent2.putExtra("content_type", imSession.getSdpContentType());
        intent2.putExtra("user_alias", imSession.getInitiatorAlias());
        intent2.putExtra(ImIntent.Extras.IS_TOKEN_USED, imSession.getIsTokenUsed());
        intent2.putExtra("conversation_id", imSession.isGroupChat() ? imSession.getChatData().getConversationId() : null);
        intent2.putExtra("contribution_id", imSession.isGroupChat() ? imSession.getChatData().getContributionId() : null);
        if (imSession.getChatData().getSessionUri() != null) {
            str2 = imSession.getChatData().getSessionUri().toString();
        }
        intent2.putExtra("session_uri", str2);
        intent2.putExtra(ImIntent.Extras.IS_CLOSED_GROUP_CHAT, ChatData.ChatType.isClosedGroupChat(imSession.getChatType()));
        if (imSession.isChatbotManualAcceptUsed()) {
            intent2.putExtra(ImIntent.Extras.IS_BOT, true);
            if (imSession.getInitiator() != null) {
                Log.i(str, "session.getInitiator=" + IMSLog.numberChecker(imSession.getInitiator()));
                intent2.putExtra(ImIntent.Extras.SERVICE_ID, imSession.getInitiator().toString());
            }
        } else {
            intent2.putExtra(ImIntent.Extras.IS_BOT, false);
        }
        if (imSession.isGroupChat()) {
            if (imSession.getInitiator() != null) {
                intent2.putExtra("remote_uri", imSession.getInitiator().toString());
            }
            if (imSession.getCreatedBy() != null) {
                intent2.putExtra("created_by", imSession.getCreatedBy().toString());
            }
            if (imSession.getInvitedBy() != null) {
                intent2.putExtra("invited_by", imSession.getInvitedBy().toString());
            }
        }
        if (!arrayList.isEmpty()) {
            intent2.putStringArrayListExtra(ImIntent.Extras.SUPPORTED_CONTENT_LIST, arrayList);
        }
        broadcastIntent(intent2, true, imSession.getPhoneId());
    }

    public void onAddParticipantsSucceeded(String str, Collection<ImsUri> collection) {
        Log.i(LOG_TAG, "onAddParticipantsSucceeded()");
        Preconditions.checkNotNull(collection);
        Intent intent = new Intent();
        intent.setAction(ImIntent.Action.ADD_PARTICIPANTS_RESPONSE);
        intent.addCategory(ImIntent.CATEGORY_ACTION);
        intent.putExtra("response_status", true);
        intent.putExtra("chat_id", str);
        intent.putStringArrayListExtra(ImIntent.Extras.PARTICIPANTS_LIST, UriUtil.convertToStringList(collection));
        broadcastIntent(intent, true, this.mImModule.getPhoneIdByChatId(str));
    }

    public void onAddParticipantsFailed(String str, Collection<ImsUri> collection, ImErrorReason imErrorReason) {
        Intent intent = new Intent();
        intent.setAction(ImIntent.Action.ADD_PARTICIPANTS_RESPONSE);
        intent.addCategory(ImIntent.CATEGORY_ACTION);
        intent.putExtra("response_status", false);
        intent.putExtra("chat_id", str);
        intent.putExtra(ImIntent.Extras.ERROR_REASON, imErrorReason.toString());
        if (collection != null) {
            intent.putStringArrayListExtra(ImIntent.Extras.PARTICIPANTS_LIST, UriUtil.convertToStringList(collection));
        }
        broadcastIntent(intent, true, this.mImModule.getPhoneIdByChatId(str));
    }

    public void onRemoveParticipantsSucceeded(String str, Collection<ImsUri> collection) {
        Log.i(LOG_TAG, "onRemoveParticipantsSucceeded()");
        Preconditions.checkNotNull(collection);
        Intent intent = new Intent();
        intent.setAction(ImIntent.Action.REMOVE_PARTICIPANTS_RESPONSE);
        intent.addCategory(ImIntent.CATEGORY_ACTION);
        intent.putExtra("response_status", true);
        intent.putExtra("chat_id", str);
        intent.putStringArrayListExtra(ImIntent.Extras.PARTICIPANTS_LIST, UriUtil.convertToStringList(collection));
        broadcastIntent(intent, true, this.mImModule.getPhoneIdByChatId(str));
    }

    public void onRemoveParticipantsFailed(String str, Collection<ImsUri> collection, ImErrorReason imErrorReason) {
        Log.i(LOG_TAG, "onRemoveParticipantsFailed()");
        Intent intent = new Intent();
        intent.setAction(ImIntent.Action.REMOVE_PARTICIPANTS_RESPONSE);
        intent.addCategory(ImIntent.CATEGORY_ACTION);
        intent.putExtra("response_status", false);
        intent.putExtra("chat_id", str);
        intent.putExtra(ImIntent.Extras.ERROR_REASON, imErrorReason.toString());
        intent.putStringArrayListExtra(ImIntent.Extras.PARTICIPANTS_LIST, UriUtil.convertToStringList(collection));
        broadcastIntent(intent, true, this.mImModule.getPhoneIdByChatId(str));
    }

    public void onChangeGroupChatLeaderSucceeded(String str, List<ImsUri> list) {
        Log.i(LOG_TAG, "onChangeGroupChatLeaderSucceeded()");
        Intent intent = new Intent();
        intent.setAction(ImIntent.Action.CHANGE_GROUPCHAT_LEADER_RESPONSE);
        intent.addCategory(ImIntent.CATEGORY_ACTION);
        intent.putExtra("response_status", true);
        intent.putExtra("chat_id", str);
        intent.putStringArrayListExtra(ImIntent.Extras.PARTICIPANTS_LIST, UriUtil.convertToStringList(list));
        broadcastIntent(intent, this.mImModule.getPhoneIdByChatId(str));
    }

    public void onChangeGroupChatLeaderFailed(String str, List<ImsUri> list, ImErrorReason imErrorReason) {
        Log.i(LOG_TAG, "onChangeGroupChatLeaderFailed()");
        Intent intent = new Intent();
        intent.setAction(ImIntent.Action.CHANGE_GROUPCHAT_LEADER_RESPONSE);
        intent.addCategory(ImIntent.CATEGORY_ACTION);
        intent.putExtra("response_status", false);
        intent.putExtra(ImIntent.Extras.ERROR_REASON, imErrorReason.toString());
        intent.putStringArrayListExtra(ImIntent.Extras.PARTICIPANTS_LIST, UriUtil.convertToStringList(list));
        broadcastIntent(intent, this.mImModule.getPhoneIdByChatId(str));
    }

    public void onChangeGroupChatSubjectSucceeded(String str, String str2) {
        Log.i(LOG_TAG, "onChangeGroupChatSubjectSucceeded()");
        Intent intent = new Intent();
        intent.setAction(ImIntent.Action.SET_CHAT_SUBJECT_RESPONSE);
        intent.addCategory(ImIntent.CATEGORY_ACTION);
        intent.putExtra("response_status", true);
        intent.putExtra("chat_id", str);
        intent.putExtra("subject", str2);
        broadcastIntent(intent, this.mImModule.getPhoneIdByChatId(str));
    }

    public void onChangeGroupChatSubjectFailed(String str, String str2, ImErrorReason imErrorReason) {
        Log.i(LOG_TAG, "onChangeGroupChatSubjectFailed()");
        Intent intent = new Intent();
        intent.setAction(ImIntent.Action.SET_CHAT_SUBJECT_RESPONSE);
        intent.addCategory(ImIntent.CATEGORY_ACTION);
        intent.putExtra("response_status", false);
        intent.putExtra(ImIntent.Extras.ERROR_REASON, imErrorReason.toString());
        intent.putExtra("subject", str2);
        broadcastIntent(intent, this.mImModule.getPhoneIdByChatId(str));
    }

    public void onChangeGroupChatIconSuccess(String str, String str2) {
        Log.i(LOG_TAG, "onChangeGroupChatIconSucceeded()");
        Intent intent = new Intent();
        intent.setAction(ImIntent.Action.SET_GROUPCHAT_ICON_RESPONSE);
        intent.addCategory(ImIntent.CATEGORY_ACTION);
        intent.putExtra("response_status", true);
        intent.putExtra("chat_id", str);
        intent.putExtra(ImIntent.Extras.GROUPCHAT_ICON_NAME, str2);
        broadcastIntent(intent, this.mImModule.getPhoneIdByChatId(str));
    }

    public void onChangeGroupChatIconFailed(String str, String str2, ImErrorReason imErrorReason) {
        Log.i(LOG_TAG, "onChangeGroupChatIconFailed()");
        Intent intent = new Intent();
        intent.setAction(ImIntent.Action.SET_GROUPCHAT_ICON_RESPONSE);
        intent.addCategory(ImIntent.CATEGORY_ACTION);
        intent.putExtra("response_status", false);
        intent.putExtra("chat_id", str);
        intent.putExtra(ImIntent.Extras.GROUPCHAT_ICON_NAME, str2);
        intent.putExtra(ImIntent.Extras.ERROR_REASON, imErrorReason.toString());
        broadcastIntent(intent, this.mImModule.getPhoneIdByChatId(str));
    }

    public void onChangeGroupAliasSucceeded(String str, String str2) {
        Log.i(LOG_TAG, "onChangeGroupAliasSucceeded()");
        Intent intent = new Intent();
        intent.setAction(ImIntent.Action.CHANGE_GROUP_ALIAS_RESPONSE);
        intent.addCategory(ImIntent.CATEGORY_ACTION);
        intent.putExtra("response_status", true);
        intent.putExtra("chat_id", str);
        intent.putExtra("user_alias", str2);
        broadcastIntent(intent, this.mImModule.getPhoneIdByChatId(str));
    }

    public void onChangeGroupAliasFailed(String str, String str2, ImErrorReason imErrorReason) {
        Log.i(LOG_TAG, "onChangeGroupAliasFailed()");
        Intent intent = new Intent();
        intent.setAction(ImIntent.Action.CHANGE_GROUP_ALIAS_RESPONSE);
        intent.addCategory(ImIntent.CATEGORY_ACTION);
        intent.putExtra("response_status", false);
        intent.putExtra(ImIntent.Extras.ERROR_REASON, imErrorReason.toString());
        intent.putExtra("user_alias", str2);
        broadcastIntent(intent, this.mImModule.getPhoneIdByChatId(str));
    }

    public void onMessageInserted(MessageBase messageBase) {
        Log.i(LOG_TAG, "onMessageInserted()");
        Preconditions.checkNotNull(messageBase, "msg is null");
        ImConstants.Type type = messageBase.getType();
        if (type != ImConstants.Type.MULTIMEDIA && type != ImConstants.Type.TEXT && type != ImConstants.Type.LOCATION && type != ImConstants.Type.SYSTEM) {
            Intent intent = new Intent(ImIntent.Action.RECEIVE_MESSAGE_INSERTED);
            intent.addCategory(ImIntent.CATEGORY_ACTION);
            intent.putExtra("message_imdn_id", messageBase.getImdnId());
            intent.putExtra("chat_id", messageBase.getChatId());
            intent.putExtra("message_type", type.getId());
            intent.putExtra("message_direction", messageBase.getDirection().getId());
            intent.putExtra(ImIntent.Extras.MESSAGE_SERVICE, messageBase.getServiceTag());
            broadcastIntent(intent, true, messageBase.getPhoneId());
        }
    }

    public void onMessageSendResponseTimeout(MessageBase messageBase) {
        Log.i(LOG_TAG, "onMessageSendResponseTimeout()");
        Preconditions.checkNotNull(messageBase, "msg is null");
        Intent intent = new Intent(ImIntent.Action.SEND_MESSAGE_RESPONSE_TAKETOOLONG);
        intent.addCategory(ImIntent.CATEGORY_ACTION);
        intent.putExtra("message_imdn_id", messageBase.getImdnId());
        intent.putExtra("chat_id", messageBase.getChatId());
        broadcastIntent(intent, messageBase.getPhoneId());
    }

    public void onMessageSendResponse(MessageBase messageBase) {
        String str = LOG_TAG;
        Log.i(str, "onMessageSendResponse()");
        Intent intent = new Intent();
        intent.setAction(ImIntent.Action.SEND_MESSAGE_RESPONSE);
        intent.addCategory(ImIntent.CATEGORY_ACTION);
        intent.putExtra("response_status", true);
        intent.putExtra("request_message_id", messageBase.getRequestMessageId() == null ? -1 : Long.valueOf(messageBase.getRequestMessageId()).longValue());
        intent.putExtra("message_imdn_id", messageBase.getImdnId());
        IMSLog.s(str, "onMessageSendResponse: " + intent + intent.getExtras());
        broadcastIntent(intent, true, messageBase.getPhoneId());
    }

    public void onMessageSendResponseFailed(String str, int i, int i2, String str2) {
        long j;
        String str3 = LOG_TAG;
        Log.i(str3, "onMessageSendResponseFailed(): reasonCode = " + i2 + " requestMessageId = " + str2);
        Intent intent = new Intent(ImIntent.Action.SEND_MESSAGE_RESPONSE);
        intent.addCategory(ImIntent.CATEGORY_ACTION);
        intent.putExtra("response_status", false);
        intent.putExtra(ImIntent.Extras.MESSAGE_NUMBER, i);
        intent.putExtra(ImIntent.Extras.ERROR_REASON, i2);
        intent.putExtra("chat_id", str);
        if (str2 == null) {
            j = -1;
        } else {
            j = Long.valueOf(str2).longValue();
        }
        intent.putExtra("request_message_id", j);
        broadcastIntent(intent, true, this.mImModule.getPhoneIdByChatId(str));
    }

    public void onMessageReceived(MessageBase messageBase, ImSession imSession) {
        String str = LOG_TAG;
        Log.i(str, "onMessageReceived()");
        Preconditions.checkNotNull(messageBase, "message is null");
        Intent intent = new Intent(ImIntent.Action.RECEIVE_NEW_MESSAGE);
        intent.addCategory(ImIntent.CATEGORY_ACTION);
        intent.putExtra("chat_id", messageBase.getChatId());
        intent.putExtra("message_id", Long.valueOf((long) messageBase.getId()));
        intent.putExtra("message_imdn_id", messageBase.getImdnId());
        intent.putExtra(ImIntent.Extras.IS_TOKEN_USED, imSession.getIsTokenUsed());
        intent.putExtra("message_type", messageBase.getType().getId());
        intent.putExtra("is_group_chat", imSession.isGroupChat());
        if (messageBase.isRoutingMsg()) {
            intent.putExtra(ImIntent.Extras.IS_ROUTING_MSG, messageBase.isRoutingMsg());
            if (!(messageBase.getRoutingType() == null || messageBase.getRoutingType() == RoutingType.NONE)) {
                intent.putExtra(ImIntent.Extras.ROUTING_MSG_TYPE, messageBase.getRoutingType().getId());
            }
        }
        intent.putExtra("message_direction", messageBase.mDirection.getId());
        if (imSession.isGroupChat() && (messageBase instanceof ImMessage)) {
            ImMessage imMessage = (ImMessage) messageBase;
            if (!imMessage.getGroupCcListUri().isEmpty()) {
                intent.putStringArrayListExtra(ImIntent.Extras.GROUP_CCUSER_LIST, UriUtil.convertToStringList(imMessage.getGroupCcListUri()));
            }
        }
        if (messageBase.getDirection() == ImDirection.INCOMING) {
            intent.putExtra("from", messageBase.getRemoteUri() == null ? "" : messageBase.getRemoteUri().toString());
        }
        if (messageBase.getChatbotMessagingTech() == ImConstants.ChatbotMessagingTech.STANDALONE_MESSAGING) {
            intent.putExtra(ImIntent.Extras.IS_BOT, true);
        }
        putMaapExtras(messageBase, intent);
        if (messageBase.getReferenceImdnId() != null) {
            intent.putExtra(ImIntent.Extras.REFERENCE_MESSAGE_IMDN_ID, messageBase.getReferenceImdnId());
        }
        if (messageBase.getReferenceType() != null) {
            intent.putExtra(ImIntent.Extras.REFERENCE_MESSAGE_TYPE, messageBase.getReferenceType());
        }
        if (messageBase.getReferenceValue() != null) {
            intent.putExtra(ImIntent.Extras.REFERENCE_MESSAGE_VALUE, messageBase.getReferenceValue());
        }
        String rcsTrafficType = messageBase.getRcsTrafficType();
        if (rcsTrafficType != null) {
            Log.i(str, "rcsTrafficType = [" + rcsTrafficType + "]");
            intent.putExtra(ImIntent.Extras.RCS_TRAFFIC_TYPE, rcsTrafficType);
        }
        broadcastIntent(intent, true, imSession.getPhoneId());
    }

    public void onImdnNotificationReceived(MessageBase messageBase, ImsUri imsUri, NotificationStatus notificationStatus, boolean z) {
        boolean z2 = !TextUtils.isEmpty(messageBase.getMessageCreator()) && messageBase.getMessageCreator().equalsIgnoreCase(ImConstants.MessageCreatorTag.SD);
        String str = LOG_TAG;
        Log.i(str, "onImdnNotificationReceived() isSDMessage: " + z2);
        if (!z2) {
            broadcastIntent(createImdnNotificationReceivedIntent(messageBase, imsUri, notificationStatus, z), true, messageBase.getPhoneId());
        }
    }

    public void onMessageRevokeTimerExpired(String str, Collection<String> collection) {
        String str2 = LOG_TAG;
        Log.i(str2, "onMessageRevokeTimerExpired(): chatId = " + str + " imdnIds = " + collection);
        Intent intent = new Intent(ImIntent.Action.MESSAGE_REVOKE_TIMER_EXPIRED);
        intent.putExtra("chat_id", str);
        intent.putStringArrayListExtra(ImIntent.Extras.MESSAGES_IMDN_ID_LIST, new ArrayList(collection));
        broadcastIntent(intent, true, this.mImModule.getPhoneIdByChatId(str));
    }

    public void onParticipantInserted(ImParticipant imParticipant) {
        String str = LOG_TAG;
        Log.i(str, "onParticipantInserted: " + IMSLog.numberChecker(imParticipant.getUri()));
        Intent intent = new Intent(ImIntent.Action.RECEIVE_PARTICIPANT_INSERTED);
        intent.putExtra("chat_id", imParticipant.getChatId());
        intent.putExtra("participant", imParticipant.getUri().toString());
        intent.putExtra(ImIntent.Extras.PARTICIPANT_ID, Long.valueOf((long) imParticipant.getId()));
        intent.putExtra(ImIntent.Extras.PARTICIPANT_STATUS, imParticipant.getStatus().getId());
        intent.putExtra("user_alias", imParticipant.getUserAlias());
        intent.setPackage(ImsConstants.Packages.PACKAGE_SEC_MSG);
        broadcastIntent(intent, true, this.mImModule.getPhoneIdByChatId(imParticipant.getChatId()));
    }

    public void onParticipantInserted(ArrayList<ImParticipantData> arrayList) {
        String str = LOG_TAG;
        Log.i(str, "onParticipantInserted: " + IMSLog.checker(arrayList));
        Intent intent = new Intent(ImIntent.Action.RECEIVE_PARTICIPANTS_INSERTED);
        intent.putParcelableArrayListExtra(ImIntent.Extras.PARTICIPANTS_LIST, arrayList);
        intent.setPackage(ImsConstants.Packages.PACKAGE_SEC_MSG);
        broadcastIntent(intent, true, this.mImModule.getPhoneIdByChatId(arrayList.get(0).getChatId()));
    }

    public void onParticipantDeleted(ImParticipant imParticipant) {
        String str = LOG_TAG;
        Log.i(str, "onParticipantDeleted: " + IMSLog.numberChecker(imParticipant.getUri()));
        Intent intent = new Intent(ImIntent.Action.RECEIVE_PARTICIPANT_DELETED);
        intent.putExtra("chat_id", imParticipant.getChatId());
        intent.putExtra("participant", imParticipant.getUri().toString());
        intent.putExtra(ImIntent.Extras.PARTICIPANT_ID, Long.valueOf((long) imParticipant.getId()));
        intent.putExtra(ImIntent.Extras.PARTICIPANT_STATUS, imParticipant.getStatus().getId());
        intent.putExtra("user_alias", imParticipant.getUserAlias());
        intent.setPackage(ImsConstants.Packages.PACKAGE_SEC_MSG);
        broadcastIntent(intent, true, this.mImModule.getPhoneIdByChatId(imParticipant.getChatId()));
    }

    public void onParticipantDeleted(ArrayList<ImParticipantData> arrayList) {
        String str = LOG_TAG;
        Log.i(str, "onParticipantDeleted: " + IMSLog.checker(arrayList));
        Intent intent = new Intent(ImIntent.Action.RECEIVE_PARTICIPANTS_DELETED);
        intent.putParcelableArrayListExtra(ImIntent.Extras.PARTICIPANTS_LIST, arrayList);
        intent.setPackage(ImsConstants.Packages.PACKAGE_SEC_MSG);
        broadcastIntent(intent, true, this.mImModule.getPhoneIdByChatId(arrayList.get(0).getChatId()));
    }

    public void onCancelMessageResponse(String str, String str2, boolean z) {
        String str3 = LOG_TAG;
        Log.i(str3, "onCancelMessageResponse: chatId = " + str + ", imdnId = " + str2 + ", isSuccess = " + z);
        Intent intent = new Intent();
        intent.setAction(ImIntent.Action.CANCEL_MESSAGE_RESPONSE);
        intent.addCategory(ImIntent.CATEGORY_ACTION);
        intent.putExtra("chat_id", str);
        intent.putExtra("message_imdn_id", str2);
        intent.putExtra("response_status", z);
        broadcastIntent(intent, true, this.mImModule.getPhoneIdByChatId(str));
    }

    public void handleIntent(Intent intent) {
        String action = intent.getAction();
        String str = LOG_TAG;
        IMSLog.s(str, "Received intent: " + action);
        action.hashCode();
        char c = 65535;
        switch (action.hashCode()) {
            case -2018218708:
                if (action.equals(ImIntent.Action.CANCEL_MESSAGE)) {
                    c = 0;
                    break;
                }
                break;
            case -1913821034:
                if (action.equals(ImIntent.Action.DELETE_MESSAGES)) {
                    c = 1;
                    break;
                }
                break;
            case -1675693016:
                if (action.equals(ImIntent.Action.SET_GROUPCHAT_ICON)) {
                    c = 2;
                    break;
                }
                break;
            case -1519516883:
                if (action.equals(ImIntent.Action.REPORT_MESSAGES)) {
                    c = 3;
                    break;
                }
                break;
            case -1476850583:
                if (action.equals(ImIntent.Action.CLOSE_CHAT)) {
                    c = 4;
                    break;
                }
                break;
            case -1241657933:
                if (action.equals(ImIntent.Action.DELETE_ALL_CHATS)) {
                    c = 5;
                    break;
                }
                break;
            case -1144800568:
                if (action.equals(ImIntent.Action.READ_MESSAGE)) {
                    c = 6;
                    break;
                }
                break;
            case -1120324265:
                if (action.equals(ImIntent.Action.REPORT_CHATBOT_AS_SPAM)) {
                    c = 7;
                    break;
                }
                break;
            case -801423832:
                if (action.equals(ImIntent.Action.ADD_PARTICIPANTS)) {
                    c = 8;
                    break;
                }
                break;
            case -653426186:
                if (action.equals(ImIntent.Action.CHANGE_GROUPCHAT_LEADER)) {
                    c = 9;
                    break;
                }
                break;
            case -589383736:
                if (action.equals(ImIntent.Action.CHATBOT_ANONYMIZE)) {
                    c = 10;
                    break;
                }
                break;
            case -479667282:
                if (action.equals(ImIntent.Action.GET_IS_COMPOSING_ACTIVE_URIS)) {
                    c = 11;
                    break;
                }
                break;
            case -478765228:
                if (action.equals(ImIntent.Action.ANSWER_GC_CHAT_INVITATION)) {
                    c = 12;
                    break;
                }
                break;
            case -418937103:
                if (action.equals(ImIntent.Action.CREATE_CHAT)) {
                    c = 13;
                    break;
                }
                break;
            case -385468532:
                if (action.equals(ImIntent.Action.SET_CHAT_SUBJECT)) {
                    c = 14;
                    break;
                }
                break;
            case -325378863:
                if (action.equals(ImIntent.Action.IGNORE_INCOMING_MESSAGE)) {
                    c = 15;
                    break;
                }
                break;
            case 375322443:
                if (action.equals(ImIntent.Action.CHANGE_GROUP_ALIAS)) {
                    c = 16;
                    break;
                }
                break;
            case 421496980:
                if (action.equals(ImIntent.Action.DELETE_ALL_MESSAGES)) {
                    c = 17;
                    break;
                }
                break;
            case 520146251:
                if (action.equals(ImIntent.Action.DELETE_GROUPCHAT_ICON)) {
                    c = 18;
                    break;
                }
                break;
            case 703339109:
                if (action.equals(ImIntent.Action.ACCEPT_CHAT)) {
                    c = 19;
                    break;
                }
                break;
            case 724573882:
                if (action.equals(ImIntent.Action.SEND_MESSAGE)) {
                    c = 20;
                    break;
                }
                break;
            case 906375040:
                if (action.equals(ImIntent.Action.DELIVERY_TIMEOUT)) {
                    c = 21;
                    break;
                }
                break;
            case 965765382:
                if (action.equals(ImIntent.Action.SEND_TYPING_NOTIFICATION)) {
                    c = 22;
                    break;
                }
                break;
            case 1017807128:
                if (action.equals(ImIntent.Action.MESSAGE_REVOKE_REQUEST)) {
                    c = 23;
                    break;
                }
                break;
            case 1039119331:
                if (action.equals(ImIntent.Action.OPEN_CHAT)) {
                    c = 24;
                    break;
                }
                break;
            case 1664331893:
                if (action.equals(ImIntent.Action.GET_LAST_MESSAGES_SENT)) {
                    c = 25;
                    break;
                }
                break;
            case 1896987889:
                if (action.equals(ImIntent.Action.DELETE_CHATS)) {
                    c = 26;
                    break;
                }
                break;
            case 2052413361:
                if (action.equals(ImIntent.Action.REMOVE_PARTICIPANTS)) {
                    c = 27;
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
                requestCancelMessage(intent);
                return;
            case 1:
                requestDeleteMessages(intent);
                return;
            case 2:
                requestChangeGroupChatIcon(intent);
                return;
            case 3:
                requestReportMessage(intent);
                return;
            case 4:
                requestCloseChat(intent);
                return;
            case 5:
                requestDeleteAllChats();
                return;
            case 6:
                requestReadMessage(intent);
                return;
            case 7:
                reportChatbotAsSpam(intent);
                return;
            case 8:
                requestAddParticipantsToChat(intent);
                return;
            case 9:
                requestChangeGroupChatLeader(intent);
                return;
            case 10:
                requestChatbotAnonymize(intent);
                return;
            case 11:
                requestComposingActiveUris(intent);
                return;
            case 12:
                requestAnswerGcChats(intent);
                return;
            case 13:
                requestCreateChat(intent);
                return;
            case 14:
                requestChangeGroupChatSubject(intent);
                return;
            case 15:
                requestIgnoreIncomingMsgSet(intent);
                return;
            case 16:
                requestChangeGroupAlias(intent);
                return;
            case 17:
                requestDeleteAllMessages(intent);
                return;
            case 18:
                requestDeleteGroupChatIcon(intent);
                return;
            case 19:
                requestAcceptChat(intent);
                return;
            case 20:
                requestSendMessage(intent);
                return;
            case 21:
                requestDeliveryTimeout(intent);
                return;
            case 22:
                requestSendComposingNotification(intent);
                return;
            case 23:
                requestMessageRevocation(intent);
                return;
            case 24:
                requestOpenChat(intent);
                return;
            case 25:
                requestGetLastSentMessages(intent);
                return;
            case 26:
                requestDeleteChats(intent);
                return;
            case 27:
                requestRemoveGroupChatParticipants(intent);
                return;
            default:
                Log.e(str, "Unexpected intent received. acition=" + action);
                return;
        }
    }

    private void broadcastIntent(Intent intent, int i) {
        broadcastIntent(intent, false, i);
    }

    private void broadcastIntent(Intent intent, boolean z, int i) {
        String str = LOG_TAG;
        IMSLog.s(str, "broadcastIntent: " + intent + intent.getExtras());
        if (z) {
            intent.addFlags(LogClass.SIM_EVENT);
        }
        UserHandle subscriptionUserHandle = TelephonyUtilsWrapper.getSubscriptionUserHandle(this.mContext, SimUtil.getSubId(i));
        if (this.mImModule.getRcsStrategy() != null && this.mImModule.getRcsStrategy().isBMode(true)) {
            IntentUtil.sendBroadcast(this.mContext, intent, ContextExt.OWNER, "com.samsung.rcs.im.READ_PERMISSION");
        } else if (subscriptionUserHandle != null) {
            IntentUtil.sendBroadcast(this.mContext, intent, subscriptionUserHandle, "com.samsung.rcs.im.READ_PERMISSION");
        } else {
            IntentUtil.sendBroadcast(this.mContext, intent, ContextExt.CURRENT_OR_SELF, "com.samsung.rcs.im.READ_PERMISSION");
        }
    }

    public void updateMessage(MessageBase messageBase, ImCacheAction imCacheAction) {
        if (imCacheAction == ImCacheAction.INSERTED) {
            onMessageInserted(messageBase);
        }
    }

    public void updateMessage(Collection<MessageBase> collection, ImCacheAction imCacheAction) {
        if (imCacheAction == ImCacheAction.INSERTED) {
            for (MessageBase onMessageInserted : collection) {
                onMessageInserted(onMessageInserted);
            }
        }
    }

    public void updateParticipant(ImParticipant imParticipant, ImCacheAction imCacheAction) {
        if (imCacheAction == ImCacheAction.INSERTED) {
            onParticipantInserted(imParticipant);
        } else if (imCacheAction == ImCacheAction.DELETED) {
            onParticipantDeleted(imParticipant);
        }
    }

    public void updateParticipant(Collection<ImParticipant> collection, ImCacheAction imCacheAction) {
        if (collection != null && !collection.isEmpty()) {
            if (imCacheAction == ImCacheAction.INSERTED || imCacheAction == ImCacheAction.DELETED) {
                ArrayList arrayList = new ArrayList();
                for (ImParticipant next : collection) {
                    arrayList.add(new ImParticipantData(next.getChatId(), next.getUri().toString(), next.getId(), next.getStatus().getId(), next.getUserAlias()));
                }
                if (imCacheAction == ImCacheAction.INSERTED) {
                    onParticipantInserted((ArrayList<ImParticipantData>) arrayList);
                } else {
                    onParticipantDeleted((ArrayList<ImParticipantData>) arrayList);
                }
            }
        }
    }

    public void onGroupChatLeaderUpdated(String str, String str2) {
        String str3 = LOG_TAG;
        Log.i(str3, "onGroupChatLeaderUpdated: " + IMSLog.numberChecker(str2));
        Intent intent = new Intent(ImIntent.Action.RECEIVE_GROUPCHAT_LEADER_CHANGED);
        intent.putExtra("chat_id", str);
        ArrayList arrayList = new ArrayList();
        arrayList.add(str2);
        intent.putStringArrayListExtra(ImIntent.Extras.PARTICIPANTS_LIST, arrayList);
        broadcastIntent(intent, true, this.mImModule.getPhoneIdByChatId(str));
    }
}
