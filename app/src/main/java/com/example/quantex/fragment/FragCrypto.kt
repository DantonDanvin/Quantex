package com.example.quantex.fragment

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
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
import com.facebook.shimmer.ShimmerFrameLayout
import kotlinx.coroutines.*

class FragCrypto : Fragment() {

    private lateinit var binding: FragmentCryptoBinding
    private lateinit var swipeToRefresh: SwipeRefreshLayout
    private lateinit var shimmerFrameLayout: ShimmerFrameLayout
    private lateinit var recyclerView: RecyclerView
    private val coins: ArrayList<Coin> = ArrayList()
    private lateinit var adapter: CryptoCoinAdapter
    private var fetchJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCryptoBinding.inflate(inflater, container, false)

        recyclerView = binding.recyclerview
        swipeToRefresh = binding.swipeRefreshLayout
        shimmerFrameLayout = binding.cryptoShimmer

        // Start shimmer
        shimmerFrameLayout.startShimmer()
        shimmerFrameLayout.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE

        swipeToRefresh.setOnRefreshListener {
            fetchJob?.cancel()
            shimmerFrameLayout.startShimmer()
            shimmerFrameLayout.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            queryData()
        }

        queryData()

        binding.search.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) v.clearFocus()
        }

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

    private fun queryData() {
        fetchJob = viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitCoinsClient.instance.getCoinsData("https://api.coinranking.com/v2/coins").execute()
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        setCoinData(response.body()?.data?.coins)
                    } else {
                        delay(5000)
                        queryData()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    shimmerFrameLayout.stopShimmer()
                    shimmerFrameLayout.visibility = View.GONE
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setCoinData(coinsList: List<Coinvalue>?) {
        coinsList?.let {
            coins.clear()
            for (coinObject in coinsList) {
                try {
                    val price = coinObject.price.toDoubleOrNull() ?: 0.0
                    val formattedPrice = String.format("%.2f", price)
                    coins.add(Coin(coinObject.uuid, coinObject.iconUrl, coinObject.symbol, coinObject.name, formattedPrice, coinObject.change))
                } catch (e: Exception) {
                    // Skip coins with invalid data
                }
            }

            // Hide shimmer, show list
            shimmerFrameLayout.stopShimmer()
            shimmerFrameLayout.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            swipeToRefresh.isRefreshing = false

            recyclerView.layoutManager = LinearLayoutManager(context)
            adapter = CryptoCoinAdapter(coins, object : CryptoCoinAdapter.OnItemClickListener {
                override fun onItemClick(coin: Coin) {
                    val intent = Intent(activity, CoinData::class.java).apply {
                        putExtra("COIN_UUID", coin.uuid)
                        putExtra("COIN_PRICE", coin.currentPrice)
                        putExtra("COIN_ICON", coin.icon)
                    }
                    startActivity(intent)
                    activity?.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                }
            })
            recyclerView.adapter = adapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        fetchJob?.cancel()
    }
}