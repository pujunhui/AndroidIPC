//
// Created by 24415 on 2025-02-07.
//

#define LOG_TAG "socket_client"

#include "socket.h"

#include <sys/types.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <unistd.h>
#include <jni.h>

void startClientSocket(const char *name, int namespaceId) {
    int fd;
    struct sockaddr_un my_addr;
    socklen_t peer_addr_size;

    fd = socket(AF_UNIX, SOCK_STREAM, 0);
    if (fd == -1) {
        LOG_E("socket client create filed!");
        return;
    }

    socklen_t addrlen;

    if (socket_make_sockaddr_un(SOCKET_PATH, namespaceId, &my_addr, &addrlen) == -1) {
        LOG_E("socket server make sockaddr_un filed!");
        return;
    }

    if (connect(fd, (struct sockaddr *) &my_addr, addrlen) == -1) {
        LOG_E("socket client connect filed!");
        return;
    }

    send(fd, "Hello from client", 17, 0);

    close(fd);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_pujh_socket_MainActivity_startNativeClientSocket(JNIEnv *env, jobject thiz) {

}

extern "C"
JNIEXPORT void JNICALL
Java_com_pujh_socket_MainActivity_stopNativeClientSocket(JNIEnv *env, jobject thiz) {

}