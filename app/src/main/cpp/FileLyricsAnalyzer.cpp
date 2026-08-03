#include "FileLyricsAnalyzer.h"
#include <sstream>
#include <android/log.h>

#define TAG "FileLyricsAnalyzer"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

struct whisper_context* FileLyricsAnalyzer::s_whisper_ctx = nullptr;

bool FileLyricsAnalyzer::initModel(const std::string& modelPath) {
    if (s_whisper_ctx) {
        whisper_free(s_whisper_ctx);
        s_whisper_ctx = nullptr;
    }

    s_whisper_ctx = whisper_init_from_file(modelPath.c_str());
    if (!s_whisper_ctx) {
        LOGE("שגיאה בטעינת מודל Whisper: %s", modelPath.c_str());
        return false;
    }
    return true;
}

std::string FileLyricsAnalyzer::processFileChunk(const std::vector<float>& floatPcm) {
    if (!s_whisper_ctx) return "[]";

    whisper_full_params params = whisper_full_default_params(WHISPER_SAMPLING_GREEDY);
    params.print_realtime   = false;
    params.print_progress   = false;
    params.print_timestamps = true;
    params.language         = "he";

    if (whisper_full(s_whisper_ctx, params, floatPcm.data(), floatPcm.size()) != 0) {
        return "[]";
    }

    std::ostringstream jsonStream;
    jsonStream << "[";

    int n_segments = whisper_full_n_segments(s_whisper_ctx);
    bool first = true;

    for (int i = 0; i < n_segments; ++i) {
        const char *text = whisper_full_get_segment_text(s_whisper_ctx, i);
        int64_t t0 = whisper_full_get_segment_t0(s_whisper_ctx, i);
        int64_t t1 = whisper_full_get_segment_t1(s_whisper_ctx, i);

        double startSec = (double)t0 / 100.0;
        double endSec = (double)t1 / 100.0;

        if (!text) continue;

        if (!first) jsonStream << ",";
        jsonStream << "{\"word\":\"" << text << "\",\"start\":" << startSec << ",\"end\":" << endSec << "}";
        first = false;
    }

    jsonStream << "]";
    return jsonStream.str();
}

void FileLyricsAnalyzer::release() {
    if (s_whisper_ctx) {
        whisper_free(s_whisper_ctx);
        s_whisper_ctx = nullptr;
    }
}
