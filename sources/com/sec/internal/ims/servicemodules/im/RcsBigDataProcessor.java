package com.sec.internal.ims.servicemodules.im;

import android.content.ContentValues;
import android.content.Context;
import com.sec.ims.ImsRegistration;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.servicemodules.im.result.Result;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.diagnosis.ImsLogAgentUtil;
import com.sec.internal.ims.servicemodules.im.interfaces.IRcsBigDataProcessor;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import java.nio.charset.StandardCharsets;

public abstract class RcsBigDataProcessor implements IRcsBigDataProcessor {
    private Context mContext;

    /* access modifiers changed from: protected */
    public abstract ImSession getImSession(String str);

    /* access modifiers changed from: protected */
    public abstract ImsRegistration getImsRegistration(int i);

    /* access modifiers changed from: protected */
    public abstract String getMessageType(MessageBase messageBase, boolean z);

    /* access modifiers changed from: protected */
    public abstract String getMessageTypeForILA(String str);

    /* access modifiers changed from: protected */
    public abstract boolean isChatBot(int i, ImSession imSession);

    /* access modifiers changed from: protected */
    public abstract boolean isWifiConnected();

    protected RcsBigDataProcessor(Context context) {
        this.mContext = context;
    }

    public void onMessageSendingSucceeded(MessageBase messageBase) {
        sendMoBigdata(messageBase, "0", (String) null, (Result.Type) null, (IMnoStrategy.StatusCode) null);
    }

    public void onMessageSendingFailed(MessageBase messageBase, Result result, IMnoStrategy.StrategyResponse strategyResponse) {
        onMessageSendingFailed(messageBase, result, getCause(result), strategyResponse);
    }

    public void onMessageSendingFailed(MessageBase messageBase, Result result, String str, IMnoStrategy.StrategyResponse strategyResponse) {
        sendMoBigdata(messageBase, getOrst(result.getType()), str, result.getType(), strategyResponse != null ? strategyResponse.getStatusCode() : null);
    }

    public void onMessageReceived(MessageBase messageBase, ImSession imSession) {
        onMessageReceived(getPhoneIdByImsi(imSession.getOwnImsi()), messageBase, imSession);
    }

    public void onMessageReceived(int i, MessageBase messageBase, ImSession imSession) {
        storeMtDrcsInfoToImsLogAgent(i, isChatBot(i, imSession), ImConstants.ChatbotTrafficType.fromString(messageBase != null ? messageBase.getMaapTrafficType() : null));
    }

    public void onMessageCancelSent(int i, int i2) {
        storeMessageCancelInfoToImsLogAgent(i, i2);
    }

    private void sendMoBigdata(MessageBase messageBase, String str, String str2, Result.Type type, IMnoStrategy.StatusCode statusCode) {
        ImSession imSession = getImSession(messageBase.getChatId());
        if (imSession != null) {
            int phoneIdByImsi = getPhoneIdByImsi(messageBase.getOwnIMSI());
            boolean isChatBot = isChatBot(phoneIdByImsi, imSession);
            MessageBase messageBase2 = messageBase;
            String messageType = getMessageType(messageBase, isChatBot);
            sendRcsmInfoToHqmAgent(messageBase, str, str2, statusCode, imSession, phoneIdByImsi, messageType);
            storeMoDrcsInfoToImsLogAgent(phoneIdByImsi, isChatBot, str, messageType, type, statusCode, messageBase.getReferenceType());
        }
    }

    private void sendRcsmInfoToHqmAgent(MessageBase messageBase, String str, String str2, IMnoStrategy.StatusCode statusCode, ImSession imSession, int i, String str3) {
        ContentValues contentValues = new ContentValues();
        prepareKeysForRcsm(i, contentValues, messageBase, imSession, str, str3, str2, statusCode);
        ImsLogAgentUtil.sendLogToAgent(i, this.mContext, DiagnosisConstants.FEATURE_RCSM, contentValues);
    }

    private void storeMtDrcsInfoToImsLogAgent(int i, boolean z, ImConstants.ChatbotTrafficType chatbotTrafficType) {
        ContentValues contentValues = new ContentValues();
        prepareKeysForMtDrcs(z, chatbotTrafficType, contentValues);
        storeDrcsInfoToImsLogAgent(i, contentValues);
    }

    private void prepareKeysForRcsm(int i, ContentValues contentValues, MessageBase messageBase, ImSession imSession, String str, String str2, String str3, IMnoStrategy.StatusCode statusCode) {
        prepareCommonKeysForRcsm(i, contentValues, messageBase, imSession, str, str2);
        if (messageBase instanceof FtMessage) {
            prepareFtSpecificKeysForRcsm(contentValues, (FtMessage) messageBase);
        }
        prepareErrorKeysForRcsm(contentValues, str, str3);
        prepareOptionalKeysForRcsm(contentValues, messageBase, statusCode);
    }

    private void prepareCommonKeysForRcsm(int i, ContentValues contentValues, MessageBase messageBase, ImSession imSession, String str, String str2) {
        contentValues.put("ORST", str);
        String str3 = "0";
        contentValues.put(DiagnosisConstants.RCSM_KEY_MDIR, str3);
        if (imSession.isGroupChat()) {
            str3 = "1";
        }
        contentValues.put(DiagnosisConstants.RCSM_KEY_MGRP, str3);
        contentValues.put(DiagnosisConstants.RCSM_KEY_MTYP, str2);
        contentValues.put(DiagnosisConstants.RCSM_KEY_MCID, messageBase.getChatId());
        contentValues.put(DiagnosisConstants.RCSM_KEY_MIID, messageBase.getImdnId());
        contentValues.put(DiagnosisConstants.RCSM_KEY_MSIZ, getMessageSize(messageBase));
        contentValues.put(DiagnosisConstants.RCSM_KEY_PTCN, String.valueOf(imSession.getParticipantsSize()));
        contentValues.put(DiagnosisConstants.RCSM_KEY_MRAT, getRegiRat(i));
    }

    private void prepareFtSpecificKeysForRcsm(ContentValues contentValues, FtMessage ftMessage) {
        contentValues.put(DiagnosisConstants.RCSM_KEY_FTYP, getFileExtension(ftMessage));
        contentValues.put(DiagnosisConstants.RCSM_KEY_FTRC, String.valueOf(ftMessage.getRetryCount()));
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void prepareErrorKeysForRcsm(android.content.ContentValues r4, java.lang.String r5, java.lang.String r6) {
        /*
            r3 = this;
            int r3 = r5.hashCode()
            r0 = 3
            r1 = 2
            r2 = 1
            switch(r3) {
                case 48: goto L_0x003d;
                case 49: goto L_0x0033;
                case 50: goto L_0x0029;
                case 51: goto L_0x001f;
                case 52: goto L_0x0015;
                case 53: goto L_0x000b;
                default: goto L_0x000a;
            }
        L_0x000a:
            goto L_0x0047
        L_0x000b:
            java.lang.String r3 = "5"
            boolean r3 = r5.equals(r3)
            if (r3 == 0) goto L_0x0047
            r3 = 5
            goto L_0x0048
        L_0x0015:
            java.lang.String r3 = "4"
            boolean r3 = r5.equals(r3)
            if (r3 == 0) goto L_0x0047
            r3 = r0
            goto L_0x0048
        L_0x001f:
            java.lang.String r3 = "3"
            boolean r3 = r5.equals(r3)
            if (r3 == 0) goto L_0x0047
            r3 = 4
            goto L_0x0048
        L_0x0029:
            java.lang.String r3 = "2"
            boolean r3 = r5.equals(r3)
            if (r3 == 0) goto L_0x0047
            r3 = r1
            goto L_0x0048
        L_0x0033:
            java.lang.String r3 = "1"
            boolean r3 = r5.equals(r3)
            if (r3 == 0) goto L_0x0047
            r3 = r2
            goto L_0x0048
        L_0x003d:
            java.lang.String r3 = "0"
            boolean r3 = r5.equals(r3)
            if (r3 == 0) goto L_0x0047
            r3 = 0
            goto L_0x0048
        L_0x0047:
            r3 = -1
        L_0x0048:
            if (r3 == 0) goto L_0x0067
            if (r3 == r2) goto L_0x0062
            if (r3 == r1) goto L_0x005c
            if (r3 == r0) goto L_0x0056
            java.lang.String r3 = "ITER"
            r4.put(r3, r6)
            goto L_0x0067
        L_0x0056:
            java.lang.String r3 = "HTTP"
            r4.put(r3, r6)
            goto L_0x0067
        L_0x005c:
            java.lang.String r3 = "MSRP"
            r4.put(r3, r6)
            goto L_0x0067
        L_0x0062:
            java.lang.String r3 = "SIPR"
            r4.put(r3, r6)
        L_0x0067:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.RcsBigDataProcessor.prepareErrorKeysForRcsm(android.content.ContentValues, java.lang.String, java.lang.String):void");
    }

    private void prepareOptionalKeysForRcsm(ContentValues contentValues, MessageBase messageBase, IMnoStrategy.StatusCode statusCode) {
        if (statusCode != null) {
            contentValues.put(DiagnosisConstants.RCSM_KEY_SRSC, statusCode.toString());
        }
        if (messageBase.getReferenceType() != null) {
            contentValues.put(DiagnosisConstants.RCSM_KEY_MRTY, messageBase.getReferenceType());
        }
        if (messageBase.getReferenceValue() != null) {
            contentValues.put(DiagnosisConstants.RCSM_KEY_MRVA, messageBase.getReferenceValue());
        }
    }

    private void storeMoDrcsInfoToImsLogAgent(int i, boolean z, String str, String str2, Result.Type type, IMnoStrategy.StatusCode statusCode, String str3) {
        ContentValues contentValues = new ContentValues();
        prepareKeysForMoDrcs(z, str, str2, type, statusCode, str3, contentValues);
        int i2 = i;
        storeDrcsInfoToImsLogAgent(i, contentValues);
    }

    private void prepareKeysForMoDrcs(boolean z, String str, String str2, Result.Type type, IMnoStrategy.StatusCode statusCode, String str3, ContentValues contentValues) {
        prepareResultKeysForMoDrcs(contentValues, z, str, str2, type);
        if (statusCode == IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY) {
            contentValues.put(DiagnosisConstants.DRCS_KEY_SMS_FALLBACK, 1);
        }
        if ("1".equals(str3)) {
            contentValues.put(DiagnosisConstants.DRCS_KEY_RCS_REPLY, 1);
        } else if ("2".equals(str3)) {
            contentValues.put(DiagnosisConstants.DRCS_KEY_RCS_REACTION, 1);
        }
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0044, code lost:
        if (r7.equals("1") == false) goto L_0x0010;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void prepareResultKeysForMoDrcs(android.content.ContentValues r5, boolean r6, java.lang.String r7, java.lang.String r8, com.sec.internal.constants.ims.servicemodules.im.result.Result.Type r9) {
        /*
            r4 = this;
            r7.hashCode()
            int r0 = r7.hashCode()
            r1 = 1
            java.lang.Integer r2 = java.lang.Integer.valueOf(r1)
            r3 = -1
            switch(r0) {
                case 48: goto L_0x0047;
                case 49: goto L_0x003e;
                case 50: goto L_0x0033;
                case 51: goto L_0x0028;
                case 52: goto L_0x001d;
                case 53: goto L_0x0012;
                default: goto L_0x0010;
            }
        L_0x0010:
            r1 = r3
            goto L_0x0051
        L_0x0012:
            java.lang.String r0 = "5"
            boolean r7 = r7.equals(r0)
            if (r7 != 0) goto L_0x001b
            goto L_0x0010
        L_0x001b:
            r1 = 5
            goto L_0x0051
        L_0x001d:
            java.lang.String r0 = "4"
            boolean r7 = r7.equals(r0)
            if (r7 != 0) goto L_0x0026
            goto L_0x0010
        L_0x0026:
            r1 = 4
            goto L_0x0051
        L_0x0028:
            java.lang.String r0 = "3"
            boolean r7 = r7.equals(r0)
            if (r7 != 0) goto L_0x0031
            goto L_0x0010
        L_0x0031:
            r1 = 3
            goto L_0x0051
        L_0x0033:
            java.lang.String r0 = "2"
            boolean r7 = r7.equals(r0)
            if (r7 != 0) goto L_0x003c
            goto L_0x0010
        L_0x003c:
            r1 = 2
            goto L_0x0051
        L_0x003e:
            java.lang.String r0 = "1"
            boolean r7 = r7.equals(r0)
            if (r7 != 0) goto L_0x0051
            goto L_0x0010
        L_0x0047:
            java.lang.String r0 = "0"
            boolean r7 = r7.equals(r0)
            if (r7 != 0) goto L_0x0050
            goto L_0x0010
        L_0x0050:
            r1 = 0
        L_0x0051:
            java.lang.String r7 = "MPOF"
            java.lang.String r0 = "RCOF"
            switch(r1) {
                case 0: goto L_0x008c;
                case 1: goto L_0x007a;
                case 2: goto L_0x007a;
                case 3: goto L_0x0068;
                case 4: goto L_0x007a;
                case 5: goto L_0x0059;
                default: goto L_0x0058;
            }
        L_0x0058:
            goto L_0x009d
        L_0x0059:
            if (r6 == 0) goto L_0x005c
            goto L_0x005d
        L_0x005c:
            r7 = r0
        L_0x005d:
            r5.put(r7, r2)
            java.lang.String r4 = r4.getFailTypeForILA(r9, r6)
            r5.put(r4, r2)
            goto L_0x009d
        L_0x0068:
            if (r6 == 0) goto L_0x006b
            goto L_0x006c
        L_0x006b:
            r7 = r0
        L_0x006c:
            r5.put(r7, r2)
            if (r6 == 0) goto L_0x0074
            java.lang.String r4 = "MOFT"
            goto L_0x0076
        L_0x0074:
            java.lang.String r4 = "ROFT"
        L_0x0076:
            r5.put(r4, r2)
            goto L_0x009d
        L_0x007a:
            if (r6 == 0) goto L_0x007d
            goto L_0x007e
        L_0x007d:
            r7 = r0
        L_0x007e:
            r5.put(r7, r2)
            if (r6 == 0) goto L_0x0086
            java.lang.String r4 = "MOFN"
            goto L_0x0088
        L_0x0086:
            java.lang.String r4 = "ROFN"
        L_0x0088:
            r5.put(r4, r2)
            goto L_0x009d
        L_0x008c:
            if (r6 == 0) goto L_0x0091
            java.lang.String r6 = "MPOS"
            goto L_0x0093
        L_0x0091:
            java.lang.String r6 = "RCOS"
        L_0x0093:
            r5.put(r6, r2)
            java.lang.String r4 = r4.getMessageTypeForILA(r8)
            r5.put(r4, r2)
        L_0x009d:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.RcsBigDataProcessor.prepareResultKeysForMoDrcs(android.content.ContentValues, boolean, java.lang.String, java.lang.String, com.sec.internal.constants.ims.servicemodules.im.result.Result$Type):void");
    }

    private void prepareKeysForMtDrcs(boolean z, ImConstants.ChatbotTrafficType chatbotTrafficType, ContentValues contentValues) {
        if (!z) {
            contentValues.put(DiagnosisConstants.DRCS_KEY_RCS_MT, 1);
            return;
        }
        String chatBotTrafficType = getChatBotTrafficType(chatbotTrafficType);
        if (!chatBotTrafficType.isEmpty()) {
            contentValues.put(chatBotTrafficType, 1);
        }
        contentValues.put(DiagnosisConstants.DRCS_KEY_MAAP_MT, 1);
    }

    private void storeMessageCancelInfoToImsLogAgent(int i, int i2) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DiagnosisConstants.DRCS_KEY_RCS_CANCEL, Integer.valueOf(i2));
        storeDrcsInfoToImsLogAgent(i, contentValues);
    }

    private void storeDrcsInfoToImsLogAgent(int i, ContentValues contentValues) {
        contentValues.put(DiagnosisConstants.KEY_SEND_MODE, 1);
        contentValues.put(DiagnosisConstants.KEY_OVERWRITE_MODE, 1);
        ImsLogAgentUtil.storeLogToAgent(i, this.mContext, DiagnosisConstants.FEATURE_DRCS, contentValues);
    }

    private int getPhoneIdByImsi(String str) {
        int phoneId = SimManagerFactory.getPhoneId(str);
        return phoneId == -1 ? SimUtil.getActiveDataPhoneId() : phoneId;
    }

    private String getMessageSize(MessageBase messageBase) {
        if (messageBase instanceof FtMessage) {
            return String.valueOf(((FtMessage) messageBase).getFileSize());
        }
        try {
            return String.valueOf(messageBase.getBody().getBytes(StandardCharsets.UTF_8).length);
        } catch (NullPointerException unused) {
            return "0";
        }
    }

    private String getRegiRat(int i) {
        ImsRegistration imsRegistration = getImsRegistration(i);
        String valueOf = imsRegistration != null ? String.valueOf(imsRegistration.getCurrentRat()) : "-1";
        if (!isWifiConnected()) {
            return valueOf;
        }
        return valueOf + DiagnosisConstants.RCSM_MRAT_WIFI_POSTFIX;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:2:0x0006, code lost:
        r2 = r1.lastIndexOf(46);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private java.lang.String getFileExtension(com.sec.internal.ims.servicemodules.im.FtMessage r2) {
        /*
            r1 = this;
            java.lang.String r1 = r2.getFileName()
            if (r1 == 0) goto L_0x0016
            r2 = 46
            int r2 = r1.lastIndexOf(r2)
            r0 = -1
            if (r2 <= r0) goto L_0x0016
            int r2 = r2 + 1
            java.lang.String r1 = r1.substring(r2)
            return r1
        L_0x0016:
            java.lang.String r1 = ""
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.RcsBigDataProcessor.getFileExtension(com.sec.internal.ims.servicemodules.im.FtMessage):java.lang.String");
    }

    private String getFailTypeForILA(Result.Type type, boolean z) {
        int[] iArr = AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$servicemodules$im$result$Result$Type;
        if (type == null) {
            type = Result.Type.UNKNOWN_ERROR;
        }
        switch (iArr[type.ordinal()]) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
                return z ? DiagnosisConstants.DRCS_KEY_MAAP_MO_FAIL_NETWORK : DiagnosisConstants.DRCS_KEY_RCS_MO_FAIL_NETWORK;
            default:
                return z ? DiagnosisConstants.DRCS_KEY_MAAP_MO_FAIL_TERMINAL : DiagnosisConstants.DRCS_KEY_RCS_MO_FAIL_TERMINAL;
        }
    }

    private String getOrst(Result.Type type) {
        int i = AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$servicemodules$im$result$Result$Type[type.ordinal()];
        if (i == 1) {
            return "1";
        }
        if (i == 2) {
            return "2";
        }
        if (i != 7) {
            return i != 8 ? DiagnosisConstants.RCSM_ORST_ITER : DiagnosisConstants.RCSM_ORST_REGI;
        }
        return DiagnosisConstants.RCSM_ORST_HTTP;
    }

    private String getCause(Result result) {
        int i = AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$servicemodules$im$result$Result$Type[result.getType().ordinal()];
        if (i != 1) {
            if (i != 2) {
                return result.getType().toString();
            }
            if (result.getMsrpResponse() != null) {
                return String.valueOf(result.getMsrpResponse().getId());
            }
            return null;
        } else if (result.getSipResponse() != null) {
            return String.valueOf(result.getSipResponse().getId());
        } else {
            return null;
        }
    }

    /* renamed from: com.sec.internal.ims.servicemodules.im.RcsBigDataProcessor$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImConstants$ChatbotTrafficType;
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$result$Result$Type;

        /* JADX WARNING: Can't wrap try/catch for region: R(26:0|(2:1|2)|3|(2:5|6)|7|(2:9|10)|11|(2:13|14)|15|(2:17|18)|19|21|22|23|24|25|26|27|28|29|30|31|32|33|34|(3:35|36|38)) */
        /* JADX WARNING: Can't wrap try/catch for region: R(29:0|(2:1|2)|3|(2:5|6)|7|9|10|11|(2:13|14)|15|(2:17|18)|19|21|22|23|24|25|26|27|28|29|30|31|32|33|34|35|36|38) */
        /* JADX WARNING: Can't wrap try/catch for region: R(31:0|1|2|3|(2:5|6)|7|9|10|11|13|14|15|(2:17|18)|19|21|22|23|24|25|26|27|28|29|30|31|32|33|34|35|36|38) */
        /* JADX WARNING: Can't wrap try/catch for region: R(32:0|1|2|3|(2:5|6)|7|9|10|11|13|14|15|17|18|19|21|22|23|24|25|26|27|28|29|30|31|32|33|34|35|36|38) */
        /* JADX WARNING: Can't wrap try/catch for region: R(33:0|1|2|3|5|6|7|9|10|11|13|14|15|17|18|19|21|22|23|24|25|26|27|28|29|30|31|32|33|34|35|36|38) */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:23:0x004f */
        /* JADX WARNING: Missing exception handler attribute for start block: B:25:0x0059 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:27:0x0063 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:29:0x006d */
        /* JADX WARNING: Missing exception handler attribute for start block: B:31:0x0077 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:33:0x0082 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:35:0x008d */
        static {
            /*
                com.sec.internal.constants.ims.servicemodules.im.ImConstants$ChatbotTrafficType[] r0 = com.sec.internal.constants.ims.servicemodules.im.ImConstants.ChatbotTrafficType.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImConstants$ChatbotTrafficType = r0
                r1 = 1
                com.sec.internal.constants.ims.servicemodules.im.ImConstants$ChatbotTrafficType r2 = com.sec.internal.constants.ims.servicemodules.im.ImConstants.ChatbotTrafficType.NONE     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r2 = r2.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r0[r2] = r1     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                r0 = 2
                int[] r2 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImConstants$ChatbotTrafficType     // Catch:{ NoSuchFieldError -> 0x001d }
                com.sec.internal.constants.ims.servicemodules.im.ImConstants$ChatbotTrafficType r3 = com.sec.internal.constants.ims.servicemodules.im.ImConstants.ChatbotTrafficType.ADVERTISEMENT     // Catch:{ NoSuchFieldError -> 0x001d }
                int r3 = r3.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2[r3] = r0     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                r2 = 3
                int[] r3 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImConstants$ChatbotTrafficType     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.sec.internal.constants.ims.servicemodules.im.ImConstants$ChatbotTrafficType r4 = com.sec.internal.constants.ims.servicemodules.im.ImConstants.ChatbotTrafficType.PAYMENT     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r4 = r4.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r3[r4] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                r3 = 4
                int[] r4 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImConstants$ChatbotTrafficType     // Catch:{ NoSuchFieldError -> 0x0033 }
                com.sec.internal.constants.ims.servicemodules.im.ImConstants$ChatbotTrafficType r5 = com.sec.internal.constants.ims.servicemodules.im.ImConstants.ChatbotTrafficType.SUBSCRIPTION     // Catch:{ NoSuchFieldError -> 0x0033 }
                int r5 = r5.ordinal()     // Catch:{ NoSuchFieldError -> 0x0033 }
                r4[r5] = r3     // Catch:{ NoSuchFieldError -> 0x0033 }
            L_0x0033:
                r4 = 5
                int[] r5 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImConstants$ChatbotTrafficType     // Catch:{ NoSuchFieldError -> 0x003e }
                com.sec.internal.constants.ims.servicemodules.im.ImConstants$ChatbotTrafficType r6 = com.sec.internal.constants.ims.servicemodules.im.ImConstants.ChatbotTrafficType.PREMIUM     // Catch:{ NoSuchFieldError -> 0x003e }
                int r6 = r6.ordinal()     // Catch:{ NoSuchFieldError -> 0x003e }
                r5[r6] = r4     // Catch:{ NoSuchFieldError -> 0x003e }
            L_0x003e:
                com.sec.internal.constants.ims.servicemodules.im.result.Result$Type[] r5 = com.sec.internal.constants.ims.servicemodules.im.result.Result.Type.values()
                int r5 = r5.length
                int[] r5 = new int[r5]
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$result$Result$Type = r5
                com.sec.internal.constants.ims.servicemodules.im.result.Result$Type r6 = com.sec.internal.constants.ims.servicemodules.im.result.Result.Type.SIP_ERROR     // Catch:{ NoSuchFieldError -> 0x004f }
                int r6 = r6.ordinal()     // Catch:{ NoSuchFieldError -> 0x004f }
                r5[r6] = r1     // Catch:{ NoSuchFieldError -> 0x004f }
            L_0x004f:
                int[] r1 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$result$Result$Type     // Catch:{ NoSuchFieldError -> 0x0059 }
                com.sec.internal.constants.ims.servicemodules.im.result.Result$Type r5 = com.sec.internal.constants.ims.servicemodules.im.result.Result.Type.MSRP_ERROR     // Catch:{ NoSuchFieldError -> 0x0059 }
                int r5 = r5.ordinal()     // Catch:{ NoSuchFieldError -> 0x0059 }
                r1[r5] = r0     // Catch:{ NoSuchFieldError -> 0x0059 }
            L_0x0059:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$result$Result$Type     // Catch:{ NoSuchFieldError -> 0x0063 }
                com.sec.internal.constants.ims.servicemodules.im.result.Result$Type r1 = com.sec.internal.constants.ims.servicemodules.im.result.Result.Type.SESSION_RELEASE     // Catch:{ NoSuchFieldError -> 0x0063 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0063 }
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0063 }
            L_0x0063:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$result$Result$Type     // Catch:{ NoSuchFieldError -> 0x006d }
                com.sec.internal.constants.ims.servicemodules.im.result.Result$Type r1 = com.sec.internal.constants.ims.servicemodules.im.result.Result.Type.NETWORK_ERROR     // Catch:{ NoSuchFieldError -> 0x006d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x006d }
                r0[r1] = r3     // Catch:{ NoSuchFieldError -> 0x006d }
            L_0x006d:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$result$Result$Type     // Catch:{ NoSuchFieldError -> 0x0077 }
                com.sec.internal.constants.ims.servicemodules.im.result.Result$Type r1 = com.sec.internal.constants.ims.servicemodules.im.result.Result.Type.DEDICATED_BEARER_ERROR     // Catch:{ NoSuchFieldError -> 0x0077 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0077 }
                r0[r1] = r4     // Catch:{ NoSuchFieldError -> 0x0077 }
            L_0x0077:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$result$Result$Type     // Catch:{ NoSuchFieldError -> 0x0082 }
                com.sec.internal.constants.ims.servicemodules.im.result.Result$Type r1 = com.sec.internal.constants.ims.servicemodules.im.result.Result.Type.REMOTE_PARTY_CANCELED     // Catch:{ NoSuchFieldError -> 0x0082 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0082 }
                r2 = 6
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0082 }
            L_0x0082:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$result$Result$Type     // Catch:{ NoSuchFieldError -> 0x008d }
                com.sec.internal.constants.ims.servicemodules.im.result.Result$Type r1 = com.sec.internal.constants.ims.servicemodules.im.result.Result.Type.HTTP_ERROR     // Catch:{ NoSuchFieldError -> 0x008d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x008d }
                r2 = 7
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x008d }
            L_0x008d:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$result$Result$Type     // Catch:{ NoSuchFieldError -> 0x0099 }
                com.sec.internal.constants.ims.servicemodules.im.result.Result$Type r1 = com.sec.internal.constants.ims.servicemodules.im.result.Result.Type.DEVICE_UNREGISTERED     // Catch:{ NoSuchFieldError -> 0x0099 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0099 }
                r2 = 8
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0099 }
            L_0x0099:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.RcsBigDataProcessor.AnonymousClass1.<clinit>():void");
        }
    }

    private String getChatBotTrafficType(ImConstants.ChatbotTrafficType chatbotTrafficType) {
        int i = AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImConstants$ChatbotTrafficType[chatbotTrafficType.ordinal()];
        if (i == 1) {
            return DiagnosisConstants.DRCS_KEY_MAAP_TRAFFIC_TYPE_NONE;
        }
        if (i == 2) {
            return DiagnosisConstants.DRCS_KEY_MAAP_TRAFFIC_TYPE_ADVERTISEMENT;
        }
        if (i == 3) {
            return DiagnosisConstants.DRCS_KEY_MAAP_TRAFFIC_TYPE_PAYMENT;
        }
        if (i != 4) {
            return i != 5 ? "" : DiagnosisConstants.DRCS_KEY_MAAP_TRAFFIC_TYPE_PREMIUM;
        }
        return DiagnosisConstants.DRCS_KEY_MAAP_TRAFFIC_TYPE_SUBSCRIPTION;
    }
}
