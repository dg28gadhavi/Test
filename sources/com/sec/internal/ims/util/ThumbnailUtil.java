package com.sec.internal.ims.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.CancellationSignal;
import android.util.Log;
import android.util.Size;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ThumbnailUtil {
    protected static final int HIGH_QUALITY = 100;
    private static final String LOG_TAG = "ThumbnailUtil";
    public static final int MAX_BYTE_COUNT = 5120;
    public static final int MAX_BYTE_COUNT_HIGH = 51200;
    private static final int MAX_THUMBNAIL_SIZE = 512;
    protected static final int QUALITY = 95;
    protected static final long VIDEO_FRAME_TIME = 15000000;

    public static byte[] getThumbnailByteArray(Context context, Uri uri) {
        String str = LOG_TAG;
        Log.d(str, "getThumbnailByteArray() contentUri : " + uri);
        Bitmap thumbnailBitmap = getThumbnailBitmap(context, uri);
        if (thumbnailBitmap == null) {
            return null;
        }
        return getCompressedBitmapByteArray(thumbnailBitmap, 95);
    }

    public static byte[] getVideoThumbnailByteArray(Context context, Uri uri, int i) {
        String str = LOG_TAG;
        Log.d(str, "getVideoThumbnailByteArray() contentUri : " + uri);
        Bitmap thumbnailBitmapFromVideo = getThumbnailBitmapFromVideo(context, uri);
        if (thumbnailBitmapFromVideo == null) {
            return null;
        }
        if (thumbnailBitmapFromVideo.getByteCount() > i) {
            thumbnailBitmapFromVideo = getScaledBitmap(thumbnailBitmapFromVideo, i);
        }
        return getCompressedBitmapByteArray(thumbnailBitmapFromVideo, 100);
    }

    private static Bitmap getThumbnailBitmap(Context context, Uri uri) {
        String str = LOG_TAG;
        Log.d(str, "getThumbnailBitmap() contentUri : " + uri);
        Bitmap loadThumbnail = loadThumbnail(context, uri);
        if (loadThumbnail == null) {
            return null;
        }
        if (loadThumbnail.getByteCount() > 5120) {
            loadThumbnail = getScaledBitmap(loadThumbnail, MAX_BYTE_COUNT);
        }
        Bitmap.Config config = loadThumbnail.getConfig();
        Bitmap.Config config2 = Bitmap.Config.RGB_565;
        if (config == config2) {
            return loadThumbnail;
        }
        try {
            return loadThumbnail.copy(config2, false);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return loadThumbnail;
        }
    }

    private static Bitmap loadThumbnail(Context context, Uri uri) {
        try {
            return context.getContentResolver().loadThumbnail(uri, new Size(MAX_THUMBNAIL_SIZE, MAX_THUMBNAIL_SIZE), (CancellationSignal) null);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Bitmap getThumbnailBitmapFromVideo(Context context, Uri uri) {
        MediaMetadataRetriever mediaMetadataRetriever;
        Bitmap bitmap = null;
        try {
            mediaMetadataRetriever = new MediaMetadataRetriever();
            mediaMetadataRetriever.setDataSource(context, uri);
            long parseLong = Long.parseLong(mediaMetadataRetriever.extractMetadata(9)) * 1000;
            long j = VIDEO_FRAME_TIME;
            if (VIDEO_FRAME_TIME > parseLong) {
                j = parseLong / 2;
            }
            bitmap = mediaMetadataRetriever.getFrameAtTime(j);
            mediaMetadataRetriever.close();
        } catch (RuntimeException e) {
            String str = LOG_TAG;
            Log.e(str, "getVideoThumbnailByteArray() failure : " + e);
        } catch (IOException e2) {
            e2.printStackTrace();
        } catch (Throwable th) {
            th.addSuppressed(th);
        }
        return bitmap;
        throw th;
    }

    private static Bitmap getScaledBitmap(Bitmap bitmap, int i) {
        if (i <= 0) {
            i = MAX_BYTE_COUNT;
        }
        int max = Math.max((int) Math.sqrt(((double) bitmap.getByteCount()) / ((double) i)), 1);
        int width = bitmap.getWidth() / max;
        int height = bitmap.getHeight() / max;
        String str = LOG_TAG;
        Log.d(str, "Width: " + width + ", height: " + height);
        return Bitmap.createScaledBitmap(bitmap, width, height, false);
    }

    private static byte[] getCompressedBitmapByteArray(Bitmap bitmap, int i) {
        ByteArrayOutputStream byteArrayOutputStream;
        try {
            byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, i, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            byteArrayOutputStream.close();
            return byteArray;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (Throwable th) {
            th.addSuppressed(th);
        }
        throw th;
    }
}
