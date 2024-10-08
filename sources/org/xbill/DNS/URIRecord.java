package org.xbill.DNS;

import java.io.IOException;

public class URIRecord extends Record {
    private static final long serialVersionUID = 7955422413971804232L;
    private int priority;
    private byte[] target = new byte[0];
    private int weight;

    URIRecord() {
    }

    /* access modifiers changed from: package-private */
    public Record getObject() {
        return new URIRecord();
    }

    /* access modifiers changed from: package-private */
    public void rrFromWire(DNSInput dNSInput) throws IOException {
        this.priority = dNSInput.readU16();
        this.weight = dNSInput.readU16();
        this.target = dNSInput.readByteArray();
    }

    /* access modifiers changed from: package-private */
    public String rrToString() {
        StringBuffer stringBuffer = new StringBuffer();
        StringBuffer stringBuffer2 = new StringBuffer();
        stringBuffer2.append(this.priority);
        stringBuffer2.append(" ");
        stringBuffer.append(stringBuffer2.toString());
        StringBuffer stringBuffer3 = new StringBuffer();
        stringBuffer3.append(this.weight);
        stringBuffer3.append(" ");
        stringBuffer.append(stringBuffer3.toString());
        stringBuffer.append(Record.byteArrayToString(this.target, true));
        return stringBuffer.toString();
    }

    /* access modifiers changed from: package-private */
    public void rrToWire(DNSOutput dNSOutput, Compression compression, boolean z) {
        dNSOutput.writeU16(this.priority);
        dNSOutput.writeU16(this.weight);
        dNSOutput.writeByteArray(this.target);
    }
}
