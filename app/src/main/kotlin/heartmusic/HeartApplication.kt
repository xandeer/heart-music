package heartmusic

import android.app.Application
import androidx.media3.exoplayer.ExoPlayer
import heartmusic.di.DataModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.java.KoinJavaComponent.get
import timber.log.Timber

class HeartApplication : Application() {
  override fun onCreate() {
    super.onCreate()

    if (BuildConfig.DEBUG) {
      Timber.plant(Timber.DebugTree())
    }

    startKoin {
      androidLogger()
      androidContext(this@HeartApplication)
      modules(DataModule)
    }
  }

  override fun onTerminate() {
    super.onTerminate()
    get<ExoPlayer>(ExoPlayer::class.java).release()
  }
}
