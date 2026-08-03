#include <jni.h>
#include <cmath>
#include <vector>
#include <string>
#include <algorithm>
#include <android/log.h>

#define LOG_TAG "ChordRecognizerNative"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

constexpr int SAMPLE_RATE = 44100;
constexpr int MAX_BUFFER_SIZE = 4096;
constexpr int NUM_PITCH_CLASSES = 12;

namespace {
    // Pre-allocated static memory buffers to guarantee 0MB runtime allocation
    float g_time_domain[MAX_BUFFER_SIZE];
    float g_real_spectrum[MAX_BUFFER_SIZE];
    float g_imag_spectrum[MAX_BUFFER_SIZE];
    float g_magnitude[MAX_BUFFER_SIZE / 2];
    float g_chroma[NUM_PITCH_CLASSES];

    const char* NOTE_NAMES[NUM_PITCH_CLASSES] = {
        "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"
    };

    void compute_dft(const float* in, float* out_real, float* out_imag, int N) {
        int num_bins = N / 2;
        for (int k = 0; k < num_bins; k++) {
            float real_acc = 0.0f;
            float imag_acc = 0.0f;
            for (int n = 0; n < N; n++) {
                float angle = (2.0f * static_cast<float>(M_PI) * k * n) / N;
                real_acc += in[n] * std::cos(angle);
                imag_acc -= in[n] * std::sin(angle);
            }
            out_real[k] = real_acc;
            out_imag[k] = imag_acc;
        }
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

    // Direct Primitive Access with Critical Lock
    jshort *buffer = static_cast<jshort*>(env->GetPrimitiveArrayCritical(audio_data, nullptr));
    if (buffer == nullptr) {
        return env->NewStringUTF("");
    }

    // Windowing & Normalization
    for (int i = 0; i < length; i++) {
        float sample = static_cast<float>(buffer[i]) / 32768.0f;
        float window = 0.5f * (1.0f - std::cos((2.0f * static_cast<float>(M_PI) * i) / (length - 1)));
        g_time_domain[i] = sample * window;
    }

    // Release Critical Array Lock immediately
    env->ReleasePrimitiveArrayCritical(audio_data, buffer, JNI_ABORT);

    // DSP Spectrum Analysis
    compute_dft(g_time_domain, g_real_spectrum, g_imag_spectrum, length);

    int num_bins = length / 2;
    for (int k = 0; k < num_bins; k++) {
        g_magnitude[k] = std::sqrt(g_real_spectrum[k] * g_real_spectrum[k] + 
                                   g_imag_spectrum[k] * g_imag_spectrum[k]);
    }

    extract_chroma(g_magnitude, length, SAMPLE_RATE);

    std::string chord = match_chord_template();

    return env->NewStringUTF(chord.c_str());
}
