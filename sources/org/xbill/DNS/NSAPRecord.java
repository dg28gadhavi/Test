package org.xbill.DNS;

import java.io.IOException;
import org.xbill.DNS.utils.base16;

public class NSAPRecord extends Record {
    private static final long serialVersionUID = -1037209403185658593L;
    private byte[] address;

    NSAPRecord() {
    }

    /* access modifiers changed from: package-private */
    public Record getObject() {
        return new NSAPRecord();
    }

    /* access modifiers changed from: package-private */
    public void rrFromWire(DNSInput dNSInput) throws IOException {
        this.address = dNSInput.readByteArray();
    }

    /* access modifiers changed from: package-private */
    public void rrToWire(DNSOutput dNSOutput, Compression compression, boolean z) {
        dNSOutput.writeByteArray(this.address);
    }

    /* access modifiers changed from: package-private */
    public String rrToString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("0x");
        stringBuffer.append(base16.toString(this.address));
        return stringBuffer.toString();
    }
}
