package org.xbill.DNS;

import java.io.IOException;

public class MINFORecord extends Record {
    private static final long serialVersionUID = -3962147172340353796L;
    private Name errorAddress;
    private Name responsibleAddress;

    MINFORecord() {
    }

    /* access modifiers changed from: package-private */
    public Record getObject() {
        return new MINFORecord();
    }

    /* access modifiers changed from: package-private */
    public void rrFromWire(DNSInput dNSInput) throws IOException {
        this.responsibleAddress = new Name(dNSInput);
        this.errorAddress = new Name(dNSInput);
    }

    /* access modifiers changed from: package-private */
    public String rrToString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(this.responsibleAddress);
        stringBuffer.append(" ");
        stringBuffer.append(this.errorAddress);
        return stringBuffer.toString();
    }

    /* access modifiers changed from: package-private */
    public void rrToWire(DNSOutput dNSOutput, Compression compression, boolean z) {
        this.responsibleAddress.toWire(dNSOutput, (Compression) null, z);
        this.errorAddress.toWire(dNSOutput, (Compression) null, z);
    }
}
