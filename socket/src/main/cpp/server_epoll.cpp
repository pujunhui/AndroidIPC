//
// Created by 24415 on 2025-02-07.
//
#define LOG_TAG "socket_server"

#include "socket.h"

#include <stdio.h>

#include <unistd.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <pthread.h>
#include <jni.h>
#include <ctype.h>
#include <sys/epoll.h>

#define LISTEN_BACKLOG 50
#define BUFFER_SIZE 1024

void startServerSocket(const char *name, int namespaceId) {
    int sfd;
    struct sockaddr_un addr;
    socklen_t addrlen;

    /**
     * af: 协议族，本地进程间通信使用AF_UNIX或AF_LOCAL，网络通信使用AF_INET
     * type: socket类型，常用类型有SOCKET_STREAM(TCP)和SOCKET_DGRAM(UDP)
     */
    sfd = socket(AF_UNIX, SOCK_STREAM, 0);
    if (sfd == -1) {
        LOG_E("socket server create filed!");
        return;
    }

    if (socket_make_sockaddr_un(name, namespaceId, &addr, &addrlen) == -1) {
        LOG_E("socket server make sockaddr_un filed!");
        return;
    }

    if (namespaceId == SOCKET_NAMESPACE_FILESYSTEM) {
        //文件系统socket在绑定之前需要unlink，不然可能会报错already in use
        unlink(name);
    }

    if (bind(sfd, (struct sockaddr *) &addr, addrlen) == -1) {
        LOG_E("socket server bind filed!");
        return;
    }

    if (listen(sfd, LISTEN_BACKLOG) == -1) {
        LOG_E("socket server listen filed!");
        return;
    }
    LOG_D("Accepting connections...");

    //创建epoll
    int epfd = epoll_create(LISTEN_BACKLOG);
    if (epfd == -1) {
        LOG_E("epoll create failed!");
        return;
    }

    //注册socket客户端连接监听
    struct epoll_event ev;
    ev.events = POLL_IN; //需要监听的事件
    ev.data.fd = sfd; //需要监听的文件句柄
    epoll_ctl(epfd, EPOLL_CTL_ADD, sfd, &ev);

    struct epoll_event events[100];
    char buffer[BUFFER_SIZE];

    while (true) {
        int num = epoll_wait(epfd, events, sizeof(events) / sizeof(struct epoll_event), -1);
        for (int i = 0; i < num; i++) {
            if (events[i].data.fd == sfd) { //有连接请求到来
                //处理新连接
                int cfd = accept(sfd, NULL, NULL);
                if (cfd == -1) {
                    LOG_E("socket server accept filed!");
                    return;
                }

                //将新连接添加到 epoll
                ev.events = EPOLLIN;
                ev.data.fd = cfd;
                if (epoll_ctl(epfd, EPOLL_CTL_ADD, cfd, &ev) == -1) {
                    LOG_E("epoll_ctl error!");
                    close(cfd);
                }
            } else { //有读写请求到来
                if (events[i].events & POLL_OUT) {
                    //忽略写事件
                    continue;
                }
                int cfd = events[i].data.fd;
                ssize_t size = read(cfd, buffer, BUFFER_SIZE);
                if (size <= 0) {
                    // 客户端断开连接
                    LOG_D("socket client has disconnect!");
                    //将客户端从epoll监听中移除
                    epoll_ctl(epfd, EPOLL_CTL_DEL, cfd, NULL);
                    close(cfd);
                } else {
                    // 回显数据
                    write(cfd, buffer, size);
                }
            }
        }
    }

    close(sfd);
    unlink(name);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_pujh_socket_RemoteService_startNativeServerSocket(JNIEnv *env, jobject thiz) {

}

extern "C"
JNIEXPORT void JNICALL
Java_com_pujh_socket_RemoteService_stopNativeServerSocket(JNIEnv *env, jobject thiz) {

}