#ifndef REALTIME_CHORDS_ANALYZER_H
#define REALTIME_CHORDS_ANALYZER_H

#include <vector>
#include <string>
#include "kiss_fft.h"

class RealtimeChordsAnalyzer {
public:
    static std::string processMicrophoneBuffer(const std::vector<short>& audioBuffer);
};

#endif // REALTIME_CHORDS_ANALYZER_H
