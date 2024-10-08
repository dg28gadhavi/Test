package com.sec.internal.helper.picturetool;

import java.util.Locale;

public class ComplexImageExtractor {
    private static final String CONTENT_TYPE_GIF = "image/gif";
    private static final String LOG_TAG = "ComplexImageExtractor";
    private static final String TEMP_FILE_PREFIX = "FT_thumb";
    private GifDecoder mDecoder = null;

    private String getFileExtension(String str) {
        int lastIndexOf = str.lastIndexOf(".");
        if (lastIndexOf < 0) {
            return null;
        }
        return str.substring(lastIndexOf + 1).toUpperCase(Locale.ENGLISH);
    }

    /* JADX WARNING: Removed duplicated region for block: B:66:0x00cf A[SYNTHETIC, Splitter:B:66:0x00cf] */
    /* JADX WARNING: Removed duplicated region for block: B:80:0x00e8 A[SYNTHETIC, Splitter:B:80:0x00e8] */
    /* JADX WARNING: Removed duplicated region for block: B:85:0x00f3 A[SYNTHETIC, Splitter:B:85:0x00f3] */
    /* JADX WARNING: Removed duplicated region for block: B:99:0x010c A[SYNTHETIC, Splitter:B:99:0x010c] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public java.io.File extractFrom(java.io.File r5) {
        /*
            r4 = this;
            r0 = 0
            if (r5 != 0) goto L_0x000b
            java.lang.String r4 = LOG_TAG
            java.lang.String r5 = "imageFile == null"
            android.util.Log.e(r4, r5)
            return r0
        L_0x000b:
            java.lang.String r1 = r5.getName()
            java.lang.String r1 = r4.getFileExtension(r1)
            if (r1 != 0) goto L_0x001d
            java.lang.String r4 = LOG_TAG
            java.lang.String r5 = "fileName == null"
            android.util.Log.e(r4, r5)
            return r0
        L_0x001d:
            java.lang.String r1 = com.sec.internal.helper.translate.ContentTypeTranslator.translate(r1)
            java.lang.String r2 = "image/gif"
            boolean r1 = r1.contains(r2)
            if (r1 == 0) goto L_0x0144
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "Gid decoder: extractFrom, file="
            r1.append(r2)
            java.lang.String r2 = r5.getAbsolutePath()
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            java.lang.String r2 = "ComplexImageExtractor"
            android.util.Log.d(r2, r1)
            com.sec.internal.helper.picturetool.GifDecoder r1 = new com.sec.internal.helper.picturetool.GifDecoder
            r1.<init>()
            r4.mDecoder = r1
            java.lang.String r2 = r5.getAbsolutePath()
            int r1 = r1.read(r2)
            if (r1 != 0) goto L_0x012d
            com.sec.internal.helper.picturetool.GifDecoder r4 = r4.mDecoder
            java.util.Vector r4 = r4.getFrames()
            com.sec.internal.helper.Preconditions.checkNotNull(r4)
            int r1 = r4.size()
            if (r1 <= 0) goto L_0x0115
            java.lang.String r1 = "FT_thumb"
            java.lang.String r2 = ".jpg"
            java.io.File r1 = java.io.File.createTempFile(r1, r2)     // Catch:{ IOException -> 0x00c9 }
            java.io.FileOutputStream r2 = new java.io.FileOutputStream     // Catch:{ IOException -> 0x00c9 }
            r2.<init>(r1)     // Catch:{ IOException -> 0x00c9 }
            r0 = 0
            java.lang.Object r4 = r4.get(r0)     // Catch:{ IOException -> 0x00c4, all -> 0x00c1 }
            com.sec.internal.helper.picturetool.GifDecoder$GifFrame r4 = (com.sec.internal.helper.picturetool.GifDecoder.GifFrame) r4     // Catch:{ IOException -> 0x00c4, all -> 0x00c1 }
            android.graphics.Bitmap r4 = r4.image     // Catch:{ IOException -> 0x00c4, all -> 0x00c1 }
            android.graphics.Bitmap$CompressFormat r0 = android.graphics.Bitmap.CompressFormat.JPEG     // Catch:{ IOException -> 0x00c4, all -> 0x00c1 }
            r3 = 100
            boolean r4 = r4.compress(r0, r3, r2)     // Catch:{ IOException -> 0x00c4, all -> 0x00c1 }
            if (r4 != 0) goto L_0x00a2
            r2.flush()     // Catch:{ IOException -> 0x0091 }
            r2.close()     // Catch:{ IOException -> 0x008a }
            goto L_0x0098
        L_0x008a:
            r4 = move-exception
            r4.printStackTrace()
            goto L_0x0098
        L_0x008f:
            r4 = move-exception
            goto L_0x0099
        L_0x0091:
            r4 = move-exception
            r4.printStackTrace()     // Catch:{ all -> 0x008f }
            r2.close()     // Catch:{ IOException -> 0x008a }
        L_0x0098:
            return r5
        L_0x0099:
            r2.close()     // Catch:{ IOException -> 0x009d }
            goto L_0x00a1
        L_0x009d:
            r5 = move-exception
            r5.printStackTrace()
        L_0x00a1:
            throw r4
        L_0x00a2:
            r2.flush()     // Catch:{ IOException -> 0x00b0 }
            r2.close()     // Catch:{ IOException -> 0x00a9 }
            goto L_0x00b7
        L_0x00a9:
            r4 = move-exception
            r4.printStackTrace()
            goto L_0x00b7
        L_0x00ae:
            r4 = move-exception
            goto L_0x00b8
        L_0x00b0:
            r4 = move-exception
            r4.printStackTrace()     // Catch:{ all -> 0x00ae }
            r2.close()     // Catch:{ IOException -> 0x00a9 }
        L_0x00b7:
            return r1
        L_0x00b8:
            r2.close()     // Catch:{ IOException -> 0x00bc }
            goto L_0x00c0
        L_0x00bc:
            r5 = move-exception
            r5.printStackTrace()
        L_0x00c0:
            throw r4
        L_0x00c1:
            r4 = move-exception
            r0 = r2
            goto L_0x00f1
        L_0x00c4:
            r4 = move-exception
            r0 = r2
            goto L_0x00ca
        L_0x00c7:
            r4 = move-exception
            goto L_0x00f1
        L_0x00c9:
            r4 = move-exception
        L_0x00ca:
            r4.printStackTrace()     // Catch:{ all -> 0x00c7 }
            if (r0 == 0) goto L_0x00e6
            r0.flush()     // Catch:{ IOException -> 0x00d5 }
            goto L_0x00e6
        L_0x00d3:
            r4 = move-exception
            goto L_0x00dd
        L_0x00d5:
            r4 = move-exception
            r4.printStackTrace()     // Catch:{ all -> 0x00d3 }
            r0.close()     // Catch:{ IOException -> 0x00ec }
            goto L_0x0144
        L_0x00dd:
            r0.close()     // Catch:{ IOException -> 0x00e1 }
            goto L_0x00e5
        L_0x00e1:
            r5 = move-exception
            r5.printStackTrace()
        L_0x00e5:
            throw r4
        L_0x00e6:
            if (r0 == 0) goto L_0x0144
            r0.close()     // Catch:{ IOException -> 0x00ec }
            goto L_0x0144
        L_0x00ec:
            r4 = move-exception
            r4.printStackTrace()
            goto L_0x0144
        L_0x00f1:
            if (r0 == 0) goto L_0x010a
            r0.flush()     // Catch:{ IOException -> 0x00f9 }
            goto L_0x010a
        L_0x00f7:
            r4 = move-exception
            goto L_0x0101
        L_0x00f9:
            r5 = move-exception
            r5.printStackTrace()     // Catch:{ all -> 0x00f7 }
            r0.close()     // Catch:{ IOException -> 0x0110 }
            goto L_0x0114
        L_0x0101:
            r0.close()     // Catch:{ IOException -> 0x0105 }
            goto L_0x0109
        L_0x0105:
            r5 = move-exception
            r5.printStackTrace()
        L_0x0109:
            throw r4
        L_0x010a:
            if (r0 == 0) goto L_0x0114
            r0.close()     // Catch:{ IOException -> 0x0110 }
            goto L_0x0114
        L_0x0110:
            r5 = move-exception
            r5.printStackTrace()
        L_0x0114:
            throw r4
        L_0x0115:
            java.lang.IllegalArgumentException r5 = new java.lang.IllegalArgumentException
            int r4 = r4.size()
            java.lang.Integer r4 = java.lang.Integer.valueOf(r4)
            java.lang.Object[] r4 = new java.lang.Object[]{r4}
            java.lang.String r0 = "Requested frame was: 0 but %d only available."
            java.lang.String r4 = java.lang.String.format(r0, r4)
            r5.<init>(r4)
            throw r5
        L_0x012d:
            java.lang.IllegalArgumentException r4 = new java.lang.IllegalArgumentException
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r0 = "GifDecoder read routine has ended with an error: "
            r5.append(r0)
            r5.append(r1)
            java.lang.String r5 = r5.toString()
            r4.<init>(r5)
            throw r4
        L_0x0144:
            return r5
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.helper.picturetool.ComplexImageExtractor.extractFrom(java.io.File):java.io.File");
    }

    public void release() {
        GifDecoder gifDecoder = this.mDecoder;
        if (gifDecoder != null) {
            gifDecoder.clean();
        }
    }
}
