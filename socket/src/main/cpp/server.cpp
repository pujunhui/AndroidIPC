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

#define LISTEN_BACKLOG 50

void *handle_client(void *arg) {
    int cfd = *(int *) arg;
    char buffer[100];

    while (true) {
        //接收服务端的消息
        ssize_t size = recv(cfd, buffer, sizeof(buffer), 0);
        if (size < 0) {
            LOG_E("socket server read filed!");
            break;
        } else if (size == 0) {
            LOG_E("socket server read EOF!");
            break;
        }
        LOG_D("socket server received: %s\n", buffer);

        if (strncmp(buffer, "quit", 4) == 0) {
            LOG_E("socket server received quit!");
            break;
        }
        for (int i = 0; i < size; i++) {
            buffer[i] = toupper(buffer[i]);
        }
        //向服务端发送消息
        send(cfd, buffer, size, 0);
    }

    close(cfd);
    return NULL;
}


void startServerSocket(const char *name, int namespaceId) {
    int sfd;
    struct sockaddr_un my_addr, peer_addr;
    socklen_t peer_addr_size;
    pthread_t pthread;

    /**
     * af: 协议族，本地进程间通信使用AF_UNIX或AF_LOCAL，网络通信使用AF_INET
     * type: socket类型，常用类型有SOCKET_STREAM(TCP)和SOCKET_DGRAM(UDP)
     */
    sfd = socket(AF_UNIX, SOCK_STREAM, 0);
    if (sfd == -1) {
        LOG_E("socket server create filed!");
        return;
    }

    socklen_t addrlen;

    if (socket_make_sockaddr_un(name, namespaceId, &my_addr, &addrlen) == -1) {
        LOG_E("socket server make sockaddr_un filed!");
        return;
    }

    if (namespaceId == SOCKET_NAMESPACE_FILESYSTEM) {
        //文件系统socket在绑定之前需要unlink，不然可能会报错already in use
        unlink(name);
    }

    if (bind(sfd, (struct sockaddr *) &my_addr, addrlen) == -1) {
        LOG_E("socket server bind filed!");
        return;
    }

    if (listen(sfd, LISTEN_BACKLOG) == -1) {
        LOG_E("socket server listen filed!");
        return;
    }

    peer_addr_size = sizeof(struct sockaddr_un);
    while (true) {
        int cfd = accept(sfd, (struct sockaddr *) &peer_addr, &peer_addr_size);
        if (cfd == -1) {
            LOG_E("socket server accept filed!");
            return;
        }
        // 创建新线程处理客户端
        pthread_create(&pthread, NULL, handle_client, (void *) &cfd);
        pthread_detach(pthread); // 使线程在结束后自动回收
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