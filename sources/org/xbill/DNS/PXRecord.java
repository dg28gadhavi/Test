package org.xbill.DNS;

import java.io.IOException;

public class PXRecord extends Record {
    private static final long serialVersionUID = 1811540008806660667L;
    private Name map822;
    private Name mapX400;
    private int preference;

    PXRecord() {
    }

    /* access modifiers changed from: package-private */
    public Record getObject() {
        return new PXRecord();
    }

    /* access modifiers changed from: package-private */
    public void rrFromWire(DNSInput dNSInput) throws IOException {
        this.preference = dNSInput.readU16();
        this.map822 = new Name(dNSInput);
        this.mapX400 = new Name(dNSInput);
    }

    /* access modifiers changed from: package-private */
    public String rrToString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(this.preference);
        stringBuffer.append(" ");
        stringBuffer.append(this.map822);
        stringBuffer.append(" ");
        stringBuffer.append(this.mapX400);
        return stringBuffer.toString();
    }

    /* access modifiers changed from: package-private */
    public void rrToWire(DNSOutput dNSOutput, Compression compression, boolean z) {
        dNSOutput.writeU16(this.preference);
        this.map822.toWire(dNSOutput, (Compression) null, z);
        this.mapX400.toWire(dNSOutput, (Compression) null, z);
    }
}
