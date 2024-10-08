package org.xbill.DNS;

public class RRSIGRecord extends SIGBase {
    private static final long serialVersionUID = -2609150673537226317L;

    RRSIGRecord() {
    }

    /* access modifiers changed from: package-private */
    public Record getObject() {
        return new RRSIGRecord();
    }
}
