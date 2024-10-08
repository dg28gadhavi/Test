package com.sec.internal.ims.cmstore.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImContract;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.ims.rcs.util.RcsUtils;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;

public class CursorContentValueTranslator {
    private static final String LOG_TAG = "CursorContentValueTranslator";

    public static ArrayList<ContentValues> convertRCSimfttoCV(Cursor cursor, Context context, int i) {
        ArrayList<ContentValues> arrayList = new ArrayList<>();
        boolean isMcsSupported = CmsUtil.isMcsSupported(context, i);
        do {
            try {
                String string = cursor.getString(cursor.getColumnIndexOrThrow("body"));
                if (!isMcsSupported || !isRevokedMessage(cursor)) {
                    if (TextUtils.isEmpty(string)) {
                        String str = LOG_TAG;
                        Log.d(str, "covertRCSimfttoCV: direction: " + cursor.getInt(cursor.getColumnIndexOrThrow("direction")) + "covertRCSimfttoCV: status: " + cursor.getInt(cursor.getColumnIndexOrThrow(ImContract.CsSession.STATUS)));
                        if ((cursor.getInt(cursor.getColumnIndexOrThrow("direction")) == ImDirection.INCOMING.getId() || cursor.getInt(cursor.getColumnIndexOrThrow("direction")) == ImDirection.OUTGOING.getId()) && (cursor.getInt(cursor.getColumnIndexOrThrow(ImContract.CsSession.STATUS)) == ImConstants.Status.SENT.getId() || cursor.getInt(cursor.getColumnIndexOrThrow(ImContract.CsSession.STATUS)) == ImConstants.Status.UNREAD.getId() || cursor.getInt(cursor.getColumnIndexOrThrow(ImContract.CsSession.STATUS)) == ImConstants.Status.READ.getId() || (isMcsSupported && (cursor.getInt(cursor.getColumnIndexOrThrow(ImContract.CsSession.STATUS)) == ImConstants.Status.TO_SEND.getId() || cursor.getInt(cursor.getColumnIndexOrThrow(ImContract.CsSession.STATUS)) == ImConstants.Status.SENDING.getId())))) {
                            copyRcsMessageCursor(cursor, arrayList, isMcsSupported);
                        }
                    } else if ((cursor.getInt(cursor.getColumnIndexOrThrow("direction")) == ImDirection.INCOMING.getId() || cursor.getInt(cursor.getColumnIndexOrThrow("direction")) == ImDirection.OUTGOING.getId()) && (cursor.getInt(cursor.getColumnIndexOrThrow("status")) == ImConstants.Status.SENT.getId() || cursor.getInt(cursor.getColumnIndexOrThrow("status")) == ImConstants.Status.UNREAD.getId() || cursor.getInt(cursor.getColumnIndexOrThrow("status")) == ImConstants.Status.READ.getId())) {
                        copyRcsMessageCursor(cursor, arrayList, isMcsSupported);
                    }
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        } while (cursor.moveToNext());
        cursor.close();
        return arrayList;
        throw th;
    }

    private static boolean isRevokedMessage(Cursor cursor) {
        int i = cursor.getInt(cursor.getColumnIndexOrThrow(ImContract.Message.REVOCATION_STATUS));
        return (i == ImConstants.RevocationStatus.NONE.getId() || i == ImConstants.RevocationStatus.AVAILABLE.getId() || i == ImConstants.RevocationStatus.FAILED.getId()) ? false : true;
    }

    private static void copyRcsMessageCursor(Cursor cursor, ArrayList<ContentValues> arrayList, boolean z) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("_id", Integer.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("_id"))));
        int i = cursor.getInt(cursor.getColumnIndexOrThrow(ImContract.ChatItem.IS_FILE_TRANSFER));
        contentValues.put(ImContract.ChatItem.IS_FILE_TRANSFER, Integer.valueOf(i));
        contentValues.put("direction", Integer.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("direction"))));
        contentValues.put("chat_id", cursor.getString(cursor.getColumnIndexOrThrow("chat_id")));
        contentValues.put("remote_uri", cursor.getString(cursor.getColumnIndexOrThrow("remote_uri")));
        contentValues.put(ImContract.ChatItem.USER_ALIAS, cursor.getString(cursor.getColumnIndexOrThrow(ImContract.ChatItem.USER_ALIAS)));
        contentValues.put("content_type", cursor.getString(cursor.getColumnIndexOrThrow("content_type")));
        contentValues.put(ImContract.ChatItem.INSERTED_TIMESTAMP, Long.valueOf(cursor.getLong(cursor.getColumnIndexOrThrow(ImContract.ChatItem.INSERTED_TIMESTAMP))));
        contentValues.put(ImContract.ChatItem.EXT_INFO, cursor.getString(cursor.getColumnIndexOrThrow(ImContract.ChatItem.EXT_INFO)));
        contentValues.put(ImContract.ChatItem.EXT_INFO, cursor.getString(cursor.getColumnIndexOrThrow(ImContract.ChatItem.EXT_INFO)));
        contentValues.put("body", cursor.getString(cursor.getColumnIndexOrThrow("body")));
        contentValues.put(ImContract.Message.NOTIFICATION_DISPOSITION_MASK, Integer.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(ImContract.Message.NOTIFICATION_DISPOSITION_MASK))));
        contentValues.put("notification_status", Integer.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("notification_status"))));
        contentValues.put(ImContract.Message.DISPOSITION_NOTIFICATION_STATUS, Integer.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(ImContract.Message.DISPOSITION_NOTIFICATION_STATUS))));
        contentValues.put(ImContract.Message.SENT_TIMESTAMP, Long.valueOf(cursor.getLong(cursor.getColumnIndexOrThrow(ImContract.Message.SENT_TIMESTAMP))));
        contentValues.put(ImContract.ChatItem.DELIVERED_TIMESTAMP, Long.valueOf(cursor.getLong(cursor.getColumnIndexOrThrow(ImContract.ChatItem.DELIVERED_TIMESTAMP))));
        contentValues.put(ImContract.Message.DISPLAYED_TIMESTAMP, Long.valueOf(cursor.getLong(cursor.getColumnIndexOrThrow(ImContract.Message.DISPLAYED_TIMESTAMP))));
        contentValues.put("message_type", Integer.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("message_type"))));
        contentValues.put(ImContract.Message.MESSAGE_ISSLM, Integer.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(ImContract.Message.MESSAGE_ISSLM))));
        contentValues.put("status", Integer.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("status"))));
        contentValues.put(ImContract.Message.NOT_DISPLAYED_COUNTER, Integer.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(ImContract.Message.NOT_DISPLAYED_COUNTER))));
        contentValues.put("imdn_message_id", cursor.getString(cursor.getColumnIndexOrThrow("imdn_message_id")));
        contentValues.put(ImContract.Message.IMDN_ORIGINAL_TO, cursor.getString(cursor.getColumnIndexOrThrow(ImContract.Message.IMDN_ORIGINAL_TO)));
        contentValues.put("conversation_id", cursor.getString(cursor.getColumnIndexOrThrow("conversation_id")));
        contentValues.put("contribution_id", cursor.getString(cursor.getColumnIndexOrThrow("contribution_id")));
        contentValues.put(ImContract.CsSession.FILE_PATH, cursor.getString(cursor.getColumnIndexOrThrow(ImContract.CsSession.FILE_PATH)));
        contentValues.put(ImContract.CsSession.FILE_NAME, cursor.getString(cursor.getColumnIndexOrThrow(ImContract.CsSession.FILE_NAME)));
        contentValues.put(ImContract.CsSession.FILE_SIZE, Long.valueOf(cursor.getLong(cursor.getColumnIndexOrThrow(ImContract.CsSession.FILE_SIZE))));
        contentValues.put(ImContract.CsSession.FILE_TRANSFER_ID, cursor.getString(cursor.getColumnIndexOrThrow(ImContract.CsSession.FILE_TRANSFER_ID)));
        contentValues.put("state", Integer.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("state"))));
        contentValues.put("reason", Integer.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("reason"))));
        contentValues.put(ImContract.CsSession.BYTES_TRANSFERRED, Long.valueOf(cursor.getLong(cursor.getColumnIndexOrThrow(ImContract.CsSession.BYTES_TRANSFERRED))));
        contentValues.put(ImContract.CsSession.STATUS, Integer.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(ImContract.CsSession.STATUS))));
        contentValues.put(ImContract.CsSession.THUMBNAIL_PATH, cursor.getString(cursor.getColumnIndexOrThrow(ImContract.CsSession.THUMBNAIL_PATH)));
        contentValues.put(ImContract.CsSession.IS_RESUMABLE, Integer.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(ImContract.CsSession.IS_RESUMABLE))));
        if (!z || i != 1) {
            contentValues.put(ImContract.CsSession.TRANSFER_MECH, Integer.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(ImContract.CsSession.TRANSFER_MECH))));
        } else {
            contentValues.put(ImContract.CsSession.TRANSFER_MECH, 1);
        }
        contentValues.put(ImContract.CsSession.DATA_URL, cursor.getString(cursor.getColumnIndexOrThrow(ImContract.CsSession.DATA_URL)));
        contentValues.put("request_message_id", cursor.getString(cursor.getColumnIndexOrThrow("request_message_id")));
        contentValues.put("sim_imsi", cursor.getString(cursor.getColumnIndexOrThrow("sim_imsi")));
        contentValues.put("is_resizable", Integer.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("is_resizable"))));
        contentValues.put(ImContract.Message.REFERENCE_ID, cursor.getString(cursor.getColumnIndexOrThrow(ImContract.Message.REFERENCE_ID)));
        contentValues.put(ImContract.Message.REFERENCE_TYPE, cursor.getString(cursor.getColumnIndexOrThrow(ImContract.Message.REFERENCE_TYPE)));
        contentValues.put(ImContract.Message.REFERENCE_VALUE, cursor.getString(cursor.getColumnIndexOrThrow(ImContract.Message.REFERENCE_VALUE)));
        contentValues.put(ImContract.Message.SUGGESTION, cursor.getString(cursor.getColumnIndexOrThrow(ImContract.Message.SUGGESTION)));
        contentValues.put("maap_traffic_type", cursor.getString(cursor.getColumnIndexOrThrow("maap_traffic_type")));
        contentValues.put(ImContract.Message.MSG_CREATOR, cursor.getString(cursor.getColumnIndexOrThrow(ImContract.Message.MSG_CREATOR)));
        arrayList.add(contentValues);
    }

    public static ArrayList<ContentValues> convertRCSparticipantstoCV(Cursor cursor) {
        if (cursor == null) {
            return null;
        }
        ArrayList<ContentValues> arrayList = new ArrayList<>();
        do {
            try {
                ContentValues contentValues = new ContentValues();
                contentValues.put("_id", Integer.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("_id"))));
                contentValues.put("chat_id", cursor.getString(cursor.getColumnIndexOrThrow("chat_id")));
                contentValues.put("status", Integer.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("status"))));
                contentValues.put("type", Integer.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("type"))));
                contentValues.put("uri", cursor.getString(cursor.getColumnIndexOrThrow("uri")));
                contentValues.put("alias", cursor.getString(cursor.getColumnIndexOrThrow("alias")));
                arrayList.add(contentValues);
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        } while (cursor.moveToNext());
        cursor.close();
        return arrayList;
        throw th;
    }

    public static ArrayList<ContentValues> convertSMStoCV(Cursor cursor, boolean z) {
        Throwable th;
        Cursor cursor2 = cursor;
        String str = CloudMessageProviderContract.BufferDBSMS.REPLY_PATH_PRESENT;
        String str2 = "status";
        String str3 = "read";
        String str4 = CloudMessageProviderContract.BufferDBSMS.PROTOCOL;
        String str5 = "sim_imsi";
        String str6 = "date_sent";
        String str7 = "sim_slot";
        String str8 = "type";
        String str9 = "deletable";
        String str10 = "seen";
        String str11 = CloudMessageProviderContract.BufferDBSMS.ERROR_CODE;
        String str12 = "locked";
        String str13 = CloudMessageProviderContract.BufferDBSMS.SERVICE_CENTER;
        String str14 = "subject";
        ArrayList<ContentValues> arrayList = new ArrayList<>();
        while (true) {
            try {
                ContentValues contentValues = new ContentValues();
                String str15 = str;
                int i = cursor2.getInt(cursor2.getColumnIndexOrThrow("_id"));
                String str16 = str2;
                String str17 = LOG_TAG;
                String str18 = str3;
                StringBuilder sb = new StringBuilder();
                String str19 = str4;
                sb.append("appId: ");
                sb.append(i);
                IMSLog.i(str17, sb.toString());
                contentValues.put("thread_id", Integer.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow("thread_id"))));
                if (!z) {
                    contentValues.put("_id", Integer.valueOf(i));
                    contentValues.put("address", cursor2.getString(cursor2.getColumnIndexOrThrow("address")));
                    contentValues.put(str8, Integer.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow(str8))));
                    contentValues.put("hidden", Integer.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow("hidden"))));
                    contentValues.put("body", cursor2.getString(cursor2.getColumnIndexOrThrow("body")));
                    contentValues.put(CloudMessageProviderContract.BufferDBSMS.GROUP_ID, Integer.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBSMS.GROUP_ID))));
                }
                contentValues.put(CloudMessageProviderContract.BufferDBSMS.PERSON, Integer.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBSMS.PERSON))));
                contentValues.put("date", Long.valueOf(cursor2.getLong(cursor2.getColumnIndexOrThrow("date"))));
                contentValues.put(str6, Long.valueOf(cursor2.getLong(cursor2.getColumnIndexOrThrow(str6))));
                String str20 = str19;
                contentValues.put(str20, Integer.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow(str20))));
                String str21 = str18;
                contentValues.put(str21, Integer.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow(str21))));
                String str22 = str16;
                contentValues.put(str22, Integer.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow(str22))));
                String str23 = str15;
                String str24 = str6;
                contentValues.put(str23, Integer.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow(str23))));
                String str25 = str14;
                String str26 = str8;
                contentValues.put(str25, cursor2.getString(cursor2.getColumnIndexOrThrow(str25)));
                String str27 = str13;
                String str28 = str25;
                contentValues.put(str27, cursor2.getString(cursor2.getColumnIndexOrThrow(str27)));
                String str29 = str12;
                String str30 = str27;
                contentValues.put(str29, Integer.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow(str29))));
                String str31 = str11;
                String str32 = str29;
                contentValues.put(str31, Integer.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow(str31))));
                String str33 = str10;
                String str34 = str31;
                contentValues.put(str33, Integer.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow(str33))));
                String str35 = str9;
                String str36 = str33;
                contentValues.put(str35, Integer.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow(str35))));
                String str37 = str7;
                String str38 = str35;
                contentValues.put(str37, Integer.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow(str37))));
                String str39 = str5;
                String str40 = str37;
                contentValues.put(str39, cursor2.getString(cursor2.getColumnIndexOrThrow(str39)));
                String str41 = str39;
                contentValues.put(CloudMessageProviderContract.BufferDBSMS.GROUP_TYPE, Integer.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBSMS.GROUP_TYPE))));
                contentValues.put(CloudMessageProviderContract.BufferDBSMS.DELIVERY_DATE, Integer.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBSMS.DELIVERY_DATE))));
                contentValues.put("app_id", Integer.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow("app_id"))));
                contentValues.put("msg_id", Integer.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow("msg_id"))));
                contentValues.put(CloudMessageProviderContract.BufferDBSMS.CALLBACK_NUMBER, cursor2.getString(cursor2.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBSMS.CALLBACK_NUMBER)));
                contentValues.put("reserved", Integer.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow("reserved"))));
                contentValues.put("pri", Integer.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow("pri"))));
                contentValues.put(CloudMessageProviderContract.BufferDBSMS.TELESERVICE_ID, Integer.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBSMS.TELESERVICE_ID))));
                contentValues.put(CloudMessageProviderContract.BufferDBSMS.LINK_URL, cursor2.getString(cursor2.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBSMS.LINK_URL)));
                contentValues.put(CloudMessageProviderContract.BufferDBSMS.SVC_CMD, cursor2.getString(cursor2.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBSMS.SVC_CMD)));
                contentValues.put(CloudMessageProviderContract.BufferDBSMS.SVC_CMD_CONTENT, Integer.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBSMS.SVC_CMD_CONTENT))));
                contentValues.put(CloudMessageProviderContract.BufferDBSMS.ROAM_PENDING, Integer.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBSMS.ROAM_PENDING))));
                contentValues.put("spam_report", Integer.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow("spam_report"))));
                contentValues.put("safe_message", Integer.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow("safe_message"))));
                contentValues.put("from_address", cursor2.getString(cursor2.getColumnIndexOrThrow("from_address")));
                ArrayList<ContentValues> arrayList2 = arrayList;
                arrayList2.add(contentValues);
                if (!cursor.moveToNext()) {
                    cursor.close();
                    return arrayList2;
                }
                arrayList = arrayList2;
                str8 = str26;
                str6 = str24;
                str14 = str28;
                str13 = str30;
                str12 = str32;
                str11 = str34;
                str10 = str36;
                str9 = str38;
                str7 = str40;
                str5 = str41;
                String str42 = str23;
                str4 = str20;
                str = str42;
                String str43 = str22;
                str3 = str21;
                str2 = str43;
            } catch (Throwable th2) {
                th.addSuppressed(th2);
            }
        }
        throw th;
    }

    public static ArrayList<ContentValues> convertPDUtoCV(Cursor cursor) {
        Throwable th;
        String str;
        String str2;
        String str3;
        String str4;
        String str5;
        String str6;
        String str7;
        String str8;
        String str9;
        String str10;
        ArrayList<ContentValues> arrayList;
        Cursor cursor2 = cursor;
        String str11 = CloudMessageProviderContract.BufferDBMMSpdu.M_TYPE;
        String str12 = CloudMessageProviderContract.BufferDBMMSpdu.TR_ID;
        String str13 = CloudMessageProviderContract.BufferDBMMSpdu.M_ID;
        String str14 = CloudMessageProviderContract.BufferDBMMSpdu.ST;
        String str15 = "read";
        String str16 = CloudMessageProviderContract.BufferDBMMSpdu.RESP_ST;
        String str17 = CloudMessageProviderContract.BufferDBMMSpdu.RPT_A;
        String str18 = CloudMessageProviderContract.BufferDBMMSpdu.RR;
        String str19 = "pri";
        String str20 = CloudMessageProviderContract.BufferDBMMSpdu.M_SIZE;
        if (cursor2 == null) {
            return null;
        }
        String str21 = CloudMessageProviderContract.BufferDBMMSpdu.V;
        ArrayList<ContentValues> arrayList2 = new ArrayList<>();
        while (true) {
            try {
                int columnIndex = cursor2.getColumnIndex(CloudMessageProviderContract.BufferDBMMSpdu.SECRET_MODE);
                String str22 = str11;
                if (columnIndex == -1 || cursor2.getInt(columnIndex) != 1) {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("_id", Integer.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow("_id"))));
                    contentValues.put("thread_id", Integer.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow("thread_id"))));
                    contentValues.put("date", Long.valueOf(cursor2.getLong(cursor2.getColumnIndexOrThrow("date")) * 1000));
                    contentValues.put("date_sent", Long.valueOf(cursor2.getLong(cursor2.getColumnIndexOrThrow("date_sent"))));
                    contentValues.put(CloudMessageProviderContract.BufferDBMMSpdu.MSG_BOX, Integer.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBMMSpdu.MSG_BOX))));
                    contentValues.put(str15, Integer.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow(str15))));
                    contentValues.put(str13, cursor2.getString(cursor2.getColumnIndexOrThrow(str13)));
                    contentValues.put(CloudMessageProviderContract.BufferDBMMSpdu.SUB, cursor2.getString(cursor2.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBMMSpdu.SUB)));
                    contentValues.put(CloudMessageProviderContract.BufferDBMMSpdu.SUB_CS, Integer.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBMMSpdu.SUB_CS))));
                    contentValues.put(CloudMessageProviderContract.BufferDBMMSpdu.CT_T, cursor2.getString(cursor2.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBMMSpdu.CT_T)));
                    contentValues.put(CloudMessageProviderContract.BufferDBMMSpdu.CT_L, cursor2.getString(cursor2.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBMMSpdu.CT_L)));
                    contentValues.put(CloudMessageProviderContract.BufferDBMMSpdu.EXP, Integer.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBMMSpdu.EXP))));
                    contentValues.put(CloudMessageProviderContract.BufferDBMMSpdu.M_CLS, cursor2.getString(cursor2.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBMMSpdu.M_CLS)));
                    str11 = str22;
                    str2 = str13;
                    contentValues.put(str11, Integer.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow(str11))));
                    String str23 = str21;
                    str3 = str15;
                    contentValues.put(str23, Integer.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow(str23))));
                    String str24 = str20;
                    str4 = str23;
                    contentValues.put(str24, Integer.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow(str24))));
                    String str25 = str19;
                    str5 = str24;
                    contentValues.put(str25, Integer.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow(str25))));
                    String str26 = str18;
                    str6 = str25;
                    contentValues.put(str26, Integer.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow(str26))));
                    String str27 = str17;
                    str7 = str26;
                    contentValues.put(str27, Integer.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow(str27))));
                    String str28 = str16;
                    str8 = str27;
                    contentValues.put(str28, Integer.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow(str28))));
                    String str29 = str14;
                    str9 = str28;
                    contentValues.put(str29, Integer.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow(str29))));
                    String str30 = str12;
                    str10 = str29;
                    contentValues.put(str30, cursor2.getString(cursor2.getColumnIndexOrThrow(str30)));
                    str = str30;
                    contentValues.put(CloudMessageProviderContract.BufferDBMMSpdu.RETR_ST, Integer.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBMMSpdu.RETR_ST))));
                    contentValues.put(CloudMessageProviderContract.BufferDBMMSpdu.RETR_TXT, cursor2.getString(cursor2.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBMMSpdu.RETR_TXT)));
                    contentValues.put(CloudMessageProviderContract.BufferDBMMSpdu.RETR_TXT_CS, Integer.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBMMSpdu.RETR_TXT_CS))));
                    contentValues.put(CloudMessageProviderContract.BufferDBMMSpdu.READ_STATUS, Integer.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBMMSpdu.READ_STATUS))));
                    contentValues.put(CloudMessageProviderContract.BufferDBMMSpdu.CT_CLS, Integer.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBMMSpdu.CT_CLS))));
                    contentValues.put(CloudMessageProviderContract.BufferDBMMSpdu.RESP_TXT, cursor2.getString(cursor2.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBMMSpdu.RESP_TXT)));
                    contentValues.put(CloudMessageProviderContract.BufferDBMMSpdu.D_TM, Integer.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBMMSpdu.D_TM))));
                    contentValues.put(CloudMessageProviderContract.BufferDBMMSpdu.D_RPT, Integer.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBMMSpdu.D_RPT))));
                    contentValues.put("locked", Integer.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow("locked"))));
                    contentValues.put("seen", Integer.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow("seen"))));
                    contentValues.put("sim_slot", Integer.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow("sim_slot"))));
                    contentValues.put("sim_imsi", cursor2.getString(cursor2.getColumnIndexOrThrow("sim_imsi")));
                    contentValues.put("deletable", Integer.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow("deletable"))));
                    contentValues.put("hidden", Integer.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow("hidden"))));
                    contentValues.put("app_id", Integer.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow("app_id"))));
                    contentValues.put("msg_id", Integer.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow("msg_id"))));
                    contentValues.put(CloudMessageProviderContract.BufferDBMMSpdu.CALLBACK_SET, Integer.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBMMSpdu.CALLBACK_SET))));
                    contentValues.put("reserved", Integer.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow("reserved"))));
                    contentValues.put(CloudMessageProviderContract.BufferDBMMSpdu.TEXT_ONLY, Integer.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBMMSpdu.TEXT_ONLY))));
                    contentValues.put("spam_report", Integer.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow("spam_report"))));
                    contentValues.put("safe_message", Integer.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow("safe_message"))));
                    contentValues.put("from_address", cursor2.getString(cursor2.getColumnIndexOrThrow("from_address")));
                    arrayList = arrayList2;
                    arrayList.add(contentValues);
                } else {
                    Log.i(LOG_TAG, "secret mode mms, so skip");
                    str = str12;
                    str10 = str14;
                    str9 = str16;
                    str8 = str17;
                    str7 = str18;
                    str6 = str19;
                    str5 = str20;
                    str4 = str21;
                    str11 = str22;
                    str2 = str13;
                    str3 = str15;
                    arrayList = arrayList2;
                }
                if (!cursor.moveToNext()) {
                    cursor.close();
                    return arrayList;
                }
                arrayList2 = arrayList;
                str15 = str3;
                str13 = str2;
                str21 = str4;
                str20 = str5;
                str19 = str6;
                str18 = str7;
                str17 = str8;
                str16 = str9;
                str14 = str10;
                str12 = str;
            } catch (Throwable th2) {
                th.addSuppressed(th2);
            }
        }
        throw th;
    }

    public static ArrayList<ContentValues> convertADDRtoCV(Cursor cursor) {
        if (cursor == null) {
            return null;
        }
        ArrayList<ContentValues> arrayList = new ArrayList<>();
        do {
            try {
                ContentValues contentValues = new ContentValues();
                contentValues.put("_id", Integer.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("_id"))));
                contentValues.put("msg_id", Integer.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("msg_id"))));
                contentValues.put(CloudMessageProviderContract.BufferDBMMSaddr.CONTACT_ID, Integer.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBMMSaddr.CONTACT_ID))));
                contentValues.put("address", cursor.getString(cursor.getColumnIndexOrThrow("address")));
                contentValues.put("type", Integer.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("type"))));
                contentValues.put("charset", Integer.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("charset"))));
                arrayList.add(contentValues);
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        } while (cursor.moveToNext());
        cursor.close();
        return arrayList;
        throw th;
    }

    public static ArrayList<ContentValues> convertPARTtoCV(Cursor cursor) {
        Throwable th;
        Cursor cursor2 = cursor;
        String str = "text";
        String str2 = CloudMessageProviderContract.BufferDBMMSpart._DATA;
        if (cursor2 == null) {
            return null;
        }
        ArrayList<ContentValues> arrayList = new ArrayList<>();
        while (true) {
            try {
                ContentValues contentValues = new ContentValues();
                contentValues.put("_id", Integer.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow("_id"))));
                contentValues.put(CloudMessageProviderContract.BufferDBMMSpart.MID, Integer.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBMMSpart.MID))));
                contentValues.put(CloudMessageProviderContract.BufferDBMMSpart.SEQ, Integer.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBMMSpart.SEQ))));
                contentValues.put(CloudMessageProviderContract.BufferDBMMSpart.CT, cursor2.getString(cursor2.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBMMSpart.CT)));
                contentValues.put("name", cursor2.getString(cursor2.getColumnIndexOrThrow("name")));
                contentValues.put(CloudMessageProviderContract.BufferDBMMSpart.CHSET, Integer.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBMMSpart.CHSET))));
                contentValues.put(CloudMessageProviderContract.BufferDBMMSpart.CD, cursor2.getString(cursor2.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBMMSpart.CD)));
                contentValues.put(CloudMessageProviderContract.BufferDBMMSpart.FN, cursor2.getString(cursor2.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBMMSpart.FN)));
                contentValues.put(CloudMessageProviderContract.BufferDBMMSpart.CID, cursor2.getString(cursor2.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBMMSpart.CID)));
                contentValues.put(CloudMessageProviderContract.BufferDBMMSpart.CL, cursor2.getString(cursor2.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBMMSpart.CL)));
                contentValues.put(CloudMessageProviderContract.BufferDBMMSpart.CTT_S, Integer.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBMMSpart.CTT_S))));
                contentValues.put(CloudMessageProviderContract.BufferDBMMSpart.CTT_T, cursor2.getString(cursor2.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBMMSpart.CTT_T)));
                contentValues.put(str2, cursor2.getString(cursor2.getColumnIndexOrThrow(str2)));
                str = str;
                String str3 = str2;
                contentValues.put(str, cursor2.getString(cursor2.getColumnIndexOrThrow(str)));
                ArrayList<ContentValues> arrayList2 = arrayList;
                arrayList2.add(contentValues);
                if (!cursor.moveToNext()) {
                    cursor.close();
                    return arrayList2;
                }
                arrayList = arrayList2;
                str2 = str3;
            } catch (Throwable th2) {
                th.addSuppressed(th2);
            }
        }
        throw th;
    }

    public static ArrayList<ContentValues> convertImdnNotificationtoCV(Cursor cursor) {
        if (cursor == null) {
            return null;
        }
        ArrayList<ContentValues> arrayList = new ArrayList<>();
        do {
            try {
                ContentValues contentValues = new ContentValues();
                contentValues.put("id", Long.valueOf(cursor.getLong(cursor.getColumnIndexOrThrow("id"))));
                contentValues.put("imdn_id", cursor.getString(cursor.getColumnIndexOrThrow("imdn_id")));
                contentValues.put(ImContract.MessageNotification.SENDER_URI, cursor.getString(cursor.getColumnIndexOrThrow(ImContract.MessageNotification.SENDER_URI)));
                contentValues.put("status", Integer.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("status"))));
                contentValues.put("timestamp", Long.valueOf(cursor.getLong(cursor.getColumnIndexOrThrow("timestamp"))));
                arrayList.add(contentValues);
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        } while (cursor.moveToNext());
        cursor.close();
        return arrayList;
        throw th;
    }

    public static ArrayList<ContentValues> convertRCSSessiontoCV(Cursor cursor) {
        Throwable th;
        Cursor cursor2 = cursor;
        String str = "contribution_id";
        String str2 = "subject_participant";
        String str3 = "status";
        String str4 = ImContract.ImSession.INSERTED_TIMESTAMP;
        String str5 = ImContract.ImSession.IS_FT_GROUP_CHAT;
        String str6 = "sim_imsi";
        String str7 = ImContract.ImSession.ICON_TIMESTAMP;
        String str8 = ImContract.ImSession.ICON_PARTICIPANT;
        String str9 = ImContract.ImSession.ICON_PATH;
        String str10 = ImContract.ImSession.PREFERRED_URI;
        String str11 = "session_uri";
        ArrayList<ContentValues> arrayList = new ArrayList<>();
        while (true) {
            try {
                ContentValues contentValues = new ContentValues();
                contentValues.put("_id", Integer.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow("_id"))));
                contentValues.put("chat_id", cursor2.getString(cursor2.getColumnIndexOrThrow("chat_id")));
                contentValues.put(ImContract.ImSession.OWN_PHONE_NUMBER, cursor2.getString(cursor2.getColumnIndexOrThrow(ImContract.ImSession.OWN_PHONE_NUMBER)));
                contentValues.put(ImContract.ImSession.CHAT_TYPE, Integer.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow(ImContract.ImSession.CHAT_TYPE))));
                contentValues.put("is_group_chat", Integer.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow("is_group_chat"))));
                contentValues.put(str5, Integer.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow(str5))));
                contentValues.put(str3, Integer.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow(str3))));
                contentValues.put("subject", cursor2.getString(cursor2.getColumnIndexOrThrow("subject")));
                contentValues.put(ImContract.ImSession.IS_MUTED, Integer.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow(ImContract.ImSession.IS_MUTED))));
                contentValues.put(ImContract.ImSession.MAX_PARTICIPANTS_COUNT, Integer.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow(ImContract.ImSession.MAX_PARTICIPANTS_COUNT))));
                contentValues.put(ImContract.ImSession.IMDN_NOTIFICATIONS_AVAILABILITY, Integer.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow(ImContract.ImSession.IMDN_NOTIFICATIONS_AVAILABILITY))));
                contentValues.put("direction", Integer.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow("direction"))));
                contentValues.put("conversation_id", cursor2.getString(cursor2.getColumnIndexOrThrow("conversation_id")));
                str = str;
                String str12 = str3;
                contentValues.put(str, cursor2.getString(cursor2.getColumnIndexOrThrow(str)));
                String str13 = str11;
                String str14 = str5;
                contentValues.put(str13, cursor2.getString(cursor2.getColumnIndexOrThrow(str13)));
                String str15 = str10;
                String str16 = str13;
                contentValues.put(str15, cursor2.getString(cursor2.getColumnIndexOrThrow(str15)));
                String str17 = str9;
                String str18 = str15;
                contentValues.put(str17, cursor2.getString(cursor2.getColumnIndexOrThrow(str17)));
                String str19 = str8;
                String str20 = str17;
                contentValues.put(str19, cursor2.getString(cursor2.getColumnIndexOrThrow(str19)));
                String str21 = str7;
                String str22 = str19;
                contentValues.put(str21, cursor2.getString(cursor2.getColumnIndexOrThrow(str21)));
                String str23 = str6;
                String str24 = str21;
                contentValues.put(str23, cursor2.getString(cursor2.getColumnIndexOrThrow(str23)));
                String str25 = str4;
                String str26 = str23;
                String string = cursor2.getString(cursor2.getColumnIndexOrThrow(str25));
                if (TextUtils.isEmpty(string)) {
                    string = String.valueOf(RcsUtils.getEpochNanosec());
                }
                contentValues.put(str25, string);
                String str27 = str2;
                String str28 = str25;
                contentValues.put(str27, cursor2.getString(cursor2.getColumnIndexOrThrow(str27)));
                String str29 = str27;
                contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Insert.getId()));
                contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.ToSendCloud.getId()));
                ArrayList<ContentValues> arrayList2 = arrayList;
                arrayList2.add(contentValues);
                if (!cursor.moveToNext()) {
                    cursor.close();
                    return arrayList2;
                }
                arrayList = arrayList2;
                str5 = str14;
                str3 = str12;
                str11 = str16;
                str10 = str18;
                str9 = str20;
                str8 = str22;
                str7 = str24;
                str6 = str26;
                str4 = str28;
                str2 = str29;
            } catch (Throwable th2) {
                th.addSuppressed(th2);
            }
        }
        throw th;
    }

    public static ArrayList<ContentValues> convertRCSIMFTFromTPtoCV(Cursor cursor, boolean z) {
        ArrayList<ContentValues> arrayList = new ArrayList<>();
        do {
            try {
                int i = cursor.getInt(cursor.getColumnIndexOrThrow("status"));
                int i2 = cursor.getInt(cursor.getColumnIndexOrThrow("type")) - 1;
                String str = LOG_TAG;
                Log.d(str, "convertRCSIMFTFromTPtoCV: direction: " + i2 + " status: " + i + " isFT: " + z);
                ContentValues contentValues = new ContentValues();
                if (z) {
                    if ((i2 == ImDirection.INCOMING.getId() || i2 == ImDirection.OUTGOING.getId()) && (i == ImConstants.Status.SENT.getId() || i == ImConstants.Status.TO_SEND.getId() || i == ImConstants.Status.SENDING.getId() || i == ImConstants.Status.UNREAD.getId() || i == ImConstants.Status.CANCELLATION.getId() || i == ImConstants.Status.READ.getId())) {
                        addFtInfoTOcv(cursor, contentValues);
                        contentValues.put(ImContract.ChatItem.IS_FILE_TRANSFER, 1);
                        contentValues.put(ImContract.CsSession.TRANSFER_MECH, 1);
                        copyTPRcsMessageCursor(cursor, arrayList, contentValues);
                    }
                } else if ((i2 == ImDirection.INCOMING.getId() || i2 == ImDirection.OUTGOING.getId()) && (i == ImConstants.Status.SENT.getId() || i == ImConstants.Status.UNREAD.getId() || i == ImConstants.Status.CANCELLATION.getId() || i == ImConstants.Status.READ.getId())) {
                    contentValues.put("body", cursor.getString(cursor.getColumnIndexOrThrow("body")));
                    contentValues.put(ImContract.ChatItem.IS_FILE_TRANSFER, 0);
                    copyTPRcsMessageCursor(cursor, arrayList, contentValues);
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        } while (cursor.moveToNext());
        cursor.close();
        return arrayList;
        throw th;
    }

    /* JADX WARNING: Can't wrap try/catch for region: R(11:0|(2:2|(1:7)(1:6))(2:8|(1:10))|11|(2:15|16)|19|20|21|22|23|24|26) */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Missing exception handler attribute for start block: B:22:0x01ba */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void copyTPRcsMessageCursor(android.database.Cursor r8, java.util.ArrayList<android.content.ContentValues> r9, android.content.ContentValues r10) {
        /*
            java.lang.String r0 = "maap_traffic_type"
            java.lang.String r1 = "suggestion"
            java.lang.String r2 = "_id"
            int r3 = r8.getColumnIndexOrThrow(r2)
            int r3 = r8.getInt(r3)
            java.lang.Integer r3 = java.lang.Integer.valueOf(r3)
            r10.put(r2, r3)
            java.lang.String r2 = "type"
            int r2 = r8.getColumnIndexOrThrow(r2)
            int r2 = r8.getInt(r2)
            int r2 = r2 + -1
            java.lang.String r3 = "direction"
            java.lang.Integer r4 = java.lang.Integer.valueOf(r2)
            r10.put(r3, r4)
            java.lang.String r3 = "remote_uri"
            int r4 = r8.getColumnIndexOrThrow(r3)
            java.lang.String r4 = r8.getString(r4)
            r10.put(r3, r4)
            java.lang.String r3 = "user_alias"
            int r3 = r8.getColumnIndexOrThrow(r3)
            java.lang.String r3 = r8.getString(r3)
            java.lang.String r4 = "sender_alias"
            r10.put(r4, r3)
            java.lang.String r3 = "content_type"
            int r4 = r8.getColumnIndexOrThrow(r3)
            java.lang.String r4 = r8.getString(r4)
            r10.put(r3, r4)
            java.lang.String r3 = "date"
            int r3 = r8.getColumnIndexOrThrow(r3)
            long r3 = r8.getLong(r3)
            java.lang.Long r3 = java.lang.Long.valueOf(r3)
            java.lang.String r4 = "inserted_timestamp"
            r10.put(r4, r3)
            java.lang.String r3 = "display_notification_status"
            int r3 = r8.getColumnIndexOrThrow(r3)
            int r3 = r8.getInt(r3)
            java.lang.Integer r3 = java.lang.Integer.valueOf(r3)
            java.lang.String r4 = "notification_status"
            r10.put(r4, r3)
            java.lang.String r3 = "date_sent"
            int r3 = r8.getColumnIndexOrThrow(r3)
            long r3 = r8.getLong(r3)
            java.lang.Long r3 = java.lang.Long.valueOf(r3)
            java.lang.String r4 = "sent_timestamp"
            r10.put(r4, r3)
            java.lang.String r3 = "delivered_timestamp"
            int r4 = r8.getColumnIndexOrThrow(r3)
            long r4 = r8.getLong(r4)
            java.lang.Long r4 = java.lang.Long.valueOf(r4)
            r10.put(r3, r4)
            java.lang.String r3 = "updated_timestamp"
            int r3 = r8.getColumnIndexOrThrow(r3)
            long r3 = r8.getLong(r3)
            java.lang.Long r3 = java.lang.Long.valueOf(r3)
            java.lang.String r4 = "displayed_timestamp"
            r10.put(r4, r3)
            java.lang.String r3 = "message_type"
            int r4 = r8.getColumnIndexOrThrow(r3)
            int r4 = r8.getInt(r4)
            int r4 = com.sec.internal.ims.cmstore.strategy.KorCmStrategy.getRCSMessageType(r4)
            java.lang.Integer r4 = java.lang.Integer.valueOf(r4)
            r10.put(r3, r4)
            java.lang.String r3 = "status"
            int r4 = r8.getColumnIndexOrThrow(r3)
            int r4 = r8.getInt(r4)
            java.lang.String r5 = "read"
            int r5 = r8.getColumnIndexOrThrow(r5)
            int r5 = r8.getInt(r5)
            com.sec.internal.constants.ims.servicemodules.im.ImConstants$Status r6 = com.sec.internal.constants.ims.servicemodules.im.ImConstants.Status.CANCELLATION
            int r7 = r6.getId()
            if (r4 != r7) goto L_0x0106
            com.sec.internal.constants.ims.servicemodules.im.ImDirection r4 = com.sec.internal.constants.ims.servicemodules.im.ImDirection.INCOMING
            int r4 = r4.getId()
            if (r2 != r4) goto L_0x0101
            com.sec.internal.constants.ims.servicemodules.im.ImConstants$Status r2 = com.sec.internal.constants.ims.servicemodules.im.ImConstants.Status.UNREAD
            int r2 = r2.getId()
            if (r5 != r2) goto L_0x0101
            com.sec.internal.constants.ims.servicemodules.im.ImConstants$Status r2 = com.sec.internal.constants.ims.servicemodules.im.ImConstants.Status.CANCELLATION_UNREAD
            int r4 = r2.getId()
            goto L_0x010f
        L_0x0101:
            int r4 = r6.getId()
            goto L_0x010f
        L_0x0106:
            com.sec.internal.constants.ims.servicemodules.im.ImDirection r6 = com.sec.internal.constants.ims.servicemodules.im.ImDirection.INCOMING
            int r6 = r6.getId()
            if (r2 != r6) goto L_0x010f
            r4 = r5
        L_0x010f:
            java.lang.Integer r2 = java.lang.Integer.valueOf(r4)
            r10.put(r3, r2)
            java.lang.String r2 = "displayed_counter"
            int r2 = r8.getColumnIndexOrThrow(r2)
            int r2 = r8.getInt(r2)
            java.lang.Integer r2 = java.lang.Integer.valueOf(r2)
            java.lang.String r3 = "not_displayed_counter"
            r10.put(r3, r2)
            java.lang.String r2 = "imdn_message_id"
            int r3 = r8.getColumnIndexOrThrow(r2)
            java.lang.String r3 = r8.getString(r3)
            r10.put(r2, r3)
            java.lang.String r2 = "creator"
            int r3 = r8.getColumnIndexOrThrow(r2)
            java.lang.String r3 = r8.getString(r3)
            r10.put(r2, r3)
            java.lang.String r2 = "recipients"
            int r3 = r8.getColumnIndexOrThrow(r2)
            java.lang.String r3 = r8.getString(r3)
            r10.put(r2, r3)
            java.lang.String r2 = "re_original_key"
            int r2 = r8.getColumnIndexOrThrow(r2)
            java.lang.String r2 = r8.getString(r2)
            java.lang.String r3 = "reference_id"
            r10.put(r3, r2)
            java.lang.String r2 = "re_type"
            int r2 = r8.getColumnIndexOrThrow(r2)
            int r2 = r8.getInt(r2)
            java.lang.Integer r2 = java.lang.Integer.valueOf(r2)
            java.lang.String r3 = "reference_type"
            r10.put(r3, r2)
            java.lang.String r3 = "re_count_info"
            int r3 = r8.getColumnIndexOrThrow(r3)
            java.lang.String r3 = r8.getString(r3)
            java.lang.String r2 = java.lang.String.valueOf(r2)
            java.lang.String r4 = "2"
            boolean r2 = r4.equals(r2)
            if (r2 == 0) goto L_0x01a9
            boolean r2 = android.text.TextUtils.isEmpty(r3)
            if (r2 != 0) goto L_0x01a9
            java.lang.Integer r2 = java.lang.Integer.valueOf(r3)     // Catch:{ NumberFormatException -> 0x01a1 }
            int r2 = r2.intValue()     // Catch:{ NumberFormatException -> 0x01a1 }
            java.lang.String r3 = com.sec.internal.ims.cmstore.utils.Util.getReactionReferenceValue(r2)     // Catch:{ NumberFormatException -> 0x01a1 }
            goto L_0x01a9
        L_0x01a1:
            java.lang.String r2 = LOG_TAG
            java.lang.String r4 = "re_type is for Reaction but re_count_info is not integer, skip converting."
            android.util.Log.i(r2, r4)
        L_0x01a9:
            java.lang.String r2 = "reference_value"
            r10.put(r2, r3)
            int r2 = r8.getColumnIndexOrThrow(r1)     // Catch:{ IllegalArgumentException -> 0x01ba }
            java.lang.String r2 = r8.getString(r2)     // Catch:{ IllegalArgumentException -> 0x01ba }
            r10.put(r1, r2)     // Catch:{ IllegalArgumentException -> 0x01ba }
        L_0x01ba:
            int r1 = r8.getColumnIndexOrThrow(r0)     // Catch:{ IllegalArgumentException -> 0x01c5 }
            java.lang.String r8 = r8.getString(r1)     // Catch:{ IllegalArgumentException -> 0x01c5 }
            r10.put(r0, r8)     // Catch:{ IllegalArgumentException -> 0x01c5 }
        L_0x01c5:
            r9.add(r10)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.utils.CursorContentValueTranslator.copyTPRcsMessageCursor(android.database.Cursor, java.util.ArrayList, android.content.ContentValues):void");
    }

    private static void addFtInfoTOcv(Cursor cursor, ContentValues contentValues) {
        contentValues.put(ImContract.CsSession.FILE_PATH, cursor.getString(cursor.getColumnIndexOrThrow(ImContract.CsSession.FILE_PATH)));
        contentValues.put(ImContract.CsSession.FILE_NAME, cursor.getString(cursor.getColumnIndexOrThrow(ImContract.CsSession.FILE_NAME)));
        contentValues.put(ImContract.CsSession.FILE_SIZE, Long.valueOf(cursor.getLong(cursor.getColumnIndexOrThrow(ImContract.CsSession.FILE_SIZE))));
        contentValues.put(ImContract.CsSession.BYTES_TRANSFERRED, Long.valueOf(cursor.getLong(cursor.getColumnIndexOrThrow(ImContract.CsSession.BYTES_TRANSFERRED))));
        contentValues.put(ImContract.CsSession.STATUS, Integer.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("status"))));
        contentValues.put(ImContract.CsSession.THUMBNAIL_PATH, cursor.getString(cursor.getColumnIndexOrThrow(ImContract.CsSession.THUMBNAIL_PATH)));
    }

    /* JADX WARNING: Removed duplicated region for block: B:25:0x015c A[Catch:{ all -> 0x01ef, all -> 0x01f6 }] */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x0165 A[Catch:{ all -> 0x01ef, all -> 0x01f6 }] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static java.util.ArrayList<android.content.ContentValues> convertTPRCSSessionToCV(android.database.Cursor r14, com.sec.internal.ims.cmstore.MessageStoreClient r15) {
        /*
            java.lang.String r0 = "conversation_type"
            java.lang.String r1 = "self_phone_number"
            java.lang.String r2 = "_id"
            java.lang.String r3 = "sim_imsi"
            java.util.ArrayList r4 = new java.util.ArrayList
            r4.<init>()
        L_0x000f:
            android.content.ContentValues r5 = new android.content.ContentValues     // Catch:{ all -> 0x01ef }
            r5.<init>()     // Catch:{ all -> 0x01ef }
            int r6 = r14.getColumnIndexOrThrow(r3)     // Catch:{ all -> 0x01ef }
            java.lang.String r6 = r14.getString(r6)     // Catch:{ all -> 0x01ef }
            boolean r7 = android.text.TextUtils.isEmpty(r6)     // Catch:{ all -> 0x01ef }
            r8 = 0
            r9 = 1
            if (r7 != 0) goto L_0x0026
            r7 = r9
            goto L_0x0027
        L_0x0026:
            r7 = r8
        L_0x0027:
            java.lang.String r10 = LOG_TAG     // Catch:{ all -> 0x01ef }
            java.lang.StringBuilder r11 = new java.lang.StringBuilder     // Catch:{ all -> 0x01ef }
            r11.<init>()     // Catch:{ all -> 0x01ef }
            java.lang.String r12 = "convertTPRCSSessionToCV: isImsiPresent: "
            r11.append(r12)     // Catch:{ all -> 0x01ef }
            r11.append(r7)     // Catch:{ all -> 0x01ef }
            java.lang.String r11 = r11.toString()     // Catch:{ all -> 0x01ef }
            android.util.Log.i(r10, r11)     // Catch:{ all -> 0x01ef }
            int r10 = r14.getColumnIndexOrThrow(r2)     // Catch:{ all -> 0x01ef }
            int r10 = r14.getInt(r10)     // Catch:{ all -> 0x01ef }
            java.lang.Integer r10 = java.lang.Integer.valueOf(r10)     // Catch:{ all -> 0x01ef }
            r5.put(r2, r10)     // Catch:{ all -> 0x01ef }
            java.lang.String r10 = "contribution_id"
            java.lang.String r11 = "conversation_id"
            java.lang.String r12 = "chat_id"
            java.lang.String r13 = "session_uri"
            if (r7 == 0) goto L_0x00a0
            java.lang.String r7 = r15.getCurrentIMSI()     // Catch:{ all -> 0x01ef }
            boolean r6 = r6.equals(r7)     // Catch:{ all -> 0x01ef }
            if (r6 == 0) goto L_0x00a0
            java.lang.String r6 = "session_id"
            int r6 = r14.getColumnIndexOrThrow(r6)     // Catch:{ all -> 0x01ef }
            java.lang.String r6 = r14.getString(r6)     // Catch:{ all -> 0x01ef }
            r5.put(r12, r6)     // Catch:{ all -> 0x01ef }
            java.lang.String r6 = "im_conversation_id"
            int r6 = r14.getColumnIndexOrThrow(r6)     // Catch:{ all -> 0x01ef }
            java.lang.String r6 = r14.getString(r6)     // Catch:{ all -> 0x01ef }
            r5.put(r11, r6)     // Catch:{ all -> 0x01ef }
            java.lang.String r6 = "im_contribution_id"
            int r6 = r14.getColumnIndexOrThrow(r6)     // Catch:{ all -> 0x01ef }
            java.lang.String r6 = r14.getString(r6)     // Catch:{ all -> 0x01ef }
            r5.put(r10, r6)     // Catch:{ all -> 0x01ef }
            int r6 = r14.getColumnIndexOrThrow(r13)     // Catch:{ all -> 0x01ef }
            java.lang.String r6 = r14.getString(r6)     // Catch:{ all -> 0x01ef }
            r5.put(r13, r6)     // Catch:{ all -> 0x01ef }
            int r6 = r14.getColumnIndexOrThrow(r3)     // Catch:{ all -> 0x01ef }
            java.lang.String r6 = r14.getString(r6)     // Catch:{ all -> 0x01ef }
            r5.put(r3, r6)     // Catch:{ all -> 0x01ef }
            goto L_0x00e4
        L_0x00a0:
            java.lang.String r6 = "session_id2"
            int r6 = r14.getColumnIndexOrThrow(r6)     // Catch:{ all -> 0x01ef }
            java.lang.String r6 = r14.getString(r6)     // Catch:{ all -> 0x01ef }
            r5.put(r12, r6)     // Catch:{ all -> 0x01ef }
            java.lang.String r6 = "im_conversation_id2"
            int r6 = r14.getColumnIndexOrThrow(r6)     // Catch:{ all -> 0x01ef }
            java.lang.String r6 = r14.getString(r6)     // Catch:{ all -> 0x01ef }
            r5.put(r11, r6)     // Catch:{ all -> 0x01ef }
            java.lang.String r6 = "im_contribution_id2"
            int r6 = r14.getColumnIndexOrThrow(r6)     // Catch:{ all -> 0x01ef }
            java.lang.String r6 = r14.getString(r6)     // Catch:{ all -> 0x01ef }
            r5.put(r10, r6)     // Catch:{ all -> 0x01ef }
            java.lang.String r6 = "session_uri2"
            int r6 = r14.getColumnIndexOrThrow(r6)     // Catch:{ all -> 0x01ef }
            java.lang.String r6 = r14.getString(r6)     // Catch:{ all -> 0x01ef }
            r5.put(r13, r6)     // Catch:{ all -> 0x01ef }
            java.lang.String r6 = "sim_imsi2"
            int r6 = r14.getColumnIndexOrThrow(r6)     // Catch:{ all -> 0x01ef }
            java.lang.String r6 = r14.getString(r6)     // Catch:{ all -> 0x01ef }
            r5.put(r3, r6)     // Catch:{ all -> 0x01ef }
        L_0x00e4:
            java.lang.String r6 = "own_sim_imsi"
            int r7 = r14.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x01ef }
            java.lang.String r7 = r14.getString(r7)     // Catch:{ all -> 0x01ef }
            r5.put(r6, r7)     // Catch:{ all -> 0x01ef }
            java.lang.String r6 = "chat_type"
            int r7 = r14.getColumnIndexOrThrow(r0)     // Catch:{ all -> 0x01ef }
            int r7 = r14.getInt(r7)     // Catch:{ all -> 0x01ef }
            int r7 = r7 - r9
            java.lang.Integer r7 = java.lang.Integer.valueOf(r7)     // Catch:{ all -> 0x01ef }
            r5.put(r6, r7)     // Catch:{ all -> 0x01ef }
            java.lang.String r6 = "subject"
            java.lang.String r7 = "alias"
            int r7 = r14.getColumnIndexOrThrow(r7)     // Catch:{ all -> 0x01ef }
            java.lang.String r7 = r14.getString(r7)     // Catch:{ all -> 0x01ef }
            r5.put(r6, r7)     // Catch:{ all -> 0x01ef }
            java.lang.String r6 = "subject_timestamp"
            java.lang.String r7 = "message_date"
            int r7 = r14.getColumnIndexOrThrow(r7)     // Catch:{ all -> 0x01ef }
            java.lang.String r7 = r14.getString(r7)     // Catch:{ all -> 0x01ef }
            r5.put(r6, r7)     // Catch:{ all -> 0x01ef }
            java.lang.String r6 = "date"
            int r6 = r14.getColumnIndexOrThrow(r6)     // Catch:{ NumberFormatException -> 0x0136 }
            java.lang.String r6 = r14.getString(r6)     // Catch:{ NumberFormatException -> 0x0136 }
            long r6 = java.lang.Long.parseLong(r6)     // Catch:{ NumberFormatException -> 0x0136 }
            r10 = 1000000(0xf4240, double:4.940656E-318)
            long r6 = r6 * r10
            goto L_0x0138
        L_0x0136:
            r6 = 0
        L_0x0138:
            java.lang.String r10 = "inserted_time_stamp"
            java.lang.Long r6 = java.lang.Long.valueOf(r6)     // Catch:{ all -> 0x01ef }
            r5.put(r10, r6)     // Catch:{ all -> 0x01ef }
            int r6 = r14.getColumnIndexOrThrow(r0)     // Catch:{ all -> 0x01ef }
            int r6 = r14.getInt(r6)     // Catch:{ all -> 0x01ef }
            java.lang.String r7 = "status"
            com.sec.internal.constants.ims.servicemodules.im.ChatData$State r10 = com.sec.internal.constants.ims.servicemodules.im.ChatData.State.ACTIVE     // Catch:{ all -> 0x01ef }
            int r10 = r10.getId()     // Catch:{ all -> 0x01ef }
            java.lang.Integer r10 = java.lang.Integer.valueOf(r10)     // Catch:{ all -> 0x01ef }
            r5.put(r7, r10)     // Catch:{ all -> 0x01ef }
            r7 = 3
            if (r6 != r7) goto L_0x0165
            java.lang.String r5 = LOG_TAG     // Catch:{ all -> 0x01ef }
            java.lang.String r6 = "Ignore closed the group chat"
            android.util.Log.i(r5, r6)     // Catch:{ all -> 0x01ef }
            goto L_0x01e5
        L_0x0165:
            r7 = 2
            if (r6 != r7) goto L_0x016a
            r6 = r9
            goto L_0x016b
        L_0x016a:
            r6 = r8
        L_0x016b:
            java.lang.String r7 = "is_group_chat"
            if (r6 == 0) goto L_0x0170
            r8 = r9
        L_0x0170:
            java.lang.Integer r6 = java.lang.Integer.valueOf(r8)     // Catch:{ all -> 0x01ef }
            r5.put(r7, r6)     // Catch:{ all -> 0x01ef }
            java.lang.String r6 = "is_muted"
            java.lang.String r7 = "is_mute"
            int r7 = r14.getColumnIndexOrThrow(r7)     // Catch:{ all -> 0x01ef }
            int r7 = r14.getInt(r7)     // Catch:{ all -> 0x01ef }
            java.lang.Integer r7 = java.lang.Integer.valueOf(r7)     // Catch:{ all -> 0x01ef }
            r5.put(r6, r7)     // Catch:{ all -> 0x01ef }
            java.lang.String r6 = "preferred_uri"
            int r7 = r14.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x01ef }
            java.lang.String r7 = r14.getString(r7)     // Catch:{ all -> 0x01ef }
            android.content.Context r8 = r15.getContext()     // Catch:{ all -> 0x01ef }
            int r9 = r15.getClientID()     // Catch:{ all -> 0x01ef }
            java.lang.String r8 = com.sec.internal.ims.cmstore.utils.Util.getSimCountryCode(r8, r9)     // Catch:{ all -> 0x01ef }
            java.lang.String r7 = com.sec.internal.ims.cmstore.utils.Util.getTelUri(r7, r8)     // Catch:{ all -> 0x01ef }
            r5.put(r6, r7)     // Catch:{ all -> 0x01ef }
            java.lang.String r6 = "max_participants_count"
            r7 = 100
            java.lang.Integer r7 = java.lang.Integer.valueOf(r7)     // Catch:{ all -> 0x01ef }
            r5.put(r6, r7)     // Catch:{ all -> 0x01ef }
            java.lang.String r6 = "icon_path"
            java.lang.String r7 = "menustring"
            int r7 = r14.getColumnIndexOrThrow(r7)     // Catch:{ all -> 0x01ef }
            java.lang.String r7 = r14.getString(r7)     // Catch:{ all -> 0x01ef }
            r5.put(r6, r7)     // Catch:{ all -> 0x01ef }
            java.lang.String r6 = "syncdirection"
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r7 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Insert     // Catch:{ all -> 0x01ef }
            int r7 = r7.getId()     // Catch:{ all -> 0x01ef }
            java.lang.Integer r7 = java.lang.Integer.valueOf(r7)     // Catch:{ all -> 0x01ef }
            r5.put(r6, r7)     // Catch:{ all -> 0x01ef }
            java.lang.String r6 = "syncaction"
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r7 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.ToSendCloud     // Catch:{ all -> 0x01ef }
            int r7 = r7.getId()     // Catch:{ all -> 0x01ef }
            java.lang.Integer r7 = java.lang.Integer.valueOf(r7)     // Catch:{ all -> 0x01ef }
            r5.put(r6, r7)     // Catch:{ all -> 0x01ef }
            r4.add(r5)     // Catch:{ all -> 0x01ef }
        L_0x01e5:
            boolean r5 = r14.moveToNext()     // Catch:{ all -> 0x01ef }
            if (r5 != 0) goto L_0x000f
            r14.close()
            return r4
        L_0x01ef:
            r15 = move-exception
            if (r14 == 0) goto L_0x01fa
            r14.close()     // Catch:{ all -> 0x01f6 }
            goto L_0x01fa
        L_0x01f6:
            r14 = move-exception
            r15.addSuppressed(r14)
        L_0x01fa:
            throw r15
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.utils.CursorContentValueTranslator.convertTPRCSSessionToCV(android.database.Cursor, com.sec.internal.ims.cmstore.MessageStoreClient):java.util.ArrayList");
    }
}
