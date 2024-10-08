package org.xbill.DNS;

public class CNAMERecord extends SingleCompressedNameBase {
    private static final long serialVersionUID = -4020373886892538580L;

    CNAMERecord() {
    }

    /* access modifiers changed from: package-private */
    public Record getObject() {
        return new CNAMERecord();
    }

    public Name getTarget() {
        return getSingleName();
    }
}
