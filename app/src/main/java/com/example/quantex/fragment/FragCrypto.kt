package com.example.quantex.fragment

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.quantex.R
import com.example.quantex.crypto.coinsList.Coin
import com.example.quantex.crypto.coin.CoinData
import com.example.quantex.crypto.coinsList.Coinvalue
import com.example.quantex.crypto.coinsList.CryptoCoinAdapter
import com.example.quantex.crypto.RetrofitCoinsClient
import com.example.quantex.databinding.FragmentCryptoBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FragCrypto : Fragment() {

    private lateinit var binding: FragmentCryptoBinding // View binding
    private lateinit var swipeToRefresh: SwipeRefreshLayout
    private lateinit var recyclerView: RecyclerView
    private val coins: ArrayList<Coin> = ArrayList()    // this arraylist can store one coin data from api on single element, and set into adaptor.
    private lateinit var adapter: CryptoCoinAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCryptoBinding.inflate(inflater,container,false)

        recyclerView = binding.recyclerview
        swipeToRefresh = binding.swipeRefreshLayout

        // reload crypto data.
        swipeToRefresh.setOnRefreshListener {
            try {
                queryData()
            }
            catch(e:Exception) {
                Toast.makeText(context,e.message,Toast.LENGTH_SHORT).show()
            }
        }

        // by-default call.
        try {
            queryData()
        }
        catch(e:Exception) {
            Toast.makeText(context,e.message,Toast.LENGTH_SHORT).show()
        }

        // change focus
        binding.search.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                v.clearFocus();
            }
        }

        // search
        binding.search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().lowercase()
                val filteredCoins = ArrayList<Coin>()
                for (coin in coins) {
                    if (coin.name.lowercase().contains(query) || coin.fullname.lowercase().contains(query)) {
                        filteredCoins.add(coin)
                    }
                }
                if (filteredCoins.isEmpty()) {
                    Toast.makeText(requireContext(), "No results found", Toast.LENGTH_SHORT).show()
                } else {
                    adapter.setCoins(filteredCoins)
                }
            }
        })

        return binding.root
    }

    // call api
    private fun queryData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitCoinsClient.instance.getCoinsData("https://api.coinranking.com/v2/coins").execute()
                if (response.isSuccessful) {
                    val coinsResponse = response.body()
                    withContext(Dispatchers.Main) {
                         setCoinData(coinsResponse?.data?.coins)
                    }
                } else {
                    // Handle unsuccessful response
                    withContext(Dispatchers.Main) {
//                        Toast.makeText(context, "Exception (on response)", Toast.LENGTH_SHORT).show()
                        onStop()
                        binding.progressBar.visibility = View.VISIBLE
                        Handler(Looper.getMainLooper()).postDelayed({
                            queryData()
                        }, 7000)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Exception: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // set data on recyclerView.
    private fun setCoinData(coinsList: List<Coinvalue>?) {
        coinsList?.let {

            coins.clear()

            for (i in coinsList.indices) {
                val coinObject = coinsList[i]
                val uuid = coinObject.uuid
                val iconUrl = coinObject.iconUrl
                val symbol = coinObject.symbol
                val name = coinObject.name
                val priceStr = coinObject.price
                val price = priceStr.toDouble()
                val formattedPrice = String.format("%.2f", price)
                val change = coinObject.change

                coins.add(Coin(uuid, iconUrl, symbol, name, formattedPrice, change))

            }

            binding.progressBar.visibility = View.GONE
            swipeToRefresh.isRefreshing = false

            recyclerView.layoutManager = LinearLayoutManager(context)
            adapter = CryptoCoinAdapter(coins, object : CryptoCoinAdapter.OnItemClickListener {
                override fun onItemClick(coin: Coin) {
                    val intent = Intent(activity, CoinData::class.java).apply {
                        putExtra("COIN_UUID", coin.uuid)
                        putExtra("COIN_PRICE",coin.currentPrice)
                        putExtra("COIN_ICON",coin.icon)
                    }
                    startActivity(intent)
                    activity?.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                }
            })
            recyclerView.adapter = adapter
        }
    }

}