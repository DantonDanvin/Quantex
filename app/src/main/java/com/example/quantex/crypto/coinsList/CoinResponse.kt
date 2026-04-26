package com.example.quantex.crypto.coinsList

import com.google.gson.annotations.SerializedName

data class CoinResponse(
    @SerializedName("data") val data: Data?
)

data class Data(
    @SerializedName("coins") val coins: List<Coinvalue>?
)

data class Coinvalue(
    @SerializedName("uuid") val uuid: String = "",
    @SerializedName("iconUrl") val iconUrl: String = "",
    @SerializedName("symbol") val symbol: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("price") val price: String = "0",
    @SerializedName("change") val change: String = "0"
)

// setup all the way to call api and get data.
