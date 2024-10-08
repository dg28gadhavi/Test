package com.sec.internal.helper.picturetool;

import android.graphics.Bitmap;
import android.util.Log;
import java.io.File;
import java.io.IOException;

public class JpgContext extends SupportedContextAdapter {
    private static final String LOG_TAG = "JpgContext";
    private ExifProcessor mExifProcessor;

    public JpgContext(ExifProcessor exifProcessor) {
        this.mExifProcessor = exifProcessor;
    }

    public Bitmap.CompressFormat getDestinationFormat() {
        return Bitmap.CompressFormat.JPEG;
    }

    public String toString() {
        return JpgContext.class.getSimpleName();
    }

    public void processSpecificData(File file, File file2) throws IOException {
        Log.d(LOG_TAG, "processSpecificData: Enter");
        try {
            this.mExifProcessor.process(file, file2);
        } catch (IOException unused) {
            Log.d(LOG_TAG, "IOException from ExifProcessor but use destinationFile");
        }
        Log.d(LOG_TAG, "processSpecificData: Exit");
    }
}
