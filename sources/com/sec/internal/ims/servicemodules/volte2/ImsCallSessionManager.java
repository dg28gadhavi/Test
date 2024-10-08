package com.sec.internal.ims.servicemodules.volte2;

import android.content.Context;
import android.os.Looper;
import android.os.RemoteException;
import android.telephony.CellInfo;
import android.telephony.PreciseDataConnectionState;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.telephony.Call;
import com.sec.ims.ImsRegistration;
import com.sec.ims.extensions.TelephonyManagerExt;
import com.sec.ims.util.SipError;
import com.sec.ims.volte2.data.CallProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.SipErrorBase;
import com.sec.internal.constants.ims.os.VoPsIndication;
import com.sec.internal.constants.ims.servicemodules.volte2.CallConstants;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.ImsCallUtil;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.im.util.FileTaskUtil;
import com.sec.internal.ims.servicemodules.volte2.data.IncomingCallEvent;
import com.sec.internal.ims.servicemodules.volte2.data.SIPDataEvent;
import com.sec.internal.ims.settings.GlobalSettingsManager;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.core.IPdnController;
import com.sec.internal.interfaces.ims.core.IRegistrationGovernor;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.core.NetworkStateListener;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.json.JSONException;

public class ImsCallSessionManager {
    private static final int INVALID_PHONE_ID = -1;
    /* access modifiers changed from: private */
    public static final String LOG_TAG = "ImsCallSessionManager";
    private ImsCallSessionBuilder mImsCallSessionBuilder;
    private ImsCallSession mIncomingCallSession = null;
    private ImsCallSession mIncomingCallSession2 = null;
    private final NetworkStateListener mNetworkStateListener;
    private IPdnController mPdnController;
    private ImsCallSession mPendingOutgoingCall = null;
    private final IRegistrationManager mRegMan;
    private ImsCallSessionFactory mSessionFactory;
    /* access modifiers changed from: private */
    public final Map<Integer, ImsCallSession> mSessionMap;
    private ITelephonyManager mTelephonyManager;
    private final Map<Integer, ImsCallSession> mUnmodifiableSessionMap;
    private IVolteServiceModuleInternal mVolteServiceModule;

    public ImsCallSessionManager(IVolteServiceModuleInternal iVolteServiceModuleInternal, ITelephonyManager iTelephonyManager, IPdnController iPdnController, IRegistrationManager iRegistrationManager, Looper looper) {
        ConcurrentHashMap concurrentHashMap = new ConcurrentHashMap();
        this.mSessionMap = concurrentHashMap;
        this.mUnmodifiableSessionMap = Collections.unmodifiableMap(concurrentHashMap);
        AnonymousClass1 r1 = new NetworkStateListener() {
            public void onDefaultNetworkStateChanged(int i) {
            }

            public void onEpdgDeregisterRequested(int i) {
            }

            public void onEpdgHandoverEnableChanged(int i, boolean z) {
            }

            public void onEpdgIpsecDisconnected(int i) {
            }

            public void onEpdgRegisterRequested(int i, boolean z) {
            }

            public void onIKEAuthFAilure(int i) {
            }

            public void onMobileRadioConnected(int i) {
            }

            public void onMobileRadioDisconnected(int i) {
            }

            public void onPreciseDataConnectionStateChanged(int i, PreciseDataConnectionState preciseDataConnectionState) {
            }

            public void onDataConnectionStateChanged(int i, boolean z, int i2) {
                String r2 = ImsCallSessionManager.LOG_TAG;
                Log.i(r2, "onDataConnectionStateChanged(): networkType [" + TelephonyManagerExt.getNetworkTypeName(i) + "]isWifiConnected [" + z + "], phoneId [" + i2 + "]");
            }

            public void onCellInfoChanged(List<CellInfo> list, int i) {
                String r1 = ImsCallSessionManager.LOG_TAG;
                Log.i(r1, "onCellInfoChanged, phoneId: " + i);
            }

            public void onEpdgConnected(int i) {
                String r0 = ImsCallSessionManager.LOG_TAG;
                Log.i(r0, "onEpdgConnected: [" + i + "]");
                handleEpdgState(i, true);
            }

            public void onEpdgDisconnected(int i) {
                String r0 = ImsCallSessionManager.LOG_TAG;
                Log.i(r0, "onEpdgDisconnected: [" + i + "]");
                handleEpdgState(i, false);
            }

            private void handleEpdgState(int i, boolean z) {
                int i2 = z ? LogClass.VOLTE_EPDG_CONNECTED : LogClass.VOLTE_EPDG_DISCONNECTED;
                IMSLog.c(i2, "" + i);
                for (ImsCallSession imsCallSession : ImsCallSessionManager.this.mSessionMap.values()) {
                    if (imsCallSession.getPhoneId() == i && imsCallSession.getCallProfile().getNetworkType() != 15) {
                        imsCallSession.setEpdgState(z);
                    }
                }
            }
        };
        this.mNetworkStateListener = r1;
        this.mVolteServiceModule = iVolteServiceModuleInternal;
        this.mSessionFactory = new ImsCallSessionFactory(iVolteServiceModuleInternal, looper);
        this.mTelephonyManager = iTelephonyManager;
        this.mPdnController = iPdnController;
        this.mRegMan = iRegistrationManager;
        iPdnController.registerForNetworkState(r1);
        this.mImsCallSessionBuilder = new ImsCallSessionBuilder(this, iVolteServiceModuleInternal, iTelephonyManager, iPdnController, iRegistrationManager, looper);
        concurrentHashMap.clear();
    }

    public ImsCallSession createSession(Context context, CallProfile callProfile, ImsRegistration imsRegistration) throws RemoteException {
        return this.mImsCallSessionBuilder.createSession(context, callProfile, imsRegistration);
    }

    public void addCallSession(ImsCallSession imsCallSession) {
        int sessionId = imsCallSession.getSessionId();
        for (ImsCallSession next : this.mSessionMap.values()) {
            if ((sessionId != -1 && next.getSessionId() == sessionId) || next.getCallState() == CallConstants.STATE.EndedCall) {
                if (next.equals(imsCallSession)) {
                    String str = LOG_TAG;
                    Log.e(str, "same CallSession has been found : Session id is:" + next.getSessionId() + " And corresponding CallId is:" + next.getCallId());
                    return;
                }
                this.mSessionMap.remove(Integer.valueOf(next.getCallId()));
            }
        }
        this.mSessionMap.put(Integer.valueOf(imsCallSession.getCallId()), imsCallSession);
    }

    public Map<Integer, ImsCallSession> getSessionMap() {
        return this.mSessionMap;
    }

    public int getSessionCount() {
        return this.mSessionMap.size();
    }

    public int getSessionCount(int i) {
        int i2 = 0;
        for (ImsCallSession phoneId : this.mSessionMap.values()) {
            if (phoneId.getPhoneId() == i) {
                i2++;
            }
        }
        return i2;
    }

    public List<ImsCallSession> getSessionList() {
        ArrayList arrayList = new ArrayList();
        arrayList.addAll(this.mSessionMap.values());
        return arrayList;
    }

    public List<ImsCallSession> getSessionList(int i) {
        ArrayList arrayList = new ArrayList();
        for (ImsCallSession next : this.mSessionMap.values()) {
            if (next.getPhoneId() == i) {
                arrayList.add(next);
            }
        }
        return arrayList;
    }

    public ImsCallSession getForegroundSession() {
        return getForegroundSession(-1);
    }

    public ImsCallSession getForegroundSession(int i) {
        for (ImsCallSession next : this.mSessionMap.values()) {
            if ((i == -1 || next.getPhoneId() == i) && next.getCallState() == CallConstants.STATE.InCall) {
                return next;
            }
        }
        return null;
    }

    public boolean hasActiveCall(int i) {
        CallConstants.STATE callState;
        for (ImsCallSession next : this.mSessionMap.values()) {
            if (next.getPhoneId() == i && (callState = next.getCallState()) != CallConstants.STATE.Idle && callState != CallConstants.STATE.EndingCall && callState != CallConstants.STATE.EndedCall) {
                return true;
            }
        }
        return false;
    }

    public boolean hasEstablishedCall(int i) {
        for (ImsCallSession next : this.mSessionMap.values()) {
            if (next.getPhoneId() == i && next.getCmcType() == 0) {
                CallConstants.STATE callState = next.getCallState();
                if (callState == CallConstants.STATE.InCall || callState == CallConstants.STATE.HeldCall || callState == CallConstants.STATE.HoldingCall) {
                    return true;
                }
            }
        }
        return false;
    }

    public ImsCallSession getSession(int i) {
        for (ImsCallSession next : this.mSessionMap.values()) {
            if (next.getSessionId() == i) {
                return next;
            }
        }
        return null;
    }

    public ImsCallSession getSessionByCallId(int i) {
        return this.mSessionMap.get(Integer.valueOf(i));
    }

    public ImsCallSession getSessionBySipCallId(String str) {
        if (str == null) {
            return null;
        }
        for (ImsCallSession next : this.mSessionMap.values()) {
            if (str.equals(next.getCallProfile().getSipCallId())) {
                return next;
            }
        }
        return null;
    }

    public List<ImsCallSession> getSessionByState(CallConstants.STATE state) {
        return getSessionByState(-1, state);
    }

    public List<ImsCallSession> getSessionByState(int i, CallConstants.STATE state) {
        ArrayList arrayList = new ArrayList();
        for (ImsCallSession next : this.mSessionMap.values()) {
            CallProfile callProfile = next.getCallProfile();
            if ((callProfile == null || !callProfile.isConferenceCall()) && (i == -1 || next.getPhoneId() == i)) {
                String str = LOG_TAG;
                Log.i(str, "getSessionByState(" + next.getCallId() + ") : " + next.getCallState().toString());
                if (next.getCallState() == state) {
                    arrayList.add(next);
                }
            }
        }
        return arrayList;
    }

    public List<ImsCallSession> getSessionByCallType(int i) {
        return getSessionByCallType(-1, i);
    }

    public List<ImsCallSession> getSessionByCallType(int i, int i2) {
        CallProfile callProfile;
        ArrayList arrayList = new ArrayList();
        for (ImsCallSession next : this.mSessionMap.values()) {
            if ((i == -1 || next.getPhoneId() == i) && (((callProfile = next.getCallProfile()) == null || !callProfile.isConferenceCall()) && callProfile != null && callProfile.getCallType() == i2)) {
                arrayList.add(next);
            }
        }
        return arrayList;
    }

    public ImsCallSession getSessionByTelecomCallId(String str) {
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        for (ImsCallSession next : this.mSessionMap.values()) {
            if (next.getIdcData() != null && str.equals(next.getIdcData().getTelecomCallId())) {
                return next;
            }
        }
        return null;
    }

    public String getTelecomCallIdBySessionId(int i) {
        ImsCallSession session = getSession(i);
        if (session == null || session.getIdcData() == null) {
            return null;
        }
        return session.getIdcData().getTelecomCallId();
    }

    public void setPendingOutgoingCall(ImsCallSession imsCallSession) {
        this.mPendingOutgoingCall = imsCallSession;
    }

    public boolean hasSipCallId(String str) {
        for (Map.Entry<Integer, ImsCallSession> value : this.mSessionMap.entrySet()) {
            ImsCallSession imsCallSession = (ImsCallSession) value.getValue();
            CallProfile callProfile = imsCallSession.getCallProfile();
            if (callProfile != null && callProfile.getSipCallId() != null && callProfile.getSipCallId().equals(str)) {
                String str2 = LOG_TAG;
                Log.i(str2, "exclude the dialog with sipCallId: " + str + " sessionId: " + imsCallSession.getSessionId());
                return true;
            }
        }
        return false;
    }

    public ImsCallSession getSessionByRegId(int i) {
        for (ImsCallSession next : this.mSessionMap.values()) {
            if (next.getRegistration().getHandle() == i) {
                return next;
            }
        }
        return null;
    }

    public List<ImsCallSession> getEmergencySession() {
        ArrayList arrayList = new ArrayList();
        for (ImsCallSession next : this.mSessionMap.values()) {
            CallProfile callProfile = next.getCallProfile();
            if (callProfile != null && ImsCallUtil.isE911Call(callProfile.getCallType())) {
                arrayList.add(next);
            }
        }
        return arrayList;
    }

    public List<ImsCallSession> getEmergencySession(int i) {
        ArrayList arrayList = new ArrayList();
        for (ImsCallSession next : this.mSessionMap.values()) {
            CallProfile callProfile = next.getCallProfile();
            if (callProfile != null && ImsCallUtil.isE911Call(callProfile.getCallType()) && next.getPhoneId() == i) {
                arrayList.add(next);
            }
        }
        return arrayList;
    }

    public ImsCallSession removeSession(int i) {
        for (Map.Entry next : this.mSessionMap.entrySet()) {
            Integer num = (Integer) next.getKey();
            if (((ImsCallSession) next.getValue()).getSessionId() == i) {
                return this.mSessionMap.remove(num);
            }
        }
        return null;
    }

    public void removeSessionByCmcType(int i) {
        String str = LOG_TAG;
        Log.i(str, "removeSessionByCmcType cmcType " + i);
        for (ImsCallSession next : this.mSessionMap.values()) {
            try {
                if (i == next.getCmcType()) {
                    removeSession(next.getSessionId());
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                String str2 = LOG_TAG;
                Log.e(str2, "removeSessionByCmcType: " + e.getMessage());
            }
        }
    }

    public int getActiveCallSessionId() {
        for (ImsCallSession next : this.mSessionMap.values()) {
            try {
                if (next.getCallState() != CallConstants.STATE.Idle && !ImsCallUtil.isEndCallState(next.getCallState())) {
                    return next.getSessionId();
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                String str = LOG_TAG;
                Log.e(str, "getActiveCallSession : " + e.getMessage());
            }
        }
        return -1;
    }

    private List<ImsCallSession> getActiveCallSession(int i) {
        ArrayList arrayList = new ArrayList();
        for (ImsCallSession next : this.mSessionMap.values()) {
            if (next != null && (i == -1 || next.getPhoneId() == i)) {
                try {
                    if (next.getCallState() != CallConstants.STATE.Idle) {
                        if (!ImsCallUtil.isEndCallState(next.getCallState())) {
                            if (next.getCallProfile() != null) {
                                arrayList.add(next);
                            }
                        }
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    String str = LOG_TAG;
                    Log.e(str, "getActiveCallSession : " + e.getMessage());
                }
            }
        }
        return arrayList;
    }

    public ImsCallSession getAlertingCallSession(int i) {
        for (ImsCallSession next : this.mSessionMap.values()) {
            if (next != null && ((i == -1 || next.getPhoneId() == i) && next.getCallState() == CallConstants.STATE.AlertingCall)) {
                return next;
            }
        }
        return null;
    }

    public int getIncomingSessionPhoneIdForCmc() {
        for (ImsCallSession next : this.mSessionMap.values()) {
            if (next.getCallState() == CallConstants.STATE.IncomingCall) {
                return next.getPhoneId();
            }
        }
        return -1;
    }

    public int getTotalCallCount(int i) {
        return getActiveCallSession(i).size();
    }

    public int getVideoCallCount(int i) {
        return (int) getActiveCallSession(i).stream().filter(new ImsCallSessionManager$$ExternalSyntheticLambda0()).count();
    }

    public int getDowngradedCallCount(int i) {
        return (int) getActiveCallSession(i).stream().filter(new ImsCallSessionManager$$ExternalSyntheticLambda3()).filter(new ImsCallSessionManager$$ExternalSyntheticLambda4()).count();
    }

    /* access modifiers changed from: private */
    public static /* synthetic */ boolean lambda$getDowngradedCallCount$1(ImsCallSession imsCallSession) {
        return !ImsCallUtil.isVideoCall(imsCallSession.getCallProfile().getCallType());
    }

    public int getE911CallCount(int i) {
        return (int) getActiveCallSession(i).stream().filter(new ImsCallSessionManager$$ExternalSyntheticLambda5()).count();
    }

    public int getEpsFbCallCount(int i) {
        return (int) getActiveCallSession(i).stream().filter(new ImsCallSessionManager$$ExternalSyntheticLambda6()).count();
    }

    public int getNrSaCallCount(int i) {
        return (int) getActiveCallSession(i).stream().filter(new ImsCallSessionManager$$ExternalSyntheticLambda2()).count();
    }

    public int getEpdgCallCount(int i) {
        return (int) getActiveCallSession(i).stream().filter(new ImsCallSessionManager$$ExternalSyntheticLambda1()).count();
    }

    public int getActiveExtCallCount() {
        int i = 0;
        for (ImsCallSession next : this.mSessionMap.values()) {
            if (next.getCmcType() == 0 && (next.getCallState() == CallConstants.STATE.InCall || next.mIsEstablished)) {
                i++;
            }
        }
        return i;
    }

    public boolean getExtMoCall() {
        for (ImsCallSession next : this.mSessionMap.values()) {
            if (next.getCmcType() == 0) {
                return next.getCallProfile().isMOCall();
            }
        }
        return false;
    }

    public boolean hasVideoCall() {
        for (ImsCallSession callProfile : this.mSessionMap.values()) {
            if (ImsCallUtil.isVideoCall(callProfile.getCallProfile().getCallType())) {
                return true;
            }
        }
        return false;
    }

    public boolean hasRttCall() {
        for (ImsCallSession callProfile : this.mSessionMap.values()) {
            if (ImsCallUtil.isRttCall(callProfile.getCallProfile().getCallType())) {
                return true;
            }
        }
        return false;
    }

    public int convertToSessionId(int i) {
        ImsCallSession imsCallSession = this.mSessionMap.get(Integer.valueOf(i));
        if (imsCallSession == null) {
            return -1;
        }
        return imsCallSession.getSessionId();
    }

    public boolean hasRingingCall() {
        return hasRingingCall(-1);
    }

    public boolean hasRingingCall(int i) {
        for (ImsCallSession next : this.mSessionMap.values()) {
            if ((i == -1 || next.getPhoneId() == i) && (next.getCallState() == CallConstants.STATE.IncomingCall || next.getPreAlerting())) {
                String str = LOG_TAG;
                Log.i(str, "session(" + next.getSessionId() + ") is in IncomingCall");
                return true;
            }
        }
        return false;
    }

    public void forceNotifyCurrentCodec() {
        for (ImsCallSession forceNotifyCurrentCodec : this.mSessionMap.values()) {
            forceNotifyCurrentCodec.forceNotifyCurrentCodec();
        }
    }

    public void sendRttMessage(String str) {
        if (str != null) {
            for (ImsCallSession sendText : this.mSessionMap.values()) {
                sendText.sendText(str, str.length());
            }
            return;
        }
        Log.i(LOG_TAG, "sendRttMessage: receive null string / do nothing");
    }

    public boolean triggerPsRedial(int i, int i2, int i3, ImsRegistration imsRegistration) {
        ImsCallSession imsCallSession = this.mSessionMap.get(Integer.valueOf(i2));
        if (imsRegistration == null || imsCallSession == null) {
            String str = LOG_TAG;
            Log.e(str, "TMO_E911 Call session or IMS Registration is not exist!");
            Log.e(str, "TMO_E911 triggerPsRedial = false");
            return false;
        }
        CallProfile callProfile = imsCallSession.getCallProfile();
        if (callProfile == null) {
            Log.e(LOG_TAG, "TMO_E911 triggerPsRedial = false, origProfile is null");
            return false;
        }
        callProfile.setNetworkType(i3);
        ImsCallSession create = this.mSessionFactory.create(callProfile, imsRegistration, false);
        if (create == null) {
            return false;
        }
        try {
            create.replaceSessionEventListener(imsCallSession);
            create.start(callProfile.getDialingNumber(), (CallProfile) null);
            this.mSessionMap.replace(Integer.valueOf(i2), imsCallSession, create);
            create.setCallId(i2);
            imsCallSession.notifySessionChanged(i2);
            Log.e(LOG_TAG, "TMO_E911 triggerPsRedial = true");
            return true;
        } catch (RemoteException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "TMO_E911 triggerPsRedial = false");
            return false;
        }
    }

    public void handleSrvccStateChange(int i, Call.SrvccState srvccState, Mno mno) {
        IRegistrationGovernor registrationGovernor;
        ImsRegistration imsRegistration = null;
        for (ImsCallSession next : this.mSessionMap.values()) {
            if (next != null) {
                try {
                    if (next.getPhoneId() == i) {
                        if (srvccState == Call.SrvccState.STARTED) {
                            next.setSrvccStarted(true);
                            next.update((CallProfile) null, 100, "SRVCC HO STARTED");
                        } else if (srvccState == Call.SrvccState.COMPLETED) {
                            next.setSrvccStarted(false);
                            next.terminate(8);
                            if (imsRegistration == null) {
                                imsRegistration = next.getRegistration();
                            }
                        } else if (srvccState == Call.SrvccState.FAILED || srvccState == Call.SrvccState.CANCELED) {
                            String str = "failure to transition to CS domain";
                            if ((mno.isOrangeGPG() || mno == Mno.ORANGE_MOLDOVA) && !next.getSrvccStarted()) {
                                str = "handover cancelled";
                            }
                            next.setSrvccStarted(false);
                            next.update((CallProfile) null, 487, str);
                        }
                    }
                } catch (RemoteException | ArrayIndexOutOfBoundsException e) {
                    Log.e(LOG_TAG, "handleReinvite: " + e.getMessage());
                }
            }
        }
        if (srvccState == Call.SrvccState.COMPLETED && imsRegistration != null && (registrationGovernor = this.mRegMan.getRegistrationGovernor(imsRegistration.getHandle())) != null) {
            registrationGovernor.onSrvccComplete();
        }
    }

    public void handleEpdgHandover(int i, ImsRegistration imsRegistration, Mno mno) {
        for (ImsCallSession next : this.mSessionMap.values()) {
            if (next.getPhoneId() == imsRegistration.getPhoneId() && !next.isE911Call()) {
                boolean isEpdgConnected = this.mPdnController.isEpdgConnected(i);
                if (isEpdgConnected && imsRegistration.getEpdgStatus()) {
                    next.setEpdgState(true);
                } else if (!isEpdgConnected) {
                    next.setEpdgState(false);
                }
                if (mno == Mno.ATT || mno == Mno.ROGERS) {
                    try {
                        next.reinvite();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void notifyConfError(ImsRegistration imsRegistration) {
        for (ImsCallSession next : this.mSessionMap.values()) {
            if (next.getPhoneId() == imsRegistration.getPhoneId() && next.getCallProfile().getConferenceType() == 1) {
                ((ImsConfSession) next).notifyOnErrorBeforeEndParticipant();
                return;
            }
        }
    }

    public void endCallByDeregistered(ImsRegistration imsRegistration) {
        notifyConfError(imsRegistration);
        int handle = imsRegistration.getHandle();
        for (ImsCallSession next : this.mSessionMap.values()) {
            try {
                ImsRegistration registration = next.getRegistration();
                if (registration != null && handle == registration.getHandle()) {
                    if (next.getCallState() == CallConstants.STATE.IncomingCall) {
                        next.reject(2);
                    } else {
                        String str = LOG_TAG;
                        Log.i(str, "end call " + next.getSessionId() + " by MMTEL deregistered");
                        next.terminate(ImsCallUtil.convertDeregiReason(imsRegistration.getDeregiReason()), true);
                    }
                }
            } catch (RemoteException | ArrayIndexOutOfBoundsException e) {
                String str2 = LOG_TAG;
                Log.e(str2, "endCallByDeregistered: " + e.getMessage());
            }
        }
    }

    public void endcallByNwHandover(ImsRegistration imsRegistration) {
        notifyConfError(imsRegistration);
        for (ImsCallSession next : this.mSessionMap.values()) {
            if (next.getPhoneId() == imsRegistration.getPhoneId()) {
                try {
                    if (next.getCallState() == CallConstants.STATE.IncomingCall) {
                        next.reject(4);
                    } else {
                        next.terminate(4);
                    }
                } catch (RemoteException e) {
                    Log.e(LOG_TAG, "endcallByNwHandover: ", e);
                }
            }
        }
    }

    public void onCallEndByCS(int i) {
        Log.i(LOG_TAG, "onCallEndByCS");
        for (ImsCallSession next : this.mSessionMap.values()) {
            if (next.getCallProfile().getCallType() != 7 && next.getPhoneId() == i) {
                try {
                    next.terminate(4);
                } catch (RemoteException e) {
                    Log.e(LOG_TAG, "onCallEndByCS: ", e);
                }
            }
        }
    }

    public void onUssdEndByCS(int i) {
        Log.i(LOG_TAG, "onUssdEndByCS");
        for (ImsCallSession next : this.mSessionMap.values()) {
            if (next.getCallProfile().getCallType() == 12 && next.getPhoneId() == i) {
                try {
                    next.terminate(4);
                } catch (RemoteException e) {
                    Log.e(LOG_TAG, "onUssdEndByCS: ", e);
                }
            }
        }
    }

    public void releaseAllSession(int i) {
        for (ImsCallSession next : this.mSessionMap.values()) {
            if (next != null && next.getPhoneId() == i) {
                try {
                    if (next.getCallState() == CallConstants.STATE.IncomingCall) {
                        next.reject(2);
                    } else {
                        next.terminate(5, true);
                    }
                } catch (RemoteException | ArrayIndexOutOfBoundsException e) {
                    String str = LOG_TAG;
                    Log.e(str, "release all session in F/W layer: " + e.getMessage());
                }
            }
        }
    }

    public void releaseAllVideoCall() {
        for (ImsCallSession next : getSessionList()) {
            if (next.getCallProfile().getCallType() == 2) {
                try {
                    next.terminate(-1);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void onReleaseWfcBeforeHO(int i) {
        CallProfile callProfile;
        for (ImsCallSession next : this.mSessionMap.values()) {
            if (next != null) {
                try {
                    if (next.getPhoneId() == i && (callProfile = next.getCallProfile()) != null && !ImsCallUtil.isE911Call(callProfile.getCallType())) {
                        if (next.getCallState() == CallConstants.STATE.IncomingCall) {
                            next.reject(2);
                        } else {
                            next.terminate(5, true);
                        }
                        String str = LOG_TAG;
                        Log.i(str, "end call " + next.getSessionId() + " Before HO");
                    }
                } catch (RemoteException | ArrayIndexOutOfBoundsException e) {
                    String str2 = LOG_TAG;
                    Log.e(str2, "onReleaseWfcBeforeHO: " + e.getMessage());
                }
            }
        }
    }

    public void terminateMoWfcWhenWfcSettingOff(int i) {
        CallProfile callProfile;
        for (ImsCallSession next : this.mSessionMap.values()) {
            if (next.getPhoneId() == i && (callProfile = next.getCallProfile()) != null && callProfile.getCallType() == 1 && !callProfile.isDowngradedVideoCall() && callProfile.isMOCall() && !callProfile.isConferenceCall()) {
                try {
                    next.terminate(5);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean hasOutgoingCall(int i) {
        for (ImsCallSession next : this.mSessionMap.values()) {
            if (next.getPhoneId() == i && !next.getCallProfile().isMTCall()) {
                CallConstants.STATE callState = next.getCallState();
                if (callState == CallConstants.STATE.ReadyToCall || callState == CallConstants.STATE.OutGoingCall || callState == CallConstants.STATE.AlertingCall) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasEmergencyCall(int i) {
        CallProfile callProfile;
        for (ImsCallSession next : this.mSessionMap.values()) {
            if (next.getPhoneId() == i && (callProfile = next.getCallProfile()) != null && ImsCallUtil.isE911Call(callProfile.getCallType())) {
                return true;
            }
        }
        return false;
    }

    public void setTtyMode(int i, int i2) {
        for (ImsCallSession next : this.mSessionMap.values()) {
            if (next.getPhoneId() == i) {
                next.setTtyMode(i2);
            }
        }
    }

    public void notifyDSDAVideoCapa(boolean z) {
        for (ImsCallSession notifyDSDAVideoCapa : this.mSessionMap.values()) {
            notifyDSDAVideoCapa.notifyDSDAVideoCapa(z);
        }
    }

    public void onPSBarred(boolean z) {
        String str = LOG_TAG;
        Log.i(str, "onPSBarred: on =" + z);
        if (z) {
            IMSLog.c(LogClass.VOLTE_PS_BARRING);
            for (ImsCallSession next : this.mSessionMap.values()) {
                CallConstants.STATE callState = next.getCallState();
                if (callState == CallConstants.STATE.IncomingCall || callState == CallConstants.STATE.OutGoingCall || callState == CallConstants.STATE.AlertingCall) {
                    try {
                        next.terminate(13);
                    } catch (RemoteException e) {
                        Log.e(LOG_TAG, "onNetworkChanged: ", e);
                    }
                }
            }
        }
    }

    public void getSIPMSGInfo(SIPDataEvent sIPDataEvent) {
        for (ImsCallSession onReceiveSIPMSG : this.mSessionMap.values()) {
            onReceiveSIPMSG.onReceiveSIPMSG(sIPDataEvent.getSipMessage(), sIPDataEvent.getIsRequest());
        }
    }

    public void onUpdateGeolocation() {
        for (ImsCallSession onUpdateGeolocation : this.mSessionMap.values()) {
            onUpdateGeolocation.onUpdateGeolocation();
        }
    }

    public ImsCallSession onImsIncomingCallEvent(IncomingCallEvent incomingCallEvent, CallProfile callProfile, ImsRegistration imsRegistration, int i, int i2) {
        ImsCallSession create = this.mSessionFactory.create(callProfile, imsRegistration, false);
        if (create == null) {
            Log.i(LOG_TAG, "onImsIncomingCallEvent: IncomingCallSession create failed");
            return null;
        }
        create.setSessionId(incomingCallEvent.getSessionID());
        create.updateCallProfile(incomingCallEvent.getParams());
        if (!(imsRegistration.getImsProfile().getTtyType() == 1 || imsRegistration.getImsProfile().getTtyType() == 3)) {
            create.setTtyMode(i2);
        }
        if ((imsRegistration.getImsProfile().getTtyType() == 3 || imsRegistration.getImsProfile().getTtyType() == 4) && ImsCallUtil.isRttCall(i)) {
            if (!this.mPdnController.isEpdgConnected(imsRegistration.getPhoneId())) {
                create.startRttDedicatedBearerTimer(this.mVolteServiceModule.getRttDbrTimer(imsRegistration.getPhoneId()));
            }
            create.getCallProfile().getMediaProfile().setRttMode(1);
        }
        create.setPreAlerting();
        addCallSession(create);
        if (imsRegistration.getPhoneId() == 0) {
            this.mIncomingCallSession = create;
        } else {
            this.mIncomingCallSession2 = create;
        }
        return create;
    }

    public ImsCallSession getIncomingCallSession(int i) {
        return i == 0 ? this.mIncomingCallSession : this.mIncomingCallSession2;
    }

    public void onRegistered(ImsRegistration imsRegistration) {
        ImsCallSession imsCallSession = this.mPendingOutgoingCall;
        if (imsCallSession != null) {
            imsCallSession.handleRegistrationDone(imsRegistration);
            this.mPendingOutgoingCall = null;
        }
    }

    public void handleDeregistered(Context context, int i, int i2, Mno mno) {
        if (this.mPendingOutgoingCall == null) {
            return;
        }
        if (mno.isKor() || (mno == Mno.VZW && ImsUtil.isCdmalessEnabled(context, i) && i2 == 503)) {
            this.mPendingOutgoingCall.handleRegistrationFailed();
            this.mPendingOutgoingCall = null;
        }
    }

    public void onCallEnded(int i) {
        if (this.mPendingOutgoingCall != null && !this.mVolteServiceModule.getIsLteRetrying(i)) {
            String str = LOG_TAG;
            Log.i(str, "onCallEnded(" + i + ")");
            this.mPendingOutgoingCall = null;
        }
    }

    private boolean ignoreCsfbByEpsOnlyNw(ImsRegistration imsRegistration, int i, Mno mno) {
        if (OmcCode.isKTTOmcCode() && mno == Mno.KT && this.mPdnController.isEpsOnlyReg(i)) {
            Log.e(LOG_TAG, "EPS only registered for KT LTE Preferred model!");
            return true;
        } else if (imsRegistration == null || imsRegistration.getImsProfile() == null || mno == Mno.DOCOMO || !imsRegistration.getImsProfile().getSupportLtePreferred() || !this.mPdnController.isEpsOnlyReg(i)) {
            return false;
        } else {
            Log.e(LOG_TAG, "EPS only registered for LTE Preferred model!");
            return true;
        }
    }

    public boolean isCsfbErrorCode(Context context, int i, CallProfile callProfile, SipError sipError) {
        int callType = callProfile.getCallType();
        if (sipError == null) {
            Log.e(LOG_TAG, "SipError was null!!");
            return false;
        } else if (!this.mVolteServiceModule.isSilentRedialEnabled(context, i)) {
            Log.e(LOG_TAG, "isSilentRedialEnabled was false!");
            return false;
        } else {
            ImsRegistration imsRegistration = getImsRegistration(i);
            Mno mno = imsRegistration == null ? SimUtil.getMno(i) : Mno.fromName(imsRegistration.getImsProfile().getMnoName());
            if (callType == 12 && (getSessionCount() > 1 || mno == Mno.TMOUS)) {
                Log.e(LOG_TAG, "Already activated call exist when USSD call run!");
                return false;
            } else if (ignoreCsfbByEpsOnlyNw(imsRegistration, i, mno)) {
                Log.i(LOG_TAG, "ignore CSFB due to only EPS network!");
                return false;
            } else {
                String str = LOG_TAG;
                Log.i(str, "CallType : " + callType + " SipError : " + sipError);
                if (sipError.equals(SipErrorBase.SIP_INVITE_TIMEOUT)) {
                    Log.i(str, "Timer B expired convert to INVITE_TIMEOUT");
                    sipError.setCode(1114);
                }
                if ((mno.isOrangeGPG() || mno == Mno.ORANGE_MOLDOVA) && imsRegistration != null && 18 == imsRegistration.getRegiRat()) {
                    Log.i(str, "isCsfbErrorCode ORANGE GROUP customization in VoWIFI");
                    if (isServerSipError(sipError) && this.mVolteServiceModule.isRoaming(i) && !this.mRegMan.getNetworkEvent(imsRegistration.getPhoneId()).outOfService) {
                        this.mRegMan.blockVoWifiRegisterOnRoaminByCsfbError(imsRegistration.getHandle(), FileTaskUtil.READ_DATA_TIMEOUT);
                        return false;
                    }
                }
                if (mno.isChn() && SimUtil.isDSDACapableDevice() && !ImsCallUtil.isE911Call(callType) && getSessionCount(SimUtil.getOppositeSimSlot(i)) > 0) {
                    Log.i(str, "China, DSDA, there's PS call at other slot, do not perform CSFB");
                    return false;
                } else if ((mno == Mno.LGU || mno == Mno.KDDI) && !this.mVolteServiceModule.isRoaming(i)) {
                    Log.i(str, "LGU/KDDI - Do not use CSFB in home network");
                    return false;
                } else if (mno == Mno.MTS_RUSSIA && this.mVolteServiceModule.isRoaming(i)) {
                    Log.i(str, "MTS Russia - Do not use CSFB in roaming");
                    return false;
                } else if (sipError.getCode() == 1117) {
                    Log.i(str, "CALL_ENDED_BY_NW_HANDOVER_BEFORE_100_TRYING is always trigger CSFB");
                    return true;
                } else if ((mno.isTmobile() || mno == Mno.TELEKOM_ALBANIA) && sipError.equals(SipErrorBase.MEDIA_BEARER_OR_QOS_LOST)) {
                    Log.i(str, "CSFB condition for T-Mobile EUR");
                    return true;
                } else if (mno == Mno.VIVO_BRAZIL && this.mVolteServiceModule.isRoaming(i)) {
                    Log.i(str, "VIVO doesn't support CSFB under roaming area");
                    return false;
                } else if (mno == Mno.PLAY && this.mPdnController.getNetworkState(i).isInternationalRoaming() && (imsRegistration == null || imsRegistration.getRegiRat() == 18)) {
                    Log.i(str, "PLAY_PL doesn't support CSFB under international roaming area in VoWiFi");
                    return false;
                } else {
                    if (mno == Mno.TMOUS) {
                        if (getSessionCount() > 1) {
                            Log.i(str, "has another call " + getSessionCount());
                            return false;
                        } else if (ImsCallUtil.isClientError(sipError)) {
                            Log.i(str, "TMO - Stack return -1 trigger CSFB");
                            return true;
                        }
                    } else if (mno == Mno.VZW) {
                        boolean isCdmalessEnabled = ImsUtil.isCdmalessEnabled(context, i);
                        Log.i(str, "VZW - roaming(" + this.mVolteServiceModule.isRoaming(i) + ") CDMAless(" + isCdmalessEnabled + ") getLteEpsOnlyAttached(" + this.mVolteServiceModule.getLteEpsOnlyAttached(i) + ")");
                        if ((this.mVolteServiceModule.isRoaming(i) && this.mVolteServiceModule.getLteEpsOnlyAttached(i)) || (!this.mVolteServiceModule.isRoaming(i) && isCdmalessEnabled)) {
                            return false;
                        }
                        if (this.mVolteServiceModule.isRoaming(i) && isCdmalessEnabled && !this.mVolteServiceModule.getLteEpsOnlyAttached(i) && sipError.getCode() == 2511) {
                            return true;
                        }
                        if (ImsCallUtil.isImsOutageError(sipError) || sipError.getCode() == 2502) {
                            if (this.mVolteServiceModule.isRoaming(i) || isCdmalessEnabled) {
                                return false;
                            }
                            return true;
                        } else if (isCdmalessEnabled && sipError.getCode() == 1601) {
                            return true;
                        }
                    } else if (mno == Mno.ATT && sipError.getCode() == 403 && callType == 12 && this.mVolteServiceModule.isRegisteredOver3gppPsVoice(i) && this.mVolteServiceModule.getNetwork(i).voiceOverPs == VoPsIndication.NOT_SUPPORTED) {
                        return true;
                    }
                    boolean isCsfbErrorCodeOnList = isCsfbErrorCodeOnList(context, i, callType, mno, sipError, false);
                    if (!callProfile.isECallConvertedToNormal() || !isNormalTypeECallCsfbError(sipError) || !this.mVolteServiceModule.isRoaming(i)) {
                        return isCsfbErrorCodeOnList;
                    }
                    Log.i(str, "Normal type ECall fails in roaming, CSFB retry");
                    return true;
                }
            }
        }
    }

    public boolean isCsfbErrorCodeOnList(Context context, int i, int i2, Mno mno, SipError sipError, boolean z) {
        String str = ImsCallUtil.isVideoCall(i2) ? GlobalSettingsConstants.Call.VIDEO_CSFB_ERROR_CODE_LIST : GlobalSettingsConstants.Call.VOICE_CSFB_ERROR_CODE_LIST;
        try {
            String[] stringArray = GlobalSettingsManager.getInstance(context, i).getStringArray(GlobalSettingsConstants.Call.ALL_CSFB_ERROR_CODE_LIST, (String[]) null);
            String str2 = LOG_TAG;
            Log.i(str2, "all_csfb_error_code_list " + Arrays.asList(stringArray));
            z = isMatchWithErrorCodeList(stringArray, sipError.getCode());
            if (!z) {
                String[] stringArray2 = GlobalSettingsManager.getInstance(context, i).getStringArray(str, (String[]) null);
                Log.i(str2, str + " " + Arrays.asList(stringArray2));
                z = isMatchWithErrorCodeList(stringArray2, sipError.getCode());
            }
            if (mno == Mno.TMOUS && ((this.mVolteServiceModule.getLteEpsOnlyAttached(i) || this.mRegMan.getNetworkEvent(i).network == 20) && z && !ImsCallUtil.isE911Call(i2) && sipError.getCode() != SipErrorBase.ALTERNATIVE_SERVICE.getCode())) {
                z = false;
            }
            if (!z && ImsCallUtil.isE911Call(i2)) {
                String[] stringArray3 = GlobalSettingsManager.getInstance(context, i).getStringArray(GlobalSettingsConstants.Call.E911_CSFB_ERROR_CODE_LIST, new String[0]);
                Log.i(str2, GlobalSettingsConstants.Call.E911_CSFB_ERROR_CODE_LIST + " " + Arrays.asList(stringArray3));
                z = isMatchWithErrorCodeList(stringArray3, sipError.getCode());
                if (mno.isChn() && ((sipError.getCode() == 381 || sipError.getCode() == 382) && ImsCallUtil.convertUrnToEccCat(sipError.getReason()) == 254)) {
                    Log.i(str2, "Unrecognized service urn.");
                }
            }
            if (mno.isChn() && sipError.getCode() == 487 && sipError.getReason() != null && sipError.getReason().equals("Destination out of order")) {
                Log.i(str2, "need CSFB for call forwarding");
                z = true;
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "isCsfbErrorCode fail " + e.getMessage());
        }
        Log.i(LOG_TAG, "isCsfbErrorCode Mno " + mno.getName() + " callType " + i2 + " error " + sipError + " ==> " + z);
        return z;
    }

    public ImsRegistration getImsRegistration(int i) {
        ImsRegistration[] registrationInfoByPhoneId = this.mRegMan.getRegistrationInfoByPhoneId(i);
        if (registrationInfoByPhoneId == null) {
            return null;
        }
        for (ImsRegistration imsRegistration : registrationInfoByPhoneId) {
            if (imsRegistration != null && imsRegistration.getPhoneId() == i && !imsRegistration.getImsProfile().hasEmergencySupport() && imsRegistration.getImsProfile().getCmcType() == 0) {
                return imsRegistration;
            }
        }
        return null;
    }

    private boolean isServerSipError(SipError sipError) {
        return SipErrorBase.SipErrorType.ERROR_5XX.equals(sipError) || SipErrorBase.SipErrorType.ERROR_6XX.equals(sipError) || sipError.getCode() == SipErrorBase.FORBIDDEN.getCode() || sipError.getCode() == SipErrorBase.REQUEST_TIMEOUT.getCode();
    }

    private boolean isNormalTypeECallCsfbError(SipError sipError) {
        return SipErrorBase.SipErrorType.ERROR_4XX.equals(sipError) || SipErrorBase.SipErrorType.ERROR_5XX.equals(sipError) || SipErrorBase.SipErrorType.ERROR_6XX.equals(sipError);
    }

    public boolean isMatchWithErrorCodeList(String[] strArr, int i) throws JSONException {
        int i2 = 0;
        if (strArr == null) {
            return false;
        }
        boolean z = false;
        while (i2 < strArr.length) {
            String replace = strArr[i2].replace("x", "[0-9]");
            boolean matches = String.valueOf(i).matches(replace);
            if (matches) {
                Log.i(LOG_TAG, "match with " + replace);
                return matches;
            }
            i2++;
            z = matches;
        }
        return z;
    }

    /* JADX WARNING: Removed duplicated region for block: B:16:0x0035  */
    /* JADX WARNING: Removed duplicated region for block: B:20:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int getMergeCallType(int r4, boolean r5) {
        /*
            r3 = this;
            com.sec.ims.ImsRegistration r4 = r3.getImsRegistration(r4)
            r0 = 2
            r1 = 1
            if (r4 == 0) goto L_0x0032
            com.sec.ims.settings.ImsProfile r4 = r4.getImsProfile()
            boolean r2 = r3.hasRttCall()
            boolean r3 = r3.hasVideoCall()
            if (r3 == 0) goto L_0x001e
            boolean r3 = r4.getSupportMergeVideoConference()
            if (r3 == 0) goto L_0x001e
            r3 = r1
            goto L_0x001f
        L_0x001e:
            r3 = 0
        L_0x001f:
            java.lang.String r4 = r4.getMnoName()
            com.sec.internal.constants.Mno r4 = com.sec.internal.constants.Mno.fromName(r4)
            if (r3 == 0) goto L_0x0032
            com.sec.internal.constants.Mno r3 = com.sec.internal.constants.Mno.ATT
            if (r4 == r3) goto L_0x0030
            if (r2 == 0) goto L_0x0030
            goto L_0x0032
        L_0x0030:
            r3 = r0
            goto L_0x0033
        L_0x0032:
            r3 = r1
        L_0x0033:
            if (r5 == 0) goto L_0x003c
            if (r3 != r1) goto L_0x0039
            r3 = 5
            goto L_0x003c
        L_0x0039:
            if (r3 != r0) goto L_0x003c
            r3 = 6
        L_0x003c:
            return r3
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.volte2.ImsCallSessionManager.getMergeCallType(int, boolean):int");
    }

    public void handleEpdnSetupFail(int i) {
        List<ImsCallSession> emergencySession = getEmergencySession();
        String str = LOG_TAG;
        Log.i(str, "handleEpdnSetupFail Emergency Session Count : " + emergencySession.size() + " phoneId : " + i);
        for (ImsCallSession next : emergencySession) {
            try {
                if (i == next.getPhoneId()) {
                    CallProfile callProfile = next.getCallProfile();
                    if (callProfile == null || callProfile.getNetworkType() != 11) {
                        next.terminate(22);
                    } else {
                        Log.i(LOG_TAG, "handleEpdnSetupFail : skip terminate because this session uses ims pdn");
                    }
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public int getPhoneIdByCallId(int i) {
        ImsCallSession sessionByCallId = getSessionByCallId(i);
        if (sessionByCallId != null) {
            return sessionByCallId.getPhoneId();
        }
        return -1;
    }

    public int getParticipantIdForMerge(int i, int i2) {
        List<ImsCallSession> sessionByState = getSessionByState(i, CallConstants.STATE.HeldCall);
        if (sessionByState.isEmpty()) {
            Log.e(LOG_TAG, "No Hold Call : conference fail");
            return -1;
        }
        for (ImsCallSession next : sessionByState) {
            if (next.getCallId() != i2) {
                return next.getCallId();
            }
        }
        return -1;
    }

    public void releaseSessionByState(int i, CallConstants.STATE state) {
        for (ImsCallSession next : getSessionList()) {
            if (next.getPhoneId() == i && next.getCallState() == state) {
                try {
                    next.terminate(5, true);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public int sendRttSessionModifyRequest(int i, boolean z) {
        String str = LOG_TAG;
        Log.i(str, "sendRttSessionModifyRequest:callId : " + i + ", mode : " + z);
        ImsCallSession sessionByCallId = getSessionByCallId(i);
        if (sessionByCallId == null) {
            Log.i(str, "callId(" + i + ") is invalid");
            return -1;
        }
        IMSLog.c(LogClass.VOLTE_SEND_REQUEST_RTT, sessionByCallId.getPhoneId() + "," + sessionByCallId.getSessionId() + "," + z);
        int callType = sessionByCallId.getCallProfile().getCallType();
        if (ImsCallUtil.isRttCall(callType) && z) {
            this.mVolteServiceModule.onSendRttSessionModifyResponse(i, z, true);
            return 0;
        } else if (ImsCallUtil.isRttCall(callType) || z) {
            CallProfile callProfile = new CallProfile();
            callProfile.setCallType(0);
            Log.i(str, "SessionId : " + sessionByCallId.getSessionId() + ", currCallType : " + callType);
            callProfile.setCallType(ImsCallUtil.getCallTypeForRtt(callType, z));
            if (z) {
                int phoneId = sessionByCallId.getPhoneId();
                if (!ImsRegistry.getPdnController().isEpdgConnected(phoneId)) {
                    sessionByCallId.startRttDedicatedBearerTimer(this.mVolteServiceModule.getRttDbrTimer(phoneId));
                }
            }
            try {
                sessionByCallId.update(callProfile, 0, "");
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            return 0;
        } else {
            this.mVolteServiceModule.onSendRttSessionModifyResponse(i, z, false);
            return 0;
        }
    }

    public void sendRttSessionModifyResponse(int i, boolean z) {
        String str = LOG_TAG;
        Log.i(str, "sendRttSessionModifyResponse: callId : " + i + ", accept : " + z);
        ImsCallSession sessionByCallId = getSessionByCallId(i);
        if (sessionByCallId == null) {
            Log.i(str, "callId(" + i + ") is invalid");
            return;
        }
        IMSLog.c(LogClass.VOLTE_SEND_RESPONSE_RTT, sessionByCallId.getPhoneId() + "," + sessionByCallId.getSessionId() + "," + z);
        CallProfile callProfile = new CallProfile();
        callProfile.setCallType(0);
        int callType = sessionByCallId.getCallProfile().getCallType();
        Log.i(str, "SessionId : " + sessionByCallId.getSessionId() + ", currCallType : " + callType);
        callProfile.setCallType(ImsCallUtil.getCallTypeForRtt(callType, true));
        try {
            int phoneId = sessionByCallId.getPhoneId();
            if (z) {
                if (ImsCallUtil.isRttCall(callProfile.getCallType())) {
                    if (!ImsRegistry.getPdnController().isEpdgConnected(phoneId)) {
                        sessionByCallId.startRttDedicatedBearerTimer(this.mVolteServiceModule.getRttDbrTimer(phoneId));
                    }
                    sessionByCallId.getCallProfile().getMediaProfile().setRttMode(1);
                } else {
                    sessionByCallId.getCallProfile().getMediaProfile().setRttMode(0);
                }
                sessionByCallId.accept(callProfile);
                return;
            }
            sessionByCallId.reject(0);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public boolean isRttCall(int i) {
        CallProfile callProfile;
        ImsCallSession sessionByCallId = getSessionByCallId(i);
        if (sessionByCallId == null || (callProfile = sessionByCallId.getCallProfile()) == null) {
            return false;
        }
        boolean isRttCall = ImsCallUtil.isRttCall(callProfile.getCallType());
        String str = LOG_TAG;
        Log.i(str, "isRttCall, sessionId=" + i + ", result=" + isRttCall);
        return isRttCall;
    }

    public boolean hasDsdaDialingOrIncomingVtOnOtherSlot(int i) {
        if (!SimUtil.isDSDACapableDevice()) {
            return false;
        }
        int i2 = ImsConstants.Phone.SLOT_1;
        if (i == i2) {
            i2 = ImsConstants.Phone.SLOT_2;
        }
        for (ImsCallSession next : this.mSessionMap.values()) {
            if (next.getPhoneId() == i2) {
                CallConstants.STATE callState = next.getCallState();
                if (ImsCallUtil.isVideoCall(next.getCallProfile().getCallType()) && (callState == CallConstants.STATE.IncomingCall || callState == CallConstants.STATE.OutGoingCall || callState == CallConstants.STATE.AlertingCall)) {
                    return true;
                }
            }
        }
        return false;
    }

    public Map<Integer, ImsCallSession> getUnmodifiableSessionMap() {
        return this.mUnmodifiableSessionMap;
    }

    public boolean hasPendingCall(int i) {
        for (ImsCallSession next : this.mSessionMap.values()) {
            if (next.getPhoneId() == i && next.getPendingCall()) {
                return true;
            }
        }
        return false;
    }

    public void endcallBeforeRetry(int i, int i2) {
        IMSLog.i(LOG_TAG, i, "endcallBeforeRetry");
        for (ImsCallSession next : this.mSessionMap.values()) {
            if (next.getPhoneId() == i && next.getCallState() == CallConstants.STATE.OutGoingCall) {
                String str = LOG_TAG;
                Log.i(str, "Session(" + next.getSessionId() + "), state :" + next.getCallState().toString());
                next.notifyRetryingVoLteOrCsCall(i2);
                try {
                    if ("TIMER VZW EXPIRED".equals(next.getErrorMessage())) {
                        next.terminate(28);
                    } else if ("RRC CONNECTION REJECT".equals(next.getErrorMessage())) {
                        next.terminate(23);
                    } else {
                        next.terminate(13);
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean hasQecInCall() {
        for (ImsCallSession next : this.mSessionMap.values()) {
            if (next.isQuantumEncryptionCall() && next.getCallState() == CallConstants.STATE.InCall) {
                return true;
            }
        }
        return false;
    }

    public void updateQuantumPeerProfileStatus(int i, int i2, String str, String str2, String str3) {
        ImsCallSession session = getSession(i);
        if (session == null) {
            Log.i(LOG_TAG, "no session");
        } else {
            session.updateQuantumPeerProfileStatus(i2, str, str2, str3);
        }
    }

    public void updateQuantumQMKeyStatus(int i, int i2, String str, String str2, byte[] bArr, String str3) {
        ImsCallSession session = getSession(i);
        if (session == null) {
            Log.i(LOG_TAG, "no session");
        } else {
            session.updateQuantumQMKeyStatus(i2, str, str2, bArr, str3);
        }
    }
}
