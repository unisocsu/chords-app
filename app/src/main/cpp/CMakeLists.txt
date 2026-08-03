cmake_minimum_required(VERSION 3.22.1)

project("chord_recognizer")

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# 1. Include KissFFT headers from third_party
include_directories(
        ${CMAKE_CURRENT_SOURCE_DIR}/third_party/kissfft
)

# 2. Add Native source files + KissFFT implementation
add_library(
        chord_recognizer
        SHARED
        chord_recognizer.cpp
        third_party/kissfft/kiss_fft.c
)

# 3. Find NDK logging library
find_library(
        log-lib
        log
)

# 4. Link libraries
target_link_libraries(
        chord_recognizer
        ${log-lib}
)
