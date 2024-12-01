package com.example.quantex.crypto.coin

import android.content.Context
import android.graphics.Color
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.example.quantex.R
import com.example.quantex.crypto.RetrofitCoinsClient
import com.example.quantex.databinding.ActivityCoinDataBinding
import com.example.quantex.firebaseUser.Users
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.URL
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToLong

class CoinData : AppCompatActivity() {

    private lateinit var binding: ActivityCoinDataBinding
    private lateinit var myTask: Job
    private lateinit var database: FirebaseDatabase
    private lateinit var reference: DatabaseReference
    private lateinit var db: FirebaseFirestore

    private var coinuuid: String = ""
    private var Coincolor: Int = 0
    private var Coinname: String = ""
    private var symbol: String = ""
    private var formattedPrice: String = ""
    private var userId: String = ""

    private val userfunds = arrayOf("")
    private val userportfolio = arrayOf("")
    private val totaltransactions = arrayOf("")
    private val positivetransctions = arrayOf("")
    private val negativetransactions = arrayOf("")
    var Sdebit: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coin_data)
        binding = ActivityCoinDataBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = resources.getColor(R.color.black)

        // Toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbarcoin)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Enable the back button
        // Get the custom toolbar layout
        val customToolbar: View = layoutInflater.inflate(R.layout.custom_toolbar_coindata, toolbar, false)
        // Set the custom toolbar layout as the toolbar's layout
        toolbar.addView(customToolbar)

        binding.mainlayout.visibility = View.INVISIBLE

        binding.d1.setOnClickListener {
            val color = ContextCompat.getColor(this, R.color.liteblue)
            binding.d1.setBackgroundColor(color)
            binding.w1.setBackgroundResource(R.drawable.bordergraph)
            binding.m1.setBackgroundResource(R.drawable.bordergraph)
            binding.m3.setBackgroundResource(R.drawable.bordergraph)
            binding.y1.setBackgroundResource(R.drawable.bordergraph)
            grapgtimerchange("24h")
        }

        binding.w1.setOnClickListener {
            val color = ContextCompat.getColor(this, R.color.liteblue)
            binding.d1.setBackgroundResource(R.drawable.bordergraph)
            binding.w1.setBackgroundColor(color)
            binding.m1.setBackgroundResource(R.drawable.bordergraph)
            binding.m3.setBackgroundResource(R.drawable.bordergraph)
            binding.y1.setBackgroundResource(R.drawable.bordergraph)
            grapgtimerchange("7d")
        }

        binding.m1.setOnClickListener {
            val color = ContextCompat.getColor(this, R.color.liteblue)
            binding.d1.setBackgroundResource(R.drawable.bordergraph)
            binding.w1.setBackgroundResource(R.drawable.bordergraph)
            binding.m1.setBackgroundColor(color)
            binding.m3.setBackgroundResource(R.drawable.bordergraph)
            binding.y1.setBackgroundResource(R.drawable.bordergraph)
            grapgtimerchange("30d")
        }

        binding.m3.setOnClickListener {
            val color = ContextCompat.getColor(this, R.color.liteblue)
            binding.d1.setBackgroundResource(R.drawable.bordergraph)
            binding.w1.setBackgroundResource(R.drawable.bordergraph)
            binding.m1.setBackgroundResource(R.drawable.bordergraph)
            binding.m3.setBackgroundColor(color)
            binding.y1.setBackgroundResource(R.drawable.bordergraph)
            grapgtimerchange("3m")
        }

        binding.y1.setOnClickListener {
            val color = ContextCompat.getColor(this, R.color.liteblue)
            binding.d1.setBackgroundResource(R.drawable.bordergraph)
            binding.w1.setBackgroundResource(R.drawable.bordergraph)
            binding.m1.setBackgroundResource(R.drawable.bordergraph)
            binding.m3.setBackgroundResource(R.drawable.bordergraph)
            binding.y1.setBackgroundColor(color)
            grapgtimerchange("1y")
        }


        // click buy
        binding.buy.setOnClickListener {
            showCustomDialog(this@CoinData)
        }

        // click sell
        binding.sell.setOnClickListener {
//            onBackPressed()
            Toast.makeText(this@CoinData, "Go to the Portfolio to sell.", Toast.LENGTH_SHORT).show()
        }


    } // end of Oncreate

    override fun onStart() {
        super.onStart()
        // Start your task here
        myTask = CoroutineScope(Dispatchers.Main).launch {
            // Your task code

            Handler(Looper.getMainLooper()).postDelayed({
                // Coin Data
                try {
                    queryData()
                } catch (e: IOException) {
                    throw RuntimeException(e)
                }
            }, 3000)

            Handler(Looper.getMainLooper()).postDelayed({
                //  Coin history default
                try {
                    grapgtimerchange("24h")
                } catch (e: IOException) {
                    throw RuntimeException(e)
                }
            }, 1000)

            // get current user ID
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                userId = currentUser.uid
            }


            // get user data "Funds" from realtime database
            FirebaseDatabase.getInstance().getReference("Users").child(userId).child("Funds")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        var i = 0
                        for (snapshot in dataSnapshot.children) {
                            if (i == 0) {
                                userfunds[0] = snapshot.value.toString()
                            }
                            i++
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@CoinData, error.message , Toast.LENGTH_SHORT).show()
                    }
                })

            // user Portfolio
            FirebaseDatabase.getInstance().getReference("Users").child(userId).child("Portfolio")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        var i = 0
                        for (snapshot in dataSnapshot.children) {
                            if (i == 0) {
                                userportfolio[0] = snapshot.value.toString()
                            }
                            i++
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
//                        Toast.makeText(this@CoinData, error.message , Toast.LENGTH_SHORT).show()
                    }
                })

            // User transactions
            FirebaseDatabase.getInstance().getReference("Users").child(userId).child("Transactions")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        var i = 0
                        for (snapshot in dataSnapshot.children) {
                            when (i) {
                                0 -> negativetransactions[0] = snapshot.value.toString()
                                1 -> positivetransctions[0] = snapshot.value.toString()
                                2 -> totaltransactions[0] = snapshot.value.toString()
                            }
                            i++
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
//                        Toast.makeText(this@CoinData, error.message , Toast.LENGTH_SHORT).show()
                    }
                })



        }
    }



    // Coin Data
    private fun queryData() {
        coinuuid = intent.getStringExtra("COIN_UUID") ?: ""
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitCoinsClient.instance.getCoinData("https://api.coinranking.com/v2/coin/$coinuuid").execute()
                if (response.isSuccessful) {
                    val coinDataResponse = response.body()
                    withContext(Dispatchers.Main) {
                        Handler(Looper.getMainLooper()).postDelayed({
                        binding.mainlayout.visibility = View.VISIBLE
                        binding.progressBar.visibility = View.GONE
                        }, 1000)
                        setCoinData(coinDataResponse?.data?.coin)
                    }
                } else {
                    withContext(Dispatchers.Main) {
//                        Toast.makeText(this@CoinData, "Exception (on response)", Toast.LENGTH_SHORT).show()
                        binding.progressBar.visibility=View.VISIBLE
                        binding.mainlayout.visibility=View.GONE
                        Handler(Looper.getMainLooper()).postDelayed({
                            queryData()
                        }, 7000)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@CoinData, "Exception: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setCoinData(coin: Coin?) {
        coin?.let {
            try {
                var bcolor = it.color
                if(bcolor.length<=4){bcolor="#5B000000"}
                Coinname = it.name
                if (Coinname.length > 12) {
                    binding.name.width = 500
                    binding.name.isSelected = true
                }
                symbol = it.symbol
                val priceStr = intent.getStringExtra("COIN_PRICE") ?: ""
                val price = priceStr.toDouble()
                formattedPrice = String.format("%.2f", price)
                val change = it.change
                val changeAmount = change.toDouble()
                val percentageChange = (changeAmount * price) / 100
                val decimalFormat = DecimalFormat("#0.00")
                val percentageChangeStr = decimalFormat.format(percentageChange)
                val description = it.description ?: "null"
                val rank = it.rank
                val allTimeHigh = it.allTimeHigh.price ?: "null"
                val supply = it.supply
                val confirmed = supply.confirmed ?: "null"
                val supplyAt = supply.supplyAt ?: "null"
                val max = supply.max ?: "null"
                val total = supply.total ?: "null"
                val circulating = supply.circulating ?: "null"
                val marketCap = it.marketCap ?: "null"
                val listedAt = it.listedAt ?: "null"
                val c24hVolume = it.c24hVolume ?: "null"
                val numberOfMarkets = it.numberOfMarkets ?: "null"
                val numberOfExchanges = it.numberOfExchanges ?: "null"
                val fullyDilutedMarketCap = it.fullyDilutedMarketCap ?: "null"

                // icon
                // it'll load both PNG and SVG image.
                val src: ImageView = findViewById(R.id.icon)
                val iconUrl = intent.getStringExtra("COIN_ICON") ?: ""
                val imageLoader = ImageLoader.Builder(this)
                    .components { add(SvgDecoder.Factory()) }
                    .build()
                val request = ImageRequest.Builder(this)
                    .crossfade(true)
                    .crossfade(500)
                    .placeholder(R.drawable.loading)
                    .error(R.drawable.loading)
                    .data(iconUrl)
                    .target(src)
                    .build()
                imageLoader.enqueue(request)

                // Set it
                Coincolor = Color.parseColor(bcolor)
                binding.name.text = Coinname
                binding.symbol.text = "($symbol)"
                binding.price.text = "$$formattedPrice"

                if (changeAmount < 0) {
                    binding.changes.setTextColor(ContextCompat.getColor(this@CoinData, R.color.red_my))
                    binding.change.setTextColor(ContextCompat.getColor(this@CoinData, R.color.red_my))
                } else {
                    binding.changes.setTextColor(ContextCompat.getColor(this@CoinData, R.color.green))
                    binding.change.setTextColor(ContextCompat.getColor(this@CoinData, R.color.green))
                }

                binding.changes.text = percentageChangeStr
                binding.change.text = "$change%"
                binding.description.text = description

                // Table
                binding.UUID.text = coinuuid
                binding.rank.text = rank
                binding.allTimeHigh.text = allTimeHigh
                binding.Change.text = percentageChangeStr
                binding.confirmed.text = confirmed
                binding.supplyAt.text = supplyAt
                binding.max.text = max
                binding.total.text = total
                binding.circulating.text = circulating
                binding.marketCap.text = marketCap
                binding.listedAt.text = listedAt
                binding.c24hVolume.text = c24hVolume
                binding.numberOfMarkets.text = numberOfMarkets
                binding.numberOfExchanges.text = numberOfExchanges
                binding.fullyDilutedMarketCap.text = fullyDilutedMarketCap

                binding.buy.setBackgroundColor(Coincolor)
                binding.sell.setBackgroundColor(Coincolor)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }



    // Coin history
    private fun queryDatagraph(needtime: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                coinuuid = intent.getStringExtra("COIN_UUID").toString()
                val url = "https://api.coinranking.com/v2/coin/$coinuuid/history?timePeriod=$needtime"
                val response = RetrofitCoinsClient.instance.getCoinHistory(url).execute()
                if (response.isSuccessful) {
                    val historyResponse = response.body()
                    withContext(Dispatchers.Main) {
                        if (historyResponse != null) {
                            setGraphData(historyResponse.data.history, historyResponse.data.change)
                        }
                    }
                } else { // Handle unsuccessful response
                    withContext(Dispatchers.Main) {
//                        Toast.makeText(this@CoinData, "Exception (on response)", Toast.LENGTH_SHORT).show()
                        Handler(Looper.getMainLooper()).postDelayed({
                            queryDatagraph("24h")
                        }, 6000)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@CoinData,"Exception: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setGraphData(historyList: List<HistoryEntry>?, change: String) {
        historyList?.let {

                // set change.
                val changeAmount = change.toDouble()
                if (changeAmount < 0) {
                    binding.changegraph.setTextColor(Color.RED)
                } else {
                    binding.changegraph.setTextColor(Color.GREEN)
                }
                binding.changegraph.text = "Change: $change%"


                // graph
                val rentries = mutableListOf<Entry>()
                var count: Long = 0
                val len = historyList.size - 1
                var x = 0
                while (count <= len) {
                    val priceStr = historyList[x].price
                    val price = priceStr.toDouble()

                    rentries.add(Entry(count.toFloat(), price.toFloat()))
                    count++
                    x++
                }

                val entries = mutableListOf<Entry>()
                rentries.reversed().forEachIndexed { index, entry ->
                    entries.add(Entry(index.toFloat(), entry.y))
                }

                val dataSet = LineDataSet(entries, "Label")
                dataSet.color = Color.BLUE
                dataSet.valueTextColor = Color.BLACK
                val lineData = LineData(dataSet)

                // Find the line chart in the layout
                val lineChart = binding.root.findViewById<LineChart>(R.id.line_chart)
                lineChart.data = lineData
                val description = Description()
                description.text = "Coin History"
                lineChart.description = description

                val xAxis = lineChart.xAxis
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.setDrawGridLines(true)
                xAxis.axisMinimum = 0f

                val yAxis = lineChart.axisLeft
                yAxis.setDrawLabels(false)
                yAxis.setDrawGridLines(false)

                val rightAxis = lineChart.axisRight
                rightAxis.setDrawLabels(true)
                rightAxis.setDrawGridLines(true)

                val legend = lineChart.legend
                legend.isEnabled = false

                lineChart.invalidate()

        }
    }


    // graph timer change.
    private fun grapgtimerchange(time: String) {
        try {
            queryDatagraph(time)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

        binding.graphtime.visibility = View.INVISIBLE
        binding.wait.visibility = View.VISIBLE

        Handler().postDelayed({
            binding.graphtime.visibility = View.VISIBLE
            binding.wait.visibility = View.INVISIBLE
        }, 4000)
    }


    // Buying Dialog
    private fun showCustomDialog(context: Context) {
        val builder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflate(R.layout.buycoindialog, null)
        builder.setView(dialogView)
        val dialog = builder.create()

        val textViewDialogTitle: TextView = dialogView.findViewById(R.id.textViewDialogTitle)
        textViewDialogTitle.text = "Buy Coin"

        val name: TextView = dialogView.findViewById(R.id.coinname)
        if(Coinname.length>12){
            name.width=500
            name.setSelected(true)
        }
        name.text = Coinname

        val Coinsymboll: TextView = dialogView.findViewById(R.id.symbol)
        Coinsymboll.text = symbol

        val price: TextView = dialogView.findViewById(R.id.currentprice)
        price.text = "$$formattedPrice"

        val funds: TextView = dialogView.findViewById(R.id.funds)
        funds.text = "$${userfunds[0]}"

        val num1 = formattedPrice.toDouble()
        val num2 = userfunds[0].toDouble()
        val result = num2 / num1
        var capacity = result.toString()
        val dotIndex = capacity.indexOf('.')
        if (dotIndex != -1) {
            capacity = capacity.substring(0, dotIndex)
        }
        val cap: TextView = dialogView.findViewById(R.id.capacity)
        cap.text = capacity

        val coinbuy: EditText = dialogView.findViewById(R.id.noofcoin)
        coinbuy.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val inputText = coinbuy.text.toString().trim()
                if (inputText.isNotEmpty()) {
                    val currprice = formattedPrice.toDouble()
                    val buycap = inputText.toDouble()
                    val debit = currprice * buycap
                    val roundedDebit = debit.roundToLong()
                    Sdebit = roundedDebit.toString()

                    val totaldebit: TextView = dialogView.findViewById(R.id.totaldebit)
                    totaldebit.text = "$$Sdebit"
                } else {
                    val totaldebit: TextView = dialogView.findViewById(R.id.totaldebit)
                    totaldebit.text = "Enter Quantity"
                }
            }
        })

        val buy: Button = dialogView.findViewById(R.id.buy)
        buy.setBackgroundColor(Coincolor)
        buy.setOnClickListener {
            val inputText = coinbuy.text.toString().trim()
            if (inputText.isEmpty()) {
                Toast.makeText(context, "Enter Quantity", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val userQuantity = inputText.toInt()
            var capacity = result.toString()
            if (userQuantity == 0) {
                Toast.makeText(context, "0 not allowed", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (capacity == "Infinity") {
                Toast.makeText(context, "Buy successful", Toast.LENGTH_SHORT).show()
                dialog.dismiss() // Close the dialog on success
                return@setOnClickListener
            }
            val dotIndex = capacity.indexOf('.')
            if (dotIndex != -1) {
                capacity = capacity.substring(0, dotIndex)
            }
            val buyingcapacity = capacity.toInt()

            if (userQuantity <= buyingcapacity) {
                // change balance
                val currentfund = userfunds[0].toInt()
                val minusdebit = Sdebit?.toInt()
                val solve = currentfund - minusdebit!!
                val newfund = solve.toString()
                val funds = Users(newfund)
                database = FirebaseDatabase.getInstance()
                reference = database.getReference("Users")
                reference.child(userId).child("Funds").setValue(funds).addOnCompleteListener { task ->
                    Toast.makeText(context, "Available balance: $$newfund", Toast.LENGTH_SHORT).show()
                }

                // Add new collection on Firestore for transaction
                val calendar = Calendar.getInstance()
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val date = dateFormat.format(calendar.time)
                val transaction = hashMapOf(
                    "Quantity" to inputText,
                    "Price per Coin" to formattedPrice,
                    "Transaction Date" to date,
                    "BuyOrSell" to "Buy",
                    "CoinName" to Coinname,
                    "CoinSymbol" to symbol,
                    "gainORloss" to "",
                    "totalpandl" to "",
                    "flag" to "true"
                )
                db = FirebaseFirestore.getInstance()
                db.collection("Users").document(userId).collection("Transaction").document(totaltransactions[0])
                    .set(transaction)
                    .addOnSuccessListener {
                        // handle success
                    }
                    .addOnFailureListener { e ->
                        // handle failure
                    }

                // Add new collection on Firestore for Portfolio
                val portfolio = hashMapOf(
                    "Quantity" to inputText,
                    "Buy Price" to formattedPrice,
                    "uuid" to coinuuid,
                    "flag" to "true",
                    "transaction_ID" to userportfolio[0]
                )
                db.collection("Users").document(userId).collection("Portfolio").document(userportfolio[0])
                    .set(portfolio)
                    .addOnSuccessListener {
                        // handle success
                    }
                    .addOnFailureListener { e ->
                        // handle failure
                    }

                // change no of total transactions, increase by 1
                var currentTotalTransactions = totaltransactions[0].toInt()
                currentTotalTransactions++
                val newTotalTransactions = currentTotalTransactions.toString()
                val transactions = Users(newTotalTransactions, positivetransctions[0], negativetransactions[0], true)
                reference.child(userId).child("Transactions").setValue(transactions).addOnCompleteListener { task ->
                    // handle completion
                }

                // change no of portfolio, increase by 1
                var currentTotalPortfolio = userportfolio[0].toInt()
                currentTotalPortfolio++
                val newTotalPortfolio = currentTotalPortfolio.toString()
                val userportfolio = Users(newTotalPortfolio, true)
                reference.child(userId).child("Portfolio").setValue(userportfolio).addOnCompleteListener { task ->
                    // handle completion
                }

                Toast.makeText(context, "Buy successful", Toast.LENGTH_SHORT).show()
                dialog.dismiss() // Close the dialog on success
            } else if (userQuantity == 0) {
                Toast.makeText(context, "0 not allowed", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Need To Increase Funds", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }


    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }


    // go back
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            onBackPressed() // Go back to the previous activity
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    override fun onStop() {
        super.onStop()
        // Cancel your task when fragment is stopped
        myTask.cancel()
    }


}