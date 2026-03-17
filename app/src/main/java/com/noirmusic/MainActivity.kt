package com.noirmusic

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.noirmusic.adapter.SongAdapter
import com.noirmusic.model.Song
import com.noirmusic.repository.MusicRepository
import com.noirmusic.service.MusicService
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), MusicService.MusicServiceListener {

    private lateinit var songRecyclerView: RecyclerView
    private lateinit var playPauseButton: ImageButton
    private lateinit var nextButton: ImageButton
    private lateinit var previousButton: ImageButton
    private lateinit var songTitleTextView: TextView
    private lateinit var artistTextView: TextView
    private lateinit var progressSeekBar: SeekBar
    private lateinit var emptyState: LinearLayout
    private lateinit var sleepTimerButton: ImageButton

    private var musicService: MusicService? = null
    private var serviceBound = false
    private val songs = mutableListOf<Song>()
    private lateinit var musicRepository: MusicRepository
    private var sleepTimerHandler: Handler? = null
    private var sleepTimerRunnable: Runnable? = null

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicService.MusicBinder
            musicService = binder.getService()
            serviceBound = true
            musicService?.setListener(this@MainActivity)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            serviceBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        musicRepository = MusicRepository(this)
        initializeViews()
        setupClickListeners()
        loadSongs()
        bindMusicService()
    }

    private fun initializeViews() {
        songRecyclerView = findViewById(R.id.songRecyclerView)
        playPauseButton = findViewById(R.id.playPauseButton)
        nextButton = findViewById(R.id.nextButton)
        previousButton = findViewById(R.id.previousButton)
        songTitleTextView = findViewById(R.id.songTitleTextView)
        artistTextView = findViewById(R.id.artistTextView)
        progressSeekBar = findViewById(R.id.progressSeekBar)
        emptyState = findViewById(R.id.emptyState)
        sleepTimerButton = findViewById(R.id.sleepTimerButton)

        songRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun setupClickListeners() {
        playPauseButton.setOnClickListener {
            if (musicService?.isPlaying() == true) {
                musicService?.pause()
            } else {
                musicService?.play()
            }
        }

        nextButton.setOnClickListener {
            musicService?.playNext()
        }

        previousButton.setOnClickListener {
            musicService?.playPrevious()
        }

        progressSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    musicService?.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        sleepTimerButton.setOnClickListener {
            showSleepTimerDialog()
        }
    }

    private fun loadSongs() {
        lifecycleScope.launch {
            try {
                val loadedSongs = musicRepository.getAllSongs()
                songs.clear()
                songs.addAll(loadedSongs)

                if (songs.isEmpty()) {
                    emptyState.visibility = View.VISIBLE
                    songRecyclerView.visibility = View.GONE
                } else {
                    emptyState.visibility = View.GONE
                    songRecyclerView.visibility = View.VISIBLE
                    setupRecyclerView()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@MainActivity,
                    "Error loading songs: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun setupRecyclerView() {
        val adapter = SongAdapter(songs) { song, position ->
            musicService?.setPlaylist(songs, position)
        }
        songRecyclerView.adapter = adapter
    }

    private fun bindMusicService() {
        val intent = Intent(this, MusicService::class.java)
        startService(intent)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    private fun showSleepTimerDialog() {
        val options = arrayOf("Timer Off", "15 Minutes", "30 Minutes", "1 Hour")
        val durations = arrayOf(0L, 15 * 60 * 1000L, 30 * 60 * 1000L, 60 * 60 * 1000L)

        AlertDialog.Builder(this)
            .setTitle("Sleep Timer")
            .setItems(options) { _, which ->
                cancelSleepTimer()
                if (which > 0) {
                    setSleepTimer(durations[which])
                    Toast.makeText(
                        this,
                        "Sleep timer set for ${options[which]}",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(this, "Sleep timer cancelled", Toast.LENGTH_SHORT).show()
                }
            }
            .show()
    }

    private fun setSleepTimer(duration: Long) {
        sleepTimerHandler = Handler(Looper.getMainLooper())
        sleepTimerRunnable = Runnable {
            musicService?.pause()
            Toast.makeText(this, "Sleep timer ended", Toast.LENGTH_SHORT).show()
        }
        sleepTimerHandler?.postDelayed(sleepTimerRunnable!!, duration)
    }

    private fun cancelSleepTimer() {
        sleepTimerRunnable?.let {
            sleepTimerHandler?.removeCallbacks(it)
        }
        sleepTimerHandler = null
        sleepTimerRunnable = null
    }

    override fun onSongChanged(song: Song?) {
        runOnUiThread {
            song?.let {
                songTitleTextView.text = it.title
                artistTextView.text = it.artist
                progressSeekBar.max = it.duration.toInt()
            }
        }
    }

    override fun onPlaybackStateChanged(isPlaying: Boolean) {
        runOnUiThread {
            playPauseButton.setImageResource(
                if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
            )
        }
    }

    override fun onProgressChanged(position: Int, duration: Int) {
        runOnUiThread {
            progressSeekBar.progress = position
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (serviceBound) {
            unbindService(connection)
            serviceBound = false
        }
        cancelSleepTimer()
    }
}
