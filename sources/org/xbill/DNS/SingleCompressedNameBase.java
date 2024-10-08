package org.xbill.DNS;

abstract class SingleCompressedNameBase extends SingleNameBase {
    private static final long serialVersionUID = -236435396815460677L;

    protected SingleCompressedNameBase() {
    }

    /* access modifiers changed from: package-private */
    public void rrToWire(DNSOutput dNSOutput, Compression compression, boolean z) {
        this.singleName.toWire(dNSOutput, compression, z);
    }
}
