package com.sec.internal.ims.servicemodules.volte2;

import android.content.ContentValues;
import android.content.Context;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import com.sec.ims.ImsRegistration;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.ImsUri;
import com.sec.ims.util.SipError;
import com.sec.ims.volte2.IImsCallSessionEventListener;
import com.sec.ims.volte2.data.CallProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.servicemodules.volte2.CallConstants;
import com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent;
import com.sec.internal.helper.ImsCallUtil;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.os.Debug;
import com.sec.internal.ims.diagnosis.ImsLogAgentUtil;
import com.sec.internal.ims.servicemodules.volte2.data.ConfCallSetupData;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.core.handler.IVolteServiceInterface;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.List;

public class ImsConfSession extends ImsCallSession {
    /* access modifiers changed from: private */
    public String LOG_TAG = "ImsConfSession";
    /* access modifiers changed from: private */
    public final List<String> mGroupInvitingParticipants = new ArrayList();
    /* access modifiers changed from: private */
    public final SparseArray<String> mGroupParticipants = new SparseArray<>();
    /* access modifiers changed from: private */
    public final List<ImsCallSession> mInvitingParticipants = new ArrayList();
    /* access modifiers changed from: private */
    public boolean mIsExtendToConference = false;
    /* access modifiers changed from: private */
    public final SparseIntArray mParticipantStatus = new SparseIntArray();
    /* access modifiers changed from: private */
    public final SparseArray<ImsCallSession> mParticipants = new SparseArray<>();
    /* access modifiers changed from: private */
    public int mPendingAddParticipantId = 0;

    public enum ConfUpdateCmd {
        UNKNOWN,
        ADD_PARTICIPANT,
        REMOVE_PARTICIPANT;

        public String toString() {
            int i = AnonymousClass1.$SwitchMap$com$sec$internal$ims$servicemodules$volte2$ImsConfSession$ConfUpdateCmd[ordinal()];
            if (i != 1) {
                return i != 2 ? "[Unknown]" : "[REMOVE_PARTICIPANT]";
            }
            return "[ADD_PARTICIPANT]";
        }
    }

    public class ConfCallStateMachine extends CallStateMachine {
        public static final int ON_CONFERENCE_CALL_TIMEOUT = 104;
        static final int ON_PARTICIPANT_ADDED = 101;
        static final int ON_PARTICIPANT_REMOVED = 102;
        static final int ON_PARTICIPANT_UPDATED = 103;
        private int mConfErrorCode = -1;
        /* access modifiers changed from: private */
        public ConfUpdateCmd mConfUpdateCmd = ConfUpdateCmd.UNKNOWN;
        /* access modifiers changed from: private */
        public int mPrevActiveSession = -1;
        private boolean mSentConfData = false;
        final ConfCallStateMachine mThisConfSm = this;
        final /* synthetic */ ImsConfSession this$0;

        private boolean isErrorCodeToResumeSession(int i) {
            return i == 486 || i == 487 || i == 480 || i == 403 || i == 503 || i == 400 || i == 606;
        }

        private String participantStatus(int i) {
            switch (i) {
                case 1:
                    return "INVITING";
                case 2:
                    return "ACTIVE";
                case 3:
                    return "REMOVING";
                case 4:
                    return "NON_ACTIVE";
                case 5:
                    return "ALERTING";
                case 6:
                    return "ON-HOLD";
                default:
                    return "UNKNOWN";
            }
        }

        /* JADX INFO: super call moved to the top of the method (can break code semantics) */
        ConfCallStateMachine(ImsConfSession imsConfSession, Context context, ImsCallSession imsCallSession, ImsRegistration imsRegistration, IVolteServiceModuleInternal iVolteServiceModuleInternal, Mno mno, IVolteServiceInterface iVolteServiceInterface, RemoteCallbackList<IImsCallSessionEventListener> remoteCallbackList, IRegistrationManager iRegistrationManager, IImsMediaController iImsMediaController, Looper looper) {
            super(context, imsCallSession, imsRegistration, iVolteServiceModuleInternal, mno, iVolteServiceInterface, remoteCallbackList, iRegistrationManager, iImsMediaController, "ConfCallStateMachine", looper);
            this.this$0 = imsConfSession;
            this.mReadyToCall = new ReadyToCall(this);
            this.mOutgoingCall = new OutgoingCall(this);
            this.mAlertingCall = new AlertingCall(this);
            this.mInCall = new InCall(this);
            this.mHeldCall = new HeldCall(this);
            this.mHoldingCall = new HoldingCall(this);
            this.mResumingCall = new ResumingCall(this);
            this.mEndingCall = new EndingCall(this);
        }

        class ReadyToCall extends ImsReadyToCall {
            ReadyToCall(CallStateMachine callStateMachine) {
                super(callStateMachine);
            }

            public boolean processMessage(Message message) {
                String str = this.LOG_TAG;
                Log.i(str, "[ReadyToCall] processMessage " + message.what);
                int i = message.what;
                if (i != 11) {
                    if (i != 72) {
                        return super.processMessage(message);
                    }
                    merge(message.arg1, message.arg2);
                } else if (ConfCallStateMachine.this.this$0.mCallProfile.getConferenceType() != 2) {
                    return super.processMessage(message);
                } else {
                    Log.i(this.LOG_TAG, "bindToNetwork for Group call");
                    this.mMediaController.bindToNetwork(this.mRegistration.getNetwork());
                    ConfCallStateMachine.this.this$0.mIsExtendToConference = true;
                    if (ConfCallStateMachine.this.this$0.mCallProfile.getDialingNumber() == null) {
                        ImsConfSession imsConfSession = ConfCallStateMachine.this.this$0;
                        imsConfSession.mCallProfile.setDialingNumber(imsConfSession.getConferenceUri(this.mRegistration.getImsProfile()));
                    }
                    conference((List) message.obj);
                }
                return true;
            }

            private void conference(List<String> list) {
                ConfCallStateMachine confCallStateMachine = ConfCallStateMachine.this;
                confCallStateMachine.callType = confCallStateMachine.this$0.mCallProfile.getCallType();
                if (list.size() > 0) {
                    ImsProfile imsProfile = this.mRegistration.getImsProfile();
                    ConfCallSetupData confCallSetupData = new ConfCallSetupData(ConfCallStateMachine.this.this$0.getConferenceUri(imsProfile), list, ConfCallStateMachine.this.callType);
                    confCallSetupData.enableSubscription(ConfCallStateMachine.this.this$0.getConfSubscribeEnabled(imsProfile));
                    confCallSetupData.setSubscribeDialogType(ConfCallStateMachine.this.this$0.getConfSubscribeDialogType(imsProfile));
                    confCallSetupData.setReferUriType(ConfCallStateMachine.this.this$0.getConfReferUriType(imsProfile));
                    confCallSetupData.setRemoveReferUriType(ConfCallStateMachine.this.this$0.getConfRemoveReferUriType(imsProfile));
                    confCallSetupData.setReferUriAsserted(ConfCallStateMachine.this.this$0.getConfReferUriAsserted(imsProfile));
                    confCallSetupData.setOriginatingUri(ConfCallStateMachine.this.this$0.getOriginatingUri());
                    confCallSetupData.setUseAnonymousUpdate(ConfCallStateMachine.this.this$0.getConfUseAnonymousUpdate(imsProfile));
                    confCallSetupData.setSupportPrematureEnd(ConfCallStateMachine.this.this$0.getConfSupportPrematureEnd(imsProfile));
                    int startNWayConferenceCall = this.mVolteSvcIntf.startNWayConferenceCall(this.mRegistration.getHandle(), confCallSetupData);
                    if (startNWayConferenceCall < 0) {
                        ConfCallStateMachine.this.mThisSm.sendMessage(4, 0, -1, new SipError(1005, "Not enough participant."));
                        return;
                    }
                    ConfCallStateMachine.this.this$0.mGroupInvitingParticipants.addAll(list);
                    String str = this.LOG_TAG;
                    Log.i(str, "[ReadyToCall] startNWayConferenceCall() returned session id " + startNWayConferenceCall);
                    ConfCallStateMachine.this.this$0.setSessionId(startNWayConferenceCall);
                    ConfCallStateMachine.this.this$0.mCallProfile.setDirection(0);
                    ConfCallStateMachine confCallStateMachine2 = ConfCallStateMachine.this;
                    int determineCamera = confCallStateMachine2.determineCamera(confCallStateMachine2.callType, false);
                    if (determineCamera >= 0) {
                        ConfCallStateMachine.this.this$0.startCamera(determineCamera);
                    }
                    ConfCallStateMachine confCallStateMachine3 = ConfCallStateMachine.this;
                    confCallStateMachine3.transitionTo(confCallStateMachine3.mOutgoingCall);
                    return;
                }
                ConfCallStateMachine.this.mThisSm.sendMessage(4, 0, -1, new SipError(1005, "Not enough participant."));
            }

            private void merge(int i, int i2) {
                int i3;
                int i4;
                Log.i(this.LOG_TAG, "HeldCallId : " + i + " AcitveCallId : " + i2);
                ArrayList arrayList = new ArrayList();
                ImsCallSession sessionByCallId = this.mModule.getSessionByCallId(i);
                if (sessionByCallId != null) {
                    Log.i(this.LOG_TAG, "Held Session Id : " + sessionByCallId.getSessionId());
                    arrayList.add(sessionByCallId);
                    i3 = sessionByCallId.getSessionId();
                } else {
                    i3 = -1;
                }
                ImsCallSession sessionByCallId2 = this.mModule.getSessionByCallId(i2);
                if (sessionByCallId2 == null || sessionByCallId2.getCallState() == CallConstants.STATE.ResumingCall || sessionByCallId2.getCallState() == CallConstants.STATE.ResumingVideo) {
                    i4 = -1;
                } else {
                    Log.i(this.LOG_TAG, "Active Session Id : " + sessionByCallId2.getSessionId());
                    arrayList.add(sessionByCallId2);
                    i4 = sessionByCallId2.getSessionId();
                }
                IMSLog.c(LogClass.VOLTE_MERGE, "Merge," + ConfCallStateMachine.this.this$0.mPhoneId + "," + i3 + "," + i4);
                if (i4 < 0 || i3 < 0) {
                    ConfCallStateMachine.this.mThisSm.sendMessage(4, 0, -1, new SipError(1005, "Not enough participant."));
                    return;
                }
                if (ConfCallStateMachine.this.this$0.mCallProfile.getForegroundSessionId() < 0 || ConfCallStateMachine.this.this$0.mCallProfile.getForegroundSessionId() == i4) {
                    int i5 = i4;
                    i4 = i3;
                    i3 = i5;
                }
                if (sessionByCallId2 != null) {
                    ConfCallStateMachine.this.this$0.mCallProfile.setOriginatingUri(sessionByCallId2.getOriginatingUri());
                }
                ConfCallStateMachine confCallStateMachine = ConfCallStateMachine.this;
                confCallStateMachine.callType = confCallStateMachine.this$0.mCallProfile.getCallType();
                ConfCallStateMachine.this.this$0.mInvitingParticipants.addAll(arrayList);
                ImsRegistration imsRegistration = this.mRegistration;
                if (imsRegistration == null || imsRegistration.getImsProfile() == null) {
                    ConfCallStateMachine.this.mThisSm.sendMessage(4, 0, -1, new SipError(1005, "Not Registration."));
                    return;
                }
                ConfCallStateMachine confCallStateMachine2 = ConfCallStateMachine.this;
                int i6 = confCallStateMachine2.callType;
                int mergeCallType = this.mModule.getMergeCallType(confCallStateMachine2.this$0.mPhoneId, i6 == 5 || i6 == 6);
                ImsProfile imsProfile = this.mRegistration.getImsProfile();
                String conferenceUri = ConfCallStateMachine.this.this$0.getConferenceUri(imsProfile);
                if (this.mMno == Mno.KDDI) {
                    Log.i(this.LOG_TAG, "[KDDI]Change ConfUri for Threeway merge call.");
                    conferenceUri = "sip:mmtel@3pty-factory.ims.mnc051.mcc440.3gppnetwork.org";
                }
                ConfCallSetupData confCallSetupData = new ConfCallSetupData(conferenceUri, i3, i4, mergeCallType);
                confCallSetupData.enableSubscription(ConfCallStateMachine.this.this$0.getConfSubscribeEnabled(imsProfile));
                confCallSetupData.setSubscribeDialogType(ConfCallStateMachine.this.this$0.getConfSubscribeDialogType(imsProfile));
                confCallSetupData.setReferUriType(ConfCallStateMachine.this.this$0.getConfReferUriType(imsProfile));
                confCallSetupData.setRemoveReferUriType(ConfCallStateMachine.this.this$0.getConfRemoveReferUriType(imsProfile));
                confCallSetupData.setReferUriAsserted(ConfCallStateMachine.this.this$0.getConfReferUriAsserted(imsProfile));
                confCallSetupData.setUseAnonymousUpdate(ConfCallStateMachine.this.this$0.getConfUseAnonymousUpdate(imsProfile));
                confCallSetupData.setOriginatingUri(ConfCallStateMachine.this.this$0.getOriginatingUri());
                confCallSetupData.setSupportPrematureEnd(ConfCallStateMachine.this.this$0.getConfSupportPrematureEnd(imsProfile));
                if (ConfCallStateMachine.this.this$0.mCallProfile.getAdditionalSipHeaders() != null) {
                    confCallSetupData.setExtraSipHeaders(ConfCallStateMachine.this.this$0.mCallProfile.getAdditionalSipHeaders());
                }
                int startNWayConferenceCall = this.mVolteSvcIntf.startNWayConferenceCall(this.mRegistration.getHandle(), confCallSetupData);
                if (startNWayConferenceCall < 0) {
                    ConfCallStateMachine.this.mThisSm.sendMessage(4, 0, -1, new SipError(1005, "remote exception."));
                    return;
                }
                ConfCallStateMachine.this.this$0.setSessionId(startNWayConferenceCall);
                if (ConfCallStateMachine.this.determineCamera(mergeCallType, false) >= 0) {
                    ConfCallStateMachine.this.this$0.startCamera(-1);
                }
                ConfCallStateMachine.this.mPrevActiveSession = i3;
                ConfCallStateMachine.this.this$0.mCallProfile.setDirection(0);
                ConfCallStateMachine.this.mThisConfSm.sendMessageDelayed(104, 45000);
                ConfCallStateMachine confCallStateMachine3 = ConfCallStateMachine.this;
                confCallStateMachine3.transitionTo(confCallStateMachine3.mOutgoingCall);
            }
        }

        class OutgoingCall extends ImsOutgoingCall {
            OutgoingCall(CallStateMachine callStateMachine) {
                super(callStateMachine);
            }

            public void enter() {
                ConfCallStateMachine confCallStateMachine = ConfCallStateMachine.this;
                confCallStateMachine.callType = 0;
                confCallStateMachine.errorCode = -1;
                confCallStateMachine.errorMessage = "";
                Log.i(this.LOG_TAG, "Enter [OutgoingCall]");
                ConfCallStateMachine confCallStateMachine2 = ConfCallStateMachine.this;
                CallProfile callProfile = confCallStateMachine2.this$0.mCallProfile;
                if (callProfile != null) {
                    confCallStateMachine2.callType = callProfile.getCallType();
                }
                ConfCallStateMachine confCallStateMachine3 = ConfCallStateMachine.this;
                int determineCamera = confCallStateMachine3.determineCamera(confCallStateMachine3.callType, false);
                if (determineCamera >= 0) {
                    ConfCallStateMachine.this.this$0.startCamera(determineCamera);
                }
            }

            public boolean processMessage(Message message) {
                String str = this.LOG_TAG;
                Log.i(str, "[OutgoingCall] processMessage " + message.what);
                int i = message.what;
                if (i == 4) {
                    int i2 = message.arg1;
                    if (!ConfCallStateMachine.this.this$0.mCallProfile.isConferenceCall()) {
                        return super.processMessage(message);
                    }
                    SipError sipError = (SipError) message.obj;
                    String str2 = this.LOG_TAG;
                    Log.e(str2, "[OutgoingCall] conference error code: " + sipError.getCode() + ": errorMessage " + sipError.getReason() + ": Retry After " + i2);
                    ConfCallStateMachine confCallStateMachine = ConfCallStateMachine.this;
                    confCallStateMachine.notifyOnError(1104, confCallStateMachine.errorMessage, Math.max(i2, 0));
                    ConfCallStateMachine.this.onConferenceFailError();
                    return true;
                } else if (i != 41) {
                    if (i != 104) {
                        return super.processMessage(message);
                    }
                    ConfCallStateMachine.this.notifyOnError(1104, "Conf call setup timeout");
                    return false;
                } else if (!ConfCallStateMachine.this.this$0.mCallProfile.isConferenceCall()) {
                    return super.processMessage(message);
                } else {
                    ConfCallStateMachine.this.onConferenceEstablished();
                    return true;
                }
            }
        }

        class AlertingCall extends ImsAlertingCall {
            AlertingCall(CallStateMachine callStateMachine) {
                super(callStateMachine);
            }

            public void enter() {
                ConfCallStateMachine confCallStateMachine = ConfCallStateMachine.this;
                confCallStateMachine.callType = 0;
                confCallStateMachine.errorCode = -1;
                confCallStateMachine.errorMessage = "";
                Log.i(this.LOG_TAG, "Enter [AlertingCall]");
            }

            public boolean processMessage(Message message) {
                String str = this.LOG_TAG;
                Log.i(str, "[AlertingCall] processMessage " + message.what);
                int i = message.what;
                if (i != 4) {
                    if (i != 41) {
                        if (i != 104) {
                            return super.processMessage(message);
                        }
                        ConfCallStateMachine.this.notifyOnError(1104, "Conf call setup timeout");
                        return false;
                    } else if (!ConfCallStateMachine.this.this$0.mCallProfile.isConferenceCall()) {
                        return super.processMessage(message);
                    } else {
                        ConfCallStateMachine.this.onConferenceEstablished();
                        return true;
                    }
                } else if (!ConfCallStateMachine.this.this$0.mCallProfile.isConferenceCall()) {
                    return super.processMessage(message);
                } else {
                    SipError sipError = (SipError) message.obj;
                    String str2 = this.LOG_TAG;
                    Log.e(str2, "[AlertingCall] conference error code: " + sipError.getCode() + ": errorMessage " + sipError.getReason());
                    ConfCallStateMachine confCallStateMachine = ConfCallStateMachine.this;
                    confCallStateMachine.notifyOnError(1104, confCallStateMachine.errorMessage);
                    ConfCallStateMachine.this.onConferenceFailError();
                    return true;
                }
            }
        }

        class InCall extends ImsInCall {
            InCall(CallStateMachine callStateMachine) {
                super(callStateMachine);
            }

            public boolean processMessage(Message message) {
                String str = this.LOG_TAG;
                Log.i(str, "[InCall] processMessage " + message.what);
                int i = message.what;
                if (!(i == 1 || i == 3)) {
                    if (i == 73) {
                        ConfCallStateMachine.this.this$0.mIsExtendToConference = true;
                        return super.processMessage(message);
                    } else if (i == 75) {
                        onReferStatus(message);
                        return true;
                    } else if (i == 91) {
                        ConfCallStateMachine.this.notifyOnModified(message.arg1);
                        if (ConfCallStateMachine.this.this$0.mPendingAddParticipantId != 0) {
                            ImsConfSession imsConfSession = ConfCallStateMachine.this.this$0;
                            imsConfSession.smCallStateMachine.sendMessage(53, imsConfSession.mPendingAddParticipantId, 0, (Object) null);
                            ConfCallStateMachine.this.this$0.mPendingAddParticipantId = 0;
                        }
                        return true;
                    } else if (!(i == 53 || i == 54)) {
                        switch (i) {
                            case 101:
                            case 102:
                            case 103:
                                break;
                            case 104:
                                ConfCallStateMachine.this.notifyOnError(1104, "Conf call setup timeout");
                                return false;
                            default:
                                return super.processMessage(message);
                        }
                    }
                }
                return false;
            }

            /* access modifiers changed from: package-private */
            public void onReferStatus(Message message) {
                if (this.mMno != Mno.LGU || message.arg1 <= 200 || ConfCallStateMachine.this.this$0.mCallProfile.isConferenceCall()) {
                    int i = message.arg1;
                    if ((i == 400 || i == 403 || i == 404 || i == 488 || i == 405) && this.mMno != Mno.KDDI) {
                        Log.e(this.LOG_TAG, "[InCall] On_Refer_Status Fail Error");
                        ConfCallStateMachine.this.onReferStatusFailError();
                    } else if (i == 487 && ConfCallStateMachine.this.mConfUpdateCmd == ConfUpdateCmd.ADD_PARTICIPANT) {
                        Log.i(this.LOG_TAG, "[InCall] On_Refer_Status ADD USER FAILED : notify error 487");
                        ConfCallStateMachine.this.this$0.mInvitingParticipants.clear();
                    }
                } else {
                    String str = this.LOG_TAG;
                    Log.e(str, "[InCall] On_Refer_Status conference setup fail error=" + message.arg1);
                    ConfCallStateMachine.this.notifyOnError(1105, "Add user to session failure");
                }
            }
        }

        class HoldingCall extends ImsHoldingCall {
            HoldingCall(CallStateMachine callStateMachine) {
                super(callStateMachine);
            }

            public boolean processMessage(Message message) {
                String str = this.LOG_TAG;
                Log.i(str, "[HoldingCall] processMessage " + message.what);
                int i = message.what;
                if (i == 1 || i == 53 || i == 54) {
                    return false;
                }
                switch (i) {
                    case 101:
                    case 102:
                    case 103:
                    case 104:
                        return false;
                    default:
                        return super.processMessage(message);
                }
            }
        }

        class HeldCall extends ImsHeldCall {
            HeldCall(CallStateMachine callStateMachine) {
                super(callStateMachine);
            }

            public void enter() {
                ConfCallStateMachine confCallStateMachine = ConfCallStateMachine.this;
                confCallStateMachine.callType = 0;
                confCallStateMachine.errorCode = -1;
                confCallStateMachine.errorMessage = "";
                confCallStateMachine.notifyOnHeld(true);
                Log.i(this.LOG_TAG, "Enter [HeldCall]");
            }

            public boolean processMessage(Message message) {
                String str = this.LOG_TAG;
                Log.i(str, "[HeldCall] processMessage " + message.what);
                int i = message.what;
                if (i == 1) {
                    return false;
                }
                if (i == 75) {
                    onReferStatus(message);
                    return true;
                } else if (i == 53 || i == 54) {
                    return false;
                } else {
                    switch (i) {
                        case 101:
                        case 102:
                        case 103:
                        case 104:
                            return false;
                        default:
                            return super.processMessage(message);
                    }
                }
            }

            /* access modifiers changed from: package-private */
            public void onReferStatus(Message message) {
                int i = message.arg1;
                if ((i == 403 || i == 404 || i == 488 || i == 405) && this.mMno != Mno.KDDI) {
                    Log.e(this.LOG_TAG, "[HeldCall] On_Refer_Status Fail Error");
                    ConfCallStateMachine.this.onReferStatusFailError();
                } else if (i == 487 && ConfCallStateMachine.this.mConfUpdateCmd == ConfUpdateCmd.ADD_PARTICIPANT) {
                    Log.i(this.LOG_TAG, "[HeldCall] On_Refer_Status ADD USER FAILED : notify error 487");
                    ConfCallStateMachine.this.this$0.mInvitingParticipants.clear();
                }
            }
        }

        class ResumingCall extends ImsResumingCall {
            ResumingCall(CallStateMachine callStateMachine) {
                super(callStateMachine);
            }

            public boolean processMessage(Message message) {
                String str = this.LOG_TAG;
                Log.i(str, "[ResumingCall] processMessage " + message.what);
                int i = message.what;
                if (!(i == 1 || i == 3)) {
                    if (i != 4) {
                        if (!(i == 53 || i == 54)) {
                            switch (i) {
                                case 101:
                                case 102:
                                case 103:
                                case 104:
                                    break;
                            }
                        }
                    } else {
                        SipError sipError = (SipError) message.obj;
                        int code = sipError.getCode();
                        if (ConfCallStateMachine.this.this$0.mCallProfile.isConferenceCall() && code == 800) {
                            String str2 = this.LOG_TAG;
                            Log.e(str2, "[ResumingCall] conference error code: " + code + ": errorMessage " + sipError.getReason() + "handle as NOT_HANDLED");
                            return false;
                        }
                    }
                    return super.processMessage(message);
                }
                return false;
            }
        }

        class EndingCall extends ImsEndingCall {
            EndingCall(CallStateMachine callStateMachine) {
                super(callStateMachine);
            }

            public boolean processMessage(Message message) {
                String str = this.LOG_TAG;
                Log.i(str, "[EndingCall] processMessage " + message.what);
                if (message.what != 3) {
                    return super.processMessage(message);
                }
                CallConstants.STATE state = CallConstants.STATE.Idle;
                ImsCallSession session = this.mModule.getSession(ConfCallStateMachine.this.mPrevActiveSession);
                if (session != null) {
                    state = session.getCallState();
                }
                if (ConfCallStateMachine.this.this$0.mCallProfile.isConferenceCall() && this.mMno.isChn() && (state == CallConstants.STATE.OutGoingCall || state == CallConstants.STATE.AlertingCall)) {
                    Log.e(this.LOG_TAG, "[EndingCall] conference ENDED");
                    ConfCallStateMachine.this.onConferenceFailError();
                }
                return super.processMessage(message);
            }
        }

        /* access modifiers changed from: protected */
        public void unhandledMessage(Message message) {
            String r0 = this.this$0.LOG_TAG;
            Log.i(r0, "[ANY_STATE] unhandledMessage " + message.what);
            int i = message.what;
            if (i == 1) {
                onConferenceEnded();
                super.unhandledMessage(message);
            } else if (i != 3) {
                if (i != 4) {
                    if (i == 53) {
                        addConferenceParticipant(message);
                    } else if (i != 54) {
                        switch (i) {
                            case 101:
                                onConferenceParticipantAdded(message);
                                return;
                            case 102:
                                onConferenceParticipantRemoved(message);
                                return;
                            case 103:
                                onConferenceParticipantUpdated(message);
                                return;
                            case 104:
                                onConferenceCallTimeout();
                                return;
                            default:
                                super.unhandledMessage(message);
                                return;
                        }
                    } else {
                        removeConferenceParticipant(message);
                    }
                } else if (this.this$0.mCallProfile.isConferenceCall()) {
                    SipError sipError = (SipError) message.obj;
                    int code = sipError.getCode();
                    String reason = sipError.getReason();
                    String r3 = this.this$0.LOG_TAG;
                    Log.e(r3, "[ANY_STATE] conference error code: " + code + ": errorMessage " + reason + " ConfUpdateCmd: " + this.mConfUpdateCmd.toString());
                    ConfUpdateCmd confUpdateCmd = this.mConfUpdateCmd;
                    if (confUpdateCmd == ConfUpdateCmd.ADD_PARTICIPANT) {
                        if (this.this$0.mCallProfile.getConferenceType() == 1) {
                            Log.e(this.this$0.LOG_TAG, "Participant add fail, clear list");
                            this.this$0.mInvitingParticipants.clear();
                        }
                        notifyOnError(1105, reason, 0);
                    } else if (confUpdateCmd == ConfUpdateCmd.REMOVE_PARTICIPANT) {
                        notifyOnError(1106, reason, 0);
                    }
                    this.mConfErrorCode = code;
                    onConferenceFailError(message, this.mConfUpdateCmd);
                } else {
                    super.unhandledMessage(message);
                }
            } else if (!this.this$0.mCallProfile.isConferenceCall() || this.this$0.mInvitingParticipants.size() <= 0) {
                super.unhandledMessage(message);
            } else {
                Log.e(this.this$0.LOG_TAG, "[ANY_STATE] Conference call ended before merge request is not completed");
                for (ImsCallSession terminate : this.this$0.mInvitingParticipants) {
                    try {
                        terminate.terminate(7);
                    } catch (RemoteException unused) {
                    }
                }
                this.mVolteSvcIntf.endCall(this.this$0.getSessionId(), this.this$0.mCallProfile.getCallType(), getSipReasonFromUserReason(7));
                onConferenceEnded();
                transitionTo(this.mEndingCall);
                super.unhandledMessage(message);
            }
        }

        /* access modifiers changed from: private */
        public void onConferenceFailError() {
            this.mConfUpdateCmd = ConfUpdateCmd.UNKNOWN;
            if (this.this$0.mParticipants.size() <= 0) {
                onConferenceEnded();
                if (this.this$0.getCallState() != CallConstants.STATE.EndingCall) {
                    transitionTo(this.mEndingCall);
                    sendMessage(3);
                }
            }
            if (this.this$0.mCallProfile.getCallType() != 5 && this.this$0.mCallProfile.getCallType() != 6) {
                try {
                    ImsCallSession session = this.mModule.getSession(this.mPrevActiveSession);
                    if (session != null) {
                        String r1 = this.this$0.LOG_TAG;
                        Log.e(r1, "conf fail; resume session:: " + this.mPrevActiveSession);
                        session.resume();
                    }
                    this.mPrevActiveSession = -1;
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        private void addConferenceParticipant(Message message) {
            String[] strArr;
            this.mConfUpdateCmd = ConfUpdateCmd.ADD_PARTICIPANT;
            if (this.this$0.mCallProfile.getConferenceType() == 2) {
                if (this.this$0.mIsExtendToConference) {
                    strArr = (String[]) message.obj;
                } else {
                    strArr = ((String) message.obj).split("\\$");
                }
                for (String buildUri : strArr) {
                    ImsConfSession imsConfSession = this.this$0;
                    ImsUri buildUri2 = imsConfSession.buildUri(buildUri, (String) null, imsConfSession.mCallProfile.getCallType());
                    Log.i(this.this$0.LOG_TAG, "addConferenceParticipant " + IMSLog.checker(buildUri2.toString()));
                    this.this$0.mGroupInvitingParticipants.add(buildUri2.toString());
                    if (this.mVolteSvcIntf.addParticipantToNWayConferenceCall(this.this$0.getSessionId(), buildUri2.toString()) < 0) {
                        Log.e(this.this$0.LOG_TAG, "addConferenceParticipant failed.");
                        return;
                    }
                }
                return;
            }
            int i = message.arg1;
            ImsCallSession sessionByCallId = this.mModule.getSessionByCallId(i);
            if (sessionByCallId == null) {
                Log.e(this.this$0.LOG_TAG, "[ANY_STATE] ADD_PARTICIPANT: session not exist with callId=" + i);
            } else if (sessionByCallId.getCallState() == CallConstants.STATE.InCall || sessionByCallId.getCallState() == CallConstants.STATE.HeldCall) {
                this.this$0.mInvitingParticipants.add(sessionByCallId);
                if (this.this$0.getCallState() == CallConstants.STATE.HeldCall) {
                    this.mPrevActiveSession = sessionByCallId.getSessionId();
                }
                if (this.mVolteSvcIntf.addParticipantToNWayConferenceCall(this.this$0.getSessionId(), sessionByCallId.getSessionId()) < 0) {
                    Log.e(this.this$0.LOG_TAG, "addConferenceParticipant: fail.");
                }
            } else {
                Log.e(this.this$0.LOG_TAG, "[ANY_STATE] call to be added is neither InCall nor HeldCall.");
            }
        }

        private void removeConferenceParticipant(Message message) {
            this.mConfUpdateCmd = ConfUpdateCmd.REMOVE_PARTICIPANT;
            if (this.mMno.isKor()) {
                Log.i(this.this$0.LOG_TAG, "KOR operator do not support remove participant");
            } else if (this.this$0.mCallProfile.getConferenceType() == 2) {
                ImsConfSession imsConfSession = this.this$0;
                ImsUri buildUri = imsConfSession.buildUri((String) message.obj, (String) null, imsConfSession.mCallProfile.getCallType());
                String r0 = this.this$0.LOG_TAG;
                Log.i(r0, "removeConferenceParticipant " + IMSLog.checker(buildUri.toString()));
                if (this.mVolteSvcIntf.removeParticipantFromNWayConferenceCall(this.this$0.getSessionId(), buildUri.toString()) < 0) {
                    Log.e(this.this$0.LOG_TAG, "removeConferenceParticipant failed.");
                }
            } else {
                int i = message.arg1;
                int r02 = this.this$0.getParticipantId(i);
                if (r02 == -1) {
                    String r3 = this.this$0.LOG_TAG;
                    Log.e(r3, "[ANY_STATE] REMOVE_PARTICIPANT: session not exist with callId=" + i);
                } else if (this.mVolteSvcIntf.removeParticipantFromNWayConferenceCall(this.this$0.getSessionId(), r02) < 0) {
                    Log.e(this.this$0.LOG_TAG, "removeConferenceParticipant: fail.");
                }
            }
        }

        private void onConferenceParticipantAdded(Message message) {
            for (CallStateEvent.ParticipantUser participantUser : (List) message.obj) {
                ImsCallSession r1 = this.this$0.getSessionFromInvitingParticipants(participantUser.getSessionId());
                if (this.this$0.mParticipants.get(participantUser.getParticipantId()) != null) {
                    String r12 = this.this$0.LOG_TAG;
                    Log.d(r12, "[ANY_STATE] already added participantId=" + participantUser.getParticipantId());
                } else if (r1 == null) {
                    String r13 = this.this$0.LOG_TAG;
                    Log.d(r13, "[ANY_STATE] ON_PARTICIPANT_ADDED: session not exist with sessionId=" + participantUser.getSessionId());
                } else {
                    int i = r1.isRemoteHeld() ? 6 : 2;
                    String r3 = this.this$0.LOG_TAG;
                    Log.i(r3, "[ANY_STATE] participant status=" + i);
                    notifyParticipantAdded(r1.getCallId());
                    this.mModule.onConferenceParticipantAdded(this.this$0.getSessionId(), participantUser.getUri());
                    this.this$0.mInvitingParticipants.remove(r1);
                    this.this$0.mParticipants.put(participantUser.getParticipantId(), r1);
                    this.this$0.mParticipantStatus.put(participantUser.getParticipantId(), i);
                    String r14 = this.this$0.LOG_TAG;
                    Log.i(r14, "[ANY_STATE] participant added - sessionId=" + participantUser.getSessionId() + " participantId=" + participantUser.getParticipantId());
                    if (this.this$0.mInvitingParticipants.size() == 0) {
                        Log.i(this.this$0.LOG_TAG, "[ANY_STATE] all participant add success!");
                        this.mThisConfSm.removeMessages(104);
                    }
                }
            }
            this.mConfUpdateCmd = ConfUpdateCmd.UNKNOWN;
        }

        private void onConferenceParticipantRemoved(Message message) {
            for (CallStateEvent.ParticipantUser participantUser : (List) message.obj) {
                ImsCallSession imsCallSession = (ImsCallSession) this.this$0.mParticipants.get(participantUser.getParticipantId());
                if (imsCallSession == null) {
                    String r1 = this.this$0.LOG_TAG;
                    Log.d(r1, "[ANY_STATE] ON_PARTICIPANT_REMOVED: participant not exist. participantId=" + participantUser.getParticipantId());
                } else {
                    notifyParticipantRemoved(imsCallSession.getCallId());
                    this.mModule.onConferenceParticipantRemoved(this.this$0.getSessionId(), participantUser.getUri());
                    this.this$0.mParticipants.remove(participantUser.getParticipantId());
                    this.this$0.mParticipantStatus.delete(participantUser.getParticipantId());
                    String r12 = this.this$0.LOG_TAG;
                    Log.i(r12, "[ANY_STATE] partcitipant removed - sessionId=" + participantUser.getSessionId() + " participantId=" + participantUser.getParticipantId());
                }
            }
            this.mConfUpdateCmd = ConfUpdateCmd.UNKNOWN;
            checkParticipantCount();
        }

        private void onConferenceParticipantUpdated(Message message) {
            updateConferenceParticipants((List) message.obj);
            checkParticipantCount();
            Log.i(this.this$0.LOG_TAG, "[ANY_STATE] participant list updated ");
        }

        private void handleConferenceFailResumeError(Message message) {
            try {
                ImsCallSession session = this.mModule.getSession(this.mPrevActiveSession);
                int code = ((SipError) message.obj).getCode();
                if (session != null) {
                    String r2 = this.this$0.LOG_TAG;
                    Log.e(r2, "conf fail; resume session:: " + this.mPrevActiveSession);
                    session.resume();
                }
                if (((code == 487 || code == 606) && this.mMno.isChn()) || ((code == 403 || code == 480) && this.mMno == Mno.IDEA_INDIA)) {
                    this.mVolteSvcIntf.endCall(this.this$0.getSessionId(), this.this$0.mCallProfile.getCallType(), getSipReasonFromUserReason(7));
                    onConferenceEnded();
                    transitionTo(this.mEndingCall);
                    super.unhandledMessage(message);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        private void handleConferenceFailError(Message message, ConfUpdateCmd confUpdateCmd) {
            try {
                String r0 = this.this$0.LOG_TAG;
                Log.i(r0, "confUpdateCmd : " + confUpdateCmd);
                int code = ((SipError) message.obj).getCode();
                if (confUpdateCmd != ConfUpdateCmd.ADD_PARTICIPANT) {
                    if (code != 800 || this.mMno != Mno.KDDI) {
                        if ((code != 500 || (!this.mMno.isChn() && this.mMno != Mno.DLOG)) && !(code == 5000 && this.mMno == Mno.TELIA_FINLAND)) {
                            List<ImsCallSession> sessionByState = this.mModule.getSessionByState(this.this$0.mPhoneId, CallConstants.STATE.HeldCall);
                            Log.i(this.this$0.LOG_TAG, "conf fail; terminate callsession; session::");
                            for (int i = 0; i < sessionByState.size(); i++) {
                                sessionByState.get(i).terminate(7);
                            }
                        } else {
                            ImsCallSession session = this.mModule.getSession(this.mPrevActiveSession);
                            if (session != null) {
                                String r2 = this.this$0.LOG_TAG;
                                Log.i(r2, "conf fail; resume session:: " + this.mPrevActiveSession + ", errorCode: " + code);
                                session.resume();
                            }
                        }
                        this.mVolteSvcIntf.endCall(this.this$0.getSessionId(), this.this$0.mCallProfile.getCallType(), getSipReasonFromUserReason(7));
                        onConferenceEnded();
                        transitionTo(this.mEndingCall);
                        super.unhandledMessage(message);
                    }
                } else if (code >= 5000) {
                    onConferenceEnded();
                    transitionTo(this.mEndingCall);
                    sendMessage(3);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        private void onConferenceFailError(Message message, ConfUpdateCmd confUpdateCmd) {
            this.mConfUpdateCmd = ConfUpdateCmd.UNKNOWN;
            int code = ((SipError) message.obj).getCode();
            String r1 = this.this$0.LOG_TAG;
            Log.e(r1, "[ANY_STATE] onConferenceFailError : " + code);
            if (this.this$0.mCallProfile.getCallType() != 5 && this.this$0.mCallProfile.getCallType() != 6) {
                if (isErrorCodeToResumeSession(code)) {
                    handleConferenceFailResumeError(message);
                } else {
                    handleConferenceFailError(message, confUpdateCmd);
                }
                this.mPrevActiveSession = -1;
            }
        }

        private void onConferenceEnded() {
            if (!this.mSentConfData) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(DiagnosisConstants.PSCI_KEY_PARTICIPANT_NUMBER, Integer.valueOf(this.this$0.mParticipants.size()));
                ImsLogAgentUtil.storeLogToAgent(this.this$0.mPhoneId, this.mContext, DiagnosisConstants.FEATURE_PSCI, contentValues);
                this.mSentConfData = true;
            }
            this.this$0.mParticipantStatus.clear();
            this.this$0.mParticipants.clear();
            this.mThisConfSm.removeMessages(104);
        }

        /* access modifiers changed from: private */
        public void onConferenceEstablished() {
            this.this$0.notifyOnConferenceEstablished();
            transitionTo(this.mInCall);
        }

        /* access modifiers changed from: package-private */
        public void notifyParticipantAdded(int i) {
            int beginBroadcast = this.mListeners.beginBroadcast();
            for (int i2 = 0; i2 < beginBroadcast; i2++) {
                try {
                    this.mListeners.getBroadcastItem(i2).onParticipantAdded(i);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            this.mListeners.finishBroadcast();
        }

        /* access modifiers changed from: package-private */
        public void notifyParticipantRemoved(int i) {
            int beginBroadcast = this.mListeners.beginBroadcast();
            for (int i2 = 0; i2 < beginBroadcast; i2++) {
                try {
                    this.mListeners.getBroadcastItem(i2).onParticipantRemoved(i);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            this.mListeners.finishBroadcast();
        }

        /* access modifiers changed from: package-private */
        public void notifyParticipantsUpdated(String[] strArr, int[] iArr, int[] iArr2) {
            int beginBroadcast = this.mListeners.beginBroadcast();
            for (int i = 0; i < beginBroadcast; i++) {
                try {
                    this.mListeners.getBroadcastItem(i).onParticipantUpdated(this.this$0.getSessionId(), strArr, iArr, iArr2);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            this.mListeners.finishBroadcast();
        }

        /* access modifiers changed from: package-private */
        public void updateConferenceParticipants(List<CallStateEvent.ParticipantUser> list) {
            if (this.this$0.mCallProfile.getConferenceType() == 1) {
                updateNwayConferenceParticipants(list);
            } else if (this.this$0.mCallProfile.getConferenceType() == 2) {
                updateGroupConferenceParticipants(list);
            }
        }

        private void updateNwayConferenceParticipants(List<CallStateEvent.ParticipantUser> list) {
            for (CallStateEvent.ParticipantUser next : list) {
                ImsUri parse = ImsUri.parse(next.getUri());
                String msisdn = parse != null ? parse.getMsisdn() : next.getUri();
                int participantId = next.getParticipantId();
                int participantStatus = next.getParticipantStatus();
                ImsCallSession imsCallSession = (ImsCallSession) this.this$0.mParticipants.get(participantId);
                String r5 = this.this$0.LOG_TAG;
                Log.i(r5, "updateConferenceParticipants: " + participantId + " status " + participantStatus(participantStatus));
                if (imsCallSession != null) {
                    if (participantStatus == 4) {
                        String r3 = this.this$0.LOG_TAG;
                        Log.i(r3, "old participant in non-active state. remove it." + IMSLog.checker(msisdn));
                        notifyParticipantRemoved(imsCallSession.getCallId());
                        this.mModule.onConferenceParticipantRemoved(this.this$0.getSessionId(), next.getUri());
                        this.this$0.mParticipants.remove(participantId);
                        this.this$0.mParticipantStatus.delete(participantId);
                    } else {
                        int i = this.this$0.mParticipantStatus.get(participantId);
                        this.this$0.mParticipantStatus.put(next.getParticipantId(), participantStatus);
                        if (participantStatus == 6 && i != 6 && Mno.RJIL != this.mMno) {
                            notifyConfParticipantOnHeld(imsCallSession.getCallId(), false);
                        } else if (!(participantStatus != 2 || i == 2 || Mno.RJIL == this.mMno)) {
                            notifyConfParticipanOnResumed(imsCallSession.getCallId(), false);
                        }
                    }
                }
                Log.i(this.this$0.LOG_TAG, "updateConferenceParticipants: new participant.");
            }
        }

        private void updateGroupConferenceParticipants(List<CallStateEvent.ParticipantUser> list) {
            int size = list.size();
            Log.i(this.this$0.LOG_TAG, "updateGroupConferenceParticipants participantSize=" + size);
            String[] strArr = new String[size];
            int[] iArr = new int[size];
            int[] iArr2 = new int[size];
            int[] iArr3 = new int[size];
            for (int i = 0; i < size; i++) {
                CallStateEvent.ParticipantUser participantUser = list.get(i);
                ImsUri parse = ImsUri.parse(participantUser.getUri());
                strArr[i] = parse != null ? parse.getMsisdn() : participantUser.getUri();
                iArr[i] = participantUser.getParticipantStatus();
                iArr3[i] = participantUser.getParticipantId();
                this.this$0.mParticipantStatus.put(iArr3[i], iArr[i]);
                Log.i(this.this$0.LOG_TAG, "participant=" + IMSLog.checker(strArr[i]) + ", participantId=" + iArr3[i] + ", status=" + participantStatus(iArr[i]));
                if (iArr[i] == 4) {
                    this.this$0.mGroupParticipants.remove(iArr3[i]);
                    iArr2[i] = this.mConfErrorCode;
                } else {
                    this.this$0.mGroupParticipants.put(iArr3[i], strArr[i]);
                    iArr2[i] = 0;
                }
            }
            this.mConfErrorCode = -1;
            notifyParticipantsUpdated(strArr, iArr, iArr2);
        }

        private void checkParticipantCount() {
            String r0 = this.this$0.LOG_TAG;
            Log.i(r0, "checkParticipantCount mParticipants=" + this.this$0.mParticipants.size() + ", mGroupParticipants=" + this.this$0.mGroupParticipants.size());
            if (this.this$0.mCallProfile.getConferenceType() == 1) {
                if (this.this$0.mParticipants.size() == 0 && this.this$0.mInvitingParticipants.size() == 0) {
                    this.mThisSm.sendMessage(1, 5);
                }
            } else if (this.this$0.mCallProfile.getConferenceType() == 2 && this.mMno == Mno.KDDI && this.this$0.mGroupParticipants.size() == 0) {
                this.mThisSm.sendMessage(1, 5);
            }
        }

        private void onConferenceCallTimeout() {
            Log.i(this.this$0.LOG_TAG, "onConferenceCallTimeout");
            try {
                for (ImsCallSession terminate : this.this$0.mInvitingParticipants) {
                    terminate.terminate(7);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            this.mThisSm.sendMessage(1, 5);
        }

        /* access modifiers changed from: private */
        public void onReferStatusFailError() {
            ConfUpdateCmd confUpdateCmd = this.mConfUpdateCmd;
            if (confUpdateCmd == ConfUpdateCmd.ADD_PARTICIPANT) {
                Log.e(this.this$0.LOG_TAG, "On_Refer_Status ADD USER FAILED");
                notifyOnError(1105, this.errorMessage, 0);
                this.this$0.mInvitingParticipants.clear();
            } else if (confUpdateCmd == ConfUpdateCmd.REMOVE_PARTICIPANT) {
                Log.e(this.this$0.LOG_TAG, "On_Refer_Status REMOVE USER FAILED");
                notifyOnError(1106, this.errorMessage, 0);
            } else if (this.this$0.mIsExtendToConference) {
                Log.e(this.this$0.LOG_TAG, "On_Refer_Status extendToConference failed.");
                this.this$0.mIsExtendToConference = false;
                notifyOnError(1105, "Add user to session failure");
            } else {
                Log.i(this.this$0.LOG_TAG, "On_Refer_Status TerminateConference");
                this.mThisSm.sendMessage(1, 5);
                try {
                    ImsCallSession session = this.mModule.getSession(this.mPrevActiveSession);
                    if (session != null) {
                        String r1 = this.this$0.LOG_TAG;
                        Log.e(r1, "Conf Fail; Resume Session:: " + this.mPrevActiveSession);
                        session.resume();
                    }
                    this.mPrevActiveSession = -1;
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public ImsConfSession(Context context, CallProfile callProfile, ImsRegistration imsRegistration, Looper looper, IVolteServiceModuleInternal iVolteServiceModuleInternal) {
        super(context, callProfile, imsRegistration, looper, iVolteServiceModuleInternal);
    }

    public void init(IVolteServiceInterface iVolteServiceInterface, IRegistrationManager iRegistrationManager) {
        this.mVolteSvcIntf = iVolteServiceInterface;
        this.mRegistrationManager = iRegistrationManager;
        ImsRegistration imsRegistration = this.mRegistration;
        if (imsRegistration == null) {
            this.mMno = SimUtil.getSimMno(this.mPhoneId);
        } else {
            this.mMno = Mno.fromName(imsRegistration.getImsProfile().getMnoName());
        }
        ConfCallStateMachine confCallStateMachine = new ConfCallStateMachine(this, this.mContext, this, this.mRegistration, this.mModule, this.mMno, this.mVolteSvcIntf, this.mListeners, this.mRegistrationManager, this.mMediaController, this.mLooper);
        this.smCallStateMachine = confCallStateMachine;
        confCallStateMachine.init();
        this.mImsCallDedicatedBearer = new ImsCallDedicatedBearer(this, this.mModule, this.mRegistration, this.mRegistrationManager, this.mMno, this.mAm, this.smCallStateMachine);
        this.mDiagnosisController = new DiagnosisController(this.smCallStateMachine);
        this.mImsCallSessionEventHandler = new ImsCallSessionEventHandler(this, this.mModule, this.mRegistration, this.mRegistrationManager, this.mMno, this.mAm, this.smCallStateMachine, this.mCallProfile, this.mVolteSvcIntf, this.mMediaController);
        this.mVolteSvcIntf.registerForCallStateEvent(this.mVolteStackEventHandler, 1, (Object) null);
        this.mVolteSvcIntf.registerForReferStatus(this.mVolteStackEventHandler, 5, this);
        this.mMediaController.registerForMediaEvent(this);
        Log.i(this.LOG_TAG, "start ConfCallStateMachine state");
        this.smCallStateMachine.start();
        setIsNrSaMode();
    }

    public int start(String str, CallProfile callProfile) throws RemoteException {
        if (callProfile == null) {
            callProfile = this.mCallProfile;
        }
        if (callProfile.getConferenceType() == 2) {
            startConference(str.split("\\$"), callProfile);
            return 0;
        }
        super.start(str, callProfile);
        return 0;
    }

    public void startIncoming() {
        super.startIncoming();
    }

    public void merge(int i, int i2) {
        this.smCallStateMachine.sendMessage(72, i, i2, (Object) null);
    }

    public void startConference(String[] strArr, CallProfile callProfile) throws RemoteException {
        if (callProfile == null) {
            Log.e(this.LOG_TAG, "startConference(): profile is NULL");
            throw new RemoteException("Cannot make conference call: profile is null");
        } else if (strArr != null) {
            ArrayList arrayList = new ArrayList();
            for (String buildUri : strArr) {
                arrayList.add(buildUri(buildUri, (String) null, callProfile.getCallType()).toString());
            }
            this.smCallStateMachine.sendMessage(11, (Object) arrayList);
        } else {
            Log.e(this.LOG_TAG, "start(): there is no participants");
            throw new RemoteException("Cannot conference : participants is null");
        }
    }

    public void inviteParticipants(int i) {
        ImsCallSession sessionByCallId = this.mModule.getSessionByCallId(i);
        ImsProfile imsProfile = this.mRegistration.getImsProfile();
        if (!(imsProfile == null || !imsProfile.getSupportUpgradeVideoConference() || this.mCallProfile == null || sessionByCallId == null || sessionByCallId.getCallProfile() == null || ImsCallUtil.isVideoCall(this.mCallProfile.getCallType()) || !ImsCallUtil.isVideoCall(sessionByCallId.getCallProfile().getCallType()))) {
            Log.i(this.LOG_TAG, "Need to Upgrade to Conference Call for add Video Participants");
            startCamera(-1);
            CallProfile callProfile = new CallProfile();
            callProfile.setCallType(2);
            if (this.smCallStateMachine.modifyCallType(callProfile, true)) {
                Log.i(this.LOG_TAG, "Modify Request success pending add Participant");
                this.mPendingAddParticipantId = i;
                return;
            }
        }
        this.smCallStateMachine.sendMessage(53, i, 0, (Object) null);
    }

    public void removeParticipants(int i) {
        this.smCallStateMachine.sendMessage(54, i, 0, (Object) null);
    }

    public void inviteGroupParticipant(String str) {
        this.smCallStateMachine.sendMessage(53, (Object) str);
    }

    public void removeGroupParticipant(String str) {
        this.smCallStateMachine.sendMessage(54, (Object) str);
    }

    public void extendToConference(String[] strArr) throws RemoteException {
        if (this.mIsExtendToConference) {
            this.smCallStateMachine.sendMessage(53, (Object) strArr);
        } else {
            super.extendToConference(strArr);
        }
    }

    public void holdVideo() {
        Log.i(this.LOG_TAG, "Unsupported API - holdVideo()");
    }

    public void resumeVideo() {
        Log.i(this.LOG_TAG, "Unsupported API - resumeVideo()");
    }

    public void setTtyMode(int i) {
        Log.e(this.LOG_TAG, "Not supported operation");
    }

    public boolean isQuantumEncryptionServiceAvailable() {
        Log.i(this.LOG_TAG, "isQuantumEncryptionServiceAvailable: not support for conf call");
        return false;
    }

    /* access modifiers changed from: protected */
    public void onImsCallEvent(CallStateEvent callStateEvent) {
        String str = this.LOG_TAG;
        Log.i(str, "mCallProfile.isConferenceCall() " + this.mCallProfile.isConferenceCall());
        if (!this.mCallProfile.isConferenceCall()) {
            Mno mno = this.mMno;
            if (mno != Mno.SKT && mno != Mno.LGU) {
                super.onImsCallEvent(callStateEvent);
                return;
            } else if (!callStateEvent.isConference()) {
                super.onImsCallEvent(callStateEvent);
                return;
            } else {
                Log.i(this.LOG_TAG, "Change to callprofile type");
                this.mCallProfile.setConferenceCall(2);
            }
        }
        if (callStateEvent.getSessionID() != getSessionId()) {
            String str2 = this.LOG_TAG;
            Log.i(str2, "not interest other sessionId " + callStateEvent.getSessionID());
            return;
        }
        String str3 = this.LOG_TAG;
        Log.i(str3, "event state : " + callStateEvent.getState());
        int i = AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE[callStateEvent.getState().ordinal()];
        if (i == 1) {
            updateCallProfile(callStateEvent.getParams());
            this.mCallProfile.setCallType(callStateEvent.getCallType());
            this.mCallProfile.setDialingNumber(ImsCallUtil.getRemoteCallerId(callStateEvent.getPeerAddr(), this.mMno, Debug.isProductShip()));
            this.mCallProfile.setRemoteVideoCapa(callStateEvent.getRemoteVideoCapa());
            this.smCallStateMachine.sendMessage(41);
            if (this.mMno == Mno.SKT) {
                String str4 = this.LOG_TAG;
                Log.i(str4, "event callType : " + callStateEvent.getCallType());
                this.smCallStateMachine.sendMessage(91, callStateEvent.getCallType());
                String str5 = this.LOG_TAG;
                Log.i(str5, "setDisplaySurface for video conference call (" + getSessionId() + "): " + getDisplaySurface());
                this.mMediaController.setDisplaySurface(getSessionId(), getDisplaySurface());
            }
        } else if (i == 2) {
            SipError errorCode = callStateEvent.getErrorCode();
            Log.e(this.LOG_TAG, "sendMessage CallStateMachine.ON_ENDED");
            if (errorCode == null) {
                this.smCallStateMachine.sendMessage(3);
            } else {
                this.smCallStateMachine.sendMessage(3, errorCode.getCode(), -1, errorCode.getReason());
            }
        } else if (i == 3) {
            this.mCallProfile.setCallType(callStateEvent.getCallType());
            this.smCallStateMachine.sendMessage(101, (Object) callStateEvent.getUpdatedParticipantsList());
        } else if (i == 4) {
            this.mCallProfile.setCallType(callStateEvent.getCallType());
            this.smCallStateMachine.sendMessage(102, (Object) callStateEvent.getUpdatedParticipantsList());
        } else if (i != 5) {
            super.onImsCallEvent(callStateEvent);
        } else {
            this.mCallProfile.setCallType(callStateEvent.getCallType());
            Mno mno2 = this.mMno;
            if (mno2 == Mno.SKT || mno2 == Mno.LGU) {
                this.smCallStateMachine.sendMessageDelayed(103, (Object) callStateEvent.getUpdatedParticipantsList(), 100);
            } else {
                this.smCallStateMachine.sendMessage(103, (Object) callStateEvent.getUpdatedParticipantsList());
            }
        }
    }

    /* renamed from: com.sec.internal.ims.servicemodules.volte2.ImsConfSession$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE;
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$ims$servicemodules$volte2$ImsConfSession$ConfUpdateCmd;

        /* JADX WARNING: Can't wrap try/catch for region: R(17:0|(2:1|2)|3|5|6|7|8|9|10|11|12|13|15|16|17|18|20) */
        /* JADX WARNING: Can't wrap try/catch for region: R(18:0|1|2|3|5|6|7|8|9|10|11|12|13|15|16|17|18|20) */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:11:0x0033 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:17:0x004f */
        /* JADX WARNING: Missing exception handler attribute for start block: B:7:0x001d */
        /* JADX WARNING: Missing exception handler attribute for start block: B:9:0x0028 */
        static {
            /*
                com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$CALL_STATE[] r0 = com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent.CALL_STATE.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE = r0
                r1 = 1
                com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$CALL_STATE r2 = com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent.CALL_STATE.ESTABLISHED     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r2 = r2.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r0[r2] = r1     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                r0 = 2
                int[] r2 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE     // Catch:{ NoSuchFieldError -> 0x001d }
                com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$CALL_STATE r3 = com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent.CALL_STATE.ENDED     // Catch:{ NoSuchFieldError -> 0x001d }
                int r3 = r3.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2[r3] = r0     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r2 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$CALL_STATE r3 = com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent.CALL_STATE.CONFERENCE_ADDED     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r3 = r3.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r4 = 3
                r2[r3] = r4     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                int[] r2 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE     // Catch:{ NoSuchFieldError -> 0x0033 }
                com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$CALL_STATE r3 = com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent.CALL_STATE.CONFERENCE_REMOVED     // Catch:{ NoSuchFieldError -> 0x0033 }
                int r3 = r3.ordinal()     // Catch:{ NoSuchFieldError -> 0x0033 }
                r4 = 4
                r2[r3] = r4     // Catch:{ NoSuchFieldError -> 0x0033 }
            L_0x0033:
                int[] r2 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE     // Catch:{ NoSuchFieldError -> 0x003e }
                com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$CALL_STATE r3 = com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent.CALL_STATE.CONFERENCE_PARTICIPANTS_UPDATED     // Catch:{ NoSuchFieldError -> 0x003e }
                int r3 = r3.ordinal()     // Catch:{ NoSuchFieldError -> 0x003e }
                r4 = 5
                r2[r3] = r4     // Catch:{ NoSuchFieldError -> 0x003e }
            L_0x003e:
                com.sec.internal.ims.servicemodules.volte2.ImsConfSession$ConfUpdateCmd[] r2 = com.sec.internal.ims.servicemodules.volte2.ImsConfSession.ConfUpdateCmd.values()
                int r2 = r2.length
                int[] r2 = new int[r2]
                $SwitchMap$com$sec$internal$ims$servicemodules$volte2$ImsConfSession$ConfUpdateCmd = r2
                com.sec.internal.ims.servicemodules.volte2.ImsConfSession$ConfUpdateCmd r3 = com.sec.internal.ims.servicemodules.volte2.ImsConfSession.ConfUpdateCmd.ADD_PARTICIPANT     // Catch:{ NoSuchFieldError -> 0x004f }
                int r3 = r3.ordinal()     // Catch:{ NoSuchFieldError -> 0x004f }
                r2[r3] = r1     // Catch:{ NoSuchFieldError -> 0x004f }
            L_0x004f:
                int[] r1 = $SwitchMap$com$sec$internal$ims$servicemodules$volte2$ImsConfSession$ConfUpdateCmd     // Catch:{ NoSuchFieldError -> 0x0059 }
                com.sec.internal.ims.servicemodules.volte2.ImsConfSession$ConfUpdateCmd r2 = com.sec.internal.ims.servicemodules.volte2.ImsConfSession.ConfUpdateCmd.REMOVE_PARTICIPANT     // Catch:{ NoSuchFieldError -> 0x0059 }
                int r2 = r2.ordinal()     // Catch:{ NoSuchFieldError -> 0x0059 }
                r1[r2] = r0     // Catch:{ NoSuchFieldError -> 0x0059 }
            L_0x0059:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.volte2.ImsConfSession.AnonymousClass1.<clinit>():void");
        }
    }

    public void handleRegistrationDone(ImsRegistration imsRegistration) {
        Log.i(this.LOG_TAG, "handleRegistrationDone");
        this.mRegistration = imsRegistration;
        this.smCallStateMachine.onRegistrationDone(imsRegistration);
        this.smCallStateMachine.sendMessage(11);
    }

    public void handleRegistrationFailed() {
        Log.i(this.LOG_TAG, "handleRegistrationFailed");
        this.mRegistration = null;
        this.smCallStateMachine.sendMessage(211);
    }

    public void setSessionId(int i) {
        this.LOG_TAG = IMSLog.appendSessionIdToLogTag(this.LOG_TAG, i);
        super.setSessionId(i);
    }

    /* access modifiers changed from: private */
    public void notifyOnConferenceEstablished() {
        int beginBroadcast = this.mListeners.beginBroadcast();
        for (int i = 0; i < beginBroadcast; i++) {
            try {
                this.mListeners.getBroadcastItem(i).onConferenceEstablished();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        this.mListeners.finishBroadcast();
    }

    /* access modifiers changed from: protected */
    public void notifyOnErrorBeforeEndParticipant() {
        if (this.mInvitingParticipants.size() != 0) {
            this.smCallStateMachine.notifyOnError(1104, "Conf call setup fail");
        }
    }

    /* access modifiers changed from: private */
    public int getParticipantId(int i) {
        for (int i2 = 0; i2 < this.mParticipants.size(); i2++) {
            if (this.mParticipants.valueAt(i2).getCallId() == i) {
                return this.mParticipants.keyAt(i2);
            }
        }
        return -1;
    }

    /* access modifiers changed from: private */
    public ImsCallSession getSessionFromInvitingParticipants(int i) {
        for (ImsCallSession next : this.mInvitingParticipants) {
            if (next.getSessionId() == i) {
                return next;
            }
        }
        return null;
    }
}
