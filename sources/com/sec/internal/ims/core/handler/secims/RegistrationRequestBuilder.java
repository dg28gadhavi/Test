package com.sec.internal.ims.core.handler.secims;

import android.text.TextUtils;
import android.util.Log;
import com.google.flatbuffers.FlatBufferBuilder;
import com.sec.ims.options.Capabilities;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.config.RcsConfig;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ExtraHeader;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ReqMsg;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestDnsQuery;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestNetworkSuspended;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestRegistration;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestSendDmState;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestSetPreferredImpu;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestSetTextMode;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestUACreation;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestUACreation_.MediaConfig;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestUADeletion;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestUpdateAkaResp;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestUpdateCommonConfig;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestUpdateCommonConfig_.CallConfig;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestUpdateCommonConfig_.RegiConfig;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestUpdateCommonConfig_.ScreenConfig;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestUpdateCommonConfig_.ServiceVersionConfig;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestUpdateFeatureTag;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestUpdateSrvccVersion;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestUpdateVceConfig;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestUpdateXqEnable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RegistrationRequestBuilder {
    private static final String LOG_TAG = StackRequestBuilderUtil.class.getSimpleName();

    private RegistrationRequestBuilder() {
        throw new IllegalStateException("Utility class");
    }

    static StackRequest makeConfigSrvcc(int i, int i2) {
        FlatBufferBuilder flatBufferBuilder = new FlatBufferBuilder(0);
        RequestUpdateSrvccVersion.startRequestUpdateSrvccVersion(flatBufferBuilder);
        RequestUpdateSrvccVersion.addPhoneId(flatBufferBuilder, (long) i);
        RequestUpdateSrvccVersion.addVersion(flatBufferBuilder, (long) i2);
        int endRequestUpdateSrvccVersion = RequestUpdateSrvccVersion.endRequestUpdateSrvccVersion(flatBufferBuilder);
        Request.startRequest(flatBufferBuilder);
        Request.addReqid(flatBufferBuilder, 113);
        Request.addReqType(flatBufferBuilder, (byte) 10);
        Request.addReq(flatBufferBuilder, endRequestUpdateSrvccVersion);
        return new StackRequest(flatBufferBuilder, Request.endRequest(flatBufferBuilder));
    }

    static StackRequest makeSendDmState(int i, boolean z) {
        FlatBufferBuilder flatBufferBuilder = new FlatBufferBuilder(0);
        RequestSendDmState.startRequestSendDmState(flatBufferBuilder);
        RequestSendDmState.addPhoneId(flatBufferBuilder, (long) i);
        RequestSendDmState.addIsOn(flatBufferBuilder, z);
        int endRequestSendDmState = RequestSendDmState.endRequestSendDmState(flatBufferBuilder);
        Request.startRequest(flatBufferBuilder);
        Request.addReqid(flatBufferBuilder, 116);
        Request.addReqType(flatBufferBuilder, ReqMsg.request_send_dm_state);
        Request.addReq(flatBufferBuilder, endRequestSendDmState);
        return new StackRequest(flatBufferBuilder, Request.endRequest(flatBufferBuilder));
    }

    private static int addUaCreationReq(FlatBufferBuilder flatBufferBuilder, UaProfile uaProfile) {
        int i;
        int i2;
        int i3;
        int i4;
        int translateMno;
        FlatBufferBuilder flatBufferBuilder2 = flatBufferBuilder;
        UaProfile uaProfile2 = uaProfile;
        int addMediaParameters = addMediaParameters(flatBufferBuilder, uaProfile);
        int createString = flatBufferBuilder2.createString((CharSequence) uaProfile2.iface);
        int createString2 = flatBufferBuilder2.createString((CharSequence) uaProfile2.pdn);
        int createString3 = flatBufferBuilder2.createString((CharSequence) uaProfile2.impi);
        int createString4 = flatBufferBuilder2.createString((CharSequence) uaProfile2.impu);
        int createString5 = flatBufferBuilder2.createString((CharSequence) uaProfile2.domain);
        int createString6 = flatBufferBuilder2.createString((CharSequence) uaProfile2.transtype);
        int createString7 = flatBufferBuilder2.createString((CharSequence) "");
        int createString8 = flatBufferBuilder2.createString((CharSequence) "");
        int createString9 = flatBufferBuilder2.createString((CharSequence) uaProfile2.registeralgo);
        int createString10 = flatBufferBuilder2.createString((CharSequence) uaProfile2.impu);
        int createString11 = flatBufferBuilder2.createString((CharSequence) uaProfile.getRemoteuritype().toString());
        int createString12 = flatBufferBuilder2.createString((CharSequence) uaProfile2.userAgent);
        int createString13 = flatBufferBuilder2.createString((CharSequence) uaProfile2.instanceId);
        int i5 = addMediaParameters;
        int createString14 = flatBufferBuilder2.createString((CharSequence) uaProfile.getCurPani());
        int createString15 = flatBufferBuilder2.createString((CharSequence) uaProfile2.msrpTransType);
        int createString16 = flatBufferBuilder2.createString((CharSequence) uaProfile.getPrivacyHeaderRestricted());
        int createString17 = flatBufferBuilder2.createString((CharSequence) uaProfile.getLastPaniHeader());
        int createString18 = flatBufferBuilder2.createString((CharSequence) uaProfile.getOipFromPreferred());
        int createString19 = flatBufferBuilder2.createString((CharSequence) uaProfile.getSelectTransportAfterTcpReset());
        int createString20 = flatBufferBuilder2.createString((CharSequence) uaProfile2.mvno);
        int i6 = createString13;
        int createString21 = !TextUtils.isEmpty(uaProfile2.imsiBasedImpu) ? flatBufferBuilder2.createString((CharSequence) uaProfile2.imsiBasedImpu) : -1;
        int createString22 = !TextUtils.isEmpty(uaProfile2.hostname) ? flatBufferBuilder2.createString((CharSequence) uaProfile2.hostname) : -1;
        String str = uaProfile2.sessionRefresher;
        int createString23 = str != null ? flatBufferBuilder2.createString((CharSequence) str) : -1;
        if (uaProfile2.isipsec) {
            i2 = flatBufferBuilder2.createString((CharSequence) uaProfile2.authalg);
            i = flatBufferBuilder2.createString((CharSequence) uaProfile2.encralg);
        } else {
            i2 = -1;
            i = -1;
        }
        String str2 = uaProfile2.password;
        int createString24 = str2 != null ? flatBufferBuilder2.createString((CharSequence) str2) : -1;
        String str3 = uaProfile2.displayName;
        int createString25 = str3 != null ? flatBufferBuilder2.createString((CharSequence) str3) : -1;
        String str4 = uaProfile2.uuid;
        int createString26 = str4 != null ? flatBufferBuilder2.createString((CharSequence) str4) : -1;
        String str5 = uaProfile2.contactDisplayName;
        int createString27 = str5 != null ? flatBufferBuilder2.createString((CharSequence) str5) : -1;
        String str6 = uaProfile2.realm;
        int createString28 = str6 != null ? flatBufferBuilder2.createString((CharSequence) str6) : -1;
        String str7 = uaProfile2.imMsgTech;
        int createString29 = str7 != null ? flatBufferBuilder2.createString((CharSequence) str7) : -1;
        String str8 = uaProfile2.cmcRelayType;
        int createString30 = str8 != null ? flatBufferBuilder2.createString((CharSequence) str8) : -1;
        String str9 = uaProfile2.cmcEmergencyNumbers;
        int createString31 = str9 != null ? flatBufferBuilder2.createString((CharSequence) str9) : -1;
        Set<String> set = uaProfile2.serviceList;
        int i7 = createString21;
        int createServiceListVector = set != null ? RequestUACreation.createServiceListVector(flatBufferBuilder2, StackRequestBuilderUtil.getStringOffsetArray(flatBufferBuilder2, set, set.size())) : -1;
        if (uaProfile.getOwnCapabilities() != null) {
            List<Integer> translateFeatureTag = StackRequestBuilderUtil.translateFeatureTag(uaProfile.getOwnCapabilities().getFeature());
            i3 = createServiceListVector;
            int[] iArr = new int[translateFeatureTag.size()];
            int i8 = 0;
            for (Integer intValue : translateFeatureTag) {
                iArr[i8] = intValue.intValue();
                i8++;
            }
            i4 = RequestUACreation.createFeatureTagListVector(flatBufferBuilder2, iArr);
        } else {
            i3 = createServiceListVector;
            i4 = -1;
        }
        List<String> list = uaProfile2.acb;
        int i9 = i4;
        int createAcbVector = RequestUACreation.createAcbVector(flatBufferBuilder2, StackRequestBuilderUtil.getStringOffsetArray(flatBufferBuilder2, list, list.size()));
        List<String> list2 = uaProfile2.uacSipList;
        int i10 = createAcbVector;
        int createUacSipListVector = RequestUACreation.createUacSipListVector(flatBufferBuilder2, StackRequestBuilderUtil.getStringOffsetArray(flatBufferBuilder2, list2, list2.size()));
        RequestUACreation.startRequestUACreation(flatBufferBuilder);
        RequestUACreation.addProfileId(flatBufferBuilder2, (long) uaProfile.getProfileId());
        RequestUACreation.addInterfaceNw(flatBufferBuilder2, createString);
        RequestUACreation.addNetId(flatBufferBuilder2, uaProfile.getNetId());
        RequestUACreation.addPdn(flatBufferBuilder2, createString2);
        RequestUACreation.addImpi(flatBufferBuilder2, createString3);
        RequestUACreation.addImpu(flatBufferBuilder2, createString4);
        RequestUACreation.addDomain(flatBufferBuilder2, createString5);
        RequestUACreation.addIsSipOutbound(flatBufferBuilder2, uaProfile2.issipoutbound);
        RequestUACreation.addQParam(flatBufferBuilder2, (long) uaProfile2.qparam);
        RequestUACreation.addControlDscp(flatBufferBuilder2, (long) uaProfile.getControlDscp());
        RequestUACreation.addTransType(flatBufferBuilder2, createString6);
        RequestUACreation.addIsEmergencySupport(flatBufferBuilder2, uaProfile2.isEmergencyProfile);
        RequestUACreation.addIsIpsec(flatBufferBuilder2, uaProfile2.isipsec);
        RequestUACreation.addEncrAlg(flatBufferBuilder2, createString7);
        RequestUACreation.addAuthAlg(flatBufferBuilder2, createString8);
        RequestUACreation.addRegisterAlgo(flatBufferBuilder2, createString9);
        RequestUACreation.addPrefId(flatBufferBuilder2, createString10);
        RequestUACreation.addRemoteUriType(flatBufferBuilder2, createString11);
        RequestUACreation.addIsPrecondEnabled(flatBufferBuilder2, uaProfile2.isPrecondEnabled);
        RequestUACreation.addIsPrecondInitialSendrecv(flatBufferBuilder2, uaProfile2.isPrecondInitialSendrecv);
        RequestUACreation.addWifiPreConditionEnabled(flatBufferBuilder2, uaProfile2.mIsWifiPreConditionEnabled);
        RequestUACreation.addIsSipCompactHeader(flatBufferBuilder2, uaProfile2.mUseCompactHeader);
        RequestUACreation.addSessionExpires(flatBufferBuilder2, uaProfile2.sessionExpires);
        RequestUACreation.addMinse(flatBufferBuilder2, (long) uaProfile2.minSe);
        RequestUACreation.addUserAgent(flatBufferBuilder2, createString12);
        RequestUACreation.addInstanceId(flatBufferBuilder2, i6);
        RequestUACreation.addIsSoftphoneEnabled(flatBufferBuilder2, uaProfile2.isSoftphoneEnabled);
        RequestUACreation.addIsCdmalessEnabled(flatBufferBuilder2, uaProfile2.isCdmalessEnabled);
        RequestUACreation.addRingbackTimer(flatBufferBuilder2, (long) uaProfile2.ringbackTimer);
        RequestUACreation.addRingingTimer(flatBufferBuilder2, (long) uaProfile2.ringingTimer);
        RequestUACreation.addTimer1(flatBufferBuilder2, (long) uaProfile.getTimer1());
        RequestUACreation.addTimer2(flatBufferBuilder2, (long) uaProfile.getTimer2());
        RequestUACreation.addTimer4(flatBufferBuilder2, (long) uaProfile.getTimer4());
        RequestUACreation.addTimerA(flatBufferBuilder2, (long) uaProfile.getTimerA());
        RequestUACreation.addTimerD(flatBufferBuilder2, (long) uaProfile.getTimerD());
        RequestUACreation.addTimerB(flatBufferBuilder2, (long) uaProfile.getTimerB());
        RequestUACreation.addTimerC(flatBufferBuilder2, (long) uaProfile.getTimerC());
        RequestUACreation.addTimerE(flatBufferBuilder2, (long) uaProfile.getTimerE());
        RequestUACreation.addTimerF(flatBufferBuilder2, (long) uaProfile.getTimerF());
        RequestUACreation.addTimerG(flatBufferBuilder2, (long) uaProfile.getTimerG());
        RequestUACreation.addTimerH(flatBufferBuilder2, (long) uaProfile.getTimerH());
        RequestUACreation.addTimerI(flatBufferBuilder2, (long) uaProfile.getTimerI());
        RequestUACreation.addTimerJ(flatBufferBuilder2, (long) uaProfile.getTimerJ());
        RequestUACreation.addTimerK(flatBufferBuilder2, (long) uaProfile.getTimerK());
        RequestUACreation.addTimerTs(flatBufferBuilder2, (long) uaProfile.getTimerTS());
        RequestUACreation.addMssSize(flatBufferBuilder2, (long) uaProfile.getMssSize());
        RequestUACreation.addSipMobility(flatBufferBuilder2, (long) uaProfile.getSipMobility());
        RequestUACreation.addIsEnableGruu(flatBufferBuilder2, uaProfile.getIsEnableGruu());
        RequestUACreation.addIsEnableVcid(flatBufferBuilder2, uaProfile.getIsEnableVcid());
        RequestUACreation.addIsEnableSessionId(flatBufferBuilder2, uaProfile.getIsEnableSessionId());
        RequestUACreation.addAudioEngineType(flatBufferBuilder2, (long) uaProfile.getAudioEngineType());
        RequestUACreation.addTextMode(flatBufferBuilder2, (long) uaProfile.getTextMode());
        RequestUACreation.addCurPani(flatBufferBuilder2, createString14);
        RequestUACreation.addIsVceConfigEnabled(flatBufferBuilder2, uaProfile2.isVceConfigEnabled);
        RequestUACreation.addIsGcfConfigEnabled(flatBufferBuilder2, uaProfile2.isGcfConfigEnabled);
        RequestUACreation.addIsNsdsServiceEnabled(flatBufferBuilder2, uaProfile2.isNsdsServiceEnabled);
        RequestUACreation.addIsMsrpBearerUsed(flatBufferBuilder2, uaProfile2.isMsrpBearerUsed);
        RequestUACreation.addSubscriberTimer(flatBufferBuilder2, (long) uaProfile2.subscriberTimer);
        RequestUACreation.addIsSubscribeReg(flatBufferBuilder2, uaProfile2.isSubscribeReg);
        RequestUACreation.addUseKeepAlive(flatBufferBuilder2, uaProfile2.useKeepAlive);
        RequestUACreation.addSelfPort(flatBufferBuilder2, (long) uaProfile2.selfPort);
        RequestUACreation.addScmVersion(flatBufferBuilder2, (long) uaProfile2.scmVersion);
        RequestUACreation.addActiveDataPhoneId(flatBufferBuilder2, (long) uaProfile2.activeDataPhoneId);
        RequestUACreation.addMsrpTransType(flatBufferBuilder2, createString15);
        RequestUACreation.addIsFullCodecOfferRequired(flatBufferBuilder2, uaProfile2.isFullCodecOfferRequired);
        RequestUACreation.addIsRcsTelephonyFeatureTagRequired(flatBufferBuilder2, uaProfile2.isRcsTelephonyFeatureTagRequired);
        RequestUACreation.addIsXqEnabled(flatBufferBuilder2, uaProfile2.isXqEnabled);
        RequestUACreation.addRcsProfile(flatBufferBuilder2, uaProfile.getRcsProfile());
        RequestUACreation.addNeedTransportInContact(flatBufferBuilder2, uaProfile.getIsTransportNeeded());
        RequestUACreation.addRat(flatBufferBuilder2, (long) uaProfile.getRat());
        RequestUACreation.addDbrTimer(flatBufferBuilder2, (long) uaProfile.getDbrTimer());
        RequestUACreation.addIsTcpGracefulShutdownEnabled(flatBufferBuilder2, uaProfile.getIsTcpGracefulShutdownEnabled());
        RequestUACreation.addTcpRstUacErrorcode(flatBufferBuilder2, uaProfile.getTcpRstUacErrorcode());
        RequestUACreation.addTcpRstUasErrorcode(flatBufferBuilder2, uaProfile.getTcpRstUasErrorcode());
        RequestUACreation.addPrivacyHeaderRestricted(flatBufferBuilder2, createString16);
        RequestUACreation.addUsePemHeader(flatBufferBuilder2, uaProfile.getUsePemHeader());
        RequestUACreation.addPhoneId(flatBufferBuilder2, (long) uaProfile.getPhoneId());
        RequestUACreation.addSupportEct(flatBufferBuilder2, uaProfile.getSupportEct());
        RequestUACreation.addEarlyMediaRtpTimeoutTimer(flatBufferBuilder2, (long) uaProfile.getEarlyMediaRtpTimeoutTimer());
        RequestUACreation.addAddHistinfo(flatBufferBuilder2, uaProfile.getAddHistinfo());
        RequestUACreation.addSupportedGeolocationPhase(flatBufferBuilder2, (long) uaProfile.getSupportedGeolocationPhase());
        RequestUACreation.addNeedPidfSipMsg(flatBufferBuilder2, (long) uaProfile.getNeedPidfSipMsg());
        RequestUACreation.addNeedPidfRat(flatBufferBuilder2, (long) uaProfile.getNeedPidfRat());
        RequestUACreation.addUseProvisionalResponse100rel(flatBufferBuilder2, uaProfile.getUseProvisionalResponse100rel());
        RequestUACreation.addUse183OnProgressIncoming(flatBufferBuilder2, uaProfile.getUse183OnProgressIncoming());
        RequestUACreation.addUseQ850causeOn480(flatBufferBuilder2, uaProfile.getUseQ850causeOn480());
        RequestUACreation.addSupport183ForIr92v9Precondition(flatBufferBuilder2, uaProfile.getSupport183ForIr92v9Precondition());
        RequestUACreation.addSupportImsNotAvailable(flatBufferBuilder2, uaProfile.getSupportImsNotAvailable());
        RequestUACreation.addSupportLtePreferred(flatBufferBuilder2, uaProfile.getSupportLtePreferred());
        RequestUACreation.addUseSubcontactWhenResub(flatBufferBuilder2, uaProfile.getUseSubcontactWhenResub());
        RequestUACreation.addSupportUpgradePrecondition(flatBufferBuilder2, uaProfile.getSupportUpgradePrecondition());
        RequestUACreation.addSupportReplaceMerge(flatBufferBuilder2, uaProfile.getSupportReplaceMerge());
        RequestUACreation.addIsServerHeaderEnabled(flatBufferBuilder2, uaProfile.isServerHeaderEnabled());
        RequestUACreation.addSupportAccessType(flatBufferBuilder2, uaProfile2.supportAccessType);
        RequestUACreation.addLastPaniHeader(flatBufferBuilder2, createString17);
        RequestUACreation.addOipFromPreferred(flatBufferBuilder2, createString18);
        RequestUACreation.addSelectTransportAfterTcpReset(flatBufferBuilder2, createString19);
        RequestUACreation.addSrvccVersion(flatBufferBuilder2, (long) uaProfile.getSrvccVersion());
        RequestUACreation.addSupportSubscribeDialogEvent(flatBufferBuilder2, uaProfile2.supportSubscribeDialogEvent);
        RequestUACreation.addIsSimMobility(flatBufferBuilder2, uaProfile2.isSimMobility);
        RequestUACreation.addCmcType(flatBufferBuilder2, (long) uaProfile.getCmcType());
        RequestUACreation.addSupportDualSimCmc(flatBufferBuilder2, uaProfile.getSupportDualSimCmc());
        RequestUACreation.addVideoCrbtSupportType(flatBufferBuilder2, (long) uaProfile.getVideoCrbtSupportType());
        RequestUACreation.addRetryInviteOnTcpReset(flatBufferBuilder2, uaProfile.getRetryInviteOnTcpReset());
        RequestUACreation.addEnableVerstat(flatBufferBuilder2, uaProfile.getEnableVerstat());
        RequestUACreation.addRegRetryBaseTime(flatBufferBuilder2, uaProfile.getRegRetryBaseTime());
        RequestUACreation.addRegRetryMaxTime(flatBufferBuilder2, uaProfile.getRegRetryMaxTime());
        RequestUACreation.addSupportDualRcs(flatBufferBuilder2, uaProfile.getSupportDualRcs());
        RequestUACreation.addIsPttSupported(flatBufferBuilder2, uaProfile.getIsPttSupported());
        RequestUACreation.addTryReregisterFromKeepalive(flatBufferBuilder2, uaProfile.getTryReregisterFromKeepalive());
        RequestUACreation.addSslType(flatBufferBuilder2, uaProfile.getSslType());
        RequestUACreation.addHashAlgoType(flatBufferBuilder2, (long) uaProfile.getHashAlgoTypeType());
        RequestUACreation.addSupport199ProvisionalResponse(flatBufferBuilder2, uaProfile.getSupport199ProvisionalResponse());
        RequestUACreation.addSend18xReliably(flatBufferBuilder2, uaProfile.getSend18xReliably());
        RequestUACreation.addSupportNetworkInitUssi(flatBufferBuilder2, uaProfile.getSupportNetworkInitUssi());
        RequestUACreation.addSendByeForUssi(flatBufferBuilder2, uaProfile.getSendByeForUssi());
        RequestUACreation.addSupportRfc6337ForDelayedOffer(flatBufferBuilder2, uaProfile.getSupportRfc6337ForDelayedOffer());
        RequestUACreation.addUse200offerWhenRemoteNotSupport100rel(flatBufferBuilder2, uaProfile.getUse200offerWhenRemoteNotSupport100rel());
        RequestUACreation.addVowifi5gsaMode(flatBufferBuilder2, (long) uaProfile.getVowifi5gsaMode());
        RequestUACreation.addExcludePaniVowifiInitialRegi(flatBufferBuilder2, uaProfile.getExcludePaniVowifiInitialRegi());
        RequestUACreation.addSingleRegiEnabled(flatBufferBuilder2, uaProfile.getSingleRegiEnabled());
        RequestUACreation.addNeedCheckAllowedMethodForRefresh(flatBufferBuilder2, uaProfile.getNeedCheckAllowedMethodForRefresh());
        RequestUACreation.addAddMmtelCallcomposerTag(flatBufferBuilder2, uaProfile.getIsAddMmtelCallComposerTag());
        RequestUACreation.addKeepAliveFactor(flatBufferBuilder2, uaProfile.getKeepAliveFactor());
        RequestUACreation.addEncrNullRoaming(flatBufferBuilder2, uaProfile.getEncrNullRoaming());
        RequestUACreation.addSupportUac(flatBufferBuilder2, uaProfile.getSupportUac());
        RequestUACreation.addNeedVolteRetryInNr(flatBufferBuilder2, uaProfile.getNeedVolteRetryInNr());
        RequestUACreation.addImpuPreference(flatBufferBuilder2, uaProfile.getImpuPreference());
        RequestUACreation.addIsUpdateSaOnStartSupported(flatBufferBuilder2, uaProfile.isUpdateSaOnStartSupported());
        RequestUACreation.addSupportB2cCallcomposerWithoutFeaturetag(flatBufferBuilder2, uaProfile.getSupportB2cCallcomposerWithoutFeaturetag());
        int i11 = i7;
        if (i11 != -1) {
            RequestUACreation.addImsibasedimpu(flatBufferBuilder2, i11);
        }
        int i12 = createString22;
        if (i12 != -1) {
            RequestUACreation.addHostname(flatBufferBuilder2, i12);
        }
        int i13 = createString23;
        if (i13 != -1) {
            RequestUACreation.addSessionRefresher(flatBufferBuilder2, i13);
        }
        boolean z = uaProfile2.isipsec;
        if (z) {
            RequestUACreation.addIsIpsec(flatBufferBuilder2, z);
            int i14 = i2;
            if (i14 != -1) {
                RequestUACreation.addAuthAlg(flatBufferBuilder2, i14);
            }
            int i15 = i;
            if (i15 != -1) {
                RequestUACreation.addEncrAlg(flatBufferBuilder2, i15);
            }
        }
        int i16 = createString24;
        if (i16 != -1) {
            RequestUACreation.addPassword(flatBufferBuilder2, i16);
        }
        Mno mno = uaProfile2.mno;
        if (!(mno == null || (translateMno = StackRequestBuilderUtil.translateMno(mno)) == 0)) {
            String str10 = LOG_TAG;
            Log.i(str10, "translateMno: " + translateMno);
            RequestUACreation.addMno(flatBufferBuilder2, translateMno);
        }
        RequestUACreation.addMvno(flatBufferBuilder2, createString20);
        int i17 = createString25;
        if (i17 != -1) {
            RequestUACreation.addDisplayName(flatBufferBuilder2, i17);
        }
        int i18 = createString26;
        if (i18 != -1) {
            RequestUACreation.addUuid(flatBufferBuilder2, i18);
        }
        int i19 = createString27;
        if (i19 != -1) {
            RequestUACreation.addContactDisplayName(flatBufferBuilder2, i19);
        }
        int i20 = createString28;
        if (i20 != -1) {
            RequestUACreation.addRealm(flatBufferBuilder2, i20);
        }
        int i21 = createString29;
        if (i21 != -1) {
            RequestUACreation.addImMsgTech(flatBufferBuilder2, i21);
        }
        int i22 = createString30;
        if (i22 != -1) {
            RequestUACreation.addCmcRelayType(flatBufferBuilder2, i22);
        }
        int i23 = createString31;
        if (i23 != -1) {
            RequestUACreation.addCmcEmergencyNumbers(flatBufferBuilder2, i23);
        }
        int i24 = i3;
        if (i24 != -1) {
            RequestUACreation.addServiceList(flatBufferBuilder2, i24);
        }
        int i25 = i9;
        if (i25 != -1) {
            RequestUACreation.addFeatureTagList(flatBufferBuilder2, i25);
        }
        RequestUACreation.addConfigDualIms(flatBufferBuilder2, (long) StackRequestBuilderUtil.translateConfigDualIms());
        RequestUACreation.addMediaConfig(flatBufferBuilder2, i5);
        int i26 = i10;
        if (i26 != -1) {
            RequestUACreation.addAcb(flatBufferBuilder2, i26);
        }
        if (createUacSipListVector != -1) {
            RequestUACreation.addUacSipList(flatBufferBuilder2, createUacSipListVector);
        }
        RequestUACreation.addIgnoreDisplayName(flatBufferBuilder2, uaProfile.isDisplayNameIgnored());
        return RequestUACreation.endRequestUACreation(flatBufferBuilder);
    }

    private static int addMediaParameters(FlatBufferBuilder flatBufferBuilder, UaProfile uaProfile) {
        FlatBufferBuilder flatBufferBuilder2 = flatBufferBuilder;
        CallProfile callProfile = uaProfile.getCallProfile();
        int createString = flatBufferBuilder2.createString((CharSequence) callProfile.audioCodec);
        int createString2 = flatBufferBuilder2.createString((CharSequence) callProfile.amrMode);
        int createString3 = flatBufferBuilder2.createString((CharSequence) callProfile.amrWbMode);
        int createString4 = flatBufferBuilder2.createString((CharSequence) Integer.toString(callProfile.audioAs));
        int createString5 = flatBufferBuilder2.createString((CharSequence) Integer.toString(callProfile.audioRs));
        int createString6 = flatBufferBuilder2.createString((CharSequence) Integer.toString(callProfile.audioRr));
        int createString7 = flatBufferBuilder2.createString((CharSequence) callProfile.videoCodec);
        int createString8 = flatBufferBuilder2.createString((CharSequence) callProfile.displayFormat);
        int createString9 = flatBufferBuilder2.createString((CharSequence) callProfile.packetizationMode);
        int createString10 = flatBufferBuilder2.createString((CharSequence) callProfile.evsDiscontinuousTransmission);
        int createString11 = flatBufferBuilder2.createString((CharSequence) callProfile.evsDtxRecv);
        int createString12 = flatBufferBuilder2.createString((CharSequence) callProfile.evsHeaderFull);
        int createString13 = flatBufferBuilder2.createString((CharSequence) callProfile.evsModeSwitch);
        int createString14 = flatBufferBuilder2.createString((CharSequence) callProfile.evsChannelSend);
        int createString15 = flatBufferBuilder2.createString((CharSequence) callProfile.evsChannelRecv);
        int createString16 = flatBufferBuilder2.createString((CharSequence) callProfile.evsChannelAwareReceive);
        int createString17 = flatBufferBuilder2.createString((CharSequence) callProfile.evsCodecModeRequest);
        int createString18 = flatBufferBuilder2.createString((CharSequence) callProfile.evsBitRateSend);
        int createString19 = flatBufferBuilder2.createString((CharSequence) callProfile.evsBitRateReceive);
        int createString20 = flatBufferBuilder2.createString((CharSequence) callProfile.evsBandwidthSend);
        int createString21 = flatBufferBuilder2.createString((CharSequence) callProfile.evsBandwidthReceive);
        int createString22 = flatBufferBuilder2.createString((CharSequence) callProfile.evsDefaultBandwidth);
        int createString23 = flatBufferBuilder2.createString((CharSequence) callProfile.evsDefaultBitrate);
        int createString24 = flatBufferBuilder2.createString((CharSequence) callProfile.displayFormatHevc);
        int i = createString13;
        int createString25 = flatBufferBuilder2.createString((CharSequence) callProfile.evsBitRateSendExt);
        int createString26 = flatBufferBuilder2.createString((CharSequence) callProfile.evsBitRateReceiveExt);
        int createString27 = flatBufferBuilder2.createString((CharSequence) callProfile.evsBandwidthSendExt);
        int createString28 = flatBufferBuilder2.createString((CharSequence) callProfile.evsBandwidthReceiveExt);
        int createString29 = flatBufferBuilder2.createString((CharSequence) callProfile.evsLimitedCodec);
        MediaConfig.startMediaConfig(flatBufferBuilder);
        MediaConfig.addAudioCodec(flatBufferBuilder2, createString);
        MediaConfig.addAudioPort(flatBufferBuilder2, (long) callProfile.audioPort);
        MediaConfig.addAudioDscp(flatBufferBuilder2, (long) callProfile.audioDscp);
        MediaConfig.addAmrPayload(flatBufferBuilder2, (long) callProfile.amrOaPayloadType);
        MediaConfig.addAmrbePayload(flatBufferBuilder2, (long) callProfile.amrPayloadType);
        MediaConfig.addAmrWbPayload(flatBufferBuilder2, (long) callProfile.amrWbOaPayloadType);
        MediaConfig.addAmrbeWbPayload(flatBufferBuilder2, (long) callProfile.amrWbPayloadType);
        MediaConfig.addAmrOpenPayload(flatBufferBuilder2, (long) callProfile.amrOpenPayloadType);
        MediaConfig.addDtmfPayload(flatBufferBuilder2, (long) callProfile.dtmfPayloadType);
        MediaConfig.addDtmfWbPayload(flatBufferBuilder2, (long) callProfile.dtmfWbPayloadType);
        MediaConfig.addAmrMaxRed(flatBufferBuilder2, (long) callProfile.amrOaMaxRed);
        MediaConfig.addAmrbeMaxRed(flatBufferBuilder2, (long) callProfile.amrBeMaxRed);
        MediaConfig.addAmrWbMaxRed(flatBufferBuilder2, (long) callProfile.amrOaWbMaxRed);
        MediaConfig.addAmrbeWbMaxRed(flatBufferBuilder2, (long) callProfile.amrBeWbMaxRed);
        MediaConfig.addEvsMaxRed(flatBufferBuilder2, (long) callProfile.evsMaxRed);
        MediaConfig.addAmrMode(flatBufferBuilder2, createString2);
        MediaConfig.addAmrWbMode(flatBufferBuilder2, createString3);
        MediaConfig.addAudioAs(flatBufferBuilder2, createString4);
        MediaConfig.addAudioRs(flatBufferBuilder2, createString5);
        MediaConfig.addAudioRr(flatBufferBuilder2, createString6);
        MediaConfig.addPTime(flatBufferBuilder2, (long) callProfile.pTime);
        MediaConfig.addMaxTime(flatBufferBuilder2, (long) callProfile.maxPTime);
        MediaConfig.addVideoCodec(flatBufferBuilder2, createString7);
        MediaConfig.addVideoPort(flatBufferBuilder2, (long) callProfile.videoPort);
        MediaConfig.addFrameRate(flatBufferBuilder2, (long) callProfile.frameRate);
        MediaConfig.addDisplayFormat(flatBufferBuilder2, createString8);
        MediaConfig.addDisplayFormatHevc(flatBufferBuilder2, createString24);
        MediaConfig.addPacketizationMode(flatBufferBuilder2, createString9);
        MediaConfig.addH265QvgaPayload(flatBufferBuilder2, (long) callProfile.h265QvgaPayloadType);
        MediaConfig.addH265QvgalPayload(flatBufferBuilder2, (long) callProfile.h265QvgaLPayloadType);
        MediaConfig.addH265VgaPayload(flatBufferBuilder2, (long) callProfile.h265VgaPayloadType);
        MediaConfig.addH265VgalPayload(flatBufferBuilder2, (long) callProfile.h265VgaLPayloadType);
        MediaConfig.addH265Hd720pPayload(flatBufferBuilder2, (long) callProfile.h265Hd720pPayloadType);
        MediaConfig.addH265Hd720plPayload(flatBufferBuilder2, (long) callProfile.h265Hd720pLPayloadType);
        MediaConfig.addH264720pPayload(flatBufferBuilder2, (long) callProfile.h264720pPayloadType);
        MediaConfig.addH264720plPayload(flatBufferBuilder2, (long) callProfile.h264720pLPayloadType);
        MediaConfig.addH264VgaPayload(flatBufferBuilder2, (long) callProfile.h264VgaPayloadType);
        MediaConfig.addH264VgalPayload(flatBufferBuilder2, (long) callProfile.h264VgaLPayloadType);
        MediaConfig.addH264QvgaPayload(flatBufferBuilder2, (long) callProfile.h264QvgaPayloadType);
        MediaConfig.addH264QvgalPayload(flatBufferBuilder2, (long) callProfile.h264QvgaLPayloadType);
        MediaConfig.addH264CifPayload(flatBufferBuilder2, (long) callProfile.h264CifPayloadType);
        MediaConfig.addH264CiflPayload(flatBufferBuilder2, (long) callProfile.h264CifLPayloadType);
        MediaConfig.addVideoAs(flatBufferBuilder2, (long) callProfile.videoAs);
        MediaConfig.addVideoRs(flatBufferBuilder2, (long) callProfile.videoRs);
        MediaConfig.addVideoRr(flatBufferBuilder2, (long) callProfile.videoRr);
        MediaConfig.addTextAs(flatBufferBuilder2, (long) callProfile.textAs);
        MediaConfig.addTextRs(flatBufferBuilder2, (long) callProfile.textRs);
        MediaConfig.addTextRr(flatBufferBuilder2, (long) callProfile.textRr);
        MediaConfig.addTextPort(flatBufferBuilder2, (long) callProfile.textPort);
        MediaConfig.addAudioAvpf(flatBufferBuilder2, callProfile.audioAvpf);
        MediaConfig.addAudioSrtp(flatBufferBuilder2, callProfile.audioSrtp);
        MediaConfig.addVideoAvpf(flatBufferBuilder2, callProfile.videoAvpf);
        MediaConfig.addVideoSrtp(flatBufferBuilder2, callProfile.videoSrtp);
        MediaConfig.addTextAvpf(flatBufferBuilder2, callProfile.textAvpf);
        MediaConfig.addTextSrtp(flatBufferBuilder2, callProfile.textSrtp);
        MediaConfig.addVideoCapabilities(flatBufferBuilder2, callProfile.videoCapabilities);
        MediaConfig.addEnableScr(flatBufferBuilder2, callProfile.enableScr);
        MediaConfig.addRtpTimeout(flatBufferBuilder2, (long) callProfile.rtpTimeout);
        MediaConfig.addRtcpTimeout(flatBufferBuilder2, (long) callProfile.rtcpTimeout);
        MediaConfig.addH263QcifPayload(flatBufferBuilder2, (long) callProfile.h263QcifPayloadType);
        MediaConfig.addUseSpsForH264Hd(flatBufferBuilder2, callProfile.useSpsForH264Hd);
        MediaConfig.addAudioRtcpXr(flatBufferBuilder2, callProfile.audioRtcpXr);
        MediaConfig.addVideoRtcpXr(flatBufferBuilder2, callProfile.videoRtcpXr);
        MediaConfig.addDtmfMode(flatBufferBuilder2, (long) callProfile.dtmfMode);
        MediaConfig.addEnableEvsCodec(flatBufferBuilder2, callProfile.enableEvsCodec);
        MediaConfig.addEvsDiscontinuousTransmission(flatBufferBuilder2, createString10);
        MediaConfig.addEvsDtxRecv(flatBufferBuilder2, createString11);
        MediaConfig.addEvsHeaderFull(flatBufferBuilder2, createString12);
        MediaConfig.addEvsModeSwitch(flatBufferBuilder2, i);
        MediaConfig.addEvsChannelSend(flatBufferBuilder2, createString14);
        MediaConfig.addEvsChannelRecv(flatBufferBuilder2, createString15);
        MediaConfig.addEvsChannelAwareReceive(flatBufferBuilder2, createString16);
        MediaConfig.addEvsCodecModeRequest(flatBufferBuilder2, createString17);
        MediaConfig.addEvsBitRateSend(flatBufferBuilder2, createString18);
        MediaConfig.addEvsBitRateReceive(flatBufferBuilder2, createString19);
        MediaConfig.addEvsBandwidthSend(flatBufferBuilder2, createString20);
        MediaConfig.addEvsBandwidthReceive(flatBufferBuilder2, createString21);
        MediaConfig.addEvsPayload(flatBufferBuilder2, (long) callProfile.evsPayload);
        MediaConfig.addEvs2ndPayload(flatBufferBuilder2, (long) callProfile.evs2ndPayload);
        MediaConfig.addEvsDefaultBandwidth(flatBufferBuilder2, createString22);
        MediaConfig.addEvsDefaultBitrate(flatBufferBuilder2, createString23);
        MediaConfig.addEnableRtcpOnActiveCall(flatBufferBuilder2, callProfile.enableRtcpOnActiveCall);
        MediaConfig.addEnableAvSync(flatBufferBuilder2, callProfile.enableAvSync);
        MediaConfig.addDisplayFormatHevc(flatBufferBuilder2, createString24);
        MediaConfig.addH264720pPayload(flatBufferBuilder2, (long) callProfile.h264720pPayloadType);
        MediaConfig.addH264720plPayload(flatBufferBuilder2, (long) callProfile.h264720pLPayloadType);
        MediaConfig.addIgnoreRtcpTimeoutOnHoldCall(flatBufferBuilder2, callProfile.ignoreRtcpTimeoutOnHoldCall);
        MediaConfig.addEvsPayloadExt(flatBufferBuilder2, (long) callProfile.evsPayloadExt);
        MediaConfig.addEvsBitRateSendExt(flatBufferBuilder2, createString25);
        MediaConfig.addEvsBitRateReceiveExt(flatBufferBuilder2, createString26);
        MediaConfig.addEvsBandwidthSendExt(flatBufferBuilder2, createString27);
        MediaConfig.addEvsBandwidthReceiveExt(flatBufferBuilder2, createString28);
        MediaConfig.addEvsLimitedCodec(flatBufferBuilder2, createString29);
        MediaConfig.addEvsUseDefaultRtcpBw(flatBufferBuilder2, callProfile.evsUseDefaultRtcpBw);
        return MediaConfig.endMediaConfig(flatBufferBuilder);
    }

    static StackRequest makeCreateUA(UaProfile uaProfile) {
        FlatBufferBuilder flatBufferBuilder = new FlatBufferBuilder(0);
        int addUaCreationReq = addUaCreationReq(flatBufferBuilder, uaProfile);
        Request.startRequest(flatBufferBuilder);
        Request.addReqid(flatBufferBuilder, 102);
        Request.addReqType(flatBufferBuilder, (byte) 2);
        Request.addReq(flatBufferBuilder, addUaCreationReq);
        return new StackRequest(flatBufferBuilder, Request.endRequest(flatBufferBuilder));
    }

    private static int deleteUaReq(FlatBufferBuilder flatBufferBuilder, long j) {
        RequestUADeletion.startRequestUADeletion(flatBufferBuilder);
        RequestUADeletion.addHandle(flatBufferBuilder, j);
        return RequestUADeletion.endRequestUADeletion(flatBufferBuilder);
    }

    static StackRequest makeDeleteUA(int i) {
        FlatBufferBuilder flatBufferBuilder = new FlatBufferBuilder(0);
        int deleteUaReq = deleteUaReq(flatBufferBuilder, (long) i);
        Request.startRequest(flatBufferBuilder);
        Request.addReqid(flatBufferBuilder, 103);
        Request.addReqType(flatBufferBuilder, (byte) 3);
        Request.addReq(flatBufferBuilder, deleteUaReq);
        return new StackRequest(flatBufferBuilder, Request.endRequest(flatBufferBuilder));
    }

    private static int registrationReq(int i, FlatBufferBuilder flatBufferBuilder, String str, int i2, int i3, List<String> list, List<String> list2, Capabilities capabilities, List<String> list3, String str2, String str3, boolean z, String str4, boolean z2) {
        int i4;
        FlatBufferBuilder flatBufferBuilder2 = flatBufferBuilder;
        List<String> list4 = list;
        List<String> list5 = list2;
        List<String> list6 = list3;
        String str5 = str2;
        String str6 = str3;
        String str7 = str4;
        int createPcscfAddrListVector = str != null ? RequestRegistration.createPcscfAddrListVector(flatBufferBuilder2, new int[]{flatBufferBuilder.createString((CharSequence) str)}) : -1;
        int createServiceListVector = list4 != null ? RequestRegistration.createServiceListVector(flatBufferBuilder2, StackRequestBuilderUtil.getStringOffsetArray(flatBufferBuilder2, list4, list.size())) : -1;
        int createImpuListVector = list5 != null ? RequestRegistration.createImpuListVector(flatBufferBuilder2, StackRequestBuilderUtil.getStringOffsetArray(flatBufferBuilder2, list5, list2.size())) : -1;
        int createThirdpartyFeatureListVector = list6 != null ? RequestRegistration.createThirdpartyFeatureListVector(flatBufferBuilder2, StackRequestBuilderUtil.getStringOffsetArray(flatBufferBuilder2, list6, list3.size())) : -1;
        int createString = str5 != null ? flatBufferBuilder2.createString((CharSequence) str5) : -1;
        if (capabilities != null) {
            List<Integer> translateFeatureTag = StackRequestBuilderUtil.translateFeatureTag(capabilities.getFeature());
            int[] iArr = new int[translateFeatureTag.size()];
            int i5 = 0;
            for (Integer intValue : translateFeatureTag) {
                iArr[i5] = intValue.intValue();
                i5++;
            }
            i4 = RequestUACreation.createFeatureTagListVector(flatBufferBuilder2, iArr);
        } else {
            i4 = -1;
        }
        int createString2 = str6 != null ? flatBufferBuilder2.createString((CharSequence) str6) : -1;
        int createString3 = str7 != null ? flatBufferBuilder2.createString((CharSequence) str7) : -1;
        RequestRegistration.startRequestRegistration(flatBufferBuilder);
        RequestRegistration.addHandle(flatBufferBuilder2, (long) i);
        RequestRegistration.addRegExp(flatBufferBuilder2, (long) i3);
        if (i4 != -1) {
            RequestRegistration.addFeatureTagList(flatBufferBuilder2, i4);
        }
        if (createPcscfAddrListVector != -1) {
            RequestRegistration.addPcscfAddrList(flatBufferBuilder2, createPcscfAddrListVector);
        }
        if (createServiceListVector != -1) {
            RequestRegistration.addServiceList(flatBufferBuilder2, createServiceListVector);
        }
        if (createImpuListVector != -1) {
            RequestRegistration.addImpuList(flatBufferBuilder2, createImpuListVector);
        }
        if (list6 != null) {
            RequestRegistration.addThirdpartyFeatureList(flatBufferBuilder2, createThirdpartyFeatureListVector);
        }
        if (str5 != null) {
            RequestRegistration.addAccessToken(flatBufferBuilder2, createString);
        }
        if (str6 != null) {
            RequestRegistration.addAuthServerUrl(flatBufferBuilder2, createString2);
        }
        if (str7 != null) {
            RequestRegistration.addImMsgTech(flatBufferBuilder2, createString3);
        }
        RequestRegistration.addPcscfPort(flatBufferBuilder2, (long) i2);
        RequestRegistration.addSingleRegiEnabled(flatBufferBuilder2, z);
        RequestRegistration.addAddMmtelCallcomposerTag(flatBufferBuilder2, z2);
        return RequestRegistration.endRequestRegistration(flatBufferBuilder);
    }

    static StackRequest makeRegister(int i, String str, int i2, int i3, List<String> list, List<String> list2, Capabilities capabilities, List<String> list3, String str2, String str3, boolean z, String str4, boolean z2) {
        FlatBufferBuilder flatBufferBuilder = new FlatBufferBuilder(0);
        int registrationReq = registrationReq(i, flatBufferBuilder, str, i2, i3, list, list2, capabilities, list3, str2, str3, z, str4, z2);
        Request.startRequest(flatBufferBuilder);
        Request.addReqid(flatBufferBuilder, 104);
        Request.addReqType(flatBufferBuilder, (byte) 4);
        Request.addReq(flatBufferBuilder, registrationReq);
        return new StackRequest(flatBufferBuilder, Request.endRequest(flatBufferBuilder));
    }

    private static int akaAuthInfoReq(FlatBufferBuilder flatBufferBuilder, long j, int i, String str) {
        int createString = flatBufferBuilder.createString((CharSequence) str);
        RequestUpdateAkaResp.startRequestUpdateAkaResp(flatBufferBuilder);
        RequestUpdateAkaResp.addHandle(flatBufferBuilder, j);
        RequestUpdateAkaResp.addRecvMng(flatBufferBuilder, (long) i);
        RequestUpdateAkaResp.addAkaResp(flatBufferBuilder, createString);
        return RequestUpdateAkaResp.endRequestUpdateAkaResp(flatBufferBuilder);
    }

    static StackRequest makeSendAuthResponse(int i, int i2, String str) {
        String str2 = LOG_TAG;
        Log.i(str2, "sendAuthResponse: handle " + i + " tid " + i2 + " response " + str);
        FlatBufferBuilder flatBufferBuilder = new FlatBufferBuilder(0);
        int akaAuthInfoReq = akaAuthInfoReq(flatBufferBuilder, (long) i, i2, str);
        Request.startRequest(flatBufferBuilder);
        Request.addReqid(flatBufferBuilder, 105);
        Request.addReqType(flatBufferBuilder, (byte) 5);
        Request.addReq(flatBufferBuilder, akaAuthInfoReq);
        return new StackRequest(flatBufferBuilder, Request.endRequest(flatBufferBuilder));
    }

    static StackRequest makeSendDnsQuery(int i, String str, String str2, List<String> list, String str3, String str4, String str5, long j) {
        FlatBufferBuilder flatBufferBuilder = new FlatBufferBuilder(0);
        int createString = !TextUtils.isEmpty(str5) ? flatBufferBuilder.createString((CharSequence) str5) : -1;
        int createString2 = !TextUtils.isEmpty(str4) ? flatBufferBuilder.createString((CharSequence) str4) : -1;
        int createString3 = !TextUtils.isEmpty(str3) ? flatBufferBuilder.createString((CharSequence) str3) : -1;
        int createString4 = !TextUtils.isEmpty(str2) ? flatBufferBuilder.createString((CharSequence) str2) : -1;
        int createString5 = !TextUtils.isEmpty(str) ? flatBufferBuilder.createString((CharSequence) str) : -1;
        int size = list.size();
        int[] iArr = new int[size];
        for (int i2 = 0; i2 < size; i2++) {
            iArr[i2] = flatBufferBuilder.createString((CharSequence) list.get(i2));
        }
        int createDnsServerListVector = RequestDnsQuery.createDnsServerListVector(flatBufferBuilder, iArr);
        RequestDnsQuery.startRequestDnsQuery(flatBufferBuilder);
        if (createString != -1) {
            RequestDnsQuery.addFamily(flatBufferBuilder, createString);
        }
        if (createString2 != -1) {
            RequestDnsQuery.addTransport(flatBufferBuilder, createString2);
        }
        if (createString3 != -1) {
            RequestDnsQuery.addType(flatBufferBuilder, createString3);
        }
        RequestDnsQuery.addDnsServerList(flatBufferBuilder, createDnsServerListVector);
        if (createString4 != -1) {
            RequestDnsQuery.addHostname(flatBufferBuilder, createString4);
        }
        if (createString5 != -1) {
            RequestDnsQuery.addInterfaceNw(flatBufferBuilder, createString5);
        }
        RequestDnsQuery.addHandle(flatBufferBuilder, (long) i);
        RequestDnsQuery.addNetId(flatBufferBuilder, j);
        int endRequestDnsQuery = RequestDnsQuery.endRequestDnsQuery(flatBufferBuilder);
        Request.startRequest(flatBufferBuilder);
        Request.addReqid(flatBufferBuilder, 106);
        Request.addReqType(flatBufferBuilder, (byte) 69);
        Request.addReq(flatBufferBuilder, endRequestDnsQuery);
        return new StackRequest(flatBufferBuilder, Request.endRequest(flatBufferBuilder));
    }

    private static int preferredImpuReq(FlatBufferBuilder flatBufferBuilder, long j, String str) {
        int createString = flatBufferBuilder.createString((CharSequence) str);
        RequestSetPreferredImpu.startRequestSetPreferredImpu(flatBufferBuilder);
        RequestSetPreferredImpu.addHandle(flatBufferBuilder, j);
        RequestSetPreferredImpu.addImpu(flatBufferBuilder, createString);
        return RequestSetPreferredImpu.endRequestSetPreferredImpu(flatBufferBuilder);
    }

    static StackRequest makeSetPreferredImpu(int i, String str) {
        FlatBufferBuilder flatBufferBuilder = new FlatBufferBuilder(0);
        int preferredImpuReq = preferredImpuReq(flatBufferBuilder, (long) i, str);
        Request.startRequest(flatBufferBuilder);
        Request.addReqid(flatBufferBuilder, 107);
        Request.addReqType(flatBufferBuilder, (byte) 6);
        Request.addReq(flatBufferBuilder, preferredImpuReq);
        return new StackRequest(flatBufferBuilder, Request.endRequest(flatBufferBuilder));
    }

    private static int networkSuspendReq(FlatBufferBuilder flatBufferBuilder, long j, boolean z) {
        RequestNetworkSuspended.startRequestNetworkSuspended(flatBufferBuilder);
        RequestNetworkSuspended.addHandle(flatBufferBuilder, j);
        RequestNetworkSuspended.addState(flatBufferBuilder, z);
        return RequestNetworkSuspended.endRequestNetworkSuspended(flatBufferBuilder);
    }

    static StackRequest makeNetworkSuspended(int i, boolean z) {
        String str = LOG_TAG;
        Log.i(str, "register: handle " + i + " state " + z);
        FlatBufferBuilder flatBufferBuilder = new FlatBufferBuilder(0);
        int networkSuspendReq = networkSuspendReq(flatBufferBuilder, (long) i, z);
        Request.startRequest(flatBufferBuilder);
        Request.addReqid(flatBufferBuilder, 108);
        Request.addReqType(flatBufferBuilder, (byte) 7);
        Request.addReq(flatBufferBuilder, networkSuspendReq);
        return new StackRequest(flatBufferBuilder, Request.endRequest(flatBufferBuilder));
    }

    static StackRequest makeRequestUpdateFeatureTag(int i, long j) {
        Log.i(LOG_TAG, "requestUpdateFeatureTag");
        FlatBufferBuilder flatBufferBuilder = new FlatBufferBuilder(0);
        List<Integer> translateFeatureTag = StackRequestBuilderUtil.translateFeatureTag(j);
        int size = translateFeatureTag.size();
        int[] iArr = new int[size];
        for (int i2 = 0; i2 < size; i2++) {
            iArr[i2] = translateFeatureTag.get(i2).intValue();
        }
        int createFeatureTagListVector = RequestUpdateFeatureTag.createFeatureTagListVector(flatBufferBuilder, iArr);
        RequestUpdateFeatureTag.startRequestUpdateFeatureTag(flatBufferBuilder);
        RequestUpdateFeatureTag.addFeatureTagList(flatBufferBuilder, createFeatureTagListVector);
        RequestUpdateFeatureTag.addHandle(flatBufferBuilder, (long) i);
        int endRequestUpdateFeatureTag = RequestUpdateFeatureTag.endRequestUpdateFeatureTag(flatBufferBuilder);
        Request.startRequest(flatBufferBuilder);
        Request.addReqid(flatBufferBuilder, 109);
        Request.addReqType(flatBufferBuilder, (byte) 12);
        Request.addReq(flatBufferBuilder, endRequestUpdateFeatureTag);
        return new StackRequest(flatBufferBuilder, Request.endRequest(flatBufferBuilder));
    }

    static StackRequest makeUpdateVceConfig(int i, boolean z) {
        String str = LOG_TAG;
        Log.i(str, "updateVceConfig: handle: " + i + ", vceEnabled: " + z);
        FlatBufferBuilder flatBufferBuilder = new FlatBufferBuilder(0);
        RequestUpdateVceConfig.startRequestUpdateVceConfig(flatBufferBuilder);
        RequestUpdateVceConfig.addHandle(flatBufferBuilder, (long) i);
        RequestUpdateVceConfig.addVceConfig(flatBufferBuilder, z);
        int endRequestUpdateVceConfig = RequestUpdateVceConfig.endRequestUpdateVceConfig(flatBufferBuilder);
        Request.startRequest(flatBufferBuilder);
        Request.addReqid(flatBufferBuilder, 110);
        Request.addReqType(flatBufferBuilder, ReqMsg.request_update_vce_config);
        Request.addReq(flatBufferBuilder, endRequestUpdateVceConfig);
        return new StackRequest(flatBufferBuilder, Request.endRequest(flatBufferBuilder));
    }

    static StackRequest makeSetTextMode(int i, int i2) {
        FlatBufferBuilder flatBufferBuilder = new FlatBufferBuilder(0);
        RequestSetTextMode.startRequestSetTextMode(flatBufferBuilder);
        RequestSetTextMode.addTextMode(flatBufferBuilder, (long) i2);
        RequestSetTextMode.addPhoneId(flatBufferBuilder, (long) i);
        int endRequestSetTextMode = RequestSetTextMode.endRequestSetTextMode(flatBufferBuilder);
        Request.startRequest(flatBufferBuilder);
        Request.addReqid(flatBufferBuilder, 112);
        Request.addReqType(flatBufferBuilder, (byte) 9);
        Request.addReq(flatBufferBuilder, endRequestSetTextMode);
        return new StackRequest(flatBufferBuilder, Request.endRequest(flatBufferBuilder));
    }

    static StackRequest makeUpdateXqEnable(int i, boolean z) {
        Log.i(LOG_TAG + "[" + i + "]", "updateXqEnable():  enable: " + z);
        FlatBufferBuilder flatBufferBuilder = new FlatBufferBuilder(0);
        RequestUpdateXqEnable.startRequestUpdateXqEnable(flatBufferBuilder);
        RequestUpdateXqEnable.addPhoneId(flatBufferBuilder, (long) i);
        RequestUpdateXqEnable.addEnable(flatBufferBuilder, z);
        int endRequestUpdateXqEnable = RequestUpdateXqEnable.endRequestUpdateXqEnable(flatBufferBuilder);
        Request.startRequest(flatBufferBuilder);
        Request.addReqid(flatBufferBuilder, 114);
        Request.addReqType(flatBufferBuilder, (byte) 11);
        Request.addReq(flatBufferBuilder, endRequestUpdateXqEnable);
        return new StackRequest(flatBufferBuilder, Request.endRequest(flatBufferBuilder));
    }

    static StackRequest makeUpdateServiceVersion(int i, HashMap<String, String> hashMap) {
        Log.i(LOG_TAG + "[" + i + "]", "updateServiceVersion:phoneId:" + i);
        for (Map.Entry next : hashMap.entrySet()) {
            Log.i(LOG_TAG + "[" + i + "]", ((String) next.getKey()) + " : " + ((String) next.getValue()));
        }
        int i2 = 0;
        FlatBufferBuilder flatBufferBuilder = new FlatBufferBuilder(0);
        List<Integer> translateExtraHeader = StackRequestBuilderUtil.translateExtraHeader(flatBufferBuilder, hashMap);
        int[] iArr = new int[translateExtraHeader.size()];
        for (Integer intValue : translateExtraHeader) {
            iArr[i2] = intValue.intValue();
            i2++;
        }
        int createPairVector = ExtraHeader.createPairVector(flatBufferBuilder, iArr);
        ExtraHeader.startExtraHeader(flatBufferBuilder);
        ExtraHeader.addPair(flatBufferBuilder, createPairVector);
        int endExtraHeader = ExtraHeader.endExtraHeader(flatBufferBuilder);
        ServiceVersionConfig.startServiceVersionConfig(flatBufferBuilder);
        ServiceVersionConfig.addExtraHeaders(flatBufferBuilder, endExtraHeader);
        int endServiceVersionConfig = ServiceVersionConfig.endServiceVersionConfig(flatBufferBuilder);
        RequestUpdateCommonConfig.startRequestUpdateCommonConfig(flatBufferBuilder);
        RequestUpdateCommonConfig.addConfigType(flatBufferBuilder, (byte) 5);
        RequestUpdateCommonConfig.addPhoneId(flatBufferBuilder, (long) i);
        RequestUpdateCommonConfig.addConfig(flatBufferBuilder, endServiceVersionConfig);
        int endRequestUpdateCommonConfig = RequestUpdateCommonConfig.endRequestUpdateCommonConfig(flatBufferBuilder);
        Request.startRequest(flatBufferBuilder);
        Request.addReqid(flatBufferBuilder, 101);
        Request.addReqType(flatBufferBuilder, (byte) 1);
        Request.addReq(flatBufferBuilder, endRequestUpdateCommonConfig);
        return new StackRequest(flatBufferBuilder, Request.endRequest(flatBufferBuilder));
    }

    static StackRequest makeConfigRCS(int i, RcsConfig rcsConfig) {
        FlatBufferBuilder flatBufferBuilder = new FlatBufferBuilder(0);
        int createString = flatBufferBuilder.createString((CharSequence) rcsConfig.getConfUri());
        int createString2 = flatBufferBuilder.createString((CharSequence) rcsConfig.getDownloadsPath());
        int createString3 = flatBufferBuilder.createString((CharSequence) rcsConfig.getExploderUri());
        int createString4 = flatBufferBuilder.createString((CharSequence) rcsConfig.getEndUserConfReqId());
        int createString5 = flatBufferBuilder.createString((CharSequence) rcsConfig.getSupportBotVersions());
        com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestUpdateCommonConfig_.RcsConfig.startRcsConfig(flatBufferBuilder);
        com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestUpdateCommonConfig_.RcsConfig.addRcsFtChunkSize(flatBufferBuilder, (long) rcsConfig.getFtChunkSize());
        com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestUpdateCommonConfig_.RcsConfig.addRcsIshChunkSize(flatBufferBuilder, (long) rcsConfig.getIshChunkSize());
        com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestUpdateCommonConfig_.RcsConfig.addConfUri(flatBufferBuilder, createString);
        com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestUpdateCommonConfig_.RcsConfig.addIsMsrpCema(flatBufferBuilder, rcsConfig.isMsrpCema());
        com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestUpdateCommonConfig_.RcsConfig.addDownloadsPath(flatBufferBuilder, createString2);
        com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestUpdateCommonConfig_.RcsConfig.addIsConfSubscribeEnabled(flatBufferBuilder, rcsConfig.isConfSubscribeEnabled());
        com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestUpdateCommonConfig_.RcsConfig.addExploderUri(flatBufferBuilder, createString3);
        com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestUpdateCommonConfig_.RcsConfig.addPagerModeSizeLimit(flatBufferBuilder, (long) rcsConfig.getPagerModeLimit());
        com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestUpdateCommonConfig_.RcsConfig.addUseMsrpDiscardPort(flatBufferBuilder, rcsConfig.isUseMsrpDiscardPort());
        com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestUpdateCommonConfig_.RcsConfig.addIsAggrImdnSupported(flatBufferBuilder, rcsConfig.isAggrImdnSupported());
        com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestUpdateCommonConfig_.RcsConfig.addIsCbPrivacyDisable(flatBufferBuilder, rcsConfig.isPrivacyDisable());
        com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestUpdateCommonConfig_.RcsConfig.addCbMsgTech(flatBufferBuilder, rcsConfig.getCbMsgTech());
        com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestUpdateCommonConfig_.RcsConfig.addEndUserConfReqId(flatBufferBuilder, createString4);
        com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestUpdateCommonConfig_.RcsConfig.addSupportedBotVersions(flatBufferBuilder, createString5);
        com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestUpdateCommonConfig_.RcsConfig.addSupportCancelMessage(flatBufferBuilder, rcsConfig.getSupportCancelMessage());
        com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestUpdateCommonConfig_.RcsConfig.addSupportRealtimeUserAlias(flatBufferBuilder, rcsConfig.getSupportRealtimeUserAlias());
        int endRcsConfig = com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestUpdateCommonConfig_.RcsConfig.endRcsConfig(flatBufferBuilder);
        RequestUpdateCommonConfig.startRequestUpdateCommonConfig(flatBufferBuilder);
        RequestUpdateCommonConfig.addConfigType(flatBufferBuilder, (byte) 4);
        RequestUpdateCommonConfig.addConfig(flatBufferBuilder, endRcsConfig);
        RequestUpdateCommonConfig.addPhoneId(flatBufferBuilder, (long) i);
        int endRequestUpdateCommonConfig = RequestUpdateCommonConfig.endRequestUpdateCommonConfig(flatBufferBuilder);
        Request.startRequest(flatBufferBuilder);
        Request.addReqid(flatBufferBuilder, 101);
        Request.addReqType(flatBufferBuilder, (byte) 1);
        Request.addReq(flatBufferBuilder, endRequestUpdateCommonConfig);
        return new StackRequest(flatBufferBuilder, Request.endRequest(flatBufferBuilder));
    }

    static StackRequest makeUpdateScreenOnOff(int i, int i2) {
        FlatBufferBuilder flatBufferBuilder = new FlatBufferBuilder(0);
        ScreenConfig.startScreenConfig(flatBufferBuilder);
        ScreenConfig.addOn(flatBufferBuilder, (long) i2);
        int endScreenConfig = ScreenConfig.endScreenConfig(flatBufferBuilder);
        RequestUpdateCommonConfig.startRequestUpdateCommonConfig(flatBufferBuilder);
        RequestUpdateCommonConfig.addConfigType(flatBufferBuilder, (byte) 6);
        RequestUpdateCommonConfig.addConfig(flatBufferBuilder, endScreenConfig);
        RequestUpdateCommonConfig.addPhoneId(flatBufferBuilder, (long) i);
        int endRequestUpdateCommonConfig = RequestUpdateCommonConfig.endRequestUpdateCommonConfig(flatBufferBuilder);
        Request.startRequest(flatBufferBuilder);
        Request.addReqid(flatBufferBuilder, 101);
        Request.addReqType(flatBufferBuilder, (byte) 1);
        Request.addReq(flatBufferBuilder, endRequestUpdateCommonConfig);
        return new StackRequest(flatBufferBuilder, Request.endRequest(flatBufferBuilder));
    }

    private static int imeiReq(int i, FlatBufferBuilder flatBufferBuilder, String str) {
        int createString = flatBufferBuilder.createString((CharSequence) str);
        RegiConfig.startRegiConfig(flatBufferBuilder);
        RegiConfig.addImei(flatBufferBuilder, createString);
        int endRegiConfig = RegiConfig.endRegiConfig(flatBufferBuilder);
        RequestUpdateCommonConfig.startRequestUpdateCommonConfig(flatBufferBuilder);
        RequestUpdateCommonConfig.addConfigType(flatBufferBuilder, (byte) 1);
        RequestUpdateCommonConfig.addConfig(flatBufferBuilder, endRegiConfig);
        RequestUpdateCommonConfig.addPhoneId(flatBufferBuilder, (long) i);
        return RequestUpdateCommonConfig.endRequestUpdateCommonConfig(flatBufferBuilder);
    }

    static StackRequest makeConfigRegistration(int i, String str) {
        FlatBufferBuilder flatBufferBuilder = new FlatBufferBuilder(0);
        int imeiReq = imeiReq(i, flatBufferBuilder, str);
        Request.startRequest(flatBufferBuilder);
        Request.addReqid(flatBufferBuilder, 101);
        Request.addReqType(flatBufferBuilder, (byte) 1);
        Request.addReq(flatBufferBuilder, imeiReq);
        return new StackRequest(flatBufferBuilder, Request.endRequest(flatBufferBuilder));
    }

    static StackRequest makeConfigCall(int i, boolean z, boolean z2, boolean z3) {
        FlatBufferBuilder flatBufferBuilder = new FlatBufferBuilder(0);
        CallConfig.startCallConfig(flatBufferBuilder);
        CallConfig.addTtySessionRequired(flatBufferBuilder, z);
        CallConfig.addAutomaticMode(flatBufferBuilder, z3);
        CallConfig.addRttSessionRequired(flatBufferBuilder, z2);
        int endCallConfig = CallConfig.endCallConfig(flatBufferBuilder);
        RequestUpdateCommonConfig.startRequestUpdateCommonConfig(flatBufferBuilder);
        RequestUpdateCommonConfig.addConfigType(flatBufferBuilder, (byte) 3);
        RequestUpdateCommonConfig.addConfig(flatBufferBuilder, endCallConfig);
        RequestUpdateCommonConfig.addPhoneId(flatBufferBuilder, (long) i);
        int endRequestUpdateCommonConfig = RequestUpdateCommonConfig.endRequestUpdateCommonConfig(flatBufferBuilder);
        Request.startRequest(flatBufferBuilder);
        Request.addReqid(flatBufferBuilder, 101);
        Request.addReqType(flatBufferBuilder, (byte) 1);
        Request.addReq(flatBufferBuilder, endRequestUpdateCommonConfig);
        return new StackRequest(flatBufferBuilder, Request.endRequest(flatBufferBuilder));
    }
}
