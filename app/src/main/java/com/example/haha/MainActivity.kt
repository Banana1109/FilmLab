package com.example.haha

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.ImageView
import androidx.appcompat.widget.SearchView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.google.android.material.snackbar.Snackbar
import com.zynksoftware.documentscanner.ui.DocumentScanner
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: FilmAdapter
    private var filmList: MutableList<Film?> = ArrayList()
    private var tempFilm: Film?=null
    private var sharedPrefs: String="SHARED_PREFERENCES"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //config for camera activity
        val configuration = DocumentScanner.Configuration()
        configuration.imageQuality = 100
        configuration.imageSize = 1000000 // 1 MB
        configuration.imageType = Bitmap.CompressFormat.JPEG
        DocumentScanner.init(this, configuration)
        //get films from database
        val db = Room.databaseBuilder(this, FilmDatabase::class.java, "FilmDB").allowMainThreadQueries().build()
        val filmDao = db.filmDao()

        var filmDaoList=filmDao.getAll()
        //add films to a list
        filmDaoList.forEach{
            filmList.add(it)
        }
        db.close()
        //first time guide
        var firstTime=findViewById<ImageView>(R.id.firstTime)
        if (!filmList.isEmpty())
        {
            firstTime.visibility=View.GONE
        }
        else
            firstTime.visibility=View.VISIBLE

        adapter= FilmAdapter(filmList){ showDetail(it)}
        var recyclerView=findViewById<RecyclerView>(R.id.recyclerView)
        //set the layout to 3 collumns
        val mLayoutManager: RecyclerView.LayoutManager = GridLayoutManager(this, 3)
        recyclerView.layoutManager = mLayoutManager
        recyclerView.adapter=adapter
        //handle floating button to open camera view
        val fab: View = findViewById(R.id.fab)
        fab.setOnClickListener { view ->
            var intent2= Intent(this, AppScanActivity::class.java)
            startForResult.launch(intent2)
        }

    }

    private fun showDetail(item: Film?) {
        var intent= Intent(this, DetailActivity::class.java)
        intent.putExtra("film",item)

        startForResult.launch(intent)

    }

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        when (result.resultCode) {
            RESULT_OK -> {
                val data = result.data
                if (data != null) {
                    tempFilm= data.getParcelableExtra<Film>("film")
                    if (tempFilm?.id!! >=0)
                    {
                        //handle adding new films
                        var bool_add=data.getBooleanExtra("add", false)
                        if (bool_add) {
                            //update sharePreference for next film id
                            //update recycler list
                            filmList.add(tempFilm)
                            adapter.notifyDataSetChanged()
                            val sharePreferences= getSharedPreferences(sharedPrefs, MODE_PRIVATE)
                            val editor= sharePreferences.edit()
                            var temp=sharePreferences.getInt("id",1)
                            editor.putInt("id",temp+1)
                            editor.commit()
                            var firstTime=findViewById<ImageView>(R.id.firstTime)
                            //removew first time guide
                            if (!filmList.isEmpty())
                            {
                                firstTime.visibility=View.GONE
                            }


                        }
                        //handle updating film
                        else {
                            //update film
                            val db = Room.databaseBuilder(this, FilmDatabase::class.java, "FilmDB").allowMainThreadQueries().build()
                            val filmDao = db.filmDao()

                            filmDao.Update(tempFilm!!.id,tempFilm!!.film_type, tempFilm!!.ISO)
                            var filmDaoList=filmDao.getAll()
                            filmList.clear()
                            filmDaoList.forEach{
                                filmList.add(it)
                            }
                            db.close()
                            //update tags for films
                            val db2 = Room.databaseBuilder(this, TagDatabase::class.java, "TagDB").allowMainThreadQueries().build()
                            val tagDao = db2.tagDao()
                            var tags=data.getStringExtra("tag")?.split(" ")
                            tags?.forEach{
                                var count=tagDao.getId(uid=tempFilm?.id!!,tag= it)
                                if (count.isEmpty()) {
                                    tagDao.insert(Tag(uid = tempFilm?.id!!, tag = it))
                                }
                            }
                            db2.close()

                            adapter.notifyDataSetChanged()
                        }
                        //handle deleting film
                        var tempBool=data.getBooleanExtra("delete", false)
                        if (tempBool)
                        {
                            val db = Room.databaseBuilder(this, FilmDatabase::class.java, "FilmDB").allowMainThreadQueries().build()
                            val filmDao = db.filmDao()

                            filmDao.delete(tempFilm!!)
                            var filmDaoList=filmDao.getAll()
                            filmList.clear()
                            filmDaoList.forEach{
                                filmList.add(it)
                            }
                            db.close()
                            adapter.notifyDataSetChanged()
                        }

                    }
                }
            }
        }
    }
    //handle menu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu,menu)

        val manager=getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val menuItem=menu?.findItem(R.id.search)
        val searchView=menuItem?.actionView as SearchView

        searchView.setSearchableInfo(manager.getSearchableInfo(componentName))

        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                TODO("Not yet implemented")
            }

            //handling search
            override fun onQueryTextChange(newText: String?): Boolean {

                val searchText=newText!!.toLowerCase(Locale.getDefault())
                if (searchText.isNotEmpty()) {
                    val db =
                        Room.databaseBuilder(applicationContext, FilmDatabase::class.java, "FilmDB")
                            .allowMainThreadQueries().build()
                    val filmDao = db.filmDao()
                    val db2 = Room.databaseBuilder(applicationContext, TagDatabase::class.java, "TagDB").allowMainThreadQueries().build()
                    val tagDao = db2.tagDao()
                    var textList = searchText.split(' ')
                    var filmDaoList:List<Film> = ArrayList()
                    var idList:List<Int> = ArrayList()
                    var idList2:List<Int> = ArrayList()
                    idList+=filmDao.findByString('%' + textList[0] + '%')
                    //find film id with tags
                    idList2=tagDao.findByTag(textList)
                    //find films id with ISO and film type with similar inputted string
                    textList.forEach {
                        idList=idList.intersect(filmDao.findByString('%' + it + '%')).toList()
                    }
                    //combine two list
                    idList2.forEach{
                        if(!(it in idList))
                        {
                            idList+=it
                        }
                    }
                    //reload the film list and update the recycler list
                    filmDaoList=filmDao.loadAllByIds(idList)
                    filmList.clear()
                    filmDaoList.forEach{
                        filmList.add(it)
                    }
                    adapter.notifyDataSetChanged()
                    db.close()
                    db2.close()
                }
                //when the search is empty, return the list with all films
                else{
                    val db = Room.databaseBuilder(applicationContext, FilmDatabase::class.java, "FilmDB").allowMainThreadQueries().build()
                    val filmDao = db.filmDao()

                    var filmDaoList=filmDao.getAll()
                    filmList.clear()
                    filmDaoList.forEach{
                        filmList.add(it)
                    }
                    db.close()
                    adapter.notifyDataSetChanged()
                }
                return false

            }

        })
        return super.onCreateOptionsMenu(menu)
    }
}