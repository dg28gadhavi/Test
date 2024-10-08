package org.xbill.DNS;

import java.io.IOException;

public class NSECRecord extends Record {
    private static final long serialVersionUID = -5165065768816265385L;
    private Name next;
    private TypeBitmap types;

    NSECRecord() {
    }

    /* access modifiers changed from: package-private */
    public Record getObject() {
        return new NSECRecord();
    }

    /* access modifiers changed from: package-private */
    public void rrFromWire(DNSInput dNSInput) throws IOException {
        this.next = new Name(dNSInput);
        this.types = new TypeBitmap(dNSInput);
    }

    /* access modifiers changed from: package-private */
    public void rrToWire(DNSOutput dNSOutput, Compression compression, boolean z) {
        this.next.toWire(dNSOutput, (Compression) null, false);
        this.types.toWire(dNSOutput);
    }

    /* access modifiers changed from: package-private */
    public String rrToString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(this.next);
        if (!this.types.empty()) {
            stringBuffer.append(' ');
            stringBuffer.append(this.types.toString());
        }
        return stringBuffer.toString();
    }
}
