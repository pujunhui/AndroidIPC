package com.pujh.messenger;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Client";

    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void dispatchMessage(@NonNull Message msg) {
            if (msg.what == 200) {
                Bundle bundle = msg.getData();

                //读取数据前，必须先设置ClassLoader，不然会抛出异常
                bundle.setClassLoader(User.class.getClassLoader());
                User user = bundle.getParcelable("user");
                Log.d(TAG, "client received message: what=" + msg.what + ", user=" + user);
            }
        }
    };
    private final Messenger clientMessenger = new Messenger(handler);
    private Messenger serverMessenger = null;

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

        findViewById(R.id.start_server).setOnClickListener(v -> startServer());
        findViewById(R.id.stop_server).setOnClickListener(v -> stopServer());
        findViewById(R.id.connect_server).setOnClickListener(v -> connectServer());
        findViewById(R.id.disconnect_server).setOnClickListener(v -> disconnectServer());
        findViewById(R.id.test_loopback).setOnClickListener(v -> testLoopback());
    }

    private void startServer() {
        Intent intent = new Intent(this, RemoteService.class);
        startService(intent);
    }

    private void stopServer() {
        Intent intent = new Intent(this, RemoteService.class);
        stopService(intent);
    }

    private void connectServer() {
        Intent intent = new Intent(this, RemoteService.class);
        bindService(intent, connection, BIND_AUTO_CREATE);
    }

    private void disconnectServer() {
        if (serverMessenger != null) {
            //解绑service
            unbindService(connection);
            //主动置空userManager，因为unbindService不会回调ServiceConnection#onServiceDisconnected方法
            serverMessenger = null;
        }
    }

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected: " + name);
            serverMessenger = new Messenger(service);
        }

        //该方法只在服务端主动断开时回调，当客户端调用unbindService时，并不会回调该方法
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected: " + name);
            serverMessenger = null;
        }
    };

    private void testLoopback() {
        if (serverMessenger == null) {
            return;
        }
        int id = new Random(System.currentTimeMillis()).nextInt() % 100;
        User user = new User(id, "User" + id);

        Message msg = Message.obtain();
        msg.what = 100;
        //将客户端回调的Messenger传递给服务端
        msg.replyTo = clientMessenger;

        //注意：msg.obj仅支持在framework声明的Parcelable类
//        msg.obj = "text";
//        msg.obj = user;

        Bundle bundle = new Bundle();
        bundle.putParcelable("user", user);
        msg.setData(bundle);

        try {
            serverMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}