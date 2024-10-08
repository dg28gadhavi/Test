package com.sec.internal.ims.servicemodules.volte2;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import com.android.internal.telephony.PublishDialog;
import com.samsung.android.cmcp2phelper.MdmnNsdWrapper;
import com.samsung.android.cmcp2phelper.MdmnServiceInfo;
import com.sec.ims.Dialog;
import com.sec.ims.DialogEvent;
import com.sec.ims.ImsRegistration;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.volte2.data.CallProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.cmstore.McsConstants;
import com.sec.internal.constants.ims.servicemodules.Registration;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.servicemodules.options.OptionsEvent;
import com.sec.internal.constants.ims.servicemodules.volte2.CallConstants;
import com.sec.internal.constants.ims.servicemodules.volte2.CmcInfoEvent;
import com.sec.internal.google.SecImsNotifier;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.ImsCallUtil;
import com.sec.internal.helper.PreciseAlarmManager;
import com.sec.internal.helper.os.Debug;
import com.sec.internal.ims.core.cmc.CmcConstants;
import com.sec.internal.ims.imsservice.CallStateTracker;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.options.IOptionsServiceInterface;
import com.sec.internal.ims.servicemodules.tapi.service.extension.utils.Constants;
import com.sec.internal.ims.servicemodules.volte2.data.DefaultCallProfileBuilder;
import com.sec.internal.ims.servicemodules.volte2.data.DtmfInfo;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.core.handler.IVolteServiceInterface;
import com.sec.internal.interfaces.ims.servicemodules.volte2.ICmcServiceHelper;
import com.sec.internal.log.IMSLog;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class CmcServiceHelper extends Handler implements ICmcServiceHelper, ICmcServiceHelperInternal {
    private static final int CMC_HANDOVER_TIMER_VALUE = 6000;
    private static final int CMC_PD_CHECK_TIMER_VALUE = 20;
    private static final int DIVIDABLE64 = 63;
    private static final int DUMMY_CALL_DOMAIN = 9;
    private static final long DUPLICATED_PUBLISH_DENY_TIME_IN_MILLI = 500;
    private static final int EVENT_OPTIONS_EVENT = 32;
    private static final int EVENT_P2P_OPTIONS_EVENT = 31;
    private static final int EVT_CMC_HANDOVER_TIMER = 34;
    private static final int EVT_CMC_INFO_EVENT = 35;
    private static final int EVT_CMC_PD_CHECK_TIMER = 33;
    /* access modifiers changed from: private */
    public static final String LOG_TAG = CmcServiceHelper.class.getSimpleName();
    private final Map<Integer, Long> mCmcCallEstablishTimeMap = new ConcurrentHashMap();
    private Message mCmcHandoverTimer = null;
    private final Map<Integer, Message> mCmcPdCheckTimeOut = new ArrayMap();
    private boolean mCmcTotalMnoPullable = true;
    private final Context mContext;
    private final Map<Integer, PublishDialog> mCsPublishDialogMap = new ConcurrentHashMap();
    private int mExtConfirmedCsCallCnt = 0;
    private ImsCallSessionManager mImsCallSessionManager;
    /* access modifiers changed from: private */
    public final Map<Integer, Boolean> mIsCmcPdCheckRespReceived = new ArrayMap();
    /* access modifiers changed from: private */
    public boolean mIsP2pDiscoveryDone = false;
    /* access modifiers changed from: private */
    public DialogEvent[] mLastCmcDialogEvent;
    /* access modifiers changed from: private */
    public int mLastCmcEndCallReason = 200;
    private IImsMediaController mMediaController;
    /* access modifiers changed from: private */
    public boolean mNeedToNotifyAfterP2pDiscovery = false;
    private MdmnNsdWrapper mNsd;
    private MdmnServiceInfo mNsdServiceInfo;
    private int mNumOfActiveSDs = 0;
    private IOptionsServiceInterface mOptionsSvcIntf;
    private p2pCallbackHandler mP2pCallbackHandler = null;
    private CopyOnWriteArrayList<Registration> mRegistrationList;
    private MessageDigest mSendPublishDigest;
    private byte[] mSendPublishHashedXml;
    private int mSendPublishInvokeCount = 0;
    private long mSendPublishInvokeTime = 0;
    private IVolteServiceInterface mVolteSvcIntf;

    public CmcServiceHelper(Looper looper, Context context) {
        super(looper);
        this.mContext = context;
    }

    public CmcServiceHelper(Looper looper, Context context, CopyOnWriteArrayList<Registration> copyOnWriteArrayList, IVolteServiceInterface iVolteServiceInterface, IImsMediaController iImsMediaController, ImsCallSessionManager imsCallSessionManager, IOptionsServiceInterface iOptionsServiceInterface, int i) {
        super(looper);
        this.mContext = context;
        this.mVolteSvcIntf = iVolteServiceInterface;
        this.mOptionsSvcIntf = iOptionsServiceInterface;
        this.mMediaController = iImsMediaController;
        this.mRegistrationList = copyOnWriteArrayList;
        this.mImsCallSessionManager = imsCallSessionManager;
        this.mLastCmcDialogEvent = new DialogEvent[i];
        this.mP2pCallbackHandler = new p2pCallbackHandler(looper);
        try {
            this.mSendPublishDigest = MessageDigest.getInstance(Constants.DIGEST_ALGORITHM_SHA1);
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }
    }

    public void init() {
        this.mOptionsSvcIntf.registerForCmcOptionsEvent(this, 32, (Object) null);
        this.mOptionsSvcIntf.registerForP2pOptionsEvent(this, 31, (Object) null);
        this.mVolteSvcIntf.registerForCmcInfoEvent(this, 35, (Object) null);
    }

    public void onRegistered(ImsRegistration imsRegistration) {
        int cmcType = imsRegistration.getImsProfile().getCmcType();
        if (ImsCallUtil.isCmcPrimaryType(cmcType)) {
            if (cmcType == 1 && imsRegistration.getCurrentRat() == 18 && ImsRegistry.getCmcAccountManager().isSupportSameWiFiOnly()) {
                this.mP2pCallbackHandler.setP2pRegiInfo(imsRegistration);
            }
            this.mNumOfActiveSDs = 0;
        } else if (!ImsCallUtil.isCmcSecondaryType(cmcType)) {
            String str = LOG_TAG;
            Log.i(str, "mmtel Registered ? " + imsRegistration.hasService("mmtel"));
            if (imsRegistration.hasService("mmtel")) {
                this.mCsPublishDialogMap.remove(Integer.valueOf(imsRegistration.getPhoneId()));
            }
        } else if (this.mCmcHandoverTimer != null) {
            Log.i(LOG_TAG, "do cmc handover");
            PreciseAlarmManager.getInstance(this.mContext).removeMessage(this.mCmcHandoverTimer);
            this.mCmcHandoverTimer = null;
            ImsCallSession sessionByCmcType = getSessionByCmcType(cmcType);
            if (sessionByCmcType != null) {
                CallProfile makeReplaceProfile = makeReplaceProfile(sessionByCmcType.getCallProfile());
                try {
                    this.mImsCallSessionManager.createSession(this.mContext, makeReplaceProfile, imsRegistration).start(makeReplaceProfile.getLetteringText(), makeReplaceProfile);
                    sessionByCmcType.replaceRegistrationInfo(imsRegistration);
                } catch (RemoteException e) {
                    clearAllCallsForCmcHandover(cmcType);
                    e.printStackTrace();
                }
            }
        }
    }

    public void onDeregistered(ImsRegistration imsRegistration, int i) {
        String str = LOG_TAG;
        Log.d(str, "onDeregistered reason " + imsRegistration.getDeregiReason());
        int cmcType = imsRegistration.getImsProfile().getCmcType();
        if (cmcType > 0 && this.mNsd != null) {
            Log.i(str, "stop Nsd when deregistered");
            this.mNsd.stop();
            this.mP2pCallbackHandler.setP2pRegiInfo((ImsRegistration) null);
            this.mIsP2pDiscoveryDone = false;
            this.mNeedToNotifyAfterP2pDiscovery = false;
            this.mNumOfActiveSDs = 0;
            DialogEvent dialogEvent = this.mLastCmcDialogEvent[imsRegistration.getPhoneId()];
            if (dialogEvent != null) {
                dialogEvent.clearDialogList();
            }
        }
        if (cmcType == 2 && imsRegistration.getDeregiReason() == 22) {
            clearAllCallsForCmcHandover(cmcType);
        }
    }

    public void onDeregistering(ImsRegistration imsRegistration) {
        String str = LOG_TAG;
        Log.d(str, "onDeregistering reason " + imsRegistration.getDeregiReason());
        int cmcType = imsRegistration.getImsProfile().getCmcType();
        if (ImsCallUtil.isCmcPrimaryType(cmcType) && imsRegistration.getDeregiReason() != 2) {
            Log.d(str, "onDeregistering: Send dummy publish dialog before deregistered");
            sendDummyPublishDialog(imsRegistration.getPhoneId(), imsRegistration.getImsProfile().getCmcType());
        }
        if (cmcType == 2 && imsRegistration.getDeregiReason() == 22) {
            clearAllCallsForCmcHandover(cmcType);
        }
    }

    public void onRegEventContactUriNotification(int i, List<String> list) {
        int i2;
        int i3 = this.mNumOfActiveSDs;
        this.mNumOfActiveSDs = list.size() - 1;
        String str = LOG_TAG;
        IMSLog.i(str, i, "onRegEventContactUriNotification prevNumOfActiveSDs: " + i3 + " mNumOfActiveSDs: " + this.mNumOfActiveSDs);
        startP2pDiscovery(list);
        ImsRegistration cmcRegistration = getCmcRegistration(i, 1);
        if (cmcRegistration != null && (i2 = this.mNumOfActiveSDs) > 0 && i2 != i3) {
            IMSLog.i(str, i, "send Publish when registered");
            handlePublishDialog(cmcRegistration);
        }
    }

    private void clearAllCallsForCmcHandover(int i) {
        String str = LOG_TAG;
        Log.d(str, "clearAllCallsForCmcHandover cmcType " + i);
        this.mImsCallSessionManager.removeSessionByCmcType(i);
        this.mVolteSvcIntf.clearAllCallInternal(i);
    }

    public void sendDummyPublishDialog(int i, int i2) {
        PublishDialog publishDialog = new PublishDialog();
        publishDialog.setCallCount(1);
        publishDialog.addCallId(McsConstants.AccountStatus.DELETE);
        publishDialog.addCallDomain(9);
        publishDialog.addCallStatus(0);
        publishDialog.addCallType(1);
        publishDialog.addCallDirection(0);
        publishDialog.addCallRemoteUri("");
        publishDialog.addCallPullable(true);
        publishDialog.addCallNumberPresentation(0);
        publishDialog.addCallCnapNamePresentation(0);
        publishDialog.addCallCnapName("");
        publishDialog.addCallMpty(false);
        publishDialog.addConnectedTime(0);
        sendPublishDialog(i, publishDialog, i2);
    }

    public ImsCallSession getSessionByCmcType(int i) {
        ImsCallSession imsCallSession = null;
        for (ImsCallSession next : this.mImsCallSessionManager.getUnmodifiableSessionMap().values()) {
            if (i == next.getCmcType()) {
                imsCallSession = next;
            }
        }
        return imsCallSession;
    }

    public ImsCallSession getSessionByCmcTypeAndState(int i, CallConstants.STATE state) {
        ImsCallSession imsCallSession = null;
        for (ImsCallSession next : this.mImsCallSessionManager.getUnmodifiableSessionMap().values()) {
            if (i == next.getCmcType() && next.getCallState() == state) {
                imsCallSession = next;
            }
        }
        return imsCallSession;
    }

    public boolean hasActiveCmcCallsession(int i) {
        ArrayList arrayList = new ArrayList();
        arrayList.add(CallConstants.STATE.InCall);
        arrayList.add(CallConstants.STATE.HoldingCall);
        arrayList.add(CallConstants.STATE.HeldCall);
        arrayList.add(CallConstants.STATE.ResumingCall);
        arrayList.add(CallConstants.STATE.ModifyingCall);
        arrayList.add(CallConstants.STATE.ModifyRequested);
        arrayList.add(CallConstants.STATE.HoldingVideo);
        arrayList.add(CallConstants.STATE.VideoHeld);
        arrayList.add(CallConstants.STATE.ResumingVideo);
        for (ImsCallSession next : this.mImsCallSessionManager.getUnmodifiableSessionMap().values()) {
            if (i == next.getCmcType() && arrayList.contains(next.getCallState())) {
                return true;
            }
        }
        return false;
    }

    public boolean isCmcRegExist(int i) {
        ImsProfile imsProfile;
        Iterator<Registration> it = this.mRegistrationList.iterator();
        boolean z = false;
        while (it.hasNext()) {
            Registration next = it.next();
            if (!(next == null || ((next.getImsRegi().getPhoneId() != i && !ImsRegistry.getCmcAccountManager().isSupportDualSimCMC()) || (imsProfile = next.getImsRegi().getImsProfile()) == null || imsProfile.getCmcType() == 0))) {
                z = true;
            }
        }
        return z;
    }

    public int getSessionCountByCmcType(int i, int i2) {
        int i3 = 0;
        for (ImsCallSession next : this.mImsCallSessionManager.getUnmodifiableSessionMap().values()) {
            ImsRegistration registration = next.getRegistration();
            if (registration != null) {
                ImsProfile imsProfile = registration.getImsProfile();
                if (next.getPhoneId() == i && imsProfile.getCmcType() == i2) {
                    i3++;
                }
            }
        }
        return i3;
    }

    public void onCmcDtmfInfo(DtmfInfo dtmfInfo) {
        Log.i(LOG_TAG, "onCmcDtmfInfo");
        int i = ImsRegistry.getICmcConnectivityController().isEnabledWifiDirectFeature() ? 7 : 5;
        for (int i2 = 1; i2 <= i; i2 += 2) {
            ImsCallSession sessionByCmcType = getSessionByCmcType(i2);
            if (sessionByCmcType != null) {
                sessionByCmcType.notifyCmcDtmfEvent(dtmfInfo.getEvent());
            }
        }
    }

    public class p2pCallbackHandler extends Handler {
        public static final int P2P_DISCOVERY_RESULT = 1;
        private ImsRegistration mP2pRegiInfo = null;

        public p2pCallbackHandler(Looper looper) {
            super(looper);
        }

        public void setP2pRegiInfo(ImsRegistration imsRegistration) {
            this.mP2pRegiInfo = imsRegistration;
        }

        public void handleMessage(Message message) {
            boolean z = true;
            if (message.what != 1) {
                Log.i(CmcServiceHelper.LOG_TAG, "P2P Discovery invalid callback " + message.what);
                return;
            }
            Log.i(CmcServiceHelper.LOG_TAG, "P2P Discovery result = " + message.arg1);
            CmcServiceHelper.this.printP2pList();
            CmcServiceHelper.this.mIsP2pDiscoveryDone = true;
            ImsRegistration imsRegistration = this.mP2pRegiInfo;
            if (imsRegistration != null) {
                if (!CmcServiceHelper.this.isInP2pArea(imsRegistration)) {
                    Log.i(CmcServiceHelper.LOG_TAG, "Notify empty DIALOG event after P2P discovery done");
                    DialogEvent dialogEvent = CmcServiceHelper.this.mLastCmcDialogEvent[this.mP2pRegiInfo.getPhoneId()];
                    if (dialogEvent != null) {
                        dialogEvent.clearDialogList();
                        SecImsNotifier.getInstance().onDialogEvent(dialogEvent);
                    }
                } else if (CmcServiceHelper.this.mNeedToNotifyAfterP2pDiscovery) {
                    Log.i(CmcServiceHelper.LOG_TAG, "Notify pending DIALOG event after P2P discovery done");
                    DialogEvent dialogEvent2 = CmcServiceHelper.this.mLastCmcDialogEvent[this.mP2pRegiInfo.getPhoneId()];
                    if (dialogEvent2 != null) {
                        SecImsNotifier.getInstance().onDialogEvent(dialogEvent2);
                        Iterator it = dialogEvent2.getDialogList().iterator();
                        while (true) {
                            if (!it.hasNext()) {
                                z = false;
                                break;
                            }
                            Dialog dialog = (Dialog) it.next();
                            if (dialog != null && dialog.getState() == 1) {
                                break;
                            }
                        }
                        if (z) {
                            CmcServiceHelper.this.mIsCmcPdCheckRespReceived.put(Integer.valueOf(dialogEvent2.getPhoneId()), Boolean.FALSE);
                            CmcServiceHelper.this.startCmcPdCheckTimer(dialogEvent2.getPhoneId(), 20000, this.mP2pRegiInfo.getHandle(), "sip:" + dialogEvent2.getMsisdn() + "@samsungims.com;gr=urn:duid:" + ImsRegistry.getCmcAccountManager().getCurrentLineOwnerDeviceId(), true);
                        } else {
                            Log.i(CmcServiceHelper.LOG_TAG, "No cofirmed Dilaog in nofity");
                            CmcServiceHelper.this.stopCmcPdCheckTimer(dialogEvent2.getPhoneId());
                            CmcServiceHelper.this.mLastCmcEndCallReason = 200;
                        }
                    }
                }
            }
            CmcServiceHelper.this.mNeedToNotifyAfterP2pDiscovery = false;
        }
    }

    private void handlePublishDialog(ImsRegistration imsRegistration) {
        int phoneId = imsRegistration.getPhoneId();
        int cmcType = imsRegistration.getImsProfile().getCmcType();
        if (hasActiveCmcCallsession(cmcType)) {
            Log.i(LOG_TAG, "exist Active PD callsession. do not send PUBLISH msg.");
        } else if (hasActiveCmcCallsession(0)) {
            Log.i(LOG_TAG, "Send Publish for external VoLTE Call.");
            sendPublishDialogInternal(phoneId, imsRegistration);
            this.mCsPublishDialogMap.remove(Integer.valueOf(phoneId));
        } else if (this.mCsPublishDialogMap.containsKey(Integer.valueOf(phoneId))) {
            Log.i(LOG_TAG, "Send Publish for external CS call.");
            sendPublishDialog(phoneId, this.mCsPublishDialogMap.get(Integer.valueOf(phoneId)), cmcType);
        } else {
            Log.i(LOG_TAG, "sendDummyPublishDialog because do not have external call.");
            sendDummyPublishDialog(phoneId, cmcType);
        }
    }

    /* access modifiers changed from: private */
    public void printP2pList() {
        Collection<MdmnServiceInfo> supportDevices;
        MdmnNsdWrapper mdmnNsdWrapper = this.mNsd;
        if (mdmnNsdWrapper != null && (supportDevices = mdmnNsdWrapper.getSupportDevices()) != null) {
            Log.i(LOG_TAG, "P2P list size : " + supportDevices.size());
            String str = CmcConstants.URN_PREFIX + ImsRegistry.getCmcAccountManager().getCurrentLineOwnerDeviceId();
            for (MdmnServiceInfo next : supportDevices) {
                String str2 = str.equals(next.getDeviceId()) ? "PD" : ImConstants.MessageCreatorTag.SD;
                Log.i(LOG_TAG, "line id = " + next.getLineId() + ", device id = " + next.getDeviceId() + ", deviceType = " + str2);
                IMSLog.c(LogClass.CMC_P2P_DEVICE_LIST, str2);
            }
        }
    }

    public void startP2pDiscovery(List<String> list) {
        if (this.mNsd != null && list != null && list.size() > 0) {
            int startDiscovery = this.mNsd.startDiscovery(this.mP2pCallbackHandler, 1, new ArrayList(list));
            String str = LOG_TAG;
            Log.i(str, "startDiscovery result = " + startDiscovery + " hostlist " + list);
        }
    }

    public void startP2p(String str, String str2) {
        if (this.mNsd == null) {
            String str3 = LOG_TAG;
            Log.i(str3, "startP2p lineId : " + str2);
            Log.i(str3, "startP2p deviceId : " + str);
            this.mNsdServiceInfo = new MdmnServiceInfo(str, str2);
            this.mNsd = new MdmnNsdWrapper(this.mContext, this.mNsdServiceInfo);
        }
        Log.i(LOG_TAG, "start Nsd");
        this.mNsd.start();
    }

    public void setP2pServiceInfo(String str, String str2) {
        String str3 = LOG_TAG;
        Log.i(str3, "set lineId " + str2);
        Log.i(str3, "set deviceId " + str);
        if (this.mNsd != null) {
            this.mNsd.setServiceInfo(new MdmnServiceInfo(str, str2));
        }
    }

    /* access modifiers changed from: package-private */
    public void updateCmcP2pList(ImsRegistration imsRegistration, CallProfile callProfile) {
        String str = LOG_TAG;
        Log.i(str, "updateCmcP2pList currentRat " + imsRegistration.getCurrentRat());
        if (this.mNsd != null && imsRegistration.getCurrentRat() == 18) {
            printP2pList();
            Collection<MdmnServiceInfo> supportDevices = this.mNsd.getSupportDevices();
            if (!(supportDevices == null || callProfile == null)) {
                if (imsRegistration.getImsProfile().getCmcType() != 2 || callProfile.getReplaceSipCallId() == null) {
                    ArrayList arrayList = new ArrayList();
                    for (MdmnServiceInfo next : supportDevices) {
                        String lineId = next.getLineId();
                        String deviceId = next.getDeviceId();
                        arrayList.add("sip:" + lineId + "@samsungims.com;gr=" + deviceId);
                    }
                    callProfile.setP2p(arrayList);
                } else {
                    Log.i(str, "Do not set p2p list in case of CMC handover");
                }
            }
            startP2pDiscovery(ImsRegistry.getCmcAccountManager().getRegiEventNotifyHostInfo());
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isP2pDiscoveryDone() {
        return this.mIsP2pDiscoveryDone;
    }

    /* access modifiers changed from: package-private */
    public void setNeedToNotifyAfterP2pDiscovery(boolean z) {
        this.mNeedToNotifyAfterP2pDiscovery = z;
    }

    /* access modifiers changed from: package-private */
    public boolean isInP2pArea(ImsRegistration imsRegistration) {
        if (this.mNsd == null || imsRegistration.getCurrentRat() != 18) {
            return false;
        }
        if (imsRegistration.getImsProfile().getCmcType() == 2) {
            String str = CmcConstants.URN_PREFIX + ImsRegistry.getCmcAccountManager().getCurrentLineOwnerDeviceId();
            Log.i(LOG_TAG, "PD deviceId: " + str);
            Collection<MdmnServiceInfo> supportDevices = this.mNsd.getSupportDevices();
            if (supportDevices == null) {
                return false;
            }
            for (MdmnServiceInfo deviceId : supportDevices) {
                String deviceId2 = deviceId.getDeviceId();
                String str2 = LOG_TAG;
                Log.i(str2, "p2p deviceId: " + deviceId2);
                if (str.equals(deviceId2)) {
                    Log.i(str2, "PD and SD are in P2P area");
                    return true;
                }
            }
            return false;
        } else if (ImsCallUtil.isCmcSecondaryType(imsRegistration.getImsProfile().getCmcType())) {
            return true;
        } else {
            return false;
        }
    }

    public int getCsCallPhoneIdByState(int i) {
        String str = LOG_TAG;
        Log.i(str, "getCsCallPhoneIdByState state : " + i);
        if (this.mCsPublishDialogMap.size() > 0) {
            for (Map.Entry next : this.mCsPublishDialogMap.entrySet()) {
                int intValue = ((Integer) next.getKey()).intValue();
                PublishDialog publishDialog = (PublishDialog) next.getValue();
                int callCount = publishDialog.getCallCount();
                int[] callStatus = publishDialog.getCallStatus();
                int i2 = 0;
                while (true) {
                    if (i2 < callCount) {
                        if (callStatus[i2] == i) {
                            String str2 = LOG_TAG;
                            Log.i(str2, "phone id for cs call : " + intValue);
                            return intValue;
                        }
                        i2++;
                    }
                }
            }
        }
        Log.i(LOG_TAG, "external CS call is not found");
        return -1;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x006d, code lost:
        if (r9 != r3[0]) goto L_0x0074;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0072, code lost:
        if (r21[0] == 3) goto L_0x0074;
     */
    /* JADX WARNING: Removed duplicated region for block: B:124:0x0375  */
    /* JADX WARNING: Removed duplicated region for block: B:125:0x037b  */
    /* JADX WARNING: Removed duplicated region for block: B:132:0x038e  */
    /* JADX WARNING: Removed duplicated region for block: B:133:0x0390  */
    /* JADX WARNING: Removed duplicated region for block: B:160:0x04ba  */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x00a0  */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x00d1  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void sendPublishDialog(int r55, com.android.internal.telephony.PublishDialog r56, int r57) {
        /*
            r54 = this;
            r0 = r54
            r15 = r55
            r14 = r57
            int r13 = r56.getCallCount()
            int[] r12 = r56.getCallId()
            int[] r11 = r56.getCallDomain()
            int[] r21 = r56.getCallStatus()
            int[] r22 = r56.getCallType()
            int[] r23 = r56.getCallDirection()
            java.lang.String[] r24 = r56.getCallRemoteUri()
            boolean[] r1 = r56.getCallPullable()
            int[] r25 = r56.getCallNumberPresentation()
            long[] r2 = r56.getConnectedTime()
            boolean[] r26 = r56.getCallMpty()
            java.util.ArrayList r8 = new java.util.ArrayList
            r8.<init>()
            if (r12 == 0) goto L_0x0610
            int r3 = r12.length
            r7 = 1
            if (r3 < r7) goto L_0x0610
            if (r11 == 0) goto L_0x0610
            int r3 = r11.length
            if (r3 >= r7) goto L_0x0044
            goto L_0x0610
        L_0x0044:
            r6 = 0
            r3 = r11[r6]
            r5 = 3
            r4 = 2
            if (r3 != r7) goto L_0x00d8
            java.util.Map<java.lang.Integer, com.android.internal.telephony.PublishDialog> r3 = r0.mCsPublishDialogMap
            java.lang.Integer r9 = java.lang.Integer.valueOf(r55)
            boolean r3 = r3.containsKey(r9)
            if (r3 == 0) goto L_0x0070
            java.util.Map<java.lang.Integer, com.android.internal.telephony.PublishDialog> r3 = r0.mCsPublishDialogMap
            java.lang.Integer r9 = java.lang.Integer.valueOf(r55)
            java.lang.Object r3 = r3.get(r9)
            com.android.internal.telephony.PublishDialog r3 = (com.android.internal.telephony.PublishDialog) r3
            int[] r3 = r3.getCallStatus()
            r9 = r21[r6]
            if (r9 != r5) goto L_0x0076
            r3 = r3[r6]
            if (r9 == r3) goto L_0x0076
            goto L_0x0074
        L_0x0070:
            r3 = r21[r6]
            if (r3 != r5) goto L_0x0076
        L_0x0074:
            r3 = r7
            goto L_0x0077
        L_0x0076:
            r3 = r6
        L_0x0077:
            if (r3 == 0) goto L_0x0091
            com.sec.internal.constants.ims.servicemodules.volte2.CallConstants$STATE r3 = com.sec.internal.constants.ims.servicemodules.volte2.CallConstants.STATE.IncomingCall
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r3 = r0.getSessionByCmcTypeAndState(r14, r3)
            if (r3 == 0) goto L_0x0091
            java.lang.String r9 = LOG_TAG
            java.lang.String r10 = "Send 180 Ringing msg for CMC CS call."
            android.util.Log.i(r9, r10)
            com.sec.internal.interfaces.ims.core.handler.IVolteServiceInterface r9 = r0.mVolteSvcIntf
            int r3 = r3.getSessionId()
            r9.handleCmcCsfb(r3)
        L_0x0091:
            java.util.Map<java.lang.Integer, com.android.internal.telephony.PublishDialog> r3 = r0.mCsPublishDialogMap
            java.lang.Integer r9 = java.lang.Integer.valueOf(r55)
            r10 = r56
            r3.put(r9, r10)
            r3 = r6
            r9 = r3
        L_0x009e:
            if (r3 >= r13) goto L_0x00ad
            r10 = r21[r3]
            int r10 = com.sec.internal.helper.ImsCallUtil.convertCsCallStateToDialogState(r10)
            if (r10 != r4) goto L_0x00aa
            int r9 = r9 + 1
        L_0x00aa:
            int r3 = r3 + 1
            goto L_0x009e
        L_0x00ad:
            java.lang.String r3 = LOG_TAG
            java.lang.StringBuilder r10 = new java.lang.StringBuilder
            r10.<init>()
            java.lang.String r5 = "mExtConfirmedCsCallCnt: "
            r10.append(r5)
            int r5 = r0.mExtConfirmedCsCallCnt
            r10.append(r5)
            java.lang.String r5 = ", extConfirmedCsCallCnt: "
            r10.append(r5)
            r10.append(r9)
            java.lang.String r5 = r10.toString()
            android.util.Log.i(r3, r5)
            int r3 = r0.mExtConfirmedCsCallCnt
            if (r3 == r9) goto L_0x00d6
            com.sec.internal.ims.servicemodules.options.IOptionsServiceInterface r3 = r0.mOptionsSvcIntf
            r3.updateCmcExtCallCount(r15, r9)
        L_0x00d6:
            r0.mExtConfirmedCsCallCnt = r9
        L_0x00d8:
            com.sec.ims.ImsRegistration r27 = r0.getCmcRegistration(r15, r6, r14)
            if (r27 != 0) goto L_0x00df
            return
        L_0x00df:
            java.util.Map<java.lang.Integer, java.lang.Long> r3 = r0.mCmcCallEstablishTimeMap
            r3.clear()
            r3 = r6
        L_0x00e5:
            java.lang.String r5 = ","
            if (r3 >= r13) goto L_0x0161
            java.lang.StringBuilder r9 = new java.lang.StringBuilder
            r9.<init>()
            java.lang.String r10 = "#"
            r9.append(r10)
            r9.append(r3)
            r9.append(r5)
            r10 = r12[r3]
            r9.append(r10)
            r9.append(r5)
            r10 = r11[r3]
            r9.append(r10)
            r9.append(r5)
            r10 = r21[r3]
            r9.append(r10)
            r9.append(r5)
            r10 = r22[r3]
            r9.append(r10)
            r9.append(r5)
            r10 = r23[r3]
            r9.append(r10)
            r9.append(r5)
            boolean r10 = r1[r3]
            r9.append(r10)
            r9.append(r5)
            r10 = r25[r3]
            r9.append(r10)
            r9.append(r5)
            boolean r10 = r26[r3]
            r9.append(r10)
            r9.append(r5)
            r4 = r2[r3]
            r9.append(r4)
            java.lang.String r4 = r9.toString()
            r5 = 1879048193(0x70000001, float:1.5845634E29)
            com.sec.internal.log.IMSLog.c(r5, r4)
            r4 = r21[r3]
            if (r4 != r7) goto L_0x015d
            java.util.Map<java.lang.Integer, java.lang.Long> r4 = r0.mCmcCallEstablishTimeMap
            r5 = r12[r3]
            java.lang.Integer r5 = java.lang.Integer.valueOf(r5)
            r9 = r2[r3]
            java.lang.Long r9 = java.lang.Long.valueOf(r9)
            r4.put(r5, r9)
        L_0x015d:
            int r3 = r3 + 1
            r4 = 2
            goto L_0x00e5
        L_0x0161:
            boolean r2 = r54.hasInternalCallToIgnorePublishDialog(r55)
            if (r2 == 0) goto L_0x016f
            java.lang.String r0 = LOG_TAG
            java.lang.String r1 = "Ignore sendPublishDialog : PD has internal call"
            android.util.Log.i(r0, r1)
            return
        L_0x016f:
            boolean r28 = r54.isNeedDelayToSendPublishDialog(r55)
            int r29 = r27.getHandle()
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r4 = "sip:"
            r2.append(r4)
            java.lang.String r3 = r27.getImpi()
            r2.append(r3)
            java.lang.String r3 = r2.toString()
            boolean r2 = r0.mCmcTotalMnoPullable
            r0.mCmcTotalMnoPullable = r7
            if (r1 == 0) goto L_0x01a1
            r9 = r6
        L_0x0194:
            int r10 = r1.length
            if (r9 >= r10) goto L_0x01a1
            boolean r10 = r1[r9]
            if (r10 != 0) goto L_0x019e
            r0.mCmcTotalMnoPullable = r6
            goto L_0x01a1
        L_0x019e:
            int r9 = r9 + 1
            goto L_0x0194
        L_0x01a1:
            r1 = r6
            r9 = r1
            r10 = r9
            r31 = r10
            r30 = r7
        L_0x01a8:
            java.lang.String r6 = ";gr="
            if (r10 >= r13) goto L_0x0462
            java.lang.String r19 = "test_local_tag"
            java.lang.String r32 = "test_remote_tag"
            java.lang.String r7 = LOG_TAG
            r56 = r3
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            r34 = r5
            java.lang.String r5 = "[CallInfo #"
            r3.append(r5)
            r3.append(r10)
            java.lang.String r5 = "] callId: "
            r3.append(r5)
            r5 = r12[r10]
            r3.append(r5)
            java.lang.String r5 = ", domain: "
            r3.append(r5)
            r5 = r11[r10]
            r3.append(r5)
            java.lang.String r5 = ", callState: "
            r3.append(r5)
            r5 = r21[r10]
            r3.append(r5)
            java.lang.String r5 = ", callType: "
            r3.append(r5)
            r5 = r22[r10]
            r3.append(r5)
            java.lang.String r5 = ", callDirections: "
            r3.append(r5)
            r5 = r23[r10]
            r3.append(r5)
            java.lang.String r5 = ", remoteUris: "
            r3.append(r5)
            r5 = r24[r10]
            java.lang.String r5 = com.sec.internal.log.IMSLog.checker(r5)
            r3.append(r5)
            java.lang.String r5 = ", cmcType: "
            r3.append(r5)
            r3.append(r14)
            java.lang.String r5 = ", phoneId: "
            r3.append(r5)
            r3.append(r15)
            java.lang.String r3 = r3.toString()
            android.util.Log.i(r7, r3)
            r3 = r11[r10]
            r5 = 2
            if (r3 != r5) goto L_0x024f
            boolean r3 = r0.mCmcTotalMnoPullable
            if (r2 == r3) goto L_0x024c
            java.lang.Boolean r2 = java.lang.Boolean.valueOf(r2)
            boolean r3 = r0.mCmcTotalMnoPullable
            java.lang.Boolean r3 = java.lang.Boolean.valueOf(r3)
            java.lang.Object[] r2 = new java.lang.Object[]{r2, r3}
            java.lang.String r3 = "Trying call sendPublishDialogInternal(). CmcTotalMnoPullable changed : %s ==> %s"
            java.lang.String r2 = java.lang.String.format(r3, r2)
            android.util.Log.i(r7, r2)
            r0.sendPublishDialogInternal((int) r15, (int) r14)
            r49 = r56
            r48 = r4
            r15 = r8
            r32 = r11
            r53 = r13
            r50 = r34
            goto L_0x046d
        L_0x024c:
            r35 = r2
            goto L_0x02bf
        L_0x024f:
            if (r3 == r5) goto L_0x03b4
            r3 = r12[r10]
            java.lang.String r3 = java.lang.String.valueOf(r3)
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            r5.append(r4)
            r35 = r2
            java.lang.String r2 = r27.getImpi()
            r5.append(r2)
            r5.append(r6)
            java.lang.String r2 = r27.getInstanceId()
            r5.append(r2)
            java.lang.String r2 = r5.toString()
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            r5.append(r4)
            java.lang.String r6 = r27.getImpi()
            r5.append(r6)
            java.lang.String r5 = r5.toString()
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            r6.append(r4)
            r20 = r2
            r2 = r24[r10]
            r6.append(r2)
            java.lang.String r2 = r6.toString()
            r6 = r25[r10]
            r36 = r2
            r2 = 2
            if (r6 != r2) goto L_0x02a6
            java.lang.String r2 = "anonymous"
            goto L_0x02a8
        L_0x02a6:
            r2 = r24[r10]
        L_0x02a8:
            r6 = r23[r10]
            r37 = r21[r10]
            int r37 = com.sec.internal.helper.ImsCallUtil.convertCsCallStateToDialogState(r37)
            r38 = r2
            r2 = r22[r10]
            r39 = r3
            r3 = 911(0x38f, float:1.277E-42)
            if (r2 != r3) goto L_0x02d1
            java.lang.String r2 = "ignore publish dialog when call type is 911 (Emergency)"
            android.util.Log.i(r7, r2)
        L_0x02bf:
            r49 = r56
            r48 = r4
            r15 = r8
            r0 = r10
            r32 = r11
            r36 = r12
            r53 = r13
            r50 = r34
        L_0x02cd:
            r34 = 0
            goto L_0x0448
        L_0x02d1:
            boolean r3 = r26[r10]
            r41 = r4
            r4 = 1
            if (r3 != r4) goto L_0x031a
            r2 = r21[r10]
            r3 = 2
            if (r2 != r3) goto L_0x02f0
            r49 = r56
            r15 = r8
            r0 = r10
            r32 = r11
            r36 = r12
            r53 = r13
            r50 = r34
            r48 = r41
            r30 = 2
        L_0x02ed:
            r31 = 1
            goto L_0x02cd
        L_0x02f0:
            if (r2 == 0) goto L_0x0309
            r3 = 7
            if (r2 == r3) goto L_0x0309
            r3 = 8
            if (r2 != r3) goto L_0x02fa
            goto L_0x0309
        L_0x02fa:
            r49 = r56
            r15 = r8
            r0 = r10
            r32 = r11
            r36 = r12
            r53 = r13
            r50 = r34
            r48 = r41
            goto L_0x02ed
        L_0x0309:
            r49 = r56
            r15 = r8
            r0 = r10
            r32 = r11
            r36 = r12
            r53 = r13
            r50 = r34
            r48 = r41
            r30 = 0
            goto L_0x02ed
        L_0x031a:
            if (r2 == 0) goto L_0x0357
            r3 = 911(0x38f, float:1.277E-42)
            if (r2 != r3) goto L_0x0326
            r4 = r3
            r16 = r5
            r3 = 3
            r5 = 1
            goto L_0x035d
        L_0x0326:
            r4 = 1
            if (r2 == r4) goto L_0x0339
            r3 = 2
            if (r2 == r3) goto L_0x0339
            r3 = 3
            if (r2 != r3) goto L_0x0330
            goto L_0x033a
        L_0x0330:
            r16 = r5
            r2 = 0
            r33 = 0
            r40 = 0
            r5 = r4
            goto L_0x0371
        L_0x0339:
            r3 = 3
        L_0x033a:
            if (r2 == r4) goto L_0x0347
            r4 = 2
            if (r2 == r4) goto L_0x0345
            if (r2 == r3) goto L_0x0343
            r2 = 0
            goto L_0x0348
        L_0x0343:
            r2 = 2
            goto L_0x0348
        L_0x0345:
            r2 = 4
            goto L_0x0348
        L_0x0347:
            r2 = r3
        L_0x0348:
            r4 = r21[r10]
            r16 = r5
            r5 = 1
            if (r4 != r5) goto L_0x0355
            r33 = r3
            r40 = r33
            r4 = 0
            goto L_0x0371
        L_0x0355:
            r4 = 0
            goto L_0x036d
        L_0x0357:
            r16 = r5
            r3 = 3
            r5 = 1
            r4 = 911(0x38f, float:1.277E-42)
        L_0x035d:
            if (r2 != r4) goto L_0x0361
            r4 = 7
            goto L_0x0362
        L_0x0361:
            r4 = r5
        L_0x0362:
            r2 = r21[r10]
            if (r2 != r5) goto L_0x036b
            r33 = r3
            r2 = r4
            r4 = r5
            goto L_0x036f
        L_0x036b:
            r2 = r4
            r4 = r5
        L_0x036d:
            r33 = 0
        L_0x036f:
            r40 = 0
        L_0x0371:
            r3 = r21[r10]
            if (r3 != r5) goto L_0x037b
            r17 = r4
            r45 = r5
            r4 = 2
            goto L_0x0387
        L_0x037b:
            r4 = 2
            if (r3 != r4) goto L_0x0383
            r45 = r4
            r17 = 0
            goto L_0x0387
        L_0x0383:
            r17 = 0
            r45 = 0
        L_0x0387:
            if (r17 == 0) goto L_0x0390
            boolean r4 = r0.mCmcTotalMnoPullable
            if (r4 != 0) goto L_0x038e
            goto L_0x0390
        L_0x038e:
            r4 = 0
            goto L_0x0391
        L_0x0390:
            r4 = r5
        L_0x0391:
            if (r3 == 0) goto L_0x039a
            r5 = 7
            if (r3 == r5) goto L_0x039a
            r5 = 8
            if (r3 != r5) goto L_0x039c
        L_0x039a:
            int r9 = r9 + 1
        L_0x039c:
            int r1 = r1 + 1
            r42 = r2
            r47 = r4
            r3 = r20
            r43 = r33
            r2 = r39
            r4 = r2
            r46 = r40
            r33 = r1
            r40 = r6
            r39 = r37
            r37 = r9
            goto L_0x03d9
        L_0x03b4:
            r35 = r2
            r41 = r4
            java.lang.String r2 = ""
            java.lang.String r3 = "primary_device_dialog_id"
            r33 = r1
            r4 = r2
            r16 = r4
            r36 = r16
            r38 = r36
            r37 = r9
            r39 = 0
            r40 = 0
            r42 = 0
            r43 = 0
            r45 = 0
            r46 = 0
            r47 = 0
            r2 = r3
            r3 = r38
        L_0x03d9:
            com.sec.ims.Dialog r6 = new com.sec.ims.Dialog
            r1 = r6
            java.lang.String r9 = ""
            java.lang.String r5 = ""
            r0 = r10
            r10 = r5
            r20 = 0
            r5 = r56
            r44 = 3
            r48 = r41
            r17 = 1
            r49 = r5
            r50 = r34
            r5 = r19
            r56 = r6
            r34 = 0
            r6 = r32
            r51 = r7
            r7 = r16
            r52 = r8
            r8 = r36
            r32 = r11
            r11 = r38
            r36 = r12
            r12 = r39
            r53 = r13
            r13 = r40
            r14 = r42
            r15 = r45
            r16 = r55
            r17 = r43
            r18 = r46
            r19 = r47
            r1.<init>(r2, r3, r4, r5, r6, r7, r8, r9, r10, r11, r12, r13, r14, r15, r16, r17, r18, r19, r20)
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "["
            r1.append(r2)
            r1.append(r0)
            java.lang.String r2 = "] "
            r1.append(r2)
            java.lang.String r2 = r56.toString()
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            r2 = r51
            android.util.Log.i(r2, r1)
            r1 = r56
            r15 = r52
            r15.add(r1)
            r1 = r33
            r9 = r37
        L_0x0448:
            int r10 = r0 + 1
            r0 = r54
            r14 = r57
            r8 = r15
            r11 = r32
            r2 = r35
            r12 = r36
            r4 = r48
            r3 = r49
            r5 = r50
            r13 = r53
            r7 = 1
            r15 = r55
            goto L_0x01a8
        L_0x0462:
            r49 = r3
            r48 = r4
            r50 = r5
            r15 = r8
            r32 = r11
            r53 = r13
        L_0x046d:
            r34 = 0
            r0 = r32[r34]
            r2 = 2
            if (r0 == r2) goto L_0x051a
            r0 = r53
            if (r0 < r2) goto L_0x051a
            if (r31 == 0) goto L_0x051a
            java.lang.String r5 = "test_local_tag"
            java.lang.String r0 = "test_remote_tag"
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            r7 = r48
            r3.append(r7)
            java.lang.String r4 = r27.getImpi()
            r3.append(r4)
            r3.append(r6)
            java.lang.String r4 = r27.getInstanceId()
            r3.append(r4)
            java.lang.String r3 = r3.toString()
            r4 = r22[r34]
            r14 = 1
            if (r4 == r14) goto L_0x04b0
            if (r4 == r2) goto L_0x04b0
            r2 = 3
            if (r4 != r2) goto L_0x04aa
            goto L_0x04b1
        L_0x04aa:
            r2 = 5
            r16 = r2
            r18 = r34
            goto L_0x04b6
        L_0x04b0:
            r2 = 3
        L_0x04b1:
            r4 = 6
            r18 = r2
            r16 = r4
        L_0x04b6:
            r17 = 3
            if (r30 != 0) goto L_0x04bc
            int r9 = r9 + 1
        L_0x04bc:
            r21 = r9
            int r22 = r1 + 1
            com.sec.ims.Dialog r6 = new com.sec.ims.Dialog
            r1 = r6
            java.lang.String r2 = "999"
            java.lang.String r4 = "999"
            java.lang.StringBuilder r8 = new java.lang.StringBuilder
            r8.<init>()
            r8.append(r7)
            java.lang.String r7 = r27.getImpi()
            r8.append(r7)
            java.lang.String r7 = r8.toString()
            java.lang.String r8 = "Conference call"
            java.lang.String r9 = ""
            java.lang.String r10 = ""
            java.lang.String r11 = "Conference call"
            r12 = 2
            r13 = 0
            r19 = 1
            r20 = 0
            r56 = r6
            r6 = r0
            r0 = r14
            r14 = r16
            r0 = r15
            r15 = r30
            r16 = r55
            r1.<init>(r2, r3, r4, r5, r6, r7, r8, r9, r10, r11, r12, r13, r14, r15, r16, r17, r18, r19, r20)
            java.lang.String r1 = LOG_TAG
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "conference: "
            r2.append(r3)
            java.lang.String r3 = r56.toString()
            r2.append(r3)
            java.lang.String r2 = r2.toString()
            android.util.Log.i(r1, r2)
            r1 = r56
            r0.add(r1)
            r9 = r21
            r1 = r22
            goto L_0x051b
        L_0x051a:
            r0 = r15
        L_0x051b:
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "<?xml version=\"1.0\"?>\n\t<dialog-info xmlns=\"urn:ietf:params:xml:ns:dialog-info\" xmlns:sa=\"urn:ietf:params:xml:ns:sa-dialog-info\"\n\t\tversion=\"0\" state=\"full\" entity=\""
            r2.append(r3)
            r4 = r49
            r2.append(r4)
            java.lang.String r3 = "\">\n"
            r2.append(r3)
            java.lang.String r2 = r2.toString()
            java.util.Iterator r3 = r0.iterator()
        L_0x0537:
            boolean r5 = r3.hasNext()
            if (r5 == 0) goto L_0x055f
            java.lang.Object r5 = r3.next()
            com.sec.ims.Dialog r5 = (com.sec.ims.Dialog) r5
            int r6 = r1 - r9
            r7 = 1
            if (r6 <= r7) goto L_0x054b
            r5.setIsExclusive(r7)
        L_0x054b:
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            r6.append(r2)
            java.lang.String r2 = r5.toXmlString()
            r6.append(r2)
            java.lang.String r2 = r6.toString()
            goto L_0x0537
        L_0x055f:
            r7 = 1
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            r3.append(r2)
            java.lang.String r2 = "</dialog-info>"
            r3.append(r2)
            java.lang.String r6 = r3.toString()
            com.sec.internal.constants.ims.servicemodules.volte2.CallConstants$STATE r2 = com.sec.internal.constants.ims.servicemodules.volte2.CallConstants.STATE.InCall
            r3 = r54
            r5 = r57
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r2 = r3.getSessionByCmcTypeAndState(r5, r2)
            if (r2 == 0) goto L_0x057e
            goto L_0x0580
        L_0x057e:
            r7 = r34
        L_0x0580:
            java.lang.String r2 = LOG_TAG
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r8 = "cmcPdCallCnt: "
            r5.append(r8)
            r5.append(r7)
            java.lang.String r8 = ", extCsCallCount: "
            r5.append(r8)
            r5.append(r1)
            java.lang.String r1 = ", endedCallCnt: "
            r5.append(r1)
            r5.append(r9)
            java.lang.String r1 = r5.toString()
            android.util.Log.i(r2, r1)
            boolean r1 = r3.isDuplicatedPublishDialog(r6)
            if (r1 == 0) goto L_0x05b3
            r1 = r32[r34]
            r2 = 9
            if (r1 == r2) goto L_0x05b3
            return
        L_0x05b3:
            if (r7 != 0) goto L_0x060f
            int r1 = r0.size()
            if (r1 <= 0) goto L_0x060f
            com.sec.internal.interfaces.ims.core.handler.IVolteServiceInterface r2 = r3.mVolteSvcIntf
            java.lang.String r5 = "displayName"
            r7 = 6000(0x1770, float:8.408E-42)
            r3 = r29
            r8 = r28
            r2.publishDialog(r3, r4, r5, r6, r7, r8)
            java.util.Iterator r0 = r0.iterator()
        L_0x05cc:
            boolean r1 = r0.hasNext()
            if (r1 == 0) goto L_0x060f
            java.lang.Object r1 = r0.next()
            com.sec.ims.Dialog r1 = (com.sec.ims.Dialog) r1
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            int r3 = r1.getCallType()
            r2.append(r3)
            r3 = r50
            r2.append(r3)
            int r4 = r1.getCallState()
            r2.append(r4)
            r2.append(r3)
            boolean r4 = r1.isExclusive()
            r2.append(r4)
            r2.append(r3)
            java.lang.String r1 = r1.getSipCallId()
            r2.append(r1)
            java.lang.String r1 = r2.toString()
            r2 = 1879048194(0x70000002, float:1.5845636E29)
            com.sec.internal.log.IMSLog.c(r2, r1)
            goto L_0x05cc
        L_0x060f:
            return
        L_0x0610:
            java.lang.String r0 = LOG_TAG
            java.lang.String r1 = "Ignore sendPublishDialog : Array parameters are empty!"
            android.util.Log.e(r0, r1)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.volte2.CmcServiceHelper.sendPublishDialog(int, com.android.internal.telephony.PublishDialog, int):void");
    }

    public void sendPublishDialogInternal(int i, int i2) {
        ImsRegistration cmcRegistration = getCmcRegistration(i, i2);
        if (cmcRegistration != null) {
            sendPublishDialogInternal(i, cmcRegistration);
        }
    }

    public void setCallEstablishTimeExtra(long j) {
        this.mCmcCallEstablishTimeMap.put(-1, Long.valueOf(j));
    }

    public long getCmcCallEstablishTime(String str) {
        if (str == null) {
            Log.i(LOG_TAG, "callid is null");
            return getActiveCmcCallEstablishTime();
        } else if (!this.mCmcCallEstablishTimeMap.isEmpty()) {
            try {
                return this.mCmcCallEstablishTimeMap.get(Integer.valueOf(Integer.parseInt(str))).longValue();
            } catch (NumberFormatException e) {
                String str2 = LOG_TAG;
                Log.e(str2, "callId is not integer type " + str, e);
                return getActiveCmcCallEstablishTime();
            }
        } else {
            Log.i(LOG_TAG, "mCmcCallEstablishTimeMap is empty");
            return 0;
        }
    }

    private long getActiveCmcCallEstablishTime() {
        Iterator<Long> it = this.mCmcCallEstablishTimeMap.values().iterator();
        if (!it.hasNext()) {
            return 0;
        }
        long longValue = it.next().longValue();
        String str = LOG_TAG;
        Log.i(str, "getActiveCmcCallEstablishTime " + longValue);
        return longValue;
    }

    /* access modifiers changed from: package-private */
    public int getSessionCountByCmcType(int i, ImsRegistration imsRegistration) {
        if (imsRegistration != null) {
            int cmcType = imsRegistration.getImsProfile().getCmcType();
            String str = LOG_TAG;
            Log.i(str, "curCmcType : " + cmcType);
            return getSessionCountByCmcType(i, cmcType);
        }
        Log.i(LOG_TAG, "curReg null");
        return 0;
    }

    private boolean isDuplicatedPublishDialog(String str) {
        if (this.mSendPublishDigest != null) {
            long j = this.mSendPublishInvokeTime;
            this.mSendPublishInvokeTime = System.currentTimeMillis();
            this.mSendPublishDigest.reset();
            this.mSendPublishDigest.update(str.getBytes(StandardCharsets.UTF_8));
            byte[] digest = this.mSendPublishDigest.digest();
            if (this.mSendPublishInvokeTime - j >= DUPLICATED_PUBLISH_DENY_TIME_IN_MILLI || !Arrays.equals(this.mSendPublishHashedXml, digest)) {
                this.mSendPublishInvokeCount = 0;
                this.mSendPublishHashedXml = digest;
            } else {
                int i = this.mSendPublishInvokeCount;
                if ((i & 63) == 0) {
                    Log.i(LOG_TAG, String.format("[%d] sendPublishDialog duplicated.", new Object[]{Integer.valueOf(i)}));
                }
                int i2 = this.mSendPublishInvokeCount + 1;
                this.mSendPublishInvokeCount = i2;
                if (i2 <= 50 || Debug.isProductShip()) {
                    return true;
                }
                throw new RuntimeException("Too many sendPublishDialog is called in very short time!\n" + str);
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void sendPublishDialogInternal(int i, ImsRegistration imsRegistration) {
        sendPublishDialogInternal(i, imsRegistration, false);
    }

    /* access modifiers changed from: package-private */
    public void sendPublishDialogInternal(int i, ImsRegistration imsRegistration, boolean z) {
        ImsCallSession sessionByCmcType;
        String str = LOG_TAG;
        Log.i(str, "sendPublishDialogInternal()");
        ArrayList<Dialog> arrayList = new ArrayList<>();
        if (imsRegistration == null) {
            Log.e(str, "Ignore sendPublishDialogInternal : PD is not registered");
            return;
        }
        ImsRegistration cmcRegistration = getCmcRegistration(i, 0);
        if (!(cmcRegistration == null || Mno.KT != Mno.fromName(cmcRegistration.getImsProfile().getMnoName()) || (sessionByCmcType = getSessionByCmcType(0)) == null)) {
            boolean z2 = !TextUtils.isEmpty(sessionByCmcType.getCallProfile().getNumberPlus());
            boolean contains = sessionByCmcType.getCallProfile().getDialingNumber().contains("*77");
            Log.i(str, "hasTwoPhonePrefix=" + contains + " hasNumberPlus=" + z2);
            if (contains || z2) {
                Log.e(str, "Ignore sendPublishDialogInternal in two phone mode");
                return;
            }
        }
        int handle = imsRegistration.getHandle();
        String str2 = "sip:" + imsRegistration.getImpi();
        int[] callCountForSendPublishDialog = getCallCountForSendPublishDialog(i, imsRegistration, arrayList, this.mCmcTotalMnoPullable);
        int i2 = callCountForSendPublishDialog[0];
        int i3 = callCountForSendPublishDialog[1];
        int i4 = callCountForSendPublishDialog[2];
        String str3 = "<?xml version=\"1.0\"?>\n\t<dialog-info xmlns=\"urn:ietf:params:xml:ns:dialog-info\" xmlns:sa=\"urn:ietf:params:xml:ns:sa-dialog-info\"\n\t\tversion=\"0\" state=\"full\" entity=\"" + str2 + "\">\n";
        for (Dialog dialog : arrayList) {
            if (i2 - i4 > 1) {
                dialog.setIsExclusive(true);
            }
            str3 = str3 + dialog.toXmlString();
            IMSLog.c(LogClass.CMC_SEND_PUBLISH_INTERNAL, dialog.getCallType() + "," + dialog.getCallState() + "," + dialog.isExclusive());
        }
        String str4 = str3 + "</dialog-info>";
        Log.i(LOG_TAG, "extPsCallCount: " + i2 + ", validCallCnt: " + i3 + ", endedCallCnt: " + i4);
        if (arrayList.size() > 0) {
            this.mVolteSvcIntf.publishDialog(handle, str2, "displayName", str4, CMC_HANDOVER_TIMER_VALUE, z);
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x007e  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int[] getCallCountForSendPublishDialog(int r31, com.sec.ims.ImsRegistration r32, java.util.List<com.sec.ims.Dialog> r33, boolean r34) {
        /*
            r30 = this;
            r0 = r30
            r1 = r31
            r2 = 3
            int[] r2 = new int[r2]
            com.sec.internal.ims.servicemodules.volte2.ImsCallSessionManager r3 = r0.mImsCallSessionManager
            java.util.Map r3 = r3.getUnmodifiableSessionMap()
            java.util.Collection r3 = r3.values()
            java.util.Iterator r3 = r3.iterator()
        L_0x0015:
            boolean r4 = r3.hasNext()
            if (r4 == 0) goto L_0x0204
            java.lang.Object r4 = r3.next()
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r4 = (com.sec.internal.ims.servicemodules.volte2.ImsCallSession) r4
            if (r4 == 0) goto L_0x01fe
            r5 = -1
            if (r1 == r5) goto L_0x0037
            int r5 = r4.getPhoneId()
            if (r5 == r1) goto L_0x0037
            com.sec.internal.interfaces.ims.core.ICmcAccountManager r5 = com.sec.internal.ims.registry.ImsRegistry.getCmcAccountManager()
            boolean r5 = r5.isSupportDualSimCMC()
            if (r5 != 0) goto L_0x0037
            goto L_0x0015
        L_0x0037:
            com.sec.ims.ImsRegistration r5 = r4.getRegistration()
            com.sec.ims.volte2.data.CallProfile r6 = r4.getCallProfile()
            int r6 = r6.getCallType()
            boolean r7 = com.sec.internal.helper.ImsCallUtil.isE911Call(r6)
            r14 = 1
            r27 = 0
            if (r7 == 0) goto L_0x0054
            java.lang.String r5 = LOG_TAG
            java.lang.String r7 = "Emergency call, ignore to send PUBLISH msg"
            android.util.Log.i(r5, r7)
            goto L_0x0064
        L_0x0054:
            if (r5 == 0) goto L_0x0064
            com.sec.ims.settings.ImsProfile r5 = r5.getImsProfile()
            if (r5 == 0) goto L_0x0064
            int r5 = r5.getCmcType()
            if (r5 != 0) goto L_0x0064
            r5 = r14
            goto L_0x0066
        L_0x0064:
            r5 = r27
        L_0x0066:
            java.lang.String r13 = LOG_TAG
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            r7.<init>()
            java.lang.String r8 = "canSendPublish: "
            r7.append(r8)
            r7.append(r5)
            java.lang.String r7 = r7.toString()
            android.util.Log.i(r13, r7)
            if (r5 == 0) goto L_0x01fe
            com.sec.ims.volte2.data.CallProfile r5 = r4.getCallProfile()
            java.lang.String r5 = r5.getSipCallId()
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            r7.<init>()
            java.lang.String r8 = "sip:"
            r7.append(r8)
            java.lang.String r9 = r32.getImpi()
            r7.append(r9)
            java.lang.String r9 = ";gr="
            r7.append(r9)
            java.lang.String r9 = r32.getInstanceId()
            r7.append(r9)
            java.lang.String r9 = r7.toString()
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            r7.<init>()
            r7.append(r8)
            java.lang.String r10 = r32.getImpi()
            r7.append(r10)
            java.lang.String r17 = r7.toString()
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            r7.<init>()
            r7.append(r8)
            com.sec.ims.volte2.data.CallProfile r8 = r4.getCallProfile()
            java.lang.String r8 = r8.getDialingNumber()
            r7.append(r8)
            java.lang.String r19 = r7.toString()
            java.lang.String r11 = "test_local_tag"
            java.lang.String r12 = "test_remote_tag"
            com.sec.ims.volte2.data.CallProfile r7 = r4.getCallProfile()
            java.lang.String r7 = r7.getDialingNumber()
            com.sec.internal.constants.ims.servicemodules.volte2.CallConstants$STATE r8 = r4.getCallState()
            boolean r10 = r4.mIsEstablished
            com.sec.internal.constants.ims.servicemodules.volte2.CallConstants$STATE r8 = r0.getCallstateForPublishDialog(r8, r10)
            com.sec.ims.volte2.data.CallProfile r10 = r4.getCallProfile()
            boolean r10 = r10.isMOCall()
            r20 = r10 ^ 1
            boolean r10 = com.sec.internal.helper.ImsCallUtil.isEmergencyAudioCall(r6)
            boolean r15 = com.sec.internal.helper.ImsCallUtil.isEmergencyVideoCall(r6)
            boolean r10 = r0.checkIgnorePublishDialogCase(r6, r10, r15)
            if (r10 == 0) goto L_0x0104
            goto L_0x0015
        L_0x0104:
            boolean r10 = com.sec.internal.helper.ImsCallUtil.isVideoCall(r6)
            if (r10 == 0) goto L_0x0119
            int r10 = r0.getDialogDirection(r8)
            int r15 = r0.getDialogDirection(r8)
            r23 = r10
            r24 = r15
            r10 = r27
            goto L_0x013c
        L_0x0119:
            boolean r10 = com.sec.internal.helper.ImsCallUtil.isRttCall(r6)
            if (r10 != 0) goto L_0x0130
            boolean r10 = com.sec.internal.helper.ImsCallUtil.isTtyCall(r6)
            if (r10 == 0) goto L_0x0126
            goto L_0x0130
        L_0x0126:
            int r10 = r0.getDialogDirection(r8)
            r23 = r10
            r10 = r14
            r24 = r27
            goto L_0x013c
        L_0x0130:
            java.lang.String r10 = "pullable false for RTT/TTY call"
            android.util.Log.i(r13, r10)
            r10 = r27
            r23 = r10
            r24 = r23
        L_0x013c:
            com.sec.ims.volte2.data.CallProfile r15 = r4.getCallProfile()
            boolean r15 = r15.isConferenceCall()
            if (r15 == 0) goto L_0x014d
            java.lang.String r7 = "Conference call"
            r21 = r7
            r10 = r27
            goto L_0x014f
        L_0x014d:
            r21 = r7
        L_0x014f:
            boolean r7 = r4.isRemoteHeld()
            int r15 = r0.getDialogCallState(r8, r7)
            if (r15 == r14) goto L_0x015b
            r10 = r27
        L_0x015b:
            if (r10 == 0) goto L_0x0163
            if (r34 != 0) goto L_0x0160
            goto L_0x0163
        L_0x0160:
            r7 = r27
            goto L_0x0164
        L_0x0163:
            r7 = r14
        L_0x0164:
            boolean r10 = r4.isQuantumEncryptionServiceAvailable()
            if (r10 == 0) goto L_0x017c
            com.sec.ims.volte2.data.CallProfile r10 = r4.getCallProfile()
            com.sec.ims.volte2.data.QuantumSecurityInfo r10 = r10.getQuantumSecurityInfo()
            int r10 = r10.getEncryptStatus()
            r14 = 4
            if (r10 == r14) goto L_0x017c
            r28 = 1
            goto L_0x017e
        L_0x017c:
            r28 = r7
        L_0x017e:
            boolean r7 = com.sec.internal.helper.ImsCallUtil.isDuringCallState(r8)
            if (r7 == 0) goto L_0x018b
            r14 = 1
            r7 = r2[r14]
            int r7 = r7 + r14
            r2[r14] = r7
            goto L_0x019c
        L_0x018b:
            r14 = 1
            com.sec.internal.constants.ims.servicemodules.volte2.CallConstants$STATE r7 = com.sec.internal.constants.ims.servicemodules.volte2.CallConstants.STATE.Idle
            if (r8 == r7) goto L_0x0196
            boolean r7 = com.sec.internal.helper.ImsCallUtil.isEndCallState(r8)
            if (r7 == 0) goto L_0x019c
        L_0x0196:
            r7 = 2
            r10 = r2[r7]
            int r10 = r10 + r14
            r2[r7] = r10
        L_0x019c:
            int r18 = com.sec.internal.helper.ImsCallUtil.convertImsCallStateToDialogState(r8)
            com.sec.ims.Dialog r10 = new com.sec.ims.Dialog
            r7 = r10
            java.lang.String r8 = ""
            r25 = r15
            r15 = r8
            java.lang.String r16 = ""
            int r22 = r4.getPhoneId()
            r26 = 0
            r8 = r5
            r4 = r10
            r10 = r5
            r0 = r13
            r13 = r17
            r29 = r14
            r14 = r19
            r17 = r21
            r19 = r20
            r20 = r6
            r21 = r25
            r25 = r28
            r7.<init>(r8, r9, r10, r11, r12, r13, r14, r15, r16, r17, r18, r19, r20, r21, r22, r23, r24, r25, r26)
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            java.lang.String r7 = "["
            r6.append(r7)
            r7 = r2[r27]
            r6.append(r7)
            java.lang.String r7 = "] "
            r6.append(r7)
            java.lang.String r7 = r4.toString()
            r6.append(r7)
            java.lang.String r6 = r6.toString()
            android.util.Log.i(r0, r6)
            boolean r0 = android.text.TextUtils.isEmpty(r5)
            if (r0 != 0) goto L_0x01f5
            r0 = r33
            r0.add(r4)
            goto L_0x01f7
        L_0x01f5:
            r0 = r33
        L_0x01f7:
            r4 = r2[r27]
            int r4 = r4 + 1
            r2[r27] = r4
            goto L_0x0200
        L_0x01fe:
            r0 = r33
        L_0x0200:
            r0 = r30
            goto L_0x0015
        L_0x0204:
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.volte2.CmcServiceHelper.getCallCountForSendPublishDialog(int, com.sec.ims.ImsRegistration, java.util.List, boolean):int[]");
    }

    private boolean checkIgnorePublishDialogCase(int i, boolean z, boolean z2) {
        if (i != 0) {
            return false;
        }
        Log.i(LOG_TAG, "CallType is unknown");
        return true;
    }

    private CallConstants.STATE getCallstateForPublishDialog(CallConstants.STATE state, boolean z) {
        if (state != CallConstants.STATE.IncomingCall || !z) {
            return state;
        }
        Log.i(LOG_TAG, "forced InCall state change for fast establishment [delayed ACK case]");
        return CallConstants.STATE.InCall;
    }

    private int getDialogDirection(CallConstants.STATE state) {
        return state == CallConstants.STATE.InCall ? 3 : 0;
    }

    private int getDialogCallState(CallConstants.STATE state, boolean z) {
        String str = LOG_TAG;
        Log.i(str, "session.mRemoteHeld : " + z);
        if (ImsCallUtil.isHoldCallState(state) || (state == CallConstants.STATE.InCall && z)) {
            return 2;
        }
        return ImsCallUtil.isActiveCallState(state) ? 1 : 0;
    }

    /* access modifiers changed from: package-private */
    public boolean hasInternalCallToIgnorePublishDialog(int i) {
        ImsRegistration registration;
        ImsProfile imsProfile;
        boolean z = false;
        for (ImsCallSession next : this.mImsCallSessionManager.getUnmodifiableSessionMap().values()) {
            if (next != null && ((i == -1 || next.getPhoneId() == i) && (registration = next.getRegistration()) != null && (imsProfile = registration.getImsProfile()) != null && ImsCallUtil.isCmcPrimaryType(imsProfile.getCmcType()))) {
                if (next.getCallState() == CallConstants.STATE.IncomingCall || next.getCallState() == CallConstants.STATE.InCall || (next.getCallState() == CallConstants.STATE.AlertingCall && next.getEndReason() != 5)) {
                    z = true;
                }
            }
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    public boolean isNeedDelayToSendPublishDialog(int i) {
        ImsRegistration registration;
        ImsProfile imsProfile;
        boolean z = false;
        for (ImsCallSession next : this.mImsCallSessionManager.getUnmodifiableSessionMap().values()) {
            if (next != null && ((i == -1 || next.getPhoneId() == i) && (registration = next.getRegistration()) != null && (imsProfile = registration.getImsProfile()) != null && imsProfile.getCmcType() == 1 && next.getEndReason() == 20)) {
                z = true;
            }
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    public ImsRegistration updateAudioInterfaceByCmc(int i, int i2) {
        ImsRegistration imsRegistration = null;
        int i3 = 5;
        if (i2 == 5) {
            if (ImsRegistry.getICmcConnectivityController().isEnabledWifiDirectFeature()) {
                i3 = 7;
            }
            for (int i4 = 1; i4 <= i3; i4 += 2) {
                imsRegistration = getCmcRegistration(i, false, i4);
                if (imsRegistration != null) {
                    this.mMediaController.bindToNetwork(imsRegistration.getNetwork());
                    return imsRegistration;
                }
            }
            return imsRegistration;
        } else if (i2 != 8) {
            return null;
        } else {
            Log.i(LOG_TAG, "updateAudioInterface for CMC SD call.");
            return getCmcRegistration(i, false, 2);
        }
    }

    public boolean isCallServiceAvailableOnSecondary(int i, String str, boolean z) {
        int i2 = 2;
        while (i2 <= 8) {
            ImsRegistration cmcRegistration = getCmcRegistration(i, i2);
            if (!z || cmcRegistration == null) {
                i2 += 2;
            } else {
                String str2 = LOG_TAG;
                Log.i(str2, "isCallServiceAvailableOnSecondary phoneId: " + i + ", service=" + str);
                return cmcRegistration.hasService(str);
            }
        }
        Log.e(LOG_TAG, "disallow Call Service");
        return false;
    }

    /* access modifiers changed from: package-private */
    public void onImsIncomingCallEvent(int i, int i2) {
        int i3 = ImsRegistry.getICmcConnectivityController().isEnabledWifiDirectFeature() ? 7 : 5;
        for (int i4 = 1; i4 <= i3; i4 += 2) {
            int i5 = 0;
            ImsRegistration cmcRegistration = getCmcRegistration(i, false, i4);
            if (cmcRegistration != null && i2 == 0) {
                ImsCallSession sessionByCmcTypeAndState = getSessionByCmcTypeAndState(i4, CallConstants.STATE.InCall);
                ImsCallSession sessionByCmcTypeAndState2 = getSessionByCmcTypeAndState(i4, CallConstants.STATE.HeldCall);
                if (sessionByCmcTypeAndState != null) {
                    i5 = 1;
                }
                if (sessionByCmcTypeAndState2 != null) {
                    i5++;
                }
                if (i5 == 0) {
                    sendPublishDialogInternal(i, cmcRegistration);
                }
            }
        }
        if (i2 > 0) {
            startP2pDiscovery(ImsRegistry.getCmcAccountManager().getRegiEventNotifyHostInfo());
        }
    }

    /* access modifiers changed from: package-private */
    public boolean hasDialingOrIncomingCall() {
        if (getSessionByCmcTypeAndState(0, CallConstants.STATE.IncomingCall) == null && getSessionByCmcTypeAndState(0, CallConstants.STATE.OutGoingCall) == null && getSessionByCmcTypeAndState(0, CallConstants.STATE.AlertingCall) == null) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public void onImsCallEventWhenEstablished(int i, ImsCallSession imsCallSession, ImsRegistration imsRegistration) {
        int i2 = ImsRegistry.getICmcConnectivityController().isEnabledWifiDirectFeature() ? 7 : 5;
        for (int i3 = 1; i3 <= i2; i3 += 2) {
            boolean z = false;
            ImsRegistration cmcRegistration = getCmcRegistration(i, false, i3);
            if (cmcRegistration != null && imsCallSession.getCmcType() == 0) {
                ImsCallSession sessionByCmcType = getSessionByCmcType(i3);
                if (sessionByCmcType != null) {
                    if (imsCallSession.getCallProfile().isMOCall()) {
                        if (sessionByCmcType.getCallState() != CallConstants.STATE.Idle) {
                            z = true;
                        }
                        if (!z) {
                            sendPublishDialogInternal(i, cmcRegistration);
                        }
                    }
                } else if (imsCallSession.getCallProfile().isMOCall()) {
                    sendPublishDialogInternal(i, cmcRegistration);
                } else {
                    int sessionCountByCmcType = getSessionCountByCmcType(i, imsRegistration);
                    if (imsCallSession.getCallProfile().getCallType() == 2 || imsCallSession.getCallProfile().getCallType() == 1 || sessionCountByCmcType > 1) {
                        sendPublishDialogInternal(i, cmcRegistration);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onImsCallEventWithHeldBoth(ImsCallSession imsCallSession, ImsRegistration imsRegistration) {
        if (imsRegistration != null) {
            int phoneId = imsRegistration.getPhoneId();
            int i = ImsRegistry.getICmcConnectivityController().isEnabledWifiDirectFeature() ? 7 : 5;
            for (int i2 = 1; i2 <= i; i2 += 2) {
                boolean z = false;
                ImsRegistration cmcRegistration = getCmcRegistration(phoneId, false, i2);
                if (cmcRegistration != null && imsCallSession.getCmcType() == 0) {
                    ImsCallSession sessionByCmcType = getSessionByCmcType(i2);
                    if (!(sessionByCmcType == null || sessionByCmcType.getCallState() == CallConstants.STATE.Idle)) {
                        z = true;
                    }
                    if (!z) {
                        sendPublishDialogInternal(phoneId, cmcRegistration);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onCallEndedWithSendPublish(int i, ImsCallSession imsCallSession) {
        if (isCmcRegExist(i)) {
            int i2 = ImsRegistry.getICmcConnectivityController().isEnabledWifiDirectFeature() ? 7 : 5;
            for (int i3 = 1; i3 <= i2; i3 += 2) {
                ImsRegistration cmcRegistration = getCmcRegistration(i, false, i3);
                ImsCallSession sessionByCmcTypeAndState = getSessionByCmcTypeAndState(i3, CallConstants.STATE.InCall);
                ImsCallSession sessionByCmcTypeAndState2 = getSessionByCmcTypeAndState(i3, CallConstants.STATE.HeldCall);
                int i4 = sessionByCmcTypeAndState != null ? 1 : 0;
                if (sessionByCmcTypeAndState2 != null) {
                    i4++;
                }
                if (cmcRegistration != null && imsCallSession != null && imsCallSession.getCmcType() == 0) {
                    int cmcBoundSessionId = imsCallSession.getCallProfile().getCmcBoundSessionId();
                    for (ImsCallSession next : this.mImsCallSessionManager.getUnmodifiableSessionMap().values()) {
                        if (next.getCmcType() == i3 && next.getSessionId() != cmcBoundSessionId) {
                            if (next.getCallState() == CallConstants.STATE.OutGoingCall || next.getCallState() == CallConstants.STATE.AlertingCall) {
                                i4++;
                            }
                        }
                    }
                    if (i4 == 0) {
                        sendPublishDialogInternal(i, cmcRegistration);
                    }
                } else if (!(cmcRegistration == null || imsCallSession == null || imsCallSession.getCmcType() != i3)) {
                    if (this.mImsCallSessionManager.getActiveExtCallCount() > 0 && i4 == 0 && (!imsCallSession.mIsEstablished || imsCallSession.getErrorCode() == 6007)) {
                        sendPublishDialogInternal(i, cmcRegistration);
                    }
                    if (imsCallSession.getCmcType() == 1) {
                        sendCmcCallStateForRcs(imsCallSession.getPhoneId(), ImsConstants.CmcInfo.CMC_DUMMY_TEL_NUMBER, false);
                    }
                }
            }
            if (imsCallSession != null && ImsCallUtil.isCmcSecondaryType(imsCallSession.getCmcType())) {
                this.mLastCmcEndCallReason = imsCallSession.getErrorCode();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void sendCmcCallStateForRcs(int i, String str, boolean z) {
        if (getCmcRegistration(i, false, 1) != null) {
            Log.i(LOG_TAG, "sendCmcCallStateForRcs");
            Intent intent = new Intent(this.mContext, CallStateTracker.class);
            intent.setAction(ImsConstants.Intents.ACTION_CALL_STATE_CHANGED);
            intent.putExtra(ImsConstants.Intents.EXTRA_IS_INCOMING, false);
            intent.putExtra(ImsConstants.Intents.EXTRA_TEL_NUMBER, str);
            intent.putExtra(ImsConstants.Intents.EXTRA_PHONE_ID, i);
            intent.putExtra(ImsConstants.Intents.EXTRA_CALL_EVENT, z ? 2 : 1);
            intent.putExtra(ImsConstants.Intents.EXTRA_IS_CMC_CALL, true);
            intent.putExtra(ImsConstants.Intents.EXTRA_IS_CMC_CONNECTED, z);
            this.mContext.sendBroadcast(intent);
        }
    }

    /* access modifiers changed from: package-private */
    public DialogEvent filterOngoingDialogFromDialogEvent(DialogEvent dialogEvent) {
        ArrayList arrayList = new ArrayList();
        for (Dialog dialog : dialogEvent.getDialogList()) {
            if (dialog != null && !this.mImsCallSessionManager.hasSipCallId(dialog.getSipCallId())) {
                arrayList.add(dialog);
            }
        }
        DialogEvent dialogEvent2 = new DialogEvent(dialogEvent.getMsisdn(), arrayList);
        dialogEvent2.setPhoneId(dialogEvent.getPhoneId());
        dialogEvent2.setRegId(dialogEvent.getRegId());
        return dialogEvent2;
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x006b  */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x00ac  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public com.sec.ims.DialogEvent onCmcImsDialogEvent(com.sec.ims.ImsRegistration r10, com.sec.ims.DialogEvent r11) {
        /*
            r9 = this;
            com.sec.ims.settings.ImsProfile r0 = r10.getImsProfile()
            if (r0 == 0) goto L_0x00c6
            com.sec.ims.DialogEvent r1 = r9.filterOngoingDialogFromDialogEvent(r11)
            if (r1 == 0) goto L_0x0014
            java.lang.String r11 = LOG_TAG
            java.lang.String r2 = "Filter DialogEvent"
            android.util.Log.i(r11, r2)
            r11 = r1
        L_0x0014:
            java.util.List r1 = r11.getDialogList()
            java.util.Iterator r1 = r1.iterator()
        L_0x001c:
            boolean r2 = r1.hasNext()
            r3 = 0
            if (r2 == 0) goto L_0x0033
            java.lang.Object r2 = r1.next()
            com.sec.ims.Dialog r2 = (com.sec.ims.Dialog) r2
            if (r2 == 0) goto L_0x001c
            int r2 = r2.getState()
            r4 = 1
            if (r2 != r4) goto L_0x001c
            goto L_0x0034
        L_0x0033:
            r4 = r3
        L_0x0034:
            com.sec.ims.settings.ImsProfile r1 = r10.getImsProfile()
            int r1 = r1.getCmcType()
            r2 = 2
            if (r1 != r2) goto L_0x0068
            com.sec.internal.interfaces.ims.core.ICmcAccountManager r1 = com.sec.internal.ims.registry.ImsRegistry.getCmcAccountManager()
            boolean r1 = r1.isSupportSameWiFiOnly()
            if (r1 == 0) goto L_0x0068
            boolean r1 = r9.isP2pDiscoveryDone()
            if (r1 != 0) goto L_0x0057
            java.lang.String r1 = LOG_TAG
            java.lang.String r2 = "Do not send OPTIONS until P2P discovery done"
            android.util.Log.i(r1, r2)
            goto L_0x0069
        L_0x0057:
            boolean r1 = r9.isInP2pArea(r10)
            if (r1 != 0) goto L_0x0068
            java.lang.String r1 = LOG_TAG
            java.lang.String r2 = "PD and SD are not in P2P area"
            android.util.Log.i(r1, r2)
            r11.clearDialogList()
            goto L_0x0069
        L_0x0068:
            r3 = r4
        L_0x0069:
            if (r3 == 0) goto L_0x00ac
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "sip:"
            r1.append(r2)
            java.lang.String r2 = r11.getMsisdn()
            r1.append(r2)
            java.lang.String r2 = "@samsungims.com;gr="
            r1.append(r2)
            java.lang.String r0 = r0.getPriDeviceIdWithURN()
            r1.append(r0)
            java.lang.String r7 = r1.toString()
            java.util.Map<java.lang.Integer, java.lang.Boolean> r0 = r9.mIsCmcPdCheckRespReceived
            int r1 = r11.getPhoneId()
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)
            java.lang.Boolean r2 = java.lang.Boolean.FALSE
            r0.put(r1, r2)
            int r3 = r11.getPhoneId()
            r4 = 20000(0x4e20, double:9.8813E-320)
            int r6 = r10.getHandle()
            r8 = 1
            r2 = r9
            r2.startCmcPdCheckTimer(r3, r4, r6, r7, r8)
            goto L_0x00be
        L_0x00ac:
            java.lang.String r10 = LOG_TAG
            java.lang.String r0 = "No cofirmed Dilaog in nofity"
            android.util.Log.i(r10, r0)
            int r10 = r11.getPhoneId()
            r9.stopCmcPdCheckTimer(r10)
            r10 = 200(0xc8, float:2.8E-43)
            r9.mLastCmcEndCallReason = r10
        L_0x00be:
            com.sec.ims.DialogEvent[] r9 = r9.mLastCmcDialogEvent
            int r10 = r11.getPhoneId()
            r9[r10] = r11
        L_0x00c6:
            return r11
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.volte2.CmcServiceHelper.onCmcImsDialogEvent(com.sec.ims.ImsRegistration, com.sec.ims.DialogEvent):com.sec.ims.DialogEvent");
    }

    /* access modifiers changed from: protected */
    public void startCmcPdCheckTimer(int i, long j, int i2, String str, boolean z) {
        stopCmcPdCheckTimer(i);
        String str2 = LOG_TAG;
        Log.i(str2, "startCmcPdCheckTimer: millis " + j);
        Bundle bundle = new Bundle();
        bundle.putInt("reg_id", i2);
        bundle.putString("uri", str);
        bundle.putBoolean("is_first_check", z);
        PreciseAlarmManager instance = PreciseAlarmManager.getInstance(this.mContext);
        Message obtainMessage = obtainMessage(33, i, -1, bundle);
        this.mCmcPdCheckTimeOut.put(Integer.valueOf(i), obtainMessage);
        instance.sendMessageDelayed(getClass().getSimpleName(), obtainMessage, j);
    }

    /* access modifiers changed from: protected */
    public void stopCmcPdCheckTimer(int i) {
        if (this.mCmcPdCheckTimeOut.containsKey(Integer.valueOf(i))) {
            String str = LOG_TAG;
            Log.i(str, "stopCmcPdCheckTimer[" + i + "]");
            PreciseAlarmManager.getInstance(this.mContext).removeMessage(this.mCmcPdCheckTimeOut.remove(Integer.valueOf(i)));
        }
    }

    private void checkPdAvailability(int i, Bundle bundle) {
        String string = bundle.getString("uri");
        int i2 = bundle.getInt("reg_id");
        boolean z = bundle.getBoolean("is_first_check");
        String str = LOG_TAG;
        Log.i(str, "checkPdAvailability(), isFirstCheck: " + z);
        if (!this.mIsCmcPdCheckRespReceived.containsKey(Integer.valueOf(i))) {
            return;
        }
        if (this.mIsCmcPdCheckRespReceived.get(Integer.valueOf(i)).booleanValue() || z) {
            this.mOptionsSvcIntf.requestSendCmcCheckMsg(i, i2, string);
            startCmcPdCheckTimer(i, 20000, i2, string, false);
            this.mIsCmcPdCheckRespReceived.put(Integer.valueOf(i), Boolean.FALSE);
            return;
        }
        Log.i(str, "no 200 OK(OPTION) response from PD, remove pulling UI");
        stopCmcPdCheckTimer(i);
        DialogEvent dialogEvent = this.mLastCmcDialogEvent[i];
        if (dialogEvent != null) {
            dialogEvent.clearDialogList();
            SecImsNotifier.getInstance().onDialogEvent(dialogEvent);
        }
        this.mLastCmcEndCallReason = 200;
    }

    public void forwardCmcRecordingEventToSD(int i, int i2, int i3, int i4) {
        ImsCallSession session;
        ImsCallSession session2;
        String str = LOG_TAG;
        Log.i(str, "forwardCmcRecordingEventToSD, recordEvent: " + i2 + ", extra: " + i3 + ", sessionId: " + i4);
        int convertRecordEventForCmcInfo = ImsCallUtil.convertRecordEventForCmcInfo(i2);
        StringBuilder sb = new StringBuilder();
        sb.append("recordInfoMsgEvent : ");
        sb.append(convertRecordEventForCmcInfo);
        Log.i(str, sb.toString());
        if (isCmcRegExist(i) && convertRecordEventForCmcInfo > 0 && (session = this.mImsCallSessionManager.getSession(i4)) != null && session.getCmcType() == 1 && (session2 = this.mImsCallSessionManager.getSession(session.getCallProfile().getCmcBoundSessionId())) != null) {
            Log.i(str, "send CmcRecordingEvent to SD during cmc call relay");
            String sipCallId = session2.getCallProfile().getSipCallId();
            Bundle bundle = new Bundle();
            bundle.putInt("record_event", convertRecordEventForCmcInfo);
            bundle.putInt("extra", i3);
            bundle.putString("sip_call_id", sipCallId);
            this.mVolteSvcIntf.sendCmcInfo(i4, bundle);
        }
    }

    public void onCmcRecordingInfo(CmcInfoEvent cmcInfoEvent) {
        Log.i(LOG_TAG, "onCmcRecordingInfo");
        ImsCallSession sessionByCmcTypeAndState = getSessionByCmcTypeAndState(2, CallConstants.STATE.InCall);
        if (sessionByCmcTypeAndState != null) {
            sessionByCmcTypeAndState.notifyCmcInfoEvent(cmcInfoEvent);
        }
    }

    /* access modifiers changed from: package-private */
    public ImsRegistration getCmcRegistration(int i, boolean z, int i2) {
        Iterator<Registration> it = this.mRegistrationList.iterator();
        while (it.hasNext()) {
            Registration next = it.next();
            if (next != null && ((next.getImsRegi().getPhoneId() == i || ImsRegistry.getCmcAccountManager().isSupportDualSimCMC()) && next.getImsRegi().getImsProfile().hasEmergencySupport() == z && next.getImsRegi().getImsProfile().getCmcType() == i2)) {
                return next.getImsRegi();
            }
        }
        return null;
    }

    private ImsRegistration getCmcRegistration(int i, int i2) {
        return getCmcRegistration(i, false, i2);
    }

    public ImsRegistration getCmcRegistration(int i) {
        Iterator<Registration> it = this.mRegistrationList.iterator();
        while (it.hasNext()) {
            Registration next = it.next();
            if (i == next.getImsRegi().getHandle()) {
                String str = LOG_TAG;
                Log.i(str, "getCmcRegistration: found regId=" + next.getImsRegi().getHandle());
                return next.getImsRegi();
            }
        }
        return null;
    }

    public int getCmcPdRegPhoneId() {
        Iterator<Registration> it = this.mRegistrationList.iterator();
        while (it.hasNext()) {
            Registration next = it.next();
            if (next != null && next.getImsRegi().getImsProfile() != null && next.getImsRegi().getImsProfile().getCmcType() == 1) {
                return next.getImsRegi().getPhoneId();
            }
        }
        return -1;
    }

    /* access modifiers changed from: package-private */
    public void startCmcHandoverTimer(ImsRegistration imsRegistration) {
        if (this.mCmcHandoverTimer != null) {
            Log.i(LOG_TAG, "already start cmc handover timer");
            return;
        }
        Log.i(LOG_TAG, "start cmc handover timer");
        PreciseAlarmManager instance = PreciseAlarmManager.getInstance(this.mContext);
        this.mCmcHandoverTimer = obtainMessage(34, imsRegistration);
        instance.sendMessageDelayed(getClass().getSimpleName(), this.mCmcHandoverTimer, 6000);
    }

    public void stopCmcHandoverTimer(ImsRegistration imsRegistration) {
        Message message = this.mCmcHandoverTimer;
        if (message != null) {
            if (imsRegistration == null) {
                imsRegistration = (ImsRegistration) message.obj;
            }
            String str = LOG_TAG;
            Log.i(str, "stop cmc handover timer handle : " + imsRegistration.getHandle());
            PreciseAlarmManager.getInstance(this.mContext).removeMessage(this.mCmcHandoverTimer);
            this.mCmcHandoverTimer = null;
            clearAllCallsForCmcHandover(imsRegistration.getImsProfile().getCmcType());
        }
    }

    private void onCmcHandoverTimerExpired(ImsRegistration imsRegistration) {
        String str = LOG_TAG;
        Log.i(str, "onCmcHandoverTimerExpired handle : " + imsRegistration.getHandle());
        this.mCmcHandoverTimer = null;
        clearAllCallsForCmcHandover(imsRegistration.getImsProfile().getCmcType());
    }

    private CallProfile makeReplaceProfile(CallProfile callProfile) {
        CallProfile build = new DefaultCallProfileBuilder().builder().setReplaceSipCallId(callProfile.getSipCallId()).setCallType(callProfile.getCallType()).setPhoneId(callProfile.getPhoneId()).setAlertInfo(callProfile.getAlertInfo()).setEmergencyRat(callProfile.getEmergencyRat()).setUrn(callProfile.getUrn()).setCLI(callProfile.getCLI()).setConferenceCall(callProfile.getConferenceType()).setMediaProfile(callProfile.getMediaProfile()).setLineMsisdn(callProfile.getLineMsisdn()).setOriginatingUri(callProfile.getOriginatingUri()).setCmcBoundSessionId(callProfile.getCmcBoundSessionId()).setCmcType(callProfile.getCmcType()).setForceCSFB(callProfile.isForceCSFB()).setDialingNumber(callProfile.getDialingNumber()).setNetworkType(callProfile.getNetworkType()).setSamsungMdmnCall(callProfile.isSamsungMdmnCall()).build();
        if (callProfile.getDirection() == 0) {
            build.setLetteringText(callProfile.getLetteringText());
        } else {
            build.setLetteringText(callProfile.getDialingNumber());
        }
        return build;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0094, code lost:
        if (r3.getCallProfile().isPullCall() != false) goto L_0x0096;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public com.sec.ims.cmc.CmcCallInfo getCmcCallInfo() {
        /*
            r9 = this;
            java.lang.String r0 = LOG_TAG
            java.lang.String r1 = "getCmcCallInfo"
            android.util.Log.i(r0, r1)
            com.sec.internal.interfaces.ims.core.ICmcAccountManager r0 = com.sec.internal.ims.registry.ImsRegistry.getCmcAccountManager()
            int r0 = r0.getCurrentLineSlotIndex()
            com.sec.internal.interfaces.ims.core.ICmcAccountManager r1 = com.sec.internal.ims.registry.ImsRegistry.getCmcAccountManager()
            java.lang.String r1 = r1.getCurrentLineOwnerDeviceId()
            com.sec.internal.ims.servicemodules.volte2.ImsCallSessionManager r2 = r9.mImsCallSessionManager
            java.util.Map r2 = r2.getUnmodifiableSessionMap()
            java.util.Collection r2 = r2.values()
            java.util.Iterator r2 = r2.iterator()
        L_0x0025:
            boolean r3 = r2.hasNext()
            r4 = 0
            if (r3 == 0) goto L_0x0098
            java.lang.Object r3 = r2.next()
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r3 = (com.sec.internal.ims.servicemodules.volte2.ImsCallSession) r3
            int r5 = r3.getCmcType()
            if (r5 <= 0) goto L_0x0025
            int r2 = r3.getCmcType()
            boolean r2 = com.sec.internal.helper.ImsCallUtil.isCmcPrimaryType(r2)
            r5 = 1
            r6 = 2
            if (r2 == 0) goto L_0x0046
            r2 = r5
            goto L_0x0047
        L_0x0046:
            r2 = r6
        L_0x0047:
            com.sec.internal.constants.ims.servicemodules.volte2.CallConstants$STATE r7 = r3.getCallState()
            com.sec.internal.constants.ims.servicemodules.volte2.CallConstants$STATE r8 = com.sec.internal.constants.ims.servicemodules.volte2.CallConstants.STATE.IncomingCall
            if (r7 != r8) goto L_0x0051
            r4 = r5
            goto L_0x0099
        L_0x0051:
            com.sec.internal.constants.ims.servicemodules.volte2.CallConstants$STATE r5 = r3.getCallState()
            com.sec.internal.constants.ims.servicemodules.volte2.CallConstants$STATE r7 = com.sec.internal.constants.ims.servicemodules.volte2.CallConstants.STATE.OutGoingCall
            if (r5 == r7) goto L_0x0096
            com.sec.internal.constants.ims.servicemodules.volte2.CallConstants$STATE r5 = r3.getCallState()
            com.sec.internal.constants.ims.servicemodules.volte2.CallConstants$STATE r7 = com.sec.internal.constants.ims.servicemodules.volte2.CallConstants.STATE.AlertingCall
            if (r5 != r7) goto L_0x0062
            goto L_0x0096
        L_0x0062:
            com.sec.internal.constants.ims.servicemodules.volte2.CallConstants$STATE r5 = r3.getCallState()
            com.sec.internal.constants.ims.servicemodules.volte2.CallConstants$STATE r7 = com.sec.internal.constants.ims.servicemodules.volte2.CallConstants.STATE.Idle
            if (r5 == r7) goto L_0x0084
            com.sec.internal.constants.ims.servicemodules.volte2.CallConstants$STATE r5 = r3.getCallState()
            com.sec.internal.constants.ims.servicemodules.volte2.CallConstants$STATE r7 = com.sec.internal.constants.ims.servicemodules.volte2.CallConstants.STATE.ReadyToCall
            if (r5 == r7) goto L_0x0084
            com.sec.internal.constants.ims.servicemodules.volte2.CallConstants$STATE r5 = r3.getCallState()
            com.sec.internal.constants.ims.servicemodules.volte2.CallConstants$STATE r7 = com.sec.internal.constants.ims.servicemodules.volte2.CallConstants.STATE.EndingCall
            if (r5 == r7) goto L_0x0084
            com.sec.internal.constants.ims.servicemodules.volte2.CallConstants$STATE r5 = r3.getCallState()
            com.sec.internal.constants.ims.servicemodules.volte2.CallConstants$STATE r7 = com.sec.internal.constants.ims.servicemodules.volte2.CallConstants.STATE.EndedCall
            if (r5 == r7) goto L_0x0084
            r4 = 3
            goto L_0x0099
        L_0x0084:
            com.sec.internal.constants.ims.servicemodules.volte2.CallConstants$STATE r5 = r3.getCallState()
            com.sec.internal.constants.ims.servicemodules.volte2.CallConstants$STATE r7 = com.sec.internal.constants.ims.servicemodules.volte2.CallConstants.STATE.ReadyToCall
            if (r5 != r7) goto L_0x0099
            com.sec.ims.volte2.data.CallProfile r3 = r3.getCallProfile()
            boolean r3 = r3.isPullCall()
            if (r3 == 0) goto L_0x0099
        L_0x0096:
            r4 = r6
            goto L_0x0099
        L_0x0098:
            r2 = r4
        L_0x0099:
            if (r4 != 0) goto L_0x00a2
            int r9 = r9.mLastCmcEndCallReason
            r3 = 6007(0x1777, float:8.418E-42)
            if (r9 != r3) goto L_0x00a2
            r4 = 4
        L_0x00a2:
            com.sec.ims.cmc.CmcCallInfo$Builder r9 = new com.sec.ims.cmc.CmcCallInfo$Builder
            r9.<init>()
            com.sec.ims.cmc.CmcCallInfo$Builder r9 = r9.setLineSlotId(r0)
            com.sec.ims.cmc.CmcCallInfo$Builder r9 = r9.setCmcType(r2)
            com.sec.ims.cmc.CmcCallInfo$Builder r9 = r9.setCallState(r4)
            com.sec.ims.cmc.CmcCallInfo$Builder r9 = r9.setPdDeviceId(r1)
            com.sec.ims.cmc.CmcCallInfo r9 = r9.build()
            return r9
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.volte2.CmcServiceHelper.getCmcCallInfo():com.sec.ims.cmc.CmcCallInfo");
    }

    public void handleMessage(Message message) {
        switch (message.what) {
            case 31:
                OptionsEvent optionsEvent = (OptionsEvent) ((AsyncResult) message.obj).result;
                String str = LOG_TAG;
                Log.i(str, "Received EVENT_P2P_OPTIONS_EVENT: " + optionsEvent.getPhoneId());
                SecImsNotifier.getInstance().onP2pPushCallEvent(this.mLastCmcDialogEvent[optionsEvent.getPhoneId()]);
                return;
            case 32:
                OptionsEvent optionsEvent2 = (OptionsEvent) ((AsyncResult) message.obj).result;
                String str2 = LOG_TAG;
                Log.i(str2, "Received EVENT_OPTIONS_EVENT, isSuccess: " + optionsEvent2.isSuccess());
                ImsRegistration cmcRegistration = getCmcRegistration(optionsEvent2.getSessionId());
                int cmcType = cmcRegistration != null ? cmcRegistration.getImsProfile().getCmcType() : 0;
                Log.i(str2, "optionEvent regi handle: " + optionsEvent2.getSessionId() + ", cmcType: " + cmcType);
                if (ImsCallUtil.isCmcPrimaryType(cmcType)) {
                    sendDummyPublishDialog(optionsEvent2.getPhoneId(), cmcType);
                    return;
                } else if (!ImsCallUtil.isCmcSecondaryType(cmcType)) {
                    return;
                } else {
                    if (!this.mCmcPdCheckTimeOut.containsKey(Integer.valueOf(optionsEvent2.getPhoneId()))) {
                        Log.e(str2, "CmcPdCheckTimer is not running");
                        return;
                    } else if (optionsEvent2.isSuccess()) {
                        this.mIsCmcPdCheckRespReceived.put(Integer.valueOf(optionsEvent2.getPhoneId()), Boolean.TRUE);
                        return;
                    } else {
                        Log.e(str2, "ERROR Resopnse, remove pulling UI, optionFailReason: " + optionsEvent2.getReason());
                        stopCmcPdCheckTimer(optionsEvent2.getPhoneId());
                        DialogEvent dialogEvent = this.mLastCmcDialogEvent[optionsEvent2.getPhoneId()];
                        if (dialogEvent != null) {
                            dialogEvent.clearDialogList();
                            SecImsNotifier.getInstance().onDialogEvent(dialogEvent);
                        }
                        this.mLastCmcEndCallReason = 200;
                        return;
                    }
                }
            case 33:
                checkPdAvailability(message.arg1, (Bundle) message.obj);
                return;
            case 34:
                onCmcHandoverTimerExpired((ImsRegistration) message.obj);
                return;
            case 35:
                Log.i(LOG_TAG, "Received EVT_CMC_INFO_EVENT");
                onCmcRecordingInfo((CmcInfoEvent) ((AsyncResult) message.obj).result);
                return;
            default:
                return;
        }
    }
}
