package com.sec.internal.ims.config;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.config.params.ACSConfig;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.rcs.RcsPolicyManager;
import com.sec.internal.ims.servicemodules.im.strategy.ChnStrategy;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.interfaces.ims.config.IStorageAdapter;
import com.sec.internal.interfaces.ims.config.IWorkflow;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.log.IMSLog;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

class WorkFlowController {
    private static final String LOG_TAG = "WorkFlowController";
    private final Map<Integer, ACSConfig> mAcsConfigs = new ConcurrentHashMap();
    private final Context mContext;
    private final SparseArray<String> mImsiList = new SparseArray<>();
    private final SparseArray<String> mMsisdnList = new SparseArray<>();
    private final SparseArray<String> mRcsProfileList = new SparseArray<>();
    private final SparseArray<IWorkflow> mWorkflowList = new SparseArray<>();

    WorkFlowController(Context context) {
        this.mContext = context;
        for (ISimManager iSimManager : SimManagerFactory.getAllSimManagers()) {
            this.mRcsProfileList.put(iSimManager.getSimSlotIndex(), "");
            this.mAcsConfigs.put(Integer.valueOf(iSimManager.getSimSlotIndex()), new ACSConfig());
            if (iSimManager.getSimMno().isKor()) {
                getAcsConfig(iSimManager.getSimSlotIndex()).resetAcsSettings();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public IWorkflow getWorkflow(int i) {
        return this.mWorkflowList.get(i);
    }

    /* access modifiers changed from: package-private */
    public void removeWorkFlow(int i) {
        this.mWorkflowList.remove(i);
    }

    /* access modifiers changed from: package-private */
    public void initWorkflow(int i, IWorkflow iWorkflow) {
        if (iWorkflow != null) {
            this.mWorkflowList.put(i, iWorkflow);
            iWorkflow.init();
            IMSLog.i(LOG_TAG, i, "workflow is created and init");
        }
    }

    /* access modifiers changed from: package-private */
    public ACSConfig getAcsConfig(int i) {
        return this.mAcsConfigs.get(Integer.valueOf(i));
    }

    /* access modifiers changed from: package-private */
    public void putRcsProfile(int i, String str) {
        this.mRcsProfileList.put(i, str);
    }

    /* access modifiers changed from: package-private */
    public String getRcsProfile(int i) {
        return this.mRcsProfileList.get(i);
    }

    /* access modifiers changed from: package-private */
    public void onBootCompleted() {
        for (int i = 0; i < SimUtil.getPhoneCount(); i++) {
            IWorkflow iWorkflow = this.mWorkflowList.get(i);
            if (iWorkflow != null) {
                iWorkflow.onBootCompleted();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onSimRefresh(int i) {
        if (this.mWorkflowList.get(i) != null) {
            this.mWorkflowList.get(i).cleanup();
            IMSLog.i(LOG_TAG, i, "onSimRefresh: remove workflow");
            this.mWorkflowList.remove(i);
        }
    }

    /* access modifiers changed from: package-private */
    public int getMsisdnSkipCount(int i) {
        try {
            if (getWorkflow(i) == null) {
                return 0;
            }
            String str = getWorkflow(i).read(ConfigConstants.PATH.MSISDN_SKIP_COUNT).get(ConfigConstants.PATH.MSISDN_SKIP_COUNT);
            if (TextUtils.isEmpty(str)) {
                return 0;
            }
            int parseInt = Integer.parseInt(str);
            IMSLog.i(LOG_TAG, i, "MsisdnSkipCount : " + parseInt);
            return parseInt;
        } catch (NullPointerException | NumberFormatException e) {
            Log.i(LOG_TAG, "exception on reading config. return 0");
            e.printStackTrace();
            return 0;
        }
    }

    /* access modifiers changed from: package-private */
    public int getCurrentRcsConfigVersion(int i) {
        try {
            if (getWorkflow(i) != null) {
                return Integer.parseInt(getWorkflow(i).read("root/vers/version").get("root/vers/version"));
            }
            return 0;
        } catch (NullPointerException | NumberFormatException e) {
            Log.i(LOG_TAG, "exception on reading config. return version 0");
            e.printStackTrace();
            return 0;
        }
    }

    /* access modifiers changed from: package-private */
    public void deleteConfiguration(int i, DiagnosisConstants.RCSA_TDRE rcsa_tdre) {
        IWorkflow iWorkflow = this.mWorkflowList.get(i);
        if (iWorkflow != null) {
            iWorkflow.clearAutoConfigStorage(rcsa_tdre);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isSimInfochanged(int i, boolean z) {
        boolean z2;
        ISimManager simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(i);
        if (simManagerFromSimSlot == null) {
            Log.i(LOG_TAG, "changedSimInfo: SimManager null");
            return false;
        }
        String imsi = simManagerFromSimSlot.getImsi();
        if (TextUtils.isEmpty(imsi)) {
            Log.i(LOG_TAG, "changedSimInfo: getImsi null or empty");
            return false;
        }
        String str = this.mImsiList.get(i);
        boolean z3 = true;
        if (TextUtils.equals(str, imsi) || z) {
            imsi = str;
            z2 = false;
        } else {
            IMSLog.i(LOG_TAG, i, "changedSimInfo: imsi is changed, " + toLastString(str) + " ==> " + toLastString(imsi));
            z2 = true;
        }
        this.mImsiList.put(i, imsi);
        Mno simMno = simManagerFromSimSlot.getSimMno();
        if (!(RcsPolicyManager.getRcsStrategy(SimUtil.getSimSlotPriority()) instanceof ChnStrategy) || simMno == null || ConfigUtil.isRcsChn(simMno)) {
            String acsServerType = ConfigUtil.getAcsServerType(i);
            if (simMno == Mno.SPRINT || ImsConstants.RCS_AS.JIBE.equals(acsServerType) || ImsConstants.RCS_AS.SEC.equals(acsServerType) || z) {
                String line1Number = simManagerFromSimSlot.getLine1Number();
                if (line1Number == null) {
                    IMSLog.i(LOG_TAG, i, "changedSimInfo: getLine1Number null");
                    return false;
                }
                String str2 = this.mMsisdnList.get(i);
                if (!TextUtils.equals(str2, line1Number)) {
                    IMSLog.i(LOG_TAG, i, "changedSimInfo: msisdn is changed, " + toLastString(str2) + " ==> " + toLastString(line1Number));
                } else {
                    line1Number = str2;
                    z3 = z2;
                }
                this.mMsisdnList.put(i, line1Number);
                z2 = z3;
            }
            StringBuilder sb = new StringBuilder();
            sb.append("changedSimInfo: ");
            sb.append(z2 ? "changed" : "not changed");
            IMSLog.i(LOG_TAG, i, sb.toString());
            return z2;
        }
        Log.i(LOG_TAG, "changedSimInfo: Non CMCC sim, not suport RCS: " + simMno);
        return false;
    }

    public IStorageAdapter getStorage(int i) {
        IWorkflow iWorkflow = this.mWorkflowList.get(i);
        if (iWorkflow != null) {
            return iWorkflow.getStorage();
        }
        return null;
    }

    private String toLastString(String str) {
        return (str == null || str.length() <= 2) ? "" : str.substring(str.length() - 2);
    }

    public void dump() {
        for (int i = 0; i < SimUtil.getPhoneCount(); i++) {
            Optional.ofNullable(this.mWorkflowList.get(i)).ifPresent(new WorkFlowController$$ExternalSyntheticLambda0());
        }
    }
}
