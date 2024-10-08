package org.xbill.DNS;

import java.io.IOException;
import java.security.PublicKey;
import org.xbill.DNS.utils.base64;

abstract class KEYBase extends Record {
    private static final long serialVersionUID = 3469321722693285454L;
    protected int alg;
    protected int flags;
    protected int footprint = -1;
    protected byte[] key;
    protected int proto;
    protected PublicKey publicKey = null;

    protected KEYBase() {
    }

    /* access modifiers changed from: package-private */
    public void rrFromWire(DNSInput dNSInput) throws IOException {
        this.flags = dNSInput.readU16();
        this.proto = dNSInput.readU8();
        this.alg = dNSInput.readU8();
        if (dNSInput.remaining() > 0) {
            this.key = dNSInput.readByteArray();
        }
    }

    /* access modifiers changed from: package-private */
    public String rrToString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(this.flags);
        stringBuffer.append(" ");
        stringBuffer.append(this.proto);
        stringBuffer.append(" ");
        stringBuffer.append(this.alg);
        if (this.key != null) {
            if (Options.check("multiline")) {
                stringBuffer.append(" (\n");
                stringBuffer.append(base64.formatString(this.key, 64, "\t", true));
                stringBuffer.append(" ; key_tag = ");
                stringBuffer.append(getFootprint());
            } else {
                stringBuffer.append(" ");
                stringBuffer.append(base64.toString(this.key));
            }
        }
        return stringBuffer.toString();
    }

    public int getFootprint() {
        int i;
        byte b;
        int i2 = this.footprint;
        if (i2 >= 0) {
            return i2;
        }
        DNSOutput dNSOutput = new DNSOutput();
        int i3 = 0;
        rrToWire(dNSOutput, (Compression) null, false);
        byte[] byteArray = dNSOutput.toByteArray();
        if (this.alg == 1) {
            b = byteArray[byteArray.length - 2] & 255;
            i = (byteArray[byteArray.length - 3] & 255) << 8;
        } else {
            i = 0;
            while (i3 < byteArray.length - 1) {
                i += ((byteArray[i3] & 255) << 8) + (byteArray[i3 + 1] & 255);
                i3 += 2;
            }
            if (i3 < byteArray.length) {
                i += (byteArray[i3] & 255) << 8;
            }
            b = (i >> 16) & 65535;
        }
        int i4 = (i + b) & 65535;
        this.footprint = i4;
        return i4;
    }

    /* access modifiers changed from: package-private */
    public void rrToWire(DNSOutput dNSOutput, Compression compression, boolean z) {
        dNSOutput.writeU16(this.flags);
        dNSOutput.writeU8(this.proto);
        dNSOutput.writeU8(this.alg);
        byte[] bArr = this.key;
        if (bArr != null) {
            dNSOutput.writeByteArray(bArr);
        }
    }
}
