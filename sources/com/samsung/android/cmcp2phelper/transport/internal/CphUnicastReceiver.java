package com.samsung.android.cmcp2phelper.transport.internal;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import com.samsung.android.cmcp2phelper.MdmnServiceInfo;
import com.samsung.android.cmcp2phelper.data.CphMessage;
import com.samsung.android.cmcp2phelper.utils.P2pUtils;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class CphUnicastReceiver extends CphSenderReceiver {
    public static final String LOG_TAG = ("cmcp2phelper/1.3.06/" + CphUnicastReceiver.class.getSimpleName());
    int mRecvPort;
    DatagramSocket mSocket;

    public CphUnicastReceiver(Context context, Handler handler, int i, MdmnServiceInfo mdmnServiceInfo) {
        this.mCallbackHandler = handler;
        this.mRecvPort = i;
        this.mServiceInfo = mdmnServiceInfo;
        this.mContext = context;
    }

    public void run() {
        if (P2pUtils.isWifiConnected(this.mContext)) {
            try {
                if (this.mRecvPort < 0) {
                    this.mSocket = new DatagramSocket();
                } else {
                    try {
                        this.mSocket = new DatagramSocket(this.mRecvPort, InetAddress.getByName(P2pUtils.getLocalIpAddress(this.mContext)));
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }
                }
                print("Start UDP server : binding ip - " + this.mSocket.getLocalAddress().toString() + ", binding port - " + this.mSocket.getLocalPort());
                this.mLocalBindingIP = P2pUtils.getLocalIpAddress(this.mContext);
                while (true) {
                    DatagramSocket datagramSocket = this.mSocket;
                    if (datagramSocket == null || datagramSocket.isClosed()) {
                        print("Stop Unicast Reponder");
                    } else {
                        DatagramPacket datagramPacket = new DatagramPacket(new byte[1400], 1400);
                        try {
                            this.mSocket.receive(datagramPacket);
                            CphMessage cphMessage = new CphMessage(datagramPacket);
                            if (cphMessage.isValid()) {
                                print("[U<---](" + datagramPacket.getAddress().getHostAddress() + ")" + cphMessage);
                                handleReceivedMessage(cphMessage);
                            }
                        } catch (IOException e2) {
                            e2.printStackTrace();
                            Log.e(LOG_TAG, "socket is closed");
                        }
                    }
                }
                print("Stop Unicast Reponder");
            } catch (SocketException e3) {
                e3.printStackTrace();
                Log.e(LOG_TAG, "SocketException");
                print("SocketException- Unicast Receiver");
            }
        }
    }

    public void stop() {
        if (this.mSocket != null) {
            Log.i(LOG_TAG, "stop responder");
            this.mSocket.close();
            this.mSocket = null;
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
