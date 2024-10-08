package com.sec.internal.ims.servicemodules.im;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDiskIOException;
import android.database.sqlite.SQLiteFullException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.ChatData;
import com.sec.internal.constants.ims.servicemodules.im.ChatMode;
import com.sec.internal.constants.ims.servicemodules.im.ImCacheAction;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImContract;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.constants.ims.servicemodules.im.ImImdnRecRoute;
import com.sec.internal.constants.ims.servicemodules.im.ImParticipant;
import com.sec.internal.constants.ims.servicemodules.im.NotificationStatus;
import com.sec.internal.ims.servicemodules.im.interfaces.FtIntent;
import com.sec.internal.ims.servicemodules.im.interfaces.ImIntent;
import com.sec.internal.ims.settings.RcsPolicySettings;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ImPersister {
    private static final int DATABASE_VERSION = 31;
    private static final String IN_WHERE_PENDING_MESSAGES = ("(IFNULL(status, " + ImConstants.Status.IRRELEVANT.getId() + ") in (" + ImConstants.Status.SENDING.getId() + ", " + ImConstants.Status.TO_SEND.getId() + ") AND IFNULL(" + "direction" + ", " + ImDirection.IRRELEVANT.getId() + ") = " + ImDirection.OUTGOING.getId() + ") OR (IFNULL(" + "state" + ", " + 3 + ") != " + 3 + ")");
    private static final String IN_WHERE_PENDING_NOTIFICATION;
    private static final String IN_WHERE_REVOKE = ("(IFNULL(revocation_status, " + ImConstants.RevocationStatus.NONE.getId() + ") in (" + ImConstants.RevocationStatus.AVAILABLE.getId() + ", " + ImConstants.RevocationStatus.PENDING.getId() + ", " + ImConstants.RevocationStatus.SENDING.getId() + "))");
    private static final String LOG_TAG = "ImPersister";
    private final Context mContext;
    private final ImDBHelper mImDBHelper;
    private final ImModule mImModule;
    private final ContentResolver mResolver;

    static {
        StringBuilder sb = new StringBuilder();
        sb.append("(IFNULL(status, ");
        ImConstants.Status status = ImConstants.Status.FAILED;
        sb.append(status.getId());
        sb.append(") != ");
        sb.append(status.getId());
        sb.append(" OR ");
        sb.append(ImContract.ChatItem.IS_FILE_TRANSFER);
        sb.append(" = 1) AND IFNULL(");
        sb.append("direction");
        sb.append(", 2) = ");
        sb.append(ImDirection.INCOMING.getId());
        sb.append(" AND ");
        sb.append("notification_status");
        sb.append(" < ");
        sb.append(ImContract.Message.DISPOSITION_NOTIFICATION_STATUS);
        IN_WHERE_PENDING_NOTIFICATION = sb.toString();
    }

    public ImPersister(Context context, ImModule imModule) {
        Log.i(LOG_TAG, "ImPersister create");
        this.mContext = context;
        this.mImModule = imModule;
        this.mResolver = context.getContentResolver();
        this.mImDBHelper = new ImDBHelper(context, 31);
        clearDeletedParticipants();
        closeDB();
    }

    public Cursor querySessions(String[] strArr, String str, String[] strArr2, String str2) {
        Cursor cursor = null;
        try {
            SQLiteDatabase writableDatabase = this.mImDBHelper.getWritableDatabase();
            writableDatabase.beginTransaction();
            try {
                cursor = writableDatabase.query(ImDBHelper.SESSION_TABLE, strArr, str, strArr2, (String) null, (String) null, str2);
                setTransactionSuccessful(writableDatabase);
            } catch (SQLException e) {
                String str3 = LOG_TAG;
                Log.e(str3, "SQL exception while querying all sessions. " + e);
            } catch (Throwable th) {
                endTransaction(writableDatabase);
                throw th;
            }
            endTransaction(writableDatabase);
            return cursor;
        } catch (SQLiteDiskIOException e2) {
            String str4 = LOG_TAG;
            Log.e(str4, "SQLiteDiskIOException : " + e2);
            return null;
        }
    }

    public Cursor queryMessages(String[] strArr, String str, String[] strArr2, String str2) {
        Cursor cursor = null;
        try {
            SQLiteDatabase writableDatabase = this.mImDBHelper.getWritableDatabase();
            writableDatabase.beginTransaction();
            try {
                cursor = writableDatabase.query("message", strArr, str, strArr2, (String) null, (String) null, str2);
                setTransactionSuccessful(writableDatabase);
            } catch (SQLException e) {
                String str3 = LOG_TAG;
                Log.e(str3, "SQL exception while querying all sessions. " + e);
            } catch (Throwable th) {
                endTransaction(writableDatabase);
                throw th;
            }
            endTransaction(writableDatabase);
            return cursor;
        } catch (SQLiteDiskIOException e2) {
            String str4 = LOG_TAG;
            Log.e(str4, "SQLiteDiskIOException : " + e2);
            return null;
        }
    }

    private Cursor queryMessagesForTapi(String str, String[] strArr, String str2, String[] strArr2, String str3) {
        Cursor cursor;
        SQLiteDatabase writableDatabase = this.mImDBHelper.getWritableDatabase();
        writableDatabase.beginTransaction();
        try {
            cursor = writableDatabase.query(str, strArr, str2, strArr2, (String) null, (String) null, str3);
            try {
                setTransactionSuccessful(writableDatabase);
            } catch (SQLException e) {
                e = e;
                try {
                    String str4 = LOG_TAG;
                    Log.e(str4, "SQL exception while queryMessagesForTapi. " + e);
                    endTransaction(writableDatabase);
                    return cursor;
                } catch (Throwable th) {
                    endTransaction(writableDatabase);
                    throw th;
                }
            }
        } catch (SQLException e2) {
            e = e2;
            cursor = null;
            String str42 = LOG_TAG;
            Log.e(str42, "SQL exception while queryMessagesForTapi. " + e);
            endTransaction(writableDatabase);
            return cursor;
        }
        endTransaction(writableDatabase);
        return cursor;
    }

    public Cursor queryChatMessagesForTapi(String[] strArr, String str, String[] strArr2, String str2) {
        return queryMessagesForTapi(ImDBHelper.CHAT_MESSAGE_VIEW, strArr, str, strArr2, str2);
    }

    public Cursor queryFtMessagesForTapi(String[] strArr, String str, String[] strArr2, String str2) {
        return queryMessagesForTapi(ImDBHelper.FILETRANSFER_VIEW, strArr, str, strArr2, str2);
    }

    public Cursor queryParticipants(String[] strArr, String str, String[] strArr2, String str2) {
        Cursor cursor = null;
        try {
            SQLiteDatabase writableDatabase = this.mImDBHelper.getWritableDatabase();
            writableDatabase.beginTransaction();
            try {
                cursor = writableDatabase.query("participant", strArr, str, strArr2, (String) null, (String) null, str2);
                setTransactionSuccessful(writableDatabase);
            } catch (SQLException e) {
                String str3 = LOG_TAG;
                Log.e(str3, "SQL exception while querying all sessions. " + e);
            } catch (Throwable th) {
                endTransaction(writableDatabase);
                throw th;
            }
            endTransaction(writableDatabase);
            return cursor;
        } catch (SQLiteDiskIOException e2) {
            String str4 = LOG_TAG;
            Log.e(str4, "SQLiteDiskIOException : " + e2);
            return null;
        }
    }

    public Cursor queryMessageNotification(String[] strArr, String str, String[] strArr2, String str2) {
        Cursor cursor = null;
        try {
            SQLiteDatabase writableDatabase = this.mImDBHelper.getWritableDatabase();
            writableDatabase.beginTransaction();
            try {
                cursor = writableDatabase.query("notification", strArr, str, strArr2, (String) null, (String) null, str2);
                setTransactionSuccessful(writableDatabase);
            } catch (SQLException e) {
                String str3 = LOG_TAG;
                Log.e(str3, "SQL exception while querying all sessions. " + e);
            } catch (Throwable th) {
                endTransaction(writableDatabase);
                throw th;
            }
            endTransaction(writableDatabase);
            return cursor;
        } catch (SQLiteDiskIOException e2) {
            String str4 = LOG_TAG;
            Log.e(str4, "SQLiteDiskIOException : " + e2);
            return null;
        }
    }

    private Cursor query(String str, WhereClauseArgs whereClauseArgs, String[] strArr, String str2, String str3) {
        SQLiteQueryBuilder sQLiteQueryBuilder = new SQLiteQueryBuilder();
        sQLiteQueryBuilder.setTables(str);
        Cursor cursor = null;
        try {
            SQLiteDatabase writableDatabase = this.mImDBHelper.getWritableDatabase();
            writableDatabase.beginTransaction();
            if (whereClauseArgs != null) {
                try {
                    cursor = sQLiteQueryBuilder.query(writableDatabase, strArr, whereClauseArgs.getWhereClause(), whereClauseArgs.getWhereArgs(), str2, (String) null, str3);
                } catch (SQLException e) {
                    String str4 = LOG_TAG;
                    Log.e(str4, "SQL exception while querying " + e);
                } catch (Throwable th) {
                    endTransaction(writableDatabase);
                    throw th;
                }
            } else {
                cursor = sQLiteQueryBuilder.query(writableDatabase, strArr, (String) null, (String[]) null, str2, (String) null, str3);
            }
            setTransactionSuccessful(writableDatabase);
            endTransaction(writableDatabase);
            return cursor;
        } catch (SQLiteDiskIOException e2) {
            String str5 = LOG_TAG;
            Log.e(str5, "SQLiteDiskIOException : " + e2);
            return null;
        }
    }

    private Cursor query(String str, String str2, String[] strArr, String str3, String str4) {
        return query(str, new WhereClauseArgs(str2), strArr, str3, str4);
    }

    private void update(String str, List<Pair<ContentValues, WhereClauseArgs>> list) {
        try {
            SQLiteDatabase writableDatabase = this.mImDBHelper.getWritableDatabase();
            writableDatabase.beginTransaction();
            try {
                for (Pair next : list) {
                    Object obj = next.second;
                    if (obj != null) {
                        writableDatabase.update(str, (ContentValues) next.first, ((WhereClauseArgs) obj).getWhereClause(), ((WhereClauseArgs) next.second).getWhereArgs());
                    } else {
                        writableDatabase.update(str, (ContentValues) next.first, (String) null, (String[]) null);
                    }
                }
                setTransactionSuccessful(writableDatabase);
            } catch (SQLiteFullException e) {
                String str2 = LOG_TAG;
                Log.e(str2, "SQLiteOutOfMemoryException while update. " + e);
                this.mImModule.notifyDeviceOutOfMemory();
            } catch (SQLException e2) {
                String str3 = LOG_TAG;
                Log.e(str3, "SQL exception while update. " + e2);
            } catch (Throwable th) {
                endTransaction(writableDatabase);
                throw th;
            }
            endTransaction(writableDatabase);
        } catch (SQLiteDiskIOException e3) {
            String str4 = LOG_TAG;
            Log.e(str4, "SQLiteDiskIOException : " + e3);
        }
    }

    private void update(String str, ContentValues contentValues, String str2) {
        ArrayList arrayList = new ArrayList();
        arrayList.add(new Pair(contentValues, new WhereClauseArgs(str2)));
        update(str, arrayList);
    }

    private void delete(String str, String str2) {
        try {
            SQLiteDatabase writableDatabase = this.mImDBHelper.getWritableDatabase();
            writableDatabase.beginTransaction();
            try {
                writableDatabase.delete(str, str2, (String[]) null);
                setTransactionSuccessful(writableDatabase);
            } catch (SQLiteFullException e) {
                String str3 = LOG_TAG;
                Log.e(str3, "SQLiteOutOfMemoryException while delete. " + e);
                this.mImModule.notifyDeviceOutOfMemory();
            } catch (SQLException e2) {
                String str4 = LOG_TAG;
                Log.e(str4, "SQL exception while delete. " + e2);
            } catch (Throwable th) {
                endTransaction(writableDatabase);
                throw th;
            }
            endTransaction(writableDatabase);
        } catch (SQLiteDiskIOException | IllegalStateException e3) {
            String str5 = LOG_TAG;
            Log.e(str5, "Exception : " + e3);
        }
    }

    private List<MessageBase> queryMessages(String str) {
        ArrayList arrayList = new ArrayList();
        Cursor query = query("message", str, (String[]) null, (String) null, (String) null);
        if (query == null) {
            if (query != null) {
                query.close();
            }
            return arrayList;
        }
        while (query.moveToNext()) {
            try {
                boolean z = true;
                if (query.getInt(query.getColumnIndexOrThrow(ImContract.ChatItem.IS_FILE_TRANSFER)) != 1) {
                    z = false;
                }
                if (z) {
                    arrayList.add(this.mImDBHelper.makeFtMessage(query, this.mImModule));
                } else {
                    arrayList.add(this.mImDBHelper.makeImMessage(query, this.mImModule));
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        query.close();
        return arrayList;
        throw th;
    }

    public List<ChatData> querySessions(String str) {
        ArrayList arrayList = new ArrayList();
        Cursor query = query(ImDBHelper.SESSION_TABLE, str, (String[]) null, (String) null, (String) null);
        if (query == null) {
            if (query != null) {
                query.close();
            }
            return arrayList;
        }
        while (query.moveToNext()) {
            try {
                arrayList.add(ImDBHelper.makeSession(query));
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        query.close();
        return arrayList;
        throw th;
    }

    private List<ImParticipant> queryParticipants(String str) {
        ArrayList arrayList = new ArrayList();
        Cursor query = query("participant", str, (String[]) null, (String) null, (String) null);
        if (query == null) {
            if (query != null) {
                query.close();
            }
            return arrayList;
        }
        while (query.moveToNext()) {
            try {
                arrayList.add(this.mImDBHelper.makeParticipant(query));
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        query.close();
        return arrayList;
        throw th;
    }

    private List<ImImdnRecRoute> queryImImdnRecRoutes(String str) {
        ArrayList arrayList = new ArrayList();
        Cursor query = query(ImDBHelper.IMDNRECROUTE_TABLE, str, (String[]) null, (String) null, (String) null);
        if (query == null) {
            if (query != null) {
                query.close();
            }
            return arrayList;
        }
        while (query.moveToNext()) {
            try {
                arrayList.add(this.mImDBHelper.makeImdnRecRoute(query));
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        query.close();
        return arrayList;
        throw th;
    }

    public MessageBase queryMessage(String str) {
        List<MessageBase> queryMessages = queryMessages("_id = '" + str + "'");
        if (queryMessages.isEmpty()) {
            return null;
        }
        return queryMessages.get(0);
    }

    /* access modifiers changed from: protected */
    public void insertSession(ChatData chatData) {
        String str = LOG_TAG;
        IMSLog.s(str, "insertSession: " + chatData);
        try {
            SQLiteDatabase writableDatabase = this.mImDBHelper.getWritableDatabase();
            ContentValues makeSessionRow = ImDBHelper.makeSessionRow(chatData);
            writableDatabase.beginTransaction();
            try {
                long insert = writableDatabase.insert(ImDBHelper.SESSION_TABLE, (String) null, makeSessionRow);
                if (insert != -1) {
                    Log.i(str, "Set chat id " + insert + " (" + chatData.getChatId() + ")");
                    chatData.setId((int) insert);
                    setTransactionSuccessful(writableDatabase);
                } else {
                    Log.e(str, "SQL exception while inserting a session.");
                }
            } finally {
                endTransaction(writableDatabase);
            }
        } catch (SQLiteDiskIOException e) {
            String str2 = LOG_TAG;
            Log.e(str2, "SQLiteDiskIOException : " + e);
        }
    }

    /* access modifiers changed from: protected */
    public void onSessionUpdated(ChatData chatData) {
        String str = LOG_TAG;
        IMSLog.s(str, "onSessionUpdated: " + chatData);
        update(ImDBHelper.SESSION_TABLE, ImDBHelper.makeSessionRow(chatData), "_id = " + chatData.getId());
        String chatId = chatData.getChatId();
        if (this.mResolver != null && chatId != null) {
            this.mResolver.notifyChange(Uri.parse("content://com.samsung.rcs.im/chat/" + chatId), (ContentObserver) null);
            if (chatData.getState() == ChatData.State.ACTIVE || chatData.getState() == ChatData.State.NONE || chatData.getState() == ChatData.State.CLOSED_VOLUNTARILY) {
                Uri parse = Uri.parse("content://com.samsung.rcs.cmstore/chat/" + chatId);
                Log.i(str, "onSessionUpdated, storeUri: " + parse);
                this.mResolver.notifyChange(parse, (ContentObserver) null);
            }
            Log.i(str, "onSessionUpdated: notifyChange to " + chatId + "(state=" + chatData.getState() + ")");
        }
    }

    private void deleteSession(ChatData chatData) {
        String str = LOG_TAG;
        IMSLog.s(str, "deleteSession: " + chatData);
        delete(ImDBHelper.SESSION_TABLE, "_id=" + chatData.getId());
    }

    public List<String> querySessionForAutoRejoin(boolean z) {
        String str = "(";
        if (z) {
            str = str + "status = '1' OR status = '3' OR ";
        }
        ArrayList arrayList = new ArrayList();
        Cursor query = query(ImDBHelper.SESSION_TABLE, str + "status = '4') AND chat_type = " + ChatData.ChatType.REGULAR_GROUP_CHAT.getId(), new String[]{"chat_id"}, (String) null, (String) null);
        if (query == null) {
            if (query != null) {
                query.close();
            }
            return arrayList;
        }
        while (query.moveToNext()) {
            try {
                arrayList.add(query.getString(0));
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        query.close();
        return arrayList;
        throw th;
    }

    public List<String> querySessionByChatType(boolean z) {
        StringBuilder sb = new StringBuilder();
        sb.append("is_group_chat = ");
        sb.append(z ? "'1'" : "'0'");
        List<ChatData> querySessions = querySessions(sb.toString());
        if (querySessions.isEmpty()) {
            return null;
        }
        ArrayList arrayList = new ArrayList();
        for (ChatData chatId : querySessions) {
            arrayList.add(chatId.getChatId());
        }
        return arrayList;
    }

    public ChatData querySessionByChatId(String str) {
        List<ChatData> querySessions = querySessions("chat_id = '" + str + "'");
        if (querySessions.isEmpty()) {
            return null;
        }
        return querySessions.get(0);
    }

    public ChatData querySessionByContributionId(String str, String str2, boolean z) {
        StringBuilder sb = new StringBuilder();
        sb.append("contribution_id = '");
        sb.append(str2);
        sb.append("' AND ");
        sb.append("sim_imsi");
        sb.append("='");
        sb.append(str);
        sb.append("' AND ");
        sb.append("is_group_chat");
        sb.append(" = ");
        sb.append(z ? "'1'" : "'0'");
        List<ChatData> querySessions = querySessions(sb.toString());
        if (querySessions.isEmpty()) {
            return null;
        }
        return querySessions.get(0);
    }

    public ChatData querySessionByConversationId(String str, String str2, boolean z) {
        String str3 = LOG_TAG;
        IMSLog.s(str3, "querySessionByConversationId cid=" + str2);
        StringBuilder sb = new StringBuilder();
        sb.append("conversation_id = '");
        sb.append(str2);
        sb.append("' AND ");
        sb.append("sim_imsi");
        sb.append("='");
        sb.append(str);
        sb.append("' AND ");
        sb.append("is_group_chat");
        sb.append(" = ");
        sb.append(z ? "'1'" : "'0'");
        List<ChatData> querySessions = querySessions(sb.toString());
        if (querySessions.isEmpty()) {
            return null;
        }
        return querySessions.get(0);
    }

    public List<String> queryAllSessionByParticipant(Set<ImsUri> set, ChatData.ChatType chatType) {
        String str = LOG_TAG;
        IMSLog.s(str, "queryAllSessionByParticipant chatType=" + chatType + " participants=" + set);
        ArrayList arrayList = new ArrayList();
        Cursor query = query("session, participant", String.format("%s.%s=%s.%s and %s=%s", new Object[]{ImDBHelper.SESSION_TABLE, "chat_id", "participant", "chat_id", ImContract.ImSession.CHAT_TYPE, Integer.valueOf(chatType.getId())}), new String[]{"DISTINCT session.chat_id", "participant.uri"}, (String) null, (String) null);
        if (query != null) {
            while (query.moveToNext()) {
                try {
                    if (set.contains(ImsUri.parse(query.getString(1)))) {
                        arrayList.add(query.getString(0));
                    }
                } catch (Throwable th) {
                    th.addSuppressed(th);
                }
            }
            String str2 = LOG_TAG;
            Log.i(str2, "Chats found: " + arrayList);
            query.close();
            return arrayList;
        } else if (query == null) {
            return null;
        } else {
            query.close();
            return null;
        }
        throw th;
    }

    public ChatData querySessionByParticipants(Set<ImsUri> set, ChatData.ChatType chatType, String str, ChatMode chatMode) {
        String str2;
        String str3;
        IMSLog.s(LOG_TAG, "querySessionByParticipants chatType=" + chatType + " participants=" + set);
        String format = String.format("%s.%s=%s.%s and %s=%s", new Object[]{ImDBHelper.SESSION_TABLE, "chat_id", "participant", "chat_id", ImContract.ImSession.CHAT_TYPE, Integer.valueOf(chatType.getId())});
        if (!TextUtils.isEmpty(str)) {
            str2 = format + String.format(" and %s=%s", new Object[]{"sim_imsi", str});
        } else {
            str2 = format + String.format(" and %s=%s", new Object[]{ImContract.ImSession.CHAT_MODE, Integer.valueOf(chatMode.getId())});
        }
        Cursor query = query("session, participant", str2, new String[]{"session.chat_id", "group_concat(participant.uri)", "session.preferred_uri"}, "session.chat_id", (String) null);
        if (query == null) {
            if (query != null) {
                query.close();
            }
            return null;
        }
        while (true) {
            try {
                if (!query.moveToNext()) {
                    str3 = null;
                    break;
                }
                String string = query.getString(1);
                if (string != null) {
                    HashSet hashSet = new HashSet();
                    for (String parse : string.split(",")) {
                        hashSet.add(ImsUri.parse(parse));
                    }
                    String str4 = LOG_TAG;
                    IMSLog.s(str4, "querySessionByParticipants compare participants=" + hashSet);
                    if (set.equals(hashSet)) {
                        str3 = query.getString(0);
                        Log.i(str4, "Chat found:" + str3);
                        break;
                    }
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        query.close();
        if (str3 == null) {
            return null;
        }
        return querySessionByChatId(str3);
        throw th;
    }

    /* access modifiers changed from: protected */
    public void insertParticipant(ImParticipant imParticipant) {
        ArrayList arrayList = new ArrayList();
        arrayList.add(imParticipant);
        insertParticipant((Collection<ImParticipant>) arrayList);
    }

    /* access modifiers changed from: protected */
    public void insertParticipant(Collection<ImParticipant> collection) {
        try {
            SQLiteDatabase writableDatabase = this.mImDBHelper.getWritableDatabase();
            writableDatabase.beginTransaction();
            try {
                for (ImParticipant next : collection) {
                    long insert = writableDatabase.insert("participant", (String) null, ImDBHelper.makeParticipantRow(next));
                    if (insert != -1) {
                        String str = LOG_TAG;
                        Log.i(str, "Set participant id " + insert);
                        next.setId((int) insert);
                    } else {
                        Log.e(LOG_TAG, "SQL exception while inserting a participant.");
                    }
                }
                setTransactionSuccessful(writableDatabase);
            } finally {
                endTransaction(writableDatabase);
            }
        } catch (SQLiteDiskIOException e) {
            String str2 = LOG_TAG;
            Log.e(str2, "SQLiteDiskIOException : " + e);
        }
    }

    private void deleteParticipant(ImParticipant imParticipant) {
        delete("participant", "_id=" + imParticipant.getId());
    }

    /* access modifiers changed from: protected */
    public void deleteParticipant(Collection<ImParticipant> collection) {
        ArrayList arrayList = new ArrayList();
        for (ImParticipant id : collection) {
            arrayList.add(Integer.valueOf(id.getId()));
        }
        delete("participant", "_id in ('" + TextUtils.join("', '", arrayList) + "')");
    }

    private void onParticipantUpdated(ImParticipant imParticipant) {
        ArrayList arrayList = new ArrayList();
        arrayList.add(imParticipant);
        onParticipantUpdated((Collection<ImParticipant>) arrayList);
    }

    private void onParticipantUpdated(Collection<ImParticipant> collection) {
        ArrayList arrayList = new ArrayList();
        for (ImParticipant next : collection) {
            ContentValues makeParticipantRow = ImDBHelper.makeParticipantRow(next);
            arrayList.add(new Pair(makeParticipantRow, new WhereClauseArgs("_id = " + next.getId())));
        }
        update("participant", arrayList);
    }

    private void setTransactionSuccessful(SQLiteDatabase sQLiteDatabase) {
        try {
            sQLiteDatabase.setTransactionSuccessful();
        } catch (IllegalStateException e) {
            String str = LOG_TAG;
            Log.e(str, "SQLException while setTransactionSuccessful:" + e);
        }
    }

    private void endTransaction(SQLiteDatabase sQLiteDatabase) {
        try {
            sQLiteDatabase.endTransaction();
        } catch (SQLiteFullException unused) {
            Log.e(LOG_TAG, "SQLiteOutOfMemoryException endTransaction");
            this.mImModule.notifyDeviceOutOfMemory();
        } catch (SQLException e) {
            String str = LOG_TAG;
            Log.e(str, "SQLException while endTransaction:" + e);
        }
    }

    public Set<ImParticipant> queryParticipantSet(String str) {
        return new HashSet(queryParticipants("chat_id='" + str + "'"));
    }

    public List<ImParticipant> queryParticipant(String str, String str2) {
        return queryParticipants("chat_id='" + str + "' and " + "uri" + "='" + str2 + "'");
    }

    private void insertImdnRecRoute(Collection<ImImdnRecRoute> collection, int i) {
        try {
            SQLiteDatabase writableDatabase = this.mImDBHelper.getWritableDatabase();
            writableDatabase.beginTransaction();
            try {
                for (ImImdnRecRoute next : collection) {
                    next.setMessageId(i);
                    long insert = writableDatabase.insert(ImDBHelper.IMDNRECROUTE_TABLE, (String) null, this.mImDBHelper.makeImdnRecRouteRow(next));
                    if (insert != -1) {
                        String str = LOG_TAG;
                        Log.i(str, "Set imdnrecroute id " + insert);
                        next.setId((int) insert);
                    } else {
                        Log.e(LOG_TAG, "SQL exception while inserting a imdnrecroute.");
                    }
                }
                setTransactionSuccessful(writableDatabase);
            } finally {
                endTransaction(writableDatabase);
            }
        } catch (SQLiteDiskIOException e) {
            String str2 = LOG_TAG;
            Log.e(str2, "SQLiteDiskIOException : " + e);
        }
    }

    /* access modifiers changed from: package-private */
    public List<ImImdnRecRoute> queryImImdnRecRoute(MessageBase messageBase) {
        if (messageBase == null || messageBase.getId() <= 0 || TextUtils.isEmpty(messageBase.getImdnId())) {
            return new ArrayList();
        }
        return queryImImdnRecRoutes("message_id = " + messageBase.getId() + " OR (" + "imdn_id" + " = '" + messageBase.getImdnId() + "' AND " + "message_id" + " = 0)");
    }

    private void insertMessage(MessageBase messageBase) {
        ContentValues contentValues;
        String str = LOG_TAG;
        IMSLog.s(str, "insertMessage: " + messageBase);
        try {
            SQLiteDatabase writableDatabase = this.mImDBHelper.getWritableDatabase();
            if (messageBase instanceof ImMessage) {
                contentValues = this.mImDBHelper.makeImMessageRow((ImMessage) messageBase);
            } else {
                contentValues = messageBase instanceof FtMessage ? this.mImDBHelper.makeFtMessageRow((FtMessage) messageBase) : null;
            }
            if (contentValues != null) {
                writableDatabase.beginTransaction();
                try {
                    long insert = writableDatabase.insert("message", (String) null, contentValues);
                    if (insert != -1) {
                        Log.i(str, "Set message id " + insert + " (" + messageBase.getImdnId() + ")");
                        messageBase.setId((int) insert);
                        setTransactionSuccessful(writableDatabase);
                    } else {
                        Log.e(str, "SQL exception while inserting a message.");
                    }
                } finally {
                    endTransaction(writableDatabase);
                }
            }
        } catch (SQLiteDiskIOException e) {
            String str2 = LOG_TAG;
            Log.e(str2, "SQLiteDiskIOException : " + e);
        }
    }

    private void insertMessageNotification(MessageBase messageBase) {
        try {
            SQLiteDatabase writableDatabase = this.mImDBHelper.getWritableDatabase();
            writableDatabase.beginTransaction();
            try {
                for (ImParticipant next : queryParticipantSet(messageBase.getChatId())) {
                    if (writableDatabase.insert("notification", (String) null, this.mImDBHelper.makeMessageNotificationRow(messageBase, next.getUri().toString())) != -1) {
                        String str = LOG_TAG;
                        IMSLog.s(str, "Set Notification sender_uri " + next.getUri());
                    } else {
                        Log.e(LOG_TAG, "SQL exception while inserting a notification.");
                    }
                }
                setTransactionSuccessful(writableDatabase);
            } finally {
                endTransaction(writableDatabase);
            }
        } catch (SQLiteDiskIOException e) {
            String str2 = LOG_TAG;
            Log.e(str2, "SQLiteDiskIOException : " + e);
        }
    }

    private void onMessageUpdated(MessageBase messageBase) {
        ContentValues contentValues;
        IMSLog.s(LOG_TAG, "onMessageUpdated: " + messageBase);
        String str = "_id = " + messageBase.getId();
        if (messageBase instanceof ImMessage) {
            contentValues = this.mImDBHelper.makeImMessageRow((ImMessage) messageBase);
        } else {
            contentValues = messageBase instanceof FtMessage ? this.mImDBHelper.makeFtMessageRow((FtMessage) messageBase) : null;
        }
        if (contentValues != null) {
            update("message", contentValues, str);
        }
    }

    private void onMessageNotificationUpdated(MessageBase messageBase) {
        long j;
        ImsUri notificationParticipant = messageBase.getNotificationParticipant();
        if (notificationParticipant == null) {
            Log.e(LOG_TAG, "onMessageNotificationUpdated participant is null");
            return;
        }
        String str = LOG_TAG;
        IMSLog.s(str, "onMessageNotificationUpdated participant : " + notificationParticipant);
        if (messageBase.getLastNotificationType() == NotificationStatus.DELIVERED || messageBase.getLastNotificationType() == NotificationStatus.INTERWORKING_SMS || messageBase.getLastNotificationType() == NotificationStatus.INTERWORKING_MMS) {
            j = messageBase.getDeliveredTimestamp();
        } else {
            j = messageBase.getLastNotificationType() == NotificationStatus.DISPLAYED ? messageBase.getLastDisplayedTimestamp().longValue() : 0;
        }
        Log.i(str, "onMessageNotificationUpdated status : " + messageBase.getLastNotificationType().getId() + ", timeStamp : " + j);
        ContentValues makeMessageNotificationUpdateRow = this.mImDBHelper.makeMessageNotificationUpdateRow(j, messageBase.getLastNotificationType().getId());
        update("notification", makeMessageNotificationUpdateRow, "imdn_id = '" + messageBase.getImdnId() + "' AND " + ImContract.MessageNotification.SENDER_URI + " = '" + notificationParticipant + "'");
    }

    /* access modifiers changed from: protected */
    public void deleteMessage(int i) {
        deleteMessageNotification(i);
        deleteImdnRecRoute(i);
        delete("message", "_id = " + i);
    }

    /* access modifiers changed from: protected */
    public void deleteMessage(String str) {
        List<Integer> queryAllMessageIdsByChatId = queryAllMessageIdsByChatId(str, false);
        deleteMessageNotification(queryAllMessageIdsByChatId);
        deleteImdnRecRoute(queryAllMessageIdsByChatId);
        delete("message", "chat_id = '" + str + "'");
    }

    private void deleteMessageNotification(int i) {
        ArrayList arrayList = new ArrayList();
        arrayList.add(Integer.valueOf(i));
        deleteMessageNotification((List<Integer>) arrayList);
    }

    private void deleteMessageNotification(List<Integer> list) {
        String str = "'" + TextUtils.join("', '", list) + "'";
        SQLiteQueryBuilder sQLiteQueryBuilder = new SQLiteQueryBuilder();
        sQLiteQueryBuilder.setTables("message");
        StringBuilder sb = new StringBuilder();
        sb.append("message_id in (");
        sb.append(str);
        sb.append(") OR (");
        sb.append("message_id");
        sb.append(" = 0 AND ");
        sb.append("imdn_id");
        sb.append(" in (");
        sb.append(sQLiteQueryBuilder.buildQuery(new String[]{"imdn_message_id"}, "_id in (" + str + ")", (String) null, (String) null, (String) null, (String) null));
        sb.append("))");
        delete("notification", sb.toString());
    }

    private void deleteImdnRecRoute(int i) {
        ArrayList arrayList = new ArrayList();
        arrayList.add(Integer.valueOf(i));
        deleteImdnRecRoute((List<Integer>) arrayList);
    }

    private void deleteImdnRecRoute(List<Integer> list) {
        String str = "'" + TextUtils.join("', '", list) + "'";
        SQLiteQueryBuilder sQLiteQueryBuilder = new SQLiteQueryBuilder();
        sQLiteQueryBuilder.setTables("message");
        StringBuilder sb = new StringBuilder();
        sb.append("message_id in (");
        sb.append(str);
        sb.append(") OR (");
        sb.append("message_id");
        sb.append(" = 0 AND ");
        sb.append("imdn_id");
        sb.append(" in (");
        sb.append(sQLiteQueryBuilder.buildQuery(new String[]{"imdn_message_id"}, "_id in (" + str + ")", (String) null, (String) null, (String) null, (String) null));
        sb.append("))");
        delete(ImDBHelper.IMDNRECROUTE_TABLE, sb.toString());
    }

    public List<MessageBase> queryMessages(Collection<String> collection) {
        return queryMessages("_id in ('" + TextUtils.join("', '", collection) + "')");
    }

    public List<MessageBase> queryMessagesUsingChatID(String str) {
        List<MessageBase> queryMessages = queryMessages("chat_id = '" + str + "'");
        if (queryMessages.isEmpty()) {
            return null;
        }
        return queryMessages;
    }

    public List<MessageBase> queryMessagesUsingChatIDExceptPending(String str, List<String> list) {
        List<MessageBase> queryMessages = queryMessages("chat_id = '" + str + "' AND " + "_id" + " not in ('" + TextUtils.join("', '", list) + "')");
        if (queryMessages.isEmpty()) {
            return null;
        }
        return queryMessages;
    }

    public List<MessageBase> queryMessages(Collection<String> collection, ImDirection imDirection, String str) {
        String str2;
        StringBuilder sb = new StringBuilder();
        sb.append("imdn_message_id in ('");
        sb.append(TextUtils.join("', '", collection));
        sb.append("') AND ");
        sb.append("direction");
        sb.append(" = '");
        sb.append(imDirection.getId());
        sb.append("'");
        if (str != null) {
            str2 = " AND chat_id = '" + str + "'";
        } else {
            str2 = "";
        }
        sb.append(str2);
        return queryMessages(sb.toString());
    }

    public MessageBase queryMessage(String str, ImDirection imDirection, String str2) {
        String str3;
        StringBuilder sb = new StringBuilder();
        sb.append("imdn_message_id = '");
        sb.append(str);
        sb.append("' AND ");
        sb.append("direction");
        sb.append(" = '");
        sb.append(imDirection.getId());
        sb.append("'");
        if (str2 != null) {
            str3 = " AND chat_id = '" + str2 + "'";
        } else {
            str3 = "";
        }
        sb.append(str3);
        List<MessageBase> queryMessages = queryMessages(sb.toString());
        if (queryMessages.isEmpty()) {
            return null;
        }
        return queryMessages.get(0);
    }

    public FtMessage queryFtMessageByFileTransferId(String str, String str2) {
        List<MessageBase> queryMessages = queryMessages("is_filetransfer = '1' AND file_transfer_id = '" + str + "' AND " + "chat_id" + " = '" + str2 + "'");
        if (queryMessages.isEmpty()) {
            return null;
        }
        return (FtMessage) queryMessages.get(0);
    }

    public List<Integer> queryAllMessageIdsByChatId(String str, boolean z) {
        String str2 = "chat_id = '" + str + "'";
        if (z) {
            str2 = str2 + " AND is_filetransfer = '1'";
        }
        return queryMessageIds(str2);
    }

    public Cursor queryMessagesByChatIdForDump(String str, int i) {
        SQLiteQueryBuilder sQLiteQueryBuilder = new SQLiteQueryBuilder();
        sQLiteQueryBuilder.setTables("message");
        WhereClauseArgs whereClauseArgs = new WhereClauseArgs("chat_id = '" + str + "'");
        String[] strArr = {"imdn_message_id", "message_type", "body", ImContract.CsSession.FILE_NAME, "status", ImContract.CsSession.BYTES_TRANSFERRED, ImContract.CsSession.FILE_SIZE, "direction", ImContract.Message.SENT_TIMESTAMP, ImContract.ChatItem.DELIVERED_TIMESTAMP, "notification_status"};
        Cursor cursor = null;
        try {
            SQLiteDatabase writableDatabase = this.mImDBHelper.getWritableDatabase();
            writableDatabase.beginTransaction();
            try {
                cursor = sQLiteQueryBuilder.query(writableDatabase, strArr, whereClauseArgs.getWhereClause(), whereClauseArgs.getWhereArgs(), (String) null, (String) null, "sent_timestamp DESC", Integer.toString(i));
                setTransactionSuccessful(writableDatabase);
            } catch (SQLException e) {
                String str2 = LOG_TAG;
                Log.e(str2, "SQL exception while querying " + e);
            } catch (Throwable th) {
                endTransaction(writableDatabase);
                throw th;
            }
            endTransaction(writableDatabase);
            return cursor;
        } catch (SQLiteDiskIOException e2) {
            String str3 = LOG_TAG;
            Log.e(str3, "SQLiteDiskIOException : " + e2);
            return null;
        }
    }

    private List<Integer> queryMessageIds(String str) {
        ArrayList arrayList = new ArrayList();
        Cursor query = query("message", str, new String[]{"_id"}, (String) null, (String) null);
        if (query == null) {
            if (query != null) {
                query.close();
            }
            return arrayList;
        }
        while (query.moveToNext()) {
            try {
                arrayList.add(Integer.valueOf(query.getInt(query.getColumnIndexOrThrow("_id"))));
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        query.close();
        return arrayList;
        throw th;
    }

    public List<Integer> queryPendingMessageIds(String str) {
        String str2 = LOG_TAG;
        Log.i(str2, "queryPendingMessageIds:" + str);
        return queryMessageIds("chat_id='" + str + "' AND " + IN_WHERE_PENDING_MESSAGES);
    }

    public List<Integer> queryMessagesIdsForRevoke(String str) {
        Log.i(LOG_TAG, "queryImMessagesIdsForRevoke:" + str);
        String str2 = "chat_id='" + str + "' AND " + IN_WHERE_REVOKE;
        ArrayList arrayList = new ArrayList();
        Cursor query = query("message", str2, new String[]{"_id"}, (String) null, (String) null);
        if (query != null) {
            while (query.moveToNext()) {
                try {
                    arrayList.add(Integer.valueOf(query.getInt(query.getColumnIndexOrThrow("_id"))));
                } catch (Throwable th) {
                    th.addSuppressed(th);
                }
            }
        }
        if (query != null) {
            query.close();
        }
        return arrayList;
        throw th;
    }

    public List<Integer> queryMessageIdsForPendingNotification(String str) {
        String str2 = LOG_TAG;
        Log.i(str2, "queryMessagesForPendingNotification:" + str);
        return queryMessageIds("chat_id='" + str + "' AND " + IN_WHERE_PENDING_NOTIFICATION);
    }

    public List<Integer> queryMessageIdsForDisplayAggregation(String str, ImDirection imDirection, Long l) {
        String str2 = LOG_TAG;
        Log.i(str2, "queryMessageIdsForDisplayAggregation: chatId = " + str + ", direction = " + imDirection + ", timestamp = " + l);
        return queryMessageIds("chat_id = '" + str + "' AND " + "notification_status" + " = " + NotificationStatus.DELIVERED.getId() + " AND " + ImContract.Message.NOTIFICATION_DISPOSITION_MASK + " & " + NotificationStatus.DISPLAYED.getId() + " != 0 AND " + ImContract.ChatItem.DELIVERED_TIMESTAMP + " <= " + l + " AND " + "direction" + " = " + imDirection.getId());
    }

    public List<String> queryAllChatIDwithPendingMessages() {
        Log.i(LOG_TAG, "queryAllChatIDwithPendingMessages at bootup");
        ArrayList arrayList = new ArrayList();
        Cursor query = query("message", IN_WHERE_PENDING_MESSAGES + " OR " + IN_WHERE_PENDING_NOTIFICATION + " OR " + IN_WHERE_REVOKE, new String[]{"chat_id"}, (String) null, (String) null);
        if (query == null) {
            if (query != null) {
                query.close();
            }
            return arrayList;
        }
        while (query.moveToNext()) {
            try {
                arrayList.add(query.getString(query.getColumnIndexOrThrow("chat_id")));
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        query.close();
        String str = LOG_TAG;
        Log.i(str, "queryAllChatIDwithPendingMessages: " + arrayList);
        return arrayList;
        throw th;
    }

    public List<String> queryAllChatIDwithFailedFTMessages() {
        String str = LOG_TAG;
        Log.i(str, "queryAllChatIDwithFailedFTMessages at bootup");
        ArrayList arrayList = new ArrayList();
        String str2 = "(IFNULL(ft_status, 0) == " + ImConstants.Status.FAILED.getId() + ") AND IFNULL(" + "direction" + ", 0) != " + ImDirection.IRRELEVANT.getId();
        Log.i(str, "queryAllChatIDwithFailedFTMessages lsj, inWhere: " + str2);
        Cursor query = query("message", str2, new String[]{"chat_id"}, (String) null, (String) null);
        if (query == null) {
            if (query != null) {
                query.close();
            }
            return arrayList;
        }
        while (query.moveToNext()) {
            try {
                arrayList.add(query.getString(query.getColumnIndexOrThrow("chat_id")));
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        query.close();
        Log.i(LOG_TAG, "queryAllChatIDwithFailedFTMessages: " + arrayList);
        return arrayList;
        throw th;
    }

    public NotificationStatus queryNotificationStatus(String str, ImsUri imsUri) {
        Cursor query = query("notification", "imdn_id = '" + str + "' AND " + ImContract.MessageNotification.SENDER_URI + " = '" + imsUri + "'", new String[]{"status"}, (String) null, (String) null);
        NotificationStatus notificationStatus = null;
        if (query == null) {
            if (query != null) {
                query.close();
            }
            return null;
        }
        try {
            if (query.moveToNext()) {
                notificationStatus = NotificationStatus.fromId(query.getInt(query.getColumnIndexOrThrow("status")));
            }
            query.close();
            return notificationStatus;
        } catch (Throwable th) {
            th.addSuppressed(th);
        }
        throw th;
    }

    public List<Bundle> queryLastSentMessages(List<String> list) {
        String str;
        String str2;
        Cursor query;
        Throwable th;
        int i;
        int i2;
        List<String> list2 = list;
        String str3 = LOG_TAG;
        Log.i(str3, "queryLastSentMessages listRequestMessageId size = " + list.size());
        ArrayList arrayList = new ArrayList();
        try {
            SQLiteDatabase writableDatabase = this.mImDBHelper.getWritableDatabase();
            writableDatabase.beginTransaction();
            try {
                SQLiteQueryBuilder sQLiteQueryBuilder = new SQLiteQueryBuilder();
                sQLiteQueryBuilder.setTables("message");
                Log.i(str3, "list of request message ids" + list2);
                if (list.size() >= 1) {
                    sQLiteQueryBuilder.appendWhere("request_message_id IN (" + TextUtils.join(", ", list2) + ")");
                    str2 = null;
                    str = null;
                } else {
                    str = "sent_timestamp DESC";
                    str2 = "1";
                }
                query = sQLiteQueryBuilder.query(writableDatabase, (String[]) null, (String) null, (String[]) null, (String) null, (String) null, str, str2);
                while (query != null) {
                    if (!query.moveToNext()) {
                        break;
                    }
                    int i3 = query.getInt(query.getColumnIndexOrThrow("request_message_id"));
                    String string = query.getString(query.getColumnIndexOrThrow("chat_id"));
                    int i4 = query.getInt(query.getColumnIndexOrThrow(ImContract.ChatItem.IS_FILE_TRANSFER));
                    if (i4 == 0) {
                        i2 = query.getInt(query.getColumnIndexOrThrow("status"));
                        i = 0;
                    } else {
                        i2 = query.getInt(query.getColumnIndexOrThrow(ImContract.CsSession.STATUS));
                        i = query.getInt(query.getColumnIndexOrThrow(ImContract.CsSession.IS_RESUMABLE));
                    }
                    Bundle bundle = new Bundle();
                    bundle.putString("chat_id", string);
                    bundle.putLong("request_message_id", (long) i3);
                    bundle.putInt(ImIntent.Extras.IS_FILE_TRANSFER, i4);
                    if (i2 == ImConstants.Status.FAILED.getId()) {
                        bundle.putBoolean("response_status", false);
                    } else if (i2 == ImConstants.Status.SENT.getId()) {
                        bundle.putBoolean("response_status", true);
                    }
                    bundle.putInt(FtIntent.Extras.EXTRA_RESUMABLE_OPTION_CODE, i);
                    arrayList.add(bundle);
                }
                if (query != null) {
                    query.close();
                }
                setTransactionSuccessful(writableDatabase);
            } catch (SQLException e) {
                try {
                    Log.e(LOG_TAG, "SQL exception while queryAllChatIDwithPendingMessages. " + e);
                } catch (Throwable th2) {
                    endTransaction(writableDatabase);
                    throw th2;
                }
            } catch (Throwable th3) {
                th.addSuppressed(th3);
            }
            endTransaction(writableDatabase);
            return arrayList;
            throw th;
        } catch (SQLiteDiskIOException e2) {
            Log.e(LOG_TAG, "SQLiteDiskIOException : " + e2);
            return arrayList;
        }
    }

    public Collection<ImsUri> queryChatbotRoleUris(String str) {
        ArrayList arrayList = new ArrayList();
        Cursor query = query("participant, session", "participant.chat_id = session.chat_id AND session.sim_imsi = '" + str + "' AND " + ImDBHelper.SESSION_TABLE + "." + "is_group_chat" + " = 0 AND " + ImDBHelper.SESSION_TABLE + "." + ImContract.ImSession.IS_CHATBOT_ROLE + " = 1", new String[]{"uri"}, (String) null, (String) null);
        if (query == null) {
            if (query != null) {
                query.close();
            }
            return arrayList;
        }
        while (query.moveToNext()) {
            try {
                arrayList.add(ImsUri.parse(query.getString(query.getColumnIndexOrThrow("uri"))));
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        query.close();
        String str2 = LOG_TAG;
        Log.i(str2, "queryChatbotRoleUris: size=" + arrayList.size() + " " + IMSLog.checker(arrayList));
        return arrayList;
        throw th;
    }

    /* JADX INFO: finally extract failed */
    public Uri cloudInsertMessage(Uri uri, ContentValues contentValues) {
        String str = LOG_TAG;
        IMSLog.s(str, "cloudInsertMessage: " + contentValues);
        try {
            SQLiteDatabase writableDatabase = this.mImDBHelper.getWritableDatabase();
            writableDatabase.beginTransaction();
            try {
                long insert = writableDatabase.insert("message", (String) null, contentValues);
                if (insert != -1) {
                    Log.i(str, "cloudInsertMessage: rowId=" + insert);
                    setTransactionSuccessful(writableDatabase);
                } else {
                    Log.e(str, "cloudInsertMessage: SQL exception while inserting a message.");
                }
                endTransaction(writableDatabase);
                return ContentUris.withAppendedId(uri, insert);
            } catch (Throwable th) {
                endTransaction(writableDatabase);
                throw th;
            }
        } catch (SQLiteDiskIOException e) {
            String str2 = LOG_TAG;
            Log.e(str2, "SQLiteDiskIOException : " + e);
            return null;
        }
    }

    public int cloudUpdateMessage(String str, ContentValues contentValues) {
        Log.i(LOG_TAG, "updateCloudMessage");
        int i = 0;
        try {
            SQLiteDatabase writableDatabase = this.mImDBHelper.getWritableDatabase();
            writableDatabase.beginTransaction();
            try {
                i = writableDatabase.update("message", contentValues, "_id = ?", new String[]{String.valueOf(str)});
                setTransactionSuccessful(writableDatabase);
            } catch (SQLException e) {
                String str2 = LOG_TAG;
                Log.e(str2, "SQL exception while updating a message. " + e);
            } catch (Throwable th) {
                endTransaction(writableDatabase);
                throw th;
            }
            endTransaction(writableDatabase);
            return i;
        } catch (SQLiteDiskIOException e2) {
            String str3 = LOG_TAG;
            Log.e(str3, "SQLiteDiskIOException : " + e2);
            return 0;
        }
    }

    public int cloudUpdateSession(String str, ContentValues contentValues) {
        Log.i(LOG_TAG, "updateCloudSession");
        int i = 0;
        try {
            SQLiteDatabase writableDatabase = this.mImDBHelper.getWritableDatabase();
            writableDatabase.beginTransaction();
            try {
                i = writableDatabase.update(ImDBHelper.SESSION_TABLE, contentValues, "chat_id=?", new String[]{str});
                setTransactionSuccessful(writableDatabase);
            } catch (SQLException e) {
                String str2 = LOG_TAG;
                Log.e(str2, "SQL exception while updating a message. " + e);
            } catch (Throwable th) {
                endTransaction(writableDatabase);
                throw th;
            }
            endTransaction(writableDatabase);
            return i;
        } catch (SQLiteDiskIOException e2) {
            String str3 = LOG_TAG;
            Log.e(str3, "SQLiteDiskIOException : " + e2.toString());
            return 0;
        }
    }

    /* JADX INFO: finally extract failed */
    public Uri cloudInsertParticipant(Uri uri, ContentValues contentValues) {
        try {
            SQLiteDatabase writableDatabase = this.mImDBHelper.getWritableDatabase();
            writableDatabase.beginTransaction();
            try {
                long insert = writableDatabase.insert("participant", (String) null, contentValues);
                if (insert != -1) {
                    String str = LOG_TAG;
                    Log.i(str, "cloudInsertParticipant: rowId=" + insert);
                    setTransactionSuccessful(writableDatabase);
                } else {
                    Log.e(LOG_TAG, "cloudInsertParticipant: SQL exception while inserting a participant.");
                }
                endTransaction(writableDatabase);
                return ContentUris.withAppendedId(uri, insert);
            } catch (Throwable th) {
                endTransaction(writableDatabase);
                throw th;
            }
        } catch (SQLiteDiskIOException e) {
            String str2 = LOG_TAG;
            Log.e(str2, "SQLiteDiskIOException : " + e.toString());
            return null;
        }
    }

    public int cloudDeleteParticipant(String str) {
        delete("participant", "_id=" + str);
        return 0;
    }

    public int cloudUpdateParticipant(String str, ContentValues contentValues) {
        Log.i(LOG_TAG, "cloudUpdateParticipant");
        int i = 0;
        try {
            SQLiteDatabase writableDatabase = this.mImDBHelper.getWritableDatabase();
            writableDatabase.beginTransaction();
            try {
                i = writableDatabase.update("participant", contentValues, "_id=?", new String[]{str});
                setTransactionSuccessful(writableDatabase);
            } catch (SQLException e) {
                String str2 = LOG_TAG;
                Log.e(str2, "SQL exception while updating a message. " + e);
            } catch (Throwable th) {
                endTransaction(writableDatabase);
                throw th;
            }
            endTransaction(writableDatabase);
            return i;
        } catch (SQLiteDiskIOException e2) {
            String str3 = LOG_TAG;
            Log.e(str3, "SQLiteDiskIOException : " + e2.toString());
            return 0;
        }
    }

    /* JADX INFO: finally extract failed */
    public Uri cloudInsertNotification(Uri uri, ContentValues contentValues) {
        try {
            SQLiteDatabase writableDatabase = this.mImDBHelper.getWritableDatabase();
            writableDatabase.beginTransaction();
            try {
                long insert = writableDatabase.insert("notification", (String) null, contentValues);
                if (insert != -1) {
                    setTransactionSuccessful(writableDatabase);
                } else {
                    Log.e(LOG_TAG, "cloudInsertNotification: SQL exception while inserting a notification.");
                }
                endTransaction(writableDatabase);
                return ContentUris.withAppendedId(uri, insert);
            } catch (Throwable th) {
                endTransaction(writableDatabase);
                throw th;
            }
        } catch (SQLiteDiskIOException e) {
            String str = LOG_TAG;
            Log.e(str, "SQLiteDiskIOException : " + e.toString());
            return null;
        }
    }

    public int cloudUpdateNotification(String str, ContentValues contentValues, String str2, String[] strArr) {
        String str3 = LOG_TAG;
        Log.i(str3, "cloudUpdateNotification imdnId: " + str);
        int i = 0;
        try {
            SQLiteDatabase writableDatabase = this.mImDBHelper.getWritableDatabase();
            writableDatabase.beginTransaction();
            try {
                i = writableDatabase.update("notification", contentValues, str2, strArr);
                setTransactionSuccessful(writableDatabase);
            } catch (SQLException e) {
                String str4 = LOG_TAG;
                Log.e(str4, "SQL exception while updating a message. " + e);
            } catch (Throwable th) {
                endTransaction(writableDatabase);
                throw th;
            }
            endTransaction(writableDatabase);
            return i;
        } catch (SQLiteDiskIOException e2) {
            String str5 = LOG_TAG;
            Log.e(str5, "SQLiteDiskIOException : " + e2);
            return 0;
        }
    }

    public void updateDesiredNotificationStatusAsDisplayed(Collection<String> collection, int i, long j) {
        IMSLog.s(LOG_TAG, "updateDesiredNotificationStatusAsDisplayed: messages=" + collection + " status=" + i + " displayTime=" + j);
        ContentValues contentValues = new ContentValues();
        contentValues.put(ImContract.Message.DISPOSITION_NOTIFICATION_STATUS, Integer.valueOf(i));
        contentValues.put(ImContract.Message.DISPLAYED_TIMESTAMP, Long.valueOf(j));
        update("message", contentValues, "_id in (" + TextUtils.join(", ", collection) + ")");
        ContentValues contentValues2 = new ContentValues();
        contentValues2.put("status", Integer.valueOf(ImConstants.Status.READ.getId()));
        update("message", contentValues2, "_id in (" + TextUtils.join(", ", collection) + ") AND IFNULL(" + "status" + ", 4) != " + ImConstants.Status.FAILED.getId());
    }

    private void clearDeletedParticipants() {
        StringBuilder sb = new StringBuilder();
        sb.append("status in (");
        ImParticipant.Status status = ImParticipant.Status.DECLINED;
        sb.append(status.getId());
        sb.append(", ");
        sb.append(ImParticipant.Status.FAILED.getId());
        sb.append(")");
        String sb2 = sb.toString();
        if (this.mImModule.getRcsStrategy() != null && !this.mImModule.getRcsStrategy().boolSetting(RcsPolicySettings.RcsPolicy.REMOVE_FAILED_PARTICIPANT_GROUPCHAT)) {
            sb2 = "status = " + status.getId();
        }
        delete("participant", sb2);
    }

    /* access modifiers changed from: package-private */
    public void closeDB() {
        try {
            Log.i(LOG_TAG, "closeDB()");
            this.mImDBHelper.close();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public void updateChat(ChatData chatData, ImCacheAction imCacheAction) {
        if (imCacheAction == ImCacheAction.INSERTED) {
            insertSession(chatData);
        } else if (imCacheAction == ImCacheAction.UPDATED) {
            onSessionUpdated(chatData);
        } else if (imCacheAction == ImCacheAction.DELETED) {
            deleteSession(chatData);
        }
    }

    public void updateMessage(MessageBase messageBase, ImCacheAction imCacheAction) {
        if (imCacheAction == ImCacheAction.INSERTED) {
            insertMessage(messageBase);
            if (messageBase.getDirection() == ImDirection.OUTGOING) {
                insertMessageNotification(messageBase);
            }
            List<ImImdnRecRoute> imdnRecRouteList = messageBase.getImdnRecRouteList();
            if (imdnRecRouteList != null && !imdnRecRouteList.isEmpty()) {
                insertImdnRecRoute(imdnRecRouteList, messageBase.getId());
            }
        } else if (imCacheAction == ImCacheAction.UPDATED) {
            onMessageUpdated(messageBase);
            if (messageBase.getDirection() == ImDirection.OUTGOING) {
                onMessageNotificationUpdated(messageBase);
            }
        }
    }

    public void updateParticipant(ImParticipant imParticipant, ImCacheAction imCacheAction) {
        if (imCacheAction == ImCacheAction.INSERTED) {
            insertParticipant(imParticipant);
        } else if (imCacheAction == ImCacheAction.DELETED) {
            deleteParticipant(imParticipant);
        } else if (imCacheAction == ImCacheAction.UPDATED) {
            onParticipantUpdated(imParticipant);
        }
    }

    public void updateMessage(Collection<MessageBase> collection, ImCacheAction imCacheAction) {
        for (MessageBase updateMessage : collection) {
            updateMessage(updateMessage, imCacheAction);
        }
    }

    public void updateParticipant(Collection<ImParticipant> collection, ImCacheAction imCacheAction) {
        if (imCacheAction == ImCacheAction.INSERTED) {
            insertParticipant(collection);
        } else if (imCacheAction == ImCacheAction.DELETED) {
            deleteParticipant(collection);
        } else if (imCacheAction == ImCacheAction.UPDATED) {
            onParticipantUpdated(collection);
        }
    }

    private static class WhereClauseArgs {
        private final String[] mWhereArgs;
        private final String mWhereClause;

        WhereClauseArgs(String str, String[] strArr) {
            this.mWhereClause = str;
            this.mWhereArgs = strArr;
        }

        WhereClauseArgs(String str) {
            this(str, (String[]) null);
        }

        /* access modifiers changed from: package-private */
        public String getWhereClause() {
            return this.mWhereClause;
        }

        /* access modifiers changed from: package-private */
        public String[] getWhereArgs() {
            return this.mWhereArgs;
        }
    }
}
