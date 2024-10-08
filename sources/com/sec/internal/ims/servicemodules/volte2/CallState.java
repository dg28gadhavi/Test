package com.sec.internal.ims.servicemodules.volte2;

import android.content.Context;
import android.os.RemoteCallbackList;
import com.sec.ims.ImsRegistration;
import com.sec.ims.volte2.IImsCallSessionEventListener;
import com.sec.internal.constants.Mno;
import com.sec.internal.helper.State;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.helper.os.TelephonyManagerWrapper;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.core.handler.IVolteServiceInterface;
import com.sec.internal.log.IMSLog;

public class CallState extends State {
    protected String LOG_TAG = "CallStateMachine";
    protected Context mContext = null;
    CallStateMachine mCsm = null;
    protected RemoteCallbackList<IImsCallSessionEventListener> mListeners = null;
    protected IImsMediaController mMediaController = null;
    protected Mno mMno = Mno.DEFAULT;
    protected IVolteServiceModuleInternal mModule = null;
    protected ImsRegistration mRegistration = null;
    protected IRegistrationManager mRegistrationManager = null;
    protected ImsCallSession mSession = null;
    protected ITelephonyManager mTelephonyManager;
    protected IVolteServiceInterface mVolteSvcIntf = null;

    CallState(CallStateMachine callStateMachine) {
        Context context = callStateMachine.mContext;
        this.mContext = context;
        this.mVolteSvcIntf = callStateMachine.mVolteSvcIntf;
        this.mMno = callStateMachine.mMno;
        this.mCsm = callStateMachine;
        this.mSession = callStateMachine.mSession;
        this.mRegistration = callStateMachine.mRegistration;
        this.mModule = callStateMachine.mModule;
        this.mRegistrationManager = callStateMachine.mRegistrationManager;
        this.mMediaController = callStateMachine.mMediaController;
        this.mListeners = callStateMachine.mListeners;
        this.mTelephonyManager = TelephonyManagerWrapper.getInstance(context);
    }

    public void setLogTag(int i) {
        this.LOG_TAG = IMSLog.appendSessionIdToLogTag(this.LOG_TAG, i);
    }
}
