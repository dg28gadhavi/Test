package com.sec.internal.ims.cmstore.servicecontainer;

import android.os.RemoteException;
import android.util.Log;
import com.sec.ims.ICentralMsgStoreService;
import com.sec.ims.ICentralMsgStoreServiceListener;
import com.sec.ims.ImsRegistration;
import com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.ims.cmstore.JanskyIntentTranslation;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.NetAPIWorkingStatusController;
import com.sec.internal.ims.cmstore.cloudmessagebuffer.CloudMessageBufferSchedulingHandler;
import com.sec.internal.ims.cmstore.utils.CmsUtil;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import org.json.JSONException;
import org.json.JSONObject;

public class CentralMsgStoreInterface {
    /* access modifiers changed from: private */
    public static final String LOG_TAG = "CentralMsgStoreInterface";
    private ICentralMsgStoreService.Stub mBinder = null;
    /* access modifiers changed from: private */
    public CloudMessageBufferSchedulingHandler mCloudMessageScheduler;
    private JanskyIntentTranslation mJanskyIntentTranslation;
    /* access modifiers changed from: private */
    public NetAPIWorkingStatusController mNetAPIWorkingController;
    /* access modifiers changed from: private */
    public MessageStoreClient mStoreClient;

    /* access modifiers changed from: private */
    public void logInvalidAppType() {
        Log.e(LOG_TAG, "invalid apptype ");
    }

    public CentralMsgStoreInterface(CloudMessageBufferSchedulingHandler cloudMessageBufferSchedulingHandler, NetAPIWorkingStatusController netAPIWorkingStatusController, JanskyIntentTranslation janskyIntentTranslation, ICloudMessageManagerHelper iCloudMessageManagerHelper, MessageStoreClient messageStoreClient) {
        this.mStoreClient = messageStoreClient;
        this.mCloudMessageScheduler = cloudMessageBufferSchedulingHandler;
        this.mNetAPIWorkingController = netAPIWorkingStatusController;
        this.mJanskyIntentTranslation = janskyIntentTranslation;
        this.mBinder = new ICentralMsgStoreService.Stub() {
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
                String r0 = CentralMsgStoreInterface.LOG_TAG;
                Log.i(r0, "receivedMessage " + str);
                String r02 = CentralMsgStoreInterface.LOG_TAG;
                Log.d(r02, "receivedMessage : " + str2);
                if (!CentralMsgStoreInterface.this.isValidAppType(str) || !CentralMsgStoreInterface.this.mNetAPIWorkingController.getCmsProfileEnabled()) {
                    CentralMsgStoreInterface.this.logInvalidAppType();
                } else {
                    CentralMsgStoreInterface.this.mCloudMessageScheduler.receivedMessageJson(str2);
                }
            }

            public void sentMessage(String str, String str2) throws RemoteException {
                String r0 = CentralMsgStoreInterface.LOG_TAG;
                Log.i(r0, "sentMessage " + str);
                String r02 = CentralMsgStoreInterface.LOG_TAG;
                Log.d(r02, "sentMessage : " + str2);
                if (!CentralMsgStoreInterface.this.isValidAppType(str) || !CentralMsgStoreInterface.this.mNetAPIWorkingController.getCmsProfileEnabled()) {
                    CentralMsgStoreInterface.this.logInvalidAppType();
                } else {
                    CentralMsgStoreInterface.this.mCloudMessageScheduler.sentMessageJson(str2);
                }
            }

            public void readMessage(String str, String str2) throws RemoteException {
                String r0 = CentralMsgStoreInterface.LOG_TAG;
                Log.i(r0, "readMessage " + str);
                String r02 = CentralMsgStoreInterface.LOG_TAG;
                Log.d(r02, "readMessage : " + str2);
                if (!CentralMsgStoreInterface.this.isValidAppType(str) || !CentralMsgStoreInterface.this.mNetAPIWorkingController.getCmsProfileEnabled()) {
                    CentralMsgStoreInterface.this.logInvalidAppType();
                } else {
                    CentralMsgStoreInterface.this.mCloudMessageScheduler.readMessageJson(str, str2);
                }
            }

            public void cancelMessage(String str, String str2) throws RemoteException {
                String r0 = CentralMsgStoreInterface.LOG_TAG;
                Log.i(r0, "cancelMessage " + str);
                String r02 = CentralMsgStoreInterface.LOG_TAG;
                Log.d(r02, "cancelMessage : " + str2);
                if (!CentralMsgStoreInterface.this.isValidAppType(str) || !CentralMsgStoreInterface.this.mNetAPIWorkingController.getCmsProfileEnabled()) {
                    CentralMsgStoreInterface.this.logInvalidAppType();
                } else {
                    CentralMsgStoreInterface.this.mCloudMessageScheduler.cancelMessageJson(str, str2);
                }
            }

            public void requestMessageProcess(String str, String str2, int i) {
                String r2 = CentralMsgStoreInterface.LOG_TAG;
                Log.i(r2, "requestMessageProcess " + str + " function: " + i + ": " + str2);
            }

            public void unReadMessage(String str, String str2) throws RemoteException {
                String r0 = CentralMsgStoreInterface.LOG_TAG;
                Log.i(r0, "unReadMessage " + str);
                String r02 = CentralMsgStoreInterface.LOG_TAG;
                Log.d(r02, "unReadMessage : " + str2);
                if (!CentralMsgStoreInterface.this.isValidAppType(str) || !CentralMsgStoreInterface.this.mNetAPIWorkingController.getCmsProfileEnabled()) {
                    CentralMsgStoreInterface.this.logInvalidAppType();
                } else {
                    CentralMsgStoreInterface.this.mCloudMessageScheduler.unReadMessageJson(str2);
                }
            }

            public void deleteMessage(String str, String str2) throws RemoteException {
                String r0 = CentralMsgStoreInterface.LOG_TAG;
                Log.i(r0, "deleteMessage " + str);
                String r02 = CentralMsgStoreInterface.LOG_TAG;
                Log.d(r02, "deleteMessage : " + str2);
                if (!CentralMsgStoreInterface.this.isValidAppType(str) || !CentralMsgStoreInterface.this.mNetAPIWorkingController.getCmsProfileEnabled()) {
                    CentralMsgStoreInterface.this.logInvalidAppType();
                } else {
                    CentralMsgStoreInterface.this.mCloudMessageScheduler.deleteMessageJson(str2);
                }
            }

            public void uploadMessage(String str, String str2) throws RemoteException {
                String r0 = CentralMsgStoreInterface.LOG_TAG;
                Log.i(r0, "uploadMessage " + str);
                String r02 = CentralMsgStoreInterface.LOG_TAG;
                Log.d(r02, "uploadMessage : " + str2);
                if (!CentralMsgStoreInterface.this.isValidAppType(str) || !CentralMsgStoreInterface.this.mNetAPIWorkingController.getCmsProfileEnabled()) {
                    CentralMsgStoreInterface.this.logInvalidAppType();
                } else {
                    CentralMsgStoreInterface.this.mCloudMessageScheduler.uploadMessageJson(str2);
                }
            }

            public void downloadMessage(String str, String str2) throws RemoteException {
                String r0 = CentralMsgStoreInterface.LOG_TAG;
                Log.i(r0, "downloadMessage " + str);
                String r02 = CentralMsgStoreInterface.LOG_TAG;
                Log.i(r02, "downloadMessage : " + str2);
                if (!CentralMsgStoreInterface.this.isValidAppType(str) || !CentralMsgStoreInterface.this.mNetAPIWorkingController.getCmsProfileEnabled()) {
                    CentralMsgStoreInterface.this.logInvalidAppType();
                } else {
                    CentralMsgStoreInterface.this.mCloudMessageScheduler.downloadMessageJson(str2);
                }
            }

            public void requestOperation(int i, int i2, String str, String str2) throws RemoteException {
                String r1 = CentralMsgStoreInterface.LOG_TAG;
                Log.i(r1, "requestOperation " + str + " operation: " + i2 + ": " + str2);
            }

            public void wipeOutMessage(String str, String str2) throws RemoteException {
                String r0 = CentralMsgStoreInterface.LOG_TAG;
                Log.i(r0, "wipeOutMessage " + str);
                String r02 = CentralMsgStoreInterface.LOG_TAG;
                Log.d(r02, "wipeOutMessage : " + str2);
                if (!CentralMsgStoreInterface.this.isValidAppType(str) || !CentralMsgStoreInterface.this.mNetAPIWorkingController.getCmsProfileEnabled()) {
                    CentralMsgStoreInterface.this.logInvalidAppType();
                } else {
                    CentralMsgStoreInterface.this.mCloudMessageScheduler.wipeOutMessageJson(str2);
                }
            }

            public void onUserEnterApp(String str) throws RemoteException {
                String r0 = CentralMsgStoreInterface.LOG_TAG;
                Log.i(r0, "onUserEnterApp " + str);
                if (!CentralMsgStoreInterface.this.isValidAppType(str) || !CentralMsgStoreInterface.this.mNetAPIWorkingController.getCmsProfileEnabled()) {
                    CentralMsgStoreInterface.this.logInvalidAppType();
                    return;
                }
                CentralMsgStoreInterface.this.mNetAPIWorkingController.setMsgAppForegroundStatus(true);
                CentralMsgStoreInterface.this.mCloudMessageScheduler.onReturnAppFetchingFailedMsg(str);
            }

            public void onUserLeaveApp(String str) throws RemoteException {
                String r0 = CentralMsgStoreInterface.LOG_TAG;
                Log.i(r0, "onUserLeaveApp " + str);
                if (!CentralMsgStoreInterface.this.isValidAppType(str) || !CentralMsgStoreInterface.this.mNetAPIWorkingController.getCmsProfileEnabled()) {
                    CentralMsgStoreInterface.this.logInvalidAppType();
                } else {
                    CentralMsgStoreInterface.this.mNetAPIWorkingController.setMsgAppForegroundStatus(false);
                }
            }

            public boolean onUIButtonProceed(String str, int i, String str2) throws RemoteException {
                String r0 = CentralMsgStoreInterface.LOG_TAG;
                Log.i(r0, "onUIButtonProceed " + str + " screenName: " + i);
                String r02 = CentralMsgStoreInterface.LOG_TAG;
                StringBuilder sb = new StringBuilder();
                sb.append("onUIButtonProceed , message: ");
                sb.append(str2);
                Log.d(r02, sb.toString());
                if (CentralMsgStoreInterface.this.isValidAppType(str) && CentralMsgStoreInterface.this.mNetAPIWorkingController.getCmsProfileEnabled()) {
                    return CentralMsgStoreInterface.this.mNetAPIWorkingController.onUIButtonProceed(i, str2);
                }
                CentralMsgStoreInterface.this.logInvalidAppType();
                return false;
            }

            public void onBufferDBReadResult(String str, String str2, String str3, String str4, int i, boolean z) throws RemoteException {
                String r0 = CentralMsgStoreInterface.LOG_TAG;
                Log.i(r0, "onBufferDBReadResult: " + str + " msgType: " + str2 + " bufferRowID: " + str3 + " appMessageId: " + str4 + " syncAction: " + i + " isSuccess: " + z);
                if (!CentralMsgStoreInterface.this.isValidAppType(str) || !CentralMsgStoreInterface.this.mNetAPIWorkingController.getCmsProfileEnabled()) {
                    Log.d(CentralMsgStoreInterface.LOG_TAG, "ignore");
                } else if (!CentralMsgStoreInterface.this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getIsInitSyncIndicatorRequired() || Integer.valueOf(str3).intValue() >= 0) {
                    CentralMsgStoreInterface.this.mCloudMessageScheduler.onBufferDBReadResult(str2, str3, str4, i, z);
                } else {
                    CentralMsgStoreInterface.this.mNetAPIWorkingController.hideIndicator();
                }
            }

            public void onBufferDBReadResultBatch(String str, String str2) throws RemoteException {
                String r0 = CentralMsgStoreInterface.LOG_TAG;
                Log.i(r0, "onBufferDBReadResultBatch " + str);
                String r02 = CentralMsgStoreInterface.LOG_TAG;
                Log.d(r02, "onBufferDBReadResultBatch : " + str2);
                if (!CentralMsgStoreInterface.this.isValidAppType(str) || !CentralMsgStoreInterface.this.mNetAPIWorkingController.getCmsProfileEnabled()) {
                    CentralMsgStoreInterface.this.logInvalidAppType();
                } else {
                    CentralMsgStoreInterface.this.mCloudMessageScheduler.bufferDbReadBatchMessageJson(str2);
                }
            }

            public void registerCallback(String str, ICentralMsgStoreService iCentralMsgStoreService) throws RemoteException {
                String r1 = CentralMsgStoreInterface.LOG_TAG;
                Log.i(r1, "registerCallback " + str);
            }

            public void stopSync(String str, String str2) throws RemoteException {
                String r0 = CentralMsgStoreInterface.LOG_TAG;
                Log.i(r0, "stopSync " + str);
                String r02 = CentralMsgStoreInterface.LOG_TAG;
                Log.d(r02, "stopSync : " + str2);
                if (!CentralMsgStoreInterface.this.isValidAppType(str) || !CentralMsgStoreInterface.this.mNetAPIWorkingController.getCmsProfileEnabled()) {
                    CentralMsgStoreInterface.this.logInvalidAppType();
                } else {
                    CentralMsgStoreInterface.this.mCloudMessageScheduler.stopSync(str, str2);
                }
            }

            public void startFullSync(String str, String str2) throws RemoteException {
                String r0 = CentralMsgStoreInterface.LOG_TAG;
                Log.i(r0, "startFullSync " + str);
                String r02 = CentralMsgStoreInterface.LOG_TAG;
                Log.d(r02, "startFullSync : " + str2);
                if (!CentralMsgStoreInterface.this.isValidAppType(str) || !CentralMsgStoreInterface.this.mNetAPIWorkingController.getCmsProfileEnabled()) {
                    CentralMsgStoreInterface.this.logInvalidAppType();
                } else {
                    CentralMsgStoreInterface.this.mCloudMessageScheduler.startFullSync(str, str2);
                }
            }

            public void startDeltaSync(String str, String str2) throws RemoteException {
                String r0 = CentralMsgStoreInterface.LOG_TAG;
                Log.i(r0, "startDeltaSync " + str);
                String r02 = CentralMsgStoreInterface.LOG_TAG;
                Log.d(r02, "startDeltaSync : " + str2);
                if (!CentralMsgStoreInterface.this.isValidAppType(str) || !CentralMsgStoreInterface.this.mNetAPIWorkingController.getCmsProfileEnabled()) {
                    CentralMsgStoreInterface.this.logInvalidAppType();
                } else {
                    CentralMsgStoreInterface.this.mCloudMessageScheduler.startDeltaSync(str, str2);
                }
            }

            public void deleteOldLegacyMessage(String str, String str2) throws RemoteException {
                String r2 = CentralMsgStoreInterface.LOG_TAG;
                Log.i(r2, "deleteOldLegacyMessage " + str + " thread:" + str2);
            }

            public void resumeSync(String str) throws RemoteException {
                String r2 = CentralMsgStoreInterface.LOG_TAG;
                Log.i(r2, "resumeSync " + str);
            }

            public void restartService(String str) throws RemoteException {
                String r0 = CentralMsgStoreInterface.LOG_TAG;
                Log.i(r0, "restartService " + str);
                if (CentralMsgStoreInterface.this.mNetAPIWorkingController.getCmsProfileEnabled()) {
                    CentralMsgStoreInterface.this.mNetAPIWorkingController.onRestartService();
                } else {
                    CentralMsgStoreInterface.this.logInvalidAppType();
                }
            }

            public void notifyCloudMessageUpdate(String str, String str2, String str3) throws RemoteException {
                String r2 = CentralMsgStoreInterface.LOG_TAG;
                Log.i(r2, "notifyCloudMessageUpdate, apptype: " + str + " msgType: " + str2 + " rowIDs: " + str3);
            }

            public void createSession(String str, String str2) throws RemoteException {
                String r0 = CentralMsgStoreInterface.LOG_TAG;
                Log.i(r0, "createSession " + str + " chatId: " + str2);
                CentralMsgStoreInterface.this.mCloudMessageScheduler.createSession(str2);
            }

            public void createParticipant(String str, String str2) throws RemoteException {
                String r0 = CentralMsgStoreInterface.LOG_TAG;
                Log.i(r0, "createParticipant " + str + " chatId: " + str2);
                CentralMsgStoreInterface.this.mCloudMessageScheduler.createParticipant(str2);
            }

            public void deleteSession(String str, String str2) throws RemoteException {
                String r0 = CentralMsgStoreInterface.LOG_TAG;
                Log.i(r0, "deleteSession " + str + " chatId: " + str2);
                CentralMsgStoreInterface.this.mCloudMessageScheduler.deleteSession(str2);
            }

            public void deleteParticipant(String str, String str2) throws RemoteException {
                String r0 = CentralMsgStoreInterface.LOG_TAG;
                Log.i(r0, "deleteParticipant " + str + " chatId: " + str2);
                CentralMsgStoreInterface.this.mCloudMessageScheduler.deleteParticipant(str2);
            }

            public void onRCSDBReady(String str) throws RemoteException {
                String r0 = CentralMsgStoreInterface.LOG_TAG;
                Log.i(r0, "onRCSDBReady: " + str);
                try {
                    JSONObject jSONObject = new JSONObject(str);
                    String string = jSONObject.getString(CloudMessageProviderContract.JsonParamTags.CMS_PROFILE_EVENT);
                    String string2 = jSONObject.getString(CloudMessageProviderContract.JsonParamTags.SIM_STATUS);
                    String r1 = CentralMsgStoreInterface.LOG_TAG;
                    Log.i(r1, "eventType =" + string + ", simStatus =" + string2);
                    if (CloudMessageProviderContract.SimStatusValue.SIM_REMOVED.equals(string2)) {
                        CentralMsgStoreInterface.this.mNetAPIWorkingController.setCmsProfileEnabled(false);
                        return;
                    }
                    if (CloudMessageProviderContract.SimStatusValue.SIM_READY.equals(string2) && CloudMessageProviderContract.CmsEventTypeValue.CMS_PROFILE_DISABLE.equals(string) && CmsUtil.isSimChanged(CentralMsgStoreInterface.this.mStoreClient)) {
                        CentralMsgStoreInterface.this.mStoreClient.getPrerenceManager().clearAll();
                    }
                    if (CloudMessageProviderContract.CmsEventTypeValue.CMS_PROFILE_ENABLE.equals(string)) {
                        if (!CentralMsgStoreInterface.this.mNetAPIWorkingController.getCmsProfileEnabled()) {
                            CentralMsgStoreInterface.this.mNetAPIWorkingController.setCmsProfileEnabled(true);
                            CentralMsgStoreInterface.this.mCloudMessageScheduler.onRCSDbReady();
                        }
                        if (CentralMsgStoreInterface.this.mStoreClient.getCloudMessageStrategyManager().getStrategy().needToHandleSimSwap() && CmsUtil.isSimChanged(CentralMsgStoreInterface.this.mStoreClient)) {
                            CentralMsgStoreInterface.this.mNetAPIWorkingController.onRestartService();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            public void manualSync(String str, String str2) throws RemoteException {
                String r0 = CentralMsgStoreInterface.LOG_TAG;
                Log.i(r0, "manualSync: " + str);
                String r4 = CentralMsgStoreInterface.LOG_TAG;
                Log.d(r4, "manualSync jsonSummary: " + str2);
                CentralMsgStoreInterface.this.mNetAPIWorkingController.setImpuFromImsRegistration(str2);
            }

            public void enableAutoSync(String str, String str2) throws RemoteException {
                String r1 = CentralMsgStoreInterface.LOG_TAG;
                Log.i(r1, "enableAutoSync: " + str);
            }

            public void disableAutoSync(String str, String str2) throws RemoteException {
                String r1 = CentralMsgStoreInterface.LOG_TAG;
                Log.i(r1, "disableAutoSync: " + str);
            }

            public void onFTUriResponse(String str, String str2) throws RemoteException {
                String r0 = CentralMsgStoreInterface.LOG_TAG;
                Log.i(r0, "onFtUriResponse " + str + " " + str2);
                if (!CentralMsgStoreInterface.this.isValidAppType(str) || !CentralMsgStoreInterface.this.mNetAPIWorkingController.getCmsProfileEnabled()) {
                    CentralMsgStoreInterface.this.logInvalidAppType();
                } else {
                    CentralMsgStoreInterface.this.mCloudMessageScheduler.onFtUriResponseJson(str, str2);
                }
            }

            public int getRestartScreenName(String str) {
                String r2 = CentralMsgStoreInterface.LOG_TAG;
                Log.i(r2, "Restart Screen " + str);
                return 1;
            }
        };
    }

    public void notifyUIScreen(int i, String str, int i2) {
        String str2 = LOG_TAG;
        Log.i(str2, "notifyUIScreen, screenName: " + i);
        Log.d(str2, "notifyUIScreen, message: " + str + " param: " + i2);
        this.mJanskyIntentTranslation.onNotifyMessageAppUI(i, str, i2);
    }

    public void notifyAppInitialSyncStatus(String str, String str2, String str3, CloudMessageBufferDBConstants.InitialSyncStatusFlag initialSyncStatusFlag, boolean z) {
        String str4 = LOG_TAG;
        Log.i(str4, "notifyAppInitialSyncStatus, apptype: " + str + " msgType: " + str2 + " SyncStatus: " + initialSyncStatusFlag);
        if (CloudMessageProviderContract.ApplicationTypes.MSGDATA.equals(str)) {
            this.mJanskyIntentTranslation.onNotifyMessageAppInitialSyncStatus(str3, str2, initialSyncStatusFlag);
        } else if ("VVMDATA".equals(str)) {
            this.mJanskyIntentTranslation.onNotifyVVMAppInitialSyncStatus(str3, str2, initialSyncStatusFlag, z);
        }
    }

    public void notifyCloudMessageUpdate(String str, String str2, String str3) {
        String str4 = LOG_TAG;
        Log.i(str4, "notifyCloudMessageUpdate, apptype: " + str + " msgType: " + str2 + " rowIDs: " + str3);
        if (CloudMessageProviderContract.ApplicationTypes.MSGDATA.equals(str)) {
            this.mJanskyIntentTranslation.onNotifyMessageApp(str2, str3, false);
        } else if ("VVMDATA".equals(str)) {
            this.mJanskyIntentTranslation.onNotifyVVMApp(str2, str3);
        }
    }

    public void notifyAppCloudDeleteFail(String str, String str2, String str3) {
        String str4 = LOG_TAG;
        Log.i(str4, "notifyAppCloudDeleteFail, type: " + str + " msgtype: " + str2 + " bufferId: " + str3);
        if (CloudMessageProviderContract.ApplicationTypes.MSGDATA.equals(str)) {
            this.mJanskyIntentTranslation.onNotifyMessageAppCloudDeleteFailure(str2, str3);
        } else if ("VVMDATA".equals(str)) {
            this.mJanskyIntentTranslation.onNotifyVVMAppCloudDeleteFailure(str2, str3);
        }
    }

    /* access modifiers changed from: private */
    public boolean isValidAppType(String str) {
        if (CloudMessageProviderContract.ApplicationTypes.MSGDATA.equalsIgnoreCase(str) || "VVMDATA".equalsIgnoreCase(str) || CloudMessageProviderContract.ApplicationTypes.RCSDATA.equalsIgnoreCase(str)) {
            return true;
        }
        String str2 = LOG_TAG;
        Log.i(str2, "Invalid App Type " + str);
        return false;
    }

    public ICentralMsgStoreService.Stub getBinder() {
        return this.mBinder;
    }
}
