package org.xbill.DNS;

import java.io.IOException;
import java.util.Random;

public class Header implements Cloneable {
    private static Random random = new Random();
    private int[] counts;
    private int flags;
    private int id;

    private void init() {
        this.counts = new int[4];
        this.flags = 0;
        this.id = -1;
    }

    public Header(int i) {
        init();
        setID(i);
    }

    public Header() {
        init();
    }

    Header(DNSInput dNSInput) throws IOException {
        this(dNSInput.readU16());
        this.flags = dNSInput.readU16();
        int i = 0;
        while (true) {
            int[] iArr = this.counts;
            if (i < iArr.length) {
                iArr[i] = dNSInput.readU16();
                i++;
            } else {
                return;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void toWire(DNSOutput dNSOutput) {
        dNSOutput.writeU16(getID());
        dNSOutput.writeU16(this.flags);
        int i = 0;
        while (true) {
            int[] iArr = this.counts;
            if (i < iArr.length) {
                dNSOutput.writeU16(iArr[i]);
                i++;
            } else {
                return;
            }
        }
    }

    private static boolean validFlag(int i) {
        return i >= 0 && i <= 15 && Flags.isFlag(i);
    }

    private static void checkFlag(int i) {
        if (!validFlag(i)) {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("invalid flag bit ");
            stringBuffer.append(i);
            throw new IllegalArgumentException(stringBuffer.toString());
        }
    }

    static int setFlag(int i, int i2, boolean z) {
        checkFlag(i2);
        return z ? i | (1 << (15 - i2)) : i & (~(1 << (15 - i2)));
    }

    public void setFlag(int i) {
        checkFlag(i);
        this.flags = setFlag(this.flags, i, true);
    }

    public boolean getFlag(int i) {
        checkFlag(i);
        return (this.flags & (1 << (15 - i))) != 0;
    }

    public int getID() {
        int i;
        int i2 = this.id;
        if (i2 >= 0) {
            return i2;
        }
        synchronized (this) {
            if (this.id < 0) {
                this.id = random.nextInt(65535);
            }
            i = this.id;
        }
        return i;
    }

    public void setID(int i) {
        if (i < 0 || i > 65535) {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("DNS message ID ");
            stringBuffer.append(i);
            stringBuffer.append(" is out of range");
            throw new IllegalArgumentException(stringBuffer.toString());
        }
        this.id = i;
    }

    public int getRcode() {
        return this.flags & 15;
    }

    public void setOpcode(int i) {
        if (i < 0 || i > 15) {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("DNS Opcode ");
            stringBuffer.append(i);
            stringBuffer.append("is out of range");
            throw new IllegalArgumentException(stringBuffer.toString());
        }
        this.flags = (i << 11) | (this.flags & 34815);
    }

    public int getOpcode() {
        return (this.flags >> 11) & 15;
    }

    /* access modifiers changed from: package-private */
    public void incCount(int i) {
        int[] iArr = this.counts;
        int i2 = iArr[i];
        if (i2 != 65535) {
            iArr[i] = i2 + 1;
            return;
        }
        throw new IllegalStateException("DNS section count cannot be incremented");
    }

    public int getCount(int i) {
        return this.counts[i];
    }

    public String printFlags() {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < 16; i++) {
            if (validFlag(i) && getFlag(i)) {
                stringBuffer.append(Flags.string(i));
                stringBuffer.append(" ");
            }
        }
        return stringBuffer.toString();
    }

    /* access modifiers changed from: package-private */
    public String toStringWithRcode(int i) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(";; ->>HEADER<<- ");
        StringBuffer stringBuffer2 = new StringBuffer();
        stringBuffer2.append("opcode: ");
        stringBuffer2.append(Opcode.string(getOpcode()));
        stringBuffer.append(stringBuffer2.toString());
        StringBuffer stringBuffer3 = new StringBuffer();
        stringBuffer3.append(", status: ");
        stringBuffer3.append(Rcode.string(i));
        stringBuffer.append(stringBuffer3.toString());
        StringBuffer stringBuffer4 = new StringBuffer();
        stringBuffer4.append(", id: ");
        stringBuffer4.append(getID());
        stringBuffer.append(stringBuffer4.toString());
        stringBuffer.append("\n");
        StringBuffer stringBuffer5 = new StringBuffer();
        stringBuffer5.append(";; flags: ");
        stringBuffer5.append(printFlags());
        stringBuffer.append(stringBuffer5.toString());
        stringBuffer.append("; ");
        for (int i2 = 0; i2 < 4; i2++) {
            StringBuffer stringBuffer6 = new StringBuffer();
            stringBuffer6.append(Section.string(i2));
            stringBuffer6.append(": ");
            stringBuffer6.append(getCount(i2));
            stringBuffer6.append(" ");
            stringBuffer.append(stringBuffer6.toString());
        }
        return stringBuffer.toString();
    }

    public String toString() {
        return toStringWithRcode(getRcode());
    }

    public Object clone() {
        Header header = new Header();
        header.id = this.id;
        header.flags = this.flags;
        int[] iArr = this.counts;
        System.arraycopy(iArr, 0, header.counts, 0, iArr.length);
        return header;
    }
}
