package com.sec.internal.ims.servicemodules.im;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.ChatData;
import com.sec.internal.constants.ims.servicemodules.im.ChatMode;
import com.sec.internal.constants.ims.servicemodules.im.FileDisposition;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImContract;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.constants.ims.servicemodules.im.ImImdnRecRoute;
import com.sec.internal.constants.ims.servicemodules.im.ImParticipant;
import com.sec.internal.constants.ims.servicemodules.im.NotificationStatus;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage;
import com.sec.internal.ims.servicemodules.im.FtMessage;
import com.sec.internal.ims.servicemodules.im.ImMessage;
import java.util.ArrayList;
import java.util.Date;

public class ImDBHelper extends SQLiteOpenHelper {
    public static final String CHAT_MESSAGE_VIEW = "chatmessageview";
    public static final String CLOUD_MESSAGE_PROVIDER_NAME = "com.samsung.rcs.cmstore";
    public static final String CREATE_CHAT_MESSAGE_VIEW = "CREATE VIEW IF NOT EXISTS chatmessageview AS SELECT _id,_id AS msg_id,chat_id AS chat_id,replace(remote_uri, 'tel:', '') AS contact,body AS content,inserted_timestamp AS timestamp,sent_timestamp AS timestamp_sent,delivered_timestamp AS timestamp_delivered,displayed_timestamp AS timestamp_displayed,content_type AS mime_type,status AS status,null AS reason_code,status AS read_status,direction AS direction,delivered_timestamp AS expired_delivery FROM message WHERE is_filetransfer = 0 AND message_type < 3";
    public static final String CREATE_FILETRANSFER_VIEW = "CREATE VIEW IF NOT EXISTS filetransferview AS SELECT _id,_id AS ft_id,chat_id AS chat_id,replace(remote_uri, 'tel:', '') AS contact,file_path AS file,file_name AS filename,content_type AS mime_type,thumbnail_path AS fileicon,thumbnail_path AS fileicon_mime_type,direction AS direction,file_size AS filesize,bytes_transf AS transferred,inserted_timestamp AS timestamp,sent_timestamp AS timestamp_sent,delivered_timestamp AS timestamp_delivered,displayed_timestamp AS timestamp_displayed,state||';'||direction AS state,reason AS reason_code,status AS read_status,null AS file_expiration,null AS fileicon_expiration,delivered_timestamp AS expired_delivery FROM message WHERE is_filetransfer = 1";
    public static final String CREATE_IMDNRECROUTE_TABLE = "CREATE TABLE imdnrecroute(_id INTEGER PRIMARY KEY AUTOINCREMENT,message_id INTEGER DEFAULT 0,imdn_id TEXT,uri TEXT,alias TEXT);";
    public static final String CREATE_MESSAGE_TABLE = "CREATE TABLE message(_id INTEGER PRIMARY KEY AUTOINCREMENT,is_filetransfer INTEGER,direction INTEGER,chat_id TEXT NOT NULL,remote_uri TEXT,sender_alias TEXT,content_type TEXT,inserted_timestamp LONG,ext_info TEXT,body TEXT,suggestion TEXT,notification_disposition_mask INTEGER,notification_status INTEGER DEFAULT 0,disposition_notification_status INTEGER DEFAULT 0,sent_timestamp LONG,delivered_timestamp LONG,displayed_timestamp LONG,message_type INTEGER,message_isslm INTEGER,status INTEGER,not_displayed_counter INTEGER,imdn_message_id TEXT, imdn_original_to TEXT, conversation_id TEXT, contribution_id TEXT, file_path TEXT,file_name TEXT,file_size LONG,file_transfer_id TEXT,state INTEGER,reason INTEGER,bytes_transf LONG,ft_status INTEGER,thumbnail_path TEXT,is_resumable INTEGER,transfer_mech INTEGER DEFAULT 0,data_url TEXT,request_message_id TEXT,is_resizable INTEGER DEFAULT 0,is_broadcast_msg INTEGER DEFAULT 0,is_vm2txt_msg INTEGER DEFAULT 0,extra_ft INTEGER DEFAULT 0,flag_mask INTEGER DEFAULT 0,revocation_status INTEGER DEFAULT 0,sim_imsi TEXT DEFAULT '',device_id TEXT DEFAULT NULL,file_disposition INTEGER,playing_length INTEGER DEFAULT 0,maap_traffic_type TEXT DEFAULT NULL,reference_id TEXT DEFAULT NULL,reference_type TEXT DEFAULT NULL,reference_value TEXT DEFAULT NULL,messaging_tech INTEGER DEFAULT 0,creator TEXT DEFAULT NULL);";
    public static final String CREATE_NOTIFICATION_TABLE = "CREATE TABLE notification(id INTEGER PRIMARY KEY AUTOINCREMENT,message_id INTEGER DEFAULT 0,imdn_id TEXT, sender_uri TEXT,status INTEGER DEFAULT 0,timestamp LONG);";
    public static final String CREATE_PARTICIPANT_TABLE = "CREATE TABLE participant(_id INTEGER PRIMARY KEY AUTOINCREMENT,chat_id TEXT,status INTEGER,type INTEGER,uri TEXT,alias TEXT);";
    public static final String CREATE_SESSION_TABLE = "CREATE TABLE session(_id INTEGER PRIMARY KEY AUTOINCREMENT,chat_id TEXT,own_sim_imsi TEXT,own_group_alias TEXT,direction INTEGER, chat_type INTEGER, conversation_id TEXT, contribution_id TEXT, is_group_chat INTEGER,is_ft_group_chat INTEGER DEFAULT 1, status INTEGER,subject TEXT,is_muted INTEGER,max_participants_count INTEGER,imdn_notifications_availability INTEGER DEFAULT 1, session_uri TEXT DEFAULT NULL,is_broadcast_msg INTEGER, inserted_time_stamp LONG, preferred_uri TEXT DEFAULT NULL,is_reusable INTEGER DEFAULT 1,subject_participant TEXT DEFAULT NULL,subject_timestamp LONG,icon_path TEXT DEFAULT NULL,icon_participant TEXT DEFAULT NULL,icon_timestamp LONG,icon_uri TEXT DEFAULT NULL,sim_imsi TEXT DEFAULT NULL,is_chatbot_role INTEGER DEFAULT 0,chat_mode INTEGER DEFAULT 0,created_by TEXT DEFAULT NULL,invited_by TEXT DEFAULT NULL);";
    public static final String DATABASE_NAME = "rcsim.db";
    public static final String FILETRANSFER_VIEW = "filetransferview";
    public static final String IMDNRECROUTE_TABLE = "imdnrecroute";
    public static final String LOG_TAG = ImDBHelper.class.getSimpleName();
    public static final String MESSAGE_TABLE = "message";
    public static final String NOTIFICATION_TABLE = "notification";
    public static final String PARTICIPANT_TABLE = "participant";
    public static final String SESSION_TABLE = "session";

    public ImDBHelper(Context context, int i) {
        super(context, DATABASE_NAME, (SQLiteDatabase.CursorFactory) null, i);
    }

    public static ContentValues makeSessionRow(ChatData chatData) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("chat_id", chatData.getChatId());
        contentValues.put(ImContract.ImSession.OWN_PHONE_NUMBER, chatData.getOwnPhoneNum());
        contentValues.put("sim_imsi", chatData.getOwnIMSI());
        contentValues.put(ImContract.ImSession.OWN_GROUP_ALIAS, chatData.getOwnGroupAlias());
        contentValues.put("is_group_chat", Integer.valueOf(chatData.isGroupChat() ? 1 : 0));
        contentValues.put(ImContract.ImSession.CHAT_TYPE, Integer.valueOf(chatData.getChatType().getId()));
        contentValues.put("status", Integer.valueOf(chatData.getState().getId()));
        long j = 0;
        String str = null;
        if (chatData.getSubjectData() != null) {
            contentValues.put("subject", chatData.getSubjectData().getSubject());
            contentValues.put("subject_participant", chatData.getSubjectData().getParticipant() != null ? chatData.getSubjectData().getParticipant().toString() : null);
            contentValues.put("subject_timestamp", Long.valueOf(chatData.getSubjectData().getTimestamp() != null ? chatData.getSubjectData().getTimestamp().getTime() : 0));
        } else {
            contentValues.put("subject", chatData.getSubject());
            contentValues.put("subject_participant", (String) null);
            contentValues.put("subject_timestamp", 0L);
        }
        if (chatData.getIconData() != null) {
            contentValues.put(ImContract.ImSession.ICON_PATH, chatData.getIconData().getIconLocation());
            contentValues.put(ImContract.ImSession.ICON_PARTICIPANT, chatData.getIconData().getParticipant() != null ? chatData.getIconData().getParticipant().toString() : null);
            if (chatData.getIconData().getTimestamp() != null) {
                j = chatData.getIconData().getTimestamp().getTime();
            }
            contentValues.put(ImContract.ImSession.ICON_TIMESTAMP, Long.valueOf(j));
            contentValues.put(ImContract.ImSession.ICON_URI, chatData.getIconData().getIconUri());
        } else {
            contentValues.put(ImContract.ImSession.ICON_PATH, (String) null);
            contentValues.put(ImContract.ImSession.ICON_PARTICIPANT, (String) null);
            contentValues.put(ImContract.ImSession.ICON_TIMESTAMP, 0L);
            contentValues.put(ImContract.ImSession.ICON_URI, (String) null);
        }
        contentValues.put(ImContract.ImSession.INSERTED_TIMESTAMP, Long.valueOf(chatData.getInsertedTimeStamp()));
        contentValues.put(ImContract.ImSession.IS_MUTED, Boolean.valueOf(chatData.isMuted()));
        contentValues.put(ImContract.ImSession.MAX_PARTICIPANTS_COUNT, Integer.valueOf(chatData.getMaxParticipantsCount()));
        contentValues.put("direction", Integer.valueOf(chatData.getDirection().getId()));
        contentValues.put("conversation_id", chatData.getConversationId());
        contentValues.put("contribution_id", chatData.getContributionId());
        contentValues.put("session_uri", chatData.getSessionUri() != null ? chatData.getSessionUri().toString() : null);
        int i = 0;
        contentValues.put("is_broadcast_msg", Integer.valueOf(chatData.getChatType() == ChatData.ChatType.ONE_TO_MANY_CHAT ? 1 : 0));
        contentValues.put(ImContract.ImSession.IS_REUSABLE, Integer.valueOf(chatData.isReusable() ? 1 : 0));
        contentValues.put(ImContract.ImSession.IS_CHATBOT_ROLE, Integer.valueOf(chatData.isChatbotRole() ? 1 : 0));
        if (chatData.getChatMode() != null) {
            i = chatData.getChatMode().getId();
        }
        contentValues.put(ImContract.ImSession.CHAT_MODE, Integer.valueOf(i));
        contentValues.put("created_by", chatData.getCreatedBy() != null ? chatData.getCreatedBy().toString() : null);
        if (chatData.getInvitedBy() != null) {
            str = chatData.getInvitedBy().toString();
        }
        contentValues.put("invited_by", str);
        return contentValues;
    }

    public static ContentValues makeParticipantRow(ImParticipant imParticipant) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("chat_id", imParticipant.getChatId());
        contentValues.put("status", Integer.valueOf(imParticipant.getStatus().getId()));
        contentValues.put("type", Integer.valueOf(imParticipant.getType().getId()));
        contentValues.put("uri", imParticipant.getUri().toString());
        contentValues.put("alias", imParticipant.getUserAlias());
        return contentValues;
    }

    public static ChatData makeSession(Cursor cursor) {
        Cursor cursor2 = cursor;
        String string = cursor2.getString(cursor2.getColumnIndexOrThrow("session_uri"));
        long j = cursor2.getLong(cursor2.getColumnIndexOrThrow(ImContract.ImSession.INSERTED_TIMESTAMP));
        ImsUri parse = ImsUri.parse(cursor2.getString(cursor2.getColumnIndexOrThrow("subject_participant")));
        Date date = new Date(cursor2.getLong(cursor2.getColumnIndexOrThrow("subject_timestamp")));
        ImsUri parse2 = ImsUri.parse(cursor2.getString(cursor2.getColumnIndexOrThrow(ImContract.ImSession.ICON_PARTICIPANT)));
        Date date2 = new Date(cursor2.getLong(cursor2.getColumnIndexOrThrow(ImContract.ImSession.ICON_TIMESTAMP)));
        String string2 = cursor2.getString(cursor2.getColumnIndexOrThrow("created_by"));
        String string3 = cursor2.getString(cursor2.getColumnIndexOrThrow("invited_by"));
        int i = cursor2.getInt(cursor2.getColumnIndexOrThrow("_id"));
        String string4 = cursor2.getString(cursor2.getColumnIndexOrThrow("chat_id"));
        String string5 = cursor2.getString(cursor2.getColumnIndexOrThrow(ImContract.ImSession.OWN_PHONE_NUMBER));
        String string6 = cursor2.getString(cursor2.getColumnIndexOrThrow(ImContract.ImSession.OWN_GROUP_ALIAS));
        ChatData.ChatType fromId = ChatData.ChatType.fromId(cursor2.getInt(cursor2.getColumnIndexOrThrow(ImContract.ImSession.CHAT_TYPE)));
        ChatData.State fromId2 = ChatData.State.fromId(cursor2.getInt(cursor2.getColumnIndexOrThrow("status")));
        String string7 = cursor2.getString(cursor2.getColumnIndexOrThrow("subject"));
        boolean z = cursor2.getInt(cursor2.getColumnIndexOrThrow(ImContract.ImSession.IS_MUTED)) == 1;
        int i2 = cursor2.getInt(cursor2.getColumnIndexOrThrow(ImContract.ImSession.MAX_PARTICIPANTS_COUNT));
        ImDirection fromId3 = ImDirection.fromId(cursor2.getInt(cursor2.getColumnIndexOrThrow("direction")));
        Date date3 = date2;
        String string8 = cursor2.getString(cursor2.getColumnIndexOrThrow("conversation_id"));
        Date date4 = date;
        String string9 = cursor2.getString(cursor2.getColumnIndexOrThrow("contribution_id"));
        ImsUri parse3 = string != null ? ImsUri.parse(string) : null;
        String str = string9;
        boolean z2 = cursor2.getInt(cursor2.getColumnIndexOrThrow(ImContract.ImSession.IS_REUSABLE)) == 1;
        String string10 = cursor2.getString(cursor2.getColumnIndexOrThrow("sim_imsi"));
        String string11 = cursor2.getString(cursor2.getColumnIndexOrThrow(ImContract.ImSession.ICON_PATH));
        String string12 = cursor2.getString(cursor2.getColumnIndexOrThrow(ImContract.ImSession.ICON_URI));
        boolean z3 = z2;
        boolean z4 = true;
        if (cursor2.getInt(cursor2.getColumnIndexOrThrow(ImContract.ImSession.IS_CHATBOT_ROLE)) != 1) {
            z4 = false;
        }
        return new ChatData(i, string4, string5, string6, fromId, fromId2, string7, z, i2, fromId3, string8, str, parse3, z3, j, string10, parse, date4, string11, parse2, date3, string12, z4, ChatMode.fromId(cursor2.getInt(cursor2.getColumnIndexOrThrow(ImContract.ImSession.CHAT_MODE))), !TextUtils.isEmpty(string2) ? ImsUri.parse(string2) : null, !TextUtils.isEmpty(string3) ? ImsUri.parse(string3) : null);
    }

    public void onOpen(SQLiteDatabase sQLiteDatabase) {
        Log.i(LOG_TAG, "ImDBHelper onOpen()");
        super.onOpen(sQLiteDatabase);
    }

    public void onCreate(SQLiteDatabase sQLiteDatabase) {
        Log.i(LOG_TAG, "ImDBHelper onCreate()");
        sQLiteDatabase.execSQL(CREATE_SESSION_TABLE);
        sQLiteDatabase.execSQL(CREATE_MESSAGE_TABLE);
        sQLiteDatabase.execSQL(CREATE_PARTICIPANT_TABLE);
        sQLiteDatabase.execSQL(CREATE_IMDNRECROUTE_TABLE);
        sQLiteDatabase.execSQL(CREATE_NOTIFICATION_TABLE);
        createView(sQLiteDatabase);
    }

    public void onUpgrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
        Cursor rawQuery;
        String str = LOG_TAG;
        Log.i(str, "db upgrade: oldVersion=" + i + " newVersion=" + i2);
        ArrayList arrayList = new ArrayList();
        try {
            rawQuery = sQLiteDatabase.rawQuery("pragma table_info(session)", (String[]) null);
            if (rawQuery != null) {
                if (rawQuery.getCount() > 0) {
                    while (rawQuery.moveToNext()) {
                        String string = rawQuery.getString(rawQuery.getColumnIndex("name"));
                        if (!TextUtils.isEmpty(string)) {
                            arrayList.add(string);
                        }
                    }
                    if (!arrayList.contains(ImContract.ImSession.PREFERRED_URI)) {
                        Log.i(LOG_TAG, "column preferred_uri for ims6 does not exist");
                        i = 1;
                    }
                    rawQuery.close();
                    if (i == 17) {
                        try {
                            sQLiteDatabase.execSQL("ALTER TABLE message ADD COLUMN is_vm2txt_msg INTEGER DEFAULT 0");
                        } catch (SQLException unused) {
                            Log.i(LOG_TAG, "is_vm2txt_msg column already exists");
                        }
                        i = 18;
                    }
                    if (i == 18) {
                        try {
                            sQLiteDatabase.execSQL("ALTER TABLE message ADD COLUMN file_disposition INTEGER DEFAULT 0");
                            sQLiteDatabase.execSQL("ALTER TABLE message ADD COLUMN playing_length INTEGER DEFAULT 0");
                        } catch (SQLException unused2) {
                            Log.i(LOG_TAG, "file_disposition or playing_length columns already exists");
                        }
                        i = 19;
                    }
                    if (i == 19) {
                        try {
                            sQLiteDatabase.execSQL("ALTER TABLE imdnrecroute ADD COLUMN message_id INTEGER DEFAULT 0");
                        } catch (SQLException unused3) {
                            Log.i(LOG_TAG, "message_id column already exists");
                        }
                        try {
                            sQLiteDatabase.execSQL("ALTER TABLE notification ADD COLUMN message_id INTEGER DEFAULT 0");
                        } catch (SQLException unused4) {
                            Log.i(LOG_TAG, "message_id column already exists");
                        }
                        i = 20;
                    }
                    if (i == 20) {
                        try {
                            sQLiteDatabase.execSQL("ALTER TABLE message ADD COLUMN sim_imsi TEXT DEFAULT ''");
                        } catch (SQLException unused5) {
                            Log.i(LOG_TAG, "sim_imsi column already exists");
                        }
                        i = 21;
                    }
                    if (i == 21) {
                        try {
                            sQLiteDatabase.execSQL("ALTER TABLE message ADD COLUMN messaging_tech INTEGER DEFAULT 0");
                        } catch (SQLException unused6) {
                            Log.i(LOG_TAG, "messaging_tech column already exists");
                        }
                        i = 22;
                    }
                    if (i == 22) {
                        try {
                            sQLiteDatabase.execSQL("ALTER TABLE message ADD COLUMN suggestion TEXT DEFAULT NULL");
                        } catch (SQLException unused7) {
                            Log.i(LOG_TAG, "suggestion column already exists");
                        }
                        try {
                            sQLiteDatabase.execSQL("ALTER TABLE session ADD COLUMN sim_imsi TEXT DEFAULT NULL");
                        } catch (SQLException unused8) {
                            Log.i(LOG_TAG, "sim_imsi column already exists");
                        }
                        i = 23;
                    }
                    if (i == 23) {
                        try {
                            sQLiteDatabase.execSQL("ALTER TABLE session ADD COLUMN icon_uri TEXT DEFAULT NULL;");
                        } catch (SQLException unused9) {
                            Log.i(LOG_TAG, "icon_uri column already exists");
                        }
                        i = 24;
                    }
                    if (i == 24) {
                        try {
                            sQLiteDatabase.execSQL("ALTER TABLE session ADD COLUMN is_chatbot_role INTEGER DEFAULT 0;");
                        } catch (SQLException unused10) {
                            Log.i(LOG_TAG, "is_chatbot_role column already exists");
                        }
                        i = 25;
                    }
                    if (i == 25) {
                        try {
                            sQLiteDatabase.execSQL("ALTER TABLE message ADD COLUMN maap_traffic_type TEXT DEFAULT NULL");
                        } catch (SQLException unused11) {
                            Log.i(LOG_TAG, "maap_traffic_type column already exists");
                        }
                        i = 26;
                    }
                    if (i == 26) {
                        try {
                            sQLiteDatabase.execSQL("ALTER TABLE session ADD COLUMN chat_mode INTEGER DEFAULT 0;");
                        } catch (SQLException unused12) {
                            Log.i(LOG_TAG, "chat_mode column already exists");
                        }
                        i = 27;
                    }
                    if (i == 27) {
                        try {
                            sQLiteDatabase.execSQL("ALTER TABLE message ADD COLUMN reference_id TEXT DEFAULT NULL");
                        } catch (SQLException unused13) {
                            Log.i(LOG_TAG, "reference_id column already exists");
                        }
                        try {
                            sQLiteDatabase.execSQL("ALTER TABLE message ADD COLUMN reference_type TEXT DEFAULT NULL");
                        } catch (SQLException unused14) {
                            Log.i(LOG_TAG, "reference_type column already exists");
                        }
                        i = 28;
                    }
                    if (i == 28) {
                        try {
                            sQLiteDatabase.execSQL("ALTER TABLE message ADD COLUMN reference_value TEXT DEFAULT NULL");
                        } catch (SQLException unused15) {
                            Log.i(LOG_TAG, "reference_value column already exists");
                        }
                        i = 29;
                    }
                    if (i == 29) {
                        try {
                            sQLiteDatabase.execSQL("ALTER TABLE message ADD COLUMN creator TEXT DEFAULT NULL");
                        } catch (SQLException unused16) {
                            Log.i(LOG_TAG, "creator column already exists");
                        }
                        i = 30;
                    }
                    if (i == 30) {
                        try {
                            sQLiteDatabase.execSQL("ALTER TABLE session ADD COLUMN created_by TEXT DEFAULT NULL;");
                        } catch (SQLException unused17) {
                            Log.i(LOG_TAG, "created_by column already exists");
                        }
                        try {
                            sQLiteDatabase.execSQL("ALTER TABLE session ADD COLUMN invited_by TEXT DEFAULT NULL;");
                            return;
                        } catch (SQLException unused18) {
                            Log.i(LOG_TAG, "invited_by column already exists");
                            return;
                        }
                    } else {
                        return;
                    }
                }
            }
            Log.i(str, "SESSION_TABLE doesn't exist");
            if (rawQuery != null) {
                rawQuery.close();
                return;
            }
            return;
        } catch (SQLException e) {
            Log.e(LOG_TAG, "SQL Exception while querying pragma : " + e);
        } catch (Throwable th) {
            th.addSuppressed(th);
        }
        throw th;
    }

    public void onDowngrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
        String str = LOG_TAG;
        Log.i(str, "db downgrade : oldVersion=" + i + " newVersion=" + i2);
        updateTable(SESSION_TABLE, sQLiteDatabase);
        updateTable("message", sQLiteDatabase);
        updateTable("participant", sQLiteDatabase);
        updateTable(IMDNRECROUTE_TABLE, sQLiteDatabase);
        updateTable("notification", sQLiteDatabase);
    }

    private void createView(SQLiteDatabase sQLiteDatabase) {
        Log.i(LOG_TAG, "createView()");
        sQLiteDatabase.execSQL(CREATE_CHAT_MESSAGE_VIEW);
        sQLiteDatabase.execSQL(CREATE_FILETRANSFER_VIEW);
    }

    /* JADX WARNING: Unknown top exception splitter block from list: {B:39:0x0138=Splitter:B:39:0x0138, B:11:0x006b=Splitter:B:11:0x006b} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void updateTable(java.lang.String r12, android.database.sqlite.SQLiteDatabase r13) {
        /*
            r11 = this;
            java.lang.String r0 = ","
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            r1.append(r12)
            java.lang.String r2 = "_bkp"
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "pragma table_info("
            r2.append(r3)
            r2.append(r12)
            java.lang.String r3 = ")"
            r2.append(r3)
            java.lang.String r2 = r2.toString()
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            java.lang.String r4 = "SELECT "
            r3.<init>(r4)
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r6 = "INSERT INTO "
            r5.append(r6)
            r5.append(r12)
            java.lang.String r6 = "("
            r5.append(r6)
            java.lang.String r5 = r5.toString()
            r4.<init>(r5)
            java.util.ArrayList r5 = new java.util.ArrayList
            r5.<init>()
            r13.beginTransaction()
            r6 = 0
            android.database.Cursor r7 = r13.rawQuery(r2, r6)     // Catch:{ SQLException -> 0x016f }
            if (r7 == 0) goto L_0x0138
            int r8 = r7.getCount()     // Catch:{ all -> 0x0161 }
            if (r8 > 0) goto L_0x0063
            goto L_0x0138
        L_0x0063:
            boolean r8 = r7.moveToFirst()     // Catch:{ all -> 0x0161 }
            java.lang.String r9 = "name"
            if (r8 == 0) goto L_0x007c
        L_0x006b:
            int r8 = r7.getColumnIndex(r9)     // Catch:{ all -> 0x0161 }
            java.lang.String r8 = r7.getString(r8)     // Catch:{ all -> 0x0161 }
            r5.add(r8)     // Catch:{ all -> 0x0161 }
            boolean r8 = r7.moveToNext()     // Catch:{ all -> 0x0161 }
            if (r8 != 0) goto L_0x006b
        L_0x007c:
            java.lang.StringBuilder r8 = new java.lang.StringBuilder     // Catch:{ all -> 0x0161 }
            r8.<init>()     // Catch:{ all -> 0x0161 }
            java.lang.String r10 = "ALTER TABLE "
            r8.append(r10)     // Catch:{ all -> 0x0161 }
            r8.append(r12)     // Catch:{ all -> 0x0161 }
            java.lang.String r10 = " RENAME TO "
            r8.append(r10)     // Catch:{ all -> 0x0161 }
            r8.append(r1)     // Catch:{ all -> 0x0161 }
            java.lang.String r8 = r8.toString()     // Catch:{ all -> 0x0161 }
            r13.execSQL(r8)     // Catch:{ all -> 0x0161 }
            java.lang.String r11 = r11.createTable(r12)     // Catch:{ all -> 0x0161 }
            r13.execSQL(r11)     // Catch:{ all -> 0x0161 }
            android.database.Cursor r11 = r13.rawQuery(r2, r6)     // Catch:{ all -> 0x0161 }
            r2 = 1
            r6 = 0
            if (r11 == 0) goto L_0x00d9
            boolean r8 = r11.moveToFirst()     // Catch:{ all -> 0x00cf }
            if (r8 == 0) goto L_0x00d9
        L_0x00ad:
            int r8 = r11.getColumnIndex(r9)     // Catch:{ all -> 0x00cf }
            java.lang.String r8 = r11.getString(r8)     // Catch:{ all -> 0x00cf }
            boolean r10 = r5.contains(r8)     // Catch:{ all -> 0x00cf }
            if (r10 == 0) goto L_0x00c8
            r3.append(r8)     // Catch:{ all -> 0x00cf }
            r3.append(r0)     // Catch:{ all -> 0x00cf }
            r4.append(r8)     // Catch:{ all -> 0x00cf }
            r4.append(r0)     // Catch:{ all -> 0x00cf }
            r6 = r2
        L_0x00c8:
            boolean r8 = r11.moveToNext()     // Catch:{ all -> 0x00cf }
            if (r8 != 0) goto L_0x00ad
            goto L_0x00d9
        L_0x00cf:
            r0 = move-exception
            r11.close()     // Catch:{ all -> 0x00d4 }
            goto L_0x00d8
        L_0x00d4:
            r11 = move-exception
            r0.addSuppressed(r11)     // Catch:{ all -> 0x0161 }
        L_0x00d8:
            throw r0     // Catch:{ all -> 0x0161 }
        L_0x00d9:
            if (r11 == 0) goto L_0x00de
            r11.close()     // Catch:{ all -> 0x0161 }
        L_0x00de:
            if (r6 == 0) goto L_0x011d
            int r11 = r3.length()     // Catch:{ all -> 0x0161 }
            int r11 = r11 - r2
            r3.deleteCharAt(r11)     // Catch:{ all -> 0x0161 }
            java.lang.String r11 = " FROM "
            r3.append(r11)     // Catch:{ all -> 0x0161 }
            r3.append(r1)     // Catch:{ all -> 0x0161 }
            int r11 = r4.length()     // Catch:{ all -> 0x0161 }
            int r11 = r11 - r2
            r4.deleteCharAt(r11)     // Catch:{ all -> 0x0161 }
            java.lang.String r11 = ") "
            r4.append(r11)     // Catch:{ all -> 0x0161 }
            r4.append(r3)     // Catch:{ all -> 0x0161 }
            java.lang.String r11 = LOG_TAG     // Catch:{ all -> 0x0161 }
            java.lang.StringBuilder r0 = new java.lang.StringBuilder     // Catch:{ all -> 0x0161 }
            r0.<init>()     // Catch:{ all -> 0x0161 }
            java.lang.String r2 = "Update table: "
            r0.append(r2)     // Catch:{ all -> 0x0161 }
            r0.append(r4)     // Catch:{ all -> 0x0161 }
            java.lang.String r0 = r0.toString()     // Catch:{ all -> 0x0161 }
            android.util.Log.i(r11, r0)     // Catch:{ all -> 0x0161 }
            java.lang.String r11 = r4.toString()     // Catch:{ all -> 0x0161 }
            r13.execSQL(r11)     // Catch:{ all -> 0x0161 }
        L_0x011d:
            java.lang.StringBuilder r11 = new java.lang.StringBuilder     // Catch:{ all -> 0x0161 }
            r11.<init>()     // Catch:{ all -> 0x0161 }
            java.lang.String r0 = "DROP TABLE "
            r11.append(r0)     // Catch:{ all -> 0x0161 }
            r11.append(r1)     // Catch:{ all -> 0x0161 }
            java.lang.String r11 = r11.toString()     // Catch:{ all -> 0x0161 }
            r13.execSQL(r11)     // Catch:{ all -> 0x0161 }
            r13.setTransactionSuccessful()     // Catch:{ all -> 0x0161 }
            r7.close()     // Catch:{ SQLException -> 0x016f }
            goto L_0x018e
        L_0x0138:
            java.lang.String r0 = LOG_TAG     // Catch:{ all -> 0x0161 }
            java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch:{ all -> 0x0161 }
            r1.<init>()     // Catch:{ all -> 0x0161 }
            r1.append(r12)     // Catch:{ all -> 0x0161 }
            java.lang.String r2 = " doesn't exist"
            r1.append(r2)     // Catch:{ all -> 0x0161 }
            java.lang.String r1 = r1.toString()     // Catch:{ all -> 0x0161 }
            android.util.Log.i(r0, r1)     // Catch:{ all -> 0x0161 }
            java.lang.String r11 = r11.createTable(r12)     // Catch:{ all -> 0x0161 }
            r13.execSQL(r11)     // Catch:{ all -> 0x0161 }
            r13.setTransactionSuccessful()     // Catch:{ all -> 0x0161 }
            if (r7 == 0) goto L_0x015d
            r7.close()     // Catch:{ SQLException -> 0x016f }
        L_0x015d:
            r13.endTransaction()
            return
        L_0x0161:
            r11 = move-exception
            if (r7 == 0) goto L_0x016c
            r7.close()     // Catch:{ all -> 0x0168 }
            goto L_0x016c
        L_0x0168:
            r0 = move-exception
            r11.addSuppressed(r0)     // Catch:{ SQLException -> 0x016f }
        L_0x016c:
            throw r11     // Catch:{ SQLException -> 0x016f }
        L_0x016d:
            r11 = move-exception
            goto L_0x0192
        L_0x016f:
            r11 = move-exception
            java.lang.String r0 = LOG_TAG     // Catch:{ all -> 0x016d }
            java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch:{ all -> 0x016d }
            r1.<init>()     // Catch:{ all -> 0x016d }
            java.lang.String r2 = "SQL Exception while updating "
            r1.append(r2)     // Catch:{ all -> 0x016d }
            r1.append(r12)     // Catch:{ all -> 0x016d }
            java.lang.String r12 = ": "
            r1.append(r12)     // Catch:{ all -> 0x016d }
            r1.append(r11)     // Catch:{ all -> 0x016d }
            java.lang.String r11 = r1.toString()     // Catch:{ all -> 0x016d }
            android.util.Log.e(r0, r11)     // Catch:{ all -> 0x016d }
        L_0x018e:
            r13.endTransaction()
            return
        L_0x0192:
            r13.endTransaction()
            throw r11
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.ImDBHelper.updateTable(java.lang.String, android.database.sqlite.SQLiteDatabase):void");
    }

    private String createTable(String str) {
        str.hashCode();
        char c = 65535;
        switch (str.hashCode()) {
            case 595233003:
                if (str.equals("notification")) {
                    c = 0;
                    break;
                }
                break;
            case 767422259:
                if (str.equals("participant")) {
                    c = 1;
                    break;
                }
                break;
            case 954925063:
                if (str.equals("message")) {
                    c = 2;
                    break;
                }
                break;
            case 1984987798:
                if (str.equals(SESSION_TABLE)) {
                    c = 3;
                    break;
                }
                break;
            case 2145035879:
                if (str.equals(IMDNRECROUTE_TABLE)) {
                    c = 4;
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
                return CREATE_NOTIFICATION_TABLE;
            case 1:
                return CREATE_PARTICIPANT_TABLE;
            case 2:
                return CREATE_MESSAGE_TABLE;
            case 3:
                return CREATE_SESSION_TABLE;
            case 4:
                return CREATE_IMDNRECROUTE_TABLE;
            default:
                return "";
        }
    }

    public ContentValues makeImMessageRow(ImMessage imMessage) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("chat_id", imMessage.getChatId());
        contentValues.put(ImContract.ChatItem.IS_FILE_TRANSFER, 0);
        contentValues.put("remote_uri", imMessage.getRemoteUri() != null ? imMessage.getRemoteUri().toString() : null);
        contentValues.put(ImContract.ChatItem.USER_ALIAS, imMessage.getUserAlias());
        contentValues.put("body", imMessage.getBody());
        contentValues.put(ImContract.Message.SUGGESTION, imMessage.getSuggestion());
        contentValues.put(ImContract.Message.NOTIFICATION_DISPOSITION_MASK, Integer.valueOf(NotificationStatus.encode(imMessage.getDispositionNotification())));
        contentValues.put("notification_status", Integer.valueOf(imMessage.getNotificationStatus().getId()));
        contentValues.put(ImContract.Message.DISPOSITION_NOTIFICATION_STATUS, Integer.valueOf(imMessage.getDesiredNotificationStatus().getId()));
        contentValues.put(ImContract.ChatItem.INSERTED_TIMESTAMP, Long.valueOf(imMessage.getInsertedTimestamp()));
        contentValues.put(ImContract.Message.SENT_TIMESTAMP, Long.valueOf(imMessage.getSentTimestamp()));
        contentValues.put(ImContract.ChatItem.DELIVERED_TIMESTAMP, Long.valueOf(imMessage.getDeliveredTimestamp()));
        if (imMessage.getExtInfo() != null) {
            contentValues.put(ImContract.ChatItem.EXT_INFO, imMessage.getExtInfo());
        }
        contentValues.put(ImContract.Message.DISPLAYED_TIMESTAMP, imMessage.getDisplayedTimestamp());
        contentValues.put("message_type", Integer.valueOf(imMessage.getType().getId()));
        contentValues.put(ImContract.Message.MESSAGE_ISSLM, Boolean.valueOf(imMessage.getIsSlmSvcMsg()));
        contentValues.put("status", Integer.valueOf(imMessage.getStatus().getId()));
        contentValues.put("direction", Integer.valueOf(imMessage.getDirection().getId()));
        contentValues.put("content_type", imMessage.getContentType());
        contentValues.put(ImContract.Message.NOT_DISPLAYED_COUNTER, Integer.valueOf(imMessage.getNotDisplayedCounter()));
        contentValues.put("imdn_message_id", imMessage.getImdnId());
        contentValues.put(ImContract.Message.IMDN_ORIGINAL_TO, imMessage.getImdnOriginalTo());
        contentValues.put("request_message_id", imMessage.getRequestMessageId());
        contentValues.put("is_broadcast_msg", Integer.valueOf(imMessage.isBroadcastMsg() ? 1 : 0));
        contentValues.put(ImContract.Message.IS_VM2TXT_MSG, Integer.valueOf(imMessage.isVM2TextMsg() ? 1 : 0));
        contentValues.put("conversation_id", imMessage.getConversationId());
        contentValues.put("contribution_id", imMessage.getContributionId());
        contentValues.put("device_id", imMessage.getDeviceId());
        contentValues.put(ImContract.Message.FLAG_MASK, Integer.valueOf(imMessage.getFlagMask()));
        contentValues.put(ImContract.Message.REVOCATION_STATUS, Integer.valueOf(imMessage.getRevocationStatus().getId()));
        contentValues.put("sim_imsi", imMessage.getOwnIMSI());
        contentValues.put("maap_traffic_type", imMessage.getMaapTrafficType());
        contentValues.put(ImContract.Message.MESSAGING_TECH, Integer.valueOf(imMessage.getMessagingTech().getId()));
        contentValues.put(ImContract.Message.REFERENCE_ID, imMessage.getReferenceImdnId());
        contentValues.put(ImContract.Message.REFERENCE_TYPE, imMessage.getReferenceType());
        contentValues.put(ImContract.Message.REFERENCE_VALUE, imMessage.getReferenceValue());
        return contentValues;
    }

    public ContentValues makeFtMessageRow(FtMessage ftMessage) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ImContract.ChatItem.IS_FILE_TRANSFER, 1);
        contentValues.put("direction", Integer.valueOf(ftMessage.getDirection().getId()));
        contentValues.put("chat_id", ftMessage.getChatId());
        contentValues.put("remote_uri", ftMessage.getRemoteUri() != null ? ftMessage.getRemoteUri().toString() : null);
        contentValues.put(ImContract.ChatItem.USER_ALIAS, ftMessage.getUserAlias());
        contentValues.put("content_type", ftMessage.getContentType());
        contentValues.put(ImContract.ChatItem.INSERTED_TIMESTAMP, Long.valueOf(ftMessage.getInsertedTimestamp()));
        contentValues.put(ImContract.ChatItem.DELIVERED_TIMESTAMP, Long.valueOf(ftMessage.getDeliveredTimestamp()));
        if (ftMessage.getExtInfo() != null) {
            contentValues.put(ImContract.ChatItem.EXT_INFO, ftMessage.getExtInfo());
        }
        contentValues.put(ImContract.CsSession.FILE_PATH, ftMessage.getFilePath());
        contentValues.put(ImContract.CsSession.FILE_NAME, ftMessage.getFileName());
        contentValues.put(ImContract.CsSession.FILE_SIZE, Long.valueOf(ftMessage.getFileSize()));
        if (ftMessage.getFileDisposition() != null) {
            contentValues.put("file_disposition", Integer.valueOf(ftMessage.getFileDisposition().toInt()));
        }
        contentValues.put("playing_length", Integer.valueOf(ftMessage.getPlayingLength()));
        contentValues.put(ImContract.CsSession.FILE_TRANSFER_ID, ftMessage.getFileTransferId());
        contentValues.put("state", Integer.valueOf(ftMessage.getStateId()));
        contentValues.put("reason", Integer.valueOf(ftMessage.getReasonId()));
        contentValues.put(ImContract.CsSession.BYTES_TRANSFERRED, Long.valueOf(ftMessage.getTransferredBytes()));
        contentValues.put(ImContract.CsSession.STATUS, Integer.valueOf(ftMessage.getStatus().getId()));
        contentValues.put(ImContract.CsSession.THUMBNAIL_PATH, ftMessage.getThumbnailPath());
        contentValues.put(ImContract.CsSession.IS_RESUMABLE, Integer.valueOf(ftMessage.getResumableOptionCode()));
        contentValues.put(ImContract.CsSession.TRANSFER_MECH, Integer.valueOf(ftMessage.getTransferMech()));
        if (ftMessage instanceof FtHttpIncomingMessage) {
            contentValues.put(ImContract.CsSession.DATA_URL, ((FtHttpIncomingMessage) ftMessage).getDataUrl());
        }
        contentValues.put(ImContract.Message.NOTIFICATION_DISPOSITION_MASK, Integer.valueOf(NotificationStatus.encode(ftMessage.getDispositionNotification())));
        contentValues.put("notification_status", Integer.valueOf(ftMessage.getNotificationStatus().getId()));
        contentValues.put(ImContract.Message.DISPOSITION_NOTIFICATION_STATUS, Integer.valueOf(ftMessage.getDesiredNotificationStatus().getId()));
        contentValues.put("imdn_message_id", ftMessage.getImdnId());
        contentValues.put(ImContract.Message.MESSAGE_ISSLM, Boolean.valueOf(ftMessage.getIsSlmSvcMsg()));
        contentValues.put(ImContract.Message.DISPLAYED_TIMESTAMP, ftMessage.getDisplayedTimestamp());
        contentValues.put(ImContract.Message.NOT_DISPLAYED_COUNTER, Integer.valueOf(ftMessage.getNotDisplayedCounter()));
        contentValues.put("request_message_id", ftMessage.getRequestMessageId());
        contentValues.put("is_resizable", Boolean.valueOf(ftMessage.getIsResizable()));
        contentValues.put("body", ftMessage.getBody());
        contentValues.put(ImContract.Message.SENT_TIMESTAMP, Long.valueOf(ftMessage.getSentTimestamp()));
        contentValues.put("message_type", Integer.valueOf(ftMessage.getType().getId()));
        contentValues.put("is_broadcast_msg", Integer.valueOf(ftMessage.isBroadcastMsg() ? 1 : 0));
        contentValues.put(ImContract.CsSession.EXTRA_FT, Boolean.valueOf(ftMessage.mExtraFt));
        contentValues.put("conversation_id", ftMessage.getConversationId());
        contentValues.put("contribution_id", ftMessage.getContributionId());
        contentValues.put("device_id", ftMessage.getDeviceId());
        contentValues.put(ImContract.Message.FLAG_MASK, Integer.valueOf(ftMessage.getFlagMask()));
        contentValues.put(ImContract.Message.REVOCATION_STATUS, Integer.valueOf(ftMessage.getRevocationStatus().getId()));
        contentValues.put("sim_imsi", ftMessage.getOwnIMSI());
        contentValues.put("maap_traffic_type", ftMessage.getMaapTrafficType());
        contentValues.put(ImContract.Message.MESSAGING_TECH, Integer.valueOf(ftMessage.getMessagingTech().getId()));
        return contentValues;
    }

    public ContentValues makeImdnRecRouteRow(ImImdnRecRoute imImdnRecRoute) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("message_id", Integer.valueOf(imImdnRecRoute.getMessageId()));
        contentValues.put("imdn_id", imImdnRecRoute.getImdnMsgId());
        contentValues.put("uri", imImdnRecRoute.getRecordRouteUri());
        contentValues.put("alias", imImdnRecRoute.getRecordRouteDispName());
        return contentValues;
    }

    public ContentValues makeMessageNotificationRow(MessageBase messageBase, String str) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("message_id", Integer.valueOf(messageBase.getId()));
        contentValues.put("imdn_id", messageBase.getImdnId());
        contentValues.put(ImContract.MessageNotification.SENDER_URI, str);
        contentValues.put("timestamp", Long.valueOf(messageBase.getSentTimestamp()));
        return contentValues;
    }

    public ContentValues makeMessageNotificationUpdateRow(long j, int i) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("timestamp", Long.valueOf(j));
        contentValues.put("status", Integer.valueOf(i));
        return contentValues;
    }

    public ImMessage makeImMessage(Cursor cursor, ImModule imModule) {
        String string = cursor.getString(cursor.getColumnIndexOrThrow("remote_uri"));
        String string2 = cursor.getString(cursor.getColumnIndexOrThrow("content_type"));
        String string3 = cursor.getString(cursor.getColumnIndexOrThrow("body"));
        int phoneIdByIMSI = imModule.getPhoneIdByIMSI(cursor.getString(cursor.getColumnIndexOrThrow("sim_imsi")));
        ImMessage.Builder builder = (ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ImMessage.builder().module(imModule)).imsService(ImsRegistry.getHandlerFactory().getImHandler())).slmService(ImsRegistry.getHandlerFactory().getSlmHandler())).listener(imModule.getImProcessor()).config(imModule.getImConfig(phoneIdByIMSI))).uriGenerator(imModule.getUriGenerator(phoneIdByIMSI))).id(cursor.getInt(cursor.getColumnIndexOrThrow("_id")))).direction(ImDirection.fromId(cursor.getInt(cursor.getColumnIndexOrThrow("direction"))))).chatId(cursor.getString(cursor.getColumnIndexOrThrow("chat_id")))).remoteUri(string != null ? ImsUri.parse(string) : null)).body(string3)).suggestion(cursor.getString(cursor.getColumnIndexOrThrow(ImContract.Message.SUGGESTION)))).userAlias(cursor.getString(cursor.getColumnIndexOrThrow(ImContract.ChatItem.USER_ALIAS)))).contentType(string2)).insertedTimestamp(cursor.getLong(cursor.getColumnIndexOrThrow(ImContract.ChatItem.INSERTED_TIMESTAMP)))).status(ImConstants.Status.fromId(cursor.getInt(cursor.getColumnIndexOrThrow("status"))))).dispNotification(NotificationStatus.decode(cursor.getInt(cursor.getColumnIndexOrThrow(ImContract.Message.NOTIFICATION_DISPOSITION_MASK))))).notificationStatus(NotificationStatus.fromId(cursor.getInt(cursor.getColumnIndexOrThrow("notification_status"))))).desiredNotificationStatus(NotificationStatus.fromId(cursor.getInt(cursor.getColumnIndexOrThrow(ImContract.Message.DISPOSITION_NOTIFICATION_STATUS))))).sentTimestamp(cursor.getLong(cursor.getColumnIndexOrThrow(ImContract.Message.SENT_TIMESTAMP)))).deliveredTimestamp(cursor.getLong(cursor.getColumnIndexOrThrow(ImContract.ChatItem.DELIVERED_TIMESTAMP)))).displayedTimestamp(cursor.getLong(cursor.getColumnIndexOrThrow(ImContract.Message.DISPLAYED_TIMESTAMP)))).type(ImConstants.Type.fromId(cursor.getInt(cursor.getColumnIndexOrThrow("message_type"))));
        boolean z = false;
        ImMessage.Builder builder2 = (ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) builder.isSlmSvcMsg(cursor.getInt(cursor.getColumnIndexOrThrow(ImContract.Message.MESSAGE_ISSLM)) == 1)).imdnId(cursor.getString(cursor.getColumnIndexOrThrow("imdn_message_id")))).imdnIdOriginalTo(cursor.getString(cursor.getColumnIndexOrThrow(ImContract.Message.IMDN_ORIGINAL_TO)))).notDisplayedCounter(cursor.getInt(cursor.getColumnIndexOrThrow(ImContract.Message.NOT_DISPLAYED_COUNTER)))).requestMessageId(cursor.getString(cursor.getColumnIndexOrThrow("request_message_id")))).isBroadcastMsg(cursor.getInt(cursor.getColumnIndexOrThrow("is_broadcast_msg")) == 1);
        if (cursor.getInt(cursor.getColumnIndexOrThrow(ImContract.Message.IS_VM2TXT_MSG)) == 1) {
            z = true;
        }
        return ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) builder2.isVM2TextMsg(z)).mnoStrategy(imModule.getRcsStrategy(phoneIdByIMSI))).conversationId(cursor.getString(cursor.getColumnIndexOrThrow("conversation_id")))).contributionId(cursor.getString(cursor.getColumnIndexOrThrow("contribution_id")))).deviceId(cursor.getString(cursor.getColumnIndexOrThrow("device_id")))).flagMask(cursor.getInt(cursor.getColumnIndexOrThrow(ImContract.Message.FLAG_MASK)))).revocationStatus(ImConstants.RevocationStatus.fromId(cursor.getInt(cursor.getColumnIndexOrThrow(ImContract.Message.REVOCATION_STATUS))))).simIMSI(cursor.getString(cursor.getColumnIndexOrThrow("sim_imsi")))).maapTrafficType(cursor.getString(cursor.getColumnIndexOrThrow("maap_traffic_type")))).messagingTech(ImConstants.MessagingTech.fromId(cursor.getInt(cursor.getColumnIndexOrThrow(ImContract.Message.MESSAGING_TECH))))).referenceImdnId(cursor.getString(cursor.getColumnIndexOrThrow(ImContract.Message.REFERENCE_ID)))).referenceType(cursor.getString(cursor.getColumnIndexOrThrow(ImContract.Message.REFERENCE_TYPE)))).referenceValue(cursor.getString(cursor.getColumnIndexOrThrow(ImContract.Message.REFERENCE_VALUE)))).build();
    }

    public FtMessage makeFtMessage(Cursor cursor, ImModule imModule) {
        FtMessage.Builder builder;
        int i = cursor.getInt(cursor.getColumnIndexOrThrow(ImContract.CsSession.TRANSFER_MECH));
        ImDirection fromId = ImDirection.fromId(cursor.getInt(cursor.getColumnIndexOrThrow("direction")));
        String string = cursor.getString(cursor.getColumnIndexOrThrow("remote_uri"));
        int phoneIdByIMSI = imModule.getPhoneIdByIMSI(cursor.getString(cursor.getColumnIndexOrThrow("sim_imsi")));
        if (i == 0) {
            builder = FtMsrpMessage.builder();
        } else if (fromId == ImDirection.OUTGOING) {
            builder = FtHttpOutgoingMessage.builder();
        } else {
            builder = FtHttpIncomingMessage.builder();
        }
        FileDisposition fileDisposition = null;
        boolean z = false;
        FtMessage.Builder builder2 = (FtMessage.Builder) ((FtMessage.Builder) ((FtMessage.Builder) ((FtMessage.Builder) ((FtMessage.Builder) ((FtMessage.Builder) ((FtMessage.Builder) ((FtMessage.Builder) ((FtMessage.Builder) ((FtMessage.Builder) ((FtMessage.Builder) ((FtMessage.Builder) ((FtMessage.Builder) ((FtMessage.Builder) ((FtMessage.Builder) ((FtMessage.Builder) ((FtMessage.Builder) ((FtMessage.Builder) ((FtMessage.Builder) ((FtMessage.Builder) ((FtMessage.Builder) ((FtMessage.Builder) ((FtMessage.Builder) ((FtMessage.Builder) ((FtMessage.Builder) ((FtMessage.Builder) ((FtMessage.Builder) ((FtMessage.Builder) ((FtMessage.Builder) builder.module(imModule)).imsService(ImsRegistry.getHandlerFactory().getImHandler())).slmService(ImsRegistry.getHandlerFactory().getSlmHandler())).listener(imModule.getFtProcessor()).looper(imModule.getLooper()).config(imModule.getImConfig(phoneIdByIMSI))).thumbnailTool(imModule.getFtProcessor().getThumbnailTool())).uriGenerator(imModule.getUriGenerator(phoneIdByIMSI))).id(cursor.getInt(cursor.getColumnIndexOrThrow("_id")))).direction(ImDirection.fromId(cursor.getInt(cursor.getColumnIndexOrThrow("direction"))))).chatId(cursor.getString(cursor.getColumnIndexOrThrow("chat_id")))).remoteUri(string != null ? ImsUri.parse(string) : null)).userAlias(cursor.getString(cursor.getColumnIndexOrThrow(ImContract.ChatItem.USER_ALIAS)))).contentType(cursor.getString(cursor.getColumnIndexOrThrow("content_type")))).sentTimestamp(cursor.getLong(cursor.getColumnIndexOrThrow(ImContract.Message.SENT_TIMESTAMP)))).insertedTimestamp(cursor.getLong(cursor.getColumnIndexOrThrow(ImContract.ChatItem.INSERTED_TIMESTAMP)))).deliveredTimestamp(cursor.getLong(cursor.getColumnIndexOrThrow(ImContract.ChatItem.DELIVERED_TIMESTAMP)))).displayedTimestamp(cursor.getLong(cursor.getColumnIndexOrThrow(ImContract.Message.DISPLAYED_TIMESTAMP)))).type(ImConstants.Type.fromId(cursor.getInt(cursor.getColumnIndexOrThrow("message_type"))))).isSlmSvcMsg(cursor.getInt(cursor.getColumnIndexOrThrow(ImContract.Message.MESSAGE_ISSLM)) == 1)).filePath(cursor.getString(cursor.getColumnIndexOrThrow(ImContract.CsSession.FILE_PATH))).fileName(cursor.getString(cursor.getColumnIndexOrThrow(ImContract.CsSession.FILE_NAME))).fileSize((long) cursor.getInt(cursor.getColumnIndexOrThrow(ImContract.CsSession.FILE_SIZE))).fileTransferId(cursor.getString(cursor.getColumnIndexOrThrow(ImContract.CsSession.FILE_TRANSFER_ID))).transferredBytes((long) cursor.getInt(cursor.getColumnIndexOrThrow(ImContract.CsSession.BYTES_TRANSFERRED))).setCancelReason(cursor.getInt(cursor.getColumnIndexOrThrow("reason"))).status(ImConstants.Status.fromId(cursor.getInt(cursor.getColumnIndexOrThrow(ImContract.CsSession.STATUS))))).thumbnailPath(cursor.getString(cursor.getColumnIndexOrThrow(ImContract.CsSession.THUMBNAIL_PATH))).setResumableOptions(cursor.getInt(cursor.getColumnIndexOrThrow(ImContract.CsSession.IS_RESUMABLE))).imdnId(cursor.getString(cursor.getColumnIndexOrThrow("imdn_message_id")))).imdnIdOriginalTo(cursor.getString(cursor.getColumnIndexOrThrow(ImContract.Message.IMDN_ORIGINAL_TO)))).dispNotification(NotificationStatus.decode(cursor.getInt(cursor.getColumnIndexOrThrow(ImContract.Message.NOTIFICATION_DISPOSITION_MASK))))).notificationStatus(NotificationStatus.fromId(cursor.getInt(cursor.getColumnIndexOrThrow("notification_status"))))).desiredNotificationStatus(NotificationStatus.fromId(cursor.getInt(cursor.getColumnIndexOrThrow(ImContract.Message.DISPOSITION_NOTIFICATION_STATUS))))).setState(cursor.getInt(cursor.getColumnIndexOrThrow("state"))).notDisplayedCounter(cursor.getInt(cursor.getColumnIndexOrThrow(ImContract.Message.NOT_DISPLAYED_COUNTER)))).requestMessageId(cursor.getString(cursor.getColumnIndexOrThrow("request_message_id")))).isResizable(cursor.getInt(cursor.getColumnIndexOrThrow("is_resizable")) == 1).isBroadcastMsg(cursor.getInt(cursor.getColumnIndexOrThrow("is_broadcast_msg")) == 1)).body(cursor.getString(cursor.getColumnIndexOrThrow("body")))).mnoStrategy(imModule.getRcsStrategy(phoneIdByIMSI));
        if (cursor.getInt(cursor.getColumnIndexOrThrow(ImContract.CsSession.EXTRA_FT)) == 1) {
            z = true;
        }
        FtMessage.Builder builder3 = (FtMessage.Builder) ((FtMessage.Builder) ((FtMessage.Builder) ((FtMessage.Builder) ((FtMessage.Builder) builder2.extraFt(z).conversationId(cursor.getString(cursor.getColumnIndexOrThrow("conversation_id")))).contributionId(cursor.getString(cursor.getColumnIndexOrThrow("contribution_id")))).deviceId(cursor.getString(cursor.getColumnIndexOrThrow("device_id")))).flagMask(cursor.getInt(cursor.getColumnIndexOrThrow(ImContract.Message.FLAG_MASK)))).revocationStatus(ImConstants.RevocationStatus.fromId(cursor.getInt(cursor.getColumnIndexOrThrow(ImContract.Message.REVOCATION_STATUS))));
        if (!cursor.isNull(cursor.getColumnIndexOrThrow("file_disposition"))) {
            fileDisposition = FileDisposition.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("file_disposition")));
        }
        ((FtMessage.Builder) ((FtMessage.Builder) builder3.setFileDisposition(fileDisposition).setPlayingLength(cursor.getInt(cursor.getColumnIndexOrThrow("playing_length"))).simIMSI(cursor.getString(cursor.getColumnIndexOrThrow("sim_imsi")))).maapTrafficType(cursor.getString(cursor.getColumnIndexOrThrow("maap_traffic_type")))).messagingTech(ImConstants.MessagingTech.fromId(cursor.getInt(cursor.getColumnIndexOrThrow(ImContract.Message.MESSAGING_TECH))));
        if (builder instanceof FtHttpIncomingMessage.Builder) {
            ((FtHttpIncomingMessage.Builder) builder).dataUrl(cursor.getString(cursor.getColumnIndexOrThrow(ImContract.CsSession.DATA_URL)));
        }
        return builder.build();
    }

    public ImParticipant makeParticipant(Cursor cursor) {
        return new ImParticipant(cursor.getInt(cursor.getColumnIndexOrThrow("_id")), cursor.getString(cursor.getColumnIndexOrThrow("chat_id")), ImParticipant.Status.fromId(cursor.getInt(cursor.getColumnIndexOrThrow("status"))), ImParticipant.Type.fromId(cursor.getInt(cursor.getColumnIndexOrThrow("type"))), ImsUri.parse(cursor.getString(cursor.getColumnIndexOrThrow("uri"))), cursor.getString(cursor.getColumnIndexOrThrow("alias")));
    }

    public ImImdnRecRoute makeImdnRecRoute(Cursor cursor) {
        return new ImImdnRecRoute(cursor.getInt(cursor.getColumnIndexOrThrow("_id")), cursor.getInt(cursor.getColumnIndexOrThrow("message_id")), cursor.getString(cursor.getColumnIndexOrThrow("imdn_id")), cursor.getString(cursor.getColumnIndexOrThrow("uri")), cursor.getString(cursor.getColumnIndexOrThrow("alias")));
    }
}
