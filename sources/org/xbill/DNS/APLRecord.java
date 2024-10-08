package org.xbill.DNS;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.xbill.DNS.utils.base16;

public class APLRecord extends Record {
    private static final long serialVersionUID = -1348173791712935864L;
    private List elements;

    /* access modifiers changed from: private */
    public static boolean validatePrefixLength(int i, int i2) {
        if (i2 < 0 || i2 >= 256) {
            return false;
        }
        return (i != 1 || i2 <= 32) && (i != 2 || i2 <= 128);
    }

    public static class Element {
        public final Object address;
        public final int family;
        public final boolean negative;
        public final int prefixLength;

        private Element(int i, boolean z, Object obj, int i2) {
            this.family = i;
            this.negative = z;
            this.address = obj;
            this.prefixLength = i2;
            if (!APLRecord.validatePrefixLength(i, i2)) {
                throw new IllegalArgumentException("invalid prefix length");
            }
        }

        public Element(boolean z, InetAddress inetAddress, int i) {
            this(Address.familyOf(inetAddress), z, inetAddress, i);
        }

        public String toString() {
            StringBuffer stringBuffer = new StringBuffer();
            if (this.negative) {
                stringBuffer.append("!");
            }
            stringBuffer.append(this.family);
            stringBuffer.append(":");
            int i = this.family;
            if (i == 1 || i == 2) {
                stringBuffer.append(((InetAddress) this.address).getHostAddress());
            } else {
                stringBuffer.append(base16.toString((byte[]) this.address));
            }
            stringBuffer.append("/");
            stringBuffer.append(this.prefixLength);
            return stringBuffer.toString();
        }

        public boolean equals(Object obj) {
            if (obj == null || !(obj instanceof Element)) {
                return false;
            }
            Element element = (Element) obj;
            if (this.family == element.family && this.negative == element.negative && this.prefixLength == element.prefixLength && this.address.equals(element.address)) {
                return true;
            }
            return false;
        }

        public int hashCode() {
            return this.address.hashCode() + this.prefixLength + (this.negative ? 1 : 0);
        }
    }

    APLRecord() {
    }

    /* access modifiers changed from: package-private */
    public Record getObject() {
        return new APLRecord();
    }

    private static byte[] parseAddress(byte[] bArr, int i) throws WireParseException {
        if (bArr.length > i) {
            throw new WireParseException("invalid address length");
        } else if (bArr.length == i) {
            return bArr;
        } else {
            byte[] bArr2 = new byte[i];
            System.arraycopy(bArr, 0, bArr2, 0, bArr.length);
            return bArr2;
        }
    }

    /* access modifiers changed from: package-private */
    public void rrFromWire(DNSInput dNSInput) throws IOException {
        Element element;
        this.elements = new ArrayList(1);
        while (dNSInput.remaining() != 0) {
            int readU16 = dNSInput.readU16();
            int readU8 = dNSInput.readU8();
            int readU82 = dNSInput.readU8();
            boolean z = (readU82 & 128) != 0;
            byte[] readByteArray = dNSInput.readByteArray(readU82 & -129);
            if (validatePrefixLength(readU16, readU8)) {
                if (readU16 == 1 || readU16 == 2) {
                    element = new Element(z, InetAddress.getByAddress(parseAddress(readByteArray, Address.addressLength(readU16))), readU8);
                } else {
                    element = new Element(readU16, z, readByteArray, readU8);
                }
                this.elements.add(element);
            } else {
                throw new WireParseException("invalid prefix length");
            }
        }
    }

    /* access modifiers changed from: package-private */
    public String rrToString() {
        StringBuffer stringBuffer = new StringBuffer();
        Iterator it = this.elements.iterator();
        while (it.hasNext()) {
            stringBuffer.append((Element) it.next());
            if (it.hasNext()) {
                stringBuffer.append(" ");
            }
        }
        return stringBuffer.toString();
    }

    private static int addressLength(byte[] bArr) {
        for (int length = bArr.length - 1; length >= 0; length--) {
            if (bArr[length] != 0) {
                return length + 1;
            }
        }
        return 0;
    }

    /* access modifiers changed from: package-private */
    public void rrToWire(DNSOutput dNSOutput, Compression compression, boolean z) {
        byte[] bArr;
        int i;
        for (Element element : this.elements) {
            int i2 = element.family;
            if (i2 == 1 || i2 == 2) {
                bArr = ((InetAddress) element.address).getAddress();
                i = addressLength(bArr);
            } else {
                bArr = (byte[]) element.address;
                i = bArr.length;
            }
            int i3 = element.negative ? i | 128 : i;
            dNSOutput.writeU16(element.family);
            dNSOutput.writeU8(element.prefixLength);
            dNSOutput.writeU8(i3);
            dNSOutput.writeByteArray(bArr, 0, i);
        }
    }
}
