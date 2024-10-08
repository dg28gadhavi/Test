package com.sec.internal.ims.cmstore.strategy;

import android.content.Context;
import android.util.Log;
import com.samsung.android.feature.SemCscFeature;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.os.SecFeature;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.helper.ATTGlobalVariables;
import com.sec.internal.ims.cmstore.utils.CmsUtil;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageStrategy;

public class CloudMessageStrategyManager {
    private String TAG = CloudMessageStrategyManager.class.getSimpleName();
    private ICloudMessageStrategy mCloudMessageStrategy;
    private Context mContext;
    private final Object mLock = new Object();
    private MessageStoreClient mStoreClient;

    public CloudMessageStrategyManager(MessageStoreClient messageStoreClient) {
        this.mContext = messageStoreClient.getContext();
        this.mStoreClient = messageStoreClient;
        this.TAG += "[" + messageStoreClient.getClientID() + "]";
    }

    public void createStrategy() {
        synchronized (this.mLock) {
            Mno simMno = SimUtil.getSimMno(this.mStoreClient.getClientID());
            if (simMno != null) {
                String str = this.TAG;
                Log.d(str, "Carrier: " + simMno.toString());
            }
            if (Mno.ATT.equals(simMno)) {
                if (getEnableATTCloudService()) {
                    this.mCloudMessageStrategy = new ATTCmStrategy(this.mStoreClient);
                    ATTGlobalVariables.initVersionName();
                    if (getATTDualSimCapability()) {
                        ATTGlobalVariables.setAmbsDualSimSupport(true);
                    }
                } else {
                    this.mCloudMessageStrategy = new DefaultCloudMessageStrategy(this.mStoreClient);
                }
            } else if (Mno.TMOUS.equals(simMno)) {
                this.mCloudMessageStrategy = new TMOCmStrategy(this.mStoreClient);
            } else if (CmsUtil.isMcsSupported(this.mContext, this.mStoreClient.getClientID())) {
                this.mCloudMessageStrategy = new KorCmStrategy(this.mStoreClient);
            } else {
                this.mCloudMessageStrategy = new DefaultCloudMessageStrategy(this.mStoreClient);
                Log.e(this.TAG, "Unsupported Carrier");
            }
        }
    }

    public ICloudMessageStrategy getStrategy() {
        ICloudMessageStrategy iCloudMessageStrategy;
        synchronized (this.mLock) {
            if (this.mCloudMessageStrategy == null) {
                this.mCloudMessageStrategy = new DefaultCloudMessageStrategy(this.mStoreClient);
            }
            iCloudMessageStrategy = this.mCloudMessageStrategy;
        }
        return iCloudMessageStrategy;
    }

    public boolean getEnableATTCloudService() {
        if (ATTGlobalVariables.PHASE_AMBS_SERVICE.contains(SemCscFeature.getInstance().getString(SecFeature.CSC.TAG_CSCFEATURE_MESSAGE_CONFIGOPBACKUPSYNC))) {
            return true;
        }
        Log.d(this.TAG, "Temp sim swap or CSC not enable");
        return false;
    }

    public boolean getATTDualSimCapability() {
        return SemCscFeature.getInstance().getString(SecFeature.CSC.TAG_CSCFEATURE_MESSAGE_MSTORE_DUAL_SIM).equals(CloudMessageProviderContract.JsonData.TRUE);
    }
}
