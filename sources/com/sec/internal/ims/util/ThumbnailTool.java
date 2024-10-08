package com.sec.internal.ims.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.Pair;
import com.sec.internal.constants.ims.SipMsg;
import com.sec.internal.constants.ims.servicemodules.volte2.CallConstants;
import com.sec.internal.helper.FileUtils;
import com.sec.internal.helper.picturetool.BitmapExtractor;
import com.sec.internal.helper.picturetool.ComplexImageExtractor;
import com.sec.internal.helper.picturetool.ContentTypeContextCreator;
import com.sec.internal.helper.picturetool.FullCompressionDescriptor;
import com.sec.internal.helper.picturetool.ICompressionDescriptor;
import com.sec.internal.helper.picturetool.IContentTypeContext;
import com.sec.internal.helper.picturetool.IVideoPreviewExtractor;
import com.sec.internal.helper.picturetool.ImageDimensionsExtractor;
import com.sec.internal.helper.picturetool.PanicCompressionDescriptor;
import com.sec.internal.helper.picturetool.ReadScaleCalculator;
import com.sec.internal.helper.picturetool.UniqueFilePathResolver;
import com.sec.internal.helper.picturetool.VideoPreviewExtractor;
import com.sec.internal.helper.translate.ContentTypeTranslator;
import com.sec.internal.ims.util.IThumbnailTool;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class ThumbnailTool extends Handler implements IThumbnailTool {
    private static final int EVT_THUMBNAIL_CREATE = 1000;
    private static final int EVT_THUMBNAIL_CREATE_FROM_IMAGE = 1001;
    private static final int EVT_THUMBNAIL_CREATE_FROM_IMAGE_AS_SIZE = 1002;
    private static final int EVT_THUMBNAIL_CREATE_FROM_VIDEO = 1003;
    private static final int EVT_THUMBNAIL_CREATE_FROM_VIDEO_AS_SIZE = 1004;
    private static final String EXT_PNG = "image/png";
    private static final Set<String> IMAGE_EXTENSIONS = new HashSet(Arrays.asList(new String[]{"JPG", "JPEG", "BMP", "PNG", "GIF", "WBMP"}));
    private static final String LOG_TAG = "ThumbnailTool";
    private static final String SUBDIR_NAME = ".rcs_thumbnail";
    private static final Set<String> VIDEO_EXTENSIONS = new HashSet(Arrays.asList(new String[]{"3GP", "MP4", "AVI"}));
    private BitmapExtractor mBitmapExtractor;
    private ComplexImageExtractor mComplexImageExtractor = new ComplexImageExtractor();
    private ContentTypeContextCreator mContentTypeContextCreator;
    private Context mContext;
    private ImageDimensionsExtractor mImageDimensionsExtractor = new ImageDimensionsExtractor();
    private ICompressionDescriptor mPanicDescriptor;
    private String mSavedDir = null;
    private VideoPreviewExtractor mVideoPreviewExtractor;

    private static class ThumbnailInfor {
        /* access modifiers changed from: private */
        public Message callback;
        /* access modifiers changed from: private */
        public String destFilePath;
        /* access modifiers changed from: private */
        public int height;
        /* access modifiers changed from: private */
        public long maxSize;
        /* access modifiers changed from: private */
        public String originalFilePath;
        /* access modifiers changed from: private */
        public int width;

        private ThumbnailInfor() {
        }
    }

    private String getFileExtension(String str) {
        int lastIndexOf = str.lastIndexOf(".");
        if (lastIndexOf < 0) {
            return null;
        }
        return str.substring(lastIndexOf + 1).toUpperCase(Locale.ENGLISH);
    }

    public ThumbnailTool(Context context, Looper looper) {
        super(looper);
        this.mContext = context;
        this.mPanicDescriptor = new PanicCompressionDescriptor();
        this.mBitmapExtractor = new BitmapExtractor();
        this.mContentTypeContextCreator = new ContentTypeContextCreator();
        this.mVideoPreviewExtractor = new VideoPreviewExtractor(this.mBitmapExtractor);
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void handleMessage(android.os.Message r11) {
        /*
            r10 = this;
            java.lang.Object r0 = r11.obj
            com.sec.internal.ims.util.ThumbnailTool$ThumbnailInfor r0 = (com.sec.internal.ims.util.ThumbnailTool.ThumbnailInfor) r0
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "handleMessage: "
            r1.append(r2)
            int r2 = r11.what
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            java.lang.String r2 = "ThumbnailTool"
            android.util.Log.d(r2, r1)
            int r11 = r11.what
            r1 = 0
            switch(r11) {
                case 1000: goto L_0x00b9;
                case 1001: goto L_0x0096;
                case 1002: goto L_0x0071;
                case 1003: goto L_0x004e;
                case 1004: goto L_0x0029;
                default: goto L_0x0022;
            }
        L_0x0022:
            java.lang.String r10 = "Unsupport file format!!!"
            android.util.Log.d(r2, r10)
            goto L_0x0141
        L_0x0029:
            java.io.File r4 = new java.io.File
            java.lang.String r11 = r0.originalFilePath
            r4.<init>(r11)
            java.io.File r5 = new java.io.File
            java.lang.String r11 = r0.destFilePath
            r5.<init>(r11)
            long r6 = r0.maxSize
            int r8 = r0.width
            int r9 = r0.height
            r3 = r10
            java.lang.String r10 = r3.createThumbFromVideo(r4, r5, r6, r8, r9)
            goto L_0x0142
        L_0x004e:
            java.io.File r3 = new java.io.File
            java.lang.String r11 = r0.originalFilePath
            r3.<init>(r11)
            java.io.File r4 = new java.io.File
            java.lang.String r11 = r0.destFilePath
            r4.<init>(r11)
            long r5 = r0.maxSize
            r7 = 2147483647(0x7fffffff, float:NaN)
            r8 = 2147483647(0x7fffffff, float:NaN)
            r2 = r10
            java.lang.String r10 = r2.createThumbFromVideo(r3, r4, r5, r7, r8)
            goto L_0x0142
        L_0x0071:
            java.io.File r3 = new java.io.File
            java.lang.String r11 = r0.originalFilePath
            r3.<init>(r11)
            java.io.File r4 = new java.io.File
            java.lang.String r11 = r0.destFilePath
            r4.<init>(r11)
            long r5 = r0.maxSize
            int r7 = r0.width
            int r8 = r0.height
            r2 = r10
            java.lang.String r10 = r2.createThumbFromImage(r3, r4, r5, r7, r8)
            goto L_0x0142
        L_0x0096:
            java.io.File r3 = new java.io.File
            java.lang.String r11 = r0.originalFilePath
            r3.<init>(r11)
            java.io.File r4 = new java.io.File
            java.lang.String r11 = r0.destFilePath
            r4.<init>(r11)
            long r5 = r0.maxSize
            r7 = 2147483647(0x7fffffff, float:NaN)
            r8 = 2147483647(0x7fffffff, float:NaN)
            r2 = r10
            java.lang.String r10 = r2.createThumbFromImage(r3, r4, r5, r7, r8)
            goto L_0x0142
        L_0x00b9:
            java.lang.String r11 = r0.originalFilePath
            java.lang.String r11 = r10.getFileExtension(r11)
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "handleMessage: original="
            r3.append(r4)
            java.lang.String r4 = r0.originalFilePath
            r3.append(r4)
            java.lang.String r4 = ", fileExtension="
            r3.append(r4)
            r3.append(r11)
            java.lang.String r4 = ", dest="
            r3.append(r4)
            java.lang.String r4 = r0.destFilePath
            r3.append(r4)
            java.lang.String r3 = r3.toString()
            android.util.Log.d(r2, r3)
            java.util.Set<java.lang.String> r2 = IMAGE_EXTENSIONS
            boolean r2 = r2.contains(r11)
            if (r2 == 0) goto L_0x0117
            java.io.File r4 = new java.io.File
            java.lang.String r11 = r0.originalFilePath
            r4.<init>(r11)
            java.io.File r5 = new java.io.File
            java.lang.String r11 = r0.destFilePath
            r5.<init>(r11)
            long r6 = r0.maxSize
            r8 = 2147483647(0x7fffffff, float:NaN)
            r9 = 2147483647(0x7fffffff, float:NaN)
            r3 = r10
            java.lang.String r10 = r3.createThumbFromImage(r4, r5, r6, r8, r9)
            goto L_0x0142
        L_0x0117:
            java.util.Set<java.lang.String> r2 = VIDEO_EXTENSIONS
            boolean r11 = r2.contains(r11)
            if (r11 == 0) goto L_0x0141
            java.io.File r3 = new java.io.File
            java.lang.String r11 = r0.originalFilePath
            r3.<init>(r11)
            java.io.File r4 = new java.io.File
            java.lang.String r11 = r0.destFilePath
            r4.<init>(r11)
            long r5 = r0.maxSize
            r7 = 2147483647(0x7fffffff, float:NaN)
            r8 = 2147483647(0x7fffffff, float:NaN)
            r2 = r10
            java.lang.String r10 = r2.createThumbFromVideo(r3, r4, r5, r7, r8)
            goto L_0x0142
        L_0x0141:
            r10 = r1
        L_0x0142:
            android.os.Message r11 = r0.callback
            if (r11 == 0) goto L_0x0156
            android.os.Message r11 = r0.callback
            com.sec.internal.helper.AsyncResult.forMessage(r11, r10, r1)
            android.os.Message r10 = r0.callback
            r10.sendToTarget()
        L_0x0156:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.util.ThumbnailTool.handleMessage(android.os.Message):void");
    }

    public boolean isSupported(String str) {
        Log.d(LOG_TAG, "The thumbnailFile type is " + str);
        return str.startsWith(CallConstants.ComposerData.IMAGE) || str.startsWith(SipMsg.FEATURE_TAG_MMTEL_VIDEO);
    }

    public String getThumbSavedDirectory() {
        Context context;
        if (this.mSavedDir == null && (context = this.mContext) != null) {
            File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getPath(), SUBDIR_NAME);
            if (file.isDirectory() || file.mkdirs()) {
                this.mSavedDir = file.getAbsolutePath();
            }
            Log.d(LOG_TAG, "getThumbSavedDirectory: " + this.mSavedDir);
        }
        return this.mSavedDir;
    }

    public void createThumb(String str, String str2, long j, Message message) {
        ThumbnailInfor thumbnailInfor = new ThumbnailInfor();
        thumbnailInfor.originalFilePath = str;
        thumbnailInfor.destFilePath = str2;
        thumbnailInfor.maxSize = j;
        thumbnailInfor.callback = message;
        sendMessage(obtainMessage(1000, thumbnailInfor));
    }

    public void createThumbFromImage(String str, String str2, long j, Message message) {
        ThumbnailInfor thumbnailInfor = new ThumbnailInfor();
        thumbnailInfor.originalFilePath = str;
        thumbnailInfor.destFilePath = str2;
        thumbnailInfor.maxSize = j;
        thumbnailInfor.callback = message;
        sendMessage(obtainMessage(1001, thumbnailInfor));
    }

    public void createThumbFromImage(String str, String str2, long j, int i, int i2, Message message) {
        ThumbnailInfor thumbnailInfor = new ThumbnailInfor();
        thumbnailInfor.originalFilePath = str;
        thumbnailInfor.destFilePath = str2;
        thumbnailInfor.maxSize = j;
        thumbnailInfor.callback = message;
        thumbnailInfor.width = i;
        thumbnailInfor.height = i2;
        sendMessage(obtainMessage(1002, thumbnailInfor));
    }

    public void createThumbFromVideo(String str, String str2, long j, Message message) {
        ThumbnailInfor thumbnailInfor = new ThumbnailInfor();
        thumbnailInfor.originalFilePath = str;
        thumbnailInfor.destFilePath = str2;
        thumbnailInfor.maxSize = j;
        thumbnailInfor.callback = message;
        sendMessage(obtainMessage(1003, thumbnailInfor));
    }

    public void createThumbFromVideo(String str, String str2, long j, int i, int i2, Message message) {
        ThumbnailInfor thumbnailInfor = new ThumbnailInfor();
        thumbnailInfor.originalFilePath = str;
        thumbnailInfor.destFilePath = str2;
        thumbnailInfor.maxSize = j;
        thumbnailInfor.callback = message;
        thumbnailInfor.width = i;
        thumbnailInfor.height = i2;
        sendMessage(obtainMessage(1004, thumbnailInfor));
    }

    public String createCopyPaste(File file, File file2) {
        if (file2 == null) {
            Log.e(LOG_TAG, "destinationDirectory == null");
            return null;
        }
        File uniqueFile = UniqueFilePathResolver.getUniqueFile(file.getName(), file2);
        Log.d(LOG_TAG, "createCopyPaste:" + uniqueFile);
        FileUtils.copyFile(file, uniqueFile);
        return uniqueFile.getPath();
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r8v0, resolved type: com.sec.internal.helper.picturetool.FullCompressionDescriptor} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r8v2, resolved type: com.sec.internal.helper.picturetool.FullCompressionDescriptor} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v21, resolved type: com.sec.internal.helper.picturetool.PngLazyCompressionDescriptor} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r8v4, resolved type: com.sec.internal.helper.picturetool.FullCompressionDescriptor} */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Removed duplicated region for block: B:10:0x003d  */
    /* JADX WARNING: Removed duplicated region for block: B:12:0x0043  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private java.lang.String createThumbFromImage(java.io.File r22, java.io.File r23, long r24, int r26, int r27) {
        /*
            r21 = this;
            r0 = r21
            r1 = r22
            r2 = r23
            r13 = r24
            r11 = r26
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "createThumbFromImage: [originalFile="
            r3.append(r4)
            r3.append(r1)
            java.lang.String r4 = ", destinationDirectory="
            r3.append(r4)
            r3.append(r2)
            java.lang.String r3 = r3.toString()
            java.lang.String r12 = "ThumbnailTool"
            android.util.Log.d(r12, r3)
            r15 = 0
            com.sec.internal.helper.picturetool.ComplexImageExtractor r3 = r0.mComplexImageExtractor     // Catch:{ IllegalArgumentException -> 0x0035 }
            java.io.File r1 = r3.extractFrom(r1)     // Catch:{ IllegalArgumentException -> 0x0035 }
            com.sec.internal.helper.picturetool.ComplexImageExtractor r3 = r0.mComplexImageExtractor     // Catch:{ IllegalArgumentException -> 0x0036 }
            r3.release()     // Catch:{ IllegalArgumentException -> 0x0036 }
            goto L_0x003b
        L_0x0035:
            r1 = r15
        L_0x0036:
            java.lang.String r3 = "could not extract complex image"
            android.util.Log.e(r12, r3)
        L_0x003b:
            if (r1 != 0) goto L_0x0043
            java.lang.String r0 = "complexImage == null"
            android.util.Log.e(r12, r0)
            return r15
        L_0x0043:
            long r9 = r1.length()
            android.util.Pair r7 = r0.getImageDimensions(r1)
            if (r7 != 0) goto L_0x0053
            java.lang.String r0 = "imageDimensions == null"
            android.util.Log.e(r12, r0)
            return r15
        L_0x0053:
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "createThumbFromImage: imageSize="
            r3.append(r4)
            r3.append(r9)
            java.lang.String r4 = ", maxSize="
            r3.append(r4)
            r3.append(r13)
            java.lang.String r4 = ", dimension="
            r3.append(r4)
            java.lang.Object r4 = r7.first
            r3.append(r4)
            java.lang.String r4 = ", maxWidth="
            r3.append(r4)
            r3.append(r11)
            java.lang.String r3 = r3.toString()
            android.util.Log.d(r12, r3)
            int r16 = (r9 > r13 ? 1 : (r9 == r13 ? 0 : -1))
            if (r16 > 0) goto L_0x00a0
            java.lang.Object r3 = r7.first
            java.lang.Integer r3 = (java.lang.Integer) r3
            int r3 = r3.intValue()
            if (r3 > r11) goto L_0x00a0
            java.lang.Object r3 = r7.second
            java.lang.Integer r3 = (java.lang.Integer) r3
            int r3 = r3.intValue()
            r8 = r27
            if (r3 > r8) goto L_0x00a2
            java.lang.String r0 = r0.createCopyPaste(r1, r2)
            return r0
        L_0x00a0:
            r8 = r27
        L_0x00a2:
            java.lang.Object r3 = r7.first
            java.lang.Integer r3 = (java.lang.Integer) r3
            int r5 = r3.intValue()
            java.lang.Object r3 = r7.second
            java.lang.Integer r3 = (java.lang.Integer) r3
            int r6 = r3.intValue()
            r3 = r9
            r17 = r7
            r7 = r24
            r18 = r9
            r9 = r26
            r10 = r27
            int r20 = com.sec.internal.helper.picturetool.ReadScaleCalculator.calculate(r3, r5, r6, r7, r9, r10)
            java.lang.String r3 = r1.getName()
            java.lang.String r3 = r0.getFileExtension(r3)
            if (r3 != 0) goto L_0x00d1
            java.lang.String r0 = "ext == null"
            android.util.Log.e(r12, r0)
            return r15
        L_0x00d1:
            java.lang.String r3 = com.sec.internal.helper.translate.ContentTypeTranslator.translate(r3)
            java.lang.String r4 = "image/png"
            boolean r3 = r3.equals(r4)
            if (r3 == 0) goto L_0x00ff
            if (r16 > 0) goto L_0x00ff
            com.sec.internal.helper.picturetool.PngLazyCompressionDescriptor r9 = new com.sec.internal.helper.picturetool.PngLazyCompressionDescriptor
            r3 = r17
            java.lang.Object r4 = r3.first
            java.lang.Integer r4 = (java.lang.Integer) r4
            int r4 = r4.intValue()
            java.lang.Object r3 = r3.second
            java.lang.Integer r3 = (java.lang.Integer) r3
            int r5 = r3.intValue()
            com.sec.internal.helper.picturetool.ICompressionDescriptor r8 = r0.mPanicDescriptor
            r3 = r9
            r6 = r26
            r7 = r27
            r3.<init>(r4, r5, r6, r7, r8)
            r8 = r9
            goto L_0x0122
        L_0x00ff:
            r3 = r17
            com.sec.internal.helper.picturetool.FullCompressionDescriptor r15 = new com.sec.internal.helper.picturetool.FullCompressionDescriptor
            java.lang.Object r4 = r3.first
            java.lang.Integer r4 = (java.lang.Integer) r4
            int r6 = r4.intValue()
            java.lang.Object r3 = r3.second
            java.lang.Integer r3 = (java.lang.Integer) r3
            int r7 = r3.intValue()
            com.sec.internal.helper.picturetool.ICompressionDescriptor r12 = r0.mPanicDescriptor
            r3 = r15
            r4 = r18
            r8 = r24
            r10 = r26
            r11 = r27
            r3.<init>(r4, r6, r7, r8, r10, r11, r12)
            r8 = r15
        L_0x0122:
            r0 = r21
            r2 = r23
            r3 = r20
            r4 = r18
            r6 = r24
            java.lang.String r0 = r0.createThumbFromStillPicture(r1, r2, r3, r4, r6, r8)
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.util.ThumbnailTool.createThumbFromImage(java.io.File, java.io.File, long, int, int):java.lang.String");
    }

    private Bitmap extractBitmapFromImage(File file, int i) throws IOException {
        return this.mBitmapExtractor.extractFromImage(file, i);
    }

    private String createThumbFromStillPicture(File file, File file2, int i, long j, long j2, ICompressionDescriptor iCompressionDescriptor) {
        File file3 = file;
        int i2 = i;
        try {
            try {
                return createThumbFromPicture(file, file2, j, j2, Pair.create(extractBitmapFromImage(file, i), Integer.valueOf(i)), iCompressionDescriptor);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } catch (IThumbnailTool.ThumbCreationException e2) {
                e2.printStackTrace();
                return null;
            }
        } catch (IOException e3) {
            e3.printStackTrace();
            return null;
        }
    }

    private Pair<Integer, Integer> getImageDimensions(File file) {
        return this.mImageDimensionsExtractor.extract(file);
    }

    private IContentTypeContext getContentTypeContext(File file) {
        String fileExtension = getFileExtension(file.getName());
        if (fileExtension == null) {
            Log.e(LOG_TAG, "ext == null");
            return null;
        }
        String translate = ContentTypeTranslator.translate(fileExtension);
        Log.d(LOG_TAG, "getStillContentTypeContext: mime=" + translate);
        return this.mContentTypeContextCreator.getContextByMime(translate);
    }

    /* JADX WARNING: Removed duplicated region for block: B:43:0x015c A[LOOP:0: B:12:0x004f->B:43:0x015c, LOOP_END] */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x00f7 A[SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private java.lang.String createThumbFromPicture(java.io.File r19, java.io.File r20, long r21, long r23, android.util.Pair<android.graphics.Bitmap, java.lang.Integer> r25, com.sec.internal.helper.picturetool.ICompressionDescriptor r26) throws java.io.IOException, com.sec.internal.ims.util.IThumbnailTool.ThumbCreationException {
        /*
            r18 = this;
            r6 = r18
            r7 = r25
            android.content.Context r0 = r6.mContext
            r8 = 0
            java.lang.String r9 = "ThumbnailTool"
            if (r0 == 0) goto L_0x0161
            if (r7 != 0) goto L_0x000f
            goto L_0x0161
        L_0x000f:
            java.lang.Object r0 = r7.first
            if (r0 != 0) goto L_0x0019
            java.lang.String r0 = "originalImage.first == null"
            android.util.Log.e(r9, r0)
            return r8
        L_0x0019:
            com.sec.internal.helper.picturetool.IContentTypeContext r10 = r18.getContentTypeContext(r19)
            if (r10 != 0) goto L_0x0025
            java.lang.String r0 = "mContentTypeContext == null"
            android.util.Log.e(r9, r0)
            return r8
        L_0x0025:
            r10.validateExtension()
            java.lang.String r11 = r19.getName()
            r0 = r20
            java.io.File r12 = r10.getFinalFilePath(r0, r11)
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r1 = "createThumbFromPicture: outputFile="
            r0.append(r1)
            r0.append(r12)
            java.lang.String r0 = r0.toString()
            android.util.Log.d(r9, r0)
            java.util.ArrayList r13 = new java.util.ArrayList
            r13.<init>()
            r0 = r21
            r14 = r26
        L_0x004f:
            android.util.Pair r0 = r14.next(r0)
            android.content.Context r1 = r6.mContext
            java.io.File r1 = r1.getCacheDir()
            if (r1 != 0) goto L_0x0061
            java.lang.String r0 = "file == null"
            android.util.Log.e(r9, r0)
            return r8
        L_0x0061:
            java.io.File r15 = com.sec.internal.helper.picturetool.UniqueFilePathResolver.getUniqueFile(r11, r1)
            r1 = 0
            r13.add(r1, r15)
            r5 = 1
            if (r0 == 0) goto L_0x00d2
            java.lang.Object r1 = r0.second
            java.lang.Integer r1 = (java.lang.Integer) r1
            int r1 = r1.intValue()
            if (r1 != r5) goto L_0x008a
            java.lang.Object r1 = r7.first
            android.graphics.Bitmap r1 = (android.graphics.Bitmap) r1
            java.lang.Object r0 = r0.first
            java.lang.Integer r0 = (java.lang.Integer) r0
            int r0 = r0.intValue()
            android.graphics.Bitmap$CompressFormat r2 = r10.getDestinationFormat()
            r6.saveBitmapToFile(r1, r15, r0, r2)
            goto L_0x00d2
        L_0x008a:
            android.util.Pair r2 = r18.getImageDimensions(r19)
            if (r2 != 0) goto L_0x0096
            java.lang.String r0 = "originalDimensions == null"
            android.util.Log.e(r9, r0)
            return r8
        L_0x0096:
            java.lang.Object r3 = r7.first
            android.graphics.Bitmap r3 = (android.graphics.Bitmap) r3
            java.lang.Object r0 = r0.first
            java.lang.Integer r0 = (java.lang.Integer) r0
            int r4 = r0.intValue()
            android.graphics.Bitmap$CompressFormat r16 = r10.getDestinationFormat()
            java.lang.Object r0 = r2.first
            java.lang.Integer r0 = (java.lang.Integer) r0
            int r0 = r0.intValue()
            int r0 = r0 / r1
            java.lang.Integer r0 = java.lang.Integer.valueOf(r0)
            java.lang.Object r2 = r2.second
            java.lang.Integer r2 = (java.lang.Integer) r2
            int r2 = r2.intValue()
            int r2 = r2 / r1
            java.lang.Integer r1 = java.lang.Integer.valueOf(r2)
            android.util.Pair r17 = android.util.Pair.create(r0, r1)
            r0 = r18
            r1 = r3
            r2 = r15
            r3 = r4
            r4 = r16
            r8 = r5
            r5 = r17
            r0.saveBitmapToFile(r1, r2, r3, r4, r5)
            goto L_0x00d3
        L_0x00d2:
            r8 = r5
        L_0x00d3:
            long r0 = r15.length()
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "createThumbFromPicture: tmpFile="
            r2.append(r3)
            r2.append(r15)
            java.lang.String r3 = ", size="
            r2.append(r3)
            r2.append(r0)
            java.lang.String r2 = r2.toString()
            android.util.Log.d(r9, r2)
            int r2 = (r0 > r23 ? 1 : (r0 == r23 ? 0 : -1))
            if (r2 > 0) goto L_0x015c
            r4 = 5120(0x1400, double:2.5296E-320)
            int r0 = (r0 > r4 ? 1 : (r0 == r4 ? 0 : -1))
            if (r0 > 0) goto L_0x012b
            int r0 = r13.size()
            if (r0 <= r8) goto L_0x012b
            java.lang.Object r0 = r13.get(r8)
            java.io.File r0 = (java.io.File) r0
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "createThumbFromPicture: use previous tmpFile= "
            r1.append(r2)
            r1.append(r0)
            r1.append(r3)
            long r2 = r0.length()
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            android.util.Log.d(r9, r1)
            com.sec.internal.helper.FileUtils.copyFile(r0, r12)
            goto L_0x012e
        L_0x012b:
            com.sec.internal.helper.FileUtils.copyFile(r15, r12)
        L_0x012e:
            java.util.Iterator r0 = r13.iterator()
        L_0x0132:
            boolean r1 = r0.hasNext()
            if (r1 == 0) goto L_0x014b
            java.lang.Object r1 = r0.next()
            java.io.File r1 = (java.io.File) r1
            boolean r1 = r1.delete()
            if (r1 != 0) goto L_0x0132
            java.lang.String r1 = "tmpFileToDelete.delete() error"
            android.util.Log.e(r9, r1)
            goto L_0x0132
        L_0x014b:
            java.lang.Object r0 = r7.first
            android.graphics.Bitmap r0 = (android.graphics.Bitmap) r0
            r0.recycle()
            r2 = r19
            r10.processSpecificData(r2, r12)
            java.lang.String r0 = r12.getPath()
            return r0
        L_0x015c:
            r2 = r19
            r8 = 0
            goto L_0x004f
        L_0x0161:
            java.lang.String r0 = "mContext == null && originalImage == null"
            android.util.Log.e(r9, r0)
            r0 = 0
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.util.ThumbnailTool.createThumbFromPicture(java.io.File, java.io.File, long, long, android.util.Pair, com.sec.internal.helper.picturetool.ICompressionDescriptor):java.lang.String");
    }

    private void saveBitmapToFile(Bitmap bitmap, File file, int i, Bitmap.CompressFormat compressFormat, Pair<Integer, Integer> pair) throws IOException, IThumbnailTool.ThumbCreationException {
        FileOutputStream fileOutputStream = null;
        try {
            FileOutputStream fileOutputStream2 = new FileOutputStream(file);
            try {
                Bitmap createScaledBitmap = Bitmap.createScaledBitmap(bitmap, ((Integer) pair.first).intValue(), ((Integer) pair.second).intValue(), false);
                createScaledBitmap.compress(compressFormat, i, fileOutputStream2);
                if (!createScaledBitmap.sameAs(bitmap)) {
                    createScaledBitmap.recycle();
                }
                fileOutputStream2.flush();
                closeStream(fileOutputStream2);
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

    private void saveBitmapToFile(Bitmap bitmap, File file, int i, Bitmap.CompressFormat compressFormat) throws IOException, IThumbnailTool.ThumbCreationException {
        FileOutputStream fileOutputStream = null;
        try {
            FileOutputStream fileOutputStream2 = new FileOutputStream(file);
            try {
                bitmap.compress(compressFormat, i, fileOutputStream2);
                fileOutputStream2.flush();
                closeStream(fileOutputStream2);
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

    private void closeStream(Closeable closeable) throws IOException {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                throw new IOException("Can't close stream: e=" + e);
            }
        }
    }

    private String createThumbFromVideo(File file, File file2, long j, int i, int i2) {
        try {
            return createThumbFromMotionPicture(file, file2, j, i, i2);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (IThumbnailTool.ThumbCreationException e2) {
            e2.printStackTrace();
            return null;
        }
    }

    private String createThumbFromMotionPicture(File file, File file2, long j, int i, int i2) throws IOException, IThumbnailTool.ThumbCreationException {
        Context context = this.mContext;
        File cacheDir = context != null ? context.getCacheDir() : null;
        if (cacheDir == null) {
            Log.e(LOG_TAG, "file == null");
            return null;
        }
        IVideoPreviewExtractor.IVideoPreview extract = this.mVideoPreviewExtractor.extract(file, cacheDir);
        File file3 = extract.getFile();
        Bitmap extractFromImage = this.mBitmapExtractor.extractFromImage(file3, 1);
        Pair<Integer, Integer> dimensions = extract.getDimensions();
        long size = extract.getSize();
        try {
            String createThumbFromMotionPicture = createThumbFromMotionPicture(file3, file2, size, j, extractFromImage, new FullCompressionDescriptor(size, ((Integer) dimensions.first).intValue(), ((Integer) dimensions.second).intValue(), j, i, i2, this.mPanicDescriptor));
            extractFromImage.recycle();
            if (!file3.delete()) {
                Log.e(LOG_TAG, "tmpFile.delete() error");
            }
            return createThumbFromMotionPicture;
        } catch (Throwable th) {
            Throwable th2 = th;
            extractFromImage.recycle();
            if (!file3.delete()) {
                Log.e(LOG_TAG, "tmpFile.delete() error");
            }
            throw th2;
        }
    }

    private String createThumbFromMotionPicture(File file, File file2, long j, long j2, Bitmap bitmap, ICompressionDescriptor iCompressionDescriptor) throws IOException, IThumbnailTool.ThumbCreationException {
        return createThumbFromPicture(file, file2, j, j2, Pair.create(bitmap, Integer.valueOf(getReadScale(j, j2))), iCompressionDescriptor);
    }

    private int getReadScale(long j, long j2) {
        return ReadScaleCalculator.calculate(j, j2);
    }
}
