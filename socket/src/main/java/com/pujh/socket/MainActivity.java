package com.pujh.socket;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity {
    public static final String SOCKET_NAME = "java_socket";

    private final LocalSocketClient client = new LocalSocketClient();

    static {
        System.loadLibrary("client");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        findViewById(R.id.start_java_server).setOnClickListener(v -> startJavaServerSocket());
        findViewById(R.id.stop_java_server).setOnClickListener(v -> stopJavaServerSocket());
        findViewById(R.id.start_java_client).setOnClickListener(v -> startJavaClientSocket());
        findViewById(R.id.stop_java_client).setOnClickListener(v -> stopJavaClientSocket());
        findViewById(R.id.test_java_loopback).setOnClickListener(v -> testJavaLoopback());
    }

    private void startJavaServerSocket() {
        RemoteService.startJavaServerSocket(this);
    }

    private void stopJavaServerSocket() {
        RemoteService.stopJavaServerSocket(this);
    }

    private void startJavaClientSocket() {
        client.startClient(SOCKET_NAME);
    }

    private void stopJavaClientSocket() {
        client.stopClient();
    }

    private void testJavaLoopback() {
        String text = "当前时间:" + System.currentTimeMillis();
        client.sendData(text.getBytes(StandardCharsets.UTF_8));
    }

    private native void startNativeClientSocket();

    private native void stopNativeClientSocket();
}