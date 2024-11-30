package com.dicoding.asclepius.adapter

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dicoding.asclepius.R
import com.dicoding.asclepius.api.ArticlesItem

class NewsAdapter(private val articles: List<ArticlesItem>) :
    RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    inner class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.newsTitle)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.newsDescription)
        private val imageView: ImageView = itemView.findViewById(R.id.newsImage)

        fun bind(article: ArticlesItem) {
            titleTextView.text = article.title
            descriptionTextView.text = article.description
            Glide.with(itemView.context)
                .load(article.urlToImage)
                .placeholder(R.drawable.ic_place_holder)
                .into(imageView)

            itemView.setOnClickListener {
                article.url?.let { url ->
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    itemView.context.startActivity(intent)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_news, parent, false)
        return NewsViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        holder.bind(articles[position])
    }

    override fun getItemCount() = articles.size
}

