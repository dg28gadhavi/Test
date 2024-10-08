package com.sec.internal.ims.cmstore;

import android.util.Pair;
import com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType;
import com.sec.internal.ims.cmstore.adapters.RetryStackAdapter;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface;
import com.sec.internal.interfaces.ims.cmstore.IRetryStackAdapterHelper;

public class RetryStackAdapterHelper implements IRetryStackAdapterHelper {
    public boolean searchAndPush(Pair<IHttpAPICommonInterface, SyncMsgType> pair, int i, MessageStoreClient messageStoreClient) {
        return false;
    }

    public IHttpAPICommonInterface getLastFailedRequest() {
        return RetryStackAdapter.getInstance().getLastFailedRequest();
    }

    public IHttpAPICommonInterface pop() {
        return RetryStackAdapter.getInstance().pop();
    }

    public boolean isEmpty() {
        return RetryStackAdapter.getInstance().isEmpty();
    }

    public void retryApi(IHttpAPICommonInterface iHttpAPICommonInterface, IAPICallFlowListener iAPICallFlowListener, ICloudMessageManagerHelper iCloudMessageManagerHelper, IRetryStackAdapterHelper iRetryStackAdapterHelper) {
        RetryStackAdapter.getInstance().retryApi(iHttpAPICommonInterface, iAPICallFlowListener, iCloudMessageManagerHelper, iRetryStackAdapterHelper);
    }

    public boolean checkRequestRetried(IHttpAPICommonInterface iHttpAPICommonInterface) {
        return RetryStackAdapter.getInstance().checkRequestRetried(iHttpAPICommonInterface);
    }

    public boolean isRetryTimesFinished() {
        return RetryStackAdapter.getInstance().isRetryTimesFinished(new CloudMessageManagerHelper());
    }

    public void clearRetryHistory() {
        RetryStackAdapter.getInstance().clearRetryHistory();
    }

    public boolean searchAndPush(IHttpAPICommonInterface iHttpAPICommonInterface) {
        return RetryStackAdapter.getInstance().searchAndPush(iHttpAPICommonInterface);
    }

    public void saveRetryLastFailedTime(long j) {
        RetryStackAdapter.getInstance().saveRetryLastFailedTime(System.currentTimeMillis());
    }

    public boolean retryLastApi(IAPICallFlowListener iAPICallFlowListener, ICloudMessageManagerHelper iCloudMessageManagerHelper, IRetryStackAdapterHelper iRetryStackAdapterHelper) {
        return RetryStackAdapter.getInstance().retryLastApi(iAPICallFlowListener, iCloudMessageManagerHelper, iRetryStackAdapterHelper);
    }
}
