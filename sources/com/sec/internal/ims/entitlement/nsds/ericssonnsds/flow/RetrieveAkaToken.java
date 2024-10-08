package com.sec.internal.ims.entitlement.nsds.ericssonnsds.flow;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.constants.ims.entitilement.data.NSDSRequest;
import com.sec.internal.constants.ims.entitilement.data.NSDSResponse;
import com.sec.internal.constants.ims.entitilement.data.Response3gppAuthentication;
import com.sec.internal.ims.entitlement.storagehelper.NSDSHelper;
import java.util.concurrent.atomic.AtomicInteger;

public class RetrieveAkaToken extends NSDSBaseProcedure {
    private static final String LOG_TAG = RetrieveAkaToken.class.getSimpleName();
    private String mClientId;
    private String mDeviceGroup;
    private String mMSISDN;
    private String mPushToken;
    private String mServiceName;
    private String mVIMSI;

    public RetrieveAkaToken(Looper looper, Context context, BaseFlowImpl baseFlowImpl, Messenger messenger, String str) {
        super(looper, context, baseFlowImpl, messenger, str);
        Log.i(LOG_TAG, "created.");
    }

    public NSDSRequest[] buildRequests(NSDSCommonParameters nSDSCommonParameters) {
        return new NSDSRequest[]{this.mBaseFlowImpl.getNSDSClient().buildAuthenticationRequest(new AtomicInteger().incrementAndGet(), true, nSDSCommonParameters.getChallengeResponse(), nSDSCommonParameters.getAkaToken(), (String) null, nSDSCommonParameters.getImsiEap(), nSDSCommonParameters.getDeviceId())};
    }

    /* access modifiers changed from: protected */
    public Message getResponseMessage() {
        return Message.obtain((Handler) null, 118);
    }

    /* access modifiers changed from: protected */
    public void executeOperationWithChallenge() {
        this.mBaseFlowImpl.performOperation(47, this, new Messenger(this));
    }

    /* access modifiers changed from: protected */
    public boolean processResponse(Bundle bundle) {
        String str = LOG_TAG;
        Log.i(str, "processResponse for akatoken");
        if (bundle == null) {
            return true;
        }
        Response3gppAuthentication response3gppAuthentication = (Response3gppAuthentication) bundle.getParcelable(NSDSNamespaces.NSDSMethodNamespace.REQ_3GPP_AUTH);
        if (response3gppAuthentication != null) {
            Log.i(str, "response3gppAuthentication responseCode:" + response3gppAuthentication.responseCode);
        }
        if (!retryForServerError(new NSDSResponse[]{response3gppAuthentication})) {
            return true;
        }
        Log.i(str, "processResponse - server error");
        return false;
    }

    public void retrieveAkaToken(String str, String str2, long j) {
        this.mRetryInterval = j;
        retrieveAkaToken(NSDSNamespaces.NSDSSettings.DEVICE_GROUP_20, str, str2, NSDSHelper.getVIMSIforSIMDevice(this.mContext, this.mBaseFlowImpl.getSimManager().getImsi()), this.mBaseFlowImpl.getSimManager().getMsisdn());
    }

    public void retrieveAkaToken(String str, String str2, String str3, String str4, String str5) {
        this.mVIMSI = str4;
        this.mMSISDN = str5;
        this.mDeviceGroup = str;
        this.mServiceName = NSDSNamespaces.NSDSServices.SERVICE_CONNECTIVITY_MANAGER;
        this.mClientId = str2;
        this.mPushToken = str3;
        executeOperationWithChallenge();
    }
}
