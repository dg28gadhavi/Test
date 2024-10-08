package com.sec.internal.omanetapi.nms;

import android.net.Uri;
import android.util.Log;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.log.IMSLog;

public class ObjectsOpPathToId extends BaseNMSRequest {
    private static final String TAG = ObjectsOpPathToId.class.getSimpleName();

    public ObjectsOpPathToId(String str, String str2, String str3, String str4, MessageStoreClient messageStoreClient) {
        super(str, str2, str3, str4, messageStoreClient);
        buildAPISpecificURLFromBase();
    }

    /* access modifiers changed from: protected */
    public void buildAPISpecificURLFromBase() {
        Uri.Builder buildUpon = Uri.parse(this.mBaseUrl).buildUpon();
        buildUpon.appendPath("objects");
        buildUpon.appendPath("operations");
        buildUpon.appendPath("pathToId");
        String uri = buildUpon.build().toString();
        this.mBaseUrl = uri;
        Log.i(TAG, IMSLog.checker(uri));
    }
}
