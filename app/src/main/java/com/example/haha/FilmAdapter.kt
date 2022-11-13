package com.example.haha

import android.net.Uri
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

//adapter for film class recycler view
class FilmAdapter(private var filmlist: MutableList<Film?>, private val listener:(Film)->Unit)
    : RecyclerView.Adapter<FilmAdapter.FilmHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilmAdapter.FilmHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater
            .inflate(R.layout.film, parent, false) as View
        return FilmHolder(view)
    }
    inner class FilmHolder(private val v: View): RecyclerView.ViewHolder(v) {
        private val image: ImageView = v.findViewById(R.id.imageView)
        private val detail: TextView = v.findViewById(R.id.detail)
        fun bind(item: Film?) {
            //get bitmap from uri
            val b = MediaStore.Images.Media.getBitmap(v.context.getContentResolver(), Uri.parse(item?.imageUri))
            image.setImageBitmap(b)
            detail.text=item?.film_type+" "+item?.ISO
            v.setOnClickListener {
                if (item != null) {
                    //listener that move to detail activity
                    listener(item)
                }
            }
        }
    }
    override fun onBindViewHolder(holder: FilmHolder, position: Int) {
        val item = filmlist[position]
        holder.bind(item)
    }


    override fun getItemCount() = filmlist.size
}