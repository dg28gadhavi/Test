package com.sec.internal.ims.cmstore.omanetapi.bufferdbtranslation;

import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;
import com.google.gson.Gson;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber$PhoneNumber;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.MIMEContentType;
import com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.cmstore.McsConstants;
import com.sec.internal.constants.ims.cmstore.data.FlagNames;
import com.sec.internal.constants.ims.servicemodules.im.ChatData;
import com.sec.internal.constants.ims.servicemodules.im.ImContract;
import com.sec.internal.helper.FileUtils;
import com.sec.internal.helper.httpclient.HttpPostBody;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.helper.ATTGlobalVariables;
import com.sec.internal.ims.cmstore.helper.TelephonyDbHelper;
import com.sec.internal.ims.cmstore.omanetapi.bufferdbtranslation.BufferQueryDBTranslation;
import com.sec.internal.ims.cmstore.omanetapi.nms.data.AttributeTranslator;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.params.ParamVvmUpdate;
import com.sec.internal.ims.cmstore.utils.Util;
import com.sec.internal.ims.core.cmc.CmcConstants;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.interfaces.ims.cmstore.ITelephonyDBColumns;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.common.data.FileUploadResponse;
import com.sec.internal.omanetapi.nms.data.ConferenceDescription;
import com.sec.internal.omanetapi.nms.data.ConferenceInfo;
import com.sec.internal.omanetapi.nms.data.ConferenceState;
import com.sec.internal.omanetapi.nms.data.FlagList;
import com.sec.internal.omanetapi.nms.data.Object;
import com.sec.internal.omanetapi.nms.data.Users;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BufferDBSupportTranslation extends BufferQueryDBTranslation {
    private String LOG_TAG = BufferDBSupportTranslation.class.getSimpleName();
    protected ICloudMessageManagerHelper mCloudMessageManagerHelper;
    protected final TelephonyDbHelper mTeleDBHelper;
    protected SimpleDateFormat sFormatOfName;

    public static class MessageStatus {
        public static final int MESSAGE_TYPE_INBOX = 1;
        public static final int MESSAGE_TYPE_SENT = 2;
    }

    /* access modifiers changed from: protected */
    public Pair<Object, HttpPostBody> getObjectPairFromCursor(Cursor cursor, BufferQueryDBTranslation.MessageType messageType, List<FileUploadResponse> list) {
        return null;
    }

    public BufferDBSupportTranslation(MessageStoreClient messageStoreClient, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        super(messageStoreClient);
        this.LOG_TAG += "[" + messageStoreClient.getClientID() + "]";
        this.mTeleDBHelper = new TelephonyDbHelper(this.mContext);
        this.sFormatOfName = this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getDateFormat();
    }

    /* access modifiers changed from: protected */
    public Object getVvmObjectFromDB(BufferDBChangeParam bufferDBChangeParam) {
        Object object = new Object();
        Cursor queryVvmGreetingBufferDB = queryVvmGreetingBufferDB(bufferDBChangeParam.mRowId);
        if (queryVvmGreetingBufferDB != null) {
            try {
                if (queryVvmGreetingBufferDB.moveToFirst()) {
                    FlagList flagList = new FlagList();
                    object.flags = flagList;
                    flagList.flag = new String[]{FlagNames.Cns_Greeting_on};
                    AttributeTranslator attributeTranslator = new AttributeTranslator(this.mStoreClient);
                    attributeTranslator.setDate(new String[]{this.sFormatOfName.format(Long.valueOf(System.currentTimeMillis()))});
                    attributeTranslator.setMessageId(new String[]{Util.generateHash()});
                    attributeTranslator.setMimeVersion(new String[]{"1.0"});
                    int i = queryVvmGreetingBufferDB.getInt(queryVvmGreetingBufferDB.getColumnIndex(CloudMessageProviderContract.VVMGreetingColumns.GREETINGTYPE));
                    String str = this.LOG_TAG;
                    Log.i(str, "getVvmObjectFromDB greetingType: " + i);
                    attributeTranslator.setGreetingType(new String[]{ParamVvmUpdate.VvmGreetingType.valueOf(i).getName()});
                    attributeTranslator.setContentDuration(new String[]{String.valueOf(queryVvmGreetingBufferDB.getInt(queryVvmGreetingBufferDB.getColumnIndex(CloudMessageProviderContract.VVMGreetingColumns.DURATION)))});
                    object.attributes = attributeTranslator.getAttributeList();
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (queryVvmGreetingBufferDB != null) {
            queryVvmGreetingBufferDB.close();
        }
        return object;
        throw th;
    }

    /* access modifiers changed from: protected */
    public HttpPostBody getVvmGreetingBodyFromDB(BufferDBChangeParam bufferDBChangeParam) {
        ArrayList arrayList = new ArrayList();
        Cursor queryVvmGreetingBufferDB = queryVvmGreetingBufferDB(bufferDBChangeParam.mRowId);
        if (queryVvmGreetingBufferDB != null) {
            try {
                if (queryVvmGreetingBufferDB.moveToFirst()) {
                    do {
                        String string = queryVvmGreetingBufferDB.getString(queryVvmGreetingBufferDB.getColumnIndex("filepath"));
                        if (string != null) {
                            byte[] fileContentInBytes = getFileContentInBytes(Uri.parse(string), CloudMessageBufferDBConstants.PayloadEncoding.Base64);
                            if (fileContentInBytes != null) {
                                String string2 = queryVvmGreetingBufferDB.getString(queryVvmGreetingBufferDB.getColumnIndex("fileName"));
                                if (string2 == null) {
                                    string2 = string.substring(string.lastIndexOf(47) + 1);
                                }
                                String string3 = queryVvmGreetingBufferDB.getString(queryVvmGreetingBufferDB.getColumnIndex("mimeType"));
                                if (!TextUtils.isEmpty(string3)) {
                                    HttpPostBody httpPostBody = new HttpPostBody("attachment;filename=\"" + string2 + CmcConstants.E_NUM_STR_QUOTE, string3, fileContentInBytes);
                                    httpPostBody.setContentTransferEncoding(HttpPostBody.CONTENT_TRANSFER_ENCODING_BASE64);
                                    String str = this.LOG_TAG;
                                    Log.i(str, "getVvmGreetingBodyFromDB data size: " + fileContentInBytes.length + " filename: " + string2 + " contentType: " + string3);
                                    arrayList.add(httpPostBody);
                                }
                            }
                        }
                    } while (queryVvmGreetingBufferDB.moveToNext());
                    HttpPostBody httpPostBody2 = new HttpPostBody("form-data;name=\"attachments\"", "multipart/mixed", (List<HttpPostBody>) arrayList);
                    queryVvmGreetingBufferDB.close();
                    return httpPostBody2;
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (queryVvmGreetingBufferDB == null) {
            return null;
        }
        queryVvmGreetingBufferDB.close();
        return null;
        throw th;
    }

    /* access modifiers changed from: protected */
    public HttpPostBody getThumbnailPayloadPart(Cursor cursor, File file, File file2, String str) {
        if (file != null && file.exists()) {
            String string = cursor.getString(cursor.getColumnIndex(ImContract.CsSession.FILE_NAME));
            if (TextUtils.isEmpty(string)) {
                string = Util.getRandomFileName("jpg");
            }
            String str2 = "attachment;filename=\"" + string + CmcConstants.E_NUM_STR_QUOTE;
            String string2 = cursor.getString(cursor.getColumnIndex("content_type"));
            if (!TextUtils.isEmpty(string2) && MIMEContentType.FT_HTTP.equals(string2)) {
                string2 = (file2 == null || !file2.exists()) ? FileUtils.getContentType(file) : FileUtils.getContentType(file2);
            }
            String str3 = string2;
            byte[] fileContentInBytes = getFileContentInBytes(str, CloudMessageBufferDBConstants.PayloadEncoding.None);
            if (!(fileContentInBytes == null || fileContentInBytes.length == 0 || TextUtils.isEmpty(str3))) {
                if (file2 == null || !file2.exists()) {
                    return new HttpPostBody(str2, str3, fileContentInBytes);
                }
                return new HttpPostBody("icon;filename=\"thumbnail_" + string + CmcConstants.E_NUM_STR_QUOTE, str3, fileContentInBytes, (String) null, "thumbnail");
            }
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public HttpPostBody getFilePayloadPart(Cursor cursor, File file, File file2, String str) {
        if (file != null && file.exists()) {
            String str2 = "attachment;filename=\"" + cursor.getString(cursor.getColumnIndex(ImContract.CsSession.FILE_NAME)) + CmcConstants.E_NUM_STR_QUOTE;
            String string = cursor.getString(cursor.getColumnIndex("content_type"));
            if (!TextUtils.isEmpty(string) && MIMEContentType.FT_HTTP.equals(string)) {
                string = FileUtils.getContentType(file);
            }
            String str3 = string;
            byte[] fileContentInBytes = getFileContentInBytes(str, CloudMessageBufferDBConstants.PayloadEncoding.None);
            if (!(fileContentInBytes == null || fileContentInBytes.length == 0 || TextUtils.isEmpty(str3))) {
                if (file2 == null || !file2.exists()) {
                    return new HttpPostBody(str2, str3, fileContentInBytes);
                }
                return new HttpPostBody(str2, str3, fileContentInBytes, "cid:thumbnail", (String) null);
            }
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public String getLocalFilePathForFt(Cursor cursor) {
        String string = cursor.getString(cursor.getColumnIndex("imdn_message_id"));
        long ftRowFromTelephony = this.mTeleDBHelper.getFtRowFromTelephony(string);
        if (ftRowFromTelephony == -1) {
            String str = this.LOG_TAG;
            Log.e(str, "Invalid rowId received for imdn id: " + string);
            return null;
        }
        String str2 = this.LOG_TAG;
        Log.i(str2, "row id : " + ftRowFromTelephony + " for imdn id:" + string);
        Uri withAppendedId = ContentUris.withAppendedId(Uri.parse("content://im/ft_original/"), ftRowFromTelephony);
        return FileUtils.copyFileToCacheFromUri(this.mContext, cursor.getString(cursor.getColumnIndex(ImContract.CsSession.FILE_NAME)), withAppendedId);
    }

    /* access modifiers changed from: protected */
    public List<HttpPostBody> getFtMultiBody(Cursor cursor, String str) {
        File file;
        String str2;
        String localFilePathForFt = getLocalFilePathForFt(cursor);
        Log.i(this.LOG_TAG, "getFtMultiBody localFilePath : " + localFilePathForFt + " filePath: " + str);
        String string = cursor.getString(cursor.getColumnIndexOrThrow(ImContract.CsSession.THUMBNAIL_PATH));
        File file2 = null;
        if (!TextUtils.isEmpty(localFilePathForFt)) {
            file = new File(localFilePathForFt);
            str2 = localFilePathForFt;
        } else if (!TextUtils.isEmpty(str)) {
            str2 = str;
            file = new File(str);
        } else {
            str2 = str;
            file = null;
        }
        if (!TextUtils.isEmpty(string)) {
            file2 = new File(string);
        }
        ArrayList arrayList = new ArrayList();
        if (ATTGlobalVariables.isAmbsPhaseIV()) {
            HttpPostBody filePayloadPart = getFilePayloadPart(cursor, file, file2, str2);
            if (filePayloadPart != null) {
                arrayList.add(filePayloadPart);
            }
            HttpPostBody thumbnailPayloadPart = getThumbnailPayloadPart(cursor, file2, file, string);
            if (thumbnailPayloadPart != null) {
                arrayList.add(thumbnailPayloadPart);
            }
            Log.d(this.LOG_TAG, "Filepath: " + file + " File payload size: " + arrayList.size() + " thumbnailpath: " + string + " Thumbnail payload size: " + arrayList.size());
        } else {
            if (file != null && file.exists()) {
                String str3 = "attachment;filename=\"" + cursor.getString(cursor.getColumnIndex(ImContract.CsSession.FILE_NAME)) + CmcConstants.E_NUM_STR_QUOTE;
                String string2 = cursor.getString(cursor.getColumnIndex("content_type"));
                byte[] fileContentInBytes = getFileContentInBytes(str2, CloudMessageBufferDBConstants.PayloadEncoding.None);
                if (fileContentInBytes == null || fileContentInBytes.length == 0 || TextUtils.isEmpty(string2)) {
                    return arrayList;
                }
                arrayList.add(new HttpPostBody(str3, string2, fileContentInBytes));
            }
            Log.d(this.LOG_TAG, "thumbnail filepath : " + string + " ,body size: " + arrayList.size());
        }
        FileUtils.removeFile(localFilePathForFt);
        return arrayList;
    }

    /* access modifiers changed from: protected */
    public List<HttpPostBody> getSlmMultiBody(Cursor cursor, String str, BufferQueryDBTranslation.MessageType messageType, String str2) {
        return getChatSlmMultiBody(cursor, str, messageType, str2, TextUtils.isEmpty(str) ? getLocalFilePathForFt(cursor) : null);
    }

    /* access modifiers changed from: protected */
    public List<HttpPostBody> getChatSlmMultiBody(Cursor cursor, String str, BufferQueryDBTranslation.MessageType messageType, String str2, String str3) {
        Log.i(this.LOG_TAG, "getChatSlmMultiBody localFilePath : " + str3 + " filePath: " + str2);
        if (!TextUtils.isEmpty(str3)) {
            str2 = str3;
        }
        ArrayList arrayList = new ArrayList();
        if (!TextUtils.isEmpty(str)) {
            arrayList.add(new HttpPostBody("form-data;name=\"attachments\";filename=\"sms.txt\"", MIMEContentType.PLAIN_TEXT, str));
        } else if (!TextUtils.isEmpty(str2)) {
            String string = cursor.getString(cursor.getColumnIndex(ImContract.CsSession.FILE_NAME));
            String str4 = "attachment;name=file;filename=\"" + string + CmcConstants.E_NUM_STR_QUOTE;
            if (messageType == BufferQueryDBTranslation.MessageType.MESSAGE_CHAT) {
                str4 = "attachment;filename=\"" + string + CmcConstants.E_NUM_STR_QUOTE;
            }
            String string2 = cursor.getString(cursor.getColumnIndex("content_type"));
            byte[] fileContentInBytes = getFileContentInBytes(str2, CloudMessageBufferDBConstants.PayloadEncoding.None);
            if (fileContentInBytes == null || fileContentInBytes.length == 0 || TextUtils.isEmpty(string2)) {
                return arrayList;
            }
            arrayList.add(new HttpPostBody(str4, string2, fileContentInBytes));
        }
        FileUtils.removeFile(str3);
        return arrayList;
    }

    /* access modifiers changed from: protected */
    public boolean setCpmTransMessage(AttributeTranslator attributeTranslator, Set<String> set, BufferQueryDBTranslation.MessageType messageType, String str) {
        boolean z = true;
        if (set.size() > 1) {
            attributeTranslator.setCpmGroup(new String[]{"yes"});
        } else {
            attributeTranslator.setCpmGroup(new String[]{"no"});
            z = false;
        }
        String str2 = this.LOG_TAG;
        Log.i(str2, "setCpmTransMessage  type" + messageType);
        if (messageType == BufferQueryDBTranslation.MessageType.MESSAGE_CHAT) {
            if (Util.isLocationPushContentType(str)) {
                attributeTranslator.setMessageContext(new String[]{McsConstants.McsMessageContextValues.geolocationMessage});
            } else if (Util.isBotMessageContentType(str)) {
                attributeTranslator.setMessageContext(new String[]{McsConstants.McsMessageContextValues.botMessage});
            } else if (Util.isBotResponseMessageContentType(str)) {
                attributeTranslator.setMessageContext(new String[]{McsConstants.McsMessageContextValues.responseMessage});
            } else {
                attributeTranslator.setMessageContext(new String[]{"chat-message"});
            }
        } else if (messageType == BufferQueryDBTranslation.MessageType.MESSAGE_SLM) {
            attributeTranslator.setMessageContext(new String[]{"standalone-message"});
        } else if (messageType == BufferQueryDBTranslation.MessageType.MESSAGE_FT) {
            attributeTranslator.setMessageContext(new String[]{"file-message"});
        }
        return z;
    }

    /* access modifiers changed from: protected */
    public void setConversationId(AttributeTranslator attributeTranslator, String str) {
        if (!TextUtils.isEmpty(str)) {
            Cursor queryRCSSessionDB = queryRCSSessionDB(str);
            if (queryRCSSessionDB != null) {
                try {
                    if (queryRCSSessionDB.moveToFirst()) {
                        String string = queryRCSSessionDB.getString(queryRCSSessionDB.getColumnIndex("conversation_id"));
                        String str2 = this.LOG_TAG;
                        Log.i(str2, "getObjectPairFromCursor :: conversationId : " + string);
                        attributeTranslator.setConversationId(new String[]{string});
                    }
                } catch (Throwable th) {
                    th.addSuppressed(th);
                }
            }
            if (queryRCSSessionDB != null) {
                queryRCSSessionDB.close();
                return;
            }
            return;
        }
        return;
        throw th;
    }

    /* access modifiers changed from: protected */
    public Pair<Object, HttpPostBody> getFtObjectPairFromCursor(Cursor cursor, List<FileUploadResponse> list) {
        return getObjectPairFromCursor(cursor, BufferQueryDBTranslation.MessageType.MESSAGE_FT, list);
    }

    /* access modifiers changed from: protected */
    public Pair<Object, HttpPostBody> getFtObjectPairFromCursor(Cursor cursor) {
        return getObjectPairFromCursor(cursor, BufferQueryDBTranslation.MessageType.MESSAGE_FT);
    }

    /* access modifiers changed from: protected */
    public Pair<Object, HttpPostBody> getChatObjectPairFromCursor(Cursor cursor) {
        return getObjectPairFromCursor(cursor, BufferQueryDBTranslation.MessageType.MESSAGE_CHAT);
    }

    /* access modifiers changed from: protected */
    public Pair<Object, HttpPostBody> getSlmObjectPairFromCursor(Cursor cursor) {
        return getObjectPairFromCursor(cursor, BufferQueryDBTranslation.MessageType.MESSAGE_SLM);
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x00e0 A[Catch:{ all -> 0x01ee, all -> 0x01f4 }] */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x00f9 A[Catch:{ all -> 0x01ee, all -> 0x01f4 }] */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x0153 A[SYNTHETIC, Splitter:B:38:0x0153] */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x0187 A[Catch:{ all -> 0x01ee, all -> 0x01f4 }] */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x01c6  */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x01ca  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public android.util.Pair<com.sec.internal.omanetapi.nms.data.Object, com.sec.internal.helper.httpclient.HttpPostBody> getObjectPairFromCursor(android.database.Cursor r18, com.sec.internal.ims.cmstore.omanetapi.bufferdbtranslation.BufferQueryDBTranslation.MessageType r19) {
        /*
            r17 = this;
            r0 = r17
            r7 = r18
            r4 = r19
            r8 = 0
            if (r7 != 0) goto L_0x000a
            return r8
        L_0x000a:
            com.sec.internal.omanetapi.nms.data.Object r9 = new com.sec.internal.omanetapi.nms.data.Object
            r9.<init>()
            com.sec.internal.ims.cmstore.omanetapi.nms.data.AttributeTranslator r10 = new com.sec.internal.ims.cmstore.omanetapi.nms.data.AttributeTranslator
            com.sec.internal.ims.cmstore.MessageStoreClient r1 = r0.mStoreClient
            r10.<init>(r1)
            java.util.ArrayList r1 = new java.util.ArrayList
            r1.<init>()
            boolean r2 = r18.moveToFirst()     // Catch:{ all -> 0x01ee }
            if (r2 != 0) goto L_0x0025
            r18.close()
            return r8
        L_0x0025:
            java.lang.String r2 = "chat_id"
            int r2 = r7.getColumnIndex(r2)     // Catch:{ all -> 0x01ee }
            java.lang.String r11 = r7.getString(r2)     // Catch:{ all -> 0x01ee }
            java.lang.String r2 = "direction"
            int r2 = r7.getColumnIndex(r2)     // Catch:{ all -> 0x01ee }
            int r2 = r7.getInt(r2)     // Catch:{ all -> 0x01ee }
            long r2 = (long) r2     // Catch:{ all -> 0x01ee }
            com.sec.internal.omanetapi.nms.data.FlagList r5 = new com.sec.internal.omanetapi.nms.data.FlagList     // Catch:{ all -> 0x01ee }
            r5.<init>()     // Catch:{ all -> 0x01ee }
            r9.flags = r5     // Catch:{ all -> 0x01ee }
            java.lang.String r5 = "status"
            int r5 = r7.getColumnIndex(r5)     // Catch:{ all -> 0x01ee }
            int r5 = r7.getInt(r5)     // Catch:{ all -> 0x01ee }
            java.lang.String r6 = "ft_status"
            int r6 = r7.getColumnIndex(r6)     // Catch:{ all -> 0x01ee }
            int r6 = r7.getInt(r6)     // Catch:{ all -> 0x01ee }
            com.sec.internal.constants.ims.servicemodules.im.ImConstants$Status r12 = com.sec.internal.constants.ims.servicemodules.im.ImConstants.Status.READ     // Catch:{ all -> 0x01ee }
            int r13 = r12.getId()     // Catch:{ all -> 0x01ee }
            java.lang.String r14 = "\\Flagged"
            r15 = 1
            r16 = 0
            if (r5 == r13) goto L_0x007e
            int r5 = r12.getId()     // Catch:{ all -> 0x01ee }
            if (r6 == r5) goto L_0x007e
            com.sec.internal.constants.ims.servicemodules.im.ImDirection r5 = com.sec.internal.constants.ims.servicemodules.im.ImDirection.OUTGOING     // Catch:{ all -> 0x01ee }
            int r5 = r5.getId()     // Catch:{ all -> 0x01ee }
            long r5 = (long) r5     // Catch:{ all -> 0x01ee }
            int r5 = (r2 > r5 ? 1 : (r2 == r5 ? 0 : -1))
            if (r5 != 0) goto L_0x0075
            goto L_0x007e
        L_0x0075:
            com.sec.internal.omanetapi.nms.data.FlagList r5 = r9.flags     // Catch:{ all -> 0x01ee }
            java.lang.String[] r6 = new java.lang.String[r15]     // Catch:{ all -> 0x01ee }
            r6[r16] = r14     // Catch:{ all -> 0x01ee }
            r5.flag = r6     // Catch:{ all -> 0x01ee }
            goto L_0x008b
        L_0x007e:
            com.sec.internal.omanetapi.nms.data.FlagList r5 = r9.flags     // Catch:{ all -> 0x01ee }
            r6 = 2
            java.lang.String[] r6 = new java.lang.String[r6]     // Catch:{ all -> 0x01ee }
            r6[r16] = r14     // Catch:{ all -> 0x01ee }
            java.lang.String r12 = "\\Seen"
            r6[r15] = r12     // Catch:{ all -> 0x01ee }
            r5.flag = r6     // Catch:{ all -> 0x01ee }
        L_0x008b:
            java.text.SimpleDateFormat r5 = r0.sFormatOfName     // Catch:{ all -> 0x01ee }
            java.util.Date r6 = new java.util.Date     // Catch:{ all -> 0x01ee }
            java.lang.String r12 = "inserted_timestamp"
            int r12 = r7.getColumnIndex(r12)     // Catch:{ all -> 0x01ee }
            long r12 = r7.getLong(r12)     // Catch:{ all -> 0x01ee }
            r6.<init>(r12)     // Catch:{ all -> 0x01ee }
            java.lang.String r5 = r5.format(r6)     // Catch:{ all -> 0x01ee }
            java.lang.String[] r6 = new java.lang.String[]{r5}     // Catch:{ all -> 0x01ee }
            r10.setDate(r6)     // Catch:{ all -> 0x01ee }
            java.lang.String r6 = r0.LOG_TAG     // Catch:{ all -> 0x01ee }
            java.lang.StringBuilder r12 = new java.lang.StringBuilder     // Catch:{ all -> 0x01ee }
            r12.<init>()     // Catch:{ all -> 0x01ee }
            java.lang.String r13 = "getObjectPairFromCursor :: direction : "
            r12.append(r13)     // Catch:{ all -> 0x01ee }
            r12.append(r2)     // Catch:{ all -> 0x01ee }
            java.lang.String r13 = " messagetype : "
            r12.append(r13)     // Catch:{ all -> 0x01ee }
            r12.append(r4)     // Catch:{ all -> 0x01ee }
            java.lang.String r13 = " date : "
            r12.append(r13)     // Catch:{ all -> 0x01ee }
            r12.append(r5)     // Catch:{ all -> 0x01ee }
            java.lang.String r5 = r12.toString()     // Catch:{ all -> 0x01ee }
            android.util.Log.i(r6, r5)     // Catch:{ all -> 0x01ee }
            java.util.Set r5 = r0.getAddrFromParticipantTable(r11)     // Catch:{ all -> 0x01ee }
            boolean r12 = r0.setCpmTransMessage(r10, r5, r4, r8)     // Catch:{ all -> 0x01ee }
            com.sec.internal.constants.ims.servicemodules.im.ImDirection r6 = com.sec.internal.constants.ims.servicemodules.im.ImDirection.INCOMING     // Catch:{ all -> 0x01ee }
            int r6 = r6.getId()     // Catch:{ all -> 0x01ee }
            long r13 = (long) r6     // Catch:{ all -> 0x01ee }
            int r6 = (r2 > r13 ? 1 : (r2 == r13 ? 0 : -1))
            if (r6 != 0) goto L_0x00f9
            java.lang.String r2 = "remote_uri"
            int r2 = r7.getColumnIndex(r2)     // Catch:{ all -> 0x01ee }
            java.lang.String r2 = r7.getString(r2)     // Catch:{ all -> 0x01ee }
            if (r2 != 0) goto L_0x00f5
            com.sec.internal.ims.cmstore.omanetapi.bufferdbtranslation.BufferQueryDBTranslation$MessageType r3 = com.sec.internal.ims.cmstore.omanetapi.bufferdbtranslation.BufferQueryDBTranslation.MessageType.MESSAGE_CHAT     // Catch:{ all -> 0x01ee }
            if (r4 != r3) goto L_0x00f5
            r18.close()
            return r8
        L_0x00f5:
            r0.setTransToFrom(r10, r5, r2)     // Catch:{ all -> 0x01ee }
            goto L_0x0131
        L_0x00f9:
            com.sec.internal.constants.ims.servicemodules.im.ImDirection r6 = com.sec.internal.constants.ims.servicemodules.im.ImDirection.OUTGOING     // Catch:{ all -> 0x01ee }
            int r6 = r6.getId()     // Catch:{ all -> 0x01ee }
            long r13 = (long) r6     // Catch:{ all -> 0x01ee }
            int r2 = (r2 > r13 ? 1 : (r2 == r13 ? 0 : -1))
            if (r2 != 0) goto L_0x01ea
            java.lang.String[] r2 = new java.lang.String[r15]     // Catch:{ all -> 0x01ee }
            java.lang.String r3 = "OUT"
            r2[r16] = r3     // Catch:{ all -> 0x01ee }
            r10.setDirection(r2)     // Catch:{ all -> 0x01ee }
            int r2 = r5.size()     // Catch:{ all -> 0x01ee }
            java.lang.String[] r2 = new java.lang.String[r2]     // Catch:{ all -> 0x01ee }
            java.lang.Object[] r2 = r5.toArray(r2)     // Catch:{ all -> 0x01ee }
            java.lang.String[] r2 = (java.lang.String[]) r2     // Catch:{ all -> 0x01ee }
            r10.setTo(r2)     // Catch:{ all -> 0x01ee }
            java.lang.String[] r2 = new java.lang.String[r15]     // Catch:{ all -> 0x01ee }
            com.sec.internal.ims.cmstore.MessageStoreClient r3 = r0.mStoreClient     // Catch:{ all -> 0x01ee }
            com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager r3 = r3.getPrerenceManager()     // Catch:{ all -> 0x01ee }
            java.lang.String r3 = r3.getUserCtn()     // Catch:{ all -> 0x01ee }
            java.lang.String r3 = r0.getE164FormatNumber(r3)     // Catch:{ all -> 0x01ee }
            r2[r16] = r3     // Catch:{ all -> 0x01ee }
            r10.setFrom(r2)     // Catch:{ all -> 0x01ee }
        L_0x0131:
            java.lang.String r2 = "imdn_message_id"
            int r2 = r7.getColumnIndex(r2)     // Catch:{ all -> 0x01ee }
            java.lang.String r2 = r7.getString(r2)     // Catch:{ all -> 0x01ee }
            r9.correlationId = r2     // Catch:{ all -> 0x01ee }
            java.lang.String r3 = "file_path"
            int r3 = r7.getColumnIndex(r3)     // Catch:{ all -> 0x01ee }
            java.lang.String r5 = r7.getString(r3)     // Catch:{ all -> 0x01ee }
            com.sec.internal.ims.cmstore.omanetapi.bufferdbtranslation.BufferQueryDBTranslation$MessageType r3 = com.sec.internal.ims.cmstore.omanetapi.bufferdbtranslation.BufferQueryDBTranslation.MessageType.MESSAGE_CHAT     // Catch:{ all -> 0x01ee }
            java.lang.String r6 = " body : "
            java.lang.String r13 = " correlationId : "
            java.lang.String r14 = "getObjectPairFromCursor :: filepath : "
            java.lang.String r15 = "body"
            if (r4 != r3) goto L_0x0187
            int r1 = r7.getColumnIndex(r15)     // Catch:{ all -> 0x01ee }
            java.lang.String r3 = r7.getString(r1)     // Catch:{ all -> 0x01ee }
            java.lang.String r1 = r0.LOG_TAG     // Catch:{ all -> 0x01ee }
            java.lang.StringBuilder r15 = new java.lang.StringBuilder     // Catch:{ all -> 0x01ee }
            r15.<init>()     // Catch:{ all -> 0x01ee }
            r15.append(r14)     // Catch:{ all -> 0x01ee }
            r15.append(r5)     // Catch:{ all -> 0x01ee }
            r15.append(r13)     // Catch:{ all -> 0x01ee }
            r15.append(r2)     // Catch:{ all -> 0x01ee }
            r15.append(r6)     // Catch:{ all -> 0x01ee }
            r15.append(r3)     // Catch:{ all -> 0x01ee }
            java.lang.String r2 = r15.toString()     // Catch:{ all -> 0x01ee }
            android.util.Log.i(r1, r2)     // Catch:{ all -> 0x01ee }
            r6 = 0
            r1 = r17
            r2 = r18
            r4 = r19
            java.util.List r1 = r1.getChatSlmMultiBody(r2, r3, r4, r5, r6)     // Catch:{ all -> 0x01ee }
            goto L_0x01c0
        L_0x0187:
            com.sec.internal.ims.cmstore.omanetapi.bufferdbtranslation.BufferQueryDBTranslation$MessageType r3 = com.sec.internal.ims.cmstore.omanetapi.bufferdbtranslation.BufferQueryDBTranslation.MessageType.MESSAGE_SLM     // Catch:{ all -> 0x01ee }
            if (r4 != r3) goto L_0x01b8
            int r1 = r7.getColumnIndex(r15)     // Catch:{ all -> 0x01ee }
            java.lang.String r1 = r7.getString(r1)     // Catch:{ all -> 0x01ee }
            java.lang.String r3 = r0.LOG_TAG     // Catch:{ all -> 0x01ee }
            java.lang.StringBuilder r15 = new java.lang.StringBuilder     // Catch:{ all -> 0x01ee }
            r15.<init>()     // Catch:{ all -> 0x01ee }
            r15.append(r14)     // Catch:{ all -> 0x01ee }
            r15.append(r5)     // Catch:{ all -> 0x01ee }
            r15.append(r13)     // Catch:{ all -> 0x01ee }
            r15.append(r2)     // Catch:{ all -> 0x01ee }
            r15.append(r6)     // Catch:{ all -> 0x01ee }
            r15.append(r1)     // Catch:{ all -> 0x01ee }
            java.lang.String r2 = r15.toString()     // Catch:{ all -> 0x01ee }
            android.util.Log.i(r3, r2)     // Catch:{ all -> 0x01ee }
            java.util.List r1 = r0.getSlmMultiBody(r7, r1, r4, r5)     // Catch:{ all -> 0x01ee }
            goto L_0x01c0
        L_0x01b8:
            com.sec.internal.ims.cmstore.omanetapi.bufferdbtranslation.BufferQueryDBTranslation$MessageType r2 = com.sec.internal.ims.cmstore.omanetapi.bufferdbtranslation.BufferQueryDBTranslation.MessageType.MESSAGE_FT     // Catch:{ all -> 0x01ee }
            if (r4 != r2) goto L_0x01c0
            java.util.List r1 = r0.getFtMultiBody(r7, r5)     // Catch:{ all -> 0x01ee }
        L_0x01c0:
            int r2 = r1.size()     // Catch:{ all -> 0x01ee }
            if (r2 != 0) goto L_0x01ca
            r18.close()
            return r8
        L_0x01ca:
            r18.close()
            r0.setConversationId(r10, r11)
            if (r12 == 0) goto L_0x01d5
            r0.setSubjectAndGroup(r11, r10)
        L_0x01d5:
            com.sec.internal.omanetapi.nms.data.AttributeList r0 = r10.getAttributeList()
            r9.attributes = r0
            com.sec.internal.helper.httpclient.HttpPostBody r0 = new com.sec.internal.helper.httpclient.HttpPostBody
            java.lang.String r2 = "form-data;name=\"attachments\""
            java.lang.String r3 = "multipart/mixed"
            r0.<init>((java.lang.String) r2, (java.lang.String) r3, (java.util.List<com.sec.internal.helper.httpclient.HttpPostBody>) r1)
            android.util.Pair r1 = new android.util.Pair
            r1.<init>(r9, r0)
            return r1
        L_0x01ea:
            r18.close()
            return r8
        L_0x01ee:
            r0 = move-exception
            r1 = r0
            r18.close()     // Catch:{ all -> 0x01f4 }
            goto L_0x01f9
        L_0x01f4:
            r0 = move-exception
            r2 = r0
            r1.addSuppressed(r2)
        L_0x01f9:
            throw r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.omanetapi.bufferdbtranslation.BufferDBSupportTranslation.getObjectPairFromCursor(android.database.Cursor, com.sec.internal.ims.cmstore.omanetapi.bufferdbtranslation.BufferQueryDBTranslation$MessageType):android.util.Pair");
    }

    private Set<String> getAddrFromParticipantTable(String str) {
        HashSet hashSet = new HashSet();
        Cursor queryRCSParticipantDB = queryRCSParticipantDB(str);
        if (queryRCSParticipantDB != null) {
            try {
                if (queryRCSParticipantDB.moveToFirst()) {
                    do {
                        String string = queryRCSParticipantDB.getString(queryRCSParticipantDB.getColumnIndex("uri"));
                        if (!TextUtils.isEmpty(string)) {
                            hashSet.add(ImsUri.parse(string).getMsisdn());
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

    private void setSubjectAndGroup(String str, AttributeTranslator attributeTranslator) {
        if (!TextUtils.isEmpty(str)) {
            Cursor queryRCSSessionDB = queryRCSSessionDB(str);
            if (queryRCSSessionDB != null) {
                try {
                    if (queryRCSSessionDB.moveToFirst()) {
                        String string = queryRCSSessionDB.getString(queryRCSSessionDB.getColumnIndex("subject"));
                        if (string == null) {
                            string = "";
                        }
                        attributeTranslator.setSubject(new String[]{string});
                        ChatData.ChatType fromId = ChatData.ChatType.fromId(queryRCSSessionDB.getInt(queryRCSSessionDB.getColumnIndexOrThrow(ImContract.ImSession.CHAT_TYPE)));
                        String str2 = this.LOG_TAG;
                        Log.i(str2, "getChatObjectPairFromCursor :: subject : " + string + " chatType : " + fromId);
                        if (fromId == ChatData.ChatType.REGULAR_GROUP_CHAT) {
                            attributeTranslator.setOpenGroup(new String[]{"yes"});
                        } else {
                            attributeTranslator.setOpenGroup(new String[]{"no"});
                        }
                    }
                } catch (Throwable th) {
                    th.addSuppressed(th);
                }
            }
            if (queryRCSSessionDB != null) {
                queryRCSSessionDB.close();
                return;
            }
            return;
        }
        return;
        throw th;
    }

    /* access modifiers changed from: protected */
    public HttpPostBody getMmsPartHttpPayloadFromCursor(Cursor cursor) {
        byte[] bArr;
        ArrayList arrayList = new ArrayList();
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    do {
                        String string = cursor.getString(cursor.getColumnIndex("_id"));
                        String string2 = cursor.getString(cursor.getColumnIndex(CloudMessageProviderContract.BufferDBMMSpart.CT));
                        if (!TextUtils.isEmpty(string2)) {
                            if (TextUtils.isEmpty(cursor.getString(cursor.getColumnIndex(CloudMessageProviderContract.BufferDBMMSpart._DATA)))) {
                                String string3 = cursor.getString(cursor.getColumnIndex("text"));
                                bArr = string3 != null ? string3.getBytes() : null;
                            } else {
                                bArr = getDataFromPartFile(Long.parseLong(string));
                            }
                            if (bArr != null) {
                                String string4 = cursor.getString(cursor.getColumnIndex(CloudMessageProviderContract.BufferDBMMSpart.CL));
                                HttpPostBody httpPostBody = new HttpPostBody("attachment;filename=\"" + string4 + CmcConstants.E_NUM_STR_QUOTE, string2, bArr);
                                String str = this.LOG_TAG;
                                Log.i(str, "getMmsPartHttpPayloadFromCursor id: " + string + ", contentType: " + string2 + " data size: " + bArr.length + " filename: " + string4);
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

    /* access modifiers changed from: protected */
    public Object getMmsObjectFromPduAndAddress(BufferDBChangeParam bufferDBChangeParam) {
        Object object = new Object();
        Cursor querymmsPduBufferDB = querymmsPduBufferDB(bufferDBChangeParam.mRowId);
        if (querymmsPduBufferDB != null) {
            try {
                if (querymmsPduBufferDB.moveToFirst()) {
                    object.flags = new FlagList();
                    int i = querymmsPduBufferDB.getInt(querymmsPduBufferDB.getColumnIndex("read"));
                    long j = querymmsPduBufferDB.getLong(querymmsPduBufferDB.getColumnIndex(CloudMessageProviderContract.BufferDBMMSpdu.MSG_BOX));
                    if (i == 1 || j == 2) {
                        FlagList flagList = object.flags;
                        flagList.flag = new String[]{FlagNames.Flagged, FlagNames.Seen};
                    } else {
                        FlagList flagList2 = object.flags;
                        flagList2.flag = new String[]{FlagNames.Flagged};
                    }
                    String string = querymmsPduBufferDB.getString(querymmsPduBufferDB.getColumnIndex(CloudMessageProviderContract.BufferDBMMSpdu.TR_ID));
                    String str = this.LOG_TAG;
                    Log.d(str, "getMmsObjectFromPduAndAddress: " + bufferDBChangeParam.mRowId + ", trid : " + string);
                    if (!this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isTrIdCorrelationId() || string == null || string.length() <= 2) {
                        object.correlationId = querymmsPduBufferDB.getString(querymmsPduBufferDB.getColumnIndex(CloudMessageProviderContract.BufferDBMMSpdu.M_ID));
                    } else {
                        object.correlationId = string.substring(2);
                    }
                    AttributeTranslator attributeTranslator = new AttributeTranslator(this.mStoreClient);
                    attributeTranslator.setDate(new String[]{this.sFormatOfName.format(new Date(querymmsPduBufferDB.getLong(querymmsPduBufferDB.getColumnIndex("date"))))});
                    MmsParticipant addrFromPduId = getAddrFromPduId(bufferDBChangeParam.mRowId);
                    if (j == 1) {
                        attributeTranslator.setDirection(new String[]{"IN"});
                        Set<String> set = addrFromPduId.mFrom;
                        attributeTranslator.setFrom((String[]) set.toArray(new String[set.size()]));
                        addrFromPduId.mTo.add(getE164FormatNumber(this.mStoreClient.getPrerenceManager().getUserCtn()));
                        Set<String> set2 = addrFromPduId.mTo;
                        attributeTranslator.setTo((String[]) set2.toArray(new String[set2.size()]));
                    } else if (j == 2) {
                        attributeTranslator.setDirection(new String[]{"OUT"});
                        if (addrFromPduId.mTo.size() != 0) {
                            Set<String> set3 = addrFromPduId.mTo;
                            attributeTranslator.setTo((String[]) set3.toArray(new String[set3.size()]));
                        }
                        if (addrFromPduId.mBcc.size() != 0) {
                            Set<String> set4 = addrFromPduId.mBcc;
                            attributeTranslator.setBCC((String[]) set4.toArray(new String[set4.size()]));
                        }
                        if (addrFromPduId.mCc.size() != 0) {
                            Set<String> set5 = addrFromPduId.mCc;
                            attributeTranslator.setCC((String[]) set5.toArray(new String[set5.size()]));
                        }
                        attributeTranslator.setFrom(new String[]{getE164FormatNumber(this.mStoreClient.getPrerenceManager().getUserCtn())});
                    }
                    attributeTranslator.setCpmGroup(new String[]{"no"});
                    attributeTranslator.setMessageContext(new String[]{"multimedia-message"});
                    object.attributes = attributeTranslator.getAttributeList();
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (querymmsPduBufferDB != null) {
            querymmsPduBufferDB.close();
        }
        if (!TextUtils.isEmpty(object.correlationId)) {
            return object;
        }
        Log.e(this.LOG_TAG, "getMmsObjectFromPduAndAddress: correlation id is empty!!!");
        return null;
        throw th;
    }

    public Pair<Object, HttpPostBody> getConferenceInfoObjectPair(Cursor cursor) {
        Cursor queryRCSParticipantDB;
        int i;
        int i2;
        Throwable th;
        int i3;
        Cursor cursor2 = cursor;
        String str = "connected";
        if (cursor2 == null) {
            return null;
        }
        Object object = new Object();
        AttributeTranslator attributeTranslator = new AttributeTranslator(this.mStoreClient);
        ConferenceInfo conferenceInfo = new ConferenceInfo();
        try {
            if (!cursor.moveToFirst()) {
                cursor.close();
                return null;
            }
            String userTelCtn = this.mStoreClient.getPrerenceManager().getUserTelCtn();
            conferenceInfo.mTimestamp = cursor2.getString(cursor2.getColumnIndexOrThrow(ImContract.ImSession.INSERTED_TIMESTAMP));
            conferenceInfo.mEntity = cursor2.getString(cursor2.getColumnIndex("session_uri"));
            conferenceInfo.mState = "full";
            conferenceInfo.mConferenceDescription = new ConferenceDescription();
            String string = cursor2.getString(cursor2.getColumnIndexOrThrow(ImContract.ImSession.ICON_PATH));
            if (!TextUtils.isEmpty(string)) {
                conferenceInfo.mConferenceDescription.mIcon = new ConferenceDescription.Icon();
                conferenceInfo.mConferenceDescription.mIcon.mFileInfo = new ConferenceDescription.Icon.FileInfo();
                File file = new File(string);
                String contentType = FileUtils.getContentType(file);
                if (TextUtils.equals(contentType, HttpPostBody.CONTENT_TYPE_DEFAULT)) {
                    contentType = "image/jpeg";
                }
                ConferenceDescription.Icon.FileInfo fileInfo = conferenceInfo.mConferenceDescription.mIcon.mFileInfo;
                fileInfo.mContentType = contentType;
                fileInfo.mData = Base64.encodeToString(Files.readAllBytes(file.toPath()), 2);
            }
            conferenceInfo.mConferenceDescription.mSubject = cursor2.getString(cursor2.getColumnIndex("subject"));
            long j = cursor2.getLong(cursor2.getColumnIndexOrThrow("subject_timestamp"));
            if (j > 0) {
                ConferenceDescription conferenceDescription = conferenceInfo.mConferenceDescription;
                if (conferenceDescription.mSubjectExt == null) {
                    conferenceDescription.mSubjectExt = new ConferenceDescription.SubjectExt();
                }
                conferenceInfo.mConferenceDescription.mSubjectExt.mTimestamp = String.valueOf(j);
            }
            conferenceInfo.mConferenceDescription.mMaxCount = cursor2.getInt(cursor2.getColumnIndex(ImContract.ImSession.MAX_PARTICIPANTS_COUNT));
            String string2 = cursor2.getString(cursor2.getColumnIndex("chat_id"));
            if (TextUtils.isEmpty(conferenceInfo.mEntity)) {
                String str2 = this.LOG_TAG;
                Log.e(str2, "Session URI is null with chatId " + string2);
                cursor.close();
                return null;
            }
            conferenceInfo.mConferenceState = new ConferenceState();
            boolean z = cursor2.getInt(cursor2.getColumnIndexOrThrow("status")) == ChatData.State.NONE.getId();
            conferenceInfo.mConferenceState.mActivation = !z;
            conferenceInfo.mUsers = new Users();
            queryRCSParticipantDB = queryRCSParticipantDB(string2);
            if (queryRCSParticipantDB != null) {
                i = queryRCSParticipantDB.getCount();
                conferenceInfo.mUsers.mUser = new Users.User[(i + 1)];
                if (queryRCSParticipantDB.moveToFirst()) {
                    i2 = 0;
                    while (true) {
                        String string3 = queryRCSParticipantDB.getString(queryRCSParticipantDB.getColumnIndex("uri"));
                        conferenceInfo.mUsers.mUser[i2] = new Users.User();
                        Users.User user = conferenceInfo.mUsers.mUser[i2];
                        user.mEntity = string3;
                        user.mState = "full";
                        String string4 = queryRCSParticipantDB.getString(queryRCSParticipantDB.getColumnIndex("alias"));
                        if (!TextUtils.isEmpty(string4)) {
                            conferenceInfo.mUsers.mUser[i2].mDisplayText = string4;
                        }
                        if (queryRCSParticipantDB.getInt(queryRCSParticipantDB.getColumnIndex("type")) == 2) {
                            i3 = i;
                            conferenceInfo.mUsers.mUser[i2].mRole = new String[]{"Administrator"};
                        } else {
                            i3 = i;
                            conferenceInfo.mUsers.mUser[i2].mRole = new String[]{"participant"};
                        }
                        Users.User.Endpoint[] endpointArr = new Users.User.Endpoint[1];
                        conferenceInfo.mUsers.mUser[i2].mEndpoint = endpointArr;
                        endpointArr[0] = new Users.User.Endpoint();
                        Users.User.Endpoint endpoint = conferenceInfo.mUsers.mUser[i2].mEndpoint[0];
                        endpoint.mEntity = string3;
                        endpoint.mState = "full";
                        endpoint.mStatus = str;
                        i2++;
                        if (!queryRCSParticipantDB.moveToNext()) {
                            break;
                        }
                        Cursor cursor3 = cursor;
                        i = i3;
                    }
                    conferenceInfo.mUsers.mUser[i2] = new Users.User();
                    Users.User[] userArr = conferenceInfo.mUsers.mUser;
                    Users.User user2 = userArr[i2];
                    user2.mEntity = userTelCtn;
                    user2.mState = "full";
                    user2.mOwn = true;
                    user2.mRole = new String[]{"participant"};
                    Users.User.Endpoint[] endpointArr2 = new Users.User.Endpoint[1];
                    userArr[i2].mEndpoint = endpointArr2;
                    endpointArr2[0] = new Users.User.Endpoint();
                    Users.User.Endpoint endpoint2 = conferenceInfo.mUsers.mUser[i2].mEndpoint[0];
                    endpoint2.mEntity = userTelCtn;
                    endpoint2.mState = "full";
                    if (z) {
                        str = "disconnected";
                    }
                    endpoint2.mStatus = str;
                    i = i3;
                } else {
                    int i4 = i;
                    i2 = 0;
                }
            } else {
                i2 = 0;
                i = 0;
            }
            if (queryRCSParticipantDB != null) {
                queryRCSParticipantDB.close();
            }
            if (i <= 0) {
                cursor.close();
                return null;
            }
            conferenceInfo.mConferenceState.mUserCount = i2 + 1;
            attributeTranslator.setMessageContext(new String[]{McsConstants.McsMessageContextValues.conferenceMessage});
            attributeTranslator.setContentType(new String[]{MIMEContentType.CONFERENCE_INFO});
            attributeTranslator.setDate(new String[]{this.sFormatOfName.format(Long.valueOf(System.currentTimeMillis()))});
            attributeTranslator.setMessageBody(new String[]{new Gson().toJson(conferenceInfo)});
            cursor.close();
            setConversationId(attributeTranslator, string2);
            object.attributes = attributeTranslator.getAttributeList();
            return new Pair<>(object, (Object) null);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Throwable th2) {
            Throwable th3 = th2;
            try {
                cursor.close();
            } catch (Throwable th4) {
                th3.addSuppressed(th4);
            }
            throw th3;
        }
        throw th;
    }

    /* access modifiers changed from: protected */
    public MmsParticipant getAddrFromPduId(long j) {
        HashSet hashSet;
        String str = this.LOG_TAG;
        Log.d(str, "getAddrFromPduId: " + j);
        HashSet hashSet2 = new HashSet();
        HashSet hashSet3 = new HashSet();
        HashSet hashSet4 = new HashSet();
        HashSet hashSet5 = new HashSet();
        Cursor queryAddrBufferDB = queryAddrBufferDB(j);
        if (queryAddrBufferDB != null) {
            try {
                if (queryAddrBufferDB.moveToFirst()) {
                    do {
                        String string = queryAddrBufferDB.getString(queryAddrBufferDB.getColumnIndex("address"));
                        int i = queryAddrBufferDB.getInt(queryAddrBufferDB.getColumnIndex("type"));
                        String str2 = this.LOG_TAG;
                        Log.d(str2, " direction: " + i + "address is: " + IMSLog.checker(string));
                        if (i == 137) {
                            hashSet = hashSet2;
                        } else if (i == 151) {
                            hashSet = hashSet3;
                        } else if (i == 129) {
                            hashSet = hashSet4;
                        } else if (i == 130) {
                            hashSet = hashSet5;
                        }
                        if (!TextUtils.isEmpty(string)) {
                            if (string.equals(ITelephonyDBColumns.FROM_INSERT_ADDRESS_TOKEN_STR)) {
                                hashSet.add(getE164FormatNumber(this.mStoreClient.getPrerenceManager().getUserCtn(), Util.getSimCountryCode(this.mContext, this.mPhoneId)));
                            } else {
                                hashSet.add(getE164FormatNumber(string, Util.getSimCountryCode(this.mContext, this.mPhoneId)));
                            }
                        }
                    } while (queryAddrBufferDB.moveToNext());
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (queryAddrBufferDB != null) {
            queryAddrBufferDB.close();
        }
        return new MmsParticipant(hashSet2, hashSet3, hashSet4, hashSet5);
        throw th;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x005e A[SYNTHETIC, Splitter:B:20:0x005e] */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x007b A[SYNTHETIC, Splitter:B:29:0x007b] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public byte[] getDataFromPartFile(long r6) {
        /*
            r5 = this;
            java.lang.String r0 = "getDataFromPartFile is.close() error: "
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "content://mms/part/"
            r1.append(r2)
            r1.append(r6)
            java.lang.String r6 = r1.toString()
            android.net.Uri r6 = android.net.Uri.parse(r6)
            java.io.ByteArrayOutputStream r7 = new java.io.ByteArrayOutputStream
            r7.<init>()
            r1 = 0
            com.sec.internal.ims.cmstore.helper.TelephonyDbHelper r2 = r5.mTeleDBHelper     // Catch:{ IOException -> 0x0078, all -> 0x005b }
            java.io.InputStream r6 = r2.getInputStream(r6)     // Catch:{ IOException -> 0x0078, all -> 0x005b }
            if (r6 == 0) goto L_0x003b
            r2 = 256(0x100, float:3.59E-43)
            byte[] r2 = new byte[r2]     // Catch:{ IOException -> 0x0079, all -> 0x0038 }
            int r3 = r6.read(r2)     // Catch:{ IOException -> 0x0079, all -> 0x0038 }
        L_0x002d:
            if (r3 < 0) goto L_0x003b
            r4 = 0
            r7.write(r2, r4, r3)     // Catch:{ IOException -> 0x0079, all -> 0x0038 }
            int r3 = r6.read(r2)     // Catch:{ IOException -> 0x0079, all -> 0x0038 }
            goto L_0x002d
        L_0x0038:
            r7 = move-exception
            r1 = r6
            goto L_0x005c
        L_0x003b:
            if (r6 == 0) goto L_0x0056
            r6.close()     // Catch:{ IOException -> 0x0041 }
            goto L_0x0056
        L_0x0041:
            r6 = move-exception
            java.lang.String r5 = r5.LOG_TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            r1.append(r0)
            r1.append(r6)
            java.lang.String r6 = r1.toString()
            android.util.Log.e(r5, r6)
        L_0x0056:
            byte[] r5 = r7.toByteArray()
            return r5
        L_0x005b:
            r7 = move-exception
        L_0x005c:
            if (r1 == 0) goto L_0x0077
            r1.close()     // Catch:{ IOException -> 0x0062 }
            goto L_0x0077
        L_0x0062:
            r6 = move-exception
            java.lang.String r5 = r5.LOG_TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            r1.append(r0)
            r1.append(r6)
            java.lang.String r6 = r1.toString()
            android.util.Log.e(r5, r6)
        L_0x0077:
            throw r7
        L_0x0078:
            r6 = r1
        L_0x0079:
            if (r6 == 0) goto L_0x0094
            r6.close()     // Catch:{ IOException -> 0x007f }
            goto L_0x0094
        L_0x007f:
            r6 = move-exception
            java.lang.String r5 = r5.LOG_TAG
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            r7.<init>()
            r7.append(r0)
            r7.append(r6)
            java.lang.String r6 = r7.toString()
            android.util.Log.e(r5, r6)
        L_0x0094:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.omanetapi.bufferdbtranslation.BufferDBSupportTranslation.getDataFromPartFile(long):byte[]");
    }

    private byte[] getFileContentInBytes(Uri uri, CloudMessageBufferDBConstants.PayloadEncoding payloadEncoding) {
        InputStream openInputStream;
        if (uri == null) {
            return null;
        }
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            try {
                openInputStream = this.mContext.getContentResolver().openInputStream(uri);
                if (openInputStream == null) {
                    String str = this.LOG_TAG;
                    Log.e(str, "URI open failed!!!! Uri = " + uri);
                    if (openInputStream != null) {
                        openInputStream.close();
                    }
                    byteArrayOutputStream.close();
                    return null;
                }
                String str2 = this.LOG_TAG;
                Log.i(str2, "FileUri  ==> " + uri);
                byte[] bArr = new byte[256];
                int read = openInputStream.read(bArr);
                while (read >= 0) {
                    byteArrayOutputStream.write(bArr, 0, read);
                    read = openInputStream.read(bArr);
                }
                String str3 = this.LOG_TAG;
                Log.i(str3, "getFileContentInBytes: " + uri + " " + payloadEncoding + " bytes " + read + " getVVMGreetingPayloadFromPath, all bytes: " + byteArrayOutputStream.size());
                if (CloudMessageBufferDBConstants.PayloadEncoding.Base64.equals(payloadEncoding)) {
                    byte[] encode = Base64.encode(byteArrayOutputStream.toByteArray(), 0);
                    openInputStream.close();
                    byteArrayOutputStream.close();
                    return encode;
                }
                byte[] byteArray = byteArrayOutputStream.toByteArray();
                openInputStream.close();
                byteArrayOutputStream.close();
                return byteArray;
            } catch (Throwable th) {
                byteArrayOutputStream.close();
                throw th;
            }
        } catch (IOException e) {
            String str4 = this.LOG_TAG;
            Log.e(str4, "getVVMGreetingPayloadFromPath :: " + e.getMessage());
            return null;
        } catch (Throwable th2) {
            th.addSuppressed(th2);
        }
        throw th;
    }

    /* access modifiers changed from: protected */
    public byte[] getFileContentInBytes(String str, CloudMessageBufferDBConstants.PayloadEncoding payloadEncoding) {
        FileInputStream fileInputStream;
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            try {
                fileInputStream = new FileInputStream(str);
                byte[] bArr = new byte[256];
                int read = fileInputStream.read(bArr);
                String str2 = this.LOG_TAG;
                Log.i(str2, "getFileContentInBytes: read:" + read);
                while (read >= 0) {
                    byteArrayOutputStream.write(bArr, 0, read);
                    read = fileInputStream.read(bArr);
                }
                String str3 = this.LOG_TAG;
                Log.i(str3, "getFileContentInBytes: " + str + " " + payloadEncoding + " bytes " + read + " getRcsFilePayloadFromPath, all bytes: " + byteArrayOutputStream.size());
                if (CloudMessageBufferDBConstants.PayloadEncoding.Base64.equals(payloadEncoding)) {
                    byte[] encode = Base64.encode(byteArrayOutputStream.toByteArray(), 0);
                    fileInputStream.close();
                    byteArrayOutputStream.close();
                    return encode;
                }
                byte[] byteArray = byteArrayOutputStream.toByteArray();
                fileInputStream.close();
                byteArrayOutputStream.close();
                return byteArray;
            } catch (Throwable th) {
                byteArrayOutputStream.close();
                throw th;
            }
            throw th;
        } catch (IOException e) {
            String str4 = this.LOG_TAG;
            Log.e(str4, "getFileContentInBytes :: " + e.getMessage());
            e.printStackTrace();
            return null;
        } catch (Throwable th2) {
            th.addSuppressed(th2);
        }
    }

    private void setTransToFrom(AttributeTranslator attributeTranslator, Set<String> set, String str) {
        String msisdn = ImsUri.parse(str).getMsisdn();
        attributeTranslator.setDirection(new String[]{"IN"});
        attributeTranslator.setFrom(new String[]{msisdn});
        String str2 = this.LOG_TAG;
        Log.i(str2, "parsed address : " + IMSLog.checker(msisdn) + " participants size: " + set.size());
        if (set.size() <= 1) {
            set.clear();
        } else {
            set.remove(msisdn);
        }
        set.add(getE164FormatNumber(this.mStoreClient.getPrerenceManager().getUserCtn()));
        attributeTranslator.setTo((String[]) set.toArray(new String[set.size()]));
    }

    /* access modifiers changed from: protected */
    public String getE164FormatNumber(String str) {
        return getE164FormatNumber(str, "US");
    }

    /* access modifiers changed from: protected */
    public String getE164FormatNumber(String str, String str2) {
        String str3 = this.LOG_TAG;
        Log.d(str3, "getE164FormatNumber: old[" + IMSLog.checker(str) + "]");
        PhoneNumberUtil instance = PhoneNumberUtil.getInstance();
        try {
            Phonenumber$PhoneNumber parse = instance.parse(str, str2);
            if ((!str2.equalsIgnoreCase("KR") || (!str.contains("*") && !str.contains("#"))) && parse != null) {
                String format = instance.format(parse, PhoneNumberUtil.PhoneNumberFormat.E164);
                String str4 = this.LOG_TAG;
                Log.d(str4, "getE164FormatNumber: E164[" + IMSLog.checker(format) + "]");
                return format;
            }
        } catch (NumberParseException | NullPointerException e) {
            PrintStream printStream = System.err;
            printStream.println("NumberParseException was thrown: " + e);
        }
        return str;
    }

    /* access modifiers changed from: protected */
    public String getTelE164FormatNumber(String str, String str2) {
        String str3 = this.LOG_TAG;
        Log.d(str3, "getTelE164FormatNumber: old[" + IMSLog.checker(str) + "]");
        PhoneNumberUtil instance = PhoneNumberUtil.getInstance();
        try {
            Phonenumber$PhoneNumber parse = instance.parse(str, str2);
            if (str2.equalsIgnoreCase("KR")) {
                if (str.contains("*") || str.contains("#")) {
                    return "tel:" + str;
                }
            }
            if (parse != null) {
                String format = instance.format(parse, PhoneNumberUtil.PhoneNumberFormat.E164);
                String str4 = this.LOG_TAG;
                Log.d(str4, "getTelE164FormatNumber: E164[" + IMSLog.checker(format) + "]");
                return "tel:" + format;
            }
        } catch (NumberParseException | NullPointerException e) {
            PrintStream printStream = System.err;
            printStream.println("NumberParseException was thrown: " + e.toString());
        }
        return str;
    }

    protected static class MmsParticipant {
        Set<String> mBcc;
        Set<String> mCc;
        Set<String> mFrom;
        Set<String> mTo;

        MmsParticipant(Set<String> set, Set<String> set2, Set<String> set3, Set<String> set4) {
            this.mFrom = set;
            this.mTo = set2;
            this.mBcc = set3;
            this.mCc = set4;
        }
    }
}
