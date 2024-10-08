package com.sec.internal.ims.config.exception;

public class InvalidXmlException extends Exception {
    private static final long serialVersionUID = -1084933356219231606L;
    private String message = "";

    public InvalidXmlException(String str) {
        if (str != null) {
            this.message = str;
        }
    }

    public String getMessage() {
        return this.message;
    }
}
