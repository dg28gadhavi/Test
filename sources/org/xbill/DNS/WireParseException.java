package org.xbill.DNS;

import java.io.IOException;

public class WireParseException extends IOException {
    public WireParseException() {
    }

    public WireParseException(String str) {
        super(str);
    }

    public WireParseException(String str, Throwable th) {
        super(str);
        initCause(th);
    }
}
