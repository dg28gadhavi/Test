package org.xbill.DNS;

import java.io.IOException;

public class CAARecord extends Record {
    private static final long serialVersionUID = 8544304287274216443L;
    private int flags;
    private byte[] tag;
    private byte[] value;

    CAARecord() {
    }

    /* access modifiers changed from: package-private */
    public Record getObject() {
        return new CAARecord();
    }

    /* access modifiers changed from: package-private */
    public void rrFromWire(DNSInput dNSInput) throws IOException {
        this.flags = dNSInput.readU8();
        this.tag = dNSInput.readCountedString();
        this.value = dNSInput.readByteArray();
    }

    /* access modifiers changed from: package-private */
    public String rrToString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(this.flags);
        stringBuffer.append(" ");
        stringBuffer.append(Record.byteArrayToString(this.tag, false));
        stringBuffer.append(" ");
        stringBuffer.append(Record.byteArrayToString(this.value, true));
        return stringBuffer.toString();
    }

    /* access modifiers changed from: package-private */
    public void rrToWire(DNSOutput dNSOutput, Compression compression, boolean z) {
        dNSOutput.writeU8(this.flags);
        dNSOutput.writeCountedString(this.tag);
        dNSOutput.writeByteArray(this.value);
    }
}
