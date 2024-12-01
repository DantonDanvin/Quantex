package com.example.quantex.crypto


import com.example.quantex.crypto.coin.CoinDataResponse
import com.example.quantex.crypto.coin.CoinHistoryResponse
import com.example.quantex.crypto.coinsList.CoinResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

interface CryptoCoinsApi {
    @GET
    fun getCoinsData(@Url url: String): Call<CoinResponse> // end-point

    @GET
    fun getCoinHistory(@Url url: String): Call<CoinHistoryResponse> // end-point

    @GET
    fun getCoinData(@Url url: String): Call<CoinDataResponse>

}