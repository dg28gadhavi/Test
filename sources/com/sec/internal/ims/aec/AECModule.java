package com.sec.internal.ims.aec;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SemSystemProperties;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.SparseArray;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.aec.AECNamespace;
import com.sec.internal.constants.ims.os.NetworkEvent;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.aec.receiver.SmsNotification;
import com.sec.internal.ims.aec.util.DefaultNetwork;
import com.sec.internal.ims.aec.workflow.WorkflowFactory;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.interfaces.ims.aec.IAECModule;
import com.sec.internal.interfaces.ims.aec.IWorkflowImpl;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.log.AECLog;
import java.util.ArrayList;
import java.util.Arrays;

public class AECModule extends Handler implements IAECModule {
    private static final String PROPERTY_ICC_TYPE = "ril.ICC_TYPE";
    private final String LOG_TAG = AECModule.class.getSimpleName();
    private final AECResult mAECResult;
    private final Context mContext;
    private final DefaultNetwork mDefaultNetwork;
    private final WorkflowFactory mWorkflowFactory;

    public AECModule(Looper looper, Context context) {
        super(looper);
        this.mContext = context;
        this.mAECResult = new AECResult(context);
        this.mDefaultNetwork = new DefaultNetwork(context, this);
        this.mWorkflowFactory = WorkflowFactory.getInstance();
        sendEmptyMessage(8);
    }

    public void initSequentially() {
        for (ISimManager iSimManager : SimManagerFactory.getAllSimManagers()) {
            iSimManager.registerForSimReady(this, 1, (Object) null);
            iSimManager.registerForSimRemoved(this, 2, (Object) null);
        }
        SmsNotification smsNotification = new SmsNotification(this.mContext, this);
        this.mContext.registerReceiver(smsNotification, smsNotification.getIntentFilter());
    }

    public void handleMessage(Message message) {
        switch (message.what) {
            case 1:
                onSimReady(((Integer) ((AsyncResult) message.obj).result).intValue());
                return;
            case 2:
                onSimRemoved(((Integer) ((AsyncResult) message.obj).result).intValue());
                return;
            case 3:
                onChangeConnectivity();
                return;
            case 4:
                onStartEntitlement(message);
                return;
            case 5:
                onCompletedEntitlement(message);
                return;
            case 6:
                onStopEntitlement(message);
                return;
            case 7:
                onReceivedSmsNotification(message);
                return;
            case 8:
                onClearHttpResponse();
                return;
            default:
                return;
        }
    }

    private void onStartEntitlement(Message message) {
        IWorkflowImpl workflow = this.mWorkflowFactory.getWorkflow(message.arg1);
        if (workflow != null) {
            workflow.performEntitlement((Object) null);
        }
    }

    private void onCompletedEntitlement(Message message) {
        IWorkflowImpl workflow;
        if (message != null && (workflow = this.mWorkflowFactory.getWorkflow(message.arg1)) != null) {
            if (workflow.getEntitlementForVoLte()) {
                this.mAECResult.handleCompletedEntitlementVoLTE(message);
            }
            if (workflow.getEntitlementForVoWiFi()) {
                if (!workflow.getEntitlementInitFromApp()) {
                    this.mAECResult.handleCompletedEntitlementVoWIFI(message);
                } else if (workflow.isReadyToNotifyApp()) {
                    workflow.setReadyToNotifyApp(false);
                    this.mAECResult.handleCompletedEntitlementVoWIFI(message);
                }
            }
            this.mAECResult.updateAkaToken(message.arg1, message.arg2);
        }
    }

    private void onStopEntitlement(Message message) {
        if (message != null) {
            this.mAECResult.updateAkaToken(message.arg1, message.arg2);
        }
    }

    private void onChangeConnectivity() {
        SparseArray<IWorkflowImpl> allWorkflow = this.mWorkflowFactory.getAllWorkflow();
        for (int i = 0; i < allWorkflow.size(); i++) {
            IWorkflowImpl iWorkflowImpl = allWorkflow.get(allWorkflow.keyAt(i));
            if (iWorkflowImpl != null) {
                iWorkflowImpl.changeConnectivity();
            }
        }
    }

    private void onReceivedSmsNotification(Message message) {
        IWorkflowImpl workflow = this.mWorkflowFactory.getWorkflow(message.arg1);
        if (workflow != null) {
            workflow.receivedSmsNotification((String) message.obj);
        }
    }

    private void onSimReady(int i) {
        if (!isSimAbsent(i) && isEntitlementRequired(i)) {
            AECLog.d(this.LOG_TAG, "onSimReady", i);
            IWorkflowImpl workflow = this.mWorkflowFactory.getWorkflow(i);
            if (workflow != null) {
                workflow.clearResource();
                this.mWorkflowFactory.clearWorkflow(i);
            }
            createWorkflow(i);
            this.mDefaultNetwork.registerDefaultNetworkCallback();
        }
    }

    private void onSimRemoved(int i) {
        IWorkflowImpl workflow = this.mWorkflowFactory.getWorkflow(i);
        if (workflow != null) {
            AECLog.d(this.LOG_TAG, "onSimRemoved", i);
            workflow.clearResource();
            this.mAECResult.setAkaTokenReady(i, false);
            this.mWorkflowFactory.clearWorkflow(i);
            this.mDefaultNetwork.unregisterNetworkCallback();
        }
    }

    private boolean isSimAbsent(int i) {
        ISimManager iSimManager = (ISimManager) SimManagerFactory.getAllSimManagers().get(i);
        return iSimManager != null && (iSimManager.hasNoSim() || iSimManager.hasVsim());
    }

    private void createWorkflow(int i) {
        ISimManager simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(i);
        if (simManagerFromSimSlot != null && !TextUtils.isEmpty(simManagerFromSimSlot.getImsi())) {
            if (this.mWorkflowFactory.createWorkflow(this.mContext, i, simManagerFromSimSlot.getImsi(), simManagerFromSimSlot.getSimMnoName(), getEntitlementServer(i), this)) {
                sendMessage(obtainMessage(4, i, 0, 0));
            }
        }
    }

    private String getEntitlementServer(int i) {
        return ImsRegistry.getString(i, GlobalSettingsConstants.Entitlement.SUPPORT_CONFIGSERVER, "");
    }

    private void onClearHttpResponse() {
        int parseInt;
        int phoneCount = SimUtil.getPhoneCount();
        for (int i = 0; i < phoneCount; i++) {
            SharedPreferences sharedPreferences = this.mContext.getSharedPreferences(String.format(AECNamespace.Template.AEC_RESULT, new Object[]{Integer.valueOf(i)}), 0);
            if (sharedPreferences != null && ((parseInt = Integer.parseInt(sharedPreferences.getString(AECNamespace.Path.RESPONSE, "0"))) == 400 || parseInt == 403 || parseInt == 500)) {
                SharedPreferences.Editor edit = sharedPreferences.edit();
                edit.putString(AECNamespace.Path.RESPONSE, "0");
                edit.commit();
                AECLog.i(this.LOG_TAG, "onClearHttpResponse: " + parseInt, i);
            }
        }
    }

    public boolean isEntitlementRequired(int i) {
        if (!"2".equals(TelephonyManager.getTelephonyProperty(PROPERTY_ICC_TYPE + i, "0"))) {
            AECLog.i(this.LOG_TAG, "unsupported icc type", i);
            return false;
        } else if (DmConfigHelper.getImsSwitchValue(this.mContext, DeviceConfigManager.IMS, i) != 1) {
            AECLog.i(this.LOG_TAG, "disabled ImsSwitch", i);
            return false;
        } else {
            ISimManager simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(i);
            if ("Bouygues_FR".equalsIgnoreCase(simManagerFromSimSlot != null ? simManagerFromSimSlot.getSimMnoName() : "") && !"BOG".equalsIgnoreCase(OmcCode.getNWCode(i))) {
                AECLog.i(this.LOG_TAG, "E/S: All models only on BOG SW, but not others like XEF", i);
                return false;
            } else if (isEntitlementDisabled(i)) {
                AECLog.i(this.LOG_TAG, "no needed to support Entitlement Server from new U OS");
                return false;
            } else if ("ts43".equalsIgnoreCase(getEntitlementServer(i)) || "nsds_eur".equalsIgnoreCase(getEntitlementServer(i))) {
                return true;
            } else {
                return false;
            }
        }
    }

    private boolean isEntitlementDisabled(int i) {
        ISimManager simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(i);
        if (simManagerFromSimSlot == null || simManagerFromSimSlot.getSimMno() != Mno.TELEFONICA_UK) {
            return false;
        }
        int i2 = SemSystemProperties.getInt(ImsConstants.SystemProperties.FIRST_API_VERSION, 0);
        boolean z = new ArrayList(Arrays.asList(new String[]{"SM-S711", "SM-A256"})).stream().filter(new AECModule$$ExternalSyntheticLambda0()).count() > 0;
        String str = this.LOG_TAG;
        AECLog.i(str, "firstSdkVersion : " + i2 + ", isDisabledModel : " + z);
        if (i2 >= 34 || z) {
            return true;
        }
        return false;
    }

    public boolean getEntitlementForVoLte(int i) {
        IWorkflowImpl workflow = this.mWorkflowFactory.getWorkflow(i);
        if (workflow != null) {
            return workflow.getEntitlementForVoLte();
        }
        return false;
    }

    public boolean getEntitlementForVoWiFi(int i) {
        IWorkflowImpl workflow = this.mWorkflowFactory.getWorkflow(i);
        if (workflow != null) {
            return workflow.getEntitlementForVoWiFi();
        }
        return false;
    }

    public boolean getEntitlementForSMSoIp(int i) {
        IWorkflowImpl workflow = this.mWorkflowFactory.getWorkflow(i);
        if (workflow != null) {
            return workflow.getEntitlementForSMSoIp();
        }
        return false;
    }

    public boolean getSMSoIpEntitlementStatus(int i) {
        IWorkflowImpl workflow = this.mWorkflowFactory.getWorkflow(i);
        if (workflow != null) {
            return workflow.getSMSoIpEntitlementStatus();
        }
        return false;
    }

    public boolean getVoLteEntitlementStatus(int i) {
        IWorkflowImpl workflow = this.mWorkflowFactory.getWorkflow(i);
        if (workflow != null) {
            return workflow.getVoLteEntitlementStatus();
        }
        return false;
    }

    public boolean getVoWiFiEntitlementStatus(int i) {
        IWorkflowImpl workflow = this.mWorkflowFactory.getWorkflow(i);
        if (workflow != null) {
            return workflow.getVoWiFiEntitlementStatus();
        }
        return false;
    }

    public String getAkaToken(int i) {
        IWorkflowImpl workflow = this.mWorkflowFactory.getWorkflow(i);
        if (workflow == null) {
            return AECNamespace.AkaAuthResultType.AKA_NOT_PROCESS;
        }
        String akaToken = workflow.getAkaToken();
        if (workflow.isEntitlementOngoing()) {
            workflow.setSharedAkaToken(false);
            this.mAECResult.setAkaTokenReady(i, true);
        } else if (TextUtils.isEmpty(akaToken) || workflow.isSharedAkaToken()) {
            workflow.clearAkaToken();
            workflow.setSharedAkaToken(false);
            workflow.setValidEntitlement(false);
            workflow.performEntitlement((Object) null);
            this.mAECResult.setAkaTokenReady(i, true);
        } else {
            workflow.setSharedAkaToken(true);
            return akaToken;
        }
        return "InProgress";
    }

    public void dump() {
        SparseArray<IWorkflowImpl> allWorkflow = this.mWorkflowFactory.getAllWorkflow();
        for (int i = 0; i < allWorkflow.size(); i++) {
            IWorkflowImpl iWorkflowImpl = allWorkflow.get(allWorkflow.keyAt(i));
            if (iWorkflowImpl != null) {
                iWorkflowImpl.dump();
            }
        }
    }

    public void onNetworkEventChanged(NetworkEvent networkEvent, NetworkEvent networkEvent2, int i) {
        if (networkEvent2.isDataRoaming && !networkEvent.isDataRoaming) {
            sendMessage(obtainMessage(4, i, 0, 0));
        }
    }

    public void triggerAutoConfigForApp(int i) {
        IWorkflowImpl workflow = this.mWorkflowFactory.getWorkflow(i);
        if (workflow != null) {
            workflow.triggerAutoConfigForApp();
        }
    }
}
