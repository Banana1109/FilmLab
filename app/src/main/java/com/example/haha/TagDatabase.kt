package com.example.haha

import android.content.Context
import android.os.Parcelable
import androidx.room.*
import kotlinx.android.parcel.Parcelize

@Entity
data class Tag(
    @PrimaryKey(autoGenerate = true) val id:Int=0,
    @ColumnInfo(name = "uid") var uid: Int,
    @ColumnInfo(name = "tag") var tag: String?,
    )

@Dao
interface TagDao {
    //get all rows and collumns
    @Query("SELECT * FROM Tag")
    fun getAll(): List<Tag>
    //get list of film id from tags
    @Query("SELECT uid FROM Tag WHERE tag IN (:tags)")
    fun findByTag(tags: List<String>): List<Int>
    //get tags from film id
    @Query("SELECT tag FROM Tag WHERE uid =:uid")
    fun findByuid(uid: Int): List<String>
    //get the row id from film id and tag name
    @Query("SELECT id FROM Tag WHERE uid =:uid and tag=:tag")
    fun getId(uid: Int, tag: String): List<Int>
    //insert into the tag database
    @Insert
    fun insert(vararg tag: Tag)
    //delete the tag, currently not in use
    @Delete
    fun delete(tag: Tag)
}

@Database(entities = [Tag::class], version = 1)
abstract class TagDatabase : RoomDatabase() {
    abstract fun tagDao(): TagDao
}