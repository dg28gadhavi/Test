package com.sec.internal.helper.picturetool;

import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.util.Log;
import android.util.Pair;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ImageDimensionsExtractor {
    private static final String LOG_TAG = "ImageDimensionsExtractor";

    public Pair<Integer, Integer> extract(File file) {
        FileInputStream fileInputStream;
        Log.d(LOG_TAG, "getImageDimensions:" + file.getAbsolutePath());
        Pair<Integer, Integer> pair = null;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            fileInputStream = new FileInputStream(file);
            try {
                BitmapFactory.decodeStream(fileInputStream, (Rect) null, options);
                pair = Pair.create(Integer.valueOf(options.outWidth), Integer.valueOf(options.outHeight));
            } catch (FileNotFoundException e) {
                e = e;
                try {
                    e.printStackTrace();
                    closeStream(fileInputStream);
                    return pair;
                } catch (Throwable th) {
                    th = th;
                    closeStream(fileInputStream);
                    throw th;
                }
            }
        } catch (FileNotFoundException e2) {
            e = e2;
            fileInputStream = null;
            e.printStackTrace();
            closeStream(fileInputStream);
            return pair;
        } catch (Throwable th2) {
            fileInputStream = null;
            th = th2;
            closeStream(fileInputStream);
            throw th;
        }
        closeStream(fileInputStream);
        return pair;
    }

    private static void closeStream(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                String str = LOG_TAG;
                Log.d(str, "closeStream: e=" + e);
            }
        }
    }
}
