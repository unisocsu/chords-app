package com.chords.app;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.net.Uri;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class AudioDecoder {

    private static final String TAG = "AudioDecoder";
    private static final long TIMEOUT_US = 10000;

    public interface OnAudioChunkDecodedListener {
        void onChunkDecoded(short[] pcmBuffer, int length, boolean isFinalChunk);
    }

    public static void decodeAudioFile(Context context, Uri fileUri, OnAudioChunkDecodedListener listener) {
        MediaExtractor extractor = new MediaExtractor();
        MediaCodec decoder = null;

        try {
            extractor.setDataSource(context, fileUri, null);

            int trackIndex = selectAudioTrack(extractor);
            if (trackIndex < 0) {
                Log.e(TAG, "לא נמצא ערוץ שמע בקובץ");
                return;
            }

            extractor.selectTrack(trackIndex);
            MediaFormat format = extractor.getTrackFormat(trackIndex);
            String mime = format.getString(MediaFormat.KEY_MIME);

            decoder = MediaCodec.createDecoderByType(mime);
            decoder.configure(format, null, null, 0);
            decoder.start();

            ByteBuffer[] inputBuffers = decoder.getInputBuffers();
            ByteBuffer[] outputBuffers = decoder.getOutputBuffers();

            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            boolean isEOS = false;
            boolean isDecoderEOS = false;

            while (!isDecoderEOS) {
                if (!isEOS) {
                    int inputBufferIndex = decoder.dequeueInputBuffer(TIMEOUT_US);
                    if (inputBufferIndex >= 0) {
                        ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
                        int sampleSize = extractor.readSampleData(inputBuffer, 0);

                        if (sampleSize < 0) {
                            decoder.queueInputBuffer(inputBufferIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                            isEOS = true;
                        } else {
                            long presentationTimeUs = extractor.getSampleTime();
                            decoder.queueInputBuffer(inputBufferIndex, 0, sampleSize, presentationTimeUs, 0);
                            extractor.advance();
                        }
                    }
                }

                int outputBufferIndex = decoder.dequeueOutputBuffer(bufferInfo, TIMEOUT_US);
                if (outputBufferIndex >= 0) {
                    ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];

                    short[] pcmChunk = extractShortArrayFromBuffer(outputBuffer, bufferInfo);
                    boolean isFinal = (bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0;

                    if (pcmChunk != null && pcmChunk.length > 0) {
                        listener.onChunkDecoded(pcmChunk, pcmChunk.length, isFinal);
                    }

                    decoder.releaseOutputBuffer(outputBufferIndex, false);

                    if (isFinal) {
                        isDecoderEOS = true;
                    }
                } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                    outputBuffers = decoder.getOutputBuffers();
                }
            }

        } catch (IOException e) {
            Log.e(TAG, "שגיאה בפענוח קובץ השמע", e);
        } finally {
            if (decoder != null) {
                decoder.stop();
                decoder.release();
            }
            extractor.release();
        }
    }

    private static int selectAudioTrack(MediaExtractor extractor) {
        for (int i = 0; i < extractor.getTrackCount(); i++) {
            MediaFormat format = extractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (mime != null && mime.startsWith("audio/")) {
                return i;
            }
        }
        return -1;
    }

    private static short[] extractShortArrayFromBuffer(ByteBuffer buffer, MediaCodec.BufferInfo info) {
        if (info.size <= 0) return null;

        buffer.position(info.offset);
        buffer.limit(info.offset + info.size);

        ByteBuffer pcmBuffer = buffer.slice().order(ByteOrder.LITTLE_ENDIAN);
        short[] shortArray = new short[info.size / 2];
        pcmBuffer.asShortBuffer().get(shortArray);

        return shortArray;
    }
}
