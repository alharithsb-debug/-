package com.example

import android.app.Application
import androidx.room.Room
import com.example.data.local.QuranDatabase
import com.example.data.repository.QuranRepository
import com.example.player.AudioPlayerManager

class QuranApplication : Application() {

    lateinit var database: QuranDatabase
        private set

    lateinit var repository: QuranRepository
        private set

    lateinit var playerManager: AudioPlayerManager
        private set

    override fun onCreate() {
        super.onCreate()

        database = Room.databaseBuilder(
            applicationContext,
            QuranDatabase::class.java,
            "quran_voice_db"
        ).fallbackToDestructiveMigration().build()

        repository = QuranRepository(
            context = applicationContext,
            quranDao = database.quranDao()
        )

        playerManager = AudioPlayerManager.getInstance(applicationContext).apply {
            initRepository(repository)
        }
    }
}
