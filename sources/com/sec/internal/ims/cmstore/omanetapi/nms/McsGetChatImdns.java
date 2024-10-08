package com.sec.internal.ims.cmstore.omanetapi.nms;

import android.util.Log;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.omanetapi.nms.BaseNMSRequest;
import java.io.IOException;

public class McsGetChatImdns extends BaseNMSRequest {
    public String TAG = McsGetChatImdns.class.getSimpleName();

    /* access modifiers changed from: protected */
    public void buildAPISpecificURLFromBase() {
    }

    public McsGetChatImdns(final IAPICallFlowListener iAPICallFlowListener, String str, final BufferDBChangeParam bufferDBChangeParam, MessageStoreClient messageStoreClient) {
        super(str, messageStoreClient);
        this.TAG += "[" + messageStoreClient.getClientID() + "]";
        initMcsCommonRequestHeaders(this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getContentType(), this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getAuthorizationBearer());
        initCommonGetRequest();
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            /* JADX WARNING: Removed duplicated region for block: B:14:0x007a  */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void onComplete(com.sec.internal.helper.httpclient.HttpResponseParams r5) {
                /*
                    r4 = this;
                    com.sec.internal.ims.cmstore.omanetapi.nms.McsGetChatImdns r0 = com.sec.internal.ims.cmstore.omanetapi.nms.McsGetChatImdns.this
                    java.lang.String r0 = r0.TAG
                    java.lang.StringBuilder r1 = new java.lang.StringBuilder
                    r1.<init>()
                    java.lang.String r2 = "statusCode: "
                    r1.append(r2)
                    int r2 = r5.getStatusCode()
                    r1.append(r2)
                    java.lang.String r1 = r1.toString()
                    android.util.Log.i(r0, r1)
                    int r0 = r5.getStatusCode()
                    r1 = 200(0xc8, float:2.8E-43)
                    r2 = 0
                    if (r0 != r1) goto L_0x0098
                    com.sec.internal.ims.cmstore.omanetapi.nms.McsGetChatImdns r0 = com.sec.internal.ims.cmstore.omanetapi.nms.McsGetChatImdns.this
                    java.lang.String r5 = r5.getDataString()
                    java.lang.String r5 = r0.getDecryptedString(r5)
                    com.google.gson.Gson r0 = new com.google.gson.Gson
                    r0.<init>()
                    java.lang.Class<com.sec.internal.omanetapi.nms.GetImdnList> r1 = com.sec.internal.omanetapi.nms.GetImdnList.class
                    java.lang.Object r5 = r0.fromJson(r5, r1)     // Catch:{ Exception -> 0x005e }
                    com.sec.internal.omanetapi.nms.GetImdnList r5 = (com.sec.internal.omanetapi.nms.GetImdnList) r5     // Catch:{ Exception -> 0x005e }
                    com.sec.internal.omanetapi.nms.data.ImdnList r5 = r5.imdnList     // Catch:{ Exception -> 0x005e }
                    if (r5 == 0) goto L_0x005c
                    com.sec.internal.ims.cmstore.omanetapi.nms.McsGetChatImdns r0 = com.sec.internal.ims.cmstore.omanetapi.nms.McsGetChatImdns.this     // Catch:{ Exception -> 0x005a }
                    java.lang.String r0 = r0.TAG     // Catch:{ Exception -> 0x005a }
                    java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x005a }
                    r1.<init>()     // Catch:{ Exception -> 0x005a }
                    java.lang.String r3 = "ImdnList response: "
                    r1.append(r3)     // Catch:{ Exception -> 0x005a }
                    r1.append(r5)     // Catch:{ Exception -> 0x005a }
                    java.lang.String r1 = r1.toString()     // Catch:{ Exception -> 0x005a }
                    android.util.Log.i(r0, r1)     // Catch:{ Exception -> 0x005a }
                    goto L_0x0078
                L_0x005a:
                    r0 = move-exception
                    goto L_0x0060
                L_0x005c:
                    r5 = r2
                    goto L_0x0078
                L_0x005e:
                    r0 = move-exception
                    r5 = r2
                L_0x0060:
                    com.sec.internal.ims.cmstore.omanetapi.nms.McsGetChatImdns r1 = com.sec.internal.ims.cmstore.omanetapi.nms.McsGetChatImdns.this
                    java.lang.String r1 = r1.TAG
                    java.lang.StringBuilder r3 = new java.lang.StringBuilder
                    r3.<init>()
                    r3.append(r0)
                    java.lang.String r0 = " ImdnList parse exception."
                    r3.append(r0)
                    java.lang.String r0 = r3.toString()
                    android.util.Log.e(r1, r0)
                L_0x0078:
                    if (r5 == 0) goto L_0x0098
                    com.sec.internal.omanetapi.nms.data.Object r0 = new com.sec.internal.omanetapi.nms.data.Object
                    r0.<init>()
                    r0.imdns = r5
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r5 = new com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder
                    r5.<init>()
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r5 = r5.setObject(r0)
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$ActionType r0 = com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB.ActionType.NOTIFICATION_IMDN_DOWNLOADED
                    r5.setActionType(r0)
                    com.sec.internal.ims.cmstore.params.BufferDBChangeParam r0 = r4
                    r5.setBufferDBChangeParam((com.sec.internal.ims.cmstore.params.BufferDBChangeParam) r0)
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB r2 = r5.build()
                L_0x0098:
                    com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener r5 = r2
                    com.sec.internal.ims.cmstore.omanetapi.nms.McsGetChatImdns r4 = com.sec.internal.ims.cmstore.omanetapi.nms.McsGetChatImdns.this
                    r5.onMoveOnToNext(r4, r2)
                    return
                */
                throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.omanetapi.nms.McsGetChatImdns.AnonymousClass1.onComplete(com.sec.internal.helper.httpclient.HttpResponseParams):void");
            }

            public void onFail(IOException iOException) {
                String str = McsGetChatImdns.this.TAG;
                Log.e(str, "Http request onFail: " + iOException.getMessage());
                iAPICallFlowListener.onFailedCall(this);
            }
        });
    }
}
