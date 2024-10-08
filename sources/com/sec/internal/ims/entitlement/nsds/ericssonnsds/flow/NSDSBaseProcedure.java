package com.sec.internal.ims.entitlement.nsds.ericssonnsds.flow;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.text.TextUtils;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.constants.ims.entitilement.data.NSDSRequest;
import com.sec.internal.constants.ims.entitilement.data.NSDSResponse;
import com.sec.internal.constants.ims.entitilement.data.Response3gppAuthentication;
import com.sec.internal.ims.entitlement.nsds.strategy.IMnoNsdsStrategy;
import com.sec.internal.ims.entitlement.nsds.strategy.MnoNsdsStrategyCreator;
import com.sec.internal.log.IMSLog;

public abstract class NSDSBaseProcedure extends Handler {
    protected static final int BASE_OP_MAX_RETRY = 4;
    public static final int EXEC_ENTITLEMENT_OP_WITH_CHALLENGE = 2;
    private static final String LOG_TAG = NSDSBaseProcedure.class.getSimpleName();
    public static final int RESPONSE_RECEIVED = 1;
    protected BaseFlowImpl mBaseFlowImpl;
    protected Context mContext;
    protected String mImeiForUA;
    protected Messenger mMessenger;
    protected int mRetryCount = 0;
    protected long mRetryInterval = 0;
    protected String mUserAgent;
    protected String mVersion;

    public abstract NSDSRequest[] buildRequests(NSDSCommonParameters nSDSCommonParameters);

    /* access modifiers changed from: protected */
    public abstract void executeOperationWithChallenge();

    /* access modifiers changed from: protected */
    public abstract Message getResponseMessage();

    /* access modifiers changed from: protected */
    public abstract boolean processResponse(Bundle bundle);

    public NSDSBaseProcedure(Looper looper, Context context, BaseFlowImpl baseFlowImpl, Messenger messenger, String str) {
        super(looper);
        this.mContext = context;
        this.mBaseFlowImpl = baseFlowImpl;
        this.mMessenger = messenger;
        this.mVersion = str;
        this.mUserAgent = null;
        this.mImeiForUA = null;
    }

    public NSDSBaseProcedure(Looper looper, Context context, BaseFlowImpl baseFlowImpl, Messenger messenger, String str, String str2, String str3) {
        super(looper);
        this.mContext = context;
        this.mBaseFlowImpl = baseFlowImpl;
        this.mMessenger = messenger;
        this.mVersion = str;
        this.mUserAgent = str2;
        this.mImeiForUA = str3;
    }

    public boolean isResponseAkaChallenge(Response3gppAuthentication response3gppAuthentication) {
        return response3gppAuthentication != null && response3gppAuthentication.responseCode == 1003;
    }

    /* access modifiers changed from: protected */
    public Response3gppAuthentication getResponse3gppAuthenticatoin(Bundle bundle) {
        if (bundle != null) {
            return (Response3gppAuthentication) bundle.getParcelable(NSDSNamespaces.NSDSMethodNamespace.REQ_3GPP_AUTH);
        }
        return null;
    }

    public void handleMessage(Message message) {
        int i = message.what;
        boolean z = true;
        if (i != 1) {
            if (i != 2) {
                IMSLog.i(LOG_TAG, "Unknown flow request: " + message.what);
                return;
            }
            executeOperationWithChallenge();
        } else if (isResponseAkaChallenge(getResponse3gppAuthenticatoin(message.getData()))) {
            Message message2 = (Message) message.getData().getParcelable(BaseFlowImpl.KEY_REQUEST_MESSAGE);
            Response3gppAuthentication response3gppAuthenticatoin = getResponse3gppAuthenticatoin(message.getData());
            boolean z2 = message2 != null && message2.arg1 == 1;
            if (response3gppAuthenticatoin == null || TextUtils.isEmpty(response3gppAuthenticatoin.akaChallenge)) {
                z = false;
            }
            if (message2 == null || z2 || !z) {
                reportResult(message.getData());
            } else {
                this.mBaseFlowImpl.resubmitWithChallenge(message2, response3gppAuthenticatoin);
            }
        } else if (processResponse(message.getData())) {
            reportResult(message.getData());
        }
    }

    private void reportResult(Bundle bundle) {
        try {
            if (this.mMessenger != null) {
                Message responseMessage = getResponseMessage();
                responseMessage.setData(bundle);
                this.mMessenger.send(responseMessage);
                return;
            }
            IMSLog.i(LOG_TAG, "mMessenger is null:");
        } catch (RemoteException e) {
            String str = LOG_TAG;
            IMSLog.s(str, "Could not send response to the caller" + e.getMessage());
        }
    }

    /* access modifiers changed from: protected */
    public boolean retryForServerError(NSDSResponse nSDSResponse) {
        String str = LOG_TAG;
        IMSLog.i(str, "retryForServerError:" + this.mRetryCount);
        if (nSDSResponse == null || nSDSResponse.responseCode != 1111 || this.mRetryCount >= 4) {
            if (!(nSDSResponse == null || nSDSResponse.responseCode == 1041)) {
                this.mRetryCount = 0;
            }
            return false;
        }
        IMSLog.i(str, "Failed with server error");
        this.mRetryCount++;
        sendEmptyMessageDelayed(2, this.mRetryInterval);
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean retryForServerError(NSDSResponse[] nSDSResponseArr) {
        boolean z;
        int length = nSDSResponseArr.length;
        int i = 0;
        while (true) {
            if (i >= length) {
                z = false;
                break;
            } else if (isServerErrror(nSDSResponseArr[i])) {
                z = true;
                break;
            } else {
                i++;
            }
        }
        IMnoNsdsStrategy mnoVSimStrategy = getMnoVSimStrategy();
        if (!z || mnoVSimStrategy == null || this.mRetryCount >= mnoVSimStrategy.getBaseOperationMaxRetry()) {
            this.mRetryCount = 0;
            return false;
        }
        IMSLog.i(LOG_TAG, "Failed with server error. Retrying count:" + this.mRetryCount);
        this.mRetryCount = this.mRetryCount + 1;
        executeOperationWithChallenge();
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean isServerErrror(NSDSResponse nSDSResponse) {
        return nSDSResponse != null && nSDSResponse.responseCode == 1111;
    }

    /* access modifiers changed from: protected */
    public String getVersionInfo() {
        return this.mVersion;
    }

    /* access modifiers changed from: protected */
    public String getUserAgent() {
        return this.mUserAgent;
    }

    /* access modifiers changed from: protected */
    public String getImeiForUA() {
        return this.mImeiForUA;
    }

    /* access modifiers changed from: protected */
    public IMnoNsdsStrategy getMnoVSimStrategy() {
        return MnoNsdsStrategyCreator.getInstance(this.mContext, this.mBaseFlowImpl.getSimManager().getSimSlotIndex()).getMnoStrategy();
    }
}
