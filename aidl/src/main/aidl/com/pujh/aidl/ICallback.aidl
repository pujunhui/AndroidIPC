package com.pujh.aidl;

import com.pujh.aidl.User;

interface ICallback {
    void update(in User user);
}