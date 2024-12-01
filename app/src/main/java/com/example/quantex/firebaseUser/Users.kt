package com.example.quantex.firebaseUser

class Users {

    var username: String? = null
    var mail: String? = null
    var password: String? = null
    var userId: String? = null
    var funds: String? = null
    var totalTransactions: String? = null
    var positiveTransactions: String? = null
    var negativeTransactions: String? = null
    var portfolio: String? = null

    // Empty constructor for Firebase
    constructor()

    // Constructor for funds
    constructor(funds: String?) {
        this.funds = funds
    }

    // Constructor for portfolio
    constructor(portfolio: String?, flag: Boolean) {
        this.portfolio = portfolio
    }

    // Constructor for transactions
    constructor(totalTransactions: String?, positiveTransactions: String?, negativeTransactions: String?, flag: Boolean) {
        this.totalTransactions = totalTransactions
        this.positiveTransactions = positiveTransactions
        this.negativeTransactions = negativeTransactions
    }

    // SignUp constructor
    constructor(username: String?, mail: String?, password: String?) {
        this.username = username
        this.mail = mail
        this.password = password
    }

    // Getters and setters

//    var funds: String?
//        get() = this.funds
//        set(funds) {
//            this.funds = funds
//        }

//    var username: String?
//        get() = this.username
//        set(username) {
//            this.username = username
//        }

//    var mail: String?
//        get() = this.mail
//        set(mail) {
//            this.mail = mail
//        }
//
//    var password: String?
//        get() = this.password
//        set(password) {
//            this.password = password
//        }
//
//    var userId: String?
//        get() = this.userId
//        set(userId) {
//            this.userId = userId
//        }
//
//    var totalTransactions: String?
//        get() = this.totalTransactions
//        set(totalTransactions) {
//            this.totalTransactions = totalTransactions
//        }
//
//    var positiveTransactions: String?
//        get() = this.positiveTransactions
//        set(positiveTransactions) {
//            this.positiveTransactions = positiveTransactions
//        }
//
//    var negativeTransactions: String?
//        get() = this.negativeTransactions
//        set(negativeTransactions) {
//            this.negativeTransactions = negativeTransactions
//        }
//
//    var portfolio: String?
//        get() = this.portfolio
//        set(portfolio) {
//            this.portfolio = portfolio
//        }
}
