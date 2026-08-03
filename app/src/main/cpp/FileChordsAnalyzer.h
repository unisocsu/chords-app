#ifndef FILE_CHORDS_ANALYZER_H
#define FILE_CHORDS_ANALYZER_H

#include <vector>
#include <string>
#include "kiss_fft.h"

class FileChordsAnalyzer {
public:
    // ניתוח מקטע PCM של שיר שלם לצורך זיהוי אקורדים (FFT + Chroma)
    static std::string processChordChunk(const std::vector<short>& pcmChunk);
};

#endif // FILE_CHORDS_ANALYZER_H
