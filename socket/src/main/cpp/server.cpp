//
// Created by 24415 on 2025-02-07.
//
#include "socket.h"

#include <stdio.h>

#include <unistd.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <pthread.h>
#include <jni.h>

#define LISTEN_BACKLOG 50

void *handle_client(void *arg) {
    int cfd = *(int *) arg;
    char buffer[100];

    // 接收消息
    recv(cfd, buffer, sizeof(buffer), 0);
    printf("Received: %s\n", buffer);

    close(cfd);
    return NULL;
}

void startServerSocket() {
    int sfd;
    struct sockaddr_un my_addr, peer_addr;
    socklen_t peer_addr_size;
    pthread_t pthread;

    sfd = socket(AF_UNIX, SOCK_STREAM, 0);
    if (sfd == -1) {
        return;
    }

    memset(&my_addr, 0, sizeof(struct sockaddr_un));
    my_addr.sun_family = AF_UNIX;
    strncpy(my_addr.sun_path, SOCKET_PATH, sizeof(my_addr.sun_path) - 1);

    unlink(SOCKET_PATH);

    if (bind(sfd, (struct sockaddr *) &my_addr, sizeof(struct sockaddr_un)) == -1) {
        return;
    }

    if (listen(sfd, LISTEN_BACKLOG) == -1) {
        return;
    }

    peer_addr_size = sizeof(struct sockaddr_un);
    while (true) {
        int cfd = accept(sfd, (struct sockaddr *) &peer_addr, &peer_addr_size);
        if (cfd == -1) {
            return;
        }
        // 创建新线程处理客户端
        pthread_create(&pthread, NULL, handle_client, (void *) &cfd);
        pthread_detach(pthread); // 使线程在结束后自动回收
    }

    close(sfd);
    unlink(SOCKET_PATH);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_pujh_socket_RemoteService_startNativeServerSocket(JNIEnv *env, jobject thiz) {

}

extern "C"
JNIEXPORT void JNICALL
Java_com_pujh_socket_RemoteService_stopNativeServerSocket(JNIEnv *env, jobject thiz) {

}