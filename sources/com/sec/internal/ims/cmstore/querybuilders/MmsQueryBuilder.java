package com.sec.internal.ims.cmstore.querybuilders;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.MIMEContentType;
import com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.adapters.CloudMessageTelephonyStorageAdapter;
import com.sec.internal.ims.cmstore.params.ParamOMAObject;
import com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet;
import com.sec.internal.ims.cmstore.utils.CursorContentValueTranslator;
import com.sec.internal.ims.cmstore.utils.Util;
import com.sec.internal.interfaces.ims.cmstore.IBufferDBEventListener;
import com.sec.internal.interfaces.ims.cmstore.ITelephonyDBColumns;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class MmsQueryBuilder extends QueryBuilderBase {
    private String TAG = MmsQueryBuilder.class.getSimpleName();
    private final CloudMessageTelephonyStorageAdapter mTelephonyStorage;

    public MmsQueryBuilder(MessageStoreClient messageStoreClient, IBufferDBEventListener iBufferDBEventListener) {
        super(messageStoreClient, iBufferDBEventListener);
        this.TAG += "[" + messageStoreClient.getClientID() + "]";
        this.mTelephonyStorage = new CloudMessageTelephonyStorageAdapter(messageStoreClient.getContext());
    }

    public Cursor searchMMSPduBufferUsingMidorTrId(String str, String str2) {
        String str3;
        if (str2 == null || str2.length() <= 2) {
            str2 = "invalid string";
            str3 = str2;
        } else {
            str3 = str2.substring(2);
        }
        String[] strArr = {str, str2, str, str3};
        String str4 = this.TAG;
        Log.d(str4, "searchMMSPduBufferUsingMidorTrId, mid: " + str + " tr_id: " + str2 + " subtrid:" + str3);
        return this.mBufferDB.queryMMSPDUMessages((String[]) null, "m_id=? OR tr_id=? OR correlation_id=? OR correlation_id=?", strArr, (String) null);
    }

    public Cursor searchMMsPduBufferUsingCorrelationId(String str) {
        String str2 = this.TAG;
        Log.d(str2, "searchMMsPduBufferUsingCorrelationId: " + str);
        return this.mBufferDB.queryMMSPDUMessages((String[]) null, "m_id=? OR tr_id GLOB ?", new String[]{str, "*" + str + "*"}, (String) null);
    }

    public Cursor searchMMSPduBufferUsingRowId(long j) {
        String str = this.TAG;
        Log.d(str, "searchMMSPduBufferUsingRowId: " + j);
        return this.mBufferDB.queryMMSPDUMessages((String[]) null, "_id=?", new String[]{String.valueOf(j)}, (String) null);
    }

    public Cursor queryMMSMessagesToUpload() {
        return this.mBufferDB.queryMMSPDUMessages((String[]) null, "syncdirection=? AND (res_url IS NULL OR res_url = '') AND date > ? AND sim_imsi=?", new String[]{String.valueOf(CloudMessageBufferDBConstants.DirectionFlag.ToSendCloud.getId()), String.valueOf(System.currentTimeMillis() - TimeUnit.HOURS.toMillis((long) this.mHoursToUploadMessageInitSync)), this.IMSI}, (String) null);
    }

    public void updateMMSUpdateingDevice(long j) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice.getId()));
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.UpdatePayload.getId()));
        updateTable(4, contentValues, "_bufferdbid=?", new String[]{String.valueOf(j)});
    }

    public long insertToMMSPDUBufferDB(Cursor cursor, ContentValues contentValues, boolean z, boolean z2) {
        int i;
        long j;
        Throwable th;
        Throwable th2;
        ContentValues contentValues2 = contentValues;
        boolean z3 = z2;
        ArrayList<ContentValues> convertPDUtoCV = CursorContentValueTranslator.convertPDUtoCV(cursor);
        if (convertPDUtoCV == null) {
            return 0;
        }
        String userTelCtn = this.mStoreClient.getPrerenceManager().getUserTelCtn();
        Log.d(this.TAG, "insertToPDUBufferDB size: " + convertPDUtoCV.size() + " isImsiUpdateReq " + z3);
        int i2 = 0;
        long j2 = 0;
        int i3 = 0;
        while (i3 < convertPDUtoCV.size()) {
            ContentValues contentValues3 = convertPDUtoCV.get(i3);
            if (contentValues3 != null) {
                Integer asInteger = contentValues3.getAsInteger("read");
                if (asInteger == null) {
                    i = i2;
                } else {
                    i = asInteger.intValue();
                }
                if (!z || i != 1) {
                    contentValues3.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, contentValues2.getAsInteger(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION));
                    contentValues3.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, contentValues2.getAsInteger(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION));
                } else {
                    contentValues3.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud.getId()));
                    contentValues3.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Update.getId()));
                }
                contentValues3.put(CloudMessageProviderContract.BufferDBExtensionBase.LASTMODSEQ, Integer.valueOf(i2));
                contentValues3.put("linenum", userTelCtn);
                if (z3) {
                    contentValues3.put("sim_imsi", this.IMSI);
                }
                j2 = insertDeviceMsgToBuffer(4, contentValues3);
                Integer asInteger2 = contentValues3.getAsInteger("_id");
                if (asInteger2 == null) {
                    j = 0;
                } else {
                    j = asInteger2.longValue();
                }
                Cursor telephonyAddr = this.mTelephonyStorage.getTelephonyAddr(j);
                if (telephonyAddr != null) {
                    try {
                        if (telephonyAddr.moveToFirst()) {
                            Log.d(this.TAG, "insertToAddrBufferDB: " + j2);
                            insertToMMSAddrBufferDB(telephonyAddr, j2);
                        }
                    } catch (Throwable th3) {
                        th2.addSuppressed(th3);
                    }
                }
                if (telephonyAddr != null) {
                    telephonyAddr.close();
                }
                Cursor telephonyPart = this.mTelephonyStorage.getTelephonyPart(j);
                if (telephonyPart != null) {
                    try {
                        if (telephonyPart.moveToFirst()) {
                            Log.d(this.TAG, "insertToPartBufferDB: " + j2);
                            insertToMMSPartBufferDB(telephonyPart, j2);
                        }
                    } catch (Throwable th4) {
                        th.addSuppressed(th4);
                    }
                }
                if (telephonyPart != null) {
                    telephonyPart.close();
                }
            }
            i3++;
            i2 = 0;
        }
        if (convertPDUtoCV.size() == 1) {
            return j2;
        }
        return 0;
        throw th;
        throw th2;
    }

    /* access modifiers changed from: protected */
    public void insertToMMSPartBufferDB(Cursor cursor, long j) {
        String str = this.TAG;
        Log.d(str, "we do get something from telephony MMS Part: " + cursor.getCount() + ", row=" + j);
        ArrayList<ContentValues> convertPARTtoCV = CursorContentValueTranslator.convertPARTtoCV(cursor);
        if (convertPARTtoCV != null) {
            for (int i = 0; i < convertPARTtoCV.size(); i++) {
                ContentValues contentValues = convertPARTtoCV.get(i);
                contentValues.put(CloudMessageProviderContract.BufferDBMMSpart.MID, Long.valueOf(j));
                insertDeviceMsgToBuffer(6, contentValues);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void insertToMMSAddrBufferDB(Cursor cursor, long j) {
        String str = this.TAG;
        Log.d(str, "insertToAddrBufferDB: " + j + "we do get something from telephony MMS Addr: " + cursor.getCount());
        ArrayList<ContentValues> convertADDRtoCV = CursorContentValueTranslator.convertADDRtoCV(cursor);
        if (convertADDRtoCV != null) {
            for (int i = 0; i < convertADDRtoCV.size(); i++) {
                ContentValues contentValues = convertADDRtoCV.get(i);
                contentValues.put("msg_id", Long.valueOf(j));
                insertDeviceMsgToBuffer(5, contentValues);
            }
        }
    }

    public Cursor queryMMSPduFromTelephonyDbUseID(long j) {
        String str = this.TAG;
        Log.d(str, "queryMMSPduFromTelephonyDbUseID: " + j);
        return this.mTelephonyStorage.queryMMSPduFromTelephonyDbUseID(j);
    }

    public Cursor queryAllMMSPduFromTelephonyDbWithIMSI(String str) {
        String[] strArr;
        String str2;
        Log.d(this.TAG, "queryMMSPduFromTelephonyDbWithSlot()");
        if (this.isCmsEnabled) {
            strArr = new String[]{str, String.valueOf((System.currentTimeMillis() - TimeUnit.HOURS.toMillis((long) (this.mHoursToUploadMessageInitSync + 24))) / 1000)};
            str2 = "sim_imsi=? AND date > ?";
        } else {
            strArr = new String[]{str};
            str2 = "sim_imsi=?";
        }
        return this.mTelephonyStorage.queryMMSPduFromTelephonyDb((String[]) null, str2, strArr, (String) null);
    }

    public Cursor queryMMSPduFromTelephonyDbWoIMSI() {
        String str;
        String[] strArr;
        Log.i(this.TAG, "queryMMSPduFromTelephonyDbWoIMSI()");
        if (this.isCmsEnabled) {
            strArr = new String[]{String.valueOf((System.currentTimeMillis() - TimeUnit.HOURS.toMillis((long) (this.mHoursToUploadMessageInitSync + 24))) / 1000)};
            str = "(sim_imsi IS NULL OR sim_imsi = '' ) AND date > ?";
        } else {
            str = "sim_imsi IS NULL OR sim_imsi = ''";
            strArr = null;
        }
        return this.mTelephonyStorage.queryMMSPduFromTelephonyDb((String[]) null, str, strArr, (String) null);
    }

    public Cursor queryDeltaMMSPduFromTelephonyDb() {
        String str;
        String[] strArr;
        int queryMmsPduBufferDBLargestTelephonyId = queryMmsPduBufferDBLargestTelephonyId();
        Log.i(this.TAG, "queryDeltaMMSPduFromTelephonyDb largest MMS _id: " + queryMmsPduBufferDBLargestTelephonyId);
        if (this.isCmsEnabled) {
            str = "_id > ? AND sim_imsi=?" + " AND CREATOR != 'com.samsung.android.messaging'" + " AND " + "date" + " > ?";
            strArr = new String[]{String.valueOf(queryMmsPduBufferDBLargestTelephonyId), this.IMSI, String.valueOf((System.currentTimeMillis() - TimeUnit.HOURS.toMillis((long) (this.mHoursToUploadMessageInitSync + 24))) / 1000)};
        } else {
            strArr = new String[]{String.valueOf(queryMmsPduBufferDBLargestTelephonyId), this.IMSI};
            str = "_id>?  AND sim_imsi=?";
        }
        return this.mTelephonyStorage.queryMMSPduFromTelephonyDb((String[]) null, str, strArr, (String) null);
    }

    public Cursor queryDeltaMMSPduFromTelephonyDbWoImsi() {
        String[] strArr;
        int queryMmsPduBufferDBLargestTelephonyIdWoImsi = queryMmsPduBufferDBLargestTelephonyIdWoImsi();
        Log.i(this.TAG, "queryDeltaMMSPduFromTelephonyDbWoImsi largest MMS _id: " + queryMmsPduBufferDBLargestTelephonyIdWoImsi);
        String str = "_id > ? AND (sim_imsi IS NULL OR sim_imsi = '')";
        if (this.isCmsEnabled) {
            str = str + " AND CREATOR != 'com.samsung.android.messaging'" + " AND " + "date" + " > ?";
            strArr = new String[]{String.valueOf(queryMmsPduBufferDBLargestTelephonyIdWoImsi), String.valueOf((System.currentTimeMillis() - TimeUnit.HOURS.toMillis((long) (this.mHoursToUploadMessageInitSync + 24))) / 1000)};
        } else {
            strArr = new String[]{String.valueOf(queryMmsPduBufferDBLargestTelephonyIdWoImsi)};
        }
        return this.mTelephonyStorage.queryMMSPduFromTelephonyDb((String[]) null, str, strArr, (String) null);
    }

    public Cursor queryReadMmsfromTelephony() {
        return this.mTelephonyStorage.queryMMSPduFromTelephonyDb((String[]) null, "read=? AND sim_imsi=?", new String[]{String.valueOf(1), this.IMSI}, (String) null);
    }

    /* JADX WARNING: Removed duplicated region for block: B:14:0x003e  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int queryMmsPduBufferDBLargestTelephonyId() {
        /*
            r5 = this;
            java.lang.String r0 = r5.TAG
            java.lang.String r1 = "queryMmsPduBufferDBLargestTelephonyId: "
            android.util.Log.d(r0, r1)
            java.lang.String r0 = "MAX(_id)"
            java.lang.String r1 = "_id"
            java.lang.String[] r0 = new java.lang.String[]{r0, r1}
            java.lang.String r2 = r5.IMSI
            java.lang.String[] r2 = new java.lang.String[]{r2}
            com.sec.internal.ims.cmstore.adapters.CloudMessageBufferDBPersister r5 = r5.mBufferDB
            r3 = 0
            java.lang.String r4 = "sim_imsi=?"
            android.database.Cursor r5 = r5.queryMMSPDUMessages(r0, r4, r2, r3)
            if (r5 == 0) goto L_0x003b
            boolean r0 = r5.moveToFirst()     // Catch:{ all -> 0x0031 }
            if (r0 == 0) goto L_0x003b
            int r0 = r5.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x0031 }
            int r0 = r5.getInt(r0)     // Catch:{ all -> 0x0031 }
            goto L_0x003c
        L_0x0031:
            r0 = move-exception
            r5.close()     // Catch:{ all -> 0x0036 }
            goto L_0x003a
        L_0x0036:
            r5 = move-exception
            r0.addSuppressed(r5)
        L_0x003a:
            throw r0
        L_0x003b:
            r0 = 0
        L_0x003c:
            if (r5 == 0) goto L_0x0041
            r5.close()
        L_0x0041:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.querybuilders.MmsQueryBuilder.queryMmsPduBufferDBLargestTelephonyId():int");
    }

    /* JADX WARNING: Removed duplicated region for block: B:14:0x0038  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int queryMmsPduBufferDBLargestTelephonyIdWoImsi() {
        /*
            r4 = this;
            java.lang.String r0 = r4.TAG
            java.lang.String r1 = "queryMmsPduBufferDBLargestTelephonyId: "
            android.util.Log.d(r0, r1)
            java.lang.String r0 = "MAX(_id)"
            java.lang.String r1 = "_id"
            java.lang.String[] r0 = new java.lang.String[]{r0, r1}
            com.sec.internal.ims.cmstore.adapters.CloudMessageBufferDBPersister r4 = r4.mBufferDB
            r2 = 0
            java.lang.String r3 = "sim_imsi IS NULL OR sim_imsi = ''"
            android.database.Cursor r4 = r4.queryMMSPDUMessages(r0, r3, r2, r2)
            if (r4 == 0) goto L_0x0035
            boolean r0 = r4.moveToFirst()     // Catch:{ all -> 0x002b }
            if (r0 == 0) goto L_0x0035
            int r0 = r4.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x002b }
            int r0 = r4.getInt(r0)     // Catch:{ all -> 0x002b }
            goto L_0x0036
        L_0x002b:
            r0 = move-exception
            r4.close()     // Catch:{ all -> 0x0030 }
            goto L_0x0034
        L_0x0030:
            r4 = move-exception
            r0.addSuppressed(r4)
        L_0x0034:
            throw r0
        L_0x0035:
            r0 = 0
        L_0x0036:
            if (r4 == 0) goto L_0x003b
            r4.close()
        L_0x003b:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.querybuilders.MmsQueryBuilder.queryMmsPduBufferDBLargestTelephonyIdWoImsi():int");
    }

    public ParamSyncFlagsSet insertMMSUsingObject(ParamOMAObject paramOMAObject, boolean z, long j, boolean z2) {
        long j2;
        CloudMessageBufferDBConstants.DirectionFlag directionFlag;
        CloudMessageBufferDBConstants.ActionStatusFlag actionStatusFlag;
        ParamOMAObject paramOMAObject2 = paramOMAObject;
        boolean z3 = z;
        ContentValues contentValues = new ContentValues();
        CloudMessageBufferDBConstants.DirectionFlag directionFlag2 = CloudMessageBufferDBConstants.DirectionFlag.Done;
        CloudMessageBufferDBConstants.ActionStatusFlag actionStatusFlag2 = CloudMessageBufferDBConstants.ActionStatusFlag.None;
        ParamSyncFlagsSet paramSyncFlagsSet = new ParamSyncFlagsSet(directionFlag2, actionStatusFlag2);
        paramSyncFlagsSet.mBufferId = -1;
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.CORRELATION_ID, paramOMAObject2.correlationId);
        contentValues.put(CloudMessageProviderContract.BufferDBMMSpdu.M_ID, paramOMAObject2.correlationId);
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.LASTMODSEQ, paramOMAObject2.lastModSeq);
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.RES_URL, Util.decodeUrlFromServer(paramOMAObject2.resourceURL.toString()));
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.PARENTFOLDER, Util.decodeUrlFromServer(paramOMAObject2.parentFolder.toString()));
        contentValues.put("path", Util.decodeUrlFromServer(paramOMAObject2.path));
        contentValues.put("linenum", Util.getLineTelUriFromObjUrl(paramOMAObject2.resourceURL.toString()));
        contentValues.put("date", Long.valueOf(getDateFromDateString(paramOMAObject2.DATE)));
        contentValues.put("_id", Integer.valueOf(this.VALUE_ID_UNFETCHED));
        contentValues.put(CloudMessageProviderContract.BufferDBMMSpdu.CT_T, paramOMAObject2.MULTIPARTCONTENTTYPE);
        if (this.isCmsEnabled) {
            contentValues.put("safe_message", Integer.valueOf(paramOMAObject2.SAFE_MESSAGE));
        }
        if ("IN".equalsIgnoreCase(paramOMAObject2.DIRECTION)) {
            contentValues.put(CloudMessageProviderContract.BufferDBMMSpdu.MSG_BOX, 1);
            contentValues.put(CloudMessageProviderContract.BufferDBMMSpdu.M_TYPE, 132);
        } else {
            contentValues.put(CloudMessageProviderContract.BufferDBMMSpdu.MSG_BOX, 2);
            contentValues.put(CloudMessageProviderContract.BufferDBMMSpdu.M_TYPE, 128);
        }
        contentValues.put(CloudMessageProviderContract.BufferDBMMSpdu.SUB, paramOMAObject2.SUBJECT);
        if (paramOMAObject2.mIsGoforwardSync) {
            contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(actionStatusFlag2.getId()));
            contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(directionFlag2.getId()));
        } else if (paramOMAObject2.mFlag.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Delete)) {
            CloudMessageBufferDBConstants.ActionStatusFlag actionStatusFlag3 = CloudMessageBufferDBConstants.ActionStatusFlag.Deleted;
            contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(actionStatusFlag3.getId()));
            contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(directionFlag2.getId()));
            paramSyncFlagsSet.mAction = actionStatusFlag3;
        } else {
            if (this.isCmsEnabled && paramOMAObject2.payloadPart == null && paramOMAObject2.payloadURL == null) {
                actionStatusFlag = CloudMessageBufferDBConstants.ActionStatusFlag.Insert;
                if (z2) {
                    actionStatusFlag = CloudMessageBufferDBConstants.ActionStatusFlag.FetchForce;
                }
                directionFlag = CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice;
                contentValues.put(CloudMessageProviderContract.BufferDBMMSpdu.TEXT_ONLY, 1);
            } else {
                directionFlag = directionFlag2;
                actionStatusFlag = actionStatusFlag2;
            }
            if (paramOMAObject2.payloadPart != null) {
                if (z2) {
                    actionStatusFlag = CloudMessageBufferDBConstants.ActionStatusFlag.FetchUri;
                    directionFlag = CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice;
                } else if ("OUT".equalsIgnoreCase(paramOMAObject2.DIRECTION) || ("IN".equalsIgnoreCase(paramOMAObject2.DIRECTION) && Util.isDownloadObject(paramOMAObject2.DATE, this.mStoreClient, 4))) {
                    actionStatusFlag = CloudMessageBufferDBConstants.ActionStatusFlag.FetchIndividualUri;
                    directionFlag = CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice;
                } else {
                    directionFlag = directionFlag2;
                    actionStatusFlag = actionStatusFlag2;
                }
            }
            if (paramOMAObject2.payloadURL != null) {
                if (z2 || Util.isDownloadObject(paramOMAObject2.DATE, this.mStoreClient, 4)) {
                    actionStatusFlag2 = CloudMessageBufferDBConstants.ActionStatusFlag.FetchForce;
                    directionFlag2 = CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice;
                }
                contentValues.put(CloudMessageProviderContract.BufferDBMMSpdu.CT_L, paramOMAObject2.payloadURL.toString());
                directionFlag = directionFlag2;
                actionStatusFlag = actionStatusFlag2;
            }
            String str = this.TAG;
            Log.i(str, "SyncAction: " + actionStatusFlag.getId() + " direction: " + directionFlag.getId());
            boolean equals = paramOMAObject2.mFlag.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Update);
            contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(actionStatusFlag.getId()));
            contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(directionFlag.getId()));
            contentValues.put("read", Integer.valueOf(equals ? 1 : 0));
            contentValues.put("seen", Integer.valueOf(equals));
            paramSyncFlagsSet.mAction = actionStatusFlag;
            paramSyncFlagsSet.mDirection = directionFlag;
        }
        contentValues.put(CloudMessageProviderContract.BufferDBMMSpdu.M_CLS, "personal");
        contentValues.put(CloudMessageProviderContract.BufferDBMMSpdu.V, 18);
        contentValues.put("pri", 129);
        contentValues.put(CloudMessageProviderContract.BufferDBMMSpdu.RR, 129);
        contentValues.put(CloudMessageProviderContract.BufferDBMMSpdu.D_RPT, 129);
        contentValues.put(CloudMessageProviderContract.BufferDBMMSpdu.RETR_ST, 128);
        contentValues.put(CloudMessageProviderContract.BufferDBMMSpdu.TR_ID, "D4" + paramOMAObject2.correlationId);
        contentValues.put("sim_imsi", this.IMSI);
        if (z3) {
            j2 = (long) updateTable(4, contentValues, "_bufferdbid=?", new String[]{String.valueOf(j)});
        } else {
            j2 = insertTable(4, contentValues);
        }
        paramSyncFlagsSet.mBufferId = j2;
        String str2 = this.TAG;
        Log.d(str2, "insert MMS: " + j2 + " res url: " + IMSLog.checker(paramOMAObject2.resourceURL.toString()) + " lastmdf: " + paramOMAObject2.lastModSeq + " objt size: " + IMSLog.checker(Integer.valueOf(paramOMAObject2.TO.size())) + " payloadPart: " + IMSLog.checker(paramOMAObject2.payloadPart) + " isUpdate:" + z3);
        contentValues.clear();
        contentValues.put("msg_id", Long.valueOf(j2));
        String str3 = "IN".equalsIgnoreCase(paramOMAObject2.DIRECTION) ? paramOMAObject2.FROM : ITelephonyDBColumns.FROM_INSERT_ADDRESS_TOKEN_STR;
        if (!TextUtils.isEmpty(str3)) {
            if (str3.contains("tel:")) {
                str3 = str3.replace("tel:", "");
            } else if (this.isCmsEnabled && str3.contains("unknown_address")) {
                str3 = "";
            }
        }
        contentValues.put("address", str3);
        contentValues.put("type", 137);
        contentValues.put("charset", 106);
        insertTable(5, contentValues);
        for (int i = 0; i < paramOMAObject2.TO.size(); i++) {
            contentValues.clear();
            contentValues.put("msg_id", Long.valueOf(j2));
            String str4 = paramOMAObject2.TO.get(i);
            if (str4 != null && str4.contains("tel:")) {
                str4 = str4.replace("tel:", "");
            }
            contentValues.put("address", str4);
            contentValues.put("type", 151);
            contentValues.put("charset", 106);
            insertTable(5, contentValues);
        }
        if (paramOMAObject2.payloadPart != null) {
            contentValues.clear();
            for (int i2 = 0; i2 < paramOMAObject2.payloadPart.length; i2++) {
                contentValues.put(CloudMessageProviderContract.BufferDBMMSpart.MID, Long.valueOf(j2));
                contentValues.put(CloudMessageProviderContract.BufferDBMMSpart.CT, paramOMAObject2.payloadPart[i2].contentType.split(";")[0]);
                contentValues.put(CloudMessageProviderContract.BufferDBMMSpart.CID, Util.encodedToIso8859(paramOMAObject2.payloadPart[i2].contentId));
                contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.PAYLOADURL, paramOMAObject2.payloadPart[i2].href.toString());
                contentValues.put(CloudMessageProviderContract.BufferDBMMSpart.CL, Util.generateLocationWithEncoding(paramOMAObject2.payloadPart[i2]));
                insertTable(6, contentValues);
            }
        }
        if (this.isCmsEnabled && paramOMAObject2.payloadURL == null && !TextUtils.isEmpty(paramOMAObject2.TEXT_CONTENT)) {
            contentValues.clear();
            contentValues.put(CloudMessageProviderContract.BufferDBMMSpart.MID, Long.valueOf(j2));
            contentValues.put(CloudMessageProviderContract.BufferDBMMSpart.CT, MIMEContentType.PLAIN_TEXT);
            contentValues.put("text", paramOMAObject2.TEXT_CONTENT);
            insertTable(6, contentValues);
        }
        return paramSyncFlagsSet;
    }

    public void cleanAllBufferDB() {
        if (this.isCmsEnabled) {
            Util.deleteFilesinMmsBufferFolder(this.mStoreClient.getClientID());
            cleanMMSBufferDBUsingIMSIAndTableIndex();
            return;
        }
        Log.i(this.TAG, "cleanAllBufferDB: Cms is disabled");
    }

    public void cleanMMSBufferDBUsingIMSIAndTableIndex() {
        Log.i(this.TAG, "cleanMMSBufferDBUsingIMSIAndTableIndex");
        String[] strArr = {CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID};
        String[] strArr2 = {this.IMSI};
        Cursor queryMMSPDUMessages = this.mBufferDB.queryMMSPDUMessages(strArr, "sim_imsi= ?", strArr2, (String) null);
        if (queryMMSPDUMessages != null) {
            try {
                if (queryMMSPDUMessages.moveToFirst()) {
                    do {
                        long j = queryMMSPDUMessages.getLong(queryMMSPDUMessages.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID));
                        String str = this.TAG;
                        Log.i(str, "delete addr and part entries for bufferDbId: " + j);
                        deleteAddrTable(j);
                        deletePartTable(j);
                    } while (queryMMSPDUMessages.moveToNext());
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (queryMMSPDUMessages != null) {
            queryMMSPDUMessages.close();
        }
        int deleteTable = this.mBufferDB.deleteTable(4, "sim_imsi= ?", strArr2);
        String str2 = this.TAG;
        Log.i(str2, "cleanMMSBufferDBUsingIMSIAndTableIndex isSuccess: " + deleteTable);
        return;
        throw th;
    }

    private void deleteAddrTable(long j) {
        this.mBufferDB.deleteTable(5, "msg_id= ?", new String[]{String.valueOf(j)});
    }

    private void deletePartTable(long j) {
        this.mBufferDB.deleteTable(6, "mid= ?", new String[]{String.valueOf(j)});
    }

    /* JADX WARNING: Removed duplicated region for block: B:14:0x003f  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean queryIfMmsPartsDownloadComplete(long r4) {
        /*
            r3 = this;
            java.lang.String r0 = r3.TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "queryIfMmsPartsDownloadComplete: "
            r1.append(r2)
            r1.append(r4)
            java.lang.String r1 = r1.toString()
            android.util.Log.i(r0, r1)
            java.lang.String r4 = java.lang.String.valueOf(r4)
            java.lang.String[] r4 = new java.lang.String[]{r4}
            com.sec.internal.ims.cmstore.adapters.CloudMessageBufferDBPersister r3 = r3.mBufferDB
            r5 = 0
            java.lang.String r0 = "mid= ? AND (_data IS NULL OR _data = '') AND (text IS NULL OR text = '')"
            android.database.Cursor r3 = r3.queryMMSPARTMessages(r5, r0, r4, r5)
            if (r3 == 0) goto L_0x003c
            boolean r4 = r3.moveToFirst()     // Catch:{ all -> 0x0032 }
            if (r4 == 0) goto L_0x003c
            r4 = 0
            goto L_0x003d
        L_0x0032:
            r4 = move-exception
            r3.close()     // Catch:{ all -> 0x0037 }
            goto L_0x003b
        L_0x0037:
            r3 = move-exception
            r4.addSuppressed(r3)
        L_0x003b:
            throw r4
        L_0x003c:
            r4 = 1
        L_0x003d:
            if (r3 == 0) goto L_0x0042
            r3.close()
        L_0x0042:
            return r4
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.querybuilders.MmsQueryBuilder.queryIfMmsPartsDownloadComplete(long):boolean");
    }

    public Cursor queryUndownloadedPart(long j) {
        return this.mBufferDB.queryMMSPARTMessages((String[]) null, "mid= ? AND (_data IS NULL OR _data = '') AND (text IS NULL OR text = '')", new String[]{String.valueOf(j)}, (String) null);
    }

    public Cursor queryMMSPartRowIdWithoutAppId(long j) {
        return this.mBufferDB.queryMMSPARTMessages((String[]) null, "mid= ? AND (_id IS NULL OR _id = '')", new String[]{String.valueOf(j)}, (String) null);
    }

    public Cursor queryMMSBufferDBwithResUrl(String str) {
        return this.mBufferDB.queryTablewithResUrl(4, str);
    }

    public int deleteMMSBufferDBwithResUrl(String str) {
        return this.mBufferDB.deleteTablewithResUrl(4, str);
    }

    public Cursor queryToDeviceUnDownloadedMms(String str, int i) {
        return this.mBufferDB.queryMMSPDUMessages((String[]) null, "syncaction=? AND linenum=? AND sim_imsi=?", new String[]{String.valueOf(i), str, this.IMSI}, (String) null);
    }

    public Cursor queryToCloudUnsyncedMms() {
        Log.d(this.TAG, "queryToCloudUnsyncedMms: ");
        return this.mBufferDB.queryMMSPDUMessages((String[]) null, "syncdirection=? AND res_url IS NOT NULL AND date > ? AND sim_imsi=?", new String[]{String.valueOf(CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud.getId()), String.valueOf(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(10)), this.IMSI}, (String) null);
    }

    public Cursor queryToDeviceUnsyncedMms() {
        Log.d(this.TAG, "queryToDeviceUnsyncedMms: ");
        return this.mBufferDB.queryMMSPDUMessages((String[]) null, "syncdirection=? AND sim_imsi=?", new String[]{String.valueOf(CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice.getId()), this.IMSI}, (String) null);
    }
}
