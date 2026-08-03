#include "FileChordsAnalyzer.h"
#include <sstream>

std::string FileChordsAnalyzer::processChordChunk(const std::vector<short>& pcmChunk, double currentTimestampSeconds) {
    if (pcmChunk.empty()) return "[]";

    // כאן נבצע את חישוב ה-KissFFT על המקטע הנוכחי
    // ונחליט איזה אקורד מנוגן ברגע הזה (למשל C)
    std::string detectedChord = "C"; // דוגמה

    std::ostringstream jsonStream;
    jsonStream << "[";
    jsonStream << "{\"chord\":\"" << detectedChord << "\",\"time\":" << currentTimestampSeconds << "}";
    jsonStream << "]";

    return jsonStream.str();
}
