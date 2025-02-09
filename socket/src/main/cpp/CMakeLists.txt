cmake_minimum_required(VERSION 3.22.1)

project("socket")

#socket server
add_library(server SHARED
        socket.cpp
        server.cpp)

target_link_libraries(server
        android
        log)

#socket client
add_library(client SHARED
        socket.cpp
        client.cpp)

target_link_libraries(client
        android
        log)