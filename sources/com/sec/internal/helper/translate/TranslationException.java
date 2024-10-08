package com.sec.internal.helper.translate;

public class TranslationException extends RuntimeException {
    private static final long serialVersionUID = 1;

    public TranslationException(Object obj) {
        super("Could not find translation for: " + obj);
    }
}
