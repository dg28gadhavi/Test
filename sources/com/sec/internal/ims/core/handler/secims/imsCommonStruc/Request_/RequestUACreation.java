package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.MNO;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestUACreation_.MediaConfig;
import com.sec.internal.ims.servicemodules.volte2.CallStateMachine;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestUACreation extends Table {
    public static RequestUACreation getRootAsRequestUACreation(ByteBuffer byteBuffer) {
        return getRootAsRequestUACreation(byteBuffer, new RequestUACreation());
    }

    public static RequestUACreation getRootAsRequestUACreation(ByteBuffer byteBuffer, RequestUACreation requestUACreation) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestUACreation.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestUACreation __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public String interfaceNw() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer interfaceNwAsByteBuffer() {
        return __vector_as_bytebuffer(4, 1);
    }

    public String pdn() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer pdnAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public String impu() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer impuAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public String imsibasedimpu() {
        int __offset = __offset(10);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer imsibasedimpuAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public String impi() {
        int __offset = __offset(12);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer impiAsByteBuffer() {
        return __vector_as_bytebuffer(12, 1);
    }

    public String domain() {
        int __offset = __offset(14);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer domainAsByteBuffer() {
        return __vector_as_bytebuffer(14, 1);
    }

    public boolean isSipOutbound() {
        int __offset = __offset(16);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public long qParam() {
        int __offset = __offset(18);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long controlDscp() {
        int __offset = __offset(20);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public String transType() {
        int __offset = __offset(22);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer transTypeAsByteBuffer() {
        return __vector_as_bytebuffer(22, 1);
    }

    public boolean isEmergencySupport() {
        int __offset = __offset(24);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public boolean isIpsec() {
        int __offset = __offset(26);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public String registerAlgo() {
        int __offset = __offset(28);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer registerAlgoAsByteBuffer() {
        return __vector_as_bytebuffer(28, 1);
    }

    public String prefId() {
        int __offset = __offset(30);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer prefIdAsByteBuffer() {
        return __vector_as_bytebuffer(30, 1);
    }

    public String remoteUriType() {
        int __offset = __offset(32);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer remoteUriTypeAsByteBuffer() {
        return __vector_as_bytebuffer(32, 1);
    }

    public String authName() {
        int __offset = __offset(34);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer authNameAsByteBuffer() {
        return __vector_as_bytebuffer(34, 1);
    }

    public String password() {
        int __offset = __offset(36);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer passwordAsByteBuffer() {
        return __vector_as_bytebuffer(36, 1);
    }

    public String encrAlg() {
        int __offset = __offset(38);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer encrAlgAsByteBuffer() {
        return __vector_as_bytebuffer(38, 1);
    }

    public String authAlg() {
        int __offset = __offset(40);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer authAlgAsByteBuffer() {
        return __vector_as_bytebuffer(40, 1);
    }

    public String regdomain() {
        int __offset = __offset(42);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer regdomainAsByteBuffer() {
        return __vector_as_bytebuffer(42, 1);
    }

    public String realm() {
        int __offset = __offset(44);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer realmAsByteBuffer() {
        return __vector_as_bytebuffer(44, 1);
    }

    public boolean isSipCompactHeader() {
        int __offset = __offset(46);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public boolean isSigComp() {
        int __offset = __offset(48);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public long secureClientPort() {
        int __offset = __offset(50);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long secureServerPort() {
        int __offset = __offset(52);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long selfPort() {
        int __offset = __offset(54);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public boolean isSubscribeRegEvent() {
        int __offset = __offset(56);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public String userAgent() {
        int __offset = __offset(58);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer userAgentAsByteBuffer() {
        return __vector_as_bytebuffer(58, 1);
    }

    public boolean isKeepAlive() {
        int __offset = __offset(60);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public boolean isPrecondEnabled() {
        int __offset = __offset(62);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public boolean isPrecondInitialSendrecv() {
        int __offset = __offset(64);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public int sessionExpires() {
        int __offset = __offset(66);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return -1;
    }

    public String sessionRefresher() {
        int __offset = __offset(68);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer sessionRefresherAsByteBuffer() {
        return __vector_as_bytebuffer(68, 1);
    }

    public int mno() {
        int __offset = __offset(70);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public String mvno() {
        int __offset = __offset(72);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer mvnoAsByteBuffer() {
        return __vector_as_bytebuffer(72, 1);
    }

    public String displayName() {
        int __offset = __offset(74);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer displayNameAsByteBuffer() {
        return __vector_as_bytebuffer(74, 1);
    }

    public String uuid() {
        int __offset = __offset(76);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer uuidAsByteBuffer() {
        return __vector_as_bytebuffer(76, 1);
    }

    public String contactDisplayName() {
        int __offset = __offset(78);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer contactDisplayNameAsByteBuffer() {
        return __vector_as_bytebuffer(78, 1);
    }

    public String instanceId() {
        int __offset = __offset(80);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer instanceIdAsByteBuffer() {
        return __vector_as_bytebuffer(80, 1);
    }

    public String imMsgTech() {
        int __offset = __offset(82);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer imMsgTechAsByteBuffer() {
        return __vector_as_bytebuffer(82, 1);
    }

    public long timer1() {
        int __offset = __offset(84);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long timer2() {
        int __offset = __offset(86);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long timer4() {
        int __offset = __offset(88);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long timerA() {
        int __offset = __offset(90);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long timerB() {
        int __offset = __offset(92);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long timerC() {
        int __offset = __offset(94);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long timerD() {
        int __offset = __offset(96);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long timerE() {
        int __offset = __offset(98);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long timerF() {
        int __offset = __offset(100);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long timerG() {
        int __offset = __offset(102);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long timerH() {
        int __offset = __offset(104);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long timerI() {
        int __offset = __offset(106);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long timerJ() {
        int __offset = __offset(108);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long timerK() {
        int __offset = __offset(110);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public boolean isSoftphoneEnabled() {
        int __offset = __offset(112);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public boolean isCdmalessEnabled() {
        int __offset = __offset(114);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public long mssSize() {
        int __offset = __offset(116);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long sipMobility() {
        int __offset = __offset(118);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long minse() {
        int __offset = __offset(120);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 90;
    }

    public long ringbackTimer() {
        int __offset = __offset(122);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long ringingTimer() {
        int __offset = __offset(124);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public boolean isEnableGruu() {
        int __offset = __offset(126);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public boolean isEnableVcid() {
        int __offset = __offset(128);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public boolean isEnableSessionId() {
        int __offset = __offset(130);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public long audioEngineType() {
        int __offset = __offset(132);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public String curPani() {
        int __offset = __offset(134);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer curPaniAsByteBuffer() {
        return __vector_as_bytebuffer(134, 1);
    }

    public long netId() {
        int __offset = __offset(136);
        if (__offset != 0) {
            return this.bb.getLong(__offset + this.bb_pos);
        }
        return 0;
    }

    public boolean isVceConfigEnabled() {
        int __offset = __offset(138);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public boolean isGcfConfigEnabled() {
        int __offset = __offset(140);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public String serviceList(int i) {
        int __offset = __offset(142);
        if (__offset != 0) {
            return __string(__vector(__offset) + (i * 4));
        }
        return null;
    }

    public int serviceListLength() {
        int __offset = __offset(142);
        if (__offset != 0) {
            return __vector_len(__offset);
        }
        return 0;
    }

    public int featureTagList(int i) {
        int __offset = __offset(144);
        if (__offset != 0) {
            return this.bb.getInt(__vector(__offset) + (i * 4));
        }
        return 0;
    }

    public int featureTagListLength() {
        int __offset = __offset(144);
        if (__offset != 0) {
            return __vector_len(__offset);
        }
        return 0;
    }

    public ByteBuffer featureTagListAsByteBuffer() {
        return __vector_as_bytebuffer(144, 4);
    }

    public boolean isNsdsServiceEnabled() {
        int __offset = __offset(146);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public long profileId() {
        int __offset = __offset(148);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public boolean wifiPreConditionEnabled() {
        int __offset = __offset(150);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public long timerTs() {
        int __offset = __offset(152);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public boolean isMsrpBearerUsed() {
        int __offset = __offset(154);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public long subscriberTimer() {
        int __offset = __offset(MNO.TANGO_LUXEMBOURG);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public boolean isSubscribeReg() {
        int __offset = __offset(MNO.STC_KSA);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public boolean useKeepAlive() {
        int __offset = __offset(MNO.UMOBILE);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public String msrpTransType() {
        int __offset = __offset(MNO.TMOBILE_ROMANIA);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer msrpTransTypeAsByteBuffer() {
        return __vector_as_bytebuffer(MNO.TMOBILE_ROMANIA, 1);
    }

    public String hostname() {
        int __offset = __offset(MNO.CLARO_COLOMBIA);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer hostnameAsByteBuffer() {
        return __vector_as_bytebuffer(MNO.CLARO_COLOMBIA, 1);
    }

    public boolean isFullCodecOfferRequired() {
        int __offset = __offset(MNO.TELENOR_BG);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public boolean isRcsTelephonyFeatureTagRequired() {
        int __offset = __offset(MNO.TELIA_FI);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public long scmVersion() {
        int __offset = __offset(MNO.ALTAN_MEXICO);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long activeDataPhoneId() {
        int __offset = __offset(MNO.TIGO_PANAMA);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public boolean isXqEnabled() {
        int __offset = __offset(MNO.VODAFONE_ROMANIA);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public long textMode() {
        int __offset = __offset(MNO.ORANGE_SENEGAL);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public int rcsProfile() {
        int __offset = __offset(MNO.MAGTICOM_GE);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public boolean needTransportInContact() {
        int __offset = __offset(MNO.EVR_ESN);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public long rat() {
        int __offset = __offset(MNO.TPG_SG);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long dbrTimer() {
        int __offset = __offset(MNO.WOM_CHILE);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public boolean isTcpGracefulShutdownEnabled() {
        int __offset = __offset(MNO.MTN_IRAN);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public int tcpRstUacErrorcode() {
        int __offset = __offset(MNO.CLARO_URUGUAY);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public int tcpRstUasErrorcode() {
        int __offset = __offset(MNO.MTN_GHANA);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public String privacyHeaderRestricted() {
        int __offset = __offset(MNO.TELEFONICA_SPAIN);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer privacyHeaderRestrictedAsByteBuffer() {
        return __vector_as_bytebuffer(MNO.TELEFONICA_SPAIN, 1);
    }

    public boolean usePemHeader() {
        int __offset = __offset(MNO.KOODO);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public boolean supportEct() {
        int __offset = __offset(MNO.BATELCO_BAHRAIN);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public long earlyMediaRtpTimeoutTimer() {
        int __offset = __offset(MNO.WINDTRE_IT);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public boolean addHistinfo() {
        int __offset = __offset(200);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public long supportedGeolocationPhase() {
        int __offset = __offset(202);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long phoneId() {
        int __offset = __offset(204);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long needPidfSipMsg() {
        int __offset = __offset(206);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long needPidfRat() {
        int __offset = __offset(208);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public boolean useProvisionalResponse100rel() {
        int __offset = __offset(210);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public boolean use183OnProgressIncoming() {
        int __offset = __offset(212);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public boolean useQ850causeOn480() {
        int __offset = __offset(214);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public boolean support183ForIr92v9Precondition() {
        int __offset = __offset(216);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public long configDualIms() {
        int __offset = __offset(218);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public boolean supportImsNotAvailable() {
        int __offset = __offset(220);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public boolean supportLtePreferred() {
        int __offset = __offset(222);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public boolean useSubcontactWhenResub() {
        int __offset = __offset(224);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public boolean supportUpgradePrecondition() {
        int __offset = __offset(226);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public boolean supportReplaceMerge() {
        int __offset = __offset(228);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public boolean isServerHeaderEnabled() {
        int __offset = __offset(230);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public boolean supportAccessType() {
        int __offset = __offset(232);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public String lastPaniHeader() {
        int __offset = __offset(234);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer lastPaniHeaderAsByteBuffer() {
        return __vector_as_bytebuffer(234, 1);
    }

    public String oipFromPreferred() {
        int __offset = __offset(236);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer oipFromPreferredAsByteBuffer() {
        return __vector_as_bytebuffer(236, 1);
    }

    public String selectTransportAfterTcpReset() {
        int __offset = __offset(238);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer selectTransportAfterTcpResetAsByteBuffer() {
        return __vector_as_bytebuffer(238, 1);
    }

    public long srvccVersion() {
        int __offset = __offset(240);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public boolean supportSubscribeDialogEvent() {
        int __offset = __offset(242);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public boolean isSimMobility() {
        int __offset = __offset(244);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public long cmcType() {
        int __offset = __offset(246);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public String cmcRelayType() {
        int __offset = __offset(248);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer cmcRelayTypeAsByteBuffer() {
        return __vector_as_bytebuffer(248, 1);
    }

    public String cmcEmergencyNumbers() {
        int __offset = __offset(250);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer cmcEmergencyNumbersAsByteBuffer() {
        return __vector_as_bytebuffer(250, 1);
    }

    public boolean supportDualSimCmc() {
        int __offset = __offset(252);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public long videoCrbtSupportType() {
        int __offset = __offset(MNO.TIGO_HONDURAS);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public boolean retryInviteOnTcpReset() {
        int __offset = __offset(256);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public boolean enableVerstat() {
        int __offset = __offset(MNO.TIGO_ELSALVADOR);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public int regRetryBaseTime() {
        int __offset = __offset(MNO.ANTEL_URUGUAY);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public int regRetryMaxTime() {
        int __offset = __offset(MNO.CBN);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public boolean supportDualRcs() {
        int __offset = __offset(MNO.H3G_IRELAND);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public boolean isPttSupported() {
        int __offset = __offset(MNO.VODAFONE_QATAR);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public boolean tryReregisterFromKeepalive() {
        int __offset = __offset(MNO.ORANGE_JORDAN);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public int sslType() {
        int __offset = __offset(MNO.AMERICANET_BRAZIL);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public boolean support199ProvisionalResponse() {
        int __offset = __offset(MNO.VIVA_DOMINICAN);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public boolean send18xReliably() {
        int __offset = __offset(MNO.ASIACELL_IRAQ);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public String acb(int i) {
        int __offset = __offset(MNO.INWI_MOROCCO);
        if (__offset != 0) {
            return __string(__vector(__offset) + (i * 4));
        }
        return null;
    }

    public int acbLength() {
        int __offset = __offset(MNO.INWI_MOROCCO);
        if (__offset != 0) {
            return __vector_len(__offset);
        }
        return 0;
    }

    public boolean supportNetworkInitUssi() {
        int __offset = __offset(MNO.ORANGE_EGYPT);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public boolean sendByeForUssi() {
        int __offset = __offset(MNO.VIRGIN_KSA);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public boolean supportRfc6337ForDelayedOffer() {
        int __offset = __offset(MNO.DIGI_BELIZE);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public boolean use200offerWhenRemoteNotSupport100rel() {
        int __offset = __offset(MNO.ENET_GUYANA);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public MediaConfig mediaConfig() {
        return mediaConfig(new MediaConfig());
    }

    public MediaConfig mediaConfig(MediaConfig mediaConfig) {
        int __offset = __offset(MNO.UNIFIQUE_BRAZIL);
        if (__offset != 0) {
            return mediaConfig.__assign(__indirect(__offset + this.bb_pos), this.bb);
        }
        return null;
    }

    public boolean ignoreDisplayName() {
        int __offset = __offset(MNO.MOD_QATAR);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public long hashAlgoType() {
        int __offset = __offset(290);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long vowifi5gsaMode() {
        int __offset = __offset(292);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public boolean excludePaniVowifiInitialRegi() {
        int __offset = __offset(294);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public boolean singleRegiEnabled() {
        int __offset = __offset(296);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public boolean needCheckAllowedMethodForRefresh() {
        int __offset = __offset(298);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public boolean addMmtelCallcomposerTag() {
        int __offset = __offset(300);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public int keepAliveFactor() {
        int __offset = __offset(302);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public boolean encrNullRoaming() {
        int __offset = __offset(304);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public boolean supportUac() {
        int __offset = __offset(306);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public String uacSipList(int i) {
        int __offset = __offset(CallStateMachine.ON_E911_PERM_FAIL);
        if (__offset != 0) {
            return __string(__vector(__offset) + (i * 4));
        }
        return null;
    }

    public int uacSipListLength() {
        int __offset = __offset(CallStateMachine.ON_E911_PERM_FAIL);
        if (__offset != 0) {
            return __vector_len(__offset);
        }
        return 0;
    }

    public boolean needVolteRetryInNr() {
        int __offset = __offset(310);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public int impuPreference() {
        int __offset = __offset(312);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public boolean isUpdateSaOnStartSupported() {
        int __offset = __offset(314);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public int supportB2cCallcomposerWithoutFeaturetag() {
        int __offset = __offset(316);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public static void startRequestUACreation(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(157);
    }

    public static void addInterfaceNw(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(0, i, 0);
    }

    public static void addPdn(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addImpu(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static void addImsibasedimpu(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(3, i, 0);
    }

    public static void addImpi(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(4, i, 0);
    }

    public static void addDomain(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(5, i, 0);
    }

    public static void addIsSipOutbound(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(6, z, false);
    }

    public static void addQParam(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(7, (int) j, 0);
    }

    public static void addControlDscp(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(8, (int) j, 0);
    }

    public static void addTransType(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(9, i, 0);
    }

    public static void addIsEmergencySupport(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(10, z, false);
    }

    public static void addIsIpsec(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(11, z, false);
    }

    public static void addRegisterAlgo(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(12, i, 0);
    }

    public static void addPrefId(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(13, i, 0);
    }

    public static void addRemoteUriType(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(14, i, 0);
    }

    public static void addAuthName(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(15, i, 0);
    }

    public static void addPassword(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(16, i, 0);
    }

    public static void addEncrAlg(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(17, i, 0);
    }

    public static void addAuthAlg(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(18, i, 0);
    }

    public static void addRegdomain(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(19, i, 0);
    }

    public static void addRealm(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(20, i, 0);
    }

    public static void addIsSipCompactHeader(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(21, z, false);
    }

    public static void addIsSigComp(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(22, z, false);
    }

    public static void addSecureClientPort(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(23, (int) j, 0);
    }

    public static void addSecureServerPort(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(24, (int) j, 0);
    }

    public static void addSelfPort(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(25, (int) j, 0);
    }

    public static void addIsSubscribeRegEvent(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(26, z, false);
    }

    public static void addUserAgent(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(27, i, 0);
    }

    public static void addIsKeepAlive(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(28, z, false);
    }

    public static void addIsPrecondEnabled(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(29, z, false);
    }

    public static void addIsPrecondInitialSendrecv(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(30, z, false);
    }

    public static void addSessionExpires(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(31, i, -1);
    }

    public static void addSessionRefresher(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(32, i, 0);
    }

    public static void addMno(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(33, i, 0);
    }

    public static void addMvno(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(34, i, 0);
    }

    public static void addDisplayName(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(35, i, 0);
    }

    public static void addUuid(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(36, i, 0);
    }

    public static void addContactDisplayName(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(37, i, 0);
    }

    public static void addInstanceId(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(38, i, 0);
    }

    public static void addImMsgTech(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(39, i, 0);
    }

    public static void addTimer1(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(40, (int) j, 0);
    }

    public static void addTimer2(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(41, (int) j, 0);
    }

    public static void addTimer4(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(42, (int) j, 0);
    }

    public static void addTimerA(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(43, (int) j, 0);
    }

    public static void addTimerB(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(44, (int) j, 0);
    }

    public static void addTimerC(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(45, (int) j, 0);
    }

    public static void addTimerD(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(46, (int) j, 0);
    }

    public static void addTimerE(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(47, (int) j, 0);
    }

    public static void addTimerF(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(48, (int) j, 0);
    }

    public static void addTimerG(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(49, (int) j, 0);
    }

    public static void addTimerH(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(50, (int) j, 0);
    }

    public static void addTimerI(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(51, (int) j, 0);
    }

    public static void addTimerJ(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(52, (int) j, 0);
    }

    public static void addTimerK(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(53, (int) j, 0);
    }

    public static void addIsSoftphoneEnabled(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(54, z, false);
    }

    public static void addIsCdmalessEnabled(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(55, z, false);
    }

    public static void addMssSize(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(56, (int) j, 0);
    }

    public static void addSipMobility(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(57, (int) j, 0);
    }

    public static void addMinse(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(58, (int) j, 90);
    }

    public static void addRingbackTimer(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(59, (int) j, 0);
    }

    public static void addRingingTimer(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(60, (int) j, 0);
    }

    public static void addIsEnableGruu(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(61, z, false);
    }

    public static void addIsEnableVcid(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(62, z, false);
    }

    public static void addIsEnableSessionId(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(63, z, false);
    }

    public static void addAudioEngineType(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(64, (int) j, 0);
    }

    public static void addCurPani(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(65, i, 0);
    }

    public static void addNetId(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addLong(66, j, 0);
    }

    public static void addIsVceConfigEnabled(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(67, z, false);
    }

    public static void addIsGcfConfigEnabled(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(68, z, false);
    }

    public static void addServiceList(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(69, i, 0);
    }

    public static int createServiceListVector(FlatBufferBuilder flatBufferBuilder, int[] iArr) {
        flatBufferBuilder.startVector(4, iArr.length, 4);
        for (int length = iArr.length - 1; length >= 0; length--) {
            flatBufferBuilder.addOffset(iArr[length]);
        }
        return flatBufferBuilder.endVector();
    }

    public static void startServiceListVector(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.startVector(4, i, 4);
    }

    public static void addFeatureTagList(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(70, i, 0);
    }

    public static int createFeatureTagListVector(FlatBufferBuilder flatBufferBuilder, int[] iArr) {
        flatBufferBuilder.startVector(4, iArr.length, 4);
        for (int length = iArr.length - 1; length >= 0; length--) {
            flatBufferBuilder.addInt(iArr[length]);
        }
        return flatBufferBuilder.endVector();
    }

    public static void startFeatureTagListVector(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.startVector(4, i, 4);
    }

    public static void addIsNsdsServiceEnabled(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(71, z, false);
    }

    public static void addProfileId(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(72, (int) j, 0);
    }

    public static void addWifiPreConditionEnabled(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(73, z, false);
    }

    public static void addTimerTs(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(74, (int) j, 0);
    }

    public static void addIsMsrpBearerUsed(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(75, z, false);
    }

    public static void addSubscriberTimer(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(76, (int) j, 0);
    }

    public static void addIsSubscribeReg(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(77, z, false);
    }

    public static void addUseKeepAlive(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(78, z, false);
    }

    public static void addMsrpTransType(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(79, i, 0);
    }

    public static void addHostname(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(80, i, 0);
    }

    public static void addIsFullCodecOfferRequired(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(81, z, false);
    }

    public static void addIsRcsTelephonyFeatureTagRequired(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(82, z, false);
    }

    public static void addScmVersion(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(83, (int) j, 0);
    }

    public static void addActiveDataPhoneId(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(84, (int) j, 0);
    }

    public static void addIsXqEnabled(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(85, z, false);
    }

    public static void addTextMode(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(86, (int) j, 0);
    }

    public static void addRcsProfile(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(87, i, 0);
    }

    public static void addNeedTransportInContact(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(88, z, false);
    }

    public static void addRat(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(89, (int) j, 0);
    }

    public static void addDbrTimer(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(90, (int) j, 0);
    }

    public static void addIsTcpGracefulShutdownEnabled(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(91, z, false);
    }

    public static void addTcpRstUacErrorcode(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(92, i, 0);
    }

    public static void addTcpRstUasErrorcode(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(93, i, 0);
    }

    public static void addPrivacyHeaderRestricted(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(94, i, 0);
    }

    public static void addUsePemHeader(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(95, z, false);
    }

    public static void addSupportEct(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(96, z, false);
    }

    public static void addEarlyMediaRtpTimeoutTimer(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(97, (int) j, 0);
    }

    public static void addAddHistinfo(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(98, z, false);
    }

    public static void addSupportedGeolocationPhase(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(99, (int) j, 0);
    }

    public static void addPhoneId(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(100, (int) j, 0);
    }

    public static void addNeedPidfSipMsg(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(101, (int) j, 0);
    }

    public static void addNeedPidfRat(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(102, (int) j, 0);
    }

    public static void addUseProvisionalResponse100rel(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(103, z, false);
    }

    public static void addUse183OnProgressIncoming(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(104, z, false);
    }

    public static void addUseQ850causeOn480(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(105, z, false);
    }

    public static void addSupport183ForIr92v9Precondition(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(106, z, false);
    }

    public static void addConfigDualIms(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(107, (int) j, 0);
    }

    public static void addSupportImsNotAvailable(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(108, z, false);
    }

    public static void addSupportLtePreferred(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(109, z, false);
    }

    public static void addUseSubcontactWhenResub(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(110, z, false);
    }

    public static void addSupportUpgradePrecondition(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(111, z, false);
    }

    public static void addSupportReplaceMerge(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(112, z, false);
    }

    public static void addIsServerHeaderEnabled(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(113, z, false);
    }

    public static void addSupportAccessType(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(114, z, false);
    }

    public static void addLastPaniHeader(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(115, i, 0);
    }

    public static void addOipFromPreferred(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(116, i, 0);
    }

    public static void addSelectTransportAfterTcpReset(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(117, i, 0);
    }

    public static void addSrvccVersion(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(118, (int) j, 0);
    }

    public static void addSupportSubscribeDialogEvent(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(119, z, false);
    }

    public static void addIsSimMobility(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(120, z, false);
    }

    public static void addCmcType(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(121, (int) j, 0);
    }

    public static void addCmcRelayType(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(122, i, 0);
    }

    public static void addCmcEmergencyNumbers(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(123, i, 0);
    }

    public static void addSupportDualSimCmc(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(124, z, false);
    }

    public static void addVideoCrbtSupportType(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(125, (int) j, 0);
    }

    public static void addRetryInviteOnTcpReset(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(126, z, false);
    }

    public static void addEnableVerstat(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(127, z, false);
    }

    public static void addRegRetryBaseTime(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(128, i, 0);
    }

    public static void addRegRetryMaxTime(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(129, i, 0);
    }

    public static void addSupportDualRcs(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(130, z, false);
    }

    public static void addIsPttSupported(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(131, z, false);
    }

    public static void addTryReregisterFromKeepalive(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(132, z, false);
    }

    public static void addSslType(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(133, i, 0);
    }

    public static void addSupport199ProvisionalResponse(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(134, z, false);
    }

    public static void addSend18xReliably(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(135, z, false);
    }

    public static void addAcb(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(136, i, 0);
    }

    public static int createAcbVector(FlatBufferBuilder flatBufferBuilder, int[] iArr) {
        flatBufferBuilder.startVector(4, iArr.length, 4);
        for (int length = iArr.length - 1; length >= 0; length--) {
            flatBufferBuilder.addOffset(iArr[length]);
        }
        return flatBufferBuilder.endVector();
    }

    public static void startAcbVector(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.startVector(4, i, 4);
    }

    public static void addSupportNetworkInitUssi(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(137, z, false);
    }

    public static void addSendByeForUssi(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(138, z, false);
    }

    public static void addSupportRfc6337ForDelayedOffer(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(139, z, false);
    }

    public static void addUse200offerWhenRemoteNotSupport100rel(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(140, z, false);
    }

    public static void addMediaConfig(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(141, i, 0);
    }

    public static void addIgnoreDisplayName(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(142, z, false);
    }

    public static void addHashAlgoType(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(143, (int) j, 0);
    }

    public static void addVowifi5gsaMode(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(144, (int) j, 0);
    }

    public static void addExcludePaniVowifiInitialRegi(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(145, z, false);
    }

    public static void addSingleRegiEnabled(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(146, z, false);
    }

    public static void addNeedCheckAllowedMethodForRefresh(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(147, z, false);
    }

    public static void addAddMmtelCallcomposerTag(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(148, z, false);
    }

    public static void addKeepAliveFactor(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(MNO.SBERBANK_RUSSIA, i, 0);
    }

    public static void addEncrNullRoaming(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(150, z, false);
    }

    public static void addSupportUac(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(151, z, false);
    }

    public static void addUacSipList(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(152, i, 0);
    }

    public static int createUacSipListVector(FlatBufferBuilder flatBufferBuilder, int[] iArr) {
        flatBufferBuilder.startVector(4, iArr.length, 4);
        for (int length = iArr.length - 1; length >= 0; length--) {
            flatBufferBuilder.addOffset(iArr[length]);
        }
        return flatBufferBuilder.endVector();
    }

    public static void startUacSipListVector(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.startVector(4, i, 4);
    }

    public static void addNeedVolteRetryInNr(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(153, z, false);
    }

    public static void addImpuPreference(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(154, i, 0);
    }

    public static void addIsUpdateSaOnStartSupported(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(155, z, false);
    }

    public static void addSupportB2cCallcomposerWithoutFeaturetag(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(MNO.TANGO_LUXEMBOURG, i, 0);
    }

    public static int endRequestUACreation(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 4);
        flatBufferBuilder.required(endObject, 6);
        flatBufferBuilder.required(endObject, 8);
        flatBufferBuilder.required(endObject, 12);
        flatBufferBuilder.required(endObject, 14);
        flatBufferBuilder.required(endObject, 22);
        flatBufferBuilder.required(endObject, 28);
        flatBufferBuilder.required(endObject, 30);
        flatBufferBuilder.required(endObject, 32);
        flatBufferBuilder.required(endObject, 80);
        return endObject;
    }
}
