package com.sec.internal.ims.servicemodules.base;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.telephony.ims.feature.ImsFeature;
import android.text.TextUtils;
import com.sec.ims.ImsRegistration;
import com.sec.internal.constants.ims.os.NetworkEvent;
import com.sec.internal.constants.ims.servicemodules.Registration;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.State;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.base.ModuleChannel;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.imsservice.ICall;
import com.sec.internal.interfaces.ims.servicemodules.IServiceModuleManager;
import com.sec.internal.interfaces.ims.servicemodules.base.IServiceModule;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class ServiceModuleBase extends Handler implements IServiceModule {
    protected int mActiveDataPhoneId;
    protected long[] mEnabledFeatures;
    protected final CopyOnWriteArrayList<Registration> mRegistrationList;
    State mState;

    protected static class InitialState extends State {
    }

    protected static class ReadyState extends State {
    }

    protected static class RunningState extends State {
    }

    protected static class StoppedState extends State {
    }

    public void cleanUp() {
    }

    public void dump() {
    }

    public abstract void handleIntent(Intent intent);

    public void handleModuleChannelRequest(Message message) {
    }

    public void onCallStateChanged(int i, List<ICall> list) {
    }

    public void onConfigured(int i) {
    }

    public void onDeregistering(ImsRegistration imsRegistration) {
    }

    public void onImsConifgChanged(int i, String str) {
    }

    public void onNetworkChanged(NetworkEvent networkEvent, int i) {
    }

    public void onReRegistering(int i, Set<String> set) {
    }

    public void onServiceSwitched(int i, ContentValues contentValues) {
    }

    public void onSimChanged(int i) {
    }

    public void onSimReady(int i) {
    }

    public void updateCapabilities(int i) {
    }

    protected ServiceModuleBase(Looper looper) {
        super(looper);
        this.mState = new InitialState();
        this.mActiveDataPhoneId = 0;
        this.mRegistrationList = new CopyOnWriteArrayList<>();
        this.mActiveDataPhoneId = SimUtil.getSimSlotPriority();
        long[] jArr = new long[SimUtil.getPhoneCount()];
        this.mEnabledFeatures = jArr;
        Arrays.fill(jArr, 0);
    }

    public String getName() {
        return getClass().getSimpleName();
    }

    public long getSupportFeature(int i) {
        return this.mEnabledFeatures[i];
    }

    public void init() {
        updateState(new ReadyState());
    }

    public void start() {
        updateState(new RunningState());
    }

    public void stop() {
        Arrays.fill(this.mEnabledFeatures, 0);
        this.mRegistrationList.clear();
        updateState(new StoppedState());
    }

    public void clearRegistrationList() {
        this.mRegistrationList.clear();
    }

    public boolean isReady() {
        return this.mState instanceof ReadyState;
    }

    public boolean isRunning() {
        return this.mState instanceof RunningState;
    }

    public boolean isStopped() {
        return this.mState instanceof StoppedState;
    }

    /* access modifiers changed from: protected */
    public ImsRegistration getRegistrationInfo() {
        if (this.mRegistrationList.size() > 0) {
            return this.mRegistrationList.get(0).getImsRegi();
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public int getRegistrationInfoId(ImsRegistration imsRegistration) {
        return IRegistrationManager.getRegistrationInfoId(imsRegistration.getImsProfile().getId(), imsRegistration.getPhoneId());
    }

    public ImsRegistration getImsRegistration() {
        return getImsRegistration(this.mActiveDataPhoneId);
    }

    public ImsRegistration getImsRegistration(int i) {
        Registration registration = getRegistration(i);
        if (registration != null) {
            return registration.getImsRegi();
        }
        return null;
    }

    public ImsRegistration getImsRegistration(int i, boolean z) {
        Iterator<Registration> it = this.mRegistrationList.iterator();
        while (it.hasNext()) {
            Registration next = it.next();
            if (next != null && next.getImsRegi().getPhoneId() == i && next.getImsRegi().getImsProfile().hasEmergencySupport() == z) {
                return next.getImsRegi();
            }
        }
        return null;
    }

    public Registration getRegistration(int i) {
        Iterator<Registration> it = this.mRegistrationList.iterator();
        while (it.hasNext()) {
            Registration next = it.next();
            if (next != null && next.getImsRegi().getPhoneId() == i && !next.getImsRegi().getImsProfile().hasEmergencySupport() && next.getImsRegi().getImsProfile().getCmcType() == 0) {
                return next;
            }
        }
        return null;
    }

    public void onRegistered(ImsRegistration imsRegistration) {
        int i;
        Registration next;
        Iterator<Registration> it = this.mRegistrationList.iterator();
        while (true) {
            if (!it.hasNext()) {
                i = -1;
                break;
            }
            next = it.next();
            if (next.getImsRegi().getHandle() == imsRegistration.getHandle() || (next.getImsRegi().getPhoneId() == imsRegistration.getPhoneId() && TextUtils.equals(next.getImsRegi().getImsProfile().getName(), imsRegistration.getImsProfile().getName()))) {
                i = this.mRegistrationList.indexOf(next);
            }
        }
        i = this.mRegistrationList.indexOf(next);
        if (i == -1) {
            this.mRegistrationList.add(new Registration(imsRegistration, false));
        } else {
            this.mRegistrationList.set(i, new Registration(imsRegistration, true));
        }
    }

    public void onDeregistered(ImsRegistration imsRegistration, int i) {
        Iterator<Registration> it = this.mRegistrationList.iterator();
        while (it.hasNext()) {
            Registration next = it.next();
            if (next.getImsRegi().getHandle() == imsRegistration.getHandle()) {
                this.mRegistrationList.remove(next);
                return;
            }
        }
    }

    public boolean isSupportImsDataChannel(int i) {
        ImsRegistration imsRegistration = getImsRegistration(i);
        return imsRegistration != null && imsRegistration.hasService("datachannel");
    }

    public ImsFeature.Capabilities queryCapabilityStatus(int i) {
        return new ImsFeature.Capabilities();
    }

    public void handleMessage(Message message) {
        int i = message.what;
        if (i > 8000 && i != 8999) {
            handleModuleChannelRequest(message);
        }
    }

    /* access modifiers changed from: protected */
    public void sendModuleResponse(Message message, int i, Object obj) {
        Message message2 = (Message) message.getData().getParcelable("callback_msg");
        if (message2 != null) {
            message2.arg1 = i;
            message2.obj = new Object[]{(ModuleChannel.Listener) message2.obj, obj};
            message2.sendToTarget();
        }
    }

    private void updateState(State state) {
        State state2 = this.mState;
        if (state2 != state) {
            state2.exit();
            this.mState = state;
            state.enter();
        }
    }

    /* access modifiers changed from: protected */
    public IServiceModuleManager getServiceModuleManager() {
        return ImsRegistry.getServiceModuleManager();
    }
}
