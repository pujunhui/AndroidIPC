// IUserManager.aidl
package com.pujh.aidl;

// Declare any non-default types here with import statements
import com.pujh.aidl.User;
import com.pujh.aidl.ICallback;

interface IUserManager {
    //让server端kill掉自身
    void notifyServerClash();

    //普通
    void addUser(int id, String name);
    User getUser(int id);

    //in、out、inout区别
    void addUserIn(in User user);
    void getUserOut(out User user);
    void modifyUserInout(inout User user);

    //oneway的作用
    //注意：oneway只能修饰无返回值的方法(无论是直接返回，还是通过out、inout关键字返回)
    void addUserWithoutOneway(in User user);
    oneway void addUserOneway(in User user);

    //跨进程回调
    void setCallback(ICallback callback);

    void addCallback(ICallback callback);
    void removeCallback(ICallback callback);
}