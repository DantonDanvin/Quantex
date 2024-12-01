package com.example.quantex.crypto.coinsList

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.example.quantex.R

class CryptoCoinAdapter(
    private var coins: ArrayList<Coin>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<CryptoCoinAdapter.CoinViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(coin: Coin)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CoinViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cryptocointemplate, parent, false)
        return CoinViewHolder(view)
    }

    override fun onBindViewHolder(holder: CoinViewHolder, position: Int) {
        val coin = coins[position]
        holder.bind(coin, listener)
        fadeInAnimation(holder.itemView) // show recycler item in animation.
    }

    override fun getItemCount(): Int {
        return coins.size
    }

    // search filter data show
    fun setCoins(filteredCoins: ArrayList<Coin>) {
        coins = filteredCoins
        notifyDataSetChanged()
    }


    class CoinViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val name: TextView = itemView.findViewById(R.id.name)
        private val fullName: TextView = itemView.findViewById(R.id.fullname)
        private val currentPrice: TextView = itemView.findViewById(R.id.current_price)
        private val change: TextView = itemView.findViewById(R.id.change)
        private val src: ImageView = itemView.findViewById(R.id.symbol)

        fun bind(coin: Coin, listener: OnItemClickListener) {
            val imageUrl = coin.icon

            // SVG image.
//            val lastSecondChar = if (imageUrl.length >= 2) imageUrl[imageUrl.length - 2] else null
//            if(lastSecondChar=='v' || lastSecondChar=='V' || lastSecondChar=='4') {
//                src.load(imageUrl){
//                    decoderFactory { result, options, _ -> SvgDecoder(result.source, options) }
//                        .placeholder(R.drawable.loading)
//                        .error(R.drawable.loading)
//                }
//            }
//            if(lastSecondChar=='n' || lastSecondChar=='N') {
////                // PNG image.
//                Glide.with(itemView.context)
//                    .load(imageUrl)
//                    .apply(RequestOptions.centerCropTransform())
//                    .placeholder(R.drawable.loading)
//                    .error(R.drawable.loading)
//                    .into(src)
//            }

            // it'll load both PNG and SVG.
            val imageLoader = ImageLoader.Builder(itemView.context)
                .components { add(SvgDecoder.Factory()) }
                .build()
            val request = ImageRequest.Builder(itemView.context)
                .crossfade(true)
                .crossfade(500)
                .placeholder(R.drawable.loading)
                .error(R.drawable.loading)
                .data(imageUrl)
                .target(src)
                .build()
            imageLoader.enqueue(request)



            name.text = coin.name
            fullName.text = coin.fullname
            fullName.setSelected(true);
            currentPrice.text = coin.currentPrice
            change.text = coin.change

            val changeValue = coin.change?.toDoubleOrNull() ?: 0.0
            val color = if (changeValue < 0) {
                ContextCompat.getColor(itemView.context, R.color.red)
            } else {
                ContextCompat.getColor(itemView.context, R.color.green)
            }
            currentPrice.setTextColor(color)

            // Set item click listener
            itemView.setOnClickListener {
                listener.onItemClick(coin) // this will call the function written in FragCrypto.kt
            }
        }
    }

    companion object {
        // show recycler item in animation.
        fun fadeInAnimation(view: View) {
            val anim = AlphaAnimation(0.0f, 1.0f)
            anim.duration = 800 // Set the duration of the animation in milliseconds
            view.startAnimation(anim)
        }
    }
}
