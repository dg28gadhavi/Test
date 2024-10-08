package com.sec.internal.ims.servicemodules.im.data;

import com.sec.ims.util.ImsUri;

public class ImParticipantUri {
    private final ImsUri mImsUri;

    public ImParticipantUri(ImsUri imsUri) {
        this.mImsUri = imsUri;
    }

    public ImsUri getImsUri() {
        return this.mImsUri;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj != null && getClass() == obj.getClass()) {
            return getImsUri().equals(((ImParticipantUri) obj).getImsUri());
        }
        return false;
    }

    public int hashCode() {
        int hashCode;
        if (this.mImsUri.getUriType() == ImsUri.UriType.SIP_URI) {
            String user = this.mImsUri.getUser();
            String host = this.mImsUri.getHost();
            int port = this.mImsUri.getPort();
            StringBuilder sb = new StringBuilder();
            sb.append("sip:");
            String str = "";
            sb.append(user == null ? str : "@");
            sb.append(host);
            if (port != -1) {
                str = ":" + port;
            }
            sb.append(str);
            hashCode = sb.toString().hashCode();
        } else {
            hashCode = toString().hashCode();
        }
        return hashCode + 31;
    }

    public String toString() {
        return this.mImsUri.toString();
    }
}
