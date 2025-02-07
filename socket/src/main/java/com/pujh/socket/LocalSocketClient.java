package com.pujh.socket;

import static com.pujh.socket.MainActivity.SOCKET_NAME;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class LocalSocketClient {
    private static final String TAG = "Client";

    private SocketThread socketThread = null;
    private boolean isStart = false;
    private boolean isRunning = false;

    public void startClient() {
        if (isStart && isRunning) {
            return;
        }
        isStart = true;
        try {
            LocalSocket socket = new LocalSocket();
            LocalSocketAddress address = new LocalSocketAddress(SOCKET_NAME, LocalSocketAddress.Namespace.ABSTRACT);
            socket.connect(address);
            socketThread = new SocketThread(socket);
            socketThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopClient() {
        if (!isStart) {
            return;
        }
        isStart = false;
        try {
            socketThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            socketThread = null;
        }
    }

    public void sendData(byte[] data) {
        if (socketThread == null) {
            return;
        }
        socketThread.sendData(data);
    }

    private class SocketThread extends Thread {
        private final LocalSocket socket;
        private final InputStream input;
        private final OutputStream output;

        private SocketThread(LocalSocket socket) throws IOException {
            this.socket = socket;
            input = socket.getInputStream();
            output = socket.getOutputStream();
        }

        @Override
        public void run() {
            isRunning = true;
            byte[] buffer = new byte[1024];
            while (isStart) {
                try {
                    int len = input.read(buffer);
                    if (len > 0) {
                        String text = new String(buffer, 0, len, StandardCharsets.UTF_8);
                        Log.d(TAG, "client received text: " + text);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            //线程结束时关闭client socket
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            isRunning = false;
        }

        public void sendData(byte[] data) {
            try {
                output.write(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
