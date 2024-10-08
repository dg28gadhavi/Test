package org.xbill.DNS;

import java.io.IOException;
import java.net.InetAddress;
import org.xbill.DNS.utils.base64;

public class IPSECKEYRecord extends Record {
    private static final long serialVersionUID = 3050449702765909687L;
    private int algorithmType;
    private Object gateway;
    private int gatewayType;
    private byte[] key;
    private int precedence;

    IPSECKEYRecord() {
    }

    /* access modifiers changed from: package-private */
    public Record getObject() {
        return new IPSECKEYRecord();
    }

    /* access modifiers changed from: package-private */
    public void rrFromWire(DNSInput dNSInput) throws IOException {
        this.precedence = dNSInput.readU8();
        this.gatewayType = dNSInput.readU8();
        this.algorithmType = dNSInput.readU8();
        int i = this.gatewayType;
        if (i == 0) {
            this.gateway = null;
        } else if (i == 1) {
            this.gateway = InetAddress.getByAddress(dNSInput.readByteArray(4));
        } else if (i == 2) {
            this.gateway = InetAddress.getByAddress(dNSInput.readByteArray(16));
        } else if (i == 3) {
            this.gateway = new Name(dNSInput);
        } else {
            throw new WireParseException("invalid gateway type");
        }
        if (dNSInput.remaining() > 0) {
            this.key = dNSInput.readByteArray();
        }
    }

    /* access modifiers changed from: package-private */
    public String rrToString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(this.precedence);
        stringBuffer.append(" ");
        stringBuffer.append(this.gatewayType);
        stringBuffer.append(" ");
        stringBuffer.append(this.algorithmType);
        stringBuffer.append(" ");
        int i = this.gatewayType;
        if (i == 0) {
            stringBuffer.append(".");
        } else if (i == 1 || i == 2) {
            stringBuffer.append(((InetAddress) this.gateway).getHostAddress());
        } else if (i == 3) {
            stringBuffer.append(this.gateway);
        }
        if (this.key != null) {
            stringBuffer.append(" ");
            stringBuffer.append(base64.toString(this.key));
        }
        return stringBuffer.toString();
    }

    /* access modifiers changed from: package-private */
    public void rrToWire(DNSOutput dNSOutput, Compression compression, boolean z) {
        dNSOutput.writeU8(this.precedence);
        dNSOutput.writeU8(this.gatewayType);
        dNSOutput.writeU8(this.algorithmType);
        int i = this.gatewayType;
        if (i == 1 || i == 2) {
            dNSOutput.writeByteArray(((InetAddress) this.gateway).getAddress());
        } else if (i == 3) {
            ((Name) this.gateway).toWire(dNSOutput, (Compression) null, z);
        }
        byte[] bArr = this.key;
        if (bArr != null) {
            dNSOutput.writeByteArray(bArr);
        }
    }
}
