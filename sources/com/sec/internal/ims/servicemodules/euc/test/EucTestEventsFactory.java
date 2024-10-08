package com.sec.internal.ims.servicemodules.euc.test;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.google.flatbuffers.FlatBufferBuilder;
import com.sec.ims.util.ImsUri;
import com.sec.internal.helper.Preconditions;
import com.sec.internal.helper.translate.TranslationException;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.EucMessage_.AckMessage;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.EucMessage_.BaseMessage;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.EucMessage_.EucContent;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.EucMessage_.NotificationMessage;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.EucMessage_.PersistentMessage;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.EucMessage_.RequestMessage;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.EucMessage_.SystemMessage;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.EucMessage_.TextLangPair;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.EucMessage_.VolatileMessage;
import com.sec.internal.ims.servicemodules.euc.data.AutoconfUserConsentData;
import com.sec.internal.ims.servicemodules.euc.data.EucMessageKey;
import com.sec.internal.ims.servicemodules.euc.data.EucState;
import com.sec.internal.ims.servicemodules.euc.data.EucType;
import com.sec.internal.ims.servicemodules.euc.data.IEucData;
import com.sec.internal.ims.servicemodules.euc.data.resip.IEucAcknowledgment;
import com.sec.internal.ims.servicemodules.euc.data.resip.IEucNotification;
import com.sec.internal.ims.servicemodules.euc.data.resip.IEucRequest;
import com.sec.internal.ims.servicemodules.euc.data.resip.IEucSystemRequest;
import com.sec.internal.ims.servicemodules.euc.test.EucTestIntent;
import com.sec.internal.ims.translate.AcknowledgementMessageTranslator;
import com.sec.internal.ims.translate.EucTranslatorUtil;
import com.sec.internal.ims.translate.NotificationMessageTranslator;
import com.sec.internal.ims.translate.PersistentMessageTranslator;
import com.sec.internal.ims.translate.SystemRequestMessageTranslator;
import com.sec.internal.ims.translate.TypeTranslator;
import com.sec.internal.ims.translate.VolatileMessageTranslator;
import com.sec.internal.interfaces.ims.servicemodules.euc.IEucFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class EucTestEventsFactory implements IEucTestEventsFactory {
    private static final String LOG_MSG_INVALID_INTENT = "Invalid intent, ignoring! ";
    private static final String LOG_MSG_NO_EXTRAS = "Missing extras in the intent!";
    private static final String LOG_TAG = "EucTestEventsFactory";
    private final AcknowledgementMessageTranslator mAcknowledgementMessageTranslator = new AcknowledgementMessageTranslator();
    private final IEucFactory mEucFactory;
    private final NotificationMessageTranslator mNotificationMessageTranslator = new NotificationMessageTranslator();
    private final PersistentMessageTranslator mPersistentMessageTranslator = new PersistentMessageTranslator();
    private final SystemRequestMessageTranslator mSystemRequestMessageTranslator = new SystemRequestMessageTranslator();
    private final VolatileMessageTranslator mVolatileMessageTranslator = new VolatileMessageTranslator();

    private String makeStrNotNull(String str) {
        return str != null ? str : "";
    }

    public EucTestEventsFactory(IEucFactory iEucFactory) {
        this.mEucFactory = (IEucFactory) Preconditions.checkNotNull(iEucFactory);
    }

    public IEucRequest createPersistent(Intent intent) {
        Log.d(LOG_TAG, "createPersistent");
        Bundle extras = intent.getExtras();
        if (extras == null || checkNoBaseExtras(extras)) {
            logNoExtras();
            return null;
        }
        FlatBufferBuilder flatBufferBuilder = new FlatBufferBuilder(0);
        flatBufferBuilder.finish(PersistentMessage.createPersistentMessage(flatBufferBuilder, buildRequestMessage(flatBufferBuilder, extras)));
        return (IEucRequest) translateMessageToRequest(PersistentMessage.getRootAsPersistentMessage(flatBufferBuilder.dataBuffer()), this.mPersistentMessageTranslator);
    }

    public IEucRequest createVolatile(Intent intent) {
        Log.d(LOG_TAG, "createVolatile");
        Bundle extras = intent.getExtras();
        if (extras == null) {
            logNoExtras();
            return null;
        }
        long j = extras.getLong(EucTestIntent.Extras.TIMEOUT);
        FlatBufferBuilder flatBufferBuilder = new FlatBufferBuilder(0);
        flatBufferBuilder.finish(VolatileMessage.createVolatileMessage(flatBufferBuilder, buildRequestMessage(flatBufferBuilder, extras), j));
        return (IEucRequest) translateMessageToRequest(VolatileMessage.getRootAsVolatileMessage(flatBufferBuilder.dataBuffer()), this.mVolatileMessageTranslator);
    }

    public IEucAcknowledgment createAcknowledgement(Intent intent) {
        String str = LOG_TAG;
        Log.d(str, "createAcknowledgement");
        Bundle extras = intent.getExtras();
        if (extras == null || checkNoBaseExtras(extras)) {
            logNoExtras();
            return null;
        }
        String string = extras.getString(EucTestIntent.Extras.ACK_STATUS);
        int i = 0;
        FlatBufferBuilder flatBufferBuilder = new FlatBufferBuilder(0);
        int buildBaseMessage = buildBaseMessage(flatBufferBuilder, extras);
        int buildEucContent = buildEucContent(flatBufferBuilder, extras);
        if (EucTestIntent.Extras.ACK_STATUS_OK.equals(string)) {
            Log.d(str, "createAcknowledgement, status ok");
        } else {
            if ("error".equals(string)) {
                Log.d(str, "createAcknowledgement, status error");
            } else {
                Log.d(str, "createAcknowledgement, unrecognized status, assuming error!");
            }
            i = 1;
        }
        flatBufferBuilder.finish(AckMessage.createAckMessage(flatBufferBuilder, buildBaseMessage, buildEucContent, i));
        return (IEucAcknowledgment) translateMessageToRequest(AckMessage.getRootAsAckMessage(flatBufferBuilder.dataBuffer()), this.mAcknowledgementMessageTranslator);
    }

    public IEucNotification createNotification(Intent intent) {
        String str = LOG_TAG;
        Log.d(str, "createNotification");
        Bundle extras = intent.getExtras();
        if (extras == null || checkNoBaseExtras(extras)) {
            logNoExtras();
            return null;
        }
        List arrayList = new ArrayList();
        if (extras.containsKey(EucTestIntent.Extras.OK_BUTTON_LIST)) {
            arrayList = getArrayList(extras, EucTestIntent.Extras.OK_BUTTON_LIST);
        }
        List arrayList2 = new ArrayList();
        if (extras.containsKey(EucTestIntent.Extras.OK_BUTTON_LANG_LIST)) {
            arrayList2 = getArrayList(extras, EucTestIntent.Extras.OK_BUTTON_LANG_LIST);
        }
        FlatBufferBuilder flatBufferBuilder = new FlatBufferBuilder(0);
        int buildBaseMessage = buildBaseMessage(flatBufferBuilder, extras);
        int buildEucContent = buildEucContent(flatBufferBuilder, extras);
        int[] iArr = new int[0];
        if (!arrayList.isEmpty()) {
            Log.d(str, "createNotification, okButtons");
            iArr = buildTextLangPairList(flatBufferBuilder, arrayList, arrayList2);
        }
        flatBufferBuilder.finish(NotificationMessage.createNotificationMessage(flatBufferBuilder, buildBaseMessage, buildEucContent, NotificationMessage.createOkButtonsVector(flatBufferBuilder, iArr)));
        return (IEucNotification) translateMessageToRequest(NotificationMessage.getRootAsNotificationMessage(flatBufferBuilder.dataBuffer()), this.mNotificationMessageTranslator);
    }

    public IEucSystemRequest createSystemRequest(Intent intent) {
        Log.d(LOG_TAG, "createSystemRequest");
        Bundle extras = intent.getExtras();
        if (extras == null || checkNoBaseExtras(extras) || checkNoSystemRequestExtras(extras)) {
            logNoExtras();
            return null;
        }
        String string = extras.getString(EucTestIntent.Extras.SYSTEM_TYPE);
        String string2 = extras.getString(EucTestIntent.Extras.SYSTEM_DATA);
        FlatBufferBuilder flatBufferBuilder = new FlatBufferBuilder(0);
        flatBufferBuilder.finish(SystemMessage.createSystemMessage(flatBufferBuilder, buildBaseMessage(flatBufferBuilder, extras), flatBufferBuilder.createString((CharSequence) makeStrNotNull(string)), flatBufferBuilder.createString((CharSequence) makeStrNotNull(string2))));
        return (IEucSystemRequest) translateMessageToRequest(SystemMessage.getRootAsSystemMessage(flatBufferBuilder.dataBuffer()), this.mSystemRequestMessageTranslator);
    }

    public AutoconfUserConsentData createUserConsent(Intent intent) {
        Log.d(LOG_TAG, "createUserConsent");
        Bundle extras = intent.getExtras();
        if (extras == null || checkNoUserConsentExtras(extras)) {
            logNoExtras();
            return null;
        }
        String string = extras.getString(EucTestIntent.Extras.TITLE);
        String string2 = extras.getString("message");
        return new AutoconfUserConsentData(getTimestamp(extras), extras.getBoolean(EucTestIntent.Extras.USER_ACCEPT), string, string2, extras.getString(EucTestIntent.Extras.SUBSCRIBER_IDENTITY));
    }

    private int buildBaseMessage(FlatBufferBuilder flatBufferBuilder, Bundle bundle) {
        return BaseMessage.createBaseMessage(flatBufferBuilder, (long) bundle.getInt(EucTestIntent.Extras.HANDLE), flatBufferBuilder.createString((CharSequence) bundle.getString("id")), flatBufferBuilder.createString((CharSequence) bundle.getString("remote_uri")), getTimestamp(bundle));
    }

    private int buildEucContent(FlatBufferBuilder flatBufferBuilder, Bundle bundle) {
        List<String> arrayList = getArrayList(bundle, EucTestIntent.Extras.TEXT_LIST);
        List arrayList2 = new ArrayList();
        if (bundle.containsKey(EucTestIntent.Extras.TEXT_LANG_LIST)) {
            arrayList2 = getArrayList(bundle, EucTestIntent.Extras.TEXT_LANG_LIST);
        }
        List<String> arrayList3 = getArrayList(bundle, EucTestIntent.Extras.SUBJECT_LIST);
        List arrayList4 = new ArrayList();
        if (bundle.containsKey(EucTestIntent.Extras.SUBJECT_LANG_LIST)) {
            arrayList4 = getArrayList(bundle, EucTestIntent.Extras.SUBJECT_LANG_LIST);
        }
        String str = LOG_TAG;
        Log.d(str, "buildEucContent, texts");
        int[] buildTextLangPairList = buildTextLangPairList(flatBufferBuilder, arrayList, arrayList2);
        Log.d(str, "buildEucContent, subjects");
        return EucContent.createEucContent(flatBufferBuilder, EucContent.createTextsVector(flatBufferBuilder, buildTextLangPairList), EucContent.createSubjectsVector(flatBufferBuilder, buildTextLangPairList(flatBufferBuilder, arrayList3, arrayList4)));
    }

    private int[] buildTextLangPairList(FlatBufferBuilder flatBufferBuilder, List<String> list, List<String> list2) {
        boolean z = true;
        if (list2.isEmpty()) {
            if (list.size() != 1) {
                z = false;
            }
            Preconditions.checkArgument(z, "If more than one element is presented a language (lang) attribute must be present with the two letter language codes according to the ISO 639-1");
            int createString = flatBufferBuilder.createString((CharSequence) makeStrNotNull(list.get(0)));
            TextLangPair.startTextLangPair(flatBufferBuilder);
            TextLangPair.addText(flatBufferBuilder, createString);
            return new int[]{TextLangPair.endTextLangPair(flatBufferBuilder)};
        }
        if (list.size() != list2.size()) {
            z = false;
        }
        Preconditions.checkArgument(z, "Text and language size does not match");
        int[] iArr = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            iArr[i] = TextLangPair.createTextLangPair(flatBufferBuilder, flatBufferBuilder.createString((CharSequence) makeStrNotNull(list.get(i))), flatBufferBuilder.createString((CharSequence) makeStrNotNull(list2.get(i))));
        }
        return iArr;
    }

    private int buildRequestMessage(FlatBufferBuilder flatBufferBuilder, Bundle bundle) {
        String str = LOG_TAG;
        Log.d(str, "buildRequestMessage");
        boolean z = bundle.getBoolean(EucTestIntent.Extras.PIN_INDICATION);
        boolean z2 = bundle.getBoolean(EucTestIntent.Extras.EXTERNAL_EUCR);
        List arrayList = new ArrayList();
        if (bundle.containsKey(EucTestIntent.Extras.ACCEPT_BUTTON_LIST)) {
            arrayList = getArrayList(bundle, EucTestIntent.Extras.ACCEPT_BUTTON_LIST);
        }
        List arrayList2 = new ArrayList();
        if (bundle.containsKey(EucTestIntent.Extras.ACCEPT_BUTTON_LANG_LIST)) {
            arrayList2 = getArrayList(bundle, EucTestIntent.Extras.ACCEPT_BUTTON_LANG_LIST);
        }
        List arrayList3 = new ArrayList();
        if (bundle.containsKey(EucTestIntent.Extras.REJECT_BUTTON_LIST)) {
            arrayList3 = getArrayList(bundle, EucTestIntent.Extras.REJECT_BUTTON_LIST);
        }
        List arrayList4 = new ArrayList();
        if (bundle.containsKey(EucTestIntent.Extras.REJECT_BUTTON_LANG_LIST)) {
            arrayList4 = getArrayList(bundle, EucTestIntent.Extras.REJECT_BUTTON_LANG_LIST);
        }
        int buildBaseMessage = buildBaseMessage(flatBufferBuilder, bundle);
        int buildEucContent = buildEucContent(flatBufferBuilder, bundle);
        int[] iArr = new int[0];
        if (!arrayList.isEmpty()) {
            Log.d(str, "buildRequestMessage, acceptButtons");
            iArr = buildTextLangPairList(flatBufferBuilder, arrayList, arrayList2);
        }
        int createAcceptButtonsVector = RequestMessage.createAcceptButtonsVector(flatBufferBuilder, iArr);
        int[] iArr2 = new int[0];
        if (!arrayList3.isEmpty()) {
            Log.d(str, "buildRequestMessage, rejectButtons");
            iArr2 = buildTextLangPairList(flatBufferBuilder, arrayList3, arrayList4);
        }
        return RequestMessage.createRequestMessage(flatBufferBuilder, buildBaseMessage, buildEucContent, createAcceptButtonsVector, RequestMessage.createRejectButtonsVector(flatBufferBuilder, iArr2), z, z2);
    }

    public IEucData createEucData(Intent intent) {
        Log.d(LOG_TAG, "createEucData");
        Bundle extras = intent.getExtras();
        if (extras == null || checkNoEucDataExtras(extras)) {
            logNoExtras();
            return null;
        }
        try {
            String ownIdentity = EucTranslatorUtil.getOwnIdentity(extras.getInt(EucTestIntent.Extras.HANDLE));
            ImsUri parse = ImsUri.parse(extras.getString("remote_uri"));
            if (parse == null) {
                return null;
            }
            String string = extras.getString("id");
            long j = extras.getLong(EucTestIntent.Extras.TIMEOUT);
            EucMessageKey eucMessageKey = new EucMessageKey(string, ownIdentity, j == 0 ? EucType.PERSISTENT : EucType.VOLATILE, parse);
            String string2 = extras.getString(EucTestIntent.Extras.USER_PIN);
            return this.mEucFactory.createEucData(eucMessageKey, string2 != null, string2, extras.getBoolean(EucTestIntent.Extras.EXTERNAL_EUCR), extras.getBoolean(EucTestIntent.Extras.USER_ACCEPT) ? EucState.ACCEPTED_NOT_SENT : EucState.REJECTED_NOT_SENT, getTimestamp(extras), Long.valueOf(j));
        } catch (TranslationException unused) {
            return null;
        }
    }

    private long getTimestamp(Bundle bundle) {
        long j = bundle.getLong("timestamp");
        return j == 0 ? System.currentTimeMillis() : j;
    }

    private List<String> getArrayList(Bundle bundle, String str) {
        String[] stringArray;
        Preconditions.checkNotNull(bundle, "extras is null");
        Preconditions.checkNotNull(str, "key is null");
        List<String> stringArrayList = bundle.getStringArrayList(str);
        if (stringArrayList == null && (stringArray = bundle.getStringArray(str)) != null) {
            stringArrayList = Arrays.asList(stringArray);
        }
        return stringArrayList != null ? stringArrayList : Collections.emptyList();
    }

    private <T, S> S translateMessageToRequest(T t, TypeTranslator<T, S> typeTranslator) {
        try {
            return typeTranslator.translate(t);
        } catch (TranslationException e) {
            logInvalidIntent(e);
            return null;
        }
    }

    private void logInvalidIntent(TranslationException translationException) {
        String str = LOG_TAG;
        Log.e(str, LOG_MSG_INVALID_INTENT + translationException.getMessage());
    }

    private void logNoExtras() {
        Log.e(LOG_TAG, "Invalid intent, ignoring! Missing extras in the intent!");
    }

    private boolean checkNoBaseExtras(Bundle bundle) {
        return !bundle.containsKey(EucTestIntent.Extras.HANDLE) || !bundle.containsKey("id") || !bundle.containsKey("remote_uri");
    }

    private boolean checkNoSystemRequestExtras(Bundle bundle) {
        return !bundle.containsKey(EucTestIntent.Extras.SYSTEM_TYPE) || !bundle.containsKey(EucTestIntent.Extras.SYSTEM_DATA);
    }

    private boolean checkNoUserConsentExtras(Bundle bundle) {
        return !bundle.containsKey(EucTestIntent.Extras.TITLE) || !bundle.containsKey("message") || !bundle.containsKey(EucTestIntent.Extras.SUBSCRIBER_IDENTITY);
    }

    private boolean checkNoEucDataExtras(Bundle bundle) {
        return !bundle.containsKey(EucTestIntent.Extras.HANDLE) || !bundle.containsKey("id") || !bundle.containsKey("remote_uri");
    }
}
