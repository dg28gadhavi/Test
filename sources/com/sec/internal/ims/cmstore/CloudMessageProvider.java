package com.sec.internal.ims.cmstore;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.servicemodules.im.ImContract;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.cmstore.adapters.CloudMessageBufferDBPersister;
import com.sec.internal.ims.cmstore.helper.DebugFlag;
import com.sec.internal.ims.cmstore.utils.CmsUtil;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.log.IMSLog;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;

public class CloudMessageProvider extends ContentProvider {
    protected static final String LOG_TAG = CloudMessageProvider.class.getSimpleName();
    protected static String PROVIDER_NAME = "com.samsung.rcs.cmstore";
    private static final int SIM_1 = 0;
    private static final int SIM_2 = 1;
    protected static CloudMessageBufferDBPersister[] mBufferDB = new CloudMessageBufferDBPersister[2];
    private static final Hashtable<Integer, Boolean> mBufferDBInitialized = new Hashtable<>();
    private static boolean mDualDBRequired = false;
    protected static UriMatcher sUriMatcher = null;
    private static final String simSlot2 = "/slot2";

    public String getType(Uri uri) {
        return null;
    }

    static {
        UriMatcher uriMatcher = new UriMatcher(-1);
        sUriMatcher = uriMatcher;
        uriMatcher.addURI(PROVIDER_NAME, "smsmessages/#", 3);
        sUriMatcher.addURI(PROVIDER_NAME, "mmspdumessage/#", 4);
        sUriMatcher.addURI(PROVIDER_NAME, "mmsaddrmessages/#", 5);
        sUriMatcher.addURI(PROVIDER_NAME, "mmspartmessages/#", 6);
        sUriMatcher.addURI(PROVIDER_NAME, "mmspartmessages_partid/#", 8);
        sUriMatcher.addURI(PROVIDER_NAME, "rcschatmessage/#", 1);
        sUriMatcher.addURI(PROVIDER_NAME, "rcsftmessage/#", 1);
        sUriMatcher.addURI(PROVIDER_NAME, "rcsmessages/#", 1);
        sUriMatcher.addURI(PROVIDER_NAME, "notification/#", 13);
        sUriMatcher.addURI(PROVIDER_NAME, "summarytable/#", 7);
        sUriMatcher.addURI(PROVIDER_NAME, "latestmessage/#", 33);
        sUriMatcher.addURI(PROVIDER_NAME, CloudMessageProviderContract.CONTENTPRDR_ALL_SMSMESSAGES, 31);
        sUriMatcher.addURI(PROVIDER_NAME, CloudMessageProviderContract.CONTENTPRDR_ALL_MMSPDUMESSAGE, 32);
        sUriMatcher.addURI(PROVIDER_NAME, CloudMessageProviderContract.CONTENTPRDR_USER_OPT_IN_STATUS, 37);
        sUriMatcher.addURI(PROVIDER_NAME, "max_small_file_size", 40);
        sUriMatcher.addURI(PROVIDER_NAME, "smsmessages/slot2/#", 43);
        sUriMatcher.addURI(PROVIDER_NAME, "mmspdumessage/slot2/#", 44);
        sUriMatcher.addURI(PROVIDER_NAME, "mmsaddrmessages/slot2/#", 45);
        sUriMatcher.addURI(PROVIDER_NAME, "mmspartmessages/slot2/#", 46);
        sUriMatcher.addURI(PROVIDER_NAME, "mmspartmessages_partid/slot2/#", 48);
        sUriMatcher.addURI(PROVIDER_NAME, "rcschatmessage/slot2/#", 41);
        sUriMatcher.addURI(PROVIDER_NAME, "rcsftmessage/slot2/#", 41);
        sUriMatcher.addURI(PROVIDER_NAME, "rcsmessages/slot2/#", 41);
        sUriMatcher.addURI(PROVIDER_NAME, "rcsparticipants/slot2/*", 42);
        sUriMatcher.addURI(PROVIDER_NAME, "rcssession/slot2/*", 50);
        sUriMatcher.addURI(PROVIDER_NAME, "notification/slot2/#", 53);
        sUriMatcher.addURI(PROVIDER_NAME, "summarytable/slot2/#", 47);
        sUriMatcher.addURI(PROVIDER_NAME, "rcsmessageimdn/slot2/*", 55);
        sUriMatcher.addURI(PROVIDER_NAME, "notificationimdn/slot2/*", 72);
        sUriMatcher.addURI(PROVIDER_NAME, "pendingsmsmessages/slot2/*", 56);
        sUriMatcher.addURI(PROVIDER_NAME, "pendingmmspdumessage/slot2/*", 57);
        sUriMatcher.addURI(PROVIDER_NAME, "pendingrcschatmessage/slot2/*", 58);
        sUriMatcher.addURI(PROVIDER_NAME, "pendingrcsftmessage/slot2/*", 59);
        sUriMatcher.addURI(PROVIDER_NAME, "latestmessage/slot2/#", 62);
        sUriMatcher.addURI(PROVIDER_NAME, "allsmsmessages/slot2", 60);
        sUriMatcher.addURI(PROVIDER_NAME, "allmmspdumessage/slot2", 61);
        sUriMatcher.addURI(PROVIDER_NAME, "useroptinflag/slot2", 64);
        sUriMatcher.addURI(PROVIDER_NAME, "max_small_file_size/slot2", 73);
        sUriMatcher.addURI(PROVIDER_NAME, "vvmmessages/slot2/*", 65);
        sUriMatcher.addURI(PROVIDER_NAME, "vvmprofile/slot2/*", 68);
        sUriMatcher.addURI(PROVIDER_NAME, "vvmgreeting/slot2/*", 66);
        sUriMatcher.addURI(PROVIDER_NAME, "vvmpin/slot2/*", 67);
        sUriMatcher.addURI(PROVIDER_NAME, "vvmquota/slot2/*", 69);
        sUriMatcher.addURI(PROVIDER_NAME, "pendingvvmmessages/slot2/*", 70);
        sUriMatcher.addURI(PROVIDER_NAME, "multilinestatus/slot2/*", 71);
        sUriMatcher.addURI(PROVIDER_NAME, "rcsparticipants/*", 2);
        sUriMatcher.addURI(PROVIDER_NAME, "rcssession/*", 10);
        sUriMatcher.addURI(PROVIDER_NAME, "rcsmessageimdn/*", 15);
        sUriMatcher.addURI(PROVIDER_NAME, "notificationimdn/*", 39);
        sUriMatcher.addURI(PROVIDER_NAME, "vvmmessages/*", 17);
        sUriMatcher.addURI(PROVIDER_NAME, "vvmprofile/*", 20);
        sUriMatcher.addURI(PROVIDER_NAME, "vvmgreeting/*", 18);
        sUriMatcher.addURI(PROVIDER_NAME, "vvmpin/*", 19);
        sUriMatcher.addURI(PROVIDER_NAME, "vvmquota/*", 36);
        sUriMatcher.addURI(PROVIDER_NAME, "pendingsmsmessages/*", 24);
        sUriMatcher.addURI(PROVIDER_NAME, "pendingmmspdumessage/*", 25);
        sUriMatcher.addURI(PROVIDER_NAME, "pendingrcschatmessage/*", 26);
        sUriMatcher.addURI(PROVIDER_NAME, "pendingrcsftmessage/*", 27);
        sUriMatcher.addURI(PROVIDER_NAME, "pendingvvmmessages/*", 28);
        sUriMatcher.addURI(PROVIDER_NAME, "multilinestatus/*", 23);
        sUriMatcher.addURI(PROVIDER_NAME, CloudMessageProviderContract.CONTENTPRDR_USER_DEBUG_FLAG, 99);
        sUriMatcher.addURI(PROVIDER_NAME, CloudMessageProviderContract.CONTENTPRDR_MIGRATE_SUCCESS, 35);
        sUriMatcher.addURI(PROVIDER_NAME, CloudMessageProviderContract.CONTENTPRDR_AMBSFEATURE_VERSION, 98);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:?, code lost:
        return r3.deleteTable(1, r5, r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:?, code lost:
        return r3.deleteTable(7, r5, r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:?, code lost:
        return r3.deleteTable(6, r5, r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:?, code lost:
        return r3.deleteTable(5, r5, r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:?, code lost:
        return r3.deleteTable(4, r5, r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:?, code lost:
        return r3.deleteTable(3, r5, r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:?, code lost:
        return r3.deleteTable(2, r5, r6);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int delete(android.net.Uri r4, java.lang.String r5, java.lang.String[] r6) {
        /*
            r3 = this;
            java.lang.String r0 = LOG_TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "delete "
            r1.append(r2)
            r1.append(r4)
            java.lang.String r1 = r1.toString()
            com.sec.internal.log.IMSLog.s(r0, r1)
            com.sec.internal.ims.cmstore.adapters.CloudMessageBufferDBPersister r3 = r3.getSimSlotBuff(r4)
            android.content.UriMatcher r0 = sUriMatcher
            int r4 = r0.match(r4)
            switch(r4) {
                case 1: goto L_0x004c;
                case 2: goto L_0x0046;
                case 3: goto L_0x0040;
                case 4: goto L_0x003a;
                case 5: goto L_0x0034;
                case 6: goto L_0x002e;
                case 7: goto L_0x0028;
                default: goto L_0x0023;
            }
        L_0x0023:
            switch(r4) {
                case 41: goto L_0x004c;
                case 42: goto L_0x0046;
                case 43: goto L_0x0040;
                case 44: goto L_0x003a;
                case 45: goto L_0x0034;
                case 46: goto L_0x002e;
                case 47: goto L_0x0028;
                default: goto L_0x0026;
            }
        L_0x0026:
            r3 = 0
            goto L_0x0051
        L_0x0028:
            r4 = 7
            int r3 = r3.deleteTable(r4, r5, r6)
            goto L_0x0051
        L_0x002e:
            r4 = 6
            int r3 = r3.deleteTable(r4, r5, r6)
            goto L_0x0051
        L_0x0034:
            r4 = 5
            int r3 = r3.deleteTable(r4, r5, r6)
            goto L_0x0051
        L_0x003a:
            r4 = 4
            int r3 = r3.deleteTable(r4, r5, r6)
            goto L_0x0051
        L_0x0040:
            r4 = 3
            int r3 = r3.deleteTable(r4, r5, r6)
            goto L_0x0051
        L_0x0046:
            r4 = 2
            int r3 = r3.deleteTable(r4, r5, r6)
            goto L_0x0051
        L_0x004c:
            r4 = 1
            int r3 = r3.deleteTable(r4, r5, r6)
        L_0x0051:
            return r3
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.CloudMessageProvider.delete(android.net.Uri, java.lang.String, java.lang.String[]):int");
    }

    public Uri insert(Uri uri, ContentValues contentValues) {
        String str = LOG_TAG;
        IMSLog.s(str, "insert " + uri);
        return null;
    }

    private static void setDualDBEnable(MessageStoreClient messageStoreClient) {
        Mno simMno = SimUtil.getSimMno(messageStoreClient.getClientID());
        if (Mno.ATT.equals(simMno) || Mno.TMOUS.equals(simMno)) {
            IMSLog.i(LOG_TAG, "setDualDBEnable() mDualDBRequired set true for ATT&T/TMOUS case");
            mDualDBRequired = true;
            return;
        }
        IMSLog.i(LOG_TAG, "setDualDBEnable() non ATT&T/TMOUS case");
    }

    public static synchronized void createBufferDBInstance(MessageStoreClient messageStoreClient) {
        synchronized (CloudMessageProvider.class) {
            String str = LOG_TAG;
            IMSLog.i(str, "createBufferDBInstance() slot: " + messageStoreClient.getClientID() + ", mDualDBRequired: " + mDualDBRequired);
            Hashtable<Integer, Boolean> hashtable = mBufferDBInitialized;
            if (!hashtable.containsKey(Integer.valueOf(messageStoreClient.getClientID()))) {
                IMSLog.i(str, "createBufferDBInstance() DB not loaded");
                setDualDBEnable(messageStoreClient);
                initBufferDB(messageStoreClient);
                hashtable.put(Integer.valueOf(messageStoreClient.getClientID()), Boolean.TRUE);
            } else {
                IMSLog.i(str, "createBufferDBInstance() already loaded");
            }
        }
    }

    private static void initBufferDB(MessageStoreClient messageStoreClient) {
        int clientID = messageStoreClient.getClientID();
        String str = LOG_TAG;
        IMSLog.i(str, "initBufferDB() mDualDBRequired: " + mDualDBRequired + ", for slot: " + clientID);
        if (mDualDBRequired) {
            mBufferDB[clientID] = CloudMessageBufferDBPersister.getInstance(messageStoreClient.getContext(), clientID, mDualDBRequired);
            mBufferDB[clientID].load();
            return;
        }
        Hashtable<Integer, Boolean> hashtable = mBufferDBInitialized;
        if (hashtable.containsKey(0) || hashtable.containsKey(1)) {
            IMSLog.i(str, "initBufferDB() DB already loaded for single DB Case");
            return;
        }
        mBufferDB[0] = CloudMessageBufferDBPersister.getInstance(messageStoreClient.getContext(), 0, mDualDBRequired);
        mBufferDB[0].load();
    }

    public boolean onCreate() {
        IMSLog.s(LOG_TAG, "onCreate()");
        return true;
    }

    private Cursor removeCoTag(Cursor cursor) {
        if (cursor == null) {
            return null;
        }
        ArrayList arrayList = new ArrayList(Arrays.asList(cursor.getColumnNames()));
        if (arrayList.contains(CloudMessageProviderContract.BufferDBSMS.GROUP_COTAG)) {
            arrayList.remove(CloudMessageProviderContract.BufferDBSMS.GROUP_COTAG);
        }
        String[] strArr = (String[]) arrayList.toArray(new String[0]);
        MatrixCursor matrixCursor = new MatrixCursor(strArr);
        if (cursor.moveToFirst()) {
            do {
                MatrixCursor.RowBuilder newRow = matrixCursor.newRow();
                for (String columnIndex : strArr) {
                    int columnIndex2 = cursor.getColumnIndex(columnIndex);
                    int type = cursor.getType(columnIndex2);
                    if (type == 1) {
                        newRow.add(Long.valueOf(cursor.getLong(columnIndex2)));
                    } else if (type == 2) {
                        newRow.add(Float.valueOf(cursor.getFloat(columnIndex2)));
                    } else if (type == 3) {
                        newRow.add(cursor.getString(columnIndex2));
                    } else if (type != 4) {
                        newRow.add((Object) null);
                        Log.i(LOG_TAG, "Type default: " + type);
                    } else {
                        newRow.add(cursor.getBlob(columnIndex2));
                    }
                }
            } while (cursor.moveToNext());
        }
        return matrixCursor;
    }

    private CloudMessageBufferDBPersister getSimSlotBuff(Uri uri) {
        String str = LOG_TAG;
        IMSLog.d(str, "getSimSlotBuff mDualDBRequired: " + mDualDBRequired);
        if (sUriMatcher.match(uri) < 41 || sUriMatcher.match(uri) > 73) {
            return mBufferDB[0];
        }
        IMSLog.d(str, "getSimSlotBuff mDualDBRequired slot: 1");
        if (mDualDBRequired) {
            return mBufferDB[1];
        }
        return mBufferDB[0];
    }

    /* JADX WARNING: Code restructure failed: missing block: B:100:?, code lost:
        return r2.queryTable(3, r11, "syncdirection=? OR syncdirection=?", new java.lang.String[]{java.lang.String.valueOf(com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice.getId()), java.lang.String.valueOf(com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice.getId())}, r14);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:101:?, code lost:
        return r2.queryTable(r10, 23, r11, "linenum= ?", r14);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:102:?, code lost:
        return r2.queryTable(r10, 20, r11, "_bufferdbid= ?", r14);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:103:?, code lost:
        return r2.queryTable(r10, 19, r11, "_bufferdbid= ?", r14);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:104:?, code lost:
        return r2.queryTable(r10, 18, r11, "_bufferdbid= ?", r14);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:105:?, code lost:
        return r2.queryTable(r10, 17, r11, "_bufferdbid= ?", r14);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:106:?, code lost:
        return r2.queryTable(r10, 10, r11, r12, r14);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:107:?, code lost:
        return r2.queryTablewithBufferDbId(1, (long) java.lang.Integer.parseInt(r9));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:108:?, code lost:
        return r2.queryTable(r10, 6, r11, "_bufferdbid= ?", r14);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:109:?, code lost:
        return r2.queryTable(r10, 7, r11, "_bufferdbid= ?", r14);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:110:?, code lost:
        return r2.queryTable(r10, 6, r11, "mid= ?", r14);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:111:?, code lost:
        return r2.queryTable(r10, 5, r11, "msg_id= ?", r14);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:112:?, code lost:
        return r2.queryTable(r10, 4, r11, "_bufferdbid= ?", r14);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:113:?, code lost:
        return removeCoTag(r10);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:115:?, code lost:
        return r2.queryTable(r10, 2, r11, "chat_id= ?", r14);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:116:?, code lost:
        return r2.queryTable(r10, 1, r11, "_bufferdbid= ?", r14);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0103, code lost:
        r10 = java.lang.Integer.parseInt(r10.getLastPathSegment());
        android.util.Log.d(r0, "LASTEST Message DB index = " + r10);
        r13 = new java.lang.String[]{"MAX(_bufferdbid)"};
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0125, code lost:
        if (r10 == 1) goto L_0x0135;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x0128, code lost:
        if (r10 == 3) goto L_0x0135;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x012b, code lost:
        if (r10 == 4) goto L_0x0135;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x012f, code lost:
        if (r10 != 17) goto L_0x0132;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x0132, code lost:
        return null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x014b, code lost:
        r10 = r2.queryTable(31, r11, r12, r13, (java.lang.String) null);
        r10 = r10;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x015d, code lost:
        if (com.sec.internal.constants.ims.ImsConstants.Packages.PACKAGE_SEC_MSG.equals(getCallingPackage()) == false) goto L_0x02f0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x0267, code lost:
        if (android.text.TextUtils.isEmpty(r12) == false) goto L_0x026b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x0269, code lost:
        r12 = "chat_id= ?";
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x0277, code lost:
        r9 = r10.getLastPathSegment();
        android.util.Log.d(r0, "RCS_MESSAGE_ID bufferDB = " + r9);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:70:0x02d6, code lost:
        r10 = r2.queryTable(r10, 3, r11, "_bufferdbid= ?", r14);
        r10 = r10;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:71:0x02e8, code lost:
        if (com.sec.internal.constants.ims.ImsConstants.Packages.PACKAGE_SEC_MSG.equals(getCallingPackage()) == false) goto L_0x02f0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:89:?, code lost:
        return r2.queryTable(13, r11, r12, r13, r14);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:91:?, code lost:
        return r2.queryTable(r10, 36, r11, "_bufferdbid= ?", r14);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:93:?, code lost:
        return r2.queryTable(r10, (java.lang.String[]) null, (java.lang.String) null, r13, (java.lang.String) null);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:94:?, code lost:
        return r2.queryTable(32, r11, r12, r13, (java.lang.String) null);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:95:?, code lost:
        return removeCoTag(r10);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:96:?, code lost:
        return r2.queryTable(17, r11, "syncdirection=? OR syncdirection=?", new java.lang.String[]{java.lang.String.valueOf(com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice.getId()), java.lang.String.valueOf(com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice.getId())}, r14);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:97:?, code lost:
        return r2.queryTable(1, r11, "syncdirection=? OR syncdirection=? OR is_filetransfer=?", new java.lang.String[]{java.lang.String.valueOf(com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice.getId()), java.lang.String.valueOf(com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice.getId()), java.lang.String.valueOf(1)}, r14);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:98:?, code lost:
        return r2.queryTable(1, r11, "syncdirection=? OR syncdirection=? OR is_filetransfer=?", new java.lang.String[]{java.lang.String.valueOf(com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice.getId()), java.lang.String.valueOf(com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice.getId()), java.lang.String.valueOf(0)}, r14);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:99:?, code lost:
        return r2.queryTable(4, r11, "syncdirection=? OR syncdirection=?", new java.lang.String[]{java.lang.String.valueOf(com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice.getId()), java.lang.String.valueOf(com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice.getId())}, r14);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public android.database.Cursor query(android.net.Uri r10, java.lang.String[] r11, java.lang.String r12, java.lang.String[] r13, java.lang.String r14) {
        /*
            r9 = this;
            java.lang.String r0 = LOG_TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "query "
            r1.append(r2)
            r1.append(r10)
            java.lang.String r1 = r1.toString()
            com.sec.internal.log.IMSLog.s(r0, r1)
            com.sec.internal.ims.cmstore.adapters.CloudMessageBufferDBPersister r2 = r9.getSimSlotBuff(r10)
            android.content.UriMatcher r1 = sUriMatcher
            int r1 = r1.match(r10)
            r3 = 13
            if (r1 == r3) goto L_0x03a4
            r4 = 15
            if (r1 == r4) goto L_0x0399
            r4 = 53
            if (r1 == r4) goto L_0x03a4
            r3 = 98
            if (r1 == r3) goto L_0x036c
            r3 = 99
            r4 = 0
            if (r1 == r3) goto L_0x030b
            java.lang.String r8 = "com.samsung.android.messaging"
            r3 = 1
            switch(r1) {
                case 1: goto L_0x02ff;
                case 2: goto L_0x02f3;
                case 3: goto L_0x02d6;
                case 4: goto L_0x02ca;
                case 5: goto L_0x02be;
                case 6: goto L_0x02b2;
                case 7: goto L_0x02a6;
                case 8: goto L_0x029a;
                case 9: goto L_0x0277;
                case 10: goto L_0x0263;
                default: goto L_0x003c;
            }
        L_0x003c:
            switch(r1) {
                case 17: goto L_0x0256;
                case 18: goto L_0x0249;
                case 19: goto L_0x023c;
                case 20: goto L_0x022f;
                default: goto L_0x003f;
            }
        L_0x003f:
            switch(r1) {
                case 23: goto L_0x0222;
                case 24: goto L_0x01fe;
                case 25: goto L_0x01da;
                case 26: goto L_0x01b2;
                case 27: goto L_0x018a;
                case 28: goto L_0x0165;
                default: goto L_0x0042;
            }
        L_0x0042:
            switch(r1) {
                case 31: goto L_0x014b;
                case 32: goto L_0x013f;
                case 33: goto L_0x0103;
                default: goto L_0x0045;
            }
        L_0x0045:
            java.lang.String r5 = "opt_in_flag"
            switch(r1) {
                case 35: goto L_0x00d4;
                case 36: goto L_0x00c7;
                case 37: goto L_0x009a;
                default: goto L_0x004a;
            }
        L_0x004a:
            switch(r1) {
                case 39: goto L_0x008e;
                case 40: goto L_0x0088;
                case 41: goto L_0x02ff;
                case 42: goto L_0x02f3;
                case 43: goto L_0x02d6;
                case 44: goto L_0x02ca;
                case 45: goto L_0x02be;
                case 46: goto L_0x02b2;
                case 47: goto L_0x02a6;
                case 48: goto L_0x029a;
                case 49: goto L_0x0277;
                case 50: goto L_0x0263;
                default: goto L_0x004d;
            }
        L_0x004d:
            switch(r1) {
                case 55: goto L_0x0399;
                case 56: goto L_0x01fe;
                case 57: goto L_0x01da;
                case 58: goto L_0x01b2;
                case 59: goto L_0x018a;
                case 60: goto L_0x014b;
                case 61: goto L_0x013f;
                case 62: goto L_0x0103;
                default: goto L_0x0050;
            }
        L_0x0050:
            switch(r1) {
                case 64: goto L_0x005b;
                case 65: goto L_0x0256;
                case 66: goto L_0x0249;
                case 67: goto L_0x023c;
                case 68: goto L_0x022f;
                case 69: goto L_0x00c7;
                case 70: goto L_0x0165;
                case 71: goto L_0x0222;
                case 72: goto L_0x008e;
                case 73: goto L_0x0055;
                default: goto L_0x0053;
            }
        L_0x0053:
            goto L_0x0132
        L_0x0055:
            android.database.Cursor r9 = r9.getMaxSmallFileSize(r3)
            goto L_0x03c5
        L_0x005b:
            java.lang.String r9 = "OPT_IN_STATUS2"
            com.sec.internal.log.IMSLog.s(r0, r9)
            com.sec.internal.ims.cmstore.MessageStoreClient r9 = com.sec.internal.ims.cmstore.CloudMessageService.getClientByID(r3)
            if (r9 == 0) goto L_0x0072
            com.sec.internal.ims.cmstore.MessageStoreClient r9 = com.sec.internal.ims.cmstore.CloudMessageService.getClientByID(r3)
            com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager r9 = r9.getPrerenceManager()
            boolean r4 = r9.ifSteadyState()
        L_0x0072:
            android.database.MatrixCursor r9 = new android.database.MatrixCursor
            java.lang.String[] r10 = new java.lang.String[]{r5}
            r9.<init>(r10)
            android.database.MatrixCursor$RowBuilder r10 = r9.newRow()
            java.lang.Integer r11 = java.lang.Integer.valueOf(r4)
            r10.add(r11)
            goto L_0x03c5
        L_0x0088:
            android.database.Cursor r9 = r9.getMaxSmallFileSize(r4)
            goto L_0x03c5
        L_0x008e:
            r3 = 13
            r4 = r11
            r5 = r12
            r6 = r13
            r7 = r14
            android.database.Cursor r9 = r2.queryTable((int) r3, (java.lang.String[]) r4, (java.lang.String) r5, (java.lang.String[]) r6, (java.lang.String) r7)
            goto L_0x03c5
        L_0x009a:
            java.lang.String r9 = "OPT_IN_STATUS1"
            com.sec.internal.log.IMSLog.s(r0, r9)
            com.sec.internal.ims.cmstore.MessageStoreClient r9 = com.sec.internal.ims.cmstore.CloudMessageService.getClientByID(r4)
            if (r9 == 0) goto L_0x00b1
            com.sec.internal.ims.cmstore.MessageStoreClient r9 = com.sec.internal.ims.cmstore.CloudMessageService.getClientByID(r4)
            com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager r9 = r9.getPrerenceManager()
            boolean r4 = r9.ifSteadyState()
        L_0x00b1:
            android.database.MatrixCursor r9 = new android.database.MatrixCursor
            java.lang.String[] r10 = new java.lang.String[]{r5}
            r9.<init>(r10)
            android.database.MatrixCursor$RowBuilder r10 = r9.newRow()
            java.lang.Integer r11 = java.lang.Integer.valueOf(r4)
            r10.add(r11)
            goto L_0x03c5
        L_0x00c7:
            java.lang.String r6 = "_bufferdbid= ?"
            r4 = 36
            r3 = r10
            r5 = r11
            r7 = r14
            android.database.Cursor r9 = r2.queryTable((android.net.Uri) r3, (int) r4, (java.lang.String[]) r5, (java.lang.String) r6, (java.lang.String) r7)
            goto L_0x03c5
        L_0x00d4:
            java.lang.String r9 = "DATABASE MIGRATE FLAG"
            com.sec.internal.log.IMSLog.s(r0, r9)
            com.sec.internal.ims.cmstore.MessageStoreClient r9 = com.sec.internal.ims.cmstore.CloudMessageService.getClientByID(r4)
            if (r9 == 0) goto L_0x00eb
            com.sec.internal.ims.cmstore.MessageStoreClient r9 = com.sec.internal.ims.cmstore.CloudMessageService.getClientByID(r4)
            com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager r9 = r9.getPrerenceManager()
            boolean r4 = r9.getMigrateSuccessFlag()
        L_0x00eb:
            android.database.MatrixCursor r9 = new android.database.MatrixCursor
            java.lang.String r10 = "migrate_success"
            java.lang.String[] r10 = new java.lang.String[]{r10}
            r9.<init>(r10)
            android.database.MatrixCursor$RowBuilder r10 = r9.newRow()
            java.lang.Integer r11 = java.lang.Integer.valueOf(r4)
            r10.add(r11)
            goto L_0x03c5
        L_0x0103:
            java.lang.String r9 = r10.getLastPathSegment()
            int r10 = java.lang.Integer.parseInt(r9)
            java.lang.StringBuilder r9 = new java.lang.StringBuilder
            r9.<init>()
            java.lang.String r11 = "LASTEST Message DB index = "
            r9.append(r11)
            r9.append(r10)
            java.lang.String r9 = r9.toString()
            android.util.Log.d(r0, r9)
            java.lang.String r9 = "MAX(_bufferdbid)"
            java.lang.String[] r13 = new java.lang.String[]{r9}
            if (r10 == r3) goto L_0x0135
            r9 = 3
            if (r10 == r9) goto L_0x0135
            r9 = 4
            if (r10 == r9) goto L_0x0135
            r9 = 17
            if (r10 != r9) goto L_0x0132
            goto L_0x0135
        L_0x0132:
            r9 = 0
            goto L_0x03c5
        L_0x0135:
            r11 = 0
            r12 = 0
            r14 = 0
            r9 = r2
            android.database.Cursor r9 = r9.queryTable((int) r10, (java.lang.String[]) r11, (java.lang.String) r12, (java.lang.String[]) r13, (java.lang.String) r14)
            goto L_0x03c5
        L_0x013f:
            r3 = 32
            r7 = 0
            r4 = r11
            r5 = r12
            r6 = r13
            android.database.Cursor r9 = r2.queryTable((int) r3, (java.lang.String[]) r4, (java.lang.String) r5, (java.lang.String[]) r6, (java.lang.String) r7)
            goto L_0x03c5
        L_0x014b:
            r3 = 31
            r7 = 0
            r4 = r11
            r5 = r12
            r6 = r13
            android.database.Cursor r10 = r2.queryTable((int) r3, (java.lang.String[]) r4, (java.lang.String) r5, (java.lang.String[]) r6, (java.lang.String) r7)
            java.lang.String r11 = r9.getCallingPackage()
            boolean r11 = r8.equals(r11)
            if (r11 == 0) goto L_0x02f0
            android.database.Cursor r9 = r9.removeCoTag(r10)
            goto L_0x03c5
        L_0x0165:
            java.lang.String r5 = "syncdirection=? OR syncdirection=?"
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r9 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice
            int r9 = r9.getId()
            java.lang.String r9 = java.lang.String.valueOf(r9)
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r10 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice
            int r10 = r10.getId()
            java.lang.String r10 = java.lang.String.valueOf(r10)
            java.lang.String[] r6 = new java.lang.String[]{r9, r10}
            r3 = 17
            r4 = r11
            r7 = r14
            android.database.Cursor r9 = r2.queryTable((int) r3, (java.lang.String[]) r4, (java.lang.String) r5, (java.lang.String[]) r6, (java.lang.String) r7)
            goto L_0x03c5
        L_0x018a:
            java.lang.String r5 = "syncdirection=? OR syncdirection=? OR is_filetransfer=?"
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r9 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice
            int r9 = r9.getId()
            java.lang.String r9 = java.lang.String.valueOf(r9)
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r10 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice
            int r10 = r10.getId()
            java.lang.String r10 = java.lang.String.valueOf(r10)
            java.lang.String r12 = java.lang.String.valueOf(r3)
            java.lang.String[] r6 = new java.lang.String[]{r9, r10, r12}
            r3 = 1
            r4 = r11
            r7 = r14
            android.database.Cursor r9 = r2.queryTable((int) r3, (java.lang.String[]) r4, (java.lang.String) r5, (java.lang.String[]) r6, (java.lang.String) r7)
            goto L_0x03c5
        L_0x01b2:
            java.lang.String r5 = "syncdirection=? OR syncdirection=? OR is_filetransfer=?"
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r9 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice
            int r9 = r9.getId()
            java.lang.String r9 = java.lang.String.valueOf(r9)
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r10 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice
            int r10 = r10.getId()
            java.lang.String r10 = java.lang.String.valueOf(r10)
            java.lang.String r12 = java.lang.String.valueOf(r4)
            java.lang.String[] r6 = new java.lang.String[]{r9, r10, r12}
            r3 = 1
            r4 = r11
            r7 = r14
            android.database.Cursor r9 = r2.queryTable((int) r3, (java.lang.String[]) r4, (java.lang.String) r5, (java.lang.String[]) r6, (java.lang.String) r7)
            goto L_0x03c5
        L_0x01da:
            java.lang.String r5 = "syncdirection=? OR syncdirection=?"
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r9 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice
            int r9 = r9.getId()
            java.lang.String r9 = java.lang.String.valueOf(r9)
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r10 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice
            int r10 = r10.getId()
            java.lang.String r10 = java.lang.String.valueOf(r10)
            java.lang.String[] r6 = new java.lang.String[]{r9, r10}
            r3 = 4
            r4 = r11
            r7 = r14
            android.database.Cursor r9 = r2.queryTable((int) r3, (java.lang.String[]) r4, (java.lang.String) r5, (java.lang.String[]) r6, (java.lang.String) r7)
            goto L_0x03c5
        L_0x01fe:
            java.lang.String r5 = "syncdirection=? OR syncdirection=?"
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r9 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice
            int r9 = r9.getId()
            java.lang.String r9 = java.lang.String.valueOf(r9)
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r10 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice
            int r10 = r10.getId()
            java.lang.String r10 = java.lang.String.valueOf(r10)
            java.lang.String[] r6 = new java.lang.String[]{r9, r10}
            r3 = 3
            r4 = r11
            r7 = r14
            android.database.Cursor r9 = r2.queryTable((int) r3, (java.lang.String[]) r4, (java.lang.String) r5, (java.lang.String[]) r6, (java.lang.String) r7)
            goto L_0x03c5
        L_0x0222:
            java.lang.String r6 = "linenum= ?"
            r4 = 23
            r3 = r10
            r5 = r11
            r7 = r14
            android.database.Cursor r9 = r2.queryTable((android.net.Uri) r3, (int) r4, (java.lang.String[]) r5, (java.lang.String) r6, (java.lang.String) r7)
            goto L_0x03c5
        L_0x022f:
            java.lang.String r6 = "_bufferdbid= ?"
            r4 = 20
            r3 = r10
            r5 = r11
            r7 = r14
            android.database.Cursor r9 = r2.queryTable((android.net.Uri) r3, (int) r4, (java.lang.String[]) r5, (java.lang.String) r6, (java.lang.String) r7)
            goto L_0x03c5
        L_0x023c:
            java.lang.String r6 = "_bufferdbid= ?"
            r4 = 19
            r3 = r10
            r5 = r11
            r7 = r14
            android.database.Cursor r9 = r2.queryTable((android.net.Uri) r3, (int) r4, (java.lang.String[]) r5, (java.lang.String) r6, (java.lang.String) r7)
            goto L_0x03c5
        L_0x0249:
            java.lang.String r6 = "_bufferdbid= ?"
            r4 = 18
            r3 = r10
            r5 = r11
            r7 = r14
            android.database.Cursor r9 = r2.queryTable((android.net.Uri) r3, (int) r4, (java.lang.String[]) r5, (java.lang.String) r6, (java.lang.String) r7)
            goto L_0x03c5
        L_0x0256:
            java.lang.String r6 = "_bufferdbid= ?"
            r4 = 17
            r3 = r10
            r5 = r11
            r7 = r14
            android.database.Cursor r9 = r2.queryTable((android.net.Uri) r3, (int) r4, (java.lang.String[]) r5, (java.lang.String) r6, (java.lang.String) r7)
            goto L_0x03c5
        L_0x0263:
            boolean r9 = android.text.TextUtils.isEmpty(r12)
            if (r9 == 0) goto L_0x026b
            java.lang.String r12 = "chat_id= ?"
        L_0x026b:
            r6 = r12
            r4 = 10
            r3 = r10
            r5 = r11
            r7 = r14
            android.database.Cursor r9 = r2.queryTable((android.net.Uri) r3, (int) r4, (java.lang.String[]) r5, (java.lang.String) r6, (java.lang.String) r7)
            goto L_0x03c5
        L_0x0277:
            java.lang.String r9 = r10.getLastPathSegment()
            java.lang.StringBuilder r10 = new java.lang.StringBuilder
            r10.<init>()
            java.lang.String r11 = "RCS_MESSAGE_ID bufferDB = "
            r10.append(r11)
            r10.append(r9)
            java.lang.String r10 = r10.toString()
            android.util.Log.d(r0, r10)
            int r9 = java.lang.Integer.parseInt(r9)
            long r9 = (long) r9
            android.database.Cursor r9 = r2.queryTablewithBufferDbId(r3, r9)
            goto L_0x03c5
        L_0x029a:
            java.lang.String r6 = "_bufferdbid= ?"
            r4 = 6
            r3 = r10
            r5 = r11
            r7 = r14
            android.database.Cursor r9 = r2.queryTable((android.net.Uri) r3, (int) r4, (java.lang.String[]) r5, (java.lang.String) r6, (java.lang.String) r7)
            goto L_0x03c5
        L_0x02a6:
            java.lang.String r6 = "_bufferdbid= ?"
            r4 = 7
            r3 = r10
            r5 = r11
            r7 = r14
            android.database.Cursor r9 = r2.queryTable((android.net.Uri) r3, (int) r4, (java.lang.String[]) r5, (java.lang.String) r6, (java.lang.String) r7)
            goto L_0x03c5
        L_0x02b2:
            java.lang.String r6 = "mid= ?"
            r4 = 6
            r3 = r10
            r5 = r11
            r7 = r14
            android.database.Cursor r9 = r2.queryTable((android.net.Uri) r3, (int) r4, (java.lang.String[]) r5, (java.lang.String) r6, (java.lang.String) r7)
            goto L_0x03c5
        L_0x02be:
            java.lang.String r6 = "msg_id= ?"
            r4 = 5
            r3 = r10
            r5 = r11
            r7 = r14
            android.database.Cursor r9 = r2.queryTable((android.net.Uri) r3, (int) r4, (java.lang.String[]) r5, (java.lang.String) r6, (java.lang.String) r7)
            goto L_0x03c5
        L_0x02ca:
            java.lang.String r6 = "_bufferdbid= ?"
            r4 = 4
            r3 = r10
            r5 = r11
            r7 = r14
            android.database.Cursor r9 = r2.queryTable((android.net.Uri) r3, (int) r4, (java.lang.String[]) r5, (java.lang.String) r6, (java.lang.String) r7)
            goto L_0x03c5
        L_0x02d6:
            java.lang.String r6 = "_bufferdbid= ?"
            r4 = 3
            r3 = r10
            r5 = r11
            r7 = r14
            android.database.Cursor r10 = r2.queryTable((android.net.Uri) r3, (int) r4, (java.lang.String[]) r5, (java.lang.String) r6, (java.lang.String) r7)
            java.lang.String r11 = r9.getCallingPackage()
            boolean r11 = r8.equals(r11)
            if (r11 == 0) goto L_0x02f0
            android.database.Cursor r9 = r9.removeCoTag(r10)
            goto L_0x03c5
        L_0x02f0:
            r9 = r10
            goto L_0x03c5
        L_0x02f3:
            java.lang.String r6 = "chat_id= ?"
            r4 = 2
            r3 = r10
            r5 = r11
            r7 = r14
            android.database.Cursor r9 = r2.queryTable((android.net.Uri) r3, (int) r4, (java.lang.String[]) r5, (java.lang.String) r6, (java.lang.String) r7)
            goto L_0x03c5
        L_0x02ff:
            java.lang.String r6 = "_bufferdbid= ?"
            r4 = 1
            r3 = r10
            r5 = r11
            r7 = r14
            android.database.Cursor r9 = r2.queryTable((android.net.Uri) r3, (int) r4, (java.lang.String[]) r5, (java.lang.String) r6, (java.lang.String) r7)
            goto L_0x03c5
        L_0x030b:
            java.lang.String r9 = "USER_DEBUG_FLAG"
            com.sec.internal.log.IMSLog.s(r0, r9)
            com.sec.internal.ims.cmstore.MessageStoreClient r9 = com.sec.internal.ims.cmstore.CloudMessageService.getClientByID(r4)
            if (r9 == 0) goto L_0x0321
            com.sec.internal.ims.cmstore.MessageStoreClient r9 = com.sec.internal.ims.cmstore.CloudMessageService.getClientByID(r4)
            com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager r9 = r9.getPrerenceManager()
            r9.initUserDebug()
        L_0x0321:
            android.database.MatrixCursor r9 = new android.database.MatrixCursor
            java.lang.String r0 = "AMBS_DEBUG"
            java.lang.String r1 = "app_id"
            java.lang.String r2 = "cps_host_name"
            java.lang.String r3 = "auth_host_name"
            java.lang.String r4 = "retry_time"
            java.lang.String r5 = "nc_host_name"
            java.lang.String r6 = "mcs_url"
            java.lang.String r7 = "oasis_version"
            java.lang.String[] r10 = new java.lang.String[]{r0, r1, r2, r3, r4, r5, r6, r7}
            r9.<init>(r10)
            android.database.MatrixCursor$RowBuilder r10 = r9.newRow()
            boolean r11 = com.sec.internal.ims.cmstore.helper.DebugFlag.DEBUG_RETRY_TIMELINE_FLAG
            java.lang.Boolean r11 = java.lang.Boolean.valueOf(r11)
            r10.add(r11)
            java.lang.String r11 = com.sec.internal.ims.cmstore.helper.ATTGlobalVariables.APP_ID
            r10.add(r11)
            java.lang.String r11 = com.sec.internal.ims.cmstore.helper.ATTGlobalVariables.CPS_HOST_NAME
            r10.add(r11)
            java.lang.String r11 = com.sec.internal.ims.cmstore.helper.ATTGlobalVariables.ACMS_HOST_NAME
            r10.add(r11)
            java.lang.String r11 = com.sec.internal.ims.cmstore.helper.DebugFlag.debugRetryTimeLine
            r10.add(r11)
            java.lang.String r11 = com.sec.internal.ims.cmstore.helper.ATTGlobalVariables.DEFAULT_PRODUCT_NC_HOST
            r10.add(r11)
            java.lang.String r11 = com.sec.internal.ims.cmstore.helper.DebugFlag.DEBUG_MCS_URL
            r10.add(r11)
            java.lang.String r11 = com.sec.internal.ims.cmstore.helper.DebugFlag.DEBUG_OASIS_VERSION
            r10.add(r11)
            goto L_0x03c5
        L_0x036c:
            java.lang.String r9 = "QUERY AMBS VERSION "
            com.sec.internal.log.IMSLog.s(r0, r9)
            boolean r9 = com.sec.internal.ims.cmstore.helper.ATTGlobalVariables.supportSignedBinary()
            android.database.MatrixCursor r10 = new android.database.MatrixCursor
            java.lang.String r11 = "version_num"
            java.lang.String r12 = "version_desc"
            java.lang.String[] r11 = new java.lang.String[]{r11, r12}
            r10.<init>(r11)
            android.database.MatrixCursor$RowBuilder r11 = r10.newRow()
            java.lang.Integer r12 = java.lang.Integer.valueOf(r9)
            r11.add(r12)
            if (r9 == 0) goto L_0x02f0
            java.lang.String r9 = "sbsms"
            r11.add(r9)
            goto L_0x02f0
        L_0x0399:
            java.lang.String r6 = "imdn_message_id= ?"
            r4 = 1
            r3 = r10
            r5 = r11
            r7 = r14
            android.database.Cursor r9 = r2.queryTable((android.net.Uri) r3, (int) r4, (java.lang.String[]) r5, (java.lang.String) r6, (java.lang.String) r7)
            goto L_0x03c5
        L_0x03a4:
            java.lang.String r9 = r10.getLastPathSegment()
            java.lang.StringBuilder r10 = new java.lang.StringBuilder
            r10.<init>()
            java.lang.String r11 = "RCS_MESSAGES_IMDN bufferDB = "
            r10.append(r11)
            r10.append(r9)
            java.lang.String r10 = r10.toString()
            android.util.Log.d(r0, r10)
            int r9 = java.lang.Integer.parseInt(r9)
            long r9 = (long) r9
            android.database.Cursor r9 = r2.queryTablewithBufferDbId(r3, r9)
        L_0x03c5:
            return r9
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.CloudMessageProvider.query(android.net.Uri, java.lang.String[], java.lang.String, java.lang.String[], java.lang.String):android.database.Cursor");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:42:?, code lost:
        return r1.updateTable(7, r7, r8, r9);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:?, code lost:
        return r1.updateTable(6, r7, r8, r9);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:?, code lost:
        return r1.updateTable(5, r7, r8, r9);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:?, code lost:
        return r1.updateTable(4, r7, r8, r9);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:?, code lost:
        return r1.updateTable(3, r7, r8, r9);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:?, code lost:
        return r1.updateTable(2, r7, r8, r9);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:?, code lost:
        return r1.updateTable(1, r7, r8, r9);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int update(android.net.Uri r6, android.content.ContentValues r7, java.lang.String r8, java.lang.String[] r9) {
        /*
            r5 = this;
            java.lang.String r0 = LOG_TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "update "
            r1.append(r2)
            r1.append(r6)
            java.lang.String r1 = r1.toString()
            com.sec.internal.log.IMSLog.s(r0, r1)
            com.sec.internal.ims.cmstore.adapters.CloudMessageBufferDBPersister r1 = r5.getSimSlotBuff(r6)
            android.content.UriMatcher r2 = sUriMatcher
            int r6 = r2.match(r6)
            r2 = 99
            r3 = 1
            r4 = 0
            if (r6 == r2) goto L_0x005f
            switch(r6) {
                case 1: goto L_0x0059;
                case 2: goto L_0x0052;
                case 3: goto L_0x004b;
                case 4: goto L_0x0044;
                case 5: goto L_0x003d;
                case 6: goto L_0x0036;
                case 7: goto L_0x002f;
                default: goto L_0x002a;
            }
        L_0x002a:
            switch(r6) {
                case 41: goto L_0x0059;
                case 42: goto L_0x0052;
                case 43: goto L_0x004b;
                case 44: goto L_0x0044;
                case 45: goto L_0x003d;
                case 46: goto L_0x0036;
                case 47: goto L_0x002f;
                default: goto L_0x002d;
            }
        L_0x002d:
            goto L_0x0152
        L_0x002f:
            r5 = 7
            int r4 = r1.updateTable(r5, r7, r8, r9)
            goto L_0x0152
        L_0x0036:
            r5 = 6
            int r4 = r1.updateTable(r5, r7, r8, r9)
            goto L_0x0152
        L_0x003d:
            r5 = 5
            int r4 = r1.updateTable(r5, r7, r8, r9)
            goto L_0x0152
        L_0x0044:
            r5 = 4
            int r4 = r1.updateTable(r5, r7, r8, r9)
            goto L_0x0152
        L_0x004b:
            r5 = 3
            int r4 = r1.updateTable(r5, r7, r8, r9)
            goto L_0x0152
        L_0x0052:
            r5 = 2
            int r4 = r1.updateTable(r5, r7, r8, r9)
            goto L_0x0152
        L_0x0059:
            int r4 = r1.updateTable(r3, r7, r8, r9)
            goto L_0x0152
        L_0x005f:
            java.lang.String r6 = "AMBS_DEBUG"
            java.lang.Boolean r6 = r7.getAsBoolean(r6)
            java.lang.StringBuilder r8 = new java.lang.StringBuilder
            r8.<init>()
            java.lang.String r9 = "Debug enabled?: "
            r8.append(r9)
            if (r6 == 0) goto L_0x0073
            r9 = r6
            goto L_0x0075
        L_0x0073:
            java.lang.String r9 = "null"
        L_0x0075:
            r8.append(r9)
            java.lang.String r8 = r8.toString()
            android.util.Log.d(r0, r8)
            if (r6 == 0) goto L_0x00bc
            boolean r6 = r6.booleanValue()
            if (r6 == 0) goto L_0x00bc
            java.lang.String r6 = "app_id"
            java.lang.String r6 = r7.getAsString(r6)
            java.lang.String r8 = "cps_host_name"
            java.lang.String r8 = r7.getAsString(r8)
            java.lang.String r9 = "auth_host_name"
            java.lang.String r9 = r7.getAsString(r9)
            java.lang.String r0 = "retry_time"
            java.lang.String r0 = r7.getAsString(r0)
            java.lang.String r1 = "nc_host_name"
            java.lang.String r1 = r7.getAsString(r1)
            java.lang.String r2 = "mcs_url"
            java.lang.String r7 = r7.getAsString(r2)
            com.sec.internal.ims.cmstore.helper.DebugFlag.DEBUG_MCS_URL = r7
            com.sec.internal.ims.cmstore.helper.ATTGlobalVariables.setValue(r6, r9, r8, r1)
            com.sec.internal.ims.cmstore.helper.ATTGlobalVariables.setDebugHttps(r3)
            com.sec.internal.ims.cmstore.helper.DebugFlag.DEBUG_RETRY_TIMELINE_FLAG = r3
            if (r0 == 0) goto L_0x00cb
            com.sec.internal.ims.cmstore.helper.DebugFlag.setRetryTimeLine(r0)
            goto L_0x00cb
        L_0x00bc:
            com.sec.internal.ims.cmstore.helper.ATTGlobalVariables.initDefault()
            com.sec.internal.ims.cmstore.helper.DebugFlag.DEBUG_RETRY_TIMELINE_FLAG = r4
            com.sec.internal.ims.cmstore.helper.DebugFlag.initRetryTimeLine()
            com.sec.internal.ims.cmstore.helper.ATTGlobalVariables.setDebugHttps(r4)
            java.lang.String r6 = "https://rapi.rcsoasis.kr"
            com.sec.internal.ims.cmstore.helper.DebugFlag.DEBUG_MCS_URL = r6
        L_0x00cb:
            int r6 = com.sec.internal.helper.SimUtil.getActiveDataPhoneId()
            com.sec.internal.ims.cmstore.MessageStoreClient r6 = com.sec.internal.ims.cmstore.CloudMessageService.getClientByID(r6)
            com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager r6 = r6.getPrerenceManager()
            java.lang.String r6 = r6.getOasisServerVersion()
            com.sec.internal.ims.cmstore.helper.DebugFlag.DEBUG_OASIS_VERSION = r6
            r6 = r4
        L_0x00de:
            int r7 = com.sec.internal.helper.SimUtil.getPhoneCount()
            if (r6 >= r7) goto L_0x0152
            com.sec.internal.ims.cmstore.MessageStoreClient r7 = com.sec.internal.ims.cmstore.CloudMessageService.getClientByID(r6)
            if (r7 == 0) goto L_0x014f
            java.lang.String r7 = com.sec.internal.ims.cmstore.helper.DebugFlag.DEBUG_MCS_URL
            boolean r7 = r5.needToResetMcsData(r6, r7)
            if (r7 == 0) goto L_0x0133
            com.sec.internal.ims.cmstore.MessageStoreClient r7 = com.sec.internal.ims.cmstore.CloudMessageService.getClientByID(r6)
            com.sec.internal.ims.cmstore.mcs.provision.workflow.WorkflowMcs r7 = r7.getProvisionWorkFlow()
            r7.resetMcsData()
            com.sec.internal.ims.cmstore.MessageStoreClient r7 = com.sec.internal.ims.cmstore.CloudMessageService.getClientByID(r6)
            com.sec.internal.ims.cmstore.mcs.provision.workflow.WorkflowMcs r7 = r7.getProvisionWorkFlow()
            r7.clearWorkflow()
            int r7 = com.sec.internal.helper.SimUtil.getOppositeSimSlot(r6)
            com.sec.internal.ims.cmstore.MessageStoreClient r8 = com.sec.internal.ims.cmstore.CloudMessageService.getClientByID(r7)
            if (r8 == 0) goto L_0x0128
            com.sec.internal.ims.cmstore.MessageStoreClient r8 = com.sec.internal.ims.cmstore.CloudMessageService.getClientByID(r7)
            com.sec.internal.ims.cmstore.mcs.provision.workflow.WorkflowMcs r8 = r8.getProvisionWorkFlow()
            r8.resetMcsData()
            com.sec.internal.ims.cmstore.MessageStoreClient r7 = com.sec.internal.ims.cmstore.CloudMessageService.getClientByID(r7)
            com.sec.internal.ims.cmstore.mcs.provision.workflow.WorkflowMcs r7 = r7.getProvisionWorkFlow()
            r7.clearWorkflow()
        L_0x0128:
            com.sec.internal.ims.cmstore.MessageStoreClient r7 = com.sec.internal.ims.cmstore.CloudMessageService.getClientByID(r6)
            com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager r7 = r7.getPrerenceManager()
            r7.saveUserDebug()
        L_0x0133:
            com.sec.internal.ims.cmstore.MessageStoreClient r7 = com.sec.internal.ims.cmstore.CloudMessageService.getClientByID(r6)
            com.sec.internal.ims.cmstore.NetAPIWorkingStatusController r7 = r7.getNetAPIWorkingStatusController()
            r7.resetMcsRestartReceiver()
            boolean r7 = com.sec.internal.ims.cmstore.helper.ATTGlobalVariables.supportSignedBinary()
            if (r7 == 0) goto L_0x014f
            com.sec.internal.ims.cmstore.MessageStoreClient r7 = com.sec.internal.ims.cmstore.CloudMessageService.getClientByID(r6)
            com.sec.internal.ims.cmstore.NetAPIWorkingStatusController r7 = r7.getNetAPIWorkingStatusController()
            r7.resetDataReceiver()
        L_0x014f:
            int r6 = r6 + 1
            goto L_0x00de
        L_0x0152:
            return r4
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.CloudMessageProvider.update(android.net.Uri, android.content.ContentValues, java.lang.String, java.lang.String[]):int");
    }

    private boolean needToResetMcsData(int i, String str) {
        if (!CmsUtil.isMcsSupported(getContext(), i)) {
            return false;
        }
        String keyStringValueOfUserDebug = CloudMessageService.getClientByID(i).getPrerenceManager().getKeyStringValueOfUserDebug(DebugFlag.MCS_URL, "");
        String str2 = LOG_TAG;
        IMSLog.i(str2, "newUrl: " + str);
        return !keyStringValueOfUserDebug.equals(str);
    }

    public ParcelFileDescriptor openFile(Uri uri, String str) throws FileNotFoundException {
        int i;
        int i2;
        int match = sUriMatcher.match(uri);
        if (match == 8 || match == 9) {
            Cursor query = query(uri, (String[]) null, (String) null, (String[]) null, (String) null);
            if (query != null) {
                try {
                    if (query.moveToFirst()) {
                        if (match == 8) {
                            i = query.getColumnIndex(CloudMessageProviderContract.BufferDBMMSpart._DATA);
                        } else {
                            i = match == 9 ? query.getColumnIndex(ImContract.CsSession.FILE_PATH) : -1;
                        }
                        String string = i >= 0 ? query.getString(i) : null;
                        if (query.moveToNext()) {
                            throw new FileNotFoundException("Multiple items at " + uri);
                        } else if (string != null) {
                            query.close();
                            File file = new File(string);
                            if (str.contains("w")) {
                                if (!file.exists()) {
                                    try {
                                        file.createNewFile();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                                i2 = 536870912;
                            } else {
                                i2 = 0;
                            }
                            if (str.contains("r")) {
                                i2 |= LogClass.SIM_EVENT;
                            }
                            if (str.contains("+")) {
                                i2 |= 33554432;
                            }
                            return ParcelFileDescriptor.open(file, i2);
                        } else {
                            throw new FileNotFoundException("File path is null");
                        }
                    }
                } catch (Throwable th) {
                    th.addSuppressed(th);
                }
            }
            throw new FileNotFoundException("No entry for " + uri);
        }
        throw new IllegalArgumentException("URI invalid. Use an id-based URI only.");
        throw th;
    }

    private Cursor getMaxSmallFileSize(int i) {
        long maxSmallFileSize = CloudMessageService.getClientByID(i) != null ? ((long) CloudMessageService.getClientByID(i).getPrerenceManager().getMaxSmallFileSize()) * 1024 * 1024 : 0;
        String str = LOG_TAG;
        IMSLog.s(str, "getMaxSmallFileSize slotId: " + i + ", maxSmallFileSize: " + maxSmallFileSize);
        MatrixCursor matrixCursor = new MatrixCursor(new String[]{"max_small_file_size"});
        matrixCursor.newRow().add(Long.valueOf(maxSmallFileSize));
        return matrixCursor;
    }
}
