package com.sec.internal.interfaces.ims.cmstore;

import android.util.Pair;
import com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType;
import com.sec.internal.ims.cmstore.MessageStoreClient;

public interface IRetryStackAdapterHelper {
    boolean checkRequestRetried(IHttpAPICommonInterface iHttpAPICommonInterface);

    void clearRetryHistory();

    IHttpAPICommonInterface getLastFailedRequest();

    boolean isEmpty();

    boolean isRetryTimesFinished();

    IHttpAPICommonInterface pop();

    void retryApi(IHttpAPICommonInterface iHttpAPICommonInterface, IAPICallFlowListener iAPICallFlowListener, ICloudMessageManagerHelper iCloudMessageManagerHelper, IRetryStackAdapterHelper iRetryStackAdapterHelper);

    boolean retryLastApi(IAPICallFlowListener iAPICallFlowListener, ICloudMessageManagerHelper iCloudMessageManagerHelper, IRetryStackAdapterHelper iRetryStackAdapterHelper);

    void saveRetryLastFailedTime(long j);

    boolean searchAndPush(Pair<IHttpAPICommonInterface, SyncMsgType> pair, int i, MessageStoreClient messageStoreClient);

    boolean searchAndPush(IHttpAPICommonInterface iHttpAPICommonInterface);
}
