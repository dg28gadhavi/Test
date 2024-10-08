package com.sec.internal.ims.servicemodules.im;

import android.database.Cursor;
import com.sec.internal.constants.ims.servicemodules.im.ChatData;
import com.sec.internal.constants.ims.servicemodules.im.ImContract;
import com.sec.internal.constants.ims.servicemodules.im.result.Result;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.log.IMSLog;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;

public class ImDump {
    private static final String LOG_TAG = "ImDump";
    private static final int MAX_EVENT_LOGS = 3000;
    private static final int MAX_MESSAGE_DUMP = 50;
    Date date = new Date();
    private final ArrayBlockingQueue<String> mEventLogs = new ArrayBlockingQueue<>(3000);
    private final ImCache mImCache;
    SimpleDateFormat timeFormat = new SimpleDateFormat("MM-dd HH:mm:ss.SSS");

    ImDump(ImCache imCache) {
        this.mImCache = imCache;
    }

    /* access modifiers changed from: protected */
    public void dump() {
        String str = LOG_TAG;
        IMSLog.dump(str, "Dump of " + str + ":");
        IMSLog.increaseIndent(str);
        IMSLog.dump(str, "Event Logs:");
        IMSLog.increaseIndent(str);
        Iterator<String> it = this.mEventLogs.iterator();
        while (it.hasNext()) {
            IMSLog.dump(LOG_TAG, it.next());
        }
        String str2 = LOG_TAG;
        IMSLog.decreaseIndent(str2);
        IMSLog.dump(str2, "Active Sessions:");
        for (ImSession next : this.mImCache.getAllImSessions()) {
            String str3 = LOG_TAG;
            IMSLog.dump(str3, next.toString(), false);
            IMSLog.dump(str3, "Pending messages:");
            IMSLog.increaseIndent(str3);
            for (MessageBase messageBase : this.mImCache.getAllPendingMessages(next.getChatId())) {
                IMSLog.dump(LOG_TAG, messageBase.toString(), false);
            }
            IMSLog.decreaseIndent(LOG_TAG);
        }
        IMSLog.dump(LOG_TAG, "All Sessions:");
        try {
            for (ChatData chatId : this.mImCache.getPersister().querySessions((String) null)) {
                ImSession imSession = this.mImCache.getImSession(chatId.getChatId());
                if (imSession != null) {
                    String str4 = LOG_TAG;
                    IMSLog.dump(str4, imSession.toStringForDump(), false);
                    IMSLog.increaseIndent(str4);
                    Iterator<String> it2 = generateMessagesForDump(this.mImCache.getPersister().queryMessagesByChatIdForDump(imSession.getChatId(), 50)).iterator();
                    while (it2.hasNext()) {
                        IMSLog.dump(LOG_TAG, it2.next(), false);
                    }
                    IMSLog.decreaseIndent(LOG_TAG);
                }
            }
            IMSLog.decreaseIndent(LOG_TAG);
        } catch (SecurityException unused) {
        }
    }

    /* access modifiers changed from: protected */
    public void addEventLogs(String str) {
        this.date.setTime(System.currentTimeMillis());
        String format = this.timeFormat.format(this.date);
        try {
            ArrayBlockingQueue<String> arrayBlockingQueue = this.mEventLogs;
            if (!arrayBlockingQueue.offer(format + " " + str)) {
                this.mEventLogs.poll();
                ArrayBlockingQueue<String> arrayBlockingQueue2 = this.mEventLogs;
                arrayBlockingQueue2.add(format + " " + str);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* access modifiers changed from: protected */
    public ArrayList<String> generateMessagesForDump(Cursor cursor) {
        String str;
        String str2;
        if (cursor == null) {
            return null;
        }
        ArrayList<String> arrayList = new ArrayList<>();
        while (cursor.moveToNext()) {
            if (cursor.getInt(cursor.getColumnIndexOrThrow("message_type")) == 0) {
                str = "FtMessage [";
            } else {
                str = cursor.getInt(cursor.getColumnIndexOrThrow("message_type")) == 1 ? "ImMessage [" : "  Message [";
            }
            String str3 = str + "imdnId=" + cursor.getString(cursor.getColumnIndexOrThrow("imdn_message_id")) + ", type=" + cursor.getString(cursor.getColumnIndexOrThrow("message_type")) + ", status=" + cursor.getInt(cursor.getColumnIndexOrThrow("status")) + ", direction=" + cursor.getInt(cursor.getColumnIndexOrThrow("direction")) + ", sentTime=" + cursor.getLong(cursor.getColumnIndexOrThrow(ImContract.Message.SENT_TIMESTAMP)) + ", deliveredTime=" + cursor.getLong(cursor.getColumnIndexOrThrow(ImContract.ChatItem.DELIVERED_TIMESTAMP)) + ", NotificationStatus=" + cursor.getInt(cursor.getColumnIndexOrThrow("notification_status"));
            if (cursor.getInt(cursor.getColumnIndexOrThrow("message_type")) == 0) {
                str2 = str3 + ", filename=" + IMSLog.checker(cursor.getString(cursor.getColumnIndexOrThrow(ImContract.CsSession.FILE_NAME))) + ", transferredByte=" + cursor.getLong(cursor.getColumnIndexOrThrow(ImContract.CsSession.BYTES_TRANSFERRED)) + ", fileSize=" + cursor.getLong(cursor.getColumnIndexOrThrow(ImContract.CsSession.FILE_SIZE));
            } else {
                str2 = str3 + ", body=" + IMSLog.checker(cursor.getString(cursor.getColumnIndexOrThrow("body")));
            }
            arrayList.add(str2);
        }
        return arrayList;
    }

    /* access modifiers changed from: protected */
    public void dumpIncomingSession(int i, ImSession imSession, boolean z, boolean z2) {
        String str;
        if (imSession != null) {
            ArrayList arrayList = new ArrayList();
            arrayList.add(String.valueOf(imSession.getChatType().getId()));
            arrayList.add(ImsUtil.hideInfo(imSession.getConversationId(), 4));
            String str2 = "1";
            arrayList.add(z ? str2 : "0");
            if (z2) {
                str = str2;
            } else {
                str = "0";
            }
            arrayList.add(str);
            if (!imSession.isChatbotRole()) {
                str2 = "0";
            }
            arrayList.add(str2);
            ImsUtil.listToDumpFormat(LogClass.IM_RECV_SESSION, i, imSession.getChatId(), arrayList);
        }
    }

    /* access modifiers changed from: protected */
    public void dumpMessageSendingFailed(int i, ImSession imSession, Result result, String str, String str2) {
        ArrayList arrayList = new ArrayList();
        arrayList.add(ImsUtil.hideInfo(imSession.getConversationId(), 4));
        arrayList.add(ImsUtil.hideInfo(str, 4));
        if (!(result == null || result.getType() == Result.Type.NONE)) {
            arrayList.add(result.toCriticalLog());
        }
        arrayList.add(str2);
        ImsUtil.listToDumpFormat(LogClass.IM_SEND_RES, i, imSession.getChatId(), arrayList);
    }

    /* access modifiers changed from: protected */
    public void dumpIncomingMessageReceived(int i, boolean z, String str, String str2) {
        ArrayList arrayList = new ArrayList();
        arrayList.add(z ? "1" : "0");
        arrayList.add(ImsUtil.hideInfo(str2, 4));
        ImsUtil.listToDumpFormat(LogClass.IM_RECV_IM, i, str, arrayList);
    }
}
