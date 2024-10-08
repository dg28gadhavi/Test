package org.xbill.DNS;

import java.io.IOException;
import org.xbill.DNS.utils.base16;

public class SMIMEARecord extends Record {
    private static final long serialVersionUID = 1640247915216425235L;
    private byte[] certificateAssociationData;
    private int certificateUsage;
    private int matchingType;
    private int selector;

    SMIMEARecord() {
    }

    /* access modifiers changed from: package-private */
    public Record getObject() {
        return new SMIMEARecord();
    }

    /* access modifiers changed from: package-private */
    public void rrFromWire(DNSInput dNSInput) throws IOException {
        this.certificateUsage = dNSInput.readU8();
        this.selector = dNSInput.readU8();
        this.matchingType = dNSInput.readU8();
        this.certificateAssociationData = dNSInput.readByteArray();
    }

    /* access modifiers changed from: package-private */
    public String rrToString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(this.certificateUsage);
        stringBuffer.append(" ");
        stringBuffer.append(this.selector);
        stringBuffer.append(" ");
        stringBuffer.append(this.matchingType);
        stringBuffer.append(" ");
        stringBuffer.append(base16.toString(this.certificateAssociationData));
        return stringBuffer.toString();
    }

    /* access modifiers changed from: package-private */
    public void rrToWire(DNSOutput dNSOutput, Compression compression, boolean z) {
        dNSOutput.writeU8(this.certificateUsage);
        dNSOutput.writeU8(this.selector);
        dNSOutput.writeU8(this.matchingType);
        dNSOutput.writeByteArray(this.certificateAssociationData);
    }
}
