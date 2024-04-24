package com.salmahmed.videosplitter.activities
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.salmahmed.videosplitter.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File


class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)



        CoroutineScope(Dispatchers.Default).launch {
            delay(3000)

            var intent = Intent(applicationContext, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }


    }





}
