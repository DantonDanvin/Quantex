package com.example.quantex.fragment

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.quantex.R
import com.example.quantex.crypto.coinsList.Coinvalue
import com.example.quantex.crypto.RetrofitCoinsClient
import com.example.quantex.databinding.FragmentPortfolioBinding
import com.example.quantex.firebaseUser.Users
import com.example.quantex.market_news.NewsHAdapter
import com.example.quantex.portfolio.Portfolio
import com.example.quantex.portfolio.PortfolioAdapter
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
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
    private lateinit var database:FirebaseDatabase
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentPortfolioBinding.inflate(inflater, container, false)

        // find shimmer.
        shimmerFrameLayout = binding.portfolioShimmer

        db = FirebaseFirestore.getInstance()

        recyclerView = binding.recyclerviewport
        swipeToRefresh = binding.swipeRefreshLayout

        // get current user ID.
        val currentUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
        currentUser?.let {
            userId = it.uid
        }// get user UID.

        // reload Account data.
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

        // get user data from realtime database.
        FirebaseDatabase.getInstance().getReference("Users").child(userId).child("Funds")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    var i = 0
                    for (snapshot in dataSnapshot.children) {
                        if (i == 0) {
                            userFunds[0] = snapshot.value.toString()
                        }
                        i++
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), error.message , Toast.LENGTH_SHORT).show()
                }
            })

        // User transactions from realtime database.
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
                    Toast.makeText(requireContext(), error.message , Toast.LENGTH_SHORT).show()
                }
            })

        portfolio.clear()
        // get user portfolio data.
        db.collection("Users").document(userId).collection("Portfolio")
            .whereEqualTo("flag", "true")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {
                        portfolio[document.id] = document.data
                    }
                }
            }

        // call api for --> uuid,name,symbol,price.
        try {
            queryData()
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

        buyPrice.clear()
        quantity.clear()
        transactionID.clear()
        coinUuid.clear()
        // data from firebase firestore and set in arraylist.
        Handler(Looper.getMainLooper()).postDelayed({
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
        }, 3000)

    }



    // get coin data. --> uuid,name,symbol,price.
    private fun queryData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitCoinsClient.instance.getCoinsData("https://api.coinranking.com/v2/coins").execute()
                if (response.isSuccessful) {
                    val coinResponse = response.body()
                    withContext(Dispatchers.Main) {
                        setCoinData(coinResponse?.data?.coins)
                    }
                } else {
                    // Handle unsuccessful response
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Exception (on response)", Toast.LENGTH_SHORT).show()
                        onStop()
                        Handler().postDelayed({
                            queryData()
                        }, 2800)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // set data on recyclerView.
    private fun setCoinData(coinsList: List<Coinvalue>?) {
        coinsList?.let {
//            Toast.makeText(context, coinsList[0].toString(), Toast.LENGTH_SHORT).show()
            for (i in coinsList.indices) {
                val coinObject = coinsList[i]
                val uuid = coinObject.uuid
                val symbol = coinObject.symbol
                val name = coinObject.name
                val priceStr = coinObject.price
                val price = priceStr.toDouble()
                val formattedPrice = String.format("%.2f", price)

                apidata[uuid] = arrayOf(name, symbol, formattedPrice)

            }
        }
    }


    private fun portfolioItem() {
        shimmerFrameLayout.stopShimmer()
        shimmerFrameLayout.visibility = View.GONE
        binding.body.visibility = View.VISIBLE

        temp=coinUuid.size
        portfolioItems.clear()
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = PortfolioAdapter(portfolioItems, object : PortfolioAdapter.OnItemClickListener {
            override fun onItemClick(portfolio: Portfolio) {

            }
        })
        recyclerView.adapter = adapter


        for (i in 1..temp){

            val values = apidata[coinUuid[i-1]]

            var coinName = "${values?.get(0)}"
            var coinFullName = "${values?.get(0)}"
            val symbol = "${values?.get(1)}"
            var priceStr = "${values?.get(2)}"
            val dotIndex0 = priceStr.indexOf('.')
            if (dotIndex0 != -1) {
                priceStr = priceStr.substring(0, dotIndex0 + 3)
            }
            val price = priceStr.toDouble()
            val formattedPrice = String.format("%.2f", price)

            val quantityDouble = quantity[i-1].toDouble()
            val pricePerUnitDouble = buyPrice[i-1].toDouble()
            val total = quantityDouble * pricePerUnitDouble
            var totalString = total.toString()
            val dotIndex1 = totalString.indexOf('.')
            if (dotIndex1 != -1) {
                totalString = totalString.substring(0, dotIndex1 + 2)
            }

            var gainAndLoss = ""

            val pandl = price - pricePerUnitDouble
            val totalPandl = pandl * quantityDouble
            var totalPandlString = totalPandl.toString()
            val dotIndex2 = totalPandlString.indexOf('.')
            if (dotIndex2 != -1) {
                totalPandlString = totalPandlString.substring(0, dotIndex2 + 2)
            }
            if (price < pricePerUnitDouble) {
                totalPandlString = totalPandlString.substring(1, dotIndex2 + 2)
                gainAndLoss = "Loss"
            } else {
                gainAndLoss = "Gain"
            }

            portfolioItems.add(Portfolio("$coinName - $symbol", coinFullName , symbol , formattedPrice ,buyPrice[i-1],quantity[i-1],"$$totalString",totalPandlString,gainAndLoss,transactionID[i-1],coinUuid[i-1]))
        }


        // Set up RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = PortfolioAdapter(portfolioItems, object : PortfolioAdapter.OnItemClickListener {
            override fun onItemClick(portfolio: Portfolio) {   // on recycler item select.

                // sell coin dialog.
                val builder = context?.let { AlertDialog.Builder(it) }
                val inflater = LayoutInflater.from(context)
                val dialogView = inflater.inflate(R.layout.sellcoindialog, null)
                builder?.setView(dialogView)
                val dialog = builder?.create()

                val textViewDialogTitle = dialogView.findViewById<TextView>(R.id.textViewDialogTitle)
                textViewDialogTitle.text = "Sell Coin"

                val name = dialogView.findViewById<TextView>(R.id.coinname)
                name.text = portfolio.coinFullName

                val price = dialogView.findViewById<TextView>(R.id.currentprice)
                price.text = "$${portfolio.currentPrice}"

                val funds = dialogView.findViewById<TextView>(R.id.funds)
                funds.text = "$${userFunds[0]}"

                val coinAvailable = dialogView.findViewById<TextView>(R.id.coinsavailable)
                coinAvailable.text = portfolio.quantity

                val coinSell = dialogView.findViewById<EditText>(R.id.noofcoin)
                coinSell.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                    override fun afterTextChanged(s: Editable?) {
                        val inputText = coinSell.text.toString().trim()
                        if (inputText.isNotEmpty()) {
                            val sellCap = inputText.toDouble()
                            val currentPrice = portfolio.currentPrice.toDouble()
                            val debit = currentPrice * sellCap
                            val roundedDebit = Math.round(debit).toString()
                            Sdebit = roundedDebit

                            val totalDebit = dialogView.findViewById<TextView>(R.id.totaldebit)
                            totalDebit.text = "$$Sdebit"
                        } else {
                            val totalDebit = dialogView.findViewById<TextView>(R.id.totaldebit)
                            totalDebit.text = "Enter Quantity"
                        }
                    }
                })

                val sellButton = dialogView.findViewById<Button>(R.id.sell)
                val gainLossColor = when (portfolio.gainAndLoss) {
                    "Gain" -> ContextCompat.getColor(context!!, R.color.green_my)
                    "Loss" -> ContextCompat.getColor(context!!, R.color.red)
                    else -> Color.BLACK
                }
                sellButton.setBackgroundColor(gainLossColor)

                sellButton.setOnClickListener {
                    val inputText = coinSell.text.toString().trim()
                    when {
                        inputText.isEmpty() -> {
                            Toast.makeText(context, "Enter Quantity", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }
                        inputText.toInt() == 0 -> {
                            Toast.makeText(context, "0 not allowed", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }
                        inputText.toInt() > portfolio.quantity.toInt() -> {
                            Toast.makeText(context, "Can't Sell more than available coins", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }
                    }

                    val userQuantity = inputText.toInt()
                    val totalQuantity = portfolio.quantity.toInt()
                    if (userQuantity <= totalQuantity) {


                        // Change balance
                        val currentFunds = userFunds[0].toInt()
                        val proceeds = Sdebit.toInt()
                        val newFunds = (currentFunds + proceeds).toString()
                        val funds = Users(newFunds)
                        database = FirebaseDatabase.getInstance()
                        reference = database.getReference("Users")
                        reference.child(userId).child("Funds").setValue(funds).addOnCompleteListener {
                            Toast.makeText(context, "Available balance: $$newFunds", Toast.LENGTH_SHORT).show()
                        }

                        // Update portfolio on Firestore
                        val updatePortfolio = mutableMapOf<String, Any>()
                        val newQuantity = totalQuantity - userQuantity
                        updatePortfolio["Quantity"] = newQuantity.toString()
                        updatePortfolio["Buy Price"] = portfolio.buyPrice
                        updatePortfolio["uuid"] = portfolio.coinUuid
                        updatePortfolio["transaction_ID"] = portfolio.transactionId
                        updatePortfolio["flag"] = if (newQuantity <= 0) "false" else "true"

                        db.collection("Users").document(userId).collection("Portfolio")
                            .document(portfolio.transactionId).set(updatePortfolio)
                            .addOnSuccessListener { }
                            .addOnFailureListener { }


                        // Add new transaction on Firestore
                        val calendar = Calendar.getInstance()
                        val dateFormat = SimpleDateFormat("dd/MM/yyyy")
                        val date = dateFormat.format(calendar.time)
                        val totalPnl = portfolio.totalPandLString.toDouble()
                        val onePnl = totalPnl / totalQuantity
                        val totalPnlValue = userQuantity * onePnl
                        val transaction = mutableMapOf<String, Any>()
                        transaction["Quantity"] = inputText
                        transaction["Price per Coin"] = portfolio.currentPrice
                        transaction["Transaction Date"] = date
                        transaction["BuyOrSell"] = "Sell"
                        transaction["CoinName"] = portfolio.coinFullName
                        transaction["CoinSymbol"] = portfolio.symbol
                        transaction["totalpandl"] = "$$totalPnlValue"
                        transaction["gainORloss"] = portfolio.gainAndLoss
                        transaction["flag"] = "true"

                        db.collection("Users").document(userId).collection("Transaction")
                            .document(totalTransactions[0]).set(transaction)
                            .addOnSuccessListener { }
                            .addOnFailureListener { }

                        // Update transaction counts by 1.
                        var currentTotalTransactions = totalTransactions[0].toInt()
                        currentTotalTransactions++
                        if (portfolio.gainAndLoss == "Loss") {
                            val currentNegativeTransactions = negativeTransactions[0].toInt()
                            negativeTransactions[0] = (currentNegativeTransactions + 1).toString()
                        } else {
                            val currentPositiveTransactions = positiveTransactions[0].toInt()
                            positiveTransactions[0] = (currentPositiveTransactions + 1).toString()
                        }
                        totalTransactions[0] = currentTotalTransactions.toString()

                        val transactions = Users(totalTransactions[0], positiveTransactions[0], negativeTransactions[0], true)
                        reference.child(userId).child("Transactions").setValue(transactions).addOnCompleteListener { }

                        Toast.makeText(context, "Sell successful", Toast.LENGTH_SHORT).show()
                        dialog?.dismiss() // Close the dialog on success

                        loadPortfolio()
                    } else {
                        Toast.makeText(context, "Can't Sell more than available coins", Toast.LENGTH_SHORT).show()
                    }
                }

                dialog?.show()

            }
        })
        recyclerView.adapter = adapter

    }


}
