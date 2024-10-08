package com.samsung.android.cmcp2phelper.transport.internal;

import android.os.Handler;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

public class CphUnicastSender extends CphSenderReceiver {
    public static final String LOG_TAG = ("cmcp2phelper/1.3.06/" + CphUnicastSender.class.getSimpleName());
    private int mRetransmissionCount;

    public CphUnicastSender(String str, int i, byte[] bArr, int i2, int i3) {
        this.mTargetIP = str;
        this.mPort = i;
        this.mMessage = bArr;
        this.mLength = i2;
        this.mRetransmissionCount = i3;
    }

    public CphUnicastSender(String str, int i, byte[] bArr, int i2, Handler handler, int i3) {
        this.mTargetIP = str;
        this.mPort = i;
        this.mMessage = bArr;
        this.mLength = i2;
        this.mCallbackHandler = handler;
        this.mCallbackWhat = i3;
        this.mRetransmissionCount = 5;
    }

    public void run() {
        try {
            DatagramSocket datagramSocket = new DatagramSocket();
            DatagramPacket datagramPacket = new DatagramPacket(this.mMessage, this.mLength, InetAddress.getByName(this.mTargetIP), this.mPort);
            for (int i = 0; i < this.mRetransmissionCount; i++) {
                print("[U--->](" + this.mTargetIP + ":" + this.mPort + ")" + new String(this.mMessage, StandardCharsets.UTF_8));
                datagramSocket.send(datagramPacket);
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (SocketException e2) {
            e2.printStackTrace();
        } catch (UnknownHostException e3) {
            e3.printStackTrace();
        } catch (IOException e4) {
            e4.printStackTrace();
        }
    }
}
