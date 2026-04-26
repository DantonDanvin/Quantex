package com.example.quantex.crypto.coin

import com.google.gson.annotations.SerializedName

data class CoinDataResponse(
    @SerializedName("data") val data: Data?
)

data class Data(
    @SerializedName("coin") val coin: Coin?
)

data class Coin(
    @SerializedName("uuid") val uuid: String = "",
    @SerializedName("color") val color: String? = null,
    @SerializedName("symbol") val symbol: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("price") val price: String = "0",
    @SerializedName("change") val change: String = "0",
    @SerializedName("description") val description: String? = null,
    @SerializedName("rank") val rank: String = "0",
    @SerializedName("allTimeHigh") val allTimeHigh: AllTimeHigh = AllTimeHigh(),
    @SerializedName("supply") val supply: Supply = Supply(),
    @SerializedName("marketCap") val marketCap: String? = null,
    @SerializedName("listedAt") val listedAt: String? = null,
    @SerializedName("24hVolume") val c24hVolume: String? = null,
    @SerializedName("numberOfMarkets") val numberOfMarkets: String? = null,
    @SerializedName("numberOfExchanges") val numberOfExchanges: String? = null,
    @SerializedName("fullyDilutedMarketCap") val fullyDilutedMarketCap: String? = null
)

data class AllTimeHigh(
    @SerializedName("price") val price: String? = null
)

data class Supply(
    @SerializedName("confirmed") val confirmed: String? = null,
    @SerializedName("supplyAt") val supplyAt: String? = null,
    @SerializedName("max") var max: String? = null,
    @SerializedName("total") val total: String? = null,
    @SerializedName("circulating") val circulating: String? = null
)

// setup all the way to call api and get data.
