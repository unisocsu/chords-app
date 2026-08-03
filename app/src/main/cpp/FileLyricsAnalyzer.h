#ifndef FILE_LYRICS_ANALYZER_H
#define FILE_LYRICS_ANALYZER_H

#include <vector>
#include <string>
#include "whisper.h"

class FileLyricsAnalyzer {
public:
    static bool initModel(const std::string& modelPath);
    static std::string processFileChunk(const std::vector<float>& floatPcm);
    static void release();

private:
    static struct whisper_context* s_whisper_ctx;
};

#endif // FILE_LYRICS_ANALYZER_H
