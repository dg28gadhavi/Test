package com.sec.internal.ims.servicemodules.im.util;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import com.sec.internal.log.IMSLog;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class FileDurationUtil {
    private static final String LOG_TAG = "FileDurationUtil";

    public static int getFileDurationTime(String str) {
        String str2 = LOG_TAG;
        IMSLog.i(str2, "getFileDurationTime for filePath=" + IMSLog.checker(str));
        if (str == null) {
            return -1;
        }
        File file = new File(str);
        if (!file.exists()) {
            return -1;
        }
        return (int) getDuration(getMediaMetadataRetriever(file));
    }

    public static int getFileDurationTime(Context context, Uri uri) {
        String str = LOG_TAG;
        IMSLog.i(str, "getFileDurationTime for contentUri=" + IMSLog.checker(uri));
        if (context == null || uri == null) {
            return -1;
        }
        return (int) getDuration(getMediaMetadataRetriever(context, uri));
    }

    private static MediaMetadataRetriever getMediaMetadataRetriever(File file) {
        FileInputStream fileInputStream;
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        try {
            fileInputStream = new FileInputStream(file);
            mediaMetadataRetriever.setDataSource(fileInputStream.getFD());
            fileInputStream.close();
        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
        } catch (Throwable th) {
            th.addSuppressed(th);
        }
        return mediaMetadataRetriever;
        throw th;
    }

    private static MediaMetadataRetriever getMediaMetadataRetriever(Context context, Uri uri) {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        try {
            mediaMetadataRetriever.setDataSource(context, uri);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        return mediaMetadataRetriever;
    }

    private static long getDuration(MediaMetadataRetriever mediaMetadataRetriever) {
        long j = 0;
        try {
            j = Long.parseLong(mediaMetadataRetriever.extractMetadata(9));
            String str = LOG_TAG;
            IMSLog.i(str, "getFileDurationTime, time = " + j);
            return j;
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return j;
        }
    }
}
