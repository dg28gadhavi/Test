package org.xbill.DNS;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

abstract class TXTBase extends Record {
    private static final long serialVersionUID = -4319510507246305931L;
    protected List strings;

    protected TXTBase() {
    }

    /* access modifiers changed from: package-private */
    public void rrFromWire(DNSInput dNSInput) throws IOException {
        this.strings = new ArrayList(2);
        while (dNSInput.remaining() > 0) {
            this.strings.add(dNSInput.readCountedString());
        }
    }

    /* access modifiers changed from: package-private */
    public String rrToString() {
        StringBuffer stringBuffer = new StringBuffer();
        Iterator it = this.strings.iterator();
        while (it.hasNext()) {
            stringBuffer.append(Record.byteArrayToString((byte[]) it.next(), true));
            if (it.hasNext()) {
                stringBuffer.append(" ");
            }
        }
        return stringBuffer.toString();
    }

    /* access modifiers changed from: package-private */
    public void rrToWire(DNSOutput dNSOutput, Compression compression, boolean z) {
        for (byte[] writeCountedString : this.strings) {
            dNSOutput.writeCountedString(writeCountedString);
        }
    }
}
