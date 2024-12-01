package com.example.quantex.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.quantex.databinding.FragmentMarketBinding
import com.example.quantex.market_news.NewsHAdapter
import com.example.quantex.market_news.NewsVAdapter
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.kwabenaberko.newsapilib.NewsApiClient
import com.kwabenaberko.newsapilib.models.Article
import com.kwabenaberko.newsapilib.models.request.EverythingRequest
import com.kwabenaberko.newsapilib.models.request.TopHeadlinesRequest
import com.kwabenaberko.newsapilib.models.response.ArticleResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


class FragMarket : Fragment() {


    private lateinit var binding: FragmentMarketBinding
    private lateinit var swipeToRefresh: SwipeRefreshLayout
    private lateinit var progressIndicator: LinearProgressIndicator
    private lateinit var newsHRecyclerView: RecyclerView
    private lateinit var newsVRecyclerView: RecyclerView
    private lateinit var adapterH: NewsHAdapter
    private lateinit var adapterV: NewsVAdapter
    private var articleListH: MutableList<Article> = mutableListOf()
    private var articleListV: MutableList<Article> = mutableListOf()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMarketBinding.inflate(inflater, container, false)

        progressIndicator=binding.progressBar
        newsHRecyclerView=binding.newsHRecyclerView
        newsVRecyclerView=binding.newsVRecyclerView
        swipeToRefresh = binding.swipeRefreshLayout

        // Technology.
        binding.techno.setOnClickListener {
            getNewsH("technology", "")
        }
        // Business
        binding.buss.setOnClickListener {
            getNewsH("business", "")
        }

        setUpHRecyclerView()
        setUpVRecyclerView()

        // Search.
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                getNewsH("GENERAL", query)
                return true
            }
            override fun onQueryTextChange(newText: String): Boolean {
                if (newText.isEmpty()) {
                    getNewsH("technology", "")
                    return true
                }
                return false
            }
        })

        // reload Market data.
        swipeToRefresh.setOnRefreshListener {
            loadMarket()
            swipeToRefresh.isRefreshing = false
        }

        loadMarket()

        return binding.root
    }

    private fun loadMarket() {

            getNewsH("business","")
            getNewsV("crypto")

        }

    // progress change.
     private fun changeInProgress(show : Boolean){
        if(show) {progressIndicator.visibility = View.VISIBLE;}
        else {progressIndicator.visibility = View.INVISIBLE;}
    }


    private fun setUpHRecyclerView() {
        newsHRecyclerView.layoutManager = LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false)
        adapterH = NewsHAdapter(articleListH)
        newsHRecyclerView.adapter = adapterH
    }
    private fun setUpVRecyclerView() {
        newsVRecyclerView.layoutManager = LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false)
        adapterV = NewsVAdapter(articleListV)
        newsVRecyclerView.adapter = adapterV
    }

    // for horizontal news
    private fun getNewsH(category: String, query: String) {
        changeInProgress(true)
        val newsApiClient = NewsApiClient("aa130e470f2e40c2ba6ff0704b57ea9a")

        newsApiClient.getTopHeadlines(
            TopHeadlinesRequest.Builder()
                .language("en")
                .category(category)
                .q(query)
                .build(),
            object : NewsApiClient.ArticlesResponseCallback {
                override fun onSuccess(response: ArticleResponse) {
                        changeInProgress(false)
                        articleListH = response.articles.toMutableList()
                        if (articleListH.isEmpty()) {
                            Toast.makeText(context, "No data found", Toast.LENGTH_SHORT).show()
                        }
                        adapterH.updateData(articleListH)
                        adapterH.notifyDataSetChanged()
                }

                override fun onFailure(throwable: Throwable) {
                    Toast.makeText(context, throwable.message, Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // for vertical news
    //https://newsapi.org/v2/everything?q=crypto&apiKey=9cba679244974b64bfea30aca9d3f868
    private fun getNewsV(query: String) {
        changeInProgress(true)
        val newsApiClient = NewsApiClient("9cba679244974b64bfea30aca9d3f868")

        newsApiClient.getEverything(
           EverythingRequest.Builder()
                .q(query)
                .build(),
            object : NewsApiClient.ArticlesResponseCallback {
                override fun onSuccess(response: ArticleResponse) {
                    changeInProgress(false)
                    articleListV = response.articles.toMutableList()
                    if (articleListV.isEmpty()) {
                        Toast.makeText(context, "No data found", Toast.LENGTH_SHORT).show()
                    }
                    adapterV.updateData(articleListV)
                    adapterV.notifyDataSetChanged()
                }

                override fun onFailure(throwable: Throwable) {
                    Toast.makeText(context, throwable.message, Toast.LENGTH_SHORT).show()
                }
            }
        )
    }


}