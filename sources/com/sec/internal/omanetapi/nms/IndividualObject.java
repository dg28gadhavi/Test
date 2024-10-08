package com.sec.internal.omanetapi.nms;

import android.net.Uri;
import android.util.Log;
import com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.interfaces.ims.cmstore.IRetryStackAdapterHelper;
import com.sec.internal.log.IMSLog;

public class IndividualObject extends BaseNMSRequest {
    public static final String TAG = IndividualObject.class.getSimpleName();
    private static final long serialVersionUID = -4932381371123746768L;
    private final String mObjectId;

    public HttpRequestParams getRetryInstance(SyncMsgType syncMsgType, IAPICallFlowListener iAPICallFlowListener, MessageStoreClient messageStoreClient, ICloudMessageManagerHelper iCloudMessageManagerHelper, IRetryStackAdapterHelper iRetryStackAdapterHelper) {
        return this;
    }

    public IndividualObject(String str, String str2, String str3, String str4, String str5, MessageStoreClient messageStoreClient) {
        super(str, str2, str3, str4, messageStoreClient);
        this.mObjectId = str5;
        buildAPISpecificURLFromBase();
    }

    public void initGetRequest() {
        super.initCommonGetRequest();
    }

    public void initDeleteRequest() {
        super.initCommonDeleteRequest();
    }

    /* access modifiers changed from: protected */
    public void buildAPISpecificURLFromBase() {
        Uri.Builder buildUpon = Uri.parse(this.mBaseUrl).buildUpon();
        buildUpon.appendPath("objects");
        buildUpon.appendPath(this.mObjectId);
        String uri = buildUpon.build().toString();
        this.mBaseUrl = uri;
        Log.i(TAG, IMSLog.checker(uri));
    }
}
