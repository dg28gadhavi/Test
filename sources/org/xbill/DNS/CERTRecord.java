package org.xbill.DNS;

import java.io.IOException;
import org.xbill.DNS.utils.base64;

public class CERTRecord extends Record {
    private static final long serialVersionUID = 4763014646517016835L;
    private int alg;
    private byte[] cert;
    private int certType;
    private int keyTag;

    CERTRecord() {
    }

    /* access modifiers changed from: package-private */
    public Record getObject() {
        return new CERTRecord();
    }

    /* access modifiers changed from: package-private */
    public void rrFromWire(DNSInput dNSInput) throws IOException {
        this.certType = dNSInput.readU16();
        this.keyTag = dNSInput.readU16();
        this.alg = dNSInput.readU8();
        this.cert = dNSInput.readByteArray();
    }

    /* access modifiers changed from: package-private */
    public String rrToString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(this.certType);
        stringBuffer.append(" ");
        stringBuffer.append(this.keyTag);
        stringBuffer.append(" ");
        stringBuffer.append(this.alg);
        if (this.cert != null) {
            if (Options.check("multiline")) {
                stringBuffer.append(" (\n");
                stringBuffer.append(base64.formatString(this.cert, 64, "\t", true));
            } else {
                stringBuffer.append(" ");
                stringBuffer.append(base64.toString(this.cert));
            }
        }
        return stringBuffer.toString();
    }

    /* access modifiers changed from: package-private */
    public void rrToWire(DNSOutput dNSOutput, Compression compression, boolean z) {
        dNSOutput.writeU16(this.certType);
        dNSOutput.writeU16(this.keyTag);
        dNSOutput.writeU8(this.alg);
        dNSOutput.writeByteArray(this.cert);
    }
}
