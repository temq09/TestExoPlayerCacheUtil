package dev.temq.testexoplayercacheutil

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.CacheUtil
import com.google.android.exoplayer2.upstream.cache.NoOpCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.exoplayer2.util.Util
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.schedulers.Schedulers

private const val USER_AGENT = "InstantVideoExoPlayer"

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val simpleCache = SimpleCache(
            cacheDir,
            NoOpCacheEvictor(),
//            LeastRecentlyUsedCacheEvictor(50 * 1024 * 1024),
            ExoDatabaseProvider(this)
        )

        val bandwidthMeter = DefaultBandwidthMeter.Builder(this)
            .build()

        val defaultDataSourceFactory = DefaultDataSourceFactory(
            this,
            bandwidthMeter,
            DefaultHttpDataSourceFactory(Util.getUserAgent(applicationContext, USER_AGENT), bandwidthMeter)
        )

        val preloadDataSource =
            CacheDataSource(
                simpleCache,
                defaultDataSourceFactory.createDataSource()
            )

        Completable
            .fromAction {
                CacheUtil
                    .cache(
                        DataSpec(
                            Uri.parse("http://techslides.com/demos/sample-videos/small.mp4"),
                            0,
                            80 * 1024 * 1024,
                            "cacheVideo"
                        ),
                        simpleCache,
                        null,
                        preloadDataSource,
                        null,
                        null
                    )
            }
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { showToast("Cache success") }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
