package com.salmahmed.videosplitter

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.ParseException
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.Statistics
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.salmahmed.videosplitter.activities.Foo
import com.salmahmed.videosplitter.activities.ResultActivity
import com.salmahmed.videosplitter.activities.SplitActivity
import kotlinx.android.synthetic.main.activity_home.*
import java.io.File
import java.util.concurrent.TimeUnit

class SingleVedioFragment : Fragment() ,View.OnClickListener, MediaScannerConnection.MediaScannerConnectionClient {

    private var dialog: Dialog? = null
    private lateinit var root: File
    private lateinit var rootDir: String
    private lateinit var cmd1: Array<String>
    var c1: Int = 1
    private lateinit var mInterstitialAd: InterstitialAd
    var asm: MediaScannerConnection? = null
    var b = 0
    var c = 0
    var SVS_btn : Button? = null
    var d: TextView? = null
    var e: TextView? = null
    var f: TextView? = null
    var g: TextView? = null
    var h: ImageView? = null
    lateinit var i: VideoSliceSeekBar
    var j: VideoView? = null
    private val l = ""
    private var m: String? = null
    var n: String? = null
    var o = VideoPlayerState()
    private val p: a = a()
    private var q: InterstitialAd? = null

    private inner class a : Handler() {
        private var b = false
        private var c: Runnable? = null

        init{
            this.b = false
            this.c = Runnable { this@a.ar() }
        }

        fun ar() {
            if (!this.b) {
                this.b = SingleVedioFragment.k
                sendEmptyMessage(0)
            }
        }

        override fun handleMessage(message: Message) {
            this.b = false
            i.videoPlayingProgress(j!!.currentPosition)
            if (!j!!.isPlaying || j!!.currentPosition >= i.rightProgress) {
                if (j!!.isPlaying) {
                    j!!.pause()
                    h!!.setBackgroundResource(R.drawable.play2)
                }
                i.setSliceBlocked(false)
                i.removeVideoStatusThumb()
                return
            }
            postDelayed(this.c!!, 50)
        }
    }




    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        var view = inflater.inflate(R.layout.fragment_single_vedio, container, false)

         var myActivity = activity as SplitActivity
        val message =  myActivity.myData
      //  Toast.makeText(activity,message,Toast.LENGTH_LONG).show()


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

            SVS_btn = view.findViewById(R.id.SVS_btn) as Button
            h = view.findViewById<View>(R.id.buttonply1) as ImageView
             i = view.findViewById(R.id.seek_bar1)
            f = view.findViewById<View>(R.id.Filename) as TextView
            d = view.findViewById<View>(R.id.left_pointer) as TextView
            e = view.findViewById<View>(R.id.right_pointer) as TextView
            g = view.findViewById<View>(R.id.dur) as TextView
            j = view.findViewById<View>(R.id.videoView1) as VideoView





           // i.visibility = View.VISIBLE
            h!!.setOnClickListener(this)
            m = Foo.path
            if (m == null) {
                activity?.finish()
            }


          //  fFmpeg = FFmpeg.getInstance(this.context)
          //  g()
            f!!.text = File(m).name
            j!!.setVideoPath(m)
            j!!.seekTo(100)
            e()
            j!!.setOnCompletionListener { h!!.setBackgroundResource(R.drawable.play2) }
            q = InterstitialAd(this.context)
            q!!.adUnitId = getString(R.string.InterstitialAd)
            q!!.adListener = object : AdListener() {
                override fun onAdClosed() {
                    //   VideoCutter.this.c();
                }
            }
            ah()

        SVS_btn!!.setOnClickListener(View.OnClickListener {
            if (j!!.isPlaying) {
                j!!.pause()
                h!!.setBackgroundResource(R.drawable.play2)
            }
            splitVideo()

        })




        return view


    }

    override fun onResume() {
        super.onResume()
        i.onWindowFocusChanged(true)
    }





    private fun ah() {
        if (!q!!.isLoading && !q!!.isLoaded) {
            q!!.loadAd(AdRequest.Builder().build())
        }
    }

    fun b() {
        if (q == null || !q!!.isLoaded) {
            c()
        } else {
            q!!.show()
        }
    }

    fun c() {
        val intent = Intent(this.context, ResultActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        intent.putExtra("song", n)
        startActivity(intent)
        activity?.finish()
    }




    private fun e() {
        j!!.setOnPreparedListener { mediaPlayer ->
            i.setSeekBarChangeListener { i, i2 ->
                if (this.i.selectedThumb == 1) {
                    j!!.seekTo(this.i.leftProgress)
                }
                d?.setText(formatTimeUnit(i.toLong()))
                e?.setText(formatTimeUnit(i2.toLong()))
                o.start = i
                o.stop = i2
                c = i / 1000
                b = i2 / 1000
                val textView = g
                val sb = StringBuilder()
                sb.append("Duration : ")
                sb.append(
                    String.format(
                        "%02d:%02d:%02d", *arrayOf<Any>(
                            Integer.valueOf((b - c) / 3600), Integer.valueOf(
                                (b - c) % 3600 / 60
                            ), Integer.valueOf((b - c) % 60)
                        )
                    )
                )
                textView!!.text = sb.toString()
            }
            i.setMaxValue(mediaPlayer.duration)
            i.leftProgress = 0
            i.rightProgress = mediaPlayer.duration
            i.setProgressMinDiff(0)
        }
    }

    private fun f() {
        if (j!!.isPlaying) {
            j!!.pause()
            i.setSliceBlocked(false)
            h!!.setBackgroundResource(R.drawable.play2)
            i.removeVideoStatusThumb()
            return
        }
        j!!.seekTo(i.leftProgress)
        j!!.start()
        i.videoPlayingProgress(i.leftProgress)
        h!!.setBackgroundResource(R.drawable.pause2)
        p.ar()
    }

    override fun onClick(view: View) {
        if (view === h) {
            f()
        }
    }

    override fun onMediaScannerConnected() {
        asm!!.scanFile(l, "video/*")
    }

    override fun onScanCompleted(str: String, uri: Uri) {
        asm!!.disconnect()
    }






    companion object {
        var AppContext: Context? = null
        const val k = true
    }

    fun splitVideo() {
        val valueOf = c.toString()
        b.toString()
        val valueOf2 = (b - c).toString()
        Log.i("ROOT_PATH", rootDir)
        if (!root.exists()) {
            root.mkdirs()
        }

        val videoName = File(root, "VideoPart1.mp4")
        cmd1 = arrayOf("-ss", valueOf, "-y", "-i", m.toString(), "-t", valueOf2, "-vcodec", "mpeg4", "-b:v", "2097152", "-b:a", "48000", "-ac", "2", "-ar", "22050",  videoName.absolutePath)






        val progressDialog = ProgressDialog(activity)
        progressDialog.setCancelable(false)
        progressDialog.show()


        val executionId = com.arthenica.mobileffmpeg.FFmpeg.executeAsync(cmd1) { executionId1: Long, returnCode: Int ->
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

    @Throws(ParseException::class)
    fun formatTimeUnit(j2: Long): String {

        return String.format("%02d:%02d", *arrayOf<Any>(
                java.lang.Long.valueOf(TimeUnit.MILLISECONDS.toMinutes(j2)), java.lang.Long.valueOf(
                    TimeUnit.MILLISECONDS.toSeconds(j2) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(j2))))) }
    }
