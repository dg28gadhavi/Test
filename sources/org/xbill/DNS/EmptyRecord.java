package org.xbill.DNS;

import java.io.IOException;

class EmptyRecord extends Record {
    private static final long serialVersionUID = 3601852050646429582L;

    /* access modifiers changed from: package-private */
    public void rrFromWire(DNSInput dNSInput) throws IOException {
    }

    /* access modifiers changed from: package-private */
    public String rrToString() {
        return "";
    }

    /* access modifiers changed from: package-private */
    public void rrToWire(DNSOutput dNSOutput, Compression compression, boolean z) {
    }

    EmptyRecord() {
    }

    /* access modifiers changed from: package-private */
    public Record getObject() {
        return new EmptyRecord();
    }
}
