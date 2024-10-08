package org.xbill.DNS;

public class MXRecord extends U16NameBase {
    private static final long serialVersionUID = 2914841027584208546L;

    MXRecord() {
    }

    /* access modifiers changed from: package-private */
    public Record getObject() {
        return new MXRecord();
    }

    /* access modifiers changed from: package-private */
    public void rrToWire(DNSOutput dNSOutput, Compression compression, boolean z) {
        dNSOutput.writeU16(this.u16Field);
        this.nameField.toWire(dNSOutput, compression, z);
    }
}
