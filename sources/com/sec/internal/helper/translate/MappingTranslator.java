package com.sec.internal.helper.translate;

import com.sec.internal.helper.Preconditions;
import java.util.HashMap;
import java.util.Map;

public class MappingTranslator<TranslatedType, ResultType> {
    private final Map<TranslatedType, ResultType> mMapping;

    private MappingTranslator(Map<TranslatedType, ResultType> map) {
        this.mMapping = map;
    }

    public ResultType translate(TranslatedType translatedtype) throws TranslationException {
        if (translatedtype == null) {
            return null;
        }
        if (this.mMapping.containsKey(translatedtype)) {
            return this.mMapping.get(translatedtype);
        }
        throw new TranslationException(translatedtype.toString());
    }

    public boolean isTranslationDefined(TranslatedType translatedtype) {
        return this.mMapping.containsKey(translatedtype);
    }

    public static class Builder<TranslatedType, ResultType> {
        private final Map<TranslatedType, ResultType> mMapping = new HashMap();

        public Builder<TranslatedType, ResultType> map(TranslatedType translatedtype, ResultType resulttype) throws IllegalArgumentException, IllegalStateException {
            Preconditions.checkNotNull(translatedtype, "translatedValue can't be NULL");
            Preconditions.checkState(!this.mMapping.containsKey(translatedtype));
            this.mMapping.put(translatedtype, resulttype);
            return this;
        }

        public MappingTranslator<TranslatedType, ResultType> buildTranslator() {
            Preconditions.checkState(!this.mMapping.isEmpty());
            return new MappingTranslator<>(this.mMapping);
        }
    }

    public static class TranslationException extends RuntimeException {
        private static final long serialVersionUID = 1;

        public TranslationException(Object obj) {
            super("Could not find translation for: " + obj);
        }

        public TranslationException(Object obj, Throwable th) {
            super("Could not find translation for: " + obj, th);
        }
    }
}
