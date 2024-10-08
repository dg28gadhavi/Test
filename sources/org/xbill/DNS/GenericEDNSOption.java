package org.xbill.DNS;

import java.io.IOException;
import org.xbill.DNS.utils.base16;

public class GenericEDNSOption extends EDNSOption {
    private byte[] data;

    GenericEDNSOption(int i) {
        super(i);
    }

    /* access modifiers changed from: package-private */
    public void optionFromWire(DNSInput dNSInput) throws IOException {
        this.data = dNSInput.readByteArray();
    }

    /* access modifiers changed from: package-private */
    public void optionToWire(DNSOutput dNSOutput) {
        dNSOutput.writeByteArray(this.data);
    }

    /* access modifiers changed from: package-private */
    public String optionToString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("<");
        stringBuffer.append(base16.toString(this.data));
        stringBuffer.append(">");
        return stringBuffer.toString();
    }
}
