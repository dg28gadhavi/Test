package org.xbill.DNS;

public class KEYRecord extends KEYBase {
    private static final long serialVersionUID = 6385613447571488906L;

    KEYRecord() {
    }

    /* access modifiers changed from: package-private */
    public Record getObject() {
        return new KEYRecord();
    }
}
