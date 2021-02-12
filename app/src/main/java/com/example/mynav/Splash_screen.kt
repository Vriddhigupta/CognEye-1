package com.example.mynav
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import java.util.concurrent.Executors

class SplashScreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen)

        val SPLASH_TIME_OUT = 4000.toLong()
        Executors.newSingleThreadExecutor().execute {
            Thread.sleep(SPLASH_TIME_OUT)
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}