package com.sec.internal.ims.servicemodules.openapi;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import com.sec.ims.IDialogEventListener;
import com.sec.ims.IImsRegistrationListener;
import com.sec.ims.IImsService;
import com.sec.ims.ImsRegistration;
import com.sec.ims.extensions.ContextExt;
import com.sec.ims.openapi.ISipDialogListener;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.volte2.IImsCallEventListener;
import com.sec.ims.volte2.IVolteService;
import com.sec.internal.constants.ims.SipMsg;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.os.RemoteCallbackListWrapper;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.base.ServiceModuleBase;
import com.sec.internal.interfaces.ims.core.IPdnController;
import com.sec.internal.interfaces.ims.core.handler.ISipDialogInterface;
import com.sec.internal.interfaces.ims.servicemodules.openapi.IOpenApiServiceModule;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class OpenApiServiceModule extends ServiceModuleBase implements IOpenApiServiceModule {
    private static final int EVENT_INCOMING_SIP_MESSAGE = 100;
    private static final int EVENT_SIP_DIALOG_SEND_SIP_RESP = 101;
    private static final String LOG_TAG = "OpenApiServiceModule";
    private Context mContext;
    /* access modifiers changed from: private */
    public ArrayList<IDialogEventListener> mDialogEventListener = new ArrayList<>();
    /* access modifiers changed from: private */
    public ArrayList<IImsCallEventListener> mImsCallEventListener = new ArrayList<>();
    /* access modifiers changed from: private */
    public IImsService mImsService = null;
    private ISipDialogInterface mRawSipIntf;
    /* access modifiers changed from: private */
    public ArrayList<IImsRegistrationListener> mRegiListener = new ArrayList<>();
    private int mRegistrationId = -1;
    /* access modifiers changed from: private */
    public RemoteCallbackListWrapper<ISipDialogListener> mSipDialogListeners = new RemoteCallbackListWrapper<>();
    /* access modifiers changed from: private */
    public IVolteService mVolteService = null;

    public enum MessageType {
        MESSAGE_TYPE_UNKNOWN,
        MESSAGE_TYPE_PUBLISH,
        MESSAGE_TYPE_SUBSCRIBE
    }

    public void handleIntent(Intent intent) {
    }

    public void onDeregistering(ImsRegistration imsRegistration) {
    }

    private static class ParseResult {
        String event;
        int msgType;
        boolean result;

        public ParseResult(MessageType messageType, String str, boolean z) {
            this.msgType = messageType.ordinal();
            this.event = str;
            this.result = z;
        }
    }

    public OpenApiServiceModule(Looper looper, Context context, ISipDialogInterface iSipDialogInterface) {
        super(looper);
        this.mContext = context;
        this.mRawSipIntf = iSipDialogInterface;
        iSipDialogInterface.registerForIncomingMessages(this, 100, (Object) null);
    }

    public String[] getServicesRequiring() {
        return new String[]{"mmtel", SipMsg.EVENT_PRESENCE};
    }

    public void start() {
        Log.i(LOG_TAG, "connect VoLteService/ImsService");
        super.start();
        connectVoLteService();
        connectImsService();
    }

    public void onRegistered(ImsRegistration imsRegistration) {
        if (imsRegistration == null) {
            Log.d(LOG_TAG, "regiInfo is null");
            return;
        }
        super.onRegistered(imsRegistration);
        ImsProfile imsProfile = imsRegistration.getImsProfile();
        if (imsProfile != null && !imsProfile.hasEmergencySupport()) {
            this.mRegistrationId = getRegistrationInfoId(imsRegistration);
        }
    }

    public void onDeregistered(ImsRegistration imsRegistration, int i) {
        super.onDeregistered(imsRegistration, i);
        this.mRegistrationId = -1;
        super.onDeregistered(imsRegistration, i);
    }

    public void registerDialogEventListener(IDialogEventListener iDialogEventListener) throws RemoteException {
        IImsService iImsService = this.mImsService;
        if (iImsService != null) {
            iImsService.registerDialogEventListener(this.mActiveDataPhoneId, iDialogEventListener);
        } else {
            this.mDialogEventListener.add(iDialogEventListener);
        }
    }

    public void unregisterDialogEventListener(IDialogEventListener iDialogEventListener) throws RemoteException {
        this.mImsService.unregisterDialogEventListener(this.mActiveDataPhoneId, iDialogEventListener);
    }

    public void registerImsRegistrationListener(IImsRegistrationListener iImsRegistrationListener) throws RemoteException {
        IImsService iImsService = this.mImsService;
        if (iImsService != null) {
            iImsService.registerImsRegistrationListener(iImsRegistrationListener);
        } else {
            this.mRegiListener.add(iImsRegistrationListener);
        }
    }

    public void unregisterImsRegistrationListener(IImsRegistrationListener iImsRegistrationListener) throws RemoteException {
        this.mImsService.unregisterImsRegistrationListener(iImsRegistrationListener);
    }

    public void setFeatureTags(String[] strArr) {
        Log.d(LOG_TAG, "setFeatureTags: featureTags[" + Arrays.asList(strArr) + "]");
        ImsRegistry.getRegistrationManager().setThirdPartyFeatureTags(strArr);
    }

    public void registerIncomingSipMessageListener(final ISipDialogListener iSipDialogListener) {
        try {
            iSipDialogListener.asBinder().linkToDeath(new IBinder.DeathRecipient() {
                public void binderDied() {
                    Log.i(OpenApiServiceModule.LOG_TAG, "binder died, " + iSipDialogListener);
                    synchronized (OpenApiServiceModule.this.mSipDialogListeners) {
                        OpenApiServiceModule.this.mSipDialogListeners.unregister(iSipDialogListener);
                    }
                }
            }, 0);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        synchronized (this.mSipDialogListeners) {
            this.mSipDialogListeners.register(iSipDialogListener);
        }
        this.mRawSipIntf.openSipDialog(true);
    }

    public void unregisterIncomingSipMessageListener(ISipDialogListener iSipDialogListener) {
        synchronized (this.mSipDialogListeners) {
            this.mSipDialogListeners.unregister(iSipDialogListener);
        }
        if (this.mSipDialogListeners.getRegisteredCallbackCount() == 0) {
            this.mRawSipIntf.openSipDialog(false);
        }
    }

    public void registerImsCallEventListener(IImsCallEventListener iImsCallEventListener) {
        try {
            IVolteService iVolteService = this.mVolteService;
            if (iVolteService != null) {
                iVolteService.registerForCallStateEvent(iImsCallEventListener);
            } else {
                this.mImsCallEventListener.add(iImsCallEventListener);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void unregisterImsCallEventListener(IImsCallEventListener iImsCallEventListener) {
        try {
            this.mVolteService.deregisterForCallStateEvent(iImsCallEventListener);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public boolean sendSip(String str, ISipDialogListener iSipDialogListener) {
        ImsRegistration imsRegistration = getImsRegistration();
        if (imsRegistration == null) {
            return false;
        }
        return this.mRawSipIntf.sendSip(imsRegistration.getHandle(), str, obtainMessage(101, iSipDialogListener));
    }

    public void setupMediaPath(String[] strArr) {
        IPdnController pdnController = ImsRegistry.getPdnController();
        for (String requestRouteToHostAddress : strArr) {
            pdnController.requestRouteToHostAddress(11, requestRouteToHostAddress);
        }
    }

    private void onSipMessageReceived(AsyncResult asyncResult) {
        this.mSipDialogListeners.broadcastCallback(new OpenApiServiceModule$$ExternalSyntheticLambda1(asyncResult));
    }

    private void onSipParamsReceived(int i, String str, boolean z) {
        this.mSipDialogListeners.broadcastCallback(new OpenApiServiceModule$$ExternalSyntheticLambda0(i, str, z));
    }

    public void handleMessage(Message message) {
        super.handleMessage(message);
        Log.d(LOG_TAG, "handleMessage: what " + message.what);
        int i = message.what;
        if (i == 100) {
            onSipMessageReceived((AsyncResult) message.obj);
            ParseResult parseSipMsg = parseSipMsg(((Bundle) ((AsyncResult) message.obj).result).getString("message"));
            onSipParamsReceived(parseSipMsg.msgType, parseSipMsg.event, parseSipMsg.result);
        } else if (i == 101) {
            AsyncResult asyncResult = (AsyncResult) message.obj;
            String str = (String) asyncResult.result;
            ISipDialogListener iSipDialogListener = (ISipDialogListener) asyncResult.userObj;
            try {
                ParseResult parseSipMsg2 = parseSipMsg(str);
                iSipDialogListener.onSipReceived(str);
                iSipDialogListener.onSipParamsReceived(parseSipMsg2.msgType, parseSipMsg2.event, parseSipMsg2.result);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void connectVoLteService() {
        if (this.mVolteService == null) {
            Intent intent = new Intent();
            intent.setClassName("com.sec.imsservice", "com.sec.internal.ims.imsservice.VolteService2");
            ContextExt.bindServiceAsUser(this.mContext, intent, new ServiceConnection() {
                public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                    Log.i(OpenApiServiceModule.LOG_TAG, "Connected to VolteService.");
                    OpenApiServiceModule.this.mVolteService = IVolteService.Stub.asInterface(iBinder);
                    if (OpenApiServiceModule.this.mVolteService == null) {
                        Log.e(OpenApiServiceModule.LOG_TAG, "Failed to get IVolteService with " + iBinder);
                        return;
                    }
                    try {
                        Iterator it = OpenApiServiceModule.this.mImsCallEventListener.iterator();
                        while (it.hasNext()) {
                            OpenApiServiceModule.this.mVolteService.registerForCallStateEvent((IImsCallEventListener) it.next());
                        }
                        OpenApiServiceModule.this.mImsCallEventListener.clear();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }

                public void onServiceDisconnected(ComponentName componentName) {
                    Log.i(OpenApiServiceModule.LOG_TAG, "Disconnected from VolteService.");
                    OpenApiServiceModule.this.mVolteService = null;
                }
            }, 1, ContextExt.CURRENT_OR_SELF);
        }
    }

    private void connectImsService() {
        if (this.mImsService == null) {
            Intent intent = new Intent();
            intent.setClassName("com.sec.imsservice", "com.sec.internal.ims.imsservice.ImsService");
            ContextExt.bindServiceAsUser(this.mContext, intent, new ServiceConnection() {
                public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                    Log.i(OpenApiServiceModule.LOG_TAG, "Connected to ImsService.");
                    OpenApiServiceModule.this.mImsService = IImsService.Stub.asInterface(iBinder);
                    try {
                        Iterator it = OpenApiServiceModule.this.mDialogEventListener.iterator();
                        while (it.hasNext()) {
                            OpenApiServiceModule.this.mImsService.registerDialogEventListener(OpenApiServiceModule.this.mActiveDataPhoneId, (IDialogEventListener) it.next());
                        }
                        OpenApiServiceModule.this.mDialogEventListener.clear();
                        Iterator it2 = OpenApiServiceModule.this.mRegiListener.iterator();
                        while (it2.hasNext()) {
                            OpenApiServiceModule.this.mImsService.registerImsRegistrationListener((IImsRegistrationListener) it2.next());
                        }
                        OpenApiServiceModule.this.mRegiListener.clear();
                    } catch (RemoteException | NullPointerException e) {
                        e.printStackTrace();
                    }
                }

                public void onServiceDisconnected(ComponentName componentName) {
                    Log.i(OpenApiServiceModule.LOG_TAG, "Disconnected from ImsService.");
                    OpenApiServiceModule.this.mImsService = null;
                }
            }, 1, ContextExt.CURRENT_OR_SELF);
        }
    }

    public ImsRegistration getImsRegistration() {
        if (this.mRegistrationId != -1) {
            return ImsRegistry.getRegistrationManager().getRegistrationInfo(this.mRegistrationId);
        }
        return null;
    }

    /* JADX WARNING: Removed duplicated region for block: B:9:0x0032  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private com.sec.internal.ims.servicemodules.openapi.OpenApiServiceModule.ParseResult parseSipMsg(java.lang.String r13) {
        /*
            r12 = this;
            com.sec.internal.ims.servicemodules.openapi.OpenApiServiceModule$MessageType r12 = com.sec.internal.ims.servicemodules.openapi.OpenApiServiceModule.MessageType.MESSAGE_TYPE_UNKNOWN
            java.lang.String r0 = "\r\n"
            java.lang.String[] r13 = r13.split(r0)
            r0 = 0
            r1 = r13[r0]
            java.lang.String r2 = ":"
            int r1 = r1.indexOf(r2)
            java.lang.String r3 = "OpenApiServiceModule"
            r4 = 1
            if (r1 >= 0) goto L_0x0027
            java.lang.String r1 = "onSipMessageReceived: response message"
            android.util.Log.d(r3, r1)
            r1 = r13[r0]
            java.lang.String r5 = "200"
            boolean r1 = r1.contains(r5)
            if (r1 == 0) goto L_0x002c
            r1 = r4
            goto L_0x002d
        L_0x0027:
            java.lang.String r1 = "onSipMessageReceived: request message"
            android.util.Log.d(r3, r1)
        L_0x002c:
            r1 = r0
        L_0x002d:
            int r5 = r13.length
            r6 = 0
            r7 = r0
        L_0x0030:
            if (r7 >= r5) goto L_0x0086
            r8 = r13[r7]
            int r9 = r8.indexOf(r2)
            if (r9 >= 0) goto L_0x003b
            goto L_0x0083
        L_0x003b:
            java.lang.String r8 = r8.toLowerCase()
            java.lang.String r10 = r8.substring(r0, r9)
            java.lang.String r11 = "cseq"
            boolean r11 = r10.equals(r11)
            if (r11 == 0) goto L_0x0071
            java.lang.String r9 = "publish"
            boolean r9 = r8.contains(r9)
            if (r9 == 0) goto L_0x0057
            com.sec.internal.ims.servicemodules.openapi.OpenApiServiceModule$MessageType r12 = com.sec.internal.ims.servicemodules.openapi.OpenApiServiceModule.MessageType.MESSAGE_TYPE_PUBLISH
            goto L_0x0083
        L_0x0057:
            java.lang.String r9 = "notify"
            boolean r9 = r8.contains(r9)
            if (r9 == 0) goto L_0x0063
            com.sec.internal.ims.servicemodules.openapi.OpenApiServiceModule$MessageType r12 = com.sec.internal.ims.servicemodules.openapi.OpenApiServiceModule.MessageType.MESSAGE_TYPE_SUBSCRIBE
            r1 = r4
            goto L_0x0083
        L_0x0063:
            java.lang.String r9 = "subscribe"
            boolean r8 = r8.contains(r9)
            if (r8 == 0) goto L_0x0083
            if (r1 != 0) goto L_0x0083
            com.sec.internal.ims.servicemodules.openapi.OpenApiServiceModule$MessageType r12 = com.sec.internal.ims.servicemodules.openapi.OpenApiServiceModule.MessageType.MESSAGE_TYPE_SUBSCRIBE
            goto L_0x0083
        L_0x0071:
            java.lang.String r11 = "event"
            boolean r10 = r10.equals(r11)
            if (r10 == 0) goto L_0x0083
            int r9 = r9 + 1
            java.lang.String r6 = r8.substring(r9)
            java.lang.String r6 = r6.trim()
        L_0x0083:
            int r7 = r7 + 1
            goto L_0x0030
        L_0x0086:
            com.sec.internal.ims.servicemodules.openapi.OpenApiServiceModule$ParseResult r13 = new com.sec.internal.ims.servicemodules.openapi.OpenApiServiceModule$ParseResult
            r13.<init>(r12, r6, r1)
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r2 = "msgType = "
            r0.append(r2)
            r0.append(r12)
            java.lang.String r12 = " eventType = "
            r0.append(r12)
            r0.append(r6)
            java.lang.String r12 = " result = "
            r0.append(r12)
            r0.append(r1)
            java.lang.String r12 = r0.toString()
            android.util.Log.d(r3, r12)
            return r13
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.openapi.OpenApiServiceModule.parseSipMsg(java.lang.String):com.sec.internal.ims.servicemodules.openapi.OpenApiServiceModule$ParseResult");
    }
}
