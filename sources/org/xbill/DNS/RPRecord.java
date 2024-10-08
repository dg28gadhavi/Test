package org.xbill.DNS;

import java.io.IOException;

public class RPRecord extends Record {
    private static final long serialVersionUID = 8124584364211337460L;
    private Name mailbox;
    private Name textDomain;

    RPRecord() {
    }

    /* access modifiers changed from: package-private */
    public Record getObject() {
        return new RPRecord();
    }

    /* access modifiers changed from: package-private */
    public void rrFromWire(DNSInput dNSInput) throws IOException {
        this.mailbox = new Name(dNSInput);
        this.textDomain = new Name(dNSInput);
    }

    /* access modifiers changed from: package-private */
    public String rrToString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(this.mailbox);
        stringBuffer.append(" ");
        stringBuffer.append(this.textDomain);
        return stringBuffer.toString();
    }

    /* access modifiers changed from: package-private */
    public void rrToWire(DNSOutput dNSOutput, Compression compression, boolean z) {
        this.mailbox.toWire(dNSOutput, (Compression) null, z);
        this.textDomain.toWire(dNSOutput, (Compression) null, z);
    }
}
