package com.pujh.messenger;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class RemoteService extends Service {
    private static final String TAG = "Server";

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
        return new Messenger(handler).getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind");
        return false;
    }

    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void dispatchMessage(@NonNull Message msg) {
            if (msg.what == 100) {
                Bundle bundle = msg.getData();

                //读取数据前，必须先设置ClassLoader，不然会抛出异常
                bundle.setClassLoader(User.class.getClassLoader());
                User user = bundle.getParcelable("user");
                Log.d(TAG, "server received message: what=" + msg.what + ", user=" + user);

                //获取客户端设置的回调Messenger
                Messenger client = msg.replyTo;

                Message newMsg = Message.obtain();
                newMsg.what = 200;
                Bundle newBundle = new Bundle();
                User newUser = new User(user.getId(), "Loopback " + user.getName());
                newBundle.putParcelable("user", newUser);
                newMsg.setData(newBundle);

                try {
                    client.send(newMsg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    };
}
