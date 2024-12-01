package com.example.quantex.crypto.coinsList

data class Coin(
    val uuid: String,
    var icon: String,
    val name: String,
    val fullname: String,
    val currentPrice: String,
    val change: String
)

// use for set and get data.
// set on adaptor for recyclerView.
