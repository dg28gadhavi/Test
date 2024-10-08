package com.sec.internal.ims.cmstore.querybuilders;

import android.content.ContentValues;
import android.database.Cursor;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImContract;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.adapters.CloudMessageTelephonyStorageAdapter;
import com.sec.internal.ims.cmstore.params.ParamAppJsonValue;
import com.sec.internal.ims.cmstore.params.ParamOMAObject;
import com.sec.internal.ims.cmstore.utils.CmsUtil;
import com.sec.internal.ims.cmstore.utils.CursorContentValueTranslator;
import com.sec.internal.ims.cmstore.utils.Util;
import com.sec.internal.interfaces.ims.cmstore.IBufferDBEventListener;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class SmsQueryBuilder extends QueryBuilderBase {
    private String TAG = SmsQueryBuilder.class.getSimpleName();
    private final CloudMessageTelephonyStorageAdapter mTelephonyStorage;

    public SmsQueryBuilder(MessageStoreClient messageStoreClient, IBufferDBEventListener iBufferDBEventListener) {
        super(messageStoreClient, iBufferDBEventListener);
        this.TAG += "[" + messageStoreClient.getClientID() + "]";
        this.mTelephonyStorage = new CloudMessageTelephonyStorageAdapter(messageStoreClient.getContext());
    }

    public ArrayList<Long> insertSMSUsingObject(ParamOMAObject paramOMAObject, boolean z, long j) {
        long j2;
        ParamOMAObject paramOMAObject2 = paramOMAObject;
        boolean z2 = z;
        String str = this.TAG;
        Log.i(str, "insertSMSUsingObject: " + z2 + " bufferId:" + j);
        ArrayList<Long> arrayList = new ArrayList<>();
        int size = paramOMAObject2.TO.size();
        ContentValues contentValues = new ContentValues();
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.CORRELATION_TAG, paramOMAObject2.correlationTag);
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.LASTMODSEQ, paramOMAObject2.lastModSeq);
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.RES_URL, Util.decodeUrlFromServer(paramOMAObject2.resourceURL.toString()));
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.PARENTFOLDER, Util.decodeUrlFromServer(paramOMAObject2.parentFolder.toString()));
        contentValues.put("path", Util.decodeUrlFromServer(paramOMAObject2.path));
        CloudMessageBufferDBConstants.ActionStatusFlag actionStatusFlag = CloudMessageBufferDBConstants.ActionStatusFlag.Insert;
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(actionStatusFlag.getId()));
        CloudMessageBufferDBConstants.DirectionFlag directionFlag = CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice;
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(directionFlag.getId()));
        contentValues.put("linenum", Util.getLineTelUriFromObjUrl(paramOMAObject2.resourceURL.toString()));
        contentValues.put("type", Integer.valueOf("IN".equalsIgnoreCase(paramOMAObject2.DIRECTION) ? 1 : 2));
        String str2 = "IN".equalsIgnoreCase(paramOMAObject2.DIRECTION) ? paramOMAObject2.FROM : paramOMAObject2.TO.size() > 0 ? paramOMAObject2.TO.get(0) : null;
        if (!TextUtils.isEmpty(str2)) {
            if (str2.contains("tel:")) {
                str2 = str2.replace("tel:", "");
            } else if (this.isCmsEnabled && str2.contains("unknown_address")) {
                str2 = "";
            }
        }
        contentValues.put("address", str2);
        contentValues.put("_id", Integer.valueOf(this.VALUE_ID_UNFETCHED));
        contentValues.put("date", Long.valueOf(getDateFromDateString(paramOMAObject2.DATE)));
        contentValues.put("body", paramOMAObject2.TEXT_CONTENT);
        if (this.isCmsEnabled) {
            contentValues.put("safe_message", Integer.valueOf(paramOMAObject2.SAFE_MESSAGE));
        }
        if (paramOMAObject2.mIsGoforwardSync) {
            contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.None.getId()));
            contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.Done.getId()));
        } else if (paramOMAObject2.mFlag.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Delete)) {
            contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Deleted.getId()));
            contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.Done.getId()));
        } else if (paramOMAObject2.mFlag.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Update)) {
            contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(actionStatusFlag.getId()));
            contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(directionFlag.getId()));
            contentValues.put("read", 1);
        } else {
            contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(actionStatusFlag.getId()));
            contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(directionFlag.getId()));
            contentValues.put("read", 0);
        }
        contentValues.put("sim_imsi", this.IMSI);
        if (z2) {
            j2 = (long) updateTable(3, contentValues, "_bufferdbid=?", new String[]{String.valueOf(j)});
        } else {
            long insertTable = insertTable(3, contentValues);
            if (size > 1) {
                return insertHiddenSMS(paramOMAObject, contentValues, insertTable, arrayList);
            }
            j2 = insertTable;
        }
        arrayList.add(Long.valueOf(j2));
        String str3 = this.TAG;
        Log.d(str3, "insert SMS: " + j2 + " body: " + paramOMAObject2.TEXT_CONTENT + " res url: " + IMSLog.checker(paramOMAObject2.resourceURL.toString()) + " lastmdf: " + paramOMAObject2.lastModSeq);
        return arrayList;
    }

    private ArrayList<Long> insertHiddenSMS(ParamOMAObject paramOMAObject, ContentValues contentValues, long j, ArrayList<Long> arrayList) {
        long j2;
        Log.d(this.TAG, "insertHiddenSMS ToSize:" + paramOMAObject.TO.size() + " rowId:" + j);
        int i = "Out".equals(paramOMAObject.DIRECTION) ? 2 : 1;
        int i2 = 0;
        while (i2 < paramOMAObject.TO.size()) {
            contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.CORRELATION_TAG, this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getSmsHashTagOrCorrelationTag(paramOMAObject.TO.get(i2), i, paramOMAObject.TEXT_CONTENT));
            contentValues.put(CloudMessageProviderContract.BufferDBSMS.GROUP_COTAG, paramOMAObject.correlationTag);
            contentValues.put(CloudMessageProviderContract.BufferDBSMS.GROUP_ID, Long.valueOf(j));
            contentValues.put("hidden", Integer.valueOf(i2 == 0 ? 0 : 1));
            String str = paramOMAObject.TO.get(i2);
            if (str != null && str.contains("tel:")) {
                str = str.replace("tel:", "");
            }
            contentValues.put("address", str);
            String[] strArr = {String.valueOf(j)};
            if (i2 == 0) {
                updateTable(3, contentValues, "_bufferdbid=?", strArr);
                j2 = j;
            } else {
                j2 = insertTable(3, contentValues);
            }
            Log.d(this.TAG, "insertHiddenSMS new inserted/updated row:" + j2);
            arrayList.add(Long.valueOf(j2));
            i2++;
        }
        Log.d(this.TAG, "insertHiddenSMS rowIds inserted:" + arrayList.size());
        return arrayList;
    }

    private Cursor insertSmsUsingRcsBufferDbCursor(Cursor cursor) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.CORRELATION_TAG, cursor.getString(cursor.getColumnIndex(CloudMessageProviderContract.BufferDBExtensionBase.CORRELATION_TAG)));
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.LASTMODSEQ, Long.valueOf(cursor.getLong(cursor.getColumnIndex(CloudMessageProviderContract.BufferDBExtensionBase.LASTMODSEQ))));
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.RES_URL, cursor.getString(cursor.getColumnIndex(CloudMessageProviderContract.BufferDBExtensionBase.RES_URL)));
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.PARENTFOLDER, cursor.getString(cursor.getColumnIndex(CloudMessageProviderContract.BufferDBExtensionBase.PARENTFOLDER)));
        contentValues.put("path", cursor.getString(cursor.getColumnIndex("path")));
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(cursor.getInt(cursor.getColumnIndex(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION))));
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(cursor.getInt(cursor.getColumnIndex(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION))));
        contentValues.put("linenum", cursor.getString(cursor.getColumnIndex("linenum")));
        contentValues.put("type", Integer.valueOf(ImDirection.INCOMING.getId() == cursor.getInt(cursor.getColumnIndex("direction")) ? 1 : 2));
        String string = cursor.getString(cursor.getColumnIndex("remote_uri"));
        if (string != null && string.contains("tel:")) {
            string = string.replace("tel:", "");
        }
        contentValues.put("address", string);
        contentValues.put("_id", Integer.valueOf(this.VALUE_ID_UNFETCHED));
        contentValues.put("date", Long.valueOf(cursor.getLong(cursor.getColumnIndex(ImContract.ChatItem.INSERTED_TIMESTAMP))));
        contentValues.put("body", cursor.getString(cursor.getColumnIndex("body")));
        contentValues.put("read", Integer.valueOf(ImConstants.Status.READ.getId() == cursor.getInt(cursor.getColumnIndex("status")) ? 1 : 0));
        contentValues.put("sim_imsi", this.IMSI);
        long insertTable = insertTable(3, contentValues);
        this.mBufferDB.deleteTablewithBufferDbId(1, cursor.getLong(cursor.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID)));
        updateSummaryTableMsgType(cursor.getString(cursor.getColumnIndex(CloudMessageProviderContract.BufferDBExtensionBase.RES_URL)), 3);
        return this.mBufferDB.queryTablewithBufferDbId(3, insertTable);
    }

    public Cursor searchSMSBufferUsingCorrelationTagForEarlierNmsEvent(String str, String str2) {
        String str3 = this.TAG;
        Log.i(str3, "searchSMSBufferUsingCorrelationTagForEarlierNmsEvent: " + str + " line: " + IMSLog.checker(str2));
        Cursor querySMSMessages = this.mBufferDB.querySMSMessages((String[]) null, "correlation_tag=? AND _id=?", new String[]{str, String.valueOf(this.VALUE_ID_UNFETCHED)}, "date DESC LIMIT 1");
        if (!this.mStoreClient.getCloudMessageStrategyManager().getStrategy().requiresInterworkingCrossSearch()) {
            return querySMSMessages;
        }
        if (querySMSMessages == null || !querySMSMessages.moveToFirst()) {
            return handleCrossSearchRcs(querySMSMessages, str, str2);
        }
        return querySMSMessages;
    }

    public Cursor handleCrossSearchRcs(Cursor cursor, String str, String str2) {
        Cursor queryTable = this.mBufferDB.queryTable(1, (String[]) null, "correlation_tag=? AND (_id IS NULL OR _id = '')", new String[]{str}, "inserted_timestamp DESC LIMIT 1");
        if (queryTable != null) {
            try {
                if (queryTable.moveToFirst()) {
                    Cursor insertSmsUsingRcsBufferDbCursor = insertSmsUsingRcsBufferDbCursor(queryTable);
                    queryTable.close();
                    return insertSmsUsingRcsBufferDbCursor;
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (queryTable != null) {
            queryTable.close();
        }
        return cursor;
        throw th;
    }

    public Cursor searchSMSBufferUsingRowId(long j) {
        return this.mBufferDB.querySMSMessages((String[]) null, "_id=?", new String[]{String.valueOf(j)}, (String) null);
    }

    public Cursor querySMSMessagesToUpload() {
        Log.d(this.TAG, "querySMSMessagesToUpload()");
        String str = CmsUtil.isMcsSupported(this.mContext, this.mStoreClient.getClientID()) ? " AND ((group_id !=0 AND hidden =0) OR group_id =0 OR group_id IS NULL)" : "";
        return this.mBufferDB.querySMSMessages((String[]) null, "syncdirection=?" + " AND (type = 2 OR type = 1)" + " AND " + "sim_imsi" + "=? AND (" + CloudMessageProviderContract.BufferDBExtensionBase.RES_URL + " IS NULL OR " + CloudMessageProviderContract.BufferDBExtensionBase.RES_URL + " = '') AND " + "date" + " > ?" + str, new String[]{String.valueOf(CloudMessageBufferDBConstants.DirectionFlag.ToSendCloud.getId()), this.IMSI, String.valueOf(System.currentTimeMillis() - TimeUnit.HOURS.toMillis((long) this.mHoursToUploadMessageInitSync))}, (String) null);
    }

    public Cursor querySMSUseRowId(long j) {
        return this.mTelephonyStorage.querySMSUseRowId(j);
    }

    public Cursor queryAllSMSfromTelephony() {
        return this.mTelephonyStorage.querySMSfromTelephony((String[]) null, "type=? OR type=?", new String[]{String.valueOf(1), String.valueOf(2)}, (String) null);
    }

    public Cursor querySMSfromTelephonyWithIMSI(String str) {
        String[] strArr;
        String str2;
        Log.d(this.TAG, "querySMSfromTelephonyWithIMSI: ");
        if (this.isCmsEnabled) {
            strArr = new String[]{str, String.valueOf(System.currentTimeMillis() - TimeUnit.HOURS.toMillis((long) (this.mHoursToUploadMessageInitSync + 24)))};
            str2 = "sim_imsi=? AND date > ?";
        } else {
            strArr = new String[]{str};
            str2 = "sim_imsi=?";
        }
        return this.mTelephonyStorage.querySMSfromTelephony((String[]) null, str2, strArr, (String) null);
    }

    public Cursor querySMSfromTelephonyWoIMSI() {
        String str;
        String[] strArr;
        Log.i(this.TAG, "querySMSfromTelephonyWoIMSI()");
        if (this.isCmsEnabled) {
            strArr = new String[]{String.valueOf(System.currentTimeMillis() - TimeUnit.HOURS.toMillis((long) (this.mHoursToUploadMessageInitSync + 24)))};
            str = "(sim_imsi IS NULL OR sim_imsi = '' ) AND date > ?";
        } else {
            str = "sim_imsi IS NULL OR sim_imsi = ''";
            strArr = null;
        }
        return this.mTelephonyStorage.querySMSfromTelephony((String[]) null, str, strArr, (String) null);
    }

    public Cursor queryDeltaSMSfromTelephony() {
        String[] strArr;
        int querySmsBufferDBLargestTelephonyId = querySmsBufferDBLargestTelephonyId();
        Log.i(this.TAG, "queryDeltaSMSfromTelephony largest SMS _id: " + querySmsBufferDBLargestTelephonyId);
        String str = "_id > ? AND sim_imsi=?";
        if (this.isCmsEnabled) {
            str = str + " AND CREATOR != 'com.samsung.android.messaging'" + " AND " + "date" + " > ?";
            strArr = new String[]{String.valueOf(querySmsBufferDBLargestTelephonyId), this.IMSI, String.valueOf(System.currentTimeMillis() - TimeUnit.HOURS.toMillis((long) (this.mHoursToUploadMessageInitSync + 24)))};
        } else {
            strArr = new String[]{String.valueOf(querySmsBufferDBLargestTelephonyId), this.IMSI};
        }
        return this.mTelephonyStorage.querySMSfromTelephony((String[]) null, str, strArr, (String) null);
    }

    public Cursor queryDeltaSMSfromTelephonyWoImsi() {
        String[] strArr;
        int querySmsBufferDBLargestTelephonyIdWoImsi = querySmsBufferDBLargestTelephonyIdWoImsi();
        Log.i(this.TAG, "queryDeltaSMSfromTelephonyWoImsi largest SMS _id: " + querySmsBufferDBLargestTelephonyIdWoImsi);
        String str = "_id > ? AND (sim_imsi IS NULL OR sim_imsi = '')";
        if (this.isCmsEnabled) {
            str = str + " AND CREATOR != 'com.samsung.android.messaging'" + " AND " + "date" + " > ?";
            strArr = new String[]{String.valueOf(querySmsBufferDBLargestTelephonyIdWoImsi), String.valueOf(System.currentTimeMillis() - TimeUnit.HOURS.toMillis((long) (this.mHoursToUploadMessageInitSync + 24)))};
        } else {
            strArr = new String[]{String.valueOf(querySmsBufferDBLargestTelephonyIdWoImsi)};
        }
        return this.mTelephonyStorage.querySMSfromTelephony((String[]) null, str, strArr, (String) null);
    }

    public Cursor queryReadSMSfromTelephony() {
        return this.mTelephonyStorage.querySMSfromTelephony(new String[]{"_id"}, "read=? AND sim_imsi=?", new String[]{String.valueOf(1), this.IMSI}, (String) null);
    }

    public long insertToSMSBufferDB(Cursor cursor, ContentValues contentValues, boolean z, boolean z2) {
        int i;
        ContentValues contentValues2 = contentValues;
        boolean z3 = z;
        boolean z4 = z2;
        int i2 = 0;
        ArrayList<ContentValues> convertSMStoCV = CursorContentValueTranslator.convertSMStoCV(cursor, false);
        String userTelCtn = this.mStoreClient.getPrerenceManager().getUserTelCtn();
        String str = this.TAG;
        Log.d(str, "insertToSMSBufferDB size: " + convertSMStoCV.size() + " isGoForwardSync: " + z3 + " isImsiUpdateReq " + z4);
        convertSMStoCV.sort(Comparator.comparingInt(new SmsQueryBuilder$$ExternalSyntheticLambda0()).thenComparingInt(new SmsQueryBuilder$$ExternalSyntheticLambda1()));
        HashMap hashMap = new HashMap();
        int i3 = 0;
        long j = 0;
        while (i3 < convertSMStoCV.size()) {
            ContentValues contentValues3 = convertSMStoCV.get(i3);
            Integer asInteger = contentValues3.getAsInteger("type");
            if (asInteger != null) {
                String asString = contentValues3.getAsString("address");
                if (!this.mStoreClient.getCloudMessageStrategyManager().getStrategy().shouldSkipCmasSMS(asString)) {
                    contentValues3.put(CloudMessageProviderContract.BufferDBExtensionBase.CORRELATION_TAG, this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getSmsHashTagOrCorrelationTag(asString, asInteger.intValue(), contentValues3.getAsString("body")));
                    Integer asInteger2 = contentValues3.getAsInteger("read");
                    if (asInteger2 == null) {
                        i = i2;
                    } else {
                        i = asInteger2.intValue();
                    }
                    if (!z3 || i != 1) {
                        contentValues3.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, contentValues2.getAsInteger(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION));
                        contentValues3.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, contentValues2.getAsInteger(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION));
                    } else {
                        contentValues3.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud.getId()));
                        contentValues3.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Update.getId()));
                    }
                    contentValues3.put(CloudMessageProviderContract.BufferDBExtensionBase.LASTMODSEQ, Integer.valueOf(i2));
                    contentValues3.put("linenum", userTelCtn);
                    if (z4) {
                        contentValues3.put("sim_imsi", this.IMSI);
                    }
                    long intValue = contentValues3.getAsInteger(CloudMessageProviderContract.BufferDBSMS.GROUP_ID) != null ? (long) contentValues3.getAsInteger(CloudMessageProviderContract.BufferDBSMS.GROUP_ID).intValue() : 0;
                    int intValue2 = contentValues3.getAsInteger("hidden") != null ? contentValues3.getAsInteger("hidden").intValue() : i2;
                    if (this.isCmsEnabled && intValue > 0 && intValue2 == 1 && hashMap.containsKey(Long.valueOf(intValue))) {
                        long longValue = ((Long) hashMap.get(Long.valueOf(intValue))).longValue();
                        String str2 = this.TAG;
                        IMSLog.d(str2, "group sms hidden row update group id " + longValue);
                        contentValues3.put(CloudMessageProviderContract.BufferDBSMS.GROUP_ID, Long.valueOf(longValue));
                    }
                    long insertDeviceMsgToBuffer = insertDeviceMsgToBuffer(3, contentValues3);
                    if (this.isCmsEnabled && intValue > 0 && intValue2 == 0) {
                        String str3 = this.TAG;
                        IMSLog.d(str3, " map appGroupID " + intValue + " buffer group Id " + insertDeviceMsgToBuffer);
                        hashMap.put(Long.valueOf(intValue), Long.valueOf(insertDeviceMsgToBuffer));
                        updateGroupId(insertDeviceMsgToBuffer);
                    }
                    j = insertDeviceMsgToBuffer;
                }
            }
            i3++;
            contentValues2 = contentValues;
            z3 = z;
            i2 = 0;
        }
        if (convertSMStoCV.size() == 1) {
            return j;
        }
        return 0;
    }

    /* access modifiers changed from: private */
    public static /* synthetic */ int lambda$insertToSMSBufferDB$0(ContentValues contentValues) {
        if (contentValues.getAsInteger(CloudMessageProviderContract.BufferDBSMS.GROUP_ID) != null) {
            return contentValues.getAsInteger(CloudMessageProviderContract.BufferDBSMS.GROUP_ID).intValue();
        }
        return 0;
    }

    /* access modifiers changed from: private */
    public static /* synthetic */ int lambda$insertToSMSBufferDB$1(ContentValues contentValues) {
        if (contentValues.getAsInteger("hidden") != null) {
            return contentValues.getAsInteger("hidden").intValue();
        }
        return 0;
    }

    public void updateGroupId(long j) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(CloudMessageProviderContract.BufferDBSMS.GROUP_ID, Long.valueOf(j));
        updateTable(3, contentValues, "_bufferdbid=?", new String[]{String.valueOf(j)});
    }

    private void getGroupSMSCV(ContentValues contentValues, String str, String str2) {
        contentValues.put("linenum", this.mStoreClient.getPrerenceManager().getUserTelCtn());
        String smsHashTagOrCorrelationTag = this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getSmsHashTagOrCorrelationTag(str, 2, str2);
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.CORRELATION_TAG, smsHashTagOrCorrelationTag);
        String formatNumberToE164 = PhoneNumberUtils.formatNumberToE164(str, Util.getSimCountryCode(this.mContext, this.mStoreClient.getClientID()));
        if (!TextUtils.isEmpty(formatNumberToE164)) {
            str = formatNumberToE164;
        }
        String str3 = this.TAG;
        IMSLog.i(str3, "correlationTag: " + smsHashTagOrCorrelationTag + " address:" + IMSLog.numberChecker(str));
        contentValues.put("address", str);
        contentValues.put("body", str2);
        contentValues.put("type", 2);
    }

    public long handleGroupSMSUpload(ParamAppJsonValue paramAppJsonValue) {
        Throwable th;
        SmsQueryBuilder smsQueryBuilder = this;
        ParamAppJsonValue paramAppJsonValue2 = paramAppJsonValue;
        Cursor querySMSUseRowId = smsQueryBuilder.querySMSUseRowId(Long.valueOf((long) paramAppJsonValue2.mRowId).longValue());
        long j = -1;
        if (querySMSUseRowId != null) {
            try {
                if (querySMSUseRowId.moveToFirst()) {
                    int i = 1;
                    ArrayList<ContentValues> convertSMStoCV = CursorContentValueTranslator.convertSMStoCV(querySMSUseRowId, true);
                    int i2 = 0;
                    int i3 = 0;
                    while (i3 < convertSMStoCV.size()) {
                        ContentValues contentValues = new ContentValues(convertSMStoCV.get(i3));
                        String[] split = paramAppJsonValue2.mToAddress.split("\\|");
                        if (split.length < i) {
                            IMSLog.e(smsQueryBuilder.TAG, "unexpected To Address size");
                            querySMSUseRowId.close();
                            return 0;
                        }
                        smsQueryBuilder.getGroupSMSCV(contentValues, split[i2], paramAppJsonValue2.mBody);
                        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud.getId()));
                        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Update.getId()));
                        contentValues.put("hidden", Integer.valueOf(i2));
                        contentValues.put("_id", Integer.valueOf(paramAppJsonValue2.mRowId));
                        long insertDeviceMsgToBuffer = smsQueryBuilder.insertDeviceMsgToBuffer(3, contentValues);
                        contentValues.put(CloudMessageProviderContract.BufferDBSMS.GROUP_ID, Long.valueOf(insertDeviceMsgToBuffer));
                        String[] strArr = new String[i];
                        strArr[i2] = String.valueOf(insertDeviceMsgToBuffer);
                        smsQueryBuilder.updateTable(3, contentValues, "_bufferdbid=?", strArr);
                        int i4 = i;
                        while (i4 < split.length) {
                            contentValues.clear();
                            contentValues = new ContentValues(convertSMStoCV.get(i3));
                            smsQueryBuilder.getGroupSMSCV(contentValues, split[i4], paramAppJsonValue2.mBody);
                            contentValues.put(CloudMessageProviderContract.BufferDBSMS.GROUP_ID, Long.valueOf(insertDeviceMsgToBuffer));
                            contentValues.put("hidden", Integer.valueOf(i));
                            contentValues.put("_id", Integer.valueOf(smsQueryBuilder.VALUE_ID_UNFETCHED));
                            ArrayList<ContentValues> arrayList = convertSMStoCV;
                            long insertDeviceMsgToBuffer2 = smsQueryBuilder.insertDeviceMsgToBuffer(3, contentValues);
                            IMSLog.i(smsQueryBuilder.TAG, "handleGroupSMSUpload insertedRowid:" + insertDeviceMsgToBuffer2 + " address:" + IMSLog.numberChecker(split[i4]) + " rowId: " + insertDeviceMsgToBuffer);
                            i4++;
                            i = 1;
                            smsQueryBuilder = this;
                            convertSMStoCV = arrayList;
                        }
                        ArrayList<ContentValues> arrayList2 = convertSMStoCV;
                        i3++;
                        i = 1;
                        i2 = 0;
                        smsQueryBuilder = this;
                        j = insertDeviceMsgToBuffer;
                    }
                }
            } catch (Throwable th2) {
                th.addSuppressed(th2);
            }
        }
        if (querySMSUseRowId != null) {
            querySMSUseRowId.close();
        }
        return j;
        throw th;
    }

    public Cursor querySMSBufferDBwithResUrl(String str) {
        return this.mBufferDB.queryTablewithResUrl(3, str);
    }

    public int deleteSMSBufferDBwithResUrl(String str) {
        return this.mBufferDB.deleteTablewithResUrl(3, str);
    }

    public Cursor queryToCloudUnsyncedSms() {
        return this.mBufferDB.querySMSMessages((String[]) null, "syncdirection=? AND res_url IS NOT NULL AND date > ? AND sim_imsi=?", new String[]{String.valueOf(CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud.getId()), String.valueOf(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(10)), this.IMSI}, (String) null);
    }

    public Cursor queryToDeviceUnsyncedSms() {
        return this.mBufferDB.querySMSMessages((String[]) null, "syncdirection=? AND sim_imsi=?", new String[]{String.valueOf(CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice.getId()), this.IMSI}, (String) null);
    }

    /* JADX WARNING: Removed duplicated region for block: B:14:0x0036  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int querySmsBufferDBLargestTelephonyId() {
        /*
            r5 = this;
            java.lang.String r0 = "MAX(_id)"
            java.lang.String r1 = "_id"
            java.lang.String[] r0 = new java.lang.String[]{r0, r1}
            java.lang.String r2 = r5.IMSI
            java.lang.String[] r2 = new java.lang.String[]{r2}
            com.sec.internal.ims.cmstore.adapters.CloudMessageBufferDBPersister r5 = r5.mBufferDB
            r3 = 0
            java.lang.String r4 = "sim_imsi=?"
            android.database.Cursor r5 = r5.querySMSMessages(r0, r4, r2, r3)
            if (r5 == 0) goto L_0x0033
            boolean r0 = r5.moveToFirst()     // Catch:{ all -> 0x0029 }
            if (r0 == 0) goto L_0x0033
            int r0 = r5.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x0029 }
            int r0 = r5.getInt(r0)     // Catch:{ all -> 0x0029 }
            goto L_0x0034
        L_0x0029:
            r0 = move-exception
            r5.close()     // Catch:{ all -> 0x002e }
            goto L_0x0032
        L_0x002e:
            r5 = move-exception
            r0.addSuppressed(r5)
        L_0x0032:
            throw r0
        L_0x0033:
            r0 = 0
        L_0x0034:
            if (r5 == 0) goto L_0x0039
            r5.close()
        L_0x0039:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.querybuilders.SmsQueryBuilder.querySmsBufferDBLargestTelephonyId():int");
    }

    /* JADX WARNING: Removed duplicated region for block: B:14:0x0030  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int querySmsBufferDBLargestTelephonyIdWoImsi() {
        /*
            r4 = this;
            java.lang.String r0 = "MAX(_id)"
            java.lang.String r1 = "_id"
            java.lang.String[] r0 = new java.lang.String[]{r0, r1}
            com.sec.internal.ims.cmstore.adapters.CloudMessageBufferDBPersister r4 = r4.mBufferDB
            r2 = 0
            java.lang.String r3 = "sim_imsi IS NULL OR sim_imsi = ''"
            android.database.Cursor r4 = r4.querySMSMessages(r0, r3, r2, r2)
            if (r4 == 0) goto L_0x002d
            boolean r0 = r4.moveToFirst()     // Catch:{ all -> 0x0023 }
            if (r0 == 0) goto L_0x002d
            int r0 = r4.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x0023 }
            int r0 = r4.getInt(r0)     // Catch:{ all -> 0x0023 }
            goto L_0x002e
        L_0x0023:
            r0 = move-exception
            r4.close()     // Catch:{ all -> 0x0028 }
            goto L_0x002c
        L_0x0028:
            r4 = move-exception
            r0.addSuppressed(r4)
        L_0x002c:
            throw r0
        L_0x002d:
            r0 = 0
        L_0x002e:
            if (r4 == 0) goto L_0x0033
            r4.close()
        L_0x0033:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.querybuilders.SmsQueryBuilder.querySmsBufferDBLargestTelephonyIdWoImsi():int");
    }

    public Cursor searchUnSyncedSMSBufferUsingCorrelationTag(String str) {
        if (str == null) {
            return null;
        }
        return this.mBufferDB.querySMSMessages((String[]) null, "( group_cotag=? OR correlation_tag=? ) AND (res_url IS NULL OR res_url = '')", new String[]{str, str}, "date DESC LIMIT 1");
    }

    public Cursor querySMSwithGroupId(long j) {
        String str = this.TAG;
        IMSLog.i(str, "querySMSwithGroupId " + j);
        return this.mBufferDB.querySMSMessages((String[]) null, "group_id= ?", new String[]{String.valueOf(j)}, (String) null);
    }

    public Cursor queryNonHiddenSMSwithGroupId(long j) {
        String str = this.TAG;
        IMSLog.i(str, "queryNonHiddenSMSwithGroupId " + j);
        return this.mBufferDB.querySMSMessages((String[]) null, "group_id= ? AND hidden=?", new String[]{String.valueOf(j), String.valueOf(0)}, (String) null);
    }
}
