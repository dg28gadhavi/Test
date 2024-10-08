package com.sec.internal.ims.servicemodules.im.util;

import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.servicemodules.im.FtHttpFileInfo;
import com.sec.internal.ims.servicemodules.im.interfaces.IFtHttpXmlComposer;

public class FtHttpXmlComposer implements IFtHttpXmlComposer {
    private static final String LOG_TAG = "FtHttpXmlComposer";

    public String composeXmlForAudioMessage(FtHttpFileInfo ftHttpFileInfo, int i) {
        Log.i(LOG_TAG, "buildXMLForAudioMessage");
        StringBuilder sb = new StringBuilder();
        appendXmlVersionAndEncoding(sb);
        sb.append("<file");
        appendFtHttpNamespace(sb);
        appendAudioMessagingNamespace(sb);
        if (!TextUtils.isEmpty(ftHttpFileInfo.getBrandedUrl())) {
            appendFtHttpExtNamespace(sb);
        }
        sb.append(">\n");
        if (ftHttpFileInfo.isThumbnailExist()) {
            appendThumbnail(sb, ftHttpFileInfo);
        }
        sb.append("\t<file-info type=\"file\" file-disposition=\"render\">\n");
        appendFileSize(sb, ftHttpFileInfo.getFileSize());
        appendFileName(sb, ftHttpFileInfo.getFileName());
        appendContentType(sb, ftHttpFileInfo.getContentType());
        appendPlayingLength(sb, i);
        appendDataUrlAndUntil(sb, ftHttpFileInfo.getDataUrl().toString(), ftHttpFileInfo.getDataUntil());
        if (!TextUtils.isEmpty(ftHttpFileInfo.getBrandedUrl())) {
            appendBrandedUrl(sb, ftHttpFileInfo.getBrandedUrl());
        }
        sb.append("\t</file-info>\n");
        sb.append("</file>\n");
        return sb.toString();
    }

    private void appendXmlVersionAndEncoding(StringBuilder sb) {
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    }

    private void appendFtHttpNamespace(StringBuilder sb) {
        sb.append(" xmlns=\"urn:gsma:params:xml:ns:rcs:rcs:fthttp\"");
    }

    private void appendFtHttpExtNamespace(StringBuilder sb) {
        sb.append(" xmlns:e=\"urn:gsma:params:xml:ns:rcs:rcs:up:fthttpext\"");
    }

    private void appendAudioMessagingNamespace(StringBuilder sb) {
        sb.append(" xmlns:am=\"urn:gsma:params:xml:ns:rcs:rcs:rram\"");
    }

    private void appendThumbnail(StringBuilder sb, FtHttpFileInfo ftHttpFileInfo) {
        sb.append("\t<file-info type=\"thumbnail\">\n");
        sb.append("\t\t<file-size>");
        sb.append(ftHttpFileInfo.getThumbnailFileSize());
        sb.append("</file-size>\n");
        sb.append("\t\t<content-type>");
        sb.append(ftHttpFileInfo.getThumbnailContentType());
        sb.append("</content-type>\n");
        sb.append("\t\t<data url=\"");
        sb.append(ftHttpFileInfo.getThumbnailDataUrl().toString().replace("&", "&amp;"));
        sb.append("\" until=\"");
        sb.append(ftHttpFileInfo.getThumbnailDataUntil());
        sb.append("\"/>\n");
        sb.append("\t</file-info>\n");
    }

    private void appendFileSize(StringBuilder sb, long j) {
        sb.append("\t\t<file-size>");
        sb.append(j);
        sb.append("</file-size>\n");
    }

    private void appendFileName(StringBuilder sb, String str) {
        sb.append("\t\t<file-name>");
        sb.append(str);
        sb.append("</file-name>\n");
    }

    private void appendContentType(StringBuilder sb, String str) {
        sb.append("\t\t<content-type>");
        sb.append(str);
        sb.append("</content-type>\n");
    }

    private void appendPlayingLength(StringBuilder sb, int i) {
        sb.append("\t\t<am:playing-length>");
        sb.append(i);
        sb.append("</am:playing-length>\n");
    }

    private void appendDataUrlAndUntil(StringBuilder sb, String str, String str2) {
        sb.append("\t\t<data url=\"");
        sb.append(str.replace("&", "&amp;"));
        sb.append("\" until=\"");
        sb.append(str2);
        sb.append("\"/>\n");
    }

    private void appendBrandedUrl(StringBuilder sb, String str) {
        sb.append("\t\t<e:branded-url>");
        sb.append(str.replace("&", "&amp;"));
        sb.append("</e:branded-url>\n");
    }
}
