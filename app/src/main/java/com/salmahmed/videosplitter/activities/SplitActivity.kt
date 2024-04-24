package com.salmahmed.videosplitter.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation.findNavController
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.salmahmed.videosplitter.R
import com.salmahmed.videosplitter.VideoSliceSeekBar


class SplitActivity : AppCompatActivity() {

    lateinit var myData :String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_split)

        myData = intent.getStringExtra("my_data").toString()

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        val navController = findNavController(this, R.id.fragment)
        setupWithNavController(bottomNavigationView, navController)



       // val appBarConfiguration = AppBarConfiguration(setOf(R.id.spiltWhatsFragment,R.id.customSpiltFragment,R.id.singleVedioFragment))
      //  setupActionBarWithNavController(navController,appBarConfiguration)




    }



}