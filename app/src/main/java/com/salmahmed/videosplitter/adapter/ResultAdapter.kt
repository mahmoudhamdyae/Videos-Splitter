package com.salmahmed.videosplitter.adapter

import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.StrictMode
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.gms.ads.AdRequest
import com.salmahmed.videosplitter.R
import com.salmahmed.videosplitter.model.Video1
import kotlinx.android.synthetic.main.play_video_dialog.*
import kotlinx.android.synthetic.main.result_item.view.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*


class ResultAdapter(private val context: Context, val videoList: List<Video1>) :
    RecyclerView.Adapter<ResultAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(context)
            .inflate(R.layout.result_item, parent, false)
        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return videoList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setIsRecyclable(false)
        holder.tvName.text = videoList[position].name.replace(".mp4", "")
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(context, Uri.fromFile(File(videoList[position].path)))
        val time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        if (time!!.matches("0".toRegex())) {
            holder.c1.visibility = GONE
        } else {
            holder.c1.visibility = VISIBLE
        }
        retriever.release()
        Glide.with(context)
            .load(videoList[position].path)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .into(holder.ivImg)



        holder.tvShare.setOnClickListener {

            val shareIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                val uri = Uri.parse("file://" + videoList[position].path)
                Log.d("ttt", "file://" + videoList[position].path)
              //  setPackage("com.whatsapp")
                putExtra(Intent.EXTRA_STREAM, uri)
                type = "video/*"
            }

         //   shareIntent.setPackage("com.whatsapp")
            holder.tvShare.context.startActivity(Intent.createChooser(shareIntent, "Share video"))



        }

        holder.tvSave.setOnClickListener {

            // Create an image file name

            val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())

            saveMediaFile2(videoList[position].path,true,"video_${timeStamp}_.mp4",holder.itemView.context)



        }



        holder.ivPlay.setOnClickListener {

            val dialog = Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.play_video_dialog)
            val adView = dialog.adView1
            val adRequest = AdRequest.Builder().build()
            adView.loadAd(adRequest)
            val videoView = dialog.video_view
            videoView.setVideoURI(Uri.fromFile(File(videoList[position].path)))
            videoView.start()
            videoView.setOnCompletionListener {
                dialog.dismiss()
            }
            dialog.show()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivImg = view.video_img!!
        val tvName = view.video_name!!
        val tvShare = view.tv_share!!
        val tvSave = view.tv_save !!
        val ivPlay = view.iv_play!!
        val c1 = view.v1!!
    }


    private fun saveMediaFile2(filePath: String?, isVideo: Boolean, fileName: String, context: Context) {
        filePath?.let {
            val values = ContentValues().apply {
                val folderName = if (isVideo) {
                    Environment.DIRECTORY_MOVIES
                } else {
                    Environment.DIRECTORY_PICTURES
                }
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                //put(MediaStore.Images.Media.MIME_TYPE, MimeUtils.guessMimeTypeFromExtension(getExtension(fileName)))
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, "$folderName/video/")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }

            val collection = if (isVideo) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                } else {
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                } else {
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                }

            }
            val fileUri = context.contentResolver.insert(collection, values)

            fileUri?.let {
                if (isVideo) {
                    val videoFile = File(filePath)
                    if (videoFile.exists()) {

                        context.contentResolver.openFileDescriptor(fileUri, "w").use { descriptor ->
                            descriptor?.let {
                                FileOutputStream(descriptor.fileDescriptor).use { out ->
                                    FileInputStream(videoFile).use { inputStream ->
                                        val buf = ByteArray(8192)
                                        while (true) {
                                            val sz = inputStream.read(buf)
                                            if (sz <= 0) break
                                            out.write(buf, 0, sz)
                                        }
                                    }
                                }
                            }
                        }
                        Toast.makeText(context, "Video file saved successfully",Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "Video saving failed. Source file missing.",Toast.LENGTH_LONG).show()
                    }


                } else {
                    context.contentResolver.openOutputStream(fileUri).use { out ->
                        val bmOptions = BitmapFactory.Options()
                        val bmp = BitmapFactory.decodeFile(filePath, bmOptions)
                        if (out != null) {
                            bmp.compress(Bitmap.CompressFormat.JPEG, 90, out)
                        }
                        bmp.recycle()
                    }
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    values.clear()
                    values.put(if (isVideo) MediaStore.Video.Media.IS_PENDING else MediaStore.Images.Media.IS_PENDING, 0)
                }

                context.contentResolver.update(fileUri, values, null, null)
            }
        }
    }
}
