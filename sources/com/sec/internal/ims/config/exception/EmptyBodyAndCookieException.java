package com.sec.internal.ims.config.exception;

public class EmptyBodyAndCookieException extends UnknownStatusException {
    private static final long serialVersionUID = 8141010442931458349L;

    public EmptyBodyAndCookieException(String str) {
        super(str);
    }

    public String getMessage() {
        return this.message;
    }
}
