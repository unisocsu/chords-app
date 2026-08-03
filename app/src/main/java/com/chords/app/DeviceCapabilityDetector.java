package com.chords.app;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;

public class DeviceCapabilityDetector {

    public enum EnginePerformanceMode {
        LOW_END,     // Realtime + Basic File Chords (RAM < 3GB)
        MID_RANGE,   // Realtime + File Chords + Whisper Tiny/Base (RAM 3GB-6GB)
        HIGH_END     // Full Pipeline: Realtime + File + Whisper Small (RAM > 6GB)
    }

    public static EnginePerformanceMode detectPerformanceMode(Context context) {
        ActivityManager actManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        
        if (actManager != null) {
            actManager.getMemoryInfo(memInfo);
        }

        double totalRamGB = memInfo.totalMem / (1024.0 * 1024.0 * 1024.0);
        boolean is64Bit = false;

        for (String abi : Build.SUPPORTED_ABIS) {
            if ("arm64-v8a".equals(abi)) {
                is64Bit = true;
                break;
            }
        }

        if (totalRamGB >= 6.0 && is64Bit) {
            return EnginePerformanceMode.HIGH_END;
        } else if (totalRamGB >= 3.0) {
            return EnginePerformanceMode.MID_RANGE;
        } else {
            return EnginePerformanceMode.LOW_END;
        }
    }
}
