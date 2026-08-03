package com.chords.app;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class SongZipExporter {

    public static void exportSongToZip(Song song, File destZipFile) throws Exception {
        File parentDir = destZipFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                throw new IOException("Failed to create destination directories: " + parentDir.getAbsolutePath());
            }
        }

        String jsonContent = JsonSongSerializer.serializeSong(song);

        try (ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(destZipFile)))) {
            
            ZipEntry jsonEntry = new ZipEntry("song.json");
            zos.putNextEntry(jsonEntry);
            byte[] jsonBytes = jsonContent.getBytes(StandardCharsets.UTF_8);
            zos.write(jsonBytes, 0, jsonBytes.length);
            zos.closeEntry();

            if (song.getAlbumArtPath() != null && !song.getAlbumArtPath().isEmpty()) {
                File artFile = new File(song.getAlbumArtPath());
                if (artFile.exists() && artFile.isFile()) {
                    addFileToZip(zos, artFile, artFile.getName());
                }
            }

            if (song.getAudioPath() != null && !song.getAudioPath().isEmpty()) {
                File audioFile = new File(song.getAudioPath());
                if (audioFile.exists() && audioFile.isFile()) {
                    addFileToZip(zos, audioFile, audioFile.getName());
                }
            }
        }
    }

    private static void addFileToZip(ZipOutputStream zos, File sourceFile, String entryName) throws IOException {
        ZipEntry entry = new ZipEntry(entryName);
        zos.putNextEntry(entry);

        byte[] buffer = new byte[8192];
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(sourceFile))) {
            int count;
            while ((count = bis.read(buffer)) != -1) {
                zos.write(buffer, 0, count);
            }
        }
        zos.closeEntry();
    }
}
