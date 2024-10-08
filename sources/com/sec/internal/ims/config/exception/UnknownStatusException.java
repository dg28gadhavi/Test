package com.sec.internal.ims.config.exception;

public class UnknownStatusException extends Exception {
    private static final long serialVersionUID = -8533200068421479731L;
    protected String message = "";

    public UnknownStatusException(String str) {
        if (str != null) {
            this.message = str;
        }
    }

    public String getMessage() {
        return this.message;
    }
}
