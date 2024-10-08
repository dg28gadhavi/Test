package com.sec.internal.ims.core.iil;

import java.io.ByteArrayOutputStream;

public class IilImsPreference {
    private static final int IMS_PREFERENCE_NUMBERS = 14;
    private byte eccPreference;
    private byte enableSmsFallback;
    private byte enableSmsOverIms;
    private byte enableSmsWriteUicc;
    private byte eutranDomain;
    private byte imsSupportType;
    private byte smsFormat;
    private byte srvccVersion;
    private byte ssCsfb;
    private byte ssDomain;
    private byte supportVolteRoaming;
    private byte ussdDomain;
    private byte utranDomain;

    public IilImsPreference() {
        this.smsFormat = -1;
        this.enableSmsOverIms = -1;
        this.enableSmsWriteUicc = -1;
        this.enableSmsFallback = -1;
        this.eutranDomain = -1;
        this.utranDomain = -1;
        this.ssDomain = -1;
        this.ussdDomain = -1;
        this.eccPreference = -1;
        this.ssCsfb = -1;
        this.imsSupportType = -1;
        this.srvccVersion = -1;
        this.supportVolteRoaming = -1;
    }

    public IilImsPreference(byte b) {
        this.smsFormat = b;
        this.enableSmsOverIms = b;
        this.enableSmsWriteUicc = b;
        this.enableSmsFallback = b;
        this.eutranDomain = b;
        this.utranDomain = b;
        this.ssDomain = b;
        this.ussdDomain = b;
        this.eccPreference = b;
        this.ssCsfb = b;
        this.imsSupportType = b;
        this.srvccVersion = b;
        this.supportVolteRoaming = b;
    }

    public void setSmsFormat(byte b) {
        this.smsFormat = b;
    }

    public void setSmsOverIms(byte b) {
        this.enableSmsOverIms = b;
    }

    public void setSmsWriteUicc(byte b) {
        this.enableSmsWriteUicc = b;
    }

    public void setSmsFallbackPreference(byte b) {
        this.enableSmsFallback = b;
    }

    public void setEutranDomain(byte b) {
        this.eutranDomain = b;
    }

    public void setUtranDomain(byte b) {
        this.utranDomain = b;
    }

    public void setSsDomain(byte b) {
        this.ssDomain = b;
    }

    public void setUssdDomain(byte b) {
        this.ussdDomain = b;
    }

    public void setEccPreference(byte b) {
        this.eccPreference = b;
    }

    public void setSsCsfb(byte b) {
        this.ssCsfb = b;
    }

    public void setImsSupportType(byte b) {
        this.imsSupportType = b;
    }

    public void setSrvccVersion(byte b) {
        this.srvccVersion = b;
    }

    public void setSupportVolteRoaming(byte b) {
        this.supportVolteRoaming = b;
    }

    public byte getSmsFormat() {
        return this.smsFormat;
    }

    public byte getSmsOverIms() {
        return this.enableSmsOverIms;
    }

    public byte getSmsWriteUicc() {
        return this.enableSmsWriteUicc;
    }

    public byte getSmsFallbackPreference() {
        return this.enableSmsFallback;
    }

    public byte getEutranDomain() {
        return this.eutranDomain;
    }

    public byte getUtranDomain() {
        return this.utranDomain;
    }

    public byte getSsDomain() {
        return this.ssDomain;
    }

    public byte getUssdDomain() {
        return this.ussdDomain;
    }

    public byte getEccPreference() {
        return this.eccPreference;
    }

    public byte getSsCsfb() {
        return this.ssCsfb;
    }

    public byte getImsSupportType() {
        return this.imsSupportType;
    }

    public byte getSrvccVersion() {
        return this.srvccVersion;
    }

    public byte getSupportVolteRoaming() {
        return this.supportVolteRoaming;
    }

    public static byte[] toByteArray(IilImsPreference iilImsPreference, int i) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(112);
        byteArrayOutputStream.write(iilImsPreference.getSmsFormat());
        byteArrayOutputStream.write(iilImsPreference.getSmsOverIms());
        byteArrayOutputStream.write(iilImsPreference.getSmsWriteUicc());
        byteArrayOutputStream.write(iilImsPreference.getSmsFallbackPreference());
        byteArrayOutputStream.write(iilImsPreference.getEutranDomain());
        byteArrayOutputStream.write(iilImsPreference.getUtranDomain());
        byteArrayOutputStream.write(iilImsPreference.getSsDomain());
        byteArrayOutputStream.write(iilImsPreference.getUssdDomain());
        byteArrayOutputStream.write(iilImsPreference.getEccPreference());
        byteArrayOutputStream.write(iilImsPreference.getSsCsfb());
        byteArrayOutputStream.write(iilImsPreference.getImsSupportType());
        byteArrayOutputStream.write(iilImsPreference.getSrvccVersion());
        byteArrayOutputStream.write(iilImsPreference.getSupportVolteRoaming());
        byteArrayOutputStream.write((byte) i);
        return byteArrayOutputStream.toByteArray();
    }

    public String toString() {
        return "smsFormat " + this.smsFormat + " enableSmsOverIms " + this.enableSmsOverIms + " enableSmsWriteUicc " + this.enableSmsWriteUicc + " enableSmsFallback " + this.enableSmsFallback + " eutranDomain " + this.eutranDomain + " utranDomain " + this.utranDomain + " ssDomain " + this.ssDomain + " ussdDomain " + this.ussdDomain + " eccPreference " + this.eccPreference + " ssCsfb " + this.ssCsfb + " imsSupportType " + this.imsSupportType + " srvccVersion " + this.srvccVersion + " supportVolteRoaming " + this.supportVolteRoaming;
    }
}
