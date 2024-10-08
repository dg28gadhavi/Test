package com.sec.internal.ims.translate;

import com.sec.ims.util.ImsUri;
import com.sec.internal.helper.translate.TranslationException;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.EucMessage_.AckMessage;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.EucMessage_.EucContent;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.EucMessage_.TextLangPair;
import com.sec.internal.ims.servicemodules.euc.data.resip.IEucAcknowledgment;
import com.sec.internal.ims.util.ImsUtil;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class AcknowledgementMessageTranslator implements TypeTranslator<AckMessage, IEucAcknowledgment> {
    public IEucAcknowledgment translate(AckMessage ackMessage) throws TranslationException {
        if (ackMessage == null || ackMessage.base() == null || ackMessage.content() == null) {
            throw new TranslationException("AcknowledgementMessageTranslator, incomplete or null message data!");
        }
        final EucMessageDataCollector eucMessageDataCollector = new EucMessageDataCollector();
        final ImsUri parse = ImsUri.parse(ackMessage.base().remoteUri());
        final String ownIdentity = EucTranslatorUtil.getOwnIdentity(ImsUtil.getHandle(ackMessage.base().handle()));
        EucContent content = ackMessage.content();
        int i = 0;
        while (true) {
            boolean z = true;
            if (i >= content.textsLength()) {
                break;
            }
            TextLangPair texts = content.texts(i);
            if (texts != null) {
                String text = texts.text();
                String lang = texts.lang();
                if (content.textsLength() != 1) {
                    z = false;
                }
                EucTranslatorUtil.checkTextLangPair(text, lang, z);
                eucMessageDataCollector.addText(texts.lang(), texts.text());
            }
            i++;
        }
        for (int i2 = 0; i2 < content.subjectsLength(); i2++) {
            TextLangPair subjects = content.subjects(i2);
            if (subjects != null) {
                EucTranslatorUtil.checkTextLangPair(subjects.text(), subjects.lang(), content.subjectsLength() == 1);
                eucMessageDataCollector.addSubject(subjects.lang(), subjects.text());
            }
        }
        eucMessageDataCollector.prepareMessageData();
        if (!eucMessageDataCollector.getAllElements().isEmpty()) {
            final AckMessage ackMessage2 = ackMessage;
            return new IEucAcknowledgment() {
                public Map<String, IEucAcknowledgment.IEUCMessageData> getLanguageMapping() {
                    return eucMessageDataCollector.getAllElements();
                }

                public IEucAcknowledgment.IEUCMessageData getDefaultData() {
                    return eucMessageDataCollector.getDefaultElement();
                }

                public String getEucId() {
                    return ackMessage2.base().id();
                }

                public ImsUri getFromHeader() {
                    return parse;
                }

                public String getOwnIdentity() {
                    return ownIdentity;
                }

                public long getTimestamp() {
                    return ackMessage2.base().timestamp();
                }
            };
        }
        throw new TranslationException("AcknowledgementMessageTranslator, failed to create EucMessageData objects, missing required fields in received EUC message!");
    }

    private class EucMessageDataCollector {
        private IEucAcknowledgment.IEUCMessageData mDefault;
        private final Map<String, IEucAcknowledgment.IEUCMessageData> mElements;
        private Set<String> mLanguages;
        private Map<String, String> mSubjects;
        private Map<String, String> mTexts;

        private EucMessageDataCollector() {
            this.mLanguages = new LinkedHashSet();
            this.mTexts = new LinkedHashMap();
            this.mSubjects = new LinkedHashMap();
            this.mElements = new HashMap();
            this.mDefault = null;
        }

        /* access modifiers changed from: package-private */
        public Map<String, IEucAcknowledgment.IEUCMessageData> getAllElements() {
            return this.mElements;
        }

        /* access modifiers changed from: package-private */
        public IEucAcknowledgment.IEUCMessageData getDefaultElement() {
            return this.mDefault;
        }

        /* access modifiers changed from: package-private */
        public void addText(String str, String str2) {
            this.mTexts.put(EucTranslatorUtil.addLanguage(str, this.mLanguages), str2);
        }

        /* access modifiers changed from: package-private */
        public void addSubject(String str, String str2) {
            this.mSubjects.put(EucTranslatorUtil.addLanguage(str, this.mLanguages), str2);
        }

        /* access modifiers changed from: package-private */
        public void prepareMessageData() {
            for (String next : this.mLanguages) {
                add(next, createEucMessageData(EucTranslatorUtil.getValue(next, this.mTexts), EucTranslatorUtil.getValue(next, this.mSubjects)));
            }
            releaseTemporaryData();
        }

        private void releaseTemporaryData() {
            this.mLanguages = null;
            this.mTexts = null;
            this.mSubjects = null;
        }

        private void add(String str, IEucAcknowledgment.IEUCMessageData iEUCMessageData) {
            this.mElements.put(str, iEUCMessageData);
            if (this.mDefault == null) {
                this.mDefault = iEUCMessageData;
            }
        }

        private IEucAcknowledgment.IEUCMessageData createEucMessageData(final String str, final String str2) {
            return new IEucAcknowledgment.IEUCMessageData() {
                public String getText() {
                    return str;
                }

                public String getSubject() {
                    return str2;
                }
            };
        }
    }
}
