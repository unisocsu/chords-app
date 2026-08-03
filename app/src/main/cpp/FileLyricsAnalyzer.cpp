#include "FileLyricsAnalyzer.h"
#include <sstream>
#include <android/log.h>

// ... (שאר הקוד הקיים)

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
        // Whisper מאפשר לחלץ טוקנים/מילים בודדות בתוך הסגמנט
        const char *text = whisper_full_get_segment_text(s_whisper_ctx, i);
        int64_t t0 = whisper_full_get_segment_t0(s_whisper_ctx, i); // זמן התחלה במאית שנייה (centiseconds)
        int64_t t1 = whisper_full_get_segment_t1(s_whisper_ctx, i); // זמן סיום

        double startSec = (double)t0 / 100.0;
        double endSec = (double)t1 / 100.0;

        if (!first) jsonStream << ",";
        jsonStream << "{\"word\":\"" << text << "\",\"start\":" << startSec << ",\"end\":" << endSec << "}";
        first = false;
    }

    jsonStream << "]";
    return jsonStream.str();
}
