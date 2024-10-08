package com.sec.internal.ims.entitlement.nsds.ericssonnsds.flow;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import com.sec.internal.constants.ims.entitilement.data.NSDSRequest;
import com.sec.internal.constants.ims.entitilement.data.NSDSResponse;
import com.sec.internal.constants.ims.entitilement.data.ResponseManagePushToken;
import com.sec.internal.ims.entitlement.nsds.ericssonnsds.NSDSClient;
import java.util.concurrent.atomic.AtomicInteger;

public class OperationUsingManagePushToken extends NSDSBaseProcedure {
    private static final String LOG_TAG = OperationUsingManagePushToken.class.getSimpleName();
    private String mClientId;
    private String mMSISDN;
    private int mOperation;
    private String mPushToken;
    private String mServiceName;

    public OperationUsingManagePushToken(Looper looper, Context context, BaseFlowImpl baseFlowImpl, Messenger messenger, String str) {
        super(looper, context, baseFlowImpl, messenger, str);
        Log.i(LOG_TAG, "created.");
    }

    public OperationUsingManagePushToken(Looper looper, Context context, BaseFlowImpl baseFlowImpl, Messenger messenger, String str, String str2, String str3) {
        super(looper, context, baseFlowImpl, messenger, str, str2, str3);
        Log.i(LOG_TAG, "created.");
    }

    public NSDSRequest[] buildRequests(NSDSCommonParameters nSDSCommonParameters) {
        NSDSClient nSDSClient = this.mBaseFlowImpl.getNSDSClient();
        AtomicInteger atomicInteger = new AtomicInteger();
        NSDSClient nSDSClient2 = nSDSClient;
        return new NSDSRequest[]{nSDSClient2.buildAuthenticationRequest(atomicInteger.incrementAndGet(), true, nSDSCommonParameters.getChallengeResponse(), nSDSCommonParameters.getAkaToken(), (String) null, nSDSCommonParameters.getImsiEap(), nSDSCommonParameters.getDeviceId()), nSDSClient2.buildManagePushTokenRequest(atomicInteger.incrementAndGet(), this.mMSISDN, this.mServiceName, this.mClientId, this.mOperation, this.mPushToken, nSDSCommonParameters.getDeviceId())};
    }

    /* access modifiers changed from: protected */
    public boolean processResponse(Bundle bundle) {
        ResponseManagePushToken responseManagePushToken;
        if (bundle != null) {
            responseManagePushToken = (ResponseManagePushToken) bundle.getParcelable("managePushToken");
        } else {
            Log.e(LOG_TAG, "responseCollection is null");
            responseManagePushToken = null;
        }
        return !retryForServerError((NSDSResponse) responseManagePushToken);
    }

    /* access modifiers changed from: protected */
    public Message getResponseMessage() {
        return Message.obtain((Handler) null, this.mOperation == 1 ? 113 : 112);
    }

    /* access modifiers changed from: protected */
    public void executeOperationWithChallenge() {
        if (this.mOperation == 1) {
            this.mBaseFlowImpl.performOperation(42, this, new Messenger(this));
        } else {
            this.mBaseFlowImpl.performOperation(41, this, new Messenger(this));
        }
    }

    public void updatePushToken(String str, String str2, String str3, int i, String str4) {
        this.mMSISDN = str;
        this.mServiceName = str2;
        this.mClientId = str3;
        this.mOperation = i;
        this.mPushToken = str4;
        executeOperationWithChallenge();
    }

    public void registerVoWiFiPushToken(String str, String str2, String str3, String str4, long j) {
        this.mRetryInterval = j;
        updatePushToken(str, str4, str2, 0, str3);
    }

    public void removeVoWiFiPushToken(String str, String str2, String str3, String str4, long j) {
        this.mRetryInterval = j;
        updatePushToken(str, str4, str2, 1, str3);
    }

    public void removeVoWiFiPushToken(String str, String str2, String str3, String str4, String str5, String str6, long j) {
        this.mMSISDN = str;
        this.mServiceName = str4;
        this.mClientId = str2;
        this.mOperation = 1;
        this.mPushToken = str3;
        this.mRetryInterval = j;
        executeOperationWithAkaToken(str6, str5);
    }

    /* access modifiers changed from: protected */
    public void executeOperationWithAkaToken(String str, String str2) {
        this.mBaseFlowImpl.performOperationWithAkaToken(42, str, str2, this, new Messenger(this));
    }
}
