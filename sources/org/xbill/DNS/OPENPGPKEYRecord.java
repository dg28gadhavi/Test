package org.xbill.DNS;

import java.io.IOException;
import org.xbill.DNS.utils.base64;

public class OPENPGPKEYRecord extends Record {
    private static final long serialVersionUID = -1277262990243423062L;
    private byte[] cert;

    OPENPGPKEYRecord() {
    }

    /* access modifiers changed from: package-private */
    public Record getObject() {
        return new OPENPGPKEYRecord();
    }

    /* access modifiers changed from: package-private */
    public void rrFromWire(DNSInput dNSInput) throws IOException {
        this.cert = dNSInput.readByteArray();
    }

    /* access modifiers changed from: package-private */
    public String rrToString() {
        StringBuffer stringBuffer = new StringBuffer();
        if (this.cert != null) {
            if (Options.check("multiline")) {
                stringBuffer.append("(\n");
                stringBuffer.append(base64.formatString(this.cert, 64, "\t", true));
            } else {
                stringBuffer.append(base64.toString(this.cert));
            }
        }
        return stringBuffer.toString();
    }

    /* access modifiers changed from: package-private */
    public void rrToWire(DNSOutput dNSOutput, Compression compression, boolean z) {
        dNSOutput.writeByteArray(this.cert);
    }
}
