package com.sec.internal.ims.servicemodules.volte2;

import android.os.Message;
import android.util.Log;
import com.sec.epdg.EpdgManager;
import com.sec.ims.ImsRegistration;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.servicemodules.volte2.CallConstants;
import com.sec.internal.helper.PreciseAlarmManager;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.core.ISimManager;

public class ImsCallDedicatedBearer {
    private static final String LOG_TAG = "ImsCallDedicatedBearer";
    private PreciseAlarmManager mAm = null;
    private boolean mIsDRBLost;
    private Mno mMno;
    private IVolteServiceModuleInternal mModule = null;
    private ImsRegistration mRegistration = null;
    private IRegistrationManager mRegistrationManager = null;
    private int mRttBearerState;
    private Message mRttDedicatedBearerTimeoutMessage;
    private ImsCallSession mSession = null;
    private int mVideoBearerState;
    private int mVideoNGbrBearerState;
    private int mVoiceBearerState;
    private CallStateMachine smCallStateMachine = null;

    public ImsCallDedicatedBearer(ImsCallSession imsCallSession, IVolteServiceModuleInternal iVolteServiceModuleInternal, ImsRegistration imsRegistration, IRegistrationManager iRegistrationManager, Mno mno, PreciseAlarmManager preciseAlarmManager, CallStateMachine callStateMachine) {
        char c = Mno.MVNO_DELIMITER;
        this.mVoiceBearerState = 3;
        this.mVideoBearerState = 3;
        this.mVideoNGbrBearerState = 3;
        this.mRttBearerState = 3;
        this.mRttDedicatedBearerTimeoutMessage = null;
        this.mIsDRBLost = false;
        this.mSession = imsCallSession;
        this.mModule = iVolteServiceModuleInternal;
        this.mRegistration = imsRegistration;
        this.mRegistrationManager = iRegistrationManager;
        this.mMno = mno;
        this.smCallStateMachine = callStateMachine;
        this.mAm = preciseAlarmManager;
    }

    private boolean isIgnoredDedicatedBearLost(int i) {
        if ((i == 99 || i == 1) && this.mMno == Mno.ATT) {
            return true;
        }
        if (i == 99 || (!this.mMno.isKor() && !this.mMno.isOneOf(Mno.VZW, Mno.TELENOR_NORWAY, Mno.SFR, Mno.TELE2NL, Mno.SWISSCOM, Mno.CLARO_PERU, Mno.ENTEL_PERU, Mno.SMARTFREN, Mno.CABLE_PANAMA))) {
            return false;
        }
        return true;
    }

    private void onDedicatedBearerLost(int i) {
        EpdgManager epdgManager;
        if (isIgnoredDedicatedBearLost(i)) {
            String str = LOG_TAG;
            Log.i(str, "onDedicatedBearerLost: ignore DBR lost for mno:" + this.mMno + " qci:" + i);
        } else if (this.mMno.isChn() && i == 1 && this.smCallStateMachine.getState() == CallConstants.STATE.IncomingCall) {
            String str2 = LOG_TAG;
            Log.i(str2, "onDedicatedBearerLost: ignore DBR lost at incoming state for mno:" + this.mMno + " qci:" + i);
        } else {
            ISimManager simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(this.mSession.getPhoneId());
            if (simManagerFromSimSlot != null && simManagerFromSimSlot.isSimAvailable() && this.mRegistrationManager.isVoWiFiSupported(this.mSession.getPhoneId()) && (((epdgManager = this.mModule.getEpdgManager()) != null && epdgManager.isDuringHandoverForIMSBySim(this.mSession.getPhoneId())) || this.mSession.isEpdgCall())) {
                String str3 = LOG_TAG;
                Log.i(str3, "onDedicatedBearerLost: ignore Dedicated Bearer Lost due to EPDG for mno:" + this.mMno + ", qci:" + i);
            } else if (this.mMno != Mno.KDDI || !this.smCallStateMachine.mConfCallAdded) {
                String str4 = LOG_TAG;
                Log.i(str4, "onDedicatedBearerLost: Dedicated Bearer Lost mno:" + this.mMno + ", qci:" + i);
                if (i != 99) {
                    this.mIsDRBLost = true;
                    this.smCallStateMachine.sendMessageDelayed(5000, i, (long) (this.mMno.isChn() ? 500 : 1000));
                } else if (!this.smCallStateMachine.hasMessages(5000)) {
                    Message obtainMessage = this.smCallStateMachine.obtainMessage(210);
                    this.mRttDedicatedBearerTimeoutMessage = obtainMessage;
                    this.smCallStateMachine.sendMessageDelayed(obtainMessage, 500);
                }
            } else {
                String str5 = LOG_TAG;
                Log.i(str5, "onDedicatedBearerLost: igonre dedicated Bearer Lost mno:" + this.mMno + " after ending 3way conference call");
            }
        }
    }

    public void setDedicatedBearerState(int i, int i2) {
        String str = LOG_TAG;
        Log.i(str, "qci:" + i + ", state:" + i2);
        if (i == 1) {
            if (this.mVoiceBearerState != 3 && i2 == 3) {
                onDedicatedBearerLost(i);
            }
            this.mVoiceBearerState = i2;
        } else if (i == 2 || i == 3) {
            Mno mno = this.mMno;
            if ((mno == Mno.CTC || mno == Mno.CU || mno == Mno.CTCMO) && this.mVideoBearerState != 3 && i2 == 3) {
                onDedicatedBearerLost(i);
            }
            this.mVideoBearerState = i2;
        } else if (i == 7 || i == 8 || i == 9) {
            this.mVideoNGbrBearerState = i2;
        } else if (i == 99) {
            int i3 = this.mRttBearerState;
            if (i3 == 3 && i2 == 1) {
                this.mSession.stopRttDedicatedBearerTimer();
            } else if (i3 == 1 && i2 == 3) {
                onDedicatedBearerLost(i);
            }
            this.mRttBearerState = i2;
        }
    }

    public int getDedicatedBearerState(int i) {
        int i2;
        if (i != 1) {
            if (i != 2) {
                i2 = 3;
                if (i != 3) {
                    if (i == 7 || i == 8 || i == 9) {
                        i2 = this.mVideoNGbrBearerState;
                    } else if (i != 99) {
                        String str = LOG_TAG;
                        Log.i(str, "unknown qci:" + i);
                    } else {
                        i2 = this.mRttBearerState;
                    }
                }
            }
            i2 = this.mVideoBearerState;
        } else {
            i2 = this.mVoiceBearerState;
        }
        String str2 = LOG_TAG;
        Log.i(str2, "qci:" + i + ", state:" + i2);
        return i2;
    }

    /* access modifiers changed from: protected */
    public void startRttDedicatedBearerTimer(long j) {
        if (j <= 0) {
            String str = LOG_TAG;
            Log.i(str, "startRttDedicatedBearerTimer: Not start RttDedicatedBearerTimer : millis = " + j);
            return;
        }
        Mno mno = this.mMno;
        if (mno == Mno.ATT || (mno == Mno.TMOUS && this.mSession.mIsNrSaMode)) {
            Log.i(LOG_TAG, "startRttDedicatedBearerTimer: Not start RttDedicatedBearerTimer");
            return;
        }
        ImsRegistration imsRegistration = this.mRegistration;
        if (imsRegistration != null && imsRegistration.getImsProfile() != null && this.mRegistration.getImsProfile().getUsePrecondition() == 0) {
            Log.i(LOG_TAG, "startRttDedicatedBearerTimer: Not start RttDedicatedBearerTimer");
        } else if (getDedicatedBearerState(99) != 3) {
            Log.i(LOG_TAG, "RTT Dedicated Bearer opened");
        } else if (this.mRttDedicatedBearerTimeoutMessage != null) {
            Log.i(LOG_TAG, "RTT Dedicated Bearer Timer already has been started");
        } else {
            String str2 = LOG_TAG;
            Log.i(str2, "startRttDedicatedBearerTimer: " + j);
            stopRttDedicatedBearerTimer();
            this.mRttDedicatedBearerTimeoutMessage = this.smCallStateMachine.obtainMessage(209);
            this.mAm.sendMessageDelayed(getClass().getSimpleName(), this.mRttDedicatedBearerTimeoutMessage, j);
        }
    }

    /* access modifiers changed from: protected */
    public void stopRttDedicatedBearerTimer() {
        if (this.mRttDedicatedBearerTimeoutMessage != null) {
            Log.i(LOG_TAG, "stopRttDedicatedBearerTimer: ");
            this.mAm.removeMessage(this.mRttDedicatedBearerTimeoutMessage);
            this.mRttDedicatedBearerTimeoutMessage = null;
        }
    }

    /* access modifiers changed from: protected */
    public void setRttDedicatedBearerTimeoutMessage(Message message) {
        this.mRttDedicatedBearerTimeoutMessage = message;
    }

    public boolean getDRBLost() {
        return this.mIsDRBLost;
    }

    public void setDRBLost(boolean z) {
        this.mIsDRBLost = z;
    }
}
