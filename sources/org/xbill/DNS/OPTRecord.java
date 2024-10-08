package org.xbill.DNS;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OPTRecord extends Record {
    private static final long serialVersionUID = -6254521894809367938L;
    private List options;

    OPTRecord() {
    }

    /* access modifiers changed from: package-private */
    public Record getObject() {
        return new OPTRecord();
    }

    /* access modifiers changed from: package-private */
    public void rrFromWire(DNSInput dNSInput) throws IOException {
        if (dNSInput.remaining() > 0) {
            this.options = new ArrayList();
        }
        while (dNSInput.remaining() > 0) {
            this.options.add(EDNSOption.fromWire(dNSInput));
        }
    }

    /* access modifiers changed from: package-private */
    public String rrToString() {
        StringBuffer stringBuffer = new StringBuffer();
        List list = this.options;
        if (list != null) {
            stringBuffer.append(list);
            stringBuffer.append(" ");
        }
        stringBuffer.append(" ; payload ");
        stringBuffer.append(getPayloadSize());
        stringBuffer.append(", xrcode ");
        stringBuffer.append(getExtendedRcode());
        stringBuffer.append(", version ");
        stringBuffer.append(getVersion());
        stringBuffer.append(", flags ");
        stringBuffer.append(getFlags());
        return stringBuffer.toString();
    }

    public int getPayloadSize() {
        return this.dclass;
    }

    public int getExtendedRcode() {
        return (int) (this.ttl >>> 24);
    }

    public int getVersion() {
        return (int) ((this.ttl >>> 16) & 255);
    }

    public int getFlags() {
        return (int) (this.ttl & 65535);
    }

    /* access modifiers changed from: package-private */
    public void rrToWire(DNSOutput dNSOutput, Compression compression, boolean z) {
        List<EDNSOption> list = this.options;
        if (list != null) {
            for (EDNSOption wire : list) {
                wire.toWire(dNSOutput);
            }
        }
    }

    public boolean equals(Object obj) {
        return super.equals(obj) && this.ttl == ((OPTRecord) obj).ttl;
    }
}
