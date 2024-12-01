package com.example.quantex.transactions

data class Transaction(
    val nameAndSymbol: String,
    val buyOrSell: String,
    val date: String,
    val pricePerShare: String,
    val quantity: String,
    val transactionTotal: String,
    val coinName: String,
    val gainOrLoss: String,
    val totalPandL: String
)
