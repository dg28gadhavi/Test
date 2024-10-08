package com.sec.internal.helper.translate;

import com.sec.internal.constants.ims.MIMEContentType;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.cmstore.McsConstants;
import com.sec.internal.constants.ims.entitilement.SoftphoneContract;
import com.sec.internal.constants.ims.servicemodules.sms.SmsMessage;
import com.sec.internal.helper.translate.MappingTranslator;
import com.sec.internal.ims.servicemodules.csh.event.ICshConstants;
import com.sec.internal.ims.servicemodules.volte2.idc.IdcExtra;

public class ContentTypeTranslator {
    private static final MappingTranslator<String, String> sExtensionTranslator = new MappingTranslator.Builder().map("ez", "application/andrew-inset").map("tsp", "application/dsptype").map("hta", "application/hta").map("hqx", "application/mac-binhex40").map("nb", "application/mathematica").map("mdb", "application/msaccess").map("doc", "application/msword").map("dot", "application/msword").map("oda", "application/oda").map("pdf", "application/pdf").map(McsConstants.BundleData.KEY, "application/pgp-keys").map("pgp", "application/pgp-signature").map("prf", "application/pics-rules").map("cer", "application/pkix-cert").map("rar", "application/rar").map("rdf", "application/rdf+xml").map("rss", "application/rss+xml").map("ssf", "application/ssf").map("apk", "application/vnd.android.package-archive").map("cdy", "application/vnd.cinderella").map("kml", "application/vnd.google-earth.kml+xml").map("kmz", "application/vnd.google-earth.kmz").map("xls", "application/vnd.ms-excel").map("xlt", "application/vnd.ms-excel").map("stl", "application/vnd.ms-pki.stl").map("pot", "application/vnd.ms-powerpoint").map("pps", "application/vnd.ms-powerpoint").map("ppt", "application/vnd.ms-powerpoint").map("odb", "application/vnd.oasis.opendocument.database").map("odf", "application/vnd.oasis.opendocument.formula").map("odg", "application/vnd.oasis.opendocument.graphics").map("otg", "application/vnd.oasis.opendocument.graphics-template").map("odi", "application/vnd.oasis.opendocument.image").map("ods", "application/vnd.oasis.opendocument.spreadsheet").map("ots", "application/vnd.oasis.opendocument.spreadsheet-template").map("odt", "application/vnd.oasis.opendocument.text").map("odm", "application/vnd.oasis.opendocument.text-master").map("ott", "application/vnd.oasis.opendocument.text-template").map("oth", "application/vnd.oasis.opendocument.text-web").map("dcf", "application/vnd.oma.drm.content").map("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation").map("ppsx", "application/vnd.openxmlformats-officedocument.presentationml.slideshow").map("potx", "application/vnd.openxmlformats-officedocument.presentationml.template").map("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet").map("xltx", "application/vnd.openxmlformats-officedocument.spreadsheetml.template").map("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document").map("dotx", "application/vnd.openxmlformats-officedocument.wordprocessingml.template").map("cod", "application/vnd.rim.cod").map("mmf", "application/vnd.smaf").map("sdc", "application/vnd.stardivision.calc").map("sda", "application/vnd.stardivision.draw").map("sdd", "application/vnd.stardivision.impress").map(IdcExtra.Key.SDP, "application/vnd.stardivision.impress").map("smf", "application/ogg").map("sdw", "application/vnd.stardivision.writer").map("vor", "application/vnd.stardivision.writer").map("sgl", "application/vnd.stardivision.writer-global").map("sxc", "application/vnd.sun.xml.calc").map("stc", "application/vnd.sun.xml.calc.template").map("sxd", "application/vnd.sun.xml.draw").map("std", "application/vnd.sun.xml.draw.template").map("sxi", "application/vnd.sun.xml.impress").map("sti", "application/vnd.sun.xml.impress.template").map("sxm", "application/vnd.sun.xml.math").map("sxw", "application/vnd.sun.xml.writer").map("sxg", "application/vnd.sun.xml.writer.global").map("stw", "application/vnd.sun.xml.writer.template").map("vsd", "application/vnd.visio").map("7z", "application/x-7z-compressed").map("abw", "application/x-abiword").map("dmg", "application/x-apple-diskimage").map("bcpio", "application/x-bcpio").map("torrent", "application/x-bittorrent").map("cdf", "application/x-cdf").map("vcd", "application/x-cdlink").map("pgn", "application/x-chess-pgn").map("cpio", "application/x-cpio").map("deb", "application/x-debian-package").map("udeb", "application/x-debian-package").map("dcr", "application/x-director").map(ICshConstants.ShareDatabase.KEY_SHARE_DIRECTION, "application/x-director").map("dxr", "application/x-director").map("dms", "application/x-dms").map("wad", "application/x-doom").map("dvi", "application/x-dvi").map("gsf", "application/x-font").map("pcf", "application/x-font").map("pcf.z", "application/x-font").map("pfa", "application/x-font").map("pfb", "application/x-font").map("mm", "application/x-freemind").map("spl", "application/x-futuresplash").map("gnumeric", "application/x-gnumeric").map("sgf", "application/x-go-sgf").map("gcf", "application/x-graphing-calculator").map("gtar", "application/x-gtar").map("taz", "application/x-gtar").map("tgz", "application/x-gtar").map("hdf", "application/x-hdf").map("xhtml", "application/xhtml+xml").map("ica", "application/x-ica").map("ins", "application/x-internet-signup").map("isp", "application/x-internet-signup").map("iii", "application/x-iphone").map("iso", "application/x-iso9660-image").map("jmz", "application/x-jmol").map("chrt", "application/x-kchart").map("kil", "application/x-killustrator").map("skd", "application/x-koan").map("skm", "application/x-koan").map("skp", "application/x-koan").map("skt", "application/x-koan").map("kpr", "application/x-kpresenter").map("kpt", "application/x-kpresenter").map("ksp", "application/x-kspread").map("kwd", "application/x-kword").map("kwt", "application/x-kword").map("latex", "application/x-latex").map("lha", "application/x-lha").map("lzh", "application/x-lzh").map("lzx", "application/x-lzx").map("book", "application/x-maker").map("fb", "application/x-maker").map("fbdoc", "application/x-maker").map("frame", "application/x-maker").map("frm", "application/x-maker").map("maker", "application/x-maker").map("mif", "application/x-mif").map("msi", "application/x-msi").map("wmd", "application/x-ms-wmd").map("wmz", "application/x-ms-wmz").map("pac", "application/x-ns-proxy-autoconfig").map("nwc", "application/x-nwc").map("o", "application/x-object").map("oza", "application/x-oz-application").map("pem", "application/x-pem-file").map("p12", "application/x-pkcs12").map("pfx", "application/x-pkcs12").map("p7r", "application/x-pkcs7-certreqresp").map("crl", "application/x-pkcs7-crl").map("qtl", "application/x-quicktimeplayer").map("sasf", "application/x-sasf").map("shar", "application/x-shar").map("swf", "application/x-shockwave-flash").map("sit", "application/x-stuffit").map("sv4cpio", "application/x-sv4cpio").map("sv4crc", "application/x-sv4crc").map("tar", "application/x-tar").map("texi", "application/x-texinfo").map("texinfo", "application/x-texinfo").map("roff", "application/x-troff").map("t", "application/x-troff").map("man", "application/x-troff-man").map("ustar", "application/x-ustar").map("src", "application/x-wais-source").map("webarchive", "application/x-webarchive").map("webarchivexml", "application/x-webarchive-xml").map("wz", "application/x-wingz").map("crt", "application/x-x509-ca-cert").map("xcf", "application/x-xcf").map("fig", "application/x-xfig").map(SoftphoneContract.AddressColumns.ZIP, "application/zip").map("sdoc", "application/sdoc").map("show", "application/hshow").map("hwp", "application/hancomhwdt").map("hwt", "application/haansofthwt").map("hwdt", "application/hancomhwdt").map("memo", "application/memo").map("rtx", "application/ogg").map("rtf", "application/rtf").map("spd", "application/spd").map("snb", "application/snb").map("3ga", "audio/3gpp").map("aac", "audio/aac").map("amr", "audio/amr").map("awb", "audio/amr-wb").map("snd", "audio/basic").map("flac", "audio/flac").map("imy", "audio/imelody").map("kar", "audio/midi").map(CloudMessageProviderContract.BufferDBMMSpart.MID, "audio/midi").map("midi", "audio/midi").map("ota", "audio/midi").map("rtttl", "audio/midi").map("xmf", "audio/midi").map("mxmf", "audio/mobile-xmf").map("mp4_a", "audio/mp4").map("m4a", "audio/mpeg").map("mp2", "audio/mpeg").map("mp3", "audio/mpeg").map("mpega", "audio/mpeg").map("mpga", "audio/mpeg").map("m3u", "audio/mpegurl").map("oga", "audio/ogg").map("ogg", "audio/ogg").map("sid", "audio/prs.sid").map("qcp", "audio/qcelp").map("aif", "audio/x-aiff").map("aifc", "audio/x-aiff").map("aiff", "audio/x-aiff").map("gsm", "audio/x-gsm").map("mka", "audio/x-matroska").map("wax", "audio/x-ms-wax").map("wma", "audio/x-ms-wma").map("ra", "audio/x-pn-realaudio").map("ram", "audio/x-pn-realaudio").map("rm", "audio/x-pn-realaudio").map("pls", "audio/x-scpls").map("sd2", "audio/x-sd2").map("wav", "audio/x-wav").map("bmp", "image/bmp").map("gif", "image/gif").map("cur", "image/ico").map("ief", "image/ief").map("jpe", "image/jpeg").map("jpeg", "image/jpeg").map("jpg", "image/jpeg").map("map", "image/jpeg").map("pcx", "image/pcx").map("png", "image/png").map("svg", "image/svg+xml").map("svgz", "image/svg+xml").map("tif", "image/tiff").map("tiff", "image/tiff").map("djv", "image/vnd.djvu").map("djvu", "image/vnd.djvu").map("wbmp", "image/vnd.wap.wbmp").map("webp", "image/webp").map("ras", "image/x-cmu-raster").map("cdr", "image/x-coreldraw").map("pat", "image/x-coreldrawpattern").map("cdt", "image/x-coreldrawtemplate").map("cpt", "image/x-corelphotopaint").map("ico", "image/x-icon").map("art", "image/x-jg").map("jng", "image/x-jng").map("psd", "image/x-photoshop").map("pnm", "image/x-portable-anymap").map("pbm", "image/x-portable-bitmap").map("pgm", "image/x-portable-graymap").map("ppm", "image/x-portable-pixmap").map("rgb", "image/x-rgb").map("xbm", "image/x-xbitmap").map("xpm", "image/x-xpixmap").map("xwd", "image/x-xwindowdump").map("iges", "model/iges").map("igs", "model/iges").map("mesh", "model/mesh").map("msh", "model/mesh").map("silo", "model/mesh").map("ics", "text/calendar").map("icz", "text/calendar").map("csv", "text/comma-separated-values").map("css", "text/css").map("323", "text/h323").map("htm", "text/html").map("html", "text/html").map("uls", "text/iuls").map("mml", "text/mathml").map("asc", MIMEContentType.PLAIN_TEXT).map("diff", MIMEContentType.PLAIN_TEXT).map("po", MIMEContentType.PLAIN_TEXT).map("text", MIMEContentType.PLAIN_TEXT).map("txt", MIMEContentType.PLAIN_TEXT).map("tsv", "text/tab-separated-values").map("phps", "text/text").map("bib", "text/x-bibtex").map("boo", "text/x-boo").map("h++", "text/x-c++hdr").map("hh", "text/x-c++hdr").map("hpp", "text/x-c++hdr").map("hxx", "text/x-c++hdr").map("c++", "text/x-c++src").map("cc", "text/x-c++src").map("cpp", "text/x-c++src").map("cxx", "text/x-c++src").map("h", "text/x-chdr").map("htc", "text/x-component").map("csh", "text/x-csh").map("c", "text/x-csrc").map("d", "text/x-dsrc").map("hs", "text/x-haskell").map("java", "text/x-java").map("lhs", "text/x-literate-haskell").map(MIMEContentType.XML, "text/xml").map("moc", "text/x-moc").map("p", "text/x-pascal").map("pas", "text/x-pascal").map("gcd", "text/x-pcs-gcd").map("etx", "text/x-setext").map("tcl", "text/x-tcl").map("cls", "text/x-tex").map("ltx", "text/x-tex").map("sty", "text/x-tex").map("tex", "text/x-tex").map("vcs", "text/x-vcalendar").map("vcf", "text/x-vcard").map("vnt", "text/x-vnote").map("vts", "text/x-vtodo").map(SmsMessage.FORMAT_3GPP, "video/3gpp").map("3g2", "video/3gpp2").map(SmsMessage.FORMAT_3GPP2, "video/3gpp2").map("avi", "video/avi").map("divx", "video/divx").map("dl", "video/dl").map("dif", "video/dv").map("dv", "video/dv").map("fli", "video/fli").map("m4v", "video/m4v").map("ts", "video/mp2ts").map("mpe", "video/mpeg").map("mpeg", "video/mpeg").map("mpg", "video/mpeg").map("vob", "video/mpeg").map("mov", "video/quicktime").map("qt", "video/quicktime").map("mxu", "video/vnd.mpegurl").map("webm", "video/webm").map("lsf", "video/x-la-asf").map("lsx", "video/x-la-asf").map("mkv", "video/x-matroska").map("mng", "video/x-mng").map("asx", "video/x-ms-asf").map("wm", "video/x-ms-wm").map("wmv", "video/x-ms-wmv").map("wmx", "video/x-ms-wmx").map("wvx", "video/x-ms-wvx").map("movie", "video/x-sgi-movie").map("wrf", "video/x-webex").map("flv", "video/Flv").map("trp", "video/mp2ts").map("ice", "x-conference/x-cooltalk").map("sisx", "x-epoc/x-sisx-app").buildTranslator();

    public static String translate(String str) throws NullPointerException, TranslationException {
        return sExtensionTranslator.translate(str.toLowerCase());
    }

    public static boolean isTranslationDefined(String str) throws NullPointerException {
        return sExtensionTranslator.isTranslationDefined(str.toLowerCase());
    }
}
