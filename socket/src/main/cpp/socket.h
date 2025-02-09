//
// Created by 24415 on 2025-02-07.
//

#ifndef IPC_SOCKET_H
#define IPC_SOCKET_H

#include <android/log.h>
#include <sys/types.h>

#ifndef LOG_TAG
#define LOG_TAG "socket"
#endif

#define LOG_D(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOG_E(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

#define SOCKET_PATH "/somepath"


#define SOCKET_NAMESPACE_ABSTRACT 1
#define SOCKET_NAMESPACE_FILESYSTEM 2

int socket_make_sockaddr_un(const char *name, int namespaceId,
                            struct sockaddr_un *p_addr, socklen_t *alen);

/** Returns the lesser of its two arguments. */
#define MIN(a, b) (((a)<(b))?(a):(b))
/** Returns the greater of its two arguments. */
#define MAX(a, b) (((a)>(b))?(a):(b))

#endif //IPC_SOCKET_H
