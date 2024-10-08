package com.sec.internal.ims.config.exception;

public class NoInitialDataException extends Exception {
    private static final long serialVersionUID = -1037078209338059005L;
    private String message = "";

    public NoInitialDataException(String str) {
        if (str != null) {
            this.message = str;
        }
    }

    public String getMessage() {
        return this.message;
    }
}
