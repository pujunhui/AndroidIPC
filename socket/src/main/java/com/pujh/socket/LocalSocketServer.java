package com.pujh.socket;

import static com.pujh.socket.MainActivity.SOCKET_NAME;

import android.net.Credentials;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LocalSocketServer {
    private static final String TAG = "Server";

    private DispatchClientTask dispatchClientTask = null;
    private boolean isStart = false;
    private boolean isRunning = false;

    public void startServer() {
        Log.d(TAG, "Server start");
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
        Log.d(TAG, "Server stop");
        if (!isStart) {
            return;
        }
        isStart = false;
        try {
            dispatchClientTask.closeSocket();
            dispatchClientTask.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            dispatchClientTask = null;
        }
    }

    private class DispatchClientTask extends Thread {
        private final LocalServerSocket serverSocket;
        private final ExecutorService executor = Executors.newFixedThreadPool(10);

        private DispatchClientTask(LocalServerSocket serverSocket) {
            this.serverSocket = serverSocket;
        }

        @Override
        public void run() {
            isRunning = true;
            Log.d(TAG, "Server running start");
            while (isStart) {
                try {
                    //注意：accept会阻塞当前线程，该方法不能设置超时
                    LocalSocket socket = serverSocket.accept();
                    Credentials credentials = socket.getPeerCredentials();
                    Log.i(TAG, "accept socket uid:" + credentials.getUid());
                    executor.submit(new HandleClientTask(socket));
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }

            //线程结束时关闭server socket
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            isRunning = false;

            // 主线程退出前关闭线程池
            executor.shutdownNow();

            Log.d(TAG, "Server running end");
        }

        public void closeSocket() {
            if (isRunning) {
                try {
                    Os.shutdown(serverSocket.getFileDescriptor(), OsConstants.SHUT_RDWR);
                } catch (ErrnoException e) {
                    Log.e(TAG, "关闭失败", e);
                }
            }
        }
    }

    private static class HandleClientTask implements Runnable {
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
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    int len = input.read(buffer);
                    if (len > 0) {
                        String text = new String(buffer, 0, len, StandardCharsets.UTF_8);
                        Log.d(TAG, "server received text: " + text);
                        String newText = "Loopback: " + text;
                        output.write(newText.getBytes(StandardCharsets.UTF_8));
                    } else if (len == -1) {
                        Log.d(TAG, "server received eof!");
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
        }

        public void closeSocket() {
            if (isRunning) {
                try {
                    Os.shutdown(socket.getFileDescriptor(), OsConstants.SHUT_RDWR);
                } catch (ErrnoException e) {
                    Log.e(TAG, "关闭失败", e);
                }
            }
        }
    }
}
