package org.xbill.DNS;

import java.io.IOException;

public class NULLRecord extends Record {
    private static final long serialVersionUID = -5796493183235216538L;
    private byte[] data;

    NULLRecord() {
    }

    /* access modifiers changed from: package-private */
    public Record getObject() {
        return new NULLRecord();
    }

    /* access modifiers changed from: package-private */
    public void rrFromWire(DNSInput dNSInput) throws IOException {
        this.data = dNSInput.readByteArray();
    }

    /* access modifiers changed from: package-private */
    public String rrToString() {
        return Record.unknownToString(this.data);
    }

    /* access modifiers changed from: package-private */
    public void rrToWire(DNSOutput dNSOutput, Compression compression, boolean z) {
        dNSOutput.writeByteArray(this.data);
    }
}
