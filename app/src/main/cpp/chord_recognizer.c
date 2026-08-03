#include <jni.h>
#include <math.h>
#include <android/log.h>

#define LOG_TAG "ChordRecognizer"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

JNIEXPORT jstring JNICALL
Java_com_chords_app_NativeAudioEngine_processAudioBuffer(JNIEnv *env, jobject thiz, jshortArray buffer, jint sampleRate) {
    jshort *audioData = (*env)->GetShortArrayElements(env, buffer, NULL);
    jsize length = (*env)->GetArrayLength(env, buffer);

    LOGI("Processing %d audio samples at sample rate %d 🎧⚡", length, sampleRate);

    // כאן יושבת לוגיקת עיבוד האותות וניתוח התדרים (FFT)
    
    (*env)->ReleaseShortArrayElements(env, buffer, audioData, JNI_ABORT);

    return (*env)->NewStringUTF(env, "A Major 🎶");
}
