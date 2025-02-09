package com.pujh.socket;

import static com.pujh.socket.MainActivity.SOCKET_NAME;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

public class RemoteService extends Service {
    private static final String TAG = "Server";

    private static final int START_JAVA_SERVER_SOCKET = 100;
    private static final int STOP_JAVA_SERVER_SOCKET = 200;
    private static final int START_NATIVE_SERVER_SOCKET = 300;
    private static final int STOP_NATIVE_SERVER_SOCKET = 400;

    private final LocalSocketServer server = new LocalSocketServer();

    static {
        System.loadLibrary("server");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind");
        return false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int action = intent.getIntExtra("action", -1);
        switch (action) {
            case START_JAVA_SERVER_SOCKET:
                startJavaServerSocket();
                break;
            case STOP_JAVA_SERVER_SOCKET:
                stopJavaServerSocket();
                break;
            case START_NATIVE_SERVER_SOCKET:
                startNativeServerSocket();
                break;
            case STOP_NATIVE_SERVER_SOCKET:
                stopNativeServerSocket();
                break;
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void startJavaServerSocket() {
        server.startServer(SOCKET_NAME);
    }

    private void stopJavaServerSocket() {
        server.stopServer();
    }

    private native void startNativeServerSocket();

    private native void stopNativeServerSocket();

    private static void sendRemoteAction(Context context, int action) {
        Intent intent = new Intent(context, RemoteService.class);
        intent.putExtra("action", action);
        context.startService(intent);
    }

    public static void startJavaServerSocket(Context context) {
        sendRemoteAction(context, START_JAVA_SERVER_SOCKET);
    }

    public static void stopJavaServerSocket(Context context) {
        sendRemoteAction(context, STOP_JAVA_SERVER_SOCKET);
    }

    public static void startNativeServerSocket(Context context) {
        sendRemoteAction(context, START_NATIVE_SERVER_SOCKET);
    }

    public static void stopNativeServerSocket(Context context) {
        sendRemoteAction(context, STOP_NATIVE_SERVER_SOCKET);
    }

}
