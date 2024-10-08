package com.sec.internal.ims.translate;

import com.sec.ims.util.ImsUri;
import com.sec.internal.helper.Preconditions;
import com.sec.internal.helper.translate.MapTranslator;
import com.sec.internal.helper.translate.TranslationException;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.EucMessage_.SystemMessage;
import com.sec.internal.ims.servicemodules.euc.data.resip.IEucSystemRequest;
import com.sec.internal.ims.util.ImsUtil;
import java.util.HashMap;
import java.util.Map;

public class SystemRequestMessageTranslator implements TypeTranslator<SystemMessage, IEucSystemRequest> {
    private final MapTranslator<String, IEucSystemRequest.EucSystemRequestType> mEUCSystemRequestTypeTranslator;

    public SystemRequestMessageTranslator() {
        HashMap hashMap = new HashMap();
        hashMap.put("urn:gsma:rcs:http-configuration:reconfigure", IEucSystemRequest.EucSystemRequestType.RECONFIGURE);
        this.mEUCSystemRequestTypeTranslator = new MapTranslator<>(hashMap);
    }

    public IEucSystemRequest translate(SystemMessage systemMessage) throws TranslationException {
        Preconditions.checkNotNull(systemMessage);
        Preconditions.checkNotNull(systemMessage.base());
        final IEucSystemRequest.IEUCMessageData data = getData(systemMessage);
        final IEucSystemRequest.IEUCMessageData dataAsOptional = getDataAsOptional(systemMessage);
        final ImsUri parse = ImsUri.parse(systemMessage.base().remoteUri());
        final String ownIdentity = EucTranslatorUtil.getOwnIdentity(ImsUtil.getHandle(systemMessage.base().handle()));
        final IEucSystemRequest.EucSystemRequestType translate = this.mEUCSystemRequestTypeTranslator.translate(systemMessage.type());
        final SystemMessage systemMessage2 = systemMessage;
        return new IEucSystemRequest() {
            public Map<String, IEucSystemRequest.IEUCMessageData> getLanguageMapping() {
                return new HashMap();
            }

            public IEucSystemRequest.IEUCMessageData getDefaultData() {
                return data;
            }

            public String getEucId() {
                Preconditions.checkNotNull(systemMessage2);
                Preconditions.checkNotNull(systemMessage2.base());
                return systemMessage2.base().id();
            }

            public ImsUri getFromHeader() {
                return parse;
            }

            public String getOwnIdentity() {
                return ownIdentity;
            }

            public long getTimestamp() {
                Preconditions.checkNotNull(systemMessage2);
                Preconditions.checkNotNull(systemMessage2.base());
                return systemMessage2.base().timestamp();
            }

            public IEucSystemRequest.EucSystemRequestType getType() {
                return translate;
            }

            public IEucSystemRequest.IEUCMessageData getMessageData() {
                return dataAsOptional;
            }
        };
    }

    private IEucSystemRequest.IEUCMessageData getDataAsOptional(SystemMessage systemMessage) {
        if (systemMessage.data() != null) {
            return getData(systemMessage);
        }
        return null;
    }

    private IEucSystemRequest.IEUCMessageData getData(final SystemMessage systemMessage) {
        return new IEucSystemRequest.IEUCMessageData() {
            public String getData() {
                return systemMessage.data();
            }
        };
    }
}
