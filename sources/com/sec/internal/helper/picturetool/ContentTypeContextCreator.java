package com.sec.internal.helper.picturetool;

public class ContentTypeContextCreator {
    private static final String EXT_3GP_0 = "video/3gpp";
    private static final String EXT_3GP_1 = "video/3gpp2";
    private static final String EXT_BMP = "image/x-ms-bmp";
    private static final String EXT_BMP_1 = "image/bmp";
    private static final String EXT_JPG = "image/jpeg";
    private static final String EXT_MP4 = "video/mp4";
    private static final String EXT_PNG = "image/png";
    private static final String EXT_WBMP = "image/vnd.wap.wbmp";
    private DefaultContext mDefaultCntxt = new DefaultContext(this.mExifProcessor);
    private ExifProcessor mExifProcessor = new ExifProcessor();
    private UnsupportedContext mUnsupportCntxt = new UnsupportedContext();
    private JpgContext mjpgCntxt = new JpgContext(this.mExifProcessor);
    private PngContext mpngCntxt = new PngContext();

    public IContentTypeContext getContextByMime(String str) {
        if (str.equals(EXT_BMP) || str.equals(EXT_BMP_1) || str.equals(EXT_WBMP) || str.equals(EXT_MP4) || str.equals(EXT_3GP_0) || str.equals(EXT_3GP_1)) {
            return this.mDefaultCntxt;
        }
        if (str.equals(EXT_JPG)) {
            return this.mjpgCntxt;
        }
        if (str.equals(EXT_PNG)) {
            return this.mpngCntxt;
        }
        return this.mUnsupportCntxt;
    }
}
