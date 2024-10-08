package org.xbill.DNS;

public class KXRecord extends U16NameBase {
    private static final long serialVersionUID = 7448568832769757809L;

    KXRecord() {
    }

    /* access modifiers changed from: package-private */
    public Record getObject() {
        return new KXRecord();
    }
}
