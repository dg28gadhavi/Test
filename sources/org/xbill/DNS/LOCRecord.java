package org.xbill.DNS;

import com.sec.internal.constants.ims.entitilement.SoftphoneNamespaces;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class LOCRecord extends Record {
    private static final long serialVersionUID = 9058224788126750409L;
    private static NumberFormat w2;
    private static NumberFormat w3;
    private long altitude;
    private long hPrecision;
    private long latitude;
    private long longitude;
    private long size;
    private long vPrecision;

    static {
        DecimalFormat decimalFormat = new DecimalFormat();
        w2 = decimalFormat;
        decimalFormat.setMinimumIntegerDigits(2);
        DecimalFormat decimalFormat2 = new DecimalFormat();
        w3 = decimalFormat2;
        decimalFormat2.setMinimumIntegerDigits(3);
    }

    LOCRecord() {
    }

    /* access modifiers changed from: package-private */
    public Record getObject() {
        return new LOCRecord();
    }

    /* access modifiers changed from: package-private */
    public void rrFromWire(DNSInput dNSInput) throws IOException {
        if (dNSInput.readU8() == 0) {
            this.size = parseLOCformat(dNSInput.readU8());
            this.hPrecision = parseLOCformat(dNSInput.readU8());
            this.vPrecision = parseLOCformat(dNSInput.readU8());
            this.latitude = dNSInput.readU32();
            this.longitude = dNSInput.readU32();
            this.altitude = dNSInput.readU32();
            return;
        }
        throw new WireParseException("Invalid LOC version");
    }

    private void renderFixedPoint(StringBuffer stringBuffer, NumberFormat numberFormat, long j, long j2) {
        stringBuffer.append(j / j2);
        long j3 = j % j2;
        if (j3 != 0) {
            stringBuffer.append(".");
            stringBuffer.append(numberFormat.format(j3));
        }
    }

    private String positionToString(long j, char c, char c2) {
        StringBuffer stringBuffer = new StringBuffer();
        long j2 = j - 2147483648L;
        if (j2 < 0) {
            j2 = -j2;
            c = c2;
        }
        stringBuffer.append(j2 / 3600000);
        long j3 = j2 % 3600000;
        stringBuffer.append(" ");
        stringBuffer.append(j3 / SoftphoneNamespaces.SoftphoneSettings.LONG_BACKOFF);
        long j4 = j3 % SoftphoneNamespaces.SoftphoneSettings.LONG_BACKOFF;
        stringBuffer.append(" ");
        renderFixedPoint(stringBuffer, w3, j4, 1000);
        stringBuffer.append(" ");
        stringBuffer.append(c);
        return stringBuffer.toString();
    }

    /* access modifiers changed from: package-private */
    public String rrToString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(positionToString(this.latitude, 'N', 'S'));
        stringBuffer.append(" ");
        stringBuffer.append(positionToString(this.longitude, 'E', 'W'));
        stringBuffer.append(" ");
        StringBuffer stringBuffer2 = stringBuffer;
        renderFixedPoint(stringBuffer2, w2, this.altitude - 10000000, 100);
        stringBuffer.append("m ");
        renderFixedPoint(stringBuffer2, w2, this.size, 100);
        stringBuffer.append("m ");
        renderFixedPoint(stringBuffer2, w2, this.hPrecision, 100);
        stringBuffer.append("m ");
        renderFixedPoint(stringBuffer2, w2, this.vPrecision, 100);
        stringBuffer.append("m");
        return stringBuffer.toString();
    }

    /* access modifiers changed from: package-private */
    public void rrToWire(DNSOutput dNSOutput, Compression compression, boolean z) {
        dNSOutput.writeU8(0);
        dNSOutput.writeU8(toLOCformat(this.size));
        dNSOutput.writeU8(toLOCformat(this.hPrecision));
        dNSOutput.writeU8(toLOCformat(this.vPrecision));
        dNSOutput.writeU32(this.latitude);
        dNSOutput.writeU32(this.longitude);
        dNSOutput.writeU32(this.altitude);
    }

    private static long parseLOCformat(int i) throws WireParseException {
        long j = (long) (i >> 4);
        int i2 = i & 15;
        if (j > 9 || i2 > 9) {
            throw new WireParseException("Invalid LOC Encoding");
        }
        while (true) {
            int i3 = i2 - 1;
            if (i2 <= 0) {
                return j;
            }
            j *= 10;
            i2 = i3;
        }
    }

    private int toLOCformat(long j) {
        byte b = 0;
        while (j > 9) {
            b = (byte) (b + 1);
            j /= 10;
        }
        return (int) ((j << 4) + ((long) b));
    }
}
