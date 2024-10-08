package com.sec.internal.ims.entitlement.nsds.app.flow.ericssonnsds;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import com.sec.ims.extensions.ContextExt;
import com.sec.internal.constants.ims.entitilement.NSDSErrorTranslator;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.helper.os.IntentUtil;
import com.sec.internal.ims.entitlement.nsds.ericssonnsds.flow.BaseFlowImpl;
import com.sec.internal.ims.entitlement.storagehelper.NSDSDatabaseHelper;
import com.sec.internal.interfaces.ims.entitlement.nsds.ISimSwapFlow;
import com.sec.internal.interfaces.ims.entitlement.nsds.SimSwapCompletedListener;
import java.util.ArrayList;

public class SimSwapFlow extends NSDSAppFlowBase implements ISimSwapFlow {
    private static final String LOG_TAG = SimSwapFlow.class.getSimpleName();
    private SimSwapCompletedListener mSimSwapCompletedListener;

    /* access modifiers changed from: protected */
    public void queueOperation(int i, Bundle bundle) {
    }

    public SimSwapFlow(Looper looper, Context context, BaseFlowImpl baseFlowImpl, NSDSDatabaseHelper nSDSDatabaseHelper) {
        super(looper, context, baseFlowImpl, nSDSDatabaseHelper);
    }

    public void handleSimSwap(SimSwapCompletedListener simSwapCompletedListener) {
        this.mSimSwapCompletedListener = simSwapCompletedListener;
        Log.i(LOG_TAG, "handleSimSwap....");
        notifyNSDSFlowResponse(true, (String) null, -1, -1);
    }

    /* access modifiers changed from: protected */
    public void notifyNSDSFlowResponse(boolean z, String str, int i, int i2) {
        String str2 = LOG_TAG;
        Log.i(str2, "notifyNSDSFlowResponse: success " + z);
        ArrayList arrayList = new ArrayList();
        if (!(z || str == null || i2 == -1)) {
            arrayList.add(Integer.valueOf(NSDSErrorTranslator.translate(str, i, i2)));
        }
        Intent intent = new Intent(NSDSNamespaces.NSDSActions.SIM_SWAP_COMPLETED);
        intent.putExtra(NSDSNamespaces.NSDSExtras.REQUEST_STATUS, z);
        intent.putIntegerArrayListExtra(NSDSNamespaces.NSDSExtras.ERROR_CODES, arrayList);
        IntentUtil.sendBroadcast(this.mContext, intent, ContextExt.CURRENT_OR_SELF);
        SimSwapCompletedListener simSwapCompletedListener = this.mSimSwapCompletedListener;
        if (simSwapCompletedListener != null) {
            simSwapCompletedListener.onSimSwapCompleted();
        }
    }
}
