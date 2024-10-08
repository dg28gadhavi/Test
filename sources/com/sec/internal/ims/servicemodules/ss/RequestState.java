package com.sec.internal.ims.servicemodules.ss;

import android.os.Message;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.State;
import com.sec.internal.ims.util.httpclient.GbaHttpController;
import com.sec.internal.log.IMSLog;

public class RequestState extends State {
    public static final String LOG_TAG = UtStateMachine.LOG_TAG;
    private UtStateMachine mUsm;

    public void enter() {
    }

    public RequestState(UtStateMachine utStateMachine) {
        this.mUsm = utStateMachine;
    }

    private void requestPdn() {
        UtStateMachine utStateMachine = this.mUsm;
        utStateMachine.mPdnRetryCounter = 0;
        utStateMachine.removeMessages(2);
        String setting = UtUtils.getSetting(this.mUsm.mPhoneId, GlobalSettingsConstants.SS.DOMAIN, "PS");
        if (DiagnosisConstants.PSCI_KEY_CALL_BEARER.equalsIgnoreCase(setting) || "PS_ONLY_VOLTEREGIED".equalsIgnoreCase(setting)) {
            UtStateMachine utStateMachine2 = this.mUsm;
            if (!utStateMachine2.mUtServiceModule.isVolteServiceRegistered(utStateMachine2.mPhoneId)) {
                IMSLog.i(UtStateMachine.LOG_TAG, this.mUsm.mPhoneId, "IMS is not registered, UT request must fail ");
                this.mUsm.sendMessage(12, 1013);
                return;
            }
        }
        if (!this.mUsm.hasConnection()) {
            UtStateMachine utStateMachine3 = this.mUsm;
            int startPdnConnectivity = utStateMachine3.mPdnController.startPdnConnectivity(utStateMachine3.mPdnType, utStateMachine3.mPdnListener, utStateMachine3.mPhoneId);
            if (startPdnConnectivity != 1) {
                String str = UtStateMachine.LOG_TAG;
                int i = this.mUsm.mPhoneId;
                IMSLog.i(str, i, "startPDN fails " + startPdnConnectivity);
                this.mUsm.sendMessage(12, 1014);
                return;
            }
            return;
        }
        this.mUsm.sendMessage(1);
    }

    private void processGetRequest() {
        GbaHttpController.getInstance().execute(this.mUsm.makeHttpParams());
    }

    private void processPutRequest() {
        int i;
        UtStateMachine utStateMachine = this.mUsm;
        utStateMachine.mPrevGetType = -1;
        UtProfile utProfile = utStateMachine.mProfile;
        if (utProfile.type == 101) {
            UtFeatureData utFeatureData = utStateMachine.mFeature;
            if (utFeatureData.setAllMediaCF && utFeatureData.support_media && utFeatureData.isCFSingleElement && UtUtils.convertToMedia(utProfile.serviceClass) == MEDIA.ALL) {
                IMSLog.i(UtStateMachine.LOG_TAG, "Separated requests for media, send requests for audio and video conditions");
                UtStateMachine utStateMachine2 = this.mUsm;
                utStateMachine2.mSeparatedMedia = true;
                utStateMachine2.mMainCondition = utStateMachine2.mProfile.condition;
            }
            UtStateMachine utStateMachine3 = this.mUsm;
            UtFeatureData utFeatureData2 = utStateMachine3.mFeature;
            boolean z = utFeatureData2.isCFSingleElement;
            if (z) {
                UtProfile utProfile2 = utStateMachine3.mProfile;
                if (utProfile2.condition == 2 && utFeatureData2.isNeedSeparateCFNRY && utProfile2.timeSeconds > 0 && ((i = utProfile2.action) == 1 || i == 3)) {
                    IMSLog.i(UtStateMachine.LOG_TAG, utStateMachine3.mPhoneId, "SeparatedRequest CFNRY");
                    this.mUsm.mSeparatedCFNRY = true;
                }
            }
            UtProfile utProfile3 = utStateMachine3.mProfile;
            int i2 = utProfile3.condition;
            if (i2 == 3 && utFeatureData2.isNeedSeparateCFNL) {
                IMSLog.i(UtStateMachine.LOG_TAG, utStateMachine3.mPhoneId, "SeparatedRequest CFNL");
                this.mUsm.mSeparatedCFNL = true;
            } else if (z && utFeatureData2.isNeedSeparateCFA && (i2 == 4 || i2 == 5)) {
                utProfile3.condition = i2 == 4 ? 0 : 1;
                utStateMachine3.mSeparatedCfAll = true;
                String str = UtStateMachine.LOG_TAG;
                int i3 = utStateMachine3.mPhoneId;
                IMSLog.i(str, i3, "SeparatedRequest CF ALL - start from " + this.mUsm.mProfile.condition);
            }
        }
        GbaHttpController.getInstance().execute(this.mUsm.makeHttpParams());
    }

    /* JADX WARNING: Code restructure failed: missing block: B:3:0x0012, code lost:
        if (r0.mPdnController.isNetworkRequested(r0.mPdnListener) != false) goto L_0x0014;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void initPdnInfo() {
        /*
            r4 = this;
            com.sec.internal.ims.servicemodules.ss.UtStateMachine r0 = r4.mUsm
            boolean r0 = r0.hasConnection()
            if (r0 != 0) goto L_0x0014
            com.sec.internal.ims.servicemodules.ss.UtStateMachine r0 = r4.mUsm
            com.sec.internal.interfaces.ims.core.IPdnController r1 = r0.mPdnController
            com.sec.internal.interfaces.ims.core.PdnEventListener r0 = r0.mPdnListener
            boolean r0 = r1.isNetworkRequested(r0)
            if (r0 == 0) goto L_0x0021
        L_0x0014:
            com.sec.internal.ims.servicemodules.ss.UtStateMachine r0 = r4.mUsm
            com.sec.internal.interfaces.ims.core.IPdnController r1 = r0.mPdnController
            int r2 = r0.mPdnType
            int r3 = r0.mPhoneId
            com.sec.internal.interfaces.ims.core.PdnEventListener r0 = r0.mPdnListener
            r1.stopPdnConnectivity(r2, r3, r0)
        L_0x0021:
            com.sec.internal.ims.servicemodules.ss.UtStateMachine r4 = r4.mUsm
            r0 = -1
            r4.mPdnType = r0
            r0 = 0
            r4.mSocketFactory = r0
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.ss.RequestState.initPdnInfo():void");
    }

    public boolean processMessage(Message message) {
        UtLog.i(UtStateMachine.LOG_TAG, this.mUsm.mPhoneId, "RequestState::ProcessMessage " + UtLog.getStringMessage(message.what));
        int i = message.what;
        if (i != 12) {
            if (i == 100) {
                requestPdn();
            } else if (i != 14) {
                if (i != 15) {
                    switch (i) {
                        case 1:
                            processPdnConnected();
                            break;
                        case 2:
                            this.mUsm.disconnectPdn();
                            break;
                        case 3:
                            initPdnInfo();
                            break;
                        case 4:
                            this.mUsm.processTerminalRequest();
                            break;
                        case 5:
                            UtStateMachine utStateMachine = this.mUsm;
                            utStateMachine.mHasCFCache = false;
                            utStateMachine.mHasOCBCache = false;
                            utStateMachine.mHasICBCache = false;
                            break;
                        case 6:
                            this.mUsm.mProfile.condition = 6;
                            processPutRequest();
                            UtStateMachine utStateMachine2 = this.mUsm;
                            utStateMachine2.transitionTo(utStateMachine2.mResponseState);
                            break;
                        case 7:
                            this.mUsm.mProfile.condition = 7;
                            processPutRequest();
                            UtStateMachine utStateMachine3 = this.mUsm;
                            utStateMachine3.transitionTo(utStateMachine3.mResponseState);
                            break;
                        case 8:
                            UtProfile utProfile = this.mUsm.mProfile;
                            utProfile.condition++;
                            if (utProfile.action == 0) {
                                utProfile.number = "";
                            }
                            processPutRequest();
                            UtStateMachine utStateMachine4 = this.mUsm;
                            utStateMachine4.transitionTo(utStateMachine4.mResponseState);
                            break;
                        case 9:
                            UtStateMachine utStateMachine5 = this.mUsm;
                            UtProfile utProfile2 = utStateMachine5.mProfile;
                            utProfile2.serviceClass = 16;
                            utProfile2.condition = utStateMachine5.mMainCondition;
                            if (utProfile2.action == 0) {
                                utProfile2.number = "";
                            }
                            processPutRequest();
                            UtStateMachine utStateMachine6 = this.mUsm;
                            utStateMachine6.transitionTo(utStateMachine6.mResponseState);
                            break;
                    }
                }
            } else {
                return false;
            }
        }
        return !this.mUsm.hasProfile();
    }

    private void processPdnConnected() {
        if (this.mUsm.hasProfile()) {
            UtStateMachine utStateMachine = this.mUsm;
            utStateMachine.needPdnRequestForCW = false;
            utStateMachine.isRetryingCreatePdn = false;
            if (utStateMachine.mUtServiceModule.isTerminalRequest(utStateMachine.mPhoneId, utStateMachine.mProfile)) {
                this.mUsm.removeMessages(2);
                this.mUsm.sendMessage(2);
                this.mUsm.sendMessageDelayed(4, 100);
                return;
            }
            UtStateMachine utStateMachine2 = this.mUsm;
            if (utStateMachine2.mFeature.isNeedFirstGet && utStateMachine2.mPrevGetType == -1) {
                handleNeedFirstGet();
            }
            UtStateMachine utStateMachine3 = this.mUsm;
            if (utStateMachine3.mIsGetAfter412 && utStateMachine3.mProfile.type == 115) {
                IMSLog.i(LOG_TAG, utStateMachine3.mPhoneId, "Send GET after PUT error 412");
                this.mUsm.mProfile.type = 114;
            }
            if (!UtUtils.isPutRequest(this.mUsm.mProfile.type)) {
                UtStateMachine utStateMachine4 = this.mUsm;
                int i = utStateMachine4.mProfile.type;
                if ((i != 100 || !utStateMachine4.mHasCFCache) && ((i != 104 || !utStateMachine4.mHasOCBCache) && (i != 102 || !utStateMachine4.mHasICBCache))) {
                    processGetRequest();
                } else {
                    utStateMachine4.transitionTo(utStateMachine4.mResponseState);
                    this.mUsm.sendMessage(13);
                    return;
                }
            } else if (SimUtil.getSimMno(this.mUsm.mPhoneId) == Mno.WIND_GREECE && this.mUsm.isServiceDeactive()) {
                IMSLog.e(UtStateMachine.LOG_TAG, this.mUsm.mPhoneId, "Service is disabled on network side");
                this.mUsm.mThisSm.sendMessageDelayed(12, 1011, 100);
                return;
            } else if (this.mUsm.isPutRequestBlocked()) {
                this.mUsm.sendMessageDelayed(12, 1012, 100);
                return;
            } else {
                processPutRequest();
            }
            UtStateMachine utStateMachine5 = this.mUsm;
            utStateMachine5.transitionTo(utStateMachine5.mResponseState);
        }
    }

    private void handleNeedFirstGet() {
        UtStateMachine utStateMachine = this.mUsm;
        UtProfile utProfile = utStateMachine.mProfile;
        int i = utProfile.type;
        if (i == 101 && utStateMachine.mCFCache == null) {
            IMSLog.i(UtStateMachine.LOG_TAG, utStateMachine.mPhoneId, "Send GET before PUT due to no cache.");
            UtStateMachine utStateMachine2 = this.mUsm;
            utStateMachine2.isGetBeforePut = true;
            utStateMachine2.mProfile.type = 100;
        } else if (i == 103 && utStateMachine.mICBCache == null) {
            IMSLog.i(UtStateMachine.LOG_TAG, utStateMachine.mPhoneId, "Send GET before PUT due to no cache.");
            UtStateMachine utStateMachine3 = this.mUsm;
            utStateMachine3.isGetBeforePut = true;
            utStateMachine3.mProfile.type = 102;
        } else if (i == 105 && utStateMachine.mOCBCache == null) {
            IMSLog.i(UtStateMachine.LOG_TAG, utStateMachine.mPhoneId, "Send GET before PUT due to no cache.");
            UtStateMachine utStateMachine4 = this.mUsm;
            utStateMachine4.isGetBeforePut = true;
            utStateMachine4.mProfile.type = 104;
        } else if (i == 119) {
            utProfile.condition = 8;
            utStateMachine.mIsGetForAllCb = true;
            if (utStateMachine.mOCBCache == null) {
                IMSLog.i(UtStateMachine.LOG_TAG, utStateMachine.mPhoneId, "Send GET before PUT due to no cache.");
                UtStateMachine utStateMachine5 = this.mUsm;
                utStateMachine5.isGetBeforePut = true;
                utStateMachine5.mProfile.type = 104;
                return;
            }
            utProfile.type = 105;
        }
    }
}
