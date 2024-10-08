package com.sec.internal.ims.cmstore.omanetapi.bufferdbtranslation;

import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import com.sec.internal.constants.ims.MIMEContentType;
import com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.cmstore.data.FlagNames;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImContract;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.helper.FileUtils;
import com.sec.internal.helper.httpclient.HttpPostBody;
import com.sec.internal.helper.translate.FileExtensionTranslator;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.omanetapi.bufferdbtranslation.BufferDBSupportTranslation;
import com.sec.internal.ims.cmstore.omanetapi.bufferdbtranslation.BufferQueryDBTranslation;
import com.sec.internal.ims.cmstore.omanetapi.nms.data.AttributeTranslator;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.params.ParamObjectUpload;
import com.sec.internal.ims.cmstore.utils.CmsUtil;
import com.sec.internal.ims.cmstore.utils.Util;
import com.sec.internal.ims.core.cmc.CmcConstants;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.interfaces.ims.cmstore.ITelephonyDBColumns;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.common.data.FileUploadResponse;
import com.sec.internal.omanetapi.file.FileData;
import com.sec.internal.omanetapi.nms.data.FlagList;
import com.sec.internal.omanetapi.nms.data.Object;
import com.sec.internal.omanetapi.nms.data.PayloadPartInfo;
import java.io.File;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BufferDBTranslationMcs extends BufferDBTranslation {
    private String LOG_TAG = BufferDBTranslationMcs.class.getSimpleName();

    public BufferDBTranslationMcs(MessageStoreClient messageStoreClient, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        super(messageStoreClient, iCloudMessageManagerHelper);
        this.LOG_TAG += "[" + messageStoreClient.getClientID() + "]";
    }

    /* access modifiers changed from: protected */
    public List<HttpPostBody> getMcsThumbBody(Cursor cursor, String str, String str2) {
        ArrayList arrayList = new ArrayList();
        String localFilePathForFtthumb = getLocalFilePathForFtthumb(cursor, str2);
        if (TextUtils.isEmpty(localFilePathForFtthumb)) {
            localFilePathForFtthumb = cursor.getString(cursor.getColumnIndexOrThrow(ImContract.CsSession.THUMBNAIL_PATH));
        }
        File file = !TextUtils.isEmpty(localFilePathForFtthumb) ? new File(localFilePathForFtthumb) : null;
        Log.i(this.LOG_TAG, "getMcsThumbBody filePath: " + str + " localThumbPath: " + localFilePathForFtthumb + " thumbfile:" + file);
        if (file != null && file.exists()) {
            String fileNameFromPath = FileUtils.getFileNameFromPath(localFilePathForFtthumb);
            if (TextUtils.isEmpty(fileNameFromPath)) {
                fileNameFromPath = Util.getRandomFileName("jpg");
            }
            String str3 = "form-data; name=\"file\"; filename=\"" + fileNameFromPath + CmcConstants.E_NUM_STR_QUOTE;
            String contentType = FileUtils.getContentType(file);
            if (TextUtils.isEmpty(contentType)) {
                contentType = "image/jpeg";
            }
            byte[] fileContentInBytes = getFileContentInBytes(localFilePathForFtthumb, CloudMessageBufferDBConstants.PayloadEncoding.None);
            if (!(fileContentInBytes == null || fileContentInBytes.length == 0 || TextUtils.isEmpty(contentType))) {
                arrayList.add(new HttpPostBody(str3, contentType, fileContentInBytes));
            }
        }
        Log.i(this.LOG_TAG, " ThumbnailFile payload size: " + arrayList.size());
        if (localFilePathForFtthumb != null && !localFilePathForFtthumb.startsWith("content:")) {
            FileUtils.removeFile(localFilePathForFtthumb);
        }
        return arrayList;
    }

    public FileData getLocalFileData(BufferDBChangeParam bufferDBChangeParam) {
        FileData fileData = new FileData();
        IMSLog.i(this.LOG_TAG, "getLocalFileData  ");
        if (bufferDBChangeParam.mDBIndex == 12) {
            Cursor queryrcsMessageBufferDB = queryrcsMessageBufferDB(bufferDBChangeParam.mRowId);
            if (queryrcsMessageBufferDB != null) {
                try {
                    if (queryrcsMessageBufferDB.moveToFirst()) {
                        String string = queryrcsMessageBufferDB.getString(queryrcsMessageBufferDB.getColumnIndex("imdn_message_id"));
                        long ftRowFromTelephony = this.mTeleDBHelper.getFtRowFromTelephony(string);
                        if (ftRowFromTelephony == -1) {
                            Log.e(this.LOG_TAG, "Invalid rowId received for imdn id: " + string);
                            queryrcsMessageBufferDB.close();
                            return null;
                        }
                        Log.i(this.LOG_TAG, "row id : " + ftRowFromTelephony + " for imdn id:" + string);
                        Uri withAppendedId = ContentUris.withAppendedId(Uri.parse("content://im/ft_original/"), ftRowFromTelephony);
                        String string2 = queryrcsMessageBufferDB.getString(queryrcsMessageBufferDB.getColumnIndexOrThrow(ImContract.CsSession.FILE_NAME));
                        String copyFileToCacheFromUri = FileUtils.copyFileToCacheFromUri(this.mContext, string2, withAppendedId);
                        String str = "form-data; name=\"file-part\"; filename=\"" + string2 + CmcConstants.E_NUM_STR_QUOTE;
                        String string3 = queryrcsMessageBufferDB.getString(queryrcsMessageBufferDB.getColumnIndexOrThrow("content_type"));
                        if (TextUtils.isEmpty(copyFileToCacheFromUri)) {
                            copyFileToCacheFromUri = queryrcsMessageBufferDB.getString(queryrcsMessageBufferDB.getColumnIndexOrThrow(ImContract.CsSession.FILE_PATH));
                        }
                        fileData.fileName = string2;
                        fileData.filePath = copyFileToCacheFromUri;
                        fileData.contentType = string3;
                        fileData.contentDisposition = str;
                        queryrcsMessageBufferDB.close();
                        return fileData;
                    }
                } catch (Throwable th) {
                    th.addSuppressed(th);
                }
            }
            if (queryrcsMessageBufferDB != null) {
                queryrcsMessageBufferDB.close();
            }
        }
        return null;
        throw th;
    }

    public Pair<Object, HttpPostBody> getRCSObjectPairFromCursor(BufferDBChangeParam bufferDBChangeParam, List<FileUploadResponse> list) {
        Pair<Object, HttpPostBody> pair;
        Log.i(this.LOG_TAG, "getRCSObjectPairFromCursor ::");
        int i = bufferDBChangeParam.mDBIndex;
        Pair<Object, HttpPostBody> pair2 = null;
        if (i == 1 || i == 12) {
            Cursor queryrcsMessageBufferDB = queryrcsMessageBufferDB(bufferDBChangeParam.mRowId);
            if (queryrcsMessageBufferDB != null) {
                try {
                    if (queryrcsMessageBufferDB.moveToFirst()) {
                        int i2 = queryrcsMessageBufferDB.getInt(queryrcsMessageBufferDB.getColumnIndex(ImContract.ChatItem.IS_FILE_TRANSFER));
                        String str = this.LOG_TAG;
                        Log.i(str, "getRCSObjectPairFromCursor :: isFt: " + i2);
                        if (i2 == 1) {
                            pair = getFtObjectPairFromCursor(queryrcsMessageBufferDB, list);
                        } else {
                            pair = getChatObjectPairFromCursor(queryrcsMessageBufferDB);
                        }
                        pair2 = pair;
                    }
                } catch (Throwable th) {
                    th.addSuppressed(th);
                }
            }
            if (queryrcsMessageBufferDB != null) {
                queryrcsMessageBufferDB.close();
            }
        }
        return pair2;
        throw th;
    }

    /* access modifiers changed from: protected */
    public Pair<Object, HttpPostBody> getObjectPairFromCursor(Cursor cursor, BufferQueryDBTranslation.MessageType messageType) {
        return getObjectPairFromCursor(cursor, messageType, (List<FileUploadResponse>) null);
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:112:0x01d8  */
    /* JADX WARNING: Removed duplicated region for block: B:127:0x01ff A[SYNTHETIC, Splitter:B:127:0x01ff] */
    /* JADX WARNING: Removed duplicated region for block: B:154:0x0262  */
    /* JADX WARNING: Removed duplicated region for block: B:163:0x02b4 A[SYNTHETIC, Splitter:B:163:0x02b4] */
    /* JADX WARNING: Removed duplicated region for block: B:169:0x02cb A[SYNTHETIC, Splitter:B:169:0x02cb] */
    /* JADX WARNING: Removed duplicated region for block: B:177:0x02e2  */
    /* JADX WARNING: Removed duplicated region for block: B:183:0x02fb  */
    /* JADX WARNING: Removed duplicated region for block: B:279:0x0412  */
    /* JADX WARNING: Removed duplicated region for block: B:300:0x04c2 A[Catch:{ all -> 0x04d8, all -> 0x04de }] */
    /* JADX WARNING: Removed duplicated region for block: B:310:0x04f2  */
    /* JADX WARNING: Removed duplicated region for block: B:322:0x054d  */
    /* JADX WARNING: Removed duplicated region for block: B:327:0x04f5 A[SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public android.util.Pair<com.sec.internal.omanetapi.nms.data.Object, com.sec.internal.helper.httpclient.HttpPostBody> getObjectPairFromCursor(android.database.Cursor r25, com.sec.internal.ims.cmstore.omanetapi.bufferdbtranslation.BufferQueryDBTranslation.MessageType r26, java.util.List<com.sec.internal.omanetapi.common.data.FileUploadResponse> r27) {
        /*
            r24 = this;
            r1 = r24
            r2 = r25
            r0 = r26
            java.lang.String r3 = "status"
            java.lang.String r4 = "timestamp"
            java.lang.String r5 = r1.LOG_TAG
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            java.lang.String r7 = "getObjectPairFromCursor type: "
            r6.append(r7)
            r6.append(r0)
            java.lang.String r6 = r6.toString()
            com.sec.internal.log.IMSLog.i(r5, r6)
            r5 = 0
            if (r2 != 0) goto L_0x0026
            return r5
        L_0x0026:
            com.sec.internal.omanetapi.nms.data.Object r6 = new com.sec.internal.omanetapi.nms.data.Object
            r6.<init>()
            com.sec.internal.ims.cmstore.omanetapi.nms.data.AttributeTranslator r7 = new com.sec.internal.ims.cmstore.omanetapi.nms.data.AttributeTranslator
            com.sec.internal.ims.cmstore.MessageStoreClient r8 = r1.mStoreClient
            r7.<init>(r8)
            r8 = 0
            boolean r14 = r25.moveToFirst()     // Catch:{ all -> 0x03e7 }
            if (r14 != 0) goto L_0x004c
            r25.close()     // Catch:{ Exception -> 0x003e }
            return r5
        L_0x003e:
            r0 = move-exception
            r10 = r5
            r11 = r10
            r13 = r11
            r16 = r8
            r22 = r16
            r8 = -1
        L_0x0048:
            r12 = 1
            r14 = 0
            goto L_0x03ff
        L_0x004c:
            java.lang.String r14 = "delivered_timestamp"
            int r14 = r2.getColumnIndex(r14)     // Catch:{ all -> 0x03e7 }
            long r14 = r2.getLong(r14)     // Catch:{ all -> 0x03e7 }
            int r16 = (r14 > r8 ? 1 : (r14 == r8 ? 0 : -1))
            if (r16 != 0) goto L_0x0075
            java.lang.String r12 = "sent_timestamp"
            int r12 = r2.getColumnIndex(r12)     // Catch:{ all -> 0x0066 }
            long r14 = r2.getLong(r12)     // Catch:{ all -> 0x0066 }
            goto L_0x0075
        L_0x0066:
            r0 = move-exception
            r10 = r5
            r11 = r10
            r13 = r11
            r16 = r8
            r22 = r14
            r8 = -1
            r12 = 1
            r14 = 0
            r5 = r0
            goto L_0x03f4
        L_0x0075:
            java.lang.String r12 = "chat_id"
            int r12 = r2.getColumnIndex(r12)     // Catch:{ all -> 0x03d9 }
            java.lang.String r12 = r2.getString(r12)     // Catch:{ all -> 0x03d9 }
            java.lang.String r13 = "res_url"
            int r13 = r2.getColumnIndex(r13)     // Catch:{ all -> 0x03d9 }
            java.lang.String r13 = r2.getString(r13)     // Catch:{ all -> 0x03d9 }
            java.lang.String r8 = "direction"
            int r8 = r2.getColumnIndex(r8)     // Catch:{ all -> 0x03cc }
            int r8 = r2.getInt(r8)     // Catch:{ all -> 0x03cc }
            long r8 = (long) r8
            java.lang.String r5 = "lastmodseq"
            int r5 = r2.getColumnIndex(r5)     // Catch:{ all -> 0x03c3 }
            int r5 = r2.getInt(r5)     // Catch:{ all -> 0x03c3 }
            r19 = r12
            long r11 = (long) r5
            com.sec.internal.omanetapi.nms.data.FlagList r5 = new com.sec.internal.omanetapi.nms.data.FlagList     // Catch:{ all -> 0x03b6 }
            r5.<init>()     // Catch:{ all -> 0x03b6 }
            r6.flags = r5     // Catch:{ all -> 0x03b6 }
            int r5 = r2.getColumnIndex(r3)     // Catch:{ all -> 0x03b6 }
            int r5 = r2.getInt(r5)     // Catch:{ all -> 0x03b6 }
            java.lang.String r10 = "ft_status"
            int r10 = r2.getColumnIndex(r10)     // Catch:{ all -> 0x03b6 }
            int r10 = r2.getInt(r10)     // Catch:{ all -> 0x03b6 }
            r16 = r11
            java.lang.String r11 = r1.LOG_TAG     // Catch:{ all -> 0x03b4 }
            java.lang.StringBuilder r12 = new java.lang.StringBuilder     // Catch:{ all -> 0x03b4 }
            r12.<init>()     // Catch:{ all -> 0x03b4 }
            r20 = r13
            java.lang.String r13 = " status: "
            r12.append(r13)     // Catch:{ all -> 0x03ab }
            r12.append(r5)     // Catch:{ all -> 0x03ab }
            java.lang.String r13 = " direction: "
            r12.append(r13)     // Catch:{ all -> 0x03ab }
            r12.append(r8)     // Catch:{ all -> 0x03ab }
            java.lang.String r13 = " readFt: "
            r12.append(r13)     // Catch:{ all -> 0x03ab }
            r12.append(r10)     // Catch:{ all -> 0x03ab }
            java.lang.String r12 = r12.toString()     // Catch:{ all -> 0x03ab }
            com.sec.internal.log.IMSLog.d(r11, r12)     // Catch:{ all -> 0x03ab }
            com.sec.internal.constants.ims.servicemodules.im.ImConstants$Status r11 = com.sec.internal.constants.ims.servicemodules.im.ImConstants.Status.CANCELLATION     // Catch:{ all -> 0x03ab }
            int r12 = r11.getId()     // Catch:{ all -> 0x03ab }
            java.lang.String r13 = "\\Seen"
            java.lang.String r21 = "\\Canceled"
            if (r5 != r12) goto L_0x012b
            com.sec.internal.constants.ims.servicemodules.im.ImDirection r12 = com.sec.internal.constants.ims.servicemodules.im.ImDirection.INCOMING     // Catch:{ all -> 0x011f }
            int r12 = r12.getId()     // Catch:{ all -> 0x011f }
            r22 = r14
            long r14 = (long) r12
            int r12 = (r8 > r14 ? 1 : (r8 == r14 ? 0 : -1))
            if (r12 != 0) goto L_0x012d
            com.sec.internal.omanetapi.nms.data.FlagList r5 = r6.flags     // Catch:{ all -> 0x0168 }
            r10 = 2
            java.lang.String[] r10 = new java.lang.String[r10]     // Catch:{ all -> 0x0168 }
            r11 = 0
            r10[r11] = r21     // Catch:{ all -> 0x0115 }
            r11 = 1
            r10[r11] = r13     // Catch:{ all -> 0x010d }
            r5.flag = r10     // Catch:{ all -> 0x0168 }
            goto L_0x0174
        L_0x010d:
            r0 = move-exception
            r5 = r0
            r12 = r11
        L_0x0110:
            r13 = r20
            r10 = 0
            r11 = 0
            goto L_0x0128
        L_0x0115:
            r0 = move-exception
            r5 = r0
            r14 = r11
        L_0x0118:
            r13 = r20
            r10 = 0
            r11 = 0
        L_0x011c:
            r12 = 1
            goto L_0x03f4
        L_0x011f:
            r0 = move-exception
            r22 = r14
        L_0x0122:
            r5 = r0
            r13 = r20
            r10 = 0
            r11 = 0
        L_0x0127:
            r12 = 1
        L_0x0128:
            r14 = 0
            goto L_0x03f4
        L_0x012b:
            r22 = r14
        L_0x012d:
            int r11 = r11.getId()     // Catch:{ all -> 0x03a9 }
            if (r5 == r11) goto L_0x016a
            com.sec.internal.constants.ims.servicemodules.im.ImConstants$Status r11 = com.sec.internal.constants.ims.servicemodules.im.ImConstants.Status.CANCELLATION_UNREAD     // Catch:{ all -> 0x0168 }
            int r11 = r11.getId()     // Catch:{ all -> 0x0168 }
            if (r5 != r11) goto L_0x013c
            goto L_0x016a
        L_0x013c:
            com.sec.internal.constants.ims.servicemodules.im.ImConstants$Status r11 = com.sec.internal.constants.ims.servicemodules.im.ImConstants.Status.READ     // Catch:{ all -> 0x0168 }
            int r12 = r11.getId()     // Catch:{ all -> 0x0168 }
            if (r5 == r12) goto L_0x0155
            int r5 = r11.getId()     // Catch:{ all -> 0x0168 }
            if (r10 == r5) goto L_0x0155
            com.sec.internal.constants.ims.servicemodules.im.ImDirection r5 = com.sec.internal.constants.ims.servicemodules.im.ImDirection.OUTGOING     // Catch:{ all -> 0x0168 }
            int r5 = r5.getId()     // Catch:{ all -> 0x0168 }
            long r10 = (long) r5     // Catch:{ all -> 0x0168 }
            int r5 = (r8 > r10 ? 1 : (r8 == r10 ? 0 : -1))
            if (r5 != 0) goto L_0x0174
        L_0x0155:
            com.sec.internal.omanetapi.nms.data.FlagList r5 = r6.flags     // Catch:{ all -> 0x0168 }
            r10 = 1
            java.lang.String[] r11 = new java.lang.String[r10]     // Catch:{ all -> 0x0164 }
            r10 = 0
            r11[r10] = r13     // Catch:{ all -> 0x0160 }
            r5.flag = r11     // Catch:{ all -> 0x0168 }
            goto L_0x0174
        L_0x0160:
            r0 = move-exception
            r5 = r0
            r14 = r10
            goto L_0x0118
        L_0x0164:
            r0 = move-exception
            r5 = r0
            r12 = r10
            goto L_0x0110
        L_0x0168:
            r0 = move-exception
            goto L_0x0122
        L_0x016a:
            com.sec.internal.omanetapi.nms.data.FlagList r5 = r6.flags     // Catch:{ all -> 0x03a9 }
            r10 = 1
            java.lang.String[] r11 = new java.lang.String[r10]     // Catch:{ all -> 0x03a6 }
            r10 = 0
            r11[r10] = r21     // Catch:{ all -> 0x03a2 }
            r5.flag = r11     // Catch:{ all -> 0x03a9 }
        L_0x0174:
            java.text.SimpleDateFormat r5 = r1.sFormatOfName     // Catch:{ all -> 0x03a9 }
            java.util.Date r10 = new java.util.Date     // Catch:{ all -> 0x03a9 }
            java.lang.String r11 = "inserted_timestamp"
            int r11 = r2.getColumnIndex(r11)     // Catch:{ all -> 0x03a9 }
            long r11 = r2.getLong(r11)     // Catch:{ all -> 0x03a9 }
            r10.<init>(r11)     // Catch:{ all -> 0x03a9 }
            java.lang.String r5 = r5.format(r10)     // Catch:{ all -> 0x03a9 }
            java.lang.String[] r10 = new java.lang.String[]{r5}     // Catch:{ all -> 0x03a9 }
            r7.setDate(r10)     // Catch:{ all -> 0x03a9 }
            java.lang.String r10 = r1.LOG_TAG     // Catch:{ all -> 0x03a9 }
            java.lang.StringBuilder r11 = new java.lang.StringBuilder     // Catch:{ all -> 0x03a9 }
            r11.<init>()     // Catch:{ all -> 0x03a9 }
            java.lang.String r12 = "getObjectPairFromCursor :: direction : "
            r11.append(r12)     // Catch:{ all -> 0x03a9 }
            r11.append(r8)     // Catch:{ all -> 0x03a9 }
            java.lang.String r12 = " messagetype : "
            r11.append(r12)     // Catch:{ all -> 0x03a9 }
            r11.append(r0)     // Catch:{ all -> 0x03a9 }
            java.lang.String r12 = " date : "
            r11.append(r12)     // Catch:{ all -> 0x03a9 }
            r11.append(r5)     // Catch:{ all -> 0x03a9 }
            java.lang.String r5 = r11.toString()     // Catch:{ all -> 0x03a9 }
            android.util.Log.i(r10, r5)     // Catch:{ all -> 0x03a9 }
            java.lang.String r5 = "content_type"
            int r5 = r2.getColumnIndex(r5)     // Catch:{ all -> 0x03a9 }
            java.lang.String r5 = r2.getString(r5)     // Catch:{ all -> 0x03a9 }
            r10 = r19
            java.util.Set r11 = r1.getTelAddrFromParticipantTable(r10)     // Catch:{ all -> 0x03a9 }
            r1.setCpmTransMessage(r7, r11, r0, r5)     // Catch:{ all -> 0x0399 }
            boolean r10 = r1.setInformationFromSession(r7, r10, r8)     // Catch:{ all -> 0x0399 }
            com.sec.internal.constants.ims.servicemodules.im.ImDirection r12 = com.sec.internal.constants.ims.servicemodules.im.ImDirection.INCOMING     // Catch:{ all -> 0x0399 }
            int r12 = r12.getId()     // Catch:{ all -> 0x0399 }
            long r12 = (long) r12
            int r12 = (r8 > r12 ? 1 : (r8 == r12 ? 0 : -1))
            if (r12 != 0) goto L_0x01ff
            java.lang.String r10 = "remote_uri"
            int r10 = r2.getColumnIndex(r10)     // Catch:{ all -> 0x01f8 }
            java.lang.String r10 = r2.getString(r10)     // Catch:{ all -> 0x01f8 }
            if (r10 != 0) goto L_0x01f4
            com.sec.internal.ims.cmstore.omanetapi.bufferdbtranslation.BufferQueryDBTranslation$MessageType r12 = com.sec.internal.ims.cmstore.omanetapi.bufferdbtranslation.BufferQueryDBTranslation.MessageType.MESSAGE_CHAT     // Catch:{ all -> 0x01f8 }
            if (r0 != r12) goto L_0x01f4
            r25.close()     // Catch:{ Exception -> 0x01ee }
            r1 = 0
            return r1
        L_0x01ee:
            r0 = move-exception
            r13 = r20
            r10 = 0
            goto L_0x0048
        L_0x01f4:
            r1.setTransToFrom(r7, r10)     // Catch:{ all -> 0x01f8 }
            goto L_0x0251
        L_0x01f8:
            r0 = move-exception
            r5 = r0
            r13 = r20
            r10 = 0
            goto L_0x0127
        L_0x01ff:
            com.sec.internal.constants.ims.servicemodules.im.ImDirection r12 = com.sec.internal.constants.ims.servicemodules.im.ImDirection.OUTGOING     // Catch:{ all -> 0x0399 }
            int r12 = r12.getId()     // Catch:{ all -> 0x0399 }
            long r12 = (long) r12
            int r12 = (r8 > r12 ? 1 : (r8 == r12 ? 0 : -1))
            if (r12 != 0) goto L_0x038c
            r12 = 1
            java.lang.String[] r13 = new java.lang.String[r12]     // Catch:{ all -> 0x038a }
            java.lang.String r12 = "Out"
            r14 = 0
            r13[r14] = r12     // Catch:{ all -> 0x0387 }
            r7.setDirection(r13)     // Catch:{ all -> 0x0399 }
            if (r10 != 0) goto L_0x0226
            int r10 = r11.size()     // Catch:{ all -> 0x01f8 }
            java.lang.String[] r10 = new java.lang.String[r10]     // Catch:{ all -> 0x01f8 }
            java.lang.Object[] r10 = r11.toArray(r10)     // Catch:{ all -> 0x01f8 }
            java.lang.String[] r10 = (java.lang.String[]) r10     // Catch:{ all -> 0x01f8 }
            r7.setTo(r10)     // Catch:{ all -> 0x01f8 }
        L_0x0226:
            r10 = 1
            java.lang.String[] r12 = new java.lang.String[r10]     // Catch:{ all -> 0x0384 }
            java.lang.StringBuilder r10 = new java.lang.StringBuilder     // Catch:{ all -> 0x0399 }
            r10.<init>()     // Catch:{ all -> 0x0399 }
            java.lang.String r13 = "tel:"
            r10.append(r13)     // Catch:{ all -> 0x0399 }
            com.sec.internal.ims.cmstore.MessageStoreClient r13 = r1.mStoreClient     // Catch:{ all -> 0x0399 }
            com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager r13 = r13.getPrerenceManager()     // Catch:{ all -> 0x0399 }
            java.lang.String r13 = r13.getUserCtn()     // Catch:{ all -> 0x0399 }
            java.lang.String r14 = "KR"
            java.lang.String r13 = r1.getE164FormatNumber(r13, r14)     // Catch:{ all -> 0x0399 }
            r10.append(r13)     // Catch:{ all -> 0x0399 }
            java.lang.String r10 = r10.toString()     // Catch:{ all -> 0x0399 }
            r13 = 0
            r12[r13] = r10     // Catch:{ all -> 0x0381 }
            r7.setFrom(r12)     // Catch:{ all -> 0x0399 }
        L_0x0251:
            java.lang.String r10 = "reference_type"
            int r10 = r2.getColumnIndexOrThrow(r10)     // Catch:{ all -> 0x0399 }
            java.lang.String r10 = r2.getString(r10)     // Catch:{ all -> 0x0399 }
            boolean r12 = android.text.TextUtils.isEmpty(r10)     // Catch:{ all -> 0x0399 }
            if (r12 != 0) goto L_0x02a3
            java.lang.String r12 = "0"
            boolean r12 = android.text.TextUtils.equals(r10, r12)     // Catch:{ all -> 0x01f8 }
            if (r12 != 0) goto L_0x02a3
            com.sec.internal.omanetapi.nms.data.ExtendedRCS r12 = new com.sec.internal.omanetapi.nms.data.ExtendedRCS     // Catch:{ all -> 0x01f8 }
            r12.<init>()     // Catch:{ all -> 0x01f8 }
            com.google.gson.Gson r13 = new com.google.gson.Gson     // Catch:{ all -> 0x01f8 }
            r13.<init>()     // Catch:{ all -> 0x01f8 }
            java.lang.String r14 = "reference_id"
            int r14 = r2.getColumnIndexOrThrow(r14)     // Catch:{ all -> 0x01f8 }
            java.lang.String r14 = r2.getString(r14)     // Catch:{ all -> 0x01f8 }
            r12.mReferenceId = r14     // Catch:{ all -> 0x01f8 }
            java.lang.Integer r10 = java.lang.Integer.valueOf(r10)     // Catch:{ all -> 0x01f8 }
            int r10 = r10.intValue()     // Catch:{ all -> 0x01f8 }
            r12.mReferenceType = r10     // Catch:{ all -> 0x01f8 }
            java.lang.String r10 = "reference_value"
            int r10 = r2.getColumnIndexOrThrow(r10)     // Catch:{ all -> 0x01f8 }
            java.lang.String r10 = r2.getString(r10)     // Catch:{ all -> 0x01f8 }
            r12.mReferenceValue = r10     // Catch:{ all -> 0x01f8 }
            java.lang.String r10 = r13.toJson(r12)     // Catch:{ all -> 0x01f8 }
            java.lang.String[] r10 = new java.lang.String[]{r10}     // Catch:{ all -> 0x01f8 }
            r7.setExtendedRCS(r10)     // Catch:{ all -> 0x01f8 }
        L_0x02a3:
            java.lang.String r10 = "suggestion"
            int r10 = r2.getColumnIndexOrThrow(r10)     // Catch:{ all -> 0x0399 }
            java.lang.String r10 = r2.getString(r10)     // Catch:{ all -> 0x0399 }
            boolean r12 = android.text.TextUtils.isEmpty(r10)     // Catch:{ all -> 0x0399 }
            if (r12 != 0) goto L_0x02bb
            java.lang.String[] r10 = new java.lang.String[]{r10}     // Catch:{ all -> 0x01f8 }
            r7.setChipList(r10)     // Catch:{ all -> 0x01f8 }
        L_0x02bb:
            java.lang.String r10 = "maap_traffic_type"
            int r10 = r2.getColumnIndexOrThrow(r10)     // Catch:{ all -> 0x0399 }
            java.lang.String r10 = r2.getString(r10)     // Catch:{ all -> 0x0399 }
            boolean r12 = android.text.TextUtils.isEmpty(r10)     // Catch:{ all -> 0x0399 }
            if (r12 != 0) goto L_0x02d2
            java.lang.String[] r10 = new java.lang.String[]{r10}     // Catch:{ all -> 0x01f8 }
            r7.setTrafficType(r10)     // Catch:{ all -> 0x01f8 }
        L_0x02d2:
            java.lang.String r10 = "imdn_message_id"
            int r10 = r2.getColumnIndex(r10)     // Catch:{ all -> 0x0399 }
            java.lang.String r10 = r2.getString(r10)     // Catch:{ all -> 0x0399 }
            r6.correlationId = r10     // Catch:{ all -> 0x0379 }
            com.sec.internal.ims.cmstore.omanetapi.bufferdbtranslation.BufferQueryDBTranslation$MessageType r12 = com.sec.internal.ims.cmstore.omanetapi.bufferdbtranslation.BufferQueryDBTranslation.MessageType.MESSAGE_FT     // Catch:{ all -> 0x0379 }
            if (r0 != r12) goto L_0x02fb
            java.lang.String r0 = "playing_length"
            int r0 = r2.getColumnIndex(r0)     // Catch:{ all -> 0x02f5 }
            int r0 = r2.getInt(r0)     // Catch:{ all -> 0x02f5 }
            r5 = r27
            r1.updatePayloadInfo(r6, r5, r0, r7)     // Catch:{ all -> 0x02f5 }
        L_0x02f1:
            r12 = 1
            r14 = 0
            goto L_0x0369
        L_0x02f5:
            r0 = move-exception
            r5 = r0
            r13 = r20
            goto L_0x0127
        L_0x02fb:
            java.lang.String r0 = "body"
            int r0 = r2.getColumnIndex(r0)     // Catch:{ all -> 0x0379 }
            java.lang.String r0 = r2.getString(r0)     // Catch:{ all -> 0x0379 }
            boolean r12 = com.sec.internal.ims.cmstore.utils.Util.isLocationPushContentType(r5)     // Catch:{ all -> 0x0379 }
            if (r12 == 0) goto L_0x033a
            r12 = 1
            java.lang.String[] r5 = new java.lang.String[r12]     // Catch:{ all -> 0x0334 }
            java.lang.String r12 = "application/vnd.gsma.rcspushlocation+xml"
            r13 = 0
            r5[r13] = r12     // Catch:{ all -> 0x032d }
            r7.setContentType(r5)     // Catch:{ all -> 0x02f5 }
            com.sec.internal.ims.servicemodules.gls.GlsXmlParser r5 = new com.sec.internal.ims.servicemodules.gls.GlsXmlParser     // Catch:{ all -> 0x02f5 }
            r5.<init>()     // Catch:{ all -> 0x02f5 }
            boolean r12 = android.text.TextUtils.isEmpty(r0)     // Catch:{ all -> 0x02f5 }
            if (r12 != 0) goto L_0x02f1
            java.lang.String r0 = r5.getGeoJson(r0)     // Catch:{ all -> 0x02f5 }
            java.lang.String[] r0 = new java.lang.String[]{r0}     // Catch:{ all -> 0x02f5 }
            r7.setMessageBody(r0)     // Catch:{ all -> 0x02f5 }
            goto L_0x02f1
        L_0x032d:
            r0 = move-exception
            r5 = r0
            r14 = r13
            r13 = r20
            goto L_0x011c
        L_0x0334:
            r0 = move-exception
            r5 = r0
            r13 = r20
            goto L_0x0128
        L_0x033a:
            boolean r12 = com.sec.internal.ims.cmstore.utils.Util.isBotMessageContentType(r5)     // Catch:{ all -> 0x0379 }
            if (r12 != 0) goto L_0x0359
            boolean r12 = com.sec.internal.ims.cmstore.utils.Util.isBotResponseMessageContentType(r5)     // Catch:{ all -> 0x0379 }
            if (r12 == 0) goto L_0x0347
            goto L_0x0359
        L_0x0347:
            r12 = 1
            java.lang.String[] r5 = new java.lang.String[r12]     // Catch:{ all -> 0x0357 }
            java.lang.String r13 = "text/plain"
            r14 = 0
            r5[r14] = r13     // Catch:{ all -> 0x0377 }
            r7.setContentType(r5)     // Catch:{ all -> 0x0377 }
            r7.setTextContent(r0)     // Catch:{ all -> 0x0377 }
            goto L_0x0369
        L_0x0357:
            r0 = move-exception
            goto L_0x037b
        L_0x0359:
            r12 = 1
            r14 = 0
            java.lang.String[] r5 = new java.lang.String[]{r5}     // Catch:{ all -> 0x0377 }
            r7.setContentType(r5)     // Catch:{ all -> 0x0377 }
            java.lang.String[] r0 = new java.lang.String[]{r0}     // Catch:{ all -> 0x0377 }
            r7.setMessageBody(r0)     // Catch:{ all -> 0x0377 }
        L_0x0369:
            r25.close()     // Catch:{ Exception -> 0x0372 }
            r12 = r16
            r0 = r20
            goto L_0x0405
        L_0x0372:
            r0 = move-exception
            r13 = r20
            goto L_0x03ff
        L_0x0377:
            r0 = move-exception
            goto L_0x037c
        L_0x0379:
            r0 = move-exception
            r12 = 1
        L_0x037b:
            r14 = 0
        L_0x037c:
            r5 = r0
            r13 = r20
            goto L_0x03f4
        L_0x0381:
            r0 = move-exception
            r14 = r13
            goto L_0x0388
        L_0x0384:
            r0 = move-exception
            r12 = r10
            goto L_0x039b
        L_0x0387:
            r0 = move-exception
        L_0x0388:
            r12 = 1
            goto L_0x039c
        L_0x038a:
            r0 = move-exception
            goto L_0x039b
        L_0x038c:
            r12 = 1
            r14 = 0
            r25.close()     // Catch:{ Exception -> 0x0393 }
            r1 = 0
            return r1
        L_0x0393:
            r0 = move-exception
            r13 = r20
            r10 = 0
            goto L_0x03ff
        L_0x0399:
            r0 = move-exception
            r12 = 1
        L_0x039b:
            r14 = 0
        L_0x039c:
            r5 = r0
            r13 = r20
            r10 = 0
            goto L_0x03f4
        L_0x03a2:
            r0 = move-exception
            r14 = r10
            r12 = 1
            goto L_0x03b0
        L_0x03a6:
            r0 = move-exception
            r12 = r10
            goto L_0x03af
        L_0x03a9:
            r0 = move-exception
            goto L_0x03ae
        L_0x03ab:
            r0 = move-exception
            r22 = r14
        L_0x03ae:
            r12 = 1
        L_0x03af:
            r14 = 0
        L_0x03b0:
            r5 = r0
            r13 = r20
            goto L_0x03c0
        L_0x03b4:
            r0 = move-exception
            goto L_0x03b9
        L_0x03b6:
            r0 = move-exception
            r16 = r11
        L_0x03b9:
            r20 = r13
            r22 = r14
            r12 = 1
            r14 = 0
            r5 = r0
        L_0x03c0:
            r10 = 0
            r11 = 0
            goto L_0x03f4
        L_0x03c3:
            r0 = move-exception
            r20 = r13
            r22 = r14
            r12 = 1
            r14 = 0
            r5 = r0
            goto L_0x03d6
        L_0x03cc:
            r0 = move-exception
            r20 = r13
            r22 = r14
            r12 = 1
            r14 = 0
            r5 = r0
            r8 = -1
        L_0x03d6:
            r10 = 0
            r11 = 0
            goto L_0x03e4
        L_0x03d9:
            r0 = move-exception
            r22 = r14
            r12 = 1
            r14 = 0
            r5 = r0
            r8 = -1
            r10 = 0
            r11 = 0
            r13 = 0
        L_0x03e4:
            r16 = 0
            goto L_0x03f4
        L_0x03e7:
            r0 = move-exception
            r12 = 1
            r14 = 0
            r5 = r0
            r8 = -1
            r10 = 0
            r11 = 0
            r13 = 0
            r16 = 0
            r22 = 0
        L_0x03f4:
            r25.close()     // Catch:{ all -> 0x03f8 }
            goto L_0x03fd
        L_0x03f8:
            r0 = move-exception
            r2 = r0
            r5.addSuppressed(r2)     // Catch:{ Exception -> 0x03fe }
        L_0x03fd:
            throw r5     // Catch:{ Exception -> 0x03fe }
        L_0x03fe:
            r0 = move-exception
        L_0x03ff:
            r0.printStackTrace()
            r0 = r13
            r12 = r16
        L_0x0405:
            if (r11 == 0) goto L_0x054d
            com.sec.internal.constants.ims.servicemodules.im.ImDirection r2 = com.sec.internal.constants.ims.servicemodules.im.ImDirection.OUTGOING
            int r2 = r2.getId()
            long r14 = (long) r2
            int r2 = (r8 > r14 ? 1 : (r8 == r14 ? 0 : -1))
            if (r2 != 0) goto L_0x054d
            com.sec.internal.omanetapi.nms.data.ImdnList r2 = new com.sec.internal.omanetapi.nms.data.ImdnList
            r2.<init>()
            int r5 = r11.size()
            java.lang.String[] r8 = new java.lang.String[r5]
            java.lang.Object[] r8 = r11.toArray(r8)
            java.lang.String[] r8 = (java.lang.String[]) r8
            java.util.ArrayList r9 = new java.util.ArrayList
            r9.<init>()
            r11 = 0
        L_0x0429:
            if (r11 >= r5) goto L_0x0505
            r14 = r8[r11]
            android.database.Cursor r14 = r1.queryRCSNotificationDBUsingImdnAndTelUri(r10, r14)
            if (r14 == 0) goto L_0x04e4
            boolean r15 = r14.moveToFirst()     // Catch:{ all -> 0x04d8 }
            if (r15 == 0) goto L_0x04e4
            com.sec.internal.omanetapi.nms.data.ImdnObject r15 = new com.sec.internal.omanetapi.nms.data.ImdnObject     // Catch:{ all -> 0x04d8 }
            r15.<init>()     // Catch:{ all -> 0x04d8 }
            r25 = r5
            java.util.ArrayList r5 = new java.util.ArrayList     // Catch:{ all -> 0x04d8 }
            r5.<init>()     // Catch:{ all -> 0x04d8 }
            r16 = r10
            int r10 = r14.getColumnIndex(r4)     // Catch:{ all -> 0x04d8 }
            long r20 = r14.getLong(r10)     // Catch:{ all -> 0x04d8 }
            r17 = 0
            int r10 = (r20 > r17 ? 1 : (r20 == r17 ? 0 : -1))
            if (r10 == 0) goto L_0x04b7
            int r10 = r14.getColumnIndex(r3)     // Catch:{ all -> 0x04d8 }
            int r10 = r14.getInt(r10)     // Catch:{ all -> 0x04d8 }
            r17 = r3
            java.text.SimpleDateFormat r3 = r1.sFormatOfName     // Catch:{ all -> 0x04d8 }
            r19 = r7
            int r7 = r14.getColumnIndex(r4)     // Catch:{ all -> 0x04d8 }
            long r20 = r14.getLong(r7)     // Catch:{ all -> 0x04d8 }
            java.lang.Long r7 = java.lang.Long.valueOf(r20)     // Catch:{ all -> 0x04d8 }
            java.lang.String r3 = r3.format(r7)     // Catch:{ all -> 0x04d8 }
            com.sec.internal.omanetapi.nms.data.ImdnInfo r7 = new com.sec.internal.omanetapi.nms.data.ImdnInfo     // Catch:{ all -> 0x04d8 }
            r7.<init>()     // Catch:{ all -> 0x04d8 }
            com.sec.internal.constants.ims.servicemodules.im.NotificationStatus r20 = com.sec.internal.constants.ims.servicemodules.im.NotificationStatus.DELIVERED     // Catch:{ all -> 0x04d8 }
            r21 = r4
            int r4 = r20.getId()     // Catch:{ all -> 0x04d8 }
            r20 = r6
            java.lang.String r6 = "delivered"
            if (r4 != r10) goto L_0x048f
            r7.date = r3     // Catch:{ all -> 0x04d8 }
            r7.type = r6     // Catch:{ all -> 0x04d8 }
            r5.add(r7)     // Catch:{ all -> 0x04d8 }
        L_0x048d:
            r3 = 1
            goto L_0x04c0
        L_0x048f:
            com.sec.internal.constants.ims.servicemodules.im.NotificationStatus r4 = com.sec.internal.constants.ims.servicemodules.im.NotificationStatus.DISPLAYED     // Catch:{ all -> 0x04d8 }
            int r4 = r4.getId()     // Catch:{ all -> 0x04d8 }
            if (r4 != r10) goto L_0x04bf
            java.text.SimpleDateFormat r4 = r1.sFormatOfName     // Catch:{ all -> 0x04d8 }
            java.lang.Long r10 = java.lang.Long.valueOf(r22)     // Catch:{ all -> 0x04d8 }
            java.lang.String r4 = r4.format(r10)     // Catch:{ all -> 0x04d8 }
            r7.date = r4     // Catch:{ all -> 0x04d8 }
            r7.type = r6     // Catch:{ all -> 0x04d8 }
            r5.add(r7)     // Catch:{ all -> 0x04d8 }
            com.sec.internal.omanetapi.nms.data.ImdnInfo r4 = new com.sec.internal.omanetapi.nms.data.ImdnInfo     // Catch:{ all -> 0x04d8 }
            r4.<init>()     // Catch:{ all -> 0x04d8 }
            r4.date = r3     // Catch:{ all -> 0x04d8 }
            java.lang.String r3 = "displayed"
            r4.type = r3     // Catch:{ all -> 0x04d8 }
            r5.add(r4)     // Catch:{ all -> 0x04d8 }
            goto L_0x048d
        L_0x04b7:
            r17 = r3
            r21 = r4
            r20 = r6
            r19 = r7
        L_0x04bf:
            r3 = 0
        L_0x04c0:
            if (r3 <= 0) goto L_0x04f0
            int r3 = r5.size()     // Catch:{ all -> 0x04d8 }
            com.sec.internal.omanetapi.nms.data.ImdnInfo[] r3 = new com.sec.internal.omanetapi.nms.data.ImdnInfo[r3]     // Catch:{ all -> 0x04d8 }
            java.lang.Object[] r3 = r5.toArray(r3)     // Catch:{ all -> 0x04d8 }
            com.sec.internal.omanetapi.nms.data.ImdnInfo[] r3 = (com.sec.internal.omanetapi.nms.data.ImdnInfo[]) r3     // Catch:{ all -> 0x04d8 }
            r15.imdnInfo = r3     // Catch:{ all -> 0x04d8 }
            r3 = r8[r11]     // Catch:{ all -> 0x04d8 }
            r15.originalTo = r3     // Catch:{ all -> 0x04d8 }
            r9.add(r15)     // Catch:{ all -> 0x04d8 }
            goto L_0x04f0
        L_0x04d8:
            r0 = move-exception
            r1 = r0
            r14.close()     // Catch:{ all -> 0x04de }
            goto L_0x04e3
        L_0x04de:
            r0 = move-exception
            r2 = r0
            r1.addSuppressed(r2)
        L_0x04e3:
            throw r1
        L_0x04e4:
            r17 = r3
            r21 = r4
            r25 = r5
            r20 = r6
            r19 = r7
            r16 = r10
        L_0x04f0:
            if (r14 == 0) goto L_0x04f5
            r14.close()
        L_0x04f5:
            int r11 = r11 + 1
            r5 = r25
            r10 = r16
            r3 = r17
            r7 = r19
            r6 = r20
            r4 = r21
            goto L_0x0429
        L_0x0505:
            r20 = r6
            r19 = r7
            boolean r3 = r9.isEmpty()
            if (r3 != 0) goto L_0x0543
            boolean r1 = android.text.TextUtils.isEmpty(r0)
            if (r1 != 0) goto L_0x0528
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            r1.append(r0)
            java.lang.String r0 = "/imdns"
            r1.append(r0)
            java.lang.String r0 = r1.toString()
            r2.resourceURL = r0
        L_0x0528:
            r3 = 0
            int r0 = (r12 > r3 ? 1 : (r12 == r3 ? 0 : -1))
            if (r0 <= 0) goto L_0x0530
            r2.lastModSeq = r12
        L_0x0530:
            int r0 = r9.size()
            com.sec.internal.omanetapi.nms.data.ImdnObject[] r0 = new com.sec.internal.omanetapi.nms.data.ImdnObject[r0]
            java.lang.Object[] r0 = r9.toArray(r0)
            com.sec.internal.omanetapi.nms.data.ImdnObject[] r0 = (com.sec.internal.omanetapi.nms.data.ImdnObject[]) r0
            r2.imdn = r0
            r3 = r20
            r3.imdns = r2
            goto L_0x0550
        L_0x0543:
            r3 = r20
            java.lang.String r0 = r1.LOG_TAG
            java.lang.String r1 = "getObjectPairFromCursor ImdnObjectList.isEmpty() and not added to object"
            android.util.Log.i(r0, r1)
            goto L_0x0550
        L_0x054d:
            r3 = r6
            r19 = r7
        L_0x0550:
            com.sec.internal.omanetapi.nms.data.AttributeList r0 = r19.getAttributeList()
            r3.attributes = r0
            android.util.Pair r0 = new android.util.Pair
            r1 = 0
            r0.<init>(r3, r1)
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.omanetapi.bufferdbtranslation.BufferDBTranslationMcs.getObjectPairFromCursor(android.database.Cursor, com.sec.internal.ims.cmstore.omanetapi.bufferdbtranslation.BufferQueryDBTranslation$MessageType, java.util.List):android.util.Pair");
    }

    private void updatePayloadInfo(Object object, List<FileUploadResponse> list, int i, AttributeTranslator attributeTranslator) {
        IMSLog.i(this.LOG_TAG, "updatePayloadInfo ");
        if (list.isEmpty()) {
            IMSLog.e(this.LOG_TAG, "empty response data");
            return;
        }
        ArrayList arrayList = new ArrayList();
        FileUploadResponse fileUploadResponse = null;
        FileUploadResponse fileUploadResponse2 = (list.size() <= 1 || list.get(1) == null) ? null : list.get(1);
        if (list.size() > 0 && list.get(0) != null) {
            fileUploadResponse = list.get(0);
        }
        String str = i > 0 ? "render" : HttpPostBody.CONTENT_DISPOSITION_ATTACHMENT;
        if (fileUploadResponse2 != null) {
            IMSLog.i(this.LOG_TAG, "updatePayloadInfo fullpart updation");
            PayloadPartInfo payloadPartInfo = new PayloadPartInfo();
            payloadPartInfo.contentType = fileUploadResponse2.contentType;
            String str2 = fileUploadResponse2.fileName;
            payloadPartInfo.contentDisposition = "attachment;filename=" + str2;
            payloadPartInfo.disposition = str;
            payloadPartInfo.playingLength = i;
            payloadPartInfo.size = (long) fileUploadResponse2.size;
            try {
                payloadPartInfo.href = fileUploadResponse2.href;
                payloadPartInfo.fileIcon = new URI("cid:thumbnail_1");
            } catch (Exception e) {
                String str3 = this.LOG_TAG;
                IMSLog.e(str3, "File URL or URI exception, msg: " + e.getMessage());
            }
            arrayList.add(payloadPartInfo);
            attributeTranslator.setContentType(new String[]{fileUploadResponse2.contentType});
        }
        if (fileUploadResponse != null) {
            IMSLog.i(this.LOG_TAG, "updatePayloadInfo thumbPart updation");
            PayloadPartInfo payloadPartInfo2 = new PayloadPartInfo();
            payloadPartInfo2.contentType = fileUploadResponse.contentType;
            payloadPartInfo2.contentDisposition = "icon";
            payloadPartInfo2.disposition = str;
            payloadPartInfo2.contentId = "thumbnail_1";
            payloadPartInfo2.size = (long) fileUploadResponse.size;
            try {
                payloadPartInfo2.href = fileUploadResponse.href;
            } catch (Exception e2) {
                String str4 = this.LOG_TAG;
                IMSLog.e(str4, "Thumbs URL or URI exception, msg: " + e2.getMessage());
            }
            arrayList.add(payloadPartInfo2);
        }
        object.payloadPart = new PayloadPartInfo[arrayList.size()];
        for (int i2 = 0; i2 < arrayList.size(); i2++) {
            object.payloadPart[i2] = (PayloadPartInfo) arrayList.get(i2);
        }
    }

    /* access modifiers changed from: protected */
    public void setTransToFrom(AttributeTranslator attributeTranslator, String str) {
        attributeTranslator.setDirection(new String[]{"In"});
        attributeTranslator.setFrom(new String[]{str});
        attributeTranslator.setTo(new String[]{this.mStoreClient.getPrerenceManager().getUserTelCtn()});
    }

    /* access modifiers changed from: protected */
    public boolean setInformationFromSession(AttributeTranslator attributeTranslator, String str, long j) {
        boolean z = false;
        if (!TextUtils.isEmpty(str)) {
            Cursor queryRCSSessionDB = queryRCSSessionDB(str);
            if (queryRCSSessionDB != null) {
                try {
                    if (queryRCSSessionDB.moveToFirst()) {
                        boolean z2 = queryRCSSessionDB.getInt(queryRCSSessionDB.getColumnIndexOrThrow("is_group_chat")) == 1;
                        String string = queryRCSSessionDB.getString(queryRCSSessionDB.getColumnIndex("conversation_id"));
                        String str2 = this.LOG_TAG;
                        Log.i(str2, "getObjectPairFromCursor :: conversationId : " + string);
                        attributeTranslator.setConversationId(new String[]{string});
                        if (z2) {
                            String string2 = queryRCSSessionDB.getString(queryRCSSessionDB.getColumnIndex("session_uri"));
                            String str3 = this.LOG_TAG;
                            Log.d(str3, "setGroupSessionURItoTO :: session_uri : " + string2);
                            if (j == ((long) ImDirection.OUTGOING.getId())) {
                                attributeTranslator.setTo(new String[]{string2});
                            }
                            attributeTranslator.setPAssertedService(new String[]{"urn:urn-7:3gpp-service.ims.icsi.oma.cpm.session.group"});
                        } else {
                            attributeTranslator.setPAssertedService(new String[]{"urn:urn-7:3gpp-service.ims.icsi.oma.cpm.session"});
                        }
                        z = z2;
                    }
                } catch (Throwable th) {
                    th.addSuppressed(th);
                }
            }
            if (queryRCSSessionDB != null) {
                queryRCSSessionDB.close();
            }
        }
        return z;
        throw th;
    }

    /* JADX WARNING: Removed duplicated region for block: B:24:0x006f  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private java.lang.String[] getAllToAddress(long r7, boolean r9) {
        /*
            r6 = this;
            android.database.Cursor r7 = r6.queryGroupSMS(r7)
            if (r7 == 0) goto L_0x006c
            int r8 = r7.getCount()     // Catch:{ all -> 0x0062 }
            if (r8 == 0) goto L_0x006c
            int r8 = r7.getCount()     // Catch:{ all -> 0x0062 }
            java.lang.String[] r0 = new java.lang.String[r8]     // Catch:{ all -> 0x0062 }
            java.lang.String r1 = r6.LOG_TAG     // Catch:{ all -> 0x0062 }
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ all -> 0x0062 }
            r2.<init>()     // Catch:{ all -> 0x0062 }
            java.lang.String r3 = "getAllToAddress added address:"
            r2.append(r3)     // Catch:{ all -> 0x0062 }
            r2.append(r8)     // Catch:{ all -> 0x0062 }
            java.lang.String r8 = r2.toString()     // Catch:{ all -> 0x0062 }
            android.util.Log.d(r1, r8)     // Catch:{ all -> 0x0062 }
            r8 = 0
        L_0x0029:
            boolean r1 = r7.moveToNext()     // Catch:{ all -> 0x0062 }
            if (r1 == 0) goto L_0x006d
            java.lang.String r1 = "address"
            int r1 = r7.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x0062 }
            java.lang.String r1 = r7.getString(r1)     // Catch:{ all -> 0x0062 }
            java.lang.String r2 = "KR"
            if (r9 == 0) goto L_0x0047
            int r3 = r8 + 1
            java.lang.String r1 = r6.getE164FormatNumber(r1, r2)     // Catch:{ all -> 0x0062 }
            r0[r8] = r1     // Catch:{ all -> 0x0062 }
        L_0x0045:
            r8 = r3
            goto L_0x0029
        L_0x0047:
            int r3 = r8 + 1
            java.lang.StringBuilder r4 = new java.lang.StringBuilder     // Catch:{ all -> 0x0062 }
            r4.<init>()     // Catch:{ all -> 0x0062 }
            java.lang.String r5 = "tel:"
            r4.append(r5)     // Catch:{ all -> 0x0062 }
            java.lang.String r1 = r6.getE164FormatNumber(r1, r2)     // Catch:{ all -> 0x0062 }
            r4.append(r1)     // Catch:{ all -> 0x0062 }
            java.lang.String r1 = r4.toString()     // Catch:{ all -> 0x0062 }
            r0[r8] = r1     // Catch:{ all -> 0x0062 }
            goto L_0x0045
        L_0x0062:
            r6 = move-exception
            r7.close()     // Catch:{ all -> 0x0067 }
            goto L_0x006b
        L_0x0067:
            r7 = move-exception
            r6.addSuppressed(r7)
        L_0x006b:
            throw r6
        L_0x006c:
            r0 = 0
        L_0x006d:
            if (r7 == 0) goto L_0x0072
            r7.close()
        L_0x0072:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.omanetapi.bufferdbtranslation.BufferDBTranslationMcs.getAllToAddress(long, boolean):java.lang.String[]");
    }

    /* JADX WARNING: Removed duplicated region for block: B:49:0x01a8  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public android.util.Pair<com.sec.internal.omanetapi.nms.data.Object, com.sec.internal.helper.httpclient.HttpPostBody> getSmsObjectPairFromCursor(com.sec.internal.ims.cmstore.params.BufferDBChangeParam r17) {
        /*
            r16 = this;
            r0 = r16
            java.lang.String r1 = "KR"
            java.lang.String r2 = "tel:"
            r3 = r17
            long r3 = r3.mRowId
            android.database.Cursor r3 = r0.querySMSBufferDB(r3)
            r4 = 0
            if (r3 == 0) goto L_0x01a4
            boolean r5 = r3.moveToFirst()     // Catch:{ all -> 0x0198 }
            if (r5 == 0) goto L_0x01a4
            com.sec.internal.omanetapi.nms.data.Object r5 = new com.sec.internal.omanetapi.nms.data.Object     // Catch:{ all -> 0x0198 }
            r5.<init>()     // Catch:{ all -> 0x0198 }
            java.lang.String r6 = "correlation_tag"
            int r6 = r3.getColumnIndex(r6)     // Catch:{ all -> 0x0198 }
            java.lang.String r6 = r3.getString(r6)     // Catch:{ all -> 0x0198 }
            r5.correlationTag = r6     // Catch:{ all -> 0x0198 }
            com.sec.internal.omanetapi.nms.data.FlagList r6 = new com.sec.internal.omanetapi.nms.data.FlagList     // Catch:{ all -> 0x0198 }
            r6.<init>()     // Catch:{ all -> 0x0198 }
            r5.flags = r6     // Catch:{ all -> 0x0198 }
            java.lang.String r6 = "read"
            int r6 = r3.getColumnIndex(r6)     // Catch:{ all -> 0x0198 }
            int r6 = r3.getInt(r6)     // Catch:{ all -> 0x0198 }
            java.lang.String r7 = "type"
            int r7 = r3.getColumnIndex(r7)     // Catch:{ all -> 0x0198 }
            long r7 = r3.getLong(r7)     // Catch:{ all -> 0x0198 }
            r9 = 2
            r11 = 0
            r12 = 1
            if (r6 == r12) goto L_0x0050
            int r6 = (r7 > r9 ? 1 : (r7 == r9 ? 0 : -1))
            if (r6 != 0) goto L_0x005a
        L_0x0050:
            com.sec.internal.omanetapi.nms.data.FlagList r6 = r5.flags     // Catch:{ all -> 0x0198 }
            java.lang.String[] r13 = new java.lang.String[r12]     // Catch:{ all -> 0x0198 }
            java.lang.String r14 = "\\Seen"
            r13[r11] = r14     // Catch:{ all -> 0x0198 }
            r6.flag = r13     // Catch:{ all -> 0x0198 }
        L_0x005a:
            java.lang.String r6 = "address"
            int r6 = r3.getColumnIndexOrThrow(r6)     // Catch:{ all -> 0x0198 }
            java.lang.String r6 = r3.getString(r6)     // Catch:{ all -> 0x0198 }
            java.lang.String[] r13 = new java.lang.String[r12]     // Catch:{ all -> 0x0198 }
            java.lang.StringBuilder r14 = new java.lang.StringBuilder     // Catch:{ all -> 0x0198 }
            r14.<init>()     // Catch:{ all -> 0x0198 }
            r14.append(r2)     // Catch:{ all -> 0x0198 }
            java.lang.String r15 = r0.getE164FormatNumber(r6, r1)     // Catch:{ all -> 0x0198 }
            r14.append(r15)     // Catch:{ all -> 0x0198 }
            java.lang.String r14 = r14.toString()     // Catch:{ all -> 0x0198 }
            r13[r11] = r14     // Catch:{ all -> 0x0198 }
            java.lang.String r14 = "@"
            boolean r14 = r6.contains(r14)     // Catch:{ all -> 0x0198 }
            if (r14 == 0) goto L_0x0087
            java.lang.String[] r13 = new java.lang.String[]{r6}     // Catch:{ all -> 0x0198 }
        L_0x0087:
            java.lang.String r14 = "group_id"
            int r14 = r3.getColumnIndexOrThrow(r14)     // Catch:{ all -> 0x0198 }
            int r14 = r3.getInt(r14)     // Catch:{ all -> 0x0198 }
            if (r14 == 0) goto L_0x00af
            java.lang.String r13 = "hidden"
            int r13 = r3.getColumnIndexOrThrow(r13)     // Catch:{ all -> 0x0198 }
            int r13 = r3.getInt(r13)     // Catch:{ all -> 0x0198 }
            if (r13 != r12) goto L_0x00aa
            java.lang.String r0 = r0.LOG_TAG     // Catch:{ all -> 0x0198 }
            java.lang.String r1 = "getSmsObjectPairFromCursor hidden msg - shouldn't have been added for upload"
            android.util.Log.d(r0, r1)     // Catch:{ all -> 0x0198 }
            r3.close()
            return r4
        L_0x00aa:
            long r13 = (long) r14
            java.lang.String[] r13 = r0.getAllToAddress(r13, r11)     // Catch:{ all -> 0x0198 }
        L_0x00af:
            com.sec.internal.ims.cmstore.omanetapi.nms.data.AttributeTranslator r14 = new com.sec.internal.ims.cmstore.omanetapi.nms.data.AttributeTranslator     // Catch:{ all -> 0x0198 }
            com.sec.internal.ims.cmstore.MessageStoreClient r15 = r0.mStoreClient     // Catch:{ all -> 0x0198 }
            r14.<init>(r15)     // Catch:{ all -> 0x0198 }
            java.text.SimpleDateFormat r15 = r0.sFormatOfName     // Catch:{ all -> 0x0198 }
            java.util.Date r4 = new java.util.Date     // Catch:{ all -> 0x0198 }
            java.lang.String r9 = "date"
            int r9 = r3.getColumnIndex(r9)     // Catch:{ all -> 0x0198 }
            long r9 = r3.getLong(r9)     // Catch:{ all -> 0x0198 }
            r4.<init>(r9)     // Catch:{ all -> 0x0198 }
            java.lang.String r4 = r15.format(r4)     // Catch:{ all -> 0x0198 }
            java.lang.String[] r4 = new java.lang.String[]{r4}     // Catch:{ all -> 0x0198 }
            r14.setDate(r4)     // Catch:{ all -> 0x0198 }
            r9 = 1
            int r4 = (r7 > r9 ? 1 : (r7 == r9 ? 0 : -1))
            if (r4 != 0) goto L_0x011d
            java.lang.String[] r4 = new java.lang.String[r12]     // Catch:{ all -> 0x0198 }
            java.lang.String r7 = "In"
            r4[r11] = r7     // Catch:{ all -> 0x0198 }
            r14.setDirection(r4)     // Catch:{ all -> 0x0198 }
            int r4 = r13.length     // Catch:{ all -> 0x0198 }
            if (r4 != r12) goto L_0x00f5
            boolean r4 = android.text.TextUtils.isEmpty(r6)     // Catch:{ all -> 0x0198 }
            if (r4 == 0) goto L_0x00f5
            java.lang.String[] r4 = new java.lang.String[r12]     // Catch:{ all -> 0x0198 }
            java.lang.String r6 = "unknown_address"
            r4[r11] = r6     // Catch:{ all -> 0x0198 }
            r14.setFrom(r4)     // Catch:{ all -> 0x0198 }
            goto L_0x00f8
        L_0x00f5:
            r14.setFrom(r13)     // Catch:{ all -> 0x0198 }
        L_0x00f8:
            java.lang.String[] r4 = new java.lang.String[r12]     // Catch:{ all -> 0x0198 }
            java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ all -> 0x0198 }
            r6.<init>()     // Catch:{ all -> 0x0198 }
            r6.append(r2)     // Catch:{ all -> 0x0198 }
            com.sec.internal.ims.cmstore.MessageStoreClient r2 = r0.mStoreClient     // Catch:{ all -> 0x0198 }
            com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager r2 = r2.getPrerenceManager()     // Catch:{ all -> 0x0198 }
            java.lang.String r2 = r2.getUserCtn()     // Catch:{ all -> 0x0198 }
            java.lang.String r0 = r0.getE164FormatNumber(r2, r1)     // Catch:{ all -> 0x0198 }
            r6.append(r0)     // Catch:{ all -> 0x0198 }
            java.lang.String r0 = r6.toString()     // Catch:{ all -> 0x0198 }
            r4[r11] = r0     // Catch:{ all -> 0x0198 }
            r14.setTo(r4)     // Catch:{ all -> 0x0198 }
            goto L_0x0153
        L_0x011d:
            r9 = 2
            int r4 = (r7 > r9 ? 1 : (r7 == r9 ? 0 : -1))
            if (r4 != 0) goto L_0x0153
            java.lang.String[] r4 = new java.lang.String[r12]     // Catch:{ all -> 0x0198 }
            java.lang.String r6 = "Out"
            r4[r11] = r6     // Catch:{ all -> 0x0198 }
            r14.setDirection(r4)     // Catch:{ all -> 0x0198 }
            r14.setTo(r13)     // Catch:{ all -> 0x0198 }
            java.lang.String[] r4 = new java.lang.String[r12]     // Catch:{ all -> 0x0198 }
            java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ all -> 0x0198 }
            r6.<init>()     // Catch:{ all -> 0x0198 }
            r6.append(r2)     // Catch:{ all -> 0x0198 }
            com.sec.internal.ims.cmstore.MessageStoreClient r2 = r0.mStoreClient     // Catch:{ all -> 0x0198 }
            com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager r2 = r2.getPrerenceManager()     // Catch:{ all -> 0x0198 }
            java.lang.String r2 = r2.getUserCtn()     // Catch:{ all -> 0x0198 }
            java.lang.String r0 = r0.getE164FormatNumber(r2, r1)     // Catch:{ all -> 0x0198 }
            r6.append(r0)     // Catch:{ all -> 0x0198 }
            java.lang.String r0 = r6.toString()     // Catch:{ all -> 0x0198 }
            r4[r11] = r0     // Catch:{ all -> 0x0198 }
            r14.setFrom(r4)     // Catch:{ all -> 0x0198 }
        L_0x0153:
            java.lang.String[] r0 = new java.lang.String[r12]     // Catch:{ all -> 0x0198 }
            java.lang.String r1 = "pager-message"
            r0[r11] = r1     // Catch:{ all -> 0x0198 }
            r14.setMessageContext(r0)     // Catch:{ all -> 0x0198 }
            java.lang.String[] r0 = new java.lang.String[r12]     // Catch:{ all -> 0x0198 }
            java.lang.String r1 = "text/plain"
            r0[r11] = r1     // Catch:{ all -> 0x0198 }
            r14.setContentType(r0)     // Catch:{ all -> 0x0198 }
            java.lang.String r0 = "body"
            int r0 = r3.getColumnIndex(r0)     // Catch:{ all -> 0x0198 }
            java.lang.String r0 = r3.getString(r0)     // Catch:{ all -> 0x0198 }
            r14.setTextContent(r0)     // Catch:{ all -> 0x0198 }
            java.lang.String r0 = "safe_message"
            int r0 = r3.getColumnIndexOrThrow(r0)     // Catch:{ all -> 0x0198 }
            int r0 = r3.getInt(r0)     // Catch:{ all -> 0x0198 }
            if (r0 != r12) goto L_0x018a
            java.lang.String[] r0 = new java.lang.String[r12]     // Catch:{ all -> 0x0198 }
            java.lang.String r1 = "true"
            r0[r11] = r1     // Catch:{ all -> 0x0198 }
            r14.setSafetyMessage(r0)     // Catch:{ all -> 0x0198 }
        L_0x018a:
            com.sec.internal.omanetapi.nms.data.AttributeList r0 = r14.getAttributeList()     // Catch:{ all -> 0x0198 }
            r5.attributes = r0     // Catch:{ all -> 0x0198 }
            android.util.Pair r0 = new android.util.Pair     // Catch:{ all -> 0x0198 }
            r1 = 0
            r0.<init>(r5, r1)     // Catch:{ all -> 0x0198 }
            r4 = r0
            goto L_0x01a6
        L_0x0198:
            r0 = move-exception
            r1 = r0
            r3.close()     // Catch:{ all -> 0x019e }
            goto L_0x01a3
        L_0x019e:
            r0 = move-exception
            r2 = r0
            r1.addSuppressed(r2)
        L_0x01a3:
            throw r1
        L_0x01a4:
            r1 = r4
            r4 = r1
        L_0x01a6:
            if (r3 == 0) goto L_0x01ab
            r3.close()
        L_0x01ab:
            return r4
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.omanetapi.bufferdbtranslation.BufferDBTranslationMcs.getSmsObjectPairFromCursor(com.sec.internal.ims.cmstore.params.BufferDBChangeParam):android.util.Pair");
    }

    /* access modifiers changed from: protected */
    public Object getMmsObjectFromPduAndAddress(Cursor cursor) {
        Object object = new Object();
        if (cursor != null && cursor.moveToFirst()) {
            object.flags = new FlagList();
            int i = cursor.getInt(cursor.getColumnIndex("read"));
            long j = cursor.getLong(cursor.getColumnIndex(CloudMessageProviderContract.BufferDBMMSpdu.MSG_BOX));
            if (i == 1 || j == 2) {
                object.flags.flag = new String[]{FlagNames.Seen};
            }
            String string = cursor.getString(cursor.getColumnIndex(CloudMessageProviderContract.BufferDBMMSpdu.TR_ID));
            if (!this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isTrIdCorrelationId() || string == null || string.length() <= 2) {
                object.correlationId = cursor.getString(cursor.getColumnIndex(CloudMessageProviderContract.BufferDBMMSpdu.M_ID));
            } else {
                object.correlationId = string.substring(2);
            }
            AttributeTranslator attributeTranslator = new AttributeTranslator(this.mStoreClient);
            attributeTranslator.setDate(new String[]{this.sFormatOfName.format(new Date(cursor.getLong(cursor.getColumnIndex("date"))))});
            String string2 = cursor.getString(cursor.getColumnIndex(CloudMessageProviderContract.BufferDBMMSpdu.SUB));
            if (!TextUtils.isEmpty(string2)) {
                attributeTranslator.setSubject(new String[]{new String(string2.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8)});
            }
            long j2 = cursor.getLong(cursor.getColumnIndex(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID));
            BufferDBSupportTranslation.MmsParticipant addrFromPduId = getAddrFromPduId(j2);
            if (j == 1) {
                attributeTranslator.setDirection(new String[]{"IN"});
                if (addrFromPduId.mFrom.isEmpty()) {
                    attributeTranslator.setFrom(new String[]{"unknown_address"});
                } else {
                    attributeTranslator.setFrom(new String[]{"tel:" + ((String) addrFromPduId.mFrom.stream().findFirst().get())});
                }
                attributeTranslator.setTo(new String[]{getTelE164FormatNumber(this.mStoreClient.getPrerenceManager().getUserCtn(), "KR")});
            } else if (j == 2) {
                attributeTranslator.setDirection(new String[]{"OUT"});
                if (addrFromPduId.mTo.size() != 0) {
                    HashSet hashSet = new HashSet();
                    for (String telE164FormatNumber : addrFromPduId.mTo) {
                        hashSet.add(getTelE164FormatNumber(telE164FormatNumber, "KR"));
                    }
                    attributeTranslator.setTo((String[]) hashSet.toArray(new String[hashSet.size()]));
                }
                attributeTranslator.setFrom(new String[]{getTelE164FormatNumber(this.mStoreClient.getPrerenceManager().getUserCtn(), "KR")});
            }
            attributeTranslator.setMessageContext(new String[]{"multimedia-message"});
            cursor.getInt(cursor.getColumnIndex(CloudMessageProviderContract.BufferDBMMSpdu.TEXT_ONLY));
            StringBuffer stringBuffer = new StringBuffer();
            Cursor queryPartsBufferDBUsingPduBufferId = queryPartsBufferDBUsingPduBufferId(j2);
            if (queryPartsBufferDBUsingPduBufferId != null) {
                try {
                    if (queryPartsBufferDBUsingPduBufferId.moveToFirst()) {
                        do {
                            if (MIMEContentType.PLAIN_TEXT.equalsIgnoreCase(queryPartsBufferDBUsingPduBufferId.getString(queryPartsBufferDBUsingPduBufferId.getColumnIndex(CloudMessageProviderContract.BufferDBMMSpart.CT)))) {
                                stringBuffer.append(queryPartsBufferDBUsingPduBufferId.getString(queryPartsBufferDBUsingPduBufferId.getColumnIndex("text")));
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
            String stringBuffer2 = stringBuffer.toString();
            if (!TextUtils.isEmpty(stringBuffer2)) {
                attributeTranslator.setTextContent(stringBuffer2);
            }
            if (cursor.getInt(cursor.getColumnIndexOrThrow("safe_message")) == 1) {
                attributeTranslator.setSafetyMessage(new String[]{CloudMessageProviderContract.JsonData.TRUE});
            }
            object.attributes = attributeTranslator.getAttributeList();
        }
        if (!TextUtils.isEmpty(object.correlationId)) {
            return object;
        }
        Log.e(this.LOG_TAG, "getMmsObjectFromPduAndAddress: correlation id is empty!!!");
        return null;
        throw th;
    }

    public Pair<Object, HttpPostBody> getMmsObjectPairFromCursor(BufferDBChangeParam bufferDBChangeParam) {
        Cursor querymmsPduBufferDB = querymmsPduBufferDB(bufferDBChangeParam.mRowId);
        try {
            Object mmsObjectFromPduAndAddress = getMmsObjectFromPduAndAddress(querymmsPduBufferDB);
            int i = querymmsPduBufferDB.getInt(querymmsPduBufferDB.getColumnIndex(CloudMessageProviderContract.BufferDBMMSpdu.TEXT_ONLY));
            HttpPostBody mmsPartHttpPayloadFromCursor = getMmsPartHttpPayloadFromCursor(queryPartsBufferDBUsingPduBufferId(bufferDBChangeParam.mRowId));
            querymmsPduBufferDB.close();
            return new Pair<>(mmsObjectFromPduAndAddress, mmsPartHttpPayloadFromCursor);
        } catch (Throwable th) {
            th.addSuppressed(th);
        }
        throw th;
    }

    /* access modifiers changed from: protected */
    public HttpPostBody getMmsPartHttpPayloadFromCursor(Cursor cursor) {
        String str;
        HttpPostBody httpPostBody;
        ArrayList arrayList = new ArrayList();
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    do {
                        String string = cursor.getString(cursor.getColumnIndex("_id"));
                        String string2 = cursor.getString(cursor.getColumnIndex(CloudMessageProviderContract.BufferDBMMSpart.CT));
                        if (!TextUtils.isEmpty(string2)) {
                            if (FileExtensionTranslator.isTranslationDefined(string2)) {
                                String string3 = cursor.getString(cursor.getColumnIndex(CloudMessageProviderContract.BufferDBMMSpart.CL));
                                if (TextUtils.isEmpty(string3)) {
                                    str = cursor.getString(cursor.getColumnIndex("name"));
                                    if (TextUtils.isEmpty(str)) {
                                        str = Util.getRandomFileName(FileExtensionTranslator.translate(string2));
                                    }
                                } else {
                                    str = new String(string3.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
                                }
                                if (!TextUtils.isEmpty(str) && str.lastIndexOf(".") == -1) {
                                    str = str + "." + FileExtensionTranslator.translate(string2);
                                }
                                String str2 = "attachment;filename=\"" + str + CmcConstants.E_NUM_STR_QUOTE;
                                String string4 = cursor.getString(cursor.getColumnIndex(CloudMessageProviderContract.BufferDBMMSpart.CID));
                                if (!TextUtils.isEmpty(string4)) {
                                    string4 = new String(string4.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
                                }
                                if (!ITelephonyDBColumns.xml_smil_type.equalsIgnoreCase(string2)) {
                                    if (!MIMEContentType.PLAIN_TEXT.equalsIgnoreCase(string2)) {
                                        byte[] dataFromPartFile = getDataFromPartFile(Long.parseLong(string));
                                        if (dataFromPartFile != null) {
                                            httpPostBody = new HttpPostBody(str2, string2, dataFromPartFile, string4);
                                            httpPostBody.setContentTransferEncoding(HttpPostBody.CONTENT_TRANSFER_ENCODING_BINARY);
                                            arrayList.add(httpPostBody);
                                        }
                                    }
                                }
                                httpPostBody = new HttpPostBody(str2, string2, cursor.getString(cursor.getColumnIndex("text")), string4);
                                arrayList.add(httpPostBody);
                            }
                        }
                    } while (cursor.moveToNext());
                    if (arrayList.isEmpty()) {
                        cursor.close();
                        return null;
                    }
                    HttpPostBody httpPostBody2 = new HttpPostBody("form-data;name=\"attachments\"", "multipart/mixed", (List<HttpPostBody>) arrayList);
                    cursor.close();
                    return httpPostBody2;
                }
                cursor.close();
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        return null;
        throw th;
    }

    private Set<String> getTelAddrFromParticipantTable(String str) {
        HashSet hashSet = new HashSet();
        Cursor queryRCSParticipantDB = queryRCSParticipantDB(str);
        if (queryRCSParticipantDB != null) {
            try {
                if (queryRCSParticipantDB.moveToFirst()) {
                    do {
                        String string = queryRCSParticipantDB.getString(queryRCSParticipantDB.getColumnIndex("uri"));
                        if (!TextUtils.isEmpty(string)) {
                            hashSet.add(string);
                        }
                    } while (queryRCSParticipantDB.moveToNext());
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (queryRCSParticipantDB != null) {
            queryRCSParticipantDB.close();
        }
        String str2 = this.LOG_TAG;
        Log.i(str2, "getAddrFromParticipantTable : " + IMSLog.checker(hashSet));
        return hashSet;
        throw th;
    }

    public ParamObjectUpload getThumbnailPart(BufferDBChangeParam bufferDBChangeParam) {
        String str = this.LOG_TAG;
        Log.i(str, "getThumbNailPart " + bufferDBChangeParam);
        List arrayList = new ArrayList();
        if (bufferDBChangeParam.mDBIndex == 12) {
            Cursor queryrcsMessageBufferDB = queryrcsMessageBufferDB(bufferDBChangeParam.mRowId);
            if (queryrcsMessageBufferDB != null) {
                try {
                    if (!queryrcsMessageBufferDB.moveToFirst()) {
                        IMSLog.e(this.LOG_TAG, "getAllParts cursor is null, shouldn't occur");
                        queryrcsMessageBufferDB.close();
                        return null;
                    }
                    String string = queryrcsMessageBufferDB.getString(queryrcsMessageBufferDB.getColumnIndexOrThrow("imdn_message_id"));
                    String ftFileDataFromTelephony = this.mTeleDBHelper.getFtFileDataFromTelephony(string, ImContract.CsSession.FILE_PATH);
                    String ftFileDataFromTelephony2 = this.mTeleDBHelper.getFtFileDataFromTelephony(string, ImContract.CsSession.THUMBNAIL_PATH);
                    if (!TextUtils.isEmpty(ftFileDataFromTelephony2)) {
                        arrayList = getMcsThumbBody(queryrcsMessageBufferDB, ftFileDataFromTelephony, ftFileDataFromTelephony2);
                    }
                    if (!arrayList.isEmpty()) {
                        ParamObjectUpload paramObjectUpload = new ParamObjectUpload(new Pair((Object) null, new HttpPostBody((List<HttpPostBody>) arrayList)), bufferDBChangeParam);
                        IMSLog.i(this.LOG_TAG, "thumb body is added!!!!");
                        queryrcsMessageBufferDB.close();
                        return paramObjectUpload;
                    }
                } catch (Throwable th) {
                    th.addSuppressed(th);
                }
            }
            if (queryrcsMessageBufferDB != null) {
                queryrcsMessageBufferDB.close();
            }
        }
        return null;
        throw th;
    }

    /* access modifiers changed from: protected */
    public String getLocalFilePathForFtthumb(Cursor cursor, String str) {
        String string = cursor.getString(cursor.getColumnIndex("imdn_message_id"));
        long ftRowFromTelephony = this.mTeleDBHelper.getFtRowFromTelephony(string);
        if (ftRowFromTelephony == -1) {
            String str2 = this.LOG_TAG;
            Log.e(str2, "getLocalFilePathForFtthumb Invalid rowId received for imdn id: " + string);
            return null;
        }
        String str3 = this.LOG_TAG;
        Log.i(str3, "row id : " + ftRowFromTelephony + " for imdn id:" + string);
        Uri withAppendedId = ContentUris.withAppendedId(Uri.parse("content://im/ft_thumbnail/"), ftRowFromTelephony);
        return FileUtils.copyFileToCacheFromUri(this.mContext, FileUtils.getFileNameFromPath(str), withAppendedId);
    }

    public boolean needToSkipDownloadLargeFileAndUpdateDB(long j, int i, int i2, String str, boolean z) {
        Throwable th;
        Cursor queryrcsMessageBufferDB = queryrcsMessageBufferDB(j);
        if (queryrcsMessageBufferDB != null) {
            try {
                if (queryrcsMessageBufferDB.moveToFirst()) {
                    if (!(queryrcsMessageBufferDB.getInt(queryrcsMessageBufferDB.getColumnIndexOrThrow(ImContract.ChatItem.IS_FILE_TRANSFER)) == 1)) {
                        Log.i(this.LOG_TAG, "needToSkipDownloadLargeFileAndUpdateDB isFt false");
                        queryrcsMessageBufferDB.close();
                        return false;
                    }
                    int i3 = queryrcsMessageBufferDB.getInt(queryrcsMessageBufferDB.getColumnIndexOrThrow("status"));
                    long j2 = queryrcsMessageBufferDB.getLong(queryrcsMessageBufferDB.getColumnIndexOrThrow(ImContract.CsSession.FILE_SIZE));
                    int i4 = queryrcsMessageBufferDB.getInt(queryrcsMessageBufferDB.getColumnIndexOrThrow("direction"));
                    boolean isPayloadExpired = Util.isPayloadExpired(str);
                    boolean isEmpty = TextUtils.isEmpty(queryrcsMessageBufferDB.getString(queryrcsMessageBufferDB.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.PAYLOADPARTTHUMB)));
                    String str2 = this.LOG_TAG;
                    Log.i(str2, "needToSkipDownloadLargeFileAndUpdateDB fileSize: " + j2 + ", isExpired: " + isPayloadExpired + ", isThumbnailNotPresent: " + isEmpty + ", msgStatus = " + i3);
                    if (isPayloadExpired) {
                        updateRcsMessageBufferDB(j, i, i2);
                        queryrcsMessageBufferDB.close();
                        return true;
                    } else if (i3 == ImConstants.Status.CANCELLATION.getId()) {
                        queryrcsMessageBufferDB.close();
                        return true;
                    } else if (z) {
                        queryrcsMessageBufferDB.close();
                        return false;
                    } else if (i4 == ImDirection.INCOMING.getId() && CmsUtil.isLargeSizeFile(this.mStoreClient, j2)) {
                        if (isEmpty) {
                            updateRcsMessageBufferDB(j, i, i2);
                        }
                        queryrcsMessageBufferDB.close();
                        return true;
                    }
                }
            } catch (Throwable th2) {
                th.addSuppressed(th2);
            }
        }
        if (queryrcsMessageBufferDB != null) {
            queryrcsMessageBufferDB.close();
        }
        return false;
        throw th;
    }

    public String getImdnResUrl(long j) {
        Cursor queryRCSMessageDBUsingRowId = queryRCSMessageDBUsingRowId(j);
        if (queryRCSMessageDBUsingRowId != null) {
            try {
                if (queryRCSMessageDBUsingRowId.moveToFirst()) {
                    String string = queryRCSMessageDBUsingRowId.getString(queryRCSMessageDBUsingRowId.getColumnIndex(CloudMessageProviderContract.BufferDBExtensionBase.RES_URL));
                    Log.i(this.LOG_TAG, "getImdnResUrl resUrl: " + IMSLog.numberChecker(string));
                    if (string != null) {
                        string = string + "/imdns";
                    }
                    queryRCSMessageDBUsingRowId.close();
                    return string;
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (queryRCSMessageDBUsingRowId == null) {
            return null;
        }
        queryRCSMessageDBUsingRowId.close();
        return null;
        throw th;
    }

    public boolean isMessageStatusCancelled(long j) {
        Cursor queryRCSMessageDBUsingRowId = queryRCSMessageDBUsingRowId(j);
        if (queryRCSMessageDBUsingRowId != null) {
            try {
                if (queryRCSMessageDBUsingRowId.moveToFirst()) {
                    int i = queryRCSMessageDBUsingRowId.getInt(queryRCSMessageDBUsingRowId.getColumnIndex("status"));
                    String str = this.LOG_TAG;
                    Log.i(str, "getMessageStatus resUrl: " + i);
                    if (i == ImConstants.Status.CANCELLATION.getId()) {
                        queryRCSMessageDBUsingRowId.close();
                        return true;
                    }
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (queryRCSMessageDBUsingRowId == null) {
            return false;
        }
        queryRCSMessageDBUsingRowId.close();
        return false;
        throw th;
    }

    public Pair<Object, HttpPostBody> getGroupSMSForSteadyUpload(BufferDBChangeParam bufferDBChangeParam) {
        String[] strArr;
        Cursor querySMSBufferDB = querySMSBufferDB(bufferDBChangeParam.mRowId);
        Pair<Object, HttpPostBody> pair = null;
        if (querySMSBufferDB != null) {
            try {
                if (querySMSBufferDB.moveToFirst()) {
                    Object object = new Object();
                    long j = (long) querySMSBufferDB.getInt(querySMSBufferDB.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBSMS.GROUP_ID));
                    if (j == 0) {
                        strArr = null;
                    } else if (querySMSBufferDB.getInt(querySMSBufferDB.getColumnIndexOrThrow("hidden")) == 1) {
                        Log.e(this.LOG_TAG, "getSmsObjectPairFromCursor hidden msg - shouldn't have been added for upload");
                        querySMSBufferDB.close();
                        return null;
                    } else {
                        strArr = getAllToAddress(j, true);
                    }
                    AttributeTranslator attributeTranslator = new AttributeTranslator(this.mStoreClient);
                    attributeTranslator.setTo(strArr);
                    attributeTranslator.setTextContent(querySMSBufferDB.getString(querySMSBufferDB.getColumnIndex("body")));
                    object.attributes = attributeTranslator.getAttributeList();
                    pair = new Pair<>(object, (Object) null);
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (querySMSBufferDB != null) {
            querySMSBufferDB.close();
        }
        return pair;
        throw th;
    }
}
