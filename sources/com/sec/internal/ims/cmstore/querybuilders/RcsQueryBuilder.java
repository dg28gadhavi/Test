package com.sec.internal.ims.cmstore.querybuilders;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BlockedNumberContract;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.MIMEContentType;
import com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.cmstore.McsConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImContract;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.constants.ims.servicemodules.im.ImParticipant;
import com.sec.internal.constants.ims.servicemodules.im.NotificationStatus;
import com.sec.internal.helper.httpclient.HttpPostBody;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.adapters.CloudMessageRCSStorageAdapter;
import com.sec.internal.ims.cmstore.adapters.CloudMessageTelephonyStorageAdapter;
import com.sec.internal.ims.cmstore.helper.ATTGlobalVariables;
import com.sec.internal.ims.cmstore.params.ParamOMAObject;
import com.sec.internal.ims.cmstore.utils.CursorContentValueTranslator;
import com.sec.internal.ims.cmstore.utils.Util;
import com.sec.internal.ims.util.PhoneUtils;
import com.sec.internal.ims.util.StringIdGenerator;
import com.sec.internal.interfaces.ims.cmstore.IBufferDBEventListener;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.nms.data.ImdnInfo;
import com.sec.internal.omanetapi.nms.data.ImdnList;
import com.sec.internal.omanetapi.nms.data.ImdnObject;
import com.sec.internal.omanetapi.nms.data.PayloadPartInfo;
import com.sec.sve.generalevent.VcidEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class RcsQueryBuilder extends QueryBuilderBase {
    private String TAG = RcsQueryBuilder.class.getSimpleName();
    protected String mCountryCode;
    private final CloudMessageRCSStorageAdapter mRCSStorage;
    private final CloudMessageTelephonyStorageAdapter mTelephonyStorage;

    public RcsQueryBuilder(MessageStoreClient messageStoreClient, IBufferDBEventListener iBufferDBEventListener) {
        super(messageStoreClient, iBufferDBEventListener);
        this.TAG += "[" + messageStoreClient.getClientID() + "]";
        this.mRCSStorage = new CloudMessageRCSStorageAdapter(messageStoreClient.getContext());
        this.mTelephonyStorage = new CloudMessageTelephonyStorageAdapter(messageStoreClient.getContext());
        this.mCountryCode = Util.getSimCountryCode(this.mContext, messageStoreClient.getClientID());
    }

    public Cursor searchIMFTBufferUsingImdn(String str, String str2) {
        String str3 = this.TAG;
        IMSLog.s(str3, "searchIMFTBufferUsingImdn: " + IMSLog.checker(str) + " line:" + IMSLog.checker(str2));
        if (this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isMultiLineSupported()) {
            return this.mBufferDB.queryRCSMessages((String[]) null, "imdn_message_id=? AND linenum=?", new String[]{str, str2}, (String) null);
        }
        return this.mBufferDB.queryRCSMessages((String[]) null, "imdn_message_id=?", new String[]{str}, (String) null);
    }

    public Cursor searchBufferNotificationUsingImdn(String str) {
        String str2 = this.TAG;
        Log.d(str2, "searchBufferNotificationUsingImdn: " + IMSLog.checker(str));
        return this.mBufferDB.queryRCSImdnUseImdnId(str);
    }

    public Cursor searchBufferNotificationUsingImdnAndTelUri(String str, String str2) {
        String str3 = this.TAG;
        Log.d(str3, "searchBufferNotificationUsingImdnAndTelUri: " + IMSLog.checker(str) + ", telUri=" + IMSLog.checker(str2));
        return this.mBufferDB.queryRCSImdnUseImdnIdAndTelUri(str, str2);
    }

    public Cursor searchIMFTBufferUsingRowId(String str) {
        return this.mBufferDB.queryRCSMessages((String[]) null, "_id=?", new String[]{str}, (String) null);
    }

    public Cursor searchIMFTBufferUsingChatId(String str) {
        return this.mBufferDB.queryRCSMessages((String[]) null, "chat_id=?", new String[]{str}, (String) null);
    }

    public Cursor queryRCSMessagesToUploadByMessageType(String str) {
        Log.d(this.TAG, "queryRCSMessagesToUploadByMessageType()");
        return this.mBufferDB.queryRCSMessages((String[]) null, "syncdirection=? AND (res_url IS NULL OR res_url = '') AND inserted_timestamp > ? AND sim_imsi=?" + (" AND (message_type = " + McsConstants.RCSMessageType.MULTIMEDIA.getId() + " OR " + "message_type" + " = " + McsConstants.RCSMessageType.TEXT.getId() + " OR " + "message_type" + " = " + McsConstants.RCSMessageType.LOCATION.getId() + " OR " + "message_type" + " = " + McsConstants.RCSMessageType.SINGLE.getId() + " OR " + "message_type" + " = " + McsConstants.RCSMessageType.GROUP.getId() + ")") + " AND " + ImContract.ChatItem.IS_FILE_TRANSFER + "=? AND " + "chat_id" + "=?", new String[]{String.valueOf(CloudMessageBufferDBConstants.DirectionFlag.ToSendCloud.getId()), String.valueOf(System.currentTimeMillis() - TimeUnit.HOURS.toMillis((long) this.mHoursToUploadMessageInitSync)), this.IMSI, "0", str}, (String) null);
    }

    public Cursor queryRCSMessagesToUpload() {
        Log.d(this.TAG, "queryRCSMessagesToUpload()");
        return this.mBufferDB.queryRCSMessages((String[]) null, "syncdirection=? AND (res_url IS NULL OR res_url = '') AND inserted_timestamp > ? AND sim_imsi=?", new String[]{String.valueOf(CloudMessageBufferDBConstants.DirectionFlag.ToSendCloud.getId()), String.valueOf(System.currentTimeMillis() - TimeUnit.HOURS.toMillis((long) this.mHoursToUploadMessageInitSync)), this.IMSI}, (String) null);
    }

    public Cursor queryRCSFtMessagesToUpload(String str) {
        Log.d(this.TAG, "queryRCSFtMessagesToUpload()");
        return this.mBufferDB.queryRCSMessages((String[]) null, "syncdirection=? AND (res_url IS NULL OR res_url = '') AND inserted_timestamp > ? AND sim_imsi=? AND is_filetransfer=? AND chat_id=?", new String[]{String.valueOf(CloudMessageBufferDBConstants.DirectionFlag.ToSendCloud.getId()), String.valueOf(System.currentTimeMillis() - TimeUnit.HOURS.toMillis((long) this.mHoursToUploadMessageInitSync)), this.IMSI, "1", str}, (String) null);
    }

    public Cursor queryRCSMessagesByChatId(String str, String str2) {
        return this.mBufferDB.queryRCSMessages((String[]) null, "chat_id=?", new String[]{str}, str2);
    }

    public Cursor queryImdnMessagesToUpload() {
        Log.d(this.TAG, "queryImdnMessagesToUpload()");
        return this.mBufferDB.queryRCSImdnMessages((String[]) null, "syncdirection=? AND (res_url IS NULL OR res_url = '') AND timestamp > ?", new String[]{String.valueOf(CloudMessageBufferDBConstants.DirectionFlag.ToSendCloud.getId()), String.valueOf(System.currentTimeMillis() - TimeUnit.HOURS.toMillis((long) this.mHoursToUploadMessageInitSync))}, (String) null);
    }

    public long insertToRCSMessagesBufferDB(Cursor cursor, String str, ContentValues contentValues) {
        String str2 = this.TAG;
        IMSLog.s(str2, "insertToRCSMessagesBufferDB(): " + IMSLog.checker(str) + "we do get something from RCS messages: " + cursor.getCount());
        ArrayList<ContentValues> convertRCSimfttoCV = CursorContentValueTranslator.convertRCSimfttoCV(cursor, this.mContext, this.mStoreClient.getClientID());
        String str3 = this.TAG;
        Log.d(str3, "insertToRCSMessagesBufferDB() size: " + convertRCSimfttoCV.size());
        long j = 0;
        for (int i = 0; i < convertRCSimfttoCV.size(); i++) {
            ContentValues contentValues2 = convertRCSimfttoCV.get(i);
            String extractNumberFromUri = PhoneUtils.extractNumberFromUri(contentValues2.getAsString("remote_uri"));
            if (!this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isNeedCheckBlockedNumberBeforeCopyRcsDb() || TextUtils.isEmpty(extractNumberFromUri) || !BlockedNumberContract.isBlocked(this.mContext, extractNumberFromUri)) {
                contentValues2.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, contentValues.getAsInteger(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION));
                contentValues2.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, contentValues.getAsInteger(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION));
                contentValues2.put("linenum", str);
                if (contentValues2.getAsString("sim_imsi") == null) {
                    contentValues2.put("sim_imsi", this.IMSI);
                }
                j = this.mBufferDB.insertDeviceMsgToBuffer(1, contentValues2);
                Cursor queryImdnUsingImdnId = queryImdnUsingImdnId(contentValues2.getAsString("imdn_message_id"));
                if (queryImdnUsingImdnId != null) {
                    try {
                        if (queryImdnUsingImdnId.moveToFirst()) {
                            insertToImdnNotificationBufferDB(queryImdnUsingImdnId, contentValues);
                        }
                    } catch (Throwable th) {
                        th.addSuppressed(th);
                    }
                }
                if (queryImdnUsingImdnId != null) {
                    queryImdnUsingImdnId.close();
                }
            } else {
                String str4 = this.TAG;
                Log.i(str4, "The number [" + IMSLog.checker(extractNumberFromUri) + "] has been add to block list. This message should avoid to save to BuffedDB!");
            }
        }
        convertRCSimfttoCV.size();
        return j;
        throw th;
    }

    public long insertToImdnNotificationBufferDB(Cursor cursor, ContentValues contentValues) {
        ArrayList<ContentValues> convertImdnNotificationtoCV = CursorContentValueTranslator.convertImdnNotificationtoCV(cursor);
        long j = 0;
        if (convertImdnNotificationtoCV == null) {
            return 0;
        }
        String str = this.TAG;
        Log.d(str, "insertToImdnNotificationBufferDB size: " + convertImdnNotificationtoCV.size());
        for (int i = 0; i < convertImdnNotificationtoCV.size(); i++) {
            ContentValues contentValues2 = convertImdnNotificationtoCV.get(i);
            contentValues2.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, contentValues.getAsInteger(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION));
            contentValues2.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, contentValues.getAsInteger(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION));
            contentValues.put("sim_imsi", this.IMSI);
            j = this.mBufferDB.insertDeviceMsgToBuffer(13, contentValues2);
        }
        String str2 = this.TAG;
        Log.d(str2, "insertToImdnNotificationBufferDB row: " + j);
        convertImdnNotificationtoCV.size();
        return j;
    }

    public void insertToRCSParticipantsBufferDB(Cursor cursor) {
        ArrayList<ContentValues> convertRCSparticipantstoCV = CursorContentValueTranslator.convertRCSparticipantstoCV(cursor);
        if (convertRCSparticipantstoCV != null) {
            String str = this.TAG;
            Log.d(str, "insertToRCSParticipantsBufferDB size: " + convertRCSparticipantstoCV.size());
            for (int i = 0; i < convertRCSparticipantstoCV.size(); i++) {
                ContentValues contentValues = convertRCSparticipantstoCV.get(i);
                String asString = contentValues.getAsString("chat_id");
                String asString2 = contentValues.getAsString("uri");
                Cursor queryParticipantFromBufferDb = queryParticipantFromBufferDb(asString, asString2);
                if (queryParticipantFromBufferDb != null) {
                    try {
                        if (queryParticipantFromBufferDb.moveToFirst()) {
                            String str2 = this.TAG;
                            Log.d(str2, " participant " + IMSLog.checker(asString2) + " already exist in buffer db");
                            queryParticipantFromBufferDb.close();
                        }
                    } catch (Throwable th) {
                        th.addSuppressed(th);
                    }
                }
                if (queryParticipantFromBufferDb != null) {
                    queryParticipantFromBufferDb.close();
                }
                contentValues.put("sim_imsi", this.IMSI);
                insertDeviceMsgToBuffer(2, contentValues);
            }
            return;
        }
        return;
        throw th;
    }

    public Cursor querySessionByConversationId(String str) {
        return this.mBufferDB.querySessionByConversationId(str);
    }

    public Cursor queryBufferDBSessionByChatId(String str) {
        return this.mBufferDB.querySessionByChatId(str);
    }

    public Cursor queryAllSession() {
        return this.mRCSStorage.queryAllSessionWithIMSI(this.IMSI);
    }

    public int insertSessionFromBufferDbToRCSDb(ContentValues contentValues, ArrayList<ContentValues> arrayList) {
        return this.mRCSStorage.insertSessionFromBufferDbToRCSDb(contentValues, arrayList);
    }

    public Cursor queryAllSessionWithIMSI(String str) {
        Log.d(this.TAG, "queryAllSession()");
        return this.mRCSStorage.queryAllSessionWithIMSI(str);
    }

    public Cursor queryGroupSession() {
        return this.mBufferDB.queryGroupSession(this.IMSI);
    }

    public Cursor queryOneToOneSession() {
        return this.mBufferDB.queryOneToOneSession(this.IMSI);
    }

    public Cursor queryParticipantsUsingChatId(String str) {
        return this.mRCSStorage.queryParticipantsUsingChatId(str);
    }

    public Cursor queryParticipantsFromBufferDb(String str) {
        return this.mBufferDB.queryParticipant(str);
    }

    public Cursor queryParticipantFromBufferDb(String str, String str2) {
        return this.mBufferDB.queryParticipant(str, str2);
    }

    public int deleteParticipantsUsingRowId(long j) {
        return this.mRCSStorage.deleteParticipantsUsingRowId(j);
    }

    public int deleteParticipantsFromBufferDb(String str, String str2) {
        return this.mBufferDB.deleteTable(2, "uri=? AND chat_id=?", new String[]{str, str2});
    }

    public int updateRCSParticipantsDb(long j, ContentValues contentValues) {
        if (contentValues.size() > 0) {
            return this.mRCSStorage.updateParticipantsFromBufferDbToRCSDb(j, contentValues);
        }
        return 0;
    }

    public int updateParticipantsBufferDb(String str, ContentValues contentValues) {
        String[] strArr = {str};
        if (contentValues.size() > 0) {
            return this.mBufferDB.updateRCSParticipantsTable(contentValues, "uri=?", strArr);
        }
        return 0;
    }

    public Uri insertRCSParticipantsDb(ContentValues contentValues) {
        if (contentValues.size() > 0) {
            return this.mRCSStorage.insertParticipantsFromBufferDbToRCSDb(contentValues);
        }
        return null;
    }

    public void insertRCSParticipantsDb(ArrayList<ContentValues> arrayList) {
        Uri insertParticipantsFromBufferDbToRCSDb;
        Iterator<ContentValues> it = arrayList.iterator();
        while (it.hasNext()) {
            ContentValues next = it.next();
            if (next.size() > 0 && (insertParticipantsFromBufferDbToRCSDb = this.mRCSStorage.insertParticipantsFromBufferDbToRCSDb(next)) != null) {
                String str = this.TAG;
                Log.d(str, "insert RCS participant into ImProvider result: " + IMSLog.checker(insertParticipantsFromBufferDbToRCSDb.toString()));
                int intValue = Integer.valueOf(insertParticipantsFromBufferDbToRCSDb.getLastPathSegment()).intValue();
                if (intValue > 0) {
                    String asString = next.getAsString("chat_id");
                    String asString2 = next.getAsString("uri");
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("_id", Integer.valueOf(intValue));
                    this.mBufferDB.updateRCSParticipantsTable(contentValues, "chat_id=? AND uri=?", new String[]{String.valueOf(asString), asString2});
                }
            }
        }
    }

    public Cursor queryIMFTUsingChatId(String str) {
        return this.mRCSStorage.queryIMFTUsingChatId(str);
    }

    public Cursor queryImdnUsingImdnId(String str) {
        return this.mRCSStorage.queryNotificationUsingImdn(str);
    }

    public Cursor queryIMFTUsingRowId(long j) {
        return this.mRCSStorage.queryIMFTUsingRowId(j);
    }

    public Cursor queryRcsDBMessageUsingImdnId(String str) {
        return this.mRCSStorage.queryRcsDBMessageUsingImdnId(str);
    }

    public void insertAllToRCSSessionBufferDB(Cursor cursor) {
        ArrayList<ContentValues> convertRCSSessiontoCV = CursorContentValueTranslator.convertRCSSessiontoCV(cursor);
        String str = this.TAG;
        Log.d(str, "insertAllToRCSSessionBufferDB size: " + convertRCSSessiontoCV.size());
        String userTelCtn = this.mStoreClient.getPrerenceManager().getUserTelCtn();
        for (int i = 0; i < convertRCSSessiontoCV.size(); i++) {
            ContentValues contentValues = convertRCSSessiontoCV.get(i);
            String asString = contentValues.getAsString(ImContract.ImSession.PREFERRED_URI);
            if (asString == null) {
                asString = this.mStoreClient.getPrerenceManager().getUserTelCtn();
            }
            String asString2 = contentValues.getAsString("chat_id");
            contentValues.put("linenum", asString);
            ImsUri normalizedTelUri = Util.getNormalizedTelUri(contentValues.getAsString(ImContract.ImSession.OWN_PHONE_NUMBER), this.mCountryCode);
            if (normalizedTelUri != null && !TextUtils.equals(normalizedTelUri.toString(), userTelCtn)) {
                String generateConversationId = StringIdGenerator.generateConversationId();
                contentValues.put("conversation_id", generateConversationId);
                String str2 = this.TAG;
                Log.d(str2, "new conv id====" + generateConversationId);
            }
            CloudMessageBufferDBConstants.ActionStatusFlag actionStatusFlag = CloudMessageBufferDBConstants.ActionStatusFlag.Insert;
            contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(actionStatusFlag.getId()));
            CloudMessageBufferDBConstants.DirectionFlag directionFlag = CloudMessageBufferDBConstants.DirectionFlag.ToSendCloud;
            contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(directionFlag.getId()));
            insertDeviceMsgToBuffer(10, contentValues);
            Cursor queryParticipantsUsingChatId = queryParticipantsUsingChatId(asString2);
            if (queryParticipantsUsingChatId != null) {
                try {
                    if (queryParticipantsUsingChatId.moveToFirst()) {
                        insertToRCSParticipantsBufferDB(queryParticipantsUsingChatId);
                    }
                } catch (Throwable th) {
                    th.addSuppressed(th);
                }
            }
            if (queryParticipantsUsingChatId != null) {
                queryParticipantsUsingChatId.close();
            }
            ContentValues contentValues2 = new ContentValues();
            contentValues2.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(actionStatusFlag.getId()));
            contentValues2.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(directionFlag.getId()));
            Cursor queryIMFTUsingChatId = queryIMFTUsingChatId(asString2);
            if (queryIMFTUsingChatId != null) {
                try {
                    if (queryIMFTUsingChatId.moveToFirst()) {
                        insertToRCSMessagesBufferDB(queryIMFTUsingChatId, asString, contentValues2);
                    }
                } catch (Throwable th2) {
                    th.addSuppressed(th2);
                }
            }
            if (queryIMFTUsingChatId != null) {
                queryIMFTUsingChatId.close();
            }
        }
        return;
        throw th;
        throw th;
    }

    public void insertSingleSessionToRcsBuffer(Cursor cursor) {
        ArrayList<ContentValues> convertRCSSessiontoCV = CursorContentValueTranslator.convertRCSSessiontoCV(cursor);
        for (int i = 0; i < convertRCSSessiontoCV.size(); i++) {
            ContentValues contentValues = convertRCSSessiontoCV.get(i);
            String asString = contentValues.getAsString(ImContract.ImSession.PREFERRED_URI);
            if (asString == null) {
                asString = this.mStoreClient.getPrerenceManager().getUserTelCtn();
            }
            contentValues.put("linenum", asString);
            contentValues.put("sim_imsi", this.IMSI);
            insertDeviceMsgToBuffer(10, contentValues);
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v7, resolved type: java.lang.String} */
    /* JADX WARNING: type inference failed for: r2v0 */
    /* JADX WARNING: type inference failed for: r2v2, types: [android.database.Cursor] */
    /* JADX WARNING: type inference failed for: r2v4 */
    /* JADX WARNING: type inference failed for: r2v5 */
    /* JADX WARNING: type inference failed for: r2v10, types: [java.lang.String] */
    /* JADX WARNING: type inference failed for: r2v12 */
    /* JADX WARNING: type inference failed for: r2v13 */
    /* JADX WARNING: type inference failed for: r2v14 */
    /* JADX WARNING: type inference failed for: r2v15 */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x0027, code lost:
        r1 = r14.mLine;
     */
    /* JADX WARNING: Failed to insert additional move for type inference */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Removed duplicated region for block: B:146:0x03fd  */
    /* JADX WARNING: Removed duplicated region for block: B:160:0x042f  */
    /* JADX WARNING: Removed duplicated region for block: B:170:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x0191 A[Catch:{ all -> 0x0242, all -> 0x0249, all -> 0x0255 }] */
    /* JADX WARNING: Removed duplicated region for block: B:69:0x01a4 A[SYNTHETIC, Splitter:B:69:0x01a4] */
    /* JADX WARNING: Removed duplicated region for block: B:82:0x021a A[Catch:{ all -> 0x0242, all -> 0x0249, all -> 0x0255 }] */
    /* JADX WARNING: Removed duplicated region for block: B:84:0x023e A[SYNTHETIC, Splitter:B:84:0x023e] */
    /* JADX WARNING: Removed duplicated region for block: B:95:0x0250 A[SYNTHETIC, Splitter:B:95:0x0250] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public java.lang.String searchOrCreateSession(com.sec.internal.ims.cmstore.params.ParamOMAObject r14) {
        /*
            r13 = this;
            java.lang.String r0 = "participant = "
            java.util.Set<com.sec.ims.util.ImsUri> r1 = r14.mNomalizedOtherParticipants
            r2 = 0
            if (r1 == 0) goto L_0x0433
            boolean r1 = r1.isEmpty()
            if (r1 != 0) goto L_0x0433
            java.lang.String r1 = r14.CONVERSATION_ID
            boolean r1 = android.text.TextUtils.isEmpty(r1)
            if (r1 == 0) goto L_0x0017
            goto L_0x0433
        L_0x0017:
            com.sec.internal.ims.cmstore.MessageStoreClient r1 = r13.mStoreClient
            com.sec.internal.ims.cmstore.strategy.CloudMessageStrategyManager r1 = r1.getCloudMessageStrategyManager()
            com.sec.internal.interfaces.ims.cmstore.ICloudMessageStrategy r1 = r1.getStrategy()
            boolean r1 = r1.isMultiLineSupported()
            if (r1 == 0) goto L_0x0042
            java.lang.String r1 = r14.mLine
            if (r1 == 0) goto L_0x0042
            com.sec.internal.ims.cmstore.MessageStoreClient r3 = r13.mStoreClient
            com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager r3 = r3.getPrerenceManager()
            java.lang.String r3 = r3.getUserTelCtn()
            boolean r1 = r1.equalsIgnoreCase(r3)
            if (r1 != 0) goto L_0x0042
            java.lang.String r1 = r14.mLine
            com.sec.ims.util.ImsUri r1 = com.sec.ims.util.ImsUri.parse(r1)
            goto L_0x0043
        L_0x0042:
            r1 = r2
        L_0x0043:
            java.lang.String r3 = r14.P_ASSERTED_SERVICE     // Catch:{ NullPointerException -> 0x040a }
            r4 = 0
            r5 = 1
            if (r3 == 0) goto L_0x0053
            java.lang.String r6 = "group"
            boolean r3 = r3.contains(r6)     // Catch:{ NullPointerException -> 0x040a }
            if (r3 == 0) goto L_0x0053
            r3 = r5
            goto L_0x0054
        L_0x0053:
            r3 = r4
        L_0x0054:
            java.util.Set<com.sec.ims.util.ImsUri> r6 = r14.mNomalizedOtherParticipants     // Catch:{ NullPointerException -> 0x040a }
            int r6 = r6.size()     // Catch:{ NullPointerException -> 0x040a }
            if (r6 != r5) goto L_0x0076
            if (r3 == 0) goto L_0x006b
            boolean r6 = r13.isCmsEnabled     // Catch:{ NullPointerException -> 0x040a }
            if (r6 == 0) goto L_0x006b
            com.sec.internal.ims.cmstore.adapters.CloudMessageBufferDBPersister r1 = r13.mBufferDB     // Catch:{ NullPointerException -> 0x040a }
            java.lang.String r6 = r14.CONVERSATION_ID     // Catch:{ NullPointerException -> 0x040a }
            android.database.Cursor r1 = r1.querySessionByConversationId(r6)     // Catch:{ NullPointerException -> 0x040a }
            goto L_0x008e
        L_0x006b:
            com.sec.internal.ims.cmstore.adapters.CloudMessageBufferDBPersister r6 = r13.mBufferDB     // Catch:{ NullPointerException -> 0x040a }
            java.util.Set<com.sec.ims.util.ImsUri> r7 = r14.mNomalizedOtherParticipants     // Catch:{ NullPointerException -> 0x040a }
            com.sec.internal.constants.ims.servicemodules.im.ChatData$ChatType r8 = com.sec.internal.constants.ims.servicemodules.im.ChatData.ChatType.ONE_TO_ONE_CHAT     // Catch:{ NullPointerException -> 0x040a }
            android.database.Cursor r1 = r6.querySessionByParticipants(r7, r8, r1)     // Catch:{ NullPointerException -> 0x040a }
            goto L_0x008e
        L_0x0076:
            boolean r6 = r14.IS_OPEN_GROUP     // Catch:{ NullPointerException -> 0x040a }
            if (r6 != 0) goto L_0x0086
            if (r3 == 0) goto L_0x007d
            goto L_0x0086
        L_0x007d:
            com.sec.internal.ims.cmstore.adapters.CloudMessageBufferDBPersister r6 = r13.mBufferDB     // Catch:{ NullPointerException -> 0x040a }
            java.util.Set<com.sec.ims.util.ImsUri> r7 = r14.mNomalizedOtherParticipants     // Catch:{ NullPointerException -> 0x040a }
            android.database.Cursor r1 = r6.querySessionByParticipants(r7, r2, r1)     // Catch:{ NullPointerException -> 0x040a }
            goto L_0x008e
        L_0x0086:
            com.sec.internal.ims.cmstore.adapters.CloudMessageBufferDBPersister r1 = r13.mBufferDB     // Catch:{ NullPointerException -> 0x040a }
            java.lang.String r6 = r14.CONVERSATION_ID     // Catch:{ NullPointerException -> 0x040a }
            android.database.Cursor r1 = r1.querySessionByConversationId(r6)     // Catch:{ NullPointerException -> 0x040a }
        L_0x008e:
            java.lang.String r6 = "chat_id"
            java.lang.String r7 = "sim_imsi"
            java.lang.String r8 = "subject"
            if (r1 == 0) goto L_0x0261
            boolean r9 = r1.moveToFirst()     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            if (r9 == 0) goto L_0x0261
            int r3 = r1.getColumnIndexOrThrow(r6)     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            java.lang.String r2 = r1.getString(r3)     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            java.lang.String r3 = r13.TAG     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            java.lang.StringBuilder r4 = new java.lang.StringBuilder     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            r4.<init>()     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            java.lang.String r5 = "searchOrCreateSession, chatId found = "
            r4.append(r5)     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            r4.append(r2)     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            java.lang.String r4 = r4.toString()     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            android.util.Log.i(r3, r4)     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            java.lang.String r3 = "inserted_timestamp DESC"
            android.database.Cursor r3 = r13.queryRCSMessagesByChatId(r2, r3)     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            if (r3 == 0) goto L_0x00d6
            boolean r4 = r3.moveToFirst()     // Catch:{ all -> 0x0255 }
            if (r4 == 0) goto L_0x00d6
            java.lang.String r4 = "inserted_timestamp"
            int r4 = r3.getColumnIndexOrThrow(r4)     // Catch:{ all -> 0x0255 }
            long r4 = r3.getLong(r4)     // Catch:{ all -> 0x0255 }
            goto L_0x00d8
        L_0x00d6:
            r4 = 0
        L_0x00d8:
            java.lang.String r6 = r13.TAG     // Catch:{ all -> 0x0255 }
            java.lang.StringBuilder r9 = new java.lang.StringBuilder     // Catch:{ all -> 0x0255 }
            r9.<init>()     // Catch:{ all -> 0x0255 }
            java.lang.String r10 = "getDateFromDateString(objt.DATE)="
            r9.append(r10)     // Catch:{ all -> 0x0255 }
            java.lang.String r10 = r14.DATE     // Catch:{ all -> 0x0255 }
            long r10 = r13.getDateFromDateString(r10)     // Catch:{ all -> 0x0255 }
            r9.append(r10)     // Catch:{ all -> 0x0255 }
            java.lang.String r10 = ", timeStamp="
            r9.append(r10)     // Catch:{ all -> 0x0255 }
            r9.append(r4)     // Catch:{ all -> 0x0255 }
            java.lang.String r10 = "objt.IS_CPM_GROUP = "
            r9.append(r10)     // Catch:{ all -> 0x0255 }
            boolean r10 = r14.IS_CPM_GROUP     // Catch:{ all -> 0x0255 }
            r9.append(r10)     // Catch:{ all -> 0x0255 }
            java.lang.String r9 = r9.toString()     // Catch:{ all -> 0x0255 }
            android.util.Log.d(r6, r9)     // Catch:{ all -> 0x0255 }
            int r6 = r1.getColumnIndexOrThrow(r7)     // Catch:{ all -> 0x0255 }
            java.lang.String r6 = r1.getString(r6)     // Catch:{ all -> 0x0255 }
            com.sec.internal.ims.cmstore.MessageStoreClient r9 = r13.mStoreClient     // Catch:{ all -> 0x0255 }
            java.lang.String r9 = r9.getCurrentIMSI()     // Catch:{ all -> 0x0255 }
            java.lang.String r10 = r13.TAG     // Catch:{ all -> 0x0255 }
            java.lang.StringBuilder r11 = new java.lang.StringBuilder     // Catch:{ all -> 0x0255 }
            r11.<init>()     // Catch:{ all -> 0x0255 }
            r11.append(r6)     // Catch:{ all -> 0x0255 }
            java.lang.String r12 = " | update session sim imsi : "
            r11.append(r12)     // Catch:{ all -> 0x0255 }
            r11.append(r9)     // Catch:{ all -> 0x0255 }
            java.lang.String r11 = r11.toString()     // Catch:{ all -> 0x0255 }
            android.util.Log.d(r10, r11)     // Catch:{ all -> 0x0255 }
            boolean r6 = android.text.TextUtils.isEmpty(r6)     // Catch:{ all -> 0x0255 }
            if (r6 == 0) goto L_0x0147
            boolean r6 = android.text.TextUtils.isEmpty(r9)     // Catch:{ all -> 0x0255 }
            if (r6 != 0) goto L_0x0147
            android.content.ContentValues r6 = new android.content.ContentValues     // Catch:{ all -> 0x0255 }
            r6.<init>()     // Catch:{ all -> 0x0255 }
            r6.put(r7, r9)     // Catch:{ all -> 0x0255 }
            r13.updateSessionBufferDb(r2, r6)     // Catch:{ all -> 0x0255 }
            r13.updateRCSSessionDb(r2, r6)     // Catch:{ all -> 0x0255 }
        L_0x0147:
            boolean r6 = r14.IS_CPM_GROUP     // Catch:{ all -> 0x0255 }
            if (r6 == 0) goto L_0x024e
            com.sec.internal.ims.cmstore.MessageStoreClient r6 = r13.mStoreClient     // Catch:{ all -> 0x0255 }
            com.sec.internal.ims.cmstore.strategy.CloudMessageStrategyManager r6 = r6.getCloudMessageStrategyManager()     // Catch:{ all -> 0x0255 }
            com.sec.internal.interfaces.ims.cmstore.ICloudMessageStrategy r6 = r6.getStrategy()     // Catch:{ all -> 0x0255 }
            boolean r6 = r6.shouldCareGroupChatAttribute()     // Catch:{ all -> 0x0255 }
            if (r6 == 0) goto L_0x024e
            java.lang.String r6 = r14.DATE     // Catch:{ all -> 0x0255 }
            long r6 = r13.getDateFromDateString(r6)     // Catch:{ all -> 0x0255 }
            int r4 = (r6 > r4 ? 1 : (r6 == r4 ? 0 : -1))
            if (r4 <= 0) goto L_0x024e
            int r4 = r1.getColumnIndexOrThrow(r8)     // Catch:{ all -> 0x0255 }
            java.lang.String r4 = r1.getString(r4)     // Catch:{ all -> 0x0255 }
            android.content.ContentValues r5 = new android.content.ContentValues     // Catch:{ all -> 0x0255 }
            r5.<init>()     // Catch:{ all -> 0x0255 }
            if (r4 == 0) goto L_0x018b
            java.lang.String r6 = r14.SUBJECT     // Catch:{ all -> 0x0255 }
            if (r6 == 0) goto L_0x018b
            boolean r4 = r4.equals(r6)     // Catch:{ all -> 0x0255 }
            if (r4 != 0) goto L_0x018b
            java.lang.String r4 = r13.TAG     // Catch:{ all -> 0x0255 }
            java.lang.String r6 = "subject has been changed, update it"
            android.util.Log.d(r4, r6)     // Catch:{ all -> 0x0255 }
            java.lang.String r4 = r14.SUBJECT     // Catch:{ all -> 0x0255 }
            r5.put(r8, r4)     // Catch:{ all -> 0x0255 }
        L_0x018b:
            int r4 = r5.size()     // Catch:{ all -> 0x0255 }
            if (r4 <= 0) goto L_0x0197
            r13.updateSessionBufferDb(r2, r5)     // Catch:{ all -> 0x0255 }
            r13.updateRCSSessionDb(r2, r5)     // Catch:{ all -> 0x0255 }
        L_0x0197:
            java.util.HashSet r4 = new java.util.HashSet     // Catch:{ all -> 0x0255 }
            java.util.Set<com.sec.ims.util.ImsUri> r14 = r14.mNomalizedOtherParticipants     // Catch:{ all -> 0x0255 }
            r4.<init>(r14)     // Catch:{ all -> 0x0255 }
            android.database.Cursor r14 = r13.queryParticipantsUsingChatId(r2)     // Catch:{ all -> 0x0255 }
        L_0x01a2:
            if (r14 == 0) goto L_0x0214
            boolean r5 = r14.moveToNext()     // Catch:{ all -> 0x0242 }
            if (r5 == 0) goto L_0x0214
            java.lang.String r5 = "uri"
            int r5 = r14.getColumnIndexOrThrow(r5)     // Catch:{ all -> 0x0242 }
            java.lang.String r5 = r14.getString(r5)     // Catch:{ all -> 0x0242 }
            java.lang.String r6 = r13.mCountryCode     // Catch:{ all -> 0x0242 }
            com.sec.ims.util.ImsUri r6 = com.sec.internal.ims.cmstore.utils.Util.getNormalizedTelUri(r5, r6)     // Catch:{ all -> 0x0242 }
            java.lang.String r7 = r13.TAG     // Catch:{ all -> 0x0242 }
            java.lang.StringBuilder r8 = new java.lang.StringBuilder     // Catch:{ all -> 0x0242 }
            r8.<init>()     // Catch:{ all -> 0x0242 }
            r8.append(r0)     // Catch:{ all -> 0x0242 }
            r8.append(r5)     // Catch:{ all -> 0x0242 }
            java.lang.String r9 = ", telUri = "
            r8.append(r9)     // Catch:{ all -> 0x0242 }
            java.lang.String r9 = com.sec.internal.log.IMSLog.checker(r6)     // Catch:{ all -> 0x0242 }
            r8.append(r9)     // Catch:{ all -> 0x0242 }
            java.lang.String r8 = r8.toString()     // Catch:{ all -> 0x0242 }
            android.util.Log.e(r7, r8)     // Catch:{ all -> 0x0242 }
            if (r6 != 0) goto L_0x01de
            goto L_0x01a2
        L_0x01de:
            java.lang.String r7 = " is deleted from DB."
            boolean r8 = r4.contains(r6)     // Catch:{ all -> 0x0242 }
            if (r8 == 0) goto L_0x01ec
            r4.remove(r6)     // Catch:{ all -> 0x0242 }
            java.lang.String r7 = " contains."
            goto L_0x01fc
        L_0x01ec:
            java.lang.String r6 = "_id"
            int r6 = r14.getColumnIndexOrThrow(r6)     // Catch:{ all -> 0x0242 }
            long r8 = r14.getLong(r6)     // Catch:{ all -> 0x0242 }
            r13.deleteParticipantsFromBufferDb(r5, r2)     // Catch:{ all -> 0x0242 }
            r13.deleteParticipantsUsingRowId(r8)     // Catch:{ all -> 0x0242 }
        L_0x01fc:
            java.lang.String r6 = r13.TAG     // Catch:{ all -> 0x0242 }
            java.lang.StringBuilder r8 = new java.lang.StringBuilder     // Catch:{ all -> 0x0242 }
            r8.<init>()     // Catch:{ all -> 0x0242 }
            r8.append(r0)     // Catch:{ all -> 0x0242 }
            r8.append(r5)     // Catch:{ all -> 0x0242 }
            r8.append(r7)     // Catch:{ all -> 0x0242 }
            java.lang.String r5 = r8.toString()     // Catch:{ all -> 0x0242 }
            android.util.Log.d(r6, r5)     // Catch:{ all -> 0x0242 }
            goto L_0x01a2
        L_0x0214:
            int r0 = r4.size()     // Catch:{ all -> 0x0242 }
            if (r0 <= 0) goto L_0x023c
            java.util.ArrayList r0 = r13.insertNewParticipantToBufferDB(r4, r2)     // Catch:{ all -> 0x0242 }
            r13.insertRCSParticipantsDb((java.util.ArrayList<android.content.ContentValues>) r0)     // Catch:{ all -> 0x0242 }
            java.lang.String r0 = r13.TAG     // Catch:{ all -> 0x0242 }
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ all -> 0x0242 }
            r5.<init>()     // Catch:{ all -> 0x0242 }
            java.lang.String r6 = "participants = "
            r5.append(r6)     // Catch:{ all -> 0x0242 }
            r5.append(r4)     // Catch:{ all -> 0x0242 }
            java.lang.String r4 = " are added into DB"
            r5.append(r4)     // Catch:{ all -> 0x0242 }
            java.lang.String r4 = r5.toString()     // Catch:{ all -> 0x0242 }
            android.util.Log.d(r0, r4)     // Catch:{ all -> 0x0242 }
        L_0x023c:
            if (r14 == 0) goto L_0x024e
            r14.close()     // Catch:{ all -> 0x0255 }
            goto L_0x024e
        L_0x0242:
            r0 = move-exception
            if (r14 == 0) goto L_0x024d
            r14.close()     // Catch:{ all -> 0x0249 }
            goto L_0x024d
        L_0x0249:
            r14 = move-exception
            r0.addSuppressed(r14)     // Catch:{ all -> 0x0255 }
        L_0x024d:
            throw r0     // Catch:{ all -> 0x0255 }
        L_0x024e:
            if (r3 == 0) goto L_0x03fb
            r3.close()     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            goto L_0x03fb
        L_0x0255:
            r14 = move-exception
            if (r3 == 0) goto L_0x0260
            r3.close()     // Catch:{ all -> 0x025c }
            goto L_0x0260
        L_0x025c:
            r0 = move-exception
            r14.addSuppressed(r0)     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
        L_0x0260:
            throw r14     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
        L_0x0261:
            android.content.ContentValues r0 = new android.content.ContentValues     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            r0.<init>()     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            java.util.Set<com.sec.ims.util.ImsUri> r9 = r14.mNomalizedOtherParticipants     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            com.sec.internal.constants.ims.servicemodules.im.ChatMode r10 = com.sec.internal.constants.ims.servicemodules.im.ChatMode.OFF     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            int r10 = r10.getId()     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            java.lang.String r2 = com.sec.internal.ims.util.StringIdGenerator.generateChatId(r9, r5, r10)     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            r0.put(r6, r2)     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            java.lang.String r6 = "own_sim_imsi"
            com.sec.internal.ims.cmstore.MessageStoreClient r9 = r13.mStoreClient     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager r9 = r9.getPrerenceManager()     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            java.lang.String r9 = r9.getUserCtn()     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            r0.put(r6, r9)     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            boolean r6 = r13.isCmsEnabled     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            java.lang.String r9 = "is_group_chat"
            if (r6 == 0) goto L_0x0297
            if (r3 == 0) goto L_0x028e
            r6 = r5
            goto L_0x028f
        L_0x028e:
            r6 = r4
        L_0x028f:
            java.lang.Integer r6 = java.lang.Integer.valueOf(r6)     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            r0.put(r9, r6)     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            goto L_0x02a9
        L_0x0297:
            java.util.Set<com.sec.ims.util.ImsUri> r6 = r14.mNomalizedOtherParticipants     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            int r6 = r6.size()     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            if (r6 <= r5) goto L_0x02a1
            r6 = r5
            goto L_0x02a2
        L_0x02a1:
            r6 = r4
        L_0x02a2:
            java.lang.Integer r6 = java.lang.Integer.valueOf(r6)     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            r0.put(r9, r6)     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
        L_0x02a9:
            java.lang.String r6 = "is_ft_group_chat"
            java.lang.Integer r9 = java.lang.Integer.valueOf(r5)     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            r0.put(r6, r9)     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            java.lang.String r6 = "status"
            com.sec.internal.constants.ims.servicemodules.im.ChatData$State r9 = com.sec.internal.constants.ims.servicemodules.im.ChatData.State.INACTIVE     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            int r9 = r9.getId()     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            java.lang.Integer r9 = java.lang.Integer.valueOf(r9)     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            r0.put(r6, r9)     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            java.lang.String r6 = r14.SUBJECT     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            r0.put(r8, r6)     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            java.util.Set<com.sec.ims.util.ImsUri> r6 = r14.mNomalizedOtherParticipants     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            int r6 = r6.size()     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            java.lang.String r8 = "chat_type"
            if (r6 <= r5) goto L_0x0314
            com.sec.internal.ims.cmstore.MessageStoreClient r6 = r13.mStoreClient     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            com.sec.internal.ims.cmstore.strategy.CloudMessageStrategyManager r6 = r6.getCloudMessageStrategyManager()     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            com.sec.internal.interfaces.ims.cmstore.ICloudMessageStrategy r6 = r6.getStrategy()     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            boolean r6 = r6.shouldCareGroupChatAttribute()     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            if (r6 == 0) goto L_0x02f6
            boolean r3 = r14.IS_OPEN_GROUP     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            if (r3 == 0) goto L_0x02e8
            com.sec.internal.constants.ims.servicemodules.im.ChatData$ChatType r3 = com.sec.internal.constants.ims.servicemodules.im.ChatData.ChatType.REGULAR_GROUP_CHAT     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            goto L_0x02ea
        L_0x02e8:
            com.sec.internal.constants.ims.servicemodules.im.ChatData$ChatType r3 = com.sec.internal.constants.ims.servicemodules.im.ChatData.ChatType.PARTICIPANT_BASED_GROUP_CHAT     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
        L_0x02ea:
            int r3 = r3.getId()     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            java.lang.Integer r3 = java.lang.Integer.valueOf(r3)     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            r0.put(r8, r3)     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            goto L_0x0331
        L_0x02f6:
            if (r3 == 0) goto L_0x0306
            com.sec.internal.constants.ims.servicemodules.im.ChatData$ChatType r3 = com.sec.internal.constants.ims.servicemodules.im.ChatData.ChatType.REGULAR_GROUP_CHAT     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            int r3 = r3.getId()     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            java.lang.Integer r3 = java.lang.Integer.valueOf(r3)     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            r0.put(r8, r3)     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            goto L_0x0331
        L_0x0306:
            com.sec.internal.constants.ims.servicemodules.im.ChatData$ChatType r3 = com.sec.internal.constants.ims.servicemodules.im.ChatData.ChatType.PARTICIPANT_BASED_GROUP_CHAT     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            int r3 = r3.getId()     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            java.lang.Integer r3 = java.lang.Integer.valueOf(r3)     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            r0.put(r8, r3)     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            goto L_0x0331
        L_0x0314:
            if (r3 == 0) goto L_0x0324
            com.sec.internal.constants.ims.servicemodules.im.ChatData$ChatType r3 = com.sec.internal.constants.ims.servicemodules.im.ChatData.ChatType.REGULAR_GROUP_CHAT     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            int r3 = r3.getId()     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            java.lang.Integer r3 = java.lang.Integer.valueOf(r3)     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            r0.put(r8, r3)     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            goto L_0x0331
        L_0x0324:
            com.sec.internal.constants.ims.servicemodules.im.ChatData$ChatType r3 = com.sec.internal.constants.ims.servicemodules.im.ChatData.ChatType.ONE_TO_ONE_CHAT     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            int r3 = r3.getId()     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            java.lang.Integer r3 = java.lang.Integer.valueOf(r3)     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            r0.put(r8, r3)     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
        L_0x0331:
            java.lang.String r3 = "is_muted"
            java.lang.Integer r4 = java.lang.Integer.valueOf(r4)     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            r0.put(r3, r4)     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            java.lang.String r3 = "max_participants_count"
            r4 = 100
            java.lang.Integer r4 = java.lang.Integer.valueOf(r4)     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            r0.put(r3, r4)     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            java.lang.String r3 = "imdn_notifications_availability"
            java.lang.Integer r4 = java.lang.Integer.valueOf(r5)     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            r0.put(r3, r4)     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            java.lang.String r3 = "direction"
            java.lang.String r4 = "IN"
            java.lang.String r5 = r14.DIRECTION     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            boolean r4 = r4.equalsIgnoreCase(r5)     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            if (r4 == 0) goto L_0x0361
            com.sec.internal.constants.ims.servicemodules.im.ImDirection r4 = com.sec.internal.constants.ims.servicemodules.im.ImDirection.INCOMING     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            int r4 = r4.getId()     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            goto L_0x0367
        L_0x0361:
            com.sec.internal.constants.ims.servicemodules.im.ImDirection r4 = com.sec.internal.constants.ims.servicemodules.im.ImDirection.OUTGOING     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            int r4 = r4.getId()     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
        L_0x0367:
            java.lang.Integer r4 = java.lang.Integer.valueOf(r4)     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            r0.put(r3, r4)     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            java.lang.String r3 = "conversation_id"
            java.lang.String r4 = r14.CONVERSATION_ID     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            r0.put(r3, r4)     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            java.lang.String r3 = "contribution_id"
            java.lang.String r4 = r14.CONTRIBUTION_ID     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            r0.put(r3, r4)     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            java.lang.String r3 = "syncaction"
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r4 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Insert     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            int r4 = r4.getId()     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            java.lang.Integer r4 = java.lang.Integer.valueOf(r4)     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            r0.put(r3, r4)     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            java.lang.String r3 = "syncdirection"
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r4 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            int r4 = r4.getId()     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            java.lang.Integer r4 = java.lang.Integer.valueOf(r4)     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            r0.put(r3, r4)     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            java.lang.String r3 = r14.mLine     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            if (r3 == 0) goto L_0x03b8
            com.sec.internal.ims.cmstore.MessageStoreClient r4 = r13.mStoreClient     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager r4 = r4.getPrerenceManager()     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            java.lang.String r4 = r4.getUserTelCtn()     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            boolean r3 = r3.equalsIgnoreCase(r4)     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            if (r3 != 0) goto L_0x03b8
            java.lang.String r3 = "preferred_uri"
            java.lang.String r4 = r14.mLine     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            r0.put(r3, r4)     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
        L_0x03b8:
            java.lang.String r3 = "linenum"
            java.lang.String r4 = r14.mLine     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            r0.put(r3, r4)     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            java.lang.String r3 = r13.TAG     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            java.lang.StringBuilder r4 = new java.lang.StringBuilder     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            r4.<init>()     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            java.lang.String r5 = "session sim imsi : "
            r4.append(r5)     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            com.sec.internal.ims.cmstore.MessageStoreClient r5 = r13.mStoreClient     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            java.lang.String r5 = r5.getCurrentIMSI()     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            r4.append(r5)     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            java.lang.String r4 = r4.toString()     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            android.util.Log.d(r3, r4)     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            com.sec.internal.ims.cmstore.MessageStoreClient r3 = r13.mStoreClient     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            java.lang.String r3 = r3.getCurrentIMSI()     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            r0.put(r7, r3)     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            r3 = 10
            r13.insertTable(r3, r0)     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            java.util.ArrayList r14 = r13.insertRCSParticipantToBufferDBUsingObject((com.sec.internal.ims.cmstore.params.ParamOMAObject) r14, (java.lang.String) r2)     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            com.sec.internal.ims.cmstore.adapters.CloudMessageRCSStorageAdapter r3 = r13.mRCSStorage     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            int r14 = r3.insertSessionFromBufferDbToRCSDb(r0, r14)     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            java.lang.String r2 = r13.updateBufferDbChatIdFromRcsDb(r2, r14)     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
            r13.updateParticipantsIdFromRcsDb(r2)     // Catch:{ NullPointerException -> 0x0404, all -> 0x0401 }
        L_0x03fb:
            if (r1 == 0) goto L_0x042c
            r1.close()
            goto L_0x042c
        L_0x0401:
            r13 = move-exception
            r2 = r1
            goto L_0x042d
        L_0x0404:
            r14 = move-exception
            r0 = r2
            r2 = r1
            goto L_0x040c
        L_0x0408:
            r13 = move-exception
            goto L_0x042d
        L_0x040a:
            r14 = move-exception
            r0 = r2
        L_0x040c:
            java.lang.String r13 = r13.TAG     // Catch:{ all -> 0x0408 }
            java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch:{ all -> 0x0408 }
            r1.<init>()     // Catch:{ all -> 0x0408 }
            java.lang.String r3 = "nullpointer exception: "
            r1.append(r3)     // Catch:{ all -> 0x0408 }
            java.lang.String r14 = r14.getMessage()     // Catch:{ all -> 0x0408 }
            r1.append(r14)     // Catch:{ all -> 0x0408 }
            java.lang.String r14 = r1.toString()     // Catch:{ all -> 0x0408 }
            android.util.Log.e(r13, r14)     // Catch:{ all -> 0x0408 }
            if (r2 == 0) goto L_0x042b
            r2.close()
        L_0x042b:
            r2 = r0
        L_0x042c:
            return r2
        L_0x042d:
            if (r2 == 0) goto L_0x0432
            r2.close()
        L_0x0432:
            throw r13
        L_0x0433:
            java.lang.String r13 = r13.TAG
            java.lang.String r14 = "searchOrCreateSession, invalid OMA param issue"
            android.util.Log.e(r13, r14)
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder.searchOrCreateSession(com.sec.internal.ims.cmstore.params.ParamOMAObject):java.lang.String");
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x002a  */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x002f  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public java.lang.String updateBufferDbChatIdFromRcsDb(java.lang.String r3, int r4) {
        /*
            r2 = this;
            r0 = 1
            if (r4 >= r0) goto L_0x0004
            return r3
        L_0x0004:
            com.sec.internal.ims.cmstore.adapters.CloudMessageRCSStorageAdapter r0 = r2.mRCSStorage
            android.database.Cursor r0 = r0.querySessionUsingId(r4)
            if (r0 == 0) goto L_0x0027
            boolean r1 = r0.moveToFirst()     // Catch:{ all -> 0x001d }
            if (r1 == 0) goto L_0x0027
            java.lang.String r1 = "chat_id"
            int r1 = r0.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x001d }
            java.lang.String r1 = r0.getString(r1)     // Catch:{ all -> 0x001d }
            goto L_0x0028
        L_0x001d:
            r2 = move-exception
            r0.close()     // Catch:{ all -> 0x0022 }
            goto L_0x0026
        L_0x0022:
            r3 = move-exception
            r2.addSuppressed(r3)
        L_0x0026:
            throw r2
        L_0x0027:
            r1 = 0
        L_0x0028:
            if (r0 == 0) goto L_0x002d
            r0.close()
        L_0x002d:
            if (r1 == 0) goto L_0x003c
            r2.updateIdFromRcsDb(r4, r1)
            boolean r4 = r3.equalsIgnoreCase(r1)
            if (r4 != 0) goto L_0x003c
            r2.updateChatId(r3, r1)
            return r1
        L_0x003c:
            return r3
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder.updateBufferDbChatIdFromRcsDb(java.lang.String, int):java.lang.String");
    }

    private void updateIdFromRcsDb(int i, String str) {
        String str2 = this.TAG;
        Log.d(str2, " updateIdFromRcsDb id: " + i);
        ContentValues contentValues = new ContentValues();
        contentValues.put("_id", Integer.valueOf(i));
        this.mBufferDB.updateRCSSessionTable(contentValues, "chat_id=?", new String[]{str});
    }

    private void updateChatId(String str, String str2) {
        String str3 = this.TAG;
        Log.d(str3, "updateChatId: " + str + " chatid: " + str2);
        ContentValues contentValues = new ContentValues();
        contentValues.put("chat_id", str2);
        this.mBufferDB.updateRCSSessionTable(contentValues, "chat_id=?", new String[]{str});
        ContentValues contentValues2 = new ContentValues();
        contentValues2.put("chat_id", str2);
        this.mBufferDB.updateRCSParticipantsTable(contentValues2, "chat_id=?", new String[]{str});
    }

    public ArrayList<ContentValues> insertRCSParticipantToBufferDBUsingObject(ParamOMAObject paramOMAObject, String str) {
        ArrayList<ContentValues> arrayList = new ArrayList<>();
        for (ImsUri next : paramOMAObject.mNomalizedOtherParticipants) {
            String str2 = this.TAG;
            Log.i(str2, "insertParticipant " + IMSLog.numberChecker(next.toString()));
            if (!next.toString().contains("groupchat")) {
                ContentValues contentValues = new ContentValues();
                contentValues.put("chat_id", str);
                contentValues.put("type", Integer.valueOf(ImParticipant.Type.REGULAR.getId()));
                contentValues.put("status", Integer.valueOf(ImParticipant.Status.ACCEPTED.getId()));
                contentValues.put("uri", next.toString());
                contentValues.put("sim_imsi", this.IMSI);
                insertTable(2, contentValues);
                arrayList.add(contentValues);
            }
        }
        return arrayList;
    }

    public ArrayList<ContentValues> insertRCSParticipantToBufferDBUsingObject(Set<ImsUri> set, String str) {
        ArrayList<ContentValues> arrayList = new ArrayList<>();
        for (ImsUri imsUri : set) {
            ContentValues contentValues = new ContentValues();
            contentValues.put("chat_id", str);
            contentValues.put("type", Integer.valueOf(ImParticipant.Type.REGULAR.getId()));
            contentValues.put("status", Integer.valueOf(ImParticipant.Status.ACCEPTED.getId()));
            contentValues.put("uri", imsUri.toString());
            contentValues.put("sim_imsi", this.IMSI);
            insertTable(2, contentValues);
            arrayList.add(contentValues);
        }
        return arrayList;
    }

    public ArrayList<ContentValues> insertNewParticipantToBufferDB(Set<ImsUri> set, String str) {
        ArrayList<ContentValues> arrayList = new ArrayList<>();
        for (ImsUri imsUri : set) {
            ContentValues contentValues = new ContentValues();
            contentValues.put("chat_id", str);
            contentValues.put("type", Integer.valueOf(ImParticipant.Type.REGULAR.getId()));
            contentValues.put("status", Integer.valueOf(ImParticipant.Status.ACCEPTED.getId()));
            contentValues.put("uri", imsUri.toString());
            contentValues.put("sim_imsi", this.IMSI);
            insertTable(2, contentValues);
            arrayList.add(contentValues);
        }
        return arrayList;
    }

    /* JADX WARNING: Removed duplicated region for block: B:14:0x0035  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int queryParticipantCount(java.lang.String r4) {
        /*
            r3 = this;
            android.database.Cursor r4 = r3.queryParticipantsUsingChatId(r4)
            if (r4 == 0) goto L_0x0032
            boolean r0 = r4.moveToFirst()     // Catch:{ all -> 0x0028 }
            if (r0 == 0) goto L_0x0032
            int r0 = r4.getCount()     // Catch:{ all -> 0x0028 }
            java.lang.String r3 = r3.TAG     // Catch:{ all -> 0x0028 }
            java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch:{ all -> 0x0028 }
            r1.<init>()     // Catch:{ all -> 0x0028 }
            java.lang.String r2 = "queryParticipantCount participantCount = "
            r1.append(r2)     // Catch:{ all -> 0x0028 }
            r1.append(r0)     // Catch:{ all -> 0x0028 }
            java.lang.String r1 = r1.toString()     // Catch:{ all -> 0x0028 }
            com.sec.internal.log.IMSLog.i(r3, r1)     // Catch:{ all -> 0x0028 }
            goto L_0x0033
        L_0x0028:
            r3 = move-exception
            r4.close()     // Catch:{ all -> 0x002d }
            goto L_0x0031
        L_0x002d:
            r4 = move-exception
            r3.addSuppressed(r4)
        L_0x0031:
            throw r3
        L_0x0032:
            r0 = 0
        L_0x0033:
            if (r4 == 0) goto L_0x0038
            r4.close()
        L_0x0038:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder.queryParticipantCount(java.lang.String):int");
    }

    public int queryImdnBufferDBandUpdateRcsMessageBufferDb(String str, String str2) {
        NotificationStatus notificationStatus;
        int queryParticipantCount = queryParticipantCount(str2);
        Log.i(this.TAG, "queryImdnBufferDBandUpdateRcsMessageBufferDb: " + IMSLog.checker(str) + ", notDisplayedCnt: " + queryParticipantCount);
        Cursor queryRCSImdnUseImdnId = this.mBufferDB.queryRCSImdnUseImdnId(str);
        int i = 0;
        if (queryRCSImdnUseImdnId != null) {
            try {
                if (queryRCSImdnUseImdnId.moveToFirst()) {
                    if (this.isCmsEnabled) {
                        queryParticipantCount = queryRCSImdnUseImdnId.getCount();
                        Log.i(this.TAG, "updated notDisplayedCnt: " + queryParticipantCount);
                    }
                    ContentValues contentValues = new ContentValues();
                    int i2 = 0;
                    int i3 = 0;
                    do {
                        int i4 = queryRCSImdnUseImdnId.getInt(queryRCSImdnUseImdnId.getColumnIndexOrThrow("status"));
                        notificationStatus = NotificationStatus.DISPLAYED;
                        if (i4 == notificationStatus.getId()) {
                            i2++;
                            contentValues.put(ImContract.Message.DISPOSITION_NOTIFICATION_STATUS, Integer.valueOf(notificationStatus.getId()));
                            contentValues.put(ImContract.Message.DISPLAYED_TIMESTAMP, Long.valueOf(queryRCSImdnUseImdnId.getLong(queryRCSImdnUseImdnId.getColumnIndexOrThrow("timestamp"))));
                        } else {
                            NotificationStatus notificationStatus2 = NotificationStatus.DELIVERED;
                            if (i4 == notificationStatus2.getId()) {
                                contentValues.put("notification_status", Integer.valueOf(notificationStatus2.getId()));
                                i3++;
                            }
                        }
                    } while (queryRCSImdnUseImdnId.moveToNext());
                    Log.d(this.TAG, "queryImdnBufferDBandUpdateRcsMessageBufferDb: displayedCnt=" + i2 + ", deliveredCnt=" + i3);
                    if (i2 == 0 && i3 > 0) {
                        queryRCSImdnUseImdnId.moveToFirst();
                        contentValues.put(ImContract.Message.DISPOSITION_NOTIFICATION_STATUS, Integer.valueOf(NotificationStatus.DELIVERED.getId()));
                        contentValues.put(ImContract.ChatItem.DELIVERED_TIMESTAMP, Long.valueOf(queryRCSImdnUseImdnId.getLong(queryRCSImdnUseImdnId.getColumnIndexOrThrow("timestamp"))));
                    }
                    if (i2 > 0) {
                        int i5 = queryParticipantCount - i2;
                        if (i5 < 0) {
                            i5 = 0;
                        }
                        contentValues.put(ImContract.Message.NOT_DISPLAYED_COUNTER, Integer.valueOf(i5));
                    }
                    if (i2 == queryParticipantCount) {
                        contentValues.put("notification_status", Integer.valueOf(notificationStatus.getId()));
                    }
                    if (contentValues.size() > 0) {
                        i = this.mBufferDB.updateRCSTable(contentValues, "imdn_message_id=?", new String[]{str});
                    }
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (queryRCSImdnUseImdnId != null) {
            queryRCSImdnUseImdnId.close();
        }
        return i;
        throw th;
    }

    public int updateRcsMessageBufferDbIfNewIMDNReceived(String str, int i, int i2, String str2) {
        int i3;
        Log.d(this.TAG, "updateRcsMessageBufferDbIfNewIMDNReceived: " + IMSLog.checker(str) + ", notDisplayedCnt = " + i + ", rcsMsgDisplayStatus = " + i2 + ", status = " + str2);
        if ("displayed".equalsIgnoreCase(str2)) {
            i3 = NotificationStatus.DISPLAYED.getId();
        } else {
            i3 = NotificationStatus.DELIVERED.getId();
        }
        ContentValues contentValues = new ContentValues();
        NotificationStatus notificationStatus = NotificationStatus.DISPLAYED;
        if (i3 == notificationStatus.getId()) {
            if (i2 == NotificationStatus.NONE.getId() || i2 == NotificationStatus.DELIVERED.getId()) {
                contentValues.put("notification_status", Integer.valueOf(notificationStatus.getId()));
                contentValues.put(ImContract.Message.DISPOSITION_NOTIFICATION_STATUS, Integer.valueOf(notificationStatus.getId()));
            }
            i--;
            contentValues.put(ImContract.Message.NOT_DISPLAYED_COUNTER, Integer.valueOf(i >= 0 ? i : 0));
        } else {
            NotificationStatus notificationStatus2 = NotificationStatus.DELIVERED;
            if (i3 == notificationStatus2.getId() && i2 == NotificationStatus.NONE.getId()) {
                contentValues.put("notification_status", Integer.valueOf(notificationStatus2.getId()));
                contentValues.put(ImContract.Message.DISPOSITION_NOTIFICATION_STATUS, Integer.valueOf(notificationStatus2.getId()));
            }
        }
        Log.i(this.TAG, "updateRcsMessageBufferDbIfNewIMDNReceived: newNotDisplayedCnt = " + i);
        if (contentValues.size() <= 0) {
            return 0;
        }
        return this.mBufferDB.updateRCSTable(contentValues, "imdn_message_id=?", new String[]{str});
    }

    public void setNotificationStatusAndTimestamp(ImdnObject imdnObject, ContentValues contentValues) {
        Log.i(this.TAG, "setNotificationStatusAndTimestamp");
        contentValues.put(ImContract.MessageNotification.SENDER_URI, imdnObject.originalTo);
        ImdnInfo[] imdnInfoArr = imdnObject.imdnInfo;
        int length = imdnInfoArr.length;
        String str = "";
        int i = 0;
        String str2 = str;
        String str3 = str2;
        while (true) {
            if (i >= length) {
                break;
            }
            ImdnInfo imdnInfo = imdnInfoArr[i];
            if ("displayed".equalsIgnoreCase(imdnInfo.type)) {
                str = imdnInfo.date;
                break;
            }
            if ("delivered".equalsIgnoreCase(imdnInfo.type)) {
                str2 = imdnInfo.date;
            } else {
                str3 = imdnInfo.date;
            }
            i++;
        }
        if (!TextUtils.isEmpty(str)) {
            contentValues.put("timestamp", Long.valueOf(getDateFromDateString(str)));
            contentValues.put("status", Integer.valueOf(NotificationStatus.DISPLAYED.getId()));
        } else if (!TextUtils.isEmpty(str2)) {
            contentValues.put("timestamp", Long.valueOf(getDateFromDateString(str2)));
            contentValues.put("status", Integer.valueOf(NotificationStatus.DELIVERED.getId()));
        } else {
            contentValues.put("timestamp", Long.valueOf(getDateFromDateString(str3)));
            contentValues.put("status", Integer.valueOf(NotificationStatus.NONE.getId()));
        }
    }

    public HashMap<String, Integer> queryRCSNotificationStatus(String str) {
        HashMap<String, Integer> hashMap = new HashMap<>();
        Cursor queryImdnUsingImdnId = queryImdnUsingImdnId(str);
        if (queryImdnUsingImdnId != null) {
            try {
                if (queryImdnUsingImdnId.moveToFirst()) {
                    String str2 = this.TAG;
                    Log.i(str2, "queryRCSNotificationStatus notificationCursor:" + queryImdnUsingImdnId.getCount());
                    do {
                        hashMap.put(queryImdnUsingImdnId.getString(queryImdnUsingImdnId.getColumnIndexOrThrow(ImContract.MessageNotification.SENDER_URI)), Integer.valueOf(queryImdnUsingImdnId.getInt(queryImdnUsingImdnId.getColumnIndexOrThrow("status"))));
                    } while (queryImdnUsingImdnId.moveToNext());
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (queryImdnUsingImdnId != null) {
            queryImdnUsingImdnId.close();
        }
        return hashMap;
        throw th;
    }

    public boolean insertOrUpdateToNotificationDB(ImdnList imdnList, ContentValues contentValues, String str, String str2, int i) {
        Cursor searchBufferNotificationUsingImdnAndTelUri = searchBufferNotificationUsingImdnAndTelUri(str, str2);
        if (searchBufferNotificationUsingImdnAndTelUri != null) {
            try {
                if (!(!searchBufferNotificationUsingImdnAndTelUri.moveToFirst() || imdnList == null || imdnList.imdn == null)) {
                    int i2 = searchBufferNotificationUsingImdnAndTelUri.getInt(searchBufferNotificationUsingImdnAndTelUri.getColumnIndexOrThrow("status"));
                    if ((i == NotificationStatus.DELIVERED.getId() && i2 == NotificationStatus.DISPLAYED.getId()) || i == i2) {
                        String str3 = this.TAG;
                        Log.d(str3, "insertOrUpdateToNotificationDB delivered comes after displayed or same update, shouldn't update. cloudNotificationStatus: " + i + ", bufferDBNotificationStatus: " + i2);
                        searchBufferNotificationUsingImdnAndTelUri.close();
                        return false;
                    }
                    updateTable(13, contentValues, "_bufferdbid=?", new String[]{String.valueOf(searchBufferNotificationUsingImdnAndTelUri.getLong(searchBufferNotificationUsingImdnAndTelUri.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID)))});
                    searchBufferNotificationUsingImdnAndTelUri.close();
                    return true;
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        int insertRCSNotificationDbfromBufferDB = insertRCSNotificationDbfromBufferDB(new ContentValues(contentValues));
        if (insertRCSNotificationDbfromBufferDB > 0) {
            contentValues.put("id", Integer.valueOf(insertRCSNotificationDbfromBufferDB));
        }
        contentValues.put("sim_imsi", this.IMSI);
        insertTable(13, contentValues);
        if (searchBufferNotificationUsingImdnAndTelUri != null) {
            searchBufferNotificationUsingImdnAndTelUri.close();
        }
        return false;
        throw th;
    }

    public void insertRCSImdnToBufferDBUsingObject(ParamOMAObject paramOMAObject, String str, ContentValues contentValues) {
        ImdnObject[] imdnObjectArr;
        Log.i(this.TAG, "insertRCSImdnToBufferDBUsingObject: " + paramOMAObject);
        if ("IN".equalsIgnoreCase(paramOMAObject.DIRECTION)) {
            Log.i(this.TAG, "insertRCSImdnToBufferDBUsingObject skip for incoming");
            return;
        }
        ContentValues contentValues2 = new ContentValues();
        contentValues2.put(CloudMessageProviderContract.BufferDBExtensionBase.RES_URL, Util.decodeUrlFromServer(paramOMAObject.resourceURL.toString()));
        contentValues2.put(CloudMessageProviderContract.BufferDBExtensionBase.PARENTFOLDER, Util.decodeUrlFromServer(paramOMAObject.parentFolder.toString()));
        if (!TextUtils.isEmpty(paramOMAObject.path)) {
            contentValues2.put("path", Util.decodeUrlFromServer(paramOMAObject.path));
        }
        contentValues2.put("imdn_id", paramOMAObject.correlationId);
        contentValues2.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.None.getId()));
        contentValues2.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.Done.getId()));
        ImdnList imdnList = paramOMAObject.mImdns;
        if (imdnList != null) {
            contentValues2.put(CloudMessageProviderContract.BufferDBExtensionBase.LASTMODSEQ, Long.valueOf(imdnList.lastModSeq));
        }
        ArrayList arrayList = new ArrayList();
        Cursor queryParticipantsUsingChatId = queryParticipantsUsingChatId(str);
        while (queryParticipantsUsingChatId != null) {
            try {
                if (!queryParticipantsUsingChatId.moveToNext()) {
                    break;
                }
                arrayList.add(queryParticipantsUsingChatId.getString(queryParticipantsUsingChatId.getColumnIndexOrThrow("uri")));
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (queryParticipantsUsingChatId != null) {
            queryParticipantsUsingChatId.close();
        }
        ArrayList arrayList2 = new ArrayList();
        ImdnList imdnList2 = paramOMAObject.mImdns;
        boolean z = false;
        if (!(imdnList2 == null || (imdnObjectArr = imdnList2.imdn) == null)) {
            for (ImdnObject imdnObject : imdnObjectArr) {
                if (imdnObject.imdnInfo != null) {
                    arrayList.remove(imdnObject.originalTo);
                    arrayList2.add(imdnObject);
                }
            }
        }
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            ImdnObject imdnObject2 = new ImdnObject();
            imdnObject2.originalTo = (String) it.next();
            ImdnInfo[] imdnInfoArr = new ImdnInfo[1];
            imdnObject2.imdnInfo = imdnInfoArr;
            imdnInfoArr[0] = new ImdnInfo();
            imdnObject2.imdnInfo[0].date = paramOMAObject.DATE;
            arrayList2.add(imdnObject2);
        }
        int size = arrayList2.size();
        Iterator it2 = arrayList2.iterator();
        while (it2.hasNext()) {
            ImdnObject imdnObject3 = (ImdnObject) it2.next();
            ContentValues contentValues3 = new ContentValues(contentValues2);
            setNotificationStatusAndTimestamp(imdnObject3, contentValues3);
            Integer asInteger = contentValues3.getAsInteger("status");
            if (asInteger != null) {
                if (asInteger.intValue() != NotificationStatus.DELIVERED.getId()) {
                    if (asInteger.intValue() == NotificationStatus.DISPLAYED.getId()) {
                        if (size > 0) {
                            size--;
                        }
                    }
                    insertOrUpdateToNotificationDB(paramOMAObject.mImdns, contentValues3, paramOMAObject.correlationId, imdnObject3.originalTo, asInteger.intValue());
                }
                z = true;
                insertOrUpdateToNotificationDB(paramOMAObject.mImdns, contentValues3, paramOMAObject.correlationId, imdnObject3.originalTo, asInteger.intValue());
            }
        }
        if (size == 0) {
            NotificationStatus notificationStatus = NotificationStatus.DISPLAYED;
            contentValues.put("notification_status", Integer.valueOf(notificationStatus.getId()));
            contentValues.put(ImContract.Message.DISPOSITION_NOTIFICATION_STATUS, Integer.valueOf(notificationStatus.getId()));
        } else if (z) {
            NotificationStatus notificationStatus2 = NotificationStatus.DELIVERED;
            contentValues.put("notification_status", Integer.valueOf(notificationStatus2.getId()));
            contentValues.put(ImContract.Message.DISPOSITION_NOTIFICATION_STATUS, Integer.valueOf(notificationStatus2.getId()));
        }
        if (size != arrayList2.size()) {
            contentValues.put(ImContract.Message.DISPOSITION_NOTIFICATION_STATUS, Integer.valueOf(NotificationStatus.DISPLAYED.getId()));
        }
        Log.i(this.TAG, "insertRCSImdnToBufferDBUsingObject notDisplayedCount: " + size + " isDelivered: " + z + " participants count: " + arrayList2.size());
        contentValues.put(ImContract.Message.NOT_DISPLAYED_COUNTER, Integer.valueOf(size));
        return;
        throw th;
    }

    /* JADX WARNING: Removed duplicated region for block: B:14:0x00a0  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int queryBufferDbandUpdateRcsMessageDb(java.lang.String r9) {
        /*
            r8 = this;
            java.lang.String r0 = "not_displayed_counter"
            java.lang.String r1 = "displayed_timestamp"
            java.lang.String r2 = "delivered_timestamp"
            java.lang.String r3 = "disposition_notification_status"
            java.lang.String r4 = "notification_status"
            java.lang.String r5 = r8.TAG
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            java.lang.String r7 = "queryBufferDbandUpdateRcsMessageDb: "
            r6.append(r7)
            java.lang.String r7 = com.sec.internal.log.IMSLog.checker(r9)
            r6.append(r7)
            java.lang.String r6 = r6.toString()
            android.util.Log.d(r5, r6)
            java.lang.String[] r9 = new java.lang.String[]{r9}
            com.sec.internal.ims.cmstore.adapters.CloudMessageBufferDBPersister r5 = r8.mBufferDB
            r6 = 0
            java.lang.String r7 = "imdn_message_id=?"
            android.database.Cursor r9 = r5.queryRCSMessages(r6, r7, r9, r6)
            if (r9 == 0) goto L_0x009d
            boolean r5 = r9.moveToFirst()     // Catch:{ all -> 0x0093 }
            if (r5 == 0) goto L_0x009d
            android.content.ContentValues r5 = new android.content.ContentValues     // Catch:{ all -> 0x0093 }
            r5.<init>()     // Catch:{ all -> 0x0093 }
            int r6 = r9.getColumnIndex(r4)     // Catch:{ all -> 0x0093 }
            java.lang.String r6 = r9.getString(r6)     // Catch:{ all -> 0x0093 }
            r5.put(r4, r6)     // Catch:{ all -> 0x0093 }
            int r4 = r9.getColumnIndex(r3)     // Catch:{ all -> 0x0093 }
            java.lang.String r4 = r9.getString(r4)     // Catch:{ all -> 0x0093 }
            r5.put(r3, r4)     // Catch:{ all -> 0x0093 }
            int r3 = r9.getColumnIndex(r2)     // Catch:{ all -> 0x0093 }
            long r3 = r9.getLong(r3)     // Catch:{ all -> 0x0093 }
            java.lang.Long r3 = java.lang.Long.valueOf(r3)     // Catch:{ all -> 0x0093 }
            r5.put(r2, r3)     // Catch:{ all -> 0x0093 }
            int r2 = r9.getColumnIndex(r1)     // Catch:{ all -> 0x0093 }
            long r2 = r9.getLong(r2)     // Catch:{ all -> 0x0093 }
            java.lang.Long r2 = java.lang.Long.valueOf(r2)     // Catch:{ all -> 0x0093 }
            r5.put(r1, r2)     // Catch:{ all -> 0x0093 }
            int r1 = r9.getColumnIndex(r0)     // Catch:{ all -> 0x0093 }
            int r1 = r9.getInt(r1)     // Catch:{ all -> 0x0093 }
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)     // Catch:{ all -> 0x0093 }
            r5.put(r0, r1)     // Catch:{ all -> 0x0093 }
            java.lang.String r0 = "_id"
            int r0 = r9.getColumnIndex(r0)     // Catch:{ all -> 0x0093 }
            int r0 = r9.getInt(r0)     // Catch:{ all -> 0x0093 }
            com.sec.internal.ims.cmstore.adapters.CloudMessageRCSStorageAdapter r8 = r8.mRCSStorage     // Catch:{ all -> 0x0093 }
            int r8 = r8.updateMessageFromBufferDb(r0, r5)     // Catch:{ all -> 0x0093 }
            goto L_0x009e
        L_0x0093:
            r8 = move-exception
            r9.close()     // Catch:{ all -> 0x0098 }
            goto L_0x009c
        L_0x0098:
            r9 = move-exception
            r8.addSuppressed(r9)
        L_0x009c:
            throw r8
        L_0x009d:
            r8 = 0
        L_0x009e:
            if (r9 == 0) goto L_0x00a3
            r9.close()
        L_0x00a3:
            return r8
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder.queryBufferDbandUpdateRcsMessageDb(java.lang.String):int");
    }

    public long insertRCSimdnToBufferDBUsingObject(ParamOMAObject paramOMAObject) {
        int insertRCSNotificationDbfromBufferDB;
        String str = this.TAG;
        Log.i(str, "insertRCSimdnToBufferDBUsingObject: " + paramOMAObject);
        ContentValues contentValues = new ContentValues();
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.LASTMODSEQ, paramOMAObject.lastModSeq);
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.RES_URL, Util.decodeUrlFromServer(paramOMAObject.resourceURL.toString()));
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.PARENTFOLDER, Util.decodeUrlFromServer(paramOMAObject.parentFolder.toString()));
        if (!TextUtils.isEmpty(paramOMAObject.path)) {
            contentValues.put("path", Util.decodeUrlFromServer(paramOMAObject.path));
        }
        contentValues.put("imdn_id", paramOMAObject.DISPOSITION_ORIGINAL_MESSAGEID);
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.None.getId()));
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.Done.getId()));
        if ("IN".equalsIgnoreCase(paramOMAObject.DIRECTION)) {
            contentValues.put(ImContract.MessageNotification.SENDER_URI, Util.getTelUri(Util.getPhoneNum(paramOMAObject.DISPOSITION_ORIGINAL_TO), this.mCountryCode));
        }
        if ("displayed".equalsIgnoreCase(paramOMAObject.DISPOSITION_STATUS)) {
            contentValues.put("status", Integer.valueOf(NotificationStatus.DISPLAYED.getId()));
        } else {
            contentValues.put("status", Integer.valueOf(NotificationStatus.DELIVERED.getId()));
        }
        if (this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isStoreImdnEnabled() && (insertRCSNotificationDbfromBufferDB = insertRCSNotificationDbfromBufferDB(new ContentValues(contentValues))) > 0) {
            contentValues.put("id", Integer.valueOf(insertRCSNotificationDbfromBufferDB));
        }
        contentValues.put("sim_imsi", this.IMSI);
        return insertTable(13, contentValues);
    }

    public int updateRCSNotificationUsingImdnId(String str, ContentValues contentValues, String str2) {
        ContentValues removeExtensionColumns = removeExtensionColumns(contentValues, false);
        if (removeExtensionColumns.size() > 0) {
            return this.mRCSStorage.updateRCSNotificationUsingImdnId(str, removeExtensionColumns, str2);
        }
        return 0;
    }

    /* JADX WARNING: Removed duplicated region for block: B:102:0x031c  */
    /* JADX WARNING: Removed duplicated region for block: B:105:0x0327  */
    /* JADX WARNING: Removed duplicated region for block: B:106:0x0329  */
    /* JADX WARNING: Removed duplicated region for block: B:111:0x0356  */
    /* JADX WARNING: Removed duplicated region for block: B:120:0x0388  */
    /* JADX WARNING: Removed duplicated region for block: B:123:0x039a  */
    /* JADX WARNING: Removed duplicated region for block: B:127:0x03a8  */
    /* JADX WARNING: Removed duplicated region for block: B:151:0x0471  */
    /* JADX WARNING: Removed duplicated region for block: B:155:0x047e  */
    /* JADX WARNING: Removed duplicated region for block: B:162:0x04f0  */
    /* JADX WARNING: Removed duplicated region for block: B:163:0x052c  */
    /* JADX WARNING: Removed duplicated region for block: B:86:0x0261  */
    /* JADX WARNING: Removed duplicated region for block: B:87:0x0271  */
    /* JADX WARNING: Removed duplicated region for block: B:92:0x02bd  */
    /* JADX WARNING: Removed duplicated region for block: B:93:0x02d2  */
    /* JADX WARNING: Removed duplicated region for block: B:96:0x02e3  */
    /* JADX WARNING: Removed duplicated region for block: B:99:0x0312  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet insertRCSMessageToBufferDBUsingObject(com.sec.internal.ims.cmstore.params.ParamOMAObject r22, java.lang.String r23, boolean r24) {
        /*
            r21 = this;
            r0 = r21
            r1 = r22
            r2 = r23
            r3 = r24
            java.lang.String r4 = r0.TAG
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r6 = "insertRCSMessageToBufferDBUsingObject: "
            r5.append(r6)
            r5.append(r1)
            java.lang.String r6 = " chatid: "
            r5.append(r6)
            r5.append(r2)
            java.lang.String r6 = " isInitialSync: "
            r5.append(r6)
            r5.append(r3)
            java.lang.String r5 = r5.toString()
            android.util.Log.i(r4, r5)
            com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet r4 = new com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r5 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.Done
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r6 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.None
            r4.<init>(r5, r6)
            r7 = -1
            r4.mBufferId = r7
            android.content.ContentValues r7 = new android.content.ContentValues
            r7.<init>()
            android.content.ContentValues r8 = new android.content.ContentValues
            r8.<init>()
            java.lang.String r9 = r1.P_ASSERTED_SERVICE
            r10 = 1
            r11 = 0
            if (r9 == 0) goto L_0x0055
            java.lang.String r12 = "group"
            boolean r9 = r9.contains(r12)
            if (r9 == 0) goto L_0x0055
            r9 = r10
            goto L_0x0056
        L_0x0055:
            r9 = r11
        L_0x0056:
            java.util.Set<com.sec.ims.util.ImsUri> r12 = r1.mNomalizedOtherParticipants
            if (r12 == 0) goto L_0x0567
            boolean r12 = r12.isEmpty()
            if (r12 != 0) goto L_0x0567
            java.lang.String r12 = r1.CONVERSATION_ID
            if (r12 != 0) goto L_0x0066
            goto L_0x0567
        L_0x0066:
            com.sec.internal.omanetapi.nms.data.PayloadPartInfo[] r12 = r1.payloadPart
            if (r12 == 0) goto L_0x0075
            int r12 = r12.length
            if (r12 > 0) goto L_0x0075
            java.lang.String r0 = r0.TAG
            java.lang.String r1 = "insertRCSMessageToBufferDBUsingObject, invalid payloadPart"
            android.util.Log.e(r0, r1)
            return r4
        L_0x0075:
            java.lang.String r4 = "correlation_id"
            java.lang.String r12 = r1.correlationId
            r7.put(r4, r12)
            java.lang.String r4 = "correlation_tag"
            java.lang.String r12 = r1.correlationTag
            r7.put(r4, r12)
            java.lang.String r4 = "lastmodseq"
            java.lang.Long r12 = r1.lastModSeq
            r7.put(r4, r12)
            java.net.URL r4 = r1.resourceURL
            java.lang.String r4 = r4.toString()
            java.lang.String r4 = com.sec.internal.ims.cmstore.utils.Util.decodeUrlFromServer(r4)
            java.lang.String r12 = "res_url"
            r7.put(r12, r4)
            java.net.URL r4 = r1.parentFolder
            java.lang.String r4 = r4.toString()
            java.lang.String r4 = com.sec.internal.ims.cmstore.utils.Util.decodeUrlFromServer(r4)
            java.lang.String r12 = "parentfolder"
            r7.put(r12, r4)
            java.net.URL r4 = r1.payloadURL
            if (r4 == 0) goto L_0x00b6
            java.lang.String r12 = "payloadurl"
            java.lang.String r4 = r4.toString()
            r7.put(r12, r4)
        L_0x00b6:
            com.sec.internal.ims.cmstore.MessageStoreClient r4 = r0.mStoreClient
            com.sec.internal.ims.cmstore.strategy.CloudMessageStrategyManager r4 = r4.getCloudMessageStrategyManager()
            com.sec.internal.interfaces.ims.cmstore.ICloudMessageStrategy r4 = r4.getStrategy()
            boolean r4 = r4.isNmsEventHasMessageDetail()
            if (r4 != 0) goto L_0x00d9
            java.lang.String r4 = r1.path
            boolean r4 = android.text.TextUtils.isEmpty(r4)
            if (r4 != 0) goto L_0x00d9
            java.lang.String r4 = r1.path
            java.lang.String r4 = com.sec.internal.ims.cmstore.utils.Util.decodeUrlFromServer(r4)
            java.lang.String r12 = "path"
            r7.put(r12, r4)
        L_0x00d9:
            int r4 = r1.mObjectType
            r12 = 12
            if (r4 != r12) goto L_0x00e1
            r13 = r10
            goto L_0x00e2
        L_0x00e1:
            r13 = r11
        L_0x00e2:
            r14 = 14
            if (r4 != r14) goto L_0x00f5
            java.net.URL r4 = r1.payloadURL
            if (r4 != 0) goto L_0x00f4
            com.sec.internal.omanetapi.nms.data.PayloadPartInfo[] r4 = r1.payloadPart
            if (r4 == 0) goto L_0x00f2
            int r4 = r4.length
            if (r4 <= 0) goto L_0x00f2
            goto L_0x00f4
        L_0x00f2:
            r13 = r11
            goto L_0x00f5
        L_0x00f4:
            r13 = r10
        L_0x00f5:
            java.lang.Integer r4 = java.lang.Integer.valueOf(r13)
            java.lang.String r15 = "is_filetransfer"
            r7.put(r15, r4)
            boolean r4 = r0.isCmsEnabled
            if (r4 == 0) goto L_0x010e
            if (r13 == 0) goto L_0x010e
            java.lang.String r4 = "transfer_mech"
            java.lang.Integer r15 = java.lang.Integer.valueOf(r10)
            r7.put(r4, r15)
        L_0x010e:
            java.lang.String r4 = r1.DIRECTION
            java.lang.String r15 = "IN"
            boolean r4 = r15.equalsIgnoreCase(r4)
            if (r4 == 0) goto L_0x011f
            com.sec.internal.constants.ims.servicemodules.im.ImDirection r4 = com.sec.internal.constants.ims.servicemodules.im.ImDirection.INCOMING
            int r4 = r4.getId()
            goto L_0x0125
        L_0x011f:
            com.sec.internal.constants.ims.servicemodules.im.ImDirection r4 = com.sec.internal.constants.ims.servicemodules.im.ImDirection.OUTGOING
            int r4 = r4.getId()
        L_0x0125:
            java.lang.Integer r4 = java.lang.Integer.valueOf(r4)
            java.lang.String r14 = "direction"
            r7.put(r14, r4)
            java.lang.String r4 = "chat_id"
            r7.put(r4, r2)
            java.lang.String r4 = r1.DIRECTION
            boolean r4 = r15.equalsIgnoreCase(r4)
            java.lang.String r14 = "remote_uri"
            if (r4 == 0) goto L_0x0144
            java.lang.String r4 = r1.FROM
            r7.put(r14, r4)
            goto L_0x0162
        L_0x0144:
            java.util.ArrayList<java.lang.String> r4 = r1.TO
            int r4 = r4.size()
            if (r4 != r10) goto L_0x0159
            if (r9 != 0) goto L_0x0159
            java.util.ArrayList<java.lang.String> r4 = r1.TO
            java.lang.Object r4 = r4.get(r11)
            java.lang.String r4 = (java.lang.String) r4
            r7.put(r14, r4)
        L_0x0159:
            if (r3 != 0) goto L_0x0162
            java.lang.String r4 = "creator"
            java.lang.String r9 = "SD"
            r7.put(r4, r9)
        L_0x0162:
            java.lang.String r4 = r1.MULTIPARTCONTENTTYPE
            boolean r4 = android.text.TextUtils.isEmpty(r4)
            java.lang.String r9 = "content_type"
            if (r4 != 0) goto L_0x0172
            java.lang.String r4 = r1.MULTIPARTCONTENTTYPE
            r7.put(r9, r4)
            goto L_0x017f
        L_0x0172:
            java.lang.String r4 = r1.CONTENT_TYPE
            boolean r4 = android.text.TextUtils.isEmpty(r4)
            if (r4 != 0) goto L_0x017f
            java.lang.String r4 = r1.CONTENT_TYPE
            r7.put(r9, r4)
        L_0x017f:
            java.lang.String r4 = r1.TEXT_CONTENT
            java.lang.String r14 = r1.CONTENT_TYPE
            boolean r14 = android.text.TextUtils.isEmpty(r14)
            if (r14 != 0) goto L_0x01f3
            java.lang.String r14 = r1.TEXT_CONTENT
            boolean r14 = android.text.TextUtils.isEmpty(r14)
            if (r14 != 0) goto L_0x01b2
            java.lang.String r14 = r1.CONTENT_TYPE
            boolean r14 = r0.isContentTypeDefined(r14)
            if (r14 == 0) goto L_0x01b2
            java.lang.String r14 = r1.CONTENT_TYPE
            java.lang.String r14 = r0.getFileExtension(r14)
            java.lang.String r11 = "txt"
            boolean r11 = r11.equals(r14)
            if (r11 == 0) goto L_0x01b0
            java.lang.String r11 = r0.TAG
            java.lang.String r14 = "no change, just save as txt"
            android.util.Log.d(r11, r14)
            goto L_0x01f3
        L_0x01b0:
            r11 = r10
            goto L_0x01f4
        L_0x01b2:
            java.lang.String r11 = r1.CONTENT_TYPE
            boolean r11 = com.sec.internal.ims.cmstore.utils.Util.isLocationPushContentType(r11)
            if (r11 == 0) goto L_0x01e0
            com.google.gson.Gson r4 = new com.google.gson.Gson
            r4.<init>()
            java.lang.String r11 = r1.MESSAGEBODY
            java.lang.Class<com.sec.internal.omanetapi.nms.data.GeoLocation> r14 = com.sec.internal.omanetapi.nms.data.GeoLocation.class
            java.lang.Object r4 = r4.fromJson(r11, r14)
            com.sec.internal.omanetapi.nms.data.GeoLocation r4 = (com.sec.internal.omanetapi.nms.data.GeoLocation) r4
            java.lang.String r11 = r1.CONVERSATION_ID
            java.lang.String r4 = com.sec.internal.ims.servicemodules.gls.GlsModule.generateXML(r11, r4)
            com.sec.internal.ims.servicemodules.gls.GlsXmlParser r11 = new com.sec.internal.ims.servicemodules.gls.GlsXmlParser
            r11.<init>()
            java.lang.String r11 = r11.getGlsExtInfo(r4)
            java.lang.String r14 = "ext_info"
            r7.put(r14, r11)
        L_0x01dd:
            r14 = r10
            r11 = 0
            goto L_0x01f5
        L_0x01e0:
            java.lang.String r11 = r1.CONTENT_TYPE
            boolean r11 = com.sec.internal.ims.cmstore.utils.Util.isBotMessageContentType(r11)
            if (r11 != 0) goto L_0x01f0
            java.lang.String r11 = r1.CONTENT_TYPE
            boolean r11 = com.sec.internal.ims.cmstore.utils.Util.isBotResponseMessageContentType(r11)
            if (r11 == 0) goto L_0x01f3
        L_0x01f0:
            java.lang.String r4 = r1.MESSAGEBODY
            goto L_0x01dd
        L_0x01f3:
            r11 = 0
        L_0x01f4:
            r14 = 0
        L_0x01f5:
            java.lang.String r10 = r1.DATE
            long r18 = r0.getDateFromDateString(r10)
            java.lang.Long r10 = java.lang.Long.valueOf(r18)
            java.lang.String r12 = "inserted_timestamp"
            r7.put(r12, r10)
            java.lang.String r10 = "body"
            r7.put(r10, r4)
            com.sec.internal.constants.ims.servicemodules.im.NotificationStatus r4 = com.sec.internal.constants.ims.servicemodules.im.NotificationStatus.DELIVERED
            com.sec.internal.constants.ims.servicemodules.im.NotificationStatus r10 = com.sec.internal.constants.ims.servicemodules.im.NotificationStatus.DISPLAYED
            java.util.EnumSet r4 = java.util.EnumSet.of(r4, r10)
            int r4 = com.sec.internal.constants.ims.servicemodules.im.NotificationStatus.encode(r4)
            java.lang.String r10 = "notification_disposition_mask"
            java.lang.Integer r4 = java.lang.Integer.valueOf(r4)
            r7.put(r10, r4)
            com.sec.internal.constants.ims.servicemodules.im.NotificationStatus r4 = com.sec.internal.constants.ims.servicemodules.im.NotificationStatus.NONE
            int r10 = r4.getId()
            java.lang.Integer r10 = java.lang.Integer.valueOf(r10)
            java.lang.String r12 = "notification_status"
            r7.put(r12, r10)
            int r4 = r4.getId()
            java.lang.Integer r4 = java.lang.Integer.valueOf(r4)
            java.lang.String r10 = "disposition_notification_status"
            r7.put(r10, r4)
            java.util.Set<com.sec.ims.util.ImsUri> r4 = r1.mNomalizedOtherParticipants
            int r4 = r4.size()
            java.lang.Integer r4 = java.lang.Integer.valueOf(r4)
            java.lang.String r10 = "not_displayed_counter"
            r7.put(r10, r4)
            java.lang.String r4 = r1.DATE
            long r19 = r0.getDateFromDateString(r4)
            java.lang.Long r4 = java.lang.Long.valueOf(r19)
            java.lang.String r10 = "sent_timestamp"
            r7.put(r10, r4)
            java.lang.String r4 = r1.DIRECTION
            boolean r4 = r15.equalsIgnoreCase(r4)
            if (r4 == 0) goto L_0x0271
            java.lang.String r2 = r1.DATE
            long r19 = r0.getDateFromDateString(r2)
            java.lang.Long r2 = java.lang.Long.valueOf(r19)
            java.lang.String r4 = "delivered_timestamp"
            r7.put(r4, r2)
            goto L_0x0278
        L_0x0271:
            boolean r4 = r0.isCmsEnabled
            if (r4 == 0) goto L_0x0278
            r0.insertRCSImdnToBufferDBUsingObject(r1, r2, r7)
        L_0x0278:
            com.sec.internal.omanetapi.nms.data.FlagList r2 = r1.mFlagList
            java.lang.String r4 = r1.DIRECTION
            int r2 = r0.updateMessageStatus(r2, r4)
            java.lang.String r4 = r0.TAG
            java.lang.StringBuilder r10 = new java.lang.StringBuilder
            r10.<init>()
            java.lang.String r12 = "msgStatus: "
            r10.append(r12)
            r10.append(r2)
            java.lang.String r10 = r10.toString()
            com.sec.internal.log.IMSLog.i(r4, r10)
            java.lang.String r4 = "status"
            java.lang.Integer r10 = java.lang.Integer.valueOf(r2)
            r7.put(r4, r10)
            java.lang.String r4 = "ft_status"
            java.lang.Integer r2 = java.lang.Integer.valueOf(r2)
            r7.put(r4, r2)
            com.sec.internal.omanetapi.nms.data.FlagList r2 = r1.mFlagList
            boolean r2 = r0.getIfCancelUsingFlag(r2)
            int r4 = r1.mObjectType
            r10 = 3
            java.lang.String r12 = "state"
            java.lang.String r15 = "message_type"
            r19 = r8
            r8 = 12
            if (r4 != r8) goto L_0x02d2
            java.lang.Integer r4 = java.lang.Integer.valueOf(r10)
            r7.put(r12, r4)
            com.sec.internal.constants.ims.servicemodules.im.ImConstants$Type r4 = com.sec.internal.constants.ims.servicemodules.im.ImConstants.Type.MULTIMEDIA
            int r4 = r4.getId()
            java.lang.Integer r4 = java.lang.Integer.valueOf(r4)
            r7.put(r15, r4)
            goto L_0x02df
        L_0x02d2:
            com.sec.internal.constants.ims.servicemodules.im.ImConstants$Type r4 = com.sec.internal.constants.ims.servicemodules.im.ImConstants.Type.TEXT
            int r4 = r4.getId()
            java.lang.Integer r4 = java.lang.Integer.valueOf(r4)
            r7.put(r15, r4)
        L_0x02df:
            java.lang.String r4 = r1.EXTENDEDRCS
            if (r4 == 0) goto L_0x030e
            com.google.gson.Gson r4 = new com.google.gson.Gson
            r4.<init>()
            java.lang.String r8 = r1.EXTENDEDRCS
            java.lang.Class<com.sec.internal.omanetapi.nms.data.ExtendedRCS> r10 = com.sec.internal.omanetapi.nms.data.ExtendedRCS.class
            java.lang.Object r4 = r4.fromJson(r8, r10)
            com.sec.internal.omanetapi.nms.data.ExtendedRCS r4 = (com.sec.internal.omanetapi.nms.data.ExtendedRCS) r4
            java.lang.String r8 = r4.mReferenceId
            java.lang.String r10 = "reference_id"
            r7.put(r10, r8)
            int r8 = r4.mReferenceType
            java.lang.Integer r8 = java.lang.Integer.valueOf(r8)
            java.lang.String r10 = "reference_type"
            r7.put(r10, r8)
            java.lang.String r8 = "reference_value"
            java.lang.String r4 = r4.mReferenceValue
            r7.put(r8, r4)
        L_0x030e:
            java.lang.String r4 = r1.CHIPLIST
            if (r4 == 0) goto L_0x0318
            java.lang.String r8 = "suggestion"
            r7.put(r8, r4)
        L_0x0318:
            java.lang.String r4 = r1.TRAFFICTYPE
            if (r4 == 0) goto L_0x0321
            java.lang.String r8 = "maap_traffic_type"
            r7.put(r8, r4)
        L_0x0321:
            int r4 = r1.mObjectType
            r8 = 14
            if (r4 != r8) goto L_0x0329
            r4 = 1
            goto L_0x032a
        L_0x0329:
            r4 = 0
        L_0x032a:
            java.lang.Integer r4 = java.lang.Integer.valueOf(r4)
            java.lang.String r8 = "message_isslm"
            r7.put(r8, r4)
            java.lang.String r4 = "imdn_message_id"
            java.lang.String r8 = r1.correlationId
            r7.put(r4, r8)
            java.lang.String r4 = "imdn_original_to"
            java.lang.String r8 = r1.DISPOSITION_ORIGINAL_TO
            r7.put(r4, r8)
            java.lang.String r4 = "conversation_id"
            java.lang.String r8 = r1.CONVERSATION_ID
            r7.put(r4, r8)
            java.lang.String r4 = "contribution_id"
            java.lang.String r8 = r1.CONTRIBUTION_ID
            r7.put(r4, r8)
            com.sec.internal.omanetapi.nms.data.PayloadPartInfo[] r4 = r1.payloadPart
            if (r4 == 0) goto L_0x0388
            int r4 = r4.length
            if (r4 <= 0) goto L_0x0388
            java.util.ArrayList r4 = new java.util.ArrayList
            r4.<init>()
            com.sec.internal.omanetapi.nms.data.PayloadPartInfo[] r8 = r1.payloadPart
            int r10 = r8.length
            r16 = r4
            r4 = 1
            if (r10 <= r4) goto L_0x036a
            java.lang.String r4 = "application/vnd.gsma.botsuggestion.v1.0+json"
            java.util.ArrayList r4 = r0.getValidPayload(r8, r4)
            goto L_0x036c
        L_0x036a:
            r4 = r16
        L_0x036c:
            int r8 = r4.size()
            if (r8 != 0) goto L_0x0382
            java.lang.String r8 = r0.TAG
            java.lang.String r10 = "no visible payload!"
            android.util.Log.d(r8, r10)
            com.sec.internal.omanetapi.nms.data.PayloadPartInfo[] r8 = r1.payloadPart
            r10 = 0
            r8 = r8[r10]
            r4.add(r8)
            goto L_0x0383
        L_0x0382:
            r10 = 0
        L_0x0383:
            android.content.ContentValues r8 = r0.handlePayloadWithThumbnail(r4, r1)
            goto L_0x038b
        L_0x0388:
            r10 = 0
            r8 = r19
        L_0x038b:
            com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet r4 = new com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r10 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice
            r16 = r15
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r15 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Insert
            r4.<init>(r10, r15)
            boolean r10 = r1.mIsGoforwardSync
            if (r10 == 0) goto L_0x03a8
            boolean r2 = r1.mReassembled
            if (r2 != 0) goto L_0x03a2
            r4.mAction = r6
            r4.mDirection = r5
        L_0x03a2:
            r18 = r5
            r19 = r6
            goto L_0x047c
        L_0x03a8:
            java.lang.String r10 = "thumbnail_path"
            boolean r15 = r8.containsKey(r10)
            if (r15 == 0) goto L_0x03cd
            java.lang.String r2 = r0.TAG
            java.lang.StringBuilder r9 = new java.lang.StringBuilder
            r9.<init>()
            java.lang.String r13 = "saves valid thumbnail: from downloaded object: "
            r9.append(r13)
            java.lang.String r10 = r8.getAsString(r10)
            r9.append(r10)
            java.lang.String r9 = r9.toString()
            android.util.Log.d(r2, r9)
            goto L_0x03a2
        L_0x03cd:
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r10 = r1.mFlag
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r15 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Delete
            boolean r10 = r10.equals(r15)
            if (r10 == 0) goto L_0x03de
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r2 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Deleted
            r4.mAction = r2
            r4.mDirection = r5
            goto L_0x03a2
        L_0x03de:
            java.lang.String r10 = r0.TAG
            java.lang.StringBuilder r15 = new java.lang.StringBuilder
            r15.<init>()
            r18 = r5
            java.lang.String r5 = "isCancelStatus: "
            r15.append(r5)
            r15.append(r2)
            java.lang.String r5 = ", read flag present: "
            r15.append(r5)
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r5 = r1.mFlag
            r19 = r6
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r6 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Update
            boolean r5 = r5.equals(r6)
            r15.append(r5)
            java.lang.String r5 = ", nonTextChatType: "
            r15.append(r5)
            r15.append(r14)
            java.lang.String r5 = r15.toString()
            com.sec.internal.log.IMSLog.i(r10, r5)
            java.lang.String r5 = r1.TEXT_CONTENT
            boolean r5 = android.text.TextUtils.isEmpty(r5)
            if (r5 == 0) goto L_0x047c
            if (r14 != 0) goto L_0x047c
            if (r2 != 0) goto L_0x047c
            boolean r2 = r8.containsKey(r9)
            if (r2 == 0) goto L_0x046d
            java.lang.String r2 = r8.getAsString(r9)
            java.lang.String r5 = r0.TAG
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            java.lang.String r9 = "content: "
            r6.append(r9)
            r6.append(r2)
            java.lang.String r9 = ", isFT: "
            r6.append(r9)
            r6.append(r13)
            java.lang.String r6 = r6.toString()
            android.util.Log.i(r5, r6)
            if (r2 == 0) goto L_0x046d
            if (r13 != 0) goto L_0x046d
            java.lang.String r5 = "xml"
            boolean r5 = r2.endsWith(r5)
            if (r5 != 0) goto L_0x0462
            java.lang.String r5 = "text/plain"
            boolean r5 = r2.contains(r5)
            if (r5 != 0) goto L_0x0462
            java.lang.String r5 = "json"
            boolean r2 = r2.endsWith(r5)
            if (r2 == 0) goto L_0x046d
        L_0x0462:
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r2 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.DownLoad
            r4.mAction = r2
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r2 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.Downloading
            r4.mDirection = r2
            r17 = 1
            goto L_0x046f
        L_0x046d:
            r17 = 0
        L_0x046f:
            if (r17 != 0) goto L_0x047c
            if (r3 == 0) goto L_0x0478
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r2 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.FetchUri
            r4.mAction = r2
            goto L_0x047c
        L_0x0478:
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r2 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.FetchIndividualUri
            r4.mAction = r2
        L_0x047c:
            if (r11 == 0) goto L_0x0495
            r2 = 3
            java.lang.Integer r2 = java.lang.Integer.valueOf(r2)
            r7.put(r12, r2)
            com.sec.internal.constants.ims.servicemodules.im.ImConstants$Type r2 = com.sec.internal.constants.ims.servicemodules.im.ImConstants.Type.MULTIMEDIA
            int r2 = r2.getId()
            java.lang.Integer r2 = java.lang.Integer.valueOf(r2)
            r5 = r16
            r7.put(r5, r2)
        L_0x0495:
            java.net.URL r2 = r1.resourceURL
            java.lang.String r2 = r2.toString()
            java.lang.String r2 = com.sec.internal.ims.cmstore.utils.Util.getLineTelUriFromObjUrl(r2)
            java.lang.String r5 = "linenum"
            r7.put(r5, r2)
            r7.putAll(r8)
            android.content.Context r2 = r0.mContext
            java.lang.String r5 = "phone"
            java.lang.Object r2 = r2.getSystemService(r5)
            android.telephony.TelephonyManager r2 = (android.telephony.TelephonyManager) r2
            java.lang.String r2 = r2.getSubscriberId()
            java.lang.String r5 = r0.TAG
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            java.lang.String r8 = "sim imsi : "
            r6.append(r8)
            r6.append(r2)
            java.lang.String r2 = r6.toString()
            android.util.Log.d(r5, r2)
            com.sec.internal.ims.cmstore.MessageStoreClient r2 = r0.mStoreClient
            java.lang.String r2 = r2.getCurrentIMSI()
            java.lang.String r5 = "sim_imsi"
            r7.put(r5, r2)
            java.lang.String r2 = "syncdirection"
            java.lang.String r5 = "syncaction"
            if (r3 != 0) goto L_0x052c
            boolean r3 = r1.mIsFromChangedObj
            if (r3 != 0) goto L_0x052c
            java.lang.String r1 = r1.DATE
            com.sec.internal.ims.cmstore.MessageStoreClient r3 = r0.mStoreClient
            r6 = 1
            boolean r1 = com.sec.internal.ims.cmstore.utils.Util.isDownloadObject(r1, r3, r6)
            if (r1 == 0) goto L_0x04f0
            goto L_0x052c
        L_0x04f0:
            int r1 = r19.getId()
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)
            r7.put(r5, r1)
            int r1 = r18.getId()
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)
            r7.put(r2, r1)
            r1 = r19
            r4.mAction = r1
            r1 = r18
            r4.mDirection = r1
            java.lang.String r1 = r0.TAG
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "normal sync insert RCS db: set action as: "
            r2.append(r3)
            r2.append(r4)
            java.lang.String r2 = r2.toString()
            android.util.Log.i(r1, r2)
            r1 = 1
            long r0 = r0.insertTable(r1, r7)
            r4.mBufferId = r0
            goto L_0x0566
        L_0x052c:
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r1 = r4.mAction
            int r1 = r1.getId()
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)
            r7.put(r5, r1)
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r1 = r4.mDirection
            int r1 = r1.getId()
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)
            r7.put(r2, r1)
            java.lang.String r1 = r0.TAG
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "initial sync insert RCS db or normal sync from extended changed object: "
            r2.append(r3)
            r2.append(r4)
            java.lang.String r2 = r2.toString()
            android.util.Log.d(r1, r2)
            r1 = 1
            long r1 = r0.insertTable(r1, r7)
            r4.mBufferId = r1
            r0.insertRCSMessageDbfromBufferDB(r1, r7)
        L_0x0566:
            return r4
        L_0x0567:
            java.lang.String r0 = r0.TAG
            java.lang.String r1 = "insertRCSMessageToBufferDBUsingObject, invalid OMA param issue"
            android.util.Log.e(r0, r1)
            return r4
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder.insertRCSMessageToBufferDBUsingObject(com.sec.internal.ims.cmstore.params.ParamOMAObject, java.lang.String, boolean):com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet");
    }

    public int updateRCSMessageInBufferDBUsingObject(ParamOMAObject paramOMAObject, ContentValues contentValues, String str, String[] strArr) {
        ContentValues contentValues2 = new ContentValues();
        PayloadPartInfo[] payloadPartInfoArr = paramOMAObject.payloadPart;
        if (payloadPartInfoArr != null && payloadPartInfoArr.length > 0) {
            ArrayList<PayloadPartInfo> arrayList = new ArrayList<>();
            PayloadPartInfo[] payloadPartInfoArr2 = paramOMAObject.payloadPart;
            if (payloadPartInfoArr2.length > 1) {
                arrayList = getValidPayload(payloadPartInfoArr2, MIMEContentType.BOT_SUGGESTION);
            }
            if (arrayList != null && arrayList.size() == 0) {
                Log.d(this.TAG, "no visible payload!");
                arrayList.add(paramOMAObject.payloadPart[0]);
            }
            contentValues2 = handlePayloadWithThumbnail(arrayList, paramOMAObject);
        }
        contentValues.putAll(contentValues2);
        return updateTable(1, contentValues, str, strArr);
    }

    /* access modifiers changed from: protected */
    public ArrayList<PayloadPartInfo> getValidPayload(PayloadPartInfo[] payloadPartInfoArr, String str) {
        if (payloadPartInfoArr == null) {
            return null;
        }
        ArrayList<PayloadPartInfo> arrayList = new ArrayList<>();
        for (PayloadPartInfo payloadPartInfo : payloadPartInfoArr) {
            if (payloadPartInfo != null && !payloadPartInfo.contentType.toUpperCase().contains(str.toUpperCase())) {
                arrayList.add(payloadPartInfo);
            }
        }
        return arrayList;
    }

    public ContentValues handlePayloadParts(PayloadPartInfo[] payloadPartInfoArr, String str) {
        ContentValues contentValues = new ContentValues();
        if (payloadPartInfoArr == null) {
            return contentValues;
        }
        if (str.contains(VcidEvent.BUNDLE_VALUE_ACTION_START)) {
            for (PayloadPartInfo payloadPartInfo : payloadPartInfoArr) {
                String str2 = payloadPartInfo.contentId;
                String str3 = null;
                if (str2 == null || payloadPartInfo.contentEncoding == null || !str.contains(str2) || !HttpPostBody.CONTENT_TRANSFER_ENCODING_BASE64.equalsIgnoreCase(payloadPartInfo.contentEncoding)) {
                    String str4 = payloadPartInfo.contentType;
                    if (str4 != null && isContentTypeDefined(str4)) {
                        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.PAYLOADENCODING, Integer.valueOf(translatePayloadEncoding(payloadPartInfo.contentEncoding).getId()));
                        URL url = payloadPartInfo.href;
                        if (url != null) {
                            str3 = url.toString();
                        }
                        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.PAYLOADPARTFULL, str3);
                        String randomFileName = Util.getRandomFileName(getFileExtension(payloadPartInfo.contentType));
                        contentValues.put("content_type", payloadPartInfo.contentType);
                        contentValues.put(ImContract.CsSession.FILE_NAME, randomFileName);
                    }
                } else {
                    try {
                        URL url2 = payloadPartInfo.href;
                        if (url2 != null) {
                            str3 = url2.toString();
                        }
                        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.PAYLOADPARTTHUMB, str3);
                        byte[] decode = Base64.decode(payloadPartInfo.content, 0);
                        String randomFileName2 = Util.getRandomFileName(getFileExtension(payloadPartInfo.contentType));
                        String generateUniqueFilePath = Util.generateUniqueFilePath(this.mContext, randomFileName2, false, this.mStoreClient.getClientID());
                        Util.saveFiletoPath(decode, generateUniqueFilePath);
                        contentValues.put("content_type", payloadPartInfo.contentType);
                        contentValues.put(ImContract.CsSession.THUMBNAIL_PATH, generateUniqueFilePath);
                        contentValues.put(ImContract.CsSession.FILE_NAME, randomFileName2);
                    } catch (IOException e) {
                        Log.e(this.TAG, "IOException: " + e.getMessage());
                        e.printStackTrace();
                        return contentValues;
                    } catch (NullPointerException e2) {
                        Log.e(this.TAG, "nullpointer: " + e2.getMessage());
                        e2.printStackTrace();
                        return contentValues;
                    }
                }
            }
        } else {
            for (PayloadPartInfo payloadPartInfo2 : payloadPartInfoArr) {
                if (isContentTypeDefined(payloadPartInfo2.contentType)) {
                    contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.PAYLOADENCODING, Integer.valueOf(translatePayloadEncoding(payloadPartInfo2.contentEncoding).getId()));
                    contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.PAYLOADPARTFULL, payloadPartInfo2.href.toString());
                    String randomFileName3 = Util.getRandomFileName(getFileExtension(payloadPartInfo2.contentType));
                    contentValues.put("content_type", payloadPartInfo2.contentType);
                    contentValues.put(ImContract.CsSession.FILE_NAME, randomFileName3);
                }
            }
        }
        return contentValues;
    }

    public ContentValues handlePayloadWithThumbnail(ArrayList<PayloadPartInfo> arrayList, ParamOMAObject paramOMAObject) {
        ContentValues contentValues = new ContentValues();
        if (arrayList == null) {
            return contentValues;
        }
        if (arrayList.size() > 1) {
            Iterator<PayloadPartInfo> it = arrayList.iterator();
            String str = null;
            while (it.hasNext()) {
                PayloadPartInfo next = it.next();
                URI uri = next.fileIcon;
                if (uri != null) {
                    str = uri.toString();
                    String[] split = str.split(":");
                    if (split != null && split.length > 1) {
                        str = split[1];
                    }
                    Log.d(this.TAG, "fileIconCId : " + str);
                    contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.PAYLOADPARTFULL, next.href.toString());
                    if (ATTGlobalVariables.isAmbsPhaseIV()) {
                        contentValues.put(ImContract.CsSession.FILE_NAME, Util.generateUniqueFileName(next));
                    } else {
                        contentValues.put(ImContract.CsSession.FILE_NAME, Util.generateLocation(next));
                    }
                    contentValues.put(ImContract.CsSession.FILE_SIZE, Long.valueOf(next.size));
                    contentValues.put("content_type", next.contentType.split(";")[0]);
                } else {
                    String str2 = next.contentId;
                    if (!(str2 == null || str == null || !str.equals(str2))) {
                        URL url = next.href;
                        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.PAYLOADPARTTHUMB, url != null ? url.toString() : null);
                        if (ATTGlobalVariables.isAmbsPhaseIV()) {
                            contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.PAYLOADPARTTHUMB_FILENAME, Util.generateUniqueFileName(next));
                        } else {
                            contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.PAYLOADPARTTHUMB_FILENAME, Util.generateLocation(next));
                        }
                    }
                }
            }
        } else {
            Iterator<PayloadPartInfo> it2 = arrayList.iterator();
            while (it2.hasNext()) {
                PayloadPartInfo next2 = it2.next();
                contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.PAYLOADPARTFULL, next2.href.toString());
                contentValues.put(ImContract.CsSession.FILE_NAME, Util.generateLocation(next2));
                contentValues.put(ImContract.CsSession.FILE_SIZE, Long.valueOf(next2.size));
                String[] split2 = next2.contentType.split(";");
                contentValues.put("content_type", split2[0]);
                contentValues.put("file_disposition", Integer.valueOf("render".equalsIgnoreCase(next2.disposition) ? 1 : 0));
                contentValues.put("playing_length", Integer.valueOf(next2.playingLength));
                if (paramOMAObject.mObjectType == 14 && split2[0].trim().equalsIgnoreCase(MIMEContentType.PLAIN_TEXT)) {
                    Log.d(this.TAG, "this message should be large message, not fileTransfer");
                    contentValues.put(ImContract.ChatItem.IS_FILE_TRANSFER, 0);
                }
            }
        }
        return contentValues;
    }

    private ContentValues removeExtensionColumns(ContentValues contentValues, boolean z) {
        contentValues.remove(CloudMessageProviderContract.BufferDBExtensionBase.CORRELATION_ID);
        contentValues.remove(CloudMessageProviderContract.BufferDBExtensionBase.CORRELATION_TAG);
        contentValues.remove(CloudMessageProviderContract.BufferDBExtensionBase.RES_URL);
        contentValues.remove(CloudMessageProviderContract.BufferDBExtensionBase.PARENTFOLDER);
        contentValues.remove(CloudMessageProviderContract.BufferDBExtensionBase.PAYLOADURL);
        contentValues.remove(CloudMessageProviderContract.BufferDBExtensionBase.PAYLOADPARTTHUMB);
        contentValues.remove(CloudMessageProviderContract.BufferDBExtensionBase.PAYLOADPARTTHUMB_FILENAME);
        contentValues.remove(CloudMessageProviderContract.BufferDBExtensionBase.PAYLOADPARTFULL);
        contentValues.remove(CloudMessageProviderContract.BufferDBExtensionBase.PAYLOADENCODING);
        contentValues.remove(CloudMessageProviderContract.BufferDBExtensionBase.FLAGRESOURCEURL);
        contentValues.remove("path");
        contentValues.remove(CloudMessageProviderContract.BufferDBExtensionBase.PARENTFOLDERPATH);
        contentValues.remove(CloudMessageProviderContract.BufferDBExtensionBase.LASTMODSEQ);
        contentValues.remove(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION);
        contentValues.remove(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION);
        contentValues.remove("linenum");
        if (!z) {
            contentValues.remove("sim_imsi");
        }
        return contentValues;
    }

    public void insertRCSMessageDbfromBufferDB(long j, ContentValues contentValues) {
        Uri insertMessageFromBufferDb = this.mRCSStorage.insertMessageFromBufferDb(removeExtensionColumns(contentValues, true));
        if (insertMessageFromBufferDb != null) {
            String str = this.TAG;
            Log.d(str, "insert RCS message into ImProvider result: " + IMSLog.checker(insertMessageFromBufferDb.toString()));
            String lastPathSegment = insertMessageFromBufferDb.getLastPathSegment();
            ContentValues contentValues2 = new ContentValues();
            int intValue = Integer.valueOf(lastPathSegment).intValue();
            if (intValue > 0) {
                contentValues2.put("_id", Integer.valueOf(intValue));
                this.mBufferDB.updateRCSTable(contentValues2, "_bufferdbid=?", new String[]{String.valueOf(j)});
            }
        }
    }

    public int deleteRCSMessageDb(int i) {
        return this.mRCSStorage.deleteRCSDBmessageUsingId(i);
    }

    public int updateRCSMessageDb(int i, ContentValues contentValues) {
        String str = this.TAG;
        Log.d(str, "updateRCSMessageDb: " + i);
        ContentValues removeExtensionColumns = removeExtensionColumns(contentValues, false);
        if (removeExtensionColumns.size() > 0) {
            return this.mRCSStorage.updateMessageFromBufferDb(i, removeExtensionColumns);
        }
        return 0;
    }

    public int insertRCSNotificationDbfromBufferDB(ContentValues contentValues) {
        if (contentValues == null) {
            Log.d(this.TAG, "insertRCSNotificationDbfromBufferDB null input");
            return 0;
        }
        Uri insertNotificationFromBufferDb = this.mRCSStorage.insertNotificationFromBufferDb(removeExtensionColumns(contentValues, false));
        if (insertNotificationFromBufferDb == null) {
            return 0;
        }
        String str = this.TAG;
        Log.d(str, "insert RCS notification into ImProvider result: " + IMSLog.checker(insertNotificationFromBufferDb.toString()));
        return Integer.valueOf(insertNotificationFromBufferDb.getLastPathSegment()).intValue();
    }

    public int updateRCSSessionDb(String str, ContentValues contentValues) {
        ContentValues removeExtensionColumns = removeExtensionColumns(contentValues, false);
        if (removeExtensionColumns.size() > 0) {
            return this.mRCSStorage.updateSessionFromBufferDbToRCSDb(str, removeExtensionColumns);
        }
        return 0;
    }

    public int updateSessionBufferDb(String str, ContentValues contentValues) {
        String str2 = this.TAG;
        Log.i(str2, "updateSessionBufferDb: " + str);
        String[] strArr = {str};
        if (contentValues.size() > 0) {
            return this.mBufferDB.updateRCSSessionTable(contentValues, "chat_id=?", strArr);
        }
        return 0;
    }

    public Cursor queryRCSBufferDBwithResUrl(String str) {
        String str2 = this.TAG;
        Log.d(str2, "queryRCSBufferDBwithResUrl: " + IMSLog.checker(str));
        return this.mBufferDB.queryTablewithResUrl(1, str);
    }

    public int deleteRCSBufferDBwithResUrl(String str) {
        String str2 = this.TAG;
        Log.d(str2, "deleteRCSBufferDBwithResUrl: " + IMSLog.checker(str));
        return this.mBufferDB.deleteTablewithResUrl(1, str);
    }

    public Cursor queryToDeviceUnDownloadedRcs(String str, int i) {
        return this.mBufferDB.queryRCSMessages((String[]) null, "syncaction=? AND linenum=? AND sim_imsi=?", new String[]{String.valueOf(i), str, this.IMSI}, (String) null);
    }

    public Cursor queryToCloudUnsyncedRcs() {
        return this.mBufferDB.queryRCSMessages((String[]) null, "syncdirection=? AND res_url IS NOT NULL AND inserted_timestamp > ? AND sim_imsi=?", new String[]{String.valueOf(CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud.getId()), String.valueOf(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(10)), this.IMSI}, (String) null);
    }

    public Cursor queryToDeviceUnsyncedRcs() {
        return this.mBufferDB.queryRCSMessages((String[]) null, "syncdirection=? AND sim_imsi=?", new String[]{String.valueOf(CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice.getId()), this.IMSI}, (String) null);
    }

    public Cursor querySessionUsingChatId(String str) {
        return this.mRCSStorage.querySessionUsingChatId(str);
    }

    /* JADX WARNING: Removed duplicated region for block: B:29:0x00e2  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void insertToRCSMessagesBufferDBFromTP(android.database.Cursor r9, java.lang.String r10, android.content.ContentValues r11, boolean r12) {
        /*
            r8 = this;
            java.lang.String r0 = r8.TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "insertToRCSMessagesBufferDBFromTP(): "
            r1.append(r2)
            java.lang.String r10 = com.sec.internal.log.IMSLog.checker(r10)
            r1.append(r10)
            java.lang.String r10 = "we do get something from RCS messages: "
            r1.append(r10)
            int r10 = r9.getCount()
            r1.append(r10)
            java.lang.String r10 = r1.toString()
            com.sec.internal.log.IMSLog.s(r0, r10)
            java.util.ArrayList r9 = com.sec.internal.ims.cmstore.utils.CursorContentValueTranslator.convertRCSIMFTFromTPtoCV(r9, r12)
            java.lang.String r10 = r8.TAG
            java.lang.StringBuilder r12 = new java.lang.StringBuilder
            r12.<init>()
            java.lang.String r0 = "insertToRCSMessagesBufferDBFromTP() size: "
            r12.append(r0)
            int r0 = r9.size()
            r12.append(r0)
            java.lang.String r12 = r12.toString()
            android.util.Log.d(r10, r12)
            r10 = 0
        L_0x0046:
            int r12 = r9.size()
            if (r10 >= r12) goto L_0x013b
            java.lang.Object r12 = r9.get(r10)
            android.content.ContentValues r12 = (android.content.ContentValues) r12
            java.lang.String r0 = "remote_uri"
            java.lang.String r0 = r12.getAsString(r0)
            java.lang.String r0 = com.sec.internal.ims.util.PhoneUtils.extractNumberFromUri(r0)
            com.sec.internal.ims.cmstore.MessageStoreClient r1 = r8.mStoreClient
            com.sec.internal.ims.cmstore.strategy.CloudMessageStrategyManager r1 = r1.getCloudMessageStrategyManager()
            com.sec.internal.interfaces.ims.cmstore.ICloudMessageStrategy r1 = r1.getStrategy()
            boolean r1 = r1.isNeedCheckBlockedNumberBeforeCopyRcsDb()
            if (r1 == 0) goto L_0x009c
            boolean r1 = android.text.TextUtils.isEmpty(r0)
            if (r1 != 0) goto L_0x009c
            android.content.Context r1 = r8.mContext
            boolean r1 = android.provider.BlockedNumberContract.isBlocked(r1, r0)
            if (r1 == 0) goto L_0x009c
            java.lang.String r12 = r8.TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "The number ["
            r1.append(r2)
            java.lang.String r0 = com.sec.internal.log.IMSLog.checker(r0)
            r1.append(r0)
            java.lang.String r0 = "] has been add to block list. This message should avoid to save to BuffedDB!"
            r1.append(r0)
            java.lang.String r0 = r1.toString()
            android.util.Log.i(r12, r0)
            goto L_0x0137
        L_0x009c:
            java.lang.String r0 = "sim_imsi"
            java.lang.String r1 = r12.getAsString(r0)
            boolean r1 = android.text.TextUtils.isEmpty(r1)
            if (r1 == 0) goto L_0x00ae
            java.lang.String r1 = r8.IMSI
            r12.put(r0, r1)
        L_0x00ae:
            java.lang.String r0 = "imdn_message_id"
            java.lang.String r0 = r12.getAsString(r0)
            boolean r1 = android.text.TextUtils.isEmpty(r0)
            r2 = -1
            if (r1 != 0) goto L_0x00e6
            com.sec.internal.ims.cmstore.adapters.CloudMessageBufferDBPersister r1 = r8.mBufferDB
            android.database.Cursor r1 = r1.queryRCSMessageUsingImdnId(r0)
            if (r1 == 0) goto L_0x00df
            boolean r4 = r1.moveToFirst()     // Catch:{ all -> 0x00d5 }
            if (r4 == 0) goto L_0x00df
            java.lang.String r4 = "_id"
            int r4 = r1.getColumnIndexOrThrow(r4)     // Catch:{ all -> 0x00d5 }
            long r4 = r1.getLong(r4)     // Catch:{ all -> 0x00d5 }
            goto L_0x00e0
        L_0x00d5:
            r8 = move-exception
            r1.close()     // Catch:{ all -> 0x00da }
            goto L_0x00de
        L_0x00da:
            r9 = move-exception
            r8.addSuppressed(r9)
        L_0x00de:
            throw r8
        L_0x00df:
            r4 = r2
        L_0x00e0:
            if (r1 == 0) goto L_0x00e7
            r1.close()
            goto L_0x00e7
        L_0x00e6:
            r4 = r2
        L_0x00e7:
            java.lang.String r1 = r8.TAG
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            java.lang.String r7 = "insertToRCSMessagesBufferDBFromTP() rowId: "
            r6.append(r7)
            r6.append(r4)
            java.lang.String r7 = " imdnID: "
            r6.append(r7)
            r6.append(r0)
            java.lang.String r6 = r6.toString()
            android.util.Log.i(r1, r6)
            int r1 = (r4 > r2 ? 1 : (r4 == r2 ? 0 : -1))
            if (r1 != 0) goto L_0x0137
            java.lang.String r1 = "recipients"
            java.lang.String r2 = r12.getAsString(r1)
            r12.remove(r1)
            r8.addCVValuesFromSessionCursor(r12, r11)
            com.sec.internal.ims.cmstore.adapters.CloudMessageBufferDBPersister r1 = r8.mBufferDB
            r3 = 1
            r1.insertDeviceMsgToBuffer(r3, r12)
            java.lang.String r1 = "message_type"
            java.lang.Integer r1 = r12.getAsInteger(r1)
            if (r1 == 0) goto L_0x0137
            boolean r3 = android.text.TextUtils.isEmpty(r0)
            if (r3 != 0) goto L_0x0137
            int r1 = r1.intValue()
            boolean r1 = r8.isUpdateRequired(r1)
            if (r1 == 0) goto L_0x0137
            r8.insertToImdnNotificationBufferDBFromTP(r12, r0, r2)
        L_0x0137:
            int r10 = r10 + 1
            goto L_0x0046
        L_0x013b:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder.insertToRCSMessagesBufferDBFromTP(android.database.Cursor, java.lang.String, android.content.ContentValues, boolean):void");
    }

    private boolean isUpdateRequired(int i) {
        return i == McsConstants.RCSMessageType.MULTIMEDIA.getId() || i == McsConstants.RCSMessageType.TEXT.getId() || i == McsConstants.RCSMessageType.SINGLE.getId() || i == McsConstants.RCSMessageType.GROUP.getId();
    }

    private void insertToImdnNotificationBufferDBFromTP(ContentValues contentValues, String str, String str2) {
        Integer asInteger = contentValues.getAsInteger("notification_status");
        if (asInteger != null) {
            Log.i(this.TAG, "insertToImdnNotificationBufferDBFromTP recipients isempty: " + TextUtils.isEmpty(str2) + ", status: " + asInteger);
            Long asLong = contentValues.getAsLong(ImContract.ChatItem.DELIVERED_TIMESTAMP);
            Integer asInteger2 = contentValues.getAsInteger("direction");
            if (asInteger2 != null && asInteger2.intValue() == ImDirection.OUTGOING.getId() && !TextUtils.isEmpty(str2)) {
                ContentValues contentValues2 = new ContentValues();
                String[] split = str2.split(";");
                contentValues2.put("id", contentValues.getAsString("_id"));
                contentValues2.put("imdn_id", str);
                contentValues2.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.ToSendCloud.getId()));
                contentValues2.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Insert.getId()));
                contentValues2.put("status", asInteger);
                for (String insertToImdnNotificationBufferDBHelper : split) {
                    insertToImdnNotificationBufferDBHelper(contentValues2, insertToImdnNotificationBufferDBHelper, asLong, asInteger.intValue());
                }
            }
        }
    }

    private void insertToImdnNotificationBufferDBHelper(ContentValues contentValues, String str, Long l, int i) {
        contentValues.put(ImContract.MessageNotification.SENDER_URI, Util.getTelUri(str, this.mCountryCode));
        if (i == NotificationStatus.NONE.getId()) {
            contentValues.put("timestamp", 0);
        } else if (i == NotificationStatus.DELIVERED.getId()) {
            contentValues.put("timestamp", l);
        } else if (i == NotificationStatus.DISPLAYED.getId()) {
            contentValues.put("timestamp", l);
        }
        contentValues.put("sim_imsi", this.IMSI);
        this.mBufferDB.insertDeviceMsgToBuffer(13, contentValues);
    }

    private void addCVValuesFromSessionCursor(ContentValues contentValues, ContentValues contentValues2) {
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Insert.getId()));
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.ToSendCloud.getId()));
        contentValues.put("chat_id", contentValues2.getAsString("chat_id"));
        contentValues.put("conversation_id", contentValues2.getAsString("conversation_id"));
        contentValues.put("contribution_id", contentValues2.getAsString("contribution_id"));
        contentValues.put("linenum", contentValues2.getAsString("linenum"));
        contentValues.put("sim_imsi", contentValues2.getAsString("sim_imsi"));
    }

    public Cursor queryParticipantsUsingChatIdFromTP(String str) {
        return this.mTelephonyStorage.queryParticipantsUsingChatIdFromTP(str);
    }

    public Cursor queryParticipantsInfoFromTP(String str) {
        return this.mTelephonyStorage.queryParticipantsInfoFromTP(str);
    }

    public Cursor queryAllRCSChatFromTP(String str, String str2) {
        return this.mTelephonyStorage.queryAllRCSChatFromTP(str, str2);
    }

    public Cursor queryAllFtRCSFromTelephony(String str, String str2) {
        return this.mTelephonyStorage.queryAllFtRCSFromTelephony(str, str2);
    }

    public void insertSessionFromTPDBToRCSSessionBufferDB(Cursor cursor) {
        ArrayList<ContentValues> convertTPRCSSessionToCV = CursorContentValueTranslator.convertTPRCSSessionToCV(cursor, this.mStoreClient);
        String str = this.TAG;
        Log.d(str, "insertSessionFromTPDBToRCSSessionBufferDB size: " + convertTPRCSSessionToCV.size());
        String userTelCtn = this.mStoreClient.getPrerenceManager().getUserTelCtn();
        for (int i = 0; i < convertTPRCSSessionToCV.size(); i++) {
            ContentValues contentValues = convertTPRCSSessionToCV.get(i);
            String asString = contentValues.getAsString(ImContract.ImSession.PREFERRED_URI);
            if (asString == null) {
                asString = this.mStoreClient.getPrerenceManager().getUserTelCtn();
            }
            String asString2 = contentValues.getAsString("chat_id");
            if (TextUtils.isEmpty(asString2)) {
                Log.i(this.TAG, "insertSessionFromTPDBToRCSSessionBufferDB chatId is empty");
            } else {
                contentValues.put("linenum", asString);
                ImsUri normalizedTelUri = Util.getNormalizedTelUri(contentValues.getAsString(ImContract.ImSession.OWN_PHONE_NUMBER), this.mCountryCode);
                if (normalizedTelUri != null && !TextUtils.equals(normalizedTelUri.toString(), userTelCtn)) {
                    String generateConversationId = StringIdGenerator.generateConversationId();
                    contentValues.put("conversation_id", generateConversationId);
                    String str2 = this.TAG;
                    Log.d(str2, "new conv id====" + generateConversationId);
                }
                if (checkIfSessionPresentInBufferDB(asString2).longValue() == -1) {
                    contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Insert.getId()));
                    contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.ToSendCloud.getId()));
                    insertDeviceMsgToBuffer(10, contentValues);
                    copyParticipantsToBuffer(asString2);
                }
                Cursor queryAllRCSChatFromTP = queryAllRCSChatFromTP(asString2, this.IMSI);
                if (queryAllRCSChatFromTP != null) {
                    try {
                        if (queryAllRCSChatFromTP.moveToFirst()) {
                            insertToRCSMessagesBufferDBFromTP(queryAllRCSChatFromTP, asString, contentValues, false);
                        }
                    } catch (Throwable th) {
                        th.addSuppressed(th);
                    }
                }
                if (queryAllRCSChatFromTP != null) {
                    queryAllRCSChatFromTP.close();
                }
                Cursor queryAllFtRCSFromTelephony = queryAllFtRCSFromTelephony(asString2, this.IMSI);
                if (queryAllFtRCSFromTelephony != null) {
                    try {
                        if (queryAllFtRCSFromTelephony.moveToFirst()) {
                            insertToRCSMessagesBufferDBFromTP(queryAllFtRCSFromTelephony, asString, contentValues, true);
                        }
                    } catch (Throwable th2) {
                        th.addSuppressed(th2);
                    }
                }
                if (queryAllFtRCSFromTelephony != null) {
                    queryAllFtRCSFromTelephony.close();
                }
            }
        }
        return;
        throw th;
        throw th;
    }

    private void copyParticipantsToBuffer(String str) {
        Cursor queryParticipantsInfoFromTP;
        Cursor queryParticipantsUsingChatIdFromTP = queryParticipantsUsingChatIdFromTP(str);
        if (queryParticipantsUsingChatIdFromTP != null) {
            try {
                if (queryParticipantsUsingChatIdFromTP.moveToFirst()) {
                    for (String queryParticipantsInfoFromTP2 : queryParticipantsUsingChatIdFromTP.getString(queryParticipantsUsingChatIdFromTP.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBSMS.RECIPIENT_ID)).split(" ")) {
                        ContentValues contentValues = new ContentValues();
                        contentValues.put("chat_id", str);
                        queryParticipantsInfoFromTP = queryParticipantsInfoFromTP(queryParticipantsInfoFromTP2);
                        if (queryParticipantsInfoFromTP != null) {
                            if (queryParticipantsInfoFromTP.moveToFirst()) {
                                String string = queryParticipantsInfoFromTP.getString(queryParticipantsInfoFromTP.getColumnIndexOrThrow("address"));
                                contentValues.put("_id", Integer.valueOf(queryParticipantsInfoFromTP.getInt(queryParticipantsInfoFromTP.getColumnIndexOrThrow("_id"))));
                                contentValues.put("uri", Util.getTelUri(string, this.mCountryCode));
                                contentValues.put("sim_imsi", this.IMSI);
                                insertDeviceMsgToBuffer(2, contentValues);
                            }
                        }
                        if (queryParticipantsInfoFromTP != null) {
                            queryParticipantsInfoFromTP.close();
                        }
                    }
                }
            } catch (Throwable th) {
                try {
                    queryParticipantsUsingChatIdFromTP.close();
                } catch (Throwable th2) {
                    th.addSuppressed(th2);
                }
                throw th;
            }
        }
        if (queryParticipantsUsingChatIdFromTP != null) {
            queryParticipantsUsingChatIdFromTP.close();
            return;
        }
        return;
        throw th;
    }

    public Cursor querySessionByChatId(String str) {
        return this.mBufferDB.querySessionByChatId(str);
    }

    /* JADX WARNING: Removed duplicated region for block: B:14:0x0025  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public java.lang.Long checkIfSessionPresentInBufferDB(java.lang.String r3) {
        /*
            r2 = this;
            android.database.Cursor r2 = r2.querySessionByChatId(r3)
            if (r2 == 0) goto L_0x0021
            boolean r3 = r2.moveToFirst()     // Catch:{ all -> 0x0017 }
            if (r3 == 0) goto L_0x0021
            java.lang.String r3 = "_id"
            int r3 = r2.getColumnIndexOrThrow(r3)     // Catch:{ all -> 0x0017 }
            long r0 = r2.getLong(r3)     // Catch:{ all -> 0x0017 }
            goto L_0x0023
        L_0x0017:
            r3 = move-exception
            r2.close()     // Catch:{ all -> 0x001c }
            goto L_0x0020
        L_0x001c:
            r2 = move-exception
            r3.addSuppressed(r2)
        L_0x0020:
            throw r3
        L_0x0021:
            r0 = -1
        L_0x0023:
            if (r2 == 0) goto L_0x0028
            r2.close()
        L_0x0028:
            java.lang.Long r2 = java.lang.Long.valueOf(r0)
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder.checkIfSessionPresentInBufferDB(java.lang.String):java.lang.Long");
    }

    public Cursor queryLargestLastModSeqRow(String str) {
        String str2 = this.TAG;
        Log.i(str2, "queryLargestLastModSeqRow imdnID: " + str);
        return this.mBufferDB.queryRCSImdnMessages((String[]) null, "imdn_id=?", new String[]{str}, "lastmodseq DESC LIMIT 1");
    }

    public void updateParticipantsIdFromRcsDb(String str) {
        String str2 = this.TAG;
        Log.d(str2, "updateParticipantsIdFromRcsDb chatId " + str);
        Cursor queryParticipantsUsingChatId = queryParticipantsUsingChatId(str);
        if (queryParticipantsUsingChatId != null) {
            while (queryParticipantsUsingChatId.moveToNext()) {
                try {
                    int i = queryParticipantsUsingChatId.getInt(queryParticipantsUsingChatId.getColumnIndexOrThrow("_id"));
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("_id", Integer.valueOf(i));
                    this.mBufferDB.updateRCSParticipantsTable(contentValues, "chat_id=? AND uri=?", new String[]{str, queryParticipantsUsingChatId.getString(queryParticipantsUsingChatId.getColumnIndexOrThrow("uri"))});
                } catch (Throwable th) {
                    th.addSuppressed(th);
                }
            }
        }
        if (queryParticipantsUsingChatId != null) {
            queryParticipantsUsingChatId.close();
            return;
        }
        return;
        throw th;
    }
}
