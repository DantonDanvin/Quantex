package com.example.quantex.market_news

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.quantex.R
import com.kwabenaberko.newsapilib.models.Article
import com.squareup.picasso.Picasso

class NewsVAdapter(
    private var articles: List<Article>
) : RecyclerView.Adapter<NewsVAdapter.NewsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.newsv_template, parent, false)
        return NewsViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val article = articles[position]
        holder.title.text = article.title
        holder.source.text = article.source.name
        Picasso.get().load(article.urlToImage)
            .error(R.drawable.no_image)
            .into(holder.image)

        holder.itemView.setOnClickListener { v ->
            val intent = Intent(v.context, NewsDetail::class.java)
            intent.putExtra("url", article.url)
            v.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return articles.size
    }

    fun updateData(data: List<Article>) {
        articles = data
        notifyDataSetChanged()
    }

    class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.title)
        val source: TextView = itemView.findViewById(R.id.source)
        val image: ImageView = itemView.findViewById(R.id.artical_image)
    }
}