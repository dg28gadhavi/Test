package org.xbill.DNS;

import java.io.IOException;

public class SRVRecord extends Record {
    private static final long serialVersionUID = -3886460132387522052L;
    private int port;
    private int priority;
    private Name target;
    private int weight;

    SRVRecord() {
    }

    /* access modifiers changed from: package-private */
    public Record getObject() {
        return new SRVRecord();
    }

    /* access modifiers changed from: package-private */
    public void rrFromWire(DNSInput dNSInput) throws IOException {
        this.priority = dNSInput.readU16();
        this.weight = dNSInput.readU16();
        this.port = dNSInput.readU16();
        this.target = new Name(dNSInput);
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
        StringBuffer stringBuffer4 = new StringBuffer();
        stringBuffer4.append(this.port);
        stringBuffer4.append(" ");
        stringBuffer.append(stringBuffer4.toString());
        stringBuffer.append(this.target);
        return stringBuffer.toString();
    }

    public int getPriority() {
        return this.priority;
    }

    public int getWeight() {
        return this.weight;
    }

    public Name getTarget() {
        return this.target;
    }

    /* access modifiers changed from: package-private */
    public void rrToWire(DNSOutput dNSOutput, Compression compression, boolean z) {
        dNSOutput.writeU16(this.priority);
        dNSOutput.writeU16(this.weight);
        dNSOutput.writeU16(this.port);
        this.target.toWire(dNSOutput, (Compression) null, z);
    }
}
