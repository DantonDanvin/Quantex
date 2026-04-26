package com.example.quantex.crypto.coin

import com.google.gson.annotations.SerializedName

data class CoinHistoryResponse(
    @SerializedName("data") val data: HistoryData?
)

data class HistoryData(
    @SerializedName("history") val history: List<HistoryEntry>?,
    @SerializedName("change") val change: String = "0"
)

data class HistoryEntry(
    @SerializedName("price") val price: String = "0"
)
// setup all the way to call api and get data.
