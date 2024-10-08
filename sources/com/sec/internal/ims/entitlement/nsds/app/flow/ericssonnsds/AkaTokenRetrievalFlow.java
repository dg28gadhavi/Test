package com.sec.internal.ims.entitlement.nsds.app.flow.ericssonnsds;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import com.sec.ims.extensions.ContextExt;
import com.sec.internal.constants.ims.entitilement.NSDSErrorTranslator;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.constants.ims.entitilement.data.Response3gppAuthentication;
import com.sec.internal.helper.os.IntentUtil;
import com.sec.internal.ims.entitlement.nsds.app.flow.ericssonnsds.NSDSAppFlowBase;
import com.sec.internal.ims.entitlement.nsds.ericssonnsds.flow.BaseFlowImpl;
import com.sec.internal.ims.entitlement.nsds.ericssonnsds.flow.RetrieveAkaToken;
import com.sec.internal.ims.entitlement.nsds.strategy.IMnoNsdsStrategy;
import com.sec.internal.ims.entitlement.storagehelper.NSDSDatabaseHelper;
import com.sec.internal.interfaces.ims.entitlement.nsds.IAkaTokenRetrievalFlow;
import java.util.ArrayList;

public class AkaTokenRetrievalFlow extends NSDSAppFlowBase implements IAkaTokenRetrievalFlow {
    public static final String ACTION_AKA_TOKEN_RETRIEVED = "com.samsung.nsds.action.AKA_TOKEN_RETRIEVED";
    private static final String LOG_TAG = AkaTokenRetrievalFlow.class.getSimpleName();
    private static final int RETRIEVE_AKA_TOKEN = 1;

    public AkaTokenRetrievalFlow(Looper looper, Context context, BaseFlowImpl baseFlowImpl, NSDSDatabaseHelper nSDSDatabaseHelper) {
        super(looper, context, baseFlowImpl, nSDSDatabaseHelper);
    }

    /* access modifiers changed from: protected */
    public NSDSAppFlowBase.NSDSResponseStatus handleAkaTokenRetrievalResponse(Bundle bundle) {
        Response3gppAuthentication response3gppAuthentication;
        int httpErrRespCode = getHttpErrRespCode(bundle);
        String str = LOG_TAG;
        Log.i(str, "handleAkaTokenRetrievalResponse: errorResponseCode: " + httpErrRespCode);
        NSDSAppFlowBase.NSDSResponseStatus nSDSResponseStatus = new NSDSAppFlowBase.NSDSResponseStatus(httpErrRespCode, NSDSNamespaces.NSDSMethodNamespace.REQ_3GPP_AUTH, -1);
        if (!(bundle == null || httpErrRespCode > 0 || (response3gppAuthentication = (Response3gppAuthentication) bundle.getParcelable(NSDSNamespaces.NSDSMethodNamespace.REQ_3GPP_AUTH)) == null)) {
            nSDSResponseStatus.methodName = NSDSNamespaces.NSDSMethodNamespace.REQ_3GPP_AUTH;
            int i = response3gppAuthentication.responseCode;
            nSDSResponseStatus.responseCode = i;
            if (i != 1000) {
                Log.e(str, "Aka Token Retrival failed:");
            }
        }
        return nSDSResponseStatus;
    }

    public void performAkaTokenRetrieval(int i, int i2) {
        Log.i(LOG_TAG, "performAkaTokenRetrieval()");
        this.mDeviceEventType = i;
        this.mRetryCount = i2;
        performNextOperationIf(-1, new NSDSAppFlowBase.NSDSResponseStatus(-1, (String) null, -1), (Bundle) null);
    }

    /* access modifiers changed from: protected */
    public void queueOperation(int i, Bundle bundle) {
        String str = LOG_TAG;
        Log.i(str, "queueOperation: " + i);
        int i2 = i != 15 ? -1 : 1;
        if (i2 != -1) {
            Message obtainMessage = obtainMessage(i2);
            obtainMessage.setData(bundle);
            sendMessage(obtainMessage);
        }
    }

    private void startAkaTokenretrieval() {
        Log.i(LOG_TAG, "startAkaTokenretrieval()");
        IMnoNsdsStrategy mnoNsdsStrategy = getMnoNsdsStrategy();
        new RetrieveAkaToken(getLooper(), this.mContext, this.mBaseFlowImpl, new Messenger(this), "1.0").retrieveAkaToken((String) null, (String) null, mnoNsdsStrategy != null ? mnoNsdsStrategy.getRetryInterval() : 0);
    }

    private int translateErrorCode(boolean z, String str, int i, int i2) {
        if (z || str == null || i2 == -1) {
            return -1;
        }
        return NSDSErrorTranslator.translate(str, i, i2);
    }

    public void handleMessage(Message message) {
        String str = LOG_TAG;
        Log.i(str, "msg:" + message.what);
        int i = message.what;
        if (i == 1) {
            startAkaTokenretrieval();
        } else if (i == 118) {
            performNextOperationIf(15, handleAkaTokenRetrievalResponse(message.getData()), (Bundle) null);
        }
    }

    /* access modifiers changed from: protected */
    public void notifyNSDSFlowResponse(boolean z, String str, int i, int i2) {
        String str2 = LOG_TAG;
        Log.i(str2, "notifyNSDSFlowResponse: success " + z);
        ArrayList arrayList = new ArrayList();
        arrayList.add(Integer.valueOf(translateErrorCode(z, str, i, i2)));
        Intent intent = new Intent("com.samsung.nsds.action.AKA_TOKEN_RETRIEVED");
        intent.putExtra(NSDSNamespaces.NSDSExtras.REQUEST_STATUS, z);
        intent.putIntegerArrayListExtra(NSDSNamespaces.NSDSExtras.ERROR_CODES, arrayList);
        IntentUtil.sendBroadcast(this.mContext, intent, ContextExt.CURRENT_OR_SELF);
    }
}
