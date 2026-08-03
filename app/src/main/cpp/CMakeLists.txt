cmake_minimum_required(VERSION 3.22.1)

project("chord_recognizer")

# Set C++ Standard to C++17
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Add native library compiled from C++
add_library(
        chord_recognizer
        SHARED
        chord_recognizer.cpp
)

# Find Android NDK logging library
find_library(
        log-lib
        log
)

# Link log library
target_link_libraries(
        chord_recognizer
        ${log-lib}
)
