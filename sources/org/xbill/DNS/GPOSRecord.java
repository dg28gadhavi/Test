package org.xbill.DNS;

import java.io.IOException;

public class GPOSRecord extends Record {
    private static final long serialVersionUID = -6349714958085750705L;
    private byte[] altitude;
    private byte[] latitude;
    private byte[] longitude;

    GPOSRecord() {
    }

    /* access modifiers changed from: package-private */
    public Record getObject() {
        return new GPOSRecord();
    }

    private void validate(double d, double d2) throws IllegalArgumentException {
        if (d < -90.0d || d > 90.0d) {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("illegal longitude ");
            stringBuffer.append(d);
            throw new IllegalArgumentException(stringBuffer.toString());
        } else if (d2 < -180.0d || d2 > 180.0d) {
            StringBuffer stringBuffer2 = new StringBuffer();
            stringBuffer2.append("illegal latitude ");
            stringBuffer2.append(d2);
            throw new IllegalArgumentException(stringBuffer2.toString());
        }
    }

    /* access modifiers changed from: package-private */
    public void rrFromWire(DNSInput dNSInput) throws IOException {
        this.longitude = dNSInput.readCountedString();
        this.latitude = dNSInput.readCountedString();
        this.altitude = dNSInput.readCountedString();
        try {
            validate(getLongitude(), getLatitude());
        } catch (IllegalArgumentException e) {
            throw new WireParseException(e.getMessage());
        }
    }

    /* access modifiers changed from: package-private */
    public String rrToString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(Record.byteArrayToString(this.longitude, true));
        stringBuffer.append(" ");
        stringBuffer.append(Record.byteArrayToString(this.latitude, true));
        stringBuffer.append(" ");
        stringBuffer.append(Record.byteArrayToString(this.altitude, true));
        return stringBuffer.toString();
    }

    public String getLongitudeString() {
        return Record.byteArrayToString(this.longitude, false);
    }

    public double getLongitude() {
        return Double.parseDouble(getLongitudeString());
    }

    public String getLatitudeString() {
        return Record.byteArrayToString(this.latitude, false);
    }

    public double getLatitude() {
        return Double.parseDouble(getLatitudeString());
    }

    /* access modifiers changed from: package-private */
    public void rrToWire(DNSOutput dNSOutput, Compression compression, boolean z) {
        dNSOutput.writeCountedString(this.longitude);
        dNSOutput.writeCountedString(this.latitude);
        dNSOutput.writeCountedString(this.altitude);
    }
}
