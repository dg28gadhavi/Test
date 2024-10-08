package com.sec.internal.ims.cmstore.callHandling.successfullCall;

import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.cmstore.ReqConstant;
import com.sec.internal.constants.ims.cmstore.enumprovision.EnumProvision;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.ambs.cloudrequest.ReqSession;
import com.sec.internal.ims.cmstore.ambs.cloudrequest.RequestAccount;
import com.sec.internal.ims.cmstore.ambs.cloudrequest.RequestCreateAccount;
import com.sec.internal.ims.cmstore.ambs.cloudrequest.RequestPat;
import com.sec.internal.ims.cmstore.ambs.provision.ProvisionController;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface;
import com.sec.internal.interfaces.ims.cmstore.IRetryStackAdapterHelper;
import java.util.List;

public class SuccessfulCallHandling {
    public static final String HAPPY_PATH_DEFAULT = "HAP.DEF";
    public static final String TAG = "SuccessfulCallHandling";
    private static final long ZERO_DELAY = 0;

    public static void callHandling(MessageStoreClient messageStoreClient, ProvisionController provisionController, IHttpAPICommonInterface iHttpAPICommonInterface, IRetryStackAdapterHelper iRetryStackAdapterHelper, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        internalCallHandling(messageStoreClient, provisionController, iHttpAPICommonInterface, HAPPY_PATH_DEFAULT, 0, iRetryStackAdapterHelper, iCloudMessageManagerHelper);
    }

    public static void callHandling(MessageStoreClient messageStoreClient, ProvisionController provisionController, IHttpAPICommonInterface iHttpAPICommonInterface, String str, IRetryStackAdapterHelper iRetryStackAdapterHelper, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        internalCallHandling(messageStoreClient, provisionController, iHttpAPICommonInterface, str, 0, iRetryStackAdapterHelper, iCloudMessageManagerHelper);
    }

    private static void internalCallHandling(MessageStoreClient messageStoreClient, ProvisionController provisionController, IHttpAPICommonInterface iHttpAPICommonInterface, String str, long j, IRetryStackAdapterHelper iRetryStackAdapterHelper, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        String str2;
        String str3;
        IHttpAPICommonInterface lastFailedRequest = messageStoreClient.getRetryStackAdapter().getLastFailedRequest();
        if (lastFailedRequest != null && lastFailedRequest.getClass().getSimpleName().equals(iHttpAPICommonInterface.getClass().getSimpleName())) {
            IHttpAPICommonInterface pop = messageStoreClient.getRetryStackAdapter().pop();
            if (pop == null) {
                str3 = null;
            } else {
                str3 = pop.getClass().getSimpleName();
            }
            Log.d(TAG + "[" + messageStoreClient.getClientID() + "]", "API " + str3 + " Pop from Retry Stack");
        }
        int totalRetryCounter = messageStoreClient.getPrerenceManager().getTotalRetryCounter();
        if (messageStoreClient.getRetryStackAdapter().isEmpty() && totalRetryCounter > 0 && isEndOfCallFlow(iHttpAPICommonInterface, str)) {
            Log.d(TAG + "[" + messageStoreClient.getClientID() + "]", "end of call flow. stack is empty. reset the counter to 0");
            messageStoreClient.getPrerenceManager().saveTotalRetryCounter(0);
        }
        if (iHttpAPICommonInterface.getClass().getSimpleName().equalsIgnoreCase(RequestAccount.class.getSimpleName())) {
            Log.d(TAG + "[" + messageStoreClient.getClientID() + "]", "RequestAccount request has no happy path in strategy");
            return;
        }
        StringBuilder sb = new StringBuilder();
        String str4 = TAG;
        sb.append(str4);
        sb.append("[");
        sb.append(messageStoreClient.getClientID());
        sb.append("]");
        Log.d(sb.toString(), "Proceeding Flow:: " + str);
        EnumProvision.ProvisionEventType findEvent = findEvent(messageStoreClient, iHttpAPICommonInterface, str, iCloudMessageManagerHelper);
        if (findEvent != null) {
            provisionController.updateDelay(findEvent.getId(), j);
            return;
        }
        Log.e(str4 + "[" + messageStoreClient.getClientID() + "]", "event is null. end of call flow");
        if (!messageStoreClient.getRetryStackAdapter().isEmpty()) {
            IHttpAPICommonInterface lastFailedRequest2 = messageStoreClient.getRetryStackAdapter().getLastFailedRequest();
            if (lastFailedRequest2 != null) {
                str2 = "Retry API:: " + lastFailedRequest2.getClass().getSimpleName();
                messageStoreClient.getRetryStackAdapter().retryApi(lastFailedRequest2, provisionController, iCloudMessageManagerHelper, iRetryStackAdapterHelper);
            } else {
                str2 = "Retry API:: is null";
            }
            Log.d(str4 + "[" + messageStoreClient.getClientID() + "]", "stack is NOT empty, " + str2);
            return;
        }
        Log.d(str4 + "[" + messageStoreClient.getClientID() + "]", "stack is empty. reset the counter to 0");
        messageStoreClient.getPrerenceManager().saveTotalRetryCounter(0);
    }

    private static boolean isEndOfCallFlow(IHttpAPICommonInterface iHttpAPICommonInterface, String str) {
        String simpleName = iHttpAPICommonInterface.getClass().getSimpleName();
        if (simpleName.equalsIgnoreCase(ReqSession.class.getSimpleName())) {
            return true;
        }
        if ((!simpleName.equalsIgnoreCase(RequestAccount.class.getSimpleName()) || !ReqConstant.HAPPY_PATH_REQACCOUNT_EXIST_USER.equalsIgnoreCase(str)) && !simpleName.equalsIgnoreCase(RequestCreateAccount.class.getSimpleName()) && !simpleName.equalsIgnoreCase(RequestPat.class.getSimpleName())) {
            return false;
        }
        return true;
    }

    private static EnumProvision.ProvisionEventType findEvent(MessageStoreClient messageStoreClient, IHttpAPICommonInterface iHttpAPICommonInterface, String str, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        List<SuccessCallFlow> list;
        if (!(TextUtils.isEmpty(str) || messageStoreClient.getCloudMessageStrategyManager().getStrategy().getSuccessfullCallFlowTranslator() == null || (list = messageStoreClient.getCloudMessageStrategyManager().getStrategy().getSuccessfullCallFlowTranslator().get(iHttpAPICommonInterface.getClass())) == null)) {
            for (SuccessCallFlow successCallFlow : list) {
                if (str.equals(successCallFlow.mFlow)) {
                    return successCallFlow.mProvisionEventType;
                }
            }
        }
        return null;
    }
}
