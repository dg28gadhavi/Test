package org.xbill.DNS;

public class DNSKEYRecord extends KEYBase {
    private static final long serialVersionUID = -8679800040426675002L;

    DNSKEYRecord() {
    }

    /* access modifiers changed from: package-private */
    public Record getObject() {
        return new DNSKEYRecord();
    }
}
