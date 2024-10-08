package com.sec.internal.helper.picturetool;

import android.util.Log;
import java.io.File;
import java.io.IOException;

public abstract class SupportedContextAdapter implements IContentTypeContext {
    private static final String LOG_TAG = "SupportedContextAdapter";

    public File getFinalFilePath(File file, String str) throws NullPointerException, IOException {
        return UniqueFilePathResolver.getUniqueFile(str, file);
    }

    public void validateExtension() throws IOException {
        Log.v(LOG_TAG, "validateExtension:: Exit");
    }
}
