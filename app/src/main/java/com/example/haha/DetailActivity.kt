package com.example.haha

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room

class DetailActivity: AppCompatActivity() {
    var film: Film? = null
    var remove_bool=false
    var tags:String=""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        val filmType = findViewById<EditText>(R.id.filmType)
        val ISO = findViewById<EditText>(R.id.ISO)
        val image= findViewById<ImageView>(R.id.imageView2)
        val id = findViewById<TextView>(R.id.textView3)
        val tag=findViewById<EditText>(R.id.Tag)
        film = intent.getParcelableExtra<Film>("film")
        film?.let {
            //set the input field with text from parcel and imageview with uri
            filmType.setText(it.film_type)
            if (it.ISO!=null){
                ISO.setText(it.ISO.toString())
            }
            else ISO.setText("")
            val b = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(it?.imageUri))
            image.setImageBitmap(b)
            id.text=it.id.toString()
            //set tags input text by getting it directly from tags database
            val db = Room.databaseBuilder(this, TagDatabase::class.java, "TagDB").allowMainThreadQueries().build()
            val tagDao = db.tagDao()
            var listTags=tagDao.findByuid(it.id)
            var tags=""
            if (listTags.isNotEmpty()) {
                tags=listTags[0]
                for(i in 1..listTags.size-1)
                {
                    tags+=" "+listTags[i]
                }
            }
            tag.setText(tags)
            db.close()
        }
        //handle save button
        val button = findViewById<Button>(R.id.save)
        button.setOnClickListener() {
            var bool = true
            //validate film)_type is not empty
            if (filmType.text.toString().length == 0) {
                filmType.setError("Film Type is required")
                bool = false
            }
            //validate ISO is numeric
            if (!ISO.text.toString().matches("^[0-9]+$".toRegex())) {
                ISO.setError("ISO should be integer")
                bool = false
            }
            //if both of the validation success, save the user input
            if (bool) {
                film?.film_type = filmType.text.toString()
                film?.ISO = ISO.text.toString().toInt()
                tags=tag.text.toString()
                val toast = Toast.makeText(applicationContext, "Information Saved", Toast.LENGTH_LONG)
                toast.show()
                onBackPressed()
            }
        }
        //handle delete button
        val delete=findViewById<ImageView>(R.id.delete)
        delete.setOnClickListener(){
            //set remove_bool as true to return it later and notify main activity
            remove_bool=true
            val toast = Toast.makeText(applicationContext, "Deleted", Toast.LENGTH_LONG)
            toast.show()
            onBackPressed()
        }
    }
    //this is handling go back to previous activity
    override fun onBackPressed() {
        //not allowing going back if the validation is not correct
        if (film?.film_type!="" && film?.ISO.toString().matches("^[0-9]+$".toRegex())) {
            val i = intent.apply {
                putExtra("film", film)
                //this indicate whether or not main activity will delete this film
                putExtra("delete", remove_bool)
                putExtra("tag", tags)
            }
            setResult(Activity.RESULT_OK, i)
            super.onBackPressed()
        }
        else {
            val toast = Toast.makeText(applicationContext, "Please enter details", Toast.LENGTH_LONG)
            toast.show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu,menu)
        return super.onCreateOptionsMenu(menu)
    }
}