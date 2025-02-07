package com.pujh.aidl;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.Timer;
import java.util.TimerTask;

public class RemoteService extends Service {
    private static final String TAG = "Server";

    private final Timer timer = new Timer();
    private TimerTask timerTask = null;

    private ICallback callback = null;
    private final RemoteCallbackList<ICallback> callbacks = new RemoteCallbackList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        timerTask = new LogTask();
        timer.schedule(timerTask, 0, 1000);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        timerTask.cancel();
        timerTask = null;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return userInterface;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind");
        return false;
    }

    private final IUserManager.Stub userInterface = new IUserManager.Stub() {

        @Override
        public void notifyServerClash() throws RemoteException {
            Log.d(TAG, "server will be killed");
            Process.killProcess(Process.myPid());
        }

        @Override
        public void addUser(int id, String name) throws RemoteException {
            Log.d(TAG, "addUser server: id=" + id + ", name=" + name);
        }

        @Override
        public User getUser(int id) throws RemoteException {
            return new User(id, "user" + id);
        }

        @Override
        public void addUserIn(User user) throws RemoteException {
            Log.d(TAG, "server addUserIn start: " + user);
            user.setName("newName");
            Log.d(TAG, "server addUserIn end: " + user);
        }

        @Override
        public void getUserOut(User user) throws RemoteException {
            Log.d(TAG, "server getUserOut start: " + user);
            user.setName("newName");
            Log.d(TAG, "server addUserOut end: " + user);
        }

        @Override
        public void modifyUserInout(User user) throws RemoteException {
            Log.d(TAG, "server modifyUserInout start: " + user);
            user.setName("newName");
            Log.d(TAG, "server modifyUserInout end: " + user);
        }

        @Override
        public void addUserWithoutOneway(User user) throws RemoteException {
            Log.d(TAG, "server addUserWithoutOneway start");
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "server addUserWithoutOneway end");
        }

        @Override
        public void addUserOneway(User user) throws RemoteException {
            Log.d(TAG, "server addUserOneway start");
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "server addUserOneway end");
        }

        @Override
        public void setCallback(ICallback callback) throws RemoteException {
            if (RemoteService.this.callback != null) {
                RemoteService.this.callback.asBinder().unlinkToDeath(recipient, 0);
                RemoteService.this.callback = null;
            }
            if (callback != null) {
                RemoteService.this.callback = callback;
                callback.asBinder().linkToDeath(recipient, 0);
            }
        }

        @Override
        public void addCallback(ICallback callback) throws RemoteException {
            callbacks.register(callback);
        }

        @Override
        public void removeCallback(ICallback callback) throws RemoteException {
            callbacks.unregister(callback);
        }
    };

    private final IBinder.DeathRecipient recipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            Log.d(TAG, "Client is died!");
            RemoteService.this.callback = null;
        }
    };

    private class LogTask extends TimerTask {
        private int i = 0;

        @Override
        public void run() {
            Log.v(TAG, "RemoteService is running! " + i);
            User user = new User(i, "user" + i);

            //单个callback回调
            if (callback != null) {
                try {
                    callback.update(user);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            //多个callback回调
            int count = callbacks.beginBroadcast();
            for (int i = 0; i < count; i++) {
                try {
                    callbacks.getBroadcastItem(i).update(user);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            callbacks.finishBroadcast();
            i++;
        }
    }
}
