package org.xbill.DNS;

public class NSRecord extends SingleCompressedNameBase {
    private static final long serialVersionUID = 487170758138268838L;

    NSRecord() {
    }

    /* access modifiers changed from: package-private */
    public Record getObject() {
        return new NSRecord();
    }
}
