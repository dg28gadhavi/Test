package com.sec.internal.ims.config.exception;

public class InvalidHeaderException extends Exception {
    private static final long serialVersionUID = 8374723406515232560L;
    private String message = "";

    public InvalidHeaderException(String str) {
        if (str != null) {
            this.message = str;
        }
    }

    public String getMessage() {
        return this.message;
    }
}
