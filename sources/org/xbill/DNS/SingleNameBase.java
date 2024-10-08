package org.xbill.DNS;

import java.io.IOException;

abstract class SingleNameBase extends Record {
    private static final long serialVersionUID = -18595042501413L;
    protected Name singleName;

    protected SingleNameBase() {
    }

    /* access modifiers changed from: package-private */
    public void rrFromWire(DNSInput dNSInput) throws IOException {
        this.singleName = new Name(dNSInput);
    }

    /* access modifiers changed from: package-private */
    public String rrToString() {
        return this.singleName.toString();
    }

    /* access modifiers changed from: protected */
    public Name getSingleName() {
        return this.singleName;
    }

    /* access modifiers changed from: package-private */
    public void rrToWire(DNSOutput dNSOutput, Compression compression, boolean z) {
        this.singleName.toWire(dNSOutput, (Compression) null, z);
    }
}
