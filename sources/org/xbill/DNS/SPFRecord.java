package org.xbill.DNS;

public class SPFRecord extends TXTBase {
    private static final long serialVersionUID = -2100754352801658722L;

    SPFRecord() {
    }

    /* access modifiers changed from: package-private */
    public Record getObject() {
        return new SPFRecord();
    }
}
