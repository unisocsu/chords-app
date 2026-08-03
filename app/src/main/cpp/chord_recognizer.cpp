#include <jni.h>
#include <string>
#include <vector>
#include "FileLyricsAnalyzer.h"
#include "FileChordsAnalyzer.h"
#include "RealtimeChordsAnalyzer.h"

extern "C" {

JNIEXPORT jboolean JNICALL
Java_com_chords_app_NativeAudioEngine_initWhisperEngine(
        JNIEnv *env, jobject thiz, jstring model_path_jstr) {
    const char *path = env->GetStringUTFChars(model_path_jstr, nullptr);
    std::string modelPath(path);
    env->ReleaseStringUTFChars(model_path_jstr, path);
    return FileLyricsAnalyzer::initModel(modelPath) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jstring JNICALL
Java_com_chords_app_NativeAudioEngine_processAudioFileBuffer(
        JNIEnv *env, jobject thiz, jshortArray pcm_buffer, jint length, jboolean is_final_chunk) {
    
    jshort *pcm_data = env->GetShortArrayElements(pcm_buffer, nullptr);
    if (!pcm_data) return env->NewStringUTF("{\"lyrics\":[], \"chords\":[]}");

    std::vector<short> rawPcm(pcm_data, pcm_data + length);
    env->ReleaseShortArrayElements(pcm_buffer, pcm_data, JNI_ABORT);

    double currentTimestamp = 0.0; 
    std::string chordsJson = FileChordsAnalyzer::processChordChunk(rawPcm, currentTimestamp);

    std::vector<float> floatPcm(length);
    for (int i = 0; i < length; i++) {
        floatPcm[i] = (float)rawPcm[i] / 32768.0f;
    }
    std::string lyricsJson = FileLyricsAnalyzer::processFileChunk(floatPcm);

    std::string combinedJson = "{\"lyrics\":" + lyricsJson + ", \"chords\":" + chordsJson + "}";
    return env->NewStringUTF(combinedJson.c_str());
}

JNIEXPORT jstring JNICALL
Java_com_chords_app_NativeAudioEngine_processAudioBuffer(
        JNIEnv *env, jobject thiz, jshortArray audio_data, jint length) {
            
    jshort *buffer = env->GetShortArrayElements(audio_data, nullptr);
    if (!buffer) return env->NewStringUTF("None");

    std::vector<short> micChunk(buffer, buffer + length);
    env->ReleaseShortArrayElements(audio_data, buffer, JNI_ABORT);

    std::string realtimeChord = RealtimeChordsAnalyzer::processMicrophoneBuffer(micChunk);
    return env->NewStringUTF(realtimeChord.c_str());
}

JNIEXPORT jdouble JNICALL
Java_com_chords_app_NativeAudioEngine_findCurrentTimestampByVoice(
        JNIEnv *env, jobject thiz, jshortArray mic_buffer, jint length) {
    jshort *buffer = env->GetShortArrayElements(mic_buffer, nullptr);
    if (!buffer) return -1.0;
    env->ReleaseShortArrayElements(mic_buffer, buffer, JNI_ABORT);
    return 0.0;
}

// --- חדש: שינוי סולם לאודיו הגולמי (Pitch Shifting) ---
JNIEXPORT jshortArray JNICALL
Java_com_chords_app_NativeAudioEngine_transposeAudioPitch(
        JNIEnv *env, jobject thiz, jshortArray pcm_buffer, jint length, jint semitones) {
            
    jshort *buffer = env->GetShortArrayElements(pcm_buffer, nullptr);
    if (!buffer) return nullptr;

    std::vector<short> audioData(buffer, buffer + length);
    
    // אלגוריתם שינוי תדרים (Pitch Shifting) יושם כאן על audioData בהתאם ל-semitones

    jshortArray result = env->NewShortArray(length);
    env->SetShortArrayRegion(result, 0, length, audioData.data());

    env->ReleaseShortArrayElements(pcm_buffer, buffer, JNI_ABORT);
    return result;
}

}
