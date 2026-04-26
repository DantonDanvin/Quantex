package com.example.quantex.fragment

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.quantex.R
import com.example.quantex.crypto.coinsList.Coinvalue
import com.example.quantex.crypto.RetrofitCoinsClient
import com.example.quantex.databinding.FragmentPortfolioBinding
import com.example.quantex.firebaseUser.Users
import com.example.quantex.portfolio.Portfolio
import com.example.quantex.portfolio.PortfolioAdapter
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.Calendar

class FragPortfolio : Fragment() {

    private lateinit var binding: FragmentPortfolioBinding
    private lateinit var swipeToRefresh: SwipeRefreshLayout
    private lateinit var shimmerFrameLayout: ShimmerFrameLayout
    private lateinit var recyclerView: RecyclerView
    private val portfolioItems = ArrayList<Portfolio>()
    private lateinit var adapter: PortfolioAdapter
    private lateinit var db: FirebaseFirestore
    private lateinit var database: FirebaseDatabase
    private lateinit var reference: DatabaseReference
    private var userId: String = ""
    private var temp: Int = 0
    private var Sdebit: String = ""
    private var portfolio = mutableMapOf<String, Any>()
    private val buyPrice = ArrayList<String>()
    private val quantity = ArrayList<String>()
    private val coinUuid = ArrayList<String>()
    private val transactionID = ArrayList<String>()
    private val userFunds = arrayOf("")
    private val totalTransactions = arrayOf("")
    private val positiveTransactions = arrayOf("")
    private val negativeTransactions = arrayOf("")
    val apidata: MutableMap<String, Array<String>> = mutableMapOf()
    private var loadJob: Job? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentPortfolioBinding.inflate(inflater, container, false)
        shimmerFrameLayout = binding.portfolioShimmer
        db = FirebaseFirestore.getInstance()
        recyclerView = binding.recyclerviewport
        swipeToRefresh = binding.swipeRefreshLayout

        val currentUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
        currentUser?.let { userId = it.uid }

        swipeToRefresh.setOnRefreshListener {
            binding.body.visibility = View.INVISIBLE
            loadPortfolio()
            swipeToRefresh.isRefreshing = false
        }

        binding.body.visibility = View.INVISIBLE
        loadPortfolio()

        return binding.root
    }

    fun loadPortfolio() {
        shimmerFrameLayout.startShimmer()
        shimmerFrameLayout.visibility = View.VISIBLE
        binding.body.visibility = View.INVISIBLE

        FirebaseDatabase.getInstance().getReference("Users").child(userId).child("Funds")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    var i = 0
                    for (snapshot in dataSnapshot.children) {
                        if (i == 0) userFunds[0] = snapshot.value.toString()
                        i++
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT).show()
                }
            })

        FirebaseDatabase.getInstance().getReference("Users").child(userId).child("Transactions")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    var i = 0
                    for (snapshot in dataSnapshot.children) {
                        when (i) {
                            0 -> negativeTransactions[0] = snapshot.value.toString()
                            1 -> positiveTransactions[0] = snapshot.value.toString()
                            2 -> totalTransactions[0] = snapshot.value.toString()
                        }
                        i++
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT).show()
                }
            })

        portfolio.clear()
        db.collection("Users").document(userId).collection("Portfolio")
            .whereEqualTo("flag", "true").get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {
                        portfolio[document.id] = document.data
                    }
                }
            }

        // API call with proper coroutine
        queryData()

        buyPrice.clear(); quantity.clear(); transactionID.clear(); coinUuid.clear()
        loadJob?.cancel()
        loadJob = viewLifecycleOwner.lifecycleScope.launch {
            delay(3000)
            for ((_, value) in portfolio) {
                val portfolioData = value as Map<String, Any>
                buyPrice.add(portfolioData["Buy Price"] as String)
                quantity.add(portfolioData["Quantity"] as String)
                transactionID.add(portfolioData["transaction_ID"] as String)
                coinUuid.add(portfolioData["uuid"] as String)
            }
            if (coinUuid.isNotEmpty()) {
                portfolioItem()
                binding.nothingimg.visibility = View.GONE
                binding.nothingtext.visibility = View.GONE
            } else {
                shimmerFrameLayout.stopShimmer()
                portfolioItem()
                shimmerFrameLayout.visibility = View.GONE
                binding.body.visibility = View.VISIBLE
                binding.nothingimg.visibility = View.VISIBLE
                binding.nothingtext.visibility = View.VISIBLE
            }
        }
    }

    private fun queryData() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitCoinsClient.instance.getCoinsData("https://api.coinranking.com/v2/coins").execute()
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        setCoinData(response.body()?.data?.coins)
                    } else {
                        delay(3000)
                        queryData()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setCoinData(coinsList: List<Coinvalue>?) {
        coinsList?.let {
            for (coinObject in coinsList) {
                val price = coinObject.price.toDouble()
                apidata[coinObject.uuid] = arrayOf(coinObject.name, coinObject.symbol, String.format("%.2f", price))
            }
        }
    }

    private fun portfolioItem() {
        shimmerFrameLayout.stopShimmer()
        shimmerFrameLayout.visibility = View.GONE
        binding.body.visibility = View.VISIBLE

        temp = coinUuid.size
        portfolioItems.clear()

        for (i in 1..temp) {
            val values = apidata[coinUuid[i - 1]]
            val coinName = "${values?.get(0)}"
            val coinFullName = "${values?.get(0)}"
            val symbol = "${values?.get(1)}"
            var priceStr = "${values?.get(2)}"
            val dotIndex0 = priceStr.indexOf('.')
            if (dotIndex0 != -1) priceStr = priceStr.substring(0, dotIndex0 + 3)
            val price = priceStr.toDouble()
            val formattedPrice = String.format("%.2f", price)

            val quantityDouble = quantity[i - 1].toDouble()
            val pricePerUnitDouble = buyPrice[i - 1].toDouble()
            val total = quantityDouble * pricePerUnitDouble
            var totalString = total.toString()
            val dotIndex1 = totalString.indexOf('.')
            if (dotIndex1 != -1) totalString = totalString.substring(0, dotIndex1 + 2)

            var gainAndLoss: String
            val pandl = price - pricePerUnitDouble
            val totalPandl = pandl * quantityDouble
            var totalPandlString = totalPandl.toString()
            val dotIndex2 = totalPandlString.indexOf('.')
            if (dotIndex2 != -1) totalPandlString = totalPandlString.substring(0, dotIndex2 + 2)
            if (price < pricePerUnitDouble) {
                totalPandlString = totalPandlString.substring(1, dotIndex2 + 2)
                gainAndLoss = "Loss"
            } else {
                gainAndLoss = "Gain"
            }

            portfolioItems.add(Portfolio("$coinName - $symbol", coinFullName, symbol, formattedPrice, buyPrice[i - 1], quantity[i - 1], "$$totalString", totalPandlString, gainAndLoss, transactionID[i - 1], coinUuid[i - 1]))
        }

        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = PortfolioAdapter(portfolioItems, object : PortfolioAdapter.OnItemClickListener {
            override fun onItemClick(portfolio: Portfolio) {
                showSellDialog(portfolio)
            }
        })
        recyclerView.adapter = adapter
    }

    private fun showSellDialog(portfolio: Portfolio) {
        val builder = context?.let { AlertDialog.Builder(it, com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog) }
        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflate(R.layout.sellcoindialog, null)
        builder?.setView(dialogView)
        val dialog = builder?.create()

        dialogView.findViewById<TextView>(R.id.textViewDialogTitle).text = "Sell Coin"
        dialogView.findViewById<TextView>(R.id.coinname).text = portfolio.coinFullName
        dialogView.findViewById<TextView>(R.id.currentprice).text = "$${portfolio.currentPrice}"
        dialogView.findViewById<TextView>(R.id.funds).text = "$${userFunds[0]}"
        dialogView.findViewById<TextView>(R.id.coinsavailable).text = portfolio.quantity

        val coinSell = dialogView.findViewById<EditText>(R.id.noofcoin)
        coinSell.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val inputText = coinSell.text.toString().trim()
                val totalDebit = dialogView.findViewById<TextView>(R.id.totaldebit)
                if (inputText.isNotEmpty()) {
                    val debit = portfolio.currentPrice.toDouble() * inputText.toDouble()
                    Sdebit = Math.round(debit).toString()
                    totalDebit.text = "$$Sdebit"
                } else {
                    totalDebit.text = "Enter Quantity"
                }
            }
        })

        val sellButton = dialogView.findViewById<Button>(R.id.sell)
        sellButton.setOnClickListener {
            val inputText = coinSell.text.toString().trim()
            when {
                inputText.isEmpty() -> { Toast.makeText(context, "Enter Quantity", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
                inputText.toInt() == 0 -> { Toast.makeText(context, "0 not allowed", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
                inputText.toInt() > portfolio.quantity.toInt() -> { Toast.makeText(context, "Can't Sell more than available coins", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            }

            val userQuantity = inputText.toInt()
            val totalQuantity = portfolio.quantity.toInt()
            if (userQuantity <= totalQuantity) {
                val newFunds = (userFunds[0].toInt() + Sdebit.toInt()).toString()
                database = FirebaseDatabase.getInstance()
                reference = database.getReference("Users")
                reference.child(userId).child("Funds").setValue(Users(newFunds))

                val newQuantity = totalQuantity - userQuantity
                val updatePortfolio = mutableMapOf<String, Any>(
                    "Quantity" to newQuantity.toString(), "Buy Price" to portfolio.buyPrice,
                    "uuid" to portfolio.coinUuid, "transaction_ID" to portfolio.transactionId,
                    "flag" to if (newQuantity <= 0) "false" else "true"
                )
                db.collection("Users").document(userId).collection("Portfolio").document(portfolio.transactionId).set(updatePortfolio)

                val calendar = Calendar.getInstance()
                val date = SimpleDateFormat("dd/MM/yyyy").format(calendar.time)
                val totalPnl = portfolio.totalPandLString.toDouble()
                val onePnl = totalPnl / totalQuantity
                val totalPnlValue = userQuantity * onePnl
                val transaction = mutableMapOf<String, Any>(
                    "Quantity" to inputText, "Price per Coin" to portfolio.currentPrice,
                    "Transaction Date" to date, "BuyOrSell" to "Sell",
                    "CoinName" to portfolio.coinFullName, "CoinSymbol" to portfolio.symbol,
                    "totalpandl" to "$$totalPnlValue", "gainORloss" to portfolio.gainAndLoss, "flag" to "true"
                )
                db.collection("Users").document(userId).collection("Transaction").document(totalTransactions[0]).set(transaction)

                var currentTotalTransactions = totalTransactions[0].toInt()
                currentTotalTransactions++
                if (portfolio.gainAndLoss == "Loss") {
                    negativeTransactions[0] = (negativeTransactions[0].toInt() + 1).toString()
                } else {
                    positiveTransactions[0] = (positiveTransactions[0].toInt() + 1).toString()
                }
                totalTransactions[0] = currentTotalTransactions.toString()
                reference.child(userId).child("Transactions").setValue(Users(totalTransactions[0], positiveTransactions[0], negativeTransactions[0], true))

                Toast.makeText(context, "Sell successful", Toast.LENGTH_SHORT).show()
                dialog?.dismiss()
                loadPortfolio()
            }
        }
        dialog?.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        loadJob?.cancel()
    }
}
