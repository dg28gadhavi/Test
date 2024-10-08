package org.xbill.DNS;

public class PTRRecord extends SingleCompressedNameBase {
    private static final long serialVersionUID = -8321636610425434192L;

    PTRRecord() {
    }

    /* access modifiers changed from: package-private */
    public Record getObject() {
        return new PTRRecord();
    }
}
