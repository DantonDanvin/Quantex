package com.example.quantex.portfolio

data class Portfolio(
    val nameAndSymbol: String,
    val coinFullName: String,
    val symbol: String,
    val currentPrice: String,
    val buyPrice: String,
    val quantity: String,
    val investment: String,
    val totalPandLString: String,
    val gainAndLoss: String,
    val transactionId: String,
    val coinUuid: String
)
