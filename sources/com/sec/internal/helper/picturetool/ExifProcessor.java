package com.sec.internal.helper.picturetool;

import android.media.ExifInterface;
import android.util.Log;
import java.io.File;
import java.io.IOException;

public class ExifProcessor {
    private static final String LOG_TAG = "ExifProcessor";

    public void process(File file, File file2) throws IOException {
        String str = LOG_TAG;
        Log.d(str, "process: Enter");
        int attributeInt = new ExifInterface(file.getAbsolutePath()).getAttributeInt("Orientation", 0);
        ExifInterface exifInterface = new ExifInterface(file2.getAbsolutePath());
        exifInterface.setAttribute("Orientation", Integer.toString(attributeInt));
        exifInterface.saveAttributes();
        Log.d(str, "process: Exit");
    }
}
