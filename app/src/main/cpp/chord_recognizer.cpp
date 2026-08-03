#include <jni.h>
#include <cmath>
#include <vector>
#include <string>
#include <algorithm>
#include <android/log.h>

// Include KissFFT header
#include "kiss_fft.h"

#define LOG_TAG "ChordRecognizerNative"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

constexpr int SAMPLE_RATE = 44100;
constexpr int MAX_BUFFER_SIZE = 4096;
constexpr int NUM_PITCH_CLASSES = 12;

namespace {
    // Static pre-allocated buffers
    float g_time_domain[MAX_BUFFER_SIZE];
    kiss_fft_cpx g_fft_in[MAX_BUFFER_SIZE];
    kiss_fft_cpx g_fft_out[MAX_BUFFER_SIZE];
    float g_magnitude[MAX_BUFFER_SIZE / 2];
    float g_chroma[NUM_PITCH_CLASSES];

    // Reusable KissFFT State Configuration
    kiss_fft_cfg g_fft_cfg = nullptr;
    int g_last_fft_size = 0;

    const char* NOTE_NAMES[NUM_PITCH_CLASSES] = {
        "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"
    };

    void run_kiss_fft(int N) {
        // Allocate or reuse KissFFT plan only if buffer size changes
        if (g_fft_cfg == nullptr || g_last_fft_size != N) {
            if (g_fft_cfg != nullptr) {
                free(g_fft_cfg);
            }
            g_fft_cfg = kiss_fft_alloc(N, 0, nullptr, nullptr);
            g_last_fft_size = N;
        }

        // Fast Fourier Transform execution
        kiss_fft(g_fft_cfg, g_fft_in, g_fft_out);
    }

    int frequency_to_pitch_class(float freq) {
        if (freq < 65.0f || freq > 2000.0f) return -1;
        float midi_note = 69.0f + 12.0f * std::log2(freq / 440.0f);
        int note_index = static_cast<int>(std::round(midi_note)) % 12;
        if (note_index < 0) note_index += 12;
        return note_index;
    }

    void extract_chroma(const float* magnitude, int N, int sample_rate) {
        std::fill(std::begin(g_chroma), std::end(g_chroma), 0.0f);
        int num_bins = N / 2;
        float bin_resolution = static_cast<float>(sample_rate) / static_cast<float>(N);

        for (int k = 1; k < num_bins; k++) {
            float freq = k * bin_resolution;
            int pitch_class = frequency_to_pitch_class(freq);
            if (pitch_class >= 0 && pitch_class < NUM_PITCH_CLASSES) {
                g_chroma[pitch_class] += magnitude[k];
            }
        }
    }

    std::string match_chord_template() {
        float max_energy = 0.0f;
        int best_root = 0;
        bool is_minor = false;

        for (int root = 0; root < NUM_PITCH_CLASSES; root++) {
            // Major Triad Template
            int maj_3rd = (root + 4) % 12;
            int p5th = (root + 7) % 12;
            float maj_score = g_chroma[root] + g_chroma[maj_3rd] + g_chroma[p5th];

            if (maj_score > max_energy) {
                max_energy = maj_score;
                best_root = root;
                is_minor = false;
            }

            // Minor Triad Template
            int min_3rd = (root + 3) % 12;
            float min_score = g_chroma[root] + g_chroma[min_3rd] + g_chroma[p5th];

            if (min_score > max_energy) {
                max_energy = min_score;
                best_root = root;
                is_minor = true;
            }
        }

        if (max_energy < 10.0f) {
            return "";
        }

        std::string result = NOTE_NAMES[best_root];
        if (is_minor) {
            result += "m";
        }
        return result;
    }
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_chords_app_NativeAudioEngine_processAudioBuffer(
        JNIEnv *env,
        jobject thiz,
        jshortArray audio_data,
        jint length) {

    if (length > MAX_BUFFER_SIZE) {
        length = MAX_BUFFER_SIZE;
    }

    jshort *buffer = static_cast<jshort*>(env->GetPrimitiveArrayCritical(audio_data, nullptr));
    if (buffer == nullptr) {
        return env->NewStringUTF("");
    }

    // Windowing & KissFFT Complex Array Preparation
    for (int i = 0; i < length; i++) {
        float sample = static_cast<float>(buffer[i]) / 32768.0f;
        float window = 0.5f * (1.0f - std::cos((2.0f * static_cast<float>(M_PI) * i) / (length - 1)));
        float windowed_sample = sample * window;
        
        g_fft_in[i].r = windowed_sample;
        g_fft_in[i].i = 0.0f;
    }

    env->ReleasePrimitiveArrayCritical(audio_data, buffer, JNI_ABORT);

    // Compute FFT via KissFFT
    run_kiss_fft(length);

    // Compute Spectral Magnitudes from Complex Output
    int num_bins = length / 2;
    for (int k = 0; k < num_bins; k++) {
        float r = g_fft_out[k].r;
        float i = g_fft_out[k].i;
        g_magnitude[k] = std::sqrt(r * r + i * i);
    }

    extract_chroma(g_magnitude, length, SAMPLE_RATE);

    std::string chord = match_chord_template();

    return env->NewStringUTF(chord.c_str());
}
