package com.sec.internal.ims.cmstore.omanetapi.bufferdbtranslation;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.ims.cmstore.MessageStoreClient;

public class BufferQueryDBTranslation {
    protected static final Uri CONTENT_URI_BUFFERDB = Uri.parse("content://com.samsung.rcs.cmstore");
    public static final String PROVIDER_NAME_BUFFERDB = "com.samsung.rcs.cmstore";
    private String SLOT_ID;
    protected Context mContext;
    protected int mPhoneId = 0;
    protected final ContentResolver mResolver;
    protected MessageStoreClient mStoreClient;

    public enum MessageType {
        MESSAGE_CHAT,
        MESSAGE_SLM,
        MESSAGE_FT
    }

    public BufferQueryDBTranslation(MessageStoreClient messageStoreClient) {
        String str = "";
        this.SLOT_ID = str;
        this.mStoreClient = messageStoreClient;
        Context context = messageStoreClient.getContext();
        this.mContext = context;
        this.mResolver = context.getContentResolver();
        this.SLOT_ID = messageStoreClient.getClientID() != 0 ? "slot2/" : str;
        this.mPhoneId = this.mStoreClient.getClientID();
    }

    /* access modifiers changed from: protected */
    public Cursor queryVvmGreetingBufferDB(long j) {
        return this.mResolver.query(Uri.parse(CONTENT_URI_BUFFERDB + "/" + CloudMessageProviderContract.CONTENTPRDR_VVMGREETING + "/" + this.SLOT_ID + j), (String[]) null, (String) null, (String[]) null, (String) null);
    }

    /* access modifiers changed from: protected */
    public Cursor queryRCSParticipantDB(String str) {
        return this.mResolver.query(Uri.parse(CONTENT_URI_BUFFERDB + "/" + CloudMessageProviderContract.CONTENTPRDR_RCSPARTICIPANTS + "/" + this.SLOT_ID + str), (String[]) null, (String) null, (String[]) null, (String) null);
    }

    /* access modifiers changed from: protected */
    public Cursor queryRCSSessionDB(String str) {
        return this.mResolver.query(Uri.parse(CONTENT_URI_BUFFERDB + "/" + CloudMessageProviderContract.CONTENTPRDR_RCSSESSION + "/" + this.SLOT_ID + str), (String[]) null, "chat_id= ?", (String[]) null, (String) null);
    }

    /* access modifiers changed from: protected */
    public Cursor querySMSBufferDB(long j) {
        return this.mResolver.query(Uri.parse(CONTENT_URI_BUFFERDB + "/" + CloudMessageProviderContract.CONTENTPRDR_SMSMESSAGES + "/" + this.SLOT_ID + j), (String[]) null, (String) null, (String[]) null, (String) null);
    }

    /* access modifiers changed from: protected */
    public Cursor queryGroupSMS(long j) {
        return this.mResolver.query(Uri.parse(CONTENT_URI_BUFFERDB + "/" + CloudMessageProviderContract.CONTENTPRDR_ALL_SMSMESSAGES + "/" + this.SLOT_ID), (String[]) null, "group_id= ?", new String[]{String.valueOf(j)}, (String) null);
    }

    /* access modifiers changed from: protected */
    public Cursor querySummaryDB(long j) {
        return this.mResolver.query(Uri.parse(CONTENT_URI_BUFFERDB + "/" + CloudMessageProviderContract.CONTENTPRDR_SUMMARYTABLE + "/" + this.SLOT_ID + j), (String[]) null, (String) null, (String[]) null, (String) null);
    }

    /* access modifiers changed from: protected */
    public Cursor queryRCSNotificationDB(long j) {
        return this.mResolver.query(Uri.parse(CONTENT_URI_BUFFERDB + "/" + "notification" + "/" + this.SLOT_ID + j), (String[]) null, (String) null, (String[]) null, (String) null);
    }

    /* access modifiers changed from: protected */
    public Cursor queryRCSNotificationDBUsingImdnAndTelUri(String str, String str2) {
        String[] strArr = {str, str2};
        return this.mResolver.query(Uri.parse(CONTENT_URI_BUFFERDB + "/" + CloudMessageProviderContract.CONTENTPRDR_NOTIFICATION_IMDN + "/" + this.SLOT_ID + str), (String[]) null, "imdn_id=? AND sender_uri=?", strArr, (String) null);
    }

    /* access modifiers changed from: protected */
    public Cursor queryRCSMessageDBUsingRowId(long j) {
        return this.mResolver.query(Uri.parse(CONTENT_URI_BUFFERDB + "/" + CloudMessageProviderContract.CONTENTPRDR_RCSMESSAGES + "/" + this.SLOT_ID + j), (String[]) null, (String) null, (String[]) null, (String) null);
    }

    /* access modifiers changed from: protected */
    public Cursor queryRCSMessageDBUsingImdn(String str) {
        return this.mResolver.query(Uri.parse(CONTENT_URI_BUFFERDB + "/" + CloudMessageProviderContract.CONTENTPRDR_RCSMESSAGEIMDN + "/" + this.SLOT_ID + str), (String[]) null, (String) null, (String[]) null, (String) null);
    }

    /* access modifiers changed from: protected */
    public Cursor queryVvmDataBufferDB(long j) {
        return this.mResolver.query(Uri.parse(CONTENT_URI_BUFFERDB + "/" + CloudMessageProviderContract.CONTENTPRDR_VVMMESSAGES + "/" + this.SLOT_ID + j), (String[]) null, (String) null, (String[]) null, (String) null);
    }

    /* access modifiers changed from: protected */
    public Cursor querymmsPduBufferDB(long j) {
        return this.mResolver.query(Uri.parse(CONTENT_URI_BUFFERDB + "/" + CloudMessageProviderContract.CONTENTPRDR_MMSPDUMESSAGE + "/" + this.SLOT_ID + j), (String[]) null, (String) null, (String[]) null, (String) null);
    }

    /* access modifiers changed from: protected */
    public Cursor queryAddrBufferDB(long j) {
        return this.mResolver.query(Uri.parse(CONTENT_URI_BUFFERDB + "/" + CloudMessageProviderContract.CONTENTPRDR_MMSADDRMESSAGES + "/" + this.SLOT_ID + j), (String[]) null, (String) null, (String[]) null, (String) null);
    }

    /* access modifiers changed from: protected */
    public Cursor queryPartsBufferDBUsingPduBufferId(long j) {
        return this.mResolver.query(Uri.parse(CONTENT_URI_BUFFERDB + "/" + CloudMessageProviderContract.CONTENTPRDR_MMSPARTMESSAGES_PDUID + "/" + this.SLOT_ID + j), (String[]) null, (String) null, (String[]) null, (String) null);
    }

    /* access modifiers changed from: protected */
    public Cursor queryPartsBufferDBUsingPartBufferId(long j) {
        return this.mResolver.query(Uri.parse(CONTENT_URI_BUFFERDB + "/" + CloudMessageProviderContract.CONTENTPRDR_MMSPARTMESSAGES_PARTID + "/" + this.SLOT_ID + j), (String[]) null, (String) null, (String[]) null, (String) null);
    }

    /* access modifiers changed from: protected */
    public int updateRcsMessageBufferDB(long j, int i, int i2) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(i2));
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(i));
        Uri parse = Uri.parse(CONTENT_URI_BUFFERDB + "/" + CloudMessageProviderContract.CONTENTPRDR_RCSCHATMESSAGE + "/" + this.SLOT_ID + j);
        return this.mResolver.update(parse, contentValues, "_bufferdbid=?", new String[]{Long.toString(j)});
    }

    /* access modifiers changed from: protected */
    public Cursor queryrcsMessageBufferDB(long j) {
        return this.mResolver.query(Uri.parse(CONTENT_URI_BUFFERDB + "/" + CloudMessageProviderContract.CONTENTPRDR_RCSCHATMESSAGE + "/" + this.SLOT_ID + j), (String[]) null, (String) null, (String[]) null, (String) null);
    }

    /* access modifiers changed from: protected */
    public Cursor queryVvmPinBufferDB(long j) {
        return this.mResolver.query(Uri.parse(CONTENT_URI_BUFFERDB + "/" + CloudMessageProviderContract.CONTENTPRDR_VVMPIN + "/" + this.SLOT_ID + j), (String[]) null, (String) null, (String[]) null, (String) null);
    }

    /* access modifiers changed from: protected */
    public Cursor queryVvmProfileBufferDB(long j) {
        return this.mResolver.query(Uri.parse(CONTENT_URI_BUFFERDB + "/" + CloudMessageProviderContract.CONTENTPRDR_VVMPROFILE + "/" + this.SLOT_ID + j), (String[]) null, (String) null, (String[]) null, (String) null);
    }

    /* access modifiers changed from: protected */
    public Cursor queryGroupSessionDB(long j) {
        return this.mResolver.query(Uri.parse(CONTENT_URI_BUFFERDB + "/" + CloudMessageProviderContract.CONTENTPRDR_RCSSESSION + "/" + this.SLOT_ID + j), (String[]) null, "_bufferdbid= ?", (String[]) null, (String) null);
    }
}
