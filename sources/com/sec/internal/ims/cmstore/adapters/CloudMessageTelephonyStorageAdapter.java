package com.sec.internal.ims.cmstore.adapters;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import com.sec.internal.ims.cmstore.helper.TelephonyDbHelper;
import com.sec.internal.interfaces.ims.cmstore.ITelephonyDBColumns;
import java.text.MessageFormat;
import java.util.Arrays;

public class CloudMessageTelephonyStorageAdapter {
    public static final String LOG_TAG = "CloudMessageTelephonyStorageAdapter";
    private final Context mContext;
    private final TelephonyDbHelper mTeleDBHelper;

    public CloudMessageTelephonyStorageAdapter(Context context) {
        this.mContext = context;
        this.mTeleDBHelper = new TelephonyDbHelper(context);
    }

    public Cursor getTelephonyAddr(long j) {
        return this.mTeleDBHelper.query(Uri.parse(MessageFormat.format("content://mms/{0}/addr", new Object[]{String.valueOf(j)})), (String[]) null, "msg_id=" + j, (String[]) null, (String) null);
    }

    public Cursor getTelephonyPart(long j) {
        return this.mTeleDBHelper.query(Uri.parse("content://mms/part"), (String[]) null, "mid=" + j, (String[]) null, (String) null);
    }

    public Cursor queryMMSPduFromTelephonyDbUseID(long j) {
        Uri uri = ITelephonyDBColumns.CONTENT_MMS;
        return this.mTeleDBHelper.query(uri, (String[]) null, "_id = " + j, (String[]) null, (String) null);
    }

    public Cursor querySMSfromTelephony(String[] strArr, String str, String[] strArr2, String str2) {
        return this.mTeleDBHelper.query(ITelephonyDBColumns.CONTENT_SMS, strArr, str, strArr2, str2);
    }

    public Cursor querySMSUseRowId(long j) {
        return this.mTeleDBHelper.query(ITelephonyDBColumns.CONTENT_SMS, (String[]) null, "_id=?", new String[]{Long.toString(j)}, (String) null);
    }

    public Cursor queryMMSPduFromTelephonyDb(String[] strArr, String str, String[] strArr2, String str2) {
        String str3 = LOG_TAG;
        Log.d(str3, "queryMMSPduFromTelephonyDb,  whereClaus: " + str + " selectionArgs: " + Arrays.toString(strArr2));
        return this.mTeleDBHelper.query(ITelephonyDBColumns.CONTENT_MMS, strArr, str, strArr2, str2);
    }

    public long getFtRowFromTelephonyDb(String str) {
        return this.mTeleDBHelper.getFtRowFromTelephony(str);
    }

    public Cursor queryAllSessionsFromTelephony(String str) {
        return this.mTeleDBHelper.queryAllSessionsFromTelephony(str);
    }

    public Cursor queryAllFtRCSFromTelephony(String str, String str2) {
        return this.mTeleDBHelper.queryAllFtRCSFromTelephony(str, str2);
    }

    public Cursor queryParticipantsUsingChatIdFromTP(String str) {
        return this.mTeleDBHelper.queryParticipantsUsingChatIdFromTP(str);
    }

    public Cursor queryParticipantsInfoFromTP(String str) {
        return this.mTeleDBHelper.queryParticipantsInfoFromTP(str);
    }

    public Cursor queryAllRCSChatFromTP(String str, String str2) {
        return this.mTeleDBHelper.queryAllRCSChatFromTP(str, str2);
    }
}
