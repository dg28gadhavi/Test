package org.xbill.DNS;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

public final class Address {
    public static String toDottedQuad(byte[] bArr) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(bArr[0] & 255);
        stringBuffer.append(".");
        stringBuffer.append(bArr[1] & 255);
        stringBuffer.append(".");
        stringBuffer.append(bArr[2] & 255);
        stringBuffer.append(".");
        stringBuffer.append(bArr[3] & 255);
        return stringBuffer.toString();
    }

    public static int familyOf(InetAddress inetAddress) {
        if (inetAddress instanceof Inet4Address) {
            return 1;
        }
        if (inetAddress instanceof Inet6Address) {
            return 2;
        }
        throw new IllegalArgumentException("unknown address family");
    }

    public static int addressLength(int i) {
        if (i == 1) {
            return 4;
        }
        if (i == 2) {
            return 16;
        }
        throw new IllegalArgumentException("unknown address family");
    }

    public static InetAddress truncate(InetAddress inetAddress, int i) {
        int i2;
        int addressLength = addressLength(familyOf(inetAddress)) * 8;
        if (i < 0 || i > addressLength) {
            throw new IllegalArgumentException("invalid mask length");
        } else if (i == addressLength) {
            return inetAddress;
        } else {
            byte[] address = inetAddress.getAddress();
            int i3 = i / 8;
            int i4 = i3 + 1;
            while (true) {
                if (i4 >= address.length) {
                    break;
                }
                address[i4] = 0;
                i4++;
            }
            byte b = 0;
            for (i2 = 0; i2 < i % 8; i2++) {
                b |= 1 << (7 - i2);
            }
            address[i3] = (byte) (address[i3] & b);
            try {
                return InetAddress.getByAddress(address);
            } catch (UnknownHostException unused) {
                throw new IllegalArgumentException("invalid address");
            }
        }
    }
}
