package com.example.quantex.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.quantex.databinding.FragmentTransactionsBinding
import com.example.quantex.transactions.Transaction
import com.example.quantex.transactions.TransactionAdapter
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.roundToLong

class FragTransactions : Fragment() {

    private lateinit var binding: FragmentTransactionsBinding
    private lateinit var swipeToRefresh: SwipeRefreshLayout
    private lateinit var shimmerFrameLayout: ShimmerFrameLayout
    private lateinit var recyclerView: RecyclerView
    private val transactionItem = ArrayList<Transaction>()
    private lateinit var adapter: TransactionAdapter
    private lateinit var db: FirebaseFirestore
    private var userId: String = ""
    private var coinName: String = ""
    private val transaction = mutableMapOf<String, Any>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTransactionsBinding.inflate(inflater, container, false)

        // find shimmer.
        shimmerFrameLayout = binding.transactionShimmer
        shimmerFrameLayout.startShimmer()
        shimmerFrameLayout.visibility = View.VISIBLE
        swipeToRefresh = binding.swipeRefreshLayout

        db = FirebaseFirestore.getInstance()

        recyclerView = binding.recyclerviewtrans

        // Get current user ID
        val currentUser = FirebaseAuth.getInstance().currentUser
        userId = currentUser?.uid ?: "" // get user UID


        // reload Transaction data.
        swipeToRefresh.setOnRefreshListener {
            shimmerFrameLayout.startShimmer()
            shimmerFrameLayout.visibility = View.VISIBLE
            binding.body.visibility = View.INVISIBLE
            loadTransactionData()
            swipeToRefresh.isRefreshing = false
        }

        binding.body.visibility = View.INVISIBLE
        loadTransactionData()

        return binding.root
    }

    private fun loadTransactionData() {

        db.collection("Users").document(userId).collection("Transaction")
            .whereEqualTo("flag", "true")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    transaction.clear()
                    for (document in task.result) {
                        transaction[document.id] = document.data
                    }
                }
                else {
                    Toast.makeText(requireContext(), "${task.exception}", Toast.LENGTH_SHORT).show()
                }
            }

        Handler(Looper.getMainLooper()).postDelayed({
            shimmerFrameLayout.stopShimmer()
            shimmerFrameLayout.visibility = View.GONE
            binding.body.visibility = View.VISIBLE

            if(transaction.isEmpty()){
                binding.nothingimg.visibility = View.VISIBLE
                binding.nothingtext.visibility = View.VISIBLE
            }
            else {
                binding.nothingimg.visibility = View.GONE
                binding.nothingtext.visibility = View.GONE
            }

            transactionItem.clear()

            for ((transactionId, transactionData) in transaction) {
                @Suppress("UNCHECKED_CAST")
                val data = transactionData as Map<String, Any>
                val symbol = data["CoinSymbol"] as String
                var nameAndSymbol = data["CoinName"] as String
                coinName = data["CoinName"] as String
                nameAndSymbol += " - $symbol"
                val transactionType = data["BuyOrSell"] as String
                val gainOrLoss = data["gainORloss"] as String
                var totalPandL = data["totalpandl"] as String
                val dotIndex1 = totalPandL.indexOf('.')
                if (dotIndex1 != -1) {
                    totalPandL = totalPandL.substring(0, dotIndex1 + 2)
                }
                val date = data["Transaction Date"] as String
                val pricePerUnit = data["Price per Coin"] as String
                val quantity = data["Quantity"] as String
                val quantityDouble = quantity.toDouble()
                val pricePerUnitDouble = pricePerUnit.toDouble()
                val total = quantityDouble * pricePerUnitDouble
                val roundedDebit = total.roundToLong()
                val totalString = roundedDebit.toString()

                transactionItem.add(Transaction(nameAndSymbol, transactionType, date, "$$pricePerUnit", quantity, "$$totalString", coinName, gainOrLoss, totalPandL))
            }

            // Set up RecyclerView
            recyclerView.layoutManager = LinearLayoutManager(context)
            adapter = TransactionAdapter(transactionItem, object : TransactionAdapter.OnItemClickListener {
                override fun onItemClick(transaction: Transaction) {   // on recycler item select.
                    Toast.makeText(requireContext(), transaction.coinName, Toast.LENGTH_SHORT).show()
                }
            })
            recyclerView.adapter = adapter


        }, 3000)

    }


}
