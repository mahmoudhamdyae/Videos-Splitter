package com.salmahmed.videosplitter

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import com.arthenica.mobileffmpeg.Statistics
import com.bumptech.glide.load.engine.bitmap_recycle.IntegerArrayAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.salmahmed.videosplitter.activities.Foo
import com.salmahmed.videosplitter.activities.ResultActivity
import com.salmahmed.videosplitter.activities.SplitActivity
import kotlinx.android.synthetic.main.activity_home.*
import java.io.File
import kotlin.properties.Delegates


class CustomSpiltFragment : Fragment() {

    private var dialog: Dialog? = null
    private lateinit var root: File
    private lateinit var rootDir: String
    private lateinit var cmd1: Array<String>
    var c1: Int = 1
    private lateinit var mInterstitialAd: InterstitialAd
    lateinit var  message :String
    lateinit  var timeEdt : EditText
    lateinit  var start_btn : Button
    var time_in_sec by Delegates.notNull<Int>()
    private var m: String? = null


    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_custom_spilt, container, false)

        timeEdt = view.findViewById(R.id.timeEdt)
        start_btn = view.findViewById(R.id.btn_split_custom)
        val videoView = view.findViewById<VideoView>(R.id.videoView_Custom)
        val fileName = view.findViewById<TextView>(R.id.Filename_custom)

        val adRequest = AdRequest.Builder().build()
        activity?.adView?.loadAd(adRequest)
        mInterstitialAd = InterstitialAd(activity)
        mInterstitialAd.adUnitId = getString(R.string.interstitial_ad_id)
        mInterstitialAd.loadAd(AdRequest.Builder().build())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val cacheDir = (activity as SplitActivity).externalMediaDirs[0].toString()
            rootDir = "$cacheDir/WhatsappSplit"
            root = File(rootDir)
            if (!root.exists()) {
                root.mkdirs()
            }
        } else {
            var cacheDir = (activity as SplitActivity).externalCacheDir
            if (cacheDir == null)
                cacheDir = (activity as SplitActivity).cacheDir
            rootDir = cacheDir!!.absolutePath + "/WhatsappSplit"
            root = File(rootDir)

        }


        start_btn.setOnClickListener {
            if (!timeEdt.text.equals("")) {
                time_in_sec = (timeEdt.text.toString()).toInt()
                splitVideo(Foo.path, time_in_sec)

            }
        }



        m = Foo.path
        if (m == null) {
            activity?.finish()
        }
        fileName.text = File(m).name
        videoView.setVideoPath(m)
        videoView.seekTo(100)







        return view
    }




    fun splitVideo(path: String , time : Int) {
        val duration = MediaPlayer.create(activity, Uri.fromFile(File(path)))?.duration ?: 0
        Log.i("Duration", duration.toString())
        val durationInSec: Int = duration / 1000
        if (time > durationInSec) {
            Toast.makeText(activity, "the time beggar than video duration ", Toast.LENGTH_LONG)
                .show()
        } else if (time == 0)  {
            Toast.makeText(activity, "the time can not equal 0 ", Toast.LENGTH_LONG)
                .show()
        } else {
            val parts: Int = durationInSec / time
            Log.i("ROOT_PATH", rootDir)
            if (!root.exists()) {
                root.mkdirs()
            }



            cmd1 = arrayOf("-y")
            for (i in 0..durationInSec step time) {
                c1 = i
                val a = (c1 / time) + 1
                val videoName = File(root, "VideoPart${String.format("%2d", a)}.mp4"/*.replace(" ", "")*/)
                cmd1 += arrayOf(
                    "-i",
                    path,
                    "-ss",
                    "$i",
                    "-t",
                    "$time",
                    "-reset_timestamps",
                    "1",
                    "-map",
                    "0",
                    "-preset",
                    "ultrafast",
                    videoName.absolutePath
                )
            }


            val progressDialog = ProgressDialog(activity)
            progressDialog.setCancelable(false)
            progressDialog.show()


            val executionId = FFmpeg.executeAsync(cmd1) { executionId1: Long, returnCode: Int ->
                if (returnCode == Config.RETURN_CODE_SUCCESS) {
                    if (mInterstitialAd.isLoaded) {
                        mInterstitialAd.show()
                    } else {
                        startActivity(Intent(activity, ResultActivity::class.java))
                        mInterstitialAd.loadAd(AdRequest.Builder().build())
                        Log.i("TAG", "The interstitial wasn't loaded yet.")
                        activity?.finish()
                    }
                    mInterstitialAd.adListener = object : AdListener() {
                        override fun onAdClosed() {
                            super.onAdClosed()
                            if (dialog != null && dialog!!.isShowing) {
                                dialog!!.dismiss()
                            }
                            startActivity(Intent(activity, ResultActivity::class.java))
                            mInterstitialAd.loadAd(AdRequest.Builder().build())
                            activity?.finish()
                        }
                    }

                } else if (returnCode == Config.RETURN_CODE_CANCEL) {
                    Log.i(Config.TAG, "Async command execution cancelled by user.")
                    activity?.runOnUiThread { progressDialog.dismiss() }

                    Toast.makeText(context, "RETURN_CODE_CANCEL", Toast.LENGTH_SHORT).show()

                } else {
                    Log.i(
                        Config.TAG,
                        String.format(
                            "Async command execution failed with returnCode=%d.",
                            returnCode
                        )
                    )
                    Toast.makeText(context, "RETURN_CODE_failed", Toast.LENGTH_SHORT).show()
                    activity?.runOnUiThread { progressDialog.dismiss() }


                }
            }


            Config.enableStatisticsCallback { statistics: Statistics ->
                val sb = "progress : " + statistics.time.toString()
                activity?.runOnUiThread { progressDialog.setMessage(sb) }
            }


        }
    }

}