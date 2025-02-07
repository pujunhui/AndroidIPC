//
// Created by 24415 on 2025-02-07.
//

#include "socket.h"

#include <sys/types.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <unistd.h>
#include <jni.h>

void startClientSocket() {
    int fd;
    struct sockaddr_un my_addr;
    socklen_t peer_addr_size;

    fd = socket(AF_UNIX, SOCK_STREAM, 0);
    if (fd == -1) {
        return;
    }

    memset(&my_addr, 0, sizeof(struct sockaddr_un));
    my_addr.sun_family = AF_UNIX;
    strncpy(my_addr.sun_path, SOCKET_PATH, sizeof(my_addr.sun_path) - 1);

    if (connect(fd, (struct sockaddr *) &my_addr, sizeof(struct sockaddr_un)) == -1) {
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