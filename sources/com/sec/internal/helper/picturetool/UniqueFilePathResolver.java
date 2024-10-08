package com.sec.internal.helper.picturetool;

import android.util.Log;
import java.io.File;
import java.io.IOException;

public class UniqueFilePathResolver {
    public static File getUniqueFile(String str, File file) {
        String name = new File(file.getAbsoluteFile(), str).getName();
        int lastIndexOf = name.lastIndexOf(".");
        if (lastIndexOf == -1) {
            lastIndexOf = name.length();
        }
        String substring = name.substring(0, lastIndexOf);
        String substring2 = name.substring(lastIndexOf);
        int i = 1;
        while (new File(file.getAbsoluteFile(), str).exists()) {
            str = substring + "(" + i + ")" + substring2;
            i++;
        }
        File file2 = new File(file.getAbsoluteFile(), str);
        Log.d("UniqueFilePathResolver", "file path=" + file2.getAbsolutePath());
        try {
            file2.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file2;
    }
}
