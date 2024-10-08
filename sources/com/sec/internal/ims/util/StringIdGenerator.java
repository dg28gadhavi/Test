package com.sec.internal.ims.util;

import android.text.TextUtils;
import com.sec.ims.util.ImsUri;
import com.sec.internal.helper.HashManager;
import com.sec.internal.helper.Preconditions;
import com.sec.internal.helper.StringGenerator;
import com.sec.internal.log.IMSLog;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

public class StringIdGenerator {
    private static final int FILETRANSFERID_MAX_LEN = 32;
    private static final int FILETRANSFERID_MIN_LEN = 10;
    private static final String LOG_TAG = "StringIdGenerator";
    private static final int SUBSCRIPTIONID_MAX_LEN = 32;
    private static final int SUBSCRIPTIONID_MIN_LEN = 10;

    public static String generateUuid() {
        return UUID.randomUUID().toString();
    }

    public static String generateChatId(Set<ImsUri> set, String str, boolean z, int i) {
        Preconditions.checkNotNull(set, "Passed URI Set is null.");
        String str2 = LOG_TAG;
        IMSLog.s(str2, "generateChatId(Set<URI> participants = " + set.toString() + " ) withTimeStamp: " + z);
        StringBuilder sb = new StringBuilder();
        if (z) {
            sb.append(new Timestamp(new Date().getTime()).toString());
        }
        for (ImsUri imsUri : set) {
            sb.append(imsUri.toString());
        }
        if (!TextUtils.isEmpty(str)) {
            sb.append(str);
        }
        sb.append(i);
        try {
            String sb2 = sb.toString();
            String str3 = LOG_TAG;
            IMSLog.s(str3, "feeding data: " + sb2);
            return HashManager.generateHash(sb.toString());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new RuntimeException("NoSuchAlgorithmException caught when trying to generate chatId");
        }
    }

    public static String generateChatId(Set<ImsUri> set, boolean z, int i) {
        Preconditions.checkNotNull(set, "Passed URI Set is null.");
        String str = LOG_TAG;
        IMSLog.s(str, "generateChatId(Set<URI> participants = " + set.toString() + " ) withTimeStamp: " + z + "ChatMode: " + i);
        StringBuilder sb = new StringBuilder();
        if (z) {
            sb.append(new Timestamp(new Date().getTime()).toString());
        }
        for (ImsUri imsUri : set) {
            sb.append(imsUri.toString());
        }
        sb.append(i);
        try {
            String sb2 = sb.toString();
            String str2 = LOG_TAG;
            IMSLog.s(str2, "feeding data: " + sb2);
            return HashManager.generateHash(sb.toString());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new RuntimeException("NoSuchAlgorithmException caught when trying to generate chatId");
        }
    }

    public static String generateImdn() {
        return generateUuid();
    }

    public static String generateFileTransferId() {
        return StringGenerator.generateString(10, 32);
    }

    public static String generateSubscriptionId() {
        return StringGenerator.generateString(10, 32);
    }

    public static String generateContributionId() {
        return UUID.randomUUID().toString();
    }

    public static String generateConversationId() {
        return UUID.randomUUID().toString();
    }
}
