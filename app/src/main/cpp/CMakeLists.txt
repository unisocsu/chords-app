cmake_minimum_required(VERSION 3.22.1)

project("chord_recognizer")

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# הגדרת נתיבי התיקיות החיצוניות (Third Party)
set(THIRD_PARTY_DIR ${CMAKE_CURRENT_SOURCE_DIR}/third_party)

include_directories(
        ${THIRD_PARTY_DIR}/kissfft
        ${THIRD_PARTY_DIR}/whisper.cpp
        ${THIRD_PARTY_DIR}/whisper.cpp/ggml/include
        ${THIRD_PARTY_DIR}/whisper.cpp/ggml/src
)

# הגדרת קבצי הקוד של הפרויקט ושל הספריות החיצוניות
add_library(
        chord_recognizer
        SHARED
        chord_recognizer.cpp
        # קבצי KissFFT
        ${THIRD_PARTY_DIR}/kissfft/kiss_fft.c
        # קבצי הליבה של Whisper.cpp ו-GGML
        ${THIRD_PARTY_DIR}/whisper.cpp/whisper.cpp
        ${THIRD_PARTY_DIR}/whisper.cpp/ggml/src/ggml.c
        ${THIRD_PARTY_DIR}/whisper.cpp/ggml/src/ggml-alloc.c
        ${THIRD_PARTY_DIR}/whisper.cpp/ggml/src/ggml-backend.c
        ${THIRD_PARTY_DIR}/whisper.cpp/ggml/src/ggml-quants.c
)

# הגדרת פלאגים לדחיסה ואופטימיזציה למעבדי נייד (ARM NEON)
if(ANDROID)
    target_compile_definitions(chord_recognizer PRIVATE GGML_USE_ACCELERATE=0)
    # הפעלת תמיכה ב-NEON אם המעבדים תומכים בכך (תקף לכל מנועי המובייל המודרניים)
    if(CMAKE_ANDROID_ARCH_ABI STREQUAL "arm64-v8a" OR CMAKE_ANDROID_ARCH_ABI STREQUAL "armeabi-v7a")
        target_compile_options(chord_recognizer PRIVATE -mfpu=neon -O3)
    endif()
endif()

find_library(
        log-lib
        log
)

target_link_libraries(
        chord_recognizer
        ${log-lib}
)
