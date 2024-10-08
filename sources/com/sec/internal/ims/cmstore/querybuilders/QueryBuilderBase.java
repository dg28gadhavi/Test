package com.sec.internal.ims.cmstore.querybuilders;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.cmstore.data.FlagNames;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.helper.httpclient.HttpPostBody;
import com.sec.internal.helper.translate.FileExtensionTranslator;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.adapters.CloudMessageBufferDBPersister;
import com.sec.internal.ims.cmstore.params.ParamVvmUpdate;
import com.sec.internal.ims.cmstore.utils.CmsUtil;
import com.sec.internal.ims.cmstore.utils.Util;
import com.sec.internal.interfaces.ims.cmstore.IBufferDBEventListener;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.nms.data.FlagList;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class QueryBuilderBase {
    protected String IMSI;
    private String TAG = QueryBuilderBase.class.getSimpleName();
    protected int VALUE_ID_UNFETCHED;
    protected boolean isCmsEnabled = false;
    protected final CloudMessageBufferDBPersister mBufferDB;
    protected final IBufferDBEventListener mCallbackMsgApp;
    protected final Context mContext;
    protected final int mHoursToSendCloudUnsyncMessage = 10;
    protected int mHoursToUploadMessageInitSync = 2184;
    protected MessageStoreClient mStoreClient;
    SimpleDateFormat[] sFormatOfName;

    public QueryBuilderBase(MessageStoreClient messageStoreClient, IBufferDBEventListener iBufferDBEventListener) {
        SimpleDateFormat[] simpleDateFormatArr = {new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault()), new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()), new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ", Locale.getDefault()), new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault()), new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.getDefault())};
        this.sFormatOfName = simpleDateFormatArr;
        this.VALUE_ID_UNFETCHED = 0;
        for (SimpleDateFormat timeZone : simpleDateFormatArr) {
            timeZone.setTimeZone(TimeZone.getTimeZone("UTC"));
        }
        this.mStoreClient = messageStoreClient;
        this.TAG += "[" + messageStoreClient.getClientID() + "]";
        this.IMSI = this.mStoreClient.getCurrentIMSI();
        Context context = messageStoreClient.getContext();
        this.mContext = context;
        this.mBufferDB = CloudMessageBufferDBPersister.getInstance(context, this.mStoreClient.getClientID(), false);
        this.mCallbackMsgApp = iBufferDBEventListener;
        this.isCmsEnabled = CmsUtil.isMcsSupported(context, this.mStoreClient.getClientID());
        onUpdateCmsConfigInitSyncDataTtl();
    }

    public void resetImsi() {
        this.IMSI = this.mStoreClient.getCurrentIMSI();
        String str = this.TAG;
        Log.i(str, "resetImsi new val: " + IMSLog.checker(this.IMSI));
    }

    public int updateTable(int i, ContentValues contentValues, String str, String[] strArr) {
        String str2 = this.TAG;
        Log.d(str2, "updateTable: " + i);
        if (contentValues.size() < 1) {
            return 0;
        }
        return this.mBufferDB.updateTable(i, contentValues, str, strArr);
    }

    public void cleanAllBufferDBUsingIMSIAndTableIndex() {
        String[] strArr = {this.IMSI};
        this.mBufferDB.deleteTable(3, "sim_imsi=?", strArr);
        this.mBufferDB.deleteTable(10, "sim_imsi=?", strArr);
        this.mBufferDB.deleteTable(1, "sim_imsi=?", strArr);
        this.mBufferDB.deleteTable(2, "sim_imsi=?", strArr);
        this.mBufferDB.deleteTable(13, "sim_imsi=?", strArr);
        this.mBufferDB.deleteTable(7, "sim_imsi=?", strArr);
        if (this.mBufferDB.deleteTable(23, "sim_imsi=?", strArr) == 0) {
            int deleteTable = this.mBufferDB.deleteTable(23, "linenum=?", new String[]{this.mStoreClient.getPrerenceManager().getUserTelCtn()});
            String str = this.TAG;
            Log.i(str, "cleanAllBufferDBUsingIMSIAndTableIndex rowsDeleted via linenum: " + deleteTable);
        }
    }

    public void deleteAllUsingLineAndTableIndex(int i, String str) {
        int deleteTable = this.mBufferDB.deleteTable(i, "linenum=?", new String[]{str});
        String str2 = this.TAG;
        Log.d(str2, "deleteAllUsingLineAndTableIndex isSuccess: " + deleteTable);
    }

    public long getDateFromDateString(String str) {
        SimpleDateFormat[] simpleDateFormatArr = this.sFormatOfName;
        int length = simpleDateFormatArr.length;
        int i = 0;
        while (i < length) {
            try {
                Date parse = simpleDateFormatArr[i].parse(str);
                if (parse != null) {
                    return parse.getTime();
                }
                return System.currentTimeMillis();
            } catch (ParseException e) {
                String str2 = this.TAG;
                Log.e(str2, "ParseException: " + e.getMessage());
                i++;
            }
        }
        return System.currentTimeMillis();
    }

    public Cursor queryTablewithBufferDbId(int i, long j) {
        String str = this.TAG;
        Log.d(str, "queryTablewithBufferDbId, table: " + i + " key: " + j);
        return this.mBufferDB.queryTablewithBufferDbId(i, j);
    }

    public long insertTable(int i, ContentValues contentValues) {
        return this.mBufferDB.insertTable(i, contentValues);
    }

    public long insertDeviceMsgToBuffer(int i, ContentValues contentValues) {
        long insertDeviceMsgToBuffer = this.mBufferDB.insertDeviceMsgToBuffer(i, contentValues);
        String str = this.TAG;
        Log.d(str, "insertDeviceMsgToBuffer, tableindex: " + i + " rowid: " + insertDeviceMsgToBuffer);
        return insertDeviceMsgToBuffer;
    }

    public void cleanAllBufferDB() {
        if (this.isCmsEnabled) {
            cleanAllBufferDBUsingIMSIAndTableIndex();
            return;
        }
        Util.deleteFilesinMmsBufferFolder(this.mStoreClient.getClientID());
        this.mBufferDB.cleanAllBufferDB();
    }

    public CloudMessageBufferDBConstants.ActionStatusFlag getCloudActionPerFlag(FlagList flagList) {
        CloudMessageBufferDBConstants.ActionStatusFlag actionStatusFlag;
        CloudMessageBufferDBConstants.ActionStatusFlag actionStatusFlag2 = CloudMessageBufferDBConstants.ActionStatusFlag.Insert;
        if (flagList != null && flagList.flag != null) {
            int i = 0;
            while (true) {
                String[] strArr = flagList.flag;
                if (i >= strArr.length) {
                    break;
                }
                if (strArr[i].equalsIgnoreCase(FlagNames.Seen)) {
                    actionStatusFlag = CloudMessageBufferDBConstants.ActionStatusFlag.Update;
                    if (actionStatusFlag.getId() <= actionStatusFlag2.getId()) {
                        i++;
                    }
                } else if (flagList.flag[i].equalsIgnoreCase(FlagNames.Deleted)) {
                    actionStatusFlag = CloudMessageBufferDBConstants.ActionStatusFlag.Delete;
                    if (actionStatusFlag.getId() <= actionStatusFlag2.getId()) {
                        i++;
                    }
                } else {
                    if (flagList.flag[i].equalsIgnoreCase(FlagNames.Canceled)) {
                        actionStatusFlag = CloudMessageBufferDBConstants.ActionStatusFlag.Cancel;
                        if (actionStatusFlag.getId() <= actionStatusFlag2.getId()) {
                        }
                    }
                    i++;
                }
                actionStatusFlag2 = actionStatusFlag;
                i++;
            }
        }
        return actionStatusFlag2;
    }

    public int updateMessageStatus(FlagList flagList, String str) {
        ImConstants.Status status;
        IMSLog.i(this.TAG, "updateMessageStatus");
        if (flagList != null && flagList.flag != null) {
            int i = 0;
            boolean z = false;
            boolean z2 = false;
            while (true) {
                String[] strArr = flagList.flag;
                if (i >= strArr.length) {
                    break;
                }
                if (strArr[i].equalsIgnoreCase(FlagNames.Canceled)) {
                    z = true;
                } else if (flagList.flag[i].equalsIgnoreCase(FlagNames.Seen)) {
                    z2 = true;
                }
                i++;
            }
            if (z && z2) {
                return ImConstants.Status.CANCELLATION.getId();
            }
            if ("OUT".equalsIgnoreCase(str)) {
                return (z ? ImConstants.Status.CANCELLATION : ImConstants.Status.SENT).getId();
            } else if (z) {
                return ImConstants.Status.CANCELLATION_UNREAD.getId();
            } else {
                if (z2) {
                    status = ImConstants.Status.READ;
                } else {
                    status = ImConstants.Status.UNREAD;
                }
                return status.getId();
            }
        } else if ("OUT".equalsIgnoreCase(str)) {
            return ImConstants.Status.SENT.getId();
        } else {
            return ImConstants.Status.UNREAD.getId();
        }
    }

    public void notifyApplication(String str, String str2, long j) {
        JsonArray jsonArray = new JsonArray();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", String.valueOf(j));
        jsonArray.add(jsonObject);
        this.mCallbackMsgApp.notifyCloudMessageUpdate(str, str2, jsonArray.toString(), false);
    }

    public int getIfSeenValueUsingFlag(FlagList flagList) {
        if (flagList != null && flagList.flag != null) {
            int i = 0;
            while (true) {
                String[] strArr = flagList.flag;
                if (i >= strArr.length) {
                    break;
                } else if (strArr[i].equalsIgnoreCase(FlagNames.Seen)) {
                    return 1;
                } else {
                    i++;
                }
            }
        }
        return 0;
    }

    public int getIfisGreetingOnUsingFlag(FlagList flagList) {
        if (flagList == null || flagList.flag == null) {
            return ParamVvmUpdate.GreetingOnFlag.GreetingOff.getId();
        }
        int i = 0;
        while (true) {
            String[] strArr = flagList.flag;
            if (i >= strArr.length) {
                return ParamVvmUpdate.GreetingOnFlag.GreetingOff.getId();
            }
            if (FlagNames.Cns_Greeting_on.equalsIgnoreCase(strArr[i])) {
                return ParamVvmUpdate.GreetingOnFlag.GreetingOn.getId();
            }
            i++;
        }
    }

    public boolean getIfCancelUsingFlag(FlagList flagList) {
        if (flagList != null && flagList.flag != null) {
            int i = 0;
            while (true) {
                String[] strArr = flagList.flag;
                if (i >= strArr.length) {
                    break;
                } else if (strArr[i].equalsIgnoreCase(FlagNames.Canceled)) {
                    return true;
                } else {
                    i++;
                }
            }
        }
        return false;
    }

    public CloudMessageBufferDBConstants.PayloadEncoding translatePayloadEncoding(String str) {
        String str2 = this.TAG;
        Log.d(str2, "translatePayloadEncoding: " + str);
        if (HttpPostBody.CONTENT_TRANSFER_ENCODING_BASE64.equalsIgnoreCase(str)) {
            return CloudMessageBufferDBConstants.PayloadEncoding.Base64;
        }
        return CloudMessageBufferDBConstants.PayloadEncoding.None;
    }

    public String getFileExtension(String str) {
        String str2 = this.TAG;
        Log.d(str2, "getFileExtension: " + str);
        return (TextUtils.isEmpty(str) || !FileExtensionTranslator.isTranslationDefined(str)) ? "" : FileExtensionTranslator.translate(str);
    }

    public boolean isContentTypeDefined(String str) {
        String str2 = this.TAG;
        Log.d(str2, "isContentTypeDefined: " + str);
        if (TextUtils.isEmpty(str)) {
            return false;
        }
        if (FileExtensionTranslator.isTranslationDefined(str) || str.toLowerCase().contains("multipart/related")) {
            return true;
        }
        return false;
    }

    public void insertResUrlinSummary(String str, int i) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("messagetype", Integer.valueOf(i));
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.RES_URL, Util.decodeUrlFromServer(str));
        contentValues.put("linenum", Util.getLineTelUriFromObjUrl(str));
        contentValues.put("sim_imsi", this.IMSI);
        this.mBufferDB.insertTable(7, contentValues);
    }

    /* access modifiers changed from: protected */
    public void updateSummaryTableMsgType(String str, int i) {
        String str2 = this.TAG;
        Log.i(str2, "updateSummaryTableMsgType: " + IMSLog.checker(str) + " msgType: " + i);
        String extractObjIdFromResUrl = Util.extractObjIdFromResUrl(str);
        String lineTelUriFromObjUrl = Util.getLineTelUriFromObjUrl(str);
        String[] strArr = {"*" + extractObjIdFromResUrl, lineTelUriFromObjUrl};
        ContentValues contentValues = new ContentValues();
        contentValues.put("messagetype", Integer.valueOf(i));
        this.mBufferDB.updateTable(7, contentValues, "res_url GLOB ? AND linenum=?", strArr);
    }

    public void updateAppFetchingFailed(int i, long j) {
        String str = this.TAG;
        Log.i(str, "updateAppFetchingFailed: " + i + " bufferId: " + j);
        ContentValues contentValues = new ContentValues();
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.FetchingFail.getId()));
        this.mBufferDB.updateTable(i, contentValues, "_bufferdbid=?", new String[]{Long.toString(j)});
    }

    public void setMsgDeleted(int i, long j) {
        String str = this.TAG;
        Log.i(str, "setMsgDeleted: " + i + " bufferId: " + j);
        ContentValues contentValues = new ContentValues();
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.Done.getId()));
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Deleted.getId()));
        this.mBufferDB.updateTable(i, contentValues, "_bufferdbid=?", new String[]{Long.toString(j)});
    }

    public Cursor queryMessageBySyncDirection(int i, String str) {
        String str2 = this.TAG;
        Log.i(str2, "queryMessageBySyncDirection: " + i + " syncDirection: " + str);
        return this.mBufferDB.queryTable(i, (String[]) null, "sim_imsi=? AND syncdirection=?", new String[]{this.IMSI, String.valueOf(str)}, (String) null);
    }

    public Cursor queryMessageBySyncAction(int i, int i2) {
        String str = this.TAG;
        Log.i(str, "queryMessageBySyncAction: " + i + " syncAction: " + i2);
        return this.mBufferDB.queryTable(i, (String[]) null, "sim_imsi=? AND syncaction=?", new String[]{this.IMSI, String.valueOf(i2)}, (String) null);
    }

    public void onUpdateCmsConfigInitSyncDataTtl() {
        if (this.isCmsEnabled) {
            this.mHoursToUploadMessageInitSync = this.mStoreClient.getPrerenceManager().getCmsDataTtl() / 3600;
            String str = this.TAG;
            Log.i(str, "ttl value: " + this.mHoursToUploadMessageInitSync);
        }
    }
}
