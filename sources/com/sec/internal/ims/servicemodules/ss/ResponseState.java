package com.sec.internal.ims.servicemodules.ss;

import android.os.Bundle;
import android.os.Message;
import android.telephony.ims.ImsSsInfo;
import android.text.TextUtils;
import com.sec.internal.constants.Mno;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.State;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.ims.servicemodules.ss.CallBarringData;
import com.sec.internal.ims.servicemodules.ss.CallForwardingData;
import com.sec.internal.ims.servicemodules.ss.SsRuleData;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ResponseState extends State {
    public static final String LOG_TAG = UtStateMachine.LOG_TAG;
    HttpResponseParams mResponseData = null;
    private UtStateMachine mUsm;

    public void enter() {
    }

    public ResponseState(UtStateMachine utStateMachine) {
        this.mUsm = utStateMachine;
    }

    public boolean processMessage(Message message) {
        String str = UtStateMachine.LOG_TAG;
        int i = this.mUsm.mPhoneId;
        UtLog.i(str, i, "ResponseState::ProcessMessage " + UtLog.getStringMessage(message.what));
        Mno simMno = SimUtil.getSimMno(this.mUsm.mPhoneId);
        int i2 = message.what;
        if (i2 == 2) {
            this.mUsm.disconnectPdn();
        } else if (i2 == 3) {
            this.mUsm.sendMessage(12);
        } else if (i2 != 100) {
            switch (i2) {
                case 10:
                    this.mResponseData = (HttpResponseParams) message.obj;
                    handleHttpResult();
                    break;
                case 11:
                    this.mUsm.mIsUtConnectionError = true;
                    int i3 = message.arg1;
                    if (!simMno.isChn()) {
                        this.mUsm.sendMessage(12, i3);
                        break;
                    } else {
                        this.mUsm.sendMessage(12, i3, 0, message.obj);
                        break;
                    }
                case 12:
                    if (simMno == Mno.CTC || simMno == Mno.CTCMO) {
                        UtStateMachine utStateMachine = this.mUsm;
                        if (utStateMachine.mIsSuspended) {
                            utStateMachine.mIsFailedBySuspended = true;
                            utStateMachine.transitionTo(utStateMachine.mRequestState);
                        }
                    }
                    return false;
                case 13:
                    responseGetFromCache();
                    break;
                case 14:
                case 15:
                    return false;
            }
        } else {
            this.mUsm.sendMessage(12, 1016);
        }
        return true;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0062, code lost:
        r0 = r7.mUsm;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void handleHttpResult() {
        /*
            r7 = this;
            com.sec.internal.helper.httpclient.HttpResponseParams r0 = r7.mResponseData
            int r0 = r0.getStatusCode()
            r1 = 200(0xc8, float:2.8E-43)
            r2 = 12
            r3 = 116(0x74, float:1.63E-43)
            if (r0 == r1) goto L_0x00ce
            com.sec.internal.helper.httpclient.HttpResponseParams r0 = r7.mResponseData
            int r0 = r0.getStatusCode()
            r1 = 201(0xc9, float:2.82E-43)
            if (r0 != r1) goto L_0x001a
            goto L_0x00ce
        L_0x001a:
            com.sec.internal.helper.httpclient.HttpResponseParams r0 = r7.mResponseData
            int r0 = r0.getStatusCode()
            r1 = 404(0x194, float:5.66E-43)
            r4 = 1
            if (r0 != r1) goto L_0x0058
            com.sec.internal.ims.servicemodules.ss.UtStateMachine r0 = r7.mUsm
            com.sec.internal.ims.servicemodules.ss.UtFeatureData r1 = r0.mFeature
            boolean r1 = r1.supportSimservsRetry
            if (r1 == 0) goto L_0x0094
            com.sec.internal.ims.servicemodules.ss.UtProfile r0 = r0.mProfile
            int r0 = r0.type
            boolean r0 = com.sec.internal.ims.servicemodules.ss.UtUtils.isPutRequest(r0)
            if (r0 != 0) goto L_0x0094
            com.sec.internal.ims.servicemodules.ss.UtStateMachine r0 = r7.mUsm
            com.sec.internal.ims.servicemodules.ss.UtProfile r1 = r0.mProfile
            int r1 = r1.type
            if (r1 == r3) goto L_0x0094
            java.lang.String r1 = com.sec.internal.ims.servicemodules.ss.UtStateMachine.LOG_TAG
            int r0 = r0.mPhoneId
            java.lang.String r2 = "Requested document is not found. Get simserv document."
            com.sec.internal.ims.servicemodules.ss.UtLog.i(r1, r0, r2)
            com.sec.internal.ims.servicemodules.ss.UtStateMachine r0 = r7.mUsm
            r0.mIsGetSdBy404 = r4
            com.sec.internal.ims.servicemodules.ss.UtProfile r1 = r0.mProfile
            int r2 = r1.type
            r0.mPrevGetType = r2
            r1.type = r3
            r7.sendHttp()
            return
        L_0x0058:
            com.sec.internal.helper.httpclient.HttpResponseParams r0 = r7.mResponseData
            int r0 = r0.getStatusCode()
            r1 = 412(0x19c, float:5.77E-43)
            if (r0 != r1) goto L_0x0094
            com.sec.internal.ims.servicemodules.ss.UtStateMachine r0 = r7.mUsm
            int r1 = r0.mCount412RetryDone
            r5 = 3
            if (r1 >= r5) goto L_0x0094
            com.sec.internal.ims.servicemodules.ss.UtProfile r5 = r0.mProfile
            int r5 = r5.type
            r6 = 101(0x65, float:1.42E-43)
            if (r5 == r6) goto L_0x0087
            r6 = 103(0x67, float:1.44E-43)
            if (r5 == r6) goto L_0x0087
            r6 = 105(0x69, float:1.47E-43)
            if (r5 == r6) goto L_0x0087
            r6 = 115(0x73, float:1.61E-43)
            if (r5 == r6) goto L_0x007e
            goto L_0x0094
        L_0x007e:
            r0.mIsGetAfter412 = r4
            int r1 = r1 + r4
            r0.mCount412RetryDone = r1
            r7.sendHttp()
            return
        L_0x0087:
            r2 = -1
            r0.mPrevGetType = r2
            int r1 = r1 + r4
            r0.mCount412RetryDone = r1
            r0.clearCachedSsData(r5)
            r7.sendHttp()
            return
        L_0x0094:
            com.sec.internal.ims.servicemodules.ss.UtStateMachine r0 = r7.mUsm
            com.sec.internal.ims.servicemodules.ss.UtProfile r1 = r0.mProfile
            int r1 = r1.type
            if (r1 != r3) goto L_0x00a3
            boolean r0 = r0.mIsGetSdBy404
            if (r0 == 0) goto L_0x00a3
            r7.recoverUtProfileAfter404Retry()
        L_0x00a3:
            com.sec.internal.helper.httpclient.HttpResponseParams r0 = r7.mResponseData
            java.lang.String r0 = r0.getDataString()
            boolean r0 = android.text.TextUtils.isEmpty(r0)
            if (r0 != 0) goto L_0x00c2
            com.sec.internal.ims.servicemodules.ss.UtStateMachine r0 = r7.mUsm
            com.sec.internal.helper.httpclient.HttpResponseParams r1 = r7.mResponseData
            int r1 = r1.getStatusCode()
            com.sec.internal.helper.httpclient.HttpResponseParams r7 = r7.mResponseData
            java.lang.String r7 = r7.getDataString()
            r3 = 0
            r0.sendMessage(r2, r1, r3, r7)
            goto L_0x00cd
        L_0x00c2:
            com.sec.internal.ims.servicemodules.ss.UtStateMachine r0 = r7.mUsm
            com.sec.internal.helper.httpclient.HttpResponseParams r7 = r7.mResponseData
            int r7 = r7.getStatusCode()
            r0.sendMessage((int) r2, (int) r7)
        L_0x00cd:
            return
        L_0x00ce:
            com.sec.internal.ims.servicemodules.ss.UtStateMachine r0 = r7.mUsm
            com.sec.internal.ims.servicemodules.ss.UtProfile r0 = r0.mProfile
            int r0 = r0.type
            boolean r0 = com.sec.internal.ims.servicemodules.ss.UtUtils.isPutRequest(r0)
            if (r0 == 0) goto L_0x00de
            r7.responsePutResult()
            goto L_0x0108
        L_0x00de:
            com.sec.internal.helper.httpclient.HttpResponseParams r0 = r7.mResponseData
            java.lang.String r0 = r0.getDataString()
            boolean r0 = android.text.TextUtils.isEmpty(r0)
            if (r0 != 0) goto L_0x00ee
            r7.responseGetResult()
            goto L_0x0108
        L_0x00ee:
            com.sec.internal.ims.servicemodules.ss.UtStateMachine r0 = r7.mUsm
            com.sec.internal.ims.servicemodules.ss.UtProfile r1 = r0.mProfile
            int r1 = r1.type
            if (r1 != r3) goto L_0x00fd
            boolean r0 = r0.mIsGetSdBy404
            if (r0 == 0) goto L_0x00fd
            r7.recoverUtProfileAfter404Retry()
        L_0x00fd:
            com.sec.internal.ims.servicemodules.ss.UtStateMachine r0 = r7.mUsm
            com.sec.internal.helper.httpclient.HttpResponseParams r7 = r7.mResponseData
            int r7 = r7.getStatusCode()
            r0.sendMessage((int) r2, (int) r7)
        L_0x0108:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.ss.ResponseState.handleHttpResult():void");
    }

    private void responsePutResult() {
        UtStateMachine utStateMachine = this.mUsm;
        UtProfile utProfile = utStateMachine.mProfile;
        int i = utProfile.type;
        if (i == 109) {
            if (SimUtil.getMno(utStateMachine.mPhoneId).isOneOf(Mno.VINAPHONE)) {
                UtStateMachine utStateMachine2 = this.mUsm;
                utStateMachine2.setUserSet(utStateMachine2.mPhoneId, "ss_clir_pref", utStateMachine2.mProfile.condition);
            }
        } else if (i == 101 && utProfile.action == 3) {
            utStateMachine.mPreviousCFCache.copyRule(utStateMachine.mCFCache.getRule(utProfile.condition, UtUtils.convertToMedia(utProfile.serviceClass)));
        } else if (i == 105 && utStateMachine.mIsGetForAllCb) {
            utStateMachine.mIsGetForAllCb = false;
            utStateMachine.mPrevGetType = -1;
            utProfile.type = 103;
            utProfile.condition = 9;
            utStateMachine.transitionTo(utStateMachine.mRequestState);
            this.mUsm.sendMessage(1);
            return;
        }
        UtStateMachine utStateMachine3 = this.mUsm;
        if (utStateMachine3.mSeparatedCFNRY && !utStateMachine3.mSeparatedMedia) {
            utStateMachine3.transitionTo(utStateMachine3.mRequestState);
            this.mUsm.sendMessage(7);
            this.mUsm.mSeparatedCFNRY = false;
        } else if (utStateMachine3.mSeparatedCFNL) {
            utStateMachine3.transitionTo(utStateMachine3.mRequestState);
            this.mUsm.sendMessage(6);
            this.mUsm.mSeparatedCFNL = false;
        } else {
            if (utStateMachine3.mSeparatedCfAll) {
                String str = UtStateMachine.LOG_TAG;
                int i2 = utStateMachine3.mPhoneId;
                IMSLog.i(str, i2, "mUsm.mProfile.condition : " + this.mUsm.mProfile.condition);
                UtStateMachine utStateMachine4 = this.mUsm;
                int i3 = utStateMachine4.mProfile.condition;
                if (i3 == 3 || i3 == 6) {
                    utStateMachine4.mSeparatedCfAll = false;
                } else {
                    utStateMachine4.removeMessages(15);
                    this.mUsm.mThisSm.sendMessageDelayed(15, 1017, 32500);
                    UtStateMachine utStateMachine5 = this.mUsm;
                    UtProfile utProfile2 = utStateMachine5.mProfile;
                    if (utProfile2.condition == 7) {
                        utProfile2.condition = 2;
                    }
                    utStateMachine5.transitionTo(utStateMachine5.mRequestState);
                    this.mUsm.sendMessage(8);
                    return;
                }
            }
            UtStateMachine utStateMachine6 = this.mUsm;
            if (utStateMachine6.mSeparatedMedia) {
                utStateMachine6.transitionTo(utStateMachine6.mRequestState);
                this.mUsm.sendMessage(9);
                this.mUsm.mSeparatedMedia = false;
                return;
            }
            if (utStateMachine6.mFeature.isNeedFirstGet) {
                utStateMachine6.clearCachedSsData(-1);
            }
            UtStateMachine utStateMachine7 = this.mUsm;
            utStateMachine7.mCount412RetryDone = 0;
            utStateMachine7.completeUtRequest();
        }
    }

    private void cfInfoFromCache() {
        ArrayList arrayList = new ArrayList();
        UtStateMachine utStateMachine = this.mUsm;
        utStateMachine.isGetBeforePut = false;
        int i = utStateMachine.mProfile.condition;
        if (i == 4 || i == 5) {
            cfAllInfoFromCache(arrayList, (CallForwardingData.Rule) null);
            if (arrayList.isEmpty()) {
                IMSLog.i(UtStateMachine.LOG_TAG, this.mUsm.mPhoneId, "There is no matched rule for CF ALL.");
                Bundle bundle = new Bundle();
                bundle.putInt("status", 0);
                bundle.putInt(UtConstant.SERVICECLASS, this.mUsm.mProfile.serviceClass);
                bundle.putInt(UtConstant.CONDITION, this.mUsm.mProfile.condition);
                this.mUsm.completeUtRequest(bundle);
                return;
            }
        } else {
            if (utStateMachine.mFeature.support_media) {
                for (MEDIA media : MEDIA.values()) {
                    if (media != MEDIA.ALL) {
                        UtStateMachine utStateMachine2 = this.mUsm;
                        CallForwardingData.Rule rule = utStateMachine2.mCFCache.getRule(utStateMachine2.mProfile.condition, media);
                        IMSLog.i(UtStateMachine.LOG_TAG, this.mUsm.mPhoneId, "GET RULE ID [" + media + "] " + rule.ruleId);
                        if (!TextUtils.isEmpty(rule.ruleId)) {
                            arrayList.add(makeCFBundle(rule));
                        }
                    }
                }
            } else {
                CallForwardingData callForwardingData = utStateMachine.mCFCache;
                MEDIA media2 = MEDIA.ALL;
                CallForwardingData.Rule rule2 = callForwardingData.getRule(i, media2);
                IMSLog.i(UtStateMachine.LOG_TAG, this.mUsm.mPhoneId, "GET RULE ID [" + media2 + "] " + rule2.ruleId);
                if (!TextUtils.isEmpty(rule2.ruleId)) {
                    arrayList.add(makeCFBundle(rule2));
                }
                if (arrayList.isEmpty()) {
                    for (MEDIA media3 : MEDIA.values()) {
                        if (media3 != MEDIA.ALL) {
                            UtStateMachine utStateMachine3 = this.mUsm;
                            CallForwardingData.Rule rule3 = utStateMachine3.mCFCache.getRule(utStateMachine3.mProfile.condition, media3);
                            IMSLog.i(UtStateMachine.LOG_TAG, this.mUsm.mPhoneId, "GET RULE ID [" + media3 + "] " + rule3.ruleId);
                            if (!TextUtils.isEmpty(rule3.ruleId)) {
                                arrayList.add(makeCFBundle(rule3));
                            }
                        }
                    }
                }
            }
            if (arrayList.isEmpty()) {
                UtStateMachine utStateMachine4 = this.mUsm;
                CallForwardingData callForwardingData2 = utStateMachine4.mCFCache;
                int i2 = utStateMachine4.mProfile.condition;
                MEDIA media4 = MEDIA.ALL;
                CallForwardingData.Rule rule4 = callForwardingData2.getRule(i2, media4);
                IMSLog.i(UtStateMachine.LOG_TAG, this.mUsm.mPhoneId, "GET RULE ID [" + media4 + "] " + rule4.ruleId);
                arrayList.add(makeCFBundle(rule4));
            }
        }
        UtStateMachine utStateMachine5 = this.mUsm;
        utStateMachine5.mHasCFCache = true;
        utStateMachine5.removeMessages(5);
        this.mUsm.sendMessageDelayed(5, 1000);
        this.mUsm.completeUtRequest((Bundle[]) arrayList.toArray(new Bundle[0]));
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x006c, code lost:
        r3 = false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void cfAllInfoFromCache(java.util.List<android.os.Bundle> r17, com.sec.internal.ims.servicemodules.ss.CallForwardingData.Rule r18) {
        /*
            r16 = this;
            r0 = r16
            com.sec.internal.ims.servicemodules.ss.UtStateMachine r1 = r0.mUsm
            com.sec.internal.ims.servicemodules.ss.UtProfile r1 = r1.mProfile
            int r1 = r1.condition
            r2 = 5
            r4 = 0
            if (r1 != r2) goto L_0x000e
            r1 = 1
            goto L_0x000f
        L_0x000e:
            r1 = r4
        L_0x000f:
            com.sec.internal.ims.servicemodules.ss.MEDIA[] r2 = com.sec.internal.ims.servicemodules.ss.MEDIA.values()
            int r5 = r2.length
            r6 = r18
            r7 = r4
        L_0x0017:
            if (r7 >= r5) goto L_0x00b3
            r8 = r2[r7]
            java.lang.String r9 = com.sec.internal.ims.servicemodules.ss.UtStateMachine.LOG_TAG
            com.sec.internal.ims.servicemodules.ss.UtStateMachine r10 = r0.mUsm
            int r10 = r10.mPhoneId
            java.lang.StringBuilder r11 = new java.lang.StringBuilder
            r11.<init>()
            java.lang.String r12 = "MEDIA = "
            r11.append(r12)
            r11.append(r8)
            java.lang.String r11 = r11.toString()
            com.sec.internal.log.IMSLog.i(r9, r10, r11)
            r9 = -1
            r10 = 0
            r11 = r1
            r12 = r9
        L_0x0039:
            r13 = 4
            if (r11 >= r13) goto L_0x0095
            com.sec.internal.ims.servicemodules.ss.UtStateMachine r6 = r0.mUsm
            com.sec.internal.ims.servicemodules.ss.CallForwardingData r6 = r6.mCFCache
            com.sec.internal.ims.servicemodules.ss.CallForwardingData$Rule r6 = r6.getRule((int) r11, (com.sec.internal.ims.servicemodules.ss.MEDIA) r8)
            java.lang.String r13 = com.sec.internal.ims.servicemodules.ss.UtStateMachine.LOG_TAG
            com.sec.internal.ims.servicemodules.ss.UtStateMachine r14 = r0.mUsm
            int r14 = r14.mPhoneId
            java.lang.StringBuilder r15 = new java.lang.StringBuilder
            r15.<init>()
            java.lang.String r3 = "GET RULE ID "
            r15.append(r3)
            r15.append(r11)
            java.lang.String r3 = " = "
            r15.append(r3)
            java.lang.String r3 = r6.ruleId
            r15.append(r3)
            java.lang.String r3 = r15.toString()
            com.sec.internal.log.IMSLog.i(r13, r14, r3)
            java.lang.String r3 = r6.ruleId
            if (r3 != 0) goto L_0x006e
        L_0x006c:
            r3 = r4
            goto L_0x0096
        L_0x006e:
            if (r12 != r9) goto L_0x0075
            com.sec.internal.ims.servicemodules.ss.Condition r3 = r6.conditions
            boolean r12 = r3.state
            goto L_0x007c
        L_0x0075:
            com.sec.internal.ims.servicemodules.ss.Condition r3 = r6.conditions
            boolean r3 = r3.state
            if (r12 == r3) goto L_0x007c
            goto L_0x006c
        L_0x007c:
            if (r12 != 0) goto L_0x007f
            goto L_0x0092
        L_0x007f:
            if (r10 != 0) goto L_0x0087
            com.sec.internal.ims.servicemodules.ss.ForwardTo r3 = r6.fwdElm
            java.lang.String r3 = r3.target
            r10 = r3
            goto L_0x0092
        L_0x0087:
            com.sec.internal.ims.servicemodules.ss.ForwardTo r3 = r6.fwdElm
            java.lang.String r3 = r3.target
            boolean r3 = r10.equals(r3)
            if (r3 != 0) goto L_0x0092
            goto L_0x006c
        L_0x0092:
            int r11 = r11 + 1
            goto L_0x0039
        L_0x0095:
            r3 = 1
        L_0x0096:
            if (r3 == 0) goto L_0x00ad
            java.lang.String r3 = com.sec.internal.ims.servicemodules.ss.UtStateMachine.LOG_TAG
            com.sec.internal.ims.servicemodules.ss.UtStateMachine r8 = r0.mUsm
            int r8 = r8.mPhoneId
            java.lang.String r9 = "This target number is valid for CF ALL."
            com.sec.internal.log.IMSLog.i(r3, r8, r9)
            android.os.Bundle r3 = r0.makeCFBundle(r6)
            r8 = r17
            r8.add(r3)
            goto L_0x00af
        L_0x00ad:
            r8 = r17
        L_0x00af:
            int r7 = r7 + 1
            goto L_0x0017
        L_0x00b3:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.ss.ResponseState.cfAllInfoFromCache(java.util.List, com.sec.internal.ims.servicemodules.ss.CallForwardingData$Rule):void");
    }

    private void responseGetFromCache() {
        CallBarringData callBarringData;
        int i = this.mUsm.mProfile.type;
        if (i == 100) {
            cfInfoFromCache();
        } else if (i == 102 || i == 104) {
            List arrayList = new ArrayList();
            UtStateMachine utStateMachine = this.mUsm;
            UtProfile utProfile = utStateMachine.mProfile;
            if (utProfile.type == 104) {
                callBarringData = utStateMachine.mOCBCache;
            } else {
                callBarringData = utStateMachine.mICBCache;
            }
            UtFeatureData utFeatureData = utStateMachine.mFeature;
            if (!utFeatureData.support_media || utFeatureData.noMediaForCB) {
                int i2 = utProfile.condition;
                MEDIA media = MEDIA.ALL;
                CallBarringData.Rule rule = callBarringData.getRule(i2, media);
                IMSLog.i(UtStateMachine.LOG_TAG, this.mUsm.mPhoneId, "GET RULE ID [" + media + "] " + rule.ruleId);
                if (this.mUsm.mProfile.condition == 10) {
                    arrayList = createRuleId(callBarringData);
                } else if (!TextUtils.isEmpty(rule.ruleId)) {
                    arrayList.add(makeCBBundle(rule));
                }
                if (arrayList.isEmpty()) {
                    for (MEDIA media2 : MEDIA.values()) {
                        if (media2 != MEDIA.ALL) {
                            CallBarringData.Rule rule2 = callBarringData.getRule(this.mUsm.mProfile.condition, media2);
                            IMSLog.i(UtStateMachine.LOG_TAG, this.mUsm.mPhoneId, "GET RULE ID [" + media2 + "] " + rule2.ruleId);
                            if (!TextUtils.isEmpty(rule2.ruleId)) {
                                arrayList.add(makeCBBundle(rule2));
                            }
                        }
                    }
                }
            } else {
                for (MEDIA media3 : MEDIA.values()) {
                    if (media3 != MEDIA.ALL) {
                        CallBarringData.Rule rule3 = callBarringData.getRule(this.mUsm.mProfile.condition, media3);
                        IMSLog.i(UtStateMachine.LOG_TAG, this.mUsm.mPhoneId, "GET RULE ID [" + media3 + "] " + rule3.ruleId);
                        if (!TextUtils.isEmpty(rule3.ruleId)) {
                            arrayList.add(makeCBBundle(rule3));
                        }
                    }
                }
                if (arrayList.isEmpty()) {
                    int i3 = this.mUsm.mProfile.condition;
                    MEDIA media4 = MEDIA.ALL;
                    CallBarringData.Rule rule4 = callBarringData.getRule(i3, media4);
                    IMSLog.i(LOG_TAG, this.mUsm.mPhoneId, "GET RULE ID [" + media4 + "] " + rule4.ruleId);
                    if (!TextUtils.isEmpty(rule4.ruleId)) {
                        arrayList.add(makeCBBundle(rule4));
                    }
                }
            }
            UtStateMachine utStateMachine2 = this.mUsm;
            if (utStateMachine2.mProfile.type == 104) {
                utStateMachine2.mHasOCBCache = true;
            } else {
                utStateMachine2.mHasICBCache = true;
            }
            utStateMachine2.removeMessages(5);
            this.mUsm.sendMessageDelayed(5, 1000);
            this.mUsm.completeUtRequest((Bundle[]) arrayList.toArray(new Bundle[0]));
        }
    }

    public void responseGetResult() {
        UtXmlParse utXmlParse = new UtXmlParse();
        String str = UtStateMachine.LOG_TAG;
        int i = this.mUsm.mPhoneId;
        UtLog.i(str, i, "Print GET Result" + IMSLog.numberChecker(this.mResponseData.getDataString()));
        UtStateMachine utStateMachine = this.mUsm;
        UtProfile utProfile = utStateMachine.mProfile;
        int i2 = utProfile.type;
        if (i2 == 100) {
            utStateMachine.mCFCache = utXmlParse.parseCallForwarding(this.mResponseData.getDataString(), SimUtil.getMno(this.mUsm.mPhoneId));
            UtStateMachine utStateMachine2 = this.mUsm;
            if (utStateMachine2.isGetBeforePut) {
                utStateMachine2.mProfile.type = 101;
                utStateMachine2.isGetBeforePut = false;
                utStateMachine2.mHasCFCache = false;
                sendHttp();
                return;
            }
            responseGetFromCache();
        } else if (i2 == 102 || i2 == 104) {
            CallBarringData parseCallBarring = utXmlParse.parseCallBarring(this.mResponseData.getDataString());
            UtStateMachine utStateMachine3 = this.mUsm;
            UtProfile utProfile2 = utStateMachine3.mProfile;
            if (utProfile2.type == 104) {
                utStateMachine3.mOCBCache = parseCallBarring;
                if (utStateMachine3.isGetBeforePut) {
                    utProfile2.type = 105;
                    utStateMachine3.isGetBeforePut = false;
                    utStateMachine3.mHasOCBCache = false;
                    sendHttp();
                    return;
                }
            } else {
                utStateMachine3.mICBCache = parseCallBarring;
                if (utStateMachine3.isGetBeforePut) {
                    utProfile2.type = 103;
                    utStateMachine3.isGetBeforePut = false;
                    utStateMachine3.mHasICBCache = false;
                    sendHttp();
                    return;
                }
            }
            responseGetFromCache();
        } else if (i2 == 106) {
            ImsSsInfo build = new ImsSsInfo.Builder(utXmlParse.parseCallWaitingOrClip(this.mResponseData.getDataString()) ? 1 : 0).setIncomingCommunicationBarringNumber("").build();
            Bundle bundle = new Bundle();
            bundle.putParcelable(UtConstant.IMSSSINFO, build);
            this.mUsm.completeUtRequest(bundle);
        } else if (i2 == 108) {
            int[] iArr = {utXmlParse.parseClir(this.mResponseData.getDataString()), 4};
            if (SimUtil.getMno(this.mUsm.mPhoneId).isOneOf(Mno.VINAPHONE) && iArr[0] != 1) {
                UtStateMachine utStateMachine4 = this.mUsm;
                iArr[0] = utStateMachine4.getUserSetToInt(utStateMachine4.mPhoneId, "ss_clir_pref", 0);
            }
            Bundle bundle2 = new Bundle();
            bundle2.putIntArray(UtConstant.QUERYCLIR, iArr);
            this.mUsm.completeUtRequest(bundle2);
        } else if (i2 != 114) {
            if (i2 == 116 && !handleResponseSd()) {
                this.mUsm.completeUtRequest();
            }
        } else if (utStateMachine.mIsGetAfter412) {
            utProfile.type = 115;
            utStateMachine.mIsGetAfter412 = false;
            sendHttp();
        } else {
            this.mUsm.completeUtRequest(utXmlParse.parseCallWaitingOrClip(this.mResponseData.getDataString()));
        }
    }

    private boolean handleResponseSd() {
        UtStateMachine utStateMachine = this.mUsm;
        int i = utStateMachine.mPrevGetType;
        if (i == -1) {
            return false;
        }
        if (utStateMachine.isGetBeforePut) {
            utStateMachine.isGetBeforePut = false;
            if (i == 104) {
                utStateMachine.mProfile.type = 105;
                utStateMachine.mHasOCBCache = false;
                sendHttp();
                return true;
            } else if (i == 102) {
                utStateMachine.mProfile.type = 103;
                utStateMachine.mHasICBCache = false;
                sendHttp();
                return true;
            } else if (i == 100) {
                utStateMachine.mProfile.type = 101;
                utStateMachine.mHasCFCache = false;
                sendHttp();
                return true;
            } else {
                String str = UtStateMachine.LOG_TAG;
                int i2 = utStateMachine.mPhoneId;
                IMSLog.i(str, i2, "Unknown access. mUsm.mPrevGetType: " + UtLog.getStringRequestType(this.mUsm.mPrevGetType));
                completeGetSdByRetry();
                return true;
            }
        } else if (utStateMachine.mIsGetSdBy404) {
            completeGetSdByRetry();
            return true;
        } else {
            String str2 = UtStateMachine.LOG_TAG;
            int i3 = utStateMachine.mPhoneId;
            IMSLog.i(str2, i3, "Unknown access. mUsm.mPrevGetType: " + UtLog.getStringRequestType(this.mUsm.mPrevGetType));
            completeGetSdByRetry();
            return true;
        }
    }

    private void sendHttp() {
        UtStateMachine utStateMachine = this.mUsm;
        utStateMachine.transitionTo(utStateMachine.mRequestState);
        this.mUsm.sendMessage(1);
    }

    private void completeGetSdByRetry() {
        recoverUtProfileAfter404Retry();
        UtStateMachine utStateMachine = this.mUsm;
        int i = utStateMachine.mProfile.type;
        if (i == 100 || i == 102 || i == 104) {
            Bundle bundle = new Bundle();
            bundle.putInt("status", 0);
            bundle.putInt(UtConstant.SERVICECLASS, this.mUsm.mProfile.serviceClass);
            bundle.putInt(UtConstant.CONDITION, this.mUsm.mProfile.condition);
            this.mUsm.completeUtRequest(bundle);
        } else if (i == 106) {
            Bundle bundle2 = new Bundle();
            bundle2.putParcelable(UtConstant.IMSSSINFO, new ImsSsInfo.Builder(0).setIncomingCommunicationBarringNumber("").build());
            this.mUsm.completeUtRequest(bundle2);
        } else if (i == 108) {
            Bundle bundle3 = new Bundle();
            bundle3.putIntArray(UtConstant.QUERYCLIR, new int[]{0, 4});
            this.mUsm.completeUtRequest(bundle3);
        } else if (i != 114) {
            utStateMachine.completeUtRequest();
        } else {
            utStateMachine.completeUtRequest(true);
        }
    }

    private void recoverUtProfileAfter404Retry() {
        UtStateMachine utStateMachine = this.mUsm;
        utStateMachine.mProfile.type = utStateMachine.mPrevGetType;
        utStateMachine.mPrevGetType = -1;
        utStateMachine.mIsGetSdBy404 = false;
    }

    private List<Bundle> createRuleId(CallBarringData callBarringData) {
        ArrayList arrayList = new ArrayList();
        Iterator<SsRuleData.SsRule> it = callBarringData.rules.iterator();
        while (it.hasNext()) {
            CallBarringData.Rule rule = (CallBarringData.Rule) it.next();
            if (rule.conditions.condition == 10 && rule.ruleId.contains("DBL")) {
                Bundle bundle = new Bundle();
                StringBuilder sb = new StringBuilder();
                boolean z = false;
                for (String next : rule.target) {
                    if (z) {
                        sb.append("$");
                    }
                    sb.append(next);
                    z = true;
                }
                bundle.putString("number", rule.ruleId + "," + sb.toString());
                if (rule.conditions.state) {
                    bundle.putInt("status", 1);
                } else {
                    bundle.putInt("status", 0);
                }
                bundle.putInt(UtConstant.CONDITION, rule.conditions.condition);
                arrayList.add(bundle);
            }
        }
        return arrayList;
    }

    private Bundle makeCFBundle(CallForwardingData.Rule rule) {
        int i;
        Mno simMno = SimUtil.getSimMno(this.mUsm.mPhoneId);
        Bundle bundle = new Bundle();
        if (!rule.conditions.state || TextUtils.isEmpty(rule.fwdElm.target)) {
            bundle.putInt("status", 0);
        } else {
            bundle.putInt("status", 1);
        }
        bundle.putInt(UtConstant.CONDITION, this.mUsm.mProfile.condition);
        if (!TextUtils.isEmpty(rule.fwdElm.target)) {
            if ("+".contains(rule.fwdElm.target)) {
                bundle.putInt(UtConstant.TOA, 145);
            } else {
                bundle.putInt(UtConstant.TOA, 129);
            }
            String numberFromURI = UtUtils.getNumberFromURI(rule.fwdElm.target);
            if ((simMno == Mno.SINGTEL || simMno == Mno.VODAFONE_QATAR) && numberFromURI.charAt(0) != '+') {
                numberFromURI = UtUtils.makeInternationalNumber(numberFromURI, simMno);
            }
            if (simMno == Mno.KDDI && numberFromURI.charAt(0) == '+') {
                numberFromURI = UtUtils.removeUriPlusPrefix(numberFromURI, "+81");
            }
            bundle.putString("number", numberFromURI);
        }
        int doconvertMediaTypeToSsClass = UtUtils.doconvertMediaTypeToSsClass(rule.conditions.media);
        if (simMno == Mno.ATT && doconvertMediaTypeToSsClass == 255) {
            bundle.putInt(UtConstant.SERVICECLASS, 1);
        } else if ((simMno == Mno.VODAFONE_SPAIN || simMno == Mno.SMARTONE || simMno == Mno.YOIGO_SPAIN) && doconvertMediaTypeToSsClass == 255) {
            bundle.putInt(UtConstant.SERVICECLASS, 49);
        } else {
            bundle.putInt(UtConstant.SERVICECLASS, doconvertMediaTypeToSsClass);
        }
        CallForwardingData callForwardingData = this.mUsm.mCFCache;
        if (!(callForwardingData == null || (i = callForwardingData.replyTimer) == 0)) {
            bundle.putInt("NoReplyTimer", i);
        }
        return bundle;
    }

    private Bundle makeCBBundle(CallBarringData.Rule rule) {
        Bundle bundle = new Bundle();
        if (rule.conditions.state) {
            bundle.putInt("status", 1);
        } else {
            bundle.putInt("status", 0);
        }
        bundle.putInt(UtConstant.CONDITION, rule.conditions.condition);
        bundle.putInt(UtConstant.SERVICECLASS, UtUtils.doconvertMediaTypeToSsClass(rule.conditions.media));
        return bundle;
    }
}
