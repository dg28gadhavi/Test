package org.xbill.DNS;

import java.io.IOException;
import java.util.ArrayList;

public class WKSRecord extends Record {
    private static final long serialVersionUID = -9104259763909119805L;
    private byte[] address;
    private int protocol;
    private int[] services;

    WKSRecord() {
    }

    /* access modifiers changed from: package-private */
    public Record getObject() {
        return new WKSRecord();
    }

    /* access modifiers changed from: package-private */
    public void rrFromWire(DNSInput dNSInput) throws IOException {
        this.address = dNSInput.readByteArray(4);
        this.protocol = dNSInput.readU8();
        byte[] readByteArray = dNSInput.readByteArray();
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < readByteArray.length; i++) {
            for (int i2 = 0; i2 < 8; i2++) {
                if ((readByteArray[i] & 255 & (1 << (7 - i2))) != 0) {
                    arrayList.add(new Integer((i * 8) + i2));
                }
            }
        }
        this.services = new int[arrayList.size()];
        for (int i3 = 0; i3 < arrayList.size(); i3++) {
            this.services[i3] = ((Integer) arrayList.get(i3)).intValue();
        }
    }

    /* access modifiers changed from: package-private */
    public String rrToString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(Address.toDottedQuad(this.address));
        stringBuffer.append(" ");
        stringBuffer.append(this.protocol);
        for (int append : this.services) {
            StringBuffer stringBuffer2 = new StringBuffer();
            stringBuffer2.append(" ");
            stringBuffer2.append(append);
            stringBuffer.append(stringBuffer2.toString());
        }
        return stringBuffer.toString();
    }

    /* access modifiers changed from: package-private */
    public void rrToWire(DNSOutput dNSOutput, Compression compression, boolean z) {
        dNSOutput.writeByteArray(this.address);
        dNSOutput.writeU8(this.protocol);
        int[] iArr = this.services;
        byte[] bArr = new byte[((iArr[iArr.length - 1] / 8) + 1)];
        int i = 0;
        while (true) {
            int[] iArr2 = this.services;
            if (i < iArr2.length) {
                int i2 = iArr2[i];
                int i3 = i2 / 8;
                bArr[i3] = (byte) ((1 << (7 - (i2 % 8))) | bArr[i3]);
                i++;
            } else {
                dNSOutput.writeByteArray(bArr);
                return;
            }
        }
    }
}
