package com.sec.internal.ims.translate;

import com.sec.ims.util.ImsUri;
import com.sec.internal.helper.translate.TranslationException;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.EucMessage_.EucContent;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.EucMessage_.RequestMessage;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.EucMessage_.TextLangPair;
import com.sec.internal.ims.servicemodules.euc.data.resip.IEucRequest;
import com.sec.internal.ims.util.ImsUtil;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

class EucMessageTranslator {
    EucMessageTranslator() {
    }

    /* access modifiers changed from: protected */
    public IEucRequest translate(RequestMessage requestMessage, Long l, IEucRequest.EucRequestType eucRequestType) throws TranslationException {
        if (requestMessage == null || requestMessage.base() == null || requestMessage.content() == null) {
            throw new TranslationException("EucMessageTranslator, incomplete or null message data!");
        }
        final EucMessageDataCollector eucMessageDataCollector = new EucMessageDataCollector();
        final ImsUri parse = ImsUri.parse(requestMessage.base().remoteUri());
        final String ownIdentity = EucTranslatorUtil.getOwnIdentity(ImsUtil.getHandle(requestMessage.base().handle()));
        EucContent content = requestMessage.content();
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
        for (int i3 = 0; i3 < requestMessage.acceptButtonsLength(); i3++) {
            TextLangPair acceptButtons = requestMessage.acceptButtons(i3);
            if (acceptButtons != null) {
                EucTranslatorUtil.checkTextLangPair(acceptButtons.text(), acceptButtons.lang(), requestMessage.acceptButtonsLength() == 1);
                eucMessageDataCollector.addAcceptButton(acceptButtons.lang(), acceptButtons.text());
            }
        }
        for (int i4 = 0; i4 < requestMessage.rejectButtonsLength(); i4++) {
            TextLangPair rejectButtons = requestMessage.rejectButtons(i4);
            if (rejectButtons != null) {
                EucTranslatorUtil.checkTextLangPair(rejectButtons.text(), rejectButtons.lang(), requestMessage.rejectButtonsLength() == 1);
                eucMessageDataCollector.addRejectButton(rejectButtons.lang(), rejectButtons.text());
            }
        }
        eucMessageDataCollector.prepareMessageData();
        if (!eucMessageDataCollector.getAllElements().isEmpty()) {
            final RequestMessage requestMessage2 = requestMessage;
            final Long l2 = l;
            final IEucRequest.EucRequestType eucRequestType2 = eucRequestType;
            return new IEucRequest() {
                public Map<String, IEucRequest.IEucMessageData> getLanguageMapping() {
                    return eucMessageDataCollector.getAllElements();
                }

                public IEucRequest.IEucMessageData getDefaultData() {
                    return eucMessageDataCollector.getDefaultElement();
                }

                public String getEucId() {
                    if (requestMessage2.base() != null) {
                        return requestMessage2.base().id();
                    }
                    return null;
                }

                public boolean isPinRequested() {
                    return requestMessage2.pin();
                }

                public boolean isExternal() {
                    return requestMessage2.externalEucr();
                }

                public Long getTimeOut() {
                    return l2;
                }

                public ImsUri getFromHeader() {
                    return parse;
                }

                public String getOwnIdentity() {
                    return ownIdentity;
                }

                public long getTimestamp() {
                    if (requestMessage2.base() != null) {
                        return requestMessage2.base().timestamp();
                    }
                    return 0;
                }

                public IEucRequest.EucRequestType getType() {
                    return eucRequestType2;
                }
            };
        }
        throw new TranslationException("EucMessageTranslator, failed to create EucMessageData objects, missing required fields in received EUC message!");
    }

    private class EucMessageDataCollector {
        private Map<String, String> mAcceptButtons;
        private IEucRequest.IEucMessageData mDefault;
        private final Map<String, IEucRequest.IEucMessageData> mElements;
        private Set<String> mLanguages;
        private Map<String, String> mRejectButtons;
        private Map<String, String> mSubjects;
        private Map<String, String> mTexts;

        private EucMessageDataCollector() {
            this.mLanguages = new LinkedHashSet();
            this.mTexts = new LinkedHashMap();
            this.mSubjects = new LinkedHashMap();
            this.mAcceptButtons = new LinkedHashMap();
            this.mRejectButtons = new LinkedHashMap();
            this.mElements = new HashMap();
            this.mDefault = null;
        }

        /* access modifiers changed from: package-private */
        public Map<String, IEucRequest.IEucMessageData> getAllElements() {
            return this.mElements;
        }

        /* access modifiers changed from: package-private */
        public IEucRequest.IEucMessageData getDefaultElement() {
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
        public void addAcceptButton(String str, String str2) {
            this.mAcceptButtons.put(EucTranslatorUtil.addLanguage(str, this.mLanguages), str2);
        }

        /* access modifiers changed from: package-private */
        public void addRejectButton(String str, String str2) {
            this.mRejectButtons.put(EucTranslatorUtil.addLanguage(str, this.mLanguages), str2);
        }

        /* access modifiers changed from: package-private */
        public void prepareMessageData() {
            for (String next : this.mLanguages) {
                add(next, createEucMessageData(EucTranslatorUtil.getValue(next, this.mTexts), EucTranslatorUtil.getValue(next, this.mSubjects), EucTranslatorUtil.nullIfEmpty(this.mAcceptButtons.get(next)), EucTranslatorUtil.nullIfEmpty(this.mRejectButtons.get(next))));
            }
            releaseTemporaryData();
        }

        private void releaseTemporaryData() {
            this.mLanguages = null;
            this.mTexts = null;
            this.mSubjects = null;
            this.mAcceptButtons = null;
            this.mRejectButtons = null;
        }

        private void add(String str, IEucRequest.IEucMessageData iEucMessageData) {
            this.mElements.put(str, iEucMessageData);
            if (this.mDefault == null) {
                this.mDefault = iEucMessageData;
            }
        }

        private IEucRequest.IEucMessageData createEucMessageData(String str, String str2, String str3, String str4) {
            final String str5 = str;
            final String str6 = str2;
            final String str7 = str4;
            final String str8 = str3;
            return new IEucRequest.IEucMessageData() {
                public String getText() {
                    return str5;
                }

                public String getSubject() {
                    return str6;
                }

                public String getRejectButton() {
                    return str7;
                }

                public String getAcceptButton() {
                    return str8;
                }
            };
        }
    }
}
