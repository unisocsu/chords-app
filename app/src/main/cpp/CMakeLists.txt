cmake_minimum_required(VERSION 3.22.1)
project("chord_recognizer")

# הגדרת ספרית הנייטיב שלנו מבוססת קובץ ה-C הטהור
add_library(
        chord_recognizer
        SHARED
        chord_recognizer.c
)

# חיבור ללוגים של אנדרואיד (Android Log) כדי שנוכל להדפיס פריטים מתוך קוד ה-C ל-Logcat
find_library(
        log-lib
        log
)

target_link_libraries(
        chord_recognizer
        ${log-lib}
)
