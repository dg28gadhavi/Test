package com.sec.internal.ims.servicemodules.im;

import android.content.Intent;
import android.util.Log;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.NotificationStatus;
import com.sec.internal.constants.ims.servicemodules.im.result.Result;
import com.sec.internal.ims.servicemodules.im.interfaces.ImIntent;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.log.IMSLog;
import org.json.JSONException;
import org.json.JSONObject;

public class TranslationBase {
    private static final String LOG_TAG = "TranslationBase";

    /* renamed from: com.sec.internal.ims.servicemodules.im.TranslationBase$1  reason: invalid class name */
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
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.TranslationBase.AnonymousClass1.<clinit>():void");
        }
    }

    /* access modifiers changed from: protected */
    public int getRequiredAction(IMnoStrategy.StatusCode statusCode) {
        int i = AnonymousClass1.$SwitchMap$com$sec$internal$ims$servicemodules$im$strategy$IMnoStrategy$StatusCode[statusCode.ordinal()];
        if (i == 2) {
            return 1;
        }
        if (i != 3) {
            return i != 4 ? 0 : 3;
        }
        return 2;
    }

    /* access modifiers changed from: protected */
    public void putMaapExtras(MessageBase messageBase, Intent intent) {
        String suggestion = messageBase.getSuggestion();
        if (suggestion != null) {
            try {
                JSONObject jSONObject = new JSONObject(suggestion);
                jSONObject.remove("persistent");
                suggestion = jSONObject.toString();
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "no suggestions ");
            }
            String str = LOG_TAG;
            Log.i(str, "suggestion = " + IMSLog.checker(suggestion));
            intent.putExtra(ImIntent.Extras.SUGGESTION_TEXT, suggestion);
        }
        String maapTrafficType = messageBase.getMaapTrafficType();
        if (maapTrafficType != null) {
            String str2 = LOG_TAG;
            Log.i(str2, "maapTrafficType = [" + maapTrafficType + "]");
            intent.putExtra("maap_traffic_type", maapTrafficType);
        }
    }

    public Intent createMessageSendingFailedIntent(MessageBase messageBase, IMnoStrategy.StrategyResponse strategyResponse, Result result) {
        Intent intent = new Intent(ImIntent.Action.RECEIVE_SEND_MESSAGE_RESPONSE);
        intent.addCategory(ImIntent.CATEGORY_ACTION);
        intent.putExtra("message_imdn_id", messageBase.getImdnId());
        intent.putExtra("response_status", false);
        intent.putExtra(ImIntent.Extras.ERROR_REASON, messageBase.getRcsStrategy() != null ? messageBase.getRcsStrategy().getErrorReasonForStrategyResponse(messageBase, strategyResponse) : null);
        if (!(messageBase.getRcsStrategy() == null || result == null)) {
            if (messageBase.getRcsStrategy().isDisplayBotError() && result.getSipResponse() != null) {
                intent.putExtra(ImIntent.Extras.SIP_ERROR, result.getSipResponse().getId());
            }
            if (messageBase.getRcsStrategy().isDisplayBotError() && result.getMsrpResponse() != null) {
                intent.putExtra(ImIntent.Extras.SIP_ERROR, result.getMsrpResponse().getId());
            }
            if (messageBase.getRcsStrategy().isDisplayWarnText() && result.getImError() != null) {
                intent.putExtra(ImIntent.Extras.WARN_TEXT, result.getImError().toString());
            }
        }
        intent.putExtra("request_message_id", messageBase.getRequestMessageId() == null ? -1 : Long.valueOf(messageBase.getRequestMessageId()).longValue());
        intent.putExtra("is_broadcast_msg", messageBase.isBroadcastMsg());
        if (strategyResponse != null) {
            intent.putExtra(ImIntent.Extras.REQUIRED_ACTION, getRequiredAction(strategyResponse.getStatusCode()));
            intent.putExtra(ImIntent.Extras.ERROR_NOTIFICATION_ID, strategyResponse.getErrorNotificationId().ordinal());
        } else {
            intent.putExtra(ImIntent.Extras.REQUIRED_ACTION, getRequiredAction(IMnoStrategy.StatusCode.DISPLAY_ERROR));
            intent.putExtra(ImIntent.Extras.ERROR_NOTIFICATION_ID, IMnoStrategy.ErrorNotificationId.NONE.ordinal());
        }
        return intent;
    }

    public Intent createImdnNotificationReceivedIntent(MessageBase messageBase, ImsUri imsUri, NotificationStatus notificationStatus, boolean z) {
        Intent intent = new Intent(ImIntent.Action.RECEIVE_MESSAGE_NOTIFICATION_STATUS);
        intent.addCategory(ImIntent.CATEGORY_ACTION);
        intent.putExtra("message_imdn_id", messageBase.getImdnId());
        intent.putExtra("chat_id", messageBase.getChatId());
        intent.putExtra("message_notification_status", messageBase.getNotificationStatus().getId());
        intent.putExtra(ImIntent.Extras.MESSAGE_NOTIFICATION_STATUS_RECEIVED, messageBase.getLastNotificationType().getId());
        intent.putExtra("is_group_chat", z);
        return intent;
    }
}
