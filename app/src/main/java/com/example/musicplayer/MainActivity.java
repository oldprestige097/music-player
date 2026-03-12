package com.example.musicplayer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.musicplayer.databinding.ActivityMainBinding;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_MEDIA_PERMISSION = 101;

    private ActivityMainBinding binding;
    private MediaPlayer mediaPlayer;
    private final List<Song> songs = new ArrayList<>();
    private int currentIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupButtons();
        checkPermissionAndLoadSongs();
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.player_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull android.view.MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_about) {
            Toast.makeText(this, getString(R.string.about_text), Toast.LENGTH_LONG).show();
            return true;
        } else if (itemId == R.id.action_refresh) {
            loadSongsFromDevice();
            return true;
        } else if (itemId == R.id.action_stop) {
            stopSong();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupButtons() {
        binding.playButton.setOnClickListener(v -> playCurrentSong());
        binding.pauseButton.setOnClickListener(v -> pauseSong());
        binding.stopButton.setOnClickListener(v -> stopSong());
        binding.nextButton.setOnClickListener(v -> nextSong(false));
        binding.prevButton.setOnClickListener(v -> previousSong());
    }

    private void checkPermissionAndLoadSongs() {
        String permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_AUDIO;
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            loadSongsFromDevice();
        } else {
            requestPermissions(new String[]{permission}, REQUEST_MEDIA_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_MEDIA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadSongsFromDevice();
            } else {
                Toast.makeText(this, getString(R.string.permission_required), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void loadSongsFromDevice() {
        songs.clear();

        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.ARTIST
        };

        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";

        try (android.database.Cursor cursor = getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                MediaStore.Audio.Media.DATE_ADDED + " DESC"
        )) {
            if (cursor != null) {
                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
                int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME);
                int artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);

                while (cursor.moveToNext()) {
                    long id = cursor.getLong(idColumn);
                    String name = cursor.getString(nameColumn);
                    String artist = cursor.getString(artistColumn);

                    if (name == null) {
                        name = getString(R.string.unknown_song);
                    }
                    if (artist == null) {
                        artist = getString(R.string.unknown_artist);
                    }

                    Uri songUri = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, String.valueOf(id));
                    songs.add(new Song(name, artist, songUri));
                }
            }
        }

        currentIndex = 0;
        updateSongInfo();

        if (songs.isEmpty()) {
            Toast.makeText(this, getString(R.string.no_songs_found), Toast.LENGTH_LONG).show();
        }
    }

    private void playCurrentSong() {
        if (songs.isEmpty()) {
            Toast.makeText(this, getString(R.string.no_songs_found), Toast.LENGTH_SHORT).show();
            return;
        }

        Song selectedSong = songs.get(currentIndex);

        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioAttributes(
                    new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
            );

            try {
                mediaPlayer.setDataSource(this, selectedSong.getUri());
                mediaPlayer.prepare();
                mediaPlayer.setOnCompletionListener(mp -> nextSong(true));
            } catch (IOException e) {
                Toast.makeText(this, getString(R.string.playback_error), Toast.LENGTH_SHORT).show();
                releasePlayer();
                return;
            }
        }

        mediaPlayer.start();
        updateSongInfo();
    }

    private void pauseSong() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            Toast.makeText(this, getString(R.string.paused), Toast.LENGTH_SHORT).show();
        }
    }

    private void stopSong() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            releasePlayer();
            Toast.makeText(this, getString(R.string.stopped), Toast.LENGTH_SHORT).show();
        }
    }

    private void nextSong(boolean autoPlay) {
        if (songs.isEmpty()) {
            return;
        }
        stopSong();
        currentIndex = (currentIndex + 1) % songs.size();
        updateSongInfo();
        if (autoPlay) {
            playCurrentSong();
        }
    }

    private void previousSong() {
        if (songs.isEmpty()) {
            return;
        }
        stopSong();
        if (currentIndex == 0) {
            currentIndex = songs.size() - 1;
        } else {
            currentIndex = currentIndex - 1;
        }
        updateSongInfo();
    }

    private void updateSongInfo() {
        if (songs.isEmpty()) {
            binding.songTitle.setText(getString(R.string.no_song_loaded));
            binding.songArtist.setText(getString(R.string.add_audio_files_hint));
            return;
        }

        Song selectedSong = songs.get(currentIndex);
        binding.songTitle.setText(selectedSong.getName());
        binding.songArtist.setText(selectedSong.getArtist());
    }

    private void releasePlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayer();
    }
}
