package com.sec.internal.ims.cmstore.adapters;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import com.sec.internal.constants.ims.servicemodules.im.ImContract;
import com.sec.internal.ims.cmstore.helper.RCSDBHelper;
import java.util.ArrayList;

public class CloudMessageRCSStorageAdapter {
    public static final String LOG_TAG = "CloudMessageRCSStorageAdapter";
    public final String PROVIDER_NAME = ImContract.PROVIDER_NAME;
    private final Context mContext;
    private final RCSDBHelper mRCSDBHelper;

    public CloudMessageRCSStorageAdapter(Context context) {
        this.mContext = context;
        this.mRCSDBHelper = new RCSDBHelper(context);
    }

    public Uri insertMessageFromBufferDb(ContentValues contentValues) {
        Uri parse = Uri.parse("content://com.samsung.rcs.im/cloudinsertmessage");
        if (contentValues != null) {
            return this.mRCSDBHelper.insert(parse, contentValues);
        }
        return null;
    }

    public int insertSessionFromBufferDbToRCSDb(ContentValues contentValues, ArrayList<ContentValues> arrayList) {
        Uri parse = Uri.parse("content://com.samsung.rcs.im/cloudinsertsession");
        String str = LOG_TAG;
        Log.d(str, "insertSessionFromBufferDb: " + arrayList.size());
        if (arrayList.size() == 0) {
            Log.d(str, " empty participants list return");
            return 0;
        }
        ArrayList arrayList2 = new ArrayList();
        arrayList2.add(contentValues);
        arrayList2.addAll(arrayList);
        return this.mRCSDBHelper.insertSingleSessionPartsToDB(parse, (ContentValues[]) arrayList2.toArray(new ContentValues[arrayList2.size()]));
    }

    public int updateSessionFromBufferDbToRCSDb(String str, ContentValues contentValues) {
        return this.mRCSDBHelper.update(Uri.parse("content://com.samsung.rcs.im/cloudupdatesession/" + str), contentValues, "chat_id=?", new String[]{str});
    }

    public int updateParticipantsFromBufferDbToRCSDb(long j, ContentValues contentValues) {
        return this.mRCSDBHelper.update(Uri.parse("content://com.samsung.rcs.im/cloudupdateparticipant/" + j), contentValues, "_id =?", new String[]{String.valueOf(j)});
    }

    public Uri insertParticipantsFromBufferDbToRCSDb(ContentValues contentValues) {
        if (contentValues.get("sim_imsi") != null) {
            Log.i(LOG_TAG, "remove imsi for cloudinsertparticipant");
            contentValues.remove("sim_imsi");
        }
        return this.mRCSDBHelper.insert(Uri.parse("content://com.samsung.rcs.im/cloudinsertparticipant"), contentValues);
    }

    public int updateMessageFromBufferDb(int i, ContentValues contentValues) {
        return this.mRCSDBHelper.update(Uri.parse("content://com.samsung.rcs.im/cloudupdatemessage/" + i), contentValues, "_id=?", new String[]{String.valueOf(i)});
    }

    public Cursor queryAllSession() {
        return this.mRCSDBHelper.query(Uri.parse("content://com.samsung.rcs.im/session"), (String[]) null, (String) null, (String[]) null, (String) null);
    }

    public Cursor queryAllSession(String[] strArr) {
        return this.mRCSDBHelper.query(Uri.parse("content://com.samsung.rcs.im/session"), strArr, (String) null, (String[]) null, (String) null);
    }

    public Cursor queryAllSessionWithIMSI(String str) {
        Uri parse = Uri.parse("content://com.samsung.rcs.im/session");
        Log.d(LOG_TAG, "queryAllSession");
        return this.mRCSDBHelper.query(parse, (String[]) null, "sim_imsi=?", new String[]{str}, (String) null);
    }

    public Cursor queryAllMessage() {
        Uri parse = Uri.parse("content://com.samsung.rcs.im/message");
        Log.d(LOG_TAG, "queryAllMessage");
        return this.mRCSDBHelper.query(parse, (String[]) null, (String) null, (String[]) null, (String) null);
    }

    public Cursor queryAllMessage(String[] strArr) {
        Uri parse = Uri.parse("content://com.samsung.rcs.im/message");
        Log.d(LOG_TAG, "queryAllMessage");
        return this.mRCSDBHelper.query(parse, strArr, (String) null, (String[]) null, (String) null);
    }

    public Cursor queryIMFTUsingChatId(String str) {
        return this.mRCSDBHelper.query(Uri.parse("content://com.samsung.rcs.im/cloudquerymessagechatid/" + str), (String[]) null, (String) null, (String[]) null, (String) null);
    }

    public Cursor queryParticipantsUsingChatId(String str) {
        return this.mRCSDBHelper.query(Uri.parse("content://com.samsung.rcs.im/cloudqueryparticipant/" + str), (String[]) null, (String) null, (String[]) null, (String) null);
    }

    public int deleteParticipantsUsingRowId(long j) {
        return this.mRCSDBHelper.delete(Uri.parse("content://com.samsung.rcs.im/clouddeleteparticipant/" + j), "chat_id =?", new String[]{String.valueOf(j)});
    }

    public Cursor queryNotificationUsingImdn(String str) {
        return this.mRCSDBHelper.query(Uri.parse("content://com.samsung.rcs.im/messagenotifications/" + str), (String[]) null, (String) null, (String[]) null, (String) null);
    }

    public Cursor queryIMFTUsingRowId(long j) {
        return this.mRCSDBHelper.query(Uri.parse("content://com.samsung.rcs.im/cloudquerymessagerowid/" + j), (String[]) null, (String) null, (String[]) null, (String) null);
    }

    public Cursor queryRcsDBMessageUsingImdnId(String str) {
        return this.mRCSDBHelper.query(Uri.parse("content://com.samsung.rcs.im/cloudquerymessageimdnid/" + str), (String[]) null, (String) null, (String[]) null, (String) null);
    }

    public int deleteRCSDBmessageUsingId(int i) {
        return this.mRCSDBHelper.delete(Uri.parse("content://com.samsung.rcs.im/clouddeletemessage/" + i), "_id=?", new String[]{Integer.valueOf(i).toString()});
    }

    public Cursor querySessionUsingId(int i) {
        return this.mRCSDBHelper.query(Uri.parse("content://com.samsung.rcs.im/cloudquerysessionid/" + i), (String[]) null, (String) null, (String[]) null, (String) null);
    }

    public Cursor querySessionUsingChatId(String str) {
        return this.mRCSDBHelper.query(Uri.parse("content://com.samsung.rcs.im/cloudquerysessionchatid/" + str), (String[]) null, (String) null, (String[]) null, (String) null);
    }

    public Uri insertNotificationFromBufferDb(ContentValues contentValues) {
        if (contentValues == null) {
            return null;
        }
        return this.mRCSDBHelper.insert(Uri.parse("content://com.samsung.rcs.im/cloudinsertnotification"), contentValues);
    }

    public int updateRCSNotificationUsingImdnId(String str, ContentValues contentValues, String str2) {
        if (contentValues == null) {
            return 0;
        }
        return this.mRCSDBHelper.update(Uri.parse("content://com.samsung.rcs.im/cloudupdatenotification/" + str), contentValues, "imdn_id=? AND sender_uri=?", new String[]{str, str2});
    }
}
