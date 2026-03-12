package com.example.musicplayer

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.musicplayer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var mediaPlayer: MediaPlayer? = null
    private val songs = mutableListOf<Song>()
    private var currentIndex = 0

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                loadSongsFromDevice()
            } else {
                Toast.makeText(this, getString(R.string.permission_required), Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupButtons()
        checkPermissionAndLoadSongs()
    }

    override fun onCreateOptionsMenu(menu: android.view.Menu): Boolean {
        menuInflater.inflate(R.menu.player_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_about -> {
                Toast.makeText(this, getString(R.string.about_text), Toast.LENGTH_LONG).show()
                true
            }

            R.id.action_refresh -> {
                loadSongsFromDevice()
                true
            }

            R.id.action_stop -> {
                stopSong()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupButtons() {
        binding.playButton.setOnClickListener { playCurrentSong() }
        binding.pauseButton.setOnClickListener { pauseSong() }
        binding.stopButton.setOnClickListener { stopSong() }
        binding.nextButton.setOnClickListener { nextSong() }
        binding.prevButton.setOnClickListener { previousSong() }
    }

    private fun checkPermissionAndLoadSongs() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        when {
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED -> {
                loadSongsFromDevice()
            }

            else -> requestPermissionLauncher.launch(permission)
        }
    }

    private fun loadSongsFromDevice() {
        songs.clear()

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.ARTIST
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"

        contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            "${MediaStore.Audio.Media.DATE_ADDED} DESC"
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn) ?: getString(R.string.unknown_song)
                val artist = cursor.getString(artistColumn) ?: getString(R.string.unknown_artist)
                val songUri = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id.toString())
                songs.add(Song(name = name, artist = artist, uri = songUri))
            }
        }

        currentIndex = 0
        updateSongInfo()

        if (songs.isEmpty()) {
            Toast.makeText(this, getString(R.string.no_songs_found), Toast.LENGTH_LONG).show()
        }
    }

    private fun playCurrentSong() {
        if (songs.isEmpty()) {
            Toast.makeText(this, getString(R.string.no_songs_found), Toast.LENGTH_SHORT).show()
            return
        }

        val selectedSong = songs[currentIndex]
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(this@MainActivity, selectedSong.uri)
                prepare()
                setOnCompletionListener { nextSong(autoPlay = true) }
            }
        }
        mediaPlayer?.start()
        updateSongInfo()
    }

    private fun pauseSong() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
            Toast.makeText(this, getString(R.string.paused), Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopSong() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        Toast.makeText(this, getString(R.string.stopped), Toast.LENGTH_SHORT).show()
    }

    private fun nextSong(autoPlay: Boolean = false) {
        if (songs.isEmpty()) return
        stopSong()
        currentIndex = (currentIndex + 1) % songs.size
        updateSongInfo()
        if (autoPlay) playCurrentSong()
    }

    private fun previousSong() {
        if (songs.isEmpty()) return
        stopSong()
        currentIndex = if (currentIndex == 0) songs.lastIndex else currentIndex - 1
        updateSongInfo()
    }

    private fun updateSongInfo() {
        if (songs.isEmpty()) {
            binding.songTitle.text = getString(R.string.no_song_loaded)
            binding.songArtist.text = getString(R.string.add_audio_files_hint)
            return
        }

        val selectedSong = songs[currentIndex]
        binding.songTitle.text = selectedSong.name
        binding.songArtist.text = selectedSong.artist
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
