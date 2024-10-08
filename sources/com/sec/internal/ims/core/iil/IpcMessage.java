package com.sec.internal.ims.core.iil;

import com.sec.internal.log.IMSLog;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class IpcMessage {
    public static final int IPC_CMD_CFRM = 4;
    public static final int IPC_CMD_EVENT = 5;
    public static final int IPC_CMD_EXEC = 1;
    public static final int IPC_CMD_GET = 2;
    public static final int IPC_CMD_INDI = 1;
    public static final int IPC_CMD_NOTI = 3;
    public static final int IPC_CMD_RESP = 2;
    public static final int IPC_CMD_SET = 3;
    public static final int IPC_DEBUG_HDR_SIZE = 12;
    public static final int IPC_FROM_IIL = 1;
    public static final int IPC_FROM_RIL = 0;
    public static final int IPC_GEN_CMD = 128;
    public static final int IPC_GEN_ERR_INVALID_STATE = 32773;
    public static final int IPC_GEN_ERR_NONE = 32768;
    public static final int IPC_GEN_ERR_SIM_PIN2_PERM_BLOCKED = 32782;
    public static final int IPC_GEN_PHONE_RES = 1;
    public static final int IPC_HDR_SIZE = 7;
    public static final int IPC_IIL_CHANGE_PREFERRED_NETWORK_TYPE = 21;
    public static final int IPC_IIL_CMD = 112;
    public static final int IPC_IIL_EMC_ATTACH_AUTH = 32;
    public static final int IPC_IIL_IIL_CONNECTED = 18;
    public static final int IPC_IIL_IMS_SUPPORT_STATE = 16;
    public static final int IPC_IIL_ISIM_LOADED = 17;
    public static final int IPC_IIL_PREFERENCE = 6;
    public static final int IPC_IIL_REGISTRATION = 1;
    public static final int IPC_IIL_RETRYOVER = 12;
    public static final int IPC_IIL_SET_DEREGISTRATION = 11;
    public static final int IPC_IIL_SIP_SUSPEND = 22;
    public static final int IPC_IIL_SSAC_INFO = 14;
    public static final int IPC_IIL_VONR_USER_STATUS = 33;
    public static final int IPC_IMS_ERR_403_FORBIDDEN = 34049;
    public static final int IPC_IMS_ERR_MAX_RANGE = 34303;
    public static final int IPC_SS_ERR_MISTYPED_PARAM = 33298;
    private static final String LOG_TAG = "IpcMessage";
    public static final int MAX_IPC_HEADER = 19;
    protected int mAsequence;
    protected int mCmdType;
    protected int mDir;
    protected byte[] mIpcBody;
    protected byte[] mIpcData;
    protected byte[] mIpcHeader;
    protected int mLength;
    protected int mMainCmd;
    protected int mNetworkType;
    protected int mSequence;
    protected int mSubCmd;

    public byte[] encode() {
        return new byte[0];
    }

    public IpcMessage() {
    }

    public IpcMessage(int i, int i2, int i3) {
        this.mMainCmd = i;
        this.mSubCmd = i2;
        this.mCmdType = i3;
    }

    public boolean makeHeader() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(100);
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        try {
            dataOutputStream.write(0);
            dataOutputStream.write(0);
            dataOutputStream.write(this.mMainCmd);
            dataOutputStream.write(this.mSubCmd);
            dataOutputStream.write(this.mCmdType);
            dataOutputStream.close();
            this.mIpcHeader = byteArrayOutputStream.toByteArray();
            return true;
        } catch (IOException e) {
            IMSLog.e(LOG_TAG, "failed in makeHeader() " + e);
            return false;
        }
    }

    public byte[] createIpcMessage() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(100);
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        byte[] bArr = this.mIpcBody;
        int i = 7;
        if (bArr != null) {
            i = 7 + bArr.length;
        }
        makeHeader();
        try {
            dataOutputStream.write(i);
            dataOutputStream.write(i >> 8);
            dataOutputStream.write(this.mIpcHeader, 0, 5);
            byte[] bArr2 = this.mIpcBody;
            if (bArr2 != null) {
                dataOutputStream.write(bArr2, 0, bArr2.length);
            }
            this.mIpcData = byteArrayOutputStream.toByteArray();
            dataOutputStream.close();
            return this.mIpcData;
        } catch (IOException e) {
            IMSLog.e(LOG_TAG, "failed in createIpcMessage() " + e);
            return null;
        }
    }

    public void setDir(int i) {
        this.mDir = i;
    }

    public int getMainCmd() {
        return this.mMainCmd;
    }

    public int getSubCmd() {
        return this.mSubCmd;
    }

    public int getLength() {
        return this.mLength;
    }

    public int getCmdType() {
        return this.mCmdType;
    }

    public byte[] getBody() {
        return this.mIpcBody;
    }

    public byte[] getData() {
        return this.mIpcData;
    }

    public int getNetworkType() {
        return this.mNetworkType;
    }

    public String typeStr() {
        if (this.mDir == 0) {
            int i = this.mCmdType;
            if (i == 1) {
                return "EXEC";
            }
            if (i == 2) {
                return "GET";
            }
            if (i == 3) {
                return "SET";
            }
            if (i == 4) {
                return "CFRM";
            }
            if (i == 5) {
                return "EVENT";
            }
            return "UNKNOWN(" + Integer.toHexString(this.mCmdType) + ")";
        }
        int i2 = this.mCmdType;
        if (i2 == 1) {
            return "INDI";
        }
        if (i2 == 2) {
            return "RESP";
        }
        if (i2 == 3) {
            return "NOTI";
        }
        return "UNKNOWN(" + Integer.toHexString(this.mCmdType) + ")";
    }

    public String mainCmdStr() {
        int i = this.mMainCmd;
        if (i == 112) {
            return "IPC_IIL_CMD";
        }
        if (i == 128) {
            return "IPC_GEN_CMD";
        }
        return "Unknown: " + this.mMainCmd;
    }

    private String subIilCmdStr() {
        int i = this.mSubCmd;
        if (i == 1) {
            return "IPC_IIL_REGISTRATION";
        }
        if (i == 6) {
            return "IPC_IIL_PREFERENCE";
        }
        if (i == 14) {
            return "IPC_IIL_SSAC_INFO";
        }
        if (i == 11) {
            return "IPC_IIL_SET_DEREGISTRATION";
        }
        if (i == 12) {
            return "IPC_IIL_RETRYOVER";
        }
        if (i == 21) {
            return "IPC_IIL_CHANGE_PREFERRED_NETWORK_TYPE";
        }
        if (i == 22) {
            return "IPC_IIL_SIP_SUSPEND";
        }
        if (i == 32) {
            return "IPC_IIL_EMC_ATTACH_AUTH";
        }
        if (i == 33) {
            return "IPC_IIL_VONR_USER_STATUS";
        }
        switch (i) {
            case 16:
                return "IPC_IIL_IMS_SUPPORT_STATE";
            case 17:
                return "IPC_IIL_ISIM_LOADED";
            case 18:
                return "IPC_IIL_IIL_CONNECTED";
            default:
                return "Unknown: " + this.mSubCmd;
        }
    }

    private String subGenCmdStr() {
        if (this.mSubCmd == 1) {
            return "IPC_GEN_PHONE_RES";
        }
        return "Unknown: " + this.mSubCmd;
    }

    public String subCmdStr() {
        int i = this.mMainCmd;
        if (i == 112) {
            return subIilCmdStr();
        }
        if (i == 128) {
            return subGenCmdStr();
        }
        return "Unknown Main: " + this.mMainCmd;
    }

    public static IpcMessage parseIpc(byte[] bArr, int i) {
        IilIpcMessage iilIpcMessage;
        DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(bArr));
        try {
            byte readByte = dataInputStream.readByte();
            byte readByte2 = dataInputStream.readByte();
            byte readByte3 = dataInputStream.readByte();
            if (readByte3 != 112) {
                iilIpcMessage = null;
            } else {
                iilIpcMessage = new IilIpcMessage();
            }
            if (iilIpcMessage != null) {
                iilIpcMessage.mSequence = readByte;
                iilIpcMessage.mAsequence = readByte2;
                iilIpcMessage.mMainCmd = readByte3;
                iilIpcMessage.mSubCmd = dataInputStream.readUnsignedByte();
                iilIpcMessage.mCmdType = dataInputStream.readByte();
                iilIpcMessage.mLength = i;
                if (i > 7) {
                    int i2 = i - 7;
                    byte[] bArr2 = new byte[i2];
                    iilIpcMessage.mIpcBody = bArr2;
                    if (dataInputStream.read(bArr2, 0, i2) < 0) {
                        IMSLog.s(LOG_TAG, "parseIpc: ipcMsg.mIpcBody - the end of the stream has been reached.");
                    }
                    iilIpcMessage.mNetworkType = iilIpcMessage.mIpcBody[0];
                }
                iilIpcMessage.mDir = 0;
            }
            dataInputStream.close();
            return iilIpcMessage;
        } catch (IOException e) {
            IMSLog.e(LOG_TAG, e.getMessage());
            try {
                dataInputStream.close();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
            return null;
        }
    }

    public boolean encodeGeneralResponse(int i, IpcMessage ipcMessage) {
        byte[] bArr = new byte[5];
        bArr[0] = (byte) ipcMessage.mMainCmd;
        bArr[1] = (byte) ipcMessage.mSubCmd;
        bArr[2] = (byte) ipcMessage.mCmdType;
        if ((i < 32768 || i > 32782) && ((i < 34049 || i > 34303) && i != 33298)) {
            IMSLog.e(LOG_TAG, "encodeGeneralResponse(): ipcErrorCause is out of range with value ( " + String.format("%04X ", new Object[]{Integer.valueOf(i)}) + " ), but keep going. ");
            i = IPC_GEN_ERR_INVALID_STATE;
        }
        bArr[3] = (byte) (i & 255);
        bArr[4] = (byte) ((i >> 8) & 255);
        this.mIpcBody = bArr;
        createIpcMessage();
        return true;
    }
}
