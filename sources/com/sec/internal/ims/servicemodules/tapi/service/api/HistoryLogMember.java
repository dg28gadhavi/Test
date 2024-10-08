package com.sec.internal.ims.servicemodules.tapi.service.api;

import android.net.Uri;
import java.util.Map;

public class HistoryLogMember {
    private Map<String, String> mColumnMapping;
    private int mProviderId;
    private String mTable;
    private String mUri;

    public HistoryLogMember(int i, String str, String str2, Map<String, String> map) {
        this.mProviderId = i;
        this.mUri = str;
        this.mTable = str2;
        this.mColumnMapping = map;
    }

    public int getProviderId() {
        return this.mProviderId;
    }

    public Uri getUri() {
        if (!this.mUri.contains(this.mTable)) {
            this.mUri += "/" + this.mTable;
        }
        return Uri.parse(this.mUri);
    }

    public Map<String, String> getColumnMapping() {
        return this.mColumnMapping;
    }
}
