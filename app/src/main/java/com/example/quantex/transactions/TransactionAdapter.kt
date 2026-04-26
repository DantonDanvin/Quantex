package com.example.quantex.transactions

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.quantex.R

class TransactionAdapter(
    private val transactions: ArrayList<Transaction>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(transaction: Transaction)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.transactiontemplate, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.bind(transaction, listener)
        fadeInAnimation(holder.itemView)
    }

    override fun getItemCount(): Int = transactions.size

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameAndsymbol: TextView = itemView.findViewById(R.id.nameAndsymbol)
        private val buyOrsell: TextView = itemView.findViewById(R.id.buyOrsell)
        private val date: TextView = itemView.findViewById(R.id.date)
        private val pricePerShare: TextView = itemView.findViewById(R.id.pricePerShare)
        private val quantity: TextView = itemView.findViewById(R.id.quantity)
        private val transactiontotal: TextView = itemView.findViewById(R.id.transactiontotal)
        private val pnl: TextView = itemView.findViewById(R.id.pnl)
        private val totalpandl: TextView = itemView.findViewById(R.id.totalpandl)

        fun bind(transaction: Transaction, listener: OnItemClickListener) {
            nameAndsymbol.text = transaction.nameAndSymbol
            nameAndsymbol.isSelected = true
            date.text = transaction.date
            pricePerShare.text = transaction.pricePerShare
            quantity.text = transaction.quantity
            transactiontotal.text = transaction.transactionTotal
            pnl.text = transaction.gainOrLoss
            totalpandl.text = transaction.totalPandL

            val context = itemView.context
            val gainColor = ContextCompat.getColor(context, R.color.gain)
            val lossColor = ContextCompat.getColor(context, R.color.loss)

            // Buy/Sell badge styling
            if (transaction.buyOrSell.startsWith('B')) {
                buyOrsell.text = "Buy"
                buyOrsell.setTextColor(gainColor)
                buyOrsell.setBackgroundResource(R.drawable.gain_badge_bg)
            } else {
                buyOrsell.text = "Sell"
                buyOrsell.setTextColor(lossColor)
                buyOrsell.setBackgroundResource(R.drawable.loss_badge_bg)
            }

            // P&L coloring for sell transactions
            if (transaction.buyOrSell.startsWith('S') && transaction.gainOrLoss.startsWith('G')) {
                pnl.setTextColor(gainColor)
                totalpandl.setTextColor(gainColor)
            }
            if (transaction.buyOrSell.startsWith('S') && transaction.gainOrLoss.startsWith('L')) {
                pnl.setTextColor(lossColor)
                totalpandl.setTextColor(lossColor)
            }

            itemView.setOnClickListener { listener.onItemClick(transaction) }
        }
    }

    companion object {
        fun fadeInAnimation(view: View) {
            val anim = AlphaAnimation(0.0f, 1.0f).apply { duration = 500 }
            view.startAnimation(anim)
        }
    }
}
