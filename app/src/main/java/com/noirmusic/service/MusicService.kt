package com.noirmusic.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.noirmusic.MainActivity
import com.noirmusic.R
import com.noirmusic.model.Song

class MusicService : Service(), MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener {

    private var mediaPlayer: MediaPlayer? = null
    private var currentSong: Song? = null
    private var playlist: MutableList<Song> = mutableListOf()
    private var currentPosition = 0
    private var isRepeatMode = false
    private var isShuffleMode = false
    private val binder = MusicBinder()
    private var listener: MusicServiceListener? = null

    private lateinit var audioManager: AudioManager
    private var audioFocusRequest: AudioFocusRequest? = null

    inner class MusicBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }

    interface MusicServiceListener {
        fun onSongChanged(song: Song?)
        fun onPlaybackStateChanged(isPlaying: Boolean)
        fun onProgressChanged(position: Int, duration: Int)
    }

    override fun onCreate() {
        super.onCreate()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        createNotificationChannel()
    }

    override fun onBind(intent: Intent?): IBinder = binder

    fun setListener(listener: MusicServiceListener) {
        this.listener = listener
    }

    fun setPlaylist(songs: List<Song>, position: Int = 0) {
        playlist.clear()
        playlist.addAll(songs)
        currentPosition = position
        playSong(currentPosition)
    }

    fun playSong(position: Int) {
        if (position < 0 || position >= playlist.size) return

        currentPosition = position
        currentSong = playlist[position]

        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(currentSong!!.path)
                setOnCompletionListener(this@MusicService)
                setOnPreparedListener(this@MusicService)
                prepareAsync()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onPrepared(mp: MediaPlayer?) {
        requestAudioFocus()
        mp?.start()
        listener?.onSongChanged(currentSong)
        listener?.onPlaybackStateChanged(true)
        showNotification()
        startProgressUpdates()
    }

    override fun onCompletion(mp: MediaPlayer?) {
        if (isRepeatMode) {
            playSong(currentPosition)
        } else {
            playNext()
        }
    }

    fun play() {
        if (mediaPlayer == null && playlist.isNotEmpty()) {
            playSong(currentPosition)
        } else {
            requestAudioFocus()
            mediaPlayer?.start()
            listener?.onPlaybackStateChanged(true)
            showNotification()
            startProgressUpdates()
        }
    }

    fun pause() {
        mediaPlayer?.pause()
        listener?.onPlaybackStateChanged(false)
        showNotification()
    }

    fun playNext() {
        if (isShuffleMode) {
            currentPosition = (0 until playlist.size).random()
        } else {
            currentPosition = (currentPosition + 1) % playlist.size
        }
        playSong(currentPosition)
    }

    fun playPrevious() {
        currentPosition = if (currentPosition - 1 < 0) playlist.size - 1 else currentPosition - 1
        playSong(currentPosition)
    }

    fun isPlaying(): Boolean = mediaPlayer?.isPlaying ?: false

    fun getCurrentPosition(): Int = mediaPlayer?.currentPosition ?: 0

    fun getDuration(): Int = mediaPlayer?.duration ?: 0

    fun seekTo(position: Int) {
        mediaPlayer?.seekTo(position)
    }

    fun toggleRepeat() {
        isRepeatMode = !isRepeatMode
    }

    fun toggleShuffle() {
        isShuffleMode = !isShuffleMode
    }

    fun getCurrentSong(): Song? = currentSong

    private fun requestAudioFocus(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()

            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(audioAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener { focusChange ->
                    when (focusChange) {
                        AudioManager.AUDIOFOCUS_LOSS -> pause()
                        AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> pause()
                        AudioManager.AUDIOFOCUS_GAIN -> play()
                    }
                }
                .build()

            audioManager.requestAudioFocus(audioFocusRequest!!) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                { focusChange ->
                    when (focusChange) {
                        AudioManager.AUDIOFOCUS_LOSS -> pause()
                        AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> pause()
                        AudioManager.AUDIOFOCUS_GAIN -> play()
                    }
                },
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            ) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
    }

    private fun startProgressUpdates() {
        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                if (isPlaying()) {
                    listener?.onProgressChanged(getCurrentPosition(), getDuration())
                    handler.postDelayed(this, 1000)
                }
            }
        }
        handler.post(runnable)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Music Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Controls for music playback"
                setShowBadge(false)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification() {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(currentSong?.title ?: "Noir Music")
            .setContentText(currentSong?.artist ?: "No song playing")
            .setSmallIcon(R.drawable.ic_music_note)
            .setContentIntent(pendingIntent)
            .setOngoing(isPlaying())
            .setOnlyAlertOnce(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let {
                audioManager.abandonAudioFocusRequest(it)
            }
        }
    }

    companion object {
        private const val CHANNEL_ID = "music_playback_channel"
        private const val NOTIFICATION_ID = 1
    }
}
