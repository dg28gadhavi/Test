package com.sec.internal.helper.picturetool;

import android.graphics.Bitmap;
import android.util.Log;
import java.io.InputStream;
import java.util.Vector;

public class GifDecoder {
    public static final String LOG_TAG = "GifDecoder";
    private static final int MAX_FRAMES = 50;
    protected static final int MAX_STACK_SIZE = 4096;
    public static final int STATUS_FORMAT_ERROR = 1;
    public static final int STATUS_OK = 0;
    public static final int STATUS_OPEN_ERROR = 2;
    protected int[] act;
    protected int bgColor;
    protected int bgIndex;
    protected byte[] block = new byte[256];
    protected int blockSize = 0;
    protected int delay = 0;
    protected int dispose = 0;
    protected int frameCount;
    protected Vector<GifFrame> frames;
    protected int[] gct;
    protected boolean gctFlag;
    protected int gctSize;
    protected int height;
    protected int ih;
    protected Bitmap image;
    protected InputStream in;
    protected boolean interlace;
    protected int iw;
    protected int ix;
    protected int iy;
    protected int lastBgColor;
    protected Bitmap lastBitmap;
    protected int lastDispose = 0;
    protected int[] lct;
    protected boolean lctFlag;
    protected int lctSize;
    protected int loopCount = 1;
    protected int lrh;
    protected int lrw;
    protected int lrx;
    protected int lry;
    protected int pixelAspect;
    protected byte[] pixelStack;
    protected byte[] pixels;
    protected short[] prefix;
    protected int status;
    protected byte[] suffix;
    protected int transIndex;
    protected boolean transparency = false;
    protected int width;

    public static class GifFrame {
        public int delay;
        public Bitmap image;

        public GifFrame(Bitmap bitmap, int i) {
            this.image = bitmap;
            this.delay = i;
        }
    }

    /* access modifiers changed from: protected */
    public void setPixels() {
        int i;
        int[] iArr = new int[(this.image.getWidth() * this.image.getHeight())];
        int i2 = this.lastDispose;
        int i3 = 0;
        if (i2 > 0) {
            if (i2 == 3) {
                int i4 = this.frameCount - 2;
                if (i4 > 0) {
                    this.lastBitmap = getFrame(i4 - 1);
                } else {
                    this.lastBitmap = null;
                }
            }
            Bitmap bitmap = this.lastBitmap;
            if (bitmap != null) {
                int i5 = this.width;
                bitmap.getPixels(iArr, 0, i5, 0, 0, i5, this.height);
                if (this.lastDispose == 2) {
                    int i6 = !this.transparency ? this.lastBgColor : 0;
                    for (int i7 = 0; i7 < this.lrh; i7++) {
                        int i8 = ((this.lry + i7) * this.width) + this.lrx;
                        int i9 = this.lrw + i8;
                        while (i8 < i9) {
                            iArr[i8] = i6;
                            i8++;
                        }
                    }
                }
            }
        }
        int i10 = 8;
        int i11 = 0;
        int i12 = 1;
        while (true) {
            int i13 = this.ih;
            if (i3 < i13) {
                if (this.interlace) {
                    if (i11 >= i13) {
                        i12++;
                        if (i12 == 2) {
                            i11 = 4;
                        } else if (i12 == 3) {
                            i10 = 4;
                            i11 = 2;
                        } else if (i12 == 4) {
                            i10 = 2;
                            i11 = 1;
                        }
                    }
                    i = i11 + i10;
                } else {
                    i = i11;
                    i11 = i3;
                }
                int i14 = i11 + this.iy;
                if (i14 < this.height) {
                    int i15 = this.width;
                    int i16 = i14 * i15;
                    int i17 = this.ix + i16;
                    int i18 = this.iw;
                    int i19 = i17 + i18;
                    if (i16 + i15 < i19) {
                        i19 = i16 + i15;
                    }
                    int i20 = i18 * i3;
                    while (i17 < i19) {
                        int i21 = i20 + 1;
                        int i22 = this.act[this.pixels[i20] & 255];
                        if (i22 != 0) {
                            iArr[i17] = i22;
                        }
                        i17++;
                        i20 = i21;
                    }
                }
                i3++;
                i11 = i;
            } else {
                Bitmap bitmap2 = this.image;
                bitmap2.setPixels(iArr, 0, bitmap2.getWidth(), 0, 0, this.image.getWidth(), this.image.getHeight());
                return;
            }
        }
    }

    public Bitmap getFrame(int i) {
        int i2;
        int i3 = this.frameCount;
        if (i3 > 0 && (i2 = i % i3) >= 0 && i2 < this.frames.size()) {
            return this.frames.elementAt(i2).image;
        }
        return null;
    }

    /* JADX WARNING: Removed duplicated region for block: B:21:0x0030 A[SYNTHETIC, Splitter:B:21:0x0030] */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x003f A[SYNTHETIC, Splitter:B:28:0x003f] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int read(java.lang.String r4) {
        /*
            r3 = this;
            java.lang.String r0 = "Could not close stream"
            r3.init()
            r1 = 0
            java.io.FileInputStream r2 = new java.io.FileInputStream     // Catch:{ FileNotFoundException -> 0x002b }
            r2.<init>(r4)     // Catch:{ FileNotFoundException -> 0x002b }
            r3.in = r2     // Catch:{ FileNotFoundException -> 0x0027, all -> 0x0024 }
            r3.readHeader()     // Catch:{ FileNotFoundException -> 0x0027, all -> 0x0024 }
            boolean r4 = r3.err()     // Catch:{ FileNotFoundException -> 0x0027, all -> 0x0024 }
            if (r4 != 0) goto L_0x0020
            r3.readContents()     // Catch:{ FileNotFoundException -> 0x0027, all -> 0x0024 }
            int r4 = r3.frameCount     // Catch:{ FileNotFoundException -> 0x0027, all -> 0x0024 }
            if (r4 >= 0) goto L_0x0020
            r4 = 1
            r3.status = r4     // Catch:{ FileNotFoundException -> 0x0027, all -> 0x0024 }
        L_0x0020:
            r2.close()     // Catch:{ Exception -> 0x0034 }
            goto L_0x003a
        L_0x0024:
            r3 = move-exception
            r1 = r2
            goto L_0x003d
        L_0x0027:
            r1 = r2
            goto L_0x002b
        L_0x0029:
            r3 = move-exception
            goto L_0x003d
        L_0x002b:
            r4 = 2
            r3.status = r4     // Catch:{ all -> 0x0029 }
            if (r1 == 0) goto L_0x003a
            r1.close()     // Catch:{ Exception -> 0x0034 }
            goto L_0x003a
        L_0x0034:
            r4 = move-exception
            java.lang.String r1 = LOG_TAG
            android.util.Log.e(r1, r0, r4)
        L_0x003a:
            int r3 = r3.status
            return r3
        L_0x003d:
            if (r1 == 0) goto L_0x0049
            r1.close()     // Catch:{ Exception -> 0x0043 }
            goto L_0x0049
        L_0x0043:
            r4 = move-exception
            java.lang.String r1 = LOG_TAG
            android.util.Log.e(r1, r0, r4)
        L_0x0049:
            throw r3
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.helper.picturetool.GifDecoder.read(java.lang.String):int");
    }

    private void allcateBitmapData(int i) {
        byte[] bArr = this.pixels;
        if (bArr == null || bArr.length < i) {
            this.pixels = new byte[i];
        }
        if (this.prefix == null) {
            this.prefix = new short[MAX_STACK_SIZE];
        }
        if (this.suffix == null) {
            this.suffix = new byte[MAX_STACK_SIZE];
        }
        if (this.pixelStack == null) {
            this.pixelStack = new byte[4097];
        }
    }

    /* JADX WARNING: type inference failed for: r4v12, types: [short[]] */
    /* JADX WARNING: type inference failed for: r3v10, types: [short] */
    /* access modifiers changed from: protected */
    public void decodeBitmapData() {
        int i;
        int i2;
        int i3;
        byte b;
        byte b2;
        byte b3;
        int i4 = this.iw * this.ih;
        allcateBitmapData(i4);
        int read = read();
        int i5 = 1 << read;
        int i6 = i5 + 1;
        int i7 = i5 + 2;
        int i8 = read + 1;
        int i9 = (1 << i8) - 1;
        for (int i10 = 0; i10 < i5; i10++) {
            this.prefix[i10] = 0;
            this.suffix[i10] = (byte) i10;
        }
        int i11 = i8;
        int i12 = i9;
        int i13 = 0;
        int i14 = 0;
        int i15 = 0;
        int i16 = 0;
        int i17 = 0;
        int i18 = 0;
        byte b4 = 0;
        int i19 = 0;
        byte b5 = -1;
        int i20 = i7;
        while (i13 < i4) {
            if (i14 == 0) {
                if (i15 >= i11) {
                    byte b6 = i16 & i12;
                    i16 >>= i11;
                    i15 -= i11;
                    if (b6 > i20 || b6 == i6) {
                        break;
                    } else if (b6 == i5) {
                        i11 = i8;
                        i20 = i7;
                        i12 = i9;
                        b5 = -1;
                    } else {
                        byte b7 = b5;
                        i3 = i8;
                        byte b8 = b7;
                        if (b8 == -1) {
                            this.pixelStack[i14] = this.suffix[b6];
                            b4 = b6;
                            i14++;
                            i8 = i3;
                            i6 = i6;
                            b5 = b4;
                        } else {
                            i = i6;
                            if (b6 == i20) {
                                b3 = b6;
                                this.pixelStack[i14] = (byte) b4;
                                b6 = b8;
                                i14++;
                            } else {
                                b3 = b6;
                            }
                            byte b9 = b6;
                            while (b9 > i5) {
                                this.pixelStack[i14] = this.suffix[b9];
                                i14++;
                                i5 = i5;
                                b9 = this.prefix[b9];
                            }
                            i2 = i5;
                            byte[] bArr = this.suffix;
                            b = bArr[b9] & 255;
                            if (i20 >= MAX_STACK_SIZE) {
                                break;
                            }
                            int i21 = i14 + 1;
                            byte b10 = (byte) b;
                            this.pixelStack[i14] = b10;
                            this.prefix[i20] = (short) b8;
                            bArr[i20] = b10;
                            i20++;
                            if ((i20 & i12) == 0 && i20 < MAX_STACK_SIZE) {
                                i11++;
                                i12 += i20;
                            }
                            b2 = b3;
                            i14 = i21;
                        }
                    }
                } else {
                    if (i17 == 0) {
                        i17 = readBlock();
                        if (i17 <= 0) {
                            break;
                        }
                        i18 = 0;
                    }
                    i16 += (this.block[i18] & 255) << i15;
                    i15 += 8;
                    i18++;
                    i17--;
                }
            } else {
                i = i6;
                b = b4;
                i2 = i5;
                byte b11 = b5;
                i3 = i8;
                b2 = b11;
            }
            i14--;
            this.pixels[i19] = this.pixelStack[i14];
            i13++;
            i19++;
            i5 = i2;
            i6 = i;
            b4 = b;
            int i22 = i3;
            b5 = b2;
            i8 = i22;
        }
        for (int i23 = i19; i23 < i4; i23++) {
            this.pixels[i23] = 0;
        }
    }

    /* access modifiers changed from: protected */
    public boolean err() {
        return this.status != 0;
    }

    /* access modifiers changed from: protected */
    public void init() {
        this.status = 0;
        this.frameCount = 0;
        this.frames = new Vector<>();
        this.gct = null;
        this.lct = null;
    }

    /* access modifiers changed from: protected */
    public int read() {
        try {
            return this.in.read();
        } catch (Exception unused) {
            this.status = 1;
            return 0;
        }
    }

    /* access modifiers changed from: protected */
    public int readBlock() {
        int read = read();
        this.blockSize = read;
        int i = 0;
        if (read > 0) {
            while (true) {
                try {
                    int i2 = this.blockSize;
                    if (i >= i2) {
                        break;
                    }
                    int read2 = this.in.read(this.block, i, i2 - i);
                    if (read2 == -1) {
                        break;
                    }
                    i += read2;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (i < this.blockSize) {
                this.status = 1;
            }
        }
        return i;
    }

    /* access modifiers changed from: protected */
    public int[] readColorTable(int i) {
        int i2;
        int i3 = i * 3;
        byte[] bArr = new byte[i3];
        try {
            i2 = this.in.read(bArr);
        } catch (Exception e) {
            e.printStackTrace();
            i2 = 0;
        }
        if (i2 < i3) {
            this.status = 1;
            return null;
        }
        int[] iArr = new int[256];
        int i4 = 0;
        for (int i5 = 0; i5 < i; i5++) {
            int i6 = i4 + 1;
            int i7 = i6 + 1;
            iArr[i5] = ((bArr[i4] & 255) << 16) | -16777216 | ((bArr[i6] & 255) << 8) | (bArr[i7] & 255);
            i4 = i7 + 1;
        }
        return iArr;
    }

    /* access modifiers changed from: protected */
    public void readContents() {
        boolean z = false;
        while (!z && !err()) {
            int read = read();
            Log.d(LOG_TAG, "code=" + read);
            if (read == 33) {
                int read2 = read();
                if (read2 == 1) {
                    skip();
                } else if (read2 == 249) {
                    readGraphicControlExt();
                } else if (read2 == 254) {
                    skip();
                } else if (read2 != 255) {
                    skip();
                } else {
                    readBlock();
                    char[] cArr = new char[11];
                    for (int i = 0; i < 11; i++) {
                        cArr[i] = (char) this.block[i];
                    }
                    if ("NETSCAPE2.0".equals(new String(cArr))) {
                        readNetscapeExt();
                    } else {
                        skip();
                    }
                }
            } else if (read != 44) {
                if (read != 59) {
                    this.status = 1;
                } else {
                    z = true;
                }
            } else if (this.frameCount < 50) {
                readBitmap();
            } else {
                return;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void readGraphicControlExt() {
        read();
        int read = read();
        int i = (read & 28) >> 2;
        this.dispose = i;
        boolean z = true;
        if (i == 0) {
            this.dispose = 1;
        }
        if ((read & 1) == 0) {
            z = false;
        }
        this.transparency = z;
        this.delay = readShort() * 10;
        this.transIndex = read();
        read();
    }

    /* access modifiers changed from: protected */
    public void readHeader() {
        char[] cArr = new char[6];
        for (int i = 0; i < 6; i++) {
            cArr[i] = (char) read();
        }
        String str = new String(cArr);
        String str2 = LOG_TAG;
        Log.d(str2, "readHeader: start=" + str);
        if (!str.startsWith("GIF")) {
            this.status = 1;
            return;
        }
        readLSD();
        if (this.gctFlag && !err()) {
            int[] readColorTable = readColorTable(this.gctSize);
            this.gct = readColorTable;
            if (readColorTable != null) {
                this.bgColor = readColorTable[this.bgIndex];
            }
        }
    }

    /* access modifiers changed from: protected */
    public void readBitmap() {
        this.ix = readShort();
        this.iy = readShort();
        this.iw = readShort();
        this.ih = readShort();
        int read = read();
        int i = 0;
        this.lctFlag = (read & 128) != 0;
        int pow = (int) Math.pow(2.0d, (double) ((read & 7) + 1));
        this.lctSize = pow;
        this.interlace = (read & 64) != 0;
        if (this.lctFlag) {
            int[] readColorTable = readColorTable(pow);
            this.lct = readColorTable;
            this.act = readColorTable;
        } else {
            this.act = this.gct;
            if (this.bgIndex == this.transIndex) {
                this.bgColor = 0;
            }
        }
        if (this.act == null) {
            this.status = 1;
        } else if (!err()) {
            if (this.transparency) {
                int[] iArr = this.act;
                int i2 = this.transIndex;
                int i3 = iArr[i2];
                iArr[i2] = 0;
                i = i3;
            }
            decodeBitmapData();
            skip();
            if (!err()) {
                this.frameCount++;
                this.image = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888);
                setPixels();
                this.frames.addElement(new GifFrame(this.image, this.delay));
                if (this.transparency) {
                    this.act[this.transIndex] = i;
                }
                resetFrame();
            }
        }
    }

    /* access modifiers changed from: protected */
    public void readLSD() {
        this.width = readShort();
        this.height = readShort();
        Log.d(LOG_TAG, "w=" + this.width + ", h=" + this.height);
        int read = read();
        this.gctFlag = (read & 128) != 0;
        this.gctSize = 2 << (read & 7);
        this.bgIndex = read();
        this.pixelAspect = read();
        String str = LOG_TAG;
        Log.d(str, "pixelAspect=" + this.pixelAspect + ", gctSize=" + this.gctSize + ", gctFlag=" + this.gctFlag);
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:0:0x0000 A[LOOP_START, MTH_ENTER_BLOCK] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void readNetscapeExt() {
        /*
            r3 = this;
        L_0x0000:
            r3.readBlock()
            byte[] r0 = r3.block
            r1 = 0
            byte r1 = r0[r1]
            r2 = 1
            if (r1 != r2) goto L_0x0019
            byte r1 = r0[r2]
            r1 = r1 & 255(0xff, float:3.57E-43)
            r2 = 2
            byte r0 = r0[r2]
            r0 = r0 & 255(0xff, float:3.57E-43)
            int r0 = r0 << 8
            r0 = r0 | r1
            r3.loopCount = r0
        L_0x0019:
            int r0 = r3.blockSize
            if (r0 <= 0) goto L_0x0023
            boolean r0 = r3.err()
            if (r0 == 0) goto L_0x0000
        L_0x0023:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.helper.picturetool.GifDecoder.readNetscapeExt():void");
    }

    /* access modifiers changed from: protected */
    public int readShort() {
        return (read() << 8) | read();
    }

    /* access modifiers changed from: protected */
    public void resetFrame() {
        this.lastDispose = this.dispose;
        this.lrx = this.ix;
        this.lry = this.iy;
        this.lrw = this.iw;
        this.lrh = this.ih;
        this.lastBitmap = this.image;
        this.lastBgColor = this.bgColor;
        this.dispose = 0;
        this.transparency = false;
        this.delay = 0;
        this.lct = null;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:0:0x0000 A[LOOP_START, MTH_ENTER_BLOCK] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void skip() {
        /*
            r1 = this;
        L_0x0000:
            r1.readBlock()
            int r0 = r1.blockSize
            if (r0 <= 0) goto L_0x000d
            boolean r0 = r1.err()
            if (r0 == 0) goto L_0x0000
        L_0x000d:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.helper.picturetool.GifDecoder.skip():void");
    }

    public void clean() {
        int size = this.frames.size();
        for (int i = 0; i < size; i++) {
            this.frames.get(i).image.recycle();
            this.frames.get(i).image = null;
        }
        this.frames.clear();
    }

    public Vector<GifFrame> getFrames() {
        return this.frames;
    }
}
