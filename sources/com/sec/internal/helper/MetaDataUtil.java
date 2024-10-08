package com.sec.internal.helper;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import com.sec.internal.constants.ims.servicemodules.sms.SmsMessage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class MetaDataUtil {
    private static String checkMetaInfo(String str, String str2) {
        String substring = str.substring(str.lastIndexOf(".") + 1);
        if ("video/mp4".equalsIgnoreCase(str2)) {
            return ("3gp".equalsIgnoreCase(substring) || SmsMessage.FORMAT_3GPP.equalsIgnoreCase(substring) || "3g2".equalsIgnoreCase(substring)) ? "video/3gpp" : str2;
        }
        return str2;
    }

    public static String getContentType(File file) {
        FileInputStream fileInputStream;
        try {
            fileInputStream = new FileInputStream(file);
            MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
            mediaMetadataRetriever.setDataSource(fileInputStream.getFD());
            String checkMetaInfo = checkMetaInfo(file.getName(), mediaMetadataRetriever.extractMetadata(12));
            fileInputStream.close();
            return checkMetaInfo;
        } catch (IOException | RuntimeException unused) {
            return null;
        } catch (Throwable th) {
            th.addSuppressed(th);
        }
        throw th;
    }

    public static String getContentType(Context context, String str, Uri uri) {
        try {
            MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
            mediaMetadataRetriever.setDataSource(context, uri);
            return checkMetaInfo(str, mediaMetadataRetriever.extractMetadata(12));
        } catch (Exception unused) {
            return null;
        }
    }
}
