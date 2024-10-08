package com.sec.internal.ims.servicemodules.tapi.service.utils;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import com.sec.internal.constants.ims.SipMsg;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.cmstore.McsConstants;
import com.sec.internal.constants.ims.servicemodules.volte2.CallConstants;
import com.sec.internal.helper.httpclient.HttpPostBody;
import com.sec.internal.helper.translate.ContentTypeTranslator;

public class FileUtils {
    public static String getFilePathFromUri(Context context, Uri uri) {
        Uri uri2 = null;
        if (DocumentsContract.isDocumentUri(context, uri)) {
            if (isExternalStorageDocument(uri)) {
                String[] split = DocumentsContract.getDocumentId(uri).split(":");
                if (McsConstants.Auth.PRIMARY.equalsIgnoreCase(split[0])) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            } else if (isDownloadsDocument(uri)) {
                return getDataColumn(context, ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(DocumentsContract.getDocumentId(uri)).longValue()), (String) null, (String[]) null);
            } else if (isMediaDocument(uri)) {
                String[] split2 = DocumentsContract.getDocumentId(uri).split(":");
                String str = split2[0];
                if (CallConstants.ComposerData.IMAGE.equals(str)) {
                    uri2 = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if (SipMsg.FEATURE_TAG_MMTEL_VIDEO.equals(str)) {
                    uri2 = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(str)) {
                    uri2 = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                return getDataColumn(context, uri2, "_id=?", new String[]{split2[1]});
            }
        }
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, (String) null, (String[]) null);
        }
        if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    private static String getDataColumn(Context context, Uri uri, String str, String[] strArr) {
        Cursor query = context.getContentResolver().query(uri, new String[]{CloudMessageProviderContract.BufferDBMMSpart._DATA}, str, strArr, (String) null);
        if (query != null) {
            try {
                if (query.moveToFirst()) {
                    String string = query.getString(query.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBMMSpart._DATA));
                    query.close();
                    return string;
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (query == null) {
            return null;
        }
        query.close();
        return null;
        throw th;
    }

    public static String getContentTypeFromFileName(String str) {
        if (str == null || str.isEmpty()) {
            return null;
        }
        String substring = str.substring(str.lastIndexOf(".") + 1);
        return ContentTypeTranslator.isTranslationDefined(substring) ? ContentTypeTranslator.translate(substring) : HttpPostBody.CONTENT_TYPE_DEFAULT;
    }

    public static String getFileNameFromPath(String str) {
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        return str.substring(str.lastIndexOf("/") + 1);
    }
}
