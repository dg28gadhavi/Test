package com.sec.internal.omanetapi.nms;

import android.net.Uri;
import android.util.Log;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.log.IMSLog;

public class IndividualPayload extends BaseNMSRequest {
    public static final String TAG = IndividualPayload.class.getSimpleName();
    private static final long serialVersionUID = 7015982621979245863L;
    private String mObjectId;
    private String mPayloadPartId;

    public IndividualPayload(String str, String str2, String str3, String str4, String str5, String str6, MessageStoreClient messageStoreClient) {
        super(str, str2, str3, str4, messageStoreClient);
        this.mObjectId = str5;
        this.mPayloadPartId = str6;
        buildAPISpecificURLFromBase();
    }

    public IndividualPayload(String str, MessageStoreClient messageStoreClient) {
        super(str, messageStoreClient);
    }

    public void initGetRequest(String[] strArr) {
        super.initCommonGetRequest();
        setMultipleContentType("Accept", strArr);
    }

    public void initGetRequest() {
        super.initCommonGetRequest();
    }

    /* access modifiers changed from: protected */
    public void buildAPISpecificURLFromBase() {
        Uri.Builder buildUpon = Uri.parse(this.mBaseUrl).buildUpon();
        buildUpon.appendPath("objects");
        buildUpon.appendPath(this.mObjectId);
        buildUpon.appendPath("payloadParts");
        buildUpon.appendPath(this.mPayloadPartId);
        String uri = buildUpon.build().toString();
        this.mBaseUrl = uri;
        Log.i(TAG, IMSLog.checker(uri));
    }
}
