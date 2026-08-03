package com.chords.app;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class MediaLibraryActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayList<String> songNamesList;
    private ArrayList<Uri> songUrisList;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_library);

        listView = findViewById(R.id.listViewLibrarySongs);
        SearchView searchView = findViewById(R.id.searchViewSongs);

        songNamesList = new ArrayList<>();
        songUrisList = new ArrayList<>();

        // טעינת שירים מהזיכרון של המכשיר
        loadDeviceAudioFiles();

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, songNamesList);
        listView.setAdapter(adapter);

        // ניהול חיפוש
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // אפשר להוסיף כאן סינון לרשימה לפי חיפוש
                return true;
            }
        });

        // בחירת שיר מהרשימה
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Uri selectedUri = songUrisList.get(position);
            Intent intent = new Intent(MediaLibraryActivity.this, PlayerActivity.class);
            intent.putExtra("AUDIO_URI", selectedUri.toString());
            startActivity(intent);
        });
    }

    private void loadDeviceAudioFiles() {
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST
        };

        Cursor cursor = getContentResolver().query(uri, projection, MediaStore.Audio.Media.IS_MUSIC + " != 0", null, null);
        if (cursor != null) {
            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
            int titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
            int artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);

            while (cursor.moveToNext()) {
                long id = cursor.getLong(idColumn);
                String title = cursor.getString(titleColumn);
                String artist = cursor.getString(artistColumn);

                Uri contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
                songNamesList.add(title + " - " + (artist != null ? artist : "לא ידוע"));
                songUrisList.add(contentUri);
            }
            cursor.close();
        } else {
            Toast.makeText(this, "לא נמצאו קבצי שמע במכשיר", Toast.LENGTH_SHORT).show();
        }
    }
}
