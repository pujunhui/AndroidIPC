package com.pujh.aidl;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Client";

    private IUserManager userManager = null;

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
        findViewById(R.id.crash_client).setOnClickListener(v -> crashClient());
        findViewById(R.id.crash_server).setOnClickListener(v -> crashServer());
        findViewById(R.id.test_inout).setOnClickListener(v -> testInOut());
        findViewById(R.id.test_oneway).setOnClickListener(v -> testOneway());
        findViewById(R.id.set_callback).setOnClickListener(v -> setCallback());
        findViewById(R.id.remove_callback).setOnClickListener(v -> removeCallback());
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
        if (userManager != null) {
            //断开连接时，主动移除回调方法
            removeCallback();
            //解绑service
            unbindService(connection);
            //主动置空userManager，因为unbindService不会回调ServiceConnection#onServiceDisconnected方法
            userManager = null;
        }
    }

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected: " + name);
            userManager = IUserManager.Stub.asInterface(service);
        }

        //该方法只在服务端主动断开时回调，当客户端调用unbindService时，并不会回调该方法
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected: " + name);
            userManager = null;
        }
    };

    private void crashClient() {
        Log.d(TAG, "client will be killed");
        Process.killProcess(Process.myPid());
    }

    private void crashServer() {
        if (userManager == null) {
            return;
        }
        try {
            userManager.notifyServerClash();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void testInOut() {
        if (userManager == null) {
            return;
        }
        try {
            //测试in
            Log.d(TAG, "----------test In------------");
            User user = new User(100, "testIn");
            Log.d(TAG, "client addUserIn start: " + user);
            userManager.addUserIn(user);
            Log.d(TAG, "client addUserIn end: " + user);

            //测试out
            Log.d(TAG, "----------test Out------------");
            user = new User(200, "testOut");
            Log.d(TAG, "client getUserOut start: " + user);
            userManager.getUserOut(user);
            Log.d(TAG, "client getUserOut end: " + user);

            //测试inout
            Log.d(TAG, "----------test Inout------------");
            user = new User(300, "testInout");
            Log.d(TAG, "client modifyUserInout start: " + user);
            userManager.modifyUserInout(user);
            Log.d(TAG, "client modifyUserInout end: " + user);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void testOneway() {
        if (userManager == null) {
            return;
        }

        try {
            //测试不使用oneway
            Log.d(TAG, "----------test Without Oneway------------");
            User user = new User(400, "testWithoutOneway");
            Log.d(TAG, "client addUserWithoutOneway start");
            long start = System.currentTimeMillis();
            userManager.addUserWithoutOneway(user);
            long now = System.currentTimeMillis();
            Log.d(TAG, "client addUserWithoutOneway end: time=" + (now - start));

            //测试oneway
            Log.d(TAG, "----------test Oneway------------");
            user = new User(500, "testOneway");
            Log.d(TAG, "client addUserOneway start");
            start = System.currentTimeMillis();
            userManager.addUserOneway(user);
            now = System.currentTimeMillis();
            Log.d(TAG, "client addUserOneway end: time=" + (now - start));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void setCallback() {
        if (userManager == null) {
            return;
        }
        try {
            userManager.addCallback(callback);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void removeCallback() {
        if (userManager == null) {
            return;
        }
        try {
            userManager.removeCallback(callback);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private final ICallback.Stub callback = new ICallback.Stub() {
        @Override
        public void update(User user) throws RemoteException {
            Log.v(TAG, "client received user: " + user);
        }
    };
}