package com.sec.internal.ims.cmstore.callHandling.errorHandling;

import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.cmstore.CommonErrorName;
import com.sec.internal.constants.ims.cmstore.enumprovision.EnumProvision;
import com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType;
import com.sec.internal.constants.ims.cmstore.utils.CloudMessagePreferenceConstants;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.callHandling.errorHandling.ErrorRule;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.interfaces.ims.cmstore.IControllerCommonInterface;
import com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface;
import com.sec.internal.interfaces.ims.cmstore.IRetryStackAdapterHelper;
import java.util.List;

public class ErrorRuleHandling {
    private static final String TAG = "ErrorRuleHandling";

    public static void handleErrorCode(MessageStoreClient messageStoreClient, IControllerCommonInterface iControllerCommonInterface, IHttpAPICommonInterface iHttpAPICommonInterface, IRetryStackAdapterHelper iRetryStackAdapterHelper, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        handleErrorCode(messageStoreClient, iControllerCommonInterface, iHttpAPICommonInterface, CommonErrorName.DEFAULT_ERROR_TYPE, 0, iRetryStackAdapterHelper, iCloudMessageManagerHelper);
    }

    public static void handleErrorHeader(MessageStoreClient messageStoreClient, IControllerCommonInterface iControllerCommonInterface, IHttpAPICommonInterface iHttpAPICommonInterface, String str, int i, IRetryStackAdapterHelper iRetryStackAdapterHelper, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        handleErrorCode(messageStoreClient, iControllerCommonInterface, iHttpAPICommonInterface, str, i, iRetryStackAdapterHelper, iCloudMessageManagerHelper);
    }

    public static void handleErrorCode(MessageStoreClient messageStoreClient, IControllerCommonInterface iControllerCommonInterface, IHttpAPICommonInterface iHttpAPICommonInterface, String str, IRetryStackAdapterHelper iRetryStackAdapterHelper, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        handleErrorCode(messageStoreClient, iControllerCommonInterface, iHttpAPICommonInterface, str, 0, iRetryStackAdapterHelper, iCloudMessageManagerHelper);
    }

    private static void handleErrorCode(MessageStoreClient messageStoreClient, IControllerCommonInterface iControllerCommonInterface, IHttpAPICommonInterface iHttpAPICommonInterface, String str, int i, IRetryStackAdapterHelper iRetryStackAdapterHelper, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        StringBuilder sb = new StringBuilder();
        String str2 = TAG;
        sb.append(str2);
        sb.append(messageStoreClient.getClientID());
        Log.e(sb.toString(), "retryAfter: " + i + " errorCode: " + str);
        if (!TextUtils.isEmpty(str)) {
            ErrorRule findErrorConfig = findErrorConfig(messageStoreClient, iHttpAPICommonInterface, str, iCloudMessageManagerHelper);
            if (findErrorConfig == null) {
                findErrorConfig = findErrorConfig(messageStoreClient, iHttpAPICommonInterface, CommonErrorName.DEFAULT_ERROR_TYPE, iCloudMessageManagerHelper);
            }
            ErrorRule errorRule = findErrorConfig;
            Log.i(str2 + messageStoreClient.getClientID(), "Failed API name: " + iHttpAPICommonInterface.getClass().getSimpleName() + ", error code: " + errorRule);
            if (errorRule != null) {
                handleError(messageStoreClient, iControllerCommonInterface, iHttpAPICommonInterface, errorRule, i, iRetryStackAdapterHelper, iCloudMessageManagerHelper);
            }
        }
    }

    public static void handleError(MessageStoreClient messageStoreClient, IControllerCommonInterface iControllerCommonInterface, IHttpAPICommonInterface iHttpAPICommonInterface, ErrorRule errorRule, int i, IRetryStackAdapterHelper iRetryStackAdapterHelper, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        long j;
        if (iHttpAPICommonInterface != null) {
            boolean checkRequestRetried = messageStoreClient.getRetryStackAdapter().checkRequestRetried(iHttpAPICommonInterface);
            int totalRetryCounter = messageStoreClient.getPrerenceManager().getTotalRetryCounter();
            if (errorRule.mRetryAttr == ErrorRule.RetryAttribute.RETRY_FORBIDDEN) {
                Log.e(TAG + messageStoreClient.getClientID(), "retry forbidden");
                iControllerCommonInterface.update(errorRule.mFailEvent);
            } else if (messageStoreClient.getRetryStackAdapter().isRetryTimesFinished(iCloudMessageManagerHelper)) {
                StringBuilder sb = new StringBuilder();
                String str = TAG;
                sb.append(str);
                sb.append(messageStoreClient.getClientID());
                Log.e(sb.toString(), "retry time finished");
                if (errorRule.mFailEvent == OMASyncEventType.SYNC_ERR.getId() || errorRule.mFailEvent == EnumProvision.ProvisionEventType.ACCESS_ERR.getId()) {
                    messageStoreClient.getRetryStackAdapter().searchAndPush(iHttpAPICommonInterface);
                    messageStoreClient.getPrerenceManager().removeKey(CloudMessagePreferenceConstants.RETRY_TOTAL_COUNTER);
                    messageStoreClient.getPrerenceManager().removeKey(CloudMessagePreferenceConstants.LAST_RETRY_TIME);
                    Log.i(str + messageStoreClient.getClientID(), "steady state error screen should be displayed. saving retry instance, total counter: " + messageStoreClient.getPrerenceManager().getTotalRetryCounter());
                } else {
                    messageStoreClient.getRetryStackAdapter().clearRetryHistory();
                    Log.i(str + messageStoreClient.getClientID(), "clear retry stack and counter, total counter: " + messageStoreClient.getPrerenceManager().getTotalRetryCounter());
                }
                iControllerCommonInterface.update(errorRule.mFailEvent);
            } else {
                if (checkRequestRetried) {
                    increaseTotalRetryCounter(messageStoreClient, iCloudMessageManagerHelper);
                    totalRetryCounter = messageStoreClient.getPrerenceManager().getTotalRetryCounter();
                    j = (long) messageStoreClient.getCloudMessageStrategyManager().getStrategy().getAdaptedRetrySchedule(totalRetryCounter);
                    messageStoreClient.getRetryStackAdapter().saveRetryLastFailedTime(System.currentTimeMillis());
                } else {
                    j = 0;
                }
                Log.i(TAG + messageStoreClient.getClientID(), "RETRY LOGIC::delay from the schedule: " + j + "RETRY LOGIC::next retry Counter=" + totalRetryCounter + "RETRY LOGIC::retry event is " + errorRule.mRetryAttr);
                messageStoreClient.getRetryStackAdapter().searchAndPush(iHttpAPICommonInterface);
                if (errorRule.mRetryAttr == ErrorRule.RetryAttribute.RETRY_USE_HEADER_VALUE && i > 0) {
                    j = Math.max(j, ((long) i) * 1000);
                }
                iControllerCommonInterface.updateDelayRetry(errorRule.mRetryEvent, j);
            }
        }
    }

    private static synchronized void increaseTotalRetryCounter(MessageStoreClient messageStoreClient, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        synchronized (ErrorRuleHandling.class) {
            messageStoreClient.getPrerenceManager().saveTotalRetryCounter(messageStoreClient.getPrerenceManager().getTotalRetryCounter() + 1);
        }
    }

    private static ErrorRule findErrorConfig(MessageStoreClient messageStoreClient, IHttpAPICommonInterface iHttpAPICommonInterface, String str, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        List<ErrorRule> list;
        if (!(messageStoreClient.getCloudMessageStrategyManager().getStrategy().getFailedCallFlowTranslator() == null || (list = messageStoreClient.getCloudMessageStrategyManager().getStrategy().getFailedCallFlowTranslator().get(iHttpAPICommonInterface.getClass())) == null)) {
            for (ErrorRule errorRule : list) {
                if (str.equals(errorRule.mErrorCode)) {
                    return errorRule;
                }
            }
        }
        return null;
    }

    public static void handleMcsError(IAPICallFlowListener iAPICallFlowListener, MessageStoreClient messageStoreClient, IHttpAPICommonInterface iHttpAPICommonInterface) {
        String str = TAG;
        Log.d(str, "handleMcsError failed api param " + iHttpAPICommonInterface.getClass().getSimpleName());
        messageStoreClient.getMcsRetryMapAdapter().retryApi(iHttpAPICommonInterface, iAPICallFlowListener);
    }
}
