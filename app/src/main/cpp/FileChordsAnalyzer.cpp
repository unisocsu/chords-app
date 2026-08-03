#include "FileChordsAnalyzer.h"
#include <sstream>

std::string FileChordsAnalyzer::processChordChunk(const std::vector<short>& pcmChunk, double currentTimestampSeconds) {
    if (pcmChunk.empty()) return "[]";

    // בהמשך כאן ירוץ חישוב ה-KissFFT המלא על המקטע
    std::string detectedChord = "C"; // דוגמה זמנית

    std::ostringstream jsonStream;
    jsonStream << "[";
    jsonStream << "{\"chord\":\"" << detectedChord << "\",\"time\":" << currentTimestampSeconds << "}";
    jsonStream << "]";

    return jsonStream.str();
}
