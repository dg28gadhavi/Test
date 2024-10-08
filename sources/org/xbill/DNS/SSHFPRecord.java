package org.xbill.DNS;

import java.io.IOException;
import org.xbill.DNS.utils.base16;

public class SSHFPRecord extends Record {
    private static final long serialVersionUID = -8104701402654687025L;
    private int alg;
    private int digestType;
    private byte[] fingerprint;

    SSHFPRecord() {
    }

    /* access modifiers changed from: package-private */
    public Record getObject() {
        return new SSHFPRecord();
    }

    /* access modifiers changed from: package-private */
    public void rrFromWire(DNSInput dNSInput) throws IOException {
        this.alg = dNSInput.readU8();
        this.digestType = dNSInput.readU8();
        this.fingerprint = dNSInput.readByteArray();
    }

    /* access modifiers changed from: package-private */
    public String rrToString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(this.alg);
        stringBuffer.append(" ");
        stringBuffer.append(this.digestType);
        stringBuffer.append(" ");
        stringBuffer.append(base16.toString(this.fingerprint));
        return stringBuffer.toString();
    }

    /* access modifiers changed from: package-private */
    public void rrToWire(DNSOutput dNSOutput, Compression compression, boolean z) {
        dNSOutput.writeU8(this.alg);
        dNSOutput.writeU8(this.digestType);
        dNSOutput.writeByteArray(this.fingerprint);
    }
}
