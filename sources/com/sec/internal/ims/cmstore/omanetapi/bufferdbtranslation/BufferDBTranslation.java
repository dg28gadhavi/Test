package com.sec.internal.ims.cmstore.omanetapi.bufferdbtranslation;

import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType;
import com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType;
import com.sec.internal.constants.ims.servicemodules.im.ImContract;
import com.sec.internal.helper.httpclient.HttpPostBody;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.params.ParamObjectUpload;
import com.sec.internal.ims.cmstore.params.ParamVvmUpdate;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.common.data.FileUploadResponse;
import com.sec.internal.omanetapi.file.FileData;
import com.sec.internal.omanetapi.nms.data.Object;
import java.util.ArrayList;
import java.util.List;

public class BufferDBTranslation extends BufferDBSupportTranslation {
    private String LOG_TAG = BufferDBTranslation.class.getSimpleName();

    public Pair<Object, HttpPostBody> getGroupSMSForSteadyUpload(BufferDBChangeParam bufferDBChangeParam) {
        return null;
    }

    public String getImdnResUrl(long j) {
        return null;
    }

    public FileData getLocalFileData(BufferDBChangeParam bufferDBChangeParam) {
        return null;
    }

    public Pair<Object, HttpPostBody> getRCSFtPairFromCursor(BufferDBChangeParam bufferDBChangeParam) {
        return null;
    }

    public ParamObjectUpload getThumbnailPart(BufferDBChangeParam bufferDBChangeParam) {
        return null;
    }

    public boolean isMessageStatusCancelled(long j) {
        return false;
    }

    public boolean needToSkipDownloadLargeFileAndUpdateDB(long j, int i, int i2, String str, boolean z) {
        return false;
    }

    public BufferDBTranslation(MessageStoreClient messageStoreClient, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        super(messageStoreClient, iCloudMessageManagerHelper);
        this.LOG_TAG += "[" + messageStoreClient.getClientID() + "]";
    }

    public void resetDateFormat() {
        this.sFormatOfName = this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getDateFormat();
        String str = this.LOG_TAG;
        StringBuilder sb = new StringBuilder();
        sb.append("resetDateFormat sFormatOfName is null: ");
        sb.append(this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getDateFormat() == null);
        Log.i(str, sb.toString());
    }

    public String getSearchCursorByLine(String str, SyncMsgType syncMsgType) {
        Uri uri;
        String str2 = this.LOG_TAG;
        Log.d(str2, "getSearchCursorByLine: line " + IMSLog.checker(str) + " type: " + syncMsgType);
        if (this.mStoreClient.getClientID() == 1) {
            uri = Uri.parse(BufferQueryDBTranslation.CONTENT_URI_BUFFERDB + "/" + CloudMessageProviderContract.CONTENTPRDR_MULTILINESTATUS + "/slot2/" + str);
        } else {
            uri = Uri.parse(BufferQueryDBTranslation.CONTENT_URI_BUFFERDB + "/" + CloudMessageProviderContract.CONTENTPRDR_MULTILINESTATUS + "/" + str);
        }
        Cursor query = this.mResolver.query(uri, (String[]) null, (String) null, (String[]) null, (String) null);
        if (query != null) {
            try {
                if (query.moveToFirst()) {
                    do {
                        String string = query.getString(query.getColumnIndex(CloudMessageProviderContract.BufferDBExtensionBase.INITSYNCCURSOR));
                        if (syncMsgType.equals(SyncMsgType.valueOf(query.getInt(query.getColumnIndex("messagetype"))))) {
                            this.mStoreClient.getPrerenceManager().saveObjectSearchCursor(string);
                            query.close();
                            return string;
                        }
                    } while (query.moveToNext());
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (query == null) {
            return "";
        }
        query.close();
        return "";
        throw th;
    }

    public OMASyncEventType getInitialSyncStatusByLine(String str, SyncMsgType syncMsgType, String str2) {
        Uri uri;
        String str3 = this.LOG_TAG;
        Log.d(str3, "getInitialSyncStatusByLine: line " + IMSLog.checker(str) + " type: " + syncMsgType + " column:" + str2);
        if (this.mStoreClient.getClientID() == 1) {
            uri = Uri.parse(BufferQueryDBTranslation.CONTENT_URI_BUFFERDB + "/" + CloudMessageProviderContract.CONTENTPRDR_MULTILINESTATUS + "/slot2/" + str);
        } else {
            uri = Uri.parse(BufferQueryDBTranslation.CONTENT_URI_BUFFERDB + "/" + CloudMessageProviderContract.CONTENTPRDR_MULTILINESTATUS + "/" + str);
        }
        Cursor query = this.mResolver.query(uri, (String[]) null, (String) null, (String[]) null, (String) null);
        if (query != null) {
            try {
                if (query.moveToFirst()) {
                    int i = query.getInt(query.getColumnIndex(str2));
                    if (syncMsgType.equals(SyncMsgType.valueOf(query.getInt(query.getColumnIndex("messagetype")))) && str2.equals(CloudMessageProviderContract.BufferDBExtensionBase.INITSYNCSTATUS)) {
                        this.mStoreClient.getPrerenceManager().saveInitialSyncStatus(i);
                    }
                    OMASyncEventType valueOf = OMASyncEventType.valueOf(i);
                    query.close();
                    return valueOf;
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (query != null) {
            query.close();
        }
        return OMASyncEventType.DEFAULT;
        throw th;
    }

    public Pair<String, String> getObjectIdFlagNamePairFromBufDb(BufferDBChangeParam bufferDBChangeParam) {
        return getFlagNamePairFromBufDb(bufferDBChangeParam, false);
    }

    public Pair<String, String> getResourceUrlFlagNamePairFromBufDb(BufferDBChangeParam bufferDBChangeParam) {
        return getFlagNamePairFromBufDb(bufferDBChangeParam, true);
    }

    /* JADX WARNING: Removed duplicated region for block: B:49:0x00fc  */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x0101  */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x0107  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public android.util.Pair<java.lang.String, java.lang.String> getFlagNamePairFromBufDb(com.sec.internal.ims.cmstore.params.BufferDBChangeParam r10, boolean r11) {
        /*
            r9 = this;
            java.lang.String r0 = r9.LOG_TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "message type: "
            r1.append(r2)
            int r2 = r10.mDBIndex
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            android.util.Log.i(r0, r1)
            int r0 = r10.mDBIndex
            r1 = 3
            r2 = 17
            r3 = 1
            if (r0 != r1) goto L_0x0027
            long r0 = r10.mRowId
            android.database.Cursor r0 = r9.querySMSBufferDB(r0)
            goto L_0x004f
        L_0x0027:
            r1 = 4
            if (r0 != r1) goto L_0x0031
            long r0 = r10.mRowId
            android.database.Cursor r0 = r9.querymmsPduBufferDB(r0)
            goto L_0x004f
        L_0x0031:
            if (r0 != r3) goto L_0x003a
            long r0 = r10.mRowId
            android.database.Cursor r0 = r9.queryRCSMessageDBUsingRowId(r0)
            goto L_0x004f
        L_0x003a:
            if (r0 != r2) goto L_0x0043
            long r0 = r10.mRowId
            android.database.Cursor r0 = r9.queryVvmDataBufferDB(r0)
            goto L_0x004f
        L_0x0043:
            r1 = 18
            if (r0 != r1) goto L_0x004e
            long r0 = r10.mRowId
            android.database.Cursor r0 = r9.queryVvmGreetingBufferDB(r0)
            goto L_0x004f
        L_0x004e:
            r0 = 0
        L_0x004f:
            java.lang.String r1 = ""
            if (r0 == 0) goto L_0x00f8
            boolean r4 = r0.moveToFirst()     // Catch:{ all -> 0x00ee }
            if (r4 == 0) goto L_0x00f8
            java.lang.String r4 = "res_url"
            int r4 = r0.getColumnIndex(r4)     // Catch:{ all -> 0x00ee }
            java.lang.String r4 = r0.getString(r4)     // Catch:{ all -> 0x00ee }
            java.lang.String r5 = "syncaction"
            int r5 = r0.getColumnIndex(r5)     // Catch:{ all -> 0x00ee }
            int r5 = r0.getInt(r5)     // Catch:{ all -> 0x00ee }
            java.lang.String r6 = r9.LOG_TAG     // Catch:{ all -> 0x00ee }
            java.lang.StringBuilder r7 = new java.lang.StringBuilder     // Catch:{ all -> 0x00ee }
            r7.<init>()     // Catch:{ all -> 0x00ee }
            java.lang.String r8 = "resUrl : "
            r7.append(r8)     // Catch:{ all -> 0x00ee }
            java.lang.String r8 = com.sec.internal.log.IMSLog.checker(r4)     // Catch:{ all -> 0x00ee }
            r7.append(r8)     // Catch:{ all -> 0x00ee }
            java.lang.String r8 = " action: "
            r7.append(r8)     // Catch:{ all -> 0x00ee }
            r7.append(r5)     // Catch:{ all -> 0x00ee }
            java.lang.String r7 = r7.toString()     // Catch:{ all -> 0x00ee }
            android.util.Log.i(r6, r7)     // Catch:{ all -> 0x00ee }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r6 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Update     // Catch:{ all -> 0x00ee }
            int r6 = r6.getId()     // Catch:{ all -> 0x00ee }
            if (r5 != r6) goto L_0x00b0
            int r10 = r10.mDBIndex     // Catch:{ all -> 0x00ee }
            java.lang.String r5 = "\\Seen"
            if (r10 != r2) goto L_0x00c7
            java.lang.String r10 = "flagRead"
            int r10 = r0.getColumnIndex(r10)     // Catch:{ all -> 0x00ee }
            int r10 = r0.getInt(r10)     // Catch:{ all -> 0x00ee }
            if (r10 != 0) goto L_0x00c7
            java.lang.String r10 = "\\Flagged"
            r5 = r10
            goto L_0x00c7
        L_0x00b0:
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r10 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Delete     // Catch:{ all -> 0x00ee }
            int r10 = r10.getId()     // Catch:{ all -> 0x00ee }
            if (r5 != r10) goto L_0x00bb
            java.lang.String r5 = "\\Deleted"
            goto L_0x00c7
        L_0x00bb:
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r10 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Cancel     // Catch:{ all -> 0x00ee }
            int r10 = r10.getId()     // Catch:{ all -> 0x00ee }
            if (r5 != r10) goto L_0x00c6
            java.lang.String r5 = "\\Canceled"
            goto L_0x00c7
        L_0x00c6:
            r5 = r1
        L_0x00c7:
            java.lang.String r9 = r9.LOG_TAG     // Catch:{ all -> 0x00ee }
            java.lang.StringBuilder r10 = new java.lang.StringBuilder     // Catch:{ all -> 0x00ee }
            r10.<init>()     // Catch:{ all -> 0x00ee }
            java.lang.String r2 = "FlagNames: "
            r10.append(r2)     // Catch:{ all -> 0x00ee }
            r10.append(r5)     // Catch:{ all -> 0x00ee }
            java.lang.String r10 = r10.toString()     // Catch:{ all -> 0x00ee }
            android.util.Log.i(r9, r10)     // Catch:{ all -> 0x00ee }
            if (r4 != 0) goto L_0x00e0
            r4 = r1
        L_0x00e0:
            if (r11 != 0) goto L_0x00fa
            r9 = 47
            int r9 = r4.lastIndexOf(r9)     // Catch:{ all -> 0x00ee }
            int r9 = r9 + r3
            java.lang.String r1 = r4.substring(r9)     // Catch:{ all -> 0x00ee }
            goto L_0x00fa
        L_0x00ee:
            r9 = move-exception
            r0.close()     // Catch:{ all -> 0x00f3 }
            goto L_0x00f7
        L_0x00f3:
            r10 = move-exception
            r9.addSuppressed(r10)
        L_0x00f7:
            throw r9
        L_0x00f8:
            r4 = r1
            r5 = r4
        L_0x00fa:
            if (r0 == 0) goto L_0x00ff
            r0.close()
        L_0x00ff:
            if (r11 == 0) goto L_0x0107
            android.util.Pair r9 = new android.util.Pair
            r9.<init>(r4, r5)
            return r9
        L_0x0107:
            android.util.Pair r9 = new android.util.Pair
            r9.<init>(r1, r5)
            return r9
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.omanetapi.bufferdbtranslation.BufferDBTranslation.getFlagNamePairFromBufDb(com.sec.internal.ims.cmstore.params.BufferDBChangeParam, boolean):android.util.Pair");
    }

    public String getSummaryObjectIdFromBufDb(BufferDBChangeParam bufferDBChangeParam) {
        Cursor querySummaryDB = querySummaryDB(bufferDBChangeParam.mRowId);
        if (querySummaryDB != null) {
            try {
                if (querySummaryDB.moveToFirst()) {
                    String string = querySummaryDB.getString(querySummaryDB.getColumnIndex(CloudMessageProviderContract.BufferDBExtensionBase.RES_URL));
                    if (string == null) {
                        querySummaryDB.close();
                        return "";
                    }
                    String substring = string.substring(string.lastIndexOf(47) + 1);
                    querySummaryDB.close();
                    return substring;
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (querySummaryDB != null) {
            querySummaryDB.close();
        }
        return "";
        throw th;
    }

    public String getSmsObjectIdFromBufDb(BufferDBChangeParam bufferDBChangeParam) {
        Cursor querySMSBufferDB = querySMSBufferDB(bufferDBChangeParam.mRowId);
        if (querySMSBufferDB != null) {
            try {
                if (querySMSBufferDB.moveToFirst()) {
                    String string = querySMSBufferDB.getString(querySMSBufferDB.getColumnIndex(CloudMessageProviderContract.BufferDBExtensionBase.RES_URL));
                    if (string == null) {
                        querySMSBufferDB.close();
                        return "";
                    }
                    String substring = string.substring(string.lastIndexOf(47) + 1);
                    querySMSBufferDB.close();
                    return substring;
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (querySMSBufferDB != null) {
            querySMSBufferDB.close();
        }
        return "";
        throw th;
    }

    public String getVVMObjectIdFromBufDb(BufferDBChangeParam bufferDBChangeParam) {
        Cursor queryVvmDataBufferDB = queryVvmDataBufferDB(bufferDBChangeParam.mRowId);
        if (queryVvmDataBufferDB != null) {
            try {
                if (queryVvmDataBufferDB.moveToFirst()) {
                    String string = queryVvmDataBufferDB.getString(queryVvmDataBufferDB.getColumnIndex(CloudMessageProviderContract.BufferDBExtensionBase.RES_URL));
                    if (string == null) {
                        queryVvmDataBufferDB.close();
                        return "";
                    }
                    String substring = string.substring(string.lastIndexOf(47) + 1);
                    queryVvmDataBufferDB.close();
                    return substring;
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (queryVvmDataBufferDB != null) {
            queryVvmDataBufferDB.close();
        }
        return "";
        throw th;
    }

    /* JADX WARNING: Removed duplicated region for block: B:18:0x002d  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public java.lang.String getVVMpayLoadUrlFromBufDb(com.sec.internal.ims.cmstore.params.BufferDBChangeParam r3) {
        /*
            r2 = this;
            long r0 = r3.mRowId
            android.database.Cursor r2 = r2.queryVvmDataBufferDB(r0)
            if (r2 == 0) goto L_0x002a
            boolean r3 = r2.moveToFirst()     // Catch:{ all -> 0x0020 }
            if (r3 == 0) goto L_0x002a
            java.lang.String r3 = "payloadurl"
            int r3 = r2.getColumnIndex(r3)     // Catch:{ all -> 0x0020 }
            java.lang.String r3 = r2.getString(r3)     // Catch:{ all -> 0x0020 }
            if (r3 != 0) goto L_0x002b
            java.lang.String r3 = ""
            r2.close()
            return r3
        L_0x0020:
            r3 = move-exception
            r2.close()     // Catch:{ all -> 0x0025 }
            goto L_0x0029
        L_0x0025:
            r2 = move-exception
            r3.addSuppressed(r2)
        L_0x0029:
            throw r3
        L_0x002a:
            r3 = 0
        L_0x002b:
            if (r2 == 0) goto L_0x0030
            r2.close()
        L_0x0030:
            return r3
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.omanetapi.bufferdbtranslation.BufferDBTranslation.getVVMpayLoadUrlFromBufDb(com.sec.internal.ims.cmstore.params.BufferDBChangeParam):java.lang.String");
    }

    /* JADX WARNING: Removed duplicated region for block: B:18:0x002d  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public java.lang.String getVVMGreetingpayLoadUrlFromBufDb(com.sec.internal.ims.cmstore.params.BufferDBChangeParam r3) {
        /*
            r2 = this;
            long r0 = r3.mRowId
            android.database.Cursor r2 = r2.queryVvmGreetingBufferDB(r0)
            if (r2 == 0) goto L_0x002a
            boolean r3 = r2.moveToFirst()     // Catch:{ all -> 0x0020 }
            if (r3 == 0) goto L_0x002a
            java.lang.String r3 = "payloadurl"
            int r3 = r2.getColumnIndex(r3)     // Catch:{ all -> 0x0020 }
            java.lang.String r3 = r2.getString(r3)     // Catch:{ all -> 0x0020 }
            if (r3 != 0) goto L_0x002b
            java.lang.String r3 = ""
            r2.close()
            return r3
        L_0x0020:
            r3 = move-exception
            r2.close()     // Catch:{ all -> 0x0025 }
            goto L_0x0029
        L_0x0025:
            r2 = move-exception
            r3.addSuppressed(r2)
        L_0x0029:
            throw r3
        L_0x002a:
            r3 = 0
        L_0x002b:
            if (r2 == 0) goto L_0x0030
            r2.close()
        L_0x0030:
            return r3
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.omanetapi.bufferdbtranslation.BufferDBTranslation.getVVMGreetingpayLoadUrlFromBufDb(com.sec.internal.ims.cmstore.params.BufferDBChangeParam):java.lang.String");
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v1, resolved type: com.sec.internal.ims.cmstore.params.ParamVvmUpdate$VvmTypeChange} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v26, resolved type: java.lang.String[]} */
    /* JADX WARNING: type inference failed for: r0v0 */
    /* JADX WARNING: type inference failed for: r0v2 */
    /* JADX WARNING: type inference failed for: r0v7 */
    /* JADX WARNING: type inference failed for: r0v8 */
    /* JADX WARNING: type inference failed for: r0v9 */
    /* JADX WARNING: type inference failed for: r0v10 */
    /* JADX WARNING: type inference failed for: r0v30 */
    /* JADX WARNING: type inference failed for: r0v31 */
    /* JADX WARNING: type inference failed for: r0v32 */
    /* JADX WARNING: type inference failed for: r0v33 */
    /* JADX WARNING: Failed to insert additional move for type inference */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public com.sec.internal.ims.cmstore.params.ParamVvmUpdate.VvmTypeChange getVVMServiceProfileFromBufDb(com.sec.internal.ims.cmstore.params.BufferDBChangeParam r10, com.sec.internal.ims.cmstore.omanetapi.tmoappapi.data.VvmServiceProfile r11) {
        /*
            r9 = this;
            r0 = 0
            if (r10 == 0) goto L_0x01c8
            if (r11 != 0) goto L_0x0007
            goto L_0x01c8
        L_0x0007:
            int r1 = r10.mDBIndex
            long r2 = r10.mRowId
            r10 = 17
            java.lang.String r4 = "v2t_language"
            r5 = 0
            r6 = 1
            if (r1 == r10) goto L_0x0177
            r10 = 19
            if (r1 == r10) goto L_0x012b
            r10 = 20
            if (r1 == r10) goto L_0x001e
            goto L_0x01c8
        L_0x001e:
            android.database.Cursor r10 = r9.queryVvmProfileBufferDB(r2)
            if (r10 == 0) goto L_0x0124
            boolean r1 = r10.moveToFirst()     // Catch:{ all -> 0x011a }
            if (r1 == 0) goto L_0x0124
            java.lang.String r1 = "profile_changetype"
            int r1 = r10.getColumnIndex(r1)     // Catch:{ all -> 0x011a }
            int r1 = r10.getInt(r1)     // Catch:{ all -> 0x011a }
            com.sec.internal.ims.cmstore.omanetapi.nms.data.AttributeTranslator r2 = new com.sec.internal.ims.cmstore.omanetapi.nms.data.AttributeTranslator     // Catch:{ all -> 0x011a }
            com.sec.internal.ims.cmstore.MessageStoreClient r9 = r9.mStoreClient     // Catch:{ all -> 0x011a }
            r2.<init>(r9)     // Catch:{ all -> 0x011a }
            com.sec.internal.ims.cmstore.params.ParamVvmUpdate$VvmTypeChange r9 = com.sec.internal.ims.cmstore.params.ParamVvmUpdate.VvmTypeChange.VOICEMAILTOTEXT     // Catch:{ all -> 0x011a }
            int r3 = r9.getId()     // Catch:{ all -> 0x011a }
            if (r3 != r1) goto L_0x0074
            java.lang.String r1 = "email_addr1"
            int r1 = r10.getColumnIndex(r1)     // Catch:{ all -> 0x011a }
            java.lang.String r1 = r10.getString(r1)     // Catch:{ all -> 0x011a }
            java.lang.String r3 = "email_addr2"
            int r3 = r10.getColumnIndex(r3)     // Catch:{ all -> 0x011a }
            java.lang.String r3 = r10.getString(r3)     // Catch:{ all -> 0x011a }
            if (r1 == 0) goto L_0x0061
            if (r3 == 0) goto L_0x0061
            java.lang.String[] r0 = new java.lang.String[]{r1, r3}     // Catch:{ all -> 0x011a }
            goto L_0x006e
        L_0x0061:
            if (r1 == 0) goto L_0x0068
            java.lang.String[] r0 = new java.lang.String[]{r1}     // Catch:{ all -> 0x011a }
            goto L_0x006e
        L_0x0068:
            if (r3 == 0) goto L_0x006e
            java.lang.String[] r0 = new java.lang.String[]{r3}     // Catch:{ all -> 0x011a }
        L_0x006e:
            if (r0 == 0) goto L_0x0086
            r2.setEmailAddress(r0)     // Catch:{ all -> 0x011a }
            goto L_0x0086
        L_0x0074:
            com.sec.internal.ims.cmstore.params.ParamVvmUpdate$VvmTypeChange r9 = com.sec.internal.ims.cmstore.params.ParamVvmUpdate.VvmTypeChange.ACTIVATE     // Catch:{ all -> 0x011a }
            int r3 = r9.getId()     // Catch:{ all -> 0x011a }
            java.lang.String r7 = "true"
            if (r3 != r1) goto L_0x0089
            java.lang.String[] r0 = new java.lang.String[r6]     // Catch:{ all -> 0x011a }
            r0[r5] = r7     // Catch:{ all -> 0x011a }
            r2.setVVMOn(r0)     // Catch:{ all -> 0x011a }
        L_0x0086:
            r0 = r9
            goto L_0x0113
        L_0x0089:
            com.sec.internal.ims.cmstore.params.ParamVvmUpdate$VvmTypeChange r9 = com.sec.internal.ims.cmstore.params.ParamVvmUpdate.VvmTypeChange.DEACTIVATE     // Catch:{ all -> 0x011a }
            int r3 = r9.getId()     // Catch:{ all -> 0x011a }
            java.lang.String r8 = "false"
            if (r3 != r1) goto L_0x009b
            java.lang.String[] r0 = new java.lang.String[r6]     // Catch:{ all -> 0x011a }
            r0[r5] = r8     // Catch:{ all -> 0x011a }
            r2.setVVMOn(r0)     // Catch:{ all -> 0x011a }
            goto L_0x0086
        L_0x009b:
            com.sec.internal.ims.cmstore.params.ParamVvmUpdate$VvmTypeChange r9 = com.sec.internal.ims.cmstore.params.ParamVvmUpdate.VvmTypeChange.FULLPROFILE     // Catch:{ all -> 0x011a }
            int r3 = r9.getId()     // Catch:{ all -> 0x011a }
            if (r3 != r1) goto L_0x00a4
            goto L_0x0086
        L_0x00a4:
            com.sec.internal.ims.cmstore.params.ParamVvmUpdate$VvmTypeChange r9 = com.sec.internal.ims.cmstore.params.ParamVvmUpdate.VvmTypeChange.NUTOFF     // Catch:{ all -> 0x011a }
            int r3 = r9.getId()     // Catch:{ all -> 0x011a }
            if (r3 != r1) goto L_0x00b4
            java.lang.String[] r0 = new java.lang.String[r6]     // Catch:{ all -> 0x011a }
            r0[r5] = r8     // Catch:{ all -> 0x011a }
            r2.setNUT(r0)     // Catch:{ all -> 0x011a }
            goto L_0x0086
        L_0x00b4:
            com.sec.internal.ims.cmstore.params.ParamVvmUpdate$VvmTypeChange r9 = com.sec.internal.ims.cmstore.params.ParamVvmUpdate.VvmTypeChange.NUTON     // Catch:{ all -> 0x011a }
            int r3 = r9.getId()     // Catch:{ all -> 0x011a }
            if (r3 != r1) goto L_0x00c4
            java.lang.String[] r0 = new java.lang.String[r6]     // Catch:{ all -> 0x011a }
            r0[r5] = r7     // Catch:{ all -> 0x011a }
            r2.setNUT(r0)     // Catch:{ all -> 0x011a }
            goto L_0x0086
        L_0x00c4:
            com.sec.internal.ims.cmstore.params.ParamVvmUpdate$VvmTypeChange r9 = com.sec.internal.ims.cmstore.params.ParamVvmUpdate.VvmTypeChange.V2TLANGUAGE     // Catch:{ all -> 0x011a }
            int r3 = r9.getId()     // Catch:{ all -> 0x011a }
            if (r3 != r1) goto L_0x00dc
            int r0 = r10.getColumnIndex(r4)     // Catch:{ all -> 0x011a }
            java.lang.String r0 = r10.getString(r0)     // Catch:{ all -> 0x011a }
            java.lang.String[] r0 = new java.lang.String[]{r0}     // Catch:{ all -> 0x011a }
            r2.setV2tLanguage(r0)     // Catch:{ all -> 0x011a }
            goto L_0x0086
        L_0x00dc:
            com.sec.internal.ims.cmstore.params.ParamVvmUpdate$VvmTypeChange r9 = com.sec.internal.ims.cmstore.params.ParamVvmUpdate.VvmTypeChange.V2T_SMS     // Catch:{ all -> 0x011a }
            int r3 = r9.getId()     // Catch:{ all -> 0x011a }
            if (r3 != r1) goto L_0x00f7
            java.lang.String r0 = "v2t_sms"
            int r0 = r10.getColumnIndex(r0)     // Catch:{ all -> 0x011a }
            java.lang.String r0 = r10.getString(r0)     // Catch:{ all -> 0x011a }
            java.lang.String[] r0 = new java.lang.String[]{r0}     // Catch:{ all -> 0x011a }
            r2.setV2tSMS(r0)     // Catch:{ all -> 0x011a }
            goto L_0x0086
        L_0x00f7:
            com.sec.internal.ims.cmstore.params.ParamVvmUpdate$VvmTypeChange r9 = com.sec.internal.ims.cmstore.params.ParamVvmUpdate.VvmTypeChange.V2T_EMAIL     // Catch:{ all -> 0x011a }
            int r3 = r9.getId()     // Catch:{ all -> 0x011a }
            if (r3 != r1) goto L_0x0113
            java.lang.String r0 = "v2t_email"
            int r0 = r10.getColumnIndex(r0)     // Catch:{ all -> 0x011a }
            java.lang.String r0 = r10.getString(r0)     // Catch:{ all -> 0x011a }
            java.lang.String[] r0 = new java.lang.String[]{r0}     // Catch:{ all -> 0x011a }
            r2.setV2tEmail(r0)     // Catch:{ all -> 0x011a }
            goto L_0x0086
        L_0x0113:
            com.sec.internal.omanetapi.nms.data.AttributeList r9 = r2.getAttributeList()     // Catch:{ all -> 0x011a }
            r11.attributes = r9     // Catch:{ all -> 0x011a }
            goto L_0x0124
        L_0x011a:
            r9 = move-exception
            r10.close()     // Catch:{ all -> 0x011f }
            goto L_0x0123
        L_0x011f:
            r10 = move-exception
            r9.addSuppressed(r10)
        L_0x0123:
            throw r9
        L_0x0124:
            if (r10 == 0) goto L_0x01c8
            r10.close()
            goto L_0x01c8
        L_0x012b:
            android.database.Cursor r10 = r9.queryVvmPinBufferDB(r2)
            if (r10 == 0) goto L_0x0171
            boolean r1 = r10.moveToFirst()     // Catch:{ all -> 0x0167 }
            if (r1 == 0) goto L_0x0171
            java.lang.String r1 = "oldpwd"
            int r1 = r10.getColumnIndex(r1)     // Catch:{ all -> 0x0167 }
            java.lang.String r1 = r10.getString(r1)     // Catch:{ all -> 0x0167 }
            java.lang.String r2 = "newpwd"
            int r2 = r10.getColumnIndex(r2)     // Catch:{ all -> 0x0167 }
            java.lang.String r2 = r10.getString(r2)     // Catch:{ all -> 0x0167 }
            com.sec.internal.ims.cmstore.omanetapi.nms.data.AttributeTranslator r3 = new com.sec.internal.ims.cmstore.omanetapi.nms.data.AttributeTranslator     // Catch:{ all -> 0x0167 }
            com.sec.internal.ims.cmstore.MessageStoreClient r9 = r9.mStoreClient     // Catch:{ all -> 0x0167 }
            r3.<init>(r9)     // Catch:{ all -> 0x0167 }
            java.lang.String[] r9 = new java.lang.String[]{r1}     // Catch:{ all -> 0x0167 }
            r3.setOldPwd(r9)     // Catch:{ all -> 0x0167 }
            java.lang.String[] r9 = new java.lang.String[]{r2}     // Catch:{ all -> 0x0167 }
            r3.setPwd(r9)     // Catch:{ all -> 0x0167 }
            com.sec.internal.omanetapi.nms.data.AttributeList r9 = r3.getAttributeList()     // Catch:{ all -> 0x0167 }
            r11.attributes = r9     // Catch:{ all -> 0x0167 }
            goto L_0x0171
        L_0x0167:
            r9 = move-exception
            r10.close()     // Catch:{ all -> 0x016c }
            goto L_0x0170
        L_0x016c:
            r10 = move-exception
            r9.addSuppressed(r10)
        L_0x0170:
            throw r9
        L_0x0171:
            if (r10 == 0) goto L_0x01c8
            r10.close()
            goto L_0x01c8
        L_0x0177:
            android.database.Cursor r10 = r9.queryVvmDataBufferDB(r2)
            if (r10 == 0) goto L_0x01c3
            boolean r1 = r10.moveToFirst()     // Catch:{ all -> 0x01b9 }
            if (r1 == 0) goto L_0x01c3
            java.lang.String r0 = "res_url"
            int r0 = r10.getColumnIndexOrThrow(r0)     // Catch:{ all -> 0x01b9 }
            java.lang.String r0 = r10.getString(r0)     // Catch:{ all -> 0x01b9 }
            int r1 = r10.getColumnIndexOrThrow(r4)     // Catch:{ all -> 0x01b9 }
            java.lang.String r1 = r10.getString(r1)     // Catch:{ all -> 0x01b9 }
            com.sec.internal.ims.cmstore.omanetapi.nms.data.AttributeTranslator r2 = new com.sec.internal.ims.cmstore.omanetapi.nms.data.AttributeTranslator     // Catch:{ all -> 0x01b9 }
            com.sec.internal.ims.cmstore.MessageStoreClient r3 = r9.mStoreClient     // Catch:{ all -> 0x01b9 }
            r2.<init>(r3)     // Catch:{ all -> 0x01b9 }
            java.lang.String[] r1 = new java.lang.String[]{r1}     // Catch:{ all -> 0x01b9 }
            r2.setV2tLanguage(r1)     // Catch:{ all -> 0x01b9 }
            java.lang.String[] r1 = new java.lang.String[r6]     // Catch:{ all -> 0x01b9 }
            java.lang.String r9 = r9.encodeResURL(r0)     // Catch:{ all -> 0x01b9 }
            r1[r5] = r9     // Catch:{ all -> 0x01b9 }
            r2.setV2tResourceURL(r1)     // Catch:{ all -> 0x01b9 }
            com.sec.internal.ims.cmstore.params.ParamVvmUpdate$VvmTypeChange r9 = com.sec.internal.ims.cmstore.params.ParamVvmUpdate.VvmTypeChange.ADHOC_V2T     // Catch:{ all -> 0x01b9 }
            com.sec.internal.omanetapi.nms.data.AttributeList r0 = r2.getAttributeList()     // Catch:{ all -> 0x01b9 }
            r11.attributes = r0     // Catch:{ all -> 0x01b9 }
            r0 = r9
            goto L_0x01c3
        L_0x01b9:
            r9 = move-exception
            r10.close()     // Catch:{ all -> 0x01be }
            goto L_0x01c2
        L_0x01be:
            r10 = move-exception
            r9.addSuppressed(r10)
        L_0x01c2:
            throw r9
        L_0x01c3:
            if (r10 == 0) goto L_0x01c8
            r10.close()
        L_0x01c8:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.omanetapi.bufferdbtranslation.BufferDBTranslation.getVVMServiceProfileFromBufDb(com.sec.internal.ims.cmstore.params.BufferDBChangeParam, com.sec.internal.ims.cmstore.omanetapi.tmoappapi.data.VvmServiceProfile):com.sec.internal.ims.cmstore.params.ParamVvmUpdate$VvmTypeChange");
    }

    public Pair<Object, HttpPostBody> getVVMGreetingObjectPairFromBufDb(BufferDBChangeParam bufferDBChangeParam) {
        return new Pair<>(getVvmObjectFromDB(bufferDBChangeParam), getVvmGreetingBodyFromDB(bufferDBChangeParam));
    }

    public Pair<String, List<String>> getObjectIdPartIdFromRCSBufDb(BufferDBChangeParam bufferDBChangeParam) {
        ArrayList arrayList = new ArrayList();
        Cursor queryRCSMessageDBUsingRowId = queryRCSMessageDBUsingRowId(bufferDBChangeParam.mRowId);
        String str = "";
        if (queryRCSMessageDBUsingRowId != null) {
            try {
                if (queryRCSMessageDBUsingRowId.moveToFirst()) {
                    String string = queryRCSMessageDBUsingRowId.getString(queryRCSMessageDBUsingRowId.getColumnIndex(CloudMessageProviderContract.BufferDBExtensionBase.RES_URL));
                    String str2 = this.LOG_TAG;
                    Log.i(str2, "resUrl: " + IMSLog.checker(string));
                    if (string != null) {
                        str = string.substring(string.lastIndexOf(47) + 1);
                    }
                    String string2 = queryRCSMessageDBUsingRowId.getString(queryRCSMessageDBUsingRowId.getColumnIndex(CloudMessageProviderContract.BufferDBExtensionBase.PAYLOADPARTFULL));
                    if (string2 != null) {
                        arrayList.add(string2.substring(string2.lastIndexOf(47) + 1));
                    }
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (queryRCSMessageDBUsingRowId != null) {
            queryRCSMessageDBUsingRowId.close();
        }
        return new Pair<>(str, arrayList);
        throw th;
    }

    /* JADX WARNING: Removed duplicated region for block: B:14:0x0032  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public android.util.Pair<java.lang.String, java.lang.String> getPayloadPartandAllPayloadUrlFromRCSBufDb(com.sec.internal.ims.cmstore.params.BufferDBChangeParam r3) {
        /*
            r2 = this;
            long r0 = r3.mRowId
            android.database.Cursor r2 = r2.queryRCSMessageDBUsingRowId(r0)
            if (r2 == 0) goto L_0x002d
            boolean r3 = r2.moveToFirst()     // Catch:{ all -> 0x0023 }
            if (r3 == 0) goto L_0x002d
            java.lang.String r3 = "payloadpartFull"
            int r3 = r2.getColumnIndex(r3)     // Catch:{ all -> 0x0023 }
            java.lang.String r3 = r2.getString(r3)     // Catch:{ all -> 0x0023 }
            java.lang.String r0 = "payloadurl"
            int r0 = r2.getColumnIndex(r0)     // Catch:{ all -> 0x0023 }
            java.lang.String r0 = r2.getString(r0)     // Catch:{ all -> 0x0023 }
            goto L_0x0030
        L_0x0023:
            r3 = move-exception
            r2.close()     // Catch:{ all -> 0x0028 }
            goto L_0x002c
        L_0x0028:
            r2 = move-exception
            r3.addSuppressed(r2)
        L_0x002c:
            throw r3
        L_0x002d:
            java.lang.String r3 = ""
            r0 = r3
        L_0x0030:
            if (r2 == 0) goto L_0x0035
            r2.close()
        L_0x0035:
            android.util.Pair r2 = new android.util.Pair
            r2.<init>(r3, r0)
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.omanetapi.bufferdbtranslation.BufferDBTranslation.getPayloadPartandAllPayloadUrlFromRCSBufDb(com.sec.internal.ims.cmstore.params.BufferDBChangeParam):android.util.Pair");
    }

    /* JADX WARNING: Removed duplicated region for block: B:14:0x004a  */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x0055  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public android.util.Pair<java.lang.String, java.lang.String> getAllPayloadUrlFromRCSBufDb(com.sec.internal.ims.cmstore.params.BufferDBChangeParam r4) {
        /*
            r3 = this;
            long r0 = r4.mRowId
            android.database.Cursor r3 = r3.queryRCSMessageDBUsingRowId(r0)
            if (r3 == 0) goto L_0x0045
            boolean r0 = r3.moveToFirst()     // Catch:{ all -> 0x003b }
            if (r0 == 0) goto L_0x0045
            java.lang.String r0 = "payloadpartFull"
            int r0 = r3.getColumnIndex(r0)     // Catch:{ all -> 0x003b }
            java.lang.String r0 = r3.getString(r0)     // Catch:{ all -> 0x003b }
            java.lang.String r1 = "payloadurl"
            int r1 = r3.getColumnIndex(r1)     // Catch:{ all -> 0x003b }
            java.lang.String r1 = r3.getString(r1)     // Catch:{ all -> 0x003b }
            java.lang.String r2 = "payloadpartThumb"
            int r2 = r3.getColumnIndex(r2)     // Catch:{ all -> 0x003b }
            java.lang.String r2 = r3.getString(r2)     // Catch:{ all -> 0x003b }
            r4.mPayloadThumbnailUrl = r2     // Catch:{ all -> 0x003b }
            java.lang.String r2 = "payloadpartThumb_filename"
            int r2 = r3.getColumnIndex(r2)     // Catch:{ all -> 0x003b }
            java.lang.String r2 = r3.getString(r2)     // Catch:{ all -> 0x003b }
            r4.mFTThumbnailFileName = r2     // Catch:{ all -> 0x003b }
            goto L_0x0048
        L_0x003b:
            r4 = move-exception
            r3.close()     // Catch:{ all -> 0x0040 }
            goto L_0x0044
        L_0x0040:
            r3 = move-exception
            r4.addSuppressed(r3)
        L_0x0044:
            throw r4
        L_0x0045:
            java.lang.String r0 = ""
            r1 = r0
        L_0x0048:
            if (r3 == 0) goto L_0x004d
            r3.close()
        L_0x004d:
            java.lang.String r3 = r4.mPayloadThumbnailUrl
            boolean r3 = android.text.TextUtils.isEmpty(r3)
            if (r3 != 0) goto L_0x0058
            r3 = 1
            r4.mIsFTThumbnail = r3
        L_0x0058:
            android.util.Pair r3 = new android.util.Pair
            r3.<init>(r0, r1)
            return r3
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.omanetapi.bufferdbtranslation.BufferDBTranslation.getAllPayloadUrlFromRCSBufDb(com.sec.internal.ims.cmstore.params.BufferDBChangeParam):android.util.Pair");
    }

    /* JADX WARNING: Removed duplicated region for block: B:18:0x005a  */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x0063 A[SYNTHETIC, Splitter:B:21:0x0063] */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x00c4  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public android.util.Pair<java.lang.String, java.util.List<java.lang.String>> getObjectIdPartIdFromMmsBufDb(com.sec.internal.ims.cmstore.params.BufferDBChangeParam r10) {
        /*
            r9 = this;
            java.util.ArrayList r0 = new java.util.ArrayList
            r0.<init>()
            long r1 = r10.mRowId
            android.database.Cursor r10 = r9.querymmsPduBufferDB(r1)
            r3 = 47
            java.lang.String r4 = ""
            if (r10 == 0) goto L_0x0057
            boolean r5 = r10.moveToFirst()     // Catch:{ all -> 0x004b }
            if (r5 == 0) goto L_0x0057
            java.lang.String r5 = "res_url"
            int r5 = r10.getColumnIndex(r5)     // Catch:{ all -> 0x004b }
            java.lang.String r5 = r10.getString(r5)     // Catch:{ all -> 0x004b }
            java.lang.String r6 = r9.LOG_TAG     // Catch:{ all -> 0x004b }
            java.lang.StringBuilder r7 = new java.lang.StringBuilder     // Catch:{ all -> 0x004b }
            r7.<init>()     // Catch:{ all -> 0x004b }
            java.lang.String r8 = "resUrl: "
            r7.append(r8)     // Catch:{ all -> 0x004b }
            java.lang.String r8 = com.sec.internal.log.IMSLog.checker(r5)     // Catch:{ all -> 0x004b }
            r7.append(r8)     // Catch:{ all -> 0x004b }
            java.lang.String r7 = r7.toString()     // Catch:{ all -> 0x004b }
            android.util.Log.i(r6, r7)     // Catch:{ all -> 0x004b }
            if (r5 != 0) goto L_0x0040
            goto L_0x0057
        L_0x0040:
            int r6 = r5.lastIndexOf(r3)     // Catch:{ all -> 0x004b }
            int r6 = r6 + 1
            java.lang.String r5 = r5.substring(r6)     // Catch:{ all -> 0x004b }
            goto L_0x0058
        L_0x004b:
            r9 = move-exception
            if (r10 == 0) goto L_0x0056
            r10.close()     // Catch:{ all -> 0x0052 }
            goto L_0x0056
        L_0x0052:
            r10 = move-exception
            r9.addSuppressed(r10)
        L_0x0056:
            throw r9
        L_0x0057:
            r5 = r4
        L_0x0058:
            if (r10 == 0) goto L_0x005d
            r10.close()
        L_0x005d:
            android.database.Cursor r10 = r9.queryPartsBufferDBUsingPduBufferId(r1)
            if (r10 == 0) goto L_0x00c2
            boolean r1 = r10.moveToFirst()     // Catch:{ all -> 0x00b8 }
            if (r1 == 0) goto L_0x00c2
        L_0x0069:
            java.lang.String r1 = "payloadurl"
            int r1 = r10.getColumnIndex(r1)     // Catch:{ all -> 0x00b8 }
            java.lang.String r1 = r10.getString(r1)     // Catch:{ all -> 0x00b8 }
            boolean r2 = android.text.TextUtils.isEmpty(r1)     // Catch:{ all -> 0x00b8 }
            if (r2 == 0) goto L_0x007b
            r2 = r4
            goto L_0x0085
        L_0x007b:
            int r2 = r1.lastIndexOf(r3)     // Catch:{ all -> 0x00b8 }
            int r2 = r2 + 1
            java.lang.String r2 = r1.substring(r2)     // Catch:{ all -> 0x00b8 }
        L_0x0085:
            java.lang.String r6 = r9.LOG_TAG     // Catch:{ all -> 0x00b8 }
            java.lang.StringBuilder r7 = new java.lang.StringBuilder     // Catch:{ all -> 0x00b8 }
            r7.<init>()     // Catch:{ all -> 0x00b8 }
            java.lang.String r8 = "payLoadurl: "
            r7.append(r8)     // Catch:{ all -> 0x00b8 }
            java.lang.String r1 = com.sec.internal.log.IMSLog.checker(r1)     // Catch:{ all -> 0x00b8 }
            r7.append(r1)     // Catch:{ all -> 0x00b8 }
            java.lang.String r1 = "partId: "
            r7.append(r1)     // Catch:{ all -> 0x00b8 }
            r7.append(r2)     // Catch:{ all -> 0x00b8 }
            java.lang.String r1 = r7.toString()     // Catch:{ all -> 0x00b8 }
            android.util.Log.i(r6, r1)     // Catch:{ all -> 0x00b8 }
            boolean r1 = android.text.TextUtils.isEmpty(r2)     // Catch:{ all -> 0x00b8 }
            if (r1 == 0) goto L_0x00ae
            goto L_0x00b1
        L_0x00ae:
            r0.add(r2)     // Catch:{ all -> 0x00b8 }
        L_0x00b1:
            boolean r1 = r10.moveToNext()     // Catch:{ all -> 0x00b8 }
            if (r1 != 0) goto L_0x0069
            goto L_0x00c2
        L_0x00b8:
            r9 = move-exception
            r10.close()     // Catch:{ all -> 0x00bd }
            goto L_0x00c1
        L_0x00bd:
            r10 = move-exception
            r9.addSuppressed(r10)
        L_0x00c1:
            throw r9
        L_0x00c2:
            if (r10 == 0) goto L_0x00c7
            r10.close()
        L_0x00c7:
            android.util.Pair r9 = new android.util.Pair
            r9.<init>(r5, r0)
            return r9
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.omanetapi.bufferdbtranslation.BufferDBTranslation.getObjectIdPartIdFromMmsBufDb(com.sec.internal.ims.cmstore.params.BufferDBChangeParam):android.util.Pair");
    }

    public List<String> getPayloadPartUrlFromMmsBufDb(BufferDBChangeParam bufferDBChangeParam) {
        ArrayList arrayList = new ArrayList();
        Cursor queryPartsBufferDBUsingPduBufferId = queryPartsBufferDBUsingPduBufferId(bufferDBChangeParam.mRowId);
        if (queryPartsBufferDBUsingPduBufferId != null) {
            try {
                if (queryPartsBufferDBUsingPduBufferId.moveToFirst()) {
                    do {
                        String string = queryPartsBufferDBUsingPduBufferId.getString(queryPartsBufferDBUsingPduBufferId.getColumnIndex(CloudMessageProviderContract.BufferDBExtensionBase.PAYLOADURL));
                        String str = this.LOG_TAG;
                        Log.i(str, "payLoadurl: " + IMSLog.checker(string));
                        if (!TextUtils.isEmpty(string)) {
                            arrayList.add(string);
                        }
                    } while (queryPartsBufferDBUsingPduBufferId.moveToNext());
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (queryPartsBufferDBUsingPduBufferId != null) {
            queryPartsBufferDBUsingPduBufferId.close();
        }
        return arrayList;
        throw th;
    }

    /* JADX WARNING: Removed duplicated region for block: B:14:0x003c  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public java.lang.String getPayloadUrlfromMmsPduBufferId(com.sec.internal.ims.cmstore.params.BufferDBChangeParam r4) {
        /*
            r3 = this;
            long r0 = r4.mRowId
            android.database.Cursor r4 = r3.querymmsPduBufferDB(r0)
            if (r4 == 0) goto L_0x0039
            boolean r0 = r4.moveToFirst()     // Catch:{ all -> 0x002f }
            if (r0 == 0) goto L_0x0039
            java.lang.String r0 = "ct_l"
            int r0 = r4.getColumnIndexOrThrow(r0)     // Catch:{ all -> 0x002f }
            java.lang.String r0 = r4.getString(r0)     // Catch:{ all -> 0x002f }
            java.lang.String r3 = r3.LOG_TAG     // Catch:{ all -> 0x002f }
            java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch:{ all -> 0x002f }
            r1.<init>()     // Catch:{ all -> 0x002f }
            java.lang.String r2 = "get Payload URL "
            r1.append(r2)     // Catch:{ all -> 0x002f }
            r1.append(r0)     // Catch:{ all -> 0x002f }
            java.lang.String r1 = r1.toString()     // Catch:{ all -> 0x002f }
            android.util.Log.i(r3, r1)     // Catch:{ all -> 0x002f }
            goto L_0x003a
        L_0x002f:
            r3 = move-exception
            r4.close()     // Catch:{ all -> 0x0034 }
            goto L_0x0038
        L_0x0034:
            r4 = move-exception
            r3.addSuppressed(r4)
        L_0x0038:
            throw r3
        L_0x0039:
            r0 = 0
        L_0x003a:
            if (r4 == 0) goto L_0x003f
            r4.close()
        L_0x003f:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.omanetapi.bufferdbtranslation.BufferDBTranslation.getPayloadUrlfromMmsPduBufferId(com.sec.internal.ims.cmstore.params.BufferDBChangeParam):java.lang.String");
    }

    /* JADX WARNING: Removed duplicated region for block: B:14:0x0040  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public java.lang.String getPayloadPartUrlFromMmsPartUsingPartBufferId(com.sec.internal.ims.cmstore.params.BufferDBChangeParam r4) {
        /*
            r3 = this;
            long r0 = r4.mRowId
            android.database.Cursor r4 = r3.queryPartsBufferDBUsingPartBufferId(r0)
            if (r4 == 0) goto L_0x003d
            boolean r0 = r4.moveToFirst()     // Catch:{ all -> 0x0033 }
            if (r0 == 0) goto L_0x003d
            java.lang.String r0 = "payloadurl"
            int r0 = r4.getColumnIndex(r0)     // Catch:{ all -> 0x0033 }
            java.lang.String r0 = r4.getString(r0)     // Catch:{ all -> 0x0033 }
            java.lang.String r3 = r3.LOG_TAG     // Catch:{ all -> 0x0033 }
            java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch:{ all -> 0x0033 }
            r1.<init>()     // Catch:{ all -> 0x0033 }
            java.lang.String r2 = "payLoadurl: "
            r1.append(r2)     // Catch:{ all -> 0x0033 }
            java.lang.String r2 = com.sec.internal.log.IMSLog.checker(r0)     // Catch:{ all -> 0x0033 }
            r1.append(r2)     // Catch:{ all -> 0x0033 }
            java.lang.String r1 = r1.toString()     // Catch:{ all -> 0x0033 }
            android.util.Log.d(r3, r1)     // Catch:{ all -> 0x0033 }
            goto L_0x003e
        L_0x0033:
            r3 = move-exception
            r4.close()     // Catch:{ all -> 0x0038 }
            goto L_0x003c
        L_0x0038:
            r4 = move-exception
            r3.addSuppressed(r4)
        L_0x003c:
            throw r3
        L_0x003d:
            r0 = 0
        L_0x003e:
            if (r4 == 0) goto L_0x0043
            r4.close()
        L_0x0043:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.omanetapi.bufferdbtranslation.BufferDBTranslation.getPayloadPartUrlFromMmsPartUsingPartBufferId(com.sec.internal.ims.cmstore.params.BufferDBChangeParam):java.lang.String");
    }

    public ParamVvmUpdate.VvmGreetingType getVVMGreetingTypeFromBufDb(BufferDBChangeParam bufferDBChangeParam) {
        Cursor queryVvmGreetingBufferDB = queryVvmGreetingBufferDB(bufferDBChangeParam.mRowId);
        if (queryVvmGreetingBufferDB != null) {
            try {
                if (queryVvmGreetingBufferDB.moveToFirst()) {
                    int i = queryVvmGreetingBufferDB.getInt(queryVvmGreetingBufferDB.getColumnIndex(CloudMessageProviderContract.VVMGreetingColumns.GREETINGTYPE));
                    String str = this.LOG_TAG;
                    Log.i(str, "getVVMGreetingTypeFromBufDb : type " + i);
                    ParamVvmUpdate.VvmGreetingType valueOf = ParamVvmUpdate.VvmGreetingType.valueOf(i);
                    queryVvmGreetingBufferDB.close();
                    return valueOf;
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (queryVvmGreetingBufferDB != null) {
            queryVvmGreetingBufferDB.close();
        }
        return ParamVvmUpdate.VvmGreetingType.Default;
        throw th;
    }

    public Pair<Object, HttpPostBody> getRCSObjectPairFromCursor(BufferDBChangeParam bufferDBChangeParam, List<FileUploadResponse> list) {
        Pair<Object, HttpPostBody> chatObjectPairFromCursor;
        Log.d(this.LOG_TAG, bufferDBChangeParam.toString());
        Pair<Object, HttpPostBody> pair = null;
        if (bufferDBChangeParam.mDBIndex == 1) {
            Cursor queryrcsMessageBufferDB = queryrcsMessageBufferDB(bufferDBChangeParam.mRowId);
            if (queryrcsMessageBufferDB != null) {
                try {
                    if (queryrcsMessageBufferDB.moveToFirst()) {
                        int i = queryrcsMessageBufferDB.getInt(queryrcsMessageBufferDB.getColumnIndex(ImContract.Message.MESSAGE_ISSLM));
                        int i2 = queryrcsMessageBufferDB.getInt(queryrcsMessageBufferDB.getColumnIndex(ImContract.ChatItem.IS_FILE_TRANSFER));
                        String str = this.LOG_TAG;
                        Log.i(str, "getRCSObjectPairFromCursor :: isSlm: " + i + " isFt: " + i2);
                        if (i == 1) {
                            chatObjectPairFromCursor = getSlmObjectPairFromCursor(queryrcsMessageBufferDB);
                        } else if (i2 == 1) {
                            chatObjectPairFromCursor = getFtObjectPairFromCursor(queryrcsMessageBufferDB);
                        } else {
                            chatObjectPairFromCursor = getChatObjectPairFromCursor(queryrcsMessageBufferDB);
                        }
                        pair = chatObjectPairFromCursor;
                    }
                } catch (Throwable th) {
                    th.addSuppressed(th);
                }
            }
            if (queryrcsMessageBufferDB != null) {
                queryrcsMessageBufferDB.close();
            }
        }
        return pair;
        throw th;
    }

    public Pair<Object, HttpPostBody> getRcsSessionFromCursor(BufferDBChangeParam bufferDBChangeParam) {
        String str = this.LOG_TAG;
        Log.i(str, "getRcsSessionFromCursor " + bufferDBChangeParam.mRowId);
        Pair<Object, HttpPostBody> pair = null;
        if (bufferDBChangeParam.mDBIndex == 10) {
            Cursor queryGroupSessionDB = queryGroupSessionDB(bufferDBChangeParam.mRowId);
            if (queryGroupSessionDB != null) {
                try {
                    if (queryGroupSessionDB.moveToFirst()) {
                        pair = getConferenceInfoObjectPair(queryGroupSessionDB);
                    }
                } catch (Throwable th) {
                    th.addSuppressed(th);
                }
            }
            if (queryGroupSessionDB != null) {
                queryGroupSessionDB.close();
            }
        }
        return pair;
        throw th;
    }

    /* JADX WARNING: Removed duplicated region for block: B:28:0x011c  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public android.util.Pair<com.sec.internal.omanetapi.nms.data.Object, com.sec.internal.helper.httpclient.HttpPostBody> getSmsObjectPairFromCursor(com.sec.internal.ims.cmstore.params.BufferDBChangeParam r13) {
        /*
            r12 = this;
            long r0 = r13.mRowId
            android.database.Cursor r13 = r12.querySMSBufferDB(r0)
            if (r13 == 0) goto L_0x0119
            boolean r0 = r13.moveToFirst()     // Catch:{ all -> 0x010f }
            if (r0 == 0) goto L_0x0119
            com.sec.internal.omanetapi.nms.data.Object r0 = new com.sec.internal.omanetapi.nms.data.Object     // Catch:{ all -> 0x010f }
            r0.<init>()     // Catch:{ all -> 0x010f }
            com.sec.internal.omanetapi.nms.data.FlagList r1 = new com.sec.internal.omanetapi.nms.data.FlagList     // Catch:{ all -> 0x010f }
            r1.<init>()     // Catch:{ all -> 0x010f }
            r0.flags = r1     // Catch:{ all -> 0x010f }
            java.lang.String r1 = "read"
            int r1 = r13.getColumnIndex(r1)     // Catch:{ all -> 0x010f }
            int r1 = r13.getInt(r1)     // Catch:{ all -> 0x010f }
            java.lang.String r2 = "type"
            int r2 = r13.getColumnIndex(r2)     // Catch:{ all -> 0x010f }
            long r2 = r13.getLong(r2)     // Catch:{ all -> 0x010f }
            java.lang.String r4 = "\\Flagged"
            r5 = 2
            r7 = 0
            r8 = 1
            if (r1 == r8) goto L_0x0046
            int r1 = (r2 > r5 ? 1 : (r2 == r5 ? 0 : -1))
            if (r1 != 0) goto L_0x003d
            goto L_0x0046
        L_0x003d:
            com.sec.internal.omanetapi.nms.data.FlagList r1 = r0.flags     // Catch:{ all -> 0x010f }
            java.lang.String[] r9 = new java.lang.String[r8]     // Catch:{ all -> 0x010f }
            r9[r7] = r4     // Catch:{ all -> 0x010f }
            r1.flag = r9     // Catch:{ all -> 0x010f }
            goto L_0x0053
        L_0x0046:
            com.sec.internal.omanetapi.nms.data.FlagList r1 = r0.flags     // Catch:{ all -> 0x010f }
            r9 = 2
            java.lang.String[] r9 = new java.lang.String[r9]     // Catch:{ all -> 0x010f }
            r9[r7] = r4     // Catch:{ all -> 0x010f }
            java.lang.String r4 = "\\Seen"
            r9[r8] = r4     // Catch:{ all -> 0x010f }
            r1.flag = r9     // Catch:{ all -> 0x010f }
        L_0x0053:
            com.sec.internal.ims.cmstore.omanetapi.nms.data.AttributeTranslator r1 = new com.sec.internal.ims.cmstore.omanetapi.nms.data.AttributeTranslator     // Catch:{ all -> 0x010f }
            com.sec.internal.ims.cmstore.MessageStoreClient r4 = r12.mStoreClient     // Catch:{ all -> 0x010f }
            r1.<init>(r4)     // Catch:{ all -> 0x010f }
            java.text.SimpleDateFormat r4 = r12.sFormatOfName     // Catch:{ all -> 0x010f }
            java.util.Date r9 = new java.util.Date     // Catch:{ all -> 0x010f }
            java.lang.String r10 = "date"
            int r10 = r13.getColumnIndex(r10)     // Catch:{ all -> 0x010f }
            long r10 = r13.getLong(r10)     // Catch:{ all -> 0x010f }
            r9.<init>(r10)     // Catch:{ all -> 0x010f }
            java.lang.String r4 = r4.format(r9)     // Catch:{ all -> 0x010f }
            java.lang.String[] r4 = new java.lang.String[]{r4}     // Catch:{ all -> 0x010f }
            r1.setDate(r4)     // Catch:{ all -> 0x010f }
            java.lang.String r4 = "address"
            int r4 = r13.getColumnIndex(r4)     // Catch:{ all -> 0x010f }
            java.lang.String r4 = r13.getString(r4)     // Catch:{ all -> 0x010f }
            r9 = 1
            int r9 = (r2 > r9 ? 1 : (r2 == r9 ? 0 : -1))
            if (r9 != 0) goto L_0x00b0
            java.lang.String[] r2 = new java.lang.String[r8]     // Catch:{ all -> 0x010f }
            java.lang.String r3 = "IN"
            r2[r7] = r3     // Catch:{ all -> 0x010f }
            r1.setDirection(r2)     // Catch:{ all -> 0x010f }
            java.lang.String[] r2 = new java.lang.String[r8]     // Catch:{ all -> 0x010f }
            java.lang.String r3 = r12.getE164FormatNumber(r4)     // Catch:{ all -> 0x010f }
            r2[r7] = r3     // Catch:{ all -> 0x010f }
            r1.setFrom(r2)     // Catch:{ all -> 0x010f }
            java.lang.String[] r2 = new java.lang.String[r8]     // Catch:{ all -> 0x010f }
            com.sec.internal.ims.cmstore.MessageStoreClient r3 = r12.mStoreClient     // Catch:{ all -> 0x010f }
            com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager r3 = r3.getPrerenceManager()     // Catch:{ all -> 0x010f }
            java.lang.String r3 = r3.getUserCtn()     // Catch:{ all -> 0x010f }
            java.lang.String r12 = r12.getE164FormatNumber(r3)     // Catch:{ all -> 0x010f }
            r2[r7] = r12     // Catch:{ all -> 0x010f }
            r1.setTo(r2)     // Catch:{ all -> 0x010f }
            goto L_0x00dd
        L_0x00b0:
            int r2 = (r2 > r5 ? 1 : (r2 == r5 ? 0 : -1))
            if (r2 != 0) goto L_0x00dd
            java.lang.String[] r2 = new java.lang.String[r8]     // Catch:{ all -> 0x010f }
            java.lang.String r3 = "OUT"
            r2[r7] = r3     // Catch:{ all -> 0x010f }
            r1.setDirection(r2)     // Catch:{ all -> 0x010f }
            java.lang.String[] r2 = new java.lang.String[r8]     // Catch:{ all -> 0x010f }
            java.lang.String r3 = r12.getE164FormatNumber(r4)     // Catch:{ all -> 0x010f }
            r2[r7] = r3     // Catch:{ all -> 0x010f }
            r1.setTo(r2)     // Catch:{ all -> 0x010f }
            java.lang.String[] r2 = new java.lang.String[r8]     // Catch:{ all -> 0x010f }
            com.sec.internal.ims.cmstore.MessageStoreClient r3 = r12.mStoreClient     // Catch:{ all -> 0x010f }
            com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager r3 = r3.getPrerenceManager()     // Catch:{ all -> 0x010f }
            java.lang.String r3 = r3.getUserCtn()     // Catch:{ all -> 0x010f }
            java.lang.String r12 = r12.getE164FormatNumber(r3)     // Catch:{ all -> 0x010f }
            r2[r7] = r12     // Catch:{ all -> 0x010f }
            r1.setFrom(r2)     // Catch:{ all -> 0x010f }
        L_0x00dd:
            java.lang.String[] r12 = new java.lang.String[r8]     // Catch:{ all -> 0x010f }
            java.lang.String r2 = "no"
            r12[r7] = r2     // Catch:{ all -> 0x010f }
            r1.setCpmGroup(r12)     // Catch:{ all -> 0x010f }
            java.lang.String[] r12 = new java.lang.String[r8]     // Catch:{ all -> 0x010f }
            java.lang.String r2 = "pager-message"
            r12[r7] = r2     // Catch:{ all -> 0x010f }
            r1.setMessageContext(r12)     // Catch:{ all -> 0x010f }
            com.sec.internal.omanetapi.nms.data.AttributeList r12 = r1.getAttributeList()     // Catch:{ all -> 0x010f }
            r0.attributes = r12     // Catch:{ all -> 0x010f }
            com.sec.internal.helper.httpclient.HttpPostBody r12 = new com.sec.internal.helper.httpclient.HttpPostBody     // Catch:{ all -> 0x010f }
            java.lang.String r1 = "form-data;name=\"attachments\";filename=\"sms.txt\""
            java.lang.String r2 = "text/plain"
            java.lang.String r3 = "body"
            int r3 = r13.getColumnIndex(r3)     // Catch:{ all -> 0x010f }
            java.lang.String r3 = r13.getString(r3)     // Catch:{ all -> 0x010f }
            r12.<init>((java.lang.String) r1, (java.lang.String) r2, (java.lang.String) r3)     // Catch:{ all -> 0x010f }
            android.util.Pair r1 = new android.util.Pair     // Catch:{ all -> 0x010f }
            r1.<init>(r0, r12)     // Catch:{ all -> 0x010f }
            goto L_0x011a
        L_0x010f:
            r12 = move-exception
            r13.close()     // Catch:{ all -> 0x0114 }
            goto L_0x0118
        L_0x0114:
            r13 = move-exception
            r12.addSuppressed(r13)
        L_0x0118:
            throw r12
        L_0x0119:
            r1 = 0
        L_0x011a:
            if (r13 == 0) goto L_0x011f
            r13.close()
        L_0x011f:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.omanetapi.bufferdbtranslation.BufferDBTranslation.getSmsObjectPairFromCursor(com.sec.internal.ims.cmstore.params.BufferDBChangeParam):android.util.Pair");
    }

    public Pair<Object, HttpPostBody> getMmsObjectPairFromCursor(BufferDBChangeParam bufferDBChangeParam) {
        return new Pair<>(getMmsObjectFromPduAndAddress(bufferDBChangeParam), getMmsPartHttpPayloadFromCursor(queryPartsBufferDBUsingPduBufferId(bufferDBChangeParam.mRowId)));
    }

    /* JADX WARNING: Removed duplicated region for block: B:14:0x007f  */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x0089 A[SYNTHETIC, Splitter:B:17:0x0089] */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x011a A[Catch:{ all -> 0x0139, all -> 0x013e }] */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x011c A[Catch:{ all -> 0x0139, all -> 0x013e }] */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x0145  */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x0150 A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x0151  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public android.util.Pair<com.sec.internal.omanetapi.nms.data.Object, com.sec.internal.helper.httpclient.HttpPostBody> getImdnObjectPair(com.sec.internal.ims.cmstore.params.BufferDBChangeParam r13) {
        /*
            r12 = this;
            com.sec.internal.omanetapi.nms.data.Object r0 = new com.sec.internal.omanetapi.nms.data.Object
            r0.<init>()
            com.sec.internal.ims.cmstore.omanetapi.nms.data.AttributeTranslator r1 = new com.sec.internal.ims.cmstore.omanetapi.nms.data.AttributeTranslator
            com.sec.internal.ims.cmstore.MessageStoreClient r2 = r12.mStoreClient
            r1.<init>(r2)
            java.lang.String r2 = "imdn-message"
            java.lang.String[] r2 = new java.lang.String[]{r2}
            r1.setMessageContext(r2)
            long r2 = r13.mRowId
            android.database.Cursor r13 = r12.queryRCSNotificationDB(r2)
            r2 = 0
            if (r13 == 0) goto L_0x007b
            boolean r3 = r13.moveToFirst()     // Catch:{ all -> 0x0071 }
            if (r3 == 0) goto L_0x007b
            java.lang.String r3 = "imdn_id"
            int r3 = r13.getColumnIndex(r3)     // Catch:{ all -> 0x0071 }
            java.lang.String r3 = r13.getString(r3)     // Catch:{ all -> 0x0071 }
            r0.correlationId = r3     // Catch:{ all -> 0x0071 }
            java.lang.String[] r4 = new java.lang.String[]{r3}     // Catch:{ all -> 0x0071 }
            r1.setDispositionOriginalMessageID(r4)     // Catch:{ all -> 0x0071 }
            java.lang.String r4 = "sender_uri"
            int r4 = r13.getColumnIndex(r4)     // Catch:{ all -> 0x0071 }
            java.lang.String r4 = r13.getString(r4)     // Catch:{ all -> 0x0071 }
            com.sec.ims.util.ImsUri r4 = com.sec.ims.util.ImsUri.parse(r4)     // Catch:{ all -> 0x0071 }
            java.lang.String r4 = r4.getMsisdn()     // Catch:{ all -> 0x0071 }
            java.lang.String r5 = r12.LOG_TAG     // Catch:{ all -> 0x0071 }
            java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ all -> 0x0071 }
            r6.<init>()     // Catch:{ all -> 0x0071 }
            java.lang.String r7 = "getImdnObjectPairFromCursor :: ImdnID : "
            r6.append(r7)     // Catch:{ all -> 0x0071 }
            java.lang.String r7 = com.sec.internal.log.IMSLog.checker(r3)     // Catch:{ all -> 0x0071 }
            r6.append(r7)     // Catch:{ all -> 0x0071 }
            java.lang.String r7 = " parsed opUri : "
            r6.append(r7)     // Catch:{ all -> 0x0071 }
            java.lang.String r7 = com.sec.internal.log.IMSLog.checker(r4)     // Catch:{ all -> 0x0071 }
            r6.append(r7)     // Catch:{ all -> 0x0071 }
            java.lang.String r6 = r6.toString()     // Catch:{ all -> 0x0071 }
            android.util.Log.i(r5, r6)     // Catch:{ all -> 0x0071 }
            goto L_0x007d
        L_0x0071:
            r12 = move-exception
            r13.close()     // Catch:{ all -> 0x0076 }
            goto L_0x007a
        L_0x0076:
            r13 = move-exception
            r12.addSuppressed(r13)
        L_0x007a:
            throw r12
        L_0x007b:
            r3 = r2
            r4 = r3
        L_0x007d:
            if (r13 == 0) goto L_0x0082
            r13.close()
        L_0x0082:
            android.database.Cursor r13 = r12.queryRCSMessageDBUsingImdn(r3)
            r3 = 0
            if (r13 == 0) goto L_0x0143
            boolean r5 = r13.moveToFirst()     // Catch:{ all -> 0x0139 }
            if (r5 == 0) goto L_0x0143
            java.lang.String r5 = "notification_status"
            int r5 = r13.getColumnIndex(r5)     // Catch:{ all -> 0x0139 }
            int r5 = r13.getInt(r5)     // Catch:{ all -> 0x0139 }
            com.sec.internal.constants.ims.servicemodules.im.NotificationStatus r6 = com.sec.internal.constants.ims.servicemodules.im.NotificationStatus.DELIVERED     // Catch:{ all -> 0x0139 }
            int r6 = r6.getId()     // Catch:{ all -> 0x0139 }
            r7 = 1
            if (r6 != r5) goto L_0x00b5
            java.lang.String[] r6 = new java.lang.String[r7]     // Catch:{ all -> 0x0139 }
            java.lang.String r8 = "delivery"
            r6[r3] = r8     // Catch:{ all -> 0x0139 }
            r1.setDispositionType(r6)     // Catch:{ all -> 0x0139 }
            java.lang.String[] r6 = new java.lang.String[r7]     // Catch:{ all -> 0x0139 }
            java.lang.String r8 = "delivered"
            r6[r3] = r8     // Catch:{ all -> 0x0139 }
            r1.setDispositionStatus(r6)     // Catch:{ all -> 0x0139 }
            goto L_0x00cf
        L_0x00b5:
            com.sec.internal.constants.ims.servicemodules.im.NotificationStatus r6 = com.sec.internal.constants.ims.servicemodules.im.NotificationStatus.DISPLAYED     // Catch:{ all -> 0x0139 }
            int r6 = r6.getId()     // Catch:{ all -> 0x0139 }
            if (r6 != r5) goto L_0x00d1
            java.lang.String[] r6 = new java.lang.String[r7]     // Catch:{ all -> 0x0139 }
            java.lang.String r8 = "display"
            r6[r3] = r8     // Catch:{ all -> 0x0139 }
            r1.setDispositionType(r6)     // Catch:{ all -> 0x0139 }
            java.lang.String[] r6 = new java.lang.String[r7]     // Catch:{ all -> 0x0139 }
            java.lang.String r8 = "displayed"
            r6[r3] = r8     // Catch:{ all -> 0x0139 }
            r1.setDispositionStatus(r6)     // Catch:{ all -> 0x0139 }
        L_0x00cf:
            r6 = r3
            goto L_0x00d2
        L_0x00d1:
            r6 = r7
        L_0x00d2:
            com.sec.internal.omanetapi.nms.data.FlagList r8 = new com.sec.internal.omanetapi.nms.data.FlagList     // Catch:{ all -> 0x0139 }
            r8.<init>()     // Catch:{ all -> 0x0139 }
            r0.flags = r8     // Catch:{ all -> 0x0139 }
            r9 = 2
            java.lang.String[] r9 = new java.lang.String[r9]     // Catch:{ all -> 0x0139 }
            java.lang.String r10 = "\\Flagged"
            r9[r3] = r10     // Catch:{ all -> 0x0139 }
            java.lang.String r10 = "\\Seen"
            r9[r7] = r10     // Catch:{ all -> 0x0139 }
            r8.flag = r9     // Catch:{ all -> 0x0139 }
            java.lang.String r8 = "direction"
            int r8 = r13.getColumnIndex(r8)     // Catch:{ all -> 0x0139 }
            int r8 = r13.getInt(r8)     // Catch:{ all -> 0x0139 }
            long r8 = (long) r8     // Catch:{ all -> 0x0139 }
            java.lang.String r12 = r12.LOG_TAG     // Catch:{ all -> 0x0139 }
            java.lang.StringBuilder r10 = new java.lang.StringBuilder     // Catch:{ all -> 0x0139 }
            r10.<init>()     // Catch:{ all -> 0x0139 }
            java.lang.String r11 = "getImdnObjectPairFromCursor :: direction : "
            r10.append(r11)     // Catch:{ all -> 0x0139 }
            r10.append(r8)     // Catch:{ all -> 0x0139 }
            java.lang.String r11 = " notificationStatus: "
            r10.append(r11)     // Catch:{ all -> 0x0139 }
            r10.append(r5)     // Catch:{ all -> 0x0139 }
            java.lang.String r5 = r10.toString()     // Catch:{ all -> 0x0139 }
            android.util.Log.i(r12, r5)     // Catch:{ all -> 0x0139 }
            com.sec.internal.constants.ims.servicemodules.im.ImDirection r12 = com.sec.internal.constants.ims.servicemodules.im.ImDirection.INCOMING     // Catch:{ all -> 0x0139 }
            int r12 = r12.getId()     // Catch:{ all -> 0x0139 }
            long r10 = (long) r12     // Catch:{ all -> 0x0139 }
            int r12 = (r8 > r10 ? 1 : (r8 == r10 ? 0 : -1))
            if (r12 != 0) goto L_0x011c
            r3 = r7
            goto L_0x0143
        L_0x011c:
            com.sec.internal.constants.ims.servicemodules.im.ImDirection r12 = com.sec.internal.constants.ims.servicemodules.im.ImDirection.OUTGOING     // Catch:{ all -> 0x0139 }
            int r12 = r12.getId()     // Catch:{ all -> 0x0139 }
            long r10 = (long) r12     // Catch:{ all -> 0x0139 }
            int r12 = (r8 > r10 ? 1 : (r8 == r10 ? 0 : -1))
            if (r12 != 0) goto L_0x0137
            java.lang.String[] r12 = new java.lang.String[r7]     // Catch:{ all -> 0x0139 }
            java.lang.String r5 = "IN"
            r12[r3] = r5     // Catch:{ all -> 0x0139 }
            r1.setDirection(r12)     // Catch:{ all -> 0x0139 }
            java.lang.String[] r12 = new java.lang.String[]{r4}     // Catch:{ all -> 0x0139 }
            r1.setDispositionOriginalTo(r12)     // Catch:{ all -> 0x0139 }
        L_0x0137:
            r3 = r6
            goto L_0x0143
        L_0x0139:
            r12 = move-exception
            r13.close()     // Catch:{ all -> 0x013e }
            goto L_0x0142
        L_0x013e:
            r13 = move-exception
            r12.addSuppressed(r13)
        L_0x0142:
            throw r12
        L_0x0143:
            if (r13 == 0) goto L_0x0148
            r13.close()
        L_0x0148:
            com.sec.internal.omanetapi.nms.data.AttributeList r12 = r1.getAttributeList()
            r0.attributes = r12
            if (r3 == 0) goto L_0x0151
            return r2
        L_0x0151:
            android.util.Pair r12 = new android.util.Pair
            r12.<init>(r0, r2)
            return r12
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.omanetapi.bufferdbtranslation.BufferDBTranslation.getImdnObjectPair(com.sec.internal.ims.cmstore.params.BufferDBChangeParam):android.util.Pair");
    }

    private String encodeResURL(String str) {
        StringBuilder sb = new StringBuilder(str);
        int lastIndexOf = str.lastIndexOf(":+");
        if (lastIndexOf > 0) {
            sb.replace(lastIndexOf, lastIndexOf + 2, "%3a%2b");
        }
        int lastIndexOf2 = sb.toString().lastIndexOf(":");
        if (lastIndexOf2 > 0) {
            sb.replace(lastIndexOf2, lastIndexOf2 + 1, "%3a");
        }
        String sb2 = sb.toString();
        String str2 = this.LOG_TAG;
        Log.i(str2, "encoded startIndex: " + lastIndexOf2 + ", endIndex: " + lastIndexOf + ", newResUrl: " + IMSLog.checker(sb2));
        return sb2;
    }
}
