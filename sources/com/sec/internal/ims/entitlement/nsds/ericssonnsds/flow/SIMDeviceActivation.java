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
import com.sec.internal.constants.ims.entitilement.data.ResponseGetMSISDN;
import com.sec.internal.constants.ims.entitilement.data.ResponseManageConnectivity;
import com.sec.internal.constants.ims.entitilement.data.ResponseManagePushToken;
import com.sec.internal.ims.entitlement.nsds.ericssonnsds.NSDSClient;
import com.sec.internal.ims.entitlement.storagehelper.NSDSHelper;
import java.util.concurrent.atomic.AtomicInteger;

public class SIMDeviceActivation extends NSDSBaseProcedure {
    private static final String LOG_TAG = SIMDeviceActivation.class.getSimpleName();
    private String mClientId;
    private String mDeviceGroup;
    private String mMSISDN;
    private String mPushToken;
    private String mServiceName;
    private String mVIMSI;

    public SIMDeviceActivation(Looper looper, Context context, BaseFlowImpl baseFlowImpl, Messenger messenger, String str) {
        super(looper, context, baseFlowImpl, messenger, str);
        Log.i(LOG_TAG, "created.");
    }

    public SIMDeviceActivation(Looper looper, Context context, BaseFlowImpl baseFlowImpl, Messenger messenger, String str, String str2, String str3) {
        super(looper, context, baseFlowImpl, messenger, str, str2, str3);
        Log.i(LOG_TAG, "created.");
    }

    public NSDSRequest[] buildRequests(NSDSCommonParameters nSDSCommonParameters) {
        NSDSClient nSDSClient = this.mBaseFlowImpl.getNSDSClient();
        AtomicInteger atomicInteger = new AtomicInteger();
        NSDSClient nSDSClient2 = nSDSClient;
        return new NSDSRequest[]{nSDSClient2.buildAuthenticationRequest(atomicInteger.incrementAndGet(), true, nSDSCommonParameters.getChallengeResponse(), nSDSCommonParameters.getAkaToken(), (String) null, nSDSCommonParameters.getImsiEap(), nSDSCommonParameters.getDeviceId()), nSDSClient2.buildManageConnectivityRequest(atomicInteger.incrementAndGet(), 0, this.mVIMSI, (String) null, this.mDeviceGroup, (String) null, nSDSCommonParameters.getDeviceId()), nSDSClient2.buildManagePushTokenRequest(atomicInteger.incrementAndGet(), this.mMSISDN, this.mServiceName, this.mClientId, 0, this.mPushToken, nSDSCommonParameters.getDeviceId()), nSDSClient.buildGetMSISDNRequest(atomicInteger.incrementAndGet(), nSDSCommonParameters.getDeviceId())};
    }

    /* access modifiers changed from: protected */
    public Message getResponseMessage() {
        return Message.obtain((Handler) null, 103);
    }

    /* access modifiers changed from: protected */
    public void executeOperationWithChallenge() {
        this.mBaseFlowImpl.performOperation(32, this, new Messenger(this));
    }

    /* access modifiers changed from: protected */
    public boolean processResponse(Bundle bundle) {
        ResponseGetMSISDN responseGetMSISDN;
        ResponseManagePushToken responseManagePushToken;
        ResponseManageConnectivity responseManageConnectivity;
        if (bundle != null) {
            responseManageConnectivity = (ResponseManageConnectivity) bundle.getParcelable(NSDSNamespaces.NSDSMethodNamespace.MANAGE_CONNECTIVITY);
            responseManagePushToken = (ResponseManagePushToken) bundle.getParcelable("managePushToken");
            responseGetMSISDN = (ResponseGetMSISDN) bundle.getParcelable(NSDSNamespaces.NSDSMethodNamespace.GET_MSISDN);
            if (retryForServerError(new NSDSResponse[]{responseGetMSISDN, responseManageConnectivity, responseManagePushToken})) {
                return false;
            }
        } else {
            responseManageConnectivity = null;
            responseGetMSISDN = null;
            responseManagePushToken = null;
        }
        if (responseManageConnectivity == null || responseManagePushToken == null || responseGetMSISDN == null) {
            Log.e(LOG_TAG, "one of the responses is null");
            return true;
        }
        Log.i(LOG_TAG, "handleSimDeviceActivationResponse : responseManageConnectivity respCode:" + responseManageConnectivity.responseCode + "responseManagePushToken respCode:" + responseManagePushToken.responseCode + "responseGetMsisdn respCode:" + responseGetMSISDN.responseCode);
        return true;
    }

    public void activateSIMDevice(String str, String str2, long j) {
        this.mRetryInterval = j;
        activateSIMDevice(NSDSNamespaces.NSDSSettings.DEVICE_GROUP_20, str, str2, NSDSHelper.getVIMSIforSIMDevice(this.mContext, this.mBaseFlowImpl.getSimManager().getImsi()), this.mBaseFlowImpl.getSimManager().getMsisdn());
    }

    public void activateSIMDevice(String str, String str2, String str3, String str4, String str5) {
        this.mVIMSI = str4;
        this.mMSISDN = str5;
        this.mDeviceGroup = str;
        this.mServiceName = NSDSNamespaces.NSDSServices.SERVICE_CONNECTIVITY_MANAGER;
        this.mClientId = str2;
        this.mPushToken = str3;
        executeOperationWithChallenge();
    }
}
