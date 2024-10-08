package com.sec.internal.ims.cmstore.omanetapi.nms;

import android.util.Log;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.omanetapi.clouddatasynchandler.BaseDataChangeHandler;
import com.sec.internal.ims.cmstore.omanetapi.synchandler.BaseSyncHandler;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB;
import com.sec.internal.ims.cmstore.utils.Util;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.omanetapi.nms.AllPayloads;
import java.io.IOException;
import java.util.ArrayList;

public class CloudMessageGetAllPayloads extends AllPayloads {
    private static final long serialVersionUID = 1;
    public String TAG = CloudMessageGetAllPayloads.class.getSimpleName();

    public static CloudMessageGetAllPayloads buildFromPayloadUrl(IAPICallFlowListener iAPICallFlowListener, String str, BufferDBChangeParam bufferDBChangeParam, MessageStoreClient messageStoreClient) {
        return new CloudMessageGetAllPayloads(str, iAPICallFlowListener, bufferDBChangeParam, messageStoreClient);
    }

    private CloudMessageGetAllPayloads(String str, IAPICallFlowListener iAPICallFlowListener, BufferDBChangeParam bufferDBChangeParam, MessageStoreClient messageStoreClient) {
        super(str, messageStoreClient);
        this.TAG += "[" + messageStoreClient.getClientID() + "]";
        this.mBaseUrl = Util.replaceUrlPrefix(this.mBaseUrl, messageStoreClient);
        buildInternal(iAPICallFlowListener, bufferDBChangeParam);
    }

    private void buildInternal(final IAPICallFlowListener iAPICallFlowListener, final BufferDBChangeParam bufferDBChangeParam) {
        String validTokenByLine = this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getValidTokenByLine(bufferDBChangeParam.mLine);
        if (this.isCmsMcsEnabled) {
            initMcsCommonRequestHeaders(this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getContentType(), this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getAuthorizationBearer());
        } else {
            initCommonRequestHeaders(this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getContentType(), validTokenByLine);
        }
        initGetRequest();
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams httpResponseParams) {
                ParamOMAresponseforBufDB paramOMAresponseforBufDB;
                if (httpResponseParams.getStatusCode() != 401 || !CloudMessageGetAllPayloads.this.handleUnAuthorized(httpResponseParams)) {
                    if (httpResponseParams.getStatusCode() == 206) {
                        httpResponseParams.setStatusCode(200);
                    }
                    if (httpResponseParams.getStatusCode() == 404) {
                        paramOMAresponseforBufDB = new ParamOMAresponseforBufDB.Builder().setActionType(ParamOMAresponseforBufDB.ActionType.OBJECT_NOT_FOUND).setBufferDBChangeParam(bufferDBChangeParam).build();
                    } else if (httpResponseParams.getStatusCode() != 200) {
                        paramOMAresponseforBufDB = null;
                    } else if (httpResponseParams.getDataBinary() == null || httpResponseParams.getDataString() == null) {
                        iAPICallFlowListener.onFailedCall(this, bufferDBChangeParam);
                        return;
                    } else {
                        ArrayList arrayList = new ArrayList();
                        CloudMessageGetAllPayloads.this.parseResponsePayload(httpResponseParams, arrayList);
                        if (arrayList.size() < 1) {
                            iAPICallFlowListener.onFailedCall(this, bufferDBChangeParam);
                            return;
                        }
                        ParamOMAresponseforBufDB.Builder bufferDBChangeParam = new ParamOMAresponseforBufDB.Builder().setAllPayloads(arrayList).setBufferDBChangeParam(bufferDBChangeParam);
                        IAPICallFlowListener iAPICallFlowListener = iAPICallFlowListener;
                        if (iAPICallFlowListener instanceof BaseSyncHandler) {
                            bufferDBChangeParam.setActionType(ParamOMAresponseforBufDB.ActionType.ALL_PAYLOAD_DOWNLOAD);
                        } else if (iAPICallFlowListener instanceof BaseDataChangeHandler) {
                            bufferDBChangeParam.setActionType(ParamOMAresponseforBufDB.ActionType.NOTIFICATION_ALL_PAYLOAD_DOWNLOADED);
                        }
                        paramOMAresponseforBufDB = bufferDBChangeParam.build();
                    }
                    if (CloudMessageGetAllPayloads.this.shouldCareAfterResponsePreProcess(iAPICallFlowListener, httpResponseParams, paramOMAresponseforBufDB, bufferDBChangeParam, Integer.MIN_VALUE)) {
                        iAPICallFlowListener.onMoveOnToNext(CloudMessageGetAllPayloads.this, paramOMAresponseforBufDB);
                    }
                }
            }

            public void onFail(IOException iOException) {
                String str = CloudMessageGetAllPayloads.this.TAG;
                Log.e(str, "Http request onFail: " + iOException.getMessage());
                if (CloudMessageGetAllPayloads.this.isCmsMcsEnabled) {
                    iAPICallFlowListener.onFailedCall(this);
                } else {
                    iAPICallFlowListener.onFailedCall(this, bufferDBChangeParam);
                }
            }
        });
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x0098 A[SYNTHETIC, Splitter:B:34:0x0098] */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x00a1 A[Catch:{ MessagingException -> 0x012b }] */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x00e3 A[LOOP:1: B:45:0x00e3->B:48:0x00e9, LOOP_START, PHI: r4 
      PHI: (r4v1 int) = (r4v0 int), (r4v2 int) binds: [B:44:0x00e1, B:48:0x00e9] A[DONT_GENERATE, DONT_INLINE], SYNTHETIC, Splitter:B:45:0x00e3] */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x00ff  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void parseResponsePayload(com.sec.internal.helper.httpclient.HttpResponseParams r9, java.util.List<javax.mail.BodyPart> r10) {
        /*
            r8 = this;
            javax.mail.util.ByteArrayDataSource r0 = new javax.mail.util.ByteArrayDataSource
            byte[] r1 = r9.getDataBinary()
            java.lang.String r2 = "multipart/related"
            r0.<init>((byte[]) r1, (java.lang.String) r2)
            java.util.Map r1 = r9.getHeaders()
            if (r1 == 0) goto L_0x0130
            java.util.Map r1 = r9.getHeaders()
            boolean r1 = r1.isEmpty()
            if (r1 == 0) goto L_0x001d
            goto L_0x0130
        L_0x001d:
            java.util.Map r1 = r9.getHeaders()
            java.lang.String r2 = "content-type"
            java.lang.Object r1 = r1.get(r2)
            java.util.List r1 = (java.util.List) r1
            if (r1 == 0) goto L_0x0031
            boolean r2 = r1.isEmpty()
            if (r2 == 0) goto L_0x003d
        L_0x0031:
            java.util.Map r1 = r9.getHeaders()
            java.lang.String r2 = "Content-type"
            java.lang.Object r1 = r1.get(r2)
            java.util.List r1 = (java.util.List) r1
        L_0x003d:
            if (r1 == 0) goto L_0x0045
            boolean r2 = r1.isEmpty()
            if (r2 == 0) goto L_0x0051
        L_0x0045:
            java.util.Map r1 = r9.getHeaders()
            java.lang.String r2 = "Content-Type"
            java.lang.Object r1 = r1.get(r2)
            java.util.List r1 = (java.util.List) r1
        L_0x0051:
            java.lang.String r2 = "boundary="
            r3 = 1
            r4 = 0
            if (r1 == 0) goto L_0x007c
            boolean r5 = r1.isEmpty()
            if (r5 == 0) goto L_0x005e
            goto L_0x007c
        L_0x005e:
            r5 = r4
        L_0x005f:
            int r6 = r1.size()
            if (r5 >= r6) goto L_0x007c
            java.lang.Object r6 = r1.get(r5)
            if (r6 == 0) goto L_0x0079
            java.lang.Object r6 = r1.get(r5)
            java.lang.String r6 = (java.lang.String) r6
            boolean r6 = r6.contains(r2)
            if (r6 == 0) goto L_0x0079
            r1 = r3
            goto L_0x007d
        L_0x0079:
            int r5 = r5 + 1
            goto L_0x005f
        L_0x007c:
            r1 = r4
        L_0x007d:
            java.lang.String r9 = r9.getDataString()
            java.lang.String r9 = r8.getDecryptedString(r9)
            boolean r2 = r9.contains(r2)
            if (r2 != 0) goto L_0x0095
            java.lang.String r2 = "--"
            boolean r2 = r9.contains(r2)
            if (r2 == 0) goto L_0x0094
            goto L_0x0095
        L_0x0094:
            r3 = r1
        L_0x0095:
            r1 = 0
            if (r3 == 0) goto L_0x00a1
            javax.mail.internet.MimeMultipart r9 = new javax.mail.internet.MimeMultipart     // Catch:{ MessagingException -> 0x012b }
            r9.<init>((javax.activation.DataSource) r0)     // Catch:{ MessagingException -> 0x012b }
            r7 = r1
            r1 = r9
            r9 = r7
            goto L_0x00e1
        L_0x00a1:
            java.lang.String r0 = "Content-type:"
            int r0 = r9.indexOf(r0)     // Catch:{ MessagingException -> 0x012b }
            if (r0 >= 0) goto L_0x00af
            java.lang.String r0 = "Content-Type:"
            int r0 = r9.indexOf(r0)     // Catch:{ MessagingException -> 0x012b }
        L_0x00af:
            if (r0 >= 0) goto L_0x00b7
            java.lang.String r0 = "content-type:"
            int r0 = r9.indexOf(r0)     // Catch:{ MessagingException -> 0x012b }
        L_0x00b7:
            java.lang.String r2 = r8.TAG     // Catch:{ MessagingException -> 0x012b }
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ MessagingException -> 0x012b }
            r3.<init>()     // Catch:{ MessagingException -> 0x012b }
            java.lang.String r5 = "mimebodystart: "
            r3.append(r5)     // Catch:{ MessagingException -> 0x012b }
            r3.append(r0)     // Catch:{ MessagingException -> 0x012b }
            java.lang.String r3 = r3.toString()     // Catch:{ MessagingException -> 0x012b }
            android.util.Log.i(r2, r3)     // Catch:{ MessagingException -> 0x012b }
            if (r0 < 0) goto L_0x012a
            java.lang.String r9 = r9.substring(r0)     // Catch:{ MessagingException -> 0x012b }
            java.io.ByteArrayInputStream r0 = new java.io.ByteArrayInputStream     // Catch:{ MessagingException -> 0x012b }
            byte[] r9 = r9.getBytes()     // Catch:{ MessagingException -> 0x012b }
            r0.<init>(r9)     // Catch:{ MessagingException -> 0x012b }
            javax.mail.internet.MimeBodyPart r9 = new javax.mail.internet.MimeBodyPart     // Catch:{ MessagingException -> 0x012b }
            r9.<init>(r0)     // Catch:{ MessagingException -> 0x012b }
        L_0x00e1:
            if (r1 == 0) goto L_0x00ff
        L_0x00e3:
            int r8 = r1.getCount()     // Catch:{ MessagingException -> 0x00f7 }
            if (r4 >= r8) goto L_0x00f3
            javax.mail.BodyPart r8 = r1.getBodyPart(r4)     // Catch:{ MessagingException -> 0x00f7 }
            r10.add(r8)     // Catch:{ MessagingException -> 0x00f7 }
            int r4 = r4 + 1
            goto L_0x00e3
        L_0x00f3:
            r10.size()     // Catch:{ MessagingException -> 0x00f7 }
            return
        L_0x00f7:
            r8 = move-exception
            r10.clear()
            r8.printStackTrace()
            return
        L_0x00ff:
            if (r9 == 0) goto L_0x0127
            java.lang.String r8 = r8.TAG     // Catch:{ MessagingException -> 0x011f }
            java.lang.StringBuilder r0 = new java.lang.StringBuilder     // Catch:{ MessagingException -> 0x011f }
            r0.<init>()     // Catch:{ MessagingException -> 0x011f }
            java.lang.String r1 = "mimebodypart: "
            r0.append(r1)     // Catch:{ MessagingException -> 0x011f }
            java.lang.String r1 = r9.getContentType()     // Catch:{ MessagingException -> 0x011f }
            r0.append(r1)     // Catch:{ MessagingException -> 0x011f }
            java.lang.String r0 = r0.toString()     // Catch:{ MessagingException -> 0x011f }
            android.util.Log.i(r8, r0)     // Catch:{ MessagingException -> 0x011f }
            r10.add(r9)
            goto L_0x012a
        L_0x011f:
            r8 = move-exception
            r10.clear()
            r8.printStackTrace()
            return
        L_0x0127:
            r10.clear()
        L_0x012a:
            return
        L_0x012b:
            r8 = move-exception
            r8.printStackTrace()
            return
        L_0x0130:
            java.lang.String r8 = r8.TAG
            java.lang.String r9 = "Header is null"
            android.util.Log.i(r8, r9)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageGetAllPayloads.parseResponsePayload(com.sec.internal.helper.httpclient.HttpResponseParams, java.util.List):void");
    }
}
