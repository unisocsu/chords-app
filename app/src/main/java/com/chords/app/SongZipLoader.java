package com.chords.app;

import android.content.Context;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class SongZipLoader {

    public static Song loadSongFromZip(Context context, File zipFile) throws Exception {
        File outputDir = new File(context.getFilesDir(), "extracted_songs/" + System.currentTimeMillis());
        if (!outputDir.exists() && !outputDir.mkdirs()) {
            throw new IOException("Failed to create target directory: " + outputDir.getAbsolutePath());
        }

        unzipArchive(zipFile, outputDir);

        File jsonFile = new File(outputDir, "song.json");
        if (!jsonFile.exists()) {
            throw new IOException("song.json not found inside the ZIP archive");
        }

        String jsonContent = readFileToString(jsonFile);
        Song song = JsonSongParser.parseSong(jsonContent);

        if (!song.getAlbumArtPath().isEmpty()) {
            File artFile = new File(outputDir, song.getAlbumArtPath());
            if (artFile.exists()) {
                song.setAlbumArtPath(artFile.getAbsolutePath());
            }
        }

        if (!song.getAudioPath().isEmpty()) {
            File audioFile = new File(outputDir, song.getAudioPath());
            if (audioFile.exists()) {
                song.setAudioPath(audioFile.getAbsolutePath());
            }
        }

        return song;
    }

    private static void unzipArchive(File zipFile, File targetDirectory) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile)))) {
            ZipEntry entry;
            byte[] buffer = new byte[8192];

            while ((entry = zis.getNextEntry()) != null) {
                File file = new File(targetDirectory, entry.getName());
                
                String canonicalDestinationPath = targetDirectory.getCanonicalPath();
                String canonicalFilePath = file.getCanonicalPath();
                if (!canonicalFilePath.startsWith(canonicalDestinationPath)) {
                    throw new SecurityException("Zip entry is outside target directory: " + entry.getName());
                }

                if (entry.isDirectory()) {
                    file.mkdirs();
                } else {
                    File parent = file.getParentFile();
                    if (parent != null && !parent.exists()) {
                        parent.mkdirs();
                    }

                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        int count;
                        while ((count = zis.read(buffer)) != -1) {
                            fos.write(buffer, 0, count);
                        }
                    }
                }
                zis.closeEntry();
            }
        }
    }

    private static String readFileToString(File file) throws IOException {
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append("\n");
            }
        }
        return builder.toString();
    }
}
