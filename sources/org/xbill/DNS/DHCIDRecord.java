package org.xbill.DNS;

import java.io.IOException;
import org.xbill.DNS.utils.base64;

public class DHCIDRecord extends Record {
    private static final long serialVersionUID = -8214820200808997707L;
    private byte[] data;

    DHCIDRecord() {
    }

    /* access modifiers changed from: package-private */
    public Record getObject() {
        return new DHCIDRecord();
    }

    /* access modifiers changed from: package-private */
    public void rrFromWire(DNSInput dNSInput) throws IOException {
        this.data = dNSInput.readByteArray();
    }

    /* access modifiers changed from: package-private */
    public void rrToWire(DNSOutput dNSOutput, Compression compression, boolean z) {
        dNSOutput.writeByteArray(this.data);
    }

    /* access modifiers changed from: package-private */
    public String rrToString() {
        return base64.toString(this.data);
    }
}
