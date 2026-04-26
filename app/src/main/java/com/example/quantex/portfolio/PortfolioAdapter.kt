package com.example.quantex.portfolio

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.quantex.R

class PortfolioAdapter(
    private val portfolio: ArrayList<Portfolio>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<PortfolioAdapter.PortfolioViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PortfolioViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.portfoliotemplate, parent, false)
        return PortfolioViewHolder(view)
    }

    override fun onBindViewHolder(holder: PortfolioViewHolder, position: Int) {
        val portfolioItem = portfolio[position]
        holder.bind(portfolioItem, listener)
        fadeInAnimation(holder.itemView)
    }

    override fun getItemCount(): Int = portfolio.size

    interface OnItemClickListener {
        fun onItemClick(portfolio: Portfolio)
    }

    class PortfolioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameAndsymbol: TextView = itemView.findViewById(R.id.nameAndsymbol)
        private val currentprice: TextView = itemView.findViewById(R.id.currentprice)
        private val buyprice: TextView = itemView.findViewById(R.id.buyprice)
        private val quantity: TextView = itemView.findViewById(R.id.quantity)
        private val investment: TextView = itemView.findViewById(R.id.investment)
        private val pnl: TextView = itemView.findViewById(R.id.pnl)
        private val totalpnl: TextView = itemView.findViewById(R.id.totalpandl)

        fun bind(portfolio: Portfolio, listener: OnItemClickListener) {
            nameAndsymbol.text = portfolio.nameAndSymbol
            nameAndsymbol.isSelected = true
            currentprice.text = "$${portfolio.currentPrice}"
            buyprice.text = "$${portfolio.buyPrice}"
            quantity.text = portfolio.quantity
            investment.text = portfolio.investment
            totalpnl.text = "$${portfolio.totalPandLString}"
            pnl.text = portfolio.gainAndLoss

            val price = portfolio.currentPrice.toDouble()
            val pricePerUnit = portfolio.buyPrice.toDouble()

            if (price < pricePerUnit) {
                val lossColor = ContextCompat.getColor(itemView.context, R.color.loss)
                pnl.setTextColor(lossColor)
                pnl.setBackgroundResource(R.drawable.loss_badge_bg)
                totalpnl.setTextColor(lossColor)
            } else {
                val gainColor = ContextCompat.getColor(itemView.context, R.color.gain)
                pnl.setTextColor(gainColor)
                pnl.setBackgroundResource(R.drawable.gain_badge_bg)
                totalpnl.setTextColor(gainColor)
            }

            itemView.setOnClickListener { listener.onItemClick(portfolio) }
        }
    }

    companion object {
        fun fadeInAnimation(view: View) {
            val anim = AlphaAnimation(0.0f, 1.0f).apply { duration = 500 }
            view.startAnimation(anim)
        }
    }
}