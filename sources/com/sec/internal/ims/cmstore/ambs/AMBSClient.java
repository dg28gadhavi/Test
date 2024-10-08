package com.sec.internal.ims.cmstore.ambs;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.SemSystemProperties;
import android.telephony.PhoneStateListener;
import android.util.Log;
import com.sec.ims.ICentralMsgStoreService;
import com.sec.ims.ICentralMsgStoreServiceListener;
import com.sec.ims.ImsRegistration;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.cmstore.ATTConstants;
import com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.cmstore.CloudMessageProvider;
import com.sec.internal.ims.cmstore.CloudMessageService;
import com.sec.internal.ims.cmstore.JanskyIntentTranslation;
import com.sec.internal.ims.cmstore.MStoreDebugTool;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.NetAPIWorkingStatusController;
import com.sec.internal.ims.cmstore.RetryMapAdapterHelper;
import com.sec.internal.ims.cmstore.RetryStackAdapterHelper;
import com.sec.internal.ims.cmstore.adapters.McsRetryMapAdapter;
import com.sec.internal.ims.cmstore.adapters.RetryMapAdapter;
import com.sec.internal.ims.cmstore.adapters.RetryStackAdapter;
import com.sec.internal.ims.cmstore.cloudmessagebuffer.CloudMessageBufferSchedulingHandler;
import com.sec.internal.ims.cmstore.helper.ATTGlobalVariables;
import com.sec.internal.ims.cmstore.helper.EventLogHelper;
import com.sec.internal.ims.cmstore.mcs.provision.workflow.WorkflowMcs;
import com.sec.internal.ims.cmstore.params.ParamVvmUpdate;
import com.sec.internal.ims.cmstore.servicecontainer.CentralMsgStoreInterface;
import com.sec.internal.ims.cmstore.strategy.CloudMessageStrategyManager;
import com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager;
import com.sec.internal.ims.cmstore.utils.CmsHttpController;
import com.sec.internal.ims.cmstore.utils.CmsUtil;
import com.sec.internal.ims.cmstore.utils.PhoneStateManager;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.gba.GbaServiceModule;
import com.sec.internal.ims.settings.GlobalSettingsManager;
import com.sec.internal.interfaces.ims.IImsFramework;
import com.sec.internal.interfaces.ims.cmstore.IBufferDBEventListener;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.interfaces.ims.cmstore.IMcsFcmPushNotificationListener;
import com.sec.internal.interfaces.ims.cmstore.IUIEventCallback;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.interfaces.ims.core.imslogger.ISignallingNotifier;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AMBSClient implements MessageStoreClient, IBufferDBEventListener, IUIEventCallback {
    /* access modifiers changed from: private */
    public String LOG_TAG;
    private final String LOG_TAG_CN;
    ICentralMsgStoreService.Stub mBinder = new ICentralMsgStoreService.Stub() {
        public void getAccount(int i) throws RemoteException {
        }

        public void getSd(int i, boolean z, String str) throws RemoteException {
        }

        public void manageSd(int i, int i2, String str) throws RemoteException {
        }

        public void notifyUIScreen(String str, int i, String str2, int i2) throws RemoteException {
        }

        public void onDefaultSmsPackageChanged() throws RemoteException {
        }

        public void onDeregistered(ImsRegistration imsRegistration) throws RemoteException {
        }

        public void onRegistered(ImsRegistration imsRegistration) throws RemoteException {
        }

        public void registerCmsProvisioningListenerByPhoneId(ICentralMsgStoreServiceListener iCentralMsgStoreServiceListener, int i) throws RemoteException {
        }

        public void sendTryDeregisterCms(int i) throws RemoteException {
        }

        public void sendTryRegisterCms(int i, String str) throws RemoteException {
        }

        public void startContactSyncActivity(int i, boolean z) throws RemoteException {
        }

        public void unregisterCmsProvisioningListenerByPhoneId(ICentralMsgStoreServiceListener iCentralMsgStoreServiceListener, int i) throws RemoteException {
        }

        public void updateAccountInfo(int i, String str) throws RemoteException {
        }

        public void receivedMessage(String str, String str2) throws RemoteException {
            String r0 = AMBSClient.this.LOG_TAG;
            Log.d(r0, "receivedMessage " + str + ": " + str2);
            if (!AMBSClient.this.isValidAppType(str) || !AMBSClient.this.mNetAPIWorkingController.isCmsProfileActive()) {
                AMBSClient.this.logInvalidAppType();
            } else {
                AMBSClient.this.mCloudMessageScheduler.receivedMessageJson(str2);
            }
        }

        public void sentMessage(String str, String str2) throws RemoteException {
            String r0 = AMBSClient.this.LOG_TAG;
            Log.d(r0, "sentMessage " + str + ": " + str2);
            if (!AMBSClient.this.isValidAppType(str) || !AMBSClient.this.mNetAPIWorkingController.isCmsProfileActive()) {
                AMBSClient.this.logInvalidAppType();
            } else {
                AMBSClient.this.mCloudMessageScheduler.sentMessageJson(str2);
            }
        }

        public void readMessage(String str, String str2) throws RemoteException {
            String r0 = AMBSClient.this.LOG_TAG;
            Log.i(r0, "readMessage " + str + ": " + str2);
            if (!AMBSClient.this.isValidAppType(str) || !AMBSClient.this.mNetAPIWorkingController.isCmsProfileActive()) {
                AMBSClient.this.logInvalidAppType();
            } else {
                AMBSClient.this.mCloudMessageScheduler.readMessageJson(str, str2);
            }
        }

        public void cancelMessage(String str, String str2) throws RemoteException {
            String r2 = AMBSClient.this.LOG_TAG;
            Log.i(r2, "cancelMessage " + str + ": " + str2);
        }

        public void requestMessageProcess(String str, String str2, int i) {
            String r2 = AMBSClient.this.LOG_TAG;
            Log.i(r2, "requestMessageProcess " + str + " function: " + i + ": " + str2);
        }

        public void unReadMessage(String str, String str2) throws RemoteException {
            String r0 = AMBSClient.this.LOG_TAG;
            Log.d(r0, "unReadMessage " + str + ": " + str2);
            if (!AMBSClient.this.isValidAppType(str) || !AMBSClient.this.mNetAPIWorkingController.isCmsProfileActive()) {
                AMBSClient.this.logInvalidAppType();
            } else {
                AMBSClient.this.mCloudMessageScheduler.unReadMessageJson(str2);
            }
        }

        public void deleteMessage(String str, String str2) throws RemoteException {
            String r0 = AMBSClient.this.LOG_TAG;
            Log.i(r0, "deleteMessage " + str + ": " + str2);
            if (!AMBSClient.this.isValidAppType(str) || !AMBSClient.this.mNetAPIWorkingController.isCmsProfileActive()) {
                AMBSClient.this.logInvalidAppType();
            } else {
                AMBSClient.this.mCloudMessageScheduler.deleteMessageJson(str2);
            }
        }

        public void uploadMessage(String str, String str2) throws RemoteException {
            String r0 = AMBSClient.this.LOG_TAG;
            Log.d(r0, "uploadMessage " + str + ": " + str2);
            if (!AMBSClient.this.isValidAppType(str) || !AMBSClient.this.mNetAPIWorkingController.isCmsProfileActive()) {
                AMBSClient.this.logInvalidAppType();
            } else {
                AMBSClient.this.mCloudMessageScheduler.uploadMessageJson(str2);
            }
        }

        public void downloadMessage(String str, String str2) throws RemoteException {
            String r0 = AMBSClient.this.LOG_TAG;
            Log.d(r0, "downloadMessage " + str + ": " + str2);
            if (!AMBSClient.this.isValidAppType(str) || !AMBSClient.this.mNetAPIWorkingController.isCmsProfileActive()) {
                AMBSClient.this.logInvalidAppType();
            } else {
                AMBSClient.this.mCloudMessageScheduler.downloadMessageJson(str2);
            }
        }

        public void requestOperation(int i, int i2, String str, String str2) throws RemoteException {
            String r1 = AMBSClient.this.LOG_TAG;
            Log.i(r1, "requestOperation " + str + " operation: " + i2 + ": " + str2);
        }

        public void wipeOutMessage(String str, String str2) throws RemoteException {
            String r0 = AMBSClient.this.LOG_TAG;
            Log.d(r0, "wipeOutMessage " + str + ": " + str2);
            if (!AMBSClient.this.isValidAppType(str) || !AMBSClient.this.mNetAPIWorkingController.isCmsProfileActive()) {
                AMBSClient.this.logInvalidAppType();
                return;
            }
            AMBSClient.this.getCloudMessageStrategyManager().getStrategy().resetVVMPendingRequestCount();
            AMBSClient.this.mCloudMessageScheduler.wipeOutMessageJson(str2);
        }

        public void onUserEnterApp(String str) throws RemoteException {
            String r0 = AMBSClient.this.LOG_TAG;
            Log.d(r0, "onUserEnterApp " + str);
            if (!AMBSClient.this.isValidSim()) {
                AMBSClient.this.notifyAppUIScreen(ATTConstants.AttAmbsUIScreenNames.EligibilityError_ErrMsg1.getId(), IUIEventCallback.NON_POP_UP, 0);
            } else if (!AMBSClient.this.isValidAppType(str) || !AMBSClient.this.mNetAPIWorkingController.getCmsProfileEnabled()) {
                AMBSClient.this.logInvalidAppType();
            } else {
                AMBSClient.this.mNetAPIWorkingController.setMsgAppForegroundStatus(true);
                AMBSClient.this.mCloudMessageScheduler.onReturnAppFetchingFailedMsg(str);
            }
        }

        public void onUserLeaveApp(String str) throws RemoteException {
            String r0 = AMBSClient.this.LOG_TAG;
            Log.d(r0, "onUserLeaveApp " + str);
            if (!AMBSClient.this.isValidAppType(str) || !AMBSClient.this.mNetAPIWorkingController.getCmsProfileEnabled()) {
                AMBSClient.this.logInvalidAppType();
            } else {
                AMBSClient.this.mNetAPIWorkingController.setMsgAppForegroundStatus(false);
            }
        }

        public boolean onUIButtonProceed(String str, int i, String str2) throws RemoteException {
            String r0 = AMBSClient.this.LOG_TAG;
            Log.d(r0, "onUIButtonProceed " + str + " screenName: " + i + " ,message" + str2);
            if (!AMBSClient.this.isValidSim()) {
                AMBSClient.this.notifyAppUIScreen(ATTConstants.AttAmbsUIScreenNames.EligibilityError_ErrMsg1.getId(), IUIEventCallback.NON_POP_UP, 0);
                return false;
            } else if (AMBSClient.this.isValidAppType(str) && AMBSClient.this.mNetAPIWorkingController.isCmsProfileActive()) {
                return AMBSClient.this.mNetAPIWorkingController.onUIButtonProceed(i, str2);
            } else {
                AMBSClient.this.logInvalidAppType();
                return false;
            }
        }

        public void onBufferDBReadResult(String str, String str2, String str3, String str4, int i, boolean z) throws RemoteException {
            String str5 = "onBufferDBReadResult: " + str + " msgType: " + str2 + " bufferRowID: " + str3 + " appMessageId: " + str4 + " syncAction: " + i + " isSuccess: " + z;
            Log.d(AMBSClient.this.LOG_TAG, str5);
            if (!z) {
                EventLogHelper.add(AMBSClient.this.LOG_TAG, AMBSClient.this.mSlotID, str5);
            }
            if (!AMBSClient.this.isValidAppType(str) || !AMBSClient.this.mNetAPIWorkingController.isCmsProfileActive()) {
                Log.d(AMBSClient.this.LOG_TAG, "ignore");
            } else if (!AMBSClient.this.getCloudMessageStrategyManager().getStrategy().getIsInitSyncIndicatorRequired() || Integer.valueOf(str3).intValue() >= 0) {
                AMBSClient.this.mCloudMessageScheduler.onBufferDBReadResult(str2, str3, str4, i, z);
            } else {
                Log.d(AMBSClient.this.LOG_TAG, "rowID < 0");
                AMBSClient.this.mNetAPIWorkingController.hideIndicator();
            }
        }

        public void onBufferDBReadResultBatch(String str, String str2) throws RemoteException {
            String r0 = AMBSClient.this.LOG_TAG;
            Log.d(r0, "onBufferDBReadResultBatch " + str + ": " + str2);
            if (!AMBSClient.this.isValidAppType(str) || !AMBSClient.this.mNetAPIWorkingController.isCmsProfileActive()) {
                AMBSClient.this.logInvalidAppType();
            } else {
                AMBSClient.this.mCloudMessageScheduler.bufferDbReadBatchMessageJson(str2);
            }
        }

        public void registerCallback(String str, ICentralMsgStoreService iCentralMsgStoreService) throws RemoteException {
            String r1 = AMBSClient.this.LOG_TAG;
            Log.i(r1, "registerCallback " + str);
        }

        public void stopSync(String str, String str2) throws RemoteException {
            String r0 = AMBSClient.this.LOG_TAG;
            Log.d(r0, "stopSync " + str + " " + str2);
            if (!AMBSClient.this.isValidAppType(str) || !AMBSClient.this.mNetAPIWorkingController.isCmsProfileActive()) {
                AMBSClient.this.logInvalidAppType();
            } else {
                AMBSClient.this.mCloudMessageScheduler.stopSync(str, str2);
            }
        }

        public void startFullSync(String str, String str2) throws RemoteException {
            String r0 = AMBSClient.this.LOG_TAG;
            Log.d(r0, "startFullSync " + str + " " + str2);
            if (!AMBSClient.this.isValidAppType(str) || !AMBSClient.this.mNetAPIWorkingController.isCmsProfileActive()) {
                AMBSClient.this.logInvalidAppType();
            } else {
                AMBSClient.this.mCloudMessageScheduler.startFullSync(str, str2);
            }
        }

        public void startDeltaSync(String str, String str2) throws RemoteException {
            String r0 = AMBSClient.this.LOG_TAG;
            Log.d(r0, "startDeltaSync : " + str2);
            if (!AMBSClient.this.isValidAppType(str) || !AMBSClient.this.mNetAPIWorkingController.isCmsProfileActive()) {
                AMBSClient.this.logInvalidAppType();
            } else {
                AMBSClient.this.mCloudMessageScheduler.startDeltaSync(str, str2);
            }
        }

        public void deleteOldLegacyMessage(String str, String str2) throws RemoteException {
            String r2 = AMBSClient.this.LOG_TAG;
            Log.d(r2, "deleteOldLegacyMessage " + str + " thread:" + str2);
        }

        public void resumeSync(String str) throws RemoteException {
            String r2 = AMBSClient.this.LOG_TAG;
            Log.d(r2, "resumeSync " + str);
        }

        public void restartService(String str) throws RemoteException {
            String r0 = AMBSClient.this.LOG_TAG;
            Log.d(r0, "restartService " + str);
            if (AMBSClient.this.mNetAPIWorkingController.isCmsProfileActive()) {
                AMBSClient.this.mNetAPIWorkingController.onRestartService();
            } else {
                AMBSClient.this.logInvalidAppType();
            }
        }

        public void notifyCloudMessageUpdate(String str, String str2, String str3) throws RemoteException {
            String r2 = AMBSClient.this.LOG_TAG;
            Log.d(r2, "notifyCloudMessageUpdate, apptype: " + str + " msgType: " + str2 + " rowIDs: " + str3);
        }

        public void createSession(String str, String str2) throws RemoteException {
            String r0 = AMBSClient.this.LOG_TAG;
            Log.d(r0, "createSession " + str + " chatId: " + str2);
            AMBSClient.this.mCloudMessageScheduler.createSession(str2);
        }

        public void createParticipant(String str, String str2) throws RemoteException {
            String r0 = AMBSClient.this.LOG_TAG;
            Log.i(r0, "createParticipant " + str + " chatId: " + str2);
            AMBSClient.this.mCloudMessageScheduler.createParticipant(str2);
        }

        public void deleteSession(String str, String str2) throws RemoteException {
            String r0 = AMBSClient.this.LOG_TAG;
            Log.d(r0, "deleteSession " + str + " chatId: " + str2);
            AMBSClient.this.mCloudMessageScheduler.deleteSession(str2);
        }

        public void deleteParticipant(String str, String str2) throws RemoteException {
            String r0 = AMBSClient.this.LOG_TAG;
            Log.d(r0, "deleteParticipant " + str + " chatId: " + str2);
            AMBSClient.this.mCloudMessageScheduler.deleteParticipant(str2);
        }

        public void onRCSDBReady(String str) throws RemoteException {
            String r0 = AMBSClient.this.LOG_TAG;
            Log.i(r0, "onRCSDBReady: " + str);
            try {
                JSONObject jSONObject = new JSONObject(str);
                String string = jSONObject.getString(CloudMessageProviderContract.JsonParamTags.CMS_PROFILE_EVENT);
                String string2 = jSONObject.getString(CloudMessageProviderContract.JsonParamTags.SIM_STATUS);
                String r1 = AMBSClient.this.LOG_TAG;
                Log.d(r1, "eventType =" + string + " simStatus = " + string2);
                if (CloudMessageProviderContract.SimStatusValue.SIM_REMOVED.equals(string2)) {
                    AMBSClient.this.mNetAPIWorkingController.setCmsProfileEnabled(false);
                    AMBSClient.this.mJanskyTranslation.onNotifyMessageAppUI(ATTConstants.AttAmbsUIScreenNames.AMBS_SERVICE_DISABLE.getId(), IUIEventCallback.NON_POP_UP, 0);
                    return;
                }
                if (CloudMessageProviderContract.SimStatusValue.SIM_READY.equals(string2) && CloudMessageProviderContract.CmsEventTypeValue.CMS_PROFILE_DISABLE.equals(string) && CmsUtil.isSimChanged(AMBSClient.this.msc)) {
                    AMBSClient.this.getPrerenceManager().clearAll();
                }
                if (CloudMessageProviderContract.CmsEventTypeValue.CMS_PROFILE_ENABLE.equals(string)) {
                    Mno simMno = AMBSClient.this.getSimManager().getSimMno();
                    if (Mno.ATT.equals(simMno) || Mno.TMOUS.equals(simMno)) {
                        boolean isSimChanged = CmsUtil.isSimChanged(AMBSClient.this.msc);
                        String r12 = AMBSClient.this.LOG_TAG;
                        Log.i(r12, "CMS Account Service Stopped/Paused by server  Stop: " + AMBSClient.this.mNetAPIWorkingController.getCmsIsAccountServiceStop() + " Pause: " + AMBSClient.this.mNetAPIWorkingController.getCmsIsAccountServicePause() + " Mno: " + simMno + " isSimChanged: " + isSimChanged);
                        if (!Mno.ATT.equals(simMno) || !ATTGlobalVariables.supportSignedBinary() || !AMBSClient.this.mNetAPIWorkingController.getCmsIsAccountServiceStop()) {
                            boolean cmsProfileEnabled = AMBSClient.this.mNetAPIWorkingController.getCmsProfileEnabled();
                            if (!cmsProfileEnabled) {
                                AMBSClient.this.mJanskyTranslation.onNotifyMessageAppUI(ATTConstants.AttAmbsUIScreenNames.AMBS_SERVICE_ENABLE.getId(), IUIEventCallback.NON_POP_UP, 0);
                                AMBSClient.this.resetParams(simMno);
                                AMBSClient.this.mNetAPIWorkingController.setCmsProfileEnabled(true);
                                AMBSClient.this.mCloudMessageScheduler.onRCSDbReady();
                            }
                            if (AMBSClient.this.getCloudMessageStrategyManager().getStrategy().needToHandleSimSwap() && isSimChanged) {
                                AMBSClient.this.mNetAPIWorkingController.onRestartService();
                            }
                            if (Mno.TMOUS.equals(simMno) && isSimChanged && SemSystemProperties.getInt(ImsConstants.SystemProperties.FIRST_API_VERSION, 0) > 33 && cmsProfileEnabled) {
                                Log.i(AMBSClient.this.LOG_TAG, "TMO Esim hotswap");
                                AMBSClient.this.resetParams(simMno);
                                AMBSClient.this.mNetAPIWorkingController.onEsimHotswap();
                                AMBSClient.this.mCloudMessageScheduler.onRCSDbReady();
                            }
                            if (!ATTGlobalVariables.supportSignedBinary()) {
                                return;
                            }
                            if (Mno.TMOUS.equals(simMno) || (Mno.ATT.equals(simMno) && !AMBSClient.this.mNetAPIWorkingController.getCmsIsAccountServiceStop() && !AMBSClient.this.mNetAPIWorkingController.getCmsIsAccountServicePause())) {
                                AMBSClient.this.mJanskyTranslation.onNotifyMessageAppUI(ATTConstants.AttAmbsUIScreenNames.RestartMenu_Enable_PrmptMsg15.getId(), IUIEventCallback.NON_POP_UP, 0);
                                return;
                            }
                            return;
                        }
                        AMBSClient.this.mJanskyTranslation.onNotifyMessageAppUI(ATTConstants.AttAmbsUIScreenNames.AMBS_SERVICE_DISABLE.getId(), IUIEventCallback.NON_POP_UP, 0);
                        return;
                    }
                    String r02 = AMBSClient.this.LOG_TAG;
                    Log.d(r02, "inserted card is not a ATT or TMO card" + simMno.toString());
                    AMBSClient.this.mJanskyTranslation.onNotifyMessageAppUI(ATTConstants.AttAmbsUIScreenNames.AMBS_SERVICE_DISABLE.getId(), IUIEventCallback.NON_POP_UP, 0);
                }
            } catch (JSONException unused) {
                Log.e(AMBSClient.this.LOG_TAG, "Json parsing exception");
            }
        }

        public void manualSync(String str, String str2) throws RemoteException {
            String r0 = AMBSClient.this.LOG_TAG;
            Log.d(r0, "manualSync: " + str + " jsonSummary: " + str2);
            AMBSClient.this.mNetAPIWorkingController.setImpuFromImsRegistration(str2);
        }

        public void enableAutoSync(String str, String str2) throws RemoteException {
            String r1 = AMBSClient.this.LOG_TAG;
            Log.d(r1, "enableAutoSync: " + str);
        }

        public void disableAutoSync(String str, String str2) throws RemoteException {
            String r1 = AMBSClient.this.LOG_TAG;
            Log.d(r1, "disableAutoSync: " + str);
        }

        public void onFTUriResponse(String str, String str2) throws RemoteException {
            String r0 = AMBSClient.this.LOG_TAG;
            Log.i(r0, "onFtUriResponse " + str + " " + str2);
            if (!AMBSClient.this.isValidAppType(str) || !AMBSClient.this.mNetAPIWorkingController.getCmsProfileEnabled()) {
                AMBSClient.this.logInvalidAppType();
            } else {
                AMBSClient.this.mCloudMessageScheduler.onFtUriResponseJson(str, str2);
            }
        }

        public int getRestartScreenName(String str) {
            Log.i(AMBSClient.this.LOG_TAG, "getRestartScreenName");
            if (SimUtil.getSubId(AMBSClient.this.getClientID()) != -1 && AMBSClient.this.isValidSim() && !AMBSClient.this.mNetAPIWorkingController.getCmsIsAccountServicePause()) {
                return ATTConstants.AttAmbsUIScreenNames.RestartMenu_Enable_PrmptMsg15.getId();
            }
            Log.i(AMBSClient.this.LOG_TAG, "AMBS Paused, notify 116");
            return ATTConstants.AttAmbsUIScreenNames.RestartMenu_Disable_PrmptMsg16.getId();
        }
    };
    private HandlerThread mBufferDBHandlingThread;
    private CentralMsgStoreInterface mCentralMsgStoreWrapper;
    private int mClientID = 0;
    private CloudMessagePreferenceManager mCloudMessagePreferenceManager;
    /* access modifiers changed from: private */
    public CloudMessageBufferSchedulingHandler mCloudMessageScheduler = null;
    private final CloudMessageService mCloudMessageService;
    private CloudMessageStrategyManager mCloudMessageStrategyManager;
    private Context mContext = null;
    GbaServiceModule mGbaServiceModule;
    private CmsHttpController mHttpController;
    private IImsFramework mImsFramework;
    /* access modifiers changed from: private */
    public JanskyIntentTranslation mJanskyTranslation;
    private HandlerThread mNetAPIHandlingThread;
    /* access modifiers changed from: private */
    public NetAPIWorkingStatusController mNetAPIWorkingController = null;
    private PhoneStateListener mPhoneStateListener;
    private PhoneStateManager mPhoneStateManager;
    private RetryMapAdapter mRetryMapAdapter = null;
    private RetryStackAdapter mRetryStackAdapter = null;
    private ISimManager mSimManager;
    /* access modifiers changed from: private */
    public int mSlotID = 0;
    /* access modifiers changed from: private */
    public MessageStoreClient msc = null;

    public ArrayList<IMcsFcmPushNotificationListener> getMcsFcmPushNotificationListener() {
        return null;
    }

    public RemoteCallbackList<ICentralMsgStoreServiceListener> getMcsProvisioningListener() {
        return null;
    }

    public McsRetryMapAdapter getMcsRetryMapAdapter() {
        return null;
    }

    public boolean getProvisionStatus() {
        return false;
    }

    public WorkflowMcs getProvisionWorkFlow() {
        return null;
    }

    public boolean isRcsRegistered() {
        return false;
    }

    public void notifyAppOperationResult(String str, int i) {
    }

    public void onDestroy() {
    }

    public void registerCmsProvisioningListener(ICentralMsgStoreServiceListener iCentralMsgStoreServiceListener, boolean z) {
    }

    public void setMcsFcmPushNotificationListener(IMcsFcmPushNotificationListener iMcsFcmPushNotificationListener) {
    }

    public void setProvisionStatus(boolean z) {
    }

    public void unregisterCmsProvisioningListener(ICentralMsgStoreServiceListener iCentralMsgStoreServiceListener) {
    }

    public boolean updateDelay(int i, long j) {
        return false;
    }

    public void updateEvent(int i) {
    }

    public AMBSClient(int i, Context context, CloudMessageService cloudMessageService, IImsFramework iImsFramework) {
        Class<AMBSClient> cls = AMBSClient.class;
        this.LOG_TAG = cls.getSimpleName();
        this.LOG_TAG_CN = cls.getSimpleName();
        this.mClientID = i;
        this.mContext = context;
        this.mSlotID = i;
        this.mCloudMessageService = cloudMessageService;
        this.mImsFramework = iImsFramework;
        this.LOG_TAG += "[" + this.mClientID + "]";
        this.msc = this;
    }

    public void notifyAppNetworkOperationResult(boolean z) {
        this.mJanskyTranslation.notifyAppNetworkOperationResult(z);
    }

    /* access modifiers changed from: private */
    public void logInvalidAppType() {
        Log.e(this.LOG_TAG, "invalid apptype ");
    }

    public int getClientID() {
        return this.mClientID;
    }

    public Context getContext() {
        return this.mContext;
    }

    public Binder getBinder() {
        return this.mBinder;
    }

    public CloudMessagePreferenceManager getPrerenceManager() {
        return this.mCloudMessagePreferenceManager;
    }

    public String getCurrentIMSI() {
        return this.mSimManager.getImsi();
    }

    public RetryStackAdapter getRetryStackAdapter() {
        return this.mRetryStackAdapter;
    }

    public RetryMapAdapter getRetryMapAdapter() {
        return this.mRetryMapAdapter;
    }

    public CmsHttpController getHttpController() {
        return this.mHttpController;
    }

    public void onCreate(IImsFramework iImsFramework, GbaServiceModule gbaServiceModule) {
        this.mBufferDBHandlingThread = new HandlerThread("cloud message service buffer DB thread");
        this.mNetAPIHandlingThread = new HandlerThread("cloud message service NetAPI thread");
        this.mBufferDBHandlingThread.start();
        this.mNetAPIHandlingThread.start();
        Looper looper = this.mBufferDBHandlingThread.getLooper();
        Looper looper2 = this.mNetAPIHandlingThread.getLooper();
        this.mSimManager = SimManagerFactory.getSimManagerFromSimSlot(this.mSlotID);
        this.mCloudMessagePreferenceManager = new CloudMessagePreferenceManager(this);
        this.mCloudMessageStrategyManager = new CloudMessageStrategyManager(this);
        this.mHttpController = new CmsHttpController(this.mContext, this.mSlotID);
        this.mPhoneStateListener = new PhoneStateListener(getContext().getMainExecutor()) {
            public void onMessageWaitingIndicatorChanged(boolean z) {
                String r0 = AMBSClient.this.LOG_TAG;
                Log.i(r0, "MWI is changed. " + z);
                if (!AMBSClient.this.mNetAPIWorkingController.getCmsProfileEnabled() || !z) {
                    Log.d(AMBSClient.this.LOG_TAG, "cms profile is not enabled or mwi is false");
                } else {
                    AMBSClient.this.mNetAPIWorkingController.vvmNormalSyncRequest();
                }
            }
        };
        Mno simMno = SimUtil.getSimMno(getClientID());
        if (simMno != null) {
            String str = this.LOG_TAG;
            Log.i(str, "Carrier: " + simMno.toString());
        }
        CloudMessageProvider.createBufferDBInstance(this);
        if (Mno.ATT.equals(simMno)) {
            RetryStackAdapter retryStackAdapter = new RetryStackAdapter();
            this.mRetryStackAdapter = retryStackAdapter;
            retryStackAdapter.initRetryStackAdapter(this);
            this.mNetAPIWorkingController = new NetAPIWorkingStatusController(looper2, this, this, new RetryStackAdapterHelper(), iImsFramework, gbaServiceModule);
        } else {
            RetryMapAdapter retryMapAdapter = new RetryMapAdapter();
            this.mRetryMapAdapter = retryMapAdapter;
            retryMapAdapter.initRetryMapAdapter(this);
            this.mNetAPIWorkingController = new NetAPIWorkingStatusController(looper2, this, this, new RetryMapAdapterHelper(), iImsFramework, gbaServiceModule);
        }
        this.mCloudMessageScheduler = new CloudMessageBufferSchedulingHandler(looper, this, this.mNetAPIWorkingController, this, (ICloudMessageManagerHelper) null, false);
        this.mJanskyTranslation = new JanskyIntentTranslation(getContext(), this);
        registerMWIWithLastVVMStatus();
        this.mCloudMessageScheduler.resyncPendingMsg();
        MStoreDebugTool.getInstance(this.mContext, this.mNetAPIWorkingController, this.mCentralMsgStoreWrapper).initDebugInfo();
    }

    public void handleVVMOn(String str, String str2, String str3) {
        Cursor query;
        if ("VVMDATA".equals(str) && CloudMessageProviderContract.DataTypes.VVMPROFILE.equals(str2)) {
            try {
                JSONArray jSONArray = new JSONArray(str3);
                for (int i = 0; i < jSONArray.length(); i++) {
                    int i2 = jSONArray.getJSONObject(i).getInt("id");
                    String str4 = this.LOG_TAG;
                    int i3 = this.mSlotID;
                    EventLogHelper.debugLogAndAdd(str4, i3, "queryVvmProfileBufferDB: " + i2);
                    StringBuilder sb = new StringBuilder();
                    sb.append("content://com.samsung.rcs.cmstore/vvmprofile");
                    sb.append(this.mSlotID == 0 ? "" : "/slot2");
                    query = this.mContext.getContentResolver().query(Uri.withAppendedPath(Uri.parse(sb.toString()), String.valueOf(i2)), (String[]) null, (String) null, (String[]) null, (String) null);
                    if (query != null) {
                        if (query.moveToFirst()) {
                            String string = query.getString(query.getColumnIndex(CloudMessageProviderContract.VVMAccountInfoColumns.VVMON));
                            if (string != null) {
                                this.mCloudMessagePreferenceManager.saveLastVVMStatus(string);
                            }
                            if (CloudMessageProviderContract.JsonData.TRUE.equalsIgnoreCase(string)) {
                                int i4 = query.getInt(query.getColumnIndex(CloudMessageProviderContract.VVMAccountInfoColumns.PROFILE_CHANGETYPE));
                                if (ParamVvmUpdate.VvmTypeChange.ACTIVATE.getId() == i4) {
                                    if (query.getInt(query.getColumnIndex("uploadstatus")) == CloudMessageBufferDBConstants.UploadStatusFlag.SUCCESS.getId()) {
                                        registerMWI();
                                    }
                                } else if (ParamVvmUpdate.VvmTypeChange.FULLPROFILE.getId() == i4) {
                                    registerMWI();
                                }
                            } else if (ConfigConstants.VALUE.INFO_COMPLETED.equalsIgnoreCase(string)) {
                                unregisterMWI();
                            }
                        }
                    }
                    if (query != null) {
                        query.close();
                    }
                }
                return;
            } catch (JSONException e) {
                e.printStackTrace();
                return;
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        } else {
            return;
        }
        throw th;
    }

    public void registerMWIWithLastVVMStatus() {
        String lastVVMStatus = this.mCloudMessagePreferenceManager.getLastVVMStatus();
        String str = this.LOG_TAG_CN;
        int i = this.mSlotID;
        EventLogHelper.infoLogAndAdd(str, i, "Registering MWI with VVM profile and vvm_Status :" + lastVVMStatus);
        if (CloudMessageProviderContract.JsonData.TRUE.equalsIgnoreCase(lastVVMStatus)) {
            registerMWI();
        }
    }

    public void registerMWI() {
        Log.i(this.LOG_TAG, "registerMWI");
        if (this.mPhoneStateManager != null) {
            String str = this.LOG_TAG_CN;
            int i = this.mSlotID;
            EventLogHelper.infoLogAndAdd(str, i, "Trying to unregister for slot : " + this.mSlotID);
            this.mPhoneStateManager.unRegisterListener(this.mSlotID);
        } else {
            this.mPhoneStateManager = new PhoneStateManager(this.mContext, 4);
        }
        this.mPhoneStateManager.registerListener(this.mPhoneStateListener, this.mSlotID);
    }

    public void unregisterMWI() {
        String str = this.LOG_TAG_CN;
        int i = this.mSlotID;
        EventLogHelper.infoLogAndAdd(str, i, "unregisterMWI for slot " + this.mSlotID);
        PhoneStateManager phoneStateManager = this.mPhoneStateManager;
        if (phoneStateManager != null) {
            phoneStateManager.unRegisterListener(this.mSlotID);
        }
    }

    public CloudMessageStrategyManager getCloudMessageStrategyManager() {
        return this.mCloudMessageStrategyManager;
    }

    public void notifyAppUIScreen(int i, String str, int i2) {
        String str2 = this.LOG_TAG;
        Log.d(str2, "notifyAppUIScreen, screenName: " + i + " message: " + str + " param: " + i2);
        this.mJanskyTranslation.onNotifyMessageAppUI(i, str, i2);
    }

    public void notifyAppInitialSyncStatus(String str, String str2, String str3, CloudMessageBufferDBConstants.InitialSyncStatusFlag initialSyncStatusFlag, boolean z) {
        String str4 = this.LOG_TAG_CN;
        int i = this.mSlotID;
        EventLogHelper.infoLogAndAdd(str4, i, "notifyAppInitialSyncStatus, apptype: " + str + " msgType: " + str2 + " SyncStatus: " + initialSyncStatusFlag);
        if (CloudMessageProviderContract.ApplicationTypes.MSGDATA.equals(str)) {
            this.mJanskyTranslation.onNotifyMessageAppInitialSyncStatus(str3, str2, initialSyncStatusFlag);
        } else if ("VVMDATA".equals(str)) {
            this.mJanskyTranslation.onNotifyVVMAppInitialSyncStatus(str3, str2, initialSyncStatusFlag, z);
        }
    }

    public void notifyCloudMessageUpdate(String str, String str2, String str3, boolean z) {
        String str4 = this.LOG_TAG;
        Log.d(str4, "notifyCloudMessageUpdate, apptype: " + str + " msgType: " + str2 + " rowIDs: " + str3);
        handleVVMOn(str, str2, str3);
        if (CloudMessageProviderContract.ApplicationTypes.MSGDATA.equals(str)) {
            this.mJanskyTranslation.onNotifyMessageApp(str2, str3, z);
        } else if ("VVMDATA".equals(str)) {
            this.mJanskyTranslation.onNotifyVVMApp(str2, str3);
        }
    }

    public void notifyAppCloudDeleteFail(String str, String str2, String str3) {
        String str4 = this.LOG_TAG;
        int i = this.mSlotID;
        EventLogHelper.debugLogAndAdd(str4, i, "notifyAppCloudDeleteFail, type: " + str + " msgtype: " + str2 + " bufferId: " + str3);
        if (CloudMessageProviderContract.ApplicationTypes.MSGDATA.equals(str)) {
            this.mJanskyTranslation.onNotifyMessageAppCloudDeleteFailure(str2, str3);
        } else if ("VVMDATA".equals(str)) {
            this.mJanskyTranslation.onNotifyVVMAppCloudDeleteFailure(str2, str3);
        }
    }

    /* access modifiers changed from: private */
    public boolean isValidSim() {
        if (Mno.ATT.equals(getSimManager().getSimMno())) {
            return true;
        }
        Log.i(this.LOG_TAG, "This is not ATT sim card");
        return false;
    }

    /* access modifiers changed from: private */
    public boolean isValidAppType(String str) {
        if (!CloudMessageProviderContract.ApplicationTypes.MSGDATA.equalsIgnoreCase(str) && !"VVMDATA".equalsIgnoreCase(str)) {
            return CloudMessageProviderContract.ApplicationTypes.RCSDATA.equalsIgnoreCase(str);
        }
        return true;
    }

    public void showInitsyncIndicator(boolean z) {
        if (getCloudMessageStrategyManager().getStrategy().getIsInitSyncIndicatorRequired()) {
            this.mCloudMessageService.showInitsyncIndicator(z, this.mClientID);
        } else {
            Log.v(this.LOG_TAG, "showInitsyncIndicator: not supported");
        }
    }

    public NetAPIWorkingStatusController getNetAPIWorkingStatusController() {
        return this.mNetAPIWorkingController;
    }

    public CloudMessageBufferSchedulingHandler getCloudMessageBufferSchedulingHandler() {
        return this.mCloudMessageScheduler;
    }

    public ISimManager getSimManager() {
        return this.mSimManager;
    }

    public String[] getStringArray(int i, String str, String[] strArr) {
        getContext().enforceCallingOrSelfPermission(ISignallingNotifier.PERMISSION, this.LOG_TAG);
        return GlobalSettingsManager.getInstance(this.mContext, i).getStringArray(str, strArr);
    }

    /* access modifiers changed from: private */
    public void resetParams(Mno mno) {
        if (Mno.TMOUS.equals(mno)) {
            this.mRetryStackAdapter = null;
            RetryMapAdapter retryMapAdapter = new RetryMapAdapter();
            this.mRetryMapAdapter = retryMapAdapter;
            retryMapAdapter.initRetryMapAdapter(this);
            this.mNetAPIWorkingController.resetAdapter(new RetryMapAdapterHelper());
            return;
        }
        this.mRetryMapAdapter = null;
        RetryStackAdapter retryStackAdapter = new RetryStackAdapter();
        this.mRetryStackAdapter = retryStackAdapter;
        retryStackAdapter.initRetryStackAdapter(this);
        ATTGlobalVariables.setAmbsPhaseVersion(this.mCloudMessageService.getAMBSPhaseVersion(getClientID()));
        this.mNetAPIWorkingController.resetAdapter(new RetryStackAdapterHelper());
    }
}
