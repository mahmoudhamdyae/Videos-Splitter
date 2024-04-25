package com.salmahmed.videosplitter

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.core.app.ActivityCompat.finishAffinity
import androidx.core.app.ActivityCompat.getExternalCacheDirs
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import com.arthenica.mobileffmpeg.Statistics
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.salmahmed.videosplitter.activities.Foo
import com.salmahmed.videosplitter.activities.ResultActivity
import com.salmahmed.videosplitter.activities.SplitActivity
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.fragment_spilt_whats.*
import java.io.File


class SpiltWhatsFragment : Fragment() {

    private var dialog: Dialog? = null
    private lateinit var root: File
    private lateinit var rootDir: String
    private lateinit var cmd1: Array<String>
    private var m: String? = null
    var c1: Int = 1
    private lateinit var mInterstitialAd: InterstitialAd

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_spilt_whats, container, false)

        val videoView = view.findViewById<VideoView>(R.id.videoView_split)
        val button = view.findViewById<Button>(R.id.button5)
        val fileName = view.findViewById<TextView>(R.id.Filename_split)

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


        button.setOnClickListener {
            splitVideo(Foo.path)
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


    fun splitVideo(path: String) {
        val duration = MediaPlayer.create(activity, Uri.fromFile(File(path)))?.duration ?: 0
        Log.i("Duration", duration.toString())
        val durationInSec: Int = duration / 1000
        val parts: Int = durationInSec / 30
        Log.i("ROOT_PATH", rootDir)
        if (!root.exists()) {
            root.mkdirs()
        }



        cmd1 = arrayOf("-y")
        for (i in 0..durationInSec step 30) {
            c1 = i
            val a = (c1 / 30) + 1
            val videoName = File(root, "VideoPart${String.format("%2d", a)}.mp4".replace(" ", ""))
            cmd1 += arrayOf(
                "-i",
                path,
                "-ss",
                "$i",
                "-t",
                "30",
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
                    String.format("Async command execution failed with returnCode=%d.", returnCode)
                )
                Toast.makeText(context, "RETURN_CODE_failed", Toast.LENGTH_SHORT).show()
                activity?.runOnUiThread {progressDialog.dismiss()}


            }
        }


        Config.enableStatisticsCallback { statistics: Statistics ->
            val sb = "progress : " + statistics.time.toString()
            activity?.runOnUiThread { progressDialog.setMessage(sb) }
        }


    }


}