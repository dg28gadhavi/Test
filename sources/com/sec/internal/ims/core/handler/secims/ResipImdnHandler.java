package com.sec.internal.ims.core.handler.secims;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.google.flatbuffers.FlatBufferBuilder;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.ImImdnRecRoute;
import com.sec.internal.constants.ims.servicemodules.im.NotificationStatus;
import com.sec.internal.constants.ims.servicemodules.im.event.ImdnNotificationEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.ImdnResponseReceivedEvent;
import com.sec.internal.constants.ims.servicemodules.im.result.Result;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.Iso8601;
import com.sec.internal.helper.Registrant;
import com.sec.internal.helper.RegistrantList;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImNotificationParam;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImdnRecRoute;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ImNotificationStatusReceived;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ImdnResponseReceived;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Response_.SendImNotiResponse;
import com.sec.internal.ims.translate.ResipTranslatorCollection;
import com.sec.internal.interfaces.ims.IImsFramework;
import com.sec.internal.log.IMSLog;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ResipImdnHandler extends Handler {
    private static final int EVENT_IMDN_NOTI = 2;
    private static final int EVENT_IMDN_RESPONSE = 1;
    private static final String LOG_TAG = ResipImdnHandler.class.getSimpleName();
    private final RegistrantList mImdnNotificationRegistrants = new RegistrantList();
    private final RegistrantList mImdnResponseRegistransts = new RegistrantList();
    private final IImsFramework mImsFramework;

    private String parseStr(String str) {
        return str != null ? str : "";
    }

    public ResipImdnHandler(Looper looper, IImsFramework iImsFramework) {
        super(looper);
        this.mImsFramework = iImsFramework;
        StackIF.getInstance().registerImdnHandler(this, 2, (Object) null);
    }

    public void handleMessage(Message message) {
        int i = message.what;
        if (i == 1) {
            AsyncResult asyncResult = (AsyncResult) message.obj;
            handleSendImdnNotificationResponse((Message) asyncResult.userObj, (SendImNotiResponse) asyncResult.result);
        } else if (i == 2) {
            handleNotify((Notify) ((AsyncResult) message.obj).result);
        }
    }

    private void handleNotify(Notify notify) {
        int notifyid = notify.notifyid();
        if (notifyid == 11006) {
            handleImdnReceivedNotify(notify);
        } else if (notifyid == 11015) {
            handleSendImdnResponseNotify(notify);
        }
    }

    /* access modifiers changed from: package-private */
    public void registerForImdnNotification(Handler handler, int i, Object obj) {
        this.mImdnNotificationRegistrants.add(new Registrant(handler, i, obj));
    }

    /* access modifiers changed from: package-private */
    public void unregisterForImdnNotification(Handler handler) {
        this.mImdnNotificationRegistrants.remove(handler);
    }

    /* access modifiers changed from: package-private */
    public void registerForImdnResponse(Handler handler, int i, Object obj) {
        this.mImdnResponseRegistransts.add(new Registrant(handler, i, obj));
    }

    /* access modifiers changed from: package-private */
    public void unregisterForImdnResponse(Handler handler) {
        this.mImdnResponseRegistransts.remove(handler);
    }

    private int[] getImdnRecRouteOffsetArray(FlatBufferBuilder flatBufferBuilder, List<ImImdnRecRoute> list, int i) {
        int[] iArr = new int[i];
        int i2 = 0;
        for (ImImdnRecRoute next : list) {
            int createString = flatBufferBuilder.createString((CharSequence) next.getRecordRouteDispName());
            int createString2 = flatBufferBuilder.createString((CharSequence) next.getRecordRouteUri());
            ImdnRecRoute.startImdnRecRoute(flatBufferBuilder);
            ImdnRecRoute.addName(flatBufferBuilder, createString);
            ImdnRecRoute.addUri(flatBufferBuilder, createString2);
            iArr[i2] = ImdnRecRoute.endImdnRecRoute(flatBufferBuilder);
            i2++;
        }
        return iArr;
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x0118 A[SYNTHETIC, Splitter:B:42:0x0118] */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x01df  */
    /* JADX WARNING: Removed duplicated region for block: B:69:0x01e8  */
    /* JADX WARNING: Removed duplicated region for block: B:72:0x01f1  */
    /* JADX WARNING: Removed duplicated region for block: B:85:0x0279  */
    /* JADX WARNING: Removed duplicated region for block: B:86:0x01b6 A[EDGE_INSN: B:86:0x01b6->B:63:0x01b6 ?: BREAK  , SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:92:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void sendDispositionNotification(com.sec.internal.constants.ims.servicemodules.im.params.SendImdnParams r28, int r29, int r30) {
        /*
            r27 = this;
            r1 = r27
            r2 = r28
            r0 = r29
            r3 = r30
            java.lang.String r4 = LOG_TAG
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r6 = "sendDispositionNotification(): service = "
            r5.append(r6)
            r5.append(r0)
            java.lang.String r6 = ", sessionHandle = "
            r5.append(r6)
            r5.append(r3)
            java.lang.String r5 = r5.toString()
            android.util.Log.i(r4, r5)
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r6 = "sendDispositionNotification(): "
            r5.append(r6)
            r5.append(r2)
            java.lang.String r5 = r5.toString()
            android.util.Log.i(r4, r5)
            com.sec.internal.interfaces.ims.IImsFramework r5 = r1.mImsFramework
            com.sec.internal.interfaces.ims.core.IRegistrationManager r5 = r5.getRegistrationManager()
            if (r0 == 0) goto L_0x005d
            r6 = 2
            if (r0 == r6) goto L_0x0052
            java.lang.String r6 = "im"
            java.lang.String r7 = r2.mOwnImsi
            com.sec.internal.interfaces.ims.core.IUserAgent r5 = r5.getUserAgentByImsi(r6, r7)
            com.sec.internal.ims.core.handler.secims.UserAgent r5 = (com.sec.internal.ims.core.handler.secims.UserAgent) r5
            goto L_0x0068
        L_0x0052:
            java.lang.String r6 = "ft"
            java.lang.String r7 = r2.mOwnImsi
            com.sec.internal.interfaces.ims.core.IUserAgent r5 = r5.getUserAgentByImsi(r6, r7)
            com.sec.internal.ims.core.handler.secims.UserAgent r5 = (com.sec.internal.ims.core.handler.secims.UserAgent) r5
            goto L_0x0068
        L_0x005d:
            java.lang.String r6 = "slm"
            java.lang.String r7 = r2.mOwnImsi
            com.sec.internal.interfaces.ims.core.IUserAgent r5 = r5.getUserAgentByImsi(r6, r7)
            com.sec.internal.ims.core.handler.secims.UserAgent r5 = (com.sec.internal.ims.core.handler.secims.UserAgent) r5
        L_0x0068:
            if (r5 != 0) goto L_0x0081
            java.lang.String r0 = "sendDispositionNotification(): UserAgent not found."
            android.util.Log.e(r4, r0)
            android.os.Message r0 = r2.mCallback
            if (r0 == 0) goto L_0x0080
            com.sec.internal.constants.ims.servicemodules.im.result.Result r2 = new com.sec.internal.constants.ims.servicemodules.im.result.Result
            com.sec.internal.constants.ims.servicemodules.im.ImError r3 = com.sec.internal.constants.ims.servicemodules.im.ImError.ENGINE_ERROR
            com.sec.internal.constants.ims.servicemodules.im.result.Result$Type r4 = com.sec.internal.constants.ims.servicemodules.im.result.Result.Type.ENGINE_ERROR
            r2.<init>((com.sec.internal.constants.ims.servicemodules.im.ImError) r3, (com.sec.internal.constants.ims.servicemodules.im.result.Result.Type) r4)
            r1.sendCallback(r0, r2)
        L_0x0080:
            return
        L_0x0081:
            int r6 = r5.getHandle()
            com.google.flatbuffers.FlatBufferBuilder r7 = new com.google.flatbuffers.FlatBufferBuilder
            r8 = 0
            r7.<init>(r8)
            com.sec.ims.util.ImsUri r9 = r2.mUri     // Catch:{ NullPointerException -> 0x025a }
            java.lang.String r10 = ""
            if (r9 != 0) goto L_0x0093
            r9 = r10
            goto L_0x0097
        L_0x0093:
            java.lang.String r9 = r9.toString()     // Catch:{ NullPointerException -> 0x025a }
        L_0x0097:
            int r9 = r7.createString((java.lang.CharSequence) r9)     // Catch:{ NullPointerException -> 0x025a }
            java.lang.String r11 = r2.mConversationId     // Catch:{ NullPointerException -> 0x025a }
            if (r11 == 0) goto L_0x00a0
            goto L_0x00a1
        L_0x00a0:
            r11 = r10
        L_0x00a1:
            int r11 = r7.createString((java.lang.CharSequence) r11)     // Catch:{ NullPointerException -> 0x025a }
            java.lang.String r12 = r2.mContributionId     // Catch:{ NullPointerException -> 0x025a }
            if (r12 == 0) goto L_0x00aa
            goto L_0x00ab
        L_0x00aa:
            r12 = r10
        L_0x00ab:
            int r12 = r7.createString((java.lang.CharSequence) r12)     // Catch:{ NullPointerException -> 0x025a }
            java.lang.String r13 = r2.mDeviceId     // Catch:{ NullPointerException -> 0x025a }
            if (r13 == 0) goto L_0x00b4
            r10 = r13
        L_0x00b4:
            int r10 = r7.createString((java.lang.CharSequence) r10)     // Catch:{ NullPointerException -> 0x025a }
            java.util.Date r13 = r2.mCpimDate     // Catch:{ NullPointerException -> 0x025a }
            java.lang.String r13 = com.sec.internal.helper.Iso8601.formatMillis(r13)     // Catch:{ NullPointerException -> 0x025a }
            java.lang.String r13 = r1.parseStr(r13)     // Catch:{ NullPointerException -> 0x025a }
            int r13 = r7.createString((java.lang.CharSequence) r13)     // Catch:{ NullPointerException -> 0x025a }
            java.lang.String r14 = r2.mUserAlias     // Catch:{ NullPointerException -> 0x025a }
            java.lang.String r14 = r1.parseStr(r14)     // Catch:{ NullPointerException -> 0x025a }
            int r14 = r7.createString((java.lang.CharSequence) r14)     // Catch:{ NullPointerException -> 0x025a }
            java.util.Map<java.lang.String, java.lang.String> r15 = r2.mImExtensionMNOHeaders     // Catch:{ NullPointerException -> 0x025a }
            java.lang.String r8 = "sendDispositionNotification(): headers "
            r17 = -1
            if (r15 == 0) goto L_0x00fc
            boolean r15 = r15.isEmpty()     // Catch:{ NullPointerException -> 0x025a }
            if (r15 != 0) goto L_0x00fc
            java.lang.StringBuilder r15 = new java.lang.StringBuilder     // Catch:{ NullPointerException -> 0x025a }
            r15.<init>()     // Catch:{ NullPointerException -> 0x025a }
            r15.append(r8)     // Catch:{ NullPointerException -> 0x025a }
            r18 = r5
            java.util.Map<java.lang.String, java.lang.String> r5 = r2.mImExtensionMNOHeaders     // Catch:{ NullPointerException -> 0x025a }
            r15.append(r5)     // Catch:{ NullPointerException -> 0x025a }
            java.lang.String r5 = r15.toString()     // Catch:{ NullPointerException -> 0x025a }
            android.util.Log.i(r4, r5)     // Catch:{ NullPointerException -> 0x025a }
            java.util.Map<java.lang.String, java.lang.String> r4 = r2.mImExtensionMNOHeaders     // Catch:{ NullPointerException -> 0x025a }
            int r4 = com.sec.internal.ims.translate.ResipTranslatorCollection.translateStackImExtensionHeaders(r7, r4)     // Catch:{ NullPointerException -> 0x025a }
            goto L_0x0100
        L_0x00fc:
            r18 = r5
            r4 = r17
        L_0x0100:
            java.util.List<com.sec.internal.constants.ims.servicemodules.im.params.SendImdnParams$ImdnData> r5 = r2.mImdnDataList     // Catch:{ NullPointerException -> 0x025a }
            int r5 = r5.size()     // Catch:{ NullPointerException -> 0x025a }
            int[] r5 = new int[r5]     // Catch:{ NullPointerException -> 0x025a }
            java.util.List<com.sec.internal.constants.ims.servicemodules.im.params.SendImdnParams$ImdnData> r15 = r2.mImdnDataList     // Catch:{ NullPointerException -> 0x025a }
            java.util.Iterator r15 = r15.iterator()     // Catch:{ NullPointerException -> 0x025a }
            r19 = 0
        L_0x0110:
            boolean r20 = r15.hasNext()     // Catch:{ NullPointerException -> 0x025a }
            r21 = r14
            if (r20 == 0) goto L_0x01b6
            java.lang.Object r20 = r15.next()     // Catch:{ NullPointerException -> 0x0256 }
            r14 = r20
            com.sec.internal.constants.ims.servicemodules.im.params.SendImdnParams$ImdnData r14 = (com.sec.internal.constants.ims.servicemodules.im.params.SendImdnParams.ImdnData) r14     // Catch:{ NullPointerException -> 0x0256 }
            r20 = r15
            java.util.List<com.sec.internal.constants.ims.servicemodules.im.ImImdnRecRoute> r15 = r14.mImdnRecRouteList     // Catch:{ NullPointerException -> 0x0256 }
            r22 = r4
            if (r15 == 0) goto L_0x0135
            int r4 = r15.size()     // Catch:{ NullPointerException -> 0x025a }
            int[] r4 = r1.getImdnRecRouteOffsetArray(r7, r15, r4)     // Catch:{ NullPointerException -> 0x025a }
            int r4 = com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImNotificationParam.createImdnRecRouteVector(r7, r4)     // Catch:{ NullPointerException -> 0x025a }
            goto L_0x0137
        L_0x0135:
            r4 = r17
        L_0x0137:
            java.lang.String r15 = r14.mImdnId     // Catch:{ NullPointerException -> 0x0256 }
            java.lang.String r15 = r1.parseStr(r15)     // Catch:{ NullPointerException -> 0x0256 }
            int r15 = r7.createString((java.lang.CharSequence) r15)     // Catch:{ NullPointerException -> 0x0256 }
            r23 = r8
            java.util.Date r8 = r14.mImdnDate     // Catch:{ NullPointerException -> 0x0256 }
            java.lang.String r8 = com.sec.internal.helper.Iso8601.formatMillis(r8)     // Catch:{ NullPointerException -> 0x0256 }
            java.lang.String r8 = r1.parseStr(r8)     // Catch:{ NullPointerException -> 0x0256 }
            int r8 = r7.createString((java.lang.CharSequence) r8)     // Catch:{ NullPointerException -> 0x0256 }
            r24 = r10
            java.lang.String r10 = r14.mImdnOriginalTo     // Catch:{ NullPointerException -> 0x0256 }
            if (r10 == 0) goto L_0x0160
            java.lang.String r10 = r1.parseStr(r10)     // Catch:{ NullPointerException -> 0x025a }
            int r10 = r7.createString((java.lang.CharSequence) r10)     // Catch:{ NullPointerException -> 0x025a }
            goto L_0x0162
        L_0x0160:
            r10 = r17
        L_0x0162:
            java.util.HashSet r1 = new java.util.HashSet     // Catch:{ NullPointerException -> 0x0256 }
            r25 = r12
            r12 = 1
            com.sec.internal.constants.ims.servicemodules.im.NotificationStatus[] r12 = new com.sec.internal.constants.ims.servicemodules.im.NotificationStatus[r12]     // Catch:{ NullPointerException -> 0x0256 }
            r26 = r11
            com.sec.internal.constants.ims.servicemodules.im.NotificationStatus r11 = r14.mStatus     // Catch:{ NullPointerException -> 0x0256 }
            r16 = 0
            r12[r16] = r11     // Catch:{ NullPointerException -> 0x0256 }
            java.util.List r11 = java.util.Arrays.asList(r12)     // Catch:{ NullPointerException -> 0x0256 }
            r1.<init>(r11)     // Catch:{ NullPointerException -> 0x0256 }
            int[] r1 = com.sec.internal.ims.translate.ResipTranslatorCollection.translateFwImdnNoti(r1)     // Catch:{ NullPointerException -> 0x0256 }
            int r1 = com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImNotificationParam.createStatusVector(r7, r1)     // Catch:{ NullPointerException -> 0x0256 }
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImNotificationParam.startImNotificationParam(r7)     // Catch:{ NullPointerException -> 0x0256 }
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImNotificationParam.addImdnMessageId(r7, r15)     // Catch:{ NullPointerException -> 0x0256 }
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImNotificationParam.addStatus(r7, r1)     // Catch:{ NullPointerException -> 0x0256 }
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImNotificationParam.addImdnDateTime(r7, r8)     // Catch:{ NullPointerException -> 0x0256 }
            java.util.List<com.sec.internal.constants.ims.servicemodules.im.ImImdnRecRoute> r1 = r14.mImdnRecRouteList     // Catch:{ NullPointerException -> 0x0256 }
            if (r1 == 0) goto L_0x0193
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImNotificationParam.addImdnRecRoute(r7, r4)     // Catch:{ NullPointerException -> 0x0256 }
        L_0x0193:
            java.lang.String r1 = r14.mImdnOriginalTo     // Catch:{ NullPointerException -> 0x0256 }
            if (r1 == 0) goto L_0x019a
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImNotificationParam.addImdnOriginalTo(r7, r10)     // Catch:{ NullPointerException -> 0x0256 }
        L_0x019a:
            int r1 = r19 + 1
            int r4 = com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImNotificationParam.endImNotificationParam(r7)     // Catch:{ NullPointerException -> 0x0256 }
            r5[r19] = r4     // Catch:{ NullPointerException -> 0x0256 }
            r19 = r1
            r15 = r20
            r14 = r21
            r4 = r22
            r8 = r23
            r10 = r24
            r12 = r25
            r11 = r26
            r1 = r27
            goto L_0x0110
        L_0x01b6:
            r22 = r4
            r23 = r8
            r24 = r10
            r26 = r11
            r25 = r12
            int r1 = com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestSendImNotificationStatus.createNotificationsVector(r7, r5)     // Catch:{ NullPointerException -> 0x0256 }
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestSendImNotificationStatus.startRequestSendImNotificationStatus(r7)
            long r3 = (long) r3
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestSendImNotificationStatus.addSessionId(r7, r3)
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestSendImNotificationStatus.addNotifications(r7, r1)
            long r3 = (long) r6
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestSendImNotificationStatus.addRegistrationHandle(r7, r3)
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestSendImNotificationStatus.addUri(r7, r9)
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestSendImNotificationStatus.addService(r7, r0)
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestSendImNotificationStatus.addCpimDateTime(r7, r13)
            java.lang.String r0 = r2.mConversationId
            if (r0 == 0) goto L_0x01e4
            r0 = r26
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestSendImNotificationStatus.addConversationId(r7, r0)
        L_0x01e4:
            java.lang.String r0 = r2.mContributionId
            if (r0 == 0) goto L_0x01ed
            r0 = r25
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestSendImNotificationStatus.addContributionId(r7, r0)
        L_0x01ed:
            java.lang.String r0 = r2.mDeviceId
            if (r0 == 0) goto L_0x01f6
            r0 = r24
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestSendImNotificationStatus.addDeviceId(r7, r0)
        L_0x01f6:
            java.util.Map<java.lang.String, java.lang.String> r0 = r2.mImExtensionMNOHeaders
            if (r0 == 0) goto L_0x021d
            boolean r0 = r0.isEmpty()
            if (r0 != 0) goto L_0x021d
            java.lang.String r0 = LOG_TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            r3 = r23
            r1.append(r3)
            java.util.Map<java.lang.String, java.lang.String> r3 = r2.mImExtensionMNOHeaders
            r1.append(r3)
            java.lang.String r1 = r1.toString()
            android.util.Log.i(r0, r1)
            r4 = r22
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestSendImNotificationStatus.addExtension(r7, r4)
        L_0x021d:
            boolean r0 = r2.mIsGroupChat
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestSendImNotificationStatus.addIsGroupChat(r7, r0)
            boolean r0 = r2.mIsBotSessionAnonymized
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestSendImNotificationStatus.addIsBotSessionAnonymized(r7, r0)
            r0 = r21
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestSendImNotificationStatus.addUserAlias(r7, r0)
            int r0 = com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestSendImNotificationStatus.endRequestSendImNotificationStatus(r7)
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request.startRequest(r7)
            r1 = 506(0x1fa, float:7.09E-43)
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request.addReqid(r7, r1)
            r1 = 45
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request.addReqType(r7, r1)
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request.addReq(r7, r0)
            int r5 = com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request.endRequest(r7)
            r3 = 506(0x1fa, float:7.09E-43)
            android.os.Message r0 = r2.mCallback
            r2 = 1
            r1 = r27
            android.os.Message r6 = r1.obtainMessage(r2, r0)
            r2 = r18
            r4 = r7
            r1.sendRequestToStack(r2, r3, r4, r5, r6)
            return
        L_0x0256:
            r0 = move-exception
            r1 = r27
            goto L_0x025b
        L_0x025a:
            r0 = move-exception
        L_0x025b:
            java.lang.String r3 = LOG_TAG
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r5 = "Discard sendDispositionNotification(): "
            r4.append(r5)
            java.lang.String r0 = r0.getMessage()
            r4.append(r0)
            java.lang.String r0 = r4.toString()
            android.util.Log.i(r3, r0)
            android.os.Message r0 = r2.mCallback
            if (r0 == 0) goto L_0x0285
            com.sec.internal.constants.ims.servicemodules.im.result.Result r2 = new com.sec.internal.constants.ims.servicemodules.im.result.Result
            com.sec.internal.constants.ims.servicemodules.im.ImError r3 = com.sec.internal.constants.ims.servicemodules.im.ImError.ENGINE_ERROR
            com.sec.internal.constants.ims.servicemodules.im.result.Result$Type r4 = com.sec.internal.constants.ims.servicemodules.im.result.Result.Type.ENGINE_ERROR
            r2.<init>((com.sec.internal.constants.ims.servicemodules.im.ImError) r3, (com.sec.internal.constants.ims.servicemodules.im.result.Result.Type) r4)
            r1.sendCallback(r0, r2)
        L_0x0285:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.handler.secims.ResipImdnHandler.sendDispositionNotification(com.sec.internal.constants.ims.servicemodules.im.params.SendImdnParams, int, int):void");
    }

    private void handleSendImdnNotificationResponse(Message message, SendImNotiResponse sendImNotiResponse) {
        Log.i(LOG_TAG, "handleSendImdnNotificationResponse()");
        Result translateImResult = ResipTranslatorCollection.translateImResult(sendImNotiResponse.imError(), (Object) null);
        if (message != null) {
            sendCallback(message, translateImResult);
        }
    }

    private void handleImdnReceivedNotify(Notify notify) {
        Date date;
        Date date2;
        if (notify.notiType() != 34) {
            Log.e(LOG_TAG, "handleImNotiReceivedNotify(): invalid notify");
            return;
        }
        ImNotificationStatusReceived imNotificationStatusReceived = (ImNotificationStatusReceived) notify.noti(new ImNotificationStatusReceived());
        ImNotificationParam status = imNotificationStatusReceived.status();
        if (status == null) {
            Log.e(LOG_TAG, "handleImNotiReceivedNotify(): param is null");
            return;
        }
        try {
            date = imNotificationStatusReceived.cpimDateTime() != null ? Iso8601.parse(imNotificationStatusReceived.cpimDateTime()) : new Date();
        } catch (ParseException e) {
            Log.e(LOG_TAG, e.toString());
            date = new Date();
        }
        Date date3 = date;
        try {
            date2 = status.imdnDateTime() != null ? Iso8601.parse(status.imdnDateTime()) : date3;
        } catch (ParseException e2) {
            Log.e(LOG_TAG, e2.toString());
            date2 = date3;
        }
        ImsUri parse = ImsUri.parse(imNotificationStatusReceived.uri());
        if (parse == null) {
            String str = LOG_TAG;
            Log.i(str, "Invalid remote uri, return. uri=" + imNotificationStatusReceived.uri());
            return;
        }
        if (parse.getParam("tk") != null) {
            parse.removeParam("tk");
        }
        ImdnNotificationEvent imdnNotificationEvent = new ImdnNotificationEvent(status.imdnMessageId(), date2, parse, imNotificationStatusReceived.conversationId(), this.mImsFramework.getRegistrationManager().getImsiByUserAgentHandle((int) imNotificationStatusReceived.userHandle()), translateNotificationType(status.status(0)), date3, imNotificationStatusReceived.userAlias());
        String str2 = LOG_TAG;
        IMSLog.s(str2, "handleImNotiReceivedNotify: " + imdnNotificationEvent);
        this.mImdnNotificationRegistrants.notifyRegistrants(new AsyncResult((Object) null, imdnNotificationEvent, (Throwable) null));
    }

    private void handleSendImdnResponseNotify(Notify notify) {
        if (notify.notiType() != 37) {
            Log.e(LOG_TAG, "handleSendImdnResponseNotify(): invalid notify");
            return;
        }
        ImdnResponseReceived imdnResponseReceived = (ImdnResponseReceived) notify.noti(new ImdnResponseReceived());
        Result translateImResult = ResipTranslatorCollection.translateImResult(imdnResponseReceived.imError(), (Object) null);
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < imdnResponseReceived.imdnMessageIdLength(); i++) {
            arrayList.add(imdnResponseReceived.imdnMessageId(i));
        }
        ImdnResponseReceivedEvent imdnResponseReceivedEvent = new ImdnResponseReceivedEvent(translateImResult, arrayList);
        String str = LOG_TAG;
        IMSLog.s(str, "handleSendImdnResponseNotify() Event : " + imdnResponseReceivedEvent);
        this.mImdnResponseRegistransts.notifyRegistrants(new AsyncResult((Object) null, imdnResponseReceivedEvent, (Throwable) null));
    }

    private NotificationStatus translateNotificationType(int i) {
        if (i == 0) {
            return NotificationStatus.DELIVERED;
        }
        if (i == 1) {
            return NotificationStatus.DISPLAYED;
        }
        if (i == 2) {
            return NotificationStatus.INTERWORKING_SMS;
        }
        if (i == 3) {
            return NotificationStatus.INTERWORKING_MMS;
        }
        if (i != 4) {
            return NotificationStatus.DELIVERED;
        }
        return NotificationStatus.CANCELED;
    }

    private void sendRequestToStack(UserAgent userAgent, int i, FlatBufferBuilder flatBufferBuilder, int i2, Message message) {
        if (userAgent == null) {
            Log.e(LOG_TAG, "sendRequestToStack(): UserAgent not found.");
        } else {
            userAgent.sendRequestToStack(new ResipStackRequest(i, flatBufferBuilder, i2, message));
        }
    }

    private void sendCallback(Message message, Object obj) {
        AsyncResult.forMessage(message, obj, (Throwable) null);
        message.sendToTarget();
    }
}
