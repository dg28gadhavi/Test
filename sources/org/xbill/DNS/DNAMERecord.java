package org.xbill.DNS;

public class DNAMERecord extends SingleNameBase {
    private static final long serialVersionUID = 2670767677200844154L;

    DNAMERecord() {
    }

    /* access modifiers changed from: package-private */
    public Record getObject() {
        return new DNAMERecord();
    }
}
