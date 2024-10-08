package org.xbill.DNS;

import java.io.IOException;
import java.util.Date;
import org.xbill.DNS.utils.base64;

abstract class SIGBase extends Record {
    private static final long serialVersionUID = -3738444391533812369L;
    protected int alg;
    protected int covered;
    protected Date expire;
    protected int footprint;
    protected int labels;
    protected long origttl;
    protected byte[] signature;
    protected Name signer;
    protected Date timeSigned;

    protected SIGBase() {
    }

    /* access modifiers changed from: package-private */
    public void rrFromWire(DNSInput dNSInput) throws IOException {
        this.covered = dNSInput.readU16();
        this.alg = dNSInput.readU8();
        this.labels = dNSInput.readU8();
        this.origttl = dNSInput.readU32();
        this.expire = new Date(dNSInput.readU32() * 1000);
        this.timeSigned = new Date(dNSInput.readU32() * 1000);
        this.footprint = dNSInput.readU16();
        this.signer = new Name(dNSInput);
        this.signature = dNSInput.readByteArray();
    }

    /* access modifiers changed from: package-private */
    public String rrToString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(Type.string(this.covered));
        stringBuffer.append(" ");
        stringBuffer.append(this.alg);
        stringBuffer.append(" ");
        stringBuffer.append(this.labels);
        stringBuffer.append(" ");
        stringBuffer.append(this.origttl);
        stringBuffer.append(" ");
        if (Options.check("multiline")) {
            stringBuffer.append("(\n\t");
        }
        stringBuffer.append(FormattedTime.format(this.expire));
        stringBuffer.append(" ");
        stringBuffer.append(FormattedTime.format(this.timeSigned));
        stringBuffer.append(" ");
        stringBuffer.append(this.footprint);
        stringBuffer.append(" ");
        stringBuffer.append(this.signer);
        if (Options.check("multiline")) {
            stringBuffer.append("\n");
            stringBuffer.append(base64.formatString(this.signature, 64, "\t", true));
        } else {
            stringBuffer.append(" ");
            stringBuffer.append(base64.toString(this.signature));
        }
        return stringBuffer.toString();
    }

    public int getTypeCovered() {
        return this.covered;
    }

    /* access modifiers changed from: package-private */
    public void rrToWire(DNSOutput dNSOutput, Compression compression, boolean z) {
        dNSOutput.writeU16(this.covered);
        dNSOutput.writeU8(this.alg);
        dNSOutput.writeU8(this.labels);
        dNSOutput.writeU32(this.origttl);
        dNSOutput.writeU32(this.expire.getTime() / 1000);
        dNSOutput.writeU32(this.timeSigned.getTime() / 1000);
        dNSOutput.writeU16(this.footprint);
        this.signer.toWire(dNSOutput, (Compression) null, z);
        dNSOutput.writeByteArray(this.signature);
    }
}
