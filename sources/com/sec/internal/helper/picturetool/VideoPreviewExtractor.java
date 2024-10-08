package com.sec.internal.helper.picturetool;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.util.Log;
import android.util.Pair;
import com.sec.internal.helper.picturetool.IVideoPreviewExtractor;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class VideoPreviewExtractor implements IVideoPreviewExtractor {
    private static final Bitmap.CompressFormat COMPRESS_FORMAT = Bitmap.CompressFormat.JPEG;
    private static final String EXTENSION = "jpg";
    private static final String LOG_TAG = "VideoPreviewExtractor";
    private static final Pair<Integer, Integer> MINI_DIMENSIONS = Pair.create(512, 384);
    private static final int QUALITY = 75;
    private BitmapExtractor mBitmapExtractor;

    public VideoPreviewExtractor(BitmapExtractor bitmapExtractor) {
        this.mBitmapExtractor = bitmapExtractor;
    }

    public IVideoPreviewExtractor.IVideoPreview extract(File file, File file2) throws IOException {
        String absolutePath = file.getAbsolutePath();
        String str = extractFileNameCore(file.getName()) + "." + EXTENSION;
        Log.d(LOG_TAG, "extract: destPreviewName=" + str);
        Bitmap extractDefaultBitmap = extractDefaultBitmap(file);
        final Pair<Integer, Integer> calculatePreviewDimensions = calculatePreviewDimensions(extractVideoDimensions(absolutePath));
        final File uniqueFile = UniqueFilePathResolver.getUniqueFile(str, file2);
        if (MINI_DIMENSIONS.equals(calculatePreviewDimensions)) {
            saveBitmapToFile(extractDefaultBitmap, uniqueFile);
        } else {
            saveBitmapToFile(extractDefaultBitmap, uniqueFile, calculatePreviewDimensions);
        }
        return new IVideoPreviewExtractor.IVideoPreview() {
            public File getFile() {
                return uniqueFile;
            }

            public long getSize() {
                return uniqueFile.length();
            }

            public Pair<Integer, Integer> getDimensions() {
                return calculatePreviewDimensions;
            }
        };
    }

    private String extractFileNameCore(String str) throws IOException {
        int lastIndexOf = str.lastIndexOf(".");
        if (lastIndexOf < 0) {
            throwIOE("lack of extension:%s", str);
        }
        return str.substring(0, lastIndexOf);
    }

    private Pair<Integer, Integer> extractVideoDimensions(String str) {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(str);
        return Pair.create(Integer.valueOf(Integer.parseInt(mediaMetadataRetriever.extractMetadata(18))), Integer.valueOf(Integer.parseInt(mediaMetadataRetriever.extractMetadata(19))));
    }

    private Pair<Integer, Integer> calculatePreviewDimensions(Pair<Integer, Integer> pair) {
        int intValue = ((Integer) pair.first).intValue();
        int intValue2 = ((Integer) pair.second).intValue();
        Pair<Integer, Integer> pair2 = MINI_DIMENSIONS;
        return VideoScaleCalculator.calculate(intValue, intValue2, ((Integer) pair2.first).intValue(), ((Integer) pair2.second).intValue());
    }

    private Bitmap extractDefaultBitmap(File file) throws IOException {
        return this.mBitmapExtractor.extractFromVideo(file);
    }

    private static void throwIOE(String str, Object... objArr) throws IOException {
        throw new IOException(String.format(str, objArr));
    }

    private static void closeStream(OutputStream outputStream) {
        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (IOException e) {
                Log.w(LOG_TAG, "error closing stream", e);
            }
        }
    }

    private static void saveBitmapToFile(Bitmap bitmap, File file, Pair<Integer, Integer> pair) throws IOException {
        int i;
        int i2;
        FileOutputStream fileOutputStream = null;
        try {
            if (bitmap.getWidth() > bitmap.getHeight()) {
                i2 = Math.max(((Integer) pair.first).intValue(), ((Integer) pair.second).intValue());
                i = Math.min(((Integer) pair.first).intValue(), ((Integer) pair.second).intValue());
            } else {
                i2 = Math.min(((Integer) pair.first).intValue(), ((Integer) pair.second).intValue());
                i = Math.max(((Integer) pair.first).intValue(), ((Integer) pair.second).intValue());
            }
            FileOutputStream fileOutputStream2 = new FileOutputStream(file);
            try {
                Bitmap createScaledBitmap = Bitmap.createScaledBitmap(bitmap, i2, i, false);
                boolean compress = createScaledBitmap.compress(COMPRESS_FORMAT, 75, fileOutputStream2);
                if (!createScaledBitmap.sameAs(bitmap)) {
                    createScaledBitmap.recycle();
                }
                fileOutputStream2.flush();
                closeStream(fileOutputStream2);
                if (!compress) {
                    throwIOE("failure while compressing:%s,%d", file, 75);
                }
            } catch (Throwable th) {
                th = th;
                fileOutputStream = fileOutputStream2;
                closeStream(fileOutputStream);
                throw th;
            }
        } catch (Throwable th2) {
            th = th2;
            closeStream(fileOutputStream);
            throw th;
        }
    }

    private void saveBitmapToFile(Bitmap bitmap, File file) throws IOException {
        Throwable th;
        FileOutputStream fileOutputStream;
        try {
            fileOutputStream = new FileOutputStream(file);
            try {
                boolean compress = bitmap.compress(COMPRESS_FORMAT, 75, fileOutputStream);
                fileOutputStream.flush();
                closeStream(fileOutputStream);
                if (!compress) {
                    throwIOE("failure while compressing:%s,%d", file, 75);
                }
            } catch (Throwable th2) {
                th = th2;
                closeStream(fileOutputStream);
                throw th;
            }
        } catch (Throwable th3) {
            fileOutputStream = null;
            th = th3;
            closeStream(fileOutputStream);
            throw th;
        }
    }
}
