package com.example.haha

import android.content.Context
import android.os.Parcelable
import androidx.room.*
import kotlinx.android.parcel.Parcelize

@Entity @Parcelize
data class Film(
    @PrimaryKey var id: Int,
    @ColumnInfo(name = "ImageUri") var imageUri: String,
    @ColumnInfo(name = "film_type") var film_type: String?,
    @ColumnInfo(name = "ISO") var ISO: Int?,

):Parcelable

@Dao
interface FilmDao {
    //get all rows from the database
    @Query("SELECT * FROM Film")
    fun getAll(): List<Film>
    //get list of films by their ids
    @Query("SELECT * FROM Film WHERE id IN (:userIds)")
    fun loadAllByIds(userIds: List<Int>): List<Film>
    //get film by its id
    @Query("SELECT * FROM Film WHERE id = :userId")
    fun loadById(userId: Int): Film
    //get list of film id by comparing the input string with film type and ISO
    @Query("SELECT id FROM Film WHERE film_type LIKE :string OR ISO LIKE :string")
    fun findByString(string: String): List<Int>
    //get the last inputted film
    @Query("SELECT * FROM Film ORDER BY id DESC LIMIT 1;")
    fun findLatest(): Film
    //update an old film with new datas
    @Query("UPDATE Film SET film_type =:film_type , ISO= :ISO WHERE id = :id;")
    fun Update(id:Int, film_type:String?, ISO: Int?)
    //insert new film into database
    @Insert
    fun insert(vararg films: Film)
    //delete film from database
    @Delete
    fun delete(film: Film)
}

@Database(entities = [Film::class], version = 1)
abstract class FilmDatabase : RoomDatabase() {
    abstract fun filmDao(): FilmDao
    companion object{
        @Volatile
        private var INSTANCE: FilmDatabase?=null
        //this is to make sure there is only on instance of database, currently not in use
        fun getDatabase(context: Context): FilmDatabase{
            return INSTANCE?: synchronized(this){
                val instances:FilmDatabase= Room.databaseBuilder(context.applicationContext,FilmDatabase::class.java, "Films").build()
                INSTANCE =instances
                instances
            }
        }
    }
}