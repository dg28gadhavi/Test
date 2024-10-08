package com.sec.internal.ims.cmstore;

import android.util.Log;
import android.util.Pair;
import com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface;
import com.sec.internal.interfaces.ims.cmstore.IRetryStackAdapterHelper;

public class RetryMapAdapterHelper implements IRetryStackAdapterHelper {
    private static final String TAG = "RetryMapAdapterHelper";

    public boolean checkRequestRetried(IHttpAPICommonInterface iHttpAPICommonInterface) {
        return false;
    }

    public IHttpAPICommonInterface getLastFailedRequest() {
        return null;
    }

    public boolean isEmpty() {
        return false;
    }

    public boolean isRetryTimesFinished() {
        return false;
    }

    public IHttpAPICommonInterface pop() {
        return null;
    }

    public void retryApi(IHttpAPICommonInterface iHttpAPICommonInterface, IAPICallFlowListener iAPICallFlowListener, ICloudMessageManagerHelper iCloudMessageManagerHelper, IRetryStackAdapterHelper iRetryStackAdapterHelper) {
    }

    public boolean retryLastApi(IAPICallFlowListener iAPICallFlowListener, ICloudMessageManagerHelper iCloudMessageManagerHelper, IRetryStackAdapterHelper iRetryStackAdapterHelper) {
        return false;
    }

    public void saveRetryLastFailedTime(long j) {
    }

    public boolean searchAndPush(Pair<IHttpAPICommonInterface, SyncMsgType> pair, int i, MessageStoreClient messageStoreClient) {
        return false;
    }

    public boolean searchAndPush(IHttpAPICommonInterface iHttpAPICommonInterface) {
        return false;
    }

    public void clearRetryHistory() {
        Log.i(TAG, "Special Helper Clear History");
    }
}
