package com.sec.internal.helper.picturetool;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.media.ThumbnailUtils;
import android.os.CancellationSignal;
import android.util.Log;
import android.util.Size;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class BitmapExtractor {
    private static final String LOG_TAG = "BitmapExtractor";
    private static final Size MINI_SIZE = new Size(512, 384);
    private static final Bitmap.Config PREFERRED_IMAGE_CONFIG = Bitmap.Config.RGB_565;

    public Bitmap extractFromImage(File file, int i) throws NullPointerException, IllegalArgumentException, IOException {
        Throwable th;
        FileInputStream fileInputStream;
        Log.d(LOG_TAG, "extractBitmapFromImage(image=" + file + ", scale=" + i + ")");
        try {
            BitmapFactory.Options createData = BitmapOptions.createData(i, PREFERRED_IMAGE_CONFIG);
            fileInputStream = new FileInputStream(file);
            try {
                Bitmap decodeStream = BitmapFactory.decodeStream(fileInputStream, (Rect) null, createData);
                closeStream(fileInputStream);
                return decodeStream;
            } catch (Throwable th2) {
                th = th2;
                closeStream(fileInputStream);
                throw th;
            }
        } catch (Throwable th3) {
            fileInputStream = null;
            th = th3;
            closeStream(fileInputStream);
            throw th;
        }
    }

    public Bitmap extractFromVideo(File file) throws NullPointerException, IOException {
        Bitmap createVideoThumbnail = ThumbnailUtils.createVideoThumbnail(file, MINI_SIZE, (CancellationSignal) null);
        if (createVideoThumbnail == null) {
            throwIOE("invalid input:%s", file.getAbsolutePath());
        }
        Log.d(LOG_TAG, "extractFromVideo:: Exit");
        return createVideoThumbnail;
    }

    private static void closeStream(Closeable closeable) throws IOException {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                throw new IOException("Can't close stream: e=" + e);
            }
        }
    }

    private static void throwIOE(String str, Object... objArr) throws IOException {
        throw new IOException(String.format(str, objArr));
    }
}
