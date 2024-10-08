package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class X509CertVerifyRequest extends Table {
    public static X509CertVerifyRequest getRootAsX509CertVerifyRequest(ByteBuffer byteBuffer) {
        return getRootAsX509CertVerifyRequest(byteBuffer, new X509CertVerifyRequest());
    }

    public static X509CertVerifyRequest getRootAsX509CertVerifyRequest(ByteBuffer byteBuffer, X509CertVerifyRequest x509CertVerifyRequest) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return x509CertVerifyRequest.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public X509CertVerifyRequest __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public Cert cert(int i) {
        return cert(new Cert(), i);
    }

    public Cert cert(Cert cert, int i) {
        int __offset = __offset(4);
        if (__offset != 0) {
            return cert.__assign(__indirect(__vector(__offset) + (i * 4)), this.bb);
        }
        return null;
    }

    public int certLength() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return __vector_len(__offset);
        }
        return 0;
    }

    public String keyExchangeAlgo() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer keyExchangeAlgoAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public static int createX509CertVerifyRequest(FlatBufferBuilder flatBufferBuilder, int i, int i2) {
        flatBufferBuilder.startObject(2);
        addKeyExchangeAlgo(flatBufferBuilder, i2);
        addCert(flatBufferBuilder, i);
        return endX509CertVerifyRequest(flatBufferBuilder);
    }

    public static void startX509CertVerifyRequest(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(2);
    }

    public static void addCert(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(0, i, 0);
    }

    public static int createCertVector(FlatBufferBuilder flatBufferBuilder, int[] iArr) {
        flatBufferBuilder.startVector(4, iArr.length, 4);
        for (int length = iArr.length - 1; length >= 0; length--) {
            flatBufferBuilder.addOffset(iArr[length]);
        }
        return flatBufferBuilder.endVector();
    }

    public static void startCertVector(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.startVector(4, i, 4);
    }

    public static void addKeyExchangeAlgo(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static int endX509CertVerifyRequest(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
