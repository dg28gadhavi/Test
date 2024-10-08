package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.AdditionalContents;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ComposerData;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ExtraHeader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestMakeCall extends Table {
    public static RequestMakeCall getRootAsRequestMakeCall(ByteBuffer byteBuffer) {
        return getRootAsRequestMakeCall(byteBuffer, new RequestMakeCall());
    }

    public static RequestMakeCall getRootAsRequestMakeCall(ByteBuffer byteBuffer, RequestMakeCall requestMakeCall) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestMakeCall.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestMakeCall __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public long handle() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public String peeruri() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer peeruriAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public int callType() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public int codec() {
        int __offset = __offset(10);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public int mode() {
        int __offset = __offset(12);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public int direction() {
        int __offset = __offset(14);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public String letteringText() {
        int __offset = __offset(16);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer letteringTextAsByteBuffer() {
        return __vector_as_bytebuffer(16, 1);
    }

    public String typeOfEmergencyService() {
        int __offset = __offset(18);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer typeOfEmergencyServiceAsByteBuffer() {
        return __vector_as_bytebuffer(18, 1);
    }

    public String ecscfList(int i) {
        int __offset = __offset(20);
        if (__offset != 0) {
            return __string(__vector(__offset) + (i * 4));
        }
        return null;
    }

    public int ecscfListLength() {
        int __offset = __offset(20);
        if (__offset != 0) {
            return __vector_len(__offset);
        }
        return 0;
    }

    public int ecscfPort() {
        int __offset = __offset(22);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public AdditionalContents additionalContents() {
        return additionalContents(new AdditionalContents());
    }

    public AdditionalContents additionalContents(AdditionalContents additionalContents) {
        int __offset = __offset(24);
        if (__offset != 0) {
            return additionalContents.__assign(__indirect(__offset + this.bb_pos), this.bb);
        }
        return null;
    }

    public String origUri() {
        int __offset = __offset(26);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer origUriAsByteBuffer() {
        return __vector_as_bytebuffer(26, 1);
    }

    public String dispName() {
        int __offset = __offset(28);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer dispNameAsByteBuffer() {
        return __vector_as_bytebuffer(28, 1);
    }

    public String dialedNumber() {
        int __offset = __offset(30);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer dialedNumberAsByteBuffer() {
        return __vector_as_bytebuffer(30, 1);
    }

    public String cli() {
        int __offset = __offset(32);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer cliAsByteBuffer() {
        return __vector_as_bytebuffer(32, 1);
    }

    public String pEmergencyInfo() {
        int __offset = __offset(34);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer pEmergencyInfoAsByteBuffer() {
        return __vector_as_bytebuffer(34, 1);
    }

    public ExtraHeader additionalSipHeaders() {
        return additionalSipHeaders(new ExtraHeader());
    }

    public ExtraHeader additionalSipHeaders(ExtraHeader extraHeader) {
        int __offset = __offset(36);
        if (__offset != 0) {
            return extraHeader.__assign(__indirect(__offset + this.bb_pos), this.bb);
        }
        return null;
    }

    public String alertInfo() {
        int __offset = __offset(38);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer alertInfoAsByteBuffer() {
        return __vector_as_bytebuffer(38, 1);
    }

    public String photoRing() {
        int __offset = __offset(40);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer photoRingAsByteBuffer() {
        return __vector_as_bytebuffer(40, 1);
    }

    public boolean isLteEpsOnlyAttached() {
        int __offset = __offset(42);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public String p2pList(int i) {
        int __offset = __offset(44);
        if (__offset != 0) {
            return __string(__vector(__offset) + (i * 4));
        }
        return null;
    }

    public int p2pListLength() {
        int __offset = __offset(44);
        if (__offset != 0) {
            return __vector_len(__offset);
        }
        return 0;
    }

    public int cmcBoundSessionId() {
        int __offset = __offset(46);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public ComposerData composerData() {
        return composerData(new ComposerData());
    }

    public ComposerData composerData(ComposerData composerData) {
        int __offset = __offset(48);
        if (__offset != 0) {
            return composerData.__assign(__indirect(__offset + this.bb_pos), this.bb);
        }
        return null;
    }

    public String replaceCallId() {
        int __offset = __offset(50);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer replaceCallIdAsByteBuffer() {
        return __vector_as_bytebuffer(50, 1);
    }

    public int cmcEdCallSlot() {
        int __offset = __offset(52);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public String idcExtra() {
        int __offset = __offset(54);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer idcExtraAsByteBuffer() {
        return __vector_as_bytebuffer(54, 1);
    }

    public static int createRequestMakeCall(FlatBufferBuilder flatBufferBuilder, long j, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9, int i10, int i11, int i12, int i13, int i14, int i15, int i16, int i17, int i18, boolean z, int i19, int i20, int i21, int i22, int i23, int i24) {
        FlatBufferBuilder flatBufferBuilder2 = flatBufferBuilder;
        flatBufferBuilder.startObject(26);
        addIdcExtra(flatBufferBuilder, i24);
        addCmcEdCallSlot(flatBufferBuilder, i23);
        addReplaceCallId(flatBufferBuilder, i22);
        addComposerData(flatBufferBuilder, i21);
        addCmcBoundSessionId(flatBufferBuilder, i20);
        addP2pList(flatBufferBuilder, i19);
        addPhotoRing(flatBufferBuilder, i18);
        addAlertInfo(flatBufferBuilder, i17);
        addAdditionalSipHeaders(flatBufferBuilder, i16);
        addPEmergencyInfo(flatBufferBuilder, i15);
        addCli(flatBufferBuilder, i14);
        addDialedNumber(flatBufferBuilder, i13);
        addDispName(flatBufferBuilder, i12);
        int i25 = i11;
        addOrigUri(flatBufferBuilder, i11);
        int i26 = i10;
        addAdditionalContents(flatBufferBuilder, i10);
        int i27 = i9;
        addEcscfPort(flatBufferBuilder, i9);
        int i28 = i8;
        addEcscfList(flatBufferBuilder, i8);
        int i29 = i7;
        addTypeOfEmergencyService(flatBufferBuilder, i7);
        int i30 = i6;
        addLetteringText(flatBufferBuilder, i6);
        int i31 = i5;
        addDirection(flatBufferBuilder, i5);
        int i32 = i4;
        addMode(flatBufferBuilder, i4);
        int i33 = i3;
        addCodec(flatBufferBuilder, i3);
        int i34 = i2;
        addCallType(flatBufferBuilder, i2);
        int i35 = i;
        addPeeruri(flatBufferBuilder, i);
        addHandle(flatBufferBuilder, j);
        addIsLteEpsOnlyAttached(flatBufferBuilder, z);
        return endRequestMakeCall(flatBufferBuilder);
    }

    public static void startRequestMakeCall(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(26);
    }

    public static void addHandle(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addPeeruri(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addCallType(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(2, i, 0);
    }

    public static void addCodec(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(3, i, 0);
    }

    public static void addMode(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(4, i, 0);
    }

    public static void addDirection(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(5, i, 0);
    }

    public static void addLetteringText(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(6, i, 0);
    }

    public static void addTypeOfEmergencyService(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(7, i, 0);
    }

    public static void addEcscfList(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(8, i, 0);
    }

    public static int createEcscfListVector(FlatBufferBuilder flatBufferBuilder, int[] iArr) {
        flatBufferBuilder.startVector(4, iArr.length, 4);
        for (int length = iArr.length - 1; length >= 0; length--) {
            flatBufferBuilder.addOffset(iArr[length]);
        }
        return flatBufferBuilder.endVector();
    }

    public static void startEcscfListVector(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.startVector(4, i, 4);
    }

    public static void addEcscfPort(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(9, i, 0);
    }

    public static void addAdditionalContents(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(10, i, 0);
    }

    public static void addOrigUri(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(11, i, 0);
    }

    public static void addDispName(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(12, i, 0);
    }

    public static void addDialedNumber(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(13, i, 0);
    }

    public static void addCli(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(14, i, 0);
    }

    public static void addPEmergencyInfo(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(15, i, 0);
    }

    public static void addAdditionalSipHeaders(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(16, i, 0);
    }

    public static void addAlertInfo(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(17, i, 0);
    }

    public static void addPhotoRing(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(18, i, 0);
    }

    public static void addIsLteEpsOnlyAttached(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(19, z, false);
    }

    public static void addP2pList(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(20, i, 0);
    }

    public static int createP2pListVector(FlatBufferBuilder flatBufferBuilder, int[] iArr) {
        flatBufferBuilder.startVector(4, iArr.length, 4);
        for (int length = iArr.length - 1; length >= 0; length--) {
            flatBufferBuilder.addOffset(iArr[length]);
        }
        return flatBufferBuilder.endVector();
    }

    public static void startP2pListVector(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.startVector(4, i, 4);
    }

    public static void addCmcBoundSessionId(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(21, i, 0);
    }

    public static void addComposerData(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(22, i, 0);
    }

    public static void addReplaceCallId(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(23, i, 0);
    }

    public static void addCmcEdCallSlot(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(24, i, 0);
    }

    public static void addIdcExtra(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(25, i, 0);
    }

    public static int endRequestMakeCall(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 6);
        return endObject;
    }
}
