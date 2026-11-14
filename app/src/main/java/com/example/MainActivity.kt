package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.ui.navigation.AppNavigation
import com.example.ui.theme.AlHudaTheme
import com.example.ui.viewmodels.AppViewModelFactory
import com.example.ui.viewmodels.AudioPlayerViewModel
import com.example.ui.viewmodels.AzkarViewModel
import com.example.ui.viewmodels.HadithViewModel
import com.example.ui.viewmodels.HistoryViewModel
import com.example.ui.viewmodels.PrayerViewModel
import com.example.ui.viewmodels.QiblaViewModel
import com.example.ui.viewmodels.QuranViewModel
import com.example.worker.PrayerNotificationScheduler

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        requestNotificationPermissionIfNeeded()

        PrayerNotificationScheduler.schedulePeriodicRefreshWork(applicationContext)

        val factory = AppViewModelFactory(applicationContext)
        val quranViewModel = ViewModelProvider(this, factory)[QuranViewModel::class.java]
        val prayerViewModel = ViewModelProvider(this, factory)[PrayerViewModel::class.java]
        val historyViewModel = ViewModelProvider(this, factory)[HistoryViewModel::class.java]
        val azkarViewModel = ViewModelProvider(this, factory)[AzkarViewModel::class.java]
        val hadithViewModel = ViewModelProvider(this, factory)[HadithViewModel::class.java]
        val qiblaViewModel = QiblaViewModel(application, factory.prayerRepository)
        val audioPlayerViewModel = AudioPlayerViewModel(application)

        setContent {
            AlHudaTheme {
                AppNavigation(
                    prayerViewModel = prayerViewModel,
                    quranViewModel = quranViewModel,
                    qiblaViewModel = qiblaViewModel,
                    historyViewModel = historyViewModel,
                    azkarViewModel = azkarViewModel,
                    audioViewModel = audioPlayerViewModel,
                    hadithViewModel = hadithViewModel
                )
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val isGranted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!isGranted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
