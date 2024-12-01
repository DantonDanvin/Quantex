package com.example.quantex.crypto


import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitCoinsClient {
    private const val BASE_URL = "https://api.coinranking.com/v2/"

    val instance: CryptoCoinsApi by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(CryptoCoinsApi::class.java)
    }
}