package com.example.haha

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.room.Room
import com.zynksoftware.documentscanner.ScanActivity
import com.zynksoftware.documentscanner.model.DocumentScannerErrorModel
import com.zynksoftware.documentscanner.model.ScannerResults
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

// This is the camera view logic
class AppScanActivity: ScanActivity() {
    var film: Film? = null
    private var sharedPrefs: String="SHARED_PREFERENCES"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.app_scan_activity_layout)
        addFragmentContentLayout()
    }

    //handle error
    override fun onError(error: DocumentScannerErrorModel) {
        Log.v("Error",error.toString())
    }
    //this function handle processing bitmap and navigating to detail page activity
    override fun onSuccess(scannerResults: ScannerResults) {
        var intent= Intent(this, DetailActivity::class.java)
        var b = BitmapFactory.decodeFile(scannerResults.croppedImageFile?.getAbsolutePath())

        var sharedPreferences=getSharedPreferences(sharedPrefs, MODE_PRIVATE)
        var temp=sharedPreferences.getInt("id",1)

        val filename = "${temp}.jpg"
        var fos: OutputStream? = null
        var imageUri:Uri?=null
        //For devices running android >= Q
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            //getting the contentResolver
            applicationContext?.contentResolver?.also { resolver ->

                //Content resolver will process the contentvalues
                val contentValues = ContentValues().apply {

                    //putting file information in content values
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }

                //Inserting the contentValues to contentResolver and getting the Uri
                imageUri =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

                //Opening an outputstream with the Uri that we got
                fos = imageUri?.let { resolver.openOutputStream(it) }
            }
        } else {
            //These for devices running on android < Q
            //So I don't think an explanation is needed here
            val imagesDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDir, filename)
            fos = FileOutputStream(image)
            imageUri=Uri.fromFile(getFileStreamPath(filename))
        }

        fos?.use {
            //Finally writing the bitmap to the output stream that we opened
            //invert(b)
            //invert bitmap color is in development
            b.compress(Bitmap.CompressFormat.JPEG, 100, it)
        }
        //launch details activity for the user to input the details
        film=Film(temp,imageUri.toString(), "",null)
        intent.putExtra("film",film)
        startForResult.launch(intent)
    }

    //this function handle closing the cameraview
    override fun onClose() {
        //after user inputing the details, return the data class to the main activity that has recycler list display all the films
        val i = intent.apply {
            putExtra("film", film)
            //put boolean add to indicate the film in this parcel is new one that needed to be added to the list
            putExtra("add",true)
        }
        //store the film in the database
        val db = Room.databaseBuilder(this, FilmDatabase::class.java, "FilmDB").allowMainThreadQueries().build()
        val filmDao = db.filmDao()
        film?.let { filmDao.insert(it) }
        db.close()

        setResult(Activity.RESULT_OK, i)
        finish()
    }

    //this function handle getting user input from detail activity
    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        when (result.resultCode) {
            RESULT_OK -> {
                val data = result.data
                if (data != null) {
                    //handle storing tags for the films
                    film=data.getParcelableExtra<Film>("film")
                    val db = Room.databaseBuilder(this, TagDatabase::class.java, "TagDB").allowMainThreadQueries().build()
                    val tagDao = db.tagDao()
                    var tags=data.getStringExtra("tag")?.split(" ")
                    tags?.forEach{
                        tagDao.insert(Tag(uid=film?.id!!, tag=it))
                    }
                    db.close()
                    onClose()
                }
            }
        }
    }


    
}