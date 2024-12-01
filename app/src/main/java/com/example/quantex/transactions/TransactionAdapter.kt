package com.example.quantex.transactions

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.LinearLayout
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
        fadeInAnimation(holder.itemView) // Show recycler item in animation
    }

    override fun getItemCount(): Int {
        return transactions.size
    }

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tranitem: LinearLayout = itemView.findViewById(R.id.transactionItem)
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
            nameAndsymbol.setSelected(true)
            buyOrsell.text = transaction.buyOrSell
            date.text = transaction.date
            pricePerShare.text = transaction.pricePerShare
            quantity.text = transaction.quantity
            transactiontotal.text = transaction.transactionTotal
            pnl.text = transaction.gainOrLoss
            totalpandl.text = transaction.totalPandL

            if (transaction.buyOrSell.startsWith('S') && transaction.gainOrLoss.startsWith('G')) {
                val greenColor = ContextCompat.getColor(itemView.context, R.color.green)
                nameAndsymbol.setBackgroundColor(greenColor)
                val greenColorMY = ContextCompat.getColor(itemView.context, R.color.green_my)
                pnl.setTextColor(greenColorMY)
                totalpandl.setTextColor(greenColorMY)
            }
            if (transaction.buyOrSell.startsWith('S') && transaction.gainOrLoss.startsWith('L')) {
                val redColor = ContextCompat.getColor(itemView.context, R.color.red)
                nameAndsymbol.setBackgroundColor(redColor)
                pnl.setTextColor(redColor)
                totalpandl.setTextColor(redColor)
            }
            if(transaction.buyOrSell.startsWith('B')){
                val params = tranitem.layoutParams
                params.height = 250 // Set the desired height here (in pixels)
                tranitem.layoutParams = params
            }

            // Set item click listener
            itemView.setOnClickListener {
                listener.onItemClick(transaction) // This will call the function written in FragCrypto.kt
            }
        }
    }

    companion object {
        // Show recycler item in animation
        fun fadeInAnimation(view: View) {
            val anim = AlphaAnimation(0.0f, 1.0f)
            anim.duration = 800 // Set the duration of the animation in milliseconds
            view.startAnimation(anim)
        }
    }
}
