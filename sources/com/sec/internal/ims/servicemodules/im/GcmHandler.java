package com.sec.internal.ims.servicemodules.im;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.ChatData;
import com.sec.internal.constants.ims.servicemodules.im.ImParticipant;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImErrorReason;
import com.sec.internal.helper.FileUtils;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.servicemodules.im.listener.IChatEventListener;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GcmHandler {
    private static final String LOG_TAG = ImSessionProcessor.class.getSimpleName();
    private ImCache mCache;
    private ImModule mImModule;
    private ImSessionProcessor mImSessionProcessor;
    private ImTranslation mImTranslation;

    public GcmHandler(ImModule imModule, ImCache imCache, ImSessionProcessor imSessionProcessor, ImTranslation imTranslation) {
        this.mImModule = imModule;
        this.mCache = imCache;
        this.mImSessionProcessor = imSessionProcessor;
        this.mImTranslation = imTranslation;
    }

    /* access modifiers changed from: protected */
    public void addParticipants(String str, List<ImsUri> list) {
        String str2 = LOG_TAG;
        Log.i(str2, "AddParticipants: chatId=" + str + " participants=" + IMSLog.numberChecker((Collection<ImsUri>) list));
        ImSession imSession = this.mCache.getImSession(str);
        if (imSession == null) {
            for (IChatEventListener onAddParticipantsFailed : this.mImSessionProcessor.mChatEventListeners) {
                onAddParticipantsFailed.onAddParticipantsFailed(str, list, ImErrorReason.ILLEGAL_SESSION_STATE);
            }
            return;
        }
        int phoneIdByIMSI = this.mImModule.getPhoneIdByIMSI(imSession.getOwnImsi());
        if (!this.mImModule.isRegistered(phoneIdByIMSI)) {
            for (IChatEventListener onAddParticipantsFailed2 : this.mImSessionProcessor.mChatEventListeners) {
                onAddParticipantsFailed2.onAddParticipantsFailed(str, list, ImErrorReason.ILLEGAL_SESSION_STATE);
            }
            return;
        }
        ImsUtil.listToDumpFormat(LogClass.IM_ADD_PARTICIPANT, phoneIdByIMSI, str);
        ArrayList<ImsUri> arrayList = new ArrayList<>(list);
        arrayList.removeAll(this.mImModule.getOwnUris(SimUtil.getSimSlotPriority()));
        if (arrayList.isEmpty()) {
            Log.e(str2, "addParticipants: requested for only own uri. Invalid.");
            for (IChatEventListener onAddParticipantsFailed3 : this.mImSessionProcessor.mChatEventListeners) {
                onAddParticipantsFailed3.onAddParticipantsFailed(str, list, ImErrorReason.INVALID);
            }
        } else if (imSession.getChatType() == ChatData.ChatType.ONE_TO_MANY_CHAT || imSession.getParticipantsSize() == 0) {
            ArrayList arrayList2 = new ArrayList();
            for (ImsUri normalizeUri : arrayList) {
                ImsUri normalizeUri2 = this.mImModule.normalizeUri(normalizeUri);
                if (normalizeUri2 != null && imSession.getParticipant(normalizeUri2) == null) {
                    arrayList2.add(new ImParticipant(str, ImParticipant.Status.INVITED, ImParticipant.Type.REGULAR, normalizeUri2, ""));
                }
            }
            if (!arrayList2.isEmpty()) {
                this.mImSessionProcessor.onParticipantsInserted(imSession, arrayList2);
            }
            this.mImSessionProcessor.onAddParticipantsSucceeded(str, arrayList);
        } else {
            imSession.addParticipants(arrayList);
        }
    }

    /* access modifiers changed from: protected */
    public void changeGroupAlias(String str, String str2) {
        String str3 = LOG_TAG;
        Log.i(str3, "changeGroupAlias: chatId=" + str + " alias=" + IMSLog.checker(str2));
        ImSession imSession = this.mCache.getImSession(str);
        if (imSession == null) {
            this.mImTranslation.onChangeGroupAliasFailed(str, str2, ImErrorReason.NO_SESSION);
            return;
        }
        if (!this.mImModule.isRegistered(this.mImModule.getPhoneIdByIMSI(imSession.getOwnImsi()))) {
            this.mImTranslation.onChangeGroupAliasFailed(str, str2, ImErrorReason.ILLEGAL_SESSION_STATE);
        } else {
            imSession.changeGroupAlias(str2);
        }
    }

    /* access modifiers changed from: protected */
    public void changeGroupChatIcon(Context context, String str, String str2, Uri uri) {
        String str3 = LOG_TAG;
        Log.i(str3, "changeGroupChatIcon: chatId=" + str + " iconUri=" + uri);
        ImSession imSession = this.mCache.getImSession(str);
        if (imSession == null) {
            this.mImSessionProcessor.onChangeGroupChatIconFailed(str, str2, ImErrorReason.NO_SESSION);
            return;
        }
        if (!this.mImModule.isRegistered(this.mImModule.getPhoneIdByIMSI(imSession.getOwnImsi()))) {
            this.mImSessionProcessor.onChangeGroupChatIconFailed(str, str2, ImErrorReason.ILLEGAL_SESSION_STATE);
        } else if (uri == null) {
            Log.e(str3, "Delete icon");
            imSession.changeGroupChatIcon((String) null);
        } else {
            String copyFileToCacheFromUri = FileUtils.copyFileToCacheFromUri(context, str2, uri);
            if (TextUtils.isEmpty(copyFileToCacheFromUri)) {
                Log.e(str3, "icon file doesn't exist");
                this.mImSessionProcessor.onChangeGroupChatIconFailed(str, str2, ImErrorReason.INVALID_ICON_PATH);
                return;
            }
            imSession.changeGroupChatIcon(copyFileToCacheFromUri);
        }
    }

    /* access modifiers changed from: protected */
    public void changeGroupChatLeader(String str, List<ImsUri> list) {
        String str2 = LOG_TAG;
        Log.i(str2, "changeGroupChatLeader: chatId=" + str + " participants=" + IMSLog.numberChecker((Collection<ImsUri>) list));
        ImSession imSession = this.mCache.getImSession(str);
        if (imSession == null) {
            for (IChatEventListener onChangeGroupChatLeaderFailed : this.mImSessionProcessor.mChatEventListeners) {
                onChangeGroupChatLeaderFailed.onChangeGroupChatLeaderFailed(str, list, ImErrorReason.NO_SESSION);
            }
            return;
        }
        if (!this.mImModule.isRegistered(this.mImModule.getPhoneIdByIMSI(imSession.getOwnImsi()))) {
            for (IChatEventListener onChangeGroupChatLeaderFailed2 : this.mImSessionProcessor.mChatEventListeners) {
                onChangeGroupChatLeaderFailed2.onChangeGroupChatLeaderFailed(str, list, ImErrorReason.ILLEGAL_SESSION_STATE);
            }
            return;
        }
        imSession.changeGroupChatLeader(list);
    }

    /* access modifiers changed from: protected */
    public void changeGroupChatSubject(String str, String str2) {
        String str3 = LOG_TAG;
        Log.i(str3, "changeGroupChatSubject: chatId=" + str + " subject=" + IMSLog.checker(str2));
        ImSession imSession = this.mCache.getImSession(str);
        if (imSession == null) {
            this.mImTranslation.onChangeGroupChatSubjectFailed(str, str2, ImErrorReason.NO_SESSION);
        } else if (!SimUtil.getSimMno(this.mImModule.getPhoneIdByIMSI(imSession.getChatData().getOwnIMSI())).isEur() || ImsProfile.isRcsUp2Profile(this.mImModule.getRcsProfile())) {
            if (str2 == null) {
                str2 = "";
            }
            imSession.changeGroupChatSubject(str2);
        } else {
            this.mImTranslation.onChangeGroupChatSubjectFailed(str, str2, ImErrorReason.INVALID);
        }
    }

    /* access modifiers changed from: protected */
    public void removeParticipants(String str, List<ImsUri> list) {
        ImParticipant participant;
        String str2 = LOG_TAG;
        Log.i(str2, "removeParticipants: chatId=" + str + " participants=" + IMSLog.numberChecker((Collection<ImsUri>) list));
        ImSession imSession = this.mCache.getImSession(str);
        if (imSession == null) {
            for (IChatEventListener onRemoveParticipantsFailed : this.mImSessionProcessor.mChatEventListeners) {
                onRemoveParticipantsFailed.onRemoveParticipantsFailed(str, list, ImErrorReason.NO_SESSION);
            }
            return;
        }
        int phoneIdByIMSI = this.mImModule.getPhoneIdByIMSI(imSession.getOwnImsi());
        ImsUtil.listToDumpFormat(LogClass.IM_REMOVE_PARTICIPANT, phoneIdByIMSI, str);
        if (!this.mImModule.isRegistered(phoneIdByIMSI)) {
            for (IChatEventListener onRemoveParticipantsFailed2 : this.mImSessionProcessor.mChatEventListeners) {
                onRemoveParticipantsFailed2.onRemoveParticipantsFailed(str, list, ImErrorReason.ILLEGAL_SESSION_STATE);
            }
        } else if (imSession.getChatType() == ChatData.ChatType.ONE_TO_MANY_CHAT) {
            ArrayList arrayList = new ArrayList();
            for (ImsUri normalizeUri : list) {
                ImsUri normalizeUri2 = this.mImModule.normalizeUri(normalizeUri);
                if (!(normalizeUri2 == null || (participant = imSession.getParticipant(normalizeUri2)) == null)) {
                    participant.setStatus(ImParticipant.Status.DECLINED);
                    arrayList.add(participant);
                }
            }
            if (!arrayList.isEmpty()) {
                this.mImSessionProcessor.onParticipantsDeleted(imSession, arrayList);
            }
            this.mImSessionProcessor.onRemoveParticipantsSucceeded(str, list);
        } else {
            for (ImsUri participant2 : list) {
                ImParticipant participant3 = imSession.getParticipant(participant2);
                if (participant3 == null || !(participant3.getStatus() == ImParticipant.Status.ACCEPTED || participant3.getStatus() == ImParticipant.Status.PENDING)) {
                    for (IChatEventListener onRemoveParticipantsFailed3 : this.mImSessionProcessor.mChatEventListeners) {
                        onRemoveParticipantsFailed3.onRemoveParticipantsFailed(str, list, ImErrorReason.PARTICIPANT_ALREADY_LEFT);
                    }
                    return;
                }
            }
            imSession.removeParticipants(list);
        }
    }

    /* access modifiers changed from: protected */
    public void updateParticipants(ImSession imSession, Set<ImsUri> set) {
        if (imSession != null && imSession.getChatStateId() == ChatData.State.NONE.getId()) {
            HashSet<ImsUri> hashSet = new HashSet<>(set);
            hashSet.removeAll(imSession.getParticipantsUri());
            ArrayList arrayList = new ArrayList();
            for (ImsUri imParticipant : hashSet) {
                arrayList.add(new ImParticipant(imSession.getChatId(), imParticipant));
            }
            if (!arrayList.isEmpty()) {
                this.mCache.addParticipant(arrayList);
                imSession.addParticipant(arrayList);
            }
            HashSet<ImsUri> hashSet2 = new HashSet<>(imSession.getParticipantsUri());
            hashSet2.removeAll(set);
            ArrayList arrayList2 = new ArrayList();
            for (ImsUri participant : hashSet2) {
                ImParticipant participant2 = imSession.getParticipant(participant);
                if (participant2 != null) {
                    participant2.setStatus(ImParticipant.Status.DECLINED);
                    arrayList2.add(participant2);
                }
            }
            String str = LOG_TAG;
            Log.i(str, "added participants : " + IMSLog.numberChecker((Collection<ImsUri>) hashSet) + ", removed participants : " + IMSLog.numberChecker((Collection<ImsUri>) hashSet2));
            if (!arrayList2.isEmpty()) {
                this.mCache.deleteParticipant(arrayList2);
                imSession.deleteParticipant(arrayList2);
            }
        }
    }
}
