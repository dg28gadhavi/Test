package org.xbill.DNS;

import java.io.IOException;

public class ISDNRecord extends Record {
    private static final long serialVersionUID = -8730801385178968798L;
    private byte[] address;
    private byte[] subAddress;

    ISDNRecord() {
    }

    /* access modifiers changed from: package-private */
    public Record getObject() {
        return new ISDNRecord();
    }

    /* access modifiers changed from: package-private */
    public void rrFromWire(DNSInput dNSInput) throws IOException {
        this.address = dNSInput.readCountedString();
        if (dNSInput.remaining() > 0) {
            this.subAddress = dNSInput.readCountedString();
        }
    }

    /* access modifiers changed from: package-private */
    public void rrToWire(DNSOutput dNSOutput, Compression compression, boolean z) {
        dNSOutput.writeCountedString(this.address);
        byte[] bArr = this.subAddress;
        if (bArr != null) {
            dNSOutput.writeCountedString(bArr);
        }
    }

    /* access modifiers changed from: package-private */
    public String rrToString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(Record.byteArrayToString(this.address, true));
        if (this.subAddress != null) {
            stringBuffer.append(" ");
            stringBuffer.append(Record.byteArrayToString(this.subAddress, true));
        }
        return stringBuffer.toString();
    }
}
