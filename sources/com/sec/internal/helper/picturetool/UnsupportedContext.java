package com.sec.internal.helper.picturetool;

import android.graphics.Bitmap;
import android.util.Log;
import java.io.File;
import java.io.IOException;

public class UnsupportedContext implements IContentTypeContext {
    private static final String LOG_TAG = "UnsupportedContext";

    public Bitmap.CompressFormat getDestinationFormat() {
        throw new RuntimeException("BAD ACCESS");
    }

    public void validateExtension() throws IOException {
        Log.d(LOG_TAG, "unsupported image format");
        throw new IOException();
    }

    public File getFinalFilePath(File file, String str) throws IOException {
        throw new RuntimeException("BAD ACCESS");
    }

    public String toString() {
        return UnsupportedContext.class.getSimpleName();
    }

    public void processSpecificData(File file, File file2) throws IOException {
        throw new RuntimeException("BAD ACCESS");
    }
}
