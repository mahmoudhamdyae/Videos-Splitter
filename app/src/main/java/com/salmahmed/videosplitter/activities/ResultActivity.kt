package com.salmahmed.videosplitter.activities


import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.AdRequest
import com.salmahmed.videosplitter.R
import com.salmahmed.videosplitter.adapter.ResultAdapter
import com.salmahmed.videosplitter.model.Video1
import kotlinx.android.synthetic.main.activity_result.*
import java.io.*


class ResultActivity : AppCompatActivity() {
    lateinit var root: File
    private lateinit var rootDir: String
    lateinit var currentPhotoPath: String



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)



        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
        rv_video.layoutManager = LinearLayoutManager(this)
        val videoList: ArrayList<Video1> = ArrayList()
        videoList.clear()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
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

        for (i in root.listFiles()!!.indices) {
            val files = root.listFiles()!![i]
            if (files.absolutePath.contains(".mp4")) {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(this, Uri.fromFile(files))
                val video = Video1(i, files.name, files.absolutePath)
                retriever.release()
                videoList.add(video)
            }
        }
        val sortedList = videoList.sortedBy { it.name }
        val adapter = ResultAdapter(this, sortedList)
        rv_video.adapter = adapter
        back.setOnClickListener {
            onBackPressed()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }


    private fun deleteRecursive(fileOrDirectory: File) {
        if (fileOrDirectory.isDirectory) {
            for (child in fileOrDirectory.listFiles()!!) {
                deleteRecursive(child)
            }
        }
        fileOrDirectory.delete()
    }


    override fun onDestroy() {
        super.onDestroy()
        deleteRecursive(root)
    }


    // try to save video file
 /*   fun commonDocumentDirPath(FolderName: String): File? {
        var dir: File? = null
        dir = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                    .toString() + "/" + FolderName
            )
        } else {
            File(Environment.getExternalStorageDirectory().toString() + "/" + FolderName)
        }

        // Make sure the path directory exists.
        if (!dir.exists()) {
            // Make it, if it doesn't exit
            val success = dir.mkdirs()
            if (!success) {
                dir = null
            }
        }
        return dir
    }


 fun saveVideoToStorage(filepath : String ){
        val externalStorageStats = Environment.getExternalStorageState()
        if (externalStorageStats.equals(Environment.MEDIA_MOUNTED)){
            val storageDirectory = Environment.getExternalStorageDirectory().toString()
            val file = File(storageDirectory,"test_video.mp4")
            try {
                val stream : OutputStream = FileOutputStream(file)
                var filepathin = filepath
                stream.flush()


            }
        }

    }


    private fun saveVideoToInternalStorage(filePath: String) {
        val newfile: File
        try {
            val currentFile = File(filePath)
            val fileName = currentFile.name
            val cw = ContextWrapper(applicationContext)
            val directory = cw.getDir("videoDir", Context.MODE_PRIVATE)
            newfile = File(directory, fileName)
            if (currentFile.exists()) {
                val `in`: InputStream = FileInputStream(currentFile)
                val out: OutputStream = FileOutputStream(newfile)

                // Copy the bits from instream to outstream
                val buf = ByteArray(1024)
                var len: Int
                while (`in`.read(buf).also { len = it } > 0) {
                    out.write(buf, 0, len)
                }
                `in`.close()
                out.close()
               Toast.makeText(baseContext, "Video file saved successfully",Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(baseContext, "Video saving failed. Source file missing.",Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }*/

}