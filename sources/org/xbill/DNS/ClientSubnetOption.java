package org.xbill.DNS;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class ClientSubnetOption extends EDNSOption {
    private InetAddress address;
    private int family;
    private int scopeNetmask;
    private int sourceNetmask;

    ClientSubnetOption() {
        super(8);
    }

    /* access modifiers changed from: package-private */
    public void optionFromWire(DNSInput dNSInput) throws WireParseException {
        int readU16 = dNSInput.readU16();
        this.family = readU16;
        if (readU16 == 1 || readU16 == 2) {
            int readU8 = dNSInput.readU8();
            this.sourceNetmask = readU8;
            if (readU8 <= Address.addressLength(this.family) * 8) {
                int readU82 = dNSInput.readU8();
                this.scopeNetmask = readU82;
                if (readU82 <= Address.addressLength(this.family) * 8) {
                    byte[] readByteArray = dNSInput.readByteArray();
                    if (readByteArray.length == (this.sourceNetmask + 7) / 8) {
                        byte[] bArr = new byte[Address.addressLength(this.family)];
                        System.arraycopy(readByteArray, 0, bArr, 0, readByteArray.length);
                        try {
                            InetAddress byAddress = InetAddress.getByAddress(bArr);
                            this.address = byAddress;
                            if (!Address.truncate(byAddress, this.sourceNetmask).equals(this.address)) {
                                throw new WireParseException("invalid padding");
                            }
                        } catch (UnknownHostException e) {
                            throw new WireParseException("invalid address", e);
                        }
                    } else {
                        throw new WireParseException("invalid address");
                    }
                } else {
                    throw new WireParseException("invalid scope netmask");
                }
            } else {
                throw new WireParseException("invalid source netmask");
            }
        } else {
            throw new WireParseException("unknown address family");
        }
    }

    /* access modifiers changed from: package-private */
    public void optionToWire(DNSOutput dNSOutput) {
        dNSOutput.writeU16(this.family);
        dNSOutput.writeU8(this.sourceNetmask);
        dNSOutput.writeU8(this.scopeNetmask);
        dNSOutput.writeByteArray(this.address.getAddress(), 0, (this.sourceNetmask + 7) / 8);
    }

    /* access modifiers changed from: package-private */
    public String optionToString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(this.address.getHostAddress());
        stringBuffer.append("/");
        stringBuffer.append(this.sourceNetmask);
        stringBuffer.append(", scope netmask ");
        stringBuffer.append(this.scopeNetmask);
        return stringBuffer.toString();
    }
}
