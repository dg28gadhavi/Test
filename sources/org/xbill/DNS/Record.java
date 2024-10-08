package org.xbill.DNS;

import com.sec.internal.ims.core.cmc.CmcConstants;
import java.io.IOException;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Arrays;
import org.xbill.DNS.utils.base16;

public abstract class Record implements Cloneable, Comparable, Serializable {
    private static final DecimalFormat byteFormat;
    private static final long serialVersionUID = 2694906050116005466L;
    protected int dclass;
    protected Name name;
    protected long ttl;
    protected int type;

    /* access modifiers changed from: package-private */
    public abstract Record getObject();

    /* access modifiers changed from: package-private */
    public abstract void rrFromWire(DNSInput dNSInput) throws IOException;

    /* access modifiers changed from: package-private */
    public abstract String rrToString();

    /* access modifiers changed from: package-private */
    public abstract void rrToWire(DNSOutput dNSOutput, Compression compression, boolean z);

    static {
        DecimalFormat decimalFormat = new DecimalFormat();
        byteFormat = decimalFormat;
        decimalFormat.setMinimumIntegerDigits(3);
    }

    protected Record() {
    }

    private static final Record getEmptyRecord(Name name2, int i, int i2, long j, boolean z) {
        Record record;
        if (z) {
            Record proto = Type.getProto(i);
            if (proto != null) {
                record = proto.getObject();
            } else {
                record = new UNKRecord();
            }
        } else {
            record = new EmptyRecord();
        }
        record.name = name2;
        record.type = i;
        record.dclass = i2;
        record.ttl = j;
        return record;
    }

    private static Record newRecord(Name name2, int i, int i2, long j, int i3, DNSInput dNSInput) throws IOException {
        Record emptyRecord = getEmptyRecord(name2, i, i2, j, dNSInput != null);
        if (dNSInput != null) {
            if (dNSInput.remaining() >= i3) {
                dNSInput.setActive(i3);
                emptyRecord.rrFromWire(dNSInput);
                if (dNSInput.remaining() <= 0) {
                    dNSInput.clearActive();
                } else {
                    throw new WireParseException("invalid record length");
                }
            } else {
                throw new WireParseException("truncated record");
            }
        }
        return emptyRecord;
    }

    public static Record newRecord(Name name2, int i, int i2, long j) {
        if (name2.isAbsolute()) {
            Type.check(i);
            DClass.check(i2);
            TTL.check(j);
            return getEmptyRecord(name2, i, i2, j, false);
        }
        throw new RelativeNameException(name2);
    }

    public static Record newRecord(Name name2, int i, int i2) {
        return newRecord(name2, i, i2, 0);
    }

    static Record fromWire(DNSInput dNSInput, int i, boolean z) throws IOException {
        Name name2 = new Name(dNSInput);
        int readU16 = dNSInput.readU16();
        int readU162 = dNSInput.readU16();
        if (i == 0) {
            return newRecord(name2, readU16, readU162);
        }
        long readU32 = dNSInput.readU32();
        int readU163 = dNSInput.readU16();
        if (readU163 == 0 && z && (i == 1 || i == 2)) {
            return newRecord(name2, readU16, readU162, readU32);
        }
        return newRecord(name2, readU16, readU162, readU32, readU163, dNSInput);
    }

    /* access modifiers changed from: package-private */
    public void toWire(DNSOutput dNSOutput, int i, Compression compression) {
        this.name.toWire(dNSOutput, compression);
        dNSOutput.writeU16(this.type);
        dNSOutput.writeU16(this.dclass);
        if (i != 0) {
            dNSOutput.writeU32(this.ttl);
            int current = dNSOutput.current();
            dNSOutput.writeU16(0);
            rrToWire(dNSOutput, compression, false);
            dNSOutput.writeU16At((dNSOutput.current() - current) - 2, current);
        }
    }

    private void toWireCanonical(DNSOutput dNSOutput, boolean z) {
        this.name.toWireCanonical(dNSOutput);
        dNSOutput.writeU16(this.type);
        dNSOutput.writeU16(this.dclass);
        if (z) {
            dNSOutput.writeU32(0);
        } else {
            dNSOutput.writeU32(this.ttl);
        }
        int current = dNSOutput.current();
        dNSOutput.writeU16(0);
        rrToWire(dNSOutput, (Compression) null, true);
        dNSOutput.writeU16At((dNSOutput.current() - current) - 2, current);
    }

    private byte[] toWireCanonical(boolean z) {
        DNSOutput dNSOutput = new DNSOutput();
        toWireCanonical(dNSOutput, z);
        return dNSOutput.toByteArray();
    }

    public byte[] rdataToWireCanonical() {
        DNSOutput dNSOutput = new DNSOutput();
        rrToWire(dNSOutput, (Compression) null, true);
        return dNSOutput.toByteArray();
    }

    public String rdataToString() {
        return rrToString();
    }

    public String toString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(this.name);
        if (stringBuffer.length() < 8) {
            stringBuffer.append("\t");
        }
        if (stringBuffer.length() < 16) {
            stringBuffer.append("\t");
        }
        stringBuffer.append("\t");
        if (Options.check("BINDTTL")) {
            stringBuffer.append(TTL.format(this.ttl));
        } else {
            stringBuffer.append(this.ttl);
        }
        stringBuffer.append("\t");
        if (this.dclass != 1 || !Options.check("noPrintIN")) {
            stringBuffer.append(DClass.string(this.dclass));
            stringBuffer.append("\t");
        }
        stringBuffer.append(Type.string(this.type));
        String rrToString = rrToString();
        if (!rrToString.equals("")) {
            stringBuffer.append("\t");
            stringBuffer.append(rrToString);
        }
        return stringBuffer.toString();
    }

    protected static String byteArrayToString(byte[] bArr, boolean z) {
        StringBuffer stringBuffer = new StringBuffer();
        if (z) {
            stringBuffer.append('\"');
        }
        for (byte b : bArr) {
            byte b2 = b & 255;
            if (b2 < 32 || b2 >= Byte.MAX_VALUE) {
                stringBuffer.append('\\');
                stringBuffer.append(byteFormat.format((long) b2));
            } else if (b2 == 34 || b2 == 92) {
                stringBuffer.append('\\');
                stringBuffer.append((char) b2);
            } else {
                stringBuffer.append((char) b2);
            }
        }
        if (z) {
            stringBuffer.append('\"');
        }
        return stringBuffer.toString();
    }

    protected static String unknownToString(byte[] bArr) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("\\# ");
        stringBuffer.append(bArr.length);
        stringBuffer.append(" ");
        stringBuffer.append(base16.toString(bArr));
        return stringBuffer.toString();
    }

    public Name getName() {
        return this.name;
    }

    public int getType() {
        return this.type;
    }

    public int getRRsetType() {
        int i = this.type;
        return i == 46 ? ((RRSIGRecord) this).getTypeCovered() : i;
    }

    public int getDClass() {
        return this.dclass;
    }

    public long getTTL() {
        return this.ttl;
    }

    public boolean equals(Object obj) {
        if (obj != null && (obj instanceof Record)) {
            Record record = (Record) obj;
            if (this.type == record.type && this.dclass == record.dclass && this.name.equals(record.name)) {
                return Arrays.equals(rdataToWireCanonical(), record.rdataToWireCanonical());
            }
        }
        return false;
    }

    public int hashCode() {
        int i = 0;
        for (byte b : toWireCanonical(true)) {
            i += (i << 3) + (b & 255);
        }
        return i;
    }

    public int compareTo(Object obj) {
        Record record = (Record) obj;
        int i = 0;
        if (this == record) {
            return 0;
        }
        int compareTo = this.name.compareTo(record.name);
        if (compareTo != 0) {
            return compareTo;
        }
        int i2 = this.dclass - record.dclass;
        if (i2 != 0) {
            return i2;
        }
        int i3 = this.type - record.type;
        if (i3 != 0) {
            return i3;
        }
        byte[] rdataToWireCanonical = rdataToWireCanonical();
        byte[] rdataToWireCanonical2 = record.rdataToWireCanonical();
        while (i < rdataToWireCanonical.length && i < rdataToWireCanonical2.length) {
            int i4 = (rdataToWireCanonical[i] & 255) - (rdataToWireCanonical2[i] & 255);
            if (i4 != 0) {
                return i4;
            }
            i++;
        }
        return rdataToWireCanonical.length - rdataToWireCanonical2.length;
    }

    static int checkU16(String str, int i) {
        if (i >= 0 && i <= 65535) {
            return i;
        }
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(CmcConstants.E_NUM_STR_QUOTE);
        stringBuffer.append(str);
        stringBuffer.append("\" ");
        stringBuffer.append(i);
        stringBuffer.append(" must be an unsigned 16 bit value");
        throw new IllegalArgumentException(stringBuffer.toString());
    }
}
