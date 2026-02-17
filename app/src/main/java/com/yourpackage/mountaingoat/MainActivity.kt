package com.yourpackage.mountaingoat

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.yourpackage.mountaingoat.ui.views.GameView

/**
 * Main activity that hosts the game
 */
class MainActivity : AppCompatActivity() {

    private lateinit var gameView: GameView
    private lateinit var adView: AdView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable fullscreen immersive mode
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_FULLSCREEN
            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        )

        // Keep screen on while playing
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Initialize Mobile Ads SDK
        MobileAds.initialize(this)

        // Inflate layout with GameView and AdView
        setContentView(R.layout.activity_main)
        gameView = findViewById(R.id.gameView)
        adView = findViewById(R.id.adView)

        // Load banner ad
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }

    override fun onResume() {
        super.onResume()
        gameView.resumeGame()
        adView.resume()
    }

    override fun onPause() {
        super.onPause()
        gameView.pauseGame()
        adView.pause()
    }

    override fun onDestroy() {
        adView.destroy()
        super.onDestroy()
    }

    @Deprecated("Deprecated in Java")
    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        if (gameView.isInActiveGame()) {
            gameView.pauseGame()
        } else {
            super.onBackPressed()
        }
    }
}
