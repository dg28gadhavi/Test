package com.sec.internal.helper.picturetool;

import android.util.Log;
import java.io.File;
import java.io.IOException;

public class DefaultContext extends JpgContext {
    private static final String LOG_TAG = "DefaultContext";

    public DefaultContext(ExifProcessor exifProcessor) {
        super(exifProcessor);
    }

    public File getFinalFilePath(File file, String str) throws IOException {
        return super.getFinalFilePath(file, changeExtToJpg(str));
    }

    public String toString() {
        return DefaultContext.class.getSimpleName();
    }

    private String changeExtToJpg(String str) {
        return str.substring(0, str.lastIndexOf(".")) + ".jpg";
    }

    public void processSpecificData(File file, File file2) throws IOException {
        Log.d(LOG_TAG, "processSpecificData: Exit");
    }
}
