package com.example.quantex.crypto.coin

import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
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
import kotlinx.coroutines.*
import org.json.JSONException
import java.io.IOException
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToLong

class CoinData : AppCompatActivity() {

    private lateinit var binding: ActivityCoinDataBinding
    private var dataJob: Job? = null
    private var graphJob: Job? = null
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
        binding = ActivityCoinDataBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = ContextCompat.getColor(this, R.color.splash_bg)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.bottom_nav_bg)

        // Toolbar
        val toolbar: Toolbar = binding.toolbarcoin
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.mainlayout.visibility = View.INVISIBLE

        // Time period chip click handlers
        val chips = listOf(binding.d1, binding.w1, binding.m1, binding.m3, binding.y1)
        val periods = listOf("24h", "7d", "30d", "3m", "1y")

        chips.forEachIndexed { index, chip ->
            chip.setOnClickListener {
                chips.forEach { c ->
                    c.setBackgroundResource(R.drawable.bordergraph)
                    c.setTextColor(ContextCompat.getColor(this, R.color.chip_default_text))
                }
                chip.setBackgroundResource(R.drawable.chip_selected_bg)
                chip.setTextColor(ContextCompat.getColor(this, R.color.chip_selected_text))
                changeGraphPeriod(periods[index])
            }
        }

        binding.buy.setOnClickListener { showCustomDialog(this@CoinData) }
        binding.sell.setOnClickListener {
            Toast.makeText(this@CoinData, "Go to the Portfolio to sell.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStart() {
        super.onStart()
        dataJob = lifecycleScope.launch {
            // Get current user
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) { userId = currentUser.uid }

            // Load Firebase data
            loadFirebaseData()

            // Load coin data with delay for Firebase to settle
            delay(1500)
            queryData()

            // Load default graph
            delay(500)
            changeGraphPeriod("24h")
        }
    }

    private fun loadFirebaseData() {
        FirebaseDatabase.getInstance().getReference("Users").child(userId).child("Funds")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    var i = 0
                    for (snapshot in dataSnapshot.children) {
                        if (i == 0) userfunds[0] = snapshot.value.toString()
                        i++
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })

        FirebaseDatabase.getInstance().getReference("Users").child(userId).child("Portfolio")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    var i = 0
                    for (snapshot in dataSnapshot.children) {
                        if (i == 0) userportfolio[0] = snapshot.value.toString()
                        i++
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })

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
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    // Coin Data - proper coroutine with lifecycle scope
    private fun queryData() {
        coinuuid = intent.getStringExtra("COIN_UUID") ?: ""
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitCoinsClient.instance.getCoinData("https://api.coinranking.com/v2/coin/$coinuuid").execute()
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        delay(500)
                        binding.mainlayout.visibility = View.VISIBLE
                        binding.progressBar.visibility = View.GONE
                        setCoinData(response.body()?.data?.coin)
                    } else {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.mainlayout.visibility = View.GONE
                        delay(5000)
                        queryData()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@CoinData, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setCoinData(coin: Coin?) {
        coin?.let {
            try {
                var bcolor = it.color ?: "#6C5CE7"
                if (bcolor.length <= 4) bcolor = "#6C5CE7"
                Coinname = it.name
                if (Coinname.length > 12) {
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
                val description = it.description ?: "No description available"
                val rank = it.rank
                val allTimeHigh = it.allTimeHigh.price ?: "N/A"
                val supply = it.supply
                val confirmed = supply.confirmed ?: "N/A"
                val supplyAt = supply.supplyAt ?: "N/A"
                val max = supply.max ?: "N/A"
                val total = supply.total ?: "N/A"
                val circulating = supply.circulating ?: "N/A"
                val marketCap = it.marketCap ?: "N/A"
                val listedAt = it.listedAt ?: "N/A"
                val c24hVolume = it.c24hVolume ?: "N/A"
                val numberOfMarkets = it.numberOfMarkets ?: "N/A"
                val numberOfExchanges = it.numberOfExchanges ?: "N/A"
                val fullyDilutedMarketCap = it.fullyDilutedMarketCap ?: "N/A"

                // Icon - load with Coil
                val src: ImageView = binding.icon
                val iconUrl = intent.getStringExtra("COIN_ICON") ?: ""
                val imageLoader = ImageLoader.Builder(this)
                    .components { add(SvgDecoder.Factory()) }
                    .build()
                val request = ImageRequest.Builder(this)
                    .crossfade(true).crossfade(500)
                    .placeholder(R.drawable.loading).error(R.drawable.loading)
                    .data(iconUrl).target(src).build()
                imageLoader.enqueue(request)

                Coincolor = Color.parseColor(bcolor)
                binding.name.text = Coinname
                binding.symbol.text = "($symbol)"
                binding.price.text = "$$formattedPrice"

                val lossColor = ContextCompat.getColor(this@CoinData, R.color.loss)
                val gainColor = ContextCompat.getColor(this@CoinData, R.color.gain)
                if (changeAmount < 0) {
                    binding.changes.setTextColor(lossColor)
                    binding.change.setTextColor(lossColor)
                } else {
                    binding.changes.setTextColor(gainColor)
                    binding.change.setTextColor(gainColor)
                }

                binding.changes.text = percentageChangeStr
                binding.change.text = "$change%"
                binding.description.text = description

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

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Graph - proper coroutine
    private fun queryDatagraph(needtime: String) {
        graphJob?.cancel()
        graphJob = lifecycleScope.launch(Dispatchers.IO) {
            try {
                coinuuid = intent.getStringExtra("COIN_UUID").toString()
                val url = "https://api.coinranking.com/v2/coin/$coinuuid/history?timePeriod=$needtime"
                val response = RetrofitCoinsClient.instance.getCoinHistory(url).execute()
                withContext(Dispatchers.Main) {
                    val body = response.body()
                    if (response.isSuccessful && body?.data != null) {
                        setGraphData(body.data!!.history, body.data!!.change)
                    } else {
                        delay(5000)
                        queryDatagraph("24h")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@CoinData, "Chart error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setGraphData(historyList: List<HistoryEntry>?, change: String) {
        if (historyList.isNullOrEmpty()) return

        try {
            val changeAmount = change.toDoubleOrNull() ?: 0.0
            val lossColor = ContextCompat.getColor(this, R.color.loss)
            val gainColor = ContextCompat.getColor(this, R.color.gain)
            binding.changegraph.setTextColor(if (changeAmount < 0) lossColor else gainColor)
            binding.changegraph.text = "Change: $change%"

            val rentries = mutableListOf<Entry>()
            for (i in historyList.indices) {
                val price = historyList[i].price.toDoubleOrNull() ?: continue
                rentries.add(Entry(i.toFloat(), price.toFloat()))
            }
            if (rentries.isEmpty()) return

            val entries = mutableListOf<Entry>()
            rentries.reversed().forEachIndexed { index, entry ->
                entries.add(Entry(index.toFloat(), entry.y))
            }

            val chartColor = ContextCompat.getColor(this, R.color.chart_line)
            val dataSet = LineDataSet(entries, "").apply {
                color = chartColor
                setDrawCircles(false)
                setDrawValues(false)
                lineWidth = 2f
                mode = LineDataSet.Mode.CUBIC_BEZIER
                setDrawFilled(true)
                fillColor = chartColor
                fillAlpha = 30
            }

            val lineChart = binding.lineChart
            lineChart.data = LineData(dataSet)
            lineChart.description = Description().apply { text = "" }
            lineChart.setTouchEnabled(true)
            lineChart.setScaleEnabled(false)
            lineChart.setBackgroundColor(Color.TRANSPARENT)

            lineChart.xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                setDrawLabels(false)
            }
            lineChart.axisLeft.apply { setDrawLabels(false); setDrawGridLines(false) }
            lineChart.axisRight.apply {
                setDrawGridLines(false)
                textColor = ContextCompat.getColor(this@CoinData, R.color.on_surface_variant)
            }
            lineChart.legend.isEnabled = false
            lineChart.invalidate()
        } catch (e: Exception) {
            Toast.makeText(this, "Chart error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun changeGraphPeriod(time: String) {
        queryDatagraph(time)
        binding.graphtime.visibility = View.INVISIBLE
        binding.wait.visibility = View.VISIBLE

        lifecycleScope.launch {
            delay(3000)
            binding.graphtime.visibility = View.VISIBLE
            binding.wait.visibility = View.INVISIBLE
        }
    }

    // Buy Dialog
    private fun showCustomDialog(context: Context) {
        val builder = AlertDialog.Builder(context, com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog)
        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflate(R.layout.buycoindialog, null)
        builder.setView(dialogView)
        val dialog = builder.create()

        val name: TextView = dialogView.findViewById(R.id.coinname)
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
        if (dotIndex != -1) capacity = capacity.substring(0, dotIndex)
        val cap: TextView = dialogView.findViewById(R.id.capacity)
        cap.text = capacity

        val coinbuy: EditText = dialogView.findViewById(R.id.noofcoin)
        coinbuy.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val inputText = coinbuy.text.toString().trim()
                val totaldebit: TextView = dialogView.findViewById(R.id.totaldebit)
                if (inputText.isNotEmpty()) {
                    val debit = formattedPrice.toDouble() * inputText.toDouble()
                    Sdebit = debit.roundToLong().toString()
                    totaldebit.text = "$$Sdebit"
                } else {
                    totaldebit.text = "Enter Quantity"
                }
            }
        })

        val buy: Button = dialogView.findViewById(R.id.buy)
        buy.setOnClickListener {
            val inputText = coinbuy.text.toString().trim()
            if (inputText.isEmpty()) { Toast.makeText(context, "Enter Quantity", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            val userQuantity = inputText.toInt()
            if (userQuantity == 0) { Toast.makeText(context, "0 not allowed", Toast.LENGTH_SHORT).show(); return@setOnClickListener }

            var cap2 = result.toString()
            if (cap2 == "Infinity") { dialog.dismiss(); return@setOnClickListener }
            val di = cap2.indexOf('.')
            if (di != -1) cap2 = cap2.substring(0, di)
            val buyingcapacity = cap2.toInt()

            if (userQuantity <= buyingcapacity) {
                val currentfund = userfunds[0].toInt()
                val minusdebit = Sdebit?.toInt() ?: 0
                val newfund = (currentfund - minusdebit).toString()
                database = FirebaseDatabase.getInstance()
                reference = database.getReference("Users")
                reference.child(userId).child("Funds").setValue(Users(newfund))

                val calendar = Calendar.getInstance()
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val date = dateFormat.format(calendar.time)
                val transaction = hashMapOf(
                    "Quantity" to inputText, "Price per Coin" to formattedPrice,
                    "Transaction Date" to date, "BuyOrSell" to "Buy",
                    "CoinName" to Coinname, "CoinSymbol" to symbol,
                    "gainORloss" to "", "totalpandl" to "", "flag" to "true"
                )
                db = FirebaseFirestore.getInstance()
                db.collection("Users").document(userId).collection("Transaction").document(totaltransactions[0]).set(transaction)

                val portfolio = hashMapOf(
                    "Quantity" to inputText, "Buy Price" to formattedPrice,
                    "uuid" to coinuuid, "flag" to "true", "transaction_ID" to userportfolio[0]
                )
                db.collection("Users").document(userId).collection("Portfolio").document(userportfolio[0]).set(portfolio)

                var currentTotal = totaltransactions[0].toInt()
                currentTotal++
                reference.child(userId).child("Transactions").setValue(Users(currentTotal.toString(), positivetransctions[0], negativetransactions[0], true))

                var currentPortfolio = userportfolio[0].toInt()
                currentPortfolio++
                reference.child(userId).child("Portfolio").setValue(Users(currentPortfolio.toString(), true))

                Toast.makeText(context, "Buy successful", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            } else {
                Toast.makeText(context, "Need To Increase Funds", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.show()
    }

    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            @Suppress("DEPRECATION")
            onBackPressed()
            true
        } else super.onOptionsItemSelected(item)
    }

    override fun onStop() {
        super.onStop()
        dataJob?.cancel()
        graphJob?.cancel()
    }
}