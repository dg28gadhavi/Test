package org.xbill.DNS;

import java.io.IOException;

public class HINFORecord extends Record {
    private static final long serialVersionUID = -4732870630947452112L;
    private byte[] cpu;
    private byte[] os;

    HINFORecord() {
    }

    /* access modifiers changed from: package-private */
    public Record getObject() {
        return new HINFORecord();
    }

    /* access modifiers changed from: package-private */
    public void rrFromWire(DNSInput dNSInput) throws IOException {
        this.cpu = dNSInput.readCountedString();
        this.os = dNSInput.readCountedString();
    }

    /* access modifiers changed from: package-private */
    public void rrToWire(DNSOutput dNSOutput, Compression compression, boolean z) {
        dNSOutput.writeCountedString(this.cpu);
        dNSOutput.writeCountedString(this.os);
    }

    /* access modifiers changed from: package-private */
    public String rrToString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(Record.byteArrayToString(this.cpu, true));
        stringBuffer.append(" ");
        stringBuffer.append(Record.byteArrayToString(this.os, true));
        return stringBuffer.toString();
    }
}
