package com.pujh.socket;

import static com.pujh.socket.MainActivity.SOCKET_NAME;

import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class LocalSocketServer {
    private static final String TAG = "Server";

    private DispatchClientTask dispatchClientTask = null;
    private boolean isStart = false;
    private boolean isRunning = false;

    public void startServer() {
        if (isStart && isRunning) {
            return;
        }
        isStart = true;
        try {
            //使用LocalServerSocket，只能创建抽象命名空间socket
            LocalServerSocket serverSocket = new LocalServerSocket(SOCKET_NAME);
            dispatchClientTask = new DispatchClientTask(serverSocket);
            dispatchClientTask.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopServer() {
        if (!isStart) {
            return;
        }
        isStart = false;
        try {
            dispatchClientTask.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            dispatchClientTask = null;
        }
    }

    private class DispatchClientTask extends Thread {
        private final LocalServerSocket serverSocket;

        private DispatchClientTask(LocalServerSocket serverSocket) {
            this.serverSocket = serverSocket;
        }

        @Override
        public void run() {
            isRunning = true;
            while (isStart) {
                try {
                    LocalSocket socket = serverSocket.accept();
                    new HandleClientTask(socket).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            //线程结束时关闭server socket
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            isRunning = false;
        }
    }

    private class HandleClientTask extends Thread {
        private final LocalSocket socket;
        private final InputStream input;
        private final OutputStream output;

        private HandleClientTask(LocalSocket socket) throws IOException {
            this.socket = socket;
            input = socket.getInputStream();
            output = socket.getOutputStream();
        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            while (isStart) {
                try {
                    int len = input.read(buffer);
                    if (len > 0) {
                        String text = new String(buffer, 0, len, StandardCharsets.UTF_8);
                        Log.d(TAG, "server received text: " + text);
                        String newText = "Loopback: " + text;
                        output.write(newText.getBytes(StandardCharsets.UTF_8));
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
        }
    }
}
