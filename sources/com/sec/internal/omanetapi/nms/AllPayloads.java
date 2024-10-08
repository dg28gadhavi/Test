package com.sec.internal.omanetapi.nms;

import android.net.Uri;
import android.util.Log;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.log.IMSLog;

public class AllPayloads extends BaseNMSRequest {
    public static final String TAG = AllPayloads.class.getSimpleName();
    private static final long serialVersionUID = 6070003579804341648L;
    private String mObjectId;

    public AllPayloads(String str, String str2, String str3, String str4, String str5, MessageStoreClient messageStoreClient) {
        super(str, str2, str3, str4, messageStoreClient);
        this.mObjectId = str5;
        buildAPISpecificURLFromBase();
    }

    public AllPayloads(String str, MessageStoreClient messageStoreClient) {
        super(str, messageStoreClient);
    }

    public void initGetRequest() {
        super.initCommonGetRequest();
    }

    public void initGetRequest(String[] strArr) {
        super.initCommonGetRequest();
        setMultipleContentType("Accept", strArr);
    }

    /* access modifiers changed from: protected */
    public void buildAPISpecificURLFromBase() {
        Uri.Builder buildUpon = Uri.parse(this.mBaseUrl).buildUpon();
        buildUpon.appendPath("objects");
        buildUpon.appendPath(this.mObjectId);
        buildUpon.appendPath("payload");
        String uri = buildUpon.build().toString();
        this.mBaseUrl = uri;
        Log.i(TAG, IMSLog.checker(uri));
    }
}
