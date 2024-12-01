package com.example.quantex.crypto.coin

import com.google.gson.annotations.SerializedName

data class CoinDataResponse(
    @SerializedName("data") val data: Data
)

data class Data(
    @SerializedName("coin") val coin: Coin
)

data class Coin(
    @SerializedName("uuid") val uuid: String,
    @SerializedName("color") val color: String,
    @SerializedName("symbol") val symbol: String,
    @SerializedName("name") val name: String,
    @SerializedName("price") val price: String,
    @SerializedName("change") val change: String,
    @SerializedName("description") val description: String,
    @SerializedName("rank") val rank: String,
    @SerializedName("allTimeHigh") val allTimeHigh: AllTimeHigh,
    @SerializedName("supply") val supply: Supply,
    @SerializedName("marketCap") val marketCap: String,
    @SerializedName("listedAt") val listedAt: String,
    @SerializedName("24hVolume") val c24hVolume: String,
    @SerializedName("numberOfMarkets") val numberOfMarkets: String,
    @SerializedName("numberOfExchanges") val numberOfExchanges: String,
    @SerializedName("fullyDilutedMarketCap") val fullyDilutedMarketCap: String
)

data class AllTimeHigh(
    @SerializedName("price") val price: String
)

data class Supply(
    @SerializedName("confirmed") val confirmed: String,
    @SerializedName("supplyAt") val supplyAt: String,
    @SerializedName("max") var max: String="",
    @SerializedName("total") val total: String,
    @SerializedName("circulating") val circulating: String
)

// setup all the way to call api and get data.
