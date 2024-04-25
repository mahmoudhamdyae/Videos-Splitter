package com.salmahmed.videosplitter.activities



import android.Manifest
import android.app.Dialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.Config.RETURN_CODE_CANCEL
import com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS
import com.arthenica.mobileffmpeg.FFmpeg
import com.arthenica.mobileffmpeg.Statistics
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.salmahmed.videosplitter.R
import com.salmahmed.videosplitter.model.URIPathHelper
import kotlinx.android.synthetic.main.activity_home.adView
import kotlinx.android.synthetic.main.activity_home.tv_camera
import kotlinx.android.synthetic.main.activity_home.tv_gallery
import java.io.File


class HomeActivity : AppCompatActivity() {
    // private var progressBar: RoundedHorizontalProgressBar? = null
    lateinit var rootnow: File
    private lateinit var rootDirnow: String

    private var dialog: Dialog? = null
    private lateinit var root: File
    private lateinit var rootDir: String
    private lateinit var cmd1: Array<String>
    private val GALLERY = 1
    private val CAMERA = 2
    var c1: Int = 1
    var x: Int = 0
    var count: Int = 0
    var doubleBackToExitPressedOnce = false
    private lateinit var mInterstitialAd: InterstitialAd

    //  lateinit var fFmpeg: FFmpeg
    var context = this@HomeActivity

    private fun checkStoragePermissions(): Boolean {
        return if (SDK_INT >= Build.VERSION_CODES.R) {
            //Android is 11 (R) or above
            Environment.isExternalStorageManager()
        } else {
            //Below android 11
            val write =
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            val read =
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            read == PackageManager.PERMISSION_GRANTED && write == PackageManager.PERMISSION_GRANTED
        }
    }

    private val STORAGE_PERMISSION_CODE = 23

    private fun requestForStoragePermissions() {
        if (checkStoragePermissions()) {
            return
        }
        //Android is 11 (R) or above
        if (SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent()
                intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                val uri = Uri.fromParts("package", this.packageName, null)
                intent.setData(uri)
                storageActivityResultLauncher.launch(intent)
            } catch (e: java.lang.Exception) {
                val intent = Intent()
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                storageActivityResultLauncher.launch(intent)
            }
        } else {
            //Below android 11
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                STORAGE_PERMISSION_CODE
            )
        }
    }

    private val storageActivityResultLauncher = registerForActivityResult(StartActivityForResult()) {
        if (SDK_INT >= Build.VERSION_CODES.R) {
            //Android is 11 (R) or above
            if (Environment.isExternalStorageManager()) {
                //Manage External Storage Permissions Granted
                Log.d(
                    "HomeActivity",
                    "onActivityResult: Manage External Storage Permissions Granted"
                )
            } else {
                Toast.makeText(
                    this@HomeActivity,
                    "Storage Permissions Denied",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            //Below android 11
        }
    }

//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<String?>?,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions!!, grantResults)
//        if (requestCode == STORAGE_PERMISSION_CODE) {
//            if (grantResults.isNotEmpty()) {
//                val write = grantResults[0] == PackageManager.PERMISSION_GRANTED
//                val read = grantResults[1] == PackageManager.PERMISSION_GRANTED
//                if (read && write) {
//                    Toast.makeText(
//                        this@HomeActivity,
//                        "Storage Permissions Granted",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                } else {
//                    Toast.makeText(
//                        this@HomeActivity,
//                        "Storage Permissions Denied",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//            }
//        }
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        checkPermissions()
        requestForStoragePermissions()
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
        mInterstitialAd = InterstitialAd(this)
        mInterstitialAd.adUnitId = getString(R.string.interstitial_ad_id)
        mInterstitialAd.loadAd(AdRequest.Builder().build())

        try {
            deleteFile()
            deleteRecursive(rootnow)
        }catch (e: Exception){

        }



        if (SDK_INT >= Build.VERSION_CODES.N) {
            val cacheDir = externalMediaDirs[0].toString()
            rootDir = "$cacheDir/WhatsappSplit"
            root = File(rootDir)
            if (!root.exists()) {
                root.mkdirs()
            }
        } else {
            var cacheDir = externalCacheDir
            if (cacheDir == null)
                cacheDir = getCacheDir()
            rootDir = cacheDir!!.absolutePath + "/WhatsappSplit"
            root = File(rootDir)

        }


        tv_camera.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
            startActivityForResult(intent, CAMERA)
        }
        tv_gallery.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            intent.type = "video/*"
            startActivityForResult(intent, GALLERY)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_CANCELED) {
            return
        }
        if (data != null) {
            val contentURI = data.data
            val uriPathHelper = URIPathHelper()
            val filePath = uriPathHelper.getPath(applicationContext, contentURI!!)

            if (filePath != null) {
                Foo.path = filePath
            }
            val intent = Intent(this, SplitActivity::class.java).apply {
                putExtra("my_data",filePath!!)
            }
            startActivity(intent)
            finish()

          //  splitVideo(filePath!!)
        }
    }




    fun splitVideo(path: String) {
        val duration = MediaPlayer.create(this, Uri.fromFile(File(path))).duration
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
            val videoName = File(root, "VideoPart${String.format("%2d", a)}.mp4")
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


        val progressDialog = ProgressDialog(this)
        progressDialog.setCancelable(false)
        progressDialog.show()


        val executionId = FFmpeg.executeAsync(cmd1) { executionId1: Long, returnCode: Int ->
            if (returnCode == RETURN_CODE_SUCCESS) {
                if (mInterstitialAd.isLoaded) {
                    mInterstitialAd.show()
                } else {
                    startActivity(Intent(this@HomeActivity, ResultActivity::class.java))
                    finishAffinity()
                    mInterstitialAd.loadAd(AdRequest.Builder().build())
                    Log.i("TAG", "The interstitial wasn't loaded yet.")
                }
                mInterstitialAd.adListener = object : AdListener() {
                    override fun onAdClosed() {
                        super.onAdClosed()
                        if (dialog != null && dialog!!.isShowing) {
                            dialog!!.dismiss()
                        }
                        startActivity(Intent(this@HomeActivity, ResultActivity::class.java))
                        finishAffinity()
                        mInterstitialAd.loadAd(AdRequest.Builder().build())
                    }
                }

            } else if (returnCode == RETURN_CODE_CANCEL) {
                Log.i(Config.TAG, "Async command execution cancelled by user.")
                runOnUiThread { progressDialog.dismiss() }
                Toast.makeText(context, "RETURN_CODE_CANCEL", Toast.LENGTH_SHORT).show()

            } else {
                Log.i(
                    Config.TAG,
                    String.format("Async command execution failed with returnCode=%d.", returnCode)
                )
                Toast.makeText(context, "RETURN_CODE_failed", Toast.LENGTH_SHORT).show()
                runOnUiThread { progressDialog.dismiss() }


            }
        }


        Config.enableStatisticsCallback { statistics: Statistics ->
            val sb = "progress : " + statistics.toString()
            runOnUiThread { progressDialog.setMessage(sb) }
        }


    }


    override fun onBackPressed() {
        openQuitDialog()
    }



    private fun checkPermissions() {
        if (SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    getBaseContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    0
                )
            }

            if (ContextCompat.checkSelfPermission(
                    getBaseContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    0
                )
            }
        }

    }


    fun openQuitDialog() {
        val alert: AlertDialog.Builder
        alert = AlertDialog.Builder(this)
        alert.setTitle(R.string.app_name)
        alert.setIcon(R.mipmap.ic_launcher)
        alert.setMessage(getString(R.string.sure_quit))

        alert.setPositiveButton(R.string.exit,
            DialogInterface.OnClickListener { dialog, whichButton -> finish() })

        alert.setNegativeButton(getString(R.string.cancel),
            DialogInterface.OnClickListener { dialog, which ->
                val url = "https://play.google.com/store/apps/details?id=$packageName"

                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(url)
                startActivity(intent)

            })

        alert.setNeutralButton(R.string.other_apps,
            DialogInterface.OnClickListener { dialog, whichButton ->

                val url = "https://play.google.com/store/apps/developer?id=Various+applications"

                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(url)
                startActivity(intent)


            })


        alert.show()
    }

    private fun deleteRecursive(fileOrDirectory: File) {
        if (fileOrDirectory.isDirectory) {
            for (child in fileOrDirectory.listFiles()!!) {
                deleteRecursive(child)
            }
        }
        fileOrDirectory.delete()
    }

    fun deleteFile() {
        if (SDK_INT >= Build.VERSION_CODES.N) {
            val cacheDirnow = externalMediaDirs[0].toString()
            rootDirnow = "$cacheDirnow/WhatsappSplit"
            rootnow = File(rootDirnow)
            if (!rootnow.exists()) {
                rootnow.mkdirs()
            }
        } else {
            var cacheDirnow = externalCacheDir
            if (cacheDirnow == null)
                cacheDirnow = getCacheDir()
            rootDirnow = cacheDir!!.absolutePath + "/WhatsappSplit"
            rootnow = File(rootDirnow)

        }

        for (i in rootnow.listFiles()!!.indices) {
            val files = rootnow.listFiles()!![i]
            if (files.absolutePath.contains(".mp4")) {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(this, Uri.fromFile(files))
                retriever.release()

            }
        }
    }


}

class Foo {

    companion object {
        lateinit var instance: Foo
        lateinit var path: String
    }

    init {
        instance = this
    }

}