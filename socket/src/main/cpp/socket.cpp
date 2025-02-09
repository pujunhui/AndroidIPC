//
// Created by 24415 on 2025-02-09.
//
#include "socket.h"

#include <unistd.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <sys/un.h>

/*
 * sun_path: null-terminated pathname
 * Unix socket有两种类型：文件系统socket和抽象命名空间socket。
 * 1、文件系统socket，sun_path指向一个实际的文件路径，比如/tmp/mysocket。
 *      这样创建的socket会在文件系统中生成一个文件，其他进程可以通过这个路径来连接。
 *      不过，这样可能会有一些问题，比如需要处理文件权限，或者如果文件已经存在的话，bind可能会失败，需要先删除文件。
 *      另外，即使socket关闭后，文件可能还会残留，需要手动清理。
 * 2、抽象命名空间的socket，这是Linux特有的。
 *      这时候sun_path的第一个字符是空字节（'\0'），后面的部分作为抽象名称。例如，sun_path设置为"\0my_socket"，这样不会在文件系统中创建实际的文件。
 *      抽象名称的作用域是内核的，进程结束后自动释放，不需要处理文件残留的问题，也避免了权限和路径冲突的问题。这种方式更安全、方便，但只能在Linux上使用。
 *
 * strncpy和strcpy的区别:
 *      strncpy会复制最多n个字符，如果源字符串长度小于n，则用空字符填充剩余空间；如果源字符串长度等于或超过n，则不会添加终止空字符。
 *      而strcpy会一直复制直到遇到源字符串的终止符，可能导致缓冲区溢出。
 */
int socket_make_sockaddr_un(const char *name, int namespaceId,
                            struct sockaddr_un *p_addr, socklen_t *alen) {

    memset(p_addr, 0, sizeof(struct sockaddr_un));
    size_t namelen = strlen(name);

    switch (namespaceId) {
        case SOCKET_NAMESPACE_ABSTRACT: //抽象命名空间的socket
            //Test with length +1 for the *initial* '\0'
            if ((namelen + 1) > sizeof(p_addr->sun_path)) {
                LOG_E("socket name is invalid.");
                return -1;
            }
            /**
             * Note: The path in this case is *not* supposed to be
             * '\0'-terminated. ("man 7 unix" for the gory details.)
             */
            p_addr->sun_path[0] = '\0';
            memcpy(p_addr->sun_path + 1, name, namelen);
            break;
        case SOCKET_NAMESPACE_FILESYSTEM: //文件系统socket
            //filesystem namespace path must be'\0'-terminated.
            if (namelen > sizeof(p_addr->sun_path) - 1) {
                LOG_E("socket name is invalid.");
                return -1;
            }
            strcpy(p_addr->sun_path, name);
            break;
        default:
            return -1;
    }

    p_addr->sun_family = AF_UNIX;
    *alen = namelen + offsetof(struct sockaddr_un, sun_path) + 1;
    return 0;
}