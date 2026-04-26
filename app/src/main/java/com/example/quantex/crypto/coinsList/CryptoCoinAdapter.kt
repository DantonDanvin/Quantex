package com.example.quantex.crypto.coinsList

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.example.quantex.R

// Renamed to CryptoCoinAdapter for consistency
class CryptoCoinAdapter(
    private var coins: ArrayList<Coin>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<CryptoCoinAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(coin: Coin)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.symbol)
        val name: TextView = view.findViewById(R.id.name)
        val fullname: TextView = view.findViewById(R.id.fullname)
        val price: TextView = view.findViewById(R.id.current_price)
        val change: TextView = view.findViewById(R.id.change)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cryptocointemplate, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val coin = coins[position]
        val context = holder.itemView.context

        holder.name.text = coin.name
        holder.fullname.text = coin.fullname
        holder.price.text = "$${coin.currentPrice}"

        // Change with gain/loss color
        val changeValue = coin.change.toDoubleOrNull() ?: 0.0
        val lossColor = ContextCompat.getColor(context, R.color.loss)
        val gainColor = ContextCompat.getColor(context, R.color.gain)
        holder.change.text = "${coin.change}%"
        holder.change.setTextColor(if (changeValue < 0) lossColor else gainColor)

        // Coin icon with Coil + SVG support
        val imageLoader = ImageLoader.Builder(context)
            .components { add(SvgDecoder.Factory()) }
            .build()
        val request = ImageRequest.Builder(context)
            .crossfade(true).crossfade(300)
            .placeholder(R.drawable.loading).error(R.drawable.loading)
            .data(coin.icon)
            .target(holder.icon)
            .build()
        imageLoader.enqueue(request)

        holder.itemView.setOnClickListener {
            listener.onItemClick(coin)
        }
    }

    override fun getItemCount(): Int = coins.size

    fun setCoins(filteredCoins: ArrayList<Coin>) {
        this.coins = filteredCoins
        notifyDataSetChanged()
    }
}
