package com.pujh.socket;

import static com.pujh.socket.MainActivity.SOCKET_NAME;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
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

    public void startClient(String socketName) {
        Log.d(TAG, "Client start");
        if (isStart && isRunning) {
            return;
        }
        isStart = true;
        try {
            LocalSocket socket = new LocalSocket();
            LocalSocketAddress address = new LocalSocketAddress(socketName, LocalSocketAddress.Namespace.ABSTRACT);
            socket.connect(address);
            socketThread = new SocketThread(socket);
            socketThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopClient() {
        Log.d(TAG, "Client stop");
        if (!isStart) {
            return;
        }
        isStart = false;
        try {
            socketThread.closeSocket();
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
            Log.d(TAG, "Client running start");
            byte[] buffer = new byte[1024];
            while (isStart) {
                try {
                    int len = input.read(buffer);
                    if (len > 0) {
                        String text = new String(buffer, 0, len, StandardCharsets.UTF_8);
                        Log.d(TAG, "client received text: " + text);
                    } else if (len == -1) {
                        Log.d(TAG, "client received eof!");
                        break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }

            //线程结束时关闭client socket
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            isRunning = false;
            Log.d(TAG, "Client running end");
        }

        public void closeSocket() {
            if (isRunning) {
                try {
                    Os.shutdown(socket.getFileDescriptor(), OsConstants.SHUT_RDWR);
                } catch (ErrnoException e) {
                    if (e.errno != OsConstants.EBADF) {
                        Log.e(TAG, "关闭失败", e);
                    }
                }
            }
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
