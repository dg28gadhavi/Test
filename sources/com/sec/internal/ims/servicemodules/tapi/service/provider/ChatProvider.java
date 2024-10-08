package com.sec.internal.ims.servicemodules.tapi.service.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;
import com.gsma.services.rcs.chat.ChatLog;
import com.gsma.services.rcs.chat.GroupChat;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.servicemodules.im.ChatData;
import com.sec.internal.ims.servicemodules.csh.event.ICshConstants;
import com.sec.internal.ims.servicemodules.im.ImCache;
import com.sec.internal.ims.servicemodules.im.ImSession;
import java.util.Collection;

public class ChatProvider extends ContentProvider {
    private static final int CHATS = 1;
    private static final int CHATS_ID = 6;
    private static final String[] CHAT_COLUMS = {"_id", "chat_id", "state", "subject", "direction", "timestamp", "reason_code", "participants", ICshConstants.ShareDatabase.KEY_TARGET_CONTACT};
    private static final int CHAT_ID = 2;
    private static final String LOG_TAG = ChatProvider.class.getSimpleName();
    private static final int MESSAGES = 3;
    private static final int MESSAGES_CONTACTID = 5;
    private static final String[] MESSAGE_COLUNMS = {"_id", "chat_id", ICshConstants.ShareDatabase.KEY_TARGET_CONTACT, "msg_id", "mime_type", "content", "status", CloudMessageProviderContract.BufferDBMMSpdu.READ_STATUS, "direction", "timestamp", "timestamp_sent", "timestamp_delivered", "timestamp_displayed", "reason_code", "expired_delivery"};
    private static final int MESSAGE_ID = 4;
    private static final String PROVIDER_NAME;
    private static final UriMatcher uriMatcher;
    private ImCache mCache;

    public String getType(Uri uri) {
        return null;
    }

    static {
        String authority = ChatLog.GroupChat.CONTENT_URI.getAuthority();
        PROVIDER_NAME = authority;
        UriMatcher uriMatcher2 = new UriMatcher(-1);
        uriMatcher = uriMatcher2;
        uriMatcher2.addURI(authority, "groupchat", 1);
        uriMatcher2.addURI(authority, "groupchat/#", 2);
        uriMatcher2.addURI(authority, "groupchat/*", 6);
        uriMatcher2.addURI(authority, "chatmessage", 3);
        uriMatcher2.addURI(authority, "chatmessage/#", 4);
        uriMatcher2.addURI(authority, "chatmessage/*", 5);
    }

    public int delete(Uri uri, String str, String[] strArr) {
        throw new UnsupportedOperationException();
    }

    public Uri insert(Uri uri, ContentValues contentValues) {
        throw new UnsupportedOperationException();
    }

    public boolean onCreate() {
        this.mCache = ImCache.getInstance();
        return true;
    }

    public Cursor query(Uri uri, String[] strArr, String str, String[] strArr2, String str2) {
        String str3 = LOG_TAG;
        Log.d(str3, "query " + uri);
        if (!this.mCache.isLoaded()) {
            Log.e(str3, "ImCache is not ready yet.");
            return null;
        }
        switch (uriMatcher.match(uri)) {
            case 1:
                return buildChatCursor();
            case 2:
                return buildChatCursor(uri);
            case 3:
                return buildMessagesCursor((Uri) null, strArr, str, strArr2, str2);
            case 4:
                return buildMessagesCursor(uri, strArr, str, strArr2, str2);
            case 5:
                return buildMessagesCursor(uri, strArr, str, strArr2, str2);
            case 6:
                return buildChatCursor(uri);
            default:
                return null;
        }
    }

    public int update(Uri uri, ContentValues contentValues, String str, String[] strArr) {
        throw new UnsupportedOperationException();
    }

    private Cursor buildChatCursor(Uri uri) {
        MatrixCursor matrixCursor = new MatrixCursor(CHAT_COLUMS);
        String lastPathSegment = uri.getLastPathSegment();
        if (lastPathSegment == null) {
            return matrixCursor;
        }
        synchronized (this.mCache) {
            ImSession imSession = this.mCache.getImSession(lastPathSegment);
            if (imSession != null) {
                if (imSession.isGroupChat()) {
                    fillChatCursor(imSession, matrixCursor);
                    return matrixCursor;
                }
            }
            String str = LOG_TAG;
            Log.e(str, "buildChatCursor: Session not found " + lastPathSegment);
            return matrixCursor;
        }
    }

    private Cursor buildChatCursor() {
        MatrixCursor matrixCursor = new MatrixCursor(CHAT_COLUMS);
        synchronized (this.mCache) {
            Collection<ImSession> allImSessions = this.mCache.getAllImSessions();
            if (allImSessions == null) {
                return matrixCursor;
            }
            for (ImSession next : allImSessions) {
                if (next.isGroupChat()) {
                    fillChatCursor(next, matrixCursor);
                }
            }
            return matrixCursor;
        }
    }

    private void fillChatCursor(ImSession imSession, MatrixCursor matrixCursor) {
        int ordinal = GroupChat.State.INITIATING.ordinal();
        int chatStateId = imSession.getChatStateId();
        if (ChatData.State.ACTIVE.getId() == chatStateId) {
            ordinal = GroupChat.State.STARTED.ordinal();
        } else if (ChatData.State.CLOSED_BY_USER.getId() == chatStateId) {
            ordinal = GroupChat.State.ABORTED.ordinal();
        }
        matrixCursor.newRow().add(Long.valueOf((long) imSession.getId())).add(imSession.getChatId()).add(Integer.valueOf(ordinal)).add(imSession.getSubject()).add(Integer.valueOf(imSession.getDirection().getId())).add((Object) null).add((Object) null).add((Object) null);
    }

    private Cursor buildMessagesCursor(Uri uri, String[] strArr, String str, String[] strArr2, String str2) {
        String str3;
        if (strArr == null) {
            strArr = MESSAGE_COLUNMS;
        }
        String[] strArr3 = strArr;
        if (uri != null) {
            String lastPathSegment = uri.getLastPathSegment();
            if (lastPathSegment == null) {
                Log.e(LOG_TAG, "buildMessageCursor: No last segment.");
                return null;
            }
            str3 = lastPathSegment;
        } else {
            str3 = null;
        }
        return fillMessageCursor(str3, strArr3, str, strArr2, str2);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:100:0x025f, code lost:
        if (r1 != null) goto L_0x0261;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:102:?, code lost:
        r1.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:103:0x0265, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:104:0x0266, code lost:
        r2.addSuppressed(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:105:0x026a, code lost:
        throw r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:77:0x01e1, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:79:?, code lost:
        android.util.Log.e(LOG_TAG, "parse error: " + r0.getMessage() + ", Geo location body : " + r11);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:98:0x025d, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:99:0x025e, code lost:
        r2 = r0;
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [B:13:0x00a9, B:75:0x01d7] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private android.database.MatrixCursor fillMessageCursor(java.lang.String r21, java.lang.String[] r22, java.lang.String r23, java.lang.String[] r24, java.lang.String r25) {
        /*
            r20 = this;
            r0 = r21
            r1 = r23
            r2 = r25
            java.lang.String r3 = "rcs/groupchat-event"
            java.lang.String r4 = "application/geoloc"
            java.lang.String r5 = LOG_TAG
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            java.lang.String r7 = "fillMessageCursor idString: "
            r6.append(r7)
            r6.append(r0)
            java.lang.String r7 = ", projection: "
            r6.append(r7)
            java.lang.String r7 = java.util.Arrays.toString(r22)
            r6.append(r7)
            java.lang.String r7 = ", selection: "
            r6.append(r7)
            r6.append(r1)
            java.lang.String r7 = ", selectionArgs: "
            r6.append(r7)
            java.lang.String r7 = java.util.Arrays.toString(r24)
            r6.append(r7)
            java.lang.String r7 = ", sortOrder: "
            r6.append(r7)
            r6.append(r2)
            java.lang.String r6 = r6.toString()
            android.util.Log.d(r5, r6)
            java.lang.String r6 = "text/plain"
            if (r1 == 0) goto L_0x005b
            boolean r7 = r1.contains(r6)
            if (r7 == 0) goto L_0x005b
            java.lang.String r7 = "text/plain' OR mime_type ='text/plain;charset=UTF-8"
            java.lang.String r1 = r1.replace(r6, r7)
        L_0x005b:
            if (r0 == 0) goto L_0x0098
            boolean r7 = android.text.TextUtils.isEmpty(r1)
            if (r7 == 0) goto L_0x0075
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r7 = "msg_id = "
            r1.append(r7)
            r1.append(r0)
            java.lang.String r1 = r1.toString()
            goto L_0x0098
        L_0x0075:
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            r7.<init>()
            java.lang.String r8 = "("
            r7.append(r8)
            r7.append(r1)
            java.lang.String r1 = ") AND "
            r7.append(r1)
            java.lang.String r1 = "msg_id"
            r7.append(r1)
            java.lang.String r1 = " = "
            r7.append(r1)
            r7.append(r0)
            java.lang.String r1 = r7.toString()
        L_0x0098:
            r7 = r20
            com.sec.internal.ims.servicemodules.im.ImCache r0 = r7.mCache
            r8 = r22
            r9 = r24
            android.database.Cursor r1 = r0.queryChatMessagesForTapi(r8, r1, r9, r2)
            r2 = 0
            if (r1 != 0) goto L_0x00b2
            java.lang.String r0 = "buildMessageCursor: Message not found."
            android.util.Log.e(r5, r0)     // Catch:{ all -> 0x025d }
            if (r1 == 0) goto L_0x00b1
            r1.close()
        L_0x00b1:
            return r2
        L_0x00b2:
            java.lang.String[] r5 = r1.getColumnNames()     // Catch:{ all -> 0x025d }
            android.database.MatrixCursor r8 = new android.database.MatrixCursor     // Catch:{ all -> 0x025d }
            r8.<init>(r5)     // Catch:{ all -> 0x025d }
        L_0x00bb:
            boolean r0 = r1.moveToNext()     // Catch:{ all -> 0x025d }
            if (r0 == 0) goto L_0x024c
            android.database.MatrixCursor$RowBuilder r9 = r8.newRow()     // Catch:{ all -> 0x025d }
            int r10 = r5.length     // Catch:{ all -> 0x025d }
            r13 = r2
            r12 = 0
        L_0x00c8:
            if (r12 >= r10) goto L_0x00bb
            r0 = r5[r12]     // Catch:{ all -> 0x025d }
            int r14 = r1.getColumnIndex(r0)     // Catch:{ all -> 0x025d }
            java.lang.String r15 = "status"
            boolean r15 = r15.equals(r0)     // Catch:{ all -> 0x025d }
            if (r15 == 0) goto L_0x011d
            com.gsma.services.rcs.chat.ChatLog$Message$Content$Status r0 = com.gsma.services.rcs.chat.ChatLog.Message.Content.Status.DISPLAY_REPORT_REQUESTED     // Catch:{ all -> 0x025d }
            com.sec.internal.constants.ims.servicemodules.im.ImConstants$Status[] r0 = com.sec.internal.constants.ims.servicemodules.im.ImConstants.Status.values()     // Catch:{ all -> 0x025d }
            int r14 = r1.getInt(r14)     // Catch:{ all -> 0x025d }
            r0 = r0[r14]     // Catch:{ all -> 0x025d }
            com.gsma.services.rcs.chat.ChatLog$Message$Content$Status r0 = com.sec.internal.ims.servicemodules.tapi.service.api.ChatServiceImpl.translateStatus(r0)     // Catch:{ all -> 0x025d }
            java.lang.String r14 = "timestamp_displayed"
            int r14 = r1.getColumnIndex(r14)     // Catch:{ all -> 0x025d }
            java.lang.String r15 = "timestamp_delivered"
            int r15 = r1.getColumnIndex(r15)     // Catch:{ all -> 0x025d }
            com.gsma.services.rcs.chat.ChatLog$Message$Content$Status r11 = com.gsma.services.rcs.chat.ChatLog.Message.Content.Status.SENT     // Catch:{ all -> 0x025d }
            if (r0 != r11) goto L_0x0104
            int r11 = r1.getInt(r14)     // Catch:{ all -> 0x025d }
            if (r11 <= 0) goto L_0x0104
            com.gsma.services.rcs.chat.ChatLog$Message$Content$Status r0 = com.gsma.services.rcs.chat.ChatLog.Message.Content.Status.DISPLAYED     // Catch:{ all -> 0x025d }
            goto L_0x0110
        L_0x0104:
            com.gsma.services.rcs.chat.ChatLog$Message$Content$Status r11 = com.gsma.services.rcs.chat.ChatLog.Message.Content.Status.SENT     // Catch:{ all -> 0x025d }
            if (r0 != r11) goto L_0x0110
            int r11 = r1.getInt(r15)     // Catch:{ all -> 0x025d }
            if (r11 <= 0) goto L_0x0110
            com.gsma.services.rcs.chat.ChatLog$Message$Content$Status r0 = com.gsma.services.rcs.chat.ChatLog.Message.Content.Status.DELIVERED     // Catch:{ all -> 0x025d }
        L_0x0110:
            int r0 = r0.ordinal()     // Catch:{ all -> 0x025d }
            java.lang.Integer r0 = java.lang.Integer.valueOf(r0)     // Catch:{ all -> 0x025d }
            r9.add(r0)     // Catch:{ all -> 0x025d }
            goto L_0x0248
        L_0x011d:
            java.lang.String r11 = "reason_code"
            boolean r11 = r11.equals(r0)     // Catch:{ all -> 0x025d }
            if (r11 == 0) goto L_0x0135
            com.gsma.services.rcs.chat.ChatLog$Message$Content$ReasonCode r0 = com.gsma.services.rcs.chat.ChatLog.Message.Content.ReasonCode.UNSPECIFIED     // Catch:{ all -> 0x025d }
            int r0 = r0.ordinal()     // Catch:{ all -> 0x025d }
            java.lang.Integer r0 = java.lang.Integer.valueOf(r0)     // Catch:{ all -> 0x025d }
            r9.add(r0)     // Catch:{ all -> 0x025d }
            goto L_0x0248
        L_0x0135:
            java.lang.String r11 = "read_status"
            boolean r11 = r11.equals(r0)     // Catch:{ all -> 0x025d }
            if (r11 == 0) goto L_0x016a
            com.sec.internal.constants.ims.servicemodules.im.ImConstants$Status[] r0 = com.sec.internal.constants.ims.servicemodules.im.ImConstants.Status.values()     // Catch:{ all -> 0x025d }
            int r11 = r1.getInt(r14)     // Catch:{ all -> 0x025d }
            r0 = r0[r11]     // Catch:{ all -> 0x025d }
            com.sec.internal.constants.ims.servicemodules.im.ImConstants$Status r11 = com.sec.internal.constants.ims.servicemodules.im.ImConstants.Status.READ     // Catch:{ all -> 0x025d }
            if (r11 != r0) goto L_0x015b
            com.gsma.services.rcs.RcsService$ReadStatus r0 = com.gsma.services.rcs.RcsService.ReadStatus.READ     // Catch:{ all -> 0x025d }
            int r0 = r0.ordinal()     // Catch:{ all -> 0x025d }
            java.lang.Integer r0 = java.lang.Integer.valueOf(r0)     // Catch:{ all -> 0x025d }
            r9.add(r0)     // Catch:{ all -> 0x025d }
            goto L_0x0248
        L_0x015b:
            com.gsma.services.rcs.RcsService$ReadStatus r0 = com.gsma.services.rcs.RcsService.ReadStatus.UNREAD     // Catch:{ all -> 0x025d }
            int r0 = r0.ordinal()     // Catch:{ all -> 0x025d }
            java.lang.Integer r0 = java.lang.Integer.valueOf(r0)     // Catch:{ all -> 0x025d }
            r9.add(r0)     // Catch:{ all -> 0x025d }
            goto L_0x0248
        L_0x016a:
            java.lang.String r11 = "mime_type"
            boolean r11 = r11.equals(r0)     // Catch:{ all -> 0x025d }
            if (r11 == 0) goto L_0x01a2
            java.lang.String r13 = r1.getString(r14)     // Catch:{ all -> 0x025d }
            if (r13 == 0) goto L_0x0183
            boolean r0 = r13.contains(r6)     // Catch:{ all -> 0x025d }
            if (r0 == 0) goto L_0x0183
            r9.add(r6)     // Catch:{ all -> 0x025d }
            goto L_0x0248
        L_0x0183:
            if (r13 == 0) goto L_0x0190
            boolean r0 = r13.contains(r4)     // Catch:{ all -> 0x025d }
            if (r0 == 0) goto L_0x0190
            r9.add(r4)     // Catch:{ all -> 0x025d }
            goto L_0x0248
        L_0x0190:
            if (r13 == 0) goto L_0x019d
            boolean r0 = r13.contains(r3)     // Catch:{ all -> 0x025d }
            if (r0 == 0) goto L_0x019d
            r9.add(r3)     // Catch:{ all -> 0x025d }
            goto L_0x0248
        L_0x019d:
            r9.add(r13)     // Catch:{ all -> 0x025d }
            goto L_0x0248
        L_0x01a2:
            java.lang.String r11 = "expired_delivery"
            boolean r11 = r11.equals(r0)     // Catch:{ all -> 0x025d }
            r15 = 1
            if (r11 == 0) goto L_0x01c0
            long r16 = r1.getLong(r14)     // Catch:{ all -> 0x025d }
            r18 = 0
            int r0 = (r16 > r18 ? 1 : (r16 == r18 ? 0 : -1))
            if (r0 <= 0) goto L_0x01b6
            goto L_0x01b7
        L_0x01b6:
            r15 = 0
        L_0x01b7:
            java.lang.Integer r0 = java.lang.Integer.valueOf(r15)     // Catch:{ all -> 0x025d }
            r9.add(r0)     // Catch:{ all -> 0x025d }
            goto L_0x0248
        L_0x01c0:
            java.lang.String r11 = "content"
            boolean r0 = r11.equals(r0)     // Catch:{ all -> 0x025d }
            if (r0 == 0) goto L_0x0209
            if (r13 == 0) goto L_0x0209
            java.lang.String r0 = "rcspushlocation"
            boolean r0 = r13.contains(r0)     // Catch:{ all -> 0x025d }
            if (r0 == 0) goto L_0x0209
            java.lang.String r11 = r1.getString(r14)     // Catch:{ all -> 0x025d }
            com.sec.internal.ims.servicemodules.gls.GlsXmlParser r0 = new com.sec.internal.ims.servicemodules.gls.GlsXmlParser     // Catch:{ Exception -> 0x01e1 }
            r0.<init>()     // Catch:{ Exception -> 0x01e1 }
            java.lang.String r11 = r0.getGeolocString(r11)     // Catch:{ Exception -> 0x01e1 }
            goto L_0x0204
        L_0x01e1:
            r0 = move-exception
            java.lang.String r14 = LOG_TAG     // Catch:{ all -> 0x025d }
            java.lang.StringBuilder r15 = new java.lang.StringBuilder     // Catch:{ all -> 0x025d }
            r15.<init>()     // Catch:{ all -> 0x025d }
            java.lang.String r2 = "parse error: "
            r15.append(r2)     // Catch:{ all -> 0x025d }
            java.lang.String r0 = r0.getMessage()     // Catch:{ all -> 0x025d }
            r15.append(r0)     // Catch:{ all -> 0x025d }
            java.lang.String r0 = ", Geo location body : "
            r15.append(r0)     // Catch:{ all -> 0x025d }
            r15.append(r11)     // Catch:{ all -> 0x025d }
            java.lang.String r0 = r15.toString()     // Catch:{ all -> 0x025d }
            android.util.Log.e(r14, r0)     // Catch:{ all -> 0x025d }
        L_0x0204:
            r9.add(r11)     // Catch:{ all -> 0x025d }
            r2 = 0
            goto L_0x0248
        L_0x0209:
            int r0 = r1.getType(r14)     // Catch:{ all -> 0x025d }
            if (r0 == r15) goto L_0x023c
            r2 = 2
            if (r0 == r2) goto L_0x022f
            r2 = 3
            if (r0 == r2) goto L_0x0226
            r2 = 4
            if (r0 == r2) goto L_0x021d
            r2 = 0
            r9.add(r2)     // Catch:{ all -> 0x025d }
            goto L_0x0248
        L_0x021d:
            r2 = 0
            byte[] r0 = r1.getBlob(r14)     // Catch:{ all -> 0x025d }
            r9.add(r0)     // Catch:{ all -> 0x025d }
            goto L_0x0248
        L_0x0226:
            r2 = 0
            java.lang.String r0 = r1.getString(r14)     // Catch:{ all -> 0x025d }
            r9.add(r0)     // Catch:{ all -> 0x025d }
            goto L_0x0248
        L_0x022f:
            r2 = 0
            float r0 = r1.getFloat(r14)     // Catch:{ all -> 0x025d }
            java.lang.Float r0 = java.lang.Float.valueOf(r0)     // Catch:{ all -> 0x025d }
            r9.add(r0)     // Catch:{ all -> 0x025d }
            goto L_0x0248
        L_0x023c:
            r2 = 0
            long r14 = r1.getLong(r14)     // Catch:{ all -> 0x025d }
            java.lang.Long r0 = java.lang.Long.valueOf(r14)     // Catch:{ all -> 0x025d }
            r9.add(r0)     // Catch:{ all -> 0x025d }
        L_0x0248:
            int r12 = r12 + 1
            goto L_0x00c8
        L_0x024c:
            android.content.Context r0 = r20.getContext()     // Catch:{ all -> 0x025d }
            android.content.ContentResolver r0 = r0.getContentResolver()     // Catch:{ all -> 0x025d }
            android.net.Uri r2 = com.gsma.services.rcs.chat.ChatLog.Message.CONTENT_URI     // Catch:{ all -> 0x025d }
            r8.setNotificationUri(r0, r2)     // Catch:{ all -> 0x025d }
            r1.close()
            return r8
        L_0x025d:
            r0 = move-exception
            r2 = r0
            if (r1 == 0) goto L_0x026a
            r1.close()     // Catch:{ all -> 0x0265 }
            goto L_0x026a
        L_0x0265:
            r0 = move-exception
            r1 = r0
            r2.addSuppressed(r1)
        L_0x026a:
            throw r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.tapi.service.provider.ChatProvider.fillMessageCursor(java.lang.String, java.lang.String[], java.lang.String, java.lang.String[], java.lang.String):android.database.MatrixCursor");
    }
}
