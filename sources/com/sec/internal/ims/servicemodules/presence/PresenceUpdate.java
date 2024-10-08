package com.sec.internal.ims.servicemodules.presence;

import android.os.Handler;
import android.os.HandlerThread;
import com.sec.ims.presence.PresenceInfo;
import com.sec.ims.presence.ServiceTuple;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.options.CapabilityConstants;
import com.sec.internal.constants.ims.servicemodules.presence.PresenceSubscription;
import com.sec.internal.ims.util.UriGenerator;
import com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityEventListener;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;

public class PresenceUpdate {
    private static final String LOG_TAG = "PresenceUpdate";
    protected Handler mBackgroundHandler;
    private final PresenceModule mPresence;

    PresenceUpdate(PresenceModule presenceModule) {
        this.mPresence = presenceModule;
        HandlerThread handlerThread = new HandlerThread(LOG_TAG, 10);
        handlerThread.start();
        this.mBackgroundHandler = new Handler(handlerThread.getLooper());
    }

    /* access modifiers changed from: package-private */
    public void onNewPresenceInformation(PresenceInfo presenceInfo, int i, PresenceSubscription presenceSubscription) {
        this.mBackgroundHandler.post(new PresenceUpdate$$ExternalSyntheticLambda1(this, i, presenceInfo, presenceSubscription));
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x0155  */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x017d  */
    /* JADX WARNING: Removed duplicated region for block: B:60:0x01a8  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public /* synthetic */ void lambda$onNewPresenceInformation$0(int r14, com.sec.ims.presence.PresenceInfo r15, com.sec.internal.constants.ims.servicemodules.presence.PresenceSubscription r16) {
        /*
            r13 = this;
            r0 = r13
            r6 = r14
            r1 = r15
            r2 = r16
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "onNewPresenceInformation: uri "
            r3.append(r4)
            java.lang.String r4 = r15.getUri()
            r3.append(r4)
            java.lang.String r3 = r3.toString()
            java.lang.String r4 = "PresenceUpdate"
            com.sec.internal.log.IMSLog.s(r4, r14, r3)
            com.sec.internal.ims.servicemodules.presence.PresenceModule r3 = r0.mPresence
            boolean r3 = r3.checkModuleReady(r14)
            if (r3 != 0) goto L_0x0028
            return
        L_0x0028:
            if (r2 != 0) goto L_0x0030
            java.lang.String r0 = "onNewPresenceInformation: failed to fetch subscription"
            com.sec.internal.log.IMSLog.e(r4, r14, r0)
            return
        L_0x0030:
            java.util.ArrayList r3 = new java.util.ArrayList
            r3.<init>()
            java.util.ArrayList r5 = new java.util.ArrayList
            r5.<init>()
            java.lang.String r7 = r15.getUri()
            if (r7 != 0) goto L_0x0045
            java.lang.String r7 = r15.getTelUri()
            goto L_0x0049
        L_0x0045:
            java.lang.String r7 = r15.getUri()
        L_0x0049:
            java.util.List r8 = r15.getServiceList()
            long r8 = com.sec.ims.presence.ServiceTuple.getFeatures(r8)
            long r10 = com.sec.ims.options.Capabilities.FEATURE_CHATBOT_ROLE
            boolean r8 = com.sec.internal.ims.servicemodules.options.CapabilityUtil.hasFeature(r8, r10)
            if (r8 == 0) goto L_0x00b8
            java.lang.String r8 = r15.getTelUri()
            if (r8 == 0) goto L_0x0064
            java.lang.String r8 = r15.getTelUri()
            goto L_0x0068
        L_0x0064:
            java.lang.String r8 = r15.getUri()
        L_0x0068:
            boolean r9 = r16.isSingleFetch()
            if (r9 == 0) goto L_0x007d
            java.util.Set r8 = r16.getUriList()
            java.util.Iterator r8 = r8.iterator()
            java.lang.Object r8 = r8.next()
            com.sec.ims.util.ImsUri r8 = (com.sec.ims.util.ImsUri) r8
            goto L_0x0081
        L_0x007d:
            com.sec.ims.util.ImsUri r8 = com.sec.ims.util.ImsUri.parse(r8)
        L_0x0081:
            if (r8 == 0) goto L_0x00b8
            java.lang.StringBuilder r9 = new java.lang.StringBuilder
            r9.<init>()
            java.lang.String r10 = "onNewPresenceInformation: chatbot uri "
            r9.append(r10)
            java.lang.String r10 = r8.toString()
            r9.append(r10)
            java.lang.String r9 = r9.toString()
            com.sec.internal.log.IMSLog.s(r4, r14, r9)
            com.sec.internal.constants.ims.servicemodules.options.BotServiceIdTranslator r9 = com.sec.internal.constants.ims.servicemodules.options.BotServiceIdTranslator.getInstance()
            java.lang.String r10 = r8.getMsisdn()
            java.lang.String r11 = r15.getUri()
            r9.register(r10, r11, r14)
            java.lang.String r9 = r8.toString()
            r15.setUri(r9)
            java.lang.String r8 = r8.toString()
            r15.setTelUri(r8)
        L_0x00b8:
            com.sec.internal.ims.servicemodules.presence.PresenceModule r8 = r0.mPresence
            com.sec.internal.ims.util.UriGenerator r8 = r8.getUriGenerator(r14)
            if (r8 != 0) goto L_0x00c6
            java.lang.String r0 = "onNewPresenceInformation: uriGenerator is null"
            com.sec.internal.log.IMSLog.i(r4, r14, r0)
            return
        L_0x00c6:
            boolean r9 = r15.isFetchSuccess()
            r10 = 0
            if (r9 == 0) goto L_0x010b
            java.lang.String r9 = r15.getTelUri()
            com.sec.ims.util.ImsUri r9 = com.sec.ims.util.ImsUri.parse(r9)
            com.sec.ims.util.ImsUri r8 = r8.normalize(r9)
            r5.add(r8)
            java.lang.String r8 = r15.getUri()
            if (r8 != 0) goto L_0x00fd
            com.sec.internal.ims.servicemodules.presence.PresenceModule r8 = r0.mPresence
            java.lang.Object r9 = r5.get(r10)
            com.sec.ims.util.ImsUri r9 = (com.sec.ims.util.ImsUri) r9
            com.sec.ims.presence.PresenceInfo r8 = r8.getPresenceInfo(r9, r14)
            if (r8 == 0) goto L_0x00fd
            java.lang.String r9 = r8.getUri()
            if (r9 == 0) goto L_0x00fd
            java.lang.String r8 = r8.getUri()
            r15.setUri(r8)
        L_0x00fd:
            com.sec.ims.util.ImsUri r8 = com.sec.ims.util.ImsUri.parse(r7)
            if (r8 == 0) goto L_0x0127
            com.sec.ims.util.ImsUri r7 = com.sec.ims.util.ImsUri.parse(r7)
            r3.add(r7)
            goto L_0x0127
        L_0x010b:
            if (r7 == 0) goto L_0x012b
            com.sec.ims.util.ImsUri r9 = com.sec.ims.util.ImsUri.parse(r7)
            if (r9 == 0) goto L_0x012b
            com.sec.ims.util.ImsUri r7 = com.sec.ims.util.ImsUri.parse(r7)
            r3.add(r7)
            java.lang.Object r7 = r3.get(r10)
            com.sec.ims.util.ImsUri r7 = (com.sec.ims.util.ImsUri) r7
            com.sec.ims.util.ImsUri r7 = r8.normalize(r7)
            r5.add(r7)
        L_0x0127:
            r12 = r5
            r5 = r3
            r3 = r12
            goto L_0x0135
        L_0x012b:
            java.util.ArrayList r3 = new java.util.ArrayList
            java.util.Set r5 = r16.getUriList()
            r3.<init>(r5)
            r5 = r3
        L_0x0135:
            boolean r7 = r16.isSingleFetch()
            if (r7 != 0) goto L_0x0165
            com.sec.internal.ims.servicemodules.presence.PresenceModule r7 = r0.mPresence
            int r8 = r16.getPhoneId()
            com.sec.internal.ims.servicemodules.presence.PresenceModule$PresenceModuleInfo r7 = r7.getPresenceModuleInfo(r8)
            com.sec.internal.constants.Mno r7 = r7.mMno
            com.sec.internal.constants.Mno r8 = com.sec.internal.constants.Mno.TMOUS
            if (r7 != r8) goto L_0x0165
            java.util.Iterator r7 = r3.iterator()
        L_0x014f:
            boolean r8 = r7.hasNext()
            if (r8 == 0) goto L_0x0165
            java.lang.Object r8 = r7.next()
            com.sec.ims.util.ImsUri r8 = (com.sec.ims.util.ImsUri) r8
            boolean r9 = r2.containsDropUri(r8)
            if (r9 == 0) goto L_0x014f
            r2.removeDropUri(r8)
            goto L_0x014f
        L_0x0165:
            com.sec.internal.constants.ims.servicemodules.options.CapabilityConstants$RequestType r7 = r16.getRequestType()
            com.sec.internal.constants.ims.servicemodules.options.CapabilityConstants$RequestType r8 = com.sec.internal.constants.ims.servicemodules.options.CapabilityConstants.RequestType.REQUEST_TYPE_LAZY
            if (r7 != r8) goto L_0x01a8
            java.lang.Object r7 = r3.get(r10)
            com.sec.ims.util.ImsUri r7 = (com.sec.ims.util.ImsUri) r7
            boolean r8 = r16.isLongLivedSubscription()
            boolean r7 = com.sec.internal.ims.servicemodules.presence.PresenceSubscriptionController.checkLazySubscription(r7, r8)
            if (r7 == 0) goto L_0x01a8
            java.lang.String r2 = "onNewPresenceInformation: lazy subscription not in order"
            com.sec.internal.log.IMSLog.i(r4, r14, r2)
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r5 = "onNewPresenceInformation: delayed uri "
            r2.append(r5)
            java.lang.Object r3 = r3.get(r10)
            r2.append(r3)
            java.lang.String r2 = r2.toString()
            com.sec.internal.log.IMSLog.s(r4, r14, r2)
            com.sec.internal.ims.servicemodules.presence.PresenceModule r0 = r0.mPresence
            r2 = 11
            android.os.Message r1 = r0.obtainMessage(r2, r14, r10, r15)
            r2 = 1000(0x3e8, double:4.94E-321)
            r0.sendMessageDelayed(r1, r2)
            return
        L_0x01a8:
            com.sec.internal.ims.servicemodules.presence.PresenceModule r4 = r0.mPresence
            r4.updatePresenceDatabase(r5, r15, r14)
            java.util.List r4 = r15.getServiceList()
            long r4 = com.sec.ims.presence.ServiceTuple.getFeatures(r4)
            com.sec.internal.ims.servicemodules.presence.PresenceModule r7 = r0.mPresence
            int r8 = r16.getPhoneId()
            com.sec.internal.ims.servicemodules.presence.PresenceModule$PresenceModuleInfo r7 = r7.getPresenceModuleInfo(r8)
            com.sec.internal.constants.ims.servicemodules.presence.PresenceResponse$PresenceStatusCode r7 = r7.mLastSubscribeStatusCode
            com.sec.internal.constants.ims.servicemodules.options.CapabilityConstants$CapExResult r7 = com.sec.internal.ims.servicemodules.presence.PresenceUtil.translateToCapExResult(r15, r4, r7, r2)
            com.sec.internal.ims.servicemodules.presence.PresenceModule r0 = r0.mPresence
            com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityEventListener r0 = r0.mListener
            if (r0 == 0) goto L_0x01d7
            java.lang.String r8 = r15.getPidf()
            r1 = r3
            r2 = r4
            r4 = r7
            r5 = r8
            r6 = r14
            r0.onCapabilityUpdate(r1, r2, r4, r5, r6)
        L_0x01d7:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.presence.PresenceUpdate.lambda$onNewPresenceInformation$0(int, com.sec.ims.presence.PresenceInfo, com.sec.internal.constants.ims.servicemodules.presence.PresenceSubscription):void");
    }

    /* access modifiers changed from: package-private */
    public void onNewWatcherInformation(PresenceInfo presenceInfo, int i, PresenceSubscription presenceSubscription) {
        this.mBackgroundHandler.post(new PresenceUpdate$$ExternalSyntheticLambda0(this, i, presenceInfo, presenceSubscription));
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$onNewWatcherInformation$1(int i, PresenceInfo presenceInfo, PresenceSubscription presenceSubscription) {
        ArrayList arrayList;
        IMSLog.s(LOG_TAG, i, "onNewWatcherInformation: uri " + presenceInfo.getUri());
        UriGenerator uriGenerator = this.mPresence.getUriGenerator(i);
        if (uriGenerator == null) {
            IMSLog.i(LOG_TAG, i, "onNewWatcherInformation: uriGenerator is null");
            return;
        }
        if (presenceInfo.isFetchSuccess()) {
            arrayList = new ArrayList();
            arrayList.add(ImsUri.parse(presenceInfo.getUri()));
        } else {
            arrayList = presenceSubscription != null ? new ArrayList(presenceSubscription.getUriList()) : null;
        }
        ArrayList arrayList2 = arrayList;
        if (arrayList2 != null) {
            this.mPresence.updatePresenceDatabase(arrayList2, presenceInfo, i);
            uriGenerator.normalize((ImsUri) arrayList2.get(0));
            long features = ServiceTuple.getFeatures(presenceInfo.getServiceList());
            CapabilityConstants.CapExResult translateToCapExResult = PresenceUtil.translateToCapExResult(presenceInfo, features, this.mPresence.getPresenceModuleInfo(i).mLastSubscribeStatusCode, presenceSubscription);
            ICapabilityEventListener iCapabilityEventListener = this.mPresence.mListener;
            if (iCapabilityEventListener != null) {
                iCapabilityEventListener.onCapabilityUpdate(arrayList2, features, translateToCapExResult, presenceInfo.getPidf(), i);
            }
        }
    }
}
