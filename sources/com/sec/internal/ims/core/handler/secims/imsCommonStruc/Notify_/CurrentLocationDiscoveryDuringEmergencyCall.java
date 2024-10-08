package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.AdditionalContents;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class CurrentLocationDiscoveryDuringEmergencyCall extends Table {
    public static CurrentLocationDiscoveryDuringEmergencyCall getRootAsCurrentLocationDiscoveryDuringEmergencyCall(ByteBuffer byteBuffer) {
        return getRootAsCurrentLocationDiscoveryDuringEmergencyCall(byteBuffer, new CurrentLocationDiscoveryDuringEmergencyCall());
    }

    public static CurrentLocationDiscoveryDuringEmergencyCall getRootAsCurrentLocationDiscoveryDuringEmergencyCall(ByteBuffer byteBuffer, CurrentLocationDiscoveryDuringEmergencyCall currentLocationDiscoveryDuringEmergencyCall) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return currentLocationDiscoveryDuringEmergencyCall.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public CurrentLocationDiscoveryDuringEmergencyCall __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public int sessionId() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public AdditionalContents additionalContents() {
        return additionalContents(new AdditionalContents());
    }

    public AdditionalContents additionalContents(AdditionalContents additionalContents) {
        int __offset = __offset(6);
        if (__offset != 0) {
            return additionalContents.__assign(__indirect(__offset + this.bb_pos), this.bb);
        }
        return null;
    }

    public static int createCurrentLocationDiscoveryDuringEmergencyCall(FlatBufferBuilder flatBufferBuilder, int i, int i2) {
        flatBufferBuilder.startObject(2);
        addAdditionalContents(flatBufferBuilder, i2);
        addSessionId(flatBufferBuilder, i);
        return endCurrentLocationDiscoveryDuringEmergencyCall(flatBufferBuilder);
    }

    public static void startCurrentLocationDiscoveryDuringEmergencyCall(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(2);
    }

    public static void addSessionId(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(0, i, 0);
    }

    public static void addAdditionalContents(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static int endCurrentLocationDiscoveryDuringEmergencyCall(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
