#include "FileLyricsAnalyzer.h"
#include <android/log.h>

#define TAG "FileLyricsAnalyzer"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

struct whisper_context* FileLyricsAnalyzer::s_whisper_ctx = nullptr;

bool FileLyricsAnalyzer::initModel(const std::string& modelPath) {
    if (s_whisper_ctx) {
        whisper_free(s_whisper_ctx);
        s_whisper_ctx = nullptr;
    }

    s_whisper_ctx = whisper_init_from_file(modelPath.c_str());
    if (!s_whisper_ctx) {
        LOGE("שגיאה בטעינת מודל Whisper עבור קובץ: %s", modelPath.c_str());
        return false;
    }
    LOGD("מודל ה-Whisper לעיבוד קבצים נטען בהצלחה!");
    return true;
}

std::string FileLyricsAnalyzer::processFileChunk(const std::vector<float>& floatPcm) {
    if (!s_whisper_ctx) return "";

    whisper_full_params params = whisper_full_default_params(WHISPER_SAMPLING_GREEDY);
    params.print_realtime   = false;
    params.print_progress   = false;
    params.print_timestamps = true;
    params.language         = "he";

    if (whisper_full(s_whisper_ctx, params, floatPcm.data(), floatPcm.size()) != 0) {
        LOGE("שגיאה בהרצת Whisper על מקטע מהקובץ");
        return "";
    }

    // כאן אפשר לחלץ את הטקסט שזוהה מהמודל
    int n_segments = whisper_full_n_segments(s_whisper_ctx);
    std::string resultText = "";
    for (int i = 0; i < n_segments; ++i) {
        const char *text = whisper_full_get_segment_text(s_whisper_ctx, i);
        resultText += std::string(text) + " ";
    }

    return resultText;
}

void FileLyricsAnalyzer::release() {
    if (s_whisper_ctx) {
        whisper_free(s_whisper_ctx);
        s_whisper_ctx = nullptr;
    }
}
