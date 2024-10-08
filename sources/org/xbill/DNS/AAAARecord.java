package org.xbill.DNS;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class AAAARecord extends Record {
    private static final long serialVersionUID = -4588601512069748050L;
    private byte[] address;

    AAAARecord() {
    }

    /* access modifiers changed from: package-private */
    public Record getObject() {
        return new AAAARecord();
    }

    /* access modifiers changed from: package-private */
    public void rrFromWire(DNSInput dNSInput) throws IOException {
        this.address = dNSInput.readByteArray(16);
    }

    /* access modifiers changed from: package-private */
    public String rrToString() {
        try {
            InetAddress byAddress = InetAddress.getByAddress((String) null, this.address);
            if (byAddress.getAddress().length != 4) {
                return byAddress.getHostAddress();
            }
            StringBuffer stringBuffer = new StringBuffer("0:0:0:0:0:ffff:");
            byte[] bArr = this.address;
            int i = ((bArr[12] & 255) << 8) + (bArr[13] & 255);
            int i2 = ((bArr[14] & 255) << 8) + (bArr[15] & 255);
            stringBuffer.append(Integer.toHexString(i));
            stringBuffer.append(':');
            stringBuffer.append(Integer.toHexString(i2));
            return stringBuffer.toString();
        } catch (UnknownHostException unused) {
            return null;
        }
    }

    public InetAddress getAddress() {
        try {
            Name name = this.name;
            if (name == null) {
                return InetAddress.getByAddress(this.address);
            }
            return InetAddress.getByAddress(name.toString(), this.address);
        } catch (UnknownHostException unused) {
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    public void rrToWire(DNSOutput dNSOutput, Compression compression, boolean z) {
        dNSOutput.writeByteArray(this.address);
    }
}
