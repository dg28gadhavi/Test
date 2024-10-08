package org.xbill.DNS;

import java.io.IOException;

public class X25Record extends Record {
    private static final long serialVersionUID = 4267576252335579764L;
    private byte[] address;

    X25Record() {
    }

    /* access modifiers changed from: package-private */
    public Record getObject() {
        return new X25Record();
    }

    /* access modifiers changed from: package-private */
    public void rrFromWire(DNSInput dNSInput) throws IOException {
        this.address = dNSInput.readCountedString();
    }

    /* access modifiers changed from: package-private */
    public void rrToWire(DNSOutput dNSOutput, Compression compression, boolean z) {
        dNSOutput.writeCountedString(this.address);
    }

    /* access modifiers changed from: package-private */
    public String rrToString() {
        return Record.byteArrayToString(this.address, true);
    }
}
